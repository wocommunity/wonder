//
// Main.java: Class file for WO Component 'Main'
// Project SVGPollTest2A
//
// Created by ravi on Mon Mar 18 2002
//
package org.svgobjects.examples.popidol;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;

public class PopIdol extends WOComponent {

    public PopIdol(WOContext context) {
        super(context);
    }

    /*
     * request/response
     */
    public void appendToResponse(WOResponse response, WOContext context) {
        super.appendToResponse(response, context);

        // set the header
        response.setHeader("image/svg-xml", "Content-Type");
    }

    /*
     * actions
     */
    public WOComponent voteGarath() {
        int votes = ((Number) valueForKeyPath("application.garath.votes")).intValue();

        // increment the votes
        takeValueForKeyPath(new Integer(++votes), "application.garath.votes");

        // save to database
        ((Application) application()).saveChanges();

        // debug
        NSLog.debug.appendln("Application: vote cast: garath");

        return pageWithName("Main");
    }

    public WOComponent voteWill() {
        int votes = ((Number) valueForKeyPath("application.will.votes")).intValue();

        // increment the votes
        takeValueForKeyPath(new Integer(++votes), "application.will.votes");

        // save to database
        ((Application) application()).saveChanges();

        // debug
        NSLog.debug.appendln("Application: vote cast: will");

        return pageWithName("Main");
    }    

    /*
     * accessors
     */
    public String transform_will() {
        float total = ((Number) valueForKeyPath("application.totalVotes")).floatValue();
        float votes = ((Number) valueForKeyPath("application.will.votes")).floatValue();
        float scale = votes/total;

        return "scale(" + scale + ")";
    }

    public String transform_garath() {
        float total = ((Number) valueForKeyPath("application.totalVotes")).floatValue();
        float votes = ((Number) valueForKeyPath("application.garath.votes")).floatValue();
        float scale = votes/total;

        return "scale(" + scale + ")";
    }    
}