package er.extensions.foundation;


import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.properties.NSPropertiesCoordinator;
import com.webobjects.foundation.properties.NSPropertyProcessor;
import com.webobjects.foundation.properties.NSPropertyValidationError;
import com.webobjects.foundation.properties.NSPropertyValue;

import er.extensions.crypting.ERXCrypto;

/**
 * This processor is able to decrypt a property value whose key contains the <code>@encrypted</code> element.
 * <p>
 * Consider the following property definition:
 * </p>
 * <pre>
 * foo.bar.@encrypted=0704ef92f72f23600a9b69e42870d10eaf89b66a3e2eaf0a</pre>
 * <p>
 * <code>ERXEncryptedProcessor</code> will evaluate this as a new property with key <code>foo.bar</code>
 * and value <code>super-secret-password</code>.
 * </p>
 */
public class ERXEncryptedProcessor extends NSPropertyProcessor {

//    public String valueForKeyValueAndParameters(String key, String value, String parameters) {
//        return ERXCrypto.defaultCrypter().decrypt(value);
//    }

    @Override
    public void preProcess(NSMutableArray<NSPropertyValue> values, NSMutableArray<NSPropertyValidationError> errors, NSPropertiesCoordinator container) {
        for (NSPropertyValue value : values) {
            String key = value.currentKey();
            if (keyContainsElement(key, "@encrypted") ) {
                value.setProcessedValue(ERXCrypto.defaultCrypter().decrypt(value.currentValue()));
                value.setProcessedKey(keyByRemovingElement(key,"@encrypted"));
            }
        }
    }

    @Override
    public void postProcess(NSPropertiesCoordinator container, NSMutableArray<NSPropertyValidationError> errors) {
        
    }

}
