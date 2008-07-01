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
            File file = new File(SCUtilities.scBase(), name);
            byte data[];
            try {
                data = ERXFileUtilities.bytesFromFile(file);
            } catch (IOException e) {
                throw NSForwardException._runtimeExceptionForThrowable(e);
            }
            result.setContent(new NSData(data));

        } else {
            InputStream is = NSBundle.bundleForName(bundleName).inputStreamForResourcePath(name);
            result.setContentStream(is, 4000, 4000);
        }
        return result;
    }

}
