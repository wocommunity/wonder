package er.plugintest.tests;

import java.io.IOException;
import java.sql.SQLException;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import com.webobjects.eoaccess.EOAdaptorChannel;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.foundation.NSForwardException;

import er.extensions.eof.ERXEC;
import er.extensions.eof.ERXEOAccessUtilities;
import er.extensions.eof.ERXModelGroup;
import er.extensions.foundation.ERXFileUtilities;
import er.extensions.jdbc.ERXJDBCUtilities;
import er.testrunner.ERXTestCase;

public class PluginTest extends ERXTestCase {

	
	public PluginTest(String name) {
		super(name);

	}

	public static final Logger log = Logger.getLogger(PluginTest.class);
	protected EOModel model;
	protected String adaptorName = "DB2";

	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		model = ERXModelGroup.defaultGroup().modelNamed("PluginTest");
		
	}


	protected void resetData() {
		ERXEC ec = (ERXEC) ERXEC.newEditingContext();
		ERXEOAccessUtilities.ChannelAction action = new ERXEOAccessUtilities.ChannelAction() {
			@Override
			protected int doPerform(EOAdaptorChannel channel) {
				try {
					ERXJDBCUtilities.executeUpdateScript(channel, "update city set countryID = null;\n" +
							"update country set capitalID = null;\n" +
							"update countrylanguage set countryID = null;\n" +
							"delete from city;\n " +
							"delete from countrylanguage;\n" +
							"delete from country;");
					
					final String sql = ERXFileUtilities.stringFromInputStream(ERXFileUtilities.inputStreamForResourceNamed("world.sql", null, null));
					ERXJDBCUtilities.executeUpdateScript(channel, sql);
				}
				catch (SQLException e) {
					log.error(ExceptionUtils.getStackTrace(e), e);
					throw new NSForwardException(e);
				} catch (IOException e) {
					log.error(ExceptionUtils.getStackTrace(e), e);
					throw new NSForwardException(e);
				}
				return 0;
			}
		};
		action.perform(ec, model.name());

	}
}
