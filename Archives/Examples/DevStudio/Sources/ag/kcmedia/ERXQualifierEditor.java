//
// ERXQualifierEditor.java: Class file for WO Component 'ERXQualifierEditor'
// Project RuleEditor
//
// Created by ak on Thu Jun 20 2002
//
package ag.kcmedia;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.eocontrol.EOAndQualifier;
import com.webobjects.eocontrol.EONotQualifier;
import com.webobjects.eocontrol.EOOrQualifier;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;

public class ERXQualifierEditor extends WOComponent {

    public ERXQualifierEditor(WOContext context) {
        super(context);
    }

    public boolean isStateless() { return true;}
    public boolean synchronizesVariablesWithBindings() { return false;}

    public int index;
    public EOQualifier qualifier;
    public EOQualifier currentQualifier;

    public void reset() {
        super.reset();
        qualifier = null;
    }

    public EOQualifier childQualifier() {
      EOQualifier childQualifier;
      if (qualifier instanceof EONotQualifier) {
        childQualifier = ((EONotQualifier)qualifier).qualifier();
      }
      else {
        childQualifier = null;
      }
      return childQualifier;
    }
    
    public NSArray<EOQualifier> childQualifiers() {
      NSArray<EOQualifier> childQualifiers;
      if (qualifier instanceof EOAndQualifier) {
        childQualifiers = ((EOAndQualifier)qualifier).qualifiers();
      }
      else if (qualifier instanceof EOOrQualifier) {
        childQualifiers = ((EOOrQualifier)qualifier).qualifiers();
      } 
      else {
        childQualifiers = null;
      }
      return childQualifiers;
    }
    
    public EOQualifier qualifier() {
        if(qualifier == null) {
            qualifier = (EOQualifier)valueForBinding("qualifier");
        }
        return qualifier;
    }

    public boolean isArrayQualifier() {
        if (qualifier() instanceof EOAndQualifier)
            return true;
        if (qualifier() instanceof EOOrQualifier)
            return true;
        return false;
    }

    public boolean isNegateQualifier() {
        if (qualifier() instanceof EONotQualifier)
            return true;
        return false;
    }

    public boolean isSimpleQualifier() {
        if (isArrayQualifier() || isNegateQualifier() )
            return false;
        return true;
    }

    public boolean isFirstRow() {
        return index == 0;
    }

    public String qualifierClass() {
        if (qualifier() instanceof EOAndQualifier)
            return "and";
        if (qualifier() instanceof EOOrQualifier)
            return "or";
        if (qualifier() instanceof EONotQualifier)
            return "not";
        return "error";
    }
    public String qualifierKind() {
        if (qualifier() instanceof EOAndQualifier)
            return "A<br>N<br>D";
        if (qualifier() instanceof EOOrQualifier)
            return "O<br>R";
        if (qualifier() instanceof EONotQualifier)
            return "NOT";
        return "ERROR";
    }
}
