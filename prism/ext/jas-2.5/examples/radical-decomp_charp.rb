#
# jruby examples for jas.
# $Id: radical-decomp_charp.rb 3817 2011-10-29 18:37:12Z kredel $
#

require "examples/jas"

# polynomial examples: ideal radical decomposition, dim > 0, char p separable

#r = Ring.new( "Rat(x) L" );
#r = Ring.new( "Q(x) L" );
r = PolyRing.new(ZM(5),"x,y,z",PolyRing.lex);

puts "Ring: " + str(r);
puts;

one,x,y,z = r.gens();

#f1 = (x**2 - 5)**2;
#f1 = (y**10 - x**5)**3;
f2 = y**6 + 2 * x * y**4 + 3 * x**2 * y**2 + 4 * x**3;
f2 = f2**5;
f3 = z**10 - x**5;

f4 = (y**2 - x)**3;

#puts "f1 = " + str(f1);
puts "f2 = " + str(f2);
puts "f3 = " + str(f3);
#puts "f4 = " + str(f4);
puts;

F = r.ideal( "", list=[f2,f3] );

puts "F = " + str(F);
puts;

startLog();

t = System.currentTimeMillis();
R = F.radicalDecomp();
t = System.currentTimeMillis() - t;
puts "R = " + str(R);
puts;
puts "decomp time = " + str(t) + " milliseconds";
puts;

puts "F = " + str(F);
puts;

#startLog();
terminate();
