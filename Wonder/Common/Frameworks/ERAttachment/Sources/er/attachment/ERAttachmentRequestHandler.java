package er.attachment;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.NoSuchElementException;

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

import er.attachment.model.ERAttachment;
import er.attachment.processors.ERAttachmentProcessor;
import er.extensions.ERXEC;
import er.extensions.ERXFileUtilities;

/**
 * ERAttachmentRequestHandler is the request handler that is used for loading 
 * any proxied attachment.  To control security, you can set the delegate of this 
 * request handler in your application constructor.  By default, all proxied 
 * attachments are visible.
 * 
 * @author mschrag
 */
public class ERAttachmentRequestHandler extends WORequestHandler {
  public static final String REQUEST_HANDLER_KEY = "attachments";
  public static final Logger log = Logger.getLogger(ERAttachmentRequestHandler.class);

  /**
   * The delegate definition for this request handler.
   */
  public static interface Delegate {
    /**
     * Called prior to displaying a proxied attachment to a user and can be used to implement
     * security on top of attachments.
     * 
     * @param attachment the attachment that was requested
     * @param request the current request
     * @param context the current context
     * @return true if the current user is allowed to view this attachment
     */
    public boolean attachmentVisible(ERAttachment attachment, WORequest request, WOContext context);
  }

  private ERAttachmentRequestHandler.Delegate _delegate;

  /**
   * Sets the delegate for this request handler.
   * 
   * @param delegate the delegate for this request handler
   */
  public void setDelegate(ERAttachmentRequestHandler.Delegate delegate) {
    _delegate = delegate;
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
            ERAttachment attachment = ERAttachment.fetchRequiredAttachmentWithWebPath(editingContext, webPath);
            if (_delegate != null && !_delegate.attachmentVisible(attachment, request, context)) {
              throw new SecurityException("You are not allowed to view the requested attachment.");
            }
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

          response.setStatus(200);
          response.setContentStream(attachmentInputStream, bufferSize, (int) length);
        }
        catch (SecurityException e) {
          NSLog.out.appendln(e);
          response.setContent(e.getMessage());
          response.setStatus(403);
        }
        catch (NoSuchElementException e) {
          NSLog.out.appendln(e);
          response.setContent(e.getMessage());
          response.setStatus(404);
        }
        catch (FileNotFoundException e) {
          NSLog.out.appendln(e);
          response.setContent(e.getMessage());
          response.setStatus(404);
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
}