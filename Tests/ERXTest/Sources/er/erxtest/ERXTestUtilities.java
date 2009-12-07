package er.erxtest;

import java.util.Arrays;

import com.webobjects.eoaccess.EODatabaseContext;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSDictionary;

import er.extensions.eof.ERXEC;


public class ERXTestUtilities {

	/** Fix the ERXTest model so that it can be used by any give adaptor. What needs to be
	 * done to the model? In some cases, very little. For some adaptor, problematic attributes
	 * may need to be fixed.
	 * 
	 * @param adaptorName for example, "Memory" or "MySQL".
	 */
	public static void fixModelsForAdaptorNamed(String adaptorName) {
		
		if (adaptorName.equals("Memory")) {
			return;
		}

		if (adaptorName.equals("MySQL")) {
			
			NSDictionary conn = new NSDictionary(
					Arrays.asList(
							Application.wobuild.getProperty("wo.test.dbAccess.URL"),
							Application.wobuild.getProperty("wo.test.dbAccess.name"),
							Application.wobuild.getProperty("wo.test.dbAccess.password")).toArray(),
					Arrays.asList("URL", "username", "password").toArray());

			EOEditingContext ec = ERXEC.newEditingContext();
			
			for (EOModel model: EOModelGroup.defaultGroup().models()) {
				model.setAdaptorName("JDBC");
				EODatabaseContext.forceConnectionWithModel(model, conn, ec);
			}

			return;
		}
	}
}
