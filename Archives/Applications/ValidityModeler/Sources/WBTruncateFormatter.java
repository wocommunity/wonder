import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;

/**
 * WBTruncateFormatter is a formatter that truncates a string. Unlike the truncation performed with the
 * WBComboFormatter, the truncation will split words.
 */
public class WBTruncateFormatter extends Format {

    private int truncateLength = 0;
    private String appender = null;

     /**
     * Creates a new WBTruncateFormatter.
     *
     * @param length       Length to truncate to
     * @param append       A String appended to the truncated String should the String actually be truncated.
     */
    public WBTruncateFormatter(int length, String append){
        super();
        appender = append;
        truncateLength = length;
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
        if(!(object instanceof String) ){
            throw new IllegalArgumentException("WBTruncateFormatter only formats 'String' objects.");
        } else {
            String result = (String)object;
            int resultLength = result.length();
            if(resultLength > truncateLength){
                result = result.substring(0, truncateLength);
                if(appender != null){
                    result = result + appender;
                }
            }
            return r.append(result);
        }
    }

    public Object parseObject(String anObject, ParsePosition o){
        return anObject;
    }
    //ivars

    public int truncateLength(){
        return truncateLength;
    }

    public void setTruncateLength(int len){
        truncateLength = len;
    }

}
