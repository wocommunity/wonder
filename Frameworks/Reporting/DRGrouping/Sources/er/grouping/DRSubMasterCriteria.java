package er.grouping;

import java.text.Format;
import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSSelector;
import com.webobjects.foundation.NSTimestamp;
import com.webobjects.foundation.NSTimestampFormatter;

import er.extensions.foundation.ERXValueUtilities;


/**
 * Defines the specifics of a {@link DRMasterCriteria}.
 * How to retrieve the values, how to convert them into
 * values that can be grouped and how to group them
 * into a set of ranges, if required.
 */
 
public class DRSubMasterCriteria {
    private static final Logger log = LoggerFactory.getLogger(DRSubMasterCriteria.class);

    protected boolean _useMethod;

    protected String _key;

    protected boolean _useTimeFormat;

    /**
     * Defines the array of possible groupings. 
     */
    protected final static NSArray _possibleUseTypes = new NSArray(new Object[]{"usePredefined" , "useRange" , "usePeriodic", "NONE"});

    protected String _format;

    protected boolean _groupEdges;

    
    protected NSArray _rawPossibleValues;

    /** */
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
    protected  boolean _nonNumberOrDate;
    protected String _label;

    static public DRSubMasterCriteria withDefinitionDictionaryPossibleValues(NSDictionary smcdict, NSArray apossibleValues) {
        return new DRSubMasterCriteria(smcdict, apossibleValues);
    }
    static public DRSubMasterCriteria withKeyUseMethodUseTimeFormatFormatPossibleValuesUseTypeGroupEdgesPossibleValues(String akey, boolean auseMethod, boolean auseTimeFormat, String aformat, String apossibleValuesUseType, boolean agroupEdges, NSArray apossibleValues) {
        DRSubMasterCriteria aVal = new DRSubMasterCriteria(akey, auseMethod, auseTimeFormat, aformat, apossibleValuesUseType, agroupEdges, apossibleValues);
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
            possVals.addObject(valDictMaxMin(highVal, lowVal));
        }

        for (i = 0; i < newCount; i++) {
            Object rawPossValLow;
            Object rawPossValHigh;
            Object newPossVal;
            rawPossValLow = rawPossVals.objectAtIndex(i);
            rawPossValHigh = rawPossVals.objectAtIndex(i+1);
            newPossVal = valDictMaxMin(rawPossValHigh, rawPossValLow);
            possVals.addObject(newPossVal);
        }

        if (groupEdges()) {
            Object lowVal = rawPossVals.lastObject();
            Object highVal = "H";
            possVals.addObject(valDictMaxMin(highVal, lowVal));
        }

        return possVals;
    }

    /** Contructor that uses a {@link NSDictionary} which defines the properties. */
    public DRSubMasterCriteria(NSDictionary smcdict, NSArray apossibleValues) {
        this(
             (String)smcdict.objectForKey("key"),
             ERXValueUtilities.booleanValue(smcdict.objectForKey("useMethod")),
             ERXValueUtilities.booleanValue(smcdict.objectForKey("useTimeFormat")),
             (String)smcdict.objectForKey("format"),
             (String)smcdict.objectForKey("possibleValuesUseType"),
             ERXValueUtilities.booleanValue(smcdict.objectForKey("groupEdges")),
             apossibleValues);
    }
    
    public DRSubMasterCriteria(String akey, boolean auseMethod, boolean auseTimeFormat, String aformat, String apossibleValuesUseType, boolean agroupEdges, NSArray apossibleValues) {
        
        if(log.isDebugEnabled()) {
            log.debug("akey: {}", akey);
            log.debug("auseMethod: {}", auseMethod);
            log.debug("auseTimeFormat: {}", auseTimeFormat);
            log.debug("aformat: {}", aformat);
            log.debug("apossibleValuesUseType: {}", apossibleValuesUseType);
            log.debug("agroupEdges: {}", agroupEdges);
            log.debug("apossibleValues: {}", apossibleValues);
        }
        _label = null;
        
        setUseMethod(auseMethod);
        setUseTimeFormat(auseTimeFormat);
        setGroupEdges(agroupEdges);
        setKey(akey);
        setFormat(aformat);
        setPossibleValuesUseType(apossibleValuesUseType);
        setRawPossibleValues(apossibleValues);

        if (isPreset() && mustSearchForLookup()) {
            _possibleValues = possibleRangeValuesFromRawValues(_rawPossibleValues);
        } else {
            _possibleValues = new NSMutableArray(_rawPossibleValues);
        }

        if (isPreset()) {
            _presetLookupDict = buildPresetLookupDict();
        }
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
    }

    public boolean nonNumberOrDate() {
        return _nonNumberOrDate;
    }

    /**
     * Decides if the extraction is by method or instance variable.
     * If this returns true, then only methods will be used to extract
     * values from the raw objects, not their instance variables.
     */
    public boolean useMethod() {
        return _useMethod;
    }
    public void setUseMethod(boolean v) {
        _useMethod = v;
    }

    /**
     * Decides if the {@link #format()} given is used to convert dates
     * into strings before comparison or just compare {@link NSTimestamp}.
     * If you set this, you should also set a valid {@link NSTimestampFormatter}
     * pattern in {@link #format()}.
     */
    public boolean useTimeFormat() {
        return _useTimeFormat;
    }
    public void setUseTimeFormat(boolean v) {
        _useTimeFormat = v;
    }

    /**
     * Defines if the values not falling into the {@link #possibleValues()} are also grouped.
     * If they are, then they fall into a special <b>H</b>igh and <b>L</b>ow bucket.
     */
    public boolean groupEdges() {
        return _groupEdges;
    }
    public void setGroupEdges(boolean v) {
        _groupEdges = v;
    }

    /** The key used for retrieving values from the records by. */
    public String key() {
        return _key;
    }

    public void setKey(String v) {
        if(v !=null)
            _key = v;
        else
            _key = null;
        if (_useMethod) {
            _selKey = new NSSelector(_key);
        }
    }

    /**
     * When {@link #useTimeFormat()} is set, then date values
     * will be converted to a string before a comparison by using this format.
     * The string can be any valid {@link NSTimestampFormatter} string,
     * which means that you can also use {@link java.util.DateFormatter}
     * patterns.
     */
    public String format() {
        return _format;
    }

    public void setFormat(String v) {
        if (_useTimeFormat && v == null) {
            _format = "";
            log.error("Can't have empty format when useTimeFormat=true: {}", this);
        }
        if(v != null)
            _format = v;
        else
            _format = null;
    }

    protected boolean usePeriodic() {
        return "usePeriodic".equals(_possibleValuesUseType);
    }
    protected boolean useRange() {
        return "useRange".equals(_possibleValuesUseType);
    }
    protected boolean usePredefined() {
        return "usePredefined".equals(_possibleValuesUseType);
    }
    
    public String possibleValuesUseType() {
        return _possibleValuesUseType;
    }
    public void setPossibleValuesUseType(String v) {
        _mustSearchForLookup = false;
        _isPreset = false;
        if(v == null) {
            _possibleValuesUseType = null;
        } else {
            if (!_possibleUseTypes.containsObject(v)) {
                // invalid possibleValuesUseType
                log.error("Invalid possibleValuesUseType: {}. Allowed are only: {} {}", v, _possibleUseTypes, this);
                _possibleValuesUseType = null;
            } else {
                _possibleValuesUseType = v;
                _mustSearchForLookup = useRange() || usePeriodic();
                _isPreset = useRange() || usePredefined();
                _isPeriodic = usePeriodic();
            }
        }

    }

    public NSArray rawPossibleValues() {
        return _rawPossibleValues;
    }
    public void setRawPossibleValues(NSArray arr) {
        if (_possibleValuesUseType != null && (arr == null || arr.count() == 0)) {
            log.warn("Should use possible values but got none: {}", this);
            _rawPossibleValues = NSArray.EmptyArray;
        } else {
            _rawPossibleValues = new NSArray(arr);

            Object obj = _rawPossibleValues.lastObject();

            if (!(obj instanceof String) && !(obj instanceof Number)) {
                _nonNumberOrDate = true;
            }
        }
        if(isPeriodic()) {
            double v1 = DRValueConverter.converter().doubleForValue(_rawPossibleValues.lastObject());
            double v2 = DRValueConverter.converter().doubleForValue(_rawPossibleValues.objectAtIndex(0));
            _periodicDelta = v1 - v2;
        }
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


    public boolean mustSearchForLookup() {
        return _mustSearchForLookup;
    }

    public NSDictionary valDictMaxMin(Object highVal, Object lowVal) {
        NSDictionary valDict = new NSDictionary(new Object[]{lowVal, highVal},  new Object[]{"L", "H"});
        return valDict;
    }

    public NSMutableArray possibleValuesToUse() {
        if (isPreset() && mustSearchForLookup()) {
            return new NSMutableArray(_rawPossibleValues);
        }

        return _possibleValues;
    }

    /** Will test inbetween'ness, will create new groups for periodics */
    public NSDictionary valDictFromSearchForLookup(Object aval) {
        Object lowVal = null;
        Object highVal = null;
        NSMutableArray possibleValuesToUse = possibleValuesToUse();
        Object maxVal = possibleValuesToUse.lastObject();
        Object minVal = possibleValuesToUse.objectAtIndex(0);
        double v = DRValueConverter.converter().doubleForValue(aval);
        double maxv = DRValueConverter.converter().doubleForValue(maxVal);
        double minv = DRValueConverter.converter().doubleForValue(minVal);

        if (!isPeriodic()) {
            if (!groupEdges()) {
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
                    return valDictMaxMin(highVal, lowVal);
                }

                if (v <= minv) {
                    lowVal = "L";
                    highVal = minVal;
                    return valDictMaxMin(highVal, lowVal);
                }

            }

        } else {
            if (v > maxv) {
                lowVal = maxVal;
                highVal = newWithDelta(maxVal, _periodicDelta);
                possibleValuesToUse.addObject(highVal);
                return valDictMaxMin(highVal, lowVal);
            }

            if (v <= minv) {
                lowVal = newWithDelta(minVal, -_periodicDelta);
                highVal = minVal;
                possibleValuesToUse.insertObjectAtIndex(lowVal, 0);
                return valDictMaxMin(highVal, lowVal);
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
            minv = DRValueConverter.converter().doubleForValue(lowVal);
            maxv = DRValueConverter.converter().doubleForValue(highVal);

            if ((v <= maxv) && (v > minv)) {
                break;
            }

        }

        return valDictMaxMin(highVal, lowVal);
    }

    /**
     * Returns a new value by adding a delta to it.
     * In case of a {@link NSTimestamp}, the delta will be seconds,
     * in case of a {@link java.lang.Number Number}, the delta is added as a double.
     * Otherwise, a conversion to a double is attempted and the delta is added afterwards.
     */
    protected Object newWithDelta(Object val, double delta) {
        double v;

        if (val instanceof NSTimestamp) {
            NSTimestamp vts = (NSTimestamp)val;
            NSTimestamp nvts = vts.timestampByAddingGregorianUnits(0, 0, 0, 0, 0, (int)delta);
            return nvts;
        } else if (val instanceof Number) {
            v = DRValueConverter.converter().doubleForValue(val) + delta;
            return (Double.valueOf(v));
        }

        v = DRValueConverter.converter().doubleForValue(val) + delta;
        return Double.toString(v);
    }

    /**
     * Returns the value for the given record.
     * If {@link #useMethod()} is given, the method is called and no further
     * action is taken if that fails.
     * Otherwise we use {@link NSKeyValueCoding} which also considers instance
     * variables.
     */
    public Object valueForRecord(DRRecord rec) {
        Object aval = null;

        if (_useMethod) {
            try{
                aval = _selKey.invoke(rec.rawRecord());
            } catch(IllegalAccessException e) {
            } catch(IllegalArgumentException e) {
            } catch(java.lang.reflect.InvocationTargetException e) {
            } catch(NoSuchMethodException e) {
            }
        } else {
            aval = rec.rawRecord().valueForKeyPath(_key);
        }

        return aval;
    }

    public Object lookUpValueForRecord(DRRecord rec) {
        Object aval = valueForRecord(rec);

        if (mustSearchForLookup()) {
            aval = valDictFromSearchForLookup(aval);
        } else if (isPreset()) {
            //WARNING
            aval = _presetLookupDict.objectForKey(aval);
        }

        return aval;
    }

    /**
     * Converts a given object to a grouping value.
     * If case the value if a {@link NSTimestamp},
     * {@link #useTimeFormat()} is set and {@link #format()}
     * is a valid date format, the formatted value will returned.
     */
    public String lookUpKeyForValue(Object aVal) {
        String s;

        if (_useTimeFormat) {
            NSTimestamp ts = DRValueConverter.converter().timestampForValue(aVal);
            Format formatter = DRCriteria.formatterForFormat(_format);
            try {
                s = formatter.format(ts);
            } catch(Exception ex) {
                log.warn("Error lookup {}, value={}: {}", ex, aVal, this);
                s = aVal.toString();
            }
        } else {
            s = aVal.toString();
        }

        return s;
    }

    /** Returns the array of possible use types. */
    public NSArray possibleUseTypes() {
        return _possibleUseTypes;
    }

    /** Holds the description for the {@link #key()}. */
    private String _keyDesc = null;

    /** Returns the description for the {@link #key()}. */
    public String keyDesc() {
        if(_keyDesc == null) {
            _keyDesc = super.toString();
        }
        return _keyDesc;
    }

    @Override
    public String toString() {
        return "<DRSubMasterCriteria key: \"" + key() + "\"; label: \"" + label() + "\"; >";
    }
}
