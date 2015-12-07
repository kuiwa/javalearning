package com.ericsson.msran.test.grat.configsv;

import java.awt.Component;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.json.JSONException;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import se.ericsson.jcat.fw.annotations.Setup;
import se.ericsson.jcat.fw.annotations.Teardown;

import com.ericsson.abisco.clientlib.servers.LTS.Report;
import com.ericsson.abisco.clientlib.servers.OM_G31R01;
import com.ericsson.abisco.clientlib.servers.OM_G31R01.Enums;
import com.ericsson.abisco.clientlib.servers.OM_G31R01.FSOffset;
import com.ericsson.commonlibrary.moshell.Moshell;
import com.ericsson.commonlibrary.resourcemanager.Rm;
import com.ericsson.msran.g2.annotations.TestInfo;
import com.ericsson.msran.helpers.Helpers;
import com.ericsson.msran.jcat.TestBase;
import com.ericsson.msran.test.grat.testhelpers.AbisHelper;
import com.ericsson.msran.test.grat.testhelpers.AbiscoConnection;
import com.ericsson.msran.test.grat.testhelpers.MomHelper;
import com.ericsson.msran.test.grat.testhelpers.NodeStatusHelper;
import com.ericsson.msran.test.grat.testhelpers.prepost.AbisPrePost;
import com.ericsson.msran.test.grat.testhelpers.prepost.PidPrePost;

/**
 * 
 * @name RadioResActivation
 * 
 * @author GRAT 2015
 * 
 * @created 
 * 
 * @description This test class connect Abisco Link and set up multiple Trx and starts all TS.
 *              Uses the LTS to start a stability load test.
 * 
 * @revision efillar 2015-09-30   Used RadioResourceSetup.java as base, added LTS functionality in order
 *                                to show have an automatic SV test could look like. Prototype, needs major
 *                                refactoring.
 */

public class MaximumLoadTestSV extends TestBase {
	
	private final OM_G31R01.Enums.MOClass moClassTx = OM_G31R01.Enums.MOClass.TX;    
	private final OM_G31R01.Enums.MOClass moClassRx = OM_G31R01.Enums.MOClass.RX;    
	private final OM_G31R01.Enums.MOClass moClassTs = OM_G31R01.Enums.MOClass.TS;
	
	private PidPrePost pidPrePost;
	private MomHelper momHelper;
	private AbiscoConnection abisco;
	private NodeStatusHelper nodeStatus;
	private AbisHelper abisHelper;
	private String abisIp_ldn;
	private String sector_ldn;
    private static String BTS_FUNCTION_LDN = "ManagedElement=1,BtsFunction=1";
    private static String SECTOR_EQUIP_FUNC_LDN = "ManagedElement=1,NodeSupport=1,SectorEquipmentFunction=";
    private String sectorEquipmentFunctionLdnToUse;
	private AbisPrePost abisPrePost;
	private Component frame;
	private List<Integer> trxcList = new ArrayList<Integer>(0);
	private List<String> trxcLdnList = new ArrayList<String>(0);
	private List<Integer>  bcchTrxList= new ArrayList<Integer>(0);
	private boolean bcchFlag = false;
	private String connectionName = "host_";
	private OM_G31R01.StatusResponse statusRsp;
	private Moshell moshell;
	private static String RESET_MEASUREMENT_FILE = "/proj/gratci/system_verification/scripts/reset_measurements.mos";
	private static String MEASUREMENT_FILE = "/proj/gratci/system_verification/scripts/grat_measurements.mos";
	private long loadStartAt;
	private long loadEndAt;
	
	/**
	 * Description of test case for test reporting
	 */
	@TestInfo(tcId = "LTE4711", slogan = "Maximum load test case", 
			  requirementDocument = "1/xyz", requirementRevision = "PC5", 
			  requirementLinkTested = "..", 
			  requirementLinkLatest = "", 
			  requirementIds = { "PM 17" }, 
			  verificationStatement = "Maximum load test case with 12 TRX:es", 
			  testDescription = "Runs EFR/FR traffic on all TRX/TS.", 
			  traceGuidelines = "N/A")
	/**
	 * Precond.
	 */
	@Setup
	public void setup() {
		setTestStepBegin("setup");
		nodeStatus = new NodeStatusHelper();
		assertTrue("Node did not reach a working state before test start",
				nodeStatus.waitForNodeReady());
		abisHelper = new AbisHelper();
		pidPrePost = new PidPrePost();
		momHelper = new MomHelper();
		abisco = new AbiscoConnection();
		pidPrePost.preCond();
		abisPrePost = new AbisPrePost();
		moshell = Rm.getG2RbsList().get(0).getMoshell();
		setTestStepEnd();
	}

	/**
	 * Postcond.
	 */
	@Teardown
	public void teardown() {
		setTestStepBegin("teardown");
		nodeStatus.isNodeRunning();
		pidPrePost.postCond();
		setTestStepEnd();
	}

	/**
	 * @name configRadioTest
	 * 
	 * @description This test class connect Abisco Link and set up multiple Trx
	 * 
	 * @param testId
	 *            - unique identifier
	 * @param description
	 * @param noTrxsInGsmSector
	 *            - Total Trx no
	 * @param cellNumber 
	 *            - example 2 : means this script will setup two cells, two TG
	 * @param bcchNoList 
	 *            - example: 107,109 means bcchNo for each cell
	 * @param trxDistributionList 
	 *            - example: 0,1;0,3  means first cell will setup two TRX: BtsFunction=1,GsmSector=1,Trx=0 and BtsFunction=1,GsmSector=1,Trx=1
	 *                                      second cell will setup four TRX, BtsFunction=1,GsmSector=2,Trx=0; BtsFunction=1,GsmSector=2,Trx=1; BtsFunction=1,GsmSector=2,Trx=2; BtsFunction=1,GsmSector=2,Trx=3
	 *                                      BtsFunction=1,GsmSector=1,Trx=0 and BtsFunction=1,GsmSector=2,Trx=0 need enable BCCH
	 *            - example: 0,0  means fist cell only setup one TRX
	 * @param trxsectorDistributionList 
	 *            - example: 0,3,1:4,7,2:8,11,3  means create 1 cell, Trx0-Trx11: sectorEquipmentFunctionRef for Trx0-Trx3 is SectorEquipmentFunction=1, for Trx4-Trx7 is SectorEquipmentFunction=2, for Trx8-Trx11 is SectorEquipmentFunction=3
	 *            - example: 0,3,1;0,3,2;0,3,3   means create 3 cell, for first cell, create Trx0-Trx3: sectorEquipmentFunctionRef is SectorEquipmentFunction=1
	 *                                                                for the second cell, create Trx0-Trx3: sectorEquipmentFunctionRef is SectorEquipmentFunction=2
	 *                                                                for the third cell, create Trx0-Trx3: sectorEquipmentFunctionRef is SectorEquipmentFunction=3
	 * @param isRunManually - if true the test case will display pop ups to continue test case. Default false.
	 * @param callSetupRate - The number of CS calls per second * 1000, e.g. CPR=5 -> callSetupRate=5000. Default 5000.
	 * @param smsSetupRate - The number of SMS sent per second * 1000, e.g. SPR=1.1 -> smsSetupRate=1100. Default 1100.
	 * @param callDuration - The call duration in seconds of each call. Default 5 s.
	 * @param percentageFR - Percentage of total number of calls to be FR. Default 10%.
	 * @param percentageEFR - Percentage of total number of calls to be EFR. Default 90%.
	 * @param loadTestDuration - if isRunManually is false, this is the time in seconds the load test will be executed. Default 30 minutes.
	 * @param resetMeasureFile - mos script for reset measurement
	 * @param measurementFile - mos script for measure CPU load
	 * @param maxPagingAttempts 
	 * @param ccchLoadPagingRate 
	 * @param ccchLoadLRSAssRate 
	 * @param ccchLoadLRSAssRejRate 
	 * @param ccchLoadPSPagingRatio 
	 * @param hoppingIndicator
	 * @throws InterruptedException
	 * @throws JSONException
	 */
	@Test(timeOut = 30000000)  // approx 8.x h
	@Parameters({ "testId", "description", "noTrxsInGsmSector", "cellNumber", "bcchNoList", "trxDistributionList", "trxsectorDistributionList", "isRunManually",
		           "callSetupRate", "smsSetupRate", "callDuration", "percentageFR", "percentageEFR", "loadTestDuration", "resetMeasureFile", "measurementFile", "maxPagingAttempts", "ccchLoadPagingRate", "ccchLoadLRSAssRate", "ccchLoadLRSAssRejRate", "ccchLoadPSPagingRatio", "hoppingIndicator"})
	public void maximumLoadTest(String testId, String description, @Optional("1") int noTrxsInGsmSector, @Optional("1") int cellNumber,
	        String bcchNoList, @Optional("0,0") String trxDistributionList, @Optional("0,0,1") String trxsectorDistributionList, 
	        @Optional("false") boolean isRunManually, @Optional("5000") String callSetupRate, @Optional("1100") String smsSetupRate, 
	        @Optional("5") String callDuration, @Optional("10") String percentageFR, @Optional("90") String percentageEFR, 
	        @Optional("1800") int loadTestDuration, @Optional("") String resetMeasureFile, @Optional("") String measurementFile,@Optional("3")int maxPagingAttempts, @Optional("26400")String ccchLoadPagingRate, @Optional("3000")String ccchLoadLRSAssRate, @Optional("3000")String ccchLoadLRSAssRejRate, @Optional("50")String ccchLoadPSPagingRatio, @Optional("Off")String hoppingIndicator)
			throws InterruptedException, JSONException {
    
		setTestCase(testId, description);
		
		setTestStepBegin("Setup link and create MOs");
		String[] bcchNumberlist = bcchNoList.split(",");
		String[] trxdistribution = trxDistributionList.split(";");
		String[] trxsectordistribution = trxsectorDistributionList.split(";"); 
		String [][] trxdistributionEverySector= new String [trxsectordistribution.length][];
		for (int i=0; i< trxdistribution.length; i++) {
		      bcchTrxList.add(Integer.parseInt(trxdistribution[i].split(",")[0]));
		}
		for(int i=0; i < trxsectordistribution.length; i++) {
            trxdistributionEverySector[i]= trxsectordistribution[i].split(":"); //[0] 0,3,1  4,7,2//[1]8,11,3
		}
		
		setTestStepBegin("Disconnect and delete TG");
		for(int tgId = 0; tgId < cellNumber; tgId++) {
		    abisco.disconnectAndDeleteTG(tgId);    
		}
		setTestStepEnd();
		
		setTestStepBegin("Start TSS");
		abisco.startTss();
		setTestStepEnd();
		
		setTestStepBegin("Create TG");
        for(int tgId = 0; tgId < cellNumber; tgId++) {
            abisco.createTgPreDefBundling(tgId, connectionName+tgId, (Integer.parseInt(trxdistribution[tgId].split(",")[1])-Integer.parseInt(trxdistribution[tgId].split(",")[0])+1)); 
        }
        setTestStepEnd();
        
        setTestStepBegin("Define cell");
        for (int tgId=0; tgId< cellNumber; tgId++) { 
            abisco.defineCell(tgId, trxdistribution[tgId] , Integer.parseInt(bcchNumberlist[tgId]), 0, hoppingIndicator);
        }
        setTestStepEnd();
        
        setTestStepBegin("Connect TG");
        for (int i=0; i< cellNumber; i++) {
            abisco.connectTG(i);
        }
        setTestStepEnd();
        
      	if (!momHelper.isBtsFunctionCreated()) {
      		momHelper.createBtsFunctionMo(MomHelper.BTS_FUNCTION_LDN);
        }

        setTestStepBegin("Create MO GsmSector/AbisIP/Trx");
	    for (int gsmsector=0; gsmsector < cellNumber; gsmsector++) {
	        
	            sector_ldn = String.format("%s,GsmSector=%s", BTS_FUNCTION_LDN, Integer.toString(gsmsector+1));    
	            abisIp_ldn = String.format("%s,GsmSector=%s,AbisIp=1", BTS_FUNCTION_LDN, Integer.toString(gsmsector+1));  
	            
	            String abisIpLdn = momHelper.createSectorAndCorrespondingAbisIpMo(
	                    connectionName+gsmsector, abisco.getBscIpAddress(),abisIp_ldn, gsmsector+1);
	            for(int i =0; i< trxdistributionEverySector[gsmsector].length; i++) {
	                for (int trx = Integer.parseInt(trxdistributionEverySector[gsmsector][i].split(",")[0]); trx <= Integer.parseInt(trxdistributionEverySector[gsmsector][i].split(",")[1]); trx++) {
	                    sectorEquipmentFunctionLdnToUse = SECTOR_EQUIP_FUNC_LDN + trxdistributionEverySector[gsmsector][i].split(",")[2];
	                    trxcLdnList.add(momHelper.createTrxMo(sector_ldn , Integer.toString(trx), sectorEquipmentFunctionLdnToUse));
	                    momHelper.unlockMo(trxcLdnList.get(trx));
	                    trxcList.add(trx);  
	                }
	            }
	           momHelper.unlockMo(abisIpLdn);
	    }
	    for(int i=0; i<trxcLdnList.size();i++) {
	        momHelper.unlockMo(trxcLdnList.get(i));
	    }
		setTestStepEnd();
		
		setTestStepBegin("Establish SCF /AT/TFlinks");
		sleepSeconds(5);
		for (int tgId = 0; tgId < cellNumber; tgId++) {
		    try {
		        setTestInfo("SCF links");
		        abisco.establishLinks(tgId, false, Integer.parseInt(trxdistribution[tgId].split(",")[0]));
		        setTestInfo("AT links");
		        assertTrue("Could not activate AT for tgId=" + tgId,
	                    abisHelper.startSectorMosAndActivateAT(tgId));
		        setTestInfo("TF links");
		        OM_G31R01.TFConfigurationResult tfConfigResult = abisHelper.tfConfigRequest(tgId, 1, Enums.TFMode.Master, new FSOffset(new Integer(0xFF),new Long(0xFFFFFFFFL)));
		        assertTrue(
	                    "AccordanceIndication not according to Request",
	                    tfConfigResult.getAccordanceIndication()
	                            .getAccordanceIndication() == Enums.AccordanceIndication.AccordingToRequest);
	            assertTrue("Failed enableTf for tgId="+tgId, abisHelper.enableTf(tgId));
		    } catch (InterruptedException ie) {
	            fail("InteruptedException during establishLinks");
	        }
		}
		setTestStepEnd();
	    
	    setTestStepBegin("Establish TRX links");
	    sleepSeconds(5);
	    int[][]freqlist = new int [cellNumber][noTrxsInGsmSector];
	    int id=0;
	    for (int sectorId = 0; sectorId < cellNumber; sectorId++) {
            for(int trxId = Integer.parseInt(trxdistribution[sectorId].split(",")[0]) ; trxId <= Integer.parseInt(trxdistribution[sectorId].split(",")[1]); trxId ++) {
                int x = abisco.arfcnListMy.get(id++);
                freqlist[sectorId][trxId] = x;
            }
	    }
	    for (int sectorId = 0; sectorId < cellNumber; sectorId++) {
	        for(int trxId = Integer.parseInt(trxdistribution[sectorId].split(",")[0]) ; trxId <= Integer.parseInt(trxdistribution[sectorId].split(",")[1]); trxId ++) {
	            bcchFlag = false;
	            setTestStepBegin("Setup for trxId = " + trxId + " for sector=" + sectorId);
	            abisco.establishLinks(sectorId,true, trxId);
	            sleepSeconds(2);
	            abisPrePost.preCondTrxStateStartedMy(sectorId,trxId);
	            int freq = freqlist[sectorId][trxId];
	            assertTrue("enableTx failed", abisHelper.enableTx(sectorId,trxId,freq));
	            assertTrue("enableRx failed", abisHelper.enableRx(sectorId,trxId,freq));
	            for (int i = 0; i < bcchTrxList.size(); i++ ) {
	                if (trxId == bcchTrxList.get(i)) {
	                    bcchFlag = true;
	                }
	            }
	            if (bcchFlag == true) {
	                try {
	                    assertTrue("Enable of BCCH failed",
                                abisHelper.enableAnyTsMy(sectorId, trxId, trxId, 0,
                                        OM_G31R01.Enums.Combination.MainBCCH,freq));

	                } catch (InterruptedException | JSONException e) {
	                    e.printStackTrace();
	                }
	                System.out.println("Enable BCCHs in TRX-" + trxId);

	                try {
	                    assertTrue("Enable of TS failed SDCCH",
	                            abisHelper.enableAnyTsMy(sectorId,trxId,trxId, 1,
	                                    OM_G31R01.Enums.Combination.SDCCH,freq));
	                } catch (InterruptedException | JSONException e) {
	                    // TODO Auto-generated catch block
	                    e.printStackTrace();
	                }
	                System.out.println("Enable SDCCH in TRX-" + trxId);

	                int tsInstance = 2;
	                while (tsInstance < 8) {
	                    System.out.println("Enable TCHs on TS" + tsInstance
	                            + " in TRX-" + trxId);
	                    try {
	                        assertTrue("Enable of TS failed TCH",
	                                abisHelper.enableAnyTsMy(sectorId,trxId,trxId, tsInstance,
	                                        OM_G31R01.Enums.Combination.TCH,freq));
	                    } catch (InterruptedException e) {
	                        // TODO Auto-generated catch block
	                        e.printStackTrace();
	                    } catch (JSONException e) {
	                        // TODO Auto-generated catch block
	                        e.printStackTrace();
	                    }
	                    tsInstance++;
	                }
	            } else {
	                int tsInstance = 0;
	                //String tmpTrxLdn = momHelper.createTrxMo(sectorLdn, Integer.toString(trxId));
	                System.out.println("Enable TCHs on TS" + tsInstance
	                        + " in TRX-" + trxId);

	                while (tsInstance < 8) {
	                    try {
	                        assertTrue("Enable of TS failed TCH",
	                                abisHelper.enableAnyTsMy(sectorId, trxId, trxId, tsInstance,
	                                        OM_G31R01.Enums.Combination.TCH,freq));
	                    } catch (InterruptedException e) {
	                        // TODO Auto-generated catch block
	                        e.printStackTrace();
	                    } catch (JSONException e) {
	                        // TODO Auto-generated catch block
	                        e.printStackTrace();
	                    }
	                    tsInstance++;
	                }
	            }
	            setTestStepEnd();
	        }
	    }
		setTestStepEnd();
		
		// Send sysinfo 2, 3, and 4
		try {	
		    setTestStepBegin("Send and check sys Info");
		    for (int tgId =0; tgId < cellNumber; tgId ++) {
		        abisHelper.sendBCCHInfoWithCellId(tgId, Integer.parseInt(trxdistribution[tgId].split(",")[0]));
		    }
			setTestStepEnd();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		setTestStepBegin("checking mo status before call");
        checkStatus(cellNumber,trxdistribution);
        setTestStepEnd();
			
		// Start the LTS and check measure load
		if (isRunManually) {
			JOptionPane.showMessageDialog(frame, "Press ok to start LTS.");	
		}
		// TODO: efillar (2015-09-30) - Fix this, how long to wait for cell synch.
		// TODO: eyngjng (2015-10-16) - Will Add TM part instead of this once TM can be automatically controlled.
		sleepSeconds(90);
		
		setTestStepBegin("Create measurement load file path");
        String reset_MeasureFile;
        String measurement_File;
        String dateTime = Helpers.util().timeHelper().getTimeStamp();
        String currentUP = null;
        String cvcuResult = moshell.send("cvcu");
        String cvcuResultSplitByLine[] = cvcuResult.split("\n");
        for(int i=0 ; i<cvcuResultSplitByLine.length ; i++){
            if(cvcuResultSplitByLine[i].contains("Current SwVersion:")){
                currentUP = cvcuResultSplitByLine[i].split("_")[1].split("\\s+")[0];
            }
        }
        String latencyLogFileName = currentUP +"_" +dateTime;
        setTestInfo("latencyLogFileName is " + latencyLogFileName);
        try {
            Process p = Runtime.getRuntime().exec("mkdir -m 777 /proj/gratci/system_verification/testruns/"+ latencyLogFileName);
        } catch (IOException e1) {
            e1.printStackTrace();
            fail("*** Create directory failed ");
        }
        setTestStepEnd();
        
        if (!("").equals(resetMeasureFile))  {
             reset_MeasureFile = resetMeasureFile;
        }else {
             reset_MeasureFile = RESET_MEASUREMENT_FILE;
        }
         
        if (!("").equals(measurementFile))  {
            measurement_File = measurementFile;
       }else {
           measurement_File = MEASUREMENT_FILE;
       }
        
		try {	
		    setTestStepBegin("Reset, configure and start LTS.");
		    abisco.resetLTS();
		    moshell.send("run " + reset_MeasureFile);
	        sleepSeconds(3);
		    // @TODO: efillar (2015-09-30) - First parameters should not be hard coded.
		    // eyngjng(2015-10-13), updated.
		    for (int tgId = 0; tgId < cellNumber; tgId ++) {
		          abisco.configureLTS(tgId, tgId, 0, maxPagingAttempts, Integer.parseInt(callSetupRate.split(",")[tgId]), Integer.parseInt(smsSetupRate.split(",")[tgId]), 
		                  Integer.parseInt(callDuration.split(",")[tgId]), Integer.parseInt(percentageFR.split(",")[tgId]), Integer.parseInt(percentageEFR.split(",")[tgId]), 
		                  Integer.parseInt(ccchLoadPagingRate.split(",")[tgId]), Integer.parseInt(ccchLoadLRSAssRate.split(",")[tgId]), 
		                  Integer.parseInt(ccchLoadLRSAssRejRate.split(",")[tgId]), Integer.parseInt(ccchLoadPSPagingRatio.split(",")[tgId]), 1451+60*tgId, 60);
		    }
            abisco.startLTS();
		    loadStartAt = System.currentTimeMillis();
		    setTestStepEnd();
		} catch (InterruptedException e) {
			// Did not work to reset the LTS.
			e.printStackTrace();
		}
		
		//Part for check load
		setTestStepBegin("Collect measurement load data");
		moshell.send("l+ /proj/gratci/system_verification/testruns/" + latencyLogFileName +"/load_and_latency.log");
		sleepSeconds(3);
		moshell.send("run " + measurement_File, loadTestDuration);
		sleepSeconds(3);
		//
		if (isRunManually) {
			JOptionPane.showMessageDialog(frame, "Press ok to stop LTS.");
			// TODO: efillar (2015-09-30) - Fix this, should be a parameter to test case instead.
		} else {
		    loadEndAt = System.currentTimeMillis();
		    int measureScriptsRunningTime = (int) (loadEndAt - loadStartAt);
		    int loadtimeLeft = loadTestDuration * 1000 - measureScriptsRunningTime;
		    loadtimeLeft = loadtimeLeft < 0 ? 0 : loadtimeLeft;
	        setTestInfo("measureloadScriptStart is " + loadStartAt + " measureloadScriptEnd is " + loadEndAt + " measureScriptsRunningTime is " + measureScriptsRunningTime + " loadTimeLeft is " + loadtimeLeft);
		    sleepMilliseconds(loadtimeLeft);
		}
		
		moshell.send("l-");
		setTestStepEnd();
		
		// Report is used to get the load test statistics from the LTS after stopping the test.
		Report report = null;
		
		try { 
		    setTestStepBegin("Stop LTS and report.");
		    abisco.stopLTS();
		    report = abisco.getReportLTS(0); 

		    setTestStepEnd();
		} catch (InterruptedException e) {
			// Did not work to reset the LTS.
			e.printStackTrace();
		}
		
		if (report != null) {
			// Calculate some statistics
			int totalNumberOfSuccessfulCalls = (int)(report.getFRCallSuccTot() + report.getEFRCallSuccTot());
			int totalNumberOfFailedCalls = (int)(report.getFRCallFailsTot() + report.getEFRCallFailsTot());
			int totalNumberOfCalls = (int)(report.getFRCallsTot() + report.getEFRCallsTot());
			int timeInSeconds = (int)(report.getTotalTime() / 100) + 1; // At least on second 
			double csr = totalNumberOfSuccessfulCalls / (double)timeInSeconds;
			double cfr = totalNumberOfFailedCalls / (double)timeInSeconds;
			double ssr = report.getSMSSuccTot() / (double)timeInSeconds;
			double sfr = report.getSMSFailsTot() / (double)timeInSeconds;
		
			// Write summary info report
			setAdditionalResultInfo("<p><br><b>GRAT Stability load test summary:</b>");
			setAdditionalResultInfo("Test duration: " + timeInSeconds + " s");
			for(int tgId = 0; tgId < cellNumber; tgId ++) {
			    setAdditionalResultInfo("tgId = " + tgId +" :Call Setup Rate: " + Integer.parseInt(callSetupRate.split(",")[tgId]) / (double)1000 + " call/s");
			    setAdditionalResultInfo("tgId = " + tgId +" :SMS Setup Rate: " + Integer.parseInt(smsSetupRate.split(",")[tgId]) / (double)1000 + " sms/s<br>");
			}
			setAdditionalResultInfo("Call Success Rate (CSR): " + csr);
			setAdditionalResultInfo("Call Failure Rate (CFR): " + cfr);
			setAdditionalResultInfo("SMS Success Rate  (SSR): " + ssr);
			setAdditionalResultInfo("SMS Failure Rate  (SFR): " + sfr + "</p>");
			
			setAdditionalResultInfo("<p><b>GRAT Stability load test details:</b>");		
			setAdditionalResultInfo("########### FR&EFR calls ###########");
			setAdditionalResultInfo("Successful FR calls: " + (int)report.getFRCallSuccTot());
			setAdditionalResultInfo("Successful EFR calls: " + (int)report.getEFRCallSuccTot());
			setAdditionalResultInfo("Successful total calls: " + totalNumberOfSuccessfulCalls);
			setAdditionalResultInfo("Failed FR calls: " + (int)report.getFRCallFailsTot());
			setAdditionalResultInfo("Failed EFR calls: " + (int)report.getEFRCallFailsTot());
			setAdditionalResultInfo("Failed total calls: " + totalNumberOfFailedCalls);
			setAdditionalResultInfo("Total calls: " + totalNumberOfCalls);
			setAdditionalResultInfo("Successful Ratio is: " + (((double)totalNumberOfSuccessfulCalls)/totalNumberOfCalls)*100 + "%");
			
			setAdditionalResultInfo("########### SMS ###########");
	        setAdditionalResultInfo("Successful sent sms: " + (int)report.getSMSSuccTot());
			setAdditionalResultInfo("Failed sms: " + (int)report.getSMSFailsTot());
			setAdditionalResultInfo("Total sms: " + (int)(report.getSMSesTot()));
			setAdditionalResultInfo("Successful Ratio is: " + (((double)report.getSMSSuccTot())/((int)report.getSMSesTot()))*100 + "%");
			
			setAdditionalResultInfo("########### CCCHLoad ###########");
			setAdditionalResultInfo("Successful LRSAss: " + (int)report.getLRSAssigns());
            setAdditionalResultInfo("Total LRSAss: " + (int)(report.getLRSAssignsTot()));
            setAdditionalResultInfo("Successful LRSAss Ratio is: " + ((double)report.getLRSAssigns()/(int)report.getLRSAssignsTot())*100 + "%");
            setAdditionalResultInfo("Successful LRSAssRej: " + (int)report.getLRSAssRejs());
            setAdditionalResultInfo("Total LRSAssRej: " + (int)(report.getLRSAssRejsTot()));
            setAdditionalResultInfo("Successful LRSAssRej Ratio is: " + ((double)report.getLRSAssRejs()/(int)report.getLRSAssRejsTot())*100 + "%");
            setAdditionalResultInfo("Successful Paging: " + (int)report.getPagings());
            setAdditionalResultInfo("Total Paging: " + (int)(report.getPagingsTot()));
            setAdditionalResultInfo("Successful Paging Ratio is: " + ((double)report.getPagings()/(int)report.getPagingsTot())*100 + "%");
            setAdditionalResultInfo("###############################");
            
            setAdditionalResultInfo("TCH allocation ratio: " + report.getTCHAllocRatioTot() + " %");
			setAdditionalResultInfo("SDCCH allocation ratio: " + report.getSDCCHAllocRatioTot() + " %");
			setAdditionalResultInfo("Total pagings: " + (int)report.getPagingsTot());
			setAdditionalResultInfo("Total channel requests: " + (int)report.getChannelReqTot() + "</p>");
		}
		else {
			// Write summary info report
			setAdditionalResultInfo("<p><b>GRAT Stability load test summary:</b>");
			setAdditionalResultInfo("Call Success Rate (CSR): Failed test");
			setAdditionalResultInfo("Call Failure Rate (CFR): Failed test");
			setAdditionalResultInfo("SMS Success Rate  (SSR): Failed test");
			setAdditionalResultInfo("SMS Failure Rate  (SFR): Failed test</p>");
		}
		
		 setTestStepBegin("checking mo status after call");
		 checkStatus(cellNumber,trxdistribution);
		 setTestStepEnd();
	}
	
	private void checkStatus(int cellNumber, String [] trxdistribution) throws InterruptedException, JSONException {
	    for (int sectorId = 0; sectorId < cellNumber; sectorId++) {
         for(int trxId = Integer.parseInt(trxdistribution[sectorId].split(",")[0]) ; trxId <= Integer.parseInt(trxdistribution[sectorId].split(",")[1]); trxId ++) {
           setTestStepBegin("Check status for TrxId = " + trxId +", sector = " + sectorId);
           
           setTestInfo("Send Status Request for RX");
           statusRsp = abisHelper.statusRequest(this.moClassRx, sectorId, trxId, trxId, 255);
           setTestInfo("StatusResponse: " + statusRsp.toString());
           assertEquals("RX-state is not enabled", OM_G31R01.Enums.MOState.ENABLED, statusRsp.getMOState());
           
           setTestInfo("Send Status Request for TX");
           statusRsp = abisHelper.statusRequest(this.moClassTx, sectorId,trxId,trxId,255);
           setTestInfo("StatusResponse: " + statusRsp.toString());
           assertEquals("TX-state is not enabled", OM_G31R01.Enums.MOState.ENABLED, statusRsp.getMOState());
           
           for (int instanceNumber = 0; instanceNumber < 8; instanceNumber++) {
               setTestInfo("Send Status Request for TS = " + instanceNumber);
               statusRsp = abisHelper.statusRequest(this.moClassTs, sectorId, trxId, instanceNumber, trxId);
               setTestInfo("StatusResponse: " + statusRsp.toString());
               assertEquals("TS0-state is not enabled", OM_G31R01.Enums.MOState.ENABLED, statusRsp.getMOState());
               
               setTestInfo("Check MO state for TS = " + instanceNumber);
               String trxMo = String.format("%s,GsmSector=%s,Trx=%s", BTS_FUNCTION_LDN, Integer.toString(sectorId+1),trxId);  
               assertTrue("abisTsMoState for tsInstance 0 is not ENABLED", momHelper.waitForAbisTsMoStateGeneral(trxMo, instanceNumber, "ENABLED", 5));
           } 
           setTestStepEnd(); 
         }
    }
    }

	// Cleanup done by RestoreStack
}
