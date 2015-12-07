package com.ericsson.msran.test.grat.testhelpers;


import java.util.List;

import org.apache.log4j.Logger;

import com.ericsson.commonlibrary.ecimcom.netconf.NetconfManagedObjectHandler;
import com.ericsson.commonlibrary.remotecli.Cli;
import com.ericsson.commonlibrary.resourcemanager.exception.ResourceManagerConnectionException;
import com.ericsson.commonlibrary.resourcemanager.Rm;
import com.ericsson.commonlibrary.resourcemanager.g2.G2Rbs;
import com.ericsson.commonlibrary.remotecli.exceptions.ConnectionToServerException;
import com.ericsson.commonlibrary.managedobjects.ManagedObject;
import com.ericsson.commonlibrary.managedobjects.ManagedObjectAttribute;
import com.ericsson.commonlibrary.managedobjects.OperationMode;
import com.ericsson.commonlibrary.managedobjects.exception.ConnectionException;
import com.ericsson.commonlibrary.managedobjects.exception.OperationFailedException;
import com.ericsson.msran.jcat.TestBase;

/**
 * The NodeStatusHelper is used to check that the node being used is in a usable state.
 * It should be used at the beginning of a test (e.g. in setup()), but can also check
 * for dumps at the end of the test.
 */
public class NodeStatusHelper {
	protected static final int NODE_SHUTDOWN_TIME = 20000;
	protected static final int SSH_RETRY_LOOP_DELAY = 2000;
	protected static final int NETCONF_RETRY_LOOP_DELAY = 10000;
	protected static final int SECTOR_RETRY_LOOP_DELAY = 10000;
	protected static final int GRAT_RETRY_LOOP_DELAY = 5000;
	private final String PMD_DUMPS_MAP = "/rcs/dumps";
	private final String DSP_DUMPS_MAP = "/rcs/applicationlogs/GRAT_*/dspdumps";
	private final static String FRU1_LDN = "ManagedElement=1,Equipment=1,FieldReplaceableUnit=1";
	private final static String BTS_FUNC_LDN = MomHelper.BTS_FUNCTION_LDN;
	private final static String NODE_SUPPORT_LDN = "ManagedElement=1,NodeSupport=1";
    private final static TestBase testBase = new TestBase(); // Used for JCAT HTML log, NOT verdicts
    
	private G2Rbs rbs = null;
    private Logger logger;
    private NetconfManagedObjectHandler moHandler = null;
    private Cli cli = null;


	/**
	 * Constructor
	 */
	public NodeStatusHelper(){
        logger = Logger.getLogger(NodeStatusHelper.class);
        rbs = Rm.getG2RbsList().get(0);
        cli = rbs.getLinuxShell();
	}
	
	private boolean isSshUp(){
		try {
			cli.connect();
		} catch (ConnectionToServerException ce) {
			return false;
		}
		cli.disconnect();
		return true;
	}
	
	/**
	 * Try to open an ssh session to the node. Will wait and retry if unsuccessful.
	 * @return true if ssh was established, false otherwise
	 */
	public boolean waitForSsh() {
	    for (int i = 0; i < 20; ++i) {
	        if (isSshUp()) return true;
	        logger.info("Ssh is not up, sleep for " + SSH_RETRY_LOOP_DELAY + " ms");
	        try {
                Thread.sleep(SSH_RETRY_LOOP_DELAY);
	        } catch (InterruptedException e) {
	            logger.info("Sleep was interrupted");
	        }
	    }
	    return false;
	}
	
	private boolean isNetconfUp(){
		try {
			moHandler = rbs.getManagedObjectHandler();
	        moHandler.setOperationMode(OperationMode.MANUAL);
			moHandler.connect();
		
		} catch (ConnectionException | OperationFailedException | ResourceManagerConnectionException e) {
			return false;
        }
		return true;
	}
	
    /**
     * Try to open a netconf session to the node. Will wait and retry if unsuccessful.
     * @return true if netconf was opened, false otherwise
     */
	public boolean waitForNetconf() {
        for (int i = 0; i < 10; ++i) {
            if (isNetconfUp()) return true;
            logger.info("Netconf is not ready yet, sleep for " + NETCONF_RETRY_LOOP_DELAY + " ms");
            try {
                Thread.sleep(NETCONF_RETRY_LOOP_DELAY);
            } catch (InterruptedException e) {
                logger.info("Sleep was interrupted");
            }
        }
        return false;
	}
	
	private boolean isSectorUp() {
		moHandler.connect();

		List<String> sectorEquipmentFunctionList = 
				moHandler.findManagedObjectChildren(NODE_SUPPORT_LDN, "^SectorEquipmentFunction");
		if (sectorEquipmentFunctionList.isEmpty()) {
			return false;		
		}
		
		String momHelperSectorLdn = sectorEquipmentFunctionList.get(0);		
		for (String sectorEquipmentFunction : sectorEquipmentFunctionList) {
			ManagedObject sectorMo = moHandler.getManagedObject(sectorEquipmentFunction);
			ManagedObjectAttribute attribute = sectorMo.getAttribute("operationalState");
			if (attribute == null) {
				return false;
			}
			String stateValue = attribute.getValue();
			logger.info("Status for sector " + sectorEquipmentFunction + ": " + stateValue);
			
			if (!stateValue.contains("ENABLED")) { //at least one sector is not enabled
				return false;
			}
			
		}
		MomHelper.setSectorEquipmentFunctionLdn(momHelperSectorLdn); //This is so that the right MO is referred to when creating a TrxMO
		return true;
	}
	
    private boolean waitForSector() {
        for (int i = 0; i < 20; ++i) {
                if (isSectorUp()) { 
                	return true;
                }
            logger.info("Sector is not up yet, sleep for " + SECTOR_RETRY_LOOP_DELAY + " ms");
            try {
                Thread.sleep(SECTOR_RETRY_LOOP_DELAY);
            } catch (InterruptedException e) {
                logger.info("Sleep was interrupted");
            }
        }
        return false;	    
	}
	
	private boolean isGratUpAndRunning(){
		moHandler.connect();
		return moHandler.managedObjectExists(FRU1_LDN);
	}
	
	private boolean doDumpsExist(String dumpMap){
		boolean result = false;
		cli.connect();
        String findRet = cli.send("find " + dumpMap + " -type f");
		String prompt = cli.getMatchedPrompt();
        if (findRet.contains("No such file or directory")) {
            result = false;
        } else if (findRet.equals(prompt)) { // empty directory found
            result = false;
        } else {
            logger.warn(findRet);
            result = true;
        }
		cli.disconnect();
		return result;
	}
	
	private void clearMos() {
		MomHelper momHelper = new MomHelper();
		if (momHelper.isBtsFunctionCreated()) {
			// There is a BtsFunction MO, need to check further
			// need to check if sectors exist.
			List<String> gsmSectorList = moHandler.findManagedObjectChildren(BTS_FUNC_LDN, "^GsmSector");
			for (String gsmSectorLdn : gsmSectorList) {
				logger.warn("GsmSector with ldn " + gsmSectorLdn + " exist.");
				List<String> trxList = moHandler.findManagedObjectChildren(gsmSectorLdn, "^Trx");
				for (String trxLdn: trxList) {
					logger.warn("Trx with ldn " + trxLdn + " exist.");
					momHelper.lockMo(trxLdn);
					momHelper.deleteMo(trxLdn);
				}
				List<String> abisIpList = moHandler.findManagedObjectChildren(gsmSectorLdn, "^AbisIp");
				for (String abisIpLdn: abisIpList) { // Should only be one
					logger.warn("AbisIp with ldn " + abisIpLdn + " exist.");
					momHelper.lockMo(abisIpLdn);
					momHelper.deleteMo(abisIpLdn);
				}
				momHelper.deleteMo(gsmSectorLdn);
			}
		}
		
        TnConfigurator tnConfigurator = new TnConfigurator(momHelper);
        tnConfigurator.removeConfiguration();
	}

	/**
	 * @description Check for dumps
	 * 
	 * @return true if dump is found, else false
	 */
	public boolean CheckForDumps() {
	    boolean foundDump = false;
        if (doDumpsExist(PMD_DUMPS_MAP)) {
            foundDump = true;
            // Using h3 is ugly as ****, but it's clearly visible in the HTML log.
            // Consider an alternative
            testBase.setAdditionalResultInfo("<h3>Dump found in " + PMD_DUMPS_MAP + "</h3>");
        }
        if (doDumpsExist(DSP_DUMPS_MAP)) {
            foundDump = true;
            testBase.setAdditionalResultInfo("<h3>Dump found in " + DSP_DUMPS_MAP + "</h3>");
        }
        return foundDump;
	}
	
	/**
	 * @description Check that the node is configured and running
	 * 
	 * @return true if it is, false if something is wrong (read logs for reason)
	 */
	public boolean isNodeRunning() {
	    boolean nodeOk = isNetconfUp() &&
	                     isGratUpAndRunning() &&
	                     isSectorUp();
	    if (!nodeOk) {
	        testBase.setAdditionalResultInfo("Restart of node detected");
	    }
	    return nodeOk;
	}

    /**
     * @description Will check that the node is configured and running.
     *              If it is in a state where something is not up but can be
     *              soon, the method will wait and try again for some time.
     *              
     * @return true if node is up and running when method terminates, false otherwise (read logs for reason)
     */
	public boolean waitForNodeReady() {
	    return waitForNodeReady(true, true);
	}
	
    /**
     * @description Will check that the node is configured and running.
     *              If it is in a state where something is not up but can be
     *              soon, the method will wait and try again for some time.
     *              
     * @return true if node is up and running when method terminates, false otherwise (read logs for reason)
     */
	public boolean waitForNodeReadyNoCleanUp() {
	    return waitForNodeReady(true, false);
	}
	
	/**
	 * @param rebootIfNoGte If true, reboot once if no GTE process gets started
	 * @description Will check that the node is configured and running.
	 *              If it is in a state where something is not up but can be
	 *              soon, the method will wait and try again for some time.
	 *              
	 * @return true if node is up and running when method terminates, false otherwise (read logs for reason)
	 */
	public boolean waitForNodeReady(boolean rebootIfNoGte, boolean cleanUpMos){       
        // Check that netconf is up
		// this will implicitly check that the ssh is up
        if (!waitForNetconf()) {
            logger.error("NetConf did not come up, aborting");
            return false;
        }
        logger.info("Netconf is up");
        
        CheckForDumps(); // We could also check at the end of the test, but the node might be down
        logger.info("Checked for dumps");

        // Check that node is configured with first netconf XML
        if (!isGratUpAndRunning()) {
            logger.error("The node does not appear to be properly configured with GRAT_basic_fru_netconf_create.xml");
            return false;
        }
        
        // Second XML
        logger.info("The node is properly configured for GRAT");
        if (!waitForSector()) {
            logger.error("Sector is not up, node is probably not configured with RBSNC_2_basic_fru_netconf_create_common.xml");
            return false;
        }
        logger.info("Sector is up");
        
        if(cleanUpMos)
        	clearMos();
        
        return true;
	}
}
