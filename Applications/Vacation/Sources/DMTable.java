import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;

/** First page the user sees.  Used to login in the user. */
public class DMTable extends VacationComponent {
    protected Group group;
    protected Group currentGroup;
    protected Person person;

    /** Constructor */
    public DMTable(WOContext context) {
        super(context);
    }
}
