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
		int i = 0;
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

	private String _cachedURLForResource(String s, String s1, NSArray nsarray) {
		String s2 = null;
		if(s1 != null) {
			WODeployedBundle wodeployedbundle = _cachedBundleForFrameworkNamed(s1);
			if(wodeployedbundle != null) {
				s2 = wodeployedbundle.urlForResource(s, nsarray);
			}
			if(s2 == null) {
				s2 = "/ERROR/NOT_FOUND/framework=" + s1 + "/filename=" + (s == null ? "*null*" : s);
			}
		} else {
			s2 = TheAppProjectBundle.urlForResource(s, nsarray);
			if(s2 == null) {
				String s3 = WOApplication.application().name();
				s2 = "/ERROR/NOT_FOUND/app=" + s3 + "/filename=" + (s == null ? "*null*" : s);
			}
		}
		return s2;
	}

	public String urlForResourceNamed(String s, String s1, NSArray nsarray, WORequest worequest) {
		String s2 = null;
		if(worequest == null || worequest != null && worequest.isUsingWebServer() && !WOApplication.application()._rapidTurnaroundActiveForAnyProject()) {
			s2 = _cachedURLForResource(s, s1, nsarray);
		} else {
			URL url = pathURLForResourceNamed(s, s1, nsarray);
			String s3 = null;
			if(url == null) {
				s3 = "ERROR_NOT_FOUND_framework_" + (s1 == null ? "*null*" : s1) + "_filename_" + (s == null ? "*null*" : s);
			} else {
				s3 = url.toString();
				if(null == cachedDataForKey(s3)) {
					String s4 = contentTypeForResourceNamed(s3);
					WOURLValuedElementData wourlvaluedelementdata = new WOURLValuedElementData(null, s4, s3);
					urlValuedElementsData.setObjectForKey(wourlvaluedelementdata, s3);
				}
			}
			String s5 = _NSStringUtilities.concat("wodata", "=", WOURLEncoder.encode(s3));
			WOContext wocontext = null;
			String s6 = WOApplication.application().resourceRequestHandlerKey();
			if(worequest != null) {
				wocontext = (WOContext) ((ERXRequest)worequest).context();
			}
			if(wocontext != null) {
				s2 = wocontext.urlWithRequestHandlerKey(s6, null, s5);
			} else {
				StringBuffer stringbuffer = new StringBuffer(worequest.applicationURLPrefix());
				stringbuffer.append('/');
				stringbuffer.append(s6);
				stringbuffer.append('?');
				stringbuffer.append(s5);
				s2 = stringbuffer.toString();
			}
		}
		return s2;
	}

    private WOURLValuedElementData cachedDataForKey(String s) {
        return (WOURLValuedElementData)urlValuedElementsData.objectForKey(s);
    }
    
	public WOURLValuedElementData _cachedDataForKey(String s) {
		WOURLValuedElementData wourlvaluedelementdata = null;
		if(s != null) {
			wourlvaluedelementdata = cachedDataForKey(s);
		}
		return wourlvaluedelementdata;
	}

}
