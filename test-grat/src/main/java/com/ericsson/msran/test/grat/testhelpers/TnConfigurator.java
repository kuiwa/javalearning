package com.ericsson.msran.test.grat.testhelpers;

import java.util.ArrayList;
import java.util.List;

import com.ericsson.commonlibrary.managedobjects.ManagedObject;
import com.ericsson.commonlibrary.managedobjects.ManagedObjectValueAttribute;
import com.ericsson.msran.jcat.TestBase;

import org.apache.log4j.Logger;

/**
 * Helper class for configuring TN on the node.
 */
public class TnConfigurator {
    private MomHelper momHelper;

    private static final String MO_ATTR_USER_LABEL = "userLabel";
    private static final String TNA_PORT_LDN = "ManagedElement=1,Equipment=1,FieldReplaceableUnit=1,TnPort=TN_A";
    private static final String TRANSPORT_LDN = "ManagedElement=1,Transport=1";
    private static final String VLAN_LDN = TRANSPORT_LDN + ",VlanPort=1";
    private static final String ROUTER_LDN = TRANSPORT_LDN + ",Router=1";
    private static final String IFACE_LDN = ROUTER_LDN + ",InterfaceIPv4=GSM";
    private static final String ADDRESS_LDN = IFACE_LDN + ",AddressIPv4=GSM";
    private static final String ETHERNET_PORT_LDN = TRANSPORT_LDN + ",EthernetPort=eth10";
    private static final String ROUTE_TABLE_LDN = ROUTER_LDN + ",RouteTableIPv4Static=1";
    
    private Logger logger = Logger.getLogger(TnConfigurator.class);

    String tnaIpAddress = null;
   
    private String getVlanId() {
    	if (tnaIpAddress.contains("10.209.1")) // Lab in H72
    		return "1701";
    	if (tnaIpAddress.contains("10.198.8")) // GIC
    		return "1001";
    	if (tnaIpAddress.contains("10.86.112"))
    		return "1345";
    	else
    		return "0";
    }
    
    /**
     * Create a new TnConfigurator.
     * 
     * @param momHelper Help functions for MO management
     * 
     */
    public TnConfigurator(MomHelper momHelper) {
        this.momHelper = momHelper;
    }

    /**
     * Method for removing the IP address configuration for TN.
     */
    public void removeConfiguration() {
        logger.info("Start of removeConfiguration");

    	// specify which MOs that shall be deleted together
    	List<ManagedObject> deleteMos = new ArrayList<ManagedObject>();
				
    	// Check if exist, and if so remove all MO levels under, and including, the Router MO
    	// "ManagedElement=1,Transport=1,Router=1,InterfaceIPv4=GSM,AddressIPv4=GSM"
    	// every MO under the Router MO will be removed when the Router MO is deleted
        if (momHelper.checkMoExist(ROUTER_LDN))
        {
        	deleteMos.add(new ManagedObject(ROUTER_LDN));
        }    	

        // Check if existing, and if so remove the "ManagedElement=1,Transport=1,VlanPort=1" MO
        if (momHelper.checkMoExist(VLAN_LDN))
        {
        	deleteMos.add(new ManagedObject(VLAN_LDN));
        }

        // Check if existing, and if so remove the "ManagedElement=1,Transport=1,EthernetPort=eth10" MO
        if (momHelper.checkMoExist(ETHERNET_PORT_LDN))
        {
        	deleteMos.add(new ManagedObject(ETHERNET_PORT_LDN));
        }
        
       // Currently a bug in TN can cause a crash when this MO is deleted.
       // Since this MO will be included in basic config (allways present),
       // we will soon be able to remove these parts from here. Until then
       // we create it and forget about it.
       //deleteMo(TNA_PORT_LDN);
        
        if (!deleteMos.isEmpty()) {
        	momHelper.deleteSeveralMoInOneTx(deleteMos);
        }
        
        logger.info("End of removeConfiguration");  
    }
    

    /**
     * Method for configuring the TNA port on the node and setting up and
     * optional static route for the traffic towards f.ex. bscsim.
     * 
     * @param tnaIpAddress
     *            IP Address for TNA port
     * @param createMos Add all MOs that shall be created to this list, they will be committed in one tx           
     */   
    public void configureTnaIpAddress(final String tnaIpAddress, List<ManagedObject> createMos) {
        if (!momHelper.checkMoExist(TNA_PORT_LDN)){
        	ManagedObject moTnaPort = new ManagedObject(TNA_PORT_LDN);
        	createMos.add(moTnaPort);
        }
    	
        ManagedObject ethernetPort = new ManagedObject(ETHERNET_PORT_LDN);
        ethernetPort.addAttribute(new ManagedObjectValueAttribute(
                "autoNegEnable", "true"));
        ethernetPort.addAttribute(new ManagedObjectValueAttribute(
                MO_ATTR_USER_LABEL, "eth10"));
        ethernetPort.addAttribute(new ManagedObjectValueAttribute(
                "encapsulation", TNA_PORT_LDN));
        ethernetPort.addAttribute(new ManagedObjectValueAttribute(
                "administrativeState", "UNLOCKED"));
        createMos.add(ethernetPort);
        
        this.tnaIpAddress = tnaIpAddress;
        
      	final String VID = getVlanId();
       	ManagedObject vlanport = new ManagedObject(VLAN_LDN);
       	vlanport.addAttribute(new ManagedObjectValueAttribute("encapsulation", ETHERNET_PORT_LDN));
       	vlanport.addAttribute(new ManagedObjectValueAttribute("vlanId", VID));
       	vlanport.addAttribute(new ManagedObjectValueAttribute(MO_ATTR_USER_LABEL, "vid " + VID));
        
       	if (VID.equals("1345") || VID.equals("0")){
       		vlanport.addAttribute(new ManagedObjectValueAttribute("isTagged", "false"));
       	}
        createMos.add(vlanport);
    }

    /**
     * Method to create a static route entry.
     * 
     * @param index Unique index for route
     * @param destinationIpAddress Destination address
     * @param nextHopIpAddress Next Hop for reaching destination address
     * @param createMos Add all MOs that shall be created to this list, they will be committed in one tx
     */
    public void configureStaticRoute(final int index,
                                     final String destinationIpAddress, 
                                     final String nextHopIpAddress,
                                     List<ManagedObject> createMos) {

        if ((destinationIpAddress == null) || (nextHopIpAddress == null)) {
            // No configuration needed
            return;
        }
    	  	
      	ManagedObject router = new ManagedObject(ROUTER_LDN);
       	router.addAttribute(new ManagedObjectValueAttribute(MO_ATTR_USER_LABEL, "MO created by TC " + TestBase.getCurrentTestCaseName()));
       	createMos.add(router);
        	
       	ManagedObject routerTable = new ManagedObject(ROUTE_TABLE_LDN);
       	createMos.add(routerTable);
    	
        String dstLdn = String.format("%s,Dst=%d", ROUTE_TABLE_LDN, index);
        ManagedObject dst = new ManagedObject(dstLdn);
        dst.addAttribute(new ManagedObjectValueAttribute("dst",
                "0.0.0.0/0"));
        createMos.add(dst);
        
        ManagedObject iface = new ManagedObject(IFACE_LDN);
        iface.addAttribute(new ManagedObjectValueAttribute("encapsulation", VLAN_LDN));
        iface.addAttribute(new ManagedObjectValueAttribute(MO_ATTR_USER_LABEL, "MO created by TC " + TestBase.getCurrentTestCaseName()));
        createMos.add(iface);
        
        ManagedObject address = new ManagedObject(ADDRESS_LDN);
        address.addAttribute(new ManagedObjectValueAttribute("address", tnaIpAddress + "/24"));
        address.addAttribute(new ManagedObjectValueAttribute(MO_ATTR_USER_LABEL, "MO created by TC " + TestBase.getCurrentTestCaseName()));
        createMos.add(address);
        
        String nextHopLdn = String.format("%s,NextHop=1", dstLdn);
        ManagedObject nextHop = new ManagedObject(nextHopLdn);
        nextHop.addAttribute(new ManagedObjectValueAttribute("address", nextHopIpAddress));
        createMos.add(nextHop);
    }
}
