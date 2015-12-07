package com.ericsson.msran.test.grat.synch;

import java.util.concurrent.TimeUnit;

import org.json.JSONException;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import se.ericsson.jcat.fw.annotations.Setup;
import se.ericsson.jcat.fw.annotations.Teardown;

import com.ericsson.abisco.clientlib.servers.BG.G31TSsStatusUpdate;
import com.ericsson.abisco.clientlib.servers.OM_G31R01;
import com.ericsson.abisco.clientlib.servers.BG.G31StatusUpdate;
import com.ericsson.abisco.clientlib.servers.OM_G31R01.Enums;
import com.ericsson.abisco.clientlib.servers.OM_G31R01.FSOffset;
import com.ericsson.msran.g2.annotations.TestInfo;
import com.ericsson.msran.jcat.TestBase;
import com.ericsson.msran.test.grat.testhelpers.AbisHelper;
import com.ericsson.msran.test.grat.testhelpers.MomHelper;
import com.ericsson.msran.test.grat.testhelpers.NodeStatusHelper;
import com.ericsson.msran.test.grat.testhelpers.prepost.AbisPrePost;

/**
 * @id TBD
 * 
 * @name StopSynchTest
 * 
 * @created 2015-10-26
 * 
 * @description This test class verifies the Stop Synchronization functionality
 * 
 * @revision eraweer 2015-10-26 first version 
 *           See Git for version history.
 */

public class StopSynchTest extends TestBase {
    private AbisHelper abisHelper;
    private MomHelper momHelper;
    private NodeStatusHelper nodeStatus;
    private AbisPrePost abisPrePost;
    
    /**
     * Description of test case for test reporting
     */
    @TestInfo(
            tcId = "NodeUC511.N",
            slogan = "Verify Stop Synch functionality",
            requirementDocument = "1/00651-FCP 130 1402",
            requirementRevision = "PC5",
            requirementLinkTested = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff87c1d941?docno=1/00651-FCP1301402Uen&format=excel8book",
            requirementLinkLatest = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff86a2af3d?docno=1/00651-FCP1301402Uen&action=current&format=excel8book",
            requirementIds = { "10565-0334/21767[PA1][PREL]", "10565-0334/19034[A][APPR]",
                    "10565-0334/19417[A][APPR]" },
            verificationStatement = "Verifies NodeUC511.N",
            testDescription = "Verify stop synchronization",
            traceGuidelines = "N/A")

    
    /**
     * Preconditions
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
        abisPrePost = new AbisPrePost();
        momHelper = new MomHelper();
        setTestStepEnd();
    }
    
    /**
     * Postconditions
     */
    @Teardown
    public void teardown() {
    	setTestStepBegin("Teardown");
        nodeStatus.isNodeRunning();
        setTestStepEnd();
    }
    
    /**
     * enableAoTf
     * @throws InterruptedException 
     */
    private void enableAoTf(boolean config) throws InterruptedException {
    	
    	if(config) {    		
    		setTestInfo("Configure AoTf");
            OM_G31R01.TFConfigurationResult configRes = abisHelper.tfConfigRequest(1, Enums.TFMode.Standalone, new FSOffset(new Integer(0xFF), new Long(0xFFFFFFFFL)));
            assertEquals(OM_G31R01.Enums.MOClass.TF, configRes.getMOClass());
    	}
    	
    	setTestInfo("Enable AoTf");
    	abisHelper.enableRequest(OM_G31R01.Enums.MOClass.TF, 0);
        assertTrue("abisTfState is not ENABLED", momHelper.waitForMoAttributeStringValue(MomHelper.SECTOR_LDN, "abisTfState", "ENABLED", 6));
    }

    /**
     * disableAoTf
     * @throws InterruptedException 
     */
    private void disableAoTf() throws InterruptedException {
    	
       	abisHelper.disableRequest(OM_G31R01.Enums.MOClass.TF);
        assertTrue("abisTfState is not DISABLED", momHelper.waitForMoAttributeStringValue(MomHelper.SECTOR_LDN, "abisTfState", "DISABLED", 6));	
    }

    /**
     * enableAoTx
     * @throws InterruptedException 
     */
    private void enableAoTx(boolean config) throws InterruptedException {    	

    	assertTrue("abisTxMoState is not DISABLED", momHelper.waitForAbisTxMoState("DISABLED"));

    	if(config) {
    		setTestInfo("Configure AoTx");
    		OM_G31R01.TXConfigurationResult confRes = abisHelper.txConfigRequest(0, momHelper.getArfcnToUse(), false, 27, false);
    		assertEquals("According Indication must be According to Request",
    				Enums.AccordanceIndication.AccordingToRequest,
    				confRes.getAccordanceIndication().getAccordanceIndication());
    	}
    	
        setTestInfo("Enable AoTx");
        abisHelper.enableRequest(OM_G31R01.Enums.MOClass.TX, 0);
        assertTrue("abisTxMoState is not ENABLED", momHelper.waitForAbisTxMoState("ENABLED"));
    }
    
    /**
     * enableAoRx
     * @throws InterruptedException 
     */
    private void enableAoRx(boolean config) throws InterruptedException {

    	assertTrue("abisRxMoState is not DISABLED", momHelper.waitForAbisRxMoState("DISABLED"));

    	if(config) {
    		setTestInfo("Configure AoRx");
    		OM_G31R01.RXConfigurationResult confRes = abisHelper.rxConfigRequest(0, momHelper.getArfcnToUse(), false, "");
    		assertEquals("According Indication must be According to Request",
    				Enums.AccordanceIndication.AccordingToRequest,
    				confRes.getAccordanceIndication().getAccordanceIndication());
    	}
    	
        setTestInfo("Enable AoRx");
        abisHelper.enableRequest(OM_G31R01.Enums.MOClass.RX, 0);
        assertTrue("abisRxMoState is not ENABLED", momHelper.waitForAbisRxMoState("ENABLED"));
    }
    
    /**
     * enableAllAoTs
     * @throws InterruptedException 
     * @throws JSONException 
     */
    private void enableAllAoTs(boolean config) throws InterruptedException, JSONException {
    	
    	int associatedSoInstance = 0;
    	
    	for (int tsInstance = 0; tsInstance < 8; tsInstance++) { 
    		assertTrue("abisTsMoState for tsInstance " + tsInstance + " is not DISABLED", momHelper.waitForAbisTsMoState(tsInstance, "DISABLED", 5));

        	if(config) {
                setTestInfo("Configure AoTs");
        		OM_G31R01.TSConfigurationResult confRes = abisHelper.tsConfigRequest(tsInstance, tsInstance, tsInstance, false, OM_G31R01.Enums.Combination.TCH);
        		assertEquals("AccordanceIndication is not AccordingToRequest", 
        				OM_G31R01.Enums.AccordanceIndication.AccordingToRequest,
        				confRes.getAccordanceIndication().getAccordanceIndication());
        	}
        	
            setTestInfo("Enable AoTs");
    		abisHelper.enableRequest(OM_G31R01.Enums.MOClass.TS, tsInstance, associatedSoInstance);
    		assertTrue("abisTsMoState for tsInstance " + tsInstance + " is not ENABLED", momHelper.waitForAbisTsMoState(tsInstance, "ENABLED", 5));
    	}
    }
    
    /**
     * checkAllAos
     * @throws JSONException 
     */
    private void checkAllAos(String expectedOpState) throws JSONException {
    	
    	assertTrue("abisTxMoState is not " + expectedOpState, momHelper.waitForAbisTxMoState(expectedOpState));
        assertTrue("abisRxMoState is not " + expectedOpState, momHelper.waitForAbisRxMoState(expectedOpState));
    	
        for(int tsInstance = 0; tsInstance < 8; tsInstance++) {        
        	assertTrue("abisTsMoState for tsInstance " + tsInstance + " is not " + expectedOpState , momHelper.waitForAbisTsMoState(tsInstance, expectedOpState, 5));
        }
    }
    
    /**
     * receiveStatusUpdates
     * @throws InterruptedException 
     * 
     */
    private void receiveStatusUpdates() throws InterruptedException {
    	
    	// Receive status updates from all enabled Ao:s
    	boolean aoTxStatusReceived = false;
    	boolean aoRxStatusReceived = false;
    	boolean aoTsStatusReceived = false;

    	// AoTx & AoRx
    	for(int i=0; i < 2; ++i) {
    		
    		G31StatusUpdate statusUpdate = abisHelper.getStatusUpdate(8, TimeUnit.SECONDS);    		
    		assertTrue(statusUpdate != null);
    		
    		switch(statusUpdate.getMOClass()) {
    		
    			case TX:
    				setTestInfo("Received statusUpdate from AoTx");
    				aoTxStatusReceived = true;
    				break;
    				
    			case RX:
    				setTestInfo("Received statusUpdate from AoRx");
    				aoRxStatusReceived = true;
    				break;
    				
    			default:
    				setTestWarning("Received statusUpdate from unexpected MOClass: " + statusUpdate.getMOClass().toString());
    		}
    	}
    	
    	// AoTs
    	G31TSsStatusUpdate tsStatusUpdate = abisHelper.getTsStatusUpdate(8, TimeUnit.SECONDS);
    	assertTrue(tsStatusUpdate != null);
    	setTestInfo("Received statusUpdate from AoTs");
    	aoTsStatusReceived = true;
    	
    	assertTrue(aoTxStatusReceived && aoRxStatusReceived && aoTsStatusReceived);
    }
    
    /**
     * @name stopSynchTest
     * 
     * @description Verifies stop synchronization functionality.
     *  
     * @param testId - unique identifier
     * @param testDescription
     * 
     * @throws InterruptedException
     * @throws JSONException 
     */
    @Test (timeOut = 3600000)
    @Parameters({ "testId", "testDescription" })
    public void stopSynchTest(String testId, String testDescription) throws InterruptedException, JSONException {
        
        setTestCase(testId, testDescription);
        
        // Setup TestCase, create and enable all Mo:s & Ao:s
        setTestStepBegin("Setup TestCase");
        
        // Setup test preconditions
        abisPrePost.preCondAllMoStateStarted();
                
        // Configure & Enable TF
        enableAoTf(true);
        
        // Configure & Enable AoTx
        enableAoTx(true);
        
        // Configure & Enable AoRx
        enableAoRx(true);
        
        // Configure & Enable All AoTs
        enableAllAoTs(true);
        
        // Clear status update
        abisHelper.clearStatusUpdate();
        
        // Setup Completed
        setTestStepEnd();
        
        // Perform Test
        setTestStepBegin("Perform Test");        
        
        // Disable synch (TF)
        disableAoTf();
                
        // Receive status updates from enabled Ao:s
        receiveStatusUpdates();
        
        // Check that all Ao:s are disabled
        checkAllAos("DISABLED");
        
        // Verify that all Ao:s can be enabled again 
        
        // Enable synch (TF)
        enableAoTf(false);

        // Enable AoTx
        enableAoTx(false);
        
        // Enable AoRx
        enableAoRx(false);
        
        // Enable All AoTs
        enableAllAoTs(false);
        
    	// Check that everything is ok (Ao:s & Mo:s)
        checkAllAos("ENABLED");
        
        // Test completed
        setTestStepEnd();
    }
}
