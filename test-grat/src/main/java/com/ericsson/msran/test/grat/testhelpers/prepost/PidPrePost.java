package com.ericsson.msran.test.grat.testhelpers.prepost;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ericsson.msran.jcat.TestBase;
import com.ericsson.commonlibrary.resourcemanager.g2.G2Rbs;
import com.ericsson.commonlibrary.resourcemanager.Rm;

import com.ericsson.msran.test.grat.testhelpers.PidHandler;

/**
 * @name PidPrePost
 * 
 * @author Asa Huhtasaari (xasahuh)
 * 
 * @created 2014-02-21
 * 
 * @description Class for preconditions and postconditions for test cases that verifies
 *              that the pid:s of the GRAT are the same after the test execution as they
 *              were before.
 * 
 * @revision xasahuh 2014-02-21 First version.
 * 
 */
public class PidPrePost extends TestBase {
    
    private String[] pList = { "GOAM", "GSC", "GDLH", "GTE" };
    private PidHandler pidHandler;
    private Map<String, List<Integer>> pidHm;
    
    /**
     * Constructor to initialize the PID pre- and postcondition setup and check.
     */
    public PidPrePost() {
    }
    
    /**
     * @name preCond()
     * 
     * @description Store a list of the current GRAT PID:s
     */
    public void preCond() {
    	// Out commented because of a bug in Resource Manager requiring all paramaters to be set in beans-xml.
    	//setTestInfo("Contents of Rm.getG2RbsList() = " + Rm.getG2RbsList().toString());
        G2Rbs rbs = Rm.getG2RbsList().get(0);
        pidHandler = new PidHandler(rbs.getLinuxShell()); 
        pidHm = new HashMap<String, List<Integer>>();
        pidHm = pidHandler.checkAndSavePidsMap(this.pList);
    }
    
    /**
     * @name getPidMap()
     * 
     * @description Returns the pid Map.
     */
    public Map<String, List<Integer>> getPidMap() {
        return pidHm;
    }
    
    /**
     * @name postCond()
     * 
     * @description Compare the current GRAT PID:s with the list of saved PID:s from
     *              before the test execution.
     */
    public void postCond() {
        assertTrue("At least one process crashed during the test.", pidHandler.postCheckPidsMap(this.pidHm));
    }
}
