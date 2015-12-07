package com.ericsson.msran.test.grat.startrequest;

import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import se.ericsson.jcat.fw.annotations.Setup;
import se.ericsson.jcat.fw.annotations.Teardown;

import com.ericsson.abisco.clientlib.AbiscoServer;
import com.ericsson.abisco.clientlib.AbiscoServer.Behaviour;
import com.ericsson.abisco.clientlib.servers.OM_G31R01;
import com.ericsson.abisco.clientlib.servers.BG.Enums;
import com.ericsson.abisco.clientlib.servers.BG.SetBehaviourMode;
import com.ericsson.msran.g2.annotations.TestInfo;
import com.ericsson.msran.test.grat.testhelpers.AbisHelper;
import com.ericsson.msran.test.grat.testhelpers.MomHelper;
import com.ericsson.msran.test.grat.testhelpers.NodeStatusHelper;
import com.ericsson.msran.test.grat.testhelpers.prepost.AbisPrePost;
import com.ericsson.msran.jcat.TestBase;

/**
 * @id NodeUC481
 * 
 * @name StartRequestInterventionTest
 * 
 * @author Team Wallaby
 * 
 * @created 2013-11-07
 * 
 * @description This test class verify intervention of Status Request and Reset command
 *              for Start Request SP for SO SCF.
 * 
 * @revision xasahuh 2013-11-28 Updated for usage of Abisco and RBS in TASS.
 * @revision xasahuh 2014-01-31 Finalized implementation using the Abisco Java lib.
 * @revision xasahuh 2014-02-07 Set timeout for test case to 1 minute.
 * @revision xasahuh 2014-02-10 Added TestInfo metadata.
 * @revision xasahuh 2014-02-12 Removed AfterClass, the RestoreStack is used instead.
 * 
 */

public class StartRequestInterventionTest extends TestBase {
    private final OM_G31R01.Enums.MOClass moClass = OM_G31R01.Enums.MOClass.SCF;
   
    private AbisPrePost abisPrePost;
    private AbisHelper abisHelper;
    private MomHelper momHelper;
    
    private final String GSM_SECTOR_LDN = "ManagedElement=1,BtsFunction=1,GsmSector=1";

    private NodeStatusHelper nodeStatus;

    /**
     * Description of test case for test reporting
     */
    @TestInfo(
            tcId = "NodeUC481",
            slogan = "Abis OML downlink SP, intervention by Status Request and Reset Command",
            requirementDocument = "1/00651-FCP 130 1402",
            requirementRevision = "PC5",
            requirementLinkTested = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff87c1d941?docno=1/00651-FCP1301402Uen&format=excel8book",
            requirementLinkLatest = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff86a2af3d?docno=1/00651-FCP1301402Uen&action=current&format=excel8book",
            requirementIds = { "10565-0334/19417[A][APPR]" },
            verificationStatement = "Verifies NodeUC481.A1, NodeUC481.E2",
            testDescription = "Verifies intervention of Start SO SCF by Status Request and Reset Command.",
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
        setTestStepEnd();
    }
    
    /**
     * Configure and send a Start Request that will only send the Start Request
     * without completing the SP.
     * @throws InterruptedException 
     */
    void startStartRequestSP() throws InterruptedException {
        // Create a behaviour command to have the Abisco ignore the
        // Negotiation Request received after sending Start Request 
        setTestStepBegin("Configure the Abisco to ignore Negotiation Request");
        SetBehaviourMode setNegotiationReqNoAck = abisHelper.getBG().createSetBehaviourMode();
        setNegotiationReqNoAck.setBGCommand(Enums.BGCommand.G31NegotiationRequest);
        setNegotiationReqNoAck.setBGBehaviour(Enums.BGBehaviour.ISSUE_NOANSWER);
        setNegotiationReqNoAck.send();
        setTestStepEnd();
        
        // Send a Start Request that won't complete the SP. It will only try to
        // do the first part, ignore Start Result
        OM_G31R01 omServer = abisHelper.getOmServer();
        OM_G31R01.StartRequest startReq;
        startReq = omServer.createStartRequest();
        startReq.getRouting().setTG(0);
        startReq.setMOClass(this.moClass);
        startReq.setAssociatedSOInstance(0xFF);
        startReq.setInstanceNumber(0);
        
        Behaviour behaviour = new Behaviour();
        behaviour.setSP(AbiscoServer.BaseEnums.SP.PART1);
        startReq.setBehaviour(behaviour);
        
        setTestStepBegin("Send a Start Request to start the SP");
        startReq.sendAsync();
        setTestStepEnd();
    }
    
    

    /**
     * @name startSoScfInterventionStatusRequest
     * 
     * @description Verifies intervention of Status Request for Start SO SCF according to NodeUC481.A1
     * 
     * @param testId - unique identifier
     * @param description
     * 
     * @throws InterruptedException
     * @throws Exception
     */

    @Test (timeOut = 360000)
    @Parameters({ "testId", "description" })
    public void startSoScfInterventionStatusRequest(String testId, String description) throws InterruptedException, Exception {
        
        setTestCase(testId, description);
            
        abisHelper.clearStartResult();
        
        setTestInfo("Send Start Request to SO SCF.");
        setTestStepBegin("Send Status Request to SO SCF without waiting for Start Result");
        
        abisHelper.startRequestAsync(this.moClass);
        OM_G31R01.StatusResponse statusResp = abisHelper.statusRequest(this.moClass);
        OM_G31R01.Enums.MOState moState = statusResp.getMOState();
        setTestStepEnd();
        
        setTestStepBegin("Verify that Status Response was receved with MO State RESET or STARTED");
        if (!moState.equals(OM_G31R01.Enums.MOState.RESET) && !moState.equals(OM_G31R01.Enums.MOState.STARTED)) {
            throw new Exception("Invalid MO State in Status Response: " + moState.toString());
        }
        
        OM_G31R01.StartResult startResult = abisHelper.getStartResult();
        setTestStepEnd();

        setTestStepBegin("Verify that MO State in Start Result is STARTED");
        assertEquals(startResult.getMOState(), OM_G31R01.Enums.MOState.STARTED);
        setTestStepEnd();

        setTestStepBegin("Verify that MO:GsmSector attribute:abisSoScfState is STARTED");
        assertTrue("MO " + GSM_SECTOR_LDN + " abisScfState is not STARTED", 
        		momHelper.waitForMoAttributeStringValue(GSM_SECTOR_LDN, "abisScfState", "STARTED", 6));
        setTestStepEnd();
    }
    
    /**
     * @name startSoScfInterventionResetCommand
     * 
     * @description Verifies intervention of Reset Command for Start SO SCF according to NodeUC481.E2
     * 
     * @param testId - unique identifier
     * @param description
     * 
     * @throws InterruptedException
     * @throws Exception
     */
    @Test (timeOut = 360000)
    @Parameters({ "testId", "description" })
    public void startSoScfInterventionResetCommand(String testId, String description) throws InterruptedException, Exception {
        setTestCase(testId, description);
            
        abisHelper.clearStartResult();
        
        setTestInfo("Send Start Request to SO SCF.");
        setTestStepBegin("Send Reset Command to SO SCF without waiting for Start Result");
        
        // Create a behaviour command, restoring Abisco to default behaviour when receiving
        // a Negotiation Request
        SetBehaviourMode setNegotiationReqAck = abisHelper.getBG().createSetBehaviourMode();
        setNegotiationReqAck.setBGCommand(Enums.BGCommand.G31NegotiationRequest);
        setNegotiationReqAck.setBGBehaviour(Enums.BGBehaviour.ISSUE_ACK);
        
        startStartRequestSP();
        
        abisHelper.resetCommand(this.moClass);
        setTestStepEnd();
        
        setTestStepBegin("Verify that MO:GsmSector attribute:abisSoScfState is RESET");
        assertTrue("MO " + GSM_SECTOR_LDN + " abisScfState is not RESET", 
        		momHelper.waitForMoAttributeStringValue(GSM_SECTOR_LDN, "abisScfState", "RESET", 6));
        setTestStepEnd();
        
        setTestInfo("Restore the Negotiation Ack behaviour.");
        setNegotiationReqAck.send();
    }    
    
    /**
     * @name startSoScfInterventionAbortCommand
     * 
     * @description Verifies intervention of Abort Command for Start SO SCF according to NodeUC481.E3
     * 
     * @param testId - unique identifier
     * @param description
     * 
     * @throws InterruptedException
     * @throws Exception
     */
    @Test (timeOut = 360000)
    @Parameters({ "testId", "description" })
    public void startSoScfInterventionAbortCommand(String testId, String description) throws InterruptedException, Exception {
        setTestCase(testId, description);
        
        abisHelper.clearStartResult();
        
        setTestInfo("Send Start Request to SO SCF.");
        setTestStepBegin("Send Reset Command to SO SCF without waiting for Start Result");
        
        // Create a behaviour command, restoring Abisco to default behaviour when receiving
        // a Negotiation Request
        SetBehaviourMode setNegotiationReqAck = abisHelper.getBG().createSetBehaviourMode();
        setNegotiationReqAck.setBGCommand(Enums.BGCommand.G31NegotiationRequest);
        setNegotiationReqAck.setBGBehaviour(Enums.BGBehaviour.ISSUE_ACK);
        
        startStartRequestSP();
        
        abisHelper.abortSpCommand(this.moClass);
        setTestStepEnd();
        
        // since we do not send the negotiation ack in this test case, the mo shall still be reset
        setTestStepBegin("Verify that MO:GsmSector attribute:abisSoScfState is RESET");
        assertTrue("MO " + GSM_SECTOR_LDN + " abisScfState is not RESET", 
        		momHelper.waitForMoAttributeStringValue(GSM_SECTOR_LDN, "abisScfState", "RESET", 6));
        setTestStepEnd();
        
        setTestInfo("Restore the Negotiation Ack behaviour.");
        setNegotiationReqAck.send();
    }
}
