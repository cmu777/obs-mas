#
# jruby examples for jas.
# $Id: hermite.rb 3794 2011-10-09 17:39:11Z kredel $
#

require "examples/jas"

# hermite polynomial example
# H(0) = 1
# H(1) = 2 * x
# H(n) = 2 * x * H(n-1) - 2 * (n-1) * H(n-2)

r = Ring.new( "Z(x) L" );
puts "Ring: " + str(r);
puts;

# sage like: with generators for the polynomial ring
one,x = r.gens();

x2 = 2 * x;

N = 10;
H = [one,x2];
for n in 2..N
    h = x2 * H[n-1] - 2 * (n-1) * H[n-2];
    H << h;
end

for n in 0..N
  puts "H[#{n}] = #{H[n]}";
end
puts;

#sys.exit();
