package com.ericsson.msran.test.grat.warmrestartnode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import se.ericsson.jcat.fw.annotations.Setup;
import se.ericsson.jcat.fw.annotations.Teardown;

import com.ericsson.msran.g2.annotations.TestInfo;
import com.ericsson.msran.test.grat.testhelpers.AbisHelper;
import com.ericsson.msran.test.grat.testhelpers.AbiscoConnection;
import com.ericsson.msran.test.grat.testhelpers.MomHelper;
import com.ericsson.msran.test.grat.testhelpers.NodeStatusHelper;
import com.ericsson.msran.test.grat.testhelpers.restorecommands.DeleteMoCommand;
import com.ericsson.msran.test.grat.testhelpers.restorecommands.LockDeleteMoCommand;

import com.ericsson.msran.jcat.TestBase;
import com.ericsson.abisco.clientlib.servers.OM_G31R01;
import com.ericsson.commonlibrary.managedobjects.ManagedObject;
import com.ericsson.commonlibrary.remotecli.Cli;

import com.ericsson.commonlibrary.resourcemanager.g2.G2Rbs;
import com.ericsson.commonlibrary.resourcemanager.g2.G2RbsAsBts;
import com.ericsson.commonlibrary.resourcemanager.g2.logs.G2AvliLogRecord;
import com.ericsson.commonlibrary.resourcemanager.g2.logs.G2AvliNodeEventRecord;
import com.ericsson.commonlibrary.resourcemanager.g2.logs.G2AvliServiceState;
import com.ericsson.commonlibrary.resourcemanager.ResourceConfigurationException;
import com.ericsson.commonlibrary.resourcemanager.Rm;
import com.ericsson.commonlibrary.restorestack.RestoreCommand;
import com.ericsson.msran.helpers.Helpers;
import com.ericsson.msran.helpers.log.G2RbsAvailabilityLogHelper;
import com.ericsson.msran.configuration.MsranJcatException;

/**
 * @id WP4088
 * @name WarmRestartNodeTest
 * @author GRAT 2015
 * @created
 * @description Testcases for verifying Grat Warm Restart
 * @revision ezhouch 2015-10-08 First version
 *           ecoujin 2015-10-14 Add testcase "warmRestartTestCase1"
 *           ecoujin 2015-10-15 Add testcase "warmRestartTestCase2"
 *           eddguhu 2015-10-19 Add part with "publish restart duration time to web"
 *           ecoujin 2015-10-19 Add testcase "coldRestartForReference"
 *           ecoujin 2015-11-27 Add optional parameter enableRestartTimeReport2Web
 */

public class WarmRestartNodeTest extends TestBase {

    private G2Rbs g2rbs;
    private Cli coli;
    private NodeStatusHelper nodeStatus;
    private final String sectorMoLdn = MomHelper.SECTOR_LDN;
    private String trxMoLdn;
    private MomHelper momHelper;
    private AbiscoConnection abisco;
    private AbisHelper abisHelper;
    protected G2RbsAsBts rbs;
    private G2RbsAvailabilityLogHelper avliLogHelper;

    private class RestartDataD
    {

        List<Long> durationList = new ArrayList<Long>();
        long minDuration;
        long maxDuration;
        long avgDuration;

        public RestartDataD() {

        }

        public void addRestartTime(Long restartTime) {
            durationList.add(restartTime);
        }
    }

    private RestartDataD rcsRestartData = new RestartDataD();
    private RestartDataD tnRestartData = new RestartDataD();
    private RestartDataD gratCtrlRestartData = new RestartDataD();
    private RestartDataD rbsRestartData = new RestartDataD();

    /**
     * Description of test case for test reporting
     */
    @TestInfo(
            tcId = "WP4088",
            slogan = "Warm Restart Node",
            requirementDocument = "-",
            requirementRevision = "-",
            requirementLinkTested = "-",
            requirementLinkLatest = "-",
            requirementIds = { "-" },
            verificationStatement = "-",
            testDescription = "Verify node warm restart under different scenarios. Get AVLI log records to calculate the node restart time",
            traceGuidelines = "N/A")
    @Setup
    public void setup() {
    	setTestStepBegin("Start of setup()");
        nodeStatus = new NodeStatusHelper();
        assertTrue("Node did not reach a working state before test start", nodeStatus.waitForNodeReady());
        momHelper = new MomHelper();
        g2rbs = Rm.getG2RbsList().get(0);
        coli = g2rbs.getCsColi();
        abisco = new AbiscoConnection();
        abisHelper = new AbisHelper();
        rbs = g2rbs.asBts();
        avliLogHelper = Helpers.log().availabilityLogHelper(rbs);
        setTestStepEnd();
    }

    @Teardown
    public void teardown() {
    	setTestStepBegin("Start of teardown()");
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

        assertTrue("NegotiationRecord1Data List is null", abisHelper.compareNegotiationRecord1Data(negotiationRecord1Data, expectedOmlIwdVersions,
                expectedRslIwdVersions));
    }

    /**
     * Fetches the correct restart complete event record
     */
    private List<G2AvliNodeEventRecord> findRestartCompleteEntries(List<G2AvliLogRecord> avliLogsForRestart,
            String applicationTag, String upEvent)
    {
        List<G2AvliNodeEventRecord> foundEntries = new ArrayList<G2AvliNodeEventRecord>();

        if (applicationTag == "Rcs")
        {
            for (G2AvliLogRecord thisEntry : avliLogsForRestart) {
                if (thisEntry instanceof G2AvliNodeEventRecord) {
                    G2AvliNodeEventRecord thisNodeEvent = (G2AvliNodeEventRecord) thisEntry;
                    if (thisNodeEvent.getServiceState() == G2AvliServiceState.IN_SERVICE
                            && thisNodeEvent.getEventReason() != null
                            && thisNodeEvent.getEventReason().matches(upEvent)
                            && thisNodeEvent.getAdditionalInfoChildName().matches(applicationTag))
                    {
                        foundEntries.add(thisNodeEvent);
                    }
                }
            }
        }
        else if (applicationTag == "Tn" || applicationTag == "Grat")
        {
            for (G2AvliLogRecord thisEntry : avliLogsForRestart) {
                if (thisEntry instanceof G2AvliNodeEventRecord) {
                    G2AvliNodeEventRecord thisNodeEvent = (G2AvliNodeEventRecord) thisEntry;
                    if (thisNodeEvent.getServiceState() == G2AvliServiceState.IN_SERVICE
                            && thisNodeEvent.getRestartCompleted()
                            && thisNodeEvent.getNodeInfo().matches(upEvent)
                            && thisNodeEvent.getNodeInfo() != null
                            && thisNodeEvent.getAdditionalInfoChildName().matches(applicationTag))
                    {
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
    private List<G2AvliNodeEventRecord> findNodeDownEvents(List<G2AvliLogRecord> avliLogsForRestart, String downEvent)
    {
        List<G2AvliNodeEventRecord> foundEntries = new ArrayList<G2AvliNodeEventRecord>();
        for (G2AvliLogRecord thisEntry : avliLogsForRestart) {
            if (thisEntry instanceof G2AvliNodeEventRecord) {
                G2AvliNodeEventRecord thisNodeEvent = (G2AvliNodeEventRecord) thisEntry;
                if (thisNodeEvent.getServiceState() == G2AvliServiceState.OUT_OF_SERVICE
                        && thisNodeEvent.getEventReason() != null
                        && thisNodeEvent.getEventReason().matches(downEvent)
                        && thisNodeEvent.getRcsNodeDown())
                {
                    foundEntries.add(thisNodeEvent);
                }
            }
        }

        return foundEntries;
    }

    /**
     * Calculates time difference based on the supplied AVLI log records
     */
    private long calculateTimeDiff(List<G2AvliLogRecord> avliLogsForRestart, String applicationTag, String downEvent,
            String upEvent)
    {
        List<G2AvliNodeEventRecord> nodeDownEvents = findNodeDownEvents(avliLogsForRestart, downEvent);
        List<G2AvliNodeEventRecord> restartCompleteEntries = findRestartCompleteEntries(avliLogsForRestart,
                applicationTag, upEvent);

        long restartTimeMs = 0;

        int noOfDownEvents = nodeDownEvents.size();
        setTestInfo("   NrOfDownEvents is " + noOfDownEvents);
        int noOfRestartEvents = restartCompleteEntries.size();
        setTestInfo("   NrOfRestartEvents is " + noOfRestartEvents);

        if (noOfDownEvents != noOfRestartEvents) {
            setTestInfo("RBS Restart was not completed!");
            setTestInfo("Node was down " + noOfDownEvents
                    + "times. Node restarted " + noOfRestartEvents + " times.");
            throw new MsranJcatException("RBS Restart was not completed!");
        }
        else if ((noOfDownEvents > 1) || (noOfRestartEvents >1)) {
            setTestInfo("More than 1 DownEvents/RestartEvents found!");
            throw new MsranJcatException("More than 1 DownEvents/RestartEvents found!");
        }

        if (restartCompleteEntries.get(0).getTimeStamp() == null
                || nodeDownEvents.get(0).getDownTime() == null) {
            setTestInfo("Time stamp/down time missing from log.");
            throw new MsranJcatException(
                    "RBS downtime could not be calculated.");
        } else {
            restartTimeMs = (restartCompleteEntries.get(0)
                    .getTimeStamp().getTime() - nodeDownEvents
                    .get(0).getDownTime().getTime()) / 1000;
        }

        setTestInfo("Calculating the RBS restart time based on difference between log record id: "
                + nodeDownEvents.get(0).getRecordNumber()
                + " and "
                + restartCompleteEntries.get(0)
                        .getRecordNumber());

        return restartTimeMs;
    }

    /**
     * Find the minimum, maximum and average restart duration
     * times from the set of restarts done in this test
     */
    private void collectRestartDurationTimes(RestartDataD restartData) {
        if (restartData.durationList.size() > 0) {
            restartData.maxDuration = restartData.durationList.get(0);
            restartData.minDuration = restartData.durationList.get(0);
            restartData.avgDuration = 0;
            long curValue;
            Iterator<Long> iter = restartData.durationList.iterator();
            while (iter.hasNext()) {
                curValue = iter.next();
                if (curValue < restartData.minDuration) {
                    restartData.minDuration = curValue;
                } else if (curValue > restartData.maxDuration) {
                    restartData.maxDuration = curValue;
                }
                restartData.avgDuration += curValue;
            }
            restartData.avgDuration = restartData.avgDuration / restartData.durationList.size();
        }
    }

    /**
     * Create JSON file with restart duration times
     */
    private void writeRestartDurationJsonFile(RestartDataD restartData, String componentId,
            JSONArray restartDurationArray) {

        try {

            JSONObject arrayElemAvgValue = new JSONObject();
            arrayElemAvgValue.put("description", componentId);
            arrayElemAvgValue.put("Value", restartData.avgDuration);

            restartDurationArray.put(arrayElemAvgValue);

        } catch (JSONException e) {
            setTestWarning("Could not create JSON object");
            e.printStackTrace();
        }
    }

    private void createUnlockMosForOneSector(
            String sectorLdn,
            String abisIpLdn,
            String trx0ldn,
            String trx1ldn,
            String connection_name) {

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

        ManagedObject trxMo = momHelper
                .buildTrxMo(trx0ldn, MomHelper.LOCKED, momHelper.getSectorEquipmentFunctionLdn());
        createMos.add(trxMo);
        RestoreCommand restoreTrxMoCmd = new LockDeleteMoCommand(trxMo.getLocalDistinguishedName());
        momHelper.addRestoreCmd(restoreTrxMoCmd);

        ManagedObject trxMo1 = momHelper.buildTrxMo(trx1ldn, MomHelper.LOCKED,
                momHelper.getSectorEquipmentFunctionLdn());
        createMos.add(trxMo1);
        RestoreCommand restoreTrxMoCmd1 = new LockDeleteMoCommand(trxMo1.getLocalDistinguishedName());
        momHelper.addRestoreCmd(restoreTrxMoCmd1);

        setTestInfo("Create all MOs");
        momHelper.createSeveralMoInOneTx(createMos);

        assertEquals("Trx0 MO attributes did not reach expected values", "",
                momHelper.checkTrxMoAttributeAfterLock(trx0ldn, 30));
        assertEquals("Trx1 MO attributes did not reach expected values", "",
                momHelper.checkTrxMoAttributeAfterLock(trx1ldn, 30));
    }
    
    private void publishRestartDurationTimesOnWeb(String restartType, String restartDurationJsonFilename) {
        
    	momHelper = new MomHelper();

        String upRevision = "RXXXX";
    	String UpMoAttr = momHelper.getAttributeValue("ManagedElement=1,SystemFunctions=1,SwInventory=1","active");
        Pattern p = Pattern.compile(".*[^A-Za-z0-9]([A-Za-z0-9]+)");
        Matcher m = p.matcher(UpMoAttr);
        if(m.find()){
         	setTestInfo("Revision found: " + m.group(1) );
         	upRevision = m.group(1);
        }else {
         	setTestInfo("No Revision found: Will take revision " + upRevision + "  instead");
        }
        setTestInfo("Will send this command: /env/wrat/ci-tools/src/Statistics/add_chart_point.sh -i " + restartType + " -x " + upRevision + " -m " + restartDurationJsonFilename + " -n GRAT_CI");
 
        String reportRestartDurationCommand = "/env/wrat/ci-tools/src/Statistics/add_chart_point.sh -i " + restartType + " -x " + upRevision + " -m " + restartDurationJsonFilename + " -n GRAT_CI";
        runLinuxCommand(reportRestartDurationCommand); 
    }   
    
    private void runLinuxCommand(String Command)
    {
        try   
        {  
            // Execute script 
            Process pEnabling = Runtime.getRuntime().exec(Command);
            // Wait for command to run      
            pEnabling.waitFor();
            // Print stdout  
            BufferedReader buff = new BufferedReader(new InputStreamReader(pEnabling.getInputStream()));
            String line;
            setTestInfo("# Printing output from " + Command + " #");
            while((line = buff.readLine()) != null){
                setTestInfo(line);
            }
            // Cleanup if any output to stderr
            InputStream stderr = null;
            stderr = pEnabling.getErrorStream ();
            buff = new BufferedReader(new InputStreamReader(stderr));
            while((line = buff.readLine()) != null){
                setTestInfo(line);
            }
            buff.close();
            if (pEnabling.exitValue() != 0) {  
                setTestWarning(Command + " failed. Exit value=" +   
                            pEnabling.exitValue());
            }                              
        }
        catch( Exception e) {
            e.printStackTrace();
        } 
    }

    
    /**
     * @name warmRestartTestCase1
     * @description Unlock Trx with links, warm restart node and node is recovered. Could set trigger to calculate restart time.
     * @param testId - unique identifier of the test case
     * @param description
     * @param enableRestartTimeReport2Web
     * @param restartChartName
     * @throws InterruptedException
     */

    @Test(timeOut = 1200000)
    @Parameters({ "testId", "description", "enableRestartTimeReport2Web", "restartChartName"})
    public void warmRestartTestCase1(String testId, String description, final boolean enableRestartTimeReport2Web, String restartChartName) throws InterruptedException {

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

        List<G2AvliLogRecord> avliBeforeRestart = null;
        setTestInfo("Fetching AVLI log before node restart...");
        try {
            avliBeforeRestart = rbs.getAvailabilityLog().getAvliLogRecords();
        } catch (ResourceConfigurationException e)
        {
          setTestWarning("Skipping AVLI check because no ExternalFileServer is specified in resource data...");
        }
        
        setTestStepBegin("Enable and perform warm restart");
        coli.send("/board/restart -ew");
        coli.send("/board/restart -w");
        setTestStepEnd();

        setTestInfo("Sleeping for 60 seconds.");
        try {
            Thread.sleep(60 * 1000);
        } catch (InterruptedException ie) {
            setTestWarning("Sleep got interrupted.");
        }

        setTestStepBegin("Check Trx MO attributes after restart");
        assertEquals("Trx MO attributes did not reach expected values after restart", "",
                momHelper.checkTrxMoAttributeAfterUnlockNoLinks(trxMoLdn, 5));
        setTestStepEnd();

        setTestInfo("Establish SCF OML link");
        try {
            abisco.establishLinks();
        } catch (InterruptedException ie) {
            fail("InteruptedException during establishLinks");
        }
        setTestStepEnd();

        startSoScf();
        abisHelper.sendStartToAllMoVisibleInGsmSector(0);
        abisHelper.sendAoAtConfigPreDefBundling(0);
        assertTrue("Configuration signature has not been calculated (value = 0) ",
                0 != abisHelper.enableRequest(OM_G31R01.Enums.MOClass.AT, 0).getConfigurationSignature().intValue());

        setTestInfo("Establish Oml&Rsl links for Trxc");
        try {
            abisco.establishLinks(true);
        } catch (InterruptedException ie) {
            fail("InteruptedException during establishLinks");
        }
        assertEquals("Trx MO attributes did not reach expected values after unlock", "",
                momHelper.checkTrxMoAttributeAfterUnlockLinksEstablished(trxMoLdn, 5));
        setTestStepEnd();

        List<G2AvliLogRecord> avliAfterRestart = null;
        setTestInfo("Fetching AVLI log after node restart...");         
        try {
            avliAfterRestart = rbs.getAvailabilityLog().getAvliLogRecords();
        } catch (Exception e)
        {
          setTestWarning("Could not get restart time from AVLI log...", e);
        }

        setTestStepBegin("Get restart time from AVLI log");       
        List<G2AvliLogRecord> avliDiff = null;
        avliDiff = avliLogHelper.findAvliDifference(avliBeforeRestart, avliAfterRestart);
        long rcsRestartTimeSec = calculateTimeDiff(avliDiff, "Rcs", "UnOperational", "Operational");
        rcsRestartData.addRestartTime(rcsRestartTimeSec);
        setTestInfo("Restart of RCS took " + rcsRestartTimeSec + " seconds.");

        long tnRestartTimeSec = calculateTimeDiff(avliDiff, "Tn", "UnOperational", "RestartCompleted");
        tnRestartData.addRestartTime(tnRestartTimeSec);
        setTestInfo("Restart of TN took " + tnRestartTimeSec + " seconds.");

        long gratRestartTimeSec = calculateTimeDiff(avliDiff, "Grat", "UnOperational", "GRAT Traffic Control active");
        gratCtrlRestartData.addRestartTime(gratRestartTimeSec);
        setTestInfo("Restart of GratTrafficCtrl took " + gratRestartTimeSec + " seconds.");

        rbsRestartData.durationList.add(gratRestartTimeSec);
        rbsRestartData.addRestartTime(gratRestartTimeSec);
        setTestInfo("Restart of RBS took " + gratRestartTimeSec + " seconds.");
        setTestStepEnd();

        if (enableRestartTimeReport2Web == true)
        {       	  
          setTestStepBegin("Reporting to JSON web");

          JSONObject jsonObject = new JSONObject();
          JSONArray restartDurationArray = new JSONArray();

          collectRestartDurationTimes(gratCtrlRestartData);
          collectRestartDurationTimes(rcsRestartData);
          collectRestartDurationTimes(tnRestartData);
          collectRestartDurationTimes(rbsRestartData);

          writeRestartDurationJsonFile(gratCtrlRestartData, "GRAT", restartDurationArray);
          writeRestartDurationJsonFile(rcsRestartData, "RCS", restartDurationArray);
          writeRestartDurationJsonFile(tnRestartData, "TN", restartDurationArray);
          writeRestartDurationJsonFile(rbsRestartData, "RBS", restartDurationArray);

          String restartDurationJsonFilename;

          try {
              jsonObject.put(restartChartName, restartDurationArray);
              restartDurationJsonFilename = se.ericsson.jcat.fw.utils.TestInfo.getLogDir() + "/" + restartChartName + ".json";
              FileWriter wr = new FileWriter(new File(restartDurationJsonFilename));
              jsonObject.write(wr);
              wr.flush();
              wr.close();
              publishRestartDurationTimesOnWeb(restartChartName, restartDurationJsonFilename);

          } catch (IOException e) {
              setTestWarning("Could not write restart duration times to file");
              e.printStackTrace();
          } catch (JSONException e) {
              setTestWarning("Could not create JSON object");
              e.printStackTrace();
          }
          setTestStepEnd();
        }
        setTestInfo("Test is done!");

    }

    /**
     * @name warmRestartTestCase2
     * @description Two Trxes are locked, warm restart node and calculate restart time
     * @param testId - unique identifier of the test case
     * @param description
     * @throws InterruptedException
     */

    @Test(timeOut = 1200000)
    @Parameters({ "testId", "description"})
    public void warmRestartTestCase2(String testId, String description) throws InterruptedException {

        setTestCase(testId, description);

        int trxId_0 = 0;
        int trxId_1 = 1;
        String GsmSectorLdn_1 = "ManagedElement=1,BtsFunction=1,GsmSector=1";
        String AbisIpLdn_0 = GsmSectorLdn_1 + ",AbisIp=0";
        String TrxLdn_0_in_GsmSector_1 = GsmSectorLdn_1 + ",Trx=" + trxId_0;
        String TrxLdn_1_in_GsmSector_1 = GsmSectorLdn_1 + ",Trx=" + trxId_1;
        int tg_0 = 0;
        String connection_name_0 = "host_0";

        setTestStepBegin("TN");
        if (!momHelper.isTnConfigured())
            momHelper.configureTn(abisco.getBscIpAddress());

        setTestStepBegin("********* Make sure that TG is disconnected and deleted");
        abisco.disconnectTG(tg_0);
        abisco.deleteTG(tg_0);
        setTestStepEnd();

        setTestStepBegin("********* Start TSS");
        abisco.startTss();
        setTestStepEnd();

        setTestStepBegin("********* Create one TG with two TRXes");
        try {
            abisco.createTgPreDefBundling(tg_0, connection_name_0, 2, false);
            abisco.defineCell(tg_0, 2);
        } catch (InterruptedException e) {
            setTestInfo("Caught InterruptedException");
            e.printStackTrace();
        }
        setTestStepEnd();
        
        if (!momHelper.isBtsFunctionCreated()) {
        	setTestInfo("Precondition: Create BtsFunction MO");
        	momHelper.createBtsFunctionMo(MomHelper.BTS_FUNCTION_LDN);
        }

        setTestStepBegin("********* Create all MO:s for first sector");
        createUnlockMosForOneSector(GsmSectorLdn_1, AbisIpLdn_0, TrxLdn_0_in_GsmSector_1, TrxLdn_1_in_GsmSector_1,
                connection_name_0);
        setTestStepEnd();

        setTestStepBegin("********* Connect tg");
        abisco.connectTG(tg_0);
        setTestStepEnd();

        List<G2AvliLogRecord> avliBeforeRestart = null;
        setTestInfo("Fetching AVLI log before node restart...");
        try {
            avliBeforeRestart = rbs.getAvailabilityLog().getAvliLogRecords();
        } catch (ResourceConfigurationException e)
        {
            setTestWarning("Skipping AVLI check because no ExternalFileServer is specified in resource data...");
        }

        setTestStepBegin("Enable and perform warm restart");
        coli.send("/board/restart -ew");
        coli.send("/board/restart -w");
        setTestStepEnd();

        setTestInfo("Sleeping for 60 seconds.");
        try {
            Thread.sleep(60 * 1000);
        } catch (InterruptedException ie) {
            setTestWarning("Sleep got interrupted.");
            ;
        }

        setTestInfo("Check Trx MO attributes after restart");
        assertEquals("Trx0 MO attributes did not reach expected values", "",
                momHelper.checkTrxMoAttributeAfterLock(TrxLdn_0_in_GsmSector_1, 30));
        assertEquals("Trx1 MO attributes did not reach expected values", "",
                momHelper.checkTrxMoAttributeAfterLock(TrxLdn_1_in_GsmSector_1, 30));
        setTestStepEnd();

        List<G2AvliLogRecord> avliAfterRestart = null;     
        setTestInfo("Fetching AVLI log after node restart..."); 
        try {
            avliAfterRestart = rbs.getAvailabilityLog().getAvliLogRecords();
        } catch (Exception e)
        {
            setTestWarning("Could not get restart time from AVLI log...", e);
        }

        setTestStepBegin("Get restart time from AVLI log");
        List<G2AvliLogRecord> avliDiff = null;
        avliDiff = avliLogHelper.findAvliDifference(avliBeforeRestart, avliAfterRestart);
        long rcsRestartTimeSec = calculateTimeDiff(avliDiff, "Rcs", "UnOperational", "Operational");
        rcsRestartData.addRestartTime(rcsRestartTimeSec);
        setTestInfo("Restart of RCS took " + rcsRestartTimeSec + " seconds.");

        long tnRestartTimeSec = calculateTimeDiff(avliDiff, "Tn", "UnOperational", "RestartCompleted");
        tnRestartData.addRestartTime(tnRestartTimeSec);
        setTestInfo("Restart of TN took " + tnRestartTimeSec + " seconds.");

        long gratRestartTimeSec = calculateTimeDiff(avliDiff, "Grat", "UnOperational", "GRAT Traffic Control active");
        gratCtrlRestartData.addRestartTime(gratRestartTimeSec);
        setTestInfo("Restart of GratTrafficCtrl took " + gratRestartTimeSec + " seconds.");

        rbsRestartData.durationList.add(gratRestartTimeSec);
        rbsRestartData.addRestartTime(gratRestartTimeSec);
        setTestInfo("Restart of RBS took " + gratRestartTimeSec + " seconds.");
        setTestStepEnd();        
        
        setTestInfo("Test is done!");

    }
    
    
    /**
     * @name coldRestartForReference
     * @description Unlock Trx with links, cold restart node and node is recovered. Could set trigger to calculate restart time.
     * @param testId - unique identifier of the test case
     * @param description
     * @param enableRestartTimeReport2Web
     * @param restartChartName
     * @throws InterruptedException
     */

    @Test(timeOut = 1200000)
    @Parameters({ "testId", "description", "enableRestartTimeReport2Web", "restartChartName"})
    public void coldRestartForReference(String testId, String description, final boolean enableRestartTimeReport2Web, String restartChartName) throws InterruptedException {

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

        List<G2AvliLogRecord> avliBeforeRestart = null;
        setTestInfo("Fetching AVLI log before node restart...");
        try {
            avliBeforeRestart = rbs.getAvailabilityLog().getAvliLogRecords();
        } catch (ResourceConfigurationException e)
        {
            setTestWarning("Skipping AVLI check because no ExternalFileServer is specified in resource data...");
        }

        setTestStepBegin("Cold restart and sleep 180 seconds");
        momHelper.restartDu();   	
    	setTestInfo("Sleeping for 180 seconds."); 
        try {
            Thread.sleep(180 * 1000);
        } catch (InterruptedException ie) {
            setTestWarning("Sleep got interrupted.");
            ;
        }
        setTestStepEnd();
          
        setTestInfo("Check Trx MO attributes after restart");
        assertEquals("Trx MO attributes did not reach expected values after restart", "",
                momHelper.checkTrxMoAttributeAfterUnlockNoLinks(trxMoLdn, 5));
        setTestStepEnd();

        setTestInfo("Establish SCF OML link");
        try {
            abisco.establishLinks();
        } catch (InterruptedException ie) {
            fail("InteruptedException during establishLinks");
        }
        setTestStepEnd();

        startSoScf();
        abisHelper.sendStartToAllMoVisibleInGsmSector(0);
        abisHelper.sendAoAtConfigPreDefBundling(0);
        assertTrue("Configuration signature has not been calculated (value = 0) ",
                0 != abisHelper.enableRequest(OM_G31R01.Enums.MOClass.AT, 0).getConfigurationSignature().intValue());

        setTestInfo("Establish Oml&Rsl links for Trxc");
        try {
            abisco.establishLinks(true);
        } catch (InterruptedException ie) {
            fail("InteruptedException during establishLinks");
        }
        assertEquals("Trx MO attributes did not reach expected values after unlock", "",
                momHelper.checkTrxMoAttributeAfterUnlockLinksEstablished(trxMoLdn, 5));
        setTestStepEnd();

        List<G2AvliLogRecord> avliAfterRestart = null;
        setTestInfo("Fetching AVLI log after node restart..."); 
        try {
            avliAfterRestart = rbs.getAvailabilityLog().getAvliLogRecords();
        } catch (Exception e)
        {
            setTestWarning("Could not get restart time from AVLI log...", e);
        }

        setTestStepBegin("Get restart time from AVLI log");
        List<G2AvliLogRecord> avliDiff = null;
        avliDiff = avliLogHelper.findAvliDifference(avliBeforeRestart, avliAfterRestart);
        long rcsRestartTimeSec = calculateTimeDiff(avliDiff, "Rcs", "UnOperational", "Operational");
        rcsRestartData.addRestartTime(rcsRestartTimeSec);
        setTestInfo("Restart of RCS took " + rcsRestartTimeSec + " seconds.");

        long tnRestartTimeSec = calculateTimeDiff(avliDiff, "Tn", "UnOperational", "RestartCompleted");
        tnRestartData.addRestartTime(tnRestartTimeSec);
        setTestInfo("Restart of TN took " + tnRestartTimeSec + " seconds.");

        long gratRestartTimeSec = calculateTimeDiff(avliDiff, "Grat", "UnOperational", "GRAT Traffic Control active");
        gratCtrlRestartData.addRestartTime(gratRestartTimeSec);
        setTestInfo("Restart of GratTrafficCtrl took " + gratRestartTimeSec + " seconds.");

        rbsRestartData.durationList.add(gratRestartTimeSec);
        rbsRestartData.addRestartTime(gratRestartTimeSec);
        setTestInfo("Restart of RBS took " + gratRestartTimeSec + " seconds.");
        setTestStepEnd();

        if (enableRestartTimeReport2Web == true)
        { 
          setTestStepBegin("Reporting to JSON web");

          JSONObject jsonObject = new JSONObject();
          JSONArray restartDurationArray = new JSONArray();

          collectRestartDurationTimes(gratCtrlRestartData);
          collectRestartDurationTimes(rcsRestartData);
          collectRestartDurationTimes(tnRestartData);
          collectRestartDurationTimes(rbsRestartData);

          writeRestartDurationJsonFile(gratCtrlRestartData, "GRAT", restartDurationArray);
          writeRestartDurationJsonFile(rcsRestartData, "RCS", restartDurationArray);
          writeRestartDurationJsonFile(tnRestartData, "TN", restartDurationArray);
          writeRestartDurationJsonFile(rbsRestartData, "RBS", restartDurationArray);

          String restartDurationJsonFilename;

          try {
              jsonObject.put(restartChartName, restartDurationArray);
              restartDurationJsonFilename = se.ericsson.jcat.fw.utils.TestInfo.getLogDir() + "/" + restartChartName + ".json";
              FileWriter wr = new FileWriter(new File(restartDurationJsonFilename));
              jsonObject.write(wr);
              wr.flush();
              wr.close();
              publishRestartDurationTimesOnWeb(restartChartName, restartDurationJsonFilename);

          } catch (IOException e) {
              setTestWarning("Could not write restart duration times to file");
              e.printStackTrace();
          } catch (JSONException e) {
              setTestWarning("Could not create JSON object");
              e.printStackTrace();
          }
          setTestStepEnd();
        }
        setTestInfo("Test is done!");

    }


}
