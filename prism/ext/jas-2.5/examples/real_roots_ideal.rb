#
# jruby examples for jas.
# $Id: real_roots_ideal.rb 3818 2011-10-29 20:21:39Z kredel $
#

require "examples/jas"

# polynomial examples: real roots over Q for zero dimensional ideals

r = PolyRing.new(QQ(),"q,w,s,x",PolyRing.lex);
puts "Ring: " + str(r);
puts;

one,q,w,s,x = r.gens();

# EF.new(QQ()).realExtend("q","q^3 - 3", "[1,2]").realExtend("w", "w^2 - q", "[1,2]").realExtend("s", "s^5 - 2", "[1,2]").polynomial("x").build();

f1 = q**3 - 3;
f2 = w**2 - q;
#f3 = s**5 - 2;
f3 = s**3 - 2;
f4 = x**2 - w * s;

puts "f1 = " + str(f1);
puts "f2 = " + str(f2);
puts "f3 = " + str(f3);
puts "f4 = " + str(f4);
puts;

F = r.ideal( "", list=[f1,f2,f3,f4] );

puts "F = " + str(F);
puts;

startLog();

t = System.currentTimeMillis();
R = F.realRoots();
t = System.currentTimeMillis() - t;
puts "R = " + str(R);
puts;
puts "real roots = ";
F.realRootsPrint()
puts "real roots time = " + str(t) + " milliseconds";
puts;

puts "F = " + str(F);
puts;

#startLog();
terminate();
