package com.ericsson.msran.test.grat.testhelpers;

import org.apache.log4j.Logger;

import com.ericsson.commonlibrary.remotecli.Cli;
import com.ericsson.commonlibrary.resourcemanager.g2.G2Rbs;
import com.ericsson.commonlibrary.resourcemanager.Rm;

import com.ericsson.msran.test.grat.testhelpers.NodeStatusHelper;
import com.ericsson.msran.test.grat.testhelpers.MomHelper;

/**
 * @name CliCommands
 * 
 * @author GRAT 2014
 * 
 * @created 2014-02-04
 * 
 * @description This class contains methods for issuing various commands to the
 *              CLI. Methods for working with the TE log are found in the
 *              {@link LogHandler} class. Methods for working with processes are
 *              found in the {@link PidHandler} class.
 */
public class CliCommands {
    private static Logger logger = Logger.getLogger(CliCommands.class);
    private NodeStatusHelper nsh;
    Cli cli;
    MomHelper momhelper;

    /**
     * Constructor, finds the RBS and starts a CLI session towards it.
     */
    public CliCommands() {
        cli = Rm.getG2RbsList().get(0).getLinuxShell();
        nsh = new NodeStatusHelper();
        momhelper = new MomHelper();
    }

    /**
     * Constructor, creates a CLI session towards the provided RBS.
     * 
     * @param rbs The RBS
     */
    public CliCommands(G2Rbs rbs) {
        cli = rbs.getLinuxShell();
        nsh = new NodeStatusHelper();
        momhelper = new MomHelper();
    }

    /**
     * Constructor, will use the provided CLI session.
     * 
     * @param cliSess
     */
    public CliCommands(Cli cliSess) {
        cli = cliSess;
        nsh = new NodeStatusHelper();
        momhelper = new MomHelper();
    }

    /**
     * Reboot the system
     * 
     */
//    public void reboot() {
//        try {
//            cli.send("reboot");
//            logger.info("System Reboot \n");
//        } catch (Exception e) {
//            logger.info("System reboot not SET");
//        }
//    }


    /**
     * Reboot the system and wait for the whole node to come up again
     * 
     */
    public boolean rebootAndWaitForNode() {
    	return rebootAndWaitForNodeTime(140);
    }

    /**
     * Reboot the system and wait for the whole node to come up again
     * 
     */
    public boolean rebootAndWaitForNodeTime(int secondsToWaitBeforePoll) {
    	momhelper.restartDu();
    	// wait bofore polling
    	logger.info("Waiting " + secondsToWaitBeforePoll + " seconds before checking node status.");
    	try {
    		Thread.sleep(secondsToWaitBeforePoll * 1000);
    	} catch (InterruptedException ie) {
    		logger.info("Sleep got interrupted");
    	}
        if (nsh.waitForNodeReady())
        	logger.info("Node is up and running.");
        else {
        	logger.warn("Node is NOT up and running!!!");
        	return false;
        }
        return true;
    }

    /**
     * Reboot the system and wait for the whole node to come up again
     * 
     */
    public boolean rebootAndWaitForNodeNoCleanup() {
    	int secondsToWaitBeforePoll = 120;
    	momhelper.restartDu();
    	// wait bofore polling
    	logger.info("Waiting " + secondsToWaitBeforePoll + " seconds before checking node status.");
    	try {
    		Thread.sleep(secondsToWaitBeforePoll * 1000);
    	} catch (InterruptedException ie) {
    		logger.info("Sleep got interrupted");
    	}
        if (nsh.waitForNodeReadyNoCleanUp())
        	logger.info("Node is up and running.");
        else {
        	logger.warn("Node is NOT up and running!!!");
        	return false;
        }
        return true;
    }

    /**
     * Method to crash specified BaseBand & wait for the Dump to download in the
     * memory for 10 sec. This is temporary solution till we get support from
     * TAC team 14th Aug 2013 //elokban
     * 
     * @param gcpuband states the GCPU from which the BBlogs taken Ex:-
     *            gcpu00256
     */
    public void baseBandCrash(String gcpuband) {
        logger.info("Colish START \n\n\n");
        try {
            cli.send("colish");
        } catch (Exception e) {
            logger.info("Colish command NOT executed \n\n\n");
        }
        if (gcpuband.equals("gcpu00256")) {
            cli.send("gratBbCrash 0");
        } else if (gcpuband.equals("gcpu00512")) {
            cli.send("gratBbCrash 1");
        } else if (gcpuband.equals("gcpu00768")) {
            cli.send("gratBbCrash 2");
        } else if (gcpuband.equals("gcpu01024")) {
            cli.send("gratBbCrash 3");
        }
        cli.send("exit");
        try {
            Thread.sleep(60000);
        } catch (InterruptedException ie) {
            logger.info("Sleep got interrupted");
        }
    }

    /**
     * This method is used to check if the File is present in the specified
     * Folder/Path This is temporary Solution Till we get support from TAC team
     * 14th Aug 2013 //elokban
     * 
     * @param file Filename
     * @return true if the file was found, false otherwise
     */
    public boolean fileChecker(String file) {
        String info = cli.send("ls " + file);
        if (info.contains("No such file or directory")) {
            logger.info(file + " NOT FOUND \n\n");
            return false;
        } else {
            logger.info(file + " FOUND \n\n");
            return true;
        }
    }

    /**
     * This method is used to delete files on node
     * 
     * @param file Filename with full path
     */
    public void deleteFiles(String file) {
        cli.send("rm -f " + file);
    }
    
    /**
     * Run a command on cli.
     * 
     * @param str The string which holds the command to run
     * @return String with stdout result from command
     */
    public String cliCommand(String str) {
        return cli.send(str);
    }
    
    public int getDownlinkCarrierPower() {
    	cli.connect();
    	String result = cli.send("lhsh BXP_0 carrierListHandler print");
    	String lines[] = result.split("\n");

    	try {
    		String carrierPowerParts[] = lines[13].split(":");

    		if (!carrierPowerParts[0].contains("carrierPower")) {
    			return 0;
    		}

    		return Integer.parseInt(carrierPowerParts[1].trim());
    	} catch (IndexOutOfBoundsException e) {
    		return 0;
    	} catch (NumberFormatException e) {
    	}
    	return 0;
    }


}


