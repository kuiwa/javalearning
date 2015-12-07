/**
 * 
 */
package com.ericsson.msran.test.grat.configurationrequest;

import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import se.ericsson.jcat.fw.annotations.Setup;
import se.ericsson.jcat.fw.annotations.Teardown;

import com.ericsson.abisco.clientlib.servers.OM_G31R01;
import com.ericsson.abisco.clientlib.servers.BG.G31OperationalCondition;
import com.ericsson.abisco.clientlib.servers.BG.G31StatusAO;
import com.ericsson.abisco.clientlib.servers.BG.G31StatusUpdate;
import com.ericsson.abisco.clientlib.servers.OM_G31R01.Enums;
import com.ericsson.abisco.clientlib.servers.OM_G31R01.TFConfigurationResult;
import com.ericsson.msran.g2.annotations.TestInfo;
import com.ericsson.msran.jcat.TestBase;
import com.ericsson.msran.test.grat.testhelpers.AbisHelper;
import com.ericsson.msran.test.grat.testhelpers.MomHelper;
import com.ericsson.msran.test.grat.testhelpers.NodeStatusHelper;
import com.ericsson.msran.test.grat.testhelpers.prepost.AbisPrePostWithTwoSectors;

/**
 * @author ehunxie
 *
 */
public class ConfigurationRequestWithTwoSectorTest extends TestBase {

	private AbisPrePostWithTwoSectors abisPrePostWithTwoSectors;
	private AbisHelper abisHelper;
    private MomHelper momHelper;
	private NodeStatusHelper nodeStatus;
	
	private static Logger logger = Logger.getLogger(ConfigurationRequestWithTwoSectorTest.class);
	
	private final OM_G31R01.Enums.MOClass moClassTf = OM_G31R01.Enums.MOClass.TF;
	
	private final Integer abisClusterGroupId = 1;
    private final String radioEquipmentClockReferenceLdn = "ManagedElement=1,Transport=1,Synchronization=1,RadioEquipmentClock=1,RadioEquipmentClockReference=1";
    private final String timeSyncIoLdn = "ManagedElement=1,Transport=1,Synchronization=1,TimeSyncIO=1";
        
    /**
     * Description of test case for test reporting
     */
    @TestInfo(
            tcId = "UC512.A9",
            slogan = "Reselect of a new TF master",
            requirementDocument = "",
            requirementRevision = "WP4510",
            requirementLinkTested = "",
            requirementLinkLatest = "",
            requirementIds = { "" },
            verificationStatement = "Verify UC512.A9",
            testDescription = "Test abis configuration of reselection of a new master",
            traceGuidelines = "N/A")

    
    @Setup
    public void setup() throws InterruptedException, JSONException {
    	setTestStepBegin("Setup");
        nodeStatus = new NodeStatusHelper();
        assertTrue("Node did not reach a working state before test start", nodeStatus.waitForNodeReady());
        
        logger.debug("====== prepare sector MOM and TG =====");
        momHelper = new MomHelper();
        abisHelper = new AbisHelper();
        abisPrePostWithTwoSectors = new AbisPrePostWithTwoSectors();
        abisPrePostWithTwoSectors.preCondAllMoStarted();
        
        setTestInfo("Change from frequency to time sync to set timeLockingPossible to true");
        boolean isTimeSyncActive = momHelper.isTimeSyncActive();
        if(!isTimeSyncActive){
            // We cannot toggle sync configuration while using WA for CAT WP4689
//            logger.debug("====== prepare time sync MOM =====");
//            momHelper.deleteRadioEquipmentClockReferenceMo(radioEquipmentClockReferenceLdn);
//            momHelper.deleteFrequencySyncIoMo();
//            momHelper.createTimeSyncIoMo(timeSyncIoLdn);
//            momHelper.createRadioEquipmentClockReferenceMo(radioEquipmentClockReferenceLdn, timeSyncIoLdn);
//            isTimeSyncActive = true;
        }

        setTestStepEnd();
    }
    
    @Teardown
    public void teardown() {
    	setTestStepBegin("Teardown");
    	try {
        	logger.debug("====== teardown TF reset =====");
        	abisHelper.resetCommand(moClassTf, AbisPrePostWithTwoSectors.tg_1, 0, 0, 255);
			abisHelper.resetCommand(moClassTf, AbisPrePostWithTwoSectors.tg_0, 0, 0, 255);			
		} catch (InterruptedException e) {			
			logger.debug("teardown TF reset fail, exception:" + e.getMessage());
		}
    	
        nodeStatus.isNodeRunning();
        setTestStepEnd();
    }
    
    @Test (timeOut = 36000000)
    @Parameters({ "testId", "description" })
    public void reselectionTfMaster(String testId, String description) throws InterruptedException {
    	
    	setTestCase(testId, description);   	

    	
    	/**
    	 * Precondition: prepare a Master TF
    	 * TF configuration request parameters:
       	 * clusterGroupId = 1
    	 * tfMode = MASTER(0)
    	 * fsOffset = 0
    	 */
    	setTestStepBegin("Config a Master TF and enable it"); 
    	OM_G31R01.FSOffset fsOffset = new OM_G31R01.FSOffset();
    	OM_G31R01.TFConfigurationResult confRes1 = abisHelper.tfConfigRequest(AbisPrePostWithTwoSectors.tg_0, abisClusterGroupId, Enums.TFMode.Master, fsOffset);
    	assertEquals("Wrong MO class", moClassTf, confRes1.getMOClass());        
        assertTrue(momHelper.waitForMoAttributeStringValue(AbisPrePostWithTwoSectors.gsmSectorLdn0, "abisClusterGroupId", abisClusterGroupId.toString(), 6));
        assertTrue(momHelper.waitForMoAttributeStringValue(AbisPrePostWithTwoSectors.gsmSectorLdn0, "abisTfMode", "MASTER", 6));
    	    	   	
    	OM_G31R01.EnableResult enbRes1 = abisHelper.enableRequest(moClassTf, AbisPrePostWithTwoSectors.tg_0);
        assertEquals(OM_G31R01.Enums.MOState.ENABLED, enbRes1.getMOState());
        assertTrue(momHelper.waitForMoAttributeStringValue(AbisPrePostWithTwoSectors.gsmSectorLdn0, "abisTfState", "ENABLED", 6));
        setTestStepEnd();
    	
        /**
    	 * Precondition: prepare a Slave TF
    	 * TF configuration request parameters:
    	 * clusterGroupId = 1
    	 */
        
        
    	setTestStepBegin("Config a Slave TF and enable it");    	
    	
    	do 
    	{
    		OM_G31R01.TFConfigurationResult confRes2 = abisHelper.slaveTfConfigRequest(AbisPrePostWithTwoSectors.tg_1, abisClusterGroupId);
    		assertEquals("Wrong MO class", moClassTf, confRes2.getMOClass());
    		assertTrue(momHelper.waitForMoAttributeStringValue(AbisPrePostWithTwoSectors.gsmSectorLdn1, "abisClusterGroupId", abisClusterGroupId.toString(), 6));
    		assertTrue(momHelper.waitForMoAttributeStringValue(AbisPrePostWithTwoSectors.gsmSectorLdn1, "abisTfMode", "SLAVE", 6));

    		abisHelper.clearStatusUpdate();

    		OM_G31R01.EnableResult enbRes2 = abisHelper.enableRequest(moClassTf, AbisPrePostWithTwoSectors.tg_1);
    		assertEquals("MO state not ENABLED", OM_G31R01.Enums.MOState.ENABLED, enbRes2.getMOState());
    		assertTrue("abisTfState not ENABLED", momHelper.waitForMoAttributeStringValue(AbisPrePostWithTwoSectors.gsmSectorLdn1, "abisTfState", "ENABLED", 6));      
    	} while (checkForNoContactWithMasterStateUpdate(AbisPrePostWithTwoSectors.tg_1));

        setTestStepEnd();    
        
        
        

        
        /**
         * Case1: re-selection of Master, normal
         * 
         */
        setTestStepBegin("Reselection of Master, No Phase Jump");  
        TFConfigurationResult tfResult; 
        tfResult = abisHelper.tfConfigRequest(AbisPrePostWithTwoSectors.tg_1, 1, Enums.TFMode.Master, new OM_G31R01.FSOffset());
        logger.debug("Normal TFConfig:" + tfResult.toString());
        assertTrue(momHelper.waitForMoAttributeStringValue(AbisPrePostWithTwoSectors.gsmSectorLdn0, "abisTfMode", "SLAVE", 15));
        assertTrue(momHelper.waitForMoAttributeStringValue(AbisPrePostWithTwoSectors.gsmSectorLdn0, "abisTfState", "ENABLED", 15));
        assertTrue(momHelper.waitForMoAttributeStringValue(AbisPrePostWithTwoSectors.gsmSectorLdn1, "abisTfMode", "MASTER", 15));
        assertTrue(momHelper.waitForMoAttributeStringValue(AbisPrePostWithTwoSectors.gsmSectorLdn1, "abisTfState", "ENABLED", 15));        
        setTestStepEnd();   
                        
        // discard unconcerned status update
        abisHelper.clearStatusUpdate();                    
       
        //set tg_0 as master again
        abisHelper.tfConfigRequest(AbisPrePostWithTwoSectors.tg_0, 1, Enums.TFMode.Master, new OM_G31R01.FSOffset());
        abisHelper.clearStatusUpdate();

        /***
         * Case2: Reselection of Master, eATC Phase Jump
         */
        setTestStepBegin("Reselection of Master, With Phase Jump");                 
        abisHelper.tfConfigRequest(AbisPrePostWithTwoSectors.tg_1, 1, Enums.TFMode.Master, new OM_G31R01.FSOffset(0, 500L)); 
        assertTrue(statusUpdateMatcher(AbisPrePostWithTwoSectors.tg_1, Enums.OperationalCondition.NotOperational.getValue(), "Phase Jump Needed")); 
        
        sleepSeconds(5);
        assertTrue(statusUpdateMatcher(AbisPrePostWithTwoSectors.tg_1, Enums.MOState.RESET.getValue()));  
        
        // discard unconcerned status update
        abisHelper.clearStatusUpdate();        
               
        // start with eATC Jump
        abisHelper.startRequest(moClassTf, AbisPrePostWithTwoSectors.tg_1);
        abisHelper.tfConfigRequest(AbisPrePostWithTwoSectors.tg_1, 1, Enums.TFMode.Master, new OM_G31R01.FSOffset(0, 500L)); 
        assertTrue(statusUpdateMatcher(AbisPrePostWithTwoSectors.tg_0, Enums.OperationalCondition.NotOperational.getValue(), "Phase Jump Needed"));
        setTestStepEnd();
                
        abisHelper.clearStatusUpdate();
    }  
    
    private boolean checkForNoContactWithMasterStateUpdate(int tgId) throws InterruptedException {
    	// If Tf enable comes to quickly two extra status updates will be received 
    	G31StatusUpdate statusUpdate = abisHelper.getStatusUpdate(2, TimeUnit.SECONDS);
    	
    	if (statusUpdate != null) 
    	{
    		assertEquals(tgId, statusUpdate.getRouting().getTG().intValue());
    		assertTrue("ConditionText not as expected", statusUpdate.getStatusChoice().getG31StatusAO().getOperationalConditionText().equals("No contact with Master TF"));
    		statusUpdate = abisHelper.getStatusUpdate(10, TimeUnit.SECONDS);
    		assertEquals(tgId, statusUpdate.getRouting().getTG().intValue());
    		assertEquals(Enums.OperationalCondition.Operational.getValue(), 
    				statusUpdate.getStatusChoice().getG31StatusAO().getG31OperationalCondition().getOperationalCondition().getValue());
            abisHelper.startRequest(moClassTf, AbisPrePostWithTwoSectors.tg_1);
            
            return true;
    	}
    	
		return false;
	}

	private Boolean statusUpdateMatcher(int tgId, int opCond, String opText) throws InterruptedException
    {
    	G31StatusUpdate statusUpdate;
        G31StatusAO aoStatusUpdate;
        G31OperationalCondition operationalCondition;        
        while (null != (statusUpdate = abisHelper.getStatusUpdate(2, TimeUnit.SECONDS)))
        {
        	logger.debug("====== statusUpdateMatcherA:" + statusUpdate.toString());        	
        	aoStatusUpdate = statusUpdate.getStatusChoice().getG31StatusAO();            
        	operationalCondition = statusUpdate.getStatusChoice().getG31StatusAO().getG31OperationalCondition();
        	if ((tgId == statusUpdate.getRouting().getTG().intValue()) &&
        		(opCond == operationalCondition.getOperationalCondition().getValue()) &&
        		(opText.equals(aoStatusUpdate.getOperationalConditionText())))
        	{
        		return true;
        	}
        }      
            
    	return false;
    }
    
    private Boolean statusUpdateMatcher(int tgId, int moState) throws InterruptedException
    {
    	G31StatusUpdate statusUpdate;    	
    	while (null != (statusUpdate = abisHelper.getStatusUpdate(2, TimeUnit.SECONDS)))
        {        	
    		logger.debug("====== statusUpdateMatcherB:" + statusUpdate.toString());
        	if ((tgId == statusUpdate.getRouting().getTG().intValue()) &&
        		(moState == statusUpdate.getMOState().getValue()))
        	{
        		return true;
        	}
        }     
    	
    	return false;
    }
    
}
