package com.ericsson.msran.test.grat.testhelpers;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.ericsson.abisco.clientlib.servers.CMDHAND.Enums.Band;
import com.ericsson.abisco.clientlib.servers.CMDHAND.Enums.MSBand;
import com.ericsson.commonlibrary.ecimcom.netconf.NetconfManagedObjectHandler;
import com.ericsson.commonlibrary.managedobjects.ManagedObject;
import com.ericsson.commonlibrary.managedobjects.ManagedObjectAttribute;
import com.ericsson.commonlibrary.managedobjects.ManagedObjectStructAttribute;
import com.ericsson.commonlibrary.resourcemanager.Rm;


public class TrxParamsForRu{
	private int assignedARFCN;
	private int frequencyBand;
	private int txPower = 40;
	private int noOfRxAntennas = 2;
	private int noOfTxAntennas = 1;
	private int arfcnToUse;
	private int bandArfcnMin;
	private int bandArfcnMax;
	private int trxArfcnMin;
	private int trxArfcnMax;
	private Band gsmBandToUse = Band.GSM900P;
	private MSBand msBandToUse = MSBand.GSM900P;
	private String TGBandString = "GSM900P";
	private StpConfigHelper stpConfigHelper = StpConfigHelper.getInstance();
	private Logger logger;
	private NetconfManagedObjectHandler moHandler;
	private static Map<String, Integer> frequencyBandCache = new HashMap<String,Integer>();
	
	public TrxParamsForRu(String fruMoLdn){
		logger = Logger.getLogger(TrxParamsForRu.class);
        moHandler = Rm.getG2RbsList().get(0).getManagedObjectHandler();
		
		
		assignedARFCN = getAssignedArfcn();
		frequencyBand = getRuBand(fruMoLdn);
//		{{  0,    1,  124,    0,    0,    0,    0,  890000, 45000,  890200,  914800,  935200,  959800 },
//		{  2,  512,  810,  512,    0,    0,    0, 1850200, 80000, 1850200, 1909800, 1930200, 1989800 },
//		{  3,  512,  885,  512,    0,    0,    0, 1710200, 95000, 1710200, 1784800, 1805200, 1879800 },
//		{  5,  128,  251,  128,    0,    0,    0,  824200, 45000,  824200,  848800,  869200,  893800 },
//		{  8,    0,  124,    0,  975, 1023, 1024,  890000, 45000,  880200,  914800,  925200,  959800 }};
		
		if      (frequencyBand == 0){
			bandArfcnMin = 1;
			bandArfcnMax = 124;
			gsmBandToUse = Band.GSM900P;
			msBandToUse = MSBand.GSM900P;
			TGBandString = "GSM900P";
		} 
		else if (frequencyBand == 2){
			bandArfcnMin = 512;
			bandArfcnMax = 810;
			gsmBandToUse = Band.GSM1900;
			msBandToUse = MSBand.GSM1900;
			TGBandString = "GSM1900";
		} 
		else if (frequencyBand == 3){
			bandArfcnMin = 512;
			bandArfcnMax = 885;
			gsmBandToUse = Band.GSM1800;
			msBandToUse = MSBand.GSM1800;
			TGBandString = "GSM1800";
		} 
		else if (frequencyBand == 5){
			bandArfcnMin = 128;
			bandArfcnMax = 251;
			gsmBandToUse = Band.GSM800;
			msBandToUse = MSBand.GSM800;
			TGBandString = "GSM800";
		} 
		else if (frequencyBand == 8){
			bandArfcnMin = 0;
			bandArfcnMax = 1023;
			if (assignedARFCN == -1)
				bandArfcnMax = 124;
			else if (assignedARFCN  < 975)
				bandArfcnMax = 124;
			else if (assignedARFCN >= 975)
				bandArfcnMin = 975;
			gsmBandToUse = Band.GSM900E;
			msBandToUse = MSBand.GSM900E;
			TGBandString = "GSM900E";
		} 
		if (assignedARFCN > -1){
			logger.info("Assigned ARFCN " + assignedARFCN + " found for this STP.");
			arfcnToUse = assignedARFCN;
		}
		else{
			logger.warn("No assigned ARFCN found for this STP, using center of RU band.");
			// Maybe a random value within supported band would be better instead of center?
			arfcnToUse = getCenterArfcn();
		}
		// RU supports max BW of 100 ARFCN's, so set trxArfcn limits to (25 + arfcnToUse + 75)
		// Assymetrical since we currently think we allocate ARFCNs for several TRX:es to higher ARFCNs
		trxArfcnMin = arfcnToUse - 25;
		trxArfcnMax = arfcnToUse + 75;
		if (trxArfcnMin < bandArfcnMin){
			trxArfcnMin = bandArfcnMin;
			trxArfcnMax = trxArfcnMin + 100;
			if (trxArfcnMax > bandArfcnMax)
				trxArfcnMax = bandArfcnMax;
		}
		else if (trxArfcnMax > bandArfcnMax){
			trxArfcnMax = bandArfcnMax;
			trxArfcnMin = trxArfcnMax - 100;
			if (trxArfcnMin < bandArfcnMin)
				trxArfcnMin = bandArfcnMin;
		}
		
		logger.info("bandArfcnMin=" + bandArfcnMin + ", bandArfcnMax=" + bandArfcnMax + ", assignedARFCN=" + assignedARFCN + ", arfcnToUse=" + arfcnToUse + ", trxArfcnMin=" + trxArfcnMin + ", trxArfcnMax=" + trxArfcnMax);
	}
	public MSBand getMsBandToUse() {           
        return msBandToUse;
    }
    public int getFrequencyBand(){
		return frequencyBand;
	}
	public int getTxPower(){
		return txPower;
	}
	public int getNoOfRxAntennas(){
		return noOfRxAntennas;
	}
	public int getNoOfTxAntennas(){
		return noOfTxAntennas;
	}
	public int getArfcnMin(){
		return trxArfcnMin;
	}
	public int getArfcnMax(){
		return trxArfcnMax;
	}
	public int getArfcnToUse(){
		return arfcnToUse;
	}
	
	public Band getGsmBandToUse() {
	    return gsmBandToUse;
	}

	public String getTgBandToUse() {
	    return TGBandString;
	}

	private int getCenterArfcn(){
		return bandArfcnMin + (int)((bandArfcnMax-bandArfcnMin) / 2) ;
	}

	private int getAssignedArfcn(){
	    return stpConfigHelper.getAssignedArfcn();
	}

	private int getRuBand(String fruMoLdn) {
		Integer fBand = frequencyBandCache.get(fruMoLdn);
		if (fBand != null) {
			// cache hit!
			return fBand.intValue();
		}
		
		// not in cache, get it from the FRU mo
		moHandler.connect();
		ManagedObject mo = moHandler.getManagedObject(fruMoLdn);
		ManagedObjectStructAttribute ruProdData = (ManagedObjectStructAttribute) mo.getAttribute("productData");
		ManagedObjectAttribute ruProductNameAttr = ruProdData.getMember("productName");
		String ruProductName = ruProductNameAttr.getValue();
		logger.info("RU productData = " + ruProductName);
		Pattern pattern = Pattern.compile("^RUS\\s*\\d+\\s*B(\\d+)"); // "RUS 01 B5"
		Matcher matcher = pattern.matcher(ruProductName);
		matcher.find();
		int band = Integer.parseInt(matcher.group(1));
		logger.info("RU band = " + band);
		// store the mapping in the cache for next time
		frequencyBandCache.put(fruMoLdn, band);
		return band;
	}
}