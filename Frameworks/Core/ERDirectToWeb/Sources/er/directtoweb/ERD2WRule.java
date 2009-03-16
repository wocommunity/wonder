package er.directtoweb;

import com.webobjects.directtoweb.Assignment;
import com.webobjects.directtoweb.Rule;
import com.webobjects.eocontrol.EOAndQualifier;
import com.webobjects.eocontrol.EOKeyValueArchiver;
import com.webobjects.eocontrol.EOKeyValueUnarchiver;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableDictionary;

/**
 * Rule class that works around two problems:
 * <ul>
 * <li>when you have an assignment class that is not present in the classpath
 * then the model will not load, making for very strange errors. We replace the
 * missing class with the normal assignment class and log the error.
 * <li>when evaluating rule priorities, the default is to place rules containing <code>pageConfiguration</code>
 * keys so high up that they will get prefered over rules without such a condition, but with a higher author setting.
 * This is pretty ridiculous and leads to having to set <code>... AND (pageConfiguration like '*')</code> 
 * in all the conditions.<br>
 * We place rules with a <code>pageConfiguration</code> so high that they will be higher than rules with the same author setting
 * but lower than a rule with a higher setting.
 * </ul>
 * <br>In order to be usable with the D2WClient and Rule editor, we also patch the encoded 
 * dictionary so these tools find no trace of the patched rules.
 * @author ak
 */
public class ERD2WRule extends Rule {
    private int _priority = -1;
    private String _assignmentClassName;

    public ERD2WRule() {
        super();
    }

    public ERD2WRule(EOKeyValueUnarchiver eokeyvalueunarchiver) {
        super(eokeyvalueunarchiver.decodeIntForKey("author"),
                ((EOQualifier) eokeyvalueunarchiver.decodeObjectForKey("lhs")),
                ((Assignment) eokeyvalueunarchiver.decodeObjectForKey("rhs")));
        _assignmentClassName = (String)eokeyvalueunarchiver.decodeObjectForKey("assignmentClassName");
    }
    
    public static Object decodeWithKeyValueUnarchiver(EOKeyValueUnarchiver eokeyvalueunarchiver) {
        ERD2WRule rule = null;
        try {
            rule = new ERD2WRule(eokeyvalueunarchiver);
        } catch(Throwable t) {
            // AK: this occurs mostly when we want to load a rule that contains an assigment class which can't be found
            //HACK cheesy way to get at the encoded rule dictionary
            NSMutableDictionary dict = (NSMutableDictionary)NSKeyValueCoding.Utility.valueForKey(eokeyvalueunarchiver,"propertyList");
            String ruleString = dict.toString();
            // now store the old assignment class
            dict.takeValueForKeyPath(dict.valueForKeyPath("rhs.class"), "assignmentClassName");
            // and push in the default class
            dict.takeValueForKeyPath(Assignment.class.getName(), "rhs.class");
            // try again
            try {
                rule = new ERD2WRule(eokeyvalueunarchiver);
                ruleString = rule.toString();
                
            } finally {
                ERD2WModel.log.error("Problems with this rule: \n" +  t + "\n" + ruleString, t);
            }
        }
        return rule;
    }
    
    /**
     * Overridden to patch the normal rule class name into the generated dictionary.
     * @see com.webobjects.eocontrol.EOKeyValueArchiving#encodeWithKeyValueArchiver(com.webobjects.eocontrol.EOKeyValueArchiver)
     */
    public void encodeWithKeyValueArchiver (EOKeyValueArchiver eokeyvaluearchiver) {
        super.encodeWithKeyValueArchiver(eokeyvaluearchiver);
        ((NSMutableDictionary)eokeyvaluearchiver.dictionary()).setObjectForKey(Rule.class.getName(), "class");
    }
    
    /**
     * Overridden to work around 
     * @see com.webobjects.directtoweb.Rule#priority()
     */
    public int priority() {
        if(_priority == -1) {
            
            EOQualifier lhs = lhs();
            String lhsString = "";
                           
            _priority = 1000 * author();

            if(lhs != null) {
                lhsString = lhs.toString();
                if(lhsString.indexOf("dummyTrue") == -1) {
                    if(lhsString.indexOf("pageConfiguration") != -1) {
                        _priority += 500;
                    }
                    if(lhs() instanceof EOAndQualifier) {
                        _priority += ((EOAndQualifier)lhs()).qualifiers().count();
                    } else {
                        _priority ++;
                    }
                }
            }
        }
        return _priority;
    }

    public String assignmentClassName() {
        if(_assignmentClassName == null) {
            _assignmentClassName = rhs().getClass().getName();
        }
        return _assignmentClassName;
    }
    
    public ERD2WRule cloneRule() {
        EOKeyValueArchiver archiver = new EOKeyValueArchiver();
        encodeWithKeyValueArchiver(archiver);
        EOKeyValueUnarchiver unarchiver = new EOKeyValueUnarchiver(archiver.dictionary());
        
        return new ERD2WRule(unarchiver);
    }
    
    /**
     * Builds a string like:<br>
     * <pre><code>   100: ((entity.name = 'Bug') and (task = 'edit')) => isInspectable = true [com.directtowen.BooleanAssignment]</code></pre>
     * @return a nice description of the rule
     */
    public String toString() {
        String prefix = "      ";
        String rhsClass = assignmentClassName();
        return (
                prefix.substring(0, prefix.length() - ("" + author()).length()) + author() + " : " + 
                (lhs() == null ? "*true*" : lhs().toString()) +
                " => " +
                (rhs() == null ? "<NULL>" : rhs().keyPath() + " = " + rhs().value() +
                        ( rhsClass.equals(Assignment.class.getName()) ? "" : " [" + rhsClass + "]")
                ));
    }
}