package com.ericsson.msran.test.grat.testhelpers.restorecommands;

import org.apache.log4j.Logger;

import com.ericsson.abisco.clientlib.AbiscoClient;
import com.ericsson.abisco.clientlib.servers.CMDHLIB;
import com.ericsson.commonlibrary.restorestack.RestoreCommand;

/**
 * Command that can be used in the restore stack to disable Abis logging
 * in Abisco.
 */
public class DisableAbisLogging implements RestoreCommand {
    private final Logger LOGGER = Logger.getLogger(DisableAbisLogging.class);
    private CMDHLIB cmdhlib;
    private int tgId;


    /**
     * @param abiscoClient AbiscoClient to use
     * @param tgId The TG.
     */
    public DisableAbisLogging(AbiscoClient abiscoClient, int tgId) {
        this.cmdhlib = abiscoClient.getCMDHLIB();
        this.tgId = tgId;
    }

    @Override
    public void restore() {
        LOGGER.info("Restorestack: DisableAbisLogging");

    	CMDHLIB.AbisLogging abisLogging = cmdhlib.createAbisLogging();

    	abisLogging.setSTATE(CMDHLIB.Enums.STATE.OFF);
    	abisLogging.setTGId(tgId);
    	try {
        	abisLogging.send();
        } catch (InterruptedException e) {
            e.printStackTrace();
    	}
    }
}
