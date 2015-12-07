package com.ericsson.msran.test.grat.abortspcommand;

import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import se.ericsson.jcat.fw.annotations.Setup;
import se.ericsson.jcat.fw.annotations.Teardown;

import com.ericsson.msran.jcat.TestBase;
import com.ericsson.msran.g2.annotations.TestInfo;
import com.ericsson.abisco.clientlib.servers.OM_G31R01;
import com.ericsson.abisco.clientlib.servers.OM_G31R01.Enums;
import com.ericsson.abisco.clientlib.servers.OM_G31R01.FSOffset;
import com.ericsson.msran.test.grat.testhelpers.AbisHelper;
import com.ericsson.msran.test.grat.testhelpers.MomHelper;
import com.ericsson.msran.test.grat.testhelpers.NodeStatusHelper;
import com.ericsson.msran.test.grat.testhelpers.prepost.AbisPrePost;
    

/**
 * @id NodeUC481
 * 
 * @name AbortSpCommandTest
 * 
 * @author Asa Huhtasaari (xasahuh)
 * 
 * @created 2014-02-03
 * 
 * @description This test class verifies the Abort SP Command:
 *              Abis downlink SP, completion after Abort SP Command.
 * 
 * @revision xasahuh 2014-02-03 First revision.
 * @revision xasahuh 2014-02-07 Set timeout for test case to 1 minute.
 * @revision xasahuh 2014-02-11 Removed AfterClass, the RestoreStack is used instead.
 * @revision xasahuh 2014-02-12 Added TestInfo metadata.
 */

public class AbortSpCommandTest extends TestBase {
    private final OM_G31R01.Enums.MOClass  moClassScf = OM_G31R01.Enums.MOClass.SCF;
    private final OM_G31R01.Enums.MOClass  moClassTf = OM_G31R01.Enums.MOClass.TF;
    
    private AbisPrePost abisPrePost;
    private AbisHelper abisHelper;
    private MomHelper momHelper;
    
    private final String GSM_SECTOR_LDN = "ManagedElement=1,BtsFunction=1,GsmSector=1";
    private NodeStatusHelper nodeStatus;

    /**
     * Description of test case for test reporting
     */
    @TestInfo(
            tcId = "NodeUC481",
            slogan = "Abort SP Command of Abis downlink SP",
            requirementDocument = "1/00651-FCP 130 1402",
            requirementRevision = "PC5",
            requirementLinkTested = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff87c1d941?docno=1/00651-FCP1301402Uen&format=excel8book",
            requirementLinkLatest = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff86a2af3d?docno=1/00651-FCP1301402Uen&action=current&format=excel8book",
            requirementIds = { "10565-0334/19417[A][APPR]" },
            verificationStatement = "Verifies NodeUC481.A2",
            testDescription = "Verifies Abort SP Command of Start SO SCF, Start AO TF, Enable AO TF and Disable AO TF.",
            traceGuidelines = "N/A")

    
    /**
     * Create MO:s, establish links to Abisco, and verify preconditions.
     */
    @Setup
    public void setup() {
        setTestStepBegin("Setup");
        nodeStatus = new NodeStatusHelper();
        assertTrue("Node did not reach a working state before test start", nodeStatus.waitForNodeReady());
        abisHelper = new AbisHelper();
        abisPrePost = new AbisPrePost();
        momHelper = new MomHelper();
        abisPrePost.preCondAllMoStateReset();
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
     * @name abortSpCommandTest
     * 
     * @description Verifies Abort SP Command of Start SO SCF, Start AO TF, 
     *              Enable AO TF and Disable AO TF according to NodeUC481.A2.
     * 
     * @param testId - unique identifier
     * @param description
     * 
     * @throws InterruptedException
     */
    @Test (timeOut = 360000)
    @Parameters({ "testId", "description" })
    public void abortSpCommandTest(String testId, String description) throws InterruptedException {
        
    	setTestCase(testId, description);

        /*
         * Case 1: Abort of Start SO SCF, completion after abort
         */
    	setTestStepBegin("Send Start SO SCF and Abort SP Command");        
        abisHelper.startRequest(this.moClassScf, 0);        
        abisHelper.abortSpCommand(this.moClassScf);
        setTestStepEnd();
        
        setTestStepBegin("SP Execution shall be completed, MO:GsmSector attribute:abisSoScfState is STARTED");
        assertTrue("MO " + GSM_SECTOR_LDN + " abisScfState is not STARTED", 
        		momHelper.waitForMoAttributeStringValue(GSM_SECTOR_LDN, "abisScfState", "STARTED", 6));
        setTestStepEnd();
        
        /*
         * Case 2: Abort of Start AO TF, completion after abort
         */
        setTestStepBegin("Send Start AO TF and Abort SP Command");
        abisHelper.startRequestAsync(this.moClassTf);
        abisHelper.abortSpCommand(this.moClassTf);
        setTestStepEnd();
        
        setTestStepBegin("SP Execution shall be completed, MO:GsmSector attribute:abisTfMoState is DISABLED");
        assertTrue("MO " + GSM_SECTOR_LDN + " abisTfState is not DISABLED", 
        		momHelper.waitForMoAttributeStringValue(GSM_SECTOR_LDN, "abisTfState", "DISABLED", 3));
        setTestStepEnd();
        
        /*
         * Case 3: Abort of Enable AO TF, completion after abort
         */
        abisHelper.tfConfigRequest(1, Enums.TFMode.Master, abisHelper.getOM_G31R01fsOffsetByActiveSyncSrc()); //config before enable
        setTestStepBegin("Send Enable AO TF and Abort SP Command");
        abisHelper.enableRequestAsync(this.moClassTf);
        abisHelper.abortSpCommand(this.moClassTf);
        setTestStepEnd();
        
        setTestStepBegin("SP Execution shall be completed, MO:GsmSector attribute:abisTfMoState is ENABLED");
        assertTrue("MO " + GSM_SECTOR_LDN + " abisTfState is not ENABLED", 
        		momHelper.waitForMoAttributeStringValue(GSM_SECTOR_LDN, "abisTfState", "ENABLED", 3));
        setTestStepEnd();
            
        /*
         * Case 4: Abort of Disable AO TF, completion after abort
         */
        setTestStepBegin("Send Disable AO TF and Abort SP Command");
        abisHelper.disableRequestAsync(this.moClassTf);
        abisHelper.abortSpCommand(this.moClassTf);
        setTestStepEnd();
        
        setTestStepBegin("SP Execution shall be completed, MO:GsmSector attribute:abisTfMoState is DISABLED");
        assertTrue("MO " + GSM_SECTOR_LDN + " abisTfState is not DISABLED", 
        		momHelper.waitForMoAttributeStringValue(GSM_SECTOR_LDN, "abisTfState", "DISABLED", 3));
        setTestStepEnd();
    }        
}

