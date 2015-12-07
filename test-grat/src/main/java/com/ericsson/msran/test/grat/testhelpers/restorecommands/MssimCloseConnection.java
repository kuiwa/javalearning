package com.ericsson.msran.test.grat.testhelpers.restorecommands;

import org.apache.log4j.Logger;

import com.ericsson.commonlibrary.restorestack.RestoreCommand;
import com.ericsson.mssim.gsmb.Gsmb;

/**
 * Command that can be used in the restore stack to close connection to the
 * Abisco.
 */
public class MssimCloseConnection implements RestoreCommand {
    private final Logger LOGGER = Logger.getLogger(MssimCloseConnection.class);
    private Gsmb gsmb;

    /**
     * @param abiscoClient AbiscoClient to use for closing
     */
    public MssimCloseConnection(Gsmb gsmb) {
        this.gsmb = gsmb;
    }

    @Override
    public void restore() {
        LOGGER.info("Restorestack: Closing MSSIM connection");
        gsmb.disconnect();
    }
}
