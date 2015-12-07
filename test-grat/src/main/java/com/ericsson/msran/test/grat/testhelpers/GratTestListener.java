package com.ericsson.msran.test.grat.testhelpers;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import com.ericsson.commonlibrary.remotecli.Cli;
import com.ericsson.commonlibrary.resourcemanager.Rm;

public class GratTestListener implements ITestListener {
	// These traces do not appear in the log for each test case, but should be seen in console.log 
    private static Logger logger = Logger.getLogger(GratTestListener.class);

	@Override
	public void onTestStart(ITestResult result) {
		try {
			// Clear log before each test case
			Cli cli = Rm.getG2RbsList().get(0).getLinuxShell();
			cli.send("gtrace -t * -d -c");
			cli.send("gtrace -r -t *"); // Make sure that signal traces are enabled
		} catch (Exception e) {
		    ; //The node is probably down, ignore exception
		}
		

	}

	@Override
	public void onTestSuccess(ITestResult result) {
		logger.info(result.getName() + " passed!");
	}

	@Override
	public void onTestFailure(ITestResult result) {
		logger.info(result.getName() + " failed!");
		try {
			// Output all the gtrace logs to file
			Cli cli = Rm.getG2RbsList().get(0).getLinuxShell();
			cli.setSendTimeoutMillis(30000);
			cli.send("gtrace -t * -esi");
			
			DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
			String timeString = dateFormat.format(new Date());
			
			cli.send("mv /rcs/applicationlogs/GRAT_CXP9023458_3/gtrace_esi.gdlh1.txt /rcs/applicationlogs/GRAT_CXP9023458_3/" + result.getName() + "_" + timeString + "_gdlh1.txt");
			cli.send("mv /rcs/applicationlogs/GRAT_CXP9023458_3/gtrace_esi.goam1.txt /rcs/applicationlogs/GRAT_CXP9023458_3/" + result.getName() + "_" + timeString + "_goam1.txt");
			cli.send("mv /rcs/applicationlogs/GRAT_CXP9023458_3/gtrace_esi.gsc1.txt /rcs/applicationlogs/GRAT_CXP9023458_3/" + result.getName() + "_" + timeString + "_gsc1.txt");
			cli.send("mv /rcs/applicationlogs/GRAT_CXP9023458_3/gtrace_esi.gte14.txt /rcs/applicationlogs/GRAT_CXP9023458_3/" + result.getName() + "_" + timeString + "_gte14.txt");
			cli.send("mv /rcs/applicationlogs/GRAT_CXP9023458_3/gtrace_esi.gte15.txt /rcs/applicationlogs/GRAT_CXP9023458_3/" + result.getName() + "_" + timeString + "_gte15.txt");
			cli.send("mv /rcs/applicationlogs/GRAT_CXP9023458_3/gtrace_esi.gte16.txt /rcs/applicationlogs/GRAT_CXP9023458_3/" + result.getName() + "_" + timeString + "_gte16.txt");
			cli.send("mv /rcs/applicationlogs/GRAT_CXP9023458_3/gtrace_esi.gte17.txt /rcs/applicationlogs/GRAT_CXP9023458_3/" + result.getName() + "_" + timeString + "_gte17.txt");
			cli.send("mv /rcs/applicationlogs/GRAT_CXP9023458_3/gtrace_esi.gte16.txt /rcs/applicationlogs/GRAT_CXP9023458_3/" + result.getName() + "_" + timeString + "_gte16.txt");
			cli.send("mv /rcs/applicationlogs/GRAT_CXP9023458_3/gtrace_esi.gte17.txt /rcs/applicationlogs/GRAT_CXP9023458_3/" + result.getName() + "_" + timeString + "_gte17.txt");
			cli.send("mv /rcs/applicationlogs/GRAT_CXP9023458_3/gtrace_esi.gte18.txt /rcs/applicationlogs/GRAT_CXP9023458_3/" + result.getName() + "_" + timeString + "_gte18.txt");
			cli.send("mv /rcs/applicationlogs/GRAT_CXP9023458_3/gtrace_esi.gte19.txt /rcs/applicationlogs/GRAT_CXP9023458_3/" + result.getName() + "_" + timeString + "_gte19.txt");
			cli.send("mv /rcs/applicationlogs/GRAT_CXP9023458_3/gtrace_esi.gte110.txt /rcs/applicationlogs/GRAT_CXP9023458_3/" + result.getName() + "_" + timeString + "_gte110.txt");
			cli.send("mv /rcs/applicationlogs/GRAT_CXP9023458_3/gtrace_esi.gte111.txt /rcs/applicationlogs/GRAT_CXP9023458_3/" + result.getName() + "_" + timeString + "_gte111.txt");
		} catch (Exception e) {
			; //Just to be safe if something happens
		}
	}

	@Override
	public void onTestSkipped(ITestResult result) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStart(ITestContext context) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onFinish(ITestContext context) {
		// TODO Auto-generated method stub

	}

}
