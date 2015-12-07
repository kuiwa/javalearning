package com.ericsson.msran.test.grat.testhelpers;

import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import com.ericsson.commonlibrary.resourcemanager.Rm;
import com.ericsson.commonlibrary.resourcemanager.g2.G2Rbs;
import com.ericsson.abisco.clientlib.servers.PARAMDISP.SIMCardSubStruct;


/**
 * @name AlarmHelper
 * 
 * @author Nikos Karassavas (enikoka)
 * 
 * @created 2015-02-23
 * 
 * @description This class contains method for retrieving SIMCardSubStruct for the STP
 *              we currently use. 
 */
public class ImsiHelper {

	private static Logger logger = Logger.getLogger(AlarmHelper.class);
    
    private static G2Rbs rbs;
    private static SIMCardSubStruct simCardSubStruct;
    
    private static List<Integer> getImsiAsIntegerList(int index){
    	rbs = Rm.getG2RbsList().get(0);
		String stpName = rbs.getName().toLowerCase();
		logger.debug("STP:"+stpName);
		
    	if      (stpName.equals("ki_rbs1231-s2") || stpName.equals("wrbs745"))
    		return Arrays.asList(8,0,0,8,8,8,8,0,0,0,1,8,2,0,0);
    	else if (stpName.equals("ki_rbs1231-gwl5") || stpName.equals("rbs1192-gwl3")|| stpName.equals("wrbs651")){
    		List<Integer> intList = Arrays.asList(2,4,0,9,9,9,9,0,0,0,0,1,4,6,7);;
    		switch (index) {
    		case 1: intList = Arrays.asList(8,0,0,8,8,8,8,0,0,0,1,8,2,0,1);
    				break;
    		case 2: intList =  Arrays.asList(2,4,0,9,9,9,9,0,0,0,0,1,4,6,7);
    				break;
    		}
    		return intList;
    	}
    		//return Arrays.asList(8,0,0,8,8,8,8,0,0,0,1,8,2,0,1);
    	else
    		return Arrays.asList(2,4,0,9,9,9,9,0,0,0,0,1,4,6,7); // Our TEMS SIM card's IMSI
    }
    
    public static SIMCardSubStruct getSIMCardSubStruct() {
    	return getSIMCardSubStruct(1);
    }
	public static SIMCardSubStruct getSIMCardSubStruct(int index)
	{	
		simCardSubStruct = new SIMCardSubStruct(
				getImsiAsIntegerList(index),                       // IMSI
    			Arrays.asList(0,0,0,0),                       // TMSI
    			Arrays.asList(37,30,93,2,182,27,164,0));      // KC
		simCardSubStruct.setRAND(
    			Arrays.asList(0,1,2,4,8,16,32,64,128,1,3,7,15,31,63,127)); // RAND
		
		return simCardSubStruct;
	}	
	
}
