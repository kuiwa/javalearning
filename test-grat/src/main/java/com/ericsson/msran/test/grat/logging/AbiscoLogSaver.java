package com.ericsson.msran.test.grat.logging;

import org.testng.annotations.Test;

import se.ericsson.jcat.fw.annotations.Setup;
//import com.ericsson.commonlibrary.ecimcom.netconf.NetconfManagedObjectHandler;
import com.ericsson.msran.jcat.TestBase;
import com.ericsson.msran.test.grat.testhelpers.AbiscoLogHandler;

/**
 
 * @name LockUnlockAbisIpTest
 * 
 * @author GRAT 2015
 * 
 * @created 2015-10-24
 * 
 * @description Saves the AbiscoLogs
 * 
 * @revision ewegans 2014-03-21 first version
 *           ewegans 2014-06-16 more thorough testing
 * 
 */
public class AbiscoLogSaver extends TestBase {
	private AbiscoLogHandler abiscoLogHandler;
   
    @Setup
    public void setup() {

        abiscoLogHandler = new AbiscoLogHandler();
    }
	
    @Test(timeOut = 60000)
    public void startAbiscoLogWaypoint() {
        abiscoLogHandler.createAbiscoLogWaypoint();
    }
    @Test(timeOut = 60000)
    public void stopAbiscoLogWaypoint() {
    	String abiscoDiffFilename = abiscoLogHandler.createAbiscoLogDiff();
    	
    	// Search for btreport in path name
    	if(abiscoDiffFilename.contains("btreport"))
    		abiscoDiffFilename = "https://rbs-g2.rnd.ki.sw.ericsson.se" + abiscoDiffFilename;
    	
    	String htmlLink = "<br><a href=\""+abiscoDiffFilename+"\">Test case Abisco log</a>";
    	setAdditionalResultInfo(htmlLink);
    }
    
    @Test(timeOut = 60000)
    public void saveAbiscoLogFull() {
    	
    	abiscoLogHandler.saveAbiscoLogFull();
    	
    	
    }
}
