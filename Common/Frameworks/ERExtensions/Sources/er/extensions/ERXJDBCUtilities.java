package er.extensions;

import com.webobjects.foundation.*;


public class ERXJDBCUtilities {
    static NSTimestampFormatter TIMESTAMP_FORMATTER=new NSTimestampFormatter("%Y-%m-%d %H:%M:%S");

    public static String jdbcTimestamp(NSTimestamp t) {
        StringBuffer b = new StringBuffer();
        b.append("TIMESTAMP '").append(TIMESTAMP_FORMATTER.format(t)).append(".000'");
        return b.toString();
    }
}
