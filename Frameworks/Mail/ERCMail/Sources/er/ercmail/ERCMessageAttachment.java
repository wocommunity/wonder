// ERCMessageAttachment.java
// (c) by Anjo Krank (ak@kcmedia.ag)
package er.ercmail;

import java.io.File;

import org.apache.log4j.Logger;

import com.webobjects.eocontrol.EOEditingContext;

import er.extensions.eof.EOEnterpriseObjectClazz;

public class ERCMessageAttachment extends _ERCMessageAttachment {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    static final Logger log = Logger.getLogger(ERCMessageAttachment.class);

    public ERCMessageAttachment() {
        super();
    }

    @Override
    public void init(EOEditingContext ec) {
        super.init(ec);
    }
    
    public File file() {
      return new File(filePath());
    }
    
    
    // Class methods go here
    
    public static class ERCMessageAttachmentClazz extends _ERCMessageAttachmentClazz {
        
    }

    public static ERCMessageAttachmentClazz messageAttachmentClazz() { return (ERCMessageAttachmentClazz)EOEnterpriseObjectClazz.clazzForEntityNamed("ERCMessageAttachment"); }
}
