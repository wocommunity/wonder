package er.sproutcore;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WORequestHandler;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSBundle;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSForwardException;

import er.extensions.foundation.ERXArrayUtilities;
import er.extensions.foundation.ERXFileUtilities;

public class SCRequestHandler extends WORequestHandler {
    private static final Logger log = Logger.getLogger(SCRequestHandler.class);

    @Override
    public WOResponse handleRequest(WORequest request) {
        WOResponse result = new WOResponse();
        NSArray path = request.requestHandlerPathArray();
        String bundleName = (String) path.objectAtIndex(0);
        String name = ERXArrayUtilities.arrayByRemovingFirstObject(path).componentsJoinedByString("/");
        if ("SproutCore".equals(bundleName)) {
            name = name.replaceAll("\\.\\.+", "");
            if("prototype/prototype.js".equals(name)) {
                name = "../" + name;
            }
            File file = new File(SCUtilities.scBase(), name);
            byte data[];
            try {
                data = ERXFileUtilities.bytesFromFile(file);
                if(name.endsWith(".css")) {
                	String code = new String(data);
                	code = code.replaceAll("static_url\\([\"\'](.*?)[\"\']\\)", "url($1)");
                	data = code.getBytes();
                } else if(name.endsWith(".js")) {
                	String code = new String(data);
                	code = code.replaceAll("static_url\\([\"\'](.*?)[\"\']\\)", "'/cgi-bin/WebObjects/Foo.woa/__sc__/SproutCore/english.lproj/$1" + ".gif'");
                	data = code.getBytes();
                }
            } catch (IOException e) {
                throw NSForwardException._runtimeExceptionForThrowable(e);
            }
            result.setContent(new NSData(data));

        } else {
        	NSBundle bundle = NSBundle.bundleForName(bundleName);
        	if("app".equals(bundleName)) {
        		bundle = NSBundle.mainBundle();
        	}
            
        	try {
        		InputStream is = bundle.pathURLForResourcePath(name).openStream();
        		byte data[] = ERXFileUtilities.bytesFromInputStream(is);
        		if(name.endsWith(".css")) {
        			String code = new String(data);
        			code = code.replaceAll("static_url\\([\"\'](.*?)[\"\']\\)", "url($1)");
        			data = code.getBytes();
                } else if(name.endsWith(".js")) {
                	String code = new String(data);
                	code = code.replaceAll("static_url\\([\"\'](.*?)[\"\']\\)", "'/cgi-bin/WebObjects/Foo.woa/__sc__/app/english.lproj/$1" + ".gif'");
                	data = code.getBytes();
         		}
        		result.setContent(new NSData(data));
        	} catch (IOException e) {
        		throw NSForwardException._runtimeExceptionForThrowable(e);
			}
        }
        return result;
    }

}
