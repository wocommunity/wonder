package er.erxtest;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import com.webobjects.eoaccess.EODatabaseContext;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOFaultHandler;
import com.webobjects.eocontrol.EOFaulting;
import com.webobjects.eocontrol.EOGlobalID;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSTimestamp;

import er.erxtest.model.Company;
import er.erxtest.model.Employee;
import er.erxtest.model.Paycheck;
import er.erxtest.model.Role;
import er.extensions.eof.ERXEC;
import er.extensions.eof.ERXEOAccessUtilities;
import er.extensions.eof.ERXEOAccessUtilities.DatabaseContextOperation;
import er.extensions.foundation.ERXAssert;
import er.extensions.qualifiers.ERXKeyValueQualifier;


public class ERXTestUtilities {

	/** Fix the ERXTest model so that it can be used by any give adaptor. What needs to be
	 * done to the model? In some cases, very little. For some adaptor, problematic attributes
	 * may need to be fixed.
	 * 
	 * @param adaptorName for example, "Memory" or "MySQL".
	 */
	@SuppressWarnings("cast")
	public static void fixModelsForAdaptorNamed(String adaptorName) {
		
		if (adaptorName.equals("Memory")) {
			for (EOModel model: (NSArray<EOModel>)EOModelGroup.defaultGroup().models()) {
				model.setAdaptorName("Memory");
				model.setConnectionDictionary((NSDictionary<String, Object>) NSDictionary.EmptyDictionary);
			}
			return;
		}

		if (adaptorName.equals("MySQL")) {

			NSDictionary conn = new NSDictionary(
					Arrays.asList(
							Application.wobuild.getProperty("wo.test.dbAccess.MySQL.URL"),
							Application.wobuild.getProperty("wo.test.dbAccess.MySQL.name"),
							Application.wobuild.getProperty("wo.test.dbAccess.MySQL.password")).toArray(),
					Arrays.asList("URL", "username", "password").toArray());

			EOEditingContext ec = ERXEC.newEditingContext();
			
			for (EOModel model: (NSArray<EOModel>)EOModelGroup.defaultGroup().models()) {
				model.setAdaptorName("JDBC");
				model.setConnectionDictionary(conn);
				EODatabaseContext.forceConnectionWithModel(model, conn, ec);
			}

			return;
		}
	}
	
	public static String randomName(String prefix) {
		return prefix+"_"+System.currentTimeMillis()+"_"+(new Random()).nextDouble();
	}

	public static void deleteObjectsWithPrefix(EOEditingContext ec, String entityName, String prefix) {
		for (Object obj : (NSArray<Object>)EOUtilities.objectsWithQualifierFormat(ec, entityName, "name like '"+prefix+"_*'", null)) {
			//System.out.println("deleteObjectsWithPrefix:: deleting object: "+obj);
			ec.deleteObject((EOEnterpriseObject)obj);
		}
		ec.saveChanges();
	}

	public static void deleteObjectsWithPrefix(String entityName, String prefix) {
		EOEditingContext ec = ERXEC.newEditingContext();
		for (Object obj : (NSArray<Object>)EOUtilities.objectsWithQualifierFormat(ec, entityName, "name = '"+prefix+"_*'", null)) {
			ec.deleteObject((EOEnterpriseObject)obj);
		}
		ec.saveChanges();
	}
	
	/**
	 * @param eo
	 * @param relationshipName
	 * @return the EODatabaseContext/EODatabase toMany snapshot array for a toMany relationship on eo.
	 */
	public static NSArray snapshotArrayForRelationshipInObject(EOEnterpriseObject object, final String relationshipName) {
	    final EOEditingContext ec = object.editingContext();
	    EOEntity entity = EOUtilities.entityForObject(ec, object);

        final EOGlobalID gid = ec.globalIDForObject(object);
        String modelName = entity.model().name();
        final EODatabaseContext dbc = EOUtilities.databaseContextForModelNamed(ec, modelName);

        NSArray toManySnapshot = ERXEOAccessUtilities.executeDatabaseContextOperation(dbc, 2,
                new DatabaseContextOperation<NSArray>() {
                    public NSArray execute(EODatabaseContext databaseContext) throws Exception {
                        // Search for and return the snapshot
                        return dbc.snapshotForSourceGlobalID(gid, relationshipName, ec.fetchTimestamp());
                    }
                });
        return toManySnapshot;
	}
	
    /**
     * @param eo
     * @return the EODatabaseContext/EODatabase snapshot for eo.
     */
    public static NSDictionary snapshotForObject(EOEnterpriseObject object) {
        final EOEditingContext ec = object.editingContext();
        EOEntity entity = EOUtilities.entityForObject(ec, object);

        final EOGlobalID gid = ec.globalIDForObject(object);
        String modelName = entity.model().name();
        final EODatabaseContext dbc = EOUtilities.databaseContextForModelNamed(ec, modelName);

        NSDictionary snapshot = ERXEOAccessUtilities.executeDatabaseContextOperation(dbc, 2,
                new DatabaseContextOperation<NSDictionary>() {
                    public NSDictionary execute(EODatabaseContext databaseContext) throws Exception {
                        // Search for and return the snapshot
                        return dbc.snapshotForGlobalID(gid, ec.fetchTimestamp());
                    }
                });
        return snapshot;
    }
    
	/**
	 * Quick utility to be DRY in unit tests.
	 * 
	 * @param object
	 */
	public static void fireFault(Object object) {
	    if (EOFaultHandler.isFault(object)) {
            EOFaulting fault = (EOFaulting)object;
            fault.faultHandler().completeInitializationOfObject(fault);
            ERXAssert.POST.isFalse(EOFaultHandler.isFault(object));
        }
	}
	
	/**
	 * Useful for clearing the database before a unit test.
	 */
	public static void deleteAllObjects() {
		EOEditingContext ec = ERXEC.newEditingContext();
		// Hacky way to qualify all objects in any entity in ERXTest, assuming pk is always "id"
		EOQualifier q = new ERXKeyValueQualifier("id", EOQualifier.QualifierOperatorNotEqual, Integer.valueOf(-1));
		ERXEOAccessUtilities.deleteRowsDescribedByQualifier(ec, Company.ENTITY_NAME, q);
		ERXEOAccessUtilities.deleteRowsDescribedByQualifier(ec, Employee.ENTITY_NAME, q);
		ERXEOAccessUtilities.deleteRowsDescribedByQualifier(ec, Paycheck.ENTITY_NAME, q);
		ERXEOAccessUtilities.deleteRowsDescribedByQualifier(ec, Role.ENTITY_NAME, q);
		
		// Join table hacky delete
		q = new ERXKeyValueQualifier("roleId", EOQualifier.QualifierOperatorNotEqual, Integer.valueOf(-1));
		ERXEOAccessUtilities.deleteRowsDescribedByQualifier(ec, "EmployeeRole", q);
	}
	
	/**
	 * Convenience for simple tests.
	 * 
	 * @return the gid of the created company having 3 employees.
	 */
	public static EOGlobalID createCompanyAnd3Employees() {
		
		EOEditingContext ec = ERXEC.newEditingContext();
		ec.lock();
		try {
			Company c = (Company) EOUtilities.createAndInsertInstance(ec, Company.ENTITY_NAME);
			c.setName(randomName("Disney World"));
			Employee e1 = c.createEmployeesRelationship();
			e1.setFirstName(randomName("Mickey"));
			e1.setLastName(randomName("Mouse"));
			e1.setManager(Boolean.FALSE);
			Employee e2 = c.createEmployeesRelationship();
			e2.setFirstName(randomName("Donald"));
			e2.setLastName(randomName("Duck"));
			e2.setManager(Boolean.FALSE);
			Employee e3 = c.createEmployeesRelationship();
			e3.setFirstName(randomName("Goofy"));
			e3.setLastName(randomName("Dog"));
			e3.setManager(Boolean.FALSE);
			
			// Give Mickey a paycheck
			Paycheck p = (Paycheck) EOUtilities.createAndInsertInstance(ec, Paycheck.ENTITY_NAME);
			p.setAmount(BigDecimal.valueOf(12345.67));
			p.setCashed(Boolean.FALSE);
			p.setPaymentDate(new NSTimestamp());
			
			p.setEmployeeRelationship(e1);
			
			ec.saveChanges();
			return ec.globalIDForObject(c);
		} finally {
			ec.unlock();
		}
	}
	
	/**
	 * Convenience for simple tests.
	 * 
	 * @return the gid of the created company having zero employees.
	 */
	public static EOGlobalID createCompanyAndNoEmployees() {
		
		EOEditingContext ec = ERXEC.newEditingContext();
		ec.lock();
		try {
			Company c = (Company) EOUtilities.createAndInsertInstance(ec, Company.ENTITY_NAME);
			c.setName("Disney World");
			ec.saveChanges();
			return ec.globalIDForObject(c);
		} finally {
			ec.unlock();
		}
	}

	public static void deleteObjectsWithPrefix(EOEditingContext ec, NSArray<Object> eos) {
		for (Object obj : eos) {
			ec.deleteObject((EOEnterpriseObject)obj);
		}
		ec.saveChanges();
	}

	public static int pkOne(EOEditingContext ec, EOEnterpriseObject eo) {
		return ((Integer)((List)EOUtilities.primaryKeyForObject(ec, eo).values()).get(0)).intValue();
	}
}
