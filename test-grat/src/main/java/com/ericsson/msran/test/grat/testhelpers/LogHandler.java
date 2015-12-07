package com.ericsson.msran.test.grat.testhelpers;

import java.io.File;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ericsson.msran.g2.utils.FileUtil;
import com.ericsson.msran.g2.utils.StringUtil;

import com.ericsson.commonlibrary.remotecli.Cli;
import com.ericsson.commonlibrary.resourcemanager.g2.G2Rbs;
import com.ericsson.commonlibrary.resourcemanager.Rm;

/**
 * @name LogHandler
 *
 * @author GRAT 2014
 *       
 * @created 2014-02-04
 * 
 * @description This class helps use the TE log. Methods are available for
 *    reading, clearing, and setting trace groups for the log.
 *     
 */
public class LogHandler {
    private static Logger logger = Logger.getLogger(LogHandler.class);
    private static final String LINE_SEPARATOR = "line.separator";
    Cli cli;
    
    /**
     * Constructor that find the RBS and starts a CLI session
     * towards it.
     */
    public LogHandler()
    {
        cli = Rm.getG2RbsList().get(0).getLinuxShell();
    }
    
    /**
     * Constructor for TeLogHelper, start a CLI session
     * towards the provided rbs.
     * @param rbs The RBS
     */
    public LogHandler(G2Rbs rbs)
    {
        cli = rbs.getLinuxShell();
    }
    
    /**
     * Construction for TeLogHelper, use the provided CLI session.
     * @param cliSess The CLI session
     */
    public LogHandler(Cli cliSess)
    {
        cli = cliSess;
    }
  
    
    /**
     * Clear te log.
     */
    public void clearTeLog()
    {
        cli.send("te log clear");
    } 
    
    /**
     * Clear tex log. TODO REMOVE ME WHEN TRI MIGRATION DONE
     */
    public void clearTexLog()
    {
        cli.send("tex log clear");
    } 

    /**
     * Enable trace.
     * 
     * @param traceGroup The trace group to be enabled.
     * @param processName The process name that the trace group is activated for.
     */
    public void teEnable(String traceGroup, String processName)
    {
        cli.send("te e " + traceGroup + " " + processName);
    }
    
    /**
     * Enable trace. TODO REMOVE ME WHEN TRI MIGRATION DONE
     * 
     * @param traceGroup The trace group to be enabled.
     * @param processName The process name that the trace group is activated for.
     */
    public void texEnable(String traceGroup, String processName)
    {
        cli.send("tex e " + traceGroup + " " + processName);
    }  


    /**
     * Disable trace.
     * 
     * @param traceGroup The trace group to be enabled.
     * @param processName The process name that the trace group is activated for.
     */
    public void teDisable(String traceGroup, String processName)
    {
        cli.send("te disable " + traceGroup + " " + processName);
    }

    /**
     * Disable trace.
     * 
     * @param traceGroup The trace group to be enabled. TODO REMOVE ME WHEN TRI MIGRATION DONE
     * @param processName The process name that the trace group is activated for.
     */
    public void texDisable(String traceGroup, String processName)
    {
        cli.send("tex disable " + traceGroup + " " + processName);
    }
    
    /**
     * Save enabled traces for a process/trace object
     * 
     * @param processName The process name/trace object whose enabled traces are to be saved.
     */
    public void teSave(String processName)
    {
        cli.send("te save " + processName);
    }
    
    /**
     * Save enabled traces for a process/trace object. TODO REMOVE ME WHEN TRI MIGRATION DONE
     * 
     * @param processName The process name/trace object whose enabled traces are to be saved.
     */
    public void texSave(String processName)
    {
        cli.send("tex save " + processName);
    }

    /**
     * Revert to default trace groups for a process/trace object
     * 
     * @param processName The process name/trace object whose default traces are to be restored.
     */
    public void teDefault(String processName)
    {
        cli.send("te default " + processName);
    }
    
    /**
     * Revert to default trace groups for a process/trace object. TODO REMOVE ME WHEN TRI MIGRATION DONE
     * 
     * @param processName The process name/trace object whose default traces are to be restored.
     */
    public void texDefault(String processName)
    {
        cli.send("tex default " + processName);
    }

    /**
     * Get the te log.
     * 
     * @return String, the whole te log in a single string
     */
    public String getTeLog()
    {
        String teLog = cli.send("te log read");
        return teLog;
    }
    
    /**
     * Get the te log through TRI, should be used if the result from
     * getTeLog() does not contain the expected result.
     * 
     * @return the TRI log as a String
     * TODO: remove this method when TRI migration is complete
     */
    public String getTexLog()
    {
        logger.info("Using tex log read");
        return cli.send("tex log read");
    }
    
    
    /**
     * Read the trace and error log from the node with "te log read" and store the output in the log directory.
     * @param rbsName The name of the RBS
     */
    public void readAndStoreTeLog(String rbsName){
        String teLog = getTeLog();
        String teFilePath = FileUtil.writeFileWithTesstcaseId( "te_log_file_pre", rbsName, teLog);
        if (!teLog.isEmpty()) {
            File teFile = new File(teFilePath);
            logger.info("TE log saved : "+StringUtil.createHtmlReportFileLink(se.ericsson.jcat.fw.utils.TestInfo.getLogDir(),teFile.getAbsolutePath(), teFile.getName()));
        }           
    }
    

    
    /**
     * Get the te log as a list.
     * 
     * @return List of lines in te log
     */
    public String[] getTeLogList()
    {
        String teLog = getTeLog();
        String strList [] = teLog.split("\n");
        return strList;
    }
    
    /**
     * Try to find a string in the te log.
     * 
     * @param str string to find in te log
     * @return true if string exists in te log
     */
    public boolean teLogContains(String str)
    {
        String teLog = getTeLog();
        boolean contains = teLog.contains(str);
        if (!contains) { //TODO: remove this when TRI migration is complete
            teLog = getTexLog();
            contains = teLog.contains(str);
        }
        return contains;
    }   
    
    /**
     * Clear the log
     */
    public void logClear()
    {
        try{
            cli.send("te log clear");
            logger.info("Log Clear \n");    
        }
        catch(Exception e){
            logger.info("Didnt clear the Log"); 
        }
     }
    
    /**
     * Get the "Number Of Occurrences" of given String from te log.
     * 
     * @param str string to find in te log
     * @return true if string exists in te log
     */
    public int teLogCountMatch(String str)
    {
        String teLog = getTeLog();
        logger.info("Te Log Read: "+teLog);
        int matches = StringUtils.countMatches(teLog, str);
        if (matches == 0) { // use tex log read instead
            teLog = getTexLog(); //TODO: remove this when TRI migration is complete
            return StringUtils.countMatches(teLog, str);
        } else {
            return matches;
        }
    }
    
    /**
     * @brief returns the number of occurrences of a given regex in the T&E log
     * @param processName name of the process that produces the trace output
     * @param text text to search for
     * @return The number of matches
     */
    public int teLogCountRegExMatch(String processName, String text)
    {
        String teLog = getTeLog();
        String dateAndTimeRegex = "\\[\\d{4}-\\d{2}-\\d{2}\\s\\d{2}\\:\\d{2}\\:\\d{2}\\.\\d{3}\\]\\s";
        //this is the line where the given text appears
        //newline + "[YYYY-MM-DD hh:mm:ss.millis] " + <ProcessName> + space + <FileName> + "." + <FileExtension> + ":" + <LineNumber> + space + <TraceGroup> (zero or more alphanumerics) + ":" + <text> + space (zero or more) + newline
        String regexLine = "\\n" + dateAndTimeRegex + processName + "\\s\\w+\\.\\w+\\:\\d+\\s\\w*\\:?" + text + "\\s*\\n";
        Pattern pattern = Pattern.compile(regexLine);
        Matcher matcher = pattern.matcher(teLog);
        int occurences = 0;
        while (matcher.find())
        {
            occurences++;
        }
        if (occurences == 0)
        {
            //TODO: replace this method with texLogCountRegExMatch when TRI migration is completed.
            return texLogCountRegExMatch(processName, text);
        }
        return occurences;
    }
    
    /**
     * Same as teLogCountRegExMatch, but check the tex log, and has different regexes
     * @param processName
     * @param text
     * @return
     */
    public int texLogCountRegExMatch(String processName, String text) {
    	String texLog = getTexLog();

    	//String regexLine = "\\{ process = \"\\*\\/GOAM_TIME_SYNCH\", fileAndLine = \"GoamTimeSynch.cc:[d+]\", msg = \"NC_TDC_TIME_LOCKING_ABILITY_IND received\" \\}";
    	//[2014-04-12 17:09:55.114459316] (+0.000012950) du1 com_ericsson_trithread:INFO: { cpu_id = 0 }, { process = "*/GOAM_TIME_SYNCH", fileAndLine = "GoamTimeSynch.cc:598", msg = "FrequencyRef: INVALID, TimeRef: INVALID, TimeOffset: 0" }

    	//String regexLine = "\\n.*?{ process = \"" + processName + "\", fileAndLine = .*?, msg = \"" + text + "\" }\\n";
    	//String regexLine = "{ process = \"" + processName + "\", fileAndLine = \"\\w+\\.\\w+:\\d+\", msg = \"" + text + "\" }";

    	String regexLine = text;
    	Pattern pattern = Pattern.compile(regexLine);
        Matcher matcher = pattern.matcher(texLog);
        int occurences = 0;
        while (matcher.find())
        {
            occurences++;
        }
        return occurences;
    }
    
    /**
     * Get the BB telog.
     * This is temporary Solution Till we get support from TAC team 24th July 2013  //elokban
     * 
     * @param gcpuband  states the GCPU from which the BBlogs taken Ex:- gcpu00256
     * 
     * @return String, the whole telog in a single string
     */
    public String getBBTeLog(String gcpuband)
    {
        String bbteLog = cli.send("$ITCLNHDIR/bin/lhsh "+ gcpuband +" te log read");
        return bbteLog;
    }
    
    
    
    /**
     * Read the trace and error log from the node with "BB te Log read " and store the output in the log directory.
     * This is temporary Solution Till we get support from TAC team 24th July 2013  //elokban
     * @param rbsName The name of the RBS
     * @param gcpuband states the GCPU from which the BBlogs taken Ex:- gcpu00256
     * 
     */
    public void readAndStoreBBTeLog(String rbsName, String gcpuband){
        cli.setSendTimeoutMillis(58000);
        String teLog = cli.send("$ITCLNHDIR/bin/lhsh " + gcpuband +" te log read");
        String teFilePath = FileUtil.writeFileWithTesstcaseId( "te_log_file_pre", rbsName, teLog);
        if (!teLog.isEmpty()) {
            File teFile = new File(teFilePath);
            logger.info("BB TE log saved : " + StringUtil.createHtmlReportFileLink(se.ericsson.jcat.fw.utils.TestInfo.getLogDir(),teFile.getAbsolutePath(), teFile.getName()));
        }           
     }
    
    /**
     * Return the lines containing the given char sequence.
     * 
     * @param teLog T&E log String to search in.
     * @param sequence Sequence to search for.
     * @return Array of string matching sequence
     */
    public String[] linesContaining(String teLog, CharSequence sequence) {
        String[] allLines = teLog.split(System.getProperty(LINE_SEPARATOR));
        ArrayList<String> lines = new ArrayList<String>();
        for (int i = 0; i < allLines.length; i++) {
            if (allLines[i].contains(sequence)) {
                lines.add(allLines[i]);
            }
        }
        return lines.toArray(new String[lines.size()]);
    }
}
