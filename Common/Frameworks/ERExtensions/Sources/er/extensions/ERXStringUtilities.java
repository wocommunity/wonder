//
// StringUtilities.java
// Project linksadmin
//
// Created by ak on Mon Nov 05 2001
//
package er.extensions;
import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import java.io.*;

public class ERXStringUtilities extends Object {
   public static String stringWithContentsOfFile(String path) {
        try {
            InputStream in = new FileInputStream(path);
            
            if(null == in)
                throw new RuntimeException("The file '"+ path + "' can not be opened.");
            int length = in.available();
            if (length == 0) {
                return "";
            }
            byte buffer[] = new byte[length];
            in.read(buffer);
            in.close();
            return new String(buffer);
        } catch(Throwable t) {
            // cat.debug(t.toString());
        }
        return null;
    }

    public static Integer integerWithString(String s) {
        try {
            return new Integer(Integer.parseInt(s));
        } catch (Exception e) {
        }
        return null;
    } 

    public static String stringFromResource(String name, String extension, NSBundle bundle) {
         return stringWithContentsOfFile(WOApplication.application().resourceManager().pathForResourceNamed(name +"." + extension, null, null));
    }

}
