import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;

import com.gammastream.gammacore.gammatext.GSTextConstants;
import com.gammastream.gammacore.gammatext.GSTextUtilities;

/**
 * WBComboFormatter is a formatter that can truncate a string, strip out the HTML in a string, <BR>
 * and convert a textarea string to HTML.  WBComboFormatter can format the string useing any combination. <BR>
 * The textarea  to HTML takes a String and converts the carriage returns to breaks and tabs to five non-break spaces.
 */
public class WBComboFormatter extends Format {
  
    /**
     * Default length to truncate a String.
     */
    public static final int DEFAULT_LENGTH = 250;

    private int truncateLength = DEFAULT_LENGTH; //default length;
    private boolean truncate;
    private boolean stripHTML;
    private boolean textareaToHTML;
   
     /**
      * Creates a new WBComboFormatter.
      *
      * @param t       Needs to truncate the String
      * @param s       Needs to strip out the HTML in the String
      * @param h       Needs to convert the textarea string to HTML
      */
    public WBComboFormatter(boolean t, boolean s, boolean h){
        super();
        truncate = t;
        stripHTML = s;
        textareaToHTML = h;
        if(truncate){
            truncateLength = DEFAULT_LENGTH;
        }
    }

     /**
     * Formats the string.
     *
     * @param object      	String to format
     * @param r       		StringBuffer
     * @param d       		FieldPosition
     * @exception java.lang.IllegalArgumentException  Thrown if object is not a String.
     */
    public StringBuffer format(Object object, StringBuffer r, FieldPosition d){
        if( !(object instanceof String) ){
            throw new IllegalArgumentException("WBComboFormatter only formats 'String' objects.");
        } else {
            String result = (String)object;
            if(truncate){
                result = GSTextUtilities.truncate(result, truncateLength);
            }
            if(stripHTML){
                result = GSTextUtilities.stringStrippedOfHTML(result);
            }
            if(textareaToHTML){
                result = GSTextUtilities.replaceStringWithStringInString( 
                                            GSTextConstants.CARRIAGE_RETURN_STRING, "<BR>", result);
                result = GSTextUtilities.replaceStringWithStringInString(
                                            GSTextConstants.TAB_STRING,
                                            "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;",
                                            result);
            }
            return r.append(result);
        }
    }

    public Object parseObject(String anObject, ParsePosition o){
        return anObject;
    }

    public int truncateLength(){
        if(truncate){
            return truncateLength;
        } else {
            return 0;
        }
    }

    public void setTruncateLength(int len){
        truncateLength = len;
        truncate = true;
    }

    public boolean truncate(){
        return truncate;
    }

    public void setTruncate(boolean bool){
        truncate = bool;
    }

    public boolean stripHTML(){
        return stripHTML;
    }

    public void setStripHTML(boolean bool){
        stripHTML = bool;
    }

    public boolean textareaToHTML(){
        return textareaToHTML;
    }

    public void setTextareaToHTML(boolean bool){
        textareaToHTML = bool;
    }

}
