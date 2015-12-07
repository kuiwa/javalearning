package com.ericsson.msran.test.grat.testhelpers.restorecommands;

import org.apache.log4j.Logger;

import com.ericsson.commonlibrary.managedobjects.exception.ConnectionException;
import com.ericsson.commonlibrary.restorestack.RestoreCommand;
import com.ericsson.msran.test.grat.testhelpers.MomHelper;

/**
 * Command that can be used in the restore stack to delete an MO.
 */
public class ActivateLicenseCommand implements RestoreCommand {
    private final Logger LOGGER = Logger.getLogger(ActivateLicenseCommand.class);
    private MomHelper momHelper;
    private String moLdn;

    /**
     * Constructor for delete command.
     * 
     * @param moLdn - String LDN of MO to delete
     */
    public ActivateLicenseCommand(String moLdn) {
        momHelper = new MomHelper();
        this.moLdn = moLdn;
    }

    @Override
    public void restore() {
        LOGGER.info("Restorestack: Activate MO: " + this.moLdn);
        
        try {
            momHelper.setAttributeForMoAndCommit(moLdn, "featureState", "ACTIVATED");
        } catch (ConnectionException e) {
            LOGGER.error("ActivateLicenseCommand::restore() caught ConnectionException exception cause: " + e.getCause());
        }
        
        if (!momHelper.waitForMoAttributeStringValue(moLdn, "serviceState", "OPERABLE", 5)) {
            LOGGER.error("serviceState did not reach expected value = OPERABLE");
        }
    }
}
