package com.ericsson.msran.test.grat.testhelpers.restorecommands;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.ericsson.commonlibrary.managedobjects.ManagedObjectAttribute;
import com.ericsson.commonlibrary.managedobjects.ManagedObjectValueAttribute;
import com.ericsson.commonlibrary.managedobjects.exception.ConnectionException;
import com.ericsson.commonlibrary.managedobjects.exception.InvalidLdnException;
import com.ericsson.commonlibrary.managedobjects.exception.ManagedObjectModelLookupException;
import com.ericsson.commonlibrary.managedobjects.exception.NoSuchManagedObjectException;
import com.ericsson.commonlibrary.managedobjects.exception.OperationFailedException;
import com.ericsson.commonlibrary.restorestack.RestoreCommand;
import com.ericsson.msran.test.grat.testhelpers.MomHelper;

/**
 * Command that can be used in the restore stack to delete an MO.
 */
public class DeleteMoCommand implements RestoreCommand {
    private final Logger LOGGER = Logger.getLogger(DeleteMoCommand.class);
    private MomHelper momHelper;
    private String moLdn;

    /**
     * Constructor for delete command.
     * 
     * @param moLdn - String LDN of MO to delete
     */
    public DeleteMoCommand(String moLdn) {
        momHelper = new MomHelper();
        this.moLdn = moLdn;
    }

    @Override
    public void restore() {
    	LOGGER.info("Restorestack: Delete MO: " + this.moLdn);
    	
    	if (!momHelper.checkMoExist(this.moLdn)) {
    		LOGGER.info("Restorestack: MO " + this.moLdn + " does not exist, no need to do anything");
    		return;
    	}
    	
    	try {
    		momHelper.deleteMo(this.moLdn);
    		LOGGER.info("Restorestack: MO " + this.moLdn + " has been deleted");
    	} catch (Exception e) {
    		LOGGER.error("DeleteMoCommand::restore() caught Exception for MO: " + this.moLdn + " exception cause: " + e.getCause() + ", msg: " + e.getMessage());
    	}
    	
    	LOGGER.info("Restorestack: double check that the MO is deleted: " + this.moLdn);
    	if (momHelper.checkMoExist(this.moLdn)) {
    		LOGGER.error("Restorestack: MO " + this.moLdn + " does exist, trying to delete again.");
    		momHelper.deleteMo(this.moLdn);
    	} else {
    		LOGGER.info("Restorestack: MO has been deleted: " + this.moLdn);
    	}
    }	    
}
