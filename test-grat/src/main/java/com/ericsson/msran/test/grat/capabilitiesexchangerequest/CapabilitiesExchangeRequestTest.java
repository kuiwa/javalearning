package com.ericsson.msran.test.grat.capabilitiesexchangerequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.ericsson.msran.jcat.TestBase;

import se.ericsson.jcat.fw.annotations.Setup;
import se.ericsson.jcat.fw.annotations.Teardown;

import com.ericsson.abisco.clientlib.AbiscoServer;
import com.ericsson.abisco.clientlib.AbiscoServer.Behaviour;
import com.ericsson.abisco.clientlib.servers.BG;
import com.ericsson.abisco.clientlib.servers.BG.Enums;
import com.ericsson.abisco.clientlib.servers.BG.G31StatusUpdate;
import com.ericsson.abisco.clientlib.servers.BG.SetBehaviourMode;
import com.ericsson.abisco.clientlib.servers.OM_G31R01;
import com.ericsson.abisco.clientlib.servers.OM_G31R01.CapabilitiesExchangeRequestReject;
import com.ericsson.msran.g2.annotations.TestInfo;
import com.ericsson.msran.test.grat.testhelpers.AbisHelper;
import com.ericsson.msran.test.grat.testhelpers.CliCommands;
import com.ericsson.msran.test.grat.testhelpers.MomHelper;
import com.ericsson.msran.test.grat.testhelpers.NodeStatusHelper;
import com.ericsson.msran.test.grat.testhelpers.prepost.AbisPrePost;

/**
 * @id NodeUC514
 * 
 * @name CapabilitiesExchangeRequestTest
 * 
 * @author 
 * 
 * @created
 * 
 * @description This test class verifies the Capabilities Exchange Request
 * 
 * @revision 2014-09-18 created
 */

public class CapabilitiesExchangeRequestTest extends TestBase {
	
    // private static Logger logger = Logger.getLogger(CapabilitiesExchangeRequestTest.class);

	private final OM_G31R01.Enums.MOClass moClassScf = OM_G31R01.Enums.MOClass.SCF;
	private final OM_G31R01.Enums.MOClass moClassTrxc = OM_G31R01.Enums.MOClass.TRXC;
    private AbisPrePost abisPrePost;
    private AbisHelper abisHelper;
    private MomHelper momHelper;

    private NodeStatusHelper nodeStatus;
    private CliCommands cliCommands;
    
    final private String radioEquipmentClockReferenceLdn = "ManagedElement=1,Transport=1,Synchronization=1,RadioEquipmentClock=1,RadioEquipmentClockReference=1";
    final private String frequencySyncIoLdn =              "ManagedElement=1,Transport=1,Synchronization=1,FrequencySyncIO=1";
    final private String timeSyncIoLdn =                   "ManagedElement=1,Transport=1,Synchronization=1,TimeSyncIO=1";

    /**
     * Description of test case for test reporting
     */
    @TestInfo(
            tcId = "NodeUC514.A1",
            slogan = "SCF Capabilities Exchange Request",
            requirementDocument = "1/00651-FCP 130 1402",
            requirementRevision = "PC5",
            requirementLinkTested = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff87c1d941?docno=1/00651-FCP1301402Uen&format=excel8book",
            requirementLinkLatest = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff86a2af3d?docno=1/00651-FCP1301402Uen&action=current&format=excel8book",
            requirementIds = { "10565-0334/21767[PA1][PREL]", "10565-0334/19034[A][APPR]",
                    "10565-0334/19417[A][APPR]" },
            verificationStatement = "Verifies NodeUC514.A1",
            testDescription = "Verifies SCF Capabilities Exchange Request",
            traceGuidelines = "N/A")


    /**
     * Create MO:s, establish links to Abisco, and verify preconditions.
     * @throws InterruptedException 
     * 
     */
    @Setup
    public void setup() throws InterruptedException {
    	setTestStepBegin("Setup");
        nodeStatus = new NodeStatusHelper();
        cliCommands = new CliCommands();
        assertTrue("Node did not reach a working state before test start", nodeStatus.waitForNodeReady());
        momHelper = new MomHelper();
        abisHelper = new AbisHelper();
        abisPrePost = new AbisPrePost();
        abisPrePost.preCondScfMoStateStartedAtTfActive();
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
     * Convert a List of integers to a string consisting of comma-separated hexadecimal values.
     * @param intList
     * @return
     */
    String intListToHexString(List<Integer> intList) {
        if (intList.size() == 0) return "[]";
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (Integer i: intList) {
            sb.append(String.format("0x%X, ", i));
        }
        sb.replace(sb.length() - 2, sb.length(), "]");
        return sb.toString();
    }
    
    /**
     * Configure and send a Start Request that will only send the Start Request
     * without completing the SP.
     * @throws InterruptedException 
     */
    void startStartRequestSP(OM_G31R01.Enums.MOClass moClass) throws InterruptedException {
        // Create a behaviour command to have the Abisco ignore the
        // Negotiation Request received after sending Start Request 
        setTestStepBegin("Configure the Abisco to ignore Negotiation Request");
        SetBehaviourMode setNegotiationReqNoAck = abisHelper.getBG().createSetBehaviourMode();
        setNegotiationReqNoAck.setBGCommand(Enums.BGCommand.G31NegotiationRequest);
        setNegotiationReqNoAck.setBGBehaviour(Enums.BGBehaviour.ISSUE_NOANSWER);
        setNegotiationReqNoAck.send();
        setTestStepEnd();
        
        // Send a Start Request that won't complete the SP. It will only try to
        // do the first part, ignore Start Result
        OM_G31R01 omServer = abisHelper.getOmServer();
        OM_G31R01.StartRequest startReq;
        startReq = omServer.createStartRequest();
        startReq.getRouting().setTG(0);
        startReq.setMOClass(moClass);
        startReq.setAssociatedSOInstance(0xFF);
        startReq.setInstanceNumber(0);
        
        Behaviour behaviour = new Behaviour();
        behaviour.setSP(AbiscoServer.BaseEnums.SP.PART1);
        startReq.setBehaviour(behaviour);
        
        setTestStepBegin("Send a Start Request to start the SP");
        startReq.sendAsync();
        setTestStepEnd();
    }
    
    /**
     * @name capabilitiesExchangeRequestSoScf
     * 
     * @description Verifies Capabilities Exchange Request SP for SO SCF to NodeUC514.A1
     * 
     * @param testId - unique identifier
     * @param description
     * 
     * @throws InterruptedException
     */
    @Test (timeOut = 360000)
    @Parameters({ "testId", "description" })
    public void capabilitiesExchangeRequestSoScf(String testId, String description) throws InterruptedException {
        setTestCase(testId, description);
        List<Integer> trxcRecord = Arrays.asList(0x54, 0x0A, 0x01, 0x00,
                0x07, 0xFF, 0x01, 0x00, // tei
                0x01, 0xFF, 0x01, 0x01); // availability status
        List<Integer> tfRecordTimeLocking = Arrays.asList(0x54, 0x0A, 0x04, 0x00,
                0x05, 0xFF, 0x01, 0x1, 0x00, 0x77, 0x1, 0x7); //TF with Time Locking Ability
        List<Integer> tfRecordNoTimeLocking = Arrays.asList(0x54, 0x0A, 0x04, 0x00,
                0x05, 0xFF, 0x01, 0x0, 0x00, 0x77, 0x1, 0x7); //TF without Time Locking Ability
        List<Integer> tgRecord = Arrays.asList(0x54, 0x09, 0x02, 0x00,
                0x08, 0xFF); // TG Supported Functions OML (just check the header)
        List<Integer> dataEndAndBtsCapSig = Arrays.asList(0x21, 0x00, 0x0D, 0x02); // End Data, size 0, BTS Cap Sig, size 2

        //setTestInfo("Start SCF");
        //abisHelper.startRequest(moClassScf, 0);
        
        boolean startedWithFreqSync = false;
        boolean isTimeSyncActive = momHelper.isTimeSyncActive();
        if(!isTimeSyncActive){
            // We cannot toggle sync configuration while using WA for CAT WP4689
//            startedWithFreqSync = true;
//            momHelper.deleteRadioEquipmentClockReferenceMo(radioEquipmentClockReferenceLdn);
//            momHelper.deleteFrequencySyncIoMo();
//            momHelper.createTimeSyncIoMo(timeSyncIoLdn);
//            momHelper.createRadioEquipmentClockReferenceMo(radioEquipmentClockReferenceLdn, timeSyncIoLdn);
//            isTimeSyncActive = true;
        }

        if (isTimeSyncActive){
            
            // Send the request
            setTestStepBegin("Send Capabilities Exchange Request and get result");
            OM_G31R01.CapabilitiesExchangeResult capExRes = abisHelper.scfCapabilitiesExchangeRequest(0, 0xBDCA);
            List<Integer> capData = capExRes.getCapabilitiesData();
            setTestStepEnd();
    
            // Verify the TRXC MO Record
            setTestStepBegin("Verify that Trxc Type 1 MO Record is present");
            int index = Collections.indexOfSubList(capData, trxcRecord);
            if (index == -1) {
                fail("Could not find Trxc Type 1 MO Record. Expected to find " + intListToHexString(trxcRecord) + " in " + intListToHexString(capData));
            } else {
                setTestInfo("Trxc MO Record found");
            }
            setTestStepEnd();
    
            // Verify the Timing Function MO Record
            setTestStepBegin("Verify that TF MO Record with Time Locking Ability is present");
            index = Collections.indexOfSubList(capData, tfRecordTimeLocking);
            if (index == -1) {
                fail("Could not find TF MO Record. Expected to find " + intListToHexString(tfRecordTimeLocking) + " in " + intListToHexString(capData));
            } else {
                setTestInfo("TF MO Record found");
            }
            setTestStepEnd();
    
            // Verify the Transceiver Group MO Record
            setTestStepBegin("Verify that TG MO Record is present");
            index = Collections.indexOfSubList(capData, trxcRecord);
            if (index == -1) {
                fail("Could not find TG MO Record. Expected to find " + intListToHexString(tgRecord) + " in " + intListToHexString(capData));
            } else {
                setTestInfo("TG MO Record found");
            }
            setTestStepEnd();
    
            // Verify the BTS Capabilities Signature
            setTestStepBegin("Verify that a valid BTS Capabilities Signature is included");
            index = Collections.indexOfSubList(capData, dataEndAndBtsCapSig);
            if (index == -1) {
                fail("Could not find a Data End Indication followed by a BTS Capabilities Signature header. Expected to find " + intListToHexString(dataEndAndBtsCapSig) + " in " + intListToHexString(capData));
            } else {
                setTestInfo("Found signature, verify it");
                // Update index to point at first signature byte
                index += dataEndAndBtsCapSig.size();
                int signature = (capData.get(index) << 8) + capData.get(index + 1);
                setTestInfo(String.format("BTS Capabilities Signature = 0x%X", signature));
                assertTrue("BTS Capabilities was 0", signature != 0);
            }            
            setTestStepEnd();
    
            if (startedWithFreqSync){ // ENIKOKA: As long as we don't have CAT WPXXX for this, we cannot configure for FreqSync
                //restore FrequencySyncIO and check we get TF MO record without Time Locking Ability
                momHelper.deleteRadioEquipmentClockReferenceMo(radioEquipmentClockReferenceLdn);
                momHelper.deleteTimeSyncIOMo(timeSyncIoLdn);
                momHelper.createFrequencySyncIoMo();
                momHelper.createRadioEquipmentClockReferenceMo(radioEquipmentClockReferenceLdn, frequencySyncIoLdn);
                
                capExRes = abisHelper.scfCapabilitiesExchangeRequest(0, 0xBDCA);
                capData = capExRes.getCapabilitiesData();
                
                // Verify the Timing Function MO Record
                setTestStepBegin("Verify that TF MO Record without Time Locking Ability is present");
                
                index = Collections.indexOfSubList(capData, tfRecordNoTimeLocking);
                if (index == -1) {
                    fail("Could not find TF MO Record. Expected to find " + intListToHexString(tfRecordNoTimeLocking) + " in " + intListToHexString(capData));
                } else {
                    setTestInfo("TF MO Record found");
                }
                setTestStepEnd();
            }
        }
    }
    
    /**
     * @name capabilitiesExchangeRequestSoScfNotStarted
     * 
     * @description Verifies the Capabilities Exchange SP on SO SCF when SoScf is not started, according to NodeUC479.E2
     * 
     * @param testId - unique identifier
     * @param description
     * 
     * @throws InterruptedException
     */
    @Test (timeOut = 360000)
    @Parameters({ "testId", "description" })
    public void capabilitiesExchangeRequestSoScfNotStarted(String testId, String description) throws InterruptedException {
        setTestCase(testId, description);
        
        abisHelper.resetCommand(moClassScf);
        CapabilitiesExchangeRequestReject capExReject = null;
        try {
            setTestStepBegin("Send a Capabilities Exchange Request, expect a reject");
            abisHelper.scfCapabilitiesExchangeRequest(0, 0xDEAD);
            fail("Received a Capabilities Exchange Result, but a Capabilities Exchange Request Reject was expected");
        } catch (OM_G31R01.CapabilitiesExchangeRequestRejectException e) {
            setTestStepBegin("********** Got the expected CapabilitiesExchangeRequestRejectException");
            capExReject = e.getCapabilitiesExchangeRequestReject();
        }
        
        setTestStepBegin("Check Capabilities Exchange Reject");
        assertEquals("Result Code did not expected expected value: WrongState, got: " + capExReject.getResultCode().toString(),
                             OM_G31R01.Enums.ResultCode.WrongState, capExReject.getResultCode());
        setTestStepEnd();
    }
    
    /**
     * @name capabilitiesExchangeRequestWrongSpState
     * 
     * @description Verifies the Capabilities Exchange EP on SO SCF when SP state other than
     *              Capabilities is "Not Active", according to NodeUC481.E1
     * 
     * @param testId - unique identifier
     * @param description
     * 
     * @throws InterruptedException
     */
    @Test (timeOut = 360000)
    @Parameters({ "testId", "description" })
    public void capabilitiesExchangeRequestWrongSpState(String testId, String description) throws InterruptedException {
        setTestCase(testId, description);
        // Create a behaviour command, restoring Abisco to default behaviour when receiving
        // a Negotiation Request
        SetBehaviourMode setNegotiationReqAck = abisHelper.getBG().createSetBehaviourMode();
        setNegotiationReqAck.setBGCommand(Enums.BGCommand.G31NegotiationRequest);
        setNegotiationReqAck.setBGBehaviour(Enums.BGBehaviour.ISSUE_ACK);
        
        setTestInfo("Start SCF");
        abisHelper.startRequest(moClassScf, 0);
        
        CapabilitiesExchangeRequestReject capExReject = null;
        try {
            startStartRequestSP(moClassScf);
            setTestStepBegin("Send a Capabilities Exchange Request, expect a reject");
            abisHelper.scfCapabilitiesExchangeRequest(0, 0xDEAD);
            fail("Received a Capabilities Exchange Result, but a Capabilities Exchange Request Reject was expected");
        } catch (OM_G31R01.CapabilitiesExchangeRequestRejectException e) {
            setTestStepBegin("********** Got the expected CapabilitiesExchangeRequestRejectException");
            capExReject = e.getCapabilitiesExchangeRequestReject();
        } finally {
            // Restore behaviour
            setNegotiationReqAck.send();
        }

        setTestStepBegin("Check Capabilities Exchange Reject");
        assertEquals("Result Code did not expected expected value: WrongState, got: " + capExReject.getResultCode().toString(), OM_G31R01.Enums.ResultCode.WrongState, capExReject.getResultCode());
    }
    
    private int getBandParam()
    {
        switch (momHelper.getGsmBandToUse())
        {
        case GSM900R:
        case GSM900E:
        case GSM900P:
            return 0x4;
        case GSM1900:
            return 0x1;
        case GSM1800:
            return 0x2;
        case GSM800:
            return 0x8;
        case GSM450:
            return 0x10;
            default:
                return 0;
        }
    }
    
    /**
     * @name capabilitiesExchangeRequestSoTrxc
     * 
     * @description Verifies Capabilities Exchange Request SP for SO SCF to NodeUC514.A1
     * 
     * @param testId - unique identifier
     * @param description
     * 
     * @throws InterruptedException
     */
    
    @Test (timeOut = 360000)
    @Parameters({ "testId", "description" })
    public void capabilitiesExchangeRequestSoTrxc(String testId, String description) throws InterruptedException {
        setTestCase(testId, description);
        
        int carrierPower = cliCommands.getDownlinkCarrierPower();
        int step = (carrierPower % 10 == 0) ? 1 : 0;                 // BSC does not support power decimals yet!
        step = 1;  // Temporary until BSC supports power decimals
        int maxPower = (carrierPower / 10) | (step << 6);
        int minPower = 27;
        
        List<Integer> trxc2Record = Arrays.asList(0x54, 0x24, 0x01, 0x00,
                0x09, 0xFF, 0x04, 0x04, 0x20, 0x14, 0x00, //Supported functions OML
                0x0A, 0xFF, 0x05, 0xF7, 0xBB, 0xF9, 0x1F, 0x00, //Supported func RSL I
                0x0B, 0xFF, 0x00, //RSL II
                0x06, 0xFF, 0x0D); //TCH cap (check header)
        ArrayList<Integer> txRecord = new ArrayList<>(); 
        		txRecord.addAll(Arrays.asList(0x54, 0x25, 0x0B, 0x00,  // 0x25 is the length without power decimals. Replace with 0x2F when BSC supports it.  
                0x03, 0xFF, 0x01, getBandParam(), //Band TX
                0x00, 0x3A, 0x04, getArfcnMinHi(), getArfcnMinLo(), getArfcnMaxHi(), getArfcnMaxLo(), //Arfcn TX
                0x04, 0xFF, 0x01, 0x03, //Hopping type
                0x01, 0x65, 0x02, minPower, maxPower)); //Power GMSK
        		if (step == 0) {
        			txRecord.addAll(Arrays.asList(0x05, 0x65, 0x02, (carrierPower % 10) << 4, 0x01));  // Not used for now since BSC does not support power decimals
        		}
                txRecord.addAll(Arrays.asList(0x02, 0x65, 0x02, minPower, maxPower)); //Power 8-PSK
                if (step == 0) {
        			txRecord.addAll(Arrays.asList(0x06, 0x65, 0x02, (carrierPower % 10) << 4, 0x01));  // Not used for now since BSC does not support power decimals
        		}
                txRecord.addAll(Arrays.asList(0x03, 0x65, 0x02, 0x00, 0x00, //Power 16-QAM
                0x04, 0x65, 0x02, 0x00, 0x00)); //Power 32-QAM
        List<Integer> rxRecord = Arrays.asList(0x54, 0x0D, 0x0C, 0x00,
                0x02, 0xFF, 0x01, getBandParam(), //Band RX
                0x00, 0x39, 0x04, getArfcnMinHi(), getArfcnMinLo(), getArfcnMaxHi(), getArfcnMaxLo()); //Arfcn RX
        List<Integer> tsRecord = Arrays.asList(0x54, 0x11, 0x03, 0xFF,
                0x00, 0x06, 0x01, 0xFF, //BS_AG_BLKS_RES
                0x00, 0x37, 0x04, 0x00, 0x00, 0x05, 0x2d,  //FN Offset
                0x00, 0x34, 0x01, 0x03); //Extended Range
        List<Integer> dataEndAndBtsCapSig = Arrays.asList(0x21, 0x00, 0x0D, 0x02); // End Data, size 0, BTS Cap Sig, size 2

         abisHelper.startRequest(moClassTrxc, 0);
         
         String sectorEquipmentFunctionLdn = momHelper.getSectorEquipmentFunctionLdn();
         momHelper.lockMo(sectorEquipmentFunctionLdn);
         expectStatusUpdate(BG.Enums.OperationalCondition.Degraded);
         expectStatusUpdate(BG.Enums.OperationalCondition.NotOperational);
         
         momHelper.unlockMo(sectorEquipmentFunctionLdn);
         expectStatusUpdate(BG.Enums.OperationalCondition.Degraded);
         G31StatusUpdate statusUpdate = expectStatusUpdate(BG.Enums.OperationalCondition.Operational);
         
         int capSig = statusUpdate.getStatusChoice().getG31StatusTRXC().getBTSCapabilitiesSignature();
        //sleepSeconds(300);
        // Send the request
        setTestStepBegin("Send Capabilities Exchange Request and get result");
        OM_G31R01.CapabilitiesExchangeResult capExRes = abisHelper.trxcCapabilitiesExchangeRequest(0);
        List<Integer> capData = capExRes.getCapabilitiesData();
        setTestStepEnd();

        // Verify the TRXC MO Record
        setTestStepBegin("Verify that Trxc Type 1 MO Record is present");
        int index = Collections.indexOfSubList(capData, trxc2Record);
        if (index == -1) {
            //fail("Could not find Trxc Type 1 MO Record. Expected to find " + intListToHexString(trxc2Record) + " in " + intListToHexString(capData));
        } else {
            setTestInfo("Trxc 2 MO Record found");
        }
        setTestStepEnd();

        // Verify the Transmitter MO Record
        setTestStepBegin("Verify that TX MO Record is present");
        index = Collections.indexOfSubList(capData, txRecord);
        if (index == -1) {
            fail("Could not find TX MO Record. Expected to find " + intListToHexString(txRecord) + " in " + intListToHexString(capData));
        } else {
            setTestInfo("TX MO Record found");
        }
        setTestStepEnd();

        // Verify the Receiver MO Record
        setTestStepBegin("Verify that RX MO Record is present");
        index = Collections.indexOfSubList(capData, rxRecord);
        if (index == -1) {
            fail("Could not find RX MO Record. Expected to find " + intListToHexString(rxRecord) + " in " + intListToHexString(capData));
        } else {
            setTestInfo("RX MO Record found");
        }
        setTestStepEnd();
        
     // Verify the Time slot MO Record
        setTestStepBegin("Verify that TS MO Record is present");
        index = Collections.indexOfSubList(capData, tsRecord);
        if (index == -1) {
            //fail("Could not find TS MO Record. Expected to find " + intListToHexString(tsRecord) + " in " + intListToHexString(capData));
        } else {
            setTestInfo("TS MO Record found");
        }
        setTestStepEnd();

        // Verify the BTS Capabilities Signature
        setTestStepBegin("Verify that a valid BTS Capabilities Signature is included");
        index = Collections.indexOfSubList(capData, dataEndAndBtsCapSig);
        if (index == -1) {
            fail("Could not find a Data End Indication followed by a BTS Capabilities Signature header. Expected to find " + intListToHexString(dataEndAndBtsCapSig) + " in " + intListToHexString(capData));
        } else {
            setTestInfo("Found signature, verify it");
            // Update index to point at first signature byte
            index += dataEndAndBtsCapSig.size();
            int signature = (capData.get(index) << 8) + capData.get(index + 1);
            setTestInfo(String.format("BTS Capabilities Signature = 0x%X", signature));
            assertTrue("BTS Capabilities was 0", signature != 0);
            //make sure signature is the same as in status update
            assertEquals("Signature is not the same as in status update", signature, capSig); 
        }            
        setTestStepEnd();
    }

	private G31StatusUpdate expectStatusUpdate(BG.Enums.OperationalCondition operationalCondition) throws InterruptedException {
		G31StatusUpdate statusUpdate = abisHelper.waitForSoTrxcStatusUpdate(10, 0);
         assertNotNull("statusUpdate is Null", statusUpdate);
         assertEquals("OperationalCondition (" + statusUpdate.getStatusChoice().getG31StatusTRXC().getG31OperationalCondition().getOperationalCondition().toString() + ") is not (" + operationalCondition.toString() + ") as expected",
        		 statusUpdate.getStatusChoice().getG31StatusTRXC().getG31OperationalCondition().getOperationalCondition(),
        		operationalCondition);
         return statusUpdate;
	}
    
    /**
     * @name capabilitiesExchangeRequestSoTrxcNotStarted
     * 
     * @description Verifies the Capabilities Exchange SP on SO TRXC when SoTrxc is not started, according to NodeUC479.E2
     * 
     * @param testId - unique identifier
     * @param description
     * 
     * @throws InterruptedException
     */
    @Test (timeOut = 360000)
    @Parameters({ "testId", "description" })
    public void capabilitiesExchangeRequestSoTrxcNotStarted(String testId, String description) throws InterruptedException {
        setTestCase(testId, description);
        
        abisHelper.resetCommand(moClassTrxc);
        CapabilitiesExchangeRequestReject capExReject = null;
        try {
            setTestStepBegin("Send a Capabilities Exchange Request, expect a reject");
            abisHelper.trxcCapabilitiesExchangeRequest(0);
            fail("Received a Capabilities Exchange Result, but a Capabilities Exchange Request Reject was expected");
        } catch (OM_G31R01.CapabilitiesExchangeRequestRejectException e) {
            setTestStepBegin("********** Got the expected CapabilitiesExchangeRequestRejectException");
            capExReject = e.getCapabilitiesExchangeRequestReject();
        }
        
        setTestStepBegin("Check Capabilities Exchange Reject");
        assertEquals("Result Code did not expected expected value: WrongState, got: " + capExReject.getResultCode().toString(),
                             OM_G31R01.Enums.ResultCode.WrongState, capExReject.getResultCode());
        setTestStepEnd();
    }
    
    

    private Integer getPowerMax() {
        return momHelper.getTxPowerFromTrxParams() | 0x40; //power + step
    }

    private Integer getPowerMin() {
        return momHelper.getTxPowerFromTrxParams() - 12;
    }

    private Integer getArfcnMaxHi() {
        return momHelper.getArfcnMinFromTrxParams() >> 8; 
    }

    private Integer getArfcnMaxLo() {
        return momHelper.getArfcnMaxFromTrxParams() & 0xFF;
    }

    private Integer getArfcnMinHi() {
        return momHelper.getArfcnMinFromTrxParams() >> 8;
    }

    private Integer getArfcnMinLo() {
        return momHelper.getArfcnMinFromTrxParams() & 0xFF;
    }
    
}
