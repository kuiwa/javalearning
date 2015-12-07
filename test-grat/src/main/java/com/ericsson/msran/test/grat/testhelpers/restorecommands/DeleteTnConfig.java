package com.ericsson.msran.test.grat.testhelpers.restorecommands;

import org.apache.log4j.Logger;

import com.ericsson.msran.test.grat.testhelpers.TnConfigurator;
import com.ericsson.commonlibrary.managedobjects.exception.ConnectionException;
import com.ericsson.commonlibrary.restorestack.RestoreCommand;

/**
 * Delete the MO TN Configuration 
 */
public class DeleteTnConfig implements RestoreCommand {
    private final Logger LOGGER = Logger.getLogger(DeleteTnConfig.class);
    private TnConfigurator tnConfigurator;

    /**
     * Constructor
     */
    public DeleteTnConfig(TnConfigurator tnConfigurator) {
        this.tnConfigurator = tnConfigurator;
    }

    @Override
    public void restore() {
        LOGGER.info("Restorestack: Delete TN Configuration");
        try {
        	this.tnConfigurator.removeConfiguration();
        }
        catch (ConnectionException e) {
        	LOGGER.error("DeleteTnConfig::restore() caught ConnectionException exception cause: " + e.getCause());
        }
    }
}
