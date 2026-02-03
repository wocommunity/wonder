package er.r2d2w.delegates;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

import er.directtoweb.pages.ERD2WPage;
import er.directtoweb.pages.ERD2WPage.ValidationDelegate;

public class EditListValidationDelegate extends ValidationDelegate {
	/**
	 * Do I need to update serialVersionUID? See section 5.6 <cite>Type Changes
	 * Affecting Serialization</cite> on page 51 of the <a
	 * href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object
	 * Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;
	
	private static final Logger log = LoggerFactory.getLogger(EditListValidationDelegate.class);

	private NSMutableDictionary<EOEnterpriseObject, NSMutableArray<String>> _keyPathsWithValidationExceptions = new NSMutableDictionary<EOEnterpriseObject, NSMutableArray<String>>();

	public EditListValidationDelegate(ERD2WPage page) {
		super(page);
	}

	@Override
	public boolean hasValidationExceptionForPropertyKey() {
		Object obj = _page.object();
		NSMutableArray<String> keyPaths = _keyPathsWithValidationExceptions.objectForKey(obj);
		return keyPaths != null && _page.propertyKey() != null && keyPaths.contains(_page.propertyKey());
	}

	@Override
	public void validationFailedWithException(Throwable e, Object value, String keyPath) {
        if (log.isDebugEnabled()) {
            log.debug("Validation failed with exception: " + e + " value: " + value + " keyPath: " + keyPath);
        }
        EOEnterpriseObject obj = _page.object();
        if(obj != null && keyPath != null) {
        	NSMutableArray<String> keyPaths = _keyPathsWithValidationExceptions.objectForKey(obj);
        	if(keyPaths == null) {
        		keyPaths = new NSMutableArray<String>();
        		_keyPathsWithValidationExceptions.setObjectForKey(keyPaths, obj);
        	}
        	if(!keyPaths.contains(keyPath)) {
        		keyPaths.addObject(keyPath);
        	}
        	NSMutableDictionary<String, String> messagesForObject = 
        			(NSMutableDictionary<String, String>) errorMessages().objectForKey(obj);
        	if(messagesForObject == null) {
        		messagesForObject = new NSMutableDictionary<String, String>();
        		errorMessages().setObjectForKey(messagesForObject, obj);
        	}
        	messagesForObject.setObjectForKey(e.getMessage(), keyPath);
        } else {
        	//TODO do something useful here
        	log.info("Validation failed with exception: " + e + " value: " + value + " keyPath: " + keyPath, e);
        }
	}
	
	@Override
	public void clearValidationFailed() {
		_keyPathsWithValidationExceptions.removeAllObjects();
	}

	public String errorMessageForPropertyKey() {
        EOEnterpriseObject obj = _page.object();
        String keyPath = _page.propertyKey();
        if(obj != null && keyPath != null) {
        	NSMutableDictionary<String, String> messagesForObject = 
        			(NSMutableDictionary<String, String>) errorMessages().objectForKey(obj);
        	if(messagesForObject != null) {
        		return messagesForObject.objectForKey(keyPath);
        	}
        }
        return null;
	}
}
