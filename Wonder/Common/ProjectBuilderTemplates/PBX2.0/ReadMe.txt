
Project WONDER PBX 2.0 Templates

This folder contains project templates for Project Builder 2.x Mac OS X (PBX 2.0). Those templates should work with both WebObjects 5.1 and 5.2 series. 

Note: If you see both Project Wonder and ProjectWonder folders here, please delete Project Wonder (with the space character between "Project" and "Wonder"). 

Installation
---------------------------------------------------------
If you use WebObjects 5.2, copy ProjectWonder folder under the following location: 
/Developer/ProjectBuilder Extras/Project Templates/

If you use WebObjects 5.1, copy ProjectWonder folder under one of the following locations: 
/Developer/ProjectBuilder Extras/Project Templates/
 - or -
~/Developer/ProjectBuilder Extras/Project Templates/  (under your home directory. You might have to create the directory by yourself.) 

When you create a new project, you'll see "ProjectWonder/ERXApplication" under the New Project list. 
 

Tips
---------------------------------------------------------
- Those project templates contain demo stuff. Just run your project and see what happens. 
- If you use WebObjects 5.1, add the following argument into the Launch Arguments field, so that your application can log the process ID via Log4j. (If you use WebObjects 5.2, you don't have to do this.)

   -com.webobjects.pid $$


Limitations
---------------------------------------------------------
- Those templates assume Project Wonder frameworks to be under /Library/Frameworks directory. Not the ones under ~/Roots directory. 
- WebObjects Project Setup Assistant is not supported with this version of templates. 


-------
September 29, 2002   The initial template was created by Katsuyoshi Ito
December 9, 2002   ERXApplication template was updated and imported by Tatsuya Kawano <tatsuyak@mac.com>


