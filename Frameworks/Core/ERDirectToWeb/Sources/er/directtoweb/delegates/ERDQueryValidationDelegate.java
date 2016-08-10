package er.directtoweb.delegates;

import java.math.BigDecimal;
import java.util.Enumeration;

import com.webobjects.appserver.WODisplayGroup;
import com.webobjects.directtoweb.D2WContext;
import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSValidation;

import er.directtoweb.pages.ERD2WQueryPage;
import er.extensions.foundation.ERXStringUtilities;
import er.extensions.foundation.ERXValueUtilities;
import er.extensions.validation.ERXValidationFactory;

/**
 * A delegate class for validating user inputs before a query is executed.  Validation rules are derived from the D2W
 * context.
 * <p>
 * To disallow a query with no user inputs, create a rule like:
 * <p>
 * <code>entity.name = 'Foo' =&gt; allowsEmptyQueryValue = "false" (BooleanAssignment)</code>
 * <p>
 * To define a validation for a propertyKey, create a rule like:
 * <p>
 * <code>entity.name = 'Foo' and propertyKey = 'bar' =&gt; allowsEmptyQueryValue = "false" (BooleanAssignment)</code>
 * <p>
 * To define a minimum length validation for a (String) propertyKey, create a rule like:
 * <p>
 * <code>entity.name = 'Foo' and propertyKey = 'bar' =&gt; minimumInputLength = "3" (Assignment)</code>
 * <p>
 * Subclasses wishing to implement custom validation logic should implement the {@link #validateQueryValues} method.
 * The implementation should catch validation exceptions and invoke
 * {@link er.directtoweb.pages.ERD2WPage#validationFailedWithException(Throwable, Object, String)} with any caught exceptions.  To customize
 * behavior, while retaining the default checks, extend {@link ERDQueryValidationDelegate.DefaultQueryValidationDelegate}
 * to perform custom validations and then call {@link #validateQueryValues} on the superclass.
 *
 * @author Travis Cripps
 * @d2wKey displayPropertyKeys
 * @d2wKey maximumInputLength
 * @d2wKey minimumInputLength
 * @d2wKey maximumInputValue
 * @d2wKey minimumInputValue
 */
public abstract class ERDQueryValidationDelegate {

    // The validation keys correspond to rule names for validation definitions in the D2W model.
    public static interface ValidationKeys {
        public static final String AllowsEmptyQuery = "allowsEmptyQuery"; // Does the page allow a query with no query values?
        public static final String AllowsEmptyQueryValue = "allowsEmptyQueryValue"; // Does the corresponding property key allow an empty query value?

        public static final String MaximumInputLength = "maximumInputLength"; // A minimum input length for String attributes.
        public static final String MinimumInputLength = "minimumInputLength"; // A maximum input length for String attributes.

        public static final String MaximumInputValue = "maximumInputValue"; // A minimum input value for numeric attributes.
        public static final String MinimumInputValue = "minimumInputValue"; // A maximum input value for numeric attributes.
    }

    // The error keys should correspond to entries in the ValidationTemplate.strings file.
    public static interface ErrorKeys {
        public static final String QueryEmpty = "QueryEmpty";
        public static final String QueryValueRequired = "QueryValueRequired";

        public static final String QueryValueTooShort = "QueryValueTooShort";
        public static final String QueryValueTooLong = "QueryValueTooLong";

        public static final String QueryValueTooSmall = "QueryValueTooSmall";
        public static final String QueryValueTooLarge = "QueryValueTooLarge";
    }

    protected D2WContext d2wContext;

    /**
     * Validates the query inputs before executing the query.
     * @param sender query page whose inputs to validate
     */
    public void validateQuery(ERD2WQueryPage sender) {
        d2wContext = sender.d2wContext();

        // First check to see if there are any query values.
        WODisplayGroup displayGroup = sender.displayGroup();
        if (displayGroup.queryMatch().allKeys().count() == 0 && displayGroup.queryMin().allKeys().count() == 0 &&
            displayGroup.queryMax().allKeys().count() == 0 && displayGroup.queryBindings().allKeys().count() == 0 &&
            !ERXValueUtilities.booleanValueWithDefault(d2wContext.valueForKey(ValidationKeys.AllowsEmptyQuery), true)) {
            throw ERXValidationFactory.defaultFactory().createCustomException(null, ErrorKeys.QueryEmpty);
        }

        // Check the query values.
        String cachedPropertyKey = d2wContext.propertyKey();
        validateQueryValues(sender);
        d2wContext.setPropertyKey(cachedPropertyKey);
    }

    /**
     * Validates the query input values from the query page's display group.
     * @param sender query page whose inputs to validate
     */
    public abstract void validateQueryValues(ERD2WQueryPage sender);

    /**
     * Gets the D2WContext against which the validation definitions will be evaluated.
     * from
     * @return the D2WContext
     */
    public D2WContext d2wContext() {
        return d2wContext;
    }

    /**
     * Gets the D2W property key corresponding to the display group key by matching the key with one in the D2W
     * context's <code>displayPropertyKeys</code>.
     * @param key from the display group
     * @return the corresponding D2W key, or the original key if not found
     */
    protected String propertyKeyFromDisplayGroupKey(String key) {
        String result = key;
        if (result.indexOf(".") > 0) {
            NSArray propertyKeys = ERXValueUtilities.arrayValueWithDefault(d2wContext().valueForKey("displayPropertyKeys"), NSArray.EmptyArray);
            boolean found;
            do {
                found = propertyKeys.indexOfObject(result) > 0;
                if (!found) {
                    if (result.indexOf(".") > 0) {
                        result = ERXStringUtilities.keyPathWithoutLastProperty(result);
                    } else {
                        break;
                    }
                }
            } while (!found);
            if (!found) { // Give up.
                result = key;
            }
        }
        return result;
    }

    /**
     * Determines if the D2W context contains a validation definition for the provided validation key}.
     * @param key to check
     * @return true if a validation definition for the given key exists
     */
    public boolean hasValidationDefinitionForKey(String key) {
        return d2wContext != null && d2wContext.valueForKey(key) != null;
    }

    /**
     * Validates a string value, checking minimumInputLength and maximumInputLength.
     * @param value to validate
     * @param key of the property to validate
     * @throws NSValidation.ValidationException when the validation fails
     */
    public void validateStringValueForKey(String value, String key) throws NSValidation.ValidationException {
        int minimumLength = ERXValueUtilities.intValueWithDefault(d2wContext().valueForKey(ValidationKeys.MinimumInputLength), 0);
        if (null == value) {
            if (minimumLength > 0) {
                throw ERXValidationFactory.defaultFactory().createCustomException(null, key, value, ErrorKeys.QueryValueRequired);
            }
        } else {
            int maximumLength = ERXValueUtilities.intValueWithDefault(d2wContext().valueForKey(ValidationKeys.MaximumInputLength), -1);

            if (minimumLength > 0 && value.length() < minimumLength) {
                throw ERXValidationFactory.defaultFactory().createCustomException(null, key, value, ErrorKeys.QueryValueTooShort);
            }
            if (maximumLength > 0 && value.length() > maximumLength) {
                throw ERXValidationFactory.defaultFactory().createCustomException(null, key, value, ErrorKeys.QueryValueTooLong);
            }
        }
    }

    /**
     * Validates a string value, checking minimumInputValue and maximumInputValue.
     * @param value to validate
     * @param key of the property to validate
     * @throws NSValidation.ValidationException when the validation fails
     */
    public void validateNumericValueForKey(Number value, String key) throws NSValidation.ValidationException {
        if (null == value) {
            if (d2wContext.valueForKey(ValidationKeys.MinimumInputValue) != null) {
                throw ERXValidationFactory.defaultFactory().createCustomException(null, key, value, "QueryValueRequired");
            }
        } else {
            EOAttribute attribute = d2wContext.attribute();
            String valueType = attribute.valueType();

            if ("s".equals(valueType) || "i".equals(valueType) || "l".equals(valueType)) { // Short or Integer or Long
                long longValue = value.longValue();
                if (hasValidationDefinitionForKey(ValidationKeys.MinimumInputValue)) {
                    Long minimumValue = ERXValueUtilities.longValue(d2wContext().valueForKey(ValidationKeys.MinimumInputValue));
                    if (minimumValue.compareTo(longValue) > 0) {
                        throw ERXValidationFactory.defaultFactory().createCustomException(null, key, value, ErrorKeys.QueryValueTooSmall);
                    }
                }
                if (hasValidationDefinitionForKey(ValidationKeys.MaximumInputValue)) {
                    Long maximumValue = ERXValueUtilities.longValue(d2wContext().valueForKey(ValidationKeys.MaximumInputValue));
                    if (maximumValue.compareTo(longValue) < 0) {
                        throw ERXValidationFactory.defaultFactory().createCustomException(null, key, value, "QueryValueTooLarge");
                    }
                }
            } else if ("f".equals(valueType) || "d".equals(valueType)) { // Float or Double
                double doubleValue = value.doubleValue();
                if (hasValidationDefinitionForKey(ValidationKeys.MinimumInputValue)) {
                    Double minimumValue = ERXValueUtilities.doubleValue(d2wContext().valueForKey(ValidationKeys.MinimumInputValue));
                    if (minimumValue.compareTo(doubleValue) > 0) {
                        throw ERXValidationFactory.defaultFactory().createCustomException(null, key, value, "QueryValueTooSmall");
                    }
                }
                if (hasValidationDefinitionForKey(ValidationKeys.MaximumInputValue)) {
                    Double maximumValue = ERXValueUtilities.doubleValue(d2wContext().valueForKey(ValidationKeys.MaximumInputValue));
                    if (maximumValue.compareTo(doubleValue) < 0) {
                        throw ERXValidationFactory.defaultFactory().createCustomException(null, key, value, "QueryValueTooLarge");
                    }
                }
            } else if ("B".equals(valueType)) { // BigDecimal
                BigDecimal bdValue = (value instanceof BigDecimal) ? (BigDecimal)value : new BigDecimal(value.doubleValue());
                if (hasValidationDefinitionForKey(ValidationKeys.MinimumInputValue)) {
                    BigDecimal minimumValue = ERXValueUtilities.bigDecimalValue(d2wContext().valueForKey(ValidationKeys.MinimumInputValue));
                    if (minimumValue.compareTo(bdValue) > 0) {
                        throw ERXValidationFactory.defaultFactory().createCustomException(null, key, value, "QueryValueTooSmall");
                    }
                }
                if (hasValidationDefinitionForKey(ValidationKeys.MaximumInputValue)) {
                    BigDecimal maximumValue = ERXValueUtilities.bigDecimalValue(d2wContext().valueForKey(ValidationKeys.MaximumInputValue));
                    if (maximumValue.compareTo(bdValue) < 0) {
                        throw ERXValidationFactory.defaultFactory().createCustomException(null, key, value, "QueryValueTooLarge");
                    }
                }
            }
        }
    }


    /**
     * A "default" implementation of a query validation delegate, which simply validates each key in the query page's
     * display group against validation definitions from the D2W rules.
     */
    public static class DefaultQueryValidationDelegate extends ERDQueryValidationDelegate {

        private ERD2WQueryPage queryPage;

        @Override
        public void validateQueryValues(ERD2WQueryPage sender) {
            queryPage = sender;
            
            WODisplayGroup displayGroup = queryPage.displayGroup();
            _validateQueryValues(displayGroup.queryMatch());
            _validateQueryValues(displayGroup.queryMin());
            _validateQueryValues(displayGroup.queryMax());
            _validateQueryValues(displayGroup.queryBindings());
        }

        /**
         * Validates the values in the query dictionary.
         * @param queryDict to validate.
         */
        private void _validateQueryValues(NSDictionary queryDict) {
            for (Enumeration keysEnum = queryDict.keyEnumerator(); keysEnum.hasMoreElements();) {
                String key = (String)keysEnum.nextElement();
                Object value = queryDict.objectForKey(key);
                try {
                    validateValueForQueryKey(value, key);
                } catch (NSValidation.ValidationException ve) {
                    queryPage.validationFailedWithException(ve, null, ve.key());
                }
            }
        }

        /**
         * Validates the value of a particular key in the query dictionary.
         * @param value to validate
         * @param key of the property to validate
         * @throws NSValidation.ValidationException when the validation fails
         */
        public void validateValueForQueryKey(Object value, String key) throws NSValidation.ValidationException {
            if( value instanceof NSKeyValueCoding.Null) { value = null; }

            D2WContext d2wContext = d2wContext();
            String propertyKey = propertyKeyFromDisplayGroupKey(key);
            d2wContext().setPropertyKey(propertyKey);

            if (null == value && !ERXValueUtilities.booleanValueWithDefault(d2wContext.valueForKey(ValidationKeys.AllowsEmptyQueryValue), true)) {
                throw ERXValidationFactory.defaultFactory().createCustomException(null, propertyKey, value, "QueryValueRequired");
            }

            EOAttribute attribute = null;
            if (ERXValueUtilities.booleanValue(d2wContext.valueForKey("isAttribute"))) {
                attribute = d2wContext.attribute();
            } else {
                EORelationship relationship = d2wContext.relationship();
                if (relationship != null && !(value instanceof EOEnterpriseObject)) {
                    String keyWhenRelationship = (String)d2wContext.valueForKey("keyWhenRelationship");
                    if (keyWhenRelationship != null) {
                        EOEntity destinationEntity = relationship.destinationEntity();
                        attribute = destinationEntity.attributeNamed(keyWhenRelationship);
                    }
                }
            }

            if (attribute != null) {
                String valueClassName = attribute.className();
                if (String.class.getName().equals(valueClassName) && value instanceof String) {
                    validateStringValueForKey((String)value, propertyKey);
                } else if (Number.class.getName().equals(valueClassName) || BigDecimal.class.getName().equals(valueClassName)) {
                    validateNumericValueForKey((Number)value, propertyKey);
                }
            }
        }

    }

}
