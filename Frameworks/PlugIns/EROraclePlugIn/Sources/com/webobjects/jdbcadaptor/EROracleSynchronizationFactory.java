package com.webobjects.jdbcadaptor;

import com.webobjects.eoaccess.EOAdaptor;
import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOJoin;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.eoaccess.EOSQLExpression;
import com.webobjects.eoaccess.EOSynchronizationFactory;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;

/** Overrides OracleSynchronizationFactory. This class does not add any
 * additional implementation, its just there to be consistent with the
 * other EOF PlugIns
 * 
 * @author David Teran
 *
 */
public class EROracleSynchronizationFactory extends EOSynchronizationFactory {
  static String sequenceSetProc1 = "create procedure eo_set_sequence is\n    xxx number;\n    yyy number;\nbegin\n    select max(counter) into xxx from eo_temp_table;\n    if xxx is not NULL then\n        yyy := 0;\n        while (yyy < xxx) loop\n            select ";
  static String sequenceSetProc2 = ".nextval into yyy from dual;\n        end loop;\n    end if;\nend;\n";

  public EROracleSynchronizationFactory(EOAdaptor eoadaptor) {
    super(eoadaptor);
  }

  @Override
  public NSArray dropTableStatementsForEntityGroup(NSArray nsarray) {

    EOSQLExpression eosqlexpression = _expressionForString("drop table " + ((EOEntity) nsarray.objectAtIndex(0)).externalName() + " cascade constraints");
    NSArray nsarray1 = new NSArray(eosqlexpression);

    return nsarray1;
  }

//  @Override
//  public NSArray _statementsToDeleteTableNamedOptions(String s, NSDictionary nsdictionary) {
//
//    NSArray nsarray = new NSArray(_expressionForString("DROP TABLE " + s + " CASCADE CONSTRAINTS"));
//
//    return nsarray;
//  }

  @Override
  public NSArray primaryKeySupportStatementsForEntityGroup(NSArray nsarray) {

    NSMutableArray nsmutablearray = new NSMutableArray();

    EOEntity eoentity = null;

    boolean flag = true;

    for (int i = nsarray.count() - 1; i >= 0; i--) {
      eoentity = (EOEntity) nsarray.objectAtIndex(i);
      EOAttribute eoattribute = (EOAttribute) eoentity.primaryKeyAttributes().objectAtIndex(0);

      if (flag) {
        nsmutablearray.addObject(_expressionForString("create table eo_temp_table as select max(" + eoattribute.columnName() + ") counter from " + eoentity.externalName()));
        flag = false;
      }
      else {
        nsmutablearray.addObject(_expressionForString("insert into eo_temp_table select max(" + eoattribute.columnName() + ") counter from " + eoentity.externalName()));
      }
    }

    String s = eoentity.primaryKeyRootName() + "_SEQ";
    nsmutablearray.addObject(_expressionForString("create sequence " + s));
    nsmutablearray.addObject(_expressionForString(EROracleSynchronizationFactory.sequenceSetProc1 + s + EROracleSynchronizationFactory.sequenceSetProc2));
    nsmutablearray.addObject(_expressionForString("begin eo_set_sequence; end;"));
    nsmutablearray.addObject(_expressionForString("drop procedure eo_set_sequence"));
    nsmutablearray.addObject(_expressionForString("drop table eo_temp_table"));
    return nsmutablearray;
  }

  @Override
  public NSArray dropPrimaryKeySupportStatementsForEntityGroup(NSArray nsarray) {

    if (nsarray == null || nsarray.count() == 0) {
      return NSArray.EmptyArray;
    }
    else {
      EOEntity eoentity = (EOEntity) nsarray.lastObject();
      NSArray nsarray1 = new NSArray(_expressionForString("drop sequence " + eoentity.primaryKeyRootName() + "_SEQ"));

      return nsarray1;
    }
  }

  @Override
  public void appendExpressionToScript(EOSQLExpression eosqlexpression, StringBuffer stringbuffer) {

    if (eosqlexpression != null) {
      stringbuffer.append("" + eosqlexpression.statement() + "\n/\n\n");
    }
  }

  @Override
  public NSArray createDatabaseStatementsForConnectionDictionary(NSDictionary nsdictionary, NSDictionary nsdictionary1) {

    if (nsdictionary1 == null) {
      return null;
    }
    String s = (String) nsdictionary.objectForKey("username");
    if (s == null) {
      return null;
    }
    else {
      String s1 = (String) nsdictionary1.objectForKey("AdminInfoContainerNameKey");
      EOSQLExpression eosqlexpression = _expressionForString("CREATE USER " + s + " IDENTIFIED BY " + nsdictionary.objectForKey("password") + " " + (s1 == null ? "" : "DEFAULT TABLESPACE " + s1));
      EOSQLExpression eosqlexpression1 = _expressionForString("GRANT CONNECT, RESOURCE TO " + s);
      NSArray nsarray = new NSArray(new EOSQLExpression[] { eosqlexpression, eosqlexpression1 });

      return nsarray;
    }
  }

  @Override
  public NSArray dropDatabaseStatementsForConnectionDictionary(NSDictionary nsdictionary, NSDictionary nsdictionary1) {

    if (nsdictionary == null || nsdictionary1 == null) {
      return null;
    }
    String s = (String) nsdictionary.objectForKey("username");
    if (s == null) {
      return null;
    }
    else {
      NSArray nsarray = new NSArray(_expressionForString("DROP USER " + s + " CASCADE"));

      return nsarray;
    }
  }

  @Override
  public boolean supportsSchemaSynchronization() {
    return true;
  }

  @Override
  public NSArray foreignKeyConstraintStatementsForRelationship(EORelationship eorelationship) {

    EOEntity eoentity = eorelationship.entity();
    EOEntity eoentity1 = eorelationship.destinationEntity();
    NSMutableArray nsmutablearray = new NSMutableArray();
    NSMutableArray nsmutablearray1 = new NSMutableArray();
    NSArray nsarray = eoentity1.primaryKeyAttributes();
    NSArray nsarray1 = eorelationship.joins();
    int i = nsarray.count();

    EOModel eomodel = eoentity.model();
    EOModel eomodel1 = eoentity1.model();
    if (eomodel != eomodel1 && !eomodel.connectionDictionary().equals(eomodel1.connectionDictionary())) {
      return NSArray.EmptyArray;
    }

    if (eorelationship.isToMany()) {
      return NSArray.EmptyArray;
    }

    int k = 0;
    label0: for (int l = i; k < l; k++) {
      EOAttribute eoattribute = (EOAttribute) nsarray.objectAtIndex(k);
      nsmutablearray1.addObject(eoattribute.columnName());
      int j = nsarray1.count();
      do {
        if (j-- == 0) {
          continue label0;
        }
        EOJoin eojoin = (EOJoin) nsarray1.objectAtIndex(j);
        if (eojoin.destinationAttribute() == eoattribute) {
          nsmutablearray.addObject(eojoin.sourceAttribute().columnName());
        }
      } while (true);
    }
    if (nsmutablearray.count() != nsarray1.count()) {
      return NSArray.EmptyArray;
    }
    else {
      EOSQLExpression eosqlexpression = _expressionForEntity(eoentity);
      eosqlexpression.prepareConstraintStatementForRelationship(eorelationship, nsmutablearray, nsmutablearray1);
      return new NSArray(eosqlexpression);
    }
  }

  @Override
  public NSArray statementsToDeleteColumnNamed(String columnName, String tableName, NSDictionary options) {
    return new NSArray(_expressionForString("alter table " + tableName + " drop column " + columnName.toUpperCase() + ""));
  }

}
