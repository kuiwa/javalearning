package com.ericsson.msran.test.grat.testhelpers;

import java.util.HashMap;
import java.util.Map;

import com.ericsson.commonlibrary.resourcemanager.Rm;
import com.ericsson.commonlibrary.resourcemanager.g2.G2Rbs;

public class StpConfigHelper {
    private G2Rbs rbs;
    String rbsName;
    
    private static final Map<String, String> lmtIpMap = new HashMap<String, String>();
    private static final Map<String, String> altPgwIpMap = new HashMap<String, String>();
    private static final Map<String, Integer> assignedArfcnMap = new HashMap<String, Integer>();
    private static final Map<String, Integer> assignedArfcnMapTg1 = new HashMap<String, Integer>();
    private static final Map<String, Integer> assignedArfcnMapTg2 = new HashMap<String, Integer>();
    private static final Map<String, Integer> mssimCellConnectedMap = new HashMap<String, Integer>();

    private static StpConfigHelper instance = null;
    
    public static StpConfigHelper getInstance() {
    	if(instance == null) {
    		StpConfigHelper tmp = new StpConfigHelper();
    		instance = tmp;
    		tmp = null;
    	}
    	return instance;
    }
    
    private StpConfigHelper() {
        rbs = Rm.getG2RbsList().get(0);
        rbsName = rbs.getName().toLowerCase();
        
        // LMT-IP address
        //GRAT-STP
        lmtIpMap.put("rbs719-gwl7", "10.68.110.40");
        lmtIpMap.put("rbs719-gwl8", "10.68.110.41");
        lmtIpMap.put("ki_rbs1231-gwl1", "10.68.110.77");
        lmtIpMap.put("ki_rbs1231-s2", "10.68.110.78");
        lmtIpMap.put("ki_rbs1231-s3", "10.68.110.79");
        lmtIpMap.put("ki_rbs1231-gwl4", "10.68.110.80");
        lmtIpMap.put("ki_rbs1231-gwl5", "10.68.110.94");
        lmtIpMap.put("rbs1274-gwl3", "10.68.110.135");
        lmtIpMap.put("rbs1285-gwl2", "10.68.110.177");
        lmtIpMap.put("rbs1285-gwl4", "10.68.110.178");
        lmtIpMap.put("rbs1157-gwl2", "10.68.110.182");
        lmtIpMap.put("rbs1157-gwl5", "10.68.110.183");
        lmtIpMap.put("wrbs745", "137.58.191.168");
        lmtIpMap.put("wrbs646", "137.58.191.164");
        lmtIpMap.put("wrbs651", "137.58.191.181");
        lmtIpMap.put("wrbs746", "137.58.191.172");
        lmtIpMap.put("seliitrbs02468_node", "10.198.10.68");
        lmtIpMap.put("seliitrbs02469_node", "10.198.10.69");
        lmtIpMap.put("seliitrbs02492_node", "10.198.10.8");
        lmtIpMap.put("seliitrbs04031_node", "10.198.10.11");
        //G2-CI
        lmtIpMap.put("rbs1192-gwl1", "10.68.110.73");
        lmtIpMap.put("rbs1192-gwl2", "10.68.110.74");
        lmtIpMap.put("rbs1192-gwl3", "10.68.110.75");
        lmtIpMap.put("rbs1192-gwl4", "10.68.110.76");            
        lmtIpMap.put("rbs1138-gwl5", "10.68.110.25");
        lmtIpMap.put("rbs1175-gwl1", "10.68.110.27");
        lmtIpMap.put("rbs1138-gwl3", "10.68.110.23");
        lmtIpMap.put("rbs650-gwl5", "10.68.110.47");
        lmtIpMap.put("rbs650-gwl11", "10.68.110.49");
        lmtIpMap.put("rbs556-gwl1", "10.68.110.145");
        lmtIpMap.put("seliitrbs02497_node", "10.198.10.5");
        lmtIpMap.put("seliitrbs04043_node", "10.198.10.12");
        lmtIpMap.put("seliitrbs04032_node", "10.198.10.13");
        lmtIpMap.put("seliitrbs09876_node", "10.167.228.160");
    
        // Alternative BSC_BROKER_ADDRESS to test redirect
        //GRAT-STP
        altPgwIpMap.put("rbs719-gwl7", "10.86.160.155");
        altPgwIpMap.put("rbs719-gwl8", "10.86.160.156");
        altPgwIpMap.put("ki_rbs1231-gwl1", "10.86.160.157");
        altPgwIpMap.put("ki_rbs1231-s2", "10.86.160.158");
        altPgwIpMap.put("ki_rbs1231-s3", "10.86.160.159");
        altPgwIpMap.put("ki_rbs1231-gwl4", "10.86.160.160");
        altPgwIpMap.put("ki_rbs1231-gwl5", "10.86.160.161");
        altPgwIpMap.put("rbs1274-gwl3", "10.86.160.162");
        altPgwIpMap.put("rbs1285-gwl2", "10.86.160.163");
        altPgwIpMap.put("rbs1285-gwl4", "10.86.160.164");
        altPgwIpMap.put("rbs1157-gwl2", "10.86.160.165");
        altPgwIpMap.put("rbs1157-gwl5", "10.86.160.166");
        altPgwIpMap.put("wrbs745", "");
        altPgwIpMap.put("wrbs646", "");
        altPgwIpMap.put("wrbs651", "");
        altPgwIpMap.put("wrbs746", "");
        altPgwIpMap.put("seliitrbs02468_node", "");
        altPgwIpMap.put("seliitrbs02469_node", "");
        altPgwIpMap.put("seliitrbs02492_node", "");
        altPgwIpMap.put("seliitrbs04031_node", "");
        //G2-CI
        altPgwIpMap.put("rbs1192-gwl1", "10.86.160.167");
        altPgwIpMap.put("rbs1192-gwl2", "10.86.160.168");
        altPgwIpMap.put("rbs1192-gwl3", "10.86.160.169");
        altPgwIpMap.put("rbs1192-gwl4", "10.86.160.170");            
        altPgwIpMap.put("rbs1138-gwl5", "10.86.160.171");
        altPgwIpMap.put("rbs1175-gwl1", "10.86.160.172");
        altPgwIpMap.put("rbs1138-gwl3", "10.86.160.173");
        altPgwIpMap.put("rbs650-gwl5", "10.86.160.174");
        altPgwIpMap.put("rbs650-gwl11", "10.86.160.175");
        altPgwIpMap.put("rbs556-gwl1", "10.86.160.176");
        altPgwIpMap.put("seliitrbs02497_node", "");
        altPgwIpMap.put("seliitrbs04043_node", "");
        altPgwIpMap.put("seliitrbs04032_node", "");
        altPgwIpMap.put("seliitrbs09876_node", "");
        
        // Assigned ARFCN for this STP to use
        // According to:
        // https://ericoll.internal.ericsson.com/sites/RBS-Common-Software-Integration-and-Build/Documents/Teams/Toolsteam/Lab/KI_PDU_LTE_and_MSRBS_NWplan.doc
        //GRAT-STP
        assignedArfcnMap.put("rbs719-gwl7", 170);
        assignedArfcnMap.put("rbs719-gwl8", 168);
        assignedArfcnMap.put("ki_rbs1231-gwl1", 516);
        assignedArfcnMap.put("ki_rbs1231-s2", 50);
        assignedArfcnMap.put("ki_rbs1231-s3", 1008);
        assignedArfcnMap.put("ki_rbs1231-gwl4", 1012);
        assignedArfcnMap.put("ki_rbs1231-gwl5", 1020);
        assignedArfcnMap.put("rbs1274-gwl3", 115);
        assignedArfcnMap.put("rbs1285-gwl2", 107);
        assignedArfcnMap.put("rbs1285-gwl4", 117);
        assignedArfcnMap.put("rbs1157-gwl2", -1);
        assignedArfcnMap.put("rbs1157-gwl5", -1);
        assignedArfcnMap.put("wrbs745", 1004);
        assignedArfcnMap.put("wrbs646", 1006);
        assignedArfcnMap.put("wrbs651", 512);
        assignedArfcnMap.put("wrbs746", 514);
        assignedArfcnMap.put("seliitrbs02468_node", 999);
        assignedArfcnMap.put("seliitrbs02469_node", 999);
        assignedArfcnMap.put("seliitrbs02492_node", 107);
        assignedArfcnMap.put("seliitrbs04031_node", 979);
        //G2-CI
        assignedArfcnMap.put("rbs1192-gwl1", -1);
        assignedArfcnMap.put("rbs1192-gwl2", -1);
        assignedArfcnMap.put("rbs1192-gwl3", 992);
        assignedArfcnMap.put("rbs1192-gwl4", 996);            
        assignedArfcnMap.put("rbs1138-gwl5", -1);
        assignedArfcnMap.put("rbs1175-gwl1", -1);
        assignedArfcnMap.put("rbs1138-gwl3", -1);
        assignedArfcnMap.put("rbs650-gwl5", -1);
        assignedArfcnMap.put("rbs650-gwl11", -1);
        assignedArfcnMap.put("rbs556-gwl1", -1);
        assignedArfcnMap.put("seliitrbs02497_node", 977);
        assignedArfcnMap.put("seliitrbs04043_node", 975);
        assignedArfcnMapTg1.put("seliitrbs04043_node", 991);
        assignedArfcnMapTg2.put("seliitrbs04043_node", 1007);
        assignedArfcnMap.put("seliitrbs04032_node", 981);
        assignedArfcnMap.put("seliitrbs09876_node", 107);
        
        // Cellnumber of connected MS-SIM to use
        //GRAT-STP
        mssimCellConnectedMap.put("rbs719-gwl7", -1);
        mssimCellConnectedMap.put("rbs719-gwl8", -1);
        mssimCellConnectedMap.put("ki_rbs1231-gwl1", 0);
        mssimCellConnectedMap.put("ki_rbs1231-s2", 0);
        mssimCellConnectedMap.put("ki_rbs1231-s3", -1);
        mssimCellConnectedMap.put("ki_rbs1231-gwl4", -1);
        mssimCellConnectedMap.put("ki_rbs1231-gwl5", -1);
        mssimCellConnectedMap.put("rbs1274-gwl3", 2);
        mssimCellConnectedMap.put("rbs1285-gwl2", 0);
        mssimCellConnectedMap.put("rbs1285-gwl4", 3);
        mssimCellConnectedMap.put("rbs1157-gwl2", -1);
        mssimCellConnectedMap.put("rbs1157-gwl5", -1);
        mssimCellConnectedMap.put("wrbs745", 0);
        mssimCellConnectedMap.put("wrbs646", 1);
        mssimCellConnectedMap.put("wrbs651", 2);
        mssimCellConnectedMap.put("wrbs746", 3);
        mssimCellConnectedMap.put("seliitrbs02468_node", 1);
        mssimCellConnectedMap.put("seliitrbs02469_node", 1);
        mssimCellConnectedMap.put("seliitrbs02492_node", 0);
        mssimCellConnectedMap.put("seliitrbs04031_node", 0);
        //G2-CI
        mssimCellConnectedMap.put("rbs1192-gwl1", -1);
        mssimCellConnectedMap.put("rbs1192-gwl2", -1);
        mssimCellConnectedMap.put("rbs1192-gwl3", 1);
        mssimCellConnectedMap.put("rbs1192-gwl4", 0);            
        mssimCellConnectedMap.put("rbs1138-gwl5", -1);
        mssimCellConnectedMap.put("rbs1175-gwl1", -1);
        mssimCellConnectedMap.put("rbs1138-gwl3", -1);
        mssimCellConnectedMap.put("rbs650-gwl5", -1);
        mssimCellConnectedMap.put("rbs650-gwl11", -1);
        mssimCellConnectedMap.put("rbs556-gwl1", -1);
        mssimCellConnectedMap.put("seliitrbs02497_node", 0);
        mssimCellConnectedMap.put("seliitrbs04043_node", 0);
        mssimCellConnectedMap.put("seliitrbs04032_node", 0);
        mssimCellConnectedMap.put("seliitrbs09876_node", 0);
    }
    
    public String getLmtIpAddress(){
        return lmtIpMap.get(rbsName);
    }
    
    public String getAlternativePgwIpAddress(){
        return altPgwIpMap.get(rbsName);
    }
    
    public Integer getAssignedArfcn(){
        return assignedArfcnMap.get(rbsName);
    }
    
    public Integer getAssignedArfcnTg(int tgId){
    	switch (tgId) {
    	case 0:
    		return assignedArfcnMap.get(rbsName);
    	case 1:
    		return assignedArfcnMapTg1.get(rbsName);
    	case 2:
    		return assignedArfcnMapTg2.get(rbsName);
    		
    	default: 
    		return -1;
    	}
    }
    
    public Integer getMssimCell(){
        return  mssimCellConnectedMap.get(rbsName);
    }
    
}