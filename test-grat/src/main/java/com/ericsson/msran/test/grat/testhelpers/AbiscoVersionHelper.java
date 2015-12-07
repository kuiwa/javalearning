package com.ericsson.msran.test.grat.testhelpers;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.ericsson.abisco.clientlib.AbiscoClient;
import com.ericsson.abisco.clientlib.CommunicationErrorException;
import com.ericsson.abisco.clientlib.VersionMismatchException;
import com.ericsson.commonlibrary.remotecli.Cli;
import com.ericsson.commonlibrary.remotecli.exceptions.ReadTimeoutException;
import com.ericsson.commonlibrary.resourcemanager.ResourceConfigurationException;
import com.ericsson.commonlibrary.resourcemanager.Rm;

/**
 * @name AbiscoVersionHelper
 * @author Nikos Karassavas (enikoka)
 * @created 2015-10-02
 * @description This class contains ....
 */
public class AbiscoVersionHelper {

    private static Logger log = Logger.getLogger(AbiscoVersionHelper.class);
    private String versionRunning;
    private String stpName;
    private Cli cli = null;
    private Map<String, String> versionMap = null;
    private AbiscoClient abiscoClient = null;
    private boolean isAbiscoConnectable = false;

    public AbiscoVersionHelper()
    {
        stpName = Rm.id();
        try {
            cli = Rm.getAbiscoList().get(0).getSshCli();
        } catch (ResourceConfigurationException rce) { // probably Windows CH
            log.info("AbiscoConnection - ResourceConfigurationException: " + rce.getMessage());
            if (rce.getMessage().contains("linuxUsername for AbiscoResource has not been configured")){
                log.info("AbiscoConnection - AbiscoResource bean was not read");
            }
            log.info("AbiscoConnection - Trying to get Windows or Linux version CH");
            internalGetAbiscoClient();
        }
        
        if (connect()){ // If Linux and connectable
            disconnect();
            isAbiscoConnectable = true;
            if (abiscoClient == null) { // If we are not able to connect over CLI, it is probably a Windows version used
                log.info("AbiscoConnection - Trying to get Linux version CH");
                internalGetAbiscoClient();
            }

            if (abiscoClient == null) {                 // If LATEST was started
                log.info("AbiscoConnection - Trying to get correct Linux version CH");
                internalGetAbiscoClient();
            }
        }

        if (abiscoClient == null) { // If still nothing, one last try with no exception handling
            log.info("AbiscoConnection - Fetching abiscoClient from RM");
            abiscoClient = Rm.getAbiscoList().get(0).getAbiscoClient();
        }
        
    }

    private void internalGetAbiscoClient() {
        try {
            abiscoClient = Rm.getAbiscoList().get(0).getAbiscoClient();
        } catch (CommunicationErrorException cee) { // Abisco is probably not running
            log.info(cee.getCause());
            // cee.printStackTrace();
            if (isAbiscoConnectable){
                startLatestAbisco(); // since we can't get clientlib version we start LATEST
                try {
                    abiscoClient = Rm.getAbiscoList().get(0).getAbiscoClient();
                } catch (Exception e) { // Abisco is probably not running
                    log.info("Failed getting abiscoClient: " + e.getMessage());
                }
            }
        } catch (VersionMismatchException vme) { // Wrong version is running
            log.info(vme.getCause());
            if(versionMap == null){
                parseVersionMismatchException(vme);
                log.debug("AbiscoConnection - clientlib  = " + versionMap.get("clientlib") + ", ch_version = " + versionMap.get("ch_version"));
            }

            if (isAbiscoConnectable){
                restartAbisco(); // since we can't get clientlib version we start LATEST
                try {
                    abiscoClient = Rm.getAbiscoList().get(0).getAbiscoClient();
                } catch (Exception e) { // Abisco is probably not running
                    log.info("Failed getting abiscoClient: " + e.getMessage());
                }
            }
        }
    }

    public AbiscoClient getAbiscoClient() {
        return abiscoClient;
    }

    /**
     * Restart the same version of Abisco.
     */
    public void restartSameVersion() {
    	// Get current version
    	String currentAbiscoVersion = getCurrentVersionRunning();
    	
    	// Stop Abisco
    	stopRunningVersion();
    	
    	// Start Abisco
    	startVersion(currentAbiscoVersion);
    }
    
    private String getCurrentVersionRunning() {
        cli.connect();

        versionRunning = cli.send("stp_status.pl --version --stp " + stpName.toUpperCase());
        log.debug("versionRunning: " + versionRunning);

        cli.disconnect();
        return versionRunning;
    }

    private boolean startLatestAbisco() {
        return startVersion("LATEST");
    }

    /**
     * Try and restart correct version of Abisco. This will only work if it is run
     * on Linux host.
     * 
     * @return true if correct version was started, otherwise false
     */
    private boolean restartAbisco() {
        if (versionMap == null) {
            return false;
        }

        String g2ClientlibRevision;
        g2ClientlibRevision = versionMap.get("clientlib");
        log.info("Attempting to start Abisco version " + g2ClientlibRevision +
                " for STP " + stpName);

        String oldVersionRunning = getCurrentVersionRunning();
        log.debug("oldVersionRunning: " + oldVersionRunning);

        stopRunningVersion();

        startVersion(versionMap.get("clientlib"));

        String currVersionRunning = getCurrentVersionRunning();
        log.debug("versionRunning: " + currVersionRunning);

        if (currVersionRunning.contains("NO_VERSION_FOUND")) { // failed starting correct version, try restoring previous
            log.error("Failed starting version \"" + g2ClientlibRevision + "\", trying to restart previous "
                    + oldVersionRunning);
            cli.send("start_stp.pl --version " + oldVersionRunning + " --stp " + stpName);
            disconnect();
            return false;
        }
        if (!currVersionRunning.equals(g2ClientlibRevision)) { // Just to catch cases where versions fall out of pattern above
            log.error("Wrong version started??? \"" + currVersionRunning + "\"");
            disconnect();
            return false;
        }

        versionRunning = currVersionRunning;
        return true;
    }

    private boolean startVersion(String version) {
        connect();
        try {
            cli.send("start_stp.pl --version " + version + " --stp " + stpName);
        } catch (ReadTimeoutException rtoe) { // It happens that it cannot find prompt in cluttered output
            disconnect();
            log.debug("startVersion() - Caught ReadTimeoutException (" + rtoe.getCause() + ")" +
            		", Sleep for 5 seconds before checking that it is up and running");
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                log.debug("startVersion() - Sleep was interrupted");
            }
            if (getCurrentVersionRunning().equals(version)) { // start command succeeded
                return true;
            }
            else {
                return false;
            }
        }
        log.debug("startVersion() - version started: " + version);

        disconnect();
        return true;
    }

    private void stopRunningVersion() {
        connect();
        String stopResponse = cli.send("stop_stp.pl --stp " + stpName);
        log.debug("stopRunningVersion() - stopResponse: " + stopResponse);
        try {
            log.debug("stopRunningVersion() - Sleep for 5 seconds before disconnect");
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            log.debug("stopRunningVersion() - Sleep was interrupted");
        }
        disconnect();
    }

    private boolean connect() {
        try {
            cli.connect();
        } catch (Exception e) {
            log.warn("Failed connecting/login to Abisco host, maybe Windows version is used by this STP?:\n"
                    + e.getMessage());
            return false;
        }
        return true;
    }

    private void disconnect() {
        cli.disconnect();
    }

    private void parseVersionMismatchException(VersionMismatchException vme) {
        String message = vme.getMessage();
        versionMap = new HashMap<String, String>();
        Pattern vmeMessagePattern = Pattern
                .compile("Abisco clientlib is version (R\\d+\\D+)_\\d+ and CH is version (R\\d+\\D+)_\\d+");
        Matcher vmeMessageMatcher = vmeMessagePattern.matcher(message);

        if (vmeMessageMatcher.matches()) {
            versionMap.put("clientlib", vmeMessageMatcher.group(1));
            versionMap.put("ch_version", vmeMessageMatcher.group(2));
        }
        else {
            log.error("Failed parsing VersionMismatchException");
        }
    }

}
