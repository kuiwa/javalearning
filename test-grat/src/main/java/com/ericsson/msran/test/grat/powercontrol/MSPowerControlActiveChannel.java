package com.ericsson.msran.test.grat.powercontrol;

import org.json.JSONException;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;

import com.ericsson.msran.jcat.TestBase;

import se.ericsson.jcat.fw.annotations.Setup;
import se.ericsson.jcat.fw.annotations.Teardown;

import com.ericsson.abisco.clientlib.MessageQueue;
import com.ericsson.abisco.clientlib.servers.BG;
import com.ericsson.abisco.clientlib.servers.BG.Enums;
import com.ericsson.abisco.clientlib.servers.PARAMDISP;
import com.ericsson.abisco.clientlib.servers.BG.MeasurementResult;
import com.ericsson.abisco.clientlib.servers.PARAMDISP.MSPowerControl;
import com.ericsson.abisco.clientlib.servers.PARAMDISP.ReleaseAllCalls_Result;
import com.ericsson.abisco.clientlib.servers.TRAFFRN;
import com.ericsson.abisco.clientlib.servers.BG.SetMeasurementReporting;
import com.ericsson.abisco.clientlib.servers.PARAMDISP.TrafficCommand_Result;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.SACCHFilling5;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.SACCHFilling6;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.UNACKNOWLEDGED_MESSAGE_SENTException;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.Enums.SystemInfoType;
import com.ericsson.commonlibrary.remotecli.Cli;
import com.ericsson.commonlibrary.resourcemanager.Rm;
import com.ericsson.commonlibrary.resourcemanager.g2.G2Rbs;
import com.ericsson.commonlibrary.restorestack.RestoreCommandStack;
import com.ericsson.msran.g2.annotations.TestInfo;
import com.ericsson.msran.helpers.Helpers;
import com.ericsson.msran.test.grat.testhelpers.AbisHelper;
import com.ericsson.msran.test.grat.testhelpers.AbiscoConnection;
import com.ericsson.msran.test.grat.testhelpers.ImsiHelper;
import com.ericsson.msran.test.grat.testhelpers.MomHelper;
import com.ericsson.msran.test.grat.testhelpers.NodeStatusHelper;
import com.ericsson.msran.test.grat.testhelpers.prepost.AbisPrePost;
import com.ericsson.msran.test.grat.testhelpers.prepost.PidPrePost;
import com.ericsson.msran.test.grat.testhelpers.restorecommands.ReleaseAllCallsCommand;

public class MSPowerControlActiveChannel extends TestBase {

	private AbisPrePost abisPrePost;
	private PidPrePost pidPrePost;
	private AbisHelper abisHelper;
	private MomHelper momHelper;
	private TRAFFRN rslServer;
	private G2Rbs rbs;
	private Cli cli;

	private NodeStatusHelper nodeStatus;

	private PARAMDISP paramdisp;
	private BG bgServer;
	private SetMeasurementReporting setMeasurementReporting;
	private RestoreCommandStack restoreStack;
	private ReleaseAllCallsCommand releaseAllCallsRestoreStackCmd;

	/**
	 * Description of test case for test reporting
	 */
	@TestInfo(tcId = "MSPowerControlActiveChannel", slogan = "MSPowerControlActiveChannel", requirementDocument = "", requirementRevision = "", requirementLinkTested = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff87c1d941?docno=1/00651-FCP1301402Uen&format=excel8book", requirementLinkLatest = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff86a2af3d?docno=1/00651-FCP1301402Uen&action=current&format=excel8book", requirementIds = {
			"10565-0334/21767[PA1][PREL]", "10565-0334/19034[A][APPR]",
			"10565-0334/19417[A][APPR]" }, verificationStatement = "Handling of MS power for an active channel", testDescription = " Handling of MS power for an active channel", traceGuidelines = "N/A")
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
		// First, wait for ssh and netconf to come up
		rbs = Rm.getG2RbsList().get(0);
		cli = rbs.getLinuxShell();
		assertTrue("Node did not reach a working state before test start",
				nodeStatus.waitForNodeReady());

		abisHelper = new AbisHelper();
		momHelper = new MomHelper();
		abisPrePost = new AbisPrePost();
		pidPrePost = new PidPrePost();
		paramdisp = abisHelper.getPARAMDISP();
		new AbiscoConnection();
		rslServer = abisHelper.getRslServer();
		bgServer = abisHelper.getBG();
		restoreStack = Helpers.restore().restoreStackHelper().getRestoreStack();

		setMeasurementReporting = bgServer.createSetMeasurementReporting();

		pidPrePost.preCond();
		setTestStepEnd();
	}

	/**
	 * Postcond.
	 * 
	 * @throws InterruptedException
	 */
	@Teardown
	public void teardown() throws InterruptedException {
		setTestStepBegin("Teardown");
		nodeStatus.isNodeRunning();
		pidPrePost.postCond();
		setTestStepEnd();
	}

	/**
	 * @name Active Channel Testing
	 * 
	 * @description Verifies MS Power Control at Active Channel.
	 * 
	 * @param testId
	 *            - unique identifier
	 * @param description
	 * 
	 * @throws InterruptedException
	 */
	@Test(timeOut = 1800000)
	// 30 minutes should be enough
	@Parameters({ "testId", "description" })
	public void ActiveChannelTesting(String testId, String description)
			throws InterruptedException, JSONException {
		abisPrePost.preCondAllMoStateStarted();
		assertTrue("enableTf failed", abisHelper.enableTf());
		assertTrue("enableTx failed", abisHelper.enableTx());
		assertTrue("enableRx failed", abisHelper.enableRx());
		assertTrue("enableTsBcch failed", abisHelper.enableTsBcch());
		assertTrue("enableTsSdcch failed", abisHelper.enableTsSdcch());
		assertTrue("enableTsTch failed", abisHelper.enableTsTch());
		abisHelper.sysInfo();

		sacchFilling5();
		sacchFilling6();

		releaseHangingCalls();

		try {
			abisHelper.enableAbisLogging(0); // necessary to be able to pull
												// stuff from
												// getMeasurementResultQueue
		} catch (Exception e) {
			// Do nothing
		}

		sleepSeconds(abisHelper.SECONDS_TO_WAIT_FOR_SYNC); // allows for
															// some time
															// (20s * 10)
															// for MSSIM to
															// attach/synchronize

		int timeslot = callSetupMt();
		int set_MSPower_Value = 3;

		boolean powerStatus = powerCheck(timeslot, set_MSPower_Value);

		if (!powerStatus) {
			setTestInfo("ABISCO: Expected change of MS Power not detected");
			fail("Failed to change MS Power");
		}

		disconnectCallMt();
	}

	private boolean powerCheck(int timeslot, int expectedDiff)
			throws InterruptedException {
		setTestStepBegin("Power Check, verify MSPower");
		// MS Power Control
		int expectedValue;

		sleepSeconds(2);

		// Get average Power from checkMeasurements then change power and fetch
		// new average Power.
		// Measure MS power before
		int resp1 = checkMeasurements(3, timeslot);

		setTestInfo("powerCheck, first measurement=" + resp1);
		expectedValue = resp1 + expectedDiff;
		msPowerControl(expectedValue);

		sleepSeconds(2);
		// Measure MS power after
		int resp2 = checkMeasurements(3, timeslot);

		// Verify change
		setTestInfo("ABISCO: Expected value of MSPower " + expectedValue
				+ ", detected value " + resp2);

		// Check change, accept +/- 1
		if (resp2 > (expectedValue - 2) && resp2 < (expectedValue + 2)) {
			return (true);
		}
		return (false);
	}

	private int checkMeasurements(int number, int timeslot)
			throws InterruptedException {
		// Check MSPower

		BG.Enums.ChannelType channelType = BG.Enums.ChannelType.Bm;

		int counterHit = 0;

		int accumulated = 0;

		// Turn on sending of Measurement Results from TSS
		onOffMeasurements(BG.Enums.BGMeasurementReporting.On, timeslot);

		MessageQueue<MeasurementResult> measurementResultQueue = bgServer
				.getMeasurementResultQueue();

		sleepSeconds(2);
		// Read the requested number of Measurement Results
		for (int counterRead = 0; counterHit < number; counterRead++) {
			MeasurementResult msg = measurementResultQueue.poll(2,
					TimeUnit.SECONDS); // MeasurementResult extends
										// AbiscoResponse
			setTestInfo("checkMeasurements, counterRead=" + counterRead
					+ " msg: " + msg);
			if (msg == null) {
				fail("Measurement Result queue shouldn't be empty");
			}
			if (msg.getRouting().getTG() == 0
					&& msg.getRouting().getTRXC() == 0
					&& msg.getChannelNoStruct().getTimeSlotNo() == timeslot
					&& msg.getChannelNoStruct().getChannelType() == channelType) {
				try {
					int tempValue = msg.getL1InfoStruct().getMSPowerLevel();
					counterHit++;
					accumulated += tempValue;
					setTestInfo("ABISCO: MeasurementResult "
							+ msg.getMeasResultNoValue() + " MSPower = "
							+ tempValue);
				} catch (NullPointerException e) {
					// Do nothing
					setTestInfo("This measurement report does not contains L1InfoStruct/MSPowerLevel: "
							+ msg);
				}
			}

			if (counterRead > (number + 20)) {
				fail("ABISCO: Matching MeasurementResults not received");
			}
		}

		onOffMeasurements(BG.Enums.BGMeasurementReporting.Off, timeslot);

		return (accumulated / counterHit);
	}

	private void msPowerControl(int newValue) {
		setTestInfo("ABISCO: MSPowerControl Value=" + newValue);
		PARAMDISP.SIMCardSubStruct simCardSubStruct = ImsiHelper
				.getSIMCardSubStruct();
		MSPowerControl msPowerControl = paramdisp.createMSPowerControl();
		msPowerControl.setMSPowerValue(newValue);
		msPowerControl.setLMSID(0);
		msPowerControl.setSIMCardSubStruct(simCardSubStruct);

		TrafficCommand_Result res;
		try {
			res = msPowerControl.send();
			if (res.getStatus()
					.equals(com.ericsson.abisco.clientlib.servers.PARAMDISP.Enums.Status.Cmd_OK))
				setTestInfo("MS Power successfully changed.");
			else {
				setTestInfo(res.toString());
				fail("Failed to change MS Power.");
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
			fail("Failed to send MSPowerControl");
		}

	}

	private void onOffMeasurements(BG.Enums.BGMeasurementReporting turnOnOff,
			int timeslot) throws InterruptedException {
		setTestInfo("onOffMeasurements: " + turnOnOff);
		setMeasurementReporting.setBGMeasurementReporting(turnOnOff);

		Enums.BGMeasurementReporting bgMeasRepToSet = setMeasurementReporting
				.getBGMeasurementReporting();

		try {
			BG.BGReportingStatus res = setMeasurementReporting.send();
			setTestInfo("onOffMeasurements: bgMeasRepToSet="
					+ bgMeasRepToSet.toString() + ", res="
					+ res.getBGMeasurementReporting());
			if (!res.getBGMeasurementReporting().equals(bgMeasRepToSet)) {
				fail("Failed to set BGMeasurementReporting to "
						+ bgMeasRepToSet.toString());
			}
		} catch (InterruptedException e) {
			fail("Failed to turn measurement " + turnOnOff);
		}
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
		restoreStack.remove(releaseAllCallsRestoreStackCmd);
		setTestStepEnd();
	}

	private int callSetupMt() throws InterruptedException {
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
		int timeslot = res.getCallLocSubStruct().getCallLocationTCH_TS();
		setTestInfo("callSetupMT, timeslot=" + timeslot);
		return timeslot;
	}

	private int callSetupMt(int msPower) throws InterruptedException {
		setTestStepBegin("Setup Call, MT");

		releaseAllCallsRestoreStackCmd = new ReleaseAllCallsCommand(
				abisHelper.getPARAMDISP());
		restoreStack.add(releaseAllCallsRestoreStackCmd);

		String msPowerComment = " with MSPower = " + msPower;

		TrafficCommand_Result res = abisHelper.sendcallSetupMt(msPower);
		if (res.getStatus()
				.equals(com.ericsson.abisco.clientlib.servers.PARAMDISP.Enums.Status.Cmd_OK))
			setTestInfo("Call" + msPowerComment + " was successfully set up!");
		else {
			setTestInfo(res.toString());
			restoreStack.remove(releaseAllCallsRestoreStackCmd);
			fail("Failed connecting call" + msPowerComment + ".");
		}

		setTestStepEnd();
		int timeslot = res.getCallLocSubStruct().getCallLocationTCH_TS();
		setTestInfo("callSetupMT, timeslot=" + timeslot);
		return timeslot;
	}

	private void releaseHangingCalls() {
		try {
			ReleaseAllCalls_Result res = paramdisp.createReleaseAllCalls()
					.send();
			setTestInfo("Released all calls: " + res.getStatus());
		} catch (Exception e) {

		}
	}

	// Helper for sysinfo 5/6
	/**
	 * @name sacchFilling
	 * 
	 * @description Verifies sacchFilling
	 * 
	 */
	public void sacchFilling5() {
		SACCHFilling5 msg = rslServer.createSACCHFilling5();
		try {
			msg.setSystemInfoType(SystemInfoType.SI5);
			setTestInfo("sacchFilling5: " + msg.toString());
			msg.send();
		} catch (UNACKNOWLEDGED_MESSAGE_SENTException e) {
			// Normal response, do nothing
		} catch (Exception e) {
			fail("Failed to set SACCH fillings SI6");
			e.printStackTrace();
		}
	}

	public void sacchFilling6() {
		SACCHFilling6 msg = rslServer.createSACCHFilling6();
		try {
			msg.setSystemInfoType(SystemInfoType.SI6);
			setTestInfo("sacchFilling6: " + msg.toString());
			msg.send();
		} catch (UNACKNOWLEDGED_MESSAGE_SENTException e) {
			// Normal response, do nothing
		} catch (Exception e) {
			fail("Failed to set SACCH fillings SI6");
			e.printStackTrace();
		}
	}
}
