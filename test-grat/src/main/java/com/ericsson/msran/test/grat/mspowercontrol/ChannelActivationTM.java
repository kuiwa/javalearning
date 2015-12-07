package com.ericsson.msran.test.grat.mspowercontrol;

import java.util.concurrent.TimeUnit;

import org.json.JSONException;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.ericsson.msran.jcat.TestBase;
import se.ericsson.jcat.fw.annotations.Setup;
import se.ericsson.jcat.fw.annotations.Teardown;

import com.ericsson.abisco.clientlib.MessageQueue;
import com.ericsson.abisco.clientlib.servers.BG;
import com.ericsson.abisco.clientlib.servers.BG.ChannelNoStruct;
import com.ericsson.abisco.clientlib.servers.BG.Enums;
import com.ericsson.abisco.clientlib.servers.BG.MeasurementResult;
import com.ericsson.abisco.clientlib.servers.PARAMDISP;
import com.ericsson.abisco.clientlib.servers.PARAMDISP.ReleaseAllCalls_Result;
import com.ericsson.abisco.clientlib.servers.TRAFFRN;
import com.ericsson.abisco.clientlib.servers.BG.SetMeasurementReporting;
import com.ericsson.abisco.clientlib.servers.PARAMDISP.MSPowerControl;
import com.ericsson.abisco.clientlib.servers.PARAMDISP.SetupSubStruct;
import com.ericsson.abisco.clientlib.servers.PARAMDISP.TrafficCommand_Result;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.ChannelModeStruct;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.Enums.ChannelType;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.MSPowerStruct;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.SACCHFilling5;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.Enums.SystemInfoType;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.SACCHFilling6;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.UNACKNOWLEDGED_MESSAGE_SENTException;
import com.ericsson.commonlibrary.resourcemanager.Rm;
import com.ericsson.commonlibrary.resourcemanager.g2.G2Rbs;
import com.ericsson.commonlibrary.restorestack.RestoreCommandStack;
import com.ericsson.msran.g2.annotations.TestInfo;
import com.ericsson.msran.helpers.Helpers;
import com.ericsson.msran.test.grat.testhelpers.AbisHelper;
import com.ericsson.msran.test.grat.testhelpers.AbiscoConnection;
import com.ericsson.msran.test.grat.testhelpers.ImsiHelper;
import com.ericsson.msran.test.grat.testhelpers.MomHelper;
import com.ericsson.msran.test.grat.testhelpers.NodeStatusHelper;
import com.ericsson.msran.test.grat.testhelpers.prepost.AbisPrePost;
import com.ericsson.msran.test.grat.testhelpers.prepost.PidPrePost;
import com.ericsson.msran.test.grat.testhelpers.restorecommands.ReleaseAllCallsCommand;

/**
 * @id ChannelActivation
 * 
 * @name ChannelActivation
 *
 * @description This test class verifies MS Power Control at Channel Activation
 */

public class ChannelActivationTM extends TestBase {
    private AbisPrePost abisPrePost;
    private PidPrePost pidPrePost;
    private AbisHelper abisHelper;
    private TRAFFRN rslServer;
    private G2Rbs rbs;
    private NodeStatusHelper nodeStatus;

    private PARAMDISP paramdisp;
    private BG bgServer;
    private SetMeasurementReporting setMeasurementReporting;
    private RestoreCommandStack restoreStack;
    private ReleaseAllCallsCommand releaseAllCallsRestoreStackCmd;
    
    private final int MinMSPower = 13;
    private final int LowMSPower = 10;
    private final int MedMSPower = 7;
    private final int HighMSPower = 3;
    private final int MaxMSPower = 0;

    
    /**
     * Description of test case for test reporting
     */
    @TestInfo(
            tcId = "MS Power Control Channel Activation",
            slogan = "MS Power Control Channel Activation",
            requirementDocument = "1/00651-FCP 130 1402",
            requirementRevision = "PC5",
            requirementLinkTested = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff87c1d941?docno=1/00651-FCP1301402Uen&format=excel8book",
            requirementLinkLatest = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff86a2af3d?docno=1/00651-FCP1301402Uen&action=current&format=excel8book",
            requirementIds = { "10565-0334/21767[PA1][PREL]", "10565-0334/19034[A][APPR]",
                    "10565-0334/19417[A][APPR]" },
            verificationStatement = "Verifies MS Power Control at Channel Activation",
            testDescription = "Sets up a call with an explicit value of MS Power that is not equal to default value, and measures that MS Power has been set on MS.",
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
        rbs.getLinuxShell();
        assertTrue("Node did not reach a working state before test start", nodeStatus.waitForNodeReady());

        abisHelper = new AbisHelper();
        new MomHelper();
        abisPrePost = new AbisPrePost();
        pidPrePost = new PidPrePost();
        paramdisp = abisHelper.getPARAMDISP();
        new AbiscoConnection();
        rslServer   = abisHelper.getRslServer();
        bgServer = abisHelper.getBG();
        restoreStack = Helpers.restore().restoreStackHelper().getRestoreStack();
        
        setMeasurementReporting =  bgServer.createSetMeasurementReporting();  
        
        pidPrePost.preCond();
        setTestStepEnd();
    }
    
    /**
     * Postcond.
     * @throws InterruptedException 
     */
    @Teardown
    public void teardown() throws InterruptedException {
    	setTestStepBegin("Teardown");
        nodeStatus.isNodeRunning();
        pidPrePost.postCond();
        setTestStepEnd();
    }
    

    
    /**
     * @name normalCase
     * 
     * @description Verifies MS Power Control at Channel Activation.
     * 
     * @param testId - unique identifier
     * @param description
     * 
     * @throws InterruptedException
     */
    @Test (timeOut = 1800000) // 30 minutes should be enough
    @Parameters({ "testId", "description" })
    public void normalCase(String testId, String description) throws InterruptedException, JSONException {
       abisPrePost.preCondAllMoStateStarted();
        assertTrue("enableTf failed", abisHelper.enableTf());
        assertTrue("enableTx failed", abisHelper.enableTx());
        assertTrue("enableRx failed", abisHelper.enableRx());
        assertTrue("enableTsBcch failed", abisHelper.enableTsBcch());
        assertTrue("enableTsSdcch failed", abisHelper.enableTsSdcch());
        assertTrue("enableTsTch failed", abisHelper.enableTsTch());
        abisHelper.sysInfo();
        
      sacchFilling5();
      sacchFilling6();
      
      releaseHangingCalls();

      
      try {
    	  abisHelper.enableAbisLogging(0); // necessary to be able to pull stuff from getMeasurementResultQueue
      }catch (Exception e){
    	  // Do nothing
      }
    	sleepSeconds(abisHelper.SECONDS_TO_WAIT_FOR_SYNC); //allows for some time (20s * 10) for MSSIM to attach/synchronize

    	int msPower = 4;
    	int ms = 1;
    	int tch_TS = 2;
    	int timeslot = callSetupMt(ms, msPower, tch_TS);
    	
    	float result = checkMeasurements(4, timeslot);
    	
		// Check change, accept +/- 1, plus measuring method with bad precision.
    	if(result > (msPower - 2) && (result < (msPower + 2)))
    		setTestInfo("MS Power Control at Channel Activation verifyed!");
    	else
    		fail("MS Power Control at Channel Activation failed.");	
   	
    	disconnectCallMt(); 
    }
    
    /**
     * @name normalCase
     * 
     * @description Verifies MS Power Control at Channel Activation.
     * 
     * @param testId - unique identifier
     * @param description
     * 
     * @throws InterruptedException
     */
    @Test (timeOut = 1800000) // 30 minutes should be enough
    @Parameters({ "testId", "description" })
    public void multiCallCase(String testId, String description) throws InterruptedException, JSONException {
       abisPrePost.preCondAllMoStateStarted();
        assertTrue("enableTf failed", abisHelper.enableTf());
        assertTrue("enableTx failed", abisHelper.enableTx());
        assertTrue("enableRx failed", abisHelper.enableRx());
        assertTrue("enableTsBcch failed", abisHelper.enableTsBcch());
        assertTrue("enableTsSdcch failed", abisHelper.enableTsSdcch());
        assertTrue("enableTsTch failed", abisHelper.enableTsTch());
        abisHelper.sysInfo();
        
      sacchFilling5();
      sacchFilling6();
      
      releaseHangingCalls();

      try {
    	  abisHelper.enableAbisLogging(0); // necessary to be able to pull stuff from getMeasurementResultQueue
      }catch (Exception e){
    	  // Do nothing
      }
    	sleepSeconds(abisHelper.SECONDS_TO_WAIT_FOR_SYNC); //allows for some time (20s * 10) for MSSIM to attach/synchronize

    	int ms1 = 1;
    	int tch_TS = 2;
//    	int timeslot = callSetupMt(ms1, HighMSPower, tch_TS);
    	
    	int timeslot = channelActivationNormalAssign(ChannelType.Bm, tch_TS, 
    	    	TRAFFRN.Enums.TypeOfCh.SPEECH, TRAFFRN.Enums.ChannelRate.Bm, HighMSPower);
    	
    	setTestStepBegin("Check measurement reports from MSSIM.");
    	float result = checkMeasurements(4, timeslot);
    	setTestStepEnd();
    	
		// Check change, accept +/- 1, plus measuring method with bad precision.
    	if(result > (HighMSPower - 2) && (result < (HighMSPower + 2)))
    		setTestInfo("MS Power Control at level:" + HighMSPower + " at Channel Activation verifyed!");
    	else
    		fail("MS Power Control at level:" + HighMSPower + " at Channel Activation failed.");	
    	
    	int ms2 = 2;
    	tch_TS = 3;
        assertTrue("enableTsTch on timeslot " + tch_TS +" failed", abisHelper.enableTsTch(tch_TS));
        int timeslot2 = callSetupMt(ms2, MinMSPower, tch_TS);

    	setTestStepBegin("Check measurement reports from MSSIM.");
    	result = checkMeasurements(4, timeslot2);
    	setTestStepEnd();
	
    	
        // Check change, accept +/- 1, plus measuring method with bad precision.
    	if(result > (MinMSPower - 2) && (result < (MinMSPower + 2)))
    		setTestInfo("MS Power Control at level:" + MinMSPower + " at Channel Activation verifyed!");
    	else
    		fail("MS Power Control at level:" + MinMSPower + " at Channel Activation failed.");	
    	
        
    	disconnectCallMt(ms1);
    	disconnectCallMt(ms2);
    }
    

    private int callSetupMt(int ms, int msPower, int tch_TS) throws InterruptedException {
    	setTestStepBegin("Setup Call, MT at timeslot " + tch_TS);
    	
    	releaseAllCallsRestoreStackCmd = new ReleaseAllCallsCommand(abisHelper.getPARAMDISP());
    	restoreStack.add(releaseAllCallsRestoreStackCmd);
    	
    	String msPowerComment = " with MSPower = " + msPower;
    	
    	PARAMDISP.SIMCardSubStruct simCardSubStruct = ImsiHelper.getSIMCardSubStruct(ms);
    	TrafficCommand_Result res = sendcallSetupMt(simCardSubStruct, false, msPower, tch_TS);
    	//TrafficCommand_Result res = abisHelper.sendcallSetupMt(msPower);
    	if (res.getStatus().equals(com.ericsson.abisco.clientlib.servers.PARAMDISP.Enums.Status.Cmd_OK))
    		setTestInfo("Call" + msPowerComment + " was successfully set up!");
    	else {
    		setTestInfo(res.toString());
    		restoreStack.remove(releaseAllCallsRestoreStackCmd);
    		fail("Failed connecting call" + msPowerComment + ".");
    	}
    	
    	setTestStepEnd();
    	int timeslot = res.getCallLocSubStruct().getCallLocationTCH_TS();
    	setTestInfo("callSetupMT, timeslot=" + timeslot);
    	return timeslot;
    }

    public TrafficCommand_Result sendcallSetupMt(PARAMDISP.SIMCardSubStruct simCardSubStruct, 
			boolean withEncryption, int msPower, int tch_TS) throws InterruptedException {
    	PARAMDISP.CallSetupMT callSetupMT = paramdisp.createCallSetupMT();
    	PARAMDISP.SetupSubStruct setupSubStruct = new SetupSubStruct();
    	setupSubStruct.setTCH_TS(tch_TS);
    	callSetupMT.setSetupSubStruct(setupSubStruct);
    	return abisHelper.sendcallSetupMt(callSetupMT, simCardSubStruct, withEncryption, msPower);
    }

    private int mycallSetupMt(int msPower) throws InterruptedException {
    	setTestStepBegin("Setup Call, MT");
    	
    	releaseAllCallsRestoreStackCmd = new ReleaseAllCallsCommand(abisHelper.getPARAMDISP());
    	restoreStack.add(releaseAllCallsRestoreStackCmd);
    	
    	String msPowerComment = " with MSPower = " + msPower;

    	PARAMDISP.SIMCardSubStruct simCardSubStruct = ImsiHelper.getSIMCardSubStruct();
    	TrafficCommand_Result res = abisHelper.sendcallSetupMt(simCardSubStruct, false, msPower);
    	if (res.getStatus().equals(com.ericsson.abisco.clientlib.servers.PARAMDISP.Enums.Status.Cmd_OK))
    		setTestInfo("Call" + msPowerComment + " was successfully set up!");
    	else {
    		setTestInfo(res.toString());
    		restoreStack.remove(releaseAllCallsRestoreStackCmd);
    		fail("Failed connecting call" + msPowerComment + ".");
    	}
    	
    	setTestStepEnd();
    	int timeslot = res.getCallLocSubStruct().getCallLocationTCH_TS();
    	setTestInfo("callSetupMT, timeslot=" + timeslot);
    	return timeslot;
    }

    private boolean powerCheck(int timeslot, int expectedDiff) throws  InterruptedException {
      setTestStepBegin("Power Check, verify MSPower");
      // MS Power Control
      int expectedValue;
      
      sleepSeconds(2);

      // Get average Power from checkMeasurements then change power and fetch new average Power.
      // Measure MS power before
      float resp1 = checkMeasurements(4, timeslot);
      
      setTestInfo("powerCheck, first measurement=" + resp1);
      expectedValue = (int)resp1 + expectedDiff;
      msPowerControl(expectedValue);
      
      sleepSeconds(2);
      // Measure MS power after
      float resp2 = checkMeasurements(4, timeslot);
      
      // Verify change
      setTestInfo("ABISCO: Expected value of MSPower " + expectedValue + 
          ", detected value " + resp2);
     
      // Check change, accept +/- 1
      if (resp2 > (expectedValue - 2) && resp2 < (expectedValue + 2)) {
        return(true);
      }
      return(false);
    }
    
 
  
    private void intraCellHandoverTCH(int timeslot) {
      setTestStepBegin("IntracellHandoverTCH");
      PARAMDISP.SIMCardSubStruct simCardSubStruct = ImsiHelper.getSIMCardSubStruct();
      PARAMDISP.IntracellhandoverTCH intraCellHandoverTCH = paramdisp.createIntracellhandoverTCH();
      PARAMDISP.TCH_HO_SubStruct tch_HO_SubStruct = new PARAMDISP.TCH_HO_SubStruct(0,0,timeslot);

      intraCellHandoverTCH.setTCH_HO_SubStruct(tch_HO_SubStruct);
      intraCellHandoverTCH.setSpeechAlgorithm(PARAMDISP.Enums.SpeechAlgorithm.NormalFullRate);
      
      intraCellHandoverTCH.setSIMCardSubStruct(simCardSubStruct);
      
      setTestInfo("Intracellhandover LMSID=0 TG=0 TRX=0 TS=" + timeslot + " SubCh=0");
      try {
        PARAMDISP.TrafficCommand_Result res = intraCellHandoverTCH.send();
        if(res.getStatus().equals(com.ericsson.abisco.clientlib.servers.PARAMDISP.Enums.Status.Cmd_OK) &&
        		res.getCallLocSubStruct().getCallLocationTCH_TS() == timeslot)
          setTestInfo("IntraCellHandoverTCH was successful");
        else {
          setTestInfo(res.toString());
          fail("Failed to do IntraCellHandoverTCH.");
        }
      } catch (InterruptedException e) {
        fail("IntraCellHandoverTCH: send was not successful");
        e.printStackTrace();
      }
      setTestStepEnd();
    }

    private void disconnectCallMt() throws InterruptedException {
    	disconnectCallMt(1);
    }
    private void disconnectCallMt(int ms) throws InterruptedException {
      setTestStepBegin("Disconnect Call, MT");
      PARAMDISP.SIMCardSubStruct simCardSubStruct = ImsiHelper.getSIMCardSubStruct(ms);
      TrafficCommand_Result res = abisHelper.disconnectCallMt(simCardSubStruct);
      if (res.getStatus().equals(com.ericsson.abisco.clientlib.servers.PARAMDISP.Enums.Status.Cmd_OK))
        setTestInfo("Call was successfully disconnected!");
      else {
        setTestInfo(res.toString());
        fail("Failed disconnecting call.");
      }
      restoreStack.remove(releaseAllCallsRestoreStackCmd);
      setTestStepEnd();
    }
    
    private void msPowerControl(int newValue) {
      setTestInfo("ABISCO: MSPowerControl Value=" + newValue);
      PARAMDISP.SIMCardSubStruct simCardSubStruct = ImsiHelper.getSIMCardSubStruct();
      MSPowerControl msPowerControl = paramdisp.createMSPowerControl();
      msPowerControl.setMSPowerValue(newValue);
      msPowerControl.setLMSID(0);
      msPowerControl.setSIMCardSubStruct(simCardSubStruct);

      TrafficCommand_Result res;
      try {
        res = msPowerControl.send();
        if (res.getStatus().equals(com.ericsson.abisco.clientlib.servers.PARAMDISP.Enums.Status.Cmd_OK))
          setTestInfo("MS Power successfully changed.");
        else {
          setTestInfo(res.toString());
          fail("Failed to change MS Power.");
        }
      } catch (InterruptedException e) {
        e.printStackTrace();
        fail("Failed to send MSPowerControl");
      }
      
    }
    

    private float checkMeasurements(int number, int timeslot) throws InterruptedException {
      // Check MSPower
    	
    	BG.Enums.ChannelType channelType = BG.Enums.ChannelType.Bm;
      
    	int counterHit  = 0;

    	int accumulated = 0;

           
    	//Turn on sending of Measurement Results from TSS
    	onOffMeasurements(BG.Enums.BGMeasurementReporting.On);

    	MessageQueue < MeasurementResult > measurementResultQueue = bgServer.getMeasurementResultQueue();

        sleepSeconds(2);
        //  Read the requested number of Measurement Results
        for (int counterRead = 0;counterHit < number; counterRead++) 
        {  
        	MeasurementResult  msg = measurementResultQueue.poll(2, TimeUnit.SECONDS); //MeasurementResult extends AbiscoResponse
        	setTestInfo("checkMeasurements, counterRead=" + counterRead + " msg: " + msg);
        	if (msg == null) 
            {
              fail("Measurement Result queue shouldn't be empty");
            }
        	if (msg.getRouting().getTG() == 0 &&
            msg.getRouting().getTRXC() == 0 &&
            msg.getChannelNoStruct().getTimeSlotNo() == timeslot &&
            msg.getChannelNoStruct().getChannelType() == channelType) {
        		try{
        			int tempValue = msg.getL1InfoStruct().getMSPowerLevel();
        			counterHit++;
        			accumulated += tempValue;
        			setTestInfo("ABISCO: MeasurementResult " + msg.getMeasResultNoValue() 
        					+ " MSPower = " + tempValue);
        		}catch(NullPointerException e) {
        			// Do nothing
        			setTestInfo("This measurement report does not contains L1InfoStruct/MSPowerLevel: " + msg);
        		}
        	}
        	if (counterRead > (number + 20))
        	{
        		fail("ABISCO: Matching MeasurementResults not received");
        	}
        }

        onOffMeasurements(BG.Enums.BGMeasurementReporting.Off);
        
      
        return (accumulated / counterHit);
        }
    
    private void onOffMeasurements(BG.Enums.BGMeasurementReporting turnOnOff) throws InterruptedException {
      setTestInfo("onOffMeasurements: " + turnOnOff);
      setMeasurementReporting.setBGMeasurementReporting(turnOnOff);
      
      Enums.BGMeasurementReporting bgMeasRepToSet = setMeasurementReporting.getBGMeasurementReporting();

      
      try {
        BG.BGReportingStatus res = setMeasurementReporting.send();
        setTestInfo("onOffMeasurements: bgMeasRepToSet=" + bgMeasRepToSet.toString() + ", res=" + res.getBGMeasurementReporting());
        if(!res.getBGMeasurementReporting().equals(bgMeasRepToSet)) {
          fail("Failed to set BGMeasurementReporting to " + bgMeasRepToSet.toString());
        }
      } catch (InterruptedException e) {
        fail("Failed to turn measurement " + turnOnOff);
      }
    }
    
    
    private int channelActivationNormalAssign(ChannelType chType, int ts, 
    	TRAFFRN.Enums.TypeOfCh typeOfCh, TRAFFRN.Enums.ChannelRate chRate, int powerLevel) {
    	int ret = -1;
    	TRAFFRN.ChannelActNormalAssign channelActNA = rslServer.createChannelActNormalAssign();
    	TRAFFRN.ChannelNoStruct channelNoStruct = channelActNA.getChannelNoStruct();
    	channelNoStruct.setChannelType(chType); // // Bm | ...
    	channelNoStruct.setTimeSlotNo(ts); // 0 .. 7
    	TRAFFRN.ActivationTypeStruct actTypeStruct = channelActNA.getActivationTypeStruct();
    	actTypeStruct.setActivationType(TRAFFRN.Enums.ActivationType.INTRA_NOR);
    	TRAFFRN.ChannelModeStruct channelModeStruct = channelActNA.getChannelModeStruct();
    	channelModeStruct.setTypeOfCh(typeOfCh); // SPEECH | SIGNAL
    	channelModeStruct.setChannelRate(chRate); // Bm | Lm | ...
    	TRAFFRN.MSPowerStruct msPowerStruct = channelActNA.getMSPowerStruct();
    	msPowerStruct.setPowerLevel(powerLevel);
    	
    	try {
    		TRAFFRN.ChannelActAck channelActAck = channelActNA.send();
    		ret = channelActAck.getChannelNoStruct().getTimeSlotNo();
    	} catch (InterruptedException e) {
    		fail("Failed to send ChannelActivation.");
    	}

    	return ret;
    }
    
    // Helper for sysinfo 5/6
    /**
     * @name sacchFilling
     * 
     * @description Verifies sacchFilling 
     * 
     */
  public void sacchFilling5 () {
      SACCHFilling5 msg = rslServer.createSACCHFilling5();
    try {
      msg.setSystemInfoType(SystemInfoType.SI5);
      setTestInfo("sacchFilling5: " + msg.toString());
      msg.send();
    } catch (UNACKNOWLEDGED_MESSAGE_SENTException e) {
        // Normal response, do nothing
    } catch (Exception e) {
      fail("Failed to set SACCH fillings SI6");
      e.printStackTrace();
    }
  }
  public void sacchFilling6 () {
      SACCHFilling6 msg = rslServer.createSACCHFilling6();
    try {
      msg.setSystemInfoType(SystemInfoType.SI6);
      setTestInfo("sacchFilling6: " + msg.toString());
      msg.send();
    } catch (UNACKNOWLEDGED_MESSAGE_SENTException e) {
        // Normal response, do nothing
    } catch (Exception e) {
      fail("Failed to set SACCH fillings SI6");
      e.printStackTrace();
    }
  }

  private void releaseHangingCalls(){
	  //releaseRFChannel(2);
	  try{
		  ReleaseAllCalls_Result res = paramdisp.createReleaseAllCalls().send();
		  setTestInfo("Released all calls: " + res.getStatus());
	  }catch(Exception e){
		  //
	  }
  }
 }   
