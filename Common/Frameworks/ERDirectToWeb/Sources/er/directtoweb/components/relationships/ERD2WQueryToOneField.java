//
// ERD2WQueryToOneField.java: Class file for WO Component 'ERD2WQueryToOneField'
// Project ERDirectToWeb
//
// Created by giorgio on 17/02/05
//
package er.directtoweb.components.relationships;

import java.text.Format;

import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WQueryToOneField;
import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.foundation.NSNumberFormatter;
import com.webobjects.foundation.NSTimestampFormatter;

public class ERD2WQueryToOneField extends D2WQueryToOneField {

    public ERD2WQueryToOneField(WOContext context) {
        super(context);
    }

    public Format valueFormatter() {
        Format f = null;
        EORelationship rel = (EORelationship) valueForKeyPath("d2wContext.smartRelationship");
        EOEntity destEnt = rel.destinationEntity();
        EOAttribute searchAttr = destEnt.attributeNamed(keyWhenRelationship());
        if(searchAttr != null) {
            String className = searchAttr.className();
            if(className.equals("java.lang.Number"))
                f = new NSNumberFormatter("0");
            if(className.equals("java.lang.BigDecimal"))
                f = new NSNumberFormatter("$#,##0.00;-$#,##0.00");
            if(className.equals("com.webobjects.foundation.NSTimestamp"))
                f = new NSTimestampFormatter("%b %d,%Y");            
            
        }
        return f;
    }
}
