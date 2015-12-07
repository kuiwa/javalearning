package com.ericsson.msran.test.grat.nodeidentityexchange;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import se.ericsson.jcat.fw.annotations.Setup;
import se.ericsson.jcat.fw.annotations.Teardown;

import com.ericsson.abisco.clientlib.servers.CHEXTRAS.CHRejectException;
import com.ericsson.abisco.clientlib.servers.OM_G31R01;
import com.ericsson.msran.g2.annotations.TestInfo;
import com.ericsson.msran.test.grat.testhelpers.AbisHelper;
import com.ericsson.msran.test.grat.testhelpers.AbiscoConnection;
import com.ericsson.msran.test.grat.testhelpers.MomHelper;
import com.ericsson.msran.test.grat.testhelpers.NodeStatusHelper;
import com.ericsson.msran.test.grat.testhelpers.prepost.AbisPrePost;
import com.ericsson.msran.jcat.TestBase;


/**
 * @id NodeUC476, NodeUC509
 * 
 * @name NodeIdentityExchangeRequestTest
 * 
 * @author Grat 
 * 
 * @created 2014-12-03
 * 
 * @description This test class verifies the Abis Node Identity Exchange EP.
 * 
 * @revision grat 2014-12-03 First version.
 * 
 */

public class NodeIdentityExchangeRequestTest extends TestBase {
    private final OM_G31R01.Enums.MOClass moClassScf = OM_G31R01.Enums.MOClass.SCF;
    
    private AbisPrePost abisPrePost;

    private AbisHelper abisHelper;
    private MomHelper momHelper;
    
    private NodeStatusHelper nodeStatus;

    /**
     * Description of test case for test reporting
     */
    @TestInfo(
            tcId = "NodeUC651",
            slogan = "Abis SO SCF Node Identity Exchange",
            requirementDocument = "1/00651-FCP 130 1402",
            requirementRevision = "PC5",
            requirementLinkTested = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff87c1d941?docno=1/00651-FCP1301402Uen&format=excel8book",
            requirementLinkLatest = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff86a2af3d?docno=1/00651-FCP1301402Uen&action=current&format=excel8book",
            requirementIds = { "10565-0334/21767[PA1][PREL]", "10565-0334/19034[A][APPR]",
                    "10565-0334/19417[A][APPR]" },
            verificationStatement = "Verifies NodeUC651.N",
            testDescription = "Verifies Abis SO SCF Node Identity Exchange Request",
            traceGuidelines = "N/A")


    /**
     * Create MO:s, establish links to Abisco, and verify preconditions.
     */
    @Setup
    public void setup() {
    	setTestStepBegin("Setup");
        nodeStatus = new NodeStatusHelper();
        assertTrue("Node did not reach a working state before test start", nodeStatus.waitForNodeReady());
        
        abisHelper = new AbisHelper();
        abisPrePost = new AbisPrePost();
        momHelper = new MomHelper();
        abisPrePost.preCondSoScfReset();
        setTestStepEnd();
    }
    
    /**
     * Postcond.
     */
    @Teardown
    public void teardown() {
    	setTestStepBegin("Teardown");
        nodeStatus.isNodeRunning();
        abisHelper.setDefaultNegotiationBehaviour();
        setTestStepEnd();
    }
    

    /**
     * @name nodeIdentityExchangeRequest
     * 
     * @description Verifies the Node Identity Exchange EP on SO SCF according to NodeUC651.N
     *
     * @param testId - unique identifier
     * @param description
     * 
     * @throws InterruptedException
     */
    @Test (timeOut = 360000)
    @Parameters({ "testId", "description" })
    public void nodeIdentityExchangeRequest(String testId, String description) throws InterruptedException {
    	setTestCase(testId, description);
    	
    	setTestStepBegin("Start SO SCF");
    	startRequestSoScfImpl();
    	
    	setTestStepBegin("Send Node Identity Exchange Request");
    	
    	String bscId = "BSC_ID_1";
    	int tgId     = 0;
    	String tgIdentifier  = "TG_ID_" + tgId;
    	
    	// Send Node Identity Exchange Request, and wait for a response
    	OM_G31R01.NodeIdentityExchangeResponse nodeIdExResult = abisHelper.nodeIdentityExchange(tgId, bscId, tgIdentifier, 0xABCD);
    	
    	setTestStepBegin("Check Node Identity Exchange Response");
    	
    	assertEquals("Node Identity Exchange Response contained an unexpected Radio Node Identity", MomHelper.MANAGED_ELEMENT_ID_VALUE, nodeIdExResult.getRBSIdentity());
    	assertEquals("Node Identity Exchange Response contained an unexpected GSM Sector Name", AbiscoConnection.getConnectionName(), nodeIdExResult.getGSMSectorName());
    	
    	assertEquals("Node Identity Exchange Response contained an unexpected GSM Sector Id", MomHelper.GSM_SECTOR_ID_VALUE, nodeIdExResult.getGSMSectorId()); 
    	
    	// this attribute comes as a number of List<Integer>
    	String btsVersion = nodeIdExResult.getBTSVersion().getManufacturerId();
    	btsVersion = btsVersion + nodeIdExResult.getBTSVersion().getBTSGeneration();
    	btsVersion = btsVersion + nodeIdExResult.getBTSVersion().getBTSRevision();
    	btsVersion = btsVersion + nodeIdExResult.getBTSVersion().getBTSVariant();
    	
    	assertEquals("Node Identity Exchange Response contained an unexpected Bts Version", MomHelper.BTS_VERSION_VALUE, btsVersion);
    	assertTrue("Node Identity Exchange Response contained an unexpected Bts Node Identity Signature", 0 != nodeIdExResult.getBTSNodeIdentitySignature());   	
    	
    	setTestStepBegin("Check MOM Attributes");
    	
    	assertTrue("MO " + MomHelper.SECTOR_LDN + " " + MomHelper.BSC_NODE_IDENTITY + " does not have the expected value of " + bscId, 
    			momHelper.waitForMoAttributeStringValue(MomHelper.SECTOR_LDN, MomHelper.BSC_NODE_IDENTITY, bscId, 5));
    	
    	assertTrue("MO " + MomHelper.SECTOR_LDN + " " + MomHelper.BSC_TG_IDENTITY + " does not have the expected value of " + tgIdentifier,
    			momHelper.waitForMoAttributeStringValue(MomHelper.SECTOR_LDN, MomHelper.BSC_TG_IDENTITY, tgIdentifier, 5));
    	
    	setTestStepEnd();
    }
    
    
    /**
     * @name nodeIdentityExchangeRequest_SoScf_Not_Started
     * 
     * @description Verifies the Node Identity Exchange EP on SO SCF when SoScf is not started, according to NodeUC479.E2
     * 
     * @param testId - unique identifier
     * @param description
     * 
     * @throws InterruptedException
     */
    @Test (timeOut = 360000)
    @Parameters({ "testId", "description" })
    public void nodeIdentityExchangeRequest_SoScf_Not_Started(String testId, String description) throws InterruptedException {
    	setTestCase(testId, description);
    	
    	setTestStepBegin("Send Node Identity Exchange Request");
    	
    	String bscId = "BSC_ID_1";
    	int tgId     = 0;
    	String tgIdentifier  = "TG_ID_" + tgId;
    	
    	OM_G31R01.NodeIdentityExchangeReject nodeIdExReject = null;
    	
    	try {
    		abisHelper.nodeIdentityExchange(tgId, bscId, tgIdentifier, 0xABCD);
    		fail("Received a Node Identity Exchange Response, but a Node Identity Exchange Reject was expected");
    	} catch (OM_G31R01.NodeIdentityExchangeRejectException e) {
    		setTestStepBegin("********** Got the expected NodeIdentityExchangeRejectException");
    		nodeIdExReject = e.getNodeIdentityExchangeReject();
    	} catch (CHRejectException e) {
    		setTestStepBegin("********** Got CHRejectException: " + e.getCHReject().getReasonForRejection() + " " + e.getCHReject().getFurtherInformation());
    		fail("******************* got CHRejectException");
    	}
    	
    	setTestStepBegin("Check Node Identity Exchange Reject");
		assertEquals("", OM_G31R01.Enums.ResultCode.WrongState,	nodeIdExReject.getResultCode());
    	setTestStepEnd();
    }
    
    
    /**
     * @name nodeIdentityExchangeRequest_Attribute_Error
     * 
     * @description Verifies the Node Identity Exchange EP on SO SCF when the request contains an attribute error, according to NodeUC479.E1
     * 
     * @param testId - unique identifier
     * @param description
     * 
     * @throws InterruptedException
     */
    @Test (timeOut = 360000)
    @Parameters({ "testId", "description" })
    public void nodeIdentityExchangeRequest_Attribute_Error(String testId, String description) throws InterruptedException {
    	setTestCase(testId, description);
   	
    	setTestStepBegin("Start SO SCF");
    	startRequestSoScfImpl();
    	
    	setTestStepBegin("Send Node Identity Exchange Request");
    	
    	String bscId = "BSC_ID_1";
    	int tgId     = 0;
    	String tgIdentifier  = "TG_ID_" + tgId;
    	
    	OM_G31R01.NodeIdentityExchangeReject nodeIdExReject = null;
    	
    	try {
    		// set the bscNodeSignature to 0, to get a reject from the grat
    		abisHelper.nodeIdentityExchange(tgId, bscId, tgIdentifier, 0);
    		fail("Received a Node Identity Exchange Response, but a Node Identity Exchange Reject was expected");
    	} catch (OM_G31R01.NodeIdentityExchangeRejectException e) {
    		setTestStepBegin("********** Got the expected NodeIdentityExchangeRejectException");
    		nodeIdExReject = e.getNodeIdentityExchangeReject();
    	} catch (CHRejectException e) {
    		setTestStepBegin("********** Got CHRejectException: " + e.getCHReject().getReasonForRejection() + " " + e.getCHReject().getFurtherInformation());
    		fail("******************* got CHRejectException");
    	}
    	
    	setTestStepBegin("Check Node Identity Exchange Reject");
		assertEquals("", OM_G31R01.Enums.ResultCode.ProtocolError,	nodeIdExReject.getResultCode());
    	setTestStepEnd();
    }
    
       
    private void startRequestSoScfImpl() throws InterruptedException {
    	setTestInfo("Send Start Request command to SO SCF");

    	ArrayList<String> expectedOmlIwdVersions = new ArrayList<String>(
    			Arrays.asList(AbiscoConnection.OML_IWD_VERSION));
    	ArrayList<String> expectedRslIwdVersions = new ArrayList<String>(Arrays.asList(
    			AbiscoConnection.RSL_IWD_VERSION_1, AbiscoConnection.RSL_IWD_VERSION_2));

    	abisHelper.clearNegotiationRecord1Data();
    	OM_G31R01.StartResult startResult = abisHelper.startRequest(this.moClassScf, 0);

    	List<Integer> negotiationRecord1Data = abisHelper.getNegotiationRecord1Data();

    	assertTrue("OmlIwd or RslIwd is not what expected.", abisHelper.compareNegotiationRecord1Data(negotiationRecord1Data, expectedOmlIwdVersions,
    			expectedRslIwdVersions));

    	setTestInfo("Verify that MO State in Start Result is STARTED");
    	assertEquals("MO state is not what expected!", OM_G31R01.Enums.MOState.STARTED, startResult.getMOState());

    	setTestInfo("Verify that MO:GsmSector attribute:abisScfState is STARTED");
    	assertTrue("MO " + MomHelper.SECTOR_LDN + " abisScfState is not STARTED", 
    			momHelper.waitForMoAttributeStringValue(MomHelper.SECTOR_LDN, "abisScfState", "STARTED", 6));
    }
}
