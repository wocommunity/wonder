// Setting.java
// Created on Mon Nov 05 10:54:22  2001 by Apple EOModeler Version 410

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import java.util.*;
import java.math.BigDecimal;
import com.webobjects.eoaccess.*;

public class Setting extends _Setting {
    
    public Setting() {
        super();
    }

    public static Setting currentSettingForContext(EOEditingContext context) {
        NSArray settings =  EOUtilities.objectsForEntityNamed(context,"Setting");
        if (settings.count()>0) return (Setting) settings.objectAtIndex(0);
        else return null;
    }

}
