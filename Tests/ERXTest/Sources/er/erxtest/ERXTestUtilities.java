package er.erxtest;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import com.webobjects.eoaccess.EODatabaseContext;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;

import er.extensions.eof.ERXEC;


public class ERXTestUtilities {

	/** Fix the ERXTest model so that it can be used by any give adaptor. What needs to be
	 * done to the model? In some cases, very little. For some adaptor, problematic attributes
	 * may need to be fixed.
	 * 
	 * @param adaptorName for example, "Memory" or "MySQL".
	 */
	@SuppressWarnings("cast")
	public static void fixModelsForAdaptorNamed(String adaptorName) {
		
		if (adaptorName.equals("Memory")) {
			for (EOModel model: (NSArray<EOModel>)EOModelGroup.defaultGroup().models()) {
				model.setAdaptorName("Memory");
				model.setConnectionDictionary((NSDictionary<String, Object>) NSDictionary.EmptyDictionary);
			}
			return;
		}

		if (adaptorName.equals("MySQL")) {

			NSDictionary conn = new NSDictionary(
					Arrays.asList(
							Application.wobuild.getProperty("wo.test.dbAccess.MySQL.URL"),
							Application.wobuild.getProperty("wo.test.dbAccess.MySQL.name"),
							Application.wobuild.getProperty("wo.test.dbAccess.MySQL.password")).toArray(),
					Arrays.asList("URL", "username", "password").toArray());

			EOEditingContext ec = ERXEC.newEditingContext();
			
			for (EOModel model: (NSArray<EOModel>)EOModelGroup.defaultGroup().models()) {
				model.setAdaptorName("JDBC");
				model.setConnectionDictionary(conn);
				EODatabaseContext.forceConnectionWithModel(model, conn, ec);
			}

			return;
		}
	}
	
	public static String randomName(String prefix) {
		return prefix+"_"+System.currentTimeMillis()+"_"+(new Random()).nextDouble();
	}

	public static void deleteObjectsWithPrefix(EOEditingContext ec, String entityName, String prefix) {
		for (Object obj : (NSArray<Object>)EOUtilities.objectsWithQualifierFormat(ec, entityName, "name like '"+prefix+"_*'", null)) {
			//System.out.println("deleteObjectsWithPrefix:: deleting object: "+obj);
			ec.deleteObject((EOEnterpriseObject)obj);
		}
		ec.saveChanges();
	}

	public static void deleteObjectsWithPrefix(String entityName, String prefix) {
		EOEditingContext ec = ERXEC.newEditingContext();
		for (Object obj : (NSArray<Object>)EOUtilities.objectsWithQualifierFormat(ec, entityName, "name = '"+prefix+"_*'", null)) {
			ec.deleteObject((EOEnterpriseObject)obj);
		}
		ec.saveChanges();
	}

	public static void deleteObjectsWithPrefix(EOEditingContext ec, NSArray<Object> eos) {
		for (Object obj : eos) {
			ec.deleteObject((EOEnterpriseObject)obj);
		}
		ec.saveChanges();
	}

	public static int pkOne(EOEditingContext ec, EOEnterpriseObject eo) {
		return ((Integer)((List)EOUtilities.primaryKeyForObject(ec, eo).values()).get(0)).intValue();
	}
}
