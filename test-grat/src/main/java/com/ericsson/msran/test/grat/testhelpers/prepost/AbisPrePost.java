package com.ericsson.msran.test.grat.testhelpers.prepost;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

import com.ericsson.msran.jcat.TestBase;
import com.ericsson.abisco.clientlib.servers.CMDHAND;
import com.ericsson.abisco.clientlib.servers.OM_G31R01;
import com.ericsson.abisco.clientlib.servers.OM_G31R01.Enums;
import com.ericsson.abisco.clientlib.servers.OM_G31R01.FSOffset;
import com.ericsson.commonlibrary.managedobjects.ManagedObject;
import com.ericsson.commonlibrary.restorestack.RestoreCommand;
import com.ericsson.msran.test.grat.testhelpers.AbisHelper;
import com.ericsson.msran.test.grat.testhelpers.AbiscoConnection;
import com.ericsson.msran.test.grat.testhelpers.MomHelper;
import com.ericsson.msran.test.grat.testhelpers.TgLdns;
import com.ericsson.msran.test.grat.testhelpers.restorecommands.DeleteMoCommand;
import com.ericsson.msran.test.grat.testhelpers.restorecommands.LockDeleteMoCommand;

/**
 * @name AbisPrePost
 * 
 * @author Asa Huhtasaari (xasahuh)
 * 
 * @created 2014-02-21
 * 
 * @description Class for preconditions and postconditions for Abis test cases.
 *              The class shall be instantiated in the Setup method of test cases.
 * 
 * @revision xasahuh 2014-02-21 First version.
 * 
 */
public class AbisPrePost extends TestBase {
    private final OM_G31R01.Enums.MOClass  moClassScf = OM_G31R01.Enums.MOClass.SCF;
    private final OM_G31R01.Enums.MOClass  moClassTf = OM_G31R01.Enums.MOClass.TF;
    private final OM_G31R01.Enums.MOClass  moClassAt = OM_G31R01.Enums.MOClass.AT;
    private final OM_G31R01.Enums.MOClass  moClassTrxc = OM_G31R01.Enums.MOClass.TRXC;
    private final OM_G31R01.Enums.MOClass  moClassTx = OM_G31R01.Enums.MOClass.TX;
    private final OM_G31R01.Enums.MOClass  moClassRx = OM_G31R01.Enums.MOClass.RX;
    private final OM_G31R01.Enums.MOClass  moClassTs = OM_G31R01.Enums.MOClass.TS;
    
    private MomHelper momHelper;
    private AbiscoConnection abisco;
    private AbisHelper abisHelper;
    private TgLdns tgLdns;

    /**
     * Constructor to initialize Abis pre- and postcondition setup and check.
     */
    public AbisPrePost() {
        momHelper = new MomHelper();
        abisHelper = new AbisHelper();
        abisco = new AbiscoConnection();
        abisco.setupAbisco(false);
        tgLdns = new TgLdns(MomHelper.SECTOR_LDN, MomHelper.ABIS_IP_LDN, MomHelper.TRX_LDN); 
    }

    public AbisPrePost(int trxesPerTg) {
        momHelper = new MomHelper();
        abisHelper = new AbisHelper();
        abisco = new AbiscoConnection();
        abisco.setupAbisco(1, trxesPerTg, false);
        tgLdns = new TgLdns(MomHelper.SECTOR_LDN, MomHelper.ABIS_IP_LDN); 
        
        for (int i=0 ; i < trxesPerTg; ++i) {
        	tgLdns.trxLdnList.add(MomHelper.SECTOR_LDN + ",Trx=" + i);
        }
    }
    
    /**
     * @name preCondAllMoStateReset
     * 
     * @description Setup and check preconditions for test cases that expects
     *              SO SCF and AO TF to be RESET.
     */
    public void preCondAllMoStateReset() {
        basicPreCond();
        setTestInfo("Precondition: MO:GsmSector check that the attributes are in the correct state");
        assertEquals(tgLdns.sectorLdn + " did not reach the expected state", "", momHelper.checkGsmSectorMoAttributeAfterCreateLinksEstablished(tgLdns.sectorLdn, 10));
    }
    
    /**
     * @name preCondScfMoStateStarted
     * 
     * @description Setup and check preconditions for test cases that expects
     *              GsmSector MOs SCF, AT, and TF to be started.
     *              
     * @throws InterruptedException
     *              
     */
    public void preCondScfMoStateStarted() throws InterruptedException {
        basicPreCond();
        setTestInfo("Precondition: start SCF");
        abisHelper.startRequest(this.moClassScf, 0);
        
        setTestInfo("Precondition: start AT");
        abisHelper.startRequest(this.moClassAt, 0);
        
        setTestInfo("Precondition: start TF");
        abisHelper.startRequest(this.moClassTf, 0);
        
        setTestInfo("Check that the MOs are in the correct state");
        assertEquals("All MOs were not started", "", momHelper.checkGsmSectorMoAttributeAfterAllMosStarted(tgLdns.sectorLdn, 10));
    }

    public void preCondScfMoStateStartedAfterRestart() throws InterruptedException {
        try {
            abisco.establishLinks();
        } catch (InterruptedException e) {
            fail("Establish SCF Links failed");
        }
        
        setTestInfo("Precondition: MO:GsmSector check that SCF OML is UP " + tgLdns.sectorLdn);
        assertEquals("All MOs were not started", "", momHelper.checkGsmSectorMoAttributeAfterCreateLinksEstablished(tgLdns.sectorLdn, 30));
        
        setTestInfo("Precondition: start SCF");
        abisHelper.startRequest(this.moClassScf, 0);
        
        setTestInfo("Precondition: start AT");
        abisHelper.startRequest(this.moClassAt, 0);
        
        setTestInfo("Precondition: start TF");
        abisHelper.startRequest(this.moClassTf, 0);
        
        setTestInfo("Precondition: MO:GsmSector check attributes have expected state " + tgLdns.sectorLdn);
        assertEquals("All MOs were not started", "", momHelper.checkGsmSectorMoAttributeAfterAllMosStarted(tgLdns.sectorLdn, 10));
    }

    
    /**
     * @name preCondScfMoStateStartedAtActive
     * 
     * @description Setup and check preconditions for test cases that expects
     *              GsmSector MOs SCF, AT, and TF to be started with an active
     *              AT configuration.
     *              
     * @throws InterruptedException
     *              
     */
    public void preCondScfMoStateStartedAtTfActive() throws InterruptedException {
        preCondScfMoStateStarted();
        
        setTestInfo("Precondition: configure AT");
        abisHelper.sendAoAtConfigPreDefBundling(0);
        
        setTestInfo("Precondition: enable AT");
        assertTrue("Configuration signature has not been calculated (value = 0) ", 
                0 != abisHelper.enableRequest(OM_G31R01.Enums.MOClass.AT, 0).getConfigurationSignature());
        
        assertEquals("Did not receive an AT Bundling Info Update", "", abisHelper.waitForAtBundlingInfoUpdateWithBundlingData(10, 0));

        setTestInfo("Establish Trx OML & RSL");
        abisco.establishLinks(true);

        setTestInfo("Precondition: configure AO TF");
        OM_G31R01.TFConfigurationResult tfConfigResult = abisHelper.tfConfigRequest(1, Enums.TFMode.Master, abisHelper.getOM_G31R01fsOffsetByActiveSyncSrc());
        assertTrue("AccordanceIndication not according to Request", tfConfigResult.getAccordanceIndication().getAccordanceIndication() == Enums.AccordanceIndication.AccordingToRequest);
        
        setTestInfo("Precondition: enable AO TF");
        assertTrue("Configuration signature has not been calculated (value = 0) ", 
                   "MO state is not ENABLED", OM_G31R01.Enums.MOState.ENABLED == abisHelper.enableRequest(this.moClassTf, 0).getMOState());

        setTestInfo("Precondition: MO:GsmSector attribute:abisAtState and attribute:abisTfState is ENABLED");
        assertEquals("All MOs were not ENABLED", "", momHelper.checkGsmSectorMoAttributeAfterAllMosStartedAoAtAndAoTfEnabled(tgLdns.sectorLdn, 10));
    }

    public void preCondScfMoStateStartedAtTfActiveAfterRestart() throws InterruptedException {
        preCondScfMoStateStartedAfterRestart();
        
        setTestInfo("Precondition: configure AT");
        abisHelper.sendAoAtConfigPreDefBundling(0);
        
        setTestInfo("Precondition: enable AT");
        assertTrue("Configuration signature has not been calculated (value = 0) ", 
                0 != abisHelper.enableRequest(OM_G31R01.Enums.MOClass.AT, 0).getConfigurationSignature());
        
        assertEquals("Did not receive an AT Bundling Info Update", "", abisHelper.waitForAtBundlingInfoUpdateWithBundlingData(10, 0));
        
        setTestInfo("Establish Trx OML & RSL");
        abisco.establishLinks(true);
               
        setTestInfo("Precondition: configure AO TF");
        OM_G31R01.TFConfigurationResult tfConfigResult = abisHelper.tfConfigRequest(1, Enums.TFMode.Master, abisHelper.getOM_G31R01fsOffsetByActiveSyncSrc());
        assertTrue("AccordanceIndication not according to Request", tfConfigResult.getAccordanceIndication().getAccordanceIndication() == Enums.AccordanceIndication.AccordingToRequest);
        
        setTestInfo("Precondition: enable AO TF");
        assertTrue("Configuration signature has not been calculated (value = 0) ", 
                   "MO state is not ENABLED", OM_G31R01.Enums.MOState.ENABLED == abisHelper.enableRequest(this.moClassTf, 0).getMOState());
        
        setTestInfo("Precondition: MO:GsmSector attribute:abisAtState and attribute:abisTfState is ENABLED");
        assertEquals("All MOs were not ENABELD", "", momHelper.checkGsmSectorMoAttributeAfterAllMosStartedAoAtAndAoTfEnabled(tgLdns.sectorLdn, 10));
    }


    /**
     * @name preCondAllMoStateStarted
     * 
     * @description Setup and check preconditions for test cases that expects
     *              all MOs to be in state STARTED. AT will be ENABLED.
     *              
     * @throws InterruptedException
     * @throws JSONException 
     *              
     */
    public void preCondAllMoStateStarted() throws InterruptedException, JSONException {
        preCondScfMoStateStartedAtTfActive();
        
        setTestInfo("Precondition: start TRXC");
        abisHelper.startRequest(this.moClassTrxc, 0);
        
        setTestInfo("Precondition: start TX");
        abisHelper.startRequest(this.moClassTx, 0);
        
        setTestInfo("Precondition: start RX");
        abisHelper.startRequest(this.moClassRx, 0);
        
        int tsAssociatedSoInstance = 0;
        int tsInstance;
        for (tsInstance=0; tsInstance<8; tsInstance++)
        {
            setTestInfo("Precondition: start TS, instance=" + tsInstance);
            abisHelper.startRequest(this.moClassTs, tsInstance, tsAssociatedSoInstance);
        }
        
    	setTestInfo("Precondition: Verify Trx MO states after start of all MOs for " + tgLdns.trxLdnList.get(0));
        assertEquals(tgLdns.trxLdnList.get(0) + " did not reach correct state", "", 
                momHelper.checkTrxMoAttributeAfterUnlockAllStartedLinksEstablished(tgLdns.trxLdnList.get(0), 10));
    }

    public void preCondAllMoStateStartedAfterRestart() throws InterruptedException, JSONException {
        preCondScfMoStateStartedAtTfActiveAfterRestart();
        
        setTestInfo("Precondition: start TRXC");
        abisHelper.startRequest(this.moClassTrxc, 0);
        
        setTestInfo("Precondition: start TX");
        abisHelper.startRequest(this.moClassTx, 0);
        
        setTestInfo("Precondition: start RX");
        abisHelper.startRequest(this.moClassRx, 0);
        
        int tsAssociatedSoInstance = 0;
        int tsInstance;
        for (tsInstance=0; tsInstance<8; tsInstance++)
        {
            setTestInfo("Precondition: start TS, instance=" + tsInstance);
            abisHelper.startRequest(this.moClassTs, tsInstance, tsAssociatedSoInstance);
        }
        
    	setTestInfo("Precondition: Verify Trx MO states after start of all MOs for " + tgLdns.trxLdnList.get(0));
        assertEquals(tgLdns.trxLdnList.get(0) + " did not reach correct state", "", 
                momHelper.checkTrxMoAttributeAfterUnlockAllStartedLinksEstablished(tgLdns.trxLdnList.get(0), 10));
    }

    
    /**
     * @name preCondTrxStateStartedMy
     * @param tgId
     * @param trxId
     * @throws InterruptedException
     * @throws JSONException
     */
    public void preCondTrxStateStartedMy(int tgId, int trxId) throws InterruptedException, JSONException {
    	//  preCondScfMoStateStartedAtTfActive();

    	setTestInfo("Precondition: start TRXC");

    	abisHelper.startRequest(this.moClassTrxc,tgId,trxId,trxId,255);

    	setTestInfo("Precondition: start TX");
    	abisHelper.startRequest(this.moClassTx,tgId,trxId,trxId,255);

    	setTestInfo("Precondition: start RX");
    	abisHelper.startRequest(this.moClassRx,tgId,trxId,trxId,255);

    	int tsAssociatedSoInstance = trxId;
    	int tsInstance;
    	for (tsInstance=0; tsInstance<8; tsInstance++)
    	{
    		setTestInfo("Precondition: start TS, instance=" + tsInstance);
    		abisHelper.startRequest(this.moClassTs,tgId,trxId,tsInstance,tsAssociatedSoInstance);

    	}  
    }
  
    public void checkAbisTsMoState(String expectedState) throws JSONException {
        for (int tsInstance=0; tsInstance<8; tsInstance++)
        {
        	setTestInfo("Check MO:Trx attribute:abisTsMoState[" + tsInstance +"] is " + expectedState);
			assertTrue("Trx abisTsMoState[" + tsInstance +"] is not " + expectedState, momHelper.waitForAbisTsMoState(tsInstance, expectedState, 5));
        }
    } 
    
    
    /**
     * @name preCondSoScfStateStarted
     * 
     * @description Setup and check preconditions for test cases that expects
     *              SO SCF to be STARTED.
     *              
     * @throws InterruptedException             
     */
    public void preCondSoScfStarted() throws InterruptedException {
        
        basicPreCond();
        
        setTestInfo("Precondition: start SCF");
        startSoScf();
    }
    
    public void startSoScf()  throws InterruptedException {
        abisHelper.startRequest(this.moClassScf, 0);
        
        setTestInfo("MO:GsmSector attribute:abisScfState is STARTED");
        assertTrue("MO " + tgLdns.sectorLdn + " abisScfState is not STARTED", 
        		momHelper.waitForMoAttributeStringValue(tgLdns.sectorLdn, MomHelper.ABIS_SO_SCF_STATE, MomHelper.STARTED, 10));
    }
    
    
    /**
     * @name preCondSoScfStateReset
     * 
     * @description Setup and check preconditions for test cases that expects
     *              SO SCF to be RESET.
     *                     
     */
    public void preCondSoScfReset() {
        
        basicPreCond();
 
        setTestInfo("Precondition: MO:GsmSector attribute:abisSoScfState is RESET");
        assertTrue("MO " + tgLdns.sectorLdn + " abisScfState is not RESET", 
        		momHelper.waitForMoAttributeStringValue(tgLdns.sectorLdn, MomHelper.ABIS_SO_SCF_STATE, MomHelper.RESET, 10));
    }
    
   public void startSoTrxc() throws InterruptedException {
        abisHelper.startRequest(this.moClassTrxc, 0);
        
        setTestInfo("Precondition: MO:Trx attribute:abisTrxcState is STARTED");
        assertTrue("Trx abisTrxcState is not STARTED", 
        		momHelper.waitForMoAttributeStringValue(tgLdns.trxLdnList.get(0), MomHelper.ABIS_SO_TRXC_STATE, MomHelper.STARTED, 10));
    }
    
   
    /**
     * @name preCondSoScfAndSoTrxcReset
     * 
     * @description Setup and check preconditions for test cases that expects
     *              SO TRXC to be STARTED.
     *              
     * @throws InterruptedException             
     */
    public void preCondSoScfAndSoTrxcReset() throws InterruptedException {
        basicPreCond();

        setTestInfo("Precondition: MO:GsmSector attribute:abisSoScfState is RESET");
        assertTrue("MO " + tgLdns.sectorLdn + " abisScfState is not RESET", 
        		momHelper.waitForMoAttributeStringValue(tgLdns.sectorLdn, MomHelper.ABIS_SO_SCF_STATE, MomHelper.RESET, 30));
        
        setTestInfo("Precondition: start TRXC");
        abisHelper.resetCommand(this.moClassTrxc);
        
        setTestInfo("Precondition: MO:Trx attribute:abisSoTrxcState is RESET");
        assertTrue("MO " + tgLdns.trxLdnList.get(0) + " abisScfState is not RESET", 
        		momHelper.waitForMoAttributeStringValue(tgLdns.trxLdnList.get(0), MomHelper.ABIS_SO_TRXC_STATE, MomHelper.RESET, 30));
    }
    
    /**
     * @throws InterruptedException
     */
    public void preCondAoTxReset() throws InterruptedException {
        
        basicPreCond();
        
        setTestInfo("Precondition: reset AO TX");
        abisHelper.resetCommand(this.moClassTx);
        
        setTestInfo("Precondition: MO:Trx attribute:abisTxState is RESET");
        assertTrue("Trx abisTxState is not RESET", 
        		momHelper.waitForMoAttributeStringValue(tgLdns.trxLdnList.get(0), MomHelper.ABIS_TX_MO_STATE, MomHelper.RESET, 10));
    }
    
    public void basicPreCond() {
        if (!momHelper.isTnConfigured()) {
        	momHelper.configureTn(abisco.getBscIpAddress());
        }
    	
    	List<ManagedObject> createMos = new ArrayList<ManagedObject>();
    	
    	setTestInfo("Create all GRAT MOs as unlocked");
    	if (!momHelper.isBtsFunctionCreated()) {
    		setTestInfo("Precondition: BtsFunction MO shall be created");
    		createMos.add(momHelper.buildBtsFunctionMo(MomHelper.BTS_FUNCTION_LDN, MomHelper.BTS_USER_LABEL_VALUE));
    		momHelper.addRestoreCmd(new DeleteMoCommand(MomHelper.BTS_FUNCTION_LDN));
    	}

    	// GsmSector MO
    	createMos.add(momHelper.buildGsmSectorMo(tgLdns.sectorLdn));
    	momHelper.addRestoreCmd(new DeleteMoCommand(tgLdns.sectorLdn));

    	// AbisIp MO as unlocked
    	createMos.add(momHelper.buildAbisIpMo(AbiscoConnection.getConnectionName(),
    										  abisco.getBscIpAddress(),
    										  tgLdns.abisIpLdn,
    										  MomHelper.TNA_IP_LDN,
    										  MomHelper.UNLOCKED));
    	momHelper.addRestoreCmd(new LockDeleteMoCommand(tgLdns.abisIpLdn));

    	// Trx MO as unlocked
    	createMos.add(momHelper.buildTrxMo(tgLdns.trxLdnList.get(0), MomHelper.UNLOCKED, momHelper.getSectorEquipmentFunctionLdn()));
    	momHelper.addRestoreCmd(new LockDeleteMoCommand(tgLdns.trxLdnList.get(0)));

    	momHelper.createSeveralMoInOneTx(createMos);    	
    	setTestInfo("All GRAT MOs shall now have been created as unlocked");
    	
    	
    	
        setTestInfo("Precondition: Verify MO:AbisIp after unlock " + tgLdns.abisIpLdn);
        assertEquals(tgLdns.abisIpLdn + " did not reach correct state", "", 
                momHelper.checkAbisIpMoAttributeAfterUnlockBscConnected(tgLdns.abisIpLdn, abisco.getBscIpAddress(), 30));

        setTestInfo("Precondition: Verify MO:Trx after unlock " + tgLdns.trxLdnList.get(0));
        assertEquals(tgLdns.trxLdnList.get(0) + " did not reach correct state", "", 
                momHelper.checkTrxMoAttributeAfterUnlockNoLinks(tgLdns.trxLdnList.get(0), 30));
   
        // Establish SCF links to the Abisco
        try {
            abisco.establishLinks();
        } catch (InterruptedException e) {
            fail("Establish SCF Links failed");
        }
        
        setTestInfo("Precondition: Verify MO:GsmSector after establish SCF OML " + tgLdns.sectorLdn);
        assertEquals(tgLdns.sectorLdn + " did not reach correct state", "", 
                momHelper.checkGsmSectorMoAttributeAfterCreateLinksEstablished(tgLdns.sectorLdn, 30));
    }
    
    /**
     * @name establishLinks
     * 
     * @description Establishes the links to the Abisco. Should be used when 
     *              links goes down, e.g. after sleeping for too long.
     */
    public void establishLinks()
    {
        try {
        	abisco.establishLinks();
        } catch (InterruptedException ie) {
        	fail("InteruptedException during establishLinks");
        }
    }
    
    /**
     * @name establishLinks
     * 
     * @description Establishes the links to the Abisco. Should be used when 
     *              links goes down, e.g. after sleeping for too long.
     */
    public void establishLinks(boolean establishTrxc)
    {
        try {
        	abisco.establishLinks(establishTrxc);
        } catch (InterruptedException ie) {
        	fail("InteruptedException during establishLinks");
        }
    }
}
