package com.ericsson.msran.test.grat.modulecheck;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import se.ericsson.jcat.fw.annotations.Setup;
import se.ericsson.jcat.fw.annotations.Teardown;

import com.ericsson.msran.g2.annotations.TestInfo;

import com.ericsson.msran.test.grat.testhelpers.CliCommands;
import com.ericsson.msran.test.grat.testhelpers.LogHandler;
import com.ericsson.msran.test.grat.testhelpers.NodeStatusHelper;
import com.ericsson.msran.test.grat.testhelpers.PidHandler;

import com.ericsson.commonlibrary.remotecli.Cli;
import com.ericsson.msran.jcat.TestBase;
import com.ericsson.commonlibrary.resourcemanager.g2.G2Rbs;
import com.ericsson.commonlibrary.resourcemanager.Rm;

/**
 * @id GRAT000002-GRAT000006
 * 
 * @name ModuleCheckTest
 * 
 * @author Lokeswara rao Bandaru (elokban)
 * 
 * @created 2013-06-10
 * 
 * @description 
 * 
 * @revision elokban 2013-06-10 first version
 * @revision uabhens 2013-07-24 added setnumberOfOccurances
 * @revision xasahuh 2014-02-07 Set timeout for test case to 5 minutes.
 * @revision xasahuh 2014-02-10 Added TestInfo metadata.
 * @revision xasahuh 2014-02-13 Removed builder pattern structure.
 * 
 */

public class ModuleCheckTest extends TestBase {
    
    private String[] pList = { "GOAM", "GSC", "GDLH", "GTE" };
    private G2Rbs rbs;
    private PidHandler pidHandler;
    private LogHandler logHandler;
    private Map<String, List<Integer>> pidHm;
    Cli cli; 
    private CliCommands cliCommands;
    private NodeStatusHelper nodeStatus;
    
    /**
     * Description of test case for test reporting
     */
    @TestInfo(
            tcId = "GRAT000002-GRAT000006",
            slogan = "Verification that a GRAT load module is up and running",
            requirementDocument = "-",
            requirementRevision = "-",
            requirementLinkTested = "-",
            requirementLinkLatest = "-",
            requirementIds = { "-" },
            verificationStatement = "-",
            testDescription = "Verifies that a specific GRAT load module is running.",
            traceGuidelines = "N/A")  

    
    /**
     * Used here for clearing the t&e log and activating trace groups.
     */
    @Setup
    public void setup() {
        nodeStatus = new NodeStatusHelper();
        assertTrue("Node did not reach a working state before test start", nodeStatus.waitForNodeReady());
        
        rbs = Rm.getG2RbsList().get(0);
        pidHandler = new PidHandler(rbs);
        
        cli = rbs.getLinuxShell();
        logHandler = new LogHandler(cli);
        cliCommands = new CliCommands(cli);
        logHandler.teEnable("trace1", "*/GDLH");
        logHandler.teEnable("trace1", "*/GOAM");
    }

    /**
     * Used here for clearing the t&e log and deactivating trace groups.
     */
    @Teardown
    public void teardown() {
        nodeStatus.isNodeRunning();
        logHandler.readAndStoreTeLog(rbs.getName());
        
        logHandler.teDisable("trace1", "*/GDLH");
        logHandler.teDisable("trace1", "*/GOAM");
    
        assertTrue("At least one process crashed during the test.", pidHandler.postCheckPidsMap(pidHm));
    }

    /**
     * @name checkLoadModule
     * 
     * @param testId
     * @param description 
     * @param loadModule
     * @param numberOfOccurances
     * @param rebootStr - The first ModuleCheckTest in the suite shall clear logs and reboot
     */
    @Test(timeOut = 600000)
    @Parameters({ "testId", "description", "loadModule", "numberOfOccurances", "reboot" })
    public void checkLoadModule(String testId, String description,
            String loadModule, int numberOfOccurances, String rebootStr) {
        
        setTestCase(testId, description);
        
        boolean reboot = rebootStr.equals("true");
        if (reboot) {
            setTestStepBegin("Clear logs and reboot");
            
            logHandler.logClear();
            assertTrue("Node didnt come alive after reboot...", cliCommands.rebootAndWaitForNode());
            setTestStepEnd();
        }
        
        setTestStepBegin("Save current pid to compare after test execution.");
        
        pidHm = new HashMap<String, List<Integer>>();
        pidHm = pidHandler.checkAndSavePidsMap(pList);
        setTestStepEnd();
    }
}
