package com.ericsson.msran.test.grat.trxtestnorestore;

import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import se.ericsson.jcat.fw.annotations.Setup;
import se.ericsson.jcat.fw.annotations.Teardown;
import com.ericsson.commonlibrary.managedobjects.ManagedObject;
import com.ericsson.commonlibrary.managedobjects.ManagedObjectValueAttribute;
import com.ericsson.commonlibrary.managedobjects.ManagedObjectValueStringAttribute;
import com.ericsson.msran.g2.annotations.TestInfo;
import com.ericsson.msran.jcat.TestBase;
import com.ericsson.msran.test.grat.testhelpers.AbiscoConnection;
import com.ericsson.msran.test.grat.testhelpers.MomHelper;

/**
 * @id NodeUC424,NodeUC426
 * 
 * @name CreateAndUnlockTrx
 * 
 * @author GRAT
 * 
 * @created 2015-03-12
 * 
 * @description This test will create and unlock 3 Trx:es.
 *              NOTE! No cleanup nor restore stack will be used - i.e. the Trx:es will deliberately be left on the node.
 * 
 * @revision eraweer 2015-03-12 first version
 * 
 */

public class CreateAndUnlockTrx extends TestBase {   
    private MomHelper momHelper;

    /**
     * Description of test case for test reporting
     */
    @TestInfo(
            tcId = "NodeUC424,NodeUC426",
            slogan = "Create And Unlock TRX",
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
     * @name createAndUnlockTrx
     * 
     * @description Create and unlock 3 Trx MOs
     * 
     * @param testId - unique identifier of the test case
     * @param description
     */
    @Test(timeOut = 500000)
    @Parameters({ "testId", "description" })
    public void createAndUnlockTrx(String testId, String description) {
        
        setTestCase(testId, description);
        String sectorLdn = "ManagedElement=1,BtsFunction=1,GsmSector=1";
        String seqEqFuncLdn = momHelper.getSectorEquipmentFunctionLdn();
        ManagedObject trxMo;
        
        // Create BtsFunction
        ManagedObject btsFunctionMo = momHelper.buildBtsFunctionMo("ManagedElement=1,BtsFunction=1", MomHelper.BTS_USER_LABEL_VALUE);
      	momHelper.createManagedObject(btsFunctionMo);

        // Create GsmSector
        ManagedObject sectorMo = momHelper.buildGsmSectorMo(sectorLdn);
        momHelper.createManagedObject(sectorMo);
        
        // Create AbisIp
    	String abisIpLdn = sectorLdn + ",AbisIp=1";        
      	ManagedObject abisMo = momHelper.buildAbisIpMo(
    			AbiscoConnection.getConnectionName(),
    			"147.214.13.37",
    			abisIpLdn,
    			MomHelper.TNA_IP_LDN,
    			MomHelper.LOCKED);        
        momHelper.createManagedObject(abisMo);
        
        // Create and Unlock 3 Trx
        for (int i = 1; i <= 3; i++) { 
            String trxLdn = String.format("%s,Trx=%s", sectorLdn, Integer.toString(i));          
            trxMo = new ManagedObject(trxLdn);
            trxMo.addAttribute(new ManagedObjectValueAttribute("txPower", "10"));
            trxMo.addAttribute(new ManagedObjectValueAttribute("arfcnMin", "1"));
            trxMo.addAttribute(new ManagedObjectValueAttribute("arfcnMax", "120"));
            trxMo.addAttribute(new ManagedObjectValueAttribute("frequencyBand", "0"));
            trxMo.addAttribute(new ManagedObjectValueStringAttribute(MomHelper.SECTOR_EQUIP_FUNC_REF, seqEqFuncLdn));
            momHelper.createManagedObject(trxMo);
            assertTrue("MO (" + trxLdn + ") exists", momHelper.checkMoExist(trxLdn));
            
            momHelper.setAttributeForMoAndCommit(trxLdn, MomHelper.ADMINISTRATIVE_STATE, "UNLOCKED");
            // Check that it is unlocked
            assertEquals("Trx MO attributes did not reach expected values after unlock", "", momHelper.checkTrxMoAttributeAfterUnlockNoLinks(trxLdn, 15));
        }
        
        // Leave it!!!
    }
}