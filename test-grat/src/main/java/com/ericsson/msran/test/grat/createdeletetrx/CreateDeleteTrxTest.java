package com.ericsson.msran.test.grat.createdeletetrx;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import se.ericsson.jcat.fw.annotations.Setup;
import se.ericsson.jcat.fw.annotations.Teardown;

import com.ericsson.msran.jcat.TestBase;
import com.ericsson.abisco.clientlib.servers.BG.G31StatusSCF;
import com.ericsson.abisco.clientlib.servers.BG.G31StatusUpdate;
import com.ericsson.abisco.clientlib.servers.OM_G31R01;
import com.ericsson.commonlibrary.resourcemanager.Rm;
import com.ericsson.commonlibrary.resourcemanager.g2.G2Rbs;

import com.ericsson.msran.g2.annotations.TestInfo;

import com.ericsson.commonlibrary.ecimcom.exception.GracefulDisconnectFailedException;
import com.ericsson.commonlibrary.ecimcom.netconf.NetconfClient;
import com.ericsson.commonlibrary.ecimcom.netconf.NetconfManagedObjectHandler;

import com.ericsson.commonlibrary.managedobjects.ManagedObject;
import com.ericsson.commonlibrary.managedobjects.ManagedObjectValueAttribute;
import com.ericsson.commonlibrary.managedobjects.ManagedObjectValueStringAttribute;
import com.ericsson.commonlibrary.managedobjects.exception.OperationFailedException;

import com.ericsson.msran.test.grat.testhelpers.AbisHelper;
import com.ericsson.msran.test.grat.testhelpers.AbiscoConnection;
import com.ericsson.msran.test.grat.testhelpers.MomHelper;
import com.ericsson.msran.test.grat.testhelpers.NodeStatusHelper;

/**
 * @id NodeUC424,NodeUC425,NodeUC522
 * 
 * @name CreateDeleteTrxTest
 * 
 * @author Team Roadrunner
 * 
 * @created 2013-07-01
 * 
 * @description A test that creates the specified number of GsmSectors with the
 *              specified number of Trxs in each sector. An option boolean
 *              (default false) specifies whether to unlock and lock the Trxs
 *              after creation.
 * 
 * @revision Team Roadrunner 2013-07-01 first version.
 * @revision xasahuh 2014-02-07 Set timeout for test case to 1 minute.
 * @revision xasahuh 2014-02-10 Added TestInfo metadata.
 * @revision xasahuh 2014-02-12 Removed builder pattern structure and use Restore Stack.
 * @revision xasahuh 2014-02-24 Renamed from CreateMosTest, moved methods here from other packages
 *                              that verifies the same usecase.
 * @revision emaomar 2014-03-14 MAX_NO_OF_MOS = 127->12     
 * @revision eraweer 2014-03-25 NodeUC424.E1 replaced by NodeUC522.N.
 * @revision etxheda 2015-02-18 MAX_NO_OF_TRX_PER_MO = 12
 * @revision eraweer 2015-02-24 Verification of NodeUC424.E2.
 */

public class CreateDeleteTrxTest extends TestBase {
    
    private static final int MAX_NO_OF_TRX_PER_MO = 12;
    private static final String NON_EXIST_SECTOR_EQUIP_FUNC_LDN = "ManagedElement=1,NodeSupport=1,SectorEquipmentFunction=666";
    
    private MomHelper momHelper;
    private AbiscoConnection abisco;
    private AbisHelper abisHelper;

    private NodeStatusHelper nodeStatus;
    
    /**
     * Description of test case for test reporting
     */
    @TestInfo(
            tcId = "NodeUC424,NodeUC425,NodeUC522",
            slogan = "Create TRX - Delete TRX",
            requirementDocument = "1/00651-FCP 130 1402",
            requirementRevision = "PC5",
            requirementLinkTested = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff87c1d941?docno=1/00651-FCP1301402Uen&format=excel8book",
            requirementLinkLatest = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff86a2af3d?docno=1/00651-FCP1301402Uen&action=current&format=excel8book",
            requirementIds = { "10565-0334/19417[A][APPR]" },
            verificationStatement = "Verifies NodeUC424.N, NodeUC425.N, NodeUC425.E1, NodeUC424.E2, and NodeUC522.N",
            testDescription = "Verifies creation of a specified number of GsmSectors with a specified number of Trxs in each sector",
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


    /**
     * @name createTrxAndVerifyTrxIndex
     * 
     * @description 	Creates a Trx MO and verifies that it gets the expected Trx Index attribute
     * 
     * @param sectorLdn 	The LDN of the Sector MO where to create the Trx MO
     * @param trx 		The Trx part of LDN
     * @param expectedTrxIndex	The exptected value of Trx Index attibute
     * @return                  The LDN of the created Trx MO
     */
    private String createTrxAndVerifyTrxIndex(String sectorLdn, Integer trx, Integer expectedTrxIndex) {
      String trxLdnStr = momHelper.createTrxMo(sectorLdn, Integer.toString(trx));
      setTestStepBegin("Created and verifying: " + trxLdnStr);
      assertTrue("MO (" + trxLdnStr + ") doesn't exist", momHelper.checkMoExist(trxLdnStr));
      assertEquals("Trx MO attributes did not reach expected values", "", momHelper.checkTrxMoAttributeAfterLock(trxLdnStr, 5, expectedTrxIndex.toString()));  
      setTestStepEnd();
      return trxLdnStr;
    }
    
    /**
     * Send the startSoScf to Abisco. Needed for precondition in test.
     */
    private void startSoScf() {
        ArrayList<String> expectedOmlIwdVersions = new ArrayList<String>(
                Arrays.asList(AbiscoConnection.OML_IWD_VERSION));
        ArrayList<String> expectedRslIwdVersions = new ArrayList<String>(Arrays.asList(
                AbiscoConnection.RSL_IWD_VERSION_1, AbiscoConnection.RSL_IWD_VERSION_2));

        abisHelper.clearNegotiationRecord1Data();
        try {
            abisHelper.startRequest(OM_G31R01.Enums.MOClass.SCF, 0);
        } catch (InterruptedException e) {
            fail("SCF start request was interrupted");
        }

        List<Integer> negotiationRecord1Data = abisHelper.getNegotiationRecord1Data();
                
        assertTrue("NegotiationRecord1Data List empty", abisHelper.compareNegotiationRecord1Data(negotiationRecord1Data, expectedOmlIwdVersions,
                expectedRslIwdVersions));
    }

    /**
     * @name createDeleteTrx
     * 
     * @description Create a number of GsmSector MO:s with a number of Trx MO:s per sector.
     *
     *              This test will currently also verify that the trxInstance attribute has been updated correctly
     *              In doing so it will actually create more Trx per GsmSector than given in noOfTrxsPerGsmSector.
     *              Maybe the trxInstance should be moved to an one test case.
     * 
     * @param testId - String
     * @param description - String
     * @param noOfGsmSectors - String 
     * @param noOfTrxsPerGsmSector - String
     * 
     * @throws Exception
     */
    @Test(timeOut = 600000)
    @Parameters({"testId", "description", "noOfGsmSectors", "noOfTrxsPerGsmSector" })
    public void createDeleteTrx(String testId, String description, String noOfGsmSectors, String noOfTrxsPerGsmSector) throws Exception {
        
        setTestCase(testId, description);
        
        setTestInfo("Create " + noOfGsmSectors + " GsmSector MO:s with " + noOfTrxsPerGsmSector + " TRX MO per GsmSector");
        
        /*
         * Create MO:s
         */
        
    	if (!momHelper.isBtsFunctionCreated()) {
    		setTestInfo("Precondition: Create BtsFunction MO");
    		momHelper.createBtsFunctionMo(MomHelper.BTS_FUNCTION_LDN);
    	}
        
        setTestStepBegin("Create GsmSector MO:s");
        
        int noGsmSectors = Integer.parseInt(noOfGsmSectors);
        int noTrxsPerGsmSector = Integer.parseInt(noOfTrxsPerGsmSector);
        
        /*
         * Create GsmSector MO:s 
         */
        ArrayList<String> sectorLdnList = momHelper.createGsmSectorMos(noGsmSectors);
        for (String sectorLdn : sectorLdnList) {
            // Pre-conditions
            setTestInfo("Precondition for Trx creation: Create AbisIp MO and link not established");
            momHelper.createAbisIpMoForSector(sectorLdn, AbiscoConnection.getConnectionName(), abisco.getBscIpAddress(), false);
            assertEquals("GsmSector MO attributes did not reach expected values", "", momHelper.checkGsmSectorMoAttributeAfterCreateNoLinks(sectorLdn, 5));
        }
        setTestStepEnd();

        /*
         * Create Trx MO:s 
         */
        String trxLdnStr;
        ArrayList<String> trxLdnList = new ArrayList<String>();
        for (String sectorLdn : sectorLdnList) {
            for (int i = 1; i < noTrxsPerGsmSector + 1; i++) {
                setTestStepBegin("Creating: Trx=" + i + " MO.");
                trxLdnStr = momHelper.createTrxMo(sectorLdn, Integer.toString(i));
                trxLdnList.add(trxLdnStr);
                setTestStepEnd();
            }
        }   
        
        for (String trxLdn : trxLdnList) {
        	setTestStepBegin("Verifying " + trxLdn);
            assertTrue("MO (" + trxLdn +") doesn't exist", momHelper.checkMoExist(trxLdn));
            
            // Post-conditions 
            setTestInfo("Post-conditions: Check TRX MO attributes");
            assertEquals("Trx MO attributes did not reach expected values", "", momHelper.checkTrxMoAttributeAfterLock(trxLdn, 5)); 
            setTestStepEnd();
        }

        /*
         * Check that trxIndex is set correctly with strange trxLdn
         */
        for (String sectorLdn : sectorLdnList) {
          /*
           * Create Trx Mo with trxLdn > 126
           * Expected trxIndex is 0 which is the first free when 1 already have been chosen
           */
          trxLdnStr = createTrxAndVerifyTrxIndex(sectorLdn, 127, 0);
          trxLdnList.add(trxLdnStr);
          /*
           * Create Trx Mo with trxLdn 2222
           * Expected trxIndex is 2 which is the first free when 0,1 already have been chosen
           * Do not add to trxLdnList as it will be deleted soon
           */
          String trxLdnStr2222 = createTrxAndVerifyTrxIndex(sectorLdn, 2222, 2);
          /*
           * Create Trx Mo with trxLdn 3333
           * Expected trxIndex is 3 which is the first free when 0,1,2 already have been chosen
           */
          trxLdnStr = createTrxAndVerifyTrxIndex(sectorLdn, 3333, 3);
          trxLdnList.add(trxLdnStr);
          /*
           * Delete Trx Mo with trxLdn 2222
           * Will free trxIndex 2
           */
          setTestStepBegin("Delete: " + trxLdnStr2222);
          momHelper.deleteMo(trxLdnStr2222);
          setTestStepEnd();
          /*
           * Create Trx Mo with trxLdn 4444
           * Expected trxIndex is 2 which is the first free when 0,1,3 already have been chosen
           */
          trxLdnStr = createTrxAndVerifyTrxIndex(sectorLdn, 4444, 2);
          trxLdnList.add(trxLdnStr);
        }
        
        /*
         * Delete Trx MO:s
         */
        for (String trxLdn : trxLdnList) {
            setTestStepBegin("Deleting: " + trxLdn);
            momHelper.deleteMo(trxLdn);
            setTestStepEnd();
        }
        
        /*
         * Delete GsmSector MO
         */
        for (String sectorLdn : sectorLdnList) {
            setTestStepBegin("Delete: " + sectorLdn);
            momHelper.deleteMo(sectorLdn);
            setTestStepEnd();
        }       
    }
    
    /**
     * @name deleteUnlockedTrxFails
     * 
     * @description Delete of an unlocked MO (Trx in this case) shall fail,
     *              according to NodeUC425.E1.
     * 
     * @param testId - unique identifier of the test case
     * @param description
     */
    @Test(timeOut = 360000)
    @Parameters({ "testId", "description" })
    public void deleteUnlockedTrxFails(String testId, String description) {
        
        setTestCase(testId, description);
        
    	if (!momHelper.isBtsFunctionCreated()) {
    		setTestInfo("Precondition: Create BtsFunction MO");
    		momHelper.createBtsFunctionMo(MomHelper.BTS_FUNCTION_LDN);
    	}
        
        setTestInfo("Precondition: Create GsmSector MO");
        String sectorMoLdn = momHelper.createGsmSectorMos(1).get(0); 
        
        setTestInfo("Precondition for Trx creation: Create AbisIp MO");
        momHelper.createAbisIpMo(AbiscoConnection.getConnectionName(), momHelper.randomPrivateIp(), false);

        String trxLdn = momHelper.createTrxMo(sectorMoLdn, "1");
        setTestStepBegin("Created and now verifying: " + trxLdn);
        assertEquals("Trx MO attributes did not reach expected values", "", momHelper.checkTrxMoAttributeAfterLock(trxLdn, 5));
        setTestStepEnd();
        
        setTestStepBegin("Unlock MO: " + trxLdn);
        momHelper.unlockMo(trxLdn);
        assertEquals("Trx MO attributes did not reach expected values", "", momHelper.checkTrxMoAttributeAfterUnlockNoLinks(trxLdn, 5));
        setTestStepEnd();
            
        try {
            momHelper.deleteMo(trxLdn);
        } catch (Exception e) {
            setTestInfo("Verified that it an unlocked MO cannot be deleted");
            assertTrue("MO (" + trxLdn +") doesn't exists", momHelper.checkMoExist(trxLdn));
            return;
        }
        
        fail("Unlock MO was expected to fail");
    }
    
    /**
     * @name createTrxFails
     * 
     * @description Verify that an attempt to create more TRX MO:s than allowed fails. 
     *              The creation of the MO:s are not committed, so there is no need to
     *              delete the MO:s after the test execution (to save time).
     *              Verification according to NodeUC522.N.
     * 
     * @param testId - unique identifier of the test case
     * @param description
     * 
     */
    @Test(timeOut = 360000)
    @Parameters({ "testId", "description" })
    public void createTrxFails(String testId, String description) {
        
        setTestCase(testId, description);
        
    	if (!momHelper.isBtsFunctionCreated()) {
    		setTestInfo("Precondition: Create BtsFunction MO");
    		momHelper.createBtsFunctionMo(MomHelper.BTS_FUNCTION_LDN);
    	}
        
        setTestInfo("Precondition: Create GsmSector MO");
        String sectorMoLdn = momHelper.createGsmSectorMos(1).get(0); 
        
        G2Rbs rbs = Rm.getG2RbsList().get(0);
        NetconfManagedObjectHandler moHandler = rbs.getManagedObjectHandler();
                
        if (moHandler instanceof NetconfClient) {
            setTestStepBegin("Setting operation timeout to 50 seconds");
            ((NetconfClient) moHandler).setOperationTimeout(50, TimeUnit.SECONDS);
            setTestStepEnd();
        }
        
        setTestStepBegin("Create " + MAX_NO_OF_TRX_PER_MO + " Trx MO:s");
        
        /* 
         * Create MAX_NO_OF_TRX_PER_MO Trx MO:S
         */
        String trxLdn;
        String seqEqFuncLdn = momHelper.getSectorEquipmentFunctionLdn();
        ManagedObject trxMo;
        moHandler.connect();
        for (int i = 1; i <= MAX_NO_OF_TRX_PER_MO; i++) { 
            trxLdn = String.format("%s,Trx=%s", sectorMoLdn, Integer.toString(i));
            setTestInfo("Create Trx MO with LDN " + trxLdn + " SectorEquipmentFunctionRef " + seqEqFuncLdn);
            
            trxMo = new ManagedObject(trxLdn);
            // Create MO without commit
            trxMo.addAttribute(new ManagedObjectValueAttribute("txPower", "10"));
            trxMo.addAttribute(new ManagedObjectValueAttribute("arfcnMin", "1"));
            trxMo.addAttribute(new ManagedObjectValueAttribute("arfcnMax", "120"));
            trxMo.addAttribute(new ManagedObjectValueAttribute("frequencyBand", "0"));
            trxMo.addAttribute(new ManagedObjectValueStringAttribute(MomHelper.SECTOR_EQUIP_FUNC_REF, seqEqFuncLdn));
            moHandler.createManagedObject(trxMo);
        }
        setTestStepEnd();
        
        /*
         * Try to create one more, shall fail
         */
        trxLdn = String.format("%s,Trx=%s", sectorMoLdn, Integer.toString(MAX_NO_OF_TRX_PER_MO + 1));
        setTestInfo("Create Trx MO with LDN " + trxLdn + " SectorEquipmentFunctionRef " + seqEqFuncLdn);
        
        trxMo = new ManagedObject(trxLdn);
        // Create MO without commit
        trxMo.addAttribute(new ManagedObjectValueAttribute("txPower", "10"));
        trxMo.addAttribute(new ManagedObjectValueAttribute("arfcnMin", "1"));
        trxMo.addAttribute(new ManagedObjectValueAttribute("arfcnMax", "120"));
        trxMo.addAttribute(new ManagedObjectValueAttribute("frequencyBand", "0"));
        trxMo.addAttribute(new ManagedObjectValueStringAttribute(MomHelper.SECTOR_EQUIP_FUNC_REF, seqEqFuncLdn));

        setTestStepBegin("Try to create more Trx MO:s than the maximum limit, shall fail");
        try {
            moHandler.createManagedObject(trxMo);
            fail("Successfully created " + trxLdn + ", which exceeds the maximum number of allowed Trx MOs.");
        } catch (OperationFailedException e) {
            setTestInfo("Create of more Trx MO:s than the maximum limit failed as expected");
            setTestInfo( "Msg = " + e.getMessage().toString() );
        }
        setTestStepEnd();

        try {
            moHandler.disconnect();
            // Fail commented out, since above exception now cause a netconf "abort", which enables graceful disconnect
            // this might change again soon, so I'm keeping it in here...
            // fail("Commit of failed transaction succeeded");
        } catch (Exception e) {
        	// It shouldn't be possible to end up in here, unless behavior changes again...
            setTestInfo( "Exception caught = " + e.getMessage().toString() );
            fail("Disconnect failed...");
        }
    }
    
    /**
     * @name createTrxWithNonExistingSectorEqFuncRefFails
     * 
     * @description Create a Trx that refers to a non existing SectorEquipmentFunction MO.
     *              Verification according to NodeUC424.E1.
     *              
     * @param testId - unique identifier of the test case
     * @param description
     * 
     */
    @Test(timeOut = 360000)
    @Parameters({ "testId", "description" })
    public void createTrxWithNonExistingSectorEqFuncRefFails(String testId, String description) {
        
        setTestCase(testId, description);
        
    	if (!momHelper.isBtsFunctionCreated()) {
    		setTestInfo("Precondition: Create BtsFunction MO");
    		momHelper.createBtsFunctionMo(MomHelper.BTS_FUNCTION_LDN);
    	}
        
        setTestInfo("Precondition: Create GsmSector MO");
        String sectorMoLdn = momHelper.createGsmSectorMos(1).get(0); 
        
        G2Rbs rbs = Rm.getG2RbsList().get(0);
        NetconfManagedObjectHandler moHandler = rbs.getManagedObjectHandler();
                
        if (moHandler instanceof NetconfClient) {
            setTestStepBegin("Setting operation timeout to 50 seconds");
            ((NetconfClient) moHandler).setOperationTimeout(50, TimeUnit.SECONDS);
            setTestStepEnd();
        }

        /* 
         * Create a Trx with a non-existing GsmSector MO
         */
        String trxLdn;
        ManagedObject trxMo;
        trxLdn = String.format("%s,Trx=1", sectorMoLdn);
        setTestInfo("Create Trx MO with LDN " + trxLdn);

        // Create MO 
        trxMo = new ManagedObject(trxLdn);
        trxMo.addAttribute(new ManagedObjectValueAttribute("txPower", "10"));
        trxMo.addAttribute(new ManagedObjectValueAttribute("arfcnMin", "1"));
        trxMo.addAttribute(new ManagedObjectValueAttribute("arfcnMax", "120"));
        trxMo.addAttribute(new ManagedObjectValueAttribute("frequencyBand", "0"));
        trxMo.addAttribute(new ManagedObjectValueStringAttribute(MomHelper.SECTOR_EQUIP_FUNC_REF, NON_EXIST_SECTOR_EQUIP_FUNC_LDN));

        setTestStepBegin("Create a Trx that referes to a non-existing SectorEquipmenFunction MO, shall fail");
        try {
            momHelper.createManagedObject(trxMo);
            fail("Successfully created " + trxLdn + ", which a non-existing MO.");
        } catch (GracefulDisconnectFailedException e) {       	        	
        	if (!e.getLocalizedMessage().contains("error-description: Transaction commit failed, [The requested operation failed. (sa_ais_err_failed_operation)Attribute 'sectorEquipmentFunctionRef' in object: 'ManagedElement=1,BtsFunction=1,GsmSector=1,Trx=1' is a reference to non existent object: 'ManagedElement=1,NodeSupport=1,SectorEquipmentFunction=666']"))
        	{
        		fail("The caught excption did not contain the expected error. Caught exception: " + e.getLocalizedMessage());
        	}
        	setTestInfo("Got the expected exception when trying to create Trx with non existing sector function");
        }
        setTestStepEnd();      
    }
    
   
    
    /**
     * @name createTrxFailsNoAbisIp
     * 
     * @description Verify that an attempt to create TRX MO when no AbisIp MO exists 
     *              for the GsmSector MO fails. 
     *              Verification according to NodeUC424.E1.
     * 
     * @param testId - unique identifier of the test case
     * @param description
     * 
     * @deprecated
     * 
     */
    @Test(timeOut = 360000)
    @Parameters({ "testId", "description" })
    public void createTrxFailsNoAbisIp(String testId, String description) {
        
        setTestCase(testId, description);
        
    	if (!momHelper.isBtsFunctionCreated()) {
    		setTestInfo("Precondition: Create BtsFunction MO");
    		momHelper.createBtsFunctionMo(MomHelper.BTS_FUNCTION_LDN);
    	}

        setTestInfo("Precondition: Create GsmSector MO");
        String sectorMoLdn = momHelper.createGsmSectorMos(1).get(0); 
               
        setTestStepBegin("Try to create TRX MO");        
        try {
            momHelper.createTrxMo(sectorMoLdn, "1");
        } catch (GracefulDisconnectFailedException e) {
        	try { 
                if (!e.getCause().getMessage().contains("Transaction commit failed")) {
                    fail("Transaction didn't fail with expected message. Faulty message: " + e);
                }
    	    } catch (NullPointerException npe) {
    	    	setTestInfo("Transaction failed with NULL message. " + npe);
            }
        }
        setTestStepEnd();
        
        /*
         * Delete GsmSector MO
         */
        momHelper.deleteMo(sectorMoLdn);
      
    }

    /**
     * @name createDeleteTrxWithEstablishedScfOml
     * 
     * @description Create a TRX with an established SCF OML link and verify attributes.
     *              Verification according to NodeUC424.N and NodeUC425.N
     * 
     * @param testId - String
     * @param description - String
     * 
     * @throws Exception
     */
    @Test(timeOut = 500000)
    @Parameters({"testId", "description"})
    public void createDeleteTrxWithEstablishedScfOml(String testId, String description) throws Exception {

        setTestCase(testId, description);
        
    	if (!momHelper.isBtsFunctionCreated()) {
    		setTestInfo("Precondition: Create BtsFunction MO");
    		momHelper.createBtsFunctionMo(MomHelper.BTS_FUNCTION_LDN);
    	}
        
        // Pre-conditions: Create GsmSector, AbisIp and establish link
        setTestInfo("Preconditions: Create GsmSector, AbisIp MO and link established");
        String sectorLdn = "ManagedElement=1,BtsFunction=1,GsmSector=1";
        String abisIpLdn = sectorLdn + ",AbisIp=1";
        abisIpLdn = momHelper.createSectorAndAbisIpMo(AbiscoConnection.getConnectionName(), abisco.getBscIpAddress(), abisIpLdn, false);
        
        // Unlock AbisIp
        setTestInfo("Unlock AbisIp");
        momHelper.unlockMo(abisIpLdn);
        
        // Setup Abisco 
        abisco.setupAbisco(false);
        
        // Establish link
        setTestInfo("Establish links");
        try {
            abisco.establishLinks();
        } catch (InterruptedException ie) {
            fail("InteruptedException during establishLinks");
        }
        
        startSoScf();
        
        // Check SCF OML state
        setTestStepBegin("Verify that the OML state is UP and ScfState STARTED");
        assertEquals("GsmSector MO attributes did not reach expected values", "", momHelper.checkGsmSectorMoAttributeAfterCreateLinksEstablishedSoScfStarted(sectorLdn, 5));
        setTestStepEnd();
        
        
        int calandarTimeSequenceNumber = abisHelper.calendarTimeExchange(0, 2014, 12, 14, 13, 20, 5, 0).getCalendarTimeSequenceNum();
        int bscNodeIdentitySig = 0xAABB;
        abisHelper.nodeIdentityExchange(0, "BSC_ID_1", "TG_ID_0", bscNodeIdentitySig);
        
        int bscCapabilitiesSig = 0xFEED;
        abisHelper.scfCapabilitiesExchangeRequest(0, bscCapabilitiesSig);

        // Create Trx MO
        setTestStepBegin("Creating Trx MO");
        abisHelper.clearStatusUpdate();
        String trxLdn = momHelper.createTrxMo(sectorLdn, "1");
        assertTrue("MO (" + trxLdn +") doesn't exists", momHelper.checkMoExist(trxLdn));
        setTestStepEnd();
        
        // Get a Status Update and check the BTS Capabilities Signature
        setTestStepBegin("Wait for a Status Update from SCF");
        int createTrxBtsCapsSig = -1;
        G31StatusUpdate statusUpdate = abisHelper.getStatusUpdate(10, TimeUnit.SECONDS);
        if (statusUpdate != null) {
            G31StatusSCF soScfStatusUpdate = statusUpdate.getStatusChoice().getG31StatusSCF();
            if (soScfStatusUpdate != null) {
                createTrxBtsCapsSig = soScfStatusUpdate.getBTSCapabilitiesSignature();
                saveAssertTrue("BTS Capabilities signature is not valid", createTrxBtsCapsSig != 0);
                saveAssertTrue("BTS Node Identity Signature", 0 != soScfStatusUpdate.getBTSNodeIdentitySignature());
                saveAssertTrue("Agreed Negotiation Signature", 0 != soScfStatusUpdate.getAgreedNegotiationSignature());
                saveAssertTrue("Offered Negotiation Signature", 0 != soScfStatusUpdate.getOfferedNegotiationSignature());
                saveAssertTrue("Calendar Time Sequence Number is not as expected", calandarTimeSequenceNumber == soScfStatusUpdate.getCalendarTimeSequenceNum());
                saveAssertTrue("BSC Node Identity Signature is not as expected", bscNodeIdentitySig == soScfStatusUpdate.getBSCNodeIdentitySignature());
                saveAssertTrue("BSC Capabilites Signature is not as expected", bscCapabilitiesSig == soScfStatusUpdate.getBSCCapabilitiesSignature());
            }
        }
        saveAssertTrue("Did not get a SCF Status Update", createTrxBtsCapsSig != -1);
        setTestInfo("BTS Node Identity Signature after creating Trx = " + createTrxBtsCapsSig);
        setTestStepEnd();

        // Post-conditions 
        setTestStepBegin("Post-conditions: TRX MO has attributes operationalState = DISABLED, availabilityStatus = OFF_LINE, abisTrxcOmlState = DOWN and abisTrxcState = DOWN");
        assertEquals("Trx MO attributes did not reach expected values", "", momHelper.checkTrxMoAttributeAfterLock(trxLdn, 5));
        setTestStepEnd();

        // Delete MOs
        abisHelper.clearStatusUpdate();
        setTestInfo("Delete MOs");
        momHelper.deleteMo(trxLdn);

        // Get a Status Update and get the (new) BTS Capabilities Signature
        setTestStepBegin("Wait for a Status Update from SCF");
        int deleteTrxBtsCapsSig = -1;
        statusUpdate = abisHelper.getStatusUpdate(10, TimeUnit.SECONDS);
        if (statusUpdate != null) {
            G31StatusSCF soScfStatusUpdate = statusUpdate.getStatusChoice().getG31StatusSCF();
            if (soScfStatusUpdate != null) {
                deleteTrxBtsCapsSig = soScfStatusUpdate.getBTSCapabilitiesSignature();
                saveAssertTrue("BTS Capabilities signature is not valid", deleteTrxBtsCapsSig != 0);
                saveAssertTrue("BTS Node Identity Signature", 0 != soScfStatusUpdate.getBTSNodeIdentitySignature());
                saveAssertTrue("Agreed Negotiation Signature", 0 != soScfStatusUpdate.getAgreedNegotiationSignature());
                saveAssertTrue("Offered Negotiation Signature", 0 != soScfStatusUpdate.getOfferedNegotiationSignature());
                saveAssertTrue("Calendar Time Sequence Number is not as expected", calandarTimeSequenceNumber == soScfStatusUpdate.getCalendarTimeSequenceNum());
                saveAssertTrue("BSC Node Identity Signature is not as expected", bscNodeIdentitySig == soScfStatusUpdate.getBSCNodeIdentitySignature());
                saveAssertTrue("BSC Capabilites Signature is not as expected", bscCapabilitiesSig == soScfStatusUpdate.getBSCCapabilitiesSignature());
            }
        }
        saveAssertTrue("Did not get a SCF Status Update", deleteTrxBtsCapsSig != -1);

        setTestInfo("BTS Node Identity Signature after deleting Trx = " + deleteTrxBtsCapsSig);
        setTestInfo("Verify that that BTS Node Identity Signature has changed");
        assertTrue("BTS Node Id Sig did not change after deleting Trx", 
                createTrxBtsCapsSig != deleteTrxBtsCapsSig);
        setTestStepEnd();
        
        // lock AbisIp
        setTestInfo("lock AbisIp");
        momHelper.lockMo(abisIpLdn);
        momHelper.deleteMo(abisIpLdn); 
        momHelper.deleteMo(sectorLdn);
    }

    /**
     * @name createTrxWithMismatchArfcnAndFrequency
     * 
     * @description Create TRX with mismatch between ARFCN and frequency band.
     *              Verification according to NodeUC424.E2.
     *          
     * @param testId - unique identifier of the test case
     * @param description
     * 
     */
    @Test(timeOut = 360000)
    @Parameters({ "testId", "description" })
    public void createTrxWithMismatchArfcnAndFrequency(String testId, String description) {
        
        setTestCase(testId, description);
        
    	if (!momHelper.isBtsFunctionCreated()) {
    		setTestInfo("Precondition: Create BtsFunction MO");
    		momHelper.createBtsFunctionMo(MomHelper.BTS_FUNCTION_LDN);
    	}
        
        setTestInfo("Precondition: Create GsmSector MO");
        String sectorMoLdn = momHelper.createGsmSectorMos(1).get(0); 
        
        G2Rbs rbs = Rm.getG2RbsList().get(0);
        NetconfManagedObjectHandler moHandler = rbs.getManagedObjectHandler();
                
        if (moHandler instanceof NetconfClient) {
            setTestStepBegin("Setting operation timeout to 50 seconds");
            ((NetconfClient) moHandler).setOperationTimeout(50, TimeUnit.SECONDS);
            setTestStepEnd();
        }

        /* 
         * Create a Trx with a mismatch between ARFCN and frequency band
         */
        String trxLdn;
        ManagedObject trxMo;
        trxLdn = String.format("%s,Trx=1", sectorMoLdn);
        setTestInfo("Create Trx MO with LDN " + trxLdn);

        String arfcnMinArray[] = {"0", "1", "511","512","511","512","127","128","974","0"};  // Not Valid, Valid, ...
        String arfcnMaxArray[] = {"124", "125", "810","811","885","886","251","252","1023","125"};  // Valid, Not Valid, ...
        String frequencyBandArray[] = {"0","0","2","2","3","3","5","5","8","8"};
        
        // Create MO 
        for(int n = 0; n < 10; ++n)
        {
        	trxMo = new ManagedObject(trxLdn);
        	trxMo.addAttribute(new ManagedObjectValueAttribute("txPower", "10"));
        	trxMo.addAttribute(new ManagedObjectValueAttribute("arfcnMin", arfcnMinArray[n]));
        	trxMo.addAttribute(new ManagedObjectValueAttribute("arfcnMax", arfcnMaxArray[n]));
        	trxMo.addAttribute(new ManagedObjectValueAttribute("frequencyBand", frequencyBandArray[n]));
        	trxMo.addAttribute(new ManagedObjectValueStringAttribute(MomHelper.SECTOR_EQUIP_FUNC_REF, momHelper.getSectorEquipmentFunctionLdn()));

        	setTestStepBegin("Create a Trx with a mismatch between ARFCN and frequency band, shall fail");
        	try {
        		momHelper.createManagedObject(trxMo);
        		fail("Successfully created " + trxLdn + ", whith a mismatch between ARFCN and frequency band.");
        	} catch (GracefulDisconnectFailedException e) {
        		if (!e.getLocalizedMessage().contains("GratInvalidArfcnValueException"))
        		{
        			fail("The caught excption did not contain the expected error. Caught exception: " + e.getLocalizedMessage());
        		}
        		setTestInfo("Got the expected exception when trying to create Trx with a mismatch between ARFCN and frequency band");
        	}
        }
        	
        setTestStepEnd();      
    }
}
