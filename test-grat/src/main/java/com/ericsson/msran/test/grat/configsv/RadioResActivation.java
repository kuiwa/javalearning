package com.ericsson.msran.test.grat.configsv;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.json.JSONException;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import se.ericsson.jcat.fw.annotations.Setup;
import se.ericsson.jcat.fw.annotations.Teardown;

import com.ericsson.abisco.clientlib.servers.OM_G31R01;
import com.ericsson.abisco.clientlib.servers.OM_G31R01.Enums;
import com.ericsson.abisco.clientlib.servers.OM_G31R01.FSOffset;
import com.ericsson.msran.g2.annotations.TestInfo;
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
 * @description This test class connect Abisco Link and set up multiple Trx
 * 
 * @revision  epkuad & epksteo First version. 
 *            eyngjng 2015-08-1 Optimize for WP4489
 */

public class RadioResActivation extends TestBase {
	
	 private final OM_G31R01.Enums.MOClass moClassTx = OM_G31R01.Enums.MOClass.TX;    
	    private final OM_G31R01.Enums.MOClass moClassRx = OM_G31R01.Enums.MOClass.RX;    
	    private final OM_G31R01.Enums.MOClass moClassTs = OM_G31R01.Enums.MOClass.TS;
	
	
	private PidPrePost pidPrePost;
	private MomHelper momHelper;
	private AbiscoConnection abisco;
	private NodeStatusHelper nodeStatus;
	private AbisHelper abisHelper;
//	private static String sectorLdn = "ManagedElement=1,BtsFunction=1,GsmSector=1";
//	private static String SECTOR_EQUIP_FUNC_LDN = "ManagedElement=1,NodeSupport=1,SectorEquipmentFunction=1";
	private String abisIp_ldn;
	private String sector_ldn;
    private static String BTS_FUNCTION_LDN = "ManagedElement=1,BtsFunction=1";
    private static String SECTOR_EQUIP_FUNC_LDN = "ManagedElement=1,NodeSupport=1,SectorEquipmentFunction=";
    private String sectorEquipmentFunctionLdnToUse;
	private AbisPrePost abisPrePost;
	private Component frame;
	private List<Integer> trxcList = new ArrayList(0);
	private List<String> trxcLdnList = new ArrayList(0);
	private List<Integer>  bcchTrxList= new ArrayList(0);
	private String[] sectorRefList;
	private boolean bcchFlag = false;
	private String connectionName = "host_";
	private OM_G31R01.StatusResponse statusRsp;
	

	/**
	 * Description of test case for test reporting
	 */
	@TestInfo(tcId = "NodeUC524", slogan = "Detect, recover from and report GSM Fault", requirementDocument = "1/00651-FCP 130 1402", requirementRevision = "PC5", requirementLinkTested = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff87c1d941?docno=1/00651-FCP1301402Uen&format=excel8book", requirementLinkLatest = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff86a2af3d?docno=1/00651-FCP1301402Uen&action=current&format=excel8book", requirementIds = {
			"10565-0334/21767[PA1][PREL]", "10565-0334/19034[A][APPR]",
			"10565-0334/19417[A][APPR]" }, verificationStatement = "Verifies NodeUC524.A3, NodeUC524.A4", testDescription = "Detect that BSC disconnects and connects.", traceGuidelines = "N/A")
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
	 * 
	 * @throws InterruptedException
	 * @throws JSONException
	 */
	@Test(timeOut = 5400000)
	@Parameters({ "testId", "description", "noTrxsInGsmSector", "cellNumber", "bcchNoList", "trxDistributionList", "trxsectorDistributionList", "hoppingIndicator"})
	public void configRadioTest(String testId, String description, @Optional("1") int noTrxsInGsmSector, @Optional("1") int cellNumber,
	        String bcchNoList, @Optional("0,0") String trxDistributionList, @Optional("0,0,1") String trxsectorDistributionList, @Optional("Off") String hoppingIndicator)
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
        
//		abisco.setupAbisco(noTrxsInGsmSector, cellNumber, bcchNumberlist, trxdistribution);
        
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
		
		setTestStepBegin("Estabish SCF /AT/TFlinks");
		sleep(10);
		for (int tgId = 0; tgId < cellNumber; tgId++) {
		    try {
		        setTestInfo("SCF links");
		        abisco.establishLinks(tgId, false, Integer.parseInt(trxdistribution[tgId].split(",")[0]));
		        setTestInfo("AT links");
		        assertTrue("Could not activate AT for tgId=" + tgId,
	                    abisHelper.startSectorMosAndActivateAT(tgId));
		        setTestInfo("TF links");
		        OM_G31R01.TFConfigurationResult tfConfigResult = abisHelper.tfConfigRequest(tgId, 1, Enums.TFMode.Master, new FSOffset(new Integer(0xFF),new Long(0xFFFFFFFFL)));
//	              OM_G31R01.TFConfigurationResult tfConfigResult = abisHelper.tfConfigRequest(tgId, 1, Enums.TFMode.Master, new FSOffset(new Long(0xFFFFFFFFL), new Integer(0xFF)));      
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
		
		// Establish SCF Links
//		setTestStepBegin("Estabish SCF Links and start SCF/TF/AT");
//		try {
//			abisco.establishLinks();
//		} catch (InterruptedException ie) {
//			fail("InteruptedException during establishLinks");
//		}
		
//		setTestStepBegin("Establish AT links");
//		for (int tgId=0; tgId < cellNumber; tgId++) {
//		    assertTrue("Could not activate AT for tgId=" + tgId,
//	                abisHelper.startSectorMosAndActivateAT(tgId));
//		}
//		setTestStepEnd(); //Amy Yang
		
		// Enable AT
//		assertTrue("Could not activate AT",
//				abisHelper.startSectorMosAndActivateAT());
		
//		sleep(20);
//		setTestStepBegin("Establish TF links"); 
//		for (int tgId=0; tgId < cellNumber; tgId++){
//		    OM_G31R01.TFConfigurationResult tfConfigResult = abisHelper.tfConfigRequest(tgId, 1, Enums.TFMode.Master, new FSOffset(new Long(0xFFFFFFFFL), new Integer(0xFF)));
//		    assertTrue(
//	                "AccordanceIndication not according to Request",
//	                tfConfigResult.getAccordanceIndication()
//	                        .getAccordanceIndication() == Enums.AccordanceIndication.AccordingToRequest);
//		    assertTrue("Failed enableTf for tgId="+tgId, abisHelper.enableTf(tgId));
//		}
//		setTestStepEnd();//Amy Yang
		
//		setTestInfo("Precondition: configure AO TF");
//		OM_G31R01.TFConfigurationResult tfConfigResult = abisHelper.tfConfigRequest(1, Enums.TFMode.Master, new FSOffset(new Long(0xFFFFFFFFL), new Integer(0xFF)));
//		assertTrue(
//				"AccordanceIndication not according to Request",
//				tfConfigResult.getAccordanceIndication()
//						.getAccordanceIndication() == Enums.AccordanceIndication.AccordingToRequest);
//
//		assertTrue("enableTf failed", abisHelper.enableTf());
//	    setTestStepEnd();
	    
	    setTestStepBegin("Establish TRX links");
	    sleep(10);
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
//	                    assertTrue("Enable of  BCCH failed",
//	                            abisHelper.enableAnyTs(sectorId,trxId, 0,
//	                                    OM_G31R01.Enums.Combination.MainBCCH));
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
		// Establish TRX links
//		setTestStepBegin("Establish TRX links for each Trx");
//		for (int trxId = 0; trxId < trxcList.size(); trxId++) {
//			
//		    bcchFlag = false;
//			setTestStepBegin("Setup for TrxListSize = " + trxcList.size() + "trxId = " + trxId);
//			
//			abisco.establishLinks(0,true, trxcList.get(trxId));
//			sleepSeconds(2);
//
//			abisPrePost.preCondTrxStateStartedMy(trxId);
//			
//
//		    int freq = abisco.arfcnListMy.get(trxId);
//			
//			assertTrue("enableTx failed", abisHelper.enableTx(trxId,freq));
//			assertTrue("enableRx failed", abisHelper.enableRx(trxId,freq));
//			
//			for (int i = 0; i < bcchTrxList.size(); i++ ) {
//			    if (trxId == bcchTrxList.get(i)) {
//			        bcchFlag = true;
//			    }
//			}
//			  
//			if (bcchFlag == true) {
//				try {
//					assertTrue("Enable of  BCCH failed",
//							abisHelper.enableAnyTs(trxId, 0,
//									OM_G31R01.Enums.Combination.MainBCCH));
//
//				} catch (InterruptedException | JSONException e) {
//					e.printStackTrace();
//				}
//				System.out.println("Enable BCCHs in TRX-" + trxId);
//
//				try {
//					assertTrue("Enable of TS failed SDCCH",
//							abisHelper.enableAnyTs(trxId, 1,
//									OM_G31R01.Enums.Combination.SDCCH));
//				} catch (InterruptedException | JSONException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				System.out.println("Enable SDCCH in TRX-" + trxId);
//
//				int tsInstance = 2;
//				while (tsInstance < 8) {
//					System.out.println("Enable TCHs on TS" + tsInstance
//							+ " in TRX-" + trxId);
//					try {
//						assertTrue("Enable of TS failed TCH",
//								abisHelper.enableAnyTs(trxId, tsInstance,
//										OM_G31R01.Enums.Combination.TCH));
//					} catch (InterruptedException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					} catch (JSONException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//					tsInstance++;
//				}
//			} else {
//				int tsInstance = 0;
//				//String tmpTrxLdn = momHelper.createTrxMo(sectorLdn, Integer.toString(trxId));
//				System.out.println("Enable TCHs on TS" + tsInstance
//						+ " in TRX-" + trxId);
//
//				while (tsInstance < 8) {
//					try {
//						assertTrue("Enable of TS failed TCH",
//								abisHelper.enableAnyTsMy(trxcLdnList.get(trxId),trxId, tsInstance,
//										OM_G31R01.Enums.Combination.TCH,freq));
//					} catch (InterruptedException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					} catch (JSONException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//					tsInstance++;
//				}
//
//			}
//			setTestStepEnd();
//		}
//		setTestStepEnd();
		
		try {	
		    setTestStepBegin("Send and heck sys Info");
		    for (int tgId =0; tgId < cellNumber; tgId ++) {
		        abisHelper.sendBCCHInfoWithCellId(tgId, Integer.parseInt(trxdistribution[tgId].split(",")[0]));
		    }
//			abisHelper.sysInfo();
			setTestStepEnd();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		JOptionPane.showMessageDialog(frame, "Press ok to continue1.");	
		JOptionPane.showMessageDialog(frame, "Press ok to continue2.");	
		JOptionPane.showMessageDialog(frame, "Press ok again to end the test case.");	
		
//		 checkStatus(); 
		 setTestInfo("checking mo status");
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
		/* callSetupMt(); sleepSeconds(120); // allows for some time before
		 everything is torn // down disconnectCallMt();
*/		 
	}

	
	private void checkStatus() throws InterruptedException, JSONException {
        OM_G31R01.StatusResponse statusRsp;
        
        for (int trxId = 0; trxId < trxcList.size(); trxId++) {
            setTestStepBegin("Check status for TrxId = " + trxId);
            int freq = abisco.arfcnListMy.get(trxId);
            
            setTestInfo("Send Status Request for RX");
            statusRsp = abisHelper.statusRequest(this.moClassRx, 0, trxId, trxId, 255);
            setTestInfo("StatusResponse: " + statusRsp.toString());
            assertEquals("RX-state is not enabled", OM_G31R01.Enums.MOState.ENABLED, statusRsp.getMOState());
            
            setTestInfo("Send Status Request for TX");
            statusRsp = abisHelper.statusRequest(this.moClassTx, 0,trxId,trxId,255);
            setTestInfo("StatusResponse: " + statusRsp.toString());
            assertEquals("TX-state is not enabled", OM_G31R01.Enums.MOState.ENABLED, statusRsp.getMOState());
            
            for (int instanceNumber = 0; instanceNumber < 8; instanceNumber++) {
                setTestInfo("Send Status Request for TS = " + instanceNumber);
                statusRsp = abisHelper.statusRequest(this.moClassTs, 0, trxId, instanceNumber, trxId);
                setTestInfo("StatusResponse: " + statusRsp.toString());
                assertEquals("TS0-state is not enabled", OM_G31R01.Enums.MOState.ENABLED, statusRsp.getMOState());
                
                setTestInfo("Check MO state for TS = " + instanceNumber);
                String trxMo = MomHelper.SECTOR_LDN + ",Trx=" + trxId;
                assertTrue("abisTsMoState for tsInstance 0 is not ENABLED", momHelper.waitForAbisTsMoStateGeneral(trxMo, instanceNumber, "ENABLED", 5));
            } 
            setTestStepEnd(); 
        }
    	
     /*   setTestStepBegin("Print out stats for later faultfinding");
    	cli.connect();
    	setTestInfo("rhdc icmstatus:\n"     + cli.send("rhdc icmstatus"));
    	setTestInfo("rhdc icmtemp:\n"       + cli.send("rhdc icmtemp"));
    	setTestInfo("rhdc icmxio_status:\n" + cli.send("rhdc icmxio_status"));
    	setTestInfo("rhdc icmiqcx_stats:\n" + cli.send("rhdc icmiqcx_stats"));
    	setTestInfo("rhdc icmiqx_status:\n" + cli.send("rhdc icmiqx_status"));
    	setTestInfo("rhdc icmiqx_config:\n" + cli.send("rhdc icmiqx_config"));
    	cli.disconnect();
    	setTestStepEnd();*/

}

	// Cleanup done by RestoreStack
}
