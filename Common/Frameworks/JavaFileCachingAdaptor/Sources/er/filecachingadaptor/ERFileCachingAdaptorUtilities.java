//
//  ERXFileCachingAdaptorUtilities.java
//
//  Created by Thomas Burkholder on Thu May 10, 2005.
//
package er.filecachingadaptor;

import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import com.webobjects.eoaccess.*;
import java.util.*;
import java.math.BigDecimal;
import java.io.*;
import java.text.ParseException;
import er.extensions.*;

public class ERFileCachingAdaptorUtilities {
    public static final ERXLogger log = ERXLogger.getERXLogger(ERFileCachingAdaptorUtilities.class);
    public static final String FileCacheLocationKey = "ERFileCachingAdaptor.FileCache";
    public static final String GeneratePListKey="ERFileCachingAdaptor.generatePListFile";
    public static final String GeneratePListGZippedKey="ERFileCachingAdaptor.generatePListGZippedFile";
    public static final String ReadGZippedCacheKey="ERFileCachingAdaptor.readGZippedCache";
    public static final String ExcludeModelsKey="ERFileCachingAdaptor.ModelsToExcludeFromCache";

    private static NSDictionary _cache;
    private static NSTimestampFormatter _dateFormatter=new NSTimestampFormatter();

    public static boolean hasSharedEntities(EOModel model) {
	NSArray entities = model.entities();
	for(int i=0;i<entities.count();i++) {
	    EOEntity e = (EOEntity)entities.objectAtIndex(i);
	    NSArray fsnames = e.sharedObjectFetchSpecificationNames();
	    if (fsnames != null && fsnames.count() > 0) {
		return true;
	    }
	}
	return false;
    }

    private static void _subvert(NSArray models, NSSet exclude) {
        boolean disabled = ERXProperties.booleanForKeyWithDefault("ERFileCachingAdaptor.disabled",false);
        if (!disabled) {
            _getCache();
            if (!_cacheIsHoarked) {
                for (int i=0;i<models.count();i++) {
                    EOModel model = (EOModel)models.objectAtIndex(i);
                    if (hasSharedEntities(model) && !exclude.containsObject(model.name())) {
                        model.setAdaptorName("FileCaching");
                        if (log.isDebugEnabled()) log.debug("Subverting "+model.name()+" - it has shared entities.");
                    } else {
                        if (log.isDebugEnabled()) log.debug("Skipping "+model.name()+" - it has no shared entities.");
                    }
                }
            }
        }
    }

	   
    public static void subvertModels() {
        // This should be called as early as possible, so that this call to EOModelGroup.globalModelGroup()
        // is the first such call - otherwise, we're f*cked.
        // Suggest you put it in your WOApplication.run() implementation
        //        _subvert(EOModelGroup.globalModelGroup().models());
	NSArray a = ERXProperties.arrayForKeyWithDefault(ExcludeModelsKey,NSArray.EmptyArray);
	NSSet exclude = new NSSet(a);
        _subvert(EOModelGroup.defaultGroup().models(),exclude);
        //        _subvert(ERXModelGroup.globalModelGroup().models());
        //        _subvert(ERXModelGroup.defaultModelGroup().models());
    }

    private static Number _convertNumber(String value, EOAttribute att) {
        String str = att.valueType();
        char t;
        if ((str == null) || (str.length() != 1)) {
            t = ' ';
        } else {
            t = str.charAt(0);
        }
        try {
            switch (t) {
            case EOAttribute._VTBoolean:
                return NSPropertyListSerialization.booleanForString(value) ? ERXConstant.OneInteger : ERXConstant.ZeroInteger;
            case EOAttribute._VTShort:
                return Short.valueOf(value);
            case EOAttribute._VTInteger:
                return Integer.valueOf(value);
            case EOAttribute._VTLong:
                return Long.valueOf(value);
            case EOAttribute._VTFloat:
                return Float.valueOf(value);
            case EOAttribute._VTDouble:
                return Double.valueOf(value);
            case EOAttribute._VTByte:
                return Byte.valueOf(value);
            case EOAttribute._VTBigDecimal:
                BigDecimal bd = new BigDecimal(value);
                return bd.setScale(att.scale());
            default:
                throw new IllegalStateException("value type " + t + " of attribute " + att.name() + " of entity " + ((EOEntity) att.parent()).name() + " is invalid.");
            }
        } catch (NumberFormatException nfe) {
            throw new IllegalStateException("Got a Number format exception while attempting to convert "+value+" (which is of class "+value.getClass().getName()+") to valueType "+t+", in attribute " + att.name() + " of entity " + ((EOEntity) att.parent()).name());
        }
    }

    private static Object _getModeledValueForRow(NSDictionary row, EOAttribute att) {
        Object value = row.valueForKey(att.name());
        if (value == null) {
            return NSKeyValueCoding.NullValue;
        }
        // type conversion
        switch (att.adaptorValueType()) {
        case EOAttribute.AdaptorBytesType:
            return (NSData)value; // Should be NSData
        case EOAttribute.AdaptorCharactersType:
            return (String)value; // Should be a String
        case EOAttribute.AdaptorDateType:
            try {
                return _dateFormatter.parseObject((String)value); // Convert String to Date
            } catch (ParseException pe) {
                throw new IllegalStateException("Got a Porse format exception while attempting to convert "+value+" to an NSTimestamp, in attribute " + att.name() + " of entity " + ((EOEntity) att.parent()).name());
            }
        case EOAttribute.AdaptorNumberType:
            return _convertNumber((String)value,att);
        }
        return value;
    }

    private static NSArray _propertyListInfoToCacheDictionary(EOEntity e, NSArray rows) {
        NSArray attributes = e.attributes();
        NSMutableArray results = new NSMutableArray();
        for (int i=0;i<rows.count();i++) {
            NSDictionary row = (NSDictionary)rows.objectAtIndex(i);
            NSMutableDictionary outputRow = new NSMutableDictionary();
            for (int j=0;j<attributes.count();j++) {
                EOAttribute att = (EOAttribute)attributes.objectAtIndex(j);
                outputRow.takeValueForKey(_getModeledValueForRow(row,att),att.name());
            }
            results.addObject(outputRow);
        }
        return results;
    }

    private static NSMutableDictionary _transformPropertyListDictionaryToCacheDictionary(NSDictionary dict) {
        // This sux - foundation's support for property lists is pretty lame.  What we have to do here is,
        // re-interpret all the parts of the property list in light of the types defined by the attributes
        // of the entity.
        NSMutableDictionary result = new NSMutableDictionary();
        NSArray models = EOModelGroup.globalModelGroup().models();
        for (int h=0;h<models.count();h++) {
            EOModel model = (EOModel)models.objectAtIndex(h);
            NSDictionary dict2 = (NSDictionary)dict.valueForKey(model.name());
            if (dict2 != null) {
                NSMutableDictionary result2 = new NSMutableDictionary();
                NSArray entities = model.entities();
                for (int i=0;i<entities.count();i++) {
                    EOEntity e = (EOEntity)entities.objectAtIndex(i);
                    NSArray values = (NSArray)dict2.valueForKey(e.name());
                    if (values != null) {
                        NSArray newValues = _propertyListInfoToCacheDictionary(e, values);
                        result2.takeValueForKey(newValues,e.name());
                    }
                }
                result.takeValueForKey(result2,model.name());
            }
        }
        return result;
    }

    private static Object _convertToPListType(Object o) {
        // Conversion shouldn't actually be necessary here; the property list will do
        // everything that's necessary when converting to a string
        if (o instanceof NSKeyValueCoding.Null) {
            return null;
        } else {
            return o;
        }
    }

    private static NSDictionary _generateDictionaryForEO(EOEntity e, EOEnterpriseObject eo) {
        NSArray classProps = e.attributesToFetch();
        // all the class properties
        NSMutableDictionary dict = new NSMutableDictionary();
        for (int i=0;i<classProps.count();i++) {
            EOAttribute a = (EOAttribute)classProps.objectAtIndex(i);
            EODatabaseContext databaseContext = EOUtilities.databaseContextForModelNamed(eo.editingContext(), e.model().name());
            databaseContext.lock();
            try {
                EODatabase database = databaseContext.database();
                NSDictionary snapshot = database.snapshotForGlobalID(eo.editingContext().globalIDForObject(eo));
                dict.takeValueForKey(_convertToPListType(snapshot.valueForKey(a.name())),a.name());
            } finally {
                databaseContext.unlock();
            }
        }
        
        return dict;
    }

    private static NSArray _generateArrayForEntity(EOEntity e) {
        NSArray a = ERXEOControlUtilities.sharedObjectsForEntityNamed(e.name());
        NSMutableArray result = new NSMutableArray();
        for (int i=0;i<a.count();i++) {
            EOEnterpriseObject eo = (EOEnterpriseObject)a.objectAtIndex(i);
            result.addObject(_generateDictionaryForEO(e,eo));
        }
        return result;
    }

    private static NSDictionary _generatePropertyListDictionaryFromModelGroup(EOModelGroup modelGroup, boolean allSharedEntities) {
        NSMutableDictionary dict = new NSMutableDictionary();
        NSArray models = modelGroup.models();
        for (int i=0;i<models.count();i++) {
            EOModel m = (EOModel)models.objectAtIndex(i);
            if (allSharedEntities || "FileCaching".equals(m.adaptorName())) {
                NSDictionary d2 = _generatePropertyListDictionaryFromModel(m);
                dict.takeValueForKey(d2,m.name());
            }
        }
        return dict;
    }

    public static boolean isEntitySuitableForCaching(EOEntity e) {
        NSArray fsnames = e.sharedObjectFetchSpecificationNames();
        if (fsnames != null && fsnames.count() > 0) {
            return true;
        } else {
            return false;
        }
    }

    private static NSDictionary _generatePropertyListDictionaryFromModel(EOModel model) {
        NSMutableDictionary dict = new NSMutableDictionary();
        NSArray entities = model.entities();
        for (int i=0;i<entities.count();i++) {
            EOEntity entity = (EOEntity)entities.objectAtIndex(i);
            if (isEntitySuitableForCaching(entity)) {
                dict.takeValueForKey(_generateArrayForEntity(entity),entity.name());
            }
        }
        return dict;
    }

    public static void generateFileCacheForAllModels(boolean allSharedEntities) {
        boolean genPList = ERXProperties.booleanForKeyWithDefault(GeneratePListKey,true);
        boolean genPListZipped = ERXProperties.booleanForKeyWithDefault(GeneratePListGZippedKey,true);
        if (genPList || genPListZipped) {
            NSDictionary dict = _generatePropertyListDictionaryFromModelGroup(EOModelGroup.globalModelGroup(),allSharedEntities);
            String output = NSPropertyListSerialization.stringFromPropertyList(dict);
            String filename = ERXProperties.stringForKey(FileCacheLocationKey);
            if (filename != null) {
                if (genPList) {
                    try {
                        ERXFileUtilities.stringToFile(output,new File(filename));
                    } catch (Exception e) {
                        log.error("Could not write model cache to file "+filename);
                    }
                }
                if (genPListZipped) {
                    String fname = filename+".gz";
                    try {
                        ERXFileUtilities.stringToGZippedFile(output,new File(fname));
                    } catch (Exception e) {
                        log.error("Could not write model cache to file "+fname);
                    }
                }
            } else {
                log.error("FileCachingAdaptor.generateFileCacheForModel - file path property not set: "+FileCacheLocationKey);
            }
        } else {
            log.error("Nonsensical: both "+GeneratePListKey+" and "+GeneratePListGZippedKey+" are false.");
        }
    }

    private static boolean _cacheIsHoarked = false;

    private static void _getCache() {
        if (!_cacheIsHoarked) {
            if (_cache == null) {
                NSMutableDictionary dict = null;
                String file = ERXProperties.stringForKey(FileCacheLocationKey);
                if (file != null) {
                    NSDictionary d1 = null;
                    boolean readGZippedCache = ERXProperties.booleanForKeyWithDefault(ReadGZippedCacheKey,true);
                    if (readGZippedCache) {
                        file = file+".gz";
                    }
                    try {
                        String str;
                        if (readGZippedCache) {
                            str = ERXFileUtilities.stringFromGZippedFile(new File(file));
                        } else {
                            str = ERXFileUtilities.stringFromFile(new File(file));
                        }
                        d1 = (NSDictionary)NSPropertyListSerialization.propertyListFromString(str);
                    } catch (Exception e) {
                        log.error("Could not read, or possibly properly interpret, property list at "+file,e);
                        _cacheIsHoarked = true;
                    }
                    if (!_cacheIsHoarked) {
                        dict = _transformPropertyListDictionaryToCacheDictionary(d1);
                    }
                }
                if (dict == null) {
                    log.error("FileCachingAdaptor used, but no "+FileCacheLocationKey+" set; caching behaviour not enabled.");
                }
                _cache = dict;
            }
        }
    }

    private static NSMutableDictionary _kvCaches = new NSMutableDictionary();
    private static final NSArray _NOT_FOUND = new NSArray();

    public static NSArray cacheDataForEntityWithKeyValue(EOEntity entity, String key, Object value) {
        _getCache();
        String kvCacheKeyPrefix = entity.model().name()+"."+entity.name()+"."+key+".";
        String kvCacheKey = kvCacheKeyPrefix+value;
        NSArray result = (NSArray)_kvCaches.valueForKey(kvCacheKey);
        if (result == null) {
            NSArray rows = cacheDataForEntity(entity);
            if (rows != null) {
                if (rows.count() == 0) {
                    _kvCaches.takeValueForKey(NSArray.EmptyArray,kvCacheKey);
                } else {
                    for (int i=0;i<rows.count();i++) {
                        NSDictionary row = (NSDictionary)rows.objectAtIndex(i);
                        Object thisValue = row.valueForKey(key);
                        NSMutableArray newRowOrder = (NSMutableArray)_kvCaches.valueForKey(kvCacheKeyPrefix+thisValue);
                        if (newRowOrder == null) {
                            newRowOrder = new NSMutableArray();
                            _kvCaches.takeValueForKey(newRowOrder,kvCacheKeyPrefix+thisValue);
                        }
                        newRowOrder.addObject(row);
                    }
                }
            } else {
                _kvCaches.takeValueForKey(_NOT_FOUND,kvCacheKey);
            }
            if (log.isDebugEnabled()) {
                log.debug("Getting "+kvCacheKey+", kvCaches is "+_kvCaches);
            }
            result = (NSArray)_kvCaches.valueForKey(kvCacheKey);
        }

        if (result == _NOT_FOUND) {
            result = null;
        }

        return result;
    }

    public static NSArray cacheDataForEntity(EOEntity entity) {
        _getCache();
        NSDictionary dict = (NSDictionary)_cache.valueForKey(entity.model().name());
        if (dict != null) {
            return (NSArray)dict.valueForKey(entity.name());
        } else {
            return null;
        }
    }

}
