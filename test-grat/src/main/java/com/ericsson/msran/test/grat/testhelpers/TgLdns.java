package com.ericsson.msran.test.grat.testhelpers;

import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates the LDNs of a GRAT TG (GsmSector)
 */
public class TgLdns
{
    public String sectorLdn, abisIpLdn;
    public List<String> trxLdnList;
    
    public TgLdns() {
        sectorLdn = abisIpLdn = "";
        trxLdnList = new ArrayList<>();
    }
    
    public TgLdns(String sectorLdn, String abisIpLdn) {
        this.sectorLdn = sectorLdn;
        this.abisIpLdn = abisIpLdn;
        this.trxLdnList = new ArrayList<>();
    }
    
    public TgLdns(String sectorLdn, String abisIpLdn, String trxLdn) {
        this.sectorLdn = sectorLdn;
        this.abisIpLdn = abisIpLdn;
        this.trxLdnList = new ArrayList<>();
        this.trxLdnList.add(trxLdn);
    }
}
