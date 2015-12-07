package com.ericsson.msran.test.grat.configsv;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import se.ericsson.jcat.fw.annotations.Setup;
import se.ericsson.jcat.fw.annotations.Teardown;

import com.ericsson.abisco.clientlib.servers.CHEXTRAS;
import com.ericsson.abisco.clientlib.servers.CMDHLIB.CompleteStartRejectException;
import com.ericsson.abisco.clientlib.servers.LTS.Report;
import com.ericsson.commonlibrary.ecimcom.exception.ComCliException;
import com.ericsson.commonlibrary.ecimcom.exception.GracefulDisconnectFailedException;
import com.ericsson.commonlibrary.ecimcom.netconf.NetconfManagedObjectHandler;
import com.ericsson.commonlibrary.managedobjects.ManagedObjectStructAttribute;
import com.ericsson.commonlibrary.moshell.Moshell;
import com.ericsson.commonlibrary.resourcemanager.Rm;
import com.ericsson.commonlibrary.resourcemanager.g2.G2Rbs;
import com.ericsson.commonlibrary.resourcemanager.upgrade.G2UpgradePackage;
import com.ericsson.msran.configuration.MsranJcatException;
import com.ericsson.msran.g2.annotations.TestInfo;
import com.ericsson.msran.helpers.Helpers;
import com.ericsson.msran.helpers.upgradepackage.G2RbsUpgradePackageHelper;
import com.ericsson.msran.helpers.upgradepackage.G2UpgradeHelper;
import com.ericsson.msran.helpers.upgradepackage.UpgradePackageHelperFactory;
import com.ericsson.msran.helpers.util.ZipHelper;
import com.ericsson.msran.test.grat.testhelpers.AbisHelper;
import com.ericsson.msran.test.grat.testhelpers.AbiscoConnection;
import com.ericsson.msran.test.grat.testhelpers.MomHelper;
import com.ericsson.msran.test.grat.testhelpers.NodeStatusHelper;
import com.ericsson.msran.test.grat.testhelpers.TgLdns;
import com.ericsson.msran.test.grat.testhelpers.prepost.PidPrePost;
import com.ericsson.msran.jcat.TestBase;

/**
 * @id NodeUC426,NodeUC428
 * 
 * @name CreateUnlockMultipleTrexes
 * 
 * @author GRAT Cell
 * 
 * @created 2015-11-04
 * 
 * @description This test will create GsmSectors with several Trxes.
 *              Everything will be configured and enabled with the complete startup
 *              command towards Abisco.
 *              Use LTS to start traffic, and download SW if needed.
 * 
 * @revision eyngjng 2015-11-04 Initial version
 */

public class MaximumLoadTrafficTestProductSV extends TestBase {
    
    private MomHelper momHelper;
    private PidPrePost pidPrePost;
    private AbiscoConnection abisco;
    private AbisHelper abisHelper;
    private NodeStatusHelper nodeStatus;
    private Component frame;
    private Moshell moshell;
    private MoshellScriptThread moshellThread;
    private static String RESET_MEASUREMENT_FILE = "/proj/gratci/system_verification/scripts/reset_measurements.mos";
    private static String MEASUREMENT_FILE = "/proj/gratci/system_verification/scripts/grat_measurements.mos";
    private static Integer IMSI_START = 1451; // Test manager IMSI numbers start on 1451 (as last four digits)
    private static Integer NUMBER_OF_MS = 180; // Total number of MS IMSI numbers configured in TM
    private long loadStartAt;
    private long loadEndAt;
    private G2Rbs rbs;
    private G2UpgradePackage gratRbsUpgradePackage;
    private UpgradePackageHelperFactory upgradePackageHelperFactory;
    private G2UpgradeHelper gratUpgradeHelper;
    private G2RbsUpgradePackageHelper gratRbsUpgradeHlper;
    private NetconfManagedObjectHandler moHandler;
    private final static String SWM_MO_LDN = "ManagedElement=1,SystemFunctions=1,SwM=1";
    private File upDestinationDir;
    private String upDestinationPath;
    private GetReportThread getRepeatThread;
    private static final String CONNECTION_NAME_PREFIX = "host_";

    
    /**
     * Description of test case for test reporting
     */
    @TestInfo(
            tcId = "UC513.N, UC602.N, UC607.N, UC627.N, UC646.N",
            slogan = "Complete startup of all MOs",
            requirementDocument = "1/00651-FCP 130 1402",
            requirementRevision = "PC5",
            requirementLinkTested = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff87c1d941?docno=1/00651-FCP1301402Uen&format=excel8book",
            requirementLinkLatest = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff86a2af3d?docno=1/00651-FCP1301402Uen&action=current&format=excel8book",
            requirementIds = { "10565-0334/19417[A][APPR]" },
            verificationStatement = "Verifies (not checking that every MO state is correct after every step) UC513.N, UC602.N, UC607.N, UC627.N, UC646.N",
            testDescription = "Verifies that all MOs can be created, unlocked, and enabled with the Abisco Complete Start command",
            traceGuidelines = "N/A")
    
    
    /**
     * Precheck.
     */
    @Setup
    public void setup() {
        setTestStepBegin("setup");
        nodeStatus = new NodeStatusHelper();
        assertTrue("Node did not reach a working state before test start", nodeStatus.waitForNodeReady());
        momHelper = new MomHelper();
        pidPrePost = new PidPrePost();
        abisco = new AbiscoConnection();
        abisHelper = new AbisHelper();
        rbs = Rm.getG2RbsList().get(0);
        moshell = rbs.getMoshell();
        // Moved to test case temporarily since upgradeServer resource not available in LI GIC
        //gratRbsUpgradePackage = rbs.getUpgradePackageOnFtp(rbs.getName());
        //upgradePackageHelperFactory = new UpgradePackageHelperFactory();
        //gratUpgradeHelper = upgradePackageHelperFactory.upgradeHelper(rbs, gratRbsUpgradePackage);
        //gratRbsUpgradeHlper = upgradePackageHelperFactory.upgradePackageHelper(rbs);
        moHandler = rbs.getManagedObjectHandler();
        setTestInfo("Save current pid to compare after test execution.");
        pidPrePost.preCond();
        setTestStepEnd();
    }
    
    /**
     * Postcheck.
     */
    @Teardown
    public void teardown() {
        setTestStepBegin("teardown");
        try {
              nodeStatus.isNodeRunning();
        }catch (GracefulDisconnectFailedException ie) {
            setTestInfo("Exception when close netconf,but finally disconnect"); 
        }catch (Exception e) {
            setTestInfo("Other exception: " + e);
            e.printStackTrace();
        }
        pidPrePost.postCond();
        setTestStepEnd();
    }
    
    /**
     * @name maximumLoadTrafficTest
     * 
     * @description Create, unlock and enable four Trxes on one sector.
     *              Use LTS to start traffic and get LTS report.
     *              Parameter "isSWdownloadEnable" default value is true, if tester re-set to false, will not download upgrade package.
     * 
     * @param testId - unique identifier of the test case
     * @param description
     * @param numOfTGs 
     * @param numOfTrxesPerTG 
     * @param isRunManually 
     * @param callSetupRate 
     * @param smsSetupRate 
     * @param callDuration 
     * @param percentageFR 
     * @param percentageEFR 
     * @param loadTestDuration 
     * @param resetMeasureFile 
     * @param measurementFile 
     * @param maxPagingAttempts 
     * @param ccchLoadPagingRate 
     * @param ccchLoadLRSAssRate 
     * @param ccchLoadLRSAssRejRate 
     * @param ccchLoadPSPagingRatio 
     * @param hoppingIndicator 
     * @param isSWdownloadEnable 
     * @throws InterruptedException
     */
    @Test(timeOut = 2000000000)
    @Parameters({ "testId", "description", "numOfTGs", "numOfTrxesPerTG", "isRunManually",
        "callSetupRate", "smsSetupRate", "callDuration", "percentageFR", "percentageEFR", 
        "loadTestDuration", "resetMeasureFile", "measurementFile", "maxPagingAttempts", 
        "ccchLoadPagingRate", "ccchLoadLRSAssRate", "ccchLoadLRSAssRejRate", 
        "ccchLoadPSPagingRatio", "hoppingIndicator", "isSWdownloadEnable","interval" })
    public void maximumLoadTrafficTest(String testId, String description, final int numOfTGs, final int numOfTrxesPerTG,
            @Optional("false") boolean isRunManually, @Optional("5000") String callSetupRate, @Optional("1100") String smsSetupRate, 
            @Optional("5") String callDuration, @Optional("10") String percentageFR, @Optional("90") String percentageEFR, 
            @Optional("1800") int loadTestDuration, @Optional("") String resetMeasureFile, @Optional("") String measurementFile,@Optional("3")int maxPagingAttempts, 
            @Optional("26400")String ccchLoadPagingRate, @Optional("3000")String ccchLoadLRSAssRate, @Optional("3000")String ccchLoadLRSAssRejRate, 
            @Optional("50")String ccchLoadPSPagingRatio, @Optional("Off")String hoppingIndicator, @Optional("true") Boolean isSWdownloadEnable, @Optional("900") int interval) throws InterruptedException {
        setTestCase(testId, description);
        
        // Set up upgrade server
        if (isSWdownloadEnable) {      	
            gratRbsUpgradePackage = rbs.getUpgradePackageOnFtp(rbs.getName());
            upgradePackageHelperFactory = new UpgradePackageHelperFactory();
            gratUpgradeHelper = upgradePackageHelperFactory.upgradeHelper(rbs, gratRbsUpgradePackage);
            gratRbsUpgradeHlper = upgradePackageHelperFactory.upgradePackageHelper(rbs);
        }
        
        // Test variables
        List<TgLdns> tgLdnsList = momHelper.createUnlockAllGratMos(numOfTGs, numOfTrxesPerTG, true);
        
        setTestStepBegin("Setup Abisco for " + numOfTGs + " TGs");
        abisco.setupAbisco(numOfTGs, numOfTrxesPerTG, false, hoppingIndicator);
        setTestStepEnd();
        
        tgLdnsList = momHelper.findGratMos();

        // Unlock and enable everything
        for (int current_tg = 0; current_tg < numOfTGs; ++current_tg) {
            setTestInfo("Current TG = " + current_tg);
            TgLdns tg = tgLdnsList.get(current_tg);
            
            setTestStepBegin("Verify AbisIp MO states after unlock");
            assertEquals(tg.abisIpLdn + " did not reach correct state", "", 
                    momHelper.checkAbisIpMoAttributeAfterUnlockBscConnected(tg.abisIpLdn, abisco.getBscIpAddress(), 30));
            setTestStepEnd();
            
            for (String trxLdn: tg.trxLdnList) {
            	setTestStepBegin("Verify Trx MO states after unlock for " + trxLdn);
                assertEquals(trxLdn + " did not reach correct state", "", 
                        momHelper.checkTrxMoAttributeAfterUnlockNoLinks(trxLdn, 20));
                setTestStepEnd();
            }
            
            // This is where it happens
            setTestStepBegin("Enable all Abis MOs in TG " + current_tg);
            try {
            	abisHelper.completeStartup(current_tg, numOfTrxesPerTG);
            }
            catch (CompleteStartRejectException e) {
            	fail("Complete startup of TG = " + current_tg + " caused CompleteStartRejectException: " + e.getMessage());
        	}
            
            setTestStepEnd();
            
            setTestStepBegin("Verify AbisIp MO after complete startup " + tg.abisIpLdn);
            assertEquals(tg.abisIpLdn + " did not reach correct state", "", 
                    momHelper.checkAbisIpMoAttributeAfterUnlockBscConnected(tg.abisIpLdn, abisco.getBscIpAddress(), 10));
            setTestStepEnd();
            
            setTestStepBegin("Verify GsmSector MO after complete startup " + tg.sectorLdn);
            assertEquals(tg.sectorLdn + " did not reach correct state", "", 
                    momHelper.checkGsmSectorMoAttributeAfterAllMosStartedAoAtAndAoTfEnabled(tg.sectorLdn, 10));
            setTestStepEnd();
            
            for (String trxLdn: tg.trxLdnList) {
            	setTestStepBegin("Verify Trx MO states after complete start up " + trxLdn);
                assertEquals(trxLdn + " did not reach correct state", "", 
                        momHelper.checkTrxMoAttributeAfterUnlockAllEnabledLinksEstablished(trxLdn, 10));
                setTestStepEnd();
            }
        }

       // Start the LTS and check measure load
        if (isRunManually) {
            JOptionPane.showMessageDialog(frame, "Press ok to start LTS."); 
        }
        else {
          // TODO: eyngjng (2015-10-16) - Will Add TM part instead of this once TM can be automatically controlled.
          sleepSeconds(60);
        }
        
        setTestStepBegin("Create measurement load file path");
        String reset_MeasureFile;
        String measurement_File;
        String dateTime = Helpers.util().timeHelper().getTimeStamp();
        String currentUP = "MSUPX";
        // TODO: Enable this again currently a arrayOutOfBoundsException is received due to wrong MOShell version / Rewrite?
        /*String cvcuResult = moshell.send("cvcu");
        String cvcuResultSplitByLine[] = cvcuResult.split("\n");
        for(int i=0 ; i<cvcuResultSplitByLine.length ; i++){
            if(cvcuResultSplitByLine[i].contains("Current SwVersion:")){
                currentUP = cvcuResultSplitByLine[i].split("_")[1].split("\\s+")[0];
            }
        }*/
        String latencyLogFileName = currentUP +"_" +dateTime;
        setTestInfo("latencyLogFileName is " + latencyLogFileName);
        try {
            Process p = Runtime.getRuntime().exec("mkdir -m 777 /proj/gratci/system_verification/testruns/"+ latencyLogFileName);
        } catch (IOException e1) {
            e1.printStackTrace();
            fail("*** Create directory failed ");
        }
        setTestStepEnd();
        
        if (!("").equals(resetMeasureFile))  {
             reset_MeasureFile = resetMeasureFile;
        }else {
             reset_MeasureFile = RESET_MEASUREMENT_FILE;
        }
         
        if (!("").equals(measurementFile))  {
            measurement_File = measurementFile;
       }else {
           measurement_File = MEASUREMENT_FILE;
       }
        
        try {   
            setTestStepBegin("Reset, configure and start LTS.");
            abisco.resetLTS();
            moshell.send("run " + reset_MeasureFile);
            sleepSeconds(3);
          
            // Configure LTS
            int numOfImsiPerTg = NUMBER_OF_MS / numOfTGs;  // Assume the MSes are evenly distributed
            for (int tgId = 0; tgId < numOfTGs; tgId ++) {
                  abisco.configureLTS(tgId, 0, 0, maxPagingAttempts, Integer.parseInt(callSetupRate.split(",")[tgId]), Integer.parseInt(smsSetupRate.split(",")[tgId]), 
                          Integer.parseInt(callDuration.split(",")[tgId]), Integer.parseInt(percentageFR.split(",")[tgId]), Integer.parseInt(percentageEFR.split(",")[tgId]), 
                          Integer.parseInt(ccchLoadPagingRate.split(",")[tgId]), Integer.parseInt(ccchLoadLRSAssRate.split(",")[tgId]), 
                          Integer.parseInt(ccchLoadLRSAssRejRate.split(",")[tgId]), Integer.parseInt(ccchLoadPSPagingRatio.split(",")[tgId]),
                          IMSI_START+(tgId*numOfImsiPerTg), numOfImsiPerTg);
            }
            abisco.startLTS();
            loadStartAt = System.currentTimeMillis();
            setTestStepEnd();
        } catch (InterruptedException e) {
            // Did not work to reset the LTS.
            e.printStackTrace();
        }
        
        getRepeatThread = new GetReportThread(interval, loadTestDuration, abisco,this, callSetupRate, smsSetupRate,numOfTGs);
        getRepeatThread.start();
        
        //Part for check load
        setTestStepBegin("Collect measurement load data");
        moshell.send("l+ /proj/gratci/system_verification/testruns/" + latencyLogFileName +"/load_and_latency.log");
        sleepSeconds(3);
        String[] commands = {"run " + measurement_File, "l-"};
        moshellThread = new MoshellScriptThread(commands,loadTestDuration,moshell);
        moshellThread.start();

        if (isSWdownloadEnable) {
            sleepSeconds(300);

            setTestStepBegin("Starting download software");
            setTestStepBegin("Prepare for sw download");
            prepareUpgradePackage();
            setTestStepEnd();
                
            setTestStepBegin("Create sw");
            createUpgradePackage();
            setTestStepEnd();
                
            setTestStepBegin("Action upgrade");
            performUpgradePrepare();
            setTestStepEnd();
                
            setTestStepBegin("Delete upgrade package");
            deleteUpgradePackage();
            setTestStepEnd();
            setTestStepEnd();
        }
        
        if (isRunManually) {
            JOptionPane.showMessageDialog(frame, "Press ok to stop LTS.");
            // TODO: efillar (2015-09-30) - Fix this, should be a parameter to test case instead.
        } else {
            loadEndAt = System.currentTimeMillis();
            int pastRunningTime = (int) (loadEndAt - loadStartAt);
            int loadtimeLeft = loadTestDuration * 1000 - pastRunningTime;
            loadtimeLeft = loadtimeLeft < 0 ? 0 : loadtimeLeft;
            setTestInfo("LTSstart time is " + loadStartAt + " swDownloadEnd time is " + loadEndAt + " pastRunningTime is " + pastRunningTime + " loadTimeLeft is " + loadtimeLeft);
            sleepMilliseconds(loadtimeLeft);
        }

       
        setTestStepBegin("Stop LTS."); 
        try { 
            abisco.stopLTS();
        } catch (InterruptedException e) {
            // Did not work to reset the LTS.
            e.printStackTrace();
        }
        setTestStepEnd();
        
        // Print out summary report for each TG
        for (int tgId = 0; tgId < numOfTGs; tgId ++) {
          // Report is used to get the load test statistics from the LTS after stopping the test.
          Report report = null;
          report = abisco.getReportLTS(tgId); 
        	
        if (report != null) {
            // Calculate some statistics
            int totalNumberOfSuccessfulCalls = (int)(report.getFRCallSuccTot() + report.getEFRCallSuccTot());
            int totalNumberOfFailedCalls = (int)(report.getFRCallFailsTot() + report.getEFRCallFailsTot());
            int totalNumberOfCalls = (int)(report.getFRCallsTot() + report.getEFRCallsTot());
            int timeInSeconds = (int)(report.getTotalTime() / 100) + 1; // At least on second 
            double csr = totalNumberOfSuccessfulCalls / (double)timeInSeconds;
            double cfr = totalNumberOfFailedCalls / (double)timeInSeconds;
            double ssr = report.getSMSSuccTot() / (double)timeInSeconds;
            double sfr = report.getSMSFailsTot() / (double)timeInSeconds;
        
            // Write summary info report
            setAdditionalResultInfo("##########################################################");
            setAdditionalResultInfo("<br><b>GRAT Stability load test summary:</b>");
            setAdditionalResultInfo("Test duration: " + timeInSeconds + " s");
            
            setAdditionalResultInfo("tgId = " + tgId +" :Call Setup Rate: " + Integer.parseInt(callSetupRate.split(",")[tgId]) / (double)1000 + " call/s");
            setAdditionalResultInfo("tgId = " + tgId +" :SMS Setup Rate: " + Integer.parseInt(smsSetupRate.split(",")[tgId]) / (double)1000 + " sms/s<br>");
          
            setAdditionalResultInfo("Call Success Rate (CSR): " + csr);
            setAdditionalResultInfo("Call Failure Rate (CFR): " + cfr);
            setAdditionalResultInfo("SMS Success Rate  (SSR): " + ssr);
            setAdditionalResultInfo("SMS Failure Rate  (SFR): " + sfr);
            setAdditionalResultInfo("Successful Call Ratio is: " + (((double)totalNumberOfSuccessfulCalls)/totalNumberOfCalls)*100 + "%");
            setAdditionalResultInfo("Successful SMS Ratio is: " + (((double)report.getSMSSuccTot())/((int)report.getSMSesTot()))*100 + "%");
            
            
            setAdditionalResultInfo("<b>GRAT Stability load test details:</b>");     
            setAdditionalResultInfo("########### FR&EFR calls ###########");
            setAdditionalResultInfo("Successful FR calls: " + (int)report.getFRCallSuccTot());
            setAdditionalResultInfo("Successful EFR calls: " + (int)report.getEFRCallSuccTot());
            setAdditionalResultInfo("Successful total calls: " + totalNumberOfSuccessfulCalls);
            setAdditionalResultInfo("Failed FR calls: " + (int)report.getFRCallFailsTot());
            setAdditionalResultInfo("Failed EFR calls: " + (int)report.getEFRCallFailsTot());
            setAdditionalResultInfo("Failed total calls: " + totalNumberOfFailedCalls);
            setAdditionalResultInfo("Total calls: " + totalNumberOfCalls);
            
            
            setAdditionalResultInfo("########### SMS ###########");
            setAdditionalResultInfo("Successful sent sms: " + (int)report.getSMSSuccTot());
            setAdditionalResultInfo("Failed sms: " + (int)report.getSMSFailsTot());
            setAdditionalResultInfo("Total sms: " + (int)(report.getSMSesTot()));
            
            
            setAdditionalResultInfo("########### CCCHLoad ###########");
                       setAdditionalResultInfo("Total LRSAss: " + (int)(report.getLRSAssignsTot()));           
            setAdditionalResultInfo("Total LRSAssRej: " + (int)(report.getLRSAssRejsTot()));           
            
            setAdditionalResultInfo("Total Paging: " + (int)(report.getPagingsTot()));
            
            setAdditionalResultInfo("###############################");
            
            setAdditionalResultInfo("TCH allocation ratio: " + report.getTCHAllocRatioTot() + " %");
            setAdditionalResultInfo("SDCCH allocation ratio: " + report.getSDCCHAllocRatioTot() + " %");
            setAdditionalResultInfo("Total pagings: " + (int)report.getPagingsTot());
            setAdditionalResultInfo("Total channel requests: " + (int)report.getChannelReqTot());
            
            setAdditionalResultInfo("##########################################################");
        }
    
        }
    }
    
    
    private void prepareUpgradePackage () {

        setTestInfo("Preparing Upgrade Package");

        // Get the path to the UpgradePackage.
        upDestinationPath = System
          .getProperty("cfg.sftpFilePathPrepend") + gratRbsUpgradePackage.getUpFilePathOnFtpServer();

        upDestinationDir = new File(upDestinationPath);

        if (upDestinationDir.exists()) {
          // Clean up directory if it already exist
          setTestInfo("Cleaning up directory: " + upDestinationPath);
          try {
            FileUtils.cleanDirectory(upDestinationDir);
          } catch (IOException e) {
            throw new MsranJcatException("Failed to clean directory: "
                + upDestinationPath);
          }
        }
        
        String upgradePackageToUnpack = new String(upDestinationPath
            + "/CXP9024418_2.zip");
        File upgradePackageZip = new File(upgradePackageToUnpack);

        setTestInfo("FetchUP starts");
        // Fetch UP
        try {
          final int connectionTimeout = 10000; // 10 seconds
          final int readTimeout = 600000; // 10 minutes
          String upgragePackageUrl = System
            .getProperty("jcat.upgrade_up_path");
          setTestInfo("Downloading: " + upgragePackageUrl);
          FileUtils.copyURLToFile(new URL(upgragePackageUrl),
              upgradePackageZip, connectionTimeout, readTimeout);
        } catch (Exception e) {
          throw new MsranJcatException("Failed to download UpgradePackage", e);
        }
        setTestInfo("FetchUP complete");
        
        // Unpack UP
        ZipHelper.unzipFiles(upgradePackageZip, upDestinationDir);

        setTestInfo("prepareUpgradePackageStep complete");
    }
    
    private void createUpgradePackage () {
        setTestInfo("Start create UpgradePackage on rbs");
        try{
            gratUpgradeHelper.createUpgradePackage();
          }catch(MsranJcatException e)
          {//to clarify root cause in report if MO was created failed with error
            setTestInfo("got the expected expection when trying to createUpgradePackage MO");
            ManagedObjectStructAttribute reportProgress = moHandler.getAttribute(SWM_MO_LDN, "reportProgress", ManagedObjectStructAttribute.class);
            String cause = reportProgress.getMember("resultInfo").getValue();
            throw new ComCliException(cause, e);
          }
          setTestStepEnd();
    }
    
    private void performUpgradePrepare() {
        setTestInfo("Start perform prepare UpgradePackage on rbs");
        gratUpgradeHelper.prepareUpgradePackage();
        setAdditionalResultInfo("SW upgrade succesfully");
    }

    private void deleteUpgradePackage() {
        setTestInfo("Start perform prepare UpgradePackage on rbs");
        String upgradeUrl = System.getProperty("jcat.upgrade_up_path");
        int idx = upgradeUrl.lastIndexOf("/"); 
        String deleteUp = upgradeUrl.substring(idx + 1, upgradeUrl.length()-4);
        setTestInfo("Starting delete UP : " + deleteUp);
        gratRbsUpgradeHlper.deleteUpgradePackage(deleteUp);
        setTestInfo("Starting delete UP file Path: " + upDestinationPath);
        try {
            FileUtils.deleteDirectory(upDestinationDir);
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }
}
