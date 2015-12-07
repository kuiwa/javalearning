package com.ericsson.msran.test.grat.testhelpers.restorecommands;

import org.apache.log4j.Logger;

import com.ericsson.abisco.clientlib.servers.PARAMDISP;
import com.ericsson.commonlibrary.restorestack.RestoreCommand;

/**
 * Command that can be used in the restore stack to close connection to the
 * Abisco.
 */
public class ReleaseAllCallsCommand implements RestoreCommand {

    private final Logger LOGGER = Logger.getLogger(ReleaseAllCallsCommand.class);
    private PARAMDISP paramdisp;

    /**
     * @param abiscoClient AbiscoClient to use for closing
     */
    public ReleaseAllCallsCommand(PARAMDISP paramdisp) {
        this.paramdisp = paramdisp;
    }

    @Override
    public void restore() {
        LOGGER.info("Restorestack: Closing all calls.");
        try {
            paramdisp.createReleaseAllCalls().send();
        } catch (InterruptedException e) {
            LOGGER.info("Restorestack: Closing all calls FAILED");
            e.printStackTrace();
        }
    }
}
