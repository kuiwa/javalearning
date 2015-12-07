package com.ericsson.msran.test.grat.smsp2p;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.json.JSONException;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import se.ericsson.jcat.fw.annotations.Setup;
import se.ericsson.jcat.fw.annotations.Teardown;

import com.ericsson.abisco.clientlib.MessageQueue;
import com.ericsson.abisco.clientlib.AbiscoResponse;
import com.ericsson.abisco.clientlib.servers.BG;
import com.ericsson.abisco.clientlib.servers.BG.DataIndication;
import com.ericsson.abisco.clientlib.servers.BG.ReleaseIndication;
import com.ericsson.abisco.clientlib.servers.BG.EstablishIndication;
import com.ericsson.abisco.clientlib.servers.BG.Enums.LinkId;
import com.ericsson.abisco.clientlib.servers.TRAFFRN;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.ChannelNoStruct;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.DataReqMultipurposeReq;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.Enums.ActivationType;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.Enums.AlgOrRate;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.Enums.ChannelRate;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.Enums.ChannelType;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.Enums.DTXDownlink;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.Enums.DTXUplink;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.Enums.RFB;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.Enums.Rbit;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.Enums.TypeOfCh;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.ActivationTypeStruct;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.ChannelActImmediateAssign;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.ChannelModeStruct;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.L3Information;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.LinkIdStruct;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.RFChannelRelease;
import com.ericsson.commonlibrary.resourcemanager.Rm;
import com.ericsson.msran.g2.annotations.TestInfo;
import com.ericsson.msran.jcat.TestBase;
import com.ericsson.msran.test.grat.testhelpers.AbisHelper;
import com.ericsson.msran.test.grat.testhelpers.AbiscoConnection;
import com.ericsson.msran.test.grat.testhelpers.GsmbHelper;
import com.ericsson.msran.test.grat.testhelpers.MomHelper;
import com.ericsson.msran.test.grat.testhelpers.MssimHelper;
import com.ericsson.msran.test.grat.testhelpers.NodeStatusHelper;
import com.ericsson.mssim.gsmb.Confirmation;
import com.ericsson.mssim.gsmb.EstablishMode;
import com.ericsson.mssim.gsmb.Gsmb;
import com.ericsson.mssim.gsmb.GsmbSrvMS_SET_MEAS_CMD;
import com.ericsson.mssim.gsmb.GsmphMPH_CHN_CLOSE_CNF;
import com.ericsson.mssim.gsmb.GsmphMPH_CHN_CLOSE_REQ;
import com.ericsson.mssim.gsmb.GsmphMPH_CS_CHN_OPEN_REQ;
import com.ericsson.mssim.gsmb.ChnComb;
import com.ericsson.mssim.gsmb.ChnMain;
import com.ericsson.mssim.gsmb.Indication;
import com.ericsson.mssim.gsmb.LapdmChannelType;
import com.ericsson.mssim.gsmb.LapdmDL_DATA_IND;
import com.ericsson.mssim.gsmb.LapdmDL_DATA_REQ;
import com.ericsson.mssim.gsmb.LapdmDL_ESTABLISH_REQ;
import com.ericsson.mssim.gsmb.LapdmDL_ESTABLISH_CNF;
import com.ericsson.mssim.gsmb.LapdmDL_RELEASE_REQ;
import com.ericsson.mssim.gsmb.LapdmMDL_SET_T200_REQ;
import com.ericsson.mssim.gsmb.ReleaseMode;
import com.ericsson.mssim.gsmb.Response;
import com.ericsson.mssim.gsmb.Sapi;
import com.ericsson.mssim.gsmb.FrequencyStructure.FrStructureType;
import com.ericsson.mssim.gsmb.GsmbSrvMS_SET_MEAS_CMD.ChanBit;
import com.ericsson.mssim.gsmb.impl.GsmbFactory;
import com.ericsson.mssim.gsmb.packing.internal.PhErrorType;

/**
 * @id LTE00237844
 * @name SMS Point To Point, MO
 * @author GRAT Cell
 * @created 2015-10-20
 * @description Verify the SMS Point To Point(MO) function.
 */

public class SmsPointToPointMO extends TestBase {
    private NodeStatusHelper nodeStatus;
    private Gsmb gsmb;
    private AbisHelper abisHelper;
    private MssimHelper mssimHelper;
    private GsmbHelper gsmbHelper;
    private AbiscoConnection abisco;
    private short mssimCell; 
    private BG bgServer;
    AbiscoResponse msg = null;
    long CcId = 0;
    private MomHelper momHelper;
    int arfcn = 0;
    Sapi inSapi = null;
    LapdmChannelType inChan = null;
    int t200val = 0;
    int expSapi = 0;
	LinkId expLinkId = null;
	boolean smsData = false;
    short [] SAPI3_SMS_CP_DATA_UL = {72,101,121,32,100,117,100,101,10,71,82,65,84,32,
    								105,115,32,116,104,101,32,115,104,105,116,33,33};
    List<Integer> EXP_SAPI3_SMS_CP_DATA_UL = Arrays.asList(72,101,121,32,100,117,100,101,10,71,82,65,84,32,
														   105,115,32,116,104,101,32,115,104,105,116,33,33);
    short [] CP_DATA_ACK_MO_UL = {9,4};
    List<Integer> EXP_CP_DATA_ACK_MO_UL = Arrays.asList(9,4);
    
    List<Integer> CP_DATA_ACK_MO_DL = Arrays.asList(137,4);
    short [] EXP_CP_DATA_ACK_MO_DL = {137,4};
    List<Integer> RP_DATA_ACK_MO_DL = Arrays.asList(137,1,7,3,42,65,3,0,1,0); //Delivery report
    short [] EXP_RP_DATA_ACK_MO_DL = {137,1,7,3,42,65,3,0,1,0};
    /**
     * Description of test case for test reporting
     */
    @TestInfo(
            tcId = "LTE00237844",
            slogan = "SMS Mobile Originated",
            requirementDocument = "1/00651-FCP 130 1402",
            requirementRevision = "PC5",
            requirementLinkTested = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff87c1d941?docno=1/00651-FCP1301402Uen&format=excel8book",
            requirementLinkLatest = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff86a2af3d?docno=1/00651-FCP1301402Uen&action=current&format=excel8book",
            requirementIds = { "10565-0334/21767[PA1][PREL]", "10565-0334/19034[A][APPR]",
                    "10565-0334/19417[A][APPR]" },
            verificationStatement = "Verifies GRAT-36.03:004",
            testDescription = "Verifies the SMS point to point",
            traceGuidelines = "N/A")
    /**
     * Precond.
     */
    @Setup
    public void setup() {
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
        arfcn = momHelper.getArfcnToUse();
        setTestStepEnd();
    }

    /**
     * Postcheck.
     */
    @Teardown
    public void teardown() {
    	setTestStepBegin("Teardown");
        nodeStatus.isNodeRunning();
        setTestStepEnd();
    }

    /**
     * @name SMS Point To Point, Mobile Originated
     * @description Verify the SMS point to point.
     * @param testId - String - Unique identifier of the test case.
     * @param description - String - Brief description of test case.
     * @throws JSONException
     * @throws InterruptedException
     */
    @Test(timeOut = 1800000)
    @Parameters({ "testId", "description" })
    public void SmsPointToPointMOTest(String testId, String description) throws InterruptedException,
            JSONException {
   	
        setTestCase(testId, description);
        
        setTestStepBegin("Init MS-SIM");
        assertTrue("Failed to initiate MSSIM",gsmbHelper.mssimInit(getCurrentTestCaseName(), true));
        setTestStepEnd();
        
        setTestStepBegin("Create MOs");
        momHelper.createUnlockAllGratMos(1, 1);
        setTestStepEnd();
        
        setTestStepBegin("Setup Abisco");
        abisco.setupAbisco(1, 1, false);
        setTestStepEnd();
        
        setTestStepBegin("Start Abis MOs");
        abisHelper.completeStartup(0, 1);
        setTestStepEnd();
        
        setTestStepBegin("Start MS-SIM");
        assertTrue("Failed to define cell in MSSIM",gsmbHelper.mssimDefineCell());
        setTestStepEnd();
   
        // Prepare MessageQueues for establishIndication and dataIndication
	    MessageQueue<EstablishIndication> establishIndicationQueue = bgServer.getEstablishIndicationQueue();
	    establishIndicationQueue.clear();
	    MessageQueue<DataIndication> dataIndicationQueue = bgServer.getDataIndicationQueue();
	    dataIndicationQueue.clear();
	    MessageQueue<ReleaseIndication> releaseIndicationQueue = bgServer.getReleaseIndicationQueue();
        releaseIndicationQueue.clear();
	    
        setTestStepBegin("Release BTS channel");
	    rfChannelRelease();
	    setTestStepEnd();
	    
	    setTestStepBegin("Activate BTS channel");
	    channelActivationImmediateAssign();
	    setTestStepEnd();
	    
	    setTestStepBegin("Activate MS-SIM channel");
	    chActMssim();
	    
	    // Clear list of indications
        java.util.List<Indication> listOfIndications = gsmb.getIndications();
        listOfIndications.clear();
        
	    // Setup SAPI0 to be able to activate SAPI3 link
	    inSapi = Sapi.SAPI_0;
	    inChan = LapdmChannelType.LAPDM_CH_SDCCH;
	    t200val = 220;
	    setTestStepBegin("Setup SAPI " + inSapi);
	    estReqMSSim(inSapi, inChan, t200val);
	    setTestStepEnd();
	    
	    setTestStepBegin("Verify ESTABLISH INDICATION from BTS to BSC");
	    verifyEstablishInd(establishIndicationQueue);
	    setTestStepEnd();
	    
	    // Setup SAPI3 to be able to send SMS
	    inSapi = Sapi.SAPI_3;
	    inChan = LapdmChannelType.LAPDM_CH_SDCCH;
	    t200val = 600;
	    setTestStepBegin("Setup SAPI " + inSapi);
	    estReqMSSim(inSapi, inChan, t200val);
	    setTestStepEnd();
	    
	    setTestStepBegin("Verify ESTABLISH INDICATION from BTS to BSC");
	    verifyEstablishInd(establishIndicationQueue);
	    setTestStepEnd();
	    
	    setTestStepBegin("Send SMS from MSSIM over SAPI 3");
	    sendSMSMssim(dataIndicationQueue);
	    setTestStepEnd();
	    
    	// Release SAPI 3
    	inSapi = Sapi.SAPI_3;
	    inChan = LapdmChannelType.LAPDM_CH_SDCCH;
	    setTestStepBegin("Release SAPI " + inSapi);
    	relReqMssim(inSapi, inChan);
    	setTestStepEnd();
    	
    	setTestStepBegin("Verify RELEASE INDICATION from BTS to BSC");
    	verifyReleaseInd(releaseIndicationQueue);
    	setTestStepEnd();
    	releaseIndicationQueue.clear();
    	
    	// Release SAPI 0
    	inSapi = Sapi.SAPI_0;
	    inChan = LapdmChannelType.LAPDM_CH_SDCCH;
	    setTestStepBegin("Release SAPI " + inSapi);
    	relReqMssim(inSapi, inChan);
    	setTestStepEnd();
    	
    	setTestStepBegin("Verify RELEASE INDICATION from BTS to BSC");
    	verifyReleaseInd(releaseIndicationQueue);
    	setTestStepEnd();
    	releaseIndicationQueue.clear();
    	
    	setTestStepBegin("Release MS-SIM channel");
    	chRelMssim();
    	setTestStepEnd();
    	
    	setTestStepBegin("Release BTS channel");
	    rfChannelRelease();
	    setTestStepEnd();
	   	    
    } 
	    
	
    private void channelActivationImmediateAssign() throws InterruptedException {
    	setTestInfo("Sending channelActivationImmediateAssign");
        
    	TRAFFRN traffrn = abisHelper.getRslServer();
        ChannelActImmediateAssign channelActImmediateAssign = traffrn.createChannelActImmediateAssign();
                
        ActivationTypeStruct activationTypeStruct = new ActivationTypeStruct();
        activationTypeStruct.setReserved(0);
        activationTypeStruct.setRFB(RFB.Fixed);
        activationTypeStruct.setRbit(Rbit.Activate);
        
        ChannelModeStruct channelModeStruct = new ChannelModeStruct();
        channelModeStruct.setDTXUplink(DTXUplink.Off);
        channelModeStruct.setDTXDownlink(DTXDownlink.Off);
        channelModeStruct.setReserved(0);
        channelModeStruct.setTypeOfCh(TypeOfCh.SIGNALLING);
        channelModeStruct.setAlgOrRate(AlgOrRate.NoResourcesRequired);
        
        ChannelNoStruct channelNoStruct1 = new ChannelNoStruct();
        channelNoStruct1.setTimeSlotNo(1); 
        channelNoStruct1.setChannelType(ChannelType.SDCCH_8_0); 
        activationTypeStruct.setActivationType(ActivationType.INTRA_IMM);
        channelModeStruct.setChannelRate(ChannelRate.SDCCH); 
        
        channelActImmediateAssign.setChannelNoStruct(channelNoStruct1);
        channelActImmediateAssign.setActivationTypeStruct(activationTypeStruct);
        channelActImmediateAssign.setChannelModeStruct(channelModeStruct);
        
        channelActImmediateAssign.send();        
    }
    
    private void chActMssim() throws InterruptedException {
    	
    	// Channel activation on MSSIM
        GsmphMPH_CS_CHN_OPEN_REQ cs_openreq = GsmbFactory.getGsmphMPH_CS_CHN_OPEN_REQBuilder(
        		mssimCell,
                FrStructureType.ARFCN).timeout(20)
                .ref(0)
                .chnComb(ChnComb.GSMPH_SDCCHX8_SACCHXC8)
                .chnMain(ChnMain.GSMPH_TCH_FS)
                .ts((short) 1) // requires that TS 1 is configured as SDCCH
                .sub((short)0)
                .msId(0)
                .tsc((short) 1) // 1 4
                .rxAcchOn(true)
                .allSacchSI(false)
                .sdcchByp(false)
                .facchByp(false)
                .sacchByp(false)
                .rtOutOff(false)
                .rxTrfcOn(false)
                .undecFrm(false)
                .narfcn((short) 0)
                .trxNum((short) 0) 
                .frArfcn(arfcn)
                .build();
        Confirmation confirmation = gsmb.send(cs_openreq);
        assertEquals("gsmphMPH_CS_CCH_OPEN_REQ confirmation error", PhErrorType.GSM_PH_ENOERR, confirmation.getErrorType());
            
        // get CcId from GsmphMPH_CHN_OPEN_CFN
        CcId = cs_openreq.getDetailedConfirmation(confirmation).getCcId();     
        setTestInfo("Value CcId: " + CcId);
        
        // Send MSSIM.gsmbSrvMS_SET_MEAS_CMD
        GsmbSrvMS_SET_MEAS_CMD set_meas = GsmbFactory.getGsmbSrvMS_SET_MEAS_CMDBuilder().
        		timeout(20)
        		.chan(ChanBit.GSMB_SRV_SACCH)
        		.msId(0)
        		.l2hdr((short) 2)
        		.spare(new short[] {0, 0})
        		.data(new short[] {6, 21, 56, 56, 0, 111, 5, 128, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0})
        		.build();
        Response response = gsmb.send(set_meas);
        assertTrue("gsmbSrvMS_SET_MEAS_CMD response failed", response.isSuccess());
        
    }
       
    
    private void sendSMSMssim(MessageQueue<DataIndication> dataIndicationQueue) throws InterruptedException {
    	
    	setTestInfo("Send SMS from MSSim over SAPI3 link (Mobile Originated)");
    	
    	java.util.List<Indication> listOfIndications = gsmb.getIndications();
        listOfIndications.clear();
    	
    	LapdmDL_DATA_REQ dataReq = GsmbFactory.getLapdmDL_DATA_REQBuilder().timeout(10)
    			.chan(LapdmChannelType.LAPDM_CH_SDCCH)
    			.sapi(Sapi.SAPI_3)
    			.data(SAPI3_SMS_CP_DATA_UL)
    			.build();
    	Confirmation confirmation = gsmb.send(dataReq);
        assertEquals("lapdmMDL_DATA_REQ confirmation error", PhErrorType.GSM_PH_ENOERR,confirmation.getErrorType());
        
        smsData = true;
        verifyDataInd(smsData, dataIndicationQueue);
        
        sendMultiPurposeReq(smsData);
	    
	    sleepSeconds(3);
	    
	    setTestInfo("Fetch GSMB indications");
        listOfIndications = gsmb.getIndications();
        
        for(Indication myInd: listOfIndications) {
            if(myInd instanceof LapdmDL_DATA_IND){
            	setTestInfo("CP-Ack received by MSSim");
            	short [] dataVec = ((LapdmDL_DATA_IND) myInd).getData();
            	
            	if(dataVec.length == EXP_CP_DATA_ACK_MO_DL.length ) {
            		
            		setTestInfo("Expected lenght " + EXP_CP_DATA_ACK_MO_DL.length);
            		
            		for (short i = 0; i < dataVec.length; ++i)  
                    {
            			if (dataVec[i] == EXP_CP_DATA_ACK_MO_DL[i]){
            				setTestInfo("Checking values " + dataVec[i]);
            			}else{
            				fail("Wrong value received " + dataVec[i] + " expected " +EXP_CP_DATA_ACK_MO_DL[i]);
            			}
                    }
            	}else{
            		fail("LapdmDL_DATA_IND(CP-ACK) received, but with wrong size " +dataVec.length);
            	}
 
            }else {
                fail("Did not get a LapdmDL_DATA_IND, expected CP-Ack");
            }
            
        }
        
        listOfIndications.clear();
        
        smsData = false;
        sendMultiPurposeReq(smsData);
        	    
	    sleepSeconds(3);
	    
	    setTestInfo("Fetch GSMB indications");
        listOfIndications = gsmb.getIndications();
        
        for(Indication myInd: listOfIndications) {
            if(myInd instanceof LapdmDL_DATA_IND){
            	setTestInfo("Received delivery report (RP-ACK) at mssim " + myInd.getType());
            	
            	short [] dataVec = ((LapdmDL_DATA_IND) myInd).getData();
            	
            	if(dataVec.length == EXP_RP_DATA_ACK_MO_DL.length ) {
            		
            		setTestInfo("Expected lenght " + EXP_RP_DATA_ACK_MO_DL.length);
            		
            		for (short i = 0; i < dataVec.length; ++i)
                    {
            			if (dataVec[i] == EXP_RP_DATA_ACK_MO_DL[i]){
            				setTestInfo("Checking values " + dataVec[i]);
            			}else{
            				fail("Wrong value received " + dataVec[i] + " expected " + EXP_RP_DATA_ACK_MO_DL[i]);
            			}
                    }
            	}else{
            		fail("LapdmDL_DATA_IND (RP-ACK) received, but L3 data has wrong size " +dataVec.length);
            	}
            }else {
                fail("Did not get a LapdmDL_DATA_IND, expected delivery report (RP-ACK)");
            }
            
        }
        
        //Send final ACK on delivery report
        
        LapdmDL_DATA_REQ dlDataReq = GsmbFactory.getLapdmDL_DATA_REQBuilder().timeout(10)
    			.chan(LapdmChannelType.LAPDM_CH_SDCCH)
    			.sapi(Sapi.SAPI_3)
    			.data(CP_DATA_ACK_MO_UL)
    			.build();
    	confirmation = gsmb.send(dlDataReq);
        assertEquals("lapdmMDL_DATA_REQ confirmation error", PhErrorType.GSM_PH_ENOERR,confirmation.getErrorType());
        
        smsData = false;
        verifyDataInd(smsData, dataIndicationQueue);
        
        

    }
     
    private void estReqMSSim(Sapi inSapi,LapdmChannelType inChan, int t200Val ) throws InterruptedException {
    	
    	setTestInfo("Sending establish request to MSSIM for " + inSapi + inChan);
    	
    	LapdmMDL_SET_T200_REQ setTimerReq = GsmbFactory.getLapdmMDL_SET_T200_REQBuilder()
    			.t200val(t200Val)
    			.sapi(inSapi)
    			.chan(inChan).build();
    	Confirmation confirmation = gsmb.send(setTimerReq);
        assertEquals("lapdmMDL_SET_T200_REQ confirmation error", PhErrorType.GSM_PH_ENOERR,confirmation.getErrorType());
    	
    	LapdmDL_ESTABLISH_REQ estabReq = GsmbFactory.getLapdmDL_ESTABLISH_REQBuilder()
    			.mode(EstablishMode.LAPDM_EST_NORM)
    			.sapi(inSapi)
    			.data(new short[] {})
    			.chan(inChan).build();
    	confirmation = gsmb.send(estabReq);
        assertEquals("lapdmDL_ESTABLISH_REQ confirmation error", PhErrorType.GSM_PH_ENOERR,confirmation.getErrorType());
        
        if (confirmation instanceof LapdmDL_ESTABLISH_CNF ){
        	setTestInfo("LapdmDL_ESTABLISH_CNF received" );
        }else {
        	fail("Did not get LapdmDL_ESTABLISH_CNF");
        }
        sleepSeconds(1);
    }
    
    private void sendMultiPurposeReq(boolean smsData) throws InterruptedException {
    	
    	TRAFFRN traffrn = abisHelper.getRslServer();
	    DataReqMultipurposeReq dataReqMulti = traffrn.createDataReqMultipurposeReq();
	    L3Information l3information = new L3Information();
    	
    	if (smsData){
    		setTestInfo("Sending CP DATA ACK on Abis");
    	    l3information.setData(CP_DATA_ACK_MO_DL);
    		
    	}
    	else{
            setTestInfo("Sending delivery report (RP-Ack) on Abis");
            l3information.setData(RP_DATA_ACK_MO_DL);
    	}

    	ChannelNoStruct channelnostruct = new ChannelNoStruct();
	    channelnostruct.setTimeSlotNo(1);
	    channelnostruct.setChannelType(TRAFFRN.Enums.ChannelType.SDCCH_8_0);
	    dataReqMulti.setChannelNoStruct(channelnostruct);

	    LinkIdStruct linkidstruct = new LinkIdStruct();
	    linkidstruct.setSAPI(3);
	    linkidstruct.setReserved(0);
	    linkidstruct.setNABit(0);
	    linkidstruct.setLinkId(TRAFFRN.Enums.LinkId.FACCH_SDCCH);
	    dataReqMulti.setLinkIdStruct(linkidstruct);
	    
	    dataReqMulti.setL3Information(l3information);
	    
	    dataReqMulti.sendAsync();
               
    }
    
    private void chRelMssim() throws InterruptedException {

    	setTestInfo("Release channel in MSSIM");
        GsmphMPH_CHN_CLOSE_REQ chn_closereq = GsmbFactory.getGsmphMPH_CHN_CLOSE_REQBuilder(CcId).timeout(20).build();
        Confirmation confirmation = gsmb.send(chn_closereq);
        assertEquals("gsmphMPH_CHN_CLOSE_REQ confirmation error", PhErrorType.GSM_PH_ENOERR, confirmation.getErrorType());
    	
        if (confirmation instanceof GsmphMPH_CHN_CLOSE_CNF ){
        	setTestInfo("GsmphMPH_CHN_CLOSE_CNF received");
        }else {
        	fail("Did not get GsmphMPH_CHN_CLOSE_CNF");
        }
        
    }
    
    private void relReqMssim(Sapi inSapi,LapdmChannelType inChan) throws InterruptedException {
    	
    	setTestInfo("Releasing SAPI " +inSapi);
    	    	   	
    	LapdmDL_RELEASE_REQ releaseReq = GsmbFactory.getLapdmDL_RELEASE_REQBuilder()
    			.sapi(inSapi).chan(inChan)
    			.mode(ReleaseMode.LAPDM_REL_NORM).build();
    	Confirmation confirmation = gsmb.send(releaseReq);
        assertEquals("LapdmDL_RELEASE_REQ confirmation error", PhErrorType.GSM_PH_ENOERR,confirmation.getErrorType());
     
    }
    
    
    
    private void verifyEstablishInd(MessageQueue<EstablishIndication> establishIndicationQueue) throws InterruptedException {

    	// Verifies that a Establish Indication has been sent from BTS to BSC on ABIS
        setTestInfo("ABISCO: Waiting for EstablishIndication");
        
        if (inSapi == Sapi.SAPI_0){
        	expSapi = 0;
        	expLinkId = LinkId.FACCH_SDCCH;
        }
        else{
        	expSapi = 3;
        	expLinkId = LinkId.FACCH_SDCCH;
        }
        
	    try        
	    {
	      	msg = establishIndicationQueue.poll(10, TimeUnit.SECONDS);

	        if (msg == null) 
	        {
	        	setTestInfo("EstablishIndication queue empty");
	        	setTestInfo("No EstablishIndication found!");
	           	fail("ABISCO: Did not receive EstablishIndication");
	        }
	        else
	        {
	        	setTestInfo("ABISCO: Recieved EstablishIndication");
	        	setTestInfo(msg.toString());
	        }
	    } catch (Exception e) {
	    	setTestInfo(e.toString());
	        e.printStackTrace();
	        fail("Exception occurred while waiting for EstablishIndication:");
	        }

	    setTestInfo("Checking received values in EstablishIndication");
	    if (((EstablishIndication) msg).getLinkIdStruct().getSAPI() == expSapi & 
	    		((EstablishIndication) msg).getLinkIdStruct().getLinkId() == expLinkId){
	    	setTestDebug("Received EstablishIndication with correct SAPI " + expSapi);
	    	setTestDebug("Received EstablishIndication with correct LinkId " + expLinkId);
	    }
        else{
        	fail("Wrong values in EstablishIndication received");
        	setTestDebug("Received SAPI: " + ((EstablishIndication) msg).getLinkIdStruct().getSAPI() + " expected: "  + expSapi);
        	setTestDebug("Received SAPI: " + ((EstablishIndication) msg).getLinkIdStruct().getLinkId() + " expected: "  + expLinkId);
        }
    	
    }
    
    private void verifyReleaseInd(MessageQueue<ReleaseIndication> releaseIndicationQueue) throws InterruptedException {

    	// Verifies that a Release Indication has been sent from BTS to BSC on ABIS
        setTestInfo("ABISCO: Waiting for ReleaseIndication");
        
        if (inSapi == Sapi.SAPI_0){
        	expSapi = 0;
        	expLinkId = LinkId.FACCH_SDCCH;
        }
        else{
        	expSapi = 3;
        	expLinkId = LinkId.FACCH_SDCCH;
        }
        
        try        
	    {
	      	msg = releaseIndicationQueue.poll(10, TimeUnit.SECONDS);

	        if (msg == null) 
	        {
	        	setTestInfo("ReleaseIndication queue empty");
	        	setTestInfo("No ReleaseIndication found!");
	           	fail("ABISCO: Did not receive ReleaseIndication");
	        }
	        else
	        {
	        	setTestInfo("ABISCO: Recieved ReleaseIndication");
	        	setTestInfo(msg.toString());
	        	
	        }
	    } catch (Exception e) {
	    	setTestInfo(e.toString());
	        e.printStackTrace();
	        fail("Exception occurred while waiting for ReleaseIndication:");
	      }
        setTestInfo("Checking received values in ReleaseIndication");
        if (((ReleaseIndication) msg).getLinkIdStruct().getSAPI() == expSapi & 
	    		((ReleaseIndication) msg).getLinkIdStruct().getLinkId() == expLinkId){
        	setTestDebug("Received ReleaseIndication with correct SAPI " + expSapi);
        	setTestDebug("Received ReleaseIndication with correct LinkId " + expLinkId);
	    }
        else{
        	fail("Wrong values in ReleaseIndication received");
        	setTestDebug("Received SAPI: " + ((ReleaseIndication) msg).getLinkIdStruct().getSAPI() + " expected: "  + expSapi);
        	setTestDebug("Received SAPI: " + ((ReleaseIndication) msg).getLinkIdStruct().getLinkId() + " expected: "  + expLinkId);
        }
     	
    }
    
    private void verifyDataInd(boolean smsData,MessageQueue<DataIndication> dataIndicationQueue) throws InterruptedException {

    	// Verifies that a Data Indication has been sent from BTS to BSC on ABIS
        setTestInfo("ABISCO: Waiting for DataIndication");
        List<Integer> expL3Data = null;
        
        if (smsData){
        	expSapi = 3;
        	expLinkId = LinkId.FACCH_SDCCH;
        	expL3Data = EXP_SAPI3_SMS_CP_DATA_UL;
        }        
        else{
        	expSapi = 3;
        	expLinkId = LinkId.FACCH_SDCCH;
        	expL3Data = EXP_CP_DATA_ACK_MO_UL;
        }
        
        try        
	    {
	      	msg = dataIndicationQueue.poll(10, TimeUnit.SECONDS);

	        if (msg == null) 
	        {
	        	setTestInfo("DataIndication queue empty");
	        	setTestInfo("No DataIndication found!");
	           	fail("ABISCO: Did not receive DataIndication");
	        }
	        else
	        {
	        	setTestInfo("ABISCO: Data Indication(L3 Info) received");
	        	setTestInfo(msg.toString());
	        	
	        }
	    } catch (Exception e) {
	    	setTestInfo(e.toString());
	        e.printStackTrace();
	        fail("Exception occurred while waiting for DataIndication:");
	      }
        setTestInfo("Checking received values in DataIndication");
        if (((DataIndication) msg).getLinkIdStruct().getSAPI() == expSapi & 
	    		((DataIndication) msg).getLinkIdStruct().getLinkId() == expLinkId ){
        	setTestDebug("Received DataIndication with correct SAPI " + expSapi);
        	setTestDebug("Received DataIndication with correct LinkId " + expLinkId);
        	
        	
			if( ((DataIndication) msg).getL3Data().size() == expL3Data.size()){
        		setTestDebug("Size of SMS data checked successfully");
        		
        		if (((DataIndication) msg).getL3Data().equals(expL3Data)){
        			setTestDebug("Correct L3 Data received");
        		}else{
        			fail("Wrong L3 Data received");
        		}
        	}else{
        		fail("DATA INDICATION received, but L3 data has wrong size " + ((DataIndication) msg).getL3Data().size());
        	}

	    }
        else{
        	fail("Wrong values in DataIndication received");
        	setTestDebug("Received SAPI: " + ((DataIndication) msg).getLinkIdStruct().getSAPI() + " expected: "  + expSapi);
        	setTestDebug("Received SAPI: " + ((DataIndication) msg).getLinkIdStruct().getLinkId() + " expected: "  + expLinkId);
        }
     	
    }
 
    private void  rfChannelRelease() throws InterruptedException {
    	setTestInfo("Sending rfChannelRelease on Abis");
        
    	TRAFFRN traffrn = abisHelper.getRslServer();
        RFChannelRelease rFChannelRelease = traffrn.createRFChannelRelease();
        
        ChannelNoStruct channelNoStruct = new ChannelNoStruct();   
        channelNoStruct.setTimeSlotNo(1); 
        channelNoStruct.setChannelType(ChannelType.SDCCH_8_0); 
        rFChannelRelease.setChannelNoStruct(channelNoStruct);
        
        rFChannelRelease.send();
        
    }
    
        

}