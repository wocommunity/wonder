package er.attachment.components;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;

import er.attachment.model.ERAttachment;
import er.attachment.processors.ERAttachmentProcessor;

public class ERAttachmentViewer extends WOComponent {
  public ERAttachmentViewer(WOContext context) {
    super(context);
    ERAttachmentRequestHandler.ensureRequestHandlerRegistered();
  }

  public ERAttachment attachment() {
    return (ERAttachment) valueForBinding("attachment");
  }

  public String attachmentUrl() {
    WOContext context = context();
    ERAttachment attachment = attachment();
    String configurationName = (String) valueForBinding("configurationName");
    return ERAttachmentProcessor.processorForType(attachment).attachmentUrl(attachment, context.request(), context, configurationName);
  }

  @Override
  public boolean synchronizesVariablesWithBindings() {
    return false;
  }
}