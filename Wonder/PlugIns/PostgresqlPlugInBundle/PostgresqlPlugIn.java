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

public class PostgresqlPlugIn extends JDBCPlugIn {
    
    public PostgresqlPlugIn(JDBCAdaptor adaptor) {
        super(adaptor);
        System.out.println("Initializing PostgresqlPlugin");
    }

    public Class defaultExpressionClass() {
        return PostgresqlExpression.class;
    }

    public EOSynchronizationFactory createSynchronizationFactory() {
        return new PostgresqlSynchronizationFactory(_adaptor);
    }

    public String defaultDriverName() {
        return "org.postgresql.Driver";
    }

}
