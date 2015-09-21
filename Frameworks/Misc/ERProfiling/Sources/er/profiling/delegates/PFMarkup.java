package er.profiling.delegates;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOMessage;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;

import er.profiling.PFProfiler;
import er.profiling.PFStatsNode;

public class PFMarkup implements PFProfiler.Delegate {
    private static Pattern _tagPattern = Pattern.compile("<[a-zA-Z]+[ />]?", Pattern.MULTILINE);

    private static final String _marker = "~~PROFILER_MARKER~~";

    private boolean _markupEnabled;

    private ThreadLocal<List<MarkerStats>> _markerList = new ThreadLocal<List<MarkerStats>>();

    private Field _contentField;

    public void requestStarted(WORequest request) {
        // just trigger markup enablement on heatEnabled right now
        _markupEnabled = PFHeatMap.isHeatEnabled();
        _markerList.set(new LinkedList<MarkerStats>());
        try {
            _contentField = WOMessage.class.getDeclaredField("_content");
            _contentField.setAccessible(true);
        } catch (Throwable t) {
            throw new RuntimeException("Unable to load the _content field of WOMessage.", t);
        }
    }

    public void requestEnded(WORequest request) {
        // DO NOTHING
    }

    public void responseEnded(WOResponse response, WOContext context) {
        // DO NOTHING
    }

    protected static void append(Object content, String str) {
        if (content instanceof StringBuffer) {
            ((StringBuffer) content).append(str);
        } else if (content instanceof StringBuilder) {
            ((StringBuilder) content).append(str);
        } else {
            throw new IllegalArgumentException("Don't know how to append to '" + content + "'.");
        }
    }

    protected static void insert(Object content, int index, String str) {
        if (content instanceof StringBuffer) {
            ((StringBuffer) content).insert(index, str);
        } else if (content instanceof StringBuilder) {
            ((StringBuilder) content).insert(index, str);
        } else {
            throw new IllegalArgumentException("Don't know how to insert into '" + content + "'.");
        }
    }

    protected static void replace(Object content, int start, int end, String str) {
        if (content instanceof StringBuffer) {
            ((StringBuffer) content).replace(start, end, str);
        } else if (content instanceof StringBuilder) {
            ((StringBuilder) content).replace(start, end, str);
        } else {
            throw new IllegalArgumentException("Don't know how to replace strings in '" + content + "'.");
        }
    }

    protected static int indexOf(Object content, String str, int fromIndex) {
        if (content instanceof StringBuffer) {
            return ((StringBuffer) content).indexOf(str, fromIndex);
        } else if (content instanceof StringBuilder) {
            return ((StringBuilder) content).indexOf(str, fromIndex);
        } else {
            throw new IllegalArgumentException("Don't know how to insert into '" + content + "'.");
        }
    }

    protected static boolean regionMatches(CharSequence str, int toffset, String other, int ooffset, int len) {
        int to = toffset;
        int po = ooffset;
        // Note: toffset, ooffset, or len might be near -1>>>1.
        int count = str.length();
        int otherCount = other.length();
        if ((ooffset < 0) || (toffset < 0) || (toffset > (long) count - len) || (ooffset > (long) otherCount - len)) {
            return false;
        }
        while (len-- > 0) {
            if (str.charAt(to++) != other.charAt(po++)) {
                return false;
            }
        }
        return true;
    }

    public void willAppendToResponse(WOElement element, WOResponse response, WOContext context) {
        if (_markupEnabled) {
            try {
                MarkerStats ms = new MarkerStats();
                ms._stats = PFProfiler.currentStats();
                Object content = _contentField.get(response);
                if (content != null) {
                    ms._index = ((CharSequence) content).length();
                    append(content, PFMarkup._marker);
                }
                _markerList.get().add(ms);
            } catch (Throwable t) {
                throw new RuntimeException("Failed to get the length of the response.", t);
            }
        }
    }

    public void didAppendToResponse(WOElement element, WOResponse response, WOContext context) {
        if (_markupEnabled) {
            try {
                CharSequence contentStr = (CharSequence) _contentField.get(response);
                if (contentStr != null) {
                    List<MarkerStats> markerList = _markerList.get();
                    MarkerStats markerStats = markerList.remove(markerList.size() - 1);
                    int startIndex = markerStats._index;
                    int markerIndex = indexOf(contentStr, PFMarkup._marker, startIndex);
                    if (markerIndex != -1) {
                        replace(contentStr, markerIndex, markerIndex + PFMarkup._marker.length(), "");
                        startIndex = markerIndex;

                        int tagIndex = -1;
                        int endIndex = contentStr.length();
                        for (int i = startIndex; i < endIndex; i++) {
                            char ch = contentStr.charAt(i);
                            if (ch == '<') {
                                if (i < endIndex - 1 && contentStr.charAt(i + 1) != '/' && contentStr.charAt(i + 1) != '!') {
                                    tagIndex = i;
                                    break;
                                }
                            }
                        }

                        if (tagIndex != -1) {
                            String profilerIDAttributeName = "class";
                            String profilerIDAttributeStart = " " + profilerIDAttributeName + "=\"";
                            boolean foundClassAttribute = false;
                            int closeOffset = StringUtils.indexOf(contentStr, '>', tagIndex);
                            int attributeOffset = StringUtils.indexOf(contentStr, profilerIDAttributeName, tagIndex);
                            if (attributeOffset == -1 || attributeOffset > closeOffset) {
                                char ch = contentStr.charAt(closeOffset - 1);
                                if (ch == '/') {
                                    attributeOffset = closeOffset - 1;
                                } else {
                                    attributeOffset = closeOffset;
                                }
                            } else {
                                attributeOffset += profilerIDAttributeStart.length() -1;
                                foundClassAttribute = true;
                            }
                            String profilerID = markerStats._stats.cssID();
                            if (foundClassAttribute) {
                                insert(contentStr, attributeOffset, profilerID + " ");
                            } else {
                                insert(contentStr, attributeOffset, profilerIDAttributeStart + profilerID + "\"");
                            }
                        }
                    }
                }
            } catch (Throwable t) {
                throw new RuntimeException("Failed to replace markers in the response.", t);
            }
        }
    }

    protected static class MarkerStats {
        public PFStatsNode _stats;

        public int _index;
    }
}
