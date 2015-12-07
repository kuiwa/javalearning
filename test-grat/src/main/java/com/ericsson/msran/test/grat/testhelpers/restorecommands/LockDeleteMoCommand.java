package com.ericsson.msran.test.grat.testhelpers.restorecommands;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.ericsson.commonlibrary.ecimcom.exception.GracefulDisconnectFailedException;
import com.ericsson.commonlibrary.ecimcom.exception.NetconfProtocolException;
import com.ericsson.commonlibrary.managedobjects.ManagedObject;
import com.ericsson.commonlibrary.managedobjects.ManagedObjectAttribute;
import com.ericsson.commonlibrary.managedobjects.ManagedObjectValueAttribute;
import com.ericsson.commonlibrary.managedobjects.exception.ConnectionException;
import com.ericsson.commonlibrary.managedobjects.exception.InvalidLdnException;
import com.ericsson.commonlibrary.managedobjects.exception.ManagedObjectModelLookupException;
import com.ericsson.commonlibrary.managedobjects.exception.NoSuchAttributeException;
import com.ericsson.commonlibrary.managedobjects.exception.NoSuchEnumMemberException;
import com.ericsson.commonlibrary.managedobjects.exception.NoSuchManagedObjectException;
import com.ericsson.commonlibrary.managedobjects.exception.NoSuchStructMemberException;
import com.ericsson.commonlibrary.managedobjects.exception.OperationFailedException;
import com.ericsson.commonlibrary.managedobjects.exception.WrongAttributeValueException;
import com.ericsson.commonlibrary.restorestack.RestoreCommand;
import com.ericsson.msran.test.grat.testhelpers.MomHelper;

/**
 * Command that can be used in the restore stack to first lock and then delete an MO.
 */
public class LockDeleteMoCommand implements RestoreCommand {
    private final Logger LOGGER = Logger.getLogger(LockDeleteMoCommand.class);
    private MomHelper momHelper;
    private String moLdn;

    /**
     * Constructor for lock and delete command.
     * 
     * @param moLdn - String LDN of MO to lock and delete
     */
    public LockDeleteMoCommand(String moLdn) {
        momHelper = new MomHelper();
        this.moLdn = moLdn;
    }

    @Override
    public void restore() {
    	LOGGER.info("Restorestack: Lock and delete MO: " + this.moLdn);
    	
    	if (!momHelper.checkMoExist(this.moLdn)) {
    		LOGGER.info("Restorestack: MO " + this.moLdn + " does not exist, no need to do anything");
    		return;
    	}
    	
       	List<ManagedObjectAttribute> attributes = new ArrayList<ManagedObjectAttribute>();
    	attributes.add(new ManagedObjectValueAttribute(MomHelper.ADMINISTRATIVE_STATE, MomHelper.LOCKED));
    	
    	try {
    		momHelper.setAttributesAndDeleteMoInSameTx(this.moLdn, attributes);
    		LOGGER.info("Restorestack: MO " + this.moLdn + " has been locked and deleted");
    	} catch (Exception e) {
    		LOGGER.error("DeleteMoCommand::restore() caught Exception for MO: " + this.moLdn + " exception cause: " + e.getCause() + ", msg: " + e.getMessage());
    	}
    	
    	LOGGER.info("Restorestack: double check that the MO is deleted: " + this.moLdn);
    	if (momHelper.checkMoExist(this.moLdn)) {
    		LOGGER.error("Restorestack: MO " + this.moLdn + " does exist, trying to lock and delete again.");
    		momHelper.setAttributesAndDeleteMoInSameTx(this.moLdn, attributes);
    	} else {
    		LOGGER.info("Restorestack: MO has been deleted: " + this.moLdn);
    	}
    }	
}
