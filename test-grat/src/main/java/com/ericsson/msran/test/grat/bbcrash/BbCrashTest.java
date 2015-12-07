package com.ericsson.msran.test.grat.bbcrash;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import se.ericsson.jcat.fw.annotations.Setup;
import se.ericsson.jcat.fw.annotations.Teardown;

import com.ericsson.msran.g2.annotations.TestInfo;
import com.ericsson.msran.helpers.Helpers;
import com.ericsson.msran.test.grat.testhelpers.CliCommands;
import com.ericsson.msran.test.grat.testhelpers.LogHandler;
import com.ericsson.msran.test.grat.testhelpers.NodeStatusHelper;
import com.ericsson.msran.test.grat.testhelpers.restorecommands.EmptyDspDumpsdir;
import com.ericsson.commonlibrary.restorestack.RestoreCommandStack;
import com.ericsson.commonlibrary.remotecli.Cli;
import com.ericsson.msran.jcat.TestBase;
import com.ericsson.commonlibrary.resourcemanager.g2.G2Rbs;
import com.ericsson.commonlibrary.resourcemanager.Rm;

/**
 * @id GRAT000009,GRAT000010
 * 
 * @name BbCrashTest
 * 
 * @author Lokeswara rao Bandaru (elokban)
 * 
 * @created 2013-08-20
 * 
 * @description 
 * 
 * @revision elokban 2013-08-20 first version
 * @revision xasahuh 2014-02-07 Set timeout for test case to 12 minutes.
 * @revision xasahuh 2014-02-10 Added TestInfo metadata.
 * @revision xasahuh 2014-02-18 Removed builder pattern structure.
 * 
 */

public class BbCrashTest extends TestBase {
    
    private G2Rbs rbs;
    private LogHandler logHandler;
    private Cli cli; 
    private CliCommands cliCommands;
    private RestoreCommandStack restoreStack;
    private String dspDumpPath = "/rcs/applicationlogs/GRAT_CXP9023458_";
    private NodeStatusHelper nodeStatus;
    boolean dus41 = false;
    /**
     * Description of test case for test reporting
     */
    @TestInfo(
            tcId = "GRAT000009,GRAT000010",
            slogan = "Verify that log dump files are created for BB",
            requirementDocument = "-",
            requirementRevision = "-",
            requirementLinkTested = "-",
            requirementLinkLatest = "-",
            requirementIds = { "-" },
            verificationStatement = "-",
            testDescription = "Verify that log dump files are created for BB.",
            traceGuidelines = "N/A")  
    
    @BeforeClass
    public void beforeClass() {
        setTestCase("beforeClass", "beforeClass");
        nodeStatus = new NodeStatusHelper();
        assertTrue("Node did not reach a working state before test start", nodeStatus.waitForNodeReady());
        
        rbs = Rm.getG2RbsList().get(0);
        restoreStack = Helpers.restore().restoreStack();
        cli = rbs.getLinuxShell();
        logHandler = new LogHandler(cli);
        cliCommands = new CliCommands(cli);
        // Make sure no dumps exist
        if(cliCommands.fileChecker(dspDumpPath+"2"))
        {
        	dspDumpPath+="2/dspdumps/";
        	dus41=true;
        }
        else
        {
        	dspDumpPath+="3/dspdumps/";
        }
        if (callFileCheck("EMdump*", "GCPUdump*") > 0){
        	fail("ERROR: Dumps allready exist, exiting...");
        }
        restoreStack.add(new EmptyDspDumpsdir(dspDumpPath));
    } 
    
    /**
     * Clear the T&E log and activating trace groups.
     */
    @Setup
    public void setup() {
        logHandler.teEnable("trace1", "*/GDLH");
        logHandler.teEnable("trace1", "*/GOAM");
    }

    /**
     * Clear the T&E log and deactivate trace groups.
     */
    @Teardown
    public void teardown() {
        nodeStatus.isNodeRunning();
        // Save BB T&E log
        logHandler.readAndStoreTeLog(rbs.getName());
        
        logHandler.teDisable("trace1", "*/GDLH");
        logHandler.teDisable("trace1", "*/GOAM");

        // LogClear and Reboot RBS So It does not Effect other Test Cases after
        // the Crash is done!!
        logHandler.logClear(); // Clear Log
        assertTrue("Node didnt come alive after reboot...", cliCommands.rebootAndWaitForNode());
    }

    /**
     * @name makeBbCrash
     * 
     * @param testId 
     * @param description
     * @param loadModule
     * @param dsp emca number
     * @param numberOfOccurances - Describes how many times the String is found
     * @param rebootStr - The first BbCrashTest in the suite shall clear logs and reboot
     */
    @Test(timeOut = 750000)
    @Parameters({ "testId", "description", "loadModule", "dsp", "numberOfOccurances", "reboot" })
    public void makeBbCrash(String testId, String description,
                            String loadModule, int dsp, int numberOfOccurances, String rebootStr) {
        
        setTestCase(testId, description);
        
        String path1;
        String path2;
              
        boolean reboot = rebootStr.equals("true");
        if (reboot) {
            setTestStepBegin("Clear logs and reboot");
            logHandler.logClear();
            assertTrue("Node didnt come alive after reboot...", cliCommands.rebootAndWaitForNode());
            setTestStepEnd();
        }
        
        setTestStepBegin("Execute colish commands");
        
        if(dus41)
        {
        	dsp=2; //dus41 always loads emca 2 aka snid 512
        	path1 = "EMdump"+(256*dsp)+"*";
        	path2 = "GCPUdump"+(256*dsp)+"*";
        }
        else
        {
        	path1 = "lm_"+(256*dsp)+"*";
        	path2 = "cm_"+(256*dsp)+"*";
        }
        
        
        cli.send("gratBbCrash "+dsp);
        setTestInfo("gratBbCrash executed \n\n\n");
        setTestStepEnd();
        sleepSeconds(60);
                
        // Checks if the specified file existed for specified gcpuband
        if (callFileCheck(path1, path2) < 2) {
        	failAndRestart("ERROR: Could not find " + path1 + " or " + path2);
        }
        deleteFiles(path1, path2);
    }
    
    /**
     * This method is used to check if the File is present in the specified
     * Folder/Path This is temporary Solution Till we get support from TAC team
     * 14th Aug 2013 //elokban
     */
    private int callFileCheck(String path1, String path2) {
        int count = 0;
        String file = dspDumpPath + path1;
        if (cliCommands.fileChecker(file)) {
            count++;
        }
        String file1 = dspDumpPath + path2;
        if (cliCommands.fileChecker(file1)) {
            count++;
            setTestInfo("Count = " + count);
        }
        return count;
    }
    
    /**
     * This method is used to cleanup the node after generating dumps
     */
    private void deleteFiles(String path1, String path2) {
        cliCommands.deleteFiles(dspDumpPath + path1);
        cliCommands.deleteFiles(dspDumpPath + path2);
    }

    /**
     * This method is used to cleanup the node after generating dumps
     */
    private void failAndRestart(String message) {
        setTestInfo("\ngratBbCrash test failed, because " + message + " and will try to restore node...\n");
        // Storing Logs in CI console
        logHandler.readAndStoreTeLog(rbs.getName());

        // LogClear and Reboot RBS So It does not Effect other Test Cases after
        // the Crash is done!!
        logHandler.logClear(); // Clear Log
        assertTrue("Node didnt come alive after reboot...", cliCommands.rebootAndWaitForNode());
        cliCommands.deleteFiles(dspDumpPath + "*");
    	fail("ERROR: " + message);
    }
}

