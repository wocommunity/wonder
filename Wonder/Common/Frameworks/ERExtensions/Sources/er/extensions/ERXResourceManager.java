package er.extensions;

import java.lang.reflect.Field;
import java.net.URL;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResourceManager;
import com.webobjects.appserver._private.WODeployedBundle;
import com.webobjects.appserver._private.WOProjectBundle;
import com.webobjects.appserver._private.WOURLEncoder;
import com.webobjects.appserver._private.WOURLValuedElementData;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSLog;
import com.webobjects.foundation._NSStringUtilities;
import com.webobjects.foundation._NSThreadsafeMutableDictionary;

/**
 * Replacement of the WOResourceManager which adds:<ul>
 * <li> dealing with nested web server resources when not deploying
 * </ul>
 * @author ak
 *
 */
public class ERXResourceManager extends WOResourceManager {
	private WODeployedBundle TheAppProjectBundle;
	private _NSThreadsafeMutableDictionary urlValuedElementsData;
	
	protected ERXResourceManager() {
		TheAppProjectBundle = _initAppBundle();
		try {
			Field field = WOResourceManager.class.getDeclaredField("_urlValuedElementsData");
			field.setAccessible(true);
			// AK: yeah, hack, I know...
			urlValuedElementsData = (_NSThreadsafeMutableDictionary) field.get(this);
		}
		catch (Exception e) {
			throw NSForwardException._runtimeExceptionForThrowable(e);
		}
	}

	private static WODeployedBundle _initAppBundle() {
		Object obj = null;
		try {
			WODeployedBundle wodeployedbundle = WODeployedBundle.deployedBundle();
			obj = wodeployedbundle.projectBundle();
			if(obj != null) {
				NSLog.err.appendln("Application project found: Will locate resources in '" + ((WOProjectBundle)obj).projectPath() + "' rather than '" + wodeployedbundle.bundlePath() + "' .");
			} else {
				obj = wodeployedbundle;
			}
		}
		catch(Exception exception) {
			NSLog.err.appendln("<WOResourceManager> Unable to initialize AppProjectBundle for reason:" + exception.toString());
			if(NSLog.debugLoggingAllowedForLevelAndGroups(1, 0L)) {
				NSLog.debug.appendln(exception);
			}
			throw NSForwardException._runtimeExceptionForThrowable(exception);
		}
		return ((WODeployedBundle) (obj));
	}

	private String _cachedURLForResource(String name, String bundleName, NSArray languages, WORequest request) {
		String result = null;
		if(bundleName != null) {
			WODeployedBundle wodeployedbundle = _cachedBundleForFrameworkNamed(bundleName);
			if(wodeployedbundle != null) {
				result = wodeployedbundle.urlForResource(name, languages);
			}
			if(result == null) {
				result = "/ERROR/NOT_FOUND/framework=" + bundleName + "/filename=" + (name == null ? "*null*" : name);
			}
		} else {
			result = TheAppProjectBundle.urlForResource(name, languages);
			if(result == null) {
				String appName = WOApplication.application().name();
				result = "/ERROR/NOT_FOUND/app=" + appName + "/filename=" + (name == null ? "*null*" : name);
			}
		}
	
		String resourceUrlPrefix = null;
		if (ERXRequest.isRequestSecure(request)) {
			resourceUrlPrefix = ERXProperties.stringForKey("er.extensions.ERXResourceManager.secureResourceUrlPrefix");
		}
		else {
			resourceUrlPrefix = ERXProperties.stringForKey("er.extensions.ERXResourceManager.resourceUrlPrefix");
		}
		if (resourceUrlPrefix != null && resourceUrlPrefix.length() > 0) {
			result = resourceUrlPrefix + result;
		}
		
		return result;
	}

	public String urlForResourceNamed(String name, String bundleName, NSArray languages, WORequest request) {
		String completeURL = null;
		if(request == null || request != null && request.isUsingWebServer() && !WOApplication.application()._rapidTurnaroundActiveForAnyProject()) {
			completeURL = _cachedURLForResource(name, bundleName, languages, request);
		} else {
			URL url = pathURLForResourceNamed(name, bundleName, languages);
			String fileURL = null;
			if(url == null) {
				fileURL = "ERROR_NOT_FOUND_framework_" + (bundleName == null ? "*null*" : bundleName) + "_filename_" + (name == null ? "*null*" : name);
			} else {
				fileURL = url.toString();
				cacheDataIfNotInCache(fileURL);
			}
			String encoded = WOURLEncoder.encode(fileURL);
			WOContext context = null;
			String key = WOApplication.application().resourceRequestHandlerKey();
			if (WOApplication.application().isDirectConnectEnabled() && ERXApplication.isWO54()) {
				// AK: 5.4
				key =  "_wr_";
			}
			if(request != null) {
				context = (WOContext) request.valueForKey("context");
			}
			String wodata = _NSStringUtilities.concat("wodata", "=", encoded);
			if(context != null) {
				completeURL = context.urlWithRequestHandlerKey(key, null, wodata);
			} else {
				StringBuffer stringbuffer = new StringBuffer(request.applicationURLPrefix());
				stringbuffer.append('/');
				stringbuffer.append(key);
				stringbuffer.append('?');
				stringbuffer.append(wodata);
				completeURL = stringbuffer.toString();
			}
			// AK: TODO get rid of regex
			int offset = completeURL.indexOf("?wodata=file%3A");
			if(offset >= 0) {
				completeURL = completeURL.replaceFirst("\\?wodata=file%3A", "/wodata=");
				if(completeURL.indexOf("/wodata=") > 0) {
					completeURL = completeURL.replaceAll("%2F", "/");
				}
			}
		}
		return completeURL;
	}

    private WOURLValuedElementData cachedDataForKey(String key) {
    	WOURLValuedElementData data = (WOURLValuedElementData)urlValuedElementsData.objectForKey(key);
    	if (data == null && key != null && key.startsWith("file:") && ERXApplication.isDevelopmentModeSafe()) {
    		data = cacheDataIfNotInCache(key);
    	}
    	return data;
    }
    
    protected WOURLValuedElementData cacheDataIfNotInCache(String key) {
    	WOURLValuedElementData data = (WOURLValuedElementData)urlValuedElementsData.objectForKey(key);
    	if (data == null) {
			String contentType = contentTypeForResourceNamed(key);
			data = new WOURLValuedElementData(null, contentType, key);
			if (data != null) {
				urlValuedElementsData.setObjectForKey(data, key);
			}
    	}
    	return data;
    }
    
	public WOURLValuedElementData _cachedDataForKey(String key) {
		WOURLValuedElementData wourlvaluedelementdata = null;
		if(key != null) {
			wourlvaluedelementdata = cachedDataForKey(key);
		}
		return wourlvaluedelementdata;
	}

}
