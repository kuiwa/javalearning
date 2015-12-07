package com.ericsson.msran.test.grat.disablerequest;

import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import se.ericsson.jcat.fw.annotations.Setup;
import se.ericsson.jcat.fw.annotations.Teardown;

import com.ericsson.abisco.clientlib.AbiscoResponse;
import com.ericsson.abisco.clientlib.MessageQueue;
import com.ericsson.abisco.clientlib.servers.BG;
import com.ericsson.abisco.clientlib.servers.OM_G31R01;
import com.ericsson.abisco.clientlib.servers.BG.MeasurementResult;
import com.ericsson.abisco.clientlib.servers.BG.SetMeasurementReporting;
import com.ericsson.abisco.clientlib.servers.BG.Enums.BGMeasurementReporting;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.ChannelActAck;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.ChannelActNegAckException;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.ChannelActNormalAssign;

import com.ericsson.msran.g2.annotations.TestInfo;
import com.ericsson.msran.test.grat.happytestingistfsalive.HappyTestingIsTfsAliveTest;
import com.ericsson.msran.test.grat.testhelpers.AbisHelper;
import com.ericsson.msran.test.grat.testhelpers.MomHelper;
import com.ericsson.msran.test.grat.testhelpers.NodeStatusHelper;
import com.ericsson.msran.test.grat.testhelpers.prepost.AbisPrePost;
import com.ericsson.msran.jcat.TestBase;

/**
 * @id NodeUC511
 * 
 * @name DisableRequestTest
 * 
 * @author Asa Huhtasaari (xasahuh)
 * 
 * @created 2014-01-30
 * 
 * @description This test class verifies the Disable Request
 * 
 * @revision xasahuh 2014-01-30 Revised from first version.
 * @revision xasahuh 2014-02-07 Set timeout for test case to 1 minute.
 * @revision xasahuh 2014-02-10 Added TestInfo metadata.
 * @revision xasahuh 2014-02-12 Removed AfterClass, the RestoreStack is used instead.
 * 
 */

public class DisableRequestTest extends TestBase {
    private final OM_G31R01.Enums.MOClass  moClassTf = OM_G31R01.Enums.MOClass.TF;
    private final OM_G31R01.Enums.MOClass  moClassTx = OM_G31R01.Enums.MOClass.TX;
    private final OM_G31R01.Enums.MOClass  moClassRx = OM_G31R01.Enums.MOClass.RX;
    private final OM_G31R01.Enums.MOClass  moClassTs = OM_G31R01.Enums.MOClass.TS;

    private static final Logger myLogger = Logger.getLogger(HappyTestingIsTfsAliveTest.class);
    
    private AbisPrePost abisPrePost;
    private AbisHelper abisHelper;
    private MomHelper momHelper;
    private BG bgServer;
    private SetMeasurementReporting setMeasurementReporting;
    
    private final String GSM_SECTOR_LDN = "ManagedElement=1,BtsFunction=1,GsmSector=1";
    private NodeStatusHelper nodeStatus;

    /**
     * Description of test case for test reporting
     */
    @TestInfo(
            tcId = "NodeUC511",
            slogan = "Abis TF Disable",
            requirementDocument = "1/00651-FCP 130 1402",
            requirementRevision = "PC5",
            requirementLinkTested = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff87c1d941?docno=1/00651-FCP1301402Uen&format=excel8book",
            requirementLinkLatest = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff86a2af3d?docno=1/00651-FCP1301402Uen&action=current&format=excel8book",
            requirementIds = { "10565-0334/21767[PA1][PREL]", "10565-0334/19034[A][APPR]",
                    "10565-0334/19417[A][APPR]" },
            verificationStatement = "Verifies NodeUC511.N",
            testDescription = "Verifies Abis TF Disable.",
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
        bgServer    = abisHelper.getBG();
        
        abisPrePost.preCondAllMoStateStarted();
        
        setMeasurementReporting =  bgServer.createSetMeasurementReporting();
        setMeasurementReporting.setBGMeasurementReporting(BGMeasurementReporting.On);
        setMeasurementReporting.send();
        setTestStepEnd();
    }
    
    /**
     * Postcond.
     */
    @Teardown
    public void teardown() {
    	setTestStepBegin("Teardown");
        nodeStatus.isNodeRunning();
        
        setMeasurementReporting.setBGMeasurementReporting(BGMeasurementReporting.Off);
        try {
			setMeasurementReporting.send();
		} catch (InterruptedException e) {
			myLogger.warn("setMeasurementReporting.send() got Interrupted!");
			e.printStackTrace();
		}
        setTestStepEnd();
    }
    
    /**
     * @name disableRequestAoTf
     * 
     * @description Verifies Disable Request EP for SO SCF according to NodeUC511.N
     * 
     * @param testId - unique identifier
     * @param description
     * 
     * @throws InterruptedException
     */
    @Test (timeOut = 360000)
    @Parameters({ "testId", "description" })
    public void disableRequestAoTf(String testId, String description) throws InterruptedException {
    	
        setTestCase(testId, description);

        /**
         * Case 1: Disable Request in ENABLED state 
         */
        setTestStepBegin("AO TF Disable Request in ENABLED state");        
    	setTestInfo("Send Disable Request to AO TF");

    	OM_G31R01.DisableResult result = abisHelper.disableRequest(this.moClassTf);

    	setTestInfo("Verify that MO State in Disable Result is DISABLED");
    	assertEquals("MO state (" + result.getMOState().toString() + ") not DISABLED", OM_G31R01.Enums.MOState.DISABLED, result.getMOState());

    	setTestInfo("Verify that MO:GsmSector attribute:abisTfMoState is DISABLED");
        assertTrue("MO " + GSM_SECTOR_LDN + " abisTfState is not DISABLED", 
        		momHelper.waitForMoAttributeStringValue(GSM_SECTOR_LDN, "abisTfState", "DISABLED", 6));
        setTestStepEnd();

        /**
         * Case 2: Disable Request in DISABLED state 
         */
        setTestStepBegin("AO TF Disable Request in DISABLED state");
        setTestInfo("Precondition: Check TF State DISABLED");
        assertTrue("abisToMoState is not DISABLED", momHelper.waitForAbisTfMoState("DISABLED")); 

        result = abisHelper.disableRequest(this.moClassTf);

        setTestInfo("Verify that MO State in Disable Result is DISABLED");
        assertEquals("MO State in Disable Result is NOT DISABLED", OM_G31R01.Enums.MOState.DISABLED, result.getMOState());

        setTestInfo("Verify that MO:GsmSector attribute:abisTfMoState is DISABLED");
        assertTrue("MO " + GSM_SECTOR_LDN + " abisTfState is not DISABLED", 
                momHelper.waitForMoAttributeStringValue(GSM_SECTOR_LDN, "abisTfState", "DISABLED", 6));
      
        setTestStepEnd();
    }
    
    /**
     * @todo NodeUC511.A1: Abis TF Disable with unlocked TRXs 
     */
    /*@Test (timeOut = ...)
    @Parameters({ "testId", "description" })
    public void disableRequestAoTfUnlockedTrx(String testId, String description) throws InterruptedException {
        setTestcase(testId, description);
    }*/
    
    /**
     * @name disableRequestAoTx
     * 
     * @description Verifies Disable Request EP for AO TX according to NodeUC600.N
     * 
     * @param testId - unique identifier
     * @param description
     * 
     * @throws InterruptedException
     */
    @Test (timeOut = 360000)
    @Parameters({ "testId", "description" })
    public void disableRequestAoTx(String testId, String description) throws InterruptedException {
        
        setTestCase(testId, description);
        
        setTestStepBegin("Enable AO TX");
        OM_G31R01.TXConfigurationResult confRes = abisHelper.txConfigRequest(0, momHelper.getArfcnToUse(), false, 27, false);
        assertEquals("AccordanceIndication is NOT AccordingToRequest", 
                OM_G31R01.Enums.AccordanceIndication.AccordingToRequest, 
        		confRes.getAccordanceIndication().getAccordanceIndication());
        abisHelper.enableRequest(this.moClassTx, 0);
        assertTrue("abisTxMoState is not ENABLED", momHelper.waitForAbisTxMoState("ENABLED")); 
        setTestStepEnd();
        
        setTestStepBegin("Send Disable Request to AO TX");
        OM_G31R01.DisableResult result = abisHelper.disableRequest(this.moClassTx);
        setTestStepEnd();
        
        setTestStepBegin("Verify that MO State in Disable Result is DISABLED");
        assertEquals("MO State in Disable Result is NOT DISABLED", result.getMOState(), OM_G31R01.Enums.MOState.DISABLED);
        setTestStepEnd();
        
        setTestStepBegin("Verify that MO:Trx attribute:abisTxMoState is DISABLED");
        assertTrue("Trx abisTxMoState is not DISABLED", momHelper.waitForAbisTxMoState("DISABLED"));
        setTestStepEnd();
    }
    
    /**
     * @name disableRequestAoTxInStateDisabled
     * 
     * @description Verifies Disable Request EP for AO TX according to NodeUC600.A1
     * 
     * @param testId - unique identifier
     * @param description
     * 
     * @throws InterruptedException
     */
    @Test (timeOut = 360000)
    @Parameters({ "testId", "description" })
    public void disableRequestAoTxInStateDisabled(String testId, String description) throws InterruptedException {
        
        setTestCase(testId, description);
        
        setTestStepBegin("Verify AO TX in state disabled");
        assertTrue("abisTxMoState is not DISABLED", momHelper.waitForAbisTxMoState("DISABLED"));
        setTestStepEnd();
        
        setTestStepBegin("Send Disable Request to AO TX");
        OM_G31R01.DisableResult result = abisHelper.disableRequest(this.moClassTx);
        setTestStepEnd();
        
        setTestStepBegin("Verify that MO State in Disable Result is DISABLED");
        assertEquals("MO State in Disable Result is NOT DISABLED", OM_G31R01.Enums.MOState.DISABLED, result.getMOState());
        setTestStepEnd();
        
        setTestStepBegin("Verify that MO:Trx attribute:abisTxMoState is DISABLED");
        assertTrue("abisTxMoState is not DISABLED", momHelper.waitForAbisTxMoState("DISABLED"));
        setTestStepEnd();
    }
    
    /**
     * @name disableRequestAoRx
     * 
     * @description Verifies Disable Request EP for AO RX according to NodeUC605.N
     * 
     * @param testId - unique identifier
     * @param description
     * 
     * @throws InterruptedException
     */
    @Test (timeOut = 360000)
    @Parameters({ "testId", "description" })
    public void disableRequestAoRx(String testId, String description) throws InterruptedException {
        
        setTestCase(testId, description);
        
        setTestStepBegin("Enable AO RX");
        OM_G31R01.RXConfigurationResult confRes = abisHelper.rxConfigRequest(0, momHelper.getArfcnToUse(), false, "");
        assertEquals("AccordanceIndication is NOT AccordingToRequest", 
        		OM_G31R01.Enums.AccordanceIndication.AccordingToRequest,
        		confRes.getAccordanceIndication().getAccordanceIndication());
        abisHelper.enableRequest(this.moClassRx, 0);
        assertTrue("abisRxMoState is not ENABLED", momHelper.waitForAbisRxMoState("ENABLED")); 
        setTestStepEnd();
        
        setTestStepBegin("Send Disable Request to AO RX");
        OM_G31R01.DisableResult result = abisHelper.disableRequest(this.moClassRx);
        setTestStepEnd();
        
        setTestStepBegin("Verify that MO State in Disable Result is DISABLED");
        assertEquals("MO state is not DISABLED", OM_G31R01.Enums.MOState.DISABLED, result.getMOState());
        setTestStepEnd();
        
        setTestStepBegin("Verify that MO:Trx attribute:abisRxMoState is DISABLED");
        assertTrue("abisRxMoState is not DISABLED", momHelper.waitForAbisRxMoState("DISABLED")); 
        setTestStepEnd();
    }
    
    /**
     * @name disableRequestAoRxInStateDisabled
     * 
     * @description Verifies Disable Request EP for AO RX according to NodeUC605.A1
     * 
     * @param testId - unique identifier
     * @param description
     * 
     * @throws InterruptedException
     */
    @Test (timeOut = 360000)
    @Parameters({ "testId", "description" })
    public void disableRequestAoRxInStateDisabled(String testId, String description) throws InterruptedException {
        
        setTestCase(testId, description);
        
        setTestStepBegin("Verify AO RX in state disabled");
        assertTrue("abisRxMoState is not DISABLED", momHelper.waitForAbisRxMoState("DISABLED")); 
        setTestStepEnd();
        
        setTestStepBegin("Send Disable Request to AO RX");
        OM_G31R01.DisableResult result = abisHelper.disableRequest(this.moClassRx);
        setTestStepEnd();
        
        setTestStepBegin("Verify that MO State in Disable Result is DISABLED");
        assertEquals("MO state (" + result.getMOState().toString() + ") not DISABLED", result.getMOState(), OM_G31R01.Enums.MOState.DISABLED);
        setTestStepEnd();
        
        setTestStepBegin("Verify that MO:Trx attribute:abisRxMoState is DISABLED");
        assertTrue("abisRxMoState is not DISABLED", momHelper.waitForAbisRxMoState("DISABLED")); 
        setTestStepEnd();
    }
    
    /**
     * @name pollMeasurementResults
     * 
     * @description Fetches Measurement Results 
     * 
     */
	public void pollMeasurementResults ()
	{   
	    MessageQueue<MeasurementResult > measurementResultQueue = bgServer.getMeasurementResultQueue();

	    try        
	    {
	    	AbiscoResponse msg = measurementResultQueue.poll(5, TimeUnit.SECONDS);
	    	if (msg != null) 
	    	{
	    		fail("Measurement Result queue should be empty");
	    	}

	    } catch (Exception e) {

	    	setTestInfo("Verified that there are no measurement reports");
	    }
	}
    
    /**
     * @name disableRequestAllAoTs
     * 
     * @description Verifies Disable Request EP for AO TS according to NodeUC625.N
     * 
     * @param testId - unique identifier
     * @param description
     * 
     * @throws InterruptedException
     * @throws JSONException 
     */
    @Test (timeOut = 60000)
    @Parameters({ "testId", "description" })
    public void disableRequestAllAoTs(String testId, String description) throws InterruptedException, JSONException {

    	int associatedSoInstance = 0;
    	ChannelActNormalAssign msg;
    	
    	setTestCase(testId, description);

    	// Set all TS instances in state ENABLED
    	setTestStepBegin("Precondition: Enabel all TS instances");
    	for (int tsInstance = 0; tsInstance < 8; tsInstance++)
    	{ 
    		setSubTestStep("Configure AO TS instance " + tsInstance);
    		OM_G31R01.TSConfigurationResult confRes = abisHelper.tsConfigRequest(tsInstance, tsInstance, tsInstance, false, OM_G31R01.Enums.Combination.TCH);
            assertEquals("AccordanceIndication is not AccordingToRequest", OM_G31R01.Enums.AccordanceIndication.AccordingToRequest, confRes.getAccordanceIndication().getAccordanceIndication());
    		
            setSubTestStep("Send Enable Request to AO TS instance " + tsInstance);
    		abisHelper.enableRequest(this.moClassTs, tsInstance, associatedSoInstance);

    		setSubTestStep("Verify that MO:Trx attribute:abisTsMoState for instance " + tsInstance + " is ENABLED");
    		assertTrue("abisTsMoState for tsInstance " + tsInstance + " is not ENABLED", momHelper.waitForAbisTsMoState(tsInstance, "ENABLED", 5));
    	}
    	setTestStepEnd();
    	
    	// Set all TS instances in state DISABLED
    	for (int tsInstance = 0; tsInstance < 8; tsInstance++)
    	{ 
    		setTestStepBegin("Send Disable Request to AO TS instance " + tsInstance);
    		OM_G31R01.DisableResult result = abisHelper.disableRequest(this.moClassTs, tsInstance, associatedSoInstance);
    		setTestStepEnd();

    		setTestStepBegin("Verify that MO State in Disable Result is DISABLED");
    		assertEquals("MO state (" + result.getMOState().toString() + ") is not DISABLED", OM_G31R01.Enums.MOState.DISABLED, result.getMOState());
    		setTestStepEnd();

    		setTestStepBegin("Verify that MO:Trx attribute:abisTsMoState for instance " + tsInstance + " is DISABLED");
    		assertTrue("abisTsMoState for tsInstance " + tsInstance + " is not DISABLED", momHelper.waitForAbisTsMoState(tsInstance, "DISABLED", 5));
    		setTestStepEnd();
    		
    		/* tn is equal to tsInstance in this test*/
    		setTestStepBegin("Verify that channel activation is rejected for tn " + tsInstance);
        	msg = abisHelper.channelActivationNormalAssignment (tsInstance); 
       		try {
       			myLogger.debug(msg.toString());

       			ChannelActAck ack = msg.send();
       			myLogger.debug(ack.toString());

       			fail("Error: Expected channel activation to be rejected since instance: " + tsInstance + " is DISABLED");

       		} catch (ChannelActNegAckException e) {
       			setTestInfo("Verified that it's not possible to activate channel on tn " + tsInstance + " wich is configured on instance: " + tsInstance + " that is DISABLED");
       		}
       		setTestStepEnd();
    	}
    	
    	setTestStepBegin("Verify that there are no measurement reports");
    	pollMeasurementResults();
    	setTestStepEnd();
    }
}
