//
// ERD2WQueryEncryptedString.java: Class file for WO Component 'ERD2WQueryEncryptedString'
// Project ERDirectToWeb
//
// Created by bposokho on Mon Jan 13 2003
//
package er.directtoweb.components.strings;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.directtoweb.D2WQueryStringComponent;

import er.extensions.crypting.ERXCrypterInterface;

/**
 * @d2wKey crypter
 */
public class ERD2WQueryEncryptedString extends D2WQueryStringComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;


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

    @Override
    public void takeValuesFromRequest(WORequest request, WOContext context) {
        super.takeValuesFromRequest(request, context);
        if(clearValue!=null){
            setValue(crypter().encrypt(clearValue));
        }
    }
}
