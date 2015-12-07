package com.ericsson.msran.test.grat.firstcall;

import java.util.HashMap;
import java.util.List;
import java.util.Arrays;
import java.util.Map;

//import org.apache.commons.lang.ArrayUtils;
import org.json.JSONException;

import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.ericsson.msran.jcat.TestBase;
import se.ericsson.jcat.fw.annotations.Setup;
import se.ericsson.jcat.fw.annotations.Teardown;

import com.ericsson.abisco.clientlib.servers.OM_G31R01;
import com.ericsson.abisco.clientlib.servers.PARAMDISP.TrafficCommand_Result;
import com.ericsson.commonlibrary.remotecli.Cli;
import com.ericsson.commonlibrary.resourcemanager.Rm;
import com.ericsson.commonlibrary.resourcemanager.g2.G2Rbs;
import com.ericsson.msran.g2.annotations.TestInfo;
import com.ericsson.msran.test.grat.testhelpers.AbisHelper;
import com.ericsson.msran.test.grat.testhelpers.ImsiHelper;
import com.ericsson.msran.test.grat.testhelpers.MomHelper;
import com.ericsson.msran.test.grat.testhelpers.NodeStatusHelper;
import com.ericsson.msran.test.grat.testhelpers.prepost.AbisPrePost;
import com.ericsson.mssim.tstm.Tstm;
import com.ericsson.mssim.tstm.TstmMobile;
import com.ericsson.mssim.tstm.statistics.SpeechStatistics;
import com.ericsson.mssim.tstm.statistics.TstmStatisticalCounters;
import com.ericsson.mssim.tstm.statistics.TstmStatistics;
import com.ericsson.mssim.tstm.statistics.DataStatistics;


/**
 * @id FirstCall
 * @name FirstCallTest
 * @description This test class verifies the Enable Request
 */

public class PayloadTest extends TestBase {

	private final OM_G31R01.Enums.MOClass moClassTx = OM_G31R01.Enums.MOClass.TX;
	private final OM_G31R01.Enums.MOClass moClassRx = OM_G31R01.Enums.MOClass.RX;
	private final OM_G31R01.Enums.MOClass moClassTs = OM_G31R01.Enums.MOClass.TS;


	private AbisPrePost abisPrePost;
	private AbisHelper abisHelper;
	private MomHelper momHelper;
	private G2Rbs rbs;
	private Cli cli;



	private NodeStatusHelper nodeStatus;
	//private CliCommands cliCommands;

	 private static final String HOST = "sekivlx93.lmera.ericsson.se";

	    final static String CELL1 = "2";
	    
	    final String cellList = "4 5 6 7";

	    Tstm tstm;
	    //Response response;

	    //private static final String LOGFILE_NAME = "grat/log/ms.log";
	    private static final int TIMEOUT = 30;
	    private static final String MSSIM_FTP_LOGIN = "user";
	    private static final String MSSIM_FTP_PASSWORD = "user";
	    private static final String FILES_SOURCE_LOCATION = "C:\\Temp\\mssim"; //Use your location
	    private static final String MS_DATABASE_FILE_NAME = "grat/csv/ms.csv";
	    private static final String CONFOGURATION_FILE_NAME = "grat/cfg/ms.tsm";
	    private static final String TARGET_LOCATION_FOR_CONFIGURATION_FILES = "/home/user/um_tstm";
	    private static final int PORT = 22;
	    

    /**
	 * Description of test case for test reporting
	 */
	@TestInfo(tcId = "First Call", slogan = "First Call Embryo", requirementDocument = "1/00651-FCP 130 1402", requirementRevision = "PC5", requirementLinkTested = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff87c1d941?docno=1/00651-FCP1301402Uen&format=excel8book", requirementLinkLatest = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff86a2af3d?docno=1/00651-FCP1301402Uen&action=current&format=excel8book", requirementIds = {
			"10565-0334/21767[PA1][PREL]", "10565-0334/19034[A][APPR]",
			"10565-0334/19417[A][APPR]" }, verificationStatement = "Verifies Part of First Call", testDescription = "Verifies First Call", traceGuidelines = "N/A")
	/**
	 * Make sure ricm patch is running, create MO:s, establish links to Abisco, and verify preconditions.
	 * 
	 * @throws InterruptedException
	 * @throws JSONException 
	 */
	@Setup
	public void setup() throws InterruptedException, JSONException {
		setTestStepBegin("Setup");
		nodeStatus = new NodeStatusHelper();
		// First, wait for ssh and netconf to come up.
		rbs = Rm.getG2RbsList().get(0);
		cli = rbs.getLinuxShell();
		assertTrue("Node did not reach a working state before test start",
				nodeStatus.waitForNodeReady());

		//cliCommands = new CliCommands();
		abisHelper = new AbisHelper();
		momHelper = new MomHelper();
		abisPrePost = new AbisPrePost();
		
		/**
		 * @todo Create TRX MO when postconditions for it can be verified (sync
		 *       is ok).
		 */

		/**
		 * @todo Verify Trx MO pre and post cond when we have sync working
		 */

		// Initiate the Test Manager.
		// MsSIM IP address (STP // wrbs651 //Username // password
		
/*		 tstm = TstmFactory.getTstmBuilder(HOST, PORT)
	                .configurationFileName(CONFOGURATION_FILE_NAME)
	                .targetLocationForConfigurationFiles(TARGET_LOCATION_FOR_CONFIGURATION_FILES)
	                .msDatabaseFileName(MS_DATABASE_FILE_NAME)
	                .filesSourceLocation(FILES_SOURCE_LOCATION)
	                .tstmFtpPassword(MSSIM_FTP_PASSWORD)
	                .tstmFtpUser(MSSIM_FTP_LOGIN)
	                .commandTimeout(TIMEOUT)
	                .build();*/
		  
	
		tstm.connect();
		try {
			tstm.connect();
			// if we make it to this line, success!
		} catch (Exception e) {
			fail("tstm not connected");
		}
		setTestStepEnd();
	}

	/**
	 * Postcond.
	 */
	@Teardown
	public void teardown() {
		setTestStepBegin("Teardown");
		// Stop and disconnect the Test Manager.
/*        STOP_CMD stopCmd = TstmFactory.getSTOP_CMDBuilder().build();
        response = tstm.send(stopCmd);
        Assert.assertTrue(response.isSuccess());*/

        tstm.disconnect();
        
		nodeStatus.isNodeRunning();
		setTestStepEnd();
	}

	/**
	 * @name firstCallMsSim
	 * @description Verifies part of first call with AO TX, RX and TS
	 * @param testId
	 *            - unique identifier
	 * @param description
	 * @throws InterruptedException
	 */
	@Test(timeOut = 360000)
	// six minutes should be enough
	@Parameters({ "testId", "description" })
	public void firstCallMsSimAndPayloadTesting(String testId, String description)
			throws InterruptedException, JSONException {

		abisPrePost.preCondAllMoStateStarted();
		assertTrue("enableTf failed", abisHelper.enableTf());
		assertTrue("enableTx failed", abisHelper.enableTx());
		assertTrue("enableRx failed", abisHelper.enableRx());
		assertTrue("enableTsBcch failed", abisHelper.enableTsBcch());
		assertTrue("enableTsSdcch failed", abisHelper.enableTsSdcch());
		assertTrue("enableTsTch failed", abisHelper.enableTsTch());
		abisHelper.sysInfo();
		sleepSeconds(10); // allows for some time for MSSIM to
							// attach/synchronize
		checkStatus(); // make sure nothing brakes while UE syncs

		List<Integer> imsiList = ImsiHelper.getSIMCardSubStruct().getIMSI();
		String imsi = Arrays.toString(imsiList.toArray());

		// Initiate MS in Test Manager.
		TstmMobile ms = new TstmMobile("0", // MsSim ID
				imsi, // MsSim IMSI number
				"msSimIsdn"); // MsSim ISDN number

		// Setup a call.
		callSetupMt();
		sleepSeconds(3);

		// Enable statistics and wait some time before reading the counters.
		sleepSeconds(60);
		enableAndGetPrbsStatistics(ms);

		// Disconnect the call.
		disconnectCallMt();
		
		// Stop and Disconnect Test manager
		teardown();
		
		
	}

	private void checkStatus() throws InterruptedException, JSONException {
		OM_G31R01.StatusResponse statusRsp;

		setTestStepBegin("Send Status Request for RX");
		statusRsp = abisHelper.statusRequest(this.moClassRx);
		setTestInfo("StatusResponse: " + statusRsp.toString());
		assertEquals("RX-state is not enabled",
				OM_G31R01.Enums.MOState.ENABLED, statusRsp.getMOState());
		setTestStepEnd();

		setTestStepBegin("Send Status Request for TX");
		statusRsp = abisHelper.statusRequest(this.moClassTx);
		setTestInfo("StatusResponse: " + statusRsp.toString());
		assertEquals("TX-state is not enabled",
				OM_G31R01.Enums.MOState.ENABLED, statusRsp.getMOState());
		setTestStepEnd();

		setTestStepBegin("Send Status Request for TS0");
		statusRsp = abisHelper.statusRequest(this.moClassTs, 0, 0);
		setTestInfo("StatusResponse: " + statusRsp.toString());
		assertEquals("TS0-state is not enabled",
				OM_G31R01.Enums.MOState.ENABLED, statusRsp.getMOState());
		setTestStepEnd();

		setTestStepBegin("Send Status Request for TS1");
		statusRsp = abisHelper.statusRequest(this.moClassTs, 1, 0);
		setTestInfo("StatusResponse: " + statusRsp.toString());
		assertEquals("TS1-state is not enabled",
				OM_G31R01.Enums.MOState.ENABLED, statusRsp.getMOState());
		setTestStepEnd();

		setTestStepBegin("Send Status Request for TS2");
		statusRsp = abisHelper.statusRequest(this.moClassTs, 2, 0);
		setTestInfo("StatusResponse: " + statusRsp.toString());
		assertEquals("TS2-state is not enabled",
				OM_G31R01.Enums.MOState.ENABLED, statusRsp.getMOState());
		setTestStepEnd();

		setTestStepBegin("Check MO state for TS0");
		assertTrue("abisTsMoState for tsInstance 0 is not ENABLED",
				momHelper.waitForAbisTsMoState(0, "ENABLED", 5));
		setTestStepEnd();
		setTestStepBegin("Check MO state for TS1");
		assertTrue("abisTsMoState for tsInstance 1 is not ENABLED",
				momHelper.waitForAbisTsMoState(1, "ENABLED", 5));
		setTestStepEnd();
		setTestStepBegin("Check MO state for TS2");
		assertTrue("abisTsMoState for tsInstance 2 is not ENABLED",
				momHelper.waitForAbisTsMoState(2, "ENABLED", 5));
		setTestStepEnd();

		setTestStepBegin("Print out stats for later faultfinding");
		cli.connect();
		setTestInfo("rhdc icmstatus:\n" + cli.send("rhdc icmstatus"));
		setTestInfo("rhdc icmtemp:\n" + cli.send("rhdc icmtemp"));
		setTestInfo("rhdc icmxio_status:\n" + cli.send("rhdc icmxio_status"));
		setTestInfo("rhdc icmiqcx_stats:\n" + cli.send("rhdc icmiqcx_stats"));
		setTestInfo("rhdc icmiqx_status:\n" + cli.send("rhdc icmiqx_status"));
		setTestInfo("rhdc icmiqx_config:\n" + cli.send("rhdc icmiqx_config"));
		cli.disconnect();
		setTestStepEnd();
	}

	private void callSetupMt() throws InterruptedException {
		setTestStepBegin("Setup Call, MT");
		TrafficCommand_Result res = abisHelper.sendcallSetupMt();
		if (res.getStatus()
				.equals(com.ericsson.abisco.clientlib.servers.PARAMDISP.Enums.Status.Cmd_OK))
			setTestInfo("Call was successfully set up!");
		else {
			setTestInfo(res.toString());
			fail("Failed connecting call.");
		}
		setTestStepEnd();
	}

	private void disconnectCallMt() throws InterruptedException {
		setTestStepBegin("Disconnect Call, MT");
		TrafficCommand_Result res = abisHelper.disconnectCallMt();
		if (res.getStatus()
				.equals(com.ericsson.abisco.clientlib.servers.PARAMDISP.Enums.Status.Cmd_OK))
			setTestInfo("Call was successfully disconnected!");
		else {
			setTestInfo(res.toString());
			fail("Failed disconnecting call.");
		}
		setTestStepEnd();
	}

	private void enableAndGetPrbsStatistics(TstmMobile ms) {
		
		// Set counter names in Test Manager.
/*		List<TstmStatisticalCounters> counters = new ArrayList<TstmStatisticalCounters>();
		counters.add(TstmStatisticalCounters.PRBS_TX_FILLED);
		counters.add(TstmStatisticalCounters.PRBS_RX_HDR_CORR);
		
		SET_COUNTER_NAMES_MS_INFO_CMD setCountersCmd = TstmFactory
				.getMsInformationCommand()
				.getSET_COUNTER_NAMES_MS_INFO_CMDBuilder(0)
				.counterNames(counters).build();
		response = tstm.send(setCountersCmd);
		Assert.assertTrue(response.isSuccess());
		
		GET_COUNTERS_MS_INFO_CMD getCountersMsInfoCmd = TstmFactory.getMsInformationCommand()
                .getGET_COUNTERS_MS_INFO_CMDBuilder(0)
                .build();
        response = tstm.send(getCountersMsInfoCmd);
        Assert.assertTrue(response.isSuccess());
        
		int MAX_FAILURE_PERCENT = 2; // The requirement is less than 2% failed
        
        double sentBlocks = getTstmStatistic(response.stringRepresentation()).data().getPrbsTxFilled();
        double receivedBlocks = getTstmStatistic(response.stringRepresentation()).data().getPrbsRxHdrCorr();
    	
        // Check the percent of failed blocks.
		double failedPercent = 100 - (receivedBlocks / sentBlocks) * 100;
		System.out.println("Failure percent: " + failedPercent);

		assertTrue("Measured errors has exceeded the acceptance level",
				failedPercent > MAX_FAILURE_PERCENT);*/
	}

	private TstmStatistics getTstmStatistic(String answer) {
	    final String[] values = answer.split("COUNTER_VALUES=")[1].split("}")[0].split(" ");
	    final Map<TstmStatisticalCounters, Double> valueMap = new HashMap<TstmStatisticalCounters, Double>();

	    for (final TstmStatisticalCounters stat : TstmStatisticalCounters.values()) {
	        valueMap.put(stat, Double.parseDouble(values[stat.ordinal()]));
	    }

	    final SpeechStatistics speechStatistics = new SpeechStatistics(valueMap);
	    final DataStatistics dataStatistics = new DataStatistics(valueMap);

	    return new TstmStatistics(dataStatistics, speechStatistics);
	}

}