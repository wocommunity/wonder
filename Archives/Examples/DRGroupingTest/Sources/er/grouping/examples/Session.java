//
// Session.java
// Project DRGroupingTestJava
//
// Created by dneumann on Tue Oct 02 2001
//
package er.grouping.examples;

import com.webobjects.eoaccess.EODatabaseDataSource;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.foundation.NSArray;

import er.extensions.appserver.ERXSession;
import er.grouping.DRReportModel;

public class Session extends ERXSession {

    protected String _selectedGroupingCriteriaString = "category";
    protected DRReportModel _reportModel; // report model for EOs

    protected NSArray _attribArray = new NSArray(); // for grouping of fetched EOs in memory
    protected NSArray _critArray = new NSArray(); // for grouping of fetched EOs in memory

    public NSArray _objects; // EOs fetched with a fetch spec

    public Session() {
        super();
        getObjects ();
        /* ** Put your per-session initialization code here ** */
    }


    public void getObjects(){
        EODatabaseDataSource ds = new EODatabaseDataSource(defaultEditingContext(), "Movie");
        EOFetchSpecification fs = ds.fetchSpecification();
        fs.setPrefetchingRelationshipKeyPaths(new NSArray("studio"));
        _objects = ds.fetchObjects();
        System.out.println("getRawRows: objects: "+ _objects.count());
    }

    public DRReportModel reportModel() {
        return _reportModel;
    }
    public void setReportModel(DRReportModel v){
        _reportModel = v;
    }
    
    public NSArray critArray() {
        return _critArray;
    }
    public void setCritArray(NSArray v){
        _critArray = v;
    }

    public NSArray attribArray() {
        return _attribArray;
    }
    public void setAttribArray(NSArray v){
        _attribArray = v;
    }

    public NSArray objects() {
        return _objects;
    }
    public void setObjects(NSArray v){
        _objects = v;
    }

    public String selectedGroupingCriteriaString() {
        return _selectedGroupingCriteriaString;
    }
    public void setSelectedGroupingCriteriaString(String v){
        _selectedGroupingCriteriaString = v;
    }
    
}
