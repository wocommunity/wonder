package er.erxtest.tests;

import java.util.UUID;

import junit.framework.TestCase;

import com.webobjects.eocontrol.EOEditingContext;

import er.erxtest.model.Company;
import er.extensions.eof.ERXEC;
import er.extensions.eof.ERXEnterpriseObjectCache;

public class ERXEnterpriseObjectCacheTestCase extends TestCase {
  public void testInsert() {
    testInsert(true);
  }

  public void testUpdate() {
    testUpdate(true);
  }

  public void testDelete() {
    testDelete(true);
  }

  public void testInsertNoFetchInitialValues() {
    testInsert(false);
  }

  public void testUpdateNoFetchInitialValues() {
    testUpdate(false);
  }

  public void testDeleteNoFetchInitialValues() {
    testDelete(false);
  }

  public void testFetchWithFetchInitialValues() {
    String name = "Company " + UUID.randomUUID().toString();
    EOEditingContext editingContext = ERXEC.newEditingContext();
    Company c1 = Company.createCompany(editingContext, name);
    editingContext.saveChanges();

    ERXEnterpriseObjectCache<Company> cache = new ERXEnterpriseObjectCache<Company>(Company.ENTITY_NAME, Company.NAME_KEY, null, 0);
    Company c1Test = cache.objectForKey(editingContext, name);
    assertSame(c1, c1Test);
    cache.stop();
  }

  public void testFetchNoFetchInitialValues() {
    String name = "Company " + UUID.randomUUID().toString();
    EOEditingContext editingContext = ERXEC.newEditingContext();
    Company c1 = Company.createCompany(editingContext, name);
    editingContext.saveChanges();

    ERXEnterpriseObjectCache<Company> cache = new ERXEnterpriseObjectCache<Company>(Company.ENTITY_NAME, Company.NAME_KEY, null, 0);
    cache.setFetchInitialValues(false);
    Company c1Test = cache.objectForKey(editingContext, name);
    assertSame(c1, c1Test);
    cache.stop();
  }

  public void testFetchWithQualifier() {
    String name1 = "Company " + UUID.randomUUID().toString();
    EOEditingContext editingContext = ERXEC.newEditingContext();
    Company c1 = Company.createCompany(editingContext, name1);
    editingContext.saveChanges();

    String name2 = "Company Test " + UUID.randomUUID().toString();
    Company c2 = Company.createCompany(editingContext, name2);
    editingContext.saveChanges();

    ERXEnterpriseObjectCache<Company> cache = new ERXEnterpriseObjectCache<Company>(Company.ENTITY_NAME, Company.NAME_KEY, Company.NAME.contains("Test"), 0);
    cache.setFetchInitialValues(false);
    Company c1Test = cache.objectForKey(editingContext, name1);
    assertNull(c1Test);
    Company c2Test = cache.objectForKey(editingContext, name2);
    assertSame(c2, c2Test);
    cache.stop();
  }

  public void testUpdateWithQualifier() {
    String name1 = "Company " + UUID.randomUUID().toString();
    EOEditingContext editingContext = ERXEC.newEditingContext();
    Company c1 = Company.createCompany(editingContext, name1);
    editingContext.saveChanges();

    String name2 = "Company Test " + UUID.randomUUID().toString();
    Company c2 = Company.createCompany(editingContext, name2);
    editingContext.saveChanges();

    ERXEnterpriseObjectCache<Company> cache = new ERXEnterpriseObjectCache<Company>(Company.ENTITY_NAME, Company.NAME_KEY, Company.NAME.contains("Test"), 0);
    cache.setFetchInitialValues(false);
    Company c1Test = cache.objectForKey(editingContext, name1);
    assertNull(c1Test);
    Company c2Test = cache.objectForKey(editingContext, name2);
    assertSame(c2, c2Test);

    String name1Update = "Another Test Company " + UUID.randomUUID().toString();
    c1.setName(name1Update);
    editingContext.saveChanges();

    Company c1UpdateTest = cache.objectForKey(editingContext, name1Update);
    assertSame(c1, c1UpdateTest);

    String name2Update = "Company 2 " + UUID.randomUUID().toString();
    c2.setName(name2Update);
    editingContext.saveChanges();

    Company c2UpdateTest = cache.objectForKey(editingContext, name2Update);
    assertNull(c2UpdateTest);

    cache.stop();
  }

  public void testInsert(boolean fetchInitialValues) {
    ERXEnterpriseObjectCache<Company> cache = new ERXEnterpriseObjectCache<Company>(Company.ENTITY_NAME, Company.NAME_KEY, null, 0);
    cache.setFetchInitialValues(fetchInitialValues);
    EOEditingContext editingContext = ERXEC.newEditingContext();
    String name1 = "Company " + UUID.randomUUID().toString();
    Company c1 = Company.createCompany(editingContext, name1);
    editingContext.saveChanges();
    Company c1Test = cache.objectForKey(editingContext, name1);
    assertSame(c1, c1Test);
    cache.stop();
  }

  public void testDelete(boolean fetchInitialValues) {
    ERXEnterpriseObjectCache<Company> cache = new ERXEnterpriseObjectCache<Company>(Company.ENTITY_NAME, Company.NAME_KEY, null, 0);
    cache.setFetchInitialValues(fetchInitialValues);
    EOEditingContext editingContext = ERXEC.newEditingContext();
    String name1 = "Company " + UUID.randomUUID().toString();
    Company c1 = Company.createCompany(editingContext, name1);
    editingContext.saveChanges();
    Company c1Test = cache.objectForKey(editingContext, name1);
    assertSame(c1, c1Test);
    c1.delete();
    editingContext.saveChanges();
    Company c1DeleteTest = cache.objectForKey(editingContext, name1);
    assertNull(c1DeleteTest);
    cache.stop();
  }

  public void testUpdate(boolean fetchInitialValues) {
    ERXEnterpriseObjectCache<Company> cache = new ERXEnterpriseObjectCache<Company>(Company.ENTITY_NAME, Company.NAME_KEY, null, 0);
    cache.setFetchInitialValues(fetchInitialValues);
    EOEditingContext editingContext = ERXEC.newEditingContext();
    String name1 = "Company " + UUID.randomUUID().toString();
    Company c1 = Company.createCompany(editingContext, name1);
    editingContext.saveChanges();

    String name2 = "Company " + UUID.randomUUID().toString();
    c1.setName(name2);
    editingContext.saveChanges();

    Company c1DeleteTest = cache.objectForKey(editingContext, name1);
    assertNull(c1DeleteTest);

    Company c2UpdateTest = cache.objectForKey(editingContext, name2);
    assertSame(c1, c2UpdateTest);
    cache.stop();
  }
}
