package com.ericsson.msran.test.grat.configsv;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.ericsson.msran.jcat.TestBase;
import com.ericsson.abisco.clientlib.servers.LTS.Report;
import com.ericsson.msran.test.grat.testhelpers.AbiscoConnection;
import com.ericsson.commonlibrary.resourcemanager.helpers.Log;

class GetReportThread extends Thread {

    private final long interval;
    private final long duration;
    private final AbiscoConnection abisco;
    private final TestBase testBase;
    private final String callSetupRate;
    private final String smsSetupRate;
    private final int numOfTGs;

    /**
     * Instantiates a GetReportThread
     * 
     * @param interval print report according with interval(seconds)
     * @param duration test duration(seconds)
     * @param abisco get report from
     */
    public GetReportThread(long interval, long duration, AbiscoConnection abisco, TestBase testBase,
            String callSetupRate, String smsSetupRate, int numOfTGs) {

        this.interval = interval * 1000;
        this.duration = duration * 1000;
        this.abisco = abisco;
        this.testBase = testBase;
        this.callSetupRate = callSetupRate;
        this.smsSetupRate = smsSetupRate;
        this.numOfTGs = numOfTGs;
    }

    @Override
    public void run() {
        Report report = null;
        Date date = new Date();
        long beginTime = date.getTime();
        long fixedBeginTime = beginTime;
        long times = duration / interval;
        Log.debug("interval=" + interval + ",duration =" + duration + " times=" + times);
        try {
            Thread.sleep(interval);
        } catch (InterruptedException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        Log.debug("date.getTime()=" + date.getTime() + ",fixedBeginTime =" + fixedBeginTime + " duration=" + duration);
        while (date.getTime()< (fixedBeginTime+duration)) {
            try {
                date = new Date();
                if (endTimeExceedDeltaThanBeginTime(date.getTime(), beginTime, interval)) {
                    beginTime = date.getTime();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                    Log.debug("Begin to get report at " + sdf.format(beginTime));
                    for (int tgId = 0; tgId < numOfTGs; tgId++) {
                    try {
                        report = abisco.getReportLTS(tgId);
                    } catch (InterruptedException e) {
                        // Did not work to reset the LTS.
                        e.printStackTrace();
                    }
                    if (report != null) {
                        // Calculate some statistics
                        int totalNumberOfSuccessfulCalls = (int) (report.getFRCallSuccTot() + report
                                .getEFRCallSuccTot());
                        int totalNumberOfFailedCalls = (int) (report.getFRCallFailsTot() + report.getEFRCallFailsTot());
                        int totalNumberOfCalls = (int) (report.getFRCallsTot() + report.getEFRCallsTot());
                        int timeInSeconds = (int) (report.getTotalTime() / 100) + 1; // At least on second 
                        double csr = totalNumberOfSuccessfulCalls / (double) timeInSeconds;
                        double cfr = totalNumberOfFailedCalls / (double) timeInSeconds;
                        double ssr = report.getSMSSuccTot() / (double) timeInSeconds;
                        double sfr = report.getSMSFailsTot() / (double) timeInSeconds;

                        // Write summary info report
                        testBase.setAdditionalResultInfo("<br><b>GRAT Stability load test summary at: </b>"+sdf.format(beginTime));
                        testBase.setAdditionalResultInfo("Test duration: " + timeInSeconds + " s");
                        
                        testBase.setAdditionalResultInfo("tgId = " + tgId + " :Call Setup Rate: "
                                    + Integer.parseInt(callSetupRate.split(",")[tgId]) / (double) 1000 + " call/s");
                        testBase.setAdditionalResultInfo("tgId = " + tgId + " :SMS Setup Rate: "
                                    + Integer.parseInt(smsSetupRate.split(",")[tgId]) / (double) 1000 + " sms/s<br>");
                        
                        testBase.setAdditionalResultInfo("Call Success Rate (CSR): " + csr);
                        testBase.setAdditionalResultInfo("Call Failure Rate (CFR): " + cfr);
                        testBase.setAdditionalResultInfo("SMS Success Rate  (SSR): " + ssr);
                        testBase.setAdditionalResultInfo("SMS Failure Rate  (SFR): " + sfr);
                        testBase.setAdditionalResultInfo("Successful Call Ratio is: " + (((double)totalNumberOfSuccessfulCalls)/totalNumberOfCalls)*100 + "%");
                        testBase.setAdditionalResultInfo("Successful SMS Ratio is: " + (((double)report.getSMSSuccTot())/((int)report.getSMSesTot()))*100 + "%");

                        testBase.setAdditionalResultInfo("<b>GRAT Stability load test details:</b>");
                        testBase.setAdditionalResultInfo("########### FR&EFR calls ###########");
                        testBase.setAdditionalResultInfo("Successful FR calls: " + (int) report.getFRCallSuccTot());
                        testBase.setAdditionalResultInfo("Successful EFR calls: " + (int) report.getEFRCallSuccTot());
                        testBase.setAdditionalResultInfo("Successful total calls: " + totalNumberOfSuccessfulCalls);
                        testBase.setAdditionalResultInfo("Failed FR calls: " + (int) report.getFRCallFailsTot());
                        testBase.setAdditionalResultInfo("Failed EFR calls: " + (int) report.getEFRCallFailsTot());
                        testBase.setAdditionalResultInfo("Failed total calls: " + totalNumberOfFailedCalls);
                        testBase.setAdditionalResultInfo("Total calls: " + totalNumberOfCalls);
                        
                        testBase.setAdditionalResultInfo("########### SMS ###########");
                        testBase.setAdditionalResultInfo("Successful sent sms: " + (int) report.getSMSSuccTot());
                        testBase.setAdditionalResultInfo("Failed sms: " + (int) report.getSMSFailsTot());
                        testBase.setAdditionalResultInfo("Total sms: " + (int) (report.getSMSesTot()));
                        
                        testBase.setAdditionalResultInfo("########### CCCHLoad ###########");
                        
                        testBase.setAdditionalResultInfo("Total LRSAss: " + (int) (report.getLRSAssignsTot()));
                        
                        testBase.setAdditionalResultInfo("Total LRSAssRej: " + (int) (report.getLRSAssRejsTot()));
                        
                        testBase.setAdditionalResultInfo("Total Paging: " + (int) (report.getPagingsTot()));
                        
                        testBase.setAdditionalResultInfo("###############################");

                        testBase.setAdditionalResultInfo("TCH allocation ratio: " + report.getTCHAllocRatioTot() + " %");
                        testBase.setAdditionalResultInfo("SDCCH allocation ratio: " + report.getSDCCHAllocRatioTot()
                                + " %");
                        testBase.setAdditionalResultInfo("Total pagings: " + (int) report.getPagingsTot());
                        testBase.setAdditionalResultInfo("Total channel requests: " + (int) report.getChannelReqTot());
                    }
                    }
                    //Thread.sleep(5 * 1000);
                } else {
                    Log.debug("Not get report at " + date.getTime());
                    //Thread.sleep(5 * 1000);
                }
                Thread.sleep(interval);
                date = new Date();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    boolean endTimeExceedDeltaThanBeginTime(long endTime, long beginTime, long delta) {
        Log.debug("Check if endtime - begintime bigger than interval in" + beginTime + "endTime=" + endTime + "interval=" + delta);
        if (endTime - beginTime > delta)
        {
            return true;
        }
        return false;
    }

}
