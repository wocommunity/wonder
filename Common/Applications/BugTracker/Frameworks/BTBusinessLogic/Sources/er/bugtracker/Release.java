// Release.java
// 
package er.bugtracker;
import org.apache.log4j.Logger;

import com.webobjects.eocontrol.EOEditingContext;

import er.extensions.ERXConstant;

public class Release extends _Release {
    static final Logger log = Logger.getLogger(Release.class);

    public Release() {
        super();
    }

    public void awakeFromInsertion(EOEditingContext ec) {
        super.awakeFromInsertion(ec);
    }
    

    public void setIsOpenAsBoolean(boolean open) {
        setIsOpen(open ? ERXConstant.OneInteger:ERXConstant.ZeroInteger);
    }
    public boolean isOpenAsBoolean() {
        return ERXConstant.OneInteger.equals(isOpen());
    }

    // Class methods go here
    
    public static class ReleaseClazz extends _ReleaseClazz {
        // FIXME: (ak) these are just stubs
        // I don't have the slightest idea what a "release" in the NS context is!
        public Release defaultRelease(EOEditingContext ec) {
            return (Release)(allObjects(ec).lastObject());
        }
        public Release targetRelease(EOEditingContext ec) {
            return (Release)(allObjects(ec).lastObject());
        }
    }

    public static final ReleaseClazz clazz = new ReleaseClazz();
}
