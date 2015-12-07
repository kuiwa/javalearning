package com.ericsson.msran.test.grat.calendartimeexchange;

import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import se.ericsson.jcat.fw.annotations.Setup;
import se.ericsson.jcat.fw.annotations.Teardown;

import com.ericsson.abisco.clientlib.servers.CHEXTRAS.CHRejectException;
import com.ericsson.abisco.clientlib.servers.OM_G31R01;
import com.ericsson.msran.g2.annotations.TestInfo;
import com.ericsson.msran.test.grat.testhelpers.AbisHelper;
import com.ericsson.msran.test.grat.testhelpers.NodeStatusHelper;
import com.ericsson.msran.test.grat.testhelpers.prepost.AbisPrePost;
import com.ericsson.msran.jcat.TestBase;


/**
 * @id NodeUC476, NodeUC509
 * 
 * @name CalendarTimeExchangeRequestTest
 * 
 * @author Grat 
 * 
 * @created 2014-12-03
 * 
 * @description This test class verifies the Abis Calendar Exchange Request EP.
 * 
 * @revision grat 2014-12-03 First version.
 * 
 */

public class CalendarTimeExchangeRequestTest extends TestBase {
    private AbisPrePost abisPrePost;
    private AbisHelper abisHelper;
    private NodeStatusHelper nodeStatus;
    
    /**
     * Description of test case for test reporting
     */
    @TestInfo(
            tcId = "NodeUC650",
            slogan = "Abis SO SCF Calendar Time Exchange",
            requirementDocument = "1/00651-FCP 130 1402",
            requirementRevision = "PC5",
            requirementLinkTested = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff87c1d941?docno=1/00651-FCP1301402Uen&format=excel8book",
            requirementLinkLatest = "http://erilink.ericsson.se/eridoc/erl/objectId/09004cff86a2af3d?docno=1/00651-FCP1301402Uen&action=current&format=excel8book",
            requirementIds = { "10565-0334/21767[PA1][PREL]", "10565-0334/19034[A][APPR]",
                    "10565-0334/19417[A][APPR]" },
            verificationStatement = "Verifies NodeUC650.N",
            testDescription = "Verifies Abis SO SCF Calendar Time Exchange Request",
            traceGuidelines = "N/A")


    /**
     * Create MO:s, establish links to Abisco, and verify preconditions.
     */
    @Setup
    public void setup() {
    	setTestStepBegin("Setup");
        nodeStatus = new NodeStatusHelper();
        assertTrue("Node did not reach a working state before test start", nodeStatus.waitForNodeReady());
        abisHelper = new AbisHelper();
        abisPrePost = new AbisPrePost();
        abisPrePost.preCondSoScfReset();
        setTestStepEnd();
    }
    
    /**
     * Postcond.
     */
    @Teardown
    public void teardown() {
    	setTestStepBegin("Teardown");
        nodeStatus.isNodeRunning();
        abisHelper.setDefaultNegotiationBehaviour();
        setTestStepEnd();
    }
    

    /**
     * @name calendarTimeExchangeRequest
     * 
     * @description Verifies the Calendar Time Exchange EP on SO SCF according to NodeUC650.N
     *
     * @param testId - unique identifier
     * @param description
     * 
     * @throws InterruptedException
     */
    @Test (timeOut = 360000)
    @Parameters({ "testId", "description" })
    public void calendarTimeExchangeRequest(String testId, String description) throws InterruptedException {
    	setTestCase(testId, description);
    	
    	int tgId           = 0;
    	int bsc_year       = 2014;
    	int bsc_month      = 12;
    	int bsc_day        = 9;
    	int bsc_hour       = 17;
    	int bsc_minute     = 10;
    	int bsc_second     = 43;
    	int bsc_deciSecond = 2;
    	
    	for (int i = 0 ; i < 3 ; ++i) {
    		
    		setTestStepBegin("Send Calendar Time Exchange Request");
    		
    		// Send Calendar Time Exchange Request, and wait for a response
    		OM_G31R01.CalendarTimeExchangeResponse calendarExResponse = 
    				abisHelper.calendarTimeExchange(tgId, bsc_year, bsc_month, bsc_day, bsc_hour, bsc_minute, bsc_second, bsc_deciSecond);

    		setTestStepBegin("Check Calendar Time Exchange Response");

    		setTestInfo("Checking the BTS Time in the response");
    		OM_G31R01.CalendarTimeBTS btsTime = calendarExResponse.getCalendarTimeBTS();
    		setTestInfo("Received BTS Time in the response:" + btsTime.getYear() + " " + btsTime.getMonth() + " " + btsTime.getDay() + " " + btsTime.getHour() + " " + btsTime.getMinute() + " " + btsTime.getSecond() + " " + btsTime.getDeciSecond());

    		assertTrue("Calendar Exchange Response contained an unexpected year in BTS Time " + btsTime.getYear(),  ((1970 < btsTime.getYear()) && ((9999 > btsTime.getYear()))));
    		assertTrue("Calendar Exchange Response contained an unexpected month in BTS Time " + btsTime.getMonth(), ((0 < btsTime.getMonth()) && (13 > btsTime.getMonth())));
    		assertTrue("Calendar Exchange Response contained an unexpected day in BTS Time " + btsTime.getDay(), ((0 < btsTime.getDay()) && (32 > btsTime.getDay())));
    		assertTrue("Calendar Exchange Response contained an unexpected hour in BTS Time " + btsTime.getHour(), (24 > btsTime.getHour()));
    		assertTrue("Calendar Exchange Response contained an unexpected minute in BTS Time " + btsTime.getMinute(), (60 > btsTime.getMinute()));
    		assertTrue("Calendar Exchange Response contained an unexpected second in BTS Time " + btsTime.getSecond(), (60 > btsTime.getSecond()));
    		assertTrue("Calendar Exchange Response contained an unexpected deciSecond in BTS Time " + btsTime.getDeciSecond(), (10 > btsTime.getDeciSecond()));

    		setTestInfo("Checking the BSC Time in the response");
    		OM_G31R01.CalendarTimeBSC bscTime = calendarExResponse.getCalendarTimeBSC();
    		setTestInfo("Received BSC Time in the response:" + bscTime.getYear() + " " + bscTime.getMonth() + " " + bscTime.getDay() + " " + bscTime.getHour() + " " + bscTime.getMinute() + " " + bscTime.getSecond() + " " + bscTime.getDeciSecond());

    		assertEquals("Calendar Exchange Response contained an unexpected year in BSC Time " + bscTime.getYear(),       bsc_year,       bscTime.getYear());
    		assertEquals("Calendar Exchange Response contained an unexpected month in BSC Time " + bscTime.getMonth(),      bsc_month,      bscTime.getMonth());
    		assertEquals("Calendar Exchange Response contained an unexpected day in BSC Time " + bscTime.getDay(),        bsc_day,        bscTime.getDay());
    		assertEquals("Calendar Exchange Response contained an unexpected hour in BSC Time " + bscTime.getHour(),       bsc_hour,       bscTime.getHour());
    		assertEquals("Calendar Exchange Response contained an unexpected minute in BSC Time " + bscTime.getMinute(),     bsc_minute,     bscTime.getMinute());
    		assertEquals("Calendar Exchange Response contained an unexpected second in BSC Time " + bscTime.getSecond(),     bsc_second,     bscTime.getSecond());
    		assertEquals("Calendar Exchange Response contained an unexpected deciSecond in BSC Time " + bscTime.getDeciSecond(), bsc_deciSecond, bscTime.getDeciSecond());

    		setTestInfo("Checking the Sequence Number in the response");
    		assertEquals("Calendar Exchange Response contained an unexpected sequence number", i + 1, calendarExResponse.getCalendarTimeSequenceNum());

    	}
    	
    	setTestStepEnd();
    }

    /**
     * @name calendarTimeExchangeRequest_Attribute_Error
     * 
     * @description Verifies the Calendar Time Exchange EP on SO SCF when the request contains an attribute error, according to NodeUC479.E1
     * 
     * @param testId - unique identifier
     * @param description
     * 
     * @throws InterruptedException
     */
    @Test (timeOut = 360000)
    @Parameters({ "testId", "description" })
    public void calendarTimeExchangeRequest_Attribute_Error(String testId, String description) throws InterruptedException {
    	setTestCase(testId, description);
   	
    	setTestStepBegin("Send Calendar Time Exchange Request");
    	
    	int tgId     = 0;
    	    	
    	int bsc_year       = 2014;
    	int bsc_month      = 12;
    	int bsc_day        = 9;
    	int bsc_hour       = 17;
    	int bsc_minute     = 10;
    	int bsc_second     = 43;
    	int bsc_deciSecond = 2;
    	
    	OM_G31R01.CalendarTimeExchangeReject calendarExReject = null;
    	
    	try {
    		// set the deciSeconds to a to large value to get a reject from BTS
    		bsc_deciSecond = 25;
    		abisHelper.calendarTimeExchange(tgId, bsc_year, bsc_month, bsc_day, bsc_hour, bsc_minute, bsc_second, bsc_deciSecond);
    		fail("Received a Calendar Time Exchange Response, but a Calendar Time Exchange Reject was expected");
    	} catch (OM_G31R01.CalendarTimeExchangeRejectException e) {
    		setTestStepBegin("********** Got the expected CalendarTimeExchangeReject");
    		calendarExReject = e.getCalendarTimeExchangeReject();
    	} catch (CHRejectException e) {
    		setTestStepBegin("********** Got CHRejectException: " + e.getCHReject().getReasonForRejection() + " " + e.getCHReject().getFurtherInformation());
    		fail("******************* got CHRejectException");
    	} 
    	
    	setTestStepBegin("Check Calendar Time Exchange Reject");
		assertEquals("Result code (" + calendarExReject.getResultCode().toString() + ") is not (" + OM_G31R01.Enums.ResultCode.ProtocolError.toString() + ") as expected", OM_G31R01.Enums.ResultCode.ProtocolError,	calendarExReject.getResultCode());
    	setTestStepEnd();
    }
  
 }
