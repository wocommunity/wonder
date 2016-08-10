package er.extensions.appserver;


import java.io.File;

import com.webobjects._ideservices._WOProject;
import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver._private.WODeployedBundle;
import com.webobjects.appserver._private.WOProjectBundle;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSBundle;
import com.webobjects.foundation.NSLog;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSPathUtilities;
import com.webobjects.foundation.NSProperties;
import com.webobjects.foundation.NSPropertyListSerialization;
import com.webobjects.foundation._NSStringUtilities;

import er.extensions.foundation.ERXProperties;

/**
 * Replacement of the WODeployedBundle which adds:
 * <ul>
 * <li> Determines whether the Bundle is embedded within the Main Bundle 
 * <li> If so, mark the state and the name of embedding main bundle
 * <li> Resource URLs for embedded Frameworks are automatically adapted to refer to the embedded Framework, 
 *      rather than to the Frameworks base URL
 * </ul>
 * 
 * There are two styles of webserver resource packages, with ANT (old style) builds, embedded frameworks are embedded this way
 *    MyApp.woa/Frameworks/EmbeddedFramework.framework/...
 * 
 * while with Maven builds (and some newer ANT builds), frameworks are embedded in the WebServerResources package the same way as in the Application package:
 *    MyApp.woa/Contents/Frameworks/EmbeddedFramework.framework/...
 * 
 * ERXDeployedBundle introduces automatic url generation for embedded frameworks. The new property WOEmbeddedFrameworksPath 
 * helps adjusting to the deployment schemes on embedding:
 * 
 * in the cited cases above, the property should be
 *   WOEmbeddedFrameworksPath=Frameworks (default)
 *   or
 *   WOEmbeddedFrameworksPath=Contents/Frameworks
 * 
 * However if WOFrameworksBaseURL is custom defined, you get the behaviour as before, there is nointervention in url generation.
 * In the case of a mixed deployment (some frameworks globally installed, some embedded), the property WOOverrideEmbeddedFrameworksPath
 * lets activate automatic url generation for embedded frameworks, while globally installad frameworks do get their path from WOFrameworksBaseURL.
 * 
 * @author mstoll
 */
public class ERXDeployedBundle extends WODeployedBundle {

    private final NSMutableDictionary _myURLs;
    private static final NSMutableDictionary TheBundles = new NSMutableDictionary(NSBundle.frameworkBundles().count());
    private static final boolean _allowRapidTurnaround = NSPropertyListSerialization.booleanForString(NSProperties.getProperty("WOAllowRapidTurnaround"));
    private boolean isEmbeddedFramework = false;
    private String embeddingWrapperName = null;
    private static final String defaultFrameworkBaseURL = "/WebObjects/Frameworks";
    
    /**
     * Initializer, determines by comparing bundle paths whether bundle is embedded. 
     * 
     * @param nsb the given NSBundle
     */
    public ERXDeployedBundle(NSBundle nsb)
    {
    	super(nsb);

    	_myURLs = new NSMutableDictionary();
		embeddingWrapperName = NSBundle.mainBundle().name() + ".woa";

    	if(bundlePath().startsWith(NSBundle.mainBundle().bundlePath()))
    	{
    		isEmbeddedFramework = true;
    	}
    }

    @Override
    public String urlForResource(String resourceName, NSArray languagesList) {

        String aRelativePath = relativePathForResource(resourceName, languagesList);
        String aURL = null;
        synchronized(_myURLs)
        {
            aURL = _cachedURL(aRelativePath);
        }
        return aURL;
    }
    
    /**
     *  Get cached URL for relative path within current bundle. If URL not yet in cache,
     *  create one, taking into account whether the current Bundle is embedded within main Bundle
	 * 
     * @param aRelativePath a relative path to a resource within current Bundle
     * @return The URL path to the resource
     */
    private String _cachedURL(String aRelativePath)
    {
        String aURL = null;
        if(aRelativePath != null)
        {
            aURL = (String)_myURLs.objectForKey(aRelativePath);
            if(aURL == null)
            {
                String aBaseURL = null;
                if(isFramework())
                {
                	// WOFrameworksBaseURL is never null but rather by default "/WebObjects/Frameworks"
                	boolean enableAutomaticEmbeddedFrameworkPath = defaultFrameworkBaseURL.equals(WOApplication.application().frameworksBaseURL()) ||
                			ERXProperties.booleanForKeyWithDefault("WOOverrideEmbeddedFrameworksPath", false);
                	if(isEmbeddedFramework && enableAutomaticEmbeddedFrameworkPath)
                	{
            			String embeddedFrameworkPath = ERXProperties.stringForKeyWithDefault("WOEmbeddedFrameworksPath", "Frameworks");
                        aBaseURL = WOApplication.application().applicationBaseURL() + "/" + embeddingWrapperName + "/" + embeddedFrameworkPath;
                	}
                	else
                		aBaseURL = WOApplication.application().frameworksBaseURL();
                }
                else
                    aBaseURL = WOApplication.application().applicationBaseURL();
                String aWrapperName = wrapperName();
                if(aBaseURL != null && aWrapperName != null)
                {
                    aURL = _NSStringUtilities.concat(aBaseURL, File.separator, aWrapperName, File.separator, aRelativePath);
                    aURL = NSPathUtilities._standardizedPath(aURL);
                    _myURLs.setObjectForKey(aURL, aRelativePath);
                }
            }
        }
        return aURL;
    }

    public static synchronized WODeployedBundle bundleWithNSBundle(NSBundle nsBundle)
    {
        Object aBundle = TheBundles.objectForKey(nsBundle);
        if(aBundle == null)
        {
            WODeployedBundle deployedBundle = new ERXDeployedBundle(nsBundle);
            if(_allowRapidTurnaround)
            {
                String bundlePath = nsBundle.bundlePathURL().getPath();
                try
                {
                	if(_WOProject.ideProjectAtPath(bundlePath) != null)
                        aBundle = new WOProjectBundle(bundlePath, deployedBundle);
                    else
                        aBundle = deployedBundle;
                }
                catch(Exception e)
                {
                    if(NSLog.debugLoggingAllowedForLevel(1))
                    {
                        NSLog.debug.appendln((new StringBuilder()).append("<WOProjectBundle>: Warning - Unable to find project at path ").append(nsBundle.bundlePathURL().getPath()).append(" - Ignoring project.").toString());
                        NSLog.debug.appendln(e);
                    }
                    aBundle = deployedBundle;
                }
            } else
            {
                aBundle = deployedBundle;
            }
            TheBundles.setObjectForKey(aBundle, nsBundle);
        }
        return (WODeployedBundle)aBundle;
    }
    
    public static synchronized WODeployedBundle deployedBundleForFrameworkNamed(String aFrameworkName)
    {
        WODeployedBundle aBundle = null;
        NSArray bundleArray = TheBundles.allValues();
        int baCount = TheBundles.count();
        NSBundle nsBundle = NSBundle.bundleForName(aFrameworkName);
        if(nsBundle == null)
            nsBundle = NSBundle.bundleWithPath(aFrameworkName);
        if(nsBundle != null)
        {
            int i = 0;
            do
            {
                if(i >= baCount)
                    break;
                WODeployedBundle aFrameworkBundle = (WODeployedBundle)bundleArray.objectAtIndex(i);
                if(nsBundle.equals(aFrameworkBundle.nsBundle()))
                {
                    aBundle = aFrameworkBundle;
                    WODeployedBundle dBundle = aBundle.projectBundle();
                    if(dBundle != null)
                        aBundle = dBundle;
                    break;
                }
                i++;
            } while(true);
        }
        return aBundle;
    }

	public boolean isEmbedded() {
		return isEmbeddedFramework;
	}

}