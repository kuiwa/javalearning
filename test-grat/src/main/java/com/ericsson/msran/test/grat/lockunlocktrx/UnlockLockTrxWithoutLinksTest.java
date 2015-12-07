package com.ericsson.msran.test.grat.lockunlocktrx;

import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import se.ericsson.jcat.fw.annotations.Setup;
import se.ericsson.jcat.fw.annotations.Teardown;

import com.ericsson.abisco.clientlib.servers.OM_G31R01;
import com.ericsson.msran.g2.annotations.TestInfo;
import com.ericsson.msran.jcat.TestBase;
import com.ericsson.msran.test.grat.testhelpers.AbisHelper;
import com.ericsson.msran.test.grat.testhelpers.AbiscoConnection;
import com.ericsson.msran.test.grat.testhelpers.MomHelper;
import com.ericsson.msran.test.grat.testhelpers.NodeStatusHelper;

/**
 * @id NodeUC426.A4,NodeUC428.A2
 * 
 * @name UnlockLockTrxWithoutLinksTest
 * 
 * @author GRAT 2014
 * 
 * @created 2014-06-12
 * 
 * @description This test will unlock and lock the Trx several times with just TG connected.
 *              This will be followed by a full link establishment to verify that everything still works. 
 * 
 * @revision eraweer 2014-06-12 first version
 * 
 */

public class UnlockLockTrxWithoutLinksTest extends TestBase {

    private MomHelper momHelper;
    private String sectorMoLdn;
    private String trxMoLdn;
    private String abisIpMoLdn;
    private AbiscoConnection abisco;
    private NodeStatusHelper nodeStatus;  
    private AbisHelper abisHelper;
    
    /**
     * Description of test case for test reporting
     */
    @TestInfo(
            tcId = "NodeUC426.A4,NodeUC428.A2",
            slogan = "Lock and unlock TRX without links",
            requirementDocument = "1/00651-FCP 130 1402",
            requirementRevision = "PC5",
            requirementLinkTested = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff87c1d941?docno=1/00651-FCP1301402Uen&format=excel8book",
            requirementLinkLatest = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff86a2af3d?docno=1/00651-FCP1301402Uen&action=current&format=excel8book",
            requirementIds = { "10565-0334/19417[A][APPR]" },
            verificationStatement = "Verifies NodeUC426.A4 and NodeUC428.A2",
            testDescription = "Verifies that a Trx MO can be unlocked and locked several times, followed by link establishment",
            traceGuidelines = "N/A")


    /**
     * Setup
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
     * teardown
     */    
    @Teardown
    public void teardown() {
    	setTestStepBegin("Teardown");
        nodeStatus.isNodeRunning();
        setTestStepEnd();
    }
    
    
    /**
     * @name unlockLockTrxWithoutLinks
     * 
     * @description Unlock and lock Trx MO without links
     * 
     * @param testId - unique identifier of the test case
     * @param description
     * @throws InterruptedException 
     */
    @Test(timeOut = 800000)
    @Parameters({ "testId", "description" })
    public void unlockLockTrxWithoutLinks(String testId, String description) throws InterruptedException {
        setTestCase(testId, description);        
        setTestInfo("Start Test");
        abisco.disconnectTG(); // If previous test left a TG
        
        // Create MOs
    	if (!momHelper.isBtsFunctionCreated()) {
            setTestInfo("Precondition: Create BtsFunction MO");
    		momHelper.createBtsFunctionMo(MomHelper.BTS_FUNCTION_LDN);
    	}        
        
        setTestInfo("Precondition: Create GsmSector MO");
        sectorMoLdn = momHelper.createGsmSectorMos(1).get(0);
        
        setTestInfo("Precondition: Create AbisIp MO");
        abisIpMoLdn = momHelper.createTnAndAbisIpMo(AbiscoConnection.getConnectionName(), abisco.getBscIpAddress(), false);

        setTestInfo("Precondition: Create Trx MO");
        trxMoLdn = momHelper.createTrxMo(sectorMoLdn, "0");    
        
        // Unlock AbisIp
        setTestInfo("Precondition: Unlock AbisIp MO");
        momHelper.unlockMo(abisIpMoLdn);

        // Loop unlock-lock Trx
        for(int i=0; i < 2; ++i) {
            setTestInfo("Check preconditions");
            if (i == 0) { // First iteration, no BSC connection
                assertEquals("AbisIp MO attributes did not reach expected values", "", momHelper.checkAbisIpMoAttributeAfterUnlockBscDisconnected(abisIpMoLdn, 60));
                assertEquals("Trx MO attributes did not reach expected values", "", momHelper.checkGsmSectorMoAttributeAfterCreateNoLinks(sectorMoLdn, 5));
            } else { // Second iteration, yes BSC and SCF OML
                assertEquals("AbisIp MO attributes did not reach expected values", "", momHelper.checkAbisIpMoAttributeAfterUnlockBscConnected(abisIpMoLdn, abisco.getBscIpAddress(), 30));
                assertEquals("Trx MO attributes did not reach expected values", "", momHelper.checkGsmSectorMoAttributeAfterAllMosStarted(sectorMoLdn, 5));                
            }
            
            // UNLOCK TRX
            setTestInfo("Unlock Trx.");
            abisHelper.clearStatusUpdate();
            momHelper.unlockMo(trxMoLdn);
            if (i == 1) {
                // Get a Status Update and check the BTS Capabilities Signature
                setTestStepBegin("Wait for a Status Update from SCF");
                assertEquals("Did not receive a valid SO SCF Status Update", "", abisHelper.waitForSoScfStatusUpdate(10, 0, new StringBuffer()));
                setTestStepEnd();
            }
            
            // Verify Unlock (NodeUC426.A4: Unlock TRX with no active AT configuration) 
            // The Trx MO must have the following attribute values: 
            // administrativeState = UNLOCKED, operationalState = ENABLED, availabilityStatus = <empty>, abisTrxcState = RESET, abisTrxcOmlState = DOWN and abisTrxRslState = DOWN.
            assertEquals("Trx MO attributes did not reach expected values", "", momHelper.checkTrxMoAttributeAfterUnlockNoLinks(trxMoLdn, 5));
            
            // LOCK TRX
            setTestInfo("Lock Trx.");
            momHelper.lockMo(trxMoLdn);
            if (i == 1) {
                // Get a Status Update and check the BTS Capabilities Signature
                setTestStepBegin("Wait for a Status Update from SCF");
                assertEquals("Did not receive a valid SO SCF Status Update", "", abisHelper.waitForSoScfStatusUpdate(10, 0, new StringBuffer()));
                setTestStepEnd();
            }
            
            // Verify Lock (NodeUC428.A2: Lock TRX with not established SCF and TRXC OML on first iteration)
            // The Trx MO must have the following attribute values:
            // administrativeState = LOCKED, operationalState = DISABLED, availabilityStatus = OFF_LINE and abisTrxcState = <empty>. 
            assertEquals("Trx MO attributes did not reach expected values", "", momHelper.checkTrxMoAttributeAfterLock(trxMoLdn, 5));
            
            if (i == 0) {
                setTestInfo("Connect BSC and start sector");
                abisco.setupAbisco(false);
                abisco.establishLinks();
                abisHelper.sendStartToAllMoVisibleInGsmSector(0);
            }
        }

        // Enable AT
        abisHelper.sendAoAtConfigPreDefBundling(0);
        assertTrue(abisHelper.enableRequest(OM_G31R01.Enums.MOClass.AT, 0).getConfigurationSignature() != 0);

        // Unlock Trx
        setTestInfo("Unlock Trx, after loop.");
        momHelper.unlockMo(trxMoLdn);
        assertEquals("Trx MO attributes did not reach expected values", "", momHelper.checkTrxMoAttributeAfterUnlockNoLinks(trxMoLdn, 5));
        
        // Get a Status Update and check the BTS Capabilities Signature
        setTestStepBegin("Wait for a Status Update from SCF");
        assertEquals("Did not receive a valid SO SCF Status Update", "", abisHelper.waitForSoScfStatusUpdate(10, 0, new StringBuffer()));
        setTestStepEnd();
        
        // Get a AT BundlingInfo Update
        setTestStepBegin("Wait for an AT Bundling Info Update");
        assertEquals("Did not receive a valid AT Bundling Info Update", "", abisHelper.waitForAtBundlingInfoUpdateWithBundlingData(10, 0));
        setTestStepEnd();
        
        // Establish Links
        setTestInfo("Estabish Trx Links");
        abisco.establishLinks(true);
       

        // Verify test by checking MO status attributes:        
        // GsmSector MO
        //     abisScfOmlState     = UP
        // AbisIp MO
        //     administrativeState = UNLOCKED
        //     operationalState    = ENABLED
        //     availabilityStatus  = <empty>
        // Trx MO
        //     administrativeState = UNLOCKED
        //     operationalState    = ENABLED
        //     abisTrxRslState     = UP
        //     abisTrxcOmlState    = UP
        //     availabilityStatus  = <empty>
        //     abisTrxcState       = RESET
        setTestInfo("Check MO attribute values." );
        
        // Sector
        assertEquals("GsmSector MO attributes did not reach expected values", "", momHelper.checkGsmSectorMoAttributeAfterAllMosStartedAoAtEnabled(sectorMoLdn, 5));
        // AbisIp
        assertEquals("AbisIp MO attributes did not reach expected values", "", momHelper.checkAbisIpMoAttributeAfterUnlockBscConnected(abisIpMoLdn, abisco.getBscIpAddress(), 30));
        // Trx        
        assertEquals("Trx MO attributes did not reach expected values", "", momHelper.checkTrxMoAttributeAfterUnlockLinksEstablished(trxMoLdn, 5));

        // Test done!
        // Cleanup performed by RestoreStack
        setTestInfo("Test ended");
    }

}
