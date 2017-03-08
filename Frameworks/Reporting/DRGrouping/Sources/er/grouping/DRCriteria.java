package er.grouping;

import java.text.Format;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSTimestamp;
import com.webobjects.foundation.NSTimestampFormatter;

/**
 * The "value" for the {@link DRMasterCriteria}. For example, given
 * a <code>category</code> key, it would contain "Drama", "Sci-Fi" and "Comedy".
 * It also assigns a {@link #score()} to objects to support header sorting.
 * Some DRCriteria have very high scores and so always end up on the bottom;
 * for example: "OTHER" and "TOTAL".
 * <p>
 * For numeric DRCriteria, the value is a really huge number and
 * 1 + a really huge number ({@link #MAXNUMBER}), respectively.
 * For alpha DRCriteria. the value is a long word filled with z's ({@link #MAXSTRING})
 * and the same with one z concatenated, respectively.
 */
public class DRCriteria {
    protected NSDictionary _valueDict;
    
    // The keys in the dict are DRSubMasterCriteria in the masterCriteria
    protected DRMasterCriteria _masterCriteria;
    protected String _label;

    public static final double MAXNUMBER = 99999999999.0;
    public static final String MAXSTRING = "zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz";

    public static String _defaultCalendarFormatString = "%m/%d/%Y";
    
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
        aVal.setValueDict(new NSDictionary(
                                            new Object[]{"*", "|Other|" , "OTHER"},
                                            new Object[]{ "OTHER" , "lookupKey" , smc.keyDesc()})
                           );
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

    @Override
    public String toString() {
        return  "<DRCriteria valueDict: " + _valueDict + "; >";
    }

    private String _keyDesc = null;
    public String keyDesc() {
        if(_keyDesc == null) {
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
        return _defaultCalendarFormatString;
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

    static public Format formatterForFormat(String calFormat) {
        synchronized(_calFormatDict) {
            if(calFormat == null) calFormat = _defaultCalendarFormatString;
            NSTimestampFormatter v = (NSTimestampFormatter)_calFormatDict.objectForKey(calFormat);
            if(v == null) {
                v = new NSTimestampFormatter(calFormat);
                setFormatterForFormat(v, calFormat);
            }
            return v;
        }
    }
    static public void setFormatterForFormat(Format formatter, String calFormat) {
        synchronized(_calFormatDict) {
            _calFormatDict.setObjectForKey(formatter, calFormat);
        }
    }
    
    public String labelForDict(NSDictionary dict) {
        String highString, lowString;
        String lbl = "";
        Object high = dict.objectForKey("H");
        Object low = dict.objectForKey("L");

        String calFormat = calendarFormatForDates();
        Format formatter = DRCriteria.formatterForFormat(calFormat);

        if (high instanceof NSTimestamp) {
            highString = formatter.format(high);
        } else {
            highString = high.toString();
        }

        if (low instanceof NSTimestamp) {
            lowString = formatter.format(low);
        } else {
            lowString = low.toString();
        }
        
        lbl = lbl + lowString + rangeSeparator() + highString;
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
                    lbl = lbl.concat(labelForDict((NSDictionary)rawVal));
                } else {
                    lbl = lbl.concat(smc.lookUpKeyForValue(rawVal));
                }

                if (!(i == (cnt-1))) {
                    lbl = lbl.concat(compoundSeparator());
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
                        scr = Double.valueOf(MAXNUMBER+1);
                    }
                } else if (_valueDict.objectForKey("TOTAL") != null) {
                    if (subMcs.count() > 1 || _masterCriteria.isString()) {
                        scr = MAXSTRING.concat("z");
                    } else {
                        scr = Double.valueOf(MAXNUMBER+2);
                    }
                } else if (subMcs.count() > 1) {
                    scr = label().toLowerCase();
                } else if (rawVal instanceof NSDictionary) {
                    Object v = ((NSDictionary)rawVal).objectForKey("L");
                    //OWDebug.println(1, "v:"+v);
                    try{
                        if (v instanceof String)
                            scr = Double.valueOf((String)v);
                        else
                            scr = DRValueConverter.converter().numberForValue(v);
                    } catch(NumberFormatException e) {
                        scr = Double.valueOf(-1.0*MAXNUMBER);
                    }
                } else if(rawVal instanceof String){
                    scr = ((String)rawVal).toLowerCase();
                } else {
                    scr = DRValueConverter.converter().numberForValue(rawVal);
                }
                _score = scr;
            }
        }
        return _score;
    }
}
