package com.ericsson.msran.test.grat.trxtestnorestore;

import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import se.ericsson.jcat.fw.annotations.Setup;
import se.ericsson.jcat.fw.annotations.Teardown;
import com.ericsson.msran.g2.annotations.TestInfo;
import com.ericsson.msran.jcat.TestBase;
import com.ericsson.msran.test.grat.testhelpers.MomHelper;

/**
 * @id NodeUC428,NodeUC425
 * 
 * @name LockAndDeleteTrx
 * 
 * @author GRAT
 * 
 * @created 2015-03-12
 * 
 * @description This test will lock and delete 3 Trx:es.
 *              NOTE! This test case assumes that 3 Trx:es already have been created 
 *                    - i.e. this test case will deliberately not create any Trx:es.
 * 
 * @revision eraweer 2015-03-12 first version
 * 
 */


public class LockAndDeleteTrx extends TestBase {
    private MomHelper momHelper;

    /**
     * Description of test case for test reporting
     */
    @TestInfo(
            tcId = "NodeUC428,NodeUC425",
            slogan = "Lock and Delete TRX",
            requirementDocument = "1/00651-FCP 130 1402",
            requirementRevision = "PC5",
            requirementLinkTested = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff87c1d941?docno=1/00651-FCP1301402Uen&format=excel8book",
            requirementLinkLatest = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff86a2af3d?docno=1/00651-FCP1301402Uen&action=current&format=excel8book",
            requirementIds = { "10565-0334/19417[A][APPR]" },
            verificationStatement = "",
            testDescription = "",
            traceGuidelines = "N/A")

    
    /**
     * Precheck.
     */
    @Setup
    public void setup() {
    	setTestStepBegin("Start of setup()");
        momHelper = new MomHelper();
        setTestStepEnd();
    }
    
    /**
     * Postcheck.
     */
    @Teardown
    public void teardown() {
    	setTestStepBegin("Start of teardown()");
    	setTestStepEnd();
    }

/**
 * @name LockAndDeleteTrx 
 * 
 * @description Lock and delete 3 Trx MOs
 * 
 * @param testId - unique identifier of the test case
 * @param description
 */
@Test(timeOut = 500000)
@Parameters({ "testId", "description" })
public void lockAndDeleteTrx(String testId, String description) {
    String sectorLdn = "ManagedElement=1,BtsFunction=1,GsmSector=1";
    
    setTestCase(testId, description);
    
    // Precond. 3 Trx MOs (Unlocked), 1 AbisIp MO (Locked), 1 GsmSector MO, BtsFunction MO.
    
    // Lock and Delete 3 Trx
    for (int i = 1; i <= 3; i++) { 
        String trxLdn = String.format("%s,Trx=%s", sectorLdn, Integer.toString(i)); 
        momHelper.setAttributeForMoAndCommit(trxLdn, MomHelper.ADMINISTRATIVE_STATE, "LOCKED");        
        assertEquals("Trx MO attributes did not reach expected values after lock", "", momHelper.checkTrxMoAttributeAfterLock(trxLdn, 15));
        
        momHelper.deleteMo(trxLdn);
        assertFalse("MO (" + trxLdn + ") exists", momHelper.checkMoExist(trxLdn));
    }
    
    // Delete AbisIp
	String abisIpLdn = sectorLdn + ",AbisIp=1";
    momHelper.deleteMo(abisIpLdn); 

    // Delete GsmSector
    momHelper.deleteMo(sectorLdn);
    
    // Delete BtsFunction
    momHelper.deleteMo(MomHelper.BTS_FUNCTION_LDN);
	}
}
