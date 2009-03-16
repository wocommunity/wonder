#!/usr/bin/perl -w
# 
# woaFrameworkMerger.pl
#
# Copyright (c) 2002 Red Shed Software. All rights reserved.
# by Jonathan 'Wolf' Rentzsch, jon at redshed dot net
#
# Fri Apr 19 2002 wolf: Created.
# Mon Jul 01 2002 wolf: Now handles relative input paths.
#                       Now handles relative framework paths.
# Thu Aug 15 2002 wolf: Now handles rebuilds (that's when the ClassPath file is regenerated
#                       but copied frameworks still exist in the "Frameworks" folder).
# Thu Aug 29 2002 wolf: Now overwrites previous framework copy if original is newer than copy.
#                       TODO: Merge WebServerResources?
#						TODO: Handle Windows (somebody else will have to implement this)

use Cwd 'cwd', 'chdir', 'abs_path';
%paths = ();
foreach $woa ( @ARGV[0..$#ARGV] ) {
	$woa = abs_path( $woa );
	
	my $macOSPath = $woa . '/Contents/MacOS/MacOSClassPath.txt';
	my $unixPath = $woa . '/Contents/Unix/UnixClassPath.txt';
	my $frameworksPath = $woa . '/Contents/Frameworks';
	my $macOS = slurp( $macOSPath );
	my @inputPaths = split( /[\n\r]+/, $macOS );
	my @outputPaths = ();
	my $madeFrameworkDir = 0;
	
	chdir( "$woa" ); # cd into .woa
	chdir( "../.." );# go up from project/build/app.woa to project/
	$cpCWD = cwd();
	#print "cpCWD = $cpCWD\n";
	
	for $inputPath ( @inputPaths ) {
		# fix up ant-generated paths
		$inputPath =~ s#^/System#WOROOT# if($inputPath =~ m#^/System#);

		if( $inputPath =~ m#^(/|\.\.|LOCALROOT|HOMEROOT|WOROOT).+\.jar$# ) {
			if( $madeFrameworkDir == 0 ) {
				if ( -e $frameworksPath ) {
					print  "removing $frameworksPath\n";
					system "rm -rf $frameworksPath";
				}
				if( ! -e $frameworksPath ) {
					mkdir $frameworksPath or die 'couldn\'t make Frameworks directory';
				}
				$madeFrameworkDir = 1;
			}
			$inputPath =~ s|Versions/./||;
			my @pathArray = split( /\//, $inputPath );
			my $pathCount = $#pathArray;
			
			# From:
			# /Volumes/Island/wolf/code/Log4JFramework/build/Log4J.framework/Resources/Java/Log4J.jar
			# To:
			# /Volumes/Island/wolf/code/Log4JFramework/build/Log4J.framework
			my $copyFromPath = join( '/', @pathArray[ 0..($pathCount-3) ] );
			$copyFromPath = join( '/', @pathArray[ 0..($pathCount-3) ] ) if();

			# LOCALROOT is redundant on X, delete it.
			$copyFromPath =~ s/^WOROOT/\/System/;
			$copyFromPath =~ s/^LOCALROOT//;
			$copyFromPath =~ s/^HOMEROOT/~/;
			
			my $frameworkClassPath = 'APPROOT/Frameworks/' . join( '/', @pathArray[ ($pathCount-3)..$pathCount ] );
#			print "frameworkClassPath = $frameworkClassPath\n";
			my $frameworkName = $pathArray[ ($pathCount-3) ];
			
			my $currentFrameworkPath = $frameworksPath . '/' . $frameworkName;
			if(( -e $currentFrameworkPath) && (modDate($copyFromPath) > modDate($currentFrameworkPath)) ) {
				print "deleting outdated $frameworkName copy...\n";
				system( "cd $cpCWD;rm -rf $currentFrameworkPath" ) == 0 or die 'failed to delete outdated framework copy';
			}
			if( !-e $currentFrameworkPath ) {
				print "copying $frameworkName...\n";
				system( "cd $cpCWD; cp -Rp $copyFromPath $frameworksPath" ) == 0 or die 'failed to copy framework';
			}
			push @outputPaths, $frameworkClassPath if(!defined($paths{$frameworkClassPath}));
			$paths{$frameworkClassPath} = $frameworkClassPath;
		} else {
			push @outputPaths, $inputPath if(!defined($paths{$inputPath}));
			$paths{$inputPath} = $inputPath;
		}
	}
	$macOS = join( "\n", @outputPaths ) . "\n";
	print "rewriting MacOSClassPath.txt...\n";
	burp( $macOSPath, $macOS );
	print "rewriting UnixClassPath.txt...\n";
	burp( $unixPath, $macOS );
}

sub slurp {
	my ($file) = @_;
	
	open( FILE, $file ) or die "error: couldn't open file $file";
	my @file_stat = stat( FILE );
	my $file_size = $file_stat[ 7 ];
	my $file_content = "";
	
	if( $file_size != 0 ) {
		read( FILE, $file_content, $file_size ) or die "error: couldn't read file $file\n";
	} else {
		print "warning: file $file is empty\n";
	}
	close FILE;
	
	return $file_content;
}

sub burp {
	my ($file, $file_content) = @_;
	
	open( FILE, ">$file" ) or die "error: couldn't open file $file";
	print FILE $file_content or die "error: couldn't write to file $file";
	close( FILE ) or die "error: could not close file $file";
}

sub modDate {
	my ($path) = @_;
	return (-e $path ? (stat($path))[9] : 0 );
}
