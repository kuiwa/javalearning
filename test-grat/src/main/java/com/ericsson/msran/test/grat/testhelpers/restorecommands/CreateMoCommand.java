package com.ericsson.msran.test.grat.testhelpers.restorecommands;

import java.util.List;

import org.apache.log4j.Logger;

import com.ericsson.commonlibrary.ecimcom.netconf.NetconfManagedObjectHandler;
import com.ericsson.commonlibrary.managedobjects.ManagedObject;
import com.ericsson.commonlibrary.managedobjects.ManagedObjectAttribute;
import com.ericsson.commonlibrary.restorestack.RestoreCommand;
import com.ericsson.commonlibrary.resourcemanager.Rm;

public class CreateMoCommand implements RestoreCommand {
    private static final Logger logger = Logger.getLogger(CreateMoCommand.class);
    private ManagedObject mo;
    
    public CreateMoCommand(String ldn, List<ManagedObjectAttribute> attributeList)
    {
        mo = new ManagedObject(ldn);
        if (attributeList != null)
        {
            for (ManagedObjectAttribute att: attributeList)
            {
                mo.addAttribute(att);
            }
        }
    }
    
    @Override
    public void restore() {
        logger.info("RestoreStack: Create MO: " + mo.toString());
        NetconfManagedObjectHandler moHandler = Rm.getG2RbsList().get(0).getManagedObjectHandler();
        moHandler.connect();
        moHandler.createManagedObject(mo);
        moHandler.disconnect();
    }

}
