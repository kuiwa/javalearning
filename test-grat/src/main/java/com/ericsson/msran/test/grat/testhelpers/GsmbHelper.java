package com.ericsson.msran.test.grat.testhelpers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;

import com.ericsson.abisco.clientlib.servers.CMDHAND.InfoCellResponse;
import com.ericsson.abisco.clientlib.servers.CMDHAND.InfoTGResponse;
import com.ericsson.abisco.clientlib.servers.CMDHAND.Enums.HoppingIndicator;
import com.ericsson.abisco.clientlib.servers.PARAMDISP.SIMCardSubStruct;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.Enums.ActivationType;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.Enums.AlgOrRate;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.Enums.ChannelRate;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.Enums.ChannelType;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.Enums.TypeOfCh;
import com.ericsson.commonlibrary.resourcemanager.Rm;
import com.ericsson.commonlibrary.resourcemanager.g2.G2Rbs;
import com.ericsson.commonlibrary.restorestack.RestoreCommandStack;
import com.ericsson.msran.helpers.Helpers;
import com.ericsson.msran.test.grat.testhelpers.restorecommands.MssimCloseCCCHCommand;
import com.ericsson.msran.test.grat.testhelpers.restorecommands.MssimCloseConnection;
import com.ericsson.msran.test.grat.testhelpers.MssimHelper;
import com.ericsson.mssim.gsmb.ChnComb;
import com.ericsson.mssim.gsmb.ChnMain;
import com.ericsson.mssim.gsmb.Confirmation;
import com.ericsson.mssim.gsmb.Gsmb;
import com.ericsson.mssim.gsmb.GsmbSrvCELL_INFO_1_ACK;
import com.ericsson.mssim.gsmb.GsmbSrvCELL_INFO_1_CMD;
import com.ericsson.mssim.gsmb.GsmbSrvCELL_INFO_ACK_Common;
import com.ericsson.mssim.gsmb.GsmbSrvCELL_INFO_CMD;
import com.ericsson.mssim.gsmb.GsmbSrvCELL_PPU_1_ACK;
import com.ericsson.mssim.gsmb.GsmbSrvCELL_PPU_1_CMD;
import com.ericsson.mssim.gsmb.GsmbSrvCONFIG_CMD;
import com.ericsson.mssim.gsmb.GsmbSrvLOGIN_CMD;
import com.ericsson.mssim.gsmb.GsmbSrvLOGIN_ZOT_CMD;
import com.ericsson.mssim.gsmb.GsmbSrvMS_CREATE_1_CMD;
import com.ericsson.mssim.gsmb.GsmbSrvMS_SET_MEAS_CMD;
import com.ericsson.mssim.gsmb.GsmbSrvSTART_CMD;
import com.ericsson.mssim.gsmb.GsmbSrvTRX_INFO_1_ACK;
import com.ericsson.mssim.gsmb.GsmbSrvTRX_INFO_1_CMD;
import com.ericsson.mssim.gsmb.GsmphMPH_CCCH_OPEN_REQ;
import com.ericsson.mssim.gsmb.GsmphMPH_CS_CHN_OPEN_REQ;
import com.ericsson.mssim.gsmb.Response;
import com.ericsson.mssim.gsmb.SdrType;
import com.ericsson.mssim.gsmb.FrequencyStructure.FrStructureType;
import com.ericsson.mssim.gsmb.GsmbSrvMS_CREATE_1_CMD.UsedStructureType;
import com.ericsson.mssim.gsmb.GsmbSrvMS_SET_MEAS_CMD.ChanBit;
import com.ericsson.mssim.gsmb.GsmbSrvTRX_INFO_1_ACK.GsmbSrvTRX_CHN;
import com.ericsson.mssim.gsmb.impl.GsmbFactory;
import com.ericsson.mssim.gsmb.impl.GsmbSrvMS_CREATE_1_CMDBuilder;
import com.ericsson.mssim.gsmb.packing.internal.PhErrorType;

public class GsmbHelper {
    private G2Rbs rbs;
    private Gsmb gsmb;
    private Logger logger;
    private RestoreCommandStack restoreStack;
    private MssimCloseConnection mssimCloseConCommand;
    private MssimHelper mssimHelper;
    private SdrType sdrType = null;
    
    public GsmbHelper(Gsmb gsmb) {
    	this.gsmb = gsmb;
    	logger = Logger.getLogger(GsmbHelper.class);
        rbs = Rm.getG2RbsList().get(0);
        restoreStack = Helpers.restore().restoreStackHelper().getRestoreStack();
        mssimCloseConCommand = new MssimCloseConnection(gsmb);
        mssimHelper = new MssimHelper(gsmb);
    }
    
    private String createLoginUserName(String testCaseName) {
        String stpName = rbs.getName().toLowerCase();
        String userName = testCaseName + "-" + stpName;
        // username can be at most 39 characters or login fails
        if (userName.length() > 39) {
            String trimmedTestCaseName = testCaseName.substring(0, 39 - 1 - stpName.length());
            userName = trimmedTestCaseName + "-" + stpName;
        }
        return userName;
    }
    
    public Response login (String testCaseName){
        String userName = createLoginUserName(testCaseName);
        logger.info("logging in as: " + userName);
        GsmbSrvLOGIN_CMD login = GsmbFactory.getGsmbSrvLOGIN_CMDBuilder(userName).build();
        return gsmb.send(login);               
    }
    
    public Response zotLogin (String testCaseName){
        String userName = createLoginUserName(testCaseName);
        logger.info("logging in as: " + userName);
        GsmbSrvLOGIN_ZOT_CMD login = GsmbFactory.getGsmbSrvLOGIN_ZOT_CMDBuilder(userName).build();
        return gsmb.send(login);               
    }
    /**
     * To connect, login, config and start the GSMB stack in mssim
     * @param testCaseName
     * @return boolean - True: mssim initiated successfully
     * 					 False: Something went wrong
     */
     public boolean mssimInit(String testCaseName){
     	return mssimInit(testCaseName, false);
     	
     }
     /**
      * To connect, login, config and start the GSMB stack in mssim
      * @param testCaseName
      * @param zotLogin
      * @return boolean - True: Initiate of mssim successfully
      * 					 False: Something went wrong
      */
     public boolean mssimInit(String testCaseName, boolean zotLogin){
     	
     	logger.info("Connecting to MSSIM");
         boolean connect = gsmb.connect();
         if (!connect){
     		logger.error("Connecting to MSSIM failed");
         	return false;
         }
         restoreStack.add(mssimCloseConCommand);
         
         if (zotLogin){
         	Response response = zotLogin(testCaseName);
         	if (!response.isSuccess()){
         		logger.error("Login to MSSIM failed");
             	return false;
             }
         }
         else{
         	Response response = login(testCaseName);
         	if (!response.isSuccess()){
         		logger.error("Login to MSSIM failed");
             	return false;
             }
         }
         
         GsmbSrvCONFIG_CMD config = GsmbFactory.getGsmbSrvCONFIG_CMDBuilder(new short[] {mssimHelper.getMssimCellToUse()}).maxMs(9).build();
         Response response = gsmb.send(config);
         if (!response.isSuccess()){
         	logger.error("GsmbSrvCONFIG_CMD response failed");
         	return false;
         }
         
         GsmbSrvSTART_CMD start = GsmbFactory.getGsmbSrvSTART_CMDBuilder().spare((short) 0).build();
         response = gsmb.send(start);
         if (!response.isSuccess()){
         	logger.error("GsmbSrvSTART_CMD response failed");
         	return false;
         }
         
         return true;
         
     }
     
     /**
     * To define a cell in MSSIM, and wait for the cell to be synchronized.
     * 				Default function, creates one Ms (in GsmbSrvMS_CREATE_1_CMD)
     * @return True/False - Returns true when cell is synchronized				
     */
     public boolean mssimDefineCell(){
    	 return mssimDefineCell(1);
     }
     /**
     * To define a cell in MSSIM, and wait for the cell to be synchronized.
     * @param numberOfMs - Number of Ms (to create create in GsmbSrvMS_CREATE_1_CMD)
     * @return True/False - Returns true when cell is synchronized				
     */
     public boolean mssimDefineCell(int numberOfMs){
     	 
         GsmbSrvMS_CREATE_1_CMDBuilder gsmbSrvMS_CREATE_1_CMDBuilder = GsmbFactory.getGsmbSrvMS_CREATE_1_CMDBuilder(
         		getMssimSdrType(), mssimHelper.getMssimCellToUse(),UsedStructureType.USR);

         SIMCardSubStruct sim = ImsiHelper.getSIMCardSubStruct();

         int[] intArray = ArrayUtils.toPrimitive(sim.getIMSI().toArray(new Integer[sim.getIMSI().size()]));
         short[] shortArray = new short[intArray.length];

         for (short i = 0; i < intArray.length; ++i)
         {
             shortArray[i] = (short) intArray[i];
         }
         
         for(int i=0; i<numberOfMs; ++i){
        	 GsmbSrvMS_CREATE_1_CMD ms_CREATE_1_CMD = gsmbSrvMS_CREATE_1_CMDBuilder
        			 .msId(i)
              	     .imsi(shortArray)
                     .build();
              Response response = gsmb.send(ms_CREATE_1_CMD);
              if (!response.isSuccess()){
              	logger.error("GsmbSrvMS_CREATE_1_CMD response failed");
              	return false;
              }
         }
         
         short mssimCell = mssimHelper.getMssimCellToUse();
         GsmphMPH_CCCH_OPEN_REQ open_REQ = GsmbFactory.getGsmphMPH_CCCH_OPEN_REQBuilder(mssimCell).build();
         Confirmation confirmation = gsmb.send(open_REQ);
         if (confirmation.getErrorType() != PhErrorType.GSM_PH_ENOERR){
         	logger.error("GsmphMPH_CCCH_OPEN_REQ confirmation error " + confirmation.getErrorType());
         	return false;
         }
         restoreStack.add(new MssimCloseCCCHCommand(gsmb, mssimCell));
                 
         return waitForMssimCellSync();
     }

     /**
     * Return SdrType, based on MSSIM version
     * @return SdrType
     * 				- SdrType.TYPE0_GSM2 	(version 1)
     * 				- SdrType.TYPE1_GSM16 	(version 3)
     */
     public SdrType getMssimSdrType(){
     	
     	if(sdrType == null){
     		
     		GsmbSrvCELL_PPU_1_CMD cellPpu1 = GsmbFactory.getGsmbSrvCELL_PPU_1_CMDBuilder().type(0).build();
             GsmbSrvCELL_PPU_1_ACK response = (GsmbSrvCELL_PPU_1_ACK) gsmb.send(cellPpu1);
             if (!response.isSuccess()){
             	logger.error("GsmbSrvCELL_PPU_1_CMD response failed");
             	return sdrType; //Null (User to take care of that)
             }
                         
             if (response.getInfo().get(0).getSdrMode() == 1){
             	sdrType = SdrType.TYPE0_GSM2; //MSSIM version 1
             	return sdrType;
             }
             else{
             	sdrType = SdrType.TYPE1_GSM16; //MSSIM version 3
             	return sdrType;
             }    		
     	}
     	else {
     		return sdrType;
     		
     	}
     }
     
     
     /**
     * Returns true when cell is synchronized, or false if timeout
     * @return True/False - Returns true when cell is synchronized
     */
     public boolean waitForMssimCellSync(){
     	
     	boolean isSync = false;
     	int tmp = -1;
     	long noSync = tmp & 0xffffffffl; // 4294967295
     	int count = 0;
     	
     	while (isSync == false){
     		
     		if (getMssimSdrType() == SdrType.TYPE1_GSM16){
     			//Should be updated to check "sync" attribute in response instead of "fn". (for TYPE1_GSM16)
     			GsmbSrvCELL_INFO_1_CMD cellInfo1 = GsmbFactory.getGsmbSrvCELL_INFO_1_CMDBuilder(mssimHelper.getMssimCellToUse()).build();
     			GsmbSrvCELL_INFO_1_ACK response = (GsmbSrvCELL_INFO_1_ACK) gsmb.send(cellInfo1);
     			
     			if (!response.isSuccess()){
     				logger.error("GsmbSrvCELL_INFO_1_CMD response failed");
                 	return false;
     			}
     			if (response.getFn() != noSync){
         			logger.info("Cell synchronized " + response.getFn());
         			isSync = true;
         		}
     			else {
         			logger.info("Cell not synchronized " + response.getFn());
         			
         			gsmb.delay(6);
         			count++;
         			logger.info("Waiting for Cell synchronization, retry number: " + count);
         			if (count == 25){
         				logger.info("Timed out while waiting for Cell synchronization");
         				return false;   				
         			}
         		}
     			
     		}
     		else if (getMssimSdrType() == SdrType.TYPE0_GSM2){
     			GsmbSrvCELL_INFO_CMD cellInfo = GsmbFactory.getGsmbSrvCELL_INFO_CMDBuilder(mssimHelper.getMssimCellToUse()).build();
         		GsmbSrvCELL_INFO_ACK_Common response = (GsmbSrvCELL_INFO_ACK_Common) gsmb.send(cellInfo);
         		
         		if (!response.isSuccess()){
                 	logger.error("GsmbSrvCELL_INFO_CMD response failed");
                 	return false;
                 }
         		if (response.getFn() != noSync){
         			logger.info("Cell synchronized " + response.getFn());
         			isSync = true;
         		}
         		else {
         			logger.info("Cell not synchronized " + response.getFn());
         			
         			gsmb.delay(6);
         			count++;
         			logger.info("Waiting for Cell synchronization, retry number: " + count);
         			if (count == 25){
         				logger.info("Timed out while waiting for Cell synchronization");
         				return false;   				
         			}
         		}
     		}
     		else{
     			logger.error("Failed to get MS-SIM Sdr Type");
     			return false;
     		}
     		
     	}
     	return true;
     }
     
    public void disconnect (){
         gsmb.disconnect();               
         restoreStack.remove(mssimCloseConCommand);
 	}
 	

    /**
     * Get the average radio frequency levels for the open channels.
     * Will wait until the values stabilize before returning
     * @param mssimCell - ID of the MsSim cell
     * @param trx - The MsSim TRX ID
     * @return Map, key=timeslot, value=average radio frequency level (1/256 dBm).
     *         Returns null when failed
     */
    public Map<Integer, Integer> getAverageRadioFrequencyLevels(int mssimCell, int trx) {
        Map<Integer, Integer> lastAvgRfls = new HashMap<>();
        for (int i = 0; i < 20; ++i) {
            GsmbSrvTRX_INFO_1_CMD trx_info_1_cmd = GsmbFactory.getGsmbSrvTRX_INFO_1_CMDBuilder(mssimCell, (short)trx).build();
            Response response = gsmb.send(trx_info_1_cmd);
            if (!response.isSuccess()) {
                logger.warn("GsmbSrvTRX_INFO_1_CMD failed, " + response.stringRepresentation());
                continue;
            }
            if (!(response instanceof GsmbSrvTRX_INFO_1_ACK)) {
                logger.warn("TRX_INFO_1_CMD did not get an ACK");
                continue;
            }

            GsmbSrvTRX_INFO_1_ACK trx_info_1_ack = (GsmbSrvTRX_INFO_1_ACK)response;
            logger.debug("GsmbSrvTRX_INFO_1_ACK cell = " + trx_info_1_ack.getCell() + ", trx = " + trx_info_1_ack.getTrx() + 
                            ", number of channels = " + trx_info_1_ack.getNumChn());
            if (trx_info_1_ack.getNumChn() == 0) {
                logger.warn("No open channels found");
            }
            
            boolean levelsAreStable = true;
            // Check that all channels are stable
            for (GsmbSrvTRX_CHN trxChn: trx_info_1_ack.getTrxChn()) {
                int ts = trxChn.getTs();
                int avgRfl = trxChn.getAvgRfl();
                logger.info("Ts = " + trxChn.getTs() +
                        ", Chan = " + trxChn.getChan() + 
                        ", AvgRfl = " + trxChn.getAvgRfl());
                if (lastAvgRfls.containsKey(ts) == false || Math.abs(avgRfl - lastAvgRfls.get(ts)) > 5) { // Check if level has stabilized
                    levelsAreStable = false;
                }
                lastAvgRfls.put(ts, avgRfl);
            }
            if (levelsAreStable) {
                // Done!
                return lastAvgRfls;
            }
            try {
                Thread.sleep(10000);
            } catch (Exception e) { logger.info("Sleep interrupted"); }
        }
        logger.warn("Did not get a stable radio frequency level");
        return null;
    }

    public enum ChannelKey {
      OUT_OF_BOUNDS("OUT_OF_BOUNDS", -1),	
	  BM("Bm", 1), 
	  BM_BI("Bm_BI", 2), 
	  BM_UNI("Bm_UNI", 3), 
	  LM("Lm", 4), 
	  SDCCH_4("Sdcch_4", 5),
	  SDCCH_8("Sdcch_8", 6),
	  PDCH_BTTI("Pdch_BTTI", 7),
	  PDCH_RTTI("Pdch_RTTI", 8);

	  private String name;
	  private int code;

	  private ChannelKey(String n, int c) 
	  {
		name = n;	 
		code = c;
	  }

	  public String getName()
	  {
	    return name;
	  }
	 
	  public int getCode() 
	  {
		return code;
	  }
	}		 

    public CHNParams mssimGetCHNParam(int tg, int trxc, short ts, ChannelType channeltype, ChannelRate chRate, TypeOfCh typeOfCh, AlgOrRate algOrRate, short amrCodec, String pdchMode, ActivationType chActType) {
	  ChnMain chnMain = ChnMain.NON_AMR_OR_PDTCH_CHANNEL;
	  ChnComb chnComb;
	  short tsc = -1; 
	  short narfcn; 
	  long fr = -1; 
	  int[] arfcnl;

	  short sub = getSubChannel(channeltype);
		
	  AbisHelper abisHelper;
	  abisHelper = new AbisHelper();
	  
	  ChannelKey chKey = getChannelKey(channeltype, chRate, pdchMode);

	  // Get Channel Combination
	  chnComb = getChnComb(chKey, chActType);
      
	  // Get Main channel
	  switch (typeOfCh) 
	  {
	    case SPEECH:
		   chnMain = getChnMainForSpeech(chKey, algOrRate, amrCodec);
           break;

	    case DATA: 
		   chnMain =getChnMainForData(chKey, algOrRate, amrCodec);
	       break;
	      
	    case SIGNALLING:        
		   chnMain = getChnMainForSignalling(chKey, algOrRate, amrCodec);
	       break;
	      
	    default:
	       logger.error("Incorrect type of channel");
	       break;
	  } 	  

	  HoppingIndicator hoppInd = HoppingIndicator.OUT_OF_BOUNDS;
	  List<Integer> arfcnList = new ArrayList<Integer>();
	  int arfcn = -1;
	  int maio = 0;
	  int bcchTrx = -1;
	  int bcchNo = -1;
	  int hsn = -1;

	  InfoTGResponse cmdInfoTgRsp = abisHelper.sendInfoTG(tg);
	  List<Integer> cellList = cmdInfoTgRsp.getCellList();	  
	  
	  boolean found = false;
	  for(int cell : cellList)
	  {
	    InfoCellResponse cmdInfoCellRsp = abisHelper.sendInfoCell(tg, cell);
	    hoppInd = cmdInfoCellRsp.getHoppingIndicator();
	    List<Integer> trxList = cmdInfoCellRsp.getTRXList();
	    arfcnList = cmdInfoCellRsp.getARFCNList();
	    bcchTrx = cmdInfoCellRsp.getBCCHtrx();
	    bcchNo = cmdInfoCellRsp.getBCCHno();
	    hsn = cmdInfoCellRsp.getHSN();
	    tsc = (short) cmdInfoCellRsp.getTSC();

	    maio = 0;
	    for(int trx : trxList)
	    {
	      arfcn = arfcnList.get(maio); // MAIO is normally same as TRX index in the cell
	      if (trxc == trx)
	      {
	        found = true;
	        break;
	      }
	      else maio++;
	    }
	    if (found) break;
	  }
      
	  if(!found) 
	  {
		logger.error("Trx not found" + trxc); 
		return null; // or raise a exception as    runtimeException::IllegalArgumentException which Thrown to indicate that a method has been passed an illegal or inappropriate argument.
	  }

      if(hoppInd.equals(HoppingIndicator.OUT_OF_BOUNDS))
      {
  		logger.error("HoppingIndicator was wrong"); 
  		return null;  // or raise a exception as    runtimeException::IllegalArgumentException which Thrown to indicate that a method has been passed an illegal or inappropriate argument. 	  
      }

	  //Get parameters for Hopping off
	  if (hoppInd.equals(HoppingIndicator.Off))
	  {
	    fr = arfcn;
	    arfcnList.clear();
	  }
	  else // Get parameters for Hopping
	  {
	    if (hoppInd.equals(HoppingIndicator.SYNTH)
	        || (hoppInd.equals(HoppingIndicator.BASEBAND) && (ts == 0)))
	    {
	      if (trxc == bcchTrx)
	      {
	        // No hopping for BCCH (in case of BASEBAND only for TS0)
	        fr = bcchNo;
	        arfcnList.clear();
	      }
	      else
	      {
	        // Not BCCH TRXC, remove BCCH ARFCN from hopping list	    
	        for(int index = 0; index < arfcnList.size(); index++)
	        {
	          if (arfcnList.get(index) == bcchNo)
	          {
	            arfcnList.remove(index);
	            break;
	          }
	        }
	        maio = maio - 1; // what happens if maio = 0 here and become < 0 
	      }
	    }
	  }

	  if (!arfcnList.isEmpty())
	  {
	    fr = (hsn << 16) + maio; //  MAIO/HSN
	  }

	  narfcn = (short) arfcnList.size();

	  arfcnl = ArrayUtils.toPrimitive(arfcnList.toArray(new Integer[arfcnList.size()]));

	  return new CHNParams(chnComb, chnMain, tsc, narfcn, fr, arfcnl, sub);
    }

    private ChnComb getChnComb(ChannelKey chnKey, ActivationType chActType)	{
	  ChnComb chnComb;	
      switch(chnKey) {
          case BM:
        	 chnComb = ChnComb.GSMPH_TCHXF_FACCHXF_SACCHXTF;  // 'TCHxF_FACCHxF_SACCHxTF',
        	 break;
          case LM:
        	 chnComb = ChnComb.GSMPH_TCHXH_FACCHXH_SACCHXTH;  //   'TCHxH_FACCHxH_SACCHxTH',
             break;
          case BM_BI:
        	 if(chActType.equals(ActivationType.SEC_MUL))
             {
           	   chnComb = ChnComb.GSMPH_TCHXF_SACCHXM;
             }
             else
             {	 
        	   chnComb = ChnComb.GSMPH_TCHXF_FACCHXF_SACCHXM;    //   'TCHxF_FACCHxF_SACCHxM',
             }  
        	 break;
          case SDCCH_4:
        	 chnComb = ChnComb.GSMPH_SDCCHX4_SACCHXC4;   //   'SDCCHx4_SACCHxC4',
        	 break;
          case SDCCH_8:
        	 chnComb = ChnComb.GSMPH_SDCCHX8_SACCHXC8;   //   'SDCCHx8_SACCHxC8',
        	 break;
          case PDCH_BTTI:   
        	 chnComb = ChnComb.GSMPH_PDTXBTTI;    //   'PDTxBTTI',
        	 break;
          case PDCH_RTTI:  
        	 chnComb = ChnComb.GSMPH_PDTXRTTI;   //   'PDTxRTTI',
        	 break;
          case BM_UNI:
        	 chnComb = ChnComb.GSMPH_TCHXF_SACCHXM;    //  'TCHxF_SACCHxM',
        	 break;
   	     default:
		     logger.error("Incorrect channel type: " + chnKey.name);
   	    	 return null;  // raise an exception instead ?
        }
		return chnComb;
	}

    private ChannelKey getChannelKey(ChannelType channelType, ChannelRate in_chRate, String pdchMode) {
	  ChannelKey chnKey;
	 
	  if(in_chRate.equals(ChannelRate.Bm_BI)) {
	    chnKey = ChannelKey.BM_BI;
	  }
	  if(in_chRate.equals(ChannelRate.Bm_UNI)) {
	    chnKey = ChannelKey.BM_UNI;
	  }
	  else {	 
	    switch (channelType.toString()) {
	      case "Bm":
	      case "Bm_1":	
             chnKey = ChannelKey.BM;
		     break;
		  
	      case "Lm/0":
	      case "Lm/0_1":	   
	      case "Lm/1":
	      case "Lm/1_1":
	 	     chnKey = ChannelKey.LM;
		     break;		   
		   
	      case "SDCCH_4/0":
	      case "SDCCH_4/2orCBCH_V":	   //???
	      case "SDCCH_4/1":
	      case "SDCCH_4/2":
	      case "SDCCH_4/3":
		     chnKey = ChannelKey.SDCCH_4;
		     break;
		  
	      case "SDCCH_8/0":		  
	      case "SDCCH_8/1":		  
	      case "SDCCH_8/2":		  
	      case "SDCCH_8/3":	
	      case "SDCCH_8/4":		   
	      case "SDCCH_8/5":
	      case "SDCCH_8/6":		   
	      case "SDCCH_8/7":
		     chnKey = ChannelKey.SDCCH_8;
		     break;			  

	      case "PDCH":
		     if(pdchMode.equals("BTTI"))
		     {
		       chnKey = ChannelKey.PDCH_BTTI;  
		     }
		     else if(pdchMode.equals("RTTI"))
		     {
			   chnKey = ChannelKey.PDCH_RTTI;  
		     }
		     else
		     {
			   chnKey = ChannelKey.OUT_OF_BOUNDS;  
			   logger.warn("Unexpected PDCH Mode: " + pdchMode);
		     }
		     break;		  
/*		  case "CBCH_V":
	      case "CBCH_VII":			   
	      case "BCCH":
	      case "CCCH_U":	
	      case "CCCH_D":
	    	 chnKey = ChannelKey.OUT_OF_BOUNDS;     // raise an exception instead ?
			 logger.warn("Unexpected channel type: " + channelType.toString());
			 break;*/
	     default:
		     logger.error("Incorrect channel type: " + channelType.toString());
  	    	 return null;  // raise an exception instead ?
	    } 
	  }
	  return chnKey;
    }

    private short getSubChannel(ChannelType channelType) {
      short sub;	
      switch (channelType.name()) {
        case "Bm":
        case "Lm/0":	
        case "SDCCH_4/0":
        case "SDCCH_4/2orCBCH_V":	   //??? 
        case "CBCH_V":
        case "SDCCH_8/0":	
        case "CBCH_VII":	
        case "BCCH":
        case "CCCH_U":	
        case "CCCH_D":
        case "Bm_1":
        case "Lm/0_1":
        case "PDCH":	
           sub = 0;
           break;
        
        case "Lm/1":
        case "Lm/1_1":
        case "SDCCH_4/1":
        case "SDCCH_8/1":
           sub = 1;
           break;	
        
        case "SDCCH_4/2":
        case "SDCCH_8/2":
            sub = 2;
            break;        	

        case "SDCCH_4/3":
        case "SDCCH_8/3":	
            sub = 2;
            break;  	

        case "SDCCH_8/4":
           sub = 4;	
           break;
        	  
        case "SDCCH_8/5":
           sub = 5;
           break;
           
        case "SDCCH_8/6":
           sub = 6;
           break;
           
        case "SDCCH_8/7":
           sub = 7;
           break;
           
        default:
           sub = 0;
           break;
      }
	  return sub;
	}

    private ChnMain getChnMainForSignalling(ChannelKey chKey, AlgOrRate in_algOrRate, short amrCodec) {
      ChnMain chnMain;
      if( !(in_algOrRate.equals(AlgOrRate.NoResourcesRequired))) {
        logger.warn("This AlgOrRate is not implemented for SIGNALLING " + in_algOrRate.name());
        return null; // raise an exception instead ?
      }
      else {	
        switch(chKey) {
          case BM:
          case BM_BI:
          case SDCCH_4:
          case SDCCH_8:
   		     if(amrCodec == 0) {chnMain = ChnMain.GSMPH_TCH_FS; } 
   		     else 
   		     {
   		       logger.error("Wrong a amr codec " + amrCodec + " for AlgOrRate= " + in_algOrRate.name() );
   		       return null; // raise an exception instead ?
   		     }
        	 break; 
          
          case LM:
   		     if(amrCodec == 0) {chnMain = ChnMain.GSMPH_TCH_HS; } 
   		     else 
   		     {
   		       logger.error("Wrong a amr codec " + amrCodec + " for AlgOrRate= " + in_algOrRate.name() );
   		       return null; // raise an exception instead ?
   		     }
        	 break; 
  		  
          default:
 		     logger.error("This chnKey is not correct " + chKey.name());
 		     return null; // raise an exception instead ?
          
        }
      }
	  return chnMain;
	}

    private ChnMain getChnMainForData(ChannelKey chKey, AlgOrRate in_algOrRate, short amrCodec)	{
	  ChnMain chnMain;
	  switch (in_algOrRate) {
	    case T4_8kbits_s:
		   if(amrCodec == 0) {chnMain = ChnMain.GSMPH_TCH_F4_8; } 
		   else 
		   {
		     logger.error("Wrong a amr codec " + amrCodec + " for AlgOrRate= " + in_algOrRate.name() );
		     return null; // raise an exception instead ?  
		   }
		   break;

		case T9_6kbits_s: 
		   if(amrCodec == 0) {chnMain = ChnMain.GSMPH_TCH_F9_6; } 
		   else 
		   {
		     logger.error("Wrong a amr codec " + amrCodec + " for AlgOrRate= " + in_algOrRate.name() );
		     return null; // raise an exception instead ?  
		   }
		   break;
				 
		case T14_4kbits_s: 
		   if(amrCodec == 0) {chnMain = ChnMain.GSMPH_TCH_F14_4; } 
		   else 
		   {
		     logger.error("Wrong a amr codec " + amrCodec + " for AlgOrRate= " + in_algOrRate.name() );
		     return null; // raise an exception instead ?  
		   }
		   break;
	     
		case NT12kbits_s: 
		   if(amrCodec == 0) {chnMain = ChnMain.GSMPH_TCH_F9_6; } 
		   else 
		   {
		     logger.error("Wrong a amr codec " + amrCodec + " for AlgOrRate= " + in_algOrRate.name() );
		     return null; // raise an exception instead ?  
		   }
		   break;		                

		case NT14_5kbits_s: 
		   if(amrCodec == 0) {chnMain = ChnMain.GSMPH_TCH_F14_4; } 
		   else 
		   {
		     logger.error("Wrong a amr codec " + amrCodec + " for AlgOrRate= " + in_algOrRate.name() );
		     return null; // raise an exception instead ?  
		   }
		   break;			                
	   
		default:
		   logger.error("This AlgOrRate is not implemented for DATA " + in_algOrRate.name());
		   return null; // raise an exception instead ?
      }		
	  return chnMain;
	}

    private ChnMain getChnMainForSpeech(ChannelKey chKey, AlgOrRate in_algOrRate, short amrCodec) {
	  ChnMain chnMain;
      switch (in_algOrRate) {
	    case GSM1:
           if(amrCodec == 0) {chnMain = ChnMain.GSMPH_TCH_FS; } 
		   else 
		   {
		     logger.error("Wrong a amr codec " + amrCodec + " for AlgOrRate= " + in_algOrRate.name() );
		     return null; // raise an exception instead ?
		   }
           break;

	    case GSM2: 
           if(amrCodec == 0) {chnMain = ChnMain.GSMPH_TCH_EFS; } 
           else 
           {
             logger.error("Wrong a amr codec " + amrCodec + " for AlgOrRate= " + in_algOrRate.name() );
             return null; // raise an exception instead ?  
           }
           break;
		 
	    case GSM3:        
		   if(amrCodec == 1) {chnMain = ChnMain.GSMPH_TCH_AFS4_75; }
		   else if(amrCodec == 2) {chnMain = ChnMain.GSMPH_TCH_AFS5_15; }
		   else if(amrCodec == 4) {chnMain = ChnMain.GSMPH_TCH_AFS5_9; }
		   else if(amrCodec == 8) {chnMain = ChnMain.GSMPH_TCH_AFS6_7; }
		   else if(amrCodec == 16) {chnMain = ChnMain.GSMPH_TCH_AFS7_4; }
		   else if(amrCodec == 32) {chnMain = ChnMain.GSMPH_TCH_AFS7_95; }
		   else if(amrCodec == 64) {chnMain = ChnMain.GSMPH_TCH_AFS10_2; }
		   else if(amrCodec == 128) {chnMain = ChnMain.GSMPH_TCH_AFS12_2; }
		   else 
		   {
		     logger.error("Wrong a amr codec " + amrCodec + " for AlgOrRate= " + in_algOrRate.name() );
		     return null; // raise an exception instead ?
		   }		          
		   break;
		 
	    case GSM5:        
		   if(amrCodec == 1) {chnMain = ChnMain.GSMPH_TCH_WFS6_60; }
		   else if(amrCodec == 2) {chnMain = ChnMain.GSMPH_TCH_WFS8_85; }
		   else if(amrCodec == 4) {chnMain = ChnMain.GSMPH_TCH_WFS12_65; }
		   else 
		   {
		     logger.error("Wrong a amr codec " + amrCodec + " for AlgOrRate= " + in_algOrRate.name());
		     return null; // raise an exception instead ?
		   }
		   break;
			    
		default:
		   logger.error("This AlgOrRate is not implemented for SPEECH " + in_algOrRate.name());
		   return null; // raise an exception instead ?
	  }		
	  return chnMain;
	}
    
   /*
    * Purpose: To activate a channel in the MSSIM, sets the CcId package variable
    * Input: bypass (set to '1' to bypass MSSIM automatic LAPDm handling)
    */

    public long chActMSSIM(short cell, int tg, int trxc, short ts, int msId, ChannelType chType, boolean bypass, boolean allSacchSI, ChannelRate in_chRate,
	            TypeOfCh in_typeOfCh, AlgOrRate in_algOrRate, ActivationType in_ActType, short amrCodec, String pdchMode) {
		
    	CHNParams chnParam =  mssimGetCHNParam(tg, trxc, ts, chType, in_chRate, in_typeOfCh, in_algOrRate, amrCodec, pdchMode, in_ActType);  

	    // Send GsmphMPH_CS_CHN_OPEN_REQ (ChanActivation on Mssim)
		logger.info("Send MSSIM.gsmphMPH_CS_CHN_OPEN_REQ");

	    short trxNum = 0;
	    // Send MSSIM.gsmphMPH_CS_CHN_OPEN_REQ     
	    GsmphMPH_CS_CHN_OPEN_REQ open_Req = GsmbFactory.getGsmphMPH_CS_CHN_OPEN_REQBuilder(cell, FrStructureType.ARFCN)
	                .timeout(20)
	                .ref(0)
	                .chnComb(chnParam.getChnComb())
	                .chnMain(chnParam.getChnMain())
	                .ts(ts)
	                .sub(chnParam.getSub())
	                .msId(msId)
	                .tsc(chnParam.getTsc())
	                .rxAcchOn(true)   
	                .allSacchSI(allSacchSI)
	                .sdcchByp(bypass) 
	                .facchByp(bypass)  
	                .sacchByp(false) 
	                .rtOutOff(false)
	                .rxTrfcOn(false)
	                .undecFrm(false)
	                .narfcn(chnParam.getNarfcn())
	                .trxNum(trxNum)
	                .frArfcn(chnParam.getFrArfcn())
	                .build();

	    Confirmation confirmation = gsmb.send(open_Req);
     	if (confirmation.getErrorType() != PhErrorType.GSM_PH_ENOERR) {
     		logger.error("GsmphMPH_CHN_OPEN_CNF confirmation error");
         	return -1;
         }
	    
     	// get CcId from GsmphMPH_CHN_OPEN_CFN
	    long ccId = open_Req.getDetailedConfirmation(confirmation).getCcId();

	    GsmbSrvMS_SET_MEAS_CMD meas_cmd = GsmbFactory.getGsmbSrvMS_SET_MEAS_CMDBuilder().timeout(20)
	                .msId(msId)
	                .l2hdr((short) 2)
	                .chan(ChanBit.GSMB_SRV_SACCH) 
	                .spare(new short[] { 0, 0 })
	                .data(new short[] { 6, 21, 56, 56, 0, 111, 5, 128, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }) 
	                .build();
	    Response response = gsmb.send(meas_cmd);

     	if (!response.isSuccess()) {
     	    logger.error("GsmbSrvMS_SET_MEAS_CMD response failed");
         	return -1;
        }	    

	    return ccId;
    }	
	
}