package com.ericsson.msran.test.grat.firstcall;

import org.json.JSONException;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.ericsson.msran.jcat.TestBase;
import se.ericsson.jcat.fw.annotations.Setup;
import se.ericsson.jcat.fw.annotations.Teardown;

import com.ericsson.abisco.clientlib.servers.OM_G31R01;
import com.ericsson.abisco.clientlib.servers.PARAMDISP.TrafficCommand_Result;
import com.ericsson.commonlibrary.remotecli.Cli;
import com.ericsson.commonlibrary.resourcemanager.Rm;
import com.ericsson.commonlibrary.resourcemanager.g2.G2Rbs;
import com.ericsson.msran.g2.annotations.TestInfo;
import com.ericsson.msran.test.grat.testhelpers.AbisHelper;
import com.ericsson.msran.test.grat.testhelpers.MomHelper;
import com.ericsson.msran.test.grat.testhelpers.NodeStatusHelper;
import com.ericsson.msran.test.grat.testhelpers.prepost.AbisPrePost;

/**
 * @id FirstCall
 * 
 * @name FirstCallTest
 *
 * @description This test class verifies the Enable Request
 */

public class FirstCallTest extends TestBase {
    private final OM_G31R01.Enums.MOClass moClassTx = OM_G31R01.Enums.MOClass.TX;    
    private final OM_G31R01.Enums.MOClass moClassRx = OM_G31R01.Enums.MOClass.RX;    
    private final OM_G31R01.Enums.MOClass moClassTs = OM_G31R01.Enums.MOClass.TS;
    
    private AbisPrePost abisPrePost;
    private AbisHelper abisHelper;
    private MomHelper momHelper;
    private G2Rbs rbs;
    private Cli cli; 

    private NodeStatusHelper nodeStatus;

    /**
     * Description of test case for test reporting
     */
    @TestInfo(
            tcId = "First Call",
            slogan = "First Call Embryo",
            requirementDocument = "1/00651-FCP 130 1402",
            requirementRevision = "PC5",
            requirementLinkTested = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff87c1d941?docno=1/00651-FCP1301402Uen&format=excel8book",
            requirementLinkLatest = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff86a2af3d?docno=1/00651-FCP1301402Uen&action=current&format=excel8book",
            requirementIds = { "10565-0334/21767[PA1][PREL]", "10565-0334/19034[A][APPR]",
                    "10565-0334/19417[A][APPR]" },
            verificationStatement = "Verifies Part of First Call",
            testDescription = "Verifies First Call",
            traceGuidelines = "N/A")


    /**
     * Make sure ricm patch is running, create MO:s, establish links to Abisco, and verify preconditions.
     * 
     * @throws InterruptedException
     * @throws JSONException 
     */
    @Setup
    public void setup() throws InterruptedException, JSONException {
    	setTestStepBegin("Setup");
        nodeStatus = new NodeStatusHelper();
        //First, wait for ssh and netconf to come up
        rbs = Rm.getG2RbsList().get(0);
        cli = rbs.getLinuxShell();
        assertTrue("Node did not reach a working state before test start", nodeStatus.waitForNodeReady());
        abisHelper = new AbisHelper();
        momHelper = new MomHelper();
        abisPrePost = new AbisPrePost();

        /**
         * @todo Create TRX MO when postconditions for it can be verified (sync is ok).
         */
        
        /**
         * @todo Verify Trx MO pre and post cond when we have sync working
         */        
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
     * @name firstCallTxRxTs
     * 
     * @description Verifies part of first call with AO TX, RX and TS
     * 
     * @param testId - unique identifier
     * @param description
     * 
     * @throws InterruptedException
     */
    @Test (timeOut = 360000) // six minutes should be enough
    @Parameters({ "testId", "description" })
    public void firstCallTxRxTs(String testId, String description) throws InterruptedException, JSONException {
        abisPrePost.preCondAllMoStateStarted();
        assertTrue("enableTf failed", abisHelper.enableTf());
        assertTrue("enableTx failed", abisHelper.enableTx());
        assertTrue("enableRx failed", abisHelper.enableRx());
        assertTrue("enableTsBcch failed", abisHelper.enableTsBcch());
        assertTrue("enableTsSdcch failed", abisHelper.enableTsSdcch());
        assertTrue("enableTsTch failed", abisHelper.enableTsTch());
        abisHelper.sysInfo();
    	//callSetupMt();
    	sleepSeconds(10); //allows for some time before everything is torn down
    }
    
    /**
     * @name firstCallMsSim
     * 
     * @description Verifies part of first call with AO TX, RX and TS
     * 
     * @param testId - unique identifier
     * @param description
     * 
     * @throws InterruptedException
     */
    @Test (timeOut = 360000) // six minutes should be enough
    @Parameters({ "testId", "description" })
    public void firstCallMsSim(String testId, String description) throws InterruptedException, JSONException {
        abisPrePost.preCondAllMoStateStarted();
        assertTrue("enableTf failed", abisHelper.enableTf());
        assertTrue("enableTx failed", abisHelper.enableTx());
        assertTrue("enableRx failed", abisHelper.enableRx());
        assertTrue("enableTsBcch failed", abisHelper.enableTsBcch());
        assertTrue("enableTsSdcch failed", abisHelper.enableTsSdcch());
        assertTrue("enableTsTch failed", abisHelper.enableTsTch());
        abisHelper.sysInfo();
    	
        setTestStepBegin("Wait for MSSIM to synch");
        // Allows for some time for MSSIM to attach/synchronize
        waitForSync(abisHelper.SECONDS_TO_WAIT_FOR_SYNC * 10);
        setTestStepEnd();
        
    	checkStatus(); //make sure nothing brakes while UE syncs
    	callSetupMt();
    	sleepSeconds(3); //allows for some time before everything is torn down
    	disconnectCallMt();
    }
    
    private void waitForSync(int secondsToWaitForSync) throws InterruptedException {
        // Send an SMS each 5 seconds. If successful return.
        while (secondsToWaitForSync > 0) {
          // Sleep a while...
          sleepSeconds(5);
             
          // Now send an Mobile Terminated SMS.
          TrafficCommand_Result res = abisHelper.sendSMSSetupMT();
             
          // Check result, if good, MSSIM is in synch, else do another iteration.
          if (res.getStatus().equals(com.ericsson.abisco.clientlib.servers.PARAMDISP.Enums.Status.Cmd_OK)) {
            setTestInfo("SMS was successfully sent to MSSIM, MSSIM should thus be in synch!");
            break; // out of while-loop
          }
          else {
        	// Decrease numIterations with one.
        	  secondsToWaitForSync -= 5;
          }
        }
    }
    
    private void checkStatus() throws InterruptedException, JSONException {
        OM_G31R01.StatusResponse statusRsp;
        
        setTestStepBegin("Send Status Request for RX");
        statusRsp = abisHelper.statusRequest(this.moClassRx);
        setTestInfo("StatusResponse: " + statusRsp.toString());
        assertEquals("RX-state is not enabled", OM_G31R01.Enums.MOState.ENABLED, statusRsp.getMOState());
    	setTestStepEnd();
        
        setTestStepBegin("Send Status Request for TX");
        statusRsp = abisHelper.statusRequest(this.moClassTx);
        setTestInfo("StatusResponse: " + statusRsp.toString());
        assertEquals("TX-state is not enabled", OM_G31R01.Enums.MOState.ENABLED, statusRsp.getMOState());
    	setTestStepEnd();
        
        setTestStepBegin("Send Status Request for TS0");
        statusRsp = abisHelper.statusRequest(this.moClassTs, 0, 0);
        setTestInfo("StatusResponse: " + statusRsp.toString());
        assertEquals("TS0-state is not enabled", OM_G31R01.Enums.MOState.ENABLED, statusRsp.getMOState());
    	setTestStepEnd();

        setTestStepBegin("Send Status Request for TS1");
        statusRsp = abisHelper.statusRequest(this.moClassTs, 1, 0);
        setTestInfo("StatusResponse: " + statusRsp.toString());
        assertEquals("TS1-state is not enabled", OM_G31R01.Enums.MOState.ENABLED, statusRsp.getMOState());
    	setTestStepEnd();

        setTestStepBegin("Send Status Request for TS2");
        statusRsp = abisHelper.statusRequest(this.moClassTs, 2, 0);
        setTestInfo("StatusResponse: " + statusRsp.toString());
        assertEquals("TS2-state is not enabled", OM_G31R01.Enums.MOState.ENABLED, statusRsp.getMOState());
    	setTestStepEnd();

        setTestStepBegin("Check MO state for TS0");
    	assertTrue("abisTsMoState for tsInstance 0 is not ENABLED", momHelper.waitForAbisTsMoState(0, "ENABLED", 5));
    	setTestStepEnd();
        setTestStepBegin("Check MO state for TS1");
    	assertTrue("abisTsMoState for tsInstance 1 is not ENABLED", momHelper.waitForAbisTsMoState(1, "ENABLED", 5));
    	setTestStepEnd();
        setTestStepBegin("Check MO state for TS2");
    	assertTrue("abisTsMoState for tsInstance 2 is not ENABLED", momHelper.waitForAbisTsMoState(2, "ENABLED", 5));
    	setTestStepEnd();
    	
    	
        setTestStepBegin("Print out stats for later faultfinding");
    	cli.connect();
    	setTestInfo("rhdc icmstatus:\n"     + cli.send("rhdc icmstatus"));
    	setTestInfo("rhdc icmtemp:\n"       + cli.send("rhdc icmtemp"));
    	setTestInfo("rhdc icmxio_status:\n" + cli.send("rhdc icmxio_status"));
    	setTestInfo("rhdc icmiqcx_stats:\n" + cli.send("rhdc icmiqcx_stats"));
    	setTestInfo("rhdc icmiqx_status:\n" + cli.send("rhdc icmiqx_status"));
    	setTestInfo("rhdc icmiqx_config:\n" + cli.send("rhdc icmiqx_config"));
    	cli.disconnect();
    	setTestStepEnd();

}
    
    private void callSetupMt() throws InterruptedException {
    	setTestStepBegin("Setup Call, MT");
    	TrafficCommand_Result res = abisHelper.sendcallSetupMt();
    	if (res.getStatus().equals(com.ericsson.abisco.clientlib.servers.PARAMDISP.Enums.Status.Cmd_OK))
    		setTestInfo("Call was successfully set up!");
    	else {
    		setTestInfo(res.toString());
    		fail("Failed connecting call.");
    	}
    	setTestStepEnd();
    }
    
    private void disconnectCallMt() throws InterruptedException {
    	setTestStepBegin("Setup Call, MT");
    	TrafficCommand_Result res = abisHelper.disconnectCallMt();
    	if (res.getStatus().equals(com.ericsson.abisco.clientlib.servers.PARAMDISP.Enums.Status.Cmd_OK))
    		setTestInfo("Call was successfully disconnected!");
    	else {
    		setTestInfo(res.toString());
    		fail("Failed disconnecting call.");
    	}
    	setTestStepEnd();
    }
 }   
