package er.grouping;

import java.lang.*;
import java.util.*;
import java.io.*;
import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.appserver.*;

/* DRSubMasterCriteria.h created by Administrator on Mon 02-Nov-1998 */
//#import <WebObjects/WebObjects.h>
public class DRSubMasterCriteria extends Object  {

    //
    // parameters used to extract a value from a Record
    //
    protected boolean _useMethod;

    // id _YES, will fire method for key
    // else will use valueForKey: with key
    protected String _key;

    // either a key used with valueForKeyPath: or
    // the method name to fired against the record
    protected boolean _useTimeFormat;

    // if _YES will convert dates to strings
    // and then compare
    protected String _format;

    // only used if useTimeFormat is _YES
    // matches based on string compare to dates formatted
    // into strings with 'format'
    //
    // Parameters used to decide what to compare against
    // when selecting a record group to place a record in
    //
    protected boolean _groupEdges;

    protected NSArray _rawPossibleValues;

    protected NSMutableArray _possibleValues;

    protected double _periodicDelta;

    // an array of numbers or dates. Only used if 
    // possibleValuesUseType is non-_nil
    protected String _possibleValuesUseType;

    // if 'usePredefined', uses a list of pre-existing values as
    // possible matches to tests and no other possible values
    // for key 
    // if 'useRange', possibleValues are used with two tests to assess
    // whether the record is between each pair of contiguous values 
    // in the possibleValues list 
    // if 'usePeriodic', expect exactly 2 values in possibleValues representing 
    // delta and TYPE of delta: e.g. date vs. number
    // these two values also serve to set the start point from
    // which deltas are built.
    protected boolean _isPreset;
    
    protected boolean _isPeriodic;
    protected boolean _mustSearchForLookup;
    protected NSDictionary _presetLookupDict;
    protected NSSelector _selKey;
    protected NSArray _possibleUseTypes;
    protected  boolean _nonNumberOrDate;
    protected String _label;


    static public DRSubMasterCriteria withKeyUseMethodUseTimeFormatFormatPossibleValuesUseTypeGroupEdgesPossibleValues(String akey, boolean auseMethod, boolean auseTimeFormat, String aformat, String apossibleValuesUseType, boolean agroupEdges, NSArray apossibleValues) {
        DRSubMasterCriteria aVal = new DRSubMasterCriteria();
        aVal.initWithKey(akey, auseMethod, auseTimeFormat, aformat, apossibleValuesUseType, agroupEdges, apossibleValues);
        return aVal;
    }


    public NSMutableArray possibleRangeValuesFromRawValues(NSArray rawPossVals) {
        int i;
        NSMutableArray possVals = new NSMutableArray();
        int rawCount = rawPossVals.count();
        int newCount = rawCount-1;

        if (groupEdges()) {
            Object lowVal = "L";
            Object highVal = rawPossVals.objectAtIndex(0);
            possVals.addObject(this.valDictMaxMin(highVal, lowVal));
        }

        for (i = 0; i < newCount; i++) {
            Object rawPossValLow;
            Object rawPossValHigh;
            Object newPossVal;
            rawPossValLow = rawPossVals.objectAtIndex(i);
            rawPossValHigh = rawPossVals.objectAtIndex(i+1);
            newPossVal = this.valDictMaxMin(rawPossValHigh, rawPossValLow);
            possVals.addObject(newPossVal);
        }

        if (groupEdges()) {
            Object lowVal = rawPossVals.lastObject();
            Object highVal = "H";
            possVals.addObject(this.valDictMaxMin(highVal, lowVal));
        }

        return possVals;
    }


    public DRSubMasterCriteria initWithKey(String akey, boolean auseMethod, boolean auseTimeFormat, String aformat, String apossibleValuesUseType, boolean agroupEdges, NSArray apossibleValues) {
        //OWDebug.println(1, "akey: "+akey);
        //OWDebug.println(1, "auseMethod: "+auseMethod);
        //OWDebug.println(1, "auseTimeFormat: "+auseTimeFormat);
        //OWDebug.println(1, "aformat: "+aformat);
        //OWDebug.println(1, "apossibleValuesUseType: "+apossibleValuesUseType);
        //OWDebug.println(1, "agroupEdges: "+agroupEdges);
        //OWDebug.println(1, "apossibleValues: "+apossibleValues);
        _label = null;
        _useMethod = auseMethod;
        _useTimeFormat = auseTimeFormat;
        _groupEdges = agroupEdges;

        if (akey == null) {
            _key = "";
        } else {
            _key = akey+"";
        }

        if (_useMethod) {
            _selKey = new NSSelector(_key);
        }

        if (_useTimeFormat && aformat != null) {
            _format = "";
        } else {
            _format = aformat+"";
        }

        if (apossibleValuesUseType != null && !apossibleValuesUseType.equals("usePredefined") && !apossibleValuesUseType.equals("useRange") && !apossibleValuesUseType.equals("usePeriodic")) {
            // invalid possibleValuesUseType
            _possibleValuesUseType = null;
        } else {
            _possibleValuesUseType = apossibleValuesUseType;
        }
        //OWDebug.println(1, "_possibleValuesUseType: "+_possibleValuesUseType);

        if (_possibleValuesUseType != null && apossibleValues == null) {
            _rawPossibleValues = new NSArray();
        } else {
            _rawPossibleValues = new NSArray(apossibleValues);

            if (_rawPossibleValues.count() > 0) {
                //////NSLog(@"--about to call isKindOfClass");
                //////NSLog(@"--about to call obj: %@", obj);
                Object obj = _rawPossibleValues.objectAtIndex(0);

                if (!(obj instanceof String) && !(obj instanceof Number)) {
                    _nonNumberOrDate = true;
                }

                //////NSLog(@"--called to call obj: ");
            }

        }

        _mustSearchForLookup = this.decideIfMustSearchForLookup();
        _isPreset = this.decideIsPreset();
        //OWDebug.println(1, "_isPreset: "+_isPreset);

        //if(![self mustSearchForLookup] && [self isPreset]){
        if (apossibleValuesUseType != null && apossibleValuesUseType.equals("usePeriodic")) {
            _isPeriodic = true;
            double v1 = DRCriteria.doubleForValue(_rawPossibleValues.lastObject());
            double v2 = DRCriteria.doubleForValue(_rawPossibleValues.objectAtIndex(0));
            //periodicDelta = rawPossibleValues.lastObject().doubleValue()-rawPossibleValues.objectAtIndex(0).doubleValue();
            _periodicDelta = v1 - v2;
        } else {
            _isPeriodic = false;
        }

        if (this.isPreset() && this.mustSearchForLookup()) {
            _possibleValues = this.possibleRangeValuesFromRawValues(_rawPossibleValues);
        } else {
            _possibleValues = new NSMutableArray(_rawPossibleValues);
        }
        //OWDebug.println(1, "_possibleValues: "+_possibleValues);

        if (this.isPreset()) {
            _presetLookupDict = this.buildPresetLookupDict();
        }
        //OWDebug.println(1, "_presetLookupDict: "+_presetLookupDict);

        _possibleUseTypes = new NSArray(new Object[]{"usePredefined" , "useRange" , "usePeriodic"});
        return this;
    }


    public String label() {
        if (_label == null) {
            String lbl = _key;

            if (_useTimeFormat) {
                lbl = lbl + " " + _format;
            }

            if (_possibleValuesUseType != null) {
                lbl = lbl + " [" + _possibleValuesUseType + "]";
            }

            _label = lbl;
        }

        return _label;
    }


    public NSDictionary buildPresetLookupDict() {
        NSMutableDictionary adict = new NSMutableDictionary();
        Enumeration anEnum = _possibleValues.objectEnumerator();

        while (anEnum.hasMoreElements()) {
            Object aval = anEnum.nextElement();
            // WARNING
            adict.setObjectForKey(aval, aval);
        }

        return new NSDictionary(adict);
    }


    public DRSubMasterCriteria() {
        super();
        _nonNumberOrDate = false;
    }


    public boolean nonNumberOrDate() {
        return _nonNumberOrDate;
    }


    public boolean useMethod() {
        return _useMethod;
    }
    public void setUseMethod(boolean v) {
        _useMethod = v;
    }


    public boolean useTimeFormat() {
        return _useTimeFormat;
    }
    public void setUseTimeFormat(boolean v) {
        _useTimeFormat = v;
    }


    public boolean groupEdges() {
        return _groupEdges;
    }
    public void setGroupEdges(boolean v) {
        _groupEdges = v;
    }


    public String key() {
        return _key;
    }


    public void setKey(String v) {
        if(v !=null)
            _key = v+"";
        else
            _key = null;
    }


    public String format() {
        return _format;
    }


    public void setFormat(String v) {
        if(v != null)
            _format = v+"";
        else
            _format = null;
    }


    public String possibleValuesUseType() {
        return _possibleValuesUseType;
    }
    public void setPossibleValuesUseType(String v) {
        if(v == null){
            _possibleValuesUseType = null;
        }else{
            if (!v.equals("usePredefined") && !v.equals("useRange") && !v.equals("usePeriodic")) {
                // invalid possibleValuesUseType
                _possibleValuesUseType = null;
            } else {
                _possibleValuesUseType = v+"";
            }
        }

    }


    public NSArray rawPossibleValues() {
        return _rawPossibleValues;
    }
    public void setRawPossibleValues(NSArray arr) {
        _rawPossibleValues = arr;
    }


    public NSArray possibleValues() {
        return _possibleValues;
    }
    
    public boolean isPreset() {
        return _isPreset;
    }


    public boolean isPeriodic() {
        return _isPeriodic;
    }


    public boolean decideIsPreset() {
        if (_possibleValuesUseType == null) {
            return false;
        }

        if (_possibleValuesUseType.equals("useRange") || _possibleValuesUseType.equals("usePredefined")) {
            return true;
        }

        return false;
    }


    public boolean mustSearchForLookup() {
        return _mustSearchForLookup;
    }


    public boolean decideIfMustSearchForLookup() {
        if (_possibleValuesUseType == null) {
            return false;
        }

        if (_possibleValuesUseType.equals("useRange") || _possibleValuesUseType.equals("usePeriodic")) {
            return true;
        }

        return false;
    }


    public NSDictionary valDictMaxMin(Object highVal, Object lowVal) {
        NSDictionary valDict = new NSDictionary(new Object[]{lowVal, highVal},  new Object[]{"L", "H"});
        return valDict;
    }


    public NSMutableArray possibleValuesToUse() {
        if (this.isPreset() && this.mustSearchForLookup()) {
            return new NSMutableArray(_rawPossibleValues);
        }

        return _possibleValues;
    }


    // this method will test inbetween'ness, will create new groups for periodics
    public NSDictionary valDictFromSearchForLookup(Object aval) {
        Object lowVal = null;
        Object highVal = null;
        NSMutableArray possibleValuesToUse = this.possibleValuesToUse();
        Object maxVal = possibleValuesToUse.lastObject();
        Object minVal = possibleValuesToUse.objectAtIndex(0);
        double v = DRCriteria.doubleForValue(aval);
        double maxv = DRCriteria.doubleForValue(maxVal);
        double minv = DRCriteria.doubleForValue(minVal);

        if (!this.isPeriodic()) {
            if (!this.groupEdges()) {
                if (v > maxv) {
                    return null;
                }

                if (v <= minv) {
                    return null;
                }

            } else {
                if (v > maxv) {
                    lowVal = maxVal;
                    highVal = "H";
                    return this.valDictMaxMin(highVal, lowVal);
                }

                if (v <= minv) {
                    lowVal = "L";
                    highVal = minVal;
                    return this.valDictMaxMin(highVal, lowVal);
                }

            }

        } else {
            if (v > maxv) {
                lowVal = maxVal;
                highVal = this.newWithDelta(maxVal, _periodicDelta);
                possibleValuesToUse.addObject(highVal);
                return this.valDictMaxMin(highVal, lowVal);
            }

            if (v <= minv) {
                lowVal = this.newWithDelta(minVal, -_periodicDelta);
                highVal = minVal;
                possibleValuesToUse.insertObjectAtIndex(lowVal, 0);
                return this.valDictMaxMin(highVal, lowVal);
            }

        }

        int pvcount = possibleValuesToUse.count();

        for (int i = 0; i < pvcount; i++) {
            int nextIndex = i+1;

            if (nextIndex == pvcount) {
                return null;
            }

            lowVal = possibleValuesToUse.objectAtIndex(i);
            highVal = possibleValuesToUse.objectAtIndex(nextIndex);
            minv = DRCriteria.doubleForValue(lowVal);
            maxv = DRCriteria.doubleForValue(highVal);

            if ((v <= maxv) && (v > minv)) {
                break;
            }

        }

        return this.valDictMaxMin(highVal, lowVal);
    }


    public Object newWithDelta(Object val, double delta) {
        double v;

        if (val instanceof NSTimestamp) {
            NSTimestamp vts = (NSTimestamp)val;
            NSTimestamp nvts = vts.timestampByAddingGregorianUnits(0, 0, 0, 0, 0, (int)delta);
            return nvts;
            //return vts.dateByAddingTimeInterval(delta);
        } else if (val instanceof Number) {
            v = DRCriteria.doubleForValue(val) + delta;
            return (new Double(v));
        }

        v = DRCriteria.doubleForValue(val) + delta;
        return (new Double(v)).toString();
    }


    public Object valueForRecord(DRRecord rec) {
        Object aval = null;

        if (_useMethod) {
            try{
                aval = _selKey.invoke(rec.rawRecord());
            }catch(IllegalAccessException e){
            }catch(IllegalArgumentException e){
            }catch(java.lang.reflect.InvocationTargetException e){
            }catch(NoSuchMethodException e){
            }
        } else {
            aval = rec.rawRecord().valueForKeyPath(_key);
        }

        return aval;
    }


    public Object lookUpValueForRecord(DRRecord rec) {
        Object aval = this.valueForRecord(rec);

        //OWDebug.println(1, "aval:"+aval);
        if (this.mustSearchForLookup()) {
            //OWDebug.println(1, "must search for lookup");
            aval = this.valDictFromSearchForLookup(aval);
        } else if (this.isPreset()) {
            //OWDebug.println(1, "is preset");
            //WARNING
            aval = _presetLookupDict.objectForKey(aval);
        }
        //OWDebug.println(1, "converted value: aval:"+aval);

        return aval;
    }


    public String lookUpKeyForValue(Object aVal) {
        String s;

        if (_useTimeFormat && aVal instanceof NSTimestamp) {
            NSTimestamp ts = (NSTimestamp)aVal;
            NSTimestampFormatter formatter = DRCriteria.formatterForFormat(_format);
            s = formatter.format(ts);  
            //s = aVal.descriptionWithCalendarFormat(format);
        } else {
            s = aVal.toString();
        }

        return s;
    }


    public NSArray possibleUseTypes() {
        return _possibleUseTypes;
    }

    private String _keyDesc = null;
    public String keyDesc(){
        if(_keyDesc == null){
            _keyDesc = super.toString();
        }
        return _keyDesc;
    }

}