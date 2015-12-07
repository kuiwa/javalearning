package com.ericsson.msran.test.grat.lockunlockabisip;


import java.util.ArrayList;
import java.util.List;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import se.ericsson.jcat.fw.annotations.Setup;
import se.ericsson.jcat.fw.annotations.Teardown;

import com.ericsson.abisco.clientlib.servers.OM_G31R01;
import com.ericsson.abisco.clientlib.servers.OM_G31R01.Enums;
import com.ericsson.commonlibrary.faultmanagement.com.ComAlarm;
import com.ericsson.commonlibrary.managedobjects.ManagedObjectAttribute;
import com.ericsson.commonlibrary.managedobjects.ManagedObjectValueAttribute;
import com.ericsson.commonlibrary.remotecli.Cli;
import com.ericsson.commonlibrary.resourcemanager.Rm;
import com.ericsson.msran.g2.annotations.TestInfo;
import com.ericsson.msran.test.grat.testhelpers.AbisHelper;
import com.ericsson.msran.test.grat.testhelpers.AbiscoConnection;
import com.ericsson.msran.test.grat.testhelpers.AlarmHelper;
import com.ericsson.msran.test.grat.testhelpers.LogHandler;
import com.ericsson.msran.test.grat.testhelpers.MomHelper;
import com.ericsson.msran.test.grat.testhelpers.NodeStatusHelper;
import com.ericsson.msran.jcat.TestBase;

/**
 * @id NodeUC459, NodeUC461
 *  
 * @name LockUnlockAbisIpTest
 * 
 * @author GRAT 2014
 * 
 * @created 2014-03-21
 * 
 * @description Unlock and lock the AbisIp MO.
 * 
 * @revision ewegans 2014-03-21 first version
 *           ewegans 2014-06-16 more thorough testing
 * 
 */
public class LockUnlockAbisIpTest extends TestBase {
    private MomHelper momHelper;
    private AbiscoConnection abisco;    
    private String abisIpLdn;
    private Cli cli;
    private LogHandler logHandler;
    private static String sectorLdn = MomHelper.SECTOR_LDN; // ManagedElement=1,BtsFunction=1,GsmSector=1
    private String trxLdn;
    private NodeStatusHelper nodeStatus;
    private AbisHelper abisHelper;
    private AlarmHelper alarmHelper;

    /**
     * Description of test case for test reporting
     */
    @TestInfo(
            tcId = "NodeUC459,NodeUC461,NodeUC523",
            slogan = "Unlock AbisIp MO - Lock AbisIp MO",
            requirementDocument = "-",
            requirementRevision = "-",
            requirementLinkTested = "-",
            requirementLinkLatest = "-",
            requirementIds = { "-" },
            verificationStatement = "Verifies NodeUC459, NodeUC461,NodeUC523",
            testDescription = "Verifies unlocking and locking of the AbisIp MO",
            traceGuidelines = "N/A")   

    /**
     * Precheck.
     */
    @Setup
    public void setup() {
        setTestStepBegin("Start of setup()");
        nodeStatus = new NodeStatusHelper();
        assertTrue("Node did not reach a working state before test start", nodeStatus.waitForNodeReady());
        momHelper = new MomHelper();
        abisco = new AbiscoConnection();
        logHandler = new LogHandler();
        abisHelper = new AbisHelper();
        alarmHelper = new AlarmHelper();
        abisco.setupAbisco(false);
        setTestStepEnd();
    }

    /**
     * Postcheck.
     */
    @Teardown
    public void teardown() {
    	setTestStepBegin("Start of teardown()");
        nodeStatus.isNodeRunning();
        setTestStepEnd();
    }

    private void checkLockedAbisIp() {
        setTestInfo("Check: AbisIp administrativeState = LOCKED, operationalState = DISABLED, and availabilityStatus = OFFLINE");
        assertEquals("AbisIp MO attributes did not reach expected values", "", momHelper.checkAbisIpMoAttributeAfterLock(abisIpLdn, 5));
        setTestInfo("Check: OML Layer 2 link is not established for SCF, GsmSector,abisScfOmlState = DOWN");
        assertEquals("GsmSector MO attributes did not reach expected values", "", momHelper.checkGsmSectorMoAttributeAfterCreateNoLinks(sectorLdn, 5));
    }

    private void checkUnlockedAbisIpNotConnected() {
        setTestInfo("Check: AbisIp administrativeState = UNLOCKED, operationalState = DISABLED, availabilityStatus = FAILED, peerIpAddress = empty");
        assertEquals("AbisIp MO attributes did not reach expected values", "", momHelper.checkAbisIpMoAttributeAfterUnlockBscDisconnected(abisIpLdn, 5));
        setTestInfo("Check: OML Layer 2 link is not established for SCF, GsmSector,abisScfOmlState = DOWN");
        assertEquals("GsmSector MO attributes did not reach expected values", "", momHelper.checkGsmSectorMoAttributeAfterCreateNoLinks(sectorLdn, 20));
    }

    private void checkUnlockedAbisIpOmlDown() {
        setTestInfo("Check: AbisIp administrativeState = UNLOCKED, operationalState = ENABLED, availabilityStatus = empty, peerIpAddress = " + abisco.getBscIpAddress());
        assertEquals("AbisIp MO attributes did not reach expected values", "", momHelper.checkAbisIpMoAttributeAfterUnlockBscConnected(abisIpLdn, abisco.getBscIpAddress(), 30));
        setTestInfo("Check: OML Layer 2 link is established for SCF, GsmSector,abisScfOmlState = DOWN");
        assertEquals("GsmSector MO attributes did not reach expected values", "", momHelper.checkGsmSectorMoAttributeAfterCreateNoLinks(sectorLdn, 5));
    }
    
    private void checkUnlockedAbisIpOmlDownAndOffline() {
        setTestInfo("Check: AbisIp administrativeState = UNLOCKED, operationalState = DISABLED, availabilityStatus = FAILED, peerIpAddress = " + abisco.getBscIpAddress());
        assertEquals("AbisIp MO attributes did not reach expected values", "", momHelper.checkAbisIpMoAttributeAfterUnlockBscWithRedirect(abisIpLdn, 30, null));
        setTestInfo("Check: OML Layer 2 link is established for SCF, GsmSector,abisScfOmlState = DOWN");
        assertEquals("GsmSector MO attributes did not reach expected values", "", momHelper.checkGsmSectorMoAttributeAfterCreateNoLinks(sectorLdn, 5));
    }

    private void checkUnlockedAbisIpOmlUp() {
        setTestInfo("Check: AbisIp administrativeState = UNLOCKED, operationalState = ENABLED, availabilityStatus = empty, peerIpAddress = " + abisco.getBscIpAddress());
        assertEquals("AbisIp MO attributes did not reach expected values", "", momHelper.checkAbisIpMoAttributeAfterUnlockBscConnected(abisIpLdn, abisco.getBscIpAddress(), 30));
        setTestInfo("Check: OML Layer 2 link is established for SCF, GsmSector,abisScfOmlState = UP");
        assertEquals("GsmSector MO attributes did not reach expected values", "", momHelper.checkGsmSectorMoAttributeAfterCreateLinksEstablished(sectorLdn, 5));
    }
    
    private void checkUnlockedTrx() {
        setTestInfo("Check: Trx administrativeState = UNLOCKED, operationalState = ENABLED, availabilityStatus = empty");
        assertEquals("Trx MO attributes did not reach expected values", "", momHelper.checkTrxMoAttributeAfterUnlockNoLinks(trxLdn, 5));
    }
    
    private void checkLockedTrx() {
        setTestInfo("Check: Trx administrativeState = LOCKED, operationalState = DISABLED, availabilityStatus = OFF_LINE");
        assertEquals("Trx MO attributes did not reach expected values", "", momHelper.checkTrxMoAttributeAfterLock(trxLdn, 5));
    }    
    
    /**
     * @name unlockAbisIpWithRedirect
     * 
     * @description Unlock AbisIp and the BSC redirects the connection to alternative IP address.
     *              The links should be re-established on the alternative IP address.
     *              Verification according to NodeUC459.A1.
     * 
     * @param testId - unique identifier of the test case
     * @param description
     * 
     */
    @Test(timeOut = 300000)
    @Parameters({ "testId", "description" })
    public void unlockAbisIpWithRedirect(String testId, String description) {
        setTestCase(testId, description);
        cli = Rm.getG2RbsList().get(0).getLinuxShell(); //used only for no secure mode
        
    	if (!momHelper.isBtsFunctionCreated()) {
    		setTestInfo("Precondition: Create BtsFunction MO");
    		momHelper.createBtsFunctionMo(MomHelper.BTS_FUNCTION_LDN);
    	}
        
        abisIpLdn = sectorLdn + ",AbisIp=1";
        abisIpLdn = momHelper.createSectorAndAbisIpMo(AbiscoConnection.getConnectionName(), abisco.getAltPGWIP(), abisIpLdn, true);
        trxLdn = momHelper.createTrxMo(sectorLdn, "0"); 

        setTestInfo("Unlock AbisIp");
        momHelper.unlockMo(abisIpLdn);
 
        setTestInfo("***** AbisIp unlocked, check MO state and link state is DOWN");
        checkUnlockedAbisIpOmlDownAndOffline();

        setTestStepBegin("**** Unlock Trx");
        momHelper.unlockMo(trxLdn);
        checkUnlockedTrx();
        setTestStepEnd();

        setTestStepBegin(" Abisco Setup with redirect");
        abisco.setupAbisco(true);
        setTestStepEnd();
        
        setTestInfo("RRR Source address = " + momHelper.getAttributeValue("ManagedElement=1,Transport=1,Router=1,InterfaceIPv4=GSM,AddressIPv4=GSM", "address"));
        setTestInfo("RRR NextHop address = " + momHelper.getAttributeValue("ManagedElement=1,Transport=1,Router=1,RouteTableIPv4Static=1,Dst=1,NextHop=1", "address"));
        setTestInfo("RRR BscBrokerIpAddress = " + momHelper.getBscBrokerIpAddress());
        setTestInfo("RRR Destination network address = " + momHelper.getAttributeValue("ManagedElement=1,Transport=1,Router=1,RouteTableIPv4Static=1,Dst=1", "dst"));
        setTestInfo("RRR PeerIpAddress = " + momHelper.getPeerIpAddress(abisIpLdn));
        setTestInfo("RRR abisco.getBscIpAddress() = " + abisco.getBscIpAddress());
        setTestInfo("RRR abisco.getAltPGWIP() = " + abisco.getAltPGWIP());
        
        cli.send("route");
        cli.send("ip netns exec fib_2 traceroute " + abisco.getBscIpAddress());
        cli.send("ip netns exec fib_2 traceroute " + abisco.getAltPGWIP());

        setTestInfo("**** Establish links");
        try {
            abisco.establishLinks(false); //SCF
            setTestInfo("***** Abisco setup, check AbisIp MO state and link state is UP");
            checkUnlockedAbisIpOmlUp();
            assertTrue("Could not activate Abis Transport", abisHelper.startSectorMosAndActivateAT());
            abisco.establishLinks(true); //TRX
        } catch (InterruptedException ie) {
            fail("InteruptedException during establishLinks");
        }

        setTestInfo("RRR BscBrokerIpAddress = " + momHelper.getBscBrokerIpAddress());
        setTestInfo("RRR PeerIpAddress = " + momHelper.getPeerIpAddress(abisIpLdn));
        
        setTestStepBegin("Check pre-condition: AbisIp MO exists and has attribute administrativeState = UNLOCKED. GsmSector attribute abisScfOml = UP");
        assertEquals("AbisIp MO attributes did not reach expected values", "", momHelper.checkAbisIpMoAttributeAfterUnlockBscConnected(abisIpLdn, abisco.getBscIpAddress(), 30));
        assertEquals("GsmSector MO attributes did not reach expected values", "", momHelper.checkGsmSectorMoAttributeAfterAllMosStartedAoAtEnabled(sectorLdn, 5));
        assertFalse("peerIpAddress " + momHelper.getPeerIpAddress(abisIpLdn) + " must be different than BscBrokerIpAddress after a successfull redirect " + momHelper.getBscBrokerIpAddress(), momHelper.getPeerIpAddress(abisIpLdn).equals(momHelper.getBscBrokerIpAddress()));
        setTestStepEnd();
        
        setTestStepBegin("Check pre-condition: Trx MO exists and has attribute administrativeState = UNLOCKED. Trx has attribute abisTrxcOmlState = UP and abisTrxRslState = UP");
        assertEquals("Trx MO attributes did not reach expected values", "", momHelper.checkTrxMoAttributeAfterUnlockLinksEstablished(trxLdn, 5));
        setTestStepEnd();
        
        setTestStepBegin("Lock abisIp MO");
        momHelper.lockMo(abisIpLdn);
        setTestStepEnd();
        
        // Post-conditions
        setTestStepBegin("Check post-condition: AbisIp attributes administrativeState = LOCKED, operationalState = DISABLED, availState = OFF_LINE.");
        assertEquals("AbisIp MO attributes did not reach expected values", "", momHelper.checkAbisIpMoAttributeAfterLock(abisIpLdn, 5));
        setTestStepEnd();
        
        momHelper.lockMo(trxLdn);
        checkLockedTrx();   
        setTestStepBegin("Check post-condition: Sector attributes abisScfOmlState = DOWN.");
        assertEquals("GsmSector MO attributes did not reach expected values", "", momHelper.checkGsmSectorMoAttributeAfterAllMosStartedAoAtResetNoLinks(sectorLdn, 5));
        setTestStepEnd();               
    }
    
    /**
     * @name unlockAbisIpWithRedirectAndRevertToBrokerAfterStopCCN
     * 
     * @description Unlock AbisIp and one TRX with redirect from Broker IP address to alternative IP address.
     *              The links should be re-established on the alternative IP address. 
     *              Then let Abisco disconnect the links (StopCCN) which results in that radio node reverts to broker IP address.
     *              Establish ctrl connection using broker IP address which again results in a redirect to alternative IP address.
     *              Then establish links again on the alternative IP address.                                          
     *              Verification according to NodeUC459.A1, NodeUC524.A2.
     * 
     * @param testId - unique identifier of the test case
     * @param description
     * 
     */

    @Test(timeOut = 300000)        
    @Parameters({ "testId", "description" })
    public void unlockAbisIpWithRedirectAndRevertToBrokerAfterStopCCN(String testId, String description) 
    {
        setTestCase(testId, description);        
        
    	if (!momHelper.isBtsFunctionCreated()) {
    		setTestInfo("Precondition: Create BtsFunction MO");
    		momHelper.createBtsFunctionMo(MomHelper.BTS_FUNCTION_LDN);
    	}
    	
        abisIpLdn = momHelper.createSectorAndAbisIpMo(AbiscoConnection.getConnectionName(), abisco.getAltPGWIP(), true);        
    	trxLdn = momHelper.createTrxMo(sectorLdn, "0");    	
    
    	setTestStepBegin("**** Unlock AbisIp, Trx and establish links using redirected IP address.");    	    	
    	momHelper.unlockMo(abisIpLdn);              
    	momHelper.unlockMo(trxLdn);       	    	
        checkUnlockedTrx();
                
        setTestInfo("**** Abisco Setup with redirect");
        abisco.setupAbisco(true);        
                                    	
    	setTestInfo("**** Establish SCF OML and TRX OML&RSL links");
    	try {
            abisco.establishLinks();
            assertTrue("**** Could not activate Abis Transport", abisHelper.startSectorMosAndActivateAT());
            abisco.establishLinks(true);            
        } 
    	catch (InterruptedException ie) {
            fail("**** InteruptedException during establishLinks");        
    	}                                
                    
        assertEquals("**** AbisIp MO attributes did not reach expected values", "", momHelper.checkAbisIpMoAttributeAfterUnlockBscConnected(abisIpLdn, abisco.getBscIpAddress(), 30));
        assertEquals("**** GsmSector MO attributes did not reach expected values", "", momHelper.checkGsmSectorMoAttributeAfterAllMosStartedAoAtEnabled(sectorLdn, 5));
        assertEquals("**** Trx MO attributes did not reach expected values", "", momHelper.checkTrxMoAttributeAfterUnlockLinksEstablished(trxLdn, 5));        
        setTestStepEnd();
                
        setTestStepBegin("**** Disconnect links using StopCCN and then re-establish links to redirected IP address.");                         
        setTestInfo("**** Disconnect TG - StopCCN.");
        abisco.disconnectTG();
        
        try {        	
        	setTestInfo("**** Connect TG.");
            abisco.connectTG();
            
            setTestInfo("**** Verify that control connection is re-established back to redirected IP address.");
            assertEquals("AbisIp MO attributes did not reach expected values", "", momHelper.checkAbisIpMoAttributeAfterUnlockBscConnected(abisIpLdn, abisco.getBscIpAddress(), 30));            
        	
        	setTestInfo("**** Establish SCF OML link.");
            abisco.establishLinks();
            
        	setTestInfo("**** Send AT Bundling Info Request to give Abisco knowledge of current bundling setup.");
            abisHelper.atBundlingInfoRequest(1, 0);                        
            
            setTestInfo("**** Establish TRX OML&RSL links.");
            abisco.establishLinks(true);                  
            } catch (InterruptedException e) {
            fail("**** InterruptedException during establish links.");
            }
                                                    
       assertEquals("**** AbisIp MO attributes did not reach expected values", "", momHelper.checkAbisIpMoAttributeAfterUnlockBscConnected(abisIpLdn, abisco.getBscIpAddress(), 30));
       assertEquals("**** GsmSector MO attributes did not reach expected values", "", momHelper.checkGsmSectorMoAttributeAfterAllMosStartedAoAtEnabled(sectorLdn, 30));
       assertEquals("**** Trx MO attributes did not reach expected values", "", momHelper.checkTrxMoAttributeAfterUnlockLinksEstablished(trxLdn, 5));                    	            
       setTestStepEnd();                                  
    }

    
    /**
     * @name unlockAbisIpWithRedirectAndRevertToBrokerAfterLinkBreak
     * 
     * @description Unlock AbisIp and one TRX with redirect from Broker IP address to alternative IP address.
     *              The links should be re-established on the alternative IP address. 
     *              Then break the links which results in that radio node reverts to broker IP address.
     *              Establish ctrl connection using broker IP address which again results in a redirect to alternative IP address.
     *              Then establish links again on the alternative IP address.                            
     *              Verification according to NodeUC459.A1, NodeUC524.N.
     * 
     * @param testId - unique identifier of the test case
     * @param description
     * 
     */
    @Test(timeOut = 300000)        
    @Parameters({ "testId", "description" })
    public void unlockAbisIpWithRedirectAndRevertToBrokerAfterLinkBreak(String testId, String description) 
    {
    	String tnEthPort = MomHelper.ETH_PORT_LDN; //ManagedElement=1,Transport=1,EthernetPort=eth10
        setTestCase(testId, description);
                
    	if (!momHelper.isBtsFunctionCreated()) {
    		setTestInfo("Precondition: Create BtsFunction MO");
    		momHelper.createBtsFunctionMo(MomHelper.BTS_FUNCTION_LDN);
    	}
    	
        abisIpLdn = momHelper.createSectorAndAbisIpMo(AbiscoConnection.getConnectionName(), abisco.getAltPGWIP(), true);        
    	trxLdn = momHelper.createTrxMo(sectorLdn, "0");    	
    
    	setTestStepBegin("**** Unlock AbisIp, Trx and establish links using redirected IP address.");    	    	
    	momHelper.unlockMo(abisIpLdn);              
    	momHelper.unlockMo(trxLdn);       	    	
        checkUnlockedTrx();
                
        setTestInfo("**** Abisco Setup with redirect");
        abisco.setupAbisco(true);        
                                    	
    	setTestInfo("**** Establish SCF OML and TRX OML&RSL links");
    	try {
            abisco.establishLinks();
            assertTrue("**** Could not activate Abis Transport", abisHelper.startSectorMosAndActivateAT());
            abisco.establishLinks(true);            
        } 
    	catch (InterruptedException ie) {
            fail("**** InteruptedException during establishLinks");        
    	}                                
                    
        assertEquals("**** AbisIp MO attributes did not reach expected values", "", momHelper.checkAbisIpMoAttributeAfterUnlockBscConnected(abisIpLdn, abisco.getBscIpAddress(), 30));
        assertEquals("**** GsmSector MO attributes did not reach expected values", "", momHelper.checkGsmSectorMoAttributeAfterAllMosStartedAoAtEnabled(sectorLdn, 5));
        assertEquals("**** Trx MO attributes did not reach expected values", "", momHelper.checkTrxMoAttributeAfterUnlockLinksEstablished(trxLdn, 5));        
        setTestStepEnd();
                
        setTestStepBegin("**** Break links and then re-establish links to redirected IP address.");
        
        // Lock TF MO (Ethernet port)
        setTestInfo("**** Lock TF MO");
        momHelper.lockMo(tnEthPort);                        

        // Verify that the link break has been detected by checking MO status attributes
        // AbisIp MO attributes:
        //     administrativeState = UNLOCKED
        //     operationalState    = DISABLED 
        //     availabilityStatus  = FAILED 
        //     peerIpAddress       = <empty>
        // GsmSector MO attribute:
        //     abisScfOmlState = DOWN
        // Trx MO attributes:
        //     abisTrxRslState = DOWN
        //     abisTrxcOmlState = DOWN
        setTestStepBegin("**** Verify the link break");
        assertEquals("**** AbisIp MO attributes did not reach expected values", "", momHelper.checkAbisIpMoAttributeAfterUnlockBscDisconnected(abisIpLdn, 15));
        assertEquals("**** GsmSector MO attributes did not reach expected values", "", momHelper.checkGsmSectorMoAttributeAfterAllMosStartedAoAtEnabledNoLinks(sectorLdn, 5));
        assertEquals("**** Trx MO attributes did not reach expected values", "", momHelper.checkTrxMoAttributeAfterUnlockNoLinks(trxLdn, 5));            
        
        setTestInfo("**** Disconnect TG");
        abisco.disconnectTG();
        
        // Unlock TF MO
        setTestInfo("**** Unlock TF MO");
        momHelper.unlockMo(tnEthPort);        
        
        try {
        	setTestInfo("**** Connect TG.");
            abisco.connectTG();
            
            setTestInfo("**** Verify that control connection is re-established back to redirected IP address.");
            assertEquals("AbisIp MO attributes did not reach expected values", "", momHelper.checkAbisIpMoAttributeAfterUnlockBscConnected(abisIpLdn, abisco.getBscIpAddress(), 30));            
        	
        	setTestInfo("**** Establish SCF OML link.");
            abisco.establishLinks();
            
        	setTestInfo("**** Send AT Bundling Info Request to give Abisco knowledge of current bundling setup.");
            abisHelper.atBundlingInfoRequest(1, 0);                        
            
            setTestInfo("**** Establish TRX OML&RSL links.");
            abisco.establishLinks(true);                  
            } catch (InterruptedException e) {
            fail("**** InterruptedException during establish links.");
            }
                                                    
       assertEquals("**** AbisIp MO attributes did not reach expected values", "", momHelper.checkAbisIpMoAttributeAfterUnlockBscConnected(abisIpLdn, abisco.getBscIpAddress(), 30));
       assertEquals("**** GsmSector MO attributes did not reach expected values", "", momHelper.checkGsmSectorMoAttributeAfterAllMosStartedAoAtEnabled(sectorLdn, 5));
       assertEquals("**** Trx MO attributes did not reach expected values", "", momHelper.checkTrxMoAttributeAfterUnlockLinksEstablished(trxLdn, 5));                    	            
       setTestStepEnd();                                  
    }

            
    /**
     * @name redirectOnTheFlyWithUnlockedAbisIp
     * 
     * @description Unlock AbisIp with establish links and then 
     *              let Abisco redirect the connection to another IP address.
     *              The links should be re-established.
     *              Verification according to NodeUC523.N.
     * 
     * @param testId - unique identifier of the test case
     * @param description
     * 
     */
    @Test(timeOut = 300000)    
    @Parameters({ "testId", "description" })
    public void redirectOnTheFlyWithUnlockedAbisIp(String testId, String description) 
    {
        setTestCase(testId, description);
        
    	if (!momHelper.isBtsFunctionCreated()) {
    		setTestInfo("Precondition: Create BtsFunction MO");
    		momHelper.createBtsFunctionMo(MomHelper.BTS_FUNCTION_LDN);
    	}
        
        abisIpLdn = momHelper.createSectorAndAbisIpMo(AbiscoConnection.getConnectionName(), abisco.getBscIpAddress(), true);
    
    	setTestStepBegin("**** Unlock AbisIp and establish links.");    	    	
    	momHelper.unlockMo(abisIpLdn);                  	
                                    	
    	setTestInfo("**** Establish SCF OML link");
    	try {
            abisco.establishLinks();                        
        } 
    	catch (InterruptedException ie) {
            fail("**** InteruptedException during establishLinks");        
    	}                                
                        	
        assertEquals("**** AbisIp MO attributes did not reach expected values", "", momHelper.checkAbisIpMoAttributeAfterUnlockBscConnected(abisIpLdn, abisco.getBscIpAddress(), 30));
        assertEquals("**** GsmSector MO attributes did not reach expected values", "", momHelper.checkGsmSectorMoAttributeAfterCreateLinksEstablished(sectorLdn, 5));                
        setTestStepEnd();
        
        setTestStepBegin("**** Abisco redirect on the fly.");        
        try {        
        	abisco.redirectToIpAddress(0, abisco.getAltPGWIP());
        	
            } catch (InterruptedException ie) {
            fail("**** InteruptedException during redirectToIpAddress");
            }
        setTestInfo("**** Verify that control connection is established to new IP address.");
        assertEquals("AbisIp MO attributes did not reach expected values", "", momHelper.checkAbisIpMoAttributeAfterUnlockBscConnected(abisIpLdn, abisco.getAltPGWIP(), 30));
                
        try {        	
            setTestInfo("**** Establish SCF OML link.");
            abisco.establishLinks();                                          
            } catch (InterruptedException e) {
            fail("**** InterruptedException during establish links.");
            }
                            
        setTestInfo("**** Verify that SCF OML is re-established after redirect.");                        
        assertEquals("**** GsmSector MO attributes did not reach expected values", "", momHelper.checkGsmSectorMoAttributeAfterCreateLinksEstablished(sectorLdn, 5));
        setTestInfo("RRR BscBrokerIpAddress = " + momHelper.getBscBrokerIpAddress());
        setTestInfo("RRR PeerIpAddress = " + momHelper.getPeerIpAddress(abisIpLdn));
        assertFalse("peerIpAddress " + momHelper.getPeerIpAddress(abisIpLdn) + " must be different than BscBrokerIpAddress after a successfull redirect " + momHelper.getBscBrokerIpAddress(), momHelper.getPeerIpAddress(abisIpLdn).equals(momHelper.getBscBrokerIpAddress()));
        setTestStepEnd();
        
        // Stop TSS since there is a fault in abisco with StopCCN 
        // after redirect has been performed
        abisco.stopTss();
    }
    
    /**
     * @name redirectOnTheFlyWithUnlockedAbisIpAndTrx
     * 
     * @description Unlock AbisIp and one TRX with establish links and then 
     *              let Abisco redirect the connection to another IP address.
     *              The links should be re-established.
     *              Verification according to NodeUC523.N.
     * 
     * @param testId - unique identifier of the test case
     * @param description
     * 
     */
    @Test(timeOut = 300000)    
    @Parameters({ "testId", "description" })
    public void redirectOnTheFlyWithUnlockedAbisIpAndTrx(String testId, String description) 
    {
        setTestCase(testId, description);
        
    	if (!momHelper.isBtsFunctionCreated()) {
    		setTestInfo("Precondition: Create BtsFunction MO");
    		momHelper.createBtsFunctionMo(MomHelper.BTS_FUNCTION_LDN);
    	}
        
        abisIpLdn = momHelper.createSectorAndAbisIpMo(AbiscoConnection.getConnectionName(), abisco.getBscIpAddress(), true);
    	trxLdn = momHelper.createTrxMo(sectorLdn, "0");    	
    
    	setTestStepBegin("**** Unlock AbisIp, Trx and establish links.");    	    	
    	momHelper.unlockMo(abisIpLdn);              
    	momHelper.unlockMo(trxLdn);       	    	
        checkUnlockedTrx();
                                    	
    	setTestInfo("**** Establish SCF OML and TRX OML&RSL links");
    	try {
            abisco.establishLinks();
            assertTrue("**** Could not activate Abis Transport", abisHelper.startSectorMosAndActivateAT());
            abisco.establishLinks(true);            
        } 
    	catch (InterruptedException ie) {
            fail("**** InteruptedException during establishLinks");        
    	}                                
                    
        assertEquals("**** AbisIp MO attributes did not reach expected values", "", momHelper.checkAbisIpMoAttributeAfterUnlockBscConnected(abisIpLdn, abisco.getBscIpAddress(), 30));
        assertEquals("**** GsmSector MO attributes did not reach expected values", "", momHelper.checkGsmSectorMoAttributeAfterAllMosStartedAoAtEnabled(sectorLdn, 5));
        assertEquals("**** Trx MO attributes did not reach expected values", "", momHelper.checkTrxMoAttributeAfterUnlockLinksEstablished(trxLdn, 5));        
        setTestStepEnd();
        
        setTestStepBegin("**** Abisco redirect on the fly.");        
        try {        
        	abisco.redirectToIpAddress(0, abisco.getAltPGWIP());
        	
            } catch (InterruptedException ie) {
            fail("**** InteruptedException during redirectToIpAddress");
            }
        setTestInfo("**** Verify that control connection is established to new IP address.");
        assertEquals("AbisIp MO attributes did not reach expected values", "", momHelper.checkAbisIpMoAttributeAfterUnlockBscConnected(abisIpLdn, abisco.getAltPGWIP(), 30));
                
        try {
        	setTestInfo("**** Establish SCF OML link.");
            abisco.establishLinks();
            
        	setTestInfo("**** Send AT Bundling Info Request to give Abisco knowledge of current bundling setup.");
            abisHelper.atBundlingInfoRequest(1, 0);                        
            
            setTestInfo("**** Establish TRX OML&RSL links.");
            abisco.establishLinks(true);                  
            } catch (InterruptedException e) {
            fail("**** InterruptedException during establish links.");
            }
                            
        setTestInfo("**** Verify that SCF OML and TRX OML&RSL are re-established after redirect.");                        
        assertEquals("**** GsmSector MO attributes did not reach expected values", "", momHelper.checkGsmSectorMoAttributeAfterAllMosStartedAoAtEnabled(sectorLdn, 5));
        assertEquals("**** Trx MO attributes did not reach expected values", "", momHelper.checkTrxMoAttributeAfterUnlockLinksEstablished(trxLdn, 5));
        setTestInfo("RRR BscBrokerIpAddress = " + momHelper.getBscBrokerIpAddress());
        setTestInfo("RRR PeerIpAddress = " + momHelper.getPeerIpAddress(abisIpLdn));
        assertFalse("peerIpAddress " + momHelper.getPeerIpAddress(abisIpLdn) + " must be different than BscBrokerIpAddress after a successfull redirect " + momHelper.getBscBrokerIpAddress(), momHelper.getPeerIpAddress(abisIpLdn).equals(momHelper.getBscBrokerIpAddress()));
        setTestStepEnd();
        
        // Stop TSS since there is a fault in abisco with StopCCN 
        // after redirect has been performed
        abisco.stopTss();
    }        
    
    /**
     * @name redirectOnTheFlyFailureWithUnlockedAbisIpAndTrx
     * 
     * @description Unlock AbisIp and one TRX with establish links and then 
     *              let Abisco redirect the connection to an undefined IP address.
     *              The links should be re-established back to broker IP address.
     *              Verification according to NodeUC523.N, GRAT Sequence: Failure when setting
     *              up primary control connection to a redirected IP address
     * 
     * @param testId - unique identifier of the test case
     * @param description
     * 
     */
    @Test(timeOut = 300000)            
    @Parameters({ "testId", "description" })
    public void redirectOnTheFlyFailureWithUnlockedAbisIpAndTrx(String testId, String description) 
    {
        setTestCase(testId, description);
        
    	if (!momHelper.isBtsFunctionCreated()) {
    		setTestInfo("Precondition: Create BtsFunction MO");
    		momHelper.createBtsFunctionMo(MomHelper.BTS_FUNCTION_LDN);
    	}
        
        abisIpLdn = momHelper.createSectorAndAbisIpMo(AbiscoConnection.getConnectionName(), abisco.getBscIpAddress(), true);
    	trxLdn = momHelper.createTrxMo(sectorLdn, "0");    	
    
    	setTestStepBegin("**** Unlock AbisIp, Trx and establish links.");    	    	
    	momHelper.unlockMo(abisIpLdn);              
    	momHelper.unlockMo(trxLdn);       	    	
        checkUnlockedTrx();
                                    	
    	setTestInfo("**** Establish SCF OML and TRX OML&RSL links");
    	try {
            abisco.establishLinks();
            assertTrue("**** Could not activate Abis Transport", abisHelper.startSectorMosAndActivateAT());
            abisco.establishLinks(true);            
        } 
    	catch (InterruptedException ie) {
            fail("**** InteruptedException during establishLinks");        
    	}                                
                    
        assertEquals("**** AbisIp MO attributes did not reach expected values", "", momHelper.checkAbisIpMoAttributeAfterUnlockBscConnected(abisIpLdn, abisco.getBscIpAddress(), 30));
        assertEquals("**** GsmSector MO attributes did not reach expected values", "", momHelper.checkGsmSectorMoAttributeAfterAllMosStartedAoAtEnabled(sectorLdn, 5));
        assertEquals("**** Trx MO attributes did not reach expected values", "", momHelper.checkTrxMoAttributeAfterUnlockLinksEstablished(trxLdn, 5));        
        setTestStepEnd();
        
        setTestStepBegin("**** Abisco redirect on the fly to undefined IP address.");        
        try {        
        	abisco.redirectToIpAddress(0, "1.2.3.4");
        	
            } catch (InterruptedException ie) {
            fail("**** InteruptedException during redirectToIpAddress");
            }
                       
        try {
        	setTestInfo("**** Connect TG.");
            abisco.connectTG(0);
            
            setTestInfo("**** Verify that control connection is re-established back to broker IP address instead.");
            assertEquals("AbisIp MO attributes did not reach expected values", "", momHelper.checkAbisIpMoAttributeAfterUnlockBscConnected(abisIpLdn, abisco.getBscIpAddress(), 30));            
        	
        	setTestInfo("**** Establish SCF OML link.");
            abisco.establishLinks();
            
        	setTestInfo("**** Send AT Bundling Info Request to give Abisco knowledge of current bundling setup.");
            abisHelper.atBundlingInfoRequest(1, 0);                        
            
            setTestInfo("**** Establish TRX OML&RSL links.");
            abisco.establishLinks(true);                  
            } catch (InterruptedException e) {
            fail("**** InterruptedException during establish links.");
            }
                            
        setTestInfo("**** Verify that SCF OML and TRX OML&RSL are re-established after redirect failure.");                        
        assertEquals("**** GsmSector MO attributes did not reach expected values", "", momHelper.checkGsmSectorMoAttributeAfterAllMosStartedAoAtEnabled(sectorLdn, 5));
        assertEquals("**** Trx MO attributes did not reach expected values", "", momHelper.checkTrxMoAttributeAfterUnlockLinksEstablished(trxLdn, 5));
        setTestInfo("RRR BscBrokerIpAddress = " + momHelper.getBscBrokerIpAddress());
        setTestInfo("RRR PeerIpAddress = " + momHelper.getPeerIpAddress(abisIpLdn));
        assertTrue("peerIpAddress " + momHelper.getPeerIpAddress(abisIpLdn) + " must be equal to the BscBrokerIpAddress after a failed redirect " + momHelper.getBscBrokerIpAddress(), momHelper.getPeerIpAddress(abisIpLdn).equals(momHelper.getBscBrokerIpAddress()));
        setTestStepEnd();     
        
        // Stop TSS since there is a fault in abisco with StopCCN 
        // after redirect has been performed
        abisco.stopTss();
    }
    
    /**
     * @name unlockLockUnlockAbisIp
     * 
     * @description Unlock and lock AbisIp multiple times before a 
     *              final unlock of AbisIp, the establish links.
     * 
     * @param testId - unique identifier of the test case
     * @param description
     * 
     */
    @Test(timeOut = 420000)
    @Parameters({ "testId", "description" })
    public void unlockLockUnlockAbisIp(String testId, String description) {
        setTestCase(testId, description);
        
    	if (!momHelper.isBtsFunctionCreated()) {
    		setTestInfo("Precondition: Create BtsFunction MO");
    		momHelper.createBtsFunctionMo(MomHelper.BTS_FUNCTION_LDN);
    	}
        
        abisIpLdn = momHelper.createSectorAndAbisIpMo(AbiscoConnection.getConnectionName(), abisco.getBscIpAddress(), false);
        setTestStepBegin("Check pre-conditions: AbisIp administrativeState = LOCKED, operationalState = DISABLED, and availabilityStatus = OFFLINE");
        assertEquals("AbisIp MO attributes did not reach expected values", "", momHelper.checkAbisIpMoAttributeAfterLock(abisIpLdn, 5));
        
        for (int i = 0; i < 5; ++i) {
            // Unlock-lock AbisIp
            setTestStepBegin("Unlock AbisIp");
            momHelper.unlockMo(abisIpLdn);
            setTestStepBegin("Lock AbisIp");
            momHelper.lockMo(abisIpLdn);
        }

        // Unlock AbisIp
        setTestStepBegin("Unlock AbisIp");
        momHelper.unlockMo(abisIpLdn);
        setTestStepBegin("Establish links");
        try {
            abisco.establishLinks();
        } catch (InterruptedException ie) {
            fail("InteruptedException during establishLinks");
        }
        
        setTestStepBegin("Post-conditions: AbisIp administrativeState = UNLOCKED, operationalState = ENABLED, and availabilityStatus = empty");
        assertEquals("AbisIp MO attributes did not reach expected values", "", momHelper.checkAbisIpMoAttributeAfterUnlockBscConnected(abisIpLdn, abisco.getBscIpAddress(), 30));
        setTestStepBegin("Post-condition: OML Layer 2 link is established for SCF, GsmSector,abisScfOmlState=UP");
        assertEquals("GsmSector MO attributes did not reach expected values", "", momHelper.checkGsmSectorMoAttributeAfterCreateLinksEstablished(sectorLdn, 5));       
    }
    
    /**
     * @name unlockLockAbisIp
     * 
     * @description Unlock and lock AbisIp with TRX MO created. First and third iterations
     *              lock abisIp with established links and TRX unlocked. Second iteration
     *              locks abisIp with established link but with TRX locked.              
     *              Verification according to NodeUC461.N and NodeUC461.A1.
     * 
     * @param testId - unique identifier of the test case
     * @param description
     * 
     */
    @Test(timeOut = 420000)
    @Parameters({ "testId", "description" })
    public void unlockLockAbisIp(String testId, String description) {
        setTestCase(testId, description);
        int iterations = 3;
        
    	if (!momHelper.isBtsFunctionCreated()) {
    		setTestInfo("Precondition: Create BtsFunction MO");
    		momHelper.createBtsFunctionMo(MomHelper.BTS_FUNCTION_LDN);
    	}        
        abisIpLdn = sectorLdn + ",AbisIp=1";
        abisIpLdn = momHelper.createSectorAndAbisIpMo(AbiscoConnection.getConnectionName(), abisco.getBscIpAddress(), abisIpLdn, false);
        trxLdn = momHelper.createTrxMo(sectorLdn, "0"); 
               
        for (int i = 1; i <= iterations; ++i) {            
            setTestStepBegin("Main loop iteration " + (i));
            
            setTestInfo("Unlock AbisIp");
            momHelper.unlockMo(abisIpLdn);
            setTestInfo("***** AbisIp unlocked, check MO state and link state is DOWN");
            checkUnlockedAbisIpOmlDown();
            setTestStepEnd();
            
            if(i % 2 == 1) {
                setTestStepBegin("**** Unlock Trx");
                momHelper.unlockMo(trxLdn);
                checkUnlockedTrx();
                setTestStepEnd();
            }
            
            setTestStepBegin(" Abisco Setup");
            abisco.setupAbisco(false);
            setTestInfo("***** Abisco setup, check AbisIp MO state and link state is DOWN");
            checkUnlockedAbisIpOmlDown();
            
        
            setTestInfo("**** Establish links");
            try {
                abisco.establishLinks();
            } catch (InterruptedException ie) {
                fail("InteruptedException during establishLinks");
            }
            assertTrue("Could not activate Abis Transport", abisHelper.startSectorMosAndActivateAT());
            
            if(i % 2 == 1) {
                try {
                    abisco.establishLinks(true);
                } catch (InterruptedException ie) {
                    fail("InteruptedException during establishLinks");
                }
            }
            
            setTestStepBegin("Check pre-condition: AbisIp MO exists and has attribute administrativeState = UNLOCKED. GsmSector attribute abisScfOml = UP");
            assertEquals("AbisIp MO attributes did not reach expected values", "", momHelper.checkAbisIpMoAttributeAfterUnlockBscConnected(abisIpLdn, abisco.getBscIpAddress(), 30));
            assertEquals("GsmSector MO attributes did not reach expected values", "", momHelper.checkGsmSectorMoAttributeAfterAllMosStartedAoAtEnabled(sectorLdn, 5));

            if(i % 2 == 1) {
                setTestStepBegin("Check pre-condition: Trx MO exists and has attribute administrativeState = UNLOCKED. Trx has attribute abisTrxcOmlState = UP and abisTrxRslState = UP");
                assertEquals("Trx MO attributes did not reach expected values", "", momHelper.checkTrxMoAttributeAfterUnlockLinksEstablished(trxLdn, 5));
                setTestStepEnd();
            }

            
            setTestStepBegin("Lock abisIp MO");
            momHelper.lockMo(abisIpLdn);

            // Post-conditions
            setTestStepBegin("Check post-condition: AbisIp attributes administrativeState = LOCKED, operationalState = DISABLED, availState = OFF_LINE.");
            assertEquals("AbisIp MO attributes did not reach expected values", "", momHelper.checkAbisIpMoAttributeAfterLock(abisIpLdn, 5));
            
            setTestStepBegin("Check post-condition: Sector attributes abisScfOmlState = DOWN.");
            assertEquals("GsmSector MO attributes did not reach expected values", "", momHelper.checkGsmSectorMoAttributeAfterAllMosStartedAoAtResetNoLinks(sectorLdn, 5));
            
            setTestStepBegin("Check post-condition: Trx attributes abisTrxcOmlState = DOWN, abisTrxRslState = DOWN.");
            if(i % 2 == 1) {
            	// Trx is unlocked
            	assertEquals("Trx MO attributes did not reach expected values", "", momHelper.checkTrxMoAttributeAfterUnlockNoLinks(trxLdn, 5));
            } else {
            	// Trx is locked
            	assertEquals("Trx MO attributes did not reach expected values", "", momHelper.checkTrxMoAttributeAfterLock(trxLdn, 5));
            }

            if(i % 2 == 1) {
                
                setTestStepBegin("Lock Trx MO");
                // Lock Trx MO for next iteration
                momHelper.lockMo(trxLdn);
                checkLockedTrx(); 
                setTestStepEnd();
            }
            
            // Clean-up for next iteration
            setTestStepBegin("Clean-up, reset MOs");
            momHelper.unlockMo(abisIpLdn);
            try {
                abisco.establishLinks();
                abisHelper.resetCommand(OM_G31R01.Enums.MOClass.AT);
                abisHelper.resetCommand(OM_G31R01.Enums.MOClass.TF);
                abisHelper.resetCommand(OM_G31R01.Enums.MOClass.SCF);
            } catch (InterruptedException ie) {
                fail("InteruptedException during establishLinks");
            }
            momHelper.lockMo(abisIpLdn);
            abisco.disconnectTG();
            abisco.connectTG();
        }
        
        // Clean-up
        setTestStepBegin("Delete MOs");
        momHelper.deleteMo(trxLdn); 
        momHelper.deleteMo(abisIpLdn);
        momHelper.deleteMo(sectorLdn);    
    }
    
    /**
     * @name unlockAbisIpNoBsc
     * 
     * @description Repeatedly unlock and lock AbisIp MO without a BSC connection.
     * 
     * @param testId - unique identifier of the test case
     * @param description
     *      
     */ 
    @Test(timeOut = 500000)
    @Parameters({ "testId", "description" })
    public void unlockAbisIpNoBsc(String testId, String description)
    {
        setTestCase(testId, description);
        
    	if (!momHelper.isBtsFunctionCreated()) {
    		setTestInfo("Precondition: Create BtsFunction MO");
    		momHelper.createBtsFunctionMo(MomHelper.BTS_FUNCTION_LDN);
    	}
        
        int iterations = 2;        
        // Create a connection to BSC that does not exist
        abisIpLdn = momHelper.createSectorAndAbisIpMo(AbiscoConnection.getConnectionName(), "123.123.123.123", false);
        
        //enable the log          
        final String traceGroup = "trace1";
        final String processName = "*/GSC*";        
        logHandler.teEnable(traceGroup, processName);
        
        setTestInfo("Number of iterations is " + iterations);
        for (int i = 1; i <= iterations; ++i)
        {
            setTestStepBegin("Iteration " + i);
            setTestStepBegin("Check pre-conditions: AbisIp administrativeState = LOCKED, operationalState = DISABLED, and availabilityStatus = OFFLINE");
            assertEquals("AbisIp MO attributes did not reach expected values", "", momHelper.checkAbisIpMoAttributeAfterLock(abisIpLdn, 5));
            
            // Unlock AbisIp
            setTestInfo("Unlock AbisIp");
            momHelper.unlockMo(abisIpLdn);    
            sleepSeconds(10);
            setTestStepEnd();

            if (i == iterations)
            {
              // WP3794, check alarm is raised
              setTestStepBegin("Check alarm is raised"); 
              // This sleep is here to ensure that the GRAT got long enough time to try to connect to the non-existing BSC
              sleepSeconds(65);   
              long timeoutInSeconds = 90; 
              List<ComAlarm> alarmList = alarmHelper.getAlarmList(timeoutInSeconds);                
              boolean isAlarmActive = alarmHelper.isBscDisconnectAlarm(alarmList, abisIpLdn);                          
              assertTrue(isAlarmActive);
              setTestStepEnd();
            }  
                 
            setTestStepBegin("Post-conditions: AbisIp administrativeState = UNLOCKED, operationalState = DISABLED, availabilityStatus = FAILED");
            assertEquals("AbisIp MO attributes did not reach expected values", "", momHelper.checkAbisIpMoAttributeAfterUnlockBscDisconnected(abisIpLdn, 5));
            setTestInfo("Post-condition: OML Layer 2 link is not established for SCF, GsmSector,abisScfOmlState = DOWN");
            assertEquals("GsmSector MO attributes did not reach expected values", "", momHelper.checkGsmSectorMoAttributeAfterCreateNoLinks(sectorLdn, 5));
            
            // Lock AbisIp
            setTestInfo("Lock AbisIp");
            momHelper.lockMo(abisIpLdn);
            setTestStepEnd();
            
            if (i == iterations)
            {
              // WP3794, check alarm is ceased
              setTestStepBegin("Check alarm is ceased"); 
              sleepSeconds(5);
              List<ComAlarm> alarmList = alarmHelper.getAlarmList();
              boolean isAlarmActive = alarmHelper.isBscDisconnectAlarm(alarmList, abisIpLdn);
              assertFalse(isAlarmActive);
              setTestStepEnd();
            }        
        }
        
        setTestStepBegin("Check AbisIp Mo Attributes");
        logHandler.teDisable(traceGroup, processName);        
        // Final post-check
        assertEquals("AbisIp MO attributes did not reach expected values", "", momHelper.checkAbisIpMoAttributeAfterLock(abisIpLdn, 5));
        setTestStepEnd();
    }  

    /**
     * @name unlockLockAbisIpNotConnected
     * 
     * @description Repeatedly unlock and lock AbisIp MO before connecting to BSC.
     * 
     * @param testId - unique identifier of the test case
     * @param description
     * 
     */ 
    @Test(timeOut = 700000)
    @Parameters({ "testId", "description" })
    public void unlockLockAbisIpNotConnected(String testId, String description) {
        setTestCase(testId, description);
        
    	if (!momHelper.isBtsFunctionCreated()) {
    		setTestInfo("Precondition: Create BtsFunction MO");
    		momHelper.createBtsFunctionMo(MomHelper.BTS_FUNCTION_LDN);
    	}
        
        abisIpLdn = momHelper.createSectorAndAbisIpMo(AbiscoConnection.getConnectionName(), abisco.getBscIpAddress(), false);
        abisco.disconnectTG();
        final int iterations = 2; // Do everything twice

        setSubTestcase("Unlock-lock", "Unlock-lock AbisIp without BSC connected");
        for (int i = 1; i <= iterations; i++) {
            setTestStepBegin("Iteration " + i);
            checkLockedAbisIp();

            setTestInfo("Unlock AbisIp and wait for 10 seconds");
            momHelper.unlockMo(abisIpLdn);
            // this sleep is here to allow grat to connect to the BSC although the BSC does not have any connected TG
            sleepSeconds(10);
            checkUnlockedAbisIpNotConnected();

            setTestInfo("Lock AbisIp");
            momHelper.lockMo(abisIpLdn);
        }

        setSubTestcase("Unlock-connect TG-lock", "Unlock AbisIp followed by connecting TG, then lock and disconnect");
        for (int i = 1; i <= iterations; i++) {
            setTestStepBegin("Iteration " + i);
            checkLockedAbisIp();

            setTestInfo("Unlock AbisIp and connect TG");
            momHelper.unlockMo(abisIpLdn);
            abisco.connectTG();
            checkUnlockedAbisIpOmlDown();

            setTestInfo("Lock AbisIp and disconnect TG");
            momHelper.lockMo(abisIpLdn);
            abisco.disconnectTG();
        }

        setSubTestcase("Unlock-connect TG-disconnect TG-lock", "Unlock AbisIp followed by connect TG, disconnect TG before locking AbisIp");
        for (int i = 1; i <= iterations; i++) {
            setTestStepBegin("Iteration " + i);
            checkLockedAbisIp();

            setTestInfo("Unlock AbisIp and connect TG");
            momHelper.unlockMo(abisIpLdn);
            abisco.connectTG();           
            setTestInfo("********************* TG connected, check AbisIp MO state");
            checkUnlockedAbisIpOmlDown();
            setTestInfo("********************* TG connected, AbisIp MO state as expected");
            setTestInfo("Disconnect TG");
            abisco.disconnectTG();
            setTestInfo("********************* TG disconnected, check AbisIp MO state");
            checkUnlockedAbisIpNotConnected();
            setTestInfo("********************* TG disconnected, AbisIp MO state as expected");

            setTestInfo("Lock AbisIp");
            momHelper.lockMo(abisIpLdn);
        }

        setSubTestcase("Unlock-connect TG-establish OML-lock", "Unlock AbisIp followed by connect TG and establish OML, then lock AbisIp och disconnect");
        for (int i = 1; i <= iterations; i++) {
            setTestStepBegin("Iteration " + i);
            checkLockedAbisIp();

            setTestInfo("Unlock AbisIp, connect TG and establish links");
            momHelper.unlockMo(abisIpLdn);
            abisco.connectTG();
            try {
                abisco.establishLinks();
            } catch (InterruptedException e) {
                fail("Could not establish links");
            }
            checkUnlockedAbisIpOmlUp();

            setTestInfo("Lock AbisIp and disconnect TG");
            momHelper.lockMo(abisIpLdn);
            abisco.releaseLinks();
            abisco.disconnectTG();
        }

        setSubTestcase("Unlock-connect TG-establish OML-disconnect TG-lock", "Unlock AbisIp, connect TG, establish links, then disconnect TG before locking AbisIp");
        for (int i = 1; i <= iterations; i++) {
            setTestStepBegin("Iteration " + i);
            checkLockedAbisIp();

            setTestInfo("************************ Unlock AbisIp, connect TG, establish links and disconnect TG");
            momHelper.unlockMo(abisIpLdn);
            abisco.connectTG();
            try {
                abisco.establishLinks();
            } catch (InterruptedException e) {
                fail("Could not establish links");
            }
            setTestInfo("********************* Established links, check AbisIp MO state");
            checkUnlockedAbisIpOmlUp();
            setTestInfo("********************* After Established links, AbisIp MO state is as expected");
            abisco.disconnectTG();
            setTestInfo("********************* TG disconnected, check AbisIp MO state");
            checkUnlockedAbisIpNotConnected();
            setTestInfo("********************* TG disconnected, AbisIp MO state is as expected");
            setTestInfo("Lock AbisIp");
            momHelper.lockMo(abisIpLdn);           
        }

        setSubTestcase("Unlock-connect TG-establish OML-release OML-lock", "Unlock AbisIp, connect TG, establish links, then release OML before locking AbisIp and disconnecting TG");
        for (int i = 1; i <= iterations; i++) {
            setTestStepBegin("Iteration " + i);
            checkLockedAbisIp();

            setTestInfo("Unlock AbisIp, connect TG, establish links and release them");
            momHelper.unlockMo(abisIpLdn);
            abisco.connectTG();
            try {
                abisco.establishLinks();
            } catch (InterruptedException e) {
                fail("Could not establish links");
            }
            abisco.releaseLinks();
            checkUnlockedAbisIpOmlDown();

            setTestInfo("Lock AbisIp and disconnect TG");
            momHelper.lockMo(abisIpLdn);
            abisco.disconnectTG();
        }

        setSubTestcase("Unlock-connect TG-establish OML-release OML-disconnect TG-lock", "Unlock AbisIp, connect TG, establish and release links, then disconnect TG before locking AbisIp");
        for (int i = 1; i <= iterations; i++) {
            setTestStepBegin("Iteration " + i);
            checkLockedAbisIp();

            setTestInfo("Unlock AbisIp, connect TG, establish links and release them, then disconnect TG");
            momHelper.unlockMo(abisIpLdn);
            abisco.connectTG();
            try {
                abisco.establishLinks();
            } catch (InterruptedException e) {
                fail("Could not establish links");
            }
            abisco.releaseLinks();
            abisco.disconnectTG();
            checkUnlockedAbisIpNotConnected();

            setTestInfo("Lock AbisIp");
            momHelper.lockMo(abisIpLdn);
        }

        setTestInfo("Final check: AbisIp administrativeState = LOCKED, operationalState = DISABLED, and availabilityStatus = OFFLINE");
        assertEquals("AbisIp MO attributes did not reach expected values", "", momHelper.checkAbisIpMoAttributeAfterLock(abisIpLdn, 5));
        setTestInfo("Check: OML Layer 2 link is not established for SCF, GsmSector,abisScfOmlState = DOWN");
        assertEquals("GsmSector MO attributes did not reach expected values", "", momHelper.checkGsmSectorMoAttributeAfterCreateNoLinks(sectorLdn, 5));
    }

    /**
     * @name unlockLockAbisIpConnected
     * 
     * @description Repeatedly unlock and lock AbisIp MO with a connected BSC when unlocking.
     * 
     * @param testId - unique identifier of the test case
     * @param description
     * 
     */ 
    @Test(timeOut = 900000)
    @Parameters({ "testId", "description" })
    public void unlockLockAbisIpConnected(String testId, String description) {
        setTestCase(testId, description);
        
    	if (!momHelper.isBtsFunctionCreated()) {
    		setTestInfo("Precondition: Create BtsFunction MO");
    		momHelper.createBtsFunctionMo(MomHelper.BTS_FUNCTION_LDN);
    	}
        
        abisIpLdn = momHelper.createSectorAndAbisIpMo(AbiscoConnection.getConnectionName(), abisco.getBscIpAddress(), false);
        final int iterations = 2; // Do (almost) everything twice

        setSubTestcase("Unlock-establish OML-disconnect-lock", "Unlock AbisIp followed by establish OML, then disconnect and lock AbisIp");
        for (int i = 1; i <= iterations; i++) {
            setTestStepBegin("Iteration " + i);
            checkLockedAbisIp();

            setTestInfo("********************* Unlock AbisIp, establish links and release them");
            momHelper.unlockMo(abisIpLdn);
            try {
                abisco.establishLinks();
            } catch (InterruptedException e) {
                fail("Could not establish links");
            }
            setTestInfo("********************* Established links, check AbisIp MO state");
            checkUnlockedAbisIpOmlUp();
            setTestInfo("********************* After Established links, AbisIp MO state is as expected");            
            abisco.disconnectTG();
            setTestInfo("********************* TG disconnected, check AbisIp MO state");
            checkUnlockedAbisIpNotConnected();
            setTestInfo("********************* TG disconnected, AbisIp MO state is as expected");

            setTestInfo("Lock AbisIp");
            momHelper.lockMo(abisIpLdn);
            abisco.connectTG();
        }
    }

    /**
     * @name unlockLockAbisIpLinksEstablished
     * 
     * @description Repeatedly unlock and lock AbisIp MO with a connected BSC having OML established.
     * 
     * @param testId - unique identifier of the test case
     * @param description
     * 
     */ 
    @Test(timeOut = 500000)
    @Parameters({ "testId", "description" })
    public void unlockLockAbisIpLinksEstablished(String testId, String description) {
        setTestCase(testId, description);
        
    	if (!momHelper.isBtsFunctionCreated()) {
    		setTestInfo("Precondition: Create BtsFunction MO");
    		momHelper.createBtsFunctionMo(MomHelper.BTS_FUNCTION_LDN);
    	}
    	
        abisIpLdn = momHelper.createSectorAndAbisIpMo(AbiscoConnection.getConnectionName(), abisco.getBscIpAddress(), false);
        final int iterations = 2; // Do everything twice
        abisco.establishLinksAsync();

        setSubTestcase("unlock-lock", "Unlock AbisIp with established links, then lock");
        for (int i = 1; i <= iterations; ++i) {
            setTestStepBegin("Iteration " + i);
            setTestInfo("***** Check that AbisIp MO is locked");
            checkLockedAbisIp();

            setTestInfo("Unlock AbisIp");
            momHelper.unlockMo(abisIpLdn);
            setTestInfo("***** AbisIp unlocked, check MO state and link state is UP");
            checkUnlockedAbisIpOmlUp();

            setTestInfo("Lock AbisIp and re-establish links");
            momHelper.lockMo(abisIpLdn);
            setTestInfo("***** AbisIp locked, check MO state");
            checkLockedAbisIp();
            abisco.establishLinksAsync();
        }

        setSubTestcase("unlock-release OML-lock", "Unlock AbisIp with established links, release the links, then lock");
        for (int i = 1; i <= iterations; ++i) {
            setTestStepBegin("Iteration " + i);
            setTestInfo("***** Check that AbisIp MO is locked");
            checkLockedAbisIp();

            setTestInfo("Unlock AbisIp and release links");
            momHelper.unlockMo(abisIpLdn);
            setTestInfo("***** AbisIp unlocked, check MO state and link state is UP");
            checkUnlockedAbisIpOmlUp();
            abisco.releaseLinks();
            setTestInfo("***** AbisIp unlocked, link released, check MO state and link state is DOWN");
            checkUnlockedAbisIpOmlDown();

            setTestInfo("Lock AbisIp and re-establish links");
            momHelper.lockMo(abisIpLdn);
            setTestInfo("***** Check that AbisIp MO is locked");
            checkLockedAbisIp();            
            abisco.establishLinksAsync();
        }

        setSubTestcase("unlock-disconnect TG-lock", "Unlock AbisIp with established links, disconnect the TG, then lock");
        for (int i = 1; i <= iterations; ++i) {
            setTestStepBegin("Iteration " + i);
            checkLockedAbisIp();

            setTestInfo("Unlock AbisIp and disconnect TG");
            momHelper.unlockMo(abisIpLdn);
            setTestInfo("***** AbisIp unlocked, check MO state and link state is UP");
            checkUnlockedAbisIpOmlUp();
            abisco.disconnectTG();
            setTestInfo("***** AbisIp unlocked, TG disconnected, check MO state and link state is DOWN");
            checkUnlockedAbisIpNotConnected();

            setTestInfo("Lock AbisIp, connect TG and re-establish links");
            momHelper.lockMo(abisIpLdn);
            setTestInfo("***** AbisIp locked, check MO state and link state is DOWN");
            checkLockedAbisIp();
            
            if (i != iterations)
            {
            	// we do not want to do this on the last iteration, i.e. the end of the test case because
            	// it appears to confuse Abisco in the next test case
            	abisco.connectTG();
            	abisco.establishLinksAsync();
            }
        }
        checkLockedAbisIp();
    }
    
    /**
     * @name createUnlockAbisIpInOneTxLockDeleteAbisIpInOnTx
     * 
     * @description Creates and unlocks an AbisIp in the same transaction. Establish links. Locks and deletes the AbisIp in the same transaction. 
     *             
     * 
     * @param testId - unique identifier of the test case
     * @param description
     * 
     */
    @Test(timeOut = 420000)
    @Parameters({ "testId", "description" })
    public void createUnlockAbisIpInOneTxLockDeleteAbisIpInOnTx(String testId, String description) {
        setTestCase(testId, description);
        
    	if (!momHelper.isBtsFunctionCreated()) {
    		setTestInfo("Precondition: Create BtsFunction MO");
    		momHelper.createBtsFunctionMo(MomHelper.BTS_FUNCTION_LDN);
    	}
        
        setTestStepBegin("Pre-condition: Create TN configuration and a sector");
        momHelper.configureTn(abisco.getBscIpAddress());
        String sectorLdn = momHelper.createGsmSectorMos(1).get(0);
        setTestStepEnd();
        
        // run the create and delete AbisIp sequence twice to ensure that  we do not leave things behind
        for (int i=0 ; i < 2 ; ++i)
        {
        	setTestStepBegin("Create and unlock the AbisIp MO in the same transaction");
        	abisIpLdn = momHelper.createAbisIpMoOnly(AbiscoConnection.getConnectionName(), 
        			abisco.getBscIpAddress(), 
        			MomHelper.ABIS_IP_LDN, 
        			MomHelper.TNA_IP_LDN,
        			MomHelper.UNLOCKED);
        	setTestStepEnd();

        	setTestStepBegin("Check pre-conditions: AbisIp administrativeState = UNLOCKED, operationalState = ENABLED, and availabilityStatus = [] empty");
        	assertEquals("AbisIp MO attributes did not reach expected values", "", momHelper.checkAbisIpMoAttributeAfterUnlockBscConnected(abisIpLdn, abisco.getBscIpAddress(), 30));
        	setTestStepEnd();

        	setTestStepBegin("Establish links");
        	try {
        		abisco.establishLinks();
        	} catch (InterruptedException ie) {
        		fail("InteruptedException during establishLinks");
        	}
        	setTestStepBegin("Post-conditions: AbisIp administrativeState = UNLOCKED, operationalState = ENABLED, and availabilityStatus = empty");
        	assertEquals("AbisIp MO attributes did not reach expected values", "", momHelper.checkAbisIpMoAttributeAfterUnlockBscConnected(abisIpLdn, abisco.getBscIpAddress(), 30));

        	setTestStepBegin("Post-condition: OML Layer 2 link is established for SCF, GsmSector,abisScfOmlState=UP");
        	assertEquals("GsmSector MO attributes did not reach expected values", "", momHelper.checkGsmSectorMoAttributeAfterCreateLinksEstablished(sectorLdn, 5));
        	
        	// lock and delete the AbisIp MO
        	setTestStepBegin("Lock and delete AbisIp MO in the same transaction");
        	List<ManagedObjectAttribute> attributes = new ArrayList<ManagedObjectAttribute>();
        	attributes.add(new ManagedObjectValueAttribute(MomHelper.ADMINISTRATIVE_STATE, MomHelper.LOCKED));
        	momHelper.setAttributesAndDeleteMoInSameTx(abisIpLdn, attributes);
        	setTestStepEnd();

        	setTestStepBegin("Verify that MO was deleted");
        	assertFalse(momHelper.checkMoExist(abisIpLdn));
        	setTestStepEnd();
        }
    }
    
    /**
     * @name lockUnlockAbisIpWithUnlockedTrx
     * 
     * @description Lock and unlock AbisIp MO with unlocked TRX  
     *             
     * 
     * @param testId - unique identifier of the test case
     * @param description Lock and unlock AbisIp when there is an unlocked Trx, and all links are established.
     *                    We check that the AO AT becomes reset and that the configuration and bundling signatures are set to zero, when locking AbisIp. 
     *                    Main affected UCs: NodeUC459.N, NodeUC461.N, NodeUC478.N, NodeUC646.A1, NodeUC426.A4
     *                    
     * @throws InterruptedException 
     * 
     */
    @Test(timeOut = 420000)
    @Parameters({ "testId", "description" })
    public void lockUnlockAbisIpWithUnlockedTrx(String testId, String description) throws InterruptedException {
        setTestCase(testId, description);
        
        setTestStepBegin("Pre-condition: Create TN configuration");
        momHelper.configureTn(abisco.getBscIpAddress());
        setTestStepEnd();
        
    	if (!momHelper.isBtsFunctionCreated()) {
    		setTestInfo("Precondition: Create BtsFunction MO");
    		momHelper.createBtsFunctionMo(MomHelper.BTS_FUNCTION_LDN);
    	}
        
        // run the create and delete AbisIp sequence twice to ensure that  we do not leave things behind
        for (int i=0 ; i < 2 ; ++i)
        {
        	setTestStepBegin("Create GsmSector, AbisIp, and Trx MOs. Unlock AbisIp and Trx MOs, establish links");
        	abisIpLdn = momHelper.createSectorAndAbisIpMo(AbiscoConnection.getConnectionName(), abisco.getBscIpAddress(), false);
        	trxLdn = momHelper.createTrxMo(sectorLdn, "0"); 
        	momHelper.unlockMo(abisIpLdn);
        	momHelper.unlockMo(trxLdn);
            abisco.establishLinks();
            assertTrue("Could not activate Abis Transport", abisHelper.startSectorMosAndActivateAT());
            abisco.establishLinks(true);
            assertEquals("AbisIp MO attributes did not reach expected values", "", momHelper.checkAbisIpMoAttributeAfterUnlockBscConnected(abisIpLdn, abisco.getBscIpAddress(), 30));
            assertEquals("GsmSector MO attributes did not reach expected values", "", momHelper.checkGsmSectorMoAttributeAfterAllMosStartedAoAtEnabled(sectorLdn, 5));
            assertEquals("Trx MO attributes did not reach expected values", "", momHelper.checkTrxMoAttributeAfterUnlockLinksEstablished(trxLdn, 5));

            OM_G31R01.StatusResponse statusResponse = abisHelper.statusRequest(OM_G31R01.Enums.MOClass.AT);
            saveAssertEquals("MO State must be ENABLED", Enums.MOState.ENABLED, statusResponse.getMOState());
            saveAssertEquals("OperationalCondition must be Operational", OM_G31R01.Enums.OperationalCondition.Operational.getValue(), statusResponse.getStatusChoice().getStatusAO().getOperationalCondition().getOperationalCondition().getValue());
            // the signatures shall NOT be zero
            saveAssertTrue("Mismatch in Bundling Signature",      0 != statusResponse.getStatusChoice().getStatusAO().getBundlingSignature().intValue());
            saveAssertTrue("Mismatch in Configuration Signature", 0 != statusResponse.getStatusChoice().getStatusAO().getConfigurationSignature());
            setTestStepEnd();
        	
        	
        	setTestStepBegin("Lock AbisIp MOs while Trx is unlocked and links are established links.");
        	momHelper.lockMo(abisIpLdn);
        	abisco.disconnectTG();
            assertEquals("AbisIp MO attributes did not reach expected values", "", momHelper.checkAbisIpMoAttributeAfterLock(abisIpLdn, 5));
            assertEquals("GsmSector MO attributes did not reach expected values", "", momHelper.checkGsmSectorMoAttributeAfterAllMosStartedAoAtResetNoLinks(sectorLdn, 10));
           	assertEquals("Trx MO attributes did not reach expected values", "", momHelper.checkTrxMoAttributeAfterUnlockNoLinks(trxLdn, 5));
        	setTestStepEnd();
        	
        	abisco.connectTG();
        	setTestStepBegin("Unlock AbisIp MOs while Trx is unlocked, links were disconnected when AbisIp were previously locked.");
        	momHelper.unlockMo(abisIpLdn);
        	assertEquals("AbisIp MO attributes did not reach expected values", "", momHelper.checkAbisIpMoAttributeAfterUnlockBscConnected(abisIpLdn, abisco.getBscIpAddress(), 30));
            assertEquals("GsmSector MO attributes did not reach expected values", "", momHelper.checkGsmSectorMoAttributeAfterAllMosStartedAoAtResetNoLinks(sectorLdn, 10));
           	assertEquals("Trx MO attributes did not reach expected values", "", momHelper.checkTrxMoAttributeAfterUnlockNoLinks(trxLdn, 10));
        	setTestStepEnd();
        	

        	setTestStepBegin("Establich SCF OML and send a status request to AO AT to check that the configuration and bundling signature are set to zero and AO AT is reset.");
            abisco.establishLinks();
            //signatures are reset in state RESET AT
            OM_G31R01.StatusResponse statusResponse2 = abisHelper.statusRequest(OM_G31R01.Enums.MOClass.AT);
            saveAssertEquals("MO State must be RESET", Enums.MOState.RESET, statusResponse2.getMOState());
            saveAssertEquals("OperationalCondition must be Operational", OM_G31R01.Enums.OperationalCondition.Operational.getValue(), statusResponse2.getStatusChoice().getStatusAO().getOperationalCondition().getOperationalCondition().getValue());
            // the signatures shall be zero
            saveAssertTrue("Mismatch in Bundling Signature",      0 == statusResponse2.getStatusChoice().getStatusAO().getBundlingSignature().intValue());
            saveAssertTrue("Mismatch in Configuration Signature", 0 == statusResponse2.getStatusChoice().getStatusAO().getConfigurationSignature());
        	assertEquals("AbisIp MO attributes did not reach expected values", "", momHelper.checkAbisIpMoAttributeAfterUnlockBscConnected(abisIpLdn, abisco.getBscIpAddress(), 30));
            assertEquals("GsmSector MO attributes did not reach expected values", "", momHelper.checkGsmSectorMoAttributeAfterAllMosStartedAoAtResetLinkEstablished(sectorLdn, 5));
            assertEquals("Trx MO attributes did not reach expected values", "", momHelper.checkTrxMoAttributeAfterUnlockNoLinks(trxLdn, 5));
            setTestStepEnd();
        	
        	
        	setTestStepBegin("Configure and enable AO AT, establich OML+RSL for Trx again");
            assertTrue("Could not activate Abis Transport", abisHelper.startSectorMosAndActivateAT());
            abisco.establishLinks(true);
            assertEquals("AbisIp MO attributes did not reach expected values", "", momHelper.checkAbisIpMoAttributeAfterUnlockBscConnected(abisIpLdn, abisco.getBscIpAddress(), 30));
            assertEquals("GsmSector MO attributes did not reach expected values", "", momHelper.checkGsmSectorMoAttributeAfterAllMosStartedAoAtEnabled(sectorLdn, 5));
            assertEquals("Trx MO attributes did not reach expected values", "", momHelper.checkTrxMoAttributeAfterUnlockLinksEstablished(trxLdn, 5));
            OM_G31R01.StatusResponse statusResponse3 = abisHelper.statusRequest(OM_G31R01.Enums.MOClass.AT);
            saveAssertEquals("MO State must be ENABLED", Enums.MOState.ENABLED, statusResponse3.getMOState());
            saveAssertEquals("OperationalCondition must be Operational", OM_G31R01.Enums.OperationalCondition.Operational.getValue(), statusResponse3.getStatusChoice().getStatusAO().getOperationalCondition().getOperationalCondition().getValue());
            // the signatures shall NOT be zero
            saveAssertTrue("Mismatch in Bundling Signature",      0 != statusResponse3.getStatusChoice().getStatusAO().getBundlingSignature().intValue());
            saveAssertTrue("Mismatch in Configuration Signature", 0 != statusResponse3.getStatusChoice().getStatusAO().getConfigurationSignature());
        	setTestStepEnd();
        	
        	setTestStepBegin("Lock and delete all MOs");
        	momHelper.lockMo(abisIpLdn);
        	momHelper.lockMo(trxLdn);
        	momHelper.deleteMo(abisIpLdn);
        	momHelper.deleteMo(trxLdn);
        	momHelper.deleteMo(sectorLdn);
        	assertFalse(momHelper.checkMoExist(abisIpLdn));
        	assertFalse(momHelper.checkMoExist(trxLdn));
        	assertFalse(momHelper.checkMoExist(sectorLdn)); 
        	setTestStepEnd();
        }     	
    }
    
    /**
     * @name unlockAbisLockAbisWithTrxLockedGt60s
     * 
     * @description NodeUC461.N: Lock AbisIp, NodeUC461.A1 Lock AbisIp with locked TRX MOs great than 60s.
     * 
     * @param testId - unique identifier of the test case
     * @param description
     *      
     */ 
    @Test(timeOut = 500000)
    @Parameters({ "testId", "description" })
    public void unlockAbisLockAbisWithTrxLockedGt60s(String testId, String description)
    {
    	 setTestCase(testId, description);
    	 
     	 if (!momHelper.isBtsFunctionCreated()) {
    	 	setTestInfo("Precondition: Create BtsFunction MO");
    	 	momHelper.createBtsFunctionMo(MomHelper.BTS_FUNCTION_LDN);
    	 }
    	 
         // Create a connection to BSC that does not exist
         abisIpLdn = momHelper.createSectorAndAbisIpMo(AbiscoConnection.getConnectionName(), "123.123.123.123", false);

         trxLdn = momHelper.createTrxMo(sectorLdn, "0");
         assertTrue(momHelper.checkMoExist(trxLdn)); 
         assertEquals("Trx MO attributes did not reach expected values", "", momHelper.checkTrxMoAttributeAfterLock(trxLdn, 5));
         
         // Enable the log
         final String traceGroup = "trace1";
         final String processName = "*/GSC*";
         logHandler.teEnable(traceGroup, processName);
         
         // Enable SNMP
         setTestStepBegin("Set SNMP configuration");
         momHelper.enableSnmp();
         setTestStepEnd();
         
         setTestStepBegin("Check pre-conditions: AbisIp administrativeState = LOCKED, operationalState = DISABLED, and availabilityStatus = OFFLINE");
         assertEquals("AbisIp MO attributes did not reach expected values", "", momHelper.checkAbisIpMoAttributeAfterLock(abisIpLdn, 5));
    
         // Unlock AbisIp
         setTestInfo("Unlock AbisIp");
         momHelper.unlockMo(abisIpLdn);   
         momHelper.lockMo(trxLdn);            
         checkLockedTrx();            
         //cliCommands.setBasebandTransportTesting("1", false);
         setTestStepEnd();

         setTestStepBegin("Check alarm is raised");
         // This sleep is here to ensure that the GRAT got long enough time to try to connect to the non-existing BSC
         sleepSeconds(65);
             
         // WP3794, Checkalarm is raised here
         long timeoutInSeconds = 90;
         List<ComAlarm> alarmList = alarmHelper.getAlarmList(timeoutInSeconds);
         boolean isAlarmActive = alarmHelper.isBscDisconnectAlarm(alarmList, abisIpLdn);
         assertTrue(isAlarmActive);
         setTestStepEnd();
             
         setTestStepBegin("Post-conditions: AbisIp administrativeState = UNLOCKED, operationalState = DISABLED, availabilityStatus = FAILED");
         assertEquals("AbisIp MO attributes did not reach expected values", "", momHelper.checkAbisIpMoAttributeAfterUnlockBscDisconnected(abisIpLdn, 5));
         setTestInfo("Post-condition: OML Layer 2 link is not established for SCF, GsmSector,abisScfOmlState = DOWN");
         assertEquals("GsmSector MO attributes did not reach expected values", "", momHelper.checkGsmSectorMoAttributeAfterCreateNoLinks(sectorLdn, 5));
         
         // Lock AbisIp
         setTestInfo("Lock AbisIp");
         momHelper.lockMo(abisIpLdn);
         sleepSeconds(10);
         checkLockedTrx();
         setTestStepEnd();

         //cliCommands.setBasebandTransportTesting("1", false);
         setTestStepBegin("Check pre-conditions: AbisIp administrativeState = LOCKED, operationalState = DISABLED, and availabilityStatus = OFFLINE");
         assertEquals("AbisIp MO attributes did not reach expected values", "", momHelper.checkAbisIpMoAttributeAfterLock(abisIpLdn, 5));
         setTestStepEnd();
         
         setTestStepBegin("Check alarm is ceased");
         // WP3794, alarm is Ceased here
         // Alarm should not exist since we already locked AbisIpMo
         alarmList = alarmHelper.getAlarmList();
         isAlarmActive = alarmHelper.isBscDisconnectAlarm(alarmList, abisIpLdn);
         assertTrue(!isAlarmActive);
         setTestStepEnd();

         setTestStepBegin("Check AbisIp Mo Attributes");
         logHandler.teDisable(traceGroup, processName);
         // Final post-check
         assertEquals("AbisIp MO attributes did not reach expected values", "", momHelper.checkAbisIpMoAttributeAfterLock(abisIpLdn, 5));
         setTestStepEnd();
    }
    
    /**
     * @name unlockAbisLockAbisWithTrxLockedLt60s
     * 
     * @description NodeUC461.N: Lock AbisIp, NodeUC461.A1 Lock AbisIp with locked TRX MOs less than 60s.
     * 
     * @param testId - unique identifier of the test case
     * @param description
     *      
     */ 
    @Test(timeOut = 500000)
    @Parameters({ "testId", "description" })
    public void unlockAbisLockAbisWithTrxLockedLt60s(String testId, String description)
    {
    	 setTestCase(testId, description);
    	 
     	 if (!momHelper.isBtsFunctionCreated()) {
    	 	setTestInfo("Precondition: Create BtsFunction MO");
    	 	momHelper.createBtsFunctionMo(MomHelper.BTS_FUNCTION_LDN);
    	 }
    	 
         // Create a connection to BSC that does not exist
         abisIpLdn = momHelper.createSectorAndAbisIpMo(AbiscoConnection.getConnectionName(), "123.123.123.123", false);

         trxLdn = momHelper.createTrxMo(sectorLdn, "0");
         assertTrue(momHelper.checkMoExist(trxLdn));
         assertEquals("Trx MO attributes did not reach expected values", "", momHelper.checkTrxMoAttributeAfterLock(trxLdn, 5));
         
         // Enable the log
         final String traceGroup = "trace1";
         final String processName = "*/GSC*";        
         logHandler.teEnable(traceGroup, processName);
         
         // Enable SNMP
         setTestStepBegin("Set SNMP configuration");       
         momHelper.enableSnmp(); 
         setTestStepEnd();
         
         setTestStepBegin("Check pre-conditions: AbisIp administrativeState = LOCKED, operationalState = DISABLED, and availabilityStatus = OFFLINE");
         assertEquals("AbisIp MO attributes did not reach expected values", "", momHelper.checkAbisIpMoAttributeAfterLock(abisIpLdn, 5));
    
         // Unlock AbisIp
         setTestInfo("Unlock AbisIp");
         momHelper.unlockMo(abisIpLdn);   
         momHelper.lockMo(trxLdn);            
         checkLockedTrx();            
         //cliCommands.setBasebandTransportTesting("1", false);
             
         // This sleep is here to ensure that the GRAT got long enough time to try to connect to the non-existing BSC
         sleepSeconds(5);
             
         // Lock AbisIp
         setTestInfo("Lock AbisIp");
         momHelper.lockMo(abisIpLdn);
         sleepSeconds(70);
         checkLockedTrx();
         setTestStepEnd();
 
         setTestStepBegin("Check alarm is not raised");
         // WP3794, Alarm is not raised here
         // Alarm should not exist since we already locked AbisIpMo
         List<ComAlarm> alarmList = alarmHelper.getAlarmList();
         boolean isAlarmActive = alarmHelper.isBscDisconnectAlarm(alarmList, abisIpLdn);
         assertTrue(!isAlarmActive);
         setTestStepEnd();

         setTestStepBegin("Check AbisIp Mo Attributes");
         logHandler.teDisable(traceGroup, processName);
         // Final post-check
         assertEquals("AbisIp MO attributes did not reach expected values", "", momHelper.checkAbisIpMoAttributeAfterLock(abisIpLdn, 5));
         setTestStepEnd();
    }    
}
