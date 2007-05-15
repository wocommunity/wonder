package er.rest;

import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOKeyGlobalID;

public class ERXRestUtils {
	public static String idForEO(EOEnterpriseObject eo) {
		EOKeyGlobalID gid = (EOKeyGlobalID) eo.editingContext().globalIDForObject(eo);
		Object id = gid.keyValues()[0];
		return String.valueOf(id);
	}
}
