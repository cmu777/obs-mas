#
# jruby examples for jas.
# $Id: cgb_0.rb 3792 2011-10-03 12:37:47Z kredel $
#

require "examples/jas"

# simple example for comprehensive GB
# integral/rational function coefficients

#r = Ring( "RatFunc(u,v) (x,y) L" );
#r = Ring.new( "IntFunc(u,v) (x,y) L" );
r = PolyRing.new( PolyRing.new(ZZ(),"(u,v)",PolyRing.lex),"(x,y)", PolyRing.lex );
puts "Ring: " + str(r);
puts;

ps = """
(
 ( { v } x y + x ),
 ( { u } y^2 + x^2 )
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

#bg = gs.isCGBsystem();
#puts "isCGBsystem: " + str(bg);
#puts;

#sys.exit();

#startLog();

gs = f.CGB();
puts "CGB: " + str(gs);
puts;

#startLog();

#bg = gs.isCGB();
#puts "isCGB: " + str(bg);
#puts;

terminate();
#sys.exit();

