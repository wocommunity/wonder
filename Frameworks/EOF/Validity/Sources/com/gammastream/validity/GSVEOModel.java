package com.gammastream.validity;

import java.io.File;
import java.io.FileInputStream;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSPathUtilities;
import com.webobjects.foundation.NSPropertyListSerialization;

public class GSVEOModel {

    private NSDictionary model = null;
    private String path = null;
    private String name = null;
    private String eomodelVersion = null;
    private String adaptorName = null;
    private NSDictionary connectionDictionary = null;
    private NSArray entities = null;
    private NSArray gsveoEntities = null;
   
    public GSVEOModel(NSDictionary d, String p){
        model = d;
        path = p;
    }

    public GSVEOModel(String p) throws java.io.IOException{
            path=p;
            File f = new File(path+"/index.eomodeld");
            FileInputStream fis = new FileInputStream(f);
            byte[] bytes = new byte[(int)f.length()];
            fis.read(bytes);
            model = (NSDictionary)NSPropertyListSerialization.propertyListFromString(new String(bytes));
     }

    
    public String path(){
        return path;
    }
    
    public String name(){
        if(name != null)
            return name;
        name = NSPathUtilities.stringByDeletingPathExtension(NSPathUtilities.lastPathComponent(path));
        return name;
    }
    
    public String eomodelVersion(){
        if(eomodelVersion != null)
            return eomodelVersion;
        eomodelVersion = (String)model.objectForKey("EOModelVersion");
        return name;
    }
    
    public String adaptorName(){
        if(adaptorName != null)
            return adaptorName;
        adaptorName = (String)model.objectForKey("adaptorName");
        return name;
    }
    
    public NSDictionary connectionDictionary(){
        if(connectionDictionary != null)
            return connectionDictionary;
        connectionDictionary = (NSDictionary)model.objectForKey("connectionDictionary");
        return connectionDictionary;
    }
    
    public NSArray entities(){
        if(entities != null)
            return entities;
        entities = (NSArray)model.objectForKey("entities");
        return entities;
    }

    public NSArray gsveoEntities(){
        if(gsveoEntities != null)
            return gsveoEntities;
         NSMutableArray temp = new NSMutableArray();
         for(int i=0;i<entities().count();i++)
             temp.addObject(new GSVEOEntity(this, (String)((NSDictionary)(entities().objectAtIndex(i))).objectForKey("name")));
         gsveoEntities = temp;
         return gsveoEntities;
     }

    
    public GSVEOEntity entityNamed(String name){
        NSDictionary currentEntity = null;
        for(int i=0;i<this.entities().count();i++){
            currentEntity = (NSDictionary)this.entities().objectAtIndex(i);
            if(currentEntity.objectForKey("name").equals(name))
               return new GSVEOEntity(this,name);
        }
        return null;
    }
    
}
