#
# jruby examples for jas.
# $Id: boolean_cgb.rb 3785 2011-09-29 21:43:06Z kredel $
#

require "examples/jas"

# Boolean coefficient boolean GB
# see S. Inoue and A. Nagai "On the Implementation of Boolean Groebner Bases" in ASCM 2009
# Z_2 regular ring coefficent example


r = PolyRing.new(RR(ZM(2),3),"a,x,y",PolyRing.lex);
puts "r  = " + str(r);
#puts len(r.gens())

s1,s2,s3,a,x,y = r.gens();
one = r.one();

puts "one = " + str(one);
puts "s1  = " + str(s1);
puts "s2  = " + str(s2);
puts "s3  = " + str(s3);
puts "a   = " + str(a);
puts "x   = " + str(x);
puts "y   = " + str(y);

#brel = [ a**2 - a, x**2 - x, y**2 - y ]; 
brel = [ x**2 - x, y**2 - y ]; 

#puts "brel = " + str(brel[0]) + ", " + str(brel[1]) + ", " + str(brel[2]);
puts "brel = " + str(brel[0]) + ", " + str(brel[1]);

pl = [ ( one + s1 + s2 ) * ( x*y + x + y ), s1 * x + s1, a * y + a, x * y ];


pl = pl + brel;


startLog();

f = ParamIdeal.new(r,"",pl);
puts "Ideal: " + str(f);

gb = f.regularGB();
puts "boolean GB: " + str(gb);

#ss = gb.stringSlice();
#puts "regular string slice: " + str(ss);

terminate();
#sys.exit();

