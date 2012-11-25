package com.gammastream.validity;
//javadoc *.java -author -package com.gammastream.validity -d api

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Enumeration;

import com.webobjects.appserver.xml.WOXMLCoder;
import com.webobjects.appserver.xml.WOXMLCoding;
import com.webobjects.appserver.xml.WOXMLDecoder;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSLog;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSPathUtilities;

/**
 *	GSVModel used for the mapping validation rules to an entities' attributes.<BR>
 *	The structure is very similar to Apple's EOModel.
 *
 *	@author GammaStream Technologies, Inc.
 */
public final class GSVModel implements WOXMLCoding {


    /********************************  STATIC  ********************************/

    /**
     *	The name of the GSVModel file included in the '.eomodeld' wrapper.<BR>
     *	Currently the full name is: 'Validity.model'
     */
    public static final String MODEL_NAME = "Validity";
    
    /**
     *	The extension of the GSVModel file included in the '.eomodeld' wrapper.<BR>
     *	Currently the full name is: 'Validity.model'
     */
    public static final String MODEL_EXTENSION = "model";
    
    
    /********************************  INSTANCE  ********************************/
    
    //persistant model attributes
    private NSMutableArray _entities = null;
    private String _eomodelPath = null;
    private String _eomodelName = null;
    private boolean inited = false;
    
    //helper for mapping the GSVModel to an EOModel
    private EOModelGroup _eomodelGroup = null;
    
    /**
     *	Creates a new GSVModel using the path to an '.eomodeld' file.<BR>
     *	<BR>
     *	Example of creating a GSVModel for the Movies example which ships with WebObjects.<BR>
     *	<BR>
     *	<blockquote>
     *	<code>
     *		EOModel eoModel = EOModelGroup.defaultGroup().modelNamed("Moview");<BR>
     * 		GSVModel model = new GSVModel(eoModel.path());<BR>
     *	</code>
     *	</blockquote>
     *
     *	@param path										Path to an '.eomodeld' file.
     *	@exception java.lang.IllegalArgumentException	Thrown if valid '.eomodeld' file does not exist at the specified path.
     */
    public GSVModel(String path) throws IllegalArgumentException {
        if(this.validateEOModelForPath(path)){
            _entities = new NSMutableArray();
            _eomodelPath = path;
            _eomodelName = NSPathUtilities.stringByDeletingPathExtension(NSPathUtilities.lastPathComponent(_eomodelPath));
            _eomodelGroup = new EOModelGroup();
            _eomodelGroup.addModelWithPath(_eomodelPath);
            //this.saveModel();
        } else {
            throw new IllegalArgumentException("Could not find a valid EOModel at: " + path);
        }
    }
    
    /**
     *	Determines that there is indeed a file located at the given path.
     * 
     *	@return         <code>true</code> if an '.eomodeld' file is located at the given path; otherwise, we return <code>false</code>.
     *	@param path		Path to the desired '.eomodeld' file.
     * 
     */
    public boolean validateEOModelForPath(String path){
        try {
            File f = new File(path);
            return f.exists();
        } catch(Exception e){
            return false;
        }
    }
    
    /**
     *	Saves the GSVModel to inside the '.eomodeld' file wrapper.
     *
     *	@return			<code>true</code> if save is successful; otherwise, returns <code>false</code>.
     */
    public boolean saveModel(){
        String codedString = WOXMLCoder.coder().encodeRootObjectForKey(this, "Model");
        String fullFileName = NSPathUtilities.stringByAppendingPathExtension(GSVModel.MODEL_NAME, GSVModel.MODEL_EXTENSION);
        String xmlPath = NSPathUtilities.stringByAppendingPathComponent(_eomodelPath, fullFileName);
        try {
            File configurationFile = new File(xmlPath);

            OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(configurationFile), "UTF-8");
            osw.write(codedString);
            osw.close();
	    NSLog.out.appendln("did save model to file");
            return true;
        } catch(IOException e) {
            NSLog.out.appendln(e);
            return false;
        }
    }
    
    /**
     *	Returns the EOModelGroup that is being used to query information from the EOModel.
     *
     *	@return		EOModelGroup for the EOModel.
     */
    public EOModelGroup eomodelGroup(){
        return _eomodelGroup;
    }

    /**
     *	Returns the EOModel Name for the associated GSVModel.
     *
     *	@return		Name of the EOModel
     */
    public String eomodelName(){
        return _eomodelName;
    }

    /**
     *	Returns the path of the '.eomodeld' file.
     *
     *	@return		Path of the EOModel File.
     *	@see #setEomodelPath
     */
    public String eomodelPath(){
        return _eomodelPath;
    }
    
    /**
     *	Set the path of the EOModel file.
     *
     *	@param path	Path of the EOModel File.
     *	@see #eomodelPath
     */

    public void setEomodelPath(String path){
        _eomodelPath = path;
    }
    
    /**
     *	Returns the NSArray of GSVEntity Objects.
     *
     *	@return		NSArray of GSVEntity Objects.
     *	@see #addEntity
     *	@see #removeEntity
     */
    
    public NSArray entities(){
        return _entities;
    }
    
    /**
     *	Adds a GSVEntity object to this GSVModel.  The GSVEntity must not already exsit.<BR>
     *	If it does, IllegalArgumentException is thrown.
     *
     *	@param		newEntity    GSVEntity object
     *	@exception       java.lang.IllegalArgumentException  GSVEnitity already exsits.
     *	@see #entities
     *	@see #removeEntity
     */
    public void addEntity(GSVEntity newEntity) throws IllegalArgumentException{
        GSVEntity currentEntity = null;
        for(int i=0;i<_entities.count();i++){
            currentEntity = (GSVEntity)_entities.objectAtIndex(i);
            if(currentEntity.name().equals(newEntity.name())){
                throw new IllegalArgumentException("Entity for name '"+newEntity.name()+"' already exsits in model named "+this.eomodelName());
            }
        }
        _entities.addObject(newEntity);
    }

    /**
     *	Removes a GSVEntity object from this GSVModel.
     *
     *	@param		oldEntity    GSVEntity object to remove.
     *	@see #entities
     *	@see #addEntity
     */
    public void removeEntity(GSVEntity oldEntity){
        _entities.removeObject(oldEntity);
    }

    /**
     *	Returns the GSVEntity object associated with the provided EOEnterpriseObject.
     *
     *	@param			object EOEnterpriseObject
     *	@return 		GSVEntity if the EOEnterpriseObject has an GSVEntity Null if not.
     *	@see #entityNamed
     */
    public GSVEntity entityForObject(Object object){
        if(object instanceof EOEnterpriseObject){
            GSVEntity currentEntity = null;
            for(int i=0;i<_entities.count();i++){
                currentEntity = (GSVEntity)_entities.objectAtIndex(i);
                if(currentEntity.name().equals(((EOEnterpriseObject)object).entityName()))
                    return currentEntity;
            }
        }
        return null;
    }
    
    /**
     *	Returns the GSVEntity object for the provided entity name.
     *
     *	@param		name name of entity
     *	@return 		GSVEntity if an GSVEntity is named name, <code>null</code> if it could not be found.
     *	@see #entityForObject
     */
    public GSVEntity entityNamed(String name){
        GSVEntity currentEntity = null;
        for(int i=0;i<_entities.count();i++){
            currentEntity = (GSVEntity)_entities.objectAtIndex(i);
            if(currentEntity.name().equals(name))
               return currentEntity;
        }
        return null;
    }
    
    /**
     *	Returns an NSArray containing the GSVEntity names.
     *
     *	@return 		NSArray names of GSVEntities.
     */
    public NSArray entityNames(){
        NSMutableArray names = new NSMutableArray();
        GSVEntity currentEntity = null;
        for(int i=0;i<_entities.count();i++){
            currentEntity = (GSVEntity)_entities.objectAtIndex(i);
            names.addObject(currentEntity.name());
        }
        return names;
    }
    
    /**
     *	Internal method for saving paths
     */
    public void savePath(String s){
        _eomodelPath = s;
        _eomodelGroup.addModelWithPath(_eomodelPath);
        this.saveModel();
    }
    
    /********************************  WOXMLCoding Impl  ********************************/

    /**
     *	WOXMLCoding Impl
     *	
     *	@param	coder	WOXMLCoder
     *
     *	@see #GSVModel
     */
    public void encodeWithWOXMLCoder(WOXMLCoder coder) {
        coder.encodeObjectForKey(_entities.immutableClone(), "Entities");
        coder.encodeObjectForKey(_eomodelName, "EOModelName");
        coder.encodeObjectForKey(_eomodelPath, "EOModelPath");
    }

    /**
     *	WOXMLCoding Impl
     *
     *	@param	decoder	WOXMLDecoder
     *
     *	@see #encodeWithWOXMLCoder
     */
    public GSVModel(WOXMLDecoder decoder) {
        _entities = new NSMutableArray((NSArray)decoder.decodeObjectForKey("Entities"));
       _eomodelName = (String)decoder.decodeObjectForKey("EOModelName");
        
        //We changed "Name" to "EOModelName", so for backward compatibility, if "EOModelName"
        //doesn't exist, we have to check for "Name"
        if( _eomodelName == null ){
            _eomodelName = (String)decoder.decodeObjectForKey("Name");
        }
        _eomodelGroup = new EOModelGroup();
        _eomodelPath = (String)decoder.decodeObjectForKey("EOModelPath");
    }
    

    /**
     *	WOXMLCoding Impl
     */
    public Class classForCoder() {
        try{
            return Class.forName("com.gammastream.validity.GSVModel");
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public void init(EOModel eomodel) {
	if (!inited) {
	    inited = true;
	    for (Enumeration e = entities().immutableClone().objectEnumerator(); e.hasMoreElements();) {
		GSVEntity entity = (GSVEntity)e.nextElement();
                String name = entity.name();
		EOEntity eoentity = eomodel.entityNamed(name);
		//NSLog.debug.appendln("checking gsventity"+entity.name());
		if ( eoentity == null) {
		    removeEntity(entity);
		    //NSLog.debug.appendln("removed obsolete gsventity"+entity.name());
		} else {
		    entity.init(this, eoentity);
		}
	    }
	}
    }
    
}
