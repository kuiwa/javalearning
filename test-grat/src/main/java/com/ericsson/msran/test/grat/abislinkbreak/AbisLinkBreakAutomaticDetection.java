package com.ericsson.msran.test.grat.abislinkbreak;

import org.json.JSONException;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import se.ericsson.jcat.fw.annotations.Setup;
import se.ericsson.jcat.fw.annotations.Teardown;

import com.ericsson.abisco.clientlib.servers.OM_G31R01;
import com.ericsson.abisco.clientlib.servers.OM_G31R01.Enums;
import com.ericsson.abisco.clientlib.servers.OM_G31R01.FSOffset;
import com.ericsson.msran.g2.annotations.TestInfo;
import com.ericsson.msran.test.grat.testhelpers.AbisHelper;
import com.ericsson.msran.test.grat.testhelpers.AbiscoConnection;
import com.ericsson.msran.test.grat.testhelpers.MomHelper;
import com.ericsson.msran.test.grat.testhelpers.NodeStatusHelper;
import com.ericsson.msran.jcat.TestBase;


/**
 * @id NodeUC524.A5
 * 
 * @name AbisLinkBreakAutomaticDetection
 * 
 * @author GRAT 2015
 * 
 * @created 2015-05-08
 * 
 * @description This test class verifies the scenario when an Abis link break not ordered by BSC is detected automatically.
 * 
 * @revision uabkabe 2015-05-08 First version.           
 */

public class AbisLinkBreakAutomaticDetection extends TestBase {
  private final OM_G31R01.Enums.MOClass  moClassTf = OM_G31R01.Enums.MOClass.TF;
  private final OM_G31R01.Enums.MOClass moClassTx = OM_G31R01.Enums.MOClass.TX;   
  private final OM_G31R01.Enums.MOClass  moClassTrxc = OM_G31R01.Enums.MOClass.TRXC;

  private MomHelper momHelper;
  private AbiscoConnection abisco;
  private NodeStatusHelper nodeStatus;
  private AbisHelper abisHelper;
  private static String sectorLdn = "ManagedElement=1,BtsFunction=1,GsmSector=1";
  /**
   * Description of test case for test reporting
   */
  @TestInfo(
      tcId = "NodeUC524",
      slogan = "Detect, recover from and report GSM Fault",
      requirementDocument = "1/00651-FCP 130 1402",
      requirementRevision = "PC188",
      requirementLinkTested = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff87c1d941?docno=1/00651-FCP1301402Uen&format=excel8book",
      requirementLinkLatest = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff86a2af3d?docno=1/00651-FCP1301402Uen&action=current&format=excel8book",
      requirementIds = { "10565-0334/21767[PA1][PREL]", "10565-0334/19034[A][APPR]", "10565-0334/19417[A][APPR]" },
      verificationStatement = "Verifies NodeUC524.A5",
      testDescription = "Detect that an abnormal Abis link break is detected automatically.",
      traceGuidelines = "N/A")


  /**
   * Precond.
   */
  @Setup
  public void setup() throws InterruptedException, JSONException {
	setTestStepBegin("Setup");
    nodeStatus = new NodeStatusHelper();
    assertTrue("Node did not reach a working state before test start", nodeStatus.waitForNodeReady());
    abisHelper = new AbisHelper();
    momHelper = new MomHelper();
    abisco = new AbiscoConnection();
    setTestStepEnd();
  }

  /**
   * Postcond.
   */
  @Teardown
  public void teardown() {
	setTestStepBegin("Teardown");
    nodeStatus.isNodeRunning();
    setTestStepEnd();
  }    

  /**
   * @name AbisLinkBreakAutomaticDetection
   * 
   * @description Verifies NodeUC524.A5: "Detect and report 85s long Abis TRXC O&M link break",
   *
   * @param testId - unique identifier
   * @param description
   */
  @Test (timeOut = 540000)
  @Parameters({ "testId", "description" })
  public void abisLinkBreakAutomaticDetectionTest(String testId, String description) {
    setTestCase(testId, description);           

    setTestStepBegin("Setup link and create MOs");
    abisco.setupAbisco(false);
    
	if (!momHelper.isBtsFunctionCreated()) {
		setTestInfo("Precondition: Create BtsFunction MO");
		momHelper.createBtsFunctionMo(MomHelper.BTS_FUNCTION_LDN);
	}
    
    String abisIpLdn = momHelper.createSectorAndAbisIpMo(AbiscoConnection.getConnectionName(), abisco.getBscIpAddress(), false);
    String trxLdn = momHelper.createTrxMo(sectorLdn, "0"); 
    // Unlock AbisIp and TRX
    setTestInfo("Unlock abisIp and Trx");  
    momHelper.unlockMo(trxLdn);
    momHelper.unlockMo(abisIpLdn);

    // Establish SCF Links
    setTestInfo("Establish SCF Links");
    try {
      abisco.establishLinkForScfOml();
    } catch (InterruptedException ie) {
      fail("InteruptedException during establishLinks");
    }

    // Enable AT
    setTestInfo("Precondition: enable AT");
    assertTrue("Could not activate AT", abisHelper.startSectorMosAndActivateAT());

    // Establish TRX links 
    setTestInfo("Establish TRX links");
    try {
      abisco.establishLinkForTrxOmlRsl(); // TRX OML and TRX RSL will be established
    } catch (InterruptedException ie) {
      fail("InteruptedException during establishLinks");
    }        

   try {
      setTestInfo("Precondition: configure AO TF");
      OM_G31R01.TFConfigurationResult tfConfigResult = abisHelper.tfConfigRequest(1, Enums.TFMode.Master, abisHelper.getOM_G31R01fsOffsetByActiveSyncSrc());
      assertTrue("AccordanceIndication not according to Request", tfConfigResult.getAccordanceIndication().getAccordanceIndication() == Enums.AccordanceIndication.AccordingToRequest);
    } catch (InterruptedException ie) {
      fail("InteruptedException during establishLinks");
    }

    try {
      setTestInfo("Enable AO TF");  
      abisHelper.enableRequest(this.moClassTf, 0);
      assertTrue("MO " + sectorLdn + " abisTfState is not ENABLED", 
          momHelper.waitForMoAttributeStringValue(sectorLdn, "abisTfState", "ENABLED", 6));

    } catch (InterruptedException ie) {
      fail("InteruptedException during enable AO TF");
    } 

    // Verify that the links has been established by checking MO attributes:
    setTestInfo("Check pre-conditions: GsmSector attribute abisScfOmlState = UP, Trx attributes abisTrxcOmlState = UP and abisTrxRslState = UP");
    assertEquals("GsmSector MO attributes did not reach expected values", "", momHelper.checkGsmSectorMoAttributeAfterAllMosStartedAoAtAndAoTfEnabled(sectorLdn, 5));
    assertEquals("Trx MO attributes did not reach expected values", "", momHelper.checkTrxMoAttributeAfterUnlockLinksEstablished(trxLdn, 5));

    try {
      setTestInfo("Precondition: start TRXC");
      abisHelper.startRequest(this.moClassTrxc, 0);
      setTestInfo("Precondition: start TX");
      abisHelper.startRequest(this.moClassTx, 0);
    } catch (InterruptedException ie) {
      fail("InteruptedException during startRequestAoTx");
    } 

    setTestInfo("Precondition: enable Ao Tx");
    try {
      enableRequestAoTx(true);
    } catch (InterruptedException ie) {
      fail("InteruptedException during enableRequestAoTx");
    } 

    setTestStepEnd();

    setTestInfo("TEST 1: Check that timeout 85 sec does not expire if links come up within 85 sec.");
    // Release OML Link  
    setTestStepBegin("Release OML Link");           
    abisco.releaseLinkForTrxOml();
    setTestStepEnd();

    // Verify that the automatic detection of link break has not timed out.
    setTestStepBegin("Verify that the automatic detection of link break has not timed out after 60 sec: abisTrxcOmlState = DOWN");
    sleepSeconds(60);
    assertEquals("GsmSector MO attributes did not reach expected values", "", momHelper.checkGsmSectorMoAttributeAfterAllMosStartedAoAtAndAoTfEnabled(sectorLdn, 5));
    assertEquals("Trx MO attributes did not reach expected values", "", momHelper.checkTrxMoAttribute85SecAutoDetectLinkBreakPreCond(trxLdn, 5));
    setTestStepEnd();

    // Setup link again.
    setTestStepBegin("Establish links");
    try {
      abisco.establishLinkForTrxOml();
    } catch (InterruptedException ie) {
      fail("InteruptedException during establishLinks");
    }
    setTestStepEnd();

    // Verify that the links has been established by checking MO attributes:
    setTestStepBegin("Verify that the links have been established. abisScfOmlState = UP, abisTrxcOmlState = UP and abisTrxRslState = UP");    
    assertEquals("GsmSector MO attributes did not reach expected values", "", momHelper.checkGsmSectorMoAttributeAfterAllMosStartedAoAtAndAoTfEnabled(sectorLdn, 5));
    assertEquals("Trx MO attributes did not reach expected values", "", momHelper.checkTrxMoAttribute85SecAutoDetectLinkBreakRslOmlUp(trxLdn, 5));            
    setTestStepEnd();

    // Wait until more than 85 sec (90 sec) has passed since first link break.
    sleepSeconds(30);

    // Verify that the links has been established by checking MO attributes:
    setTestStepBegin("Verify that the links are still up. abisScfOmlState = UP, abisTrxcOmlState = UP and abisTrxRslState = UP");    
    assertEquals("GsmSector MO attributes did not reach expected values", "", momHelper.checkGsmSectorMoAttributeAfterAllMosStartedAoAtAndAoTfEnabled(sectorLdn, 5));
    assertEquals("Trx MO attributes did not reach expected values", "", momHelper.checkTrxMoAttribute85SecAutoDetectLinkBreakRslOmlUp(trxLdn, 5));            
    setTestStepEnd();

    setTestInfo("TEST 2: Check that a RSL link break does not start timer with 85 sec timeout.");
    // Release RSL Link  
    setTestStepBegin("Release RSL Link");           
    abisco.releaseLinkForTrxRsl();
    setTestStepEnd();

    // Verify that OML link is up after a disconnect of RSL link
    setTestStepBegin("Verify that OML link is up after a disconnect of RSL link. abisScfOmlState = UP, abisTrxcOmlState = UP and abisTrxRslState = DOWN");    
    assertEquals("GsmSector MO attributes did not reach expected values", "", momHelper.checkGsmSectorMoAttributeAfterAllMosStartedAoAtAndAoTfEnabled(sectorLdn, 5));
    assertEquals("Trx MO attributes did not reach expected values", "", momHelper.checkTrxMoAttribute85SecAutoDetectLinkBreakRslDown(trxLdn, 5));            
    setTestStepEnd();

    // Verify that no 85 sec timer was started.
    setTestStepBegin("Verify that OML link is up after a disconnect of RSL link also after 90 sec. abisScfOmlState = UP, abisTrxcOmlState = UP and abisTrxRslState = DOWN");   
    sleepSeconds(90);
    assertEquals("GsmSector MO attributes did not reach expected values", "", momHelper.checkGsmSectorMoAttributeAfterAllMosStartedAoAtAndAoTfEnabled(sectorLdn, 5));
    assertEquals("Trx MO attributes did not reach expected values", "", momHelper.checkTrxMoAttribute85SecAutoDetectLinkBreakRslDown(trxLdn, 5));            
    setTestStepEnd();

    // Setup link again.
    setTestStepBegin("Establish links");
    try {
      abisco.establishLinkForTrxRsl();
    } catch (InterruptedException ie) {
      fail("InteruptedException during establishLinks");
    }
    setTestStepEnd();

    // Verify that the links has been established by checking MO attributes:
    setTestStepBegin("Verify that the Rsl link have been established. abisScfOmlState = UP, abisTrxcOmlState = UP and abisTrxRslState = UP");    
    assertEquals("GsmSector MO attributes did not reach expected values", "", momHelper.checkGsmSectorMoAttributeAfterAllMosStartedAoAtAndAoTfEnabled(sectorLdn, 5));
    assertEquals("Trx MO attributes did not reach expected values", "", momHelper.checkTrxMoAttribute85SecAutoDetectLinkBreakRslOmlUp(trxLdn, 5));            
    setTestStepEnd();

    setTestInfo("TEST 3: Check that timeout after 85 sec work.");
    // Release OML Link  
    setTestStepBegin("Release OML Link");           
    abisco.releaseLinkForTrxOml();
    setTestStepEnd();

    // Verify that the automatic detection of link break has not timed out after 75 sec
    setTestStepBegin("Verify that the automatic detection of link break has not timed out after 75 sec by checking MO attributes: abisTrxcOmlState = DOWN");
    sleepSeconds(75);
//    assertEquals("GsmSector MO attributes values", "", momHelper.checkGsmSectorMoAttributeAfterAllMosStartedAoAtEnabledNoLinksValues(sectorLdn, 5));
    assertEquals("GsmSector MO attributes values", "", momHelper.checkGsmSectorMoAttributeScfOmlUp(sectorLdn, 5));
    assertEquals("Trx MO attributes did not reach expected values", "", momHelper.checkTrxMoAttribute85SecAutoDetectLinkBreakPreCond(trxLdn, 5));
    setTestStepEnd();

    // Verify that the automatic detection of link break has timed out after 87 sec
    setTestStepBegin("Verify that the automatic detection of link break has timed out after 87 sec by checking MO attributes: abisTrxcOmlState = DOWN, abisTxMoState = DISABLED and abisTrxRslState = UP");
    sleepSeconds(12);
//    assertEquals("GsmSector MO attributes values", "", momHelper.checkGsmSectorMoAttributeAfterAllMosStartedAoAtEnabledNoLinksValues(sectorLdn, 5));
    assertEquals("GsmSector MO attributes values", "", momHelper.checkGsmSectorMoAttributeScfOmlUp(sectorLdn, 5));
    assertEquals("Trx MO attributes did not reach expected values", "", momHelper.checkTrxMoAttribute85SecAutoDetectLinkBreakPostCond(trxLdn, 5));
    setTestStepEnd();


    setTestStepBegin("Establishing Trx Oml link");
    setTestInfo("Establish links after timeout");
    try {
      abisco.establishLinkForTrxOml();
    } catch (InterruptedException ie) {
      fail("InteruptedException during establishLinkForTrxOml");
    }

    setTestInfo("Enable AO TX after link is up");
    try {
      enableRequestAoTx();
    } catch (InterruptedException ie) {
      fail("InteruptedException during enableRequestAoTx");
    }    
    setTestStepEnd();
    
    // Verify that the links has been established by checking MO attributes:
    setTestStepBegin("Verify that the links have been established. abisScfOmlState = UP, abisTrxcOmlState = UP and abisTrxRslState = UP");    
//    assertEquals("GsmSector MO attributes values", "", momHelper.checkGsmSectorMoAttributeAfterAllMosStartedAoAtEnabledNoLinksValues(sectorLdn, 5));
//  assertEquals("GsmSector MO attributes values", "", momHelper.checkGsmSectorMoAttributeAfterAllMosStartedAoAtAndAoTfEnabled(sectorLdn, 5));
    assertEquals("GsmSector MO attributes values", "", momHelper.checkGsmSectorMoAttributeScfOmlUp(sectorLdn, 5));
    assertEquals("Trx MO attributes did not reach expected values", "", momHelper.checkTrxMoAttribute85SecAutoDetectLinkBreakRslOmlUp(trxLdn, 5));            
    setTestStepEnd();
    
    // Verify that timer is not still alive after another timer cycle;
    setTestStepBegin("Verify that the TX MO state = ENABLED after another 90 sec,  abisTxOmState = ENABLED");
    sleepSeconds(90);
//  assertEquals("GsmSector MO attributes values", "", momHelper.checkGsmSectorMoAttributeAfterAllMosStartedAoAtEnabledNoLinksValues(sectorLdn, 5));
//  assertEquals("GsmSector MO attributes values", "", momHelper.checkGsmSectorMoAttributeAfterAllMosStartedAoAtAndAoTfEnabled(sectorLdn, 5));
    assertEquals("GsmSector MO attributes values", "", momHelper.checkGsmSectorMoAttributeScfOmlUp(sectorLdn, 5));
    assertEquals("Trx MO attributes did not reach expected values", "", momHelper.checkTrxMoAttribute85SecAutoDetectLinkBreakRslOmlUp(trxLdn, 5));            
    setTestStepEnd();
    // Cleanup done by RestoreStack
  }
  
  public void enableRequestAoTx() throws InterruptedException {
    enableRequestAoTx(false);
  }

  public void enableRequestAoTx(boolean doTXconfig) throws InterruptedException {        

    OM_G31R01.EnableResult result;

    // Only need to do configure if we come from state RESET
    setTestStepBegin("Configure AO TX");
    if (doTXconfig) {
      setTestInfo("enableRequestAoTx: Configure AO TX");
      OM_G31R01.TXConfigurationResult confRes = abisHelper.txConfigRequest(0, momHelper.getArfcnToUse(), false, 27, false);
      saveAssertEquals("According Indication must be According to Request",
          Enums.AccordanceIndication.AccordingToRequest,
          confRes.getAccordanceIndication().getAccordanceIndication());

    }
    setTestStepEnd();

    setTestStepBegin("Send Enable Request to AO TX");
    setTestInfo("enableRequestAoTx: Enable AO TX");
    result = abisHelper.enableRequest(this.moClassTx, 0);
    setTestStepEnd();

    setTestStepBegin("Verify that MO State in Enable Result is ENABLED");
    setTestInfo("enableRequestAoTx: Verify AO TX ENABLED");
    saveAssertEquals("MoState is not ENABLED", OM_G31R01.Enums.MOState.ENABLED, result.getMOState());
    setTestStepEnd();

    setTestStepBegin("Verify that MO:Trx attribute:abisTxMoState is ENABLED");
    setTestInfo("enableRequestAoTx: Verify AO TX ENABLE");
    assertTrue("abisTxMoState is not ENABLED", momHelper.waitForAbisTxMoState("ENABLED"));
    setTestStepEnd();

  }
}