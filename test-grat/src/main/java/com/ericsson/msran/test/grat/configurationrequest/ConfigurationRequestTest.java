package com.ericsson.msran.test.grat.configurationrequest;

import java.util.concurrent.TimeUnit;
import java.util.List;
import java.util.Arrays;
import org.json.JSONException;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import com.ericsson.msran.jcat.TestBase;
import se.ericsson.jcat.fw.annotations.Setup;
import se.ericsson.jcat.fw.annotations.Teardown;
import com.ericsson.abisco.clientlib.servers.OM_G31R01;
import com.ericsson.abisco.clientlib.servers.OM_G31R01.Enums;
import com.ericsson.abisco.clientlib.servers.OM_G31R01.FSOffset;
import com.ericsson.abisco.clientlib.AbiscoServer.Behaviour;
import com.ericsson.abisco.clientlib.servers.OM_G31R01.TSConfigurationRequestRejectException;
import com.ericsson.abisco.clientlib.servers.OM_G31R01.Enums.TFResultControl;
import com.ericsson.abisco.clientlib.servers.BG.G31StatusUpdate;
import com.ericsson.abisco.clientlib.servers.BG.G31OperationalCondition;
import com.ericsson.abisco.clientlib.servers.BG.G31OperationalConditionReasonsMap;
import com.ericsson.abisco.clientlib.servers.OM_G31R01.TXConfigurationRequestRejectException;
import com.ericsson.msran.g2.annotations.TestInfo;
import com.ericsson.msran.test.grat.testhelpers.AbisHelper;
import com.ericsson.msran.test.grat.testhelpers.AbiscoConnection;
import com.ericsson.msran.test.grat.testhelpers.MomHelper;
import com.ericsson.msran.test.grat.testhelpers.NodeStatusHelper;
import com.ericsson.msran.test.grat.testhelpers.prepost.AbisPrePost;
import org.apache.log4j.Logger;

/**
 * @todo Uncomment unused imports when implementing tfConfigurationRequestFsOffset
 */

/**
 * @id NodeUC512
 * 
 * @name ConfigurationRequestTest
 * 
 * @author GRAT Cell
 * 
 * @created 2014-01-30
 * 
 * @description This test class verifies the Configuration Request
 * 
 * @revision See Git for revision history.
 *
 */

public class ConfigurationRequestTest extends TestBase {
    
	private static Logger logger = Logger.getLogger(ConfigurationRequestTest.class);
    private AbisPrePost abisPrePost;
    private static final String radioClockLdn = "ManagedElement=1,Transport=1,Synchronization=1,RadioEquipmentClock=1";
    private static final List<String> frequencyRefValid = Arrays.asList("FREQUENCY_HOLDOVER, FREQUENCY_LOCKED", "TIME_OFFSET_HOLDOVER", "TIME_OFFSET_LOCKED", "RNT_TIME_HOLDOVER", "RNT_TIME_LOCKED");
    private static final List<String> timeRefValid = Arrays.asList("TIME_OFFSET_HOLDOVER", "TIME_OFFSET_LOCKED", "RNT_TIME_HOLDOVER", "RNT_TIME_LOCKED");
    private final OM_G31R01.Enums.MOClass moClassTf = OM_G31R01.Enums.MOClass.TF;
    private final OM_G31R01.Enums.MOClass moClassTx = OM_G31R01.Enums.MOClass.TX;
    private final OM_G31R01.Enums.MOClass moClassRx = OM_G31R01.Enums.MOClass.RX;
    private final OM_G31R01.Enums.MOClass moClassTs = OM_G31R01.Enums.MOClass.TS;
    final private String radioEquipmentClockReferenceLdn = "ManagedElement=1,Transport=1,Synchronization=1,RadioEquipmentClock=1,RadioEquipmentClockReference=1";
    final private String timeSyncIoLdn = "ManagedElement=1,Transport=1,Synchronization=1,TimeSyncIO=1";
    private final int POWER_LEVEL = 27;

    private AbisHelper abisHelper;
    private MomHelper momHelper;
    private AbiscoConnection abiscoConnection;
    private final String GSM_SECTOR_LDN = "ManagedElement=1,BtsFunction=1,GsmSector=1";
    private NodeStatusHelper nodeStatus;

    /**
     * Description of test case for test reporting
     */
    @TestInfo(
            tcId = "NodeUC512",
            slogan = "Abis TF Configuration",
            requirementDocument = "1/00651-FCP 130 1402",
            requirementRevision = "PC5",
            requirementLinkTested = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff87c1d941?docno=1/00651-FCP1301402Uen&format=excel8book",
            requirementLinkLatest = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff86a2af3d?docno=1/00651-FCP1301402Uen&action=current&format=excel8book",
            requirementIds = { "10565-0334/21767[PA1][PREL]", "10565-0334/19034[A][APPR]",
                    "10565-0334/19417[A][APPR]" },
            verificationStatement = "Verifies NodeUC512.N and NodeUC512.E1",
            testDescription = "Verifies Abis TF Configuration.",
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
        momHelper = new MomHelper();        
        abisHelper = new AbisHelper();
        abiscoConnection = new AbiscoConnection();
        abisPrePost = new AbisPrePost(2);
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
     * @name tfConfigurationRequest
     * 
     * @description Verifies TF Configuration Request according to NodeUC512.N
     * 
     * @param testId - unique identifier
     * @param description
     * 
     * @throws InterruptedException
     */
    @Test (timeOut = 360000)
    @Parameters({ "testId", "description" })
    public void tfConfigurationRequest(String testId, String description) throws InterruptedException {
        
        setTestCase(testId, description);

        /*
         * Case 1: cluster group id 1
         */
        setTestStepBegin("Send TF Configuration Request (cluster group id 1)");
        
        OM_G31R01.TFConfigurationResult confRes = abisHelper.tfConfigRequest(1);
        int confSignature1 = confRes.getConfigurationSignature();

        setTestInfo("Verify MO:GsmSector attribute abisClusterGroupId is 1");
        assertTrue("MO " + GSM_SECTOR_LDN + " abisClusterGroupId is not 1",
                momHelper.waitForMoAttributeStringValue(MomHelper.SECTOR_LDN, "abisClusterGroupId", "1", 6));

        /*
         * Case 2: cluster group id Undefined
         */
        setTestStepBegin("Send TF Configuration Request (cluster group id Undefined)");

        setTestInfo("Reset and start TF for cluster group id chaning is not allowed after TF enable");
        abisHelper.resetCommand(this.moClassTf);

        abisHelper.startRequest(this.moClassTf, 0);
        setTestInfo("Precondition: Verify that MO:GsmSector attribute:abisTfMoState is DISABLE");
        assertTrue("MO " + GSM_SECTOR_LDN + " abisTfState is not DISABLE",
                momHelper.waitForMoAttributeStringValue(GSM_SECTOR_LDN, "abisTfState", "DISABLE", 6));

        confRes = abisHelper.tfConfigRequest(65535);
        int confSignature2 = confRes.getConfigurationSignature();
        setTestInfo("Verify MO:GsmSector attribute abisClusterGroupId is empty");
        assertTrue("MO " + GSM_SECTOR_LDN + " abisClusterGroupId is not empty",
                momHelper.waitForMoAttributeStringValue(MomHelper.SECTOR_LDN, "abisClusterGroupId", "", 6));

        assertNotSame("confSignature is unexpectedly the same", confSignature1, confSignature2);


        /*
         * Case 3: TF control attribute does not affect configuration signature
         */

        OM_G31R01.TFConfigurationRequest confReq = abisHelper.initTfConfigRequest();
        confReq.setClusterGroupId(65535);
        confReq.setTFResultControl(TFResultControl.ResultOnChange);
        assertEquals("confSignature (" + confReq.send().getConfigurationSignature() + ") is not the same as the expected (" + confSignature2 + ")", 
        		confReq.send().getConfigurationSignature(), confSignature2);

        /*
         * Case 4: Abis TF Configuration with same configuration signature
         */
        setTestStepBegin("Abis TF Configuration with same configuration signature");

        OM_G31R01.TFConfigurationRequest confReq1 = abisHelper.initTfConfigRequest();
        confReq1.setClusterGroupId(65535);

        OM_G31R01.TFConfigurationRequest confReq2 = abisHelper.initTfConfigRequest();
        confReq2.setClusterGroupId(65535);
        assertEquals("confSignature (" + confReq2.send().getConfigurationSignature() + ") is not the same as the expected (" + confReq1.send().getConfigurationSignature() + ")", 
        		confReq2.send().getConfigurationSignature(), confReq1.send().getConfigurationSignature());

        /*
         * Case 5: TF Mode is MASTER(0)
         */
        setTestStepBegin("Send TF configuration Request (Tf Mode is MASTER)");

        OM_G31R01.TFConfigurationRequest confReq3 = abisHelper.initTfConfigRequest();
        confReq3.setTFMode(Enums.TFMode.Master);
        confReq3.setFSOffset(new FSOffset(new Integer(0xFF), new Long(0xFFFFFFFFL)));
        OM_G31R01.TFConfigurationResult confRes3 = confReq3.send();
        assertEquals("AccordanceIndication (" + confRes3.getAccordanceIndication().getAccordanceIndication().toString() + ") is not (" + Enums.AccordanceIndication.AccordingToRequest.toString() + ") as expected", Enums.AccordanceIndication.AccordingToRequest, confRes3.getAccordanceIndication().getAccordanceIndication());

        setTestInfo("Verify MO:GsmSector attribute abisTfMode is MASTER");
        assertTrue("MO " + GSM_SECTOR_LDN + " abisTfMode is not MASTER",
                momHelper.waitForMoAttributeStringValue(MomHelper.SECTOR_LDN, "abisTfMode", "MASTER", 6));

        setTestStepEnd();

        /*
         * Case 6: TF Mode is STANDALONE(1)
         */
        setTestStepBegin("Send TF configuration Request (Tf Mode is STANDALONE)");

        confReq3 = abisHelper.initTfConfigRequest();
        confReq3.setTFMode(Enums.TFMode.Standalone);
        confReq3.setFSOffset(new FSOffset(new Integer(0xFF), new Long(0xFFFFFFFFL)));
        confRes3 = confReq3.send();
        assertEquals("AccordanceIndication not AccordingToRequest", Enums.AccordanceIndication.AccordingToRequest, confRes3.getAccordanceIndication().getAccordanceIndication());

        setTestInfo("Verify MO:GsmSector attribute abisTfMode is STANDALONE");
        assertTrue("MO " + GSM_SECTOR_LDN + " abisTfMode is not STANDALONE",
                momHelper.waitForMoAttributeStringValue(MomHelper.SECTOR_LDN, "abisTfMode", "STANDALONE", 6));

        setTestStepEnd();

        /*
         * Case 7: TF Mode is SLAVE(2)
         */
        setTestStepBegin("Send TF configuration Request (Tf Mode is SLAVE)");


        OM_G31R01.TFConfigurationRequest confReq4 = abisHelper.initTfConfigRequest();
        confReq4.setTFMode(Enums.TFMode.Slave);
        //if TF mode is slave, FSOffset should not be presented in tfConfigurationRequest, using undefined fsOffset instead
        confReq4.setFSOffset(new FSOffset(new Integer(0xFF), new Long(0xFFFFFFFFL)));
        OM_G31R01.TFConfigurationResult confRes4 = confReq4.send();
        assertEquals("AccordanceIndication not AccordingToRequest", Enums.AccordanceIndication.AccordingToRequest, confRes4.getAccordanceIndication().getAccordanceIndication());

        setTestInfo("Verify MO:GsmSector attribute abisTfMode is SLAVE");
        assertTrue("MO " + GSM_SECTOR_LDN + " abisTfMode is not SLAVE",
                momHelper.waitForMoAttributeStringValue(MomHelper.SECTOR_LDN, "abisTfMode", "SLAVE", 6));

        setTestStepEnd();

        setTestInfo("Change from frequency to time sync to set timeLockingPossible to true");
        boolean isTimeSyncActive = momHelper.isTimeSyncActive();
        if(!isTimeSyncActive){
            // We cannot toggle sync configuration while using WA for CAT WP4689
//            momHelper.deleteRadioEquipmentClockReferenceMo(radioEquipmentClockReferenceLdn);
//            momHelper.deleteFrequencySyncIoMo();
//            momHelper.createTimeSyncIoMo(timeSyncIoLdn);
//            momHelper.createRadioEquipmentClockReferenceMo(radioEquipmentClockReferenceLdn, timeSyncIoLdn);
//            isTimeSyncActive = true;
        }

        if (isTimeSyncActive){
            /*
             * Case 8: TF Mode is MASTER with FSOffset is valid and timeLockingPossible is true
             */
            setTestStepBegin("Send TF configuration Request (Tf Mode is MASTER, FSOffset is valid(0) and timeLockingPossible is true)");
            
            OM_G31R01.TFConfigurationRequest confReq5 = abisHelper.initTfConfigRequest();
            confReq5.setTFMode(Enums.TFMode.Master);
            confReq5.setFSOffset(new FSOffset());
            OM_G31R01.TFConfigurationResult confRes5 = confReq5.send();
            assertEquals("AccordanceIndication not AccordingToRequest", Enums.AccordanceIndication.AccordingToRequest, confRes5.getAccordanceIndication().getAccordanceIndication());
    
            setTestInfo("Verify MO:GsmSector attribute abisTfMode is MASTER");
            assertTrue("MO " + GSM_SECTOR_LDN + " abisTfMode is not MASTER",
                    momHelper.waitForMoAttributeStringValue(MomHelper.SECTOR_LDN, "abisTfMode", "MASTER", 6));
    
            setTestStepEnd();
        }
    }
    
  
    /**
     * @name tfConfigurationRequest
     * 
     * @description Verifies TF Configuration Request with illegal data according to NodeUC512.N
     * 
     * @param testId - unique identifier
     * @param description
     * 
     * @throws InterruptedException
     */
    @Test (timeOut = 360000)
    @Parameters({ "testId", "description" })
    public void tfConfigurationRequestWithIllegalData(String testId, String description) throws InterruptedException {
        
        setTestCase(testId, description);

        /*
         * Case 1: cluster group Id out of range, Data_Not_According_To_Req
         */
        boolean isTimeSyncActive = momHelper.isTimeSyncActive();
        // TODO: Modify this test to work also when we have TimeSync
        if(!isTimeSyncActive){
            setTestStepBegin("Send TF Configuration Request with illegal cluster group Id ");
            // Here we could make a capability exchange request and confirm time locking possible.
            abisHelper.clearStatusUpdate();
    
            OM_G31R01.TFConfigurationRequest confReq = abisHelper.initTfConfigRequest();
            confReq.setClusterGroupId(2049);
            OM_G31R01.TFConfigurationResult confRes = confReq.send();
            
            assertEquals("Status update sent:", null, abisHelper.getStatusUpdate(1, TimeUnit.SECONDS));
            assertEquals("AccordanceIndication in configuration result is not Data_Not_According_To_Req",
              confRes.getAccordanceIndication().getAccordanceIndication(), Enums.AccordanceIndication.NotAccordingToRequest);
    
            /*
             * Case 2: timeLockingPossible false, Capability_Constraint_Violation
             */
            // Here we could make a capability exchange request and confirm time locking possible.
            abisHelper.clearStatusUpdate();
            confReq = abisHelper.initTfConfigRequest();
            confReq.setClusterGroupId(1);
            confReq.setTFMode(Enums.TFMode.Master);
            confReq.setFSOffset(new FSOffset());
    
            confRes = confReq.send();
            
            assertEquals("Status update sent:", null, abisHelper.getStatusUpdate(1, TimeUnit.SECONDS));
            assertEquals("AccordanceIndication in configuration result is not Capability_Constraint_Violation",
       		             confRes.getAccordanceIndication().getAccordanceIndication(), 
       		             Enums.AccordanceIndication.CapabilityConstraintViolation);
            // Attribute Identifier: Configuration Parameter Error
            assertEquals("AttributeIdentifier in configuration result is not Configuration_Parameter_Error",
       		             confRes.getAttributeIdentifier().getAttributeIdentifier(), 0xFFFF);
            
            /*
             * Case 3: TF mode is UNDEFINED, Data_Not_According_To_Req
             */
            setTestStepBegin("Send TF Configuration Request with TF mode UNDEFINED");
            abisHelper.clearStatusUpdate();
            confReq = abisHelper.initTfConfigRequest();
            confReq.setTFMode(Enums.TFMode.NotDefined);
            confRes = confReq.send();
    
            assertEquals("Status update sent:", null, abisHelper.getStatusUpdate(1, TimeUnit.SECONDS));
            assertEquals("AccordanceIndication in configuration result is not Data_Not_According_To_Req",
                    confRes.getAccordanceIndication().getAccordanceIndication(), Enums.AccordanceIndication.NotAccordingToRequest);
            setTestStepEnd();
        }

    }
    
    /**
     * @deprecated - TO BE REMOVED
     * @name tfConfigurationRequestFsOffset
     * 
     * @description Verifies TF Configuration, FS OFFSET defined and RBS is not time synchronized according to NodeUC512.E1.
     * 
     * @param testId Unique identifier
     * @param description 
     * @throws InterruptedException
     */
    @Test (timeOut = 360000)
    @Parameters({ "testId", "description" })
    public void tfConfigurationRequestFsOffset(String testId, String description) throws InterruptedException {
        setTestCase(testId, description);
    
        //Precondition, RBS not time synchronized
        assertTrue("RadioEquipmentClockReference MO is missing", momHelper.checkMoExist(radioClockLdn));
        assertTrue("Frequency Reference is not valid as expected",
                momHelper.waitForMoAttributeStringValue(radioClockLdn, "radioClockState", frequencyRefValid, 5));
        // TODO: if RBS is time synched, remove the time synch for the duration of this test.
        
        setTestStepBegin("Send TF Configuration Request with FS OFFSET defined according to capability reported by the RBS");
        
        // Here we could make a capability exchange request and confirm time locking possible.
        abisHelper.clearStatusUpdate();
        
        setTestInfo("Change from frequency to time sync to set timeLockingPossible to true");
        boolean isTimeSyncActive = momHelper.isTimeSyncActive();
        if(!isTimeSyncActive){
            // We cannot toggle sync configuration while using WA for CAT WP4689
//            momHelper.deleteRadioEquipmentClockReferenceMo(radioEquipmentClockReferenceLdn);
//            momHelper.deleteFrequencySyncIoMo();
//            momHelper.createTimeSyncIoMo(timeSyncIoLdn);
//            momHelper.createRadioEquipmentClockReferenceMo(radioEquipmentClockReferenceLdn, timeSyncIoLdn);
//            isTimeSyncActive = true;
        }

        if (isTimeSyncActive){
            OM_G31R01.TFConfigurationResult configRes = abisHelper.tfConfigRequest(1, Enums.TFMode.Master, new FSOffset(new Integer(0x0), new Long(0x0L))); //Integer.MAX_VALUE + 1
            saveAssertEquals("AccordanceIndication differs from AccordingToRequest", 
                              Enums.AccordanceIndication.AccordingToRequest, configRes.getAccordanceIndication().getAccordanceIndication());
    
            momHelper.deleteRadioEquipmentClockReferenceMo(radioEquipmentClockReferenceLdn);
    
            //TF sends status update with operational condition = Degraded, operational condition reason=No Frame Synch and MO State still DISABLED.
            setTestStepBegin("Receive Status Update");
            G31StatusUpdate statusUpdate = abisHelper.getStatusUpdate(1, TimeUnit.SECONDS);
            
            setTestStepBegin("Verify that Operational Condition is Degraded");
            
            G31OperationalCondition opCond = statusUpdate.getStatusChoice().getG31StatusAO().getG31OperationalCondition();
            assertEquals("Not DEGRADED: ", Enums.OperationalCondition.Degraded.getValue(), opCond.getOperationalCondition().getValue());
            
            setTestStepBegin("Verify that Operational Condition Reasons Map says No Frame Synch (bit number 3)");
            
            G31OperationalConditionReasonsMap opMap = statusUpdate.getStatusChoice().getG31StatusAO().getG31OperationalConditionReasonsMap();
            assertEquals("We have sync...", 1, opMap.getNoFrameSynch());
        }
    }

    /**
     * @name tfConfigurationRequestFsOffset4A1
     * 
     * @description Verifies TF Configuration, result sent immediately according to NodeUC512.A1.
     * 
     * @param testId Unique identifier
     * @param description 
     * @throws InterruptedException
     */
    @Test (timeOut = 360000)
    @Parameters({ "testId", "description" })
    public void tfConfigurationRequestFsOffset4A1(String testId, String description) throws InterruptedException {
    
        setTestCase(testId, description);
        
        setTestInfo("Change from frequency to time sync to set timeLockingPossible to true");
        boolean isTimeSyncActive = momHelper.isTimeSyncActive();
        if(!isTimeSyncActive){
            // We cannot toggle sync configuration while using WA for CAT WP4689
//            momHelper.deleteRadioEquipmentClockReferenceMo(radioEquipmentClockReferenceLdn);
//            momHelper.deleteFrequencySyncIoMo();
//            momHelper.createTimeSyncIoMo(timeSyncIoLdn);
//            momHelper.createRadioEquipmentClockReferenceMo(radioEquipmentClockReferenceLdn, timeSyncIoLdn);
//            isTimeSyncActive = true;
        }

        if (isTimeSyncActive){
            assertTrue("Time Reference is not valid as expected",
                    momHelper.waitForMoAttributeStringValue(radioClockLdn, "radioClockState", timeRefValid, 175)); //wait max 175 seconds for locking to GPS time
            setTestStepEnd();
            
            // Here we could make a capability exchange request and confirm time locking possible.
            abisHelper.clearStatusUpdate();
            
            //since AoTf is enabled in setup, disable it to allow immediate phase adjustment of eATC, based on new FS offset
            boolean status = abisHelper.disableTf();
            assertTrue("AO TF not disabled as expected", status == true);
            assertTrue("MO " + GSM_SECTOR_LDN + " abisTfState is not DISABLED after tf disable",
                    momHelper.waitForMoAttributeStringValue(GSM_SECTOR_LDN, "abisTfState", "DISABLED", 6));
    
            //send first TF configuration request with FS Offset defined
            setTestStepBegin("Configure and Enable TF: Master, FS Offset=0");
            abisHelper.tfConfigRequest(1, Enums.TFMode.Master, new FSOffset());
                    
            Thread.sleep(960); //Sleep 240ms*4 to allow one sync regulation loop finish and apply the new eATC immediately
            abisHelper.enableTf();
            assertTrue("MO " + GSM_SECTOR_LDN + " abisTfState is not ENABLED after tf enable",
                    momHelper.waitForMoAttributeStringValue(GSM_SECTOR_LDN, "abisTfState", "ENABLED", 6));
            setTestStepEnd();
            
            // Reconfigure TF with a new FS Offset
            setTestStepBegin("Re-configure TF with FS Offset=30, ResultImmediately");
            long startTime = System.currentTimeMillis();
            
            OM_G31R01.TFConfigurationResult configRes = abisHelper.tfConfigRequest(1, Enums.TFMode.Master, new FSOffset(new Integer(0x0), new Long(0x30L))); //Integer.MAX_VALUE + 1
            
            long endTime =  System.currentTimeMillis();
            
            long deltaTime = endTime - startTime;
            
            assertTrue("deltaTime not less than 1000", deltaTime < 1000);
            
            saveAssertEquals("AccordanceIndication differs from AccordingToRequest", 
                              Enums.AccordanceIndication.AccordingToRequest, configRes.getAccordanceIndication().getAccordanceIndication());
        }
    }
    /**
     * @name tfConfigurationRequestBigFsOffset
     *
     * @description Verifies TF Configuration, too big time difference according to NodeUC512.A8.
     *
     * @param testId Unique identifier
     * @param description
     * @throws InterruptedException
     */
    @Test (timeOut = 360000)
    @Parameters({ "testId", "description" })
    public void tfConfigurationRequestBigFsOffset(String testId, String description) throws InterruptedException {
        setTestCase(testId, description);
        
        setTestInfo("Change from frequency to time sync to set timeLockingPossible to true");
        boolean isTimeSyncActive = momHelper.isTimeSyncActive();
        if(!isTimeSyncActive){
            // We cannot toggle sync configuration while using WA for CAT WP4689
//            momHelper.deleteRadioEquipmentClockReferenceMo(radioEquipmentClockReferenceLdn);
//            momHelper.deleteFrequencySyncIoMo();
//            momHelper.createTimeSyncIoMo(timeSyncIoLdn);
//            momHelper.createRadioEquipmentClockReferenceMo(radioEquipmentClockReferenceLdn, timeSyncIoLdn);
//            isTimeSyncActive = true;
        }

        if (isTimeSyncActive){
            setTestStepBegin("Wait for timeRefValid");
            assertTrue("Time Reference is not valid as expected",
                    momHelper.waitForMoAttributeStringValue(radioClockLdn, "radioClockState", timeRefValid, 175)); //wait max 175 seconds for locking to GPS time
            setTestStepEnd();
            
            // Here we could make a capability exchange request and confirm time locking possible.
            abisHelper.clearStatusUpdate();
    
            //since AoTf is enabled in setup, disable it to allow immediate phase adjustment of eATC, based on new FS offset
            boolean status = abisHelper.disableTf();
            assertTrue("AO TF not disabled as expected", status == true);
            assertTrue("MO " + GSM_SECTOR_LDN + " abisTfState is not DISABLED after tf disable",
                    momHelper.waitForMoAttributeStringValue(GSM_SECTOR_LDN, "abisTfState", "DISABLED", 6));
    
            //send first TF configuration request with FS Offset defined
            setTestStepBegin("Configure and Enable TF: Master, FS Offset=0");
            abisHelper.tfConfigRequest(1, Enums.TFMode.Master, new FSOffset());
                    
            Thread.sleep(960); //Sleep 240ms*4 to allow one sync regulation loop finish and apply the new eATC immediately
            abisHelper.enableTf();
            assertTrue("MO " + GSM_SECTOR_LDN + " abisTfState is not ENABLED after tf enable",
                    momHelper.waitForMoAttributeStringValue(GSM_SECTOR_LDN, "abisTfState", "ENABLED", 6));
            setTestStepEnd();
            
            //wait 240ms*4 for the calculation done.
            sleepMilliseconds(960);
            //set FSOffset to 0x200L, it will be 49152 amount of eatc. we set it a little bigger than we need to avoid failures caused by that the last adjustment haven't finished yet.
            setTestStepBegin("Send TF configuration request with big FSOffset");
            OM_G31R01.TFConfigurationResult configRes = abisHelper.tfConfigRequest(1, Enums.TFMode.Master, new FSOffset(new Integer(0x0), new Long(0x200L))); //Integer.MAX_VALUE + 1
            saveAssertEquals("AccordanceIndication differs from AccordingToRequest",
                              Enums.AccordanceIndication.AccordingToRequest, configRes.getAccordanceIndication().getAccordanceIndication());
            
            //TF sends status update with operational condition = NotOperational
            setTestStepBegin("TF Not Operational, Phase Jump Needed");
            G31StatusUpdate statusUpdateNotOperational = abisHelper.getStatusUpdate(2, TimeUnit.SECONDS);
    
            G31OperationalCondition opCond3 = statusUpdateNotOperational.getStatusChoice().getG31StatusAO().getG31OperationalCondition();
            assertEquals("Not NOT_OPERATIONAL: ", Enums.OperationalCondition.NotOperational.getValue(), opCond3.getOperationalCondition().getValue());
    
            String opText = statusUpdateNotOperational.getStatusChoice().getG31StatusAO().getOperationalConditionText();
            assertEquals("We have sync...", "Phase Jump Needed", opText);
    
            //TF sends status update with operational condition = Operational and MO State = Reset
            setTestStepBegin("TF perform internal Reset and becomes Operational");
            G31StatusUpdate statusUpdateOperational = abisHelper.getStatusUpdate(10, TimeUnit.SECONDS);
    
            assertEquals("Not RESET: ", Enums.MOState.RESET.getValue(), statusUpdateOperational.getMOState().getValue());
    
            G31OperationalCondition opCond4 = statusUpdateOperational.getStatusChoice().getG31StatusAO().getG31OperationalCondition();
            assertEquals("Not OPERATIONAL: ", Enums.OperationalCondition.Operational.getValue(), opCond4.getOperationalCondition().getValue());
    
            assertTrue("MO " + GSM_SECTOR_LDN + " abisTfState is not RESET",
                    momHelper.waitForMoAttributeStringValue(GSM_SECTOR_LDN, "abisTfState", "RESET", 6));
        }
    }

    /**
     * @name tfConfigurationRequestSmallOffset
     *
     * @description Verifies TF Configuration, Standalone mode, a small FS OFFSET defined according to NodeUC512.A2, result sent after FS Offset adjustment is applied.
     *
     * @param testId Unique identifier
     * @param description
     * @throws InterruptedException
     */
    @Test (timeOut = 360000)
    @Parameters({ "testId", "description" })
    public void tfConfigurationRequestSmallOffset(String testId, String description) throws InterruptedException {
        setTestCase(testId, description);

        setTestInfo("Change from frequency to time sync to set timeLockingPossible to true");
        boolean isTimeSyncActive = momHelper.isTimeSyncActive();
        if(!isTimeSyncActive){
            // We cannot toggle sync configuration while using WA for CAT WP4689
//            momHelper.deleteRadioEquipmentClockReferenceMo(radioEquipmentClockReferenceLdn);
//            momHelper.deleteFrequencySyncIoMo();
//            momHelper.createTimeSyncIoMo(timeSyncIoLdn);
//            momHelper.createRadioEquipmentClockReferenceMo(radioEquipmentClockReferenceLdn, timeSyncIoLdn);
//            isTimeSyncActive = true;
        }

        if (isTimeSyncActive){
            setTestStepBegin("Add Time Reference and wait for timeRefValid");
            assertTrue("Time Reference is not valid as expected",
                    momHelper.waitForMoAttributeStringValue(radioClockLdn, "radioClockState", timeRefValid, 175)); //wait max 175 seconds for locking to GPS time
            setTestStepEnd();
            
            // Here we could make a capability exchange request and confirm time locking possible.
            abisHelper.clearStatusUpdate();
    
            //since AoTf is enabled in setup, disable it to allow immediate phase adjustment of eATC, based on new FS offset
            boolean status = abisHelper.disableTf();
            assertTrue("AO TF not disabled as expected", status == true);
            assertTrue("MO " + GSM_SECTOR_LDN + " abisTfState is not DISABLED after tf disable",
                    momHelper.waitForMoAttributeStringValue(GSM_SECTOR_LDN, "abisTfState", "DISABLED", 6));
    
            //send first TF configuration request with cluster group id 1 and offset 19
            setTestStepBegin("Configure and Enable TF: Standalone, clusterGroupId=1, FS Offset=19");
            Integer FSOffsetHigh8 = 0;
            long FSOffsetLow32 = 19;
            OM_G31R01.FSOffset fsOffset = new OM_G31R01.FSOffset(FSOffsetHigh8, FSOffsetLow32);
            OM_G31R01.TFConfigurationResult confRes = abisHelper.tfConfigRequest(1, Enums.TFMode.Standalone, fsOffset);
            assertEquals("AccordanceIndication don't match Request", Enums.AccordanceIndication.AccordingToRequest,
                    confRes.getAccordanceIndication().getAccordanceIndication());
    
            int oldConfSignature = confRes.getConfigurationSignature();
            
            Thread.sleep(240); //Sleep 240ms to allow one sync regulation loop finish and apply the new eATC immediately
            abisHelper.enableTf();
            assertTrue("MO " + GSM_SECTOR_LDN + " abisTfState is not ENABLED after tf enable",
                    momHelper.waitForMoAttributeStringValue(GSM_SECTOR_LDN, "abisTfState", "ENABLED", 6));
            setTestStepEnd();
            
            //send 2nd tf configuration request with offset 20 and tfresultcontrol 1
            setTestStepBegin("Re-Configure TF with FS Offset = 20, ResultOnChange");
            OM_G31R01.TFConfigurationRequest confReq = abisHelper.initTfConfigRequest();
            FSOffsetHigh8 = 0;
            FSOffsetLow32 = 20;
            fsOffset.setFSOffsetHigh8(FSOffsetHigh8);
            fsOffset.setFSOffsetLow32(FSOffsetLow32);
            confReq.setFSOffset(fsOffset);
            OM_G31R01.Enums.TFResultControl tfResultControlParam = OM_G31R01.Enums.TFResultControl.ResultOnChange;
            confReq.setTFResultControl(tfResultControlParam);
            confReq.setTFMode(Enums.TFMode.Standalone);
            confReq.setClusterGroupId(1);//since cluster group id is using default 0 in initTfConfigRequest, so set to 1 consistent with the former TF configure
    
            logger.debug("jcat:small offset set:  FSOffsetHigh8:"
    				+ confReq.getFSOffset().getFSOffsetHigh8() + "FSOffsetLow32:" + confReq.getFSOffset().getFSOffsetLow32() + "resultcontrol:" + tfResultControlParam);
            
            long expectedTime = 96 * 240 / 8; //in miliseond
            long timeout = (long)(1.5 * expectedTime); // give a 50% error margin. 
            //@FIXME: a better way is to repeat the test several times and compute the average and standard deviation from expectedTime (less than 10%).
            logger.debug("jcat:timeout:"
    				+ timeout + "expectedTime:" + expectedTime);
    
            long aa = System.currentTimeMillis();
            confReq.sendAsync();
    
            confRes = abisHelper.waitForTfConfigResult(timeout);
            long bb = System.currentTimeMillis();
    
            logger.debug("func return,confRes:"
    				+ confRes);
            assertTrue("confRes is Null", confRes != null);
    
            long diff = bb - aa;
            logger.debug("jcat:timeout:"
    				+ timeout + "bb:" + bb + "aa: " + aa + "bb-aa:" + diff);
            assertTrue("diff (" + diff + ") is not less than (" + timeout + ") as expected", diff < timeout);
            assertTrue("diff (" + diff + ") is not greater or equal to (" + expectedTime + ") as expected", diff >= expectedTime);
    
            int newConfSignature = confRes.getConfigurationSignature();
            setTestDebug("old confSignature: " + oldConfSignature + " new confSignature: " + newConfSignature);
            assertNotSame("old signature same as new", oldConfSignature, newConfSignature);
            assertEquals("AccordanceIndication don't match Request", Enums.AccordanceIndication.AccordingToRequest,
                    confRes.getAccordanceIndication().getAccordanceIndication()); 
            setTestStepEnd();
        }
    }

    
    /**
     * @name tfConfigurationRequestSmallOffsetForMaster
     *
     * @description Verifies TF Configuration when TF mode is master, a small FS OFFSET defined according to NodeUC512.A2, result sent after FS Offset adjustment is applied.
     *              It will take a longer time to calculate eATC/ecBFN relation than standalone
     *
     * @param testId Unique identifier
     * @param description
     * @throws InterruptedException
     */
    @Test (timeOut = 360000)
    @Parameters({ "testId", "description" })
    public void tfConfigurationRequestSmallOffsetForMaster(String testId, String description) throws InterruptedException {
        setTestCase(testId, description);

        setTestInfo("Change from frequency to time sync to set timeLockingPossible to true");
        boolean isTimeSyncActive = momHelper.isTimeSyncActive();
        if(!isTimeSyncActive){
            // We cannot toggle sync configuration while using WA for CAT WP4689
//            momHelper.deleteRadioEquipmentClockReferenceMo(radioEquipmentClockReferenceLdn);
//            momHelper.deleteFrequencySyncIoMo();
//            momHelper.createTimeSyncIoMo(timeSyncIoLdn);
//            momHelper.createRadioEquipmentClockReferenceMo(radioEquipmentClockReferenceLdn, timeSyncIoLdn);
//            isTimeSyncActive = true;
        }

        if (isTimeSyncActive){
            setTestStepBegin("Add Time Reference and wait for timeRefValid");
            assertTrue("Time Reference is not valid as expected",
                    momHelper.waitForMoAttributeStringValue(radioClockLdn, "radioClockState", timeRefValid, 175)); //wait max 175 seconds for locking to GPS time
            setTestStepEnd();
            
            // Here we could make a capability exchange request and confirm time locking possible.
            abisHelper.clearStatusUpdate();
    
            //since AoTf is enabled in setup, disable it to allow immediate phase adjustment of eATC, based on new FS offset
            boolean status = abisHelper.disableTf();
            assertTrue("AO TF not disabled as expected", status == true);
            assertTrue("MO " + GSM_SECTOR_LDN + " abisTfState is not DISABLED after tf disable",
                    momHelper.waitForMoAttributeStringValue(GSM_SECTOR_LDN, "abisTfState", "DISABLED", 6));
    
            //send first TF configuration request with cluster group id 1 and offset 19
            setTestStepBegin("Configure and Enable TF: Master, clusterGroupId=1, FS Offset=19");
            Integer FSOffsetHigh8 = 0;
            long FSOffsetLow32 = 19;
            OM_G31R01.FSOffset fsOffset = new OM_G31R01.FSOffset(FSOffsetHigh8, FSOffsetLow32);
            OM_G31R01.TFConfigurationResult confRes = abisHelper.tfConfigRequest(1, Enums.TFMode.Master, fsOffset);
            assertEquals("AccordanceIndication don't match Request", Enums.AccordanceIndication.AccordingToRequest,
                    confRes.getAccordanceIndication().getAccordanceIndication());
    
            int oldConfSignature = confRes.getConfigurationSignature();
            
            Thread.sleep(1920); //Sleep 960ms * 2 to allow one sync regulation loop finish and apply the new eATC immediately because it is not sure that the current count of regulation loop is 
            abisHelper.enableTf();
            assertTrue("MO " + GSM_SECTOR_LDN + " abisTfState is not ENABLED after tf enable",
                    momHelper.waitForMoAttributeStringValue(GSM_SECTOR_LDN, "abisTfState", "ENABLED", 6));
            setTestStepEnd();
    
            //send 2nd tf configuration request with offset 20 and tfresultcontrol 1
            setTestStepBegin("Re-Configure TF with FS Offset = 20, ResultOnChange");
            OM_G31R01.TFConfigurationRequest confReq = abisHelper.initTfConfigRequest();
            FSOffsetLow32 = 20;
            fsOffset.setFSOffsetHigh8(FSOffsetHigh8);
            fsOffset.setFSOffsetLow32(FSOffsetLow32);
            confReq.setFSOffset(fsOffset);
            OM_G31R01.Enums.TFResultControl tfResultControlParam = OM_G31R01.Enums.TFResultControl.ResultOnChange;
            confReq.setTFResultControl(tfResultControlParam);
            confReq.setTFMode(Enums.TFMode.Master);
            confReq.setClusterGroupId(1);//since cluster group id is using default 0 in initTfConfigRequest, so set to 1 consistent with the former TF configure
    
            // Set the SP timeout TF_CONFIG_TIMEOUT to 120 seconds according to Abis IWD
            Behaviour myBehaviour = new Behaviour();
            myBehaviour.setTIMER2(120000);
            confReq.setBehaviour(myBehaviour);
            
            logger.debug("jcat:small offset set:  FSOffsetHigh8:"
    				+ confReq.getFSOffset().getFSOffsetHigh8() + "FSOffsetLow32:" + confReq.getFSOffset().getFSOffsetLow32() + "resultcontrol:" + tfResultControlParam);
            
            long expectedTime = 96 * 960 / 8; //in miliseond
            long timeout = (long)(1.5 * expectedTime); // give a 50% error margin. 
            //@FIXME: a better way is to repeat the test several times and compute the average and standard deviation from expectedTime (less than 10%).
            logger.debug("jcat:timeout:"
    				+ timeout + "expectedTime:" + expectedTime);
    
            long aa = System.currentTimeMillis();
            confReq.sendAsync();
    
            confRes = abisHelper.waitForTfConfigResult(timeout);
            long bb = System.currentTimeMillis();
    
            logger.debug("func return,confRes:"
    				+ confRes);
            assertTrue("Result is null", confRes != null);
            
            long diff = bb - aa;
            logger.debug("jcat:timeout:"
    				+ timeout + "bb:" + bb + "aa: " + aa + "bb-aa:" + diff);
            assertTrue("diff (" + diff + ") is not less than timeout (" + timeout + ")", diff < timeout);
            assertTrue("diff (" + diff + ") is not greater or equal to the expectedTime (" + expectedTime + ")", diff >= expectedTime);
    
            int newConfSignature = confRes.getConfigurationSignature();
            setTestDebug("old confSignature: " + oldConfSignature + " new confSignature: " + newConfSignature);
            assertNotSame("old signature same as new", oldConfSignature, newConfSignature);
            assertEquals("AccordanceIndication don't match Request", Enums.AccordanceIndication.AccordingToRequest,
                    confRes.getAccordanceIndication().getAccordanceIndication()); 
            setTestStepEnd();
        }
    }

    /**
     * @name txConfigurationRequest
     * 
     * @description Verifies TX Configuration Request according to NodeUC601.N
     * 
     * @param testId - unique identifier
     * @param description
     * 
     * @throws InterruptedException
     */
    @Test (timeOut = 360000)
    @Parameters({ "testId", "description" })
    public void txConfigurationRequest(String testId, String description) throws InterruptedException {
        
        setTestCase(testId, description);
        
        setTestStepBegin("Verify AO TX is in disabled state");
        assertTrue("AO TX is not DISABLED",
                momHelper.waitForAbisTxMoState("DISABLED"));
        setTestStepEnd();
        
        setTestStepBegin("Send TX Configuration Request ");
        
        OM_G31R01.TXConfigurationResult confRes = abisHelper.txConfigRequest(0, momHelper.getArfcnToUse(), false, POWER_LEVEL, false);
        int configurationSignature = confRes.getConfigurationSignature();        
        setTestStepEnd();
              
        assertEquals("AccordanceIndication differs from Request", 
                Enums.AccordanceIndication.AccordingToRequest, confRes.getAccordanceIndication().getAccordanceIndication());
        
        setTestStepBegin("Send same TX Config again and compare configuration signatures");
        confRes = abisHelper.txConfigRequest(0, momHelper.getArfcnToUse(), false, POWER_LEVEL, false);
        assertEquals("Configuration signature has changed", configurationSignature, confRes.getConfigurationSignature());
        setTestStepEnd();
    }
    
    /**
     * @name txConfigurationRequest in state enabled valid config
     * 
     * @description Verifies TX Configuration Request according to NodeUC601.A1
     * 
     * @param testId - unique identifier
     * @param description
     * 
     * @throws InterruptedException
     */
    @Test (timeOut = 360000)
    @Parameters({ "testId", "description" })
    public void txConfigurationRequestInStateEnabledValid(String testId, String description) throws InterruptedException {
        
        setTestCase(testId, description);
        
        setTestStepBegin("Verify AO TX is in disabled state");
        assertTrue("AO TX is not DISABLED", momHelper.waitForAbisTxMoState("DISABLED"));
        setTestStepEnd();
        
        setTestStepBegin("Send first TX Configuration Request ");
        OM_G31R01.TXConfigurationResult confRes = abisHelper.txConfigRequest(0, momHelper.getArfcnToUse(), false, POWER_LEVEL, false);
        int configurationSignature = confRes.getConfigurationSignature();
        setTestStepEnd();
        
        setTestStepBegin("Enable AO TX");
        abisHelper.enableRequest(moClassTx, 0);
        assertTrue("AO TX is not ENABLED", momHelper.waitForAbisTxMoState("ENABLED"));
        setTestStepEnd();
        
        setTestStepBegin("Send TX Configuration Request in state Enabled, change power parameter");
        confRes = abisHelper.txConfigRequest(0, momHelper.getArfcnToUse(), false, POWER_LEVEL, false);
        assertNotSame("Configuration signature should change", configurationSignature, confRes.getConfigurationSignature());
        assertEquals("AccordanceIndication differs from Request", 
                Enums.AccordanceIndication.AccordingToRequest, confRes.getAccordanceIndication().getAccordanceIndication());
        setTestStepEnd();
    }
    
    /**
     * @name txConfigurationRequest in state enabled invalid config
     * 
     * @description Verifies TX Configuration Request according to NodeUC601.E2
     * 
     * @param testId - unique identifier
     * @param description
     * 
     * @throws InterruptedException
     */
    @Test (timeOut = 360000)
    @Parameters({ "testId", "description" })
    public void txConfigurationRequestInStateEnabledInvalid(String testId, String description) throws InterruptedException {
        
        setTestCase(testId, description);
        
        setTestStepBegin("Verify AO TX is in disabled state");
        assertTrue("AO TX is not DISABLED", momHelper.waitForAbisTxMoState("DISABLED"));
        setTestStepEnd();
        
        setTestStepBegin("Send first TX Configuration Request ");      
        OM_G31R01.TXConfigurationResult confRes = abisHelper.txConfigRequest(0, momHelper.getArfcnToUse(), false, POWER_LEVEL, false);
        setTestStepEnd();
        
        setTestStepBegin("Enable AO TX");
        abisHelper.enableRequest(moClassTx, 0);
        assertTrue("AO TX is not ENABLED", momHelper.waitForAbisTxMoState("ENABLED"));
        setTestStepEnd();
        
        setTestStepBegin("Send TX Configuration Request in state Enabled, change arfcn parameter (not allowed)");
        confRes = abisHelper.txConfigRequest(0, momHelper.getArfcnToUse() + 1, false, POWER_LEVEL, false);
        assertEquals("AccordanceIndication match Request", 
                Enums.AccordanceIndication.NotAccordingToRequest, confRes.getAccordanceIndication().getAccordanceIndication());
        setTestStepEnd();
    }

    /**
     * @name twoTxConfigReqWithSameTxAddress
     * 
     * @description Verifies that it is not possible to configure a TX with the same TX address as another enabled TX.
     * 
     * @param testId - unique identifier
     * @param description
     * 
     * @throws InterruptedException
     */
    @Test (timeOut = 360000)
    @Parameters({ "testId", "description" })
    public void twoTxConfigReqWithSameTxAddress(String testId, String description) throws InterruptedException {
    	// Declare TC constants
        final int TG0 						= 0;
        final int TRX0 						= 0;
        final int TRX1 						= 1;
        final int TXADDRESS 				= 0;
        final int FREQUENCY_SPECIFIER_TX 	= 0x3A;
        final int PARAMETER_ERROR 			= 0x08;
        final int ASSOCIATED_SO_INSTANCE 	= 0xFF;
        
        // Start TC
        setTestCase(testId, description);
        
        // Create one additional Trx in same TG
        setTestStepBegin("Create and setup one additional Trx");
        momHelper.createTrxMo(MomHelper.SECTOR_LDN, "1");
        momHelper.unlockMo(MomHelper.SECTOR_LDN+",Trx=1");
        
    	setTestStepBegin("Verify Trx MO states for " + MomHelper.SECTOR_LDN+",Trx=1");
        assertEquals(MomHelper.SECTOR_LDN+",Trx=1" + " did not reach correct state", "", 
                momHelper.checkTrxMoAttributeAfterUnlockNoLinks(MomHelper.SECTOR_LDN+",Trx=1", 20));
        
        // Establish links to second Trx
        abiscoConnection.establishLinks(TG0, true, TRX1);
        
        // Start all Ao:s
        assertTrue("Could not start AO:s as expected!", abisHelper.startTrxMos(TRX1));
        
        // Establish links to first Trx (again)
        abiscoConnection.establishLinks(TG0, true, TRX0);
        setTestStepEnd();

        // Check second AoTx
        setTestStepBegin("Verify that second AoTx is in disabled state");
        assertTrue("AoTx is not DISABLED", momHelper.waitForAbisTxMoState(TRX1, "DISABLED"));
        setTestStepEnd();

        // Configure second AoTx with 'No frequency' mark
        setTestStepBegin("Send Configuration Request to second AoTx with 'No frequency' mark");
        OM_G31R01.TXConfigurationResult confRes = abisHelper.txConfigRequest(TG0, TRX1, TXADDRESS, 1023, true, POWER_LEVEL, false);
        setTestStepEnd();
        
        // Configure and enable first TX
        setTestStepBegin("Verify that first AoTx is in disabled state");
        assertTrue("AoTx is not DISABLED", momHelper.waitForAbisTxMoState("DISABLED"));
        setTestStepEnd();
        
        // Configuration Request shall succeed because the second AoTx is configured with 'No frequency' mark
        setTestStepBegin("Send Configuration Request to first AoTx");
        confRes = abisHelper.txConfigRequest(TG0, TRX0, TXADDRESS, momHelper.getArfcnToUse(), false, POWER_LEVEL, false);
        setTestStepEnd();
        
        setTestStepBegin("Enable first AoTx");
        abisHelper.enableRequest(moClassTx, TRX0);
        assertTrue("AoTx is not ENABLED", momHelper.waitForAbisTxMoState("ENABLED"));
        setTestStepEnd();
        
        // Configure second TX. Use same TX address as the first AoTx. Expect it to fail
        setTestStepBegin("Send AoTx Configuration Request in state Disable with a txAddress that is already in use");
        boolean txConfigRejectReceived = false;
        try {
        	confRes = abisHelper.txConfigRequest(TG0, TRX1, TXADDRESS, momHelper.getArfcnToUse(), false, POWER_LEVEL, false);
        }catch (TXConfigurationRequestRejectException e) {
        	// Catched expected exception, check Reason code = 0x3A & Result code = 0x08
        	assertEquals("The Reason Code (" + e.getTXConfigurationRequestReject().getReasonCode() + ") is not (" + FREQUENCY_SPECIFIER_TX + ") as expected!", 
        			FREQUENCY_SPECIFIER_TX, e.getTXConfigurationRequestReject().getReasonCode());
        	assertEquals("The Result Code (" + e.getTXConfigurationRequestReject().getResultCode().getValue() + ") is not (" + PARAMETER_ERROR + ") as expected!", 
        			PARAMETER_ERROR, e.getTXConfigurationRequestReject().getResultCode().getValue());
        	
        	txConfigRejectReceived = true;
        }
        
        assertTrue("Did not receive TxConfigReject as expected", txConfigRejectReceived);
        setTestStepEnd();
        
        // Reset first AoTx (free txAddress)
        setTestStepBegin("Reset first AoTx");
        abisHelper.resetCommand(this.moClassTx);
        saveAssertTrue("abisTxMoState is not RESET", momHelper.waitForAbisTxMoState(TRX0, "RESET"));
        setTestStepEnd();
        
        // Configure second AoTx again with same txAddress as before and verify that it works
        setTestStepBegin("Send TX Configuration Request in state Disable with a TX address that is already in use");
        confRes = abisHelper.txConfigRequest(TG0, TRX1, TXADDRESS, momHelper.getArfcnToUse(), false, POWER_LEVEL, false);
        assertEquals("AccordanceIndication match Request", Enums.AccordanceIndication.AccordingToRequest, confRes.getAccordanceIndication().getAccordanceIndication());
        setTestStepEnd();
        
        // Configure second AoTx with another TxAddress (free first txAddress)
        setTestStepBegin("Send TX Configuration Request again to same AoTx with a new TX address");
        confRes = abisHelper.txConfigRequest(TG0, TRX1, (TXADDRESS+1), momHelper.getArfcnToUse(), false, POWER_LEVEL, false);
        assertEquals("AccordanceIndication match Request", Enums.AccordanceIndication.AccordingToRequest, confRes.getAccordanceIndication().getAccordanceIndication());
        setTestStepEnd();
        
        // Check second AoTx
        setTestStepBegin("Verify that second AoTx is in disabled state");
        assertTrue("AoTx is not DISABLED", momHelper.waitForAbisTxMoState(TRX1, "DISABLED"));
        setTestStepEnd();
        
        // Enable second AoTx
        setTestStepBegin("Enable second AoTx");
        abisHelper.enableRequest(moClassTx, TG0, TRX1, TRX1, ASSOCIATED_SO_INSTANCE);
        assertTrue("AoTx is not ENABLED", momHelper.waitForAbisTxMoState(TRX1, "ENABLED"));
        setTestStepEnd();
        
        // Start first AoTx
        setTestStepBegin("Start first AoTx");
        abisHelper.startRequest(OM_G31R01.Enums.MOClass.TX, TG0, TRX0, TRX0, ASSOCIATED_SO_INSTANCE);
        setTestStepEnd();
        
        // Check first AoTx
        setTestStepBegin("Verify that first AoTx is in disabled state");
        assertTrue("AoTx is not DISABLED", momHelper.waitForAbisTxMoState(TRX0, "DISABLED"));
        setTestStepEnd();
        
        // Configure first AoTx with the first TxAddress again (now free to use).
        setTestStepBegin("Send Configuration Request to first AoTx");
        confRes = abisHelper.txConfigRequest(TG0, TRX0, TXADDRESS, momHelper.getArfcnToUse(), false, POWER_LEVEL, false);
        setTestStepEnd();
        
        // Enable first AoTx
        setTestStepBegin("Enable first AoTx");
        abisHelper.enableRequest(moClassTx, TRX0);
        assertTrue("AoTx is not ENABLED", momHelper.waitForAbisTxMoState("ENABLED"));
        setTestStepEnd();
    }
    
    /**
     * @name rxConfigurationRequest
     * 
     * @description Verifies RX Configuration Request according to NodeUC606.N
     * 
     * @param testId - unique identifier
     * @param description
     * 
     * @throws InterruptedException
     */
    @Test (timeOut = 360000)
    @Parameters({ "testId", "description" })
    public void rxConfigurationRequest(String testId, String description) throws InterruptedException {
        
        setTestCase(testId, description);
        
        setTestStepBegin("Verify AO RX is in disabled state");
        assertTrue("AO RX is not DISABLED", momHelper.waitForAbisRxMoState("DISABLED"));
        setTestStepEnd();
    	
        setTestStepBegin("Send RX Configuration Request ");
 
        OM_G31R01.RXConfigurationResult confRes = abisHelper.rxConfigRequest(0, momHelper.getArfcnToUse(), false, "");
        int configurationSignature = confRes.getConfigurationSignature();
        assertEquals("AccordanceIndication don't match Request", 
        		Enums.AccordanceIndication.AccordingToRequest, confRes.getAccordanceIndication().getAccordanceIndication());

    	setTestStepEnd();
    	
    	setTestStepBegin("Send RX Config again, assert same configuration signature");
    	confRes = abisHelper.rxConfigRequest(0, momHelper.getArfcnToUse(), false, "");
    	assertEquals("Configuration signature should be the same", configurationSignature,
    	        confRes.getConfigurationSignature());
    	setTestStepEnd();
    }
    
    /**
     * @name rxConfigurationRequest in state enabled valid config
     * 
     * @description Verifies RX Configuration Request according to NodeUC606.A1
     * 
     * @param testId - unique identifier
     * @param description
     * 
     * @throws InterruptedException
     */
    @Test (timeOut = 360000)
    @Parameters({ "testId", "description" })
    public void rxConfigurationRequestInStateEnabledValid(String testId, String description) throws InterruptedException {
        
        setTestCase(testId, description);
        
        setTestStepBegin("Verify AO RX is in disabled state");
        assertTrue("AO RX is not DISABLED", momHelper.waitForAbisRxMoState("DISABLED"));
        setTestStepEnd();
        
        setTestStepBegin("Send first RX Configuration Request ");
        OM_G31R01.RXConfigurationResult confRes = abisHelper.rxConfigRequest(0, momHelper.getArfcnToUse(), false, "");
        int configurationSignature = confRes.getConfigurationSignature();
        setTestStepEnd();
        
        setTestStepBegin("Enable AO RX");
        abisHelper.enableRequest(moClassRx, 0);
        saveAssertTrue("AO RX is not ENABLED", momHelper.waitForAbisRxMoState("ENABLED"));
        setTestStepEnd();
        
        setTestStepBegin("Send RX Configuration Request in state Enabled, change cell name");
        confRes = abisHelper.rxConfigRequest(0, momHelper.getArfcnToUse(), false, "cell1");
        assertNotSame("Configuration signature should change", configurationSignature, confRes.getConfigurationSignature());
        saveAssertEquals("AccordanceIndication don't match Reaquest", 
        		Enums.AccordanceIndication.AccordingToRequest, confRes.getAccordanceIndication().getAccordanceIndication());
        setTestStepEnd();
    }
    
    /**
     * @name rxConfigurationRequest in state enabled invalid config
     * 
     * @description Verifies RX Configuration Request according to NodeUC606.E2
     * 
     * @param testId - unique identifier
     * @param description
     * 
     * @throws InterruptedException
     */
    @Test (timeOut = 360000)
    @Parameters({ "testId", "description" })
    public void rxConfigurationRequestInStateEnabledInvalid(String testId, String description) throws InterruptedException {
        
        setTestCase(testId, description);
        
        setTestStepBegin("Verify AO RX is in disabled state");
        assertTrue("AO RX is not DISABLED", momHelper.waitForAbisRxMoState("DISABLED"));
        setTestStepEnd();
        
        setTestStepBegin("Send first RX Configuration Request ");        
        abisHelper.rxConfigRequest(0, momHelper.getArfcnToUse(), false, "");
        setTestStepEnd();
        
        setTestStepBegin("Enable AO RX");
        abisHelper.enableRequest(moClassRx, 0);
        assertTrue("AO RX is not ENABLED", momHelper.waitForAbisRxMoState("ENABLED"));
        setTestStepEnd();
        
        setTestStepBegin("Send RX Configuration Request in state Enabled, change arfcn parameter (not allowed)");
        OM_G31R01.RXConfigurationResult confRes = abisHelper.rxConfigRequest(0, momHelper.getArfcnToUse() +1, false, "");
        saveAssertEquals("AccordanceIndication don't match Reaquest", 
        		Enums.AccordanceIndication.NotAccordingToRequest, confRes.getAccordanceIndication().getAccordanceIndication());
        setTestStepEnd();
    }
    
    
    /**
     * @name tsConfigurationRequest_MainBCCHCombined
     * 
     * @description Verifies TS Configuration Request according to NodeUC626.N
     * 
     * @param testId - unique identifier
     * @param description
     * 
     * @throws Exception 
     * 
     */
    @Test (timeOut = 60000)
    @Parameters({ "testId", "description" })
    public void tsConfigurationRequest_MainBCCHCombined(String testId, String description) throws Exception {

    	// choose tsInstance (0-7) randomly   	
    	int seed = (int)System.currentTimeMillis();  
    	seed = (seed < 0) ? ((-1) * seed) : seed;
    	seed = (seed > 2000) ? (seed % 2000) : seed;
        int tsInstance = seed % 8;
    	
        setTestCase(testId, description);
        
        /*
         * Precondition: TS is in state DISABLED
         */
        setTestInfo("Precondition: Verify AO TS is in disabled state, instance: " + tsInstance);
        assertTrue("AO TS is not DISABLED", momHelper.waitForAbisTsMoState(tsInstance, "DISABLED", 5));
     
        /*
         * Case 1: Configure a TS with combination: MainBCCHCombined
         */
        setTestStepBegin("Send TS Configuration Request, combination: MainBCCHCombined, instance: " + tsInstance);
        setTestDebug("Random seed: " + seed);
        OM_G31R01.TSConfigurationResult confRes = abisHelper.tsConfigRequest(tsInstance, seed, seed, false, OM_G31R01.Enums.Combination.MainBCCHCombined);
        int confSignature = confRes.getConfigurationSignature();
        saveAssertEquals("AccordanceIndication don't match Reaquest", Enums.AccordanceIndication.AccordingToRequest, 
        		confRes.getAccordanceIndication().getAccordanceIndication());
        assertTrue("abisTsMoState for tsInstance " + tsInstance + " is not DISABLED", momHelper.waitForAbisTsMoState(tsInstance, "DISABLED", 5));
        setTestStepEnd();
        
        /*
         * Case 2: Change all mandatory parameters
         */
        setTestStepBegin("Send TS Configuration Request, combination: MainBCCHCombined, all mandatory parameters changed, instance: " + tsInstance); 
        confRes = abisHelper.tsConfigRequest(tsInstance, seed+1, seed+1, false, OM_G31R01.Enums.Combination.MainBCCHCombined);
        int oldConfSignature = confSignature;
        confSignature = confRes.getConfigurationSignature();
        assertFalse("Configuration signature should change", (confSignature == oldConfSignature));
        saveAssertEquals("AccordanceIndication don't match Reaquest", Enums.AccordanceIndication.AccordingToRequest, 
        		confRes.getAccordanceIndication().getAccordanceIndication());
        assertTrue("abisTsMoState for tsInstance " + tsInstance + " is not DISABLED", momHelper.waitForAbisTsMoState(tsInstance, "DISABLED", 5));
        setTestStepEnd();
        
        /*
         * Case 3: Change all optional parameters
         */
        setTestStepBegin("Send TS Configuration Request, combination: MainBCCHCombined, all optional parameters changed, instance: " + tsInstance);
        confRes = abisHelper.tsConfigRequest(tsInstance, seed+1, 1, true, OM_G31R01.Enums.Combination.MainBCCHCombined);
        oldConfSignature = confSignature;
        confSignature = confRes.getConfigurationSignature();
        assertFalse("Configuration signature should change", (confSignature == oldConfSignature));
        saveAssertEquals("AccordanceIndication don't match Reaquest", Enums.AccordanceIndication.AccordingToRequest, 
        		confRes.getAccordanceIndication().getAccordanceIndication());    
        assertTrue("abisTsMoState for tsInstance " + tsInstance + " is not DISABLED", momHelper.waitForAbisTsMoState(tsInstance, "DISABLED", 5));
        setTestStepEnd();
    }
    
    /**
     * @name tsConfigurationRequest_MainBCCH
     * 
     * @description Verifies TS Configuration Request according to NodeUC626.N
     * 
     * @param testId - unique identifier
     * @param description
     * 
     * @throws Exception 
     * 
     */
    @Test (timeOut = 60000)
    @Parameters({ "testId", "description" })
    public void tsConfigurationRequest_MainBCCH(String testId, String description) throws Exception {

    	// choose tsInstance (0-7) randomly   	
    	int seed = (int)System.currentTimeMillis();  
    	seed = (seed < 0) ? ((-1) * seed) : seed;
    	seed = (seed > 2000) ? (seed % 2000) : seed;
        int tsInstance = seed % 8;

        setTestCase(testId, description);
        
        /*
         * Precondition: TS is in state DISABLED
         */
        setTestInfo("Precondition: Verify AO TS is in disabled state, instance: " + tsInstance);
        assertTrue("AO TS is not DISABLED", momHelper.waitForAbisTsMoState(tsInstance, "DISABLED", 5));
     
        /*
         * Case 1: Configure a TS with combination: MainBCCH 
         */
        setTestStepBegin("Send TS Configuration Request, combination: MainBCCH, instance: " + tsInstance);
        setTestDebug("Random seed: " + seed);
        OM_G31R01.TSConfigurationResult confRes = abisHelper.tsConfigRequest(tsInstance, seed, seed, false, OM_G31R01.Enums.Combination.MainBCCH);
        int confSignature = confRes.getConfigurationSignature();
        assertEquals("AccordanceIndication don't match Reaquest", Enums.AccordanceIndication.AccordingToRequest, 
        		confRes.getAccordanceIndication().getAccordanceIndication());
        assertTrue("abisTsMoState for tsInstance " + tsInstance + " is not DISABLED", momHelper.waitForAbisTsMoState(tsInstance, "DISABLED", 5));
        setTestStepEnd();
        
        /*
         * Case 2: Change all mandatory parameters
         */
        setTestStepBegin("Send TS Configuration Request, combination: MainBCCH, all mandatory parameters changed, instance: " + tsInstance); 
        confRes = abisHelper.tsConfigRequest(tsInstance, seed+1, seed+1, false, OM_G31R01.Enums.Combination.MainBCCH);
        int oldConfSignature = confSignature;
        confSignature = confRes.getConfigurationSignature();
        assertFalse("Configuration signature should change", (confSignature == oldConfSignature));
        assertEquals("AccordanceIndication don't match Reaquest", Enums.AccordanceIndication.AccordingToRequest, 
        		confRes.getAccordanceIndication().getAccordanceIndication());
        assertTrue("abisTsMoState for tsInstance " + tsInstance + " is not DISABLED", momHelper.waitForAbisTsMoState(tsInstance, "DISABLED", 5));
        setTestStepEnd();
        
        /*
         * Case 3: Change all optional parameters
         */
        setTestStepBegin("Send TS Configuration Request, combination: MainBCCH, all optional parameters changed, instance: " + tsInstance);
        confRes = abisHelper.tsConfigRequest(tsInstance, seed+1, seed, true, OM_G31R01.Enums.Combination.MainBCCH);
        oldConfSignature = confSignature;
        confSignature = confRes.getConfigurationSignature();
        assertFalse("Configuration signature should change", (confSignature == oldConfSignature));
        assertEquals("AccordanceIndication don't match Reaquest", Enums.AccordanceIndication.AccordingToRequest, 
        		confRes.getAccordanceIndication().getAccordanceIndication());    
        assertTrue("abisTsMoState for tsInstance " + tsInstance + " is not DISABLED", momHelper.waitForAbisTsMoState(tsInstance, "DISABLED", 5));
        setTestStepEnd();
    }

    /**
     * @name tsConfigurationRequest_OptionalBCCH
     * 
     * @description Verifies TS Configuration Request according to NodeUC626.N
     * 
     * @param testId - unique identifier
     * @param description
     * 
     * @throws Exception 
     * 
     */
    @Test (timeOut = 60000)
    @Parameters({ "testId", "description" })
    public void tsConfigurationRequest_OptionalBCCH(String testId, String description) throws Exception {

        // choose tsInstance (0-7) randomly     
        int seed = (int)System.currentTimeMillis();  
        seed = (seed < 0) ? ((-1) * seed) : seed;
        seed = (seed > 2000) ? (seed % 2000) : seed;
        int tsInstance = seed % 8;

        setTestCase(testId, description);
        
        /*
         * Precondition: TS is in state DISABLED
         */
        setTestInfo("Precondition: Verify AO TS is in disabled state, instance: " + tsInstance);
        assertTrue("AO TS is not DISABLED", momHelper.waitForAbisTsMoState(tsInstance, "DISABLED", 5));
     
        /*
         * Case 1: Configure a TS with combination: OptionalBCCH 
         */
        setTestStepBegin("Send TS Configuration Request, combination: OptionalBCCH, instance: " + tsInstance);
        setTestDebug("Random seed: " + seed);
        OM_G31R01.TSConfigurationResult confRes = abisHelper.tsConfigRequest(tsInstance, seed, seed, false, OM_G31R01.Enums.Combination.OptionalBCCH);
        int confSignature = confRes.getConfigurationSignature();
        assertEquals("AccordanceIndication don't match Reaquest", Enums.AccordanceIndication.AccordingToRequest, 
                confRes.getAccordanceIndication().getAccordanceIndication());
        assertTrue("abisTsMoState for tsInstance " + tsInstance + " is not DISABLED", momHelper.waitForAbisTsMoState(tsInstance, "DISABLED", 5));
        setTestStepEnd();
        
        /*
         * Case 2: Change all mandatory parameters
         */
        setTestStepBegin("Send TS Configuration Request, combination: OptionalBCCH, all mandatory parameters changed, instance: " + tsInstance); 
        confRes = abisHelper.tsConfigRequest(tsInstance, seed+1, seed+1, false, OM_G31R01.Enums.Combination.OptionalBCCH);
        int oldConfSignature = confSignature;
        confSignature = confRes.getConfigurationSignature();
        assertFalse("Configuration signature should change", (confSignature == oldConfSignature));
        assertEquals("AccordanceIndication don't match Reaquest", Enums.AccordanceIndication.AccordingToRequest, 
                confRes.getAccordanceIndication().getAccordanceIndication());
        assertTrue("abisTsMoState for tsInstance " + tsInstance + " is not DISABLED", momHelper.waitForAbisTsMoState(tsInstance, "DISABLED", 5));
        setTestStepEnd();
        
        /*
         * Case 3: Change all optional parameters
         */
        setTestStepBegin("Send TS Configuration Request, combination: OptionalBCCH, all optional parameters changed, instance: " + tsInstance);
        confRes = abisHelper.tsConfigRequest(tsInstance, seed+1, seed, true, OM_G31R01.Enums.Combination.OptionalBCCH);
        oldConfSignature = confSignature;
        confSignature = confRes.getConfigurationSignature();
        assertFalse("Configuration signature should change", (confSignature == oldConfSignature));
        assertEquals("AccordanceIndication don't match Reaquest", Enums.AccordanceIndication.AccordingToRequest, 
                confRes.getAccordanceIndication().getAccordanceIndication());    
        assertTrue("abisTsMoState for tsInstance " + tsInstance + " is not DISABLED", momHelper.waitForAbisTsMoState(tsInstance, "DISABLED", 5));
        setTestStepEnd();
    }
    
    /**
     * @name tsConfigurationRequest_SDCCH
     * 
     * @description Verifies TS Configuration Request according to NodeUC626.N
     * 
     * @param testId - unique identifier
     * @param description
     * 
     * @throws Exception 
     * 
     */
    @Test (timeOut = 600000)
    @Parameters({ "testId", "description" })
    public void tsConfigurationRequest_SDCCH(String testId, String description) throws Exception {

        // choose tsInstance (0-7) randomly     
        int seed = (int)System.currentTimeMillis();  
        seed = (seed < 0) ? ((-1) * seed) : seed;
        seed = (seed > 2000) ? (seed % 2000) : seed;
        int tsInstance = seed % 8;

        setTestCase(testId, description);
        
        /*
         * Precondition: TS is in state DISABLED
         */
        setTestInfo("Precondition: Verify AO TS is in disabled state, instance: " + tsInstance);
        assertTrue("AO TS is not DISABLED", momHelper.waitForAbisTsMoState(tsInstance, "DISABLED", 5));
     
        /*
         * Case 1: Configure a TS with combination: SDCCH 
         */
        setTestStepBegin("Send TS Configuration Request, combination: SDCCH, instance: " + tsInstance);
        setTestDebug("Random seed: " + seed);
        OM_G31R01.TSConfigurationResult confRes = abisHelper.tsConfigRequest(tsInstance, seed, seed, false, OM_G31R01.Enums.Combination.SDCCH);
        int confSignature = confRes.getConfigurationSignature();
        assertEquals("AccordanceIndication don't match Reaquest", Enums.AccordanceIndication.AccordingToRequest, 
                confRes.getAccordanceIndication().getAccordanceIndication());
        assertTrue("abisTsMoState for tsInstance " + tsInstance + " is not DISABLED", momHelper.waitForAbisTsMoState(tsInstance, "DISABLED", 5));
        setTestStepEnd();
        
        /*
         * Case 2: Change all mandatory parameters
         */
        setTestStepBegin("Send TS Configuration Request, combination: SDCCH, all mandatory parameters changed, instance: " + tsInstance); 
        confRes = abisHelper.tsConfigRequest(tsInstance, seed+1, seed+1, false, OM_G31R01.Enums.Combination.SDCCH);
        int oldConfSignature = confSignature;
        confSignature = confRes.getConfigurationSignature();
        assertFalse("Configuration signature should change", (confSignature == oldConfSignature));
        assertEquals("AccordanceIndication don't match Reaquest", Enums.AccordanceIndication.AccordingToRequest, 
                confRes.getAccordanceIndication().getAccordanceIndication());
        assertTrue("abisTsMoState for tsInstance " + tsInstance + " is not DISABLED", momHelper.waitForAbisTsMoState(tsInstance, "DISABLED", 5));
        setTestStepEnd();
        
        /*
         * Case 3: Change all optional parameters
         */
        setTestStepBegin("Send TS Configuration Request, combination: SDCCH, all optional parameters changed, instance: " + tsInstance);
        confRes = abisHelper.tsConfigRequest(tsInstance, seed+1, seed, true, OM_G31R01.Enums.Combination.SDCCH);
        oldConfSignature = confSignature;
        confSignature = confRes.getConfigurationSignature();
        assertFalse("Configuration signature should change", (confSignature == oldConfSignature));
        assertEquals("AccordanceIndication don't match Reaquest", Enums.AccordanceIndication.AccordingToRequest, 
                confRes.getAccordanceIndication().getAccordanceIndication());    
        assertTrue("abisTsMoState for tsInstance " + tsInstance + " is not DISABLED", momHelper.waitForAbisTsMoState(tsInstance, "DISABLED", 5));
        setTestStepEnd();
    }
    
    /**
     * @name tsConfigurationRequest_TCH
     * 
     * @description Verifies TS Configuration Request according to NodeUC626.N
     * 
     * @param testId - unique identifier
     * @param description
     * 
     * @throws Exception 
     * 
     */
    @Test (timeOut = 60000)
    @Parameters({ "testId", "description" })
    public void tsConfigurationRequest_TCH(String testId, String description) throws Exception {

    	// choose tsInstance (0-7) randomly
    	int seed = (int)System.currentTimeMillis();  
    	seed = (seed < 0) ? ((-1) * seed) : seed;
    	seed = (seed > 2000) ? (seed % 2000) : seed;
        int tsInstance = seed % 8;
    	
        setTestCase(testId, description);
                
        /*
         * Precondition: TS is in state DISABLED
         */
        setTestInfo("Precondition: Verify AO TS is in disabled state, instance: " + tsInstance);
        assertTrue("AO TS is not DISABLED", momHelper.waitForAbisTsMoState(tsInstance, "DISABLED", 5));
     
        /*
         * Case 1: Configure a TS with combination: TCH
         */
        setTestStepBegin("Send TS Configuration Request, combination: TCH, instance: " + tsInstance);
        setTestDebug("Random seed: " + seed);
        OM_G31R01.TSConfigurationResult confRes = abisHelper.tsConfigRequest(tsInstance, seed, seed, false, OM_G31R01.Enums.Combination.TCH);
        int confSignature = confRes.getConfigurationSignature();
        assertEquals("AccordanceIndication don't match Reaquest", Enums.AccordanceIndication.AccordingToRequest, 
        		confRes.getAccordanceIndication().getAccordanceIndication());
        assertTrue("abisTsMoState for tsInstance " + tsInstance + " is not DISABLED", momHelper.waitForAbisTsMoState(tsInstance, "DISABLED", 5));
        setTestStepEnd();
        
        /*
         * Case 2: Change all mandatory parameters
         */
        setTestStepBegin("Send TS Configuration Request, combination: TCH, all mandatory parameters changed, instance: " + tsInstance); 
        confRes = abisHelper.tsConfigRequest(tsInstance, seed+1, seed+1, false, OM_G31R01.Enums.Combination.TCH);
        int oldConfSignature = confSignature;
        confSignature = confRes.getConfigurationSignature();
        setTestDebug("old confSignature: " + oldConfSignature + " new confSignature: " + confSignature);
        assertFalse("Configuration signature should change", (confSignature == oldConfSignature));
        assertEquals("AccordanceIndication don't match Reaquest", Enums.AccordanceIndication.AccordingToRequest, 
        		confRes.getAccordanceIndication().getAccordanceIndication());
        assertTrue("abisTsMoState for tsInstance " + tsInstance + " is not DISABLED", momHelper.waitForAbisTsMoState(tsInstance, "DISABLED", 5));
        setTestStepEnd();
        
        /*
         * Case 3: Change all optional parameters
         */
        setTestStepBegin("Send TS Configuration Request, combination: TCH, all optional parameters changed, instance: " + tsInstance);
        confRes = abisHelper.tsConfigRequest(tsInstance, seed+1, seed+1, true, OM_G31R01.Enums.Combination.TCH);
        oldConfSignature = confSignature;
        confSignature = confRes.getConfigurationSignature();
        setTestDebug("old confSignature: " + oldConfSignature + " new confSignature: " + confSignature);
        assertFalse("Configuration signature should change", (confSignature == oldConfSignature));
        assertEquals("AccordanceIndication don't match Reaquest", Enums.AccordanceIndication.AccordingToRequest, 
        		confRes.getAccordanceIndication().getAccordanceIndication());
        assertTrue("abisTsMoState for tsInstance " + tsInstance + " is not DISABLED", momHelper.waitForAbisTsMoState(tsInstance, "DISABLED", 5));
        setTestStepEnd();
    }
    
    /**
     * @name tsConfigurationRequest in state enabled valid configuration, combination: TCH
     * 
     * @description Verifies TS Configuration Request according to NodeUC626.A1
     * 
     * @param testId - unique identifier
     * @param description
     * 
     * @throws Exception 
     * 
     */
    @Test (timeOut = 60000)
    @Parameters({ "testId", "description" })
    public void tsConfigurationRequestInStateEnabledValid_TCH(String testId, String description) throws Exception {

    	// choose tsInstance (0-7) randomly
    	int seed = (int)System.currentTimeMillis();  
    	seed = (seed < 0) ? ((-1) * seed) : seed;
    	seed = (seed > 2000) ? (seed % 2000) : seed;
    	int tsInstance = seed % 8;
    	int associatedSoInstance = 0;

    	setTestCase(testId, description);

    	setTestStepBegin("Verify AO TS is in disabled state, instance: " + tsInstance);
    	assertTrue("AO TS is not DISABLED", momHelper.waitForAbisTsMoState(tsInstance, "DISABLED", 5));
    	setTestStepEnd();

    	setTestStepBegin("Send first TS Configuration Request, instance: " + tsInstance);
        setTestDebug("Random seed: " + seed);
    	OM_G31R01.TSConfigurationResult confRes = abisHelper.tsConfigRequest(tsInstance, seed, seed, true, OM_G31R01.Enums.Combination.TCH);
    	setTestStepEnd();

    	setTestStepBegin("Enable AO TS, instance: " + tsInstance);
    	abisHelper.enableRequest(this.moClassTs, tsInstance, associatedSoInstance);
    	assertTrue("AO TS  for tsInstance: " + tsInstance + " is not ENABLED", momHelper.waitForAbisTsMoState(tsInstance, "ENABLED", 5));
    	setTestStepEnd();

    	setTestStepBegin("Send TS Configuration Request in state Enabled, change only parameters allowed to be changed in state ENABLED");
    	confRes = abisHelper.tsConfigRequest(tsInstance, seed, seed+1, true, OM_G31R01.Enums.Combination.TCH);
    	assertEquals("AccordanceIndication don't match Reaquest", 
    			Enums.AccordanceIndication.AccordingToRequest, confRes.getAccordanceIndication().getAccordanceIndication());
    	assertTrue("AO TS  for tsInstance: " + tsInstance + " is not ENABLED", momHelper.waitForAbisTsMoState(tsInstance, "ENABLED", 5));
    	setTestStepEnd();
    }
    
    
    /**
     * @name tsConfigurationRequest in state enabled invalid configuration, combination: TCH
     * 
     * @description Verifies TS Configuration Request according to NodeUC626.A1
     * 
     * @param testId - unique identifier
     * @param description
     * 
     * @throws Exception 
     * 
     */
    @Test (timeOut = 60000)
    @Parameters({ "testId", "description" })
    public void tsConfigurationRequestInStateEnabledInvalid_TCH(String testId, String description) throws Exception {

    	// choose tsInstance (0-7) randomly
    	int seed = (int)System.currentTimeMillis();  
    	seed = (seed < 0) ? ((-1) * seed) : seed;
    	seed = (seed > 2000) ? (seed % 2000) : seed;
    	int tsInstance = seed % 8;
    	int associatedSoInstance = 0;

    	setTestCase(testId, description);

    	setTestStepBegin("Verify AO TS is in disabled state, instance: " + tsInstance);
    	assertTrue("AO TS is not DISABLED", momHelper.waitForAbisTsMoState(tsInstance, "DISABLED", 5));
    	setTestStepEnd();

    	setTestStepBegin("Send first TS Configuration Request, instance: " + tsInstance);
        setTestDebug("Random seed: " + seed);
    	abisHelper.tsConfigRequest(tsInstance, seed, seed, true, OM_G31R01.Enums.Combination.TCH);
    	setTestStepEnd();

    	setTestStepBegin("Enable AO TS, instance: " + tsInstance);
    	abisHelper.enableRequest(this.moClassTs, tsInstance, associatedSoInstance);
    	assertTrue("AO TS  for tsInstance: " + tsInstance + " is not ENABLED", momHelper.waitForAbisTsMoState(tsInstance, "ENABLED", 5));
    	setTestStepEnd();

    	
    	setTestStepBegin("Send TS Configuration Request in state Enabled, change parameter: combination (not allowed)");     	
    	try
    	{ 
    		abisHelper.tsConfigRequestWithChanged_Combination(tsInstance, seed, true, OM_G31R01.Enums.Combination.MainBCCHCombined);
    		fail("Expected Reject on TS Config Request with parameter: Combination changed in state ENABLED");
    	} catch (TSConfigurationRequestRejectException e) {
    		assertEquals("Not ProtocolError: ", Enums.ResultCode.ProtocolError, e.getTSConfigurationRequestReject().getResultCode());
    	}
    	assertTrue("AO TS  for tsInstance: " + tsInstance + " is not ENABLED", momHelper.waitForAbisTsMoState(tsInstance, "ENABLED", 5));
    	setTestStepEnd();

    	
    	setTestStepBegin("Send TS Configuration Request in state Enabled, change parameter: Time Slot Number (not allowed)");
    	try
    	{
    		abisHelper.tsConfigRequestWithChanged_TimeSlotNumber(tsInstance, seed, true, OM_G31R01.Enums.Combination.TCH);
    		fail("Expected Reject on TS Config Request with parameter: Time Slot Number changed in state ENABLED");
    	} catch (TSConfigurationRequestRejectException e) {
    		assertEquals("Not ProtocolError: ", Enums.ResultCode.ProtocolError, e.getTSConfigurationRequestReject().getResultCode());
    	}
    	assertTrue("AO TS  for tsInstance: " + tsInstance + " is not ENABLED", momHelper.waitForAbisTsMoState(tsInstance, "ENABLED", 5));
    	setTestStepEnd();

    	setTestStepBegin("Send TS Configuration Request in state Enabled, change parameter: HSN (not allowed)");
    	try
    	{
    		abisHelper.tsConfigRequestWithChanged_HSN(tsInstance, seed, true, OM_G31R01.Enums.Combination.TCH);
    		fail("Expected Reject on TS Config Request with parameter: HSN changed in state ENABLED");
    	} catch (TSConfigurationRequestRejectException e) {
    		assertEquals("Not ProtocolError: ", Enums.ResultCode.ProtocolError, e.getTSConfigurationRequestReject().getResultCode());
    	}
    	assertTrue("AO TS  for tsInstance: " + tsInstance + " is not ENABLED", momHelper.waitForAbisTsMoState(tsInstance, "ENABLED", 5));
    	setTestStepEnd();

    	setTestStepBegin("Send TS Configuration Request in state Enabled, change parameter: MAIO (not allowed)");
    	try
    	{
    		abisHelper.tsConfigRequestWithChanged_MAIO(tsInstance, seed, true, OM_G31R01.Enums.Combination.TCH);
    		fail("Expected Reject on TS Config Request with parameter: MAIO changed in state ENABLED");
    	} catch (TSConfigurationRequestRejectException e) {
    		assertEquals("Not ProtocolError: ", Enums.ResultCode.ProtocolError, e.getTSConfigurationRequestReject().getResultCode());
    	}
    	assertTrue("AO TS  for tsInstance: " + tsInstance + " is not ENABLED", momHelper.waitForAbisTsMoState(tsInstance, "ENABLED", 5));
    	setTestStepEnd();

    	setTestStepBegin("Send TS Configuration Request in state Enabled, change parameter: BSIC (not allowed)");
    	try
    	{ 
    		abisHelper.tsConfigRequestWithChanged_BSIC(tsInstance, seed, true, OM_G31R01.Enums.Combination.TCH);
    		fail("Expected Reject on TS Config Request with parameter: BSIC changed in state ENABLED");
    	} catch (TSConfigurationRequestRejectException e) {
    		assertEquals("Not ProtocolError: ", Enums.ResultCode.ProtocolError, e.getTSConfigurationRequestReject().getResultCode());
    	}
    	assertTrue("AO TS  for tsInstance: " + tsInstance + " is not ENABLED", momHelper.waitForAbisTsMoState(tsInstance, "ENABLED", 5));
    	setTestStepEnd();

    	setTestStepBegin("Send TS Configuration Request in state Enabled, change parameter: FN Offset (not allowed)");
    	try 
    	{
    		abisHelper.tsConfigRequestWithChanged_FNOffset(tsInstance, seed, true, OM_G31R01.Enums.Combination.TCH);
    		fail("Expected Reject on TS Config Request with parameter: FN Offset changed in state ENABLED");
    	} catch (TSConfigurationRequestRejectException e) {
    		assertEquals("Not ProtocolError: ", Enums.ResultCode.ProtocolError, e.getTSConfigurationRequestReject().getResultCode());
    	}
    	assertTrue("AO TS  for tsInstance: " + tsInstance + " is not ENABLED", momHelper.waitForAbisTsMoState(tsInstance, "ENABLED", 5));
    	setTestStepEnd();

    	setTestStepBegin("Send TS Configuration Request in state Enabled, change parameter: TSC (not allowed)");
    	try 
    	{
    		abisHelper.tsConfigRequestWithChanged_TSC(tsInstance, seed, true);
    		fail("Expected Reject on TS Config Request with parameter: TSC changed in state ENABLED");
    	} catch (TSConfigurationRequestRejectException e) {
    		assertEquals("Not ProtocolError: ", Enums.ResultCode.ProtocolError, e.getTSConfigurationRequestReject().getResultCode());
    	}
    	assertTrue("AO TS  for tsInstance: " + tsInstance + " is not ENABLED", momHelper.waitForAbisTsMoState(tsInstance, "ENABLED", 5));
    	setTestStepEnd();

    	setTestStepBegin("Send TS Configuration Request in state Enabled, change parameter: TTA (not allowed)");
    	try
    	{
    		abisHelper.tsConfigRequestWithChanged_TTA(tsInstance, seed, true);
    		fail("Expected Reject on TS Config Request with parameter: TTA changed in state ENABLED");
    	} catch (TSConfigurationRequestRejectException e) {
    		assertEquals("Not ProtocolError: ", Enums.ResultCode.ProtocolError, e.getTSConfigurationRequestReject().getResultCode());
    	}
    	assertTrue("AO TS  for tsInstance: " + tsInstance + " is not ENABLED", momHelper.waitForAbisTsMoState(tsInstance, "ENABLED", 5));
    	setTestStepEnd();

    	setTestStepBegin("Send TS Configuration Request in state Enabled, change parameter: ICM Channel Rate (not allowed)");
    	try 
    	{
    		abisHelper.tsConfigRequestWithChanged_ICMChannelRate(tsInstance, seed, true);
    		fail("Expected Reject on TS Config Request with parameter: ICM Channel Rate changed in state ENABLED");
    	} catch (TSConfigurationRequestRejectException e) {
    		assertEquals("Not ProtocolError: ", Enums.ResultCode.ProtocolError, e.getTSConfigurationRequestReject().getResultCode());
    	}
    	assertTrue("AO TS  for tsInstance: " + tsInstance + " is not ENABLED", momHelper.waitForAbisTsMoState(tsInstance, "ENABLED", 5));
    	setTestStepEnd();

    	setTestStepBegin("Send TS Configuration Request in state Enabled, change parameter: Link Supervision Control (not allowed)");
    	try
    	{
    		abisHelper.tsConfigRequestWithChanged_LinkSupervisionControl(tsInstance, seed, true);
    		fail("Expected Reject on TS Config Request with parameter: Link Supervision Control changed in state ENABLED");
    	} catch (TSConfigurationRequestRejectException e) {
    		assertEquals("Not ProtocolError: ", Enums.ResultCode.ProtocolError, e.getTSConfigurationRequestReject().getResultCode());
    	}
    	assertTrue("AO TS  for tsInstance: " + tsInstance + " is not ENABLED", momHelper.waitForAbisTsMoState(tsInstance, "ENABLED", 5));
    	setTestStepEnd();
    	
    	setTestStepBegin("Send TS Configuration Request in state Enabled, change parameter: Link Supervision Filtering Time (not allowed)");
    	try
    	{
    		abisHelper.tsConfigRequestWithChanged_LinkSupervisionFilteringTime(tsInstance, seed, true);
    		fail("Expected Reject on TS Config Request with parameter: Link Supervision Filtering Time changed in state ENABLED");
    	} catch (TSConfigurationRequestRejectException e) {
    		assertEquals("Not ProtocolError: ", Enums.ResultCode.ProtocolError, e.getTSConfigurationRequestReject().getResultCode());
    	}
    	assertTrue("AO TS  for tsInstance: " + tsInstance + " is not ENABLED", momHelper.waitForAbisTsMoState(tsInstance, "ENABLED", 5));
    	setTestStepEnd();
    	
       	setTestStepBegin("Send TS Configuration Request in state Enabled, change parameter: Call Supervision Time (not allowed)");
    	try
    	{
    		abisHelper.tsConfigRequestWithChanged_CallSupervisionTime(tsInstance, seed, true);
    		fail("Expected Reject on TS Config Request with parameter: Call Supervision Time changed in state ENABLED");
    	} catch (TSConfigurationRequestRejectException e) {
    		assertEquals("Not ProtocolError: ", Enums.ResultCode.ProtocolError, e.getTSConfigurationRequestReject().getResultCode());
    	}
    	assertTrue("AO TS  for tsInstance: " + tsInstance + " is not ENABLED", momHelper.waitForAbisTsMoState(tsInstance, "ENABLED", 5));
    	setTestStepEnd();
    }
    
    /**
     * @name tsConfigurationRequest_TCHDefaultValueTest
     * 
     * @description Verifies TS Configuration Request according to NodeUC626.N
     * 
     * @param testId - unique identifier
     * @param description
     * 
     * @throws Exception 
     * 
     */
    @Test (timeOut = 60000)
    @Parameters({ "testId", "description" })
    public void tsConfigurationRequest_TCHDefaultValueTest(String testId, String description) throws Exception {

        // choose tsInstance (0-7) randomly
        int seed = (int)System.currentTimeMillis();  
        seed = (seed < 0) ? ((-1) * seed) : seed;
        seed = (seed > 2000) ? (seed % 2000) : seed;
        int tsInstance = seed % 8;
        
        setTestCase(testId, description);
                
        /*
         * Precondition: TS is in state DISABLED
         */
        setTestInfo("Precondition: Verify AO TS is in disabled state, instance: " + tsInstance);
        assertTrue("AO TS is not DISABLED", momHelper.waitForAbisTsMoState(tsInstance, "DISABLED", 5));
     
        /*
         * Case 1: Configure a TS with combination: TCH
         */
        setTestStepBegin("Send TS Configuration Request, combination: TCH, instance: " + tsInstance);
        setTestDebug("Random seed: " + seed);
        OM_G31R01.TSConfigurationResult confRes = abisHelper.tsConfigRequestFirstCall(tsInstance, OM_G31R01.Enums.Combination.TCH);
        int confSignature = confRes.getConfigurationSignature();
        assertEquals("AccordanceIndication don't match Reaquest", Enums.AccordanceIndication.AccordingToRequest, 
                confRes.getAccordanceIndication().getAccordanceIndication());
        setTestStepEnd();
        
        /*
         * Case 2: Include default value for link supervision filtering time in request
         */
        setTestStepBegin("Send TS Configuration Request, combination: TCH, set link supervision filtering time to default value: " + tsInstance); 
        confRes = abisHelper.tsConfigRequestFirstCall(tsInstance, OM_G31R01.Enums.Combination.TCH, true);
        int oldConfSignature = confSignature;
        confSignature = confRes.getConfigurationSignature();
        setTestDebug("old confSignature: " + oldConfSignature + " new confSignature: " + confSignature);
        assertEquals("Configuration signature should not change", confSignature, oldConfSignature);
        assertEquals("AccordanceIndication don't match Reaquest", Enums.AccordanceIndication.AccordingToRequest, 
                confRes.getAccordanceIndication().getAccordanceIndication());
    	assertTrue("AO TS  for tsInstance: " + tsInstance + " is not DISABLED", momHelper.waitForAbisTsMoState(tsInstance, "DISABLED", 5));

        setTestStepEnd();
        
    }
    
    /**
     * @name tsConfigurationRequest in state enabled invalid configuration, combination: MainBCCHCombined
     * 
     * @description Verifies TS Configuration Request according to NodeUC626.A1
     * 
     * @param testId - unique identifier
     * @param description
     * 
     * @throws Exception 
     * 
     */
    @Test (timeOut = 60000)
    @Parameters({ "testId", "description" })
    public void tsConfigurationRequestInStateEnabledInvalid_MainBCCHCombined(String testId, String description) throws Exception {

    	// choose tsInstance (0-7) randomly
    	int seed = (int)System.currentTimeMillis();  
    	seed = (seed < 0) ? ((-1) * seed) : seed;
    	seed = (seed > 2000) ? (seed % 2000) : seed;
    	int tsInstance = seed % 8;
    	int associatedSoInstance = 0;

    	setTestCase(testId, description);

    	setTestStepBegin("Verify AO TS is in disabled state, instance: " + tsInstance);
    	assertTrue("AO TS is not DISABLED", momHelper.waitForAbisTsMoState(tsInstance, "DISABLED", 5));
    	setTestStepEnd();

    	setTestStepBegin("Send first TS Configuration Request, instance: " + tsInstance);
        setTestDebug("Random seed: " + seed);
    	abisHelper.tsConfigRequest(tsInstance, seed, seed, true, OM_G31R01.Enums.Combination.MainBCCHCombined);
    	setTestStepEnd();

    	setTestStepBegin("Enable AO TS, instance: " + tsInstance);
    	abisHelper.enableRequest(this.moClassTs, tsInstance, associatedSoInstance);
    	assertTrue("AO TS  for tsInstance: " + tsInstance + " is not ENABLED", momHelper.waitForAbisTsMoState(tsInstance, "ENABLED", 5));
    	setTestStepEnd();

    	setTestStepBegin("Send TS Configuration Request in state Enabled, change parameter: BS PA MFRMS (not allowed)");
    	try
    	{
    		abisHelper.tsConfigRequestWithChanged_BsPaMfrms(tsInstance, seed, true);
    		fail("Expected Reject on TS Config Request with parameter: BS PA MFRMS changed in state ENABLED");
    	} catch (TSConfigurationRequestRejectException e) {
    		assertEquals("Not ProtocolError: ", Enums.ResultCode.ProtocolError, e.getTSConfigurationRequestReject().getResultCode());
    	}
    	assertTrue("AO TS  for tsInstance: " + tsInstance + " is not ENABLED", momHelper.waitForAbisTsMoState(tsInstance, "ENABLED", 5));
    	setTestStepEnd();

    	setTestStepBegin("Send TS Configuration Request in state Enabled, change parameter: CBCH Indicator (not allowed)");
    	try
    	{
    		abisHelper.tsConfigRequestWithChanged_CbchIndicator(tsInstance, seed, true);
    		fail("Expected Reject on TS Config Request with parameter: CBCH Indicator changed in state ENABLED");
    	} catch (TSConfigurationRequestRejectException e) {
    		assertEquals("Not ProtocolError: ", Enums.ResultCode.ProtocolError, e.getTSConfigurationRequestReject().getResultCode());
    	}
    	assertTrue("AO TS  for tsInstance: " + tsInstance + " is not ENABLED", momHelper.waitForAbisTsMoState(tsInstance, "ENABLED", 5));
    	setTestStepEnd();

    	setTestStepBegin("Send TS Configuration Request in state Enabled, change parameter: BS AG BLKS RES (not allowed)");
    	try
    	{
    		abisHelper.tsConfigRequestWithChanged_BsAgBlksRes(tsInstance, seed, true);
    		fail("Expected Reject on TS Config Request with parameter: BS AG BLKS RES changed in state ENABLED");
    	} catch (TSConfigurationRequestRejectException e) {
    		assertEquals("Not ProtocolError: ", Enums.ResultCode.ProtocolError, e.getTSConfigurationRequestReject().getResultCode());
    	}
    	assertTrue("AO TS  for tsInstance: " + tsInstance + " is not ENABLED", momHelper.waitForAbisTsMoState(tsInstance, "ENABLED", 5));
    	setTestStepEnd();
    }
}
