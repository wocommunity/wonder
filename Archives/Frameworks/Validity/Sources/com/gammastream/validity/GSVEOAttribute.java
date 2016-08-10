package com.gammastream.validity;

import com.webobjects.foundation.NSDictionary;


public class GSVEOAttribute {

    private GSVEOEntity entity = null;
    private NSDictionary attribute = null;
    private String name = null;
    private String allowsNull = null;
    private String columnName = null;
    private String externalType = null;
    private String valueClassName = null;
    private Integer width = null;
    private String valueType = null;

    public GSVEOAttribute(GSVEOEntity e, NSDictionary n){
        entity = e;
        attribute = n;        
    }

    public String name(){
        if(name != null)
            return name;
        name = (String)attribute.objectForKey("name");
        return name;
    }

    public Integer width(){
        if(width != null)
            return width;
        String temp = (String)attribute.objectForKey("width");
        if(temp!=null)
            width = Integer.valueOf(temp);
        else
            width = Integer.valueOf(0);
        return width;
    }

    public String valueType(){
        if(valueType!= null)
            return valueType;
        valueType = (String)attribute.objectForKey("valueType");
        return valueType;
    }

    public String allowsNull(){
        if(allowsNull != null)
            return allowsNull;
        allowsNull = (String)attribute.objectForKey("allowsNull");
        return allowsNull;
    }

    public String columnName(){
        if(columnName != null)
            return columnName;
        columnName = (String)attribute.objectForKey("columnName");
        return columnName;
    }

    public String externalType(){
        if(externalType != null)
            return externalType;
        externalType = (String)attribute.objectForKey("externalType");
        return externalType;
    }

    public String valueClassName(){
        if(valueClassName != null)
            return valueClassName;
        valueClassName = (String)attribute.objectForKey("valueClassName");
        return valueClassName;
    }

    public GSVEOEntity entity(){
        return entity;
    }


}
