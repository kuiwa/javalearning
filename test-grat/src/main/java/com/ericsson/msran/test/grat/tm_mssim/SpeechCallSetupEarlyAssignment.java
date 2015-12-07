package com.ericsson.msran.test.grat.tm_mssim;

import java.util.concurrent.TimeUnit;

import org.json.JSONException;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.ericsson.msran.jcat.TestBase;
import se.ericsson.jcat.fw.annotations.Setup;
import se.ericsson.jcat.fw.annotations.Teardown;

import com.ericsson.abisco.clientlib.MessageQueue;
import com.ericsson.abisco.clientlib.servers.BG;
import com.ericsson.abisco.clientlib.servers.BG.Enums;
import com.ericsson.abisco.clientlib.servers.BG.MeasurementResult;
import com.ericsson.abisco.clientlib.servers.OM_G31R01;
import com.ericsson.abisco.clientlib.servers.PARAMDISP;
import com.ericsson.abisco.clientlib.servers.PARAMDISP.EncryptionCommand;
import com.ericsson.abisco.clientlib.servers.PARAMDISP.EncryptionSubStruct;
import com.ericsson.abisco.clientlib.servers.PARAMDISP.Enums.EncryptionActive;
import com.ericsson.abisco.clientlib.servers.PARAMDISP.ReleaseAllCalls_Result;
import com.ericsson.abisco.clientlib.servers.TRAFFRN;
import com.ericsson.abisco.clientlib.servers.BG.SetMeasurementReporting;
import com.ericsson.abisco.clientlib.servers.PARAMDISP.MSPowerControl;
import com.ericsson.abisco.clientlib.servers.PARAMDISP.TrafficCommand_Result;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.SACCHFilling5;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.Enums.SystemInfoType;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.SACCHFilling6;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.UNACKNOWLEDGED_MESSAGE_SENTException;
import com.ericsson.commonlibrary.remotecli.Cli;
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
 * @id SpeechCallSetupEarlyAssignment
 * 
 * @name SpeechCallSetupEarlyAssignment
 *
 * @description This test class verifies the Enable Request
 */

public class SpeechCallSetupEarlyAssignment extends TestBase {
  private final OM_G31R01.Enums.MOClass moClassTx = OM_G31R01.Enums.MOClass.TX;    
  private final OM_G31R01.Enums.MOClass moClassRx = OM_G31R01.Enums.MOClass.RX;    
  private final OM_G31R01.Enums.MOClass moClassTs = OM_G31R01.Enums.MOClass.TS;

  private AbisPrePost abisPrePost;
  private PidPrePost pidPrePost;
  private AbisHelper abisHelper;
  private MomHelper momHelper;
  private TRAFFRN rslServer;
  private G2Rbs rbs;
  private Cli cli; 

  private NodeStatusHelper nodeStatus;

  private PARAMDISP paramdisp;
  private BG bgServer;
  private SetMeasurementReporting setMeasurementReporting;
  private RestoreCommandStack restoreStack;
  private ReleaseAllCallsCommand releaseAllCallsRestoreStackCmd;

  /**
   * Description of test case for test reporting
   */
  @TestInfo(
      tcId = "Extended First Call",
      slogan = "Extended First Call",
      requirementDocument = "1/00651-FCP 130 1402",
      requirementRevision = "PC5",
      requirementLinkTested = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff87c1d941?docno=1/00651-FCP1301402Uen&format=excel8book",
      requirementLinkLatest = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff86a2af3d?docno=1/00651-FCP1301402Uen&action=current&format=excel8book",
      requirementIds = { "10565-0334/21767[PA1][PREL]", "10565-0334/19034[A][APPR]",
      "10565-0334/19417[A][APPR]" },
      verificationStatement = "Verifies Part of First Call, SMS in active call, MSpower measurement and MSpower change, Intracell handover between TCH channels",
      testDescription = "Extended First Call with SMS, MSpower change and intra cell HO",
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
   * @name extendedFirstCallMsSim
   * 
   * @description Verifies Mobile Terminated call, Mobile Terminated SMS in call, MS Power change and Intra Cell HO. Also verifies that call will last for 15 minutes.
   * 
   * @param testId - unique identifier
   * @param description
   * 
   * @throws InterruptedException
   */
  @Test (timeOut = 1800000) // 30 minutes should be enough
  @Parameters({ "testId", "description" })
  public void extendedFirstCallMsSim(String testId, String description) throws InterruptedException, JSONException {
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
    setTestStepBegin("Wait for MSSIM to synch");
    // Allows for some time for MSSIM to attach/synchronize
    waitForSync(abisHelper.SECONDS_TO_WAIT_FOR_SYNC * 10);
    setTestStepEnd();

    checkStatus(); //make sure nothing brakes while UE syncs

    boolean withEncryptionA5_3 = false;
    int timeslot = callSetupMt(withEncryptionA5_3);

    // SMSText does not work right now, waiting for Abisco R13C
    sendSMSText("Hello World!");
    smsSetupMT();

    boolean powerStatus = powerCheck(timeslot, 4);

    if(!powerStatus) {
      setTestInfo("ABISCO: Expected change of MS Power not detected");
      fail("Failed to change MS Power");
    }

    int hoTimeslot = 3;
    assertTrue("enableTsTch on timeslot " + hoTimeslot +" failed", abisHelper.enableTsTch(hoTimeslot));
    intraCellHandoverTCH(hoTimeslot);

    setTestStepBegin("Call will last for 15 minutes");
    sleepSeconds(15*60); // call will last for 15 minutes.
    setTestStepEnd();

    disconnectCallMt();

    // Verify that bug in Abisco R13B is removed, sending two SMS:s.
    smsSetupMT();
    smsSetupMT();  
  }

  /**
   * @name extendedFirstCallEncryptionMsSim
   * 
   * @description Verifies encryption with A5_3 and A5_1 in Mobile Terminated call. Verifies that it is possible top enable/change encryption with Encryption Command.
   * 
   * @param testId - unique identifier
   * @param description
   * 
   * @throws InterruptedException
   */
  @Test (timeOut = 480000) // eight minutes should be enough
  @Parameters({ "testId", "description" })
  public void extendedFirstCallEncryptionMsSim(String testId, String description) throws InterruptedException, JSONException {
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

    setTestStepBegin("Wait for MSSIM to synch");
    // Allows for some time for MSSIM to attach/synchronize
    waitForSync(abisHelper.SECONDS_TO_WAIT_FOR_SYNC * 10);
    setTestStepEnd();

    checkStatus(); //make sure nothing brakes while UE syncs

    boolean withEncryptionA5_3 = true;
    callSetupMt(withEncryptionA5_3);

    sendSMSText("Hello World!");
    smsSetupMT();

    // Does not work to turn encryption off with encryption command. It result in TimeoutEncrytionCommand.
    //    	encryptionCommand(EncryptionActive.Off, PARAMDISP.Enums.EncryptionAlgorithm.A5_3);

    sleepSeconds(3); //allows for some time before everything is torn down

    disconnectCallMt();

    withEncryptionA5_3 = false;
    callSetupMt(withEncryptionA5_3);

    encryptionCommand(EncryptionActive.On, PARAMDISP.Enums.EncryptionAlgorithm.A5_1);

    sendSMSText("Hello World again!");
    smsSetupMT();

    // Change encryption algorithm
    encryptionCommand(EncryptionActive.On, PARAMDISP.Enums.EncryptionAlgorithm.A5_3);

    sleepSeconds(3);

    disconnectCallMt();
  }

  /*   
    private int callSetupMt() throws InterruptedException {
    		return callSetupMt(false);
    }
   */  
  private int callSetupMt(boolean withEncryption) throws InterruptedException {
    setTestStepBegin("Setup Call, MT");

    releaseAllCallsRestoreStackCmd = new ReleaseAllCallsCommand(abisHelper.getPARAMDISP());
    restoreStack.add(releaseAllCallsRestoreStackCmd);

    String encryptionComment = withEncryption ? " with encryption" : "";

    TrafficCommand_Result res = abisHelper.sendcallSetupMt(withEncryption);
    if (res.getStatus().equals(com.ericsson.abisco.clientlib.servers.PARAMDISP.Enums.Status.Cmd_OK))
      setTestInfo("Call" + encryptionComment + " was successfully set up!");
    else {
      setTestInfo(res.toString());
      restoreStack.remove(releaseAllCallsRestoreStackCmd);
      fail("Failed connecting call" + encryptionComment + ".");
    }

    setTestStepEnd();
    int timeslot = res.getCallLocSubStruct().getCallLocationTCH_TS();
    setTestInfo("callSetupMT, timeslot=" + timeslot);
    return timeslot;
  }

  /*    private int callSetupMTEncryption() throws InterruptedException {
    	setTestStepBegin("Setup Call, MT with encryption");

       	releaseAllCallsRestoreStackCmd = new ReleaseAllCallsCommand(abisHelper.getPARAMDISP());
    	restoreStack.add(releaseAllCallsRestoreStackCmd);

        PARAMDISP.SIMCardSubStruct simCardSubStruct = ImsiHelper.getSIMCardSubStruct();
        TrafficCommand_Result res = sendcallSetupMtEncryption(simCardSubStruct);
        if (res.getStatus().equals(com.ericsson.abisco.clientlib.servers.PARAMDISP.Enums.Status.Cmd_OK))
    		setTestInfo("Call with encryption was successfully set up!");
    	else {
    		setTestInfo(res.toString());
    		restoreStack.remove(releaseAllCallsRestoreStackCmd);
    		fail("Failed connecting call with encryption.");
    	}

    	setTestStepEnd();
    	int timeslot = res.getCallLocSubStruct().getCallLocationTCH_TS();
    	setTestInfo("callSetupMT, timeslot=" + timeslot);
    	return timeslot;
    }


   private TrafficCommand_Result sendcallSetupMtEncryption(PARAMDISP.SIMCardSubStruct simCardSubStruct) throws InterruptedException {
      	PARAMDISP.PagingAttemptsStruct pagingAttemptsStruct = new PARAMDISP.PagingAttemptsStruct(5);
       	PARAMDISP.CallSetupMT callSetupMT = paramdisp.createCallSetupMT();
       	callSetupMT.setLMSID(0);
       	callSetupMT.setSIMCardSubStruct(simCardSubStruct);
       	callSetupMT.setSpeechGenerationMode(PARAMDISP.Enums.SpeechGenerationMode.LoopBack);
       	callSetupMT.setPagingAttemptsStruct(pagingAttemptsStruct);

       	// Add encryption
       	//EncryptionSubStruct encryptionSubStruct = new EncryptionSubStruct();
       	EncryptionSubStruct encryptionSubStruct = new EncryptionSubStruct(EncryptionActive.On, PARAMDISP.Enums.EncryptionAlgorithm.A5_3 );
       	encryptionSubStruct.setEncryptionActive(EncryptionActive.On);
       	callSetupMT.setEncryptionSubStruct(encryptionSubStruct);

       	return callSetupMT.send();
     }
   */    

  private void smsSetupMT() throws InterruptedException {
    setTestStepBegin("Setup SMS MT");
    //TrafficCommand_Result res = sendSMSSetupMT();
    TrafficCommand_Result res = abisHelper.sendSMSSetupMT();
    if (res.getStatus().equals(com.ericsson.abisco.clientlib.servers.PARAMDISP.Enums.Status.Cmd_OK))
      setTestInfo("SMS was successfully sent to MSSIM!");
    else {
      setTestInfo(res.toString());
      fail("Failed to send SMS.");
    }
    setTestStepEnd();
  }

  private boolean powerCheck(int timeslot, int expectedDiff) throws  InterruptedException {
    setTestStepBegin("Power Check, verify MSPower");
    // MS Power Control
    int expectedValue;

    sleepSeconds(2);

    // Get average Power from checkMeasurements then change power and fetch new average Power.
    // Measure MS power before
    int resp1 = checkMeasurements(4, timeslot);

    setTestInfo("powerCheck, first measurement=" + resp1);
    expectedValue = resp1 + expectedDiff;
    msPowerControl(expectedValue);

    sleepSeconds(2);
    // Measure MS power after
    int resp2 = checkMeasurements(4, timeslot);

    // Verify change
    setTestInfo("ABISCO: Expected value of MSPower " + expectedValue + 
        ", detected value " + resp2);

    // Check change, accept +/- 1
    if (resp2 > (expectedValue - 2) && resp2 < (expectedValue + 2)) {
      return(true);
    }
    return(false);
  }



  private void sendSMSText(String text) throws InterruptedException {
    PARAMDISP.SIMCardSubStruct simCardSubStruct = ImsiHelper.getSIMCardSubStruct();
    PARAMDISP.SMSText smsText = paramdisp.createSMSText();
    smsText.setLMSID(0);
    smsText.setSIMCardSubStruct(simCardSubStruct);
    smsText.setSMSMessage(text);
    smsText.send();
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
    setTestStepBegin("Disconnect Call, MT");
    TrafficCommand_Result res = abisHelper.disconnectCallMt();
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


  private int checkMeasurements(int number, int timeslot) throws InterruptedException {
    // Check MSPower

    BG.Enums.ChannelType channelType = BG.Enums.ChannelType.Bm;

    int counterHit  = 0;

    int accumulated = 0;

    //Turn on sending of Measurement Results from TSS
    onOffMeasurements(BG.Enums.BGMeasurementReporting.On, timeslot);

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

    onOffMeasurements(BG.Enums.BGMeasurementReporting.Off, timeslot);

    return (accumulated / counterHit);
  }

  private void onOffMeasurements(BG.Enums.BGMeasurementReporting turnOnOff, int timeslot) throws InterruptedException {
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
  /* private void releaseRFChannel(int timeSlotNo) {
	  try {
	//	rslServer.createRFChannelRelease().send();
		RFChannelRelease rfChannelRelease = rslServer.createRFChannelRelease();
		ChannelNoStruct channelNoStruct = new ChannelNoStruct();
		channelNoStruct.setChannelType(TRAFFRN.Enums.ChannelType.Bm);
		channelNoStruct.setTimeSlotNo(timeSlotNo);
		rfChannelRelease.setChannelNoStruct(channelNoStruct);
		rfChannelRelease.send();
	} catch (Exception e) {
		// Do nothing
	}
  }*/

  private void releaseHangingCalls(){
    //releaseRFChannel(2);
    try{
      ReleaseAllCalls_Result res = paramdisp.createReleaseAllCalls().send();
      setTestInfo("Released all calls: " + res.getStatus());
    }catch(Exception e){
      //
    }
  }

  /*
   * encryptionCommand(
   */
  private void encryptionCommand(EncryptionActive onOrOff, 
      PARAMDISP.Enums.EncryptionAlgorithm encAlg) throws InterruptedException {

    setTestStepBegin("Turning " + encAlg + " encryption " + onOrOff + " with Encryption Command.");
    EncryptionCommand encryptionCommand = paramdisp.createEncryptionCommand();
    PARAMDISP.SIMCardSubStruct simCardSubStruct = ImsiHelper.getSIMCardSubStruct();

    encryptionCommand.setSIMCardSubStruct(simCardSubStruct);
    encryptionCommand.setLMSID(0);
    encryptionCommand.setEncryptionSubStruct(createEncryptionSubStruct(onOrOff, encAlg));
    TrafficCommand_Result encRes = encryptionCommand.send();

    if(encRes.getStatus().equals(com.ericsson.abisco.clientlib.servers.PARAMDISP.Enums.Status.Cmd_OK))
      setTestInfo("Turning encryption " + onOrOff + " in call was successful.");
    else
      fail("Failed to turn " + onOrOff + " encryption in call. Failure cause: " + encRes.getStatus());
    setTestStepEnd();
  }

  private EncryptionSubStruct createEncryptionSubStruct(EncryptionActive onOrOff, PARAMDISP.Enums.EncryptionAlgorithm algorithm) {
    EncryptionSubStruct encryptionSubStruct = new EncryptionSubStruct();
    encryptionSubStruct.setEncryptionActive(onOrOff);
    encryptionSubStruct.setEncryptionAlgorithm(algorithm);
    return encryptionSubStruct;
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
}   
