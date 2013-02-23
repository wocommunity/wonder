//
// ERD2WExtendedRule.java
// Project RuleEditor
//
// Created by ak on Fri Jun 21 2002
//
package ag.kcmedia;

import org.apache.log4j.Logger;

import com.webobjects.directtoweb.Assignment;
import com.webobjects.directtoweb.Rule;
import com.webobjects.eocontrol.EOKeyValueArchiver;
import com.webobjects.eocontrol.EOKeyValueUnarchiver;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.foundation.ERXMutableUserInfoHolderInterface;

public class ERD2WExtendedRule extends Rule implements ERXMutableUserInfoHolderInterface {
    private static Logger log = Logger.getLogger(DirectAction.class);

    public ERD2WExtendedRule() {
        super();
    }

    public ERD2WExtendedRule(EOKeyValueUnarchiver eokeyvalueunarchiver) {
        super(eokeyvalueunarchiver.decodeIntForKey("author"),
              ((EOQualifier) eokeyvalueunarchiver.decodeObjectForKey("lhs")),
              ((Assignment) eokeyvalueunarchiver.decodeObjectForKey("rhs")));
        NSDictionary dict = (NSDictionary)eokeyvalueunarchiver.decodeObjectForKey("userInfo");
        setAuthor(eokeyvalueunarchiver.decodeIntForKey("author"));
        assignmentClassName = (String)eokeyvalueunarchiver.decodeObjectForKey("assignmentClassName");
        if(dict != null)
            setMutableUserInfo(dict.mutableClone());
    }

    public static Object  decodeWithKeyValueUnarchiver(EOKeyValueUnarchiver eokeyvalueunarchiver) {
        ERD2WExtendedRule rule = null;
        try {
            rule = new ERD2WExtendedRule(eokeyvalueunarchiver);
        } catch(Throwable t) {
            NSMutableDictionary dict = (NSMutableDictionary)NSKeyValueCoding.Utility.valueForKey(eokeyvalueunarchiver,"propertyList");
            log.info("Problems with this rule: " + dict + "," + t.getMessage());
            dict.takeValueForKeyPath(dict.valueForKeyPath("rhs.class"), "assignmentClassName");
            dict.takeValueForKeyPath("com.webobjects.directtoweb.Assignment", "rhs.class");
            rule = new ERD2WExtendedRule(eokeyvalueunarchiver);
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

    String assignmentClassName;
    public String assignmentClassName() {
        if(assignmentClassName == null) {
            assignmentClassName = rhs().getClass().getName();
        }
        return assignmentClassName;
    }
    
    public ERD2WExtendedRule cloneRule() {
        EOKeyValueArchiver archiver = new EOKeyValueArchiver();
        encodeWithKeyValueArchiver(archiver);
        EOKeyValueUnarchiver unarchiver = new EOKeyValueUnarchiver(archiver.dictionary());

        return new ERD2WExtendedRule(unarchiver);
    }

    public String description() {
        String prefix = "      ";
        String authorString = "" + author();
        String rhsClass = assignmentClassName();
        return (
                prefix.substring(0, prefix.length() - ("" + author()).length()) + author() + " : " + 
                (lhs() == null ? "*true*" : lhs().toString()) +
                " => " +
                (rhs() == null ? "<NULL>" : rhs().keyPath() + " = " + rhs().value() +
                 ( rhsClass.equals("com.webobjects.directtoweb.Assignment") ? "" : " [" + rhsClass + "]")
                ));
    }
}
