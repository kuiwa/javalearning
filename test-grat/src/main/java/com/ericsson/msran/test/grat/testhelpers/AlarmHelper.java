package com.ericsson.msran.test.grat.testhelpers;

import java.util.List;

import org.apache.log4j.Logger;

import com.ericsson.commonlibrary.resourcemanager.Rm;
import com.ericsson.commonlibrary.resourcemanager.g2.G2Rbs;
import com.ericsson.commonlibrary.faultmanagement.com.ComAlarm;
import com.ericsson.commonlibrary.faultmanagement.ComFaultManagementHandler;
import com.ericsson.msran.helpers.Helpers;

/**
 * @name AlarmHelper
 * 
 * @author Huan XIE (ehunxie)
 * 
 * @created 2014-12-04
 * 
 * @description This class contains methods for retrieving active alarms in grat
 *              rbs and also for compare expected alarm within the retrieved
 *              active alarm list
 */
public class AlarmHelper {

	private static Logger logger = Logger.getLogger(AlarmHelper.class);
	private static final int bsc_disconn_majorType = 193;
	private static final int bsc_disconn_minorType = 9175139;
    private static final int configuration_requires_feature_activation_majorType = 193;
    private static final int configuration_requires_feature_activation_minorType = 9175042;

	private MomHelper momHelper;

	private static ComFaultManagementHandler faultManagementHandler = null;

	public AlarmHelper() {
		// enable snmp
		momHelper = new MomHelper();
		momHelper.enableSnmp();		
		
		if (null == faultManagementHandler) {
			
			G2Rbs rbs = Rm.getG2RbsList().get(0);
			faultManagementHandler = rbs.getFaultManagementHandler();
			
			// call this to let netconfClient connected
			getAlarmList();
		}
	}

	/**
	 * Returns a List of ComAlarm objects which represent the current active
	 * alarm list in the G2Rbs contained in the STC.
	 * <p>
	 * This method will ignore any exceptions thrown by FaultManagementHandler
	 * and retry for the specified number of seconds. If the alarm list is not
	 * retrieved within this time, the exception thrown by the
	 * FaultManagementHandler is re-thrown out of this method.
	 * 
	 * @param timeoutdInSeconds
	 *            Timeout after which this method will stop retrying to retrieve
	 *            the active alarm list
	 * @return List of ComAlarm objects which represent the current active alarm
	 *         list in the G2RbsAsNodeB
	 */
	public List<ComAlarm> getAlarmList(long timeoutInSeconds) {
		long startTime = System.currentTimeMillis();
		long secondsSinceStart = 0;
		List<ComAlarm> alarmList = null;
		logger.info("Begin to get active alarm list, startTime:" + startTime);
		int i = 0;
		while (secondsSinceStart < timeoutInSeconds) {
			try {
				i++;
				alarmList = faultManagementHandler.getActiveAlarmList();
				if (null != alarmList && !alarmList.isEmpty()) {
					logger.debug("Alarm list is retrieved after "
							+ secondsSinceStart + "s, round:" + i);
					logger.trace("Alarm currentTime:"
							+ System.currentTimeMillis());
					return alarmList;
				}

				logger.trace("Alarm list is empty, wait a while and retry again. currentTime:"
						+ System.currentTimeMillis() + ", round:" + i);
				Helpers.util().timeHelper().sleepSeconds(10);
				secondsSinceStart = (System.currentTimeMillis() - startTime) / 1000;
			} catch (Exception e) {
				logger.trace("Alarm exeception, time:"
						+ System.currentTimeMillis() + ", round:" + i
						+ ", exception:" + e.getMessage());
				Helpers.util().timeHelper().sleepSeconds(10);
				secondsSinceStart = (System.currentTimeMillis() - startTime) / 1000;
				continue;
			}
		}

		logger.debug("No active alarm is retrieved after " + secondsSinceStart
				+ "s.");
		return alarmList;
	}

	public List<ComAlarm> getAlarmList() {
		long startTime = System.currentTimeMillis();
		List<ComAlarm> alarmList = null;
		logger.info("Begin to get active alarm list, startTime:" + startTime);
		try {
			alarmList = faultManagementHandler.getActiveAlarmList();
			return alarmList;
		} catch (Exception e) {
			logger.info(e.getStackTrace());
		} 
	    return alarmList;
		
	}

	public boolean isBscDisconnectAlarm(List<ComAlarm> alarmList,
			String managedObj) {
	    return isAlarmPresent(alarmList, bsc_disconn_majorType, bsc_disconn_minorType, managedObj);
	}
	
	public boolean isFeatureActivationAlarmRaised(String moLdn) {
	    try {
	        Thread.sleep(2000);
	    } catch (Exception e) {}
	    List<ComAlarm> alarmList = getAlarmList(5);
	    return isAlarmPresent(alarmList, configuration_requires_feature_activation_majorType, configuration_requires_feature_activation_minorType, moLdn);
	}
	
	public boolean isAlarmPresent(List<ComAlarm> alarmList, int alarmMajorType, int alarmMinorType, String managedObj) {
        if (alarmList == null || alarmList.isEmpty()) {  
            logger.info("alarm list is empty");
            return false;
        }

        for (ComAlarm alarm : alarmList) {
            logger.trace("alarm: major:" + alarm.getMajorType() + ", minor:"
                    + alarm.getMinorType() + ", eventType:"
                    + alarm.getEventType() + ", mo:" + alarm.getManagedObject());
            if (alarmMajorType == alarm.getMajorType()
                    && alarmMinorType == alarm.getMinorType()
                    && managedObj.equals(alarm.getManagedObject())) {
                return true;
            }
        }
        return false;
	}
}
