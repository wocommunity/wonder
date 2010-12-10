// Voting.java
// Created on Sun Oct 22 12:13:10 US/Pacific 2000 by Apple EOModeler Version 410

package demo.eo;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import java.util.*;
import java.math.BigDecimal;

public class Voting extends EOGenericRecord {


    public Number numberOfVotes() { return (Number)storedValueForKey("numberOfVotes"); }
    public void setNumberOfVotes(Number value) {takeStoredValueForKey(value, "numberOfVotes"); }

    public Number runningAverage() { return (Number)storedValueForKey("runningAverage"); }
    public void setRunningAverage(Number value) {takeStoredValueForKey(value, "runningAverage"); }

    public demo.eo.Movie movie() { return (demo.eo.Movie)storedValueForKey("movie"); }
    public void setMovie(demo.eo.Movie value) { takeStoredValueForKey(value, "movie"); }
}
