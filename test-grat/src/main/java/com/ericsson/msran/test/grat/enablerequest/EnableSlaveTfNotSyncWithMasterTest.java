package com.ericsson.msran.test.grat.enablerequest;

import org.json.JSONException;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;

import se.ericsson.jcat.fw.annotations.Setup;
import se.ericsson.jcat.fw.annotations.Teardown;

import com.ericsson.abisco.clientlib.servers.BG.G31OperationalCondition;
import com.ericsson.abisco.clientlib.servers.BG.G31StatusAO;
import com.ericsson.abisco.clientlib.servers.OM_G31R01;

import com.ericsson.abisco.clientlib.servers.BG.G31StatusUpdate;
import com.ericsson.abisco.clientlib.servers.OM_G31R01.Enums;
import com.ericsson.commonlibrary.remotecli.Cli;
import com.ericsson.commonlibrary.resourcemanager.Rm;
import com.ericsson.commonlibrary.resourcemanager.g2.G2Rbs;
import com.ericsson.msran.jcat.TestBase;
import com.ericsson.msran.test.grat.testhelpers.AbisHelper;
import com.ericsson.msran.test.grat.testhelpers.MomHelper;
import com.ericsson.msran.test.grat.testhelpers.NodeStatusHelper;
import com.ericsson.msran.test.grat.testhelpers.prepost.AbisPrePostWithTwoSectors;

/**
 * @id WP4510
 * 
 * @name EnableSlaveTfNotSyncWithMasterTest
 * 
 * @created 2015-06-01
 * 
 * @description This test class verifies the eATC ecBFN relation synchronization between master TF and slave TF
 * 
 * @revision 2015-06-01 first version 
 * @revision 2015-07-02 use random drift 
 */
public class EnableSlaveTfNotSyncWithMasterTest extends TestBase {
	private AbisPrePostWithTwoSectors abisPrePostWithTwoSectors;
	private AbisHelper abisHelper;
    private MomHelper momHelper;
	private NodeStatusHelper nodeStatus;
	private G2Rbs rbs;
	
	private final OM_G31R01.Enums.MOClass moClassTf = OM_G31R01.Enums.MOClass.TF;
 
	private final int clusterGroupId = 1231;
	private final String clusterGroupIdString = "1231";
	
    Cli cli;
    
    @Setup
    public void setup() throws InterruptedException, JSONException {
    	setTestStepBegin("Setup");
        nodeStatus = new NodeStatusHelper();
        assertTrue("Node did not reach a working state before test start", nodeStatus.waitForNodeReady());

        rbs = Rm.getG2RbsList().get(0);
        cli = rbs.getLinuxShell();
        cli.send("gtrace -t gsc1 -s -g -d -c");
        
        momHelper = new MomHelper();
        abisHelper = new AbisHelper();

        //prepare two sectors with abisIp and Trx Mo, start SCF and enable AT
        abisPrePostWithTwoSectors = new AbisPrePostWithTwoSectors();
        abisPrePostWithTwoSectors.preCondAllMoStarted();
        setTestStepEnd();
    }
    
    @Teardown
    public void teardown() {
    	setTestStepBegin("Teardown");
        try {
			abisHelper.resetCommand(moClassTf,AbisPrePostWithTwoSectors.tg_0, 0, 0, 255);
			abisHelper.resetCommand(moClassTf,AbisPrePostWithTwoSectors.tg_1, 0, 0, 255);
		} catch (InterruptedException e) {
			setTestInfo("failed to reset aoTf!");
			e.printStackTrace();
		}
        
        nodeStatus.isNodeRunning();
        setTestStepEnd();
    }
    
    @Test (timeOut = 100000)
    @Parameters({ "testId", "description" })
    public void enableSlaveTfNotSyncWithMaster(String testId, String description) throws InterruptedException {
    	
    	setTestCase(testId, description);  	

    	/**
    	 * prepare a Slave TF
    	 * TF configuration request parameters:
    	 * tfResultControl = ResultImmediately(0)
    	 * clusterGroupId = $clusterGroupId
    	 */
    	setTestStepBegin("prepare a Slave TF");
    	OM_G31R01.TFConfigurationResult confRes = abisHelper.slaveTfConfigRequest(AbisPrePostWithTwoSectors.tg_1, clusterGroupId);
    	
    	setTestInfo("Verify TF configuration result is received from TF");
    	assertEquals("Not from TF", this.moClassTf, confRes.getMOClass());
    	
        setTestInfo("Verify AccordanceIndication in configuration result for SLAVE is Data_According_To_Request");
        assertEquals("AccodanceIndication in configuration result is not AccordingToRequest", 
        		Enums.AccordanceIndication.AccordingToRequest, confRes.getAccordanceIndication().getAccordanceIndication());
        
        setTestInfo("Verify MO:GsmSector=1 attribute abisClusterGroupId");
        assertTrue("MO " + AbisPrePostWithTwoSectors.gsmSectorLdn1 + " abisClusterGroupId is not " + clusterGroupIdString,
        		momHelper.waitForMoAttributeStringValue(AbisPrePostWithTwoSectors.gsmSectorLdn1, "abisClusterGroupId", clusterGroupIdString, 6));
        
        setTestInfo("Verify MO:GsmSector=1 attribute abisTfMode");
        assertTrue("MO " + AbisPrePostWithTwoSectors.gsmSectorLdn1 + " abisTfMode is not SLAVE",
        		momHelper.waitForMoAttributeStringValue(AbisPrePostWithTwoSectors.gsmSectorLdn1, "abisTfMode", "SLAVE", 6));
    	setTestStepEnd();    	
    	
    	/**
    	 * enable Slave TF which not sync with master, it will become not operational
    	 */
        setTestStepBegin("Enable Slave TF");
        abisHelper.clearStatusUpdate();
        OM_G31R01.EnableResult enbRes = abisHelper.enableRequest(moClassTf, AbisPrePostWithTwoSectors.tg_1);
        
        setTestInfo("Verify that MO State for SLAVE in Enable Result is ENABLED");
        assertEquals("MO State in Enable Result is not ENABLED", OM_G31R01.Enums.MOState.ENABLED, enbRes.getMOState());

        setTestInfo("Verify that MO:GsmSector=1 attribute:abisTfMoState is ENABLED");
        assertTrue("MO " + AbisPrePostWithTwoSectors.gsmSectorLdn1 + " abisTfState is not ENABLED", 
                momHelper.waitForMoAttributeStringValue(AbisPrePostWithTwoSectors.gsmSectorLdn1, "abisTfState", "ENABLED", 6));
        
        setTestInfo("check abis status updated after slave TF enabled");

        G31StatusUpdate statusUpdate;
        boolean aoStatusUpdateFound = false;
        do {
          statusUpdate = abisHelper.getStatusUpdate(5, TimeUnit.SECONDS);
          if (statusUpdate != null && statusUpdate.getRouting().getTG() == AbisPrePostWithTwoSectors.tg_1) {
        	  G31StatusAO aoStatusUpdate = statusUpdate.getStatusChoice().getG31StatusAO();
        	  if (aoStatusUpdate != null) {
        		  G31OperationalCondition operationalCondition = aoStatusUpdate.getG31OperationalCondition();
        		  setTestInfo("get AoStatusUpdate " + operationalCondition.getOperationalCondition().toString() 
        				  +" " +aoStatusUpdate.getOperationalConditionText());
        		  
        		  assertEquals("G31 Stauts AO Operatinal Condition is not as expected",
        				  Enums.OperationalCondition.NotOperational.getValue(),
        				  operationalCondition.getOperationalCondition().getValue());
        		  assertEquals("G31 Stauts AO Operatinal Condition TEXT is not as expected",
        				  "No contact with Master TF", 
        				  aoStatusUpdate.getOperationalConditionText());
        		  aoStatusUpdateFound = true;
        		  break;
        		}
        	}
        }while (statusUpdate != null);
        assertTrue(aoStatusUpdateFound);
        setTestStepEnd();
        
        abisHelper.clearStatusUpdate();
        
    	/**
    	 * prepare a Master TF
    	 * TF configuration request parameters:
    	 * tfResultControl = ResultImmediately(0)
    	 * clusterGroupId = $clusterGroupId
    	 * tfMode = MASTER(0)
    	 * fsOffset = undefined
    	 */
    	setTestStepBegin("prepare a Master TF");
    	OM_G31R01.FSOffset fsOffset = new OM_G31R01.FSOffset(new Integer(0xFF), new Long(0xFFFFFFFFL));
    	confRes = abisHelper.tfConfigRequest(AbisPrePostWithTwoSectors.tg_0, clusterGroupId, Enums.TFMode.Master, fsOffset);
    	
    	setTestInfo("Verify TF configuration result is received from TF");
    	assertEquals("Not from TF", this.moClassTf, confRes.getMOClass());
        setTestInfo("Verify AccordanceIndication in configuration result for MASTER is Data_According_To_Request");
        assertEquals("AccodanceIndication in configuration result is not AccordingToRequest", 
        		Enums.AccordanceIndication.AccordingToRequest, confRes.getAccordanceIndication().getAccordanceIndication());
        setTestInfo("Verify MO:GsmSector=0 attribute abisClusterGroupId");
        assertTrue("MO " + AbisPrePostWithTwoSectors.gsmSectorLdn0 + " abisClusterGroupId is not " + clusterGroupIdString,
        		momHelper.waitForMoAttributeStringValue(AbisPrePostWithTwoSectors.gsmSectorLdn0, "abisClusterGroupId", clusterGroupIdString, 6));
        setTestInfo("Verify MO:GsmSector=0 attribute abisTfMode");
        assertTrue("MO " + AbisPrePostWithTwoSectors.gsmSectorLdn0 + " abisTfMode is not MASTER",
        		momHelper.waitForMoAttributeStringValue(AbisPrePostWithTwoSectors.gsmSectorLdn0, "abisTfMode", "MASTER", 6));
    	setTestStepEnd();
        
    	/**
    	 * slave TF will become operational
    	 */
        setTestInfo("check abis status updated for slave TF");
        aoStatusUpdateFound = false;
        do {
            statusUpdate = abisHelper.getStatusUpdate(5, TimeUnit.SECONDS);
            if (statusUpdate != null && statusUpdate.getRouting().getTG() == AbisPrePostWithTwoSectors.tg_1) {
              G31StatusAO aoStatusUpdate = statusUpdate.getStatusChoice().getG31StatusAO();
              if (aoStatusUpdate != null) {
                G31OperationalCondition operationalCondition = aoStatusUpdate.getG31OperationalCondition();
                setTestInfo("get AoStatusUpdate " + operationalCondition.getOperationalCondition().toString() +" " 
                + aoStatusUpdate.getOperationalConditionText());                
                                
                assertEquals("G31 Stauts AO Operatinal Condition is not as expected",
              		Enums.OperationalCondition.Operational.getValue(),
                      operationalCondition.getOperationalCondition().getValue());
                aoStatusUpdateFound = true;
                break; 
              }
            }
        }while (statusUpdate != null);
        assertTrue(aoStatusUpdateFound);

        setTestStepEnd();
    }
}
