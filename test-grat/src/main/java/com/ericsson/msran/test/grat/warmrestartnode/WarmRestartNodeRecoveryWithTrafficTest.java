package com.ericsson.msran.test.grat.warmrestartnode;

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
 * @id WP4088
 * 
 * @name WarmRestartNodeTest
 * 
 * @author GRAT 2015
 * 
 * @created 
 * 
 * @description Testcases for verifying Grat Warm Restart Recoveryed With Traffic
 * 
 * @revision ezhonsu/esicwan 2015-10-15 first version
 * 
 */

public class WarmRestartNodeRecoveryWithTrafficTest extends TestBase {
    private final OM_G31R01.Enums.MOClass moClassTx = OM_G31R01.Enums.MOClass.TX;    
    private final OM_G31R01.Enums.MOClass moClassRx = OM_G31R01.Enums.MOClass.RX;    
    private final OM_G31R01.Enums.MOClass moClassTs = OM_G31R01.Enums.MOClass.TS;
    
    private AbisPrePost abisPrePost;
    private AbisHelper abisHelper;
    private MomHelper momHelper;
    private G2Rbs rbs;
    private Cli cli; 
    private Cli coli;
    
    private NodeStatusHelper nodeStatus;

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
            testDescription = "traffic can be setup again after warm restart ",
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
        rbs = Rm.getG2RbsList().get(0);
        cli = rbs.getLinuxShell();
        coli = rbs.getCsColi();
        assertTrue("Node did not reach a working state before test start", nodeStatus.waitForNodeReady());
        
        abisHelper = new AbisHelper();
        momHelper = new MomHelper();
        abisPrePost = new AbisPrePost();
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
     * @name warmRestartNodeRecoveryWithTrafficTest
     * 
     * @description 
     *   Create and unlocks MO:s. Establish OML links to the GsmSector and OML+RSL links to the Trx.
     *   Setup traffic, broadcast and call.
     *   Reboot node(Warm).
     *   MO:s are recreated and unlocked and that OML and RSL links can be reestablished.
     *   Traffics can be setup again.
     * 
     * @param testId - unique identifier
     * @param description
     * 
     * @throws InterruptedException
     */
    @Test (timeOut = 3000000)
    @Parameters({ "testId", "description" })
    public void warmRestartNodeRecoveryWithTrafficTest(String testId, String description) throws InterruptedException, JSONException {
        abisPrePost.preCondAllMoStateStarted();
        assertTrue("enableTf failed", abisHelper.enableTf());
        assertTrue("enableTx failed", abisHelper.enableTx());
        assertTrue("enableRx failed", abisHelper.enableRx());
        
        assertTrue("enableTsBcch failed", abisHelper.enableTsBcch());
        assertTrue("enableTsSdcch failed", abisHelper.enableTsSdcch());
        assertTrue("enableTsTch failed", abisHelper.enableTsTch());
        abisHelper.sysInfo();
    	sleepSeconds(abisHelper.SECONDS_TO_WAIT_FOR_SYNC); //allows for some time for MSSIM to attach/synchronize
    	checkStatus(); //make sure nothing brakes while UE syncs
    	callSetupMt();
    	sleepSeconds(3); //allows for some time before everything is torn down
    	disconnectCallMt();
    	
    	// Restart manually warm/cold
    	setTestStepBegin("Warm Restart Node, verify the AVLI entries and calculate the node restart time.");
    	coli.send("/board/restart -ew");
    	coli.send("/board/restart -w");//warm restart the node
    	setTestStepEnd();
    	
    	sleepSeconds(60);
    	
    	//start call again
        //First, wait for ssh and netconf to come up
        nodeStatus.waitForSsh();
        nodeStatus.waitForNetconf();
        rbs = Rm.getG2RbsList().get(0);
        cli = rbs.getLinuxShell(); 
	
    	abisPrePost.preCondAllMoStateStartedAfterRestart();
        assertTrue("enableTf failed", abisHelper.enableTf());
        assertTrue("enableTx failed", abisHelper.enableTx());
        assertTrue("enableRx failed", abisHelper.enableRx());
        assertTrue("enableTsBcch failed", abisHelper.enableTsBcch());
        assertTrue("enableTsSdcch failed", abisHelper.enableTsSdcch());
        assertTrue("enableTsTch failed", abisHelper.enableTsTch());
        abisHelper.sysInfo();
    	sleepSeconds(abisHelper.SECONDS_TO_WAIT_FOR_SYNC); //allows for some time for MSSIM to attach/synchronize
    	checkStatus(); //make sure nothing brakes while UE syncs
    	callSetupMt();
    	sleepSeconds(3); //allows for some time before everything is torn down
    	disconnectCallMt();   	
	
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
