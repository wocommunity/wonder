package er.directtoweb.pages;

import java.io.File;
import java.io.IOException;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSMutableArray;

import er.extensions.appserver.ERXApplication;
import er.extensions.foundation.ERXFileUtilities;
import er.extensions.foundation.ERXRuntimeUtilities;
import er.extensions.foundation.ERXRuntimeUtilities.Result;
import er.extensions.foundation.ERXRuntimeUtilities.TimeoutException;

/**
 * Creates a GrahpViz page for those that needs such trivial tools. Call up with
 * the ERD2WDirectAction and use visibleEntityNames to restrict based on page
 * config name.
 * 
 * @author ak
 * 
 */
public class ERD2WGraphVizPage extends ERD2WPage {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    /** interface for all the keys used in this pages code */
    public interface Keys extends ERD2WPage.Keys {
	    public static String format = "format";
	}
	   
    private int _nodeID = 0;

    public ERD2WGraphVizPage(WOContext context) {
        super(context);
    }
    
    public int nodeID() {
        return _nodeID ++;
    }
    
    public NSArray<String> attributes() {
        NSMutableArray<String> result = new NSMutableArray<String>();
        EOEntity entity = entity();
        for (String key : (NSArray<String>)displayPropertyKeys()) {
            if(entity.classDescriptionForInstances().attributeKeys().containsObject(key) && entity.classPropertyNames().containsObject(key)) {
                result.addObject(key);
            }
        }
        return result;
    }
    
    public NSArray<String> toOneRelationships() {
        NSMutableArray<String> result = new NSMutableArray<String>();
        EOEntity entity = entity();
        for (String key : (NSArray<String>)displayPropertyKeys()) {
            if(entity.classDescriptionForInstances().toOneRelationshipKeys().containsObject(key) && entity.classPropertyNames().containsObject(key)) {
                result.addObject(key);
            }
        }
        return result;
    }
    
    public String tag() {
        return "<" + propertyKey() + ">";
    }
    
    public String nodeType() {
        return "->";
    }
    
    public NSArray<String> toManyRelationships() {
        NSMutableArray<String> result = new NSMutableArray<String>();
        EOEntity entity = entity();
        for (String key : (NSArray<String>)displayPropertyKeys()) {
            if(entity.classDescriptionForInstances().toManyRelationshipKeys().containsObject(key) && entity.classPropertyNames().containsObject(key)) {
                result.addObject(key);
            }
        }
        return result;
    }
    
    /* 
     * Disable click to open
     */
    public boolean clickToOpenEnabled(WOResponse response, WOContext context) {
        return false;
    }
    
    @Override
    public void appendToResponse(WOResponse response, WOContext context) {
        // we do NOT want to expose our model to just everyone...
        if (ERXApplication.isDevelopmentModeSafe()) {
            super.appendToResponse(response, context);
        }
        response.setHeader("text/plain", "content-type");
        String format = (String) d2wContext().valueForKey(Keys.format);
        if (format == null) {
            format = context.request().stringFormValueForKey("format");
        }
        if (format != null) {
            String dot = response.contentString();
            File f = null;
            try {
                f = File.createTempFile("GVTemp", "dot");
                ERXFileUtilities.stringToFile(dot, f);
                Result result = ERXRuntimeUtilities.execute(new String[] { "/usr/local/bin/dot", "-T" + format, "", f.getAbsolutePath() }, null, null, 0);
                response.setContent(new NSData(result.getResponse()));
                if (format.equals("svg")) {
                    response.setHeader("image/svg+xml", "content-type");
                } else if (format.equals("pdf")) {
                    response.setHeader("application/pdf", "content-type");
                } else {
                    throw new IllegalArgumentException("Only handles 'pdf' and 'svg'");
                }
            } catch (IOException ex) {
                log.error(ex, ex);
            } catch (TimeoutException ex) {
                log.error(ex, ex);
            } finally {
                if(f != null) {
                    f.delete();
                }
            }
        }
    }
}