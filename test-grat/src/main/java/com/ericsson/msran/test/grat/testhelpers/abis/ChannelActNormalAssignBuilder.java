package com.ericsson.msran.test.grat.testhelpers.abis;

import com.ericsson.abisco.clientlib.AbiscoClient;
import com.ericsson.abisco.clientlib.AbiscoServer.Routing;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.BSPowerStruct;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.ChannelActNormalAssign;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.Enums.ActivationType;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.Enums.AlgOrRate;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.Enums.BSPower;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.Enums.ChannelRate;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.Enums.TypeOfCh;
import com.ericsson.msran.test.grat.testhelpers.AbiscoVersionHelper;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.Enums.ChannelType;


public class ChannelActNormalAssignBuilder {
	private ChannelActNormalAssign command = null;
	
	public ChannelActNormalAssignBuilder(int tg, int trxc){
		AbiscoClient abiscoClient;
        //AbiscoVersionHelper abiscoVersionHelper = new AbiscoVersionHelper();
		abiscoClient = new AbiscoVersionHelper().getAbiscoClient();
		command = abiscoClient.getTRAFFRN().createChannelActNormalAssign();
		
	    Routing routing = new Routing();
	    routing.setTG(0);
	    routing.setTRXC(0);
	    command.setRouting(routing);
	}
	
    public ChannelActNormalAssignBuilder setTimeSlotNo(int timeSlotNo) {
        this.command.getChannelNoStruct().setTimeSlotNo(timeSlotNo);
        return this;
    }
    
    public ChannelActNormalAssignBuilder setChannelType(ChannelType chType) {
        this.command.getChannelNoStruct().setChannelType(chType);
        return this;
    }
    
    public ChannelActNormalAssignBuilder setActivationType(ActivationType actType) {
        this.command.getActivationTypeStruct().setActivationType(actType);
        return this;
    }

    public ChannelActNormalAssignBuilder setTypeOfCh(TypeOfCh typeOfCh) {
        this.command.getChannelModeStruct().setTypeOfCh(typeOfCh);
        return this;
    }
    
    public ChannelActNormalAssignBuilder setChannelRate(ChannelRate chRate) {
        this.command.getChannelModeStruct().setChannelRate(chRate);
        return this;
    }
    
    public ChannelActNormalAssignBuilder setAlgOrRate(AlgOrRate algOrRate) {
        this.command.getChannelModeStruct().setAlgOrRate(algOrRate);
        return this;
    }

    public ChannelActNormalAssignBuilder setTimingAdvanceValue(int ta) {
        this.command.getTimingAdvanceStruct().setTimingAdvanceValue(ta);
        return this;
    }
    
    public ChannelActNormalAssignBuilder setBSPower(BSPower bsPower) {
        if (bsPower != BSPower.OUT_OF_BOUNDS) // OUT_OF_BOUNDS means sending without bsPower
        {
          //The parameter BSPowerStruct is not mandatory therefore we need to create the object
          BSPowerStruct bspowerstruct = new BSPowerStruct();
          bspowerstruct.setBSPower(bsPower);
          bspowerstruct.setReserved(0);
          this.command.setBSPowerStruct(bspowerstruct);          
        }
        return this;
    }
    
    public ChannelActNormalAssignBuilder setMSPower(int powerLevel) {
        this.command.getMSPowerStruct().setPowerLevel(powerLevel);
        return this;
    }

    /**
     * @return ChannelActNormalAssign.
     */
    public ChannelActNormalAssign build() {
    	return command;
    }
        
}
