package com.ericsson.msran.test.grat.psdata;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import se.ericsson.jcat.fw.annotations.Setup;
import se.ericsson.jcat.fw.annotations.Teardown;

import com.ericsson.abisco.clientlib.AbiscoServer.Routing;
import com.ericsson.abisco.clientlib.servers.DISPATCH;
import com.ericsson.abisco.clientlib.servers.PAYLOAD;
import com.ericsson.abisco.clientlib.servers.PAYLOAD.AllocateGslLinkResult;
import com.ericsson.abisco.clientlib.servers.PAYLOAD.DeactivateGslData;
import com.ericsson.abisco.clientlib.servers.PAYLOAD.DeactivateGslDataResult;
import com.ericsson.abisco.clientlib.servers.PAYLOAD.GslStatistics;
import com.ericsson.abisco.clientlib.servers.PAYLOAD.GslStatisticsResult;

import com.ericsson.abisco.clientlib.servers.PAYLOAD.StartGslLinkResult;
import com.ericsson.abisco.clientlib.servers.TRAFFRN;
import com.ericsson.abisco.clientlib.servers.PAYLOAD.AllocateGslLink;
import com.ericsson.abisco.clientlib.servers.PAYLOAD.MACHdrData0;
import com.ericsson.abisco.clientlib.servers.PAYLOAD.MACHdrStruct;
import com.ericsson.abisco.clientlib.servers.PAYLOAD.StartGslLink;
import com.ericsson.abisco.clientlib.servers.PAYLOAD.Enums.PRBSTestSeq;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.ActivationTypeStruct;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.ChannelActAck;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.ChannelActPacketChannel;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.ChannelNoStruct;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.PGSLTimersStruct;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.RFChannelRelease;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.UNACKNOWLEDGED_MESSAGE_STOREDException;

import com.ericsson.commonlibrary.resourcemanager.Rm;
import com.ericsson.msran.g2.annotations.TestInfo;
import com.ericsson.msran.test.grat.testhelpers.AbisHelper;
import com.ericsson.msran.test.grat.testhelpers.AbiscoConnection;
import com.ericsson.msran.test.grat.testhelpers.GsmbHelper;
import com.ericsson.msran.test.grat.testhelpers.MomHelper;
import com.ericsson.msran.test.grat.testhelpers.MssimHelper;
import com.ericsson.msran.test.grat.testhelpers.TgLdns;
import com.ericsson.msran.test.grat.testhelpers.NodeStatusHelper;
import com.ericsson.msran.jcat.TestBase;
import com.ericsson.mssim.gsmb.ChnComb;
import com.ericsson.mssim.gsmb.Confirmation;
import com.ericsson.mssim.gsmb.FrequencyStructure.FrStructureType;
import com.ericsson.mssim.gsmb.GsmappPRBS_START_CMD;
import com.ericsson.mssim.gsmb.GsmappPRBS_STATE_ACK.ChannelType;
import com.ericsson.mssim.gsmb.GsmappPRBS_STATE_ACK.PS;
import com.ericsson.mssim.gsmb.GsmappPRBS_STATE_ACK.PsCnt;
import com.ericsson.mssim.gsmb.GsmappPRBS_STOP_ACK;
import com.ericsson.mssim.gsmb.GsmappPRBS_STOP_CMD;
import com.ericsson.mssim.gsmb.Gsmb;
import com.ericsson.mssim.gsmb.GsmbSrvTEST_FILE_CMD;
import com.ericsson.mssim.gsmb.GsmbSrvTEST_FILE_CMD.PatternConfigurationFileType;
import com.ericsson.mssim.gsmb.GsmphMPH_CHN_CLOSE_REQ;
import com.ericsson.mssim.gsmb.GsmphMPH_PS_CHN_OPEN_REQ;
import com.ericsson.mssim.gsmb.PRBSMode;
import com.ericsson.mssim.gsmb.Response;
import com.ericsson.mssim.gsmb.impl.GsmbFactory;
import com.ericsson.mssim.gsmb.packing.internal.PhErrorType;

/**
 * @id WP4957
 * @name PacketSwitchedData
 * @author GRAT Cell
 * @created 2015-10-14
 * @description Verify GPRS & EGPRS.
 * @revision See Git for version history.
 */

public class PacketSwitchedData extends TestBase {

    private enum CodingAndPuncturingScheme
    {
        // Some of the coding and puncturing scheme combinations that exists, list is not complete
        // 
        CS1,
        CS2,
        MCS1_P1,
        MCS1_P2,
        MCS2_P1,
        MCS2_P2,
        MCS3_P1,
        MCS3_P2,
        MCS3_P3,
        MCS4_P1,
        MCS4_P2,
        MCS4_P3,
        MCS5_P1,
        MCS5_P2,
        MCS6_P1,
        MCS6_P2,

        MCS7_P1P1,
        MCS7_P1P2,
        MCS7_P1P3,
        MCS7_P2P1,
        MCS7_P2P2,
        MCS7_P2P3,
        MCS7_P3P1,
        MCS7_P3P2,
        MCS7_P3P3,

        MCS8_P1P1,
        MCS8_P1P2,
        MCS8_P1P3,
        MCS8_P2P1,
        MCS8_P2P2,
        MCS8_P2P3,
        MCS8_P3P1,
        MCS8_P3P2,
        MCS8_P3P3,

        MCS9_P1P1,
        MCS9_P1P2,
        MCS9_P1P3,
        MCS9_P2P1,
        MCS9_P2P2,
        MCS9_P2P3,
        MCS9_P3P1,
        MCS9_P3P2,
        MCS9_P3P3
    }

    private NodeStatusHelper nodeStatus;
    private Gsmb gsmb;
    private AbisHelper abisHelper;
    private AbiscoConnection abisco;
    private GsmbHelper gsmbHelper;
    private MssimHelper mssimHelper;
    private short mssimCell;
    private MomHelper momHelper;
    
    private int arfcn;
    private final String defaultPatternConfigurationFileDirectory = "/lsu/cfg/gsmb/";

    private static final Map<CodingAndPuncturingScheme, String> codingSchemeMap = new LinkedHashMap<CodingAndPuncturingScheme, String>();
    static
    {
        codingSchemeMap.put(CodingAndPuncturingScheme.CS1, "GRAT_PRBS_CS-1.cfg");
        codingSchemeMap.put(CodingAndPuncturingScheme.CS2, "GRAT_PRBS_CS-2.cfg");

        codingSchemeMap.put(CodingAndPuncturingScheme.MCS1_P2, "GRAT_PRBS_MCS-1_P2.cfg");
        codingSchemeMap.put(CodingAndPuncturingScheme.MCS2_P1, "GRAT_PRBS_MCS-2_P1.cfg");
        codingSchemeMap.put(CodingAndPuncturingScheme.MCS3_P1, "GRAT_PRBS_MCS-3_P1.cfg");
        codingSchemeMap.put(CodingAndPuncturingScheme.MCS4_P1, "GRAT_PRBS_MCS-4_P1.cfg");
        codingSchemeMap.put(CodingAndPuncturingScheme.MCS5_P1, "GRAT_PRBS_MCS-5_P1.cfg");
        codingSchemeMap.put(CodingAndPuncturingScheme.MCS6_P1, "GRAT_PRBS_MCS-6_P1.cfg");
        codingSchemeMap.put(CodingAndPuncturingScheme.MCS7_P1P1, "GRAT_PRBS_MCS-7_P1_P1.cfg");
        codingSchemeMap.put(CodingAndPuncturingScheme.MCS8_P1P1, "GRAT_PRBS_MCS-8_P1_P1.cfg");
        codingSchemeMap.put(CodingAndPuncturingScheme.MCS9_P1P1, "GRAT_PRBS_MCS-9_P1_P1.cfg");
    };

    /**
     * Description of test case for test reporting
     */
    @TestInfo(
            tcId = "WP4957",
            slogan = "Verify Packet Switched Data",
            requirementDocument = "1/00651-FCP 130 1402",
            requirementRevision = "PC5",
            requirementLinkTested = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff87c1d941?docno=1/00651-FCP1301402Uen&format=excel8book",
            requirementLinkLatest = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff86a2af3d?docno=1/00651-FCP1301402Uen&action=current&format=excel8book",
            requirementIds = { "10565-0334/21767[PA1][PREL]", "10565-0334/19034[A][APPR]",
                    "10565-0334/19417[A][APPR]" },
            verificationStatement = "Packet Switched Data",
            testDescription = "Verifies the GPRS & EGPRS functions.",
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
        gsmb = Rm.getMssimList().get(0).getGsmb();
        gsmbHelper = new GsmbHelper(gsmb);
        mssimHelper = new MssimHelper(gsmb);
        mssimCell = mssimHelper.getMssimCellToUse();
        arfcn = momHelper.getArfcnToUse();
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
     * @name packetSwitchedDataTest
     * @description Verify GPRS and EGPRS.
     *              The test is driven by the MSSIM that sends RLC/MAC blocks containing PRBS
     *              sequences uplink.
     *              TSS receives the data and loops it back to the MSSIM.
     *              The PRBS sequence is verified in both the TSS and the MSSIM.
     *              The same coding scheme is used UL and DL.
     *              The following is tested:
     *              - coding Schemes for GPRS (CS-1 - CS-2)
     *              - coding Schemes for EDGE (MSC-1 - MCS-9)
     *              - BTTI mode in downlink/uplink
     * @param testId - String - Unique identifier of the test case.
     * @param description - String - Brief description of test case.
     * @throws JSONException
     * @throws InterruptedException
     */
    @Test(timeOut = 1800000)
    @Parameters({ "testId", "description" })
    public void packetSwitchedDataTest(String testId, String description) throws InterruptedException, JSONException {

        setTestCase(testId, description);

        PAYLOAD.Enums.Encoding codingScheme;
        CodingAndPuncturingScheme codeAndPunctureScheme;
        String patternCfgFileName = null;

        long ccId; // Channel combination identifier
        int numOfTGs = 1; // The number of Transceiver Groups (TGs) used
        int numOfTrxesPerTG = 1; // The number of TRXes in every TG
        int numberOfMSes = 1; // The number of Mobile Stations that will be used at one time
        short ts1 = 3; // main time slot
        short ts2 = 5; // secondary time slot, applicable only with RTTI (RTTI is not supported in 16B)

        // First init MsSIM (to give MsSIM some extra time to synch)
        setTestStepBegin("Setup MSSIM");
        assertTrue("Failed to initiate MsSIM", gsmbHelper.mssimInit(getCurrentTestCaseName(), true));
        setTestStepEnd();

        setTestStepBegin("Create MOs and setup Abisco");
        List<TgLdns> tgList = momHelper.createUnlockAllGratMos(numOfTGs, numOfTrxesPerTG);
        abisco.setupAbisco(numOfTGs, numOfTrxesPerTG, false);
        abisHelper.completeStartup(numOfTGs, numOfTrxesPerTG, tgList);
        assertEquals("Node did not reach correct state after Complete Start", "",
                momHelper.checkAllGratMosAfterCompleteStart(numOfTGs, numOfTrxesPerTG));
        setTestStepEnd();

        setTestStepBegin("Define Cell");
        assertTrue("Failed to define cell in MSSIM", gsmbHelper.mssimDefineCell(numberOfMSes));
        setTestStepEnd();

        setTestStepBegin("Release Channel");
        rfChannelRelease(ts1);
        setTestStepEnd();

        // Start loop over all coding schemes  
        for (Map.Entry<CodingAndPuncturingScheme, String> entry : codingSchemeMap.entrySet())
        {
            codeAndPunctureScheme = entry.getKey();
            codingScheme = convertToCodingScheme(codeAndPunctureScheme);
            patternCfgFileName = defaultPatternConfigurationFileDirectory + entry.getValue();
            setTestStepBegin("Test coding scheme: " + codingScheme +
                    ", use pattern configuration file: " + patternCfgFileName);

            setTestDebug("codeAndPunctureScheme: " + codeAndPunctureScheme);

            setSubTestStep("Activate channel for packet data");
            channelActPacketChannel(ts1);

            setSubTestStep("Wait 6 seconds");
            sleepSeconds(6);

            setSubTestStep("Open PS channel");
            ccId = openPsChannel(ts1, ts2);

            setSubTestStep("Load the pattern configuration file");
            loadPatternConfigFile(patternCfgFileName);

            setSubTestStep("Deactivate GSL Data");
            deactivateGslData(ts1);

            setSubTestStep("Allocate GSL Link");
            allocateGslLink(ts1);

            setSubTestStep("Start GSL Link");
            startGslLink(ts1, codingScheme, codeAndPunctureScheme);

            setSubTestStep("Start PRBS");
            startPRBS(ccId, patternCfgFileName);

            setSubTestStep("Wait 30 seconds");
            sleepSeconds(30);

            setSubTestStep("Stop PRBS and verify received blocks");
            stopPRBS(ccId, codingScheme);

            setSubTestStep("Wait 2 seconds");
            sleepSeconds(2);

            setSubTestStep("Get statistics");
            getGslStatistics(ts1);

            setSubTestStep("Deactivate GSL Data");
            deactivateGslData(ts1);

            setSubTestStep("Close PS channel");
            closeChannel(ccId);

            setSubTestStep("Release Channel");
            rfChannelRelease(ts1);

            setTestStepEnd();
        }

        // MS-SIM disconnected and MOs removed by RestoreStack
    }

    //MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
    // Help methods
    //MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
    //MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM

    /**
     * @name isEgprs
     * @description Returns true if coding scheme is an EGPRS coding scheme
     */
    private boolean isEGPRS(PAYLOAD.Enums.Encoding codingScheme)
    {
        switch (codingScheme)
        {
        case CS_1:
        case CS_2:
        case CS_3:
        case CS_4:
            return false;

        default:
            return true;
        }
    }

    /**
     * @name convertToCodingScheme
     * @description Returns coding scheme
     */
    private PAYLOAD.Enums.Encoding convertToCodingScheme(CodingAndPuncturingScheme puncturingScheme)
    {
        switch (puncturingScheme)
        {
        case CS1:
            return PAYLOAD.Enums.Encoding.CS_1;
        case CS2:
            return PAYLOAD.Enums.Encoding.CS_2;

            // Header type 3
        case MCS1_P1:
        case MCS1_P2:
            return PAYLOAD.Enums.Encoding.MCS_1;

        case MCS2_P1:
        case MCS2_P2:
            return PAYLOAD.Enums.Encoding.MCS_2;

        case MCS3_P1:
        case MCS3_P2:
        case MCS3_P3:
            return PAYLOAD.Enums.Encoding.MCS_3;

        case MCS4_P1:
        case MCS4_P2:
        case MCS4_P3:
            return PAYLOAD.Enums.Encoding.MCS_4;

            // Header type 2
        case MCS5_P1:
        case MCS5_P2:
            return PAYLOAD.Enums.Encoding.MCS_5;

        case MCS6_P1:
        case MCS6_P2:
            return PAYLOAD.Enums.Encoding.MCS_6;

            // Header type 1
        case MCS7_P1P1:
        case MCS7_P1P2:
        case MCS7_P1P3:
        case MCS7_P2P1:
        case MCS7_P2P2:
        case MCS7_P2P3:
        case MCS7_P3P1:
        case MCS7_P3P2:
        case MCS7_P3P3:
            return PAYLOAD.Enums.Encoding.MCS_7;

        case MCS8_P1P1:
        case MCS8_P1P2:
        case MCS8_P1P3:
        case MCS8_P2P1:
        case MCS8_P2P2:
        case MCS8_P2P3:
        case MCS8_P3P1:
        case MCS8_P3P2:
        case MCS8_P3P3:
            return PAYLOAD.Enums.Encoding.MCS_8;

        case MCS9_P1P1:
        case MCS9_P1P2:
        case MCS9_P1P3:
        case MCS9_P2P1:
        case MCS9_P2P2:
        case MCS9_P2P3:
        case MCS9_P3P1:
        case MCS9_P3P2:
        case MCS9_P3P3:
            return PAYLOAD.Enums.Encoding.MCS_9;

        default:
            fail("Convertion of Puncturing and Coding Scheme: " + puncturingScheme + " is not yet supported in this test case");
            return PAYLOAD.Enums.Encoding.OUT_OF_BOUNDS;
        }
    }

    /**
     * @throws InterruptedException
     * @name deactivateGslData
     * @description Send deactivateGslData
     */
    private void deactivateGslData(short ts) throws InterruptedException
    {
        DeactivateGslData msg = abisHelper.getPayloadServer().createDeactivateGslData();
        msg.setTS(ts);
        setTestDebug("DeactivateGslData: " + msg.toString());
        DeactivateGslDataResult result = msg.send();
        setTestDebug("DeactivateGslDataResult: " + result.toString());
    }

    /**
     * @throws InterruptedException
     * @name allocateGslLink
     * @description Send allocateGslLink
     */
    private void allocateGslLink(int ts) throws InterruptedException
    {
        AllocateGslLink msg = abisHelper.getPayloadServer().createAllocateGslLink();
        msg.setTG(0);
        msg.setTRXC(0);
        msg.setTS(ts);
        msg.setTLINK(6);
        msg.setTSUP(12);
        msg.setPTA(9);
        msg.setPAL(0);

        setTestDebug("AllocateGslLink: " + msg.toString());
        AllocateGslLinkResult result = msg.send();
        setTestDebug("AllocateGslLinkResult: " + result.toString());
    }

    /**
     * @name getULCodingScheme
     * @param codingScheme
     * @returns Coding scheme to expect for traffic frames uplink
     */
    private PAYLOAD.Enums.EncodingUL getULCodingScheme(PAYLOAD.Enums.Encoding codingSceme)
    {
        switch (codingSceme)
        {
        case CS_1:
            return PAYLOAD.Enums.EncodingUL.CS_1;
        case CS_2:
            return PAYLOAD.Enums.EncodingUL.CS_2;
        case MCS_1:
            return PAYLOAD.Enums.EncodingUL.MCS_1;
        case MCS_2:
            return PAYLOAD.Enums.EncodingUL.MCS_2;
        case MCS_3:
            return PAYLOAD.Enums.EncodingUL.MCS_3;
        case MCS_4:
            return PAYLOAD.Enums.EncodingUL.MCS_4;
        case MCS_5:
            return PAYLOAD.Enums.EncodingUL.MCS_5;
        case MCS_6:
            return PAYLOAD.Enums.EncodingUL.MCS_6;
        case MCS_7:
            return PAYLOAD.Enums.EncodingUL.MCS_7;
        case MCS_8:
            return PAYLOAD.Enums.EncodingUL.MCS_8;
        case MCS_9:
            return PAYLOAD.Enums.EncodingUL.MCS_9;

        default:
            // Only coding scheme: CS_1-CS_2 and MCS_1-MCS_9 supported so far in this test case
            fail("Coding scheme " + codingSceme + " is not supported in this test case");
            return PAYLOAD.Enums.EncodingUL.OUT_OF_BOUNDS;
        }
    }

    /**
     * @name getUCM
     * @param codingScheme
     * @returns Uplink Channel Mode to be used with given coding scheme
     */
    private PAYLOAD.Enums.UCM getUCM(PAYLOAD.Enums.Encoding codingScheme)
    {
        switch (codingScheme)
        {
        case CS_1:
        case CS_2:
        case MCS_1:
        case MCS_2:
        case MCS_3:
        case MCS_4:
            return PAYLOAD.Enums.UCM.NB_GMSK;

        case CS_3:
        case CS_4:
        case MCS_5:
        case MCS_6:
        case MCS_7:
        case MCS_8:
        case MCS_9:
            return PAYLOAD.Enums.UCM.NB_unkn;

        default:
            // Only coding scheme: CS_1-CS_4 and MCS_1-MCS_9 supported so far in this test case
            fail("Coding scheme " + codingScheme + " is not supported in this test case");
            return PAYLOAD.Enums.UCM.OUT_OF_BOUNDS;
        }
    }

    /**
     * @name createMacHeader0
     * @description Create MAC Header Data0
     * @returns - a list of integers to be used when setting MAC Header Data0
     */
    private List<Integer> createMacHeader0(CodingAndPuncturingScheme puncturingScheme)
    {
        List<Integer> machHeaderData0 = Arrays.asList(0, 0, 0, 0);
        switch (puncturingScheme)
        {
        // Header type 3, see 3GPP spec: Table 10.4.8a.3.1, page 253
        case MCS1_P2: // octet 4, bit 2-5: xxx1100x
            machHeaderData0 = Arrays.asList(0, 0, 0, 24);
            break;
        case MCS2_P1: // octet 4, bit 2-5: xxx1001x
            machHeaderData0 = Arrays.asList(0, 0, 0, 18);
            break;
        case MCS3_P1: // octet 4, bit 2-5: xxx0011x
            machHeaderData0 = Arrays.asList(0, 0, 0, 6);
            break;
        case MCS4_P1: // octet 4, bit 2-5:	xxx0000x
            machHeaderData0 = Arrays.asList(0, 0, 0, 0);						
            break;

        // Header type 2, see 3GPP spec: Table 10.4.8a.2.1, page 252
        case MCS5_P1: // octet 4, bit 2-4: xxxx100x	
            machHeaderData0 = Arrays.asList(0, 0, 0, 8);
            break;
        case MCS6_P1: // octet 4, bit 2-4: xxxx000x
            machHeaderData0 = Arrays.asList(0, 0, 0, 0);							
            break;

        // Header type 1, see 3GPP spec: Table 10.4.8a.1.1, page 251
        case MCS7_P1P1: // octet 5, bit 4-8:	10100xxx
            machHeaderData0 = Arrays.asList(0, 0, 0, 0, 160);
            break;
        case MCS8_P1P1: // octet 5, bit 4-8: 01011xxx
            machHeaderData0 = Arrays.asList(0, 0, 0, 0, 88);
            break;
        case MCS9_P1P1: // octet 5, bit 4-8: 00000xxx
            machHeaderData0 = Arrays.asList(0, 0, 0, 0, 0);
            break;
        default:
            fail("Creating MAC header for Coding and puncturing scheme: " + puncturingScheme + "is not yet supported in this test case");
        }
        return machHeaderData0;
    }

    

 
    
  
    
    /**
     * @throws InterruptedException
     * @name startGslLink
     * @description Send startGslLink
     */
    private void startGslLink(int ts, PAYLOAD.Enums.Encoding codingScheme, CodingAndPuncturingScheme puncturingScheme)
            throws InterruptedException
    {
        PAYLOAD payload = abisHelper.getPayloadServer();
        StartGslLink startGslLink = payload.createStartGslLink();

        startGslLink.setTG(0);
        startGslLink.setTRXC(0);
        startGslLink.setTS(ts);
        startGslLink.setOperationMode(PAYLOAD.Enums.OperationMode.OP_MODE_PRBS_LOOP);
        startGslLink.setNumberOfFrames(0);
        startGslLink.setEncoding(codingScheme);
        startGslLink.setEncodingUL(getULCodingScheme(codingScheme));

        startGslLink.setPRBSTestSeq(PRBSTestSeq.PRBS_511);

        startGslLink.setTimingOffset(0);
        startGslLink.setPowerControl(0);
        startGslLink.setUCM(getUCM(codingScheme));

        startGslLink.setOffsetForValidation(0);
        startGslLink.setNumberOfOctetsForValidation(0);
        startGslLink.setULPANIndicator(1);
        startGslLink.setData(Arrays.asList(0, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17,
                17, 17, 17, 17, 17));

        MACHdrStruct machdrstruct = new MACHdrStruct();
        MACHdrData0 machdrdata0 = new MACHdrData0();

        if (isEGPRS(codingScheme))
        {
            setTestInfo("Set MAC Header Data0 (only needed for EGPRS)");
            machdrdata0.setHdrData(createMacHeader0(puncturingScheme));
            machdrstruct.setMACHdrData0(machdrdata0);
        }
        startGslLink.setMACHdrStruct(machdrstruct);

        setTestDebug("StartGslLink: " + startGslLink.toString());
        StartGslLinkResult startGslLinkResult = startGslLink.send();
        setTestDebug("StartGslLinkResult: " + startGslLinkResult.toString());
    }

    /**
     * @throws InterruptedException
     * @name getGslStatistics
     * @description Send GslStatistics
     */
    private void getGslStatistics(short ts) throws InterruptedException
    {
        PAYLOAD payload = abisHelper.getPayloadServer();
        GslStatistics req = payload.createGslStatistics();
        req.setTS(ts);
        GslStatisticsResult statisticsResult = req.send();
        setTestDebug("statisticsResult: " + statisticsResult.toString());
    }

    /**
     * @throws InterruptedException
     * @name channelActPacketChannel
     * @description Send channelActPacketChannel
     */
    private void channelActPacketChannel(short ts) throws InterruptedException
    {
        TRAFFRN traffrn = abisHelper.getRslServer();
        ChannelActPacketChannel msg = traffrn.createChannelActPacketChannel();
        Routing routing = new Routing();
        routing.setTG(0);
        routing.setTRXC(0);
        msg.setRouting(routing);

        ChannelNoStruct channelnostruct = new ChannelNoStruct();
        channelnostruct.setTimeSlotNo(ts);
        channelnostruct.setChannelType(TRAFFRN.Enums.ChannelType.PDCH);
        msg.setChannelNoStruct(channelnostruct);

        ActivationTypeStruct activationtypestruct = new ActivationTypeStruct();
        activationtypestruct.setActivationType(TRAFFRN.Enums.ActivationType.PACK_CH);
        activationtypestruct.setReserved(0);
        activationtypestruct.setRFB(TRAFFRN.Enums.RFB.Fixed);
        activationtypestruct.setRbit(TRAFFRN.Enums.Rbit.Activate);
        msg.setActivationTypeStruct(activationtypestruct);

        PGSLTimersStruct pgsltimersstruct = new PGSLTimersStruct();
        pgsltimersstruct.setPTA(9);
        pgsltimersstruct.setSpare1(0);
        pgsltimersstruct.setPAL(0);
        pgsltimersstruct.setSpare2(0);
        msg.setPGSLTimersStruct(pgsltimersstruct);

        setTestDebug("ChannelActPacketChannel: " + msg.toString());
        ChannelActAck ack = msg.send();
        setTestDebug("ChannelActAck: " + ack.toString());
    }

    /**
     * @throws InterruptedException
     * @name rfChannelRelease
     * @description Send RFChannelRelease
     * @param ts - time slot
     */
    private void rfChannelRelease(short ts) throws InterruptedException
    {
        TRAFFRN traffrn = abisHelper.getRslServer();
        RFChannelRelease rFChannelRelease = traffrn.createRFChannelRelease();

        ChannelNoStruct channelnostruct = new ChannelNoStruct();
        channelnostruct.setTimeSlotNo(ts);
        channelnostruct.setChannelType(TRAFFRN.Enums.ChannelType.PDCH);
        rFChannelRelease.setChannelNoStruct(channelnostruct);

        // Send rFChannelRelease
        try {
            setTestDebug("RFChannelRelease: " + rFChannelRelease.toString());
            rFChannelRelease.send();
        } catch (UNACKNOWLEDGED_MESSAGE_STOREDException ums) {
            setTestInfo("Catched UNACKNOWLEDGED_MESSAGE_STOREDException");
        }

        DISPATCH dispatch = abisHelper.getDISPATCH();
        DISPATCH.SendStoredMessages sendStoredMessages = dispatch.createSendStoredMessages();
        sendStoredMessages.setIssueUNACK(DISPATCH.Enums.IssueUNACK.No);
        sendStoredMessages.sendAsync();
    }

    /**
     * @name openPsChannel
     * @description Send GsmphMPH_PS_CHN_OPEN_REQ
     * @param ts1 - main timesloth
     * @param ts2 - secondary timeslot, applicable only with RTTI (RTTI is not supported in 16B)
     * @return Channel combination identifier
     */
    private long openPsChannel(short ts1, short ts2)
    {
        setTestDebug("mssimCell: " + mssimCell);
        GsmphMPH_PS_CHN_OPEN_REQ openReq = GsmbFactory.getGsmphMPH_PS_CHN_OPEN_REQBuilder(
                mssimCell,
                FrStructureType.ARFCN)
                .ts1(ts1)
                .ts2(ts2)
                .frArfcn(arfcn)
                .tsc((short) 1)
                .chnComb(ChnComb.GSMPH_PDTXBTTI) // use BTTI in this test case
                .build();
        setTestDebug("GsmphMPH_PS_CHN_OPEN_REQ: " + openReq.stringRepresentation());

        Confirmation confirmation = gsmb.send(openReq);
        setTestDebug("confirmation: " + confirmation.stringRepresentation());

        assertEquals("GsmphMPH_PS_CHN_OPEN_REQ failed", PhErrorType.GSM_PH_ENOERR, confirmation.getErrorType());
        long ccId = openReq.getDetailedConfirmation(confirmation).getCcId();
        setTestDebug("GsmphMPH_PS_CHN_OPEN_CNF, ccId: " + ccId);
        return ccId;
    }

    /**
     * @name closeChannel
     * @description Send GsmphMPH_CHN_CLOSE_REQ
     * @param ccId - channel combination identifier
     */
    private void closeChannel(long ccId)
    {
        GsmphMPH_CHN_CLOSE_REQ closeReq = GsmbFactory.getGsmphMPH_CHN_CLOSE_REQBuilder(ccId).build();
        Confirmation confirmation = gsmb.send(closeReq);
        setTestDebug("confirmation: " + confirmation.stringRepresentation());
        assertEquals("GsmphMPH_CHN_CLOSE_REQ failed", PhErrorType.GSM_PH_ENOERR, confirmation.getErrorType());
    }

    /**
     * @description Load pattern configuration file.
     *              Precondition: patter configuration file must exists on LSU in MSSIM
     * @name loadPatternConfigFile
     * @param fileName - path and name of PS pattern configuration file
     */
    private void loadPatternConfigFile(String fileName)
    {
        System.out.print("CfnfileName:fileName");
        System.out.println(fileName);

        GsmbSrvTEST_FILE_CMD cmd = GsmbFactory.getGsmbSrvTEST_FILE_CMDBuilder(
                PatternConfigurationFileType.PS,
                fileName
                ).build();

        setTestDebug("GsmbSrvTEST_FILE_CMD: " + cmd.stringRepresentation());

        Response response = gsmb.send(cmd);
        setTestDebug("response: " + response.stringRepresentation());
        assertTrue("GsmbSrvTEST_FILE_CMD failed", response.isSuccess());
    }

    /**
     * @name startPRBS
     * @description Send GsmappPRBS_START_CMD
     * @param ccId - channel combination identifier
     * @param txFName - path and name of pattern configuration file
     */
    private void startPRBS(long ccId, String txFName)
    {
        GsmappPRBS_START_CMD cmd = GsmbFactory.getGsmappPRBS_START_CMDBuilder(
                ccId,
                txFName)
                .txOneShot(false)
                .txId((short) 1)
                .txMode_CMDBulider(PRBSMode.GSMAPP_PRBS_9)
                .rxId((short) 1)
                .rxMode(PRBSMode.GSMAPP_PRBS_9)
                .build();

        setTestDebug("GsmappPRBS_START_CMD: " + cmd.stringRepresentation());
        Response response = gsmb.send(cmd);
        setTestDebug("response: " + response.stringRepresentation());
        assertTrue("GsmappPRBS_START_CMD failed", response.isSuccess());
    }

    //used when verifying received packets...
    private PAYLOAD.Enums.Encoding convertToCodingScheme(long cs)
    {
        int codingScheme;
        if (cs > 99)
            return PAYLOAD.Enums.Encoding.OUT_OF_BOUNDS;
        codingScheme = (int) cs;
        switch (codingScheme)
        {
        case 1:
            return PAYLOAD.Enums.Encoding.CS_1;
        case 2:
            return PAYLOAD.Enums.Encoding.CS_2;
        case 3:
            return PAYLOAD.Enums.Encoding.CS_3;
        case 4:
            return PAYLOAD.Enums.Encoding.CS_4;
        case 5:
            return PAYLOAD.Enums.Encoding.MCS_1;
        case 6:
            return PAYLOAD.Enums.Encoding.MCS_2;
        case 7:
            return PAYLOAD.Enums.Encoding.MCS_3;
        case 8:
            return PAYLOAD.Enums.Encoding.MCS_4;
        case 9:
            return PAYLOAD.Enums.Encoding.MCS_5;
        case 10:
            return PAYLOAD.Enums.Encoding.MCS_6;
        case 11:
            return PAYLOAD.Enums.Encoding.MCS_7;
        case 12:
            return PAYLOAD.Enums.Encoding.MCS_8;
        case 13:
            return PAYLOAD.Enums.Encoding.MCS_9;

        default:
            return PAYLOAD.Enums.Encoding.OUT_OF_BOUNDS;
        }
    }

    // Verify contents of received blocks 
    private void verifyReceivedPackets(GsmappPRBS_STOP_ACK ack, long expectedCcId,
            PAYLOAD.Enums.Encoding expectedCodingScheme)
    {   
    	final int SEQ_NO_SYNCHRONIZED = 1;		// Sequence number synchronized
        final long MIN_NO_EXPECTED_PKT = 1500; 	// Min number of packets with expected coding scheme
        final long MAX_NO_UNEXPECTED_PKT = 2; 	// Max number of packets with wrong coding scheme 
        final int MAX_NO_ERR_PKT         = 2;   // Max number of erroneous packets
        final int MAX_NO_HDR_ERR_PKT     = 5;	// Max number of blocks with checksum errored
       
        ///////////////////////////////////////////
        // Verify general stuff 
        assertEquals("CcId", expectedCcId, ack.getCcId());
        assertEquals("ChnType", ChannelType.PS_TYPE, ack.getChnType());

        ///////////////////////////////////////////
        // Verify: TRANSMISSION STATE
        assertEquals("NotTx", 0, ack.getNotTx());
        assertEquals("InvMs", 0, ack.getInvMs());
        assertEquals("TxNotFilled", 0, ack.getTxNotFilled());
        
        ///////////////////////////////////////////
        // Verify: RECEPTION STATE
        long rxHdrChkErr = ack.getRxHdrChkErr();
        assertTrue("RxHdrChkErr (counter for blocks with checksum errored) exceeds limit, received: " +
        		rxHdrChkErr + " (should be <= " + " " + MAX_NO_HDR_ERR_PKT + ")",
        		(rxHdrChkErr <= MAX_NO_HDR_ERR_PKT));
        assertEquals("RxHdrIdErr", 0, ack.getRxHdrIdErr());

        // Verify: Sequence number checking
        assertEquals("RxSeqSync", SEQ_NO_SYNCHRONIZED, ack.getRxSeqSync());        
        assertEquals("RxSeqLossCnt", 0, ack.getRxSeqLossCnt());
        
        // Verify: D-bits PRBS checking
        assertEquals("RxFrmErr", 0, ack.getRxFrmErr());
        assertEquals("RxBitErr", 0, ack.getRxBitErr());
        assertEquals("RxSlips", 0, ack.getRxSlips());

        ///////////////////////////////////////////
        // Verify unhandled packets due to CRC error:
        PS ps = ack.getPS();
        assertEquals("RxErrHdrUsf", 0, ps.getRxErrHdrUsf());
        assertEquals("RxErrPan", 0, ps.getRxErrPan());
        
        long rxErrTot = ps.getRxErrTot();
        assertTrue("RxErrTot(total number of packets with error) exceeds limit, received: " + 
        rxErrTot + " (limit is <= " + MAX_NO_ERR_PKT + ")", (rxErrTot <= MAX_NO_ERR_PKT)); 

        long rxErrData = ps.getRxErrData();
        assertTrue("RxErrData(number of packets with error in data blocks) exceeds limit, received: " + 
        rxErrData + " (limit is <= " + MAX_NO_ERR_PKT + ")", (rxErrData <= MAX_NO_ERR_PKT)); 

        // Verify list of received/sent packets
        List<PsCnt> psCntList = ps.getPsCnt();
        boolean foundPacketsWithRightCodingScheme = false;
        long cs;
        long noRxPkt;
        long noTxPkt;
        for (int index = 0; index < psCntList.size(); index++)
        {
            cs = psCntList.get(index).getCs();
            noRxPkt = psCntList.get(index).getRxPkt();
            noTxPkt = psCntList.get(index).getTxPkt();

            if (expectedCodingScheme == convertToCodingScheme(cs))
            {
                // Verify packets with expected coding scheme
                setTestDebug("Found packets with expected coding scheme, cs: " + cs + ", noRxPkt: " + noRxPkt
                        + ", noTxPkt: " + noTxPkt);
                foundPacketsWithRightCodingScheme = true;
                assertTrue("Too few packets with expected coding scheme transmitted on the PS channel, transmitted: " + 
                		noTxPkt + " (expect at least: " + MIN_NO_EXPECTED_PKT + ")", 
                		(noTxPkt >= MIN_NO_EXPECTED_PKT)); 

                assertTrue("Too few packets with expected coding scheme received on the PS channel, received: " + 
                		noRxPkt + " (expect at least: " + MIN_NO_EXPECTED_PKT + ")", 
                		(noRxPkt >= MIN_NO_EXPECTED_PKT)); 
            }
            else
            {
                // verify that packets with wrong coding scheme doesn't exceed limit
                assertEquals("TxPkt", 0, noTxPkt);                
                assertTrue("RxPkt(number of packets received on the PS channel) with wrong coding scheme, exceeds limit, received: " +
                		noRxPkt + " (limit is <= " + MAX_NO_UNEXPECTED_PKT + ")", 
                		(noRxPkt <= MAX_NO_UNEXPECTED_PKT));
            }
        }
        assertTrue("Did not find any packets with expected coding scheme: " + expectedCodingScheme,
                foundPacketsWithRightCodingScheme);
    }

    /**
     * @name stopPRBS
     * @description Send GsmappPRBS_STOP_CMD
     * @param ccId - channel combination identifier
     */
    private void stopPRBS(long ccId, PAYLOAD.Enums.Encoding codingScheme)
    {

        GsmappPRBS_STOP_CMD cmd = GsmbFactory.getGsmappPRBS_STOP_CMDBuilder(ccId).build();
        setTestDebug("GsmappPRBS_STOP_CMD: " + cmd.stringRepresentation());
        Response response = gsmb.send(cmd);
        setTestDebug("response: " + response.stringRepresentation());
        assertTrue("GsmappPRBS_STOP_CMD failed", response.isSuccess());

        //GsmappPRBS_STOP_ACK ack = (GsmappPRBS_STOP_ACK) response;
        //verifyReceivedPackets(ack, ccId, codingScheme);
    }
}
