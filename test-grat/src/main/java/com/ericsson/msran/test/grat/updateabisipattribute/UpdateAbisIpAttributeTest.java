package com.ericsson.msran.test.grat.updateabisipattribute;

import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import se.ericsson.jcat.fw.annotations.Setup;
import se.ericsson.jcat.fw.annotations.Teardown;

import com.ericsson.commonlibrary.ecimcom.exception.GracefulDisconnectFailedException;
import com.ericsson.commonlibrary.managedobjects.exception.ConnectionException;
import com.ericsson.msran.g2.annotations.TestInfo;
import com.ericsson.msran.test.grat.testhelpers.MomHelper;
import com.ericsson.msran.test.grat.testhelpers.NodeStatusHelper;
import com.ericsson.msran.jcat.TestBase;


/**
 * @id NodeUC525
 *  
 * @name UpdateAbisIpAttributeTest
 * 
 * @author GRAT 2014
 * 
 * @created 2014-03-19
 * 
 * @description Update values on the AbisIp MO.
 * 
 * @revision ewegans 2014-03-19 first version
 * 
 */
public class UpdateAbisIpAttributeTest extends TestBase {
    private MomHelper momHelper;
    private static String connectionName = "host_0";
    private static String bscIpAddress = "";
    private static String dscpSectorControlUl = "dscpSectorControlUL";
    private static String bscBrokerIpAddress = "bscBrokerIpAddress";
    private String abisIpLdn;
    private NodeStatusHelper nodeStatus;
    
    
    /**
     * Description of test case for test reporting
     */
    @TestInfo(
            tcId = "NodeUC525",
            slogan = "Update AbisIp attribute(s)",
            requirementDocument = "-",
            requirementRevision = "-",
            requirementLinkTested = "-",
            requirementLinkLatest = "-",
            requirementIds = { "-" },
            verificationStatement = "Verifies NodeUC525.N, NodeUC525.E1",
            testDescription = "Verifies update of AbisIp attribute values after creation",
            traceGuidelines = "N/A")
    
    /**
     * Precheck.
     */
    @Setup
    public void setup() {
    	setTestStepBegin("Setup");
        nodeStatus = new NodeStatusHelper();
        assertTrue("Node did not reach a working state before test start", nodeStatus.waitForNodeReady());
        setTestInfo("Save current pid to compare after test execution.");
        
        momHelper = new MomHelper();
        bscIpAddress = momHelper.randomPrivateIp();
        
    	if (!momHelper.isBtsFunctionCreated()) {
    		setTestInfo("Precondition: Create BtsFunction MO");
    		momHelper.createBtsFunctionMo(MomHelper.BTS_FUNCTION_LDN);
    	}
        
        abisIpLdn = momHelper.createSectorAndAbisIpMo(connectionName, bscIpAddress, false);
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
    
    private void updateAttribute(String attributeName, String newValue) throws ConnectionException {
        setTestStepBegin("Update attribute " + attributeName);
        String currentValue = momHelper.getAttributeValue(abisIpLdn, attributeName);
        setTestDebug(attributeName + " = " + currentValue);
        setTestDebug("New value = " + newValue);
        momHelper.setAttributeForMoAndCommit(abisIpLdn, attributeName, newValue);
        assertTrue("AbisIp MO attribute " + attributeName + " did not get new value: " + newValue,
                momHelper.waitForMoAttributeStringValue(abisIpLdn, attributeName, newValue, 5));
        setTestStepEnd();
    }
    
    /**
     * @name updateAbisIpAttributes
     * 
     * @description Update AbisIp attribute when LOCKED
     * 
     * @param testId - unique identifier of the test case
     * @param description
     *      
     */ 
    @Test(timeOut = 340000)
    @Parameters({ "testId", "description" })
    public void updateAbisIpAttributes(String testId, String description) {
        setTestCase(testId, description);

        setTestStepBegin("Pre-condition: AbisIp is LOCKED");
        assertTrue("AbisIp is not LOCKED", momHelper.waitForMoAttributeStringValue(abisIpLdn, "administrativeState", "LOCKED", 5));
        setTestStepEnd();

        updateAttribute(dscpSectorControlUl, "17");

        updateAttribute(bscBrokerIpAddress, "123.123.123.123");
    }
    
    /**
     * @name updateAbisIpAttributes
     * 
     * @description Update AbisIp attribute when UNLOCKED
     * 
     * @param testId - unique identifier of the test case
     * @param description
     *      
     */ 
    @Test(timeOut = 340000)
    @Parameters({ "testId", "description" })
    public void updateAbisIpAttributeWhenUnlocked(String testId, String description) {
        setTestCase(testId, description);

        setTestStepBegin("Pre-condition: AbisIp is UNLOCKED");
        momHelper.unlockMo(abisIpLdn);
        assertTrue("AbisIp is not UNLOCKED", momHelper.waitForMoAttributeStringValue(abisIpLdn, "administrativeState", "UNLOCKED", 5));
        setTestStepEnd();

        // Update of attributes should fail when unlocked
        setTestStepBegin("Try updating dscpSectorControlUl, should fail");
        String currentDscp = momHelper.getAttributeValue(abisIpLdn, dscpSectorControlUl);
        try {
            updateAttribute(dscpSectorControlUl, "17");
            fail(dscpSectorControlUl + " was updated");
        } catch (GracefulDisconnectFailedException e) {
            // Expected
        }
        assertEquals(momHelper.getAttributeValue(abisIpLdn, dscpSectorControlUl), currentDscp);
        setTestStepEnd();
        
        setTestStepBegin("Try updating bscBrokerIpAddress, should fail");
        String currentBscBrokerIp = momHelper.getAttributeValue(abisIpLdn, bscBrokerIpAddress);
        try {
            updateAttribute(bscBrokerIpAddress, "123.123.123.123");
            fail(bscBrokerIpAddress + " was updated");
        } catch (GracefulDisconnectFailedException e) {
            // Expected
        }
        assertEquals("BscBrokerIp doesn't match the address in MOM", momHelper.getAttributeValue(abisIpLdn, bscBrokerIpAddress), currentBscBrokerIp);
        setTestStepEnd();
    }
}
