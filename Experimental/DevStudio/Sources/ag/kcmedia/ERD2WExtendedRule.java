//
// ERD2WExtendedRule.java
// Project RuleEditor
//
// Created by ak on Fri Jun 21 2002
//
package ag.kcmedia;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.directtoweb.*;
import er.extensions.*;

public class ERD2WExtendedRule extends Rule implements ERXMutableUserInfoHolderInterface {
    public ERD2WExtendedRule() {
        super();
    }

    public ERD2WExtendedRule(EOKeyValueUnarchiver eokeyvalueunarchiver) {
        super(eokeyvalueunarchiver.decodeIntForKey("author"),
              ((EOQualifier) eokeyvalueunarchiver.decodeObjectForKey("lhs")),
              ((Assignment) eokeyvalueunarchiver.decodeObjectForKey("rhs")));
        NSDictionary dict = (NSDictionary)eokeyvalueunarchiver.decodeObjectForKey("userInfo");
        setAuthor(eokeyvalueunarchiver.decodeIntForKey("author"));
        if(dict != null)
            setMutableUserInfo(dict.mutableClone());
    }

    public static Object  decodeWithKeyValueUnarchiver(EOKeyValueUnarchiver eokeyvalueunarchiver) {
        ERD2WExtendedRule rule = null;
        try {
            rule = new ERD2WExtendedRule(eokeyvalueunarchiver);
        } catch(Throwable t) {
            throw new IllegalArgumentException("Error with this rule: " + NSKeyValueCoding.Utility.valueForKey(eokeyvalueunarchiver,"propertyList") + "," + t.getMessage());
        }
        return rule;
    }
    public void encodeWithKeyValueArchiver (EOKeyValueArchiver eokeyvaluearchiver) {
        super.encodeWithKeyValueArchiver(eokeyvaluearchiver);
        if(mutableUserInfo != null && mutableUserInfo.allKeys().count() > 0)
            eokeyvaluearchiver.encodeObject(mutableUserInfo, "userInfo");
        ((NSMutableDictionary)eokeyvaluearchiver.dictionary()).setObjectForKey("com.webobjects.directtoweb.Rule", "class");
    }
    
    NSMutableDictionary mutableUserInfo;
    public NSMutableDictionary mutableUserInfo() {
        if(mutableUserInfo == null) {
            mutableUserInfo = new NSMutableDictionary();
        }
        return mutableUserInfo;
    }
    public void setMutableUserInfo(NSMutableDictionary dict) {
        mutableUserInfo = dict;
    }

    int author;
    public int author() {
        return author;
    }
    
    public void setAuthor(int value) {
        author = value;
    }

    public String assignmentClassName() {
        return rhs().getClass().getName();
    }

    public ERD2WExtendedRule cloneRule() {
        EOKeyValueArchiver archiver = new EOKeyValueArchiver();
        encodeWithKeyValueArchiver(archiver);
        EOKeyValueUnarchiver unarchiver = new EOKeyValueUnarchiver(archiver.dictionary());

        return new ERD2WExtendedRule(unarchiver);
    }
}
