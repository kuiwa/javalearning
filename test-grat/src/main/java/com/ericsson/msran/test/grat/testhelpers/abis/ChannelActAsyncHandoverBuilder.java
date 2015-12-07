package com.ericsson.msran.test.grat.testhelpers.abis;

import com.ericsson.abisco.clientlib.AbiscoClient;
import com.ericsson.abisco.clientlib.AbiscoServer.Routing;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.BSPowerStruct;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.ChannelActAsyncHandover;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.Enums.ActivationType;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.Enums.AlgOrRate;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.Enums.ChannelRate;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.Enums.TypeOfCh;
import com.ericsson.msran.test.grat.testhelpers.AbiscoVersionHelper;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.Enums.ChannelType;
import com.ericsson.abisco.clientlib.servers.TRAFFRN.Enums.BSPower;

public class ChannelActAsyncHandoverBuilder {
	private ChannelActAsyncHandover command = null;
	
	public ChannelActAsyncHandoverBuilder(int tg, int trxc){
		AbiscoClient abiscoClient;
        //AbiscoVersionHelper abiscoVersionHelper = new AbiscoVersionHelper();
		abiscoClient = new AbiscoVersionHelper().getAbiscoClient();
		command = abiscoClient.getTRAFFRN().createChannelActAsyncHandover();
		
	    Routing routing = new Routing();
	    routing.setTG(0);
	    routing.setTRXC(0);
	    command.setRouting(routing);
	}
	
    public ChannelActAsyncHandoverBuilder setTimeSlotNo(int timeSlotNo) {
        this.command.getChannelNoStruct().setTimeSlotNo(timeSlotNo);
        return this;
    }
    
    public ChannelActAsyncHandoverBuilder setChannelType(ChannelType chType) {
        this.command.getChannelNoStruct().setChannelType(chType);
        return this;
    }
    
    public ChannelActAsyncHandoverBuilder setActivationType(ActivationType actType) {
        this.command.getActivationTypeStruct().setActivationType(actType);
        return this;
    }

    public ChannelActAsyncHandoverBuilder setTypeOfCh(TypeOfCh typeOfCh) {
        this.command.getChannelModeStruct().setTypeOfCh(typeOfCh);
        return this;
    }
    
    public ChannelActAsyncHandoverBuilder setChannelRate(ChannelRate chRate) {
        this.command.getChannelModeStruct().setChannelRate(chRate);
        return this;
    }
    
    public ChannelActAsyncHandoverBuilder setAlgOrRate(AlgOrRate algOrRate) {
        this.command.getChannelModeStruct().setAlgOrRate(algOrRate);
        return this;
    }

    public ChannelActAsyncHandoverBuilder setBSPower(BSPower bsPower) {
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
    
    public ChannelActAsyncHandoverBuilder setMSPower(int powerLevel) {
        this.command.getMSPowerStruct().setPowerLevel(powerLevel);
        return this;
    }
    
    /**
     * @return ChannelActAsyncHandover.
     */
    public ChannelActAsyncHandover build() {
    	return command;
    }
 
}
