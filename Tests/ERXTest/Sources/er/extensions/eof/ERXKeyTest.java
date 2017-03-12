package er.extensions.eof;

import java.math.BigDecimal;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSRange;
import com.webobjects.foundation.NSTimestamp;

import er.erxtest.ERXTestCase;
import er.erxtest.model.Company;
import er.erxtest.model.Employee;
import er.erxtest.model.Paycheck;
import er.erxtest.model.Role;
import er.extensions.foundation.ERXArrayUtilities;

public class ERXKeyTest extends ERXTestCase {

	private EOEditingContext ec;
	
	private Company acme;
	private Company shinraInc;
	private Company tyrellCorp;
	private Company sortCo;
	
	private Employee moe;
	private Employee larry;
	private Employee curly;
	private Employee tyrell;
	private Employee deckard;
	private Employee rachael;
	private Employee rufus;
	private Employee rude;
	private Employee elena;
	private Employee sephiroth;
	private Employee aaBb;
	private Employee Abba;
	
	private Paycheck paycheck5000;
	private Paycheck paycheck10000;
	private Paycheck paycheck15000;
	
	private Role headHunter;
	private Role bigBoss;
	private Role accountant;
	private Role doctor;
	private Role barber;
	private NSArray<Role> roles;
	
	private NSArray<Employee> shinraEmployees;
	private NSArray<Employee> shinraNonManager;
	private NSArray<String> shinraNonManagerNames;
	private NSArray<Employee> tyrellEmployees;
	private NSArray<Paycheck> paychecks;
	
	private static final String numKey = "num";
	private static final ERXKey<Integer> num = new ERXKey<>(numKey);
	private static final NSDictionary<String, Integer> uno = new NSDictionary<>(Integer.valueOf(1), numKey);
	private static final NSDictionary<String, Integer> dos = new NSDictionary<>(Integer.valueOf(2), numKey);
	private static final NSDictionary<String, Integer> tres = new NSDictionary<>(Integer.valueOf(3), numKey);
	private static final NSDictionary<String, Integer> quatro = new NSDictionary<>(Integer.valueOf(4), numKey);
	private static final NSDictionary<String, Integer> cinco = new NSDictionary<>(Integer.valueOf(5), numKey);
	private static final NSArray<NSDictionary<String, Integer>> numbers = new NSArray<NSDictionary<String,Integer>>(uno, dos, tres, quatro, cinco);
	private static final NSArray<Integer> simpleNumbers = new NSArray<>(Integer.valueOf(1),Integer.valueOf(2),Integer.valueOf(3),Integer.valueOf(4),Integer.valueOf(5));

	
	@Override
	@Before
	public void setUp() throws Exception {
		ec = ERXEC.newEditingContext();
		
		headHunter = Role.createRole(ec);
		bigBoss = Role.createRole(ec);
		accountant = Role.createRole(ec);
		roles = new NSArray<>(headHunter, bigBoss, accountant);
		doctor = Role.createRole(ec);
		barber = Role.createRole(ec);
		
		acme = Company.createCompany(ec, "Acme");
		shinraInc = Company.createCompany(ec, "Shinra Inc.");
		tyrellCorp = Company.createCompany(ec, "Tyrell Corporation");
		sortCo = Company.createCompany(ec, "Sort test");
		
		moe = Employee.createEmployee(ec, "Moe", "Stooge", Boolean.TRUE, acme);
		larry = Employee.createEmployee(ec, "Larry", "Stooge", Boolean.FALSE, acme);
		curly = Employee.createEmployee(ec, "Curly", "Stooge", Boolean.FALSE, acme);
		
		tyrell = Employee.createEmployee(ec, "Eldon", "Tyrell", Boolean.TRUE, tyrellCorp);
		deckard = Employee.createEmployee(ec, "Rick", "Deckard", Boolean.FALSE, tyrellCorp);
		rachael = Employee.createEmployee(ec, "Rachael", "Replicant", Boolean.FALSE, tyrellCorp);
		tyrellEmployees = new NSArray<>(tyrell, deckard, rachael);
		
		rufus = Employee.createEmployee(ec, "Rufus", "Shinra", Boolean.TRUE, shinraInc);
		rude = Employee.createEmployee(ec, "Rude", "Terk", Boolean.FALSE, shinraInc);
		elena = Employee.createEmployee(ec, "Elena", "turk", Boolean.FALSE, shinraInc);
		sephiroth = Employee.createEmployee(ec, "Sephiroth", "Jenova", Boolean.FALSE, shinraInc);
		shinraEmployees = new NSArray<>(rufus, rude, elena, sephiroth);
		shinraNonManager = new NSArray<>(rude, elena, sephiroth);
		shinraNonManagerNames = new NSArray<>(rude.firstName(), elena.firstName(), sephiroth.firstName());
		
		elena.setBestSalesTotal(BigDecimal.valueOf(10000L));
		rude.setBestSalesTotal(BigDecimal.valueOf(5000L));
		
		aaBb = Employee.createEmployee(ec, "aa", "Bb", Boolean.FALSE, sortCo);
		Abba = Employee.createEmployee(ec, "Ab", "ba", Boolean.FALSE, sortCo);

		
		NSTimestamp now = new NSTimestamp();
		paycheck5000 = Paycheck.createPaycheck(ec, BigDecimal.valueOf(5000L), Boolean.FALSE, now.timestampByAddingGregorianUnits(0, 0, -14, 0, 0, 0), rufus);
		paycheck10000 = Paycheck.createPaycheck(ec, BigDecimal.valueOf(10000L), Boolean.FALSE, now.timestampByAddingGregorianUnits(0, 0, -7, 0, 0, 0), rufus);
		paycheck15000 = Paycheck.createPaycheck(ec, BigDecimal.valueOf(15000L), Boolean.FALSE, now, rufus);
		paychecks = new NSArray(paycheck5000, paycheck10000, paycheck15000);
		
		
		rufus.addToRoles(headHunter);
		rufus.addToRoles(bigBoss);
		sephiroth.addToRoles(headHunter);
		rude.addToRoles(accountant);
		rude.addToRoles(headHunter);
		elena.addToRoles(accountant);
		
		moe.addObjectToBothSidesOfRelationshipWithKey(doctor, Employee.ROLES_KEY);
		moe.addObjectToBothSidesOfRelationshipWithKey(barber, Employee.ROLES_KEY);
		
		ec.saveChanges();		
	}

	@Override
	@After
	public void tearDown() throws Exception {
		roles = null;
		shinraEmployees = null;
		shinraNonManager = null;
		shinraNonManagerNames = null;
		tyrellEmployees = null;

		paychecks = null;

		if (ERXTestCase.adaptorName().equals("JDBC")) {

			ERXEOAccessUtilities.evaluateSQLWithEntity(ec, ERXEOAccessUtilities.entityNamed(ec, "Company"), "delete from Company");
			ERXEOAccessUtilities.evaluateSQLWithEntity(ec, ERXEOAccessUtilities.entityNamed(ec, "Employee"), "delete from Employee");
                	ERXEOAccessUtilities.evaluateSQLWithEntity(ec, ERXEOAccessUtilities.entityNamed(ec, "EmployeeHI"), "delete from EmployeeHI");
                	ERXEOAccessUtilities.evaluateSQLWithEntity(ec, ERXEOAccessUtilities.entityNamed(ec, "EmployeeVI"), "delete from EmployeeVI");
                	ERXEOAccessUtilities.evaluateSQLWithEntity(ec, ERXEOAccessUtilities.entityNamed(ec, "EmployeeRole"), "delete from EmployeeRole");
			ERXEOAccessUtilities.evaluateSQLWithEntity(ec, ERXEOAccessUtilities.entityNamed(ec, "Paycheck"), "delete from Paycheck");
			ERXEOAccessUtilities.evaluateSQLWithEntity(ec, ERXEOAccessUtilities.entityNamed(ec, "Role"), "delete from Role");

		}

		if (ERXTestCase.adaptorName().equals("Memory")) {

			shinraInc.delete();
			tyrellCorp.delete();
			sortCo.delete();
		
			moe.delete();
			larry.delete();
			curly.delete();
			tyrell.delete();
			deckard.delete();
			rachael.delete();
			rufus.delete();
			rude.delete();
			elena.delete();
			sephiroth.delete();
			aaBb.delete();
			Abba.delete();

			paycheck5000.delete();
			paycheck10000.delete();
			paycheck15000.delete();
		
			headHunter.delete();
			bigBoss.delete();
			accountant.delete();
			doctor.delete();
			barber.delete();

			ec.saveChanges();
		}
	}

	@Test
	public void testAppendString() {
		assertEquals("one", new ERXKey(null).append("one").key());
		assertEquals("one", new ERXKey("").append("one").key());
		assertEquals("one.two", new ERXKey("one").append("two").key());
	}
	
	@Test
	public void testAvgNonNullERXKeyOfQ() {
		BigDecimal d = Company.EMPLOYEES.dot(ERXKey.avgNonNull(Employee.BEST_SALES_TOTAL)).valueInObject(shinraInc);
		assertTrue(BigDecimal.valueOf(7500L).compareTo(d) == 0);
	}
	
	@Test
	public void testAvgNonNull() {
		BigDecimal d = Company.EMPLOYEES.dot(Employee.BEST_SALES_TOTAL).dot(ERXKey.avgNonNull()).valueInObject(shinraInc);
		assertTrue(BigDecimal.valueOf(7500L).compareTo(d) == 0);
	}

	@Test
	public void testAtAvgNonNullERXKeyOfQ() {
		BigDecimal d = Company.EMPLOYEES.atAvgNonNull(Employee.BEST_SALES_TOTAL).valueInObject(shinraInc);
		assertTrue(BigDecimal.valueOf(7500L).compareTo(d) == 0);
	}
	
	@Test
	public void testAtAvgNonNull() {
		BigDecimal d = Company.EMPLOYEES.dot(Employee.BEST_SALES_TOTAL).atAvgNonNull().valueInObject(shinraInc);
		assertTrue(BigDecimal.valueOf(7500L).compareTo(d) == 0);
	}

	@Test
	public void testFetchSpecStringERXKeyOfU() {
		NSArray<String> plebs = Company.EMPLOYEES.dot(ERXKey.fetchSpec("plebs", Employee.FIRST_NAME)).valueInObject(shinraInc);
		assertTrue(ERXArrayUtilities.arraysAreIdenticalSets(shinraNonManagerNames, plebs));
	}

	@Test
	public void testAtFetchSpecStringERXKeyOfU() {
		NSArray<String> plebs = Company.EMPLOYEES.atFetchSpec("plebs", Employee.FIRST_NAME).valueInObject(shinraInc);
		assertTrue(ERXArrayUtilities.arraysAreIdenticalSets(shinraNonManagerNames, plebs));
	}

	@Test
	public void testFetchSpecString() {
		NSArray<Employee> plebs = (NSArray<Employee>) Company.EMPLOYEES.dot(ERXKey.fetchSpec("plebs")).valueInObject(shinraInc);
		assertTrue(ERXArrayUtilities.arraysAreIdenticalSets(shinraNonManager, plebs));
	}

	@Test
	public void testAtFetchSpecString() {
		NSArray<Employee> plebs = (NSArray<Employee>) Company.EMPLOYEES.atFetchSpec("plebs").valueInObject(shinraInc);
		assertTrue(ERXArrayUtilities.arraysAreIdenticalSets(shinraNonManager, plebs));
	}

	@Test
	public void testFlattenERXKeyOfU() {
		Object o = Company.EMPLOYEES.dot(Employee.COMPANY).dot(Company.EMPLOYEES).dot(ERXKey.flatten()).dot(Employee.COMPANY).dot(Company.EMPLOYEES).valueInObject(shinraInc);
		NSArray array = (NSArray)o;
		assertTrue(array.count() == 16);
		NSArray<BigDecimal> shinraPaychecks = Company.EMPLOYEES.dot(Employee.PAYCHECKS).dot(ERXKey.flatten(Paycheck.AMOUNT)).valueInObject(shinraInc);
		assertTrue(shinraPaychecks.count() == 3);
	}

	@Test
	public void testAtFlattenERXKeyOfU() {
		BigDecimal shinraPayroll = Company.EMPLOYEES.dot(Employee.PAYCHECKS).atFlatten(Paycheck.AMOUNT).atSum().valueInObject(shinraInc);
		assertTrue(shinraPayroll.compareTo(new BigDecimal(30000L)) == 0);
	}

	@Test
	public void testFlatten() {
		NSArray<Paycheck> shinraPaychecks = (NSArray<Paycheck>) Company.EMPLOYEES.dot(Employee.PAYCHECKS).dot(ERXKey.flatten()).valueInObject(shinraInc);
		assertTrue(ERXArrayUtilities.arraysAreIdenticalSets(paychecks, shinraPaychecks));
	}

	@Test
	public void testAtFlatten() {
		NSArray<Paycheck> shinraPaychecks = Company.EMPLOYEES.dot(Employee.PAYCHECKS).atFlatten().arrayValueInObject(shinraInc);
		assertTrue(ERXArrayUtilities.arraysAreIdenticalSets(paychecks, shinraPaychecks));
		NSArray<Employee> duplicateEmployees = Company.EMPLOYEES.dot(Employee.COMPANY).dot(Company.EMPLOYEES).atFlatten().arrayValueInObject(shinraInc);
		assertEquals(16, duplicateEmployees.count());
	}

	@Test
	public void testIsEmpty() {
		assertEquals(Boolean.TRUE, Employee.PAYCHECKS.dot(ERXKey.isEmpty()).valueInObject(elena));
		assertEquals(Boolean.FALSE, Employee.PAYCHECKS.dot(ERXKey.isEmpty()).valueInObject(rufus));
	}

	@Test
	public void testAtIsEmpty() {
		assertEquals(Boolean.TRUE, Employee.PAYCHECKS.atIsEmpty().valueInObject(elena));
		assertEquals(Boolean.FALSE, Employee.PAYCHECKS.atIsEmpty().valueInObject(rufus));
	}

	@Test
	public void testLimitIntegerERXKeyOfU() {
		NSArray<String> names = Employee.COMPANY.dot(Company.EMPLOYEES).dot(ERXKey.limit(Integer.valueOf(2), Employee.FIRST_NAME)).valueInObject(sephiroth);
		assertTrue(names.count() == 2);
	}

	@Test
	public void testAtLimitIntegerERXKeyOfU() {
		NSArray<String> names = Employee.COMPANY.dot(Company.EMPLOYEES).atLimit(Integer.valueOf(2), Employee.FIRST_NAME).valueInObject(sephiroth);
		assertTrue(names.count() == 2);
	}

	@Test
	public void testLimitInteger() {
		NSArray<Employee> employees = (NSArray<Employee>) Company.EMPLOYEES.dot(ERXKey.limit(Integer.valueOf(2))).valueInObject(shinraInc);
		assertTrue(employees.count() == 2);
	}

	@Test
	public void testAtLimitInteger() {
		NSArray<Employee> employees = Company.EMPLOYEES.atLimit(Integer.valueOf(2)).arrayValueInObject(shinraInc);
		assertTrue(employees.count() == 2);
	}
	
	@Test
	public void testMedian() {
		BigDecimal d = Employee.PAYCHECKS.dot(Paycheck.AMOUNT).dot(ERXKey.median()).valueInObject(rufus);
		assertTrue(BigDecimal.valueOf(10000L).compareTo(d) == 0);
		d = ERXKey.median().valueInObject(new NSArray(Integer.valueOf(1000), Integer.valueOf(2000), Integer.valueOf(3000), Integer.valueOf(4000) ));
		assertTrue(BigDecimal.valueOf(2500L).compareTo(d) == 0);
		d = ERXKey.median().valueInObject(new NSArray(Integer.valueOf(2000)));
		assertTrue(BigDecimal.valueOf(2000L).compareTo(d) == 0);
		
		//Should work with strings too
		d = ERXKey.median().valueInObject(new NSArray("1000", "2000", "3000", "4000"));
		assertTrue(BigDecimal.valueOf(2500L).compareTo(d) == 0);
		d = ERXKey.median().valueInObject(new NSArray("1000", "2000", "3000"));
		assertTrue(BigDecimal.valueOf(2000L).compareTo(d) == 0);
		d = ERXKey.median().valueInObject(new NSArray("2000"));
		assertTrue(BigDecimal.valueOf(2000L).compareTo(d) == 0);
		
		//Return null on empty arrays
		d = ERXKey.median().valueInObject(NSArray.EmptyArray);
		assertTrue(d == null);
	}
	
	@Test
	public void testAtMedian() {
		BigDecimal d = Employee.PAYCHECKS.dot(Paycheck.AMOUNT).atMedian().valueInObject(rufus);
		assertTrue(BigDecimal.valueOf(10000L).compareTo(d) == 0);
	}

	@Test
	public void testMedianERXKeyOfQ() {
		BigDecimal d = Employee.PAYCHECKS.dot(ERXKey.median(Paycheck.AMOUNT)).valueInObject(rufus);
		assertTrue(BigDecimal.valueOf(10000L).compareTo(d) == 0);
	}

	@Test
	public void testAtMedianERXKeyOfQ() {
		BigDecimal d = Employee.PAYCHECKS.atMedian(Paycheck.AMOUNT).valueInObject(rufus);
		assertTrue(BigDecimal.valueOf(10000L).compareTo(d) == 0);
	}

	@Test
	public void testObjectAtIndexIntegerERXKeyOfU() {
		String fetched = Company.EMPLOYEES.atFetchSpec("plebs").atObjectAtIndex(Integer.valueOf(0)).dot(Employee.FIRST_NAME).valueInObject(acme);
		assertEquals("Curly", fetched);
	}

	@Test
	public void testAtObjectAtIndexIntegerERXKeyOfU() {
		String fetched = Company.EMPLOYEES.atFetchSpec("plebs").atObjectAtIndex(Integer.valueOf(0)).dot(Employee.FIRST_NAME).valueInObject(shinraInc);
		assertEquals("Elena", fetched);
	}

	@Test
	public void testObjectAtIndexInteger() {
		Employee fetched = (Employee) Company.EMPLOYEES.atFetchSpec("plebs").dot(ERXKey.objectAtIndex(Integer.valueOf(1))).valueInObject(shinraInc);
		assertEquals(rude, fetched);
	}

	@Test
	public void testAtObjectAtIndexInteger() {
		Employee fetched = (Employee) Company.EMPLOYEES.atFetchSpec("plebs").atObjectAtIndex(Integer.valueOf(2)).valueInObject(shinraInc);
		assertEquals(sephiroth, fetched);
	}
	
	@Test 
	public void testSortWithKeyPath() {
		EOSortOrdering so = new EOSortOrdering("firstName.length", EOSortOrdering.CompareAscending);
		NSArray<Employee> sortedEmps = shinraInc.employees(null, new NSArray(so), false);
		assertTrue(sortedEmps.objectAtIndex(0).firstName().equals("Rude"));
	}

	@Test
	public void testRemoveNullValuesERXKeyOfU() {
		Object o = Company.EMPLOYEES.dot(Employee.BEST_SALES_TOTAL).valueInObject(shinraInc);
		NSArray<?> d4 = (NSArray<?>) o;
		assertTrue(d4.count() == 4);
		NSArray<BigDecimal> d2 = (NSArray<BigDecimal>) Company.EMPLOYEES.dot(Employee.BEST_SALES_TOTAL).dot(ERXKey.removeNullValues()).valueInObject(shinraInc);
		NSArray<BigDecimal> d2plus = Company.EMPLOYEES.dot(Employee.BEST_SALES_TOTAL).dot(ERXKey.removeNullValues(new ERXKey<NSArray<BigDecimal>>("plus"))).valueInObject(shinraInc);
		assertEquals(d2, d2plus);
	}

	@Test
	public void testAtRemoveNullValuesERXKeyOfU() {
		//These don't actually contain null values, but using remove null values should be benign!
		Object o = Company.EMPLOYEES.atRemoveNullValues(Employee.ROLES).dot(Role.EMPLOYEES).atFlatten().valueInObject(acme);
		NSArray m1 = (NSArray)o;
		assertFalse(m1.contains(larry));
		assertFalse(m1.contains(curly));
		assertTrue(m1.contains(moe));
		assertTrue(m1.count() == 2);
		
		o = Company.EMPLOYEES.dot(Employee.ROLES).atFlatten().atRemoveNullValues(Role.EMPLOYEES).valueInObject(acme);
		NSArray m2 = (NSArray)o;
		assertTrue(m2.count() == 2);
		assertTrue(m2.objectAtIndex(0) instanceof NSArray);
		NSArray m3 = (NSArray)m2.objectAtIndex(0);
		assertTrue(m3.count() == 1);
		assertTrue(m3.objectAtIndex(0) instanceof Employee);
		
		o = Company.EMPLOYEES.dot(Employee.BEST_SALES_TOTAL).valueInObject(shinraInc);
		NSArray<?> d4 = (NSArray<?>) o;
		assertTrue(d4.count() == 4);
		NSArray<BigDecimal> d2 = Company.EMPLOYEES.dot(Employee.BEST_SALES_TOTAL).atRemoveNullValues().arrayValueInObject(shinraInc);
		NSArray<BigDecimal> d2plus = Company.EMPLOYEES.dot(Employee.BEST_SALES_TOTAL).atRemoveNullValues(new ERXKey<NSArray<BigDecimal>>("plus")).valueInObject(shinraInc);
		assertEquals(d2, d2plus);
	}

	@Test
	public void testRemoveNullValues() {
		Object o = Company.EMPLOYEES.dot(Employee.BEST_SALES_TOTAL).valueInObject(shinraInc);
		NSArray<?> d4 = (NSArray<?>) o;
		assertTrue(d4.count() == 4);
		NSArray<BigDecimal> d2 = (NSArray<BigDecimal>) Company.EMPLOYEES.dot(Employee.BEST_SALES_TOTAL).dot(ERXKey.removeNullValues()).valueInObject(shinraInc);
		assertTrue(d2.count() == 2);
	}

	@Test
	public void testAtRemoveNullValues() {
		Object o = Company.EMPLOYEES.dot(Employee.BEST_SALES_TOTAL).valueInObject(shinraInc);
		NSArray<?> d4 = (NSArray<?>) o;
		assertTrue(d4.count() == 4);
		NSArray<BigDecimal> d2 = Company.EMPLOYEES.dot(Employee.BEST_SALES_TOTAL).atRemoveNullValues().arrayValueInObject(shinraInc);
		assertTrue(d2.count() == 2);
	}

	@Test
	public void testReverseERXKeyOfU() {
		NSArray<Employee> shinraDesc = shinraInc.employees(
				Employee.MANAGER.eq(Boolean.FALSE),
				Employee.FIRST_NAME.descs(),
				true);
		NSArray<String> shinraAscReverse = Company.EMPLOYEES.atFetchSpec("plebs").atReverse(Employee.FIRST_NAME).valueInObject(shinraInc);
		assertEquals(shinraDesc.valueForKey(Employee.FIRST_NAME_KEY), shinraAscReverse);
	}

	@Test
	public void testAtReverseERXKeyOfU() {
		NSArray<Employee> shinraDesc = shinraInc.employees(
				Employee.MANAGER.eq(Boolean.FALSE),
				Employee.FIRST_NAME.descs(),
				true);
		NSArray<String> shinraAscReverse = Company.EMPLOYEES.atFetchSpec("plebs").dot(ERXKey.reverse(Employee.FIRST_NAME)).valueInObject(shinraInc);
		assertEquals(shinraDesc.valueForKey(Employee.FIRST_NAME_KEY), shinraAscReverse);
	}

	@Test
	public void testReverse() {
		NSArray<Employee> shinraAsc = Company.EMPLOYEES.atSortAsc(Employee.FIRST_NAME).arrayValueInObject(shinraInc);
		NSArray<Employee> shinraDescReverse = Company.EMPLOYEES.atSortDesc(Employee.FIRST_NAME).arrayValueInObject(shinraInc);
		shinraDescReverse = (NSArray<Employee>) ERXKey.reverse().valueInObject(shinraDescReverse);
		assertEquals(shinraAsc, shinraDescReverse);
	}

	@Test
	public void testAtReverse() {
		NSArray<Employee> shinraDesc = shinraInc.employees(
				Employee.MANAGER.eq(Boolean.FALSE),
				Employee.FIRST_NAME.descs(),
				true);
		NSArray<Employee> shinraAscReverse = (NSArray<Employee>) Company.EMPLOYEES.atFetchSpec("plebs").atReverse().valueInObject(shinraInc);
		assertEquals(shinraDesc, shinraAscReverse);
	}

	@Test
	public void testSort() {
		NSArray<Employee> first = (NSArray<Employee>) Company.EMPLOYEES.dot(ERXKey.sort(Employee.FIRST_NAME)).valueInObject(sortCo);
		assertEquals(new NSArray<>(Abba, aaBb), first);
		NSArray<Employee> last = (NSArray<Employee>) Company.EMPLOYEES.dot(ERXKey.sort(Employee.LAST_NAME,Employee.FIRST_NAME)).valueInObject(sortCo);
		assertEquals(new NSArray<>(aaBb, Abba), last);
	}

	@Test
	public void testAtSort() {
		NSArray<Employee> first = Company.EMPLOYEES.atSort(Employee.FIRST_NAME).arrayValueInObject(sortCo);
		assertEquals(new NSArray<>(Abba, aaBb), first);
		NSArray<Employee> last = Company.EMPLOYEES.atSort(Employee.LAST_NAME,Employee.FIRST_NAME).arrayValueInObject(sortCo);
		assertEquals(new NSArray<>(aaBb, Abba), last);
	}

	@Test
	public void testSortAsc() {
		NSArray<Employee> first = (NSArray<Employee>) Company.EMPLOYEES.dot(ERXKey.sortAsc(Employee.FIRST_NAME)).valueInObject(sortCo);
		assertEquals(new NSArray<>(Abba, aaBb), first);
		NSArray<Employee> last = (NSArray<Employee>) Company.EMPLOYEES.dot(ERXKey.sortAsc(Employee.LAST_NAME,Employee.FIRST_NAME)).valueInObject(sortCo);
		assertEquals(new NSArray<>(aaBb, Abba), last);
	}

	@Test
	public void testAtSortAsc() {
		NSArray<Employee> first = Company.EMPLOYEES.atSortAsc(Employee.FIRST_NAME).arrayValueInObject(sortCo);
		assertEquals(new NSArray<>(Abba, aaBb), first);
		NSArray<Employee> last = Company.EMPLOYEES.atSortAsc(Employee.LAST_NAME,Employee.FIRST_NAME).arrayValueInObject(sortCo);
		assertEquals(new NSArray<>(aaBb, Abba), last);
	}

	@Test
	public void testSortDesc() {
		NSArray<Employee> first = (NSArray<Employee>) Company.EMPLOYEES.dot(ERXKey.sortDesc(Employee.FIRST_NAME)).valueInObject(sortCo);
		assertEquals(new NSArray<>(aaBb, Abba), first);
		NSArray<Employee> last = (NSArray<Employee>) Company.EMPLOYEES.dot(ERXKey.sortDesc(Employee.LAST_NAME,Employee.FIRST_NAME)).valueInObject(sortCo);
		assertEquals(new NSArray<>(Abba, aaBb), last);
	}

	@Test
	public void testAtSortDesc() {
		NSArray<Employee> first = Company.EMPLOYEES.atSortDesc(Employee.FIRST_NAME).arrayValueInObject(sortCo);
		assertEquals(new NSArray<>(aaBb, Abba), first);
		NSArray<Employee> last = Company.EMPLOYEES.atSortDesc(Employee.LAST_NAME,Employee.FIRST_NAME).arrayValueInObject(sortCo);
		assertEquals(new NSArray<>(Abba, aaBb), last);
	}

	@Test
	public void testSortInsensitiveAsc() {
		NSArray<Employee> first = (NSArray<Employee>) Company.EMPLOYEES.dot(ERXKey.sortInsensitiveAsc(Employee.FIRST_NAME)).valueInObject(sortCo);
		assertEquals(new NSArray<>(aaBb, Abba), first);
		NSArray<Employee> last = (NSArray<Employee>) Company.EMPLOYEES.dot(ERXKey.sortInsensitiveAsc(Employee.LAST_NAME,Employee.FIRST_NAME)).valueInObject(sortCo);
		assertEquals(new NSArray<>(Abba, aaBb), last);
	}

	@Test
	public void testAtSortInsensitiveAsc() {
		NSArray<Employee> first = Company.EMPLOYEES.atSortInsensitiveAsc(Employee.FIRST_NAME).arrayValueInObject(sortCo);
		assertEquals(new NSArray<>(aaBb, Abba), first);
		NSArray<Employee> last = Company.EMPLOYEES.atSortInsensitiveAsc(Employee.LAST_NAME,Employee.FIRST_NAME).arrayValueInObject(sortCo);
		assertEquals(new NSArray<>(Abba, aaBb), last);
	}

	@Test
	public void testSortInsensitiveDesc() {
		NSArray<Employee> first = Company.EMPLOYEES.atSortInsensitiveDesc(Employee.FIRST_NAME).arrayValueInObject(sortCo);
		assertEquals(new NSArray<>(Abba, aaBb), first);
		NSArray<Employee> last = Company.EMPLOYEES.atSortInsensitiveDesc(Employee.LAST_NAME,Employee.FIRST_NAME).arrayValueInObject(sortCo);
		assertEquals(new NSArray<>(aaBb, Abba), last);
	}

	@Test
	public void testAtSortInsensitiveDesc() {
		NSArray<Employee> first = (NSArray<Employee>) Company.EMPLOYEES.dot(ERXKey.sortInsensitiveDesc(Employee.FIRST_NAME)).valueInObject(sortCo);
		assertEquals(new NSArray<>(Abba, aaBb), first);
		NSArray<Employee> last = (NSArray<Employee>) Company.EMPLOYEES.dot(ERXKey.sortInsensitiveDesc(Employee.LAST_NAME,Employee.FIRST_NAME)).valueInObject(sortCo);
		assertEquals(new NSArray<>(aaBb, Abba), last);
	}

	@Test
	public void testSubarrayWithRangeNSRangeERXKeyOfU() {
		NSArray<String> acme1_1 = Company.EMPLOYEES.atFetchSpec("plebs").dot(ERXKey.subarrayWithRange(new NSRange(0,1), Employee.FIRST_NAME)).valueInObject(acme);
		assertEquals(new NSArray<>("Curly"), acme1_1);
	}

	@Test
	public void testAtSubarrayWithRangeNSRangeERXKeyOfU() {
		NSArray<String> acme1_1 = Company.EMPLOYEES.atFetchSpec("plebs").atSubarrayWithRange(new NSRange(1,1), Employee.FIRST_NAME).valueInObject(acme);
		assertEquals(new NSArray<>("Larry"), acme1_1);
	}

	@Test
	public void testSubarrayWithRangeNSRange() {
		NSArray<Employee> shinraAsc1_2 = (NSArray<Employee>) Company.EMPLOYEES.atFetchSpec("plebs").atSubarrayWithRange(new NSRange(1,2)).valueInObject(shinraInc);
		assertEquals(new NSArray<>(rude,sephiroth), shinraAsc1_2);
	}

	@Test
	public void testAtSubarrayWithRangeNSRange() {
		NSArray<Employee> shinraAsc2_1 = (NSArray<Employee>) Company.EMPLOYEES.atFetchSpec("plebs").atSubarrayWithRange(new NSRange(2,1)).valueInObject(shinraInc);
		assertEquals(new NSArray<>(sephiroth), shinraAsc2_1);
	}

	@Test
	public void testUniqueERXKeyOfU() {
		NSArray<String> companyNames = Employee.COMPANY.atUnique(Company.NAME).valueInObject(tyrellEmployees);
		assertEquals(new NSArray<>("Tyrell Corporation"), companyNames);
	}

	@Test
	public void testAtUniqueERXKeyOfU() {
		NSArray<String> companyNames = Employee.COMPANY.dot(ERXKey.unique(Company.NAME)).valueInObject(tyrellEmployees);
		assertEquals(new NSArray<>("Tyrell Corporation"), companyNames);
	}

	@Test
	public void testUnique() {
		NSArray<Employee> duplicates = shinraEmployees.arrayByAddingObjectsFromArray(shinraEmployees);
		NSArray<Employee> uniqued = (NSArray<Employee>)ERXKey.unique().valueInObject(duplicates);
		assertTrue(ERXArrayUtilities.arraysAreIdenticalSets(shinraEmployees, uniqued));
	}

	@Test
	public void testAtUnique() {
		NSArray<Role> uniqueRoles = Company.EMPLOYEES.dot(Employee.ROLES).atFlatten().atUnique().arrayValueInObject(shinraInc);
		assertTrue(ERXArrayUtilities.arraysAreIdenticalSets(uniqueRoles, roles));
		assertTrue(uniqueRoles.count() == 3);
		NSArray<Role> roleArrays = Company.EMPLOYEES.dot(Employee.ROLES).atUnique().atFlatten().arrayValueInObject(shinraInc);
		assertTrue(roleArrays.count() == 6);
	}

	@Test
	public void testSumERXKeyOfQ() {
		BigDecimal d = Employee.PAYCHECKS.dot(ERXKey.sum(Paycheck.AMOUNT)).valueInObject(rufus);
		assertTrue(BigDecimal.valueOf(30000L).compareTo(d) == 0);
	}

	@Test
	public void testSum() {
		BigDecimal d = Employee.PAYCHECKS.dot(Paycheck.AMOUNT).dot(ERXKey.sum()).valueInObject(rufus);
		assertTrue(BigDecimal.valueOf(30000L).compareTo(d) == 0);
	}

	@Test
	public void testAtSumERXKeyOfQ() {
		BigDecimal d = Employee.PAYCHECKS.atSum(Paycheck.AMOUNT).valueInObject(rufus);
		assertTrue(BigDecimal.valueOf(30000L).compareTo(d) == 0);
	}

	@Test
	public void testAtSum() {
		BigDecimal d = Employee.PAYCHECKS.dot(Paycheck.AMOUNT).atSum().valueInObject(rufus);
		assertTrue(BigDecimal.valueOf(30000L).compareTo(d) == 0);
	}

	@Test
	public void testAvgERXKeyOfQ() {
		BigDecimal d = Employee.PAYCHECKS.dot(ERXKey.avg(Paycheck.AMOUNT)).valueInObject(rufus);
		assertTrue(BigDecimal.valueOf(10000L).compareTo(d) == 0);
	}

	@Test
	public void testAtAvgERXKeyOfQ() {
		BigDecimal d = Employee.PAYCHECKS.atAvg(Paycheck.AMOUNT).valueInObject(rufus);
		assertTrue(BigDecimal.valueOf(10000L).compareTo(d) == 0);
	}

	@Test
	public void testAvg() {
		BigDecimal d = Employee.PAYCHECKS.dot(Paycheck.AMOUNT).dot(ERXKey.avg()).valueInObject(rufus);
		assertTrue(BigDecimal.valueOf(10000L).compareTo(d) == 0);
	}

	@Test
	public void testAtAvg() {
		BigDecimal d = Employee.PAYCHECKS.dot(Paycheck.AMOUNT).atAvg().valueInObject(rufus);
		assertTrue(BigDecimal.valueOf(10000L).compareTo(d) == 0);
	}

	@Test
	public void testMinERXKeyOfU() {
		BigDecimal d = Employee.PAYCHECKS.dot(ERXKey.min(Paycheck.AMOUNT)).valueInObject(rufus);
		assertTrue(paycheck5000.amount().compareTo(d) == 0);
		NSTimestamp ts = Employee.PAYCHECKS.dot(ERXKey.min(Paycheck.PAYMENT_DATE)).valueInObject(rufus);
		assertTrue(paycheck5000.paymentDate().equals(ts));
		String s = Company.EMPLOYEES.dot(ERXKey.min(Employee.FIRST_NAME)).valueInObject(shinraInc);
		assertTrue(elena.firstName().equals(s));
	}

	@Test
	public void testAtMinERXKeyOfU() {
		BigDecimal d = Employee.PAYCHECKS.atMin(Paycheck.AMOUNT).valueInObject(rufus);
		assertTrue(paycheck5000.amount().compareTo(d) == 0);
		NSTimestamp ts = Employee.PAYCHECKS.atMin(Paycheck.PAYMENT_DATE).valueInObject(rufus);
		assertTrue(paycheck5000.paymentDate().equals(ts));
		String s = Company.EMPLOYEES.atMin(Employee.FIRST_NAME).valueInObject(shinraInc);
		assertTrue(elena.firstName().equals(s));
	}

	@Test
	public void testMin() {
		BigDecimal d = (BigDecimal) Employee.PAYCHECKS.dot(Paycheck.AMOUNT).dot(ERXKey.min()).valueInObject(rufus);
		assertTrue(paycheck5000.amount().compareTo(d) == 0);
		NSTimestamp ts = (NSTimestamp) Employee.PAYCHECKS.dot(Paycheck.PAYMENT_DATE).dot(ERXKey.min()).valueInObject(rufus);
		assertTrue(paycheck5000.paymentDate().equals(ts));
		String s = (String) Company.EMPLOYEES.dot(Employee.FIRST_NAME).dot(ERXKey.min()).valueInObject(shinraInc);
		assertTrue(elena.firstName().equals(s));
	}

	@Test
	public void testAtMin() {
		BigDecimal d = (BigDecimal) Employee.PAYCHECKS.dot(Paycheck.AMOUNT).atMin().valueInObject(rufus);
		assertTrue(paycheck5000.amount().compareTo(d) == 0);
		NSTimestamp ts = (NSTimestamp) Employee.PAYCHECKS.dot(Paycheck.PAYMENT_DATE).atMin().valueInObject(rufus);
		assertTrue(paycheck5000.paymentDate().equals(ts));
		String s = (String) Company.EMPLOYEES.dot(Employee.FIRST_NAME).atMin().valueInObject(shinraInc);
		assertTrue(elena.firstName().equals(s));
	}

	@Test
	public void testMaxERXKeyOfU() {
		BigDecimal d = Employee.PAYCHECKS.dot(ERXKey.max(Paycheck.AMOUNT)).valueInObject(rufus);
		assertTrue(paycheck15000.amount().compareTo(d) == 0);
		NSTimestamp ts = Employee.PAYCHECKS.dot(ERXKey.max(Paycheck.PAYMENT_DATE)).valueInObject(rufus);
		assertTrue(paycheck15000.paymentDate().equals(ts));
		String s = Company.EMPLOYEES.dot(ERXKey.max(Employee.FIRST_NAME)).valueInObject(shinraInc);
		assertTrue(sephiroth.firstName().equals(s));
	}

	@Test
	public void testAtMaxERXKeyOfU() {
		BigDecimal d = Employee.PAYCHECKS.atMax(Paycheck.AMOUNT).valueInObject(rufus);
		assertTrue(paycheck15000.amount().compareTo(d) == 0);
		NSTimestamp ts = Employee.PAYCHECKS.atMax(Paycheck.PAYMENT_DATE).valueInObject(rufus);
		assertTrue(paycheck15000.paymentDate().equals(ts));
		String s = Company.EMPLOYEES.atMax(Employee.FIRST_NAME).valueInObject(shinraInc);
		assertTrue(sephiroth.firstName().equals(s));
	}

	@Test
	public void testMax() {
		BigDecimal d = (BigDecimal) Employee.PAYCHECKS.dot(Paycheck.AMOUNT).dot(ERXKey.max()).valueInObject(rufus);
		assertTrue(paycheck15000.amount().compareTo(d) == 0);
		NSTimestamp ts = (NSTimestamp) Employee.PAYCHECKS.dot(Paycheck.PAYMENT_DATE).dot(ERXKey.max()).valueInObject(rufus);
		assertTrue(paycheck15000.paymentDate().equals(ts));
		String s = (String) Company.EMPLOYEES.dot(Employee.FIRST_NAME).dot(ERXKey.max()).valueInObject(shinraInc);
		assertTrue(sephiroth.firstName().equals(s));
	}

	@Test
	public void testAtMax() {
		BigDecimal d = (BigDecimal) Employee.PAYCHECKS.dot(Paycheck.AMOUNT).atMax().valueInObject(rufus);
		assertTrue(paycheck15000.amount().compareTo(d) == 0);
		NSTimestamp ts = (NSTimestamp) Employee.PAYCHECKS.dot(Paycheck.PAYMENT_DATE).atMax().valueInObject(rufus);
		assertTrue(paycheck15000.paymentDate().equals(ts));
		String s = (String) Company.EMPLOYEES.dot(Employee.FIRST_NAME).atMax().valueInObject(shinraInc);
		assertTrue(sephiroth.firstName().equals(s));
	}

	@Test
	public void testCount() {
		Integer count = Employee.PAYCHECKS.dot(ERXKey.count()).valueInObject(rufus);
		assertEquals(Integer.valueOf(3), count);
		count = ERXKey.count().valueInObject(shinraEmployees);
		assertEquals(Integer.valueOf(4), count);
	}

	@Test
	public void testAtCount() {
		Integer count = Employee.PAYCHECKS.atCount().valueInObject(rufus);
		assertEquals(Integer.valueOf(3), count);
		count = Company.EMPLOYEES.atCount().valueInObject(shinraInc);
		assertEquals(Integer.valueOf(4), count);
		count = Company.EMPLOYEES.dot(Employee.ROLES).atCount().valueInObject(shinraInc);
		assertEquals(Integer.valueOf(4), count);
	}

	public void testPopStdDev() {
		BigDecimal yuri = ERXKey.popStdDev().valueInObject(simpleNumbers);
		assertTrue(BigDecimal.valueOf(Math.sqrt(2)).compareTo(yuri) == 0);
	}

	public void testAtPopStdDev() {
		BigDecimal yuri = num.atPopStdDev().valueInObject(numbers);
		assertTrue(BigDecimal.valueOf(Math.sqrt(2)).compareTo(yuri) == 0);
	}

	public void testPopStdDevERXKeyOfQ() {
		BigDecimal yuri = ERXKey.popStdDev(num).valueInObject(numbers);
		assertTrue(BigDecimal.valueOf(Math.sqrt(2)).compareTo(yuri) == 0);
	}

	public void testAtPopStdDevERXKeyOfQ() {
		//Normally wouldn't need to do this, but works with strings as well as numbers
		BigDecimal yuri = num.atPopStdDev(new ERXKey<String>("toString")).valueInObject(numbers);
		assertTrue(BigDecimal.valueOf(Math.sqrt(2)).compareTo(yuri) == 0);
	}

	public void testStdDev() {
		BigDecimal yuri = ERXKey.stdDev().valueInObject(simpleNumbers);
		assertTrue(BigDecimal.valueOf(Math.sqrt(2.5)).compareTo(yuri) == 0);
	}

	public void testAtStdDev() {
		BigDecimal yuri = num.atStdDev().valueInObject(numbers);
		assertTrue(BigDecimal.valueOf(Math.sqrt(2.5)).compareTo(yuri) == 0);
	}

	public void testStdDevERXKeyOfQ() {
		BigDecimal yuri = ERXKey.stdDev(num).valueInObject(numbers);
		assertTrue(BigDecimal.valueOf(Math.sqrt(2.5)).compareTo(yuri) == 0);
	}

	public void testAtStdDevERXKeyOfQ() {
		//Normally wouldn't need to do this, but works with strings as well as numbers
		BigDecimal yuri = num.atStdDev(new ERXKey<String>("toString")).valueInObject(numbers);
		assertTrue(BigDecimal.valueOf(Math.sqrt(2.5)).compareTo(yuri) == 0);
	}

}
