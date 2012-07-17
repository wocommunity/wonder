package er.attachment.components.viewers;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSDictionary;

import er.extensions.foundation.ERXValueUtilities;

/**
 * ERAttachmentQuickTimeViewer is the viewer for QuickTime 
 * files. For more information on parameter bindings, see 
 * Apple's documentation regarding
 * <a href="http://developer.apple.com/DOCUMENTATION/QuickTime/Conceptual/QTScripting_HTML/QTScripting_HTML_Document/chapter_1000_section_5.html">
 * QuickTime Object Parameters</a>
 * 
 * @author Ramsey Gurley
 * @binding attachment the attachment to display
 * @binding class (optional) the class for the html &lt;object&gt;
 * @binding configurationName (optional) the configuration name for this attachment (see top level documentation)
 * @binding emptyAttachmentComponentName (optional) the name of an alternate component to display if the attachment relationship is empty
 * @binding height (optional) the height for the html &lt;object&gt;
 * @binding id (optional) the id for the html &lt;object&gt;
 * @binding parameterDictionary (optional) the NSDictionary containing QuickTime &lt;object&gt; parameters
 * @binding title (optional) the title for the html &lt;object&gt;
 * @binding standby (optional) the text to display for the html &lt;object&gt; while it is loading
 * @binding width (optional) the width for the html &lt;object&gt;
 *
 */

public class ERAttachmentQuickTimeViewer extends AbstractERAttachmentViewer {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    private String item;
    private NSDictionary parameters;

	public ERAttachmentQuickTimeViewer(WOContext context) {
        super(context);
    }
	
	@Override
  public void reset() {
		super.reset();
		item = null;
		parameters = null;
	}

	/**
	 * @return the item
	 */
	public String item() {
		return item;
	}

	/**
	 * @param item the item to set
	 */
	public void setItem(String item) {
		this.item = item;
	}
	
	public NSDictionary parameters() {
		if (parameters == null) {
			parameters = ERXValueUtilities.dictionaryValue(valueForBinding("parameterDictionary"));
		}
		return parameters;
	}

	public String parameterValue() {
		return (parameters() == null)?null:(String)parameters().valueForKey(item());
	}
}