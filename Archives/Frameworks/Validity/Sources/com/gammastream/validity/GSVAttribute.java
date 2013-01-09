package com.gammastream.validity;

import com.webobjects.appserver.xml.WOXMLCoder;
import com.webobjects.appserver.xml.WOXMLCoding;
import com.webobjects.appserver.xml.WOXMLDecoder;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

/**
 *	This structure maps validation rules to a specific attribute of an entity.
 *	For instance, we might wish to map a 'Provided text is > 5 characters' to
 *	the 'username' attribute of the 'user' entity.
 *
 *	@author GammaStream Technologies, Inc.
 */
public final class GSVAttribute implements WOXMLCoding {

    //persistant attribute attributes
    private GSVEntity entity;
    private String name;
    private NSMutableArray rules;

    /**
     *	Creates a new GSVAttribute with the provided information.
     *
     *	@param  	anEntity	The entity this attribute belongs to.
     *	@param  	aName		The name of this attribute.
     *	@exception java.lang.IllegalArgumentException	Thrown if the selected EOModel does not contain an attribute of the specified name in the provided entity.
     */
    public GSVAttribute(GSVEntity anEntity, String aName) throws IllegalArgumentException {
        if(this.validateAttributeForName(anEntity, aName)){
            entity = anEntity;
            name = aName;
            rules = new NSMutableArray();
        }else{
            throw new IllegalArgumentException("EOAttribute named '"+ aName +"' does not exist for EOEnity named '"+anEntity.name()+"' in EOModel for path: "+anEntity.model().eomodelPath());
        }
    }

    /**
     *	Private validation for checking for valid attributes.
     */
    private boolean validateAttributeForName(GSVEntity anEntity, String aName){
        try{
            GSVEOModel gsmodel = new GSVEOModel(anEntity.model().eomodelPath());
            GSVEOEntity eoEntity = (gsmodel).entityNamed(anEntity.name());
            return ( eoEntity.attributeNamed(aName) != null );
        }catch(java.io.IOException e){
            System.out.println(e);
            return false;
        }
    }
    
    /**
     *	Returns the name of this attribute.
     *
     *	@return    The name of the attribute.
     *	@see #setName
     */
    public String name(){
        return name;
    }

    /**
     *	Sets the name of the attribute.
     *
     *	@param	newName	The new name for the attribute.
     *	@see #name
     */
    public void setName(String newName){
        name = newName;
    }

    /**
     *	The list of rules assigned to this attribute.
     *
     *	@return	An NSArray of rules.
     *	@see #removeRule
     *	@see #addRule
     */
    public NSArray rules(){
        return rules;
    }
    
    /**
     *	Adds the rule to this attribute.
     *
     * @param	newRule	The rule to add.
     * @see #removeRule
     * @see #rules
     */
    public void addRule(GSVRule newRule){
        rules.addObject(newRule);
    }

    /**
     *	Remove the rule from this attribute.
     *
     *	@param	oldRule	The rule to remove.
     *	@see #addRule
     *	@see #rules
     */
    public void removeRule(GSVRule oldRule){
        rules.removeObject(oldRule);
    }
    
    
    /**
     *	Returns the rule with the provided name.
     *
     *	@param	name	The name of the rule you wish to fetch.
     *	@return	Returns the rule with the provided name or null if one was not found.
     */
    public GSVRule ruleNamed(String name){
        GSVRule currentrule = null;
        for(int i=0;i<rules.count();i++){
            currentrule = (GSVRule)rules.objectAtIndex(i);
            if(currentrule.ruleName().equals(name))
                return currentrule;
        }
        return null;
    }
    
    /**
     *	Returns the parent entity for this attribute.
     *
     *	@return parent entity for this attribute.
     *	@see #setEntity
     */
    public GSVEntity entity(){
       return entity;
    }
    
    /**
     *	Sets the parent entity for this attribute to the provided entity.
     *
     *	@param	parentEntity	The parent entity for this attribute.
     *	@see #entity
     */
    public void setEntity(GSVEntity parentEntity){
        entity = parentEntity;
    }
    
    /********************************  WOXMLCoding Impl  ********************************/
    
    /**
     *	WOXMLCoding Impl
     *	
     *	@param	coder	WOXMLCoder
     *
     *	@see #GSVAttribute
     */
    public void encodeWithWOXMLCoder(WOXMLCoder coder) {
        coder.encodeObjectForKey(entity, "Entity");
        coder.encodeObjectForKey(name, "Name");
        coder.encodeObjectForKey(new NSArray(rules), "Rules");
    }
    
    /**
     *	WOXMLCoding Impl
     *
     *	@param	decoder	WOXMLDecoder
     *
     *	@see #encodeWithWOXMLCoder
     */
    public GSVAttribute(WOXMLDecoder decoder) {
        entity = (GSVEntity)decoder.decodeObjectForKey("Entity");
        name = (String)decoder.decodeObjectForKey("Name");
        rules = new NSMutableArray((NSArray)decoder.decodeObjectForKey("Rules"));
    }
    
    /**
	 *	WOXMLCoding Impl
	 */
    public Class classForCoder() {
        try{
            return Class.forName("com.gammastream.validity.GSVAttribute");
        }catch(ClassNotFoundException e){
            return null;
        }
    }


}
