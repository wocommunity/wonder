package er.rest;

import er.extensions.ERXProperties;

public class ERXXmlRestResponseWriter extends ERXAbstractXmlRestResponseWriter {
	public static final String REST_PREFIX = "ERXRest.";
	public static final String DETAILS_PREFIX = ".details";
	public static final String PROPERTIES_PREFIX = ".properties";
	
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
		boolean displayDetails = Boolean.valueOf(cascadingValue(context, result, ERXXmlRestResponseWriter.REST_PREFIX, ERXXmlRestResponseWriter.DETAILS_PREFIX, "false")).booleanValue();
		return displayDetails;
	}

	protected String[] displayProperties(ERXRestContext context, ERXRestKey result) throws ERXRestException, ERXRestNotFoundException, ERXRestSecurityException {
		String[] displayPropertyNames;
		String displayPropertyNamesStr = cascadingValue(context, result, ERXXmlRestResponseWriter.REST_PREFIX, ERXXmlRestResponseWriter.PROPERTIES_PREFIX, null);
		if (displayPropertyNamesStr == null) {
			displayPropertyNames = null;
		}
		else {
			displayPropertyNames = displayPropertyNamesStr.split(",");
		}
		return displayPropertyNames;
	}
}