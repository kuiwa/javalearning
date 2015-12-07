package com.ericsson.msran.test.grat.bblog;

import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import se.ericsson.jcat.fw.annotations.Setup;
import se.ericsson.jcat.fw.annotations.Teardown;

import com.ericsson.msran.g2.annotations.TestInfo;
import com.ericsson.msran.test.grat.testhelpers.CliCommands;
import com.ericsson.msran.test.grat.testhelpers.LogHandler;
import com.ericsson.msran.test.grat.testhelpers.NodeStatusHelper;

import com.ericsson.commonlibrary.remotecli.Cli;
import com.ericsson.msran.jcat.TestBase;
import com.ericsson.commonlibrary.resourcemanager.g2.G2Rbs;
import com.ericsson.commonlibrary.resourcemanager.Rm;

/**
 * @id GRAT000007,GRAT000008
 * 
 * @name BbLogTest
 * 
 * @author Lokeswara rao Bandaru (elokban)
 * 
 * @created 2013-08-19
 * 
 * @description 
 * 
 * @revision elokban 2013-08-20 first version
 * @revision xasahuh 2014-02-07 Set timeout for test case to 7 minutes.
 * @revision xasahuh 2014-02-10 Added TestInfo metadata.
 * @revision xasahuh 2014-02-18 Removed builder pattern structure.
 * 
 */

public class BbLogTest extends TestBase {
    
    private G2Rbs rbs;
    private LogHandler logHandler;
    Cli cli; 
    private CliCommands cliCommands;
    private NodeStatusHelper nodeStatus;
    
    /**
     * Description of test case for test reporting
     */
    @TestInfo(
            tcId = "GRAT000007,GRAT000008",
            slogan = "Store logs for BB",
            requirementDocument = "-",
            requirementRevision = "-",
            requirementLinkTested = "-",
            requirementLinkLatest = "-",
            requirementIds = { "-" },
            verificationStatement = "-",
            testDescription = "Store logs for BB.",
            traceGuidelines = "N/A")  

    
    /**
     * Clear the T&E log and activating trace groups.
     */
    @Setup
    public void setup() {
        nodeStatus = new NodeStatusHelper();
        assertTrue("Node did not reach a working state before test start", nodeStatus.waitForNodeReady());
        
        rbs = Rm.getG2RbsList().get(0);
        
        cli = rbs.getLinuxShell();
        logHandler = new LogHandler(cli);
        cliCommands = new CliCommands(cli);
        logHandler.teEnable("trace1", "*/GDLH");
        logHandler.teEnable("trace1", "*/GOAM");
    }
    
    /**
     * Save the BB T&E log
     */
    @Teardown
    public void teardown() {
        nodeStatus.isNodeRunning();
        logHandler.readAndStoreTeLog(rbs.getName());
    }

    /**
     * @name storeBbLog
     * 
     * @param testId 
     * @param description
     * @param gcpuband - SNID number ex:gcpu00256 or gcpu00512
     * @param rebootStr - The first BbLogTest in the suite shall clear logs and reboot
     */
    @Test(timeOut = 720000)
    @Parameters({ "testId", "description", "gcpuband", "reboot" })
    public void storeBbLog(String testId, String description, String gcpuband, String rebootStr) {
            
        setTestCase(testId, description);
        
        boolean reboot = rebootStr.equals("true");
        if (reboot) {
            setTestStepBegin("Clear log and reboot");
            logHandler.logClear(); 
            assertTrue("Node didnt come alive after reboot...", cliCommands.rebootAndWaitForNode());
            setTestStepEnd();
        }
        
        setTestStepBegin("Store BB logs of " + gcpuband);
        logHandler.readAndStoreBBTeLog(rbs.getName(), gcpuband);
        setTestStepEnd();
    }
}
