import com.sun.tools.doclets.Taglet;
import com.sun.javadoc.*;
import java.util.Map;

/**
 * Taglet representing @property. This tag can be used in any kind of
 * {@link com.sun.javadoc.Doc}.  It is not an inline tag. For
 * example, "@property er.migration.skipModelNames a comma-separated list of 
 * model names to NOT be migrated." would be shown as:
 * <DL>
 * <DT>
 * <B>Properties:</B>
 * <DD><table><tr><th>er.migration.skipModelNames</th><td>
 * a comma-separated list of model names
 * to NOT be migrated.</td></tr></table></DD>
 * </DL>
 *
 * @author chill
 */

public class PropertyTaglet extends AbstractTaglet  {
    
    private static final String NAME = "property";
    private static final String HEADER = "Properties";
    
    public String getName() {
        return NAME;
    }
    
    public String getHeader() {
        return HEADER;
    }
    
    public boolean inMethod() { return true; }

    /**
     * Register this Taglet.
     * @param tagletMap the map to register this tag to.
     */
    public static void register(Map<String,Taglet> tagletMap) {
       PropertyTaglet tag = new PropertyTaglet();
       Taglet t = (Taglet) tagletMap.get(tag.getName());
       if (t != null) {
           tagletMap.remove(tag.getName());
       }
       tagletMap.put(tag.getName(), tag);
    }
}

