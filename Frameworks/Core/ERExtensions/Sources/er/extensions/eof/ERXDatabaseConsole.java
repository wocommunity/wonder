package er.extensions.eof;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EOModelGroup;

import er.extensions.foundation.ERXUtilities;
import er.extensions.jdbc.ERXJDBCConnectionBroker;

public class ERXDatabaseConsole extends WOComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;


    public Connection con;
    public String sql;
    public String response;
    
    public ERXDatabaseConsole(WOContext context) {
        super(context);
    }

    public WOComponent executeQuery() {
        EOModel m = EOModelGroup.defaultGroup().models().objectAtIndex(0);
        con = ERXJDBCConnectionBroker.connectionBrokerForModel(m).getConnection();
        try {
            con.setAutoCommit(false);
            Statement s = con.createStatement();
            ResultSet rs = s.executeQuery(sql);
            con.commit();
            StringBuffer buf = new StringBuffer();
            
            // append header
            buf.append("<table border=\"1\"><tr>");
            ResultSetMetaData rsmd = rs.getMetaData();
            int colcount = rsmd.getColumnCount();
            for (int i = 1; i <= colcount; i++) {
                buf.append("<td>");
                buf.append(rsmd.getColumnName(i));
                buf.append("</td>");
            }
            buf.append("</tr>");

            while (rs.next()) {
                buf.append("<tr>");
                for (int i = 1; i <= colcount; i++) {
                    buf.append("<td>");
                    Object o = rs.getObject(i);
                    buf.append(o == null ? "" : o.toString());
                    buf.append("</td>");
                }
                buf.append("</tr>");
            }
            buf.append("</table>");
            response = buf.toString();
        } catch (SQLException e) {
            response = ERXUtilities.stackTrace(e);
        } finally {
            ERXJDBCConnectionBroker.connectionBrokerForModel(m).freeConnection(con);
        }
        return context().page();
    }
    
    public WOComponent executeUpdate() {
        EOModel m = EOModelGroup.defaultGroup().models().objectAtIndex(0);
        con = ERXJDBCConnectionBroker.connectionBrokerForModel(m).getConnection();
        try {
            Statement s = con.createStatement();
            int r = s.executeUpdate(sql);
            response = "result: "+r;
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            ERXJDBCConnectionBroker.connectionBrokerForModel(m).freeConnection(con);
        }
        return context().page();
    }

    
    @Override
    public void appendToResponse(WOResponse r, WOContext c) {
        if (session().objectForKey("ERXDatabaseConsole.enabled") != null) {
            super.appendToResponse(r, c);
        } else {
            r.appendContentString("please use the ERXDirectAction databaseConsoleAction to login first!");
        }
    }
    
}