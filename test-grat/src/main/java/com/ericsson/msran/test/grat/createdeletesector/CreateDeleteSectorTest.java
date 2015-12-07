package com.ericsson.msran.test.grat.createdeletesector;

import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import se.ericsson.jcat.fw.annotations.Setup;
import se.ericsson.jcat.fw.annotations.Teardown;

import com.ericsson.msran.jcat.TestBase;
import com.ericsson.commonlibrary.ecimcom.exception.GracefulDisconnectFailedException;
import com.ericsson.msran.g2.annotations.TestInfo;
import com.ericsson.msran.test.grat.testhelpers.AbiscoConnection;
import com.ericsson.msran.test.grat.testhelpers.MomHelper;
import com.ericsson.msran.test.grat.testhelpers.NodeStatusHelper;

/**
 * @id NodeUC419, NodeUC420
 *  
 * @name CreateDeleteSectorTest
 * 
 * @author Roadrunner
 * 
 * @created 2013-09-20
 * 
 * @description Create and delete the GsmSector MO.
 * 
 * @revision Roadrunner 2013-09-20 first version
 * @revision xasahuh 2014-02-07 Set timeout for test case to 1 minute.
 * @revision xasahuh 2014-02-10 Added TestInfo metadata.
 * @revision xasahuh 2014-02-12 Removed builder pattern structure and use Restore Stack.
 * @revision xasahuh 2014-02-24 Renamed from CreateDeleteMoTest, and moved methods
 *                              here that verifies the same usecases.
 * 
 */

public class CreateDeleteSectorTest extends TestBase {
    private MomHelper momHelper;
    private AbiscoConnection abisco;
    private NodeStatusHelper nodeStatus;
    
    /**
     * Description of test case for test reporting
     */
    @TestInfo(
            tcId = "NodeUC419,NodeUC420",
            slogan = "Create GSM Sector - Delete GSM Sector",
            requirementDocument = "1/00651-FCP 130 1402",
            requirementRevision = "PC5",
            requirementLinkTested = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff87c1d941?docno=1/00651-FCP1301402Uen&format=excel8book",
            requirementLinkLatest = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff86a2af3d?docno=1/00651-FCP1301402Uen&action=current&format=excel8book",
            requirementIds = { "10565-0334/19417[A][APPR]" },
            verificationStatement = "Verifies NodeUC419.N, NodeUC419.E1, NodeUC420.N and NodeUC420.E1",
            testDescription = "Verifies create and delete of the GsmSector MO",
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
        abisco.setupAbisco(false);
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
     * @name createDeleteSector
     * 
     * @description Create and delete GsmSector MO.
     * 
     * @param testId - unique identifier of the test case
     * @param description
     *      
     */ 
    @Test(timeOut = 340000)
    @Parameters({ "testId", "description" })

    public void createDeleteSector(String testId, String description) {
        
		setTestCase(testId, description);
        
        /*
         * Create MO
         */
		
    	if (!momHelper.isBtsFunctionCreated()) {
    		setTestInfo("Precondition: Create BtsFunction MO");
    		momHelper.createBtsFunctionMo(MomHelper.BTS_FUNCTION_LDN);
    	}       

        setTestStepBegin("Create GsmSector MO");
        String sectorLdn = momHelper.createGsmSectorMos(1).get(0);
        setTestStepEnd();
        
        setTestStepBegin("Verify that MO was created");
        assertTrue("MO (" + sectorLdn +") doesn't exist", momHelper.checkMoExist(sectorLdn));
        setTestStepEnd();

        setTestStepBegin("Verify GsmSector attributes: abisScfOmlState = DOWN, abisAtState = RESET, abisScfState = RESET, " +
                         "abisTfState = RESET, abisTfMode = UNDEFINED and abisClusterGroupId = null");
        assertEquals("GsmSector MO attributes did not reach expected values", "", momHelper.checkGsmSectorMoAttributeAfterCreateNoLinks(sectorLdn, 5));
        
        /*
         * Delete MO
         */
        setTestStepBegin("Delete GsmSector MO");
        momHelper.deleteMo(sectorLdn);
        setTestStepEnd();
        
        setTestStepBegin("Verify that MO was deleted");
        assertFalse("MO (" + sectorLdn +") exists", momHelper.checkMoExist(sectorLdn));
        setTestStepEnd();       
    }
    
    /**
     * @name deleteSectorWithLockedChildren
     * 
     * @description Delete GsmSector MO. The child MO:s (Trx and AbisIp) are also deleted in the same
     *              transaction. Note that the child MO:s must be locked.
     *              Verifies NodeUC420.N.
     *
     * @param testId - unique identifier of the test case
     * @param description 
     */
    @Test(timeOut = 340000)
    @Parameters({ "testId", "description" })
    public void deleteSectorWithLockedChildren(String testId, String description) {
        
        setTestCase(testId, description);
        
    	if (!momHelper.isBtsFunctionCreated()) {
    		setTestInfo("Precondition: Create BtsFunction MO");
    		momHelper.createBtsFunctionMo(MomHelper.BTS_FUNCTION_LDN);
    	}       
        
        setTestInfo("Precondition: Create GsmSector MO");
        String sectorMoLdn = momHelper.createGsmSectorMos(1).get(0);
        
        setTestInfo("Precondition: Create AbisIp MO");
        momHelper.createAbisIpMo(AbiscoConnection.getConnectionName(), abisco.getBscIpAddress(), false);
        
        setTestInfo("Precondition: Create Trx MO");
        momHelper.createTrxMo(sectorMoLdn, "1");
        
        setTestStepBegin("Delete GsmSector MO (child MO:s are locked)");
        momHelper.deleteMo(sectorMoLdn);
        
        setTestStepBegin("Verify that MO was deleted");
        assertFalse("MO (" + sectorMoLdn +") exists", momHelper.checkMoExist(sectorMoLdn));
    }
    
    /**
     * @name deleteSectorWithChildrenFails
     * 
     * @description Delete of GsmSector MO with unlocked child MO:s shall fail,
     *              according to NodeUC420.E1
     * 
     * @param testId - unique identifier of the test case
     * @param description
     */
    @Test(timeOut = 390000)
    @Parameters({ "testId", "description" })
    public void deleteSectorWithChildrenFails(String testId, String description) {
        
        setTestCase(testId, description);
        
    	if (!momHelper.isBtsFunctionCreated()) {
    		setTestInfo("Precondition: Create BtsFunction MO");
    		momHelper.createBtsFunctionMo(MomHelper.BTS_FUNCTION_LDN);
    	}       
        
        setTestInfo("Precondition: Create GsmSector MO");
        String sectorMoLdn = momHelper.createGsmSectorMos(1).get(0);

        setTestInfo("Precondition: Create AbisIp MO");
        String abisIpMoLdn = momHelper.createAbisIpMo(AbiscoConnection.getConnectionName(), abisco.getBscIpAddress(), false);
        
        setTestInfo("Precondition: Trx and AbisIp MO:s are unlocked");
        
        setTestInfo("Precondition: Create Trx MO");
        String trxMoLdn = momHelper.createTrxMo(sectorMoLdn, "1");

        momHelper.unlockMo(trxMoLdn);
        momHelper.unlockMo(abisIpMoLdn);

        setTestStepBegin("Delete GsmSector MO (child MO:s are unlocked) - delete shall fail");
        
        boolean deleteOk = true;
        try {
            momHelper.deleteMo(sectorMoLdn);
        } catch (GracefulDisconnectFailedException e) {
            deleteOk = false;
        	try {
        	    if (!e.getCause().getMessage().contains("Transaction commit failed")) {
                    fail("Transaction didn't fail with expected message. Faulty message: " + e);
                }
        	} catch (NullPointerException npe) {
        		setTestInfo("Transaction failed with NULL message. " + npe);
            }
        }
        assertFalse("Delete GsmSector MO did not fail as expected", deleteOk);
        
        setTestStepBegin("Verify that the GsmSector MO still exists");
        assertTrue("Expected GsmSector MO to exist", momHelper.checkMoExist(sectorMoLdn));
    }
}
    