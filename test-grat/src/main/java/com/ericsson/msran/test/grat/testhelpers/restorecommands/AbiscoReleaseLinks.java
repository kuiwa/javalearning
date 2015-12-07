package com.ericsson.msran.test.grat.testhelpers.restorecommands;

import java.util.List;

import org.apache.log4j.Logger;

import com.ericsson.abisco.clientlib.AbiscoClient;
import com.ericsson.abisco.clientlib.servers.CHEXTRAS;
import com.ericsson.abisco.clientlib.servers.CMDHLIB;
import com.ericsson.commonlibrary.restorestack.RestoreCommand;

/**
 * Command that can be used in the restore stack send RelaseLinks command to the
 * Abisco.
 */
public class AbiscoReleaseLinks implements RestoreCommand {
    private final Logger LOGGER = Logger.getLogger(AbiscoReleaseLinks.class);
    private AbiscoClient abiscoClient;
    private List<Integer> trxcList;
    private int tgId;

    /**
     * @param abiscoClient AbiscoClient to use for releasing links
     * @param trxcList List of trxc's used.
     */
    public AbiscoReleaseLinks(AbiscoClient abiscoClient, List<Integer> trxcList) {
        this.abiscoClient = abiscoClient;
        this.trxcList = trxcList;
        this.tgId = 0;
    }

    /**
     * @param abiscoClient AbiscoClient to use for releasing links
     * @param trxcList List of trxc's used.
     * @param tgId The TG.
     */
    public AbiscoReleaseLinks(AbiscoClient abiscoClient, List<Integer> trxcList, int tgId) {
        this.abiscoClient = abiscoClient;
        this.trxcList = trxcList;
        this.tgId = tgId;
    }

    @Override
    public void restore() {
        LOGGER.info("Restorestack: Abisco release links");

        try {
            CMDHLIB.ReleaseLinks releaseLinks = this.abiscoClient.getCMDHLIB().createReleaseLinks();
            releaseLinks.setTGId(tgId);
            if (trxcList != null) { // IF trxcList is empty, we aren't using RSL/OML
                releaseLinks.setTRXList(trxcList);
                releaseLinks.setRSL(CMDHLIB.Enums.RSL.YES);
                releaseLinks.setOML(CMDHLIB.Enums.OML.YES);
            }
            releaseLinks.send();
            LOGGER.info("Link released");
        } catch (CMDHLIB.ReleaseLinksRejectException e) {
            LOGGER.info("Links already released");
        } catch (CHEXTRAS.CHRejectException e) {
            LOGGER.info("Cannot release links");
        } catch (InterruptedException e) {
            LOGGER.info("Link release was interrupted");
        }
    }
}
