package com.ericsson.msran.test.grat.testhelpers.restorecommands;

import org.apache.log4j.Logger;
import com.ericsson.commonlibrary.restorestack.RestoreCommand;
import com.ericsson.msran.test.grat.testhelpers.CliCommands;
import com.ericsson.msran.jcat.TestBase;

/**
 * Command that can be used in the restore stack to first lock and then delete an MO.
 */
public class RebootNodeCommand implements RestoreCommand {
    private final Logger LOGGER = Logger.getLogger(RebootNodeCommand.class);
    private CliCommands clicommands;
    private TestBase testbase = new TestBase();

    /**
     * Constructor for lock and delete command.
     */
    public RebootNodeCommand() {
    	clicommands = new CliCommands();
    }

    @Override
    public void restore() {
        LOGGER.info("Restorestack: Rebooting node");
       	clicommands.rebootAndWaitForNode();
       	testbase.sleepSeconds(180);
    }
}
