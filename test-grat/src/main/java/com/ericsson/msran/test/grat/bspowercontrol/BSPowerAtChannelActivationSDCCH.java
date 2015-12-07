package com.ericsson.msran.test.grat.bspowercontrol;

/**
 * @id GRAT-02.01:004
 * @name BS Power Control at Channel Activation (SDCCH)
 * @author GRAT XFT Meerkats
 * @created 2015-11-25
 * @description Test the transmission power control function, at channel activation (Immediate Assignment) for different BS Power levels.
 * @revision
 */


import java.util.HashMap;
import java.util.Map;
import java.lang.Math;

import java.util.concurrent.TimeUnit;

import org.json.JSONException;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.ericsson.msran.jcat.TestBase;
import se.ericsson.jcat.fw.annotations.Setup;
import se.ericsson.jcat.fw.annotations.Teardown;

import com.ericsson.abisco.clientlib.MessageQueue;
import com.ericsson.abisco.clientlib.servers.BG;
import com.ericsson.abisco.clientlib.servers.BG.Enums;
//import com.ericsson.abisco.clientlib.servers.BG.Enums.BSPower;
import com.ericsson.abisco.clientlib.servers.BG.MeasurementResult;
import com.ericsson.abisco.clientlib.servers.PARAMDISP;
import com.ericsson.abisco.clientlib.servers.PARAMDISP.ReleaseAllCalls_Result;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.Enums.BSPower;
import com.ericsson.abisco.clientlib.servers.BG.SetMeasurementReporting;
import com.ericsson.abisco.clientlib.servers.PARAMDISP.TrafficCommand_Result;
import com.ericsson.abisco.clientlib.servers.TRAFFRN;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.SACCHFilling5;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.Enums.ActivationType;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.Enums.AlgOrRate;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.Enums.ChannelRate;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.Enums.ChannelType;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.Enums.DTXDownlink;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.Enums.DTXUplink;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.Enums.RFB;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.Enums.Rbit;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.Enums.SystemInfoType;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.Enums.TypeOfCh;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.ActivationTypeStruct;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.ChannelActImmediateAssign;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.ChannelActNormalAssign;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.ChannelModeStruct;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.ChannelNoStruct;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.BSPowerStruct;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.RFChannelRelease;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.SACCHFilling6;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.TimingAdvanceStruct;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.UNACKNOWLEDGED_MESSAGE_SENTException;
import com.ericsson.commonlibrary.remotecli.Cli;
import com.ericsson.commonlibrary.resourcemanager.Rm;
import com.ericsson.commonlibrary.resourcemanager.g2.G2Rbs;
import com.ericsson.commonlibrary.restorestack.RestoreCommandStack;
import com.ericsson.msran.g2.annotations.TestInfo;
import com.ericsson.msran.helpers.Helpers;
import com.ericsson.msran.test.grat.testhelpers.AbisHelper;
import com.ericsson.msran.test.grat.testhelpers.AbiscoConnection;
import com.ericsson.msran.test.grat.testhelpers.MomHelper;
import com.ericsson.msran.test.grat.testhelpers.GsmbHelper;
import com.ericsson.msran.test.grat.testhelpers.MssimHelper;
import com.ericsson.msran.test.grat.testhelpers.NodeStatusHelper;
import com.ericsson.msran.test.grat.testhelpers.prepost.AbisPrePost;
import com.ericsson.msran.test.grat.testhelpers.prepost.PidPrePost;
import com.ericsson.msran.test.grat.testhelpers.CHNParams;
import com.ericsson.msran.test.grat.testhelpers.restorecommands.ReleaseAllCallsCommand;
import com.ericsson.mssim.gsmb.ChnComb;
import com.ericsson.mssim.gsmb.ChnMain;
import com.ericsson.mssim.gsmb.Confirmation;
import com.ericsson.mssim.gsmb.Gsmb;
import com.ericsson.mssim.gsmb.GsmbSrvMS_SET_MEAS_CMD;
import com.ericsson.mssim.gsmb.GsmphMPH_CCCH_CLOSE_REQ;
import com.ericsson.mssim.gsmb.GsmphMPH_CHN_CLOSE_REQ;
import com.ericsson.mssim.gsmb.GsmphMPH_CS_CHN_OPEN_REQ;
import com.ericsson.mssim.gsmb.Response;
import com.ericsson.mssim.gsmb.FrequencyStructure.FrStructureType;
import com.ericsson.mssim.gsmb.GsmbSrvMS_SET_MEAS_CMD.ChanBit;
import com.ericsson.mssim.gsmb.impl.GsmbFactory;
import com.ericsson.mssim.gsmb.packing.internal.PhErrorType;
import com.ericsson.abisco.clientlib.AbiscoServer.Routing;
import com.ericsson.abisco.clientlib.AbiscoClient;
import com.ericsson.abisco.clientlib.servers.OM_G31R01.RadioChannelsReleaseCommand;
import com.ericsson.abisco.clientlib.servers.OM_G31R01.RadioChannelsReleaseComplete;


public class BSPowerAtChannelActivationSDCCH extends TestBase {	
	private AbisHelper abisHelper; 
	private Gsmb gsmb;
	//private BG bgServer;
	//private RestoreCommandStack restoreStack;
	private MssimHelper mssimHelper;
	private GsmbHelper gsmbHelper;
	private MomHelper momHelper;  
	private AbiscoConnection abisco;
	private NodeStatusHelper nodeStatus;
	//private DeActivateForwardChReqToBG DeActivateForwardChReqToBGRestoreStackCmd;
	private AbiscoClient abiscoClient;
	private final short abisco_cell = 0; //default cell number	

    //from Claes
	/*
    private AbisPrePost abisPrePost;
    private PidPrePost pidPrePost;



    private TRAFFRN rslServer;
    private G2Rbs rbs;
    private Cli cli; 

    private PARAMDISP paramdisp;
    private BG bgServer;
    private SetMeasurementReporting setMeasurementReporting;
    private RestoreCommandStack restoreStack;
    private ReleaseAllCallsCommand ReleaseAllCallsRestoreStackCmd;
    */



    private final int CC_1 = 1;
    private final int CC_2 = 2;
    private final int CC_7 = 7;

    private int channelCombinations[] = new int[2];

    private final int msId[] = { 0, 1, 2, 3, 4, 5, 6, 7 }; // identifies the msid
    private long ccId[] = new long[8]; // channel identifier returned by ChActMSSIM, identifies the activated channel



    private Map<Integer, Integer> avgRfls = new HashMap<>();
    private double avgRfl[] = new double[8];
    private double expectedAvgRfl[] = new double[8];

   
    private final int Tg = 0; // the transceiver group to test
    private final int Trxc = 1; //TODO ...: find the way to read this trxc from abisco
    private short mssimCell;    
    private final short Ts = 1;
    
    private final String ChAct = "ImmediateAssign";
    private final ActivationType ActType = ActivationType.INTRA_IMM; 
    private final TypeOfCh TpOfCh = TypeOfCh.SIGNALLING;
    private final ChannelRate ChRate = ChannelRate.SDCCH;
    private final AlgOrRate AlgRate = AlgOrRate.NoResourcesRequired;
    private final short AMRCodec = 0;
    private final String PdchMode = "";
    
    
    //        Chan       => [ 'SDCCH', ],
    //        ChanSapi3  => [ 'SACCH',  ],
    //        N200       => [ '23',],
    //	    Automated  => [ 'Fully'],
    //        StabilityTime => [ 100 ],    


    /**
     * Description of test case for test reporting
     */
    @TestInfo(
            tcId = "BS Power at Channel Activation",
            slogan = "BS Power at Channel Activation",
            requirementDocument = "1/00651-FCP 130 1402",
            requirementRevision = "PC5",
            requirementLinkTested = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff87c1d941?docno=1/00651-FCP1301402Uen&format=excel8book",
            requirementLinkLatest = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff86a2af3d?docno=1/00651-FCP1301402Uen&action=current&format=excel8book",
            requirementIds = { "10565-0334/21767[PA1][PREL]", "10565-0334/19034[A][APPR]",
                    "10565-0334/19417[A][APPR]" },
            verificationStatement = "   ",
            testDescription = "   ",
            traceGuidelines = "N/A")


    /**
     * Make sure ricm patch is running, create MO:s, establish links to Abisco, and verify preconditions.
     * 
     * @throws InterruptedException
     * @throws JSONException 
     */
    @Setup
    public void setup() throws InterruptedException, JSONException {
      
    	setTestStepBegin("Setup");
        nodeStatus = new NodeStatusHelper();
        assertTrue("Node did not reach a working state before test start", nodeStatus.waitForNodeReady());
        momHelper = new MomHelper();
        abisco = new AbiscoConnection();    
        abisHelper = new AbisHelper();
        gsmb = Rm.getMssimList().get(0).getGsmb();
        mssimHelper = new MssimHelper(gsmb);
        mssimCell = mssimHelper.getMssimCellToUse();
        gsmbHelper = new GsmbHelper(gsmb);
        abiscoClient = abisHelper.getAbiscoClient();
        //bgServer = abisHelper.getBG();
        //restoreStack = Helpers.restore().restoreStackHelper().getRestoreStack();
        setTestStepEnd();
        
    }
    
    @Teardown
    public void teardown()
    {
  	  setTestStepBegin("Teardown");
      nodeStatus.isNodeRunning();
      setTestStepEnd();
    }

    /**
     * @name bsPowerAtChannelActivationSDCCH
     * 
     * @description Verifies BS power control at Channel Activation (SDCCH)
     * 
     * @param testId - unique identifier
     * @param description
     * 
     * @throws InterruptedException
     */
    @Test (timeOut = 60000000) // six minutes should be enough
    @Parameters({ "testId", "description" })
    public void bsPowerAtChannelActivationSDCCH(String testId, String description) throws InterruptedException,
            JSONException {

        setTestCase(testId, description);

        setTestStepBegin("Init MS-SIM");
        assertTrue("Failed to initiate MSSIM", gsmbHelper.mssimInit(getCurrentTestCaseName()));
        setTestStepEnd();

        setTestStepBegin("Create MOs");
        momHelper.createUnlockAllGratMos(1, 2);
        setTestStepEnd();

        setTestStepBegin("Setup Abisco");
        abisco.setupAbisco(1, 2, false);
        setTestStepEnd();

        setTestStepBegin("Start Abis MOs");
        abisHelper.completeStartup(0, 2);
        setTestStepEnd();

        setTestStepBegin("Start MS-SIM");
        assertTrue("Failed to define cell in MSSIM", gsmbHelper.mssimDefineCell(8));
        setTestStepEnd();

        // 02.01.004 BS Power control at Channel Activation (SDCCH)
        //
        // SDCCH (CC_7)
        // --------------------------------------------
        
        
        double expectedAvgRfl[] = {0, 2, 4, 8, 14, 18, 22, 26};
        BSPower power[] = { BSPower.Pn, BSPower.Pn_2dB, BSPower.Pn_4dB, BSPower.Pn_8dB, BSPower.Pn_14dB, BSPower.Pn_18dB, BSPower.Pn_22dB, BSPower.Pn_26dB};
        ChannelType chType[] = {ChannelType.SDCCH_8_0,  ChannelType.SDCCH_8_1,  ChannelType.SDCCH_8_2,  ChannelType.SDCCH_8_3,  ChannelType.SDCCH_8_4,  ChannelType.SDCCH_8_5,  ChannelType.SDCCH_8_6,  ChannelType.SDCCH_8_7};        
        
        setTestStepBegin("Setup 8 SDCCH subchannels on ts 1"); 
        
        // Setup 8 SDCCH subchannels on ts 1. 
        for(int i = 0; i < 8; i++){
        	    setTestInfo("Setup subchannels " + chType[i].toString() + " on ts 1 ");
            	ccId[i] = setupChannel(mssimCell, Trxc, Tg, chType[i], Ts, msId[i], power[i], ActType, ChRate, TpOfCh, AlgRate, AMRCodec, PdchMode);
         }

         avgRfls = gsmbHelper.getAverageRadioFrequencyLevels(mssimCell, Trxc);

/*         for (int i = 0; i < 3; i++) { // Loop for each time slot 
                // Get power levels.
                //double avgRfl = Math.abs(avgRfls.get((i + 1) * 2) & 0x7FFF);   // might be negative
                //int avgRfl1 = (avgRfls.get((i + 1) * 2));
                int avgRfl1 = (avgRfls.get(i + 1));

                //setTestInfo("Measured power level TS " + ((i + 1) * 2) + " = " + avgRfl1);
                setTestInfo("Measured power level TS " + (i + 1) + " = " + avgRfl1);
                avgRfl[i] = avgRfl1 / 256; // unit is 1/256 dBm
                //setTestInfo("Measured power level / 256 for TS " + ((i + 1) * 2) + " = " + avgRfl[i]);
                setTestInfo("Measured power level / 256 for TS " + (i + 1) + " = " + avgRfl[i]);

                // Verify power level.
                if (avgRfl[i] - avgRfl[0] - expectedAvgRfl[i] > 1) // avgRfl[2] used as reference
                {
                    fail("Difference between measured and expected values");
                }

                // Release channel.
                //releaseChannel(trxc, channelCombinations[cc], (short) ((i + 1) * 2), ccId[i]);
                releaseChannel(Trxc, channelCombinations[cc], (short)(i + 1), ccId[i]);
         }*/
         setTestStepEnd();
         
         for(int i = 0; i < 8; i++){
         	releaseChannel(Tg, Trxc, chType[i], Ts, ccId[i]);
         }


        // Cleanup.
        setTestStepBegin("Cleanup");

        // Send GsmphMPH_CCCH_CLOSE_REQ.
        GsmphMPH_CCCH_CLOSE_REQ close_REQ = GsmbFactory.getGsmphMPH_CCCH_CLOSE_REQBuilder(mssimCell).build();
        Confirmation confirmation = gsmb.send(close_REQ);
        assertEquals("GsmphMPH_CCCH_CLOSE_REQ confirmation error", confirmation.getErrorType(),
                PhErrorType.GSM_PH_ENOERR);

        // Disconnect from MS-SIM.
        gsmb.disconnect();

        setTestStepEnd();
    }

    private void releaseChannel(int tg, int trxc, ChannelType chType, short ts, long ccId) {

        // Deactivate channel in MSSIM.
        setTestInfo("Deactivate channel in MSSIM");

        GsmphMPH_CHN_CLOSE_REQ chn_closereq = GsmbFactory.getGsmphMPH_CHN_CLOSE_REQBuilder(ccId).timeout(20).build();
        Confirmation confirmation1 = gsmb.send(chn_closereq);
        assertEquals("gsmphMPH_CHN_CLOSE_REQ confirmation error", PhErrorType.GSM_PH_ENOERR,
                confirmation1.getErrorType());

        // Release channel in Abisco.
        setTestInfo("Send RFChannelRelease");

        TRAFFRN traffrn = abisHelper.getRslServer();
        RFChannelRelease channelRel = traffrn.createRFChannelRelease();

        Routing routing = new Routing();
        routing.setTG(tg);
        routing.setTRXC(trxc);
        channelRel.setRouting(routing);

        ChannelNoStruct channelnostruct = new ChannelNoStruct();
        channelnostruct.setTimeSlotNo(ts);
        channelnostruct.setChannelType(chType);
        channelRel.setChannelNoStruct(channelnostruct);

        try
        {
            // RFChannelReleaseAck response = command.send();
            channelRel.send();
        } catch (InterruptedException e)
        {
            fail("Failed sending message RFChannelRelease ", e);
        }
		
	}

	private long setupChannel(short cell, int trxc, int tg, ChannelType chType, short ts, int msId, BSPower bsPower, ActivationType actType, ChannelRate chRate, TypeOfCh tpOfCh, AlgOrRate algRate, short amrCodec, String pdchMode){

        setTestInfo("Channel Activation SDCCH, = " + chType.toString() +
                ", time slot = " + ts +
                ", msId = " + msId +
                ", BS power = " + bsPower.getValue());


        // Activate channel in MSSIM.
        setTestInfo("Activate channel in MSSIM");

        long ccId = gsmbHelper.chActMSSIM(cell,
                tg,
                trxc,
                ts,
                msId,
                chType,
                false, // bypass = false
                true, // allSacchSI
                chRate,
                tpOfCh,
                algRate,
                actType,
                amrCodec,
                pdchMode); 

            setTestInfo("Send ChannelActImmediateAssign");
            ChannelActImmediateAssign chActImAssign = channelActivationImmediateAssign (tg, trxc, Ts, chType, ActType, ChRate, TpOfCh, AlgRate, bsPower);

            try {
            	chActImAssign.send();  
            } 
            catch (InterruptedException e){
              fail("Failed sending message ChannelActImmediateAssign. ", e);  
            }

        return ccId;
    }    
    
    
    //To activate a channel in the BTS
    private ChannelActImmediateAssign channelActivationImmediateAssign(int tg, int trxc, short ts, ChannelType chType, ActivationType actType, ChannelRate chRate, TypeOfCh typeOfCh, AlgOrRate algOrRate, BSPower bsPower){
    
    	ChannelActImmediateAssign channelActCmd = abiscoClient.getTRAFFRN().createChannelActImmediateAssign();

        Routing routing = new Routing();
        routing.setTG(tg);
        routing.setTRXC(trxc);
        channelActCmd.setRouting(routing);

        ChannelNoStruct channelnostruct = new ChannelNoStruct();
        channelnostruct.setTimeSlotNo(ts);
        channelnostruct.setChannelType(chType);
        channelActCmd.setChannelNoStruct(channelnostruct);

        ActivationTypeStruct activationtypestruct = new ActivationTypeStruct();
        activationtypestruct.setActivationType(actType);
        channelActCmd.setActivationTypeStruct(activationtypestruct);

        ChannelModeStruct channelmodestruct = new ChannelModeStruct();
        channelmodestruct.setTypeOfCh(typeOfCh);
        channelmodestruct.setChannelRate(chRate);
        channelmodestruct.setAlgOrRate(algOrRate);
        channelActCmd.setChannelModeStruct(channelmodestruct);
        
        if (bsPower != BSPower.OUT_OF_BOUNDS) // OUT_OF_BOUNDS means sending without bsPower
        {
            BSPowerStruct bspowerstruct = new BSPowerStruct();
            bspowerstruct.setBSPower(bsPower);
            bspowerstruct.setReserved(0);
            channelActCmd.setBSPowerStruct(bspowerstruct);
        }        
		return channelActCmd;
	}

  
/*  #=============================================================================
    # Subroutine: RadioChReleaseCommand
    # Purpose:    Cleans all radiochannels connected to TG
    # Input:      TG
    #             TRXC
    # Output:     TC result value
    #=============================================================================  */
    
  private RadioChannelsReleaseComplete  RadioChReleaseCommand(int tg, int trxc) throws InterruptedException {
	  
	  RadioChannelsReleaseCommand command = abiscoClient.getOM_G31R01().createRadioChannelsReleaseCommand();

	  Routing routing = new Routing();
	  routing.setTG(tg);
	  routing.setTRXC(trxc);
	  command.setRouting(routing);

/*	  command.setMOClass(OM_G31R01.Enums.MOClass.SCF);
	  command.setAssociatedSOInstance(255);
	  command.setInstanceNumber(0);*/

	  RadioChannelsReleaseComplete response = command.send(); 
	  return response;
  }
  
}
