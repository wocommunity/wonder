// Review.java
// Created on Sun Oct 22 12:13:04 US/Pacific 2000 by Apple EOModeler Version 410

package demo.eo;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import java.util.*;
import java.math.BigDecimal;

public class Review extends EOGenericRecord {


    public String reviewer() { return (String)storedValueForKey("reviewer"); }
    public void setReviewer(String value) {takeStoredValueForKey(value, "reviewer"); }

    public String review() { return (String)storedValueForKey("review"); }
    public void setReview(String value) {takeStoredValueForKey(value, "review"); }

    public demo.eo.Movie movie() { return (demo.eo.Movie)storedValueForKey("movie"); }
    public void setMovie(demo.eo.Movie value) { takeStoredValueForKey(value, "movie"); }
}
