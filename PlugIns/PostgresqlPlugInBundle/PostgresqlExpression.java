/* 
    PostgresqlPluginBundle v1.2
    Copyright (C) 2001 Kenny Leung

    This bundle is free software; you can redistribute it and/or
    modify it under the terms of the GNU Lesser General Public
    License version 2.1 as published by the Free Software Foundation.
 
    This bundle is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
    Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public
    License along with this library; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/


// OS X
import com.apple.cocoa.foundation.*;

// Windows
// import com.apple.yellow.foundation.*;

import com.apple.yellow.eoaccess.*;


public class PostgresqlExpression extends JDBCExpression {

    // BEGIN_BRIDGE
    private static PostgresqlExpression _sharedInstance = null;
    private static PostgresqlSynchronizationFactory _sharedSyncFactory = null;

    public static EOSQLExpression sharedInstance() {
        if (_sharedInstance == null) {
            _sharedInstance = new PostgresqlExpression();
        }
        return _sharedInstance;
    }

    public static EOSynchronizationFactory sharedSyncFactory() {
        if (_sharedSyncFactory == null) {
            _sharedSyncFactory = new PostgresqlSynchronizationFactory();
        }
        return _sharedSyncFactory;
    }
    // END_BRIDGE

    public PostgresqlExpression(EOEntity entity) {
        super(entity);
    }

    // BEGIN_BRIDGE
    public PostgresqlExpression() {
        super();
    }
    // END_BRIDGE

    // BEGIN_BRIDGE
    public Class _synchronizationFactoryClass() {
        return PostgresqlSynchronizationFactory.class;
    }
    // END_BRIDGE
            
    public PostgresqlExpression(String sqlString) {
        super();
        setStatement(sqlString);
    }





    //public static boolean useQuotedExternalNames() { return true; }



    //public void addCreateClauseForAttribute(EOAttribute eoattribute) {
    //    eoattribute.setColumnName("\"" + eoattribute.columnName() + "\"");
    //    super.addCreateClauseForAttribute(eoattribute);
    //    eoattribute.setColumnName(eoattribute.columnName().substring(1, eoattribute.columnName().length() - 1));
    //}
}
