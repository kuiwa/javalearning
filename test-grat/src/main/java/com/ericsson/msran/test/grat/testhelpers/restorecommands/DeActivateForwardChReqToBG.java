package com.ericsson.msran.test.grat.testhelpers.restorecommands;

import org.apache.log4j.Logger;

import com.ericsson.abisco.clientlib.servers.PARAMDISP;
import com.ericsson.commonlibrary.restorestack.RestoreCommand;

/**
 * Command that can be used in the restore stack to deactivate ChannelRequest forwarding
 * to BG in Abisco.
 */
public class DeActivateForwardChReqToBG implements RestoreCommand {
    private final Logger LOGGER = Logger.getLogger(DeActivateForwardChReqToBG.class);
    private PARAMDISP paramdisp;


    /**
     * @param abiscoClient AbiscoClient to use
     */
    public DeActivateForwardChReqToBG(PARAMDISP paramdisp) {
        this.paramdisp = paramdisp;
    }

    @Override
    public void restore() {
        LOGGER.info("Restorestack: DeActivateForwardChReqToBG");

        PARAMDISP.ForwardChReqToBG forwardChReqToBG = paramdisp.createForwardChReqToBG();
        forwardChReqToBG.setSTATE(PARAMDISP.Enums.STATE.ProcessOnly);
        try {
			forwardChReqToBG.send();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    }
}
