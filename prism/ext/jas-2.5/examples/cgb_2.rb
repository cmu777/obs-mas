#
# jruby examples for jas.
# $Id: cgb_2.rb 3785 2011-09-29 21:43:06Z kredel $
#

require "examples/jas"

# 2 univariate polynomials of degree 2 example for comprehensive GB
# integral/rational function coefficients

#r = Ring.new( "IntFunc(a2, a1, a0, b2, b1, b0) (x) L" );
r = PolyRing.new( PolyRing.new(ZZ(),"(a2, a1, a0, b2, b1, b0)",PolyRing.lex),"(x)", PolyRing.lex );
puts "Ring: " + str(r);
puts;

ps = """
(
 ( { a2 } x^2 + { a1 } x + { a0 } ),
 ( { b2 } x^2 + { b1 } x + { b0 } )
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
puts;

#sys.exit();

gs = f.CGB();
puts "CGB: " + str(gs);
puts;

terminate();
#sys.exit();

#bg = gs.isCGB();
#puts "isCGB: " + str(bg);
#puts;

#terminate();
#sys.exit();


