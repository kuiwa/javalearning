package com.ericsson.msran.test.grat.startrequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.json.JSONException;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import se.ericsson.jcat.fw.annotations.Setup;
import se.ericsson.jcat.fw.annotations.Teardown;

import com.ericsson.abisco.clientlib.servers.OM_G31R01;
import com.ericsson.abisco.clientlib.servers.BG.Enums;
import com.ericsson.abisco.clientlib.servers.BG.G31StatusUpdate;
import com.ericsson.abisco.clientlib.servers.BG.G31OperationalCondition;
import com.ericsson.abisco.clientlib.servers.BG.G31OperationalConditionReasonsMap;
import com.ericsson.msran.g2.annotations.TestInfo;
import com.ericsson.msran.test.grat.testhelpers.AbisHelper;
import com.ericsson.msran.test.grat.testhelpers.AbiscoConnection;
import com.ericsson.msran.test.grat.testhelpers.CliCommands;
import com.ericsson.msran.test.grat.testhelpers.MomHelper;
import com.ericsson.msran.test.grat.testhelpers.NodeStatusHelper;
import com.ericsson.msran.test.grat.testhelpers.prepost.AbisPrePost;
import com.ericsson.msran.jcat.TestBase;


/**
 * @id NodeUC476, NodeUC509
 * 
 * @name StartRequestTest
 * 
 * @author Asa Huhtasaari (xasahuh)
 * 
 * @created 2013-09-19
 * 
 * @description This test class verifies the Abis Start Request SP.
 * 
 * @revision xasahuh 2013-09-19 First version.
 * @revision xasahuh 2013-11-28 Updated for usage of Abisco and RBS in TASS.
 * @revision xasahuh 2014-01-30 Start AO TF test case added.   
 * @revision xasahuh 2014-02-06 Updated Start AO TF test case with verification that SO SCF
 *                              cannot be RESET.
 * @revision xasahuh 2014-02-07 Set timeout for test case to 1 minute. 
 * @revision xasahuh 2014-02-10 Added TestInfo metadata.           
 * @revision xasahuh 2014-02-12 Removed AfterClass, the RestoreStack is used instead.
 * @revision ewegans 2014-02-19 Updated the testcase for Start AoTf without sync.             
 */

public class StartRequestTest extends TestBase {
    private final OM_G31R01.Enums.MOClass moClassScf = OM_G31R01.Enums.MOClass.SCF;
    private final OM_G31R01.Enums.MOClass moClassTf = OM_G31R01.Enums.MOClass.TF;
    private final OM_G31R01.Enums.MOClass moClassAt = OM_G31R01.Enums.MOClass.AT;
    
    private AbisPrePost abisPrePost;
    
    private static final String radioClockLdn = "ManagedElement=1,Transport=1,Synchronization=1,RadioEquipmentClock=1";

    private AbisHelper abisHelper;
    private MomHelper momHelper;
    
    private NodeStatusHelper nodeStatus;

    /**
     * Description of test case for test reporting
     */
    @TestInfo(
            tcId = "NodeUC476, NodeUC509, NodeUC644",
            slogan = "Abis SO SCF Start, Abis TF Start, Abis AT Start",
            requirementDocument = "1/00651-FCP 130 1402",
            requirementRevision = "PC5",
            requirementLinkTested = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff87c1d941?docno=1/00651-FCP1301402Uen&format=excel8book",
            requirementLinkLatest = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff86a2af3d?docno=1/00651-FCP1301402Uen&action=current&format=excel8book",
            requirementIds = { "10565-0334/21767[PA1][PREL]", "10565-0334/19034[A][APPR]",
                    "10565-0334/19417[A][APPR]" },
            verificationStatement = "Verifies NodeUC476.N, NodeUC476.E1, NodeUC476.E2, NodeUC476.E3, NodeUC509.N, NodeUC509.E1 and NodeUC644.N",
            testDescription = "Verifies Abis SO SCF Start, TF Start and AT Start.",
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
     * @name startRequestSoScf
     * 
     * @description Verifies the Start Request SP for SO SCF according to NodeUC476.N
     *
     * @param testId - unique identifier
     * @param description
     * 
     * @throws InterruptedException
     */
    @Test (timeOut = 360000)
    @Parameters({ "testId", "description" })
    public void startRequestSoScf(String testId, String description) throws InterruptedException {
        
        setTestCase(testId, description);
        
        // we do this twice, in the first iteration starts the abisScfState is RESET, 
        // and in the second iteration starts, the abisScfState shall be STARTED
        for (int i = 0 ; i < 2 ; ++i)
        {
        	startRequestSoScfImpl();
        }
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

    /**
     * @name startRequestSoScfNegotiationUnsuccessful
     * 
     * @description Verifies the Start Request SP for SO SCF according to
     *              NodeUC476.E1 Case 1: Negotiation Ack lacks OML IWD Case 2:
     *              Negotiation Ack lacks RSL IWD
     *
     * @param testId - unique identifier
     * @param description
     * 
     * @throws InterruptedException
     * @throws JSONException 
     */
    @Test (timeOut = 360000)
    @Parameters({ "testId", "description" })
    public void startRequestSoScfNegotiationUnsuccessful(String testId, String description) throws InterruptedException, JSONException {
        
        setTestCase(testId, description);

        OM_G31R01.StartResult startResult;
        final char[] omlIwdVersion = AbiscoConnection.OML_IWD_VERSION.toCharArray();
        final char[] rslIwdVersion = AbiscoConnection.RSL_IWD_VERSION_1.toCharArray();
  
        setTestStepBegin("Start the SoScf to be able to test the failing negotiation on a started SoScf");
        abisPrePost.startSoScf();
        setTestStepEnd();

        // we do this twice, in the first iteration starts the abisScfState is STARTED, 
        // and in the second iteration starts, the abisScfState shall be RESET
        for (int i = 0 ; i < 2 ; ++i)
        {
        	/*
        	 * Case 1: Send Start Request Answer with Negotiation Ack where OML IWD
        	 * is missing Start Result is received with MO State RESET
        	 */
        	setTestStepBegin("Send Start Request command to SO SCF, and answer with Negotiation ACK with missing OML IWD version");
        	setTestInfo("Send Start Request command to SO SCF, and answer with Negotiation ACK with missing OML IWD version");
        	abisHelper.setNegotiationAckMissingIwd(rslIwdVersion, false);
        	startResult = abisHelper.startRequest(this.moClassScf, 0);

        	setTestInfo("Verify that MO State in Start Result is RESET");
        	assertEquals("MO state is not what expected!", OM_G31R01.Enums.MOState.RESET, startResult.getMOState());
        	setTestStepEnd();


        	// check the state of the GsmSector MO
        	setTestInfo("Verify that MO:GsmSector attributes");
            assertEquals(MomHelper.SECTOR_LDN + " did not reach correct state", "", 
                    momHelper.checkGsmSectorMoAttributeAfterCreateLinksEstablished(MomHelper.SECTOR_LDN, 10));
        	
        	// check the state of the TRX MO        
            setTestInfo("Verify that MO:Trx attributes");
            assertEquals(MomHelper.TRX_LDN + " did not reach correct state", "", 
                    momHelper.checkTrxMoAttributeAfterUnlockNoLinks(MomHelper.TRX_LDN, 10));
            
        	/*
        	 * Case 2: Send Start Request Answer with Negotiation Ack where RSL IWD
        	 * is missing Start Result is received with MO State RESET
        	 */
        	setTestStepBegin("Send Start Request command to SO SCF, and answer with Negotiation ACK with missing RSL IWD version");
        	abisHelper.setNegotiationAckMissingIwd(omlIwdVersion, true);
        	startResult = abisHelper.startRequest(this.moClassScf, 0);

        	setTestInfo("Verify that MO State in Start Result is RESET");
        	assertEquals("MO state is not what expected!", OM_G31R01.Enums.MOState.RESET, startResult.getMOState());

        	// check the state of the GsmSector MO
        	setTestInfo("Verify that MO:GsmSector attributes");
            assertEquals(MomHelper.SECTOR_LDN + " did not reach correct state", "", 
                    momHelper.checkGsmSectorMoAttributeAfterCreateLinksEstablished(MomHelper.SECTOR_LDN, 10));
        	
        	// check the state of the TRX MO
            setTestInfo("Verify that MO:Trx attributes");
            assertEquals(MomHelper.TRX_LDN + " did not reach correct state", "", 
                    momHelper.checkTrxMoAttributeAfterUnlockNoLinks(MomHelper.TRX_LDN, 10));

            setTestStepEnd();
        }
    }

    /**
     * @name startRequestSoScfNegotiationTimeout
     * 
     * @description Verifies the Start Request SP for SO SCF according to
     *              NodeUC476.E2 Case 1: One timeout on Negotiation Request Case
     *              2: Timeout all Negotiation Request attempts
     *
     * @param testId - unique identifier
     * @param description
     *
     * @throws InterruptedException
     */
    @Test (timeOut = 360000)
    @Parameters({ "testId", "description" })
    public void startRequestSoScfNegotiationTimeout(String testId, String description) throws InterruptedException {
        
        setTestCase(testId, description);

        OM_G31R01.StartResult startResult;

        /*
         * Case 1: Send a Start Request Do not answer first Negotiation Request
         * Answer second Negotiation Request Receive Start Result MO State =
         * STARTED
         */
        setTestStepBegin("Send Start Request command to SO SCF, do not answer first Negotiation Request, but answer second");
        abisHelper.setNegotiationTimeoutBehaviour(false);
        startResult = abisHelper.startRequest(this.moClassScf, 0);

        // MO State is expected to be STARTED
        setTestStepBegin("Verify that MO State in Start Result is STARTED");
        assertEquals("MO state is not what expected!", OM_G31R01.Enums.MOState.STARTED, startResult.getMOState());

        setTestStepEnd();
        /*
         * Case 2: Send a Start Request Do not answer Negotiation Request
         * Receive Start Result MO State = RESET
         */
        setTestStepBegin("Send a Start Request Do not answer Negotiation Request");
        setTestInfo("Send Reset Command to SO SCF");
        abisHelper.resetCommand(this.moClassScf);

        // Send Start Request with timeout on negotiation request
        try {
            setTestInfo("Send Start Request command to SO SCF, do not answer Negotiation Request");
            abisHelper.setNegotiationTimeoutBehaviour(true);
            abisHelper.startRequest(this.moClassScf, 0);
        } catch (OM_G31R01.TIMEOUT_EP2Exception e) {
            e.printStackTrace();
        }

        setTestInfo("Verify that MO:GsmSector attribute:abisScfState is RESET");
        assertTrue("MO " + MomHelper.SECTOR_LDN + " abisScfState is not RESET", 
        		momHelper.waitForMoAttributeStringValue(MomHelper.SECTOR_LDN, "abisScfState", "RESET", 6));
        setTestStepEnd();
    }

    /**
     * @name startRequestSoScfNegotiationNak
     * 
     * @description Verifies the Start Request SP for SO SCF according to NodeUC476.E3
     * 
     * @param testId - unique identifier
     * @param description
     * 
     * @throws InterruptedException
     */
    @Test (timeOut = 360000)
    @Parameters({ "testId", "description" })
    public void startRequestSoScfNegotiationNak(String testId, String description) throws InterruptedException {
        
        setTestCase(testId, description);

        final OM_G31R01.Enums.MOState expectedMoState = OM_G31R01.Enums.MOState.RESET;

        // Send Start Request with NAK answer on negotiation request
        setTestInfo("Send Start Request command to SO SCF, answer with Negotiation Request NAK");
        abisHelper.setNegotiationNack();
        // Send Start Request, do not expect any Start Result returned
        try {
            abisHelper.startRequest(this.moClassScf, 0);
        } catch (OM_G31R01.TIMEOUT_EP2Exception e) {
            e.printStackTrace();
        }

        // MO State is expected to remain in RESET
        setTestInfo("Send Status Request to SO SCF");
        OM_G31R01.StatusResponse statusRsp = abisHelper.statusRequest(this.moClassScf);

        setTestInfo("Verify that MO State in Status Response is RESET");
        assertEquals("MO state is not what expected!", expectedMoState, statusRsp.getMOState());

        setTestInfo("Verify that MO:GsmSector attribute:abisScfState is RESET");
        assertTrue("MO " + MomHelper.SECTOR_LDN + " abisScfState is not RESET", 
        		momHelper.waitForMoAttributeStringValue(MomHelper.SECTOR_LDN, "abisScfState", "RESET", 6));
    }
    
    /**
     * @name startRequestAoTf
     * 
     * @description Verifies the Start Request SP for AO TF according to NodeUC509.N,
     *              including verification of that SO SCF cannot be in state reset for TF Start.
     *
     * @param testId - unique identifier
     * @param description  
     *  
     * @throws InterruptedException
     * @throws Exception 
     */
    @Test (timeOut = 360000)
    @Parameters({ "testId", "description" })
    public void startRequestAoTf(String testId, String description) throws InterruptedException, Exception  {
        
        setTestCase(testId, description);

        /**
         * Case 1: Normal
         */
        setTestStepBegin("Start Request to AO TF in state RESET");
        setTestInfo("Precondition: Start SO SCF");
        abisHelper.startRequest(this.moClassScf, 0);
        
        setTestInfo("Send Start Request command to AO TF");
        
        OM_G31R01.StartResult startResult = abisHelper.startRequest(this.moClassTf, 0);
               
        setTestInfo("Verify that MO State in Start Result is DISABLED");
        assertEquals("MO state is not what expected!", OM_G31R01.Enums.MOState.DISABLED, startResult.getMOState());
        
        setTestInfo("Verify that MO:GsmSector attribute:abisTfMoState is DISABLED");
        assertTrue("MO " + MomHelper.SECTOR_LDN + " abisTfState is not DISABLED", 
        		momHelper.waitForMoAttributeStringValue(MomHelper.SECTOR_LDN, "abisTfState", "DISABLED", 6));

        setTestStepEnd();
        /**
         * Case 2: AO TF Start when in Disabled state
         */
        setTestStepBegin("Start Request to AO TF in state DISABLED");
        
        setTestInfo("Send Start Request command to AO TF");
        
        startResult = abisHelper.startRequest(this.moClassTf, 0);
        
        setTestInfo("Verify that MO State in Start Result is DISABLED");
        assertEquals("MO state is not what expected!", OM_G31R01.Enums.MOState.DISABLED, startResult.getMOState());
        
        setTestInfo("Verify that MO:GsmSector attribute:abisTfMoState is DISABLED");
        assertTrue("MO " + MomHelper.SECTOR_LDN + " abisTfState is not DISABLED", 
                momHelper.waitForMoAttributeStringValue(MomHelper.SECTOR_LDN, "abisTfState", "DISABLED", 6));
        
        setTestStepEnd();
        
        /**
         * Case 3: SO SCF is not in state started, TF start shall be rejected.
         */
        setTestStepBegin("Set SO SCF in state RESET, verify that TF Start is rejected");
        abisHelper.resetCommand(this.moClassScf);
        
        try {
            setTestInfo("Send Start Request command to AO TF");
            startResult = abisHelper.startRequest(this.moClassTf, 0);
            
        } catch (OM_G31R01.StartRequestRejectException sre) {
            OM_G31R01.StartRequestReject rejMsg = sre.getStartRequestReject();
            
            setTestInfo("Verify that Start Request Reject result code is 'Wrong state'");
            assertEquals("ResultCode is not what expected!", OM_G31R01.Enums.ResultCode.WrongState, rejMsg.getResultCode());
            setTestStepEnd();
            return;
        }
        
        fail("Expected TF Start Request Reject when SO SCF is RESET");
    }
    
    /**
     * @name startRequestAoTfNoSync
     * 
     * @description Verify TF start when RBS is not synchronized according to NodeUC509.E1
     * 
     * @param testId - unique identifier
     * @param description   
     *   
     * @throws InterruptedException
     * 
     */
    @Test (timeOut = 600000)
    @Parameters({ "testId", "description" })
    public void startRequestAoTfNoSync(String testId, String description) throws InterruptedException {
        // TODO: uncomment commented sections when TRXC can send status update
        setTestCase(testId, description);
        
        if (momHelper.checkMoExist(radioClockLdn))
        {
        	setTestStepBegin("RBS is synchronized, remove sync for the duration of this test.");
            momHelper.deleteSyncMos();
            CliCommands cliCommand = new CliCommands();
            assertTrue("Node didnt come alive after reboot...", cliCommand.rebootAndWaitForNode());
            // links have been released by now, reestablish them
            abisPrePost.establishLinks();
        }
        
        /*final int numOfTrxs = 4;
        setTestStep("Preparation: create " + numOfTrxs + " TRXs");
        for (int i = 1; i <= numOfTrxs; ++i)
        {
            momHelper.createTrxMo(MomHelper.SECTOR_LDN, i);
        }*/

        setTestStepBegin("Precondition: Start SO SCF");
        abisHelper.startRequest(this.moClassScf, 0);
        setTestInfo("Precondition: MO:GsmSector attribute:abisScfState is STARTED");
        assertTrue("MO " + MomHelper.SECTOR_LDN + " abisScfState is not STARTED", 
        		momHelper.waitForMoAttributeStringValue(MomHelper.SECTOR_LDN, "abisScfState", "STARTED", 6));
        
        abisHelper.clearStatusUpdate();
        
        setTestStepBegin("Send Start Request command to AO TF");
        
        OM_G31R01.StartResult startResult = abisHelper.startRequest(this.moClassTf, 0);
        
        setTestStepBegin("Verify that MO State in Start Result is DISABLED");
        assertEquals("MO state is not what expected!", OM_G31R01.Enums.MOState.DISABLED, startResult.getMOState());
        
        //TF and TRXs send Status Update with Operational Condition = Not Operational, Reason = Not Synchronized.

        setTestStepBegin("Receive Status Updates");
        // expect one status update from TF and one status update from each TRXC
        int tfUpdatesReceived = 0;
        //int trxUpdatesReceived = 0;
        G31StatusUpdate statusUpdate = null;
        while ((statusUpdate = abisHelper.getStatusUpdate(5, TimeUnit.SECONDS)) != null)
        {            
            if (statusUpdate.getMOClass() == Enums.MOClass.TF)
            {
                setTestInfo("Verify that TF Operational Condition is Not Operational");
                G31OperationalCondition opCond = statusUpdate.getStatusChoice().getG31StatusAO().getG31OperationalCondition();
                assertEquals("OperationalCondition is not what expected!", Enums.OperationalCondition.NotOperational, opCond.getOperationalCondition());

                setTestInfo("Verify that TF Operational Condition Reasons Map says Not Synchronized (bit number 2)");
                
                G31OperationalConditionReasonsMap opMap = statusUpdate.getStatusChoice().getG31StatusAO().getG31OperationalConditionReasonsMap();
                assertEquals("OperationalConditionReason is not what expected!", 0, opMap.getNoFrameSynch());
                ++tfUpdatesReceived;
            }
            /*else if (statusUpdate.getMOClass() == BG.Enums.MOClass.TRXC)
            {
                setTestInfo("Verify that TRXC instanceNo=" + statusUpdate.getInstanceNumber() +
                        " Operational Condition is Not Operational");
                G31OperationalCondition opCond = statusUpdate.getStatusChoice().getG31StatusTRXC().getG31OperationalCondition();
                assertEquals("OperationalCondition is not what expected!", Enums.OperationalCondition.NotOperational, opCond.getOperationalCondition());
                
                setTestInfo("Verify that TRXC instanceNo=" + statusUpdate.getInstanceNumber() +
                        " Operational Condition Reasons Map is Not Synchronized");
                G31OperationalConditionReasonsMap opMap = statusUpdate.getStatusChoice().getG31StatusTRXC().getG31OperationalConditionReasonsMap();

                assertEquals("OperationalConditionReason is not what expected!", 1, opMap.getNotSynchronized());
                ++trxUpdatesReceived; // the test could also keep track of the individual Trxs
            }*/
            else
            {
            	// status update for SCF can arrive a bit delayd, here while we wait for TF status update, no need to fail.
            	setTestInfo("Received unexpected status update, MO Class = " + statusUpdate.getMOClass());
            }
        }
        
        assertEquals("Did not receive exactly one status update from AO TF.", 1, tfUpdatesReceived);
        //assertEquals("Did not receive status update from all TRXs", numOfTrxs, trxUpdatesReceived);
                 
        setTestStepBegin("Verify that MO:GsmSector attribute:abisTfMoState is DISABLED");
        assertTrue("MO " + MomHelper.SECTOR_LDN + " abisTfState is not DISABLED", 
        		momHelper.waitForMoAttributeStringValue(MomHelper.SECTOR_LDN, "abisTfState", "DISABLED", 6));
        
        /**
         * All TRX MOs in the same GSM Sector MO have attribute operationalState = DISABLED and availabilityStatus=DEPENDENCY_FAILED.
         */
        /*for (int i = 1; i < numOfTrxs + 1; ++i)
        {
            String ldn = MomHelper.SECTOR_LDN + ",Trx=" + i;
            String opState = momHelper.getOpState(ldn);
            assertEquals(ldn + " did not have the expected operational state", "DISABLED", opState);
            String availStatus = momHelper.getAvailStatus(ldn);
            assertEquals(ldn + " did not have the expected availability status", "DEPENDENCY_FAILED", availStatus);
        }*/
        
    }
   
    /**
     * @name startRequestAoAt
     * 
     * @description Verifies the Start Request SP for AO AT according to NodeUC644.N,
     *              including verification of that SO SCF cannot be in state reset for AT Start.
     *
     * @param testId - unique identifier
     * @param description  
     *  
     * @throws InterruptedException
     * @throws Exception 
     */
    @Test (timeOut = 360000)
    @Parameters({ "testId", "description" })
    public void startRequestAoAt(String testId, String description) throws InterruptedException, Exception  {
        
        setTestCase(testId, description);

        /**
         * Case 1: Normal
         */
        setTestStepBegin("Start Request to AO AT in state RESET");
        setTestInfo("Precondition: Start SO SCF");
        abisHelper.startRequest(this.moClassScf, 0);
        
        setTestInfo("Send Start Request command to AO AT");
        
        OM_G31R01.StartResult startResult = abisHelper.startRequest(this.moClassAt, 0);
        
        setTestInfo("Verify that MO State in Start Result is DISABLED");
        assertEquals("MO state is not what expected!", OM_G31R01.Enums.MOState.DISABLED, startResult.getMOState());
        
        setTestInfo("Verify that MO:GsmSector attribute:abisAtMoState is DISABLED");
        assertTrue("MO " + MomHelper.SECTOR_LDN + " abisAtState is not DISABLED", 
                momHelper.waitForMoAttributeStringValue(MomHelper.SECTOR_LDN, "abisAtState", "DISABLED", 6));

        setTestStepEnd();
        /**
         * Case 2: AO AT Start when in Disabled state
         */
        setTestStepBegin("Start Request to AO AT in state DISABLED");
        
        setTestInfo("Send Start Request command to AO AT");
        
        startResult = abisHelper.startRequest(this.moClassAt, 0);
        
        setTestInfo("Verify that MO State in Start Result is DISABLED");
        assertEquals("MO state is not what expected!", OM_G31R01.Enums.MOState.DISABLED, startResult.getMOState());
        
        setTestInfo("Verify that MO:GsmSector attribute:abisAtMoState is DISABLED");
        assertTrue("MO " + MomHelper.SECTOR_LDN + " abisAtState is not DISABLED", 
                momHelper.waitForMoAttributeStringValue(MomHelper.SECTOR_LDN, "abisAtState", "DISABLED", 6));
        
        setTestStepEnd();
        
        /**
         * Case 3: SO SCF is not in state started, AT start shall be rejected.
         */
        setTestStepBegin("Set SO SCF in state RESET, verify that AT Start is rejected");
        abisHelper.resetCommand(this.moClassScf);
        
        try {
            setTestInfo("Send Start Request command to AO AT");
            startResult = abisHelper.startRequest(this.moClassAt, 0);
            
        } catch (OM_G31R01.StartRequestRejectException sre) {
            OM_G31R01.StartRequestReject rejMsg = sre.getStartRequestReject();
            
            setTestInfo("Verify that Start Request Reject result code is 'Wrong state'");
            assertEquals("ResultCode is not what expected!", OM_G31R01.Enums.ResultCode.WrongState, rejMsg.getResultCode());
            setTestStepEnd();
            return;
        }
        
        fail("Expected AT Start Request Reject when SO SCF is RESET");
    }
    
}
