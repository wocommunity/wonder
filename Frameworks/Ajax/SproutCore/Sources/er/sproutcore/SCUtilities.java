package er.sproutcore;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSBundle;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.appserver.ERXWOContext;
import er.extensions.foundation.ERXArrayUtilities;
import er.extensions.foundation.ERXFileUtilities;
import er.extensions.foundation.ERXProperties;
import er.extensions.foundation.ERXThreadStorage;

public class SCUtilities {

    protected final static Logger log = Logger.getLogger(SCUtilities.class);

    /**
     * Holds the external URL for the sproutcore libs via the property
     * <code>er.sproutcore.base</code>.
     */
    private static String scBase;

    private static NSMutableDictionary<String, NSMutableArray<String>> deps = new NSMutableDictionary<String, NSMutableArray<String>>();

    private static NSMutableDictionary<String, String> code = new NSMutableDictionary<String, String>();

    /**
     * Returns an external URL for the sproutcore libs to prepend via the
     * property <code>er.sproutcore.base</code> or the empty String. MUST end
     * with "/" if set.
     * 
     * @return
     */
    public static String scBase() {
        if (scBase == null) {
            String version = ERXProperties.stringForKeyWithDefault("er.sproutcore.version", "0.9.13");
            scBase = ERXProperties.stringForKeyWithDefault("er.sproutcore.base", "/Library/Ruby/Gems/1.8/gems/sproutcore-" + version + "/frameworks/sproutcore");
        }
        return scBase;
    }

    public static synchronized NSArray require(String bundleName, String name) {
        String base = (bundleName != null && !"SproutCore".equals(bundleName) ? bundleName : scBase());
        String fullName = bundleName + "/" + name;
        NSMutableDictionary<String, NSMutableArray<String>> deps = (NSMutableDictionary) ERXThreadStorage.valueForKey("SCUtils.deps");
        if(deps == null) {
            deps = new NSMutableDictionary<String, NSMutableArray<String>>();
            ERXThreadStorage.takeValueForKey(deps, "SCUtils.deps");
        }
        NSMutableArray<String> dependencies = deps.objectForKey(fullName);
        if (dependencies == null) {
            dependencies = new NSMutableArray<String>();
            deps.put(fullName, dependencies);
            File file = new File(base, name);
            if("app".equals(base)) {
            	file = new File(NSBundle.mainBundle().bundlePathURL().getFile(), "Contents" + File.separator + "Resources" + File.separator + name);
            }
            try {
                String content = ERXFileUtilities.stringFromFile(file);
                Pattern pattern = Pattern.compile("\\s*require\\(\\s*\\W+([A-Za-z0-9/_]+).?\\s*\\)");
                Matcher matcher = pattern.matcher(content);
                while (matcher.find()) {
                    String otherDep = matcher.group(1) + ".js";
                    NSArray others = require(bundleName, otherDep);
                    dependencies = ERXArrayUtilities.arrayByAddingObjectsFromArrayWithoutDuplicates(dependencies, others).mutableClone();

                    otherDep = bundleName + "/" + otherDep;
                    if (!dependencies.containsObject(otherDep)) {
                        dependencies.add(otherDep);
                    }
                    log.debug("otherDep: " + otherDep);
                }
                if (!dependencies.containsObject(fullName)) {
                    dependencies.add(fullName);
                }
                log.debug("string: " + fullName);
            } catch (IOException e) {
                throw NSForwardException._runtimeExceptionForThrowable(e);
            }
            log.info(name +  "->" + dependencies);
        }
        return dependencies.immutableClone();
    }

    public static void include(String name) {

    }

    public static String staticUrl(String asset) {
    	ERXWOContext context = (ERXWOContext) ERXWOContext.currentContext();
    	String url = context.urlWithRequestHandlerKey(SproutCore.SC_KEY, "SproutCore/english.lproj/" + asset, null);
        return url;
    }
}
