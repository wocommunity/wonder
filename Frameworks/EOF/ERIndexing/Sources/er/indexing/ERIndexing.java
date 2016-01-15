/**
 * 
 */
package er.indexing;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private static final Logger log = LoggerFactory.getLogger(ERIndexing.class);

	// Master dictionary of indices
	NSMutableDictionary indices = ERXMutableDictionary.synchronizedDictionary();
	
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

	/**
	 * Searches all bundles for *.indexModel resources files and dserializes the index definitions and adds
	 * the index definitions to the master dictionary of application indices
	 */
	public void loadIndexDefinitions() {
		// Check every bundle (app and frameworks)
	    for (Enumeration bundles = NSBundle._allBundlesReally().objectEnumerator(); bundles.hasMoreElements();) {
	        NSBundle bundle = (NSBundle) bundles.nextElement();
	        
	        // Get list of all files with extension indexModel
	        NSArray<String> files = bundle.resourcePathsForResources("indexModel", null);
	        for (String file : files) {
	            URL url = bundle.pathURLForResourcePath(file);
	            
	            // Get the name of the indexModel file withut the directory path and without the file extension
	            String name = url.toString().replaceAll(".*?/(\\w+)\\.indexModel$", "$1");
	            if(url != null) {
	            	
	            	// If in development mode, observe the indexModel file for changes
	            	// so that the index can be recreated
	                if(ERXApplication.isDevelopmentModeSafe()) {
	                    NSSelector selector = ERXSelectorUtilities.notificationSelector("fileDidChange");
	                    ERXFileNotificationCenter.defaultCenter().addObserver(this, selector, url.getFile());
	                }
	                
	                // Get contents of indexModel file
	                String string = ERXStringUtilities.stringFromResource(name, "indexModel", bundle);
	                
	                // Convert file contents into nested NSDictionary
	                NSDictionary dict = (NSDictionary)NSPropertyListSerialization.propertyListFromString(string);
	                
	                // Create the lucene index with name and dictionary definition
	                addIndex(name, dict);
	                log.info("Added index: {}", name);
	            }
			}
		}
	}

	public void fileDidChange(NSNotification n) throws MalformedURLException {
		File file = (File) n.object();
		loadModel(file.toURI().toURL());
	}

	private void loadModel(URL url) {
		NSDictionary def = (NSDictionary) NSPropertyListSerialization.propertyListWithPathURL(url);
		for (Enumeration keys = def.allKeys().objectEnumerator(); keys.hasMoreElements();) {
			String key = (String) keys.nextElement();
			NSDictionary indexDef = (NSDictionary) def.objectForKey(key);
			addIndex(key, indexDef);
		}
	}
	
    /**
     * @param key the name of the index
     * @param index the indexer instance having the name of the index and the index definition from the indexModel file
     */
    protected void addIndex(String key, ERIndex index) {
        indices.setObjectForKey(index, key);
    }
    
	/**
	 * @param key the name of the index
	 * @param indexDef the dictionary containing the index definition (usually deserialized from the indexModel file)
	 */
	private void addIndex(String key, NSDictionary indexDef) {
		// Classname for the class that will create the lucene index
		String className = (String) indexDef.objectForKey("index");
		NSMutableDictionary dict = indexDef.mutableClone();
		
		// If index store not defined, default to index named the dsame as the indexModel file in the indexRoot directory
		if(!dict.containsKey("store")) {
			try {
				dict.setObjectForKey(new File(indexRoot(), key).toURI().toURL().toString(), "store");
			} catch (MalformedURLException e) {
				throw NSForwardException._runtimeExceptionForThrowable(e);
			}
		}
		
		// Create the class that will create the index. Defaults to ERAutoIndex
		ERIndex index;
		if (className != null) {
            Class c = ERXPatcher.classForName(className);
            index = (ERIndex) _NSUtilities.instantiateObject(c, new Class[] { String.class, NSDictionary.class }, new Object[] { key, dict }, true, false);
        } else {
            index = new ERAutoIndex(key, dict);
        }
		
		// Add the index
		addIndex(key, index);
    }

    @Override
    public void finishInitialization() {
    	// load index definition files into indices
        loadIndexDefinitions();
    }

    public static ERIndexing indexing() {
        return sharedInstance(ERIndexing.class);
    }

    public void clear() {
        new ERIndexer(indexing().indices.allValues()).clear();
    }
}