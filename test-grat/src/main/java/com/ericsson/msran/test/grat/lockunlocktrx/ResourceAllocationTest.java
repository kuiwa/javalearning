package com.ericsson.msran.test.grat.lockunlocktrx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import se.ericsson.jcat.fw.annotations.Setup;
import se.ericsson.jcat.fw.annotations.Teardown;
import com.ericsson.abisco.clientlib.servers.BG;
import com.ericsson.abisco.clientlib.servers.BG.G31OperationalCondition;
import com.ericsson.abisco.clientlib.servers.BG.G31OperationalConditionReasonsMap;
import com.ericsson.abisco.clientlib.servers.BG.G31StatusUpdate;
import com.ericsson.abisco.clientlib.servers.OM_G31R01;
import com.ericsson.abisco.clientlib.servers.OM_G31R01.Enums;
import com.ericsson.abisco.clientlib.servers.OM_G31R01.FSOffset;
import com.ericsson.commonlibrary.faultmanagement.com.ComAlarm;
import com.ericsson.commonlibrary.managedobjects.ManagedObject;
import com.ericsson.commonlibrary.restorestack.RestoreCommand;
import com.ericsson.msran.g2.annotations.TestInfo;
import com.ericsson.msran.test.grat.testhelpers.AbisHelper;
import com.ericsson.msran.test.grat.testhelpers.AbiscoConnection;
import com.ericsson.msran.test.grat.testhelpers.AlarmHelper;
import com.ericsson.msran.test.grat.testhelpers.CliCommands;
import com.ericsson.msran.test.grat.testhelpers.MomHelper;
import com.ericsson.msran.test.grat.testhelpers.NodeStatusHelper;
import com.ericsson.msran.test.grat.testhelpers.prepost.PidPrePost;
import com.ericsson.msran.test.grat.testhelpers.restorecommands.DeleteMoCommand;
import com.ericsson.msran.test.grat.testhelpers.restorecommands.LockDeleteMoCommand;

import com.ericsson.msran.jcat.TestBase;

/**
 * @id NodeUC426,NodeUC428
 * 
 * @name ResourceAllocationTest
 * 
 * @author GRAT 2014
 * 
 * @created 2014-08-21
 * 
 * @description Verifies resource allocation by unlocking a number of trxs, setting up rx on all of them and then doing
 * a call on one randomly chosen trx in each gsm sector
 * 
 * @revision erablme 2015-03-25 first version
 * 
 */

public class ResourceAllocationTest extends TestBase {  
    
    private enum SectorCommands {DISCONNECT_AND_DELETE_TG,
        CREATE_TGS_AND_CELLS,
        CREATE_MOS,
        UNLOCK_TRXS,
        SETUP_SECTOR,
        ESTABLISH_ALL_LINKS,
        CHECK_MO_STATUS_ALL_LINKS_UP,
        START_ALL_TRXC,
        ENABLE_ALL_RX,
        ENABLE_ALL_TX_HOPPING, SETUP_SECTOR_AFTER_REBOOT, CHECK_MO_STATUS_AFTER_REBOOT}
        
    private static Map<SectorCommands, String> testStepSlogans = new HashMap<SectorCommands, String>();
    
    static {
        testStepSlogans.put(SectorCommands.DISCONNECT_AND_DELETE_TG, 
                "********* Pre-test: Make sure that TG is disconnected and deleted for sector: ");
        testStepSlogans.put(SectorCommands.CREATE_TGS_AND_CELLS, "********* Pre-test: Create TG for sector: ");
        testStepSlogans.put(SectorCommands.CREATE_MOS, "********* Create all MO:s for sector: ");
        testStepSlogans.put(SectorCommands.UNLOCK_TRXS, "********* Unlock all trx:s in sector: ");
        testStepSlogans.put(SectorCommands.SETUP_SECTOR, "********* Setup MOs in sector: ");
        testStepSlogans.put(SectorCommands.CHECK_MO_STATUS_ALL_LINKS_UP, 
                "********* Check MO status of GsmSector, AbisIp, and Trx after MO creation, unlock, connect TG, all links established, So Scf, AO TF Started, and Ao AT enabled for sector: ");
    testStepSlogans.put(SectorCommands.START_ALL_TRXC, "********* Start all TRXCS in TG: ");
    testStepSlogans.put(SectorCommands.CHECK_MO_STATUS_ALL_LINKS_UP, "********* Check MO status of Trx:es after Start TRXCs in TG: ");
    testStepSlogans.put(SectorCommands.ENABLE_ALL_RX, "********* Enable all RX in TG: ");
        
    }
    
    private static Logger logger = Logger.getLogger(AlarmHelper.class);

    
    private final OM_G31R01.Enums.MOClass moClassTrxc = OM_G31R01.Enums.MOClass.TRXC;
    private final OM_G31R01.Enums.MOClass moClassTx = OM_G31R01.Enums.MOClass.TX;
    private final OM_G31R01.Enums.MOClass moClassRx = OM_G31R01.Enums.MOClass.RX;
    private final OM_G31R01.Enums.MOClass moClassTs = OM_G31R01.Enums.MOClass.TS;
    private final OM_G31R01.Enums.MOClass moClassTf = OM_G31R01.Enums.MOClass.TF;
    
    private MomHelper momHelper;
    private PidPrePost pidPrePost;
    private AbiscoConnection abisco;
    private NodeStatusHelper nodeStatus;
    private AbisHelper abisHelper;
    private AlarmHelper alarmHelper;
    private ArrayList<Integer> sectorEquipmentFunctions = new ArrayList<Integer>(3);
    private Map<Integer, Integer> sectorToTrxs = new HashMap<Integer, Integer>(); 
    
    private int currentSectorEquipmentFunction = 0;
    private CliCommands cliCommands;
    

    

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
        assertTrue("Node did not reach a working state before test start", nodeStatus.waitForNodeReadyNoCleanUp());
        momHelper = new MomHelper();
        abisco = new AbiscoConnection();
        setTestInfo("Save current pid to compare after test execution.");
        abisHelper = new AbisHelper();
        alarmHelper = new AlarmHelper();
        sectorToTrxs.clear();
        sectorEquipmentFunctions.clear();
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
    
    private void checkGsmSectorMoAttributeAfterCreateLinkEstablished(String gsmSectorLdn) {
    	assertEquals("GsmSector MO attributes did not reach expected values", "", momHelper.checkGsmSectorMoAttributeAfterCreateLinksEstablished(gsmSectorLdn, 10));
    } 
    
    private void checkGsmSectorMoAttributeAfterCreateLinkEstablishedAllMosStartedAoAtEnabled(String gsmSectorLdn) {
    	assertEquals("GsmSector MO attributes did not reach expected values", "", momHelper.checkGsmSectorMoAttributeAfterAllMosStartedAoAtAndAoTfEnabled(gsmSectorLdn, 10));
    }       
    
    
    
    private void checkAbisIpMoAttributeAfterCreateUnlockBscConnected(String abisIpLdn) {
    	assertEquals("AbisIp MO attributes did not reach expected values", "", momHelper.checkAbisIpMoAttributeAfterUnlockBscConnected(abisIpLdn, abisco.getBscIpAddress(), 30));
    }
    
    private void checkTrxMoAttributeAfterCreateUnlockLinksEstablished(String trxLdn) {
    	assertEquals("Trx MO attributes did not reach expected values", "", momHelper.checkTrxMoAttributeAfterUnlockLinksEstablished(trxLdn, 10));
    }
    
   
    
    private void sendAoATEnable(int tgId) throws InterruptedException  {
    	assertTrue("Configuration signature has not been calculated (value = 0) ", 0 != abisHelper.enableRequest(OM_G31R01.Enums.MOClass.AT, tgId).getConfigurationSignature().intValue());
    }
    
   
    private void checkMoStatusAfterUnlockedAbisIpAndTrxAndEstablishedScfOml(String gsmSectorLdn, String abisIpLdn) {
    	checkGsmSectorMoAttributeAfterCreateLinkEstablished(gsmSectorLdn);
    	checkAbisIpMoAttributeAfterCreateUnlockBscConnected(abisIpLdn);
    }
    
    private void checkMoStatusAfterUnlockedAbisIpAndTrxAndMosSoScfAoTfStartedAoAtEnabled(String gsmSectorLdn, String abisIpLdn) {
    	checkGsmSectorMoAttributeAfterCreateLinkEstablishedAllMosStartedAoAtEnabled(gsmSectorLdn);
    	checkAbisIpMoAttributeAfterCreateUnlockBscConnected(abisIpLdn);
     }
    
    private void checkMoStatusAfterUnlockedAbisIpAndTrxAndMosSoScfAoTfStartedAoAtEnabledAllLinksUp(int sectorId, int noTrxsInSector) {
        String gsmSectorLdn = "ManagedElement=1,BtsFunction=1,GsmSector=" + sectorId;
        String abisIpLdn = gsmSectorLdn + ",AbisIp=" + sectorId;
        checkGsmSectorMoAttributeAfterCreateLinkEstablishedAllMosStartedAoAtEnabled(gsmSectorLdn);
        checkAbisIpMoAttributeAfterCreateUnlockBscConnected(abisIpLdn);
        for (int i = 0; i < noTrxsInSector; i++)
        {
            String trxLdn = gsmSectorLdn + ",Trx=" + i;
            checkTrxMoAttributeAfterCreateUnlockLinksEstablished(trxLdn);
        }
    }
    
    private void createMosForOneSector(int sectorId, int noOfTrxsInSector)
    {
        List<ManagedObject> createMos = new ArrayList<ManagedObject>();
        String sectorLdn = "ManagedElement=1,BtsFunction=1,GsmSector=" + sectorId;
        String abisIpLdn = sectorLdn + ",AbisIp=" + sectorId;
        String connection_name = "host_" + sectorId;
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
        //momHelper.createSeveralMoInOneTx(createMos);
        //createMos.clear();
        for (int i = 0; i < noOfTrxsInSector; i++)
        {
            String trxLdn = sectorLdn + ",Trx=" + i;
            int txPower = 40;
            if (sectorLdn.endsWith("0"))
            {
                if (i == 5)
                {
                    txPower = 43;
                } else
                {
                    txPower = 39;
                }
            }
            ManagedObject trxMo =  momHelper.buildTrxMo(trxLdn, MomHelper.LOCKED, 
                    "ManagedElement=1,NodeSupport=1,SectorEquipmentFunction=" + getSectorEquipmentFunctionId(), txPower);
            createMos.add(trxMo);
            RestoreCommand restoreTrxMoCmd = new LockDeleteMoCommand(trxMo.getLocalDistinguishedName());
            momHelper.addRestoreCmd(restoreTrxMoCmd);
        }
        momHelper.createSeveralMoInOneTx(createMos);
//        createMos.clear();
//        for (int i = 6; i < 12; i++)
//        {
//            String trxLdn = sectorLdn + ",Trx=" + i;
//            ManagedObject trxMo =  momHelper.buildTrxMo(trxLdn, MomHelper.LOCKED, 
//                    "ManagedElement=1,SectorEquipmentFunction=" + getSectorEquipmentFunctionId());
//            createMos.add(trxMo);
//            RestoreCommand restoreTrxMoCmd = new LockDeleteMoCommand(trxMo.getLocalDistinguishedName());
//            momHelper.addRestoreCmd(restoreTrxMoCmd);
//        }
//        momHelper.createSeveralMoInOneTx(createMos);
    }

    
    
    private void unlockTrxsForOneSector(int sectorId, int noOfTrxsInSector) {
        for (int i = 0; i < noOfTrxsInSector; i++)
        {
            String trxLdn = "ManagedElement=1,BtsFunction=1,GsmSector=" + sectorId + ",Trx=" + i;
            momHelper.unlockMo(trxLdn);
        }
    }
	
	private void startSoScfAoTfAoAtSendAtConfigAtEnableAndTfEnable(int tgId) throws InterruptedException {
		abisHelper.sendStartToAllMoVisibleInGsmSector(tgId);
		abisHelper.sendAoAtConfigPreDefBundling(tgId);
		sendAoATEnable(tgId);
		 OM_G31R01.TFConfigurationResult tfConfigResult = abisHelper.tfConfigRequest(tgId, 1, new FSOffset(new Integer(0xFF), new Long(0xFFFFFFFFL)));
	        assertTrue("AccordanceIndication not according to Request", tfConfigResult.getAccordanceIndication().getAccordanceIndication() == Enums.AccordanceIndication.AccordingToRequest);
	        
		OM_G31R01.EnableResult result = abisHelper.enableRequest(this.moClassTf, tgId);
        assertEquals("AO TF is not ENABLED", OM_G31R01.Enums.MOState.ENABLED, result.getMOState());
	}
	
	private void startAllTrxcMosInTg(int tgId, int noOfTrxsInSector) throws InterruptedException {
	    for (int i = 0; i < noOfTrxsInSector; i++)
	    {
	        OM_G31R01.StartResult startResult = abisHelper.startRequest(moClassTrxc, tgId, i, i, 255);
	        assertEquals("MO state is not what expected!", OM_G31R01.Enums.MOState.STARTED, startResult.getMOState());
	        startResult = abisHelper.startRequest(moClassRx, tgId, i, i, 255);
            assertEquals("MO state is not what expected!", OM_G31R01.Enums.MOState.DISABLED, startResult.getMOState());
            startResult = abisHelper.startRequest(moClassTx, tgId, i, i, 255);
            assertEquals("MO state is not what expected!", OM_G31R01.Enums.MOState.DISABLED, startResult.getMOState());
            
            for (int j = 0; j < 8; j++) {
                startResult = abisHelper.startRequest(moClassTs, tgId, i, j, i);
                assertEquals("MO state is not what expected!", OM_G31R01.Enums.MOState.DISABLED, startResult.getMOState());
            }
                
                
	    }
	}
	
	private void enableAllRxInTg(int tgId, int noOfTrxsInSector) throws InterruptedException {
	    for (int i = 0; i < noOfTrxsInSector; i++)
        {
            OM_G31R01.RXConfigurationResult configResult = abisHelper.rxConfigRequest(tgId, i, i, i, 
                    0, true, "");
            assertEquals("AccordanceIndication don't match Request", 
                    Enums.AccordanceIndication.AccordingToRequest, configResult.getAccordanceIndication().getAccordanceIndication());
            
            OM_G31R01.EnableResult enableResult = abisHelper.enableRequest(moClassRx, tgId, i, i, 255);
            
            assertEquals("MoState is not ENABLED", OM_G31R01.Enums.MOState.ENABLED, enableResult.getMOState());
        }
        
    }
    
	private void enableTxForTgAndTrxc(int tgId, int trxc, boolean useFrequencyHopping) throws InterruptedException {
	    OM_G31R01.StatusResponse statusRsp = abisHelper.statusRequest(this.moClassTx, tgId, trxc, trxc, 255);
	    if (statusRsp.getMOState() == OM_G31R01.Enums.MOState.ENABLED) {
	        abisHelper.disableRequest(this.moClassTx, tgId, trxc, trxc, 255);
	    }
	    OM_G31R01.TXConfigurationResult confRes = abisHelper.txConfigRequest(tgId, trxc, trxc, 
	            useFrequencyHopping ? 0 : momHelper.getArfcnToUse(), useFrequencyHopping, 27, false);
        assertEquals("According Indication must be According to Request",
                Enums.AccordanceIndication.AccordingToRequest,
                confRes.getAccordanceIndication().getAccordanceIndication());

        OM_G31R01.EnableResult result = abisHelper.enableRequest(this.moClassTx, tgId, trxc, trxc, 255);
        
        assertEquals("MoState is not ENABLED", OM_G31R01.Enums.MOState.ENABLED, result.getMOState());
	}
    
    private void disconnectAndDeleteTg(int tgId) {
    	abisco.disconnectTG(tgId);
    	abisco.deleteTG(tgId);
    }
    
    private void createTgAndCell(int sectorId) {
        try {
            abisco.createTgPreDefBundling(sectorId, "host_" + sectorId, sectorToTrxs.get(sectorId), false);
            abisco.defineCell(sectorId, sectorToTrxs.get(sectorId));
        } catch (InterruptedException e) {
            setTestInfo("Caught InterruptedException");
            e.printStackTrace();
        }
    }
    
    private void setupSector(int sectorId, Integer noOfTrxsInSector, boolean connectTg) throws InterruptedException {
        if (connectTg) {
            abisco.connectTG(sectorId);
        }
     // Establish Oml link for Scf
        abisco.establishLinks(sectorId, false, 0);
        String sectorLdn = "ManagedElement=1,BtsFunction=1,GsmSector=" + sectorId;
        String abisIpLdn = sectorLdn + ",AbisIp=" + sectorId;
        checkMoStatusAfterUnlockedAbisIpAndTrxAndEstablishedScfOml(sectorLdn, abisIpLdn);
        startSoScfAoTfAoAtSendAtConfigAtEnableAndTfEnable(sectorId);
        
        ArrayList<Integer> trxList = new ArrayList<>();
        for (int i = 0; i < noOfTrxsInSector; i++)
        {
            trxList.add(i);
        }
       // assertEquals("Did not receive a valid AT Bundling Info Update", "", 
         //       abisHelper.waitForAtBundlingInfoUpdateWithPredefinedBundlingData(10, sectorId, trxList));
        checkMoStatusAfterUnlockedAbisIpAndTrxAndMosSoScfAoTfStartedAoAtEnabled(sectorLdn, abisIpLdn);       
    }
    
    private void checkMoStatusAfterReboot(int sectorId) {
        String sectorLdn = "ManagedElement=1,BtsFunction=1,GsmSector=" + sectorId;
        String abisIpLdn = sectorLdn + ",AbisIp=" + sectorId;
        assertEquals("GsmSector MO attributes did not reach expected values", "", 
                momHelper.checkGsmSectorMoAttributeAfterCreateNoLinks(sectorLdn, 5));
        assertEquals("AbisIp MO attributes did not reach expected values", "", 
                momHelper.checkAbisIpMoAttributeAfterUnlockBscConnected(abisIpLdn, abisco.getBscIpAddress(), 30));
        
        for (int i = 0; i < sectorToTrxs.get(sectorId); i++)
        {
            String trxLdn = "ManagedElement=1,BtsFunction=1,GsmSector=" + sectorId + ",Trx=" + i;
            assertEquals("Trx MO attributes did not reach expected values", "", momHelper.checkTrxMoAttributeAfterUnlockNoLinks(trxLdn, 5));
        }

    }
    
    private int getSectorEquipmentFunctionId()
    {
        int noOfTrxs = sectorEquipmentFunctions.get(currentSectorEquipmentFunction);
        
        if (noOfTrxs == 8)
        {
            currentSectorEquipmentFunction++;
            noOfTrxs = 1;
        } else
        {
            noOfTrxs++;
        }
        sectorEquipmentFunctions.set(currentSectorEquipmentFunction, noOfTrxs);
        return currentSectorEquipmentFunction + 1;
    }
    
    private void forEachSector(SectorCommands command) throws InterruptedException {
        for(int sectorId : sectorToTrxs.keySet()) {
            setTestStepBegin(testStepSlogans.get(command) + sectorId);

            switch (command) {
            case DISCONNECT_AND_DELETE_TG:
                disconnectAndDeleteTg(sectorId);
                break;
            case CREATE_TGS_AND_CELLS:
                createTgAndCell(sectorId);
                break;
            case CREATE_MOS:
                createMosForOneSector(sectorId, sectorToTrxs.get(sectorId));
                break;
            case CHECK_MO_STATUS_AFTER_REBOOT:
                checkMoStatusAfterReboot(sectorId);
                break;
            case UNLOCK_TRXS:
                unlockTrxsForOneSector(sectorId, sectorToTrxs.get(sectorId));
                break;
            case SETUP_SECTOR:
                setupSector(sectorId, sectorToTrxs.get(sectorId), true);
                break;
            case SETUP_SECTOR_AFTER_REBOOT:
                setupSector(sectorId, sectorToTrxs.get(sectorId), false);
                break;
            case ESTABLISH_ALL_LINKS: {
                ArrayList<Integer> trxList = new ArrayList<Integer>();
                int noTrxs = sectorToTrxs.get(sectorId);
                for (int i = 0; i < noTrxs; i++)
                {
                    trxList.add(i);
                }
                abisco.establishLinksForMultipleTrxs(sectorId, trxList);
                break;
            }
            case CHECK_MO_STATUS_ALL_LINKS_UP:
                checkMoStatusAfterUnlockedAbisIpAndTrxAndMosSoScfAoTfStartedAoAtEnabledAllLinksUp(sectorId, sectorToTrxs.get(sectorId));
                break;
            case START_ALL_TRXC:
                startAllTrxcMosInTg(sectorId, sectorToTrxs.get(sectorId));
                break;
            case ENABLE_ALL_RX:
                enableAllRxInTg(sectorId, sectorToTrxs.get(sectorId));
                break;
            case ENABLE_ALL_TX_HOPPING:
                for (int i = 0; i < sectorToTrxs.get(sectorId); i++) {
                    enableTxForTgAndTrxc(sectorId, i, true);
                }
                    
            }
        }
    }
    
    
    /**
     * resourceAllocationWithMultipleTrxs
     * @description Testing that resource allocation works by unlocking several TRX:s. This test case needs to be run a node
     * with more than one RUS connected.
     * 
     * @param testId - unique identifier of the test case
     * @param description
     * @param gsmSectorToTrxsMapping 
     * @throws InterruptedException 
     */
    @Test(timeOut = 850000)
    @Parameters({ "testId", "description", "gsmSectorToTrxsMapping", "rebootNode" })
    public void resourceAllocationWithMultipleTrxs(String testId, String description, 
            String gsmSectorToTrxsMapping, String rebootNode) throws InterruptedException {
        setTestCase(testId, description);
        
    	if (!momHelper.isBtsFunctionCreated()) {
    		setTestInfo("Precondition: Create BtsFunction MO");
    		momHelper.createBtsFunctionMo(MomHelper.BTS_FUNCTION_LDN);
    	}

        for (String sectorToTrxsMapping : gsmSectorToTrxsMapping.split(",")) {
            String[] parts = sectorToTrxsMapping.split(":");
            assertEquals("Wrong format of sector to trxs mapping. Expected <sector id>:<no trxs per sector>, e.g. 0:4",
                    2, parts.length);
            sectorToTrxs.put(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
        }

        boolean shouldReboot = (rebootNode.equalsIgnoreCase("true"));
        for (int i = 0; i < 6; i++)
        {
            sectorEquipmentFunctions.add(0);
        }
        currentSectorEquipmentFunction = 0;    
        
        setTestStepBegin("TN");
        if (!momHelper.isTnConfigured())
            momHelper.configureTn(abisco.getBscIpAddress());
        setTestStepEnd();

        forEachSector(SectorCommands.DISCONNECT_AND_DELETE_TG);
        
        setTestStepBegin("********* Pre-test: Start TSS");
        abisco.startTss();
        setTestStepEnd();
        
        forEachSector(SectorCommands.CREATE_TGS_AND_CELLS);
        forEachSector(SectorCommands.CREATE_MOS);
        forEachSector(SectorCommands.UNLOCK_TRXS);
        forEachSector(SectorCommands.SETUP_SECTOR);
        forEachSector(SectorCommands.ESTABLISH_ALL_LINKS);
        forEachSector(SectorCommands.CHECK_MO_STATUS_ALL_LINKS_UP);
        forEachSector(SectorCommands.START_ALL_TRXC);
        forEachSector(SectorCommands.ENABLE_ALL_RX);
        forEachSector(SectorCommands.ENABLE_ALL_TX_HOPPING);
        sleepSeconds(300);
        if (shouldReboot) {

            SortedSet<Integer> sortedSectorIds = new TreeSet<Integer>(sectorToTrxs.keySet());
            String lastTrxInLastSectorLdn = "ManagedElement=1,BtsFunction=1,GsmSector=" + sortedSectorIds.last() + 
                    ",Trx=" + (sectorToTrxs.get(sortedSectorIds.last()) - 1);
            momHelper.lockMo(lastTrxInLastSectorLdn);
            momHelper.deleteMo(lastTrxInLastSectorLdn);

            int noOfTrxsInLastSector = sectorToTrxs.get(sortedSectorIds.last());
            sectorToTrxs.put(sortedSectorIds.last(), noOfTrxsInLastSector - 1);

            setTestStepBegin("********* Time to reboot the node.");
            pidPrePost.postCond(); // needed since the processes will have a different pid after the restart   
            assertTrue("Node didnt come alive after reboot...", cliCommands.rebootAndWaitForNodeNoCleanup());
            setTestStepEnd();


            forEachSector(SectorCommands.CHECK_MO_STATUS_AFTER_REBOOT);
            forEachSector(SectorCommands.SETUP_SECTOR_AFTER_REBOOT);
            forEachSector(SectorCommands.ESTABLISH_ALL_LINKS);
            forEachSector(SectorCommands.CHECK_MO_STATUS_ALL_LINKS_UP);
            forEachSector(SectorCommands.START_ALL_TRXC);
            sleepSeconds(20);
            forEachSector(SectorCommands.ENABLE_ALL_RX);
            forEachSector(SectorCommands.ENABLE_ALL_TX_HOPPING);
        }
        //Commented out for now, until we start traffic testing on several trx:es
//        //for(int sectorId : sectorToTrxs.keySet()) {
//        int sectorId = 1;
//            enableTxForTgAndTrxc(sectorId, 0, false);
//            Map<Integer, Integer> frequencyList = new HashMap<Integer, Integer>();
//            frequencyList.put(0, momHelper.getArfcnToUse());
//            enableTsForTgAndTrxc(sectorId, 0, 0, frequencyList, OM_G31R01.Enums.Combination.MainBCCH);
//            enableTsForTgAndTrxc(sectorId, 0, 1, frequencyList, OM_G31R01.Enums.Combination.SDCCH);
//            enableTsForTgAndTrxc(sectorId, 0, 2,  frequencyList, OM_G31R01.Enums.Combination.TCH);
//            abisHelper.sendBCCHInfo(sectorId, 0);
//            sleepSeconds(5000);
//            disableTsForTgAndTrxc(sectorId, 0, 0);
//            disableTsForTgAndTrxc(sectorId, 0, 1);
//            disableTsForTgAndTrxc(sectorId, 0, 2);
//            enableTxForTgAndTrxc(sectorId, 0, true);
//        //}
    }

    /**
     * resourceAllocationFailure
     * @description Testing that we get an alarm if we unlock more Trx:s than the resource allocation algorithm can handle
     * This is currently done by first creating two GsmSectors with 6 Trx:s in each. This will mean that they will be allocated
     * on one Emca. Then we try to unlock 3 more Trx:s in the second GsmSector. Since Trx:s beloning to the same GsmSector must be
     * allocated to the same Emca these 3 new Trx:s won't fit in to the Emca and an alarm will be raised.
     * 
     * @param testId - unique identifier of the test case
     * @param description
     * @throws InterruptedException 
     */
    @Test(timeOut = 850000)
    @Parameters({ "testId", "description"})
    public void resourceAllocationFailure(String testId, String description) throws InterruptedException {
        setTestCase(testId, description);

    	if (!momHelper.isBtsFunctionCreated()) {
    		setTestInfo("Precondition: Create BtsFunction MO");
    		momHelper.createBtsFunctionMo(MomHelper.BTS_FUNCTION_LDN);
    	}
        
        sectorToTrxs.put(0, 6);
        sectorToTrxs.put(1, 6);
        
        for (int i = 0; i < 6; i++)
        {
            sectorEquipmentFunctions.add(0);
        }
        currentSectorEquipmentFunction = 0;    
        
        setTestStepBegin("TN");
        if (!momHelper.isTnConfigured())
            momHelper.configureTn(abisco.getBscIpAddress());
        setTestStepEnd();

        forEachSector(SectorCommands.DISCONNECT_AND_DELETE_TG);
        
        setTestStepBegin("********* Pre-test: Start TSS");
        abisco.startTss();
        setTestStepEnd();
        
        forEachSector(SectorCommands.CREATE_TGS_AND_CELLS);
        forEachSector(SectorCommands.CREATE_MOS);
        forEachSector(SectorCommands.UNLOCK_TRXS);
        forEachSector(SectorCommands.SETUP_SECTOR);

        
        setTestStepBegin("********* Create additional Trx:s");
        
        String parentLdn = "ManagedElement=1,BtsFunction=1,GsmSector=1";
        
        for (int i = 6; i < 9; i++) {
            momHelper.createTrxMo(parentLdn, i + "", "ManagedElement=1,NodeSupport=1,SectorEquipmentFunction=" + getSectorEquipmentFunctionId());
        }
        setTestStepEnd();
        
        //sleepSeconds(3600);
        
        setTestStepBegin("********* Unlock and check that we have alarms on all additional Trx:s");
        abisHelper.clearStatusUpdate();
        for (int i = 6; i < 9; i++) {
            boolean alarmRaised = false;
            String trxLdn = parentLdn + ",Trx=" + i;
            momHelper.unlockMo(trxLdn);
                
            momHelper.checkTrxMoAttributesAfterUnlockNoBbResources(trxLdn, 10);
            sleepSeconds(9);
            List<ComAlarm> alarms = alarmHelper.getAlarmList();
            
            for (ComAlarm alarm : alarms) {
                logger.info("alarm: major:" + alarm.getMajorType() + ", minor:"
                        + alarm.getMinorType() + ", eventType:"
                        + alarm.getEventType() + ", mo:" + alarm.getManagedObject());
                if (trxLdn.equals(alarm.getManagedObject())) {
                    alarmRaised = true;
                    break;
                }
            }
            assertTrue("No alarm raised for Trx: " + trxLdn, alarmRaised);
        }
        G31StatusUpdate statusUpdate = abisHelper.waitForSoScfStatusUpdate(10, 1);
        assertNotNull("No status updated received", statusUpdate);
        G31OperationalCondition operCond = statusUpdate.getStatusChoice().getG31StatusSCF().getG31OperationalCondition();
        G31OperationalConditionReasonsMap reasonsMap = statusUpdate.getStatusChoice().getG31StatusSCF().getG31OperationalConditionReasonsMap();
        assertTrue("Operational condition is not degraded", 
        		operCond.getOperationalCondition() == BG.Enums.OperationalCondition.Degraded);
        assertEquals("TRXC internal fault bit not set in reasons map", reasonsMap.getNoFrameSynch(), 1);
        		
        setTestStepEnd();
        
        setTestStepBegin("********* Lock each additional Trx and check that the corresponding alarm is cleared");
        
        for (int i = 6; i < 9; i++) {
            boolean alarmCleared = true;
            String trxLdn = parentLdn + ",Trx=" + i;
            momHelper.lockMo(trxLdn);
            momHelper.checkTrxMoAttributeAfterLock(trxLdn, 10);
            
            sleepSeconds(9);
            List<ComAlarm> alarms = alarmHelper.getAlarmList();
            
            for (ComAlarm alarm : alarms) {
                logger.info("alarm: major:" + alarm.getMajorType() + ", minor:"
                        + alarm.getMinorType() + ", eventType:"
                        + alarm.getEventType() + ", mo:" + alarm.getManagedObject());
                if (trxLdn.equals(alarm.getManagedObject())) {
                    alarmCleared = false;
                    break;
                }
            }
            assertTrue("Alarm not cleared for Trx: " + trxLdn, alarmCleared);
        }
        
        statusUpdate = abisHelper.waitForSoScfStatusUpdate(10, 1);
        assertNotNull("No status updated received", statusUpdate);
        operCond = statusUpdate.getStatusChoice().getG31StatusSCF().getG31OperationalCondition();
        assertTrue("Operational condition is still degraded", 
        		operCond.getOperationalCondition() == BG.Enums.OperationalCondition.Operational);
     
    }
    
}   
  
      
