package com.ericsson.msran.test.grat.radiochannelsrelease;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.ericsson.msran.jcat.TestBase;
import se.ericsson.jcat.fw.annotations.Setup;
import se.ericsson.jcat.fw.annotations.Teardown;

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
 * @id NodeUC628
 * 
 * @name Abis Radio Channel Release Test
 * 
 * @author Mats Andersson
 * 
 * @created 2014-10-03
 * 
 * @description This test class verifies Abis Radio Channels Release
 * 
 * 
 */

public class RadioChannelsReleaseTest extends TestBase {
    private final OM_G31R01.Enums.MOClass moClassTf = OM_G31R01.Enums.MOClass.TF;
    private final OM_G31R01.Enums.MOClass moClassTs = OM_G31R01.Enums.MOClass.TS;
    private final OM_G31R01.Enums.MOClass  moClassScf = OM_G31R01.Enums.MOClass.SCF;
    
    private static final Logger myLogger = Logger.getLogger(RadioChannelsReleaseTest.class);
    private AbisPrePost abisPrePost;
    private AbisHelper abisHelper;
    private MomHelper momHelper;

   
    private NodeStatusHelper nodeStatus;

    /**
     * Description of test case for test reporting
     */
    @TestInfo(
            tcId = "NodeUC628",
            slogan = "Abis Radio Channels Release",
            requirementDocument = "1/00651-FCP 130 1402",
            requirementRevision = "PC5",
            requirementLinkTested = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff87c1d941?docno=1/00651-FCP1301402Uen&format=excel8book",
            requirementLinkLatest = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff86a2af3d?docno=1/00651-FCP1301402Uen&action=current&format=excel8book",
            requirementIds = { "" },
            verificationStatement = "Verifies NodeUC628.N",
            testDescription = "Verifies Radio Channels Release.",
            traceGuidelines = "N/A")


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
 
        setTestInfo("Verify that MO State for instance "+ tsInstance + " is DISABLED");  
        assertTrue("abisTsMoState for tsInstance " + tsInstance + " is not DISABLED", momHelper.waitForAbisTsMoState(tsInstance, "DISABLED", 5));
            
        setTestInfo("Configure AO TS instance " + tsInstance);
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

        setTestInfo("Send Enable Request to AO TS instance " + tsInstance);
        result = abisHelper.enableRequest(this.moClassTs, tsInstance, associatedSoInstance);

        setTestInfo("Verify that MO State in Enable Result is ENABLED");
        assertEquals("MO state is not ENABLED", OM_G31R01.Enums.MOState.ENABLED, result.getMOState());

        setTestInfo("Verify that MO:Trx attribute:abisTsMoState for instance " + tsInstance + " is ENABLED");
        assertTrue("abisTsMoState for tsInstance " + tsInstance + " is not ENABLED", momHelper.waitForAbisTsMoState(tsInstance, "ENABLED", 5));
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

	    ChannelActNormalAssign msg = abisHelper.channelActivationNormalAssignment(tn);
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
      * @throws InterruptedException 
      * @name radioChannelsRelease
      * 
      * @description Verifies radioChannelsRelease cmd
      */
     public void radioChannelsRelease () throws InterruptedException 
     {
       try  
       {
           setTestStepBegin("Send RadioChannelsRelease cmd");
           OM_G31R01.RadioChannelsReleaseComplete result = abisHelper.radioChannelsReleaseCommand(OM_G31R01.Enums.MOClass.SCF, 0, 255); 
           setTestStepEnd();
           myLogger.debug(result.toString());

       } catch (OM_G31R01.RadioChannelsReleaseRejectException e) {
           setTestStepBegin("RadioChannelsReleaseReject");
           myLogger.debug(e.toString());
           setTestStepEnd();
           e.printStackTrace();
           fail("Error: RadioChannelsReleaseRejectException occured");
       }
     }

     /**
     * @name radioChannelsRelease
     * 
     * @description verifies OML Radio Channels Release command. Release all channels after they have been 
     * activated (1st channel activation). Verify that they have been released by trying to activate 
     * them again (2nd channel activation). 3rd channel activation should be rejected due to channels 
     * already are activated. Last part test that we get a reject if SCF state != STARTED
     * 
     * @param testId - unique identifier
     * @param description
     * 
     * @throws InterruptedException
     * @throws JSONException
     */
    @Test (timeOut = 120000)
    @Parameters({ "testId", "description" })
    public void radioChannelsRelease (String testId, String description) throws InterruptedException, JSONException {

        /* OBS! tsInstance is equal to tn in this test suite*/        
        setTestCase(testId, description);

        setTestStepBegin("Enable AO TF and then Enable TS 0-7");
    	enableRequestAoTf();        

        for (int i = 0; i < 8; i++)
        {
          setTestInfo("Enable TS-" + i);
          enableRequestAoTs(i, OM_G31R01.Enums.Combination.TCH);
        }
        setTestStepEnd();   
        sleepSeconds(1);

        // 1st Channel activation
        setTestStepBegin("1st Channel Activation Normal Assignment TS 0-7");
        for (int i = 0; i < 8; i++)
        {
          setTestInfo("Channel Activation Normal Assignment TS-" + i);
	      channelActivationNormalAssignment (i);
        }
        setTestStepEnd();       
        sleepSeconds(5);

        // Send RadioChannelsRelease cmd with invalid MO class
        try {
            setTestInfo("Send RadioChannelsRelease cmd with invalid MO Class");
            OM_G31R01.RadioChannelsReleaseComplete result = abisHelper.radioChannelsReleaseCommand(OM_G31R01.Enums.MOClass.AT, 0, 255); 
            myLogger.debug(result.toString());
            
           fail("Error: Expected Radio Channels Release Rejected since MO Class != SCF");

        } catch (OM_G31R01.RadioChannelsReleaseRejectException e) {
            setTestInfo("Received RadioChannelsReleaseReject as expected");
        }
        
        // Release all Channels 
        radioChannelsRelease();
 
        // 2nd Channel activation should be ok since channels have been released before 
        setTestStepBegin("2nd Channel Activation Normal Assignment 0-7. OK ");       
        for (int i = 0; i < 8; i++)
        {
          setTestInfo("Channel Activation Normal Assignment TS-" + i);
          channelActivationNormalAssignment (i);
        }
        setTestStepEnd();
        
        /* 3rd channel activation should be rejected since channels already are activated */
        setTestStepBegin("3rd Channel Activation Normal Assignment TS 0-7. FAIL");
        
        for (int i = 0; i < 8; i++)
        {
          setTestInfo("Channel Activation Normal Assignment TS-" + i);
          
          ChannelActNormalAssign msg = abisHelper.channelActivationNormalAssignment(i);
          try {
              myLogger.debug(msg.toString());

              ChannelActAck ack = msg.send();
              myLogger.debug(ack.toString());
              
              fail("Error: Expected channel activation to be rejected since it's already activated");
              
          } catch (ChannelActNegAckException e) {
              setTestInfo("Verified that it's not possible to activate channel since it's already active");
          }   
        }
        setTestStepEnd();
        
        // Test that we get a Radio Channels Release Reject if AbisScfState = RESET
        setTestStepBegin("Radio Channels Release Reject due to AbisScfState = RESET");     

        abisHelper.resetCommand(this.moClassScf);
        setTestInfo("Verify that MO:GsmSector attribute:abisScfState is RESET");
        assertTrue("MO " + MomHelper.SECTOR_LDN + " abisScfState is not RESET", 
                momHelper.waitForMoAttributeStringValue(MomHelper.SECTOR_LDN, "abisScfState", "RESET", 6));
        
        try {
            setTestInfo("Send RadioChannelsRelease cmd");
            OM_G31R01.RadioChannelsReleaseComplete result = abisHelper.radioChannelsReleaseCommand(OM_G31R01.Enums.MOClass.SCF, 0, 255); 
            myLogger.debug(result.toString());
            
           fail("Error: Expected Radio Channels Release Rejected since SO SCF = RESET");

        } catch (OM_G31R01.RadioChannelsReleaseRejectException e) {
            setTestInfo("Received RadioChannelsReleaseReject as expected");
        }
        setTestStepEnd();
       
    }
}   
