package com.ericsson.msran.test.grat.mspowercontrol;

import java.util.List;
import org.json.JSONException;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.ericsson.msran.jcat.TestBase;
import se.ericsson.jcat.fw.annotations.Setup;
import se.ericsson.jcat.fw.annotations.Teardown;

import com.ericsson.abisco.clientlib.servers.TRAFFRN;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.ChannelActAck;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.ChannelActAsyncHandover;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.MSPowerStruct;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.Enums.ActivationType;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.Enums.AlgOrRate;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.Enums.ChannelRate;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.Enums.ChannelType;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.Enums.TypeOfCh;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.ActivationTypeStruct;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.ChannelModeStruct;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.ChannelNoStruct;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.RFChannelRelease;
import com.ericsson.commonlibrary.resourcemanager.Rm;
import com.ericsson.msran.g2.annotations.TestInfo;
import com.ericsson.msran.test.grat.testhelpers.AbisHelper;
import com.ericsson.msran.test.grat.testhelpers.AbiscoConnection;
import com.ericsson.msran.test.grat.testhelpers.MomHelper;
import com.ericsson.msran.test.grat.testhelpers.GsmbHelper;
import com.ericsson.msran.test.grat.testhelpers.MssimHelper;
import com.ericsson.msran.test.grat.testhelpers.NodeStatusHelper;
import com.ericsson.mssim.gsmb.Confirmation;
import com.ericsson.mssim.gsmb.Gsmb;
import com.ericsson.mssim.gsmb.GsmphMPH_CCCH_CLOSE_REQ;
import com.ericsson.mssim.gsmb.GsmphMPH_CHN_CLOSE_REQ;
import com.ericsson.mssim.gsmb.Indication;
import com.ericsson.mssim.gsmb.LapdmDL_UNITDATA_IND;
import com.ericsson.mssim.gsmb.impl.GsmbFactory;
import com.ericsson.mssim.gsmb.packing.internal.PhErrorType;
import com.ericsson.abisco.clientlib.AbiscoServer.Routing;

/**
 * @id 
 * @name MS Power Channel Activation at Handover
 * @author
 * @created 2015-11-23
 * @description Verify MS Power Channel Activation at Handover
 */

public class MSPowerChannelActivation extends TestBase {

  private AbisHelper abisHelper;
  private MomHelper momHelper;
  private Gsmb gsmb;
  private MssimHelper mssimHelper;
  private GsmbHelper gsmbHelper;
  private AbiscoConnection abisco;

  private NodeStatusHelper nodeStatus;

  private final int tg = 0; // the transceiver group to test
  private short cell;

  private final int CC_1 = 1;
  private final int CC_2 = 2;
  private final int CC_5 = 5;
  private final int CC_7 = 7;

  private final int HighMSPower = 3;
  private final int MediumMSPower = 7;

  private final int msId = 0; // identifies the msid
  private long ccId[] = new long[8]; // channel identifier returned by ChActMSSIM, identifies the activated channel

  private ChannelType chType;
  private ChannelRate chRate;
  private TypeOfCh typeOfCh;
  private AlgOrRate algOrRate;

  private final short AMRCodec = 0;
  private final String PdchMode = "";

  /**
   * Description of test case for test reporting
   */
  @TestInfo(
      tcId = "",
      slogan = "MS Power at Channel Activation, Async Handover",
      requirementDocument = "1/00651-FCP 130 1402",
      requirementRevision = "PC5",
      requirementLinkTested = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff87c1d941?docno=1/00651-FCP1301402Uen&format=excel8book",
      requirementLinkLatest = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff86a2af3d?docno=1/00651-FCP1301402Uen&action=current&format=excel8book",
      requirementIds = { "10565-0334/21767[PA1][PREL]", "10565-0334/19034[A][APPR]",
      "10565-0334/19417[A][APPR]" },
      verificationStatement = "   ",
      testDescription = "Verifies that MS Power is handled correctly by Channel Activation Async Handover",
      traceGuidelines = "N/A")

  @Setup
  public void setup() throws InterruptedException, JSONException {
    setTestStepBegin("Setup");

    nodeStatus = new NodeStatusHelper();
    assertTrue("Node did not reach a working state before test start", nodeStatus.waitForNodeReady());

    abisHelper = new AbisHelper();
    abisco = new AbiscoConnection();
    gsmb = Rm.getMssimList().get(0).getGsmb();
    mssimHelper = new MssimHelper(gsmb);
    gsmbHelper = new GsmbHelper(gsmb);
    cell = mssimHelper.getMssimCellToUse();
    momHelper = new MomHelper();

    setTestStepEnd();
  }

  /**
   * Postcond.
   * 
   * @throws InterruptedException
   */
  @Teardown
  public void teardown() throws InterruptedException {
    nodeStatus.isNodeRunning();
  }

  /**
   * @name msPower
   * @description Verifies MS power at Channel Activation
   * @param testId - unique identifier
   * @param description
   * @throws InterruptedException
   */
  @Test(timeOut = 60000000)
  // six minutes should be enough
  @Parameters({ "testId", "description" })
  public void msPower(String testId, String description) throws InterruptedException,
  JSONException {

    setTestCase(testId, description);

    setTestStepBegin("Init MS-SIM");
    assertTrue("Failed to initiate MSSIM", gsmbHelper.mssimInit(getCurrentTestCaseName()));
    setTestStepEnd();

    setTestStepBegin("Create MOs");
    momHelper.createUnlockAllGratMos(1, 1);
    setTestStepEnd();

    setTestStepBegin("Setup Abisco");
    abisco.setupAbisco(1, 1, false);
    setTestStepEnd();

    setTestStepBegin("Start Abis MOs");
    abisHelper.completeStartup(0, 1);
    setTestStepEnd();

    setTestStepBegin("Start MS-SIM");
    assertTrue("Failed to define cell in MSSIM", gsmbHelper.mssimDefineCell(3));
    setTestStepEnd();

    // 23.02.001 Handling of MS power at channel activation
    // --------------------------------------------
    setTestInfo("23.02.001 Handling of MS power at channel activation.");

    final int trxc = 0;
    final int[] channelCombinations = {CC_7, CC_1};
    final short[] timeslot = {1, 2};
    final int[] msPower = {HighMSPower, MediumMSPower};

    // Do test for each channel combination
    for (int i = 0; i < 2; i++)
    {
      String chComb = (channelCombinations[i] == CC_7) ? "Channel Combination (vii)" : "Channel Combination (i)";
      setTestStepBegin("MS Power at Channel Activation Async Handover for " + chComb);
      // Setup channel on TS 2. 
      ccId[i] = setupChannel(trxc, channelCombinations[i], timeslot[i], msId, msPower[i]);

      sleepSeconds(3);

      short resp = receiveMSSIM();

      if (resp != msPower[i])
      {
        fail("Failed to set MS power at Channel Activation Async Handover. Requested MS Power: " + msPower[i] + ", received MS Power: " + resp);
      }

      releaseChannel(trxc, channelCombinations[i], timeslot[i], ccId[i]);
      
      setTestStepEnd();
    }
    // Cleanup.
//    setTestStepBegin("Cleanup");
    // Close of CCCH and disconnect of gsmb is handled by restorestack
    // Send GsmphMPH_CCCH_CLOSE_REQ.
//    GsmphMPH_CCCH_CLOSE_REQ close_REQ = GsmbFactory.getGsmphMPH_CCCH_CLOSE_REQBuilder(cell).build();
//    Confirmation confirmation = gsmb.send(close_REQ);
//    assertEquals("GsmphMPH_CCCH_CLOSE_REQ confirmation error", confirmation.getErrorType(),
//        PhErrorType.GSM_PH_ENOERR);

    // Disconnect from MS-SIM.
 //   gsmb.disconnect();
//    setTestStepEnd();
  }

  private short receiveMSSIM() {
    List<Indication> listOfIndications = null;
    short ret = 0;

    listOfIndications = gsmb.getIndications();
    //listOfIndications.clear();

    for(Indication myInd: listOfIndications) 
    {
      if((myInd instanceof LapdmDL_UNITDATA_IND)) 
      {            	            	
        // Get MS Power out of SACCH blocks in the message
        ret = (short)((LapdmDL_UNITDATA_IND) myInd).getPhyHdr()[0];
        setTestDebug("receiveMSSIM: msPower = " + ret);
      }
    }
    return ret;
  }

  private long setupChannel(int trxc, int channelCombination, short ts, int msId, int msPower)
      throws InterruptedException {
    setTestInfo("Channel Activation, channel combination = " + channelCombination +
        ", time slot = " + ts +
        ", msId = " + msId +
        ", MS power = " + msPower);

    // Set channel parameters depending on specified channel combination (CC_1, ...).
    if (channelCombination == CC_1)
    {
      chType = TRAFFRN.Enums.ChannelType.Bm;
      chRate = TRAFFRN.Enums.ChannelRate.Bm;
      typeOfCh = TypeOfCh.SPEECH;
      algOrRate = AlgOrRate.GSM1;
    }
    else if (channelCombination == CC_2)
    {
      chType = TRAFFRN.Enums.ChannelType.Lm_0;
      chRate = TRAFFRN.Enums.ChannelRate.Lm;
      typeOfCh = TypeOfCh.SPEECH;
      algOrRate = AlgOrRate.GSM1;
    }
    else if (channelCombination == CC_5)
    {
      chType = TRAFFRN.Enums.ChannelType.SDCCH_4_0;
      chRate = TRAFFRN.Enums.ChannelRate.SDCCH;
      typeOfCh = TypeOfCh.SIGNALLING;
      algOrRate = AlgOrRate.NoResourcesRequired;
    }
    else // channelCombination == CC_7
        {
      chType = TRAFFRN.Enums.ChannelType.SDCCH_8_0;
      chRate = TRAFFRN.Enums.ChannelRate.SDCCH;
      typeOfCh = TypeOfCh.SIGNALLING;
      algOrRate = AlgOrRate.NoResourcesRequired;
        }
 
    // Send Channel activation from Abisco.
    setTestInfo("Send ChannelActAsyncHandover");

    TRAFFRN traffrn = abisHelper.getRslServer();

    setTestInfo("createChannelActAsyncHandover");
    ChannelActAsyncHandover channelAct = traffrn.createChannelActAsyncHandover();

    Routing routing = new Routing();
    routing.setTG(tg);
    routing.setTRXC(trxc);
    channelAct.setRouting(routing);

    ChannelNoStruct channelNoStruct = new ChannelNoStruct();
    channelNoStruct.setTimeSlotNo(ts);
    channelNoStruct.setChannelType(chType);
    channelAct.setChannelNoStruct(channelNoStruct);

    ActivationTypeStruct activationTypeStruct = new ActivationTypeStruct();
    activationTypeStruct.setActivationType(ActivationType.INTER_ASYNC);
    channelAct.setActivationTypeStruct(activationTypeStruct);

    ChannelModeStruct channelModeStruct = new ChannelModeStruct();
    channelModeStruct.setChannelRate(chRate);
    channelModeStruct.setTypeOfCh(typeOfCh);
    channelModeStruct.setAlgOrRate(algOrRate);
    channelAct.setChannelModeStruct(channelModeStruct);

    MSPowerStruct mspowerstruct = new MSPowerStruct();
    mspowerstruct.setPowerLevel(msPower);
    channelAct.setMSPowerStruct(mspowerstruct);

    ChannelActAck resp = channelAct.send();

    setTestDebug("ChannelActAck: " + resp);

    // Activate channel in MSSIM.
    setTestInfo("Activate channel in MSSIM");

    long ccId = gsmbHelper.chActMSSIM(cell,
        tg,
        trxc,
        (short) ts,
        msId,
        chType,
        true, // bypass = false
        true, // allSacchSI
        chRate,
        typeOfCh,
        algOrRate,
        ActivationType.INTER_ASYNC,
        AMRCodec,
        PdchMode);

    return ccId;
  }

  private void releaseChannel(int trxc, int channelCombination, short ts, long ccId) throws InterruptedException
  {
    setTestInfo("Channel Release, channel combination = " + channelCombination +
        ", time slot = " + ts);

    // Set channel type depending on specified channel combination (CC_1, ...).
    if (channelCombination == CC_1)
    {
      chType = TRAFFRN.Enums.ChannelType.Bm;
    }
    else if (channelCombination == CC_2)
    {
      chType = TRAFFRN.Enums.ChannelType.Lm_0;
    }
    else // channelCombination == CC_7
    {
      chType = TRAFFRN.Enums.ChannelType.SDCCH_8_0;
    }

    // Deactivate channel in MSSIM.
    setTestInfo("Deactivate channel in MSSIM");

    GsmphMPH_CHN_CLOSE_REQ chn_closereq = GsmbFactory.getGsmphMPH_CHN_CLOSE_REQBuilder(ccId).timeout(20).build();
    Confirmation confirmation1 = gsmb.send(chn_closereq);
    assertEquals("gsmphMPH_CHN_CLOSE_REQ confirmation error", PhErrorType.GSM_PH_ENOERR,
        confirmation1.getErrorType());

    // Release channel in Abisco.
    setTestInfo("Send RFChannelRelease");

    TRAFFRN traffrn = abisHelper.getRslServer();
    RFChannelRelease channelRel = traffrn.createRFChannelRelease();

    Routing routing = new Routing();
    routing.setTG(tg);
    routing.setTRXC(trxc);
    channelRel.setRouting(routing);

    ChannelNoStruct channelnostruct = new ChannelNoStruct();
    channelnostruct.setTimeSlotNo(ts);
    channelnostruct.setChannelType(chType);
    channelRel.setChannelNoStruct(channelnostruct);

    channelRel.send();
  }
}

