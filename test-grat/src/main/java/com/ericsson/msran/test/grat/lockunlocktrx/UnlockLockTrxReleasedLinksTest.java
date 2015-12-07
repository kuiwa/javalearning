package com.ericsson.msran.test.grat.lockunlocktrx;

import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import se.ericsson.jcat.fw.annotations.Setup;
import se.ericsson.jcat.fw.annotations.Teardown;

import com.ericsson.msran.g2.annotations.TestInfo;

import com.ericsson.msran.test.grat.testhelpers.AbisHelper;
import com.ericsson.msran.test.grat.testhelpers.AbiscoConnection;
import com.ericsson.msran.test.grat.testhelpers.MomHelper;
import com.ericsson.msran.test.grat.testhelpers.NodeStatusHelper;

import com.ericsson.msran.jcat.TestBase;

/**
 * @id NodeUC426,NodeUC428
 * 
 * @name LockUnlockTrxTest
 * 
 * @author GRAT 2014
 * 
 * @created 2014-06-17
 * 
 * @description This test will create MO GsmSector and Trx (if not already
 *              created). It will unlock AbisIp and establish links, then remove
 *              the links before unlocking the Trx. After doing this several times,
 *              a final unlock with established links will be performed to verify
 *              that it's still possible.
 *              
 * @revision ewegans 2014-06-17 first version
 * @revision ewegans 2014-10-21 updated for WP2982
 * 
 */

public class UnlockLockTrxReleasedLinksTest extends TestBase {
            
    private final String sectorMoLdn = "ManagedElement=1,BtsFunction=1,GsmSector=1";
    private String trxMoLdn;
    private String abisIpLdn;
    
    private MomHelper momHelper;
    private AbiscoConnection abisco;
    private NodeStatusHelper nodeStatus;
    private AbisHelper abisHelper;

    /**
     * Description of test case for test reporting
     */
    @TestInfo(
            tcId = "NodeUC426,NodeUC428",
            slogan = "Unlock TRX - Lock TRX",
            requirementDocument = "1/00651-FCP 130 1402",
            requirementRevision = "PC5",
            requirementLinkTested = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff87c1d941?docno=1/00651-FCP1301402Uen&format=excel8book",
            requirementLinkLatest = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff86a2af3d?docno=1/00651-FCP1301402Uen&action=current&format=excel8book",
            requirementIds = { "10565-0334/19417[A][APPR]" },
            verificationStatement = "Verifies NodeUC426.A3, NodeUC428.A2",
            testDescription = "Verifies that Trx MO can be created and unlocked with correct a state, with Trxc OML and RSL coming up. Also verifies locking",
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
        abisco.setupAbisco(false);
        
    	if (!momHelper.isBtsFunctionCreated()) {
    		setTestInfo("Precondition: Create BtsFunction MO");
    		momHelper.createBtsFunctionMo(MomHelper.BTS_FUNCTION_LDN);
    	}
        
        setTestInfo("Precondition: Create GsmSector MO");
        abisIpLdn = momHelper.createSectorAndAbisIpMo(AbiscoConnection.getConnectionName(), 
				  abisco.getBscIpAddress(),
				  false);
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
     * lockUnlockTrxWithReleasedLinks
     * @description Unlock and lock Trx MO after establishing and releasing links.
     * 				Unlocking Trx should therefore not result in links coming up.
     * 
     * @param testId - unique identifier of the test case
     * @param description
     */
    @Test(timeOut = 500000)
    @Parameters({ "testId", "description" })
    public void lockUnlockTrxWithReleasedLinks(String testId, String description) throws InterruptedException {
        setTestCase(testId, description);
        setTestInfo("Precondition: Create Trx MO");
        trxMoLdn = momHelper.createTrxMo(sectorMoLdn, "0");
        setTestInfo("Unlock abisIp");
        momHelper.unlockMo(abisIpLdn);
        setTestInfo("Precondition: Activate AT");
        abisco.establishLinks();
        assertTrue("Could not start Abis MOs and activate AT", abisHelper.startSectorMosAndActivateAT());
        
        for (int i = 0; i < 3; ++i) {
            setTestStepBegin("Connection TG and request establish");
            abisco.connectTG();
            try {
				abisco.establishLinks();
			} catch (InterruptedException e) {
				fail("Links could not be established");
			}
            setTestInfo("Check: OML Layer 2 link is established for SCF - shown in the GsmSector MO attribute abisScfOmlState = UP.");
            assertEquals("GsmSector MO attributes did not reach expected values", "", momHelper.checkGsmSectorMoAttributeAfterAllMosStartedAoAtEnabled(sectorMoLdn, 10));
            
            if (i == 0) {
                setTestInfo("Release Links");
            	abisco.releaseLinks();
            } else {
                setTestInfo("Disconnect TG");
            	abisco.disconnectTG();
            }
            setTestStepEnd();
        	setTestStepBegin("Check pre-condition: Trx MO exists and has attribute administrativeState = LOCKED.");
        	assertEquals("Trx MO attributes did not reach expected values", "", momHelper.checkTrxMoAttributeAfterLock(trxMoLdn, 10));
                        
        	setTestInfo("Check pre-condition: OML Layer 2 link is not established for SCF - shown in the GsmSector MO attribute abisScfOmlState = DOWN.");
        	assertEquals("GsmSector MO attributes did not reach expected values", "", momHelper.checkGsmSectorMoAttributeAfterAllMosStartedAoAtEnabledNoLinks(sectorMoLdn, 10));

        	if (i == 0) {
            	setTestInfo("Check pre-condition: AbisIp MO exists and has attribute administrativeState = UNLOCKED, operationalState = ENABLED.");
            	assertEquals("AbisIp MO attributes did not reach expected values", "", momHelper.checkAbisIpMoAttributeAfterUnlockBscConnected(abisIpLdn, abisco.getBscIpAddress(), 30));
            }else {
            	setTestInfo("Check pre-condition: AbisIp MO exists and has attribute administrativeState = UNLOCKED, operationalState = DISABLED.");
            	assertEquals("AbisIp MO attributes did not reach expected values", "", momHelper.checkAbisIpMoAttributeAfterUnlockBscDisconnected(abisIpLdn, 30));
            }
            setTestStepEnd();
            
            setTestStepBegin("Unlock Trx");
            momHelper.unlockMo(trxMoLdn);
        	setTestInfo("Check post-conditions: The Trx MO has attribute administrativeState = UNLOCKED, operationalState = ENABLED, availabilityStatus = empty, abisTrxcState = RESET, abisTrxcOmlState = DOWN and abisTrxRslState = DOWN");
        	assertEquals("Trx MO attributes did not reach expected values", "", momHelper.checkTrxMoAttributeAfterUnlockNoLinks(trxMoLdn, 10));
        	setTestStepEnd();
        	
        	setTestStepBegin("Lock Trx");
        	setTestInfo("Check: OML Layer 2 link is not established for SCF - shown in the GsmSector MO attribute abisScfOmlState = DOWN.");
        	assertEquals("GsmSector MO attributes did not reach expected values", "", momHelper.checkGsmSectorMoAttributeAfterAllMosStartedAoAtEnabledNoLinks(sectorMoLdn, 10));
        	momHelper.lockMo(trxMoLdn);
            
        	setTestInfo("Check post-conditions: The Trx MO has attribute administrativeState = LOCKED, operationalState = DISABLED, availabilityStatus = OFF_LINE and abisTrxcState = empty");
        	assertEquals("Trx MO attributes did not reach expected values", "", momHelper.checkTrxMoAttributeAfterLock(trxMoLdn, 10));
        	setTestStepEnd();
        	abisco.disconnectTG();
        }
        
        setTestStepBegin("Finally, unlock Trx and establish links");
        momHelper.unlockMo(trxMoLdn);
        abisco.connectTG();
        
        try {
            abisco.establishLinks();
        } catch (InterruptedException e) {
            fail("Could not establish links");
        }        
        
        // Establish TRXC links
        try {
            abisco.establishLinks(true);
        } catch (InterruptedException e) {
            fail("Could not establish links");
        }
        setTestInfo("Check post-conditions: The Trx MO has attribute administrativeState = UNLOCKED, operationalState = ENABLED, availabilityStatus = empty, abisTrxcState = RESET, abisTrxcOmlState = UP and abisTrxRslState = UP");
        assertEquals("Trx MO attributes did not reach expected values", "", momHelper.checkTrxMoAttributeAfterUnlockLinksEstablished(trxMoLdn, 5));
        setTestStepEnd();

        /*
         * Now we lock the Trx and check attributes
         */
    	setTestStepBegin("Lock TRX");
    	abisHelper.clearStatusUpdate();
    	momHelper.lockMo(trxMoLdn);
    	
        // Get a AT BundlingInfo Update for TG=0
        setTestStepBegin("Wait for an AT Bundling Info Update for TG=0 after locking tei=0 and tei=1, empty bundling is expected");
        assertEquals("Did not receive a valid AT Bundling Info Update", "", abisHelper.waitForAtBundlingInfoUpdateWithEmptyBundlingData(10, 0));
        setTestStepEnd();
        
        // Get a Status Update and check the BTS Capabilities Signature, one for each locked trx
        setTestStepBegin("Wait for a Status Update from SCF");
    	assertEquals("Did not receive a valid SO SCF Status Update", "", abisHelper.waitForSoScfStatusUpdate(10, 0, new StringBuffer()));
        setTestStepEnd();
    	
    	setTestInfo("Check post-conditions: The Trx MO has attribute administrativeState = LOCKED, operationalState = DISABLED, availabilityStatus = OFFLINE and abisTrxcState = empty");
    	assertEquals("Trx MO attributes did not reach expected values", "", momHelper.checkTrxMoAttributeAfterLock(trxMoLdn, 10));
    	setTestStepEnd();

    	setTestInfo("Test is done");
    }
}
