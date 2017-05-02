import com.sun.tools.doclets.Taglet;
import com.sun.javadoc.*;
import java.util.Map;

/**
 * Abstract Taglet class for our binding and D2W keys.
 *
 * @author ak
 */
public abstract class AbstractTaglet implements Taglet {
    public abstract String getName();  
  
    public abstract String getHeader();    

    public boolean inField() {
        return false;
    }

    public boolean inConstructor() {
        return false;
    }

    public boolean inMethod() {
        return false;
    }
    
    public boolean inOverview() {
        return false;
    }

    public boolean inPackage() {
        return false;
    }

    public boolean inType() {
        return true;
    }
    
    public boolean isInlineTag() {
        return false;
    }
    
    /**
     * Given the <code>Tag</code> representation of this custom
     * tag, return its string representation.
     * 
     * @param tag
     *            the <code>Tag</code> representation of this custom tag
     * @return string representation of the <code>Tag</code>
     */
    public String toString(Tag tag) {
        return toString(new Tag[] {tag});
    }
    
    private String bindingName(Tag tag) {
        String result = tag.text();
        if (result != null) {
            int space = result.indexOf(" ");
            if (space >= 0) {
                result = result.substring(0, space);
            }
        }
        return result;
    }
    
    private String bindingDescription(Tag tag) {
        String result = tag.text();
        if (result != null) {
            int space = result.indexOf(" ");
            if (space >= 0) {
                result = result.substring(space);
            }
        }
        return result;
    }
    
    /**
     * Given an array of <code>Tag</code>s representing this custom
     * tag, return its string representation.
     * 
     * @param tags
     *            the array of <code>Tag</code>s representing of this custom tag
     * @return string representation of the <code>Tag</code>s
     */
    public String toString(Tag[] tags) {
        if (tags.length == 0) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<table class=\"memberSummary\" border=\"0\" cellpadding=\"3\" cellspacing=\"0\" summary=\"" + getHeader() + "\">");
        sb.append("<caption><span>" + getHeader() + "</span><span class=\"tabEnd\">&nbsp;</span></caption>");
        sb.append("<thead><tr>");
        sb.append("<th class=\"colFirst\" scope=\"col\">Name</th>");
        sb.append("<th class=\"colLast\" scope=\"col\">Description</th>");
        sb.append("</tr></thead>");
        sb.append("<tbody>");
        for (int i = 0; i < tags.length; i++) {
            sb.append("<tr class=\"" + (i % 2 == 0 ? "altColor" : "rowColor") + "\">");
            sb.append("<td class=\"colFirst\"><code>");
            sb.append(bindingName(tags[i]));
            sb.append("</code></td>");
            sb.append("<td class=\"colLast\">");
            sb.append(bindingDescription(tags[i]));
            sb.append("</td>");
            sb.append("</tr>");
        }
        sb.append("</tbody></table>\n");
        return sb.toString();
    }
}
