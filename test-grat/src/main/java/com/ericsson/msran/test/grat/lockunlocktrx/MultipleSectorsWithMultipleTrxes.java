package com.ericsson.msran.test.grat.lockunlocktrx;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import se.ericsson.jcat.fw.annotations.Setup;
import se.ericsson.jcat.fw.annotations.Teardown;

import com.ericsson.abisco.clientlib.servers.CMDHAND;
import com.ericsson.abisco.clientlib.servers.OM_G31R01;
import com.ericsson.abisco.clientlib.servers.CMDHAND.BundlingProfile1;
import com.ericsson.abisco.clientlib.servers.CMDHAND.BundlingProfile2;
import com.ericsson.abisco.clientlib.servers.CMDHAND.BundlingProfile3;
import com.ericsson.abisco.clientlib.servers.CMDHAND.BundlingProfile4;
import com.ericsson.abisco.clientlib.servers.CMDHAND.BundlingProfile5;
import com.ericsson.abisco.clientlib.servers.CMDHAND.BundlingProfiles;
import com.ericsson.commonlibrary.managedobjects.ManagedObject;
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
 * @name MultipleSectorsWithMultipleTrxes
 * 
 * @author GRAT 2014
 * 
 * @created 2014-08-21
 * 
 * @description Verifies transport functionality when using several GsmSectors with multiple Trx:es in each GsmSector.
 * 
 * @revision efrenil 2014-08-21 first version
 * 
 */

public class MultipleSectorsWithMultipleTrxes extends TestBase {   
    private MomHelper momHelper;
    private AbiscoConnection abisco;
    private NodeStatusHelper nodeStatus;
    private AbisHelper abisHelper;
    
    private int trxId_0 = 0;
    private int trxId_1 = 1;
    private String GsmSectorLdn_0 = "ManagedElement=1,BtsFunction=1,GsmSector=0";
    private String GsmSectorLdn_1 = "ManagedElement=1,BtsFunction=1,GsmSector=1";
    private String AbisIpLdn_0 = GsmSectorLdn_0 + ",AbisIp=0";
    private String AbisIpLdn_1 = GsmSectorLdn_1 + ",AbisIp=1";
    private String TrxLdn_0_in_GsmSector_0 = GsmSectorLdn_0 + ",Trx=" + trxId_0;
    private String TrxLdn_1_in_GsmSector_0 = GsmSectorLdn_0 + ",Trx=" + trxId_1;
    private String TrxLdn_0_in_GsmSector_1 = GsmSectorLdn_1 + ",Trx=" + trxId_0;
    private String TrxLdn_1_in_GsmSector_1 = GsmSectorLdn_1 + ",Trx=" + trxId_1;
    private int tg_0 = 0;
    private int tg_1 = 1;
    private String connection_name_0 = "host_0";
    private String connection_name_1 = "host_1";

    

    /**
     * Description of test case for test reporting
     */
    @TestInfo(
            tcId = "UC419.N, UC420.N, UC424.N, UC425.N, UC426.N, UC428.N, UC457.N, UC458.N , UC459.N, UC461.N",
            slogan = "Multiple GsmSectors with multiple Trx:es",
            requirementDocument = "1/00651-FCP 130 1402",
            requirementRevision = "PC5",
            requirementLinkTested = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff87c1d941?docno=1/00651-FCP1301402Uen&format=excel8book",
            requirementLinkLatest = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff86a2af3d?docno=1/00651-FCP1301402Uen&action=current&format=excel8book",
            requirementIds = { "10565-0334/19417[A][APPR]" },
            verificationStatement = "Partially verifies (not checking that every MO state is correct after every step) UC419.N, UC420.N, UC424.N, UC425.N, UC426.A4, UC428.N, UC457.N, UC458.N , UC459.N, UC461.N",
            testDescription = "Verifies transport functionality when using several GsmSectors with multiple Trx:es in each GsmSector.",
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
    
    private void checkGsmSectorMoAttributeAfterCreateNoLinks(String gsmSectorLdn) {
    	assertEquals("GsmSector MO attributes did not reach expected values", "", momHelper.checkGsmSectorMoAttributeAfterCreateNoLinks(gsmSectorLdn, 30));
    }
    
    private void checkGsmSectorMoAttributeAfterCreateLinkEstablished(String gsmSectorLdn) {
    	assertEquals("GsmSector MO attributes did not reach expected values", "", momHelper.checkGsmSectorMoAttributeAfterCreateLinksEstablished(gsmSectorLdn, 30));
    }

/* Unused
    private void checkGsmSectorMoAttributeAfterCreateLinkEstablishedAllMosStarted(String gsmSectorLdn) {
    	assertEquals("GsmSector MO attributes did not reach expected values", "", momHelper.checkGsmSectorMoAttributeAfterAllMosStarted(gsmSectorLdn, 10));
    }    
*/
    
    private void checkGsmSectorMoAttributeAfterCreateLinkEstablishedAllMosStartedAoAtEnabled(String gsmSectorLdn) {
    	assertEquals("GsmSector MO attributes did not reach expected values", "", momHelper.checkGsmSectorMoAttributeAfterAllMosStartedAoAtEnabled(gsmSectorLdn, 30));
    }        

    private void checkGsmSectorMoAttributeAfterAllMosStartedAoAtEnabledNoLink(String gsmSectorLdn) {
    	assertEquals("GsmSector MO attributes did not reach expected values", "", momHelper.checkGsmSectorMoAttributeAfterAllMosStartedAoAtEnabledNoLinks(gsmSectorLdn, 30));
    }        
    
    private void checkGsmSectorMoAttributeAfterAllMosStartedAoAtResetNoLink(String gsmSectorLdn) {
    	assertEquals("GsmSector MO attributes did not reach expected values", "", momHelper.checkGsmSectorMoAttributeAfterAllMosStartedAoAtResetNoLinks(gsmSectorLdn, 30));
    }        
    
    private void checkAbisIpMoAttributeAfterLock(String abisIpLdn) {
    	assertEquals("AbisIp MO attributes did not reach expected values", "", momHelper.checkAbisIpMoAttributeAfterLock(abisIpLdn, 30));
    }
    
    private void checkAbisIpMoAttributeAfterCreateUnlockBscConnected(String abisIpLdn) {
    	assertEquals("AbisIp MO attributes did not reach expected values", "", momHelper.checkAbisIpMoAttributeAfterUnlockBscConnected(abisIpLdn, abisco.getBscIpAddress(), 30));
    }
    
    private void checkAbisIpMoAttributeAfterCreateUnlockBscDisconnected(String abisIpLdn) {
    	assertEquals("AbisIp MO attributes did not reach expected values", "", momHelper.checkAbisIpMoAttributeAfterUnlockBscDisconnected(abisIpLdn, 30));
    }
    
    private void checkTrxMoAttributeAfterLock(String trxLdn) {
    	assertEquals("Trx MO attributes did not reach expected values", "", momHelper.checkTrxMoAttributeAfterLock(trxLdn, 30));
    }
    
    private void checkTrxMoAttributeAfterCreateUnlockNoLinks(String trxLdn) {
    	assertEquals("Trx MO attributes did not reach expected values", "", momHelper.checkTrxMoAttributeAfterUnlockNoLinks(trxLdn, 30));
    }
    
    private void checkTrxMoAttributeAfterCreateUnlockLinksEstablished(String trxLdn) {
    	assertEquals("Trx MO attributes did not reach expected values", "", momHelper.checkTrxMoAttributeAfterUnlockLinksEstablished(trxLdn, 30));
    }
    
    private void checkThatGsmSectorAbisIpTrxMoAreDeleted(String gsmSectorLdn, String abisIpLdn, String trxLdn0, String trxLdn1) {
    	assertFalse(momHelper.checkMoExist(trxLdn0));
    	assertFalse(momHelper.checkMoExist(trxLdn1));
    	assertFalse(momHelper.checkMoExist(abisIpLdn));
    	assertFalse(momHelper.checkMoExist(gsmSectorLdn));  	
    }
    
    private void sendAoATEnable(int tgId) throws InterruptedException  {
    	assertTrue("Configuration signature has not been calculated (value = 0) ", 0 != abisHelper.enableRequest(OM_G31R01.Enums.MOClass.AT, tgId).getConfigurationSignature().intValue());
    }
 
    private void checkMoStatusAfterUnlockedAbisIpAndTrxAndConnectedTG(String gsmSectorLdn, String abisIpLdn, String trxLdn0, String trxLdn1) {
    	checkGsmSectorMoAttributeAfterCreateNoLinks(gsmSectorLdn);
    	checkAbisIpMoAttributeAfterCreateUnlockBscConnected(abisIpLdn);
    	checkTrxMoAttributeAfterCreateUnlockNoLinks(trxLdn0);
    	checkTrxMoAttributeAfterCreateUnlockNoLinks(trxLdn1);
    }
    
    private void checkMoStatusAfterUnlockedAbisIpAndTrxAndEstablishedScfOml(String gsmSectorLdn, String abisIpLdn, String trxLdn0, String trxLdn1) {
    	checkGsmSectorMoAttributeAfterCreateLinkEstablished(gsmSectorLdn);
    	checkAbisIpMoAttributeAfterCreateUnlockBscConnected(abisIpLdn);
    	checkTrxMoAttributeAfterCreateUnlockNoLinks(trxLdn0);
    	checkTrxMoAttributeAfterCreateUnlockNoLinks(trxLdn1);
    }

/* Unused
    private void checkMoStatusAfterUnlockedAbisIpAndTrxAndMosSoScfAoTfAoAtStarted(String gsmSectorLdn, String abisIpLdn, String trxLdn0, String trxLdn1) {
    	checkGsmSectorMoAttributeAfterCreateLinkEstablishedAllMosStarted(gsmSectorLdn);
    	checkAbisIpMoAttributeAfterCreateUnlockBscConnected(abisIpLdn);
    	checkTrxMoAttributeAfterCreateUnlockNoLinks(trxLdn0);
    	checkTrxMoAttributeAfterCreateUnlockNoLinks(trxLdn1);    
    }
*/
    
    private void checkMoStatusAfterUnlockedAbisIpAndTrxAndMosSoScfAoTfStartedAoAtEnabled(String gsmSectorLdn, String abisIpLdn, String trxLdn0, String trxLdn1) {
    	checkGsmSectorMoAttributeAfterCreateLinkEstablishedAllMosStartedAoAtEnabled(gsmSectorLdn);
    	checkAbisIpMoAttributeAfterCreateUnlockBscConnected(abisIpLdn);
    	checkTrxMoAttributeAfterCreateUnlockNoLinks(trxLdn0);
    	checkTrxMoAttributeAfterCreateUnlockNoLinks(trxLdn1);    
    }
    
    private void checkMoStatusAfterUnlockedAbisIpAndTrxAndDisconnectedTG(String gsmSectorLdn, String abisIpLdn, String trxLdn0, String trxLdn1) {
    	checkGsmSectorMoAttributeAfterAllMosStartedAoAtEnabledNoLink(gsmSectorLdn);
    	checkAbisIpMoAttributeAfterCreateUnlockBscDisconnected(abisIpLdn);
    	checkTrxMoAttributeAfterCreateUnlockNoLinks(trxLdn0);
    	checkTrxMoAttributeAfterCreateUnlockNoLinks(trxLdn1);
    }
    
    private void checkMoStatusAfterUnlockedAbisIpAndTrxAndDisconnectedReconnectedTG(String gsmSectorLdn, String abisIpLdn, String trxLdn0, String trxLdn1) {
    	checkGsmSectorMoAttributeAfterAllMosStartedAoAtEnabledNoLink(gsmSectorLdn);
    	checkAbisIpMoAttributeAfterCreateUnlockBscConnected(abisIpLdn);
    	checkTrxMoAttributeAfterCreateUnlockNoLinks(trxLdn0);
    	checkTrxMoAttributeAfterCreateUnlockNoLinks(trxLdn1);
    }
    
    private void checkMoStatusAfterUnlockedAbisIpAndTrxAndMosSoScfAoTfStartedAoAtEnabledAllLinksUp(String gsmSectorLdn, String abisIpLdn, String trxLdn0, String trxLdn1) {
    	checkGsmSectorMoAttributeAfterCreateLinkEstablishedAllMosStartedAoAtEnabled(gsmSectorLdn);
    	checkAbisIpMoAttributeAfterCreateUnlockBscConnected(abisIpLdn);
    	checkTrxMoAttributeAfterCreateUnlockLinksEstablished(trxLdn0);
    	checkTrxMoAttributeAfterCreateUnlockLinksEstablished(trxLdn1);    
    }
    
    private void checkMoStatusAfterSoScfAoTfStartedAtEnabledAbisIpAndTrxLocked(String gsmSectorLdn, String abisIpLdn, String trxLdn0, String trxLdn1) {
    	//checkGsmSectorMoAttributeAfterAllMosStartedAoAtEnabledNoLink(gsmSectorLdn);
    	checkGsmSectorMoAttributeAfterAllMosStartedAoAtResetNoLink(gsmSectorLdn);
        checkAbisIpMoAttributeAfterLock(abisIpLdn); // efrenil
        checkTrxMoAttributeAfterLock(trxLdn0);
        checkTrxMoAttributeAfterLock(trxLdn1);    	
    }
    
    private void checkMoStatusAfterMosUnlockedSoScfAoTfStartedAtEnabledMosLockedMosUnlocked(String gsmSectorLdn, String abisIpLdn, String trxLdn0, String trxLdn1) {
    	//checkGsmSectorMoAttributeAfterAllMosStartedAoAtEnabledNoLink(gsmSectorLdn);
    	checkGsmSectorMoAttributeAfterAllMosStartedAoAtResetNoLink(gsmSectorLdn);
    	checkAbisIpMoAttributeAfterCreateUnlockBscConnected(abisIpLdn);
    	checkTrxMoAttributeAfterCreateUnlockNoLinks(trxLdn0);
    	checkTrxMoAttributeAfterCreateUnlockNoLinks(trxLdn1);    	
    }

    private void createUnlockMosForOneSector(
    		String sectorLdn,
    		String abisIpLdn,
    		String trx0ldn,
    		String trx1ldn,
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
    	
    	ManagedObject trxMo1 =  momHelper.buildTrxMo(trx1ldn, MomHelper.UNLOCKED, momHelper.getSectorEquipmentFunctionLdn());
    	createMos.add(trxMo1);
    	RestoreCommand restoreTrxMoCmd1 = new LockDeleteMoCommand(trxMo1.getLocalDistinguishedName());
    	momHelper.addRestoreCmd(restoreTrxMoCmd1);

    	setTestInfo("Create all MOs");
    	momHelper.createSeveralMoInOneTx(createMos);
    	
    	checkTrxMoAttributeAfterCreateUnlockNoLinks(trx1ldn);
    }
    
    private void unlockMosForOneSector(    		
    		String abisIpLdn,
    		String trx0ldn,
    		String trx1ldn) {
        momHelper.unlockMo(abisIpLdn);
        momHelper.unlockMo(trx0ldn);
        momHelper.unlockMo(trx1ldn);
    }
    
    private void lockMosForOneSector(    		
    		String abisIpLdn,
    		String trx0ldn,
    		String trx1ldn) {
    	momHelper.lockMo(trx0ldn);
    	momHelper.lockMo(trx1ldn);
    	momHelper.lockMo(abisIpLdn);
   	}
    
    
    private void lockAllGratMOs() {
    	lockMosForOneSector(AbisIpLdn_0, TrxLdn_0_in_GsmSector_0, TrxLdn_1_in_GsmSector_0);
    	lockMosForOneSector(AbisIpLdn_1, TrxLdn_0_in_GsmSector_1, TrxLdn_1_in_GsmSector_1);
    }
    
    private void deleteAllGratMOs() {
    	momHelper.deleteMo(TrxLdn_0_in_GsmSector_0);
    	momHelper.deleteMo(TrxLdn_1_in_GsmSector_0);
    	momHelper.deleteMo(TrxLdn_0_in_GsmSector_1);
    	momHelper.deleteMo(TrxLdn_1_in_GsmSector_1);
    	momHelper.deleteMo(AbisIpLdn_0);
    	momHelper.deleteMo(AbisIpLdn_1); 
    	momHelper.deleteMo(GsmSectorLdn_0);
    	momHelper.deleteMo(GsmSectorLdn_1);
    }
    
    private void establishScfOmlLinks() throws InterruptedException {
    	establishScfOmlLinkForTg(tg_0);
    	establishScfOmlLinkForTg(tg_1);
    }
    
    private void establishAllLinks() throws InterruptedException {
    	establishAllLinksForTg(tg_0);
    	establishAllLinksForTg(tg_1);
    }
    
    private void establishScfOmlLinkForTg(int tgId) throws InterruptedException {
    	abisco.establishLinks(tgId, false, trxId_0);
    }
    
    private void establishAllLinksForTg(int tgId) throws InterruptedException {
    	abisco.establishLinks(tgId, true, trxId_0);
    	abisco.establishLinks(tgId, true, trxId_1);
    }

/* Unused
	private void startSoScfAoTfAoAt() throws InterruptedException {
		abisHelper.sendStartToAllMoVisibleInGsmSector(tg_0);
		abisHelper.sendStartToAllMoVisibleInGsmSector(tg_1);
	}
	
	private void sendAoAtConfig() throws InterruptedException {
		abisHelper.sendAoAtConfigPreDefBundling(tg_0);
		abisHelper.sendAoAtConfigPreDefBundling(tg_1);
	}
*/
    
	private void startSoScfAoTfAoAtSendAtConfigAndAtEnable(int tgId) throws InterruptedException {
		abisHelper.sendStartToAllMoVisibleInGsmSector(tgId);
		abisHelper.sendAoAtConfigPreDefBundling(tgId);
		sendAoATEnable(tgId);
	}
	    
/* Unused
    private void establishAllLinksAsync() {
		abisco.establishLinksAsync(tg_0, true, trxId_0);
		abisco.establishLinksAsync(tg_0, true, trxId_1);
		abisco.establishLinksAsync(tg_1, true, trxId_0);
		abisco.establishLinksAsync(tg_1, true, trxId_1);   	
    }
*/ 
    
    private void disconnectAndDeleteTg(int tgId) {
    	abisco.disconnectTG(tgId);
    	abisco.deleteTG(tgId);
    }
    
    private void createTgsAndCells() {
    	try {
    		// create two TG with two TRX
    		abisco.createTgPreDefBundling(tg_0, connection_name_0, 2, false);
    		abisco.defineCell(tg_0, 2);
    		abisco.createTgPreDefBundling(tg_1, connection_name_1, 2, false);
    		abisco.defineCell(tg_1, 2);
    	} catch (InterruptedException e) {
    		setTestInfo("Caught InterruptedException");
    		e.printStackTrace();
    	}
    }
    
    private void createTgAndCellFiveBundlingProfiles(int tgId) {
        String connectionName = "host_" + tgId;
        // Create bundling profiles
        BundlingProfile1 bp1 = new CMDHAND.BundlingProfile1();
        bp1.setSAPIList(Arrays.asList(0));
        bp1.setCRC(CMDHAND.Enums.CRC.UseCRC);
        bp1.setDSCP(51);
        bp1.setBundlingSize(1465);

        BundlingProfile2 bp2 = new CMDHAND.BundlingProfile2();
        bp2.setSAPIList(Arrays.asList(10));
        bp2.setCRC(CMDHAND.Enums.CRC.UseCRC);
        bp2.setDSCP(52);
        bp2.setBundlingSize(1465);

        BundlingProfile3 bp3 = new CMDHAND.BundlingProfile3();
        bp3.setSAPIList(Arrays.asList(11));
        bp3.setCRC(CMDHAND.Enums.CRC.UseCRC);
        bp3.setDSCP(53);
        bp3.setBundlingSize(1465);

        BundlingProfile4 bp4 = new CMDHAND.BundlingProfile4();
        bp4.setSAPIList(Arrays.asList(12));
        bp4.setCRC(CMDHAND.Enums.CRC.UseCRC);
        bp4.setDSCP(54);
        bp4.setBundlingSize(1465);
        
        BundlingProfile5 bp5 = new CMDHAND.BundlingProfile5();
        bp5.setSAPIList(Arrays.asList(62));
        bp5.setCRC(CMDHAND.Enums.CRC.UseCRC);
        bp5.setDSCP(55);
        bp5.setBundlingSize(1465);
        
        BundlingProfiles bundlingProfiles = new BundlingProfiles();
        bundlingProfiles.setBundlingProfileCount(5);
        bundlingProfiles.setBundlingProfile1(bp1);
        bundlingProfiles.setBundlingProfile2(bp2);
        bundlingProfiles.setBundlingProfile3(bp3);
        bundlingProfiles.setBundlingProfile4(bp4);
        bundlingProfiles.setBundlingProfile5(bp5);
        try {
            abisco.createTg(tgId, connectionName, 2, bundlingProfiles, false);
            abisco.defineCell(tgId, 2);
        } catch (InterruptedException e) {
            setTestInfo("Caught InterruptedException");
            e.printStackTrace();
        }
    }

    /**
     * Used together with #createTgAndCellFiveBundlingProfiles(int) to configure AT
     * with a correct configuration for these profiles
     */
    private void atConfigRequestFiveBundlingProfiles(int tgId) {
        AbisHelper.TransportProfile transportProfile = new AbisHelper.TransportProfile();
        
        List<Integer> allSapis = Arrays.asList(0, 10, 11, 12, 62);
        for (int i = 0; i < 5; ++i) {
            List<Integer> sapiList = Arrays.asList(allSapis.get(i));
            int bundlingTimeUl = 1;
            int bundlingMaxPayloadSizeUl = 1465;
            int dscpUl = 51+i;
            int overloadThreashold = 0;
            int overloadReportInterval = 1;
            boolean useCrc = true;
            transportProfile.addBp(new AbisHelper.TransportProfile.BundlingProfile(
                    sapiList, bundlingTimeUl, bundlingMaxPayloadSizeUl, useCrc, dscpUl, overloadThreashold, overloadReportInterval));            
        }
        try {
            abisHelper.atConfigRequest(transportProfile, tgId);
        } catch (InterruptedException e) {
            fail("Interrupted while executing AT Configuration Request");            
        }
        try {
            abisHelper.enableRequest(OM_G31R01.Enums.MOClass.AT, tgId);
        } catch (InterruptedException e) {
            fail("Interrupted while executing AT Enable Request");  
        }
    }
    
    
    /**
     * multipleGsmSectorsWithMultipleTrxes_1
     * @description Testing transport with two GsmSectors with two Trx:es each.
     *              Grat MO:s are created and unlocked before the TGs are connected.
     *				The SCF OML and TRX OML+RSL are established for both GsmSectors and all Trx:es 
     *    			Both AbisIp and all Trx MO:s are locked and unlocked 
     * 				The SCF OML and TRX OML+RSL are established for both GsmSectors and all Trx:es
     * 				The TGs are disconnected
     * 				GRAT MO:s are locked and deleted.
     * 				The sequence above are looped a couple of times.				
     *              This TC does not completely verify any UC, but the following UCs are involved: 
     *              UC419.N, UC420.N, UC424.N, UC425.N, UC426.A4, UC428.N, UC457.N, UC458.N , UC459.N, UC461.N
     * 
     * @param testId - unique identifier of the test case
     * @param description
     */
    @Test(timeOut = 850000)
    @Parameters({ "testId", "description" })
    public void multipleGsmSectorsWithMultipleTrxes_1(String testId, String description) throws InterruptedException {
        setTestCase(testId, description);
        
        setTestStepBegin("TN");
        if (!momHelper.isTnConfigured())
        	momHelper.configureTn(abisco.getBscIpAddress());
        
    	if (!momHelper.isBtsFunctionCreated()) {
    		setTestInfo("Precondition: Create BtsFunction MO");
    		momHelper.createBtsFunctionMo(MomHelper.BTS_FUNCTION_LDN);
    	}
        
        
        setTestStepBegin("********* Pre-test: Make sure that TG:s are disconnected and deleted");
        disconnectAndDeleteTg(tg_0);
        disconnectAndDeleteTg(tg_1);
        setTestStepEnd();

        setTestStepBegin("********* Pre-test: Start TSS");
        abisco.startTss();
        setTestStepEnd();
        
        setTestStepBegin("********* Pre-test: Create two TGs with two TRX:es each");
        createTgsAndCells();
        setTestStepEnd();
       
        // run the same test a couple of times to see that we clean up
        for (int i = 0 ; i < 2 ; ++i)
        {
        	setTestStepBegin("********* Create all MO:s for first sector");
        	createUnlockMosForOneSector(GsmSectorLdn_0, AbisIpLdn_0, TrxLdn_0_in_GsmSector_0, TrxLdn_1_in_GsmSector_0, connection_name_0);
        	setTestStepEnd();

        	setTestStepBegin("********* Create all MO:s for second sector");
        	createUnlockMosForOneSector(GsmSectorLdn_1, AbisIpLdn_1, TrxLdn_0_in_GsmSector_1, TrxLdn_1_in_GsmSector_1, connection_name_1);
        	setTestStepEnd();
        	
        	// no SO SCF started, so we can not check for any SCF Status Updates here from the create and unlock trx

        	setTestStepBegin("********* Connect tg");
        	abisco.connectTG(tg_0);
        	abisco.connectTG(tg_1);
        	setTestStepEnd();
        	
        	setTestStepBegin("********* Check MO status of GsmSector, AbisIp, and Trx after MO creation, unlock, and connect TG. GsmSector=" + GsmSectorLdn_0);
        	checkMoStatusAfterUnlockedAbisIpAndTrxAndConnectedTG(GsmSectorLdn_0, AbisIpLdn_0, TrxLdn_0_in_GsmSector_0, TrxLdn_1_in_GsmSector_0);
        	setTestStepEnd();

        	setTestStepBegin("********* Check MO status of GsmSector, AbisIp, and Trx after MO creation, unlock, and connect TG. GsmSector=" + GsmSectorLdn_1);
        	checkMoStatusAfterUnlockedAbisIpAndTrxAndConnectedTG(GsmSectorLdn_1, AbisIpLdn_1, TrxLdn_0_in_GsmSector_1, TrxLdn_1_in_GsmSector_1);
        	setTestStepEnd();
       	
        	// Establish Oml link for Scf
        	setTestStepBegin("********* Establish OML link for Scf");
        	establishScfOmlLinks();
        	setTestStepEnd();
            
        	setTestStepBegin("********* Check MO status of GsmSector, AbisIp, and Trx after MO creation, unlock, connect TG, and SCF OML established. GsmSector=" + GsmSectorLdn_0);
        	checkMoStatusAfterUnlockedAbisIpAndTrxAndEstablishedScfOml(GsmSectorLdn_0, AbisIpLdn_0, TrxLdn_0_in_GsmSector_0, TrxLdn_1_in_GsmSector_0);
        	setTestStepEnd();

        	setTestStepBegin("********* Check MO status of GsmSector, AbisIp, and Trx after MO creation, unlock, connect TG, and SCF OML established. GsmSector=" + GsmSectorLdn_1);
        	checkMoStatusAfterUnlockedAbisIpAndTrxAndEstablishedScfOml(GsmSectorLdn_1, AbisIpLdn_1, TrxLdn_0_in_GsmSector_1, TrxLdn_1_in_GsmSector_1);
        	setTestStepEnd();

        	setTestStepBegin("********* Start SO Scf, AO Tf, and AO AT. Send AT Config and AT Enabled for TG=0");
        	startSoScfAoTfAoAtSendAtConfigAndAtEnable(tg_0);
        	setTestStepEnd();
        	
        	// check for AT BUndling Info for both TG:s, check for default bundling and both teis
            // Get a AT BundlingInfo Update
            setTestStepBegin("Wait for an AT Bundling Info Update for TG=0");
            assertEquals("Did not receive a valid AT Bundling Info Update", "", abisHelper.waitForAtBundlingInfoUpdateWithPredefinedBundlingData(10, tg_0, new ArrayList<Integer>(Arrays.asList(trxId_0, trxId_1))));
            setTestStepEnd();

        	setTestStepBegin("********* Start SO Scf, AO Tf, and AO AT. Send AT Config and AT Enabled for TG=1");
        	
        	startSoScfAoTfAoAtSendAtConfigAndAtEnable(tg_1);
        	setTestStepEnd();
            
            // check for AT BUndling Info for both TG:s, check for default bundling and both teis
            // Get a AT BundlingInfo Update
            setTestStepBegin("Wait for an AT Bundling Info Update for TG=1");
        	assertEquals("Did not receive a valid AT Bundling Info Update", "", abisHelper.waitForAtBundlingInfoUpdateWithPredefinedBundlingData(10, tg_1, new ArrayList<Integer>(Arrays.asList(trxId_0, trxId_1))));
            setTestStepEnd();

        	setTestStepBegin("********* Check MO status of GsmSector, AbisIp, and Trx after MO creation, unlock, connect TG, SCF OML UP, So Scf, AO TF Started, and Ao AT enabled. GsmSector=" + GsmSectorLdn_0);
        	checkMoStatusAfterUnlockedAbisIpAndTrxAndMosSoScfAoTfStartedAoAtEnabled(GsmSectorLdn_0, AbisIpLdn_0, TrxLdn_0_in_GsmSector_0, TrxLdn_1_in_GsmSector_0);
        	setTestStepEnd();

        	setTestStepBegin("********* Check MO status of GsmSector, AbisIp, and Trx after MO creation, unlock, connect TG, SCF OML UP, So Scf, AO TF started, and Ao AT enabled. GsmSector=" + GsmSectorLdn_1);
        	checkMoStatusAfterUnlockedAbisIpAndTrxAndMosSoScfAoTfStartedAoAtEnabled(GsmSectorLdn_1, AbisIpLdn_1, TrxLdn_0_in_GsmSector_1, TrxLdn_1_in_GsmSector_1);
        	setTestStepEnd();
      	
        	// Establish Oml link for Scf and OML+RSL for Trx
        	setTestStepBegin("********* Establish OML link for Scf and OML+RSL for Trx");
        	establishAllLinks();
        	setTestStepEnd();
        	
        	setTestStepBegin("********* Check MO status of GsmSector, AbisIp, and Trx after MO creation, unlock, connect TG, all links established, So Scf, AO TF Started, and Ao AT enabled. GsmSector=" + GsmSectorLdn_0);
        	checkMoStatusAfterUnlockedAbisIpAndTrxAndMosSoScfAoTfStartedAoAtEnabledAllLinksUp(GsmSectorLdn_0, AbisIpLdn_0, TrxLdn_0_in_GsmSector_0, TrxLdn_1_in_GsmSector_0);
        	setTestStepEnd();

        	setTestStepBegin("********* Check MO status of GsmSector, AbisIp, and Trx after MO creation, unlock, connect TG, all links established, So Scf, AO TF Started, and Ao AT enabled. GsmSector=" + GsmSectorLdn_1);
        	checkMoStatusAfterUnlockedAbisIpAndTrxAndMosSoScfAoTfStartedAoAtEnabledAllLinksUp(GsmSectorLdn_1, AbisIpLdn_1, TrxLdn_0_in_GsmSector_1, TrxLdn_1_in_GsmSector_1);
        	setTestStepEnd();
        	
        	
        	// Start locking MO:s under the first GsmSector
        	abisHelper.clearStatusUpdate();
        	
        	setTestStepBegin("********* Lock all GRAT MOs under GsmSector=" + GsmSectorLdn_0);
        	lockMosForOneSector(AbisIpLdn_0, TrxLdn_0_in_GsmSector_0, TrxLdn_1_in_GsmSector_0);
        	setTestStepEnd();
        	
            // Get a AT BundlingInfo Update for TG=0
            setTestStepBegin("Wait for an AT Bundling Info Update for TG=0 after locking tei=0 (only tei=1 shall remain in the bundling info)");
            assertEquals("Did not receive a valid AT Bundling Info Update", "", abisHelper.waitForAtBundlingInfoUpdateWithPredefinedBundlingData(10, tg_0, new ArrayList<Integer>(Arrays.asList(trxId_1))));
            setTestStepEnd();
            
            // Get a Status Update and check the BTS Capabilities Signature, one for each locked trx
            setTestStepBegin("Wait for a Status Update from SCF");
        	assertEquals("Did not receive a valid SO SCF Status Update", "", abisHelper.waitForSoScfStatusUpdate(10, tg_0, new StringBuffer()));
            setTestStepEnd();

            
            setTestStepBegin("Wait for an AT Bundling Info Update for TG=0 after locking tei=0 and tei=1, empty bundling is expected");
            assertEquals("Did not receive a valid AT Bundling Info Update", "", abisHelper.waitForAtBundlingInfoUpdateWithEmptyBundlingData(10, tg_0));
            setTestStepEnd();
            
        	setTestStepBegin("********* Check MO status of GsmSector, AbisIp, and Trx after MO lock. GsmSector=" + GsmSectorLdn_0);
        	checkMoStatusAfterSoScfAoTfStartedAtEnabledAbisIpAndTrxLocked(GsmSectorLdn_0, AbisIpLdn_0, TrxLdn_0_in_GsmSector_0, TrxLdn_1_in_GsmSector_0);
        	setTestStepEnd();
            
        	// Start locking MO:s under the second GsmSector
        	abisHelper.clearStatusUpdate();
            
        	setTestStepBegin("********* Lock all GRAT MOs under GsmSector=" + GsmSectorLdn_1);
        	lockMosForOneSector(AbisIpLdn_1, TrxLdn_0_in_GsmSector_1, TrxLdn_1_in_GsmSector_1);
        	setTestStepEnd();
        	
            // Get a AT BundlingInfo Update for TG=1
            setTestStepBegin("Wait for an AT Bundling Info Update for TG=1 after locking tei=0 (only tei=1 shall remain in the bundling info)");
            assertEquals("Did not receive a valid AT Bundling Info Update", "", abisHelper.waitForAtBundlingInfoUpdateWithPredefinedBundlingData(10, tg_1, new ArrayList<Integer>(Arrays.asList(trxId_1))));
            setTestStepEnd();

            // Get a Status Update and check the BTS Capabilities Signature, one for each locked trx
            setTestStepBegin("Wait for a Status Update from SCF");
        	assertEquals("Did not receive a valid SO SCF Status Update", "", abisHelper.waitForSoScfStatusUpdate(10, tg_1, new StringBuffer()));
            setTestStepEnd();
            
            setTestStepBegin("Wait for an AT Bundling Info Update for TG=1 after locking tei=0 and tei=1, empty bundling is expected");
            assertEquals("Did not receive a valid AT Bundling Info Update", "", abisHelper.waitForAtBundlingInfoUpdateWithEmptyBundlingData(10, tg_1));
            setTestStepEnd();
        	
        	setTestStepBegin("********* Check MO status of GsmSector, AbisIp, and Trx after MO lock. GsmSector=" + GsmSectorLdn_1);
        	checkMoStatusAfterSoScfAoTfStartedAtEnabledAbisIpAndTrxLocked(GsmSectorLdn_1, AbisIpLdn_1, TrxLdn_0_in_GsmSector_1, TrxLdn_1_in_GsmSector_1);
        	setTestStepEnd();
        	
        	
        	// Start unlock MOs under the first GsmSector

        	setTestStepBegin("********* Unlock all MO:s for first sector");
        	unlockMosForOneSector(AbisIpLdn_0, TrxLdn_0_in_GsmSector_0, TrxLdn_1_in_GsmSector_0);
        	setTestStepEnd();
        	
        	setTestStepBegin("********* Check MO status of GsmSector, AbisIp, and Trx after MO lock and unlock. GsmSector=" + GsmSectorLdn_0);
        	checkMoStatusAfterMosUnlockedSoScfAoTfStartedAtEnabledMosLockedMosUnlocked(GsmSectorLdn_0, AbisIpLdn_0, TrxLdn_0_in_GsmSector_0, TrxLdn_1_in_GsmSector_0);
        	setTestStepEnd();
        	
        	//Start unlock MOs under the second GsmSector

        	setTestStepBegin("********* Unlock all MO:s for second sector");
        	unlockMosForOneSector(AbisIpLdn_1, TrxLdn_0_in_GsmSector_1, TrxLdn_1_in_GsmSector_1);
        	setTestStepEnd();
        	
        	setTestStepBegin("********* Check MO status of GsmSector, AbisIp, and Trx after MO lock and unlock. GsmSector=" + GsmSectorLdn_1);
        	checkMoStatusAfterMosUnlockedSoScfAoTfStartedAtEnabledMosLockedMosUnlocked(GsmSectorLdn_1, AbisIpLdn_1, TrxLdn_0_in_GsmSector_1, TrxLdn_1_in_GsmSector_1);
        	setTestStepEnd();
        	
        	// Establish Oml link for Scf
        	setTestStepBegin("********* Establish OML link for Scf");
        	establishScfOmlLinks();
        	setTestStepEnd();
       	
        	setTestStepBegin("********* Start SO Scf, AO Tf, and AO AT. Send AT Config and AT Enabled for TG=1 again");
        	startSoScfAoTfAoAtSendAtConfigAndAtEnable(tg_1);
        	setTestStepEnd();
        	
            // Get a AT BundlingInfo Update
            setTestStepBegin("Wait for an AT Bundling Info Update for TG=1");
            assertEquals("Did not receive a valid AT Bundling Info Update", "", abisHelper.waitForAtBundlingInfoUpdateWithPredefinedBundlingData(10, tg_1, new ArrayList<Integer>(Arrays.asList(trxId_0, trxId_1))));
            setTestStepEnd();

        	setTestStepBegin("********* Start SO Scf, AO Tf, and AO AT. Send AT Config and AT Enabled for TG=0 again");
        	startSoScfAoTfAoAtSendAtConfigAndAtEnable(tg_0);
        	setTestStepEnd();
       	
            // Get a AT BundlingInfo Update
            setTestStepBegin("Wait for an AT Bundling Info Update for TG=0");
            assertEquals("Did not receive a valid AT Bundling Info Update", "", abisHelper.waitForAtBundlingInfoUpdateWithPredefinedBundlingData(10, tg_0, new ArrayList<Integer>(Arrays.asList(trxId_0, trxId_1))));
            setTestStepEnd();

        	// Establish Oml link for Scf and OML+RSL for Trx
        	setTestStepBegin("********* Establish OML link for Scf and OML+RSL for Trx again");
        	establishAllLinks();
        	setTestStepEnd();

        	setTestStepBegin("********* Check MO status of GsmSector, AbisIp, and Trx after MO creation, unlock, connect TG, and links established. GsmSector=" + GsmSectorLdn_0);
        	checkMoStatusAfterUnlockedAbisIpAndTrxAndMosSoScfAoTfStartedAoAtEnabledAllLinksUp(GsmSectorLdn_0, AbisIpLdn_0, TrxLdn_0_in_GsmSector_0, TrxLdn_1_in_GsmSector_0);
        	setTestStepEnd();

        	setTestStepBegin("********* Check MO status of GsmSector, AbisIp, and Trx after MO creation, unlock, connect TG, and links established. GsmSector=" + GsmSectorLdn_1);
        	checkMoStatusAfterUnlockedAbisIpAndTrxAndMosSoScfAoTfStartedAoAtEnabledAllLinksUp(GsmSectorLdn_1, AbisIpLdn_1, TrxLdn_0_in_GsmSector_1, TrxLdn_1_in_GsmSector_1);
        	setTestStepEnd();

        	setTestStepBegin("********* Disconect TG " + tg_0);
        	abisco.disconnectTG(tg_0);
        	setTestStepEnd();

        	setTestStepBegin("********* Disconect TG " + tg_1);
        	abisco.disconnectTG(tg_1);
        	setTestStepEnd();

        	setTestStepBegin("********* Check MO status of GsmSector, AbisIp, and Trx after MO creation, unlock, disconnected TG. GsmSector=" + GsmSectorLdn_0);
        	checkMoStatusAfterUnlockedAbisIpAndTrxAndDisconnectedTG(GsmSectorLdn_0, AbisIpLdn_0, TrxLdn_0_in_GsmSector_0, TrxLdn_1_in_GsmSector_0);
        	setTestStepEnd();

        	setTestStepBegin("********* Check MO status of GsmSector, AbisIp, and Trx after MO creation, unlock, disconnected TG. GsmSector=" + GsmSectorLdn_1);
        	checkMoStatusAfterUnlockedAbisIpAndTrxAndDisconnectedTG(GsmSectorLdn_1, AbisIpLdn_1, TrxLdn_0_in_GsmSector_1, TrxLdn_1_in_GsmSector_1);
        	setTestStepEnd();

        	setTestStepBegin("********* Lock all GRAT MOs");
        	lockAllGratMOs();
        	setTestStepEnd();

        	setTestStepBegin("********* Delete all GRAT MOs");
        	deleteAllGratMOs();
        	setTestStepEnd();
        	
        	// TG disconnected, so we can not check for any SCF Status Updates here from the delete trx
        	

        	setTestStepBegin("********* Check that GsmSector, AbisIp, and Trx MOs are deleted. GsmSector=" + GsmSectorLdn_0);
        	checkThatGsmSectorAbisIpTrxMoAreDeleted(GsmSectorLdn_0, AbisIpLdn_0, TrxLdn_0_in_GsmSector_0, TrxLdn_1_in_GsmSector_0);
        	setTestStepEnd();

        	setTestStepBegin("********* Check that GsmSector, AbisIp, and Trx MOs are deleted. GsmSector=" + GsmSectorLdn_1);
        	checkThatGsmSectorAbisIpTrxMoAreDeleted(GsmSectorLdn_1, AbisIpLdn_1, TrxLdn_0_in_GsmSector_1, TrxLdn_1_in_GsmSector_1);
        	setTestStepEnd();
        }
        
        setTestStepBegin("********* Post-test: Disconnect and delete the TGs");
        disconnectAndDeleteTg(tg_0);
        disconnectAndDeleteTg(tg_1);
        setTestStepEnd();
    }
    
    /**
     * multipleGsmSectorsWithMultipleTrxes_2
     * @description Testing transport with two GsmSectors with two Trx:es each.
     *              Grat MO:s are created and unlocked after the TGs are connected.
     *				The SCF OML and TRX OML+RSL are established for both GsmSectors and all Trx:es 
     *				The TGs are disconnected one at a time, and the MO status is checked after each TG has been disconnected.
     *				The TGs are connected one at a time, and the MO status is checked after each TG has been connected.
     *				The SCF OML and TRX OML+RSL are established separately for each TG, and the MO status is checked.
     *				The TRX:es and the AbisIp for each GsmSector are locked separately, MO status is checked.
     * 				The TGs are disconnected
     * 				GRAT MO:s are deleted.
     * 				The sequence above are looped a couple of times.				
     *              This TC does not completely verify any UC, but the following UCs are involved: 
     *              UC419.N, UC420.N, UC424.N, UC425.N, UC426.A4, UC428.N, UC457.N, UC458.N , UC459.N, UC461.N
     * 
     * @param testId - unique identifier of the test case
     * @param description
     */
    @Test(timeOut = 850000)
    @Parameters({ "testId", "description" })
    public void multipleGsmSectorsWithMultipleTrxes_2(String testId, String description) throws InterruptedException {
        setTestCase(testId, description);
        
        setTestStepBegin("TN");
        if (!momHelper.isTnConfigured())
        	momHelper.configureTn(abisco.getBscIpAddress());
        
    	if (!momHelper.isBtsFunctionCreated()) {
    		setTestInfo("Precondition: Create BtsFunction MO");
    		momHelper.createBtsFunctionMo(MomHelper.BTS_FUNCTION_LDN);
    	}
        
        setTestStepBegin("********* Pre-test: Make sure that TG:s are disconnected and deleted");
        disconnectAndDeleteTg(tg_0);
        disconnectAndDeleteTg(tg_1);
        setTestStepEnd();

        setTestStepBegin("********* Pre-test: Start TSS");
        abisco.startTss();
        setTestStepEnd();
        
        setTestStepBegin("********* Pre-test: Create two TGs with two TRX:es each");
        createTgsAndCells();
        setTestStepEnd();
        
        // run the same test a couple of times to see that we clean up
        for (int i = 0 ; i < 2 ; ++i)
        {
        	setTestStepBegin("********* Connect tg");
        	abisco.connectTG(tg_0);
        	abisco.connectTG(tg_1);
        	setTestStepEnd();
        	
        	// test this when we have AT, right now we crash in l2tp transport in gte
          	// Establish Oml link for Scf and OML+RSL for Trx, do this asynchronously, to have a completely ready BSC side
//        	setTestStepBegin("********* Establish OML link for Scf and OML+RSL for Trx");
//        	establishAllLinksAsync();
//        	setTestStepEnd();

            // We will place both TRXes in GsmSector=0 on one GTE, and the TRXes in GsmSector=1 on different GTEs
        	setTestStepBegin("********* Create all MO:s for first sector");
        	createUnlockMosForOneSector(GsmSectorLdn_0, AbisIpLdn_0, TrxLdn_0_in_GsmSector_0, TrxLdn_1_in_GsmSector_0, connection_name_0);
        	// Make the TRXes for this sector ends up on the same GTE by locking and unlocking one
        	momHelper.lockMo(TrxLdn_1_in_GsmSector_0);
        	momHelper.unlockMo(TrxLdn_1_in_GsmSector_0);
        	setTestStepEnd();

        	setTestStepBegin("********* Create all MO:s for second sector");
        	createUnlockMosForOneSector(GsmSectorLdn_1, AbisIpLdn_1, TrxLdn_0_in_GsmSector_1, TrxLdn_1_in_GsmSector_1, connection_name_1);
        	setTestStepEnd();
        	

        	// TODO: check note above by doing this async
        	// Establish Oml link for Scf
        	setTestStepBegin("********* Establish OML link for Scf");
        	establishScfOmlLinks();
        	setTestStepEnd();
   
        	setTestStepBegin("********* Check MO status of GsmSector, AbisIp, and Trx after MO creation, unlock, connect TG, and SCF OML established. GsmSector=" + GsmSectorLdn_0);
        	checkMoStatusAfterUnlockedAbisIpAndTrxAndEstablishedScfOml(GsmSectorLdn_0, AbisIpLdn_0, TrxLdn_0_in_GsmSector_0, TrxLdn_1_in_GsmSector_0);
        	setTestStepEnd();

        	setTestStepBegin("********* Check MO status of GsmSector, AbisIp, and Trx after MO creation, unlock, connect TG, and SCF OML established. GsmSector=" + GsmSectorLdn_1);
        	checkMoStatusAfterUnlockedAbisIpAndTrxAndEstablishedScfOml(GsmSectorLdn_1, AbisIpLdn_1, TrxLdn_0_in_GsmSector_1, TrxLdn_1_in_GsmSector_1);
        	setTestStepEnd();
       	
        	setTestStepBegin("********* Start SO Scf, AO Tf, and AO AT. Send AT Config and AT Enabled for TG=0");
        	startSoScfAoTfAoAtSendAtConfigAndAtEnable(tg_0);
        	setTestStepEnd();
        	
        	setTestStepBegin("********* Check MO status of GsmSector, AbisIp, and Trx after MO creation, unlock, connect TG, SCF OML UP, So Scf, AO TF Started, and Ao AT enabled. GsmSector=" + GsmSectorLdn_0);
        	checkMoStatusAfterUnlockedAbisIpAndTrxAndMosSoScfAoTfStartedAoAtEnabled(GsmSectorLdn_0, AbisIpLdn_0, TrxLdn_0_in_GsmSector_0, TrxLdn_1_in_GsmSector_0);
        	setTestStepEnd();
        	
        	setTestStepBegin("********* Start SO Scf, AO Tf, and AO AT. Send AT Config and AT Enabled for TG=1");
        	startSoScfAoTfAoAtSendAtConfigAndAtEnable(tg_1);
        	setTestStepEnd();

        	setTestStepBegin("********* Check MO status of GsmSector, AbisIp, and Trx after MO creation, unlock, connect TG, SCF OML UP, So Scf, AO TF started, and Ao AT enabled. GsmSector=" + GsmSectorLdn_1);
        	checkMoStatusAfterUnlockedAbisIpAndTrxAndMosSoScfAoTfStartedAoAtEnabled(GsmSectorLdn_1, AbisIpLdn_1, TrxLdn_0_in_GsmSector_1, TrxLdn_1_in_GsmSector_1);
        	setTestStepEnd();
      	
        	// Establish Oml link for Scf and OML+RSL for Trx
        	setTestStepBegin("********* Establish OML link for Scf and OML+RSL for Trx");
        	establishAllLinks();
        	setTestStepEnd();
        	
        	setTestStepBegin("********* Check MO status of GsmSector, AbisIp, and Trx after MO creation, unlock, connect TG, all links established, So Scf, AO TF Started, and Ao AT enabled. GsmSector=" + GsmSectorLdn_0);
        	checkMoStatusAfterUnlockedAbisIpAndTrxAndMosSoScfAoTfStartedAoAtEnabledAllLinksUp(GsmSectorLdn_0, AbisIpLdn_0, TrxLdn_0_in_GsmSector_0, TrxLdn_1_in_GsmSector_0);
        	setTestStepEnd();

        	setTestStepBegin("********* Check MO status of GsmSector, AbisIp, and Trx after MO creation, unlock, connect TG, all links established, So Scf, AO TF Started, and Ao AT enabled. GsmSector=" + GsmSectorLdn_1);
        	checkMoStatusAfterUnlockedAbisIpAndTrxAndMosSoScfAoTfStartedAoAtEnabledAllLinksUp(GsmSectorLdn_1, AbisIpLdn_1, TrxLdn_0_in_GsmSector_1, TrxLdn_1_in_GsmSector_1);
        	setTestStepEnd();
        	       	
        	// disconnect one tg at a time, and check MO status afterwards
        	
        	setTestStepBegin("********* Disconect TG " + tg_0);
        	abisco.disconnectTG(tg_0);
        	setTestStepEnd();

        	setTestStepBegin("********* Check MO status of GsmSector, AbisIp, and Trx after MO creation, unlock, disconnected TG. GsmSector=" + GsmSectorLdn_0);
        	checkMoStatusAfterUnlockedAbisIpAndTrxAndDisconnectedTG(GsmSectorLdn_0, AbisIpLdn_0, TrxLdn_0_in_GsmSector_0, TrxLdn_1_in_GsmSector_0);
        	setTestStepEnd();
        	
        	setTestStepBegin("********* Check MO status of GsmSector, AbisIp, and Trx after MO creation, unlock, connect TG, all links established, So Scf, AO TF Started, and Ao AT enabled. GsmSector=" + GsmSectorLdn_1);
        	checkMoStatusAfterUnlockedAbisIpAndTrxAndMosSoScfAoTfStartedAoAtEnabledAllLinksUp(GsmSectorLdn_1, AbisIpLdn_1, TrxLdn_0_in_GsmSector_1, TrxLdn_1_in_GsmSector_1);
        	setTestStepEnd();
        	
        	setTestStepBegin("********* Disconect TG " + tg_1);
        	abisco.disconnectTG(tg_1);
        	setTestStepEnd();
        	
        	setTestStepBegin("********* Check MO status of GsmSector, AbisIp, and Trx after MO creation, unlock, disconnected TG. GsmSector=" + GsmSectorLdn_0);
        	checkMoStatusAfterUnlockedAbisIpAndTrxAndDisconnectedTG(GsmSectorLdn_0, AbisIpLdn_0, TrxLdn_0_in_GsmSector_0, TrxLdn_1_in_GsmSector_0);
        	setTestStepEnd();

        	setTestStepBegin("********* Check MO status of GsmSector, AbisIp, and Trx after MO creation, unlock, disconnected TG. GsmSector=" + GsmSectorLdn_1);
        	checkMoStatusAfterUnlockedAbisIpAndTrxAndDisconnectedTG(GsmSectorLdn_1, AbisIpLdn_1, TrxLdn_0_in_GsmSector_1, TrxLdn_1_in_GsmSector_1);
        	setTestStepEnd();
        	
        	// connect the TGs one at a time, and check mo status
        	
        	setTestStepBegin("********* Connect tg " + tg_1);
        	abisco.connectTG(tg_1);
        	setTestStepEnd();

        	setTestStepBegin("********* Check MO status of GsmSector, AbisIp, and Trx after MO creation, unlock, disconnected TG. GsmSector=" + GsmSectorLdn_0);
        	checkMoStatusAfterUnlockedAbisIpAndTrxAndDisconnectedTG(GsmSectorLdn_0, AbisIpLdn_0, TrxLdn_0_in_GsmSector_0, TrxLdn_1_in_GsmSector_0);
        	setTestStepEnd();
        	
        	setTestStepBegin("********* Check MO status of GsmSector, AbisIp, and Trx after MO creation, unlock, and connect TG. GsmSector=" + GsmSectorLdn_1);
        	checkMoStatusAfterUnlockedAbisIpAndTrxAndDisconnectedReconnectedTG(GsmSectorLdn_1, AbisIpLdn_1, TrxLdn_0_in_GsmSector_1, TrxLdn_1_in_GsmSector_1);
        	setTestStepEnd();
        	        	
        	setTestStepBegin("********* Connect tg " + tg_0);
        	abisco.connectTG(tg_0);
        	setTestStepEnd();
        	
        	setTestStepBegin("********* Check MO status of GsmSector, AbisIp, and Trx after MO creation, unlock, and connect TG. GsmSector=" + GsmSectorLdn_0);
        	checkMoStatusAfterUnlockedAbisIpAndTrxAndDisconnectedReconnectedTG(GsmSectorLdn_0, AbisIpLdn_0, TrxLdn_0_in_GsmSector_0, TrxLdn_1_in_GsmSector_0);
        	setTestStepEnd();

        	setTestStepBegin("********* Check MO status of GsmSector, AbisIp, and Trx after MO creation, unlock, and connect TG. GsmSector=" + GsmSectorLdn_1);
        	checkMoStatusAfterUnlockedAbisIpAndTrxAndDisconnectedReconnectedTG(GsmSectorLdn_1, AbisIpLdn_1, TrxLdn_0_in_GsmSector_1, TrxLdn_1_in_GsmSector_1);
        	setTestStepEnd();
        	
        	// establish the links for one TG at a time, and check MO status
        	// Establish Oml link for Scf and OML+RSL for Trx
        	setTestStepBegin("********* Establish OML link for Scf for TG " + tg_0);
            establishScfOmlLinkForTg(0);
            setTestStepEnd();
            setTestStepBegin("********* AT Bundling Info Request for " + tg_0);
            abisHelper.atBundlingInfoRequest(1, tg_0);
            setTestStepEnd();
            setTestStepBegin("********* Establish OML+RSL for Trx for TG " + tg_0);
        	establishAllLinksForTg(tg_0);
        	setTestStepEnd();
        	
        	setTestStepBegin("********* Check MO status of GsmSector, AbisIp, and Trx after MO creation, unlock, connect TG, and links established. GsmSector=" + GsmSectorLdn_0);
        	checkMoStatusAfterUnlockedAbisIpAndTrxAndMosSoScfAoTfStartedAoAtEnabledAllLinksUp(GsmSectorLdn_0, AbisIpLdn_0, TrxLdn_0_in_GsmSector_0, TrxLdn_1_in_GsmSector_0);
        	setTestStepEnd();
        	
        	setTestStepBegin("********* Check MO status of GsmSector, AbisIp, and Trx after MO creation, unlock, and connect TG. GsmSector=" + GsmSectorLdn_1);
        	checkMoStatusAfterUnlockedAbisIpAndTrxAndDisconnectedReconnectedTG(GsmSectorLdn_1, AbisIpLdn_1, TrxLdn_0_in_GsmSector_1, TrxLdn_1_in_GsmSector_1);
        	setTestStepEnd();
        	
        	// Establish Oml link for Scf and OML+RSL for Trx
        	setTestStepBegin("********* Establish OML link for Scf for TG " + tg_1);
            establishScfOmlLinkForTg(1);
            setTestStepEnd();
            setTestStepBegin("********* AT Bundling Info Request for " + tg_1);
            abisHelper.atBundlingInfoRequest(1, tg_1);
            setTestStepEnd();
        	setTestStepBegin("********* Establish OML link for Scf and OML+RSL for Trx for TG " + tg_1);
        	establishAllLinksForTg(tg_1);
        	setTestStepEnd();        	
        	
        	setTestStepBegin("********* Check MO status of GsmSector, AbisIp, and Trx after MO creation, unlock, connect TG, and links established. GsmSector=" + GsmSectorLdn_0);
        	checkMoStatusAfterUnlockedAbisIpAndTrxAndMosSoScfAoTfStartedAoAtEnabledAllLinksUp(GsmSectorLdn_0, AbisIpLdn_0, TrxLdn_0_in_GsmSector_0, TrxLdn_1_in_GsmSector_0);
        	setTestStepEnd();

        	setTestStepBegin("********* Check MO status of GsmSector, AbisIp, and Trx after MO creation, unlock, connect TG, and links established. GsmSector=" + GsmSectorLdn_1);
        	checkMoStatusAfterUnlockedAbisIpAndTrxAndMosSoScfAoTfStartedAoAtEnabledAllLinksUp(GsmSectorLdn_1, AbisIpLdn_1, TrxLdn_0_in_GsmSector_1, TrxLdn_1_in_GsmSector_1);
        	setTestStepEnd();
        	
        	// lock MOs for one sector at a time and check MO status
        	
        	setTestStepBegin("********* Lock all GRAT MOs in GsmSector " + GsmSectorLdn_0);
            lockMosForOneSector(AbisIpLdn_0, TrxLdn_0_in_GsmSector_0, TrxLdn_1_in_GsmSector_0);
        	setTestStepEnd();
        	
        	setTestStepBegin("********* Check MO status of GsmSector, AbisIp, and Trx after MO lock. GsmSector=" + GsmSectorLdn_0);
        	checkMoStatusAfterSoScfAoTfStartedAtEnabledAbisIpAndTrxLocked(GsmSectorLdn_0, AbisIpLdn_0, TrxLdn_0_in_GsmSector_0, TrxLdn_1_in_GsmSector_0);
        	setTestStepEnd();
        	
        	setTestStepBegin("********* Check MO status of GsmSector, AbisIp, and Trx after MO creation, unlock, connect TG, and links established. GsmSector=" + GsmSectorLdn_1);
        	checkMoStatusAfterUnlockedAbisIpAndTrxAndMosSoScfAoTfStartedAoAtEnabledAllLinksUp(GsmSectorLdn_1, AbisIpLdn_1, TrxLdn_0_in_GsmSector_1, TrxLdn_1_in_GsmSector_1);
        	setTestStepEnd();
        	
        	setTestStepBegin("********* Lock all GRAT MOs in GsmSector " + GsmSectorLdn_1);
            lockMosForOneSector(AbisIpLdn_1, TrxLdn_0_in_GsmSector_1, TrxLdn_1_in_GsmSector_1);
        	setTestStepEnd();
        	
        	setTestStepBegin("********* Check MO status of GsmSector, AbisIp, and Trx after MO lock. GsmSector=" + GsmSectorLdn_0);
        	checkMoStatusAfterSoScfAoTfStartedAtEnabledAbisIpAndTrxLocked(GsmSectorLdn_0, AbisIpLdn_0, TrxLdn_0_in_GsmSector_0, TrxLdn_1_in_GsmSector_0);
        	setTestStepEnd();

        	setTestStepBegin("********* Check MO status of GsmSector, AbisIp, and Trx after MO lock. GsmSector=" + GsmSectorLdn_1);
        	checkMoStatusAfterSoScfAoTfStartedAtEnabledAbisIpAndTrxLocked(GsmSectorLdn_1, AbisIpLdn_1, TrxLdn_0_in_GsmSector_1, TrxLdn_1_in_GsmSector_1);
        	setTestStepEnd();

        	// disconnect the TG:s
        	
        	setTestStepBegin("********* Disconect TG " + tg_0);
        	abisco.disconnectTG(tg_0);
        	setTestStepEnd();

        	setTestStepBegin("********* Disconect TG " + tg_1);
        	abisco.disconnectTG(tg_1);
        	setTestStepEnd();

        	// delete all GRAT MO:s, and check that they are gone
        	
        	setTestStepBegin("********* Delete all GRAT MOs");
        	deleteAllGratMOs();
        	setTestStepEnd();

        	setTestStepBegin("********* Check that GsmSector, AbisIp, and Trx MOs are deleted. GsmSector=" + GsmSectorLdn_0);
        	checkThatGsmSectorAbisIpTrxMoAreDeleted(GsmSectorLdn_0, AbisIpLdn_0, TrxLdn_0_in_GsmSector_0, TrxLdn_1_in_GsmSector_0);
        	setTestStepEnd();

        	setTestStepBegin("********* Check that GsmSector, AbisIp, and Trx MOs are deleted. GsmSector=" + GsmSectorLdn_1);
        	checkThatGsmSectorAbisIpTrxMoAreDeleted(GsmSectorLdn_1, AbisIpLdn_1, TrxLdn_0_in_GsmSector_1, TrxLdn_1_in_GsmSector_1);
        	setTestStepEnd();
        }
        
        setTestStepBegin("********* Post-test: Disconnect and delete the TGs");
        disconnectAndDeleteTg(tg_0);
        disconnectAndDeleteTg(tg_1);
        setTestStepEnd();
    }
    
    /**
     * GsmSectorWithMultipleTrxes_1
     * @description Testing transport with one GsmSectors with two Trx:es.
     *              The test verifies that it is possible to create two Trxes
     *              on different GTEs, with a AT configuration consisting of
     *              one profile per SAPI. Verifies that OML and RSL are established
     *              for all Trxes.
     *              The sequence above are looped a couple of times.                
     *              This TC does not completely verify any UC, but the following UCs are involved: 
     *              UC419.N, UC420.N, UC424.N, UC425.N, UC426.A4, UC428.N, UC457.N, UC458.N , UC459.N, UC461.N
     *              
     * @param testId - unique identifier of the test case
     * @param description
     */
    @Test(timeOut = 850000)
    @Parameters({ "testId", "description" })
    public void GsmSectorWithMultipleTrxes_1(String testId, String description) throws InterruptedException {
        setTestCase(testId, description);
        
        setTestStepBegin("TN");
        if (!momHelper.isTnConfigured())
            momHelper.configureTn(abisco.getBscIpAddress());
        
    	if (!momHelper.isBtsFunctionCreated()) {
    		setTestInfo("Precondition: Create BtsFunction MO");
    		momHelper.createBtsFunctionMo(MomHelper.BTS_FUNCTION_LDN);
    	}
        
        setTestStepBegin("********* Pre-test: Make sure that TG is disconnected and deleted");
        disconnectAndDeleteTg(tg_0);
        setTestStepEnd();

        setTestStepBegin("********* Pre-test: Start TSS");
        abisco.startTss();
        setTestStepEnd();
        
        setTestStepBegin("********* Pre-test: Create two TGs with two TRX:es each");
        createTgAndCellFiveBundlingProfiles(tg_0);
        setTestStepEnd();
        
        // run the same test a couple of times to see that we clean up
        for (int i = 0 ; i < 2 ; ++i)
        {
            setTestStepBegin("********* Connect tg");
            abisco.connectTG(tg_0);
            setTestStepEnd();
                        
            setTestStepBegin("********* Create all MO:s for first sector");
            createUnlockMosForOneSector(GsmSectorLdn_0, AbisIpLdn_0, TrxLdn_0_in_GsmSector_0, TrxLdn_1_in_GsmSector_0, connection_name_0);
            setTestStepEnd();            
            
            // no SO SCF started, so we can not check for any SCF Status Updates here from the create and unlock trx
            
            setTestStepBegin("********* Check MO status of GsmSector, AbisIp, and Trx after MO creation, unlock, and connect TG. GsmSector=" + GsmSectorLdn_0);
            checkMoStatusAfterUnlockedAbisIpAndTrxAndConnectedTG(GsmSectorLdn_0, AbisIpLdn_0, TrxLdn_0_in_GsmSector_0, TrxLdn_1_in_GsmSector_0);
            setTestStepEnd();
        
            // Establish Oml link for Scf
            setTestStepBegin("********* Establish OML link for Scf");
            establishScfOmlLinkForTg(tg_0);
            setTestStepEnd();
   
            setTestStepBegin("********* Check MO status of GsmSector, AbisIp, and Trx after MO creation, unlock, connect TG, and SCF OML established. GsmSector=" + GsmSectorLdn_0);
            checkMoStatusAfterUnlockedAbisIpAndTrxAndEstablishedScfOml(GsmSectorLdn_0, AbisIpLdn_0, TrxLdn_0_in_GsmSector_0, TrxLdn_1_in_GsmSector_0);
            setTestStepEnd();

            setTestStepBegin("********* Start SO Scf, AO Tf, and AO AT");
            abisHelper.startRequest(OM_G31R01.Enums.MOClass.SCF, tg_0);
            abisHelper.startRequest(OM_G31R01.Enums.MOClass.TF, tg_0);
            abisHelper.startRequest(OM_G31R01.Enums.MOClass.AT, tg_0);
            setTestStepEnd();
            
            setTestStepBegin("********* Configure and Enable AT");
            atConfigRequestFiveBundlingProfiles(tg_0);
            setTestStepEnd();
            
            // check for AT BUndling Info for both TG:s, check for default bundling and both teis
            // Get a AT BundlingInfo Update
            setTestStepBegin("Wait for an AT Bundling Info Update for TG=0");
            assertEquals("Did not receive a valid AT Bundling Info Update", "", abisHelper.waitForAtBundlingInfoUpdateWithPredefinedBundlingData(10, tg_0, new ArrayList<Integer>(Arrays.asList(trxId_0, trxId_1))));
            setTestStepEnd();
            
            setTestStepBegin("********* Check MO status of GsmSector, AbisIp, and Trx after MO creation, unlock, connect TG, SCF OML UP, So Scf, AO TF Started, and Ao AT enabled. GsmSector=" + GsmSectorLdn_0);
            checkMoStatusAfterUnlockedAbisIpAndTrxAndMosSoScfAoTfStartedAoAtEnabled(GsmSectorLdn_0, AbisIpLdn_0, TrxLdn_0_in_GsmSector_0, TrxLdn_1_in_GsmSector_0);
            setTestStepEnd();

            // Establish Oml link for Scf and OML+RSL for Trx
            setTestStepBegin("********* Establish OML link for Scf and OML+RSL for Trx");
            establishAllLinksForTg(tg_0);
            setTestStepEnd();
            
            setTestStepBegin("********* Check MO status of GsmSector, AbisIp, and Trx after MO creation, unlock, connect TG, all links established, So Scf, AO TF Started, and Ao AT enabled. GsmSector=" + GsmSectorLdn_0);
            checkMoStatusAfterUnlockedAbisIpAndTrxAndMosSoScfAoTfStartedAoAtEnabledAllLinksUp(GsmSectorLdn_0, AbisIpLdn_0, TrxLdn_0_in_GsmSector_0, TrxLdn_1_in_GsmSector_0);
            setTestStepEnd();

            
            // Start locking MO:s under the first GsmSector
            abisHelper.clearStatusUpdate();
            
            setTestStepBegin("********* Lock all GRAT MOs under GsmSector=" + GsmSectorLdn_0);
            lockMosForOneSector(AbisIpLdn_0, TrxLdn_0_in_GsmSector_0, TrxLdn_1_in_GsmSector_0);
            setTestStepEnd();
            
            // Get a AT BundlingInfo Update for TG=0
            setTestStepBegin("Wait for an AT Bundling Info Update for TG=0 after locking tei=0 (only tei=1 shall remain in the bundling info)");
            assertEquals("Did not receive a valid AT Bundling Info Update", "", abisHelper.waitForAtBundlingInfoUpdateWithPredefinedBundlingData(10, tg_0, new ArrayList<Integer>(Arrays.asList(trxId_1))));
            setTestStepEnd();
            
            // Get a Status Update and check the BTS Capabilities Signature, one for each locked trx
            setTestStepBegin("Wait for a Status Update from SCF");
            assertEquals("Did not receive a valid SO SCF Status Update", "", abisHelper.waitForSoScfStatusUpdate(10, tg_0, new StringBuffer()));
            setTestStepEnd();

            setTestStepBegin("Wait for an AT Bundling Info Update for TG=0 after locking tei=0 and tei=1, empty bundling is expected");
            assertEquals("Did not receive a valid AT Bundling Info Update", "", abisHelper.waitForAtBundlingInfoUpdateWithEmptyBundlingData(10, tg_0));
            setTestStepEnd();
            
            setTestStepBegin("********* Check MO status of GsmSector, AbisIp, and Trx after MO lock. GsmSector=" + GsmSectorLdn_0);
            checkMoStatusAfterSoScfAoTfStartedAtEnabledAbisIpAndTrxLocked(GsmSectorLdn_0, AbisIpLdn_0, TrxLdn_0_in_GsmSector_0, TrxLdn_1_in_GsmSector_0);
            setTestStepEnd();            
            
            // Start unlock MOs under the first GsmSector

            setTestStepBegin("********* Unlock all MO:s for first sector");
            unlockMosForOneSector(AbisIpLdn_0, TrxLdn_0_in_GsmSector_0, TrxLdn_1_in_GsmSector_0);
            setTestStepEnd();
            
            setTestStepBegin("********* Check MO status of GsmSector, AbisIp, and Trx after MO lock and unlock. GsmSector=" + GsmSectorLdn_0);
            checkMoStatusAfterMosUnlockedSoScfAoTfStartedAtEnabledMosLockedMosUnlocked(GsmSectorLdn_0, AbisIpLdn_0, TrxLdn_0_in_GsmSector_0, TrxLdn_1_in_GsmSector_0);
            setTestStepEnd();
            
            // Establish Oml link for Scf
            setTestStepBegin("********* Establish OML link for Scf");
            establishScfOmlLinkForTg(tg_0);
            setTestStepEnd();

            setTestStepBegin("********* Start SO Scf, AO Tf, and AO AT");
            abisHelper.startRequest(OM_G31R01.Enums.MOClass.SCF, tg_0);
            abisHelper.startRequest(OM_G31R01.Enums.MOClass.TF, tg_0);
            abisHelper.startRequest(OM_G31R01.Enums.MOClass.AT, tg_0);
            setTestStepEnd();
            
            setTestStepBegin("********* Configure and Enable AT");
            atConfigRequestFiveBundlingProfiles(tg_0);
            setTestStepEnd();
        
            // Get a AT BundlingInfo Update
            setTestStepBegin("Wait for an AT Bundling Info Update for TG=0");
            assertEquals("Did not receive a valid AT Bundling Info Update", "", abisHelper.waitForAtBundlingInfoUpdateWithPredefinedBundlingData(10, tg_0, new ArrayList<Integer>(Arrays.asList(trxId_0, trxId_1))));
            setTestStepEnd();

            // Establish Oml link for Scf and OML+RSL for Trx
            setTestStepBegin("********* Establish OML link for Scf and OML+RSL for Trx again");
            establishAllLinksForTg(tg_0);
            setTestStepEnd();

            setTestStepBegin("********* Check MO status of GsmSector, AbisIp, and Trx after MO creation, unlock, connect TG, and links established. GsmSector=" + GsmSectorLdn_0);
            checkMoStatusAfterUnlockedAbisIpAndTrxAndMosSoScfAoTfStartedAoAtEnabledAllLinksUp(GsmSectorLdn_0, AbisIpLdn_0, TrxLdn_0_in_GsmSector_0, TrxLdn_1_in_GsmSector_0);
            setTestStepEnd();

            setTestStepBegin("********* Disconect TG " + tg_0);
            abisco.disconnectTG(tg_0);
            setTestStepEnd();

            setTestStepBegin("********* Check MO status of GsmSector, AbisIp, and Trx after MO creation, unlock, disconnected TG. GsmSector=" + GsmSectorLdn_0);
            checkMoStatusAfterUnlockedAbisIpAndTrxAndDisconnectedTG(GsmSectorLdn_0, AbisIpLdn_0, TrxLdn_0_in_GsmSector_0, TrxLdn_1_in_GsmSector_0);
            setTestStepEnd();

            setTestStepBegin("********* Lock all GRAT MOs in sector");
            lockMosForOneSector(AbisIpLdn_0, TrxLdn_0_in_GsmSector_0, TrxLdn_1_in_GsmSector_0);
            setTestStepEnd();

            setTestStepBegin("********* Delete all GRAT MOs");
            momHelper.deleteMo(TrxLdn_0_in_GsmSector_0);
            momHelper.deleteMo(TrxLdn_1_in_GsmSector_0);
            momHelper.deleteMo(AbisIpLdn_0);
            momHelper.deleteMo(GsmSectorLdn_0);
            setTestStepEnd();
            
            // TG disconnected, so we can not check for any SCF Status Updates here from the delete trx
            setTestStepBegin("********* Check that GsmSector, AbisIp, and Trx MOs are deleted. GsmSector=" + GsmSectorLdn_0);
            checkThatGsmSectorAbisIpTrxMoAreDeleted(GsmSectorLdn_0, AbisIpLdn_0, TrxLdn_0_in_GsmSector_0, TrxLdn_1_in_GsmSector_0);
            setTestStepEnd();
        }
        
        setTestStepBegin("********* Post-test: Disconnect and delete the TGs");
        disconnectAndDeleteTg(tg_0);
        setTestStepEnd();
    }   
}   
