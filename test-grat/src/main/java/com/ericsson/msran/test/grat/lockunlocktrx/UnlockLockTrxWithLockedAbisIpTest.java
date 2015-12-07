package com.ericsson.msran.test.grat.lockunlocktrx;

import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import se.ericsson.jcat.fw.annotations.Setup;
import se.ericsson.jcat.fw.annotations.Teardown;

import com.ericsson.msran.g2.annotations.TestInfo;
import com.ericsson.msran.jcat.TestBase;
import com.ericsson.msran.test.grat.testhelpers.AbisHelper;
import com.ericsson.msran.test.grat.testhelpers.AbiscoConnection;
import com.ericsson.msran.test.grat.testhelpers.MomHelper;
import com.ericsson.msran.test.grat.testhelpers.NodeStatusHelper;

/**
 * @id NodeUC426.A2,NodeUC428.A2,NodeUC524.A3
 * 
 * @name UnlockLockTrxWithLockedAbisIpTest
 * 
 * @author GRAT 2014
 * 
 * @created 2014-06-16
 * 
 * @description This test will unlock and lock the Trx several times with AbisIp locked.
 *              This will be followed by an unlock of AbisIp and a link establishment. 
 * 
 * @revision eraweer 2014-06-16 first version
 * 
 */

public class UnlockLockTrxWithLockedAbisIpTest extends TestBase {

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
            tcId = "NodeUC426.A2,NodeUC428.A2,NodeUC524.A3",
            slogan = "Unlock and lock the TRX with locked AbisIp",
            requirementDocument = "1/00651-FCP 130 1402",
            requirementRevision = "PC5",
            requirementLinkTested = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff87c1d941?docno=1/00651-FCP1301402Uen&format=excel8book",
            requirementLinkLatest = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff86a2af3d?docno=1/00651-FCP1301402Uen&action=current&format=excel8book",
            requirementIds = { "10565-0334/19417[A][APPR]" },
            verificationStatement = "Verifies NodeUC426.A2, NodeUC428.A2 and NodeUC524.A3",
            testDescription = "Verifies that a Trx MO can be unlocked and locked several times with AbisIp locked, followed by an unlock of AbisIp and a link establishment.",
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
        abisco.setupAbisco(false);
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
     * @name unlockLockTrxWithLockedAbisIp
     * 
     * @description Unlock and lock Trx MO with locked AbisIp
     * 
     * @param testId - unique identifier of the test case
     * @param description
     */
    @Test(timeOut = 800000)
    @Parameters({ "testId", "description" })
    public void unlockLockTrxWithLockedAbisIp(String testId, String description) {       
        setTestCase(testId, description);        
        setTestInfo("Start Test");
        
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
        
        // Loop unlock-lock Trx
        for(int i=0; i < 2; ++i) {
            // UNLOCK TRX
            setTestInfo("Unlock Trx (inside loop).");
            momHelper.unlockMo(trxMoLdn);

            // Verify Unlock (NodeUC426.A2: Unlock TRX with locked AbisIp)
            // The Trx MO must have the following attribute values: 
            // administrativeState = UNLOCKED, operationalState = ENABLED, availabilityStatus = <empty>, abisTrxcState = RESET, abisTrxcOmlState = DOWN and abisTrxRslState = DOWN.
            assertEquals("Trx MO attributes did not reach expected values", "", momHelper.checkTrxMoAttributeAfterUnlockNoLinks(trxMoLdn, 5));

            // LOCK TRX
            setTestInfo("Lock Trx (inside loop).");
            momHelper.lockMo(trxMoLdn);
            
            // Verify Lock (NodeUC428.A2: Lock TRX with not established SCF and TRXC OML)
            // The Trx MO must have the following attribute values:
            // administrativeState = LOCKED, operationalState = DISABLED, availabilityStatus = OFF_LINE and abisTrxcState = <empty>. 
            assertEquals("Trx MO attributes did not reach expected values", "", momHelper.checkTrxMoAttributeAfterLock(trxMoLdn, 5));
        }

        // Unlock TRX
        setTestInfo("Unlock Trx.");
        momHelper.unlockMo(trxMoLdn);
        assertEquals("Trx MO attributes did not reach expected values", "", momHelper.checkTrxMoAttributeAfterUnlockNoLinks(trxMoLdn, 5));

        // Unlock AbisIp
        setTestInfo("Unlock AbisIp.");
        momHelper.unlockMo(abisIpMoLdn);
        assertEquals("AbisIp MO attributes did not reach expected values", "", momHelper.checkAbisIpMoAttributeAfterUnlockBscConnected(abisIpMoLdn, abisco.getBscIpAddress(), 30));

        // No need to connect TG, this is done already during setup phase
        
        // Establish SCF Links
        setTestInfo("Estabish Links");
        try {
            abisco.establishLinks();
        } catch (InterruptedException ie) {
            fail("InteruptedException during establishLinks");
        }

        // Enable AT
        assertTrue("Could not activate AT", abisHelper.startSectorMosAndActivateAT());

        // Establish TRXC Links
        try {
            abisco.establishLinks(true);
        } catch (InterruptedException ie) {
            fail("InteruptedException during establishLinks");
        }


        // Verify link establishment (NodeUC524.A3: Detect and report that BSC connects the OML or RSL link)
        // Check the following MO attribute values: 
        // abisTrxcOmlState = UP, abisTrxRslState = UP and abisScfOmlState = UP.
        
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
        setTestInfo("Check MO attribute values.");
        
        // Sector
        assertEquals("GsmSector MO attributes did not reach expected values", "", momHelper.checkGsmSectorMoAttributeAfterAllMosStartedAoAtEnabled(sectorMoLdn, 5));
        // AbisIp
        assertEquals("AbisIp MO attributes did not reach expected values", "", momHelper.checkAbisIpMoAttributeAfterUnlockBscConnected(abisIpMoLdn, abisco.getBscIpAddress(), 30));
        // Trx
        assertEquals("Trx MO attributes did not reach expected values", "", momHelper.checkTrxMoAttributeAfterUnlockLinksEstablished(trxMoLdn, 5));

        // Test done!
        // Cleanup performed by RestoreStack
        setTestInfo("Test ended");

        // Lock TRX
        setTestInfo("Lock Trx.");
        momHelper.lockMo(trxMoLdn);
        assertEquals("Trx MO attributes did not reach expected values", "", momHelper.checkTrxMoAttributeAfterLock(trxMoLdn, 5));
    }
}
