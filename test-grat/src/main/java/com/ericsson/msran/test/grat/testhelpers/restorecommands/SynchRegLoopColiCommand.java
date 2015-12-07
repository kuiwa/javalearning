package com.ericsson.msran.test.grat.testhelpers.restorecommands;

import com.ericsson.commonlibrary.restorestack.RestoreCommand;
import com.ericsson.commonlibrary.remotecli.Cli;

/**
 * 
 */
public class SynchRegLoopColiCommand implements RestoreCommand {
    
    private Cli cli;
    private String sectorIndex;
    
    /**
     * @param cli
     * @param index
     */
    public SynchRegLoopColiCommand(Cli cli, String index) {
        this.cli = cli;
        this.sectorIndex = index;
        cli.send("regloop -t gsc1 on " + sectorIndex);
    }

    @Override
    public void restore() {
        cli.send("regloop -t gsc1 off " + sectorIndex);
    }
}
