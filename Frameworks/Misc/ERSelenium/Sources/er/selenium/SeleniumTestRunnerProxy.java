/*
 * Copyright (c) 2007 Design Maximum - http://www.designmaximum.com
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * 
 */

package er.selenium;

import java.net.URL;
import java.util.Iterator;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WORequestHandler;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSNotificationCenter;

import er.extensions.appserver.ERXHttpStatusCodes;
import er.extensions.appserver.ERXResponse;
import er.extensions.foundation.ERXFileUtilities;

/**
 * Request hanlder for selenium tests. Returns the files to get parsed by the runner.
 */
public class SeleniumTestRunnerProxy extends WORequestHandler {
	private static final Logger log = Logger.getLogger(SeleniumTestRunnerProxy.class);
	
	protected class CachedFile {
		public NSData data;
		public String mimeType;
	}
	protected NSMutableDictionary _cache = new NSMutableDictionary();
	
    public SeleniumTestRunnerProxy() {
        super();
    }

    @Override
    public WOResponse handleRequest(WORequest request) {
        if(!ERSelenium.testsEnabled()) {
            return new ERXResponse(ERXHttpStatusCodes.STATUS_FORBIDDEN);
        }
 
    	NSArray pathElements = request.requestHandlerPathArray();
    	
    	StringBuilder builder = new StringBuilder();
    	Iterator iter = pathElements.iterator();
    	while (iter.hasNext()) {
    		builder.append(iter.next());
    		if (iter.hasNext())
    			builder.append('/');
    	}
    	
		String filePath = builder.toString();
		log.debug("Processing file '" + filePath + "'");
		
		/*
		 * Synchronization mistakes are possible here, but not fatal at all.
		 * At the worst case the file will be read 2-or-more times instead of 1 (if process 1
		 * checks that the file is not cached and process 2 does the same check before
		 * process 1 has updated the cache).
		 */
	
		CachedFile cachedFile;
		synchronized (_cache) {
			cachedFile = (CachedFile)_cache.objectForKey(filePath);
    	}
		
		if (cachedFile == null) {
			cachedFile = new CachedFile();

			URL fileUrl = WOApplication.application().resourceManager().pathURLForResourceNamed(filePath, "ERSelenium", null);
			if (fileUrl == null) {
				throw new RuntimeException("Can't find specified resource ('" + filePath + "')");
			}
			cachedFile.mimeType = WOApplication.application().resourceManager().contentTypeForResourceNamed(filePath);
			if (cachedFile.mimeType == null) {
				throw new RuntimeException("Can't determine resource mime type ('" + filePath + "')");
			}
			
			try {
				cachedFile.data = new NSData(ERXFileUtilities.bytesFromInputStream(fileUrl.openStream()));
			} catch (Exception e) {
				throw new RuntimeException("Error reading file '" + fileUrl.getPath() + "'", e);
			}
			
			synchronized (_cache) {
				_cache.setObjectForKey(cachedFile, filePath);
	    	}
		}
    	
		ERXResponse response = new ERXResponse();
		response.setHeader(cachedFile.mimeType, "content-type");
		response.setContent(cachedFile.data);
		
		NSNotificationCenter.defaultCenter().postNotification(WORequestHandler.DidHandleRequestNotification, response);
    	return response;
    }
}