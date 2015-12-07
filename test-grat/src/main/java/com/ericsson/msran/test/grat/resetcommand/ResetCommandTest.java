package com.ericsson.msran.test.grat.resetcommand;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import se.ericsson.jcat.fw.annotations.Setup;
import se.ericsson.jcat.fw.annotations.Teardown;

import com.ericsson.abisco.clientlib.servers.OM_G31R01;
import com.ericsson.abisco.clientlib.servers.BG.Enums.OperationalCondition;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.ChannelActAck;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.ChannelActNegAckException;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.ChannelActNormalAssign;
import com.ericsson.msran.g2.annotations.TestInfo;
import com.ericsson.msran.test.grat.testhelpers.AbisHelper;
import com.ericsson.msran.test.grat.testhelpers.MomHelper;
import com.ericsson.msran.test.grat.testhelpers.NodeStatusHelper;
import com.ericsson.msran.test.grat.testhelpers.prepost.AbisPrePost;
import com.ericsson.msran.jcat.TestBase;

/**
 * @id NodeUC477, NodeUC510
 * 
 * @name ResetCommandTest
 * 
 * @author Helena Larsson
 * 
 * @created 2013-10-15
 * 
 * @description This test class verifies the Reset Command. It also verifies the blocking structure
 *              when Reset Command is sent.
 * 
 * @revision         2013-10-15 First version. 
 * @revision xasahuh 2013-10-24 Minor updates in beforeClass. Uncommented check for abisSoScfState RESET. 
 * @revision xasahuh 2013-11-21 Updated for usage of Abisco and RBS in TASS. Moved to msr-test.
 * @revision xasahuh 2014-01-30 Reset AO TF test case added.
 * @revision xasahuh 2014-02-04 Extended NodeUC477 (Reset SO SCF) with check of reset of AO TF.
 * @revision xasahuh 2014-02-07 Set timeout for test case to 1 minute.
 * @revision xasahuh 2014-02-10 Added TestInfo metadata.
 * @revision xasahuh 2014-02-12 Removed AfterClass, the RestoreStack is used instead.
 * 
 */

public class ResetCommandTest extends TestBase {
    private final OM_G31R01.Enums.MOClass  moClassScf = OM_G31R01.Enums.MOClass.SCF;
    private final OM_G31R01.Enums.MOClass  moClassTrxc = OM_G31R01.Enums.MOClass.TRXC;
    private final OM_G31R01.Enums.MOClass  moClassTf = OM_G31R01.Enums.MOClass.TF;
    private final OM_G31R01.Enums.MOClass  moClassAt = OM_G31R01.Enums.MOClass.AT;
    private final OM_G31R01.Enums.MOClass  moClassTx = OM_G31R01.Enums.MOClass.TX;
    private final OM_G31R01.Enums.MOClass  moClassRx = OM_G31R01.Enums.MOClass.RX;
    private final OM_G31R01.Enums.MOClass  moClassTs = OM_G31R01.Enums.MOClass.TS;
    private static final Logger myLogger = Logger.getLogger(ResetCommandTest.class);
    
    private AbisPrePost abisPrePost;
    private AbisHelper abisHelper;
    private MomHelper momHelper;
    
    private final String GSM_SECTOR_LDN = "ManagedElement=1,BtsFunction=1,GsmSector=1";
    private NodeStatusHelper nodeStatus;

    /**
     * Description of test case for test reporting
     */
    @TestInfo(
            tcId = "NodeUC477, NodeUC510, NodeUC643",
            slogan = "Abis SO SCF Reset, Abis TF Reset, Abis AT Reset",
            requirementDocument = "1/00651-FCP 130 1402",
            requirementRevision = "PC5",
            requirementLinkTested = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff87c1d941?docno=1/00651-FCP1301402Uen&format=excel8book",
            requirementLinkLatest = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff86a2af3d?docno=1/00651-FCP1301402Uen&action=current&format=excel8book",
            requirementIds = { "10565-0334/21767[PA1][PREL]", "10565-0334/19034[A][APPR]",
                    "10565-0334/19417[A][APPR]" },
            verificationStatement = "Verifies NodeUC477.N, NodeUC501.N and NodeUC643",
            testDescription = "Verifies Abis SO SCF Reset, TF Reset and AT Reset.",
            traceGuidelines = "N/A")


    /**
     * Create MO:s, establish links to Abisco, and verify preconditions.
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
        momHelper = new MomHelper();
        abisPrePost = new AbisPrePost();
        abisPrePost.preCondAllMoStateStarted();
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
     * @name channelActivationNormalAssignment
     * 
     * @description Verifies channelActivationNormalAssignment 
     * 
     * @param tn - timeslot number
     * 
     * @throws InterruptedException
     */
   public void channelActivationNormalAssignment (int tn) throws InterruptedException {

	   	ChannelActNormalAssign msg;
    	msg = abisHelper.channelActivationNormalAssignment (tn); 
    	try {
            myLogger.debug(msg.toString());

            ChannelActAck ack = msg.send();
            myLogger.debug(ack.toString());
            
            fail("Error: Expected channel activation to be rejected due to reset command");
            
        } catch (ChannelActNegAckException e) {
            setTestInfo("Expected since all channels on the TS have been deactivated");
        }
        
    }
    
    
    /**
     * @name resetCommandSoScf
     * 
     * @description Verifies Reset Command EP for SO SCF according to NodeUC477.N.
     *              Also verifies that AO TF is reset.
     *              
     * @param testId - unique identifier
     * @param description
     * 
     * @throws InterruptedException
     */
    @Test (timeOut = 360000)
    @Parameters({ "testId", "description" })
    public void resetCommandSoScf(String testId, String description) throws InterruptedException, JSONException {
    	
        setTestCase(testId, description);

        // running this twice, in the first iteration the abisScfState is STARTED (from tc setup), 
        // in the second iteration the abisScfState is RESET
        for (int i=0 ; i < 2 ; ++i) {
        	setTestStepBegin("Send Reset Command to SO SCF");

        	OM_G31R01.ResetComplete result = abisHelper.resetCommand(this.moClassScf);

        	setTestStepBegin("Verify that Reset Complete is received from SO SCF");
        	assertEquals("Not from SO SCF", this.moClassScf, result.getMOClass());
        	assertEquals("AssociatedSOInstance is not 0xFF", 0xFF, result.getAssociatedSOInstance());
        	assertEquals("InstanceNumber is not 0", 0, result.getInstanceNumber());


        	// check the state of the GsmSector MO
        	setTestInfo("Verify that MO:GsmSector attribute:abisScfState is RESET");
        	assertTrue("MO " + MomHelper.SECTOR_LDN + " abisScfState is not RESET", 
        			momHelper.waitForMoAttributeStringValue(MomHelper.SECTOR_LDN, "abisScfState", "RESET", 6));

        	setTestInfo("Verify that MO:GsmSector attribute:abisTfState is RESET");
        	assertTrue("MO " + MomHelper.SECTOR_LDN + " abisTfState is not RESET", 
        			momHelper.waitForMoAttributeStringValue(MomHelper.SECTOR_LDN, "abisTfState", "RESET", 6));

        	setTestInfo("Verify that MO:GsmSector attribute:abisAtState is RESET");
        	assertTrue("MO " + MomHelper.SECTOR_LDN + " abisAtState is not RESET", 
        			momHelper.waitForMoAttributeStringValue(MomHelper.SECTOR_LDN, "abisAtState", "RESET", 6));

        	// check the state of the TRX MO
        	setTestInfo("Verify that MO:GsmSector attribute:abisTrxcState is RESET");
        	assertTrue("MO " + MomHelper.TRX_LDN + " abisTrxcState is not RESET", 
        			momHelper.waitForMoAttributeStringValue(MomHelper.TRX_LDN, "abisTrxcState", "RESET", 6));        

        	setTestInfo("Verify that MO:GsmSector attribute:abisTxState is RESET");
        	assertTrue("MO " + MomHelper.TRX_LDN + " abisTxState is not RESET", 
        			momHelper.waitForMoAttributeStringValue(MomHelper.TRX_LDN, "abisTxState", "RESET", 6));        

        	setTestInfo("Verify that MO:GsmSector attribute:abisRxState is RESET");
        	assertTrue("MO " + MomHelper.TRX_LDN + " abisRxState is not RESET", 
        			momHelper.waitForMoAttributeStringValue(MomHelper.TRX_LDN, "abisRxState", "RESET", 6));        

        	setTestInfo("Verify that MO:GsmSector attribute:abisTsState is RESET");
        	abisPrePost.checkAbisTsMoState("RESET");
     
        	OM_G31R01.StatusResponse scfStatusRsp = abisHelper.statusRequest(this.moClassScf);
        	saveAssertEquals("Operational Condition must be Operational for SO SCF", OperationalCondition.Operational.getValue(), scfStatusRsp.getStatusChoice().getStatusSCF().getOperationalCondition().getOperationalCondition().getValue());
        	saveAssertEquals("Operational Condition Reason Map must be empty for SO SCF", null, scfStatusRsp.getStatusChoice().getStatusSCF().getOperationalConditionReasonsMap());
        	saveAssertEquals("Operational Condition Text must be empty for SO SCF", null, scfStatusRsp.getStatusChoice().getStatusSCF().getOperationalConditionText());
        }
    }
     
    /**
     * 
     * @name resetCommandAoTf
     * 
     * @description Verifies Reset Command EP for AO TF according to NodeUC510.N and UC479.E2
     * 
     * @param testId - unique identifier
     * @param description
     * 
     * @throws InterruptedException
     */
    @Test (timeOut = 360000)
    @Parameters({ "testId", "description" })
    public void resetCommandAoTf(String testId, String description) throws InterruptedException {
       
        setTestCase(testId, description);
        
        /**
         * Case 1: Reset Command in AO TF state ENABLED (after setup)
         */
        abisHelper.resetCommand(this.moClassTf);
        setTestStepBegin("Abis TF Reset in state ENABLED");
        setTestInfo("Send Reset Command to AO TF");
        OM_G31R01.ResetComplete result = abisHelper.resetCommand(this.moClassTf);
        setTestInfo("Verify that Reset Complete is received from AO TF");
        assertEquals("Not from AO TF", this.moClassTf, result.getMOClass());
        assertEquals("AssociatedSOInstance is not 0xFF", 0xFF, result.getAssociatedSOInstance());
        assertEquals("InstanceNumber is not 0", 0, result.getInstanceNumber());
                
        setTestInfo("Verify that MO:GsmSector attribute:abisTfMoState is RESET");
        assertTrue("MO " + GSM_SECTOR_LDN + " abisTfState is not RESET", 
                momHelper.waitForMoAttributeStringValue(GSM_SECTOR_LDN, "abisTfState", "RESET", 6));    
      
        //check abisClusterGroupId and abisTfMode state for WP4510
        setTestInfo("Verify that MO:GsmSector attribute:abisClusterGroupId is empty");
        assertTrue("MO " + GSM_SECTOR_LDN + " abisClusterGroupId is not empty", 
                    momHelper.waitForMoAttributeStringValue(MomHelper.SECTOR_LDN, "abisClusterGroupId", "", 6));

        setTestInfo("Verify that MO:GsmSector attribute:abisTfMode is UNDEFINED");
        assertTrue("MO " + GSM_SECTOR_LDN + " abisClusterGroupId is not UNDEFINED",
                    momHelper.waitForMoAttributeStringValue(MomHelper.SECTOR_LDN, "abisTfMode", "UNDEFINED", 6));
        
        /**
         * Case 2: Reset Command in AO TF state RESET
         */
        abisHelper.resetCommand(this.moClassTf);
        setTestStepBegin("Abis TF Reset in state RESET");
        setTestInfo("Send Reset Command to AO TF");
        result = abisHelper.resetCommand(this.moClassTf);
        
        setTestInfo("Verify that Reset Complete is received from AO TF");
    	assertEquals("Not from AO TF", this.moClassTf, result.getMOClass());
    	assertEquals("AssociatedSOInstance is not 0xFF", 0xFF, result.getAssociatedSOInstance());
    	assertEquals("InstanceNumber is not 0", 0, result.getInstanceNumber());

        setTestInfo("Verify that MO:GsmSector attribute:abisTfMoState is RESET");
        assertTrue("MO " + GSM_SECTOR_LDN + " abisTfState is not RESET", 
        		momHelper.waitForMoAttributeStringValue(GSM_SECTOR_LDN, "abisTfState", "RESET", 6));
        
       //check abisClusterGroupId and abisTfMode state for WP4510
        setTestInfo("Verify that MO:GsmSector attribute:abisClusterGroupId is empty");
        assertTrue("MO " + GSM_SECTOR_LDN + " abisClusterGroupId is not empty", 
                    momHelper.waitForMoAttributeStringValue(MomHelper.SECTOR_LDN, "abisClusterGroupId", "", 6));
        
        setTestInfo("Verify that MO:GsmSector attribute:abisTfMode is UNDEFINED");
        assertTrue("MO " + GSM_SECTOR_LDN + " abisClusterGroupId is not UNDEFINED",
                    momHelper.waitForMoAttributeStringValue(MomHelper.SECTOR_LDN, "abisTfMode", "UNDEFINED", 6));
        setTestStepEnd();
        
        /**
         * Case 3: Reset Command in AO TF state DISABLED
         */
        setTestStepBegin("Abis TF Reset in state DISABLED");
        setTestInfo("Precondition: Send Start Command to AO TF");
        abisHelper.startRequest(this.moClassTf, 0);

        setTestInfo("Precondition: Verify that MO:GsmSector attribute:abisTfMoState is DISABLE");
        assertTrue("MO " + GSM_SECTOR_LDN + " abisTfState is not DISABLE", 
                momHelper.waitForMoAttributeStringValue(GSM_SECTOR_LDN, "abisTfState", "DISABLE", 6));

        setTestInfo("Send Reset Command to AO TF");
        result = abisHelper.resetCommand(this.moClassTf);

        setTestInfo("Verify that Reset Complete is received from AO TF");
    	assertEquals("Not from AO TF", this.moClassTf, result.getMOClass());
    	assertEquals("AssociatedSOInstance is not 0xFF", 0xFF, result.getAssociatedSOInstance());
    	assertEquals("InstanceNumber is not 0", 0, result.getInstanceNumber());
                
        setTestInfo("Verify that MO:GsmSector attribute:abisTfMoState is RESET");
        assertTrue("MO " + GSM_SECTOR_LDN + " abisTfState is not RESET", 
                momHelper.waitForMoAttributeStringValue(GSM_SECTOR_LDN, "abisTfState", "RESET", 6));
       //check abisClusterGroupId and abisTfMode state for WP4510
        setTestInfo("Verify that MO:GsmSector attribute:abisClusterGroupId is empty");
        assertTrue("MO " + GSM_SECTOR_LDN + " abisClusterGroupId is not empty", 
                    momHelper.waitForMoAttributeStringValue(MomHelper.SECTOR_LDN, "abisClusterGroupId", "", 6));
        
        setTestInfo("Verify that MO:GsmSector attribute:abisTfMode is UNDEFINED");
        assertTrue("MO " + GSM_SECTOR_LDN + " abisClusterGroupId is not UNDEFINED",
                    momHelper.waitForMoAttributeStringValue(MomHelper.SECTOR_LDN, "abisTfMode", "UNDEFINED", 6));
        setTestStepEnd();
        
        //UC479.E2
        abisHelper.resetCommand(this.moClassScf);
        try {
        	abisHelper.resetCommand(this.moClassTf);
        	fail("Reset Reject must be received here");
        } catch (OM_G31R01.ResetRejectException rej) {
        	saveAssertEquals("Error Code must be Wrong State", OM_G31R01.Enums.ResultCode.WrongState, rej.getResetReject().getResultCode());
        }
       
    }
//    
//    /**
//     * @todo NodeUC510.A1: Abis TF Reset with unlocked TRXs
//     */
//    /* @Test (timeOut = ....)
//    @Parameters({ "testId", "description" })
//    public void resetCommandAoTfUnlockedTrx(String testId, String description) throws InterruptedException {
//        
//        setTestCase(testId, description);
//    
//        setTestInfo("Precondition: At least one TRX MO in the GSM Sector MO is unlocked");
//        
//        // At least one TRX MO in the GSM Sector MO is unlocked 
//    }*/

    /**
     * @name resetCommandAoAt
     * 
     * @description Verifies Reset Command EP for AO AT according to NodeUC643.N and UC479.E2
     *              
     * @param testId - unique identifier
     * @param description
     * 
     * @throws InterruptedException
     */
    @Test (timeOut = 360000)
    @Parameters({ "testId", "description" })
    public void resetCommandAoAt(String testId, String description) throws InterruptedException {
        
        setTestCase(testId, description);
        momHelper.waitForAbisScfOmlState("UP");
        
        /**
         * Case 1: Reset Command in AO AT state RESET
         */
        abisHelper.resetCommand(this.moClassAt);
        setTestStepBegin("Abis AT Reset in state RESET");
        setTestInfo("Send Reset Command to AO AT");
        OM_G31R01.ResetComplete result = abisHelper.resetCommand(this.moClassAt);
        
        setTestInfo("Verify that Reset Complete is received from AO AT");
    	assertEquals("Not from AO AT", this.moClassAt, result.getMOClass());
    	assertEquals("AssociatedSOInstance is not 0xFF", 0xFF, result.getAssociatedSOInstance());
    	assertEquals("InstanceNumber is not 0", 0, result.getInstanceNumber());

        setTestInfo("Verify that MO:GsmSector attribute:abisAtState is RESET");
        assertTrue("MO " + GSM_SECTOR_LDN + " abisAtState is not RESET", 
                momHelper.waitForMoAttributeStringValue(GSM_SECTOR_LDN, "abisAtState", "RESET", 6));
        setTestStepEnd();
        
        /**
         * Case 2: Reset Command in AO AT state DISABLE
         */
        setTestStepBegin("Abis AT Reset in state DISABLE");
        setTestInfo("Precondition: Send Start Command to AO AT");
        OM_G31R01.StartResult startResult = abisHelper.startRequest(this.moClassAt, 0);

        setTestInfo("Precondition: Verify that MO:GsmSector attribute:abisAtState is DISABLE");
        assertTrue("MO " + GSM_SECTOR_LDN + " abisAtState is not DISABLE", 
                momHelper.waitForMoAttributeStringValue(GSM_SECTOR_LDN, "abisAtState", "DISABLE", 6));

        setTestInfo("Send Reset Command to AO AT");
        result = abisHelper.resetCommand(this.moClassAt);

        setTestInfo("Verify that Reset Complete is received from AO AT");
    	assertEquals("Not from AO AT", this.moClassAt, result.getMOClass());
    	assertEquals("AssociatedSOInstance is not 0xFF", 0xFF, result.getAssociatedSOInstance());
    	assertEquals("InstanceNumber is not 0", 0, result.getInstanceNumber());
                
        setTestInfo("Verify that MO:GsmSector attribute:abisAtState is RESET");
        assertTrue("MO " + GSM_SECTOR_LDN + " abisAtState is not RESET", 
                momHelper.waitForMoAttributeStringValue(GSM_SECTOR_LDN, "abisAtState", "RESET", 6));
        setTestStepEnd();

        /**
         * Case 3: Reset Command in AO AT state ENABLE
         */
        setTestStepBegin("Abis AT Reset in state ENABLE");
        setTestInfo("Precondition: Send Start Request to AO AT");
        startResult = abisHelper.startRequest(this.moClassAt, 0);
    	assertEquals("Not from AO AT", this.moClassAt, startResult.getMOClass());

        setTestInfo("Precondition: Send Config Request to AO AT");
        OM_G31R01.ATConfigurationResult configResult = abisHelper.atConfigRequest(0x11, 0x06, 0x08, 0x33, 0x2E, 0x1C);
        assertEquals(configResult.getMOClass(), this.moClassAt);

        setTestInfo("Precondition: Send Enable Request to AO AT");
        OM_G31R01.EnableResult enableResult = abisHelper.enableRequest(this.moClassAt, 0);
    	assertEquals("Not from AO AT", this.moClassAt, enableResult.getMOClass());

        setTestInfo("Precondition: Verify that MO:GsmSector attribute:abisAtState is ENABLE");
        assertTrue("MO " + GSM_SECTOR_LDN + " abisAtState is not ENABLE", 
                momHelper.waitForMoAttributeStringValue(GSM_SECTOR_LDN, "abisAtState", "ENABLE", 6));
        
        setTestInfo("Send Reset Command to AO AT");
        result = abisHelper.resetCommand(this.moClassAt);

        setTestInfo("Verify that Reset Complete is received from AO AT");
    	assertEquals("Not from AO AT", this.moClassAt, result.getMOClass());
    	assertEquals("AssociatedSOInstance is not 0xFF", 0xFF, result.getAssociatedSOInstance());
    	assertEquals("InstanceNumber is not 0", 0, result.getInstanceNumber());
                
        setTestInfo("Verify that MO:GsmSector attribute:abisAtMoState is RESET");
        assertTrue("MO " + GSM_SECTOR_LDN + " abisAtState is not RESET", 
                momHelper.waitForMoAttributeStringValue(GSM_SECTOR_LDN, "abisAtState", "RESET", 6));
        
        setTestStepEnd();
        
        //UC479.E2
        abisHelper.resetCommand(this.moClassScf);
        try {
        	abisHelper.resetCommand(this.moClassAt);
        	fail("Reset Reject must be received here");
        } catch (OM_G31R01.ResetRejectException rej) {
        	saveAssertEquals("Error Code must be Wrong State", OM_G31R01.Enums.ResultCode.WrongState, rej.getResetReject().getResultCode());
        }
    }
    
    /**
     * @name resetCommandSoTrxc
     * 
     * @description Verifies Reset Command EP for SO TRXC according to NodeUC653.N.
     *              
     * @param testId - unique identifier
     * @param description
     * 
     * @throws InterruptedException
     */
    @Test (timeOut = 360000)
    @Parameters({ "testId", "description" })
    public void resetCommandSoTrxc(String testId, String description) throws InterruptedException, JSONException {
        
        setTestCase(testId, description);

        //Do the below two times to cover these cases:
        //Case 1 SO TRXC state started (done in setup)
      //Case 2 SO TRXC state RESET
        
        for (int i = 0; i < 2; i++) {
            setTestStepBegin("Send Reset Command to SO TRXC");
            OM_G31R01.ResetComplete result = abisHelper.resetCommand(this.moClassTrxc);
            setTestStepEnd();
        
            setTestStepBegin("Verify that Reset Complete is received from SO TRXC");
        	assertEquals("Not from SO TRXC", this.moClassTrxc, result.getMOClass());
        	assertEquals("AssociatedSOInstance is not 0xFF", 0xFF, result.getAssociatedSOInstance());
        	assertEquals("InstanceNumber is not 0", 0, result.getInstanceNumber());
            setTestStepEnd();
            
            OM_G31R01.StatusResponse trxcStatusRsp = abisHelper.statusRequest(this.moClassTrxc);
            saveAssertEquals("Operational Condition must be Operational for SO TRXC", OperationalCondition.Operational.getValue(), trxcStatusRsp.getStatusChoice().getStatusTRXC().getOperationalCondition().getOperationalCondition().getValue());
            saveAssertEquals("Operational Condition Reason Map must be empty for SO TRXC", null, trxcStatusRsp.getStatusChoice().getStatusTRXC().getOperationalConditionReasonsMap());
            saveAssertEquals("Operational Condition Text must be empty for SO TRXC", null, trxcStatusRsp.getStatusChoice().getStatusTRXC().getOperationalConditionText());

            setTestStepBegin("Verify that MO:Trx attribute:abisTrxcState is RESET");
            assertTrue("abisSoTrxcState is not RESET", momHelper.waitForAbisTrxcState("RESET"));
            setTestStepEnd(); 
            setTestStepBegin("Verify that MO:Trx attribute:abisTxState is RESET");
            assertTrue("abisTxMoState is not RESET", momHelper.waitForAbisTxMoState("RESET"));
            setTestStepEnd();
            setTestStepBegin("Verify that MO:Trx attribute:abisRxState is RESET");
            assertTrue("abisRxMoState is not RESET", momHelper.waitForAbisRxMoState("RESET"));
            setTestStepEnd();
            
        	setTestInfo("Verify that MO:GsmSector attribute:abisTsState is RESET");
        	abisPrePost.checkAbisTsMoState("RESET");
            setTestStepEnd();
            
            // the MO:s under the sector shall not be affected
            setTestStepBegin("Verify that the MO:GsmSector attribute:abisScfState is still STARTED");
            assertTrue("abisScfState is not STARTED", momHelper.waitForAbisScfState("STARTED"));
            setTestStepEnd();
                        
            setTestStepBegin("Verify that the MO:GsmSector attribute:abisTfState is still ENABLED");
            assertTrue("abisTfState is not ENABLED", momHelper.waitForAbisTfMoState("ENABLED"));
            setTestStepEnd();
        }
    }
    
    /**
     * 
     * @name resetCommandAoTx
     * 
     * @description Verifies Reset Command EP for AO TX according to NodeUC599.N
     * 
     * @param testId - unique identifier
     * @param description
     * 
     * @throws InterruptedException
     */
    @Test (timeOut = 360000)
    @Parameters({ "testId", "description" })
    public void resetCommandAoTx(String testId, String description) throws InterruptedException {
       
        setTestCase(testId, description);

        //CASE 1: AO TX in state DISABLED
        setTestStepBegin("Send Reset Command to AO TX");
        saveAssertTrue("abisTxMoState is not DISABLED", momHelper.waitForAbisTxMoState("DISABLED"));
        OM_G31R01.ResetComplete result = abisHelper.resetCommand(this.moClassTx);
        setTestStepEnd();
        
        setTestStepBegin("Verify that Reset Complete is received from AO TX");
    	saveAssertEquals("Not from AO TX", this.moClassTx, result.getMOClass());
    	saveAssertEquals("AssociatedSOInstance is not 0xFF", 0xFF, result.getAssociatedSOInstance());
    	saveAssertEquals("InstanceNumber is not 0", 0, result.getInstanceNumber());
        setTestStepEnd();
        
        setTestStepBegin("Verify that MO:Trx attribute:abisTxMoState is RESET");
        saveAssertTrue("abisTxMoState is not RESET", momHelper.waitForAbisTxMoState("RESET"));
        setTestStepEnd();
        
        //CASE 2: AO TX in state RESET
        setTestStepBegin("Verify that MO:Trx attribute:abisTxMoState is RESET");
        abisHelper.resetCommand(this.moClassTx);
        saveAssertTrue("abisTxMoState is not RESET", momHelper.waitForAbisTxMoState("RESET"));
        setTestStepEnd();
        
        //CASE 3: AO TX in state ENABLED
        setTestStepBegin("Start, configure, enable then reset AO TX");
        OM_G31R01.StartResult startResult = abisHelper.startRequest(this.moClassTx, 0);
        saveAssertEquals("abisTxMoState is not DISABLED", OM_G31R01.Enums.MOState.DISABLED, startResult.getMOState());
        abisHelper.txConfigRequest(0, momHelper.getArfcnToUse(), false, 27, false);
        abisHelper.enableRequest(moClassTx, 0);
        assertTrue("abisTxMoState is not ENABLED", momHelper.waitForAbisTxMoState("ENABLED"));
        abisHelper.resetCommand(this.moClassTx);
        saveAssertTrue("abisTxMoState is not RESET", momHelper.waitForAbisTxMoState("RESET"));
        setTestStepEnd();
    }
    
    /**
     * 
     * @name resetCommandAoRx
     * 
     * @description Verifies Reset Command EP for AO RX according to NodeUC604.N
     * 
     * @param testId - unique identifier
     * @param description
     * 
     * @throws InterruptedException
     */
    @Test (timeOut = 360000)
    @Parameters({ "testId", "description" })
    public void resetCommandAoRx(String testId, String description) throws InterruptedException {
       
        setTestCase(testId, description);

        //CASE 1: AO RX in state DISABLED
        setTestStepBegin("Send Reset Command to AO RX");
        saveAssertTrue("abisRxMoState is not DISABLED", momHelper.waitForAbisRxMoState("DISABLED"));
        OM_G31R01.ResetComplete result = abisHelper.resetCommand(this.moClassRx);
        setTestStepEnd();
        
        setTestStepBegin("Verify that Reset Complete is received from AO RX");
    	saveAssertEquals("Not from AO RX", this.moClassRx, result.getMOClass());
    	saveAssertEquals("AssociatedSOInstance is not 0xFF", 0xFF, result.getAssociatedSOInstance());
    	saveAssertEquals("InstanceNumber is not 0", 0, result.getInstanceNumber());
        setTestStepEnd();
        
        setTestStepBegin("Verify that MO:Trx attribute:abisRxMoState is RESET");
        saveAssertTrue("abisRxMoState is not RESET", momHelper.waitForAbisRxMoState("RESET"));
        setTestStepEnd();
        
        //CASE 2: AO RX in state RESET
        setTestStepBegin("Verify that MO:Trx attribute:abisRxMoState is RESET");
        abisHelper.resetCommand(this.moClassRx);
        saveAssertTrue("abisRxMoState is not RESET", momHelper.waitForAbisRxMoState("RESET"));
        setTestStepEnd();
        
        //CASE 3: AO RX in state ENABLED
        setTestStepBegin("Start, configure, enable then reset AO RX");
        OM_G31R01.StartResult startResult = abisHelper.startRequest(this.moClassRx, 0);
        saveAssertEquals("abisRxMoState is not DISABLED", OM_G31R01.Enums.MOState.DISABLED, startResult.getMOState());
        abisHelper.rxConfigRequest(0, momHelper.getArfcnToUse(), false, "");
        abisHelper.enableRequest(moClassRx, 0);
        assertTrue("abisRxMoState is not ENABLED", momHelper.waitForAbisRxMoState("ENABLED"));
        abisHelper.resetCommand(this.moClassRx);
        saveAssertTrue("abisRxMoState is not RESET", momHelper.waitForAbisRxMoState("RESET"));
        setTestStepEnd();
    }
    
    /**
     * 
     * @name resetCommandAllAoTs
     * 
     * @description Verifies Reset Command EP for AO TS according to NodeUC623.N
     * 
     * @param testId - unique identifier
     * @param description
     * 
     * @throws InterruptedException
     * @throws JSONException 
     */
    @Test (timeOut = 500000)
    @Parameters({ "testId", "description" })
    public void resetCommandAllAoTs(String testId, String description) throws InterruptedException, JSONException {

    	int associatedSoInstance = 0;

    	setTestCase(testId, description);

    	for (int tsInstance = 0; tsInstance < 8; tsInstance++)
    	{ 
            /**
             * Case 1: Reset Command in AO TS state RESET
             */
       		setTestStepBegin("Send Reset Command to AO TS instance " + tsInstance);
    		OM_G31R01.ResetComplete resetResult = abisHelper.resetCommand(this.moClassTs, tsInstance, associatedSoInstance);
    		setTestStepEnd();

    		setTestStepBegin("Verify that Reset Complete is received from AO TS instance " + tsInstance);
        	assertEquals("Not from AO TS", this.moClassTs, resetResult.getMOClass());
        	assertEquals("AssociatedSOInstance is not " + associatedSoInstance, associatedSoInstance, resetResult.getAssociatedSOInstance());
        	assertEquals("InstanceNumber is not " + tsInstance, tsInstance, resetResult.getInstanceNumber());
    		setTestStepEnd();

    		setTestStepBegin("Verify that MO:Trx attribute:abisTsMoState for instance "+ tsInstance + " is RESET");
    		assertTrue("abisTsMoState for tsInstance " + tsInstance + " is not RESET", momHelper.waitForAbisTsMoState(tsInstance, "RESET", 5));
    		setTestStepEnd();
    		
            /**
             * Case 2: Reset Command in AO TS state DISABLED
             */
    		setTestStepBegin("Send Start Command to AO TS instance " + tsInstance);
    		abisHelper.startRequest(this.moClassTs, tsInstance, associatedSoInstance);
    		assertTrue("abisTsMoState for tsInstance " + tsInstance + " is not DISABLED", momHelper.waitForAbisTsMoState(tsInstance, "DISABLED", 5));
    		setTestStepEnd();

    		setTestStepBegin("Send Reset Command to AO TS instance " + tsInstance);
    		resetResult = abisHelper.resetCommand(this.moClassTs, tsInstance, associatedSoInstance);
    		setTestStepEnd();

    		setTestStepBegin("Verify that Reset Complete is received from AO TS instance " + tsInstance);
    		assertEquals("Not from AO TS", this.moClassTs, resetResult.getMOClass());
    		assertEquals("AssociatedSOInstance is not " + associatedSoInstance, associatedSoInstance, resetResult.getAssociatedSOInstance());
    		assertEquals("InstanceNumber is not " + tsInstance, tsInstance, resetResult.getInstanceNumber());
    		setTestStepEnd();
    		  		 
    		setTestStepBegin("Verify that MO:Trx attribute:abisTsMoState for instance "+ tsInstance + " is RESET");
    		assertTrue("abisTsMoState for tsInstance " + tsInstance + " is not RESET", momHelper.waitForAbisTsMoState(tsInstance, "RESET", 5));
    		setTestStepEnd();
    	}
    }
    
    /**
     * 
     * @name resetCommandAllAoTs in Enabled state
     * 
     * @description Verifies Reset Command EP for AO TS according to NodeUC623.N
     * 
     * @param testId - unique identifier
     * @param description
     * 
     * @throws InterruptedException
     * @throws JSONException 
     */
    @Test (timeOut = 500000)
    @Parameters({ "testId", "description" })
    public void resetCommandAllAoTs_InEnabledState(String testId, String description) throws InterruptedException, JSONException {
    
       	int associatedSoInstance = 0;

    	setTestCase(testId, description);
    	
    	// Prepare: Enable all TS instances
    	setTestStepBegin("Prepare: Enable all TS instances");
    	for (int tsInstance = 0; tsInstance < 8; tsInstance++)
    	{
    		setSubTestStep("Send Start Command to AO TS instance " + tsInstance);
    		abisHelper.startRequest(this.moClassTs, tsInstance, associatedSoInstance);
    		assertTrue("abisTsMoState for tsInstance " + tsInstance + " is not DISABLED", momHelper.waitForAbisTsMoState(tsInstance, "DISABLED", 5));
    		
    		setSubTestStep("Send TS Configuration Request, instance: " + tsInstance);
    		abisHelper.tsConfigRequest(tsInstance, tsInstance+1, tsInstance+1, true, OM_G31R01.Enums.Combination.TCH);

    		setSubTestStep("Send TS Enable Request, instance: " + tsInstance);
    		OM_G31R01.EnableResult enableResult = abisHelper.enableRequest(this.moClassTs, tsInstance, associatedSoInstance);
    		assertEquals("MO state is not what expected!", OM_G31R01.Enums.MOState.ENABLED, enableResult.getMOState());
    		sleepSeconds(1);
    		assertTrue("AO TS  for tsInstance: " + tsInstance + " is not ENABLED", momHelper.waitForAbisTsMoState(tsInstance, "ENABLED", 5));
    	}
    	setTestStepEnd();
   	
    	// Reset all TS instances
    	for (int tsInstance = 0; tsInstance < 8; tsInstance++)
    	{
    		setTestStepBegin("Send Reset Command to AO TS, instance: " + tsInstance);
    		OM_G31R01.ResetComplete resetResult = abisHelper.resetCommand(this.moClassTs, tsInstance, associatedSoInstance);
    		setTestStepEnd();

    		setTestStepBegin("Verify that Reset Complete is received from AO TS instance " + tsInstance);
    		assertEquals("Not from AO TS", this.moClassTs, resetResult.getMOClass());
    		assertEquals("AssociatedSOInstance is not " + associatedSoInstance, associatedSoInstance, resetResult.getAssociatedSOInstance());
    		assertEquals("InstanceNumber is not " + tsInstance, tsInstance, resetResult.getInstanceNumber());
    		setTestStepEnd();
    		
    	    OM_G31R01.StatusResponse tsStatusRsp = abisHelper.statusRequest(this.moClassTs, tsInstance, associatedSoInstance);
    	    assertEquals("TS configuration signature must be 0", 0, tsStatusRsp.getStatusChoice().getStatusAO().getConfigurationSignature());

    		setTestStepBegin("Verify that MO:Trx attribute:abisTsMoState for instance "+ tsInstance + " is RESET");
    		assertTrue("abisTsMoState for tsInstance " + tsInstance + " is not RESET", momHelper.waitForAbisTsMoState(tsInstance, "RESET", 5));
    		setTestStepEnd();
    	}
    	
        setTestStepBegin("Channel Activation should be rejected since all channels on the TS are deactivated");
        channelActivationNormalAssignment (1);
        setTestStepEnd();
    }
   
    /**
     * @name resetCommandSoTrxcWithOtherThanOperationalCondition
     * 
     * @description Verifies Reset Command EP for SO TRXC according to NodeUC653.N.
     *              
     * @param testId - unique identifier
     * @param description
     * 
     * @throws InterruptedException
     */
    @Test (timeOut = 360000)
    @Parameters({ "testId", "description" })
    public void resetCommandSoTrxcWithOtherThanOperationalCondition(String testId, String description) throws InterruptedException, JSONException {

    	setTestCase(testId, description);
    	
    	// Unlock SectorEquipmentFunction
        setTestStepBegin("Test Unlock SectorEquipmentFunction MO: " + momHelper.getSectorEquipmentFunctionLdn());
        momHelper.setAttributeForMoAndCommit(momHelper.getSectorEquipmentFunctionLdn(), "administrativeState", "UNLOCKED");     
        setTestStepEnd();


    	//Trxc is Operational
    	OM_G31R01.StatusResponse trxcStatusRsp = abisHelper.statusRequest(this.moClassTrxc);
    	saveAssertEquals("Operational Condition must be Operational for SO TRXC", OperationalCondition.Operational.getValue(), trxcStatusRsp.getStatusChoice().getStatusTRXC().getOperationalCondition().getOperationalCondition().getValue());
    	saveAssertEquals("Operational Condition Reason Map must be empty for SO TRXC", null, trxcStatusRsp.getStatusChoice().getStatusTRXC().getOperationalConditionReasonsMap());
    	saveAssertEquals("Operational Condition Text must be empty for SO TRXC", null, trxcStatusRsp.getStatusChoice().getStatusTRXC().getOperationalConditionText());
    	
    	// Lock SectorEquipmentFunction
    	setTestStepBegin("Test Lock SectorEquipmentFunction MO: " + momHelper.getSectorEquipmentFunctionLdn());
    	momHelper.setAttributeForMoAndCommit(momHelper.getSectorEquipmentFunctionLdn(), "administrativeState", "LOCKED");      
    	setTestStepEnd();

    	//Trxc is Not Operational
    	trxcStatusRsp = abisHelper.statusRequest(this.moClassTrxc);
    	saveAssertEquals("Operational Condition must be Not Operational for SO TRXC", OperationalCondition.NotOperational.getValue(), trxcStatusRsp.getStatusChoice().getStatusTRXC().getOperationalCondition().getOperationalCondition().getValue());
    	
    	//Reset Trxc
    	setTestStepBegin("Send Reset Command to SO TRXC");
    	OM_G31R01.ResetComplete result = abisHelper.resetCommand(this.moClassTrxc);
    	setTestStepEnd();
    	
    	setTestStepBegin("Verify that Reset Complete is received from SO TRXC");
    	assertEquals("Not from SO TRXC", this.moClassTrxc, result.getMOClass());
    	assertEquals("AssociatedSOInstance is not 0xFF", 0xFF, result.getAssociatedSOInstance());
    	assertEquals("InstanceNumber is not 0", 0, result.getInstanceNumber());
    	setTestStepEnd();


    	setTestStepBegin("Verify that MO:Trx attribute:abisTrxcState is RESET");
    	assertTrue("abisSoTrxcState is not RESET", momHelper.waitForAbisTrxcState("RESET"));
    	setTestStepEnd(); 
    	setTestStepBegin("Verify that MO:Trx attribute:abisTxState is RESET");
    	assertTrue("abisTxMoState is not RESET", momHelper.waitForAbisTxMoState("RESET"));
    	setTestStepEnd();
    	setTestStepBegin("Verify that MO:Trx attribute:abisRxState is RESET");
    	assertTrue("abisRxMoState is not RESET", momHelper.waitForAbisRxMoState("RESET"));
    	setTestStepEnd();

    	setTestInfo("Verify that MO:GsmSector attribute:abisTsState is RESET");
    	abisPrePost.checkAbisTsMoState("RESET");
    	setTestStepEnd();

    	// the MO:s under the sector shall not be affected
    	setTestStepBegin("Verify that the MO:GsmSector attribute:abisScfState is still STARTED");
    	assertTrue("abisScfState is not STARTED", momHelper.waitForAbisScfState("STARTED"));
    	setTestStepEnd();

    	setTestStepBegin("Verify that the MO:GsmSector attribute:abisTfState is still ENABLED");
    	assertTrue("abisTfState is not ENABLED", momHelper.waitForAbisTfMoState("ENABLED"));
    	setTestStepEnd();
    	
    	//Trxc is Not Operational
    	trxcStatusRsp = abisHelper.statusRequest(this.moClassTrxc);
    	saveAssertEquals("Operational Condition must be Not Operational for SO TRXC", OperationalCondition.NotOperational.getValue(), trxcStatusRsp.getStatusChoice().getStatusTRXC().getOperationalCondition().getOperationalCondition().getValue());
    	
    	// Unlock SectorEquipmentFunction
        setTestStepBegin("Test Unlock SectorEquipmentFunction MO: " + momHelper.getSectorEquipmentFunctionLdn());
        momHelper.setAttributeForMoAndCommit(momHelper.getSectorEquipmentFunctionLdn(), "administrativeState", "UNLOCKED");
        setTestStepEnd();

    }
    
    /**
     * 
     * @name resetCommandAoRxWithOtherThanOperationalCondition
     * 
     * @description Verifies Reset Command EP for AO RX according to NodeUC604.N
     * 
     * @param testId - unique identifier
     * @param description
     * 
     * @throws InterruptedException
     */
    @Test (timeOut = 360000)
    @Parameters({ "testId", "description" })
    public void resetCommandAoRxWithOtherThanOperationalCondition(String testId, String description) throws InterruptedException {
       
        setTestCase(testId, description);

     // Unlock SectorEquipmentFunction
        setTestStepBegin("Test Unlock SectorEquipmentFunction MO: " + momHelper.getSectorEquipmentFunctionLdn());
        momHelper.setAttributeForMoAndCommit(momHelper.getSectorEquipmentFunctionLdn(), "administrativeState", "UNLOCKED");
        setTestStepEnd();

       
        //RX is Operational
    	OM_G31R01.StatusResponse rxStatusRsp = abisHelper.statusRequest(this.moClassRx);
    	saveAssertEquals("Operational Condition must be Operational for RX", OperationalCondition.Operational.getValue(), rxStatusRsp.getStatusChoice().getStatusAO().getOperationalCondition().getOperationalCondition().getValue());
    	
    	// Lock SectorEquipmentFunction
    	setTestStepBegin("Test Lock SectorEquipmentFunction MO: " + momHelper.getSectorEquipmentFunctionLdn());
    	momHelper.setAttributeForMoAndCommit(momHelper.getSectorEquipmentFunctionLdn(), "administrativeState", "LOCKED");
    	setTestStepEnd();
    	
    	//RX is Not Operational
    	rxStatusRsp = abisHelper.statusRequest(this.moClassRx);
    	saveAssertEquals("Operational Condition must be Not Operational for RX", OperationalCondition.NotOperational.getValue(), rxStatusRsp.getStatusChoice().getStatusAO().getOperationalCondition().getOperationalCondition().getValue());
    	
    	//CASE 1: AO RX in state DISABLED
        setTestStepBegin("Send Reset Command to AO RX");
        saveAssertTrue("abisRxMoState is not DISABLED", momHelper.waitForAbisRxMoState("DISABLED"));//
        OM_G31R01.ResetComplete result = abisHelper.resetCommand(this.moClassRx);
        setTestStepEnd();
    	
        setTestStepBegin("Verify that Reset Complete is received from AO RX");
    	saveAssertEquals("Not from AO RX", this.moClassRx, result.getMOClass());
    	saveAssertEquals("AssociatedSOInstance is not 0xFF", 0xFF, result.getAssociatedSOInstance());
    	saveAssertEquals("InstanceNumber is not 0", 0, result.getInstanceNumber());
        setTestStepEnd();
          
        setTestStepBegin("Verify that MO:Trx attribute:abisRxMoState is RESET");
        saveAssertTrue("abisRxMoState is not RESET", momHelper.waitForAbisRxMoState("RESET"));
        setTestStepEnd();  
        
      //RX is Not Operational
    	rxStatusRsp = abisHelper.statusRequest(this.moClassRx);
    	saveAssertEquals("Operational Condition must be Not Operational for RX", OperationalCondition.NotOperational.getValue(), rxStatusRsp.getStatusChoice().getStatusAO().getOperationalCondition().getOperationalCondition().getValue());
    	
    	// Unlock SectorEquipmentFunction
        setTestStepBegin("Test Unlock SectorEquipmentFunction MO: " + momHelper.getSectorEquipmentFunctionLdn());
        momHelper.setAttributeForMoAndCommit(momHelper.getSectorEquipmentFunctionLdn(), "administrativeState", "UNLOCKED");     
        setTestStepEnd();
    }

    /**
     * 
     * @name resetCommandAoTxWithOtherThanOperationalCondition
     * 
     * @description Verifies Reset Command EP for AO TX according to NodeUC599.N
     * 
     * @param testId - unique identifier
     * @param description
     * 
     * @throws InterruptedException
     */
    @Test (timeOut = 360000)
    @Parameters({ "testId", "description" })
    public void resetCommandAoTxWithOtherThanOperationalCondition(String testId, String description) throws InterruptedException {

    	setTestCase(testId, description);

    	// Unlock SectorEquipmentFunction
        setTestStepBegin("Test Unlock SectorEquipmentFunction MO: " + momHelper.getSectorEquipmentFunctionLdn());
        momHelper.setAttributeForMoAndCommit(momHelper.getSectorEquipmentFunctionLdn(), "administrativeState", "UNLOCKED");     
        setTestStepEnd();


    	//TX is Operational
    	OM_G31R01.StatusResponse txStatusRsp = abisHelper.statusRequest(this.moClassTx);
    	saveAssertEquals("Operational Condition must be Operational for TX", OperationalCondition.Operational.getValue(), txStatusRsp.getStatusChoice().getStatusAO().getOperationalCondition().getOperationalCondition().getValue());

    	// Lock SectorEquipmentFunction
    	setTestStepBegin("Test Lock SectorEquipmentFunction MO: " + momHelper.getSectorEquipmentFunctionLdn());
    	momHelper.setAttributeForMoAndCommit(momHelper.getSectorEquipmentFunctionLdn(), "administrativeState", "LOCKED");      
    	setTestStepEnd();

    	//TX is Not Operational
    	txStatusRsp = abisHelper.statusRequest(this.moClassTx);
    	saveAssertEquals("Operational Condition must be Not Operational for TX", OperationalCondition.NotOperational.getValue(), txStatusRsp.getStatusChoice().getStatusAO().getOperationalCondition().getOperationalCondition().getValue());

    	//CASE 1: AO RX in state DISABLED
    	setTestStepBegin("Send Reset Command to AO TX");
    	saveAssertTrue("abisTxMoState is not DISABLED", momHelper.waitForAbisTxMoState("DISABLED"));//
    	OM_G31R01.ResetComplete result = abisHelper.resetCommand(this.moClassTx);
    	setTestStepEnd();

    	setTestStepBegin("Verify that Reset Complete is received from AO TX");
    	saveAssertEquals("Not from AO TX", this.moClassTx, result.getMOClass());
    	saveAssertEquals("AssociatedSOInstance is not 0xFF", 0xFF, result.getAssociatedSOInstance());
    	saveAssertEquals("InstanceNumber is not 0", 0, result.getInstanceNumber());
    	setTestStepEnd();

    	setTestStepBegin("Verify that MO:Trx attribute:abisTxMoState is RESET");
    	saveAssertTrue("abisTxMoState is not RESET", momHelper.waitForAbisTxMoState("RESET"));
    	setTestStepEnd();

    	//TX is Not Operational
    	txStatusRsp = abisHelper.statusRequest(this.moClassTx);
    	saveAssertEquals("Operational Condition must be Not Operational for TX", OperationalCondition.NotOperational.getValue(), txStatusRsp.getStatusChoice().getStatusAO().getOperationalCondition().getOperationalCondition().getValue());
    	
    	// Unlock SectorEquipmentFunction
        setTestStepBegin("Test Unlock SectorEquipmentFunction MO: " + momHelper.getSectorEquipmentFunctionLdn());
        momHelper.setAttributeForMoAndCommit(momHelper.getSectorEquipmentFunctionLdn(), "administrativeState", "UNLOCKED");     
        setTestStepEnd();
    }
}
