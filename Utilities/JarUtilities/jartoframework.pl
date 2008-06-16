#!/usr/bin/perl
# Creates frameworks with optional source from a directory maven/jar files.
# usage: cd <dir with jars> && jartoframework.pl

@jars  = split("\n", join("", `ls -1 *.jar`));

# warning: our american friends might find themself in guantanamo for this
$bundlesToJad = "JavaFoundation/JavaEOAccess/JavaEOControl/JavaWebObjects/JavaWebServicesClient/JavaWebServicesGeneration/JavaWebServicesSupport/JavaDirectToWeb";

foreach $jar (@jars) {
    if($jar =~ /(\w+)\-.*?\.jar/) {
        my ($bundle) = ($1);
        my ($jarName, $frameworkName) = (lc($bundle . ".jar"), $bundle . ".framework");
        warn("Processing: $bundle\n");
        `mkdir -p $frameworkName/Resources/Java && cd $frameworkName/Resources/Java && jar xvf ../../../$jar && rm META-INF/INDEX.LIST`;
        if(-e "$frameworkName/Resources/Java/Resources") {
            `mv $frameworkName/Resources/Java/Resources/* $frameworkName/Resources && rm -Rf $frameworkName/Resources/Java/Resources`;
        }
        if(-e "$frameworkName/Resources/Java/WebServerResources") {
            `mv $frameworkName/Resources/Java/WebServerResources $frameworkName/ && rm -Rf $frameworkName/Resources/Java/WebServerResources`;
        }
        `cd $frameworkName/Resources/Java/ $framework && jar cvf ../$jarName *`;
        if($bundlesToJad =~ /$bundle/) {
            `cd $frameworkName/Resources/Java && mkdir ../Sources && find . -name \\*.class |xargs jad -d ../Sources -lnc -o -r -s java -ff -nonlb`;
        }
        `rm -Rf $frameworkName/Resources/Java/* && mv $frameworkName/Resources/$jarName $frameworkName/Resources/Java/$jarName`;
        if(-e "$frameworkName/Resources/Sources") {
             `cd $frameworkName/Resources/Sources/ && jar cvf ../Java/src.jar *`;
        }
    }
}
