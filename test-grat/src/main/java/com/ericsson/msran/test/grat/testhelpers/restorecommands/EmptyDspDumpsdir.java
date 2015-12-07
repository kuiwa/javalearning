package com.ericsson.msran.test.grat.testhelpers.restorecommands;

import org.apache.log4j.Logger;
import com.ericsson.commonlibrary.restorestack.RestoreCommand;
import com.ericsson.msran.test.grat.testhelpers.CliCommands;

/**
* Command that can be used in the restore stack to first lock and then delete an MO.
*/
public class EmptyDspDumpsdir implements RestoreCommand {
	private final Logger LOGGER = Logger.getLogger(EmptyDspDumpsdir.class);
	private CliCommands clicommands;
	private String dspDumpDir;

	/**
	 * Constructor for lock and delete command.
	 */
	public EmptyDspDumpsdir(String dir) {
		clicommands = new CliCommands();
		dspDumpDir = dir+"./*";
	}

	@Override
	public void restore() {
		LOGGER.info("Empty "+dspDumpDir);
		clicommands.deleteFiles(dspDumpDir);// the ./ is for not cleaning / 
	}
} 
