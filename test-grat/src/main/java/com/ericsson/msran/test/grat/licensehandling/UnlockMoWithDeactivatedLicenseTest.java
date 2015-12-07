//not used for the moment

//package com.ericsson.msran.test.grat.licensehandling;
//
//import java.util.HashMap;
//import java.util.Map;
//
//import org.json.JSONException;
//import org.testng.annotations.Parameters;
//import org.testng.annotations.Test;
//
//import se.ericsson.jcat.fw.annotations.Setup;
//import se.ericsson.jcat.fw.annotations.Teardown;
//
//import com.ericsson.commonlibrary.restorestack.RestoreCommand;
//import com.ericsson.msran.g2.annotations.TestInfo;
//import com.ericsson.msran.helpers.Helpers;
//
//import com.ericsson.msran.test.grat.testhelpers.AbiscoConnection;
//import com.ericsson.msran.test.grat.testhelpers.AlarmHelper;
//import com.ericsson.msran.test.grat.testhelpers.MomHelper;
//import com.ericsson.msran.test.grat.testhelpers.NodeStatusHelper;
//import com.ericsson.msran.test.grat.testhelpers.prepost.PidPrePost;
//import com.ericsson.msran.test.grat.testhelpers.restorecommands.ActivateLicenseCommand;
//
//import com.ericsson.msran.jcat.TestBase;
//
///**
// * @id NodeUC426,NodeUC428
// * 
// * @name UnlockMoWithDeactivatedLicenseTest
// * 
// * @author GRAT Cell
// * 
// * @created 2015-09-16
// * 
// * @description This test will create and unlock AbisIp and Trx MOs
// *              without an operable license, and verify correct behaviour
// * 
// * 
// */
//
//public class UnlockMoWithDeactivatedLicenseTest extends TestBase {
//    private String sectorLdn;
//    private String abisIpLdn;
//    private String trxLdn;
//    
//    private MomHelper momHelper;
//    private PidPrePost pidPrePost;
//    private AbiscoConnection abisco;
//    private NodeStatusHelper nodeStatus;
//    private AlarmHelper alarmHelper;
//
//    /**
//     * Description of test case for test reporting
//     */
//    @TestInfo(
//            tcId = "",
//            slogan = "Unlock GRAT MOs with a deactivated license",
//            requirementDocument = "1/00651-FCP 130 1402",
//            requirementRevision = "PC5",
//            requirementLinkTested = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff87c1d941?docno=1/00651-FCP1301402Uen&format=excel8book",
//            requirementLinkLatest = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff86a2af3d?docno=1/00651-FCP1301402Uen&action=current&format=excel8book",
//            requirementIds = { "10565-0334/19417[A][APPR]" },
//            verificationStatement = "Verifies NodeUC426.A9, NodeUC459.A4",
//            testDescription = "Verifies that AbisIp and Trx MOs reach the correct state when unlocked with a deactivated GSM license",
//            traceGuidelines = "N/A")
//    
//    /**
//     * Precheck.
//     */
//    @Setup
//    public void setup() {
//        nodeStatus = new NodeStatusHelper();
//        assertTrue("Node did not reach a working state before test start", nodeStatus.waitForNodeReady());
//        momHelper = new MomHelper();
//        pidPrePost = new PidPrePost();
//        abisco = new AbiscoConnection();
//        alarmHelper = new AlarmHelper();
//
//        setTestInfo("Save current pid to compare after test execution.");
//        pidPrePost.preCond();
//        
//        setTestInfo("Precondition: Create GsmSector MO");
//        sectorLdn = momHelper.createGsmSectorMos(1).get(0);
//        
//        setTestInfo("Precondition for Trx creation: Create AbisIp MO");
//        abisIpLdn = momHelper.createTnAndAbisIpMo(AbiscoConnection.getConnectionName(), abisco.getBscIpAddress(), false);
//        
//        setTestInfo("Precondition: Create Trx MO");
//        trxLdn = momHelper.createTrxMo(sectorLdn, "1");
//    }
//    
//    /**
//     * Postcheck.
//     */
//    @Teardown
//    public void teardown() {
//        nodeStatus.isNodeRunning();
//        pidPrePost.postCond();
//    }
//    
//    /**
//     * @name unlockAbisIpTrxWithDeactivatedLicense
//     * 
//     * @description Unlock AbisIP and TRX MOs with a deactivated license
//     * 
//     * @param testId - unique identifier of the test case
//     * @param description
//     */
//    @Test(timeOut = 500000)
//    @Parameters({ "testId", "description" })
//    public void unlockAbisIpTrxWithDeactivatedLicense(String testId, String description) throws JSONException {
//        
//        setTestCase(testId, description);
//        
//        momHelper.setAttributeForMoAndCommit(MomHelper.GRAT_FEATURESTATE_LDN, "featureState", "DEACTIVATED");
//        RestoreCommand restoreActiveLicense = new ActivateLicenseCommand(MomHelper.GRAT_FEATURESTATE_LDN);
//        Helpers.restore().restoreStackHelper().getRestoreStack().add(restoreActiveLicense);
//        Map<String, String> expectedFeatureStateMoState = new HashMap<>();
//        expectedFeatureStateMoState.put("serviceState", "INOPERABLE");
//        expectedFeatureStateMoState.put("featureState", "DEACTIVATED");
//        assertEquals("FeatureState MO did not reach the expected state", "", momHelper.waitForMoAttributes(MomHelper.GRAT_FEATURESTATE_LDN, expectedFeatureStateMoState, 5));
//        
//        setTestStepBegin("Unlock AbisIp and verify that it reaches the correct MO state");
//        momHelper.unlockMo(abisIpLdn);
//        Map<String, String> expectedUnlockedAbisIpState = new HashMap<>();
//        expectedUnlockedAbisIpState.put("administrativeState", "UNLOCKED");
//        expectedUnlockedAbisIpState.put("operationalState", "DISABLED");
//        expectedUnlockedAbisIpState.put("availabilityStatus", "FAILED");
//        expectedUnlockedAbisIpState.put("peerIpAddress", null);
//        assertEquals("AbisIp MO did not reach the expected state", "", momHelper.waitForMoAttributes(abisIpLdn, expectedUnlockedAbisIpState, 5));
//        assertTrue("AbisIp has not raised a \"Configuration Requires Feature Activation\" alarm",
//                    alarmHelper.isFeatureActivationAlarmRaised(abisIpLdn));    
//        
//        setTestStepEnd();
//        
//        setTestStepBegin("Lock AbisIp and verify that alarm disappears");
//        momHelper.lockMo(abisIpLdn);
//        assertEquals("Locked AbisIp did not reach the expected state", "", momHelper.checkAbisIpMoAttributeAfterLock(abisIpLdn, 5));        
//        assertFalse("AbisIp still has a \"Configuration Requires Feature Activation\" alarm",
//                     alarmHelper.isFeatureActivationAlarmRaised(abisIpLdn));
//        setTestStepEnd();
//        
//        setTestStepBegin("Unlock Trx and verify that it reaches the correct MO state");
//        momHelper.unlockMo(trxLdn);
//
//        Map<String, String> expectedUnlockedTrxState = new HashMap<>();
//        expectedUnlockedTrxState.put("administrativeState", "UNLOCKED");
//        expectedUnlockedTrxState.put("operationalState", "DISABLED");
//        expectedUnlockedTrxState.put("availabilityStatus", "FAILED");
//        expectedUnlockedTrxState.put("abisRxState", null);
//        expectedUnlockedTrxState.put("abisTrxcState", null);
//        expectedUnlockedTrxState.put("abisTxState", null);
//        expectedUnlockedTrxState.put("abisTsState", null);
//        //TODO: enable when UC is implemented
//        //assertEquals("Trx MO did not reach the expected state", "", momHelper.waitForMoAttributes(trxMoLdn, expectedUnlockedTrxState, 5));
//        //assertTrue("Trx MO has not raised a \"Configuration Requires Feature Activation\" alarm",
//        //            alarmHelper.isFeatureActivationAlarmRaised(trxIpLdn));  
//        
//        setTestStepEnd();
//
//        setTestStepBegin("Lock Trx and verify that alarm disappears");
//        momHelper.lockMo(trxLdn);
//        assertEquals("Locked Trx did not reach the expected state", "", momHelper.checkTrxMoAttributeAfterLock(trxLdn, 5));
//        assertFalse("Trx still has a \"Configuration Requires Feature Activation\" alarm",
//                     alarmHelper.isFeatureActivationAlarmRaised(trxLdn));
//        setTestStepEnd();
//    }
//      
//}
