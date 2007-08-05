package er.attachment.components;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WORequestHandler;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver.WOSession;
import com.webobjects.appserver._private.WODynamicURL;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSLog;

import er.ajax.AjaxFileUploadRequestHandler;
import er.attachment.model.ERAttachment;
import er.attachment.processors.ERAttachmentProcessor;
import er.extensions.ERXEC;
import er.extensions.ERXFileUtilities;

public class ERAttachmentRequestHandler extends WORequestHandler {
  public static final String REQUEST_HANDLER_KEY = "attachments";
  public static final Logger log = Logger.getLogger(AjaxFileUploadRequestHandler.class);

  public ERAttachmentRequestHandler() {
  }

  @Override
  public WOResponse handleRequest(WORequest request) {
    int bufferSize = 16384;

    WOApplication application = WOApplication.application();
    application.awake();
    try {
      WOContext context = application.createContextForRequest(request);
      WOResponse response = application.createResponseInContext(context);

      String wosid = (String) request.formValueForKey("wosid");
      if (wosid == null) {
        wosid = request.cookieValueForKey("wosid");
      }
      context._setRequestSessionID(wosid);
      WOSession session = null;
      if (context._requestSessionID() != null) {
        session = WOApplication.application().restoreSessionWithID(wosid, context);
      }
      try {
        WODynamicURL url = request._uriDecomposed();
        // MS: This is kind of goofy because we lookup by path, your web path needs to 
        // have a leading slash on it.
        String webPath = "/" + url.requestHandlerPath();

        try {
          InputStream attachmentInputStream;
          String mimeType;
          long length;

          EOEditingContext editingContext = ERXEC.newEditingContext();
          editingContext.lock();

          try {
            ERAttachment attachment = ERAttachment.fetchAttachmentWithWebPath(editingContext, webPath);
            mimeType = attachment.mimeType();
            length = attachment.size().longValue();
            InputStream rawAttachmentInputStream = ERAttachmentProcessor.processorForType(attachment).attachmentInputStream(attachment);
            attachmentInputStream = new BufferedInputStream(rawAttachmentInputStream, bufferSize);
          }
          finally {
            editingContext.unlock();
          }
          response.setHeader(mimeType, "Content-Type");
          response.setHeader(String.valueOf(length), "Content-Length");
          
          String queryString = url.queryString();
          if (queryString != null && queryString.contains("attachment=true")) {
            response.setHeader("attachment; filename=" + ERXFileUtilities.fileNameFromBrowserSubmittedPath(webPath), "Content-Disposition");
          }

          response.setContentStream(attachmentInputStream, bufferSize, (int) length);
        }
        catch (IOException e) {
          NSLog.out.appendln(e);
          response.setContent(e.getMessage());
          response.setStatus(500);
        }

        return response;
      }
      finally {
        if (context._requestSessionID() != null) {
          WOApplication.application().saveSessionForContext(context);
        }
      }
    }
    finally {
      application.sleep();
    }
  }
  
  private static boolean _requestHandlerRegistered;
  public static void ensureRequestHandlerRegistered() {
    if (!_requestHandlerRegistered) {
      synchronized (ERAttachmentViewer.class) {
        if (!_requestHandlerRegistered) {
          if (WOApplication.application().requestHandlerForKey(ERAttachmentRequestHandler.REQUEST_HANDLER_KEY) == null) {
            WOApplication.application().registerRequestHandler(new ERAttachmentRequestHandler(), ERAttachmentRequestHandler.REQUEST_HANDLER_KEY);
          }
          _requestHandlerRegistered = true;
        }
      }
    }
  }
}