
package org.ganymede.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.appserver.WOContext;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;

import er.extensions.components.ERXComponent;
import er.extensions.eof.ERXEC;

public class AddTestResultPage extends ERXComponent {
    private static final Logger log = LoggerFactory.getLogger(AddTestResultPage.class);

    public AddTestResultPage(WOContext context) {
        super(context);
    }

    @Override
    public void awake() {

        EOEditingContext ec = ERXEC.newEditingContext();

        EOEnterpriseObject result = EOUtilities.createAndInsertInstance(ec, "Result");

        NSDictionary<String,NSArray<Object>> values = context().request().formValues();

        for (String key : values.allKeys()) {

            if (key.equals("when")) {
                String when = values.objectForKey(key).get(0).toString();
                result.takeValueForKey(when, "whence");
            }

            if (key.equals("timezone")) {
                String when = values.objectForKey(key).get(0).toString();
                result.takeValueForKey(when, "timeZone");
            }

            if (key.equals("duration")) {
                Number duration = Long.valueOf(values.objectForKey(key).get(0).toString());
                result.takeValueForKey(duration, "duration");
            }

            if (key.equals("email")) {
                String email = values.objectForKey(key).get(0).toString();
                result.takeValueForKey(email, "email");
            }

            if (key.startsWith("env")) {
                String env = values.objectForKey(key).get(0).toString();
                NSArray<EOEnterpriseObject> found = EOUtilities.objectsMatchingKeyAndValue(ec, "Environment", "info", env);
                EOEnterpriseObject eo;
                if (found == null || found.size() == 0) {
                    eo = EOUtilities.createAndInsertInstance(ec, "Environment");
                    eo.takeValueForKey(env, "info");
                } else {
                    eo = found.get(0);
                }
                result.addObjectToBothSidesOfRelationshipWithKey(eo, "environments");
            }

            if (key.startsWith("vers")) {
                NSArray<String> parts = NSArray.componentsSeparatedByString(values.objectForKey(key).get(0).toString(), " ");
                EOEnterpriseObject eo = EOUtilities.createAndInsertInstance(ec, "VersionDigest");
                eo.takeValueForKey(parts.get(1), "rname");
                eo.takeValueForKey(parts.get(0), "digest");
                result.addObjectToBothSidesOfRelationshipWithKey(eo, "digests");
            }

            if (key.startsWith("fail")) {
                String message = values.objectForKey(key).get(0).toString();
                EOEnterpriseObject eo = EOUtilities.createAndInsertInstance(ec, "Failure");
                eo.takeValueForKey(message, "message");
                result.addObjectToBothSidesOfRelationshipWithKey(eo, "failures");
            }
        }

        ec.saveChanges();
        log.debug("Ok!");
    }
}
