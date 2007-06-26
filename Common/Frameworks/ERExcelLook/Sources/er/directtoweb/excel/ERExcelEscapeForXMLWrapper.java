//
// ERExcelEscapeForXMLWrapper.java: Class file for WO Component 'ERExcelEscapeForXMLWrapper'
// Project ERExcelLook
//
// Created by David Scheck on Dec 6, 2006
//
package er.directtoweb.excel;

import com.webobjects.appserver.*;
import er.directtoweb.*;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import er.extensions.ERXLogger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import er.extensions.*;

// This class tries to strip out all the HTML in subcomponents so that it can be generically exported using the Excel libs
public class ERExcelEscapeForXMLWrapper extends WOComponent {
    public static ERXLogger log = ERXLogger.getERXLogger(ERExcelEscapeForXMLWrapper.class);

    public ERExcelEscapeForXMLWrapper(WOContext context) {
        super(context);
    }

    public boolean synchronizesVariablesWithBindings() {
        return false;
    }

    public void appendToResponse(WOResponse r, WOContext aContext) {
        String contentSoFar = r.contentString();

        WOResponse tempResp = new WOResponse();
        super.appendToResponse(tempResp, aContext);

        String preEscapeString = tempResp.contentString();

        String removedHTMLString = stripHtml(preEscapeString);

        String escapeForXML = escapeForXML(removedHTMLString);

        if (log.isDebugEnabled()) {
            log.debug("preEscapeString = ["+preEscapeString+"]");
            log.debug("removedHTMLString = ["+removedHTMLString+"]");
            log.debug("escapeForXML = ["+escapeForXML+"]");
        }

        r.appendContentString(escapeForXML);
    }

   public static String stripHtml(String text) {
        if (text == null)
            return "";

        String result = text;
        try {
            Pattern htmlNewlinePattern = Pattern.compile("(</div>|</p>|<br[^>]+>)", Pattern.CASE_INSENSITIVE);
            Pattern realNewlinePattern = Pattern.compile("(\r(\n)?)+", Pattern.DOTALL);
            Pattern nonBlankingSpacePattern = Pattern.compile("(\\&nbsp\\;)+", Pattern.CASE_INSENSITIVE);
            Pattern collapseWhiteSpacePattern = Pattern.compile("(\\s{2,})+", Pattern.CASE_INSENSITIVE);
            Pattern stylePattern = Pattern.compile("<style[^>]*>[^<]+</style>", Pattern.CASE_INSENSITIVE);
            Pattern scriptPattern = Pattern.compile("<script[^>]*>[^<]+</script>", Pattern.CASE_INSENSITIVE);

            text = realNewlinePattern.matcher(text).replaceAll("");
            text = htmlNewlinePattern.matcher(text).replaceAll("\n");
            text = nonBlankingSpacePattern.matcher(text).replaceAll(" ");
            text = stylePattern.matcher(text).replaceAll("");
            text = scriptPattern.matcher(text).replaceAll("");

            String extraRegExToStrip=ERXProperties.stringForKey("er.directtoweb.excel.ERExcelEscapeForXMLWrapper.extraRegExToStrip");

            if (extraRegExToStrip != null)
                text = text.replaceAll(extraRegExToStrip,"");

            text = text.replaceAll("<[^>]+>", ""); // strip ALL HTML tags; it's not worth dealing with.

            text = collapseWhiteSpacePattern.matcher(text).replaceAll("\n");

            result = text.trim();

        } catch (RuntimeException e) {
            log.warn("RuntimeException on stripHtml() for text " + text,e);
        }
        return result;
    }

    public static final String DONT_ESCAPE_INDICATOR = "~";

    private static char dontEscapeCharacter(){
	return DONT_ESCAPE_INDICATOR.charAt(0);
    }

    /**
     * Escape the 5 characters that have corresponding XML entities
     * @param string the string to escape. If the text includes a ~ before a
     * char that would normally be escaped, the normally escaped char is left alone
     * and the ~ is removed.
     */
    public static String escapeForXML(String string) {
        if (string != null) {
            int len = string.length();
            char dontEscChar = dontEscapeCharacter();
            StringBuffer buf = null;
            for (int i = 0; i < len; i++) {
                char c = string.charAt(i);
                char cminus1 = c;
                if(i > 0) cminus1 = string.charAt(i-1);
                boolean dontEscapeIfEscaping = (cminus1 == dontEscChar);

                if (c == '&' && !dontEscapeIfEscaping) {
                    if (buf == null) buf = new StringBuffer(string.substring(0, i));

                    String stringAtC=string.substring(i);

                    // Need to make sure the & is not already an escaped sequence.
                    if (stringAtC.startsWith("&amp;") ||stringAtC.startsWith("&lt;") || stringAtC.startsWith("&gt;") ||
                        stringAtC.startsWith("&#34;") || stringAtC.startsWith("&#39;")) {
                        buf.append('&');
                    }
                    else {
                        buf.append("&amp;");
                    }
                } else if (c == '<' && !dontEscapeIfEscaping){
                    if (buf == null) buf = new StringBuffer(string.substring(0, i));
                    buf.append("&lt;");
                } else if (c == '>' && !dontEscapeIfEscaping){
                    if (buf == null) buf = new StringBuffer(string.substring(0, i));
                    buf.append("&gt;");
                } else if (c == '"' && !dontEscapeIfEscaping){
                    if (buf == null) buf = new StringBuffer(string.substring(0, i));
                    buf.append("&#34;"); // don't make this &quot; or the puma plist parser chokes
                } else if (c == '\'' && !dontEscapeIfEscaping){
                    if (buf == null) buf = new StringBuffer(string.substring(0, i));
                    buf.append("&#39;"); // don't make this &apos; or the puma plist parser chokes
                } else if(c == dontEscChar){
                    if(i < len-1){
                        char cplus1 = string.charAt(i+1);
                        // remove the escape marking character
                        if(cplus1 == '\'' || cplus1 == '"' || cplus1 == '>' || cplus1 == '<' || cplus1 == '&'){
                            // create the buffer now so that we make sure the ~ doesn't get added
                            if (buf == null) buf = new StringBuffer(string.substring(0, i));
                        }else{
                            if (buf != null) {
                                if (c>31 || c==10 || c==13) buf.append(c); // filter out bad ascii
                            }
                        }
                    }
                }else{
                    if (buf != null) {
                        if (c>31 || c==10 || c==13) buf.append(c); // filter out bad ascii
                    }
                }

            }
            if (buf != null) {
                string = buf.toString();
            }
        }
        return string;
    }

}
