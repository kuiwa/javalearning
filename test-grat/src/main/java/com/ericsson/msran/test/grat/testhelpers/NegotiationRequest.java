package com.ericsson.msran.test.grat.testhelpers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

/**
* @name NegotiationRequest
*
* @author Marika Johansson (xrsmari) 
*       
* @created 2013-10-01
* 
* @description This class handles a Negotiation Request string received from Abisco    
*           
* @revision xrsmari 2013-10-01 First version.
* 
*/
public class NegotiationRequest extends AbisHelper {
	private Logger logger;
	
    ArrayList<String> omlIwdList = new ArrayList<String>();
    ArrayList<String> rslIwdList = new ArrayList<String>();
    
	final private int IWD_TYPE_OML = 0;
	final private int IWD_TYPE_RSL = 1;
	
    private class NegotiationIwdVersion {
	  int             btsPrerequisite;
	  int             currentlyRunningIwdVersion;
	  String          iwd;
    }

    private class NegotiationIwdType {
	  int                     noOfIwdVersions;
	  int                     iwdType;
	  ArrayList<NegotiationIwdVersion> iwdVersionList = new ArrayList<NegotiationIwdVersion>();
	}

    private class NegotiationRecord {
	  int                  noOfIwdTypes;
	  ArrayList<NegotiationIwdType> iwdTypeList = new ArrayList<NegotiationIwdType>();
    }

    /**
     * @name NegotiationRequest
     * 
     * @description Constructor that parses a Negotiation Request string and 
     *              stores all data in member variables.
     * @param data List<Integer> - The Negotiation Request 
     */
    public NegotiationRequest(List<Integer> data) {
    	logger = Logger.getLogger(NegotiationRequest.class);
        NegotiationRecord negRecord = new NegotiationRecord();

        Iterator<Integer> it = data.iterator();
        negRecord.noOfIwdTypes = it.next();

        for (int i = 0; i < negRecord.noOfIwdTypes; i++) {
			NegotiationIwdType negIwdType = new NegotiationIwdType();

			negIwdType.noOfIwdVersions = it.next();
			negIwdType.iwdType = it.next();
		
			// Loop through all IWD versions in current IWD type.
			for (int j = 0; j < negIwdType.noOfIwdVersions; j++) {
				NegotiationIwdVersion negIwdVersion = new NegotiationIwdVersion();
				
				int tmp = it.next();
				negIwdVersion.btsPrerequisite = tmp >> 1;
				negIwdVersion.currentlyRunningIwdVersion = tmp & 1;
				logger.debug("btsPrerequisite = " + negIwdVersion.btsPrerequisite + " and currentlyRunningIwdVersion = " + negIwdVersion.currentlyRunningIwdVersion);
				
				// Fetch IWD version from data array 
				negIwdVersion.iwd = new String();
				for (int k = 0; k < 6; k++) {
					int c = it.next();
					negIwdVersion.iwd = negIwdVersion.iwd.concat(Character.toString((char)c));
				}
			
				// Assign IWD version lists according to iwd type
			    if (negIwdType.iwdType == IWD_TYPE_OML) {
			    	omlIwdList.add(negIwdVersion.iwd);
			    }
			    else if (negIwdType.iwdType == IWD_TYPE_RSL) {
			    	rslIwdList.add(negIwdVersion.iwd);
			    }
			    negIwdType.iwdVersionList.add(negIwdVersion);
			}
			
			negRecord.iwdTypeList.add(negIwdType);
        }
    }

    /**
     * @name getOmlIwdVersions
     * 
     * @description Method that fetches the OML IWD versions
     * @return A list of OML IWD versions
     */
    public ArrayList<String> getOmlIwdVersions() {
        return omlIwdList;
    }
    
    /**
     * @name getRslIwdVersions
     * 
     * @description Method that fetches the RSL IWD versions
     * @return A list of RSL IWD versions
     */
    public ArrayList<String> getRslIwdVersions() {
        return rslIwdList;
    }
}
