package com.ericsson.msran.test.grat.lockunlocktrx;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import se.ericsson.jcat.fw.annotations.Setup;
import se.ericsson.jcat.fw.annotations.Teardown;

import com.ericsson.commonlibrary.managedobjects.ManagedObjectAttribute;
import com.ericsson.commonlibrary.managedobjects.ManagedObjectValueAttribute;
import com.ericsson.msran.g2.annotations.TestInfo;

import com.ericsson.msran.test.grat.testhelpers.AbiscoConnection;
import com.ericsson.msran.test.grat.testhelpers.MomHelper;
import com.ericsson.msran.test.grat.testhelpers.NodeStatusHelper;

import com.ericsson.msran.jcat.TestBase;

/**
 * @id NodeUC426,NodeUC428
 * 
 * @name LockUnlockTrxTest
 * 
 * @author Roadrunner
 * 
 * @created 2013-06-26
 * 
 * @description This test will create MO GsmSector and Trx (if not already
 *              created). Then it will lock MO Trx and make sure that the
 *              operational state and availability status is as expected.
 *              Finally the test case will unlock MO Trx and once again check
 *              the operational state and availability status.
 * 
 * @revision eraweer 2013-06-26 first version
 * @revision xasahuh 2014-02-07 Set timeout for test case to 1 minute.
 * @revision xasahuh 2014-02-10 Added TestInfo metadata.
 * @revision xasahuh 2014-02-17 Removed builder pattern structure and use Restore Stack.
 * @revision xasahuh 2014-02-24 Moved methods here from classes that verifies the same usecases.
 * 
 */

public class LockUnlockTrxTest extends TestBase {
    private String sectorMoLdn;
    private String abisMoLdn;
    private String trxMoLdn;
    
    private MomHelper momHelper;
    private AbiscoConnection abisco;
    private NodeStatusHelper nodeStatus;    

    /**
     * Description of test case for test reporting
     */
    @TestInfo(
            tcId = "NodeUC426,NodeUC428",
            slogan = "Unlock TRX - Lock TRX",
            requirementDocument = "1/00651-FCP 130 1402",
            requirementRevision = "PC5",
            requirementLinkTested = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff87c1d941?docno=1/00651-FCP1301402Uen&format=excel8book",
            requirementLinkLatest = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff86a2af3d?docno=1/00651-FCP1301402Uen&action=current&format=excel8book",
            requirementIds = { "10565-0334/19417[A][APPR]" },
            verificationStatement = "Verifies NodeUC426.N, NodeUC428.N, NodeUC426.A1 and NodeUC428.A1",
            testDescription = "Verifies that Trx MO can be created, and that it can be set to locked and unlocked state.",
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
        abisco = new AbiscoConnection();

    	if (!momHelper.isBtsFunctionCreated()) {
    		setTestInfo("Precondition: Create BtsFunction MO");
    		momHelper.createBtsFunctionMo(MomHelper.BTS_FUNCTION_LDN);
    	}
        
        setTestInfo("Precondition: Create GsmSector MO");
        sectorMoLdn = momHelper.createGsmSectorMos(1).get(0);
        
        setTestInfo("Precondition for Trx creation: Create AbisIp MO");
        abisMoLdn = momHelper.createTnAndAbisIpMo(AbiscoConnection.getConnectionName(), abisco.getBscIpAddress(), false);

        setTestInfo("Precondition: Unlock AbisIp MO");
        momHelper.unlockMo(abisMoLdn);
        
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
     * @name lockUnlockTrx
     * 
     * @description Unlock and lock Trx MO
     * 
     * @param testId - unique identifier of the test case
     * @param description
     */
    @Test(timeOut = 500000)
    @Parameters({ "testId", "description" })
    public void lockUnlockTrx(String testId, String description) throws JSONException {
        
        setTestCase(testId, description);
        
        // Unlock Trx
        setTestStepBegin("Test Unlock MO: " + trxMoLdn);
        momHelper.unlockMo(trxMoLdn);
        // Check that it is unlocked
        assertEquals("Trx MO attributes did not reach expected values after unlock", "", momHelper.checkTrxMoAttributeAfterUnlockNoLinks(trxMoLdn, 15));
        setTestStepEnd();

        // Lock Trx
        setTestStepBegin("Test Lock MO: " + trxMoLdn);
        momHelper.lockMo(trxMoLdn);
        // Check that it is locked
        assertEquals("Trx MO attributes did not reach expected values after lock", "", momHelper.checkTrxMoAttributeAfterLock(trxMoLdn, 5));
        setTestStepEnd();
    }
    
    /**
     * @name lockAlreadyLockedTrx
     * 
     * @description Lock an already locked TRX MO
     * @param testId - unique identifier of the test case
     * @param description
     */
    @Test(timeOut = 360000)
    @Parameters({ "testId", "description" })
    public void lockAlreadyLockedTrx(String testId, String description) {
        
        setTestCase(testId, description);
        
        setTestStepBegin("Verify that Trx MO is locked");
        assertEquals("Trx MO attributes did not reach expected values after lock", "", momHelper.checkTrxMoAttributeAfterLock(trxMoLdn, 5));
        setTestStepEnd();
        
        setTestStepBegin("Set administrativeState to LOCKED once more");
        momHelper.setAttributeForMoAndCommit(trxMoLdn, "administrativeState", "LOCKED");
        assertEquals("Trx MO attributes did not reach expected values after another lock", "", momHelper.checkTrxMoAttributeAfterLock(trxMoLdn, 5));
        setTestStepEnd();
    }

    /**
     * @name unlockedAlreadyUnlockedTrx
     * @description Unlock an already unlocked TRX MO
     * @param testId - unique identifier of the test case
     * @param description
     */
    @Test(timeOut = 360000)
    @Parameters({ "testId", "description" })
    public void unlockAlreadyUnlockedTrx(String testId, String description) {
        
        setTestCase(testId, description);
        
        setTestStepBegin("Set administrativeState to UNLOCKED");
        momHelper.setAttributeForMoAndCommit(trxMoLdn, "administrativeState", "UNLOCKED");
        setTestStepEnd();

        setTestStepBegin("Verify that Trx MO is unlocked");
        assertEquals("Trx MO attributes did not reach expected values after unlock", "", momHelper.checkTrxMoAttributeAfterUnlockNoLinks(trxMoLdn, 15));
        setTestStepEnd();
        
        setTestStepBegin("Set administrativeState to UNLOCKED once more");
        momHelper.setAttributeForMoAndCommit(trxMoLdn, "administrativeState", "UNLOCKED");
        assertEquals("Trx MO attributes did not reach expected values after unlock", "", momHelper.checkTrxMoAttributeAfterUnlockNoLinks(trxMoLdn, 5));
        setTestStepEnd();
    }
    
    /**
     * @name UnlockLockDeleteTrx 
     * 
     * @description Unlock then lock,delete Trx MO in same transaction
     * 
     * @param testId - unique identifier of the test case
     * @param description
     */
    @Test(timeOut = 500000)
    @Parameters({ "testId", "description" })
    public void lockDeleteTrx(String testId, String description) {
        
        setTestCase(testId, description);
        
        // Unlock Trx
        setTestStepBegin("Test Unlock MO: " + trxMoLdn);
        momHelper.unlockMo(trxMoLdn);
        assertEquals("Trx MO attributes did not reach expected values after unlock", "", momHelper.checkTrxMoAttributeAfterUnlockNoLinks(trxMoLdn, 15));
        setTestStepEnd();
        
        // Lock and delete TRX in same transaction
        setTestStepBegin("Test Lock and Delete MO: " + trxMoLdn);
        List<ManagedObjectAttribute> attributes = new ArrayList<ManagedObjectAttribute>();
        attributes.add(new ManagedObjectValueAttribute(MomHelper.ADMINISTRATIVE_STATE, MomHelper.LOCKED));
        momHelper.setAttributesAndDeleteMoInSameTx(trxMoLdn, attributes);        
        setTestStepEnd();
        
        setTestStepBegin("Verify that MO was deleted");
        assertFalse(momHelper.checkMoExist(trxMoLdn));
        setTestStepEnd();
    }
      
}
