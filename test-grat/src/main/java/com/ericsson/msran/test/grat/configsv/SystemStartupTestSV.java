package com.ericsson.msran.test.grat.configsv;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import se.ericsson.jcat.fw.annotations.Setup;
import se.ericsson.jcat.fw.annotations.Teardown;

import com.ericsson.abisco.clientlib.servers.CMDHLIB.CompleteStartRejectException;
import com.ericsson.abisco.clientlib.servers.LTS.Report;
import com.ericsson.abisco.clientlib.servers.OM_G31R01;
import com.ericsson.abisco.clientlib.servers.OM_G31R01.Enums;
import com.ericsson.abisco.clientlib.servers.OM_G31R01.FSOffset;
import com.ericsson.commonlibrary.ecimcom.exception.ComCliException;
import com.ericsson.commonlibrary.ecimcom.netconf.NetconfManagedObjectHandler;
import com.ericsson.commonlibrary.managedobjects.ManagedObjectStructAttribute;
import com.ericsson.commonlibrary.moshell.Moshell;
import com.ericsson.commonlibrary.remotecli.Cli;
import com.ericsson.commonlibrary.resourcemanager.ResourceConfigurationException;
import com.ericsson.commonlibrary.resourcemanager.Rm;
import com.ericsson.commonlibrary.resourcemanager.g2.G2Cell;
import com.ericsson.commonlibrary.resourcemanager.g2.G2RadioUnit;
import com.ericsson.commonlibrary.resourcemanager.g2.G2Rbs;
import com.ericsson.commonlibrary.resourcemanager.g2.G2RestartRank;
import com.ericsson.commonlibrary.resourcemanager.g2.logs.G2AvliLogRecord;
import com.ericsson.commonlibrary.resourcemanager.g2.logs.G2AvliNodeEventRecord;
import com.ericsson.commonlibrary.resourcemanager.g2.logs.G2AvliServiceState;
import com.ericsson.commonlibrary.resourcemanager.upgrade.G2UpgradePackage;
import com.ericsson.msran.configuration.MsranJcatException;
import com.ericsson.msran.g2.annotations.TestInfo;
import com.ericsson.msran.helpers.Helpers;
import com.ericsson.msran.helpers.log.G2RbsAvailabilityLogHelper;
import com.ericsson.msran.helpers.upgradepackage.G2RbsUpgradePackageHelper;
import com.ericsson.msran.helpers.upgradepackage.G2UpgradeHelper;
import com.ericsson.msran.helpers.upgradepackage.UpgradePackageHelperFactory;
import com.ericsson.msran.helpers.util.ZipHelper;
import com.ericsson.msran.jcat.TestBase;
import com.ericsson.msran.test.grat.testhelpers.AbisHelper;
import com.ericsson.msran.test.grat.testhelpers.AbiscoConnection;
import com.ericsson.msran.test.grat.testhelpers.MomHelper;
import com.ericsson.msran.test.grat.testhelpers.NodeStatusHelper;
import com.ericsson.msran.test.grat.testhelpers.TgLdns;
import com.ericsson.msran.test.grat.testhelpers.prepost.AbisPrePost;
import com.ericsson.msran.test.grat.testhelpers.prepost.PidPrePost;

/**
 * @name SystemStartupTestSV
 * @author GRAT 2015
 * @created
 * @description This test class connect Abisco Link and set up multiple Trx and restart RU
 * @revision eyyelli 2015-11-10
 */

public class SystemStartupTestSV extends TestBase {

    private final OM_G31R01.Enums.MOClass moClassTx = OM_G31R01.Enums.MOClass.TX;
    private final OM_G31R01.Enums.MOClass moClassRx = OM_G31R01.Enums.MOClass.RX;
    private final OM_G31R01.Enums.MOClass moClassTs = OM_G31R01.Enums.MOClass.TS;

    private PidPrePost pidPrePost;
    private MomHelper momHelper;
    private AbiscoConnection abisco;
    private NodeStatusHelper nodeStatus;
    private AbisHelper abisHelper;
    private String abisIp_ldn;
    private String sector_ldn;
    private static String BTS_FUNCTION_LDN = "ManagedElement=1,BtsFunction=1";
    private static String SECTOR_EQUIP_FUNC_LDN = "ManagedElement=1,NodeSupport=1,SectorEquipmentFunction=";
    private String sectorEquipmentFunctionLdnToUse;
    private AbisPrePost abisPrePost;
    private Component frame;
    private List<Integer> trxcList = new ArrayList<Integer>(0);
    private List<String> trxcLdnList = new ArrayList<String>(0);
    private List<Integer> bcchTrxList = new ArrayList<Integer>(0);
    private boolean bcchFlag = false;
    private String connectionName = "host_";
    private OM_G31R01.StatusResponse statusRsp;
    private G2Rbs rbs;
    private G2UpgradePackage gratRbsUpgradePackage;
    private UpgradePackageHelperFactory upgradePackageHelperFactory;
    private boolean isRunManually;
    private int cellNumber;
    private int maxPagingAttempts;
    private String callSetupRate;
    private String smsSetupRate;
    private String callDuration;
    private String percentageFR;
    private String percentageEFR;
    private int loadTestDuration;
    private String ccchLoadPagingRate;
    private String ccchLoadLRSAssRate;
    private String ccchLoadLRSAssRejRate;
    private String ccchLoadPSPagingRatio;
    private Report report = null;
    private Cli coli;
    private G2RbsAvailabilityLogHelper avliLogHelper;
    private List<G2AvliLogRecord> avliBeforeRestart = null;
    private List<TgLdns> tgLdnsList;
    private G2RestartRank rank; 

    /**
     * Description of test case for test reporting
     */
    @TestInfo(tcId = "LTE4711", slogan = "System Startup Action test case", requirementDocument = "1/xyz", requirementRevision = "PC5", requirementLinkTested = "..", requirementLinkLatest = "", requirementIds = {
            "PM 17" }, verificationStatement = "System Startup Action test case", testDescription = "Runs System Startup Action", traceGuidelines = "N/A")
    /**
     * Precond.
     */
    @Setup
    public void setup() {
        setTestStepBegin("setup");
        nodeStatus = new NodeStatusHelper();
        assertTrue("Node did not reach a working state before System Startup action",
                nodeStatus.waitForNodeReady());
        abisHelper = new AbisHelper();
        pidPrePost = new PidPrePost();
        momHelper = new MomHelper();
        abisco = new AbiscoConnection();
        pidPrePost.preCond();
        abisPrePost = new AbisPrePost();
        rbs = Rm.getG2RbsList().get(0);
        gratRbsUpgradePackage = rbs.getUpgradePackageOnFtp(rbs.getName());
        upgradePackageHelperFactory = new UpgradePackageHelperFactory();
        coli = rbs.getCsColi();
        avliLogHelper = Helpers.log().availabilityLogHelper(rbs);
        setTestStepEnd();
    }

    /**
     * Postcond.
     */
    @Teardown
    public void teardown() {
        setTestStepBegin("teardown");
        nodeStatus.isNodeRunning();
        pidPrePost.postCond();
        setTestStepEnd();
    }

    /**
     * @name lockUnlockRadiounitTest
     * @description This test class connect Abisco Link and set up multiple Trx and lock/unlock MO
     *              related to GRAT operablity.Lock/unlock radio with traffic 10 times
     * @param testId
     *        - unique identifier
     * @param description
     * @param numOfTGs
     * @param numOfTrxesPerTG
     * @param isRunManually - if true the test case will display pop ups to continue test case.
     *        Default false.
     * @param callSetupRate - The number of CS calls per second * 1000, e.g. CPR=5 ->
     *        callSetupRate=5000. Default 5000.
     * @param smsSetupRate - The number of SMS sent per second * 1000, e.g. SPR=1.1 ->
     *        smsSetupRate=1100. Default 1100.
     * @param callDuration - The call duration in seconds of each call. Default 5 s.
     * @param percentageFR - Percentage of total number of calls to be FR. Default 10%.
     * @param percentageEFR - Percentage of total number of calls to be EFR. Default 90%.
     * @param loadTestDuration - if isRunManually is false, this is the time in seconds the load
     *        test will be executed. Default 30 minutes.
     * @param maxPagingAttempts
     * @param ccchLoadPagingRate
     * @param ccchLoadLRSAssRate
     * @param ccchLoadLRSAssRejRate
     * @param ccchLoadPSPagingRatio
     * @param hoppingIndicator
     * @param loopNum
     */
    @Test(timeOut = 30000000) // approx 8.x h
    @Parameters({ "testId", "description", "numOfTGs", "numOfTrxesPerTG", "isRunManually",
            "callSetupRate", "smsSetupRate", "callDuration", "percentageFR", "percentageEFR", "loadTestDuration",
            "maxPagingAttempts", "ccchLoadPagingRate", "ccchLoadLRSAssRate", "ccchLoadLRSAssRejRate",
            "ccchLoadPSPagingRatio", "loopNum" })
    public void restartRUTest(String testId, String description, final int numOfTGs,
            final int numOfTrxesPerTG,
            @Optional("false") boolean isRunManually, @Optional("5000") String callSetupRate,
            @Optional("1100") String smsSetupRate,
            @Optional("5") String callDuration, @Optional("10") String percentageFR,
            @Optional("90") String percentageEFR,
            @Optional("1800") int loadTestDuration, @Optional("3") int maxPagingAttempts,
            @Optional("26400") String ccchLoadPagingRate, @Optional("3000") String ccchLoadLRSAssRate,
            @Optional("3000") String ccchLoadLRSAssRejRate,
            @Optional("50") String ccchLoadPSPagingRatio, @Optional("10") int loopNum) {

        setTestCase(testId, description);

        this.isRunManually = isRunManually;
        this.maxPagingAttempts = maxPagingAttempts;
        this.callSetupRate = callSetupRate;
        this.smsSetupRate = smsSetupRate;
        this.callDuration = callDuration;
        this.percentageFR = percentageFR;
        this.percentageEFR = percentageEFR;
        this.loadTestDuration = loadTestDuration;
        this.ccchLoadPagingRate = ccchLoadPagingRate;
        this.ccchLoadLRSAssRate = ccchLoadLRSAssRate;
        this.ccchLoadLRSAssRejRate = ccchLoadLRSAssRejRate;
        this.ccchLoadPSPagingRatio = ccchLoadPSPagingRatio;

        setTestCase(testId, description);

        // Test variables
        tgLdnsList = momHelper.createUnlockAllGratMos(numOfTGs, numOfTrxesPerTG, true);

        setTestStepBegin("Setup Abisco for " + numOfTGs + " TGs");
//        abisco.setupAbisco(numOfTGs, numOfTrxesPerTG, false);
         abisco.setupAbisco(numOfTGs, numOfTrxesPerTG, false,"Off");
        setTestStepEnd();

        tgLdnsList = momHelper.findGratMos();

        // Unlock and enable everything
        completeStartup(numOfTGs, numOfTrxesPerTG);


        setTestStepBegin("Print out LTS report Before RU restart.");
        printLTSReportBeforeMOMAction(report);
        setTestStepEnd();
        for (int loop = 1; loop <= loopNum; loop++) {

            setTestStepBegin("Starting to Perform Restart Radio unit");
            for (G2RadioUnit gsmRadio : rbs.getRadioUnits()) {
                setTestInfo("Start to restart "+gsmRadio.getManagedObjectLdn());
                    gsmRadio.restart(G2RestartRank.COLD);
                    try {
                        setTestInfo("Sleeping for 60 seconds.");
                        Thread.sleep(60 * 1000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        setTestWarning("Sleep got interrupted.");
                    }
            }
            setTestStepEnd();
            completeStartup(numOfTGs, numOfTrxesPerTG);
            
            setTestStepBegin("Print out LTS report after unlock");
            printLTSReportAfterlockUnlockAction(report, loop, numOfTGs);
            setTestStepEnd();

        }
    }

    private void printLTSReportBeforeMOMAction(Report report) {

        if (report != null) {
            // Calculate some statistics
            int totalNumberOfSuccessfulCalls = (int) (report.getFRCallSuccTot() + report.getEFRCallSuccTot());
            int totalNumberOfFailedCalls = (int) (report.getFRCallFailsTot() + report.getEFRCallFailsTot());
            int totalNumberOfCalls = (int) (report.getFRCallsTot() + report.getEFRCallsTot());
            int timeInSeconds = (int) (report.getTotalTime() / 100) + 1; // At least on second 
            double csr = totalNumberOfSuccessfulCalls / (double) timeInSeconds;
            double cfr = totalNumberOfFailedCalls / (double) timeInSeconds;
            double ssr = report.getSMSSuccTot() / (double) timeInSeconds;
            double sfr = report.getSMSFailsTot() / (double) timeInSeconds;

            // Write summary info report
            setAdditionalResultInfo("<br><b>GRAT Stability load test summary Before System Startup Action:</b>");
            setAdditionalResultInfo("Test duration: " + timeInSeconds + " s");
            for (int tgId = 0; tgId < cellNumber; tgId++) {
                setAdditionalResultInfo("tgId = " + tgId + " :Call Setup Rate: "
                        + Integer.parseInt(callSetupRate.split(",")[tgId]) / (double) 1000 + " call/s");
                setAdditionalResultInfo("tgId = " + tgId + " :SMS Setup Rate: "
                        + Integer.parseInt(smsSetupRate.split(",")[tgId]) / (double) 1000 + " sms/s<br>");
            }
            setAdditionalResultInfo("Call Success Rate (CSR): " + csr);
            setAdditionalResultInfo("Call Failure Rate (CFR): " + cfr);
            setAdditionalResultInfo("SMS Success Rate  (SSR): " + ssr);
            setAdditionalResultInfo("SMS Failure Rate  (SFR): " + sfr);

            setAdditionalResultInfo("<b>GRAT Stability load test details Before System Startup Action:</b>");
            setAdditionalResultInfo("########### FR&EFR calls ###########");
            setAdditionalResultInfo("Successful FR calls: " + (int) report.getFRCallSuccTot());
            setAdditionalResultInfo("Successful EFR calls: " + (int) report.getEFRCallSuccTot());
            setAdditionalResultInfo("Successful total calls: " + totalNumberOfSuccessfulCalls);
            setAdditionalResultInfo("Failed FR calls: " + (int) report.getFRCallFailsTot());
            setAdditionalResultInfo("Failed EFR calls: " + (int) report.getEFRCallFailsTot());
            setAdditionalResultInfo("Failed total calls: " + totalNumberOfFailedCalls);
            setAdditionalResultInfo("Total calls: " + totalNumberOfCalls);
            setAdditionalResultInfo("Successful Ratio is: "
                    + (((double) totalNumberOfSuccessfulCalls) / totalNumberOfCalls) * 100 + "%");

            setAdditionalResultInfo("########### SMS ###########");
            setAdditionalResultInfo("Successful sent sms: " + (int) report.getSMSSuccTot());
            setAdditionalResultInfo("Failed sms: " + (int) report.getSMSFailsTot());
            setAdditionalResultInfo("Total sms: " + (int) (report.getSMSesTot()));
            setAdditionalResultInfo("Successful Ratio is: "
                    + (((double) report.getSMSSuccTot()) / ((int) report.getSMSesTot())) * 100 + "%");

            setAdditionalResultInfo("########### CCCHLoad ###########");
            setAdditionalResultInfo("Successful LRSAss: " + (int) report.getLRSAssigns());
            setAdditionalResultInfo("Total LRSAss: " + (int) (report.getLRSAssignsTot()));
            setAdditionalResultInfo("Successful LRSAss Ratio is: "
                    + ((double) report.getLRSAssigns() / (int) report.getLRSAssignsTot()) * 100 + "%");
            setAdditionalResultInfo("Successful LRSAssRej: " + (int) report.getLRSAssRejs());
            setAdditionalResultInfo("Total LRSAssRej: " + (int) (report.getLRSAssRejsTot()));
            setAdditionalResultInfo("Successful LRSAssRej Ratio is: "
                    + ((double) report.getLRSAssRejs() / (int) report.getLRSAssRejsTot()) * 100 + "%");
            setAdditionalResultInfo("Successful Paging: " + (int) report.getPagings());
            setAdditionalResultInfo("Total Paging: " + (int) (report.getPagingsTot()));
            setAdditionalResultInfo("Successful Paging Ratio is: "
                    + ((double) report.getPagings() / (int) report.getPagingsTot()) * 100 + "%");
            setAdditionalResultInfo("###############################");

            setAdditionalResultInfo("TCH allocation ratio: " + report.getTCHAllocRatioTot() + " %");
            setAdditionalResultInfo("SDCCH allocation ratio: " + report.getSDCCHAllocRatioTot() + " %");
            setAdditionalResultInfo("Total pagings: " + (int) report.getPagingsTot());
            setAdditionalResultInfo("Total channel requests: " + (int) report.getChannelReqTot());
        } else {
            // Write summary info report
            setAdditionalResultInfo("<b>GRAT Stability load test summary Before System Startup Action:</b>");
            setAdditionalResultInfo("Call Success Rate (CSR): Failed test");
            setAdditionalResultInfo("Call Failure Rate (CFR): Failed test");
            setAdditionalResultInfo("SMS Success Rate  (SSR): Failed test");
            setAdditionalResultInfo("SMS Failure Rate  (SFR): Failed test");
        }
    }

    private void printLTSReportAfterlockUnlockAction(Report report, int loop, int numOfTGs) {

        if (report != null) {
            // Calculate some statistics
            int totalNumberOfSuccessfulCalls = (int) (report.getFRCallSuccTot() + report.getEFRCallSuccTot());
            int totalNumberOfFailedCalls = (int) (report.getFRCallFailsTot() + report.getEFRCallFailsTot());
            int totalNumberOfCalls = (int) (report.getFRCallsTot() + report.getEFRCallsTot());
            int timeInSeconds = (int) (report.getTotalTime() / 100) + 1; // At least on second 
            double csr = totalNumberOfSuccessfulCalls / (double) timeInSeconds;
            double cfr = totalNumberOfFailedCalls / (double) timeInSeconds;
            double ssr = report.getSMSSuccTot() / (double) timeInSeconds;
            double sfr = report.getSMSFailsTot() / (double) timeInSeconds;

            // Write summary info report
            setAdditionalResultInfo("<br><b>GRAT Stability load test summary after System Startup Action :</b>" + loop + " <b>times</b>");
            setAdditionalResultInfo("Test duration: " + timeInSeconds + " s");
            for(int tgId = 0; tgId < numOfTGs; tgId ++) {
                setAdditionalResultInfo("tgId = " + tgId +" :Call Setup Rate: " + Integer.parseInt(callSetupRate.split(",")[tgId]) / (double)1000 + " call/s");
                setAdditionalResultInfo("tgId = " + tgId +" :SMS Setup Rate: " + Integer.parseInt(smsSetupRate.split(",")[tgId]) / (double)1000 + " sms/s<br>");
            }
            setAdditionalResultInfo("Call Success Rate (CSR): " + csr);
            setAdditionalResultInfo("Call Failure Rate (CFR): " + cfr);
            setAdditionalResultInfo("SMS Success Rate  (SSR): " + ssr);
            setAdditionalResultInfo("SMS Failure Rate  (SFR): " + sfr);

            setTestInfo("<b>GRAT Stability load test details after System Startup Action :</b>" + loop + " <b>times</b>");
            setTestInfo("########### FR&EFR calls ###########");
            setTestInfo("Successful FR calls: " + (int) report.getFRCallSuccTot());
            setTestInfo("Successful EFR calls: " + (int) report.getEFRCallSuccTot());
            setTestInfo("Successful total calls: " + totalNumberOfSuccessfulCalls);
            setTestInfo("Failed FR calls: " + (int) report.getFRCallFailsTot());
            setTestInfo("Failed EFR calls: " + (int) report.getEFRCallFailsTot());
            setTestInfo("Failed total calls: " + totalNumberOfFailedCalls);
            setTestInfo("Total calls: " + totalNumberOfCalls);

            setAdditionalResultInfo("Successful FR&EFR Ratio is: "
                    + (((double) totalNumberOfSuccessfulCalls) / totalNumberOfCalls) * 100 + "%");

            setTestInfo("########### SMS ###########");
            setTestInfo("Successful sent sms: " + (int) report.getSMSSuccTot());
            setTestInfo("Failed sms: " + (int) report.getSMSFailsTot());
            setTestInfo("Total sms: " + (int) (report.getSMSesTot()));
            setAdditionalResultInfo("Successful SMS Ratio is: "
                    + (((double) report.getSMSSuccTot()) / ((int) report.getSMSesTot())) * 100 + "%");

            setTestInfo("########### CCCHLoad ###########");
            setTestInfo("Successful LRSAss: " + (int) report.getLRSAssigns());
            setTestInfo("Total LRSAss: " + (int) (report.getLRSAssignsTot()));
            setAdditionalResultInfo("Successful LRSAss Ratio is: "
                    + ((double) report.getLRSAssigns() / (int) report.getLRSAssignsTot()) * 100 + "%");
            setTestInfo("Successful LRSAssRej: " + (int) report.getLRSAssRejs());
            setTestInfo("Total LRSAssRej: " + (int) (report.getLRSAssRejsTot()));
            setAdditionalResultInfo("Successful LRSAssRej Ratio is: "
                    + ((double) report.getLRSAssRejs() / (int) report.getLRSAssRejsTot()) * 100 + "%");
            setTestInfo("Successful Paging: " + (int) report.getPagings());
            setTestInfo("Total Paging: " + (int) (report.getPagingsTot()));
            setAdditionalResultInfo("Successful Paging Ratio is: "
                    + ((double) report.getPagings() / (int) report.getPagingsTot()) * 100 + "%");
            setAdditionalResultInfo("###############################");

            setTestInfo("TCH allocation ratio: " + report.getTCHAllocRatioTot() + " %");
            setTestInfo("SDCCH allocation ratio: " + report.getSDCCHAllocRatioTot() + " %");
            setTestInfo("Total pagings: " + (int) report.getPagingsTot());
            setTestInfo("Total channel requests: " + (int) report.getChannelReqTot());
        } else {
            // Write summary info report
            setAdditionalResultInfo(
                    "<b>GRAT Stability load test summary after System Startup Action :</b>" + loop + " <b>times</b>");
            setAdditionalResultInfo("Call Success Rate (CSR): Failed test");
            setAdditionalResultInfo("Call Failure Rate (CFR): Failed test");
            setAdditionalResultInfo("SMS Success Rate  (SSR): Failed test");
            setAdditionalResultInfo("SMS Failure Rate  (SFR): Failed test");
        }
    }

    private void stopLTS() {
        if (isRunManually) {
            JOptionPane.showMessageDialog(frame, "Press ok to stop LTS.");
            // TODO: efillar (2015-09-30) - Fix this, should be a parameter to test case instead.
        } else {
            sleepSeconds(loadTestDuration);
        }

        // Report is used to get the load test statistics from the LTS after stopping the test.
        try {
            abisco.stopLTS();
            report = abisco.getReportLTS(0);
        } catch (InterruptedException e) {
            // Did not work to reset the LTS.
            e.printStackTrace();
        }

    }

    /**
     * Fetches the correct restart complete event record
     */
    private List<G2AvliNodeEventRecord> findRestartCompleteEntries(List<G2AvliLogRecord> avliLogsForRestart,
            String applicationTag, String upEvent) {
        List<G2AvliNodeEventRecord> foundEntries = new ArrayList<G2AvliNodeEventRecord>();

        if (applicationTag == "Rcs") {
            for (G2AvliLogRecord thisEntry : avliLogsForRestart) {
                if (thisEntry instanceof G2AvliNodeEventRecord) {
                    G2AvliNodeEventRecord thisNodeEvent = (G2AvliNodeEventRecord) thisEntry;
                    if (thisNodeEvent.getServiceState() == G2AvliServiceState.IN_SERVICE
                            && thisNodeEvent.getEventReason() != null
                            && thisNodeEvent.getEventReason().matches(upEvent)
                            && thisNodeEvent.getAdditionalInfoChildName().matches(applicationTag)) {
                        foundEntries.add(thisNodeEvent);
                    }
                }
            }
        } else if (applicationTag == "Tn" || applicationTag == "Grat") {
            for (G2AvliLogRecord thisEntry : avliLogsForRestart) {
                if (thisEntry instanceof G2AvliNodeEventRecord) {
                    G2AvliNodeEventRecord thisNodeEvent = (G2AvliNodeEventRecord) thisEntry;
                    if (thisNodeEvent.getServiceState() == G2AvliServiceState.IN_SERVICE
                            && thisNodeEvent.getRestartCompleted()
                            && thisNodeEvent.getNodeInfo().matches(upEvent)
                            && thisNodeEvent.getNodeInfo() != null
                            && thisNodeEvent.getAdditionalInfoChildName().matches(applicationTag)) {
                        foundEntries.add(thisNodeEvent);
                    }
                }
            }
        }
        return foundEntries;
    }

    /**
     * Fetches the correct down event record
     */
    private List<G2AvliNodeEventRecord> findNodeDownEvents(List<G2AvliLogRecord> avliLogsForRestart, String downEvent) {
        List<G2AvliNodeEventRecord> foundEntries = new ArrayList<G2AvliNodeEventRecord>();
        for (G2AvliLogRecord thisEntry : avliLogsForRestart) {
            if (thisEntry instanceof G2AvliNodeEventRecord) {
                G2AvliNodeEventRecord thisNodeEvent = (G2AvliNodeEventRecord) thisEntry;
                if (thisNodeEvent.getServiceState() == G2AvliServiceState.OUT_OF_SERVICE
                        && thisNodeEvent.getEventReason() != null
                        && thisNodeEvent.getEventReason().matches(downEvent)
                        && thisNodeEvent.getRcsNodeDown()) {
                    foundEntries.add(thisNodeEvent);
                }
            }
        }

        return foundEntries;
    }

    private void completeStartup(int numOfTGs, int numOfTrxesPerTG) {
        for (int current_tg = 0; current_tg < numOfTGs; ++current_tg) {
            setTestInfo("Current TG = " + current_tg);
            TgLdns tg = tgLdnsList.get(current_tg);

            setTestStepBegin("Verify AbisIp MO states after unlock");
            assertEquals(tg.abisIpLdn + " did not reach correct state", "",
                    momHelper.checkAbisIpMoAttributeAfterUnlockBscConnected(tg.abisIpLdn, abisco.getBscIpAddress(),
                            30));
            setTestStepEnd();

            // This is where it happens
            setTestStepBegin("Enable all Abis MOs in TG " + current_tg);

            try {
                abisHelper.completeStartup(current_tg, numOfTrxesPerTG);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
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
        }
    }

    // Cleanup done by RestoreStack
}
