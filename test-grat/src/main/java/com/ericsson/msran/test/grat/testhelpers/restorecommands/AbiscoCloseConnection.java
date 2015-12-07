package com.ericsson.msran.test.grat.testhelpers.restorecommands;

import org.apache.log4j.Logger;

import com.ericsson.abisco.clientlib.AbiscoClient;
import com.ericsson.commonlibrary.restorestack.RestoreCommand;

/**
 * Command that can be used in the restore stack to close connection to the
 * Abisco.
 */
public class AbiscoCloseConnection implements RestoreCommand {
    private final Logger LOGGER = Logger.getLogger(AbiscoCloseConnection.class);
    private AbiscoClient abiscoClient;

    /**
     * @param abiscoClient AbiscoClient to use for closing
     */
    public AbiscoCloseConnection(AbiscoClient abiscoClient) {
        this.abiscoClient = abiscoClient;
    }

    @Override
    public void restore() {
        LOGGER.info("Restorestack: Closing Abisco connection");
        abiscoClient.close();
    }
}
