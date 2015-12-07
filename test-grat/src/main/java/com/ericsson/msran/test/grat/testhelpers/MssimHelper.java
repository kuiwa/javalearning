package com.ericsson.msran.test.grat.testhelpers;



import org.apache.log4j.Logger;
import com.ericsson.commonlibrary.restorestack.RestoreCommandStack;
import com.ericsson.msran.helpers.Helpers;
import com.ericsson.mssim.gsmb.Gsmb;
import com.ericsson.msran.test.grat.testhelpers.restorecommands.MssimCloseConnection;

/**
 * @name MssimHelper
 * 
 * @author Nikos Karassavas (enikoka)
 * 
 * @created 2015-05-29
 * 
 * @description This class provides MSSIM-related help.
 * 
 * @revision enikoka 2015-05-29 First version.
 * 
 */

public class MssimHelper {
    private Gsmb gsmb;
    private RestoreCommandStack restoreStack;
    private MssimCloseConnection mssimCloseConCommand;
    private StpConfigHelper stpConfigHelper;
    
    public MssimHelper(Gsmb gsmb) {
    	this.gsmb = gsmb;
        restoreStack = Helpers.restore().restoreStackHelper().getRestoreStack();
        mssimCloseConCommand = new MssimCloseConnection(gsmb);
        stpConfigHelper = StpConfigHelper.getInstance();
    }
    
	public short getMssimCellToUse(){
			return stpConfigHelper.getMssimCell().shortValue();
	}
    
	public void disconnect (){
        gsmb.disconnect();               
        restoreStack.remove(mssimCloseConCommand);
	}
}
    

