package er.memoryadaptor;

import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOSQLExpression;
import com.webobjects.foundation.NSMutableDictionary;

/**
 * This stub exists only to make memory adaptor migrations function.
 * 
 * @author mschrag
 */
public class ERMemoryExpression extends EOSQLExpression {
  public ERMemoryExpression(EOEntity entity) {
    super(entity);
  }

  @Override
  public NSMutableDictionary bindVariableDictionaryForAttribute(EOAttribute paramEOAttribute, Object paramObject) {
    return null;
  }

}
