import com.apple.cocoa.foundation.*;
import com.apple.yellow.eoaccess.*;
import com.apple.yellow.eocontrol.*;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

public class FrontbasePlugIn extends JDBCPlugIn {
    static final String _frontbaseIncludeSynonyms                       = System.getProperty("jdbcadaptor.frontbase.includeSynonyms", null);
    static final String _frontbaseWildcardPatternForAttributes 		= System.getProperty("jdbcadaptor.frontbase.wildcardPatternForAttributes", null);
    static final String _frontbaseWildcardPatternForTables              = System.getProperty("jdbcadaptor.frontbase.wildcardPatternForTables", "%");
    static final String _frontbaseWildcardPatternForSchema              = System.getProperty("jdbcadaptor.frontbase.wildcardPatternForSchema", null);
    static final String _frontbaseSqlStatementForGettingProcedureNames 	= System.getProperty("jdbcadaptor.frontbase.sqlStatementForGettingProcedureNames", null);
    static final String _frontbaseStoredProcedureCatalogPattern         = System.getProperty("jdbcadaptor.frontbase.storedProcedureCatalogPattern", null);
    static final String _frontbaseStoredProcedureSchemaPattern 		= System.getProperty("jdbcadaptor.frontbase.storedProcedureSchemaPattern", null);
    static final String _frontbaseSqlStatementForGettingTableNames	= System.getProperty("jdbcadaptor.frontbase.sqlStatementForGettingTableNames", null);


    public FrontbasePlugIn(JDBCAdaptor jdbcadaptor) {
        super(jdbcadaptor);
        NSLog.allowDebugLoggingForGroups(NSLog.DebugGroupDatabaseAccess);
    }


    public static String getPlugInVersion() {
        return "2.6.4";
    }


    public boolean canDescribeStoredProcedure(String s) {
        return true;
    }


    public EOSynchronizationFactory createSynchronizationFactory() {
        return new FrontbasePlugIn.FrontbaseSynchronizationFactory(_adaptor);
    }


    public String defaultDriverName() {
        return "com.frontbase.jdbc.FBJDriver";
    }


    public String databaseProductName() {
        return "FrontBase";
    }


    public Class defaultExpressionClass() {
        return FrontbaseExpression.class;
    }


    public String wildcardPatternForSchema() {
        if (_frontbaseWildcardPatternForSchema != null) 
            return _frontbaseWildcardPatternForSchema;
        else {
           String schema = (String) adaptor().connectionDictionary().objectForKey("schema");
           return (schema != null) ? schema.toUpperCase() : "CURRENT_SCHEMA";
        }
    }


    public String schemaNameForEntity(EOEntity eoentity) {
       String s = super.schemaNameForEntity(eoentity);

       if (s == null) {
          s = (String) adaptor().connectionDictionary().objectForKey("schema");
          return (s != null) ? s.toUpperCase() : "CURRENT_SCHEMA";
       }
       else
          return s;
    }

    
    public String storedProcedureSchemaPattern() {
        if (_frontbaseStoredProcedureSchemaPattern != null)
            return _frontbaseStoredProcedureSchemaPattern;
        else
            return "CURRENT_SCHEMA";
    }


    public NSDictionary jdbcInfo() {
        NSMutableDictionary dictionary = new NSMutableDictionary(super.jdbcInfo());
        NSMutableDictionary dictionary1 = new NSMutableDictionary((NSDictionary) dictionary.objectForKey("typeInfo"));
        NSDictionary nsdictionary = (NSDictionary) dictionary1.objectForKey("CHARACTER");
        dictionary1.setObjectForKey(nsdictionary, "CHAR");
        nsdictionary = (NSDictionary) dictionary1.objectForKey("CHARACTER VARYING");
        dictionary1.setObjectForKey(nsdictionary, "VARCHAR");
        dictionary1.setObjectForKey(nsdictionary, "CHAR VARYING");
        nsdictionary = (NSDictionary) dictionary1.objectForKey("BIT");
        dictionary1.setObjectForKey(nsdictionary, "BYTE");
        nsdictionary = (NSDictionary) dictionary1.objectForKey("BIT VARYING");
        dictionary1.setObjectForKey(nsdictionary, "BYTE VARYING");

        dictionary.setObjectForKey(dictionary1, "typeInfo");
        JDBCContext jdbccontext = adaptor()._cachedAdaptorContext();

        try {
            jdbccontext.connection().commit();
        }
        catch (SQLException sqlexception) {
            if (NSLog.debugLoggingAllowedForLevelAndGroups(3, 0x000010000L))
                NSLog.debug.appendln(sqlexception);
        }
        return dictionary;
    }


/*    public Properties connectionPropertiesForConnectionDictionary(NSDictionary connectionDictionary){
        Properties properties = super.connectionPropertiesForConnectionDictionary(connectionDictionary);

        //Check for dbPasswd in connection Dictionary
        Object temp = connectionDictionary.objectForKey("dbpasswd");
        if (temp != null){
            properties.put ("dbpasswd", temp);
        }
        //Check for session in connection Dictionary
        temp = connectionDictionary.objectForKey("session");
        if (temp != null){
                properties.put ("session", temp);
        }
        //Check for session in connection Dictionary
        temp = connectionDictionary.objectForKey("system");
        if (temp != null){
            properties.put ("system", temp);
        }
        //Check for session in connection Dictionary
        temp = connectionDictionary.objectForKey("isolation");
        if (temp != null){
            properties.put ("isolation", temp);
        }
        //Check for session in connection Dictionary
        temp = connectionDictionary.objectForKey("locking");
        if (temp != null){
            properties.put ("locking", temp);
        }
        //Check for session in connection Dictionary
        temp = connectionDictionary.objectForKey("readOnly");
        if (temp != null){
            properties.put ("readOnly", temp);
        }
        return properties;
    }
*/

    public Object fetchBLOB(ResultSet resultset, int i, EOAttribute attribute, boolean flag) throws SQLException {
        Blob blob = resultset.getBlob(i);
        
        if (blob == null)
            return null;
            
        if (!flag)
            return blob;
        else {
            try {
                return new NSData(blob.getBytes(1, (int) blob.length()));
            }
            catch(Exception ioexception) {
                throw new JDBCAdaptorException(ioexception.getMessage(), null);
            }
        }
    }


    public Object fetchCLOB(ResultSet resultset, int i, EOAttribute attribute, boolean flag) throws SQLException {
        Clob clob = resultset.getClob(i);
        
        if (clob == null)
            return null;
            
        if (!flag)
            return clob;
        else
            return clob.getSubString(1L, (int) clob.length());
    }


    public NSArray newPrimaryKeys(int i, EOEntity eoentity, JDBCChannel jdbcchannel) {
        NSMutableArray nsmutablearray = new NSMutableArray();
        
        for (int j = 0 ; j < i ; j++) {
            NSDictionary nsdictionary = _newPrimaryKey(eoentity, jdbcchannel);
            
            if (nsdictionary == null)
                return null;
                
            nsmutablearray.addObject(nsdictionary);
        }
        return nsmutablearray;
    }


    private NSDictionary _newPrimaryKey(EOEntity eoentity, JDBCChannel jdbcchannel) {
       String sql;

       NSArray primaryKeyAttributes = eoentity.primaryKeyAttributes();

       NSMutableDictionary d = new NSMutableDictionary();
       d.setObjectForKey("UNIQUE", "name");

       if (primaryKeyAttributes == null)
          return null;

       EOAttribute attribute = (EOAttribute) primaryKeyAttributes.lastObject();

       if (attribute.valueClassName().endsWith("NSData")) {
          d.setObjectForKey("NSData", "valueClassName");

          if (attribute.externalType().startsWith("BIT")) {
             sql = "VALUES NEW_UID(" + (attribute.width() >> 3) + ");";
          }
          else {
             sql = "VALUES NEW_UID(" + attribute.width() + ");";
          }
       }
       else {
          sql = "SELECT UNIQUE FROM " + quoteTableName(eoentity.primaryKeyRootName());
          d.setObjectForKey("NSNumber", "valueClassName");
       }

       EOSQLExpression eosqlexpression = expressionFactory().expressionForString(sql);
       NSArray tmp = jdbcchannel._fetchRowsForSQLExpressionAndAttributes(eosqlexpression, new NSArray(new EOAttribute(d, null)));

       if (tmp != null && tmp.count() > 0) {
          Object obj = ((NSDictionary) tmp.lastObject()).objectForKey("UNIQUE");

          NSMutableDictionary result = new NSMutableDictionary();

          for (int i = 0 ; i < primaryKeyAttributes.count() ; i++) {
             result.setObjectForKey(obj, ((EOAttribute) primaryKeyAttributes.objectAtIndex(i)).name());
          }

          return result;
       }
       else
          return null;
    }
    
    
    protected static final int FB_Boolean 	= 1;
    protected static final int FB_Integer 	= 2;
    protected static final int FB_SmallInteger	= 3;
    protected static final int FB_Float 	= 4;
    protected static final int FB_Real 		= 5;
    protected static final int FB_Double	= 6;
    protected static final int FB_Numeric	= 7;
    protected static final int FB_Decimal	= 8;
    protected static final int FB_Character	= 9;
    protected static final int FB_VCharacter	= 10;
    protected static final int FB_Bit		= 11;
    protected static final int FB_VBit		= 12;
    protected static final int FB_Date		= 13;
    protected static final int FB_Time		= 14;
    protected static final int FB_TimeTZ	= 15;
    protected static final int FB_Timestamp	= 16;
    protected static final int FB_TimestampTZ	= 17;
    protected static final int FB_YearMonth	= 18;
    protected static final int FB_DayTime	= 19;
    protected static final int FB_CLOB		= 20;
    protected static final int FB_BLOB		= 21;
    protected static final int FB_TinyInteger	= 22;
    protected static final int FB_LongInteger	= 23;


    protected static String quoteTableName(String s) {
        if (s == null)
            return null;
        int i = s.lastIndexOf(46);
        if (i == -1)
            return "\"" + s + "\"";
        else
            return "\"" + s.substring(0, i) + "\".\"" + s.substring(i + 1, s.length()) + "\"";
    }


    protected static int internalTypeForExternal(String externalType) {
        if (externalType.equals("BOOLEAN")) 
            return FB_Boolean;
        else if (externalType.equals("INTEGER") || externalType.equals("INT")) 
            return FB_Integer;
        else if (externalType.equals("SMALLINT")) 
            return FB_SmallInteger;
        else if (externalType.equals("LONGINT")) 
            return FB_LongInteger;
        else if (externalType.equals("TINYINT")) 
            return FB_TinyInteger;
        else if (externalType.equals("FLOAT")) 
            return FB_Float;
        else if (externalType.equals("REAL")) 
            return FB_Real;
        else if (externalType.equals("DOUBLE PRECISION")) 
            return FB_Double;
        else if (externalType.equals("NUMERIC")) 
            return FB_Numeric;
        else if (externalType.equals("DECIMAL")) 
            return FB_Decimal;
        else if (externalType.equals("CHAR") || externalType.equals("CHARACTER")) 
            return FB_Character;
        else if (externalType.equals("VARCHAR") || externalType.equals("CHARACTER VARYING") || externalType.equals("CHAR VARYING")) 
            return FB_VCharacter;
        else if (externalType.equals("BIT") || externalType.equals("BYTE")) 
            return FB_Bit;
        else if (externalType.equals("BIT VARYING") || externalType.equals("BYTE VARYING")) 
            return FB_VBit;
        else if (externalType.equals("DATE")) 
            return FB_Date;
        else if (externalType.equals("TIME")) 
            return FB_Time;
        else if (externalType.equals("TIME WITH TIME ZONE")) 
            return FB_TimeTZ;
        else if (externalType.equals("TIMESTAMP")) 
            return FB_Timestamp;
        else if (externalType.equals("TIMESTAMP WITH TIME ZONE")) 
            return FB_TimestampTZ;
        else if (externalType.equals("BLOB")) 
            return FB_BLOB;
        else if (externalType.equals("CLOB")) 
            return FB_CLOB;
        else
            return -1;
    }


    public static class FrontbaseSynchronizationFactory extends EOSynchronizationFactory {
        NSArray EmptyArray = new NSArray();


        public FrontbaseSynchronizationFactory(EOAdaptor eoadaptor) {
            super(eoadaptor);
        }


        public FrontbaseSynchronizationFactory() {
            super();
        }


        public boolean supportsSchemaSynchronization() {
            return true;
        }


/*
    public String _nameInObjectStoreForEntityGroupWithChangeDictionary(NSArray nsarray, NSDictionary nsdictionary)
    {
        System.out.println((String)nsdictionary.objectForKey("externalName"));
        System.out.println(((EOEntity)nsarray.lastObject()).externalName());
        return super._nameInObjectStoreForEntityGroupWithChangeDictionary( nsarray,  nsdictionary);
    }

        public NSArray _entityGroupInModelForTableNamed(EOModel eomodel, String s) {
            System.out.println("_entityGroupInModelForTableNamed");
            System.out.println("s: " + eomodel);
            System.out.println("s: " + s);
            NSArray result = super._entityGroupInModelForTableNamed(eomodel, s);
            System.out.println(result);
            return result;
        }

*/        

        public NSArray schemaCreationStatementsForEntities(NSArray entities, NSDictionary options) {
            NSMutableArray result = new NSMutableArray();

            if (entities == null || entities.count() == 0)
                return result;
    
            NSDictionary nsdictionary1 = ((EOEntity) entities.lastObject()).model().connectionDictionary();

            if (HackedUtils.boolValueForKeyDefault(options, "dropDatabase", false)) {
                result.addObjectsFromArray(dropDatabaseStatementsForConnectionDictionary(nsdictionary1, null));
            }
            if (HackedUtils.boolValueForKeyDefault(options, "createDatabase", false)) {
                result.addObjectsFromArray(createDatabaseStatementsForConnectionDictionary(nsdictionary1, null));
            }
            if (HackedUtils.boolValueForKeyDefault(options, "dropPrimaryKeySupport", true)) {
                NSArray nsarray1 = primaryKeyEntityGroupsForEntities(entities);
                result.addObjectsFromArray(dropPrimaryKeySupportStatementsForEntityGroups(nsarray1));
            }
            if (HackedUtils.boolValueForKeyDefault(options, "dropTables", true)) {
                NSArray nsarray2 = tableEntityGroupsForEntities(entities);
                result.addObjectsFromArray(dropTableStatementsForEntityGroups(nsarray2));
            }
            if (HackedUtils.boolValueForKeyDefault(options, "createTables", true)) {
                NSArray nsarray3 = tableEntityGroupsForEntities(entities);
                result.addObjectsFromArray(createTableStatementsForEntityGroups(nsarray3));
                result.addObjectsFromArray(createIndexStatementsForEntityGroups(nsarray3));
            }
            if (HackedUtils.boolValueForKeyDefault(options, "createPrimaryKeySupport", true)) {
                NSArray nsarray4 = primaryKeyEntityGroupsForEntities(entities);
                result.addObjectsFromArray(primaryKeySupportStatementsForEntityGroups(nsarray4));
            }
            if (HackedUtils.boolValueForKeyDefault(options, "primaryKeyConstraints", true)) {
                NSArray nsarray5 = tableEntityGroupsForEntities(entities);
                result.addObjectsFromArray(primaryKeyConstraintStatementsForEntityGroups(nsarray5));
            }
            if (HackedUtils.boolValueForKeyDefault(options, "foreignKeyConstraints", false)) {
                NSMutableSet nsmutableset = new NSMutableSet();
                NSArray nsarray6 = tableEntityGroupsForEntities(entities);
                for(int i = 0 ; i < nsarray6.count(); i++)
                    result.addObjectsFromArray(_foreignKeyConstraintStatementsForEntityGroup((NSArray)nsarray6.objectAtIndex(i)));
            }
            
            if (result != null && result.count() > 0) {
                NSMutableArray mutablearray = new NSMutableArray();
                
                mutablearray.addObject(_expressionForString("SET TRANSACTION ISOLATION LEVEL SERIALIZABLE, LOCKING PESSIMISTIC"));
                mutablearray.addObjectsFromArray(result);
                mutablearray.addObject(_expressionForString("COMMIT"));
                return mutablearray;
            }
            else {
                return result;
            }
        }


        public NSArray dropPrimaryKeySupportStatementsForEntityGroups(NSArray nsarray) {
            return new NSArray(_expressionForString("-- The 'Drop Primary Key Support' option is unavailable."));
        }


        public NSArray dropDatabaseStatementsForConnectionDictionary(NSDictionary nsdictionary, NSDictionary nsdictionary1) {

            return new NSArray(_expressionForString("-- The 'Drop Database' option is unavailable."));
        }


        public NSArray createDatabaseStatementsForConnectionDictionary(NSDictionary nsdictionary, NSDictionary nsdictionary1) {
            return new NSArray(_expressionForString("-- The 'Create Database' option is unavailable."));
        }


        public NSArray dropTableStatementsForEntityGroup(NSArray nsarray) {
            EOEntity entity = (EOEntity) nsarray.objectAtIndex(0);
            NSDictionary dictionary = entity.userInfo();
            String dropType = " CASCADE";
            
            if (dictionary != null) {
                String type = (String) dictionary.valueForKey("Restrict");
                if (type != null && type.equals("true"))
                    dropType = " RESTRICT";
            }
            
            EOSQLExpression expression = _expressionForString("DROP TABLE " + quoteTableName(entity.externalName()) + dropType);
            
            NSArray result = new NSArray(expression);
            _retain(result);
            return result;
        }


        public boolean _isTableCopyingRequiredByColumnChangesToEntityGroup(NSDictionary nsdictionary, NSArray nsarray) {
            return false;
        }


        public NSArray statementsToImplementForeignKeyConstraintsOnEntityGroups(NSArray nsarray, NSDictionary nsdictionary, NSDictionary nsdictionary1) {
            if (HackedUtils.boolValueForKeyDefault(nsdictionary1, "EOSchemaSynchronizationForeignKeyConstraintsKey", true))
                return super.statementsToImplementForeignKeyConstraintsOnEntityGroups(nsarray,  nsdictionary,  nsdictionary1);
            else return EmptyArray;
        }


        public NSArray statementsToImplementPrimaryKeyConstraintsOnEntityGroups(NSArray nsarray, NSDictionary nsdictionary, NSDictionary nsdictionary1) {
            if (HackedUtils.boolValueForKeyDefault(nsdictionary1, "EOSchemaSynchronizationPrimaryKeyConstraintsKey", true))
                return primaryKeyConstraintStatementsForEntityGroups(nsarray);
            else return EmptyArray;
        }


        public NSArray statementsToImplementPrimaryKeySupportForEntityGroups(NSArray nsarray, NSDictionary nsdictionary, NSDictionary nsdictionary1) {
            if (HackedUtils.boolValueForKeyDefault(nsdictionary1, "EOSchemaSynchronizationPrimaryKeySupportKey", true))
                return primaryKeySupportStatementsForEntityGroups(_primaryKeyEntityGroupsForEntityGroups(nsarray));
            else return EmptyArray;
        }

        
        // Synchronisation adds a commt itself making this obsolete. 
/*        public NSArray statementsToUpdateObjectStoreForModel(EOModel eomodel, NSDictionary nsdictionary, NSDictionary nsdictionary1) {
            NSArray nsarray = super.statementsToUpdateObjectStoreForModel(eomodel, nsdictionary, nsdictionary1);

            if (nsarray != null && nsarray.count() > 0) {
            	NSMutableArray nsmutablearray = new NSMutableArray();
                nsmutablearray.addObjectsFromArray(nsarray);
                nsmutablearray.addObject(_expressionForString("COMMIT"));
                return nsmutablearray;
            }
            else {
                return nsarray;
            }
	}
  */     

        public NSArray _statementsToDirectlyUpdateObjectStoreForEntityGroup(NSArray nsarray, NSDictionary nsdictionary, NSDictionary nsdictionary1) {
            if (nsdictionary1 == null)
                nsdictionary1 = HackedUtils.EmptyDictionary;
            if (nsdictionary == null)
                nsdictionary = HackedUtils.EmptyDictionary;
            NSMutableArray nsmutablearray = new NSMutableArray();
            _retain(nsmutablearray);
            NSMutableArray nsmutablearray1 = new NSMutableArray();
            NSMutableArray nsmutablearray2 = new NSMutableArray();
            String s = ((EOEntity) nsarray.lastObject()).externalName();
            NSDictionary nsdictionary2 = (NSDictionary) nsdictionary.objectForKey("updated");
            String s1 = (String) nsdictionary.objectForKey("externalName");
            if (s1 != null)
                nsmutablearray.addObjectsFromArray(_statementsCommentedWithString(statementsToRenameTableNamed(s1, s, nsdictionary1), "statementsToRenameTableNamed:" + s1 + " newName:" + s));
            if (nsdictionary2 != null) {
                NSArray nsarray1 = nsdictionary2.allKeys();
                nsmutablearray1.addObjectsFromArray(nsarray1);
                for (int j = 0 ; j < nsarray1.count() ; j++) {
                    String s3 = (String) nsarray1.objectAtIndex(j);
                    EOAttribute eoattribute = _firstAttributeInEntityGroupWithColumnName(nsarray, s3);
                    NSDictionary nsdictionary3 = (NSDictionary) nsdictionary2.objectForKey(s3);
                    String s2 = (String) nsdictionary3.objectForKey("columnName");
                    if (s2 != null)
                        nsmutablearray.addObjectsFromArray(_statementsCommentedWithString(statementsToRenameColumnNamed(s2, s, s3, nsdictionary1), "statementsToRenameColumnNamed(" + s2 + ", " + s + ", " + s3 + ")"));
                    if (nsdictionary3.objectForKey("allowsNull") != null)
                        nsmutablearray.addObjectsFromArray(_statementsCommentedWithString(statementsToModifyColumnNullRule(s3, s, eoattribute.allowsNull(), nsdictionary1), "statementsToModifyColumnNullRule(" + s3 + ", " + s + ", " + (eoattribute.allowsNull() ? Boolean.TRUE : Boolean.FALSE) + ")"));
                }
            }
            int i = nsmutablearray.count();
            Object obj = _directCoercionsForEntityGroupInTable(nsarray, s, nsdictionary, nsdictionary1);
            if (obj != null)
                if (obj instanceof String)
                    nsmutablearray2.addObject(obj);
                else
                    nsmutablearray.addObjectsFromArray((NSArray) obj);
            NSArray nsarray2 = (NSArray) nsdictionary.objectForKey("deleted");
            if (nsarray2 != null) {
                nsmutablearray1.addObjectsFromArray(nsarray2);
                String s5 = _alterPhraseDeletingColumnsWithNames(nsarray2, nsarray, nsdictionary1);
                if (s5 != null) {
                    nsmutablearray2.addObject(s5);
                }
                else {
                    for (int k = 0 ; k < nsarray2.count() ; k++) {
                        String s4 = (String) nsarray2.objectAtIndex(k);
                        nsmutablearray.addObjectsFromArray(_statementsCommentedWithString(statementsToDeleteColumnNamed(s4, s, nsdictionary1), "statementsToDeleteColumnNamed:" + s4 + " inTableNamed:" + s));
                    }
                }
            }
            nsarray2 = (NSArray) nsdictionary.objectForKey("inserted");
            if (nsarray2 != null) {
                nsmutablearray1.addObjectsFromArray(nsarray2);
                for (int l = 0 ; l < nsarray2.count() ; l++) {
                    EOAttribute eoattribute1 = _firstAttributeInEntityGroupWithColumnName(nsarray, (String) nsarray2.objectAtIndex(l));
                    nsmutablearray.addObjectsFromArray(_statementsCommentedWithString(statementsToInsertColumnForAttribute(eoattribute1, nsdictionary1), "statementsToInsertColumnForAttribute:" + eoattribute1.entity().name() + "." + eoattribute1.name()));
                }
            }
            return nsmutablearray.count() <= 0 ? null : nsmutablearray;
        }

        
        public NSArray statementsToInsertColumnForAttribute(EOAttribute eoattribute, NSDictionary nsdictionary) {
            StringBuffer sql = new StringBuffer();
            
            sql.append("ALTER TABLE ");
            sql.append(quoteTableName(eoattribute.entity().externalName()));
            sql.append(" ADD COLUMN ");
            sql.append(addCreateClauseForAttribute(eoattribute));
            
            return new NSArray(_expressionForString(sql.toString()));
        }


        public NSArray statementsToConvertColumnType(String s, String s1, EOSchemaSynchronization.ColumnTypes columntypes, EOSchemaSynchronization.ColumnTypes columntypes1, NSDictionary nsdictionary) {
            StringBuffer sql = new StringBuffer();
            
            sql.append("ALTER COLUMN ");
            sql.append(quoteTableName(s1));
            sql.append(".\"");
            sql.append(s);
            sql.append("\" TO ");
            sql.append(statementToCreateDataTypeClause(columntypes1));
        
            return new NSArray(_expressionForString(sql.toString()));
        }
        
        
        public NSArray statementsToDeleteColumnNamed(String s, String s1, NSDictionary nsdictionary) {
            StringBuffer sql = new StringBuffer();
            
            sql.append("ALTER TABLE ");
            sql.append(quoteTableName(s1));
            sql.append(" DROP COLUMN \"");
            sql.append(s);
            sql.append("\" CASCADE");
            
            return new NSArray(_expressionForString(sql.toString()));
        }


        public NSArray statementsToRenameTableNamed(String s, String s1, NSDictionary nsdictionary) {
            StringBuffer sql = new StringBuffer();
            
            sql.append("ALTER TABLE NAME ");
            sql.append(quoteTableName(s));
            sql.append(" TO ");
            sql.append(quoteTableName(s1));

            return new NSArray(_expressionForString(sql.toString()));
        }


        public NSArray statementsToRenameColumnNamed(String s, String s1, String s2, NSDictionary nsdictionary) {
            StringBuffer sql = new StringBuffer();
            
            sql.append("ALTER COLUMN NAME ");
            sql.append(quoteTableName(s1));
            sql.append(".\"");
            sql.append(s);
            sql.append("\" TO \"");
            sql.append(s2);
            sql.append("\"");
            
            return new NSArray(_expressionForString(sql.toString()));
        }


        public NSArray statementsToDropForeignKeyConstraintsOnEntityGroups(NSArray nsarray, NSDictionary nsdictionary, NSDictionary nsdictionary1) {
            return EmptyArray;
        }


        public NSArray statementsToDropPrimaryKeyConstraintsOnEntityGroups(NSArray nsarray, NSDictionary nsdictionary, NSDictionary nsdictionary1) {
            return EmptyArray;
        }


        public boolean supportsDirectColumnCoercion() {
            return true;
        }


        public boolean supportsDirectColumnDeletion() {
            return true;
        }


        public boolean supportsDirectColumnInsertion() {
            return true;
        }


        public boolean supportsDirectColumnNullRuleModification() {
            return true;
        }


        public EOSchemaSynchronization.ColumnTypes _columnTypeForAttribute(EOAttribute eoattribute)
        {
            System.out.println("_columnTypeForAttribute: " + eoattribute);
            
            System.out.println("_columnTypeForAttribute: " + eoattribute.externalType());
            if (eoattribute.externalType().equals("BYTE"))
                return _columnTypeNamedWithPrecisionScaleAndWidth("BIT", eoattribute.precision(), eoattribute.scale(), eoattribute.width());
            return _columnTypeNamedWithPrecisionScaleAndWidth(eoattribute.externalType(), eoattribute.precision(), eoattribute.scale(), eoattribute.width()*8);
        }
        
        
        public boolean isColumnTypeEquivalentToColumnType(EOSchemaSynchronization.ColumnTypes columntypes, EOSchemaSynchronization.ColumnTypes columntypes1, NSDictionary nsdictionary) {
            System.out.println("eomodel type: " + columntypes1);
            System.out.println("database type: " + columntypes);
            System.out.println("dictionary: " + nsdictionary);

            boolean bool = columntypes != null && columntypes1 != null && internalTypeForExternal(columntypes.name()) == internalTypeForExternal(columntypes1.name()) && columntypes.precision() == columntypes1.precision() && columntypes.scale() == columntypes1.scale() && columntypes.width() == columntypes1.width();
            return bool;         
        }
        

        public String _alterPhraseInsertionClausePrefixAtIndex(int i) {
            return "ADD COLUMN";
        }


        public NSArray primaryKeySupportStatementsForEntityGroup(NSArray entityGroup) {
            if (entityGroup == null) 
                return EmptyArray;
                
            NSMutableArray result = new NSMutableArray();
            EOEntity eoentity = null;

            for (int i = entityGroup.count() - 1; i >= 0; i--) {
                eoentity = (EOEntity) entityGroup.objectAtIndex(i);
                String externalName = eoentity.externalName();
                
                if (externalName != null && externalName.length() > 0) {
                    result.addObject(_expressionForString("SET UNIQUE = 1000000 FOR " + quoteTableName(externalName)));
                }
            }
            return result;
        }


        public NSArray foreignKeyConstraintStatementsForRelationship(EORelationship relationship) {
            if (!relationship.isToMany() && isPrimaryKeyAttributes(relationship.destinationEntity(), relationship.destinationAttributes())) {
                StringBuffer sql = new StringBuffer();

                sql.append("ALTER TABLE ");
                sql.append(quoteTableName(relationship.entity().externalName().toUpperCase()));
                sql.append(" ADD FOREIGN KEY (");
                    
                NSArray attributes = relationship.sourceAttributes();
                    
                for (int i = 0 ; i < attributes.count() ; i++) {
                    if (i != 0) 
                        sql.append(", ");
                            
                    sql.append("\"");
                    sql.append(((EOAttribute) attributes.objectAtIndex(i)).columnName().toUpperCase());
                    sql.append("\"");
                }
                    
                sql.append(") REFERENCES ");
                sql.append(quoteTableName(relationship.destinationEntity().externalName().toUpperCase()));
                sql.append(" (");
                    
                attributes = relationship.destinationAttributes();
                    
                for (int i = 0 ; i < attributes.count() ; i++) {
                    if (i != 0) 
                        sql.append(", ");
                            
                    sql.append("\"");
                    sql.append(((EOAttribute) attributes.objectAtIndex(i)).columnName().toUpperCase());
                    sql.append("\"");
                }
                    
                sql.append(") DEFERRABLE INITIALLY DEFERRED");
                    
                return new NSArray(_expressionForString(sql.toString()));
            }
            return EmptyArray;
        }


        public NSArray createTableStatementsForEntityGroups(NSArray nsarray) {
            NSMutableArray nsmutablearray = new NSMutableArray();
            _retain(nsmutablearray);
            
            for (int i = 0 ; i < nsarray.count() ; i++) {
                nsmutablearray.addObjectsFromArray(createTableStatementsForEntityGroup((NSArray) nsarray.objectAtIndex(i)));
            }
            
            return nsmutablearray;
        }


	public NSArray createTableStatementsForEntityGroup(NSArray nsarray) {
            EOSQLExpression eosqlexpression = null;
            EOEntity eoentity = null;
            NSMutableArray nsmutablearray = new NSMutableArray();
            int j = nsarray != null ? nsarray.count() : 0;

            if (j == 0)
                return EmptyArray;

            StringBuffer columns = new StringBuffer();
            eosqlexpression = _expressionForEntity((EOEntity) nsarray.objectAtIndex(0));

            for (int i = 0 ; i < j ; i++) {
                eoentity = (EOEntity) nsarray.objectAtIndex(i);
                NSArray nsarray1 = eoentity.attributes();
                int l = nsarray1 != null ? nsarray1.count() : 0;
                
                for (int k = 0 ; k < l ; k++) {
                    EOAttribute eoattribute = (EOAttribute) nsarray1.objectAtIndex(k);
                    String column = eoattribute.columnName();
                    
                    if (!eoattribute.isDerived() && !eoattribute.isFlattened() && column != null && column.length() > 0 && nsmutablearray.indexOfObject(column) == NSArray.NotFound) {

                        if (columns.length() > 0) {
                            columns.append(',');
                            columns.append('\n');
                            columns.append('\t');
                        }
                            
                        columns.append(addCreateClauseForAttribute(eoattribute));
                        nsmutablearray.addObject(column);
                    }
                }
            }
            
            StringBuffer sql = new StringBuffer();
            sql.append("CREATE TABLE ");
            sql.append(quoteTableName(eoentity.externalName()));
            sql.append(" (\n\t");
            sql.append(columns.toString());
            sql.append("\n)");
            
            eosqlexpression.setStatement(sql.toString());
            
            NSArray result = new NSArray(eosqlexpression);
            _retain(result);
            return result;
	}


        public NSArray createIndexStatementsForEntityGroups(NSArray nsarray) {
            NSMutableArray nsmutablearray = new NSMutableArray();
            
            for (int i = 0 ; i < nsarray.count() ; i++) {
                nsmutablearray.addObjectsFromArray(createIndexStatementsForEntityGroup((NSArray) nsarray.objectAtIndex(i)));
            }
            
            return nsmutablearray;
        }


	public NSArray createIndexStatementsForEntityGroup(NSArray nsarray) {
            NSMutableArray result = new NSMutableArray();
            EOSQLExpression eosqlexpression = null;
            EOEntity eoentity = null;
            NSMutableArray nsmutablearray = new NSMutableArray();
            int j = nsarray != null ? nsarray.count() : 0;
            
            if(j == 0)
                return EmptyArray;
            
            eosqlexpression = _expressionForEntity((EOEntity) nsarray.objectAtIndex(0));
            
            for (int i = 0 ; i < j ; i++) {
                eoentity = (EOEntity) nsarray.objectAtIndex(i);
                NSDictionary dictionary = eoentity.userInfo();
                
                if (dictionary != null && dictionary.valueForKey("Index") != null) {
                    dictionary = (NSDictionary) dictionary.valueForKey("Index");
                    java.util.Enumeration e = dictionary.keyEnumerator();
                    
                    while (e.hasMoreElements()) {
                        eosqlexpression.setStatement((String) dictionary.objectForKey(e.nextElement()));
                        result.addObject(eosqlexpression);
                    }
                }
            }
            return result;
	}


        public int _nullCountForColumnNamedInTableNamedBeneathModel(String s, String s1, EOModel eomodel) {
            StringBuffer sql = new StringBuffer();
            
            sql.append("SELECT COUNT(*) FROM ");
            sql.append(quoteTableName(s1));
            sql.append(" where \"");
            sql.append(s);
            sql.append("\" is NULL");
            
            return _intForExpressionStringAdaptorChannel(sql.toString(), _schemaSynchronizationAdaptorChannelForModel(eomodel));
        }
        
        
        public StringBuffer addCreateClauseForAttribute(EOAttribute eoattribute) {
            StringBuffer sql = new StringBuffer();
            
            sql.append("\"");
            sql.append(eoattribute.columnName());
            sql.append("\" ");
            sql.append(columnTypeStringForAttribute(eoattribute));

            NSDictionary dictionary = eoattribute.userInfo();
            
            if (dictionary == null) {
                sql.append(eoattribute.allowsNull()? "" : " NOT NULL");
                return sql;
            }

            // Default values.
            if (dictionary.valueForKey("Default") != null) {
                sql.append(" DEFAULT ");
                sql.append(dictionary.valueForKey("Default"));
            }

            // Column constraints.
            if (!eoattribute.allowsNull()) {
                sql.append(" NOT NULL");
            }            
            if (dictionary.valueForKey("Unique") != null && dictionary.valueForKey("Unique").equals("true")) {
                sql.append(" UNIQUE");
            }
            if (dictionary.valueForKey("Check") != null) {
                sql.append(" CHECK ");
                sql.append(dictionary.valueForKey("Check"));
            }
            
            // Column collation.
            if (dictionary.valueForKey("Collate") != null) {
                sql.append(" COLLATE ");
                sql.append(dictionary.valueForKey("Collate"));
            }
            return sql;
        }


        public String columnTypeStringForAttribute(EOAttribute eoattribute) {
            String s = eoattribute.externalType();
            NSDictionary nsdictionary = JDBCAdaptor.typeInfoForModel(((EOEntity) eoattribute.parent()).model());
            NSDictionary nsdictionary1 = (NSDictionary) nsdictionary.objectForKey(s);
            
            if (nsdictionary1 == null)
                throw new JDBCAdaptorException("Unable to find type information for external type '" + s + "' in attribute '" + eoattribute.name() + "' of entity '" + ((EOEntity) eoattribute.parent()).name() + "'.  Check spelling and capitalization.", null);
            int i;
            try {
                i = Integer.parseInt((String) nsdictionary1.objectForKey("createParams"));
            }
            catch (NumberFormatException numberformatexception) {
                i = 0;
            }
            switch (i) {
                case 2:
                    int j = eoattribute.precision();
                    if (j == 0)
                        return eoattribute.externalType();
                    int k = eoattribute.scale();
                    if (k == 0)
                        return eoattribute.externalType() + "(" + j + ")";
                    else
                        return eoattribute.externalType() + "(" + j + "," + k + ")";
                case 1:
                    int l = eoattribute.width();
                    if (l == 0)
                        l = eoattribute.precision();
                    if (l == 0)
                        return eoattribute.externalType();
                    else
                        return eoattribute.externalType() + "(" + l + ")";
            }
            return eoattribute.externalType();
        }


        private String statementToCreateDataTypeClause(EOSchemaSynchronization.ColumnTypes columntypes) {
            switch (internalTypeForExternal(columntypes.name())) {
                case FB_Decimal: 
                case FB_Numeric:
                    int j = columntypes.precision();
                    if (j == 0)
                        return columntypes.name();
                    int k = columntypes.scale();
                    if (k == 0)
                        return columntypes.name() + "(" + j + ")";
                    else
                        return columntypes.name() + "(" + j + "," + k + ")";

                case FB_Float:
                case FB_Bit:
                case FB_VBit:
                case FB_Character:
                case FB_VCharacter:
                    int l = columntypes.width();
                    if (l == 0)
                        l = columntypes.precision();
                    if (l == 0)
                        return columntypes.name();
                    else
                        return columntypes.name() + "(" + l + ")";
                case FB_Timestamp:
                    int m = columntypes.precision();
                    if (m == 0)
                        return columntypes.name();
                    else
                        return columntypes.name() + "(" + m + ")";
            }
            return columntypes.name();
        }


        boolean isPrimaryKeyAttributes(EOEntity entity, NSArray attributes) {
            NSArray keys = entity.primaryKeyAttributeNames();
            boolean result = attributes.count() == keys.count();
            
            if (result) {
                for (int i = 0; i < keys.count() ; i++) {
                    if (!(result = keys.indexOfObject(((EOAttribute) attributes.objectAtIndex(i)).name()) != NSArray.NotFound)) 
                        break;
                }
            }
            return result;
        }


        public NSArray primaryKeyConstraintStatementsForEntityGroups(NSArray entityGroups) {
            NSMutableArray result = new NSMutableArray();
            
            for (int i = 0 ; i < entityGroups.count() ; i++) {
                result.addObjectsFromArray(primaryKeyConstraintStatementsForEntityGroup((NSArray) entityGroups.objectAtIndex(i)));
            }
            
            return result;
        }


        public NSArray primaryKeyConstraintStatementsForEntityGroup(NSArray entityGroup) {
            if (entityGroup.count() != 0) {
                EOEntity entity = (EOEntity) entityGroup.objectAtIndex(0);
                String tableName = entity.externalName().toUpperCase();
                NSArray keys = entity.primaryKeyAttributeNames();
                StringBuffer sql = new StringBuffer();
                
                if (tableName != null && keys.count() > 0) {
                    sql.append("ALTER TABLE ");
                    sql.append(quoteTableName(tableName));
                    sql.append(" ADD PRIMARY KEY (");
                    
                    for (int j = 0; j < keys.count(); j++) {
                        if (j != 0) 
                            sql.append(",");
                        
                        sql.append("\"");
                        sql.append(entity.attributeNamed((String) keys.objectAtIndex(j)).columnName().toUpperCase());
                        sql.append("\"");
                    }
                    sql.append(") NOT DEFERRABLE INITIALLY IMMEDIATE");
                    
                    return new NSArray(_expressionForString(sql.toString()));
                }
            }
            return EmptyArray;
        }
    }


    public static class FrontbaseExpression extends JDBCExpression {
        NSMutableArray _lobList;
        private static FrontbaseExpression _sharedInstance = null;
        private static FrontbaseSynchronizationFactory _sharedSyncFactory = null;


        public FrontbaseExpression(EOEntity eoentity) {
            super(eoentity);
            setUseQuotedExternalNames(true);
            _rtrimFunctionName = null;
            _externalQuoteChar = "\"";
        }


        public FrontbaseExpression() {
            setUseQuotedExternalNames(true);
            _rtrimFunctionName = null;
            _externalQuoteChar = "\"";
        }


        public FrontbaseExpression(boolean flag, int i) {
            super(flag, i);
            setUseQuotedExternalNames(true);
            _rtrimFunctionName = null;
            _externalQuoteChar = "\"";
        }


        public static EOSQLExpression sharedInstance() {
            if (_sharedInstance == null)
                _sharedInstance = new FrontbaseExpression();
            return _sharedInstance;
        }


        public static EOSynchronizationFactory sharedSyncFactory() {
            if (_sharedSyncFactory == null)
                _sharedSyncFactory = new FrontbaseSynchronizationFactory();
            return _sharedSyncFactory;
        }


        public Class _synchronizationFactoryClass() {
            return FrontbaseSynchronizationFactory.class;
        }


        public String lockClause() {
            return "";
        }


        public String assembleDeleteStatementWithQualifier(EOQualifier eoqualifier, String table, String s1) {
            if (table != null && table.indexOf('"') == -1)
                return super.assembleDeleteStatementWithQualifier(eoqualifier, quoteTableName(table), s1);
            else
                return super.assembleDeleteStatementWithQualifier(eoqualifier, table, s1);
        }


        public String assembleInsertStatementWithRow(NSDictionary nsdictionary, String table, String s1, String s2) {
            if (table != null && table.indexOf('"') == -1)
                return super.assembleInsertStatementWithRow(nsdictionary, quoteTableName(table), s1, s2);
            else
                return super.assembleInsertStatementWithRow(nsdictionary, table, s1, s2);
        }


        public String assembleUpdateStatementWithRow(NSDictionary dictionary, EOQualifier qualifier, String table, String s1, String s2) {
            if (table != null && table.indexOf('"') == -1)
                return super.assembleUpdateStatementWithRow(dictionary, qualifier, quoteTableName(table), s1, s2);
            else
                return super.assembleUpdateStatementWithRow(dictionary, qualifier, table, s1, s2);
        }


        public boolean shouldUseBindVariableForAttribute(EOAttribute eoattribute) {
            return false;
        }


        public boolean mustUseBindVariableForAttribute(EOAttribute eoattribute) {
            return false;
        }


        public String sqlStringForKeyValueQualifier(EOKeyValueQualifier eokeyvaluequalifier) {
            String s1 = eokeyvaluequalifier.key();
            String s = sqlStringForAttributeNamed(s1);
            
            if (s == null)
                throw new IllegalStateException("sqlStringForKeyValueQualifier: attempt to generate SQL for " + eokeyvaluequalifier.getClass().getName() + " " + eokeyvaluequalifier + " failed because attribute identified by key '" + s1 + "' was not reachable from from entity '" + entity().name() + "'");
            
            Object obj1 = eokeyvaluequalifier.value();
            
            if (obj1 instanceof EOQualifierVariable)
                throw new IllegalStateException("sqlStringForKeyValueQualifier: attempt to generate SQL for " + eokeyvaluequalifier.getClass().getName() + " " + eokeyvaluequalifier + " failed because the qualifier variable '$" + ((EOQualifierVariable) obj1).key() + "' is unbound.");
                
            EOAttribute eoattribute = JDBCPlugIn._attributeForPath(entity(), s1);
            String s7 = eoattribute.readFormat();
            
            if (s7 == null && eoattribute.adaptorValueType() == 1 && JDBCAdaptor._valueTypeCharForAttribute(eoattribute) == 'c')
                s7 = "TRIM(%V)";
                
            s = EOSQLExpression.formatSQLString(s, s7);
            NSSelector nsselector = eokeyvaluequalifier.selector();
            Object obj;
            
            if (nsselector.equals(EOQualifier.QualifierOperatorLike) || nsselector.equals(EOQualifier.QualifierOperatorCaseInsensitiveLike))
                obj = EOSQLExpression.sqlPatternFromShellPattern((String) obj1);
            else
                obj = obj1;
                
            String s6;
        
            if (nsselector.equals(EOQualifier.QualifierOperatorCaseInsensitiveLike)) {
                String s2 = sqlStringForValue(obj, s1);
                String s4 = sqlStringForSelector(nsselector, obj);
                s6 = sqlStringForCaseInsensitiveLike(s2, s);
            } 
            else {
                if (nsselector.equals(EOQualifier.QualifierOperatorEqual) && "".equals(obj))
                    obj = HackedUtils.NullValue;
                String s3 = sqlStringForValue(obj, s1);
                String s5 = sqlStringForSelector(nsselector, obj);
                s6 = s + " " + s5 + " " + s3;
            }
            return s6;
        }


        public String sqlStringForValue(Object obj, String s) {
            EOAttribute eoattribute = HackedUtils.attributeForPath(entity(), s);
            
            if (obj == EONullValue.nullValue())
                return "NULL";
                
            return hackedFormatValueForAttribute(obj, eoattribute);
        }


        public String sqlStringForCaseInsensitiveLike (String s, String s1) {
            StringBuffer sql = new StringBuffer();
            
            sql.append(s1);
            sql.append(" LIKE ");
            sql.append(s);
            sql.append(" COLLATE INFORMATION_SCHEMA.CASE_INSENSITIVE");
            
            return sql.toString();
       }


        public String hackedFormatValueForAttribute(Object obj, EOAttribute eoattribute) {
            if (obj != null) {
                switch (internalTypeForExternal(eoattribute.externalType())) {
                    case FB_Character:
                    case FB_VCharacter: {
                        String value = obj.toString();
                        
                        if (value.indexOf("'") == -1)
                            return "'" + value + "'";
                        else {
                            return "'" + addEscapeChars(value) + "'";
                        }
                    }
                    case FB_Integer: {
                        return ((Number) obj).intValue() + "";
                    }
                    default:
                        return obj.toString();
               }
            }
            return obj.toString();
        }


        String addEscapeChars(String value) {
            StringBuffer sb = new StringBuffer(value);
            int index = 0;

            for (int i = 0 ; i < value.length() ; i++, index++) {
                index = value.indexOf("'", index);
                
                if (index == -1)
                    break;
 
                sb.insert(index + i, "'");
            }
            return sb.toString();
        }
    }
}
