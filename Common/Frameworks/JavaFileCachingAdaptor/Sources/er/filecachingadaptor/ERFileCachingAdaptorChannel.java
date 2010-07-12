//
//  ERFileCachingAdaptorChannel.java
//
//  Created by Thomas Burkholder on Thu May 10, 2005.
//
package er.filecachingadaptor;

import org.apache.log4j.Logger;

import com.webobjects.eoaccess.EOAdaptorChannel;
import com.webobjects.eoaccess.EOAdaptorContext;
import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOKeyComparisonQualifier;
import com.webobjects.eocontrol.EOKeyValueQualifier;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSPropertyListSerialization;

import er.extensions.eof.ERXForwardingAdaptorChannel;
import er.extensions.foundation.ERXProperties;

public class ERFileCachingAdaptorChannel extends ERXForwardingAdaptorChannel {
    public static final Logger log = Logger.getLogger(ERFileCachingAdaptorChannel.class);

    public ERFileCachingAdaptorChannel(EOAdaptorContext context, EOAdaptorChannel channel) {
        super(context, channel);
    }

    private NSArray _cacheDataForEntity;
    private int _rowToReturn;
    private boolean _fetchFromAdaptorInParallelAndDebug;

    private boolean _keysAreSameKey(EOKeyComparisonQualifier q) {
        // Sometimes EOF does this to fool the db into something.  Don't know, don't care.
        return q.leftKey().equals(q.rightKey());
    }

    private boolean _isNullOrUseless(EOQualifier q) {
        boolean result = (q==null || (q instanceof EOKeyComparisonQualifier && _keysAreSameKey((EOKeyComparisonQualifier)q)));
        return result;
    }
    
    private boolean _isEqualKeyValueQualifier(EOQualifier q) {
        return (q instanceof EOKeyValueQualifier && ((EOKeyValueQualifier)q).selector() == EOQualifier.QualifierOperatorEqual);
    }

    private boolean _isEntityDisabledForCaching(EOEntity entity) {
        NSDictionary dict = entity.userInfo();
        return NSPropertyListSerialization.booleanForString((String)dict.valueForKey("bloodyWellDontCacheMe")) || ERXProperties.booleanForKey("ERFileCachingAdapter.disableEntityCachingFor_"+entity.name());
    }

    public void selectAttributes(NSArray attributes, EOFetchSpecification fetchSpecification, boolean yn, EOEntity entity) {
        _fetchFromAdaptorInParallelAndDebug = ERXProperties.booleanForKeyWithDefault("ERFileCachingAdaptorChannel.doDBFetchesInParallel",false);
        boolean disabled = ERXProperties.booleanForKeyWithDefault("ERFileCachingAdaptor.disabled",false) || _isEntityDisabledForCaching(entity);
        boolean suitable = ERFileCachingAdaptorUtilities.isEntitySuitableForCaching(entity);
        if (!disabled && suitable) {
            EOQualifier q = fetchSpecification.qualifier();
            if (_isNullOrUseless(fetchSpecification.qualifier())) {
                _cacheDataForEntity = ERFileCachingAdaptorUtilities.cacheDataForEntity(entity);
            } else if (_isEqualKeyValueQualifier(q)) {
                EOKeyValueQualifier kvq = (EOKeyValueQualifier)q;
                String key = kvq.key();
                Object value = kvq.value();
                _cacheDataForEntity = ERFileCachingAdaptorUtilities.cacheDataForEntityWithKeyValue(entity,key,value);
                if (log.isDebugEnabled()) {
                    log.debug("result of key value lookup on "+entity.name()+" of ("+key+" = "+value+") = "+_cacheDataForEntity);
                }
            } else {
                log.warn("ERFileCachingAdaptor currently only supports full table fetches, or simple key value qualifiers, suitable for shared object caching, for instance.  Received qualifier "+fetchSpecification.qualifier()+" so falling back to underlying adaptor fetch.");
            }
        }
        if (_cacheDataForEntity == null) {
            super.selectAttributes(attributes,fetchSpecification,yn,entity);
        } else {
            if (_fetchFromAdaptorInParallelAndDebug) {
                super.selectAttributes(attributes,fetchSpecification,yn,entity);
            }
            if (log.isDebugEnabled()) {
                log.debug("ERFileCachingAdaptorChannel - cached call to "+this.hashCode()+".selectAttributes("+attributes+", "+fetchSpecification+", "+yn+", "+entity+")");
            }
            _rowToReturn = 0;
        }
    }

    public boolean isFetchInProgress() {
        if (_cacheDataForEntity != null) {
            return true;
        } else {
            return super.isFetchInProgress();
        }
    }

    public void cancelFetch() {
        if (_cacheDataForEntity != null) {
            if (_fetchFromAdaptorInParallelAndDebug) {
                super.cancelFetch();
            }
            _cacheDataForEntity = null;
        } else {
            super.cancelFetch();
        }
    }

    public NSMutableDictionary fetchRow() {
        NSMutableDictionary d;

        if (_cacheDataForEntity != null) {
            if (_fetchFromAdaptorInParallelAndDebug) {
                NSMutableDictionary a = super.fetchRow();
                log.debug("fetched version looks like: "+a);
            }
            if (_rowToReturn >= _cacheDataForEntity.count()) {
                d = null;
                _cacheDataForEntity = null;
            } else {
                d = (NSMutableDictionary)_cacheDataForEntity.objectAtIndex(_rowToReturn++);
            }
            if (log.isDebugEnabled()) {
                log.debug("cached version looks like: "+d);
            }
        } else {
            d = super.fetchRow();
        }
        return d;
    }

}
