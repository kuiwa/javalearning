package com.ericsson.msran.test.grat.atbundlinginforequest;

import java.util.ArrayList;
import java.util.List;

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
 * @todo Uncomment unused imports when implementing tfConfigurationRequestFsOffset
 */

/**
 * @id NodeUC512
 * 
 * @name ConfigurationRequestTest
 * 
 * @author Asa Huhtasaari
 * 
 * @created 2014-01-30
 * 
 * @description This test class verifies the Configuration Request
 * 
 * @revision xasahuh 2014-01-30 Revised from first version. 
 * @revision xasahuh 2014-02-07 Set timeout for test case to 1 minute.
 * @revision xasahuh 2014-02-10 Added TestInfo metadata.
 * @revision xasahuh 2014-02-11 Removed AfterClass, the RestoreStack is used instead.
 * @revision ewegans 2014-02-24 Updated tfConfigurationRequestFsOffset
 * @revision emaomar 2014-04-09 Set Cluster Group Id to undefined in case2
 *
 */

public class AtBundlingInfoRequestTest extends TestBase {
    
    private AbisPrePost abisPrePost;
    private final OM_G31R01.Enums.MOClass moClassAt = OM_G31R01.Enums.MOClass.AT;

    private AbisHelper abisHelper;
    private MomHelper momHelper;
    
    private final String GSM_SECTOR_LDN = MomHelper.SECTOR_LDN; //ManagedElement=1,BtsFunction=1,GsmSector=1"
    private NodeStatusHelper nodeStatus;

    /**
     * Description of test case for test reporting
     */
    @TestInfo(
            tcId = "NodeUC512",
            slogan = "Abis TF Configuration",
            requirementDocument = "1/00651-FCP 130 1402",
            requirementRevision = "PC5",
            requirementLinkTested = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff87c1d941?docno=1/00651-FCP1301402Uen&format=excel8book",
            requirementLinkLatest = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff86a2af3d?docno=1/00651-FCP1301402Uen&action=current&format=excel8book",
            requirementIds = { "10565-0334/21767[PA1][PREL]", "10565-0334/19034[A][APPR]",
                    "10565-0334/19417[A][APPR]" },
            verificationStatement = "Verifies NodeUC512.N and NodeUC512.E1",
            testDescription = "Verifies Abis TF Configuration.",
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
     * @name atBundlingInfoRequest in state disabled
     * 
     * @description Verifies AT Configuration Request according to NodeUC648.N, NodeUC479.E2 and NodeUC479.E1
     * 
     * @param testId - unique identifier
     * @param description
     * 
     * @throws Exception 
     * 
     */
    @Test (timeOut = 60000)
    @Parameters({ "testId", "description" })
    public void atBundlingInfoRequest(String testId, String description) throws Exception {
        setTestCase(testId, description);
    	//checks that abisScfOmlState = UP and abisAtState = DISABLED
        saveAssertEquals("GsmSector MO attributes did not reach expected values", "", momHelper.checkGsmSectorMoAttributeAfterAllMosStarted(GSM_SECTOR_LDN, 5));
        
        //BUNDLING INFO REQ with no AT Config, config signature must be 0
        OM_G31R01.ATBundlingInfoResponse firstResponse = abisHelper.atBundlingInfoRequest(1, 0);
        saveAssertEquals("Configuration Signature must be 0", 0, firstResponse.getConfigurationSignature().intValue());

        //CONFIGURE AT
    	OM_G31R01.ATConfigurationResult confRes = abisHelper.atConfigRequest(0x11, 0x06, 0x08, 0x33, 0x2E, 0x1C);
    	saveAssertTrue("", confRes.getMOClass() == OM_G31R01.Enums.MOClass.AT);
    	saveAssertTrue("", confRes.getAssociatedSOInstance() == 0xFF);
    	saveAssertTrue("", confRes.getInstanceNumber() == 0x0);
    	saveAssertEquals("AccordanceIndication don't match Request", 
    			Enums.AccordanceIndication.AccordingToRequest, confRes.getAccordanceIndication().getAccordanceIndication());

    	//ENABLE AT
    	OM_G31R01.EnableResult enableResult = abisHelper.enableRequest(this.moClassAt, 0);
    	saveAssertEquals("AO AT is not ENABLED", OM_G31R01.Enums.MOState.ENABLED, enableResult.getMOState());
    	
    	List<Integer> bundlingGroupOctets = new ArrayList<Integer>();
    	
    	bundlingGroupOctets.add(0x3);  //nr of bundling groups
    	bundlingGroupOctets.add(0x5);  //length of group1
    	bundlingGroupOctets.add(0x1);  //id of group1
        bundlingGroupOctets.add(0x11); //SAPI OML (0x10) and RSL (0x1)
    	bundlingGroupOctets.add(0x1);  //nr of TEI
    	bundlingGroupOctets.add(0x0);  //TEI
    	bundlingGroupOctets.add(0x5);  //length of group2
    	bundlingGroupOctets.add(0x2);  //id of group2
    	bundlingGroupOctets.add(0x6); //SAPI CS and CSD
    	bundlingGroupOctets.add(0x1);  //nr of TEI
    	bundlingGroupOctets.add(0x0);  //TEI
    	bundlingGroupOctets.add(0x5);  //length of group3
    	bundlingGroupOctets.add(0x3);  //id of group3
        bundlingGroupOctets.add(0x8); //SAPI PS
    	bundlingGroupOctets.add(0x1);  //nr of TEI
    	bundlingGroupOctets.add(0x0);  //TEI
    	
        //BUNDLING INFO REQUEST positive
    	final int NUM_OF_RETRIES = 5;
    	for (int i=0 ; i < NUM_OF_RETRIES ; ++i) {
    		setTestStepBegin("AT Bundling Request after AT Enable, iteration=" + i);
    		OM_G31R01.ATBundlingInfoResponse response = abisHelper.atBundlingInfoRequest(1, 0);
    		saveAssertEquals("MO Class must be AT", response.getMOClass(), OM_G31R01.Enums.MOClass.AT);
    		saveAssertEquals("Associated SO instance number must be 0xFF", response.getAssociatedSOInstance(), 0xFF);
    		saveAssertEquals("MO instance number must be 0", confRes.getInstanceNumber(), 0x0);
    		saveAssertEquals("List Number should be 1", 1, response.getListNumber().getListNumber());
    		saveAssertEquals("End List Number should be 1", 1, response.getEndListNumber().getEndListNumber());
    		saveAssertTrue("Bundling Signature must be != 0", response.getBundlingSignature() != 0);
    		saveAssertTrue("Configuration Signature must be != 0", response.getConfigurationSignature() != 0);
    		
    		if (bundlingGroupOctets.equals(response.getBundlingGroups().getBundlingGroupsData())) {
    			// received the expected bundling group information
    			break;
    		} else if (i == (NUM_OF_RETRIES - 1)) {
    			fail("Mismatch in Bundling Group Info: Expected: " + bundlingGroupOctets + " Actual: " + response.getBundlingGroups().getBundlingGroupsData());
    		} else {
    			Thread.sleep(1000);
    		}
    		setTestStepEnd();
    	}
    	
        //wrong param List Number => PROTOCOL_ERROR, NodeUC479.E1
        try {
        	abisHelper.atBundlingInfoRequest(2, 0); //wrong List Number
        	fail("AT Bundling Info Reject must be received with error code Protocol Error");
        } catch (OM_G31R01.ATBundlingInfoRejectException rej) {
    		saveAssertEquals("Error Code must be ProtocolError: ", Enums.ResultCode.ProtocolError, rej.getATBundlingInfoReject().getResultCode());
    	}
        
        //wrong state, NodeUC479.E2
        abisHelper.resetCommand(moClassAt);
        try {
        	abisHelper.atBundlingInfoRequest(1, 0);
        	fail("AT Bundling Info Reject must be received with error code Wrong State");
        } catch (OM_G31R01.ATBundlingInfoRejectException rej) {
    		saveAssertEquals("Error Code must be WrongState: ", Enums.ResultCode.WrongState, rej.getATBundlingInfoReject().getResultCode());
    	}
    }
}
