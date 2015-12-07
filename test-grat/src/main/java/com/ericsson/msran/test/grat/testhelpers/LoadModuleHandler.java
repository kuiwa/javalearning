package com.ericsson.msran.test.grat.testhelpers;

import com.ericsson.commonlibrary.remotecli.Cli;
import com.ericsson.commonlibrary.resourcemanager.g2.G2Rbs;

/**
 * @name LoadModuleHandler
 * 
 * @author Markus Hedvall (xmahedv)
 * 
 * @created 2013-11-21
 * 
 * @description This Exception is thrown if something unexpected happens during
 *              a restart of a load module.
 * 
 * @revision xmahedv 2013-11-21 First version.
 */
public class LoadModuleHandler {

    /**
     * String representation of the GRAT GSC load module.
     */
    public static final String LOAD_MODULE_GSC = "GSC";

    private PidHandler mPidHandler;

    /**
     * Constructs a new load module handler.
     */
    public LoadModuleHandler() {
        mPidHandler = new PidHandler();
    }

    /**
     * Constructs a new load module handler for the provided RBS.
     * 
     * @param rbs the RBS which load modules to handle.
     */
    public LoadModuleHandler(G2Rbs rbs) {
        mPidHandler = new PidHandler(rbs);
    }

    /**
     * Constructs a new load module handler using the provided CLI session to
     * handle the load modules.
     * 
     * @param cliSession the CLI session.
     */
    public LoadModuleHandler(Cli cliSession) {
        mPidHandler = new PidHandler(cliSession);
    }

    /**
     * Constructs a new load module handler using the provided PID handler to
     * handle the load modules.
     * 
     * @param pidHandler the PID handler to use when handling load modules.
     */
    public LoadModuleHandler(PidHandler pidHandler) {
        mPidHandler = pidHandler;
    }

    /**
     * Restarts a specific load module.
     * 
     * @param loadModule the name of the load module to restart.
     * @throws LoadModuleException if the load module isn't restarted
     *             successfully or is not found.
     */
    public void restartLoadModule(String loadModule) throws LoadModuleException {
        int pidBeforeRestart = mPidHandler.getPid(loadModule);

        if (pidBeforeRestart == 0) {
            throw new LoadModuleException("Load module " + loadModule + " not found");
        }

        mPidHandler.killPid(pidBeforeRestart);

        // Sleep for 500ms to give the RBS some time to restart the load module.
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            // Nothing to do.
        }

        int pidAfterRestart = mPidHandler.getPid(loadModule);

        if (pidBeforeRestart == pidAfterRestart || pidAfterRestart == 0) {
            throw new LoadModuleException("Failed in restarting " + loadModule);
        }
    }

}
