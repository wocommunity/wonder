package er.indexing.attributes;

import com.webobjects.eocontrol.*;

public class ERIValidationRule extends _ERIValidationRule {

    @SuppressWarnings("unused")
    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ERIValidationRule.class);

    public static final ERIValidationRuleClazz clazz = new ERIValidationRuleClazz();
    public static class ERIValidationRuleClazz extends _ERIValidationRule._ERIValidationRuleClazz {
        /* more clazz methods here */
    }

    public interface Key extends _ERIValidationRule.Key {}

    /**
     * Initializes the EO. This is called when an EO is created, not when it is 
     * inserted into an EC.
     */
    public void init(EOEditingContext ec) {
        super.init(ec);
    }
    
    // more EO methods here
}
