package com.ericsson.msran.test.grat.shutdowntrx;

import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import se.ericsson.jcat.fw.annotations.Setup;
import se.ericsson.jcat.fw.annotations.Teardown;

import com.ericsson.commonlibrary.ecimcom.exception.GracefulDisconnectFailedException;

import com.ericsson.msran.g2.annotations.TestInfo;

import com.ericsson.msran.test.grat.testhelpers.AbiscoConnection;
import com.ericsson.msran.test.grat.testhelpers.MomHelper;
import com.ericsson.msran.test.grat.testhelpers.NodeStatusHelper;
import com.ericsson.msran.jcat.TestBase;

/**
 * @id NodeUC427
 *
 * @name ShutDownTrxTest
 * 
 * @author uabmoda
 * 
 * @created 2013-07-05
 * 
 * @revision xasahuh 2014-02-07 Set timeout for test case to 1 minute.
 * @revision xasahuh 2014-02-10 Added TestInfo metadata.
 * @revision xasahuh 2014-02-17 Removed builder pattern structure and use Restore Stack.
 */

public class ShutDownTrxTest extends TestBase {

    private String sectorMoLdn, trxMoLdn;
    private MomHelper momHelper;
    private NodeStatusHelper nodeStatus;

    /**
     * Description of test case for test reporting
     */
    @TestInfo(
            tcId = "NodeUC427",
            slogan = "Shut Down TRX",
            requirementDocument = "1/00651-FCP 130 1402",
            requirementRevision = "PC5",
            requirementLinkTested = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff87c1d941?docno=1/00651-FCP1301402Uen&format=excel8book",
            requirementLinkLatest = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff86a2af3d?docno=1/00651-FCP1301402Uen&action=current&format=excel8book",
            requirementIds = { "10565-0334/19417[A][APPR]" },
            verificationStatement = "Verifies NodeUC427.N",
            testDescription = "Verifies that an attempt to change administrative state to SHUTTING DOWN does not change the administrative state",
            traceGuidelines = "N/A")

    
    /**
     * Precheck.
     */
    @Setup
    public void setup() {
    	setTestStepBegin("Setup");
        nodeStatus = new NodeStatusHelper();
        assertTrue("Node did not reach a working state before test start", nodeStatus.waitForNodeReady());
        
        momHelper = new MomHelper();
        
    	if (!momHelper.isBtsFunctionCreated()) {
    		setTestInfo("Precondition: Create BtsFunction MO");
    		momHelper.createBtsFunctionMo(MomHelper.BTS_FUNCTION_LDN);
    	}
    
        setTestInfo("Precondition: Create GsmSector MO");
        sectorMoLdn = momHelper.createGsmSectorMos(1).get(0);
    
        setTestInfo("Precondition for Trx creation: Create AbisIp MO");
        momHelper.createAbisIpMo(AbiscoConnection.getConnectionName(), momHelper.randomPrivateIp(),false);

        setTestInfo("Precondition: Create Trx MO");
        trxMoLdn = momHelper.createTrxMo(sectorMoLdn, "1");
        setTestStepEnd();
    }
    
    /**
     * Postcheck.
     */
    @Teardown
    public void teardown() {
    	setTestStepBegin("Teardown");
        nodeStatus.isNodeRunning();
        setTestStepEnd();
    }
    
    /**
     * @name shutdownTrxTest
     * @name shutdownTrxTest
     * @description Setting TRX MO administrativeState to SHUTTINGDOWN shall fail. The administrativeState
     *              shall be unchanged after the attempt.
     *
     * @param testId - unique identifier of the test case
     * @param description
     */
    @Test(timeOut = 360000)
    @Parameters({ "testId", "description" })
    public void shutdownTrxTest(String testId, String description) {
        
        setTestCase(testId, description);
        
        setTestStepBegin("Verify that Trx MO is LOCKED");    
        assertTrue("Trx MO is not LOCKED", momHelper.waitForMoAttributeStringValue(trxMoLdn, "administrativeState", "LOCKED", 5));
            
        setTestStepBegin("Try to shutdown the MO " + trxMoLdn);
        try {
            momHelper.setAttributeForMoAndCommit(trxMoLdn, "administrativeState", "SHUTTINGDOWN");
        } catch (GracefulDisconnectFailedException e) {
        	try {
                if (!e.getCause().getMessage().contains("Transaction commit failed")) {
                    fail("Transaction didn't fail with expected message. Faulty message: " + e);
                }
    	    } catch (NullPointerException npe) {
    	    	setTestInfo("Transaction failed with NULL message. " + npe);
            }
        }
            
        setTestStepBegin("Verify that Trx MO is still LOCKED");    
        assertTrue("Trx MO is not LOCKED", momHelper.waitForMoAttributeStringValue(trxMoLdn, "administrativeState", "LOCKED", 5));
    }
}
