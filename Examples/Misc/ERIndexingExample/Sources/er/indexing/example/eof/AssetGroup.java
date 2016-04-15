package er.indexing.example.eof;

import com.webobjects.eocontrol.EOEditingContext;

public class AssetGroup extends _AssetGroup {
    public static final AssetGroupClazz clazz = new AssetGroupClazz();
    public static class AssetGroupClazz extends _AssetGroup._AssetGroupClazz {
        /* more clazz methods here */
    }

    public interface Key extends _AssetGroup.Key {}

    /**
     * Initializes the EO. This is called when an EO is created, not when it is 
     * inserted into an EC.
     */
    @Override
    public void init(EOEditingContext ec) {
        super.init(ec);
    }
    
    // more EO methods here
}
