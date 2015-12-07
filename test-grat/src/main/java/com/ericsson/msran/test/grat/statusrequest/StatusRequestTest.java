package com.ericsson.msran.test.grat.statusrequest;

import java.util.concurrent.TimeUnit;

import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.ericsson.msran.jcat.TestBase;

import se.ericsson.jcat.fw.annotations.Setup;
import se.ericsson.jcat.fw.annotations.Teardown;

import com.ericsson.abisco.clientlib.servers.BG.Enums.OperationalCondition;
import com.ericsson.abisco.clientlib.servers.BG;
import com.ericsson.abisco.clientlib.servers.OM_G31R01;
import com.ericsson.abisco.clientlib.servers.OM_G31R01.Enums;
import com.ericsson.abisco.clientlib.servers.OM_G31R01.TFConfigurationResult;
import com.ericsson.msran.g2.annotations.TestInfo;
import com.ericsson.msran.test.grat.testhelpers.AbisHelper;
import com.ericsson.msran.test.grat.testhelpers.MomHelper;
import com.ericsson.msran.test.grat.testhelpers.NodeStatusHelper;
import com.ericsson.msran.test.grat.testhelpers.prepost.AbisPrePost;

/**
 * @id NodeUC478
 * 
 * @name StatusRequestTest
 * 
 * @author Marika Johansson (xrsmari)
 * 
 * @created 2013-10-01
 * 
 * @description This test class verifies the Abis Status Request command.
 * 
 * @revision xrsmari 2013-10-01 First version.
 * @revision xasahuh 2013-11-21 Updated for usage of Abisco and RBS in TASS.
 * @revision xasahuh 2014-02-07 Set timeout for test case to 1 minute.
 * @revision xasahuh 2014-02-10 Added TestInfo metadata.
 * @revision xasahuh 2014-02-12 Removed AfterClass, the RestoreStack is used instead.
 * 
 */

public class StatusRequestTest extends TestBase {
    private final OM_G31R01.Enums.MOClass soScfClass = OM_G31R01.Enums.MOClass.SCF;
    private final OM_G31R01.Enums.MOClass soTrxcClass = OM_G31R01.Enums.MOClass.TRXC;
    private final OM_G31R01.Enums.MOClass aoTfClass = OM_G31R01.Enums.MOClass.TF;
    private final OM_G31R01.Enums.MOClass aoAtClass = OM_G31R01.Enums.MOClass.AT;
    private final OM_G31R01.Enums.MOClass aoTxClass = OM_G31R01.Enums.MOClass.TX;
    private final OM_G31R01.Enums.MOClass aoRxClass = OM_G31R01.Enums.MOClass.RX;
    private final OM_G31R01.Enums.MOClass aoTsClass = OM_G31R01.Enums.MOClass.TS;
    
    private AbisPrePost abisPrePost;
    private AbisHelper abisHelper;
    private NodeStatusHelper nodeStatus;
    private MomHelper momHelper;

    
    /**
     * Description of test case for test reporting
     */
    @TestInfo(
            tcId = "NodeUC478",
            slogan = "Abis OML Status Request EP on SO SCF",
            requirementDocument = "1/00651-FCP 130 1402",
            requirementRevision = "PC5",
            requirementLinkTested = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff87c1d941?docno=1/00651-FCP1301402Uen&format=excel8book",
            requirementLinkLatest = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff86a2af3d?docno=1/00651-FCP1301402Uen&action=current&format=excel8book",
            requirementIds = { "10565-0334/21767[PA1][PREL]", "10565-0334/19034[A][APPR]",
                    "10565-0334/19417[A][APPR]" },
            verificationStatement = "Verifies NodeUC478.N",
            testDescription = "Verifies Abis SO SCF Status Request.",
            traceGuidelines = "N/A")


    /**
     * Create MO:s, establish links to Abisco, and verify preconditions.
     * @throws InterruptedException 
     */
    @Setup
    public void setup() throws InterruptedException {
    	setTestStepBegin("Setup");
        nodeStatus = new NodeStatusHelper();
        assertTrue("Node did not reach a working state before test start", nodeStatus.waitForNodeReady());
        abisHelper = new AbisHelper();
        abisPrePost = new AbisPrePost();
        momHelper = new MomHelper();
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
     * @name statusRequest
     * 
     * @description Verifies the Status Request EP according to NodeUC478.N Case
     *              1: Status Request in MO State = RESET Case 2: Status Request
     *              in MO State = STARTED
     *              
     * @param testId - unique identifier
     * @param description
     * 
     * @throws InterruptedException
     */
    @Test (timeOut = 360000)
    @Parameters({ "testId", "description" })
    public void statusRequest(String testId, String description) throws InterruptedException {
        
        setTestCase(testId, description);

        OM_G31R01.Enums.MOState expectedMoState;
        int expectedOfferedNegotiationSig;
        int expectedAgreedNegotiationSig;

        OM_G31R01.StartResult startRes;
        OM_G31R01.StatusResponse scfStatusRsp;
        OM_G31R01.Enums.MOState soScfState;
        int calandarTimeSequenceNumber = 0;
        int offeredNegotiationSig;
        int agreedNegotiationSig;
        int bscNodeIdentitySig = 0;
        int btsNodeIdentitySig = 0;
        int bscCapabilitiesSig = 0;
        int btsCapabilitiesSig = 0;

        /*
         * Case 1: MO State RESET
         */
        setTestStepBegin("Send Reset Command to SO SCF");
        abisHelper.resetCommand(this.soScfClass);

        /*
         * Status Request
         */
        setTestStepBegin("Send Status Request to SO SCF");
        scfStatusRsp = abisHelper.statusRequest(this.soScfClass);
        soScfState = scfStatusRsp.getMOState();
        calandarTimeSequenceNumber = scfStatusRsp.getStatusChoice().getStatusSCF().getCalendarTimeSequenceNum();
        offeredNegotiationSig = scfStatusRsp.getStatusChoice().getStatusSCF().getOfferedNegotiationSignature();
        agreedNegotiationSig = scfStatusRsp.getStatusChoice().getStatusSCF().getAgreedNegotiationSignature();
        bscNodeIdentitySig = scfStatusRsp.getStatusChoice().getStatusSCF().getBSCNodeIdentitySignature();
        btsNodeIdentitySig = scfStatusRsp.getStatusChoice().getStatusSCF().getBTSNodeIdentitySignature();
        bscCapabilitiesSig = scfStatusRsp.getStatusChoice().getStatusSCF().getBSCCapabilitiesSignature();
        btsCapabilitiesSig = scfStatusRsp.getStatusChoice().getStatusSCF().getBTSCapabilitiesSignature();

        /*
         * Expect MO State to be RESET Expect NegotiationSignatures to be 0
         */
        setTestStepBegin("Verify that MO State in Status Response is RESET and that signatures are 0");
        expectedMoState = OM_G31R01.Enums.MOState.RESET;
        expectedOfferedNegotiationSig = 0;
        expectedAgreedNegotiationSig = 0;

        saveAssertEquals("soScfState not what expected", expectedMoState, soScfState);
        saveAssertEquals("Calendar Time Sequence number is not what expected", 0, calandarTimeSequenceNumber);
        saveAssertEquals("expectedOfferedNegotiationSig not 0 as expected", expectedOfferedNegotiationSig, offeredNegotiationSig);
        saveAssertEquals("expectedAgreedNegotiationSig not 0 as expected", expectedAgreedNegotiationSig, agreedNegotiationSig);
        saveAssertEquals("bscNodeIdentitySig is not what expected", 0, bscNodeIdentitySig);
        saveAssertEquals("btsNodeIdentitySig is not what expected", 0, btsNodeIdentitySig);
        saveAssertEquals("bscCapabilitiesSig is not what expected", 0, bscCapabilitiesSig);
        saveAssertEquals("btsCapabilitiesSig is not what expected", 0, btsCapabilitiesSig);
        
  
        /*
         * AO TF and AO AT must be reset as a result of the first reset on SO SCF
         * configuration signature must be 0 after AO TF reset and AO AT reset
         * SO SCF must be started otherwise there is a blocking predecessor relation from SO SCF to AO AT and AO TF
         */
        startRes = abisHelper.startRequest(this.soScfClass, 0);
        OM_G31R01.StatusResponse tfStatusRsp = abisHelper.statusRequest(this.aoTfClass);
        saveAssertEquals("configuration signature must be 0 after AO TF reset and AO AT reset", 0, tfStatusRsp.getStatusChoice().getStatusAO().getConfigurationSignature());
        OM_G31R01.StatusResponse atStatusRsp = abisHelper.statusRequest(this.aoAtClass);
        saveAssertEquals("configuration signature must be 0 after AO TF reset and AO AT reset", 0, atStatusRsp.getStatusChoice().getStatusAO().getConfigurationSignature()); 
        
        /**
         * Case 2: MO State STARTED
         */
        setTestStepBegin("Send Start Request to SO SCF");
        abisHelper.startRequest(this.aoTfClass, 0);
        abisHelper.startRequest(this.aoAtClass, 0);

        /*
         * Status Request
         */
        setTestStepBegin("Send Status Request to SO SCF");
        scfStatusRsp = abisHelper.statusRequest(this.soScfClass);
        soScfState = scfStatusRsp.getMOState();
        calandarTimeSequenceNumber = scfStatusRsp.getStatusChoice().getStatusSCF().getCalendarTimeSequenceNum();
        offeredNegotiationSig = scfStatusRsp.getStatusChoice().getStatusSCF().getOfferedNegotiationSignature();
        agreedNegotiationSig = scfStatusRsp.getStatusChoice().getStatusSCF().getAgreedNegotiationSignature();
        bscNodeIdentitySig = scfStatusRsp.getStatusChoice().getStatusSCF().getBSCNodeIdentitySignature();
        btsNodeIdentitySig = scfStatusRsp.getStatusChoice().getStatusSCF().getBTSNodeIdentitySignature();
        bscCapabilitiesSig = scfStatusRsp.getStatusChoice().getStatusSCF().getBSCCapabilitiesSignature();
        btsCapabilitiesSig = scfStatusRsp.getStatusChoice().getStatusSCF().getBTSCapabilitiesSignature();

        /*
         * Expect MO State to be STARTED Expect NegotiationSignatures to be
         * equal to signatures in startResult
         */
        expectedMoState = startRes.getMOState();
        expectedOfferedNegotiationSig = startRes.getOfferedNegotiationSignature();
        expectedAgreedNegotiationSig = startRes.getAgreedNegotiationSignature();

        setTestStepBegin("Verify that MO State in Status Response is STARTED and that signatures are equal to signatures in Start Result");
        saveAssertEquals("soScfState is not what expected...", expectedMoState, soScfState);
        saveAssertEquals("offeredNegotiationSig is not what expected...", expectedOfferedNegotiationSig, offeredNegotiationSig);
        saveAssertEquals("agreedNegotiationSig is not what expected...", expectedAgreedNegotiationSig, agreedNegotiationSig);
        setTestStepEnd();
        setTestStepBegin("Verify that signatures of BTS Capabilities and BTS Node Identity have been calculated");
        saveAssertTrue("Calendar Time Sequence number is not what expected", 0 == calandarTimeSequenceNumber);      
        saveAssertTrue("bscNodeIdentitySig is not what expected", 0 == bscNodeIdentitySig);
        saveAssertTrue("btsNodeIdentitySig is not what expected", 0 != btsNodeIdentitySig);
        saveAssertTrue("bscCapabilitiesSig is not what expected", 0 == bscCapabilitiesSig);
        saveAssertTrue("btsCapabilitiesSig is not what expected", 0 != btsCapabilitiesSig);
        

        tfStatusRsp = abisHelper.statusRequest(this.aoTfClass);
        saveAssertEquals("ConfigurationSignature not 0", 0, tfStatusRsp.getStatusChoice().getStatusAO().getConfigurationSignature());
        
        try {
            int synchStatus = tfStatusRsp.getStatusChoice().getStatusAO().getOperationalConditionReasonsMap().getNoFrameSynch();
            setTestDebug("no exception is thrown. SynschStatus " + synchStatus);
            fail("tfStatusRsp.getStatusChoice().getStatusAO().getOperationalConditionReasonsMap() must thrown a null exception because we have synch now and nothing abnormal to report");
        } catch (Exception e) {
            setTestDebug("exception " + e.getMessage() + " is thrown but that is expected, because we have synch now and nothing abnormal to report");
        }
        
        atStatusRsp = abisHelper.statusRequest(this.aoAtClass);
        saveAssertEquals("ConfigurationSignature not 0", 0, atStatusRsp.getStatusChoice().getStatusAO().getConfigurationSignature());

        /*
         * Configuration Request now to AO TF, to get a non-zero configuration signature
         */
        OM_G31R01.TFConfigurationRequest confReq = abisHelper.initTfConfigRequest();

        //default constructor will set FS offset to 0, which is valid
        OM_G31R01.FSOffset fsOffset = new OM_G31R01.FSOffset();
        confReq.setFSOffset(fsOffset);
        TFConfigurationResult tfConfigResult = confReq.send();
        int configSignature = tfConfigResult.getConfigurationSignature();

        /*
         * now verify that Status Request on AO TF will return the same configuration signature as was sent in Configuration Result
         */
        tfStatusRsp = abisHelper.statusRequest(this.aoTfClass);
        saveAssertEquals("ConfigurationSignature is not " + configSignature, configSignature, tfStatusRsp.getStatusChoice().getStatusAO().getConfigurationSignature()); 

        
        setTestStepBegin("Test that the Calendar time exchange is updated in the status request");
        calandarTimeSequenceNumber = abisHelper.calendarTimeExchange(0, 2014, 12, 13, 15, 4, 25, 8).getCalendarTimeSequenceNum();
        scfStatusRsp = abisHelper.statusRequest(this.soScfClass);
        
        // only the calendar time should be different
        saveAssertTrue("Calendar Time Sequence number is not what expected", calandarTimeSequenceNumber == scfStatusRsp.getStatusChoice().getStatusSCF().getCalendarTimeSequenceNum());
        saveAssertEquals("expectedOfferedNegotiationSig not as expected", expectedOfferedNegotiationSig, scfStatusRsp.getStatusChoice().getStatusSCF().getOfferedNegotiationSignature());
        saveAssertEquals("expectedAgreedNegotiationSig not as expected", expectedAgreedNegotiationSig, agreedNegotiationSig = scfStatusRsp.getStatusChoice().getStatusSCF().getAgreedNegotiationSignature());
        saveAssertTrue("bscNodeIdentitySig is not what expected", 0 == scfStatusRsp.getStatusChoice().getStatusSCF().getBSCNodeIdentitySignature());
        saveAssertTrue("btsNodeIdentitySig is not what expected", btsNodeIdentitySig == scfStatusRsp.getStatusChoice().getStatusSCF().getBTSNodeIdentitySignature());
        saveAssertTrue("bscCapabilitiesSig is not what expected", 0 == scfStatusRsp.getStatusChoice().getStatusSCF().getBSCCapabilitiesSignature());
        saveAssertTrue("btsCapabilitiesSig is not what expected", btsCapabilitiesSig == scfStatusRsp.getStatusChoice().getStatusSCF().getBTSCapabilitiesSignature());
        
        setTestStepBegin("Test that the Bsc Id is updated in the status request");
        bscNodeIdentitySig = 0xABCD;
        abisHelper.nodeIdentityExchange(0, "BSC_ID_1", "TG_ID_0", bscNodeIdentitySig);
        scfStatusRsp = abisHelper.statusRequest(this.soScfClass);
        
        // only the BSC id signature should be different
        saveAssertTrue("Calendar Time Sequence number is not what expected", calandarTimeSequenceNumber == scfStatusRsp.getStatusChoice().getStatusSCF().getCalendarTimeSequenceNum());      
        saveAssertEquals("expectedOfferedNegotiationSig not as expected", expectedOfferedNegotiationSig, scfStatusRsp.getStatusChoice().getStatusSCF().getOfferedNegotiationSignature());
        saveAssertEquals("expectedAgreedNegotiationSig not as expected", expectedAgreedNegotiationSig, agreedNegotiationSig = scfStatusRsp.getStatusChoice().getStatusSCF().getAgreedNegotiationSignature());
        saveAssertTrue("bscNodeIdentitySig is not what expected", bscNodeIdentitySig == scfStatusRsp.getStatusChoice().getStatusSCF().getBSCNodeIdentitySignature());
        saveAssertTrue("btsNodeIdentitySig is not what expected", btsNodeIdentitySig == scfStatusRsp.getStatusChoice().getStatusSCF().getBTSNodeIdentitySignature());
        saveAssertTrue("bscCapabilitiesSig is not what expected", 0 == scfStatusRsp.getStatusChoice().getStatusSCF().getBSCCapabilitiesSignature());
        saveAssertTrue("btsCapabilitiesSig is not what expected", btsCapabilitiesSig == scfStatusRsp.getStatusChoice().getStatusSCF().getBTSCapabilitiesSignature());
        
        
        setTestStepBegin("Test that the Bsc capability signature is updated in the status request");
        bscCapabilitiesSig = 0xBEEF;
        abisHelper.scfCapabilitiesExchangeRequest(0, bscCapabilitiesSig);
        scfStatusRsp = abisHelper.statusRequest(this.soScfClass);
        
        // only the BSC capabilities signature should be different
        saveAssertTrue("Calendar Time Sequence number is not what expected", calandarTimeSequenceNumber == scfStatusRsp.getStatusChoice().getStatusSCF().getCalendarTimeSequenceNum());      
        saveAssertEquals("expectedOfferedNegotiationSig not as expected", expectedOfferedNegotiationSig, scfStatusRsp.getStatusChoice().getStatusSCF().getOfferedNegotiationSignature());
        saveAssertEquals("expectedAgreedNegotiationSig not as expected", expectedAgreedNegotiationSig, agreedNegotiationSig = scfStatusRsp.getStatusChoice().getStatusSCF().getAgreedNegotiationSignature());
        saveAssertTrue("bscNodeIdentitySig is not what expected", bscNodeIdentitySig == scfStatusRsp.getStatusChoice().getStatusSCF().getBSCNodeIdentitySignature());
        saveAssertTrue("btsNodeIdentitySig is not what expected", btsNodeIdentitySig == scfStatusRsp.getStatusChoice().getStatusSCF().getBTSNodeIdentitySignature());
        saveAssertTrue("bscCapabilitiesSig is not what expected", bscCapabilitiesSig == scfStatusRsp.getStatusChoice().getStatusSCF().getBSCCapabilitiesSignature());
        saveAssertTrue("btsCapabilitiesSig is not what expected", btsCapabilitiesSig == scfStatusRsp.getStatusChoice().getStatusSCF().getBTSCapabilitiesSignature());
        
        
        /*
         * now verify a new Reset will clear the configuration signature
         */
        abisHelper.resetCommand(this.aoTfClass);
        tfStatusRsp = abisHelper.statusRequest(this.aoTfClass);
        saveAssertEquals("ConfigurationSignature not 0", 0, tfStatusRsp.getStatusChoice().getStatusAO().getConfigurationSignature());
        
        
        /*
         * Verfy that all the signatures will be reset when SO SCF is reset
         */
        setTestStepBegin("Send Reset Command to SO SCF");
        abisHelper.resetCommand(this.soScfClass);
        scfStatusRsp = abisHelper.statusRequest(this.soScfClass);
        saveAssertTrue("Calendar Time Sequence number is not what expected", 0 == scfStatusRsp.getStatusChoice().getStatusSCF().getCalendarTimeSequenceNum());      
        saveAssertTrue("expectedOfferedNegotiationSig not as expected",      0 == scfStatusRsp.getStatusChoice().getStatusSCF().getOfferedNegotiationSignature());
        saveAssertTrue("expectedAgreedNegotiationSig not as expected",       0 == scfStatusRsp.getStatusChoice().getStatusSCF().getAgreedNegotiationSignature());
        saveAssertTrue("bscNodeIdentitySig is not what expected",            0 == scfStatusRsp.getStatusChoice().getStatusSCF().getBSCNodeIdentitySignature());
        saveAssertTrue("btsNodeIdentitySig is not what expected",            0 == scfStatusRsp.getStatusChoice().getStatusSCF().getBTSNodeIdentitySignature());
        saveAssertTrue("bscCapabilitiesSig is not what expected",            0 == scfStatusRsp.getStatusChoice().getStatusSCF().getBSCCapabilitiesSignature());
        saveAssertTrue("btsCapabilitiesSig is not what expected",            0 == scfStatusRsp.getStatusChoice().getStatusSCF().getBTSCapabilitiesSignature());
    }
    
    /**
     * @name statusRequest for SO TRXC, AO TX and AO RX
     * 
     * @description Verifies the Status Request EP according to NodeUC478.N Case
     *              1: Status Request in MO State = RESET Case 2: Status Request
     *              in MO State = STARTED
     *              
     * @param testId - unique identifier
     * @param description
     * 
     * @throws InterruptedException
     */
    @Test (timeOut = 360000)
    @Parameters({ "testId", "description" })
    public void statusRequestSoTrxcAoTxAoRx(String testId, String description) throws InterruptedException {
        
        setTestCase(testId, description);

        OM_G31R01.Enums.MOState expectedMoState;
        OM_G31R01.StartResult startRes;
        OM_G31R01.StatusResponse trxcStatusRsp;
        OM_G31R01.Enums.MOState soTrxcState;

        /*
         * Case 1: MO State RESET (done in setup)
         */

        /*
         * Status Request
         */
        setTestStepBegin("Send Status Request to SO TRXC");
        trxcStatusRsp = abisHelper.statusRequest(this.soTrxcClass);
        soTrxcState = trxcStatusRsp.getMOState();
        setTestStepEnd();
        
        /*
         * Expect MO State to be RESET Expect NegotiationSignatures to be 0
         */
        setTestStepBegin("Verify that MO State in Status Response is RESET and that signatures are 0");
        expectedMoState = OM_G31R01.Enums.MOState.RESET;
        
        saveAssertEquals("soTrxcState is not what expected", expectedMoState, soTrxcState);
        setTestStepEnd();
        
        /**
         * Case 2: MO State STARTED
         */
        setTestStepBegin("Send Start Request to SO TRXC");
        startRes = abisHelper.startRequest(this.soScfClass, 0);
        startRes = abisHelper.startRequest(this.soTrxcClass, 0);
        setTestStepEnd();
        
        /*
         * Status Request
         */
        setTestStepBegin("Send Status Request to SO TRXC");
        trxcStatusRsp = abisHelper.statusRequest(this.soTrxcClass);
        soTrxcState = trxcStatusRsp.getMOState();
        setTestStepEnd();
        
        /*
         * Expect MO State to be STARTED Expect NegotiationSignatures to be
         * equal to signatures in startResult
         */
        expectedMoState = startRes.getMOState();

        setTestStepBegin("Verify that MO State in Status Response is STARTED and that signatures are equal to signatures in Start Result");
        saveAssertEquals("soTrxcState is not what expected", expectedMoState, soTrxcState);
        setTestStepEnd();
        
        abisHelper.resetCommand(this.aoTxClass);
        OM_G31R01.StatusResponse txStatusRsp = abisHelper.statusRequest(this.aoTxClass);
        saveAssertEquals("ConfigurationSignature in not 0", 0, txStatusRsp.getStatusChoice().getStatusAO().getConfigurationSignature()); 

        /*
         * Configuration Request now to AO TX, to get a non-zero configuration signature
         */
        abisHelper.startRequest(this.aoTxClass, 0);
        OM_G31R01.TXConfigurationResult txConfRes = abisHelper.txConfigRequest(0, momHelper.getArfcnToUse(), false, 27, false);
        int configSignature = txConfRes.getConfigurationSignature();

        /*
         * now verify that Status Request on AO TX will return the same configuration signature as was sent in Configuration Result
         */
        txStatusRsp = abisHelper.statusRequest(this.aoTxClass);
        setTestDebug("After start " + configSignature);
        saveAssertEquals("configuration signature must not change after AO TX Status Request", configSignature, txStatusRsp.getStatusChoice().getStatusAO().getConfigurationSignature()); 
        saveAssertEquals("Operational Condition must be Operational for AO TX", OperationalCondition.Operational.getValue(), txStatusRsp.getStatusChoice().getStatusAO().getOperationalCondition().getOperationalCondition().getValue());
        saveAssertEquals("Operational Condition Reason Map must be empty for AO TX", null, txStatusRsp.getStatusChoice().getStatusAO().getOperationalConditionReasonsMap());
        saveAssertEquals("Operational Condition Text must be empty for AO TX", null, txStatusRsp.getStatusChoice().getStatusAO().getOperationalConditionText());

        /*
         * now verify a new Reset will clear the configuration signature
         */
        abisHelper.resetCommand(this.aoTxClass);
        txStatusRsp = abisHelper.statusRequest(this.aoTxClass);
        saveAssertEquals("configuration signature be 0 after AO TX Status Request", 0, txStatusRsp.getStatusChoice().getStatusAO().getConfigurationSignature());
               
        //RX
        abisHelper.resetCommand(this.aoRxClass);
        OM_G31R01.StatusResponse rxStatusRsp = abisHelper.statusRequest(this.aoRxClass);
        saveAssertEquals("RX configuration signature must be 0", 0, rxStatusRsp.getStatusChoice().getStatusAO().getConfigurationSignature()); 

        /*
         * Configuration Request now to AO RX, to get a non-zero configuration signature
         */
        abisHelper.startRequest(this.aoRxClass, 0);
        OM_G31R01.RXConfigurationResult rxConfRes = abisHelper.rxConfigRequest(0, momHelper.getArfcnToUse(), false, "");
        configSignature = rxConfRes.getConfigurationSignature();

        /*
         * now verify that Status Request on AO RX will return the same configuration signature as was sent in Configuration Result
         */
        rxStatusRsp = abisHelper.statusRequest(this.aoRxClass);
        setTestDebug("After start " + configSignature);
        saveAssertEquals("configuration signature must not change after AO RX Status Request", configSignature, rxStatusRsp.getStatusChoice().getStatusAO().getConfigurationSignature()); 
        saveAssertEquals("Operational Condition must be Operational for AO RX", OperationalCondition.Operational.getValue(), rxStatusRsp.getStatusChoice().getStatusAO().getOperationalCondition().getOperationalCondition().getValue());
        saveAssertEquals("Operational Condition Reason Map must be empty for AO RX", null, rxStatusRsp.getStatusChoice().getStatusAO().getOperationalConditionReasonsMap());
        saveAssertEquals("Operational Condition Text must be empty for AO RX", null, rxStatusRsp.getStatusChoice().getStatusAO().getOperationalConditionText());


        /*
         * now verify a new Reset will clear the configuration signature
         */
        abisHelper.resetCommand(this.aoRxClass);
        rxStatusRsp = abisHelper.statusRequest(this.aoRxClass);
        saveAssertEquals("configuration signature be 0 after AO RX Status Request", 0, rxStatusRsp.getStatusChoice().getStatusAO().getConfigurationSignature());
    }
    
    
    /**
     * @name statusRequest for AO TS
     * 
     * @description Verifies the Status Request EP according to NodeUC478.N Case
     *              1: Status Request in MO State = RESET Case 2: Status Request
     *              in MO State = STARTED
     *              
     * @param testId - unique identifier
     * @param description
     * 
     * @throws InterruptedException
     */
    @Test (timeOut = 360000)
    @Parameters({ "testId", "description" })
    public void statusRequestAoTs(String testId, String description) throws InterruptedException {

    	// choose tsInstance (0-7) randomly
    	int seed = (int)System.currentTimeMillis();
    	int tsInstance = (seed < 0) ? ((-1)*seed) % 8 : seed % 8;
    	int associatedSoInstance = 0;

    	setTestCase(testId, description);

    	setTestStepBegin("Send Start Request to SO TRXC");
    	abisHelper.startRequest(this.soScfClass, 0);
        abisHelper.startRequest(this.soTrxcClass, 0);
    	setTestStepEnd();

    	setTestStepBegin("Verify AO TS Status Request, instance: " + tsInstance);
    	abisHelper.resetCommand(this.aoTsClass, tsInstance, associatedSoInstance);
    	OM_G31R01.StatusResponse tsStatusRsp = abisHelper.statusRequest(this.aoTsClass, tsInstance, associatedSoInstance);
    	saveAssertEquals("TS configuration signature must be 0", 0, tsStatusRsp.getStatusChoice().getStatusAO().getConfigurationSignature()); 
    	setTestStepEnd();
    	
    	/*
    	 * Configuration Request now to AO TS, to get a non-zero configuration signature
    	 */
    	setTestStepBegin("Configuration Request now to AO TS, to get a non-zero configuration signature");
    	abisHelper.startRequest(this.aoTsClass, tsInstance, associatedSoInstance);
    	setTestDebug("Random seed used for configuring TS: " + seed);
    	OM_G31R01.TSConfigurationResult tsConfRes = abisHelper.tsConfigRequest(tsInstance);
    	int tsConfigSignature = tsConfRes.getConfigurationSignature();
    	setTestStepEnd();
    	
    	/*
    	 * now verify that Status Request on AO TS will return the same configuration signature as was sent in Configuration Result
    	 */
    	setTestStepBegin("Verify that Status Request on AO TS will return the same configuration signature as was sent in Configuration Result");
    	tsStatusRsp = abisHelper.statusRequest(this.aoTsClass, tsInstance, associatedSoInstance);
    	setTestDebug("After start " + tsConfigSignature);
    	saveAssertEquals("configuration signature must not change after AO TS Status Request", tsConfigSignature, tsStatusRsp.getStatusChoice().getStatusAO().getConfigurationSignature()); 
    	saveAssertEquals("Operational Condition must be Operational for AO TS", OperationalCondition.Operational.getValue(), tsStatusRsp.getStatusChoice().getStatusAO().getOperationalCondition().getOperationalCondition().getValue());
    	saveAssertEquals("Operational Condition Reason Map must be empty for AO TS", null, tsStatusRsp.getStatusChoice().getStatusAO().getOperationalConditionReasonsMap());
    	saveAssertEquals("Operational Condition Text must be empty for AO TS", null, tsStatusRsp.getStatusChoice().getStatusAO().getOperationalConditionText());
    	setTestStepEnd();
    	
    	/*
    	 * now verify a new Reset will clear the configuration signature
    	 */
    	setTestStepBegin("Verify that a new AO TS Reset will clear the configuration signature");
    	abisHelper.resetCommand(this.aoTsClass, tsInstance, associatedSoInstance);
    	tsStatusRsp = abisHelper.statusRequest(this.aoTsClass, tsInstance, associatedSoInstance);
    	saveAssertEquals("configuration signature must be 0 after AO TS Status Request", 0, tsStatusRsp.getStatusChoice().getStatusAO().getConfigurationSignature());
    	setTestStepEnd();
    }
    
    /**
     * @name statusRequest for AO AT
     * 
     * @description Verifies the Status Request EP according to NodeUC478.N
     *              
     * @param testId - unique identifier
     * @param description
     * 
     * @throws InterruptedException
     */
    @Test (timeOut = 360000)
    @Parameters({ "testId", "description" })
    public void statusRequestAoAt(String testId, String description) throws InterruptedException {   
        setTestCase(testId, description);
        
        momHelper.waitForAbisScfOmlState("UP");
        saveAssertTrue("abisTrxcOmlState must be UP", momHelper.waitForAbisTrxcOmlState("UP"));
        
        //precondition: AT configured and enabled 
    	//save signatures for later comparisons
        OM_G31R01.StatusResponse statusResponse1 = abisHelper.statusRequest(aoAtClass);
        saveAssertEquals("MO State must be ENABLED", Enums.MOState.ENABLED, statusResponse1.getMOState());
        saveAssertEquals("OperationalCondition must be Operational", OM_G31R01.Enums.OperationalCondition.Operational.getValue(), statusResponse1.getStatusChoice().getStatusAO().getOperationalCondition().getOperationalCondition().getValue());
        int bundlingSignature1 = statusResponse1.getStatusChoice().getStatusAO().getBundlingSignature().intValue();
        int configSignature1   = statusResponse1.getStatusChoice().getStatusAO().getConfigurationSignature();
        saveAssertTrue("mismatch in Bundling Signature",      0 != bundlingSignature1);
        saveAssertTrue("mismatch in Configuration Signature", 0 != configSignature1);

        
        
        //checksAT Bundling Info Response contains same signatures as in AT Status Response
        OM_G31R01.ATBundlingInfoResponse bundlingInfoResponse1 = abisHelper.atBundlingInfoRequest(1, 0);
        saveAssertEquals("mismatch in Bundling Signature", bundlingSignature1, bundlingInfoResponse1.getBundlingSignature().intValue());
        saveAssertEquals("mismatch in Configuration Signature", configSignature1, bundlingInfoResponse1.getConfigurationSignature().intValue());

        //RESET AT
        abisHelper.resetCommand(aoAtClass);
        //signatures are reset in state RESET AT
        OM_G31R01.StatusResponse statusResponse2 = abisHelper.statusRequest(aoAtClass);
        saveAssertEquals("MO State must be RESET", Enums.MOState.RESET, statusResponse2.getMOState());
        saveAssertEquals("OperationalCondition must be Operational", OM_G31R01.Enums.OperationalCondition.Operational.getValue(), statusResponse2.getStatusChoice().getStatusAO().getOperationalCondition().getOperationalCondition().getValue());
        saveAssertEquals("Mismatch in Bundling Signature", 0, statusResponse2.getStatusChoice().getStatusAO().getBundlingSignature().intValue());
        saveAssertEquals("Mismatch in Configuration Signature", 0, statusResponse2.getStatusChoice().getStatusAO().getConfigurationSignature());
        saveAssertTrue("abisTrxcOmlState must be DOWN", momHelper.waitForAbisTrxcOmlState("DOWN"));

        //START AT
        abisHelper.startRequest(aoAtClass, 0);
        OM_G31R01.StatusResponse statusResponse3 = abisHelper.statusRequest(aoAtClass);
        saveAssertEquals("MO Class must be AT", OM_G31R01.Enums.MOClass.AT, statusResponse3.getMOClass());
        saveAssertEquals("MO Class must be AT", 0xFF, statusResponse3.getAssociatedSOInstance());
        saveAssertEquals("MO Class must be AT", 0, statusResponse3.getInstanceNumber());
        saveAssertEquals("MO State must be DISABLED", Enums.MOState.DISABLED, statusResponse3.getMOState());
        saveAssertEquals("OperationalCondition must be Operational", OM_G31R01.Enums.OperationalCondition.Operational.getValue(), statusResponse3.getStatusChoice().getStatusAO().getOperationalCondition().getOperationalCondition().getValue());
        saveAssertEquals("Bundling Signature must be 0", 0, statusResponse3.getStatusChoice().getStatusAO().getBundlingSignature().intValue());
        saveAssertEquals("Configuration Signature must be 0", 0, statusResponse3.getStatusChoice().getStatusAO().getConfigurationSignature());
        
        //CONFIGURE AT
        //@todo verify we get a different bundling signature when Abisco supports non-hardcoded AT configurations
        //abisHelper.atConfigRequest(0x11, 0x04, 0x0A, 0x33, 0x2E, 0x1D);
    	abisHelper.atConfigRequest(0x11, 0x06, 0x08, 0x33, 0x2E, 0x1D); //different configuration signature but same bundling signature

    	abisHelper.clearAtBundlingInfoUpdateQueue();
    	
    	//ENABLE AT
    	abisHelper.enableRequest(this.aoAtClass, 0);
    	
    	// the bundling signature is calculated when grat sends the bundling info update
    	BG.G31ATBundlingInfoUpdate atBundlingInfoUpdate = abisHelper.getAtBundlingInfoUpdate(5, TimeUnit.SECONDS);
    	saveAssertTrue("Did not receive any at bundling info update", null != atBundlingInfoUpdate);
    	saveAssertTrue("Bundling signature in AT bundling info update must not be 0", 0 != atBundlingInfoUpdate.getBundlingSignature().intValue());
    	
    	    	
        //Signatures must be != 0 and different from initial values
        OM_G31R01.StatusResponse statusResponse4 = abisHelper.statusRequest(aoAtClass);
        saveAssertEquals("MO State must be ENABLED", Enums.MOState.ENABLED, statusResponse4.getMOState());
        saveAssertEquals("OperationalCondition must be Operational", OM_G31R01.Enums.OperationalCondition.Operational.getValue(), statusResponse4.getStatusChoice().getStatusAO().getOperationalCondition().getOperationalCondition().getValue());
        //@todo verify we get a different bundling signature when Abisco supports non-hardcoded AT configurations
        //saveAssertTrue("Mismatch in Bundling Signature", bundlingSignature1 != statusResponse4.getStatusChoice().getStatusAO().getBundlingSignature().intValue());
        saveAssertTrue("Mismatch in Configuration Signature", configSignature1 != statusResponse4.getStatusChoice().getStatusAO().getConfigurationSignature());
        saveAssertTrue("Bundling Signature must be the same as in at bundling info update", statusResponse4.getStatusChoice().getStatusAO().getBundlingSignature().intValue() == atBundlingInfoUpdate.getBundlingSignature().intValue());
        saveAssertTrue("Configuration Signature must be != 0", statusResponse4.getStatusChoice().getStatusAO().getConfigurationSignature() != 0);
        
        abisPrePost.establishLinks(true);
        saveAssertTrue("abisTrxcOmlState must be UP", momHelper.waitForAbisTrxcOmlState("UP"));
    }
    
    /**
     * @name statusRequest for AO AT when SO SCF is not started
     * 
     * @description Verifies the Status Request EP according to NodeUC479.E2 Case
     *              
     * @param testId - unique identifier
     * @param description
     * 
     * @throws InterruptedException
     */
    @Test (timeOut = 360000)
    @Parameters({ "testId", "description" })
    public void statusRequestAoAt_SoScfNotStarted(String testId, String description) throws InterruptedException {   
        setTestCase(testId, description);
     
        setTestStepBegin("Verify AO AT Status request when SO SCF is not in state STARTED");
        abisHelper.resetCommand(soScfClass);
   
        OM_G31R01.StatusReject statusReject = null;
    	
    	try {
    	    abisHelper.statusRequest(aoAtClass);
    		fail("Received a Status Response, but a Status Reject was expected");
    	} catch (OM_G31R01.StatusRejectException e) {
    		setTestStepBegin("********** Got the expected StatusRejectException");
    		statusReject = e.getStatusReject();
    	}
    	
		assertEquals("Unexpected result code in the reject message", OM_G31R01.Enums.ResultCode.WrongState,	statusReject.getResultCode());
    	setTestStepEnd();
    }
}
