package com.ericsson.msran.test.grat.framealignment;

import java.util.concurrent.TimeUnit;
import org.json.JSONException;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import se.ericsson.jcat.fw.annotations.Setup;
import se.ericsson.jcat.fw.annotations.Teardown;
import com.ericsson.msran.test.grat.testhelpers.GsmbHelper;
import com.ericsson.abisco.clientlib.MessageQueue;
import com.ericsson.abisco.clientlib.servers.BG;
import com.ericsson.abisco.clientlib.servers.BG.ChannelRequired;
import com.ericsson.abisco.clientlib.servers.CMDHAND.CHResponse;
import com.ericsson.abisco.clientlib.servers.CMDHAND.Enums.BCCHType;
import com.ericsson.commonlibrary.resourcemanager.Rm;
import com.ericsson.commonlibrary.restorestack.RestoreCommandStack;
import com.ericsson.msran.g2.annotations.TestInfo;
import com.ericsson.msran.helpers.Helpers;
import com.ericsson.msran.helpers.util.Timer;
import com.ericsson.msran.jcat.TestBase;
import com.ericsson.msran.test.grat.testhelpers.AbisHelper;
import com.ericsson.msran.test.grat.testhelpers.AbiscoConnection;
import com.ericsson.msran.test.grat.testhelpers.MomHelper;
import com.ericsson.msran.test.grat.testhelpers.MssimHelper;
import com.ericsson.msran.test.grat.testhelpers.NodeStatusHelper;
import com.ericsson.msran.test.grat.testhelpers.restorecommands.DeActivateForwardChReqToBG;
import com.ericsson.mssim.gsmb.Confirmation;
import com.ericsson.mssim.gsmb.Gsmb;
import com.ericsson.mssim.gsmb.GsmbSrvMS_SETATTR_1_CMD;
import com.ericsson.mssim.gsmb.GsmphMPH_CCCH_CLOSE_REQ;
import com.ericsson.mssim.gsmb.GsmphMPH_CCCH_DATA_REQ;
import com.ericsson.mssim.gsmb.Response;
import com.ericsson.mssim.gsmb.SacchOption;
import com.ericsson.mssim.gsmb.SdrType;
import com.ericsson.mssim.gsmb.GsmbSrvMS_CREATE_1_CMD.UsedStructureType;
import com.ericsson.mssim.gsmb.impl.GsmbFactory;
import com.ericsson.mssim.gsmb.packing.internal.PhErrorType;
import com.ericsson.abisco.clientlib.AbiscoClient;
import com.ericsson.abisco.clientlib.servers.CMDHAND;
import com.google.common.base.Stopwatch;

/**
 * @id GRAT-04.01:001
 * @name InitialFrameAlignment
 * @author 
 * @created
 * @description Verify the initial alignment according to 3GPP 51.021
 *              MSSim will send the signal to BTS. Random Access Burst
 *              shall be put on RACH check the TA values.
 * @revision
 */
public class InitialFrameAlignment extends TestBase
{
  private AbisHelper abisHelper; 
  private Gsmb gsmb;
  private BG bgServer;
  private RestoreCommandStack restoreStack;
  private MssimHelper mssimHelper;
  private GsmbHelper gsmbHelper;
  private NodeStatusHelper nodeStatus;
  private DeActivateForwardChReqToBG DeActivateForwardChReqToBGRestoreStackCmd;
  private MomHelper momHelper;
  private AbiscoClient abiscoClient;
  private AbiscoConnection abisco;
  private final int msId = 0; 
  private final short abisco_cell = 0; 
  private final int Tg = 0; 
  private final BCCHType[] TypOfBCCH = {BCCHType.NCOMB, BCCHType.COMB};
  //private final char[] ARFCN = { 'B', 'M', 'T', 'E', 'B', 'M', 'T' };
  private final int undefValue = 0xFFFF;
 
  
  /**
  * Description of test case for test reporting
  */
  @TestInfo(
            tcId = "GRAT-04.01:001",
            slogan = "Initiate Frame Alignment",
            requirementDocument = "1/00651-FCP 130 1402",
            requirementRevision = "PC5",
            requirementLinkTested = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff87c1d941?docno=1/00651-FCP1301402Uen&format=excel8book",
            requirementLinkLatest = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff86a2af3d?docno=1/00651-FCP1301402Uen&action=current&format=excel8book",
            requirementIds = { "10565-0334/21767[PA1][PREL]", "10565-0334/19034[A][APPR]",
                    "10565-0334/19417[A][APPR]" },
            verificationStatement = "Verifies GRAT-04.01:001",
            testDescription = "Verifies Initiate Frame Alignment",
            traceGuidelines = "N/A")
  /**
  * preCond. 
  **/
  
  @Setup
  public void setup()
  {
	setTestStepBegin("Setup");
    nodeStatus = new NodeStatusHelper();
    assertTrue("Node did not reach a working state before test start", nodeStatus.waitForNodeReady());
    abisHelper = new AbisHelper();
    bgServer = abisHelper.getBG();
    gsmb = Rm.getMssimList().get(0).getGsmb();
    mssimHelper = new MssimHelper(gsmb);
    momHelper = new MomHelper();
    abisco = new AbiscoConnection();
    gsmbHelper = new GsmbHelper(gsmb);
    abiscoClient = abisHelper.getAbiscoClient();
    restoreStack = Helpers.restore().restoreStackHelper().getRestoreStack();
    setTestStepEnd();
  }

  /**
  * Postcond.
  **/
  
  @Teardown
  public void teardown() 
  {
	setTestStepBegin("Teardown");
    nodeStatus.isNodeRunning();
    setTestStepEnd();
  }

  /**
   * @name InitialFrameAlignment
   * @description Verify the Initial Frame Alignment.
   * @param testId - String - 04.01.001.
   * @param description - String - initial Frame Alignment
   * @throws JSONException
   * @throws InterruptedException
   **/

  @Test(timeOut = 1800000)
  @Parameters({ "testId", "description" })
  public void initialFrameAlignment(String testId, String description) throws InterruptedException, JSONException
  {
    final short cell = mssimHelper.getMssimCellToUse(); // Precondition: One cell configure in the MSSIM.

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

    /************************************************************/
    /************************************************************/
    setTestStepBegin("Perform test - Initiate Frame Alignment");
    /************************************************************/
    /************************************************************/ 
    /* we use the arfcn configurated in the cell planning */

    int arfcnToUse = momHelper.getArfcnToUse();
    
    int systemTA = undefValue;
    
    for(int index = 0; index < 2; index++ )
    {
      setTestInfo("SubTestCase: " + index + " TypOfBCCH: " + TypOfBCCH[index]);	
      updateBcchTypeIfNeeded( TypOfBCCH[index] /*, abisco_cell*/);
      
      // activate forwarding of ChannelRequest
      DeActivateForwardChReqToBGRestoreStackCmd = new DeActivateForwardChReqToBG(abisHelper.getPARAMDISP());
      restoreStack.add(DeActivateForwardChReqToBGRestoreStackCmd);
      abisHelper.activateForwardChReqToBG();

      // Create message queue  
      MessageQueue<ChannelRequired> channelRequiredQueue = bgServer.getChannelRequiredQueue();

      if(systemTA == undefValue)
      {
    	systemTA = newChannelRequestTA(cell, msId, channelRequiredQueue); 
    	setTestInfo("OJO -- system TA: " + systemTA);	
      }
   
      assertTrue("Could not read the system delay value in signal ChannelRequired", systemTA != undefValue);
    	  
      setTestInfo("Selecting TA values");
      int[][] ta_values = { { 0, 31, 60 }, { 0, 109, 219 } };
      int[] ta_list = new int[3];

      String bandUsed = momHelper.getTgBandToUse();
      setTestInfo("arfcnToUse: " + arfcnToUse + " bandUsed: " + bandUsed);

      if (bandUsed != null && !bandUsed.equals(""))
      {
        if (bandUsed.equalsIgnoreCase("GSM450"))
        {
          ta_list = ta_values[1].clone();
        }
        else
        {
          ta_list = ta_values[0].clone(); 
        }
      }
      else
      {
        fail("Band used is not defined");   
      }

      setTestInfo("Cycle through TA-values");

      for (int i = 0; i < 3; i++)
      {
        setTestInfo("OJO -- Setting TA-value to " + ta_list[i]);
        int newTaAb = ta_list[i];

        int in_taSacch = -1; 
        int in_taNb = 0;
        short in_mssimPower = 32767;   
        int expectedValue = newTaAb + systemTA;
        
        setAttrMSSIM(cell, in_taSacch, newTaAb, in_taNb, in_mssimPower);

        int[] deltaSum = { 0, 0, 0 };
        int[] sigmaSum = { 0, 0, 0 };
        int[] delta = { 0, 0, 0 };
        int[] sigma = { 0, 0, 0 };
        int iterations = 10;

        for (int j = 0; j < iterations; j++)
        {
          int deltaTemp = 0;
          int reqTA = undefValue;
          
          int counter = 0;
          do
          {	 
            reqTA = newChannelRequestTA(cell, msId, channelRequiredQueue);  // return accessDelay value
            counter++;
          } 
          while (!validateData(reqTA, expectedValue) && (counter <= 25));
          assertTrue("Could not read the access delay value in signal ChannelRequired", reqTA != undefValue);
    
          setTestDebug("Timing Advance = " + ta_list[i] + " Measured TA (reqTA) : " + reqTA);
          reqTA -= systemTA;  
          deltaTemp = reqTA - ta_list[i];
          deltaSum[i] += deltaTemp;
          sigmaSum[i] += deltaTemp * deltaTemp;
          setTestDebug("OJO -- iter: " + j + " reqTA: " + reqTA + " deltaTemp: " + deltaTemp + " deltaSum[i]: " + deltaSum[i] + " sigmaSum[i]: " + sigmaSum[i]);
        }

        delta[i] = deltaSum[i] / iterations;
        sigma[i] = (int) Math.sqrt(sigmaSum[i] / iterations);
        
        setTestDebug("OJO -- Results for Timing Advance = " + ta_list[i]);
        setTestDebug("OJO -- Delta value: " + delta[i] + " Sigma value: " + sigma[i]);
       
        if (Math.abs(delta[i]) > 1) //Check values for result
        {
          fail("Delta value for TA: " + ta_list[i] + " is " + delta[i] + ". Should be 0 +/- 1");
        }

        if (Math.abs(sigma[i]) > 1) //Check values for result
        {
          fail("Standard deviation for TA: " + ta_list[i] + " is " + sigma[i] + ". Should be less than or equal 1.");
        }
      }
    } //end for 
    
    setTestStepEnd();

    // Cleanup
    setTestStepBegin("Cleanup");

    // Deactivate forwarding of ChannelRequest
    abisHelper.deActivateForwardChReqToBG();
    restoreStack.remove(DeActivateForwardChReqToBGRestoreStackCmd);

    // Send GsmphMPH_CCCH_CLOSE_REQ
    GsmphMPH_CCCH_CLOSE_REQ close_REQ = GsmbFactory.getGsmphMPH_CCCH_CLOSE_REQBuilder(cell).build();
    Confirmation confirmation = gsmb.send(close_REQ);
    assertEquals("GsmphMPH_CCCH_CLOSE_REQ confirmation error", confirmation.getErrorType(),
              PhErrorType.GSM_PH_ENOERR);

    // Disconnect from MS-SIM
    gsmb.disconnect();
    setTestStepEnd();

    // MOs removed by RestoreStack

  }

  private int newChannelRequestTA(short cell, int msId2, MessageQueue<ChannelRequired> channelRequiredQueue) 
  {
    int accessDelay = undefValue;

    setTestInfo("Send MSSIM.gsmphMPH_CCCH_DATA_REQ");
    GsmphMPH_CCCH_DATA_REQ data_REQ = GsmbFactory.getGsmphMPH_CCCH_DATA_REQBuilder(cell).msId(msId2).build();
    Confirmation confirmation = gsmb.send(data_REQ);
    assertEquals("gsmphMPH_CCCH_DATA_REQ confirmation error", confirmation.getErrorType(), PhErrorType.GSM_PH_ENOERR);
      
      // Verify that a Channel Required has been sent from BTS to BSC on ABIS        
    setTestInfo("ABISCO: Waiting for ChannelRequired");
    ChannelRequired response = null;
    try
    {
      response = channelRequiredQueue.poll(20, TimeUnit.SECONDS);
    } 
    catch (Exception e)
    {
      fail("Exception occurred while waiting for ChannelRequired" + e);
    }
    if (response == null)
    {
      setTestDebug("ChannelRequired queue empty ");
    }
    else 
    {
      setTestDebug("Reading channelRequired ");
      setTestInfo(response.toString());
      accessDelay = response.getAccessDelayStruct().getAccessDelayValue();
    }   	
    
    return accessDelay;
  }
  



  private boolean validateData(int accessDelay, int expectedValue) 
  {
    if ((expectedValue - 1 <= accessDelay ) && (accessDelay < expectedValue + 2))
    {
      setTestDebug("OJO -- Validating outData -> expectedValue: " + expectedValue + " accessDelay: " + accessDelay); 	
      return true;
    }	
    else
    {
      setTestDebug("OJO -- Incorrect outData -> expectedValue: " + expectedValue + " accessDelay: " + accessDelay);	
      return false;
    }  
  }

  private void setAttrMSSIM(short in_cell, int in_taSacch, int in_taAb, int in_taNb, short in_mssimPower)
  {
    GsmbSrvMS_SETATTR_1_CMD ms_SETATTR_1_CMD =
    GsmbFactory.getGsmbSrvMS_SETATTR_1_CMDBuilder(UsedStructureType.USR, SdrType.TYPE0_GSM2, in_cell).timeout(20)
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

  private CHResponse updateCell(int tgId, short cell, BCCHType bccHTypeToTest)
  {
    CMDHAND.UpdateCell cmdUpdateCell = abiscoClient.getCMDHAND().createUpdateCell();
    cmdUpdateCell.setTGId(tgId);
    cmdUpdateCell.setCellNumber(abisco_cell);
    cmdUpdateCell.setBCCHType(bccHTypeToTest);

    CMDHAND.CHResponse uptCellRsp = null;
    try
    {
      uptCellRsp = cmdUpdateCell.send();
    } 
    catch (InterruptedException e)
    {
      //logger.error("Failed to send message UpdateCell.", e); 
      fail("Failed to send message UpdateCell " + e);
    }
    return uptCellRsp;
  }

}
