/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.assignments.delayed;

import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.directtoweb.D2WContext;
import com.webobjects.directtoweb.Rule;
import com.webobjects.eocontrol.EOKeyValueUnarchiver;
import com.webobjects.eocontrol.EOQualifierEvaluation;
import com.webobjects.foundation.NSArray;

/**
 * DelayedRuleAssignment expects an array of rules as its value. The rules are
 * evaluated in turn until one returns a non-null result. This is done
 * every time that the propertyKey is requested thus making the rule system
 * a lot more dynamic.
 */

public class ERDDelayedRuleAssignment extends ERDDelayedAssignment {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public static Object decodeWithKeyValueUnarchiver(EOKeyValueUnarchiver eokeyvalueunarchiver)  {
        return new ERDDelayedRuleAssignment(eokeyvalueunarchiver);
    }
    
    /** Logging support */
    public final static Logger log = LoggerFactory.getLogger(ERDDelayedRuleAssignment.class);

    public ERDDelayedRuleAssignment(EOKeyValueUnarchiver u) { super(u); }
    public ERDDelayedRuleAssignment(String key, Object value) { super(key,value); }

    // FIXME: this assignment should in theory return all the keys in the LHS of the rule
    // as the dependent keys!!
    
    /**
     * This method is called whenever the propertyKey is requested,
     * but the value in the cache is actually a rule.
     */
    @Override
    public Object fireNow(D2WContext c) {
        Object result = null;
        NSArray rules = (NSArray)value();
        Enumeration ruleEnumerator = rules.objectEnumerator();
        Rule rule;
        while (ruleEnumerator.hasMoreElements()) {
            rule = (Rule)ruleEnumerator.nextElement();
            EOQualifierEvaluation eval = rule.lhs();
            log.debug("Qualifier eval: \n" + eval);
            if (eval.evaluateWithObject(c)) {
                result = rule.rhs().value();
                log.debug("RHS value: " +  result);
                break;
            }
            log.debug("    object.expansionRSF:" +
                               c.valueForKey("object") +
                               c.valueForKeyPath("object.expansionRSF"));
            if (c.valueForKeyPath("object.expansionRSF") == null)
                log.debug("It is null");
        }
        return result;
    }
}
