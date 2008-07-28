/**
 * 
 */
package er.indexing;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;

import org.apache.log4j.Logger;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSBundle;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSNotification;
import com.webobjects.foundation.NSPropertyListSerialization;
import com.webobjects.foundation.NSSelector;
import com.webobjects.foundation._NSUtilities;

import er.extensions.ERXExtensions;
import er.extensions.ERXFrameworkPrincipal;
import er.extensions.appserver.ERXApplication;
import er.extensions.foundation.ERXFileNotificationCenter;
import er.extensions.foundation.ERXMutableDictionary;
import er.extensions.foundation.ERXPatcher;
import er.extensions.foundation.ERXProperties;
import er.extensions.foundation.ERXSelectorUtilities;
import er.extensions.foundation.ERXStringUtilities;

public class ERIndexing extends ERXFrameworkPrincipal {
	
	private Logger log = Logger.getLogger(ERIndexing.class);

	NSMutableDictionary indices = (NSMutableDictionary) ERXMutableDictionary.synchronizedDictionary();
	
    public final static Class REQUIRES[] = new Class[] {ERXExtensions.class};

    static {
        setUpFrameworkPrincipalClass (ERIndexing.class);
    }

	private File _indexRoot;
	
	private File indexRoot() {
		if(_indexRoot == null) {
			String name = ERXProperties.stringForKeyWithDefault("er.indexing.ERIndexModel.rootDirectory", "/tmp");
			_indexRoot = new File(name);
		}
		return _indexRoot;
	}

	public void loadIndexDefinitions() {
	    for (Enumeration bundles = NSBundle._allBundlesReally().objectEnumerator(); bundles.hasMoreElements();) {
	        NSBundle bundle = (NSBundle) bundles.nextElement();
	        NSArray<String> files = bundle.resourcePathsForResources("indexModel", null);
	        for (String file : files) {
	            URL url = bundle.pathURLForResourcePath(file);
	            String name = url.toString().replaceAll(".*?/(\\w+)\\.indexModel$", "$1");
	            if(url != null) {
	                if(ERXApplication.isDevelopmentModeSafe()) {
	                    NSSelector selector = ERXSelectorUtilities.notificationSelector("fileDidChange");
	                    ERXFileNotificationCenter.defaultCenter().addObserver(this, selector, url.getFile());
	                }
	                String string = ERXStringUtilities.stringFromResource(name, "indexModel", bundle);
	                NSDictionary dict = (NSDictionary)NSPropertyListSerialization.propertyListFromString(string);
	                addIndex(name, dict);
	                log.info("Added index: " + name);
	            }
			}
		}
	}

	public void fileDidChange(NSNotification n) throws MalformedURLException {
		File file = (File) n.object();
		loadModel(file.toURL());
	}

	private void loadModel(URL url) {
		NSDictionary def = (NSDictionary) NSPropertyListSerialization.propertyListWithPathURL(url);
		for (Enumeration keys = def.allKeys().objectEnumerator(); keys.hasMoreElements();) {
			String key = (String) keys.nextElement();
			NSDictionary indexDef = (NSDictionary) def.objectForKey(key);
			addIndex(key, indexDef);
		}
	}
	
    protected void addIndex(String key, ERIndex index) {
        indices.setObjectForKey(index, key);
    }
    
	private void addIndex(String key, NSDictionary indexDef) {
		String className = (String) indexDef.objectForKey("index");
		NSMutableDictionary dict = indexDef.mutableClone();
		if(!dict.containsKey("store")) {
			try {
				dict.setObjectForKey(new File(indexRoot(), key).toURL().toString(), "store");
			} catch (MalformedURLException e) {
				throw NSForwardException._runtimeExceptionForThrowable(e);
			}
		}
		ERIndex index;
		if (className != null) {
            Class c = ERXPatcher.classForName(className);
            index = (ERIndex) _NSUtilities.instantiateObject(c, new Class[] { String.class, NSDictionary.class }, new Object[] { key, dict }, true, false);
        } else {
            index = new ERAutoIndex(key, dict);
        }
		addIndex(key, index);
    }

    @Override
    public void finishInitialization() {
        loadIndexDefinitions();
    }

    public static ERIndexing indexing() {
        return sharedInstance(ERIndexing.class);
    }

    public void clear() {
        new ERIndexer(indexing().indices.allValues()).clear();
    }
}