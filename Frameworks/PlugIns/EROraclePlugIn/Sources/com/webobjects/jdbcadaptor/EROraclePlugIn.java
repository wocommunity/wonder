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
    return new EROracleSynchronizationFactory(adaptor());
  }
}
