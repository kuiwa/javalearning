package com.ericsson.msran.test.grat.lockunlocktrx;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import se.ericsson.jcat.fw.annotations.Setup;
import se.ericsson.jcat.fw.annotations.Teardown;

import com.ericsson.abisco.clientlib.AbiscoClient;
import com.ericsson.abisco.clientlib.servers.CHEXTRAS;
import com.ericsson.abisco.clientlib.servers.CMDHLIB;
import com.ericsson.abisco.clientlib.servers.OM_G31R01;
import com.ericsson.abisco.clientlib.servers.BG.Enums.OperationalCondition;
import com.ericsson.abisco.clientlib.servers.CMDHLIB.CompleteStartRejectException;
import com.ericsson.commonlibrary.managedobjects.ManagedObject;
import com.ericsson.commonlibrary.resourcemanager.Rm;
import com.ericsson.commonlibrary.restorestack.RestoreCommand;
import com.ericsson.commonlibrary.restorestack.RestoreCommandStack;
import com.ericsson.msran.g2.annotations.TestInfo;
import com.ericsson.msran.helpers.Helpers;

import com.ericsson.msran.test.grat.testhelpers.AbisHelper;
import com.ericsson.msran.test.grat.testhelpers.AbiscoConnection;
import com.ericsson.msran.test.grat.testhelpers.MomHelper;
import com.ericsson.msran.test.grat.testhelpers.NodeStatusHelper;
import com.ericsson.msran.test.grat.testhelpers.TgLdns;
import com.ericsson.msran.test.grat.testhelpers.prepost.AbisPrePost;
import com.ericsson.msran.test.grat.testhelpers.restorecommands.AbiscoCloseConnection;
import com.ericsson.msran.test.grat.testhelpers.restorecommands.AbiscoReleaseLinks;
import com.ericsson.msran.test.grat.testhelpers.restorecommands.DeleteMoCommand;
import com.ericsson.msran.test.grat.testhelpers.restorecommands.LockDeleteMoCommand;

import com.ericsson.msran.jcat.TestBase;

/**
 * @id NodeUC426,NodeUC428
 * @name LockUnlockTrxTest
 * @author GRAT 2014
 * @created 2014-06-11
 * @description This test will create MO GsmSector and Trx.
 *              Then it will ask Abisco to try to establish links and unlock
 *              the Trx. The test will then verify that Trx OML and RSL links
 *              are established and that the Trx MO attributes are updated
 *              correctly. This is followed by a lock and verification that
 *              it went well.
 * @revision ewegans 2014-06-11 first version
 *           ewegans 2014-08-27 Updated for WP3455
 *           ecoujin 2015-06-26 Updated for WP4252
 */

public class UnlockLockTrxEstablishedLinksTest extends TestBase {

    private final String sectorMoLdn = MomHelper.SECTOR_LDN; // ManagedElement=1,BtsFunction=1,GsmSector=1
    private String trxMoLdn;
    private String abisIpLdn;

    private MomHelper momHelper;
    private AbiscoConnection abisco;
    private AbisHelper abisHelper;
    private NodeStatusHelper nodeStatus;
    private RestoreCommandStack restoreStack;

    private AbisPrePost abisPrePost;

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
            verificationStatement = "Verifies NodeUC426.N, NodeUC428.N, NodeUC428.A2",
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
        abisPrePost = new AbisPrePost();
        setTestInfo("Save current pid to compare after test execution.");
        restoreStack = Helpers.restore().restoreStackHelper().getRestoreStack();
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
     * Send the startSoScf to Abisco. Needed for precondition in test.
     */
    private void startSoScf() {
        ArrayList<String> expectedOmlIwdVersions = new ArrayList<String>(
                Arrays.asList(AbiscoConnection.OML_IWD_VERSION));
        ArrayList<String> expectedRslIwdVersions = new ArrayList<String>(Arrays.asList(
                AbiscoConnection.RSL_IWD_VERSION_1, AbiscoConnection.RSL_IWD_VERSION_2));

        abisHelper.clearNegotiationRecord1Data();
        try {
            abisHelper.startRequest(OM_G31R01.Enums.MOClass.SCF, 0);
        } catch (InterruptedException e) {
            fail("SCF start request was interrupted");
        }

        List<Integer> negotiationRecord1Data = abisHelper.getNegotiationRecord1Data();

        assertTrue(abisHelper.compareNegotiationRecord1Data(negotiationRecord1Data, expectedOmlIwdVersions,
                expectedRslIwdVersions));
    }

    /**
     * A modified version of setupAbisco used in abisHelper. This version creates a TG
     * with three Trx's, so that we can test that a trxId > 0 works.
     */
    private void setupAbiscoSpecial() {
        AbiscoClient abiscoClient = Rm.getAbiscoList().get(0).getAbiscoClient();
        restoreStack.add(new AbiscoCloseConnection(abiscoClient));
        try {
            // Disconnect and Delete TG
            abisco.disconnectAndDeleteTG(0);

            // Tss should already be up, or we're ****ed

            // create a tg with three trx:es
            abisco.createTgPreDefBundling(0, "host_0", 3, false);

            // define a cell with three trx:es
            abisco.defineCell(0, 3);

            // Get running version of the Abisco (for debug purpose)
            CHEXTRAS.GetVersionInformation getVersionInfo = abiscoClient.getCHEXTRAS()
                    .createGetVersionInformation();
            CHEXTRAS.GetVersionInformationResponse response = getVersionInfo.send();
            setTestInfo(response.getCH_VersionNumber());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Modified version of establishLinksAsync found in abisHelper.
     * This goes together with setupAbiscoSpecial, and establishes links
     * for Trx=2.
     */
    private void establishLinksAsyncSpecial() {
        AbiscoClient abiscoClient = Rm.getAbiscoList().get(0).getAbiscoClient();
        List<Integer> trxcList = Arrays.asList(2);
        try {
            CMDHLIB.EstablishLinks estLinks = abiscoClient.getCMDHLIB().createEstablishLinks();
            estLinks.setTRXList(trxcList);
            estLinks.setRSL(CMDHLIB.Enums.RSL.YES);
            estLinks.setOML(CMDHLIB.Enums.OML.YES);
            estLinks.sendAsync();
            Thread.sleep(1000);
        } catch (CMDHLIB.EstablishLinksRejectException e) {
            setTestInfo("Links already established");
        } catch (CHEXTRAS.CHRejectException e) {
            setTestInfo("Cannot establish links");
        } catch (InterruptedException ie) {
            setTestInfo("Got InterruptedException sleep.");
        }
        //Put release links on the stack
        restoreStack.add(new AbiscoReleaseLinks(abiscoClient, trxcList));
    }

    private void createUnlockMosForOneSector(
            String sectorLdn,
            String abisIpLdn,
            String trx0ldn,
            String trx1ldn,
            String connection_name) {
        // specify which MOs that shall be created together, and their parameter values
        List<ManagedObject> createMos = new ArrayList<ManagedObject>();

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

        ManagedObject trxMo = momHelper.buildTrxMo(trx0ldn, MomHelper.UNLOCKED,
                momHelper.getSectorEquipmentFunctionLdn());
        createMos.add(trxMo);
        RestoreCommand restoreTrxMoCmd = new LockDeleteMoCommand(trxMo.getLocalDistinguishedName());
        momHelper.addRestoreCmd(restoreTrxMoCmd);

        ManagedObject trxMo1 = momHelper.buildTrxMo(trx1ldn, MomHelper.UNLOCKED,
                momHelper.getSectorEquipmentFunctionLdn());
        createMos.add(trxMo1);
        RestoreCommand restoreTrxMoCmd1 = new LockDeleteMoCommand(trxMo1.getLocalDistinguishedName());
        momHelper.addRestoreCmd(restoreTrxMoCmd1);

        setTestInfo("Create all MOs");
        momHelper.createSeveralMoInOneTx(createMos);

        assertEquals("Trx MO attributes did not reach expected values", "",
                momHelper.checkTrxMoAttributeAfterUnlockNoLinks(trx1ldn, 30));
    }

    /**
     * lockUnlockTrxWithLinks
     * 
     * @description Unlock and lock Trx MO with Abisco told to establish links, so
     *              Trx RSL and OML should come up when Trx is unlocked. The the
     *              test locks the Trx again.
     *              This is repeated multiple times with different names for
     *              AbisIp and Trx MOs, operations are also done in mixed order.
     * @param testId - unique identifier of the test case
     * @param description
     */
    @Test(timeOut = 850000)
    @Parameters({ "testId", "description" })
    public void lockUnlockTrxWithLinks(String testId, String description) throws InterruptedException {
        setTestCase(testId, description);
        final String trxLdns[] = { "0", "Kalle", "2" };
        final String abisIpLdns[] = { "1", "0", "1" };
        final int ITERATIONS = 3;
        Integer expectedTrxIndex;
        // we use tg=0 as default
        int tg_0 = 0;
        
        abisco.setupAbisco(false);
        
    	if (!momHelper.isBtsFunctionCreated()) {
    		setTestInfo("Precondition: Create BtsFunction MO");
    		momHelper.createBtsFunctionMo(MomHelper.BTS_FUNCTION_LDN);
    	}

        for (int iteration = 0; iteration < ITERATIONS; ++iteration) {
            setTestStepBegin("Main loop iteration " + (iteration + 1) +
                    ", Trx=" + trxLdns[iteration] + " AbisIp=" + abisIpLdns[iteration]);

            setTestInfo("Setup Abisco");
            if (iteration == ITERATIONS - 1) {
                // Since TrxId != 0, we need to define more Trxes in the TG
                setupAbiscoSpecial();
            }

            setTestInfo("Precondition for Trx creation: Create AbisIp MO");

            abisIpLdn = momHelper.createSectorAndAbisIpMo(AbiscoConnection.getConnectionName(),
                    abisco.getBscIpAddress(),
                    sectorMoLdn + ",AbisIp=" + abisIpLdns[iteration],
                    false);

            setTestInfo("AbisIp LDN = " + abisIpLdn);

            if (iteration == 0) {
                setTestInfo("Precondition: Create Trx MO");
                trxMoLdn = momHelper.createTrxMo(sectorMoLdn, trxLdns[iteration]);
            }
            setTestInfo("Unlock abisIp, ldn = " + abisIpLdn);
            momHelper.unlockMo(abisIpLdn);
            if (iteration == 1) {
                setTestInfo("Precondition: Create Trx MO");
                trxMoLdn = momHelper.createTrxMo(sectorMoLdn, trxLdns[iteration]);
            }
            setTestInfo("Connection TG and establish OML and RSL links");
            abisco.connectTG();
            if (iteration == 2) {
                establishLinksAsyncSpecial();
            } else {
                abisco.establishLinksAsync(true);
            }
            assertEquals("SCF OML could not be established", "",
                    momHelper.checkGsmSectorMoAttributeAfterCreateLinksEstablished(sectorMoLdn, 25));

            startSoScf();
            abisHelper.sendStartToAllMoVisibleInGsmSector(0);
            abisHelper.sendAoAtConfigPreDefBundling(0);
            // send AT ENABLE
            assertTrue("Configuration signature has not been calculated (value = 0) ",
                    0 != abisHelper.enableRequest(OM_G31R01.Enums.MOClass.AT, 0).getConfigurationSignature().intValue());

            StringBuffer btsCapSig_createTrx = new StringBuffer();

            if (iteration == 2) {
                setTestInfo("Precondition: Create Trx MO");
                abisHelper.clearStatusUpdate();
                trxMoLdn = momHelper.createTrxMo(sectorMoLdn, trxLdns[iteration]);
                // since so scf is started we should get a status update when we create the trx 
                setTestStepBegin("Wait for a Status Update from SCF");
                assertEquals("Did not receive a valid SO SCF Status Update", "",
                        abisHelper.waitForSoScfStatusUpdate(10, tg_0, btsCapSig_createTrx));
                setTestStepEnd();
            }

            if (iteration == 1) {
                // for Trx=Kalle
                expectedTrxIndex = 0;
            } else {
                expectedTrxIndex = iteration;
            }

            /*
             * Check pre-conditions
             */
            setTestStepBegin("Check pre-condition: Trx MO exists and has attribute administrativeState = LOCKED.");
            assertEquals("Trx MO attributes did not reach expected values", "",
                    momHelper.checkTrxMoAttributeAfterLock(trxMoLdn, 5, Integer.toString(expectedTrxIndex)));
            setTestStepBegin("Check pre-condition: OML Layer 2 link has been established for SCF and SCF is started - shown in the GsmSector MO attribute abisScfOmlState = UP, abisScfState = STARTED, and abisAtState = ENABLED.");
            assertEquals("GsmSector MO attributes did not reach expected values", "",
                    momHelper.checkGsmSectorMoAttributeAfterAllMosStartedAoAtEnabled(sectorMoLdn, 5));
            setTestStepEnd();

            // We do these parts three times so that the first GTE is used twice
            final int RUNS = 3;
            for (int i = 1; i <= RUNS; ++i) {
                setTestStepBegin("Iteration " + i + ": unlock Trx, then lock it.");

                /*
                 * First we unlock the Trx and check all attributes of interest
                 */
                abisHelper.clearStatusUpdate();
                momHelper.unlockMo(trxMoLdn);
                StringBuffer btsCapSig_unlockTrx = new StringBuffer();
                StringBuffer btsCapSig_lockTrx = new StringBuffer();

                // Get a Status Update and check the BTS Capabilities Signature
                setTestStepBegin("Wait for a Status Update from SCF");
                assertEquals("Did not receive a valid SO SCF Status Update", "",
                        abisHelper.waitForSoScfStatusUpdate(10, tg_0, new StringBuffer()));
                assertTrue("The bts capabilities signature did not change between create and unlock trx: "
                        + btsCapSig_createTrx + " " + btsCapSig_unlockTrx,
                        btsCapSig_unlockTrx.equals(btsCapSig_createTrx) == false);
                setTestStepEnd();

                // Get a AT BundlingInfo Update
                setTestStepBegin("Wait for an AT Bundling Info Update");
                assertEquals("Did not receive a valid AT Bundling Info Update", "",
                        abisHelper.waitForAtBundlingInfoUpdateWithPredefinedBundlingData(10, tg_0,
                                new ArrayList<Integer>(Arrays.asList(expectedTrxIndex))));
                setTestStepEnd();

                setTestInfo("Check post-conditions: The Trx MO has attribute administrativeState = UNLOCKED, operationalState = ENABLED, availabilityStatus = empty, abisTrxcState = RESET, abisTrxcOmlState = UP, and abisTrxRslState = UP");
                assertEquals(
                        "Trx MO attributes did not reach expected values",
                        "",
                        momHelper.checkTrxMoAttributeAfterUnlockLinksEstablished(trxMoLdn, 10,
                                Integer.toString(expectedTrxIndex)));
                setTestStepEnd();

                /*
                 * Now we lock the Trx and check attributes
                 */

                abisHelper.clearStatusUpdate();
                setTestStepBegin("Lock TRX");
                momHelper.lockMo(trxMoLdn);

                // Get a AT BundlingInfo Update
                setTestStepBegin("Wait for an AT Bundling Info Update");
                assertEquals("Did not receive a valid AT Bundling Info Update", "",
                        abisHelper.waitForAtBundlingInfoUpdateWithEmptyBundlingData(10, tg_0));
                setTestStepEnd();

                // Get a Status Update and check the BTS Capabilities Signature
                setTestStepBegin("Wait for a Status Update from SCF");
                assertEquals("Did not receive a valid SO SCF Status Update", "",
                        abisHelper.waitForSoScfStatusUpdate(10, tg_0, new StringBuffer()));
                assertTrue("The bts capabilities signature did not change between unlock and lock trx",
                        btsCapSig_lockTrx.equals(btsCapSig_unlockTrx) == false);
                setTestStepEnd();

                setTestInfo("Check post-conditions: The Trx MO has attribute administrativeState = LOCKED, operationalState = DISABLED, availabilityStatus = OFF_LINE, abisTrxcState = RESET, abisTrxcOmlState = DOWN and abisTrxRslState = DOWN");
                assertEquals("Trx MO attributes did not reach expected values", "",
                        momHelper.checkTrxMoAttributeAfterLock(trxMoLdn, 5, Integer.toString(expectedTrxIndex)));
                setTestStepEnd();

                abisco.releaseLinks(true);
                if (i != RUNS) { // Don't prepare for next iteration if it's the last
                    if (iteration == 2) {
                        establishLinksAsyncSpecial();
                    } else {
                        abisco.establishLinksAsync(true);
                    }
                }
            }
            // Cleanup before next iteration
            setTestInfo("Main loop iteration " + iteration + ", done, remove Trx, AbisIp and sector");
            abisHelper.clearStatusUpdate();
            momHelper.deleteMo(trxMoLdn);

            momHelper.lockMo(abisIpLdn);
            momHelper.deleteMo(abisIpLdn);
            momHelper.deleteMo(sectorMoLdn);
        }
        setTestInfo("Test is done!");
    }

    /**
     * lockUnlockTrxWithLinks2
     * 
     * @description Unlock and lock Trx MO with Abisco told to establish links, so
     *              Trx RSL and OML should come up when Trx is unlocked. Then release
     *              the links before locking Trx again. This is repeated multiple times.
     *              Verification according to NodeUC426.N and NodeUC428.A2.
     * @param testId - unique identifier of the test case
     * @param description
     */
    @Test(timeOut = 750000)
    @Parameters({ "testId", "description" })
    public void lockUnlockTrxWithLinks2(String testId, String description) throws InterruptedException {
        setTestCase(testId, description);

        abisco.setupAbisco(false);
        
    	if (!momHelper.isBtsFunctionCreated()) {
    		setTestInfo("Precondition: Create BtsFunction MO");
    		momHelper.createBtsFunctionMo(MomHelper.BTS_FUNCTION_LDN);
    	}

        setTestInfo("Pre-conditions: Create GsmSector, AbisIp and Trx-Mo");
        String abisIpLdn = sectorMoLdn + ",AbisIp=1";
        abisIpLdn = momHelper.createSectorAndAbisIpMo(AbiscoConnection.getConnectionName(), abisco.getBscIpAddress(),
                false);
        trxMoLdn = momHelper.createTrxMo(sectorMoLdn, "0");

        // we use tg=0 as default
        int tg_0 = 0;

        // Unlock AbisIp
        setTestInfo("Unlock AbisIp");
        momHelper.unlockMo(abisIpLdn);

        final int ITERATIONS = 3;
        for (int iteration = 0; iteration < ITERATIONS; ++iteration) {
            setTestStepBegin("Main loop iteration " + (iteration + 1));

            // Setup Abisco 
            abisco.setupAbisco(false);

            // Establish Oml link for Scf
            setTestInfo("Establish Oml link for Scf");
            try {
                abisco.establishLinks();
            } catch (InterruptedException ie) {
                fail("InteruptedException during establishLinks");
            }

            if (iteration == 0) {
                // only get AT into enable at first iteration, for further iterations the AT should be considered as already enabled before
                startSoScf();
                abisHelper.sendStartToAllMoVisibleInGsmSector(0);
                abisHelper.sendAoAtConfigPreDefBundling(0);
                // send AT ENABLE
                assertTrue("Configuration signature has not been calculated (value = 0) ", 0 != abisHelper
                        .enableRequest(OM_G31R01.Enums.MOClass.AT, 0).getConfigurationSignature().intValue());
            }

            // Check pre-conditions
            setTestStepBegin("Check pre-condition: Trx MO exists and has attribute administrativeState = LOCKED.");
            assertEquals("Trx MO attributes did not reach expected values", "",
                    momHelper.checkTrxMoAttributeAfterLock(trxMoLdn, 5));
            setTestStepBegin("Check pre-condition: OML Layer 2 link has been established for SCF and SCF is started - shown in the GsmSector MO attribute abisScfOmlState = UP and abisScfState = STARTED.");
            assertEquals("GsmSector MO attributes did not reach expected values", "",
                    momHelper.checkGsmSectorMoAttributeAfterAllMosStartedAoAtEnabled(sectorMoLdn, 5));
            setTestStepEnd();

            setTestStepBegin("Unlock Trx");
            abisHelper.clearStatusUpdate();
            // Unlock TRX
            momHelper.unlockMo(trxMoLdn);

            // Get a Status Update and check the BTS Capabilities Signature
            setTestStepBegin("Wait for a Status Update from SCF");
            assertEquals("Did not receive a valid SO SCF Status Update", "",
                    abisHelper.waitForSoScfStatusUpdate(10, tg_0, new StringBuffer()));
            setTestStepEnd();

            // Get a AT BundlingInfo Update
            setTestStepBegin("Wait for an AT Bundling Info Update");
            assertEquals("Did not receive a valid AT Bundling Info Update", "",
                    abisHelper.waitForAtBundlingInfoUpdateWithPredefinedBundlingData(10, tg_0, new ArrayList<Integer>(
                            Arrays.asList(0))));
            setTestStepEnd();

            // Establish links for Trxc
            setTestInfo("Establish Oml&Rsl links for Trxc");
            try {
                abisco.establishLinks(true);
            } catch (InterruptedException ie) {
                fail("InteruptedException during establishLinks");
            }

            // Check post-conditions
            setTestInfo("Check post-conditions: The Trx MO has attribute administrativeState = UNLOCKED, operationalState = ENABLED, availabilityStatus = empty, abisTrxcState = RESET, abisTrxcOmlState = UP, and abisTrxRslState = UP");
            assertEquals("Trx MO attributes did not reach expected values", "",
                    momHelper.checkTrxMoAttributeAfterUnlockLinksEstablished(trxMoLdn, 5));
            setTestStepEnd();

            // Release Links
            abisco.releaseLinks(true);

            // Check pre-conditions
            setTestInfo("Check pre-conditions: The Trx MO has attribute administrativeState = UNLOCKED, abisTrxcOmlState = DOWN and abisTrxRslState = DOWN");
            assertEquals("Trx MO attributes did not reach expected values", "",
                    momHelper.checkTrxMoAttributeAfterUnlockNoLinks(trxMoLdn, 5));
            setTestInfo("Check pre-conditions: The GsmSector MO has attribute abisScfOmlState = DOWN and abisScfState = STARTED");
            assertEquals("GsmSector MO attributes did not reach expected values", "",
                    momHelper.checkGsmSectorMoAttributeAfterAllMosStartedAoAtEnabledNoLinks(sectorMoLdn, 5));

            // Lock TRX
            setTestStepBegin("Lock TRX");
            momHelper.lockMo(trxMoLdn);

            // since we have released the links, we will not get any So Scf Status update or Ao AT Bundling Info

            // Check post-conditions
            setTestInfo("Check post-conditions: The Trx MO has attribute administrativeState = LOCKED, operationalState = DISABLED, availabilityStatus = OFFLINE and abisTrxcState = <empty>");
            assertEquals("Trx MO attributes did not reach expected values", "",
                    momHelper.checkAbisIpMoAttributeAfterLock(trxMoLdn, 5));
        }

        // Cleanup 
        setTestInfo("Cleanup delete MOs");
        momHelper.deleteMo(trxMoLdn);
        momHelper.lockMo(abisIpLdn);
        momHelper.deleteMo(abisIpLdn);
        momHelper.deleteMo(sectorMoLdn);

        setTestInfo("Test is done!");
    }

    /**
     * @name unlockTrxAndLockUnlockSectorEquipmentFunc1
     * @description Unlock Trx, Lock SectorEquipmentFunction, Lock and Unlock Trx, Unlock
     *              SectorEquipmentFunction
     * @param testId - unique identifier of the test case
     * @param description
     * @throws InterruptedException
     */

    @Test(timeOut = 500000)
    @Parameters({ "testId", "description" })
    public void unlockTrxAndLockUnlockSectorEquipmentFunc1(String testId, String description)
            throws InterruptedException {

        setTestCase(testId, description);

        abisco.setupAbisco(false);
        
    	if (!momHelper.isBtsFunctionCreated()) {
    		setTestInfo("Precondition: Create BtsFunction MO");
    		momHelper.createBtsFunctionMo(MomHelper.BTS_FUNCTION_LDN);
    	}

        setTestInfo("Precondition: Create GsmSector MO, AbisIp MO and Trx MO");
        String abisIpLdn = sectorMoLdn + ",AbisIp=1";
        abisIpLdn = momHelper.createSectorAndAbisIpMo(AbiscoConnection.getConnectionName(), abisco.getBscIpAddress(),
                false);
        trxMoLdn = momHelper.createTrxMo(sectorMoLdn, "0");

        setTestInfo("Unlock AbisIp");
        momHelper.unlockMo(abisIpLdn);

        setTestInfo("Connection TG and establish SCF OML link");
        abisco.connectTG();

        try {
            abisco.establishLinks();
        } catch (InterruptedException ie) {
            fail("InteruptedException during establishLinks");
        }

        startSoScf();
        abisHelper.sendStartToAllMoVisibleInGsmSector(0);
        abisHelper.sendAoAtConfigPreDefBundling(0);
        assertTrue("Configuration signature has not been calculated (value = 0) ",
                0 != abisHelper.enableRequest(OM_G31R01.Enums.MOClass.AT, 0).getConfigurationSignature().intValue());

        // Unlock Trx
        setTestStepBegin("Test Unlock Trx MO: " + trxMoLdn);
        momHelper.setAttributeForMoAndCommit(trxMoLdn, "administrativeState", "UNLOCKED");

        setTestInfo("Establish Oml&Rsl links for Trxc");
        try {
            abisco.establishLinks(true);
        } catch (InterruptedException ie) {
            fail("InteruptedException during establishLinks");
        }
        assertEquals("Trx MO attributes did not reach expected values after unlock", "",
                momHelper.checkTrxMoAttributeAfterUnlockLinksEstablished(trxMoLdn, 5));
        setTestStepEnd();

        // Lock SectorEquipmentFunction
        setTestStepBegin("Test Lock SectorEquipmentFunction MO: " + momHelper.getSectorEquipmentFunctionLdn());
        momHelper
                .setAttributeForMoAndCommit(momHelper.getSectorEquipmentFunctionLdn(), "administrativeState", "LOCKED");
        assertEquals("Trx MO attributes did not reach expected values after unlock", "",
                momHelper.checkTrxMoAttributeWithLockedSectorEquipmentFunc(trxMoLdn, 10));
        setTestStepEnd();

        // Lock Trx with locked SectorEquipmentFunction
        setTestStepBegin("Test Lock Trx MO: " + trxMoLdn);
        momHelper.setAttributeForMoAndCommit(trxMoLdn, "administrativeState", "LOCKED");
        assertEquals("Trx MO attributes did not reach expected values after lock", "",
                momHelper.checkTrxMoAttributeAfterLock(trxMoLdn, 5));
        setTestStepEnd();

        // Unlock Trx with locked SectorEquipmentFunction
        setTestStepBegin("Test Unlock Trx MO: " + trxMoLdn);
        momHelper.setAttributeForMoAndCommit(trxMoLdn, "administrativeState", "UNLOCKED");

        setTestInfo("Establish Oml&Rsl links for Trxc");
        try {
            abisco.establishLinks(true);
        } catch (InterruptedException ie) {
            fail("InteruptedException during establishLinks");
        }
        assertEquals("Trx MO attributes did not reach expected values after unlock", "",
                momHelper.checkTrxMoAttributeWithLockedSectorEquipmentFunc(trxMoLdn, 5));
        setTestStepEnd();

        // Unlock SectorEquipmentFunction
        setTestStepBegin("Test Unlock SectorEquipmentFunction MO: " + momHelper.getSectorEquipmentFunctionLdn());
        momHelper.setAttributeForMoAndCommit(momHelper.getSectorEquipmentFunctionLdn(), "administrativeState",
                "UNLOCKED");
        saveAssertEquals("Trx MO attributes did not reach expected values after unlock", "",
                momHelper.checkTrxMoAttributeAfterUnlockLinksEstablished(trxMoLdn, 5));
        setTestStepEnd();

        // Cleanup 
        setTestInfo("Cleanup delete MOs");
        momHelper.lockMo(trxMoLdn);
        momHelper.deleteMo(trxMoLdn);
        momHelper.lockMo(abisIpLdn);
        momHelper.deleteMo(abisIpLdn);
        momHelper.deleteMo(sectorMoLdn);

        setTestInfo("Test is done!");

    }

    /**
     * @name unlockTrxAndLockUnlockSectorEquipmentFunc2
     * @description Unlock Trx and Lock-Unlock SectorEquipmentFunction
     * @param testId - unique identifier of the test case
     * @param description
     * @throws InterruptedException
     */

    @Test(timeOut = 500000)
    @Parameters({ "testId", "description" })
    public void unlockTrxAndLockUnlockSectorEquipmentFunc2(String testId, String description)
            throws InterruptedException {

        setTestCase(testId, description);

        abisco.setupAbisco(false);
        
    	if (!momHelper.isBtsFunctionCreated()) {
    		setTestInfo("Precondition: Create BtsFunction MO");
    		momHelper.createBtsFunctionMo(MomHelper.BTS_FUNCTION_LDN);
    	}

        setTestInfo("Precondition: Create GsmSector MO, AbisIp MO and Trx MO");
        String abisIpLdn = sectorMoLdn + ",AbisIp=1";
        abisIpLdn = momHelper.createSectorAndAbisIpMo(AbiscoConnection.getConnectionName(), abisco.getBscIpAddress(),
                false);
        trxMoLdn = momHelper.createTrxMo(sectorMoLdn, "0");

        setTestInfo("Unlock AbisIp");
        momHelper.unlockMo(abisIpLdn);

        setTestInfo("Connection TG and establish SCF OML link");
        abisco.connectTG();

        try {
            abisco.establishLinks();
        } catch (InterruptedException ie) {
            fail("InteruptedException during establishLinks");
        }

        startSoScf();
        abisHelper.sendStartToAllMoVisibleInGsmSector(0);
        abisHelper.sendAoAtConfigPreDefBundling(0);
        assertTrue("Configuration signature has not been calculated (value = 0) ",
                0 != abisHelper.enableRequest(OM_G31R01.Enums.MOClass.AT, 0).getConfigurationSignature().intValue());

        // Unlock Trx
        setTestStepBegin("Test Unlock Trx MO: " + trxMoLdn);
        momHelper.setAttributeForMoAndCommit(trxMoLdn, "administrativeState", "UNLOCKED");
        assertEquals("Trx MO attributes did not reach expected values after unlock", "",
                momHelper.checkTrxMoAttributeAfterUnlockNoLinks(trxMoLdn, 5));
        setTestStepEnd();

        setTestInfo("Establish Oml&Rsl links for Trxc");
        try {
            abisco.establishLinks(true);
        } catch (InterruptedException ie) {
            fail("InteruptedException during establishLinks");
        }

        // Lock SectorEquipmentFunction
        setTestStepBegin("Test Lock SectorEquipmentFunction MO: " + momHelper.getSectorEquipmentFunctionLdn());
        momHelper
                .setAttributeForMoAndCommit(momHelper.getSectorEquipmentFunctionLdn(), "administrativeState", "LOCKED");
        assertEquals("Trx MO attributes did not reach expected values after unlock", "",
                momHelper.checkTrxMoAttributeWithLockedSectorEquipmentFunc(trxMoLdn, 10));
        setTestStepEnd();

        // Unlock SectorEquipmentFunction
        setTestStepBegin("Test Unlock SectorEquipmentFunction MO: " + momHelper.getSectorEquipmentFunctionLdn());
        momHelper.setAttributeForMoAndCommit(momHelper.getSectorEquipmentFunctionLdn(), "administrativeState",
                "UNLOCKED");
        saveAssertEquals("Trx MO attributes did not reach expected values after unlock", "",
                momHelper.checkTrxMoAttributeAfterUnlockLinksEstablished(trxMoLdn, 5));
        setTestStepEnd();

        // Cleanup 
        setTestInfo("Cleanup delete MOs");
        momHelper.lockMo(trxMoLdn);
        momHelper.deleteMo(trxMoLdn);
        momHelper.lockMo(abisIpLdn);
        momHelper.deleteMo(abisIpLdn);
        momHelper.deleteMo(sectorMoLdn);

        setTestInfo("Test is done!");

    }

    /**
     * @name unlockTrxAndLockUnlockSectorEquipmentFunc3
     * @description Unlock two Trxes and Lock-Unlock SectorEquipmentFunction
     * @param testId - unique identifier of the test case
     * @param description
     * @throws InterruptedException
     */

    @Test(timeOut = 850000)
    @Parameters({ "testId", "description" })
    public void unlockTrxAndLockUnlockSectorEquipmentFunc3(String testId, String description)
            throws InterruptedException {

        setTestCase(testId, description);

        int trxId_0 = 0;
        int trxId_1 = 1;
        String GsmSectorLdn_1 = "ManagedElement=1,BtsFunction=1,GsmSector=0";
        String TrxLdn_0_in_GsmSector_1 = GsmSectorLdn_1 + ",Trx=" + trxId_0;
        String TrxLdn_1_in_GsmSector_1 = GsmSectorLdn_1 + ",Trx=" + trxId_1;
        int tg_0 = 0;


        final int numOfTGs = 1;
        final int numOfTrxesPerTG = 2;
        
        // Test variables
        List<TgLdns> tgLdnsList = momHelper.createUnlockAllGratMos(numOfTGs, numOfTrxesPerTG);

        setTestStepBegin("Setup Abisco for " + numOfTGs + " TGs");
        abisco.setupAbisco(numOfTGs, numOfTrxesPerTG, false);
        setTestStepEnd();

        tgLdnsList = momHelper.findGratMos();

        int current_tg=0;
        setTestInfo("Current TG = " + current_tg);
        TgLdns tg = tgLdnsList.get(current_tg);

        setTestStepBegin("Verify AbisIp MO states after unlock");
        assertEquals(tg.abisIpLdn + " did not reach correct state", "",
                        momHelper.checkAbisIpMoAttributeAfterUnlockBscConnected(tg.abisIpLdn, abisco.getBscIpAddress(),
                                30));
        setTestStepEnd();

        for (String trxLdn : tg.trxLdnList) {
                    setTestStepBegin("Verify Trx MO states after unlock for " + trxLdn);
                    assertEquals(trxLdn + " did not reach correct state", "",
                            momHelper.checkTrxMoAttributeAfterUnlockNoLinks(trxLdn, 20));
                    setTestStepEnd();
                }

        // This is where it happens
        setTestStepBegin("Enable all Abis MOs in TG " + current_tg);
        try {
                    abisHelper.completeStartup(current_tg, numOfTrxesPerTG);
        } catch (CompleteStartRejectException e) {
                    fail("Complete startup of TG = " + current_tg + " caused CompleteStartRejectException: "
                            + e.getMessage());
        }

        setTestStepEnd();

        setTestStepBegin("Verify AbisIp MO after complete startup " + tg.abisIpLdn);
        assertEquals(tg.abisIpLdn + " did not reach correct state", "",
                        momHelper.checkAbisIpMoAttributeAfterUnlockBscConnected(tg.abisIpLdn, abisco.getBscIpAddress(),
                                10));
        setTestStepEnd();

        setTestStepBegin("Verify GsmSector MO after complete startup " + tg.sectorLdn);
        assertEquals(tg.sectorLdn + " did not reach correct state", "",
                        momHelper.checkGsmSectorMoAttributeAfterAllMosStartedAoAtAndAoTfEnabled(tg.sectorLdn, 10));
        setTestStepEnd();

        for (String trxLdn : tg.trxLdnList) {
             setTestStepBegin("Verify Trx MO states after complete start up " + trxLdn);
             assertEquals(trxLdn + " did not reach correct state", "",
                            momHelper.checkTrxMoAttributeAfterUnlockAllEnabledLinksEstablished(trxLdn, 10));
             setTestStepEnd();
        }

        sleepSeconds(10);
      
        // Check Status Update result
        setTestStepBegin("********* Check Status Update result before lock SectorEquipmentFunction");

        OM_G31R01.StatusResponse trxcStatusRsp_0 = abisHelper.statusRequest(OM_G31R01.Enums.MOClass.TRXC, tg_0,
                trxId_0, 0, 255);
        saveAssertEquals("Operational Condition should be Operational for TRXC id-0",
                OperationalCondition.Operational.getValue(), trxcStatusRsp_0.getStatusChoice().getStatusTRXC()
                        .getOperationalCondition().getOperationalCondition().getValue());
        OM_G31R01.StatusResponse trxcStatusRsp_1 = abisHelper.statusRequest(OM_G31R01.Enums.MOClass.TRXC, tg_0,
                trxId_1, 1, 255);
        saveAssertEquals("Operational Condition should be Operational for TRXC id-1",
                OperationalCondition.Operational.getValue(), trxcStatusRsp_1.getStatusChoice().getStatusTRXC()
                        .getOperationalCondition().getOperationalCondition().getValue());

        OM_G31R01.StatusResponse scfStatusRsp_nr0 = abisHelper.statusRequest(OM_G31R01.Enums.MOClass.SCF, tg_0,
                trxId_0, 0, 255);
        saveAssertEquals("Operational Condition should be Operational for SCF",
                OperationalCondition.Operational.getValue(), scfStatusRsp_nr0.getStatusChoice().getStatusSCF()
                        .getOperationalCondition().getOperationalCondition().getValue());
        OM_G31R01.CapabilitiesExchangeResult scfCapExchangeResult = abisHelper.scfCapabilitiesExchangeRequest(tg_0,
                0xABCD);
        saveAssertEquals("Trx-id0 should be available from SCF Capability Exchange Result", 1, scfCapExchangeResult
                .getCapabilitiesData().get(34).intValue());
        saveAssertEquals("Trx-id1 should be available from SCF Capability Exchange Result", 1, scfCapExchangeResult
                .getCapabilitiesData().get(46).intValue());
        setTestStepEnd();

        
        // Lock SectorEquipmentFunction
        setTestStepBegin("********* Lock SectorEquipmentFunction");
        momHelper
                .setAttributeForMoAndCommit(momHelper.getSectorEquipmentFunctionLdn(), "administrativeState", "LOCKED");
        saveAssertEquals("Trx-0 MO attributes did not reach expected values after lock SectorEquipmentFunction", "",
                momHelper.checkTrxMoAttributeAfterLockSectorEquipFuncWithAllEnabledLinksEstablished(
                        TrxLdn_0_in_GsmSector_1, 5));
        saveAssertEquals("Trx-1 MO attributes did not reach expected values after lock SectorEquipmentFunction", "",
                momHelper.checkTrxMoAttributeAfterLockSectorEquipFuncWithAllEnabledLinksEstablished(
                        TrxLdn_1_in_GsmSector_1, 5));
        setTestStepEnd();


        trxcStatusRsp_0 = abisHelper.statusRequest(OM_G31R01.Enums.MOClass.TRXC, tg_0, trxId_0, 0, 255);
        saveAssertEquals("Operational Condition should be Not_Operational for TRXC id-0",
                OperationalCondition.NotOperational.getValue(), trxcStatusRsp_0.getStatusChoice().getStatusTRXC()
                        .getOperationalCondition().getOperationalCondition().getValue());
        trxcStatusRsp_1 = abisHelper.statusRequest(OM_G31R01.Enums.MOClass.TRXC, tg_0, trxId_1, 1, 255);
        saveAssertEquals("Operational Condition should be Not_Operational for TRXC id-1",
                OperationalCondition.NotOperational.getValue(), trxcStatusRsp_1.getStatusChoice().getStatusTRXC()
                        .getOperationalCondition().getOperationalCondition().getValue());
        scfStatusRsp_nr0 = abisHelper.statusRequest(OM_G31R01.Enums.MOClass.SCF, tg_0, trxId_0, 0, 255);
        saveAssertEquals("Operational Condition should be Degraded for SCF", OperationalCondition.Degraded.getValue(),
                scfStatusRsp_nr0.getStatusChoice().getStatusSCF().getOperationalCondition().getOperationalCondition()
                        .getValue());
        scfCapExchangeResult = abisHelper.scfCapabilitiesExchangeRequest(tg_0, 0xABCD);
        saveAssertEquals("Trx-id0 should be available from SCF Capability Exchange Result", 1, scfCapExchangeResult
                .getCapabilitiesData().get(34).intValue());
        saveAssertEquals("Trx-id1 should be available from SCF Capability Exchange Result", 1, scfCapExchangeResult
                .getCapabilitiesData().get(46).intValue());
        setTestStepEnd();

   
        
        setTestStepBegin("********* unlock SectorEquipmentFunction");
        momHelper
        .setAttributeForMoAndCommit(momHelper.getSectorEquipmentFunctionLdn(), "administrativeState", "UNLOCKED");
		setTestStepEnd();
        setTestStepEnd();
        
        
        setTestStepBegin("Verify AbisIp MO after complete startup " + tg.abisIpLdn);
        assertEquals(tg.abisIpLdn + " did not reach correct state", "",
                momHelper.checkAbisIpMoAttributeAfterUnlockBscConnected(tg.abisIpLdn, abisco.getBscIpAddress(),
                        10));
        setTestStepEnd();

        setTestStepBegin("Verify GsmSector MO after complete startup " + tg.sectorLdn);
        assertEquals(tg.sectorLdn + " did not reach correct state", "",
                momHelper.checkGsmSectorMoAttributeAfterAllMosStartedAoAtAndAoTfEnabled(tg.sectorLdn, 10));
        setTestStepEnd();

        for (String trxLdn : tg.trxLdnList) {
            setTestStepBegin("Verify Trx MO states after complete start up " + trxLdn);
            assertEquals(trxLdn + " did not reach correct state", "",
                    momHelper.checkTrxMoAttributeAfterUnlockAllEnabledLinksEstablished(trxLdn, 10));
            setTestStepEnd();
        }

        sleepSeconds(5);
      
        
        setTestStepBegin("Lock AbisIp MO and all " + numOfTrxesPerTG + " Trx MOs in TG " + current_tg);
        for (String trxLdn : tg.trxLdnList) {
            momHelper.lockMo(trxLdn);
            assertEquals(trxLdn + " did not reach correct state", "",
                        momHelper.checkTrxMoAttributeAfterLock(trxLdn, 20));
        }
        momHelper.lockMo(tg.abisIpLdn);

        assertEquals(tg.abisIpLdn + " did not reach correct state", "",
                    momHelper.checkAbisIpMoAttributeAfterLock(tg.abisIpLdn, 30));

        setTestStepEnd();
        
    }

    //Codes below are comment out due to limitation in CAT implementation!
    //See comments in UCR426.A6.
    //
    //    /**
    //     * @name unlockTrxAndLockUnlockSectorEquipmentFunc4
    //     * 
    //     * @description Lock SectorEquipmentFunction, Unlock Trx and Unlock SectorEquipmentFunction
    //     * 
    //     * @param testId - unique identifier of the test case
    //     * @param description
    //     * @throws InterruptedException 
    //     */
    //
    //    @Test(timeOut = 500000)
    //    @Parameters({ "testId", "description" })
    //    public void unlockTrxAndLockUnlockSectorEquipmentFunc4(String testId, String description) throws InterruptedException {
    //
    //    	setTestCase(testId, description);
    //
    //    	abisco.setupAbisco(false);
    //
    //    	setTestInfo("Precondition: Create GsmSector MO, AbisIp MO and Trx MO");   
    //    	String abisIpLdn = sectorMoLdn + ",AbisIp=1";
    //    	abisIpLdn = momHelper.createSectorAndAbisIpMo(AbiscoConnection.getConnectionName(), abisco.getBscIpAddress(), false);
    //    	trxMoLdn = momHelper.createTrxMo(sectorMoLdn, "0");        
    //
    //    	setTestInfo("Unlock AbisIp");
    //    	momHelper.unlockMo(abisIpLdn);
    //
    //    	setTestInfo("Connection TG and establish SCF OML link");
    //    	abisco.connectTG();
    //
    //    	try {
    //    		abisco.establishLinks();
    //    	} catch (InterruptedException ie) {
    //    		fail("InteruptedException during establishLinks");
    //    	}
    //
    //    	startSoScf();
    //    	abisHelper.sendStartToAllMoVisibleInGsmSector(0);
    //    	abisHelper.sendAoAtConfigPreDefBundling(0);
    //    	assertTrue("Configuration signature has not been calculated (value = 0) ", 0 != abisHelper.enableRequest(OM_G31R01.Enums.MOClass.AT, 0).getConfigurationSignature().intValue());
    //
    //    	// Lock SectorEquipmentFunction
    //    	setTestStepBegin("Test Lock SectorEquipmentFunction MO: " + momHelper.getSectorEquipmentFunctionLdn());
    //    	momHelper.setAttributeForMoAndCommit(momHelper.getSectorEquipmentFunctionLdn(), "administrativeState", "LOCKED");      
    //    	setTestStepEnd();
    //
    //    	// Unlock Trx
    //    	setTestStepBegin("Test Unlock Trx MO: " + trxMoLdn);
    //    	momHelper.setAttributeForMoAndCommit(trxMoLdn, "administrativeState", "UNLOCKED");
    //    	assertEquals("Trx MO attributes did not reach expected values after unlock", "", momHelper.checkTrxMoAttributeWithLockedSectorEquipmentFunc(trxMoLdn, 5));
    //    	setTestStepEnd();        
    //
    //    	setTestInfo("Establish Oml&Rsl links for Trxc");
    //    	try {
    //    		abisco.establishLinks(true);
    //    	} catch (InterruptedException ie) {
    //    		fail("InteruptedException during establishLinks");
    //    	}        
    //
    //    	// Unlock SectorEquipmentFunction
    //    	setTestStepBegin("Test Unlock SectorEquipmentFunction MO: " + momHelper.getSectorEquipmentFunctionLdn());
    //    	momHelper.setAttributeForMoAndCommit(momHelper.getSectorEquipmentFunctionLdn(), "administrativeState", "UNLOCKED");     
    //    	saveAssertEquals("Trx MO attributes did not reach expected values after unlock", "", momHelper.checkTrxMoAttributeAfterUnlockLinksEstablished(trxMoLdn, 5));
    //    	setTestStepEnd();
    //
    //    	// Cleanup 
    //    	setTestInfo("Cleanup delete MOs");
    //    	momHelper.lockMo(trxMoLdn);
    //    	momHelper.deleteMo(trxMoLdn);
    //    	momHelper.lockMo(abisIpLdn);
    //    	momHelper.deleteMo(abisIpLdn);
    //    	momHelper.deleteMo(sectorMoLdn);
    //
    //    	setTestInfo("Test is done!");        
    //
    //    }  
}
