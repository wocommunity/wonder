/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import org.apache.log4j.*;
import java.util.*;

public class ERXLog4JConfiguration extends WOComponent {

    public ERXLog4JConfiguration(WOContext aContext) {
        super(aContext);
    }

    private Category _category;
    private String _filterString;
    private String _ruleKey;
    private String _categoryName;

    public boolean showAll=true;
    
    public Category category() { return _category; }
    public void setCategory(Category newValue) { _category=newValue; }
    
    public String filterString() { return _filterString; }
    public void setFilterString(String newValue) { _filterString=newValue; }

    public String categoryName() { return _categoryName; }
    public void setCategoryName(String newValue) { _categoryName=newValue; }

    public String ruleKey() { return _ruleKey; }
    public void setRuleKey(String newValue) { _ruleKey=newValue; }

    public final static EOSortOrdering NAME_SORT_ORDERING=new EOSortOrdering("name",
                                                                            EOSortOrdering.CompareAscending);
    public final static NSMutableArray SORT_BY_NAME=new NSMutableArray(NAME_SORT_ORDERING);

    // not obvious what the appropriate API is to get your parent in Log4J 1.1
    public Category parentForCategory(Category c) {
        Category result=null;
        String name=c.getName();
        int i=name.lastIndexOf('.');
        if (i!=-1) {
            String parentName=name.substring(0,i);
            result=Category.getInstance(parentName);
        }
        return result;
    }
    
    public NSArray categories() {
        NSMutableArray result=new NSMutableArray();
        for (Enumeration e=Category.getCurrentCategories(); e.hasMoreElements();) {
            Category cat=(Category)e.nextElement();
            while (cat!=null) {
                addCategory(cat, result);
                cat=parentForCategory(cat);
            }
        }
        EOSortOrdering.sortArrayUsingKeyOrderArray(result, SORT_BY_NAME);
        return result;
    }

    public void addCategory(Category cat, NSMutableArray result) {
        if ((filterString()==null ||
             filterString().length()==0 ||
             cat.getName().toLowerCase().indexOf(filterString().toLowerCase())!=-1) &&
            (showAll || cat.getPriority()!=null) &&
            !result.containsObject(cat)) {
            result.addObject(cat);
        }        
    }
    
    
    public WOComponent filter() { return null; }
    public WOComponent resetFilter() { _filterString=null; return null; }
    public WOComponent update() { return null; }
    public WOComponent showAll() { showAll=true; return null; }
    public WOComponent showExplicitelySet() { showAll=false; return null; }
    public WOComponent addCategory() {
        Category.getInstance(categoryName());
        setFilterString(categoryName());
        return null;
    }
    // This functionality depends on ERDirectToWeb's presence..    
    public WOComponent addRuleKey() {
        String prefix="er.directtoweb.rules."+ruleKey();
        Category.getInstance(prefix+".fire");
        Category.getInstance(prefix+".cache");
        setFilterString(prefix);
        return null;
    }


    
    public Integer debugLevel() { return ERXConstant.integerForInt(Priority.DEBUG.toInt()); }
    public Integer infoLevel() { return ERXConstant.integerForInt(Priority.INFO.toInt()); }
    public Integer warnLevel() { return ERXConstant.integerForInt(Priority.WARN.toInt()); }
    public Integer errorLevel() { return ERXConstant.integerForInt(Priority.ERROR.toInt()); }
    public Integer fatalLevel() { return ERXConstant.integerForInt(Priority.FATAL.toInt()); }
    public Integer unsetLevel() { return ERXConstant.MinusOneInteger; }

    public Integer categoryPriorityValue() {
        return category()!=null && category().getPriority()!=null ?
        ERXConstant.integerForInt(category().getPriority().toInt()) : ERXConstant.MinusOneInteger;
    }

    public boolean categoryIsNotDebug() { return category()!=null && category().getPriority()!=Priority.DEBUG; }
    public boolean categoryIsNotInfo() { return category()!=null && category().getPriority()!=Priority.INFO; }
    public boolean categoryIsNotWarn() { return category()!=null && category().getPriority()!=Priority.WARN; }
    public boolean categoryIsNotError() { return category()!=null && category().getPriority()!=Priority.ERROR; }
    public boolean categoryIsNotFatal() { return category()!=null && category().getPriority()!=Priority.FATAL; }

    
    public void setCategoryPriorityValue(Integer newValue) {
        int pr=newValue!=null ? newValue.intValue() : -1;
        category().setPriority(pr!=-1 ? Priority.toPriority(pr) : null);
    }
    
    private final static NSDictionary BG_COLORS=new NSDictionary(
                                                                 new Object[] {
                                                                     "#ffbbbb",
                                                                     "#eeccbb",
                                                                     "#ddddbb",
                                                                     "#cceebb",
                                                                     "#bbffbb"
                                                                 },
                                                                 new Object[] {
                                                                     ERXConstant.integerForInt(Priority.DEBUG.toInt()),
                                                                     ERXConstant.integerForInt(Priority.INFO.toInt()),
                                                                     ERXConstant.integerForInt(Priority.WARN.toInt()),
                                                                     ERXConstant.integerForInt(Priority.ERROR.toInt()),
                                                                     ERXConstant.integerForInt(Priority.FATAL.toInt()),
                                                                 });
    public String bgColor() {
        return (String)BG_COLORS.objectForKey(categoryPriorityValue());
    }

    public int indentLevel() {
        return ERXExtensions.numberOfOccurrencesOfCharInString('.',category().getName());
    }


}
