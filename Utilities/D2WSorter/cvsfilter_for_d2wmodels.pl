#!/usr/bin/perl 
die ("Usage: $@ infile outfile\n") if($#ARGV != 1);
my ($in, $out) = @ARGV;

my $sep = "\n        {";
my $comment = "\n//11\n//12\n//13\n//14\n//15\n//16$sep";

`cp "$in" "$out"`;
`~/Roots/D2WSorter "$out" author.intValue rhs.keyPath lhs.description`;
open(OUT, "$out");
$content = join("", <OUT>);
close(OUT);
$content =~ s|\n            \s*||sg;
$content =~ s|\n        \}(,)?|\}\1|sg;
open(OUT, ">$out");
print OUT join($comment, split($sep, $content));
close(OUT);
