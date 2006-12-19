package er.extensions;

import java.lang.reflect.Field;

import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOEntityClassDescription;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.eocontrol.EOCustomObject;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOGlobalID;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.ERXStringUtilities;

/**
 * Put POJOs into EOF (sort of). This class is mainly usefull when used with
 * D2W, when you don't want to create components for non-persistent objects.
 * Should be regarded as experimental :) Here's a usage example, showing how to
 * call up an edit page for a single "object" and a list page for an array. Note 
 * that the list-inspect-edit workflow and sorting, batching etc work out of the box.
 * 
 * <pre><code>
 * public class Main extends WOComponent {
 * 
 * 	public static class Test {
 * 		
 * 		public String string;
 * 		public Number number;
 * 		public Boolean flag;
 * 		
 * 		public Test(String string, Number number, Boolean flag) {
 * 			this.string = string;
 * 			this.number = number;
 * 			this.flag = flag;
 * 		}
 * 	}
 * 	
 * 	public Test object;
 * 	public NSArray list;
 * 
 * 	public Main(WOContext context) {
 * 		super(context);
 * 		NSMutableArray l = new NSMutableArray();
 * 		for (int i = 0; i &gt; 5; i++) {
 * 			Test o = new Test("Foo "+ i, new Integer(i^i % (i+1)), i % 2 == 0? Boolean.TRUE : Boolean.FALSE);
 * 			l.addObject(o);
 * 		}
 * 		object = (Test) l.lastObject();
 * 		list = l.immutableClone();
 * 	}
 * 
 * 	public WOComponent edit() {
 * 		EOEnterpriseObject eo = new ERXDummyRecord(object);
 * 		WOComponent result = D2W.factory().pageForTaskAndEntityNamed("edit", eo.entityName(), session());
 * 		result.takeValueForKey(eo, "object");
 * 		result.takeValueForKey(context().page(), "nextPage");
 * 		return result;
 * 	}
 * 
 * 	public WOComponent showList() {
 * 		EOEditingContext ec = null;
 * 		EOClassDescription cd = null;
 * 		NSMutableArray objects = new NSMutableArray();
 * 		for (Enumeration iter = list.objectEnumerator(); iter.hasMoreElements();) {
 * 			Test o = (Test) iter.nextElement();
 * 
 * 			EOEnterpriseObject eo = new ERXDummyRecord(o, ec);
 * 			ec = eo.editingContext();
 * 			cd = eo.classDescription();
 * 			objects.addObject(eo);
 * 		}
 * 
 * 		WOComponent result = D2W.factory().pageForTaskAndEntityNamed("list", cd.entityName(), session());
 * 		EOArrayDataSource ds = new EOArrayDataSource(cd, ec);
 * 		ds.setArray(objects);
 * 		result.takeValueForKey(ds, "dataSource");
 * 		result.takeValueForKey(context().page(), "nextPage");
 * 		return result;
 * 	}
 * }</code></pre>
 * @author ak
 */

public class ERXDummyRecord extends EOCustomObject {

	public static class GlobalID extends EOGlobalID {

		private Object object;

		public GlobalID(Object o) {
			object = o;
		}

		public boolean equals(Object obj) {
			return ((GlobalID) obj).object == object;
		}

		public int hashCode() {
			return System.identityHashCode(object);
		}

		public boolean isTemporary() {
			return true;
		}
	}

	public static class EditingContext extends EOEditingContext {
		private NSMutableDictionary objects = new NSMutableDictionary();

		private NSMutableDictionary gids = new NSMutableDictionary();

		public void recordObject(EOEnterpriseObject eo, EOGlobalID gid) {
			objects.setObjectForKey(eo, gid);
			gids.setObjectForKey(gid, eo);
			super.recordObject(eo, gid);
		}

		public EOGlobalID globalIDForObject(EOEnterpriseObject eo) {
			return (EOGlobalID) gids.objectForKey(eo);
		}
	}

	private static EOModel pojoModel;

	private Object object;

	private EOClassDescription classDescription;

	private EOEditingContext editingContext;

	public ERXDummyRecord(Object o, EOEditingContext ec) {
		editingContext = ec == null ? new EditingContext() : ec;
		object = o;
		String entityName = "Pojo" + ERXStringUtilities.lastPropertyKeyInKeyPath(object.getClass().getName().replaceAll("\\$", ""));
		if (pojoModel == null) {
			pojoModel = new EOModel();
			pojoModel.setName("PojoModel");
			EOModelGroup.defaultGroup().addModel(pojoModel);
		}
		EOEntity entity = EOModelGroup.defaultGroup().entityNamed(entityName);
		if (entity != null) {
			pojoModel.removeEntity(entity);
			entity = null;
		}
		if (entity == null) {
			entity = new EOEntity();
			entity.setName(entityName);
			NSMutableArray fields = new NSMutableArray();
			Field f[] = object.getClass().getDeclaredFields();
			for (int i = 0; i < f.length; i++) {
				Field field = f[i];
				fields.addObject(field.getName());
				EOAttribute attribute = new EOAttribute();
				attribute.setName(field.getName());
				attribute.setClassName(field.getType().getName());
				entity.addAttribute(attribute);
			}
			classDescription = new EOEntityClassDescription(entity);
			NSKeyValueCoding.Utility.takeValueForKey(entity, classDescription, "classDescription");
			pojoModel.addEntity(entity);
			EOClassDescription.registerClassDescription(classDescription, object.getClass());
		} else {
			classDescription = entity.classDescriptionForInstances();
		}
		EOGlobalID gid = new GlobalID(o);
		editingContext.recordObject(this, gid);
	}

	public ERXDummyRecord(Object o) {
		this(o, new EditingContext());
	}

	public EOClassDescription classDescription() {
		return classDescription;
	}

	public EOEditingContext editingContext() {
		return editingContext;
	}

	public Object handleQueryWithUnboundKey(String key) {
		return NSKeyValueCoding.Utility.valueForKey(object, key);
	}

	public void handleTakeValueForUnboundKey(Object value, String key) {
		NSKeyValueCoding.Utility.takeValueForKey(object, value, key);
	}

}
