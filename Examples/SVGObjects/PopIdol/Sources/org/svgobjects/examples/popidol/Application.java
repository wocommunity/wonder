//
// Application.java
// Project PopIdol1B
//
// Created by ravi on Mon Mar 18 2002
//
package org.svgobjects.examples.popidol;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;

public class Application extends WOApplication {
    private EOEditingContext editingContext = new EOEditingContext();
    public EOEnterpriseObject garath;
    public EOEnterpriseObject will;

    public static void main(String argv[]) {
        WOApplication.main(argv, Application.class);
    }

    /*
     * constructor
     */
    public Application() {
        super();
        System.out.println("Welcome to " + this.name() + "!");

        /* ** Put your application initialization code here ** */
        garath = EOUtilities.objectMatchingKeyAndValue(editingContext, "Idol", "name", "Garath Gates");
        will = EOUtilities.objectMatchingKeyAndValue(editingContext, "Idol", "name", "Will Young");
    }

    /*
     * actions
     */
    public void saveChanges() {
        editingContext.saveChanges();
    }

    /*
     * accessors
     */
    public int totalVotes() {
        int votes_garath = ((Number) will.valueForKey("votes")).intValue();
        int votes_will = ((Number) will.valueForKey("votes")).intValue();

        return votes_garath + votes_will;
    }
}
