package er.sproutcore;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.foundation.ERXFileUtilities;
import er.extensions.foundation.ERXProperties;

public class SCUtilities {

    protected final static Logger log = Logger.getLogger(SCUtilities.class);

    /**
     * Holds the external URL for the sproutcore libs via the property <code>er.sproutcore.base</code>.
     */
    private static String scBase;

    private static NSMutableDictionary<String, Set<String>> deps = new NSMutableDictionary<String, Set<String>>();
    private static NSMutableDictionary<String, String> code = new NSMutableDictionary<String, String>();
    
    /**
     * Returns an external URL for the sproutcore libs to prepend via the property <code>er.sproutcore.base</code> or the empty String. MUST end with "/" if set.
     * @return
     */
    public static String scBase() {
        if(scBase == null) {
            String version = ERXProperties.stringForKeyWithDefault("er.sproutcore.version", "0.9.10");
            scBase = ERXProperties.stringForKeyWithDefault("er.sproutcore.base","/Library/Ruby/Gems/1.8/gems/sproutcore-" + version + "/frameworks/sproutcore");
        }
        return scBase;
    }
    
    public static synchronized String[] require(String bundle, String name) {
        String base = (bundle != null && !"SproutCore".equals(bundle) ? bundle : scBase());
        String fullName =  bundle + "/" + name;
        Set<String> d = deps.objectForKey(fullName);
        if(d == null || true) {
            d = new TreeSet<String>();
            deps.put(fullName, d);
            File file = new File(base, name);
            try {
                String content = ERXFileUtilities.stringFromFile(file);
                Pattern pattern = Pattern.compile("\\s*require\\(\\s*\\W+([A-Za-z0-9/]+).?\\s*\\)");
                Matcher matcher = pattern.matcher(content);
                while(matcher.find()) {
                    String otherDep = matcher.group(1) + ".js";
                    String others[] = require(bundle, otherDep);
                    for (int i = 0; i < others.length; i++) {
                        String string = others[i];
                        d.add(string);
                        log.info("string: " + string);
                    }
                    otherDep = bundle + "/" + otherDep;
                    d.add(otherDep);
                    log.info("otherDep: " + otherDep);
                }
                d.add(fullName);
                log.info("string: " + fullName);
            } catch (IOException e) {
                throw NSForwardException._runtimeExceptionForThrowable(e);
            }
        }
        // debugging
        deps.remove(name);
        return (String[]) d.toArray(new String[0]);
    }
    
    public static void include(String name) {
        
    }
    
    public static String staticUrl(String asset) {
        return "blank";
    }
}
