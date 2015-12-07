package com.ericsson.msran.test.grat.testhelpers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;

import com.ericsson.abisco.clientlib.servers.CMDHAND.Enums.Band;
import com.ericsson.abisco.clientlib.servers.CMDHAND.Enums.MSBand;
import com.ericsson.commonlibrary.ecimcom.netconf.NetconfManagedObjectHandler;

import com.ericsson.commonlibrary.managedobjects.ActionResult;
import com.ericsson.commonlibrary.managedobjects.ManagedObject;
import com.ericsson.commonlibrary.managedobjects.ManagedObjectAttribute;
import com.ericsson.commonlibrary.managedobjects.ManagedObjectStructAttribute;
import com.ericsson.commonlibrary.managedobjects.ManagedObjectValueAttribute;
import com.ericsson.commonlibrary.managedobjects.ManagedObjectValueIntegerAttribute;
import com.ericsson.commonlibrary.managedobjects.ManagedObjectValueStringAttribute;
import com.ericsson.commonlibrary.managedobjects.OperationMode;
import com.ericsson.commonlibrary.managedobjects.exception.ConnectionException;
import com.ericsson.commonlibrary.managedobjects.exception.InvalidLdnException;
import com.ericsson.commonlibrary.managedobjects.exception.ManagedObjectException;
import com.ericsson.commonlibrary.managedobjects.exception.ManagedObjectModelLookupException;
import com.ericsson.commonlibrary.managedobjects.exception.NoSuchAttributeException;
import com.ericsson.commonlibrary.managedobjects.exception.NoSuchEnumMemberException;
import com.ericsson.commonlibrary.managedobjects.exception.NoSuchManagedObjectException;
import com.ericsson.commonlibrary.managedobjects.exception.NoSuchStructMemberException;

import com.ericsson.commonlibrary.managedobjects.exception.OperationFailedException;
import com.ericsson.commonlibrary.ecimcom.exception.NetconfProtocolException;
import com.ericsson.commonlibrary.remotecli.Cli;
import com.ericsson.commonlibrary.restorestack.RestoreCommand;
import com.ericsson.commonlibrary.restorestack.RestoreCommandStack;

import com.ericsson.msran.helpers.Helpers;
import com.ericsson.msran.test.grat.testhelpers.restorecommands.CreateMoCommand;
import com.ericsson.msran.test.grat.testhelpers.restorecommands.DeleteMoCommand;
import com.ericsson.msran.test.grat.testhelpers.restorecommands.DeleteTnConfig;
import com.ericsson.msran.test.grat.testhelpers.restorecommands.LockDeleteMoCommand;

import com.ericsson.commonlibrary.resourcemanager.g2.G2Rbs;
import com.ericsson.commonlibrary.resourcemanager.g2.G2RbsAsBts;
import com.ericsson.commonlibrary.resourcemanager.g2.G2RbsAsNodeB;
import com.ericsson.commonlibrary.resourcemanager.g2.G2RbsAsEnodeB;
import com.ericsson.commonlibrary.resourcemanager.Rm;
import com.ericsson.commonlibrary.resourcemanager.resourcedata.IpAddressResourceData;

/**
 * @name MomHelper
 * 
 * @author Asa Huhtasaari (xasahuh)
 * 
 * @created 2013-10-01
 * 
 * @description This class handles MOM object
 *              creation/deletion/unlocking/locking etc.
 * 
 * @revision xasahuh 2013-10-01 First version.
 * @revision xasahuh 2013-10-24 Updated method to get abisSoScfState. Moved hard
 *           coded mapping of Abisco IP to new test case ConnectAbiscoTest.java.
 * @revision xasahuh 2014-01-30 Added method getAbisTfMoState.
 * @revision xasahuh 2014-02-04 Renamed class from AbisMom to MomHelper to be more generic.
 * @revision ewegans 2014-02-11 Removed inheritance from TestBase.
 * @revision xasahuh 2014-02-11 Adaption to tac-bundle support of Abisco Java library.
 * @revision emaomar 2014-02-26 Added Trx attributes
 * @revision emaomar 2014-03-14 Renamed abisSoScfState to abisScfState
 * @revision emaomar 2014-04-04 Renamed ipv4AddressRef to addressIpV4Ref.
 * 
 */

public class MomHelper {
    
    private Logger logger;
    
    public static final String UNLOCKED = "UNLOCKED";
    public static final String LOCKED = "LOCKED";
    public static final String DEPENDENCY_FAILED = "DEPENDENCY_FAILED";
    
    public static final String ENABLED = "ENABLED";
    public static final String DISABLED = "DISABLED";
    public static final String RESET = "RESET";
    public static final String UP = "UP";
    public static final String DOWN = "DOWN";
    public static final String OFF_LINE = "OFF_LINE";
    public static final String FAILED = "FAILED";
    public static final String NOT_INSTALLED = "NOT_INSTALLED";
    public static final String ARRAY8_RESET = "[\"RESET\",\"RESET\",\"RESET\",\"RESET\",\"RESET\",\"RESET\",\"RESET\",\"RESET\"]";
    public static final String ARRAY8_ENABLED = "[\"ENABLED\",\"ENABLED\",\"ENABLED\",\"ENABLED\",\"ENABLED\",\"ENABLED\",\"ENABLED\",\"ENABLED\"]";
    public static final String ARRAY8_DISABLED = "[\"DISABLED\",\"DISABLED\",\"DISABLED\",\"DISABLED\",\"DISABLED\",\"DISABLED\",\"DISABLED\",\"DISABLED\"]";
    public static final String STARTED = "STARTED";
    public static final String UNDEFINED ="UNDEFINED";
    
    public static final String ADMINISTRATIVE_STATE = "administrativeState";
    public static final String OPERATIONAL_STATE = "operationalState";
    public static final String AVAILABILITY_STATUS = "availabilityStatus";
    public static final String ABIS_SO_SCF_STATE = "abisScfState";
    public static final String ABIS_TF_MO_STATE = "abisTfState";
    public static final String ABIS_TX_MO_STATE = "abisTxState";
    public static final String ABIS_RX_MO_STATE = "abisRxState";
    public static final String ABIS_TS_MO_STATE = "abisTsState";
    public static final String ABIS_AT_MO_STATE = "abisAtState";
    public static final String ABIS_SO_TRXC_STATE = "abisTrxcState";
    public static final String ABIS_SCF_OML_STATE = "abisScfOmlState";
    public static final String ABIS_TRXC_OML_STATE = "abisTrxcOmlState";
    public static final String ABIS_TRX_RSL_STATE = "abisTrxRslState";
    public static final String ABIS_TF_MODE = "abisTfMode";
    public static final String ABIS_CLUSTER_GROUP_ID = "abisClusterGroupId";
    
    private static final String USER_LABEL = "userLabel";
    private static final String BTS_VERSION = "btsVersion";
    public static final String PEER_IP_ADDRESS = "peerIpAddress";
    public static final String BSC_BROKER_IP_ADDRESS = "bscBrokerIpAddress";
    public static final String TRX_INDEX = "trxIndex";
    
    public static final String BSC_NODE_IDENTITY = "bscNodeIdentity";
    public static final String BSC_TG_IDENTITY = "bscTgIdentity";

    public static final String GSM_SECTOR_ID_VALUE = "1";
    public static final String MANAGED_ELEMENT_ID_VALUE = "1";
    public static final String BTS_FUNCTION_ID_VALUE = "1";
    public static final String BTS_VERSION_VALUE = "ERAG51RXXV01";
    public static final String BTS_USER_LABEL_VALUE = "GRAT_FIRST_CALL_AND_SMS_ON_DUS52_20141125";

    public static final String FRU_DU_LDN = "ManagedElement=1,Equipment=1,FieldReplaceableUnit=1";
    public static final String FRU_RU_LDN = "ManagedElement=1,Equipment=1,FieldReplaceableUnit=2";
    public static final String RESTART_DU_ACTION = "restartUnit";
    public static final String RESTART_DU_RANK_COLD = "RESTART_COLD";
    public static final String RESTART_DU_REASON = "PLANNED_RECONFIGURATION";
    public static final String RESTART_DU_INFO = "Restarted by GRAT JCAT";

    public static final String BTS_FUNCTION_LDN = "ManagedElement=" + MANAGED_ELEMENT_ID_VALUE + ",BtsFunction=" + BTS_FUNCTION_ID_VALUE;
    public static final String SECTOR_LDN_NO_ID = BTS_FUNCTION_LDN + ",GsmSector=";
    public static final String SECTOR_LDN = SECTOR_LDN_NO_ID + GSM_SECTOR_ID_VALUE;
    public static final String SECTOR_EQUIP_FUNC_REF = "sectorEquipmentFunctionRef";
    public static final String SECTOR_EQUIP_FUNC_LDN_1 = "ManagedElement=1,NodeSupport=1,SectorEquipmentFunction=1";
    public static final String SECTOR_EQUIP_FUNC_LDN_2 = "ManagedElement=1,NodeSupport=1,SectorEquipmentFunction=2";
    public static final String SECTOR_EQUIP_FUNC_LDN_3 = "ManagedElement=1,NodeSupport=1,SectorEquipmentFunction=3";
    private static final String TRANSPORT_LDN = "ManagedElement=1,Transport=1";
    private static final String SYNCHRONIZATION_LDN = TRANSPORT_LDN + ",Synchronization=1";
    public static final String ETH_PORT_LDN = TRANSPORT_LDN + ",EthernetPort=eth10";
    public static final String VLAN_LDN = TRANSPORT_LDN + ",VlanPort=1";
    private static final String ROUTER_LDN = TRANSPORT_LDN + ",Router=1";
    private static final String IF_IPv4_LDN = ROUTER_LDN + ",InterfaceIPv4=GSM";
    public static final String TNA_IP_LDN = IF_IPv4_LDN + ",AddressIPv4=GSM";
    private static final String RADIO_EQUIP_CLOCK = SYNCHRONIZATION_LDN + ",RadioEquipmentClock=1";
    private static final String RADIO_EQUIP_CLOCK_REF = RADIO_EQUIP_CLOCK + ",RadioEquipmentClockReference=1";
    public static final String ABIS_IP_LDN = SECTOR_LDN + ",AbisIp=1";
    public static final String TRX_LDN = SECTOR_LDN + ",Trx=0";
    public static final String SNMP_LDN = "ManagedElement=1,SystemFunctions=1,SysM=1,Snmp=1";
    
    public static final int MAX_NUMBER_OF_TRX_FOR_RUS01 = 4;
    public static final int MAX_NUMBER_OF_TRX_FOR_RUS02 = 8;

    private NetconfManagedObjectHandler moHandler;
    private String tnaIpAddress;
    private String defaultRouterIpAddress;
    private RestoreCommandStack restoreStack;
    private G2Rbs rbs;
    private StpConfigHelper stpConfigHelper;
    
    private TrxParamsForRu trxParams;

    // Following stuff is needed to test Synch
    private CreateMoCommand createRadioEquipmentReferenceClock;
    private LockDeleteMoCommand deleteRadioEquipmentReferenceClock;
    List<ManagedObjectAttribute> radioEquipmentClockReferenceAttributes;
    private String frequencySyncIoLdn;
    private String timeSyncIoLdn;
    private CreateMoCommand createFrequencySyncIo;
    List<ManagedObjectAttribute> frequencySyncIoAttributes;
    private ManagedObjectValueAttribute syncPortEncapsulation;
    private DeleteMoCommand deleteTimeSyncIo;
    
    private static String sectorEquipmentFunctionLdnToUse = SECTOR_EQUIP_FUNC_LDN_1;
    private String site;
    
    /**
     * @name MomHelper
     * 
     * @description Constructor that will connect to MO handler.
     */
    
    public MomHelper() {
    	logger = Logger.getLogger(MomHelper.class);
    	rbs = Rm.getG2RbsList().get(0);
        moHandler = rbs.getManagedObjectHandler();
        moHandler.setOperationMode(OperationMode.MANUAL);
        moHandler.connect();
        restoreStack = Helpers.restore().restoreStackHelper().getRestoreStack();
        trxParams = new TrxParamsForRu(FRU_RU_LDN);
        site = Rm.getSite();
        stpConfigHelper = StpConfigHelper.getInstance();
                
        IpAddressResourceData ipTransmHost = null;
        
        if (rbs.isBts()){
        	logger.info("isBts = TRUE");
            G2RbsAsBts bts = (G2RbsAsBts) rbs.asBts();
            ipTransmHost = bts.getG2RbsBtsResourceData().getIpTransmissionIpAccessHostEt();
        }
        else if (rbs.isNodeB()){
        	logger.info("isNodeB = TRUE");
            G2RbsAsNodeB nodeB = (G2RbsAsNodeB) rbs.asNodeB();
            ipTransmHost = nodeB.getNodeBResourceData().getIpTransmissionIpAccessHostEt();
        }
        else if (rbs.isEnodeB()){
        	logger.info("isEnodeB = TRUE");
            G2RbsAsEnodeB eNodeB = (G2RbsAsEnodeB) rbs.asEnodeB();
            ipTransmHost = eNodeB.getENodeBResourceData().getIpTransmissionIpAccessHostEt();
        }
        
        if (ipTransmHost == null)
        	logger.fatal("CANNOT DETERMINE RBSTYPE!!");

        tnaIpAddress = ipTransmHost.getIp();
        defaultRouterIpAddress = ipTransmHost.getSubnet().getDefaultRouter();
        
        logger.info("Using tnaIpAddress=" + tnaIpAddress + " and defaultRouterIpAddress=" + defaultRouterIpAddress);
    }
       
    /**
     * @name configureTn
     * @param bscIpAddress
     */
    public void configureTn(final String bscIpAddress) {
    	logger.info("Start of configureTn");
    	for (int i=0 ; i < 5 ; ++i) {
    		try {
    			TnConfigurator tnConfigurator = new TnConfigurator(this);
    			tnConfigurator.removeConfiguration();

    			// specify which MOs that shall be created together, and their parameter values
    			List<ManagedObject> createMos = new ArrayList<ManagedObject>();

    			tnConfigurator.configureTnaIpAddress(this.tnaIpAddress, createMos);
    	        // Configure static route 
    			tnConfigurator.configureStaticRoute(1, "0.0.0.0/0", this.defaultRouterIpAddress, createMos);

    			// create and commit all MOs in one transaction
    			createSeveralMoInOneTx(createMos);
    			restoreStack.add(new DeleteTnConfig(tnConfigurator));
    	 
    			// give TN some time to get ready before we continue 
    	        if (!waitForMoAttributeStringValue(ETH_PORT_LDN, MomHelper.OPERATIONAL_STATE, MomHelper.ENABLED, 10)) {
    	        	logger.error("TN MO: " + ETH_PORT_LDN + ", did not reach operational state ENABLED");
    	        }
    	        logger.info("End of configureTn");
    	        return;
    		} catch (Exception e) {
        		logger.error("configureTn caught Exception toString()=" + e.toString() + " getMessage()=" + e.getMessage() + " getCause()=" + e.getCause());
        		e.printStackTrace();
    		}
        	// try to make netconf come back to life
        	moHandler.connect();
        	moHandler.abort();
        	moHandler.disconnect();
        	
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                logger.info("Sleep got interrupted");
            }
    	}
    }
    
    /**
     * Create and unlock complete GRAT TGs.
     * Public parameters:
     * @param numOfTgs - The number of TGs to create
     * @param numOfTrxsPerTg - The number of TRXs in each TG
     * @param useDefaultSectors - Set to false if default sectors shall not be used.
     *                            Optional parameter, default value is true.
     *
     * Private parameters:
     * @param numberOfTrxsPerSectorEqFunc - Max number of TRXs per SectorEquipmentFunction
     * 
     * @throws ArrayIndexOutOfBoundsException - If the numOfTgs*numOfTrxsPerTg exceeds numberOfTrxsPerSectorEqFunc*sectorEqFunctList.size().
     * @return
     */
    public List<TgLdns> createUnlockAllGratMos(int numOfTgs, int numOfTrxsPerTg) {
        // only put 4 TRXs per sector equipment function to ensure we can use a RUS 01
    	return createUnlockAllGratMos(numOfTgs, numOfTrxsPerTg, MAX_NUMBER_OF_TRX_FOR_RUS01, true);
    }
    
    public List<TgLdns> createUnlockAllGratMos(int numOfTgs, int numOfTrxsPerTg, boolean useDefaultSectors) {
        // only put 4 TRXs per sector equipment function to ensure we can use a RUS 01
    	return createUnlockAllGratMos(numOfTgs, numOfTrxsPerTg, MAX_NUMBER_OF_TRX_FOR_RUS01, useDefaultSectors);
    }
    
    private List<TgLdns> createUnlockAllGratMos(int numOfTgs, int numOfTrxsPerTg, int numberOfTrxsPerSectorEqFunc, boolean useDefaultSectors) {
        // Putting the AbiscoConnection as a class member will result in a
        // circular dependency, requiring infinite space, so create it locally
        AbiscoConnection abisco = new AbiscoConnection();
        List<TgLdns> tgList = new ArrayList<>(numOfTgs);
        String sectorEquipFuncLdn1 = MomHelper.SECTOR_EQUIP_FUNC_LDN_1;
        String sectorEquipFuncLdn2 = MomHelper.SECTOR_EQUIP_FUNC_LDN_2;
        
        if (useDefaultSectors == false)
        {
        	sectorEquipFuncLdn1 = MomHelper.SECTOR_EQUIP_FUNC_LDN_2;
        	sectorEquipFuncLdn2 = MomHelper.SECTOR_EQUIP_FUNC_LDN_1;
        }	

        final List<String> sectorEqFunctList = Arrays.asList(
        		sectorEquipFuncLdn1, 
        		sectorEquipFuncLdn2, 
                MomHelper.SECTOR_EQUIP_FUNC_LDN_3);
        
        if (!isTnConfigured()) {
        	configureTn(abisco.getBscIpAddress());
        }

        List<ManagedObject> createMos = new ArrayList<ManagedObject>();
        
    	if (!isBtsFunctionCreated()) {
    		createMos.add(buildBtsFunctionMo(MomHelper.BTS_FUNCTION_LDN, MomHelper.BTS_USER_LABEL_VALUE));
    		addRestoreCmd(new DeleteMoCommand(MomHelper.BTS_FUNCTION_LDN));
    	}

        int numberOfCreatedTrxs = 0;
        for (int current_tg = 0; current_tg < numOfTgs; ++current_tg) {
            TgLdns tg = new TgLdns();
            tgList.add(tg);
            logger.debug("Create GsmSector, create and unlock AbisIp");
            tg.sectorLdn = MomHelper.SECTOR_LDN_NO_ID + current_tg;
            tg.abisIpLdn = tg.sectorLdn + ",AbisIp=1"; 
            
            createMos.add(buildGsmSectorMo(tg.sectorLdn));
        	addRestoreCmd(new DeleteMoCommand(tg.sectorLdn));
        	
        	createMos.add(buildAbisIpMo(AbiscoConnection.getConnectionName(current_tg),
        			                    abisco.getBscIpAddress(),
        			                    tg.abisIpLdn,
        			                    MomHelper.TNA_IP_LDN,
        			                    MomHelper.UNLOCKED));
        	addRestoreCmd(new LockDeleteMoCommand(tg.abisIpLdn));
        	
        	createSeveralMoInOneTx(createMos);
        	createMos.clear(); // very important to not get the BtsFunction MO, GsmSector, and AbisIp again for next TG
        	logger.info("Created " + tg.sectorLdn + ", creted and unlocked " + tg.abisIpLdn);        	
        	       	
            logger.debug("Create and unlock " + numOfTrxsPerTg + " TRXs in the sector");
            for (int i = 0; i < numOfTrxsPerTg; ++i, ++numberOfCreatedTrxs) {
            	tg.trxLdnList.add(String.format("%s,Trx=%s", tg.sectorLdn, Integer.toString(i)));
                List<ManagedObject> createTrxMo = new ArrayList<ManagedObject>();
                createTrxMo.add(buildTrxMo(tg.trxLdnList.get(i), MomHelper.UNLOCKED, sectorEqFunctList.get(numberOfCreatedTrxs/numberOfTrxsPerSectorEqFunc)));
            	addRestoreCmd(new LockDeleteMoCommand(tg.trxLdnList.get(i)));
            	createSeveralMoInOneTx(createTrxMo);
                logger.info("Created and unlocked " + tg.trxLdnList.get(i));
            }
        }
        return tgList;
    }
    
    /**
     * @name createSectorAndAbisIpMo
     * 
     * @description Create GsmSector MO and AbisIp Mo.
     * 
     * @param connectionName
     * @param bscIpAddress
     * @param initialRedirect Flag indicating if the Abisco shall imitate the BSC with an initial IP address redirection
     * 
     * @return String - LDN of the AbisIp MO
     * 
     * @throws ConnectionException
     */
    public String createSectorAndAbisIpMo(final String connectionName, final String bscIpAddress, boolean initialRedirect)
    		throws ConnectionException {
    	return createSectorAndAbisIpMo(connectionName, bscIpAddress, ABIS_IP_LDN, initialRedirect);
    }
    
    /**
     * @name createSectorAndAbisIpMo
     * 
     * @description Create GsmSector MO and AbisIp Mo.
     * 
     * @param connectionName
     * @param bscIpAddress
     * @param abisIpLdn
     * @param initialRedirect Flag indicating if the Abisco shall imitate the BSC with an initial IP address redirection
     * 
     * @return String - LDN of the AbisIp MO
     * 
     * @throws ConnectionException
     */
    public String createSectorAndAbisIpMo(final String connectionName, final String bscIpAddress, final String abisIpLdn, boolean initialRedirect)
            throws ConnectionException {
            
        if (!isTnConfigured())
        	configureTn(bscIpAddress);
     
        // Create GsmSector and AbisIp MO
        createGsmSectorMos(1);
        createAbisIpMo(connectionName, bscIpAddress, abisIpLdn, initialRedirect);
            
        return abisIpLdn;
    }

    /**
     * @name createSectorAbisIpMoAndTrxMo
     * 
     * @description Create GsmSector MO, AbisIp Mo and Trx MO
     * 
     * @param connectionName
     * @param bscIpAddress
     * @param initialRedirect Flag indicating if the Abisco shall imitate the BSC with an initial IP address redirection
     * 
     * @return String - LDN of the AbisIp MO
     * 
     * @throws ConnectionException
     */
    public TgLdns createSectorAbisIpMoAndTrxMo(final String connectionName, final String bscIpAddress, boolean initialRedirect) throws ConnectionException {
            
        if (!isTnConfigured())
        	configureTn(bscIpAddress);
       
        // Create GsmSector and AbisIp MO
        String sectorLdn = createGsmSectorMos(1).get(0);
        
        return new TgLdns(sectorLdn,
                createAbisIpMo(connectionName, bscIpAddress, initialRedirect),
                createTrxMo(sectorLdn, "0"));
    }
    
    /**
     * @name createSectorAndAbisIpMo
     * 
     * @description Create GsmSector MO and AbisIp Mo.
     * 
     * @param connectionName
     * @param bscIpAddress
     * @param abisIpLdn
     * @param sectorNo
     * 
     * @return String - LDN of the AbisIp MO
     * 
     * @throws ConnectionException
     */
    public String createSectorAndCorrespondingAbisIpMo(final String connectionName, final String bscIpAddress, final String abisIpLdn, int sectorNo)
            throws ConnectionException {
            
        if (!isTnConfigured())
            configureTn(bscIpAddress);
     
        // Create GsmSector and AbisIp MO
        String sectorLdn = String.format("%s,GsmSector=%s", BTS_FUNCTION_LDN, Integer.toString(sectorNo));
        createGsmSectorMo(sectorLdn);
        createAbisIpMo(connectionName, bscIpAddress, abisIpLdn);
            
        return abisIpLdn;
    }

    /**
     * @name setAttributeForMoAndCommit
     * 
     * @description Helper method for setting a given attribute on a MO and
     *              commit it.
     * 
     * @param ldn - LDN of MO
     * @param attributeName - Attribute name
     * @param value - Attribute value
     * @throws ConnectionException - Thrown if commit fails or not possible to
     *             connect to/with moHandler.
     */
    public void setAttributeForMoAndCommit(final String ldn, final String attributeName, final String value)
            throws ConnectionException {
        moHandler.connect();
        ManagedObjectValueAttribute attribute = new ManagedObjectValueAttribute(attributeName, value);
        moHandler.setAttribute(ldn, attribute);
        moHandler.commit();
    }

    /**
     * @name createBtsFunctionMo
     * 
     * @description Method for creating one BtsFu8nction MO.
     * 
     * @param btsFunctionLdn       - String the LDN of the BtsFunction that shall be created
     * 
     * @return void
     * 
     * @throws ConnectionException
     */   
    public void createBtsFunctionMo(String btsFunctionLdn) throws ConnectionException {
        logger.info("Create BtsFunction MO with LDN " + btsFunctionLdn);
        
        ManagedObject btsFunctionMo = buildBtsFunctionMo(btsFunctionLdn, BTS_USER_LABEL_VALUE);

        for (int i=0 ; i < 10 ; ++i) {
        	try {
        		createManagedObject(btsFunctionMo);
        		logger.info("createBtsFunctionMo creation of BtsFunction appears to have been successfull");
                //Put delete of the BtsFunction MO on the stack
                restoreStack.add(new DeleteMoCommand(btsFunctionLdn));
        		return;
        	} catch (Exception e) {
        		logger.error("configureTn caught Exception toString()=" + e.toString() + " getMessage()=" + e.getMessage() + " getCause()=" + e.getCause());
        		e.printStackTrace();
        	}
        	
        	// try to make netconf come back to life
        	moHandler.connect();
        	moHandler.abort();
        	moHandler.disconnect();
        	
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                logger.info("Sleep got interrupted");
            }
        }
        logger.error("createBtsFunctionMo failed to create BtsFunction MO");
    }
    
    /**
     * @name buildBtsFunctionMo
     * 
     * @description Method for creating one GsmSector MO Java object. No MO is created on the node.
     * 
     * @param btsFunctionLdn       - String the LDN of the GsmSector that shall be created
     * @param userLabelValue       - The String to put in the user label
     * 
     * @return ManagedObject - The MO 
     * 
     */   
    public ManagedObject buildBtsFunctionMo(String btsFunctionLdn) {
        return new ManagedObject(btsFunctionLdn);
    }
    
    public ManagedObject buildBtsFunctionMo(String btsFunctionLdn, String userLabelValue) {    	
        ManagedObject btsFunctionMo = buildBtsFunctionMo(btsFunctionLdn);
        
        ManagedObjectAttribute userLabelAttr = new ManagedObjectValueStringAttribute(USER_LABEL, userLabelValue);
        btsFunctionMo.addAttribute(userLabelAttr);
        
        return btsFunctionMo;
    }
    
    /**
     * @name isBtsFunctionCreated
     * 
     * @description Helper method for checking if BtsFunction exist.
     * 
     */
    public boolean isBtsFunctionCreated() {
        moHandler.connect();
        return moHandler.managedObjectExists(BTS_FUNCTION_LDN);
    }
    
    /**
     * @name randomPrivateIp
     * 
     * @description returns a random IP-address in "192.168.XXX.XXX" format.
     * 
     */
    public String randomPrivateIp() {
    	// Generate two random octets between 10 and 233
    	int rndOctet1 = 10 + (int)(Math.random() * (234-10));
    	int rndOctet2 = 10 + (int)(Math.random() * (234-10));
    	String rndIp = "192.168." + Integer.toString(rndOctet1) + "." + Integer.toString(rndOctet2) ;
    	logger.info("randomPrivateIp returning: " + rndIp);
        return rndIp;
    }
    
    /**
     * @name deleteMo
     * 
     * @description Delete the MO with specified LDN.
     *
     * @param moLdn
     */
    public void deleteMo(String moLdn) {
        moHandler.connect();
        moHandler.deleteManagedObject(moLdn);
        moHandler.commit();
    }
    
    /**
     * @name setAttributesAndDeleteMoInSameTx
     * 
     * @description Sets attributes and delete the MO, with specified LDN, in the same transaction.
     *
     * @param ldn
     * @param attributes
     */
    
    public void setAttributesAndDeleteMoInSameTx(String ldn, List<ManagedObjectAttribute> attributes) {
        moHandler.connect();
        moHandler.setAttributes(ldn, attributes);
        moHandler.deleteManagedObject(ldn);
        moHandler.commit();
    }
    
    
    /**
     * @name createSeveralMoInOneTx
     * 
     * @description Creates several MOs in one transaction
     *
     * @param A list of the MO:s that shall be created in the transaction. The MOs will be created in the order
     * they have in the list.
     * 
     */    
    public void createSeveralMoInOneTx(List<ManagedObject> mos) {
        moHandler.connect();
        
        for (int i = 0; i < mos.size(); i++) {
        	moHandler.createManagedObject(mos.get(i));
        }
        moHandler.commit();       
    }

    /**
     * @name deleteSeveralMoInOneTx
     * 
     * @description Sets attribute values and deletes several MOs in one transaction. 
     *
     * @param A list of the MO:s that shall be deleted in the transaction. The MOs will be deleted in the order
     * they have in the list.
     */    
    public void deleteSeveralMoInOneTx(List<ManagedObject> mos) {
        moHandler.connect();
        
        for (int i = 0; i < mos.size(); i++) {
        	moHandler.setAttributes(mos.get(i).getLocalDistinguishedName(), mos.get(i).getAttributes());
        	moHandler.deleteManagedObject(mos.get(i));
        }
        moHandler.commit();       
    }
    
    public void deleteSeveralMoInOneTx(List<ManagedObject> mosToDelete, List<ManagedObject> mosToUpdateAttributes) {
        moHandler.connect();
        
        for (int i = 0; i < mosToUpdateAttributes.size(); i++) {
        	moHandler.setAttributes(mosToUpdateAttributes.get(i).getLocalDistinguishedName(), mosToUpdateAttributes.get(i).getAttributes());
        }
        
        for (int i = 0; i < mosToDelete.size(); i++) {
        	moHandler.deleteManagedObject(mosToDelete.get(i));
        }
        moHandler.commit();       
    }
  
    /**
     * @name unlockMo
     * 
     * @description Set administrativeState to UNLOCKED
     * 
     * @param moLdn - String LDN to UNLOCK
     */
    public void unlockMo(String moLdn) {
        setAttributeForMoAndCommit(moLdn, ADMINISTRATIVE_STATE, UNLOCKED);
    }

    /**
     * @name lockMo
     * 
     * @description Method for locking AbisIP MO.
     * 
     * @param moLdn - String LDN to LOCK
     */
    public void lockMo(String moLdn) {
        setAttributeForMoAndCommit(moLdn, ADMINISTRATIVE_STATE, LOCKED);
    }
    
    /**
     * @name getAdminState
     * 
     * @description Method to get administrativeState for a given LDN.
     *
     * @param moLdn - String LDN of the MO to get state of
     * 
     * @return String administrativeState ("LOCKED, "UNLOCKED"...)
     */
    public String getAdminState(String moLdn) {
        moHandler.connect();
        ManagedObject mo = moHandler.getManagedObject(moLdn);
        ManagedObjectAttribute attribute = mo.getAttribute(ADMINISTRATIVE_STATE);
        
        if (null == attribute)
        {
        	return "";
        }
        else
        {
        	return attribute.getValue();
        }        
        
    }

    /**
     * @name getOpState
     * 
     * @description Method to get the operationalState for a given LDN.
     * 
     * @param moLdn - String LDN of the MO to get state of
     * 
     * @return String operationalState ("ENABLED", "DISABLED"...)
     */
    public String getOpState(String moLdn) {
        moHandler.connect();
        ManagedObject mo = moHandler.getManagedObject(moLdn);
        ManagedObjectAttribute attribute = mo.getAttribute(OPERATIONAL_STATE);
        
        // Attribute's value can be null
        if (attribute == null) {
        	return "";
        } 
        return attribute.getValue();
    }
        
    /**
     * @getAvailStatus
     * 
     * @description Method to get the availabilityStatus for a given LDN.
     * 
     * @param moLdn - String LDN of the MO to get status of
     * 
     * @return String availabilityStatus
     */
    public String getAvailStatus(String moLdn) {
        moHandler.connect();
        ManagedObject mo = moHandler.getManagedObject(moLdn);
        ManagedObjectAttribute attribute = mo.getAttribute(AVAILABILITY_STATUS);
        
        // Attribute's value is null in case the availability list is empty
        if (attribute == null) {
        	return "";
        } 
        return attribute.getValue();
    }
    
    /**
     * Get the specified attribute value from an MO as a String
     * @param moLdn String LDN of the MO
     * @param attributeName The name of the attribute
     * @return String value
     */
    public String getAttributeValue(String moLdn, String attributeName) {
        moHandler.connect();
        ManagedObject mo = moHandler.getManagedObject(moLdn);
        ManagedObjectAttribute attribute = mo.getAttribute(attributeName);
        
        // Attribute's value is null in case the attribute's value is an empty list
        if (attribute == null)
        	return "";
        
        return attribute.getValue();
    }
    
    /**
     * @name checkMoExist
     * 
     * @param moLdn
     * 
     * @return true if the MO exists
     */
    public boolean checkMoExist(String moLdn) {
    	moHandler.connect();
    	return moHandler.managedObjectExists(moLdn);
    }
    
    /**
     * @name waitForOperationalState
     * 
     * @param moLdn
     * @param String - wanted OPERATIONAL STATE
     * @return String Requested OpState or emty string if not found
     * 
     * @description Tests that the OPERATIONAL STATE of MO with trxLdn has value opState
     * 				If not, sleep for 1s
     * 				Repeat attempts number of times until it gets the right OPERATIONL STATE or until attempts * sleepIntervalSeconds have gone,
     * 				in which case an empty String is returned
     */
    public String waitForOperationalState(String moLdn, String opState, int attempts) {
        // Wait for maximum attempts * 1s for desired operational state
        String tmpOpState = "";
        for (int i = 0; i < attempts; i++) {
        	logger.debug("Waiting for " + moLdn + " to get " + opState);
            tmpOpState = getOpState(moLdn);
            if (tmpOpState.equals(opState)) {
                break;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                logger.info("Sleep got interrupted");
            }
        }
        return tmpOpState;
    }
    
    /**
     * @name waitForMoAttributeStringValue
     * 
     * @param moLdn
     * @param String - attributeName
     * @param String - valueToWaitFor
     * 
     * @return boolean false if not found, true if found
     * 
     * @description Tests that the attributeName of MO with moLdn contains valueToWaitFor within number of attempts
     * 				Repeat attempts number of times until it gets the valueToWaitFor or until attempts * 1s have gone,
     * 				in which case false is returned
     */
    public boolean waitForMoAttributeStringValue(String moLdn, String attributeName, String valueToWaitFor, int attempts) {
        // Wait for maximum attempts * 1s for desired operational state
        String tmpAttrValue = "";
        for (int i = 0; i < attempts; i++) {
        	tmpAttrValue = getAttributeValue(moLdn, attributeName);
            if (tmpAttrValue.contains(valueToWaitFor)) {
            	return true;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                logger.info("Sleep got interrupted");
            }
        }
        logger.info("MO " + moLdn + " attribute " + attributeName + " did not reach expected value. Got: " + tmpAttrValue + ", expected: " + valueToWaitFor);
        return false;
    }
    
    
    /**
     * @name waitForMoAttributeStringValue
     * 
     * @param moLdn
     * @param String - attributeName
     * @param List<String> - valuesToWaitFor
     * 
     * @return boolean false if not found, true if found
     * 
     * @description Tests that the attributeName of MO with moLdn contains any of the values in the list provided valuesToWaitFor within number of attempts
     * 				Repeat attempts number of times until it gets the valueToWaitFor or until attempts * 1s have gone,
     * 				in which case false is returned
     */
    public boolean waitForMoAttributeStringValue(String moLdn, String attributeName, List<String> valuesToWaitFor, int attempts) {
        // Wait for maximum attempts * 1 seconds for any values in the List provided
        String tmpAttrValue = "";
        for (int i = 0; i < attempts; i++) {
            tmpAttrValue = getAttributeValue(moLdn, attributeName);
            for (int j=0 ; j < valuesToWaitFor.size() ; ++j) {
                if (tmpAttrValue.contains(valuesToWaitFor.get(j))) {
                    return true;
                }
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                logger.info("Sleep got interrupted");
            }
        }       	
        logger.info("MO " + moLdn + " attribute " + attributeName + " did not reach expected value. Got: " + tmpAttrValue + ", expected: " + valuesToWaitFor.toString());
        return false;
    }
    /**
     * @name waitForAvailState
     * 
     * @param ldn
     * @param String - wanted AVAILABILITY STATE
     * 
     * @return boolean - true if wanted AVAILABILITY STATE
     * 
     * @description Tests that the AVAILABILITY STATE of MO with ldn has value opState
     * 				If not, sleep for 1s
     * 				Repeat attempts number of times until it gets the right AVAILABILITY STATE or until attempts * sleepIntervalSeconds have gone,
     * 				in which case FALSE returned
     */
    public boolean waitForAvailState(String ldn, String availState, int attempts) {
        // Wait for maximum attempts * 1s for desired operational state
        for (int i = 0; i < attempts; i++) {
            if (getAvailStatus(ldn).contains(availState)) {
                return true;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                logger.info("Sleep got interrupted");
            }
        }
        return false;
    }
    
    /**
     * @name waitForEmptyAvailState
     * 
     * @param ldn
     * 
     * @return boolean
     * 
     * @description Tests that the AVAILABILITY STATE of MO with ldn has value opState
     * 				If not, sleep for 1s
     * 				Repeat attempts number of times until it gets the right AVAILABILITY STATE or until attempts * sleepIntervalSeconds have gone,
     * 				in which case FALSE returned
     */
    public boolean waitForEmptyAvailState(String ldn, int attempts) {
        // Wait for maximum attempts * 1s for desired operational state
        for (int i = 0; i < attempts; i++) {
            if (getAvailStatus(ldn).isEmpty()) {
                return true;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                logger.info("Sleep got interrupted");
            }
        }
        return false;
    }
    
    /**
     * @name waitForEmptyAttributeValue
     * 
     * @param ldn
     * @param attribute
     * 
     * @return boolean
     * 
     * @description Tests that the AVAILABILITY STATE of MO with ldn has value opState
     * 				If not, sleep for 1s
     * 				Repeat attempts number of times until it gets the right AVAILABILITY STATE or until attempts * 1s have gone,
     * 				in which case FALSE returned
     */
    public boolean waitForEmptyAttributeValue(String ldn, String attribute, int attempts) {
        // Wait for maximum attempts * 1s for desired operational state
        for (int i = 0; i < attempts; i++) {
            if (getAttributeValue(ldn, attribute).isEmpty()) {
                return true;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                logger.info("Sleep got interrupted");
            }
        }
        return false;
    }
    
    /**
     * @name waitForMoAttributes
     * 
     * @param ldn - The ldn to the MO that shall be checked
     * @param attribute - A map of attributes and their expected values
     * @param secondsToTimeout The number of seconds we wait for the MO to get the expected state
     * 
     * @return String An empty string if the MO reached expected state, otherwise the string includes an error message
     * 
     * @description This method checks if an MO has reached the expected state as described with the supplied attributes and values 
     * 
     */
    public String waitForMoAttributes(String ldn, Map<String,String> attributes, int secondsToTimeout) {
        int secondsElapsed = 0;
        String errorStr = "";
        
        boolean attributeHasExpectedValue = false;
       
        // Try until we timeout
        while ((secondsElapsed <= secondsToTimeout) && (attributeHasExpectedValue == false)) {
        	logger.info("***** waitForMoAttributes iteration=" + secondsElapsed); 
        	moHandler.connect();
            ManagedObject mo = moHandler.getManagedObject(ldn);
            logger.info("Got MO: " + ldn + ";" + mo.toString());
            List<ManagedObjectAttribute> moData = mo.getAttributes();
            printAtributes(moData);
            
            // Loop over the attributes we want to check
            for (Map.Entry<String, String> entry : attributes.entrySet()) {
                boolean foundAttribute = false;
                attributeHasExpectedValue = false;
            	// Loop over the attributes received in the net5conf response from the node 
            	for (int i=0 ; i < moData.size() ; ++i) {
            		//logger.info("***** Data to compare: moData.get(i).getName()=" + moData.get(i).getName() + ", entry.getKey()=" + entry.getKey() + ", moData.get(i).getValue()=" + moData.get(i).getValue() + ", entry.getValue()=" + entry.getValue());

            		if (moData.get(i).getName().equals(entry.getKey())) {
            			foundAttribute = true; // the attribute is found
            			if (moData.get(i).getValue().equals(entry.getValue())) {
            				// Found a matching attribute name and value
            				attributeHasExpectedValue = true;
            				logger.info("*** Attribute name=" + entry.getKey() + " with value=" + entry.getValue() + " exist and has the expected value");
            			} else {
            				attributeHasExpectedValue = false;
            			}
        				// Break inner for-loop, to start searching for next attribute we want to check
        				break;
            		}
            	}
            	// we have now searched through all the received MO attributes for one of the attributes we want to check
            	if (attributeHasExpectedValue == false) {
            		if ((foundAttribute == false) && (entry.getValue() == null)) {
            			// We consider the attribute to have the expected value since the attribute is not 
            			// present in the netconf response if has an empty value
            			attributeHasExpectedValue = true;
            			logger.info("*** Attribute="+ entry.getKey() + " is not found in the netconf response, but since the expected value is null, consider attribute to have expected value.");
            		} else {
            			if (foundAttribute == false) {
            				logger.info("*** Did not find the expected attribute="+ entry.getKey() + " , sleep and try again.");
            			} else {
            				logger.info("*** Found the expected attribute="+ entry.getKey() + ", but with a non expected value=" + entry.getValue() + " , sleep and try again.");
            			}
            			errorStr = "Attribute=" + entry.getKey() + " does not have expected value=" + entry.getValue();
            			// Did not find the expected attribute with expected value, sleep for a while and try again
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ie) {
                            logger.info("Sleep got interrupted");
                        }
            			secondsElapsed++;
                        // Start to search from the beginning again by breaking outer for-loop.
                        break;            			
            		}
            	}
            }
        }
        
        if (secondsElapsed > secondsToTimeout) {
        	logger.info("*** Timeout: " + errorStr);
        	return errorStr;
        }
        
        logger.info("*** All investigated attributes had the expected value");
        return "";
     }    
    
    private void printAtributes(List<ManagedObjectAttribute> attributes)
    {
    	StringBuffer attr = new StringBuffer();
    	
    	attr.append("MO contains " + attributes.size() + " attributes: ");
    	
    	for (int i=0 ; i < attributes.size() ; ++i)
    	{
    		attr.append("Name: " + attributes.get(i).getName() + " Value: " + attributes.get(i).getValue() + "; "); 
    	}
    	logger.info(attr.toString());
    }
    
    public String checkBtsFunctionMoAttributesAfterCreation(String btsFunctionLdn, int noOfRetries) {
    	Map<String, String> attributes = new HashMap<String,String>();
    	attributes.put(MomHelper.BTS_VERSION, MomHelper.BTS_VERSION_VALUE);
    	attributes.put(MomHelper.USER_LABEL, MomHelper.BTS_USER_LABEL_VALUE);
    	// Add checks for bscNodeIdentity and bscTgIdentity
    	return waitForMoAttributes(btsFunctionLdn, attributes, noOfRetries);  	    	
    }
    
    public String checkGsmSectorMoAttributeAfterCreateNoLinks(String gsmSectorLdn, int noOfRetries) {
    	Map<String, String> attributes = new HashMap<String,String>();
    	attributes.put(MomHelper.ABIS_SCF_OML_STATE, MomHelper.DOWN);
    	attributes.put(MomHelper.ABIS_AT_MO_STATE, MomHelper.RESET);
    	attributes.put(MomHelper.ABIS_SO_SCF_STATE, MomHelper.RESET);
    	attributes.put(MomHelper.ABIS_TF_MO_STATE, MomHelper.RESET);
        attributes.put(MomHelper.ABIS_TF_MODE, MomHelper.UNDEFINED);  //default value of abisTfMode is "undefined"
        attributes.put(MomHelper.ABIS_CLUSTER_GROUP_ID, null);  //default value of abisClusterGroupId is null
    	// Add checks for bscNodeIdentity and bscTgIdentity
    	return waitForMoAttributes(gsmSectorLdn, attributes, noOfRetries);  	
    }
    
    public String checkGsmSectorMoAttributeAfterCreateLinksEstablished(String gsmSectorLdn, int noOfRetries) {
    	Map<String, String> attributes = new HashMap<String,String>();
    	attributes.put(MomHelper.ABIS_SCF_OML_STATE, MomHelper.UP);
    	attributes.put(MomHelper.ABIS_AT_MO_STATE, MomHelper.RESET);
    	attributes.put(MomHelper.ABIS_SO_SCF_STATE, MomHelper.RESET);
    	attributes.put(MomHelper.ABIS_TF_MO_STATE, MomHelper.RESET);
    	return waitForMoAttributes(gsmSectorLdn, attributes, noOfRetries);    
    }  	
    
    public String checkGsmSectorMoAttributeAfterCreateLinksEstablishedSoScfStarted(String gsmSectorLdn, int noOfRetries) {
    	Map<String, String> attributes = new HashMap<String,String>();
    	attributes.put(MomHelper.ABIS_SCF_OML_STATE, MomHelper.UP);
    	attributes.put(MomHelper.ABIS_AT_MO_STATE, MomHelper.RESET);
    	attributes.put(MomHelper.ABIS_SO_SCF_STATE, MomHelper.STARTED);
    	attributes.put(MomHelper.ABIS_TF_MO_STATE, MomHelper.RESET);
    	return waitForMoAttributes(gsmSectorLdn, attributes, noOfRetries);    
    }

    public String checkGsmSectorMoAttributeAfterAllMosStarted(String gsmSectorLdn, int noOfRetries) {
    	Map<String, String> attributes = new HashMap<String,String>();
    	attributes.put(MomHelper.ABIS_SCF_OML_STATE, MomHelper.UP);
    	attributes.put(MomHelper.ABIS_AT_MO_STATE, MomHelper.DISABLED);
    	attributes.put(MomHelper.ABIS_SO_SCF_STATE, MomHelper.STARTED);
    	attributes.put(MomHelper.ABIS_TF_MO_STATE, MomHelper.DISABLED);
    	return waitForMoAttributes(gsmSectorLdn, attributes, noOfRetries);    
    }
   
    public String checkGsmSectorMoAttributeAfterAllMosStartedAoAtEnabled(String gsmSectorLdn, int noOfRetries) {
    	Map<String, String> attributes = new HashMap<String,String>();
    	attributes.put(MomHelper.ABIS_SCF_OML_STATE, MomHelper.UP);
    	attributes.put(MomHelper.ABIS_AT_MO_STATE, MomHelper.ENABLED);
    	attributes.put(MomHelper.ABIS_SO_SCF_STATE, MomHelper.STARTED);
    	attributes.put(MomHelper.ABIS_TF_MO_STATE, MomHelper.DISABLED);
    	return waitForMoAttributes(gsmSectorLdn, attributes, noOfRetries);    
    }       
    
    public String checkGsmSectorMoAttributeAfterAllMosStartedAoAtAndAoTfEnabled(String gsmSectorLdn, int noOfRetries) {
        Map<String, String> attributes = new HashMap<String,String>();
        attributes.put(MomHelper.ABIS_SCF_OML_STATE, MomHelper.UP);
        attributes.put(MomHelper.ABIS_AT_MO_STATE, MomHelper.ENABLED);
        attributes.put(MomHelper.ABIS_SO_SCF_STATE, MomHelper.STARTED);
        attributes.put(MomHelper.ABIS_TF_MO_STATE, MomHelper.ENABLED);
        return waitForMoAttributes(gsmSectorLdn, attributes, noOfRetries);    
    }       
    
    public String checkGsmSectorMoAttributeAfterAllMosStartedAoAtEnabledNoLinks(String gsmSectorLdn, int noOfRetries) {
        Map<String, String> attributes = new HashMap<String,String>();
        attributes.put(MomHelper.ABIS_SCF_OML_STATE, MomHelper.DOWN);
        attributes.put(MomHelper.ABIS_AT_MO_STATE, MomHelper.ENABLED);
        attributes.put(MomHelper.ABIS_SO_SCF_STATE, MomHelper.STARTED);
        attributes.put(MomHelper.ABIS_TF_MO_STATE, MomHelper.DISABLED);
        return waitForMoAttributes(gsmSectorLdn, attributes, noOfRetries);
    }
    
    public String checkGsmSectorMoAttributeAfterAllMosStartedAoAtAndAoTfEnabledNoLinks(String gsmSectorLdn, int noOfRetries) {
        Map<String, String> attributes = new HashMap<String,String>();
        attributes.put(MomHelper.ABIS_SCF_OML_STATE, MomHelper.DOWN);
        attributes.put(MomHelper.ABIS_AT_MO_STATE, MomHelper.ENABLED);
        attributes.put(MomHelper.ABIS_SO_SCF_STATE, MomHelper.STARTED);
        attributes.put(MomHelper.ABIS_TF_MO_STATE, MomHelper.ENABLED);
        return waitForMoAttributes(gsmSectorLdn, attributes, noOfRetries);    
    }       
    
    
    public String checkGsmSectorMoAttributeAfterAllMosStartedAoAtResetNoLinks(String gsmSectorLdn, int noOfRetries) {
        Map<String, String> attributes = new HashMap<String,String>();
        attributes.put(MomHelper.ABIS_SCF_OML_STATE, MomHelper.DOWN);
        attributes.put(MomHelper.ABIS_AT_MO_STATE, MomHelper.RESET);
        attributes.put(MomHelper.ABIS_SO_SCF_STATE, MomHelper.STARTED);
        attributes.put(MomHelper.ABIS_TF_MO_STATE, MomHelper.DISABLED);
        return waitForMoAttributes(gsmSectorLdn, attributes, noOfRetries);
    }
    
    public String checkGsmSectorMoAttributeAfterAllMosStartedAoAtResetLinkEstablished(String gsmSectorLdn, int noOfRetries) {
        Map<String, String> attributes = new HashMap<String,String>();
        attributes.put(MomHelper.ABIS_SCF_OML_STATE, MomHelper.UP);
        attributes.put(MomHelper.ABIS_AT_MO_STATE, MomHelper.RESET);
        attributes.put(MomHelper.ABIS_SO_SCF_STATE, MomHelper.STARTED);
        attributes.put(MomHelper.ABIS_TF_MO_STATE, MomHelper.DISABLED);
        return waitForMoAttributes(gsmSectorLdn, attributes, noOfRetries);
    }
  
    public String checkAbisIpMoAttributeAfterLock(String abisIpLdn, int noOfRetries) {
       	Map<String, String> attributes = new HashMap<String,String>();
    	attributes.put(MomHelper.ADMINISTRATIVE_STATE, MomHelper.LOCKED);
    	attributes.put(MomHelper.OPERATIONAL_STATE, MomHelper.DISABLED);
    	attributes.put(MomHelper.AVAILABILITY_STATUS, MomHelper.OFF_LINE);
    	attributes.put(MomHelper.PEER_IP_ADDRESS, null);
    	return waitForMoAttributes(abisIpLdn, attributes, noOfRetries);    	
    }

    public String checkAbisIpMoAttributeAfterUnlockBscConnected(String abisIpLdn, String bscIpAddress, int noOfRetries) {
       	Map<String, String> attributes = new HashMap<String,String>();
    	attributes.put(MomHelper.ADMINISTRATIVE_STATE, MomHelper.UNLOCKED);
    	attributes.put(MomHelper.OPERATIONAL_STATE, MomHelper.ENABLED);
    	attributes.put(MomHelper.AVAILABILITY_STATUS, null);
    	attributes.put(MomHelper.PEER_IP_ADDRESS, bscIpAddress);
    	return waitForMoAttributes(abisIpLdn, attributes, noOfRetries);    	
    }    	
    
     public String checkAbisIpMoAttributeAfterUnlockBscDisconnected(String abisIpLdn, int noOfRetries) {
    	Map<String, String> attributes = new HashMap<String,String>();
        attributes.put(MomHelper.ADMINISTRATIVE_STATE, MomHelper.UNLOCKED);
        attributes.put(MomHelper.OPERATIONAL_STATE, MomHelper.DISABLED);
        attributes.put(MomHelper.AVAILABILITY_STATUS, MomHelper.FAILED);
    	attributes.put(MomHelper.PEER_IP_ADDRESS, null);
        return waitForMoAttributes(abisIpLdn, attributes, noOfRetries);    	
    }
     
     public String checkAbisIpMoAttributeAfterUnlockBscWithRedirect(String abisIpLdn, int noOfRetries, String ipPeerAddress) {
         Map<String, String> attributes = new HashMap<String,String>();
         attributes.put(MomHelper.ADMINISTRATIVE_STATE, MomHelper.UNLOCKED);
         attributes.put(MomHelper.OPERATIONAL_STATE, MomHelper.DISABLED);
         attributes.put(MomHelper.AVAILABILITY_STATUS, FAILED);
         attributes.put(MomHelper.PEER_IP_ADDRESS, ipPeerAddress);
         attributes.put(MomHelper.BSC_BROKER_IP_ADDRESS, stpConfigHelper.getAlternativePgwIpAddress());
         return waitForMoAttributes(abisIpLdn, attributes, noOfRetries);     
     }
    
    public String checkTrxMoAttributeAfterLock(String trxLdn, int noOfRetries, String expectedTrxIndex) {
    	Map<String, String> attributes = new HashMap<String,String>();
    	attributes.put(MomHelper.ADMINISTRATIVE_STATE, MomHelper.LOCKED);
    	attributes.put(MomHelper.OPERATIONAL_STATE, MomHelper.DISABLED);
    	attributes.put(MomHelper.AVAILABILITY_STATUS, MomHelper.OFF_LINE);
    	attributes.put(MomHelper.ABIS_TRXC_OML_STATE, MomHelper.DOWN);
    	attributes.put(MomHelper.ABIS_TRX_RSL_STATE, MomHelper.DOWN);
    	attributes.put(MomHelper.ABIS_RX_MO_STATE, null);
    	attributes.put(MomHelper.ABIS_TX_MO_STATE, null);
    	attributes.put(MomHelper.ABIS_SO_TRXC_STATE, null);
    	attributes.put(MomHelper.ABIS_TS_MO_STATE, null);
    	attributes.put(MomHelper.TRX_INDEX, expectedTrxIndex);
    	return waitForMoAttributes(trxLdn, attributes, noOfRetries);
    }
     
    public String checkTrxMoAttributeAfterLock(String trxLdn, int noOfRetries) {
    	return checkTrxMoAttributeAfterLock(trxLdn, noOfRetries, trxLdn.split("Trx=")[1]);
    }
    
   public String checkTrxMoAttributeAfterUnlockNoLinks(String trxLdn, int noOfRetries) {
    	Map<String, String> attributes = new HashMap<String,String>();
    	attributes.put(MomHelper.ADMINISTRATIVE_STATE, MomHelper.UNLOCKED);
    	attributes.put(MomHelper.OPERATIONAL_STATE, MomHelper.ENABLED);
    	attributes.put(MomHelper.AVAILABILITY_STATUS, null);
    	attributes.put(MomHelper.ABIS_TRXC_OML_STATE, MomHelper.DOWN);
    	attributes.put(MomHelper.ABIS_TRX_RSL_STATE, MomHelper.DOWN);   	
    	attributes.put(MomHelper.ABIS_RX_MO_STATE, MomHelper.RESET);
    	attributes.put(MomHelper.ABIS_TX_MO_STATE, MomHelper.RESET);
    	attributes.put(MomHelper.ABIS_SO_TRXC_STATE, MomHelper.RESET);
    	attributes.put(MomHelper.ABIS_TS_MO_STATE, MomHelper.ARRAY8_RESET);
    	attributes.put(MomHelper.TRX_INDEX, trxLdn.split("Trx=")[1]);
    	return waitForMoAttributes(trxLdn, attributes, noOfRetries);
    }
   
   public String checkTrxMoAttributeWithLockedSectorEquipmentFunc(String trxLdn, int noOfRetries) {
	   	Map<String, String> attributes = new HashMap<String,String>();
	   	attributes.put(MomHelper.ADMINISTRATIVE_STATE, MomHelper.UNLOCKED);
	   	attributes.put(MomHelper.OPERATIONAL_STATE, MomHelper.DISABLED);
	   	attributes.put(MomHelper.AVAILABILITY_STATUS, MomHelper.DEPENDENCY_FAILED);
	   	attributes.put(MomHelper.ABIS_TRXC_OML_STATE, MomHelper.UP);
	   	attributes.put(MomHelper.ABIS_TRX_RSL_STATE, MomHelper.UP);   	
	   	attributes.put(MomHelper.ABIS_RX_MO_STATE, MomHelper.RESET);
	   	attributes.put(MomHelper.ABIS_TX_MO_STATE, MomHelper.RESET);
	   	attributes.put(MomHelper.ABIS_SO_TRXC_STATE, MomHelper.RESET);
	   	attributes.put(MomHelper.ABIS_TS_MO_STATE, MomHelper.ARRAY8_RESET);
	   	attributes.put(MomHelper.TRX_INDEX, trxLdn.split("Trx=")[1]);
	   	return waitForMoAttributes(trxLdn, attributes, noOfRetries);
	   }
 
   public String checkTrxMoAttributesAfterUnlockNoBbResources(String trxLdn, int noOfRetries) {
       Map<String, String> attributes = new HashMap<String,String>();
       attributes.put(MomHelper.ADMINISTRATIVE_STATE, MomHelper.UNLOCKED);
       attributes.put(MomHelper.OPERATIONAL_STATE, MomHelper.DISABLED);
       attributes.put(MomHelper.AVAILABILITY_STATUS, MomHelper.NOT_INSTALLED);
       attributes.put(MomHelper.ABIS_TRXC_OML_STATE, MomHelper.DOWN);
       attributes.put(MomHelper.ABIS_TRX_RSL_STATE, MomHelper.DOWN);
       attributes.put(MomHelper.ABIS_RX_MO_STATE, null);
       attributes.put(MomHelper.ABIS_TX_MO_STATE, null);
       attributes.put(MomHelper.ABIS_SO_TRXC_STATE, null);
       attributes.put(MomHelper.ABIS_TS_MO_STATE, null);
       attributes.put(MomHelper.TRX_INDEX, trxLdn.split("Trx=")[1]);
       return waitForMoAttributes(trxLdn, attributes, noOfRetries);
   }
   
   public String checkTrxMoAttributeAfterLockSectorEquipFuncWithAllEnabledLinksEstablished(String trxLdn, int noOfRetries) {
       return checkTrxMoAttributeAfterLockSectorEquipFuncWithAllEnabledLinksEstablished(trxLdn, noOfRetries, trxLdn.split("Trx=")[1]);  
   }
   
   public String checkTrxMoAttributeAfterLockSectorEquipFuncWithAllEnabledLinksEstablished(String trxLdn, int noOfRetries, String expectedTrxIndex) {
       Map<String, String> attributes = new HashMap<String,String>();
       attributes.put(MomHelper.ADMINISTRATIVE_STATE, MomHelper.UNLOCKED);
       attributes.put(MomHelper.OPERATIONAL_STATE, MomHelper.DISABLED);
       attributes.put(MomHelper.AVAILABILITY_STATUS, MomHelper.DEPENDENCY_FAILED);
       attributes.put(MomHelper.ABIS_TRXC_OML_STATE, MomHelper.UP);
       attributes.put(MomHelper.ABIS_TRX_RSL_STATE, MomHelper.UP);
       attributes.put(MomHelper.ABIS_RX_MO_STATE, MomHelper.ENABLED);
       attributes.put(MomHelper.ABIS_TX_MO_STATE, MomHelper.ENABLED);
       attributes.put(MomHelper.ABIS_SO_TRXC_STATE, MomHelper.STARTED);
       attributes.put(MomHelper.ABIS_TS_MO_STATE, MomHelper.ARRAY8_ENABLED);
       attributes.put(MomHelper.TRX_INDEX, expectedTrxIndex);
       return waitForMoAttributes(trxLdn, attributes, noOfRetries);   
   }
   
   public String checkTrxMoAttributeAfterUnlockAllEnabledLinksEstablished(String trxLdn, int noOfRetries) {
       return checkTrxMoAttributeAfterUnlockAllEnabledLinksEstablished(trxLdn, noOfRetries, trxLdn.split("Trx=")[1]);  
   }
   
   public String checkTrxMoAttributeAfterUnlockAllEnabledLinksEstablished(String trxLdn, int noOfRetries, String expectedTrxIndex) {
       Map<String, String> attributes = new HashMap<String,String>();
       attributes.put(MomHelper.ADMINISTRATIVE_STATE, MomHelper.UNLOCKED);
       attributes.put(MomHelper.OPERATIONAL_STATE, MomHelper.ENABLED);
       attributes.put(MomHelper.AVAILABILITY_STATUS, null);
       attributes.put(MomHelper.ABIS_TRXC_OML_STATE, MomHelper.UP);
       attributes.put(MomHelper.ABIS_TRX_RSL_STATE, MomHelper.UP);
       attributes.put(MomHelper.ABIS_RX_MO_STATE, MomHelper.ENABLED);
       attributes.put(MomHelper.ABIS_TX_MO_STATE, MomHelper.ENABLED);    
       attributes.put(MomHelper.ABIS_SO_TRXC_STATE, MomHelper.STARTED);
       attributes.put(MomHelper.ABIS_TS_MO_STATE, MomHelper.ARRAY8_ENABLED);
       attributes.put(MomHelper.TRX_INDEX, expectedTrxIndex);
       return waitForMoAttributes(trxLdn, attributes, noOfRetries);        
   }
 
    public String checkTrxMoAttributeAfterUnlockLinksEstablished(String trxLdn, int noOfRetries, String expectedTrxIndex) {
    	Map<String, String> attributes = new HashMap<String,String>();
    	attributes.put(MomHelper.ADMINISTRATIVE_STATE, MomHelper.UNLOCKED);
    	attributes.put(MomHelper.OPERATIONAL_STATE, MomHelper.ENABLED);
    	attributes.put(MomHelper.AVAILABILITY_STATUS, null);
    	attributes.put(MomHelper.ABIS_TRXC_OML_STATE, MomHelper.UP);
    	attributes.put(MomHelper.ABIS_TRX_RSL_STATE, MomHelper.UP);
    	attributes.put(MomHelper.ABIS_RX_MO_STATE, MomHelper.RESET);
    	attributes.put(MomHelper.ABIS_TX_MO_STATE, MomHelper.RESET);   	
    	attributes.put(MomHelper.ABIS_SO_TRXC_STATE, MomHelper.RESET);
    	attributes.put(MomHelper.ABIS_TS_MO_STATE, MomHelper.ARRAY8_RESET);
    	attributes.put(MomHelper.TRX_INDEX, expectedTrxIndex);
    	return waitForMoAttributes(trxLdn, attributes, noOfRetries);    	
    }
    
    public String checkTrxMoAttributeAfterResetAoRx(String trxLdn, int noOfRetries) {
    	Map<String, String> attributes = new HashMap<String,String>();
    	//A4.8. The RBS updates Trx MO attributes operationalState=Enabled, availabilityStatus=<empty>, abisRxState=Reset 
    	attributes.put(MomHelper.ADMINISTRATIVE_STATE, MomHelper.UNLOCKED);
    	attributes.put(MomHelper.OPERATIONAL_STATE, MomHelper.ENABLED);
    	attributes.put(MomHelper.AVAILABILITY_STATUS, null);
    	attributes.put(MomHelper.ABIS_RX_MO_STATE, MomHelper.RESET);
    	return waitForMoAttributes(trxLdn, attributes, noOfRetries);    	
    }
    
    public String checkTrxMoAttributeAfterResetSoTrxOrResetBoard(String trxLdn, int noOfRetries) {
    	Map<String, String> attributes = new HashMap<String,String>();
    	//A2.6 The RBS updates Trx MO attributes with operationalState = Enabled, availabilityStatus=<empty>, abisTxState=Reset, abisRxState=Reset, abisTsState=Reset, abisTrxcState=Reset
    	//Trx MO has attributes: -abisTxState=Reset -abisRxState=Reset -abisTsState=Reset -abisTrxcState=Reset
    	attributes.put(MomHelper.ADMINISTRATIVE_STATE, MomHelper.UNLOCKED);
    	attributes.put(MomHelper.OPERATIONAL_STATE, MomHelper.ENABLED);
    	attributes.put(MomHelper.AVAILABILITY_STATUS, null);
    	attributes.put(MomHelper.ABIS_RX_MO_STATE, MomHelper.RESET);
    	attributes.put(MomHelper.ABIS_TX_MO_STATE, MomHelper.RESET);   	
    	attributes.put(MomHelper.ABIS_SO_TRXC_STATE, MomHelper.RESET);
    	attributes.put(MomHelper.ABIS_TS_MO_STATE, MomHelper.ARRAY8_RESET);
    	return waitForMoAttributes(trxLdn, attributes, noOfRetries);    	
    }
        
    public String checkTrxMoAttributeAfterUnlockLinksEstablished(String trxLdn, int noOfRetries) {
    	return checkTrxMoAttributeAfterUnlockLinksEstablished(trxLdn, noOfRetries, trxLdn.split("Trx=")[1]);
    }
    
    public String checkTrxMoAttributeAfterUnlockAllStartedLinksEstablished(String trxLdn, int noOfRetries) {
    	Map<String, String> attributes = new HashMap<String,String>();
    	attributes.put(MomHelper.ADMINISTRATIVE_STATE, MomHelper.UNLOCKED);
    	attributes.put(MomHelper.OPERATIONAL_STATE, MomHelper.ENABLED);
    	attributes.put(MomHelper.AVAILABILITY_STATUS, null);
    	attributes.put(MomHelper.ABIS_TRXC_OML_STATE, MomHelper.UP);
    	attributes.put(MomHelper.ABIS_TRX_RSL_STATE, MomHelper.UP);
    	attributes.put(MomHelper.ABIS_RX_MO_STATE, MomHelper.DISABLED);
    	attributes.put(MomHelper.ABIS_TX_MO_STATE, MomHelper.DISABLED);    
    	attributes.put(MomHelper.ABIS_SO_TRXC_STATE, MomHelper.STARTED);
    	attributes.put(MomHelper.ABIS_TS_MO_STATE, MomHelper.ARRAY8_DISABLED);
    	attributes.put(MomHelper.TRX_INDEX, trxLdn.split("Trx=")[1]);
    	return waitForMoAttributes(trxLdn, attributes, noOfRetries);        
    }
    
    public String checkTrxMoAttribute85SecAutoDetectLinkBreakPreCond(String trxLdn, int noOfRetries) {
      Map<String, String> attributes = new HashMap<String,String>();
      attributes.put(MomHelper.ADMINISTRATIVE_STATE, MomHelper.UNLOCKED);
      attributes.put(MomHelper.OPERATIONAL_STATE, MomHelper.ENABLED);
      attributes.put(MomHelper.AVAILABILITY_STATUS, null);
      attributes.put(MomHelper.ABIS_TRXC_OML_STATE, MomHelper.DOWN);
      attributes.put(MomHelper.ABIS_TRX_RSL_STATE, MomHelper.UP);
      attributes.put(MomHelper.ABIS_TX_MO_STATE, MomHelper.ENABLED);      
      return waitForMoAttributes(trxLdn, attributes, noOfRetries);
  }

  
  public String checkTrxMoAttribute85SecAutoDetectLinkBreakPostCond(String trxLdn, int noOfRetries) {
      Map<String, String> attributes = new HashMap<String,String>();
      attributes.put(MomHelper.ADMINISTRATIVE_STATE, MomHelper.UNLOCKED);
      attributes.put(MomHelper.OPERATIONAL_STATE, MomHelper.ENABLED);
      attributes.put(MomHelper.AVAILABILITY_STATUS, null);
      attributes.put(MomHelper.ABIS_TRXC_OML_STATE, MomHelper.DOWN);
      attributes.put(MomHelper.ABIS_TRX_RSL_STATE, MomHelper.UP);
      attributes.put(MomHelper.ABIS_TX_MO_STATE, MomHelper.DISABLED);     
      return waitForMoAttributes(trxLdn, attributes, noOfRetries);
  }

  public String checkTrxMoAttribute85SecAutoDetectLinkBreakRslDown(String trxLdn, int noOfRetries) {
      Map<String, String> attributes = new HashMap<String,String>();
      attributes.put(MomHelper.ADMINISTRATIVE_STATE, MomHelper.UNLOCKED);
      attributes.put(MomHelper.OPERATIONAL_STATE, MomHelper.ENABLED);
      attributes.put(MomHelper.AVAILABILITY_STATUS, null);
      attributes.put(MomHelper.ABIS_TRXC_OML_STATE, MomHelper.UP);
      attributes.put(MomHelper.ABIS_TRX_RSL_STATE, MomHelper.DOWN);
      attributes.put(MomHelper.ABIS_TX_MO_STATE, MomHelper.ENABLED);      
      return waitForMoAttributes(trxLdn, attributes, noOfRetries);
  }
  
  public String checkTrxMoAttribute85SecAutoDetectLinkBreakRslOmlUp(String trxLdn, int noOfRetries) {
      Map<String, String> attributes = new HashMap<String,String>();
      attributes.put(MomHelper.ADMINISTRATIVE_STATE, MomHelper.UNLOCKED);
      attributes.put(MomHelper.OPERATIONAL_STATE, MomHelper.ENABLED);
      attributes.put(MomHelper.AVAILABILITY_STATUS, null);
      attributes.put(MomHelper.ABIS_TRXC_OML_STATE, MomHelper.UP);
      attributes.put(MomHelper.ABIS_TRX_RSL_STATE, MomHelper.UP);
      attributes.put(MomHelper.ABIS_TX_MO_STATE, MomHelper.ENABLED);      
      return waitForMoAttributes(trxLdn, attributes, noOfRetries);
  }

  public String checkGsmSectorMoAttributeScfOmlUp(String gsmSectorLdn, int noOfRetries) {
    Map<String, String> attributes = new HashMap<String,String>();
    attributes.put(MomHelper.ABIS_SCF_OML_STATE, MomHelper.UP);
    return waitForMoAttributes(gsmSectorLdn, attributes, noOfRetries);
}
  public String checkMoOperationalState(String moLdn, int noOfRetries) {
	    Map<String, String> attributes = new HashMap<String,String>();
	    attributes.put(MomHelper.OPERATIONAL_STATE, MomHelper.ENABLED);
	    return waitForMoAttributes(moLdn, attributes, noOfRetries);
	}
  
    /**
     * Check the state of all MOs in the TG to verify the expected state
     * after complete start.
     * @param tgLdns
     * @return
     */
    public String checkAllTgMosAfterCompleteStart(TgLdns tgLdns) {
        AbiscoConnection abisco = new AbiscoConnection();
        String res;
        res = checkGsmSectorMoAttributeAfterAllMosStartedAoAtAndAoTfEnabled(tgLdns.sectorLdn, 10);
        if (!res.equals("")) return res;

        res = checkAbisIpMoAttributeAfterUnlockBscConnected(tgLdns.abisIpLdn, abisco.getBscIpAddress(), 10);
        if (!res.equals("")) return res;
        
        for (String trxLdn: tgLdns.trxLdnList) {
            res = checkTrxMoAttributeAfterUnlockAllEnabledLinksEstablished(trxLdn, 10);
            if (!res.equals("")) return res;
        }        
        return ""; // All good
    }
  
    /**
     * Check the state of all MOs after a complete start to see that the node
     * is up and running properly
     * @param tgLdnsList List of TgLdns representing the GRAT MOs in the node
     * @return The string describing the failed check
     */      
    public String checkAllGratMosAfterCompleteStart(List<TgLdns> tgLdnsList) {
        String res;
        for (TgLdns tg: tgLdnsList) {
            res = checkAllTgMosAfterCompleteStart(tg);
            if (!res.equals("")) return res;
        }
        return ""; // All checks ok
    }
  
    /**
     * Check the state of all MOs after a complete start to see that the node
     * is up and running properly
     * @param numOfTgs
     * @param numOfTrxsPerTg
     * @return The string describing the failed check
     */
    public String checkAllGratMosAfterCompleteStart(int numOfTgs, int numOfTrxsPerTg) {
        List<TgLdns> tgLdnsList = createTgLdnsList(numOfTgs, numOfTrxsPerTg);
        return checkAllGratMosAfterCompleteStart(tgLdnsList);
    }
    
    /**
     * @name createGsmSectorMos
     * 
     * @description Method for creating GsmSector MO:s.
     * 
     * @param noOfSectors - int the number of sectors to create
     * 
     * @return ArrayList<String> - a list of created TRX MO LDN.
     * 
     * @throws ConnectionException
     */   
    public ArrayList<String> createGsmSectorMos(int noOfSectors) throws ConnectionException {
       
        String sectorLdn;
        ArrayList<String> sectorLdnList = new ArrayList<String>();
        
        for (int i = 1; i < noOfSectors + 1; i++) {
            
            sectorLdn = String.format("%s,GsmSector=%s", BTS_FUNCTION_LDN, Integer.toString(i));
            logger.info("Create GsmSector MO with LDN " + sectorLdn);
            
            createGsmSectorMo(sectorLdn);
            sectorLdnList.add(sectorLdn);
        }
        
        return sectorLdnList;
    }
    
    /**
     * @name createGsmSectorMo
     * 
     * @description Method for creating one GsmSector MO.
     * 
     * @param sectorLdn       - String the LDN of the GsmSector that shall be created
     * 
     * @return void
     * 
     * @throws ConnectionException
     */   
    public void createGsmSectorMo(String sectorLdn) throws ConnectionException {
        logger.info("Create GsmSector MO with LDN " + sectorLdn);
        
        ManagedObject sectorMo = buildGsmSectorMo(sectorLdn);
        createManagedObject(sectorMo);
        
        // Put delete of the GsmSector MO on the stack
        restoreStack.add(new DeleteMoCommand(sectorLdn));
    }
    
    /**
     * @name buildGsmSectorMo
     * 
     * @description Method for creating one GsmSector MO Java object. No MO is created on the node.
     * 
     * @param sectorLdn       - String the LDN of the GsmSector that shall be created
     * 
     * @return ManagedObject - The MO 
     * 
     */   
    public ManagedObject buildGsmSectorMo(String gsmSectorLdn) {    	
        ManagedObject gsmSectorMo = new ManagedObject(gsmSectorLdn);
 
        return gsmSectorMo;
    }

    /**
     * @name createAbisIpMo
     *
     * @description Method for creating a locked AbisIp MO. Will use the default
     *              AbisIp LDN.
     * 
     * @param connectionName - Name of el2tp connection (gsmSectorName)
     * @param bscIpAddress - IP address of Abisco (BSC simulator)
     * @param initialRedirect Flag indicating if the Abisco shall imitate the BSC with an initial IP address redirection
     * 
     * @return String - the LDN of the AbisIp MO
     * 
     * @throws ConnectionException - Thrown if commit fails or not possible to
     *             connect to/with moHandler.
     */
    public String createAbisIpMo(final String connectionName, final String bscIpAddress, boolean initialRedirect)
            throws ConnectionException {
        return createAbisIpMo(connectionName, bscIpAddress, ABIS_IP_LDN, initialRedirect);
    }

    /**
     * @name createTnAndAbisIpMo
     *
     * @description Method for creating a locked AbisIp MO. Will use the default
     *              AbisIp LDN.
     * 
     * @param connectionName - Name of el2tp connection (gsmSectorName)
     * @param bscIpAddress - IP address of Abisco (BSC simulator)
     * @param initialRedirect Flag indicating if the Abisco shall imitate the BSC with an initial IP address redirection
     * 
     * @return String - the LDN of the AbisIp MO
     * 
     * @throws ConnectionException - Thrown if commit fails or not possible to
     *             connect to/with moHandler.
     */
    public String createTnAndAbisIpMo(final String connectionName, final String bscIpAddress, boolean initialRedirect)
            throws ConnectionException {
        if (!isTnConfigured())
        	configureTn(bscIpAddress);
        return createAbisIpMo(connectionName, bscIpAddress, ABIS_IP_LDN, initialRedirect);
    }

    /**
     * @name createAbisIpMo
     * 
     * @description Method for creating an locked AbisIp MO.
     * 
     * @param connectionName - Name of el2tp connection (gsmSectorName)
     * @param bscIpAddress - IP address of Abisco (BSC simulator)
     * @param abisIpLdn - the desired Ldn of the new AbisIp MO
     * @param initialRedirect Flag indicating if the Abisco shall imitate the BSC with an initial IP address redirection
     * 
     * @return String - the LDN of the AbisIp MO
     * 
     * @throws ConnectionException - Thrown if commit fails or not possible to
     *             connect to/with moHandler.
     */
    public String createAbisIpMo(final String connectionName, final String bscIpAddress, final String abisIpLdn, boolean initialRedirect)
            throws ConnectionException {
        logger.info("Create AbisIP MO");

        if (!isTnConfigured())
        	configureTn(bscIpAddress);
        ManagedObject abisMo = new ManagedObject(abisIpLdn);
        if(!connectionName.isEmpty())
        {
        	addAbisIpAttribute(abisMo, AbisIpAttribute.gsmSectorName, connectionName);
        }
        addAbisIpAttribute(abisMo, AbisIpAttribute.bscBrokerIpAddress, bscIpAddress);
        addAbisIpAttribute(abisMo, AbisIpAttribute.ipv4Address, TNA_IP_LDN);
        addAbisIpAttribute(abisMo, AbisIpAttribute.dscpSectorControlUL, "46");
        addAbisIpAttribute(abisMo, AbisIpAttribute.administrativeState, LOCKED);

        createManagedObject(abisMo);
                
        //Put lock and delete of AbisIp MO on the stack
        restoreStack.add(new LockDeleteMoCommand(abisIpLdn));
    
        return abisIpLdn;
    }
    
    /**
     * @name createAbisIpMo
     * 
     * @description Method for creating an locked AbisIp MO.
     * 
     * @param connectionName - Name of el2tp connection (gsmSectorName)
     * @param bscIpAddress - IP address of Abisco (BSC simulator)
     * @param abisIpLdn - the desired Ldn of the new AbisIp MO
     * 
     * @return String - the LDN of the AbisIp MO
     * 
     * @throws ConnectionException - Thrown if commit fails or not possible to
     *             connect to/with moHandler.
     */
    public String createAbisIpMo(final String connectionName, final String bscIpAddress, final String abisIpLdn)
            throws ConnectionException {
        logger.info("Create AbisIP MO");

        if (!isTnConfigured())
            configureTn(bscIpAddress);
        ManagedObject abisMo = new ManagedObject(abisIpLdn);
        if(!connectionName.isEmpty())
        {
            addAbisIpAttribute(abisMo, AbisIpAttribute.gsmSectorName, connectionName);
        }
        addAbisIpAttribute(abisMo, AbisIpAttribute.bscBrokerIpAddress, bscIpAddress);
        addAbisIpAttribute(abisMo, AbisIpAttribute.ipv4Address,TNA_IP_LDN);
        addAbisIpAttribute(abisMo, AbisIpAttribute.dscpSectorControlUL, "46");
        addAbisIpAttribute(abisMo, AbisIpAttribute.administrativeState, LOCKED);

        createManagedObject(abisMo);
                
        //Put lock and delete of AbisIp MO on the stack
        restoreStack.add(new LockDeleteMoCommand(abisIpLdn));
    
        return abisIpLdn;
    }
    
    /**
     * @name createAbisIpMoOnly
     * 
     * @description Method for creating an locked AbisIp MO. No other MO is created by this method.
     * 
     * @param connectionName - Name of el2tp connection (gsmSectorName)
     * @param bscIpAddress - IP address of Abisco (BSC simulator)
     * @param abisIpLdn - the desired Ldn of the new AbisIp MO
     * 
     * @return String - the LDN of the AbisIp MO
     * 
     * @throws ConnectionException - Thrown if commit fails or not possible to
     *             connect to/with moHandler.
     */
    public String createAbisIpMoOnly(final String connectionName, final String bscIpAddress, final String abisIpLdn)
            throws ConnectionException {
    	
    	return createAbisIpMoOnly(connectionName, bscIpAddress, abisIpLdn, TNA_IP_LDN, LOCKED);
    }

    /**
     * @name createAbisIpMoOnly
     * 
     * @description Method for creating an locked AbisIp MO. No other MO is created by this method.
     * 
     * @param connectionName - Name of el2tp connection (gsmSectorName)
     * @param bscIpAddress - IP address of Abisco (BSC simulator)
     * @param abisIpLdn - the desired Ldn of the new AbisIp MO
     * @param tnaIpLdn  - the ldn to the TN AddressIpV4 MO 
     * @param adminState - the administrative state of the mo shall have at creation
     * 
     * @return String - the LDN of the AbisIp MO
     * 
     * @throws ConnectionException - Thrown if commit fails or not possible to
     *             connect to/with moHandler.
     */
    public String createAbisIpMoOnly(final String connectionName, 
    								 final String bscIpAddress, 
    								 final String abisIpLdn, 
    								 final String tnaIpLdn,
    								 final String adminState)
            throws ConnectionException {
    	
        logger.info("Create AbisIP MO");
        
        ManagedObject abisMo = buildAbisIpMo(connectionName, 
				                             bscIpAddress,
				                             abisIpLdn,
				                             tnaIpLdn,
				                             adminState);
        createManagedObject(abisMo);
                
        // Put lock and delete of AbisIp MO on the stack
        restoreStack.add(new LockDeleteMoCommand(abisIpLdn));
    
        return abisIpLdn;
    }
    
    /**
     * @name buildAbisIpMo
     * 
     * @description Method for building an AbisIp MO. Only the Java object is built, no MO is created on the node
     * 
     * @param connectionName - Name of el2tp connection (gsmSectorName)
     * @param bscIpAddress - IP address of Abisco (BSC simulator)
     * @param abisIpLdn - the desired Ldn of the new AbisIp MO
     * @param tnaIpLdn  - the ldn to the TN AddressIpV4 MO 
     * @param adminState - the administrative state of the mo shall have at creation
     * 
     * @return ManagedObject - The AbisIp MO
     * 
     */
    public ManagedObject buildAbisIpMo(
    		final String connectionName, 
       		final String bscIpAddress, 
    		final String abisIpLdn, 
    		final String tnaIpLdn,
    		final String adminState) {
        ManagedObject abisMo = new ManagedObject(abisIpLdn);

        addAbisIpAttribute(abisMo, AbisIpAttribute.gsmSectorName, connectionName);
        addAbisIpAttribute(abisMo, AbisIpAttribute.bscBrokerIpAddress, bscIpAddress);
        addAbisIpAttribute(abisMo, AbisIpAttribute.ipv4Address, tnaIpLdn);
        addAbisIpAttribute(abisMo, AbisIpAttribute.dscpSectorControlUL, "46");
        addAbisIpAttribute(abisMo, AbisIpAttribute.administrativeState, adminState);
        
        return abisMo;
    }

    /**
     * @name createAbisIpMoForSector
     * 
     * @description Method for creating an locked AbisIP MO.
     * 
     * @param sectorLdn - sector LDN under wich to create AbisIp MO
     * @param connectionName - Name of el2tp connection (gsmSectorName)
     * @param bscIpAddress - IP address of Abisco (BSC simulator)
     * @param initialRedirect Flag indicating if the Abisco shall imitate the BSC with an initial IP address redirection
     * 
     * @return String - the LDN of the AbisIp MO
     * 
     * @throws ConnectionException - Thrown if commit fails or not possible to
     *             connect to/with moHandler.
     */
    public String createAbisIpMoForSector(final String sectorLdn, final String connectionName, final String bscIpAddress, boolean initialRedirect)
            throws ConnectionException {
        if (!isTnConfigured())
        	configureTn(bscIpAddress);
    	String abisIpLdn = sectorLdn + ",AbisIp=1";
    	logger.info("Create AbisIP MO");
        
        ManagedObject abisMo = buildAbisIpMo(
        		connectionName, 
                bscIpAddress,
                abisIpLdn,
                TNA_IP_LDN,
                LOCKED);
    	
        createManagedObject(abisMo);
                
        // Put lock and delete of AbisIp MO on the stack
        restoreStack.add(new LockDeleteMoCommand(abisIpLdn));
    
        return abisIpLdn;
    }
    
    /**
     * @name createTrxMo
     * 
     * @description Method for creating a locked Trx MO.
     * 
     * @param parentLdn - the String LDN of the parent (GsmSector MO)
     * @param trxLdnId - Final part of LDN ...,Trx=trxLdnId
     * 
     * @return String - the LDN of the created MO
     * 
     * @throws ConnectionException
     */
    public String createTrxMo(String parentLdn, String trxLdnId) throws ConnectionException {
    	return createTrxMo(parentLdn, trxLdnId, sectorEquipmentFunctionLdnToUse);
    }
    
    /**
     * @name createTrxMo
     * 
     * @description Method for creating a locked Trx MO.
     * 
     * @param parentLdn - the String LDN of the parent (GsmSector MO)
     * @param trxLdnId - Final part of LDN ...,Trx=trxLdnId
     * @param sectorEqFuncLdn - the String for sectorEquipmentFunctionRef
     * 
     * @return String - the LDN of the created MO
     * 
     * @throws ConnectionException
     */
    public String createTrxMo(String parentLdn, String trxLdnId, String sectorEqFuncLdn) throws ConnectionException {
        String trxLdn = String.format("%s,Trx=%s", parentLdn, trxLdnId);
        logger.info("Create Trx MO with LDN " + trxLdn + " SectorEquipmentFunctionRef " + sectorEqFuncLdn);
    	
    	ManagedObject trxMo = buildTrxMo(trxLdn, LOCKED, sectorEqFuncLdn);
   
    	createManagedObject(trxMo);
    	
        // Put lock and delete of Trx on the stack
        restoreStack.add(new LockDeleteMoCommand(trxLdn));
        
        return trxLdn;
    }
    
    /**
     * @name buildTrxMo
     * 
     * @description Method for creating a Trx MO Java object, it does not create the MO on the node.
     * 
     * @param trxLdnId -  the String LDN of the Trx that shall be created
     * @param adminState - The administrative state of the MO
     * @param sectorEqFuncLdn - the String for sectorEquipmentFunctionRef
     *      
     * @return ManagedObject - the Trx MO
     * 
     */
    
    public ManagedObject buildTrxMo(String trxLdn, String adminState, String sectorEqFuncLdn) {
        ManagedObject trxMo = new ManagedObject(trxLdn);
        addTrxAttribute(trxMo, TrxAttribute.administrativeState, adminState);
        addTrxAttribute(trxMo, TrxAttribute.txPower, trxParams.getTxPower());   // Gets converted to 25% on CGAI
        addTrxAttribute(trxMo, TrxAttribute.arfcnMin, trxParams.getArfcnMin());
        addTrxAttribute(trxMo, TrxAttribute.arfcnMax, trxParams.getArfcnMax());   // Gives a BW of 10 MHz
        addTrxAttribute(trxMo, TrxAttribute.frequencyBand, trxParams.getFrequencyBand());
        addTrxAttribute(trxMo, TrxAttribute.noOfRxAntennas, trxParams.getNoOfRxAntennas());
        addTrxAttribute(trxMo, TrxAttribute.noOfTxAntennas, trxParams.getNoOfTxAntennas());
        trxMo.addAttribute(new ManagedObjectValueStringAttribute(SECTOR_EQUIP_FUNC_REF, sectorEqFuncLdn));
        
        return trxMo;   	
    }
    
    public ManagedObject buildTrxMo(String trxLdn, String adminState, String sectorEqFuncLdn, int txPower) {
        ManagedObject trxMo = new ManagedObject(trxLdn);
        addTrxAttribute(trxMo, TrxAttribute.administrativeState, adminState);
        addTrxAttribute(trxMo, TrxAttribute.txPower, txPower);   // Gets converted to 25% on CGAI
        addTrxAttribute(trxMo, TrxAttribute.arfcnMin, trxParams.getArfcnMin());
        addTrxAttribute(trxMo, TrxAttribute.arfcnMax, trxParams.getArfcnMax());   // Gives a BW of 10 MHz
        addTrxAttribute(trxMo, TrxAttribute.frequencyBand, trxParams.getFrequencyBand());
        addTrxAttribute(trxMo, TrxAttribute.noOfRxAntennas, trxParams.getNoOfRxAntennas());
        addTrxAttribute(trxMo, TrxAttribute.noOfTxAntennas, trxParams.getNoOfTxAntennas());
        trxMo.addAttribute(new ManagedObjectValueStringAttribute(SECTOR_EQUIP_FUNC_REF, sectorEqFuncLdn));
        
        return trxMo;       
    }
    
    /**
     * @name getMo
     * 
     * @description Gets MO by LDN
     * 
     * @param ldn - the String LDN of the MO to get.
     * @return ManagedObject - the ManagedObject identified by ldn.
     */
    private ManagedObject getMo(String ldn)
    {
        ManagedObject mo = null;
        
        moHandler.connect();
        mo = moHandler.getManagedObject(ldn);
        return mo;
    }
   	/**
     * @name deleteSyncMos
     * 
     * @description Deletes the MOs that enables sync. The MOs will be restored after test case execution.
     */
    public void deleteSyncMos()
    {
        logger.info("Remove MOs " + RADIO_EQUIP_CLOCK + " and " + RADIO_EQUIP_CLOCK_REF + " and add CreateMoCommands to Restore Stack");
        
        // Get MO's to restore after test
        ManagedObject radioEquipClockMo = getMo(RADIO_EQUIP_CLOCK);
        List<ManagedObjectAttribute> radioEquipClockMoAttributes = new ArrayList<ManagedObjectAttribute>();
        radioEquipClockMoAttributes = radioEquipClockMo.getAttributes();
        
        logger.info("RADIO_EQUIP_CLOCK attributes: " + radioEquipClockMoAttributes.toString());
        // Remove all the readonly attributes wich cannot be set during creation
        // hardcoded by now, but we need to find a way to get this info preferably from MOM. 
        Iterator <ManagedObjectAttribute> it = radioEquipClockMoAttributes.iterator();
        while (it.hasNext())
        {
        	ManagedObjectAttribute tmp = it.next();
        	if(tmp.toString().contains("clockOperQuality"))
        		it.remove();
        	else if(tmp.toString().contains("currentRadioClockReference"))
        		it.remove();
        	else if(tmp.toString().contains("radioClockPriorityTable"))
        		it.remove();
        	else if(tmp.toString().contains("radioClockState"))
        		it.remove();
        }
        logger.info("RADIO_EQUIP_CLOCK attributes: " + radioEquipClockMoAttributes.toString());
        

        ManagedObject radioEquipClockRefMo = getMo(RADIO_EQUIP_CLOCK_REF);
        List<ManagedObjectAttribute> radioEquipClockRefMoAttributes = new ArrayList<ManagedObjectAttribute>();
        radioEquipClockRefMoAttributes = radioEquipClockRefMo.getAttributes();

        logger.info("RADIO_EQUIP_CLOCK_REF attributes: " + radioEquipClockRefMoAttributes.toString());
        it = radioEquipClockRefMoAttributes.iterator();
        while (it.hasNext())
        {
        	ManagedObjectAttribute tmp = it.next();
        	if(tmp.toString().contains("availabilityStatus"))
        		it.remove();
        	else if(tmp.toString().contains("operationalState"))
        		it.remove();
        	else if(tmp.toString().contains("operQualityLevel"))
        		it.remove();
        	else if(tmp.toString().contains("referenceStatus"))
        		it.remove();
        	else if(tmp.toString().contains("syncRefType"))
        		it.remove();
        }
        logger.info("RADIO_EQUIP_CLOCK_REF attributes: " + radioEquipClockRefMoAttributes.toString());
        
        deleteMo(RADIO_EQUIP_CLOCK);
        
        // Add CreateMoCommands to the Restore Stack with the mandatory attribute values
        restoreStack.add(new CreateMoCommand(RADIO_EQUIP_CLOCK_REF, radioEquipClockRefMoAttributes));
        restoreStack.add(new CreateMoCommand(RADIO_EQUIP_CLOCK, radioEquipClockMoAttributes));
    }
    
    /**
     * @name waitForAbisScfState
     * 
     * @description Method to get the abisScfState for GsmSector MO.
     * 
     * @param expected The expected value
     * 
     * @return boolean true if the expected value was found, else false
     */
    public boolean waitForAbisScfState(String expected) {
        return waitForMoAttributeStringValue(SECTOR_LDN, ABIS_SO_SCF_STATE, expected, 5);
    }
    
    public boolean waitForAbisScfOmlState(String expected) {
        return waitForMoAttributeStringValue(SECTOR_LDN, ABIS_SCF_OML_STATE, expected, 5);
    }
    
    /**
     * @name waitForAbisTfMoState
     * 
     * @description Method to get the abisTfMoState for GsmSector MO.
     * 
     * @param expected The expected value
     * 
     * @return boolean true if the expected value was found, else false
     */
    public boolean waitForAbisTfMoState(String expected) {
        return waitForMoAttributeStringValue(SECTOR_LDN, ABIS_TF_MO_STATE, expected, 5);
    }
    
    /**
     * @name waitForAbisAtMoState
     * 
     * @description Method to get the abisAtMoState for Trx MO.
     * 
     * @param expected The expected value
     * 
     * @return boolean true if the expected value was found, else false
     */
    public boolean waitForAbisAtMoState(String expected) {
        return waitForMoAttributeStringValue(SECTOR_LDN, ABIS_AT_MO_STATE, expected, 5);
    }

    /**
     * @name waitForAbisTrxcState
     * 
     * @description Method to get the abisTrxcState for Trx MO.
     * 
     * @param expected The expected value
     * 
     * @return boolean true if the expected value was found, else false
     */
    public boolean waitForAbisTrxcState(String expected) {
        return waitForMoAttributeStringValue(TRX_LDN, ABIS_SO_TRXC_STATE, expected, 10);
    }
    public boolean waitForAbisTrxcState(int trxId, String expected) {
        String trxLdn = SECTOR_LDN + ",Trx=" + trxId;
        return waitForMoAttributeStringValue(trxLdn, ABIS_SO_TRXC_STATE, expected, 10);
    }
    
    public boolean waitForAbisTrxcOmlState(String expected) {
        return waitForMoAttributeStringValue(TRX_LDN, ABIS_TRXC_OML_STATE, expected, 60);
    }

    /**
     * @name waitForAbisTxMoState
     * 
     * @description Method to get the abisTxMoState for Trx MO.
     * 
     * @param expected The expected value
     * 
     * @return boolean true if the expected value was found, else false
     */
    public boolean waitForAbisTxMoState(String expected) {
        return waitForMoAttributeStringValue(TRX_LDN, ABIS_TX_MO_STATE, expected, 10);
    }
    public boolean waitForAbisTxMoState(int trxId, String expected) {
        String trxLdn = SECTOR_LDN + ",Trx=" + trxId;
        return waitForMoAttributeStringValue(trxLdn, ABIS_TX_MO_STATE, expected, 10);
    }
    
    /**
     * @name waitForAbisRxMoState
     * 
     * @description Method to get the abisRxMoState for Trx MO.
     * 
     * @param expected The expected value
     * 
     * @return boolean true if the expected value was found, else false
     */
    public boolean waitForAbisRxMoState(String expected) {
        return waitForMoAttributeStringValue(TRX_LDN, ABIS_RX_MO_STATE, expected, 10);
    }
    public boolean waitForAbisRxMoState(int trxId, String expected) {
        String trxLdn = SECTOR_LDN + ",Trx=" + trxId;
        return waitForMoAttributeStringValue(trxLdn, ABIS_RX_MO_STATE, expected, 10);
    }
    
    
    /**
     * @name waitForAbisTsMoState
     * @param tsInstance
     * @param expected the expected value to get
     * @param attempts the number of attempts to get the value
     * @return true if the expected value was found, false otherwise
     * @throws JSONException 
     */
    public boolean waitForAbisTsMoState(int tsInstance, String expected, int attempts) throws JSONException  {
        return waitForAbisTsMoStateGeneral(TRX_LDN, tsInstance, expected, attempts);
     }
    public boolean waitForAbisTsMoState(int trxId, int tsInstance, String expected, int attempts) throws JSONException  {
        String trxLdn = SECTOR_LDN + ",Trx=" + trxId;
        return waitForAbisTsMoStateGeneral(trxLdn, tsInstance, expected, attempts);
     }
    
    /**
     * @name waitForAbisTsMoState
     * @param trxLdn
     * @param tsInstance
     * @param expected the expected value to get
     * @param attempts the number of attempts to get the value
     * @return true if the expected value was found, false otherwise
     * @throws JSONException 
     */
    public boolean waitForAbisTsMoStateGeneral(String trxLdn, int tsInstance, String expected, int attempts) throws JSONException  {
        String tmpTsState = "";
        // Wait for maximum attempts * 1s for desired operational state
        for (int i = 0; i < attempts; i++) {
            moHandler.connect();
            ManagedObject trxMo = moHandler.getManagedObject(trxLdn);
            ManagedObjectAttribute attribute = trxMo.getAttribute(ABIS_TS_MO_STATE);

            if (expected.isEmpty()) return attribute == null;
            if (attribute == null)  return expected.isEmpty(); // To avoid NullPointerException
            
            JSONArray jArray = new JSONArray(attribute.getValue());
            tmpTsState = (String)jArray.get(tsInstance);
            if (expected.equals(tmpTsState)) return true;
            
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                logger.info("Sleep got interrupted");
            }
        }
        logger.info("MO " + TRX_LDN + " abisTsMoState for timeslot " + tsInstance + " did not reach the expected value. Got: " + tmpTsState + ", expected: " + expected);
        return false;
    }
    
    /**
     * Get the TNA IP address for the RBS.
     * 
     * @return String address
     */
    public String getTnaIpAddress() {
        return tnaIpAddress;
    }

    /**
     * Get default router IP address.
     * 
     * @return Default Router IP Address
     */
    public String getDefaultRouterIpAddress() {
        return defaultRouterIpAddress;
    }
    
    /**
     * @name createMoWithAttributes
     * 
     * @description General method for creating MO. Mainly used by the SynchStartup test.
     * 
     * @param moLdn
     * @param attributes
     * @throws ManagedObjectException
     * @throws ConnectionException
     */
    public void createMoWithAttributes(String moLdn, List<ManagedObjectAttribute> attributes) 
            throws ManagedObjectException, ConnectionException {
        
        try {
            ManagedObject mo = new ManagedObject(moLdn);
            if (attributes != null) {
                for (ManagedObjectAttribute attr : attributes) {
                    mo.addAttribute(attr);
                }
            }
            moHandler.connect();
            moHandler.createManagedObject(mo);
            moHandler.commit();
            
            restoreStack.add(new LockDeleteMoCommand(moLdn));
        }
        catch (ManagedObjectException e) {
            logger.warn("The Create MO with Attributes operation could not be done on " + moLdn + " due to " + e.getMessage());
            throw e;
        }
    }

    /**
     * @name createMoWithoutAttributes
     * 
     * @description General method for creating MO. Mainly used by the TnConfigurator.
     * 
     * @param moLdn
     * @param useRestoreStack
     * @throws ManagedObjectException
     * @throws ConnectionException
     */
    public void createMoWithoutAttributes(String moLdn, boolean useRestoreStack) 
            throws ManagedObjectException, ConnectionException {
        
        try {
            ManagedObject mo = new ManagedObject(moLdn);
            moHandler.connect();
            moHandler.createManagedObject(mo);
            moHandler.commit();
            
            if (useRestoreStack)
            	restoreStack.add(new DeleteMoCommand(moLdn));
        }
        catch (ManagedObjectException e) {
            logger.warn("The Create MO with Attributes operation could not be done on " + moLdn + " due to " + e.getMessage());
            throw e;
        }
    }

    /*
     * This method return true if Bts is configured for time sync
     */
    public boolean isTimeSyncActive(){
        moHandler.connect();
        ManagedObject radioEquipmentReferenceClockMo = moHandler.getManagedObject(RADIO_EQUIP_CLOCK_REF); 
        ManagedObjectValueAttribute encapsulation = (ManagedObjectValueAttribute)radioEquipmentReferenceClockMo.getAttribute("encapsulation");
        return encapsulation.getValue().contains("TimeSyncIO");
    }

    /*
     * This method deletes an MO of type RadioEquipmentClockReference
     * The attributes of the old MO are used to recreate a new MO instance with the same attributes as the old one. 
     */
    public void deleteRadioEquipmentClockReferenceMo(String radioEquipmentReferenceClockLdn) {
        moHandler.connect();
        ManagedObject radioEquipmentReferenceClockMo = moHandler.getManagedObject(radioEquipmentReferenceClockLdn); 
        ManagedObjectValueAttribute encapsulation = (ManagedObjectValueAttribute)radioEquipmentReferenceClockMo.getAttribute("encapsulation");
        ManagedObjectValueAttribute priority = (ManagedObjectValueAttribute)radioEquipmentReferenceClockMo.getAttribute("priority");
        ManagedObjectValueAttribute administrativeState = (ManagedObjectValueAttribute)radioEquipmentReferenceClockMo.getAttribute("administrativeState");
        ManagedObjectStructAttribute adminQualityLevel = (ManagedObjectStructAttribute)radioEquipmentReferenceClockMo.getAttribute("adminQualityLevel");
        radioEquipmentClockReferenceAttributes = new ArrayList<ManagedObjectAttribute>();
        radioEquipmentClockReferenceAttributes.add(encapsulation);
        radioEquipmentClockReferenceAttributes.add(priority);
        radioEquipmentClockReferenceAttributes.add(adminQualityLevel);
        radioEquipmentClockReferenceAttributes.add(administrativeState);
        moHandler.deleteManagedObject(radioEquipmentReferenceClockLdn);
        moHandler.commit();
        createRadioEquipmentReferenceClock = new CreateMoCommand(radioEquipmentReferenceClockLdn, radioEquipmentClockReferenceAttributes);
        String rercEncapsulation = encapsulation.getValue();
        if (rercEncapsulation.contains("FrequencySyncIO")) {
            frequencySyncIoLdn = rercEncapsulation;
        } else if (rercEncapsulation.contains("TimeSyncIO")) {
            timeSyncIoLdn = rercEncapsulation;
        }
        restoreStack.add(createRadioEquipmentReferenceClock);
    }
    
    /*
     * This method creates an MO of type RadioEquipmentClockReference
     * The attributes of the old MO are used to recreate a new MO instance with the same attributes as the old one. 
     */
    public void createRadioEquipmentClockReferenceMo(String radioEquipmentReferenceClockLdn, String encapsulationLdn) {
        ManagedObject radioEquipmentReferenceClockMo = new ManagedObject(radioEquipmentReferenceClockLdn);
        for (ManagedObjectAttribute attribute : radioEquipmentClockReferenceAttributes) {
            if (attribute.getName().contains("encapsulation")) {
                radioEquipmentReferenceClockMo.addAttribute(new ManagedObjectValueAttribute("encapsulation", encapsulationLdn));
            } else {
                radioEquipmentReferenceClockMo.addAttribute(attribute);
            }
        }
        moHandler.connect();
        moHandler.createManagedObject(radioEquipmentReferenceClockMo);
        moHandler.commit();
        deleteRadioEquipmentReferenceClock = new LockDeleteMoCommand(radioEquipmentReferenceClockLdn);
        restoreStack.add(deleteRadioEquipmentReferenceClock);
    }
    
    /*
     * This method deletes an MO of type FrequencySyncIO
     * The attributes of the old MO are used to recreate a new MO instance with the same attributes as the old one.
     * Attribute syncPortEncapsulation is returned because it is needed in createAndDeleteTimeSyncIOMo 
     */
    public void deleteFrequencySyncIoMo() {
        moHandler.connect();
        ManagedObject frequencySyncIo = moHandler.getManagedObject(frequencySyncIoLdn); 
        syncPortEncapsulation = (ManagedObjectValueAttribute)frequencySyncIo.getAttribute("encapsulation");
        frequencySyncIoAttributes = new ArrayList<ManagedObjectAttribute>();
        frequencySyncIoAttributes.add(syncPortEncapsulation);
        moHandler.deleteManagedObject(frequencySyncIoLdn);
        moHandler.commit();
        createFrequencySyncIo = new CreateMoCommand(frequencySyncIoLdn, frequencySyncIoAttributes);
        restoreStack.add(createFrequencySyncIo);
    }
    
    /*
     * createAndDeleteTimeSyncIoMo
     */
    public void createTimeSyncIoMo(String timeSyncIoLdn) {
        ManagedObject timeSyncIoMo = new ManagedObject(timeSyncIoLdn);
        timeSyncIoMo.addAttribute(syncPortEncapsulation);
        moHandler.connect();
        moHandler.createManagedObject(timeSyncIoMo);
        moHandler.commit();
        deleteTimeSyncIo = new DeleteMoCommand(timeSyncIoLdn);
        restoreStack.add(deleteTimeSyncIo);
        syncPortEncapsulation = null;
    }
    
    /*
     * createAndDeleteTimeSyncIoMo
     */
    public void deleteTimeSyncIOMo(String timeSyncIoLdn) {
        moHandler.connect();
        moHandler.deleteManagedObject(timeSyncIoLdn);
        moHandler.commit();
        restoreStack.remove(deleteTimeSyncIo);
        deleteTimeSyncIo = null;
    }
    
    public void createFrequencySyncIoMo() {
        ManagedObject frequencySyncIoMo = new ManagedObject(frequencySyncIoLdn);
        for (ManagedObjectAttribute attribute : frequencySyncIoAttributes) {
        	frequencySyncIoMo.addAttribute(attribute);
        }
        moHandler.connect();
        moHandler.createManagedObject(frequencySyncIoMo);
        moHandler.commit();
        restoreStack.remove(createFrequencySyncIo);
        createFrequencySyncIo = null;
        frequencySyncIoAttributes = null;
    }
    
    /**
     * @name createManagedObject
     * 
     * @description Helper method for creating a ManagedObject. Will open
     *              connection and close after creating, so we know for sure the
     *              data is flushed.
     * 
     * @param mo - The Managed Object to create
     * @throws ConnectionException
     */
    public void createManagedObject(ManagedObject mo) { //throws ConnectionException {
        moHandler.connect();
        moHandler.createManagedObject(mo);
        moHandler.commit();
    }
        
    /**
     * @name addAbisIpAttribute
     * 
     * @description Helper method for adding attribute to mo.
     * 
     * @param mo - MO to add attribute to.
     * @param attribute - Attribute enum to add.
     * @param value - Attribute value to set.
     */
    private void addAbisIpAttribute(ManagedObject mo, AbisIpAttribute attribute, String value) {
        mo.addAttribute(new ManagedObjectValueAttribute(attribute.name(), value));
    }

    /**
     * @name addTrxAttribute
     * 
     * @description Helper method for adding attribute to mo.
     * 
     * @param mo - MO to add attribute to.
     * @param attribute - Attribute enum to add.
     * @param value - Attribute value to set.
     *
     */
    private void addTrxAttribute(ManagedObject mo, TrxAttribute attribute, int value) {
    	addTrxAttribute(mo, attribute, new Integer(value).toString());
    }

    /**
     * @name addTrxAttribute
     * 
     * @description Helper method for adding attribute to mo.
     * 
     * @param mo - MO to add attribute to.
     * @param attribute - Attribute enum to add.
     * @param value - Attribute value to set.
     *
     */
    private void addTrxAttribute(ManagedObject mo, TrxAttribute attribute, String value) {
        mo.addAttribute(new ManagedObjectValueAttribute(attribute.name(), value));
    }
    
    /**
     * Create a list of TgLdns objects representing
     * the specified GRAT MO structure.
     * @param numOfTgs
     * @param numOfTrxsPerTg
     * @return
     */
    public List<TgLdns> createTgLdnsList(int numOfTgs, int numOfTrxsPerTg) {
        List<TgLdns> tgLdnsList = new ArrayList<>();
        for (int tgId = 0; tgId < numOfTgs; ++tgId) {
            TgLdns tg = new TgLdns();
            tg.sectorLdn = SECTOR_LDN_NO_ID + tgId;
            tg.abisIpLdn = tg.sectorLdn + ",AbisIp=1";
            for (int trx = 0; trx < numOfTrxsPerTg; ++trx) {
                tg.trxLdnList.add(tg.sectorLdn + ",Trx=" + trx);
            }
            tgLdnsList.add(tg);
        }
        return tgLdnsList;
    }
   
    /**
     * Find all GRAT-related MOs under BtsFunction=1
     * @return a list of TgLdns objects representing the different TGs (GsmSectors)
     */
    public List<TgLdns> findGratMos() {
        List<TgLdns> result = new ArrayList<>();
        moHandler.connect();

        List<String> sectorLdnList = moHandler.findManagedObjectChildren(BTS_FUNCTION_LDN, "^GsmSector");
        Collections.sort(sectorLdnList); // Should not be necessary, but still nice to know it's sorted
        for (String sector: sectorLdnList) {
            logger.info("Found GsmSector with LDN = " + sector);
            TgLdns tg = new TgLdns();
            tg.sectorLdn = sector;
            
            // Find AbisIp MO
            List<String> abisIpLdnList = moHandler.findManagedObjectChildren(sector, "^AbisIp");
            if (abisIpLdnList.size() != 1) {
                tg.abisIpLdn = "";
                if (abisIpLdnList.size() > 1) {
                    logger.warn("Unexpected number of AbisIp MOs found under " + sector);
                    for (String abisIpLdn: abisIpLdnList) logger.warn("Found AbisIp with LDN " + abisIpLdn);
                }
            } else {
                logger.info("Found AbisIp with LDN = " + abisIpLdnList.get(0));
                tg.abisIpLdn = abisIpLdnList.get(0);
            }
            
            // Find TRX MOs
            List<String> trxLdnList = moHandler.findManagedObjectChildren(sector, "^Trx");
            Collections.sort(trxLdnList); // Should not be necessary, but still nice to know it's sorted
            for (String trxLdn: trxLdnList) {
                logger.info("Found TRX with LDN = " + trxLdn);
                tg.trxLdnList.add(trxLdn);
            }
            result.add(tg);
        }
        return result;
    }

    /**
     * @name isTnConfigured
     * 
     * @description Helper method for checking TN MO's.
     * 
     */
    public boolean isTnConfigured() {
        moHandler.connect();
        
        if (!moHandler.managedObjectExists(TNA_IP_LDN)) {
        	return false;
        }
        
    	ManagedObject ethMo;

    	try {
        	ethMo = moHandler.getManagedObject(ETH_PORT_LDN);
        } catch (Exception e){
        	return false;
        }
        
        logger.info("isTnConfigured: Content of the eth MO: " + ethMo.toString());
        
        if (ethMo.getAttribute("operationalState") == null)
        {
        	logger.info("isTnConfigured: Content of ethMo.getAttribute(operationalState) == null, this indicates that TN is not well, reboot node");
        }
                
        if (ethMo.getAttribute("operationalState").toString().contains("DISABLED"))
        	return false;
    	
        return true;
    }
    
    /**
     * @name restartDu
     * 
     * @description Helper method for restarting DU via MO-action.
     *              This one needs to be implemented (and tested) instead of CliCommands.reboot()
     */
	public void restartDu() {
		logger.info( "Rebooting node, by performing action " + RESTART_DU_ACTION + " on " + FRU_DU_LDN);
		ManagedObjectStructAttribute actionParameters = new ManagedObjectStructAttribute(RESTART_DU_ACTION);
		actionParameters.addMember(new ManagedObjectValueAttribute( "restartRank", RESTART_DU_RANK_COLD));
		actionParameters.addMember(new ManagedObjectValueAttribute( "restartReason", RESTART_DU_REASON));
		actionParameters.addMember(new ManagedObjectValueAttribute( "restartInfo", RESTART_DU_INFO));
		try {
			moHandler.connect();
			ActionResult actionResult = moHandler.performAction( FRU_DU_LDN, RESTART_DU_ACTION, actionParameters );
			moHandler.commit();
			logger.info( "Result of the Manual Restart Action: " + actionResult.getResult() );
		} catch(Exception e) {
			logger.warn( "Exception: performAction :: " + e.getMessage());
			moHandler.commit();
		}
	}
	
	public void enableSnmp(){
		logger.info("begin enable snmp");
		moHandler.connect();
		if (moHandler.getManagedObject(SNMP_LDN).getAttribute("operationalState").getValue().contains("ENABLED") ) 
		{
			logger.info("snmp's operationalState is enabled");
			return;
		}
		lockMo(SNMP_LDN);
		String stpName = rbs.getName().toLowerCase();
		logger.debug("STP:"+stpName);
		String stpIp = stpConfigHelper.getLmtIpAddress();
		ManagedObjectStructAttribute agentAddress = new ManagedObjectStructAttribute("agentAddress");
		agentAddress.addMember(new ManagedObjectValueStringAttribute("host", stpIp));
		agentAddress.addMember(new ManagedObjectValueIntegerAttribute("port", 1161));
		moHandler.connect();
		moHandler.setAttribute(SNMP_LDN, agentAddress);	
		moHandler.commit();
		unlockMo(SNMP_LDN);
		waitForOperationalState(SNMP_LDN, "ENABLED", 5);
		logger.info("Successfully enable snmp");
	}
	
	public void addRestoreCmd(RestoreCommand cmd) {
		restoreStack.add(cmd);
	}
	
	public void removeRestoreCmd(RestoreCommand cmd) {
		restoreStack.remove(cmd);
	}
	
	public int getFrequencyBandFromTrxParams() {
	    return trxParams.getFrequencyBand();
	}
	
	public int getArfcnMinFromTrxParams() {
	    return trxParams.getArfcnMin();
	}

	public int getArfcnMaxFromTrxParams() {
	    return trxParams.getArfcnMax();
	}
	
	public int getTxPowerFromTrxParams() {
	    return trxParams.getTxPower();
	}
	
	
	public int getArfcnToUse(){
		return trxParams.getArfcnToUse();
	}
	
	public int getArfcnToUseTg(int tgId){
		return stpConfigHelper.getAssignedArfcnTg(tgId);
	}
	
	public Band getGsmBandToUse() {
	    return trxParams.getGsmBandToUse();
	}
	
	public MSBand getMsBandToUse() {
	    return trxParams.getMsBandToUse();
	}
    
	public String getTgBandToUse() {
	    return trxParams.getTgBandToUse();
	}
	
	public String getBscBrokerIpAddress() {
	    return getAttributeValue(ABIS_IP_LDN, "bscBrokerIpAddress");
	}
	
	public String getPeerIpAddress(String abisIpLdn) {
	    return getAttributeValue(ABIS_IP_LDN, PEER_IP_ADDRESS);
	}

	public static void setSectorEquipmentFunctionLdn(String ldn) {
        sectorEquipmentFunctionLdnToUse = ldn;
    }
	public String getSectorEquipmentFunctionLdn() {
        return sectorEquipmentFunctionLdnToUse;
    }
	
    /**
     * @name checkgsmSectorName
     * 
     * @description Method to check whether gsmSectorName is set to last20char of  gsmSectorId.
     * 
     * @param expected The expected value
     * 
     * @return boolean true if the expected value was found, else false
     */
	public boolean checkgsmSectorName(String sectorName) {
		if(sectorName.isEmpty()){
			String str = getAttributeValue(SECTOR_LDN, "gsmSectorId");
	        if(str.length()>=20){      
	        	return getAttributeValue(ABIS_IP_LDN, "gsmSectorName").equals(str.substring(str.length()-20, str.length()));
	        }           	
			return getAttributeValue(ABIS_IP_LDN, "gsmSectorName").equals(str);
		}
		return getAttributeValue(ABIS_IP_LDN, "gsmSectorName").equals(sectorName);
	}
}
    

