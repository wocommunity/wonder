#!/usr/bin/perl
my %cache = ();

#also add simple @bindings without real text

$simpleComments = 0;

@projects = (
    "extras/webobjects/armehaut/AHApp",
    "extras/webobjects/armehaut/AHLook",
    "extras/webobjects/depfa/CMSApp",
    "extras/webobjects/depfa/CMSLook",
    "extras/webobjects/edelman/PRApp",
    "extras/webobjects/edelman/PRLook",
    "extras/webobjects/support/ERExtras",
    "Wonder/Common/Frameworks/ERExtensions",
    "Wonder/Common/Frameworks/ERCoreBusinessLogic",
    "Wonder/Common/Frameworks/ERDirectToWeb",
    "Wonder/Common/Frameworks/ERNeutralLook",
    "Wonder/Common/Frameworks/ERJavaMail",
    "Wonder/Common/Frameworks/JavaWOExtensions"
);

@packages = (
    "ag/kcmedia", 
    "ag/kcmedia/cms", 
    "net/taranga", 
    "er/testrunner", 
    "er/extensions", 
    "er/directtoweb", 
    "er/javamail",
    "er/corebusinesslogic",
    "com/webobjects/woextensions"
);

die("Usage: $0 Wonder/Common/Frameworks/*/Components/*.api ...\nYou should call this script from above of the Wonder tree\n") if($#ARGV < 0);

my $prefix;


sub beautify {
    my ($in, $suffix) = (@_);    
    $in =~ s|([A-Z]+)| " " . lc($1)|eg;    
    
    return "($suffix$prefix) $in";    
    
}

sub readFile {
	my ($name) = (@_);
	my ($out);

	open(IN, $name) ||die("Can't open $name");
	$out = join("", <IN>);
	close(IN);

	return $out;
}

sub bindingsFromAPI {
	my ($str) = (@_);
	my %out;	
	
	while($str =~ m|<binding name="(.*?)"|g) {
	    my $name = $1;	    
	    
	    $out{$name} = beautify($name, "api");	    
	}
	
	return %out;	
	
}

sub bindingsFromWOD {
	my ($str) = (@_);
	my %out;	
	
	while($str =~ m|=\s*\^(.+?);|g) {
	    my $name = $1;
	    $name = $1 if($name =~ m|^(.+?)\.|i);	    
	    
	    $out{$name} = beautify($name, "wod");	    
	}
	
	return %out;	
	
}

sub bindingsFromSRC {
	my ($str) = (@_);
	my %out;	
	
	while($str =~ m|valueForBinding\("(.*?)"\)|g) {
	    my $name = $1;	    
	    
	    $out{$name} = beautify($name, "src");	    
	}
	while($str =~ m|\w*ForBinding\w*\("(.*?)"|g) {
	    my $name = $1;	    
	    
	    $out{$name} = beautify($name, "src");	    
	}
	while($str =~ m|hasBinding\("(.*?)"\)|g) {
	    my $name = $1;	    
	    
	    $out{$name} = beautify($name, "src");	    
	}
	
	return %out;	
	
}

sub headComment {
    my ($str) = (@_);    
    
    return $1 if($str =~ m|/\*\*(.*?)\*/(.*?)[\n\r]public|s);    
    
    return "";    
}

sub bindingsFromCMT {
	my ($str) = (@_);
	my (%out,$headComment, $line);	
	
	$headComment = headComment($str);	
	
	if(length($headComment) > 0) {
	    # skip the first entry
	    
	    my @arr = split("\@binding", $headComment);	    
	    shift(@arr);	    
	    
	    
	    
	    foreach $line (@arr) {
		$line =~ s|\" = defaults=\"(.*)||i;		
		$line =~ s|\@created.*||i;		
		$line =~ s|\@project.*||i;		
		$line =~ s|//.*||i;		
		
		my ($name, $comment) = ($1, $2) if($line =~ m|\s+(.*?)\s+(.*)|s);	    
		($name, $comment) = ($1, $1) if(!defined($name) && $line =~ m|\s+(.*?)|s);		
		if(defined($name)) {
		    next if($name =~ m|\"|i);		    
		    
		    $comment =~ s|\n| |g;		  
		    $comment =~ s|\*\s+| |g;		  
		    $comment =~ s|\*/||g;		  
		    $comment =~ s|\s+| |g;		
		    if($comment =~ m|^\s*$|i) {
			next  if(!$simpleComments);
			$comment = beautify($name, "cmt");			
		    } else {
			$comment = "(CMT$prefix) $comment";		    
		    }
		  
		  $out{$name} = $comment;
	      } else {
		  warn("No name defined: $str");		  
		  
	      }
	    }
	}
	
	return %out;	
	
}


sub hashToString {
    my ($prefix, $hash) = (@_);    
    my ($out);    
    
    
    foreach $key (sort(keys(%$hash))) {
	$out .= "$prefix: $key = $$hash{$key} \n";	
	
    }
    return $out;    
}

sub mergeHash {
    my (%out);    
    
    
    foreach $hash (@_) {
	foreach $key (keys(%$hash)) {
	    my ($old, $new, $src);	    
	    
	    $old = $out{$key};	    
	    $src = $1 if($old =~ m|^\(\s*(.*?)\s*\)|);	    
	    $new =  $$hash{$key};	    

	    if(defined($src)) {
		$new =~ s|^\((.+?)\s*\)|($1, $src)|i;
	    }
	    $new =~ s|\s+\)|)|;	    
	    $new =~ s|\s+| |;	    
	
	    $out{$key} = $new if(length($key) > 0 && "sample" ne $key && "localContext" ne $key);
	}
	
    }
    return %out;    
}

sub srcFile {
    my ($componentName) = (@_);    
    my ($project,$package);    
    
    foreach $project (@projects) {
	foreach $package (@packages) {
	    if(-e "$project/Sources/$package/$componentName.java") {
		    return "$project/Sources/$package/$componentName.java";
	    }
	
	}
    }
    return undef;    
    
}

sub apiFile {
    my ($componentName) = (@_);    
    my ($dir);    
    
    foreach $dir (@projects) {
	    if(-e "$dir/Components/$componentName.api") {
		    return "$dir/Components/$componentName.api";
	    }
    }
    return undef; 
}

sub wodFile {
    my ($componentName) = (@_);    
    my ($dir);    
    
    foreach $dir (@projects) {
	    if(-e "$dir/Components/$componentName.wo/$componentName.wod") {
		    return "$dir/Components/$componentName.wo/$componentName.wod";
	    }
	    if(-e "$dir/Components/Nonlocalized.lproj/$componentName.wo/$componentName.wod") {
		return "$dir/Components/Nonlocalized.lproj/$componentName.wo/$componentName.wod";
	    }
    }
    return undef;    
    
}

sub superClass {
    my ($src) = (@_);
    my ($out);    
    return undef if(length($src) == 0);    
    
    $out = $1 if($src =~ m|\npublic\s+class\s+.*?\s+extends\s+(.+?)\s+|s);    
    
    return $out if(defined($out) && defined(srcFile($out)));    
    return undef;    
    
}

sub bindingsForComponentNamed {
    my ($componentName) = (@_);    
    my ($srcFile, $wodFile, $apiFile);
    my ($src, $wod, $api);
    my ($out, $superClass, $superBindings, %apiBindings, %wodBindings, %srcBindings, %allBindings, %cmtBindings, %all);    

 #   return $cache{$componentName} if(defined($cache{$componentName}));    
    
    $srcFile = srcFile($componentName);    
    $wodFile = wodFile($componentName);    
    $apiFile = apiFile($componentName);    
 
    $src = readFile($srcFile) if(-e $srcFile);
    $wod = readFile($wodFile) if(-e $wodFile);
    $api = readFile($apiFile) if(-e $apiFile);

#    warn("$apiFile:" . length($api) . ", $wodFile: " . length($wod) . ", $srcFile: " . length($src));    
    
    if(length($src) > 0) {
	$superClass = superClass($src);
	if(defined($superClass) && length($superClass) > 0) {
	    $prefix .= " > $superClass ";	
	    
	    $superBindings = bindingsForComponentNamed($superClass);	

	    $prefix = substr($prefix, 0, length($prefix) - (4 + length($superClass)));	
	   
#	    $cache{$componentName} = $superBindings;    
	}
    }
    %srcBindings = bindingsFromSRC($src);	
    %wodBindings = bindingsFromWOD($wod);	
    %apiBindings = bindingsFromAPI($api);	
    %cmtBindings = bindingsFromCMT($src);	

    %allBindings = (mergeHash($superBindings, \%wodBindings, \%srcBindings, \%apiBindings, \%cmtBindings));	
#    %allBindings = (mergeHash($superBindings, \%wodBindings, \%srcBindings, \%apiBindings));	
        
    return \%allBindings;    
}
my %out;

my $fileName, $componentName;
foreach $fileName (sort(@ARGV)) {
	my ($componentName, %all, $all);
	$componentName = $1 if($fileName =~ m|(\w+)\.wo|);
	$componentName = $1 if($fileName =~ m|(\w+)\.api|);
	$componentName = $1 if($fileName =~ m|(\w+)\.java|);
	$prefix = "";	
#	warn $componentName;	
	$all = bindingsForComponentNamed($componentName) if(length($componentName) > 0);
	$out{$componentName} = $all;	
}

foreach $componentName (sort(keys(%out))) {
    print hashToString("$componentName", $out{$componentName});    
   
}