package com.gammastream.validity;

import com.webobjects.appserver.xml.*;
import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import java.lang.*;
import java.util.*;
import java.net.*;
import java.math.*;

/**
 *	GSVEntity is the data structure associated with a paritcular table or EOEntity in an EOModel.
 *
 *	@author GammaStream Technologies, Inc.
 */
public final class GSVEntity extends Object implements WOXMLCoding {

    //persistant entity attributes
    private GSVModel _model;
    private String _name;
    private NSMutableArray _attributes;

    /**
     *	Creates a new GSVEntity.
     *
     *	@param		model		The GSVModel this GSVEntity belongs to.
     *	@param		entityname	The name of the GSVEntity.
     *	@exception java.lang.IllegalArgumentException	EOEntity does not exsit in EOModel
     */
    public GSVEntity(GSVModel model, String entityname) throws IllegalArgumentException{
        if(this.validateEntityForName(model,entityname)){
            _model = model;
            _name = entityname;
            _attributes = new NSMutableArray();
        }else{
            throw new IllegalArgumentException("EOEntity named '"+ entityname +"' does not exist in EOModel for path: "+model.eomodelPath());
        }
    }

    /**
     *	Private
     */
    private boolean validateEntityForName(GSVModel model, String name){
        return ( model.eomodelGroup().modelNamed(model.eomodelName()).entityNamed(name)!=null );
    }
    
    /**
     *	Returns the GSVModel the GSVEntity belongs to.
     *
     *	@return    GSVModel the GSVEntity belongs to.
     *	@see #setModel
     */
    public GSVModel model(){
        return _model;
    }

    /**
     *	Sets the GSVModel the GSVEntity belongs to.
     *
     *	@param   newModel  GSVModel for the GSVEntity
     *	@see #model
     */
    public void setModel(GSVModel newModel){
        _model = newModel;
    }

    /**
     *	Returns the name of the GSVEntity.
     *
     *	@return    Returns the name of the GSVEntity.
     */
    public String name(){
        return _name;
    }

    /**
     *	Returns the attributes for this entity.
     *
     *	@return    Returns the GSVEntity attributes.
     *	@see #addAttribute
     *	@see #removeAttribute
     */
    public NSMutableArray attributes(){
        return _attributes;
    }

    /**
     *	Adds the attribute to this entity.
     *
     *	@param  newAtt	The attribute to add.
     *	@see #attributes
     *	@see #removeAttribute
     */
    public void addAttribute(GSVAttribute newAtt){
        _attributes.addObject(newAtt);
    }

    /**
     *	Removes the attribute from this entity.
     *
     *	@param  oldAtt	The attribute to remove.
     *	@see #attributes
     *	@see #addAttribute
     */
    public void removeAttribute(GSVAttribute oldAtt){
        _attributes.removeObject(oldAtt);
    }

    /**
     *	Returns the attribute with the provided name.
     *
     *	@param	name	The name of the attribute you wish to fetch.
     *	@return	Returns the attribute witht he provided name or null if one was not found.
     */
    public GSVAttribute attributeNamed(String name){
        GSVAttribute currentAttribute = null;
        for(int i=0;i<_attributes.count();i++){
            currentAttribute = (GSVAttribute)_attributes.objectAtIndex(i);
            if(currentAttribute.name().equals(name))
                return currentAttribute;
        }
        return null;
    }
    
    /********************************  WOXMLCoding Impl  ********************************/
    
    /**
     *	WOXMLCoding Impl
     *	
     *	@param	coder	WOXMLCoder
     *
     *	@see #GSVEntity
     */
    public void encodeWithWOXMLCoder(WOXMLCoder coder) {
        coder.encodeObjectForKey(_model, "Model");
        coder.encodeObjectForKey(_name, "Name");
        coder.encodeObjectForKey(_attributes.immutableClone(), "Attributes");
	}
    
    /**
     *	WOXMLCoding Impl
     *
     *	@param	decoder	WOXMLDecoder
     *
     *	@see #encodeWithWOXMLCoder
     */
	public GSVEntity(WOXMLDecoder decoder) {
        _model = (GSVModel)decoder.decodeObjectForKey("Model");
        _name = (String)decoder.decodeObjectForKey("Name");
        _attributes = new NSMutableArray((NSArray)decoder.decodeObjectForKey("Attributes"));
	}
   
	/**
	 *	WOXMLCoding Impl
	 */
    public Class classForCoder() {
        try{
            return Class.forName("com.gammastream.validity.GSVEntity");
        }catch(ClassNotFoundException e){
            return null;
        }
    }

    public void init(GSVModel model, EOEntity eoentity) {
	NSArray myattributes = attributes().immutableClone();
	for ( Enumeration e = myattributes.objectEnumerator(); e.hasMoreElements();) {
	    GSVAttribute attribute = (GSVAttribute)e.nextElement();
	    //NSLog.debug.appendln("checking attribute"+eoentity.name()+"."+attribute.name());
	    EOAttribute a = eoentity.attributeNamed(attribute.name());
	    EORelationship p = eoentity.relationshipNamed(attribute.name());
	    
	    if (a == null) {
		NSLog.out.appendln("attribute "+attribute.name() + " does not exist in entity " + name() + " (anymore?), deleted from Valididy model");
		removeAttribute(attribute);
	    } else if (p != null) {
		NSLog.out.appendln("attribute "+attribute.name() + " in entity " + name() + " is (now?) an relationship which cannot have a validation rule, deleted from Valididy model");
		removeAttribute(attribute);
	    } else {
                EOEditingContext ec = new EOEditingContext();
                ec.lock();
		try {
		    if (attribute == null) {
			NSLog.debug.appendln("attribute == null");
		    } else if (attribute.name() == null) {
			NSLog.debug.appendln("attribute.name() == null, attribute = "+attribute);
		    } else {
			//NSLog.debug.appendln("checking eo="+eoentity.name()+", attributename="+attribute.name());
			EOClassDescription eoclassdescription = EOClassDescription.classDescriptionForEntityName(eoentity.name());
			EOEnterpriseObject eoenterpriseobject = eoclassdescription.createInstanceWithEditingContext(null, null);
                        eoenterpriseobject.valueForKeyPath(attribute.name());
                        
		    }
		} catch (com.webobjects.foundation.NSKeyValueCoding.UnknownKeyException e1) {
		    //NSLog.debug.appendln(e1);
		    NSLog.out.appendln("attribute "+attribute.name() + " does not exist in entity " + name() + " anymore, deleted from Valididy model");
		    removeAttribute(attribute);
                } catch (Exception e1) {
                    
                } finally {
                    ec.unlock();
                    ec.dispose();
                    
                }
	    }
	}
    }
}
