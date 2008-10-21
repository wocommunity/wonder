package com.webobjects.jdbcadaptor;

import com.webobjects.eoaccess.EOSynchronizationFactory;

/** Overrides OraclePlugIn in order to provide the modified
 * EROracleExpression class to EOF.
 * 
 * @author David Teran
 *
 */
public class EROraclePlugIn extends OraclePlugIn {
  public EROraclePlugIn(JDBCAdaptor jdbcadaptor) {
    super(jdbcadaptor);
    System.out.println("EROraclePlugIn.enclosing_method: ");
  }

  /* (non-Javadoc)
   * @see com.webobjects.jdbcadaptor.JDBCPlugIn#defaultExpressionClass()
   */
  public Class defaultExpressionClass() {
    return EROracleExpression.class;
  }

  /* (non-Javadoc)
   * @see com.webobjects.jdbcadaptor.JDBCPlugIn#createSynchronizationFactory()
   */
  public EOSynchronizationFactory createSynchronizationFactory() {
    System.out.println("EROraclePlugIn.createSynchronizationFactory: ");
    return new EROracleSynchronizationFactory(adaptor());
  }
}
