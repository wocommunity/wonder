package er.grouping;

import java.lang.*;
import java.util.*;
import java.io.*;
import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.appserver.*;
import er.extensions.*;

/* DRCriteria.h created by Administrator on Sun 01-Nov-1998 */
//#import <WebObjects/WebObjects.h>
public class DRCriteria extends Object  {
    private static final ERXLogger log = ERXLogger.getLogger(DRCriteria.class,"grouping");

    protected NSDictionary _valueDict;

    // The keys in the dict are DRSubMasterCriteria in the masterCriteria
    protected DRMasterCriteria _masterCriteria;
    protected String _label;

    public static final double MAXNUMBER = 99999999999.0;
    public static final String MAXSTRING = "zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz";

    protected boolean _isTotal;
    protected boolean _isOther;
    protected Object _score;

    public boolean isTotal() {
        return _isTotal;
    }
    public void setIsTotal(boolean v) {
        _isTotal = v;
    }


    public boolean isOther() {
        return _isOther;
    }
    public void setIsOther(boolean v) {
        _isOther = v;
    }


    static public DRCriteria asOtherWithMasterCriteria(DRMasterCriteria mc) {
        NSArray subMcs;
        DRSubMasterCriteria smc;
        DRCriteria aVal = new DRCriteria();
        String lb = (String)mc.userInfo().objectForKey("OTHER_LABEL");
        aVal.setMasterCriteria(mc);
        subMcs = aVal.masterCriteria().subCriteriaList();
        smc = (DRSubMasterCriteria)subMcs.objectAtIndex(0);
        aVal.setValueDict(new NSDictionary(new Object[]{"*", "|Other|" , "OTHER"}, new Object[]{ "OTHER" , "lookupKey" , smc.keyDesc()}));
        if (lb != null) {
            aVal.setLabel(lb);
        } else {
            aVal.setLabel("Other");
        }
        aVal.setIsTotal(false);
        aVal.setIsOther(true);
        return aVal;
    }


    static public DRCriteria asTotalWithMasterCriteria(DRMasterCriteria mc) {
        NSArray subMcs;
        DRSubMasterCriteria smc;
        DRCriteria aVal = new DRCriteria();
        String lb = (String)mc.userInfo().objectForKey("TOTAL_LABEL");
        aVal.setMasterCriteria(mc);
        subMcs = aVal.masterCriteria().subCriteriaList();
        smc = (DRSubMasterCriteria)subMcs.objectAtIndex(0);
        aVal.setValueDict(new NSDictionary(
            new Object[]{"*", "|Total|" , "TOTAL"},
            new Object[]{ "TOTAL" , "lookupKey" , smc.keyDesc()})
        );
        
        if (lb != null) {
            aVal.setLabel(lb);
        } else {
            aVal.setLabel("Total");
        }

        aVal.setIsTotal(true);
        return aVal;
    }


    static public DRCriteria withMasterCriteriaValueDict(DRMasterCriteria mc, NSDictionary valD) {
        DRCriteria aVal = new DRCriteria();
        aVal.setMasterCriteria(mc);
        aVal.setValueDict(valD);
        return aVal;
    }


    public DRCriteria() {
        super();
        _label = null;
        _isTotal = false;
        _isOther = false;
        _score = null;
        return;
    }


    public void setMasterCriteria(DRMasterCriteria val) {
        _masterCriteria = val;
    }
    public DRMasterCriteria masterCriteria() {
        return _masterCriteria;
    }


    public void setValueDict(NSDictionary val) {
        _valueDict = val;
    }
    public NSDictionary valueDict() {
        return _valueDict;
    }

    public String toString() {
        return ""+(super.toString())+"-"+(_valueDict.toString());
    }
    
    private String _keyDesc = null;
    public String keyDesc(){
        if(_keyDesc == null){
            _keyDesc = super.toString();
        }
        return _keyDesc;
    }


    public String calendarFormatForDates() {
        NSDictionary info = _masterCriteria.userInfo();
        String v = (String)info.objectForKey("calendarFormat");
        if (v != null) {
            return v;
        }
        return "%m/%d/%Y";
    }

    public String rangeSeparator() {
        NSDictionary info = _masterCriteria.userInfo();
        String v = (String)info.objectForKey("rangeSeparator");
        if (v != null) {
            return v;
        }
        return " to ";
    }

    public String compoundSeparator() {
        NSDictionary info = _masterCriteria.userInfo();
        String calFrmt = (String)info.objectForKey("compoundSeparator");
        if (calFrmt != null) {
            return calFrmt;
        }
        return "|";
    }
    
    static private NSMutableDictionary _calFormatDict = new NSMutableDictionary();
    static public NSMutableDictionary calFormatDict(){
        return _calFormatDict;
    }
    static public NSTimestampFormatter formatterForFormat(String calFormat){
        if(calFormat == null) calFormat = "%m/%d/%y";
        NSTimestampFormatter v = (NSTimestampFormatter)_calFormatDict.objectForKey(calFormat);
        if(v == null){
            v = new NSTimestampFormatter(calFormat);
            calFormatDict().setObjectForKey(v, calFormat);
        }
        return v;
    }

    public String labelForDict(NSDictionary dict) {
        String highString, lowString;
        String lbl = "";
        Object high = dict.objectForKey("H");
        Object low = dict.objectForKey("L");

        String calFormat = this.calendarFormatForDates();
        NSTimestampFormatter formatter = DRCriteria.formatterForFormat(calFormat);
        if (high instanceof NSTimestamp) {
            highString = formatter.format(high);
            if (low instanceof NSTimestamp) {
                lowString = formatter.format(low);
            } else {
                lowString = low.toString();
            }

        } else {
            highString = high.toString();
            if (low instanceof NSTimestamp) {
                lowString = formatter.format(low);
            } else {
                lowString = low.toString();
            }

        }

        lbl = lbl + lowString + this.rangeSeparator() + highString;
        return lbl;
    }


    public String label() {
        if (_label == null) {
            NSArray subMcs = _masterCriteria.subCriteriaList();
            int cnt = subMcs.count();
            String lbl = "";
            int i;

            for (i = 0; i < cnt; i++) {
                DRSubMasterCriteria smc = (DRSubMasterCriteria)subMcs.objectAtIndex(i);
                Object rawVal = _valueDict.objectForKey(smc.keyDesc());

                if (rawVal instanceof NSDictionary) {
                    lbl = lbl.concat(this.labelForDict((NSDictionary)rawVal));
                } else {
                    lbl = lbl.concat(smc.lookUpKeyForValue(rawVal));
                }

                if (!(i == (cnt-1))) {
                    lbl = lbl.concat(this.compoundSeparator());
                }

            }

            _label = lbl;
        }

        return _label;
    }


    public void setLabel(String lbl) {
        _label = lbl;
    }


    public Object score() {
        if (_score == null) {
            synchronized(this){
                Object scr = null;
                NSArray subMcs = _masterCriteria.subCriteriaList();
                DRSubMasterCriteria smc = (DRSubMasterCriteria)subMcs.objectAtIndex(0);
                Object rawVal = _valueDict.objectForKey(smc.keyDesc());
    
                if (_valueDict.objectForKey("OTHER") != null) {
                    if (subMcs.count() > 1 || _masterCriteria.isString()) {
                        scr = MAXSTRING;
                    } else {
                        scr = new Double(MAXNUMBER+1);
                    }
                } else if (_valueDict.objectForKey("TOTAL") != null) {
                    if (subMcs.count() > 1 || _masterCriteria.isString()) {
                        scr = MAXSTRING.concat("z");
                    } else {
                        scr = new Double(MAXNUMBER+2);
                    }
                } else if (subMcs.count() > 1) {
                    scr = this.label();
                } else if (rawVal instanceof NSDictionary) {
                    Object v = ((NSDictionary)rawVal).objectForKey("L");
                    //OWDebug.println(1, "v:"+v);
                    try{
                        if (v instanceof String)
                            scr = new Double((String)v);
                        else
                            scr = DRCriteria.numberForValue(v);
                    } catch(NumberFormatException e) {
                        scr = new Double(-1.0*MAXNUMBER);
                    }
                } else if(rawVal instanceof String){
                    scr = rawVal;
                } else {
                    scr = DRCriteria.numberForValue(rawVal);
               }
                _score = scr;
            }
        }

        return _score;
    }

    static public double doubleForValue(Object v){
        double scr = 0.0;
        if(v == null){
            return 0.0;
        } else if (v instanceof String) {
            try{
                scr = (new Double((String)v)).doubleValue();
            }catch(NumberFormatException e){
                //OWDebug.println(1, "v:"+v);
                scr = 0.0;
            }
        } else if(v instanceof Number){
            Number vv = (Number)v;
            scr = vv.doubleValue();
        } else if (v instanceof NSTimestamp) {
            NSTimestamp vv = (NSTimestamp)v;
            scr = (double)vv.getTime() / 1000.0;
        } else if(v == NSKeyValueCoding.NullValue){
            scr = 0.0;
        } else {
            scr = (new Double(v+"")).doubleValue();
        }
        return scr;
     } 

    static public Number numberForValue(Object v){
        double vv = doubleForValue(v);
        Number scr = new Double(vv);
        return scr;
     } 
}