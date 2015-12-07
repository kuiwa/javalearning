package com.ericsson.msran.test.grat.lockunlocktrx;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import se.ericsson.jcat.fw.annotations.Setup;
import se.ericsson.jcat.fw.annotations.Teardown;

import com.ericsson.abisco.clientlib.servers.OM_G31R01;
import com.ericsson.commonlibrary.managedobjects.ManagedObject;
import com.ericsson.commonlibrary.managedobjects.ManagedObjectValueAttribute;
import com.ericsson.commonlibrary.restorestack.RestoreCommand;
import com.ericsson.msran.g2.annotations.TestInfo;
import com.ericsson.msran.test.grat.testhelpers.AbisHelper;
import com.ericsson.msran.test.grat.testhelpers.AbiscoConnection;
import com.ericsson.msran.test.grat.testhelpers.MomHelper;
import com.ericsson.msran.test.grat.testhelpers.NodeStatusHelper;
import com.ericsson.msran.test.grat.testhelpers.restorecommands.DeleteMoCommand;
import com.ericsson.msran.test.grat.testhelpers.restorecommands.LockDeleteMoCommand;
import com.ericsson.msran.jcat.TestBase;

/**
 * @id NodeUC426,NodeUC428
 * 
 * @name CreateDeleteUnlockLockMoCombinationsTest
 * 
 * @author GRAT 2014
 * 
 * @created 2014-08-21
 * 
 * @description This test will create, delete, unlock and lock GsmSector, AbisIp and Trx MOs in different combinations.

 * 				Then it will ask Abisco to try to establish links and unlock
 * 				the Trx. The test will then verify that Trx OML and RSL links
 * 				are	established and that the Trx MO attributes are updated
 * 				correctly. This is followed by a lock and verification that
 * 				it went well.
 * 
 * @revision efrenil 2014-08-21 first version
 * 
 */

public class CreateDeleteUnlockLockMoCombinationsTest extends TestBase {   
    private MomHelper momHelper;
    private AbiscoConnection abisco;
    private NodeStatusHelper nodeStatus;
    private AbisHelper abisHelper;
    
    // we use tg=0 as default
    private static final int tg_0 = 0;

    /**
     * Description of test case for test reporting
     */
    @TestInfo(
            tcId = "UC419.N, UC420.N, UC424.N, UC425.N, UC426.A4, UC428.N, UC457.N, UC458.N , UC459.N, UC461.N",
            slogan = "Create, Unlock, Lock, and Delete of GsmSector, AbisIp, and Trx in different ways",
            requirementDocument = "1/00651-FCP 130 1402",
            requirementRevision = "PC5",
            requirementLinkTested = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff87c1d941?docno=1/00651-FCP1301402Uen&format=excel8book",
            requirementLinkLatest = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff86a2af3d?docno=1/00651-FCP1301402Uen&action=current&format=excel8book",
            requirementIds = { "10565-0334/19417[A][APPR]" },
            verificationStatement = "Partially verifies (not checking that every MO state is correct after every step) UC419.N, UC420.N, UC424.N, UC425.N, UC426.A4, UC428.N, UC457.N, UC458.N , UC459.N, UC461.N",
            testDescription = "Verifies that several MO operations on the GsmSector, AbisIp, and Trx MOs can be combined in the same transaction.",
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
        abisHelper = new AbisHelper();
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
    
    void checkGsmSectorMoAttributeAfterCreateNoLinks() {
    	assertEquals("GsmSector MO attributes did not reach expected values", "", momHelper.checkGsmSectorMoAttributeAfterCreateNoLinks(MomHelper.SECTOR_LDN, 10));
    }
    
    void checkGsmSectorMoAttributeAfterCreateLinksEstablishedAndStartAllMos() {
    	assertEquals("GsmSector MO attributes did not reach expected values", "", momHelper.checkGsmSectorMoAttributeAfterAllMosStarted(MomHelper.SECTOR_LDN, 10));
    }
    
    void checkGsmSectorMoAttributeAfterCreateLinksEstablishedAndStartAllMosAtEnabled() {
    	assertEquals("GsmSector MO attributes did not reach expected values", "", momHelper.checkGsmSectorMoAttributeAfterAllMosStartedAoAtEnabled(MomHelper.SECTOR_LDN, 10));
    }
    
    void checkAbisIpMoAttributeAfterCreateUnlockBscConnected() {
    	assertEquals("AbisIp MO attributes did not reach expected values", "", momHelper.checkAbisIpMoAttributeAfterUnlockBscConnected(MomHelper.ABIS_IP_LDN, abisco.getBscIpAddress(), 30));
    }
    
    void checkTrxMoAttributeAfterCreateUnlockNoLinks() {
    	assertEquals("Trx MO attributes did not reach expected values", "", momHelper.checkTrxMoAttributeAfterUnlockNoLinks(MomHelper.TRX_LDN, 10));
    }
    
    void checkTrxMoAttributeAfterCreateUnlockLinksEstablished() {
    	assertEquals("Trx MO attributes did not reach expected values", "", momHelper.checkTrxMoAttributeAfterUnlockLinksEstablished(MomHelper.TRX_LDN, 10));
    }
       
    void checkThatGsmSectorAbisIpTrxMoAreDeleted() {
    	assertFalse(momHelper.checkMoExist(MomHelper.TRX_LDN));
    	assertFalse(momHelper.checkMoExist(MomHelper.SECTOR_LDN));
    	assertFalse(momHelper.checkMoExist(MomHelper.ABIS_IP_LDN));  	
    }
    
    void startAllMoVisibleInGsmSector() throws InterruptedException {
    	abisHelper.startRequest(OM_G31R01.Enums.MOClass.SCF, 0);
    	abisHelper.startRequest(OM_G31R01.Enums.MOClass.TF, 0); 
    	abisHelper.startRequest(OM_G31R01.Enums.MOClass.AT, 0);
    }
    
    void sendAoAtConfigPreDefBundling() throws InterruptedException {
    	AbisHelper.TransportProfile tp = new AbisHelper.TransportProfile();
    	tp.createPredefinedBundlingProfiles();       	
    	abisHelper.atConfigRequest(tp, 0);
    }

	
    /**
     * createUnlockAllMoInOneTxLockDeleteAllMoInOneTx_1
     * @description Create GsmSector, AbisIp (as UNLOCKED), and Trx (as UNLOCKED) in one transaction
     *              See that all links can be established
     *              Delete Trx (and LOCK), AbisIp (and LOCK), and GsmSector in one transaction
     *              This TC does not completely verify any UC, but the following UCs are involved: 
     *              UC419.N, UC420.N, UC424.N, UC425.N, UC426.A4, UC428.N, UC457.N, UC458.N , UC459.N, UC461.N
     * 
     * @param testId - unique identifier of the test case
     * @param description
     */
    @Test(timeOut = 850000)
    @Parameters({ "testId", "description" })
    public void createUnlockAllMoInOneTxLockDeleteAllMoInOneTx_1(String testId, String description) throws InterruptedException {
        setTestCase(testId, description);
        
        setTestStepBegin("TN");
        if (!momHelper.isTnConfigured())
        	momHelper.configureTn(abisco.getBscIpAddress());
        
    	if (!momHelper.isBtsFunctionCreated()) {
    		setTestInfo("Precondition: Create BtsFunction MO");
    		momHelper.createBtsFunctionMo(MomHelper.BTS_FUNCTION_LDN);
    	}        
               
        // Run the same test a couple of times to see that we clean up
        // create GsmSector, AbisIp (as UNLOCKED), and Trx (as UNLOCKED) in one transaction
        // see that all links can be established
        // delete Trx (and LOCK), AbisIp (and LOCK), and GsmSector in one transaction
        for (int i = 0 ; i < 3 ; ++i)
        {
        	// make sure there are no old AT Bundling Infos in queue
        	abisHelper.clearAtBundlingInfoUpdateQueue();
        	
        	// specify which MOs that shall be created together, and their parameter values
        	List<ManagedObject> createMos = new ArrayList<ManagedObject>();

        	setTestStepBegin("Build MO objects");
        	ManagedObject gsmSectorMo = momHelper.buildGsmSectorMo(MomHelper.SECTOR_LDN);
        	createMos.add(gsmSectorMo);
        	RestoreCommand restoreGsmSectorMoCmd = new DeleteMoCommand(gsmSectorMo.getLocalDistinguishedName());
        	momHelper.addRestoreCmd(restoreGsmSectorMoCmd);

        	ManagedObject abisIpMo = momHelper.buildAbisIpMo(
        			AbiscoConnection.getConnectionName(),
        			abisco.getBscIpAddress(),
        			MomHelper.ABIS_IP_LDN,
        			MomHelper.TNA_IP_LDN,
        			MomHelper.UNLOCKED);
        	createMos.add(abisIpMo);
        	RestoreCommand restoreAbisIpMoCmd = new LockDeleteMoCommand(abisIpMo.getLocalDistinguishedName());
        	momHelper.addRestoreCmd(restoreAbisIpMoCmd);

        	ManagedObject trxMo =  momHelper.buildTrxMo(MomHelper.TRX_LDN, MomHelper.UNLOCKED, momHelper.getSectorEquipmentFunctionLdn());
        	createMos.add(trxMo);
        	RestoreCommand restoreTrxMoCmd = new LockDeleteMoCommand(trxMo.getLocalDistinguishedName());
        	momHelper.addRestoreCmd(restoreTrxMoCmd);

        	setTestStepBegin("Create all MOs");
        	momHelper.createSeveralMoInOneTx(createMos);
 
          	setTestStepBegin("Check post-conditions: Check MO status of GsmSector, AbisIp, and Trx after MO creation and unlock");
        	checkGsmSectorMoAttributeAfterCreateNoLinks();
          	checkAbisIpMoAttributeAfterCreateUnlockBscConnected();
        	checkTrxMoAttributeAfterCreateUnlockNoLinks();
        	      
         	// Establish Oml link for Scf
        	setTestStepBegin("Establish OML link for Scf");
        	abisco.establishLinks();
        	
        	setTestStepBegin("Start SO SCF, AO TF, and AO AT");
        	startAllMoVisibleInGsmSector();

        	setTestStepBegin("Check MO status of GsmSector, AbisIp, and Trx after link establishment and start of SO SCF, AO TF, and AO AT");
        	checkGsmSectorMoAttributeAfterCreateLinksEstablishedAndStartAllMos();
          	checkAbisIpMoAttributeAfterCreateUnlockBscConnected();
          	checkTrxMoAttributeAfterCreateUnlockNoLinks();
        	       	
        	setTestStepBegin("Send AO AT Config and AO AT Enable");
        	sendAoAtConfigPreDefBundling();
        	assertTrue("Configuration signature has not been calculated (value = 0) ", 0 != abisHelper.enableRequest(OM_G31R01.Enums.MOClass.AT, 0).getConfigurationSignature().intValue());
            
        	setTestStepBegin("Establish OML+RSL for Trx, check MO state");
            abisco.establishLinks(true);
        	checkGsmSectorMoAttributeAfterCreateLinksEstablishedAndStartAllMosAtEnabled();
          	checkAbisIpMoAttributeAfterCreateUnlockBscConnected();
        	checkTrxMoAttributeAfterCreateUnlockLinksEstablished();
        	
            // Get a AT BundlingInfo Update
            setTestStepBegin("Wait for an AT Bundling Info Update");
        	assertEquals("Did not receive a valid AT Bundling Info Update", "", abisHelper.waitForAtBundlingInfoUpdateWithBundlingData(10, tg_0));
            setTestStepEnd();
        	
        	setTestStepBegin("Lock and delete MOs");

        	// We have a seperate list for the MO:s that shall be deleted so that we can specify the delete order, and which attributes to set
        	List<ManagedObject> deleteMos = new ArrayList<ManagedObject>();

        	ManagedObject delTrx = new ManagedObject(trxMo.getLocalDistinguishedName());
        	delTrx.addAttribute(new ManagedObjectValueAttribute(MomHelper.ADMINISTRATIVE_STATE, MomHelper.LOCKED));
        	deleteMos.add(delTrx);        	 

        	ManagedObject delAbisIp = new ManagedObject(abisIpMo.getLocalDistinguishedName());
        	delAbisIp.addAttribute(new ManagedObjectValueAttribute(MomHelper.ADMINISTRATIVE_STATE, MomHelper.LOCKED));
        	deleteMos.add(delAbisIp);

            ManagedObject delGsmSector = new ManagedObject(gsmSectorMo.getLocalDistinguishedName());
            deleteMos.add(delGsmSector);

        	momHelper.deleteSeveralMoInOneTx(deleteMos);
        	                  	
        	// There is no guarantee that the lock Trx will be executed before lock AbisIp, so we
        	// do not wait for any AT BundlingInfo Update or Status Update from SCF since the SCF OML 
            // link is is closed when we lock the AbisIp MO before the TRX MO.        	        	
        	
        	
        	setTestStepBegin("Verify that MOs were deleted");
        	checkThatGsmSectorAbisIpTrxMoAreDeleted();

        	setTestStepBegin("Remove MOs from restore stack, since they were deleted successfully");
        	momHelper.removeRestoreCmd(restoreTrxMoCmd);
        	momHelper.removeRestoreCmd(restoreAbisIpMoCmd);
        	momHelper.removeRestoreCmd(restoreGsmSectorMoCmd);
        }
        setTestInfo("Test is done!");
    }
    
    /**
     * createUnlockAllMoInOneTxLockDeleteAllMoInOneTx_2
     * @description Create GsmSector, Trx (as UNLOCKED), and AbisIp (as UNLOCKED) in one transaction
     *              See that all links can be established
     *              Delete AbisIp (and LOCK), Trx (and LOCK), and GsmSector in one transaction
     *              This TC does not completely verify any UC, but the following UCs are involved: 
     *              UC419.N, UC420.N, UC424.N, UC425.N, UC426.A4, UC428.N, UC457.N, UC458.N , UC459.N, UC461.N
     * 
     * @param testId - unique identifier of the test case
     * @param description
     */    
    @Test(timeOut = 850000)
    @Parameters({ "testId", "description" })
    public void createUnlockAllMoInOneTxLockDeleteAllMoInOneTx_2(String testId, String description) throws InterruptedException {
        setTestCase(testId, description);
        
        setTestStepBegin("TN");
        if (!momHelper.isTnConfigured())
        	momHelper.configureTn(abisco.getBscIpAddress());
        
    	if (!momHelper.isBtsFunctionCreated()) {
    		setTestInfo("Precondition: Create BtsFunction MO");
    		momHelper.createBtsFunctionMo(MomHelper.BTS_FUNCTION_LDN);
    	}
        
        // we use tg=0 as default
        int tg_0 = 0;
        
        // Run the same test a couple of times to see that we clean up
        // create GsmSector, Trx (as UNLOCKED), and AbisIp (as UNLOCKED) in one transaction
        // see that all links can be established
        // delete AbisIp (and LOCK), Trx (and LOCK), and GsmSector in one transaction
        for (int i = 0 ; i < 3 ; ++i)
        {
        	// make sure there are no old AT Bundling Infos in queue
        	abisHelper.clearAtBundlingInfoUpdateQueue();
        	
        	// Specify which MOs that shall be created together, and their parameter values
        	List<ManagedObject> createMos = new ArrayList<ManagedObject>();

        	setTestStepBegin("Build MO objects");
        	ManagedObject gsmSectorMo = momHelper.buildGsmSectorMo(MomHelper.SECTOR_LDN);
        	createMos.add(gsmSectorMo);
        	RestoreCommand restoreGsmSectorMoCmd = new DeleteMoCommand(gsmSectorMo.getLocalDistinguishedName());
        	momHelper.addRestoreCmd(restoreGsmSectorMoCmd);

        	ManagedObject trxMo =  momHelper.buildTrxMo(MomHelper.TRX_LDN, MomHelper.UNLOCKED, momHelper.getSectorEquipmentFunctionLdn());
        	createMos.add(trxMo);
        	RestoreCommand restoreTrxMoCmd = new LockDeleteMoCommand(trxMo.getLocalDistinguishedName());
        	momHelper.addRestoreCmd(restoreTrxMoCmd);
        	
        	ManagedObject abisIpMo = momHelper.buildAbisIpMo(
        			AbiscoConnection.getConnectionName(),
        			abisco.getBscIpAddress(),
        			MomHelper.ABIS_IP_LDN,
        			MomHelper.TNA_IP_LDN,
        			MomHelper.UNLOCKED);
        	createMos.add(abisIpMo);
        	RestoreCommand restoreAbisIpMoCmd = new LockDeleteMoCommand(abisIpMo.getLocalDistinguishedName());
        	momHelper.addRestoreCmd(restoreAbisIpMoCmd);


        	setTestStepBegin("Create all MOs");
        	momHelper.createSeveralMoInOneTx(createMos);
        	
          	setTestStepBegin("Check post-conditions: Check MO status of GsmSector, AbisIp, and Trx after MO creation and unlock");
        	checkGsmSectorMoAttributeAfterCreateNoLinks();
          	checkAbisIpMoAttributeAfterCreateUnlockBscConnected();
        	checkTrxMoAttributeAfterCreateUnlockNoLinks();

            // Establish Oml link for Scf
            setTestStepBegin("Establish OML link for Scf");
            abisco.establishLinks();
            
            setTestStepBegin("Start SO SCF, AO TF, and AO AT");
            startAllMoVisibleInGsmSector();

            setTestStepBegin("Check MO status of GsmSector, AbisIp, and Trx after link establishment and start of SO SCF, AO TF, and AO AT");
            checkGsmSectorMoAttributeAfterCreateLinksEstablishedAndStartAllMos();
            checkAbisIpMoAttributeAfterCreateUnlockBscConnected();
            checkTrxMoAttributeAfterCreateUnlockNoLinks();
                    
            setTestStepBegin("Send AO AT Config and AO AT Enable");
            sendAoAtConfigPreDefBundling();
            assertTrue("Configuration signature has not been calculated (value = 0) ", 0 != abisHelper.enableRequest(OM_G31R01.Enums.MOClass.AT, 0).getConfigurationSignature().intValue());
            
            setTestStepBegin("Establish OML+RSL for Trx, check MO state");
            abisco.establishLinks(true);
            checkGsmSectorMoAttributeAfterCreateLinksEstablishedAndStartAllMosAtEnabled();
            checkAbisIpMoAttributeAfterCreateUnlockBscConnected();
            checkTrxMoAttributeAfterCreateUnlockLinksEstablished();
        	
            // Get a AT BundlingInfo Update
            setTestStepBegin("Wait for an AT Bundling Info Update");
        	assertEquals("Did not receive a valid AT Bundling Info Update", "", abisHelper.waitForAtBundlingInfoUpdateWithBundlingData(10, tg_0));
            setTestStepEnd();
        	
        	setTestStepBegin("Lock and delete MOs");

        	// A list for the MO:s that shall be deleted
        	List<ManagedObject> deleteMos = new ArrayList<ManagedObject>();

        	ManagedObject delAbisIp = new ManagedObject(abisIpMo.getLocalDistinguishedName());
        	delAbisIp.addAttribute(new ManagedObjectValueAttribute(MomHelper.ADMINISTRATIVE_STATE, MomHelper.LOCKED));
        	deleteMos.add(delAbisIp);
        	
        	ManagedObject delTrx = new ManagedObject(trxMo.getLocalDistinguishedName());
        	delTrx.addAttribute(new ManagedObjectValueAttribute(MomHelper.ADMINISTRATIVE_STATE, MomHelper.LOCKED));
        	deleteMos.add(delTrx);
        	
        	ManagedObject delGsmSector = new ManagedObject(gsmSectorMo.getLocalDistinguishedName());
        	deleteMos.add(delGsmSector);

        	momHelper.deleteSeveralMoInOneTx(deleteMos);        	          
     
        	// Do not wait for any AT BundlingInfo Update or Status Update from SCF since the SCF OML 
        	// link is is closed when we lock the AbisIp MO before the TRX MO.
        	
        	setTestStepBegin("Verify that MOs were deleted");
        	checkThatGsmSectorAbisIpTrxMoAreDeleted();

        	setTestStepBegin("Remove MOs from restore stack, since they were deleted successfully");
        	momHelper.removeRestoreCmd(restoreTrxMoCmd);
        	momHelper.removeRestoreCmd(restoreAbisIpMoCmd);
        	momHelper.removeRestoreCmd(restoreGsmSectorMoCmd);
        }
        setTestInfo("Test is done!");
    }
    
    
    /**
     * createUnlockAllMoInOneTxLockDeleteAllMoInOneTx_3
     * @description Create GsmSector, Trx (as UNLOCKED), and AbisIp (as UNLOCKED) in one transaction
     *              See that all links can be established
     *              Lock AbisIp and Trx MOs in the same transaction as we delete the GsmSector MO.
     *              The AbisIp and Trx MOs shall be deleted implicitly by the delete of the GsmSector MO
     *              
     *              This TC does not completely verify any UC, but the following UCs are involved: 
     *              UC419.N, UC420.N, UC424.N, UC425.N, UC426.A4, UC428.N, UC457.N, UC458.N , UC459.N, UC461.N
     * 
     * @param testId - unique identifier of the test case
     * @param description
     */    
    @Test(timeOut = 850000)
    @Parameters({ "testId", "description" })
    public void createUnlockAllMoInOneTxLockDeleteAllMoInOneTx_3(String testId, String description) throws InterruptedException {
        setTestCase(testId, description);
        
        setTestStepBegin("TN");
        if (!momHelper.isTnConfigured())
        	momHelper.configureTn(abisco.getBscIpAddress());
        
    	if (!momHelper.isBtsFunctionCreated()) {
    		setTestInfo("Precondition: Create BtsFunction MO");
    		momHelper.createBtsFunctionMo(MomHelper.BTS_FUNCTION_LDN);
    	}
        
        // Run the same test a couple of times to see that we clean up
        // create GsmSector, Trx (as UNLOCKED), and AbisIp (as UNLOCKED) in one transaction
        // see that all links can be established
        // Lock AbisIp and Trx, and delete GsmSector in one transaction (Abis Ip and Trx will be deleted implicitly)
        for (int i = 0 ; i < 3 ; ++i)
        {
        	// make sure there are no old AT Bundling Infos in queue
        	abisHelper.clearAtBundlingInfoUpdateQueue();
        	
        	// Specify which MOs that shall be created together, and their parameter values
        	List<ManagedObject> createMos = new ArrayList<ManagedObject>();

        	setTestStepBegin("Build MO objects");
        	ManagedObject gsmSectorMo = momHelper.buildGsmSectorMo(MomHelper.SECTOR_LDN);
        	createMos.add(gsmSectorMo);
        	RestoreCommand restoreGsmSectorMoCmd = new DeleteMoCommand(gsmSectorMo.getLocalDistinguishedName());
        	momHelper.addRestoreCmd(restoreGsmSectorMoCmd);

        	ManagedObject trxMo =  momHelper.buildTrxMo(MomHelper.TRX_LDN, MomHelper.UNLOCKED, momHelper.getSectorEquipmentFunctionLdn());
        	createMos.add(trxMo);
        	RestoreCommand restoreTrxMoCmd = new LockDeleteMoCommand(trxMo.getLocalDistinguishedName());
        	momHelper.addRestoreCmd(restoreTrxMoCmd);
        	
        	ManagedObject abisIpMo = momHelper.buildAbisIpMo(
        			AbiscoConnection.getConnectionName(),
        			abisco.getBscIpAddress(),
        			MomHelper.ABIS_IP_LDN,
        			MomHelper.TNA_IP_LDN,
        			MomHelper.UNLOCKED);
        	createMos.add(abisIpMo);
        	RestoreCommand restoreAbisIpMoCmd = new LockDeleteMoCommand(abisIpMo.getLocalDistinguishedName());
        	momHelper.addRestoreCmd(restoreAbisIpMoCmd);

        	setTestStepBegin("Create all MOs");
        	momHelper.createSeveralMoInOneTx(createMos);
        	
          	setTestStepBegin("Check post-conditions: Check MO status of GsmSector, AbisIp, and Trx after MO creation and unlock");
        	checkGsmSectorMoAttributeAfterCreateNoLinks();
          	checkAbisIpMoAttributeAfterCreateUnlockBscConnected();
        	checkTrxMoAttributeAfterCreateUnlockNoLinks();

            // Establish Oml link for Scf
            setTestStepBegin("Establish OML link for Scf");
            abisco.establishLinks();
            
            setTestStepBegin("Start SO SCF, AO TF, and AO AT");
            startAllMoVisibleInGsmSector();

            setTestStepBegin("Check MO status of GsmSector, AbisIp, and Trx after link establishment and start of SO SCF, AO TF, and AO AT");
            checkGsmSectorMoAttributeAfterCreateLinksEstablishedAndStartAllMos();
            checkAbisIpMoAttributeAfterCreateUnlockBscConnected();
            checkTrxMoAttributeAfterCreateUnlockNoLinks();
                    
            setTestStepBegin("Send AO AT Config and AO AT Enable");
            sendAoAtConfigPreDefBundling();
            assertTrue("Configuration signature has not been calculated (value = 0) ", 0 != abisHelper.enableRequest(OM_G31R01.Enums.MOClass.AT, 0).getConfigurationSignature().intValue());
            
            setTestStepBegin("Establish OML+RSL for Trx, check MO state");
            abisco.establishLinks(true);
            checkGsmSectorMoAttributeAfterCreateLinksEstablishedAndStartAllMosAtEnabled();
            checkAbisIpMoAttributeAfterCreateUnlockBscConnected();
            checkTrxMoAttributeAfterCreateUnlockLinksEstablished();
        	
            // Get a AT BundlingInfo Update
            setTestStepBegin("Wait for an AT Bundling Info Update");
        	assertEquals("Did not receive a valid AT Bundling Info Update", "", abisHelper.waitForAtBundlingInfoUpdateWithBundlingData(10, tg_0));
            setTestStepEnd();
        			
        	setTestStepBegin("Lock and delete MOs");

        	// We use separate lists for the MOs that shall be deleted, and for the MOs where we will only
        	// update MO attributes
        	// in this test case it means that we will update lock Trx and AbisIp, but only call delete on the GsmSector MO
        	// the Trx and AbisIp MOs shall be deleted implicitly by the delete of the GsmSector
        	List<ManagedObject> deleteMos = new ArrayList<ManagedObject>();
        	List<ManagedObject> setAttributesInMo = new ArrayList<ManagedObject>();

        	ManagedObject delAbisIp = new ManagedObject(abisIpMo.getLocalDistinguishedName());
        	delAbisIp.addAttribute(new ManagedObjectValueAttribute(MomHelper.ADMINISTRATIVE_STATE, MomHelper.LOCKED));
        	setAttributesInMo.add(delAbisIp);
        	
        	ManagedObject delTrx = new ManagedObject(trxMo.getLocalDistinguishedName());
        	delTrx.addAttribute(new ManagedObjectValueAttribute(MomHelper.ADMINISTRATIVE_STATE, MomHelper.LOCKED));
        	setAttributesInMo.add(delTrx);

            ManagedObject delGsmSector = new ManagedObject(gsmSectorMo.getLocalDistinguishedName());
        	deleteMos.add(delGsmSector);

        	momHelper.deleteSeveralMoInOneTx(deleteMos, setAttributesInMo);
        	        
        	// Do not wait for any AT BundlingInfo Update or Status Update from SCF since the SCF OML 
            // link is is closed when we lock the AbisIp MO before the TRX MO.        	        	
        	             
        	setTestStepBegin("Verify that MOs were deleted");
        	checkThatGsmSectorAbisIpTrxMoAreDeleted();

        	setTestStepBegin("Remove MOs from restore stack, since they were deleted successfully");
        	momHelper.removeRestoreCmd(restoreTrxMoCmd);
        	momHelper.removeRestoreCmd(restoreAbisIpMoCmd);
        	momHelper.removeRestoreCmd(restoreGsmSectorMoCmd);
        }
        setTestInfo("Test is done!");
    }
    
    /**
     * createUnlockAllMoInOneTxLockDeleteAllMoInOneTx_4
     * @description Create GsmSector, Trx (as UNLOCKED), and AbisIp (as UNLOCKED) in one transaction
     *              See that all links can be established
     *              Lock Trx and AbisIp MOs in the same transaction as we delete the GsmSector MO.
     *              The AbisIp and Trx MOs shall be deleted implicitly by the delete of the GsmSector MO
     *              
     *              This TC does not completely verify any UC, but the following UCs are involved: 
     *              UC419.N, UC420.N, UC424.N, UC425.N, UC426.A4, UC428.N, UC457.N, UC458.N , UC459.N, UC461.N
     * 
     * @param testId - unique identifier of the test case
     * @param description
     */    
    @Test(timeOut = 850000)
    @Parameters({ "testId", "description" })
    public void createUnlockAllMoInOneTxLockDeleteAllMoInOneTx_4(String testId, String description) throws InterruptedException {
        setTestCase(testId, description);
        
        setTestStepBegin("TN");
        if (!momHelper.isTnConfigured())
        	momHelper.configureTn(abisco.getBscIpAddress());
        
    	if (!momHelper.isBtsFunctionCreated()) {
    		setTestInfo("Precondition: Create BtsFunction MO");
    		momHelper.createBtsFunctionMo(MomHelper.BTS_FUNCTION_LDN);
    	}
        
        // Run the same test a couple of times to see that we clean up
        // create GsmSector, Trx (as UNLOCKED), and AbisIp (as UNLOCKED) in one transaction
        // see that all links can be established
        // Lock Trx and AbisIp, and delete GsmSector in one transaction (Abis Ip and Trx will be deleted implicitly)
        for (int i = 0 ; i < 3 ; ++i)
        {
        	// make sure there are no old AT Bundling Infos in queue
        	abisHelper.clearAtBundlingInfoUpdateQueue();
        	
        	// Specify which MOs that shall be created together, and their parameter values
        	List<ManagedObject> createMos = new ArrayList<ManagedObject>();

        	setTestStepBegin("Build MO objects");
        	ManagedObject gsmSectorMo = momHelper.buildGsmSectorMo(MomHelper.SECTOR_LDN);
        	createMos.add(gsmSectorMo);
        	RestoreCommand restoreGsmSectorMoCmd = new DeleteMoCommand(gsmSectorMo.getLocalDistinguishedName());
        	momHelper.addRestoreCmd(restoreGsmSectorMoCmd);

        	ManagedObject trxMo =  momHelper.buildTrxMo(MomHelper.TRX_LDN, MomHelper.UNLOCKED, momHelper.getSectorEquipmentFunctionLdn());
        	createMos.add(trxMo);
        	RestoreCommand restoreTrxMoCmd = new LockDeleteMoCommand(trxMo.getLocalDistinguishedName());
        	momHelper.addRestoreCmd(restoreTrxMoCmd);
        	
        	ManagedObject abisIpMo = momHelper.buildAbisIpMo(
        			AbiscoConnection.getConnectionName(),
        			abisco.getBscIpAddress(),
        			MomHelper.ABIS_IP_LDN,
        			MomHelper.TNA_IP_LDN,
        			MomHelper.UNLOCKED);
        	createMos.add(abisIpMo);
        	RestoreCommand restoreAbisIpMoCmd = new LockDeleteMoCommand(abisIpMo.getLocalDistinguishedName());
        	momHelper.addRestoreCmd(restoreAbisIpMoCmd);


        	setTestStepBegin("Create all MOs");
        	momHelper.createSeveralMoInOneTx(createMos);
        	
          	setTestStepBegin("Check post-conditions: Check MO status of GsmSector, AbisIp, and Trx after MO creation and unlock");
        	checkGsmSectorMoAttributeAfterCreateNoLinks();
          	checkAbisIpMoAttributeAfterCreateUnlockBscConnected();
        	checkTrxMoAttributeAfterCreateUnlockNoLinks();
        	
            // Establish Oml link for Scf
            setTestStepBegin("Establish OML link for Scf");
            abisco.establishLinks();
            
            setTestStepBegin("Start SO SCF, AO TF, and AO AT");
            startAllMoVisibleInGsmSector();

            setTestStepBegin("Check MO status of GsmSector, AbisIp, and Trx after link establishment and start of SO SCF, AO TF, and AO AT");
            checkGsmSectorMoAttributeAfterCreateLinksEstablishedAndStartAllMos();
            checkAbisIpMoAttributeAfterCreateUnlockBscConnected();
            checkTrxMoAttributeAfterCreateUnlockNoLinks();
                    
            setTestStepBegin("Send AO AT Config and AO AT Enable");
            sendAoAtConfigPreDefBundling();
            assertTrue("Configuration signature has not been calculated (value = 0) ", 0 != abisHelper.enableRequest(OM_G31R01.Enums.MOClass.AT, 0).getConfigurationSignature().intValue());
            
            setTestStepBegin("Establish OML+RSL for Trx, check MO state");
            abisco.establishLinks(true);
            checkGsmSectorMoAttributeAfterCreateLinksEstablishedAndStartAllMosAtEnabled();
            checkAbisIpMoAttributeAfterCreateUnlockBscConnected();
            checkTrxMoAttributeAfterCreateUnlockLinksEstablished();
        	
            // Get a AT BundlingInfo Update
            setTestStepBegin("Wait for an AT Bundling Info Update");
        	assertEquals("Did not receive a valid AT Bundling Info Update", "", abisHelper.waitForAtBundlingInfoUpdateWithBundlingData(10, tg_0));
            setTestStepEnd();
        	
        	setTestStepBegin("Lock and delete MOs");

        	// we use separate lists for the MOs that shall be deleted, and for the MOs where we will only
        	// update MO attributes
        	// in this test case it means that we will update lock Trx and AbisIp, but only call delete on the GsmSector MO
        	// the Trx and AbisIp MOs shall be deleted implicitly by the delete of the GsmSector
        	List<ManagedObject> deleteMos = new ArrayList<ManagedObject>();
        	List<ManagedObject> setAttributesInMo = new ArrayList<ManagedObject>();

        	ManagedObject delTrx = new ManagedObject(trxMo.getLocalDistinguishedName());
        	delTrx.addAttribute(new ManagedObjectValueAttribute(MomHelper.ADMINISTRATIVE_STATE, MomHelper.LOCKED));
        	setAttributesInMo.add(delTrx);
        	
        	ManagedObject delAbisIp = new ManagedObject(abisIpMo.getLocalDistinguishedName());
        	delAbisIp.addAttribute(new ManagedObjectValueAttribute(MomHelper.ADMINISTRATIVE_STATE, MomHelper.LOCKED));
        	setAttributesInMo.add(delAbisIp);
        	    
        	ManagedObject delGsmSector = new ManagedObject(gsmSectorMo.getLocalDistinguishedName());
        	deleteMos.add(delGsmSector);

        	momHelper.deleteSeveralMoInOneTx(deleteMos, setAttributesInMo);
            
        	// There is no guarantee that the lock Trx will be executed before lock AbisIp, so we
        	// do not wait for any AT BundlingInfo Update or Status Update from SCF since the SCF OML 
            // link is is closed when we lock the AbisIp MO before the TRX MO.  
            
        	setTestStepBegin("Verify that MOs were deleted");
        	checkThatGsmSectorAbisIpTrxMoAreDeleted();

        	setTestStepBegin("Remove MOs from restore stack, since they were deleted successfully");
        	momHelper.removeRestoreCmd(restoreTrxMoCmd);
        	momHelper.removeRestoreCmd(restoreAbisIpMoCmd);
        	momHelper.removeRestoreCmd(restoreGsmSectorMoCmd);
        }
        setTestInfo("Test is done!");
    }

    /**
     * createUnlockAllMoInOneTxPerMoLockDeleteAllMoInOneTxPerMo_1
     * @description Create GsmSector in one transaction, Trx (as UNLOCKED) in one transaction, and AbisIp (as UNLOCKED) in one transaction
     *              See that all links can be established
     *              Delete AbisIp (and LOCK) in one transaction, delete Trx (and LOCK) in one transaction, and delete GsmSector in one transaction
     *              This TC does not completely verify any UC, but the following UCs are involved: 
     *              UC419.N, UC420.N, UC424.N, UC425.N, UC426.A4, UC428.N, UC457.N, UC458.N , UC459.N, UC461.N
     * 
     * @param testId - unique identifier of the test case
     * @param description
     */    
    @Test(timeOut = 850000)
    @Parameters({ "testId", "description" })
    public void createUnlockAllMoInOneTxPerMoLockDeleteAllMoInOneTxPerMo_1(String testId, String description) throws InterruptedException {
        setTestCase(testId, description);
        
        setTestStepBegin("TN");
        if (!momHelper.isTnConfigured())
        	momHelper.configureTn(abisco.getBscIpAddress());
        
    	if (!momHelper.isBtsFunctionCreated()) {
    		setTestInfo("Precondition: Create BtsFunction MO");
    		momHelper.createBtsFunctionMo(MomHelper.BTS_FUNCTION_LDN);
    	}
        
        // Run the same test a couple of times to see that we clean up
        // create GsmSector, Trx (as UNLOCKED), and AbisIp (as UNLOCKED) in one transaction
        // see that all links can be established
        // delete AbisIp (and LOCK), Trx (and LOCK), and GsmSector in one transaction
        for (int i = 0 ; i < 3 ; ++i)
        {
        	// make sure there are no old AT Bundling Infos in queue
        	abisHelper.clearAtBundlingInfoUpdateQueue();
        	
        	// Specify which MOs that shall be created together, and their parameter values
        	List<ManagedObject> createMos = new ArrayList<ManagedObject>();

        	setTestStepBegin("Build and create GsmSector MO");
        	createMos.clear();
        	ManagedObject gsmSectorMo = momHelper.buildGsmSectorMo(MomHelper.SECTOR_LDN);      	
        	createMos.add(gsmSectorMo);
        	RestoreCommand restoreGsmSectorMoCmd = new DeleteMoCommand(gsmSectorMo.getLocalDistinguishedName());
        	momHelper.addRestoreCmd(restoreGsmSectorMoCmd);
        	momHelper.createSeveralMoInOneTx(createMos);
        	
        	setTestStepBegin("Build and create Trx MO");
        	createMos.clear();
        	ManagedObject trxMo =  momHelper.buildTrxMo(MomHelper.TRX_LDN, MomHelper.UNLOCKED, momHelper.getSectorEquipmentFunctionLdn());
        	createMos.add(trxMo);
        	RestoreCommand restoreTrxMoCmd = new LockDeleteMoCommand(trxMo.getLocalDistinguishedName());
        	momHelper.addRestoreCmd(restoreTrxMoCmd);
        	momHelper.createSeveralMoInOneTx(createMos);
        	
        	setTestStepBegin("Build and create AbisIp MO");
        	createMos.clear();
        	ManagedObject abisIpMo = momHelper.buildAbisIpMo(
        			AbiscoConnection.getConnectionName(),
        			abisco.getBscIpAddress(),
        			MomHelper.ABIS_IP_LDN,
        			MomHelper.TNA_IP_LDN,
        			MomHelper.UNLOCKED);
        	createMos.add(abisIpMo);
        	RestoreCommand restoreAbisIpMoCmd = new LockDeleteMoCommand(abisIpMo.getLocalDistinguishedName());
        	momHelper.addRestoreCmd(restoreAbisIpMoCmd);
        	momHelper.createSeveralMoInOneTx(createMos);

          	setTestStepBegin("Check post-conditions: Check MO status of GsmSector, AbisIp, and Trx after MO creation and unlock");
        	checkGsmSectorMoAttributeAfterCreateNoLinks();
          	checkAbisIpMoAttributeAfterCreateUnlockBscConnected();
        	checkTrxMoAttributeAfterCreateUnlockNoLinks();
        	
            // Establish Oml link for Scf
            setTestStepBegin("Establish OML link for Scf");
            abisco.establishLinks();
            
            setTestStepBegin("Start SO SCF, AO TF, and AO AT");
            startAllMoVisibleInGsmSector();

            setTestStepBegin("Check MO status of GsmSector, AbisIp, and Trx after link establishment and start of SO SCF, AO TF, and AO AT");
            checkGsmSectorMoAttributeAfterCreateLinksEstablishedAndStartAllMos();
            checkAbisIpMoAttributeAfterCreateUnlockBscConnected();
            checkTrxMoAttributeAfterCreateUnlockNoLinks();
                    
            setTestStepBegin("Send AO AT Config and AO AT Enable");
            sendAoAtConfigPreDefBundling();
            assertTrue("Configuration signature has not been calculated (value = 0) ", 0 != abisHelper.enableRequest(OM_G31R01.Enums.MOClass.AT, 0).getConfigurationSignature().intValue());
            
            setTestStepBegin("Establish OML+RSL for Trx, check MO state");
            abisco.establishLinks(true);
            checkGsmSectorMoAttributeAfterCreateLinksEstablishedAndStartAllMosAtEnabled();
            checkAbisIpMoAttributeAfterCreateUnlockBscConnected();
            checkTrxMoAttributeAfterCreateUnlockLinksEstablished();
        	
            // Get a AT BundlingInfo Update
            setTestStepBegin("Wait for an AT Bundling Info Update");
        	assertEquals("Did not receive a valid AT Bundling Info Update", "", abisHelper.waitForAtBundlingInfoUpdateWithBundlingData(10, tg_0));
            setTestStepEnd();

        	setTestStepBegin("Lock and delete MOs");

        	// In this test we manipulate each MO in its own transaction
        	List<ManagedObject> deleteMos = new ArrayList<ManagedObject>();

        	deleteMos.clear();
        	ManagedObject delAbisIp = new ManagedObject(abisIpMo.getLocalDistinguishedName());
        	delAbisIp.addAttribute(new ManagedObjectValueAttribute(MomHelper.ADMINISTRATIVE_STATE, MomHelper.LOCKED));
        	deleteMos.add(delAbisIp);
        	momHelper.deleteSeveralMoInOneTx(deleteMos);
        	
        	deleteMos.clear();
        	ManagedObject delTrx = new ManagedObject(trxMo.getLocalDistinguishedName());
        	delTrx.addAttribute(new ManagedObjectValueAttribute(MomHelper.ADMINISTRATIVE_STATE, MomHelper.LOCKED));
        	deleteMos.add(delTrx);
        	momHelper.deleteSeveralMoInOneTx(deleteMos);

        	deleteMos.clear();
        	ManagedObject delGsmSector = new ManagedObject(gsmSectorMo.getLocalDistinguishedName());
        	deleteMos.add(delGsmSector);        	
        	momHelper.deleteSeveralMoInOneTx(deleteMos);
        	
        	// Do not wait for any AT BundlingInfo Update or Status Update from SCF since the SCF OML 
        	// link is is closed when we lock the ABisIp MO before the TRX MO. 
        	
        	setTestStepBegin("Verify that MOs were deleted");
        	checkThatGsmSectorAbisIpTrxMoAreDeleted();

        	setTestStepBegin("Remove MOs from restore stack, since they were deleted successfully");
        	momHelper.removeRestoreCmd(restoreTrxMoCmd);
        	momHelper.removeRestoreCmd(restoreAbisIpMoCmd);
        	momHelper.removeRestoreCmd(restoreGsmSectorMoCmd);
        }
        setTestInfo("Test is done!");
    }
    
    
    /**
     * createUnlockAllMoInOneTxPerMoLockDeleteAllMoInOneTxPerMo_2
     * @description Create GsmSector in one transaction, Trx (as UNLOCKED) in one transaction, and AbisIp (as UNLOCKED) in one transaction
     *              See that all links can be established
     *              Delete Trx (and LOCK) in one transaction, delete AbisIp (and LOCK) in one transaction, and delete GsmSector in one transaction
     *              This TC does not completely verify any UC, but the following UCs are involved: 
     *              UC419.N, UC420.N, UC424.N, UC425.N, UC426.A4, UC428.N, UC457.N, UC458.N , UC459.N, UC461.N
     * 
     * @param testId - unique identifier of the test case
     * @param description
     */    
    @Test(timeOut = 850000)
    @Parameters({ "testId", "description" })
    public void createUnlockAllMoInOneTxPerMoLockDeleteAllMoInOneTxPerMo_2(String testId, String description) throws InterruptedException {
        setTestCase(testId, description);
        
        setTestStepBegin("TN");
        if (!momHelper.isTnConfigured())
        	momHelper.configureTn(abisco.getBscIpAddress());
        
    	if (!momHelper.isBtsFunctionCreated()) {
    		setTestInfo("Precondition: Create BtsFunction MO");
    		momHelper.createBtsFunctionMo(MomHelper.BTS_FUNCTION_LDN);
    	}
        
        // Run the same test a couple of times to see that we clean up
        // create GsmSector, Trx (as UNLOCKED), and AbisIp (as UNLOCKED) in one transaction
        // see that all links can be established
        // delete AbisIp (and LOCK), Trx (and LOCK), and GsmSector in one transaction
        for (int i = 0 ; i < 3 ; ++i)
        {
        	// make sure there are no old AT Bundling Infos in queue
        	abisHelper.clearAtBundlingInfoUpdateQueue();
        	
        	abisHelper.clearStatusUpdate();
        	// Specify which MOs that shall be created together, and their parameter values
        	List<ManagedObject> createMos = new ArrayList<ManagedObject>();

        	setTestStepBegin("Build and create GsmSector MO");
        	createMos.clear();
        	ManagedObject gsmSectorMo = momHelper.buildGsmSectorMo(MomHelper.SECTOR_LDN);      	
        	createMos.add(gsmSectorMo);
        	RestoreCommand restoreGsmSectorMoCmd = new DeleteMoCommand(gsmSectorMo.getLocalDistinguishedName());
        	momHelper.addRestoreCmd(restoreGsmSectorMoCmd);
        	momHelper.createSeveralMoInOneTx(createMos);
        	
        	setTestStepBegin("Build and create Trx MO");
        	createMos.clear();
        	ManagedObject trxMo =  momHelper.buildTrxMo(MomHelper.TRX_LDN, MomHelper.UNLOCKED, momHelper.getSectorEquipmentFunctionLdn());
        	createMos.add(trxMo);
        	RestoreCommand restoreTrxMoCmd = new LockDeleteMoCommand(trxMo.getLocalDistinguishedName());
        	momHelper.addRestoreCmd(restoreTrxMoCmd);
        	momHelper.createSeveralMoInOneTx(createMos);
        	
        	setTestStepBegin("Build and create AbisIp MO");
        	createMos.clear();
        	ManagedObject abisIpMo = momHelper.buildAbisIpMo(
        			AbiscoConnection.getConnectionName(),
        			abisco.getBscIpAddress(),
        			MomHelper.ABIS_IP_LDN,
        			MomHelper.TNA_IP_LDN,
        			MomHelper.UNLOCKED);
        	createMos.add(abisIpMo);
        	RestoreCommand restoreAbisIpMoCmd = new LockDeleteMoCommand(abisIpMo.getLocalDistinguishedName());
        	momHelper.addRestoreCmd(restoreAbisIpMoCmd);
        	momHelper.createSeveralMoInOneTx(createMos);

          	setTestStepBegin("Check post-conditions: Check MO status of GsmSector, AbisIp, and Trx after MO creation and unlock");
        	checkGsmSectorMoAttributeAfterCreateNoLinks();
          	checkAbisIpMoAttributeAfterCreateUnlockBscConnected();
        	checkTrxMoAttributeAfterCreateUnlockNoLinks();

            // Establish Oml link for Scf
            setTestStepBegin("Establish OML link for Scf");
            abisco.establishLinks();
            
            setTestStepBegin("Start SO SCF, AO TF, and AO AT");
            startAllMoVisibleInGsmSector();

            setTestStepBegin("Check MO status of GsmSector, AbisIp, and Trx after link establishment and start of SO SCF, AO TF, and AO AT");
            checkGsmSectorMoAttributeAfterCreateLinksEstablishedAndStartAllMos();
            checkAbisIpMoAttributeAfterCreateUnlockBscConnected();
            checkTrxMoAttributeAfterCreateUnlockNoLinks();
                    
            setTestStepBegin("Send AO AT Config and AO AT Enable");
            sendAoAtConfigPreDefBundling();
            assertTrue("Configuration signature has not been calculated (value = 0) ", 0 != abisHelper.enableRequest(OM_G31R01.Enums.MOClass.AT, 0).getConfigurationSignature().intValue());
            
            setTestStepBegin("Establish OML+RSL for Trx, check MO state");
            abisco.establishLinks(true);
            checkGsmSectorMoAttributeAfterCreateLinksEstablishedAndStartAllMosAtEnabled();
            checkAbisIpMoAttributeAfterCreateUnlockBscConnected();
            checkTrxMoAttributeAfterCreateUnlockLinksEstablished();
        	
            // Get a AT BundlingInfo Update
            setTestStepBegin("Wait for an AT Bundling Info Update");
        	assertEquals("Did not receive a valid AT Bundling Info Update", "", abisHelper.waitForAtBundlingInfoUpdateWithBundlingData(10, tg_0));
            setTestStepEnd();
        	
        	setTestStepBegin("Lock and delete MOs");
        	abisHelper.clearStatusUpdate();

        	// In this test we manipulate each MO in its own transaction
        	List<ManagedObject> deleteMos = new ArrayList<ManagedObject>();

        	deleteMos.clear();
        	ManagedObject delTrx = new ManagedObject(trxMo.getLocalDistinguishedName());
        	delTrx.addAttribute(new ManagedObjectValueAttribute(MomHelper.ADMINISTRATIVE_STATE, MomHelper.LOCKED));
        	deleteMos.add(delTrx);
        	momHelper.deleteSeveralMoInOneTx(deleteMos);
        	
        	deleteMos.clear();
        	ManagedObject delAbisIp = new ManagedObject(abisIpMo.getLocalDistinguishedName());
        	delAbisIp.addAttribute(new ManagedObjectValueAttribute(MomHelper.ADMINISTRATIVE_STATE, MomHelper.LOCKED));
        	deleteMos.add(delAbisIp);
        	momHelper.deleteSeveralMoInOneTx(deleteMos);       	

        	deleteMos.clear();
        	ManagedObject delGsmSector = new ManagedObject(gsmSectorMo.getLocalDistinguishedName());
        	deleteMos.add(delGsmSector);
        	momHelper.deleteSeveralMoInOneTx(deleteMos);
        	
            // Get a AT BundlingInfo Update
            setTestStepBegin("Wait for an AT Bundling Info Update");
        	assertEquals("Did not receive a valid AT Bundling Info Update", "", abisHelper.waitForAtBundlingInfoUpdateWithEmptyBundlingData(10, tg_0));
            setTestStepEnd();
        	
            // Get a Status Update and check the BTS Capabilities Signature
            setTestStepBegin("Wait for two Status Update from SCF");
        	assertEquals("Did not receive a valid SO SCF Status Update 1", "", abisHelper.waitForSoScfStatusUpdate(10, tg_0, new StringBuffer()));
            setTestStepEnd();

        	setTestStepBegin("Verify that MOs were deleted");
        	checkThatGsmSectorAbisIpTrxMoAreDeleted();

        	setTestStepBegin("Remove MOs from restore stack, since they were deleted successfully");
        	momHelper.removeRestoreCmd(restoreTrxMoCmd);
        	momHelper.removeRestoreCmd(restoreAbisIpMoCmd);
        	momHelper.removeRestoreCmd(restoreGsmSectorMoCmd);
        }
        setTestInfo("Test is done!");
    }
    
    /**
     * loadTest
     * @description TBD...
     * Create GsmSector, AbisIp (as UNLOCKED), and Trx (as UNLOCKED) in one transaction
     *              See that all links can be established
     *              Delete Trx (and LOCK), AbisIp (and LOCK), and GsmSector in one transaction
     *              This TC does not completely verify any UC, but the following UCs are involved: 
     *              
     * 
     * @param testId - unique identifier of the test case
     * @param description
     */
    @Test(timeOut = 850000)
    @Parameters({ "testId", "description" })
    public void loadTest(String testId, String description) {
        setTestCase(testId, description);
        
        setTestStepBegin("TN");
        if (!momHelper.isTnConfigured())
        	momHelper.configureTn(abisco.getBscIpAddress());
        
    	if (!momHelper.isBtsFunctionCreated()) {
    		setTestInfo("Precondition: Create BtsFunction MO");
    		momHelper.createBtsFunctionMo(MomHelper.BTS_FUNCTION_LDN);
    	}
  
    	// Specify which MOs that shall be created together, and their parameter values
    	List<ManagedObject> createMos = new ArrayList<ManagedObject>();

    	setTestStepBegin("Build and create GsmSector MO");
    	createMos.clear();
    	ManagedObject gsmSectorMo = momHelper.buildGsmSectorMo(MomHelper.SECTOR_LDN);      	
    	createMos.add(gsmSectorMo);
    	RestoreCommand restoreGsmSectorMoCmd = new DeleteMoCommand(gsmSectorMo.getLocalDistinguishedName());
    	momHelper.addRestoreCmd(restoreGsmSectorMoCmd);
    	momHelper.createSeveralMoInOneTx(createMos);

    	setTestStepBegin("Build and create Trx MO");
    	createMos.clear();
    	ManagedObject trxMo =  momHelper.buildTrxMo(MomHelper.TRX_LDN, MomHelper.UNLOCKED, momHelper.getSectorEquipmentFunctionLdn());
    	createMos.add(trxMo);
    	RestoreCommand restoreTrxMoCmd = new LockDeleteMoCommand(trxMo.getLocalDistinguishedName());
    	momHelper.addRestoreCmd(restoreTrxMoCmd);

    	// In this test we manipulate each MO in its own transaction
    	List<ManagedObject> deleteMos = new ArrayList<ManagedObject>();

    	deleteMos.clear();
    	ManagedObject delTrx = new ManagedObject(trxMo.getLocalDistinguishedName());
    	delTrx.addAttribute(new ManagedObjectValueAttribute(MomHelper.ADMINISTRATIVE_STATE, MomHelper.LOCKED));
    	deleteMos.add(delTrx);

        // Run the same test a couple of times to see that we clean up
        // create GsmSector, Trx (as UNLOCKED), and AbisIp (as UNLOCKED) in one transaction
        // see that all links can be established
        // delete AbisIp (and LOCK), Trx (and LOCK), and GsmSector in one transaction
        for (int i = 0 ; i < 15 ; ++i)
        {
        	setTestStepBegin("Create and unlock Trx");
        	momHelper.createSeveralMoInOneTx(createMos);

        	setTestStepBegin("Lock and delete Trx");
        	momHelper.deleteSeveralMoInOneTx(deleteMos);
        }
        
    	deleteMos.clear();
    	ManagedObject delGsmSector = new ManagedObject(gsmSectorMo.getLocalDistinguishedName());
    	deleteMos.add(delGsmSector);
    	momHelper.deleteSeveralMoInOneTx(deleteMos);
        
    	setTestStepBegin("Remove MOs from restore stack, since they were deleted successfully");
    	momHelper.removeRestoreCmd(restoreTrxMoCmd);
    	//momHelper.removeRestoreCmd(restoreAbisIpMoCmd);
    	momHelper.removeRestoreCmd(restoreGsmSectorMoCmd);

        
    }   
}   
  
      