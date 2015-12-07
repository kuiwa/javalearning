package com.ericsson.msran.test.grat.frequencyhopping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.json.JSONException;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import se.ericsson.jcat.fw.annotations.Setup;
import se.ericsson.jcat.fw.annotations.Teardown;

import com.ericsson.abisco.clientlib.AbiscoClient;
import com.ericsson.abisco.clientlib.AbiscoServer.Routing;
import com.ericsson.abisco.clientlib.servers.CMDHAND;
import com.ericsson.abisco.clientlib.servers.CMDHAND.Channel_Group1;
import com.ericsson.abisco.clientlib.servers.CMDHAND.Channel_Group2;
import com.ericsson.abisco.clientlib.servers.CMDHAND.Enums;
import com.ericsson.abisco.clientlib.servers.CMDHAND.Enums.HoppingType;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.ActivationTypeStruct;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.BSPowerStruct;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.ChannelActNormalAssign;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.ChannelModeStruct;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.ChannelNoStruct;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.Enums.ActivationType;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.Enums.ChannelType;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.Enums.TypeOfCh;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.MSPowerStruct;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.TimingAdvanceStruct;
import com.ericsson.commonlibrary.resourcemanager.Mssim;
import com.ericsson.commonlibrary.resourcemanager.Rm;
import com.ericsson.msran.g2.annotations.TestInfo;
import com.ericsson.msran.jcat.TestBase;
import com.ericsson.msran.test.grat.testhelpers.AbisHelper;
import com.ericsson.msran.test.grat.testhelpers.AbiscoConnection;
import com.ericsson.msran.test.grat.testhelpers.GsmbHelper;
import com.ericsson.msran.test.grat.testhelpers.MomHelper;
import com.ericsson.msran.test.grat.testhelpers.MssimHelper;
import com.ericsson.msran.test.grat.testhelpers.NodeStatusHelper;
import com.ericsson.mssim.gsmb.ChnComb;
import com.ericsson.mssim.gsmb.ChnMain;
import com.ericsson.mssim.gsmb.Confirmation;
import com.ericsson.mssim.gsmb.FrequencyStructure.FrStructureType;
import com.ericsson.mssim.gsmb.Gsmb;
import com.ericsson.mssim.gsmb.GsmphMPH_CHN_OPEN_CNF;
import com.ericsson.mssim.gsmb.GsmphMPH_CS_CHN_OPEN_REQ;
import com.ericsson.mssim.gsmb.impl.GsmbFactory;
import com.ericsson.mssim.gsmb.impl.GsmphMPH_CS_CHN_OPEN_REQBuilder;
import com.ericsson.mssim.gsmb.packing.internal.PhErrorType;

public class FrequencyHopping extends TestBase {
    private NodeStatusHelper nodeStatus;
    private Gsmb gsmb;
    private AbisHelper abisHelper;
    private AbiscoConnection abisco;
    private MomHelper momHelper;
    private GsmbHelper gsmbHelper;
    private MssimHelper mssimHelper;
    private short mssimCell;

    private final int HSN_ = 2;
    private final int TG = 0;
    

    /**
     * Description of test case for test reporting
     */
    @TestInfo(
            tcId = "FrequencyHopping",
            slogan = "Verify Frequency Hopping",
            requirementDocument = "1/00651-FCP 130 1402",
            requirementRevision = "PC5",
            requirementLinkTested = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff87c1d941?docno=1/00651-FCP1301402Uen&format=excel8book",
            requirementLinkLatest = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff86a2af3d?docno=1/00651-FCP1301402Uen&action=current&format=excel8book",
            requirementIds = { "10565-0334/21767[PA1][PREL]", "10565-0334/19034[A][APPR]",
            "10565-0334/19417[A][APPR]" },
            verificationStatement = "Frequency Hopping",
            testDescription = "Verifies the Frequency Hopping functions.",
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
        abisco = new AbiscoConnection();
        momHelper = new MomHelper();
        Mssim msSim = Rm.getMssimList().get(0);
        gsmb = msSim.getGsmb();
        mssimHelper = new MssimHelper(gsmb);
        mssimCell = mssimHelper.getMssimCellToUse();
        gsmbHelper = new GsmbHelper(gsmb);
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
     * Define a cell configured for hopping. The first frequency in the list will be used for
     * channel group 1 (BCCH),
     * while the rest are used for channel group 2 that hops.
     * 
     * @param useBasebandHopping - Use synthesizer hopping if false
     * @param numberOfTrxes
     * @param frequencyList
     * @throws InterruptedException
     */
    public void defineHoppingCell(HoppingType hoppingType, int numberOfTrxes, List<Integer> frequencyList)
            throws InterruptedException {
        //TODO Channel Groups can't be changed with UpdateCell, so we define the cell from scratch.
        // Change when Abisco releases a fix.
        AbiscoClient abiscoClient = abisHelper.getAbiscoClient();
        CMDHAND.DefineCell defineCell = abiscoClient.getCMDHAND().createDefineCell();
        defineCell.setTGId(TG);
        defineCell.setBCCHno(frequencyList.get(0)); // First frequency is used for BCCH

        List<Integer> trxList = new ArrayList<Integer>();
        for (int i = 0; i < numberOfTrxes; ++i) {
            trxList.add(i);
        }

        defineCell.setTRXList(trxList);
        defineCell.setARFCNList(frequencyList);
        defineCell.setBS_AG_BLKS_RES(1);
        defineCell.setBS_PA_MFRMS(3);
        defineCell.setTXCell(Arrays.asList(0));
        defineCell.setTXChannelGroup(Arrays.asList(0));
        defineCell.setBSPWRB(34);
        defineCell.setBSPWRT(33);
        defineCell.setHSN(HSN_);
        defineCell.setBand(momHelper.getGsmBandToUse());
        defineCell.setMSBand(momHelper.getMsBandToUse());
        defineCell.setHoppingIndicator(Enums.HoppingIndicator.CH_GROUPS);
        Channel_Group1 cg1 = new Channel_Group1(Enums.HoppingType.NO_HOPPING);
        cg1.setTrxList(Arrays.asList(0));
        cg1.setArfcn(Arrays.asList(frequencyList.get(0)));
        defineCell.setChannel_Group1(cg1);

        Channel_Group2 cg2 = new Channel_Group2(hoppingType);
        cg2.setHsn(HSN_);
        cg2.setMaio(trxList.subList(0, numberOfTrxes - 1)); // [0,..,number of hopping Trxes)
        cg2.setTrxList(trxList.subList(1, numberOfTrxes)); // Use all but Trx=0 in this ChGr
        cg2.setArfcn(frequencyList.subList(1, frequencyList.size())); // Hop with all but BCCH frequency
        defineCell.setChannel_Group2(cg2);
        defineCell.send();
    }

    /**
     * Open a TCH channel on mssim
     * 
     * @param frequencyList List of frequencies
     * @return the channel id
     */
    long openChannelMssim(List<Integer> frequencyList, int ts) {
        setTestStepBegin("Open channel on MsSIM side");
        // Turn List into primitive array
        int freqArr[] = ArrayUtils.toPrimitive(frequencyList.toArray(new Integer[0]));

        // First set the common values...
        GsmphMPH_CS_CHN_OPEN_REQBuilder cs_chn_open_builder = GsmbFactory
                .getGsmphMPH_CS_CHN_OPEN_REQBuilder(mssimCell, 
                        frequencyList.size() == 1 ? FrStructureType.ARFCN : FrStructureType.HOP)
                        .timeout(20)
                        .ref(0)
                        .msId(ts)
                        .trxNum((short) 0)
                        .ts((short) ts)
                        .chnComb(ChnComb.GSMPH_TCHXF_FACCHXF_SACCHXTF)
                        .chnMain(ChnMain.GSMPH_TCH_FS)
                        .sub((short) 0)
                        .tsc((short) 1)
                        .rxAcchOn(true)
                        .allSacchSI(false)
                        .undecFrm(true);

        // ...then the specific values depending on if hopping
        if (frequencyList.size() == 1) { // No hop
            cs_chn_open_builder = cs_chn_open_builder
                    .narfcn((short)0)
                    .frArfcn(frequencyList.get(0));
        } else {                         // Yes hop
            cs_chn_open_builder = cs_chn_open_builder
                    .narfcn((short) freqArr.length)
                    .arfcnl(freqArr)
                    .frHopHsn(HSN_)
                    .frHopMaio(0);
        }

        GsmphMPH_CS_CHN_OPEN_REQ ms_CHN_OPEN_REQ = cs_chn_open_builder.build();
        Confirmation confirm = gsmb.send(ms_CHN_OPEN_REQ);
        assertEquals("GsmphMPH_CS_CHN_OPEN_REQ confirm failed", PhErrorType.GSM_PH_ENOERR, confirm.getErrorType());
        long ccId = 0;
        if (confirm instanceof GsmphMPH_CHN_OPEN_CNF) {
            ccId = ((GsmphMPH_CHN_OPEN_CNF) confirm).getCcId();
            setTestInfo("Channel opened, ccId = " + ccId);
        } else {
            fail("Did not get a GsmphMPH_CHN_OPEN_CNF");
        }
        setTestStepEnd();
        return ccId;
    }

    void activateChannelNormalAssignment(int trxc, int ts) throws InterruptedException {
        setTestInfo("Activate Channel");
        ChannelActNormalAssign chAct = abisHelper.getRslServer().createChannelActNormalAssign();

        chAct.setRouting(new Routing(TG, trxc));
        chAct.getCH_Action().setSyntaxCheckOff();
        chAct.setChannelNoStruct(new ChannelNoStruct(ts, ChannelType.Bm));
        chAct.setActivationTypeStruct(new ActivationTypeStruct(ActivationType.INTRA_NOR));

        ChannelModeStruct chanMode = new ChannelModeStruct();
        chanMode.setTypeOfCh(TypeOfCh.SPEECH);
        chAct.setChannelModeStruct(chanMode);

        chAct.setMSPowerStruct(new MSPowerStruct(0));

        BSPowerStruct bsPow = new BSPowerStruct();
        bsPow.setRawBSPower(0);
        chAct.setBSPowerStruct(bsPow);

        chAct.setTimingAdvanceStruct(new TimingAdvanceStruct(0));
        chAct.send();
    }

    /**
     * @name synthHoppingTest
     * @description Verify frequency hopping using synth hop.
     * @param testId - String - Unique identifier of the test case.
     * @param description - String - Brief description of test case.
     * @param hoppingType - Type of hopping used. Must be "synth" or "baseband"
     * @param numOfFreqs - Number of frequencies used, must be at least 3 (1 for non-hopping, rest hops).
     * @throws JSONException
     * @throws InterruptedException
     */
    @Test(timeOut = 1800000)
    @Parameters({ "testId", "description", "hoppingType", "numOfFreqs"})
    public void hoppingTest(String testId, String description, String hoppingTypeString, int numOfFreqs) throws InterruptedException, JSONException {
        /**
         * This test opens two TCH channels, one on TRXC=0 that does not frequency hop, and one on TRXC=1 that does hop.
         * The frequency levels are measured by the mssim and compared to see that they do not differ (too much).
         */
        setTestCase(testId, description);
        // First verify indata
        if (!hoppingTypeString.equals("synth") && !hoppingTypeString.equals("baseband")) fail("Unknown hopping type: " + hoppingTypeString);
        if (numOfFreqs < 3) fail("Too few frequencies used, " + numOfFreqs);

        // Config node, abisco, mssim
        int NUM_OF_TRXES = 0;
        HoppingType hoppingType;
        if (hoppingTypeString.equals("synth")) {
            setTestInfo("Test uses synth hopping");
            NUM_OF_TRXES = 2;
            hoppingType = HoppingType.SYNTH;
        } else {
            setTestInfo("Test uses baseband hopping");
            NUM_OF_TRXES = numOfFreqs;
            hoppingType = HoppingType.BASEBAND;
        }
        
        setTestStepBegin("Init MS-SIM");
        assertTrue("Could not initialize MS-SIM", gsmbHelper.mssimInit(getCurrentTestCaseName(), true));
        setTestStepEnd();

        setTestStepBegin("Create Abisco TG");
        abisco.disconnectAndDeleteTG(TG);
        abisco.createTgPreDefBundling(TG, AbiscoConnection.getConnectionName(TG), NUM_OF_TRXES, false);
        // Create list of frequencies, increment by 2
        int startArfcn = momHelper.getArfcnToUse();
        List<Integer> fullFrequencyList = new ArrayList<>();
        for (int i = 0; i < numOfFreqs; i++) fullFrequencyList.add(startArfcn + 2 * i);
        defineHoppingCell(hoppingType, NUM_OF_TRXES, fullFrequencyList);
        
        List<Integer> noHopFrequencyList = fullFrequencyList.subList(0, 1);
        List<Integer> hopFrequencyList = fullFrequencyList.subList(1, fullFrequencyList.size());
        abisco.connectTG();
        setTestStepEnd();

        setTestStepBegin("Create MOs");
        momHelper.createUnlockAllGratMos(1, NUM_OF_TRXES);
        setTestStepEnd();

        setTestStepBegin("Start Abis MOs");
        abisHelper.completeStartup(TG, NUM_OF_TRXES);
        setTestStepEnd();
        
        setTestStepBegin("Verify that RBS is in right state");
        assertEquals("Node did not reach correct state after Complete Start", "", momHelper.checkAllGratMosAfterCompleteStart(1, NUM_OF_TRXES));
        setTestStepEnd();

        setTestStepBegin("Start MS-SIM");
        assertTrue("Could not define MS-SIM cell", gsmbHelper.mssimDefineCell(5));
        setTestStepEnd();

        // Open channels
        int trxNoHop = 0, tsNoHop = 2, trxHop = 1, tsHop = 4;
        
        setTestStepBegin("Open non-hopping channel on TRXC=" + trxNoHop + ", TS=" + tsNoHop);
        activateChannelNormalAssignment(trxNoHop, tsNoHop);
        abisHelper.activateSpeech(TG, trxNoHop, tsNoHop);
        // Use the first frequency for the non-hopping channel
        long ccNohopId = openChannelMssim(noHopFrequencyList, tsNoHop);
        setTestStepEnd();

        setTestStepBegin("Open hopping channel on TRXC=" + trxHop + ", TS=" + tsHop);
        activateChannelNormalAssignment(trxHop, tsHop);
        abisHelper.activateSpeech(TG, trxHop, tsHop);
        // Use all but the first frequency for hopping
        long ccHopId = openChannelMssim(hopFrequencyList, tsHop);
        setTestStepEnd();
        
        // Measure
        setTestStepBegin("Measure the frequency levels on the channels");
        Map<Integer, Integer> avgRflMap = gsmbHelper.getAverageRadioFrequencyLevels(mssimCell, 0);
        assertNotNull("Could not get frequency measurements", avgRflMap);
        setTestStepEnd();

        setTestStepBegin("Close the channels");
        Confirmation mph_CHN_CLOSE_CNF;
        
        abisHelper.deactivateSpeech(TG, trxNoHop, tsNoHop);
        abisHelper.channelRelease(TG, trxNoHop, tsNoHop).send();
        mph_CHN_CLOSE_CNF = gsmb.send(GsmbFactory.getGsmphMPH_CHN_CLOSE_REQBuilder(ccNohopId).build());
        assertEquals("MPH_CHN_CLOSE_CNF got an error", PhErrorType.GSM_PH_ENOERR, mph_CHN_CLOSE_CNF.getErrorType());
        
        abisHelper.deactivateSpeech(TG, trxHop, tsHop);
        abisHelper.channelRelease(TG, trxHop, tsHop).send();
        mph_CHN_CLOSE_CNF = gsmb.send(GsmbFactory.getGsmphMPH_CHN_CLOSE_REQBuilder(ccHopId).build());
        assertEquals("MPH_CHN_CLOSE_CNF got an error", PhErrorType.GSM_PH_ENOERR, mph_CHN_CLOSE_CNF.getErrorType());
        setTestStepEnd();

        // Compare
        setTestStepBegin("Compare Radio Frequency Levels between the channels");
        int avgRflHop = avgRflMap.get(tsHop);
        int avgRflNoHop = avgRflMap.get(tsNoHop);
        setTestInfo("Average Radio Frequency Level for non-hopping channel: " + avgRflNoHop + "/256 dBm");
        setTestInfo("Average Radio Frequency Level for hopping channel: " + avgRflHop + "/256 dBm");
        int avgRflDifference = Math.abs(avgRflNoHop - avgRflHop);
        setTestInfo("Difference = " + avgRflDifference + "/256 dBm");
        assertTrue("Average Radio Frequency Levels differ too much.",
                avgRflDifference < 256); // 256 units = 1 dBm
        setTestStepEnd();
    }
}
