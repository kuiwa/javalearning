package com.ericsson.msran.test.grat.completestartup;

import java.util.List;

import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import se.ericsson.jcat.fw.annotations.Setup;
import se.ericsson.jcat.fw.annotations.Teardown;

import com.ericsson.abisco.clientlib.servers.CMDHLIB.CompleteStartRejectException;
import com.ericsson.msran.g2.annotations.TestInfo;

import com.ericsson.msran.test.grat.testhelpers.AbisHelper;
import com.ericsson.msran.test.grat.testhelpers.AbiscoConnection;
import com.ericsson.msran.test.grat.testhelpers.MomHelper;
import com.ericsson.msran.test.grat.testhelpers.NodeStatusHelper;
import com.ericsson.msran.test.grat.testhelpers.TgLdns;

import com.ericsson.msran.jcat.TestBase;

/**
 * @id NodeUC426,NodeUC428
 * 
 * @name CreateUnlockMultipleTrexes
 * 
 * @author GRAT Cell
 * 
 * @created 2015-07-08
 * 
 * @description This test will create GsmSectors with several Trxes.
 *              Everything will be configured and enabled with the complete startup
 *              command towards Abisco.
 * 
 * @revision ewegans 2016-07-08 Initial version
 */

public class CompleteStartupTest extends TestBase {

    private MomHelper momHelper;
    private AbiscoConnection abisco;
    private AbisHelper abisHelper;
    private NodeStatusHelper nodeStatus;
    
    /**
     * Description of test case for test reporting
     */
    @TestInfo(
            tcId = "UC513.N, UC602.N, UC607.N, UC627.N, UC646.N",
            slogan = "Complete startup of all MOs",
            requirementDocument = "1/00651-FCP 130 1402",
            requirementRevision = "PC5",
            requirementLinkTested = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff87c1d941?docno=1/00651-FCP1301402Uen&format=excel8book",
            requirementLinkLatest = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff86a2af3d?docno=1/00651-FCP1301402Uen&action=current&format=excel8book",
            requirementIds = { "10565-0334/19417[A][APPR]" },
            verificationStatement = "Verifies (not checking that every MO state is correct after every step) UC513.N, UC602.N, UC607.N, UC627.N, UC646.N",
            testDescription = "Verifies that all MOs can be created, unlocked, and enabled with the Abisco Complete Start command",
            traceGuidelines = "N/A")
    
    
    /**
     * Precheck.
     */
    @Setup
    public void setup() {
        setTestStepBegin("Start of setup()");
        nodeStatus = new NodeStatusHelper();
        setTestStepBegin("After new NodeStatusHelper();");
        assertTrue("Node did not reach a working state before test start", nodeStatus.waitForNodeReady());
        setTestStepBegin("After nodeStatus.waitForNodeReady()");
        momHelper = new MomHelper();
        setTestStepBegin("After new MomHelper();");
        abisco = new AbiscoConnection();
        setTestStepBegin("After new AbiscoConnection();");
        abisHelper = new AbisHelper();
        setTestStepBegin("End of setup()");
    }
    
    /**
     * Postcheck.
     */
    @Teardown
    public void teardown() {
        setTestStepBegin("Start of teardown()");
        nodeStatus.isNodeRunning();
        setTestStepBegin("End of teardown()");
    }
    
    /**
     * @name completeStartup
     * 
     * @description Create, unlock and enable four Trxes on one sector.
     * 
     * @param testId - unique identifier of the test case
     * @param description
     * @param numOfTGs 
     * @param numOfTrxesPerTG 
     * @param loopNum 
     * @throws InterruptedException
     */
    @Test(timeOut = 7200000)
    @Parameters({ "testId", "description", "numOfTGs", "numOfTrxesPerTG", "loopNum"})
    public void completeStartup(String testId, String description, final int numOfTGs, final int numOfTrxesPerTG, @Optional("5") int loopNum) throws InterruptedException {
        setTestCase(testId, description);
        
        // Test variables
        List<TgLdns> tgLdnsList = momHelper.createUnlockAllGratMos(numOfTGs, numOfTrxesPerTG);
        
        setTestStepBegin("Setup Abisco for " + numOfTGs + " TGs");
        abisco.setupAbisco(numOfTGs, numOfTrxesPerTG, false, "off");
        setTestStepEnd();
        
        tgLdnsList = momHelper.findGratMos();
        
        for (int iteration = 1; iteration <= loopNum; ++iteration) {
            setTestInfo("Start iteration = " + iteration);
            
            // Unlock and enable everything
            for (int current_tg = 0; current_tg < numOfTGs; ++current_tg) {
                setTestInfo("Current TG = " + current_tg);
                TgLdns tg = tgLdnsList.get(current_tg);
                
                // MOs are unlocked at the start of first iteration
                if (iteration > 1) {
                    setTestStepBegin("Unlock MOs");
                    momHelper.unlockMo(tg.abisIpLdn);
                    for (String trxLdn: tg.trxLdnList) momHelper.unlockMo(trxLdn);
                    setTestStepEnd();
                }
                
                setTestStepBegin("Verify AbisIp MO states after unlock");
                assertEquals(tg.abisIpLdn + " did not reach correct state", "", 
                        momHelper.checkAbisIpMoAttributeAfterUnlockBscConnected(tg.abisIpLdn, abisco.getBscIpAddress(), 30));
                setTestStepEnd();
                
                for (String trxLdn: tg.trxLdnList) {
                    setTestStepBegin("Verify Trx MO states after unlock for " + trxLdn);
                    assertEquals(trxLdn + " did not reach correct state", "", 
                            momHelper.checkTrxMoAttributeAfterUnlockNoLinks(trxLdn, 20));
                    setTestStepEnd();
                }
                
                // This is where it happens
                setTestStepBegin("Enable all Abis MOs in TG " + current_tg);
                try {
                    abisHelper.completeStartup(current_tg, numOfTrxesPerTG);
                }
                catch (CompleteStartRejectException e) {
                    fail("Complete startup of TG = " + current_tg + " caused CompleteStartRejectException: " + e.getMessage());
                }
                
                setTestStepEnd();
                
                setTestStepBegin("Verify AbisIp MO after complete startup " + tg.abisIpLdn);
                assertEquals(tg.abisIpLdn + " did not reach correct state", "", 
                        momHelper.checkAbisIpMoAttributeAfterUnlockBscConnected(tg.abisIpLdn, abisco.getBscIpAddress(), 10));
                setTestStepEnd();
                
                setTestStepBegin("Verify GsmSector MO after complete startup " + tg.sectorLdn);
                assertEquals(tg.sectorLdn + " did not reach correct state", "", 
                        momHelper.checkGsmSectorMoAttributeAfterAllMosStartedAoAtAndAoTfEnabled(tg.sectorLdn, 10));
                setTestStepEnd();
                
                for (String trxLdn: tg.trxLdnList) {
                    setTestStepBegin("Verify Trx MO states after complete start up " + trxLdn);
                    assertEquals(trxLdn + " did not reach correct state", "", 
                            momHelper.checkTrxMoAttributeAfterUnlockAllEnabledLinksEstablished(trxLdn, 10));
                    setTestStepEnd();
                }
            }
            
            // Let it stand for a while
            sleepSeconds(15);
            
            // check that everything is still up
            for (int current_tg = 0; current_tg < numOfTGs; ++current_tg) {
                TgLdns tg = tgLdnsList.get(current_tg);
                setTestStepBegin("Verify AbisIp MO after complete startup " + tg.abisIpLdn);
                assertEquals(tg.abisIpLdn + " did not reach correct state", "", 
                        momHelper.checkAbisIpMoAttributeAfterUnlockBscConnected(tg.abisIpLdn, abisco.getBscIpAddress(), 10));
                setTestStepEnd();
                
                setTestStepBegin("Verify GsmSector MO after complete startup " + tg.sectorLdn);
                assertEquals(tg.sectorLdn + " did not reach correct state", "", 
                        momHelper.checkGsmSectorMoAttributeAfterAllMosStartedAoAtAndAoTfEnabled(tg.sectorLdn, 10));
                setTestStepEnd();
                
                for (String trxLdn: tg.trxLdnList) {
                    setTestStepBegin("Verify Trx MO states after complete start up " + trxLdn);
                    assertEquals(trxLdn + " did not reach correct state", "", 
                            momHelper.checkTrxMoAttributeAfterUnlockAllEnabledLinksEstablished(trxLdn, 10));
                    setTestStepEnd();
                }               
            }
            
            
            // Lock everything
            for (int current_tg = 0; current_tg < numOfTGs; ++current_tg) {
                TgLdns tg = tgLdnsList.get(current_tg);
                setTestStepBegin("Lock AbisIp MO and all " + numOfTrxesPerTG + " Trx MOs in TG " + current_tg);
                for (String trxLdn: tg.trxLdnList) {
                    momHelper.lockMo(trxLdn);
                }
                momHelper.lockMo(tg.abisIpLdn);
                setTestStepEnd();
            }
            setTestInfo("Finished iteration = " + iteration);
        }
    }  
}