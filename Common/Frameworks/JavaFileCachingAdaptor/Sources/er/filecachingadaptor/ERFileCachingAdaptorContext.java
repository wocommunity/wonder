//
//  ERFileCachingAdaptorContext.java
//
//  Created by Thomas Burkholder on Thu May 10, 2005.
//
package er.filecachingadaptor;

import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import com.webobjects.eoaccess.*;
import java.util.*;
import er.extensions.*;

public class ERFileCachingAdaptorContext extends ERXForwardingAdaptorContext {
    public ERFileCachingAdaptorContext(EOAdaptor adaptor, EOAdaptorContext ac) {
        super(adaptor,ac);
    }

    private HashMap _wrappedChannels = new HashMap();

    public EOAdaptorChannel createAdaptorChannel() {
        EOAdaptorChannel raw = super.createAdaptorChannel();
        EOAdaptorChannel cooked = (EOAdaptorChannel)_wrappedChannels.get(raw);
        if (cooked == null) {
            cooked = new ERFileCachingAdaptorChannel(this,raw);
            _wrappedChannels.put(raw,cooked);
        }
        return cooked;
    }
}
