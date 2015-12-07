package com.ericsson.msran.test.grat.testhelpers.restorecommands;

import org.apache.log4j.Logger;

import com.ericsson.commonlibrary.restorestack.RestoreCommand;
import com.ericsson.mssim.gsmb.Confirmation;
import com.ericsson.mssim.gsmb.Gsmb;
import com.ericsson.mssim.gsmb.GsmphMPH_CCCH_CLOSE_REQ;
import com.ericsson.mssim.gsmb.impl.GsmbFactory;
import com.ericsson.mssim.gsmb.packing.internal.PhErrorType;

/**
 * Restore Command that can be used in the restore stack to close
 * CCCH on an MSSIM cell
 */
public class MssimCloseCCCHCommand implements RestoreCommand {
    private final Logger LOGGER = Logger.getLogger(MssimCloseCCCHCommand.class);
    private Gsmb gsmb;
    private int mssimCell;

    /**
     * @param Gsmb gsmb instance that will send the request
     * @param mssimCell ID of the cell for which CCCH to close
     */
    public MssimCloseCCCHCommand(Gsmb gsmb, int mssimCell) {
        this.gsmb = gsmb;
        this.mssimCell = mssimCell;
    }

    @Override
    public void restore() {
        LOGGER.info("Restorestack: Close CCCH on mssim cell = " + mssimCell);
        GsmphMPH_CCCH_CLOSE_REQ close_REQ = GsmbFactory.getGsmphMPH_CCCH_CLOSE_REQBuilder(mssimCell).build();
        Confirmation mph_CCCH_CLOSE_CFM = gsmb.send(close_REQ);
        if (mph_CCCH_CLOSE_CFM.getErrorType() != PhErrorType.GSM_PH_ENOERR) {
            LOGGER.error("CCCH_CLOSE_CFM got an error: " + mph_CCCH_CLOSE_CFM.getErrorType());
        }
    }
}
