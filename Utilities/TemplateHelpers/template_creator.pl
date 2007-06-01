#!/usr/bin/perl
# Template creator tool
use Getopt::Long;

my %options;
my ($dest, $prefix, $postfix, $package, $neutral);

$ok = GetOptions(\%options, "package=s", "prefix=s", "postfix=s",  "output=s", "neutral", "repetitions!");
($dest, $prefix, $postfix, $package, $neutral) = ($options{'output'}, $options{'prefix'}, $options{'postfix'}, $options{'package'}, $options{'neutral'});
$ok = $ok && defined($dest) && defined($dest) && defined($prefix) && defined($postfix) && defined($package);  
if(!$ok) {
    die(q(
 Use this tool to easily create your own look from the ERDirectToWebJava
 templates
 It creates subclasses for every class whose name ends with "Template"
 and pre- and postfixes those with supplied parameters:
   ERD2WInspectPage -> CMSInspectPagePlain
 it will also create a d2wmodel that contains rules like
   *true* -> templateNameForInspectPage = "CMSInspectPagePlain" (20)
   
 Usage: template_creator.pl
        -package com.foo   package name for the templates
        -prefix  BAS       prefix for the templates
        -postfix AdminPage postfix for the templates
        -output  BASLook   destination directory
        -neutral           use ERNeutralLook instead of ERD2W
    ) . "\n");
}

if($neutral) {
    # you may need to adjust this path
    # ERNeutralLook has "standard" pages
    $pathToERDirectToWeb = "~/Roots/ERNeutralLook.framework/Resources/";
    die("Can't find $pathToERDirectToWeb") if(!-e $pathToERDirectToWeb);
    $pattern = 'ERNEU(.*?)Page';
    $Nonlocalized = "";
    $Suffix = "Page.wo";
} else {
    # ERD2W has more experimental but advanced pages
    $pathToERDirectToWeb = "~/Roots/ERDirectToWeb.framework/Resources/";
#    die("Can't find $pathToERDirectToWeb") if(!-e $pathToERDirectToWeb);
    $pattern = 'ERD2W(.*?)PageTemplate';
    $Nonlocalized = "";
    $Suffix = "Template.wo";
}

#$dest = "tmp";
$packageDir = $package;
$packageDir =~ s|\.|/|g;

$cmd = "find $pathToERDirectToWeb -name \\*$Suffix -type d |grep -v CVS";
#warn $cmd;
@files = `$cmd`;

foreach $file (@files) {
    if($file =~ m|(.*?)/([A-Za-z0-9_]+).wo|) {
        ($source, $class) = ($1, $2);
        if($class =~ m|$pattern|) {
            $typeName = "$1";
            $newClass = "$prefix$typeName$postfix";
            warn "creating $newClass\n";
            system "mkdir -p $dest/Sources/$packageDir" if (!-e "$dest/Sources/$packageDir/");
            open OUT, ">$dest/Sources/$packageDir/$newClass.java" || die("Can't open $dest/Sources/$packageDir/$newClass.java");
            print OUT "package $package;\n";
            print OUT "import com.webobjects.appserver.*;\n";
            print OUT "import com.webobjects.directtoweb.*;\n";
            print OUT "import er.extensions.*;\n";
            print OUT "import er.directtoweb.*;\n";
            print OUT "public class $newClass extends $class {\n";
            print OUT "\tpublic $newClass(WOContext wocontext) {\n";
            print OUT "\t\tsuper(wocontext);\n";
            print OUT "\t\}\n\n}\n";
            close OUT;
            system "mkdir -p $dest/Components/Nonlocalized.lproj" if(!-e "$dest/Components/Nonlocalized.lproj");
            system "rm -rf $dest/Components/Nonlocalized.lproj/$newClass.wo" if(-e "$dest/Components/Nonlocalized.lproj/$newClass.wo");
            system "cp -r $source/$Nonlocalized$class.wo $dest/Components/Nonlocalized.lproj/$newClass.wo";
            system "cp -r $source/$class.api $dest/Components/$newClass.api";
            system "rm -rf $dest/Components/Nonlocalized.lproj/$newClass.wo/CVS";
            
            # print "~/Roots/PBXTool $dest/IDE/*.pbproj/project.pbxproj $newClass $newClass.java $newClass.api $newClass.wo\n";
            $classes .= " $newClass ";
            
            system "mv $dest/Components/Nonlocalized.lproj/$newClass.wo/$class.html $dest/Components/Nonlocalized.lproj/$newClass.wo/$newClass.html";
            system "mv $dest/Components/Nonlocalized.lproj/$newClass.wo/$class.wod $dest/Components/Nonlocalized.lproj/$newClass.wo/$newClass.wod";
            system "mv $dest/Components/Nonlocalized.lproj/$newClass.wo/$class.woo $dest/Components/Nonlocalized.lproj/$newClass.wo/$newClass.woo";
            $rules .= "
{
    author = 20; 
    class = com.webobjects.directtoweb.Rule; 
    rhs = {
        class = com.webobjects.directtoweb.Assignment; 
        keyPath = templateNameFor$typeName" . "Page; 
        value = $newClass; 
    }; 
},";            
        }
    }
}
chop($rules);
system "mkdir -p $dest/Resources/";

            # print "~/Roots/PBXTool $dest/*.pbproj/project.pbxproj 'Components' $classes\n";
open D2W, ">$dest/Resources/d2w.d2wmodel" || die("Can't open $dest/Resources/d2w.d2wmodel");
print D2W "{ rules = ( $rules );}";
close D2W;

open D2W, ">$dest/.classpath" || die("Can't open $dest/.classpath");
print D2W q(<?xml version="1.0" encoding="UTF-8"?>
<classpath>
        <classpathentry kind="src" path="Sources"/>
        <classpathentry kind="con" path="org.objectstyle.wolips.WO_CLASSPATH/ERJars/ERExtensions/ERDirectToWeb/JavaDirectToWeb/JavaDTWGeneration/JavaEOAccess/JavaEOControl/JavaFoundation/JavaWebObjects/JavaXML/JavaWOExtensions"/>
        <classpathentry kind="con" path="org.eclipse.jdt.launching.JRE_CONTAINER"/>
        <classpathentry kind="output" path="bin"/>
</classpath>
);
close D2W;

open D2W, ">$dest/.project" || die("Can't open $dest/.project");
print D2W q(<?xml version="1.0" encoding="UTF-8"?>
<projectDescription>
        <name>) . $dest . q(</name>
        <comment></comment>
        <projects>
        </projects>
        <buildSpec>
                <buildCommand>
                        <name>org.eclipse.jdt.core.javabuilder</name>
                        <arguments>
                        </arguments>
                </buildCommand>
                <buildCommand>
                        <name>org.objectstyle.wolips.incrementalbuilder</name>
                        <arguments>
                        </arguments>
                </buildCommand>
        </buildSpec>
        <natures>
                <nature>org.eclipse.jdt.core.javanature</nature>
                <nature>org.objectstyle.wolips.incrementalframeworknature</nature>
        </natures>
</projectDescription>
);
close D2W;

