#!/usr/bin/perl
# Creates java source from a directory maven/jar files.
# usage: cd <dir with jars> && frameworktosrc.pl

@jars  = split("\n", join("", `ls -1d *.framework`));

# warning: our american friends might find themself in guantanamo for this
$bundlesToJad = "JavaFoundation/JavaEOAccess/JavaEOControl/JavaWebObjects/JavaWebServicesClient/JavaWebServicesGeneration/JavaWebServicesSupport/JavaDirectToWeb";

foreach $jar (@jars) {
    if($jar =~ /(\w+)\.framework/) {
        my ($bundle) = ($1);
        my ($frameworkName) = ($bundle . ".framework");
        warn("Processing: $bundle\n");
        `mkdir -p $frameworkName/Resources/Java/classes`;
        `cd $frameworkName/Resources/Java/classes && jar xvf ../$bundle.jar`;
        if($bundlesToJad =~ /$bundle/) {
            `cd $frameworkName/Resources/Java/classes && mkdir -p ../../Sources && find . -name \\*.class |xargs jad -d ../../Sources -lnc -o -r -s java -ff -nonlb`;
        }
        if(-e "$frameworkName/Resources/Sources") {
             `cd $frameworkName/Resources/Sources/ && jar cvf ../Java/src.jar *`;
        }
    }
}
