package er.rest.routes.components;

import com.webobjects.appserver.WOContext;

/**
 * Generate a WOHyperlink that points to a particular ERXRouteController route (this is a quicky impl and not totally thought out yet).
 * 
 * @author mschrag
 * @binding entityName (optional) the name of the entity to link to
 * @binding id (optional) the id of the entity to link to
 * @binding record (optional) the record to link to
 * @binding action (optional) the rest action to perform (defaults to "show" when an id or record is set, "index" if only an entityName is set)
 * @binding secure (optional) whether or not to generate a secure url (defaults to the same as the current request)
 * @binding queryDictionary (optional) additional query parameters dictionary
 * @binding format (optional) the format to link to (defaults to "html")
 */
public class ERXRouteLink extends ERXRouteURL {
	public ERXRouteLink(WOContext context) {
		super(context);
	}
}
