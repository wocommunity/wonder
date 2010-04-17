/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.pages;
import com.webobjects.appserver.WOContext;
import com.webobjects.eoaccess.EODatabaseDataSource;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eocontrol.EODataSource;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.foundation.NSDictionary;

/**
 * Query page utilizing fetch specifications.<br />
 * @deprecated use ERD2WQueryPage instead
 * 
 */
public class ERD2WQueryPageWithFetchSpecification extends ERD2WQueryPage {

    public ERD2WQueryPageWithFetchSpecification(WOContext context) {
        super(context);
    }

    private EOFetchSpecification _fetchSpecification;
    public EOFetchSpecification fetchSpecification() { return _fetchSpecification; }
    public void setFetchSpecification(EOFetchSpecification fs) {
        _fetchSpecification=fs;
    }

    public void setFetchSpecificationName(String name) {
        d2wContext().takeValueForKey(name,"fetchSpecificationName");
        //_fetchSpecificationName=name;
        EOEntity e=entity();
        setFetchSpecification(e.fetchSpecificationNamed(name));
    }

    public String fetchSpecificationName() {
        return (String)d2wContext().valueForKey("fetchSpecificationName");
    }

    public EOFetchSpecification queryFetchSpecification() {
        NSDictionary valuesFromBinding=displayGroup.queryMatch();
        return fetchSpecification().fetchSpecificationWithQualifierBindings(valuesFromBinding);
    }

    public EODataSource queryDataSource() {
        EODatabaseDataSource qds;
        if (dataSource()==null || !(dataSource() instanceof EODatabaseDataSource)) {
            qds=new EODatabaseDataSource(session().defaultEditingContext(), entity().name());
            setDataSource(qds);
        }
        else
            qds=(EODatabaseDataSource)dataSource();
        qds.setFetchSpecification(queryFetchSpecification());
        return qds;
    }
}