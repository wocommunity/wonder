// Movie.java
// Created on Sat Oct 21 16:27:10 US/Pacific 2000 by Apple EOModeler Version 410

package demo.eo;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import java.util.*;
import java.math.BigDecimal;

public class Movie extends EOGenericRecord {


    public String category() { return (String)storedValueForKey("category"); }
    public void setCategory(String value) { takeStoredValueForKey(value, "category"); }

    public NSTimestamp dateReleased() { return (NSTimestamp)storedValueForKey("dateReleased"); }
    public void setDateReleased(NSTimestamp value) { takeStoredValueForKey(value, "dateReleased"); }

    public String title() { return (String)storedValueForKey("title"); }
    public void setTitle(String value) { takeStoredValueForKey(value, "title"); }

    public BigDecimal revenue() { return (BigDecimal)storedValueForKey("revenue"); }
    public void setRevenue(BigDecimal value) { takeStoredValueForKey(value, "revenue"); }

    public String posterName() { return (String)storedValueForKey("posterName"); }
    public void setPosterName(String value) { takeStoredValueForKey(value, "posterName"); }

    public String trailerName() { return (String)storedValueForKey("trailerName"); }
    public void setTrailerName(String value) { takeStoredValueForKey(value, "trailerName"); }

    public String rated() { return (String)storedValueForKey("rated"); }
    public void setRated(String value) { takeStoredValueForKey(value, "rated"); }

    public demo.eo.PlotSummary plotSummary() { return (demo.eo.PlotSummary)storedValueForKey("plotSummary"); }
    public void setPlotSummary(demo.eo.PlotSummary value) { takeStoredValueForKey(value, "plotSummary"); }

    public demo.eo.Studio studio() { return (demo.eo.Studio)storedValueForKey("studio"); }
    public void setStudio(demo.eo.Studio value) { takeStoredValueForKey(value, "studio"); }

    public demo.eo.Voting voting() { return (demo.eo.Voting)storedValueForKey("voting"); }
    public void setVoting(demo.eo.Voting value) { takeStoredValueForKey(value, "voting"); }

    public NSArray directors() { return (NSArray)storedValueForKey("directors"); }
    public void setDirectors(NSMutableArray value) { takeStoredValueForKey(value, "directors"); }

    public void addToDirectors(demo.eo.Talent object) {
        NSMutableArray array = (NSMutableArray)directors();
        willChange();
        array.addObject(object);
    }

    public void removeFromDirectors(demo.eo.Talent object) {
        NSMutableArray array = (NSMutableArray)directors();
        willChange();
        array.removeObject(object);
    }

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

    public NSArray reviews() { return (NSArray)storedValueForKey("reviews"); }
    public void setReviews(NSMutableArray value) { takeStoredValueForKey(value, "reviews"); }

    public void addToReviews(demo.eo.Review object) {
        NSMutableArray array = (NSMutableArray)reviews();
        willChange();
        array.addObject(object);
    }

    public void removeFromReviews(demo.eo.Review object) {
        NSMutableArray array = (NSMutableArray)reviews();
        willChange();
        array.removeObject(object);
    }
}
