package com.ericsson.msran.test.grat.createdeletebtsfunction;

import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import se.ericsson.jcat.fw.annotations.Setup;
import se.ericsson.jcat.fw.annotations.Teardown;

import com.ericsson.commonlibrary.ecimcom.exception.GracefulDisconnectFailedException;
import com.ericsson.commonlibrary.managedobjects.ManagedObject;
import com.ericsson.msran.g2.annotations.TestInfo;
import com.ericsson.msran.test.grat.testhelpers.AbiscoConnection;
import com.ericsson.msran.test.grat.testhelpers.MomHelper;
import com.ericsson.msran.jcat.TestBase;

/**
 * @id NodeUC729, NodeUC730
 *  
 * @name CreateDeleteBtsFunctionTest
 * 
 * @author GRAT 2014
 * 
 * @created 2014-03-17
 * 
 * @description Create and delete the BtsFunction MO.
 * 
 * @revision efrenil 2014-12-19 first version
 * 
 */
public class CreateDeleteBtsFunctionTest extends TestBase {
    private MomHelper momHelper;
       
    /**
     * Description of test case for test reporting
     */
    @TestInfo(
            tcId = "NodeUC729.N, NodeUC729.E1 NodeUC730.N, NodeUC730.E1",
            slogan = "Create BtsFunction MO - Delete BtsFunction MO",
            requirementDocument = "-",
            requirementRevision = "-",
            requirementLinkTested = "-",
            requirementLinkLatest = "-",
            requirementIds = { "-" },
            verificationStatement = "Verifies NodeUC729.N, NodeUC729.E1 NodeUC730.N, NodeUC730.E1",
            testDescription = "Verifies create and delete of the BtsFunction MO",
            traceGuidelines = "N/A")
    
    /**
     * Precheck.
     */
    @Setup
    public void setup() {
        setTestStepBegin("Setup");
        momHelper = new MomHelper();
        setTestStepEnd();
    }
    
    /**
     * Postcheck.
     */
    @Teardown
    public void teardown() {
        setTestStepBegin("Tear it down!");
        setTestStepEnd();
    }
    
    /**
     * @name createDeleteBtsFunction
     * 
     * @description Create and delete BtsFunction MO. NodeUC729.N, NodeUC729.E1, NodeUC730.N, and NodeUC730.E1
     * 
     * @param testId - unique identifier of the test case
     * @param description
     *      
     */ 
    @Test(timeOut = 340000)
    @Parameters({ "testId", "description" })
    public void createDeleteBtsFunction(String testId, String description) {
        setTestCase(testId, description);

        setTestStepBegin("Pre-condition: Delete BtsFunction MO if it exist");
        if (momHelper.checkMoExist(MomHelper.BTS_FUNCTION_LDN))
        {
        	setTestInfo(MomHelper.BTS_FUNCTION_LDN + " exist. Try to delete BtsFunction MO");
        	momHelper.deleteMo(MomHelper.BTS_FUNCTION_LDN);
        }
        setTestStepEnd();

        setTestStepBegin("Pre-condition: Check that BtsFunction MO does not exist");
        assertFalse("MO ("+ MomHelper.BTS_FUNCTION_LDN + ") exists", momHelper.checkMoExist(MomHelper.BTS_FUNCTION_LDN)); 
        setTestStepEnd();
        
        setTestStepBegin("Create BtsFunction MO fail, NodeUC729.E1");
        ManagedObject btsFunc = momHelper.buildBtsFunctionMo(MomHelper.BTS_FUNCTION_LDN);
        try {
        	momHelper.createManagedObject(btsFunc);
        	fail("Unexpectedly managed to create BtsFunction MO");
        } catch (GracefulDisconnectFailedException e) {
          	if (!e.getLocalizedMessage().contains("error-description: Transaction commit failed, [The requested operation failed. (sa_ais_err_failed_operation)Not allowed to create the BtsFunction MO: ManagedElement=1,BtsFunction=1]"))
        	{
        		fail("The caught exception did not contain the expected error. Caught exception: " + e.getLocalizedMessage());
        	}
        	setTestInfo("Got the expected exception when trying to create BtsFunction without the correct userLabel");
        }
        setTestStepEnd();
        
        setTestStepBegin("Create BtsFunction MO sucess, NodeUC729.N");
        btsFunc = momHelper.buildBtsFunctionMo(MomHelper.BTS_FUNCTION_LDN, MomHelper.BTS_USER_LABEL_VALUE);
    	momHelper.createManagedObject(btsFunc);
    	assertTrue("MO ("+ MomHelper.BTS_FUNCTION_LDN + ") doesn't exist", momHelper.checkMoExist(MomHelper.BTS_FUNCTION_LDN));
    	assertEquals("BtsFunction MO attributes did not reach expected values", "", momHelper.checkBtsFunctionMoAttributesAfterCreation(MomHelper.BTS_FUNCTION_LDN, 5));
        setTestStepEnd();

        setTestStepBegin("Delete BtsFunction MO fail, NodeUC730.E1");
        
        AbiscoConnection abisco = new AbiscoConnection();
        abisco.setupAbisco(false);

        if (!momHelper.isTnConfigured())
        	momHelper.configureTn(abisco.getBscIpAddress());
        momHelper.createGsmSectorMos(1).get(0);
        String abisIpMoLdn = momHelper.createAbisIpMo(AbiscoConnection.getConnectionName(), abisco.getBscIpAddress(), false);
        momHelper.unlockMo(abisIpMoLdn);
        setTestInfo("Delete BtsFunction MO (child MO:s are unlocked) - delete shall fail");
        try {
            momHelper.deleteMo(MomHelper.BTS_FUNCTION_LDN);
            fail("Could unexpectedly delete the BtsdFunction MO");
        } catch (GracefulDisconnectFailedException e) {
        	try {
        	    if (!e.getCause().getMessage().contains("Transaction commit failed")) {
                    fail("Transaction didn't fail with expected message. Faulty message: " + e);
                }
        	} catch (NullPointerException npe) {
        		setTestInfo("Transaction failed with NULL message. " + npe);
            }
        }
        setTestInfo("Verify that the BtsFunction MO still exists");
        assertTrue("Expected BtsFunction MO to exist", momHelper.checkMoExist(MomHelper.BTS_FUNCTION_LDN));        
        setTestStepEnd();
        
        setTestStepBegin("Delete BtsFunction MO, NodeUC730.N");
        momHelper.lockMo(abisIpMoLdn);
        momHelper.deleteMo(MomHelper.BTS_FUNCTION_LDN);
        setTestStepEnd();
        setTestStepBegin("Verify that MO was deleted");
        assertFalse("MO ("+ MomHelper.BTS_FUNCTION_LDN + ") exists", momHelper.checkMoExist(MomHelper.BTS_FUNCTION_LDN));
        setTestStepEnd();
    }
}
