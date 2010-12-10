// MovieRole.java
// Created on Sun Oct 22 12:13:02 US/Pacific 2000 by Apple EOModeler Version 410

package demo.eo;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import java.util.*;
import java.math.BigDecimal;

public class MovieRole extends EOGenericRecord {


    public String roleName() { return (String)storedValueForKey("roleName"); }
    public void setRoleName(String value) {takeStoredValueForKey(value, "roleName"); }

    public demo.eo.Movie movie() { return (demo.eo.Movie)storedValueForKey("movie"); }
    public void setMovie(demo.eo.Movie value) { takeStoredValueForKey(value, "movie"); }

    public demo.eo.Talent talent() { return (demo.eo.Talent)storedValueForKey("talent"); }
    public void setTalent(demo.eo.Talent value) { takeStoredValueForKey(value, "talent"); }
}
