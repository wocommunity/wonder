import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import java.util.*;

/** The core class of the application;
Displays a calendar for a specific month and group of user.
*/
public class CalendarView extends VacationComponent {

    protected NSMutableArray calendar;

    protected GregorianCalendar currentDate;

    protected static NSArray months = new NSArray( new Object[] {"January","February","March","April","May","June","July","August",
            "September","October","November","December"});
            
    protected NSMutableArray years;
    
    protected static NSArray week = new NSArray(new Object[] {"Sunday","Monday","Tuesday","Wednesday","Thursday","Friday","Saturday"});
    
    protected NSArray row;
    protected String day;

    protected int rowIndex;
    protected int colIndex;
    protected VacationEvent date;

    protected String currentMonthSelection;
    protected Integer currentYearSelection;
    
    protected NSArray groups;

    /** @TypeInfo Group */
    protected EOEnterpriseObject group;
    protected String legendItem;

    public CalendarView(WOContext context) {
        super(context);

        // set up calendar for current month
        currentDate = new GregorianCalendar();

        int year = currentDate.get(GregorianCalendar.YEAR);
        years = new NSMutableArray();

        for (int i=(year-2); i<(year+4); i++) {
            years.addObject(new Integer(i));
        }
        
    
        groups = EOUtilities.objectsForEntityNamed(session.defaultEditingContext(),"Group");

        generateCalendar();
    }

    public NSArray months() {return months;}
    public NSArray week() {return week;}
    
    public void generateCalendar() {
    
        // generate the 1st of the month
        GregorianCalendar firstDate = new GregorianCalendar(currentDate.get(GregorianCalendar.YEAR), currentDate.get(GregorianCalendar.MONTH), 1, 0, 0, 0);
        
        firstDate.setFirstDayOfWeek(GregorianCalendar.SUNDAY);

        calendar = new NSMutableArray();
        
        // generate blank dates until the first day of the month
        NSMutableArray firstWeek = new NSMutableArray();
        for (int i=1; i<firstDate.get(GregorianCalendar.DAY_OF_WEEK); i++) {
            firstWeek.addObject("");
        }
        
        // add the first day of the month to the array
        firstWeek.addObject(new Integer(firstDate.get(GregorianCalendar.DAY_OF_MONTH)));
        firstDate.add(firstDate.DATE,1);

        while (firstDate.get(GregorianCalendar.DAY_OF_WEEK)!=firstDate.getFirstDayOfWeek()) {
            firstWeek.addObject(new Integer(firstDate.get(GregorianCalendar.DAY_OF_MONTH)));
            firstDate.add(firstDate.DATE,1); // increment the date by 1
        }


        calendar.addObject(firstWeek);

        NSMutableArray lastweek = null;

        boolean doContinue = true;
        
        while(doContinue) {
            NSMutableArray currentWeek = new NSMutableArray();

            boolean endOfWeek = false;

            while (firstDate.get(GregorianCalendar.DAY_OF_MONTH)!=1 && !endOfWeek) {
                currentWeek.addObject(new Integer(firstDate.get(GregorianCalendar.DAY_OF_MONTH)));
                firstDate.add(firstDate.DATE,1);
                if (firstDate.get(GregorianCalendar.DAY_OF_WEEK)==firstDate.getFirstDayOfWeek()) endOfWeek=true;
            }

            if (firstDate.get(GregorianCalendar.DAY_OF_MONTH)==1) {
                doContinue=false;
                lastweek = currentWeek;
            }
            else if (currentWeek.count()==7) {
                calendar.addObject(currentWeek);
            }
        }

        for (int i=(lastweek.count()-1); i<6; i++) {
            lastweek.addObject("");
        }

        calendar.addObject(lastweek);

    }

    public WOComponent nextMonth() {
        currentDate.add(currentDate.MONTH,1);
        currentMonthSelection = null;
        currentYearSelection = null;
        generateCalendar();
        // datesForMonth();
        return null;
    }

    public WOComponent previousMonth() {
        currentDate.add(currentDate.MONTH,-1);
        currentMonthSelection = null;
        currentYearSelection = null;
        generateCalendar();
        // datesForMonth();
        return null;
    }

    public String currentMonthSelection() {
        if (currentMonthSelection == null) {
            currentMonthSelection = (String) months.objectAtIndex(currentDate.get(GregorianCalendar.MONTH));
        }

        return currentMonthSelection;
    }

    public Integer currentYearSelection() {
        if (currentYearSelection == null) {
            currentYearSelection = new Integer(currentDate.get(GregorianCalendar.YEAR));
        }

        return currentYearSelection;
    }

    public Object calendarDate() {
        return ((NSArray) calendar.objectAtIndex(rowIndex)).objectAtIndex(colIndex);
    }

    public String periodStringForDate() {
        if (calendarDate().getClass()==((new Integer(0)).getClass())) {
            GregorianCalendar compareDate = dateFromDayNumber((Number) calendarDate());
            // compare this date to the start and finish date of the vacation
            return date.periodStringForDate(compareDate);
        }
        else return null;
    }

    public GregorianCalendar dateFromDayNumber(Number numericDay) {
        GregorianCalendar returnDate = new GregorianCalendar(currentDate.get(GregorianCalendar.YEAR), currentDate.get(GregorianCalendar.MONTH), ((Integer) calendarDate()).intValue(), 0, 0, 0);

        returnDate.setTimeZone(currentDate.getTimeZone());

        return returnDate;
    }



    /*" This method is called for each calendar date in the month, and basically returns an array of events that are found for that date "*/
    public NSArray datesForDate() {

        Session session = (Session) session();
        NSArray datesForDate = new NSArray();

        if (calendarDate().getClass()==((new Integer(0)).getClass())) {

            // construct calendar date
            GregorianCalendar myDate = dateFromDayNumber((Number) calendarDate());

            // if today is a weekend, and weekends are not enabled, return nothing;
            if (!application.settings.objectForKey("showDatesOnWeekends").equals("true"))
                if (myDate.get(GregorianCalendar.DAY_OF_WEEK)==GregorianCalendar.SATURDAY || myDate.get(GregorianCalendar.DAY_OF_WEEK)==GregorianCalendar.SUNDAY)
                    return null;

            NSMutableDictionary bindings = new NSMutableDictionary();
            bindings.setObjectForKey(myDate.getTime(),"DATE");

            if (session.user().group()!=null) {
                bindings.setObjectForKey(session.user().group(),"GROUP");
                bindings.setObjectForKey(NSKeyValueCoding.NullValue,"EXCEPTION");
            }

            // retrieve the fetch spec from the eomodel
            datesForDate = EOUtilities.objectsWithFetchSpecificationAndBindings(session.defaultEditingContext(),
                                                                                "VacationEvent","EventsForDate",bindings);
        }

        EOQualifier qual = EOQualifier.qualifierWithQualifierFormat("type = 'Holiday'", null);
        NSArray holidays = EOQualifier.filteredArrayWithQualifier(datesForDate, qual);
        if (holidays.count() > 0) return holidays;


        // remove any non-calendar types from the array
        NSArray calendarChoices = (NSArray) application.settings.objectForKey("calendarChoices");
        NSMutableArray parsedDates = new NSMutableArray();
        Enumeration enumerator = datesForDate.objectEnumerator();

        while (enumerator.hasMoreElements()) {
            VacationEvent currentEvent = (VacationEvent) enumerator.nextElement();
            if (calendarChoices.indexOfObject(currentEvent.type())!=NSArray.NotFound) parsedDates.addObject(currentEvent);
        }

        return parsedDates;
    }
    
    public WOComponent goDirectToMonth() {

        currentDate = new GregorianCalendar(currentYearSelection.intValue(),
                                            months.indexOfObject(currentMonthSelection), 1, 0, 0, 0);

        generateCalendar();
        return null;
    }

    // needs to be refactored to use CalendarChoices from the plist
    public String legendForDateType() {

        // automatic legend:
        String returnText = "";
        if (date.type().equals("Holiday")) {
            returnText = "PUBLIC HOLIDAY";
        }
        else if (!date.type().equals("Lieu Time Earned") && !date.type().equals("Cancelled")) {
            returnText = date.legendText(); 
        }

        return returnText;
    }

    public String shortCodeForLegend() {
        return VacationEvent.legendTextForType(legendItem);
    }
    
    public WOComponent editPerson() {
        WOComponent nextPage=null;

        if (date.person() == session.user && date.person().group()!=null && date.person().group().parentGroup()!=null) {
            nextPage = (UserInfo) pageWithName("UserInfo");
        }
        else {
            nextPage = (PersonEditor)pageWithName("PersonEditor");
            ((PersonEditor) nextPage).setPerson(date.person());
        }
        return nextPage;
    }
    
}
