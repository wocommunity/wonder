package er.erxtest.d2w;

import com.webobjects.directtoweb.D2WContext;
import com.webobjects.directtoweb.ERD2WContext;

import er.erxtest.ERXTestCase;

public class ERD2WModelTest extends ERXTestCase {
	public void testSimpleValueForKey() {
		// ERD2WModel model = new ERD2WModel(ERXTestUtilities.resourcePathURL("BugTracker.d2wmodel", ERD2WModelTest.class));
		// D2WModel model = ERD2WModel.defaultModel();
		// System.out.println("ERD2WModelTest.testModel: " + model);

		// EOEditingContext ec = ERXEC.newEditingContext();
		// People p = People.createPeople(ec, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, "person", "password");
		// Bug b = Bug.createBug(ec, Boolean.FALSE, Boolean.FALSE, State.ANALYZE, "This is a bug", Component.createComponent(ec, "Component", p), p, p, Priority.createPriority(ec, Integer.valueOf(0), "Top"), Release.createRelease(ec, new NSTimestamp(),
		// Boolean.TRUE, "Version 1"));
		// ec.saveChanges();
		// d2wContext.takeValueForKey(new NSDictionary(new String[] { "password", "DATETIME" }, new String[] { "name", "externalType" }), "attribute");

		D2WContext d2wContext = ERD2WContext.newContext();
		d2wContext.setTask("edit");
		d2wContext.setPropertyKey("passwordConfirmation");
		assertEquals("ERDEditPasswordConfirmation", d2wContext.valueForKey("componentName"));
	}
}
