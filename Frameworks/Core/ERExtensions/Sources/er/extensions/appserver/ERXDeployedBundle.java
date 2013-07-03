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

public class ERXDeployedBundle extends WODeployedBundle {

    private final NSMutableDictionary _myURLs;
    private static final NSMutableDictionary TheBundles = new NSMutableDictionary(NSBundle.frameworkBundles().count());
    private static final boolean _allowRapidTurnaround = NSPropertyListSerialization.booleanForString(NSProperties.getProperty("WOAllowRapidTurnaround"));
    private boolean isEmbeddedFramework = false;
    private String embeddingWrapperName = null;
    
    public ERXDeployedBundle(NSBundle nsb)
    {
    	super(nsb);

    	_myURLs = new NSMutableDictionary();

    	if(bundlePath().startsWith(NSBundle.mainBundle().bundlePath()))
    	{
    		isEmbeddedFramework = true;
    		embeddingWrapperName = NSBundle.mainBundle().name() + ".woa";
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
                	if(isEmbeddedFramework)
                        aBaseURL = WOApplication.application().applicationBaseURL() + "/" + embeddingWrapperName + "/Frameworks";
                	else
                    aBaseURL = WOApplication.application().frameworksBaseURL();
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
