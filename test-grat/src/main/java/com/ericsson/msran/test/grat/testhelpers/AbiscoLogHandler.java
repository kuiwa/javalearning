package com.ericsson.msran.test.grat.testhelpers;

import java.io.File;

import org.apache.log4j.Logger;

import se.ericsson.jcat.fw.utils.TestInfo;

import com.ericsson.commonlibrary.remotecli.CliFactory;
import com.ericsson.commonlibrary.remotecli.Scp;
import com.ericsson.commonlibrary.remotecli.exceptions.ScpException;
import com.ericsson.commonlibrary.resourcemanager.Rm;
import com.ericsson.msran.jcat.TestBase;

/**
 * @name AbiscoLogHandler
 *
 * @author GRAT 2015
 *       
 * @created 2015-10-22
 * 
 * @description This class helps to retrieve the logs from Abisco per test case and diff beteween test cases.
 *     
 */

    
public class AbiscoLogHandler extends TestBase {
	private static Logger logger = Logger.getLogger(LogHandler.class);
    
    private String nodeName;
    private String logPath;
    private String abiscoHostname = "abiscochsrv1.rnd.ki.sw.ericsson.se";
    private String abiscoUsername = "gratci";
    private String abiscoPassword = "RoadR-14";
    private String localPath;// 	  = "/proj/wcdma_rbs/ewamagn/tmp/";
    private Scp scp;
    
    public AbiscoLogHandler()
    {
    	//Save the node name
    	nodeName=Rm.getG2RbsList().get(0).getName();
    	//WILL ONLY WORK ON KISTA NODES DUE TO THE KI_ PREFIX TODO: EWAMAGN 2015-10-22
    	logPath = "/home/gratci/commandhandler/stps/KI_" + nodeName.toUpperCase() + "/log/";
    	scp = CliFactory.newScp(abiscoHostname, abiscoUsername, abiscoPassword);
    	localPath = TestInfo.getLogDir() + File.separator;
    }
    
    public void saveAbiscoLogFull()
    {

    	for(int i = 0; i < 5; i++)
    	{
    		String savePath = localPath+"Abisco_"+ i +".log";
    		
    		String htmlLink = "";
    			
    		try {
    			saveAbiscoLogToPath(savePath, i); 
        		// Search for btreport in path name
        		if(savePath.contains("btreport"))
        			savePath  = "https://rbs-g2.rnd.ki.sw.ericsson.se" + savePath;
        		
        		htmlLink = "<a href=\""+savePath +"\">Abisco log " + i + "</a>";
    		}
    		catch (ScpException e)
    		{
    			htmlLink = "Abisco log " + i;
    		}
    		
    		

    		setAdditionalResultInfo(htmlLink);
    	}
    }
    
    public void saveAbiscoLogToPath(String savePath, int logIndex) throws ScpException
    {
    	String fullPath = logPath + "Abisco_"+logIndex+".log";
    	logger.info("Trying to read from:" + fullPath);  
        scp.connect();
        logger.info("SFTP connected? " + scp.isConnected());
    	
    	scp.get(fullPath, savePath);
    	
    	scp.disconnect();

        //logger.error("ERROR!!!" + scp.isConnected());   	
    }
    
    public void createAbiscoLogWaypoint()
    {
    	saveAbiscoLogToPath(localPath+"waypoint", 0);
    }
    
    public String createAbiscoLogDiff()
    {
    	//1. Check that a waypoint file exists
    	
    	//2. Save the current
    	saveAbiscoLogToPath(localPath+"new_waypoint", 0);
    	
    	//3. Calculate the name of the next abiscoDiff-file index based on the number of abiscoDiffs that already exists
    	int noDiffFiles = 0;
    	
    	File dir = new File(localPath);
    	File[] directoryListing = dir.listFiles();
    	if (directoryListing != null) {
    	  for (File child : directoryListing) {
    		  
    		  //Check if file contains the name abiscoDiff
    		  if (child.getName().contains("abiscoDiff"))
    		  {
    			  noDiffFiles++;
    		  }
    	  }
    	}
    	
    	//2. 
    	//Diff and save to file
    	//cli.setSendTimeoutMillis(30000);
		//String command ="awk 'FNR==NR{old[$0];next};!($0 in old)' /proj/wcdma_rbs/ewamagn/tmp/waypoint /proj/wcdma_rbs/ewamagn/tmp/new_waypoint > /proj/wcdma_rbs/ewamagn/tmp/1";
		
    	String abiscoLogFilename = localPath+"abiscoDiff_"+noDiffFiles+".log";
    	String command ="comm -13 "+localPath+"waypoint "+localPath+"new_waypoint > "+abiscoLogFilename;
		
		//TODO: Make more robust, with timeouts etc, shoudln't throw exceptions!
		CliFactory.newLocalBuilder().enableNewConnectOnEverySend().build().send(command);
		//cli.send(command);
		
    	//Rename waypoint 
		return abiscoLogFilename;
    	
    }
}
