import com.sun.tools.doclets.Taglet;
import com.sun.javadoc.*;
import java.util.Map;

/**
 * Abstract Taglet class for our binding and D2W keys
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
     * @param tag   the <code>Tag</code> representation of this custom tag.
     */
    public String toString(Tag tag) {
        return toString(new Tag[] {tag});
    }
    
    private String bindingName(Tag tag) {
        String result = tag.text();
        if(result != null) {
            int space = result.indexOf(" ");
            if(space >= 0) {
                result = result.substring(0, space);
            }
        }
        return result;
    }
    
    private String bindingDescription(Tag tag) {
        String result = tag.text();
        if(result != null) {
            int space = result.indexOf(" ");
            if(space >= 0) {
                result = result.substring(space);
            }
        }
        return result;
    }
    
    /**
     * Given an array of <code>Tag</code>s representing this custom
     * tag, return its string representation.
     * @param tags  the array of <code>Tag</code>s representing of this custom tag.
     */
    public String toString(Tag[] tags) {
        if (tags.length == 0) {
            return null;
        }
        String result = "";
        result += "<TABLE BORDER=\"1\" WIDTH=\"100%\" CELLPADDING=\"3\" CELLSPACING=\"0\" SUMMARY=\"\">";
        result += "<TR BGCOLOR=\"#CCCCFF\" CLASS=\"TableHeadingColor\">";
        result += "<TD COLSPAN=2><FONT SIZE=\"+2\">";
        result += "<B>"+getHeader()+"</B></FONT></TD>";
        result += "</TR>";
        for (int i = 0; i < tags.length; i++) {
            result += "<TR BGCOLOR=\"white\" CLASS=\"TableRowColor\">";
            result += "<TD ALIGN=\"right\" VALIGN=\"top\" WIDTH=\"1%\"><FONT SIZE=\"-1\">";
            result += "<CODE>";
            result += bindingName(tags[i]);
            result += "</CODE></FONT></TD>";
            result += "<TD>";
            result += bindingDescription(tags[i]);
            result += "<BR>";
            result += "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</TD>";
            result += "</TR>";
        }
        return result + "</TABLE>\n";
    }
}

