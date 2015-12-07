package com.ericsson.msran.test.grat.configurationrequest;

import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.ericsson.msran.jcat.TestBase;
import se.ericsson.jcat.fw.annotations.Setup;
import se.ericsson.jcat.fw.annotations.Teardown;

import com.ericsson.abisco.clientlib.servers.OM_G31R01;
import com.ericsson.abisco.clientlib.servers.OM_G31R01.Enums;

import com.ericsson.msran.g2.annotations.TestInfo;

import com.ericsson.msran.test.grat.testhelpers.AbisHelper;
import com.ericsson.msran.test.grat.testhelpers.MomHelper;
import com.ericsson.msran.test.grat.testhelpers.NodeStatusHelper;
import com.ericsson.msran.test.grat.testhelpers.prepost.AbisPrePost;
        

/**
 * @id NodeUC645
 * 
 * @name AtConfigurationRequestTest
 * 
 * @author GRAT 2014
 * 
 * @created 2014-10-15
 * 
 * @description This test class verifies the At Configuration Request
 * 
 * @revision ewegans 2014-10-15 Moved tests here from ConfigurationRequestTest
 *
 */

public class AtConfigurationRequestTest extends TestBase {
    
    private AbisPrePost abisPrePost;
    private final OM_G31R01.Enums.MOClass moClassAt = OM_G31R01.Enums.MOClass.AT;

    private AbisHelper abisHelper;
    private MomHelper momHelper;
    
    private final String GSM_SECTOR_LDN = "ManagedElement=1,BtsFunction=1,GsmSector=1";
    private NodeStatusHelper nodeStatus;

    /**
     * Description of test case for test reporting
     */
    @TestInfo(
            tcId = "NodeUC645",
            slogan = "Abis TF Configuration",
            requirementDocument = "1/00651-FCP 130 1402",
            requirementRevision = "PC5",
            requirementLinkTested = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff87c1d941?docno=1/00651-FCP1301402Uen&format=excel8book",
            requirementLinkLatest = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff86a2af3d?docno=1/00651-FCP1301402Uen&action=current&format=excel8book",
            requirementIds = { "10565-0334/21767[PA1][PREL]", "10565-0334/19034[A][APPR]",
                    "10565-0334/19417[A][APPR]" },
            verificationStatement = "Verifies NodeUC645",
            testDescription = "Verifies Abis AT Configuration.",
            traceGuidelines = "N/A")

    
    /**
     * Create MO:s, establish links to Abisco, and verify preconditions.
     * 
     * @throws InterruptedException
     */
    @Setup
    public void setup() throws InterruptedException {
    	setTestStepBegin("Setup");
        nodeStatus = new NodeStatusHelper();
        assertTrue("Node did not reach a working state before test start", nodeStatus.waitForNodeReady());
        momHelper = new MomHelper();
        abisHelper = new AbisHelper();
        abisPrePost = new AbisPrePost();
        abisPrePost.preCondScfMoStateStarted();
        setTestStepEnd();
    }
    
    /**
     * Postcond.
     */
    @Teardown
    public void teardown() {
    	setTestStepBegin("Teardown");
        nodeStatus.isNodeRunning();
        setTestStepEnd();
    }
    
    /**
     * @name atConfigurationRequestInStateDisabled
     * 
     * @description Verifies AT Configuration Request according to NodeUC645.N and NodeUC645.A2
     * 
     * @param testId - unique identifier
     * @param description
     * 
     * @throws Exception 
     * 
     */
    @Test (timeOut = 60000)
    @Parameters({ "testId", "description" })
    public void atConfigurationRequestInStateDisabled(String testId, String description) throws Exception {
        setTestCase(testId, description);
    	//checks that abisScfOmlState = UP and abisAtState = DISABLED
        saveAssertEquals("GsmSector MO attributes did not reach expected values", "", momHelper.checkGsmSectorMoAttributeAfterAllMosStarted(GSM_SECTOR_LDN, 5));
        OM_G31R01.ATConfigurationResult confRes = null;
        for (int i = 0; i < 3; ++i) {
        	if (i < 2) {
        		confRes = abisHelper.atConfigRequest(0x11, 0x06, 0x08, 0x33, 0x2E, 0x1C); //same data twice
        	} else {
        		confRes = abisHelper.atConfigRequest(0x11, 0x06, 0x08, 0x33, 0x2E, 0x1D); //different data last time
        	}
        	saveAssertTrue("", confRes.getMOClass() == OM_G31R01.Enums.MOClass.AT);
        	saveAssertTrue("", confRes.getAssociatedSOInstance() == 0xFF);
        	saveAssertTrue("", confRes.getInstanceNumber() == 0x0);
        	saveAssertEquals("AccordanceIndication don't match Request", 
        			         Enums.AccordanceIndication.AccordingToRequest, confRes.getAccordanceIndication().getAccordanceIndication());
        }
    }
    
    /**
     * @name atConfigurationRejectInconsitentDataInStateDisabled
     * 
     * @description Verifies AT Configuration Request according to NodeUC645.E1
     * 
     * @param testId - unique identifier
     * @param description
     * 
     * @throws Exception 
     * 
     */
    @Test (timeOut = 60000)
    @Parameters({ "testId", "description" })
    public void atConfigurationRejectInconsitentDataInStateDisabled(String testId, String description) throws Exception {
        setTestCase(testId, description);
    	try {
    		abisHelper.atConfigRequest(0x11, 0x06, 0x0C, 0x33, 0x2E, 0x1C); //repeating SAPIs, last one should be 8
    		fail("Expected AtT Configuration Requested not received");
    	} catch (OM_G31R01.ATConfigurationRequestRejectException rej) {
    		saveAssertEquals("Not ProtocolError: ", Enums.ResultCode.ProtocolError, rej.getATConfigurationRequestReject().getResultCode());
    	}
    	
    	abisHelper.resetCommand(this.moClassAt);
    	try {
    		abisHelper.atConfigRequest(0x11, 0x06, 0x08, 0x33, 0x2E, 0x1C); //worng state in RESET
    		fail("Expected AtT Configuration Requested not received");
    	} catch (OM_G31R01.ATConfigurationRequestRejectException rej) {
    		saveAssertEquals("Not ProtocolError: ", Enums.ResultCode.WrongState, rej.getATConfigurationRequestReject().getResultCode());
    	}   	
    }
    
    /**
     * @name atConfigurationRequestInStateEnabledThenDisabled
     * 
     * @description Verifies AT Configuration Request according to NodeUC645.A1, NodeUC645.E2 and NodeUC645.E3
     * 
     * @param testId - unique identifier
     * @param description
     * 
     * @throws Exception 
     * 
     */
    @Test (timeOut = 60000)
    @Parameters({ "testId", "description" })
    public void atConfigurationRequestInStateEnabledThenDisabled(String testId, String description) throws Exception {
        setTestCase(testId, description);
    	//first a valid configuration in state DISABLED
    	saveAssertEquals("GsmSector MO attributes did not reach expected values", "", momHelper.checkGsmSectorMoAttributeAfterAllMosStarted(GSM_SECTOR_LDN, 5));
    	OM_G31R01.ATConfigurationResult confRes = abisHelper.atConfigRequest(0x11, 0x06, 0x08, 0x33, 0x2E, 0x1C);
    	saveAssertTrue("", confRes.getMOClass() == OM_G31R01.Enums.MOClass.AT);
    	saveAssertTrue("", confRes.getAssociatedSOInstance() == 0xFF);
    	saveAssertTrue("", confRes.getInstanceNumber() == 0x0);
    	saveAssertEquals("AccordanceIndication don't match Request", 
    			Enums.AccordanceIndication.AccordingToRequest, confRes.getAccordanceIndication().getAccordanceIndication());

    	//ENABLE
    	OM_G31R01.EnableResult enableResult = abisHelper.enableRequest(this.moClassAt, 0);
    	saveAssertEquals("AO AT is not ENABLED", OM_G31R01.Enums.MOState.ENABLED, enableResult.getMOState());
    	
    	//same config in state ENABLED,  NodeUC645.A1 
    	confRes = abisHelper.atConfigRequest(0x11, 0x06, 0x08, 0x33, 0x2E, 0x1C);
    	saveAssertEquals("AccordanceIndication don't match Request", 
    			Enums.AccordanceIndication.AccordingToRequest, confRes.getAccordanceIndication().getAccordanceIndication());
    	
    	//different config in state ENABLED,  NodeUC645.E3 
    	confRes = abisHelper.atConfigRequest(0x11, 0x06, 0x08, 0x33, 0x2E, 0x1D); //different data
    	saveAssertEquals("AccordanceIndication don't match Request", 
    			Enums.AccordanceIndication.NotAccordingToRequest, confRes.getAccordanceIndication().getAccordanceIndication());
    	
    	//DISABLE
    	OM_G31R01.DisableResult disableResult = abisHelper.disableRequest(this.moClassAt);
    	saveAssertEquals("AO AT is not ENABLED", OM_G31R01.Enums.MOState.DISABLED, disableResult.getMOState());
    	
    	//different data in state DISABLED, NodeUC645.E2
    	try {
    		abisHelper.atConfigRequest(0x11, 0x06, 0x08, 0x33, 0x2E, 0x1D); //different data
    		fail("Expected AtT Configuration Requested not received");
    	} catch (OM_G31R01.ATConfigurationRequestRejectException rej) {
    		saveAssertEquals("Not ProtocolError: ", Enums.ResultCode.FuncNotSupported, rej.getATConfigurationRequestReject().getResultCode());
    	}
    	
    	//same config in state DISABLED,  NodeUC645.A2 
    	confRes = abisHelper.atConfigRequest(0x11, 0x06, 0x08, 0x33, 0x2E, 0x1C);
    	saveAssertEquals("AccordanceIndication don't match Request", 
    			Enums.AccordanceIndication.AccordingToRequest, confRes.getAccordanceIndication().getAccordanceIndication());
    }
    
    /**
     * @name atConfigurationRequestInWrongSoScfState
     * 
     * @description Verifies AT Configuration Request according to NodeUC479.E2
     * 
     * @param testId - unique identifier
     * @param description
     * 
     * @throws Exception 
     * 
     */
    @Test (timeOut = 60000)
    @Parameters({ "testId", "description" })
    public void atConfigurationRequestInWrongSoScfState(String testId, String description) throws Exception {
        setTestCase(testId, description);       
    	
    	setTestStepBegin("Reset SO SCF, and check the GsmSector MO state");
    	abisHelper.resetCommand(OM_G31R01.Enums.MOClass.SCF);
    	saveAssertEquals("GsmSector MO attributes did not reach expected values", "", momHelper.checkGsmSectorMoAttributeAfterCreateLinksEstablished(GSM_SECTOR_LDN, 5));

    	setTestStepBegin("Send an AT Config when SO SCF is in state RESET");
    	try {
    		abisHelper.atConfigRequest(0x11, 0x06, 0x08, 0x33, 0x2E, 0x1C); 
    		fail("Expected AT Configuration Request Reject not received");
    	} catch (OM_G31R01.ATConfigurationRequestRejectException rej) {
    		saveAssertEquals("Not WrongState: ", Enums.ResultCode.WrongState, rej.getATConfigurationRequestReject().getResultCode());
    	}   	
    }   
}
