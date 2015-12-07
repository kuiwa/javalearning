package com.ericsson.msran.test.grat.bspowercontrol;

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
	import com.ericsson.abisco.clientlib.servers.BG.Enums.BSPower;
	import com.ericsson.abisco.clientlib.servers.BG.MeasurementResult;
	import com.ericsson.abisco.clientlib.servers.OM_G31R01;
	import com.ericsson.abisco.clientlib.servers.PARAMDISP;
	import com.ericsson.abisco.clientlib.servers.PARAMDISP.ReleaseAllCalls_Result;
	import com.ericsson.abisco.clientlib.servers.TRAFFRN;
	import com.ericsson.abisco.clientlib.servers.BG.SetMeasurementReporting;
	import com.ericsson.abisco.clientlib.servers.PARAMDISP.TrafficCommand_Result;
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

	public class AtChannelActivation006 extends TestBase {
	    /**
	     * @id ChannelActivationBsPowerControl
	     * @name ChannelActivationBsPowerControl
	     * @description This test class verifies the BS power at Channel Activation
	     */

	    private AbisPrePost abisPrePost;
	    private PidPrePost pidPrePost;
	    private AbisHelper abisHelper;
	    private MomHelper momHelper;
	    private Gsmb gsmb;
	    private MssimHelper mssimHelper;
	    private GsmbHelper gsmbHelper;
	    private AbiscoConnection abisco;
	    private short mssimCell;

	    private TRAFFRN rslServer;
	    private G2Rbs rbs;
	    private Cli cli;
	    private NodeStatusHelper nodeStatus;

	    private PARAMDISP paramdisp;
	    private BG bgServer;
	    private SetMeasurementReporting setMeasurementReporting;
	    private RestoreCommandStack restoreStack;
	    private ReleaseAllCallsCommand ReleaseAllCallsRestoreStackCmd;

	    private final int tg = 0; // the transceiver group to test
	    private int trxc = 1;
	    private short cell;

	    private int channelCombinations[] = new int[2];

	    private final int CC_1 = 1;
	    private final int CC_2 = 2;
	    private final int CC_7 = 7;

	    private Map<Integer, Integer> avgRfls = new HashMap<>();

	    private int ts[] = new int[8];
	    private final int msId[] = { 0, 1, 2, 3, 4, 5, 6, 7 }; // identifies the msid
	    private TRAFFRN.Enums.BSPower power[] = new TRAFFRN.Enums.BSPower[8];
	    private double powerLevel[] = new double[8];
	    private double expectedPowerLevel[] = new double[8];
	    private long ccId[] = new long[8]; // channel identifier returned by ChActMSSIM, identifies the activated channel

	    private ChannelType chType;
	    private ChannelRate chRate;
	    private TypeOfCh typeOfCh;
	    private AlgOrRate algOrRate;

	    private final short AMRCodec = 0;
	    private final String PdchMode = "";

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

	        abisHelper = new AbisHelper();
	        abisco = new AbiscoConnection();
	        gsmb = Rm.getMssimList().get(0).getGsmb();
	        mssimHelper = new MssimHelper(gsmb);
	        gsmbHelper = new GsmbHelper(gsmb);
	        mssimCell = mssimHelper.getMssimCellToUse();
	        momHelper = new MomHelper();
	        bgServer = abisHelper.getBG();

	        setTestStepEnd();
	    }

	    /**
	     * Postcond.
	     * 
	     * @throws InterruptedException
	     */
	    @Teardown
	    public void teardown() throws InterruptedException {
	        setTestStepBegin("Teardown");
	        nodeStatus.isNodeRunning();
	        setTestStepEnd();
	    }

	    /**
	     * @name bsPowerAtChannelActivation
	     * @description Verifies BS power at Channel Activation
	     * @param testId - unique identifier
	     * @param description
	     * @throws InterruptedException
	     */
	    @Test(timeOut = 60000000)
	    // six minutes should be enough
	    @Parameters({ "testId", "description" })
	    public void bsPowerAtChannelActivation006(String testId, String description) throws InterruptedException,
	            JSONException {

	        setTestCase(testId, description);

	        cell = mssimCell;

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

	        // 02.01.006 BS Power below allowed level
	        //
	        // Sub test case 1 - Full rate TCH channel Bm (CC_1)
	        // Sub test case 2 - Half rate TCH channel Lm (CC_2)
	        // -------------------------------------------------
	        trxc = 1;
	        channelCombinations[0] = CC_1;
	        channelCombinations[1] = CC_2;

	        ts[0] = 2;
	        ts[1] = 7;
	        
	        power[0] = TRAFFRN.Enums.BSPower.Pn;      // power level for TS 2
	        power[1] = TRAFFRN.Enums.BSPower.Pn_30dB; // power level for TS 7
	        
	        expectedPowerLevel[0] = 0;
	        expectedPowerLevel[1] = 0;  // should be = powerLevel[0]
	        
	        // CC_1
	        // Setup channels on TS 2 and 7.
	        setTestStepBegin("02.01.006 BS Power below allowed level, CC_1");

	        for (int i = 0; i < 2; i++) // Loop for each time slot
	        {
	            ccId[i] = setupChannel(trxc, CC_1, (short)ts[i], msId[i], power[i]);
	        }

	        avgRfls = gsmbHelper.getAverageRadioFrequencyLevels(cell, trxc);

	        for (int i = 0; i < 2; i++) // Loop for each time slot
	        {
	            // Get power levels.
	            //double avgRfl = Math.abs(avgRfls.get(i) & 0x7FFF);   // might be negative
	            int avgRfl = avgRfls.get(ts[i]);
	            powerLevel[i] = avgRfl / 256; // unit is 1/256 dBm
	            setTestInfo("avgRfl for TS " + ts[i] + " = " + avgRfl + ", power level = " + powerLevel[i] + " (avgRfl / 256)");

	            // Verify power levels.
	            if (powerLevel[i] - powerLevel[0] - expectedPowerLevel[i] > 1) // powerLevel[0] used as reference
	            {
	                fail("Difference between measured and expected values");
	            }

	            // Release channel.
	            releaseChannel(trxc, CC_1, (short)ts[i], ccId[i]);
	        }

	        setTestStepEnd();

	        // CC_2
	        // Setup channels on TS 0, 1, 2, ...           Half rate not implemented yet.


	        // Cleanup.
	        setTestStepBegin("Cleanup");
	        
	        // Disconnect from MS-SIM.
	        gsmb.disconnect();
	        setTestStepEnd();
	    }

	    private long setupChannel(int trxc, int channelCombination, short ts, int msId, TRAFFRN.Enums.BSPower bsPower)
	            throws InterruptedException {
	        setTestInfo("Channel Activation, channel combination = " + channelCombination +
	                ", time slot = " + ts +
	                ", msId = " + msId +
	                ", BS power = " + bsPower.getValue());

	        // Set channel parameters depending on specified channel combination (CC_1, ...).
	        if (channelCombination == CC_1)
	        {
	            chType = TRAFFRN.Enums.ChannelType.Bm;
	            chRate = TRAFFRN.Enums.ChannelRate.Bm;
	            typeOfCh = TypeOfCh.SPEECH;
	            algOrRate = AlgOrRate.GSM1;
	        }
	        else if (channelCombination == CC_2)
	        {
	            chType = TRAFFRN.Enums.ChannelType.Lm_0;
	            chRate = TRAFFRN.Enums.ChannelRate.Lm;
	            typeOfCh = TypeOfCh.SPEECH;
	            algOrRate = AlgOrRate.GSM1;
	        }
	        else // channelCombination == CC_7
	        {
	            chType = TRAFFRN.Enums.ChannelType.SDCCH_8_0;
	            chRate = TRAFFRN.Enums.ChannelRate.SDCCH;
	            typeOfCh = TypeOfCh.SIGNALLING;
	            algOrRate = AlgOrRate.NoResourcesRequired;
	        }

	        // Activate channel in MSSIM.
	        setTestInfo("Activate channel in MSSIM");

	        long ccId = gsmbHelper.chActMSSIM(cell,
	                tg,
	                trxc,
	                (short) ts,
	                msId,
	                chType,
	                false, // bypass = false
	                true, // allSacchSI
	                chRate,
	                typeOfCh,
	                algOrRate,
	                ActivationType.INTRA_NOR,
	                AMRCodec,
	                PdchMode);

	        // Send Channel activation from Abisco.
	        setTestInfo("Send ChannelActNormalAssign/ChannelActImmediateAssign");

	        TRAFFRN traffrn = abisHelper.getRslServer();
	        ChannelActNormalAssign channelAct = traffrn.createChannelActNormalAssign();

	        Routing routing = new Routing();
	        routing.setTG(tg);
	        routing.setTRXC(trxc);
	        channelAct.setRouting(routing);

	        ChannelNoStruct channelNoStruct = new ChannelNoStruct();
	        channelNoStruct.setTimeSlotNo(ts);
	        channelNoStruct.setChannelType(chType);
	        channelAct.setChannelNoStruct(channelNoStruct);

	        ActivationTypeStruct activationTypeStruct = new ActivationTypeStruct();
	        activationTypeStruct.setReserved(0);
	        activationTypeStruct.setRFB(RFB.Fixed);
	        activationTypeStruct.setRbit(Rbit.Activate);
	        activationTypeStruct.setActivationType(ActivationType.INTRA_NOR);
	        channelAct.setActivationTypeStruct(activationTypeStruct);

	        ChannelModeStruct channelModeStruct = new ChannelModeStruct();
	        channelModeStruct.setDTXUplink(DTXUplink.Off);
	        channelModeStruct.setDTXDownlink(DTXDownlink.Off);
	        channelModeStruct.setReserved(0);
	        channelModeStruct.setChannelRate(chRate);
	        channelModeStruct.setTypeOfCh(typeOfCh);
	        channelModeStruct.setAlgOrRate(algOrRate);
	        channelAct.setChannelModeStruct(channelModeStruct);

	        if (bsPower != TRAFFRN.Enums.BSPower.OUT_OF_BOUNDS) // OUT_OF_BOUNDS means sending without bsPower
	        {
	            BSPowerStruct bspowerstruct = new BSPowerStruct();
	            bspowerstruct.setBSPower(bsPower);
	            bspowerstruct.setReserved(0);
	            channelAct.setBSPowerStruct(bspowerstruct);
	        }

	    	try {
	    		TRAFFRN.ChannelActAck channelActAck = channelAct.send();
	    	} catch (InterruptedException e) {
	    		fail("Failed to send ChannelActNormalAssign");
	    	}

	        return ccId;
	    }

	    private void releaseChannel(int trxc, int channelCombination, short ts, long ccId)
	    {
	        setTestInfo("Channel Release, channel combination = " + channelCombination +
	                ", time slot = " + ts);

	        // Set channel type depending on specified channel combination (CC_1, ...).
	        if (channelCombination == CC_1)
	        {
	            chType = TRAFFRN.Enums.ChannelType.Bm;
	        }
	        else if (channelCombination == CC_2)
	        {
	            chType = TRAFFRN.Enums.ChannelType.Lm_0;
	        }
	        else // channelCombination == CC_7
	        {
	            chType = TRAFFRN.Enums.ChannelType.SDCCH_8_0;
	        }

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
	}
