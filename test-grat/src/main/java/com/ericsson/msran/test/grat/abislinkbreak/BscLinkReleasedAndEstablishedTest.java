package com.ericsson.msran.test.grat.abislinkbreak;

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
 * @id NodeUC524
 * 
 * @name BscLinkReleasedAndEstablishedTest
 * 
 * @author GRAT 2014
 * 
 * @created 2014-04-03
 * 
 * @description This test class verifies the scenario when BSC releases the link and then reestablish it again.
 * 
 * @revision eraweer 2014-04-03 First version.     
 *           ewegans 2014-08-27 Updated test for WP3455        
 */

public class BscLinkReleasedAndEstablishedTest extends TestBase {
    private MomHelper momHelper;
    private AbiscoConnection abisco;
    private NodeStatusHelper nodeStatus;
    private AbisHelper abisHelper;
    private static String sectorLdn = "ManagedElement=1,BtsFunction=1,GsmSector=1";
    private static int NUMBER_OF_LOOPS = 2;
    
    /**
     * Description of test case for test reporting
     */
    @TestInfo(
            tcId = "NodeUC524",
            slogan = "Detect, recover from and report GSM Fault",
            requirementDocument = "1/00651-FCP 130 1402",
            requirementRevision = "PC5",
            requirementLinkTested = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff87c1d941?docno=1/00651-FCP1301402Uen&format=excel8book",
            requirementLinkLatest = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff86a2af3d?docno=1/00651-FCP1301402Uen&action=current&format=excel8book",
            requirementIds = { "10565-0334/21767[PA1][PREL]", "10565-0334/19034[A][APPR]", "10565-0334/19417[A][APPR]" },
            verificationStatement = "Verifies NodeUC524.A3, NodeUC524.A4",
            testDescription = "Detect that BSC disconnects and connects.",
            traceGuidelines = "N/A")


    /**
     * Precond.
     */
    @Setup
    public void setup() {
    	setTestStepBegin("Setup");
        nodeStatus = new NodeStatusHelper();
        assertTrue("Node did not reach a working state before test start", nodeStatus.waitForNodeReady());
        abisHelper = new AbisHelper();
        momHelper = new MomHelper();
        abisco = new AbiscoConnection();
        setTestStepEnd();
    }
    
    /**
     * Postcond.
     */
    @Teardown
    public void teardown() {
    	setTestStepBegin("Teardown");
        nodeStatus.isNodeRunning();
        setTestStepEnd();
    }    
              
    /**
     * @name BscLinkReleasedAndEstablishedTest
     * 
     * @description Verifies NodeUC524.A3: "Detect and report that BSC connects the OML or RSL link",
     *                       NodeUC524.A4: "Detect and report that BSC disconnects the OML or RSL link"
     *
     * @param testId - unique identifier
     * @param description
     */
    @Test (timeOut = 540000)
    @Parameters({ "testId", "description" })
    public void bscLinkReleasedAndEstablishedTest(String testId, String description) {
        
        setTestCase(testId, description);           

        setTestStepBegin("Setup link and create MOs");
        abisco.setupAbisco(false);
        
    	if (!momHelper.isBtsFunctionCreated()) {
    		setTestInfo("Precondition: Create BtsFunction MO");
    		momHelper.createBtsFunctionMo(MomHelper.BTS_FUNCTION_LDN);
    	}
        
        String abisIpLdn = momHelper.createSectorAndAbisIpMo(AbiscoConnection.getConnectionName(), abisco.getBscIpAddress(), false);
        String trxLdn = momHelper.createTrxMo(sectorLdn, "0"); 
        
        // Unlock AbisIp and TRX
        setTestInfo("Unlock abisIp and Trx");  
        momHelper.unlockMo(trxLdn);
        momHelper.unlockMo(abisIpLdn);

        // Establish SCF Links
        setTestInfo("Estabish SCF Links");
        try {
            abisco.establishLinks();
        } catch (InterruptedException ie) {
            fail("InteruptedException during establishLinks");
        }

        // Enable AT
        assertTrue("Could not activate AT", abisHelper.startSectorMosAndActivateAT());
        // Establish TRX links 
        setTestInfo("Establish TRX links");
        try {
            abisco.establishLinks(true);
        } catch (InterruptedException ie) {
            fail("InteruptedException during establishLinks");
        }        
        setTestStepEnd();

        // Verify that the links has been established by checking MO attributes:
        setTestStepBegin("Check pre-conditions: GsmSector attribute abisScfOmlState = UP, Trx attributes abisTrxcOmlState = UP and abisTrxRslState = UP");
        assertEquals("AbisIp MO attributes did not reach expected values", "", momHelper.checkAbisIpMoAttributeAfterUnlockBscConnected(abisIpLdn, abisco.getBscIpAddress(), 30));
        assertEquals("GsmSector MO attributes did not reach expected values", "", momHelper.checkGsmSectorMoAttributeAfterAllMosStartedAoAtEnabled(sectorLdn, 5));
        assertEquals("Trx MO attributes did not reach expected values", "", momHelper.checkTrxMoAttributeAfterUnlockLinksEstablished(trxLdn, 5));
        setTestStepEnd();

        for(int i=0; i < NUMBER_OF_LOOPS; ++i) {
            // Release Link  
            setTestStepBegin("Release Links");           
            abisco.releaseLinks(true);
            setTestStepEnd();

            // Verify that the links has been released by checking MO attributes:
            setTestStepBegin("Verify that the links have been released by checking MO attributes. abisScfOmlState = DOWN, abisTrxcOmlState = DOWN and abisTrxRslState = DOWN");
            assertEquals("AbisIp MO attributes did not reach expected values", "", momHelper.checkAbisIpMoAttributeAfterUnlockBscConnected(abisIpLdn, abisco.getBscIpAddress(), 30));
            assertEquals("GsmSector MO attributes did not reach expected values", "", momHelper.checkGsmSectorMoAttributeAfterAllMosStartedAoAtEnabledNoLinks(sectorLdn, 5));
            assertEquals("Trx MO attributes did not reach expected values", "", momHelper.checkTrxMoAttributeAfterUnlockNoLinks(trxLdn, 5));
            setTestStepEnd();

            // Establish links to the Abisco
            setTestInfo("Establish links");
            try {
                abisco.establishLinks(true);
            } catch (InterruptedException ie) {
                fail("InteruptedException during establishLinks");
            }

            // Verify that the links has been established by checking MO attributes:
            setTestStepBegin("Verify that the links have been established. abisScfOmlState = UP, abisTrxcOmlState = UP and abisTrxRslState = UP");    
            assertEquals("AbisIp MO attributes did not reach expected values", "", momHelper.checkAbisIpMoAttributeAfterUnlockBscConnected(abisIpLdn, abisco.getBscIpAddress(), 30));
            assertEquals("GsmSector MO attributes did not reach expected values", "", momHelper.checkGsmSectorMoAttributeAfterAllMosStartedAoAtEnabled(sectorLdn, 5));
            assertEquals("Trx MO attributes did not reach expected values", "", momHelper.checkTrxMoAttributeAfterUnlockLinksEstablished(trxLdn, 5));            
            setTestStepEnd();
        }       

        // Cleanup done by RestoreStack
    }
}