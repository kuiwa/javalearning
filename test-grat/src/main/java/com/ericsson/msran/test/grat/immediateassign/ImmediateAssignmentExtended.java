package com.ericsson.msran.test.grat.immediateassign;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONException;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import se.ericsson.jcat.fw.annotations.Setup;
import se.ericsson.jcat.fw.annotations.Teardown;

import com.ericsson.abisco.clientlib.AbiscoServer.BaseEnums.EP1_BEH;
import com.ericsson.abisco.clientlib.AbiscoServer.Behaviour;
import com.ericsson.abisco.clientlib.servers.DISPATCH;
import com.ericsson.abisco.clientlib.servers.TRAFFRN;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.ChannelNoStruct;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.Enums.ChannelType;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.IARestOctetsStruct;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.ImmAssignInfoP2;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.MobileAllocationStruct;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.UNACKNOWLEDGED_MESSAGE_STOREDException;
import com.ericsson.commonlibrary.resourcemanager.Rm;
import com.ericsson.msran.g2.annotations.TestInfo;
import com.ericsson.msran.jcat.TestBase;
import com.ericsson.msran.test.grat.testhelpers.AbisHelper;
import com.ericsson.msran.test.grat.testhelpers.AbiscoConnection;
import com.ericsson.msran.test.grat.testhelpers.GsmbHelper;
import com.ericsson.msran.test.grat.testhelpers.MomHelper;
import com.ericsson.msran.test.grat.testhelpers.NodeStatusHelper;
import com.ericsson.msran.test.grat.testhelpers.TgLdns;
import com.ericsson.mssim.gsmb.Gsmb;
import com.ericsson.mssim.gsmb.GsmphMPH_CCCH_DATA_IND;
import com.ericsson.mssim.gsmb.Indication;

/**
 * @id GRAT-03.01:14
 * 
 * @name ImmediateAssignmentExtended
 * 
 * @author GRAT Cell
 * 
 * @created 2015-04-28
 * 
 * @description Verify the Immediate Assignment Extended function.
 * 
 * @revision eraweer 2015-04-28 First version.
 *           See Git for version history.
 *           
 */

public class ImmediateAssignmentExtended extends TestBase {
    private NodeStatusHelper nodeStatus;
    private Gsmb gsmb;
    private AbisHelper abisHelper;
    private AbiscoConnection abisco;
    private GsmbHelper gsmbHelper;
    private MomHelper momHelper;
    
    /**
     * Description of test case for test reporting
     */
    @TestInfo(
            tcId = "GRAT-03.01:14",
            slogan = "Immediate Assignment Extended",
            requirementDocument = "1/00651-FCP 130 1402",
            requirementRevision = "PC5",
            requirementLinkTested = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff87c1d941?docno=1/00651-FCP1301402Uen&format=excel8book",
            requirementLinkLatest = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff86a2af3d?docno=1/00651-FCP1301402Uen&action=current&format=excel8book",
            requirementIds = { "10565-0334/21767[PA1][PREL]", "10565-0334/19034[A][APPR]",
            "10565-0334/19417[A][APPR]" },
            verificationStatement = "Verifies GRAT-03.01:14",
            testDescription = "Verifies the Immediate Assignment Extended function.",
            traceGuidelines = "N/A")

    /**
     * Precond.
     */
    @Setup
    public void setup() {
        setTestStepBegin("Setup");
        nodeStatus = new NodeStatusHelper();
        assertTrue("Node did not reach a working state before test start", nodeStatus.waitForNodeReady());
        abisHelper = new AbisHelper();
        abisco = new AbiscoConnection();
        momHelper = new MomHelper();
        gsmb = Rm.getMssimList().get(0).getGsmb();
        gsmbHelper = new GsmbHelper(gsmb);
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
     * @name ImmediateAssignmentExtended
     * 
     * @description Verify the Immediate Assignment Extended function.
     *
     * @param testId - String - Unique identifier of the test case.
     * @param description - String - Brief description of test case.
     * @param useDefaultSector - Boolean - Defines if default sectors shall be used or not.
     * @throws JSONException 
     * @throws InterruptedException 
     */
    @Test (timeOut = 1800000)
    @Parameters({ "testId", "description", "useDefaultSector" })
    public void immediateAssignmentExtendedTest(String testId, String description, boolean useDefaultSector)
    		throws InterruptedException, JSONException {

        final short IMMEDIATE_ASSIGNMENT_EXTENDED_MESSAGE_TYPE = 57;
        final short IMMEDIATE_ASSIGNMENT_MESSAGE_TYPE = 63;
        final short EXPECTED_PAGE_MODE = 0;
                
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
        
        setTestStepBegin("Configure the MsSim"); 
        assertTrue("Failed to define cell in MSSIM", gsmbHelper.mssimDefineCell());
        
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

        setTestStepEnd();

        setTestStepBegin("Perform test");
        
        // Actions:
        // Send two IMMEDIATE ASSIGN COMMAND on Abis.
        // The element 'Page Mode' is set to 'Normal'.
        // Their mobile allocation fields must be identical and the length of the mobile allocation fields must not exceed five (5) octets.
        // The 'IA Rest Octets' field must not contain any information.
  	    // In order to make the two messages part of a two-message assignment, 'SpareBits' needs to be set.
  	    // See 3GPP 44.018 Ch 10.5.2.25b  'Dedicated mode or TBF'
  	    // First message:  SpareBits => 0b0111 = 7
  	    // Second message: SpareBits => 0b0001 = 1 
        //
        // Do this twice to make at least two of the four signals be received within same time frame in the BTS.
   
        TRAFFRN traffrn = abisHelper.getRslServer();
        ImmAssignInfoP2 immAssignInfoP2 = traffrn.createImmAssignInfoP2();

        // Set Behaviour
        Behaviour myBehaviour = new Behaviour();
        myBehaviour.setEP1_BEH(EP1_BEH.TXQUEUE);
        immAssignInfoP2.setBehaviour(myBehaviour);
        
        // Set PageMode
        immAssignInfoP2.setPageMode(TRAFFRN.Enums.PageMode.Normal);
        
        // Set MobileAllocationStruct
        MobileAllocationStruct myMobileAllocationStruct = new MobileAllocationStruct();
        List<Integer> myList = new ArrayList<Integer>(Arrays.asList(0));
        myMobileAllocationStruct.setMA_C(myList);
        immAssignInfoP2.setMobileAllocationStruct(myMobileAllocationStruct);        
        
        // Set IARestOctetsStruct
        List<Integer> myIaList = new ArrayList<Integer>(Arrays.asList(43, 43, 43, 43, 43, 43, 43, 43, 43, 43));        
        IARestOctetsStruct myIARestOctetsStruct = new IARestOctetsStruct(myIaList);
        immAssignInfoP2.setIARestOctetsStruct(myIARestOctetsStruct);
        
        // Set Channel Type
        ChannelNoStruct channelNoStruct = new ChannelNoStruct();
        channelNoStruct.setTimeSlotNo(0);
        channelNoStruct.setChannelType(ChannelType.CCCH_D);
        immAssignInfoP2.setChannelNoStruct(channelNoStruct);
        
        // Send two sets of immediate assign commands
        for(int i=0; i < 2; ++i) {
        	// Set SpareBits (first message)
        	immAssignInfoP2.setSpareBits(7);
        
        	// Send first IMMEDIATE ASSIGN COMMAND of two
        	try { 
        		immAssignInfoP2.send();
        	} catch (UNACKNOWLEDGED_MESSAGE_STOREDException ums) {
        		setTestInfo("Catched UNACKNOWLEDGED_MESSAGE_STOREDException");
        	}
        
        	// Update spare bits (second message)
        	immAssignInfoP2.setSpareBits(1);
        
        	// Send second IMMEDIATE ASSIGN COMMAND of two
        	try { 
        		immAssignInfoP2.send();
        	} catch (UNACKNOWLEDGED_MESSAGE_STOREDException ums) {
        		setTestInfo("Catched UNACKNOWLEDGED_MESSAGE_STOREDException");
        	}
        }
        
        setTestInfo("Sending four Immediate Assign Commands");
        
        DISPATCH dispatch = abisHelper.getDISPATCH();
        DISPATCH.SendStoredMessages sendStoredMessages = dispatch.createSendStoredMessages();
        sendStoredMessages.setIssueUNACK(DISPATCH.Enums.IssueUNACK.No);
        sendStoredMessages.sendAsync();
        
        // Verify that either two IMMEDIATE ASSIGNMENT EXTENDED messages are sent on the air interface
        // where the element 'Page Mode' has the value 'Normal' OR one IMMEDIATE ASSIGNMENT EXTENDED message (with normal Page Mode)
        // and two normal IMMEDIATE ASSIGNMENTS.
        // No response on Abis.
        
        setTestInfo("Trying to read MS-SIM gsmphMPH_CCCH_DATA_IND message");

        // Wait for MS-SIM
        gsmb.delay(5);
        
        // Fetch indications
        setTestInfo("Fetch indications");
        listOfIndications = gsmb.getIndications();
        
        // Parse list of indications to find the IMMEDIATE ASSIGNMENT EXTENDED message(s)
        int immediateAssignmentExtendedFound = 0;
        int immediateAssignmentFound = 0;
        setTestInfo("Parse list of indications to find the IMMEDIATE ASSIGNMENT EXTENDED message(s)");
        
        for(Indication myInd: listOfIndications) {
            if(myInd instanceof GsmphMPH_CCCH_DATA_IND) {            	            	
            	// Message is of the correct type. Now check the contents:
        	    // Check that Message Type is 'Immediate Assignment Extended' (57) and that PageMode is 'Normal' (0)
        	    // PageMode is only 4 bit => masking with 0x0F
            	short [] dataVec = ((GsmphMPH_CCCH_DATA_IND) myInd).getData();
            	
            	if(dataVec.length >= 4) {
            		if (dataVec[2] == IMMEDIATE_ASSIGNMENT_EXTENDED_MESSAGE_TYPE && (dataVec[3]&0x0F) == EXPECTED_PAGE_MODE) {
            			immediateAssignmentExtendedFound++;
            		}
            		else if(dataVec[2] == IMMEDIATE_ASSIGNMENT_MESSAGE_TYPE && (dataVec[3]&0x0F) == EXPECTED_PAGE_MODE) {
            	        immediateAssignmentFound++;
            		}
            		else {
            		    setTestWarning("An incorrect message was received over Um. Received message type: (" + dataVec[2] + ") Expected: (" + 
            		            	   IMMEDIATE_ASSIGNMENT_EXTENDED_MESSAGE_TYPE + ") or (" + IMMEDIATE_ASSIGNMENT_MESSAGE_TYPE +
            		            	   "), Received page mode: (" + (dataVec[3]&0x0F) + ") Expected: (" + EXPECTED_PAGE_MODE + ")");
            		}
            	}
            	else {
            		setTestWarning("GsmphMPH_CCCH_DATA_IND received but with wrong size: (" + dataVec.length + ")");
            	}
            }
        }

        // Evaluate test result
        setTestInfo("Evaluate test result");
        
        if(immediateAssignmentExtendedFound == 2 && immediateAssignmentFound == 0) {
          	setTestInfo("Passed. Two Immediate Assignment Extended were sent on the air interface as expected.");
        }
        else if (immediateAssignmentExtendedFound == 1 && immediateAssignmentFound == 2) {
        	setTestInfo("Passed. One Immediate Assignment Extended and two Immediate Assignments were sent on the air interface.");
        }
        else {
        	setTestInfo(immediateAssignmentExtendedFound + " Immediate Assignment Extended signal(s) received.");
        	setTestInfo(immediateAssignmentFound + " Immediate Assignment signal(s) received.");
        	fail("Failed to receive the expected number of messages");
        }
        
        setTestStepEnd();
        // MOs removed by RestoreStack
    }
}