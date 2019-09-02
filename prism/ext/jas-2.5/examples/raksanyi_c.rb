#
# jruby examples for jas.
# $Id: raksanyi_c.rb 4650 2013-10-03 11:08:01Z kredel $
#

require "examples/jas"

# Raksanyi & Walter example
# integral/rational function coefficients

#nono: r = Ring.new( "RatFunc(a1, a2, a3, a4) (x1, x2, x3, x4) L" );
#r = Ring.new( "IntFunc(a1, a2, a3, a4) (x1, x2, x3, x4) G" );
#r = Ring.new( "IntFunc(a1, a2, a3, a4) (x4, x3, x2, x1) L" );
r = PolyRing.new( PolyRing.new(QQ(),"a1, a2, a3, a4",PolyRing.lex), "x1, x2, x3, x4", PolyRing.grad );
puts "Ring: " + str(r);
puts;

ps = """
(
 ( x4 - { a4 - a2 } ),
 ( x1 + x2 + x3 + x4 - { a1 + a3 + a4 } ),
 ( x1 x3 + x1 x4 + x2 x3 + x3 x4 - { a1 a4 + a1 a3 + a3 a4 } ),
 ( x1 x3 x4 - { a1 a3 a4 } )
) 
""";

f = r.paramideal( ps );
puts "ParamIdeal: " + str(f);
puts;

#sys.exit();

#startLog();

gs = f.CGBsystem();
puts "CGBsystem: " + str(gs);
puts;

#sys.exit();

bg = gs.isCGBsystem();
puts "isCGBsystem: " + str(bg);
puts

#sys.exit();

gs = f.CGBsystem();
gs = f.CGBsystem();
gs = f.CGBsystem();

puts "2-CGBsystem: " + str(gs);
puts;

gs = f.CGB();
puts "CGB: " + str(gs);
puts;

bg = gs.isCGB();
puts "isCGB: " + str(bg);
puts;

terminate();
#sys.exit();

