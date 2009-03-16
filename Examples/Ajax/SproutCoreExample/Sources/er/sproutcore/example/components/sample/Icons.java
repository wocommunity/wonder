package er.sproutcore.example.components.sample;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;

import er.sproutcore.views.SCComponent;

public class Icons extends SCComponent {
	
	public String item;
	
    public Icons(WOContext context) {
        super(context);
        setClassName("SC.View");
    }
    
    public String name48() {
    	return "sc-icon-" + item + "-48";
    }
    
    public String name24() {
    	return "sc-icon-" + item + "-24";
    }
    
    public String name16() {
    	return "sc-icon-" + item + "-16";
    }
    
    public NSArray icons48() {
    	return NSArray.componentsSeparatedByString("alert info error", " ");
    }
    
    public NSArray icons24() {
    	return NSArray.componentsSeparatedByString("tools bookmark info help alert left right up down undo redo group user cancel options folder trash document favorite", " ");
    }
    
    public NSArray icons16() {
    	return NSArray.componentsSeparatedByString("bookmark info help alert group user options folder trash document favorite", " ");
    }
}