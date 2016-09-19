package er.profiling;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;

import er.profiling.delegates.PFHeatMap;
import er.profiling.delegates.PFMarkup;
import er.profiling.delegates.PFSummary;

public class PFProfiler {
    public static interface Delegate {
        public void requestStarted(WORequest request);

        public void requestEnded(WORequest request);

        public void responseEnded(WOResponse response, WOContext context);

        public void willAppendToResponse(WOElement element, WOResponse response, WOContext context);

        public void didAppendToResponse(WOElement element, WOResponse response, WOContext context);
    }

    private static ThreadLocal<PFStatsNode> _currentStats;

    private static Map<String, PFStatsNode> _stats;

    private static List<PFProfiler.Delegate> _delegates;

    private static long _statsID;

    static {
        _currentStats = new ThreadLocal<>();

        // ideally this should match your backtrack cache size, but i didn't
        // want to touch WOApplication too early
        _stats = new LRUMap<>(30);

        _delegates = new LinkedList<>();

        _statsID = 0;

        _delegates.add(new PFSummary());
        _delegates.add(new PFMarkup());
        _delegates.add(new PFHeatMap());
    }

    public static synchronized long nextStatsID() {
        return _statsID++;
    }

    public static void setCurrentStats(PFStatsNode stats) {
        _currentStats.set(stats);
    }

    public static PFStatsNode currentStats() {
        return _currentStats.get();
    }

    public static void startRequest(WORequest request) {
        PFProfiler._currentStats.set(new PFStatsNode("request", null, request, null));
        PFProfiler._currentStats.get().start();
        for (PFProfiler.Delegate delegate : _delegates) {
            delegate.requestStarted(request);
        }
    }

    public static void endRequest(WORequest request) {
        for (PFProfiler.Delegate delegate : _delegates) {
            delegate.requestEnded(request);
        }
    }

    public static void pushStats(String name, String type, Object target, Object context) {
        PFStatsNode currentStats = PFProfiler._currentStats.get();
        if (currentStats != null) {
            currentStats.push(name, type, target, context);
        }
    }

    public static PFStatsNode popStats() {
        PFStatsNode currentStats = PFProfiler._currentStats.get();
        if (currentStats != null) {
            currentStats.pop();
        }
        return currentStats;
    }

    public static void incrementCounter(String counterName) {
        PFStatsNode currentStats = PFProfiler._currentStats.get();
        if (currentStats != null) {
            currentStats.incrementCounter(counterName);
        }
    }

    public static void willAppendToResponse(WOElement element, WOResponse response, WOContext context) {
        for (PFProfiler.Delegate delegate : _delegates) {
            delegate.willAppendToResponse(element, response, context);
        }
    }

    public static void didAppendToResponse(WOElement element, WOResponse response, WOContext context) {
        PFStatsNode stats = PFProfiler._currentStats.get();
        if (stats != null) {
          stats.end();
        }

        for (PFProfiler.Delegate delegate : _delegates) {
            delegate.didAppendToResponse(element, response, context);
        }

        if (stats != null && stats.parentStats().isRoot()) {
            for (PFProfiler.Delegate delegate : _delegates) {
                delegate.responseEnded(response, context);
            }
        }
    }

    public static void setStatsWithID(PFStatsNode stats, String id) {
        synchronized (_stats) {
            _stats.put(id, stats);
        }
    }

    public static PFStatsNode statsWithID(String id) {
        synchronized (_stats) {
            return _stats.get(id);
        }
    }

    public static void registerRequestHandler() {
        WOApplication.application().registerRequestHandler(new PFProfilerRequestHandler(), "profiler");
    }

    protected static class LRUMap<U, V> extends LinkedHashMap<U, V> {
    	/**
    	 * Do I need to update serialVersionUID?
    	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
    	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
    	 */
    	private static final long serialVersionUID = 1L;

        private int _maxSize;

        public LRUMap(int maxSize) {
            super(16, 0.75f, true);
            _maxSize = maxSize;
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<U, V> eldest) {
            return size() > _maxSize;
        }
    }
}
