// ERCMessageAttachment.java
// (c) by Anjo Krank (ak@kcmedia.ag)
package er.corebusinesslogic;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import er.extensions.*;
import java.io.*;

public class ERCMessageAttachment extends _ERCMessageAttachment {
    static final ERXLogger log = ERXLogger.getERXLogger(ERCMessageAttachment.class);

    public ERCMessageAttachment() {
        super();
    }

    public void awakeFromInsertion(EOEditingContext ec) {
        super.awakeFromInsertion(ec);
    }

    public File file() {
        return new File(filePath());
    }
    
    // Class methods go here
    
    public static class ERCMessageAttachmentClazz extends _ERCMessageAttachmentClazz {
        
    }

    public static ERCMessageAttachmentClazz messageAttachmentClazz() { return (ERCMessageAttachmentClazz)EOEnterpriseObjectClazz.clazzForEntityNamed("ERCMessageAttachment"); }
}
