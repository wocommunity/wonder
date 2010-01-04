package er.extensions.eof;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import com.webobjects.eocontrol.EOEditingContext;

import er.erxtest.ERXTestCase;
import er.erxtest.model.Company;

public class ERXEOControlUtilitiesTest extends ERXTestCase {

	private EOEditingContext ec;
	
	@Before
	public void setUp() throws Exception {
		ec = ERXEC.newEditingContext();
	}

	@Test
	public void testCreateAndInsertObjectEOEditingContextClassOfT() {
		Company co = ERXEOControlUtilities.createAndInsertObject(ec, Company.class);
		//Only testing for nullness since the above should throw a class cast ex if
		//co is not a company.
		Assert.assertNotNull(co);
		Assert.assertTrue(ec.insertedObjects().contains(co));
	}

}