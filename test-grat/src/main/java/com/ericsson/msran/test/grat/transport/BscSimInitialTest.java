package com.ericsson.msran.test.grat.transport;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.ericsson.msran.g2.annotations.TestInfo;

import se.ericsson.jcat.msrbs.library.netconf.impl.NetconfErrorException;

import com.ericsson.commonlibrary.ecimcom.netconf.NetconfManagedObjectHandler;
import com.ericsson.commonlibrary.managedobjects.ManagedObject;
import com.ericsson.commonlibrary.managedobjects.ManagedObjectAttribute;
import com.ericsson.commonlibrary.managedobjects.ManagedObjectHandler;
import com.ericsson.commonlibrary.managedobjects.exception.ConnectionException;
import com.ericsson.commonlibrary.remotecli.Cli;
import com.ericsson.commonlibrary.remotecli.exceptions.RemoteCliException;
import com.ericsson.msran.test.grat.testhelpers.AbisIpAttribute;
import com.ericsson.msran.test.grat.testhelpers.MomHelper;
import com.ericsson.msran.test.grat.testhelpers.NodeStatusHelper;
import com.ericsson.commonlibrary.resourcemanager.bscSimulator.AbisLower;
import com.ericsson.commonlibrary.resourcemanager.bscSimulator.AbisLowerMode;
import com.ericsson.commonlibrary.resourcemanager.bscSimulator.BscConfiguration;
import com.ericsson.commonlibrary.resourcemanager.bscSimulator.BscSimulatorMode;
import com.ericsson.commonlibrary.resourcemanager.bscSimulator.ConfigTable;
import com.ericsson.commonlibrary.resourcemanager.bscSimulator.Connection;
import com.ericsson.msran.jcat.TestBase;
import com.ericsson.commonlibrary.resourcemanager.gsm.BscSimulator;
import com.ericsson.commonlibrary.resourcemanager.g2.G2Rbs;
import com.ericsson.commonlibrary.resourcemanager.Rm;


/**
 * @todo If these test cases are to be run again, check if usage of the RestoreStack is needed.
 * 
 * @id TC_TransportTest_001
 * @name Testing of el2tp traffic.
 * @author Robert Egelnor (erobege)
 * @created 2013-04-10
 * @description Testing el2tp traffic
 * @revision erobege 2013-04-10 first version
 * @revision erunnil 2015-07-08 Replaced TnConfigurator and minor changes. Renamed TransportTest to BscSimInitialTest.
 */
public class BscSimInitialTest extends TestBase {
    private static final String OML = "oml";

    private static final int SAPI_OML = 62;

    private static final String UNLOCKED = "UNLOCKED";

    private static final String LOCKED = "LOCKED";

    private static final String ADMINISTRATIVE_STATE = "administrativeState";

    private static Logger logger = Logger.getLogger(BscSimInitialTest.class);

    private static final String BTS_FUNCTION_LDN = "ManagedElement=1,BtsFunction=1";
    private static final String SECTOR_LDN = BTS_FUNCTION_LDN + ",GsmSector=1";
    private static final String ABIS_IP_LDN = SECTOR_LDN + ",AbisIp=1";

    private static final String LOG_LEVELS = "check error enter return info "
            + "trace1 trace2 trace3 trace4 trace5 trace6 trace7 trace8 trace9";
    
    private NodeStatusHelper nodeStatus;

    /**
     * RBS to test towards
     */
    private G2Rbs rbs;

    /**
     * BSCSim instance.
     */
    private BscSimulator bscSim;

    /**
     * MoHandler for RBS
     */
    private ManagedObjectHandler moHandler;

    /**
     * Linux Shell on the dus used during execution
     */
    Cli linuxShell;

    /**
     * Regexp used for prompt matching in Linux Shell
     */
    private String defaultLinuxShellRegexPrompt;

    /**
     * Mom Helper instance
     */
    private MomHelper momHelper;
    
    /**
     * @todo If these test cases are to be run in G2 CI again, update the TestInfo!!!
     * 
     * Description of test case for test reporting
     */
    @TestInfo(
            tcId = "NodeUC....",
            slogan = "TBD",
            requirementDocument = "TBD",
            requirementRevision = "TBD",
            requirementLinkTested = "TBD",
            requirementLinkLatest = "TBD",
            requirementIds = { "TBD" },
            verificationStatement = "TBD",
            testDescription = "TBD.",
            traceGuidelines = "N/A")  

    /**
     * Set to test resources. getAvailabilityStatus
     * 
     * @throws RemoteCliException
     * @throws ConnectionException
     */
    @BeforeClass
    public void beforeClass() throws RemoteCliException, ConnectionException {
    	setTestStepBegin("beforeClass");
        setTestInfo("Allocate lab resources and initialize connections");

        setTestInfo("setup");
        nodeStatus = new NodeStatusHelper();
        nodeStatus.waitForSsh();
        nodeStatus.waitForNetconf();
        assertTrue("Node did not reach a working state before test start", nodeStatus.waitForNodeReady());
        momHelper = new MomHelper();
        rbs = Rm.getG2RbsList().get(0);
        moHandler = getMoHandler(rbs);

        linuxShell = rbs.getLinuxShell();
        defaultLinuxShellRegexPrompt = linuxShell.getExpectedRegexPrompt();

        // Timeout to 121 seconds
        linuxShell.setSendTimeoutMillis(121 * 1000);

        showBoardInfo(linuxShell);
        initLogging(linuxShell);

        /**
         * @todo Make sure the Restore Stack is used instead
         */
        //setTestInfo("Clean up any MOs that might be "
        //        + "left from previous execution");
        //momHelper.deleteMos();
        
        setTestStepEnd();
    }

    /**
     * Free test resources.
     */
    @AfterClass
    public void afterClass() {
    	setTestStepBegin("afterClass");
        setTestInfo("Close connections  and free lab resources");
        moHandler = null;
        rbs = null;
        resetLinuShellRegexPrompt();
        setTestStepEnd();
    }

    /**
     * @throws RemoteCliException
     */
    @BeforeMethod
    public void beforeMethod() throws RemoteCliException {
        // Empty
    }

    /**
     * @throws RemoteCliException
     * @throws ConnectionException
     */
    @AfterMethod
    public void afterMethod() throws RemoteCliException, ConnectionException {
        resetLinuShellRegexPrompt();
        /**
         * @todo Make sure the Restore Stack is used instead
         */
        //momHelper.deleteMos();
        stopBscSim();
        getLog(linuxShell);
    }

    /**
     * Helper method for getting the mo handler and configure it with some sane
     * default values
     * 
     * @param rbs
     *            RBS object to get mo Handler from
     * @return NetconfManagedObjectHandler instance for RBS
     */
    private NetconfManagedObjectHandler getMoHandler(G2Rbs rbs) {
        NetconfManagedObjectHandler moHandler = rbs.getManagedObjectHandler();
        moHandler.setDisconnectTimeout(60, TimeUnit.SECONDS);
        moHandler.setOperationTimeout(60, TimeUnit.SECONDS);
        return moHandler;
    }

    private String getBscSimTrafficHost() {
        return Rm.getBscSimulatorList().get(0).getTrafficHost();
    }

    /**
     * Test case that tries to delete a unlocked AbisIP MO. It should not be
     * possible to delete it when it is unlocked (adminstate = UNLOCKED)
     * @param testId 
     * @param description 
     * 
     * @throws Exception
     */
    @Test
    @Parameters({
            "testId", "description"
    })
    public void testDeleteOnUnlockedAbisIpMo(@Optional("") String testId,
            @Optional("") String description) throws Exception {
        setTestCase(testId, description);
        setTestInfo("----- TEST testDeleteOnUnlockedAbisIpMo -----");

        String connectionName = "host_0";

        moHandler.connect();
        momHelper.createGsmSectorMos(1);
        momHelper.createAbisIpMo(connectionName, getBscSimTrafficHost(), false);

        setTestStepBegin("Unlock AbisIp MO");
        momHelper.setAttributeForMoAndCommit(ABIS_IP_LDN, ADMINISTRATIVE_STATE,
                UNLOCKED);
        sleepSeconds(3);

        checkOperationalState(OperationalState.DISABLED);
        checkAvailabilityStatus(AvailabilityStatus.OFF_LINE);

        setTestStepBegin("Try to delete unlocked AbisIp MO");

        setTestInfo("Delete AbisIP MO");

        try {
            momHelper.deleteMo(ABIS_IP_LDN);
            fail("Succeeded to delete unlocked AbisIp MO!");
        } catch (Exception e) {
            NetconfErrorException nee = getNetconfErrorException(e);
            logger.info("Error thrown when deleting locked MO: "
                    + nee.getErrors().get(0).getErrorDescription());
        }

        // AbisMO will be deleted in AfterMethod method
    }
    
    /**
     * Test case that tests the BscSim connection.
     * 
     * 1. Start BSC sim 2. Configure AbisIP MO. 3. See succesful connection with Wireshark
     * @param testId 
     * @param description 
     * 
     * @throws Exception
     */
    @Test
    @Parameters({
            "testId", "description"
    })
    public void bscSimConnectionTest(@Optional("") String testId,
            @Optional("") String description) throws Exception {
        setTestCase(testId, description);
        setTestInfo("----- TEST bscSimConnectionTest -----");

        String connectionName = "host_0";

        startBscSim(connectionName);
        sleepSeconds(3);

        moHandler.connect();
        momHelper.createGsmSectorMos(1);
        momHelper.createAbisIpMo(connectionName, bscSim.getTrafficHost(), false);

        // Wait a while to be sure that attributes is set on MO's
        sleepSeconds(3);
        checkOperationalState(OperationalState.DISABLED);
        checkAvailabilityStatus(AvailabilityStatus.OFF_LINE);

        setTestStepBegin("Unlock AbisIp MO");
        momHelper.setAttributeForMoAndCommit(ABIS_IP_LDN, ADMINISTRATIVE_STATE,
                UNLOCKED);

        // Wait for link to come up
        sleepSeconds(10);

        checkOperationalState(OperationalState.ENABLED);
        checkAvailabilityStatus(AvailabilityStatus.NO_STATUS);

        //Verify connection with Wireshark
        sleepSeconds(10);

        setTestInfo("Stop BSCSim");
        stopBscSim();
        sleepSeconds(10);

        checkOperationalState(OperationalState.DISABLED);
        checkAvailabilityStatus(AvailabilityStatus.FAILED);

        // -------------------- TEARDOWN ----------------------------
        setTestStepBegin("Tear down test");

        setTestInfo("Lock Abis IP");
        momHelper.setAttributeForMoAndCommit(ABIS_IP_LDN, ADMINISTRATIVE_STATE,
                LOCKED);

        // It can take a while for the el2tp to be uninstalled
        // successfully after having transmitted data.
        setTestInfo("Waiting for el2tp to be uninstalled");
        sleepSeconds(10);

        // AbisMO will be deleted in AfterMethod method
    }
    
    

    /**
     * Test case that tests the normal flow for el2tp traffic.
     * 
     * 1. Start BSC sim 2. Configure AbisIP MO. 3. Init lapd_stub 4. Start
     * sending frames. 5 Verify frames sent/received.
     * @param testId 
     * @param description 
     * 
     * @throws Exception
     */
    @Test
    @Parameters({
            "testId", "description"
    })
    public void el2TpTrafficTest(@Optional("") String testId,
            @Optional("") String description) throws Exception {
        setTestCase(testId, description);
        setTestInfo("----- TEST el2TpTrafficTest -----");

        String connectionName = "host_0";

        startBscSim(connectionName);
        sleepSeconds(3);

        moHandler.connect();
        momHelper.createGsmSectorMos(1);
        momHelper.createAbisIpMo(connectionName, bscSim.getTrafficHost(), false);

        // Wait a while to be sure that attributes is set on MO's
        sleepSeconds(3);
        checkOperationalState(OperationalState.DISABLED);
        checkAvailabilityStatus(AvailabilityStatus.OFF_LINE);

        setTestStepBegin("Unlock AbisIp MO");
        momHelper.setAttributeForMoAndCommit(ABIS_IP_LDN, ADMINISTRATIVE_STATE,
                UNLOCKED);

        // Wait for link to come up
        sleepSeconds(10);

        checkOperationalState(OperationalState.ENABLED);
        checkAvailabilityStatus(AvailabilityStatus.NO_STATUS);

        startLapdStub();

        int frameCount = 400;
        int frameSpeed = 100;

        setTestStepBegin("Send LAPD frames");

        startColish();

        boolean successfulDataSend = false;
        String stopCfiResponse = null;

        //
        // Loop for trying to get a successful data send. Sometimes we get into
        // a state where cfi (lapd_stub) reports "Cannot Stop Traffic"
        // so we loop here to avoid this.
        //
        while (!successfulDataSend) {
            setTestInfo("Create lapd_stub instance");
            linuxShell.send("cfi icc 1 create " + connectionName);
            setTestInfo("Start lapd_stub instance");
            linuxShell.send("cfi icc 1 start speed " + frameSpeed
                    + " sapi 0 tei 6 length 46 frames " + frameCount);

            // Wait for frames to be sent
            sleepSeconds(frameCount / frameSpeed + 2);

            setTestInfo("Stop lapd_stub instance");
            stopCfiResponse = linuxShell.send("cfi icc 1 stop");

            if (stopCfiResponse.contains("Cannot Stop Traffic")) {
                logger.warn("Have to do abort on sending frames, since "
                        + "cfi icc stop indcates Cannot Stop Traffic");
                linuxShell.send("cfi icc 1 abort");
                logger.info("Re-trying to send frames...");
            } else {
                successfulDataSend = true;
            }
        }

        exitColish();

        Map<String, String> coliRes = parseColiResponse(stopCfiResponse);

        setTestStepBegin("Check LAPD frame count");
        Assert.assertEquals(
                "Did not receive the same number of frames as we sent!",
                frameCount, Integer.parseInt(coliRes.get("Received frames")));

        setTestInfo("Stop BSCSim");
        stopBscSim();
        sleepSeconds(10);

        checkOperationalState(OperationalState.DISABLED);
        checkAvailabilityStatus(AvailabilityStatus.DEPENDENCY_FAILED);

        // -------------------- TEARDOWN ----------------------------
        setTestStepBegin("Tear down test");
        setTestInfo("Stop LAPD stub");
        linuxShell.send("killall -9 lapd_stub");

        setTestInfo("Lock Abis IP");
        momHelper.setAttributeForMoAndCommit(ABIS_IP_LDN, ADMINISTRATIVE_STATE,
                LOCKED);

        // It can take a while for the el2tp to be uninstalled
        // successfully after having transmitted data.
        setTestInfo("Waiting for el2tp to be uninstalled");
        sleepSeconds(10);

        // AbisMO will be deleted in AfterMethod method
    }

    /**
     * Test case that tests the normal setup and tear down of LAPD link.
     * 
     * 1. Start BSC sim 2. Configure AbisIP MO. 3. Start LAPD link 4. Verify
     * LAPD link is up 5. Stop LAPD link 6. Verify LAPD link is down 7. Repeat
     * start/stop of LAPD link one time
     * @param testId 
     * @param description 
     * 
     * @throws Exception
     */
    @Test
    @Parameters({
            "testId", "description"
    })
    public void lapdStartStopTest(@Optional("") String testId,
            @Optional("") String description) throws Exception {
        setTestCase(testId, description);
        setTestInfo("----- TEST lapdStartStopTest -----");

        String connectionName = "host_0";

        startBscSim(connectionName);
        sleepSeconds(3);

        moHandler.connect();
        momHelper.createGsmSectorMos(1);
        momHelper.createAbisIpMo(connectionName, bscSim.getTrafficHost(), false);

        // Wait a while to be sure that attributes is set on MO's
        sleepSeconds(1);
        checkOperationalState(OperationalState.DISABLED);
        checkAvailabilityStatus(AvailabilityStatus.OFF_LINE);

        setTestStepBegin("Unlock AbisIp MO");
        momHelper.setAttributeForMoAndCommit(ABIS_IP_LDN, ADMINISTRATIVE_STATE,
                UNLOCKED);

        // Wait for link to come up
        sleepSeconds(10);

        checkOperationalState(OperationalState.ENABLED);
        checkAvailabilityStatus(AvailabilityStatus.NO_STATUS);

        // Setup and tear down of LAPD link
        startLapdAndCheckStatus();
        stopLapdAndCheckStatus();

        // Check that setup and tear down of LAPD link can be repeated
        startLapdAndCheckStatus();
        stopLapdAndCheckStatus();

        setTestInfo("Stop BSCSim");
        stopBscSim();
        sleepSeconds(10);

        checkOperationalState(OperationalState.DISABLED);
        checkAvailabilityStatus(AvailabilityStatus.DEPENDENCY_FAILED);

        // -------------------- TEARDOWN ----------------------------
        setTestInfo("Lock Abis IP");
        momHelper.setAttributeForMoAndCommit(ABIS_IP_LDN, ADMINISTRATIVE_STATE,
                LOCKED);

        // It can take a while for the el2tp to be uninstalled
        // successfully after having transmitted data.
        setTestInfo("Waiting for el2tp to be uninstalled");
        sleepSeconds(10);

        // AbisMO will be deleted in AfterClass method
    }

    /**
     * Test that an existing LAPD entity/session recovers and is re-established
     * after link break.
     * 
     * 1. Start BSC sim 2. Configure AbisIP MO. 3. Start LAPD link 4. Verify
     * LAPD link is up 5. Block LAPD link 6. Verify LAPD link is down 5. Unlock
     * LAPD link 6. Verify LAPD link is up 7. Repeat block/unblock of LAPD link
     * one time
     * @param testId 
     * @param description 
     * 
     * @throws Exception
     */
    @Test
    @Parameters({
            "testId", "description"
    })
    public void lapdReestablishTest(@Optional("") String testId,
            @Optional("") String description) throws Exception {
        setTestCase(testId, description);
        setTestInfo("----- TEST lapdReestablishTest -----");

        String connectionName = "host_0";

        startBscSim(connectionName);
        sleepSeconds(3);

        moHandler.connect();
        momHelper.createGsmSectorMos(1);
        momHelper.createAbisIpMo(connectionName, bscSim.getTrafficHost(), false);

        // Wait a while to be sure that attributes is set on MO's
        sleepSeconds(3);
        setTestStepBegin("Check status after AbisIP creation");
        checkOperationalState(OperationalState.DISABLED);
        checkAvailabilityStatus(AvailabilityStatus.OFF_LINE);

        setTestStepBegin("Unlock AbisIp MO");
        momHelper.setAttributeForMoAndCommit(ABIS_IP_LDN, ADMINISTRATIVE_STATE,
                UNLOCKED);

        // Wait for link to come up
        sleepSeconds(10);

        checkOperationalState(OperationalState.ENABLED);
        checkAvailabilityStatus(AvailabilityStatus.NO_STATUS);

        // Setup LAPD link
        startLapdAndCheckStatus();

        // Block all frames to and from LAPD. Check that link goes down.
        blockLapdAndCheckStatus();

        // Enable frames to and from LAPD again. Check that link comes up.
        unblockLapdAndCheckStatus();

        // Repeat blocking/unblocking of LAPD link can be repeated. Check that
        // link goes down and up.
        blockLapdAndCheckStatus();
        unblockLapdAndCheckStatus();

        // Stop LAPD link
        stopLapdAndCheckStatus();

        setTestInfo("Stop BSCSim");
        stopBscSim();
        sleepSeconds(10);

        checkOperationalState(OperationalState.DISABLED);
        checkAvailabilityStatus(AvailabilityStatus.DEPENDENCY_FAILED);

        // -------------------- TEARDOWN ----------------------------
        setTestStepBegin("Tear down");

        setTestInfo("Lock Abis IP");
        momHelper.setAttributeForMoAndCommit(ABIS_IP_LDN, ADMINISTRATIVE_STATE,
                LOCKED);

        // It can take a while for the el2tp to be uninstalled
        // successfully after having transmitted data.
        setTestInfo("Waiting for el2tp to be uninstalled");
        sleepSeconds(10);

        // AbisMO will be deleted in AfterClass method
    }

    /**
     * Start LAPD link and check that status is up
     */
    private void startLapdAndCheckStatus() {
        startLapd(OML, SAPI_OML);
        sleepSeconds(5);
        setTestStepBegin("Check status of LAPD link after start, should report up");
        LapdStatus lapdStatus = getLapdStatus();
        Assert.assertEquals("Lapd status after starting LAPD should report up",
                LapdStatus.UP, lapdStatus);
    }

    /**
     * Stop LAPD link and check that status is down
     */
    private void stopLapdAndCheckStatus() {
        stopLapd();
        sleepSeconds(5);
        setTestStepBegin("Check status of LAPD link after stop, should report down");
        LapdStatus lapdStatus = getLapdStatus();
        Assert.assertEquals(
                "Lapd Status after stopping LAPD should report down",
                LapdStatus.DOWN, lapdStatus);
    }

    /**
     * Block LAPD link and check that status is down
     */
    private void blockLapdAndCheckStatus() {
        setLapdBlockStatus(LapdBlockStatus.BOTH);
        sleepSeconds(20);
        LapdStatus lapdStatus = getLapdStatus();
        setTestStepBegin("Check status of LAPD link after block, should report down");
        Assert.assertEquals(
                "Lapd Status after blocked LAPD should report down",
                LapdStatus.DOWN, lapdStatus);

    }

    /**
     * Unblock LAPD link and check that status is up
     */
    private void unblockLapdAndCheckStatus() {
        setLapdBlockStatus(LapdBlockStatus.NONE);
        sleepSeconds(5);
        LapdStatus lapdStatus = getLapdStatus();
        setTestStepBegin("Check status of LAPD link after unblock, should report up");
        Assert.assertEquals(
                "Lapd Status after unblocked LAPD should report up",
                LapdStatus.UP, lapdStatus);
    }

    private void exitColish() {
        // Exit colish
        resetLinuShellRegexPrompt();
        linuxShell.send("exit");
    }

    private void startColish() {
        // Start coli shell
        linuxShell.setExpectedRegexPrompt("^\\$ $");
        linuxShell.send("colish");
    }

    private void startLapdStub() {
        setTestInfo("Start LAPD stub");
        linuxShell.send("killall -9 lapd_stub");
        linuxShell
                .send("/home/sirpa/software/GRAT_CXP9023458*/grat_deliver/tgt_powerpc/bin/lapd_stub &");
        sleepSeconds(3);
    }

    /**
     * Method for resetting the LinuxShell regex for the prompt.
     */
    private void resetLinuShellRegexPrompt() {
        if (defaultLinuxShellRegexPrompt != null) {
            linuxShell.setExpectedRegexPrompt(defaultLinuxShellRegexPrompt);
        }
    }

    /**
     * Method for getting attribute value as given enumeration from given LDN.
     * 
     * @param enumeration
     *            Enumeration class to use
     * @param ldn
     *            LDN of Mo to get attribute from
     * @param attributeName
     *            Attribute name
     * @param defaultValue
     *            Value to return if no value was set, or the value was not
     *            among the valid enumeration values.
     * @return Enumeration value
     */
    @SuppressWarnings({
            "unchecked", "rawtypes"
    })
    private <T extends Enum> T getMoEnumAttribute(Class<T> enumeration,
            ManagedObject mo, String attributeName, T defaultValue) {
        T result = defaultValue;
        String attributeValue = null;
        ManagedObjectAttribute attribute = mo.getAttribute(attributeName);
        if (null == attribute) {
            logger.debug("No attribute named " + attributeName + " on "
                    + mo.getLocalDistinguishedName());
        } else {
            attributeValue = attribute.getValue();
        }

        if (null != attributeValue) {
            try {
                result = (T) Enum.valueOf(enumeration, attributeValue);
            } catch (IllegalArgumentException e) {
                logger.debug("Value \"" + attributeValue
                        + "\" is not a valid enum value in "
                        + enumeration.getName());
            }
        }

        logger.debug("Reported " + attributeName + " value on "
                + mo.getLocalDistinguishedName() + ": " + result);
        return result;
    }

    private ManagedObject getAbisIpMo() {
        moHandler.connect();
        return moHandler.getManagedObject(ABIS_IP_LDN);
    }

    /**
     * Get operational state from Abis MO.
     * 
     * @param abisMo
     *            AbisIPMo
     * @return OperationalState attribute value
     * @throws ConnectionException
     */
    private OperationalState getOperationalState(ManagedObject abisMo)
            throws ConnectionException {
        return getMoEnumAttribute(OperationalState.class, abisMo,
                AbisIpAttribute.operationalState.name(),
                OperationalState.UNKNOWN);
    }

    /**
     * Get availability status from Abis MO.
     * 
     * @param abisMo
     *            AbisIPMo
     * @return AvailabilityStatus attribute value
     * @throws ConnectionException
     */
    private AvailabilityStatus getAvailabilityStatus(ManagedObject abisMo)
            throws ConnectionException {
        return getMoEnumAttribute(AvailabilityStatus.class, abisMo,
                AbisIpAttribute.availabilityStatus.name(),
                AvailabilityStatus.NO_STATUS);
    }

    private void checkOperationalState(OperationalState expectedState) {
        ManagedObject abisMo = getAbisIpMo();

        setTestInfo("Check operational state, should report "
                + expectedState.name());
        Assert.assertEquals(
                "operationalstate should report" + expectedState.name(),
                expectedState, getOperationalState(abisMo));
    }

    private void checkAvailabilityStatus(AvailabilityStatus expectedStatus) {
        ManagedObject abisMo = getAbisIpMo();

        setTestInfo("Check availability status, should report "
                + expectedStatus.name());
        Assert.assertEquals(
                "availabilitystatus should report" + expectedStatus.name(),
                expectedStatus, getAvailabilityStatus(abisMo));
    }

    /**
     * Method for starting BSC sim
     * 
     * @param connectionName
     *            el2tp connection name
     * @throws RemoteCliException
     *             If communication fails.
     * @throws ConnectionException
     *             If communication fails.
     */
    private void startBscSim(String connectionName) throws RemoteCliException,
            ConnectionException {
        setTestStepBegin("Start BSCSim and configure it");
        bscSim = Rm.getBscSimulatorList().get(0);
        bscSim.connect();

        configureBscSim(bscSim, momHelper.getTnaIpAddress(), connectionName);
        bscSim.start();
    }

    /**
     * Stop the BSC sim instance.
     * 
     * @throws RemoteCliException
     *             If communication fails.
     */
    private void stopBscSim() throws RemoteCliException {
        if (bscSim != null) {
            logger.info("bscsim stop: " + bscSim.stop());
            bscSim.disconnect();
            bscSim = null;
        }
    }

    /**
     * Start LAPD link in bscsim.
     * 
     * @throws RemoteCliException
     *             If communication fails.
     */
    private void startLapd(String linkType, int sapi) throws RemoteCliException {
        if (bscSim != null) {
            setTestInfo("Start LAPD link");
            logger.info("bscSim.send: "
                    + bscSim.send("lapd start " + "\"" + linkType + "\" "
                            + sapi));
        } else {
            logger.info("bscsim not available");
        }
    }

    /**
     * Stop LAPD link in bscsim.
     * 
     * @throws RemoteCliException
     *             If communication fails.
     */
    private void stopLapd() throws RemoteCliException {
        if (bscSim != null) {
            setTestInfo("Stop LAPD link");
            logger.info("bscSim.send: " + bscSim.send("lapd stop"));
        } else {
            logger.info("bscsim not available");
        }
    }

    /**
     * Get status of LAPD link in bscsim.
     * 
     * @throws RemoteCliException
     *             If communication fails.
     */
    private LapdStatus getLapdStatus() throws RemoteCliException {
        String lapdStatusString = null;
        if (bscSim != null) {
            setTestInfo("Check status of LAPD link");
            lapdStatusString = bscSim.send("lapd status");
            logger.info("bscSim.send: " + lapdStatusString);
        } else {
            logger.info("bscsim not available");
        }
        logger.debug("Reported lapd status: " + lapdStatusString);

        if (lapdStatusString == null) {
            return LapdStatus.UNKNOWN;
        }
        // Extract the part of string that is after the equal sign
        lapdStatusString = lapdStatusString.split("=")[1].trim();

        return LapdStatus.getByValue(lapdStatusString);
    }

    /**
     * Set blocking status of LAPD link in bscsim.
     * 
     * Blocking status indicates if bscsim shall block incoming/outgoing LAPD
     * frames.
     * 
     * @throws RemoteCliException
     *             If communication fails.
     */
    private void setLapdBlockStatus(LapdBlockStatus blockStatus) {
        if (bscSim != null) {
            setTestInfo("Block/Unblock LAPD link");
            logger.info("bscSim.send: "
                    + bscSim.send("lapd block " + blockStatus.getValue()));
        } else {
            logger.info("bscsim not available");
        }
    }

    /**
     * Helper method for getting the base NetConf exception from a error stack.
     * 
     * @param e
     *            Top error
     * @return NetConfError exception if found in error stack, or null if not
     *         found.
     */
    private NetconfErrorException getNetconfErrorException(final Throwable e) {
        Throwable cause = e;

        while ((cause != null) && !(cause instanceof NetconfErrorException)) {
            cause = cause.getCause();
        }

        return (NetconfErrorException) cause;
    }

    /**
     * Initialize logging.
     * 
     * @param dusShell
     *            DUS shell to configure in.
     * @throws RemoteCliException
     *             Thrown if communication fails.
     */
    private void initLogging(final Cli dusShell) throws RemoteCliException {
        setTestInfo("Init logging");
        // Enable logs
        dusShell.send(String.format("te enable %s G*", LOG_LEVELS));
        dusShell.send(String.format("te enable %s */G*", LOG_LEVELS));
    }

    /**
     * Method for extracting the log on the node.
     * 
     * @param dusShell
     *            Shell to run te command in.
     * @return The log as text
     * @throws RemoteCliException
     *             If communication fails.
     */
    private String getLog(Cli dusShell) throws RemoteCliException {
        setTestInfo("Get log");
        String log = dusShell.send("te log read");

        // Clear log afterwards
        dusShell.send("te log clear");

        logger.info("Log: " + log);

        // String erlangLog = dusShell.send("cat /rcs/erlang/erlang.log*");
        // logger.info("Erlang Log: " + erlangLog);
        return log;
    }

    /**
     * Trace some board info into the log.
     * 
     * @param dusShell
     *            The shell to execute command in.
     * @throws RemoteCliException
     *             If communication fails.
     */
    private void showBoardInfo(Cli dusShell) throws RemoteCliException {
        setTestInfo("Get board info");
        logger.info("Processes:\n\n" + dusShell.send("ps auxww"));
        logger.info("Installed software:\n\n"
                + dusShell.send("ls -l /home/sirpa/software/"));
    }

    /**
     * Helper method for configuring the BSCSim.
     * 
     * @param bsc
     *            BSCSim instance.
     * @param peerIp
     *            Peer IP (The IP that the RBS will connect from)
     * @param hostName
     *            HostName config.
     * @throws RemoteCliException
     *             Thrown if communication fails.
     */
    private void configureBscSim(final BscSimulator bsc, final String peerIp,
            final String hostName) throws RemoteCliException {

        String localIp = bsc.getTrafficHost();

        final BscConfiguration conf = new BscConfiguration(
                BscSimulatorMode.BSC, localIp);

        conf.addConnection(createConnection0Configuration(localIp, peerIp));
        conf.addConnection(createConnection1Configuration(localIp, peerIp,
                hostName));

        bsc.configureBsc(conf);
        for (int i = 0; i < 3; i++) {
            bsc.configureSession(i, new int[] {}, true);
        }
    }

    /**
     * Create connection 1 configuration.
     * 
     * @param localIp
     *            Local IP (The IP BSCSim is running on)
     * @param peerIp
     *            Peer IP (The IP that the RBS will connect from)
     * @param hostName
     *            HostName config.
     * 
     * @return Connection configuration
     */
    private Connection createConnection1Configuration(final String localIp,
            final String peerIp, final String hostName) {
        final ConfigTable tb1_0 = new ConfigTable(localIp, new int[] {
                0, 10, 11, 12, SAPI_OML
        });

        final AbisLower ab1_0 = new AbisLower(6, 11, 0);

        final Connection conn1 = Connection.newBuilder()
                .abisLowerMode(AbisLowerMode.SUPER_CHANNEL_MODE)
                .numberOfSessions(4).protocolVersion(6).hostname(hostName)
                .peerIpAddress(peerIp).build();

        conn1.addConfigTable(tb1_0).addAbisLower(ab1_0);

        return conn1;
    }

    /**
     * Create connection 0 configuration.
     * 
     * @param localIp
     *            Local IP (The IP BSCSim is running on)
     * @param peerIp
     *            Peer IP (The IP that the RBS will connect from)
     * @param hostName
     *            HostName config.
     * 
     * @return Connection configuration
     */
    private Connection createConnection0Configuration(final String localIp,
            final String peerIp) {
        final ConfigTable tb0_0 = new ConfigTable(localIp, new int[] {
                0, 10, 11, 12
        });

        final ConfigTable tb0_1 = new ConfigTable(localIp, new int[] {
            SAPI_OML
        });

        final AbisLower ab0_0 = new AbisLower(2, 4, 1);
        final AbisLower ab0_1 = new AbisLower(5, 5, 2);

        final Connection conn0 = Connection.newBuilder()
                .abisLowerMode(AbisLowerMode.SINGLE_TIMESLOT_MODE)
                .numberOfSessions(4).protocolVersion(6).hostname(null)
                .peerIpAddress(peerIp).build();

        conn0.addConfigTable(tb0_0).addConfigTable(tb0_1).addAbisLower(ab0_0)
                .addAbisLower(ab0_1);

        return conn0;
    }

    /**
     * Helper method for parsing the output from a coli command into a map.
     * 
     * @param response
     *            Coli command response to parse
     * @return Map with resulting output.
     */
    private Map<String, String> parseColiResponse(final String response)
            throws RemoteCliException {
        logger.info("Parse coli response: " + response);

        Map<String, String> data = new HashMap<String, String>();

        String[] lines = response.split("\n");
        for (String line : lines) {
            String[] keyVal = line.split(":");
            if (keyVal.length == 2) {
                data.put(keyVal[0].trim(), keyVal[1].trim());
            }
        }

        return data;
    }
}
