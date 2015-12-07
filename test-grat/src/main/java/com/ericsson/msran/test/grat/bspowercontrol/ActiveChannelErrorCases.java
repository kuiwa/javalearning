package com.ericsson.msran.test.grat.bspowercontrol;

import java.util.Arrays;

import org.json.JSONException;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import se.ericsson.jcat.fw.annotations.Setup;
import se.ericsson.jcat.fw.annotations.Teardown;

import com.ericsson.abisco.clientlib.AbiscoServer.Behaviour;
import com.ericsson.abisco.clientlib.AbiscoServer.LAPD;
import com.ericsson.abisco.clientlib.AbiscoServer.Routing;
import com.ericsson.abisco.clientlib.AbiscoResponse;
import com.ericsson.abisco.clientlib.servers.BITSTREAM;
import com.ericsson.abisco.clientlib.servers.BITSTREAM.ReceiveData;
import com.ericsson.abisco.clientlib.servers.BITSTREAM.SendData;
import com.ericsson.abisco.clientlib.servers.TRAFFRN;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.BSPowerStruct;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.ChannelActAck;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.ChannelActNormalAssign;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.ChannelNoStruct;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.RFChannelRelease;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.Enums.ActivationType;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.Enums.AlgOrRate;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.Enums.ChannelRate;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.Enums.ChannelType;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.Enums.TypeOfCh;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.ActivationTypeStruct;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.ChannelModeStruct;
import com.ericsson.commonlibrary.resourcemanager.Rm;
import com.ericsson.msran.g2.annotations.TestInfo;
import com.ericsson.msran.jcat.TestBase;
import com.ericsson.msran.test.grat.testhelpers.AbisHelper;
import com.ericsson.msran.test.grat.testhelpers.AbiscoConnection;
import com.ericsson.msran.test.grat.testhelpers.GsmbHelper;
import com.ericsson.msran.test.grat.testhelpers.MomHelper;
import com.ericsson.msran.test.grat.testhelpers.NodeStatusHelper;
import com.ericsson.mssim.gsmb.Gsmb;


/**
 * @id LTE00237844
 * @name BS Power Active Channel, Error cases
 * @author GRAT Cell
 * @created 2015-11-23
 * @description Verify BS Power Active Channel, Error cases
 */

public class ActiveChannelErrorCases extends TestBase {
    private NodeStatusHelper nodeStatus;
    private Gsmb gsmb;
    private AbisHelper abisHelper;
    private GsmbHelper gsmbHelper;
    private AbiscoConnection abisco;
    AbiscoResponse msg = null;
    long CcId = 0;
    private MomHelper momHelper;
    int arfcn = 0;
    
    /**
     * Description of test case for test reporting
     */
    @TestInfo(
            tcId = "LTE00237844",
            slogan = "BS Power Control, Active Channel",
            requirementDocument = "1/00651-FCP 130 1402",
            requirementRevision = "PC5",
            requirementLinkTested = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff87c1d941?docno=1/00651-FCP1301402Uen&format=excel8book",
            requirementLinkLatest = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff86a2af3d?docno=1/00651-FCP1301402Uen&action=current&format=excel8book",
            requirementIds = { "10565-0334/21767[PA1][PREL]", "10565-0334/19034[A][APPR]",
                    "10565-0334/19417[A][APPR]" },
            verificationStatement = "Verifies GRAT-36.03:004",
            testDescription = "Verifies the BS Power Control, Active Channel",
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
        gsmb = Rm.getMssimList().get(0).getGsmb();
        gsmbHelper = new GsmbHelper(gsmb);
        momHelper = new MomHelper();
        arfcn = momHelper.getArfcnToUse();
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
     * @name BS Power Control, Active Channel
     * @description Verify the BS Power Control, Active Channel.
     * @param testId - String - Unique identifier of the test case.
     * @param description - String - Brief description of test case.
     * @throws JSONException
     * @throws InterruptedException
     */
    @Test(timeOut = 1800000)
    @Parameters({ "testId", "description" })
    public void activeChannelErrorCasesTest(String testId, String description) throws InterruptedException, JSONException {

    	setTestStepBegin("Init MS-SIM");
        assertTrue("Failed to initiate MSSIM",gsmbHelper.mssimInit(getCurrentTestCaseName(), true));
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
        assertTrue("Failed to define cell in MSSIM",gsmbHelper.mssimDefineCell());
        setTestStepEnd();
      

        //Send Channel activation on the bts with TS = 2, BS Power = Pn
        channelActNormalAssign();
        
        //Send BS Power Control on the BTS with TS =2, BS Power = Pn-16 
        //Expected message from BTS is ERROR Report with cause value 104 (Invalid Information Element Contents)
        sendDataBsPower(false);
        
        rfChannelRelease();
        
        //Send Channel activation on the bts with TS = 2, BS Power = Pn
        channelActNormalAssign();
        
        //Send BS Power Control on the BTS with TS =2, BS Power = Pn-16 
        //Expected message from BTS is ERROR Report with cause value 104 (Invalid Information Element Contents)
        sendDataBsPower(true);
        
        rfChannelRelease();
 
    }
    
    private void channelActNormalAssign() throws InterruptedException {
    	setTestInfo("Sending channelActivationNormalAssign");
        
    	TRAFFRN traffrn = abisHelper.getRslServer();
        ChannelActNormalAssign channelActNormalAssign = traffrn.createChannelActNormalAssign();
                
        ActivationTypeStruct activationTypeStruct = new ActivationTypeStruct();
                
        ChannelModeStruct channelModeStruct = new ChannelModeStruct();
        channelModeStruct.setTypeOfCh(TypeOfCh.SPEECH);
        channelModeStruct.setAlgOrRate(AlgOrRate.GSM1);
        
        ChannelNoStruct channelNoStruct1 = new ChannelNoStruct();
        channelNoStruct1.setTimeSlotNo(2); 
        channelNoStruct1.setChannelType(ChannelType.Bm); 
        activationTypeStruct.setActivationType(ActivationType.INTRA_NOR);
        channelModeStruct.setChannelRate(ChannelRate.Bm); 
        
        channelActNormalAssign.setChannelNoStruct(channelNoStruct1);
        channelActNormalAssign.setActivationTypeStruct(activationTypeStruct);
        channelActNormalAssign.setChannelModeStruct(channelModeStruct);
        
        BSPowerStruct bspowerstruct = new BSPowerStruct();
        bspowerstruct.setBSPower(TRAFFRN.Enums.BSPower.Pn);
        bspowerstruct.setReserved(0);
        channelActNormalAssign.setBSPowerStruct(bspowerstruct);

        
        ChannelActAck temp = channelActNormalAssign.send();
        setTestInfo("Printing channelActAck " + temp);
    }
    private void  sendDataBsPower(boolean extraElement) throws InterruptedException {
    	setTestInfo("Sending sendData::BS Power Control");        
    	
    	SendData bitstream = abisHelper.getAbiscoClient().getBITSTREAM().createSendData();
        
        Routing routing = new Routing();
        routing.setTG(0);
        routing.setTRXC(0);
        
       LAPD lapd = new LAPD();
        lapd.setPCM_SYSTEM(0);
        lapd.setPCM_TIMESLOT(1);
        lapd.setPCM_STARTBIT(0);
        lapd.setPCM_SAPI(0);
        lapd.setPCM_TEI(0);
        routing.setLAPD(lapd);
        bitstream.setRouting(routing);

        Behaviour behaviour = new Behaviour();
        behaviour.setEP1_BEH(BITSTREAM.Enums.EP1_BEH.NORMAL);
        behaviour.setEP1_TIMER(0);
        behaviour.setEP1_TXCNT(0);
        behaviour.setTIMER1(0);
        bitstream.setBehaviour(behaviour);
        
        if (extraElement){
        	//BS Power Control, TS=2, Pn-1, extra BS power parameter (Not supported by Eric)
        	bitstream.setData(Arrays.asList(8,48,1,10,4,1,32,3,0,1,255));
        }else{
        	//BS Power Control, TS=2, Pn-16 (out of range)
        	bitstream.setData(Arrays.asList(8,48,1,10,4,16));
        }
                
        ReceiveData response = bitstream.send(); 
        setTestInfo("response " + response);
        
        verifyResponseData(response, extraElement);
    	  
    }
    
    private void  verifyResponseData(ReceiveData response,boolean extraElement) throws InterruptedException {
    	setTestInfo("Verify response data from BTS");
    	if (extraElement){
        	if (response.getData().get(1) == 28 && response.getData().get(4) == 102){
        		setTestDebug("Expected ERROR REPORT with Cause value 102 received from BTS ");
        		setTestDebug("102:Information Element non-existent");
        	}else{
        		fail("Expected ERROR REPORT with Cause value" + response.getData().get(4) + " not received ");
        	}
        }else{
        	if (response.getData().get(1) == 28 && response.getData().get(4) == 104){
        		setTestDebug("Expected ERROR REPORT with Cause value 104 received from BTS ");
        		setTestDebug("104:Invalid Information Element Contents");
        	}else{
        		fail("Expected ERROR REPORT with Cause value" + response.getData().get(4) + " not received ");
        	}
        }
      }
    
    


    private void  rfChannelRelease() throws InterruptedException {
	setTestInfo("Sending rfChannelRelease on Abis");
    
	TRAFFRN traffrn = abisHelper.getRslServer();
    RFChannelRelease rFChannelRelease = traffrn.createRFChannelRelease();
    
    ChannelNoStruct channelNoStruct = new ChannelNoStruct();   
    channelNoStruct.setTimeSlotNo(2); 
    channelNoStruct.setChannelType(ChannelType.Bm); 
    rFChannelRelease.setChannelNoStruct(channelNoStruct);
    
    rFChannelRelease.send();
    }
}
