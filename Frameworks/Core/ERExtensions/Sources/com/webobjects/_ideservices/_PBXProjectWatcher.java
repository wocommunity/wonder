/*jadclipse*/// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) lnc radix(10) lradix(10) 
// Source File Name:   _PBXProjectWatcher.java

package com.webobjects._ideservices;

import com.webobjects.foundation.*;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class _PBXProjectWatcher
{

            public _PBXProjectWatcher()
            {
            }

            public static NSArray openProjectsAppropriateForFile(String path)
            {
/*  40*/        StringBuilder buffer = new StringBuilder(4096);
/*  41*/        buffer.append("<openProjectsAppropriateForFile>");
/*  42*/        buffer.append((new StringBuilder()).append("<path>").append(path).append("</path>").toString());
/*  43*/        buffer.append("</openProjectsAppropriateForFile>");
/*  44*/        String result = _sendXMLToPB(buffer.toString());
                NSArray plist;
/*  47*/        if(result.length() > 0)
/*  48*/            plist = NSPropertyListSerialization.arrayForString(result);
/*  50*/        else
/*  50*/            plist = NSArray.emptyArray();
/*  51*/        return plist;
            }

            public static NSArray targetsInProjectContainingFile(String cookie, String path)
            {
/*  56*/        StringBuilder buffer = new StringBuilder(4096);
/*  57*/        buffer.append("<targetsInProjectContainingFile>");
/*  58*/        buffer.append((new StringBuilder()).append("<cookie>").append(cookie).append("</cookie>").toString());
/*  59*/        buffer.append((new StringBuilder()).append("<path>").append(path).append("</path>").toString());
/*  60*/        buffer.append("</targetsInProjectContainingFile>");
/*  61*/        String result = _sendXMLToPB(buffer.toString());
                NSArray plist;
/*  64*/        if(result.length() > 0)
/*  65*/            plist = NSPropertyListSerialization.arrayForString(result);
/*  67*/        else
/*  67*/            plist = NSArray.emptyArray();
/*  68*/        return plist;
            }

            public static NSArray targetsInProject(String cookie)
            {
/*  74*/        StringBuilder buffer = new StringBuilder(4096);
/*  75*/        buffer.append("<targetsInProject>");
/*  76*/        buffer.append((new StringBuilder()).append("<cookie>").append(cookie).append("</cookie>").toString());
/*  77*/        buffer.append("</targetsInProject>");
/*  78*/        String result = _sendXMLToPB(buffer.toString());
                NSArray plist;
/*  81*/        if(result.length() > 0)
/*  82*/            plist = NSPropertyListSerialization.arrayForString(result);
/*  84*/        else
/*  84*/            plist = NSArray.emptyArray();
/*  85*/        return plist;
            }

            public static String nameOfProject(String cookie)
            {
/*  90*/        StringBuilder buffer = new StringBuilder(4096);
/*  91*/        buffer.append("<nameOfProject>");
/*  92*/        buffer.append((new StringBuilder()).append("<projectCookie>").append(cookie).append("</projectCookie>").toString());
/*  93*/        buffer.append("</nameOfProject>");
/*  94*/        String result = _sendXMLToPB(buffer.toString());
/*  95*/        return result;
            }

            public static void addFilesToProjectNearFilePreferredInsertionGroupNameAddToTargetsCopyIntoGroupFolderCreateGroupsRecursively(NSArray paths, String cookie, String aFile, String aGroup, NSArray targetCookies, boolean createGroups, boolean recursively)
            {
/* 101*/        StringBuilder buffer = new StringBuilder(4096);
/* 102*/        buffer.append("<addFilesToProject>");
/* 103*/        buffer.append((new StringBuilder()).append("<addFiles>").append(_xmlStringArray(paths)).append("</addFiles>").toString());
/* 104*/        buffer.append((new StringBuilder()).append("<toProject>").append(cookie).append("</toProject>").toString());
/* 105*/        buffer.append((new StringBuilder()).append("<nearFile>").append(aFile).append("</nearFile>").toString());
/* 106*/        buffer.append((new StringBuilder()).append("<preferredInsertionGroupName>").append(aGroup).append("</preferredInsertionGroupName>").toString());
/* 107*/        buffer.append((new StringBuilder()).append("<addToTargets>").append(_xmlStringArray(targetCookies)).append("</addToTargets>").toString());
/* 108*/        buffer.append((new StringBuilder()).append("<copyIntoGroupFolder>").append(_xmlBoolean(createGroups)).append("</copyIntoGroupFolder>").toString());
/* 109*/        buffer.append((new StringBuilder()).append("<createGroupsRecursively>").append(_xmlBoolean(recursively)).append("</createGroupsRecursively>").toString());
/* 110*/        buffer.append("</addFilesToProject>");
/* 111*/        _sendXMLToPB(buffer.toString());
            }

            public static NSArray filesOfTypesInTargetOfProject(NSArray typesArray, String target, String cookie)
            {
/* 117*/        StringBuilder buffer = new StringBuilder(4096);
/* 118*/        buffer.append("<filesOfTypesInTargetOfProject>");
/* 119*/        buffer.append((new StringBuilder()).append("<cookie>").append(cookie).append("</cookie>").toString());
/* 120*/        buffer.append((new StringBuilder()).append("<target>").append(target).append("</target>").toString());
/* 121*/        buffer.append((new StringBuilder()).append("<typesArray>").append(_xmlStringArray(typesArray)).append("</typesArray>").toString());
/* 122*/        buffer.append("</filesOfTypesInTargetOfProject>");
/* 123*/        String result = _sendXMLToPB(buffer.toString());
                NSArray plist;
/* 126*/        if(result.length() > 0)
/* 127*/            plist = NSPropertyListSerialization.arrayForString(result);
/* 129*/        else
/* 129*/            plist = NSArray.emptyArray();
/* 132*/        return plist;
            }

            public static String nameOfTargetInProject(String target, String project)
            {
/* 137*/        StringBuilder buffer = new StringBuilder(4096);
/* 138*/        buffer.append("<nameOfTarget>");
/* 139*/        buffer.append((new StringBuilder()).append("<targetCookie>").append(target).append("</targetCookie >").toString());
/* 140*/        buffer.append((new StringBuilder()).append("<projectCookie>").append(project).append("</projectCookie >").toString());
/* 141*/        buffer.append("</nameOfTarget>");
/* 142*/        String result = _sendXMLToPB(buffer.toString());
/* 143*/        return result;
            }

            public static void openFile(String filename, int line, String errorMessage)
            {
/* 147*/        StringBuilder buffer = new StringBuilder(4096);
/* 157*/        buffer.append("<OpenFile><filename>");
/* 158*/        buffer.append(filename);
/* 159*/        buffer.append("</filename><linenumber>");
/* 160*/        buffer.append(line);
/* 161*/        buffer.append("</linenumber><message>");
/* 163*/        buffer.append(errorMessage);
/* 164*/        buffer.append("</message></OpenFile>");
/* 166*/        _sendXMLToPB(buffer.toString());
            }

            public static void addGroup(String name, String path, String projectCookie, String nearFile)
            {
/* 170*/        StringBuilder buffer = new StringBuilder(4096);
/* 171*/        buffer.append("<addGroup>");
/* 172*/        buffer.append((new StringBuilder()).append("<name>").append(name).append("</name >").toString());
/* 173*/        if(path != null)
/* 174*/            buffer.append((new StringBuilder()).append("<path>").append(path).append("</path >").toString());
/* 175*/        buffer.append((new StringBuilder()).append("<projectCookie>").append(projectCookie).append("</projectCookie >").toString());
/* 176*/        if(nearFile != null)
/* 177*/            buffer.append((new StringBuilder()).append("<nearFile>").append(nearFile).append("</nearFile >").toString());
/* 178*/        buffer.append("</addGroup>");
/* 179*/        _sendXMLToPB(buffer.toString());
            }

            public static void addGroupToPreferredInsertionGroup(String name, String path, String projectCookie, String nearFile, String preferredInsertionGroup)
            {
/* 183*/        StringBuilder buffer = new StringBuilder(4096);
/* 184*/        buffer.append("<addGroupToPreferredInsertionGroup>");
/* 185*/        buffer.append((new StringBuilder()).append("<name>").append(name).append("</name >").toString());
/* 186*/        if(path != null)
/* 187*/            buffer.append((new StringBuilder()).append("<path>").append(path).append("</path >").toString());
/* 188*/        buffer.append((new StringBuilder()).append("<projectCookie>").append(projectCookie).append("</projectCookie >").toString());
/* 189*/        if(nearFile != null)
/* 190*/            buffer.append((new StringBuilder()).append("<nearFile>").append(nearFile).append("</nearFile >").toString());
/* 191*/        if(preferredInsertionGroup != null)
/* 192*/            buffer.append((new StringBuilder()).append("<preferredInsertionGroup>").append(preferredInsertionGroup).append("</preferredInsertionGroup >").toString());
/* 193*/        buffer.append("</addGroupToPreferredInsertionGroup>");
/* 194*/        _sendXMLToPB(buffer.toString());
            }

            private static String _xmlStringArray(NSArray array)
            {
/* 199*/        StringBuilder buffer = new StringBuilder(4096);
/* 200*/        buffer.append("<array>");
/* 201*/        int i = 0;
/* 201*/        for(int c = array.count(); i < c; i++)
                {
/* 202*/            String str = (String)array.objectAtIndex(i);
/* 203*/            buffer.append((new StringBuilder()).append("<string>").append(str).append("</string>").toString());
                }

/* 205*/        buffer.append("</array>");
/* 206*/        return buffer.toString();
            }

            private static String _xmlBoolean(boolean value)
            {
/* 210*/        if(value)
/* 211*/            return "YES";
/* 212*/        else
/* 212*/            return "NO";
            }

            private static String _sendXMLToPB(String command)
            {
/* 217*/        String result = "";
/* 219*/        if(_communicationDisabled)
/* 220*/            return "";
/* 226*/        try
                {
/* 226*/            Socket pbSocket = new Socket(_PBHostname, _PBPort);
/* 228*/            OutputStream os = pbSocket.getOutputStream();
/* 229*/            os.write(command.getBytes());
/* 230*/            os.flush();
/* 236*/            try
                    {
/* 236*/                int buffLen = 7000;
/* 237*/                byte buffer[] = new byte[buffLen];
/* 241*/                InputStream inputSt = pbSocket.getInputStream();
/* 244*/                int i = 0;
                        int maxI;
/* 244*/                for(maxI = 50; inputSt.available() == 0 && i < maxI; i++)
/* 246*/                    Thread.sleep(100L);

/* 249*/                if(i == maxI)
                        {
/* 250*/                    _communicationDisabled = true;
/* 251*/                    NSLog.err.appendln((new StringBuilder()).append("Error - Couldn't contact Xcode to send XML command ").append(command).toString());
                        }
/* 253*/                while(inputSt.available() > 0) 
                        {
/* 254*/                    int length = inputSt.read(buffer, 0, buffLen >= inputSt.available() ? inputSt.available() : buffLen);
/* 256*/                    result = (new StringBuilder()).append(result).append(new String(buffer, 0, length)).toString();
                        }
                    }
/* 259*/            catch(Exception e)
                    {
/* 260*/                _communicationDisabled = true;
/* 261*/                NSLog.err.appendln((new StringBuilder()).append(" Error - exception raised when sending xml command to Xcode. XML: ").append(command).append(" EXCEPTION: ").append(e).toString());
/* 262*/                NSLog.err.appendln(e);
/* 263*/                result = "";
                    }
/* 265*/            pbSocket.close();
                }
/* 267*/        catch(Exception e)
                {
/* 268*/            _communicationDisabled = true;
/* 269*/            if(NSLog.debugLoggingAllowedForLevelAndGroups(2, 4L) && System.getProperty("os.name").startsWith("Mac"))
                    {
/* 271*/                if(_printRapidTurnaroundMessage)
                        {
/* 272*/                    _printRapidTurnaroundMessage = false;
/* 273*/                    NSLog.err.appendln("Cannot use rapid turnaround.  Please start Xcode and open the project for this application.");
                        }
/* 275*/                NSLog._conditionallyLogPrivateException(e);
                    }
/* 278*/            result = "";
                }
/* 281*/        return result;
            }

            private static int _PBPort;
            private static String _PBHostname = NSProperties.getProperty("ProjectBuilderHost", "localhost");
            private static volatile boolean _printRapidTurnaroundMessage = true;
            private static boolean _communicationDisabled = false;

            static 
            {
/*  24*/        String value = NSProperties.getProperty("ProjectBuilderPort", "8547");
/*  26*/        try
                {
/*  26*/            _PBPort = Integer.parseInt(value);
                }
/*  27*/        catch(NumberFormatException e)
                {
/*  29*/            if(NSLog.debugLoggingAllowedForLevel(1))
/*  30*/                NSLog.err.appendln((new StringBuilder()).append("_PBXProjectWatcher: exception while reading property 'ProjectBuilderPort'. The value '").append(value).append("' is not an integer. Using port 8547 by default.").toString());
/*  32*/            _PBPort = 8546;
                }
            }
}


/*
	DECOMPILATION REPORT

	Decompiled from: /Volumes/Home/Wonder/Frameworks/Core/ERJars/Libraries/WORapidTurnaround.jar
	Total time: 117 ms
	Jad reported messages/errors:
	Exit status: 0
	Caught exceptions:
*/