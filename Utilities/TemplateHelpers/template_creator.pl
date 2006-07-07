#!/usr/bin/perl
# Template creator tool
# Use this tool to easily create your own look from the ERDirectToWebJava
# templates
# It creates subclasses for every class whose name ends with "Template"
# and pre- and postfixes those with supplied parameters:
#   ERD2WInspectPage -> CMSInspectPagePlain
# it will also create a d2wmodel that contains rules like
#   *true* -> templateNameForInspectPage = "CMSInspectPagePlain" (20)

die print("usage: $0 <destination directory> <prefix> <postfix> <package>\n") if($#ARGV != 3);

# you may need to adjust this path
$pathToERDirectToWeb = "~/Roots/ERNeutralLook.framework/Resources/";

($dest, $prefix, $postfix, $package) = @ARGV;
#$dest = "tmp";
$pattern = 'ERNEU(.*?)Page';
$packageDir = $package;
$packageDir =~ s|\.|/|g;

$cmd = "find $pathToERDirectToWeb -name \\*Page.wo -type d |grep -v CVS";
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
            system "mkdir $dest/Components/Nonlocalized.lproj" if(!-e "$dest/Components/Nonlocalized.lproj");
            system "rm -rf $dest/Components/Nonlocalized.lproj/$newClass.wo" if(-e "$dest/Components/Nonlocalized.lproj/$newClass.wo");
            system "cp -r $source/$class.wo $dest/Components/Nonlocalized.lproj/$newClass.wo";
            system "cp -r $source/../$class.api $dest/Components/$newClass.api";
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
        keyPath = templateNameFor$typeName; 
        value = $newClass; 
    }; 
},";            
        }
    }
}
chop($rules);
            # print "~/Roots/PBXTool $dest/*.pbproj/project.pbxproj 'Components' $classes\n";
open D2W, ">$dest/Resources/d2w.d2wmodel" || die("Can't open $dest/Resources/d2w.d2wmodel");
print D2W "{ rules = ( $rules );}";
close D2W;
