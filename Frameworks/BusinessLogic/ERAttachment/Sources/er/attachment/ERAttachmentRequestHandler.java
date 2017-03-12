package er.attachment;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODynamicURL;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WORequestHandler;
import com.webobjects.appserver.WOResponse;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOGlobalID;
import com.webobjects.eocontrol.EOKeyGlobalID;
import com.webobjects.foundation.NSLog;

import er.attachment.model.ERAttachment;
import er.attachment.processors.ERAttachmentProcessor;
import er.extensions.eof.ERXEC;
import er.extensions.eof.ERXEOGlobalIDUtilities;
import er.extensions.foundation.ERXStringUtilities;

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

      String sessionIdKey = application.sessionIdKey();
      String sessionId = (String) request.formValueForKey(sessionIdKey);
      if (sessionId == null) {
        sessionId = request.cookieValueForKey(sessionIdKey);
      }
      context._setRequestSessionID(sessionId);
      if (context._requestSessionID() != null) {
        application.restoreSessionWithID(sessionId, context);
      }

      try {
    	final WODynamicURL url = request._uriDecomposed();
        final String requestPath = url.requestHandlerPath();
        final Matcher idMatcher = Pattern.compile("^id/(\\d+)/").matcher(requestPath);

        final Integer requestedAttachmentID;
        String requestedWebPath;

        final boolean requestedPathContainsAnAttachmentID = idMatcher.find();
		if (requestedPathContainsAnAttachmentID) {
          requestedAttachmentID = Integer.valueOf(idMatcher.group(1));
          requestedWebPath = idMatcher.replaceFirst("/");
        } else {
          // MS: This is kind of goofy because we lookup by path, your web path needs to 
          // have a leading slash on it.
          requestedWebPath = "/" + requestPath;
          requestedAttachmentID = null;
        }


        try {
          InputStream attachmentInputStream;
          String mimeType;
          String fileName;
          long length;
          String queryString = url.queryString();
          boolean proxyAsAttachment = (queryString != null && queryString.contains("attachment=true"));

          EOEditingContext editingContext = ERXEC.newEditingContext();
          editingContext.lock();

          try {
            ERAttachment attachment = fetchAttachmentFor(editingContext, requestedAttachmentID, requestedWebPath);
            
            if (_delegate != null && !_delegate.attachmentVisible(attachment, request, context)) {
              throw new SecurityException("You are not allowed to view the requested attachment.");
            }
            mimeType = attachment.mimeType();
            length = attachment.size().longValue();
            fileName = attachment.originalFileName();
            ERAttachmentProcessor<ERAttachment> attachmentProcessor = ERAttachmentProcessor.processorForType(attachment);
            if (!proxyAsAttachment) { 
              proxyAsAttachment = attachmentProcessor.proxyAsAttachment(attachment);
            }
            InputStream rawAttachmentInputStream = attachmentProcessor.attachmentInputStream(attachment);
            attachmentInputStream = new BufferedInputStream(rawAttachmentInputStream, bufferSize);
          } finally {
            editingContext.unlock();
          }
          
          response.setHeader(mimeType, "Content-Type");
          response.setHeader(String.valueOf(length), "Content-Length");

          if (proxyAsAttachment) {
            response.setHeader("attachment; filename=\"" + fileName + "\"", "Content-Disposition");
          }

          response.setStatus(200);
          response.setContentStream(attachmentInputStream, bufferSize, length);
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


	/**
	 * 
	 * 
	 * @param editingContext
	 *        the {@link EOEditingContext} that the result will be inserted into
	 * @param attachmentPrimaryKey
	 *        the primaryKey value of an existing ERAttachment in the database
	 * @param requestedWebPath
	 *        a URL-encoded portion of the requested ERAttachment path including the file name of the attachment
	 * @return an attachment that matches either both the {@code attachmentPrimaryKey} and the {@code requestedWebPath},
	 *         or just the {@code reqestedWebPath}. If it is null then we throw a SecurityException. 
	 * 
	 * @author davendasora
	 * @since Apr 25, 2014
	 */
	public static ERAttachment fetchAttachmentFor(final EOEditingContext editingContext, final Integer attachmentPrimaryKey, final String requestedWebPath) {
		ERAttachment attachment;
		if (attachmentPrimaryKey != null) {
			final EOGlobalID gid = EOKeyGlobalID.globalIDWithEntityName(ERAttachment.ENTITY_NAME, new Object[] {(attachmentPrimaryKey)});
			attachment = (ERAttachment) ERXEOGlobalIDUtilities.fetchObjectWithGlobalID(editingContext, gid);

			/*
			 * Ensure the attachment request is a legitimate one by comparing the attachment's webPath to the
			 * requestedWebPath.
			 */
			final boolean requestedWebPathIsInvalid = !ERAttachmentRequestHandler.requestedWebPathIsForAttachment(requestedWebPath, attachment);
			if (requestedWebPathIsInvalid) {
				throw new SecurityException("You are not allowed to view the requested attachment.");
			}
		} else {
			/*
			 * Aaron Rosenzweig April 25, 2014 
			 * WARNING: This is partially broken on an edge case and no easy fix is available. Any true fix would break
			 * current WOnder users of ERAttachment. Short version: We cannot URLDecode the column in the database efficiently. 
			 * See details below:
			 * 
			 * If the webPath value that is stored in the database (actualWebPath) contains "%20" (or any other %nn
			 * code) the following fetch will not find it because the requestedWebPath needs to be decoded, which will
			 * remove any occurrences of "%20" from it, causing it to no longer match the actualWebPath value.
			 */
			String decodedRequestedWebPath;
			try {
				decodedRequestedWebPath = new URI(requestedWebPath).getPath();
				attachment = ERAttachment.fetchRequiredAttachmentWithWebPath(editingContext, decodedRequestedWebPath);
			} catch (URISyntaxException exception) {
				attachment = null;
				exception.printStackTrace();
			}
			
			if (attachment == null) {
				throw new SecurityException("You are not allowed to view the requested attachment.");
			}
		}
		return attachment;
	}


	/**
	 * Takes into account potential URL encoding differences between the {@code requestedWebPath} and the
	 * {@code attachment}'s {@link ERAttachment#webPath() webPath()} attribute. e.g., "/the/web/path/My Attachment.jpg"
	 * will match "/the/web/path/My%20Attachment.jpg"
	 * 
	 * @param requestedWebPath
	 *        a String to compare to the {@code attachment} parameter's
	 * @param attachment
	 *        an ERAttachment
	 * @return {@code true} if the {@code requestedWebPath} matches {@code attachment.webPath()}. {@code false} if not.
	 * 
	 * @author davendasora
	 * @since Apr 25, 2014
	 */
	public static boolean requestedWebPathIsForAttachment(final String requestedWebPath, final ERAttachment attachment) {
		final String actualWebPath = attachment.webPath();
		/*
		 * We are using the form-data decoder (URLDecoder.decode(String)) instead of the more appropriate URI#getPath()
		 * because we only need to ensure that both webPath values decode identically. We can't use URI.getPath() here
		 * because the webPath value stored in the database (actualWebPath) may contain illegal values (spaces, etc.)
		 */
		final String decodedActualWebPath = ERXStringUtilities.urlDecode(actualWebPath);
		final String decodedRequestedWebPath = ERXStringUtilities.urlDecode(requestedWebPath);

		/*
		 * Aaron Rosenzweig - April 24, 2014 - Because the attachment may have been originally uploaded with "%20" (or
		 * another %nn value) already in the file name, we need to compare both the stored decoded (actualWebPath) 
		 * against the decoded requested value (requestedWebPath) otherwise we could incorrectly throw a SecurityException.
		 */
		final boolean requestedWebPathMatchesTheAttachmentWebPath = decodedRequestedWebPath.equals(decodedActualWebPath);
		return requestedWebPathMatchesTheAttachmentWebPath;
	}
}