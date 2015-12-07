package com.ericsson.msran.test.grat.testhelpers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.json.JSONException;

import com.ericsson.commonlibrary.resourcemanager.Abisco;
import com.ericsson.commonlibrary.resourcemanager.Rm;
import com.ericsson.commonlibrary.restorestack.RestoreCommandStack;
import com.ericsson.msran.helpers.Helpers;
import com.ericsson.msran.test.grat.testhelpers.restorecommands.AbiscoCloseConnection;
import com.ericsson.mssim.gsmb.impl.GsmbSrvCELL_PARM_1_CMDBuilder;

import com.ericsson.abisco.clientlib.MessageQueue;
import com.ericsson.abisco.clientlib.AbiscoClient;
import com.ericsson.abisco.clientlib.internal.EnumValue;
import com.ericsson.abisco.clientlib.internal.Substruct;
import com.ericsson.abisco.clientlib.servers.CMDHAND;
import com.ericsson.abisco.clientlib.servers.CMDHLIB;
import com.ericsson.abisco.clientlib.servers.BG;
import com.ericsson.abisco.clientlib.servers.BG.G31ATBundlingInfoUpdate;
import com.ericsson.abisco.clientlib.servers.BG.G31StatusSCF;
import com.ericsson.abisco.clientlib.servers.BG.G31StatusUpdate;
//import com.ericsson.abisco.clientlib.servers.CMDHAND.CHResponse;
import com.ericsson.abisco.clientlib.servers.CMDHAND.InfoCellResponse;
import com.ericsson.abisco.clientlib.servers.CMDHAND.Enums.BCCHType;
import com.ericsson.abisco.clientlib.servers.CMDHLIB.CompleteStart;
import com.ericsson.abisco.clientlib.servers.DISPATCH;
import com.ericsson.abisco.clientlib.servers.OM_G31R01.BSCRecord;
import com.ericsson.abisco.clientlib.servers.OM_G31R01.CalendarTimeBSC;
import com.ericsson.abisco.clientlib.servers.OM_G31R01.CalendarTimeExchangeResponse;
import com.ericsson.abisco.clientlib.servers.OM_G31R01.CombinationMainBCCH;
import com.ericsson.abisco.clientlib.servers.OM_G31R01.CombinationSDCCH;
import com.ericsson.abisco.clientlib.servers.OM_G31R01.Enums.MOClass;
import com.ericsson.abisco.clientlib.servers.OM_G31R01.FSOffset;
import com.ericsson.abisco.clientlib.servers.OM_G31R01.ListNumber;
import com.ericsson.abisco.clientlib.servers.OM_G31R01.NodeIdentityExchangeResponse;
import com.ericsson.abisco.clientlib.servers.OM_G31R01.TransportProfiles;
import com.ericsson.abisco.clientlib.servers.PARAMDISP.EncryptionSubStruct;
import com.ericsson.abisco.clientlib.servers.PARAMDISP.Enums.EncryptionActive;
import com.ericsson.abisco.clientlib.servers.PARAMDISP.SetupSubStruct;
import com.ericsson.abisco.clientlib.servers.PARAMDISP.TrafficCommand_Result;
import com.ericsson.abisco.clientlib.servers.PAYLOAD.ActivateSpeech;
import com.ericsson.abisco.clientlib.servers.PAYLOAD.DeactivateSpeech;
import com.ericsson.abisco.clientlib.servers.PAYLOAD.SpeechPatternStruct;
import com.ericsson.abisco.clientlib.servers.PAYLOAD.Enums.DTX;
import com.ericsson.abisco.clientlib.servers.PAYLOAD.Enums.SpeechGenerationMode;
import com.ericsson.abisco.clientlib.servers.PAYLOAD.Enums.Type1;
import com.ericsson.abisco.clientlib.servers.PAYLOAD.Enums.Type2;
import com.ericsson.abisco.clientlib.servers.PAYLOAD;
import com.ericsson.abisco.clientlib.servers.TRAFFRN;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.ActivationTypeStruct;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.BCCHInfoP2_3;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.BSPowerStruct;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.CellIdAirStruct;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.ChannelActNormalAssign;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.ChannelModeStruct;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.ChannelNoStruct;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.RFChannelRelease;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.Enums.ActivationType;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.UNACKNOWLEDGED_MESSAGE_SENTException;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.Enums.ChannelType;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.LAIStruct;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.MSPowerStruct;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.Enums.TypeOfCh;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.TimingAdvanceStruct;

import com.ericsson.abisco.clientlib.servers.OM_G31R01.BCC;
import com.ericsson.abisco.clientlib.servers.OM_G31R01.Enums;
import com.ericsson.abisco.clientlib.servers.OM_G31R01.FillingMarker;
import com.ericsson.abisco.clientlib.servers.OM_G31R01;
import com.ericsson.abisco.clientlib.servers.OM_G31R01.Enums.Mrk;
import com.ericsson.abisco.clientlib.servers.OM_G31R01.FrequencySpecifierTX;
import com.ericsson.abisco.clientlib.servers.OM_G31R01.FrequencySpecifierRX;
import com.ericsson.abisco.clientlib.servers.OM_G31R01.Power;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.NeighbourCellsDescr;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.NeighbourCellsDescrStruct;
import com.ericsson.abisco.clientlib.servers.PARAMDISP;

import com.ericsson.abisco.clientlib.AbiscoServer.Routing;

//**
import com.ericsson.abisco.clientlib.servers.CMDHLIB.CompleteStartRejectException;

import com.ericsson.msran.test.grat.testhelpers.abis.ChannelActAsyncHandoverBuilder;
import com.ericsson.msran.test.grat.testhelpers.abis.ChannelActImmediateAssignBuilder;
import com.ericsson.msran.test.grat.testhelpers.abis.ChannelActNormalAssignBuilder;
        
/**
 * @name AbisHelper
 * 
 * @author Asa Huhtasaari (xasahuh)
 * 
 * @created 2013-10-01
 * 
 * @description This class handles Abis message related functionality.
 * 
 * @revision xasahuh 2013-10-01 First version.
 * @revision xasahuh 2014-01-09 Minor updates for Abisco Java library.
 * @revision xasahuh 2014-01-29 Added TF Configuration Request support.
 * @revision xasahuh 2014-01-30 Added DisableRequest and EnableRequest support.
 * @revision xasahuh 2014-02-03 Added Abort SP Command support.
 * @revision xasahuh 2014-02-11 Adaption to tac-bundle support of Abisco Java library.
 * @revision See Git for full version history.
 * 
 */
public class AbisHelper {
	
    public static final int SAPI_OML = 62;
    public static final int SAPI_RSL = 0;
    public static final int SAPI_CS  = 10;
    public static final int SAPI_CSD = 11;
    public static final int SAPI_PS  = 12;
	public static final int SAPI_INDEX_RSL = 0;
	public static final int SAPI_INDEX_CS  = 1;
	public static final int SAPI_INDEX_CSD = 2;
	public static final int SAPI_INDEX_PS  = 3;
    public static final int SAPI_INDEX_OML = 4;
    public final int SECONDS_TO_WAIT_FOR_SYNC = 20;
	private static final int TRXC_ROUTING = 0;
    private final OM_G31R01.Enums.MOClass moClassTf = OM_G31R01.Enums.MOClass.TF;
    private final OM_G31R01.Enums.MOClass moClassTx = OM_G31R01.Enums.MOClass.TX;    
    private final OM_G31R01.Enums.MOClass moClassRx = OM_G31R01.Enums.MOClass.RX;    
    private final OM_G31R01.Enums.MOClass moClassTs = OM_G31R01.Enums.MOClass.TS;  
    private final BG.Enums.MOClass moClassTrxc = BG.Enums.MOClass.TRXC;
    
    private static final int ASSOCIATED_SO_INSTANCE = 255;
    private static final int INSTANCE_NUMBER = 0;
	private static final int TG_ROUTING = 0;
    private Logger logger;
    private RestoreCommandStack restoreStack;
    
    private AbiscoClient abiscoClient;
    private MomHelper momHelper;
    private OM_G31R01 omServer;
    private BG bgServer;
    private TRAFFRN traffRnServer;
    private PARAMDISP paramdisp;
    private CMDHLIB cmdhlib;
    private DISPATCH dispatch;
    private PAYLOAD payloadServer;
    
    // Needed for wp 1435
    boolean bcchCombined = false; 
    
    final int iwdTypeOml = 0;
    final int iwdTypeRsl = 1;
    
    private int arfcnOnTrx;

    private enum CHECK_BUNDLING_INFO_UPDATE_SCHEME {
    	CHECK_FOR_DEFAULT_BUNDLING_INFO,
    	CHECK_FOR_NO_BUNDLING_INFO,
    	CHECK_FOR_ANY_BUNDLING_INFO
    } 
    
    /**
     * Helper class for Abis messages to the Abisco.
     */
    public AbisHelper() {
        System.out.println("AbisHelper");
        restoreStack = Helpers.restore().restoreStackHelper().getRestoreStack();
        momHelper = new MomHelper();
        logger = Logger.getLogger(AbisHelper.class);
        try {
            List<Abisco> abiscos = Rm.getAbiscoList();
            for (Iterator<Abisco> i = abiscos.iterator(); i.hasNext();) {
                Abisco a = i.next();
                System.out.println("Abisco Tss host: " + a.getTssHost()+ " "+ a.getSiuHost() + " " +
                a.getTrafficHost());
            }
        AbiscoVersionHelper abiscoVersionHelper = new AbiscoVersionHelper();
        abiscoClient = abiscoVersionHelper.getAbiscoClient();
        restoreStack.add(new AbiscoCloseConnection(abiscoClient));
        omServer = abiscoClient.getOM_G31R01();
        System.out.println("After omServer");
       
        
        System.out.println("AbisHelper 2 + omSerer" + omServer.toString());
        bgServer = abiscoClient.getBG();
        System.out.println("AbisHelper 3");
        traffRnServer = abiscoClient.getTRAFFRN();
        System.out.println("AbisHelper 4");
        paramdisp = abiscoClient.getPARAMDISP();
        cmdhlib = abiscoClient.getCMDHLIB();
        dispatch = abiscoClient.getDISPATCH();
        payloadServer = abiscoClient.getPAYLOAD();
        } catch (Exception e) {
            System.out.println("Got exception");
            e.printStackTrace();
            throw e;
        }
    }

    public AbiscoClient getAbiscoClient ()
    {
        return abiscoClient;
    }

    public TRAFFRN getRslServer ()
    {
    	return traffRnServer;
    }

    public BG getBG ()
    {
        return bgServer;
    }
    
    public PARAMDISP getPARAMDISP ()
    {
        return paramdisp;
    }
    
    public DISPATCH getDISPATCH ()
    {
        return dispatch;
    }
    
    public OM_G31R01 getOmServer() {
        return omServer;
    }
    
    public PAYLOAD getPayloadServer() {
        return payloadServer;
    }
    
    /**
     * @name sendcallSetupMt
     * 
     * @description sends CallSetupMT
     * 
     * @return TrafficCommand_Result
     * 
     * @throws InterruptedException
     */
    //Our testsim in TEMS-mobile:
    //PARAMDISP.CallSetupMT Action=CommandAndSave,LMSID=0,SIMCardSubStruct.S_SIMCardSubStruct=TRUE,SIMCardSubStruct.IMSI=[2,4,0,9,9,9,9,0,0,0,0,1,4,6,7],SIMCardSubStruct.TMSI=[0,0,0,0],SIMCardSubStruct.KC=[37,30,93,2,182,27,164,0],SIMCardSubStruct.RAND=[0,1,2,4,8,16,32,64,128,1,3,7,15,31,63,127],AcceptAlert=Yes,SignallingChannel=SDCCH,SendChannelNeeded=No,PagingId=PagingIMSI,CallType=Fullrate,ChannelMode=SpeechMode,SpeechAlgorithm=NormalFullRate,DTX_UL=Off,DTX_DL=On,EncryptionSubStruct.EncryptionActive=Off,EncryptionSubStruct.EncryptionAlgorithm=A5/1,MSPowerValue=0,TimingAdvance=0,SpeechGenerationMode=LoopBack,PagingModeStruct.PagingMode=Standard,BearerChoice.SpeechSubStruct.S_SpeechSubStruct=TRUE,BearerChoice.SpeechSubStruct.InfoTransferCapability=0,BearerChoice.SpeechSubStruct.TransferMode=0,BearerChoice.SpeechSubStruct.BearerCodingStandard=0,BearerChoice.SpeechSubStruct.SpeechVerInd=0,BearerChoice.SpeechSubStruct.BearerCoding=0,UseCalculatedTA=Yes,PagingAttemptsStruct.NumberOfPagingAttempts=10
    public TrafficCommand_Result sendcallSetupMt() throws InterruptedException {
    	return sendcallSetupMt(false, 0);
    }
    
    public TrafficCommand_Result sendcallSetupMt(int msPower) throws InterruptedException {
    	return sendcallSetupMt(false, msPower);
    }
    public TrafficCommand_Result sendcallSetupMt(boolean withEncryption) throws InterruptedException {
    	return sendcallSetupMt(withEncryption, 0);
    }
    
    public TrafficCommand_Result sendcallSetupMt(boolean withEncryption, int msPower) throws InterruptedException {
    	PARAMDISP.SIMCardSubStruct simCardSubStruct = ImsiHelper.getSIMCardSubStruct();
    	return sendcallSetupMt(simCardSubStruct, withEncryption, msPower);
    }
    public TrafficCommand_Result sendcallSetupMt(PARAMDISP.SIMCardSubStruct simCardSubStruct,
    		boolean withEncryption, int msPower) throws InterruptedException {
    	PARAMDISP.CallSetupMT callSetupMT = paramdisp.createCallSetupMT();
    	return sendcallSetupMt(callSetupMT, simCardSubStruct, withEncryption, msPower);
    }
    public TrafficCommand_Result sendcallSetupMt(PARAMDISP.CallSetupMT callSetupMT, PARAMDISP.SIMCardSubStruct simCardSubStruct, 
    					boolean withEncryption, int msPower) throws InterruptedException {
    	//PARAMDISP.CallSetupMT callSetupMT = paramdisp.createCallSetupMT();
    	callSetupMT.setLMSID(0);
    	
    	callSetupMT.setSIMCardSubStruct(simCardSubStruct);
    	callSetupMT.setSpeechGenerationMode(PARAMDISP.Enums.SpeechGenerationMode.LoopBack);

    	if (withEncryption) {
    		// Add encryption
    		// Use encryption algorithm A5_3
           	EncryptionSubStruct encryptionSubStruct = new EncryptionSubStruct(EncryptionActive.On, PARAMDISP.Enums.EncryptionAlgorithm.A5_3);
           	encryptionSubStruct.setEncryptionActive(EncryptionActive.On);
           	callSetupMT.setEncryptionSubStruct(encryptionSubStruct);
    	}

    	PARAMDISP.PagingAttemptsStruct pagingAttemptsStruct = new PARAMDISP.PagingAttemptsStruct(5);
    	callSetupMT.setPagingAttemptsStruct(pagingAttemptsStruct);
    	
    	// Set MSPower
    	callSetupMT.setMSPowerValue(msPower);

    	logger.info("sending CallSetupMT = " + callSetupMT);
    	return callSetupMT.send();
    }
    
    public TrafficCommand_Result sendcallSetupMt(PARAMDISP.Enums.BSPowerValue bsPower) throws InterruptedException {
    	PARAMDISP.CallSetupMT callSetupMT = paramdisp.createCallSetupMT();
    	callSetupMT.setLMSID(0);

    	PARAMDISP.SIMCardSubStruct simCardSubStruct = ImsiHelper.getSIMCardSubStruct();
    	callSetupMT.setSIMCardSubStruct(simCardSubStruct);
    	callSetupMT.setSpeechGenerationMode(PARAMDISP.Enums.SpeechGenerationMode.LoopBack);

    	PARAMDISP.PagingAttemptsStruct pagingAttemptsStruct = new PARAMDISP.PagingAttemptsStruct(5);
    	callSetupMT.setPagingAttemptsStruct(pagingAttemptsStruct);

    	PARAMDISP.BSPowerSubStruct bsPowerSubStruct = new PARAMDISP.BSPowerSubStruct();
    	bsPowerSubStruct.setBSPowerValue(bsPower);
    	callSetupMT.setBSPowerSubStruct(bsPowerSubStruct);

    	logger.info("sending CallSetupMT = " + callSetupMT);
    	return callSetupMT.send();
    }
    
    public TrafficCommand_Result disconnectCallMt() throws InterruptedException {
    	PARAMDISP.SIMCardSubStruct simCardSubStruct = ImsiHelper.getSIMCardSubStruct();
    	return disconnectCallMt(simCardSubStruct);
    }

    public TrafficCommand_Result disconnectCallMt(PARAMDISP.SIMCardSubStruct simCardSubStruct) throws InterruptedException {
    	PARAMDISP.DisconnectCallMT disconnectCallMT = paramdisp.createDisconnectCallMT();
    	disconnectCallMT.setSIMCardSubStruct(simCardSubStruct);
    	
    	return disconnectCallMT.send();
    }
    
    /**
     * @name sendSMSSetupMT
     * 
     * @description sends SMSSetupMT
     * 
     * @return void
     * 
     * @throws InterruptedException
     */
    //Our testsim in TEMS-mobile:
    //PARAMDISP.CallSetupMT Action=CommandAndSave,LMSID=0,SIMCardSubStruct.S_SIMCardSubStruct=TRUE,SIMCardSubStruct.IMSI=[2,4,0,9,9,9,9,0,0,0,0,1,4,6,7],SIMCardSubStruct.TMSI=[0,0,0,0],SIMCardSubStruct.KC=[37,30,93,2,182,27,164,0],SIMCardSubStruct.RAND=[0,1,2,4,8,16,32,64,128,1,3,7,15,31,63,127],AcceptAlert=Yes,SignallingChannel=SDCCH,SendChannelNeeded=No,PagingId=PagingIMSI,CallType=Fullrate,ChannelMode=SpeechMode,SpeechAlgorithm=NormalFullRate,DTX_UL=Off,DTX_DL=On,EncryptionSubStruct.EncryptionActive=Off,EncryptionSubStruct.EncryptionAlgorithm=A5/1,MSPowerValue=0,TimingAdvance=0,SpeechGenerationMode=LoopBack,PagingModeStruct.PagingMode=Standard,BearerChoice.SpeechSubStruct.S_SpeechSubStruct=TRUE,BearerChoice.SpeechSubStruct.InfoTransferCapability=0,BearerChoice.SpeechSubStruct.TransferMode=0,BearerChoice.SpeechSubStruct.BearerCodingStandard=0,BearerChoice.SpeechSubStruct.SpeechVerInd=0,BearerChoice.SpeechSubStruct.BearerCoding=0,UseCalculatedTA=Yes,PagingAttemptsStruct.NumberOfPagingAttempts=10
    public TrafficCommand_Result sendSMSSetupMT() throws InterruptedException {
      PARAMDISP.SIMCardSubStruct simCardSubStruct = ImsiHelper.getSIMCardSubStruct();
      return sendSMSSetupMt(simCardSubStruct);
    }
    
    public TrafficCommand_Result sendSMSSetupMt(PARAMDISP.SIMCardSubStruct simCardSubStruct) throws InterruptedException {
      PARAMDISP.PagingAttemptsStruct pagingAttemptsStruct = new PARAMDISP.PagingAttemptsStruct(1);
     
      PARAMDISP.SMSSetupMT smsSetupMT = paramdisp.createSMSSetupMT();
      smsSetupMT.setLMSID(0);
      smsSetupMT.setSIMCardSubStruct(simCardSubStruct);
      smsSetupMT.setPagingAttemptsStruct(pagingAttemptsStruct);
      
      return smsSetupMT.send();
    }
    
    /**
     * @name sendBCCHInfo
     * 
     * @description sends BCCH Info for Tg 0 and trxc 0
     * @return BCCHInfoP2_3
     * @throws InterruptedException
     */
    public BCCHInfoP2_3 sendBCCHInfo() throws InterruptedException {
        return sendBCCHInfo(0, 0);
    }
    
    /**
     * 
     * @name sendBCCHInfoP1_1
     * @param tgId - int
     * @param trxc - ubt
     * @description sends sendBCCHInfoP1_1 for the given tgId and trxc
     * 
     * @return BCCHInfoP1_1
     * 
     * @throws InterruptedException
     */
    public BCCHInfoP2_3 sendBCCHInfo(int tgId, int trxc) throws InterruptedException {
    	
    	ChannelNoStruct channelNoStruct = new ChannelNoStruct();
    	channelNoStruct.setTimeSlotNo(0);
    	channelNoStruct.setChannelType(ChannelType.BCCH);
    	
    	Routing routing = new Routing();
    	routing.setTG(tgId);
    	routing.setTRXC(trxc);
    	
    	LAIStruct laiStruct = new LAIStruct();
    	List<Integer> mccList = new ArrayList<Integer> ();
    	mccList.add(2);
    	mccList.add(4);
    	mccList.add(0);
    	laiStruct.setMCCDigit(mccList);
    	
    	NeighbourCellsDescr neighbourCellsDescr = new NeighbourCellsDescr();
    	NeighbourCellsDescrStruct neighbourCellsDescrStruct = new NeighbourCellsDescrStruct();
    	neighbourCellsDescrStruct.setBA_ARFCN121_124(0);
    	neighbourCellsDescrStruct.setBA_Ind(0);
    	neighbourCellsDescrStruct.setSpare(0);
    	neighbourCellsDescrStruct.setBA_No(0);
    	List<Integer> neighbourCellList = new ArrayList<Integer> ();
    	for (int i=0; i<15; i++)
    		neighbourCellList.add(0);
    	neighbourCellsDescrStruct.setBA_ARFCN1_120(neighbourCellList);
    	neighbourCellsDescr.setNeighbourCellsDescrStruct(neighbourCellsDescrStruct);
    	
    	
    	List<Integer> mncList = new ArrayList<Integer> ();
    	mncList.add(9);
    	mncList.add(9);
    	laiStruct.setMNCDigit(mncList);
    	
    	List<Integer> lacList = new ArrayList<Integer> ();
    	lacList.add(0);
    	lacList.add(1);
    	laiStruct.setLAC(lacList);
    	
    	TRAFFRN.CellSelectionParamStruct cellSelectionParamStruct = new TRAFFRN.CellSelectionParamStruct();
    	cellSelectionParamStruct.setMSTXPowerMaxCCH(3);
    	cellSelectionParamStruct.setCellReselectHysteresis(2);
    	cellSelectionParamStruct.setRXLevelAccessMin(35);
    	cellSelectionParamStruct.setACS(0);
    	
    	TRAFFRN.RACHControlParamStruct rachControlParamStruct = new TRAFFRN.RACHControlParamStruct();
    	rachControlParamStruct.setCallReestablishmentAllowed(1);
    	
    	TRAFFRN.PLMNPermittedStruct plmnPermittedStruct = new TRAFFRN.PLMNPermittedStruct();
    	plmnPermittedStruct.setNCCPermitted(255);
    	
    	TRAFFRN.ControlChannelDescrStruct controlChannelDescrStruct = new TRAFFRN.ControlChannelDescrStruct();
    	controlChannelDescrStruct.setBS_AG_BLKS_RES(1);
    	controlChannelDescrStruct.setBS_PA_MFRMS(1);
    	// Needed to support wp 1435
    	if ( bcchCombined == true){
    		controlChannelDescrStruct.setCCCHConfirmed(1); //wp 1435
    	}
    		


    	TRAFFRN.BCCHInfoP2_2 bcchInfo2_2 = traffRnServer.createBCCHInfoP2_2();

    	bcchInfo2_2.setRouting(routing);
    	bcchInfo2_2.setChannelNoStruct(channelNoStruct);
    	bcchInfo2_2.setPLMNPermittedStruct(plmnPermittedStruct);
    	bcchInfo2_2.setRACHControlParamStruct(rachControlParamStruct);
    	bcchInfo2_2.setNeighbourCellsDescr(neighbourCellsDescr);

    	
    	TRAFFRN.BCCHInfoP2_3 bcchInfo2_3 = traffRnServer.createBCCHInfoP2_3();
    	bcchInfo2_3.setRouting(routing);
    	bcchInfo2_3.setChannelNoStruct(channelNoStruct);
    	bcchInfo2_3.setControlChannelDescrStruct(controlChannelDescrStruct);
    	bcchInfo2_3.setLAIStruct(laiStruct);
    	bcchInfo2_3.setCellSelectionParamStruct(cellSelectionParamStruct);
    	bcchInfo2_3.setRACHControlParamStruct(rachControlParamStruct);
    	
    	
    	TRAFFRN.BCCHInfoP2_4 bcchInfo2_4 = traffRnServer.createBCCHInfoP2_4();
    	bcchInfo2_4.setRouting(routing);
    	bcchInfo2_4.setChannelNoStruct(channelNoStruct);
    	bcchInfo2_4.setLAIStruct(laiStruct);
    	bcchInfo2_4.setCellSelectionParamStruct(cellSelectionParamStruct);
    	bcchInfo2_4.setRACHControlParamStruct(rachControlParamStruct);

    	logger.info("bcchInfo2_2 = " + bcchInfo2_2);
    	logger.info("bcchInfo2_3 = " + bcchInfo2_3);
    	logger.info("bcchInfo2_4 = " + bcchInfo2_4);
    	
        try {
        	bcchInfo2_2.send();
        } catch (UNACKNOWLEDGED_MESSAGE_SENTException e) {
            logger.debug("bcchInfo2_2 -> UNACKNOWLEDGED_MESSAGE_SENTException as expected");
        }
        try {
        	bcchInfo2_3.send();
        } catch (UNACKNOWLEDGED_MESSAGE_SENTException e) {
            logger.debug("bcchInfo2_3 -> UNACKNOWLEDGED_MESSAGE_SENTException as expected");
        }
        try {
        	bcchInfo2_4.send();
        } catch (UNACKNOWLEDGED_MESSAGE_SENTException e) {
            logger.debug("bcchInfo2_4 -> UNACKNOWLEDGED_MESSAGE_SENTException as expected");
        }

    	//traffRnServer.createChannelActNormalAssign();

        return bcchInfo2_3;
    }
    
    /**
     * Perform updateCell of the specified cell, 
     * with the provided tgId and BCCHType.
     * @param tgId The Transciever Group to start up
     * @param cell The number of rbsCell
     * @param BCCHType The type of BCCH to use ( COMB/NCOMB) 
     * @throws InterruptedException
     */
    public void updateCell(int tgId, short cell, BCCHType bcchType) throws InterruptedException
    {
    	CMDHAND.UpdateCell cmdUpdateCell = abiscoClient.getCMDHAND().createUpdateCell();
    	cmdUpdateCell.setTGId(tgId); 
    	cmdUpdateCell.setCellNumber(cell);
    	cmdUpdateCell.setBCCHType(bcchType);
      
    	// Send the update
    	CMDHAND.CHResponse uptCellRsp = null;
    	uptCellRsp = cmdUpdateCell.send();
    	
    }
  
    /**
     * Perform a complete startup of the specified numOfTGs, 
     * with the provided number of TrxesPerTG.
     * @param current_tg The Transciever Group to start up
     * @param numOfTrxesPerTG The number of Trxes/Tg to start
     * @throws InterruptedException
     */
    public void completeStartup(int numOfTGs, int numOfTrxesPerTG, List<TgLdns> tgLdnsList) throws InterruptedException
    {    
    	AbiscoConnection abisco;
    	abisco = new AbiscoConnection(); 
    	    	
    	for (int current_tg = 0; current_tg < numOfTGs; ++current_tg) 
    	{
    		logger.info("completeStartup loop current_tg = "+ current_tg);    	
    		TgLdns tg = tgLdnsList.get(current_tg);
    		
    		momHelper.checkAbisIpMoAttributeAfterUnlockBscConnected(tg.abisIpLdn, abisco.getBscIpAddress(), 30);
    		for (String trxLdn: tg.trxLdnList) 
    		{
    			logger.info("Verify Trx MO states after unlock for " + trxLdn);
    			momHelper.checkTrxMoAttributeAfterUnlockNoLinks(trxLdn, 20);
            }
    		
    		// Send completeStartup
    		try 
    		{
    			completeStartup(current_tg, numOfTrxesPerTG);
            }
    		catch(CompleteStartRejectException e)
    		{
    			logger.info("Failde during try completeStartup = "+ e);
            }
    		
    	}
    }
    
    /**
     * Perform a complete startup of the specified TGs.
     * Assumes that TG and TRX IDs are numbered from 0 and up in increments of one.
     * @param tgLdnsList List of Transceiver Groups LDNs
     * @throws InterruptedException
     */   
    public void completeStartup(List<TgLdns> tgLdnsList) throws InterruptedException {
        AbiscoConnection abisco = new AbiscoConnection();
        for (int current_tg = 0; current_tg < tgLdnsList.size(); ++current_tg) {
            logger.info("completeStartup loop current_tg = "+ current_tg);      
            TgLdns tg = tgLdnsList.get(current_tg);
            
            momHelper.checkAbisIpMoAttributeAfterUnlockBscConnected(tg.abisIpLdn, abisco.getBscIpAddress(), 30);
            for (String trxLdn: tg.trxLdnList) {
                logger.info("Verify Trx MO states after unlock for " + trxLdn);
                momHelper.checkTrxMoAttributeAfterUnlockNoLinks(trxLdn, 20);
            }
            
            // Send completeStartup
            completeStartup(current_tg, tg.trxLdnList.size());
        }
    }
    
    /**
     * Perform a complete startup of the specified TG, 
     * with the provided number of Trxes.
     * @param tgId The Transciever Group to start up
     * @param numOfTrxes The number of Trxes to start, index 0..(numOfTrxes-)
     * @throws InterruptedException
     */
    public void completeStartup(int tgId, int numOfTrxes) throws InterruptedException
    {
        List<Integer> trxList = new ArrayList<>();
        for (int i = 0; i < numOfTrxes; ++i) trxList.add(i);
        CompleteStart completeStart = abiscoClient.getCMDHLIB().createCompleteStart();
        completeStart.setTGId(tgId);
        completeStart.setTRXList(trxList);
        completeStart.setSiList(Arrays.asList(CMDHLIB.Enums.SiList.SI1, CMDHLIB.Enums.SiList.SI2, CMDHLIB.Enums.SiList.SI3, CMDHLIB.Enums.SiList.SI4, CMDHLIB.Enums.SiList.SI5, CMDHLIB.Enums.SiList.SI6, CMDHLIB.Enums.SiList.MI));
        completeStart.send();
    }
    
    /**
     * @name resetCommand
     * 
     * @description Handles a Reset command EP
     * 
     * @param moClass - OM_G31R01.Enums.MOClass
     * 
     * @return OM_G31R01.ResetComplete
     * 
     * @throws InterruptedException
     */
    public OM_G31R01.ResetComplete resetCommand(OM_G31R01.Enums.MOClass moClass) throws InterruptedException {
    	return resetCommand(moClass, TG_ROUTING, TRXC_ROUTING, INSTANCE_NUMBER, ASSOCIATED_SO_INSTANCE);
    }
    
    /**
     * @name resetCommand
     * 
     * @description Handles a Reset command EP
     * 
     * @param moClass - OM_G31R01.Enums.MOClass
     * @param instance - int 
     * @param associatedSoInstance - int
     * 
     * @return OM_G31R01.ResetComplete
     * 
     * @throws InterruptedException
     */
    public OM_G31R01.ResetComplete resetCommand(OM_G31R01.Enums.MOClass moClass, int instance, int associatedSoInstance) throws InterruptedException {
    	return resetCommand(moClass, TG_ROUTING, TRXC_ROUTING, instance, associatedSoInstance);
    }
    
    public OM_G31R01.ResetComplete resetCommand(OM_G31R01.Enums.MOClass moClass, int tgId, int trxc, int instance, int associatedSoInstance) throws InterruptedException {
    	OM_G31R01.ResetCommand resetCmd;

        resetCmd = omServer.createResetCommand();

        resetCmd.setMOClass(moClass);
        resetCmd.getRouting().setTG(tgId);
        if (isTrxcMo(moClass)) {
        resetCmd.getRouting().setTRXC(trxc);
        }
        resetCmd.setAssociatedSOInstance(associatedSoInstance);
        resetCmd.setInstanceNumber(instance);

        return resetCmd.send();
    }
    
    /**
     * @name abortSpCommand
     * 
     * @description Handles an Abort SP Command
     * 
     * @param moClass - OM_G31R01.Enums.MOClass
     * 
     * @return OM_G31R01.AbortSPComplete
     * 
     * @throws InterruptedException
     */
    public OM_G31R01.AbortSPComplete abortSpCommand(OM_G31R01.Enums.MOClass moClass) throws InterruptedException {

        OM_G31R01.AbortSPCommand abortCmd;

        abortCmd = omServer.createAbortSPCommand();

        abortCmd.setMOClass(moClass);
        abortCmd.setAssociatedSOInstance(ASSOCIATED_SO_INSTANCE);
        abortCmd.setInstanceNumber(INSTANCE_NUMBER);

        return abortCmd.send();
    }

    /**
     * @name startRequest
     * 
     * @description Handles a Start Request command SP
     * 
     * @param moClass - OM_G31R01.Enums.MOClass
     * @param tgId    - The TG this request is aimed for
     * 
     * @return OM_G31R01.StartResult
     * 
     * @throws InterruptedException
     */
    public OM_G31R01.StartResult startRequest(OM_G31R01.Enums.MOClass moClass, int tgId) throws InterruptedException {
    	return startRequest(moClass, tgId, TRXC_ROUTING, INSTANCE_NUMBER, ASSOCIATED_SO_INSTANCE);
    }
    
    

	/**
     * @name startRequest
     * 
     * @description Handles a Start Request command SP
     * 
     * @param moClass - OM_G31R01.Enums.MOClass
     * @param instance - int
     * @param associatedSoInstance - int
     * 
     * @return OM_G31R01.StartResult
     * 
     * @throws InterruptedException
     */
    public OM_G31R01.StartResult startRequest(OM_G31R01.Enums.MOClass moClass, int instance, int associatedSoInstance) throws InterruptedException {
    	return startRequest(moClass, TG_ROUTING, TRXC_ROUTING, instance, associatedSoInstance);
    }
    
    /**
     * @name startRequest
     * 
     * @description Handles a Start Request command SP
     * 
     * @param moClass - OM_G31R01.Enums.MOClass
     * @param tgId - int
     * @param trxcId - int
     * @param instance - int
     * @param associatedSoInstance - int
     * 
     * @return OM_G31R01.StartResult
     * 
     * @throws InterruptedException
     */
    public OM_G31R01.StartResult startRequest(OM_G31R01.Enums.MOClass moClass, int tgId, int trxcId, int instance, int associatedSoInstance) throws InterruptedException {

        OM_G31R01.StartRequest startReq;

        startReq = omServer.createStartRequest();
        startReq.getRouting().setTG(tgId);
        if (isTrxcMo(moClass)) {
        	startReq.getRouting().setTRXC(trxcId);
        }
        startReq.setMOClass(moClass);
        startReq.setAssociatedSOInstance(associatedSoInstance);
        startReq.setInstanceNumber(instance);

        return startReq.send();
    }
    
    /**
     * @name startRequestAsync
     * 
     * @description Sends Start Request without waiting for any confirmation
     * 
     * @param moClass - OM_G31R01.Enums.MOClass
     */
    public void startRequestAsync(OM_G31R01.Enums.MOClass moClass) {

        OM_G31R01.StartRequest startReq;

        startReq = omServer.createStartRequest();
        startReq.getRouting().setTG(TG_ROUTING);
        if (isTrxcMo(moClass)) {
        	startReq.getRouting().setTRXC(TRXC_ROUTING);
        }
        startReq.setMOClass(moClass);
        startReq.setAssociatedSOInstance(ASSOCIATED_SO_INSTANCE);
        startReq.setInstanceNumber(INSTANCE_NUMBER);

        startReq.sendAsync();
    }
    
    /**
     * @name nodeIdentityExchange
     * 
     * @description Handles a Node Identity Exchange Request EP
     * 
     * @param tgId               - The TG this request is aimed for
     * @param bscId              - The bsc identity
     * @param bscTgId            - The TG id in the bsc
     * @param bscNodeIdSignature - The Bsc Node Identity Signature (must fit into two bytes)
     * 
     * @return OM_G31R01.NodeIdentityExchangeResponse
     * 
     * @throws InterruptedException
     */
    
    public NodeIdentityExchangeResponse nodeIdentityExchange(int tgId, String bscId, String bscTgId, int bscNodeIdSignature) throws InterruptedException {
    	OM_G31R01.NodeIdentityExchangeRequest nodeIdExRequest;
    	
    	nodeIdExRequest = omServer.createNodeIdentityExchangeRequest();
    	nodeIdExRequest.getRouting().setTG(tgId);
    	nodeIdExRequest.setMOClass(OM_G31R01.Enums.MOClass.SCF);
    	nodeIdExRequest.setAssociatedSOInstance(ASSOCIATED_SO_INSTANCE);
    	nodeIdExRequest.setInstanceNumber(INSTANCE_NUMBER);
    	
    	List<Integer> bytes = new ArrayList<Integer>();
    	
    	for (int i=0 ; i < bscId.length() ; ++i) {
    		bytes.add(Integer.valueOf(bscId.charAt(i)));
    	}
    	nodeIdExRequest.setBSCIdentity(bytes);
  
    	bytes = new ArrayList<Integer>();
    	
    	for (int i=0 ; i < bscTgId.length() ; ++i) {
    		bytes.add(Integer.valueOf(bscTgId.charAt(i)));
    	}
    	nodeIdExRequest.setTGIdentity(bytes);
    	
    	nodeIdExRequest.setBSCNodeIdentitySignature(bscNodeIdSignature);
    	
        return nodeIdExRequest.send();
    }
    
    /**
     * @name calendarTimeExchange
     * 
     * @description Handles a Calendar Time Exchange Request EP
     * 
     * @param tgId       - The TG this request is aimed for
     * @param year       - The year in the BSC Calendar Time
     * @param month      - The month in the BSC Calendar Time
     * @param day        - The day in the BSC Calendar Time
     * @param hour       - The hour in the BSC Calendar Time
     * @param minute     - The minute in the BSC Calendar Time
     * @param second     - The second in the BSC Calendar Time
     * @param deciSecond - The deciSecond in the BSC Calendar Time
     * 
     * 
     * @return OM_G31R01.CalendarTimeExchangeRequest
     * 
     * @throws InterruptedException
     */
    
    public CalendarTimeExchangeResponse calendarTimeExchange(int tgId, Integer year, Integer month, Integer day, Integer hour, Integer minute, Integer second, Integer deciSecond) throws InterruptedException {
    	
    	OM_G31R01.CalendarTimeExchangeRequest calendarTimeExRequest;
    	
    	calendarTimeExRequest = omServer.createCalendarTimeExchangeRequest();
    	calendarTimeExRequest.getRouting().setTG(tgId);
    	calendarTimeExRequest.setMOClass(OM_G31R01.Enums.MOClass.SCF);
    	calendarTimeExRequest.setAssociatedSOInstance(ASSOCIATED_SO_INSTANCE);
    	calendarTimeExRequest.setInstanceNumber(INSTANCE_NUMBER);
    	
    	OM_G31R01.CalendarTimeBSC bscTime = new CalendarTimeBSC(year, month, day);
    	bscTime.setHour(hour);
    	bscTime.setMinute(minute);
    	bscTime.setSecond(second);
    	bscTime.setDeciSecond(deciSecond);
    	
    	calendarTimeExRequest.setCalendarTimeBSC(bscTime);
    	
    	return calendarTimeExRequest.send();
    }    
    
    /**
     * @name disableRequest
     * 
     * @description Handles a Disable Request SP
     * 
     * @param moClass - OM_G31R01.Enums.MOClass
     *
     * @return OM_G31R01.DisableResult
     * 
     * @throws InterruptedException
     */
    public OM_G31R01.DisableResult disableRequest(OM_G31R01.Enums.MOClass moClass) throws InterruptedException {
    	return disableRequest(moClass, INSTANCE_NUMBER, ASSOCIATED_SO_INSTANCE);
    }
    
    /**
     * @name disableRequest
     * 
     * @description Handles a Disable Request SP
     * 
     * @param moClass - OM_G31R01.Enums.MOClass
     * @param instance - int
     * @param associatedSoInstance - int
     *
     * @return OM_G31R01.DisableResult
     * 
     * @throws InterruptedException
     */
    public OM_G31R01.DisableResult disableRequest(OM_G31R01.Enums.MOClass moClass, int instance, int associatedSoInstance) throws InterruptedException {
    	return disableRequest(moClass, TG_ROUTING, TRXC_ROUTING, instance, associatedSoInstance);
    }
    
    /**
     * @name disableRequest
     * 
     * @description Handles a Disable Request SP
     * 
     * @param moClass - OM_G31R01.Enums.MOClass
     * @param tgId - int
     * @param trxc - int
     * @param instance - int
     * @param associatedSoInstance - int
     *
     * @return OM_G31R01.DisableResult
     * 
     * @throws InterruptedException
     */
    public OM_G31R01.DisableResult disableRequest(OM_G31R01.Enums.MOClass moClass, int tgId, int trxc, int instance, int associatedSoInstance) throws InterruptedException {
        OM_G31R01.DisableRequest disableCmd;

        disableCmd = omServer.createDisableRequest();
        disableCmd.getRouting().setTG(tgId);
        if (isTrxcMo(moClass)) {
        	disableCmd.getRouting().setTRXC(trxc);
        }
        disableCmd.setMOClass(moClass);
        disableCmd.setAssociatedSOInstance(associatedSoInstance);
        disableCmd.setInstanceNumber(instance);

        return disableCmd.send();
    }
    
    /**
     * @name disableRequestAsync
     * 
     * @description Sends Disable Request without waiting for confirmation
     * 
     * @param moClass - OM_G31R01.Enums.MOClass
     */
    public void disableRequestAsync(OM_G31R01.Enums.MOClass moClass) {
        OM_G31R01.DisableRequest disableCmd;

        disableCmd = omServer.createDisableRequest();

        disableCmd.setMOClass(moClass);
        disableCmd.getRouting().setTG(TG_ROUTING);
        if (isTrxcMo(moClass)) {
        	disableCmd.getRouting().setTRXC(TRXC_ROUTING);
        }
        disableCmd.setAssociatedSOInstance(ASSOCIATED_SO_INSTANCE);
        disableCmd.setInstanceNumber(INSTANCE_NUMBER);

        disableCmd.sendAsync();
    }
    
    /**
     * @name enableRequest
     * 
     * @description Handles a Enable Request SP
     * 
     * @param moClass - OM_G31R01.Enums.MOClass
     * @param tgId TODO
     * 
     * @return OM_G31R01.EnableResult
     * 
     * @throws InterruptedException
     */
    public OM_G31R01.EnableResult enableRequest(OM_G31R01.Enums.MOClass moClass, int tgId) throws InterruptedException {
    	return enableRequest(moClass, tgId, TRXC_ROUTING, INSTANCE_NUMBER, ASSOCIATED_SO_INSTANCE);
    }
    
    /**
     * @name enableRequest
     * 
     * @description Handles a Enable Request SP
     * 
     * @param moClass - OM_G31R01.Enums.MOClass
     * @param instance - int
     * @param associatedSoInstance - int
     * 
     * @return OM_G31R01.EnableResult
     * 
     * @throws InterruptedException
     */
    public OM_G31R01.EnableResult enableRequest(OM_G31R01.Enums.MOClass moClass, int instance, int associatedSoInstance) throws InterruptedException {
    	return enableRequest(moClass, TG_ROUTING, TRXC_ROUTING, instance, associatedSoInstance);
    }
    
    /**
     * @name enableRequest
     * 
     * @description Handles a Enable Request SP
     * 
     * @param moClass - OM_G31R01.Enums.MOClass
     * @param tgId - int
     * @param trxc - int
     * @param instance - int
     * @param associatedSoInstance - int
     * 
     * @return OM_G31R01.EnableResult
     * 
     * @throws InterruptedException
     */
    public OM_G31R01.EnableResult enableRequest(OM_G31R01.Enums.MOClass moClass, int tgId, 
            int trxc, int instance, int associatedSoInstance) throws InterruptedException {
        OM_G31R01.EnableRequest enableCmd;

        enableCmd = omServer.createEnableRequest();
        enableCmd.getRouting().setTG(tgId);
        if (isTrxcMo(moClass)) {
        	enableCmd.getRouting().setTRXC(trxc);
        }
        enableCmd.setMOClass(moClass);
        enableCmd.setAssociatedSOInstance(associatedSoInstance);
        enableCmd.setInstanceNumber(instance);

        return enableCmd.send();
    }
    
    /**
     * @name enableRequestAsync
     * 
     * @description Sends Enable Request without waiting for confirmation
     * 
     * @param moClass - OM_G31R01.Enums.MOClass
     */
    public void enableRequestAsync(OM_G31R01.Enums.MOClass moClass) {
        OM_G31R01.EnableRequest enableCmd;

        enableCmd = omServer.createEnableRequest();
        enableCmd.getRouting().setTG(TG_ROUTING);
        if (isTrxcMo(moClass)) {
        	enableCmd.getRouting().setTRXC(TRXC_ROUTING);
        }
        enableCmd.setMOClass(moClass);
        enableCmd.setAssociatedSOInstance(ASSOCIATED_SO_INSTANCE);
        enableCmd.setInstanceNumber(INSTANCE_NUMBER);

        enableCmd.sendAsync();
    }
    
    /**
     * @name clearStartResult
     * 
     * @description Clears the Start Result queue, shall be called before
     *              calling startRequestAsync if StartResult will be fetched
     *              later.
     */
    public void clearStartResult() {

        MessageQueue<OM_G31R01.StartResult> startResultQueue;

        startResultQueue = omServer.getStartResultRespQueue();
        startResultQueue.clear();
    }

    /**
     * @name getStartResult
     * 
     * @description StartResult
     * 
     * @return OM_G31R01.StartResult, null if record is empty
     * 
     * @throws InterruptedException
     */
    public OM_G31R01.StartResult getStartResult() throws InterruptedException{

        MessageQueue<OM_G31R01.StartResult> startResultQueue;

        startResultQueue = omServer.getStartResultRespQueue();

        return startResultQueue.poll(2, TimeUnit.MINUTES);
    }

    /**
     * @name tfConfigRequest
     * 
     * @description Handles TF Configuration Request
     * 
     * @param clusterId
     * @param tfMode
     * @param fsOffset
     * 
     * @return OM_G31R01.TFConfigurationResult
     * 
     * @throws InterruptedException
     */
    public OM_G31R01.TFConfigurationResult tfConfigRequest(int clusterId, Enums.TFMode tfMode, FSOffset fsOffset) throws InterruptedException {

        OM_G31R01.TFConfigurationRequest confReq;
        
        confReq = omServer.createTFConfigurationRequest();
        confReq.setClusterGroupId(clusterId);
        confReq.setTFMode(tfMode);
        confReq.setFSOffset(fsOffset);
        return confReq.send();
    }    
    
    /**
     * @name tfConfigRequest
     * 
     * @description Handles TF Configuration Request
     * 
     * @param clusterId
     * @param fsOffset
     * 
     * @return OM_G31R01.TFConfigurationResult
     * 
     * @throws InterruptedException
     */
    public OM_G31R01.TFConfigurationResult tfConfigRequest(int tgId, int clusterId, FSOffset fsOffset) throws InterruptedException {

        OM_G31R01.TFConfigurationRequest confReq;
        
        confReq = omServer.createTFConfigurationRequest();
        confReq.getRouting().setTG(tgId);
        confReq.setClusterGroupId(clusterId);
        confReq.setFSOffset(fsOffset);
        return confReq.send();
    }    
    
    /**
     * @name tfConfigRequest
     * 
     * @description Handles TF Configuration Request
     * 
     * @param clusterId
     * 
     * @return OM_G31R01.TFConfigurationResult
     * 
     * @throws InterruptedException
     */
    public OM_G31R01.TFConfigurationResult tfConfigRequest(int clusterId) throws InterruptedException {

        OM_G31R01.TFConfigurationRequest confReq;
        
        confReq = omServer.createTFConfigurationRequest();
        confReq.setClusterGroupId(clusterId);
        
        return confReq.send();
    }
    
    public OM_G31R01.TFConfigurationResult tfConfigRequest(int tgId, int clusterId, Enums.TFMode tfMode, FSOffset fsOffset) throws InterruptedException {
    	
    	OM_G31R01.TFConfigurationRequest confReq;
    	
    	confReq = omServer.createTFConfigurationRequest();
    	confReq.getRouting().setTG(tgId);
    	confReq.setClusterGroupId(clusterId);
    	confReq.setTFMode(tfMode);
   		confReq.setFSOffset(fsOffset);

    	return confReq.send();
    }
    
    public OM_G31R01.TFConfigurationResult slaveTfConfigRequest(int tgId, int clusterId) throws InterruptedException {
    	
    	OM_G31R01.TFConfigurationRequest confReq;
    	
    	confReq = omServer.createTFConfigurationRequest();
    	confReq.getRouting().setTG(tgId);
    	confReq.setClusterGroupId(clusterId);
    	confReq.setTFMode(Enums.TFMode.Slave);
    	confReq.setFSOffset(new FSOffset(new Integer(0xFF), new Long(0xFFFFFFFFL)));//undefined for slave

    	return confReq.send();
    }
    
    public OM_G31R01.TFConfigurationResult waitForTfConfigResult(long timeout) throws InterruptedException {

        MessageQueue<OM_G31R01.TFConfigurationResult> ackQ = omServer.getTFConfigurationResultRespQueue();
        ackQ.clear();
        long timeElapsed = 0;
        OM_G31R01.TFConfigurationResult ack = null;

        while (timeElapsed < timeout) {
            ack = ackQ.poll();
            if (ack != null) {
                break;
        }
        timeElapsed += 500;
        Thread.sleep(500); //wait 500 ms
        }
        return ack;
    }

    /**
     * @name initTfConfigRequest
     * 
     * @description Returns an default TF Configuration Request object.
     * 
     * @return OM_G31R01.TFConfigurationRequest
     * 
     */
    public OM_G31R01.TFConfigurationRequest initTfConfigRequest() {
              
        return omServer.createTFConfigurationRequest();
    }
    
    /**
     * @name txConfigRequest
     * 
     * @description Handles TX Configuration Request
     * 
     * @param txAddress - See G31 Abis IWD
     * @param arfcn - See G31 Abis IWD
     * @param mark - FrequencyHopping on/off - See G31 Abis IWD
     * @param power - See G31 Abis IWD
     * @param fillingMarker - C0-Filler on/off - See G31 Abis IWD
     * 
     * 
     * @return OM_G31R01.TFConfigurationResult
     * 
     * @throws InterruptedException
     */
    public OM_G31R01.TXConfigurationResult txConfigRequest(int txAddress, 
            int arfcn, boolean mark, int power, boolean fillingMarker ) throws InterruptedException {
    	return txConfigRequest(TG_ROUTING, TRXC_ROUTING, txAddress, arfcn, mark, power, fillingMarker);
    }
    
    /**
     *
     * @name txConfigRequest
     * 
     * @description Handles TX Configuration Request
     * 
     * @param tgId - int 
     * @param trxc  - int
     * @param txAddress - See G31 Abis IWD
     * @param arfcn - See G31 Abis IWD
     * @param mark - FrequencyHopping on/off - See G31 Abis IWD
     * @param power - See G31 Abis IWD
     * @param fillingMarker - C0-Filler on/off - See G31 Abis IWD
     * 
     * 
     * @return OM_G31R01.TFConfigurationResult
     * 
     * @throws InterruptedException
     */
    public OM_G31R01.TXConfigurationResult txConfigRequest(int tgId, int trxc, int txAddress, 
            int arfcn, boolean mark, int power, boolean fillingMarker ) throws InterruptedException {

        OM_G31R01.TXConfigurationRequest confReq;
        
        confReq = omServer.createTXConfigurationRequest();
        confReq.getRouting().setTG(tgId);
        confReq.getRouting().setTRXC(trxc);
        confReq.setAssociatedSOInstance(0xFF);
        confReq.setInstanceNumber(trxc);
        FrequencySpecifierTX freqSpec = new FrequencySpecifierTX(
                mark ? Mrk.FrequencyHopping : Mrk.ARFCN, txAddress); 
        freqSpec.setARFCN(arfcn);
        confReq.setFrequencySpecifierTX(freqSpec);
        boolean noFrequency = (mark && txAddress == 0 && arfcn == 1023);
        confReq.setPower(noFrequency ? null : new Power(power));
        confReq.setFillingMarker(
                noFrequency ? null : new FillingMarker(fillingMarker ? Enums.FillingMarker.Filling : Enums.FillingMarker.NoFilling));
        confReq.setBCC(noFrequency ? null : new BCC(1));
        
        return confReq.send();
    }
    
    /**
     * @name rxConfigRequest
     * 
     * @description Handles RX Configuration Request
     * 
     * @param rxAddress - See G31 Abis IWD
     * @param arfcn - See G31 Abis IWD
     * @param mark - See G31 Abis IWD
     * 
     * 
     * @return OM_G31R01.TFConfigurationResult
     * 
     * @throws InterruptedException
     */
    public OM_G31R01.RXConfigurationResult rxConfigRequest(int rxAddress, 
            int arfcn, boolean mark, String cellName) throws InterruptedException {
    	return rxConfigRequest(TG_ROUTING, TRXC_ROUTING, INSTANCE_NUMBER, rxAddress, arfcn, mark, cellName);
    }
    
    /**
     * 
     * @name rxConfigRequest
     * 
     * @description Handles RX Configuration Request
     * 
     * @param tgId - int
     * @param trxc - int
     * @param instanceNumber - int
     * @param rxAddress - See G31 Abis IWD
     * @param arfcn - See G31 Abis IWD
     * @param mark - See G31 Abis IWD
     * @param cellName - See G31 Abis IWD
     * 
     * 
     * @return OM_G31R01.TFConfigurationResult
     * 
     * @throws InterruptedException
     */
    public OM_G31R01.RXConfigurationResult rxConfigRequest(int tgId, int trxc, int instanceNumber, int rxAddress, 
            int arfcn, boolean mark, String cellName) throws InterruptedException {

        OM_G31R01.RXConfigurationRequest confReq;
        
        confReq = omServer.createRXConfigurationRequest();
        confReq.getRouting().setTG(tgId);
        confReq.getRouting().setTRXC(trxc);
        confReq.setInstanceNumber(instanceNumber);
        confReq.setAssociatedSOInstance(0xFF);
        FrequencySpecifierRX freqSpec = new FrequencySpecifierRX(
                mark ? Mrk.FrequencyHopping : Mrk.ARFCN, rxAddress); 
        freqSpec.setARFCN(arfcn);
        confReq.setFrequencySpecifierRX(freqSpec);
        confReq.setCellName(cellName);

        return confReq.send();
    }
    
    /**
     * @name atConfigRequest
     * 
     * @description Handles AT Configuration Request
     * 
     * @param 
     * @param 
     * @param 
     * 
     * 
     * @return OM_G31R01.ATConfigurationResult
     * 
     * @throws InterruptedException
     */
    public OM_G31R01.ATConfigurationResult atConfigRequest(int sapiList1, int sapiList2, int sapiList3, int dscp1, int dscp2, int dscp3) throws InterruptedException {

        OM_G31R01.ATConfigurationRequest confReq = omServer.createATConfigurationRequest();
        confReq.setJitterSize(20);
        OM_G31R01.PackingAlgorithm pa = new OM_G31R01.PackingAlgorithm(OM_G31R01.Enums.PackingAlgorithm.PackingAlgorithm1);
        confReq.setPackingAlgorithm(pa);
        List<Integer> transportProfilesOctets = new ArrayList<Integer>();
        //transportProfilesOctets.add(0x7D); //Transport Profiles DEI
        //transportProfilesOctets.add(0xB); // length, @TODO FIX ME
        transportProfilesOctets.add(0x3);  // number of profiles
        transportProfilesOctets.add(0xA);  //length of profile 1 including this octet
        transportProfilesOctets.add(sapiList1); //SAPI OML & RSL
        transportProfilesOctets.add(0x1);  //bundling time
        transportProfilesOctets.add(0x5);  //max payload size high octet
        transportProfilesOctets.add(0xB9); //max payload size low octet
        transportProfilesOctets.add(0x1);  //CRC true
        transportProfilesOctets.add(dscp1); //DSCP
        transportProfilesOctets.add(0x0);  //overload threshold high octet
        transportProfilesOctets.add(0x0);  //overload threshold low octet
        transportProfilesOctets.add(0x1);  //overload report interval
        transportProfilesOctets.add(0xA);  //length of profile 2 including this octet
        transportProfilesOctets.add(sapiList2);  //SAPI CS & CSD
        transportProfilesOctets.add(0x1);  //bundling time
        transportProfilesOctets.add(0x5);  //max payload size high octet
        transportProfilesOctets.add(0xB9); //max payload size low octet
        transportProfilesOctets.add(0x1);  //CRC true
        transportProfilesOctets.add(dscp2); //DSCP
        transportProfilesOctets.add(0x0);  //overload threshold high octet
        transportProfilesOctets.add(0x0);  //overload threshold low octet
        transportProfilesOctets.add(0x1);  //overload report interval
        transportProfilesOctets.add(0xA);  //length of profile 3 including this octet
        transportProfilesOctets.add(sapiList3);  //SAPI PS
        transportProfilesOctets.add(0x1);  //bundling time
        transportProfilesOctets.add(0x5);  //max payload size high octet
        transportProfilesOctets.add(0xB9); //max payload size low octet
        transportProfilesOctets.add(0x1);  //CRC true
        transportProfilesOctets.add(dscp3); //DSCP
        transportProfilesOctets.add(0x0);  //overload threshold high octet
        transportProfilesOctets.add(0x0);  //overload threshold low octet
        transportProfilesOctets.add(0x1);  //overload report interval
        confReq.setTransportProfiles(new TransportProfiles(transportProfilesOctets));
        return confReq.send();
    }
    
    /**
     * @name atConfigRequest
     * 
     * @description Handles AT Configuration Request
     * @param tgId TODO
     * @param The instance profile that shall be sent in the AT Config Req
     * 
     * @return OM_G31R01.ATConfigurationResult
     * 
     * @throws InterruptedException
     */
    public OM_G31R01.ATConfigurationResult atConfigRequest(AbisHelper.TransportProfile tp, int tgId) throws InterruptedException {

        OM_G31R01.ATConfigurationRequest confReq = omServer.createATConfigurationRequest();
        confReq.getRouting().setTG(tgId);
        confReq.setJitterSize(20);
        OM_G31R01.PackingAlgorithm pa = new OM_G31R01.PackingAlgorithm(OM_G31R01.Enums.PackingAlgorithm.PackingAlgorithm1);
        confReq.setPackingAlgorithm(pa);
        confReq.setTransportProfiles(new TransportProfiles(tp.toIntegerList()));
        return confReq.send();
    }
    
    
    /**
     * @name atBundlingInfoRequest
     * 
     * @description Handles AT Bundling Info Request
     * 
     * @param 
     * @param 
     * @param 
     * 
     * 
     * @return OM_G31R01.ATBundlingInfoResult
     * 
     * @throws InterruptedException
     */
    public OM_G31R01.ATBundlingInfoResponse atBundlingInfoRequest(int listNumber, int tgId) throws InterruptedException {
    	OM_G31R01.ATBundlingInfoRequest req= omServer.createATBundlingInfoRequest();
    	req.getRouting().setTG(tgId);
    	req.setListNumber(new ListNumber(listNumber));
    	return req.send();
    }

    /**
     * @name scfCapabilitiesExchangeRequest
     * 
     * @description Handles SCF Capabilities Exchange Request
     * 
     * @param tgId      	        	- The TG this request shall be sent to
     * @param bscCapabilitySignature    - The Bsc Capability signature to send
     * 
     * 
     * @return OM_G31R01.CapabilitiesExchangeResult
     * 
     * @throws InterruptedException
     */
    public OM_G31R01.CapabilitiesExchangeResult scfCapabilitiesExchangeRequest(int tgId, Integer bscCapSignature) throws InterruptedException {

    	OM_G31R01.CapabilitiesExchangeRequest scfCapabilitiesExchangeRequest = omServer.createCapabilitiesExchangeRequest();
    	
    	scfCapabilitiesExchangeRequest.getRouting().setTG(tgId);
    	scfCapabilitiesExchangeRequest.setMOClass(OM_G31R01.Enums.MOClass.SCF);
    	scfCapabilitiesExchangeRequest.setAssociatedSOInstance(0xFF);
    	scfCapabilitiesExchangeRequest.setInstanceNumber(INSTANCE_NUMBER);
    	List<Integer> bscRecordData = new ArrayList<Integer>();
    	bscRecordData.add(0x0B); //BSC Record
    	bscRecordData.add(0x07); //length of remainder
    	bscRecordData.add(0x01); //BSC Supported Functions high octet
    	bscRecordData.add(0xFE); //BSC Supported Functions low octet
    	bscRecordData.add(0x00); //length of BSC Supported Functions
    	bscRecordData.add(0x02); //Number of TRXC per TG high octet
    	bscRecordData.add(0xFE); //Number of TRXC per TG low octet
    	bscRecordData.add(0x01); //length of MO Record
    	bscRecordData.add(0x0C); //12 TRXC per TG
    	BSCRecord bscRecord = new BSCRecord(bscRecordData);
    	scfCapabilitiesExchangeRequest.setBSCRecord(bscRecord);
    	scfCapabilitiesExchangeRequest.setBSCCapabilitiesSignature(bscCapSignature);

        return scfCapabilitiesExchangeRequest.send();
    }
    
    /**
     * @name trxcCapabilitiesExchangeRequest
     * 
     * @description Handles TRXC Capabilities Exchange Request
     * 
     * @param tgId     - The TG this request shall be sent to
     * 
     * 
     * @return OM_G31R01.CapabilitiesExchangeResult
     * 
     * @throws InterruptedException
     */
    public OM_G31R01.CapabilitiesExchangeResult trxcCapabilitiesExchangeRequest(int tgId) throws InterruptedException {

        OM_G31R01.CapabilitiesExchangeRequest trxcCapabilitiesExchangeRequest = omServer.createCapabilitiesExchangeRequest();
        
        trxcCapabilitiesExchangeRequest.getRouting().setTG(tgId);
        trxcCapabilitiesExchangeRequest.getRouting().setTRXC(TRXC_ROUTING);
        trxcCapabilitiesExchangeRequest.setMOClass(OM_G31R01.Enums.MOClass.TRXC);
        trxcCapabilitiesExchangeRequest.setAssociatedSOInstance(0xFF);
        trxcCapabilitiesExchangeRequest.setInstanceNumber(INSTANCE_NUMBER);
        return trxcCapabilitiesExchangeRequest.send();
    }
  
    /**
     * @name radioChannelsReleaseCommand
     * 
     * @description Handles Radio Channels Release Command
     * 
     * @return M_G31R01.RadioChannelsReleaseComplete
     * 
     * @throws InterruptedException
     */
     public OM_G31R01.RadioChannelsReleaseComplete radioChannelsReleaseCommand(OM_G31R01.Enums.MOClass moClass, int instance, int associatedSoInstance) throws InterruptedException {
       OM_G31R01.RadioChannelsReleaseCommand radioChannelsReleaseCmd;

       radioChannelsReleaseCmd = omServer.createRadioChannelsReleaseCommand();
       radioChannelsReleaseCmd.setMOClass(moClass);
       radioChannelsReleaseCmd.setInstanceNumber(instance);
       radioChannelsReleaseCmd.setAssociatedSOInstance(associatedSoInstance);

      return radioChannelsReleaseCmd.send();
    }
    
    /**
     * @name buildCombinationMainBCCHCombined
     * 
     * @description Builds a MainBCCHCombined combination with all parameters set based on an input random seed
     * 
     * @param seedD - used as random seed for parameters only allowed to be set in state DISABLED
     * @param seedE - used as random seed for parameters also allowed to be changed in state ENABLED

     * @param setOptionalParams - boolean
     * 
     * @return OM_G31R01.CombinationMainBCCHCombined
     * 
     * @throws InterruptedException
     */
    public OM_G31R01.CombinationMainBCCHCombined buildCombinationMainBCCHCombined(int seedD, int seedE, boolean setOptionalParams) throws InterruptedException {

    	boolean boolSeedD = ((seedD % 2) == 0);  // used for parameters only allowed to be changed in state DISABLED
    	boolean boolSeedE = ((seedE % 2) == 0);  // used for parameters also allowed to be changed in state ENABLED

    	OM_G31R01.CombinationMainBCCHCombined mainBCCHcombined = new OM_G31R01.CombinationMainBCCHCombined();

    	/*
    	 * Set all parameters mandatory for combination: Main BCCH combined
    	 */    	
    	// T3105: 02 - FE
        mainBCCHcombined.setT3105(2+(seedD%13));
        
    	// Ny1: 00 - FE
    	mainBCCHcombined.setNy1(seedD%15);
 
    	// BS_PA_MFRMS: 2-9
    	mainBCCHcombined.setBS_PA_MFRMS(new OM_G31R01.BS_PA_MFRMS((seedD%8)+2));
    	
    	// CBCH Indicator: 0/1
    	mainBCCHcombined.setCBCHIndicator(new OM_G31R01.CBCHIndicator(boolSeedD ? Enums.CBCHIndicator.NoCBCH : Enums.CBCHIndicator.CBCH));
    	
    	// BS_AG_BLKS_RES: 0-2 
    	mainBCCHcombined.setBS_AG_BLKS_RES(new OM_G31R01.BS_AG_BLKS_RES(seedD%3)); 

        // TSC: 0-7
    	mainBCCHcombined.setTSC(new OM_G31R01.TSC(seedD%8));

        // ICM Indicator: 0/1
    	mainBCCHcombined.setICMIndicator(new OM_G31R01.ICMIndicator(boolSeedD ? Enums.ICM.Off : Enums.ICM.On));

    	// DRX_DEV_MAX: 0-100, this parameter is allowed to be changed in ENABLED state
    	mainBCCHcombined.setDRX_DEV_MAX(new OM_G31R01.DRX_DEV_MAX(seedE%101));

    	// CCCH Options: this parameter is allowed to be changed in ENABLED state
    	OM_G31R01.CCCHOptions ccchOptions = new OM_G31R01.CCCHOptions();
    	// CCCH Repeat parameter: 0/1
    	ccchOptions.setCCCHRepeat(boolSeedE ? Enums.CCCHRepeat.Repeat : Enums.CCCHRepeat.NoRepeat);
    	// Inhibit Paging Request type 3: 0/1
    	ccchOptions.setInhibitPagingRequestType3(boolSeedE ? 
    			Enums.InhibitPagingRequestType3.No :
    				Enums.InhibitPagingRequestType3.Yes);
    	// Age of Paging: 0,1-15
    	ccchOptions.setAgeOfPaging(seedE%16);
    	// Paging Improvements Control: 0/1
    	ccchOptions.setPagingImprovementsControl(boolSeedE ? 
    			Enums.PagingImprovementsControl.FNAdded :
    				Enums.PagingImprovementsControl.FNNotAdded);    	
    	// Reporting of Access Burst Info: 0/1 
    	// TODO uabmoda: seams not possible to set "Reporting of Access Burst Info", check if it is needed...?
    	mainBCCHcombined.setCCCHOptions(ccchOptions);
   	
       	// ICM Boundary Parameters: 0-62,  this parameter is allowed to be changed in ENABLED state
        OM_G31R01.ICMBoundaryParameters icmBoundaryParameters = new OM_G31R01.ICMBoundaryParameters();
        icmBoundaryParameters.setICMBoundaryParameter1(seedE%60);
        icmBoundaryParameters.setICMBoundaryParameter2((seedE%60)+1);
        icmBoundaryParameters.setICMBoundaryParameter3((seedE%60)+2);
        icmBoundaryParameters.setICMBoundaryParameter4((seedE%60)+3);          
        // Averaging Period: 1-31
        icmBoundaryParameters.setAveragingPeriod(1 + (seedE % 31));    
        mainBCCHcombined.setICMBoundaryParameters(icmBoundaryParameters);  
   	
    	/*
    	 * Set all parameters optional for combination Main BCCH
    	 */
    	if (setOptionalParams)
    	{
    		// Interference Rejection Combining, this parameter is allowed to be changed in ENABLED state
    		mainBCCHcombined.setInterferenceRejectionCombining(new OM_G31R01.InterferenceRejectionCombining(boolSeedE ? 
    				Enums.InterferenceRejectionCombining.Off :
    					Enums.InterferenceRejectionCombining.On));
    	}
    	return mainBCCHcombined;
    }
    
    /**
     * @name buildCombinationMainBCCH
     * 
     * @description Builds a MainBCCH combination with all parameters set based on an input random seed
     * 
     * @param seedD - used as random seed for parameters only allowed to be set in state DISABLED
     * @param seedE - used as random seed for parameters also allowed to be changed in state ENABLED
     * 
     * @param setOptionalParams - boolean
     * 
     * @return OM_G31R01.CombinationMainBCCH
     * 
     * @throws InterruptedException
     */
    public OM_G31R01.CombinationMainBCCH buildCombinationMainBCCH(int seedD, int seedE, boolean setOptionalParams) throws InterruptedException {

    	boolean boolSeedE = ((seedE % 2) == 0);  // used for parameters also allowed to be changed in state ENABLED

    	OM_G31R01.CombinationMainBCCH mainBcchComb = new OM_G31R01.CombinationMainBCCH();

    	/*
    	 * Set all parameters mandatory for combination Main BCCH
    	 */    	
    	// BS_PA_MFRMS: 2-9
    	mainBcchComb.setBS_PA_MFRMS(new OM_G31R01.BS_PA_MFRMS((seedD%8)+2));
    	
    	// BS_AG_BLKS_RES: 0-2
    	mainBcchComb.setBS_AG_BLKS_RES(new OM_G31R01.BS_AG_BLKS_RES(seedD%3));

    	// DRX_DEV_MAX: 0-100, this parameter is allowed to be changed in ENABLED state
    	mainBcchComb.setDRX_DEV_MAX(new OM_G31R01.DRX_DEV_MAX(seedE%101));

    	// CCCH Options: this parameter is allowed to be changed in ENABLED state
    	OM_G31R01.CCCHOptions ccchOptions = new OM_G31R01.CCCHOptions();
    	// CCCH Repeat parameter: 0/1
    	ccchOptions.setCCCHRepeat(boolSeedE ? Enums.CCCHRepeat.Repeat : Enums.CCCHRepeat.NoRepeat);
    	// Inhibit Paging Request type 3: 0/1
    	ccchOptions.setInhibitPagingRequestType3(boolSeedE ? 
    			Enums.InhibitPagingRequestType3.No :
    				Enums.InhibitPagingRequestType3.Yes);
    	// Age of Paging: 0,1-15
    	ccchOptions.setAgeOfPaging(seedE%16);
    	// Paging Improvements Control: 0/1
    	ccchOptions.setPagingImprovementsControl(boolSeedE ? 
    			Enums.PagingImprovementsControl.FNAdded :
    				Enums.PagingImprovementsControl.FNNotAdded);
    	// Reporting of Access Burst Info: 0/1
    	// TODO uabmoda: seams not possible to set "Reporting of Access Burst Info", check if it is needed...?
    	mainBcchComb.setCCCHOptions(ccchOptions);
    	
    	/*
    	 * Set all parameters optional for combination Main BCCH
    	 */
    	if (setOptionalParams)
    	{
    		// Interference Rejection Combining, this parameter is allowed to be changed in ENABLED state
    		mainBcchComb.setInterferenceRejectionCombining(new OM_G31R01.InterferenceRejectionCombining(boolSeedE ? 
    				Enums.InterferenceRejectionCombining.Off :
    					Enums.InterferenceRejectionCombining.On));
    	}
    	return mainBcchComb;
    }
    
    private CombinationMainBCCH buildCombinationMainBCCHFirstCall() {
        OM_G31R01.CombinationMainBCCH mainBcchComb = new OM_G31R01.CombinationMainBCCH();

        /*
         * Set all parameters mandatory for combination Main BCCH
         */     
        // BS_PA_MFRMS: 2-9
        mainBcchComb.setBS_PA_MFRMS(new OM_G31R01.BS_PA_MFRMS(3));
        
        // BS_AG_BLKS_RES: 0-2
        mainBcchComb.setBS_AG_BLKS_RES(new OM_G31R01.BS_AG_BLKS_RES(1));

        // DRX_DEV_MAX: 0-100, this parameter is allowed to be changed in ENABLED state
        mainBcchComb.setDRX_DEV_MAX(new OM_G31R01.DRX_DEV_MAX(1));

        // CCCH Options: this parameter is allowed to be changed in ENABLED state
        OM_G31R01.CCCHOptions ccchOptions = new OM_G31R01.CCCHOptions();
        // CCCH Repeat parameter: 0/1
        ccchOptions.setCCCHRepeat(Enums.CCCHRepeat.NoRepeat);
        // Inhibit Paging Request type 3: 0/1
        ccchOptions.setInhibitPagingRequestType3(Enums.InhibitPagingRequestType3.Yes);
        // Age of Paging: 0,1-15
        ccchOptions.setAgeOfPaging(1);
        // Paging Improvements Control: 0/1
        ccchOptions.setPagingImprovementsControl(Enums.PagingImprovementsControl.FNNotAdded);
        
        mainBcchComb.setCCCHOptions(ccchOptions);
       
        return mainBcchComb;
    }

    /**
     * @name buildCombinationOptionalBCCH
     * 
     * @description Builds a OptionalBCCH combination with all parameters set based on an input random seed
     * 
     * @param seedD - used as random seed for parameters only allowed to be set in state DISABLED
     * @param seedE - used as random seed for parameters also allowed to be changed in state ENABLED
     * 
     * @param setOptionalParams - boolean
     * 
     * @return OM_G31R01.CombinationOptionalBCCH
     * 
     * @throws InterruptedException
     */
    public OM_G31R01.CombinationOptionalBCCH buildCombinationOptionalBCCH(int seedD, int seedE, boolean setOptionalParams) throws InterruptedException {

        boolean boolSeedE = ((seedE % 2) == 0);  // used for parameters also allowed to be changed in state ENABLED

        OM_G31R01.CombinationOptionalBCCH optionalBcchComb = new OM_G31R01.CombinationOptionalBCCH();

        /*
         * Set all parameters mandatory for combination Optional BCCH
         */     
        // BS_PA_MFRMS: 2-9
        optionalBcchComb.setBS_PA_MFRMS(new OM_G31R01.BS_PA_MFRMS((seedD%8)+2));
        
        // BS_AG_BLKS_RES: 0-2
        optionalBcchComb.setBS_AG_BLKS_RES(new OM_G31R01.BS_AG_BLKS_RES(seedD%3));

        // DRX_DEV_MAX: 0-100, this parameter is allowed to be changed in ENABLED state
        optionalBcchComb.setDRX_DEV_MAX(new OM_G31R01.DRX_DEV_MAX(seedE%101));

        // CCCH Options: this parameter is allowed to be changed in ENABLED state
        OM_G31R01.CCCHOptions ccchOptions = new OM_G31R01.CCCHOptions();
        // CCCH Repeat parameter: 0/1
        ccchOptions.setCCCHRepeat(boolSeedE ? Enums.CCCHRepeat.Repeat : Enums.CCCHRepeat.NoRepeat);
        // Inhibit Paging Request type 3: 0/1
        ccchOptions.setInhibitPagingRequestType3(boolSeedE ? 
                Enums.InhibitPagingRequestType3.No :
                    Enums.InhibitPagingRequestType3.Yes);
        // Age of Paging: 0,1-15
        ccchOptions.setAgeOfPaging(seedE%16);
        // Paging Improvements Control: 0/1
        ccchOptions.setPagingImprovementsControl(boolSeedE ? 
                Enums.PagingImprovementsControl.FNAdded :
                    Enums.PagingImprovementsControl.FNNotAdded);
        // Reporting of Access Burst Info: 0/1
        // TODO uabmoda: seams not possible to set "Reporting of Access Burst Info", check if it is needed...?
        optionalBcchComb.setCCCHOptions(ccchOptions);
        
        /*
         * Set all parameters optional for combination Optional BCCH
         */
        if (setOptionalParams)
        {
            // Interference Rejection Combining, this parameter is allowed to be changed in ENABLED state
            optionalBcchComb.setInterferenceRejectionCombining(new OM_G31R01.InterferenceRejectionCombining(boolSeedE ? 
                    Enums.InterferenceRejectionCombining.Off :
                        Enums.InterferenceRejectionCombining.On));
        }
        return optionalBcchComb;
    }
    
    /**
     * @name buildCombinationSDCCH
     * 
     * @description Builds a SDCCH combination with all parameters set based on an input random seed
     * 
     * @param seedD - used as random seed for parameters only allowed to be set in state DISABLED
     * @param seedE - used as random seed for parameters also allowed to be changed in state ENABLED

     * @param setOptionalParams - boolean
     * 
     * @return OM_G31R01.CombinationSDCCH
     * 
     * @throws InterruptedException
     */
    public OM_G31R01.CombinationSDCCH buildCombinationSDCCH(int seedD, int seedE, boolean setOptionalParams, int tn) throws InterruptedException {

        boolean boolSeedD = ((seedD % 2) == 0);  // used for parameters only allowed to be changed in state DISABLED
        boolean boolSeedE = ((seedE % 2) == 0);  // used for parameters also allowed to be changed in state ENABLED

        OM_G31R01.CombinationSDCCH sdcch = new OM_G31R01.CombinationSDCCH();

        /*
         * Set all parameters mandatory for combination: SDCCH
         */     
        // T3105: 02 - FE
        sdcch.setT3105(2+(seedD%13));
        
        // Ny1: 00 - FE
        sdcch.setNy1(seedD%15);
        
        // CBCH Indicator: 0/1 Only allowed to be set if TN = 0..3
        if(tn<4) 
        {
          sdcch.setCBCHIndicator(new OM_G31R01.CBCHIndicator(boolSeedD ? Enums.CBCHIndicator.NoCBCH : Enums.CBCHIndicator.CBCH));
        }
        else
        {
            sdcch.setCBCHIndicator(new OM_G31R01.CBCHIndicator(Enums.CBCHIndicator.NoCBCH));
        }
        
        // TSC: 0-7
        sdcch.setTSC(new OM_G31R01.TSC(seedD%8));

        // ICM Indicator: 0/1
        sdcch.setICMIndicator(new OM_G31R01.ICMIndicator(boolSeedD ? Enums.ICM.Off : Enums.ICM.On));
    
        // ICM Boundary Parameters: 0-62,  this parameter is allowed to be changed in ENABLED state
        OM_G31R01.ICMBoundaryParameters icmBoundaryParameters = new OM_G31R01.ICMBoundaryParameters();
        icmBoundaryParameters.setICMBoundaryParameter1(seedE%60);
        icmBoundaryParameters.setICMBoundaryParameter2((seedE%60)+1);
        icmBoundaryParameters.setICMBoundaryParameter3((seedE%60)+2);
        icmBoundaryParameters.setICMBoundaryParameter4((seedE%60)+3);          
        // Averaging Period: 1-31
        icmBoundaryParameters.setAveragingPeriod(1 + (seedE % 31));    
        sdcch.setICMBoundaryParameters(icmBoundaryParameters);  
    
        /*
         * Set all parameters optional for combination SDCCH
         */
        if (setOptionalParams)
        {
            // Interference Rejection Combining, this parameter is allowed to be changed in ENABLED state
            sdcch.setInterferenceRejectionCombining(new OM_G31R01.InterferenceRejectionCombining(boolSeedE ? 
                    Enums.InterferenceRejectionCombining.Off :
                        Enums.InterferenceRejectionCombining.On));
        }
        return sdcch;
    }  
    
    private CombinationSDCCH buildCombinationSDCCHFirstCall() {
        OM_G31R01.CombinationSDCCH sdcch = new OM_G31R01.CombinationSDCCH();

        /*
         * Set all parameters mandatory for combination: SDCCH
         */     
        // T3105: 02 - FE
        sdcch.setT3105(3);
        
        // Ny1: 00 - FE
        sdcch.setNy1(1);

        sdcch.setCBCHIndicator(new OM_G31R01.CBCHIndicator(Enums.CBCHIndicator.NoCBCH));
                
        // TSC: 0-7
        sdcch.setTSC(new OM_G31R01.TSC(1));

        // ICM Indicator: 0/1
        sdcch.setICMIndicator(new OM_G31R01.ICMIndicator(Enums.ICM.On));
    
        // ICM Boundary Parameters: 0-62,  this parameter is allowed to be changed in ENABLED state
        OM_G31R01.ICMBoundaryParameters icmBoundaryParameters = new OM_G31R01.ICMBoundaryParameters();
        icmBoundaryParameters.setICMBoundaryParameter1(1);
        icmBoundaryParameters.setICMBoundaryParameter2(2);
        icmBoundaryParameters.setICMBoundaryParameter3(3);
        icmBoundaryParameters.setICMBoundaryParameter4(4);          
        // Averaging Period: 1-31
        icmBoundaryParameters.setAveragingPeriod(2);    
        sdcch.setICMBoundaryParameters(icmBoundaryParameters);  

        return sdcch;
    }
    
    /**
     * @name buildCombinationTCH
     * 
     * @description Builds a TCH combination with all parameters set based on an input random seed
     * 
     * @param seedD - used as random seed for parameters only allowed to be set in state DISABLED
     * @param seedE - used as random seed for parameters also allowed to be changed in state ENABLED
     * @param setOptionalParams - boolean 
     * 
     * @return OM_G31R01.CombinationTCH
     * 
     * @throws InterruptedException
     */
    public OM_G31R01.CombinationTCH buildCombinationTCH(int seedD, int seedE, boolean setOptionalParams) throws InterruptedException {
 
    	boolean boolSeedD = ((seedD % 2) == 0);
    	boolean boolSeedE = ((seedE % 2) == 0);
        
        OM_G31R01.CombinationTCH tchComb = new OM_G31R01.CombinationTCH();
        
        /*
         * First set all parameters mandatory for combination TCH
         */
    	// T3105: 02 - FE, this parameter is allowed to be changed in ENABLED state
        tchComb.setT3105(2+(seedE%13));
        
    	// Ny1: 00 - FE, this parameter is allowed to be changed in ENABLED state
    	tchComb.setNy1(seedE%15);
    	
        // TSC: 0-7
    	tchComb.setTSC(new OM_G31R01.TSC(seedD%8));
    	
        // ICM Indicator: 0/1, this parameter is allowed to be changed in ENABLED state
    	tchComb.setICMIndicator(new OM_G31R01.ICMIndicator(boolSeedE ? Enums.ICM.Off : Enums.ICM.On));
    	
    	// ICM Boundary Parameters: 0-62, this parameter is allowed to be changed in ENABLED state
        OM_G31R01.ICMBoundaryParameters icmBoundaryParameters = new OM_G31R01.ICMBoundaryParameters();
        icmBoundaryParameters.setICMBoundaryParameter1(seedE%60);
        icmBoundaryParameters.setICMBoundaryParameter2((seedE%60)+1);  // ICM Boundary parameter n+1 must be > ICM Boundary parameter n
        icmBoundaryParameters.setICMBoundaryParameter3((seedE%60)+2);
        icmBoundaryParameters.setICMBoundaryParameter4((seedE%60)+3);          
        // Averaging Period: 1-31
        icmBoundaryParameters.setAveragingPeriod(1 + (seedE % 31));    
        tchComb.setICMBoundaryParameters(icmBoundaryParameters);  
        
       	// TTA: 2-50
   	 	tchComb.setTTA(2 + (seedD%49));
   	 
        // LinkSupervisionControl: 0/1
        tchComb.setLinkSupervisionControl(new OM_G31R01.LinkSupervisionControl(boolSeedD ? Enums.LSA.Off : Enums.LSA.On));
        
        /*
         * Set all parameters optional for combination choice TCH
         */
        if (setOptionalParams)
        {
        	// Interference Rejection Combining, this parameter is allowed to be changed in ENABLED state
        	tchComb.setInterferenceRejectionCombining(new OM_G31R01.InterferenceRejectionCombining(boolSeedE ? 
        			Enums.InterferenceRejectionCombining.Off :
        			Enums.InterferenceRejectionCombining.On));
            
            // ICM Channel Rate,  TCH_F (=0) or TCH_H (=1)
        	tchComb.setICMChannelRate(boolSeedD ?
        		Enums.ICMChannelRate.TCH_F :
        		Enums.ICMChannelRate.TCH_H	); 
        	
        	// Link Supervision Filtering Time: 10-200
            tchComb.setLinkSupervisionFilteringTime(10+(seedD%190));
        	
            // Call supervision Time: 0, 4, 8...64
            tchComb.setCallSupervisionTime((seedD%17)*4);
            
            // TODO: set VAMOS parameters when supported in src
        }
        return tchComb;
    }
    /**
     * @param setParamsToDefaultValue
     * @return
     * @throws InterruptedException
     */
    public OM_G31R01.CombinationTCH buildCombinationTCHFirstCall(boolean setParamsToDefaultValue) throws InterruptedException {
        OM_G31R01.CombinationTCH tchComb = new OM_G31R01.CombinationTCH();
        
        /*
         * First set all parameters mandatory for combination TCH
         */
        // T3105: 02 - FE, this parameter is allowed to be changed in ENABLED state
        tchComb.setT3105(4);
        
        // Ny1: 00 - FE, this parameter is allowed to be changed in ENABLED state
        tchComb.setNy1(2);
        
        // TSC: 0-7
        tchComb.setTSC(new OM_G31R01.TSC(1));
        
        // ICM Indicator: 0/1, this parameter is allowed to be changed in ENABLED state
        tchComb.setICMIndicator(new OM_G31R01.ICMIndicator(Enums.ICM.Off));
        
        // ICM Boundary Parameters: 0-62, this parameter is allowed to be changed in ENABLED state
        OM_G31R01.ICMBoundaryParameters icmBoundaryParameters = new OM_G31R01.ICMBoundaryParameters();
        icmBoundaryParameters.setICMBoundaryParameter1(2);
        icmBoundaryParameters.setICMBoundaryParameter2(3);  // ICM Boundary parameter n+1 must be > ICM Boundary parameter n
        icmBoundaryParameters.setICMBoundaryParameter3(4);
        icmBoundaryParameters.setICMBoundaryParameter4(5);          
        // Averaging Period: 1-31
        icmBoundaryParameters.setAveragingPeriod(3);    
        tchComb.setICMBoundaryParameters(icmBoundaryParameters);  
        
        // TTA: 2-50
        tchComb.setTTA(4);
     
        // LinkSupervisionControl: 0/1
        tchComb.setLinkSupervisionControl(new OM_G31R01.LinkSupervisionControl(Enums.LSA.Off));
        
        if (setParamsToDefaultValue) {
            tchComb.setLinkSupervisionFilteringTime(30); //set to default value
        }
        return tchComb;
    }
    

    /**
     * @name buildTsConfigRequestFirstCall
     * 
     * @description Builds a TS Configuration Request with a working First call config
     * 
     * @param tsInstance - int 0-7
     * @param timeSlotNumber -
     * @param combination - 
     * @param setParamsToDefaultValue 
     * 
     * @return OM_G31R01.TSConfigurationResult
     * 
     * @throws InterruptedException
     */
    
    public OM_G31R01.TSConfigurationRequest buildTsConfigRequestFirstCall(int tsInstance, OM_G31R01.Enums.Combination combination, boolean setParamsToDefaultValue ) throws InterruptedException {
        Map<Integer, Integer> frequencyList = new HashMap<Integer, Integer>();
        frequencyList.put(0, momHelper.getArfcnToUse());
        return buildTsConfigRequestFirstCall(tsInstance, 0, combination, frequencyList, setParamsToDefaultValue); // trxcId = 0
    }
    
    /**
     * @name buildTsConfigRequestFirstCall
     * 
     * @description Builds a TS Configuration Request with a working First call config
     * 
     * @param tsInstance - int 0-7
     * @param trxcId - int
     * @param combination - 
     * @param setParamsToDefaultValue 
     * 
     * @return OM_G31R01.TSConfigurationResult
     * 
     * @throws InterruptedException
     */
    
    public OM_G31R01.TSConfigurationRequest buildTsConfigRequestFirstCall(int tsInstance, int trxcId, OM_G31R01.Enums.Combination combination, boolean setParamsToDefaultValue ) throws InterruptedException {
        Map<Integer, Integer> frequencyList = new HashMap<Integer, Integer>();
        frequencyList.put(0, momHelper.getArfcnToUse());
        return buildTsConfigRequestFirstCall(tsInstance, trxcId, combination, frequencyList, setParamsToDefaultValue);
    }   

    
    /**
     * @name buildTsConfigRequestFirstCall
     * 
     * @description Builds a TS Configuration Request with a working First call config
     * 
     * @param tsInstance - int 0-7
     * @param trxcId - int
     * @param timeSlotNumber -
     * @param combination - 
     * @param setParamsToDefaultValue 
     * 
     * @return OM_G31R01.TSConfigurationResult
     * 
     * @throws InterruptedException
     */
    public OM_G31R01.TSConfigurationRequest buildTsConfigRequestFirstCall(int tsInstance,
    		int trxcId,
            OM_G31R01.Enums.Combination combination, 
            Map<Integer, Integer> frequencyList,
            boolean setParamsToDefaultValue ) throws InterruptedException {
            //int arfcn = momHelper.getArfcnToUse();
        
        //int freqListTxAdress = 0;
        //int freqListRxAdress = 0;
        //int freqListBfmArfcnHiP = (arfcn >> 8) | 4;
        //int freqListArfcnLowP = arfcn & 255;

        OM_G31R01.TSConfigurationRequest confReq;        
        confReq = omServer.createTSConfigurationRequest();
        confReq.getRouting().setTG(TG_ROUTING);
        confReq.getRouting().setTRXC(trxcId);
        confReq.setInstanceNumber(tsInstance);
        confReq.setAssociatedSOInstance(trxcId);       
        
        // HSN: 0-63
        confReq.setHSN(new OM_G31R01.HSN(0));
        
        // MAIO: 0-63
        confReq.setMAIO(new OM_G31R01.MAIO(0));
        
        // BSIC, BCC: 0-7, PLMN: 0-7
        confReq.setBSIC(new OM_G31R01.BSIC(1, 7));
        
        // skip FN Offset - all TN must have the same FN offset, 
        // ie. this parameter is only possible to change in state RESET, so it will be tested elsewhere...
        
        // Extended Range Indicator, 0 - 1
        confReq.setExtendedRangeIndicator(new OM_G31R01.ExtendedRangeIndicator(Enums.ExtendedRangeIndicator.Normal));
        confReq.setTimeSlotNumber(new OM_G31R01.TimeSlotNumber(tsInstance)); 
        // Set Combination 
        switch (combination)
        {
        case MainBCCH:
        {
            confReq.setCombination(new OM_G31R01.Combination(OM_G31R01.Enums.Combination.MainBCCH));
            OM_G31R01.CombChoice combChoice = new OM_G31R01.CombChoice();
            combChoice.setCombinationMainBCCH(buildCombinationMainBCCHFirstCall());
            confReq.setCombChoice(combChoice);
            break;
        }
        case SDCCH:
        {
            confReq.setCombination(new OM_G31R01.Combination(OM_G31R01.Enums.Combination.SDCCH));
            OM_G31R01.CombChoice combChoice = new OM_G31R01.CombChoice();
            combChoice.setCombinationSDCCH(buildCombinationSDCCHFirstCall());
            confReq.setCombChoice(combChoice);
            break;
        }
        case TCH:
        {
            confReq.setCombination(new OM_G31R01.Combination(OM_G31R01.Enums.Combination.TCH));
            OM_G31R01.CombinationTCH tchComb = buildCombinationTCHFirstCall(setParamsToDefaultValue);  
            confReq.setCombChoice(new OM_G31R01.CombChoice(tchComb));
            break;
        }
        default:
            throw new IllegalArgumentException("Combination: " + combination + " is currently not supported, the only supported combinations are: MainBCCHCombined, MainBCCH and TCH");
        }
        // Frequency list
        List<Integer> frequencyLst = new ArrayList<Integer>();
        
        for (int txRxAddress : frequencyList.keySet()) {
            int arfcnToUse = frequencyList.get(txRxAddress);
            frequencyLst.add(txRxAddress);
            frequencyLst.add(txRxAddress);
            frequencyLst.add((arfcnToUse >> 8) | 4);
            frequencyLst.add(arfcnToUse & 255);
        }
        confReq.setFrequencyList(frequencyLst);

        return confReq;
    } 
        

    /**
     * @name buildTsConfigRequest
     * 
     * @description Builds a TS Configuration Request
     * 
     * @param tsInstance - int 0-7
     * @param seedD - int > 0
     * @param seedE - int > 0
     * @param setOptionalParams - boolean
     * @param combination - 
     * 
     * @return OM_G31R01.TSConfigurationResult
     * 
     * @throws InterruptedException
     */
    public OM_G31R01.TSConfigurationRequest buildTsConfigRequest(int tsInstance,  int seedD, int seedE, boolean setOptionalParams, OM_G31R01.Enums.Combination combination ) throws InterruptedException {
    	int arfcn = momHelper.getArfcnToUse();
    	
    	int freqListTxAdress = 0;
    	int freqListRxAdress = 0;
    	int freqListBfmArfcnHiP = (arfcn >> 8) | 4;
    	int freqListArfcnLowP = arfcn & 255;

       //	boolean even = ((seedD % 2) == 0);
        OM_G31R01.TSConfigurationRequest confReq;        
        confReq = omServer.createTSConfigurationRequest();
        confReq.getRouting().setTG(TG_ROUTING);
        confReq.getRouting().setTRXC(TRXC_ROUTING);
        confReq.setInstanceNumber(tsInstance);
        confReq.setAssociatedSOInstance(TRXC_ROUTING);
        
        // HSN: 0-63
        confReq.setHSN(new OM_G31R01.HSN(seedD%64));
        
        // MAIO: 0-63
        confReq.setMAIO(new OM_G31R01.MAIO(seedD%64));
        
        // BSIC, BCC: 0-7, PLMN: 0-7
        //confReq.setBSIC(new OM_G31R01.BSIC((seedD%8), ((seedD+1)%8)));
        confReq.setBSIC(new OM_G31R01.BSIC(1, 7));
        
        // skip FN Offset - all TN must have the same FN offset, 
        // ie. this parameter is only possible to change in state RESET, so it will be tested elsewhere...
        
        // Extended Range Indicator, 0 - 1
        confReq.setExtendedRangeIndicator(new OM_G31R01.ExtendedRangeIndicator(Enums.ExtendedRangeIndicator.Normal));
        
        // Set Combination 
        switch (combination)
        {
        case MainBCCHCombined:
        {
        	confReq.setTimeSlotNumber(new OM_G31R01.TimeSlotNumber(0)); // TN must be set to 0 for combination MainBCCHCombined
        	confReq.setCombination(new OM_G31R01.Combination(OM_G31R01.Enums.Combination.MainBCCHCombined));
        	OM_G31R01.CombChoice combChoice = new OM_G31R01.CombChoice();
        	combChoice.setCombinationMainBCCHCombined(buildCombinationMainBCCHCombined(seedD, seedE, setOptionalParams));
        	confReq.setCombChoice(combChoice);
        	freqListBfmArfcnHiP += 4; // set BFM bit in octet
        	break;
        }
        case MainBCCH:
        {
        	confReq.setTimeSlotNumber(new OM_G31R01.TimeSlotNumber(0)); // TN must be set to 0 for combination MainBCCH
        	confReq.setCombination(new OM_G31R01.Combination(OM_G31R01.Enums.Combination.MainBCCH));
        	OM_G31R01.CombChoice combChoice = new OM_G31R01.CombChoice();
        	combChoice.setCombinationMainBCCH(buildCombinationMainBCCH(seedD, seedE, setOptionalParams));
        	confReq.setCombChoice(combChoice);
        	freqListBfmArfcnHiP += 4; // set BFM bit in octet
        	break;
        }
        case SDCCH:
        {
            int tn = seedD%8;
            confReq.setTimeSlotNumber(new OM_G31R01.TimeSlotNumber(tn));
            confReq.setCombination(new OM_G31R01.Combination(OM_G31R01.Enums.Combination.SDCCH));
            OM_G31R01.CombChoice combChoice = new OM_G31R01.CombChoice();
            combChoice.setCombinationSDCCH(buildCombinationSDCCH(seedD, seedE, setOptionalParams, tn));
            confReq.setCombChoice(combChoice);
            break;
        }
        case OptionalBCCH:
        {
            int tn = seedD%8;
            tn = (tn==0) ? tn+2 : tn; 
            tn = (tn==7) ? tn=2 : tn;
            tn = ((tn % 2) == 0) ? tn : tn+1;
            
            confReq.setTimeSlotNumber(new OM_G31R01.TimeSlotNumber(tn)); // TN must be set to 2,4 or 6 for combination OptionalBCCH
            confReq.setCombination(new OM_G31R01.Combination(OM_G31R01.Enums.Combination.OptionalBCCH));
            OM_G31R01.CombChoice combChoice = new OM_G31R01.CombChoice();
            combChoice.setCombinationOptionalBCCH(buildCombinationOptionalBCCH(seedD, seedE, setOptionalParams));
            confReq.setCombChoice(combChoice);
        	freqListBfmArfcnHiP += 4; // set BFM bit in octet
            break;
        }
        case TCH:
        {
        	confReq.setTimeSlotNumber(new OM_G31R01.TimeSlotNumber(seedD%8));
        	confReq.setCombination(new OM_G31R01.Combination(OM_G31R01.Enums.Combination.TCH));
        	OM_G31R01.CombinationTCH tchComb = buildCombinationTCH(seedD, seedE, setOptionalParams);  
        	confReq.setCombChoice(new OM_G31R01.CombChoice(tchComb));
        	break;
        }
        default:
        	throw new IllegalArgumentException("Combination: " + combination + " is currently not supported, the only supported combinations are: MainBCCHCombined, MainBCCH and TCH");
        }
        // Frequency list
        List<Integer> frequencyList = new ArrayList<Integer>();
        frequencyList.add(freqListTxAdress);
        frequencyList.add(freqListRxAdress);
        frequencyList.add(freqListBfmArfcnHiP);
        frequencyList.add(freqListArfcnLowP);
        confReq.setFrequencyList(frequencyList);

        return confReq;
    }
    
    /**
     * @name tsConfigRequestFirstCall
     * 
     * @description Handles TS Configuration Request
     * 
     * @param tsInstance - int 0-7
     * @param seedD - int > 0
     * @param seedE - int > 0
     * @param setOptionalParams - boolean
     * @param combination - 
     * 
     * @return OM_G31R01.TSConfigurationResult
     * 
     * @throws InterruptedException
     */

    public OM_G31R01.TSConfigurationResult tsConfigRequestFirstCall(int tsInstance, OM_G31R01.Enums.Combination combination ) throws InterruptedException {

    	
        return tsConfigRequestFirstCall(tsInstance, combination, false);
    }
    
    /**
     * @name tsConfigRequestFirstCall
     * 
     * @description Handles TS Configuration Request
     * 
     * @param tsInstance - int 0-7
     * @param trxcId - int
     * @param combination - 
     * 
     * @return OM_G31R01.TSConfigurationResult
     * 
     * @throws InterruptedException
     */
    public OM_G31R01.TSConfigurationResult tsConfigRequestFirstCall(int tsInstance, int trxcId, OM_G31R01.Enums.Combination combination ) throws InterruptedException {

    	
        return tsConfigRequestFirstCall(tsInstance, trxcId, combination, false);
    }
  
    /**
     * @name tsConfigRequestFirstCall
     * 
     * @description Handles TS Configuration Request
     * 
     * @param tsInstance - int 0-7
     * @param seedD - int > 0
     * @param seedE - int > 0
     * @param setOptionalParams - boolean
     * @param combination - 
     * 
     * @return OM_G31R01.TSConfigurationResult
     * 
     * @throws InterruptedException
     */
    public OM_G31R01.TSConfigurationResult tsConfigRequestFirstCall(int tsInstance, OM_G31R01.Enums.Combination combination,  boolean setParamsToDefaultValue) throws InterruptedException {

        OM_G31R01.TSConfigurationRequest confReq = buildTsConfigRequestFirstCall(tsInstance, combination, setParamsToDefaultValue);
        return confReq.send();
    }
    
    /**
     * @name tsConfigRequestFirstCall
     * 
     * @description Handles TS Configuration Request
     * 
     * @param tsInstance - int 0-7
     * @param trxcId -int 
     * @param combination - 
     * @param setOptionalParams - boolean
     * 
     * @return OM_G31R01.TSConfigurationResult
     * 
     * @throws InterruptedException
     */
    
    public OM_G31R01.TSConfigurationResult tsConfigRequestFirstCall(int tsInstance, int trxcId, OM_G31R01.Enums.Combination combination,  boolean setParamsToDefaultValue) throws InterruptedException {

        OM_G31R01.TSConfigurationRequest confReq = buildTsConfigRequestFirstCall(tsInstance, trxcId, combination, setParamsToDefaultValue);
        return confReq.send();
    }
    
    /**
     * @name tsConfigRequest
     * 
     * @description Handles TS Configuration Request
     * 
     * @param tsInstance - int 0-7
     * @param seedD - int > 0
     * @param seedE - int > 0
     * @param setOptionalParams - boolean
     * @param combination - 
     * 
     * @return OM_G31R01.TSConfigurationResult
     * 
     * @throws InterruptedException
     */
    public OM_G31R01.TSConfigurationResult tsConfigRequest(int tsInstance,  int seedD, int seedE, boolean setOptionalParams, OM_G31R01.Enums.Combination combination ) throws InterruptedException {

        OM_G31R01.TSConfigurationRequest confReq = buildTsConfigRequest(tsInstance, seedD, seedE, setOptionalParams, combination);
        return confReq.send();
    }
    
    /**
     * @name tsConfigRequest with changed combination
     * 
     * @description Handles TS Configuration Request, old random seed is used for all parameters except: combination
     * 
     * @param tsInstance - int 0-7
     * @param oldSeed - int > 0
     * @param setOptionalParams - boolean
     * @param combination - 
     * 
     * @return OM_G31R01.TSConfigurationResult
     * 
     * @throws InterruptedException
     */
    public OM_G31R01.TSConfigurationResult tsConfigRequestWithChanged_Combination(int tsInstance, int oldSeed, boolean setOptionalParams, OM_G31R01.Enums.Combination combination ) throws InterruptedException {

    	// Create the same TS configuration as before...but with changed combination
    	OM_G31R01.TSConfigurationRequest confReq = buildTsConfigRequest(tsInstance, oldSeed, oldSeed, setOptionalParams, combination);
    	return confReq.send();
    }
    
    /**
     * @name tsConfigRequest with changed TimeSlotNumber
     * 
     * @description Handles TS Configuration Request, old random seed is used for all parameters except: TimeSlotNumber
     * 
     * @param tsInstance - int 0-7
     * @param oldSeed - int > 0
     * @param setOptionalParams - boolean
     * @param combination - 
     * 
     * @return OM_G31R01.TSConfigurationResult
     * 
     * @throws InterruptedException
     */
    public OM_G31R01.TSConfigurationResult tsConfigRequestWithChanged_TimeSlotNumber(int tsInstance, int oldSeed, boolean setOptionalParams, OM_G31R01.Enums.Combination combination ) throws InterruptedException {

    	// Create the same TS configuration as before...
    	OM_G31R01.TSConfigurationRequest confReq = buildTsConfigRequest(tsInstance, oldSeed, oldSeed, setOptionalParams, combination);
    
    	// ... but change the Time Slot Number parameter
    	confReq.setTimeSlotNumber(new OM_G31R01.TimeSlotNumber((oldSeed+1)%8));
    	return confReq.send();
    }
    
    /**
     * @name tsConfigRequest with changed HSN
     * 
     * @description Handles TS Configuration Request, old random seed is used for all parameters except: HSN
     * 
     * @param tsInstance - int 0-7
     * @param oldSeed - int > 0
     * @param setOptionalParams - boolean
     * @param combination - 
     * 
     * @return OM_G31R01.TSConfigurationResult
     * 
     * @throws InterruptedException
     */
    public OM_G31R01.TSConfigurationResult tsConfigRequestWithChanged_HSN(int tsInstance,  int oldSeed, boolean setOptionalParams, OM_G31R01.Enums.Combination combination) throws InterruptedException {

    	// Create the same TS configuration as before...
    	OM_G31R01.TSConfigurationRequest confReq = buildTsConfigRequest(tsInstance, oldSeed, oldSeed, setOptionalParams, combination);

        // ...but change the HSN parameter (range: 0-63)
        confReq.setHSN(new OM_G31R01.HSN((oldSeed+1)%64));

    	return confReq.send();
    }   
    
    /**
     * @name tsConfigRequest with changed MAIO
     * 
     * @description Handles TS Configuration Request, old random seed is used for all parameters except: MAIO
     * 
     * @param tsInstance - int 0-7
     * @param oldSeed - int > 0
     * @param setOptionalParams - boolean
     * @param combination - 
     * 
     * @return OM_G31R01.TSConfigurationResult
     * 
     * @throws InterruptedException
     */
    public OM_G31R01.TSConfigurationResult tsConfigRequestWithChanged_MAIO(int tsInstance,  int oldSeed, boolean setOptionalParams, OM_G31R01.Enums.Combination combination) throws InterruptedException {

    	// Create the same TS configuration as before...
    	OM_G31R01.TSConfigurationRequest confReq = buildTsConfigRequest(tsInstance, oldSeed, oldSeed, setOptionalParams, combination);
    
        // ... but change the MAIO parameter (range: 0-63)
        confReq.setMAIO(new OM_G31R01.MAIO((oldSeed+1)%64));
    	return confReq.send();
    }
   
    /**
     * @name tsConfigRequest with changed BSIC
     * 
     * @description Handles TS Configuration Request, old random seed is used for all parameters except: BSIC
     * 
     * @param tsInstance - int 0-7
     * @param oldSeed - int > 0
     * @param setOptionalParams - boolean
     * @param combination - 
     * 
     * @return OM_G31R01.TSConfigurationResult
     * 
     * @throws InterruptedException
     */
    public OM_G31R01.TSConfigurationResult tsConfigRequestWithChanged_BSIC(int tsInstance,  int oldSeed, boolean setOptionalParams, OM_G31R01.Enums.Combination combination) throws InterruptedException {

    	// Create the same TS configuration as before...
    	OM_G31R01.TSConfigurationRequest confReq = buildTsConfigRequest(tsInstance, oldSeed, oldSeed, setOptionalParams, combination);
    
        // ... but change the BSIC parameter, range BCC: 0-7, PLMN: 0-7
        confReq.setBSIC(new OM_G31R01.BSIC( ((oldSeed+1)%8), ((oldSeed+2)%8) ));
        
    	return confReq.send();
    }
    
    /**
     * @name tsConfigRequest with changed FN Offset
     * 
     * @description Handles TS Configuration Request, old random seed is used for all parameters except: FN Offset
     * 
     * @param tsInstance - int 0-7
     * @param oldSeed - int > 0
     * @param setOptionalParams - boolean
     * @param combination - 
     * 
     * @return OM_G31R01.TSConfigurationResult
     * 
     * @throws InterruptedException
     */
    public OM_G31R01.TSConfigurationResult tsConfigRequestWithChanged_FNOffset(int tsInstance,  int oldSeed, boolean setOptionalParams, OM_G31R01.Enums.Combination combination) throws InterruptedException {

    	// Create the same TS configuration as before...
    	OM_G31R01.TSConfigurationRequest confReq = buildTsConfigRequest(tsInstance, oldSeed, oldSeed, setOptionalParams, combination);
    
        // ... but change the FN Offset parameter, range: 0-1325
        confReq.setFNOffset((oldSeed+1)%1326);

    	return confReq.send();
    }
    
    /**
     * @name tsConfigRequest with changed Extended Range Indicator
     * 
     * @description Handles TS Configuration, old random seed is used for all parameters except: Extended Range Indicator
     * 
     * @param tsInstance - int 0-7
     * @param oldSeed - int > 0
     * @param setOptionalParams - boolean
     * @param combination - 
     * 
     * @return OM_G31R01.TSConfigurationResult
     * 
     * @throws InterruptedException
     */
    public OM_G31R01.TSConfigurationResult tsConfigRequestWithChanged_ExtendedRangeIndicator(int tsInstance,  int oldSeed, boolean setOptionalParams, OM_G31R01.Enums.Combination combination) throws InterruptedException {

    	// Create the same TS configuration as before...
    	OM_G31R01.TSConfigurationRequest confReq = buildTsConfigRequest(tsInstance, oldSeed, oldSeed, setOptionalParams, combination);
    
    	// ... but change the Interference Rejection Combining: range 0/1
    	OM_G31R01.CombinationTCH tchComb = buildCombinationTCH(oldSeed, oldSeed, setOptionalParams);    	
    	tchComb.setInterferenceRejectionCombining(new OM_G31R01.InterferenceRejectionCombining((((oldSeed+1) % 2) == 0) ? 
    			Enums.InterferenceRejectionCombining.Off :
    			Enums.InterferenceRejectionCombining.On));    	
    	confReq.setCombChoice(new OM_G31R01.CombChoice(tchComb));   	 	
    	return confReq.send();
    }
    
    /**
     * @name tsConfigRequest with changed BS_PA_MFRMS, combination: MainBCCHCombined
     * 
     * @description Handles TS Configuration Request, old random seed is used for all parameters except: BS_PA_MFRMS
     * 
     * @param tsInstance - int 0-7
     * @param oldSeed - int > 0
     * @param setOptionalParams - boolean
     * 
     * @return OM_G31R01.TSConfigurationResult
     * 
     * @throws InterruptedException
     */
    public OM_G31R01.TSConfigurationResult tsConfigRequestWithChanged_BsPaMfrms(int tsInstance, int oldSeed, boolean setOptionalParams) throws InterruptedException {

    	// Create the same TS configuration as before...
    	OM_G31R01.TSConfigurationRequest confReq = buildTsConfigRequest(tsInstance, oldSeed, oldSeed, setOptionalParams, OM_G31R01.Enums.Combination.MainBCCHCombined);
    	
    	// ... but change the BS_PA_MFRMS, range: 2-9
    	OM_G31R01.CombinationMainBCCHCombined mainBCCHCombined = buildCombinationMainBCCHCombined(oldSeed, oldSeed, setOptionalParams);
    	mainBCCHCombined.setBS_PA_MFRMS(new OM_G31R01.BS_PA_MFRMS(((oldSeed+1)%8)+2));

    	OM_G31R01.CombChoice combChoice = new OM_G31R01.CombChoice();
    	combChoice.setCombinationMainBCCHCombined(mainBCCHCombined);
    	confReq.setCombChoice(combChoice);
    	return confReq.send();
    }
    
    /**
     * @name tsConfigRequest with changed CBCH Indicator, combination: MainBCCHCombined
     * 
     * @description Handles TS Configuration Request, old random seed is used for all parameters except: CBCH Indicator
     * 
     * @param tsInstance - int 0-7
     * @param oldSeed - int > 0
     * @param setOptionalParams - boolean
     * 
     * @return OM_G31R01.TSConfigurationResult
     * 
     * @throws InterruptedException
     */
    public OM_G31R01.TSConfigurationResult tsConfigRequestWithChanged_CbchIndicator(int tsInstance,  int oldSeed, boolean setOptionalParams) throws InterruptedException {

    	// Create the same TS configuration as before...
    	OM_G31R01.TSConfigurationRequest confReq = buildTsConfigRequest(tsInstance, oldSeed, oldSeed, setOptionalParams, OM_G31R01.Enums.Combination.MainBCCHCombined);
    	
    	// ... but change the CBCH Indicator, range: 0/1
    	OM_G31R01.CombinationMainBCCHCombined mainBCCHCombined = buildCombinationMainBCCHCombined(oldSeed, oldSeed, setOptionalParams);
       	mainBCCHCombined.setCBCHIndicator(new OM_G31R01.CBCHIndicator((((oldSeed+1) % 2) == 0) ? 
       			Enums.CBCHIndicator.NoCBCH : 
       				Enums.CBCHIndicator.CBCH));

    	OM_G31R01.CombChoice combChoice = new OM_G31R01.CombChoice();
    	combChoice.setCombinationMainBCCHCombined(mainBCCHCombined);
    	confReq.setCombChoice(combChoice);
    	return confReq.send();
    }
    
    /**
     * @name tsConfigRequest with changed BS_AG_BLKS_RES, combination: MainBCCHCombined
     * 
     * @description Handles TS Configuration Request, old random seed is used for all parameters except: BS_AG_BLKS_RES
     * 
     * @param tsInstance - int 0-7
     * @param oldSeed - int > 0
     * @param setOptionalParams - boolean
     * 
     * @return OM_G31R01.TSConfigurationResult
     * 
     * @throws InterruptedException
     */
    public OM_G31R01.TSConfigurationResult tsConfigRequestWithChanged_BsAgBlksRes(int tsInstance,  int oldSeed, boolean setOptionalParams) throws InterruptedException {

    	// Create the same TS configuration as before...
    	OM_G31R01.TSConfigurationRequest confReq = buildTsConfigRequest(tsInstance, oldSeed, oldSeed, setOptionalParams, OM_G31R01.Enums.Combination.MainBCCHCombined);
    	    	
       	// ... but change the  BS_AG_BLKS_RES, range: 0-2 
    	OM_G31R01.CombinationMainBCCHCombined mainBCCHCombined = buildCombinationMainBCCHCombined(oldSeed, oldSeed, setOptionalParams);
    	mainBCCHCombined.setBS_AG_BLKS_RES(new OM_G31R01.BS_AG_BLKS_RES((oldSeed+1)%3));
    	
    	OM_G31R01.CombChoice combChoice = new OM_G31R01.CombChoice();
    	combChoice.setCombinationMainBCCHCombined(mainBCCHCombined);
    	confReq.setCombChoice(combChoice);
    	return confReq.send();
    }
    
    /**
     * @name tsConfigRequest with changed TSC, combination TCH
     * 
     * @description Handles TS Configuration Request, old random seed is used for all parameters except: TSC
     * 
     * @param tsInstance - int 0-7
     * @param oldSeed - int > 0
     * @param setOptionalParams - boolean
     * 
     * @return OM_G31R01.TSConfigurationResult
     * 
     * @throws InterruptedException
     */
    public OM_G31R01.TSConfigurationResult tsConfigRequestWithChanged_TSC(int tsInstance,  int oldSeed, boolean setOptionalParams) throws InterruptedException {

    	// Create the same TS configuration as before...
    	OM_G31R01.TSConfigurationRequest confReq = buildTsConfigRequest(tsInstance, oldSeed, oldSeed, setOptionalParams, OM_G31R01.Enums.Combination.TCH);
    
        // ... but change the TSC parameter, range: 0-7
    	OM_G31R01.CombinationTCH tchComb = buildCombinationTCH(oldSeed, oldSeed, setOptionalParams);
    	tchComb.setTSC(new OM_G31R01.TSC((oldSeed+1)%8));
    	confReq.setCombChoice(new OM_G31R01.CombChoice(tchComb));

    	return confReq.send();
    }
    
    /**
     * @name tsConfigRequest with changed TTA, combination TCH
     * 
     * @description Handles TS Configuration Request, old random seed is used for all parameters except: TTA
     * 
     * @param tsInstance - int 0-7
     * @param oldSeed - int > 0
     * @param setOptionalParams - boolean
     * 
     * @return OM_G31R01.TSConfigurationResult
     * 
     * @throws InterruptedException
     */
    public OM_G31R01.TSConfigurationResult tsConfigRequestWithChanged_TTA(int tsInstance,  int oldSeed, boolean setOptionalParams) throws InterruptedException {

    	// Create the same TS configuration as before...
    	OM_G31R01.TSConfigurationRequest confReq = buildTsConfigRequest(tsInstance, oldSeed, oldSeed, setOptionalParams, OM_G31R01.Enums.Combination.TCH);
    
        // ... but change the TTA parameter, range:  2-50
    	OM_G31R01.CombinationTCH tchComb = buildCombinationTCH(oldSeed, oldSeed, setOptionalParams);  
    	tchComb.setTTA(2 + ((oldSeed+1)%49));
    	confReq.setCombChoice(new OM_G31R01.CombChoice(tchComb));
   	 	
    	return confReq.send();
    }
    
    /**
     * @name tsConfigRequest with changed ICM Channel Rate, combination TCH
     * 
     * @description Handles TS Configuration Request, old random seed is used for all parameters except: ICM Channel Rate
     * 
     * @param tsInstance - int 0-7
     * @param oldSeed - int > 0
     * @param setOptionalParams - boolean
     * 
     * @return OM_G31R01.TSConfigurationResult
     * 
     * @throws InterruptedException
     */
    public OM_G31R01.TSConfigurationResult tsConfigRequestWithChanged_ICMChannelRate(int tsInstance,  int oldSeed, boolean setOptionalParams) throws InterruptedException {

    	// Create the same TS configuration as before...
    	OM_G31R01.TSConfigurationRequest confReq = buildTsConfigRequest(tsInstance, oldSeed, oldSeed, setOptionalParams, OM_G31R01.Enums.Combination.TCH);
    
        // ... but change the ICM Channel Rate parameter, range:  TCH_F (=0) or TCH_H (=1)
    	OM_G31R01.CombinationTCH tchComb = buildCombinationTCH(oldSeed, oldSeed, setOptionalParams);  
    	tchComb.setICMChannelRate((((oldSeed+1) % 2) == 0) ?
    		Enums.ICMChannelRate.TCH_F :
    		Enums.ICMChannelRate.TCH_H	); 
    	confReq.setCombChoice(new OM_G31R01.CombChoice(tchComb));
    	return confReq.send();
    }
    
    /**
     * @name tsConfigRequest with changed Link Supervision Control, combination TCH
     * 
     * @description Handles TS Configuration Request, old random seed is used for all parameters except: Link Supervision Control
     * 
     * @param tsInstance - int 0-7
     * @param oldSeed - int > 0
     * @param setOptionalParams - boolean
     * 
     * @return OM_G31R01.TSConfigurationResult
     * 
     * @throws InterruptedException
     */
    public OM_G31R01.TSConfigurationResult tsConfigRequestWithChanged_LinkSupervisionControl(int tsInstance,  int oldSeed, boolean setOptionalParams) throws InterruptedException {

    	// Create the same TS configuration as before...
    	OM_G31R01.TSConfigurationRequest confReq = buildTsConfigRequest(tsInstance, oldSeed, oldSeed, setOptionalParams, OM_G31R01.Enums.Combination.TCH);
    
        // ... but change the LinkSupervisionControl parameter, range:  0/1
    	OM_G31R01.CombinationTCH tchComb = buildCombinationTCH(oldSeed, oldSeed, setOptionalParams);  
        tchComb.setLinkSupervisionControl(new OM_G31R01.LinkSupervisionControl((((oldSeed+1) % 2) == 0) ? Enums.LSA.Off : Enums.LSA.On));
    	confReq.setCombChoice(new OM_G31R01.CombChoice(tchComb));
    	return confReq.send();
    }
    
    /**
     * @name tsConfigRequest with changed Link Supervision Filtering Time, combination TCH
     * 
     * @description Handles TS Configuration Request, old random seed is used for all parameters except: Link Supervision Filtering Time
     * 
     * @param tsInstance - int 0-7
     * @param oldSeed - int > 0
     * @param setOptionalParams - boolean
     * 
     * @return OM_G31R01.TSConfigurationResult
     * 
     * @throws InterruptedException
     */
    public OM_G31R01.TSConfigurationResult tsConfigRequestWithChanged_LinkSupervisionFilteringTime(int tsInstance,  int oldSeed, boolean setOptionalParams) throws InterruptedException {

    	// Create the same TS configuration as before...
    	OM_G31R01.TSConfigurationRequest confReq = buildTsConfigRequest(tsInstance, oldSeed, oldSeed, setOptionalParams, OM_G31R01.Enums.Combination.TCH);
    
        // ... but change the Link Supervision Filtering Time parameter, range: 10-200
    	OM_G31R01.CombinationTCH tchComb = buildCombinationTCH(oldSeed, oldSeed, setOptionalParams);
        tchComb.setLinkSupervisionFilteringTime(10 + ((oldSeed+1)%190));
    	confReq.setCombChoice(new OM_G31R01.CombChoice(tchComb));   	 	
    	return confReq.send();
    }
    
    /**
     * @name tsConfigRequest with changed Call Supervision Time, combination TCH
     * 
     * @description Handles TS Configuration, old random seed is used for all parameters except: Call Supervision Time
     * 
     * @param tsInstance - int 0-7
     * @param oldSeed - int > 0
     * @param setOptionalParams - boolean
     * 
     * @return OM_G31R01.TSConfigurationResult
     * 
     * @throws InterruptedException
     */
    public OM_G31R01.TSConfigurationResult tsConfigRequestWithChanged_CallSupervisionTime(int tsInstance,  int oldSeed, boolean setOptionalParams) throws InterruptedException {

    	// Create the same TS configuration as before...
    	OM_G31R01.TSConfigurationRequest confReq = buildTsConfigRequest(tsInstance, oldSeed, oldSeed, setOptionalParams, OM_G31R01.Enums.Combination.TCH);
    
        // ... but change the Call supervision Time, range: 0, 4, 8...64
    	OM_G31R01.CombinationTCH tchComb = buildCombinationTCH(oldSeed, oldSeed, setOptionalParams);
        tchComb.setCallSupervisionTime( ((oldSeed+1)%17) * 4);
    	confReq.setCombChoice(new OM_G31R01.CombChoice(tchComb));   	 	
    	return confReq.send();
    }
    
    
    /**
     * @name tsConfigRequest
     * 
     * @description Handles TS Configuration Request
     * 
     * @param  
     * 
     * @return OM_G31R01.TSConfigurationResult
     * 
     * @throws InterruptedException
     */
    public OM_G31R01.TSConfigurationResult tsConfigRequest(int tsInstance) throws InterruptedException {
    	
        return tsConfigRequest(tsInstance, 0, 0, false, OM_G31R01.Enums.Combination.TCH);
    }
    
    
    /**
     * @name statusRequest
     * 
     * @description Handles a Status Request command EP
     * 
     * @param moClass - OM_G31R01.Enums.MOClass
     * 
     * @return OM_G31R01.StatusResponse
     * 
     * @throws InterruptedException
     */
    public OM_G31R01.StatusResponse statusRequest(OM_G31R01.Enums.MOClass moClass) throws InterruptedException {
    	return statusRequest(moClass, TG_ROUTING, TRXC_ROUTING, INSTANCE_NUMBER, ASSOCIATED_SO_INSTANCE);
    }

    /**
     * @name statusRequest
     * 
     * @description Handles a Status Request command EP
     * 
     * @param moClass - OM_G31R01.Enums.MOClass
     * @param instance - int
     * @param associatedSoInstance - int 
     * 
     * @return OM_G31R01.StatusResponse
     * 
     * @throws InterruptedException
     */
    public OM_G31R01.StatusResponse statusRequest(OM_G31R01.Enums.MOClass moClass, int instance, int associatedSoInstance) throws InterruptedException {
    	return statusRequest(moClass, TG_ROUTING, TRXC_ROUTING, instance, associatedSoInstance);
    }
    
    /**
     * @name statusRequest
     * 
     * @description Handles a Status Request command EP
     * 
     * @param moClass - OM_G31R01.Enums.MOClass
     * @param tgId - int
     * @param trxc - int
     * @param instance - int
     * @param associatedSoInstance - int 
     * 
     * @return OM_G31R01.StatusResponse
     * 
     * @throws InterruptedException
     */
    public OM_G31R01.StatusResponse statusRequest(OM_G31R01.Enums.MOClass moClass, int tgId, int trxc, int instance, int associatedSoInstance) throws InterruptedException {

        OM_G31R01.StatusRequest statusReq;

        statusReq = omServer.createStatusRequest();
        statusReq.getRouting().setTG(tgId);
        statusReq.getRouting().setTRXC(trxc);
        statusReq.setMOClass(moClass);
        statusReq.setAssociatedSOInstance(associatedSoInstance);
        statusReq.setInstanceNumber(instance);

        return statusReq.send();
    }
    
    /**
     * @name clearStatusUpdate
     * 
     * @description Clears the Status Update queue, shall be called before any
     *              method invoking Status Update if the data shall be compared.
     */
    public void clearStatusUpdate() {

        MessageQueue<BG.G31StatusUpdate> statusUpdateQueue;

        statusUpdateQueue = bgServer.getG31StatusUpdateQueue();
        statusUpdateQueue.clear();
    }

    /**
     * @name getStatusUpdate
     * 
     * @description Returns the Status Update message
     * 
     * @param timeout
     * @param unit
     * 
     * @return Status Update message BG.G31StatusUpdate
     * 
     * @throws InterruptedException
     */
    public BG.G31StatusUpdate getStatusUpdate(long timeout, TimeUnit unit) throws InterruptedException {

        MessageQueue<BG.G31StatusUpdate> statusUpdateQueue;

        statusUpdateQueue = bgServer.getG31StatusUpdateQueue();

        return statusUpdateQueue.poll(timeout, unit);
    }
    
    /**
     * @name getTsStatusUpdate
     * 
     * @description Returns the TSs Status Update message
     * 
     * @param timeout
     * @param unit
     * 
     * @return TSs Status Update message BG.G31TSsStatusUpdate
     * 
     * @throws InterruptedException
     */
    public BG.G31TSsStatusUpdate getTsStatusUpdate(long timeout, TimeUnit unit) throws InterruptedException {

        MessageQueue<BG.G31TSsStatusUpdate> tsStatusUpdateQueue;

        tsStatusUpdateQueue = bgServer.getG31TSsStatusUpdateQueue();

        return tsStatusUpdateQueue.poll(timeout, unit);
    }
    
    /**
     * @name waitForSoScfStatusUpdate
     * 
     * @description Waits for a So Scf Status Update for a specified TG
     * 
     * @param timeoutSeconds
     * @param tgId
     * @return the received status update at success, otherwise null
     * 
     * @throws InterruptedException
     */
    public G31StatusUpdate waitForSoScfStatusUpdate(int timeoutSeconds, int tgId) throws InterruptedException {
    	String logInfo = "";
    	int iterations = 0;
    	while (iterations < timeoutSeconds) {
    		G31StatusUpdate statusUpdate = getStatusUpdate(1, TimeUnit.SECONDS);
    		if (statusUpdate != null && statusUpdate.getRouting().getTG() == tgId) {
    			return statusUpdate;
    		}
    		logInfo = "**** waitForSoScfStatusUpdate did not receive a Status Update, sleep for 1s, iteration=" + iterations;
        logger.info(logInfo);
        ++iterations;
        Thread.sleep(1000); // sleep for a second
    	}
    	return null;
    }
    /**
     * @name waitForSoScfStatusUpdate
     * 
     * @description Waits for a So Scf Status Update for a specified TG
     * 
     * @param timeoutSeconds
     * @param tgId
     * @param btsCapabilitiesSignature TODO
     * @param btsCapabilitiesSignatur TODO
     * @return String Empty at success, otherwise an error message
     * 
     * @throws InterruptedException
     */
    public String waitForSoScfStatusUpdate(int timeoutSeconds, int tgId, StringBuffer btsCapabilitiesSignature) throws InterruptedException {
    	String logInfo = "";
    	int iterations = 0;
    	while (iterations < timeoutSeconds) {
    		G31StatusUpdate statusUpdate = getStatusUpdate(1, TimeUnit.SECONDS);
            if (statusUpdate != null) {
            	// got a status update, but not sure it is for So Scf
            	G31StatusSCF soScfStatusUpdate = statusUpdate.getStatusChoice().getG31StatusSCF();
            	if (soScfStatusUpdate != null) {
            		int receivedTgId = statusUpdate.getRouting().getTG(); 
            		if (receivedTgId == tgId) {
            			// it is a So Scf Status Update
            			if (soScfStatusUpdate.getBTSCapabilitiesSignature() != 0) {
            				// there is a capabilities signature, all good!!!
            				btsCapabilitiesSignature.append(Integer.toString(soScfStatusUpdate.getBTSCapabilitiesSignature()));
            				logger.info("**** waitForSoScfStatusUpdate received a valid So Scf Status Update, iteration=" + iterations + ", bts cap sig:" + btsCapabilitiesSignature.toString());
            				return "";
            			} else {
            				logInfo = "**** waitForSoScfStatusUpdate received a So Scf Status Update, but without a valid capability signature, sleep for 1s, iteration=" + iterations;
            			}
            		} else {
            			logInfo = "**** waitForSoScfStatusUpdate received a So Scf Status Update, but for wrong TG. Expected tgId=" + tgId + ", Received tgId=" + receivedTgId + ", sleep for 1s, iteration=" + iterations;
            		}
            	} else {
                	logInfo = "**** waitForSoScfStatusUpdate did not receive a So Scf Status Update, sleep for 1s, iteration=" + iterations;
            	}
            } else {
            	logInfo = "**** waitForSoScfStatusUpdate did not receive a Status Update, sleep for 1s, iteration=" + iterations;
            }
            logger.info(logInfo);
            ++iterations;
            Thread.sleep(1000); // sleep for a second
    	}
    	return "Did not receive an SO SCF Status Update before timeout";
    }
    
    public G31StatusUpdate waitForSoTrxcStatusUpdate(int timeoutSeconds, int tgId) throws InterruptedException {
    	String logInfo = "";
    	int iterations = 0;
    	while (iterations < timeoutSeconds) {
    		G31StatusUpdate statusUpdate = getStatusUpdate(1, TimeUnit.SECONDS);
    		if (statusUpdate != null && statusUpdate.getRouting().getTG() == tgId &&
    				statusUpdate.getMOClass() == moClassTrxc) {
    			return statusUpdate;
    		}
    		logInfo = "**** waitForSoScfStatusUpdate did not receive a Status Update, sleep for 1s, iteration=" + iterations;
        logger.info(logInfo);
        ++iterations;
        Thread.sleep(1000); // sleep for a second
    	}
    	return null;
	}
    
    /**
     * @name getAtBundlingInfoUpdate
     * 
     * @description Returns the first At Bundling Info Upfdate message available
     * 
     * @param timeout
     * @param unit
     * 
     * @return Status Update message BG.G31ATBundlingInfoUpdate
     * 
     * @throws InterruptedException
     */
    public BG.G31ATBundlingInfoUpdate getAtBundlingInfoUpdate(long timeout, TimeUnit unit) throws InterruptedException {

        MessageQueue<BG.G31ATBundlingInfoUpdate> atBundlingInfoUpdateQueue;
                
        atBundlingInfoUpdateQueue = bgServer.getG31ATBundlingInfoUpdateQueue();
        
        return atBundlingInfoUpdateQueue.poll(timeout, unit);
    }
    
    
    /**
     * @name clearAtBundlingInfoUpdateQueue
     * 
     * @description Clear the queue for received At Bundling Info Update messages
     * 
     * @throws InterruptedException
     */    
    public void clearAtBundlingInfoUpdateQueue() throws InterruptedException {

        MessageQueue<BG.G31ATBundlingInfoUpdate> atBundlingInfoUpdateQueue;
                
        atBundlingInfoUpdateQueue = bgServer.getG31ATBundlingInfoUpdateQueue();
        
        atBundlingInfoUpdateQueue.clear();
    }
    
    
    
    
    /**
     * @name waitForAtBundlingInfoUpdateWithEmptyBundlingData
     * 
     * @description Waits for a At Bundling Info Update message for a specified TG. The bundling data shall be empty.
     * 
     * @param timeoutSeconds
     * @param tgId
     * 
     * @return String Empty at success, otherwise an error message
     * 
     * @throws InterruptedException
     */
    public String waitForAtBundlingInfoUpdateWithEmptyBundlingData(int timeoutSeconds, int tgId) throws InterruptedException {
    	return waitForAtBundlingInfoUpdate(timeoutSeconds, tgId, CHECK_BUNDLING_INFO_UPDATE_SCHEME.CHECK_FOR_NO_BUNDLING_INFO, null);
    }

    /**
     * @name waitForAtBundlingInfoUpdateWithBundlingData
     * 
     * @description Waits for a At Bundling Info Update message for a specified TG. The bundling data shall not be empty.
     * 
     * @param timeoutSeconds
     * @param tgId
     * 
     * @return String Empty at success, otherwise an error message
     * 
     * @throws InterruptedException
     */
    public String waitForAtBundlingInfoUpdateWithBundlingData(int timeoutSeconds, int tgId) throws InterruptedException {
    	return waitForAtBundlingInfoUpdate(timeoutSeconds, tgId, CHECK_BUNDLING_INFO_UPDATE_SCHEME.CHECK_FOR_ANY_BUNDLING_INFO, null); 	
    }

    public String waitForAtBundlingInfoUpdateWithPredefinedBundlingData(int timeoutSeconds, int tgId, List<Integer> teis) throws InterruptedException {
    	return waitForAtBundlingInfoUpdate(timeoutSeconds, tgId, CHECK_BUNDLING_INFO_UPDATE_SCHEME.CHECK_FOR_DEFAULT_BUNDLING_INFO, teis);
    }
    
    private String waitForAtBundlingInfoUpdate(int timeoutSeconds, int tgId, CHECK_BUNDLING_INFO_UPDATE_SCHEME checkBundlingScheme, List<Integer> teis) throws InterruptedException {
    	String logInfo = "";
    	int iterations = 0;
    	while (iterations < timeoutSeconds) {
    		G31ATBundlingInfoUpdate atBundlingInfo = getAtBundlingInfoUpdate(1, TimeUnit.SECONDS);
            if (atBundlingInfo != null) {
            	// got the bundling info, but not sure it is for the right tg
          		int receivedTgId = atBundlingInfo.getRouting().getTG(); 
          		if (receivedTgId == tgId) {
          			// it is a bundling info for the expected tg
          			if (atBundlingInfo.getBundlingSignature() != 0) {
          				switch (checkBundlingScheme) {
          					case CHECK_FOR_DEFAULT_BUNDLING_INFO:
                  				if (checkPredefinedBundlingData(atBundlingInfo.getG31BundlingGroups().getBundlingGroupsData(), teis)) {
                      				// we are happy with the bundling info update
                      				logger.info("**** waitForAtBundlingInfoUpdate received a valid AT Bundling Info Update, iteration=" + iterations);
                      				return "";
                      			} else {
                      				logger.info("**** waitForAtBundlingInfoUpdate received an AT Bundling Info Update but it has wrong content, iteration=" + iterations);
                      			}
          						break;
          					case CHECK_FOR_NO_BUNDLING_INFO:
                  				List<Integer> bundlingData = atBundlingInfo.getG31BundlingGroups().getBundlingGroupsData();
                  				// when there is no bundling information, the bundling data consist of one element, 
                  				// which is the number of bundling groups, this value shall be zero
                  				if ((bundlingData.size() == 1) && (bundlingData.get(0) == 0)) {
                  					// we are happy with the bundling info update
                      				logger.info("**** waitForAtBundlingInfoUpdate received a valid AT Bundling Info Update, iteration=" + iterations);
                      				return "";
                      			} else {
                      				logger.info("**** waitForAtBundlingInfoUpdate received an AT Bundling Info Update but it is not empty, size=" + bundlingData.size() + " iteration=" + iterations);
                      			}
          						break;
          					case CHECK_FOR_ANY_BUNDLING_INFO:
                  				int sizeOfBgData = atBundlingInfo.getG31BundlingGroups().getBundlingGroupsData().size(); 
                      			if (sizeOfBgData > 1) {
                      				// we are happy with the bundling info update
                      				logger.info("**** waitForAtBundlingInfoUpdate received a valid AT Bundling Info Update, iteration=" + iterations);
                      				return "";
                      			} else {
                      				logger.info("**** waitForAtBundlingInfoUpdate received an AT Bundling Info Update but it is empty, iteration=" + iterations);
                      			}
          						break;
          					default:
          						return "Unknown bundling info update verificationb scheme";
          				}
          			} else {
          				logger.info("**** waitForAtBundlingInfoUpdate received an AT Bundling Info Update but bundling signature == 0, iteration=" + iterations);
          			}
          		} else {
          			logInfo = "**** waitForAtBundlingInfoUpdate received an AT Bundling Info Update, but for wrong TG. Expected tgId=" + tgId + ", Received tgId=" + receivedTgId + ", sleep for 1s, iteration=" + iterations;
          		}
            } else {
            	logInfo = "**** waitForAtBundlingInfoUpdate did not receive an AT Bundling Info Update, sleep for 1s, iteration=" + iterations;
            }
            logger.info(logInfo);
            ++iterations;
            Thread.sleep(1000); // sleep for a second
    	}
    	return "Could not receive an AO AT Bundling Info Update before timeout";
    }
    
    
    private boolean checkPredefinedBundlingData(List<Integer> bundlingData, List<Integer> teis) {
        HashMap<Integer,Integer> usedTeiSapis = new HashMap<Integer, Integer>();
        for (Integer tei: teis) usedTeiSapis.put(tei, 0);
        logger.info("**** checkPredefinedBundlingData received bundling data: " + bundlingData.toString());
        
        int numOfBgsLeft = bundlingData.get(0);
        int index=1;
        while (index < bundlingData.size()) {
            --numOfBgsLeft;
            if (numOfBgsLeft < 0) {
                logger.info("**** checkBundlingData bundling data size mismatch");
                return false;
            }
            // Ignore size
            ++index;
            
            final int bgId = bundlingData.get(index++);
            if (bgId < 1 || bgId > 254) {
                logger.info("**** checkBundlingData the BG Id is not in accepted interval [1..254], value = " + bgId);
                return false;
            }
            
            final int sapiBits = bundlingData.get(index++);
            final int numOfTeis = bundlingData.get(index++);
            for (int i = 0; i < numOfTeis; ++i) {
                final int tei = bundlingData.get(index++);
                if (usedTeiSapis.containsKey(tei) == false) {
                    logger.info("**** checkBundlingData unexpected tei value. Expected one of: " + teis.toString() + " got " + tei);
                    return false;
                }
                int usedSapiBits = usedTeiSapis.get(tei);
                if ( (usedSapiBits & sapiBits) != 0) {
                    logger.info("**** checkBundlingData SAPI bit overlap detected. BG Id: " + bgId + ", tei: " + tei +
                            ". Already registered SAPI bits for tei: " + usedSapiBits + " Current BG sapis" + sapiBits);
                    return false;
                }
                usedTeiSapis.put(tei, (usedSapiBits | sapiBits));
            }
        }
        
        for (int tei: teis) {
            // Verify that all teis have a BG for each SAPI
            int sapis = usedTeiSapis.get(tei);
            if (sapis != 0b11111) {
                logger.info("**** checkBundlingData tei: " + tei + " does not have a BG for each SAPI. Expected SAPI bits value: 31, actual: " + sapis);
                return false;
            }
        }
        return true;
    }
    
    

    /**
     * @name setNegotiationBehaviourDefault
     * 
     * @description Set Abisco behaviour for Negotiation Request back go default
     *              (ACK).
     */
    public void setDefaultNegotiationBehaviour() {

        BG.SetBehaviourMode behaviourMode;
        BG.BGG31NegotiationSettingClear negSettingClear;

        behaviourMode = bgServer.createSetBehaviourMode();
        behaviourMode.setBGCommand(BG.Enums.BGCommand.G31NegotiationRequest);
        negSettingClear = bgServer.createBGG31NegotiationSettingClear();

        behaviourMode.sendAsync();
        negSettingClear.sendAsync();
    }

    /**
     * @name clearNegotiationRecord1Data
     * 
     * @description Clears the negotiation record 1 data queue, shall be called
     *              before any method invoking Negotiation Request if the data
     *              shall be compared.
     */
    public void clearNegotiationRecord1Data() {

        MessageQueue<BG.G31NegotiationRequest> negotiationRequestQueue;

        negotiationRequestQueue = bgServer.getG31NegotiationRequestQueue();
        negotiationRequestQueue.clear();
    }

    /**
     * @name getNegotiationRecord1Data
     * 
     * @description Returns the negotiation record 1 data
     * 
     * @param negotiationRecord1Data
     * @param expectedOmlIwdVersions
     * @param expectedRslIwdVersions
     * 
     * @return NegotiationRecord1Data List<Integer>, null if record is empty
     */
    public boolean compareNegotiationRecord1Data(List<Integer> negotiationRecord1Data,
                                                 ArrayList<String> expectedOmlIwdVersions, 
                                                 ArrayList<String> expectedRslIwdVersions) {

        NegotiationRequest negotiationRequest = new NegotiationRequest(negotiationRecord1Data);

        return (expectedOmlIwdVersions.equals(negotiationRequest.getOmlIwdVersions()) && expectedRslIwdVersions
                .equals(negotiationRequest.getRslIwdVersions()));
    }

    /**
     * @name getNegotiationRecord1Data
     * 
     * @description Returns the negotiation record 1 data
     * 
     * @return NegotiationRecord1Data List<Integer>, null if record is empty
     */
    public List<Integer> getNegotiationRecord1Data() {

        MessageQueue<BG.G31NegotiationRequest> negotiationRequestQueue;

        negotiationRequestQueue = bgServer.getG31NegotiationRequestQueue();

        return negotiationRequestQueue.remove().getG31NegotiationRecord1().getNegotiationRecord1Data();
    }

    /**
     * @name setNegotiationAckMissingIwd
     * 
     * @description Sets missing IWD version in Negotiation Request EP ACK.
     * 
     * @param iwdVersion - IWD to include in the ACK
     * @param onlyOmlIwd - true implies that Negotiation Ack is sent with only
     *            OML IWD version, false implies that Negotiation Ack is sent
     *            with only RSL IWD version
     */
    public void setNegotiationAckMissingIwd(char[] iwdVersion, boolean onlyOmlIwd) {

        final int nrOfIwdTypes = 1;
        int iwdType;

        BG.G31NegotiationRecord2 negotiationRecord2 = new BG.G31NegotiationRecord2();
        ArrayList<Integer> negotiationRecord2Data;

        // Set number of IWD types in Negotiation Record II
        // Set IWD Type in Negotiation Record II
        // Set IWD Version in Negotiation Record II
        if (onlyOmlIwd) {
            iwdType = this.iwdTypeOml;
        } else {
            iwdType = this.iwdTypeRsl;
        }
        negotiationRecord2Data = new ArrayList<Integer>(Arrays.asList(nrOfIwdTypes, iwdType,
                (int) iwdVersion[0], (int) iwdVersion[1], (int) iwdVersion[2], (int) iwdVersion[3],
                (int) iwdVersion[4], (int) iwdVersion[5]));

        // Set up Abisco handling of Negotiation Request ACK
        negotiationRecord2.setNegotiationRecord2Data(negotiationRecord2Data);
        BG.BGG31NegotiationSettingAck negotiationSettingAck = bgServer.createBGG31NegotiationSettingAck();
        negotiationSettingAck.setG31NegotiationRecord2(negotiationRecord2);
        negotiationSettingAck.sendAsync();
    }

    /**
     * @name setNegotiationTimeoutBehaviour
     * 
     * @description Sets a timeout behavior on Negotiation Request EP
     * 
     * @param timeoutSp - true = timeout entire SP, i.e. all re-send attempts of
     *            Negotiation Request false = timeout once on Negotiation
     *            Request
     */
    public void setNegotiationTimeoutBehaviour(boolean timeoutSp) {

        BG.Enums.BGBehaviour behaviour;
        BG.SetBehaviourMode behaviourMode = bgServer.createSetBehaviourMode();

        // Set up Abisco handling of Negotiation Request
        if (timeoutSp) {
            behaviour = BG.Enums.BGBehaviour.ISSUE_NOANSWER;
        } else {
            behaviour = BG.Enums.BGBehaviour.ISSUE_NOANSWER_THEN_ACK;
        }
        behaviourMode.setBGCommand(BG.Enums.BGCommand.G31NegotiationRequest);
        behaviourMode.setBGBehaviour(behaviour);
        behaviourMode.sendAsync();
    }

    /**
     * @name setNegotiationNack
     * 
     * @description Force NAK in Negotiation Request EP
     */
    public void setNegotiationNack() {
        
        BG.SetBehaviourMode behaviourMode;

        behaviourMode = bgServer.createSetBehaviourMode();

        behaviourMode.setBGBehaviour(BG.Enums.BGBehaviour.ISSUE_NAK);
        behaviourMode.setBGCommand(BG.Enums.BGCommand.G31NegotiationRequest);
        behaviourMode.sendAsync();
    }
    
    public void sendStartToAllMoVisibleInGsmSector(int tgId) throws InterruptedException {
    	startRequest(OM_G31R01.Enums.MOClass.SCF, tgId);
    	startRequest(OM_G31R01.Enums.MOClass.TF, tgId); 
    	startRequest(OM_G31R01.Enums.MOClass.AT, tgId);
    }
    
    public void sendAoAtConfigPreDefBundling(int tgId) throws InterruptedException {
    	AbisHelper.TransportProfile tp = new AbisHelper.TransportProfile();
    	tp.createPredefinedBundlingProfiles();       	
    	atConfigRequest(tp, tgId);
    }    
    
    

    /**
     * Should result in AO AT being enabled (prerequisite for establishing TRX links).
     * Will start sector, start AT, configure AT and/or enable AT, all if necessary.
     * @return True if AT was enabled, false otherwise
     */
    public boolean startSectorMosAndActivateAT() {
        try {
            sendStartToAllMoVisibleInGsmSector(0);
            if (momHelper.waitForAbisAtMoState("DISABLED")) {
                // Configure AT
                AbisHelper.TransportProfile transportProfile = new AbisHelper.TransportProfile();
                transportProfile.createPredefinedBundlingProfiles();
                atConfigRequest(transportProfile, 0);
                // Enable AT
                return enableRequest(OM_G31R01.Enums.MOClass.AT, 0).getConfigurationSignature() != 0;
            }
        } catch (InterruptedException e) {
            logger.warn("Received interrupt in activate AT. Exception: " + e.getMessage());
            return false;
        }
        return momHelper.waitForAbisAtMoState("ENABLED");
    }
    
    public boolean startSectorMosAndActivateAtAndTf() {
        if (!startSectorMosAndActivateAT()) return false;

        try {
            // Configure TF
            OM_G31R01.TFConfigurationResult tfConfigResult = tfConfigRequest(1, Enums.TFMode.Master, getOM_G31R01fsOffsetByActiveSyncSrc());
            if (tfConfigResult.getAccordanceIndication().getAccordanceIndication() != Enums.AccordanceIndication.AccordingToRequest) {
                logger.warn("Could not configure TF");
                return false;
            }

            // Enable TF
            if (OM_G31R01.Enums.MOState.ENABLED != enableRequest(this.moClassTf, 0).getMOState()) {
                logger.info("Could not enable TF");
                return false;
            }
        } catch (InterruptedException e) {
            logger.warn("Received interrupt in activate AT. Exception: " + e.getMessage());
            return false;
        }

        if (!momHelper.waitForAbisTfMoState("ENABLED")) {
            logger.info("abisTfState is not ENABLED");
            return false;
        }
        return true;
    }
    
    public boolean startTrxMos(int trxId) {
        try {
            logger.info("Start all TRXC MOs for trx = " + trxId);
            logger.info("Start TRXC");
            startRequest(OM_G31R01.Enums.MOClass.TRXC, 0, trxId, trxId, ASSOCIATED_SO_INSTANCE);

            logger.info("Start TX");
            startRequest(OM_G31R01.Enums.MOClass.TX, 0, trxId, trxId, ASSOCIATED_SO_INSTANCE);

            logger.info("Start RX");
            startRequest(OM_G31R01.Enums.MOClass.RX, 0, trxId, trxId, ASSOCIATED_SO_INSTANCE);

            for (int tsInstance=0; tsInstance<8; tsInstance++)
            {
                logger.info("Start TS, instance=" + tsInstance);
                startRequest(OM_G31R01.Enums.MOClass.TS, 0, trxId, tsInstance, trxId);
            }

            if (!momHelper.waitForAbisTrxcState(trxId, "STARTED")) {
                logger.info("Trx abisSoTrxcState is not STARTED");
                return false;
            }

            if (!momHelper.waitForAbisTxMoState(trxId, "DISABLED")) {
                logger.info("Trx abisTxState is not DISABLED");
                return false;
            }

            if (!momHelper.waitForAbisRxMoState(trxId, "DISABLED")) {
                logger.info("Trx abisRxState is not DISABLED");
                return false;
            }

            for (int tsInstance=0; tsInstance<8; tsInstance++)
            {   try {
                if (!momHelper.waitForAbisTsMoState(trxId, tsInstance, "DISABLED", 5)) {
                    logger.info("Trx abisTsMoState[" + tsInstance +"] is not DISABLED");
                    return false;
                }
            } catch (JSONException e) {
                logger.error("Received JSON Exception = " + e.getMessage());
                return false;
            }
            }
        } catch (InterruptedException e) {
            logger.warn("Received interrupt in start TRXC. Exception: " + e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * @name TransportProfile
     * 
     * @description Encapsulates a transport profile that is sent in AT COnfig request
     * 
     */    
    
    public static class TransportProfile {
    	private List<BundlingProfile> bundlingProfiles;
   	
        /**
         * @name BundlingProfile
         * 
         * @description Encapsulates a bundling profile in the transport profile
         * 
         */   
    	public static class BundlingProfile{
    		
    		public BundlingProfile(
    				List<Integer> sapiList,
    				Integer bundlingTimeUl,
    				Integer bundlingMaxPayloadSizeUl,
    				boolean useCrc,
    				Integer dscpUl,
    				Integer overloadThreashold,
    				Integer overloadReportInterval) {
    			this.sapiList                 = sapiList;
        		this.bundlingTimeUl           = bundlingTimeUl;
        		this.bundlingMaxPayloadSizeUl = bundlingMaxPayloadSizeUl;
        		this.useCrc                   = useCrc;
        		this.dscpUl                   = dscpUl;
        		this.overloadThreashold       = overloadThreashold;
        		this.overloadReportInterval   = overloadReportInterval;
    		}
    		
    		List<Integer>  sapiList;
    		public Integer bundlingTimeUl;
    		public Integer bundlingMaxPayloadSizeUl;
    		public boolean useCrc;
    		public Integer dscpUl;
    		public Integer overloadThreashold;
    		public Integer overloadReportInterval;
    	}
    	
    	public TransportProfile() {
    		bundlingProfiles = new ArrayList<BundlingProfile>();
    	}
 
    	public void addBp(BundlingProfile bp) {
    		bundlingProfiles.add(bp);
    	}
    	
        public void createPredefinedBundlingProfiles() {
            // create the default bundling profile 
        	bundlingProfiles.add(new AbisHelper.TransportProfile.BundlingProfile(Arrays.asList(AbisHelper.SAPI_RSL, AbisHelper.SAPI_OML), 1, 1465, true, 51, 0, 1));
        	bundlingProfiles.add(new AbisHelper.TransportProfile.BundlingProfile(Arrays.asList(AbisHelper.SAPI_CS,  AbisHelper.SAPI_CSD), 1, 1465, true, 46, 0, 1));
        	bundlingProfiles.add(new AbisHelper.TransportProfile.BundlingProfile(Arrays.asList(AbisHelper.SAPI_PS),                       1, 1465, true, 28, 0, 1));
        }

        /**
         * @name toIntegerList
         * 
         * @description Converting the Transport Profile into a list of integers that can be used to send the AT Config req
         * 
         * 
         * @return List<Integer> 
         * 
         */
        public List<Integer> toIntegerList() {
        	List<Integer> transportProfilesOctets = new ArrayList<Integer>();
            transportProfilesOctets.add(bundlingProfiles.size());  // number of profiles
  
            for (BundlingProfile bp : bundlingProfiles) {
            	transportProfilesOctets.add(0xA);  //length of profile 1 including this octet, as far as I can see this will always be the same
            	transportProfilesOctets.add(convertSapiListToBitMask(bp.sapiList)); //SAPIs as a bit mask 
            	transportProfilesOctets.add(bp.bundlingTimeUl);  //bundling time
            	transportProfilesOctets.add(getValueOfBit8To15(bp.bundlingMaxPayloadSizeUl)); //max payload size high octet
            	transportProfilesOctets.add(getValueOfBit0To7(bp.bundlingMaxPayloadSizeUl));  //max payload size low octet
                transportProfilesOctets.add(bp.useCrc ? 1 : 0);  //CRC 
                transportProfilesOctets.add(bp.dscpUl); //DSCP
                transportProfilesOctets.add(getValueOfBit8To15(bp.overloadThreashold));  //overload threshold high octet
                transportProfilesOctets.add(getValueOfBit8To15(bp.overloadThreashold));  //overload threshold low octet
                transportProfilesOctets.add(bp.overloadReportInterval);  //overload report interval
            }
        	return transportProfilesOctets;
        }

        private Integer convertSapiListToBitMask(List<Integer> sapiList) {
        	Integer sapibits=0;
        	
        	for (Integer sapi : sapiList) {
        		
        		switch (sapi) {
        			case SAPI_OML:
        				sapibits |= (1 << SAPI_INDEX_OML);
        				break;
        			case SAPI_RSL:
        				sapibits |= (1 << SAPI_INDEX_RSL);
        				break;
        			case SAPI_CS:
        				sapibits |= (1 << SAPI_INDEX_CS);
        				break;
        			case SAPI_CSD:
        				sapibits |= (1 << SAPI_INDEX_CSD);
        				break;
        			case SAPI_PS:
        				sapibits |= (1 << SAPI_INDEX_PS);
        				break;            				
        		}
        	}
        	return sapibits;
        }
        
        private Integer getValueOfBit8To15(Integer input) {
        	Integer byte1 = input >> 8;
        	byte1 = byte1 & 0xFF;
        	return byte1;
        }
        
        private Integer getValueOfBit0To7(Integer input) {
        	Integer byte0 = input & 0xFF;
        	return byte0;
        }
    }

    
    private ChannelNoStruct channelNo (ChannelType chanT, int tn)
    {
    	ChannelNoStruct chanNo = new ChannelNoStruct ();
    	chanNo.setTimeSlotNo (tn);
    	chanNo.setChannelType (chanT);
    	return chanNo;
    }
    
    /**
     * @name channelActivationNormalAssignment
     * 
     * @description Builds a ChannelActNormalAssign message
     * 
     * @param tn - timeslot number
     * 
     * @return ChannelActNormalAssign
     * 
     */
    public ChannelActNormalAssign channelActivationNormalAssignment (int tn) {
        return channelActivationNormalAssignment (0, 0, tn);
    }

    
    /**
     * @name channelActivationNormalAssignment
     * 
     * @description Builds a ChannelActNormalAssign message
     * 
     * @param tg - Transciever group
     * @param trxc - TRXC
     * @param tn - timeslot number
     * 
     * @return ChannelActNormalAssign
     * 
     */
	public ChannelActNormalAssign channelActivationNormalAssignment (int tg, int trxc, int tn) {

        ChannelActNormalAssign msg = getRslServer().createChannelActNormalAssign();
        msg.getCH_Action().setSyntaxCheckOff();
        
        Routing routing = new Routing();
        routing.setTG(tg);
        routing.setTRXC(trxc);
        msg.setRouting(routing);

        msg.setChannelNoStruct (channelNo (ChannelType.Bm, tn));

        ActivationTypeStruct actType = new ActivationTypeStruct ();
        actType.setActivationType(ActivationType.INTRA_NOR);
        msg.setActivationTypeStruct(actType);

        ChannelModeStruct chanMode = new ChannelModeStruct ();
        chanMode.setTypeOfCh(TypeOfCh.SPEECH);
        msg.setChannelModeStruct(chanMode);

        MSPowerStruct msPow = new MSPowerStruct();
        msPow.setPowerLevel(10);
        msg.setMSPowerStruct(msPow);

        BSPowerStruct bsPow = new BSPowerStruct ();
        bsPow.setRawBSPower(14);
        msg.setBSPowerStruct(bsPow);

        TimingAdvanceStruct timAdv = new TimingAdvanceStruct();
        timAdv.setTimingAdvanceValue(0);
        msg.setTimingAdvanceStruct(timAdv);

        return msg;
    }
	
	/**
	 * @name channelRelease
	 * 
	 * @description Builds a channelRelease message
	 * 
	 * @param tn - timeslot number
	 * 
	 * @return RFChannelRelease
	 * 
	 */
    public RFChannelRelease channelRelease (int tn) {
        return channelRelease(0, 0, tn);
    }
    
    /**
     * @name channelRelease
     * 
     * @description Builds a channelRelease message
     * 
     * @param tg - Transceiver Group
     * @param trxc - TRXC index
     * @param tn - timeslot number
     * 
     * @return RFChannelRelease
     * 
     */
    public RFChannelRelease channelRelease (int tg, int trxc, int tn) {
		RFChannelRelease msg = getRslServer().createRFChannelRelease();
		msg.setRouting(new Routing(tg, trxc));
		msg.setChannelNoStruct (channelNo (ChannelType.Bm, tn));
		return msg;	
	}
    
    /**
     * Start generating speech frames
     * @param tg - Transceiver Group
     * @param trxc - TRXC id
     * @param ts - Timeslot
     * @throws InterruptedException 
     */
    public void activateSpeech(int tg, int trxc, int ts) throws InterruptedException {
        deactivateSpeech(tg, trxc, ts); // In case some other testcase left it on
        ActivateSpeech msg = getPayloadServer().createActivateSpeech();
        msg.setChannelType(com.ericsson.abisco.clientlib.servers.PAYLOAD.Enums.ChannelType.Bm);
        msg.setSpeechGenerationMode(SpeechGenerationMode.Frames);
        msg.setTG(tg);
        msg.setTRXC(trxc);
        msg.setTS(ts);
        msg.setDTX(DTX.NotSupported);
        SpeechPatternStruct sps = new SpeechPatternStruct();
        sps.setType1(Type1.Speech);
        sps.setTime1(1);
        sps.setType2(Type2.Speech);
        sps.setTime2(1);
        msg.setSpeechPatternStruct(sps);
        msg.send();
    }
    
    /**
     * Stop generating speech frames
     * @param tg - Transceiver Group
     * @param trxc - TRXC id
     * @param ts - Timeslot
     * @throws InterruptedException
     */
    public void deactivateSpeech(int tg, int trxc, int ts) throws InterruptedException {
        // Deactivate any current speech payload on the TS
        DeactivateSpeech msg = getPayloadServer().createDeactivateSpeech();
        msg.setTG(tg);
        msg.setTRXC(trxc);
        msg.setTS(ts);
        msg.send();
    }

    /**
     * @name enableAbisLogging
     *
     * @description enableAbisLogging
     *
     * @throws InterruptedException
     */
    public void enableAbisLogging(int tgId) throws InterruptedException {

    	CMDHLIB.AbisLogging abisLogging = cmdhlib.createAbisLogging();

    	abisLogging.setSTATE(CMDHLIB.Enums.STATE.ON);
    	abisLogging.setTGId(tgId);
    	abisLogging.send();
    }

    /**
     * @name disableAbisLogging
     *
     * @description disableAbisLogging
     *
     * @throws InterruptedException
     */
    public void disableAbisLogging(int tgId) throws InterruptedException {

    	CMDHLIB.AbisLogging abisLogging = cmdhlib.createAbisLogging();

    	abisLogging.setSTATE(CMDHLIB.Enums.STATE.OFF);
    	abisLogging.setTGId(tgId);
    	abisLogging.send();
    }

    /**
     * @name activateForwardChReqToBG
     *
     * @description activateForwardChReqToBG
     */
    public void activateForwardChReqToBG() throws InterruptedException {
        PARAMDISP.ForwardChReqToBG forwardChReqToBG = paramdisp.createForwardChReqToBG();
        forwardChReqToBG.setSTATE(PARAMDISP.Enums.STATE.ForwardOnly);
        forwardChReqToBG.send();
    }

    /**
     * @name deActivateForwardChReqToBG
     *
     * @description deActivateForwardChReqToBG
     */
    public void deActivateForwardChReqToBG() throws InterruptedException {
        PARAMDISP.ForwardChReqToBG forwardChReqToBG = paramdisp.createForwardChReqToBG();
        forwardChReqToBG.setSTATE(PARAMDISP.Enums.STATE.ProcessOnly);
        forwardChReqToBG.send();
    }

    private boolean isTrxcMo(MOClass moClass) {
    	return moClass == MOClass.TRXC || moClass == MOClass.RX || moClass == MOClass.TX ||
    			moClass == MOClass.TS;
	}
    
    /**
     * @name sysInfo
     *
     * @description Set sysInfo for wp 1435
     */ 
    public void sysInfo(boolean enableBcchCombined) throws InterruptedException {
    	//pre-condition to enable lock on cell 
    	logger.info("Set sysInfo with bcchCombined");
    	bcchCombined = enableBcchCombined;
    	sendBCCHInfo();
    	// Set to default value
    	bcchCombined = false;
    }
    
    /**
     * @name sysInfo
     *
     * @description Set sysInfo
     */ 
    public void sysInfo() throws InterruptedException {
    	//pre-condition to enable lock on cell 
    	logger.info("Set sysInfo");
    	sendBCCHInfo();
    }
 
    /**
     * @name enableTf
     *
     * @description Enable AO TF
     */ 
    public boolean enableTf() throws InterruptedException {
    	//pre-condition to enable AO RX/TX/TS 
    	logger.info("Enable AO TF as a precondition to enable AO RX/TX");
    	OM_G31R01.EnableResult result = enableRequest(this.moClassTf, 0);
    	
    	if(result.getMOState() != OM_G31R01.Enums.MOState.ENABLED)
    	{
    		logger.info("AO TF is not ENABLED");
    		return false;
    	}
    	
    	return true;
    }
    
    /**
     * @name disableTf
     *
     * @description Disable AO TF
     */
    public boolean disableTf() throws InterruptedException {
    	OM_G31R01.DisableResult result = disableRequest(this.moClassTf);

    	if(result.getMOState() != OM_G31R01.Enums.MOState.DISABLED)
    	{
    		logger.info("AO TF is not DISABLED");
    		return false;
    	}

    	return true;
    }
    
    /**
     * @name enableTx
     *
     * @description Enable AO TX
     */     
    public boolean enableTx() throws InterruptedException {
    	logger.info("Verify that MO State is DISABLED");
    	
    	if(!(momHelper.waitForAbisTxMoState("DISABLED")))
    	{
    		logger.info("abisTxMoState is not DISABLED");
    		return false;
    	}

        logger.info("Configure AO TX");
        OM_G31R01.TXConfigurationResult confRes = txConfigRequest(0, momHelper.getArfcnToUse(), false, 27, true);
        
    	if(confRes.getAccordanceIndication().getAccordanceIndication() != Enums.AccordanceIndication.AccordingToRequest)
    	{
    		logger.info("According Indication must be According to Request");
    		return false;
    	}    	
        
        logger.info("Send Enable Request to AO TX");
        OM_G31R01.EnableResult result = enableRequest(this.moClassTx, 0);
        
        logger.info("Verify that MO State in Enable Result is ENABLED");
        
        if(result.getMOState() != OM_G31R01.Enums.MOState.ENABLED)
        {
        	logger.info("MoState is not ENABLED");
    		return false;
        }
        
        logger.info("Verify that MO:Trx attribute:abisTxMoState is ENABLED");
        
        if(!(momHelper.waitForAbisTxMoState("ENABLED")))
        {
        	logger.info("abisTxMoState is not ENABLED");
    		return false;
        }
        
        return true;
    }

    /**
     * @name enableRx
     *
     * @description Enable AO RX
     */   
    public boolean enableRx() throws InterruptedException {    	
    	logger.info("Verify that MO State is DISABLED");
    	
    	if(!(momHelper.waitForAbisRxMoState("DISABLED")))
    	{
    		logger.info("abisRxMoState is not DISABLED");
    		return false;
    	}
    	
        logger.info("Configure AO RX");
        OM_G31R01.RXConfigurationResult confRes = rxConfigRequest(0, momHelper.getArfcnToUse(), false, "");
        
        if(confRes.getAccordanceIndication().getAccordanceIndication() != Enums.AccordanceIndication.AccordingToRequest)
        {
    		logger.info("According Indication must be According to Request");
    		return false;
        }     
        logger.info("Send Enable Request to AO RX");
        OM_G31R01.EnableResult result = enableRequest(this.moClassRx, 0);
        
        logger.info("Verify that MO State in Enable Result is ENABLED");
        
        if(result.getMOState() != OM_G31R01.Enums.MOState.ENABLED)
        {
    		logger.info("MoState is not ENABLED");
    		return false;
        }
  
        logger.info("Verify that MO:Trx attribute:abisRxMoState is ENABLED");

        if(!(momHelper.waitForAbisRxMoState("ENABLED")))
        {
    		logger.info("abisRxMoState is not ENABLED");
    		return false;
        }
        
        return true;
    }

    /**
     * @name enableTsBcch
     *
     * @description Enable TS BCCH
     */   
    public boolean enableTsBcch() throws InterruptedException, JSONException {
    	return (enableTs(0, OM_G31R01.Enums.Combination.MainBCCH));
    }
    
    /**
     * @name enableTsSdcch
     *
     * @description Enable TS SDCCH
     */   
    public boolean enableTsSdcch() throws InterruptedException, JSONException {
    	return (enableTs(1, OM_G31R01.Enums.Combination.SDCCH));
    }
    
    /**
     * @name enableTsTch
     *
     * @description Enable TS TCH
     */     
    public boolean enableTsTch() throws InterruptedException, JSONException {
    	return (enableTs(2, OM_G31R01.Enums.Combination.TCH));
    }   
    
    /**
     * @name enableTsTch
     *
	 * @param tn - timeslot number
	 * 
     * @description Enable TS TCH on specific timeslot
     */     
    public boolean enableTsTch(int tn) throws InterruptedException, JSONException {
    	return (enableTs(tn, OM_G31R01.Enums.Combination.TCH));
    }   
    
    /**
     * @name enableTs
     *
     * @description Enable TS
     */   
    private boolean enableTs(int tsInstance, OM_G31R01.Enums.Combination combination) throws InterruptedException, JSONException {

    	int associatedSoInstance = 0;

    	logger.info("Verify that MO State for instance "+ tsInstance + " is DISABLED");  
    	
    	if(!momHelper.waitForAbisTsMoState(tsInstance, "DISABLED", 5))
    	{
    		logger.info("abisTsMoState for tsInstance " + tsInstance + " is not DISABLED");
    		return false;
    	}
    	
    	logger.info("Configure AO TS instance " + tsInstance);
    	OM_G31R01.TSConfigurationResult confRes = tsConfigRequestFirstCall(tsInstance, combination);
    	
    	if(confRes.getAccordanceIndication().getAccordanceIndication() != OM_G31R01.Enums.AccordanceIndication.AccordingToRequest)
    	{
    		logger.info("AccordanceIndication is not AccordingToRequest");
    		return false;
    	}

    	logger.info("Send Enable Request to AO TS instance " + tsInstance);
    	OM_G31R01.EnableResult result = enableRequest(this.moClassTs, tsInstance, associatedSoInstance);

    	logger.info("Verify that MO State in Enable Result is ENABLED");
    	
    	if(result.getMOState() != OM_G31R01.Enums.MOState.ENABLED)
    	{
    		logger.info("MO state is not ENABLED");
    		return false;
    	}
    	
    	logger.info("Verify that MO:Trx attribute:abisTsMoState for instance " + tsInstance + " is ENABLED");
    	
    	if(!(momHelper.waitForAbisTsMoState(tsInstance, "ENABLED", 5)))
    	{
    		logger.info("abisTsMoState for tsInstance " + tsInstance + " is not ENABLED");
    		return false;
    	}
    	    	
    	return true;
    }
    
    /**
     * 
     * @name sendBCCHInfoWithCellId
     * @param tgId - int
     * @param trxc - ubt
     * @description sends sendBCCHInfoP1_1 for the given tgId and trxc
     * 
     * 
     * @throws InterruptedException
     */
    public void sendBCCHInfoWithCellId(int tgId, int trxc) throws InterruptedException {
        
        ChannelNoStruct channelNoStruct = new ChannelNoStruct();
        channelNoStruct.setTimeSlotNo(0);
        channelNoStruct.setChannelType(ChannelType.BCCH);
        
        Routing routing = new Routing();
        routing.setTG(tgId);
        routing.setTRXC(trxc);
        
        LAIStruct laiStruct = new LAIStruct();
        List<Integer> mccList = new ArrayList<Integer> ();
        mccList.add(2);
        mccList.add(4);
        mccList.add(0);
        laiStruct.setMCCDigit(mccList);
        
        NeighbourCellsDescr neighbourCellsDescr = new NeighbourCellsDescr();
        NeighbourCellsDescrStruct neighbourCellsDescrStruct = new NeighbourCellsDescrStruct();
        neighbourCellsDescrStruct.setBA_ARFCN121_124(0);
        neighbourCellsDescrStruct.setBA_Ind(0);
        neighbourCellsDescrStruct.setSpare(0);
        neighbourCellsDescrStruct.setBA_No(0);
        List<Integer> neighbourCellList = new ArrayList<Integer> ();
        for (int i=0; i<15; i++)
            neighbourCellList.add(0);
        neighbourCellsDescrStruct.setBA_ARFCN1_120(neighbourCellList);
        neighbourCellsDescr.setNeighbourCellsDescrStruct(neighbourCellsDescrStruct);
        
        
        List<Integer> mncList = new ArrayList<Integer> ();
        mncList.add(9);
        mncList.add(9);
        laiStruct.setMNCDigit(mncList);
        
        List<Integer> lacList = new ArrayList<Integer> ();
        lacList.add(0);
        lacList.add(1);
        laiStruct.setLAC(lacList);
        
        TRAFFRN.CellSelectionParamStruct cellSelectionParamStruct = new TRAFFRN.CellSelectionParamStruct();
        cellSelectionParamStruct.setMSTXPowerMaxCCH(3);
        cellSelectionParamStruct.setCellReselectHysteresis(2);
        cellSelectionParamStruct.setRXLevelAccessMin(35);
        cellSelectionParamStruct.setACS(0);
        
        TRAFFRN.RACHControlParamStruct rachControlParamStruct = new TRAFFRN.RACHControlParamStruct();
        rachControlParamStruct.setCallReestablishmentAllowed(1);
        
        TRAFFRN.PLMNPermittedStruct plmnPermittedStruct = new TRAFFRN.PLMNPermittedStruct();
        plmnPermittedStruct.setNCCPermitted(255);
        
        TRAFFRN.ControlChannelDescrStruct controlChannelDescrStruct = new TRAFFRN.ControlChannelDescrStruct();
        controlChannelDescrStruct.setBS_AG_BLKS_RES(1);
        controlChannelDescrStruct.setBS_PA_MFRMS(1);
        
        CellIdAirStruct cellIdAirStruct = new CellIdAirStruct();
        List<Integer> cellId = new ArrayList<Integer>();
        cellId.add(0);
        cellId.add(tgId);
        cellIdAirStruct.setCellIdValue(cellId);
        
        TRAFFRN.BCCHInfoP2_2 bcchInfo2_2 = traffRnServer.createBCCHInfoP2_2();

        bcchInfo2_2.setRouting(routing);
        bcchInfo2_2.setChannelNoStruct(channelNoStruct);
        bcchInfo2_2.setPLMNPermittedStruct(plmnPermittedStruct);
        bcchInfo2_2.setRACHControlParamStruct(rachControlParamStruct);
        bcchInfo2_2.setNeighbourCellsDescr(neighbourCellsDescr);

  
        
        TRAFFRN.BCCHInfoP2_3 bcchInfo2_3 = traffRnServer.createBCCHInfoP2_3();
        bcchInfo2_3.setRouting(routing);
        bcchInfo2_3.setChannelNoStruct(channelNoStruct);
        bcchInfo2_3.setControlChannelDescrStruct(controlChannelDescrStruct);
        bcchInfo2_3.setLAIStruct(laiStruct);
        bcchInfo2_3.setCellSelectionParamStruct(cellSelectionParamStruct);
        bcchInfo2_3.setRACHControlParamStruct(rachControlParamStruct);
        bcchInfo2_3.setCellIdAirStruct(cellIdAirStruct);
        
        
        
        TRAFFRN.BCCHInfoP2_4 bcchInfo2_4 = traffRnServer.createBCCHInfoP2_4();
        bcchInfo2_4.setRouting(routing);
        bcchInfo2_4.setChannelNoStruct(channelNoStruct);
        bcchInfo2_4.setLAIStruct(laiStruct);
        bcchInfo2_4.setCellSelectionParamStruct(cellSelectionParamStruct);
        bcchInfo2_4.setRACHControlParamStruct(rachControlParamStruct);

        
        logger.info("bcchInfo2_2 = " + bcchInfo2_2);
        logger.info("bcchInfo2_3 = " + bcchInfo2_3);
        logger.info("bcchInfo2_4 = " + bcchInfo2_4);
        
        try {
            bcchInfo2_2.send();
            
        } catch (UNACKNOWLEDGED_MESSAGE_SENTException e) {
            logger.debug("bcchInfo2_2 -> UNACKNOWLEDGED_MESSAGE_SENT Exception as expected");
        }
        try {
            bcchInfo2_3.send();
        
        } catch (UNACKNOWLEDGED_MESSAGE_SENTException e) {
            logger.debug("bcchInfo2_3 -> UNACKNOWLEDGED_MESSAGE_SENT Exception as expected");
        }
        try {
            bcchInfo2_4.send();
            
        } catch (UNACKNOWLEDGED_MESSAGE_SENTException e) {
            logger.debug("bcchInfo2_4 -> UNACKNOWLEDGED_MESSAGE_SENT Exception as expected");
        }

        //traffRnServer.createChannelActNormalAssign();
       
    }
    
    public OM_G31R01.TSConfigurationRequest buildTsConfigRequestFirstCall(int tgId, int trxId, int tsInstance, 
            OM_G31R01.Enums.Combination combination, 
            boolean setParamsToDefaultValue ) throws InterruptedException {
       
         Map<Integer, Integer> frequencyList = new HashMap<Integer, Integer>();
         frequencyList.put(trxId, arfcnOnTrx);
        
        
        
        OM_G31R01.TSConfigurationRequest confReq;        
        confReq = omServer.createTSConfigurationRequest();
        confReq.getRouting().setTG(tgId);
        confReq.getRouting().setTRXC(trxId);
        confReq.setInstanceNumber(tsInstance);
        confReq.setAssociatedSOInstance(trxId);       
        
        // HSN: 0-63
        confReq.setHSN(new OM_G31R01.HSN(tsInstance));
        
        // MAIO: 0-63
        confReq.setMAIO(new OM_G31R01.MAIO(tsInstance));
        
        // BSIC, BCC: 0-7, PLMN: 0-7
        confReq.setBSIC(new OM_G31R01.BSIC(1, 7));
        
        // skip FN Offset - all TN must have the same FN offset, 
        // ie. this parameter is only possible to change in state RESET, so it will be tested elsewhere...
        
        // Extended Range Indicator, 0 - 1
        confReq.setExtendedRangeIndicator(new OM_G31R01.ExtendedRangeIndicator(Enums.ExtendedRangeIndicator.Normal));
        confReq.setTimeSlotNumber(new OM_G31R01.TimeSlotNumber(tsInstance)); 
        // Set Combination 
        switch (combination)
        {
        case MainBCCH:
        {
            confReq.setCombination(new OM_G31R01.Combination(OM_G31R01.Enums.Combination.MainBCCH));
            OM_G31R01.CombChoice combChoice = new OM_G31R01.CombChoice();
            combChoice.setCombinationMainBCCH(buildCombinationMainBCCHFirstCall());
            confReq.setCombChoice(combChoice);
            break;
        }
        case SDCCH:
        {
            confReq.setCombination(new OM_G31R01.Combination(OM_G31R01.Enums.Combination.SDCCH));
            OM_G31R01.CombChoice combChoice = new OM_G31R01.CombChoice();
            combChoice.setCombinationSDCCH(buildCombinationSDCCHFirstCall());
            confReq.setCombChoice(combChoice);
            break;
        }
        case TCH:
        {
            confReq.setCombination(new OM_G31R01.Combination(OM_G31R01.Enums.Combination.TCH));
            OM_G31R01.CombinationTCH tchComb = buildCombinationTCHFirstCall(setParamsToDefaultValue);  
            confReq.setCombChoice(new OM_G31R01.CombChoice(tchComb));
            break;
        }
        default:
            throw new IllegalArgumentException("Combination: " + combination + " is currently not supported, the only supported combinations are: MainBCCHCombined, MainBCCH and TCH");
        }
        // Frequency list
        
        List<Integer> frequencyLst = new ArrayList<Integer>();
        
        for (int txRxAddress : frequencyList.keySet()) {
            int arfcnToUse = frequencyList.get(txRxAddress);
            frequencyLst.add(txRxAddress);
            frequencyLst.add(txRxAddress);
            if (trxId==0){
            frequencyLst.add((arfcnToUse >> 8) | 4);
            }
            else{
            frequencyLst.add((arfcnToUse >> 8) | 0);    
            }frequencyLst.add(arfcnToUse & 255);
        }
        confReq.setFrequencyList(frequencyLst);

        return confReq;
    } 
    
    /**
     * Should result in AO AT being enabled (prerequisite for establishing TRX links).
     * Will start sector, start AT, configure AT and/or enable AT, all if necessary.
     * @return True if AT was enabled, false otherwise
     */
    public boolean startSectorMosAndActivateAT(int tgId) {
        String sectorMoLdn = String.format("ManagedElement=1,BtsFunction=1,GsmSector=%s", Integer.toString(tgId+1));
        try {
            sendStartToAllMoVisibleInGsmSector(tgId);
            if (momHelper.waitForMoAttributeStringValue(sectorMoLdn, "abisAtState", "DISABLED", 5)) {
                // Configure AT
                AbisHelper.TransportProfile transportProfile = new AbisHelper.TransportProfile();
                transportProfile.createPredefinedBundlingProfiles();
                atConfigRequest(transportProfile, tgId);
                // Enable AT
                return enableRequest(OM_G31R01.Enums.MOClass.AT, tgId).getConfigurationSignature() != 0;
            }
        } catch (InterruptedException e) {
            logger.warn("Received interrupt in activate AT. Exception: " + e.getMessage());
            return false;
        }
        return momHelper.waitForMoAttributeStringValue(sectorMoLdn, "abisAtState", "ENABLED", 5);
    }

    /**
     * @name enableTf
     * @param tgId
     * @description Enable AO TF
     */ 
    public boolean enableTf(int tgId) throws InterruptedException {
        //pre-condition to enable AO RX/TX/TS 
        logger.info("Enable AO TF as a precondition to enable AO RX/TX");
        OM_G31R01.EnableResult result = enableRequest(this.moClassTf, tgId);
        
        if(result.getMOState() != OM_G31R01.Enums.MOState.ENABLED)
        {
            logger.info("AO TF is not ENABLED");
            return false;
        }
        
        return true;
    }
    
    /**
     * @name enableTx
     * @param tgId, trxId, freq
     * @description Enable AO TX
     */ 
    public boolean enableTx(int tgId, int trxId,int freq) throws InterruptedException {
        logger.info("Verify that MO State is DISABLED");
        String trxMoLdn = String.format("ManagedElement=1,BtsFunction=1,GsmSector=%s,Trx=%s", Integer.toString(tgId+1),trxId);
        if(!momHelper.waitForMoAttributeStringValue(trxMoLdn, "abisTxState", "DISABLED", 10))
        {
            logger.info("abisTxMoState is not DISABLED");
            return false;
        }
       
        logger.info("Configure AO TX");
      //  OM_G31R01.TXConfigurationResult confRes = txConfigRequest(trxId, momHelper.getArfcnToUse(), false, 37, true);
        OM_G31R01.TXConfigurationResult confRes = txConfigRequest(tgId, trxId, trxId, freq, false, 27, true);
        
        if(confRes.getAccordanceIndication().getAccordanceIndication() != Enums.AccordanceIndication.AccordingToRequest)
        {
            logger.info("Accordance Indication must be According to Request");
            return false;
        }       
        
        logger.info("Send Enable Request to AO TX");
        OM_G31R01.EnableResult result = enableRequest(this.moClassTx,tgId,trxId,trxId,ASSOCIATED_SO_INSTANCE);
        
        logger.info("Verify that MO State in Enable Result is ENABLED");
        
        if(result.getMOState() != OM_G31R01.Enums.MOState.ENABLED)
        {
            logger.info("MoState is not ENABLED");
            return false;
        }
        
        logger.info("Verify that MO:Trx attribute:abisTxMoState is ENABLED");
        
        if(!(momHelper.waitForMoAttributeStringValue(trxMoLdn, "abisTxState", "ENABLED", 10)))
        {
            logger.info("abisTxMoState is not ENABLED");
            return false;
        }
        
        return true;
    }
    
    /**
     * @name enableRx
     * @param tgId, trxId, freq
     * @description Enable AO RX
     */ 
    public boolean enableRx(int tgId, int trxId,int freq) throws InterruptedException {       
        logger.info("Verify that MO State is DISABLED");
        String trxMoLdn = String.format("ManagedElement=1,BtsFunction=1,GsmSector=%s,Trx=%s", Integer.toString(tgId+1),trxId);
        if(!( momHelper.waitForMoAttributeStringValue(trxMoLdn, "abisRxState", "DISABLED", 10)))
        {
            logger.info("abisRxMoState is not DISABLED");
            return false;
        }
        
        logger.info("Configure AO RX");
     //   OM_G31R01.RXConfigurationResult confRes = rxConfigRequest(trxId, momHelper.getArfcnToUse(), false, "");
        OM_G31R01.RXConfigurationResult confRes = rxConfigRequest(tgId, trxId, trxId, trxId, freq, false, "");
        
        if(confRes.getAccordanceIndication().getAccordanceIndication() != Enums.AccordanceIndication.AccordingToRequest)
        {
            logger.info("According Indication must be According to Request");
            return false;
        }     
        
        logger.info("Send Enable Request to AO RX");
       // OM_G31R01.EnableResult result = enableRequest(this.moClassRx, trxId);
        OM_G31R01.EnableResult result = enableRequest(this.moClassRx,tgId,trxId,trxId,ASSOCIATED_SO_INSTANCE);
        
        
        logger.info("Verify that MO State in Enable Result is ENABLED");
        
        if(result.getMOState() != OM_G31R01.Enums.MOState.ENABLED)
        {
            logger.info("MoState is not ENABLED");
            return false;
        }
  
        logger.info("Verify that MO:Trx attribute:abisRxMoState is ENABLED");

        if(!(momHelper.waitForMoAttributeStringValue(trxMoLdn, "abisRxState", "ENABLED", 10)))
        {
            logger.info("abisRxMoState is not ENABLED");
            return false;
        }
        
        return true;
    }
    
    public boolean enableAnyTsMy(int tgId, int trxId ,int soInstance, int tsInstance, OM_G31R01.Enums.Combination combination,int freq) throws InterruptedException, JSONException {
            
            int associatedSoInstance = soInstance;
            this.arfcnOnTrx =freq;
            
            logger.info("Verify that MO State for instance "+ tsInstance + " is DISABLED");  
            String trxMoLdn = String.format("ManagedElement=1,BtsFunction=1,GsmSector=%s,Trx=%s", Integer.toString(tgId+1),trxId);
        //  if(!momHelper.waitForAbisTsMoState(tsInstance, "DISABLED", 5, 1))
            if(!(momHelper.waitForAbisTsMoStateGeneral(trxMoLdn, tsInstance, "DISABLED", 5)))
            {
                logger.info("abisTsMoState for tsInstance " + tsInstance + " is not DISABLED");
                return false;
            }
            
            logger.info("Configure AO TS instance " + tsInstance);
            OM_G31R01.TSConfigurationResult confRes = tsConfigRequestFirstCall(tgId, soInstance,tsInstance, combination,true);
            
            if(confRes.getAccordanceIndication().getAccordanceIndication() != OM_G31R01.Enums.AccordanceIndication.AccordingToRequest)
            {
                logger.info("AccordanceIndication is not AccordingToRequest");
                return false;
            }

            logger.info("Send Enable Request to AO TS instance " + tsInstance);
            OM_G31R01.EnableResult result = enableRequest(this.moClassTs, tgId, associatedSoInstance, tsInstance, associatedSoInstance);

            logger.info("Verify that MO State in Enable Result is ENABLED");
            
            if(result.getMOState() != OM_G31R01.Enums.MOState.ENABLED)
            {
                logger.info("MO state is not ENABLED");
                return false;
            }
            
            logger.info("Verify that MO:Trx attribute:abisTsMoState for instance " + tsInstance + " is ENABLED");
            
            if(!(momHelper.waitForAbisTsMoStateGeneral(trxMoLdn, tsInstance, "ENABLED", 5)))
                {
                    logger.info("abisTsMoState for tsInstance " + tsInstance + " is not ENABLED");
                    return false;
                }
                    
            return true;
        }
    
    public OM_G31R01.TSConfigurationResult tsConfigRequestFirstCall(int tgId, int trxInd,int tsInstance, OM_G31R01.Enums.Combination combination,  boolean setParamsToDefaultValue) throws InterruptedException {

        OM_G31R01.TSConfigurationRequest confReq = buildTsConfigRequestFirstCall(tgId,trxInd,tsInstance, combination, setParamsToDefaultValue);
        return confReq.send();
    }
    

    public CMDHAND.InfoTGResponse sendInfoTG(int tgId) 
    {
      CMDHAND.InfoTG cmdInfoTg = abiscoClient.getCMDHAND().createInfoTG();
      cmdInfoTg.setTGId(tgId);
      CMDHAND.InfoTGResponse cmdInfoTgRsp = null;
      try
      {
        cmdInfoTgRsp = cmdInfoTg.send();
      } 
      catch (InterruptedException e)
      {
        logger.error("Failed to send message InfoTG.", e);
      }
      return cmdInfoTgRsp;
    }


    public InfoCellResponse sendInfoCell(int tgId, int cellNum)
    {
      // Verify existing Arfcn in the BTS
      CMDHAND.InfoCell cmdInfoCell = abiscoClient.getCMDHAND().createInfoCell();
      cmdInfoCell.setTGId(tgId);
      cmdInfoCell.setCellNumber(cellNum);
      CMDHAND.InfoCellResponse cmdInfoCellRsp = null;
      try
      {
        cmdInfoCellRsp = cmdInfoCell.send();
      } 
      catch (InterruptedException e)
      {
        logger.error("Failed to send message InfoCell.", e);
      }
      return cmdInfoCellRsp;
    }
    
    public OM_G31R01.FSOffset getOM_G31R01fsOffsetByActiveSyncSrc() {
        Integer FSOffsetHigh8;
        long FSOffsetLow32;
        if(momHelper.isTimeSyncActive()){
            // time sync
            FSOffsetHigh8 = 0;
            FSOffsetLow32 = 0;
        }
        else {
            // frequency sync
            FSOffsetHigh8 = 0xFF;
            FSOffsetLow32 = 0xFFFFFFFFL;
        }
        return new OM_G31R01.FSOffset(FSOffsetHigh8, FSOffsetLow32);
    }

    public CMDHAND.FSOffset getCMDHANDfsOffsetByActiveSyncSrc(){
        Integer FSOffsetHigh8;
        long FSOffsetLow32;
        if(momHelper.isTimeSyncActive()){
            // time sync
            FSOffsetHigh8 = 0;
            FSOffsetLow32 = 0;
        }
        else {
            // frequency sync
            FSOffsetHigh8 = 0xFF;
            FSOffsetLow32 = 0xFFFFFFFFL;
        }
        return new CMDHAND.FSOffset(FSOffsetHigh8, FSOffsetLow32);
    }
    
    /**
     * @param tg   -  Tg number
     *        trxc -  Trxc number
     * @return ChannelActAsyncHandoverBuilder needed for creating ChannelActAsyncHandover command
     * 
     */
    public static ChannelActAsyncHandoverBuilder getChannelActAsyncHandoverBuilder(int tg, int trxc) {
        return new ChannelActAsyncHandoverBuilder(tg, trxc);
    }
    
    /**
     * @param tg   -  Tg number
     *        trxc -  Trxc number
     * @return ChannelActImmediateAssignBuilder needed for creating ChannelActImmediateAssign command
     * 
     */
    public static ChannelActImmediateAssignBuilder getChannelActImmediateAssignBuilder(int tg, int trxc) {
        return new ChannelActImmediateAssignBuilder(tg, trxc);
    } 
    
    /**
     * @param tg   -  Tg number
     *        trxc -  Trxc number
     * @return ChannelActNormalAssignBuilder needed for creating ChannelActNormalAssign command
     * 
     */
    public static ChannelActNormalAssignBuilder getChannelActNormalAssignBuilder(int tg, int trxc) {
        return new ChannelActNormalAssignBuilder(tg, trxc);
    }    

}
