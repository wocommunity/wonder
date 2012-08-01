import com.sun.tools.doclets.Taglet;
import com.sun.javadoc.*;
import java.util.Map;

/**
 * Taglet representing @binding. This tag can be used in any kind of
 * {@link com.sun.javadoc.Doc}. It is not an inline tag and will be
 * displayed as a table with all bindings grouped. For example
 * "@binding someKey The key to use" would be shown as:
 * <DL>
 * <DT><B>Bindings:</B></DT>
 * <DD><table><tr><th>someKey</th><td>
 * The key to use</td></tr></table></DD>
 * </DL>
 *
 * @author ak
 */
public class BindingTaglet extends AbstractTaglet  {
    private static final String NAME = "binding";
    private static final String HEADER = "Bindings";
    
    @Override
    public String getName() {
        return NAME;
    }
    
    @Override
    public String getHeader() {
        return HEADER;
    }
    
    /**
     * Register this Taglet.
     * 
     * @param tagletMap
     *            the map to register this tag to
     */
    @SuppressWarnings("unchecked")
    public static void register(Map tagletMap) {
       BindingTaglet tag = new BindingTaglet();
       Taglet t = (Taglet) tagletMap.get(tag.getName());
       if (t != null) {
           tagletMap.remove(tag.getName());
       }
       tagletMap.put(tag.getName(), tag);
    }
}
