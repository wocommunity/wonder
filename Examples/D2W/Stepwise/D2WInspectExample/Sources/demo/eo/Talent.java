// Talent.java
// Created on Sat Oct 21 16:27:11 US/Pacific 2000 by Apple EOModeler Version 410

package demo.eo;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import java.util.*;
import java.math.BigDecimal;

public class Talent extends EOGenericRecord {

    public String fullName() { return firstName() + " " + lastName(); }

    public String firstName() { return (String)storedValueForKey("firstName"); }
    public void setFirstName(String value) { takeStoredValueForKey(value, "firstName"); }

    public String lastName() { return (String)storedValueForKey("lastName"); }
    public void setLastName(String value) { takeStoredValueForKey(value, "lastName"); }

    public demo.eo.TalentPhoto photo() { return (demo.eo.TalentPhoto)storedValueForKey("photo"); }
    public void setPhoto(demo.eo.TalentPhoto value) { takeStoredValueForKey(value, "photo"); }

    public NSArray roles() { return (NSArray)storedValueForKey("roles"); }
    public void setRoles(NSMutableArray value) { takeStoredValueForKey(value, "roles"); }

    public void addToRoles(demo.eo.MovieRole object) {
        NSMutableArray array = (NSMutableArray)roles();
        willChange();
        array.addObject(object);
    }

    public void removeFromRoles(demo.eo.MovieRole object) {
        NSMutableArray array = (NSMutableArray)roles();
        willChange();
        array.removeObject(object);
    }

    public NSArray moviesDirected() { return (NSArray)storedValueForKey("moviesDirected"); }
    public void setMoviesDirected(NSMutableArray value) { takeStoredValueForKey(value, "moviesDirected"); }

    public void addToMoviesDirected(demo.eo.Movie object) {
        NSMutableArray array = (NSMutableArray)moviesDirected();
        willChange();
        array.addObject(object);
    }

    public void removeFromMoviesDirected(demo.eo.Movie object) {
        NSMutableArray array = (NSMutableArray)moviesDirected();
        willChange();
        array.removeObject(object);
    }
}
