// TalentPhoto.java
// Created on Sat Oct 21 16:27:12 US/Pacific 2000 by Apple EOModeler Version 410

package demo.eo;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import java.util.*;
import java.math.BigDecimal;

public class TalentPhoto extends EOGenericRecord {


    public NSData photo() { return (NSData)storedValueForKey("photo"); }
    public void setPhoto(NSData value) { takeStoredValueForKey(value, "photo"); }

    public demo.eo.Talent talent() { return (demo.eo.Talent)storedValueForKey("talent"); }
    public void setTalent(demo.eo.Talent value) { takeStoredValueForKey(value, "talent"); }
}
