#!/usr/bin/perl
$usage = <<EOF;
This tiny helper reads the ClassList.html and creates the command to move the files around in the project according to this documentation.

To use:
  cd ERDirectToWebJava
  cp  ERDirectToWebJava.pbproj/project.pbxproj  backup.pbxproj
  perl ../../Utilities/PBXTool/fix_from_html.pl ERDirectToWebJava.pbproj/project.pbxproj ERD2WClassList.html >doit
  sh doit
  open ERDirectToWebJava.pbproj
EOF

my ($proj, $desc) = (@ARGV);
$pbxtool = "../../Utilities/PBXTool/build/PBXTool";

die($usage) if(!$proj || !$desc);

open(IN, "$desc") or die ("Can't open $desc");
my $orig = join("", <IN>);
close(IN);

@parts = split("<h3>", $orig);
@groups = ();
foreach $part (@parts) {
    my ($group);
    
    $group = $1 if($part =~ m|(.*?)</h3>|i);
    if($group) {
        $group = "'$group'";
        push @groups, $group;
        while($part =~ m|<dt>(.*?)<dd>|ig) {
            $class = $1;
            $out = "$pbxtool $proj $class ";
            $out .= " $class.java" if(-e "$class.java");
            $out .= " $class.wo" if(-e "$class.wo");
            $out .= " $class.api" if(-e "$class.api");
            if(length($out) > length("$pbxtool $proj $class  $class.java")) {
                $group .= " $class ";
                print "$out\n";
            } elsif(-e "$class.java") {
                $group .= " $class.java ";
            }
        }
        print "$pbxtool $proj $group\n";
    }
}

print "$pbxtool $proj Classes " . join(" ", @groups) . "\n";
