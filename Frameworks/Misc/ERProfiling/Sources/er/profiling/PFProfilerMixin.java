package er.profiling;

import javassist.gluonj.Around;
import javassist.gluonj.Before;
import javassist.gluonj.Glue;
import javassist.gluonj.Pcd;
import javassist.gluonj.Pointcut;

@Glue
public class PFProfilerMixin {
    @Around("{ er.profiling.PFProfiler.pushStats(`takeValuesFromRequest`, null, $0, $2); $_ = $proceed($$); er.profiling.PFProfiler.popStats(); }")
    Pointcut woElementTakeValuesFromRequest = Pcd
            .call("com.webobjects.appserver.WOElement+#takeValuesFromRequest(com.webobjects.appserver.WORequest, com.webobjects.appserver.WOContext)");

    @Around("{ er.profiling.PFProfiler.pushStats(`invokeAction`, null, $0, $2); $_ = $proceed($$); er.profiling.PFProfiler.popStats(); }")
    Pointcut woElementInvokeAction = Pcd.call("com.webobjects.appserver.WOElement+#invokeAction(com.webobjects.appserver.WORequest, com.webobjects.appserver.WOContext)");

    @Around("{ er.profiling.PFProfiler.pushStats(`appendToResponse`, null, $0, $2); er.profiling.PFProfiler.willAppendToResponse($0, $1, $2); $_ = $proceed($$); er.profiling.PFProfiler.didAppendToResponse($0, $1, $2); er.profiling.PFProfiler.popStats(); }")
    Pointcut woElementAppendToResponse = Pcd.call("com.webobjects.appserver.WOElement+#appendToResponse(com.webobjects.appserver.WOResponse, com.webobjects.appserver.WOContext)");

    @Around("{ er.profiling.PFProfiler.pushStats(`D2W`, null, $1, $0); $_ = $proceed($$); er.profiling.PFProfiler.popStats(); }")
    Pointcut d2wContextValueForKey = Pcd.call("com.webobjects.directtoweb.D2WContext+#valueForKey(java.lang.String)");

    @Around("{ er.profiling.PFProfiler.pushStats(`SQL`, `evaluate`, $1, null); $_ = $proceed($$); er.profiling.PFProfiler.popStats(); }")
    Pointcut evaluateSQLExpression = Pcd.call("com.webobjects.jdbcadaptor.JDBCChannel+#_evaluateExpression(com.webobjects.eoaccess.EOSQLExpression, boolean, boolean)");

    @Before("{ er.profiling.PFProfiler.incrementCounter(`rows`); }")
    Pointcut fetchObjectSQLExpression = Pcd.call("com.webobjects.eoaccess.EODatabaseChannel+#_fetchObject()");

    @Around("{ er.profiling.PFProfiler.pushStats(`SQL`, `select`, $1, null); $_ = $proceed($$); er.profiling.PFProfiler.popStats(); }")
    Pointcut selectSQLExpression = Pcd
            .call("com.webobjects.eoaccess.EODatabaseContext+#_objectsWithFetchSpecificationEditingContext(com.webobjects.eocontrol.EOFetchSpecification, com.webobjects.eocontrol.EOEditingContext)");

    @Around("{ er.profiling.PFProfiler.pushStats(`SQL`, `insert`, $1, null); $_ = $proceed($$); er.profiling.PFProfiler.popStats(); }")
    Pointcut insertSQLExpression = Pcd.call("com.webobjects.eoaccess.EODatabaseContext+#insertRow(com.webobjects.foundation.NSDictionary, com.webobjects.eoaccess.EOEntity)");

    @Around("{ er.profiling.PFProfiler.pushStats(`SQL`, `update`, $1, null); $_ = $proceed($$); er.profiling.PFProfiler.popStats(); }")
    Pointcut updateSQLExpression = Pcd
            .call("com.webobjects.eoaccess.EODatabaseContext+#updateValuesInRowsDescribedByQualifier(com.webobjects.foundation.NSDictionary, com.webobjects.eocontrol.EOQualifier, com.webobjects.eoaccess.EOEntity)");

    @Around("{ er.profiling.PFProfiler.pushStats(`SQL`, `delete`, $1, null); $_ = $proceed($$); er.profiling.PFProfiler.popStats(); }")
    Pointcut deleteSQLExpression = Pcd
            .call("com.webobjects.eoaccess.EODatabaseContext+#deleteRowsDescribedByQualifier(com.webobjects.eocontrol.EOQualifier, com.webobjects.eoaccess.EOEntity)");

    @Around("{ er.profiling.PFProfiler.startRequest($1); $_ = $proceed($$); er.profiling.PFProfiler.endRequest($1); }")
    Pointcut woApplicationDispatchRequest = Pcd.call("com.webobjects.appserver.WOApplication#dispatchRequest(com.webobjects.appserver.WORequest)");

    @Before("{ er.profiling.PFProfiler.registerRequestHandler(); }")
    Pointcut woApplicationConstructor = Pcd.call("com.webobjects.appserver.WOApplication#_runOnce()");
}
