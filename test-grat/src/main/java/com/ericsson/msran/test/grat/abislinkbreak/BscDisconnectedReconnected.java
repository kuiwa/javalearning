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
import com.ericsson.msran.test.grat.testhelpers.TgLdns;
import com.ericsson.msran.jcat.TestBase;

/**
 * @id NodeUC524
 * 
 * @name BscDisconnectedReconnected
 * 
 * @author GRAT 2014
 * 
 * @created 2014-04-01
 * 
 * @description This test class verifies a link break followed by a reconnection between RBS & BSC.
 * 
 * @revision eraweer 2014-04-01 First version.
 *           ewegans 2014-07-01 Updated test for WP2981
 *           ewegans 2014-08-27 Updated test for WP3455
 */

public class BscDisconnectedReconnected extends TestBase {
    private MomHelper momHelper;
    private AbiscoConnection abisco;
    private NodeStatusHelper nodeStatus;
    private AbisHelper abisHelper;
    private static String tnEthPort = MomHelper.ETH_PORT_LDN; //ManagedElement=1,Transport=1,EthernetPort=eth10
    private static String sectorLdn = MomHelper.SECTOR_LDN; // ManagedElement=1,BtsFunction=1,GsmSector=1
    final int WAIT_TIME_SECONDS = 10;
    final int EXPECTED_NR_OF_PACKAGES = 1;
    private AlarmHelper alarmHelper;
//    private static Logger logger = Logger.getLogger(BscDisconnectedReconnected.class);

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
            requirementIds = { "10565-0334/21767[PA1][PREL]", "10565-0334/19034[A][APPR]",
            "10565-0334/19417[A][APPR]" },
            verificationStatement = "Verifies NodeUC524.N, NodeUC524.A1 & NodeUC524.A3",
            testDescription = "Detect that BSC is disconnected and reconnect when possible.",
            traceGuidelines = "N/A")

    /**
     * Precond.
     */
    @Setup
    public void setup() {
    	setTestStepBegin("Setup");
        nodeStatus = new NodeStatusHelper();
        assertTrue("Node did not reach a working state before test start", nodeStatus.waitForNodeReady());
        momHelper = new MomHelper();
        abisco = new AbiscoConnection();
        abisHelper = new AbisHelper();
        alarmHelper = new AlarmHelper();
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
     * @name bscDisconnectedReconnect
     * 
     * @description Verifies NodeUC524.3 "Detect and report that BSC connects the OML or RSL link",
     *                       NodeUC524.N "Detect and report that BSC is disconnected", and
     *                       NodeUC524.A1 "Detect and report that BSC is re-connected"
     *
     * @param testId - unique identifier
     * @param description
     */
    @Test (timeOut = 500000)
    @Parameters({ "testId", "description" })
    public void bscDisconnectedReconnect(String testId, String description)
    {
        setTestCase(testId, description);  
        abisco.setupAbisco(false);
        abisco.disconnectTG();

        // Setup link
        setTestStepBegin("Setup link and MOs");
        
    	if (!momHelper.isBtsFunctionCreated()) {
    		setTestInfo("Precondition: Create BtsFunction MO");
    		momHelper.createBtsFunctionMo(MomHelper.BTS_FUNCTION_LDN);
    	}        

        // Create GsmSector and AbisIp MOs.
        TgLdns moLdns = momHelper.createSectorAbisIpMoAndTrxMo(AbiscoConnection.getConnectionName(), abisco.getBscIpAddress(), false);
        String abisIpLdn = moLdns.abisIpLdn;
        String trxLdn = moLdns.trxLdnList.get(0);

        // Unlock AbisIp and Trx
        momHelper.unlockMo(trxLdn);
        momHelper.unlockMo(abisIpLdn);

        // Connect TG and establish links to the Abisco
        abisco.connectTG();
        try {
            abisco.establishLinks();
        } catch (InterruptedException ie) {
            fail("InteruptedException during establishLinks");
        }
        assertTrue("Could not activate Abis Transport", abisHelper.startSectorMosAndActivateAT());
        try {
            abisco.establishLinks(true);
        } catch (InterruptedException e1) {
            fail("Could not establish links");
        }
        setTestStepEnd();


        // Check pre-conditions
        // AbisIp MO attributes: 
        //     administrativeState = UNLOCKED
        //     operationalState    = ENABLED
        //     availabilityStatus  = <empty>
        // GsmSector MO attribute: 
        //     abisScfOmlState = UP
        // Trx MO attributes:
        //     abisTrxRslState = UP
        //     abisTrxcOmlState = UP
        setTestStepBegin("Check pre-conditions");   
        assertEquals("AbisIp MO attributes did not reach expected values", "", momHelper.checkAbisIpMoAttributeAfterUnlockBscConnected(abisIpLdn, abisco.getBscIpAddress(), 30));
        assertEquals("GsmSector MO attributes did not reach expected values", "", momHelper.checkGsmSectorMoAttributeAfterAllMosStartedAoAtEnabled(sectorLdn, 5));
        assertEquals("Trx MO attributes did not reach expected values", "", momHelper.checkTrxMoAttributeAfterUnlockLinksEstablished(trxLdn, 5));
        setTestStepEnd();
               
        // Now enter the loop where the disconnect/reconnect testing is done
        final int ITERATIONS = 2;
        for (int iteration = 1; iteration <= ITERATIONS; ++iteration)
        {
            setTestInfo("Entering loop iteration " + iteration + " out of " + ITERATIONS);
            
            // Lock TF MO (Ethernet port)
            setTestStepBegin("Lock TF MO");
            momHelper.lockMo(tnEthPort);                
            setTestStepEnd();

            // Verify that the link break has been detected by checking MO status attributes
            // AbisIp MO attributes:
            //     administrativeState = UNLOCKED
            //     operationalState    = DISABLED 
            //     availabilityStatus  = FAILED 
            //     peerIpAddress       = <empty>
            // GsmSector MO attribute:
            //     abisScfOmlState = DOWN
            // Trx MO attributes:
            //     abisTrxRslState = DOWN
            //     abisTrxcOmlState = DOWN
            setTestStepBegin("Verify the link break");
            assertEquals("AbisIp MO attributes did not reach expected values", "", momHelper.checkAbisIpMoAttributeAfterUnlockBscDisconnected(abisIpLdn, 15));
            assertEquals("GsmSector MO attributes did not reach expected values", "", momHelper.checkGsmSectorMoAttributeAfterAllMosStartedAoAtEnabledNoLinks(sectorLdn, 5));
            assertEquals("Trx MO attributes did not reach expected values", "", momHelper.checkTrxMoAttributeAfterUnlockNoLinks(trxLdn, 5));
            setTestStepEnd();
            
            if (iteration == ITERATIONS)
            {	
              // WP3794, Check alarm is raised here
              setTestStepBegin("Check alarm is raised");
              sleepSeconds(65); // Sleep for timeout
              long timeoutInSeconds = 90;
              List<ComAlarm> alarmList = alarmHelper.getAlarmList(timeoutInSeconds);
              boolean isAlarmActive = alarmHelper.isBscDisconnectAlarm(alarmList, abisIpLdn);            
              assertTrue("Alarm is not active", isAlarmActive);
              setTestStepEnd();
            }  
            
            // Unlock TF MO
            setTestStepBegin("Unlock TF MO");
            momHelper.unlockMo(tnEthPort);
            setTestStepEnd();

            // Establish links to the Abisco
            try {
                abisco.establishLinks(true);
            } catch (InterruptedException e1) {
                fail("Could not establish links");
            }


            // Verify that BSC is reconnected by checking MO status attributes
            // AbisIp MO attributes:
            //     administrativeState = UNLOCKED
            //     operationalState    = ENABLED
            //     peerIpAddress       = bscBrokerIpAddress
            //     availabilityStatus  = <empty>
            // GsmSector MO attribute:
            //     abisScfOmlState = UP
            // Trx MO attributes:
            //     abisTrxRslState = UP
            //     abisTrxcOmlState = UP
            setTestStepBegin("Verify that BSC is reconnected");
            assertEquals("AbisIp MO attributes did not reach expected values", "", momHelper.checkAbisIpMoAttributeAfterUnlockBscConnected(abisIpLdn, abisco.getBscIpAddress(), 30));
            assertEquals("GsmSector MO attributes did not reach expected values", "", momHelper.checkGsmSectorMoAttributeAfterAllMosStartedAoAtEnabled(sectorLdn, 5));
            assertEquals("Trx MO attributes did not reach expected values", "", momHelper.checkTrxMoAttributeAfterUnlockLinksEstablished(trxLdn, 5));
            setTestStepEnd();

            if (iteration == ITERATIONS)
            {
              // WP3794, check alarm is ceased
              setTestStepBegin("Check alarm is ceased");
              sleepSeconds(5);
              List<ComAlarm> alarmList = alarmHelper.getAlarmList();
              boolean isAlarmActive = alarmHelper.isBscDisconnectAlarm(alarmList, abisIpLdn);
              assertFalse("Alarm still active", isAlarmActive);
              setTestStepEnd();
            }  
        }
        momHelper.lockMo(trxLdn);
       
        // Cleanup done by RestoreStack
    }
}