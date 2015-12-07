package com.ericsson.msran.test.grat.synch;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import se.ericsson.jcat.fw.annotations.Setup;
import se.ericsson.jcat.fw.annotations.Teardown;

import com.ericsson.abisco.clientlib.servers.OM_G31R01;
import com.ericsson.abisco.clientlib.servers.OM_G31R01.Enums;
import com.ericsson.abisco.clientlib.servers.OM_G31R01.FSOffset;
import com.ericsson.commonlibrary.remotecli.Cli;
import com.ericsson.commonlibrary.resourcemanager.Rm;
import com.ericsson.commonlibrary.resourcemanager.g2.G2Rbs;
import com.ericsson.msran.g2.annotations.TestInfo;
import com.ericsson.msran.jcat.TestBase;
import com.ericsson.msran.test.grat.testhelpers.AbisHelper;
import com.ericsson.msran.test.grat.testhelpers.MomHelper;
import com.ericsson.msran.test.grat.testhelpers.NodeStatusHelper;
import com.ericsson.msran.test.grat.testhelpers.prepost.AbisPrePost;
import com.ericsson.msran.test.grat.testhelpers.restorecommands.SynchRegLoopColiCommand;

/**
 * @id NodeUC512
 * 
 * @name SynchRegulationLoopTest
 * 
 * @created 2015-02-25
 * 
 * @description This test class verifies the Synchronization regulation loop
 * 
 * @revision 2015-02-25 first version 
 *
 */

public class SynchRegulationLoopTest extends TestBase {
    
    private AbisPrePost abisPrePost;
    private final OM_G31R01.Enums.MOClass moClassTf = OM_G31R01.Enums.MOClass.TF;

    private AbisHelper abisHelper;
    private MomHelper momHelper;
    
    private final String GSM_SECTOR_LDN = "ManagedElement=1,BtsFunction=1,GsmSector=1";
    private NodeStatusHelper nodeStatus;
    
    private G2Rbs rbs;
	//private Logger logger;
	
    //private RestoreCommandStack restoreStack;
	
    Cli cli; 
    
    /**
     * Description of test case for test reporting
     */
    @TestInfo(
            tcId = "TC_SynchRegulationLoop",
            slogan = "Verify Synch regulation loop",
            requirementDocument = "1/00651-FCP 130 1402",
            requirementRevision = "PC5",
            requirementLinkTested = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff87c1d941?docno=1/00651-FCP1301402Uen&format=excel8book",
            requirementLinkLatest = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff86a2af3d?docno=1/00651-FCP1301402Uen&action=current&format=excel8book",
            requirementIds = { "10565-0334/21767[PA1][PREL]", "10565-0334/19034[A][APPR]",
                    "10565-0334/19417[A][APPR]" },
            verificationStatement = "",
            testDescription = "Verify timing of Synch regulation loop",
            traceGuidelines = "N/A")

    
    /**
     * Create MO:s, establish links to Abisco, and verify preconditions.
     * 
     * @throws InterruptedException
     * @throws JSONException 
     */
    @Setup
    public void setup() throws InterruptedException, JSONException {
    	setTestStepBegin("Setup");
        nodeStatus = new NodeStatusHelper();
        assertTrue("Node did not reach a working state before test start", nodeStatus.waitForNodeReady());
        
        rbs = Rm.getG2RbsList().get(0);
        cli = rbs.getLinuxShell();
        cli.send("gtrace -t gsc1 -s -g -d -c");

        momHelper = new MomHelper();
        abisHelper = new AbisHelper();
        abisPrePost = new AbisPrePost();
        abisPrePost.preCondAllMoStateStarted();
        
        Pattern pattern = Pattern.compile("\\w+=\\w+,\\w+=\\w+,\\w+=(\\w+)");
        Matcher matcher = pattern.matcher(GSM_SECTOR_LDN);
        String sectorIndex = "";
        if (matcher.matches()) {
            setTestDebug("SECTOR INDEX = " + matcher.group(1));
            sectorIndex = matcher.group(1);
        }
        //restoreStack = Helpers.restore().restoreStack();
        //restoreStack.add(new SynchRegLoopColiCommand(cli, sectorIndex)); //causes a mysterious crash!
        //anyway, there is no real need to restore the synch regulation loop flag beacuse its AoTf
        //will be deleted anyway. The next time it is created, it will be OFF.
        new SynchRegLoopColiCommand(cli, sectorIndex);
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
    
    /**
     * @name tfSynchRegulationLoop
     * 
     * @description Verifies timing of Synch regulation loop. It is not easy to test correctness of computations of EATC-BFN relation on target.
     * This is tested on host. On target, we can test the periodicity of the regulation loop and the frequency drift timer.
     *  
     * @param testId - unique identifier
     * @param testDescription
     * 
     * @throws InterruptedException
     */
    @Test (timeOut = 3600000)
    @Parameters({ "testId", "testDescription" })
    public void synchRegulationLoop(String testId, String testDescription) throws InterruptedException {
        
        setTestCase(testId, testDescription);
    	
        /*
         * Case 1: cluster group id 1
         */
        setTestStepBegin("Synchronization regulation loop output");
        
        OM_G31R01.TFConfigurationResult configRes = abisHelper.tfConfigRequest(1, Enums.TFMode.Standalone, new FSOffset(new Integer(0xFF), new Long(0xFFFFFFFFL))); //Integer.MAX_VALUE + 1
        assertEquals(OM_G31R01.Enums.MOClass.TF, configRes.getMOClass());
        
        OM_G31R01.EnableResult enableResult = abisHelper.enableRequest(this.moClassTf, 0);
        assertEquals(Enums.MOState.ENABLED, enableResult.getMOState());
        assertTrue("MO " + GSM_SECTOR_LDN + " abisTfState is not ENABLED", 
                momHelper.waitForMoAttributeStringValue(GSM_SECTOR_LDN, "abisTfState", "ENABLED", 6));
        
        //let the loop run some time
        sleepSeconds(100);
        
        String regulationLoopString = cli.send("gtrace -t gsc1 -d -f \"TARGET_TEST_TRACE_AOTF_240_MS_TIMEOUT\""); //Received
        String regulationLoopLines[] = regulationLoopString.split("\n");   
        
        String frequencyDriftString = cli.send("gtrace -t gsc1 -d -f \"TARGET_TEST_TRACE_GTSI_SET_EATC_BFN_RELATION_REQ\""); //Sending
        String frequencyDriftLines[] = frequencyDriftString.split("\n");

        setTestDebug("regulationLoopLines.length " + regulationLoopLines.length);
        setTestDebug("frequencyDriftLines.length " + frequencyDriftLines.length);
        
        setTestInfo("regulationLoopString " + regulationLoopString);
        setTestInfo("frequencyDriftString " + frequencyDriftString);
        
        saveAssertTrue("There are no trace message for regulation loop", regulationLoopLines.length >= 10);
        saveAssertTrue("There are no trace message for frequency drift loop", frequencyDriftLines.length >= 10);
                
        verifyString(regulationLoopLines, 240, 1, 1.0, 4.0);
        verifyString(frequencyDriftLines, 961, 1, 1.1, 4.0);
        setTestStepEnd();
    }
    
    private void verifyString(String[] lines, long nominalPeriod, long acceptedIndividualDeviation,
    		                  double acceptedAverageDeviation, double acceptedVariance) {
    	long previousTime = 0;
    	long currentTime = 0;
    	long timeDifference = 0;
    	long individualDeviation = 0;
    	long numberDeviations = 0;
    	long deviationSum = 0;
    	long varianceSum = 0;
    	long nrSamples = 0;
    	
    	for (int i = 0; i < lines.length; ++i) {
    		try {
    			setTestInfo("lines[ " + i + "] " + lines[i]);
    			Pattern pattern = Pattern.compile("\\d+\\.\\d+\\.\\d+\\s+\\:\\s+\\w+\\s+(\\w+\\s+\\d+\\s+\\d+\\:\\d+\\:\\d+\\s+\\d+)\\s+at\\s+(\\d+)us");
    			Matcher matcher = pattern.matcher(lines[i]);
    			if (matcher.find()) {
    				String dateWithoutMillis = matcher.group(1);
    				String millis = String.valueOf(Math.round(Double.parseDouble(matcher.group(2)) / 1000.0));
    				setTestInfo("MATCH DATE " + dateWithoutMillis);
    				setTestInfo("MATCH US " + millis + "ms");
    				SimpleDateFormat inputFormat = new SimpleDateFormat("MMM dd HH:mm:ss yyyy SSS");
    				Date date = inputFormat.parse(dateWithoutMillis + " " + millis);
    				SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy MMMMMMMM dd HH:mm:ss SSS");
    				setTestInfo("DATE RESULT " + outputFormat.format(date));
    				if (++nrSamples <= 2 || previousTime == 0) { // ignore first signal, save time from second signal as difference between first and second can be big for the drift timer
    					previousTime = date.getTime();
    				} else {
    				    currentTime = date.getTime();
    				    setTestInfo("DATE MILLIS " + currentTime);
    				    timeDifference = currentTime - previousTime;
    				    previousTime = currentTime;
    				    setTestInfo("DIFFERENCE SINCE PREVIOUS " + timeDifference);
    				    individualDeviation = timeDifference - nominalPeriod;
    				    setTestInfo("INDIVIDUAL DEVIATION " + individualDeviation);
    				    if (Math.abs(individualDeviation) > acceptedIndividualDeviation) {
    				        setTestWarning("IndividualDeviation bigger than accepted " + 
    				                Math.abs(individualDeviation)  + " > " + acceptedIndividualDeviation);
    				    }
    				    deviationSum += individualDeviation;
    				    varianceSum += (individualDeviation * individualDeviation);
    				    ++numberDeviations;
    				    //logger.error("Abnormal Individual Deviation " + individualDeviation);
    				}
    			}
    		} catch (Exception e) {
    			setTestInfo("Exception THROWN " + e.getMessage());
    		}
    	}
    	
    	if (numberDeviations > 0) {
    		double averageDeviation = deviationSum * 1.0 / numberDeviations;
    		setTestInfo("DEVIATION SUM " + deviationSum);
    		setTestInfo("NUMBER DEVIATIONS " + numberDeviations);
    		setTestInfo("AVERAGE DEVIATION " + (float)averageDeviation);
    		saveAssertTrue("AverageDeviation bigger than accepted",
    				       Math.abs(averageDeviation) <= acceptedAverageDeviation);
    		
    		double variance = varianceSum * 1.0 / numberDeviations;
    		setTestInfo("VARIANCE SUM " + varianceSum);
    		setTestInfo("NUMBER DEVIATIONS " + numberDeviations);
    		setTestInfo("VARIANCE " + (float)variance);
    		saveAssertTrue("Variance bigger than accepted", variance <= acceptedVariance);
    	}
    }
}
