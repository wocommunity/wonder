import com.sun.tools.doclets.Taglet;
import com.sun.javadoc.*;
import java.util.Map;

/**
 * Taglet representing @d2wKey. This tag can be used in any kind of
 * {@link com.sun.javadoc.Doc}. It is not an inline tag and will be
 * displayed as a table with all d2w keys grouped. For
 * example, "@d2wKey someKey The key to use" would be shown as:
 * <DL>
 * <DT><B>D2W Keys:</B></DT>
 * <DD><table><tr><th>someKey</th><td>
 * The key to use</td></tr></table></DD>
 * </DL>
 *
 * @author ak
 */
public class D2WKeyTaglet extends AbstractTaglet {
    private static final String NAME = "d2wKey";
    private static final String HEADER = "D2W Keys";
    
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
       D2WKeyTaglet tag = new D2WKeyTaglet();
       Taglet t = (Taglet) tagletMap.get(tag.getName());
       if (t != null) {
           tagletMap.remove(tag.getName());
       }
       tagletMap.put(tag.getName(), tag);
    }
}
