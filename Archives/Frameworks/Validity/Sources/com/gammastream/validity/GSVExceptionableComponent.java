package com.gammastream.validity;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSValidation;


public abstract class GSVExceptionableComponent extends WOComponent {

    private NSValidation.ValidationException _exception;

    public  GSVExceptionableComponent(WOContext context){
	super(context);
    }

    public void sleep(){
        _exception = null;
        super.sleep();
    }

    public void raiseGSVException(NSValidation.ValidationException e){
        _exception = e;
    }

    public NSValidation.ValidationException exception(){
        return _exception;
    }

    public void setException(NSValidation.ValidationException exception){
        _exception = exception;
    }

}
