
import com.sun.tools.doclets.Taglet;
import com.sun.javadoc.*;
import java.util.Map;

public class ProjectTaglet extends AbstractTaglet {

    public String getName() { return "project"; }

    public String getHeader() { return "Project"; }

    /**
     * Register this Taglet.
     * @param tagletMap  the map to register this tag to.
     */
    public static void register(Map tagletMap) {
       ProjectTaglet tag = new ProjectTaglet();
       Taglet t = (Taglet) tagletMap.get(tag.getName());
       if (t != null) {
           tagletMap.remove(tag.getName());
       }
       tagletMap.put(tag.getName(), tag);
    }
}
