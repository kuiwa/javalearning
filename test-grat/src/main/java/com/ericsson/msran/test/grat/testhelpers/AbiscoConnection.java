package com.ericsson.msran.test.grat.testhelpers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.testng.SkipException;
import com.ericsson.commonlibrary.restorestack.RestoreCommandStack;
import com.ericsson.abisco.clientlib.AbiscoClient;
import com.ericsson.abisco.clientlib.MessageQueue;
import com.ericsson.abisco.clientlib.servers.CHEXTRAS;
import com.ericsson.abisco.clientlib.servers.CMDHAND;
import com.ericsson.abisco.clientlib.servers.CMDHAND.BundlingProfiles;
import com.ericsson.abisco.clientlib.servers.CMDHAND.BundlingProfile1;
import com.ericsson.abisco.clientlib.servers.CMDHAND.BundlingProfile2;
import com.ericsson.abisco.clientlib.servers.CMDHAND.BundlingProfile3;
import com.ericsson.abisco.clientlib.servers.CMDHAND.CHUnsolicitedException;
import com.ericsson.abisco.clientlib.servers.CMDHAND.Channel_Group1;
import com.ericsson.abisco.clientlib.servers.CMDHAND.Channel_Group2;
import com.ericsson.abisco.clientlib.servers.CMDHAND.Enums;
import com.ericsson.abisco.clientlib.servers.CMDHAND.Enums.HoppingIndicator;
import com.ericsson.abisco.clientlib.servers.CMDHAND.Enums.HoppingType;
import com.ericsson.abisco.clientlib.servers.CMDHAND.Enums.TRXTypeList;
import com.ericsson.abisco.clientlib.servers.CMDHLIB;
import com.ericsson.abisco.clientlib.servers.LTS;
import com.ericsson.abisco.clientlib.servers.LTS.*;
import com.ericsson.abisco.clientlib.servers.PARAMDISP.SIMCardSubStruct;
import com.ericsson.msran.helpers.Helpers;
import com.ericsson.msran.test.grat.testhelpers.MomHelper;
import com.ericsson.msran.test.grat.testhelpers.restorecommands.AbiscoCloseConnection;
import com.ericsson.msran.test.grat.testhelpers.restorecommands.AbiscoReleaseLinks;
import com.ericsson.commonlibrary.resourcemanager.Abisco;
import com.ericsson.commonlibrary.resourcemanager.Rm;

/**
 * @name AbiscoConnection
 * 
 * @author Asa Huhtasaari (xasahuh)
 * 
 * @created 2013-10-01
 * 
 * @description This class handles connect and disconnect towards an Abisco. The
 *              AbisIp MO must be initialized and unlocked before connection is
 *              possible.
 * 
 * @revision xasahuh 2013-10-01 First version.
 * @revision xasahuh 2013-10-24 Split up method that connects to the Abisco.
 * @revision xasahuh 2013-11-20 Removed workaround with hard coded mapping to
 *           Abisco IP. Get IP address for RBS and Abisco in the constructor.
 * @revision xrsmari 2013-12-17 Adapted to new Abisco Java library.
 * @revision xasahuh 2014-01-08 More updates for new Abisco Java library.
 * @revision ewegans 2014-02-11 Removed inheritence from TestBase.
 * @revision xasahuh 2014-02-11 Adaption to tac-bundle support of Abisco Java library.
 *           Removed check of Abisco version.
 * 
 */
public class AbiscoConnection {
    private Logger logger; 
    private RestoreCommandStack restoreStack;
    private AbiscoClient abiscoClient;
    private Abisco abisco;
    private AbisHelper abisHelper;
    private MomHelper momhelper;
    private StpConfigHelper stpConfigHelper;

    /**
     * OML IWD version used in the tests
     */
    public static final String OML_IWD_VERSION = "G31P01";
    /**
     * First RSL IWD version used in the tests
     */
    public static final String RSL_IWD_VERSION_1 = "G01R15";
    /**
     * Second RSL IWD version used in the tests
     */
    public static final String RSL_IWD_VERSION_2 = "G01R16";
    
    /**
     * Unique address within 10.86.160 used as an alternative TSS PGW IP address that is different from the real PGW IP to trigger
     * an initial redirect of IP address in Abisco
     * VERY IMPORTANT: THIS ADDRESS MUST BE UNIQUE AND CANNOT COLLIDE WITH THE PGW IP ADDRESS OF ANY TSS NODE!
     * OTHERWISE, THERE WILL BE REDIRECT FROM ONE TSS TO ANOTHER! 
     */
    public final String ALT_BSC_PGW_IP_ADDRESS;
            
    /**
     * Name of the connection used in the Abisco
     */
    private static final String CONNECTION_NAME_PREFIX = "host_";
    
    private final int ABISCO_ATTEMPTS          = 2;

    public List<Integer> arfcnListMy = new ArrayList<Integer>();

    /**
     * Creates an Abisco connection
     */
    public AbiscoConnection() {
        logger = Logger.getLogger(AbiscoConnection.class);

        momhelper = new MomHelper();
        stpConfigHelper = StpConfigHelper.getInstance();
        ALT_BSC_PGW_IP_ADDRESS = stpConfigHelper.getAlternativePgwIpAddress();

        abisco = Rm.getAbiscoList().get(0);
        abisHelper = new AbisHelper();
        abiscoClient = abisHelper.getAbiscoClient();

        restoreStack = Helpers.restore().restoreStack();
        restoreStack.add(new AbiscoCloseConnection(abiscoClient));
    }

    /**
     * @name getBscIpAddress
     * 
     * @return String - The IP address of the Abisco
     */
    public String getBscIpAddress() {
        return abisco.getTrafficHost();
    }

    /**
     * @name getRbsIpAddress
     * 
     * @return String - The IP address of the RBS
     */
    public String getRbsIpAddress() {
        return momhelper.getTnaIpAddress();
        /*
        G2Rbs rbs = Rm.getG2RbsList().get(0);

        G2RbsAsEnodeB eNodeB = (G2RbsAsEnodeB) rbs;
        IpAddressResourceData ipTransmHost = eNodeB.getENodeBResourceData().getIpTransmissionIpAccessHostEt();
        return ipTransmHost.getIp();*/
    }
    
    /**
     * Get the connection name for the default TG (0)
     * @return
     */
    public static String getConnectionName()
    {
        return getConnectionName(0);
    }
    
    /**
     * Get the connection name for the default TG (0)
     * @return
     */
    public final String getAltPGWIP()
    {
        return ALT_BSC_PGW_IP_ADDRESS;
    }
    
    /**
     * Get the connection name for the specified TG
     * @param tgId the TG ID
     * @return
     */
    public static String getConnectionName(int tgId)
    {
        return CONNECTION_NAME_PREFIX + tgId;
    }

    /**
     * @param initialRedirect Flag indicating if the Abisco shall imitate the BSC with an initial IP address redirection
     * @name setupAbisco
     * 
     * @description Set up the Abisco with one TG (TGID=0) with one TRX.
     * 
     * @throws SkipException
     * 
     * @note The test classes must call establishLinks() before starting sending
     *       commands to the Abisco.
     * 
     */
    public void setupAbisco(boolean initialRedirect) throws SkipException {
        setupAbisco(1, 1, initialRedirect);
    }

    /**
     * @name setupAbisco
     * 
     * @description Set up the Abisco (Start TSS, New TG, Define Cell, Connect TG)
     * 
     * @param numOfTgs the number of TGs to create
     * @param trxesPerTg the number of TRXes to configure each TG for
     * @param initialRedirect Flag indicating if the Abisco shall imitate the BSC with an initial IP address redirection
     * 
     * @throws SkipException
     * 
     * @note The test classes must call establishLinks() before starting sending
     *       commands to the Abisco.
     * 
     */
    public void setupAbisco(int numOfTgs, int trxesPerTg, boolean initialRedirect) throws SkipException {

        logger.info("Abisco: Set up");
        try {

            // Start the TSS if necessary
            startTss();

            for (int tgId = 0; tgId < numOfTgs; ++tgId)
            {
                createTgAndCell(tgId, trxesPerTg, initialRedirect);
            }

            // Get running version of the Abisco (for debug purpose)
            CHEXTRAS.GetVersionInformation getVersionInfo = abiscoClient.getCHEXTRAS()
                    .createGetVersionInformation();
            CHEXTRAS.GetVersionInformationResponse response = getVersionInfo.send();
            logger.info(response.getCH_VersionNumber());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * @name setupAbisco
     * 
     * @description Set up the Abisco (Start TSS, New TG, Define Cell, Connect TG)
     * 
     * @param numOfTgs the number of TGs to create
     * @param trxesPerTg the number of TRXes to configure each TG for
     * @param initialRedirect Flag indicating if the Abisco shall imitate the BSC with an initial IP address redirection
     * @param hoppingIndicator 
     * 
     * @throws SkipException
     * 
     * @note The test classes must call establishLinks() before starting sending
     *       commands to the Abisco.
     * 
     */
    public void setupAbisco(int numOfTgs, int trxesPerTg, boolean initialRedirect, String hoppingIndicator) throws SkipException {

        logger.info("Abisco: Set up");
        try {

            // Start the TSS if necessary
            startTss();

            for (int tgId = 0; tgId < numOfTgs; ++tgId)
            {
                createTgAndCell(tgId, trxesPerTg, initialRedirect, hoppingIndicator);
            }

            // Get running version of the Abisco (for debug purpose)
            CHEXTRAS.GetVersionInformation getVersionInfo = abiscoClient.getCHEXTRAS()
                    .createGetVersionInformation();
            CHEXTRAS.GetVersionInformationResponse response = getVersionInfo.send();
            logger.info(response.getCH_VersionNumber());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Create a TG
     * @param tgId The ID of the TG
     * @param numOfTrxes How many TRXes the TG will support
     * @param initialRedirect Flag indicating if the Abisco shall imitate the BSC with an initial IP address redirection
     * @throws InterruptedException
     */
    public void createTgAndCell(int tgId, int numOfTrxes, boolean initialRedirect) throws InterruptedException
    {
        // disconnect and delete the current TG (if it exists)
        disconnectAndDeleteTG(tgId);
        
        // create a TG
        createTgPreDefBundling(tgId, getConnectionName(tgId), numOfTrxes, initialRedirect);

        // define a cell
        defineCell(tgId, numOfTrxes);

        // Connect TG
        connectTG(tgId);
        
        logger.info("Connected TG " + tgId + " ");
    }
    
    /**
     * Create a TG
     * @param tgId The ID of the TG
     * @param numOfTrxes How many TRXes the TG will support
     * @param initialRedirect Flag indicating if the Abisco shall imitate the BSC with an initial IP address redirection
     * @param hoppingIndicator 
     * @throws InterruptedException
     */
    public void createTgAndCell(int tgId, int numOfTrxes, boolean initialRedirect, String hoppingIndicator) throws InterruptedException
    {
        // disconnect and delete the current TG (if it exists)
        disconnectAndDeleteTG(tgId);
        
        // create a TG
        createTgPreDefBundling(tgId, getConnectionName(tgId), numOfTrxes, initialRedirect);

        // define a cell
        defineCell(tgId, numOfTrxes, hoppingIndicator);

        // Connect TG
        connectTG(tgId);
        
        logger.info("Connected TG " + tgId + " ");
    }
    
    /**
     * @name createTgPreDefBundling
     * 
     * @description Creates a TG with the predefined bundling groups in Abisco
     * 
     * @param tgId The id of the TG that shall be created
     * @param connectionName The id that identifies the TG
     * @param numberOfTrxes The number of trx:es that should be created in the TG
     * @param initialRedirect Flag indicating if the Abisco shall imitate the BSC with an initial IP address redirection
     * @throws InterruptedException 
     */
    public void createTgPreDefBundling(
            int    tgId,
            String connectionName,
            int numberOfTrxes,
            boolean initialRedirect) throws InterruptedException {

        BundlingProfile1 bp1 = new CMDHAND.BundlingProfile1();
        bp1.setSAPIList(Arrays.asList(0,62));
        bp1.setCRC(CMDHAND.Enums.CRC.UseCRC);
        bp1.setDSCP(51);
        bp1.setBundlingSize(1465);

        BundlingProfile2 bp2 = new CMDHAND.BundlingProfile2();
        bp2.setSAPIList(Arrays.asList(10,11));
        bp2.setCRC(CMDHAND.Enums.CRC.UseCRC);
        bp2.setDSCP(46);
        bp2.setBundlingSize(1465);

        BundlingProfile3 bp3 = new CMDHAND.BundlingProfile3();
        bp3.setSAPIList(Arrays.asList(12));
        bp3.setCRC(CMDHAND.Enums.CRC.UseCRC);
        bp3.setDSCP(28);
        bp3.setBundlingSize(1465);
        
        BundlingProfiles bundlingProfiles = new BundlingProfiles();
        bundlingProfiles.setBundlingProfileCount(3);
        bundlingProfiles.setBundlingProfile1(bp1);
        bundlingProfiles.setBundlingProfile2(bp2);
        bundlingProfiles.setBundlingProfile3(bp3);
        createTg(tgId, connectionName, numberOfTrxes, bundlingProfiles, initialRedirect);
    }

    /**
     * @name createTg
     * 
     * @description Creates a TG with the provided bundling groups in Abisco
     * 
     * @param tgId The id of the TG that shall be created
     * @param connectionName The id that identifies the TG
     * @param numberOfTrxes The number of trx:es that should be created in the TG
     * @param bundlingProfiles The bundling profiles that Abisco should recognize
     * @param initialRedirect Flag indicating if the Abisco shall imitate the BSC with an initial IP address redirection
     * @throws InterruptedException 
     */
    public void createTg(
    		int    tgId,
    		String connectionName,
    		int    numberOfTrxes,
    		BundlingProfiles bundlingProfiles,
    		boolean initialRedirect) throws InterruptedException {
    	// Create new TG
    	String rbsIpAddress = getRbsIpAddress();
    	
    	CMDHAND.NewTG newTg;
    	try {
    		newTg = abiscoClient.getCMDHAND().createNewTG();
    	} catch (CMDHAND.NewTGRejectException e) {
    		
            // Catched NewTGRejectException! 
            logger.info("Catched NewTGRejectException with cause (" + e.getCause().toString() + ")");
            logger.info("Try to recover by restarting the Absico!");
            
            // Restart Abisco as recovery action.
            AbiscoVersionHelper abiscoVerHelp;
            abiscoVerHelp = new AbiscoVersionHelper();
            abiscoVerHelp.restartSameVersion();
            
            // Throw exception to fail this TC
            throw e;    		
    	}

    	newTg.setTGId(tgId);
    	newTg.setTGTEI(127);
    	newTg.setTGBand(CMDHAND.Enums.TGBand.valueOf(momhelper.getTgBandToUse()));
    	//newTg.setAbisVersion(CMDHAND.Enums.AbisVersion.G11B);
    	newTg.setOMLIWDVersion(CMDHAND.Enums.OMLIWDVersion.G31P01);
    	newTg.setRSLIWDVersion(CMDHAND.Enums.RSLIWDVersion.G01R15);
    	newTg.setAbisMode(CMDHAND.Enums.AbisMode.IP_Dynamic);
    	newTg.setPSTUID(connectionName);
    	newTg.setPSTUIP(rbsIpAddress);
    	newTg.setNumberOfTRX(numberOfTrxes);
    	newTg.setJitterSize(20);
    	if (initialRedirect) {
    	    newTg.setAltPGWIP(ALT_BSC_PGW_IP_ADDRESS);
    	}
    	
    	// frequency sync
        //newTg.setFSOffset(new CMDHAND.FSOffset(new Integer(0xFF), new Long(0xFFFFFFFFL)));
        newTg.setFSOffset(abisHelper.getCMDHANDfsOffsetByActiveSyncSrc());
    	newTg.setTFMode(Enums.TFMode.Standalone);   	

    	List<TRXTypeList> trxTypeList = new ArrayList<TRXTypeList>();
    	for (int i=0 ; i < numberOfTrxes ; ++i) {
    		trxTypeList.add(CMDHAND.Enums.TRXTypeList.sTRU);
    	}
    	newTg.setTRXTypeList(trxTypeList);

    	CMDHAND.Pcm_IPSIU ipSiu = new CMDHAND.Pcm_IPSIU();
    	CMDHAND.PCM_A_CSIP_Specification csip = new CMDHAND.PCM_A_CSIP_Specification();

	
  		csip.setPCMNumberOfTRX(numberOfTrxes);

  		List<Integer> trxList = new ArrayList<Integer>();
   		for (int i=0 ; i < numberOfTrxes ; ++i) {
   			trxList.add(i);
   		}
   		csip.setPCMTRXTList(trxList);
      

    	ipSiu.setPCM_A_CSIP_Specification(csip);
    	newTg.setPcm_IPSIU(ipSiu);

    	
    	newTg.setBundlingProfiles(bundlingProfiles);

    	newTg.send();
    	logger.info("Created TG " + tgId);
    }
    
    
    /**
     * @name createTg
     * 
     * @description Creates a TG with the provided bundling groups in Abisco
     * 
     * @param tgId The id of the TG that shall be created
     * @param connectionName The id that identifies the TG
     * @param numberOfTrxes The number of trx:es that should be created in the TG
     * @param bundlingProfiles The bundling profiles that Abisco should recognize
     */
    public void createTg(
            int    tgId,
            String connectionName,
            int    numberOfTrxes,
            BundlingProfiles bundlingProfiles) throws InterruptedException {
        // Create new TG
        String rbsIpAddress = getRbsIpAddress();

    	CMDHAND.NewTG newTg;
    	try {
    		newTg = abiscoClient.getCMDHAND().createNewTG();
    	} catch (CMDHAND.NewTGRejectException e) {
    		
            // Catched NewTGRejectException! 
            logger.info("Catched NewTGRejectException with cause (" + e.getCause().toString() + ")");
            logger.info("Try to recover by restarting the Absico!");
            
            // Restart Abisco as recovery action.
            AbiscoVersionHelper abiscoVerHelp;
            abiscoVerHelp = new AbiscoVersionHelper();
            abiscoVerHelp.restartSameVersion();
            
            // Throw exception to fail this TC
            throw e;    		
    	}
        
        newTg.setTGId(tgId);
        newTg.setTGTEI(127);
        newTg.setTGBand(CMDHAND.Enums.TGBand.valueOf(momhelper.getTgBandToUse()));
        //newTg.setAbisVersion(CMDHAND.Enums.AbisVersion.G11B);
        newTg.setOMLIWDVersion(CMDHAND.Enums.OMLIWDVersion.G31P01);
        newTg.setRSLIWDVersion(CMDHAND.Enums.RSLIWDVersion.G01R15);
        newTg.setAbisMode(CMDHAND.Enums.AbisMode.IP_Dynamic);
        newTg.setPSTUID(connectionName);
        newTg.setPSTUIP(rbsIpAddress);
        newTg.setNumberOfTRX(numberOfTrxes);
        newTg.setJitterSize(20);

        List<TRXTypeList> trxTypeList = new ArrayList<TRXTypeList>();
        for (int i=0 ; i < numberOfTrxes ; ++i) {
            trxTypeList.add(CMDHAND.Enums.TRXTypeList.sTRU);
        }
        newTg.setTRXTypeList(trxTypeList);

        CMDHAND.Pcm_IPSIU ipSiu = new CMDHAND.Pcm_IPSIU();
        CMDHAND.PCM_A_CSIP_Specification csip = new CMDHAND.PCM_A_CSIP_Specification();

    
        csip.setPCMNumberOfTRX(numberOfTrxes);

        List<Integer> trxList = new ArrayList<Integer>();
        for (int i=0 ; i < numberOfTrxes ; ++i) {
            trxList.add(i);
        }
        csip.setPCMTRXTList(trxList);
      

        ipSiu.setPCM_A_CSIP_Specification(csip);
        newTg.setPcm_IPSIU(ipSiu);

        
        newTg.setBundlingProfiles(bundlingProfiles);
        
        newTg.send();
        logger.info("Created TG " + tgId);
    }
    
    
    /**
     * @name createTgPreDefBundling
     * 
     * @description Creates a TG with the predefined bundling groups in Abisco
     * 
     * @param tgId The id of the TG that shall be created
     * @param connectionName The id that identifies the TG
     * @param numberOfTrxes The number of trx:es that should be created in the TG
     */
    public void createTgPreDefBundling(
            int    tgId,
            String connectionName,
            int numberOfTrxes) throws InterruptedException {

        BundlingProfile1 bp1 = new CMDHAND.BundlingProfile1();
        bp1.setSAPIList(Arrays.asList(0,62));
        bp1.setCRC(CMDHAND.Enums.CRC.UseCRC);
        bp1.setDSCP(51);
        bp1.setBundlingSize(1465);

        BundlingProfile2 bp2 = new CMDHAND.BundlingProfile2();
        bp2.setSAPIList(Arrays.asList(10,11));
        bp2.setCRC(CMDHAND.Enums.CRC.UseCRC);
        bp2.setDSCP(46);
        bp2.setBundlingSize(1465);

        BundlingProfile3 bp3 = new CMDHAND.BundlingProfile3();
        bp3.setSAPIList(Arrays.asList(12));
        bp3.setCRC(CMDHAND.Enums.CRC.UseCRC);
        bp3.setDSCP(28);
        bp3.setBundlingSize(1465);
        
        BundlingProfiles bundlingProfiles = new BundlingProfiles();
        bundlingProfiles.setBundlingProfileCount(3);
        bundlingProfiles.setBundlingProfile1(bp1);
        bundlingProfiles.setBundlingProfile2(bp2);
        bundlingProfiles.setBundlingProfile3(bp3);
        createTg(tgId, connectionName, numberOfTrxes, bundlingProfiles);
    }

    /**
     * @name defineCell
     * 
     * @description Defines a cell in Abisco
     * 
     * @param tgId The id of the TG that the cell belongs to
     * @param numberOfTrxes The number of trx:es that should be created in the TG
     */
    public void defineCell(
    		int    tgId,
    		int    numberOfTrxes) throws InterruptedException {
        // Define Cell
        CMDHAND.DefineCell defineCell = abiscoClient.getCMDHAND().createDefineCell();
        defineCell.setTGId(tgId);
        defineCell.setBCCHno(momhelper.getArfcnToUse());
        
  		List<Integer> trxList = new ArrayList<Integer>();
  		List<Integer> arfcnList = new ArrayList<Integer>();
  		
   		for (int i=0 ; i < numberOfTrxes ; ++i) {
   			trxList.add(i);
   			arfcnList.add(momhelper.getArfcnToUse() + 2 * i);
   		}
   		
        defineCell.setTRXList(trxList);
        defineCell.setARFCNList(arfcnList);
        defineCell.setBS_AG_BLKS_RES(1);
        defineCell.setBS_PA_MFRMS(3);
        defineCell.setTXCell(Arrays.asList(0));
        defineCell.setTXChannelGroup(Arrays.asList(0));
        defineCell.setBSPWRB(32);
        defineCell.setBSPWRT(32);
        defineCell.setBand(momhelper.getGsmBandToUse());
        defineCell.setMSBand(momhelper.getMsBandToUse());
        defineCell.send();
        logger.info("Defined a cell in TG " + tgId);
    }
    
    
    /**
     * @name defineCell
     * 
     * @description Defines a cell in Abisco
     * 
     * @param tgId The id of the TG that the cell belongs to
     * @param numberOfTrxes The number of trx:es that should be created in the TG
     * @param hoppingIndicator 
     * @throws InterruptedException 
     */
    public void defineCell(
            int    tgId,
            int    numberOfTrxes,
            String hoppingIndicator) throws InterruptedException {
        // Define Cell
        CMDHAND.DefineCell defineCell = abiscoClient.getCMDHAND().createDefineCell();
        defineCell.setTGId(tgId);
        defineCell.setBCCHno(momhelper.getArfcnToUseTg(tgId));
        
        List<Integer> trxList = new ArrayList<Integer>();
        List<Integer> arfcnList = new ArrayList<Integer>();
        
        for (int i=0 ; i < numberOfTrxes ; ++i) {
            trxList.add(i);
            // Separate the TRX:es with at least 3 ARFCN -> 600 KHz to avoid to much interference
            // Here we set 4, that is the highest possible to run in Band 8 with 12 TRX:es (975-1023)
            arfcnList.add(momhelper.getArfcnToUseTg(tgId) + 4 * i);
        }
        
        defineCell.setTRXList(trxList);
        defineCell.setARFCNList(arfcnList);
        defineCell.setBS_AG_BLKS_RES(1);
        defineCell.setBS_PA_MFRMS(3);
        defineCell.setTXCell(Arrays.asList(0));
        defineCell.setTXChannelGroup(Arrays.asList(0));
        defineCell.setBSPWRB(32);
        defineCell.setBSPWRT(32);
        defineCell.setBand(momhelper.getGsmBandToUse());
        defineCell.setMSBand(momhelper.getMsBandToUse());
        defineCell.setCBCHIndicator(Enums.CBCHIndicator.No);  // No cell broadcast
        if(!hoppingIndicator.equalsIgnoreCase("off")) {
            HoppingType hoppingType = null;
            if (hoppingIndicator.equalsIgnoreCase("synth")) {
                hoppingType = HoppingType.SYNTH;
            } else if (hoppingIndicator.equalsIgnoreCase("baseband")) {
                hoppingType = HoppingType.BASEBAND;
            }
            defineCell.setHSN(2);
            defineCell.setHoppingIndicator(Enums.HoppingIndicator.CH_GROUPS);
            Channel_Group1 cg1 = new Channel_Group1(Enums.HoppingType.NO_HOPPING);
            cg1.setTrxList(Arrays.asList(0));
            cg1.setArfcn(Arrays.asList(arfcnList.get(0)));
            defineCell.setChannel_Group1(cg1);
    
            Channel_Group2 cg2 = new Channel_Group2(hoppingType);
            cg2.setHsn(2);
            cg2.setMaio(trxList.subList(0, numberOfTrxes - 1)); // [0,..,number of hopping Trxes)
            cg2.setTrxList(trxList.subList(1, numberOfTrxes)); // Use all but Trx=0 in this ChGr
            cg2.setArfcn(arfcnList.subList(1, arfcnList.size())); // Hop with all but BCCH frequency
            defineCell.setChannel_Group2(cg2);
        }
        defineCell.send();
        logger.info("Defined a cell in TG " + tgId);
    }
    
    /**
     * @name defineCell
     * 
     * @description Defines a cell in Abisco
     * 
     * @param tgId The id of the TG that the cell belongs to
     * @param trxdistribution eg: 0,2 this cell will setup the first,second and third Trx
     * @param bcchNo bcchNo for this cell
     * @param cellNumber 
     * @param hoppingIndicator OFF/BASEBAND/SYNTH/CH_GROUPS/OUT_OF_BOUNDS
     * @throws InterruptedException 
     */
    public void defineCell(
            int    tgId,
            String trxdistribution,
            int     bcchNo,
            int     cellNumber,
            String  hoppingIndicator) throws InterruptedException {
        // Define Cell
        CMDHAND.DefineCell defineCell = abiscoClient.getCMDHAND().createDefineCell();
        defineCell.setTGId(tgId);
        defineCell.setBCCHno(bcchNo);
        defineCell.setCellNumber(cellNumber);
        
        List<Integer> trxList = new ArrayList<Integer>();
        List<Integer> arfcnList = new ArrayList<Integer>();
        int trxStart = Integer.parseInt(trxdistribution.split(",")[0]);
        int trxEnd = Integer.parseInt(trxdistribution.split(",")[1]);
  
        for (int i= trxStart ; i < trxEnd +1 ; ++i) {
            trxList.add(i);
            arfcnList.add(bcchNo - (i * 6) );             
        }     
        defineCell.setTRXList(trxList);
        defineCell.setARFCNList(arfcnList);
        defineCell.setBS_AG_BLKS_RES(1);
        defineCell.setBS_PA_MFRMS(3);
        defineCell.setTXCell(Arrays.asList(cellNumber));
        defineCell.setTXChannelGroup(Arrays.asList(cellNumber));
        defineCell.setBand(momhelper.getGsmBandToUse());
        defineCell.setMSBand(momhelper.getMsBandToUse());
        defineCell.setHoppingIndicator(HoppingIndicator.valueOf(hoppingIndicator));
        defineCell.send();
        logger.info("define cell: " + defineCell.toString());
        logger.info("Define a cell in TG " + tgId + "with freqList" + arfcnList.toString());
        arfcnListMy.addAll(arfcnList);
    }
      

    /**
     * @name connectTG
     * 
     * @description Connects the TG with id=0
     */   
    
    public void connectTG() {
    	connectTG(0);
    }
    
    /**
     * @name connectTG
     * 
     * @description Connects a specified TG
     * 
     * @param tgId The id of the TG that shall be connected
     */
    
    public void connectTG(int tgId) {
        for (int i = 0; i < (ABISCO_ATTEMPTS*2); ++i) {
            if (i != 0) { // First try failed, wait a bit and try again.
                try {
                    logger.info("Sleep for 1 seconds before retry");
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    logger.info("Sleep was interrupted");
                }
            }
            try {
                CMDHAND.ConnectTG connectTg = abiscoClient.getCMDHAND().createConnectTG();
                connectTg.setTGId(tgId);
                connectTg.send();
                logger.info("TG " + tgId + " connected");
                return;
            } catch (CMDHAND.CHRejectException e) {
                logger.info("Cannot connect TG"); 
            } catch (CHUnsolicitedException e) {
                logger.info("Got CHUnsolicitedException when connecting TG");
            } catch (InterruptedException e) {
                logger.info("Interrupted while connecting TG");
            }
        }
    }

    /**
     * @name disconnectTG
     * 
     * @description Disconnects the TG with TG id 0
     */
    
    public void disconnectTG() {
    	disconnectTG(0);
    }

    /**
     * @name disconnectAndDeleteTG
     * 
     * @description Disconnects the TG with TG id 0
     *
     * @param tgId The id of the TG that shall be disconnected and deleted
     */
    
    public void disconnectAndDeleteTG(int tgId) {
    	disconnectTG(tgId);
    	deleteTG(tgId);
    }

    /**
     * @name disconnectTG
     * 
     * @description Disconnects a specified TG
     * 
     * @param tgId The id of the TG that shall be disconnected
     */

    public void disconnectTG(int tgId) {
        for (int i = 0; i < (ABISCO_ATTEMPTS*2); ++i) {
            if (i != 0) { // First try failed, wait a bit and retry
                try {
                    logger.info("Sleep for 1 seconds before retry");
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    logger.info("Sleep was interrupted");
                }
            }
            try {
                CMDHAND.DisconnectTG disconnectTg = abiscoClient.getCMDHAND().createDisconnectTG();
                disconnectTg.setTGId(tgId);
                disconnectTg.send();
                logger.info("TG " + tgId + " disconnected");
                return;
            } catch (CHEXTRAS.CHRejectException e) {
                logger.info("TG already disconnected");
            } catch (CMDHAND.CHRejectException e) {
                logger.info("Cannot disconnect TG");
            } catch (InterruptedException e) {
                logger.info("Interrupted while disconnecting TG");
            }
        }
    }
    
    
    public void deleteTG(int tgId) {
    	CMDHAND.DeleteTG deleteTg = abiscoClient.getCMDHAND().createDeleteTG();
    	deleteTg.setTGId(tgId);
    	try {
            deleteTg.send();
            logger.info("TG " + tgId + " deleted");
        } catch (CHEXTRAS.CHRejectException e) {
            logger.info("TG already disconnected");
        } catch (CMDHAND.CHRejectException e) {
            logger.info("Cannot disconnect TG");
        } catch (InterruptedException e) {
            logger.info("Interrupted while disconnecting TG");
        }
    }

    /**
     * @name establishLinks
     * 
     * @description Establish links from the Abisco to the RBS.
     * @throws InterruptedException When the establishLinks command gets interrupted
     */
    public void establishLinks() throws InterruptedException {
        establishLinks(false);
    }
    
    /**
     * @name establishLinks
     * 
     * @description Establish links from the Abisco to the RBS.
     * @param establishTrxc Set to true if Trxc RSL & OML are to be established
     * @throws InterruptedException When the establishLinks command gets interrupted
     */
    public void establishLinks(boolean establishTrxc) throws InterruptedException {
    	// tg=0 and trxId=0
    	establishLinks(0, establishTrxc, 0);
     }
    
    /**
     * @name establishLinks
     * 
     * @description Establish links from the Abisco to the RBS.
     * @param tgId The TG that the link belongs to.
     * @param establishTrxc Set to true if Trxc RSL & OML are to be established
     * @param trxId The id of the trx that the RSL and OML links belong to.
     * @throws InterruptedException When the establishLinks command gets interrupted
     */
//    public void establishLinks(int     tgId, 
//    		                   boolean establishTrxc,
//    						   int     trxId) throws InterruptedException {
//        List<Integer> trxcList = null;
//        if (establishTrxc) {
//        	trxcList = Arrays.asList(trxId);
//        }
//        for (int i = 0; i < ABISCO_ATTEMPTS; ++i) {
//            if (i != 0) {
//                try {
//                    logger.info("Sleep for 5 seconds before retry");
//                    Thread.sleep(5000);
//                } catch (InterruptedException e) {
//                    logger.info("Sleep was interrupted");
//                }
//            }
//            try {
//                CMDHLIB.EstablishLinks estLinks = abiscoClient.getCMDHLIB().createEstablishLinks();
//                estLinks.setTGId(tgId);
//                
//                if (establishTrxc) {
//                    estLinks.setTRXList(trxcList);
//                    estLinks.setRSL(CMDHLIB.Enums.RSL.YES);
//                    estLinks.setOML(CMDHLIB.Enums.OML.YES);
//                }
//                //Put release links on the stack. Do it before sending because
//                //Abisco will keep trying to establish even after the timeout
//                restoreStack.add(new AbiscoReleaseLinks(abiscoClient, trxcList, tgId));
//                CMDHLIB.EstablishLinksResponse estLinksResp = estLinks.send(30, TimeUnit.SECONDS);
//                logger.info("Link establish sent, estLinksResp: " + estLinksResp.toString());
//                break;
//            } catch (CMDHLIB.EstablishLinksRejectException e) {
//                logger.info("Links already established");
//            } catch (CHEXTRAS.CHRejectException e) {
//                logger.info("Cannot establish links");
//            } catch (InterruptedException ie) {
//                logger.info("Got InterruptedException from estLinks.send()");
//                throw ie;
//            }
//        }
//    }    
    public void establishLinks(int     tgId, 
        boolean establishTrxc,
        int     trxId) throws InterruptedException {
      if (establishTrxc) {
        establishLinks(tgId, trxId, true, true, true);
      } else {
        establishLinks(tgId, trxId, false, false, true);
      }
    }

    /**
     * @name establishLinks
     * 
     * @description Establish links from the Abisco to the RBS.
     * @param tgId The TG that the link belongs to.
     * @param trxId The id of the trx that the RSL and OML links belong to.
     * @param establishRSL Set to true if Trxc RSL are to be established.
     * @param establishOML Set to true if Trxc OML are to be established.
     * @param establishScfOml Set to true if Scf Oml are to be established.
     * @throws InterruptedException When the establishLinks command gets interrupted
     */
    public void establishLinks(int     tgId, 
        int     trxId,
        boolean establishRSL,
        boolean establishOML,
        boolean establishScfOml) throws InterruptedException {
      List<Integer> trxcList = null;
      if (establishRSL || establishOML){
        trxcList = Arrays.asList(trxId);
      }
      //Put release links on the restore stack.
      restoreStack.add(new AbiscoReleaseLinks(abiscoClient, trxcList, tgId));
      
      for (int i = 0; i < (ABISCO_ATTEMPTS*5); ++i) {
        if (i != 0) {
          try {
            logger.info("Sleep for 1 seconds before retry");
            Thread.sleep(1000);
          } catch (InterruptedException e) {
            logger.info("Sleep was interrupted");
          }
        }
        try {
          CMDHLIB.EstablishLinks estLinks = abiscoClient.getCMDHLIB().createEstablishLinks();
          estLinks.setTGId(tgId);

          if (establishRSL || establishOML) {
            estLinks.setTRXList(trxcList);
            if (establishRSL) {
              logger.info("Establish TRX RSL link");
              estLinks.setRSL(CMDHLIB.Enums.RSL.YES);
            }
            if (establishOML) {
              logger.info("Establish TRX OML link");
              estLinks.setOML(CMDHLIB.Enums.OML.YES);
            }
          }

          if (!establishScfOml) {
            logger.info("Don't establish SCF OML link");
            estLinks.setCF(CMDHLIB.Enums.CF.NO);
          }

          logger.info("Establish message sent:" + estLinks.toString());
          CMDHLIB.EstablishLinksResponse estLinksResp = estLinks.send(30, TimeUnit.SECONDS);
          logger.info("Link establish sent, estLinksResp: " + estLinksResp.toString());
        } catch (CMDHLIB.EstablishLinksRejectException e) {
          logger.info("Links already established");
        } catch (CHEXTRAS.CHRejectException e) {
          logger.info("Cannot establish links");
        } catch (InterruptedException ie) {
          logger.info("Got InterruptedException from estLinks.send()");
          throw ie;
        }
      }
    }
    
    /**
     * 
     * @name establishLinkForTrxOml
     * 
     * @description Establish Trx Oml link between Abisco and RBS.
     * 
     */ 
    public void establishLinkForTrxOml() throws InterruptedException {
      establishLinks(0, 0, false, true, true); // SCF OML always true
    }

    /**
     * 
     * @name establishLinkForTrxRsl
     * 
     * @description Establish Trx Rsl link between Abisco and RBS.
     * 
     */ 
    public void establishLinkForTrxRsl() throws InterruptedException {
      establishLinks(0, 0, true, false, true);
    }

    /**
     * 
     * @name establishLinkForTrxOmlRsl
     * 
     * @description Establish Trx Oml and Trx Rsl link between Abisco and RBS.
     * 
     */ 
    public void establishLinkForTrxOmlRsl() throws InterruptedException {
      establishLinks(0, 0, true, true, true); // SCF OML always true
    }

    /**
     * 
     * @name establishLinkForScfOml
     * 
     * @description Establish Scf Oml link between Abisco and RBS.
     * 
     */ 
    public void establishLinkForScfOml() throws InterruptedException {
      establishLinks(0, 0, false, false, true);
    }

    /**
     * @name establishLinksForMultipleTrxs
     * 
     * @description Establish links from the Abisco to the RBS.
     * @param tgId The TG that the link belongs to.
     * @param trxcList The list of trx ids that the RSL and OML links belong to.
     * @throws InterruptedException When the establishLinks command gets interrupted
     */
    public void establishLinksForMultipleTrxs(int     tgId, 
                               List<Integer> trxcList) throws InterruptedException {
        //Put release links on the restore stack.
        restoreStack.add(new AbiscoReleaseLinks(abiscoClient, trxcList, tgId));
        for (int i = 0; i < 25; ++i) {
            if (i != 0) {
                try {
                    logger.info("Sleep for 1 seconds before retry");
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    logger.info("Sleep was interrupted");
                }
            }
            try {
                CMDHLIB.EstablishLinks estLinks = abiscoClient.getCMDHLIB().createEstablishLinks();
                estLinks.setTGId(tgId);
                estLinks.setTRXList(trxcList);
                estLinks.setRSL(CMDHLIB.Enums.RSL.YES);
                estLinks.setOML(CMDHLIB.Enums.OML.YES);

                CMDHLIB.EstablishLinksResponse estLinksResp = estLinks.send(300, TimeUnit.SECONDS);
                logger.info("Link establish sent, estLinksResp: " + estLinksResp.toString());
                break;
            } catch (CMDHLIB.EstablishLinksRejectException e) {
                logger.info("Links already established");
            } catch (CHEXTRAS.CHRejectException e) {
                logger.info("Cannot establish links");
            } catch (InterruptedException ie) {
                logger.info("Got InterruptedException from estLinks.send()");
                throw ie;
            }
        }
    }    
    
    /**
     * @name establishLinks
     * 
     * @description Establish links from the Abisco to the RBS.
     *              Does not establish Trxc RSL & OML.
     *              This method does not wait for a response from Abisco.
     */
    public void establishLinksAsync() {
        establishLinksAsync(false);
    }
    
    /**
     * @name establishLinksAsync
     * 
     * @description Establish links from the Abisco to the RBS.
     *              This method does not wait for a response from Abisco.
     * @param establishTrxc - Set to True to also establish Trxc OML & RSL.
     */
    public void establishLinksAsync(boolean establishTrxc) {
    	// tg=0 and trxId=0
    	establishLinksAsync(0, establishTrxc, 0);
    }
    
    /**
     * @name establishLinksAsync
     * 
     * @description Establish links from the Abisco to the RBS.
     *              This method does not wait for a response from Abisco.
     *              
     * @param tgId The TG that the link belongs to.
     * @param establishTrxc Set to true if Trxc RSL & OML are to be established
     * @param trxId The id of the trx that the RSL and OML links belong to.
     * 
     */
    public void establishLinksAsync(int     tgId, 
                                    boolean establishTrxc,
			                        int     trxId) {
        List<Integer> trxcList = null;
        if (establishTrxc) {
            trxcList = new ArrayList<Integer>();
            trxcList = Arrays.asList(trxId);
        }       
        try {
            CMDHLIB.EstablishLinks estLinks = abiscoClient.getCMDHLIB().createEstablishLinks();
            estLinks.setTGId(tgId);
            
            if (establishTrxc) {
                estLinks.setTRXList(trxcList);
                estLinks.setRSL(CMDHLIB.Enums.RSL.YES);
                estLinks.setOML(CMDHLIB.Enums.OML.YES);
            }
            //Put release links on the stack. Do it before sending because
            //Abisco will keep trying to establish even after the timeout
            restoreStack.add(new AbiscoReleaseLinks(abiscoClient, trxcList, tgId));
            estLinks.sendAsync();
            Thread.sleep(5000);
        } catch (CMDHLIB.EstablishLinksRejectException e) {
            logger.info("Links already established");
        } catch (CHEXTRAS.CHRejectException e) {
            logger.info("Cannot establish links");
        } catch (InterruptedException ie) {
            logger.info("Got InterruptedException sleep.");
        }
    }
    
    
    

    /**
     * 
     * @name releaseLinks
     * 
     * @description Release links between Abisco and RBS.
     *              Trxc RSL & OML are not released.
     * 
     */
    public void releaseLinks() {
        releaseLinks(false);
    }

    /**
     * 
     * @name releaseLinks
     * 
     * @description Release links between Abisco and RBS.
     * 
     * @param releaseTrxcLinks - Set to true to also release Trxc links (RSL & OML)
     */
/*    public void releaseLinks(boolean releaseTrxcLinks) {
        for (int i = 0; i < ABISCO_ATTEMPTS; ++i) {
            if (i != 0) { // First try failed, wait a bit and try again
                try {
                    logger.info("Sleep for 5 seconds before retry");
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    logger.info("Sleep was interrupted");
                }
            }
            try {
                try {
                    CMDHLIB.ReleaseLinks releaseLinks = abiscoClient.getCMDHLIB().createReleaseLinks();
                    if (releaseTrxcLinks) {
                        releaseLinks.setTRXList(Arrays.asList(0));
                        releaseLinks.setRSL(CMDHLIB.Enums.RSL.YES);
                        releaseLinks.setOML(CMDHLIB.Enums.OML.YES);
                    }
                    releaseLinks.send();
                    logger.info("Link released");
                    break;
                } catch (CMDHLIB.ReleaseLinksRejectException e) {
                    logger.info("Links already released");
                } catch (CHEXTRAS.CHRejectException e) {
                    logger.info("Cannot release links");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
*/
    /**
     * 
     * @name releaseLinks
     * 
     * @description Release links between Abisco and RBS.
     * 
     * @param releaseTrxcLinks - Set to true to also release Trxc links (RSL & OML)
     */
    public void releaseLinks(boolean releaseTrxcLinks) {
      if (releaseTrxcLinks) {
        releaseLinks(true, true);
      } else {
        releaseLinks(false, false);
      }
    }

    /**
     * 
     * @name releaseLinks
     * 
     * @description Release links between Abisco and RBS, by default always releases Scf Rsl links.
     * 
     * @param releaseRSL - Set to true to release Trxc RSL link.
     * @param releaseOML - Set to true to release Trxc OML link.
     */ 
    public void releaseLinks(boolean releaseRSL, boolean releaseOML){
      releaseLinks(releaseRSL, releaseOML, true);
    }

    /**
     * 
     * @name releaseLinks
     * 
     * @description Release links between Abisco and RBS.
     * 
     * @param releaseRSL - Set to true to release Trxc RSL link.
     * @param releaseOML - Set to true to release Trxc OML link.
     * @param releaseScfOml - Set to true to release SCF OML link.
     */ 
    public void releaseLinks(boolean releaseRSL, boolean releaseOML, boolean releaseScfOml){
        for (int i = 0; i < (ABISCO_ATTEMPTS*5); ++i) {
            if (i != 0) { // First try failed, wait a bit and try again
                try {
                    logger.info("Sleep for 1 seconds before retry");
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    logger.info("Sleep was interrupted");
                }
            }
            try {
                CMDHLIB.ReleaseLinks releaseLinks = abiscoClient.getCMDHLIB().createReleaseLinks();
                if (releaseRSL || releaseOML) {
                    releaseLinks.setTRXList(Arrays.asList(0));
                    if (releaseRSL) {
                        releaseLinks.setRSL(CMDHLIB.Enums.RSL.YES); // NO is default
                    }
                    if(releaseOML) {
                        releaseLinks.setOML(CMDHLIB.Enums.OML.YES); // NO is default
                    }
                }
                if (!releaseScfOml) {
                    releaseLinks.setCF(CMDHLIB.Enums.CF.NO);
                }
                releaseLinks.send();
                logger.info("Link released");
                break;
            } catch (CMDHLIB.ReleaseLinksRejectException e) {
                logger.info("Links already released");
            } catch (CHEXTRAS.CHRejectException e) {
                logger.info("Cannot release links");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * 
     * @name releaseLinkTrxOml
     * 
     * @description Release Trx Oml link between Abisco and RBS.
     * 
     */ 
    public void releaseLinkForTrxOml(){
      releaseLinks(false, true, false);
    }

    /**
     * 
     * @name releaseLinkTrxRsl
     * 
     * @description Release Trx Rsl link between Abisco and RBS.
     * 
     */ 
    public void releaseLinkForTrxRsl(){
      releaseLinks(true, false, false);
    }

    /**
     * 
     * @name releaseLinkScfOml
     * 
     * @description Release Scf Oml link between Abisco and RBS.
     * 
     */ 
    public void releaseLinkForScfOml(){
      releaseLinks(false, false, true);
    }
    
    /**
     * @name resetLTS
     * 
     * @description Add LTS Mobiles, reset the LTS and counters
     * 
     */
    public void resetLTS() throws InterruptedException {
        // Add mobiles (defined in lts_mobiles.txt in .abisco dir on Ufte machine,
    	// reset LTS and statistics.
    	CMDHLIB.AddLTSMobiles addLtsMobiles = abiscoClient.getCMDHLIB().createAddLTSMobiles();
        addLtsMobiles.send();
        LTS lts = abiscoClient.getLTS();
        Reset resetCommand = lts.createReset();
        resetCommand.send();
        logger.info("Reset LTS!");
    }
    
    /**
     * @name configureLTS
     * 
     * @description Configure the maximum load test case with number of calls per second, duration and FR/EFR ratio. 
     * 
     * @param tgId - The TG id 
     * @param cellId - The cell id 
     * @param maxMsFail - Max number of failed calls before MS is marked broken by abisco.
     * @param maxPagingAttempts - Max number of pagings per call
     * @param callSetupRate - How many calls per second (NOTE: 1000=1/s, 500 = 0.5/s, 5000=5/s)
     * @param smsSetupRate - How many sms per second (NOTE: 1000=1/s, 500 = 0.5/s, 5000=5/s)
     * @param callDuration - Number of seconds call should be up.
     * @param fullRateRatio - Percentage of FR calls, e.g. 10.
     * @param ccchLoadPagingRate 
     * @param ccchLoadLRSAssRate 
     * @param ccchLoadLRSAssRejRate 
     * @param ccchLoadPSPagingRatio 
     * @param cell - enhancedFullRateRatio - Percentage of EFR calls, e.g. 90. FR+EFR should be 100.
     * @throws InterruptedException 
     */
    public void configureLTS(int tgId, int cellId, int maxMsFail, 
    		int maxPagingAttempts, int callSetupRate, int smsSetupRate, int callDuration, int fullRateRatio,
    		int enhancedFullRateRatio, int ccchLoadPagingRate, int ccchLoadLRSAssRate, int ccchLoadLRSAssRejRate, int ccchLoadPSPagingRatio,
    		int startImsi, int numberOfImsi) throws InterruptedException {
        // Configure the LTS
        LTS lts = abiscoClient.getLTS();
        
        // Set up all users/MSes, and add them to LTS
        for (int i = 0; i < numberOfImsi; i++) {
        	// Base imsi
        	Integer intArray[] = {2,4,0,9,9,9,9,0,0,0,0,0,0,0,0};
        	
        	// Make special imsi of last for digits
        	int temp = i + startImsi;
			
		    intArray[14] = temp % 10;
		    temp /= 10;
		    intArray[13] = temp % 10;
		    temp /= 10;
		    intArray[12] = temp % 10;
		    temp /= 10;
		    intArray[11] = temp % 10;
		    temp /= 10;
			
    		AddMS addMs = lts.createAddMS();
    		addMs.setIMSI(Arrays.asList(intArray));
        	addMs.setTG(tgId);
        	addMs.setCell(cellId);
        	addMs.setSim(i + (tgId * numberOfImsi));
        	addMs.setTMSI(Arrays.asList(0,0,0,0));
        	addMs.setKC(Arrays.asList(37,30,93,2,182,27,164,0));
        	addMs.setRAND(Arrays.asList(0,1,2,4,8,16,32,64,128,1,3,7,15,31,63,127));
        	addMs.send();
        }
        
        // LTS.ConfigureCell TG=0,Cell=0,MsCallPool=0,SGCallPool=0,FictiveCallPool=0
        ConfigureCell configureCell = lts.createConfigureCell();
        configureCell.setTG(tgId);
        configureCell.setCell(cellId);
        configureCell.send();
        
        // LTS.UpdateSystemSettings MaxMsFails=0,MsMaxPagingAttempts=3,TraffOperFailReport=Enable
        UpdateSystemSettings updateSettings= lts.createUpdateSystemSettings();
        updateSettings.setMaxMsFails(maxMsFail);
        updateSettings.setMsMaxPagingAttempts(maxPagingAttempts);
        //updateSettings.setTraffOperFailReport(ENABLED);
        updateSettings.send();
        
        // LTS.ConfigureCellLoad TG=0,Cell=0,MSCall.CallSetupRate=1000,MSCall.CallDuration=5,MSCall.FRRatio=0,MSCall.EFRRatio=100
        ConfigureCellLoad configureCellLoad = lts.createConfigureCellLoad();
        configureCellLoad.setTG(tgId);
        configureCellLoad.setCell(cellId);
        MSCall msCall = new MSCall();
        msCall.setCallSetupRate(callSetupRate);
        msCall.setCallDuration(callDuration);
        msCall.setFRRatio(fullRateRatio);
        msCall.setEFRRatio(enhancedFullRateRatio);
        configureCellLoad.setMSCall(msCall);

        SMS sms = new SMS();
        sms.setSMSSetupRate(smsSetupRate);
        configureCellLoad.setSMS(sms);
        
        CCCHLoad ccchLoad = new CCCHLoad();
        ccchLoad.setPagingRate(ccchLoadPagingRate);
        ccchLoad.setLRSAssRate(ccchLoadLRSAssRate);
        ccchLoad.setLRSAssRejRate(ccchLoadLRSAssRejRate);
        ccchLoad.setPSPagingRatio(ccchLoadPSPagingRatio);
        configureCellLoad.setCCCHLoad(ccchLoad);
        
        configureCellLoad.send();
        logger.info("Configure LTS!");
    }
    
    /**
     * @name startLTS
     * 
     * @description Start the load system.
     * 
     */
    public void startLTS() throws InterruptedException {
        // Start LTS
        LTS lts = abiscoClient.getLTS();
        Start start = lts.createStart();
        start.send();
        logger.info("Start LTS!");
    }
    
    /**
     * @name stopLTS
     * 
     * @description Stop the load system.
     * 
     */
    public void stopLTS() throws InterruptedException {
        // Stop LTS
        LTS lts = abiscoClient.getLTS();
        Stop stop = lts.createStop();
        stop.send();
        logger.info("Stop LTS!");
    }
    
    /**
     * @name getReportLTS
     * 
     * @description Get the report/result from test.
     * 
     * @param int tgId
     * 
     */
    public Report getReportLTS(int tgId) throws InterruptedException {
        // Get report and return it.
        LTS lts = abiscoClient.getLTS();
        GetReport getReport = lts.createGetReport();
        getReport.setTG(tgId);
        getReport.send();

        // Look in the report queue for 17 seconds.
        MessageQueue<Report> queue = lts.getReportQueue();
        Report report = queue.poll(17, TimeUnit.SECONDS);
        
        return report;
    }

    /**
     * @name startTss
     * 
     * @description Start the Abisco TSS if it is stopped or started with a
     *              different version than needed.
     * 
     */
    public void startTss() throws SkipException {
        boolean startTss = false;
        boolean stopTss = false;
        String bscIpAddress = getBscIpAddress();

        try {
            // Check if the TSS is started, and which version it is
            CHEXTRAS.GetSiteInfo getSiteInfo = abiscoClient.getCHEXTRAS().createGetSiteInfo();
            CHEXTRAS.GetSiteInfoResponse siteInfoResponse = getSiteInfo.send();
            try {
                CHEXTRAS.StandaloneTSS standaloneTss = siteInfoResponse.getStandaloneTSS();
                if (standaloneTss.getStartStopStatus() != CHEXTRAS.Enums.StartStopStatus.Started) {
                    // The TSS is not started
                    stopTss = false;
                    startTss = true;
                    logger.info("TSS is not started");
                } else if (!(standaloneTss.getPGW_IP()).contains(bscIpAddress)) {
                    // The TSS is started, but does not have correct BSC IP address
                    stopTss = true;
                    startTss = true;
                    logger.info("TSS started but wrong version: " + siteInfoResponse.getAbiscoRevision()
                            + " or bsc ip address: " + siteInfoResponse.getIP());
                }
            } catch (CHEXTRAS.GetSiteInfoResponseException e) {
                logger.info("No Site info available.");
            }

            if (stopTss) {
                logger.info("Abisco: stop and start the TSS");
                CHEXTRAS.StopStandaloneTSS stopStandaloneTss = abiscoClient.getCHEXTRAS()
                        .createStopStandaloneTSS();
                stopStandaloneTss.send();
                Thread.sleep(50000);
                logger.info("TSS stopped");
            }

            if (startTss) {
                // Start TSS takes longer than the default timeout in the CLI
                // session
                try {
                    CHEXTRAS.StartStandaloneTSS startStandaloneTss = abiscoClient.getCHEXTRAS()
                            .createStartStandaloneTSS();
                    startStandaloneTss.setPGW_IP(bscIpAddress);
                    startStandaloneTss.setRawTakeover(1);
                    startStandaloneTss.send();
                } catch (CHEXTRAS.StartStandaloneTSSRejectException e) {
                    logger.info("TSS already started");
                }
                logger.info("TSS started");

            } else {
                logger.info("Abisco: TSS already started");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @name stopTss
     * 
     * @description Stop the Abisco TSS.
     * 
     */
    public void stopTss() throws SkipException {
        logger.info("Abisco: stop the TSS");
        CHEXTRAS.StopStandaloneTSS stopStandaloneTss = abiscoClient.getCHEXTRAS()
                .createStopStandaloneTSS();
        try {
        	stopStandaloneTss.send();
        } catch (InterruptedException ie){
        	logger.info("Caugth InterruptedException during stopTSS");
        	ie.printStackTrace();
        }
        try {
        Thread.sleep(50000);
        } catch (InterruptedException ie){
        	logger.info("Sleep was interrupted");
        }
        logger.info("TSS stopped");
    }
    
    /**
     * 
     * @name redirectToIpAddress
     * 
     * @description Performs a L2TP-level redirect towards the RBS from one Abisco PGW IP address to another
     * 
     * @param tgId The TG that will be redirected   
     * @param newIpAddress - The new PGW IP address Abisco should redirect itself to
     * @throws InterruptedException when the L2TPRedirect command gets interrupted
     */ 
    public void redirectToIpAddress(int tgId, String newIpAddress) throws InterruptedException {    	                 
        try {                  	  
            CMDHLIB.L2TPRedirect redirect = abiscoClient.getCMDHLIB().createL2TPRedirect();
            redirect.setTGId(tgId);
            redirect.setNewPGWIP(newIpAddress);            		              		                
            redirect.send();
            } 
        catch (CMDHLIB.L2TPRedirectRejectException e) {
              logger.info("Failure of the L2TPRedirect command.");
            }         
        catch (CHEXTRAS.CHRejectException e) {
              logger.info("Cannot do L2TPRedirect.");
            }
        catch (InterruptedException ie) {
              logger.info("Got InterruptedException from L2TPRedirect.send()");
              throw ie;
            }
        }                  
    }
