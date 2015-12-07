package com.ericsson.msran.test.grat.testhelpers.prepost;

import java.util.ArrayList;
import java.util.List;

import com.ericsson.abisco.clientlib.servers.OM_G31R01;
import com.ericsson.commonlibrary.managedobjects.ManagedObject;
import com.ericsson.commonlibrary.restorestack.RestoreCommand;
import com.ericsson.msran.jcat.TestBase;
import com.ericsson.msran.test.grat.testhelpers.AbisHelper;
import com.ericsson.msran.test.grat.testhelpers.AbiscoConnection;
import com.ericsson.msran.test.grat.testhelpers.MomHelper;
import com.ericsson.msran.test.grat.testhelpers.restorecommands.DeleteMoCommand;
import com.ericsson.msran.test.grat.testhelpers.restorecommands.LockDeleteMoCommand;

public class AbisPrePostWithTwoSectors extends TestBase {
    
    private MomHelper momHelper;
    private AbiscoConnection abisco;
    private AbisHelper abisHelper;
                                               
    public static final String gsmSectorLdn0 = "ManagedElement=1,BtsFunction=1,GsmSector=0";
    public static final String gsmSectorLdn1 = "ManagedElement=1,BtsFunction=1,GsmSector=1";
    private final String abisIpLdn0 = gsmSectorLdn0 + ",AbisIp=0";
    private final String abisIpLdn1 = gsmSectorLdn1 + ",AbisIp=1";
    private final String trxLdnSector0 = gsmSectorLdn0 + ",Trx=0";
    private final String trxLdnSector1 = gsmSectorLdn1 + ",Trx=0";
    
    public static final int tg_0 = 0;
    public static final int tg_1 = 1;
    private String connection_name_0 = "host_0";
    private String connection_name_1 = "host_1";
    
    public AbisPrePostWithTwoSectors() {
        momHelper = new MomHelper();
        abisHelper = new AbisHelper();
        abisco = new AbiscoConnection();
    }
    
    public void preCondAllMoStarted() throws InterruptedException {
    	
        setTestStepBegin("TN");
        if (!momHelper.isTnConfigured())
        	momHelper.configureTn(abisco.getBscIpAddress());
        
        
        setTestStepBegin("********* Pre-test: Make sure that TG:s are disconnected and deleted");
        abisco.disconnectAndDeleteTG(tg_0);
    	abisco.disconnectAndDeleteTG(tg_1);
        setTestStepEnd();

        setTestStepBegin("********* Pre-test: Start TSS");
        abisco.startTss();
        setTestStepEnd();
               
        
        setTestStepBegin("********* Pre-test: Create two TGs with one TRX each");
        createTgsAndCells();
        setTestStepEnd();
        
    	if (!momHelper.isBtsFunctionCreated()) {
    		setTestInfo("Precondition: Create BtsFunction MO");
    		momHelper.createBtsFunctionMo(MomHelper.BTS_FUNCTION_LDN);
    	}       
        
    	//step1: create two sectors and abisIp, Trx
    	createTwoSectorsAbisIpTrx();
    	
    	//check mom status before establishing scf link
    	setTestInfo("********* Connect tg **********");
    	abisco.connectTG(tg_0);
    	abisco.connectTG(tg_1);
    	
    	checkMoStatusAfterUnlockedAbisIpAndTrxAndConnectedTG(gsmSectorLdn0, abisIpLdn0, trxLdnSector0);
    	checkMoStatusAfterUnlockedAbisIpAndTrxAndConnectedTG(gsmSectorLdn1, abisIpLdn1, trxLdnSector1);
    	
    	//establish scf link
    	establishScfOmlLink();
    	checkMoStatusAfterUnlockedAbisIpAndTrxAndEstablishedScfOml(gsmSectorLdn0, abisIpLdn0, trxLdnSector0);
    	checkMoStatusAfterUnlockedAbisIpAndTrxAndEstablishedScfOml(gsmSectorLdn1, abisIpLdn1, trxLdnSector1);
    	
    	/*step2: start scf and At Tf activate
    	 * started scf, at, tf
    	 * send ao at config 
    	 * establish trx oml and rsl
    	 * config tf and enable tf
    	 */
    	startSoScfAoTfAoAtSendAtConfigAndAtEnable(tg_0);
    	startSoScfAoTfAoAtSendAtConfigAndAtEnable(tg_1);
    	
    	//establish trx oml and rsl
    	establishScfAllLinks();    	

    }
    
    private void createTwoSectorsAbisIpTrx() {
    	//create one sector
    	createUnlockedMosForOneSector(gsmSectorLdn0, abisIpLdn0, trxLdnSector0, connection_name_0);
    	
    	//create the other sector
    	createUnlockedMosForOneSector(gsmSectorLdn1, abisIpLdn1, trxLdnSector1, connection_name_1);
    	
    	//check abisIp and Trx
    }
    
    private void createUnlockedMosForOneSector(String sectorLdn, String abisIpLdn, String trxLdn, String connection_name) {
    	
    	List<ManagedObject> createMos = new ArrayList<ManagedObject>();
    	
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

    	ManagedObject trxMo =  momHelper.buildTrxMo(trxLdn, MomHelper.UNLOCKED, MomHelper.SECTOR_EQUIP_FUNC_LDN_1);
    	createMos.add(trxMo);
    	RestoreCommand restoreTrxMoCmd = new LockDeleteMoCommand(trxMo.getLocalDistinguishedName());
    	momHelper.addRestoreCmd(restoreTrxMoCmd);
    	
    	momHelper.createSeveralMoInOneTx(createMos);
    	
    	checkTrxMoAttributeAfterCreateUnlockNoLinks(trxLdn);
    	
    }
    
	private void startSoScfAoTfAoAtSendAtConfigAndAtEnable(int tgId) throws InterruptedException {
		abisHelper.sendStartToAllMoVisibleInGsmSector(tgId);
		abisHelper.sendAoAtConfigPreDefBundling(tgId);
		sendAoATEnable(tgId);
	}
	
    
    private void establishScfOmlLink() throws InterruptedException {
    	abisco.establishLinks(tg_0, false, 0);
    	abisco.establishLinks(tg_1, false, 0);
    }
    
    private void establishScfAllLinks() throws InterruptedException {
    	abisco.establishLinks(tg_0, true, 0);
    	abisco.establishLinks(tg_1, true, 0);
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
    
    private void checkGsmSectorMoAttributeAfterCreateNoLinks(String gsmSectorLdn) {
    	assertEquals("GsmSector MO attributes did not reach expected values", "", momHelper.checkGsmSectorMoAttributeAfterCreateNoLinks(gsmSectorLdn, 10));
    }
    private void checkGsmSectorMoAttributeAfterCreateLinkEstablished(String gsmSectorLdn) {
    	assertEquals("GsmSector MO attributes did not reach expected values", "", momHelper.checkGsmSectorMoAttributeAfterCreateLinksEstablished(gsmSectorLdn, 10));
    }
    private void checkTrxMoAttributeAfterCreateUnlockNoLinks(String trxLdn) {
    	assertEquals("Trx MO attributes did not reach expected values", "", momHelper.checkTrxMoAttributeAfterUnlockNoLinks(trxLdn, 30));
    }
    private void checkAbisIpMoAttributeAfterCreateUnlockBscConnected(String abisIpLdn) {
    	assertEquals("AbisIp MO attributes did not reach expected values", "", momHelper.checkAbisIpMoAttributeAfterUnlockBscConnected(abisIpLdn, abisco.getBscIpAddress(), 30));
    }
    private void sendAoATEnable(int tgId) throws InterruptedException  {
    	assertTrue("Configuration signature has not been calculated (value = 0) ", 0 != abisHelper.enableRequest(OM_G31R01.Enums.MOClass.AT, tgId).getConfigurationSignature().intValue());
    }
    private void checkMoStatusAfterUnlockedAbisIpAndTrxAndConnectedTG(String gsmSectorLdn, String abisIpLdn, String trxLdn) {
    	checkGsmSectorMoAttributeAfterCreateNoLinks(gsmSectorLdn);
    	checkAbisIpMoAttributeAfterCreateUnlockBscConnected(abisIpLdn);
    	checkTrxMoAttributeAfterCreateUnlockNoLinks(trxLdn);
    } 
    private void checkMoStatusAfterUnlockedAbisIpAndTrxAndEstablishedScfOml(String gsmSectorLdn, String abisIpLdn, String trxLdn) {
    	checkGsmSectorMoAttributeAfterCreateLinkEstablished(gsmSectorLdn);
    	checkAbisIpMoAttributeAfterCreateUnlockBscConnected(abisIpLdn);
    	checkTrxMoAttributeAfterCreateUnlockNoLinks(trxLdn);
    }
}
