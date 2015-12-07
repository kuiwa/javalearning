package com.ericsson.msran.test.grat.startrequest;

import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import se.ericsson.jcat.fw.annotations.Setup;
import se.ericsson.jcat.fw.annotations.Teardown;

import com.ericsson.abisco.clientlib.servers.OM_G31R01;
import com.ericsson.abisco.clientlib.servers.BG.Enums.OperationalCondition;
import com.ericsson.abisco.clientlib.servers.OM_G31R01.StartRequestRejectException;

import com.ericsson.msran.g2.annotations.TestInfo;
import com.ericsson.msran.test.grat.testhelpers.AbisHelper;
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
 * @revision esicwan 2015-07-30 NodeUC652.N: Start SO TRXC when TRXC Operational Condition != Operational.             
 */

public class StartRequestTrxMoTest extends TestBase {
    private final OM_G31R01.Enums.MOClass moClassTrxc = OM_G31R01.Enums.MOClass.TRXC;
    private final OM_G31R01.Enums.MOClass moClassTx = OM_G31R01.Enums.MOClass.TX;
    private final OM_G31R01.Enums.MOClass moClassRx = OM_G31R01.Enums.MOClass.RX;
    private final OM_G31R01.Enums.MOClass moClassTs = OM_G31R01.Enums.MOClass.TS;
    
    private AbisPrePost abisPrePost;

    private AbisHelper abisHelper;
    private MomHelper momHelper;
    
    private NodeStatusHelper nodeStatus;

    /**
     * Description of test case for test reporting
     */
    @TestInfo(
            tcId = "NodeUC598, NodeUC603, NodeUC624, NodeUC652",
            slogan = "Abis SO TRXC Start, Abis TX Start, Abis RX Start, Abis TS Start",
            requirementDocument = "1/00651-FCP 130 1402",
            requirementRevision = "PC5",
            requirementLinkTested = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff87c1d941?docno=1/00651-FCP1301402Uen&format=excel8book",
            requirementLinkLatest = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff86a2af3d?docno=1/00651-FCP1301402Uen&action=current&format=excel8book",
            requirementIds = { "10565-0334/21767[PA1][PREL]", "10565-0334/19034[A][APPR]",
                    "10565-0334/19417[A][APPR]" },
            verificationStatement = "Verifies NodeUC598.N, NodeUC598.E1, NodeUC603.N, NodeUC603.E1, NodeUC624.N, NodeUC624.E1, NodeUC652.N, NodeUC652.E1",
            testDescription = "Verifies Abis SO SCF Start, TF Start and AT Start.",
            traceGuidelines = "N/A")


    /**
     * Create MO:s, establish links to Abisco, and verify preconditions.
     * @throws InterruptedException 
     */
    @Setup
    public void setup() throws InterruptedException {
    	setTestStepBegin("Setup");
        nodeStatus = new NodeStatusHelper();
        assertTrue("Node did not reach a working state before test start", nodeStatus.waitForNodeReady());
        
        abisHelper = new AbisHelper();
        abisPrePost = new AbisPrePost();
        momHelper = new MomHelper();
        abisPrePost.preCondScfMoStateStartedAtTfActive();
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
  * @name startRequestSoTrxcWithSoScfStarted
  * 
  * @description Verifies the Start Request SP for SO TRXC according to NodeUC652.N
  *
  * @param testId - unique identifier
  * @param description
  * 
  * @throws InterruptedException
  */
 @Test (timeOut = 360000)
 @Parameters({ "testId", "description" })
 public void startRequestSoTrxcWithSoScfStarted(String testId, String description) throws InterruptedException {
     
     setTestCase(testId, description);
     
     setTestStepBegin("Precondition: Start So TRXC");
     abisPrePost.startSoTrxc();
     setTestStepEnd();

     //Case 1 SO TRXC in state STARTED 
     setTestStepBegin("Send Start Request command to SO TRXC");
     OM_G31R01.StartResult startResult = abisHelper.startRequest(this.moClassTrxc, 0);
     setTestStepEnd();
     
     setTestStepBegin("Verify that MO State in Start Result is STARTED");
     assertEquals("MO state is not what expected!", OM_G31R01.Enums.MOState.STARTED, startResult.getMOState());
     setTestStepEnd();
     
     setTestStepBegin("Verify that MO:Trx attribute:abisTrxcState is STARTED");
     assertTrue("Trx abisTrxcState is not STARTED", momHelper.waitForAbisTrxcState("STARTED"));
     
     //Case 2 SO TRXC in state RESET
     
     setTestStepBegin("Reset SO TRXC");
     abisHelper.resetCommand(this.moClassTrxc);
     assertTrue("Trx abisTrxcState is not RESET", momHelper.waitForAbisTrxcState("RESET"));
     setTestStepEnd();
     
     setTestStepBegin("Send Start Request command to SO TRXC");
     startResult = abisHelper.startRequest(this.moClassTrxc, 0);
     setTestStepEnd();
     
     setTestStepBegin("Verify that MO State in Start Result is STARTED");
     assertEquals("MO state is not what expected!", OM_G31R01.Enums.MOState.STARTED, startResult.getMOState());
     setTestStepEnd();
     
     setTestStepBegin("Verify that MO:Trx attribute:abisTrxcState is STARTED");
     assertTrue("Trx abisTrxcState is not STARTED", momHelper.waitForAbisTrxcState("STARTED"));
     
     // Case 3 Start SO TRXC when TRXC Operational Condition != Operational
     
     setTestStepBegin("Lock SectorEquipmentFunction MO: " + momHelper.getSectorEquipmentFunctionLdn());
     momHelper.setAttributeForMoAndCommit(momHelper.getSectorEquipmentFunctionLdn(), "administrativeState", "LOCKED");      
     setTestStepEnd();
	
     setTestStepBegin("Verify that TX is Not Operational");
     OM_G31R01.StatusResponse txStatusRsp = abisHelper.statusRequest(this.moClassTx);
     saveAssertEquals("Operational Condition must be Not Operational for AO TX", OperationalCondition.NotOperational.getValue(), txStatusRsp.getStatusChoice().getStatusAO().getOperationalCondition().getOperationalCondition().getValue());
     setTestStepEnd();
     
     setTestStepBegin("Verify that RX is Not Operational");
     OM_G31R01.StatusResponse rxStatusRsp = abisHelper.statusRequest(this.moClassRx);
     saveAssertEquals("Operational Condition must be Not Operational for AO RX", OperationalCondition.NotOperational.getValue(), rxStatusRsp.getStatusChoice().getStatusAO().getOperationalCondition().getOperationalCondition().getValue());
     setTestStepEnd();
     
     setTestStepBegin("Verify that TRXC is Not Operational");
     OM_G31R01.StatusResponse trxcStatusRsp = abisHelper.statusRequest(this.moClassTrxc);
     saveAssertEquals("Operational Condition must be Not Operational for SO TRXC", OperationalCondition.NotOperational.getValue(), trxcStatusRsp.getStatusChoice().getStatusTRXC().getOperationalCondition().getOperationalCondition().getValue());
     setTestStepEnd();
	 
     setTestStepBegin("Send Start Request command to SO TRXC");
     startResult = abisHelper.startRequest(this.moClassTrxc, 0);
     setTestStepEnd();
     
     setTestStepBegin("Verify that MO State in Start Result is STARTED");
     assertEquals("MO state is not what expected!", OM_G31R01.Enums.MOState.STARTED, startResult.getMOState());
     setTestStepEnd();
     
     setTestStepBegin("Verify that MO:Trx attribute:abisTrxcState is STARTED");
     assertTrue("Trx abisTrxcState is not STARTED", momHelper.waitForAbisTrxcState("STARTED"));
     setTestStepEnd();
     
     setTestStepBegin("Verify that TX is Not Operational");
     txStatusRsp = abisHelper.statusRequest(this.moClassTx);
     saveAssertEquals("Operational Condition must be Not Operational for AO TX", OperationalCondition.NotOperational.getValue(), txStatusRsp.getStatusChoice().getStatusAO().getOperationalCondition().getOperationalCondition().getValue());
     setTestStepEnd();
     
     setTestStepBegin("Verify that RX is Not Operational");
     rxStatusRsp = abisHelper.statusRequest(this.moClassRx);
     saveAssertEquals("Operational Condition must be Not Operational for AO RX", OperationalCondition.NotOperational.getValue(), rxStatusRsp.getStatusChoice().getStatusAO().getOperationalCondition().getOperationalCondition().getValue());
     setTestStepEnd();
         
     setTestStepBegin("Verify that Trxc is Not Operational");
     trxcStatusRsp = abisHelper.statusRequest(this.moClassTrxc);
     saveAssertEquals("Operational Condition must be Not Operational for SO TRXC", OperationalCondition.NotOperational.getValue(), trxcStatusRsp.getStatusChoice().getStatusTRXC().getOperationalCondition().getOperationalCondition().getValue());
     setTestStepEnd();
 	
     setTestStepBegin("Unlock SectorEquipmentFunction MO: " + momHelper.getSectorEquipmentFunctionLdn());
     momHelper.setAttributeForMoAndCommit(momHelper.getSectorEquipmentFunctionLdn(), "administrativeState", "UNLOCKED");
 }
    
    /**
     * @name startRequestAoTx
     * 
     * @description Verifies the Start Request SP for AO TX according to NodeUC598.N,
     *
     * @param testId - unique identifier
     * @param description  
     *  
     * @throws InterruptedException
     * @throws Exception 
     */
    @Test (timeOut = 60000)
    @Parameters({ "testId", "description" })
    public void startRequestAoTx(String testId, String description) throws InterruptedException, Exception  {
        
        setTestCase(testId, description);

        setTestStepBegin("Precondition: Start So TRXC");
        abisPrePost.startSoTrxc();
        setTestStepEnd();
        
        //Case 1 Ao Tx in state reset
        setTestStepBegin("Precondition: Ao Tx in state reset");
        abisHelper.resetCommand(this.moClassTx);
        setTestInfo("Precondition: MO:Trx attribute:abisTxState is RESET");
        assertTrue("Trx abisTxState is not RESET", momHelper.waitForAbisTxMoState("RESET"));
        setTestStepEnd();
        
        setTestStepBegin("Send Start Request command to AO TX");
        OM_G31R01.StartResult startResult = abisHelper.startRequest(this.moClassTx, 0);
        setTestStepEnd();
        
        setTestStepBegin("Verify that MO State in Start Result is DISABLED");
        assertEquals("MO state is not what expected!", OM_G31R01.Enums.MOState.DISABLED, startResult.getMOState());
        setTestStepEnd();
        
        setTestStepBegin("Verify that MO:Trx attribute:abisTxMoState is DISABLED");
        assertTrue("Trx abisTxState is not DISABLED", momHelper.waitForAbisTxMoState("DISABLED"));
        setTestStepEnd();
        
        //Case 2 Ao Tx in state DISABLED
        startResult = abisHelper.startRequest(this.moClassTx, 0);
        
        setTestStepBegin("Verify that MO State in Start Result is DISABLED");
        assertEquals("MO state is not what expected!", OM_G31R01.Enums.MOState.DISABLED, startResult.getMOState());
        
        setTestStepBegin("Verify that MO:Trx attribute:abisTxMoState is DISABLED");
        assertTrue("Trx abisTxState is not DISABLED", momHelper.waitForAbisTxMoState("DISABLED"));
    }
     
    /**
     * @name startRequestAoTxInStateEnabled
     * 
     * @description Verifies the Start Request SP for AO TX according to NodeUC598.E1,
     *
     * @param testId - unique identifier
     * @param description  
     *  
     * @throws InterruptedException
     * @throws Exception 
     */
    @Test (timeOut = 360000)
    @Parameters({ "testId", "description" })
    public void startRequestAoTxInStateEnabled(String testId, String description) throws InterruptedException, Exception  {
        
        setTestCase(testId, description);

        setTestStepBegin("Precondition: Start So TRXC");
        abisPrePost.startSoTrxc();
        setTestStepEnd();

        setTestStepBegin("Send intial start to put Ao Tx in state Disabled");
        OM_G31R01.StartResult startResult = abisHelper.startRequest(this.moClassTx, 0);

        assertEquals("MO state is not what expected!", OM_G31R01.Enums.MOState.DISABLED, startResult.getMOState());
        assertTrue("Trx abisTxState is not DISABLED", momHelper.waitForAbisTxMoState("DISABLED"));
        setTestStepEnd();

        setTestStepBegin("Enable Ao Tx");
        OM_G31R01.TXConfigurationResult confRes = abisHelper.txConfigRequest(0, momHelper.getArfcnToUse(), false, 27, false);
        assertEquals("AccordanceIndication is not what expected!", OM_G31R01.Enums.AccordanceIndication.AccordingToRequest, 
        		confRes.getAccordanceIndication().getAccordanceIndication());
        OM_G31R01.EnableResult result = abisHelper.enableRequest(this.moClassTx, 0);
        saveAssertEquals("AO TX must be ENABLED", OM_G31R01.Enums.MOState.ENABLED, result.getMOState());
        saveAssertTrue("Trx abisTxState is not ENABLED", momHelper.waitForAbisTxMoState("ENABLED"));
        setTestStepEnd();
        
        setTestStepBegin("Send start request in state Enabled");
        
        try {
            abisHelper.startRequest(this.moClassTx, 0);
        } catch (StartRequestRejectException e) {
            // TODO Auto-generated catch block
            OM_G31R01.StartRequestReject rejMsg = e.getStartRequestReject();
          
          setTestStepBegin("Verify that Start Request Reject result code is 'Wrong state'");
          assertEquals("ResultCode is not what expected!", OM_G31R01.Enums.ResultCode.WrongState, rejMsg.getResultCode());
          setTestStepEnd();
          setTestStepBegin("Verify that MO:Trx attribute:abisTxMoState is ENABLED");
          assertTrue("Trx abisTxState is not ENABLED", momHelper.waitForAbisTxMoState("ENABLED"));
          return;
        }
        
        fail("Expected start request reject");
    }
    
    /**
     * @name startRequestAoRx
     * 
     * @description Verifies the Start Request SP for AO RX according to NodeUC603.N,
     *
     * @param testId - unique identifier
     * @param description  
     *  
     * @throws InterruptedException
     * @throws Exception 
     */
    @Test (timeOut = 360000)
    @Parameters({ "testId", "description" })
    public void startRequestAoRx(String testId, String description) throws InterruptedException, Exception  {
        
        setTestCase(testId, description);

        setTestStepBegin("Precondition: Start So TRXC");
        abisPrePost.startSoTrxc();
        setTestStepEnd();
        

        //Case 1 Ao Rx in state reset
        setTestStepBegin("Precondition: Ao Rx in state reset");
        abisHelper.resetCommand(this.moClassRx);
        setTestInfo("Precondition: MO:Trx attribute:abisRxState is RESET");
        assertTrue("Trx abisRxState is not RESET", momHelper.waitForAbisRxMoState("RESET"));
        setTestStepEnd();
        
        setTestStepBegin("Send Start Request command to AO RX");
        OM_G31R01.StartResult startResult = abisHelper.startRequest(this.moClassRx, 0);
        setTestStepEnd();
        
        setTestStepBegin("Verify that MO State in Start Result is DISABLED");
        saveAssertEquals("MO state is not what expected!", OM_G31R01.Enums.MOState.DISABLED, startResult.getMOState());
        setTestStepEnd();
        
        setTestStepBegin("Verify that MO:Trx attribute:abisRxMoState is DISABLED");
        assertTrue("Trx abisRxState is not DISABLED", momHelper.waitForAbisRxMoState("DISABLED"));
        setTestStepEnd();
        
        //Case 2 Ao Rx in state DISABLED
        startResult = abisHelper.startRequest(this.moClassRx, 0);
        
        setTestStepBegin("Verify that MO State in Start Result is DISABLED");
        assertEquals("MO state is not what expected!", OM_G31R01.Enums.MOState.DISABLED, startResult.getMOState());
        setTestStepEnd();
        
        setTestStepBegin("Verify that MO:Trx attribute:abisRxMoState is DISABLED");
        assertTrue("Trx abisRxState is not DISABLED", momHelper.waitForAbisRxMoState("DISABLED"));
        setTestStepEnd();
    }
     
    /**
     * @name startRequestAoRxInStateEnabled
     * 
     * @description Verifies the Start Request SP for AO RX according to NodeUC603.E1,
     *
     * @param testId - unique identifier
     * @param description  
     *  
     * @throws InterruptedException
     * @throws Exception 
     */
    @Test (timeOut = 360000)
    @Parameters({ "testId", "description" })
    public void startRequestAoRxInStateEnabled(String testId, String description) throws InterruptedException, Exception  {
        
        setTestCase(testId, description);

        setTestStepBegin("Precondition: Start So TRXC");
        abisPrePost.startSoTrxc();
        setTestStepEnd();

        setTestStepBegin("Send intial start to put Ao Rx in state Disabled");
        OM_G31R01.StartResult startResult = abisHelper.startRequest(this.moClassRx, 0);

        saveAssertEquals("MO state is not what expected!", OM_G31R01.Enums.MOState.DISABLED, startResult.getMOState());
        assertTrue("Trx abisRxState is not DISABLED", momHelper.waitForAbisRxMoState("DISABLED"));
        setTestStepEnd();
    	
        setTestStepBegin("Enable Ao Rx");
        OM_G31R01.RXConfigurationResult confRes = abisHelper.rxConfigRequest(0, momHelper.getArfcnToUse(), false, "");
        assertEquals("AccordanceIndication is not what expected!", OM_G31R01.Enums.AccordanceIndication.AccordingToRequest, 
        		confRes.getAccordanceIndication().getAccordanceIndication());
        OM_G31R01.EnableResult result = abisHelper.enableRequest(this.moClassRx, 0);
        saveAssertEquals("AO RX must be ENABLED", OM_G31R01.Enums.MOState.ENABLED, result.getMOState());
        saveAssertTrue("Trx abisRxState is not ENABLED", momHelper.waitForAbisRxMoState("ENABLED"));
        setTestStepEnd();
        
        setTestStepBegin("Send start request in state Enabled");
        
        try {
            abisHelper.startRequest(this.moClassRx, 0);
        } catch (StartRequestRejectException e) {
            // TODO Auto-generated catch block
            OM_G31R01.StartRequestReject rejMsg = e.getStartRequestReject();
          
          setTestStepBegin("Verify that Start Request Reject result code is 'Wrong state'");
          assertEquals("ResultCode is not what expected!", OM_G31R01.Enums.ResultCode.WrongState, rejMsg.getResultCode());
          setTestStepEnd();
          setTestStepBegin("Verify that MO:Trx attribute:abisRxMoState is ENABLED");
          assertTrue("Trx abisRxState is not ENABLED", momHelper.waitForAbisRxMoState("ENABLED"));
          return;
        }
        
        fail("Expected start request reject");
    }


    /**
     * @name startRequestAllAoTs
     * 
     * @description Verifies the Start Request SP for AO TS according to NodeUC624.N,
     *
     * @param testId - unique identifier
     * @param description  
     *  
     * @throws InterruptedException
     * @throws Exception 
     */
    @Test (timeOut = 500000)
    @Parameters({ "testId", "description" })
    public void startRequestAllAoTs(String testId, String description) throws InterruptedException, Exception  {

    	int associatedSoInstance = 0;

    	setTestCase(testId, description);

        setTestStepBegin("Precondition: Start So TRXC");
        abisPrePost.startSoTrxc();
        setTestStepEnd();

    	for (int tsInstance = 0; tsInstance < 8; tsInstance++)
    	{ 
    		//Case 1: AO TS in state reset
    		setTestStepBegin("Precondition: AO TS in state reset");
    		abisHelper.resetCommand(this.moClassTs, tsInstance, associatedSoInstance);
    		setTestInfo("Precondition: MO:Ts attribute:abisTsState is RESET");
    		assertTrue("abisTsState for tsInstance " + tsInstance + " is not RESET", momHelper.waitForAbisTsMoState(tsInstance, "RESET", 5));
    		setTestStepEnd();

    		setTestStepBegin("Send Start Request command to AO TS");
    		OM_G31R01.StartResult startResult = abisHelper.startRequest(this.moClassTs, tsInstance, associatedSoInstance);
    		setTestStepEnd();

    		setTestStepBegin("Verify that MO State in Start Result is DISABLED");
    		saveAssertEquals("MO state is not what expected!", OM_G31R01.Enums.MOState.DISABLED, startResult.getMOState());
    		setTestStepEnd();

    		setTestStepBegin("Verify that MO:Ts attribute:abisTsMoState is DISABLED");
            assertTrue("abisTsState for tsInstance " + tsInstance + " is not DISABLED", momHelper.waitForAbisTsMoState(tsInstance, "DISABLED", 5));
    		setTestStepEnd();

    		//Case 2: Ao Ts in state DISABLED
    		startResult = abisHelper.startRequest(this.moClassTs, tsInstance, associatedSoInstance);

    		setTestStepBegin("Verify that MO State in Start Result is DISABLED");
    		saveAssertEquals("MO state is not what expected!", OM_G31R01.Enums.MOState.DISABLED, startResult.getMOState());

    		setTestStepBegin("Verify that MO:Ts attribute:abisTsMoState is DISABLED");
            assertTrue("abisTsState for tsInstance " + tsInstance + " is not DISABLED", momHelper.waitForAbisTsMoState(tsInstance, "DISABLED", 5));
    		setTestStepEnd();
    	}
    }
    
    /**
     * @name startRequestAoTsInStateEnabled
     * 
     * @description Verifies the Start Request SP for AO TS according to NodeUC624.E1,
     *
     * @param testId - unique identifier
     * @param description  
     *  
     * @throws InterruptedException
     * @throws Exception 
     */
    @Test (timeOut = 500000)
    @Parameters({ "testId", "description" })
    public void startRequestAoTsInStateEnabled(String testId, String description) throws InterruptedException, Exception  {
        
    	int associatedSoInstance = 0;
    	
        setTestCase(testId, description);

        setTestStepBegin("Precondition: Start So TRXC");
        abisPrePost.startSoTrxc();
        setTestStepEnd();

    	for (int tsInstance = 0; tsInstance < 8; tsInstance++)
        {
          setTestStepBegin("Send intial start to put Ao Ts in state Disabled");
          OM_G31R01.StartResult startResult = abisHelper.startRequest(this.moClassTs, tsInstance, associatedSoInstance);

          assertEquals("MO state is not what expected!", OM_G31R01.Enums.MOState.DISABLED, startResult.getMOState());
          assertTrue("abisTsState for tsInstance " + tsInstance + " is not DISABLED", momHelper.waitForAbisTsMoState(tsInstance, "DISABLED", 5));
          setTestStepEnd();
          
          setTestStepBegin("Send TS Configuration Request, instance: " + tsInstance);
          abisHelper.tsConfigRequest(tsInstance, tsInstance+1, tsInstance+1, true, OM_G31R01.Enums.Combination.TCH);
          setTestStepEnd();

          sleepSeconds(2);
          
          setTestStepBegin("Send TS Enable Request, instance: " + tsInstance);
          OM_G31R01.EnableResult result = abisHelper.enableRequest(this.moClassTs, tsInstance, associatedSoInstance);

          assertEquals("MO state is not what expected!", OM_G31R01.Enums.MOState.ENABLED, result.getMOState());
      	  assertTrue("AO TS  for tsInstance: " + tsInstance + " is not ENABLED", momHelper.waitForAbisTsMoState(tsInstance, "ENABLED", 5));
          setTestStepEnd();
        
          setTestStepBegin("Send TS Start Request in state Enabled, instance: " + tsInstance);   
          setTestStepEnd();

          try {
              abisHelper.startRequest(this.moClassTs, tsInstance, 0);
              fail("Expected start request reject");
          } catch (StartRequestRejectException e) {
              OM_G31R01.StartRequestReject rejMsg = e.getStartRequestReject();
          
              setTestStepBegin("Verify that Start Request Reject result code is 'Wrong state'");
              assertEquals("ResultCode is not what expected!", OM_G31R01.Enums.ResultCode.WrongState, rejMsg.getResultCode());
              setTestStepEnd();
              setTestStepBegin("Verify that MO:Trx attribute:abisTsMoState is ENABLED");
              
              setTestStepBegin("Verify that MO:Trx attribute:abisTsMoState is still ENABLED");
              assertTrue("AO TS  for tsInstance: " + tsInstance + " is not ENABLED", momHelper.waitForAbisTsMoState(tsInstance, "ENABLED", 5));
              setTestStepEnd();
          }
        }
    }
}
