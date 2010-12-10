// Studio.java
// Created on Sun Oct 22 12:13:05 US/Pacific 2000 by Apple EOModeler Version 410

package demo.eo;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import java.util.*;
import java.math.BigDecimal;

public class Studio extends EOGenericRecord {


    public BigDecimal budget() { return (BigDecimal)storedValueForKey("budget"); }
    public void setBudget(BigDecimal value) {takeStoredValueForKey(value, "budget"); }

    public String name() { return (String)storedValueForKey("name"); }
    public void setName(String value) {takeStoredValueForKey(value, "name"); }

    public NSArray movies() { return (NSArray)storedValueForKey("movies"); }
    public void setMovies(NSMutableArray value) { takeStoredValueForKey(value, "movies"); }

    public void addToMovies(demo.eo.Movie object) {
        NSMutableArray array = (NSMutableArray)movies();
        willChange();
        array.addObject(object);
    }

    public void removeFromMovies(demo.eo.Movie object) {
        NSMutableArray array = (NSMutableArray)movies();
        willChange();
        array.removeObject(object);
    }
}
