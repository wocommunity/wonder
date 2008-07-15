package er.extensions;

import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.Logger;

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
 * Replacement of the WOResourceManager which adds:
 * <ul>
 * <li> dealing with nested web server resources when not deploying
 * <li> resource versioning (for better caching control)
 * </ul>
 * 
 * @property er.extensions.ERXResourceManager.versionManager the class name of the version manager to use (or "default", or "properties")
 * @author ak
 * @fiddler mschrag
 */
public class ERXResourceManager extends WOResourceManager {
	private static Logger log = Logger.getLogger(ERXResourceManager.class);
	private WODeployedBundle TheAppProjectBundle;
	private _NSThreadsafeMutableDictionary _urlValuedElementsData;
	private IVersionManager _versionManager;

	protected ERXResourceManager() {
		TheAppProjectBundle = _initAppBundle();
		try {
			Field field = WOResourceManager.class.getDeclaredField("_urlValuedElementsData");
			field.setAccessible(true);
			// AK: yeah, hack, I know...
			_urlValuedElementsData = (_NSThreadsafeMutableDictionary) field.get(this);
		}
		catch (Exception e) {
			throw NSForwardException._runtimeExceptionForThrowable(e);
		}
		String versionManagerClassName = ERXProperties.stringForKeyWithDefault("er.extensions.ERXResourceManager.versionManager", "default");
		if ("default".equals(versionManagerClassName)) {
			_versionManager = new DefaultVersionManager();
		}
		else if ("properties".equals(versionManagerClassName)) {
			_versionManager = new PropertiesVersionManager();
		}
		else {
			try {
				_versionManager = Class.forName(versionManagerClassName).asSubclass(IVersionManager.class).newInstance();
			}
			catch (Exception e) {
				throw new RuntimeException("Unable to create the specified version manager '" + versionManagerClassName + ".", e);
			}
		}
	}

	/**
	 * Sets the version manager to use for this resource manager.
	 * 
	 * @param versionManager the version manager to use for this resource manager
	 */
	public void setVersionManager(IVersionManager versionManager) {
		_versionManager = versionManager;
	}
	
	/**
	 * @return the current version manager for this resource manager.
	 */
	public IVersionManager versionManager() {
		return _versionManager;
	}

	private static WODeployedBundle _initAppBundle() {
		Object obj = null;
		try {
			WODeployedBundle wodeployedbundle = WODeployedBundle.deployedBundle();
			obj = wodeployedbundle.projectBundle();
			if (obj != null) {
				NSLog.err.appendln("Application project found: Will locate resources in '" + ((WOProjectBundle) obj).projectPath() + "' rather than '" + wodeployedbundle.bundlePath() + "' .");
			}
			else {
				obj = wodeployedbundle;
			}
		}
		catch (Exception exception) {
			NSLog.err.appendln("<WOResourceManager> Unable to initialize AppProjectBundle for reason:" + exception.toString());
			if (NSLog.debugLoggingAllowedForLevelAndGroups(1, 0L)) {
				NSLog.debug.appendln(exception);
			}
			throw NSForwardException._runtimeExceptionForThrowable(exception);
		}
		return ((WODeployedBundle) (obj));
	}

	private String _cachedURLForResource(String name, String bundleName, NSArray languages, WORequest request) {
		String result = null;
		if (bundleName != null) {
			WODeployedBundle wodeployedbundle = _cachedBundleForFrameworkNamed(bundleName);
			if (wodeployedbundle != null) {
				result = wodeployedbundle.urlForResource(name, languages);
			}
			if (result == null) {
				result = "/ERROR/NOT_FOUND/framework=" + bundleName + "/filename=" + (name == null ? "*null*" : name);
			}
		}
		else {
			result = TheAppProjectBundle.urlForResource(name, languages);
			if (result == null) {
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
		if (request == null || request != null && request.isUsingWebServer() && !WOApplication.application()._rapidTurnaroundActiveForAnyProject()) {
			completeURL = _cachedURLForResource(name, bundleName, languages, request);
		}
		else {
			URL url = pathURLForResourceNamed(name, bundleName, languages);
			String fileURL = null;
			if (url == null) {
				fileURL = "ERROR_NOT_FOUND_framework_" + (bundleName == null ? "*null*" : bundleName) + "_filename_" + (name == null ? "*null*" : name);
			}
			else {
				fileURL = url.toString();
				cacheDataIfNotInCache(fileURL);
			}
			String encoded = WOURLEncoder.encode(fileURL);
			WOContext context = null;
			String key = WOApplication.application().resourceRequestHandlerKey();
			if (WOApplication.application().isDirectConnectEnabled() && ERXApplication.isWO54()) {
				// AK: 5.4
				key = "_wr_";
			}
			if (request != null) {
				context = (WOContext) request.valueForKey("context");
			}
			String wodata = _NSStringUtilities.concat("wodata", "=", encoded);
			if (context != null) {
				completeURL = context.urlWithRequestHandlerKey(key, null, wodata);
			}
			else {
				StringBuffer stringbuffer = new StringBuffer(request.applicationURLPrefix());
				stringbuffer.append('/');
				stringbuffer.append(key);
				stringbuffer.append('?');
				stringbuffer.append(wodata);
				completeURL = stringbuffer.toString();
			}
			// AK: TODO get rid of regex
			int offset = completeURL.indexOf("?wodata=file%3A");
			if (offset >= 0) {
				completeURL = completeURL.replaceFirst("\\?wodata=file%3A", "/wodata=");
				if (completeURL.indexOf("/wodata=") > 0) {
					completeURL = completeURL.replaceAll("%2F", "/");
				}
			}
		}
		completeURL = _versionManager.versionedUrlForResourceNamed(completeURL, name, bundleName, languages, request);
		return completeURL;
	}

	private WOURLValuedElementData cachedDataForKey(String key) {
		WOURLValuedElementData data = (WOURLValuedElementData) _urlValuedElementsData.objectForKey(key);
		if (data == null && key != null && key.startsWith("file:") && ERXApplication.isDevelopmentModeSafe()) {
			data = cacheDataIfNotInCache(key);
		}
		return data;
	}

	protected WOURLValuedElementData cacheDataIfNotInCache(String key) {
		WOURLValuedElementData data = (WOURLValuedElementData) _urlValuedElementsData.objectForKey(key);
		if (data == null) {
			String contentType = contentTypeForResourceNamed(key);
			data = new WOURLValuedElementData(null, contentType, key);
			if (data != null) {
				_urlValuedElementsData.setObjectForKey(data, key);
			}
		}
		return data;
	}

	public WOURLValuedElementData _cachedDataForKey(String key) {
		WOURLValuedElementData wourlvaluedelementdata = null;
		if (key != null) {
			wourlvaluedelementdata = cachedDataForKey(key);
		}
		return wourlvaluedelementdata;
	}

	/**
	 * IVersionManager provides an interface for adding version numbers to
	 * WebServerResources. This allows you to turn on "infinite" expiration
	 * dates in mod_expires, and instead control reloading by changing the
	 * resource's URL. As an example, you might append a version number as a
	 * query string on the URL (whatever.gif?1).
	 * 
	 * @author mschrag
	 */
	public static interface IVersionManager {
		/**
		 * Returns the variant of the given resource URL adjusted to include
		 * version information.
		 * 
		 * @param resourceUrl
		 *            the original resource URL
		 * @param name
		 *            the name of the resource being loaded
		 * @param bundleName
		 *            the name of the bundle that contains the resource
		 * @param languages
		 *            the languages requested
		 * @param request
		 *            the request
		 * @return a versioned variant of the resourceUrl
		 */
		public String versionedUrlForResourceNamed(String resourceUrl, String name, String bundleName, NSArray languages, WORequest request);
	}

	/**
	 * DefaultVersionManager just returns the resourceUrl unmodified.
	 * 
	 * @author mschrag
	 */
	public static class DefaultVersionManager implements IVersionManager {
		/**
		 * @return resourceUrl
		 */
		public String versionedUrlForResourceNamed(String resourceUrl, String name, String bundleName, NSArray languages, WORequest request) {
			return resourceUrl;
		}
	}

	/**
	 * PropertiesVersionManager provides the ability to control resource version
	 * numbers with Properties settings, and appends the query parameter
	 * "?xxx" to WebServerResource URLs.
	 * 
	 * @property er.extensions.ERXResourceManager.versionManager.default the
	 *           default version to use when an explicit version is not
	 *           specified, defaults to app startup time. Ideally you should set
	 *           this explicitly when you deploy, or multiple instance
	 *           deployments will end up with different version numbers for the
	 *           same resource.
	 * @property er.extensions.ERXResourceManager.versionManager.[bundleName].[resourceName]
	 *           the version to send for the specified resource. If not set
	 *           explicitly, the app default version will be used instead.
	 * @author mschrag
	 */
	public static class PropertiesVersionManager implements IVersionManager {
		private String _defaultVersion;

		public PropertiesVersionManager() {
			String key = "er.extensions.ERXResourceManager.versionManager.default";
			_defaultVersion = ERXProperties.stringForKey(key);
			if (_defaultVersion == null) {
				_defaultVersion = String.valueOf(System.currentTimeMillis());
			}
		}

		public String versionedUrlForResourceNamed(String resourceUrl, String name, String bundleName, NSArray languages, WORequest request) {
			if (bundleName == null) {
				bundleName = "app";
			}
			String key = "er.extensions.ERXResourceManager.versionManager." + bundleName + "." + name;
			String version = ERXProperties.stringForKey(key);
			if (version == null) {
				version = _defaultVersion;
			}
			else if ("none".equals(version) || version.length() == 0) {
				version = null;
			}
			if (version != null) {
				try {
					ERXMutableURL url = new ERXMutableURL(resourceUrl);
					url.addQueryParameter("", version);
					resourceUrl = url.toExternalForm();
				}
				catch (MalformedURLException e) {
					ERXResourceManager.log.error("Failed to construct URL from '" + resourceUrl + "'.", e);
				}
			}
			return resourceUrl;
		}
	}
}
