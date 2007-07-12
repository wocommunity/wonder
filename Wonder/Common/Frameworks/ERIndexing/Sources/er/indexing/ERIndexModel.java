/**
 * 
 */
package er.indexing;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;

import org.apache.log4j.Logger;

import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSBundle;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSNotification;
import com.webobjects.foundation.NSPropertyListSerialization;
import com.webobjects.foundation.NSSelector;
import com.webobjects.foundation._NSUtilities;

import er.extensions.ERXApplication;
import er.extensions.ERXEC;
import er.extensions.ERXFetchSpecificationBatchIterator;
import er.extensions.ERXFileNotificationCenter;
import er.extensions.ERXMutableDictionary;
import er.extensions.ERXPatcher;
import er.extensions.ERXProperties;
import er.extensions.ERXSelectorUtilities;

public class ERIndexModel {
	
	private Logger log = Logger.getLogger(ERIndexModel.class);

	NSMutableDictionary indices = (NSMutableDictionary) ERXMutableDictionary.synchronizedDictionary();
	
	private static ERIndexModel _sharedInstance;
	
	private File _indexRoot;
	
	public File indexRoot() {
		if(_indexRoot == null) {
			String name = ERXProperties.stringForKeyWithDefault("er.indexing.ERIndexModel.rootDirectory", "/tmp");
			_indexRoot = new File(name);
		}
		return _indexRoot;
	}
	
	public static ERIndexModel indexModel() {
		if(_sharedInstance == null) {
			synchronized (ERIndexModel.class) {
				if(_sharedInstance == null) {
					_sharedInstance = new ERIndexModel();
					_sharedInstance.loadIndexDefinitions();
				}
			}
		}
		// _sharedInstance.loadIndexDefinitions();
		return _sharedInstance;
	}

	public void loadIndexDefinitions() {
		for (Enumeration bundles = NSBundle._allBundlesReally().objectEnumerator(); bundles.hasMoreElements();) {
			NSBundle bundle = (NSBundle) bundles.nextElement();
			URL url = bundle.pathURLForResourcePath("ERIndex.indexModel");
			if(url != null) {
				if(ERXApplication.isDevelopmentModeSafe()) {
					NSSelector selector = ERXSelectorUtilities.notificationSelector("fileDidChange");
					ERXFileNotificationCenter.defaultCenter().addObserver(this, selector, url.getFile());
				}
				loadModel(url);
			}
		}
	}

	public void fileDidChange(NSNotification n) throws MalformedURLException {
		File file = (File) n.object();
		loadModel(file.toURL());
	}

	public void loadModel(URL url) {
		NSDictionary def = (NSDictionary) NSPropertyListSerialization.propertyListWithPathURL(url);
		for (Enumeration keys = def.allKeys().objectEnumerator(); keys.hasMoreElements();) {
			String key = (String) keys.nextElement();
			NSDictionary indexDef = (NSDictionary) def.objectForKey(key);
			addIndex(key, indexDef);
		}
	}

	private void addIndex(String key, NSDictionary indexDef) {
		String className = (String) indexDef.objectForKey("index");
		if(className == null) {
			className = ERIndex.class.getName();
		}
		NSMutableDictionary dict = indexDef.mutableClone();
		dict.setObjectForKey(new File(indexRoot(), key).getAbsolutePath(), "store");
		Class c = ERXPatcher.classForName(className);
		ERIndex index = (ERIndex) _NSUtilities.instantiateObject(c, 
				new Class[] {ERIndexModel.class, NSDictionary.class}, 
				new Object[] {this, dict}, true, false);
		indices.setObjectForKey(index, key);
	}

	public ERIndex indexNamed(String key) {
		return (ERIndex) indices.objectForKey(key);
	}
	

	public void clear() {
		for(Enumeration i = indices.objectEnumerator(); i.hasMoreElements(); ) {
			ERIndex index = (ERIndex) i.nextElement();
			index.clear();
		}
	}

	public NSArray indicesForEntity(String entityName) {
		NSMutableArray result = new NSMutableArray();
		for(Enumeration i = indices.objectEnumerator(); i.hasMoreElements(); ) {
			ERIndex index = (ERIndex) i.nextElement();
			if(index.handlesEntity(entityName)) {
				result.addObject(index);
			}
		}
		return result;
	} 

	public void indexAllObjects(EOEntity entity) {
		if(indicesForEntity(entity.name()).count() > 0) {
			long start = System.currentTimeMillis();
			int treshhold = 10;
			EOEditingContext ec = ERXEC.newEditingContext();
			ec.lock();
			try {
				EOFetchSpecification fs = new EOFetchSpecification(entity.name(), null, null);
				ERXFetchSpecificationBatchIterator iterator = new ERXFetchSpecificationBatchIterator(fs);
				iterator.setEditingContext(ec);
				while(iterator.hasNextBatch()) {
					NSArray objects = iterator.nextBatch();
					if(iterator.currentBatchIndex() % treshhold == 0) {
						ec.unlock();
						// ec.dispose();
						ec = ERXEC.newEditingContext();
						ec.lock();
						iterator.setEditingContext(ec);
					}
					indexObjects(entity, objects);
				}
			} finally {
				ec.unlock();
			}
			log.info("Indexing " + entity.name() + " took: " + (System.currentTimeMillis() - start) + " ms");
		}
	}

	public void indexAllObjects(EOModel model) {
		for (Enumeration entities = model.entities().objectEnumerator(); entities.hasMoreElements();) {
			EOEntity entity = (EOEntity) entities.nextElement();
			indexAllObjects(entity);
		}
	}

	public void indexAllObjects(EOModelGroup group) {
		for (Enumeration models = group.models().objectEnumerator(); models.hasMoreElements();) {
			EOModel model = (EOModel) models.nextElement();
			indexAllObjects(model);
		}
	}

	public void indexObjects(EOEntity entity, NSArray objects) {
		for(Enumeration i = indicesForEntity(entity.name()).objectEnumerator(); i.hasMoreElements(); ) {
			ERIndex index = (ERIndex) i.nextElement();
			if(index.handlesEntity(entity.name())) {
				index.addObjectsToIndex(objects);
			}
		}
	}

}