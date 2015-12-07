package com.ericsson.msran.test.grat.abislinkbreak;

import java.util.List;

import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import se.ericsson.jcat.fw.annotations.Setup;
import se.ericsson.jcat.fw.annotations.Teardown;

import com.ericsson.commonlibrary.faultmanagement.com.ComAlarm;
import com.ericsson.msran.g2.annotations.TestInfo;
import com.ericsson.msran.test.grat.testhelpers.AbisHelper;
import com.ericsson.msran.test.grat.testhelpers.AbiscoConnection;
import com.ericsson.msran.test.grat.testhelpers.AlarmHelper;
import com.ericsson.msran.test.grat.testhelpers.MomHelper;
import com.ericsson.msran.test.grat.testhelpers.NodeStatusHelper;
import com.ericsson.msran.test.grat.testhelpers.prepost.AbisPrePost;
import com.ericsson.msran.jcat.TestBase;

/**
 * @id NodeUC524.A2
 *  
 * @name BscDisconnectsL2
 * 
 * @author GRAT 2014
 * 
 * @created 2014-04-01
 * 
 * @description Detect and report that BSC disconnects L2. Also verify that L2 can be reconnected.
 * 
 * @revision ewegans 2014-04-01 First version.
 *           ewegans 2014-07-01 Updated test for WP2981
 * 
 */
public class BscDisconnectsL2 extends TestBase {
    private MomHelper momHelper;
    private AbisPrePost abisPrePost;
    private AbiscoConnection abisco;
    private AbisHelper abisHelper;
    private final String GSM_SECTOR_LDN = "ManagedElement=1,BtsFunction=1,GsmSector=1";
    private final String ABISIP_LDN = GSM_SECTOR_LDN + ",AbisIp=1";
    private final String TRX_LDN = GSM_SECTOR_LDN + ",Trx=0";
    private NodeStatusHelper nodeStatus;
    private AlarmHelper alarmHelper;

    /**
     * Description of test case for test reporting
     */
    @TestInfo(
            tcId = "NodeUC524.A2",
            slogan = "Detect and report that BSC disconnects L2",
            requirementDocument = "1/00651-FCP 130 1402",
            requirementRevision = "PC5",
            requirementLinkTested = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff87c1d941?docno=1/00651-FCP1301402Uen&format=excel8book",
            requirementLinkLatest = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff86a2af3d?docno=1/00651-FCP1301402Uen&action=current&format=excel8book",
            requirementIds = { "10565-0334/21767[PA1][PREL]", "10565-0334/19034[A][APPR]",
            "10565-0334/19417[A][APPR]" },
            verificationStatement = "Verifies NodeUC524.A2, NodeUC524.A1, and NodeUC524.A3",
            testDescription = "Verifies disconnecting L2 and reconnection of BSC.",
            traceGuidelines = "N/A")   
  

    /**
     * Precheck.
     * @throws InterruptedException 
     */
    @Setup
    public void setup() throws InterruptedException {
        setTestStepBegin("Setup");
        nodeStatus = new NodeStatusHelper();
        assertTrue("Node did not reach a working state before test start", nodeStatus.waitForNodeReady());
        abisHelper = new AbisHelper();
        momHelper = new MomHelper();
        abisPrePost = new AbisPrePost();
        abisco = new AbiscoConnection();
        abisco.setupAbisco(false);
        setTestInfo("Save current pid to compare after test execution.");
        abisPrePost.preCondScfMoStateStartedAtTfActive();
        alarmHelper = new AlarmHelper();
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
     * @name bscDisconnectsL2Reconnect
     * 
     * @description When links are established, disconnect the TG and check that RBS updates MO values correctly.
     *              Then reconnect again and confirm re-establishment of links.
     * 
     * @param testId - unique identifier of the test case
     * @param description
     *      
     */ 
    @Test(timeOut = 500000)
    @Parameters({ "testId", "description" })
    public void bscDisconnectsL2Reconnect(String testId, String description)
    {
        setTestCase(testId, description);
        final int LOOPS = 3;
        for (int iteration = 1; iteration <= LOOPS; ++iteration)
        {    
            setTestStepBegin("Starting iteration number " + iteration + " out of " + LOOPS);
            if (iteration == LOOPS)
            {
                // The last loop we lock/unlock the Trx twice and confirm that this still works
                setTestInfo("Lock, unlock, lock and unlock the Trx");
                momHelper.lockMo(TRX_LDN);
                momHelper.unlockMo(TRX_LDN);
                momHelper.lockMo(TRX_LDN);
                momHelper.unlockMo(TRX_LDN);
            }
            
            // Pre-check
            setTestInfo("Check pre-condition: AbisIp MO is unlocked, operationalState = ENABLED, availabilityStatus = empty");
            assertEquals("AbisIp MO attributes did not reach expected values", "", momHelper.checkAbisIpMoAttributeAfterUnlockBscConnected(ABISIP_LDN, abisco.getBscIpAddress(), 30));
            setTestStepEnd();

            // Disconnect TG
            setTestStepBegin("Disconnect TG");
            abisco.disconnectTG();

            // Check post-conditions
            setTestInfo("Check post-conditions: The AbisIp MO has attribute administrativeState = UNLOCKED, " + 
                    "operationalState = DISABLED, availabilityStatus = FAILED " +
                    "and peerIpAddress is empty");
            
            assertEquals("AbisIp MO attributes did not reach expected values", "", momHelper.checkAbisIpMoAttributeAfterUnlockBscDisconnected(ABISIP_LDN, 10));
            assertEquals("GsmSector MO attributes did not reach expected values", "", momHelper.checkGsmSectorMoAttributeAfterAllMosStartedAoAtAndAoTfEnabledNoLinks(GSM_SECTOR_LDN, 30));
            assertEquals("Trx MO attributes did not reach expected values", "", momHelper.checkTrxMoAttributeAfterUnlockNoLinks(TRX_LDN, 10));
            setTestStepEnd();

            if (iteration == LOOPS)
            {
              // WP3794, Check alarm is raised here            
              sleepSeconds(65); // Sleep for BSC connection timeout
              setTestStepBegin("Check alarm is raised");
              long timeoutInSeconds = 90;
              List<ComAlarm> alarmList = alarmHelper.getAlarmList(timeoutInSeconds);
              boolean isAlarmActive = alarmHelper.isBscDisconnectAlarm(alarmList, ABISIP_LDN);           
              assertTrue("Alarm is not active", isAlarmActive);
              setTestStepEnd();
            }  

            // Re-establish
            setTestStepBegin("Re-establish connection");
            abisco.connectTG();
            try {
                abisco.establishLinks();
            } catch (InterruptedException e1) {
                fail("InteruptedException during establish SCF OML");
            }
            
            setTestInfo("Send AT Bundling Info Request to give Abisco knowledge of current Bundling setup");
            try {
                abisHelper.atBundlingInfoRequest(1, 0);
            } catch (InterruptedException e) {
                fail("InterruptedException during AT Bundling Info Request");
            }
            
            // Establish TRXC OML+RSL
            try {
                abisco.establishLinks(true);
            } catch (InterruptedException ie) {
                fail("InteruptedException during establishLinks");
            }

            // Check post-conditions
            setTestInfo("Check: The AbisIp MO has attribute administrativeState = UNLOCKED, " +
                    "operationalState = ENABLED, availabilityStatus = empty " +
                    "and peerIpAddress = bscBrokerIpAddress");
            
            assertEquals("AbisIp MO attributes did not reach expected values", "", momHelper.checkAbisIpMoAttributeAfterUnlockBscConnected(ABISIP_LDN, abisco.getBscIpAddress(), 30));
            assertEquals("GsmSector MO attributes did not reach expected values", "", momHelper.checkGsmSectorMoAttributeAfterAllMosStartedAoAtAndAoTfEnabled(GSM_SECTOR_LDN, 10));
            assertEquals("Trx MO attributes did not reach expected values", "", momHelper.checkTrxMoAttributeAfterUnlockLinksEstablished(TRX_LDN, 10));
            setTestStepEnd();
 
            if (iteration == LOOPS)
            {
              // WP3794, check alarm is ceased
              setTestStepBegin("Check alarm is ceased");
              sleepSeconds(5);
              List<ComAlarm> alarmList = alarmHelper.getAlarmList();
              boolean isAlarmActive = alarmHelper.isBscDisconnectAlarm(alarmList, ABISIP_LDN);
              assertFalse("Alarm is not active", isAlarmActive);
              setTestStepEnd();
            }
        }
    }
}
