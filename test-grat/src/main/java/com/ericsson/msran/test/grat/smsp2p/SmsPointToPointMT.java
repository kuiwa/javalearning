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
import com.ericsson.abisco.clientlib.AbiscoServer.Behaviour;
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
import com.ericsson.abisco.clientlib.servers.TRAFFRN.EstablishReq;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.L3Information;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.LinkIdStruct;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.MSPowerStruct;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.RFChannelRelease;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.TimingAdvanceStruct;
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
import com.ericsson.mssim.gsmb.GsmbSrvMS_SET_MEAS_CMD.ChanBit;
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
import com.ericsson.mssim.gsmb.impl.GsmbFactory;
import com.ericsson.mssim.gsmb.packing.internal.PhErrorType;

/**
 * @id LTE00237962
 * @name SMS Point To Point, MT
 * @author GRAT Cell
 * @created 2015-04-28
 * @description Verify the SMS Point To Point(MT) function.
 */

public class SmsPointToPointMT extends TestBase {
    private NodeStatusHelper nodeStatus;
    private Gsmb gsmb;
    private AbisHelper abisHelper;
    private AbiscoConnection abisco;
    private MssimHelper mssimHelper;
    private GsmbHelper gsmbHelper;
    private short mssimCell; 
    private BG bgServer;
    AbiscoResponse msg = null;
    long CcId = 0;
    private MomHelper momHelper;
    int arfcn = 0;
    Sapi inSapi = null;
    LapdmChannelType inChan = null;
	LinkId expLinkId = null;
	List<Integer> expL3Data = null;
    int expSapi = 0;
    int t200val = 0;
	boolean smsData = false;
    List<Integer> SAPI3_SMS_CP_DATA_DL = Arrays.asList(72,101,121,32,100,117,100,101,10,71,82,65,84,32,105,115,32,116,104,101,32,115,104,105,116,33,33);
    short [] EXP_SAPI3_SMS_CP_DATA_DL = {72,101,121,32,100,117,100,101,10,71,82,65,84,32,105,115,32,116,104,101,32,115,104,105,116,33,33};
    
    List<Integer> CP_DATA_ACK_MT_DL = Arrays.asList(9,4);
    short [] EXP_CP_DATA_ACK_MT_DL = {9,4};
    
    short [] CP_DATA_ACK_MT_UL = {137,4};
    List<Integer> EXP_CP_DATA_ACK_MT_UL = Arrays.asList(137,4);
    
    short [] RP_DATA_ACK_MT_UL = {137,1,7,3,42,65,3,0,1,0};
    List<Integer> EXP_RP_DATA_ACK_MT_UL = Arrays.asList(137,1,7,3,42,65,3,0,1,0);

    /**
     * Description of test case for test reporting
     */
    @TestInfo(
            tcId = "LTE00237962",
            slogan = "SMS Mobile Terminated",
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
     * @name SMS Point To Point, Mobile Terminated
     * @description Verify the SMS point to point.
     * @param testId - String - Unique identifier of the test case.
     * @param description - String - Brief description of test case.
     * @throws JSONException
     * @throws InterruptedException
     */
    @Test(timeOut = 1800000)
    @Parameters({ "testId", "description" })
    public void SmsPointToPointMTTest(String testId, String description) throws InterruptedException,
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
	    setTestStepEnd();
	    
	    // Clear list of indications
        java.util.List<Indication> listOfIndications = gsmb.getIndications();
        listOfIndications.clear();
        
	    inSapi = Sapi.SAPI_0;
	    inChan = LapdmChannelType.LAPDM_CH_SDCCH;
	    t200val = 220;
	    setTestStepBegin("Setup SAPI " + inSapi);
	    estReqMSSim(inSapi, inChan, t200val);
	    setTestStepEnd();
	    
	    setTestStepBegin("Verify ESTABLISH INDICATION from BTS to BSC");
	    verifyEstablishInd(establishIndicationQueue);
	    setTestStepEnd();
	    
	    setTestStepBegin("Setup SAPI " + inSapi);
	    estReqBTS();
	    setTestStepEnd();
	    
	    setTestStepBegin("Send SMS from BSC over SAPI 3");
	    sendSMSBts();
	    setTestStepEnd();
	    
    	inSapi = Sapi.SAPI_3;
	    inChan = LapdmChannelType.LAPDM_CH_SDCCH;
	    setTestStepBegin("Release SAPI " + inSapi);
    	relReqMssim(inSapi, inChan,releaseIndicationQueue);
    	setTestStepEnd();
    	
    	releaseIndicationQueue.clear();
    	
    	inSapi = Sapi.SAPI_0;
	    inChan = LapdmChannelType.LAPDM_CH_SDCCH;
	    setTestStepBegin("Release SAPI " + inSapi);
    	relReqMssim(inSapi, inChan, releaseIndicationQueue);
    	setTestStepEnd();
    	
    	setTestStepBegin("Release MS-SIM channel");
    	chRelMssim();
    	setTestStepEnd();
    	
    	setTestStepBegin("Release BTS channel");
	    rfChannelRelease();
	    setTestStepEnd();
	    
    }
    
    private void sendMultiPurposeReq(boolean smsData) throws InterruptedException {

    	setTestInfo("sendMultiPurposeReq");
        setTestInfo("Preparing to send SMS (CP-Data) in downlink");
    	
    	TRAFFRN traffrn = abisHelper.getRslServer();
	    DataReqMultipurposeReq dataReqMulti = traffrn.createDataReqMultipurposeReq();
	    
	    L3Information l3information = new L3Information();
	    
	    if (smsData){
    		setTestInfo("Sending CP DATA ACK on Abis");
    	    l3information.setData(SAPI3_SMS_CP_DATA_DL);
    		
    	}
    	else{
            setTestInfo("Sending delivery report (RP-Ack) on Abis");
            l3information.setData(CP_DATA_ACK_MT_DL);
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
        setTestInfo("SMS (CP-Data) sent in downlink");

    }
    
    private void sendSMSBts() throws InterruptedException {
    	
    	setTestInfo("Send SMS over SAPI3 from Abisco");
    	
    	java.util.List<Indication> listOfIndications = gsmb.getIndications();
        listOfIndications.clear();
        MessageQueue<DataIndication> dataIndicationQueue = bgServer.getDataIndicationQueue();
	    dataIndicationQueue.clear();
        
	    smsData = true;
    	sendMultiPurposeReq(smsData);
    	
    	sleepSeconds(3);
	    
	    setTestInfo("Fetch GSMB indications");
        listOfIndications = gsmb.getIndications();
        
        for(Indication myInd: listOfIndications) {
            if(myInd instanceof LapdmDL_DATA_IND){
            	setTestInfo("SMS (CP-Data) received by MSSim");
            	short [] dataVec = ((LapdmDL_DATA_IND) myInd).getData();
            	
            	if(dataVec.length == EXP_SAPI3_SMS_CP_DATA_DL.length ) {
            		
            		setTestInfo("Expected lenght " + EXP_SAPI3_SMS_CP_DATA_DL.length);
            		
            		for (short i = 0; i < dataVec.length; ++i)
                    {
            			if (dataVec[i] == EXP_SAPI3_SMS_CP_DATA_DL[i]){
            				setTestInfo("Checking values " + dataVec[i]);
            			}else{
            				setTestInfo("Wrong value received " + dataVec[i] + " expected " + EXP_SAPI3_SMS_CP_DATA_DL[i]);
            			}
                    }
            	}else{
            		setTestInfo("Error while receiving SMS (CP-Data) in MSSim, wrong size " +dataVec.length);
            	}
 
            }else {
                fail("Did not received SMS (CP-Data), a LapdmDL_DATA_IND was expected");
            }
            
        }
        
        //Send CP-Ack (Uplink)
        LapdmDL_DATA_REQ dataReq = GsmbFactory.getLapdmDL_DATA_REQBuilder().timeout(10)
    			.chan(LapdmChannelType.LAPDM_CH_SDCCH)
    			.sapi(Sapi.SAPI_3)
    			.data(CP_DATA_ACK_MT_UL)
    			.build();
    	Confirmation confirmation = gsmb.send(dataReq);
        assertEquals("lapdmMDL_DATA_REQ confirmation error", PhErrorType.GSM_PH_ENOERR,confirmation.getErrorType());
        
        verifyDataInd (true,dataIndicationQueue);
        
	    //Send RP-Ack (Uplink)
	    LapdmDL_DATA_REQ data = GsmbFactory.getLapdmDL_DATA_REQBuilder().timeout(10)
    			.chan(LapdmChannelType.LAPDM_CH_SDCCH)
    			.sapi(Sapi.SAPI_3)
    			.data(RP_DATA_ACK_MT_UL)
    			.build();
    	confirmation = gsmb.send(data);
        assertEquals("lapdmMDL_DATA_REQ confirmation error", PhErrorType.GSM_PH_ENOERR,confirmation.getErrorType());
        
        //Receive RP-Ack on Abisco
        verifyDataInd (false,dataIndicationQueue);
            
	    
	    //Send CP-Ack (Downlink)
	    smsData = false;
    	sendMultiPurposeReq(smsData);
    	
        setTestInfo("Delivery report (RP-Ack) send from Abisco");
	    sleepSeconds(2);
	    
	    setTestInfo("Fetch GSMB indications");
        listOfIndications = gsmb.getIndications();
        
        for(Indication myInd: listOfIndications) {
            if(myInd instanceof LapdmDL_DATA_IND){
            	setTestInfo("CP-Ack received by MSSim");
            	short [] dataVec = ((LapdmDL_DATA_IND) myInd).getData();
            	
            	if(dataVec.length == EXP_CP_DATA_ACK_MT_DL.length ) {
            		
            		setTestInfo("Expected lenght " + EXP_CP_DATA_ACK_MT_DL.length);
            		
            		for (short i = 0; i < dataVec.length; ++i)
                    {
            			if (dataVec[i] == EXP_CP_DATA_ACK_MT_DL[i]){
            				setTestInfo("Checking values " + dataVec[i]);
            			}else{
            				setTestInfo("Wrong value received " + dataVec[i] + " expected " + EXP_CP_DATA_ACK_MT_DL[i]);
            			}
                    }
            	}else{
            		setTestInfo("Error while receiving CP-Ack in MSSim, wrong size " +dataVec.length);
            	}
 
            }else {
                fail("Did not received CP-Ack, a LapdmDL_DATA_IND was expected");
            }
            
        }
    	
    }
    
    private void relReqMssim(Sapi inSapi,LapdmChannelType inChan, MessageQueue<ReleaseIndication> releaseIndicationQueue) throws InterruptedException {
    	
    	setTestInfo("Releasing SAPI " +inSapi);
    	
    	if (inSapi == Sapi.SAPI_0){
    		expSapi = 0;
    	}else{
    		expSapi = 3;
    	}
    	    	   	
    	LapdmDL_RELEASE_REQ releaseReq = GsmbFactory.getLapdmDL_RELEASE_REQBuilder()
    			.sapi(inSapi).chan(inChan)
    			.mode(ReleaseMode.LAPDM_REL_NORM).build();
    	Confirmation confirmation = gsmb.send(releaseReq);
        assertEquals("LapdmDL_RELEASE_REQ confirmation error", PhErrorType.GSM_PH_ENOERR,confirmation.getErrorType());
    	
     // Verifies that a Release Indication has been sent from BTS to BSC on ABIS
        setTestInfo("ABISCO: Waiting for ReleaseIndication");
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
        if (((ReleaseIndication) msg).getLinkIdStruct().getSAPI() == expSapi ){
        	setTestDebug("Received ReleaseIndication with correct SAPI " + expSapi);
	    }
        else{
        	fail("Wrong values in ReleaseIndication received");
        	setTestDebug("Received SAPI: " + ((ReleaseIndication) msg).getLinkIdStruct().getSAPI() + " expected: "  + expSapi);
        }
    }
    
    private void chActMssim() throws InterruptedException {
    	
    		
    	// Send GsmphMPH_CS_CHN_OPEN_REQ (ChanActivation on mssim)
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
                .sacchByp(true)
                .rtOutOff(false)
                .rxTrfcOn(false)
                .undecFrm(false)
                .narfcn((short) 0)
                .trxNum((short) 0) 
                .frArfcn(arfcn) // (wrbs This parameter is dependent on which band is the stp configured
                .build();
        Confirmation confirmation = gsmb.send(cs_openreq);
        assertEquals("gsmphMPH_CS_CCH_OPEN_REQ confirmation error", PhErrorType.GSM_PH_ENOERR, confirmation.getErrorType());
            
        // get CcId from GsmphMPH_CHN_OPEN_CFN
        CcId = cs_openreq.getDetailedConfirmation(confirmation).getCcId();     
        setTestInfo("Value CcId " + CcId);
        
        // Send MSSIM.gsmbSrvMS_SET_MEAS_CMD
        GsmbSrvMS_SET_MEAS_CMD set_meas = GsmbFactory.getGsmbSrvMS_SET_MEAS_CMDBuilder()
        		.timeout(20)
        		.chan(ChanBit.GSMB_SRV_SACCH)
        		.msId(0)
        		.l2hdr((short) 2)
        		.spare(new short[] {0, 0})
        		.data(new short[] {6, 21, 56, 56, 0, 111, 5, 128, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0})
        		.build();
        Response response = gsmb.send(set_meas);
        assertTrue("gsmbSrvMS_SET_MEAS_CMD response failed", response.isSuccess());
        
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
    private void estReqBTS() throws InterruptedException {
    	
    	setTestInfo("Sending establish request to MSSIM, SAPI");
    	
    	TRAFFRN traffrn = abisHelper.getRslServer();
    	EstablishReq estabReq = traffrn.createEstablishReq();

    	ChannelNoStruct channelnostruct = new ChannelNoStruct();
    	 channelnostruct.setTimeSlotNo(1);
    	 channelnostruct.setChannelType(TRAFFRN.Enums.ChannelType.SDCCH_8_0);
    	 estabReq.setChannelNoStruct(channelnostruct);

    	LinkIdStruct linkidstruct = new LinkIdStruct();
    	 linkidstruct.setSAPI(3);
    	 linkidstruct.setReserved(0);
    	 linkidstruct.setNABit(0);
    	 linkidstruct.setLinkId(TRAFFRN.Enums.LinkId.FACCH_SDCCH);
    	 estabReq.setLinkIdStruct(linkidstruct);

    	 estabReq.send();
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
	    setTestInfo("Checking correct values received in EstablishIndication");
	    if (((EstablishIndication) msg).getLinkIdStruct().getSAPI() == expSapi & 
	    		((EstablishIndication) msg).getLinkIdStruct().getLinkId() == expLinkId){
	    	setTestInfo("Received EstablishIndication with correct SAPI " + expSapi);
        	setTestInfo("Received EstablishIndication with correct LinkId " + expLinkId);
	    }
        else{
        	fail("Wrong values in EstablishIndication received");
        	setTestDebug("Received SAPI: " + ((EstablishIndication) msg).getLinkIdStruct().getSAPI() + " expected: "  + expSapi);
        	setTestDebug("Received SAPI: " + ((EstablishIndication) msg).getLinkIdStruct().getLinkId() + " expected: "  + expLinkId);
        }
    	
    }
    
    private void channelActivationImmediateAssign() throws InterruptedException {
    	setTestInfo("Sending channelActivationImmediateAssign");
        
    	TRAFFRN traffrn = abisHelper.getRslServer();
        ChannelActImmediateAssign channelActImmediateAssign = traffrn.createChannelActImmediateAssign();
        
        // Set Behaviour
        Behaviour myBehaviour = new Behaviour();
        myBehaviour.setEP1_TXCNT(1); 
        myBehaviour.setTIMER1(50);   
        channelActImmediateAssign.setBehaviour(myBehaviour);
        
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
        
        MSPowerStruct mSPowerStruct = new MSPowerStruct();
        mSPowerStruct.setPowerLevel(0);
        mSPowerStruct.setSpare(0);
        
        TimingAdvanceStruct timingAdvanceStruct = new TimingAdvanceStruct();
        timingAdvanceStruct.setTimingAdvanceValue(0);
        timingAdvanceStruct.setSpare(0);
        
        ChannelNoStruct channelNoStruct1 = new ChannelNoStruct();
        //Channel Combination (vii)
        channelNoStruct1.setTimeSlotNo(1); 
        channelNoStruct1.setChannelType(ChannelType.SDCCH_8_0); 
        activationTypeStruct.setActivationType(ActivationType.INTRA_IMM);
        channelModeStruct.setChannelRate(ChannelRate.SDCCH); 
        
        channelActImmediateAssign.setChannelNoStruct(channelNoStruct1);
        channelActImmediateAssign.setActivationTypeStruct(activationTypeStruct);
        channelActImmediateAssign.setChannelModeStruct(channelModeStruct);
        channelActImmediateAssign.setMSPowerStruct(mSPowerStruct);
        
        channelActImmediateAssign.send();
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
    
    private void verifyDataInd(boolean smsData,MessageQueue<DataIndication> dataIndicationQueue) throws InterruptedException {

    	// Verifies that a Data Indication has been sent from BTS to BSC on ABIS
        setTestInfo("ABISCO: Waiting for DataIndication");
        
        if (smsData){
        	expSapi = 3;
        	expLinkId = LinkId.FACCH_SDCCH;
        	expL3Data = EXP_CP_DATA_ACK_MT_UL;
        }        
        else{
        	expSapi = 3;
        	expLinkId = LinkId.FACCH_SDCCH;
        	expL3Data = EXP_RP_DATA_ACK_MT_UL;

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
	        	setTestInfo("ABISCO: Recieved DataIndication");
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
        		setTestDebug("Size of L3 data checked successfully");
        		
        		if (((DataIndication) msg).getL3Data().equals(expL3Data)){
        			setTestDebug("Correct L3 Data received " + expL3Data);
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

 
    
}
    

    
