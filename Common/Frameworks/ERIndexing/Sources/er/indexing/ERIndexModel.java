/**
 * 
 */
package er.indexing;

import java.net.URL;
import java.util.Enumeration;

import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSBundle;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSPropertyListSerialization;
import com.webobjects.foundation._NSUtilities;

import er.extensions.ERXEC;
import er.extensions.ERXMutableDictionary;
import er.extensions.ERXPatcher;

public class ERIndexModel {
	
	NSMutableDictionary indices = (NSMutableDictionary) ERXMutableDictionary.synchronizedDictionary();
	
	private static ERIndexModel _sharedInstance;
	
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
		for (Enumeration bundles = NSBundle.frameworkBundles().objectEnumerator(); bundles.hasMoreElements();) {
			NSBundle bundle = (NSBundle) bundles.nextElement();
			URL url = bundle.pathURLForResourcePath("ERIndex.indexModel");
			if(url != null) {
				loadModel(url);
			}
		}
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
		Class c = ERXPatcher.classForName(className);
		ERIndex index = (ERIndex) _NSUtilities.instantiateObject(c, 
				new Class[]{ERIndexModel.class, NSDictionary.class}, 
				new Object[]{this, indexDef}, true, false);
		indices.setObjectForKey(index, key);
	}

	public ERIndex indexNamed(String key) {
		return (ERIndex) indices.objectForKey(key);
	}

	public void indexAllObjects(EOEntity entity) {
		ERIndex index = indexNamed(entity.name());
		if(index != null) {
			EOEditingContext ec = ERXEC.newEditingContext();
			ec.lock();
			try {
				NSArray objects = EOUtilities.objectsForEntityNamed(ec, entity.name());
				indexObjects(entity, objects);
			} finally {
				ec.unlock();
			}
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
		ERIndex index = indexNamed(entity.name());
		if(index != null) {
			NSArray added = index.addedDocumentsForObjects(objects);
			index.addJob(added, NSArray.EmptyArray);
		}
	}

}