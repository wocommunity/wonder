package er.plugintest.tests;

import java.io.IOException;

import com.webobjects.foundation.NSData;

import er.extensions.eof.ERXEC;
import er.extensions.eof.ERXEOControlUtilities;
import er.extensions.eof.ERXModelGroup;
import er.extensions.foundation.ERXFileUtilities;
import er.plugintest.model.City;
import er.plugintest.model.Country;

public class CUDTest extends PluginTest{

	public CUDTest(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		resetData();
	}


	public void testInsert() {
		ERXEC ec = (ERXEC) ERXEC.newEditingContext();
		ec.lock();
		try {
			City city = City.createCity(ec, "Washington");
			city.setDistict("DC");
			city.setPopulation(650000);
			city.setDescription("This is some text to make sure long strings work");


			log.debug(ERXModelGroup.prototypeEntityNameForModel(model));
			//Employee emp = Employee.createEmployee(ec, "Bugs", "Bunny", false, Company.createCompany(ec, "Boston Opera"));

			ec.saveChanges();

			Integer cnt = ERXEOControlUtilities.objectCountWithQualifier(ec, City.ENTITY_NAME, City.DISTICT.eq("DC"));

			assertNotNull(cnt);
			assertEquals(Integer.valueOf(1), cnt);

			Country country = Country.createCountry(ec, "USA", "United States of America");
			country.setCapital(city);
			try {
				// make sure blobs work
				NSData flag = new NSData(ERXFileUtilities.inputStreamForResourceNamed("us.png", null, null), 1024);
				country.setFlag(flag);
			} catch (IOException e) {
				log.error(org.apache.commons.lang.exception.ExceptionUtils.getFullStackTrace(e), e);
				throw new RuntimeException(e.getMessage(), e);
			}
			ec.saveChanges();
			cnt = ERXEOControlUtilities.objectCountWithQualifier(ec, Country.ENTITY_NAME, Country.CODE.eq("USA"));

			assertNotNull(cnt);
			assertEquals(Integer.valueOf(1), cnt);
		} finally {
			ec.unlock();
		}
	}


	public void testUpdate() {
		ERXEC ec = (ERXEC) ERXEC.newEditingContext();
		ec.lock();
		try {
			Country country = Country.fetchCountry(ec, Country.CODE.eq("NLD"));
			final String headOfState = "Queen " + country.headOfState(); 
			country.setHeadOfState(headOfState);
			ec.saveChanges();

			Integer cnt = ERXEOControlUtilities.objectCountWithQualifier(ec, Country.ENTITY_NAME, Country.HEAD_OF_STATE.eq(headOfState));

			assertNotNull(cnt);
			assertEquals(Integer.valueOf(1), cnt);
		} finally {
			ec.unlock();
		}

	}

	public void testDelete() {
		ERXEC ec = (ERXEC) ERXEC.newEditingContext();
		ec.lock();
		try {
			Country country = Country.fetchCountry(ec, Country.CODE.eq("NOR"));
			country.delete();
			ec.saveChanges();

			Integer cnt = ERXEOControlUtilities.objectCountWithQualifier(ec, Country.ENTITY_NAME, Country.CODE.eq("NOR"));

			assertNotNull(cnt);
			assertEquals(Integer.valueOf(0), cnt);
		} finally {
			ec.unlock();
		}

	}
}
