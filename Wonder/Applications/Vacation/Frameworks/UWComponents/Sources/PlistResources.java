//
// PlistResources.java
// Project UWComponents
//
// Created by amishra on Wed May 15 2002
//

package com.uw.shared;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;

import java.io.*;
import java.util.*;
import java.lang.*;

public class PlistResources extends Object implements NSKeyValueCoding {

    public static PlistResources resources;

    protected NSDictionary dictionary;

    public static PlistResources plistResources(String plistName) throws Exception {
        if (resources == null) {
            resources = new PlistResources(plistName);
        }
        return resources;
    }

    public PlistResources(String plistName) throws Exception {
        super();
        // get the path to the file named settings.plist
        String path = WOApplication.application().resourceManager().pathForResourceNamed(plistName,null,null);
        File file = new File( path);

        // read the file into a dictionary (hash table) using the NSPropertyListSerialization clas
            dictionary = (NSDictionary) NSPropertyListSerialization.propertyListFromString(stringFromFile(file));
    }

    public NSDictionary rootDictionary() {
        return dictionary;
    }
    
    // load settings from a text file
    public Object valueForKey(String key) {
        return dictionary.objectForKey(key);
    }

    public NSDictionary dictionaryForKey(String key) {
        return (NSDictionary) valueForKey(key);
    }

    public void takeValueForKey(Object object, String Key) {
    }

    // static method for reading text file in
    static public String stringFromFile(File f) throws IOException {
        if (f==null)
            throw new IOException("null file");
        int size=(int) f.length();
        FileInputStream fis=new FileInputStream(f);
        byte [] data = new byte[size];
        int bytesRead=0;
        while (bytesRead<size)
            bytesRead+=fis.read(data,bytesRead,size-bytesRead);
        fis.close();
        return new String(data);
    }
}
