package er.extensions.foundation;

import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSProperties;
import com.webobjects.foundation._NSStringUtilities;
import com.webobjects.foundation.properties.NSPropertiesCoordinator;
import com.webobjects.foundation.properties.NSPropertyProcessor;
import com.webobjects.foundation.properties.NSPropertyValidationError;
import com.webobjects.foundation.properties.NSPropertyValue;

/**
 * This processor allows a property to be filtered out depending on the application instance number.
 * <p>
 * For this processor to work, the instance number must be set using the {@link #setInstanceNumber(int)}
 * method. WebObjects <strong>does not</strong> do this automatically.
 * </p>
 * <p>
 * This processor looks for the <code>@forInstance</code> key. Consider the following properties file:
 * </p>
 * <pre>
 * foo.bar=30
 * foo.bar.@forInstance.20-100=50
 * foo.bar.@forInstance.57=200</pre>
 * <p>
 * For this file, the <code>foo.bar</code> property on an application running as instance number 57
 * will have the value of <code>200</code>. Applications with instance numbers between 20 and 200 that
 * are not 57 will have the value of <code>50</code>. Finally, the value will be <code>30</code> for
 * any other instance number.
 * </p>
 * 
 */
public class ERXInstanceRangeProcessor extends NSPropertyProcessor {

    private static int _instanceNumber = -1;
    
    protected int instanceNumber() {
        if (_instanceNumber == -1) {
            _instanceNumber = NSProperties.intForKey(NSProperties.stringForKeyWithDefault("er.extensions.ERXProperties.instanceNumberKey", "er.extensions.instanceNumber"));
        }
        return _instanceNumber;
    }
    
    @Override
    public void preProcess(NSMutableArray<NSPropertyValue> values, NSMutableArray<NSPropertyValidationError> errors, NSPropertiesCoordinator container) {
        if(instanceNumber() != -1) {
            for (NSPropertyValue value : values) {
                String key = value.currentKey();
                String parameter = parameterInKeyForElement(key, "@forInstance");
                if(parameter != null && parameter.length() > 0) {
                    if(!_NSStringUtilities.isValueInRange(instanceNumber(), parameter)) {
                        value.setVisible(false);
                    }
                    value.setProcessedKey(keyByRemovingElementAndNextElements(key, "@forInstance", 1));
                }
            }
        }
    }

    @Override
    public void postProcess(NSPropertiesCoordinator container,
            NSMutableArray<NSPropertyValidationError> errors) {
        // TODO Auto-generated method stub
        
    }
}
