package com.webobjects.jdbcadaptor;


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
  @Override
  public Class<? extends JDBCExpression> defaultExpressionClass() {
    return EROracleExpression.class;
  }

//  /* (non-Javadoc)
//   * @see com.webobjects.jdbcadaptor.JDBCPlugIn#createSynchronizationFactory()
//   */
//  public EOSynchronizationFactory createSynchronizationFactory() {
//    return new EROracleSynchronizationFactory(adaptor());
//  }
}
