package com.ericsson.msran.test.grat.restartrecover;

import java.util.ArrayList;
import java.util.List;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import se.ericsson.jcat.fw.annotations.Setup;
import se.ericsson.jcat.fw.annotations.Teardown;

import com.ericsson.abisco.clientlib.servers.OM_G31R01;
import com.ericsson.commonlibrary.managedobjects.ManagedObject;
import com.ericsson.commonlibrary.restorestack.RestoreCommand;
import com.ericsson.msran.g2.annotations.TestInfo;
import com.ericsson.msran.test.grat.testhelpers.AbisHelper;
import com.ericsson.msran.test.grat.testhelpers.AbiscoConnection;
import com.ericsson.msran.test.grat.testhelpers.CliCommands;
import com.ericsson.msran.test.grat.testhelpers.MomHelper;
import com.ericsson.msran.test.grat.testhelpers.NodeStatusHelper;
import com.ericsson.msran.test.grat.testhelpers.restorecommands.DeleteMoCommand;
import com.ericsson.msran.test.grat.testhelpers.restorecommands.LockDeleteMoCommand;

import com.ericsson.msran.jcat.TestBase;

/**
 * @id NodeUC522
 * 
 * @name RecoverGsmMo
 * 
 * @author GRAT 2014
 * 
 * @created 2014-12-15
 * 
 * @description Verifies that the GRAT MOs are recovered after a node restart.
 * 
 * @revision efrenil 2014-12-15 first version
 * 
 */

public class RecoverGsmMo extends TestBase {   
    private MomHelper momHelper;
    private AbiscoConnection abisco;
    private NodeStatusHelper nodeStatus;
    private AbisHelper abisHelper;
    
    private int trxId_0 = 0;
    private String GsmSectorLdn_0 = "ManagedElement=1,BtsFunction=1,GsmSector=0";
    private String AbisIpLdn_0 = GsmSectorLdn_0 + ",AbisIp=0";
    private String TrxLdn_0_in_GsmSector_0 = GsmSectorLdn_0 + ",Trx=" + trxId_0;
    private int tg_0 = 0;
    private String connection_name_0 = "host_0";
    
    private CliCommands cliCommands;

    /**
     * Description of test case for test reporting
     */
    @TestInfo(
            tcId = "UC522.A1",
            slogan = "Recover GSM MOs after node restart",
            requirementDocument = "1/00651-FCP 130 1402",
            requirementRevision = "PC5",
            requirementLinkTested = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff87c1d941?docno=1/00651-FCP1301402Uen&format=excel8book",
            requirementLinkLatest = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff86a2af3d?docno=1/00651-FCP1301402Uen&action=current&format=excel8book",
            requirementIds = { "10565-0334/19417[A][APPR]" },
            verificationStatement = "This test case verifies UC522.A1 for the following MO classes BtsFunction, GsmSector, AbisIp, and Trx",
            testDescription = "Create and unlocks MO:s. Establish OML links to the GsmSector, and OML+RSL links to the Trx. Reboot node and see that MO:s are recreated and that OML and RSL links can be reestablished.",
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
        setTestInfo("Save current pid to compare after test execution.");
        abisHelper = new AbisHelper();
        cliCommands = new CliCommands();
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
    
    private void checkGsmSectorMoAttributeAfterCreateNoLinks(String gsmSectorLdn) {
    	assertEquals("GsmSector MO attributes did not reach expected values", "", momHelper.checkGsmSectorMoAttributeAfterCreateNoLinks(gsmSectorLdn, 5));
    }
    
    private void checkGsmSectorMoAttributeAfterCreateLinkEstablished(String gsmSectorLdn) {
    	assertEquals("GsmSector MO attributes did not reach expected values", "", momHelper.checkGsmSectorMoAttributeAfterCreateLinksEstablished(gsmSectorLdn, 5));
    }
    
    private void checkGsmSectorMoAttributeAfterCreateLinkEstablishedAllMosStartedAoAtEnabled(String gsmSectorLdn) {
    	assertEquals("GsmSector MO attributes did not reach expected values", "", momHelper.checkGsmSectorMoAttributeAfterAllMosStartedAoAtEnabled(gsmSectorLdn, 5));
    }        
   
    private void checkAbisIpMoAttributeAfterCreateUnlockBscConnected(String abisIpLdn) {
    	assertEquals("AbisIp MO attributes did not reach expected values", "", momHelper.checkAbisIpMoAttributeAfterUnlockBscConnected(abisIpLdn, abisco.getBscIpAddress(), 30));
    }
       
    private void checkTrxMoAttributeAfterCreateUnlockNoLinks(String trxLdn) {
    	assertEquals("Trx MO attributes did not reach expected values", "", momHelper.checkTrxMoAttributeAfterUnlockNoLinks(trxLdn, 5));
    }
    
    private void checkTrxMoAttributeAfterCreateUnlockLinksEstablished(String trxLdn) {
    	assertEquals("Trx MO attributes did not reach expected values", "", momHelper.checkTrxMoAttributeAfterUnlockLinksEstablished(trxLdn, 5));
    }
       
    private void sendAoATEnable(int tgId) throws InterruptedException  {
    	assertTrue("Configuration signature has not been calculated (value = 0) ", 0 != abisHelper.enableRequest(OM_G31R01.Enums.MOClass.AT, tgId).getConfigurationSignature().intValue());
    }
 
    private void checkMoStatusAfterUnlockedAbisIpAndTrxAndConnectedTG(String gsmSectorLdn, String abisIpLdn, String trxLdn0) {
    	checkGsmSectorMoAttributeAfterCreateNoLinks(gsmSectorLdn);
    	checkAbisIpMoAttributeAfterCreateUnlockBscConnected(abisIpLdn);
    	checkTrxMoAttributeAfterCreateUnlockNoLinks(trxLdn0);
    }
    
    private void checkMoStatusAfterUnlockedAbisIpAndTrxAndEstablishedScfOml(String gsmSectorLdn, String abisIpLdn, String trxLdn0) {
    	checkGsmSectorMoAttributeAfterCreateLinkEstablished(gsmSectorLdn);
    	checkAbisIpMoAttributeAfterCreateUnlockBscConnected(abisIpLdn);
    	checkTrxMoAttributeAfterCreateUnlockNoLinks(trxLdn0);
    }
       
    private void checkMoStatusAfterUnlockedAbisIpAndTrxAndMosSoScfAoTfStartedAoAtEnabled(String gsmSectorLdn, String abisIpLdn, String trxLdn0) {
    	checkGsmSectorMoAttributeAfterCreateLinkEstablishedAllMosStartedAoAtEnabled(gsmSectorLdn);
    	checkAbisIpMoAttributeAfterCreateUnlockBscConnected(abisIpLdn);
    	checkTrxMoAttributeAfterCreateUnlockNoLinks(trxLdn0);
    }
       
    private void checkMoStatusAfterUnlockedAbisIpAndTrxAndMosSoScfAoTfStartedAoAtEnabledAllLinksUp(String gsmSectorLdn, String abisIpLdn, String trxLdn0) {
    	checkGsmSectorMoAttributeAfterCreateLinkEstablishedAllMosStartedAoAtEnabled(gsmSectorLdn);
    	checkAbisIpMoAttributeAfterCreateUnlockBscConnected(abisIpLdn);
    	checkTrxMoAttributeAfterCreateUnlockLinksEstablished(trxLdn0);
    }
    
    private void createUnlockMosForOneSector(
    		String sectorLdn,
    		String abisIpLdn,
    		String trx0ldn,
    		String connection_name) {
    	// specify which MOs that shall be created together, and their parameter values
    	List<ManagedObject> createMos = new ArrayList<ManagedObject>();

    	setTestInfo("Build MO objects");
    	ManagedObject gsmSectorMo = momHelper.buildGsmSectorMo(sectorLdn);
    	createMos.add(gsmSectorMo);
    	RestoreCommand restoreGsmSectorMoCmd = new DeleteMoCommand(gsmSectorMo.getLocalDistinguishedName());
    	momHelper.addRestoreCmd(restoreGsmSectorMoCmd);

    	ManagedObject abisIpMo = momHelper.buildAbisIpMo(
    			connection_name,
    			abisco.getBscIpAddress(),
    			abisIpLdn,
    			MomHelper.TNA_IP_LDN,
    			MomHelper.UNLOCKED);
    	createMos.add(abisIpMo);
    	RestoreCommand restoreAbisIpMoCmd = new LockDeleteMoCommand(abisIpMo.getLocalDistinguishedName());
    	momHelper.addRestoreCmd(restoreAbisIpMoCmd);

    	ManagedObject trxMo =  momHelper.buildTrxMo(trx0ldn, MomHelper.UNLOCKED, momHelper.getSectorEquipmentFunctionLdn());
    	createMos.add(trxMo);
    	RestoreCommand restoreTrxMoCmd = new LockDeleteMoCommand(trxMo.getLocalDistinguishedName());
    	momHelper.addRestoreCmd(restoreTrxMoCmd);
  
    	setTestInfo("Create all MOs");
    	momHelper.createSeveralMoInOneTx(createMos);    	
    }
        
    private void establishScfOmlLinks() throws InterruptedException {
    	establishScfOmlLinkForTg(tg_0);
    }
    
    private void establishAllLinks() throws InterruptedException {
    	establishAllLinksForTg(tg_0);
    }
    
    private void establishScfOmlLinkForTg(int tgId) throws InterruptedException {
    	abisco.establishLinks(tgId, false, trxId_0);
    }
    
    private void establishAllLinksForTg(int tgId) throws InterruptedException {
    	abisco.establishLinks(tgId, true, trxId_0);
    }
	
	private void startSoScfAoTfAoAtSendAtConfigAndAtEnable(int tgId) throws InterruptedException {
		abisHelper.sendStartToAllMoVisibleInGsmSector(tgId);
		abisHelper.sendAoAtConfigPreDefBundling(tgId);
		sendAoATEnable(tgId);
	}
	
	

  /**
  * recoverAllGratMosReestablishOmlAndRsl
  * @description Create and unlock (if applicable) one instance of every GRAT MO Class (BtsFunction, GsmSector, AbisIp, and Trx).
  *              The SCF OML and TRX OML+RSL are established for GsmSector and Trx
  *              The node is rebooted.
  *              Verify that the MOs are recreated and that GRAT reestablishes the transport.
  *              The SCF OML and TRX OML+RSL are again established for GsmSector and Trx
  *				 GRAT MO:s are locked and deleted.
  *				 Verifies UC522.A1
  * 
  * @param testId - unique identifier of the test case
  * @param description
  */
 @Test(timeOut = 850000)
 @Parameters({ "testId", "description" })
 public void recoverAllGratMosReestablishOmlAndRsl(String testId, String description) throws InterruptedException {
     setTestCase(testId, description);
     
     setTestStepBegin("TN");
     if (!momHelper.isTnConfigured())
     	momHelper.configureTn(abisco.getBscIpAddress());
     
     setTestStepBegin("********* Pre-test: Create and Connect TG and Define Cell");
     abisco.setupAbisco(false);
     setTestStepEnd();
     
 	 if (!momHelper.isBtsFunctionCreated()) {
 		 setTestInfo("Precondition: Create BtsFunction MO");
 		 momHelper.createBtsFunctionMo(MomHelper.BTS_FUNCTION_LDN);
 	 }     
     
     setTestStepBegin("********* Create and unlock GsmSector, AbisIp, and Trx MO:s");
     createUnlockMosForOneSector(GsmSectorLdn_0, AbisIpLdn_0, TrxLdn_0_in_GsmSector_0, connection_name_0);
     setTestStepEnd();
     
  	 setTestStepBegin("********* Check MO status of GsmSector, AbisIp, and Trx after MO creation, unlock, and connect TG. GsmSector=" + GsmSectorLdn_0);
     checkMoStatusAfterUnlockedAbisIpAndTrxAndConnectedTG(GsmSectorLdn_0, AbisIpLdn_0, TrxLdn_0_in_GsmSector_0);
     setTestStepEnd();

     // Establish Oml link for Scf
     setTestStepBegin("********* Establish OML link for Scf");
     establishScfOmlLinks();
     setTestStepEnd();

     setTestStepBegin("********* Check MO status of GsmSector, AbisIp, and Trx after MO creation, unlock, connect TG, and SCF OML established. GsmSector=" + GsmSectorLdn_0);
     checkMoStatusAfterUnlockedAbisIpAndTrxAndEstablishedScfOml(GsmSectorLdn_0, AbisIpLdn_0, TrxLdn_0_in_GsmSector_0);
     setTestStepEnd();

     setTestStepBegin("********* Start SO Scf, AO Tf, and AO AT. Send AT Config and AT Enabled");
     startSoScfAoTfAoAtSendAtConfigAndAtEnable(tg_0);
     setTestStepEnd();     
	
     setTestStepBegin("********* Check MO status of GsmSector, AbisIp, and Trx after MO creation, unlock, connect TG, SCF OML UP, So Scf, AO TF Started, and Ao AT enabled. GsmSector=" + GsmSectorLdn_0);
     checkMoStatusAfterUnlockedAbisIpAndTrxAndMosSoScfAoTfStartedAoAtEnabled(GsmSectorLdn_0, AbisIpLdn_0, TrxLdn_0_in_GsmSector_0);
     setTestStepEnd();

     // Establish Oml link for Scf and OML+RSL for Trx
     setTestStepBegin("********* Establish OML link for Scf and OML+RSL for Trx");
     establishAllLinks();
     setTestStepEnd();

     setTestStepBegin("********* Check MO status of GsmSector, AbisIp, and Trx after MO creation, unlock, connect TG, all links established, So Scf, AO TF Started, and Ao AT enabled. GsmSector=" + GsmSectorLdn_0);
     checkMoStatusAfterUnlockedAbisIpAndTrxAndMosSoScfAoTfStartedAoAtEnabledAllLinksUp(GsmSectorLdn_0, AbisIpLdn_0, TrxLdn_0_in_GsmSector_0);
     setTestStepEnd();
	
     // The OML and RSL links are established, time to restart the node
     setTestStepBegin("********* Time to reboot the node.");
     assertTrue("Node didnt come alive after reboot...", cliCommands.rebootAndWaitForNodeNoCleanup());
     setTestStepEnd();
	
  	 setTestStepBegin("********* Check MO status of GsmSector, AbisIp, and Trx after MO creation, unlock, and connect TG. GsmSector=" + GsmSectorLdn_0);
     checkMoStatusAfterUnlockedAbisIpAndTrxAndConnectedTG(GsmSectorLdn_0, AbisIpLdn_0, TrxLdn_0_in_GsmSector_0);
     setTestStepEnd();
     
     // Establish Oml link for Scf
     setTestStepBegin("********* Establish OML link for Scf");
     establishScfOmlLinks();
     setTestStepEnd();

     setTestStepBegin("********* Check MO status of GsmSector, AbisIp, and Trx after MO creation, unlock, connect TG, and SCF OML established. GsmSector=" + GsmSectorLdn_0);
     checkMoStatusAfterUnlockedAbisIpAndTrxAndEstablishedScfOml(GsmSectorLdn_0, AbisIpLdn_0, TrxLdn_0_in_GsmSector_0);
     setTestStepEnd();

     setTestStepBegin("********* Start SO Scf, AO Tf, and AO AT. Send AT Config and AT Enabled");
     startSoScfAoTfAoAtSendAtConfigAndAtEnable(tg_0);
     setTestStepEnd();     
	
     setTestStepBegin("********* Check MO status of GsmSector, AbisIp, and Trx after MO creation, unlock, connect TG, SCF OML UP, So Scf, AO TF Started, and Ao AT enabled. GsmSector=" + GsmSectorLdn_0);
     checkMoStatusAfterUnlockedAbisIpAndTrxAndMosSoScfAoTfStartedAoAtEnabled(GsmSectorLdn_0, AbisIpLdn_0, TrxLdn_0_in_GsmSector_0);
     setTestStepEnd();

     // Establish Oml link for Scf and OML+RSL for Trx
     setTestStepBegin("********* Establish OML link for Scf and OML+RSL for Trx");
     establishAllLinks();
     setTestStepEnd();

     setTestStepBegin("********* Check MO status of GsmSector, AbisIp, and Trx after MO creation, unlock, connect TG, all links established, So Scf, AO TF Started, and Ao AT enabled. GsmSector=" + GsmSectorLdn_0);
     checkMoStatusAfterUnlockedAbisIpAndTrxAndMosSoScfAoTfStartedAoAtEnabledAllLinksUp(GsmSectorLdn_0, AbisIpLdn_0, TrxLdn_0_in_GsmSector_0);
     setTestStepEnd();
 }
}   
      
      