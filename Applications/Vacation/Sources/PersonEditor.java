import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;

/** The Person Editor page allows the Admin user to edit a user. */
public class PersonEditor extends VacationComponent {

    /** Local editing context used, so that changes to the Person being editing are local, and not commited unless explicitly so.*/
    protected EOEditingContext localContext;
    protected Person person;

    /** An admin user can create a new event for a user.  This object holds the data for the new event */
    protected VacationEvent newDate;

    /** Iterator for the other events.  Used by the WORepetition in the .wod */
    protected VacationEvent currentDate;

    public PersonEditor(WOContext context) {
        super(context);
        localContext = new EOEditingContext();
        newDate = new VacationEvent();
    }

    /** This method tries to save changes made in the localContext to the database. */
    public WOComponent save() {
        try {
            localContext.saveChanges();
            this.setMessage("Save Successful!");
        }
        catch (Exception e) {
            this.setMessage(e.getMessage());
        }

        /** Force a reload of all objects in the session */
        localContext.invalidateAllObjects();
        session().defaultEditingContext().invalidateAllObjects();

        return null;
    }

    /** Set Person accessor -- ensures that the Person object being passed is in the local editing context */
    public void setPerson(Person newPerson) {
        
        if (newPerson==null) person = new Person(localContext, application.years());
        // fetch a new version of the person, because of some context problem I haven't solved yet
        else if (newPerson.editingContext()!=null) {
            person = (Person) EOUtilities.localInstanceOfObject(localContext,newPerson);
        }
        
        person.selectedYear = new Integer (session.selectedYear.intValue());
        person.editorUser = session.user();
    }

    /** Returns the Person Summay page.  Binded to the Cancel button */
    public PersonSummary returnToPersonSummary() {
        PersonSummary nextPage = (PersonSummary)pageWithName("PersonSummary");
        return nextPage;
    }

    /** Inserts the newDate object into the localContext, saves to database, and then re-initializes the newDate object*/
    public WOComponent addDate() {

        localContext.insertObject(newDate);

        newDate.setPerson(person);
        person.addToDates(newDate);

        save(); // save the new date
        newDate = new VacationEvent();         // create a new Date object

        return null;
    }

    /** Deletes an event and saves the changes */
    public WOComponent deleteCurrentDate() {
        person.removeFromDates(currentDate);
        save();
        return null;
    }

    public WOComponent refresh() {
        person.selectedYear = new Integer(session.selectedYear.intValue());
        return null;
    }

}
