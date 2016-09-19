package er.profiling;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class PFStatsNode {
    private long _id;

    private PFStatsNode _parent;

    private String _name;

    private String _type;

    private long _startTime;

    private long _endTime;

    private List<PFStatsNode> _children;

    private Object _target;

    private Object _context;

    private Map<String, Long> _counters;

    private List<String> _errors;

    public PFStatsNode(String name, String type, Object target, Object context) {
        _name = name;
        _type = type;
        _target = target;
        _context = context;
        _id = PFProfiler.nextStatsID();
    }

    public void clearErrors() {
        _errors = null;
        if (_children != null) {
            for (PFStatsNode child : _children) {
                child.clearErrors();
            }
        }
    }

    public void addError(String error) {
        if (_errors == null) {
            _errors = new LinkedList<>();
        }
        _errors.add(error);
    }

    public boolean hasErrors() {
        return _errors != null && _errors.size() > 0;
    }

    public List<String> errors() {
        return _errors;
    }

    public String type() {
        return _type;
    }

    public PFStatsNode parentStats() {
        return _parent;
    }

    public long id() {
        return _id;
    }

    public String name() {
        return _name;
    }

    public Object target() {
        return _target;
    }

    public Object context() {
        return _context;
    }

    public int depth() {
        return _parent == null ? 0 : (_parent.depth() + 1);
    }

    public PFStatsNode rootStats() {
        return _parent == null ? this : _parent.rootStats();
    }

    public boolean isLeaf() {
        return _children == null || _children.size() == 0;
    }

    public boolean isRoot() {
        return rootStats() == this;
    }

    public void start() {
        _startTime = System.nanoTime();
        // for (int i = depth(); i > 0; i --) {
        // System.out.print("  ");
        // }
        // System.out.println(_name + ":start");
    }

    public void end() {
        _endTime = System.nanoTime();
        if (_parent != null) {
            _parent.end();
        }
        // for (int i = depth(); i > 0; i --) {
        // System.out.print("  ");
        // }
        // System.out.println(_name + ":done (" + duration() + "ms)");
    }

    public long overhead() {
        long overhead = duration();
        if (_children != null) {
            long childrenDuration = 0;
            for (PFStatsNode child : _children) {
                childrenDuration += child.duration();
            }
            overhead -= childrenDuration;
        }
        return overhead;
    }

    public long duration() {
        return _endTime - _startTime;
    }

    public double durationMillis() {
        return duration() / 1000000.0;
    }

    public long durationOf(String name, boolean recursiveSum) {
        long duration = 0;
        if (name.equals(_name)) {
            duration += duration();
        }
        if (_children != null && (recursiveSum || duration == 0)) {
            for (PFStatsNode child : _children) {
                duration += child.durationOf(name, recursiveSum);
            }
        }
        return duration;
    }

    public double durationOfMillis(String name, boolean recursiveSum) {
        return durationOf(name, recursiveSum) / 1000000.0;
    }

    public int countOf(String name, boolean recursiveCount) {
        int count = 0;
        if (name.equals(_name)) {
            count++;
        }
        if (_children != null && (recursiveCount || count == 0)) {
            for (PFStatsNode child : _children) {
                count += child.countOf(name, recursiveCount);
            }
        }
        return count;
    }

    public DurationCount countBetweenDurations(long minNanos, long maxNanos) {
        DurationCount durationCount = new DurationCount();
        countBetweenDurations(minNanos, maxNanos, durationCount);
        return durationCount;
    }

    protected void countBetweenDurations(long minNanos, long maxNanos, DurationCount durationCount) {
        long duration = overhead();
        if (duration >= minNanos && duration < maxNanos) {
            durationCount.count++;
            durationCount.duration += duration;
        }

        if (_children != null) {
            for (PFStatsNode child : _children) {
                child.countBetweenDurations(minNanos, maxNanos, durationCount);
            }
        }
    }

    public List<PFStatsNode> children() {
        return _children;
    }

    public PFStatsNode push(String name, String type, Object target, Object context) {
        if (_children == null) {
            _children = new LinkedList<>();
        }
        PFStatsNode stats = new PFStatsNode(name, type, target, context);
        stats._parent = this;
        _children.add(stats);
        PFProfiler.setCurrentStats(stats);
        stats.start();
        return stats;
    }

    public void pop() {
        end();
        if (_parent != null) {
            PFProfiler.setCurrentStats(_parent);
        }
    }

    public double percentage() {
        return (double) duration() / (double) rootStats().duration();
    }

    public boolean isAtLeastPercentage(double minimumPercentage) {
        return minimumPercentage == 0.0 || (percentage() >= minimumPercentage);
    }

    public boolean isImportant() {
        return children() == null || parentStats() == null 
            || (!isAtLeastPercentage(parentStats().percentage() * 0.9));
    }

    public String cssID() {
        return "wo_p_" + id();
    }

    public void incrementCounter(String counterName) {
        synchronized (this) {
            if (_counters == null) {
                _counters = new TreeMap<>();
            }
            Long counter = _counters.get(counterName);
            if (counter == null) {
                _counters.put(counterName, Long.valueOf(1));
            } else {
                _counters.put(counterName, Long.valueOf(counter.longValue() + 1));
            }
        }
    }

    public Map<String, Long> counters() {
        return _counters;
    }

    public static class DurationCount {
        long duration;

        int count;

        public long nanos() {
            return duration;
        }

        public int count() {
            return count;
        }

        public double millis() {
            return (double) duration / 1000000.0f;
        }
    }
}