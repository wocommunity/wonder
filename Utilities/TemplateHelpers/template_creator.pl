#!/usr/bin/perl
# Template creator tool
# Use this tool to easily create your own look from the ERDirectToWebJava
# templates
# It creates subclasses for every class whose name ends with "Template"
# and pre- and postfixes those with supplied parameters:
#   ERD2WInspectPage -> CMSInspectPagePlain
# it will also create a d2wmodel that contains rules like
#   *true* -> templateNameForInspectPage = "CMSInspectPagePlain" (20)

die print("usage: $0 <destination directory> <prefix> <postfix>\n") if($#ARGV != 2);

# you may need to adjust this path
$pathToERDirectToWeb = "~/Roots/ERDirectToWebJava.framework/Resources/";

($dest, $prefix, $postfix) = @ARGV;
$dest = "tmp";
$pattern = 'ERD2W(.*?)Template';

$cmd = "find $pathToERDirectToWeb -name \\*Template.wo";
print $cmd;
@files = `$cmd`;

foreach $file (@files) {
    if($file =~ m|(.*?)/([A-Za-z0-9_]+).wo|) {
        ($source, $class) = ($1, $2);
        if($class =~ m|$pattern|) {
            $typeName = "$1";
            $newClass = "$prefix$typeName$postfix";
            print "creating $newClass\n";
            open OUT, ">$dest/$newClass.java" || die("Can't open $dest/$newClass.java");
            print OUT "package er.directtoweb;\n";
            print OUT "import com.webobjects.appserver.WOContext;\n";
            print OUT "public class $newClass extends $class {\n";
            print OUT "\t$newClass(WOContext c) {\n";
            print OUT "\t\tsuper(c);\n";
            print OUT "\t\}\n}\n";
            close OUT;
            system "cp -r $source/$class.wo $dest/$newClass.wo";
            system "rm -rf $dest/$newClass.wo/CVS";
            system "mv $dest/$newClass.wo/$class.html $dest/$newClass.wo/$newClass.html";
            system "mv $dest/$newClass.wo/$class.wod $dest/$newClass.wo/$newClass.wod";
            system "mv $dest/$newClass.wo/$class.woo $dest/$newClass.wo/$newClass.woo";
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
open D2W, ">$dest/d2w.d2wmodel" || die("Can't open $dest/d2w.d2wmodel");
print D2W "{ rules = ( $rules )}";
close D2W;
