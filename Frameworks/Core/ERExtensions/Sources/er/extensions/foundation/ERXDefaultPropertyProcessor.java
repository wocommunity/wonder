package er.extensions.foundation;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.properties.NSCachedProperty;
import com.webobjects.foundation.properties.NSPropertiesCoordinator;
import com.webobjects.foundation.properties.NSPropertyProcessor;
import com.webobjects.foundation.properties.NSPropertyValidationError;
import com.webobjects.foundation.properties.NSPropertyValue;

/**
 * This processor allows defining a property default value using another property.
 * <p>
 * The property containing the default value of property <code>p</code> should have a similar
 * key to <code>p</code> but ending with the <code>Default</code> element.
 * </p>
 * <p>
 * Consider the following properties file:
 * </p>
 * <pre>
 * foo.bar=10
 * foo.bar.Default=20
 * my.property.Default=30</pre>
 * <p>
 * When this file is loaded, <code>foo.bar</code> will keep the value of <code>10</code>. As
 * the property is already defined, the default value doesn't apply. On the other hand, a
 * new property with key <code>my.property</code> will be created with the value of <code>30</code>.
 * All properties that end with the <code>Default</code> element are not deleted.
 * </p>
 */
public class ERXDefaultPropertyProcessor extends NSPropertyProcessor {

    public static final String DefaultKey = "Default";

    @Override
    public void preProcess(NSMutableArray<NSPropertyValue> values, NSMutableArray<NSPropertyValidationError> errors, NSPropertiesCoordinator container) {

    }

    @Override
    public void postProcess(NSPropertiesCoordinator container, NSMutableArray<NSPropertyValidationError> errors) {
        try {
            NSArray<String> allKeys = container.allKeys();
            for(int i = 0; i < allKeys.count(); i++) {
                String key = allKeys.objectAtIndex(i);
                if (DefaultKey.equals(lastElementOfKey(key))) {
                    String finalKey = keyWithoutLastElement(key);
                    NSPropertyValue value = container._getPropertyValueForKey(finalKey);
                    if( value == null ) {
                        value = (NSPropertyValue) container._getPropertyValueForKey(key).clone();
                        value.setProcessedKey(finalKey);
                        container.addValue(value);
                    }
                }
            }
        } catch (CloneNotSupportedException e) {
            throw NSForwardException._runtimeExceptionForThrowable(e);
        }
    }



}
