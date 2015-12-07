package com.ericsson.msran.test.grat.framealignment;

import java.util.List;
import org.json.JSONException;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import se.ericsson.jcat.fw.annotations.Setup;
import se.ericsson.jcat.fw.annotations.Teardown;
import com.ericsson.abisco.clientlib.servers.CMDHAND.CHResponse;
import com.ericsson.abisco.clientlib.servers.CMDHAND.Enums.BCCHType;
import com.ericsson.commonlibrary.resourcemanager.Rm;
//import com.ericsson.commonlibrary.restorestack.RestoreCommandStack;
import com.ericsson.msran.g2.annotations.TestInfo;
//import com.ericsson.msran.helpers.Helpers;
import com.ericsson.msran.jcat.TestBase;
import com.ericsson.msran.test.grat.testhelpers.AbisHelper;
import com.ericsson.msran.test.grat.testhelpers.AbiscoConnection;
import com.ericsson.msran.test.grat.testhelpers.MssimHelper;
import com.ericsson.msran.test.grat.testhelpers.CHNParams;
import com.ericsson.msran.test.grat.testhelpers.NodeStatusHelper;
import com.ericsson.mssim.gsmb.Confirmation;
import com.ericsson.mssim.gsmb.Gsmb;
import com.ericsson.mssim.gsmb.GsmphMPH_CCCH_CLOSE_REQ;
import com.ericsson.mssim.gsmb.GsmphMPH_CHN_CLOSE_REQ;
import com.ericsson.mssim.gsmb.LapdmDL_UNITDATA_IND;
import com.ericsson.mssim.gsmb.Indication;
import com.ericsson.mssim.gsmb.Response;
import com.ericsson.mssim.gsmb.SacchOption;
import com.ericsson.mssim.gsmb.SdrType;
import com.ericsson.mssim.gsmb.FrequencyStructure.FrStructureType;
import com.ericsson.mssim.gsmb.GsmbSrvMS_CREATE_1_CMD.UsedStructureType;
import com.ericsson.mssim.gsmb.impl.GsmbFactory;
import com.ericsson.mssim.gsmb.packing.internal.PhErrorType;
import com.ericsson.abisco.clientlib.AbiscoClient;
import com.ericsson.abisco.clientlib.servers.CMDHAND;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.RFChannelRelease;
import com.ericsson.abisco.clientlib.AbiscoServer.Routing;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.ChannelNoStruct;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.ActivationTypeStruct;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.TimingAdvanceStruct;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.ChannelModeStruct;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.Enums.ChannelType;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.Enums.ChannelRate;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.Enums.TypeOfCh;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.Enums.AlgOrRate;
import com.ericsson.mssim.gsmb.GsmphMPH_CS_CHN_OPEN_REQ;
import com.ericsson.mssim.gsmb.GsmbSrvMS_SET_MEAS_CMD;
import com.ericsson.mssim.gsmb.GsmbSrvMS_SETATTR_1_CMD;
import com.ericsson.mssim.gsmb.GsmbSrvMS_SET_MEAS_CMD.ChanBit;
import com.ericsson.mssim.gsmb.GsmappPRBS_START_CMD;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.ChannelActImmediateAssign;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.ChannelActNormalAssign;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.Enums.ActivationType;
import com.ericsson.msran.test.grat.testhelpers.GsmbHelper;
import com.ericsson.msran.test.grat.testhelpers.MomHelper;
import com.ericsson.msran.test.grat.testhelpers.abis.ChannelActImmediateAssignBuilder;

/**
 * @id GRAT-04.01:002
 * @name Dynamic Frame Alignment
 * @author eamacab
 * @created
 * @description Verify the initial alignment according to 3GPP 51.021
 *              MSSim will send the signal to BTS Random Access Burst
 *              shall be put on RACH check the TA values.
 * @revision
 */

public class DynamicFrameAlignment2 extends TestBase
{
  private AbisHelper abisHelper; 
  private Gsmb gsmb;
  //private BG bgServer;
  //private RestoreCommandStack restoreStack;
  private MssimHelper mssimHelper;
  private GsmbHelper gsmbHelper;
  private MomHelper momHelper;  
  private AbiscoConnection abisco;
  private NodeStatusHelper nodeStatus;
  //private DeActivateForwardChReqToBG DeActivateForwardChReqToBGRestoreStackCmd;
  private AbiscoClient abiscoClient;
  private int msId = 0; // identifies the msid
  private long ccId = 0; //channel combination identifier returned by ChActMSSIM, identifies the activated channel
  private final int Tg = 0; //The Transciever Group to test
  private final int Trxc = 0;
  private final short Ts = 2;
  private final String[] ChAct = { "ImmediateAssign", "ImmediateAssign", "ImmediateAssign", "ImmediateAssign",
            /*"NormalAssign", "NormalAssign", "NormalAssign"*/ };
  private final ActivationType[] ActType = { ActivationType.INTRA_IMM, ActivationType.INTRA_IMM,
            ActivationType.INTRA_IMM, ActivationType.INTRA_IMM /*, ActivationType.INTRA_NOR, ActivationType.INTRA_NOR,
            ActivationType.INTRA_NOR*/ };
  private final TypeOfCh[] TpOfCh = { TypeOfCh.SIGNALLING, TypeOfCh.SIGNALLING, TypeOfCh.SIGNALLING,
            TypeOfCh.SIGNALLING, /*TypeOfCh.DATA, TypeOfCh.DATA, TypeOfCh.DATA */};
  private final ChannelType ChType = ChannelType.Bm;
  private final ChannelRate[] ChRate = { ChannelRate.Bm, ChannelRate.Bm, ChannelRate.Bm, ChannelRate.Bm,
            /*ChannelRate.Bm_BI, ChannelRate.Bm_BI, ChannelRate.Bm_BI */};
  private final AlgOrRate[] AlgRate = { AlgOrRate.NoResourcesRequired, AlgOrRate.NoResourcesRequired,
            AlgOrRate.NoResourcesRequired, AlgOrRate.NoResourcesRequired, /*AlgOrRate.T9_6kbits_s, AlgOrRate.T9_6kbits_s,
            AlgOrRate.T9_6kbits_s */};
  private final short AMRCodec = 0;
  private final String PdchMode = ""; 
  private final BCCHType TypOfBCCH = BCCHType.NCOMB;
  //private final char[] ARFCN = { 'B', 'M', 'T', 'E', 'M', 'T', 'B' };
  private List<Indication> listOfIndications = null;

  private final short abisco_cell = 0; //default cell number
  //private final int undefValue = 0xFFFF;
  /**
   * Description of test case for test reporting
   */
  @TestInfo(
      tcId = "GRAT-04.01:002",
      slogan = "Dynamic Frame Alignment",
      requirementDocument = "1/00651-FCP 130 1402",
      requirementRevision = "PC5",
      requirementLinkTested = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff87c1d941?docno=1/00651-FCP1301402Uen&format=excel8book",
      requirementLinkLatest = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff86a2af3d?docno=1/00651-FCP1301402Uen&action=current&format=excel8book",
      requirementIds = { "10565-0334/21767[PA1][PREL]", "10565-0334/19034[A][APPR]", "10565-0334/19417[A][APPR]" },
      verificationStatement = "Verifies GRAT-04.01:002",
      testDescription = "Verifies Dynamic Frame Alignment",
      traceGuidelines = "N/A")
    
  /**
   * preCond. 
   */
  @Setup
  public void setup()
  {
	setTestStepBegin("Setup");
    nodeStatus = new NodeStatusHelper();
    assertTrue("Node did not reach a working state before test start", nodeStatus.waitForNodeReady());
    momHelper = new MomHelper();
    abisco = new AbiscoConnection();    
    abisHelper = new AbisHelper();
    gsmb = Rm.getMssimList().get(0).getGsmb();
    mssimHelper = new MssimHelper(gsmb);
    gsmbHelper = new GsmbHelper(gsmb);
    abiscoClient = abisHelper.getAbiscoClient();
    //restoreStack = Helpers.restore().restoreStackHelper().getRestoreStack();
    setTestStepEnd();
  }

  /**
   * Postcond.
   */
  @Teardown
  public void teardown() 
  {
	setTestStepBegin("Teardown");
    nodeStatus.isNodeRunning();
    setTestStepEnd();
  }

  /**
   * @name DynamicFrameAlignment
   * @description Verify the Dynamic Frame Alignment.
   * @param testId - String - 04.01.002.
   * @param description - String - Dynamic Frame Alignment
   * @throws JSONException
   * @throws InterruptedException
   */

  @Test(timeOut = 10000000)
  @Parameters({ "testId", "description" })
  public void dynamicFrameAlignment(String testId, String description) throws InterruptedException, JSONException
  {
    final short CELL = mssimHelper.getMssimCellToUse(); // Precondition: One cell configure in the MSSIM.

    setTestCase(testId, description);

    /************************************************/
    setTestStepBegin("Initiate mssim");
    /************************************************/    
    assertTrue("Failed to initiate MSSIM",gsmbHelper.mssimInit(getCurrentTestCaseName(),true));
    setTestStepEnd();

    /************************************************/
    setTestStepBegin("Create MOs and setup Abisco");
    /************************************************/    
    momHelper.createUnlockAllGratMos(1, 1);
    abisco.setupAbisco(1, 1, false);
    abisHelper.completeStartup(0, 1);
    
    setTestStepEnd();

    /************************************************/
    setTestStepBegin("Define MS and open CCCH channel in mssim");
    /************************************************/  
    assertTrue("Failed to define cell in MSSIM",gsmbHelper.mssimDefineCell());
    setTestStepEnd();    
    
    listOfIndications = gsmb.getIndications();
    listOfIndications.clear();
 
    /*    // activate forwarding of ChannelRequest
    DeActivateForwardChReqToBGRestoreStackCmd = new DeActivateForwardChReqToBG(abisHelper.getPARAMDISP());
    restoreStack.add(DeActivateForwardChReqToBGRestoreStackCmd);
    try 
    {
	  abisHelper.activateForwardChReqToBG();
	} 
    catch (InterruptedException e) 
	{
	  fail("Failed activating forwardChReqToBG " + e);
	}*/
    
    /************************************************************/
    /************************************************************/
    setTestStepBegin("Perform test - DynamicFrameAlignment");
    /************************************************************/
    /************************************************************/
    
    updateBcchTypeIfNeeded( TypOfBCCH /*, abisco_cell*/);

    //for(int iter = 0; iter < 7; iter++)
    for(int iter = 0; iter < 1; iter++)
    {

      setTestInfo("OJO -- Subtest: " + iter);
    	
      // Releasing all channels to make sure all are inactive
      chCleanBTS();

      boolean bypass = false; // set to 'true' when you want to bypass MSSIM automatic LAPDm handling
      boolean allSacchSI = true;
      int taBase = 0;

      // Open channels on BTS and MSSIM
      ccId = dynamicAlignmentActCh(taBase, CELL, iter, bypass, allSacchSI);

      dynamicAlignment(CELL);
  	
      // Close channels
      dynamicAlignmentRelCh(ccId);
    }
    setTestStepEnd();
 
    // Cleanup
    setTestStepBegin("Cleanup");

/*    // Deactivate forwarding of ChannelRequest
    abisHelper.deActivateForwardChReqToBG();
    restoreStack.remove(DeActivateForwardChReqToBGRestoreStackCmd);*/

    // Send GsmphMPH_CCCH_CLOSE_REQ
    GsmphMPH_CCCH_CLOSE_REQ close_REQ = GsmbFactory.getGsmphMPH_CCCH_CLOSE_REQBuilder(CELL).build();
    Confirmation confirmation = gsmb.send(close_REQ);
    assertEquals("GsmphMPH_CCCH_CLOSE_REQ confirmation error", confirmation.getErrorType(), PhErrorType.GSM_PH_ENOERR);

    // Disconnect from MS-SIM
    gsmb.disconnect();
    setTestStepEnd();

    // MOs removed by RestoreStack

  }

  private void dynamicAlignment(short cell) 
  {
	int taSacch = 0;
	int taNb = 0;

    setTestInfo("OJO --Simulating that the MS is moving away from the BTS ");
    for(taSacch = 0; taSacch < 63; taSacch++)
    {
      for(taNb = 0; taNb <=1; taNb++)
      {
        setTestInfo("OJO -- SetAttrMSSIM TaSacch= " + taSacch + " TaNb= " + taNb);
    	setAttrMSSIM(cell, taSacch, 0 /*taAb*/, taNb, (short)80 /*mssimPower*/);   
      }
      

      setTestDebug("OJO -- Begin taSacch " + taSacch);
      assertTrue("Ordered Timing Advance received is incorrect   ", checkOrderedTimingAdvance(taSacch+1));
    }

    
/*    setTestInfo("OJO --Simulating that the MS is moving towards the BTS ");
    for(taSacch = 62; taSacch >= 0; taSacch--)
    {
      for(taNb = 0; taNb <=-1; taNb--) // 0 and -1
      {
        setTestInfo("OJO -- SetAttrMSSIM TaSacch= " + taSacch + " TaNb= " + taNb);
    	setAttrMSSIM(cell, taSacch, 0 taAb, taNb, (short)80 mssimPower);   
      }

      setTestDebug("OJO -- Begin taSacch " + taSacch);
      assertTrue("Ordered Timing Advance received is incorrect   ", checkOrderedTimingAdvance(taSacch+1));
    
    }  */  
  }  
  
  private boolean checkOrderedTimingAdvance(int expectedValue) 
  {
	setTestInfo("Parse list to find the LapdmDL_UNITDATA_IND message - 20 attempts.");
	int count = 0;
	int orderedTimingAdvance = 0xFFFF;
	boolean isValid = false;
    
	do
	{
	  sleepSeconds(5);	
	  listOfIndications = gsmb.getIndications();
	  setTestDebug("eamacab -- ListInd-Size: " + listOfIndications.size());
	  
	  for(Indication myInd: listOfIndications) 
	  {
	    setTestDebug("OJO -- " + myInd.stringRepresentation());   
	    if((myInd instanceof LapdmDL_UNITDATA_IND)) 
	    {            	            	
		  // Check the contents physical layer protocol header of SACCH blocks in the message  
		  orderedTimingAdvance= (int)((LapdmDL_UNITDATA_IND) myInd).getPhyHdr()[1];
		  setTestDebug("OrderedTimingAdvance: " + orderedTimingAdvance + " expectedValue: " + expectedValue);
        }
 	  }
	  count++;
	  isValid = validateReceivedValue(orderedTimingAdvance, expectedValue);
	} while (!isValid && (count < 20));  
    return isValid;
  } 

  private boolean validateReceivedValue(int orderedTimingAdvance, int expectedValue) 
  {
    if(orderedTimingAdvance == expectedValue)
    {
      setTestDebug("Validating - OrderedTimingAdvance: " + orderedTimingAdvance + " expectedValue: " + expectedValue);
      return true;
    }
    else if(orderedTimingAdvance == expectedValue + 1)
    {
      setTestDebug("OJO - Validating - (Two steps) OrderedTimingAdvance: " + orderedTimingAdvance + " expectedValue: " + expectedValue);
      return true;
    }
    else
    {
      setTestDebug("Incorrect value - OrderedTimingAdvance: " + orderedTimingAdvance + " expectedValue: " + expectedValue);
      return false;
    }
  }

  private void dynamicAlignmentRelCh(long in_ccId) 
  {
    chRelMSSIM( in_ccId );
    chCleanBTS();				
  }

  /**
   * @name ChRelMSSIM
   * 
   * @description To release a channel in the MSSIM
   * 
   * 
   */

  private void chRelMSSIM(long in_ccId) 
  {
    // Send gsmphMPH_CHN_CLOSE_REQ
    setTestInfo("Close CHN channel from MSSIM");
    GsmphMPH_CHN_CLOSE_REQ chn_closereq = GsmbFactory.getGsmphMPH_CHN_CLOSE_REQBuilder(in_ccId).timeout(20).build();
    Confirmation confirmation1 = gsmb.send(chn_closereq);
    assertEquals("gsmphMPH_CHN_CLOSE_REQ confirmation error", PhErrorType.GSM_PH_ENOERR, confirmation1.getErrorType());		
  }

  
  private long dynamicAlignmentActCh(int taBase, short cell, int iter, boolean bypass, boolean allSacchSI) 
  {
    long ccId = gsmbHelper.chActMSSIM(cell, Tg, Trxc, Ts, msId, ChType, bypass, allSacchSI, ChRate[iter], TpOfCh[iter], AlgRate[iter], ActType[iter], AMRCodec, PdchMode);
    setTestInfo("BTS Channel Activation  Physical Context TA " + taBase);
    chActBTS(taBase, ChAct[iter], ActType[iter], ChRate[iter], TpOfCh[iter], AlgRate[iter]);
    return ccId;
  }

  private void setAttrMSSIM(short in_cell, int in_taSacch, int in_taAb, int in_taNb, short in_mssimPower)
  {
    GsmbSrvMS_SETATTR_1_CMD ms_SETATTR_1_CMD =
    GsmbFactory.getGsmbSrvMS_SETATTR_1_CMDBuilder(UsedStructureType.USR, SdrType.TYPE0_GSM2, in_cell).timeout(25)
               .msId(msId)
               .spare((short) 0)
               .tmsi(-1)
               .sens(new short[] { 32767 }) //0x7fff
               .power(new short[] { in_mssimPower })
               .sacchOpt(SacchOption.GSMB_SRV_SO_REP_OFF)
               .usrPowerSacch((short) -1)
               .usrTaSacch(in_taSacch)
               .usrTaAb( new int[] { in_taAb } ) //TA-value for Access bursts
               .usrTaNb(new int[] { in_taNb }) //TA-value for Normal bursts
               .build();
    // Send MSSIM.gsmbSrvMS_CREATE_1_CMD requesting the TaAb of the MS active
    Response response = gsmb.send(ms_SETATTR_1_CMD);
    assertTrue("gsmbSrvMS_SETATTR_1_CMD response failed", response.isSuccess());
  }

  //To activate a channel in the BTS
  private void chActBTS(int in_ta, String in_chAct, ActivationType in_actType, ChannelRate in_chRate,
        TypeOfCh in_typeOfCh, AlgOrRate in_algOrRate)
  {
    if (in_chAct.equals("ImmediateAssign"))
    {
      ChannelActImmediateAssign command = AbisHelper.getChannelActImmediateAssignBuilder(Tg, Trxc)
    		    .setTimeSlotNo(Ts)
    			.setActivationType(in_actType)
    			.setChannelRate(in_chRate)
    			.setTypeOfCh(in_typeOfCh)
    			.setAlgOrRate(in_algOrRate)
    			.setChannelType(ChType)
    			.setTimingAdvanceValue(in_ta)
    			.build();
      try
      {
    	command.send();  
      } 
      catch (InterruptedException e)
      {
    	fail("Failed sending message ChannelActImmediateAssign. ", e);  
      }
    }
    else if (in_chAct.equals("NormalAssign"))
    {
      ChannelActNormalAssign command = AbisHelper.getChannelActNormalAssignBuilder(Tg, Trxc)
    		.setTimeSlotNo(Ts)  
  			.setActivationType(in_actType)
  			.setChannelRate(in_chRate)
  			.setTypeOfCh(in_typeOfCh)
  			.setAlgOrRate(in_algOrRate)
  			.setChannelType(ChType)
  			.setTimingAdvanceValue(in_ta)
  			.build();

      try
      {
       	command.send();  
      }
      catch (InterruptedException e)
      {
    	fail("Failed sending message ChannelActNormalAssign. ", e);   
      }
    }
    else
    {
      fail("Incorrect message type. Neither ImmediateAssign or NormalAssign");
    }
  }

  // Purpose:    To release all radio traffic channels or control channels in the BTS without asserting the responses
  private void chCleanBTS()
  {
    ChannelType[] channelTypes = { ChannelType.Bm, ChannelType.Lm_0, ChannelType.Lm_1 };
    int[] channelTs = { 2, 3, 4, 5, 6, 7 };
    for(int indexCh = 0; indexCh < channelTypes.length; indexCh++)
    {
      for(int indexTs = 0; indexTs < channelTs.length; indexTs++)
      {
        //RFChannelRelease command = abisHelper.channelRelease(tn); // this method create the object with timeSlot but other default values
        RFChannelRelease command = abiscoClient.getTRAFFRN().createRFChannelRelease();

        Routing routing = new Routing();
        routing.setTG(Tg);
        routing.setTRXC(Trxc);
        command.setRouting(routing);

        ChannelNoStruct channelnostruct = new ChannelNoStruct();
        channelnostruct.setTimeSlotNo(channelTs[indexTs]);
        channelnostruct.setChannelType(channelTypes[indexCh]);
        command.setChannelNoStruct(channelnostruct);

        try
        {
          //RFChannelReleaseAck response = command.send();
          command.send();
        } 
        catch (InterruptedException e)
        {
          fail("Failed sending message RFChannelRelease. ", e);
        }
      }
    }
  }

  /*
   * The BCCHType enum is the channel combination of the cell:
   * COMB ==> Combined, channel combination
   * NCOMB ==> NonCombined, channel combination iv + vii
   */

  private void updateBcchTypeIfNeeded(BCCHType bccHTypeToTest /*, short cell*/)
  {
    // Verify existing BccHType in the BTS
    CMDHAND.InfoCellResponse cmdInfoCellRsp = abisHelper.sendInfoCell(0, abisco_cell);
    CMDHAND.Enums.BCCHType currentBccHType = cmdInfoCellRsp.getBCCHType();

    if (currentBccHType != bccHTypeToTest)
    {
      setTestDebug("BccHType received from Abisco::InfoCell = " + currentBccHType + " is different to bccHTypeToTest " +  bccHTypeToTest);
      //Updates an existing cell in the TG.
      updateCell(0, abisco_cell, bccHTypeToTest);
      
      try
      {
        abisHelper.completeStartup(Tg, 1);
      } 
      catch (InterruptedException e)
      {
        fail("Failed to send message CompleteStart.", e);
      }
    }
  }  

  //move this method to AbiscoHelper
  private CHResponse updateCell(int tgId, short cell, BCCHType bccHTypeToTest)
  {
    CMDHAND.UpdateCell cmdUpdateCell = abiscoClient.getCMDHAND().createUpdateCell();
    cmdUpdateCell.setTGId(0);
    cmdUpdateCell.setCellNumber(cell);
    cmdUpdateCell.setBCCHType(bccHTypeToTest);

    CMDHAND.CHResponse uptCellRsp = null;
    try
    {
      uptCellRsp = cmdUpdateCell.send();
    } 
    catch (InterruptedException e)
    {
      fail("Failed sending message UpdateCell " + e);
    }
    return uptCellRsp;
  }
}
