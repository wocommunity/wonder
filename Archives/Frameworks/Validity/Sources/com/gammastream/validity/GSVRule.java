package com.gammastream.validity;

import com.webobjects.appserver.xml.WOXMLCoder;
import com.webobjects.appserver.xml.WOXMLCoding;
import com.webobjects.appserver.xml.WOXMLDecoder;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;

/**
 *	This structure stores the various information which defines each rule.
 *	Information like the name, where the rule is located, error messages, 
 *	when to execute, etc.
 *
 *	@author GammaStream Technologies, Inc.
 */

public final class GSVRule implements WOXMLCoding {

    //definitions
    private String ruleName = null;
    private String cName = null;
    private String mName = null;
    private String errorMessage = null;
    private String documentation = null;

    //execution mutators
    private boolean negate = false;
    private boolean failIfNULL = true;
    private boolean stopIfFails = false;
    private boolean continueIfNULL = false;
    
    //when to execute
    private boolean onSave = true;
    private boolean onInsert = true;
    private boolean onDelete = true;
    private boolean onUpdate = true;
    
    //key-value list of parameters
    private NSMutableDictionary parameters = null;
    
   /**
     * 	Creates a new GSVRule with the provided parameters.
     *
     * @param  	rName		The name you wish to give the rule.
     * @param  	cName2		The fully qualified class name in which the rule's method is located.
     * @param  	mName2		The name of the method to be executed.
     * @param  	eMessage	An error message to provide the user in the event the rule fails.
     * @param  	doc		 	Optional documentation as to the rules function.
     * @exception java.lang.IllegalArgumentException	Thrown should the class name or method name appear to be invalid.
     */
    public GSVRule(String rName, String cName2, String mName2, String eMessage, String doc) throws IllegalArgumentException {
        
        if(!this.validateClassName(cName2)){
            throw new IllegalArgumentException("Invalid class named '"+ cName2 +"'");
        }
        
        if(!this.validateMethodName(mName2)){
            throw new IllegalArgumentException("Invalid method named '"+ mName2 +"'");
        }
        ruleName = rName;
        cName = cName2;
        mName = mName2;
        errorMessage = eMessage;
        documentation = doc;
        parameters = new NSMutableDictionary();
    }

    /**
     *	Private
     *	Determines whether the provided class name appears to be valid.
     */
    private boolean validateClassName(String cName){
        return true;
    }
    
    /**
     *	Private
     *	Determines whether the provided method name appears to be valid.
     */
    private boolean validateMethodName(String mName){
        return true;
    }

    /**
     *	Returns the name of this rule.
     *
     *	@return    The name of the rule.
     *	@see #setRuleName
     */
    public String ruleName(){
        return ruleName;
    }

    /**
     *	Sets the name of this rule.
     *
     *	@see #ruleName
     */
    public void setRuleName(String newRule) throws IllegalArgumentException {
        ruleName = newRule;
    }

    /**
     *	Returns the fully qualified class name of the class in which the method used 
     *	in this rule is located. :-)
     *
     *	@return    The name of the rule.
     *	@see #setCName
     */
    public String cName(){
        return cName;
    }

    /**
     *	Set the class name for this rule.
     *
     *	@param newClass fully qualified name of the class.
     * 	@exception java.lang.IllegalArgumentException	Thrown should the class name appear to be invalid.
     *													Currently the validation logic here always returns true.
     *	@see #cName
     */
    public void setCName(String newClass)  throws IllegalArgumentException {
        if(!this.validateClassName(newClass)){
            throw new IllegalArgumentException("Invalid class named '"+ newClass +"'");
        } else {
            cName = newClass;
        }
    }
    
    /**
     *	Returns the method name used for this rule.
     *
     *	@return    The method name.
     *	@see #setMName
     */
    public String mName(){
        return mName;
    }

    /**
     *	Set the method name for this rule.
     *
     *	@param newMethod name for the method.
     * 	@exception java.lang.IllegalArgumentException	Thrown should the method name appear to be invalid.
     *             Currently the validation logic here always returns true.
     *	@see #mName
     */
    public void setMName(String newMethod)  throws IllegalArgumentException {
        if(!this.validateMethodName(newMethod)){
            throw new IllegalArgumentException("Invalid method named '"+ newMethod +"'");
        } else {
            mName = newMethod;
        }
    }

    /**
     *	Returns the error message that should be displayed to the user
     *	when this rule fails to be validated.
     *
     *	@return    The error message.
     *	@see #setErrorMessage
     */
    public String errorMessage(){
        return errorMessage;
    }
    
    /**
     *	Set the error message.
     *
     *	@param newMessage the error message
     *
     *	@see #errorMessage
     */
    public void setErrorMessage(String newMessage){
        errorMessage = newMessage;
    }
    
    /**
     *	Returns the documentation for this rule.
     *
     *	@return    The documentation.
     *	@see #setDocumentation
     */
    public String documentation(){
        return documentation;
    }
    
    /**
     *	Set the documentation to the provided <code>String</code>.
     *
     *	@param doc the documentation.
     *
     *	@see #documentation
     */
    public void setDocumentation(String doc){
        documentation = doc;
    }

    /**
     *	Returns a dictionary of key-value pairs used for providing parameters
     *	to the validation rule's method. This parameter dictionary is
     *	passed to the method defined by <code>mName()</code>.
     *
     *	@return    The key-values pairs.
     *	@see #setParameters
     */
    public NSMutableDictionary parameters(){
        return parameters;
    }

    /**
     *	Assigns a key-value pair dictionary to this rule.
     *
     *	@param newParameters A dictionary of key-value pairs.
     *
     *	@see #parameters
     */
    public void setParameters(NSMutableDictionary newParameters){
        parameters = newParameters;
    }
    
    /**
     *	Should the outcome of this rule be negated (reversed).
     *	This might be useful if you have a method which returns <code>true</code> if you have a <code>String</code>
     *	which is empty or <code>null</code>. Your rule might indicate that you want this attribute to be
     *	required. If the method returns <code>true</code> if it is, you will want to reverse the outcome. Did that 
     *	make any sense?
     *
     *	@return	Whether the initial return value should be negated (reversed).
     *	@see #setNegate
     */
    public boolean negate(){
        return negate;
    }

    /**
     *	Assigns a key-value pair dictionary to this rule.
     *
     *	@param z A dictionary of key-value pairs.
     *
     *	@see #parameters
     */
    public void setNegate(boolean z){
        negate = z;
    }
    
    public boolean failIfNULL(){
        return failIfNULL;
    }

    public void setFailIfNULL(boolean z){
        failIfNULL = z;
    }
    
    public boolean continueIfNULL(){
        return continueIfNULL;
    }

    public void setContinueIfNULL(boolean z){
        continueIfNULL = z;
    }
    
    public boolean stopIfFails(){
        return stopIfFails;
    }

    public void setStopIfFails(boolean z){
        stopIfFails = z;
    }

    public boolean onSave(){
        return onSave;
    }

    public void setOnSave(boolean z){
        onSave = z;
    }

    public boolean onUpdate(){
        return onUpdate;
    }

    public void setOnUpdate(boolean z){
        onUpdate = z;
    }
    
    
    public boolean onInsert(){
        return onInsert;
    }

    public void setOnInsert(boolean z){
        onInsert = z;
    }
    
    public boolean onDelete(){
        return onDelete;
    }

    public void setOnDelete(boolean z){
        onDelete = z;
    }

    /********************************  WOXMLCoding Impl  ********************************/
    
    /**
     *	WOXMLCoding Impl
     *	
     *	@param	coder	WOXMLCoder
     *
     *	@see #GSVRule
     */
    public void encodeWithWOXMLCoder(WOXMLCoder coder) {
        coder.encodeObjectForKey(ruleName, "RuleName");
        coder.encodeObjectForKey(cName, "ClassName");
        coder.encodeObjectForKey(mName, "MethodName");
        coder.encodeObjectForKey(errorMessage, "ErrorMessage");
        coder.encodeObjectForKey(documentation, "Documentation");
        coder.encodeObjectForKey(new NSDictionary(parameters), "Parameters");
        coder.encodeBooleanForKey(negate, "Negate");
        coder.encodeBooleanForKey(failIfNULL, "FailIfNULL");
        coder.encodeBooleanForKey(continueIfNULL, "ContinueIfNULL");
        coder.encodeBooleanForKey(stopIfFails, "StopIfFails");
        coder.encodeBooleanForKey(onSave, "OnSave");
        coder.encodeBooleanForKey(onInsert, "OnInsert");
        coder.encodeBooleanForKey(onUpdate, "OnUpdate");
        coder.encodeBooleanForKey(onDelete, "OnDelete");
   }
    
    /**
     *	WOXMLCoding Impl
     *
     *	@param	decoder	WOXMLDecoder
     *
     *	@see #encodeWithWOXMLCoder
     */
    public GSVRule(WOXMLDecoder decoder) {
        ruleName = (String)decoder.decodeObjectForKey("RuleName");
        cName = (String)decoder.decodeObjectForKey("ClassName");
        mName = (String)decoder.decodeObjectForKey("MethodName");
        errorMessage = (String)decoder.decodeObjectForKey("ErrorMessage");
        documentation = (String)decoder.decodeObjectForKey("Documentation");
        parameters = new NSMutableDictionary((NSDictionary)decoder.decodeObjectForKey("Parameters"));
        negate = decoder.decodeBooleanForKey("Negate");
        failIfNULL = decoder.decodeBooleanForKey("FailIfNULL");
        continueIfNULL = decoder.decodeBooleanForKey("ContinueIfNULL");
        stopIfFails = decoder.decodeBooleanForKey("StopIfFails");
        onSave = decoder.decodeBooleanForKey("OnSave");
        onInsert = decoder.decodeBooleanForKey("OnInsert");
        onUpdate = decoder.decodeBooleanForKey("OnUpdate");
        onDelete = decoder.decodeBooleanForKey("OnDelete");
    }
    
    /**
	 *	WOXMLCoding Impl
	 */
    public Class classForCoder() {
        try {
            return Class.forName("com.gammastream.validity.GSVRule");
        } catch(ClassNotFoundException e) {
            return null;
        }
    }
}
