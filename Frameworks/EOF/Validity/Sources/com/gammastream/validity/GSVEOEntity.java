  package com.gammastream.validity;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSPropertyListSerialization;

public class GSVEOEntity {

    private GSVEOModel model = null;
    private NSDictionary entity = null;
    private NSArray attributes = null;
    private NSArray gsveoAttributes = null;
    private NSArray  attributesUsedForLocking = null;
    private String className = null;
    private String name = null;
    private String externalName = null;
    private NSArray classProperties = null;
    private NSArray primaryKeyAttributes = null;
    private NSArray relationships = null;

    public GSVEOEntity(GSVEOModel m, String n){
        model = m;
        name = n;
        try{
            File f = new File(model.path()+"/"+n+".plist");
            FileInputStream fis = new FileInputStream(f);
            byte[] bytes = new byte[(int)f.length()];
            fis.read(bytes);
            entity = (NSDictionary)NSPropertyListSerialization.propertyListFromString(new String(bytes));
        }catch(IOException e){System.out.println(e);}
    }

    public String name(){
        return name;
    }

    public NSArray attributes(){
        if(attributes != null)
            return attributes;
        attributes = (NSArray)entity.objectForKey("attributes");
        return attributes;
    }

    public NSArray gsveoAttributes(){
        if(gsveoAttributes != null)
            return gsveoAttributes;
        NSMutableArray temp = new NSMutableArray();
        if(attributes()!=null){
            for(int i=0;i<attributes().count();i++)
                temp.addObject(new GSVEOAttribute(this, (NSDictionary)(attributes().objectAtIndex(i))));
            gsveoAttributes = temp;
        }else{
            gsveoAttributes = new NSArray(); 
        }
        return gsveoAttributes;
    }

    public NSArray attributesUsedForLocking(){
        if(attributesUsedForLocking != null)
            return attributesUsedForLocking;
        attributesUsedForLocking = (NSArray)entity.objectForKey("attributesUsedForLocking");
        return attributesUsedForLocking;
    }

    public NSArray classProperties(){
        if(classProperties != null)
            return classProperties;
        classProperties = (NSArray)entity.objectForKey("classProperties");
        return classProperties;
    }

    public String className(){
        if(className != null)
            return className;
        className = (String)entity.objectForKey("className");
        return className;
    }

    public String externalName(){
        if(externalName != null)
            return externalName;
        externalName = (String)entity.objectForKey("externalName");
        return externalName;
    }

    public NSArray primaryKeyAttributes(){
        if(primaryKeyAttributes != null)
            return primaryKeyAttributes;
        primaryKeyAttributes = (NSArray)entity.objectForKey("primaryKeyAttributes");
        return primaryKeyAttributes;
    }

    public NSArray relationships(){
        if(relationships != null)
            return relationships;
        relationships = (NSArray)entity.objectForKey("relationships");
        return relationships;
    }

    public GSVEOModel model(){
        return model;
    }

    public GSVEOAttribute attributeNamed(String name){
        NSDictionary currentAttribute = null;
        for(int i=0;i<this.attributes().count();i++){
            currentAttribute = (NSDictionary)this.attributes().objectAtIndex(i);
            if(currentAttribute.objectForKey("name").equals(name))
                return new GSVEOAttribute(this,currentAttribute);
        }
        return null;
    }
	

}
