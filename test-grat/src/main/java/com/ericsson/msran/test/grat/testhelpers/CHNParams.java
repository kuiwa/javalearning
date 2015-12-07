package com.ericsson.msran.test.grat.testhelpers;

import com.ericsson.mssim.gsmb.ChnComb;
import com.ericsson.mssim.gsmb.ChnMain;

public class CHNParams 
{
  private ChnComb chnComb;
  private ChnMain chnMain;
  private short tsc;
  private short narfcn;
  private long frArfcn; //diffent name than in perl ????? 
  private int[] arfcnl;
  private short sub;
  

  public CHNParams(ChnComb in_chnComb, ChnMain in_chnMain, short in_tsc, short in_narfcn, long in_frArfcn,
              int[] in_arfcnl, short in_sub)
  {
    this.chnComb = in_chnComb;
    this.chnMain = in_chnMain;
    this.tsc = in_tsc;
    this.narfcn = in_narfcn;
    this.frArfcn = in_frArfcn; //different name than in perl ????? 
    this.arfcnl = in_arfcnl;
    this.sub = in_sub;
  }
  
  public ChnComb getChnComb()
  {
    return chnComb;
  }

  public ChnMain getChnMain()
  {
    return chnMain;
  }

  public short getTsc()
  {
    return tsc;
  }

  public short getNarfcn()
  {
    return narfcn;
  }

  public long getFrArfcn()
  {
    return frArfcn;
  }

  public int[] getArfcnl()
  {
    return arfcnl;
  }
  
  public short getSub()
  {
    return sub;
  }
}
    

