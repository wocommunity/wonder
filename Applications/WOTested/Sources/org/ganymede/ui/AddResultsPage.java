
package org.ganymede.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSTimestamp;

import er.extensions.components.ERXComponent;

public class AddResultsPage extends ERXComponent {
    private static final Logger log = LoggerFactory.getLogger(AddResultsPage.class);

    public AddResultsPage(WOContext context) {
        super(context);

    }

    public String results;

    public String boxResults;

    public WOActionResults add() {

        EOEditingContext ec = session().defaultEditingContext();

        NSArray<String> lines = NSArray.componentsSeparatedByString(boxResults, "\n");

        NSMutableArray<String> resultLines = new NSMutableArray<>();

        log.debug("boxResults lines # {}", lines.size());
        for (String line : lines) {
            line = line.replace("[java]", "");
            line = line.trim();
            resultLines.add(line);
        }

        results = resultLines.componentsJoinedByString("<br/>\n");

        NSTimestamp created = new NSTimestamp();

        EOEnterpriseObject result = EOUtilities.createAndInsertInstance(ec, "Result");

        result.takeValueForKey(created, "whence");

        for (String line : resultLines) {

            NSArray<String> parts = NSArray.componentsSeparatedByString(line, " ");

            if (parts.size() == 3) {

                EOEnterpriseObject digest = EOUtilities.createAndInsertInstance(ec, "VersionDigest");

                digest.takeValueForKey(parts.get(0), "digest");
                digest.takeValueForKey(parts.get(2), "rname");
                digest.takeValueForKey(result, "result");
            }
        }

        ec.saveChanges();

        boxResults = null;
        return context().page();
    }
}
