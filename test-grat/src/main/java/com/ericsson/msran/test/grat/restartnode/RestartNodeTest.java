package com.ericsson.msran.test.grat.restartnode;

import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import se.ericsson.jcat.fw.annotations.Setup;
import com.ericsson.msran.g2.annotations.TestInfo;
import com.ericsson.msran.test.grat.testhelpers.CliCommands;
import com.ericsson.msran.test.grat.testhelpers.AbiscoConnection;
import com.ericsson.msran.jcat.TestBase;

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

public class RestartNodeTest extends TestBase {
    
    private CliCommands cliCommands;
    private int secondsToWaitBeforePoll;
    private AbiscoConnection abiscoConnection;
    
    /**
     * Description of test case for test reporting
     */
    @TestInfo(
            tcId = "GRAT000000",
            slogan = "Restart Node",
            requirementDocument = "-",
            requirementRevision = "-",
            requirementLinkTested = "-",
            requirementLinkLatest = "-",
            requirementIds = { "-" },
            verificationStatement = "-",
            testDescription = "Restart Node and make sure everything starts up correctly.",
            traceGuidelines = "N/A")  
    
    /**
     * Clear the T&E log and activating trace groups.
     */
    @Setup
    public void setup() {
    	setTestStepBegin("Setup");
        cliCommands = new CliCommands();
        abiscoConnection = new AbiscoConnection();
        if(cliCommands.fileChecker("/rcs/applicationlogs/GRAT_CXP9023458_2")){ // DUS41 is really slow...
        	secondsToWaitBeforePoll = 240;
        }
        else {
        	secondsToWaitBeforePoll = 120;
        }
        setTestStepEnd();
    }
    
    /**
     * @name restartNode
     * 
     * @param testId 
     * @param description
     */
    @Test(timeOut = 420000)
    @Parameters({ "testId", "description"})
    public void restartNode(String testId, String description) {
            
        setTestCase(testId, description);
        
        setTestStepBegin("Restart Node and make sure everything starts up correctly.");
        assertTrue("Node didnt come alive after reboot...", cliCommands.rebootAndWaitForNodeTime(secondsToWaitBeforePoll));
        setTestStepEnd();
        setTestStepBegin("Restart TSS.");
        abiscoConnection.stopTss();
        abiscoConnection.setupAbisco(false);
        setTestStepEnd();
    }
}
