package com.ericsson.msran.test.grat.enablerequest;

import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.ericsson.msran.jcat.TestBase;
import se.ericsson.jcat.fw.annotations.Setup;
import se.ericsson.jcat.fw.annotations.Teardown;

import com.ericsson.abisco.clientlib.AbiscoResponse;
import com.ericsson.abisco.clientlib.MessageQueue;
import com.ericsson.abisco.clientlib.servers.BG;
import com.ericsson.abisco.clientlib.servers.OM_G31R01;
import com.ericsson.abisco.clientlib.servers.BG.MeasurementResult;
import com.ericsson.abisco.clientlib.servers.BG.SetMeasurementReporting;
import com.ericsson.abisco.clientlib.servers.BG.Enums.BGMeasurementReporting;
import com.ericsson.abisco.clientlib.servers.OM_G31R01.EnableRequestRejectException;
import com.ericsson.abisco.clientlib.servers.OM_G31R01.Enums;
import com.ericsson.abisco.clientlib.servers.OM_G31R01.RXConfigurationRequestRejectException;
import com.ericsson.abisco.clientlib.servers.OM_G31R01.TXConfigurationRequestRejectException;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.ChannelActAck;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.ChannelActNegAckException;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.ChannelActNormalAssign;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.RFChannelRelease;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.RFChannelReleaseAck;
import com.ericsson.msran.g2.annotations.TestInfo;
import com.ericsson.msran.test.grat.happytestingistfsalive.HappyTestingIsTfsAliveTest;
import com.ericsson.msran.test.grat.testhelpers.AbisHelper;
import com.ericsson.msran.test.grat.testhelpers.MomHelper;
import com.ericsson.msran.test.grat.testhelpers.NodeStatusHelper;
import com.ericsson.msran.test.grat.testhelpers.prepost.AbisPrePost;

/**
 * @id NodeUC513
 * 
 * @name EnableRequestTest
 * 
 * @author Asa Huhtasaari (xasahuh)
 * 
 * @created 2014-01-31
 * 
 * @description This test class verifies the Enable Request
 * 
 * @revision xasahuh 2014-01-31 Revised from first version.
 * @revision xasahuh 2014-02-07 Set timeout for test case to 1 minute.
 * @revision xasahuh 2014-02-10 Added TestInfo metadata.
 * @revision xasahuh 2014-02-12 Removed AfterClass, the RestoreStack is used instead.
 * 
 */

public class EnableRequestTest extends TestBase {
    private final OM_G31R01.Enums.MOClass  moClassTf = OM_G31R01.Enums.MOClass.TF;
    private final OM_G31R01.Enums.MOClass moClassTx = OM_G31R01.Enums.MOClass.TX;    
    private final OM_G31R01.Enums.MOClass moClassRx = OM_G31R01.Enums.MOClass.RX;    
    private final OM_G31R01.Enums.MOClass moClassTs = OM_G31R01.Enums.MOClass.TS;
    
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
            tcId = "NodeUC513",
            slogan = "Abis TF Enable",
            requirementDocument = "1/00651-FCP 130 1402",
            requirementRevision = "PC5",
            requirementLinkTested = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff87c1d941?docno=1/00651-FCP1301402Uen&format=excel8book",
            requirementLinkLatest = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff86a2af3d?docno=1/00651-FCP1301402Uen&action=current&format=excel8book",
            requirementIds = { "10565-0334/21767[PA1][PREL]", "10565-0334/19034[A][APPR]",
                    "10565-0334/19417[A][APPR]" },
            verificationStatement = "Verifies NodeUC513.N",
            testDescription = "Verifies Abis TF Enable.",
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
        
        /**
         * @todo Create TRX MO when postconditions for it can be verified (sync is ok).
         */
        
        /**
         * @todo Verify Trx MO pre and post cond when we have sync working
         */        
        
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
     * @name enableRequestAoTf
     * 
     * @description Verifies Enable Request EP for SO SCF according to NodeUC513.N
     * 
     * @param testId - unique identifier
     * @param description
     * 
     * @throws InterruptedException
     */
    @Test (timeOut = 360000)
    @Parameters({ "testId", "description" })
    public void enableRequestAoTf(String testId, String description) throws InterruptedException {
    	
    	setTestCase(testId, description);
    	
        /**
         * Case 1: Enable Request in state ENABLED (after setup)
         */
        setTestStepBegin("Enable Request to AO TF in state ENABLED");
        
        setTestInfo("Precondition: Check that AO TF is in state ENABLE");
        assertTrue("MO " + GSM_SECTOR_LDN + " abisTfState is not ENABLED", 
                momHelper.waitForMoAttributeStringValue(GSM_SECTOR_LDN, "abisTfState", "ENABLED", 6));
        
        setTestInfo("Send Enable Request to AO TF");
        OM_G31R01.EnableResult enableResult = abisHelper.enableRequest(this.moClassTf, 0);
        
        setTestInfo("Verify that MO State in Enable Result is ENABLED");
        assertEquals("MO State in Enable Result is not ENABLED", OM_G31R01.Enums.MOState.ENABLED, enableResult.getMOState());

        setTestInfo("Verify that MO:GsmSector attribute:abisTfMoState is ENABLED");
        assertTrue("MO " + GSM_SECTOR_LDN + " abisTfState is not ENABLED", 
                momHelper.waitForMoAttributeStringValue(GSM_SECTOR_LDN, "abisTfState", "ENABLED", 6));
        

        /**
         * Case 2: Enable Request in state DISABLED
         */
        setTestStepBegin("Enable Request to AO TF in state DISABLED");
    	setTestInfo("Send Enable Request to AO TF");

    	OM_G31R01.DisableResult disableResult = abisHelper.disableRequest(this.moClassTf);
    	setTestStepEnd();

    	setTestStepBegin("Verify that MO State in Enable Result is DISABLED");
    	saveAssertEquals("MO state is not DISABLED", OM_G31R01.Enums.MOState.DISABLED, disableResult.getMOState());
    	setTestStepEnd();

        setTestStepBegin("Verify that MO State in Enable Result is ENABLED");
    	enableResult = abisHelper.enableRequest(this.moClassTf, 0);
    	setTestStepBegin("Verify that MO:GsmSector attribute:abisTfMoState is ENABLED");
        assertTrue("MO " + GSM_SECTOR_LDN + " abisTfState is not ENABLED", 
        		momHelper.waitForMoAttributeStringValue(GSM_SECTOR_LDN, "abisTfState", "ENABLED", 6));
    	
    	setTestWarning("Trx MO operationalState and availabilityStatus is currently not verified (no sync)");
      	setTestStepEnd();
    	
    	/**
    	 * @todo Verify postconditions: All TRX MOs with administrativeState=UNLOCKED in the 
    	 * same GSM Sector MO have attribute operationalState = ENABLED and availabilityStatus=NO_STATUS
    	 */
    	
    	//setTestStep("Postcondition: MO:Trx attribute:availabilityStatus is NO_STATUS");
    	//assertEquals("attribute:availabilityStatus is not NO_STATUS", "NO_STATUS", momHelper.getTrxAvailStatus());
    	
    	//setTestStep("Postcondition: MO:Trx attribute:operationalState is ENABLED");
    	//assertEquals("attribute:operationalState is not ENABLED", "ENABLED", momHelper.getTrxOpState());
    	//setTestStepEnd();
    }
    
    /**
     * @name enableRequestAoTx
     * 
     * @description Verifies Enable Request EP for AO TX according to NodeUC602.N
     * 
     * @param testId - unique identifier
     * @param description
     * 
     * @throws InterruptedException
     */
    @Test (timeOut = 360000)
    @Parameters({ "testId", "description" })
    public void enableRequestAoTx(String testId, String description) throws InterruptedException {
        
        setTestCase(testId, description);
        
        setTestStepBegin("Verify that MO State is DISABLED");
        saveAssertTrue("abisTxMoState is not DISABLED", momHelper.waitForAbisTxMoState("DISABLED"));
        setTestStepEnd();

        setTestStepBegin("Configure AO TX");
        OM_G31R01.TXConfigurationResult confRes = abisHelper.txConfigRequest(0, momHelper.getArfcnToUse(), false, 27, false);
        saveAssertEquals("According Indication must be According to Request",
        		Enums.AccordanceIndication.AccordingToRequest,
        		confRes.getAccordanceIndication().getAccordanceIndication());
        setTestStepEnd();
        
        setTestStepBegin("Send Enable Request to AO TX");
        OM_G31R01.EnableResult result = abisHelper.enableRequest(this.moClassTx, 0);
        setTestStepEnd();
        
        setTestStepBegin("Verify that MO State in Enable Result is ENABLED");
        saveAssertEquals("MoState is not ENABLED", OM_G31R01.Enums.MOState.ENABLED, result.getMOState());
        setTestStepEnd();

        setTestStepBegin("Verify that MO:Trx attribute:abisTxMoState is ENABLED");
        saveAssertTrue("abisTxMoState is not ENABLED", momHelper.waitForAbisTxMoState("ENABLED"));
        setTestStepEnd();
    }
    
    /**
     * @name enableRequestAoTxInStateEnabled
     * 
     * @description Verifies Enable Request EP for AO TX according to NodeUC602.A1
     * 
     * @param testId - unique identifier
     * @param description
     * 
     * @throws InterruptedException
     */
    @Test (timeOut = 360000)
    @Parameters({ "testId", "description" })
    public void enableRequestAoTxInStateEnabled(String testId, String description) throws InterruptedException {
        
        setTestCase(testId, description);
       
        setTestStepBegin("Enable AO TX first time");
        OM_G31R01.TXConfigurationResult confRes = abisHelper.txConfigRequest(0, momHelper.getArfcnToUse(), false, 27, false);
        saveAssertEquals("According Indication must be According to Request",
        		Enums.AccordanceIndication.AccordingToRequest, 
        		confRes.getAccordanceIndication().getAccordanceIndication());
        
        OM_G31R01.EnableResult result = abisHelper.enableRequest(this.moClassTx, 0);
        saveAssertEquals("MoState is not ENABLED", OM_G31R01.Enums.MOState.ENABLED, result.getMOState());
        setTestStepEnd();
        
        
        setTestStepBegin("Verify that AO TX can be ENABLED in state ENABLED");
        result = abisHelper.enableRequest(this.moClassTx, 0);
        saveAssertEquals("MoState is not ENABLED", OM_G31R01.Enums.MOState.ENABLED, result.getMOState());
        saveAssertTrue("abisTxMoState is not ENABLED", momHelper.waitForAbisTxMoState("ENABLED"));
        setTestStepEnd();
    }
    
    /**
     * @name enableRequestAoTxInvalidConfig
     * 
     * @description Verifies Enable Request EP for AO TX according to NodeUC602.E1
     * 
     * @param testId - unique identifier
     * @param description
     * 
     * @throws InterruptedException
     */
    @Test (timeOut = 360000)
    @Parameters({ "testId", "description" })
    public void enableRequestAoTxInvalidConfig(String testId, String description) throws InterruptedException {
        
        setTestCase(testId, description);
        
        setTestStepBegin("Verify that MO State is DISABLED");
        saveAssertTrue("abisTxMoState is not DISABLED", momHelper.waitForAbisTxMoState("DISABLED"));
        setTestStepEnd();
    	
    	try {
    		abisHelper.txConfigRequest(0, 1, true, 27, false);
    	} catch (TXConfigurationRequestRejectException e1) {
			assertEquals("TXConfigurationRequestRejectException is not for ProtocolError", 
					Enums.ResultCode.ProtocolError, e1.getTXConfigurationRequestReject().getResultCode());
    		try {
    			abisHelper.enableRequest(this.moClassTx, 0);
    		} catch (EnableRequestRejectException e2) {
    			assertEquals("EnableRequestRejectException is not for ProtocolError", 
    					Enums.ResultCode.ProtocolError, e2.getEnableRequestReject().getResultCode());
    			setTestStepBegin("Verify that AO TX is still DISABLED");
    			assertTrue("abisTxMoState is not DISABLED", momHelper.waitForAbisTxMoState("DISABLED"));
    			setTestStepEnd();
    			return;
    		}
    	}
        
        fail("Expected Enable Request to be rejected");
       
    }
    
    /**
     * @name enableRequestAoTxNoFrequency
     * 
     * @description Verifies Enable Request EP for AO TX according to NodeUC602.E1
     * 
     * @param testId - unique identifier
     * @param description
     * 
     * @throws InterruptedException
     */
    @Test (timeOut = 360000)
    @Parameters({ "testId", "description" })
    public void enableRequestAoTxNoFrequency(String testId, String description) throws InterruptedException {
        setTestCase(testId, description);

        setTestStepBegin("Verify that MO State is DISABLED");
        saveAssertTrue("abisTxMoState is not DISABLED", momHelper.waitForAbisTxMoState("DISABLED"));
        setTestStepEnd();

        setTestStepBegin("Configure AO TX");
        OM_G31R01.TXConfigurationResult confRes = abisHelper.txConfigRequest(0, 1023, true, 27, false);
        saveAssertEquals("According Indication must be According to Request",
                Enums.AccordanceIndication.AccordingToRequest,
                confRes.getAccordanceIndication().getAccordanceIndication());
        setTestStepEnd();

        setTestStepBegin("Send Enable Request to AO TX");
        try {
            abisHelper.enableRequest(this.moClassTx, 0);
        } catch (EnableRequestRejectException e) {
            assertEquals("EnableRequestRejectException is not for ProtocolError", 
                    Enums.ResultCode.ProtocolError, e.getEnableRequestReject().getResultCode());
            setTestStepBegin("Verify that AO TX is still DISABLED");
            assertTrue("abisTxMoState is not DISABLED", momHelper.waitForAbisTxMoState("DISABLED"));
            setTestStepEnd();
            return;
        }
        fail("Expected Enable Request to be rejected");

    }
        
    
    /**
     * @name enableRequestAoRx
     * 
     * @description Verifies Enable Request EP for AO RX according to NodeUC607.N
     * 
     * @param testId - unique identifier
     * @param description
     * 
     * @throws InterruptedException
     */
    @Test (timeOut = 360000)
    @Parameters({ "testId", "description" })
    public void enableRequestAoRx(String testId, String description) throws InterruptedException {
        
        setTestCase(testId, description);
        
        setTestStepBegin("Verify that MO State is DISABLED");
        saveAssertTrue("abisRxMoState is not DISABLED", momHelper.waitForAbisRxMoState("DISABLED"));
        setTestStepEnd();

        setTestStepBegin("Configure AO RX");
        OM_G31R01.RXConfigurationResult confRes = abisHelper.rxConfigRequest(0, momHelper.getArfcnToUse(), false, "");
        saveAssertEquals("According Indication must be According to Request",
        		Enums.AccordanceIndication.AccordingToRequest,
        		confRes.getAccordanceIndication().getAccordanceIndication());        
        setTestStepEnd();
        
        setTestStepBegin("Send Enable Request to AO RX");
        OM_G31R01.EnableResult result = abisHelper.enableRequest(this.moClassRx, 0);
        setTestStepEnd();
        
        setTestStepBegin("Verify that MO State in Enable Result is ENABLED");
        saveAssertEquals("MoState is not ENABLED", OM_G31R01.Enums.MOState.ENABLED, result.getMOState());
        setTestStepEnd();
  
        setTestStepBegin("Verify that MO:Trx attribute:abisRxMoState is ENABLED");
        saveAssertTrue("abisRxMoState is not ENABLED", momHelper.waitForAbisRxMoState("ENABLED"));
        setTestStepEnd();
    }
    
    /**
     * @name enableRequestAoRxInStateEnabled
     * 
     * @description Verifies Enable Request EP for AO RX according to NodeUC607.A1
     * 
     * @param testId - unique identifier
     * @param description
     * 
     * @throws InterruptedException
     */
    @Test (timeOut = 360000)
    @Parameters({ "testId", "description" })
    public void enableRequestAoRxInStateEnabled(String testId, String description) throws InterruptedException {
        
        setTestCase(testId, description);
       
        setTestStepBegin("Enable AO RX first time");
        OM_G31R01.RXConfigurationResult confRes = abisHelper.rxConfigRequest(0, momHelper.getArfcnToUse(), false, "");
        saveAssertEquals("According Indication must be according to request",
        		Enums.AccordanceIndication.AccordingToRequest,
        		confRes.getAccordanceIndication().getAccordanceIndication());        	
        OM_G31R01.EnableResult result = abisHelper.enableRequest(this.moClassRx, 0);
        saveAssertEquals("MoState is not ENABLED", OM_G31R01.Enums.MOState.ENABLED, result.getMOState());
        setTestStepEnd();
        
        setTestStepBegin("Verify that AO RX can be ENABLED in state ENABLED");
        result = abisHelper.enableRequest(this.moClassRx, 0);
        saveAssertEquals("MoState is not ENABLED", OM_G31R01.Enums.MOState.ENABLED, result.getMOState());
        saveAssertTrue("abisRxMoState is not ENABLED", momHelper.waitForAbisRxMoState("ENABLED"));
        setTestStepEnd();
    }
    
    /**
     * @name enableRequestAoRxInvalidConfig
     * 
     * @description Verifies Enable Request EP for AO RX according to NodeUC607.E1
     * 
     * @param testId - unique identifier
     * @param description
     * 
     * @throws InterruptedException
     */
    @Test (timeOut = 360000)
    @Parameters({ "testId", "description" })
    public void enableRequestAoRxInvalidConfig(String testId, String description) throws InterruptedException {
        
        setTestCase(testId, description);
        
        setTestStepBegin("Verify that MO State is DISABLED");
        assertTrue("abisRxMoState is not DISABLED", momHelper.waitForAbisRxMoState("DISABLED"));
        setTestStepEnd();
        
    	try {
    		abisHelper.rxConfigRequest(0, 1, true, "");
    	} catch (RXConfigurationRequestRejectException e1) {
    		assertEquals("RXConfigurationRequestRejectException is not for ProtocolError", 
    				Enums.ResultCode.ProtocolError, e1.getRXConfigurationRequestReject().getResultCode());
    		try {
    			abisHelper.enableRequest(this.moClassRx, 0);
    		} catch (EnableRequestRejectException e2) {
    			assertEquals("Not ProtocolError", Enums.ResultCode.ProtocolError, e2.getEnableRequestReject().getResultCode());
    			setTestStepBegin("Verify that AO RX is still DISABLED");
    			assertTrue("abisRxMoState is not DISABLED", momHelper.waitForAbisRxMoState("DISABLED"));
    			setTestStepEnd();
    			return;
    		}
    	}
        
        fail("Expected Enable Request to be rejected");  
    }
    
    /**
     * @name pollMeasurementResults
     * 
     * @description Fetches Measurement Results 
     * 
     */
    public void pollMeasurementResults ()
    {   
    	int i=0;

    	MessageQueue<MeasurementResult > measurementResultQueue = bgServer.getMeasurementResultQueue();

    	try        
    	{
    		AbiscoResponse msg;
    		while (i < 10)
    		{
    			msg = measurementResultQueue.poll(5, TimeUnit.SECONDS);
    			if (msg == null) 
    			{
    				fail("Measurement Result queue shouldn't be empty");
    				break;
    			}

    			myLogger.debug(msg.toString());

    			i++;
    		}
    	} catch (Exception e) {
    		myLogger.debug(e.toString());
    		e.printStackTrace();
    		fail("Measurement Result exception");
    	}
    }

	/**
	 * @name channelRelease
	 * 
	 * @description Verifies channelRelease 
	 * 
	 * @param tn - timeslot number
	 * 
	 */
	public void channelRelease (int tn)
	{
		try 
		{
			RFChannelRelease msg = abisHelper.channelRelease(tn);
			myLogger.debug(msg.toString());

			RFChannelReleaseAck ack = msg.send();
			myLogger.debug(ack.toString());

		} 
		catch (Exception e)
		{
			myLogger.debug(e.toString());
			e.printStackTrace();
		}
	}
	     
    /**
     * @name enableRequestAllAoTs
     * 
     * @description Verifies Enable Request EP for AO TS according to NodeUC627.N
     * 
     * @param testId - unique identifier
     * @param description
     * 
     * @throws InterruptedException
     * @throws JSONException 
     */
    @Test (timeOut = 120000)
    @Parameters({ "testId", "description" })
    public void enableRequestAllAoTs(String testId, String description) throws InterruptedException, JSONException {

    	int associatedSoInstance = 0;
    	ChannelActNormalAssign msg;
    	
    	setTestCase(testId, description);
    	
    	for (int tsInstance = 0; tsInstance < 8; tsInstance++)
    	{ 
    		setTestStepBegin("Verify that MO State for instance "+ tsInstance + " is DISABLED");  
    		assertTrue("abisTsMoState for tsInstance " + tsInstance + " is not DISABLED", momHelper.waitForAbisTsMoState(tsInstance, "DISABLED", 5));
    		setTestStepEnd();
    		
    		OM_G31R01.TSConfigurationResult confRes = abisHelper.tsConfigRequest(tsInstance, tsInstance, tsInstance, false, OM_G31R01.Enums.Combination.TCH);
            assertEquals("AccordanceIndication is not AccordingToRequest", 
            		OM_G31R01.Enums.AccordanceIndication.AccordingToRequest,
            		confRes.getAccordanceIndication().getAccordanceIndication());
    		setTestStepEnd();

    		setTestStepBegin("Send Enable Request to AO TS instance " + tsInstance);
    		OM_G31R01.EnableResult result = abisHelper.enableRequest(this.moClassTs, tsInstance, associatedSoInstance);
    		setTestStepEnd();

    		setTestStepBegin("Verify that MO State in Enable Result is ENABLED");
    		assertEquals("MO state is not ENABLED", OM_G31R01.Enums.MOState.ENABLED, result.getMOState());
    		setTestStepEnd();

    		setTestStepBegin("Verify that MO:Trx attribute:abisTsMoState for instance " + tsInstance + " is ENABLED");
    		assertTrue("abisTsMoState for tsInstance " + tsInstance + " is not ENABLED", momHelper.waitForAbisTsMoState(tsInstance, "ENABLED", 5));
            setTestStepEnd();
            
    		/* tn is equal to tsInstance in this test*/
    		setTestStepBegin("Verify that channel activation is possible for tn " + tsInstance);
        	msg = abisHelper.channelActivationNormalAssignment (tsInstance); 
       		try {
       			myLogger.debug(msg.toString());

       			ChannelActAck ack = msg.send();
       			myLogger.debug(ack.toString());

       			setTestInfo("Verified that it's possible to activate channel on tn " + tsInstance + " which is configured on instance: " + tsInstance);

       		} catch (ChannelActNegAckException e) {
       			fail("Error: Expected channel activation to be possible since instance: " + tsInstance + " is ENABLED");
       		}
       		setTestStepEnd();
       		
       	   	setTestStepBegin("Verify that there are measurement reports");
        	pollMeasurementResults();
        	setTestStepEnd();
        	
    		setTestStepBegin("Clean up: Release Channel");
            channelRelease (tsInstance);
            setTestStepEnd();
    	}
    }
    
    /**
     * @name enableRequestAoTs invalid configuration time slot already used
     * 
     * @description Verifies Enable Request EP for AO TS according to NodeUC627.E2
     * 
     * @param testId - unique identifier
     * @param description
     * 
     * @throws Exception 
     * 
     */
    @Test (timeOut = 300000)
    @Parameters({ "testId", "description" })
    public void enableRequestAoTsInvalidConfig_TimeSlotAlreadyUsed(String testId, String description) throws Exception {

    	// choose tsInstance (0-7) randomly
    	int seed = (int)System.currentTimeMillis();  
    	seed = (seed < 0) ? ((-1) * seed) : seed;
    	seed = (seed > 2000) ? (seed % 2000) : seed;
    	int tsInstance1 = (seed+1) % 8;
    	int tsInstance2 = (seed+2) % 8;
    	int associatedSoInstance = 0;

    	setTestCase(testId, description);
    	
    	setTestStepBegin("Verify AO TS is in disabled state, instance: " + tsInstance1);
    	assertTrue("AO TS is not DISABLED", momHelper.waitForAbisTsMoState(tsInstance1, "DISABLED", 5));
    	setTestStepEnd();
    	
    	setTestStepBegin("Verify AO TS is in disabled state, instance: " + tsInstance2);
    	assertTrue("AO TS is not DISABLED", momHelper.waitForAbisTsMoState(tsInstance2, "DISABLED", 5));
    	setTestStepEnd();

    	setTestStepBegin("Send TS Configuration Request, to instance: " + tsInstance1);
        setTestDebug("Random seed used for configuring TS: " + seed);
    	abisHelper.tsConfigRequest(tsInstance1, seed, seed, true, OM_G31R01.Enums.Combination.TCH);
    	setTestStepEnd();

    	setTestStepBegin("Send TS Configuration Request with same configuration to instance: " + tsInstance2);
    	abisHelper.tsConfigRequest(tsInstance2, seed, seed, true, OM_G31R01.Enums.Combination.TCH);
    	setTestStepEnd();

    	setTestStepBegin("Enable AO TS, instance: " + tsInstance1);
    	abisHelper.enableRequest(this.moClassTs, tsInstance1, associatedSoInstance);
    	assertTrue("AO TS  for tsInstance: " + tsInstance1 + " is not ENABLED", momHelper.waitForAbisTsMoState(tsInstance1, "ENABLED", 5));
    	setTestStepEnd();
       	
    	setTestStepBegin("Try to Enable AO TS, instance: " + tsInstance2);
    	OM_G31R01.EnableResult result = abisHelper.enableRequest(this.moClassTs, tsInstance2, associatedSoInstance);
    			
		setSubTestStep("Verify that MO State in Enable Result is DISABLED");
		assertEquals("MO state is not DISABLED", OM_G31R01.Enums.MOState.DISABLED, result.getMOState());
		
		setSubTestStep("Verify that Attribute Identifier in Enable Result is set to Configuration Parameter Error");
		assertEquals("Attribute Identifier is not set to Configuration Parameter Error", 
				0xFFFF,  // Attribute Identifier: Configuration Parameter Error
				result.getAttributeIdentifier().getAttributeIdentifier());
		
		setSubTestStep("Verify that Attribute Identifier Index in Enable Result is set to Time Slot Number DEI");
		assertEquals("Attribute Identifier Index is not set to Time Slot Number DEI", 
				0x7B,  // Time Slot Number DEI 
				result.getAttributeIdentifier().getIndex());
		
	   	setTestStepEnd();
    }
}   
