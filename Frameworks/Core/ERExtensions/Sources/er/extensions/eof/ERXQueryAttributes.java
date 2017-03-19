package er.extensions.eof;

import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.foundation.NSMutableArray;

/**
 * <h1>ERXQueryAttributes.java</h1>
 * 
 * <p style="max-width:700px">
 * A convenient subclass of NSMutableArray<EOAttribute> to make it easy to create multiple
 * ad hoc attributes that can be used with ERXQuery.  It uses a fluent style API so that
 * it can be used like this:
 * <pre>
 * {@code
 * NSArray<EOAttribute> attributes =
 *     ERXQueryAttributes.create(claimEntity)
 *         .add("providerFullName", "provider.fullName", "varchar50")
 *         .add("claimCount", "COUNT(DISTINCT claimID)", "intNumber");
 * }
 * </pre>
 * </p>
 * @author Ricardo J. Parada
 */

@SuppressWarnings("javadoc")

public class ERXQueryAttributes extends NSMutableArray<EOAttribute> {
	protected EOEntity _entity;
	
	protected ERXQueryAttributes(EOEntity entity) {
		this._entity = entity;
	}
	
	public static ERXQueryAttributes create(EOEntity entity) {
		return new ERXQueryAttributes(entity);
	}
	
	public ERXQueryAttributes add(String name, String definition, String prototype) {
		EOAttribute attr = ERXQueryEOAttribute.create(_entity, name, definition, prototype);
		addObject(attr);
		return this;
	}
}