package er.extensions.foundation;


import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.properties.NSPropertiesCoordinator;
import com.webobjects.foundation.properties.NSPropertyProcessor;
import com.webobjects.foundation.properties.NSPropertyValidationError;
import com.webobjects.foundation.properties.NSPropertyValue;

/**
 * This processor handles application of template substitutions in properties.
 * <p>
 * A template place-holder is defined using <code>@@property-key@@</code>. The place-holder
 * will be replaced by the value of the property <code>property-key</code>.
 * </p>
 * <p>
 * Consider the following properties file:
 * </p>
 * <pre>
 * com.example.myApplication.fileRoot=/usr/local
 * com.example.myApplication.settingsFile=@@com.example.myApplication.fileRoot@@/etc/settings.txt</pre>
 * <p>
 * When this property file is loaded, the <code>com.example.myApplication.settingsFile</code> property
 * will have the value <code>/usr/local/etc/settings.txt</code>.
 * </p>
 */
public class ERXPropertyTemplateProcessor extends NSPropertyProcessor {

    @Override
    public void preProcess(NSMutableArray<NSPropertyValue> values, NSMutableArray<NSPropertyValidationError> errors, NSPropertiesCoordinator container) {
        
    }

    @Override
    public void postProcess(NSPropertiesCoordinator container, NSMutableArray<NSPropertyValidationError> errors) {
        while(true) {
            boolean appliedChanges = false;
            
            for (String key : container.allKeys()) {
                String value = (String)container.valueForKey(key);
                String processedValue = ERXSimpleTemplateParser.parseTemplatedStringWithObject(value, container);
                if(!processedValue.equals(value)) {
                    appliedChanges = true;
                    container.replaceProcessedValueForKey(processedValue, key);
                }
            }
            
            if(!appliedChanges) {
                return;
            }
        }
    }

}
