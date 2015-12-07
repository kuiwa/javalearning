package com.ericsson.msran.test.grat.happytestingistfsalive;

import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.ericsson.msran.jcat.TestBase;
import se.ericsson.jcat.fw.annotations.Setup;
import se.ericsson.jcat.fw.annotations.Teardown;

import com.ericsson.abisco.clientlib.AbiscoResponse;
import com.ericsson.abisco.clientlib.MessageQueue;
import com.ericsson.abisco.clientlib.servers.BG;
import com.ericsson.abisco.clientlib.servers.BG.Enums.BGMeasurementReporting;
import com.ericsson.abisco.clientlib.servers.BG.MeasurementResult;
import com.ericsson.abisco.clientlib.servers.BG.SetMeasurementReporting;
import com.ericsson.abisco.clientlib.servers.OM_G31R01.Enums.Combination;
import com.ericsson.abisco.clientlib.servers.OM_G31R01;
import com.ericsson.abisco.clientlib.servers.TRAFFRN;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.ActivationTypeStruct;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.BCCHInfoP1_1;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.ChannelActAck;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.ChannelActImmediateAssign;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.ChannelActNegAckException;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.ChannelActNormalAssign;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.ChannelModeStruct;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.ChannelNoStruct;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.DeactivateSACCH;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.Enums.ActivationType;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.Enums.ChannelType;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.Enums.SystemInfoType;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.Enums.TypeOfCh;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.Enums.ChannelRate;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.Enums.AlgOrRate;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.ImmAssignInfoP1;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.MSPowerStruct;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.RFChannelRelease;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.RFChannelReleaseAck;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.SACCHFilling5;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.TimingAdvanceStruct;
import com.ericsson.msran.g2.annotations.TestInfo;
import com.ericsson.msran.test.grat.testhelpers.AbisHelper;
import com.ericsson.msran.test.grat.testhelpers.MomHelper;
import com.ericsson.msran.test.grat.testhelpers.NodeStatusHelper;
import com.ericsson.msran.test.grat.testhelpers.prepost.AbisPrePost;

/**
 * @id xx12345xx
 * 
 * @name HappyTestingIsTfsAliveTest
 * 
 * @author Kalle Kula
 * 
 * @created 2014-08-28
 * 
 * @description This test class verifies that GSMTFS is alive by happy testing
 * 
 * 
 */

public class HappyTestingIsTfsAliveTest extends TestBase {
    private final OM_G31R01.Enums.MOClass moClassTf = OM_G31R01.Enums.MOClass.TF;
    private final OM_G31R01.Enums.MOClass moClassTs = OM_G31R01.Enums.MOClass.TS;
    
    private static final Logger myLogger = Logger.getLogger(HappyTestingIsTfsAliveTest.class);
    private AbisPrePost abisPrePost;
    private AbisHelper abisHelper;
    private MomHelper momHelper;
    private TRAFFRN rslServer;
    private BG bgServer;
    private         SetMeasurementReporting setMeasurementReporting;

   
    private NodeStatusHelper nodeStatus;

    /**
     * Description of test case for test reporting
     */
    @TestInfo(
            tcId = "N/A",
            slogan = "GSMTFS happy testing",
            requirementDocument = "N/A",
            requirementRevision = "N/A",
            requirementLinkTested = "http://www.kallekula.se",
            requirementLinkLatest = "http://www.kallekula.se",
            requirementIds = { "None", "None", "None" },
            verificationStatement = "Verifies xx",
            testDescription = "Verifies that GSMTFS is alive by happy testing.",
            traceGuidelines = "N/A")


    /**
     * Create MO:s, establish links to Abisco, and verify preconditions.
     * 
     * @throws InterruptedException
     * @throws JSONException 
     */
    @Setup
    public void setup() throws InterruptedException, JSONException {
        nodeStatus = new NodeStatusHelper();
        assertTrue("Node did not reach a working state before test start", nodeStatus.waitForNodeReady());
        
        abisHelper  = new AbisHelper();
        momHelper   = new MomHelper();
        abisPrePost = new AbisPrePost();
        rslServer   = abisHelper.getRslServer();
        bgServer    = abisHelper.getBG();
        
        /**
         * @todo Create TRX MO when postconditions for it can be verified (sync is ok).
         */
        
        /**
         * @todo Verify Trx MO pre and post cond when we have sync working
         */        
        
        abisPrePost.preCondAllMoStateStarted();      
        
        setMeasurementReporting =  bgServer.createSetMeasurementReporting();
        setMeasurementReporting.setBGMeasurementReporting(BGMeasurementReporting.On);
        setMeasurementReporting.send();
      }
    
    /**
     * Postcond.
     */
    @Teardown
    public void teardown() {
        nodeStatus.isNodeRunning();
        setMeasurementReporting.setBGMeasurementReporting(BGMeasurementReporting.Off);
        try {
			setMeasurementReporting.send();
		} catch (InterruptedException e) {
			myLogger.warn("setMeasurementReporting.send() got Interrupted!");
			e.printStackTrace();
		}
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
    	assertEquals(result.getMOState(), OM_G31R01.Enums.MOState.ENABLED);
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
     * @name deactivateSACCH
     * 
     * @description Verifies deactivateSACCH 
     * 
     * @param tn - timeslot number
     * 
     */

    public void deactivateSACCH (int tn)  {
	
		DeactivateSACCH msg = rslServer.createDeactivateSACCH();

		try {
			msg.setChannelNoStruct (channelNo (ChannelType.Bm, tn));
            myLogger.debug(msg.toString());

	        msg.sendAsync();
			
		} catch (Exception e) {
	        myLogger.debug(e.toString());
			e.printStackTrace();
		}
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
    * @name channelActivationImmediateAssignment
    * 
    * @description Verifies channelActivationImmediateAssignment 
    * 
    * @param tn - timeslot number
    * @param cc - Channel Combination
    * 
    * @throws InterruptedException
    */
   public void channelActivationImmediateAssignment (int tn, Combination cc) throws InterruptedException {

        ChannelActImmediateAssign msg = rslServer.createChannelActImmediateAssign();
        msg.getCH_Action().setSyntaxCheckOff();
        
        try {
            ChannelModeStruct chanMode = new ChannelModeStruct ();

            switch (cc)
            {
            case MainBCCHCombined:// cc_v
                msg.setChannelNoStruct (channelNo (ChannelType.SDCCH_4_0, tn));
                chanMode.setTypeOfCh(TypeOfCh.SIGNALLING);
                chanMode.setChannelRate(ChannelRate.SDCCH);
                chanMode.setAlgOrRate(AlgOrRate.NoResourcesRequired);
               break;
            case SDCCH:// cc_vii
                msg.setChannelNoStruct (channelNo (ChannelType.SDCCH_8_0, tn));
                chanMode.setTypeOfCh(TypeOfCh.SIGNALLING);
                chanMode.setChannelRate(ChannelRate.SDCCH);
                chanMode.setAlgOrRate(AlgOrRate.NoResourcesRequired);
                break;
            case TCH:// cc_i
                msg.setChannelNoStruct (channelNo (ChannelType.Bm, tn));//Fullrate for now..
                chanMode.setTypeOfCh(TypeOfCh.SPEECH);
                chanMode.setChannelRate(ChannelRate.Bm);
                chanMode.setAlgOrRate(AlgOrRate.GSM1);
                break;
            default :
                break;
            }
            
            msg.setChannelModeStruct(chanMode);
            
            ActivationTypeStruct actType = new ActivationTypeStruct ();
            actType.setActivationType(ActivationType.INTRA_IMM);
            msg.setActivationTypeStruct(actType);
          
            MSPowerStruct msPow = new MSPowerStruct();
            msPow.setPowerLevel(10);
            msg.setMSPowerStruct(msPow);

            TimingAdvanceStruct timAdv = new TimingAdvanceStruct();
            timAdv.setTimingAdvanceValue(0);
            msg.setTimingAdvanceStruct(timAdv);
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
    * @name channelRelease
    * 
    * @description Verifies channelRelease 
    * 
    * @param tn - timeslot number
    * 
    */
     public void channelRelease (int tn)
    {
		try 
	    {
			RFChannelRelease msg = abisHelper.channelRelease(tn);
	        myLogger.debug(msg.toString());
	        
	        RFChannelReleaseAck ack = msg.send();
	        myLogger.debug(ack.toString());
			
		} 
	    catch (Exception e)
	    {
	        myLogger.debug(e.toString());
			e.printStackTrace();
		}
	}

     /**
      * @name bcchInfo
      * 
      * @description Verifies bcchInfo 
      * 
      * @param tn - timeslot number
      * 
      */
	public void bcchInfo (int tn)
    {
		BCCHInfoP1_1 msg = rslServer.createBCCHInfoP1_1();

		try 
	    {
			msg.setChannelNoStruct (channelNo (ChannelType.BCCH, tn));

	        myLogger.debug(msg.toString());

            msg.sendAsync();
			
		} 
	    catch (Exception e)
	    {
	        myLogger.debug(e.toString());
			e.printStackTrace();
		}
	}

    /**
     * @name immediateAssign
     * 
     * @description Verifies immediateAssign 
     * 
     * @param tn - timeslot number
     */
	public void immediateAssign (int tn)
    {
		ImmAssignInfoP1 msg = rslServer.createImmAssignInfoP1();

		try 
	    {
			msg.setChannelNoStruct (channelNo (ChannelType.CCCH_D, tn));

	        myLogger.debug(msg.toString());

            msg.sendAsync();
		} 
	    catch (Exception e)
	    {
	        myLogger.debug(e.toString());
			e.printStackTrace();
		}
	}

    /**
     * @name sacchFilling
     * 
     * @description Verifies sacchFilling 
     * 
     */
	public void sacchFilling () {
	
		SACCHFilling5 msg = rslServer.createSACCHFilling5();

		try {
			msg.setSystemInfoType(SystemInfoType.SI5);
	        myLogger.debug(msg.toString());

	        msg.sendAsync();
			
		} catch (Exception e) {
	        myLogger.debug(e.toString());
			e.printStackTrace();
		}
	}

	
    /**
     * @name pollMeasurementResults
     * 
     * @description Fetches Measurement Results 
     * 
     */
	public void pollMeasurementResults ()
	{   
	    int i=0;

	    MessageQueue<MeasurementResult > measurementResultQueue = bgServer.getMeasurementResultQueue();

	    try        
	    {
	        AbiscoResponse msg;
	        while (i < 10)
	        {
	            msg = measurementResultQueue.poll(5, TimeUnit.SECONDS);
	            if (msg == null) 
	            {
	                fail("Measurement Result queue shouldn't be empty");
	                break;
	            }

	            myLogger.debug(msg.toString());

	            i++;
	        }
	    } catch (Exception e) {
	        myLogger.debug(e.toString());
	        e.printStackTrace();
	        fail("Measurement Result exception");
	    }
	}

	
	private ChannelNoStruct channelNo (ChannelType chanT, int tn)
	{
		ChannelNoStruct chanNo = new ChannelNoStruct ();
		chanNo.setTimeSlotNo (tn);
		chanNo.setChannelType (chanT);
		return chanNo;
    }

    /**
     * @name happyTestingIsTfsAlive
     * 
     * @description verifies that GSMTFS is alive by happy testing
     * 
     * @param testId - unique identifier
     * @param description
     * 
     * @throws InterruptedException
     * @throws JSONException
     */
    @Test (timeOut = 120000)
    @Parameters({ "testId", "description" })
    public void happyTestingIsTfsAlive (String testId, String description) throws InterruptedException, JSONException {

        /* OBS! tsInstance is equal to tn in this test suite*/
        
        setTestCase(testId, description);

        setTestStepBegin("Enable AO TF");
    	enableRequestAoTf();
        setTestStepEnd();

    	setTestStepBegin("Enable TS-0 for MainBCCH");
        enableRequestAoTs(0, OM_G31R01.Enums.Combination.MainBCCH);
        setTestStepEnd();

        setTestStepBegin("BCCH Info");
		bcchInfo (0);
        setTestStepEnd();

    	setTestStepBegin("Immediate Assign");
		immediateAssign (0);
        setTestStepEnd();
        
        sleepSeconds(1);

    	setTestStepBegin("SACCH Filling");
		sacchFilling ();
        setTestStepEnd();

        setTestStepBegin("Enable TS-1 for TCH");
        enableRequestAoTs(1, OM_G31R01.Enums.Combination.TCH);
        setTestStepEnd();

        sleepSeconds(1);

        setTestStepBegin("Channel Activation Normal Assignment");
		channelActivationNormalAssignment (1);
        setTestStepEnd();

        setTestStepBegin("Measurement Result messages");
        pollMeasurementResults();
        setTestStepEnd();

        sleepSeconds(1);
        
    	setTestStepBegin("Deactivate SACCH");
        deactivateSACCH (1);
        setTestStepEnd();
        
		setTestStepBegin("Channel Release");
        channelRelease (1);
        setTestStepEnd();

        disableRequestAoTs(0);
        setTestStepBegin("Enable TS-0 for MainBCCH Combined (cc_v)");
        enableRequestAoTs(0, OM_G31R01.Enums.Combination.MainBCCHCombined);
        setTestStepEnd();

        sleepSeconds(1);

        setTestStepBegin("Channel Activation Immediate Assignment MainBBCH combined (cc_v)");
        channelActivationImmediateAssignment (0,OM_G31R01.Enums.Combination.MainBCCHCombined);
        setTestStepEnd();

        disableRequestAoTs(0);
        setTestStepBegin("Enable TS-0 for SDCCH (cc_vii)");
        enableRequestAoTs(0, OM_G31R01.Enums.Combination.SDCCH);
        setTestStepEnd();

        sleepSeconds(1);

        setTestStepBegin("Channel Activation Immediate Assignment mainBBCH combined (cc_vii)");
        channelActivationImmediateAssignment (0, OM_G31R01.Enums.Combination.SDCCH);
        setTestStepEnd();
   
        /* Clean up */
        disableRequestAoTs(0);
        disableRequestAoTs(1);
       
    }
}   
