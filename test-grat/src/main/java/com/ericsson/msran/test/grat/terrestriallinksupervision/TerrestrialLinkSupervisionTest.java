package com.ericsson.msran.test.grat.terrestriallinksupervision;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.ericsson.msran.jcat.TestBase;
import se.ericsson.jcat.fw.annotations.Setup;
import se.ericsson.jcat.fw.annotations.Teardown;

import com.ericsson.abisco.clientlib.MessageQueue;
import com.ericsson.abisco.clientlib.servers.BG;
import com.ericsson.abisco.clientlib.servers.BG.ConnectionFailureIndication;
import com.ericsson.abisco.clientlib.servers.BG.G31OperationalCondition;
import com.ericsson.abisco.clientlib.servers.BG.G31StatusUpdate;

import com.ericsson.abisco.clientlib.servers.BG.Enums.MOState;
import com.ericsson.abisco.clientlib.servers.OM_G31R01.Enums.Combination;

import com.ericsson.abisco.clientlib.servers.OM_G31R01;

import com.ericsson.abisco.clientlib.servers.TRAFFRN.ChannelActAck;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.ChannelActNegAckException;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.ChannelActNormalAssign;

import com.ericsson.msran.g2.annotations.TestInfo;
import com.ericsson.msran.test.grat.testhelpers.AbisHelper;
import com.ericsson.msran.test.grat.testhelpers.MomHelper;
import com.ericsson.msran.test.grat.testhelpers.NodeStatusHelper;
import com.ericsson.msran.test.grat.testhelpers.prepost.AbisPrePost;

/**
 * @id NodeUC629
 * 
 * @name TerrestrialLinkSupervisionTest
 * 
 * @author Kalle Kula
 * 
 * @created 2014-10-03
 * 
 * @description This test class verifies Terrestrial Link Fault
 * 
 * 
 */

public class TerrestrialLinkSupervisionTest extends TestBase {
    private final OM_G31R01.Enums.MOClass moClassTf = OM_G31R01.Enums.MOClass.TF;
    private final OM_G31R01.Enums.MOClass moClassTs = OM_G31R01.Enums.MOClass.TS;
    
    private static final Logger myLogger = Logger.getLogger(TerrestrialLinkSupervisionTest.class);
    private AbisPrePost abisPrePost;
    private AbisHelper abisHelper;
    private MomHelper momHelper;
    private BG bgServer;

   
    private NodeStatusHelper nodeStatus;

    /**
     * Description of test case for test reporting
     */
    @TestInfo(
            tcId = "NodeUC629",
            slogan = "Detect and Report Abis Terrestrial Link Fault",
            requirementDocument = "234/155 56-HRB 105 700",
            requirementRevision = "fixme",
            requirementLinkTested = "fixme",
            requirementLinkLatest = "fixme",
            requirementIds = { "fixme ??[??][??]"},
            verificationStatement = "Verifies NodeUC629.N",
            testDescription = "Verifies Detect and Report Abis Terrestrial Link Fault",
            traceGuidelines = "fixme N/A")


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
        
        abisHelper  = new AbisHelper();
        momHelper   = new MomHelper();
        abisPrePost = new AbisPrePost();
        bgServer    = abisHelper.getBG();
        
        /**
         * @todo Create TRX MO when postconditions for it can be verified (sync is ok).
         */
        
        /**
         * @todo Verify Trx MO pre and post cond when we have sync working
         */        
        
        abisPrePost.preCondAllMoStateStarted();
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
     * @name enableRequestAoTf
     * 
     * @description Verifies Enable Request EP for AO TF
     * 
     * @throws InterruptedException
     */

    public void enableRequestAoTf() throws InterruptedException {

    	OM_G31R01.EnableResult result = abisHelper.enableRequest(this.moClassTf, 0);
    	assertEquals("MO state (" + result.getMOState().toString() + ") is not DISABLED", result.getMOState(), OM_G31R01.Enums.MOState.ENABLED);
    }
    
    /**
     * @name enableRequestAoTs
     * 
     * @description Verifies Enable Request EP for AO TS 
     * 
     * @param tsInstance - unique identifier
     * @param cc - channel combination
     *
     * @throws InterruptedException
     * @throws JSONException 
     */

    public void enableRequestAoTs(int tsInstance, Combination cc) throws InterruptedException, JSONException {

        int associatedSoInstance = 0;
        OM_G31R01.EnableResult result;
        
        OM_G31R01.Enums.Combination combination;
 
        setTestStepBegin("Verify that MO State for instance "+ tsInstance + " is DISABLED");  
        assertTrue("abisTsMoState for tsInstance " + tsInstance + " is not DISABLED", momHelper.waitForAbisTsMoState(tsInstance, "DISABLED", 5));
        setTestStepEnd();
            
        setTestStepBegin("Configure AO TS instance " + tsInstance);
        combination = OM_G31R01.Enums.Combination.TCH;

        switch (cc)
        {
        case MainBCCH:// cc_iv
            combination = OM_G31R01.Enums.Combination.MainBCCH;
            break;
        case MainBCCHCombined:// cc_v
            combination = OM_G31R01.Enums.Combination.MainBCCHCombined;
            break;
        case TCH:// cc_i
            combination = OM_G31R01.Enums.Combination.TCH;
            break;
        case SDCCH:// cc_vii
            combination = OM_G31R01.Enums.Combination.SDCCH;
            break;
        default :
            break;
        }
        
        /* tsInstance is equal to tn in this test*/
        OM_G31R01.TSConfigurationResult confRes = abisHelper.tsConfigRequest(tsInstance, tsInstance, tsInstance, false, combination);
        assertEquals("AccordanceIndication is not AccordingToRequest", 
                     OM_G31R01.Enums.AccordanceIndication.AccordingToRequest,
                     confRes.getAccordanceIndication().getAccordanceIndication());
        setTestStepEnd();

        setTestStepBegin("Send Enable Request to AO TS instance " + tsInstance);
        result = abisHelper.enableRequest(this.moClassTs, tsInstance, associatedSoInstance);
        setTestStepEnd();

        setTestStepBegin("Verify that MO State in Enable Result is ENABLED");
        assertEquals("MO state is not ENABLED", OM_G31R01.Enums.MOState.ENABLED, result.getMOState());
        setTestStepEnd();

        setTestStepBegin("Verify that MO:Trx attribute:abisTsMoState for instance " + tsInstance + " is ENABLED");
        assertTrue("abisTsMoState for tsInstance " + tsInstance + " is not ENABLED", momHelper.waitForAbisTsMoState(tsInstance, "ENABLED", 5));
        setTestStepEnd();
    }
    
    /**
     * @name disableRequestAoTs
     * 
     * @description Verifies Enable Request EP for AO TS 
     * 
     * @param tsInstance - unique identifier
     *
     * @throws InterruptedException
     * @throws JSONException 
     */

    public void disableRequestAoTs(int tsInstance) throws InterruptedException, JSONException {

        int associatedSoInstance = 0;
        OM_G31R01.DisableResult result;
        
        setTestStepBegin("Send Disable Request to AO TS instance " + tsInstance);
        result = abisHelper.disableRequest(this.moClassTs, tsInstance, associatedSoInstance);
        setTestStepEnd();

        setTestStepBegin("Verify that MO State in Disable Result is DISABLED");
        assertEquals("MO state is not DISABLED", OM_G31R01.Enums.MOState.DISABLED, result.getMOState());
        setTestStepEnd();

        setTestStepBegin("Verify that MO:Trx attribute:abisTsMoState for instance " + tsInstance + " is DISABLED");
        assertTrue("abisTsMoState for tsInstance " + tsInstance + " is not DISABLED", momHelper.waitForAbisTsMoState(tsInstance, "DISABLED", 5));
        setTestStepEnd();
    }

    /**
     * @name resetRequestAoTs
     * 
     * @description Verifies Reset Request EP for AO TS 
     * 
     * @param tsInstance - unique identifier
     *
     * @throws InterruptedException
     * @throws JSONException 
     */
    public void resetRequestAoTs(int tsInstance) throws InterruptedException, JSONException { 
        
    	int associatedSoInstance = 0;
        
        setTestStepBegin("Send Reset Request to AO TS instance " + tsInstance);
        abisHelper.resetCommand(this.moClassTs, tsInstance, associatedSoInstance);
        setTestStepEnd();
    } 

    /**
     * @name startRequestAoTs
     * 
     * @description Verifies Start Request EP for AO TS 
     * 
     * @param tsInstance - unique identifier
     *
     * @throws InterruptedException
     * @throws JSONException 
     */
    public void startRequestAoTs(int tsInstance) throws InterruptedException, JSONException { 
        
    	int associatedSoInstance = 0;
        
        setTestStepBegin("Send start Request to AO TS instance " + tsInstance);
        abisHelper.startRequest(this.moClassTs, tsInstance, associatedSoInstance);
        setTestStepEnd();
    }

    /**
     * @name channelActivationNormalAssignment
     * 
     * @description Verifies channelActivationNormalAssignment 
     * 
     * @param tn - timeslot number
     * 
     * @throws InterruptedException
     */
    public void channelActivationNormalAssignment (int tn) throws InterruptedException {
    	
    	ChannelActNormalAssign msg;
    	msg = abisHelper.channelActivationNormalAssignment (tn); 
    	try {
    		myLogger.debug(msg.toString());

            ChannelActAck ack = msg.send();
	        myLogger.debug(ack.toString());
			
		} catch (ChannelActNegAckException e) {
	        myLogger.debug(e.toString());
			e.printStackTrace();
            fail("Error: ChannelActNegAckException occured");
		}
    }
    
   /**
    * @name connectionFailureIndication
    * 
    * @description Verifies connectionFailureIndication 
    * 
    * @param tn - timeslot number
    */
    public void connectionFailureIndication (int tn) {

      int i=0;
      int tn_rec;
      
      MessageQueue<ConnectionFailureIndication> connectionFailureQueue = bgServer.getConnectionFailureIndicationQueue();

      try        
      {
          ConnectionFailureIndication ConnectionFailureIndication;
          while (i < 1)
          {
              ConnectionFailureIndication = connectionFailureQueue.poll(5, TimeUnit.SECONDS);
              
              if (ConnectionFailureIndication == null) 
              {
                  fail("Connection Failure queue shouldn't be empty");
                  break;
              }
              
              tn_rec = ConnectionFailureIndication.getChannelNoStruct().getTimeSlotNo();
              assertEquals("Wrong Timeslot numer received =" + tn_rec + "expected =" + tn , tn, tn_rec);

              myLogger.debug(ConnectionFailureIndication.toString());

              i++;
          }
      } catch (Exception e) {
          myLogger.debug(e.toString());
          e.printStackTrace();
          fail("Connection Failure exception");
      }

    }

    /**
     * @name verifyTsInstanceInTsStatusList
     * 
     * @description Verifies the TS instance part of the tsStatusList 
     */
    public int verifyTsInstanceInTsStatusList(List<Integer> tsStatusList, int currentPosition, int expectedTSInstance) {

    	final int TS_INSTANCE_DEI = 129;
    	final int NO_OCTETS_TO_FOLLOW = 1;

    	assertEquals("Next DEI code in message is not what expected!", new Integer(TS_INSTANCE_DEI), tsStatusList.get(currentPosition));
    	assertEquals("Number of octets that follow at position: " + currentPosition+1 +" is not what expected!", new Integer(NO_OCTETS_TO_FOLLOW), tsStatusList.get(currentPosition+1));
    	assertEquals("TS instance is not what expected!", new Integer(expectedTSInstance), tsStatusList.get(currentPosition+2));
    	return (currentPosition+3);
    }

    /**
     * @name verifyMoStateInTsStatusList
     * 
     * @description Verifies the MO state part of the tsStatusList 
     */
    public int verifyMoStateInTsStatusList(List<Integer> tsStatusList, int currentPosition, MOState expectedMoState) {

    	final int MO_STATE_DEI = 85;
    	final int NO_OCTETS_TO_FOLLOW = 1;    	
    	
    	assertEquals("Next DEI code in message is not what expected!", new Integer(MO_STATE_DEI), tsStatusList.get(currentPosition));
    	assertEquals("Number of octets that follow at position: " + currentPosition+1 +" is not what expected!", new Integer(NO_OCTETS_TO_FOLLOW), tsStatusList.get(currentPosition+1));
    	assertEquals("MO state is not what expected!", new Integer(expectedMoState.getValue()), tsStatusList.get(currentPosition+2));
    	return (currentPosition+3);
    }

    /**
     * @name verifyOperCondInTsStatusList
     * 
     * @description Verifies the Operational Condition part of the tsStatusList 
     */
    public int verifyOperCondInTsStatusList(List<Integer> tsStatusList, int currentPosition, BG.Enums.OperationalCondition expectedOpCond) {

    	final int OPER_COND_DEI = 95;
    	final int NO_OCTETS_TO_FOLLOW = 1;

    	assertEquals("Next DEI code in message is not what expected!", new Integer(OPER_COND_DEI), tsStatusList.get(currentPosition));
    	assertEquals("Number of octets that follow at position: " + currentPosition+1 +" is not what expected!", new Integer(NO_OCTETS_TO_FOLLOW), tsStatusList.get(currentPosition+1));
    	assertEquals("OperationalCondition is not what expected!", new Integer(expectedOpCond.getValue()), tsStatusList.get(currentPosition+2));
    	return (currentPosition+3);
    }

    /**
     * @name verifyOperCondReasonInTsStatusList
     * 
     * @description Verifies the Operational Condition Reason part of the tsStatusList 
     */
    public int verifyOperCondReasonsMapInTsStatusList(List<Integer> tsStatusList, int currentPosition) {

    	final int OPER_COND_DEI_REASON = 96;
    	final int NO_OCTETS_TO_FOLLOW = 1;
    	final int OPER_COND_REASON_MAP = 48;

    	assertEquals("Next DEI code in message is not what expected!", new Integer(OPER_COND_DEI_REASON), tsStatusList.get(currentPosition));
    	assertEquals("Number of octets that follow at position: " + currentPosition+1 +" is not what expected!", new Integer(NO_OCTETS_TO_FOLLOW), tsStatusList.get(currentPosition+1));
    	assertEquals("OperationalConditionReasonsMap is not what expected!", new Integer(OPER_COND_REASON_MAP), tsStatusList.get(currentPosition+2));
    	return (currentPosition+3);
    }
    
    /**
     * @name verifyConfigSigInTsStatusList
     * 
     * @description Verifies that remaining part of tsStatusList is big enough to fit a configuration signature
     */
    public int verifyConfigSigInTsStatusList(List<Integer> tsStatusList, int currentPosition) {
     
    	String tmpChar;
    	String tmpStr = new String("");
		int sizeOfRemainingTsStatusUpdateList = tsStatusList.size()-currentPosition;
		setTestDebug("Size of remaining message: " + sizeOfRemainingTsStatusUpdateList);	
		
		assertEquals("Remaining size of of TSs Status list is not big  enough to fit configuration signature!",
				true, (sizeOfRemainingTsStatusUpdateList >= 4));
		
		// For debug purpose show configuration signature
		for (int i=currentPosition; i<currentPosition+4 ; i++)
		{
			tmpChar = String.valueOf(tsStatusList.get(i));
			tmpStr = tmpStr + tmpChar + ',';
		}
		currentPosition = currentPosition+4;
		setTestDebug("Config signature in TSs Status list is: " + tmpStr);
		
		return currentPosition;
    }

    /**
     * @name verifyRemainingPartInTsStatusList
     * 
     * @description Verifies that remaining part of tsStatusList doesn't contain non zero characters
     */
    public void verifyRemainingPartInTsStatusList(List<Integer> tsStatusList, int currentPosition){
    	
    	String tmpChar;
    	String tmpStr;
    	int tmpInt;
    	boolean foundNonZeroCharacter = false;
    	
    	// Check size of remaining message. 
		if (tsStatusList.size()-currentPosition > 0)
		{
   			tmpStr = new String("");
   			setTestDebug("Start checking remaining content of TSs Status list from position: " + currentPosition);
			// For debug purpose show remaining content of tsStatusUpdateList
			for (int i=currentPosition; i<tsStatusList.size(); i++)
			{
				tmpInt = tsStatusList.get(i);
				tmpChar = String.valueOf(tmpInt);
				tmpStr = tmpStr + tmpChar + ',';
				
				if (tmpInt != 0)
				{
					foundNonZeroCharacter = true;
				}
			}
			setTestDebug("Remaining part of TS Status list: " + tmpStr);
			assertEquals("Found non zero character in remaining part of TSs Status list, which is not what exected!", false, foundNonZeroCharacter);
		}
    }
    
    /**
     * @throws InterruptedException 
     * @name verifyOneTsInTsStatusList
     * 
     * @description Verifies one TS in the TSsStatusList 
     */
    public int verifyOneTsInTsStatusList(List<Integer> tsStatusList, int currentPosition, int expectedTsInstance, 
    		MOState expectedMoState, BG.Enums.OperationalCondition expectedOpCond) throws InterruptedException {

    	final int MIN_SIZE_TS_STATUS_UPDATE_ONE_TS = 3+3+3+3+3;
    	int sizeOfRemainingTsStatusList;
    	
    	// Check length of remaining message
    	sizeOfRemainingTsStatusList = tsStatusList.size()-currentPosition;
    	assertEquals("Remaining tsStatusList is not big enough to hold data for one TS", 
    			true,  (sizeOfRemainingTsStatusList >= MIN_SIZE_TS_STATUS_UPDATE_ONE_TS));

    	// Check TS Instance part
    	setTestInfo("Verify TS instance");
    	currentPosition = verifyTsInstanceInTsStatusList(tsStatusList, currentPosition, expectedTsInstance);

    	// Check MO state part
    	setTestInfo("Verify that TS MO state is " + expectedMoState.toString());
    	currentPosition = verifyMoStateInTsStatusList(tsStatusList, currentPosition, expectedMoState);

    	// Check Operational Condition part
    	setTestInfo("Verify that TS Operational Condition is " + expectedOpCond.toString());
    	currentPosition = verifyOperCondInTsStatusList(tsStatusList, currentPosition, expectedOpCond);

    	// Check Operational Condition Reasons map
    	if (expectedOpCond == BG.Enums.OperationalCondition.NotOperational ||
    			expectedOpCond == BG.Enums.OperationalCondition.Degraded)
    	{
    	  setTestInfo("Verify Operational Condition Reason Map");
    	  currentPosition = verifyOperCondReasonsMapInTsStatusList(tsStatusList, currentPosition);
    	}
    	
    	currentPosition = verifyConfigSigInTsStatusList(tsStatusList, currentPosition);
    	return currentPosition;
    }

    /**
     * @throws InterruptedException 
     * @name TsStatusUpdates
     * 
     * @description Verifies the TSsStatusUpdate
     */
    public void TsStatusUpdates(MOState expectedMoState, BG.Enums.OperationalCondition expectedOpCond) throws InterruptedException {
    	// expect one status update from TS including up to 1 TS's

    	BG.G31TSsStatusUpdate tsStatusUpdate;
    	List<Integer> tsStatusList;
    	int noTsUpdatesReceived = 0;
    	int currentPosition = 0;

    	int expectedNoTsInTsStatusUpdate = 1;
    	int expectedTsInstance = 1;
    	
    	while ((tsStatusUpdate = abisHelper.getTsStatusUpdate(5, TimeUnit.SECONDS)) != null)
    	{       
    		noTsUpdatesReceived++;
    		if (tsStatusUpdate.getMOClass() == BG.Enums.MOClass.TRXC)
    		{	
    			tsStatusList = tsStatusUpdate.getTSsStatusData();
    			setTestDebug("Total length of TS Status list is: " + tsStatusList.size());

    			for (int i=0; i<expectedNoTsInTsStatusUpdate; i++)
    			{
    				currentPosition = verifyOneTsInTsStatusList(tsStatusList, currentPosition, expectedTsInstance, expectedMoState, expectedOpCond);
    			}
    			verifyRemainingPartInTsStatusList(tsStatusList, currentPosition);
    		}
    		else
    		{
    			assertEquals("Received unexpected status update, MO Class = ",  BG.Enums.MOClass.TRXC, tsStatusUpdate.getMOClass());
    		}
    	}
    	assertEquals("Did not receive exactly one TSs Status Update from AO TS.", 1, noTsUpdatesReceived);
    }

    /**
     * @throws InterruptedException 
     * @name TrxcStatusUpdates
     * 
     * @description Verifies the TrxcStatusUpdate
     */
    public void TrxcStatusUpdates(BG.Enums.OperationalCondition expectedOpCond) throws InterruptedException {
    	// expect one status update from Trxc 
    	G31StatusUpdate statusUpdate = null;
        while ((statusUpdate = abisHelper.getStatusUpdate(5, TimeUnit.SECONDS)) != null)
        {            
            if (statusUpdate.getMOClass() == BG.Enums.MOClass.TRXC)
            {
                setTestInfo("Verify that TRXC instanceNo=" + statusUpdate.getInstanceNumber() +
                        " Operational Condition is Degraded");
                G31OperationalCondition opCond = statusUpdate.getStatusChoice().getG31StatusTRXC().getG31OperationalCondition();
                assertEquals("OperationalCondition is not what expected!", expectedOpCond, opCond.getOperationalCondition());
            }
        }
    }

    /**
     * @name terrestrialLinkSupervision
     * 
     * @description verifies Terrestrial Link Supervision
     * 
     * @param testId - unique identifier
     * @param description
     * 
     * @throws InterruptedException
     * @throws JSONException
     */
    @Test (timeOut = 120000)
    @Parameters({ "testId", "description" })
    public void terrestrialLinkSupervision (String testId, String description) throws InterruptedException, JSONException {

        /* OBS! tsInstance is equal to tn in this test suite*/
        setTestCase(testId, description);

        setTestStepBegin("Enable AO TF");
    	enableRequestAoTf();
        setTestStepEnd();

        setTestStepBegin("Enable TS-1 for TCH");
        enableRequestAoTs(1, OM_G31R01.Enums.Combination.TCH);
        setTestStepEnd();

        sleepSeconds(1);

        setTestStepBegin("Channel Activation Normal Assignment");
     	channelActivationNormalAssignment (1);
        setTestStepEnd();
       
        sleepSeconds(1);

        setTestStepBegin("Connection Failure Indication");
        connectionFailureIndication (1);
        setTestStepEnd();
        
        setTestStepBegin("Receive TSs Status Updates");
        TsStatusUpdates(BG.Enums.MOState.Enabled, BG.Enums.OperationalCondition.NotOperational);
        setTestStepEnd();

        setTestStepBegin("Receive TRXC Status Updates");
        TrxcStatusUpdates(BG.Enums.OperationalCondition.Degraded);
        setTestStepEnd();
        
        sleepSeconds(1);
        
       	resetRequestAoTs(1);
       	sleepSeconds(1);
 
       	//a new status is reported from TS-1, fault is ceased.
        setTestStepBegin("Receive TSs Status Updates");
        TsStatusUpdates(BG.Enums.MOState.Reset, BG.Enums.OperationalCondition.Operational);
        setTestStepEnd();

        setTestStepBegin("Receive TRXC Status Updates");
        TrxcStatusUpdates(BG.Enums.OperationalCondition.Operational);
        setTestStepEnd();
        
       	startRequestAoTs(1);
       	
        setTestStepBegin("Enable TS-1 for TCH");
        enableRequestAoTs(1, OM_G31R01.Enums.Combination.TCH);
        setTestStepEnd();

        sleepSeconds(1);

        setTestStepBegin("Channel Activation Normal Assignment");
     	channelActivationNormalAssignment (1);
        setTestStepEnd();
       
        sleepSeconds(1);

        setTestStepBegin("Connection Failure Indication");
        connectionFailureIndication (1);
        setTestStepEnd();
        
        setTestStepBegin("Receive TSs Status Updates");
        TsStatusUpdates(BG.Enums.MOState.Enabled, BG.Enums.OperationalCondition.NotOperational);
        setTestStepEnd();

        setTestStepBegin("Receive TRXC Status Updates");
        TrxcStatusUpdates(BG.Enums.OperationalCondition.Degraded);
        setTestStepEnd();
        
        sleepSeconds(1);
        
        /* Clean up */
        disableRequestAoTs(1);
    }

}   
