package com.ericsson.msran.test.grat.softwareupgrade;

import java.io.File;
import java.io.IOException;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.ericsson.abisco.clientlib.servers.BG.Enums;
import com.ericsson.abisco.clientlib.servers.BG.G31OperationalCondition;
import com.ericsson.abisco.clientlib.servers.BG.G31OperationalConditionReasonsMap;

import com.ericsson.abisco.clientlib.servers.BG.G31StatusSCF;
import com.ericsson.abisco.clientlib.servers.BG.G31StatusUpdate;
import com.ericsson.commonlibrary.ecimcom.exception.ComCliException;
import com.ericsson.commonlibrary.ecimcom.netconf.NetconfManagedObjectHandler;
import com.ericsson.commonlibrary.faultmanagement.com.ComAlarm;
import com.ericsson.msran.configuration.MsranJcatException;

import com.ericsson.msran.g2.annotations.TestInfo;
import com.ericsson.commonlibrary.managedobjects.ManagedObjectStructAttribute;
import com.ericsson.commonlibrary.resourcemanager.Rm;
import com.ericsson.commonlibrary.resourcemanager.g2.G2Rbs;
import com.ericsson.commonlibrary.resourcemanager.upgrade.G2UpgradePackage;

import com.ericsson.msran.test.grat.testhelpers.AbisHelper;
import com.ericsson.msran.test.grat.testhelpers.AbiscoConnection;
import com.ericsson.msran.test.grat.testhelpers.AlarmHelper;
import com.ericsson.msran.test.grat.testhelpers.MomHelper;
import com.ericsson.msran.test.grat.testhelpers.NodeStatusHelper;
import com.ericsson.msran.test.grat.testhelpers.restorecommands.DeleteMoCommand;
import com.ericsson.msran.test.grat.testhelpers.restorecommands.LockDeleteMoCommand;
import com.ericsson.msran.helpers.Helpers;
import com.ericsson.msran.helpers.upgradepackage.G2UpgradeHelper;
import com.ericsson.msran.helpers.upgradepackage.UpgradePackageHelperFactory;
import com.ericsson.msran.helpers.util.ZipHelper;
import com.ericsson.msran.jcat.TestBase;
import com.ericsson.commonlibrary.managedobjects.ManagedObject;

import se.ericsson.jcat.fw.annotations.Setup;
import se.ericsson.jcat.fw.annotations.Teardown;
import com.ericsson.msran.g2.handlers.G2RbsHandler;

import com.ericsson.commonlibrary.restorestack.RestoreCommand;
import com.ericsson.commonlibrary.restorestack.RestoreCommandStack;


/**
 * @id WP3534 & WP4537
 *
 * @name SoftwareUpgradeTest
 *
 * @author ejmoqrx
 *
 * @created 2015-02-16
 *
 * @description grat software upgrade
 *
 * @revision ezhidua 2015-04-30 fine tune for stability
 * @revision ezhidua 2015-04-29 add alarm log and abisco status update check
 * @revision ejmoqrx 2015-02-16 first version
 *
 */
public class SoftwareUpgradeTest extends TestBase {

  private Logger logger;

  private NodeStatusHelper nodeStatus;

  private G2Rbs rbs;
  private G2UpgradePackage gratRbsUpgradePackage;
  private UpgradePackageHelperFactory upgradePackageHelperFactory;
  private G2UpgradeHelper gratUpgradeHelper;
  private NetconfManagedObjectHandler moHandler;

  private MomHelper momHelper;
  private AbiscoConnection abisco;
  private AbisHelper abisHelper;
  private AlarmHelper alarmHelper;

  private G2RbsHandler g2RbsHandler;
  private RestoreCommandStack restoreStack;
  
  private int tg_0 = 0;
  private int tg_1 = 1;
  private String connection_name_0 = "host_0";
  private String connection_name_1 = "host_1";
  
  private int trxId_0 = 0;
  private int trxId_1 = 1;
  private String GsmSectorLdn_0 = "ManagedElement=1,BtsFunction=1,GsmSector=0";
  private String GsmSectorLdn_1 = "ManagedElement=1,BtsFunction=1,GsmSector=1";
  private String AbisIpLdn_0 = GsmSectorLdn_0 + ",AbisIp=0";
  private String AbisIpLdn_1 = GsmSectorLdn_1 + ",AbisIp=1";
  private String TrxLdn_0_in_GsmSector_0 = GsmSectorLdn_0 + ",Trx=" + trxId_0;
  private String TrxLdn_1_in_GsmSector_0 = GsmSectorLdn_0 + ",Trx=" + trxId_1;
  private String TrxLdn_0_in_GsmSector_1 = GsmSectorLdn_1 + ",Trx=" + trxId_0;
  private String TrxLdn_1_in_GsmSector_1 = GsmSectorLdn_1 + ",Trx=" + trxId_1;

  private final static String SWM_MO_LDN = "ManagedElement=1,SystemFunctions=1,SwM=1";
  private final static String DU_MO_LDN = "ManagedElement=1,Equipment=1,FieldReplaceableUnit=1";
  private final static String RU_MO_LDN = "ManagedElement=1,Equipment=1,FieldReplaceableUnit=2";

  
  /**
   * Description of test case for test reporting
   */
  @TestInfo(
      tcId = "WP3534 & WP3537",
      slogan = "Software Upgrade",
      requirementDocument = "-",
      requirementRevision = "-",
      requirementLinkTested = "-",
      requirementLinkLatest = "-",
      requirementIds = { "-" },
      verificationStatement = "-",
      testDescription = "verify the procedure of software upgrade",
      traceGuidelines = "N/A")
    /**
     * initial rbs handler
     */
    @Setup
    public void setup(){
	  setTestStepBegin("Start of setup()");
      nodeStatus = new NodeStatusHelper();
      assertTrue("Node did not reach a working state before test start", nodeStatus.waitForNodeReady());
      momHelper = new MomHelper();
      abisco = new AbiscoConnection();
      logger = Logger.getLogger(SoftwareUpgradeTest.class);
      rbs = Rm.getG2RbsList().get(0);
      moHandler = rbs.getManagedObjectHandler();

      setTestInfo("prepare ftp information");
      // auto test in G2 CI: using /proj/rbs-g2/tmp/sftp/upgrade/$NODE in
      // 10.68.108.50
      gratRbsUpgradePackage = getG2UP();

      setTestInfo("intial upgrade helper");
      upgradePackageHelperFactory = new UpgradePackageHelperFactory();
      gratUpgradeHelper = upgradePackageHelperFactory.upgradeHelper(rbs, gratRbsUpgradePackage);
      abisHelper = new AbisHelper();
      alarmHelper = new AlarmHelper();
      
      g2RbsHandler = new G2RbsHandler(rbs);
      
      checkAlarm();
      setTestStepEnd();
    }

  /**
   * fetch and store T&E log after every test case
   */
  @Teardown
    public void teardown(){
	  setTestStepBegin("Start of teardown()");
      g2RbsHandler.readAndStoreTeLog(rbs, false);
      g2RbsHandler.clearTeLog(rbs);
      nodeStatus.isNodeRunning();
      setTestStepEnd();
    }  


  private G2UpgradePackage getG2UP() {
	return rbs.getUpgradePackageOnFtp(rbs.getName());
  }
     
  @Test(timeOut = 600000)
  @Parameters({ "testId", "description" })
  public void prepareUpgradePackageStep(String testId, String description) {

    setTestCase(testId, description);

    setTestInfo("Preparing Upgrade Package");

    // Get the path to the UpgradePackage.
    String upDestinationPath = System
      .getProperty("cfg.sftpFilePathPrepend") + gratRbsUpgradePackage.getUpFilePathOnFtpServer();

    File upDestinationDir = new File(upDestinationPath);

    if (upDestinationDir.exists()) {
      // Clean up directory if it already exist
      setTestInfo("Cleaning up directory: " + upDestinationPath);
      try {
        FileUtils.cleanDirectory(upDestinationDir);
      } catch (IOException e) {
        throw new MsranJcatException("Failed to clean directory: "
            + upDestinationPath);
      }
    }

    String upgradePackageToUnpack = new String(upDestinationPath
        + "/CXP9024418_1.zip");
    File upgradePackageZip = new File(upgradePackageToUnpack);

    setTestInfo("FetchUP starts");
    // Fetch UP
    try {
      final int connectionTimeout = 10000; // 10 seconds
      final int readTimeout = 600000; // 10 minutes
      String upgragePackageUrl = System
        .getProperty("jcat.upgrade_up_path");
      setTestInfo("Downloading: " + upgragePackageUrl);
      FileUtils.copyURLToFile(new URL(upgragePackageUrl),
          upgradePackageZip, connectionTimeout, readTimeout);
    } catch (Exception e) {
      throw new MsranJcatException("Failed to download UpgradePackage", e);
    }
    setTestInfo("FetchUP complete");
    
    // Unpack UP
    ZipHelper.unzipFiles(upgradePackageZip, upDestinationDir);

    setTestInfo("prepareUpgradePackageStep complete");
  }

  /**
   * @name createAndPrepareUpgradePackage
   *
   * @param testId
   * @param description
   */
  @Test(groups={"createAndPrepare"}, timeOut = 3000000)
    @Parameters({"testId", "description"})
    public void createAndPrepareUpgradePackage(String testId, String description) {
      setTestCase(testId, description);

      setTestStepBegin("Create UpgradePackageMo");
      try{
        gratUpgradeHelper.createUpgradePackage();
      }catch(MsranJcatException e)
      {//to clarify root cause in report if MO was created failed with error
        logger.info("got the expected expection when trying to createUpgradePackage MO");
        ManagedObjectStructAttribute reportProgress = moHandler.getAttribute(SWM_MO_LDN, "reportProgress", ManagedObjectStructAttribute.class);
        String cause = reportProgress.getMember("resultInfo").getValue();
        throw new ComCliException(cause, e);
      }
      setTestStepEnd();

      setTestStepBegin("parepare");
      gratUpgradeHelper.prepareUpgradePackage();
      setTestStepEnd();

    }

  /**
   * verifyUpgradePackage
   *
   */
  public void verifyUpgradePackage() {

    setTestStepBegin("verify software upgrade pre-conditions");
    gratUpgradeHelper.verifyPreconditions();
    setTestStepEnd();
  }

  /**
   * @name activateUpgradePackage
   *
   */
  public void activateUpgradePackage() {

    setTestStepBegin("activate upgrade package");
    gratUpgradeHelper.activateUpgradePackage();
    setTestStepEnd();
  }

  /**
   * @name cancelUpgradePackage
   *
   */

  public void cancelUpgradePackage() {

    setTestStepBegin("cancel software upgrade");
    gratUpgradeHelper.cancelUpgradeAction();
    setTestStepEnd();
  }

  /**
   * @name confirmUpgradePackage
   *
   */
  public void confirmUpgradePackage() {
    setTestStepBegin("confirm upgrade package");
    gratUpgradeHelper.confirmUpgradeAction();
    setTestStepEnd();
  }
  
  /**
   * @throws InterruptedException 
 * @name establishOmlAndCheckMoState
   *
   */
  public void establishOmlAndCheckMoState() throws InterruptedException {

    setTestInfo("establish OML link for SCF and verify MO Unlocked after activated");
    
	// Establish Oml link for Scf
	setTestStepBegin("********* Establish OML link for Scf");
	establishScfOmlLinks();
	setTestStepEnd();
    
	setTestStepBegin("********* Check MO status of GsmSector, AbisIp, and Trx after activate, unlock, connect TG, and SCF OML established. GsmSector=" + GsmSectorLdn_0);
	checkMoStatusAfterUnlockedAbisIpAndTrxAndEstablishedScfOml(GsmSectorLdn_0, AbisIpLdn_0, TrxLdn_0_in_GsmSector_0, TrxLdn_1_in_GsmSector_0);
	setTestStepEnd();

	setTestStepBegin("********* Check MO status of GsmSector, AbisIp, and Trx after activate, unlock, connect TG, and SCF OML established. GsmSector=" + GsmSectorLdn_1);
	checkMoStatusAfterUnlockedAbisIpAndTrxAndEstablishedScfOml(GsmSectorLdn_1, AbisIpLdn_1, TrxLdn_0_in_GsmSector_1, TrxLdn_1_in_GsmSector_1);
	setTestStepEnd();
  }

  /**
   * @name checkAbisStatusUpdateAfterActivate
   *
   * @description check Abis Status Update After Activate.
   * 
   *  
   * @throws Exception
   */
  public void checkAbisStatusUpdateAfterActivate() throws Exception {

    setTestInfo("check abis status updated after activate");
    setTestStepBegin("check abis status updated after activated");

    G31StatusUpdate statusUpdate;
    int statusUpdateCnt = 0;  
    do {
      statusUpdate = abisHelper.getStatusUpdate(10, TimeUnit.SECONDS);
      if (statusUpdate != null) {
        G31StatusSCF soScfStatusUpdate = statusUpdate.getStatusChoice().getG31StatusSCF();
        if (soScfStatusUpdate != null) {
          G31OperationalCondition operationalCondition = soScfStatusUpdate.getG31OperationalCondition();
          
          
          assertEquals("G31 Stauts SCF Operatinal Condition is not NotOperational",
        		  Enums.OperationalCondition.NotOperational,
                  operationalCondition.getOperationalCondition());

          assertEquals("G31 Stauts SCF SuppressHandoverIndicator is not as expected",
        		  Enums.SuppressHandoverIndicator.DoNotSuppress,
                  operationalCondition.getSuppressHandoverIndicator());
                  
          
          G31OperationalConditionReasonsMap operationalConditionReasonMap =
                      soScfStatusUpdate.getG31OperationalConditionReasonsMap();
          
          assertTrue( operationalConditionReasonMap.getSWUpgrade() == 1 );  
          
          statusUpdateCnt++;
          setTestInfo("got an SoScf status update with BTSNodeIdentitySignature " + soScfStatusUpdate.getBTSNodeIdentitySignature() 
        		  + ": " + operationalCondition.getOperationalCondition().toString() 
        		  + " " + operationalCondition.getSuppressHandoverIndicator().toString()
        		  + " SwUpgrade=" + operationalConditionReasonMap.getSWUpgrade());
        }
      }
    } while (statusUpdate != null);
    assertEquals("G31 Status update for Software upgrade is not received as expected",
    		2,//2 sectors
    		statusUpdateCnt);
    setTestStepEnd();
  }

  /**
   * @name Verify Upgrade package when AbisIp MO is locked.
   *
   * NodebUC416.A6 Upgrade Node SW when GRAT MO AbisIp is locked
   * @param testId
   * @param description
   */

  @Test(timeOut = 200000)
    @Parameters({"testId", "description"})
    public void verifyUpgradePackageLockedAbisIp(String testId, String description) {
      setTestCase(testId, description);
      
  	  if (!momHelper.isBtsFunctionCreated()) {
  		  	setTestInfo("Precondition: Create BtsFunction MO");
  		  	momHelper.createBtsFunctionMo(MomHelper.BTS_FUNCTION_LDN);
  	  }

      setTestStepBegin("Create AbisIp MO,lock AbisIp MO");
      String abisIpLdn = momHelper.createSectorAndAbisIpMo(AbiscoConnection.getConnectionName(), abisco.getBscIpAddress(), false);
      setTestStepEnd();

      setTestStepBegin("lock AbisIp MO");
      momHelper.lockMo(abisIpLdn);
      setTestStepEnd();

      setTestStepBegin("check AbisIp MO");
      momHelper.checkAbisIpMoAttributeAfterLock(abisIpLdn, 5);
      setTestStepEnd();

      setTestStepBegin("verify Preconditions");
      gratUpgradeHelper.verifyPreconditions();
      setTestStepEnd();

      setTestStepBegin("check verify result ");
      ManagedObjectStructAttribute reportProgress = gratUpgradeHelper.getUpReportProgress();
      String cause = reportProgress.getMember("additionalInfo").getValue();
      String expectedResult = "gupc(CXC1738121_1): OK";
      assertTrue(cause.indexOf(expectedResult) >= 0);
      setTestStepEnd();
    }
  /**
   * @name Verify Upgrade package when AbisIp operstate is disable.
   *
   * @param testId
   * @param description
   */
  @Test(timeOut = 1800000)
    @Parameters({ "testId", "description"})
    public void verifyAbisIpOperstateDisable(String testId, String description) {
      setTestCase(testId, description);
      
  	  if (!momHelper.isBtsFunctionCreated()) {
		  	setTestInfo("Precondition: Create BtsFunction MO");
		  	momHelper.createBtsFunctionMo(MomHelper.BTS_FUNCTION_LDN);
	  }

      setTestStepBegin("Create AbisIp MO,set 0perstatus disable ");
      String abisIpLdn = momHelper.createSectorAndAbisIpMo(AbiscoConnection.getConnectionName(), "1.1.1.1", false);
      setTestStepEnd();

      setTestStepBegin("unlock AbisIp MO");
      momHelper.unlockMo(abisIpLdn);
      setTestStepEnd();

      setTestStepBegin("check AbisIp MO");
      momHelper.checkAbisIpMoAttributeAfterUnlockBscDisconnected(abisIpLdn,5);
      setTestStepEnd();

      setTestStepBegin("verify Preconditions");
      gratUpgradeHelper.verifyPreconditions();
      setTestStepEnd();

      setTestStepBegin("check verify result ");
      ManagedObjectStructAttribute reportProgress = gratUpgradeHelper.getUpReportProgress();
      String cause = reportProgress.getMember("additionalInfo").getValue();
      String expectedResult = "OK: ManagedElement=1,BtsFunction=1,GsmSector=1,AbisIp=1 is Disabled";
      assertTrue(cause.indexOf(expectedResult) >= 0);
      setTestStepEnd();
    }

  /**
   * @name Verify Upgrade package when AbisIp is unlocked but oml link is down.
   *
   * NodebUC416.A7 Precondition deviation, message in SW log
   * @param testId
   * @param description
   */
  @Test(timeOut = 200000)
    @Parameters({"testId", "description"})
    public void verifyUpgradePackageOmlDown(String testId, String description) {
      setTestCase(testId, description);
      
      // Setup Abisco
      abisco.setupAbisco(false);
      
  	  if (!momHelper.isBtsFunctionCreated()) {
		  	setTestInfo("Precondition: Create BtsFunction MO");
		  	momHelper.createBtsFunctionMo(MomHelper.BTS_FUNCTION_LDN);
	  }

      setTestStepBegin("Create AbisIp MO and gsm sector MO");
      String abisIpLdn = momHelper.createSectorAndAbisIpMo(AbiscoConnection.getConnectionName(), abisco.getBscIpAddress(), false);
      setTestStepEnd();

      setTestStepBegin("unlock AbisIp MO");
      momHelper.unlockMo(abisIpLdn);
      setTestStepEnd();

      setTestStepBegin("check AbisIp MO");
      momHelper.checkMoExist(abisIpLdn);
      setTestStepEnd();

      setTestStepBegin("check sector MO attribute");
      momHelper.checkGsmSectorMoAttributeAfterCreateNoLinks(MomHelper.SECTOR_LDN, 5);
      setTestStepEnd();

      setTestStepBegin("verify Preconditions");
      gratUpgradeHelper.verifyPreconditions();
      setTestStepEnd();

      setTestStepBegin("check verify result ");
      ManagedObjectStructAttribute reportProgress = gratUpgradeHelper.getUpReportProgress();
      String cause = reportProgress.getMember("additionalInfo").getValue();
      String expectedResult = "OK: ManagedElement=1,BtsFunction=1,GsmSector=1 has abisScfOmlState = Down";
      assertTrue(cause.indexOf(expectedResult) >= 0);
      setTestStepEnd();
      
      abisco.disconnectTG();
    }

  /**
   * @name Verify Upgrade package when AbisIp is unlocked but oml link is down.
   *
   * NodebUC416.A7 Precondition deviation, message in SW log
   * @param testId
   * @param description
   */
  @Test(timeOut = 200000)
    @Parameters({"testId", "description"})
    public void verifyUpgradePackageMultiSector(String testId, String description) {
      setTestCase(testId, description);

      // Setup Abisco
      abisco.setupAbisco(false);
      
  	  if (!momHelper.isBtsFunctionCreated()) {
		  	setTestInfo("Precondition: Create BtsFunction MO");
		  	momHelper.createBtsFunctionMo(MomHelper.BTS_FUNCTION_LDN);
	  }
      
      setTestStepBegin("Create GsmSector MO:s");
      creatMultieSectorAndAbisIp();
      setTestStepEnd();

      setTestStepBegin("verify Preconditions");
      gratUpgradeHelper.verifyPreconditions();
      setTestStepEnd();

      setTestStepBegin("check verify result ");
      ManagedObjectStructAttribute reportProgress = gratUpgradeHelper.getUpReportProgress();
      String cause = reportProgress.getMember("additionalInfo").getValue();
      String expectedResult = "OK: ManagedElement=1,BtsFunction=1,GsmSector=1 has abisScfOmlState = Down; ManagedElement=1,BtsFunction=1,GsmSector=2,AbisIp=1 is Disabled";
      assertTrue(cause.indexOf(expectedResult) >= 0);
      
      abisco.disconnectTG();
      setTestStepEnd();
    }

  /*
   * Create GsmSector MO:s  and AbisIp MO:s on AbisIp is enable, another is disable
   */
  public void creatMultieSectorAndAbisIp(){

    int noGsmSectors = 2;
    ArrayList<String> sectorLdnList = new ArrayList<String>();
    ArrayList<String> abisIpLdnList = new ArrayList<String>();
    restoreStack = Helpers.restore().restoreStackHelper().getRestoreStack();
    for (int i = 1; i < noGsmSectors + 1; i++) {
      String sectorLdn;
      sectorLdn = "ManagedElement=1,BtsFunction=1,GsmSector=" + i;
      String abisIpLdn = sectorLdn + ",AbisIp=1";
      if (!momHelper.checkMoExist(sectorLdn))
      {
        logger.info("Create GsmSector MO with LDN " + sectorLdn);

        ManagedObject sectorMo = momHelper.buildGsmSectorMo(sectorLdn);
        momHelper.createManagedObject(sectorMo);
        if (!momHelper.checkMoExist(abisIpLdn))
        {
          if (!momHelper.isTnConfigured())
            momHelper.configureTn(abisco.getBscIpAddress());

          logger.info("Create AbisIP MO");

          ManagedObject abisMo = momHelper.buildAbisIpMo(
              "host_" + (i-1),//AbiscoConnection.getConnectionName(),
              abisco.getBscIpAddress(),
              abisIpLdn,
              MomHelper.TNA_IP_LDN,
              MomHelper.LOCKED);

          momHelper.createManagedObject(abisMo);

        }
        abisIpLdnList.add(abisIpLdn);
        momHelper.unlockMo(abisIpLdn);
        sectorLdnList.add(sectorLdn);
        restoreStack.add(new DeleteMoCommand(sectorLdn));
        restoreStack.add(new LockDeleteMoCommand(abisIpLdn));
      }
    }
  }
  
  /**
   * sometimes the RU will not be operational after node restart.
   * check the state before we check GSM stuff
   */
  private void checkDuRuOpStateAfterRestart()
  {
	  setTestStepBegin("check DU state after restart");
	  assertEquals("DU is not operational after restart", "", momHelper.checkMoOperationalState(DU_MO_LDN, 30));
	  setTestStepEnd();
	  
	  setTestStepBegin("check RU state after restart");
	  assertEquals("RU is not operational after restart", "", momHelper.checkMoOperationalState(RU_MO_LDN, 30));
	  setTestStepEnd();
  }

  /**
   * @name NodebUC416,GRAT software upgrade verify activate and cancel on Dormant State.
   *
   * @param testId
   * @param description
   * @throws Exception
   *
   */
  @Test(timeOut = 1250000)
    @Parameters({"testId", "description"})
    public void verifyActivateConfirmOnNonDormantState(String testId, String description) throws Exception
    {
      setTestCase(testId, description);
      
      if (!momHelper.isBtsFunctionCreated()) {
    	  setTestInfo("Precondition: Create BtsFunction MO");
    	  momHelper.createBtsFunctionMo(MomHelper.BTS_FUNCTION_LDN);
      }


      createMultipleSectorsAndTrxesEstablishOmlLinks();
      
      verifyUpgradePackage();

      abisHelper.clearStatusUpdate();
      
      activateUpgradePackage();       
      
      checkDuRuOpStateAfterRestart();
      
      checkAbisStatusUpdateAfterActivate();
      
      checkAlarm();
      
      establishOmlAndCheckMoState();      
      confirmUpgradePackage();
      deleteAndDisconnectTg();
    }

  /*
   * @name checkAlarm
   */
  public void checkAlarm()
  {
    setTestStepBegin("check alarm ");
    List<ComAlarm>  alarmList = alarmHelper.getAlarmList();
    setTestInfo("-----START of alarm info list-----\n");
    if (alarmList != null)
    {
      for (ComAlarm alarm : alarmList) {
        setTestInfo("Probable Cause: " + alarm.getProbableCause());
        setTestInfo("Perceived Severity: " + alarm.getSeverity());
        setTestInfo("Specific Problem: " + alarm.getSpecificProblem());
        setTestInfo("Event Type: " + alarm.getEventType());
        setTestInfo("Managed Object: " + alarm.getManagedObject());
        setTestInfo("Major Type: " + alarm.getMajorType());
        setTestInfo("Minor Type: " + alarm.getMinorType());
        setTestInfo("-----");
      }
    }
    setTestInfo("-----END of the alarm info list-----\n");

    //assertTrue("no alarm is expected before and after software upgrade", alarmList.isEmpty());
    setTestStepEnd();
  }
  
  private void disconnectAndDeleteTg(int tgId) {
  	abisco.disconnectTG(tgId);
  	abisco.deleteTG(tgId);
  }
  
  private void createTgsAndCells() {
  	try {
  		// create two TG with two TRX
  		abisco.createTgPreDefBundling(tg_0, connection_name_0, 2, false);
  		abisco.defineCell(tg_0, 2);
  		abisco.createTgPreDefBundling(tg_1, connection_name_1, 2, false);
  		abisco.defineCell(tg_1, 2);
  	} catch (InterruptedException e) {
  		setTestInfo("Caught InterruptedException");
  		e.printStackTrace();
  	}
  }
  
  private void checkTrxMoAttributeAfterCreateUnlockNoLinks(String trxLdn) {
  	assertEquals("Trx MO attributes did not reach expected values", "", momHelper.checkTrxMoAttributeAfterUnlockNoLinks(trxLdn, 30));
  }
  
  private void createUnlockMosForOneSector(
  		String sectorLdn,
  		String abisIpLdn,
  		String trx0ldn,
  		String trx1ldn,
  		String connection_name) {
	  
	setTestInfo("Build MO objects");	  
	/*
     * create btsFunction MO
     */
    setTestStepBegin("Create BtsFunction MO");
    if (!momHelper.checkMoExist(MomHelper.BTS_FUNCTION_LDN))
    {
      ManagedObject btsFunc = momHelper.buildBtsFunctionMo(MomHelper.BTS_FUNCTION_LDN, MomHelper.BTS_USER_LABEL_VALUE);
      momHelper.createManagedObject(btsFunc);
      assertTrue(momHelper.checkMoExist(MomHelper.BTS_FUNCTION_LDN));
      assertEquals("BtsFunction MO attributes did not reach expected values", "", momHelper.checkBtsFunctionMoAttributesAfterCreation(MomHelper.BTS_FUNCTION_LDN, 5));
    }
    setTestStepEnd();
  	// specify which MOs that shall be created together, and their parameter values
  	List<ManagedObject> createMos = new ArrayList<ManagedObject>();
  	
  	ManagedObject gsmSectorMo = momHelper.buildGsmSectorMo(sectorLdn);
  	createMos.add(gsmSectorMo);
  	RestoreCommand restoreGsmSectorMoCmd = new DeleteMoCommand(gsmSectorMo.getLocalDistinguishedName());
  	momHelper.addRestoreCmd(restoreGsmSectorMoCmd);

  	ManagedObject abisIpMo = momHelper.buildAbisIpMo(
  			connection_name,
  			abisco.getBscIpAddress(),
  			abisIpLdn,
  			MomHelper.TNA_IP_LDN,
  			MomHelper.UNLOCKED);
  	createMos.add(abisIpMo);
  	RestoreCommand restoreAbisIpMoCmd = new LockDeleteMoCommand(abisIpMo.getLocalDistinguishedName());
  	momHelper.addRestoreCmd(restoreAbisIpMoCmd);

  	ManagedObject trxMo =  momHelper.buildTrxMo(trx0ldn, MomHelper.UNLOCKED, momHelper.getSectorEquipmentFunctionLdn());
  	createMos.add(trxMo);
  	RestoreCommand restoreTrxMoCmd = new LockDeleteMoCommand(trxMo.getLocalDistinguishedName());
  	momHelper.addRestoreCmd(restoreTrxMoCmd);
  	
  	ManagedObject trxMo1 =  momHelper.buildTrxMo(trx1ldn, MomHelper.UNLOCKED, momHelper.getSectorEquipmentFunctionLdn());
  	createMos.add(trxMo1);
  	RestoreCommand restoreTrxMoCmd1 = new LockDeleteMoCommand(trxMo1.getLocalDistinguishedName());
  	momHelper.addRestoreCmd(restoreTrxMoCmd1);

  	setTestInfo("Create all MOs");
  	momHelper.createSeveralMoInOneTx(createMos);
  	
  	checkTrxMoAttributeAfterCreateUnlockNoLinks(trx1ldn);
  }
  
  private void checkGsmSectorMoAttributeAfterCreateNoLinks(String gsmSectorLdn) {
  	assertEquals("GsmSector MO attributes did not reach expected values", "", momHelper.checkGsmSectorMoAttributeAfterCreateNoLinks(gsmSectorLdn, 10));
  }
  
  private void checkAbisIpMoAttributeAfterCreateUnlockBscConnected(String abisIpLdn) {
  	assertEquals("AbisIp MO attributes did not reach expected values", "", momHelper.checkAbisIpMoAttributeAfterUnlockBscConnected(abisIpLdn, abisco.getBscIpAddress(), 30));
  }
  
  private void checkMoStatusAfterUnlockedAbisIpAndTrxAndConnectedTG(String gsmSectorLdn, String abisIpLdn, String trxLdn0, String trxLdn1) {
  	checkGsmSectorMoAttributeAfterCreateNoLinks(gsmSectorLdn);
  	checkAbisIpMoAttributeAfterCreateUnlockBscConnected(abisIpLdn);
  	checkTrxMoAttributeAfterCreateUnlockNoLinks(trxLdn0);
  	checkTrxMoAttributeAfterCreateUnlockNoLinks(trxLdn1);
  }
  
  
  private void establishScfOmlLinks() throws InterruptedException {
  	establishScfOmlLinkForTg(tg_0);
  	establishScfOmlLinkForTg(tg_1);
  }
  
  @SuppressWarnings("unused")
private void establishAllLinks() throws InterruptedException {
  	establishAllLinksForTg(tg_0);
  	establishAllLinksForTg(tg_1);
  }
  
  private void establishScfOmlLinkForTg(int tgId) throws InterruptedException {
  	abisco.establishLinks(tgId, false, trxId_0);
  	abisco.establishLinks(tgId, false, trxId_1);
  }
  
  private void establishAllLinksForTg(int tgId) throws InterruptedException {
  	abisco.establishLinks(tgId, true, trxId_0);
  	abisco.establishLinks(tgId, true, trxId_1);
  }
  
  private void checkGsmSectorMoAttributeAfterCreateLinkEstablished(String gsmSectorLdn) {
  	assertEquals("GsmSector MO attributes did not reach expected values", "", momHelper.checkGsmSectorMoAttributeAfterCreateLinksEstablished(gsmSectorLdn, 20));
  }
  
  private void checkMoStatusAfterUnlockedAbisIpAndTrxAndEstablishedScfOml(String gsmSectorLdn, String abisIpLdn, String trxLdn0, String trxLdn1) {
  	checkGsmSectorMoAttributeAfterCreateLinkEstablished(gsmSectorLdn);
  	checkAbisIpMoAttributeAfterCreateUnlockBscConnected(abisIpLdn);
  	checkTrxMoAttributeAfterCreateUnlockNoLinks(trxLdn0);
  	checkTrxMoAttributeAfterCreateUnlockNoLinks(trxLdn1);
  }
  
  private void checkGsmSectorMoAttributeAfterCreateLinkEstablishedAllMosStartedAoAtEnabled(String gsmSectorLdn) {
  	assertEquals("GsmSector MO attributes did not reach expected values", "", momHelper.checkGsmSectorMoAttributeAfterAllMosStartedAoAtEnabled(gsmSectorLdn, 10));
  } 
  
  private void checkTrxMoAttributeAfterCreateUnlockLinksEstablished(String trxLdn) {
  	assertEquals("Trx MO attributes did not reach expected values", "", momHelper.checkTrxMoAttributeAfterUnlockLinksEstablished(trxLdn, 30));
  }
  
  @SuppressWarnings("unused")
private void checkMoStatusAfterUnlockedAbisIpAndTrxAndMosSoScfAoTfStartedAoAtEnabledAllLinksUp(String gsmSectorLdn, String abisIpLdn, String trxLdn0, String trxLdn1) {
  	checkGsmSectorMoAttributeAfterCreateLinkEstablishedAllMosStartedAoAtEnabled(gsmSectorLdn);
  	checkAbisIpMoAttributeAfterCreateUnlockBscConnected(abisIpLdn);
  	checkTrxMoAttributeAfterCreateUnlockLinksEstablished(trxLdn0);
  	checkTrxMoAttributeAfterCreateUnlockLinksEstablished(trxLdn1);    
  }
  /**
   * @name NodebUC416,GRAT software upgrade verify activate and cancel on Dormant State.
   *
   * @param testId
   * @param description
   * @throws InterruptedException 
   *
   */
  public void createMultipleSectorsAndTrxesEstablishOmlLinks() throws InterruptedException
  { 

    setTestStepBegin("TN");
    if (!momHelper.isTnConfigured())
  	  momHelper.configureTn(abisco.getBscIpAddress());
  
  
    setTestStepBegin("********* Pre-test: Make sure that TG:s are disconnected and deleted");
    disconnectAndDeleteTg(tg_0);
    disconnectAndDeleteTg(tg_1);
    setTestStepEnd();

    setTestStepBegin("********* Pre-test: Start TSS");
    abisco.startTss();
    setTestStepEnd();
    
    setTestStepBegin("********* Pre-test: Create two TGs with two TRX:es each");
    createTgsAndCells();
    setTestStepEnd();
       
	setTestStepBegin("********* Create all MO:s for first sector");
	createUnlockMosForOneSector(GsmSectorLdn_0, AbisIpLdn_0, TrxLdn_0_in_GsmSector_0, TrxLdn_1_in_GsmSector_0, connection_name_0);
	setTestStepEnd();

	setTestStepBegin("********* Create all MO:s for second sector");
	createUnlockMosForOneSector(GsmSectorLdn_1, AbisIpLdn_1, TrxLdn_0_in_GsmSector_1, TrxLdn_1_in_GsmSector_1, connection_name_1);
	setTestStepEnd();
	
	// no SO SCF started, so we can not check for any SCF Status Updates here from the create and unlock trx

	setTestStepBegin("********* Connect tg");
	abisco.connectTG(tg_0);
	abisco.connectTG(tg_1);
	setTestStepEnd();
	
	setTestStepBegin("********* Check MO status of GsmSector, AbisIp, and Trx after MO creation, unlock, and connect TG. GsmSector=" + GsmSectorLdn_0);
	checkMoStatusAfterUnlockedAbisIpAndTrxAndConnectedTG(GsmSectorLdn_0, AbisIpLdn_0, TrxLdn_0_in_GsmSector_0, TrxLdn_1_in_GsmSector_0);
	setTestStepEnd();

	setTestStepBegin("********* Check MO status of GsmSector, AbisIp, and Trx after MO creation, unlock, and connect TG. GsmSector=" + GsmSectorLdn_1);
	checkMoStatusAfterUnlockedAbisIpAndTrxAndConnectedTG(GsmSectorLdn_1, AbisIpLdn_1, TrxLdn_0_in_GsmSector_1, TrxLdn_1_in_GsmSector_1);
	setTestStepEnd();
	
	// Establish Oml link for Scf
	setTestStepBegin("********* Establish OML link for Scf");
	establishScfOmlLinks();
	setTestStepEnd();
	
	setTestStepBegin("********* Check MO status of GsmSector, AbisIp, and Trx after MO creation, unlock, connect TG, and SCF OML established. GsmSector=" + GsmSectorLdn_0);
	checkMoStatusAfterUnlockedAbisIpAndTrxAndEstablishedScfOml(GsmSectorLdn_0, AbisIpLdn_0, TrxLdn_0_in_GsmSector_0, TrxLdn_1_in_GsmSector_0);
	setTestStepEnd();

	setTestStepBegin("********* Check MO status of GsmSector, AbisIp, and Trx after MO creation, unlock, connect TG, and SCF OML established. GsmSector=" + GsmSectorLdn_1);
	checkMoStatusAfterUnlockedAbisIpAndTrxAndEstablishedScfOml(GsmSectorLdn_1, AbisIpLdn_1, TrxLdn_0_in_GsmSector_1, TrxLdn_1_in_GsmSector_1);
	setTestStepEnd();	
  }
  
  /**
   * @name deleteAndDisconnectTg
   *
   * @throws InterruptedException 
   *
   */
  public void deleteAndDisconnectTg() throws InterruptedException
  {
  	setTestStepBegin("********* Disconect TG " + tg_0);
  	abisco.disconnectTG(tg_0);
  	setTestStepEnd();

  	setTestStepBegin("********* Disconect TG " + tg_1);
  	abisco.disconnectTG(tg_1);
  	setTestStepEnd();
  }

}




