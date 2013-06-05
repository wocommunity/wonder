package er.directtoweb.components.attachments;

import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WComponent;

/**
 * D2W component to display ERAttachments
 * 
 * The configurationName is computed: 'Entity.propertyKey'
 * The properties for this configuration name must be set:
 * @see <a href="http://jenkins.wocommunity.org/job/Wonder/javadoc/er/attachment/package-summary.html">http://jenkins.wocommunity.org/job/Wonder/javadoc/er/attachment/package-summary.html</a>
 * 
 * @author mendis
 * @d2wKey size
 */
public class ERD2WDisplayAttachment extends D2WComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public ERD2WDisplayAttachment(WOContext context) {
        super(context);
    }
}