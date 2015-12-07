package com.ericsson.msran.test.grat.enablerequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import se.ericsson.jcat.fw.annotations.Setup;
import se.ericsson.jcat.fw.annotations.Teardown;

import com.ericsson.abisco.clientlib.servers.CMDHAND.BundlingProfile1;
import com.ericsson.abisco.clientlib.servers.CMDHAND.BundlingProfile2;
import com.ericsson.abisco.clientlib.servers.CMDHAND.BundlingProfile3;
import com.ericsson.abisco.clientlib.servers.CMDHAND.BundlingProfile4;
import com.ericsson.abisco.clientlib.servers.CMDHAND.BundlingProfile5;
import com.ericsson.abisco.clientlib.servers.CMDHAND.BundlingProfiles;
import com.ericsson.abisco.clientlib.servers.CMDHAND;
import com.ericsson.abisco.clientlib.servers.OM_G31R01;
import com.ericsson.abisco.clientlib.servers.OM_G31R01.DisableResult;
import com.ericsson.abisco.clientlib.servers.OM_G31R01.EnableResult;
import com.ericsson.abisco.clientlib.servers.OM_G31R01.Enums;
import com.ericsson.msran.g2.annotations.TestInfo;
import com.ericsson.msran.jcat.TestBase;
import com.ericsson.msran.test.grat.testhelpers.AbisHelper;
import com.ericsson.msran.test.grat.testhelpers.AbiscoConnection;
import com.ericsson.msran.test.grat.testhelpers.MomHelper;
import com.ericsson.msran.test.grat.testhelpers.NodeStatusHelper;
import com.ericsson.msran.test.grat.testhelpers.prepost.AbisPrePost;


/**
 * 
 * @name EnableDisableAtRequestTest
 * 
 * @author GRAT 2014
 * 
 * @created 2014-10-07
 * 
 * @description This test class verifies the Enable and Disable Request
 *              for AO AT. The main reason for this being its own file is
 *              that testing will be done without an unlocked Trx, which is
 *              automatically created in EnableRequestTest during setup.
 * 
 * @revision ewegans 2014-10-07 first version
 * 
 */
public class EnableDisableAtRequestTest extends TestBase {
    private AbisPrePost abisPrePost;
    private AbisHelper abisHelper;
    private MomHelper momHelper;
    private AbiscoConnection abisco;

    
    private final String GSM_SECTOR_LDN = "ManagedElement=1,BtsFunction=1,GsmSector=1";
    private final String ABIS_IP_LDN = GSM_SECTOR_LDN+",AbisIp=1";
    private final String TRX_LDN = GSM_SECTOR_LDN+",Trx=0";
    private final OM_G31R01.Enums.MOClass moClassAt = OM_G31R01.Enums.MOClass.AT;
    private NodeStatusHelper nodeStatus;

    /**
     * Description of test case for test reporting
     */
    @TestInfo(
            tcId = "NodeUC646,NodeUC647",
            slogan = "Abis AT Enable/Disable",
            requirementDocument = "1/00651-FCP 130 1402",
            requirementRevision = "PC5",
            requirementLinkTested = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff87c1d941?docno=1/00651-FCP1301402Uen&format=excel8book",
            requirementLinkLatest = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff86a2af3d?docno=1/00651-FCP1301402Uen&action=current&format=excel8book",
            requirementIds = { "10565-0334/21767[PA1][PREL]", "10565-0334/19034[A][APPR]",
                    "10565-0334/19417[A][APPR]" },
            verificationStatement = "Verifies NodeUC646.N, NodeUC646.A1, and NodeUC647.N",
            testDescription = "Verifies Abis AT Enable and Abis AT Disable.",
            traceGuidelines = "N/A")

    
    /**
     * Create MO:s, establish links to Abisco, and verify preconditions.
     * 
     * @throws InterruptedException when establish links fails
     */
    @Setup
    public void setup() throws InterruptedException {
    	setTestStepBegin("Setup");
        nodeStatus = new NodeStatusHelper();
        assertTrue("Node did not reach a working state before test start", nodeStatus.waitForNodeReady());

        abisHelper = new AbisHelper();
        momHelper = new MomHelper();
        abisco = new AbiscoConnection();
        abisPrePost = new AbisPrePost();
        
    	if (!momHelper.isBtsFunctionCreated()) {
    		setTestInfo("Precondition: Create BtsFunction MO");
    		momHelper.createBtsFunctionMo(MomHelper.BTS_FUNCTION_LDN);
    	}
        
        momHelper.createSectorAbisIpMoAndTrxMo(AbiscoConnection.getConnectionName(), abisco.getBscIpAddress(), false);
        momHelper.unlockMo(ABIS_IP_LDN);
        // Leave the Trx locked
        abisco.establishLinks();
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
    
    
    
    private List<BundlingProfiles> createBundlingProfilesList() {
        List<BundlingProfiles> bundlingProfilesList = new ArrayList<BundlingProfiles>();
        // First setup, default configuration
        {
            BundlingProfile1 bp1 = new CMDHAND.BundlingProfile1();
            bp1.setSAPIList(Arrays.asList(0,62));
            bp1.setCRC(CMDHAND.Enums.CRC.UseCRC);
            bp1.setDSCP(51);
            bp1.setBundlingSize(1465);
    
            BundlingProfile2 bp2 = new CMDHAND.BundlingProfile2();
            bp2.setSAPIList(Arrays.asList(10,11));
            bp2.setCRC(CMDHAND.Enums.CRC.UseCRC);
            bp2.setDSCP(46);
            bp2.setBundlingSize(1465);
    
            BundlingProfile3 bp3 = new CMDHAND.BundlingProfile3();
            bp3.setSAPIList(Arrays.asList(12));
            bp3.setCRC(CMDHAND.Enums.CRC.UseCRC);
            bp3.setDSCP(28);
            bp3.setBundlingSize(1465);
            
            BundlingProfiles bundlingProfiles = new BundlingProfiles();
            bundlingProfiles.setBundlingProfileCount(3);
            bundlingProfiles.setBundlingProfile1(bp1);
            bundlingProfiles.setBundlingProfile2(bp2);
            bundlingProfiles.setBundlingProfile3(bp3);
            bundlingProfilesList.add(bundlingProfiles);
        }
        // Second setup, everything in one profile
        {
            BundlingProfile1 bp1 = new CMDHAND.BundlingProfile1();
            bp1.setSAPIList(Arrays.asList(0,10,11,12,62));
            bp1.setCRC(CMDHAND.Enums.CRC.UseCRC);
            bp1.setDSCP(45);
            bp1.setBundlingSize(1345);
            
            BundlingProfiles bundlingProfiles = new BundlingProfiles();
            bundlingProfiles.setBundlingProfileCount(1);
            bundlingProfiles.setBundlingProfile1(bp1);
            bundlingProfilesList.add(bundlingProfiles);
        }
        // Third setup, two profiles
        {
            BundlingProfile1 bp1 = new CMDHAND.BundlingProfile1();
            bp1.setSAPIList(Arrays.asList(0,10,12));
            bp1.setCRC(CMDHAND.Enums.CRC.UseCRC);
            bp1.setDSCP(51);
            bp1.setBundlingSize(1465);
    
            BundlingProfile2 bp2 = new CMDHAND.BundlingProfile2();
            bp2.setSAPIList(Arrays.asList(11,62));
            bp2.setCRC(CMDHAND.Enums.CRC.UseCRC);
            bp2.setDSCP(46);
            bp2.setBundlingSize(1465);
    
            BundlingProfiles bundlingProfiles = new BundlingProfiles();
            bundlingProfiles.setBundlingProfileCount(2);
            bundlingProfiles.setBundlingProfile1(bp1);
            bundlingProfiles.setBundlingProfile2(bp2);
            bundlingProfilesList.add(bundlingProfiles);
        }
        // Fourth setup, one profile per sapi
        {
            BundlingProfile1 bp1 = new CMDHAND.BundlingProfile1();
            bp1.setSAPIList(Arrays.asList(0));
            bp1.setCRC(CMDHAND.Enums.CRC.UseCRC);
            bp1.setDSCP(51);
            bp1.setBundlingSize(1465);
    
            BundlingProfile2 bp2 = new CMDHAND.BundlingProfile2();
            bp2.setSAPIList(Arrays.asList(10));
            bp2.setCRC(CMDHAND.Enums.CRC.UseCRC);
            bp2.setDSCP(52);
            bp2.setBundlingSize(1465);
    
            BundlingProfile3 bp3 = new CMDHAND.BundlingProfile3();
            bp3.setSAPIList(Arrays.asList(11));
            bp3.setCRC(CMDHAND.Enums.CRC.UseCRC);
            bp3.setDSCP(53);
            bp3.setBundlingSize(1465);
    
            BundlingProfile4 bp4 = new CMDHAND.BundlingProfile4();
            bp4.setSAPIList(Arrays.asList(12));
            bp4.setCRC(CMDHAND.Enums.CRC.UseCRC);
            bp4.setDSCP(54);
            bp4.setBundlingSize(1465);
            
            BundlingProfile5 bp5 = new CMDHAND.BundlingProfile5();
            bp5.setSAPIList(Arrays.asList(62));
            bp5.setCRC(CMDHAND.Enums.CRC.UseCRC);
            bp5.setDSCP(55);
            bp5.setBundlingSize(1465);
            
            BundlingProfiles bundlingProfiles = new BundlingProfiles();
            bundlingProfiles.setBundlingProfileCount(5);
            bundlingProfiles.setBundlingProfile1(bp1);
            bundlingProfiles.setBundlingProfile2(bp2);
            bundlingProfiles.setBundlingProfile3(bp3);
            bundlingProfiles.setBundlingProfile4(bp4);
            bundlingProfiles.setBundlingProfile5(bp5);
            bundlingProfilesList.add(bundlingProfiles);
        }
        return bundlingProfilesList;
    }
    
    
    AbisHelper.TransportProfile createTransportProfilesFromBundlingProfiles(BundlingProfiles bp) {
        AbisHelper.TransportProfile transportProfile = new AbisHelper.TransportProfile();
        // We take a BundlingProfile from method createBundlingProfilesList
        // and extract the necessary data for the matching TransportProfile.
        if (bp.getBundlingProfileCount() >= 1) {
            List<Integer> sapiList = bp.getBundlingProfile1().getSAPIList();
            int bundlingTimeUl = bp.getBundlingProfile1().getBundlingTimeout();
            int bundlingMaxPayloadSizeUl = bp.getBundlingProfile1().getBundlingSize();
            int dscpUl = bp.getBundlingProfile1().getDSCP();
            int overloadThreashold = 0;
            int overloadReportInterval = 1;
            boolean useCrc = (bp.getBundlingProfile1().getCRC() == CMDHAND.Enums.CRC.UseCRC);
            transportProfile.addBp(new AbisHelper.TransportProfile.BundlingProfile(
                    sapiList, bundlingTimeUl, bundlingMaxPayloadSizeUl, useCrc, dscpUl, overloadThreashold, overloadReportInterval));
        }

        if (bp.getBundlingProfileCount() >= 2) {
            List<Integer> sapiList = bp.getBundlingProfile2().getSAPIList();
            int bundlingTimeUl = bp.getBundlingProfile2().getBundlingTimeout();
            int bundlingMaxPayloadSizeUl = bp.getBundlingProfile2().getBundlingSize();
            int dscpUl = bp.getBundlingProfile2().getDSCP();
            int overloadThreashold = 0;
            int overloadReportInterval = 1;
            boolean useCrc = (bp.getBundlingProfile2().getCRC() == CMDHAND.Enums.CRC.UseCRC);
            transportProfile.addBp(new AbisHelper.TransportProfile.BundlingProfile(
                    sapiList, bundlingTimeUl, bundlingMaxPayloadSizeUl, useCrc, dscpUl, overloadThreashold, overloadReportInterval));
        }

        if (bp.getBundlingProfileCount() >= 3) {
            List<Integer> sapiList = bp.getBundlingProfile3().getSAPIList();
            int bundlingTimeUl = bp.getBundlingProfile3().getBundlingTimeout();
            int bundlingMaxPayloadSizeUl = bp.getBundlingProfile3().getBundlingSize();
            int dscpUl = bp.getBundlingProfile3().getDSCP();
            int overloadThreashold = 0;
            int overloadReportInterval = 1;
            boolean useCrc = (bp.getBundlingProfile3().getCRC() == CMDHAND.Enums.CRC.UseCRC);
            transportProfile.addBp(new AbisHelper.TransportProfile.BundlingProfile(
                    sapiList, bundlingTimeUl, bundlingMaxPayloadSizeUl, useCrc, dscpUl, overloadThreashold, overloadReportInterval));
        }

        if (bp.getBundlingProfileCount() >= 4) {
            List<Integer> sapiList = bp.getBundlingProfile4().getSAPIList();
            int bundlingTimeUl = bp.getBundlingProfile4().getBundlingTimeout();
            int bundlingMaxPayloadSizeUl = bp.getBundlingProfile4().getBundlingSize();
            int dscpUl = bp.getBundlingProfile4().getDSCP();
            int overloadThreashold = 0;
            int overloadReportInterval = 1;
            boolean useCrc = (bp.getBundlingProfile4().getCRC() == CMDHAND.Enums.CRC.UseCRC);
            transportProfile.addBp(new AbisHelper.TransportProfile.BundlingProfile(
                    sapiList, bundlingTimeUl, bundlingMaxPayloadSizeUl, useCrc, dscpUl, overloadThreashold, overloadReportInterval));
        }

        if (bp.getBundlingProfileCount() >= 5) {
            List<Integer> sapiList = bp.getBundlingProfile5().getSAPIList();
            int bundlingTimeUl = bp.getBundlingProfile5().getBundlingTimeout();
            int bundlingMaxPayloadSizeUl = bp.getBundlingProfile5().getBundlingSize();
            int dscpUl = bp.getBundlingProfile5().getDSCP();
            int overloadThreashold = 0;
            int overloadReportInterval = 1;
            boolean useCrc = (bp.getBundlingProfile5().getCRC() == CMDHAND.Enums.CRC.UseCRC);
            transportProfile.addBp(new AbisHelper.TransportProfile.BundlingProfile(
                    sapiList, bundlingTimeUl, bundlingMaxPayloadSizeUl, useCrc, dscpUl, overloadThreashold, overloadReportInterval));
        }
        
        return transportProfile;
    }
 
    
    /**
     * @name enableDisableRequestAoAtNoTrx
     * 
     * @description Verifies Enable Request EP for AO AT without unlocked Trx
     *              as well as Disable Request EP.
     * 
     * @param testId - unique identifier
     * @param description
     * 
     * @throws InterruptedException
     */
    @Test (timeOut = 360000)
    @Parameters({ "testId", "description" })
    public void enableDisableRequestAoAtNoTrx(String testId, String description) throws InterruptedException {
        setTestCase(testId, description);
        setTestStepBegin("Setup and check preconditions");
        
        setTestInfo("Precondition: start SCF");
        abisPrePost.startSoScf();
        
        setTestInfo("Precondition: start AT");
        abisHelper.startRequest(moClassAt, 0);
        abisHelper.startRequest(OM_G31R01.Enums.MOClass.TF, 0); // Should not matter, but there is a ready checker for this state
        
        setTestInfo("Precondition: Check that AbisIp and GsmSector are in correct state");
        assertEquals("AbisIp not in correct state", "", momHelper.checkAbisIpMoAttributeAfterUnlockBscConnected(ABIS_IP_LDN, abisco.getBscIpAddress(), 30));
        assertEquals("GsmSector not in correct state", "", momHelper.checkGsmSectorMoAttributeAfterAllMosStarted(GSM_SECTOR_LDN, 5));
        setTestInfo("Precondition: Abis AT has been configured from BSC with a valid configuration.");
        
        AbisHelper.TransportProfile tp = new AbisHelper.TransportProfile();
        tp.createPredefinedBundlingProfiles();
        
        abisHelper.atConfigRequest(tp, 0);
        
        setTestStepEnd();
        int oldSig = 0;
        for (int i = 0; i < 2; ++i) {
            // First iteration: when AT disabled, second iteration: when AT enabled
            setTestStepBegin("Enable AT");
            EnableResult enableRes = abisHelper.enableRequest(moClassAt, 0);
            setTestStepEnd();
            
            setTestStepBegin("Check post-conditions");
            Integer signature = enableRes.getConfigurationSignature();
            if (signature == null) fail("Enable result did not contain a configuration signature");
            if (i == 0) {
                setTestInfo("Postcondition: Abis AT Configuration signature is changed. (The AT configuration is activated) ");
                assertTrue("Configuration signature has not been calculated (value = 0)", signature != 0);
                oldSig = signature;
            } else {
                setTestInfo("Postcondition: Abis AT Configuration is the same as after previous enable");
                assertTrue("Configuration signature has changed, value = " + signature, signature == oldSig);
            }
            
            setTestInfo("Postcondition: Abis AT MO State = ENABLED");
            assertTrue(momHelper.waitForAbisAtMoState("ENABLED"));
            assertEquals("MO State in Enable Result is not ENABLED", OM_G31R01.Enums.MOState.ENABLED, enableRes.getMOState());
            assertEquals("Trx not in correct state", "", momHelper.checkTrxMoAttributeAfterLock(TRX_LDN, 5));
            setTestStepEnd();
        }
        
        /**
         * Case 2: Disable Request in ENABLED state 
         */
        setTestStepBegin("Disable AT");
        DisableResult disableRes = abisHelper.disableRequest(moClassAt);
        setTestStepEnd();

        setTestInfo("Postcondition: Abis AT MO State = DISABLED");
        assertEquals("MO State in Enable Result is not DISABLED", OM_G31R01.Enums.MOState.DISABLED, disableRes.getMOState());
        assertEquals("GsmSector not in correct state", "", momHelper.checkGsmSectorMoAttributeAfterAllMosStarted(GSM_SECTOR_LDN, 5));
        setTestStepEnd();
    }
    
    /**
     * @name enableDisableRequestAoAtWithTrx
     * 
     * @description Verifies Enable Request EP for AO AT with an unlocked Trx
     *              as well as Disable Request EP. Will do this multiple times,
     *              with a different AT configuration each time.
     *              
     * 
     * @param testId - unique identifier
     * @param description
     * 
     * @throws InterruptedException
     */
    @Test (timeOut = 3600000)
    @Parameters({ "testId", "description" })
    public void enableDisableRequestAoAtWithTrx(String testId, String description) throws InterruptedException {
        setTestCase(testId, description);
        momHelper.lockMo(ABIS_IP_LDN);
        List<BundlingProfiles> bundlingProfilesList= createBundlingProfilesList();
        
        for (int i = 0; i< bundlingProfilesList.size(); ++i) {
            setTestStepBegin("**************** New AT Configuration iteration, Bundling Profiles number " + i);
            BundlingProfiles bp = bundlingProfilesList.get(i);
            setTestStepEnd();
            
            setTestStepBegin("Setup and check preconditions");
            // Create a new TG with new bundling profiles
            abisco.disconnectAndDeleteTG(0);
            abisco.createTg(0, AbiscoConnection.getConnectionName(), 1, bp, false);
            abisco.defineCell(0, 1);
            abisco.connectTG();
            
            momHelper.unlockMo(ABIS_IP_LDN);
            momHelper.unlockMo(TRX_LDN);
            
            abisco.establishLinks();
            
            setTestInfo("Precondition: start SCF");
            abisPrePost.startSoScf();

            setTestInfo("Precondition: start AT");
            abisHelper.startRequest(moClassAt, 0);
            abisHelper.startRequest(OM_G31R01.Enums.MOClass.TF, 0); // Should not matter, but there is a ready checker for this state

            setTestInfo("Precondition: Check that AbisIp and GsmSector are in correct state");
            assertEquals("AbisIp not in correct state", "", momHelper.checkAbisIpMoAttributeAfterUnlockBscConnected(ABIS_IP_LDN, abisco.getBscIpAddress(), 30));
            assertEquals("GsmSector not in correct state", "", momHelper.checkGsmSectorMoAttributeAfterAllMosStarted(GSM_SECTOR_LDN, 5));
            setTestInfo("Precondition: Abis AT has been configured from BSC with a valid configuration.");

            AbisHelper.TransportProfile tp = createTransportProfilesFromBundlingProfiles(bp);

            abisHelper.atConfigRequest(tp, 0);
            setTestInfo("Establish Trx links asynchroneously");
            abisco.establishLinksAsync(true);
            setTestStepEnd();

            /**
             * Case 1: Enable Request in DISABLED state 
             */
            setTestStepBegin("Enable AT");
            EnableResult enableRes = abisHelper.enableRequest(moClassAt, 0);
            setTestStepEnd();

            setTestStepBegin("Check post-conditions");
            setTestInfo("Postcondition: Abis AT Configuration signature is changed. (The AT configuration is activated) ");
            Integer signature = enableRes.getConfigurationSignature();
            if (signature == null) fail("Enable result did not contain a configuration signature");
            assertTrue("Configuration signature has not been calculated (value = 0)", signature != 0);

            setTestInfo("Postcondition: Abis AT MO State = ENABLED");
            assertTrue(momHelper.waitForAbisAtMoState("ENABLED"));
            assertEquals("MO State in Enable Result is not ENABLED", OM_G31R01.Enums.MOState.ENABLED, enableRes.getMOState());

            setTestInfo("Check that Trx RSL & OML are UP");
            assertEquals("Trx not in correct state", "", momHelper.checkTrxMoAttributeAfterUnlockLinksEstablished(TRX_LDN, 15));

            setTestInfo("Check that a AT Bundling Info Update was received");
            assertEquals("Did not receive a valid AT Bundling Info Update", "", abisHelper.waitForAtBundlingInfoUpdateWithBundlingData(10, 0));
            setTestStepEnd();

            /**
             * Case 2: Disable Request in ENABLED state 
             */
            setTestStepBegin("Disable AT");
            DisableResult disableRes = abisHelper.disableRequest(moClassAt);
            setTestStepEnd();

            setTestInfo("Postcondition: Abis AT MO State = DISABLED");
            assertEquals("MO State in Enable Result is not DISABLED", OM_G31R01.Enums.MOState.DISABLED, disableRes.getMOState());
            assertEquals("GsmSector not in correct state", "", momHelper.checkGsmSectorMoAttributeAfterAllMosStarted(GSM_SECTOR_LDN, 5));
            setTestStepEnd();
            
            momHelper.lockMo(ABIS_IP_LDN);
            momHelper.lockMo(TRX_LDN);
        }
    }
    
    /**
     * @name enableDisableRequestAoAtWrongSoScfState
     * 
     * @description Verifies Enable Request EP and Disable Request EP for AO AT, when SO SCF is in state RESET according to NodeUC479.E2
     * 
     * @param testId - unique identifier
     * @param description
     * 
     * @throws Exception 
     * 
     */
    @Test (timeOut = 60000)
    @Parameters({ "testId", "description" })
    public void enableDisableRequestAoAtWrongSoScfState(String testId, String description) throws Exception {
        setTestCase(testId, description);       
        
        setTestStepBegin("Pre-cond: Check that SO SCF is in state RESET");
        saveAssertEquals("GsmSector MO attributes did not reach expected values", "", momHelper.checkGsmSectorMoAttributeAfterCreateLinksEstablished(GSM_SECTOR_LDN, 5));
        setTestStepEnd();
        
        setTestStepBegin("Send an AT Enable when SO SCF is in state RESET");
        try {
            abisHelper.enableRequest(moClassAt, 0);
            fail("Expected AT Enable Reject not received");
        } catch (OM_G31R01.EnableRequestRejectException rej) {
            saveAssertEquals("Not WrongState: ", Enums.ResultCode.WrongState, rej.getEnableRequestReject().getResultCode());
        }
        setTestStepEnd();
        
        setTestStepBegin("Send an AT Disable when SO SCF is in state RESET");
        try {
            abisHelper.disableRequest(moClassAt);
            fail("Expected AT Disable Reject not received");
        } catch (OM_G31R01.DisableRequestRejectException rej) {
            saveAssertEquals("Not WrongState: ", Enums.ResultCode.WrongState, rej.getDisableRequestReject().getResultCode());
        }
        setTestStepEnd();
    }   
}
