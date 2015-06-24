package er.modern.movies.test;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WORequestHandler;
import com.webobjects.appserver.WOResourceManager;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WOResourceRequestHandler;
import com.webobjects.appserver._private.WOShared;
import com.webobjects.appserver._private.WOURLValuedElementData;
import com.webobjects.foundation.NSBundle;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSLog;
import com.webobjects.foundation.NSNotificationCenter;
import com.webobjects.foundation.NSPathUtilities;

import er.extensions.foundation.ERXDictionaryUtilities;
import er.extensions.foundation.ERXProperties;

/** 
 * @author <a href="mailto:hprange@gmail.com.br">Henrique Prange</a>
 */
public class JarResourceRequestHandler extends WOResourceRequestHandler {
    
    private static final Logger LOGGER = Logger.getLogger(JarResourceRequestHandler.class);

    private final WOApplication _app;
    private String _documentRoot;

    public JarResourceRequestHandler() {
        _app = WOApplication.application();
        _documentRoot = null;
    }

    @SuppressWarnings("all")
    protected WOResponse _generateResponseForInputStream(InputStream is, long length, String type) {
        WOResponse response = _app.createResponseInContext(null);
        if (is != null) {
            if (length != 0) {
                response.setContentStream(is, 50 * 1024, length);
            }
        } else {
            response.setStatus(404);
        }
        if (type != null) {
            response.setHeader(type, "content-type");
        }
        if (length != 0) {
            response.setHeader("" + length, "content-length");
        }
        return response;
    }

    private String documentRoot() {
        if (_documentRoot == null) {
            _documentRoot = ERXProperties.stringForKey("WODocumentRoot");
            if (_documentRoot == null) {
                NSBundle bundle = NSBundle.bundleForName("JavaWebObjects");
                NSDictionary dict = ERXDictionaryUtilities.dictionaryFromPropertyList("WebServerConfig", bundle);
                _documentRoot = (String) dict.objectForKey("DocumentRoot");
            }
        }
        return _documentRoot;
    }

    protected WOResponse generateResponseForInputStream(InputStream is, long aContentLength, String aContentType) {
        WOResponse aResponse = _app.createResponseInContext(null);

        if (aContentType != null) {
            aResponse.setHeader(aContentType, "content-type");
        }

        if (is != null && aContentLength != 0L) {
            aResponse.setHeader(Long.toString(aContentLength), "content-length");
            aResponse.setContentStream(is, 131072, aContentLength);
        } else {
            LOGGER.warn("The resource was not found. Turn log DEBUG on for more details.");

            aResponse.setStatus(404);
            aResponse.setHeader(WOShared.unsignedIntString(0), "content-length");
        }

        return aResponse;
    }

    @Override
    public WOResponse handleRequest(WORequest request) {
        WOResponse response = null;
        FileInputStream is = null;
        int length = 0;
        String contentType = null;
        String uri = request.uri();
        if (uri.charAt(0) == '/') {
            WOResourceManager rm = _app.resourceManager();
            String documentRoot = documentRoot();
            File file = null;
            StringBuffer sb = new StringBuffer(documentRoot.length() + uri.length());
            String wodataKey = request.stringFormValueForKey("wodata");
            if (uri.startsWith("/cgi-bin") && wodataKey != null) {
                uri = wodataKey;
                if (uri.startsWith("file:")) {
                    // remove file:/
                    uri = uri.substring(5);
                }
            } else {
                int index = uri.indexOf("/wodata=");

                if (index >= 0) {
                    uri = uri.substring(index + "/wodata=".length());
                } else {
                    sb.append(documentRoot);
                }
            }
            sb.append(uri);
            String path = sb.toString();
            try {
                path = path.replace('+', ' ');
                path = path.replaceAll("\\?.*", "");
                file = new File(path);
                length = (int) file.length();
                is = new FileInputStream(file);

                contentType = rm.contentTypeForResourceNamed(path);
                LOGGER.debug("Reading file '" + file + "' for uri: " + uri);
            } catch (IOException ex) {
                if (!uri.toLowerCase().endsWith("/favicon.ico")) {
                    LOGGER.debug("Unable to get contents of file '" + file + "' for uri: " + uri);

                    return handleRequestWithResourceInsideJar(request);
                }
            }
        } else {
            LOGGER.error("Can't fetch relative path: " + uri);
        }
        response = _generateResponseForInputStream(is, length, contentType);
        NSNotificationCenter.defaultCenter().postNotification(WORequestHandler.DidHandleRequestNotification, response);
        response._finalizeInContext(null);
        return response;
    }

    public WOResponse handleRequestWithResourceInsideJar(WORequest request) {
        LOGGER.debug("Handling the request for (entire URI) " + request.uri());

        boolean requestHandlerContainsPath = false;

        String requestHandlerPath = request.requestHandlerPath();
        String resourceDataKey = request.stringFormValueForKey("wodata");

        if (requestHandlerPath != null
                && (requestHandlerPath.endsWith(".class") || requestHandlerPath.endsWith(".jar") || requestHandlerPath.endsWith(".zip") || requestHandlerPath
                        .endsWith(".table")) && requestHandlerPath.indexOf("..") == requestHandlerPath.indexOf('~')) {
            requestHandlerContainsPath = true;
        }

        WOResponse response = null;

        if (requestHandlerContainsPath && _app.isDirectConnectEnabled()) {
            LOGGER.debug("The path to resources is (based on request handler path) " + requestHandlerPath);

            response = responseForJavaClassAtPath(requestHandlerPath);
        } else if (StringUtils.isNotBlank(requestHandlerPath) && resourceDataKey == null) {
            // A classe ERXResourceManager altera a URL de forma errada. Ao
            // invés de ?wodata=, o Anjo mudou para /wodata=. Isso faz com
            // que seja necessário tratarmos aqui essa key.
            requestHandlerPath = StringUtils.replace(requestHandlerPath, "wodata=", "");

            // Tosca modificacao para carregar recursos no WO541 e Wonder 4 em Windows
            requestHandlerPath = StringUtils.replace(requestHandlerPath, "%3A", ":");

            LOGGER.debug("The path to resources is (based on a corrected path) " + requestHandlerPath);

            URL resourcesUrl = null;

            try {
                resourcesUrl = new URL("file", "", requestHandlerPath);

                response = responseForDataAtURL(resourcesUrl);
            } catch (MalformedURLException exception) {
                LOGGER.error("An error occurred while trying to handle the resource", exception);
            }
        } else if (resourceDataKey != null) {
            LOGGER.debug("The path to resources is (based on wodata key) " + resourceDataKey);

            response = responseForDataCachedWithKey(resourceDataKey);
        }

        if (response == null) {
            LOGGER.warn("THE REQUEST CANNOT BE CORRECTLY HANDLED. GENERATING AN EMPTY RESPONSE.");

            String contentType = request.headerForKey("content-type");

            if (contentType == null) {
                contentType = "text/plain";
            }

            response = generateResponseForInputStream(null, 0L, contentType);
        }

        NSNotificationCenter.defaultCenter().postNotification(WORequestHandler.DidHandleRequestNotification, response);
        response._finalizeInContext(null);

        return response;
    }

    protected WOResponse responseForDataAtURL(URL anURL) {
        InputStream is = null;

        long fileLength = 0L;

        String aResourcePath = anURL.toString();

        String aContentType = _app.resourceManager().contentTypeForResourceNamed(aResourcePath);

        try {
            fileLength = NSPathUtilities._contentLengthForPathURL(anURL);

            is = anURL.openStream();
        } catch (IOException ioe) {
            NSLog.err.appendln((new StringBuilder()).append("<").append(getClass().getName()).append("> Unable to get contents of file for path '").append(
                    aResourcePath).append("': ").append(ioe).toString());

            if (NSLog.debugLoggingAllowedForLevelAndGroups(2, 36L)) {
                NSLog.debug.appendln(ioe);
            }
        }

        WOResponse aResponse = generateResponseForInputStream(is, fileLength, aContentType);

        return aResponse;
    }

    protected WOResponse responseForDataCachedWithKey(String aResourceKey) {
        WOResponse response = _app.createResponseInContext(null);

        WOResourceManager resourceManager = _app.resourceManager();

        WOURLValuedElementData aResourceDataObject = resourceManager._cachedDataForKey(aResourceKey);

        if (aResourceDataObject == null) {
            LOGGER.warn("The resource was not found in cache. Turn log DEBUG on for more details.");

            return response;
        }

        aResourceDataObject.appendToResponse(response, null);

        if (aResourceDataObject.isTemporary()) {
            resourceManager.removeDataForKey(aResourceKey, null);
        }

        return response;
    }

    protected WOResponse responseForJavaClassAtPath(String aPath) {
        WOResponse aResponse = null;

        URL anURL = _app.resourceManager()._pathURLForJavaClass(aPath);

        if (anURL != null) {
            aResponse = responseForDataAtURL(anURL);
        }

        return aResponse;
    }
}