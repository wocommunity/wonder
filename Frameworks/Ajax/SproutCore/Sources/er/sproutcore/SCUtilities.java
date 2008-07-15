package er.sproutcore;

import java.io.File;
import java.io.FileFilter;
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
import er.extensions.foundation.ERXStringUtilities;
import er.extensions.foundation.ERXThreadStorage;

public class SCUtilities {

    protected final static Logger log = Logger.getLogger(SCUtilities.class);

    /**
     * Holds the external URL for the sproutcore libs via the property
     * <code>er.sproutcore.base</code>.
     */
    private static String scBase;

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
            scBase = ERXProperties.stringForKeyWithDefault("er.sproutcore.base", "/Library/Ruby/Gems/1.8/gems/sproutcore-" + version + "/frameworks");
        }
        return scBase;
    }

    public static String bundleResourcePath(String bundleName) {
        NSBundle bundle = "app".equals(bundleName) ? NSBundle.mainBundle() : NSBundle.bundleForName(bundleName);
        String basePath = "SproutCore".equals(bundleName) ? scBase() : bundle.resourcePath();
        return basePath;
    }

    public static synchronized NSArray<String> requireAll(String bundleName, String groupName) {
        String basePath = bundleResourcePath(bundleName);
        File baseDir = new File(basePath, groupName);
        if (!baseDir.exists()) {
          throw new IllegalArgumentException("The folder '" + baseDir + "' does not exist.");
        }
        File[] files = ERXFileUtilities.listFiles(baseDir, true, new FileFilter() {

            public boolean accept(File f) {
                return f.getName().endsWith(".js") || f.isDirectory();
            }
        });
        NSMutableArray<String> dependencies = new NSMutableArray<String>(files.length);
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            String name = file.getAbsolutePath().substring(baseDir.getAbsolutePath().length() + 1);
            if(!dependencies.containsObject(name) && name.endsWith(".js")) {
                NSArray<String> fileDependencies = require(bundleName, groupName, name);
                for (String depencency : fileDependencies) {
                    if(!dependencies.containsObject(depencency)) {
                        dependencies.addObject(depencency);
                    }
                }
            }
        }
        return dependencies;
    }
    
    @SuppressWarnings("unchecked")
    public static synchronized NSArray<String> require(String bundleName, String groupName, String name) {
        String basePath = bundleResourcePath(bundleName);
        String fullName = bundleName + "/" + groupName + "/" + name;
        NSMutableDictionary<String, NSMutableArray<String>> deps = (NSMutableDictionary) ERXThreadStorage.valueForKey("SCUtils.deps");
        if(deps == null) {
            deps = new NSMutableDictionary<String, NSMutableArray<String>>();
            ERXThreadStorage.takeValueForKey(deps, "SCUtils.deps");
        }
        NSMutableArray<String> dependencies = deps.objectForKey(fullName);
        if (dependencies == null) {
            dependencies = new NSMutableArray<String>();
            deps.put(fullName, dependencies);
            File file = new File(basePath, groupName + "/" + name);
            if (!file.exists()) {
              throw new IllegalArgumentException("The file '" + file + "' does not exist.");
            }
            try {
                String content = ERXFileUtilities.stringFromFile(file);
                Pattern pattern = Pattern.compile("\\s*require\\(\\s*\\W+([A-Za-z0-9/_]+).?\\s*\\)");
                Matcher matcher = pattern.matcher(content);
                while (matcher.find()) {
                    String otherDep = matcher.group(1) + ".js";
                    NSArray others = require(bundleName, groupName, otherDep);
                    dependencies = ERXArrayUtilities.arrayByAddingObjectsFromArrayWithoutDuplicates(dependencies, others).mutableClone();

                    otherDep = bundleName + "/" + groupName + "/" + otherDep;
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
            log.debug(name +  "->" + dependencies);
        }
        return dependencies.immutableClone();
    }

    public static void include(String name) {

    }

    public static String staticUrl(String asset) {
    	ERXWOContext context = (ERXWOContext) ERXWOContext.currentContext();
    	String url = context.urlWithRequestHandlerKey(SproutCore.SC_KEY, "SproutCore/sproutcore/english.lproj/" + asset, null);
        return url;
    }

	public static String defaultCssName(Class clazz) {
        // this is just the default... it morphs SCFooBar -> sc-foo-bar
		String className = ERXStringUtilities.lastPropertyKeyInKeyPath(clazz.getName());
        Pattern p = Pattern.compile("^([A-Z]+?)([A-Z])");
        Matcher m = p.matcher(className);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            m.appendReplacement(sb, String.valueOf(m.group(0)).toLowerCase() + "-");
        }
        m.appendTail(sb);
        className = sb.toString();

        p = Pattern.compile("([A-Z][a-z0-9]+)");
        m = p.matcher(className);
        sb = new StringBuffer();
        while (m.find()) {
            m.appendReplacement(sb, String.valueOf(m.group()).toLowerCase() + "-");
        }
        m.appendTail(sb);
        className = sb.toString();
        className = className.substring(0, className.length() - 1);
		return className;
	}

}
