#
# jruby examples for jas.
# $Id: legendre.rb 3794 2011-10-09 17:39:11Z kredel $

require "examples/jas"

# Legendre polynomial example
# P(0) = 1
# P(1) = x
# P(n) = 1/n [ (2n-1) * x * P(n-1) - (n-1) * P(n-2) ]

r = Ring.new( "Q(x) L" );
#r = Ring.new( "C(x) L" );
puts "Ring: " + str(r);
puts;

# sage like: with generators for the polynomial ring
one,x = r.gens();

N = 10;
P = [one,x];
for n in 2...N do
    p = (2*n-1) * x * P[n-1] - (n-1) * P[n-2];
    p = 1/n * p;
    P << p; #.monic();
end

for n in 0...N do
  puts "P[#{n}] = #{P[n]}";
end

puts;

#sys.exit();
