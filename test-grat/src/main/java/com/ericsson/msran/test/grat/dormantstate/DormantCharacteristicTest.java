package com.ericsson.msran.test.grat.dormantstate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import se.ericsson.jcat.fw.annotations.Setup;
import se.ericsson.jcat.fw.annotations.Teardown;

import com.ericsson.commonlibrary.ecimcom.netconf.NetconfManagedObjectHandler;
import com.ericsson.commonlibrary.managedobjects.ActionResult;
import com.ericsson.commonlibrary.managedobjects.ManagedObjectStructAttribute;
import com.ericsson.commonlibrary.managedobjects.ManagedObjectValueAttribute;
import com.ericsson.commonlibrary.remotecli.Cli;
import com.ericsson.commonlibrary.resourcemanager.Rm;
import com.ericsson.commonlibrary.resourcemanager.g2.G2Rbs;
import com.ericsson.msran.g2.annotations.TestInfo;
import com.ericsson.msran.jcat.TestBase;
import com.ericsson.msran.test.grat.testhelpers.MomHelper;
import com.ericsson.msran.test.grat.testhelpers.NodeStatusHelper;

/**
 * @id NodeUC512
 * 
 * @name DormantCharacteristicTest
 * 
 * @created 2015-02-25
 * 
 * @description This test class verifies some node characteristics in the dormant state
 * 
 * @revision 2015-02-25 first version 
 *
 */

public class DormantCharacteristicTest extends TestBase {
    private NodeStatusHelper nodeStatus;
    private G2Rbs rbs;
    private Cli coli;
    private Cli linux; //only used in unsecure mode
    
    private boolean isBoardSecure = false;
    private Map<String, String> loadModuleCharacteristicInfo;
    private final String GOAM = "GOAM";
    private final String GDLH = "GDLH";
    private final String GSC = "GSC";
    private final String cpuLoadLogFile = "/tmp/cpuLoad.log";
    private final long   maxAllowedGratRssMemoryKb = 204800L;   // 200 * 1024;
    private final int    maxAllowedGratCpuUsagePerSecond = 10;   // GRAT measurement:   9%, original figure   1%
    private final double maxAllowedGratCpuUsagePerMinute = 0.5; //  GRAT measurement: 0.3%, original figure 0.1%
    private final int numberOfCpuUsageMeasurements = 300;
    private int[] secondHistogram = new int[100];
    private int[] minuteHistogram = new int[10];

    /**
     * Description of test case for test reporting
     */
    @TestInfo(
            tcId = "TC_DormantStateCharacteristics",
            slogan = "Verifies GRAT CPU load and memory consumption in dormant state",
            requirementDocument = "1/00651-FCP 130 1402",
            requirementRevision = "PC5",
            requirementLinkTested = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff87c1d941?docno=1/00651-FCP1301402Uen&format=excel8book",
            requirementLinkLatest = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff86a2af3d?docno=1/00651-FCP1301402Uen&action=current&format=excel8book",
            requirementIds = { "10565-0334/21767[PA1][PREL]", "10565-0334/19034[A][APPR]",
                    "10565-0334/19417[A][APPR]" },
            verificationStatement = "",
            testDescription = "Verifies CPU load and memory consumption in dormant state",
            traceGuidelines = "N/A")
    /**
     * 
     * 
     * @throws InterruptedException
     * @throws JSONException 
     */
    @Setup
    public void setup() {
    	setTestStepBegin("Setup");
        nodeStatus = new NodeStatusHelper();
        assertTrue("Node did not reach a working state before test start", nodeStatus.waitForNodeReady(false, false));
        
        rbs = Rm.getG2RbsList().get(0);
        coli = rbs.getCsColi();
        linux = rbs.getLinuxShell(); //used only for no secure mode
        
        checkIfGratInDormantState();
        
        loadModuleCharacteristicInfo = new HashMap<String, String>();
        loadModuleCharacteristicInfo.put(GOAM, "");
        loadModuleCharacteristicInfo.put(GDLH, "");
        loadModuleCharacteristicInfo.put(GSC, "");
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
    
    private void checkIfGratInDormantState() {
        boolean isDormant = false;
        NetconfManagedObjectHandler moHandler = rbs.getManagedObjectHandler();
        
        //MomHelper creates BtsFunction at the very beginning so we cannot use this class to check MO, restart DU, etc.
        //we have to use this raw method
        moHandler.connect();
        try {
            moHandler.getManagedObject(MomHelper.BTS_FUNCTION_LDN);
            isDormant = false;
            setTestInfo("NON-DORMANT");
            moHandler.deleteManagedObject(MomHelper.BTS_FUNCTION_LDN);
            setTestInfo("AFTER DELETE BTS FUNCTION");
            moHandler.getManagedObject(MomHelper.BTS_FUNCTION_LDN);
            setTestInfo("AFTER GET BTS FUNCTION");
        } catch (Exception e) {
            isDormant = true;
            setTestInfo("DORMANT");
        } finally {
            moHandler.disconnect();
        }

        if (!isDormant) {
            fail("GRAT is not in dormant state, which is required to run this suite");         
        } else {
            //restart to be sure the GRAT is in dormant state
            //we cannot use MomHelper.restartDu() as it creates BtsFunction
            //so we use the raw method instead
            Logger logger = Logger.getLogger(DormantCharacteristicTest.class);
            try {
                moHandler.connect();
                ManagedObjectStructAttribute actionParameters = new ManagedObjectStructAttribute(MomHelper.RESTART_DU_ACTION);
                actionParameters.addMember(new ManagedObjectValueAttribute( "restartRank", MomHelper.RESTART_DU_RANK_COLD));
                actionParameters.addMember(new ManagedObjectValueAttribute( "restartReason", MomHelper.RESTART_DU_REASON));
                actionParameters.addMember(new ManagedObjectValueAttribute( "restartInfo", MomHelper.RESTART_DU_INFO));
                ActionResult actionResult = moHandler.performAction(MomHelper.FRU_DU_LDN, MomHelper.RESTART_DU_ACTION, actionParameters );
                moHandler.disconnect();
                logger.info( "Result of the Manual Restart Action: " + actionResult.getResult() );
            } catch(Exception e) {
                logger.warn( "Exception: performAction :: " + e.getMessage());
                moHandler.disconnect();
            }
            sleepSeconds(180);
        }
    }
    
    /**
     * @name dormantCahracteristics
     * 
     * @description Verifies some node characteristics in dormant state
     *  
     * @param testId - unique identifier
     * @param testDescription
     *
     */
    @Test (timeOut = 3700000)
    @Parameters({ "testId", "testDescription" })
    public void dormantCharacteristics(String testId, String testDescription) {    
        setTestCase(testId, testDescription);
        setIsBoardSecure();
        checkMemoryUsage();
        checkCpuUsage();
    }
    
    /**
     * This method returns true if it is a secure board
     */    
    private void setIsBoardSecure() {
        String secureHwPid1="KDU137925/31";
        String secureHwPid2="KDU137925/41";
        ArrayList<String> hwPidlist = new ArrayList<String>();
        hwPidlist.add(secureHwPid1);
        hwPidlist.add(secureHwPid2);
        String hwPid = coli.send("board/hwpid");       
        setTestInfo("hwpid = " + hwPid);
        for (String pid : hwPidlist) {
            if (hwPid.contains(pid)) {
               isBoardSecure = true;
               setTestInfo("Board is secure");
               break;
            }
        } 
    }
    
    private void getLoadModuleCharacteristicInfo() {
        for (Map.Entry<String, String> entry : loadModuleCharacteristicInfo.entrySet()) {
            entry.setValue(coli.send("os/ps aux | misc/grep sirpa/ | misc/grep " + entry.getKey()));
            setTestInfo("Result of " + entry.getKey() + entry.getValue());
        }
    }
    
    private void checkMemoryUsage() {
        getLoadModuleCharacteristicInfo();
        long totalMemoryUsed = 0;
        for (Map.Entry<String, String> entry : loadModuleCharacteristicInfo.entrySet()) {
            String individualMemoryUsed =  entry.getValue().split("\\s+")[6];
            setTestInfo("Memory used by " + entry.getKey() + "(KB) : " + individualMemoryUsed);
            totalMemoryUsed += Long.parseLong(individualMemoryUsed);
        }
        setTestInfo("Total RSS memory used by GRAT in dormant state (KB): " + totalMemoryUsed);
        saveAssertTrue("Residential memory for GRAT in dormant state exceeds its limit", totalMemoryUsed <= maxAllowedGratRssMemoryKb);
    }
    
    private void checkCpuUsage() {
        if (isBoardSecure) {
            checkCpuUsageWithColi();
        } else {
            checkCpuUsageWithLinux();
        }
    }
    
    private void checkCpuUsageWithColi() {
        getLoadModuleCharacteristicInfo();
        setTestStepBegin("Check CPU usage for GRAT in dormant state (secure board)");
        
        double totalCpuUsagePerMinute = 0.0;
        int index;
        for (index = 0; index < numberOfCpuUsageMeasurements; index++) {
            if ((index % 60) == 0 && index != 0) {
                checkAverageCpuUsagePerMinute(totalCpuUsagePerMinute, index);
                totalCpuUsagePerMinute = 0.0;
            }
            int totalCpuUsagePerSecond = 0;
            //COLI takes time so it should not be called more than once per measurement
            String rows[] = coli.send("os/top | misc/grep sirpa | misc/grep G").split("\n");
            for (Map.Entry<String, String> entry : loadModuleCharacteristicInfo.entrySet()) {
                for (int j = 0; j < rows.length; ++j) {
                    if (rows[j].contains(entry.getKey())) {
                        double cpuUsage = Double.parseDouble(rows[j].split("\\s+")[9]);
                        //setTestInfo("[" + System.currentTimeMillis() / 1000L + "." + System.currentTimeMillis() % 1000 + "]. cpu usage for " + entry.getKey() + " : " + cpuUsage);
                        totalCpuUsagePerSecond += cpuUsage;
                    }
                }
            }
            //setTestInfo("Total cpu usage for GRAT per second[" + index + "]: " + totalCpuUsagePerSecond);
            ++secondHistogram[totalCpuUsagePerSecond];
            saveAssertTrue("CPU usage per second for GRAT in dormant state exceeds its limit", totalCpuUsagePerSecond <= maxAllowedGratCpuUsagePerSecond);
            totalCpuUsagePerMinute += totalCpuUsagePerSecond;
            //next measurement should start on next second
            long currentTime =  System.currentTimeMillis() % 1000L;
            sleepMilliseconds(1000L - currentTime); //there should be 1 second between each measurement
        }
        if ((index % 60) == 0) {
            checkAverageCpuUsagePerMinute(totalCpuUsagePerMinute, index);
        }
        
        traceHistogram("CpuLoadPerSecondHistogram",  1.0, secondHistogram);
        traceHistogram("CpuLoadPerMinuteHistogram", 10.0, minuteHistogram);

        coli.disconnect();
        linux.disconnect();
        setTestStepEnd();
    }
    
    private void traceHistogram(String name, double divisionFactor, int[] array) {
        for (int i = 0; i < array.length; ++i) {
            setTestInfo(name + "[" + (i / divisionFactor) + "] = " + array[i]);
        }
    }
    
    private void checkCpuUsageWithLinux() {
        //Measurement time in seconds
        final int measurementTime = 1;

        getLoadModuleCharacteristicInfo();
        setTestStepBegin("Check CPU usage for GRAT in dormant state (non-secure board)");

        String pidList = "";
        String linuxCommand = "top -b -d " + measurementTime + " -n " + numberOfCpuUsageMeasurements;
        for (Map.Entry<String, String> entry : loadModuleCharacteristicInfo.entrySet()) {
            String pid = entry.getValue().split("\\s+")[2];
            pidList += " " + entry.getKey() + " pid = " + pid;
            linuxCommand += " -p " + pid;
        }
        linuxCommand += " > " + cpuLoadLogFile + " &";

        setTestInfo("pidList " + pidList);
        setTestInfo("linux command " + linuxCommand);
        linux.send(linuxCommand);
        int timeOffset = (numberOfCpuUsageMeasurements / 250); //add some offset because JCAT and the node are not time aligned;
        sleepSeconds((numberOfCpuUsageMeasurements * measurementTime) + timeOffset); 

        HashMap<String, String[]> values = new HashMap<String, String[]>();
        for (String key : loadModuleCharacteristicInfo.keySet()) {
            values.put(key, linux.send("cat " + cpuLoadLogFile + " | grep " + key).split("\\s+"));
        }
        
        double totalCpuUsagePerMinute = 0.0;
        int index;
        for (index = 0; index < numberOfCpuUsageMeasurements; index++) {
            if ((index % 60) == 0 && index != 0) {
                checkAverageCpuUsagePerMinute(totalCpuUsagePerMinute, index);
                totalCpuUsagePerMinute = 0.0;
            }
            int totalCpuUsagePerSecond = 0;
            for (Map.Entry<String, String[]> entry : values.entrySet()) {
            	double cpuUsage = Double.parseDouble(entry.getValue()[12 * index + 9]);
                //setTestInfo("cpu usage for " + entry.getKey() + " : " + cpuUsage);
                totalCpuUsagePerSecond += cpuUsage;
            }
            //setTestInfo("Total cpu usage for GRAT per second[" + index + "]: " + totalCpuUsagePerSecond);
            ++secondHistogram[totalCpuUsagePerSecond];
            saveAssertTrue("CPU usage per second for GRAT in dormant state exceeds its limit", totalCpuUsagePerSecond <= maxAllowedGratCpuUsagePerSecond);
            totalCpuUsagePerMinute += totalCpuUsagePerSecond;
        }
        if ((index % 60) == 0) {
            checkAverageCpuUsagePerMinute(totalCpuUsagePerMinute, index);
        }
        
        traceHistogram("CpuLoadPerSecondHistogram",  1.0, secondHistogram);
        traceHistogram("CpuLoadPerMinuteHistogram", 10.0, minuteHistogram);

        coli.disconnect();
        linux.disconnect();
        setTestStepEnd();
    }

    private void checkAverageCpuUsagePerMinute(double totalCpuUsagePerMinute, int index) {
        double averageCpuUsagePerMinute = totalCpuUsagePerMinute / 60;
        int averageCpuUsagePerMinuteInt = (int)java.lang.StrictMath.round(averageCpuUsagePerMinute * 10);
        setTestInfo("Average cpu usage for GRAT per minute[" + ((index / 60) - 1) + "]: " + averageCpuUsagePerMinute);
        ++minuteHistogram[averageCpuUsagePerMinuteInt];
        saveAssertTrue("CPU usage per minute for GRAT in dormant state exceeds its limit", averageCpuUsagePerMinute <= maxAllowedGratCpuUsagePerMinute);
    }
}
