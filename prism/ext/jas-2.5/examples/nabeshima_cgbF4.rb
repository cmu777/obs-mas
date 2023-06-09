#
# jruby examples for jas.
# $Id: nabeshima_cgbF4.rb 3815 2011-10-29 13:47:55Z kredel $
#

require "examples/jas"

# Nabashima, ISSAC 2007, example F4
# integral function coefficients

r = Ring.new( "IntFunc(a, b, c, d) (y, x) L" );
puts "Ring: " + str(r);
puts;

ps = """
(
 ( { a } x^3 y + { c } x y^2 ),
 ( x^4 y + { 3 d } y ),
 ( { c } x^2 + { b } x y ),
 ( x^2 y^2 + { a } x^2 ),
 ( x^5 + y^5 )
) 
""";

#startLog();

f = r.paramideal( ps );
puts "ParamIdeal: " + str(f);
puts;

gs = f.CGBsystem();
gs = f.CGBsystem();
gs = f.CGBsystem();
gs = f.CGBsystem();
puts "CGBsystem: " + str(gs);
puts;

terminate();
exit();

bg = gs.isCGBsystem();
if bg
    puts "isCGBsystem: true";
else
    puts "isCGBsystem: false";
end
puts;

#exit();

gs = f.CGB();
puts "CGB: " + str(gs);
puts;

bg = gs.isCGB();
if bg
    puts "isCGB: true";
else
    puts "isCGB: false";
end
puts;

terminate();

#------------------------------------------
#exit();
