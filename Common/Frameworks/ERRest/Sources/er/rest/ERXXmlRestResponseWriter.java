package er.rest;

import com.webobjects.foundation.NSArray;

import er.extensions.ERXProperties;

/**
 * <p>
 * ERXXmlRestResponseWriter provides a concrete implementation of a restful response writer that can generate XML
 * output. This implementation provide support for specifying rendering configuration in your application properties.
 * </p>
 * 
 * <p>
 * There are multiple levels of rendering controls that you can adjust. These adjustments come in two primary forms --
 * details and details properties.
 * </p>
 * 
 * <p>
 * The details setting allows you to specify that for a given keypath, whether or not a particular relationship should
 * display only the id of the related object or the id as well as its properties.
 * </p>
 * 
 * <p>
 * The details properties setting defines the "upper bound" of properties to display to a user for a particular keypath.
 * That is to say that for a particular key path, the user will never be shown any key that is outside of the specified
 * list. However, permissions on a particular entity may restrict access such that the user is not able to see all the
 * keys specified. This control allows you to specify at a rendering level what the user can and cannot see on an
 * object.
 * </p>
 * 
 * <p>
 * These properties take the form:
 * 
 * <pre>
 * ERXRest.[EntityName].details=true/false
 * ERXRest.[EntityName].detailsProperties=property_1,property_2,property_3,...,property_n
 * ERXRest.[EntityName].property_a.property_a_b.details=true/false
 * ERXRest.[EntityName].property_a.property_a_b.detailsProperties=property_1,property_2,property_3,...,property_n
 * </pre>
 * 
 * </p>
 * 
 * <p>
 * For example:
 * 
 * <pre>
 * ERXRest.Organization.details=true
 * ERXRest.Organization.detailsProperties=name,purchasedPlans
 * ERXRest.Organization.purchasedPlans.details=false
 * 
 * ERXRest.Site.details=true
 * ERXRest.Site.detailsProperties=title,organization,disabledAt,memberships,sheetSets,blogEntries
 * ERXRest.Site.blogEntries.details=true
 * ERXRest.Site.blogEntries.detailsProperties=author,submissionDate,title,contents
 * ERXRest.Site.sheetSets.details=false
 * ERXRest.Site.memberships.details=false
 * 
 * ERXRest.BlogEntry.details=true
 * ERXRest.BlogEntry.detailsProperties=site,author,submissionDate,title,contents
 * </pre>
 * 
 * </p>
 * 
 * <p>
 * Note that all properties that appear in a details properties definition should be "actual" property names, not
 * property aliases. Similarly, all entity references should be the actual entity name, not an entity alias.
 * </p>
 * 
 * <p>
 * In the example above, if someone requests an Organization as the top level entity, the details will be displayed. The
 * properties that will be displayed in those details includes "name" and "purchasedPlans", which is a to-many
 * relationship. In the example, we have explicitly declared that Organization.purchasedPlans will not show any details,
 * though this was technically unnecessary because the default is "false".
 * </p>
 * 
 * <p>
 * For a request for http://yoursite/yourapp.woa/rest/Organization/100.xml, the output will look like:
 * 
 * <pre>
 * &lt;Organization id = &quot;100&quot;&gt;
 *   &lt;name&gt;Organization Name&lt;/name&gt;
 *   &lt;purchasedPlans type = &quot;PurchasedPlan&quot;&gt;
 *     &lt;PurchasedPlan id = &quot;200&quot;/&gt;
 *     &lt;PurchasedPlan id = &quot;201&quot;/&gt;
 *     &lt;PurchasedPlan id = &quot;202&quot;/&gt;
 *   &lt;/purchasedPlans&gt;
 * &lt;/Organization&gt;
 * </pre>
 * 
 * </p>
 * 
 * <p>
 * In the second and third blocks of the example, you can see two different specifications for properties to display for
 * a BlogEntry. If you request Site/100/blogEntries.xml, you will see a set of properties that does not include the site
 * relationship of the blog entry (notice that ERXRest.Site.blogEntries.detailsProperties does not specify "site").
 * However, if you request BlogEntry/301.xml you will see the site relationship (notice that
 * ERXRest.BlogEntry.detailsProperties DOES contain "site"). Also note that primary keys should not be used in the
 * definition of renderer configurations. Configuration is assumed to apply to any instance of an object that
 * structurally matches the keypaths you define.
 * </p>
 * 
 * <p>
 * The renderer looks for the longest matching keypath specification in the Properties file to determine whether or not
 * to display properties and which properties to display, so you can construct arbitrarily deep specifications for which
 * properties to display for any given keypath.
 * </p>
 * 
 * @author mschrag
 */
public class ERXXmlRestResponseWriter extends ERXAbstractXmlRestResponseWriter {
	public static final String REST_PREFIX = "ERXRest.";
	public static final String DETAILS_PREFIX = ".details";
	public static final String DETAILS_PROPERTIES_PREFIX = ".detailsProperties";

	private boolean _displayAllProperties;
	private boolean _displayAllDetails;
	private boolean _displayAllToMany;

	/**
	 * Constructs an ERXXmlRestResponseWriter with displayAllProperties = false.
	 */
	public ERXXmlRestResponseWriter() {
		this(false, false);
	}

	/**
	 * Constructs an ERXXmlRestResponseWriter.
	 * 
	 * @param displayAllProperties
	 *            if true, by default all properties are eligible to be displayed (probably should only be true in
	 *            development, but it won't really hurt anything). Note that entity delegates will still control
	 *            permissions on the properties, it just defaults to checking all of them.
	 * @param displayAllToMany
	 *            if true, all to-many relationships will be displayed
	 */
	public ERXXmlRestResponseWriter(boolean displayAllProperties, boolean displayAllToMany) {
		_displayAllProperties = displayAllProperties;
		_displayAllToMany = displayAllToMany;
	}

	protected String cascadingValue(ERXRestContext context, ERXRestKey result, String propertyPrefix, String propertySuffix, String defaultValue) throws ERXRestException, ERXRestNotFoundException, ERXRestSecurityException {
		ERXRestKey cascadingKey = result.firstKey();
		String cascadingValue = defaultValue;
		boolean matchFound = false;
		while (!matchFound && cascadingKey != null) {
			String keypathWithoutGIDs = cascadingKey.path(true);
			String propertyName = propertyPrefix + keypathWithoutGIDs.replace('/', '.') + propertySuffix;
			String propertyValueStr = ERXProperties.stringForKey(propertyName);
			if (propertyValueStr != null) {
				cascadingValue = propertyValueStr;
				matchFound = true;
			}
			else if (cascadingKey.nextKey() == null) {
				cascadingKey = null;
			}
			else {
				cascadingKey = cascadingKey.nextKey();
			}
		}
		return cascadingValue;
	}

	protected boolean displayDetails(ERXRestContext context, ERXRestKey result) throws ERXRestException, ERXRestNotFoundException, ERXRestSecurityException {
		boolean displayDetails;
		String displayDetailsStr = cascadingValue(context, result, ERXXmlRestResponseWriter.REST_PREFIX, ERXXmlRestResponseWriter.DETAILS_PREFIX, null);
		if (displayDetailsStr == null) {
			displayDetails = result.previousKey() == null;
		}
		else {
			displayDetails = Boolean.valueOf(displayDetailsStr).booleanValue();
		}
		return displayDetails;
	}

	protected String[] displayProperties(ERXRestContext context, ERXRestKey result) throws ERXRestException, ERXRestNotFoundException, ERXRestSecurityException {
		String[] displayPropertyNames;
		String displayPropertyNamesStr = cascadingValue(context, result, ERXXmlRestResponseWriter.REST_PREFIX, ERXXmlRestResponseWriter.DETAILS_PROPERTIES_PREFIX, null);
		if (displayPropertyNamesStr == null) {
			if (_displayAllProperties) {
				NSArray allPropertyNames = ERXUnsafeRestEntityDelegate.allPropertyNames(result.entity(), _displayAllToMany);
				displayPropertyNames = new String[allPropertyNames.count()];
				for (int propertyNum = 0; propertyNum < displayPropertyNames.length; propertyNum++) {
					displayPropertyNames[propertyNum] = (String) allPropertyNames.objectAtIndex(propertyNum);
				}
			}
			else {
				displayPropertyNames = null;
			}
		}
		else {
			displayPropertyNames = displayPropertyNamesStr.split(",");
		}
		return displayPropertyNames;
	}
}