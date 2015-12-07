package com.ericsson.msran.test.grat.linkestablishindication;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.json.JSONException;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import com.ericsson.abisco.clientlib.AbiscoServer.Routing;
import se.ericsson.jcat.fw.annotations.Setup;
import se.ericsson.jcat.fw.annotations.Teardown;
import com.ericsson.abisco.clientlib.AbiscoResponse;
import com.ericsson.abisco.clientlib.MessageQueue;
import com.ericsson.abisco.clientlib.servers.BG;
import com.ericsson.abisco.clientlib.servers.DISPATCH;
import com.ericsson.abisco.clientlib.servers.BG.Enums;
import com.ericsson.abisco.clientlib.servers.BG.EstablishIndication;
import com.ericsson.commonlibrary.resourcemanager.Rm;
import com.ericsson.msran.g2.annotations.TestInfo;
import com.ericsson.msran.test.grat.testhelpers.AbisHelper;
import com.ericsson.msran.test.grat.testhelpers.GsmbHelper;
import com.ericsson.msran.test.grat.testhelpers.MomHelper;
import com.ericsson.msran.test.grat.testhelpers.MssimHelper;
import com.ericsson.msran.test.grat.testhelpers.NodeStatusHelper;
import com.ericsson.msran.jcat.TestBase;
import com.ericsson.mssim.gsmb.Confirmation;
import com.ericsson.mssim.gsmb.Gsmb;
import com.ericsson.abisco.clientlib.servers.BG.Enums.LinkId;
import com.ericsson.abisco.clientlib.servers.TRAFFRN;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.ActivationTypeStruct;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.ChannelActAck;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.ChannelActImmediateAssign;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.ChannelActNormalAssign;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.ChannelModeStruct;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.ChannelNoStruct;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.Enums.ActivationType;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.Enums.RFB;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.Enums.Rbit; 
import com.ericsson.abisco.clientlib.servers.TRAFFRN.Enums.DTXUplink;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.Enums.DTXDownlink;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.Enums.TypeOfCh;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.Enums.ChannelRate;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.Enums.AlgOrRate;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.MSPowerStruct;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.RFChannelRelease;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.TimingAdvanceStruct;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.Enums.ChannelType;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.UNACKNOWLEDGED_MESSAGE_STOREDException;
import com.ericsson.abisco.clientlib.AbiscoServer.Behaviour;
import com.ericsson.mssim.gsmb.GsmbSrvMS_SET_MEAS_CMD;
import com.ericsson.mssim.gsmb.GsmbSrvCELL_INFO_CMD;
import com.ericsson.mssim.gsmb.GsmphMPH_CS_CHN_OPEN_REQ;
import com.ericsson.mssim.gsmb.ChnComb;
import com.ericsson.mssim.gsmb.ChnMain;
import com.ericsson.mssim.gsmb.GsmbSrvMS_SET_MEAS_CMD.ChanBit;
import com.ericsson.mssim.gsmb.FrequencyStructure.FrStructureType;
import com.ericsson.mssim.gsmb.GsmphMPH_CCCH_CLOSE_REQ;
import com.ericsson.mssim.gsmb.GsmphMPH_CHN_CLOSE_REQ;
import com.ericsson.mssim.gsmb.GsmphMPH_CS_DATA_REQ;
import com.ericsson.mssim.gsmb.Indication;
import com.ericsson.mssim.gsmb.GsmphMPH_CS_DATA_REQ.Burst;
import com.ericsson.mssim.gsmb.GsmphMPH_CS_DATA_REQ.Chan;
import com.ericsson.mssim.gsmb.Response;
import com.ericsson.mssim.gsmb.impl.GsmbFactory;
import com.ericsson.mssim.gsmb.packing.internal.PhErrorType;
import com.ericsson.abisco.clientlib.servers.CMDHAND.Enums.BCCHType;
import com.ericsson.msran.test.grat.testhelpers.TgLdns;
import com.ericsson.msran.test.grat.testhelpers.AbiscoConnection;



/**
 * @id GRAT-03.02:027
 * 
 * @name LinkEstablishIndication
 * 
 * @author GRAT Cell
 * 
 * @created 2015-08-09
 * 
 * @description Verify the LINK ESTABLISHMENT INDICATION procedure 
 *              is used by the BTS to indicate to the BSC 
 *              that a layer 2 link on the radio path has been 
 *              established in multiframe mode at the initiative of  
 *              the MS.
 *              
 *              
 * 
 * @revision edantek 2015-09-11 First version
 *           
 *           
 */

public class LinkEstablishIndicationNorEst extends TestBase {
    private NodeStatusHelper nodeStatus;
    private Gsmb gsmb;
    private AbisHelper abisHelper; 
    private BG bgServer;
    private MomHelper momHelper;
    private MssimHelper mssimHelper;
    private GsmbHelper gsmbHelper;
    private Enums.LinkId expLinkId;
    AbiscoResponse msg = null;
    short CELL =  0;
    long CcId = 0;
    int subTc = 0;
    int SAPI = 0;
    int expSapi = 0;
    int arfcn = 0;
    private AbiscoConnection abisco;
    private final BCCHType TypOfBCCH = BCCHType.COMB;
    
    /**
     * Description of test case for test reporting
     */
    
	@TestInfo(
            tcId = "LTE00230712",
            slogan = "Link Establish Indication Normal Establishment",
            requirementDocument = "326/10264-HRB10515",
            requirementRevision = "PC1",
            requirementLinkTested = "http://erilink.ericsson.se/eridoc/????",
            requirementLinkLatest = "http://erilink.ericsson.se/eridoc/??????",
            requirementIds = { "04.08:0002" },
            verificationStatement = "Ongoing",
            testDescription = "Verifies Link Establish Indication Normal Establishment.",
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
        bgServer = abisHelper.getBG();
        gsmb = Rm.getMssimList().get(0).getGsmb();
        mssimHelper = new MssimHelper(gsmb);
        gsmbHelper = new GsmbHelper(gsmb);
        CELL = mssimHelper.getMssimCellToUse();
        momHelper = new MomHelper();
        arfcn = momHelper.getArfcnToUse();
        abisco = new AbiscoConnection();
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
     * @name LinkEstablishIndication
     * 		
     * @description Verify Link Establish Indication Normal Establishment.
     *
     * @param testId - String - Unique identifier of the test case.
     * @param description - String - Brief description of test case.
     * @throws JSONException 
     * @throws InterruptedException 
     */
    
    
    //@SuppressWarnings("null") 
	@Test (timeOut = 1800000)
    @Parameters({ "testId", "description" })
    public void linkEstablishIndicationNorEstSubTc1Test(String testId, String description) throws InterruptedException, JSONException {
		
		// SubTest case 1, Normal case
		// Tested channel Combination		Chan (SAPI=0)		Chan (SAPI=3)		ChnComb						ChnMain
		//	(i)								FACCH(2)			SACCH(3)			TCHXF_FACCHXF_SACCHXTF(1)	TCH_FS(129)
		
		setTestCase(testId, description);
		
		setTestStepBegin("Perform subTC1");
						
		// Prepare, general setup
		linkEsIndPrep(testId); 
		
		// Prepare MessageQueue for establishIndication
		MessageQueue<EstablishIndication> establishIndicationQueue = bgServer.getEstablishIndicationQueue();
		establishIndicationQueue.clear();
		
		// Set the subTc identifier parameter
		subTc = 1;
		
		setTestInfo("Sending ChannelActImmediateAssign on Abis for subTc1");
		// SubTest case 1
		activateImmeAsig();
		
		// Open cs channel
		csCchOpenReq();
		
		// Send SABM on SAPI0 from MSSIM
		SAPI = 0;
		csDataReqSapi0();
		
		// Verify that Establish Indication has been sent from BTS to BSC on ABIS 
		// on SAPI 0
		setTestInfo("ABISCO: Waiting for EstablishIndication on SAPI 0 for subTc1 ");
		expSapi = 0;
		expLinkId = LinkId.FACCH_SDCCH;
		verifyEsInd(establishIndicationQueue);
		establishIndicationQueue.clear();
				    
		// Cleanup
		setTestStepBegin("Cleanup");
		
		// rFChannelRelease
		rFChannelRelease();
		
		// Close CHN channel
		closeCHN();
		
		setTestInfo("SubTc1 End");
		setTestStepEnd(); 
	}
		
	@Test (timeOut = 1800000)
	@Parameters({ "testId", "description" })
	public void linkEstablishIndicationNorEstSubTc2Test(String testId, String description) throws InterruptedException, JSONException {
		// SubTest case 2, Normal case
		// Tested channel Combination		Chan (SAPI=0)		Chan (SAPI=3)		ChnComb						ChnMain
		//	(ii) 							FACCH(2)			SACCH(3)		TCHXF_FACCHXF_SACCHXTF(1)	TCH_HS(161)
		
		setTestCase(testId, description);
		
		setTestStepBegin("Perform subTC2");
								
		// Prepare, general setup
		linkEsIndPrep(testId);
		
		// Prepare MessageQueue for establishIndication
		MessageQueue<EstablishIndication> establishIndicationQueue = bgServer.getEstablishIndicationQueue();
		establishIndicationQueue.clear();
		
		// Set the subTc identifier parameter
		subTc = 2;
		
		setTestInfo("Sending ChannelActImmediateAssign on Abis for subTc2");
		activateImmeAsig();
		
		// Open cs channel
		csCchOpenReq();
		
		// Send SABM on SAPI0 from MSSIM
		SAPI = 0;
		csDataReqSapi0();
		
		// Verify that Establish Indication has been sent from BTS to BSC on ABIS 
		// on SAPI 0
		setTestInfo("ABISCO: Waiting for EstablishIndication on SAPI 0 for subTc2 ");
		expSapi = 0;
		expLinkId = LinkId.FACCH_SDCCH;
		verifyEsInd(establishIndicationQueue);
		establishIndicationQueue.clear();
				    
		// Cleanup
		setTestStepBegin("Cleanup");
		// rFChannelRelease
		rFChannelRelease();
		
		// Close CHN channel
		closeCHN();
				
		setTestInfo("SubTc2 End");
		setTestStepEnd();
		
	}
	
	@Test (timeOut = 1800000)
	@Parameters({ "testId", "description" })
	public void linkEstablishIndicationNorEstSubTc3Test(String testId, String description) throws InterruptedException, JSONException {
		// SubTest case 3, Normal case
		// Tested channel Combination		Chan (SAPI=0)		Chan (SAPI=3)		ChnComb						ChnMain
		//	(v)								SDCCH(6)			SDCCH(6)		SDCCHX4_SACCHXC4(32)		TCH_FS(129)
				
				
		setTestCase(testId, description);
			
		setTestStepBegin("Perform subTC3");
					
		if (TypOfBCCH == BCCHType.COMB)
		{	
			// Prepare, general setup
			linkEsIndPrep(testId);
			
			// Set the subTc identifier parameter
			subTc = 3;
						
			// Send activateImmeAsig
			setTestInfo("Sending ChannelActImmediateAssign on Abis For subTc3");
			activateImmeAsig();
					
			// Open cs channel
			csCchOpenReq();
					
			// Prepare MessageQueue for establishIndication
			MessageQueue<EstablishIndication> establishIndicationQueue = bgServer.getEstablishIndicationQueue();
			establishIndicationQueue.clear();
			
			// Send SABM on SAPI0 from MSSIM
			SAPI = 0;
			csDataReqSapi0();
				
			// Verify that a Establish Indication has been sent from BTS to BSC on ABIS        
			setTestInfo("ABISCO: Waiting for EstablishIndication");
			expSapi = 0;
			expLinkId = LinkId.FACCH_SDCCH;
			verifyEsInd(establishIndicationQueue);
			establishIndicationQueue.clear();
			
			setTestInfo("SAPI:3 is not applicable for subTc3");
			
			// Cleanup
			setTestStepBegin("Cleanup");
			// rFChannelRelease
			rFChannelRelease();
			
			// Close CHN channel
			closeCHN();
			
			setTestInfo("First SubTc3 End");
			setTestStepEnd(); 
		}
		else
		{
			setTestInfo("SubTc3 is Skiped due to TsBCCH and TsSdcch are enabled. It is supported only when TsBcchCombined is enabled");
		}
			
	}
	
	@Test (timeOut = 1800000)
	@Parameters({ "testId", "description" })
	public void linkEstablishIndicationNorEstSubTc4Test(String testId, String description) throws InterruptedException, JSONException {
		// SubTest case 4, Normal case
		// Tested channel Combination		Chan (SAPI=0)		Chan (SAPI=3)		ChnComb						ChnMain
		//	(v)								SDCCH(6)			SDCCH(6)		SDCCHX8_SACCHXC8(33)			TCH_FS(129)
		
		setTestCase(testId, description);
		
		setTestStepBegin("Perform subTC4");
					
		if (TypOfBCCH == BCCHType.NCOMB)
		{
			// Prepare, general setup
			linkEsIndPrep(testId);
			
			// Set the subTc identifier parameter
			subTc = 4;
			
			setTestInfo("Sending ChannelActImmediateAssign on Abis For subTc4");
			activateImmeAsig();
						
			// Open cs channel
			csCchOpenReq();
			
			// Prepare MessageQueue for establishIndication
			MessageQueue<EstablishIndication> establishIndicationQueue = bgServer.getEstablishIndicationQueue();
			establishIndicationQueue.clear();
			
			// Send SABM on SAPI0 from MSSIM
			SAPI = 0;
			csDataReqSapi0();
			
			// Verify that Establish Indication has been sent from BTS to BSC on ABIS 
			// on SAPI 0
			setTestInfo("ABISCO: Waiting for EstablishIndication on SAPI 0 for subTc4 ");
			expSapi = 0;
			expLinkId = LinkId.FACCH_SDCCH;
			verifyEsInd(establishIndicationQueue);
			establishIndicationQueue.clear();
			
			// Cleanup
			setTestStepBegin("Cleanup");
			
			// rFChannelRelease
			subTc = 4;
			rFChannelRelease();
			
			// Close CHN channel
			closeCHN();
			
			setTestInfo("First SubTc4 End");
			setTestStepEnd();
		}
		else
		{
			setTestInfo("SubTc4 is Skiped due to TsBcchCombined is enabled. It is supported only when TsBcch and TsSdcch are enabled");
		}
	}	
	
	@Test (timeOut = 1800000)
	@Parameters({ "testId", "description" })
	public void linkEstablishIndicationNorEstSubTc5Test(String testId, String description) throws InterruptedException, JSONException {
		// SubTest case 5, Contention Resolution case
		// Tested channel Combination		Chan (SAPI=0)		Chan (SAPI=3)		ChnComb						ChnMain
		//	(viii)							FACCH(2)			FACCH(2)		TCHXF_FACCHXF_SACCHXM(2)		TCH_FS(129)
		
		setTestCase(testId, description);
		
		setTestStepBegin("Perform subTC5");
						
		// Prepare, general setup
		linkEsIndPrep(testId); 
		
		// Prepare MessageQueue for establishIndication
		MessageQueue<EstablishIndication> establishIndicationQueue = bgServer.getEstablishIndicationQueue();
		establishIndicationQueue.clear();
		
		// Set the subTc identifier parameter
		subTc = 5;
		
		setTestInfo("Sending ChannelActNormalAssign on Abis For subTc5");
		activateNormAsig();
		
		// Open cs channel
		csCchOpenReq();
		
		// Send SABM on SAPI0 from MSSIM
		SAPI = 0;
		csDataReqSapi0();
		
		// Verify that Establish Indication has been sent from BTS to BSC on ABIS 
		// on SAPI 0
		setTestInfo("ABISCO: Waiting for EstablishIndication on SAPI 0 for subTc5 ");
		expSapi = 0;
		expLinkId = LinkId.FACCH_SDCCH;
		verifyEsInd(establishIndicationQueue);
		establishIndicationQueue.clear();
		    
		// Cleanup
		setTestStepBegin("Cleanup");

		// rFChannelRelease
		rFChannelRelease();
		         
		// Close CHN channel
		closeCHN();
		
		setTestInfo("First SubTc5 End");        
		setTestStepEnd();
	}
	
	//MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
    // Help methods
    //MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
    //MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM 
  
    /**
     * @name rFChannelRelease
     * 
     * @description Release RFchannel
     *
     */
    private void  rFChannelRelease() throws InterruptedException {
    	setTestInfo("Sending rFChannelRelease on Abis");
        
    	// rFChannelRelease
        TRAFFRN traffrn = abisHelper.getRslServer();
        RFChannelRelease rFChannelRelease = traffrn.createRFChannelRelease();
        
        // Set Routing
        Routing routing = new Routing();
        routing.setTG(0);
        routing.setTRXC(0);
        
        // Set Channel Type
        ChannelNoStruct channelNoStruct = new ChannelNoStruct();   
                
        if (subTc == 1)
        {
        	// (i) TCHXF_FACCHXF_SACCHXTF(1)
        	channelNoStruct.setTimeSlotNo(2); 
            channelNoStruct.setChannelType(ChannelType.Bm); 
        }
        else if (subTc == 2)
        {    
        	// (ii) TCHXH_FACCHXH_SACCHXTH(16)
        	channelNoStruct.setTimeSlotNo(2); 
            channelNoStruct.setChannelType(ChannelType.Lm_0); 
        }
        else if (subTc == 3)
        {       
        	// (v) SDCCHX4_SACCHXC4(32) 
        	channelNoStruct.setTimeSlotNo(0); 
            channelNoStruct.setChannelType(ChannelType.SDCCH_4_0); 
        }
        else if (subTc == 4)
        {        	
        	// (vii) SDCCHX8_SACCHXC8(33)
        	channelNoStruct.setTimeSlotNo(1); 
            channelNoStruct.setChannelType(ChannelType.SDCCH_8_0); 
        }
        else 
        {
        	// (viii) TCHXF_FACCHXF_SACCHXM)
        	channelNoStruct.setTimeSlotNo(2); 
            channelNoStruct.setChannelType(ChannelType.Bm); 
        }
        
        rFChannelRelease.setRouting(routing);
        rFChannelRelease.setChannelNoStruct(channelNoStruct);
                        
        // Send rFChannelRelease
        try { 
        	rFChannelRelease.send();
        } catch (UNACKNOWLEDGED_MESSAGE_STOREDException ums) {
        	setTestInfo("Catched UNACKNOWLEDGED_MESSAGE_STOREDException");
        }
      
        DISPATCH dispatch = abisHelper.getDISPATCH();
        DISPATCH.SendStoredMessages sendStoredMessages = dispatch.createSendStoredMessages();
        sendStoredMessages.setIssueUNACK(DISPATCH.Enums.IssueUNACK.No);
        sendStoredMessages.sendAsync();
    }
    //MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
    
    /**
     * @throws InterruptedException 
     * @name closeCHN
     * 
     * @description Closes CHN
     *
     */
    private void closeCHN() throws InterruptedException {
    	// Send GsmphMPH_CHN_CLOSE_REQ
    	setTestInfo("Close CHN");
        GsmphMPH_CHN_CLOSE_REQ chn_closereq = GsmbFactory.getGsmphMPH_CHN_CLOSE_REQBuilder(CcId).timeout(20).build();
        Confirmation confirmation1 = gsmb.send(chn_closereq);
        assertEquals("gsmphMPH_CHN_CLOSE_REQ confirmation error", PhErrorType.GSM_PH_ENOERR, confirmation1.getErrorType());
        
        // sleep
    	sleepSeconds(6);
    	        
    }
    
    //MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
    /**
     * @throws InterruptedException 
     * @name csDataReqSapi0
     * 
     * @description Send MSSIM.gsmphMPH_CS_DATA_REQ  on Link id SAPI=0
     *
     */
    private void csDataReqSapi0() throws InterruptedException, JSONException {
    	
    	
    	Chan chan;
    	
    	// Set logical channel   	
    	if (subTc == 1) //(i)
        {
    		chan = Chan.GSMPH_FACCH; 
    	}       
        else if (subTc == 2) //(ii)
        {        	
        	chan = Chan.GSMPH_FACCH; 
        }
        else if (subTc == 3) //(v)
        {        	
        	chan = Chan.GSMPH_SDCCH; 
        }
        else if (subTc == 4) //(vii)
        {        	
        	chan = Chan.GSMPH_SDCCH; 
        }
        else //(viii)
        {
        	chan = Chan.GSMPH_FACCH; 	
        }
    	
        
    	// Send MSSIM.gsmphMPH_CS_DATA_REQ //CcId=208
    	GsmphMPH_CS_DATA_REQ cs_data_REQ = GsmbFactory.getGsmphMPH_CS_DATA_REQBuilder(CcId).timeout(20)
    		.chan(chan) 
	        .burst(Burst.NORMAL_BURST)
	        .eaId((short) 0)
	        .spare((short) 0)
	        // Contention case
	        //.data(new short[] {1, 63, 45, 5, 8, 112, 0, 0, 0, 0, 0, 0, 1, 0, 43, 43, 43, 43, 43, 43, 43, 43, 43}) 
	        // Normal case 
	        .data(new short[] {1, 63, 1, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43})
	        .build(); 
    	Confirmation confirmation = gsmb.send(cs_data_REQ);
    	assertEquals("gsmphMPH_CS_DATA_REQ confirmation error", PhErrorType.GSM_PH_ENOERR, confirmation.getErrorType());
    }
        
    
    /**
     * @throws InterruptedException 
     * @name csCchOpenReq
     * 
     * @description ChanActivation on mssim
     *
     */
    private void csCchOpenReq() throws InterruptedException, JSONException {
    	
    	ChnComb chnComb;
    	ChnMain chnMain;
    	// Set Time Slot
    	short ts = 0;
    	short tsc = 0;
    	
    	if (subTc == 1)
        {
    		//Channel Combination (i)
    		chnComb = ChnComb.GSMPH_TCHXF_FACCHXF_SACCHXTF; 
        	chnMain = ChnMain.GSMPH_TCH_FS; 
        	ts = 2;
        	tsc = 1;
        }
        else if (subTc == 2)
        {        	
        	//Channel Combination (ii)
        	chnComb = ChnComb.GSMPH_TCHXH_FACCHXH_SACCHXTH; 
        	chnMain = ChnMain.GSMPH_TCH_HS; 
        	ts = 2;
        	tsc = 1;
        	
        }
        else if (subTc == 3)
        {        	
        	//Channel Combination (v)
        	chnComb = ChnComb.GSMPH_SDCCHX4_SACCHXC4; 
        	chnMain = ChnMain.GSMPH_TCH_FS; 
        	ts = 0;
        	tsc = 0;
        }
        else if (subTc == 4)
        {        
        	//Channel Combination (vii)
        	chnComb = ChnComb.GSMPH_SDCCHX8_SACCHXC8;
        	chnMain = ChnMain.GSMPH_TCH_FS; 
        	ts = 1;
        	tsc = 1;
        }
        else 
        {
        	//Channel Combination (viii)
        	chnComb = ChnComb.GSMPH_TCHXF_FACCHXF_SACCHXM; 
        	chnMain = ChnMain.GSMPH_TCH_FS; 
        	ts = 2;
        	tsc = 1;
        }
    	    	
    	// Clear list of indications
        java.util.List<Indication> listOfIndications = gsmb.getIndications();
        listOfIndications.clear();

        // Send GsmbSrvCELL_INFO_CMD        
        GsmbSrvCELL_INFO_CMD info = GsmbFactory.getGsmbSrvCELL_INFO_CMDBuilder(CELL)
        		.build();
        Response response = gsmb.send(info);
        assertTrue("gsmbSrvCELL_INFO_CMD response failed", response.isSuccess());
                
        MessageQueue<EstablishIndication> establishIndicationQueue = bgServer.getEstablishIndicationQueue();
        establishIndicationQueue.clear();     
        
        // Send GsmphMPH_CS_CHN_OPEN_REQ (ChanActivation on mssim)
        GsmphMPH_CS_CHN_OPEN_REQ cs_openreq = GsmbFactory.getGsmphMPH_CS_CHN_OPEN_REQBuilder(
                CELL,
                FrStructureType.ARFCN).timeout(20)
                .ref(0) //0 //270565392
                .chnComb(chnComb)
                .chnMain(chnMain)
                .ts(ts) 
                .sub((short)0)
                .msId(0) //0
                .tsc(tsc) // 1 //4 /
                .rxAcchOn(true)
                .allSacchSI(false)
                .sdcchByp(true)
                .facchByp(true)
                .sacchByp(true)
                .rtOutOff(false) 
                .rxTrfcOn(true) // true bcchCom
                .undecFrm(true) // true bcchCom
                .narfcn((short) 0)
                .trxNum((short) 0) 
                .frArfcn(arfcn) // (wrbs This parameter is dependent on which band is the stp configured
                //.arfcnl(new int[] {} )
                .build();
        Confirmation confirmation = gsmb.send(cs_openreq);
        assertEquals("gsmphMPH_CS_CHN_OPEN_REQ confirmation error", PhErrorType.GSM_PH_ENOERR, confirmation.getErrorType());
          
        // get CcId from GsmphMPH_CHN_OPEN_CFN
        CcId = cs_openreq.getDetailedConfirmation(confirmation).getCcId();     
        setTestInfo("ValuCcIde:CcId");
        System.out.print("ParameterCcId:CcId"); 
        System.out.println(CcId);
        
        
        // Send MSSIM.gsmbSrvMS_SET_MEAS_CMD
        GsmbSrvMS_SET_MEAS_CMD set_meas = GsmbFactory.getGsmbSrvMS_SET_MEAS_CMDBuilder().timeout(20).chan(ChanBit.GSMB_SRV_SACCH)
        		.msId(0)
        		.l2hdr((short) 2)
        		//GSMB_SRV_SACCH(3),
        		.chan(ChanBit.GSMB_SRV_SACCH)
        		.spare(new short[] {0, 0})
        		.data(new short[] {6, 21, 56, 56, 0, 111, 5, 128, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0})
        		.build();
        response = gsmb.send(set_meas);
        assertTrue("gsmbSrvMS_SET_MEAS_CMD response failed", response.isSuccess());
    
    }
    
    //MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
    /**
     * @throws InterruptedException 
     * @name linkEsIndPrep
     * 
     * @description General setup to be able to run the test
     *
     */
    private void linkEsIndPrep(String testId) throws InterruptedException, JSONException {
    	
    	// Parameter setting
    	List<TgLdns> tgList;
        int numOfTgs = 2;
        int numOfTrxsPerTg = 2;
        short rbsCell = 0;
    	int tgId = 0;
    	
    	// Connect, login, configure & start MSSIM
        setTestStepBegin("Setup MsSIM");
        assertTrue("Init MsSIM failed", gsmbHelper.mssimInit(testId, true));
        setTestStepEnd();
    	
    	// Create and unlock MOs
        setTestStepBegin("Create MOs and setup Abisco");
        tgList = momHelper.createUnlockAllGratMos(numOfTgs, numOfTrxsPerTg);
        
        // SetupAbisco
        abisco.setupAbisco(numOfTgs, numOfTrxsPerTg, false);
        
        // Enable several TGs and several TRX per TG
        abisHelper.completeStartup(numOfTgs, numOfTrxsPerTg, tgList);
        
        // Update the cell to enable BCCHType.COMB
        abisHelper.updateCell(tgId, rbsCell, TypOfBCCH);
        
        // Run completeStartup again in order to cell update to take effect
        abisHelper.completeStartup(numOfTgs, numOfTrxsPerTg, tgList);
        setTestStepEnd();
        
        // Define Cell
        setTestStepBegin("Define Cell");
        assertTrue("Define Cell failed", gsmbHelper.mssimDefineCell());
        setTestStepEnd();
                
        // Clear list of indications
        java.util.List<Indication> listOfIndications = gsmb.getIndications();
        listOfIndications.clear();
                
    }
      
    //MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
    /**
     * @throws InterruptedException 
     * @name activateNormAsig
     * 
     * @description Send ChannelActImmediateAssign
     *
     */
    private void  activateNormAsig() throws InterruptedException {
    	setTestInfo("Sending ChannelActNormalAssign on Abis");
        
        TRAFFRN traffrn = abisHelper.getRslServer();
        ChannelActNormalAssign channelActNormalAssign = traffrn.createChannelActNormalAssign();
        
        // Set Routing
        Routing routing = new Routing();
		routing.setTG(0);
		routing.setTRXC(0);
        
		// Set Channel Type
        ChannelNoStruct channelNoStruct = new ChannelNoStruct();
        
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
        
        if (subTc == 1)
        {
        	//Channel Combination (i)
        	channelNoStruct.setTimeSlotNo(2); 
        	channelNoStruct.setChannelType(ChannelType.Bm); 
        	activationTypeStruct.setActivationType(ActivationType.INTRA_IMM);
        	channelModeStruct.setChannelRate(ChannelRate.Bm); 
        }
        else if (subTc == 2)
        {     
        	//Channel Combination (ii)
        	channelNoStruct.setTimeSlotNo(2); 
        	channelNoStruct.setChannelType(ChannelType.Lm_0); 
        	activationTypeStruct.setActivationType(ActivationType.INTRA_IMM);
        	channelModeStruct.setChannelRate(ChannelRate.Lm); 
        }
        else if (subTc == 3)
        {        	
        	//Channel Combination (v)
        	channelNoStruct.setTimeSlotNo(0); 
        	channelNoStruct.setChannelType(ChannelType.SDCCH_4_0); 
        	activationTypeStruct.setActivationType(ActivationType.INTRA_IMM);
        	channelModeStruct.setChannelRate(ChannelRate.SDCCH); 
        }
        else if (subTc == 4)
        {        	
        	//Channel Combination (vii)
        	channelNoStruct.setTimeSlotNo(1); 
        	channelNoStruct.setChannelType(ChannelType.SDCCH_8_0); 
        	activationTypeStruct.setActivationType(ActivationType.INTRA_IMM);
        	channelModeStruct.setChannelRate(ChannelRate.SDCCH); 
        }
        else 
        {
        	//Channel Combination (viii)
        	channelNoStruct.setTimeSlotNo(2); 
        	channelNoStruct.setChannelType(ChannelType.Bm); 
        	activationTypeStruct.setActivationType(ActivationType.INTRA_NOR); 
        	channelModeStruct.setChannelRate(ChannelRate.Bm_BI);
        	
        }
        
        channelActNormalAssign.setChannelNoStruct(channelNoStruct);
        channelActNormalAssign.setActivationTypeStruct(activationTypeStruct);
        channelActNormalAssign.setChannelModeStruct(channelModeStruct);
        channelActNormalAssign.setMSPowerStruct(mSPowerStruct);
        
        // Send channelActNormalAssign
        try { 
        	channelActNormalAssign.send();
        } catch (UNACKNOWLEDGED_MESSAGE_STOREDException ums) {
        	setTestInfo("Catched UNACKNOWLEDGED_MESSAGE_STOREDException");
        }
      
        DISPATCH dispatch = abisHelper.getDISPATCH();
        DISPATCH.SendStoredMessages sendStoredMessages = dispatch.createSendStoredMessages();
        sendStoredMessages.setIssueUNACK(DISPATCH.Enums.IssueUNACK.No);
        sendStoredMessages.sendAsync();
    }
    //MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
    
    /**
     * @throws InterruptedException 
     * @name activateImmeAsig
     * 
     * @description Send ChannelActImmediateAssign
     *
     */
    private void  activateImmeAsig() throws InterruptedException {
    	setTestInfo("Sending ChannelActImmediateAssign on Abis");
        
        TRAFFRN traffrn = abisHelper.getRslServer();
        ChannelActImmediateAssign channelActImmediateAssign = traffrn.createChannelActImmediateAssign();
        
        // Set Behaviour
        Behaviour myBehaviour = new Behaviour();
        myBehaviour.setEP1_TXCNT(1); 
        myBehaviour.setTIMER1(50);   
        channelActImmediateAssign.setBehaviour(myBehaviour);
                
        // Set Channel Type
        ChannelNoStruct channelNoStruct1 = new ChannelNoStruct();
                
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
        
        if (subTc == 1)
        {
        	//Channel Combination (i)
        	channelNoStruct1.setTimeSlotNo(2); 
        	channelNoStruct1.setChannelType(ChannelType.Bm); 
        	activationTypeStruct.setActivationType(ActivationType.INTRA_IMM);
        	channelModeStruct.setChannelRate(ChannelRate.Bm); 
        }
        else if (subTc == 2)
        {     
        	//Channel Combination (ii)
        	channelNoStruct1.setTimeSlotNo(2); 
        	channelNoStruct1.setChannelType(ChannelType.Lm_0); 
        	activationTypeStruct.setActivationType(ActivationType.INTRA_IMM);
        	channelModeStruct.setChannelRate(ChannelRate.Lm); 
        }
        else if (subTc == 3)
        {        	
        	//Channel Combination (v)
        	channelNoStruct1.setTimeSlotNo(0); 
        	channelNoStruct1.setChannelType(ChannelType.SDCCH_4_0); 
        	activationTypeStruct.setActivationType(ActivationType.INTRA_IMM);
        	channelModeStruct.setChannelRate(ChannelRate.SDCCH); 
        }
        else if (subTc == 4)
        {        	
        	//Channel Combination (vii)
        	channelNoStruct1.setTimeSlotNo(1); 
        	channelNoStruct1.setChannelType(ChannelType.SDCCH_8_0); 
        	activationTypeStruct.setActivationType(ActivationType.INTRA_IMM);
        	channelModeStruct.setChannelRate(ChannelRate.SDCCH); 
        }
        else 
        {
        	//Channel Combination (viii)
        	channelNoStruct1.setTimeSlotNo(2); 
        	channelNoStruct1.setChannelType(ChannelType.Bm); 
        	activationTypeStruct.setActivationType(ActivationType.INTRA_NOR); 
        	channelModeStruct.setChannelRate(ChannelRate.Bm_BI);
        }
        
        channelActImmediateAssign.setChannelNoStruct(channelNoStruct1);
        channelActImmediateAssign.setActivationTypeStruct(activationTypeStruct);
        channelActImmediateAssign.setChannelModeStruct(channelModeStruct);
        channelActImmediateAssign.setMSPowerStruct(mSPowerStruct);
        
        // Send channelActImmediateAssign
        try { 
        	ChannelActAck ack = channelActImmediateAssign.send();
        	setTestInfo(ack.toString());
        	setTestInfo("ABISCO: channelActImmediateAssign has been sent");
        } catch (UNACKNOWLEDGED_MESSAGE_STOREDException ums) {
        	setTestInfo("Catched UNACKNOWLEDGED_MESSAGE_STOREDException");
        }
              
        DISPATCH dispatch = abisHelper.getDISPATCH();
        DISPATCH.SendStoredMessages sendStoredMessages = dispatch.createSendStoredMessages();
        sendStoredMessages.setIssueUNACK(DISPATCH.Enums.IssueUNACK.No);
        sendStoredMessages.sendAsync();
    }
    //MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
    /**
     * @name verifyEsInd
     * 
     * @description Verifies the reception of Establish Indication and it is sent according to /3GPP_11.23/ requirements.
     *
     */
    private void verifyEsInd(MessageQueue<EstablishIndication> establishIndicationQueue) {	
    	// Verifies that a Establish Indication has been sent from BTS to BSC on ABIS
        setTestInfo("ABISCO: Waiting for EstablishIndication");
   
        Enums.LinkId recLinkId;
        int recSapi = 0;
                
        try        
	    {
	        //AbiscoResponse msg;
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
	    	if (msg != null)
	    	{
	        	setTestInfo(msg.toString());
	    	}
	    	else
	        {
	        	setTestInfo("msg is NULL");
	        }
	    	setTestInfo(e.toString());
	        e.printStackTrace();
	        fail("Exception occurred while waiting for EstablishIndication:");
	    }
        
        // Verify the value
        setTestInfo("Verify the recieved EstablishIndication value");
        
        //LinkIdStruct.SAPI=0,
		//LinkIdStruct.LinkId=FACCH/SDCCH,
        recSapi = ((EstablishIndication) msg).getLinkIdStruct().getSAPI();
        recLinkId = ((EstablishIndication) msg).getLinkIdStruct().getLinkId();
        
        setTestInfo("recLinkId:");
        System.out.println(recLinkId);
        
    	if (recSapi == expSapi & recLinkId == expLinkId)
        {
    		setTestInfo("Correct SAPI & LinkId received");
        }
        else 
        {        	
        	fail("Fault SAPI/LinkId received");            	
        }
                	   			              
        // Clean the queue
        establishIndicationQueue.clear();
    }
}
