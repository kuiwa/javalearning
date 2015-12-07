package com.ericsson.msran.test.grat.configsv;

import com.ericsson.commonlibrary.moshell.Moshell;


class MoshellScriptThread extends Thread{
    
    private final String[] commands;
    private final int timeout;
    private final Moshell moshell;
    
    public MoshellScriptThread(String[] commands, int timeout, Moshell moshell) {
        this.commands = commands;
        this.timeout = timeout;
        this.moshell = moshell;
    }
    @Override
    public void run() {
        for(int i =0; i< commands.length; i++)
        moshell.send(commands[i], timeout);
    }

}
