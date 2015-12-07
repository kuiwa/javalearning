package com.ericsson.msran.test.grat.createdeleteabisip;

import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import se.ericsson.jcat.fw.annotations.Setup;
import se.ericsson.jcat.fw.annotations.Teardown;

import com.ericsson.commonlibrary.ecimcom.exception.GracefulDisconnectFailedException;
import com.ericsson.commonlibrary.ecimcom.exception.NetconfProtocolException;
import com.ericsson.msran.g2.annotations.TestInfo;
import com.ericsson.msran.test.grat.testhelpers.AbiscoConnection;
import com.ericsson.msran.test.grat.testhelpers.MomHelper;
import com.ericsson.msran.test.grat.testhelpers.NodeStatusHelper;
import com.ericsson.msran.jcat.TestBase;

/**
 * @id NodeUC457, NodeUC458
 *  
 * @name CreateDeleteAbisIpTest
 * 
 * @author GRAT 2014
 * 
 * @created 2014-03-17
 *
 * @description Create and delete the AbisIp MO.
 * 
 * @revision ewegans 2014-03-17 first version
 * @revision eeritau 2014-06-05 Added TC for Delete AbisIp with existing TRX MO (UC458.E2)
 * 
 */
public class CreateDeleteAbisIpTest extends TestBase {
    private MomHelper momHelper;
    private AbiscoConnection abisco;
    private NodeStatusHelper nodeStatus;    
    private static String connectionName = "host_0";
    private static String connectionNameEmpty = "";
    private static String bscIpAddress = "";
 
       
    /**
     * Description of test case for test reporting
     */
    @TestInfo(
            tcId = "NodeUC457,NodeUC458",
            slogan = "Create AbisIp MO - Delete AbisIp MO",
            requirementDocument = "-",
            requirementRevision = "-",
            requirementLinkTested = "-",
            requirementLinkLatest = "-",
            requirementIds = { "-" },
            verificationStatement = "Verifies NodeUC457.N, NodeUC458.N, NodeUC458.E1, NodeUC458.E2",
            testDescription = "Verifies create and delete of the AbisIp MO",
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
        bscIpAddress = abisco.getBscIpAddress();
        setTestStepEnd();
    }
    
    /**
     * Postcheck.
     */
    @Teardown
    public void teardown() {
        setTestStepBegin("Tear it down!");
        nodeStatus.isNodeRunning();
        setTestStepEnd();
    }
    
    /**
     * @name createDeleteAbisIp
     * 
     * @description Create and delete AbisIP MO.
     * 
     * @param testId - unique identifier of the test case
     * @param description
     *      
     */ 
    @Test(timeOut = 340000)
    @Parameters({ "testId", "description" })
    public void createDeleteAbisIp(String testId, String description) {
        setTestCase(testId, description);
        
    	if (!momHelper.isBtsFunctionCreated()) {
    		setTestInfo("Precondition: Create BtsFunction MO");
    		momHelper.createBtsFunctionMo(MomHelper.BTS_FUNCTION_LDN);
    	}        

        setTestStepBegin("Pre-condition: Create GsmSector MO");
        momHelper.createGsmSectorMos(1);
        setTestStepEnd();

        setTestStepBegin("Create AbisIp MO");
        String abisIpLdn = momHelper.createTnAndAbisIpMo(connectionName, bscIpAddress, false);
        setTestStepEnd();

        setTestStepBegin("Verify that AbisIp MO was created");
        assertTrue(momHelper.checkMoExist(abisIpLdn));
        setTestStepEnd();
        
        // Added by wp3794 NodeUC457.N
        setTestStepBegin("Verify that wp3794 set connectionName to last20charOfgsmSectorId when value is empty");
        assertTrue(momHelper.checkgsmSectorName(connectionName));
        setTestStepEnd();    
       
        setTestStepBegin("Verify that operationState=DISABLED, availabilityStatus=[OFFLINE] and peerIpAddress is empty");
        assertEquals("AbisIp MO attributes did not reach expected values", "", momHelper.checkAbisIpMoAttributeAfterLock(abisIpLdn, 5));
        setTestStepEnd();

        /*
         * Delete MO
         */
        setTestStepBegin("Delete AbisIp MO");
        momHelper.deleteMo(abisIpLdn);
        setTestStepEnd();
        setTestStepBegin("Verify that MO was deleted");
        assertFalse(momHelper.checkMoExist(abisIpLdn));
        setTestStepEnd();
    }

    /**
     * @name createDeleteAbisIpWithConnectionNameIsEmpty
     * 
     * @description Create and delete AbisIP MO.
     * 
     * @param testId - unique identifier of the test case
     * @param description
     *      
     */ 
    @Test(timeOut = 340000)
    @Parameters({ "testId", "description" })
    public void createDeleteAbisIpWithConnectionNameIsEmpty(String testId, String description) {
        setTestCase(testId, description);
        
    	if (!momHelper.isBtsFunctionCreated()) {
    		setTestInfo("Precondition: Create BtsFunction MO");
    		momHelper.createBtsFunctionMo(MomHelper.BTS_FUNCTION_LDN);
    	}

        setTestStepBegin("Pre-condition: Create GsmSector MO");
        momHelper.createGsmSectorMos(1);
        setTestStepEnd();

        setTestStepBegin("Create AbisIp MO");
        String abisIpLdn = momHelper.createTnAndAbisIpMo(connectionNameEmpty, bscIpAddress, false);
        setTestStepEnd();

        setTestStepBegin("Verify that AbisIp MO was created");
        assertTrue("MO ("+ abisIpLdn + ") doesn't exist", momHelper.checkMoExist(abisIpLdn));
        setTestStepEnd();

        // Added by wp3794 NodeUC457.N
        setTestStepBegin("Verify that wp3794 set connectionName to last20charOfgsmSectorId");
        assertTrue("The expected value (connectionNameEmpty) was not found", momHelper.checkgsmSectorName(connectionNameEmpty));      
        setTestStepEnd();    

        setTestStepBegin("Verify that operationState=DISABLED, availabilityStatus=[OFFLINE] and peerIpAddress is empty");
        assertEquals("AbisIp MO attributes did not reach expected values", "", momHelper.checkAbisIpMoAttributeAfterLock(abisIpLdn, 5));
        setTestStepEnd();

        /*
         * Delete MO
         */
        setTestStepBegin("Delete AbisIp MO");
        momHelper.deleteMo(abisIpLdn);
        setTestStepEnd();
        setTestStepBegin("Verify that MO was deleted");
        assertFalse("MO ("+ abisIpLdn + ") exists", momHelper.checkMoExist(abisIpLdn));
        setTestStepEnd();
    }

    /**
     * @name deleteAbisIpFails
     * 
     * @description Deletion of unlocked AbisIp MO fails.
     * 
     * @param testId - unique identifier of the test case
     * @param description
     *      
     */ 
    @Test(timeOut = 340000)
    @Parameters({ "testId", "description" })
    public void deleteAbisIpFails(String testId, String description) {
        setTestCase(testId, description);
        
    	if (!momHelper.isBtsFunctionCreated()) {
    		setTestInfo("Precondition: Create BtsFunction MO");
    		momHelper.createBtsFunctionMo(MomHelper.BTS_FUNCTION_LDN);
    	}

        setTestStepBegin("Pre-condition: Create AbisIp MO");
        String abisIpLdn = momHelper.createSectorAndAbisIpMo(connectionName, bscIpAddress, false);
        setTestStepEnd();

        setTestStepBegin("Unlock AbisIp MO");
        momHelper.unlockMo(abisIpLdn);
        setTestStepEnd();
        
        setTestStepBegin("Try to delete MO");
        try {
            momHelper.deleteMo(abisIpLdn);
        } catch (GracefulDisconnectFailedException e) {
            // Expected
        }
        setTestStepEnd();
        
        setTestStepBegin("Verify that AbisIp MO was not deleted");
        assertTrue("MO ("+ abisIpLdn + ") doesn't exist", momHelper.checkMoExist(abisIpLdn));
        setTestStepEnd();
    }

    /**
     * @name createAbisIpFails
     * 
     * @description Creation of AbisIp MO fails when ipv4AddressRef
     *              refers to non-existing MO.
     * 
     * @param testId - unique identifier of the test case
     * @param description
     *      
     */ 
    @Test(timeOut = 340000)
    @Parameters({ "testId", "description" })
    public void createAbisIpFails(String testId, String description) {
        setTestCase(testId, description);
        
    	if (!momHelper.isBtsFunctionCreated()) {
    		setTestInfo("Precondition: Create BtsFunction MO");
    		momHelper.createBtsFunctionMo(MomHelper.BTS_FUNCTION_LDN);
    	}
        
        setTestStepBegin("Pre-condition: Create GsmSector MO");
        String sectorLdn = momHelper.createGsmSectorMos(1).get(0);
        setTestStepEnd();
        
        String abisIpLdn = sectorLdn + ",AbisIp=1";
 
        setTestStepBegin("Create AbisIp MO, the ipv4Address is refering to a non existing AddressIpV4 MO instance");
        
        try {
        	momHelper.createAbisIpMoOnly(connectionName, bscIpAddress, abisIpLdn);
        	fail("AbisIp MO was successfully created");
        } catch (GracefulDisconnectFailedException e) {
            if (!e.getLocalizedMessage().contains("error-description: Transaction commit failed, [The requested operation failed. (sa_ais_err_failed_operation)Attribute 'ipv4Address' in object: 'ManagedElement=1,BtsFunction=1,GsmSector=1,AbisIp=1' is a reference to non existent object: 'ManagedElement=1,Transport=1,Router=1,InterfaceIPv4=GSM,AddressIPv4=GSM']"))
        	{
        		fail("The caught excption did not contain the expected error. Caught exception: " + e.getLocalizedMessage());
        	}      	        	
          	setTestInfo("Got the expected exception when trying to create AbisIP MO with non existing ipv4Address: " + abisIpLdn);
        }
        setTestStepEnd();
    
        setTestStepBegin("Verify that MO was not created: " + abisIpLdn);
        assertFalse("MO ("+ abisIpLdn + ") exist", momHelper.checkMoExist(abisIpLdn));
        setTestInfo("The MO was not created: " + abisIpLdn);
        setTestStepEnd();       


        setTestStepBegin("Create AbisIp MO, the ipv4Address is refering to the GsmSector MO, i.e. it is refering to an existing MO of the wrong type");
        
        try {
        	momHelper.createAbisIpMoOnly(connectionName, bscIpAddress, abisIpLdn, sectorLdn, MomHelper.LOCKED);
        	fail("AbisIp MO was successfully created");
        } catch (NetconfProtocolException e) {
        	if (!e.getLocalizedMessage().contains("error-description: The reference refers to the wrong type of class. Class in model information='AddressIPv4' class in the supplied DN='GsmSector'."))
        	{
        		fail("The caught excption did not contain the expected error. Caught exception: " + e.getLocalizedMessage());
        	}
        	setTestInfo("Got the expected exception when trying to create AbisIp MO sector with non existing ipv4Address: " + abisIpLdn);
        }
        setTestStepEnd();
    
        setTestStepBegin("Verify that MO was not created: " + abisIpLdn);
        assertFalse("MO ("+ abisIpLdn + ") exists", momHelper.checkMoExist(abisIpLdn));
        setTestInfo("The MO was not created: " + abisIpLdn);
        setTestStepEnd();
    }
}
