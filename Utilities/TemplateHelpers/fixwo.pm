#!/usr/bin/perl
#
# Usage: perl -mfixwo -e fixwo::run [+write] [+convert] WebObjectsFile1.wo WebObjectsFile2.wo
#
# fixes wo and wod files to create pretty decent xhtml code
# the parser is very stupid, it doesn't like tags like
#  <script src="<webobject name=mysrc></webobject>">
# but you don«t use code like that anyway, do you?


package fixwo;
use XML::Parser;

# debug mode, set to 0 when you really want to write files or provide -write and -convert

my $skipConvert = 1;
my $skipWrite = 1;


sub parseScript {
    my ($script, $encode) = (@_);
    my $notFound = "\0";
    my $GT = $notFound . "GT" . $notFound;
    my $LT = $notFound . "LT" . $notFound;

    if($encode) {
        $script =~ s|\>|$GT|gsi;
        $script =~ s|\<|$LT|gsi;
        $script =~ s|<\!-+(.*?)-+>|/** $1 */|gsi;
        $script =~ s|^(script.*?)$GT|$1>$LT\!--$LT\!\[CDATA\[\n|gsi;
        $script =~ s|$LT(/script)$|//\]\]$GT--$GT<$1|gsi;
        $script =~ s|$LT(/?webobject.*?)$GT|<$1>|gsi;
    } else {
        $script =~ s|$GT|>|gsi;
        $script =~ s|$LT|<|gsi;
    }
    return "<$script>";
}

sub parseValue {
    my ($value, $encode) = (@_);
    if($encode) {
        $value =~ s/([^A-Za-z0-9])/sprintf("%%%02X", ord($1))/seg;
    } else {
        $value =~ s/\%([A-Fa-f0-9]{2})/pack('C', hex($1))/seg;
        if($value =~ m|\"|) {
            $value = "'" . $value . "'";
        } else {
            $value = "\"" . $value . "\"";
        }
    }
    return $value;
}

sub parseTag {
    my ($end, $tag, $rest) = (@_);
    $rest =~ s|nowrap([^="]?)|nowrap=nowrap$1|gsi;
    $rest =~ s|noshade|noshade=noshade|gsi;
    $rest =~ s|^\s*(.*?)\s*$|$1|gsi;
    $rest =~ s|=\s*"(.*?)"|'=' . parseValue($1,1) . "\0"|gesi;
    $rest =~ s|=\s*'(.*?)'|'=' . parseValue($1,1) . "\0"|gesi;
    $rest =~ s|\s+|\0|gsi;
    $rest =~ s|\s+|\0|gsi;
    $rest = "$rest\0";
    $rest =~ s|\0+|\0|gsi;
    $rest =~ s#([a-zA-Z0-9_\-\:]+)\=(.*?)\0# lc($1) . '=' . parseValue($2,0) . ' ' #egsi;
    $tag = lc($tag);
    $rest = " $rest" if($rest ne "" && $rest !~ m|\s*/\s*$|i);
    $rest = "$rest /" if($rest !~ m|\s*/\s*$|i && ($tag eq 'spacer' || $tag eq 'meta' || $tag eq 'link' || $tag eq 'meta' || $tag eq 'br' || $tag eq 'img' || $tag eq 'hr' || $tag eq 'input'));
    $rest =~ s|\0| |gsi;
    $rest =~ s|\s+$||gis;
    my $result = $end . lc($tag) . $rest;

    return $result;
}

sub lowerCaseBinding {
    my ($key,$val) = (@_);
    $key = lc($key) if($key !~ m/elementname|elementid|invokeaction|omittags|othertagstring| formValues|formValue/i);
    $val = "\"$val\"" if($val =~ m/^\d+$/i);
    return "\t$key = $val;\n";
}
    
sub parseWod {
    my ($name, $class, $rest) = (@_);
    $rest =~ s|^\n*(.*?)\n*$|$1|isg;
    if($class =~ m/WOGenericContainer|WOGenericElement/) {
        $rest =~ s|elementName\s*=\s*(".*?")\s*;|"elementName = " . lc($1). ";"|iesg;
        $rest =~ s|\s*(\w+)\s*\=\s*(.*?);|lowerCaseBinding($1,$2)|iesg;
    }
    if($rest !~ m|\n$|is) {
        $out = "$name: $class {\n$rest\n}";
    } else {
        $out = "$name: $class {\n$rest}";
    }
    return $out;
}

sub fixwohtml {
    my ($in) = (@_);
    my $out = $in;

    return $in if($skipConvert != 0);

    $out =~ s|<(script.*?/script)>|parseScript($1,1)|igse;
    $out =~ s|<\s*(/?)\s*(\w+)(.*?)\s*(/?)>|"<" . parseTag($1,$2,$3) .'>'|igse;
    $out =~ s|<(script.*?/script)>|parseScript($1,0)|igse;
    return $out;
}

sub fixwowod {
    my ($in) = (@_);
    my $out = $in;

    return $in if($skipConvert != 0);

    $out =~ s|(\s*)(\w+)\s*\:\s*(\w+)\s*\{(.*?)\}|$1 . parseWod($2,$3,$4)|igse;
    return $out;
}

sub fixwo {
    return fixwowod(@_);
}

sub readFromFile {
    my ($name) = (@_);
    my $out;
    if(!open (IN, "$name")) {
        warn("Can't open '$name'") ; return $out;
    }
    $out = join("", <IN>);
    close(IN);
    return $out;
}

sub writeToFile {
    my ($name, $string) = (@_);
    my $out;
    if($skipWrite != 0) {
        print "\n=========================\n$name:\n=========================\n$string";
        return;
    }
    open (OUT, ">$name") || die("Can't open '$name'");
    print OUT $string;
    close(OUT);
}

sub run {
    die ("perl -mfixwo -e fixwo::run WebObjectsFile1.wo WebObjectsFile2.wo") if($#ARGV < 0);

    foreach $component (@ARGV) {
   	    my $debug = 0;
        if($component =~ m|^\+(\w+)|i) {
            $skipWrite = 0 if($1 eq "write");
            $skipConvert = 0 if($1 eq "convert");
            $debug = 1 if($1 eq "debug");
            next;
        }
        $component =~ s|\.wo(.*?)$||;
        my $base = $component;
        $base = $component if(!length($base) > 0);
#        warn("Processing $base\n");
        my $wod = "$component.wo/$base.wod";
        my $html = "$component.wo/$base.html";
        $xhtml = fixwohtml(readFromFile($html));
        if(!$skipConvert) {
            my $parser = new XML::Parser(NoExpand => 'true', Style => $debug ? 'Debug' : undef, ErrorContext => 3);
            my $xhtmlToCheck = $xhtml;
            # a real check just takes too much time, so we just
            # strip comments, text and processing instructions and add a dummy root element
            # the error context is not the actual html, but an abbreviated version that 
            # should at least point you to the source of the problem
            $xhtmlToCheck =~ s|<\!--(.*?)-->||isg;
            $xhtmlToCheck =~ s|<\!.*?>||isg;
            $xhtmlToCheck =~ s|>(.*?)<|>\n<|isg;
            $xhtmlToCheck =~ s|^[^<]*?(<.*?>)[^>]*?$|$1|is;
            eval{ $parser->parse("<xml>" . qq{$xhtmlToCheck} . "</xml>") };
            warn("Error in doc: $html ($evalReply)\n$xhtml\n\n") if($evalReply = $@);
        }
        writeToFile($html, $xhtml);
        writeToFile($wod , fixwowod(readFromFile($wod)));
    }
}
1;

