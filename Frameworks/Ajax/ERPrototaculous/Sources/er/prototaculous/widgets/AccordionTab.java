package er.prototaculous.widgets;

import com.webobjects.appserver.WOContext;

import er.ajax.AjaxAccordionTab;

/**
 * Encapsulation of http://www.stickmanlabs.com/accordion
 * Extends the api of AjaxAccordionTab. i.e:
 * @see er.ajax.AjaxAccordionTab
 * 
 * @author mendis
 *
 */
public class AccordionTab extends AjaxAccordionTab {
    public AccordionTab(WOContext context) {
        super(context);
    }
}