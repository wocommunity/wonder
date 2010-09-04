package com.webobjects.jdbcadaptor;

import com.webobjects.foundation.NSDictionary;

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

  @Override
  public NSDictionary jdbcInfo() {
    return super.jdbcInfo();
  }

  /* (non-Javadoc)
   * @see com.webobjects.jdbcadaptor.JDBCPlugIn#createSynchronizationFactory()
   */
//  public EOSynchronizationFactory createSynchronizationFactory() {
//    return new EROracleSynchronizationFactory(adaptor());
//  }
}
