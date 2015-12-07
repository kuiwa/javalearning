package com.ericsson.msran.test.grat.softwarecheck;

import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import se.ericsson.jcat.fw.annotations.Setup;
import se.ericsson.jcat.fw.annotations.Teardown;

import com.ericsson.commonlibrary.managedobjects.ManagedObjectHandler;
import com.ericsson.msran.g2.annotations.TestInfo;

import com.ericsson.msran.jcat.TestBase;
import com.ericsson.msran.test.grat.testhelpers.NodeStatusHelper;
import com.ericsson.commonlibrary.resourcemanager.g2.G2Rbs;
import com.ericsson.commonlibrary.resourcemanager.Rm;

/**
 * @id GRAT000001
 * 
 * @name SoftwareCheckTest
 * 
 * @author Lokeswara rao Bandaru (elokban)
 * 
 * @created 2013-03-22
 * 
 * @description Verifies that GRAT software is up and running.
 * 
 * @revision elokban 2013-03-19 first version
 * @revision xasahuh 2014-02-07 Set timeout for test case to 1 minute.
 * @revision xasahuh 2014-02-10 Added TestInfo metadata.
 * @revision xasahuh 2014-02-19 Removed builder pattern structure.
 * 
 */

public class SoftwareCheckTest extends TestBase {
    
    private G2Rbs rbs;
    private NodeStatusHelper nodeStatus;
    
    /**
     * Description of test case for test reporting
     */
    @TestInfo(
            tcId = "GRAT000001",
            slogan = "Software startup",
            requirementDocument = "-",
            requirementRevision = "-",
            requirementLinkTested = "-",
            requirementLinkLatest = "-",
            requirementIds = { "-" },
            verificationStatement = "-",
            testDescription = "Verifies that GRAT software is up and running.",
            traceGuidelines = "N/A")
    
    /**
     * Precheck.
     */
    @Setup
    public void setup() {
        nodeStatus = new NodeStatusHelper();
        assertTrue("Node did not reach a working state before test start", nodeStatus.waitForNodeReady());
    }
    
    /**
     * Postcheck.
     */
    @Teardown
    public void teardown() {
        nodeStatus.isNodeRunning();
    }
    
    /**
     * @name checkSoftware 
     * 
     * @param testId -  
     * @param description 
     * @param softwarePackage
     */
    @Test(timeOut = 360000)
    @Parameters({ "testId", "description", "softwarePackage" })
    public void checkSoftware(String testId, String description, String softwarePackage) {
        
        setTestCase(testId, description);
        
        rbs = Rm.getG2RbsList().get(0);
        ManagedObjectHandler moHandler = rbs.getManagedObjectHandler();
        
        String installedSoftware = moHandler.findManagedObjectChildren("ManagedElement=1,SystemFunctions=1,SwInventory=1").toString();
        
        setTestInfo("installedSoftware=" + installedSoftware);

        boolean swPackageFound = false;

            if (installedSoftware.contains(softwarePackage)) {
                setTestDebug("Found installed CXP " + softwarePackage);
                swPackageFound = true;
            }
        
        assertTrue(String.format(
                "Could not find software package %s",
                softwarePackage), swPackageFound);
    }
}
