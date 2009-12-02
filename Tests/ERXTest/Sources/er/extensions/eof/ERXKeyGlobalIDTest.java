
package er.extensions.eof;

import junit.framework.Assert;
import junit.framework.TestCase;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOGlobalID;
import com.webobjects.eocontrol.EOKeyGlobalID;

import er.extensions.foundation.ERXValueUtilities;

public class ERXKeyGlobalIDTest extends TestCase {

	private EOEditingContext ec;
	
	public void setUp() throws Exception {
		ec = ERXEC.newEditingContext();
	}

	public void testEquals() {

		EOEnterpriseObject emp = ERXEOControlUtilities.createAndInsertObject(ec, "Employee");
		EOEnterpriseObject co = ERXEOControlUtilities.createAndInsertObject(ec, "Company");
		
		emp.takeValueForKey("Bob", "firstName");
		emp.takeValueForKey("Roberts", "lastName");
		emp.takeValueForKey(Boolean.FALSE, "manager");
		
		co.takeValueForKey("Foobar.com", "name");
		emp.takeValueForKey(co, "company");

		ec.saveChanges();

		EOGlobalID gid = emp.editingContext().globalIDForObject(emp);

		Assert.assertFalse(ERXValueUtilities.isNull(emp));
		Assert.assertFalse(gid.isTemporary());

		EOEnterpriseObject emp2 = ERXEOControlUtilities.localInstanceOfObject(ERXEC.newEditingContext(), emp);	

		ERXKeyGlobalID xkgid = ERXKeyGlobalID.globalIDForGID((EOKeyGlobalID)gid);
		Assert.assertEquals(xkgid, xkgid);
		
		EOKeyGlobalID gid2 = (EOKeyGlobalID)emp2.editingContext().globalIDForObject(emp2);
		ERXKeyGlobalID xkgid2 = ERXKeyGlobalID.globalIDForGID(gid2);

		Assert.assertEquals(xkgid, xkgid2);
		
		ec.deleteObject(emp);
		ec.deleteObject(co);
		ec.saveChanges();
	}
}
