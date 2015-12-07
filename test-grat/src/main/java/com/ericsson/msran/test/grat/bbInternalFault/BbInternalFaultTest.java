package com.ericsson.msran.test.grat.bbInternalFault;

import org.json.JSONException;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import se.ericsson.jcat.fw.annotations.Setup;
import se.ericsson.jcat.fw.annotations.Teardown;

import com.ericsson.msran.g2.annotations.TestInfo;
import com.ericsson.msran.test.grat.testhelpers.NodeStatusHelper;
import com.ericsson.commonlibrary.remotecli.Cli;
import com.ericsson.msran.jcat.TestBase;
import com.ericsson.commonlibrary.resourcemanager.g2.G2Rbs;
import com.ericsson.commonlibrary.resourcemanager.Rm;
import com.ericsson.msran.test.grat.testhelpers.MomHelper;
import com.ericsson.msran.test.grat.testhelpers.AbisHelper;
import com.ericsson.msran.test.grat.testhelpers.prepost.AbisPrePost;
import com.ericsson.abisco.clientlib.servers.OM_G31R01;

/**
 * @id GRAT000009,GRAT000010
 * 
 * @name BbInternalFaultTest
 * 
 * @author Zhuo Cheng
 * 
 * @created 2015-08-31
 * 
 * @description 
 * 
 * @revision ezhouch 2015-08-31 first version
 * @revision ezhonsu 2015-09-14 add case resetBoard and escalation.
 * 
 */

/**
CLI command is used to inject the BB fault of the system
cli.send("gratBbFaultInject " + trxldn + rspfault + tspfault);
root@du1:/var/run/coli# gratBbFaultInject help
Used in JCAT tests to set a faultmap bit in GBB
First argument: Trx instance (trx ldn)
Second argument: Fault map bit definition for GsmRsp:
   0 = TX_Burst_Message_Fault
   1 = FM_RX_Ctrl_Message_Fault
   2 = FM_TX_PowerPrediction_Message_Fault
  13 = FM_RX_FS_Info_Mismatch_A
  14 = FM_RX_FS_Info_Mismatch_B
  17 = FM_BFN_LON
  25 = FM_IQC_DL_Message_Error
  -1 = no fault
Third argument: Fault map bit definition for GsmTsp:
   0 = RC_FAULTS_MISSING_RX_DATA
   1 = RC_FAULTS_BB_MAP_TABLE_EMPTY
  -1 = no fault
 */

public class BbInternalFaultTest extends TestBase {

	private G2Rbs rbs;
	private Cli cli;
	private NodeStatusHelper nodeStatus;
	private MomHelper momHelper;
	private AbisHelper abisHelper;
	private AbisPrePost abisPrePost;
	private final OM_G31R01.Enums.MOClass moClassTrxc = OM_G31R01.Enums.MOClass.TRXC;
	private final OM_G31R01.Enums.MOClass moClassRx = OM_G31R01.Enums.MOClass.RX;  
	//private int secondsForNodeUpAndTrxEnabled = 420;
    private String gratDumpPath = "/rcs/applicationlogs/GRAT_CXP9023458_*/";
    private String fileNameTxt1 = "*dump.gte*.txt.gz";
    private String fileNameTxt2 = "*dump.gdlh*.txt.gz";
    private String dumpPath = "/rcs/dumps/pmd/*/";
    private String fileName1 = "pmd-*tinyose*";
    private String fileName2 = "pmd-*GDLH*";
    private boolean rmDumpNeeded = false;      
    
	/**
	 * Description of test case for test reporting
	 */
	@TestInfo(
			tcId = "UC629",
			slogan = "Verify that the correct recovery action is taken when BB internal fault is detected.",
			requirementDocument = "-",
			requirementRevision = "-",
			requirementLinkTested = "-",
			requirementLinkLatest = "-",
			requirementIds = { "105 65-0771/02946, 105 65-0771/02947, 105 65-0771/02949, 105 65-0771/02950" },
			verificationStatement = "Verifies NodeUC629.A2, NodeUC629.A3, UC629.A4 and NodeUC629.A5",
			testDescription = "Verify that the correct recovery action is taken when BB internal fault is detected.",
			traceGuidelines = "N/A")

	/**
	 * Precond.
	 */
	@Setup
	public void setup() throws InterruptedException, JSONException {
		setTestStepBegin("Start of setup()");
		nodeStatus = new NodeStatusHelper();
		assertTrue("Node did not reach a working state before test start", nodeStatus.waitForNodeReady());     
		rbs = Rm.getG2RbsList().get(0);
		cli = rbs.getLinuxShell();
		abisHelper = new AbisHelper();
		momHelper = new MomHelper();
		abisPrePost=new AbisPrePost();
		abisPrePost.preCondAllMoStateStarted();
		setTestStepEnd();
	}

	/**
	 * Postcond.
	 */
    @Teardown
    public void teardown() {
    	setTestStepBegin("Teardown");
        nodeStatus.isNodeRunning();
        if(rmDumpNeeded == true)
        {
          rmExpectedDumpFiles();
        }
        setTestStepEnd();
    }
	/**
	 * @name makeReoveryFromResetBoard
	 * @param testId 
	 * @param description
	 * @throws JSONException 
	 * @throws InterruptedException 
	 */
	@Test(timeOut = 1000000)
	@Parameters({ "testId", "description"})
	public void makeReoveryFromResetBoard(String testId, String description) throws InterruptedException, JSONException {

		setTestCase(testId, description);

		setTestStepBegin("Get Trx ldn");
		abisHelper.clearStatusUpdate();
		String trxLdn = MomHelper.TRX_LDN;
		assertTrue("Trx MO " + trxLdn + " does not exist as expected", momHelper.checkMoExist(trxLdn));
		setTestStepEnd();

		//Case: fault bit (17) = FFM_BFN_LON is detected -> Reset Board
		for (int i = 0; i<5; i++)
		{
			//Send every half second
			setTestStepBegin("\n To inject the GsmRsp fault bit (17) = FFM_BFN_LON ");
			cli.send("gratBbFaultInject " + trxLdn + " 17" + " -1");
			sleepMilliseconds(500);
			setTestStepEnd();
		}
		//verify reset board
		//sleepSeconds(secondsForNodeUpAndTrxEnabled);
		//To short the sleep time
		sleepSeconds(120);
		for (int i=40; i >0 && (!nodeStatus.isNodeRunning()); i--)
		{
			setTestStepBegin("Sleep 10s more before the node is up and running ");
			sleepSeconds(10);
			setTestStepEnd();
		}
		sleepSeconds(20);
		rmDumpNeeded = true;
		
		setTestStepBegin("Verify reset board");
		verifyStatusOfResetBoard();
		setTestStepEnd();
	}

	/**
	 * @name makeReoveryFromEscalation
	 * @param testId 
	 * @param description
	 * @throws JSONException 
	 * @throws InterruptedException 
	 */
	@Test(timeOut = 1000000)
	@Parameters({ "testId", "description"})
	public void makeReoveryFromEscalation(String testId, String description) throws InterruptedException, JSONException {

		setTestCase(testId, description);

		setTestStepBegin("Get Trx ldn");
		abisHelper.clearStatusUpdate();
		String trxLdn = MomHelper.TRX_LDN;
		assertTrue("Trx MO " + trxLdn + " does not exist as expected", momHelper.checkMoExist(trxLdn));
		setTestStepEnd();

		setTestInfo("Enable Ao Rx");
		enableAoRx();
		//Wait 5 minutes to trigger the first fault, this time cannot be shortened otherwise it is a node restart is triggered directly.
		sleepSeconds(300);
		//Case: fault bit (13) = FM_RX_FS_Info_Mismatch_A is detected two times -> Reset AO RX and reset board (Escalation)
		for (int i = 0; i<5; i++)
		{
			//Send every half second
			setTestStepBegin("\n To inject the GsmRsp fault bit (13) = FM_RX_FS_Info_Mismatch_A ");
			cli.send("gratBbFaultInject " + trxLdn + " 13" + " -1");
			sleepMilliseconds(500);
			setTestStepEnd();	
		}
		//verify RX reset
		setTestStepBegin("Verify the reset of AO RX");
		sleepSeconds(10);
		verifyStatusOfResetAoRx();
		setTestStepEnd();

		setTestInfo("start RX");
		abisHelper.startRequest(this.moClassRx, 0);
		setTestInfo("Enable Ao Rx");
		enableAoRx();
		for (int i = 0; i<5; i++)
		{
			//Send every half second
			setTestStepBegin("\n To inject the GsmRsp fault bit (14) = FM_RX_FS_Info_Mismatch_B ");
			cli.send("gratBbFaultInject " + trxLdn + " 14" + " -1");
			sleepMilliseconds(500);
			setTestStepEnd();
		}
		//verify reset board	
		//sleepSeconds(secondsForNodeUpAndTrxEnabled);
		//To short the sleep time
		sleepSeconds(120);
		for (int i=40; i >0 && (!nodeStatus.isNodeRunning()); i--)
		{
			setTestStepBegin("Sleep 10s more before the node is up and running ");
			sleepSeconds(10);
			setTestStepEnd();
		}
		sleepSeconds(20);
		rmDumpNeeded = true;
		
		setTestStepBegin("Verify Escalation");
		verifyStatusOfResetBoard();
		setTestStepEnd();
	}

	/**
	 * @name makeReoveryFromResetSoTrxc
	 * @param testId 
	 * @param description
	 * @throws JSONException 
	 * @throws InterruptedException 
	 */
	@Test(timeOut = 500000)
	@Parameters({ "testId", "description"})
	public void makeReoveryFromResetSoTrxc(String testId, String description) throws InterruptedException, JSONException {

		setTestCase(testId, description);
		rmDumpNeeded = false;

		setTestStepBegin("Get Trx ldn");
		abisHelper.clearStatusUpdate();
		String trxLdn = MomHelper.TRX_LDN;
		assertTrue("Trx MO " + trxLdn + " does not exist as expected", momHelper.checkMoExist(trxLdn));
		setTestStepEnd();

		setTestInfo("Enable Ao Rx");
		enableAoRx();
		//Wait 5 minutes to trigger the first fault, this time cannot be shortened otherwise it is a node restart will be triggered directly.
		sleepSeconds(300);

		//Case: fault bit of GsmTsp (0) = RC_FAULTS_MISSING_RX_DATA -> Reset So Trx
		for (int i = 0; i<5; i++)
		{
			//Send every half second
			setTestStepBegin("\n To inject the GsmRsp fault bit (0) = RC_FAULTS_MISSING_RX_DATA ");
			cli.send("gratBbFaultInject " + trxLdn + " -1" + " 0");
			sleepMilliseconds(500);
			setTestStepEnd();	
		}
		//verify So Trxc reset
		setTestStepBegin("Verify the reset of So Trxc");
		sleepSeconds(10);
		verifyStatusOfResetSoTrxc();
		setTestStepEnd();
	}

	/**
	 * @throws InterruptedException 
	 * @throws JSONException 
	 */
	private void enableAoRx() throws InterruptedException, JSONException {
		setTestStepBegin("Verify that MO State is DISABLED");
		saveAssertTrue("abisRxMoState is not DISABLED", momHelper.waitForAbisRxMoState("DISABLED"));
		setTestStepEnd();

		setTestStepBegin("Configure AO RX");
		OM_G31R01.RXConfigurationResult confRes = abisHelper.rxConfigRequest(0, momHelper.getArfcnToUse(), false, "");
		saveAssertEquals("According Indication must be According to Request",
				OM_G31R01.Enums.AccordanceIndication.AccordingToRequest,
				confRes.getAccordanceIndication().getAccordanceIndication());        
		setTestStepEnd();

		setTestStepBegin("Send Enable Request to AO RX");
		OM_G31R01.EnableResult result = abisHelper.enableRequest(this.moClassRx, 0);
		setTestStepEnd();

		setTestStepBegin("Verify that MO State in Enable Result is ENABLED");
		saveAssertEquals("MoState is not ENABLED", OM_G31R01.Enums.MOState.ENABLED, result.getMOState());
		setTestStepEnd();

		setTestStepBegin("Verify that MO:Trx attribute:abisRxMoState is ENABLED");
		saveAssertTrue("abisRxMoState is not ENABLED", momHelper.waitForAbisRxMoState("ENABLED"));
		setTestStepEnd();
	}

	/**
	 * @throws InterruptedException 
	 * @throws JSONException 
	 */

	private void verifyStatusOfResetSoTrxc() throws InterruptedException, JSONException {
		//A2.4 The RBS internally resets the TRXC and clears all configuration signatures 

		//A2.5 TRXC sends Status Update to BSC with state=RESET and Operational Condition = Operational 
		OM_G31R01.Enums.MOState soTrxcState;
		OM_G31R01.StatusResponse trxcStatusRsp = abisHelper.statusRequest(this.moClassTrxc);
		setTestStepBegin("Verify that MO State in Status Response is RESET.");
		soTrxcState = trxcStatusRsp.getMOState();
		final OM_G31R01.Enums.MOState expectedMoState = OM_G31R01.Enums.MOState.RESET;       
		saveAssertEquals("soTrxcState is not what expected", expectedMoState, soTrxcState);
		setTestStepEnd();
		setTestStepBegin("Verify that Operational Condition in Status Response is Operational.");
		saveAssertEquals("Operational Condition must be Operational for SO TRX" +
				"C", OM_G31R01.Enums.OperationalCondition.Operational.getValue(), trxcStatusRsp.getStatusChoice().getStatusTRXC().getOperationalCondition().getOperationalCondition().getValue());
		setTestStepEnd();

		//A2.6 The RBS updates Trx MO attributes with operationalState = Enabled, availabilityStatus=<empty>, abisTxState=Reset, abisRxState=Reset, abisTsState=Reset, abisTrxcState=Reset
		//Trx MO has attributes: -administrativeState=UNLOCKED -operationalState = ENABLED -availabilityStatus=<empty> 
		String trxLdn = MomHelper.TRX_LDN;
		assertEquals(trxLdn + " did not reach correct state", "", momHelper.checkTrxMoAttributeAfterResetSoTrxOrResetBoard(trxLdn, 10));   	
	}

	private void verifyStatusOfResetAoRx() throws InterruptedException {
		//A4.5. The RBS internally resets the RX and clears its configuration signature. 

		//A4.6. RX sends Status Update to BSC with MO state=RESET and Operational Condition=Operational.
		setTestStepBegin("Verify that MO:Trx attribute:abisRxMoState is RESET");
		saveAssertTrue("abisRxMoState is not RESET", momHelper.waitForAbisRxMoState("RESET"));
		setTestStepEnd();

		//A4.7. TRXC sends Status Update to BSC with Operational Condition = Operational. 
		OM_G31R01.StatusResponse trxcStatusRsp = abisHelper.statusRequest(this.moClassTrxc);
		setTestStepBegin("Verify that Operational Condition in Status Response is Operational.");
		saveAssertEquals("Operational Condition must be Operational for SO TRXC", OM_G31R01.Enums.OperationalCondition.Operational.getValue(), trxcStatusRsp.getStatusChoice().getStatusTRXC().getOperationalCondition().getOperationalCondition().getValue());
		setTestStepEnd();

		//A4.8. The RBS updates Trx MO attributes operationalState=Enabled, availabilityStatus=<empty>, abisRxState=Reset 
		String trxLdn = MomHelper.TRX_LDN;
		assertEquals(trxLdn + " did not reach correct state", "", momHelper.checkTrxMoAttributeAfterResetAoRx(trxLdn, 10));
	}
	
	private void verifyStatusOfResetBoard() throws JSONException{
		//Trx MO has attributes: -administrativeState=UNLOCKED -operationalState = ENABLED -availabilityStatus=<empty> 
		String trxLdn = MomHelper.TRX_LDN;
		assertEquals(trxLdn + " did not reach correct state", "", momHelper.checkTrxMoAttributeAfterResetSoTrxOrResetBoard(trxLdn, 10));
	}	

	private void rmExpectedDumpFiles() {
        if(Integer.parseInt(cli.send("ls -l " + dumpPath + fileName1 + " | wc -l")) == 2 ) //two GTE dump files
        {
          cli.send("rm -f " + gratDumpPath + fileNameTxt1);
          cli.send("rm -rf " + gratDumpPath + "dspdumps");
          cli.send("ls -a " + dumpPath + fileName1 + " | sed 's/pmd-.*//g' | xargs rm -rf ");        	
        }
        if(Integer.parseInt(cli.send("ls -l " + dumpPath + fileName2 + " | wc -l")) == 1 ) //one GDLH dump file
        {
          cli.send("rm -f " + gratDumpPath + fileNameTxt2);
          cli.send("ls -a " + dumpPath + fileName2 + " | sed 's/pmd-.*//g' | xargs rm -rf ");
        }
	}

}
