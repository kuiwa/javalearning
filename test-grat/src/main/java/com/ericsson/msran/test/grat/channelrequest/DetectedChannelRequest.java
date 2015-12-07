package com.ericsson.msran.test.grat.channelrequest;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import se.ericsson.jcat.fw.annotations.Setup;
import se.ericsson.jcat.fw.annotations.Teardown;
import com.ericsson.abisco.clientlib.AbiscoResponse;
import com.ericsson.abisco.clientlib.MessageQueue;
import com.ericsson.abisco.clientlib.servers.BG;
import com.ericsson.abisco.clientlib.servers.BG.ChannelRequired;
import com.ericsson.commonlibrary.resourcemanager.Rm;
import com.ericsson.msran.g2.annotations.TestInfo;
import com.ericsson.msran.helpers.Helpers;
import com.ericsson.msran.test.grat.testhelpers.AbisHelper;
import com.ericsson.msran.test.grat.testhelpers.AbiscoConnection;
import com.ericsson.msran.test.grat.testhelpers.CliCommands;
import com.ericsson.msran.test.grat.testhelpers.GsmbHelper;
import com.ericsson.msran.test.grat.testhelpers.MomHelper;
import com.ericsson.msran.test.grat.testhelpers.MssimHelper;
import com.ericsson.msran.test.grat.testhelpers.NodeStatusHelper;
import com.ericsson.msran.test.grat.testhelpers.TgLdns;
import com.ericsson.msran.test.grat.testhelpers.restorecommands.DeActivateForwardChReqToBG;
import com.ericsson.msran.jcat.TestBase;
import com.ericsson.mssim.gsmb.Confirmation;
import com.ericsson.mssim.gsmb.Gsmb;
import com.ericsson.mssim.gsmb.GsmphMPH_CCCH_DATA_REQ;
import com.ericsson.mssim.gsmb.Indication;
import com.ericsson.mssim.gsmb.impl.GsmbFactory;
import com.ericsson.mssim.gsmb.packing.internal.PhErrorType;
import com.ericsson.commonlibrary.restorestack.RestoreCommandStack;


/**
 * @id GRAT-03.02:027
 * 
 * @name DetectedChannelRequest
 * 
 * @author GRAT Cell
 * 
 * @created 2015-02-09
 * 
 * @description Verify the channel request function by sending 
 *              one CHANNEL REQUEST message on the air interface 
 *              on each of the active RACH channels and check 
 *              that Channel Required is sent from BTS to BSC on 
 *              ABIS.
 * 
 * @revision eraweer 2015-02-09 First version.
 *           See Git for version history.
 *           
 */

public class DetectedChannelRequest extends TestBase {
    private static Logger logger = Logger.getLogger(DetectedChannelRequest.class);

    private NodeStatusHelper nodeStatus;
    private Gsmb gsmb;
    private AbisHelper abisHelper; 
    private AbiscoConnection abisco;
    private BG bgServer;
    private RestoreCommandStack restoreStack;
    private DeActivateForwardChReqToBG DeActivateForwardChReqToBGRestoreStackCmd;
    private MssimHelper mssimHelper;
    private GsmbHelper gsmbHelper;
    private MomHelper momHelper;

	private CliCommands cliCommands;
    
    /**
     * Description of test case for test reporting
     */
    
	@TestInfo(
            tcId = "GRAT-03.02:027",
            slogan = "Detected Channel Request",
            requirementDocument = "1/00651-FCP 130 1402",
            requirementRevision = "PC5",
            requirementLinkTested = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff87c1d941?docno=1/00651-FCP1301402Uen&format=excel8book",
            requirementLinkLatest = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff86a2af3d?docno=1/00651-FCP1301402Uen&action=current&format=excel8book",
            requirementIds = { "10565-0334/21767[PA1][PREL]", "10565-0334/19034[A][APPR]",
            "10565-0334/19417[A][APPR]" },
            verificationStatement = "Verifies GRAT-03.02:027",
            testDescription = "Verifies the channel request function.",
            traceGuidelines = "N/A")

    /**
     * Precond.
     */
    @Setup
    public void setup() {
        setTestStepBegin("Setup");
        nodeStatus = new NodeStatusHelper();
        assertTrue("Node did not reach a working state before test start", nodeStatus.waitForNodeReady());
        momHelper = new MomHelper();
        abisco = new AbiscoConnection();
        abisHelper = new AbisHelper();
        bgServer = abisHelper.getBG();
        restoreStack = Helpers.restore().restoreStackHelper().getRestoreStack();
        gsmb = Rm.getMssimList().get(0).getGsmb();
        mssimHelper = new MssimHelper(gsmb);
        gsmbHelper = new GsmbHelper(gsmb);
        cliCommands = new CliCommands();
        setTestStepEnd();
    }

    /**
     * Postcheck.
     */
    @Teardown
    public void teardown() {
        setTestStepBegin("Teardown");
        nodeStatus.isNodeRunning();
        setTestStepEnd();
    }
    
    
    /**
     * @name DetectedChannelRequest
     * 
     * @description Verify the channel request function.
     *
     * @param testId - String - Unique identifier of the test case.
     * @param description - String - Brief description of test case.
     * @param useDefaultSector - Boolean - Defines if default sectors shall be used or not.
     * @throws JSONException 
     * @throws InterruptedException 
     */
    @Test (timeOut = 1800000)
    @Parameters({ "testId", "description", "useDefaultSector" })
    public void detectedChannelRequestTest(String testId, String description, boolean useDefaultSector)
    		throws InterruptedException, JSONException {

        final short CELL = mssimHelper.getMssimCellToUse();  // Precondition: One cell configure in the MSSIM.
        
    	setTestCase(testId, description);    	
    	
    	// Connect to MS-SIM
       	assertTrue("Failed to initiate MSSIM", gsmbHelper.mssimInit(getCurrentTestCaseName(), true));
              
        // Setup TestCase
        setTestStepBegin("Create MOs and setup Abisco");
        List<TgLdns> tgLdnsList = momHelper.createUnlockAllGratMos(1, 1, useDefaultSector);
        abisco.setupAbisco(1, 1 , false);
        abisHelper.completeStartup(tgLdnsList);
        assertEquals("Node did not reach correct state after Complete Start", "", momHelper.checkAllGratMosAfterCompleteStart(1, 1));
        setTestStepEnd();
        
        logger.info(cliCommands.cliCommand("\n" + "rhdc icmiqcx_stats"));
        // Send gsmbSrvCONFIG command to MsSim
        setTestStepBegin("Configure the MsSim"); 
        assertTrue("Failed to define cell in MSSIM", gsmbHelper.mssimDefineCell());
        setTestStepEnd();
        
        // Send one CHANNEL REQUEST message on the air interface on 
        // each of the active RACH channels.
        // Check that Channel Required is sent from BTS to BSC on ABIS
        
        setTestStepBegin("Perform test");

        // activate forwarding of ChannelRequest
        DeActivateForwardChReqToBGRestoreStackCmd = new DeActivateForwardChReqToBG(abisHelper.getPARAMDISP());
        restoreStack.add(DeActivateForwardChReqToBGRestoreStackCmd);
        abisHelper.activateForwardChReqToBG();
        
        // Create message queue
    	MessageQueue<ChannelRequired> channelRequiredQueue = bgServer.getChannelRequiredQueue();
        
        // Clear list of indications
        java.util.List<Indication> listOfIndications = gsmb.getIndications();
        listOfIndications.clear();
     
        // Wait for indications to show up, at least sysInfo should be received...
        boolean indsReceived = false;
        int nrOfTries = 0;
        int MAX_TRIES = 10;
        while (!indsReceived && nrOfTries < MAX_TRIES+1){
            nrOfTries++;
            sleepSeconds(6);
        
            listOfIndications = gsmb.getIndications();
            setTestInfo("Nr of Inds received: " + listOfIndications.size());
            for(Indication myInd: listOfIndications) {
                indsReceived = true;
                setTestInfo("Indication received: " + myInd.stringRepresentation());
            }
        }
        listOfIndications.clear();

        // Send MSSIM.gsmphMPH_CCCH_DATA_REQ
        GsmphMPH_CCCH_DATA_REQ data_REQ = GsmbFactory.getGsmphMPH_CCCH_DATA_REQBuilder(CELL).build();
        Confirmation confirmation = gsmb.send(data_REQ);
        assertEquals("gsmphMPH_CCCH_DATA_REQ confirmation error", PhErrorType.GSM_PH_ENOERR, confirmation.getErrorType());

        // Verify that a Channel Required has been sent from BTS to BSC on ABIS        
        setTestInfo("ABISCO: Waiting for ChannelRequired");

        try        
	    {
	        AbiscoResponse msg;
	      	msg = channelRequiredQueue.poll(10, TimeUnit.SECONDS);

	        if (msg == null) 
	        {
	        	setTestInfo("ChannelRequired queue empty");
	        	setTestInfo("No ChannelRequired found!");
	        	logger.info("\n" + cliCommands.cliCommand("rhdc icmiqcx_stats"));
	           	fail("ABISCO: Did not receive ChannelRequired");
	        }
	        else
	        {
	        	setTestInfo("ABISCO: Recieved ChannelRequired");
	        	setTestInfo(msg.toString());
	        }
	    } catch (Exception e) {
	    	setTestInfo(e.toString());
	        e.printStackTrace();
	        fail("Exception occurred while waiting for ChannelRequired");
	    }  

        setTestStepEnd();

        // Cleanup
        setTestStepBegin("Cleanup");

        // Deactivate forwarding of ChannelRequest
        abisHelper.deActivateForwardChReqToBG();
        restoreStack.remove(DeActivateForwardChReqToBGRestoreStackCmd);
        setTestStepEnd();
        
        // MOs removed by RestoreStack
    }
}
