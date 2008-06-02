//
// ERD2WQueryEncryptedString.java: Class file for WO Component 'ERD2WQueryEncryptedString'
// Project ERDirectToWeb
//
// Created by bposokho on Mon Jan 13 2003
//
package er.directtoweb;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.directtoweb.D2WQueryStringComponent;

import er.extensions.crypting.ERXCrypterInterface;

public class ERD2WQueryEncryptedString extends D2WQueryStringComponent {

    public ERD2WQueryEncryptedString(WOContext context) {
        super(context);
    }

    private String clearValue;
    public String clearValue(){
        return clearValue;
    }
    public void setClearValue(String newValue){
        clearValue = newValue;
    }
    
    private ERXCrypterInterface crypter(){
        return (ERXCrypterInterface)d2wContext().valueForKey("crypter");
    }

    public void takeValuesFromRequest(WORequest request, WOContext context) {
        super.takeValuesFromRequest(request, context);
        if(clearValue!=null){
            setValue(crypter().encrypt(clearValue));
        }
    }
}
