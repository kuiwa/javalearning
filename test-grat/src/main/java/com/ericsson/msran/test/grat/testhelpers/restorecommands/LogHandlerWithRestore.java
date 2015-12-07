package com.ericsson.msran.test.grat.testhelpers.restorecommands;

import org.apache.log4j.Logger;

import com.ericsson.commonlibrary.restorestack.RestoreCommand;
import com.ericsson.msran.test.grat.testhelpers.LogHandler;

public class LogHandlerWithRestore implements RestoreCommand {
	
    private final Logger LOGGER = Logger.getLogger(LogHandlerWithRestore.class);
	private LogHandler logHandler;
	private String processName;
	
	public LogHandlerWithRestore(LogHandler log, String process) {
		logHandler = log;
		processName = process;
        logHandler.texEnable("all", this.processName);
        logHandler.texSave(this.processName);
        logHandler.clearTexLog();
	}
	
	@Override
    public void restore() {
        LOGGER.info("Restorestack: restoring T&E log settings to default for process " + this.processName);
        logHandler.texDefault(this.processName);
        logHandler.texSave(this.processName);
    }
}
