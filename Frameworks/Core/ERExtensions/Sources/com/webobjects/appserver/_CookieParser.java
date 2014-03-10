package com.webobjects.appserver;

import java.util.Iterator;

import com.webobjects.appserver.WOApplication;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSLog;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;


final class _CookieParser {

    _CookieParser() {}

    private static int findNameDelimiter(final String aCookieString, final int pos, final int length) {
        int index = pos;
        do {
            if (index >= length) {
                break;
            }
            char c = aCookieString.charAt(index);
            if (c == ';' || c == ',' || c == '=') {
                break;
            }
            index++;
        } while (true);
        return index;
    }

    private static int findMatchingBrace(final String aCookieString, final int pos, final int length) {
        int index = pos;
        int openBraces = 1;
        do {
            if (index >= length) {
                break;
            }
            char c = aCookieString.charAt(index);
            if (c == '{') {
                openBraces++;
            } else {
                if (c == '}') {
                    openBraces--;
                    if (openBraces == 0) {
                        index++;
                        break;
                    }
                }
            }
            index++;
        } while (true);
        return index;
    }

    private static boolean isWhiteSpace(final char c) {
        return c == ' ' || c == '\t' || c == '\n' || c == '\r';
    }

    private static int skipWhiteSpace(final String s, final int pos, final int length) {
        int index = pos;
        while (index < length && isWhiteSpace(s.charAt(index))) {
            index++;
        }
        return index;
    }

    public static void _parse(final String aCookieString, final NSMutableDictionary aReturnDict) {
        int length = aCookieString.length();
        int pos = skipWhiteSpace(aCookieString, 0, length);
        while (pos < length) {
            int delimiter = findNameDelimiter(aCookieString, pos, length);
            String name = aCookieString.substring(pos, delimiter).trim();
            if (name.length() == 0) {
                throw new IllegalStateException((new StringBuilder()).append("Malformed cookie content:\"").append(
                        aCookieString).append("\"").toString());
            }
            pos = delimiter;
            String value = "";
            if (pos < length) {
                boolean equalSignSeen = false;
                if (aCookieString.charAt(pos) == '=') {
                    pos = skipWhiteSpace(aCookieString, pos + 1, length);
                    equalSignSeen = true;
                }
                if (pos >= length) {
                    NSLog.err.appendln((new StringBuilder()).append("CookieParser: Found a null cookie value in: '").append(
                            aCookieString).append("'. Will continue by setting value to the empty string.").toString());
                } else {
                    switch (aCookieString.charAt(pos)) {
                        case 123: // '{' - JSON
                            int jsonEnd = findMatchingBrace(aCookieString, pos + 1, length);
                            if (jsonEnd >= length) {
                                throw new IllegalStateException(
                                        (new StringBuilder()).append(
                                                "Malformed cookie content. No closing brace found at: \"").append(
                                                aCookieString).append("\"").toString());
                            }
                            value = aCookieString.substring(pos, jsonEnd);
                            pos = skipWhiteSpace(aCookieString, jsonEnd, length);
                            if (pos < length) {
                                if (aCookieString.charAt(pos) != ';' && aCookieString.charAt(pos) != ',') {
                                    throw new IllegalStateException(
                                            (new StringBuilder()).append("Expected a \";\" instead of \"").append(
                                                    aCookieString.substring(pos)).append("\"").toString());
                                }
                                pos++;
                            }
                            break;
                        case 34: // '"'
                            int closingQuote = aCookieString.indexOf('"', pos + 1);
                            if (closingQuote < 0) {
                                throw new IllegalStateException(
                                        (new StringBuilder()).append(
                                                "Malformed cookie content. No closing quote found at: \"").append(
                                                aCookieString).append("\"").toString());
                            }
                            value = aCookieString.substring(pos + 1, closingQuote);
                            pos = skipWhiteSpace(aCookieString, closingQuote + 1, length);
                            if (pos < length) {
                                if (aCookieString.charAt(pos) != ';' && aCookieString.charAt(pos) != ',') {
                                    throw new IllegalStateException(
                                            (new StringBuilder()).append("Expected a \";\" instead of \"").append(
                                                    aCookieString.substring(pos)).append("\"").toString());
                                }
                                pos++;
                            }
                            break;

                        case 44: // ','
                        case 59: // ';'
                            if (equalSignSeen) {
                                NSLog.err.appendln((new StringBuilder()).append(
                                        "CookieParser: Found a null cookie value in: '").append(aCookieString).append(
                                        "'. Will continue by setting value to the empty string.").toString());
                                value = "";
                            }
                            pos++;
                            break;

                        default:
                            int nextPair;
                            for (nextPair = pos; nextPair < length; nextPair++) {
                                char c = aCookieString.charAt(nextPair);
                                if (!isWhiteSpace(c) && c != ';' && c != ',' && c != '"') {
                                    continue;
                                }
                                if (c != ' ') {
                                    break;
                                }
                                NSLog.err.appendln((new StringBuilder()).append(
                                        "CookieParser: Found an unquoted space in: '").append(aCookieString).append(
                                        "'. Will continue by assuming the value was properly quoted.").toString());
                            }

                            if (nextPair == length) {
                                value = aCookieString.substring(pos).trim();
                                pos = nextPair;
                            } else {
                                value = aCookieString.substring(pos, nextPair).trim();
                                pos = skipWhiteSpace(aCookieString, nextPair, length);
                                if (pos < length && aCookieString.charAt(pos) != ';'
                                        && aCookieString.charAt(pos) != ',') {
                                    throw new IllegalStateException((new StringBuilder()).append(
                                            "CookieParser: Expected a \";\" or a \",\" instead of \"").append(
                                            aCookieString.substring(pos)).append("\"").toString());
                                }
                            }
                            pos++;
                            break;
                    }
                }
            }
            pos = skipWhiteSpace(aCookieString, pos, length);
            NSMutableArray previousValues = (NSMutableArray) aReturnDict.objectForKey(name);
            if (previousValues != null) {
                previousValues.addObject(value);
            } else {
                NSMutableArray newObjects = new NSMutableArray();
                newObjects.addObject(value);
                aReturnDict.setObjectForKey(newObjects, name);
            }
        }

        if (aReturnDict.count() == 0) {
            throw new IllegalStateException("CookieParser: empty cookie set not allowed by RFC 2109");
        }
    }

    public static NSDictionary parse(final String aCookieString) {
        if (aCookieString == null || aCookieString.length() == 0) {
            return NSDictionary.emptyDictionary();
        }
        NSMutableDictionary aParsedDict = new NSMutableDictionary();
        try {
            _parse(aCookieString, aParsedDict);
        } catch (RuntimeException ex) {
            aParsedDict = WOApplication.application().handleMalformedCookieString(ex, aCookieString, aParsedDict);
        }
        NSMutableDictionary aReturnDict = new NSMutableDictionary();
        String key;
        for (Iterator iterator = aParsedDict.allKeys().iterator(); iterator.hasNext(); aReturnDict.setObjectForKey(
                ((NSMutableArray) aParsedDict.objectForKey(key)).immutableClone(),
                key)) {
            key = (String) iterator.next();
        }
        return aReturnDict.immutableClone();
    }

}
