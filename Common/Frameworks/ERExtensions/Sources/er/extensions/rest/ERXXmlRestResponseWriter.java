package er.extensions.rest;

import er.extensions.ERXProperties;

public class ERXXmlRestResponseWriter extends ERXAbstractXmlRestResponseWriter {
	protected String cascadingValue(ERXRestContext context, ERXRestResult result, String propertyPrefix, String propertySuffix, String defaultValue) throws ERXRestException, ERXRestNotFoundException, ERXRestSecurityException {
		ERXRestResult cascadingResult = result.firstResult();
		String cascadingValue = defaultValue;
		boolean matchFound = false;
		while (!matchFound && cascadingResult != null) {
			String keypathWithoutGIDs = cascadingResult.keypath(true, context);

			String propertyName = propertyPrefix + keypathWithoutGIDs.replace('/', '.') + propertySuffix;
			String propertyValueStr = ERXProperties.stringForKey(propertyName);
			if (propertyValueStr != null) {
				cascadingValue = propertyValueStr;
				matchFound = true;
			}
			else if (cascadingResult.nextKey() == null) {
				cascadingResult = null;
			}
			else {
				cascadingResult = cascadingResult.nextResult(context, false);
			}
		}
		return cascadingValue;
	}

	protected boolean displayDetails(ERXRestContext context, ERXRestResult result) throws ERXRestException, ERXRestNotFoundException, ERXRestSecurityException {
		boolean displayDetails = Boolean.valueOf(cascadingValue(context, result, "ERXRest.", ".details", "false")).booleanValue();
		return displayDetails;
	}

	protected String[] displayProperties(ERXRestContext context, ERXRestResult result) throws ERXRestException, ERXRestNotFoundException, ERXRestSecurityException {
		String[] displayPropertyNames;
		String displayPropertyNamesStr = cascadingValue(context, result, "ERXRest.", ".properties", null);
		if (displayPropertyNamesStr == null) {
			displayPropertyNames = null;
		}
		else {
			displayPropertyNames = displayPropertyNamesStr.split(",");
		}
		return displayPropertyNames;
	}
}