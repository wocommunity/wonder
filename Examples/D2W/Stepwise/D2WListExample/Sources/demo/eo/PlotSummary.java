// PlotSummary.java
// Created on Sun Oct 22 12:13:03 US/Pacific 2000 by Apple EOModeler Version 410

package demo.eo;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import java.util.*;
import java.math.BigDecimal;

public class PlotSummary extends EOGenericRecord {


    public String summary() { return (String)storedValueForKey("summary"); }
    public void setSummary(String value) {takeStoredValueForKey(value, "summary"); }

    public demo.eo.Movie movie() { return (demo.eo.Movie)storedValueForKey("movie"); }
    public void setMovie(demo.eo.Movie value) { takeStoredValueForKey(value, "movie"); }
}
