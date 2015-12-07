package com.ericsson.msran.test.grat.testhelpers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;

import com.ericsson.commonlibrary.remotecli.Cli;
import com.ericsson.commonlibrary.resourcemanager.g2.G2Rbs;
import com.ericsson.commonlibrary.resourcemanager.Rm;

/**
 * @name PidHandler
 * 
 * @author Markus Hedvall (xmahedv)
 * 
 * @created 2013-11-20
 * 
 * @description This class handles processes and process identifiers (PID). It
 *              can retrieve PIDs by process name and can kill a process by a
 *              given PID. The class uses CLI session to a RBS to handle the
 *              PIDs.
 * 
 * @revision xmahedv 2013-11-20 First version.
 * @revision ewegans 2013-02-03 Moved methods from GratHelper to PidHandler
 */
public class PidHandler {
    private static Logger logger = Logger.getLogger(PidHandler.class);

    private static final String LINE_SEPERATOR = System.getProperty("line.separator");

    Cli mCli;

    /**
     * Constructs a new PID handler for the first available RBS found.
     */
    public PidHandler() {
        G2Rbs rbs = Rm.getG2RbsList().get(0);
        mCli = rbs.getLinuxShell();
    }

    /**
     * Constructs a new PID handler for the provided RBS.
     * 
     * @param rbs the RBS which PIDs to handle.
     */
    public PidHandler(G2Rbs rbs) {
        mCli = rbs.getLinuxShell();
    }

    /**
     * Constructs a new PID handler using the provided CLI session to handle
     * PIDs.
     * 
     * @param cliSession the CLI session.
     */
    public PidHandler(Cli cliSession) {
        mCli = cliSession;
    }

    /**
     * Retrieves the first found PID for a given process name.
     * 
     * @param name the process name to search for PIDs for.
     * @return the first found PID for the process name. If no PID is found 0 is
     *         returned.
     */
    public int getPid(String name) {
        int[] pids = getPids(name);

        if (pids.length > 0) {
            return pids[0];
        }

        return 0;
    }

    /**
     * Retrieves all the PIDs associated to a given process name.
     * 
     * @param name the process name to search for PIDs for.
     * @return A list for all the found PIDs for the process name.
     */
    public int[] getPids(String name) {
        Pattern pattern = Pattern.compile("^\\s*(\\d+)\\s+");
        String psOutput = mCli.send("ps -e | grep " + name);
        String[] pidStringList = psOutput.split(LINE_SEPERATOR);

        List<Integer> pids = new ArrayList<Integer>();
        for (String pidString : pidStringList) {
            Matcher matcher = pattern.matcher(pidString);
            while (matcher.find()) {
                int pid = Integer.parseInt(matcher.group(1));
                pids.add(pid);
            }
        }

        // Convert the List<Integer> to int[] and return it.
        return ArrayUtils.toPrimitive(pids.toArray(new Integer[pids.size()]));
    }

    /**
     * Kills a process with a specific PID.
     * 
     * @param pid the PID of the process to kill.
     */
    public void killPid(int pid) {
        mCli.send("kill -9 " + pid);
    }

    /**
     * Makes sure our processes of interest are running and save their pids for
     * later comparison
     * 
     * @param pids A String Array containing the names of the processes we want
     *            to check.
     * @return A map with process names and their corresponding pids.
     */
    public Map<String, List<Integer>> checkAndSavePidsMap(String[] pids) {
        Map<String, List<Integer>> pidHm = new HashMap<String, List<Integer>>();
        for (String pStr : pids) {
            pidHm.put(pStr, getProcessRunningList(pStr));
        }
        return pidHm;
    }

    /**
     * Makes sure our processes of interest haven't crashed.
     * 
     * @param pids The Map containing processes of interest together with the
     *            pids they had previously
     * @return true if the processes are the same, false otherwise.
     */
    public boolean postCheckPidsMap(Map<String, List<Integer>> pids) {
        Iterator<String> pidIt = pids.keySet().iterator();
        boolean noChange = true;
        while (pidIt.hasNext()) {
            String pStr = pidIt.next();

            Collection<Integer> oldList = pids.get(pStr);
            Collection<Integer> currentList = getProcessRunningList(pStr);

            // if number of pids differs, we have crashed and didn't restart yet
            if (oldList.size() > currentList.size()) {
                logger.warn("number of pid(s) have changed for: " + pStr
                        + " has probably crashed during test.");
                noChange = false;
            }

            // discard elements from oldlist which are NOT in currentlist and if
            // list changes,
            // there are differences in pids before and after test, which
            // indicates our
            // process(es) have restarted.
            if (oldList.retainAll(currentList)) {
                logger.warn("pid(s) have changed for: " + pStr
                        + ", has probably crashed and restarted during test.");
                noChange = false;
            }
        }
        return noChange;
    }

    /**
     * Get the pids of a process name.
     * 
     * @param pName The process name
     * @return a list of pids, will be empty if no processes are found.
     */
    public List<Integer> getProcessRunningList(String pName) {
        int result = 0;
        List<Integer> aResult = new ArrayList<Integer>();
        Pattern pattern = Pattern.compile("^\\s*(\\d+)\\s+");
        String psOut = mCli.send("ps -e | grep " + pName);
        String strList[] = psOut.split("\n");
        for (String p : strList) {
            Matcher matcher = pattern.matcher(p);
            while (matcher.find()) {
                result = Integer.parseInt(matcher.group(1));
                aResult.add(result);
                // logger.info("pid  =\"" + result + "\"");
            }
        }
        return aResult;
    }

    /**
     * Verify a process is running.
     * 
     * @param pName The process name
     * @return true when found, false if not found
     */
    public boolean isProcessRunning(String pName) {
        return !getProcessRunningList(pName).isEmpty();
    }
}
