package com.gammastream.validity;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSValidation;

public class GSVExceptionRepetition extends WOComponent {

    public NSArray messageArray = null;
    
    private NSArray _attributeKeys = null;

    public GSVExceptionRepetition(WOContext context) {
        super(context);
    }

    /********************************  Overrides  *******************************/

    public boolean isStateless(){ return true; }
    public boolean synchronizesVariablesWithBindings(){ return false; }
    
    /********************************  Bindings  *******************************/

    public NSValidation.ValidationException exception(){
        return (NSValidation.ValidationException)this.valueForBinding("exception");
    }

    public boolean showAllErrors(){
        if(this.hasBinding("showAllErrors")){
            return ((Boolean)this.valueForBinding("showAllErrors")).booleanValue();
        }
        return false;
    }

    public NSArray attributeKeys(){
        if( _attributeKeys == null ){
            String key = (String)this.valueForBinding("attributeKey");
            _attributeKeys = NSArray.componentsSeparatedByString(key, ":");
        }
        return _attributeKeys;
    }

    public String currentMessage(){
        return (String)valueForBinding("currentMessage");
    }

    public void setCurrentMessage(String str){
        setValueForBinding(str, "currentMessage");
    }

    /********************************  Other  *******************************/
    
    public boolean show() {
        return exception() != null;
    }

    public NSDictionary messageDictionary(){
        if(this.show() && exception().userInfo().objectForKey(GSVEngine.ERROR_DICTIONARY_KEY) != null){
            return   (NSDictionary)exception().userInfo().objectForKey(GSVEngine.ERROR_DICTIONARY_KEY);
        }
        return null;
    }

    public NSArray messages(){
        if( showAllErrors() ){
            NSDictionary md = messageDictionary();
            if( md != null ) return md.allValues();
            else return NSArray.EmptyArray;
        } else {
            NSMutableArray array = new NSMutableArray();
            NSDictionary d2 = exception().userInfo();
            if( d2 != null ){
                NSDictionary d = (NSDictionary)d2.objectForKey(GSVEngine.ERROR_DICTIONARY_KEY);
                if( d != null ){
                    if( attributeKeys() != null ){
                        for( int i=0;i<attributeKeys().count();i++){
                            array.addObjectsFromArray((NSArray)d.objectForKey( attributeKeys().objectAtIndex(i) ));
                        }
                    }
                }
            }
            return array;
        }
    }

}
