package er.extensions.formatters;

import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSNumberFormatter;

public class ERXFormatterFactory implements NSKeyValueCoding {

    public ERXFormatterFactory() {
        super();
    }
    
    public ERXMultiplyingNumberFormatter multiplyingFormatter() {
        return new ERXMultiplyingNumberFormatter();
    }
    
    public ERXDividingNumberFormatter dividingFormatter() {
        return new ERXDividingNumberFormatter();
    }
    
    public NSNumberFormatter bytesToKilobytesFormatter() {
    	return ERXNumberFormatter.numberFormatterForPattern("(/1024=)0.00");
    }

    public NSNumberFormatter megabytesToKilobytesFormatter() {
    	return ERXNumberFormatter.numberFormatterForPattern("(*1024=)0.00");
    }
    
    public NSNumberFormatter bytesToMegabytesFormatter() {
    	return ERXNumberFormatter.numberFormatterForPattern("(/1048576=)0.00");
    }

    public NSNumberFormatter megabytesToBytesFormatter() {
    	return ERXNumberFormatter.numberFormatterForPattern("(*1048576=)0.00");
    }

	public Object valueForKey(String key) {
		Object result = null;
		try {
			result = NSKeyValueCoding.DefaultImplementation.valueForKey(this, key);
		} catch (UnknownKeyException ex) {
			result = ERXNumberFormatter.numberFormatterForPattern(key);
		}
		return result;
	}

	public void takeValueForKey(Object object, String key) {
		ERXNumberFormatter.setNumberFormatterForPattern((NSNumberFormatter)object, key);
	}
    
}
