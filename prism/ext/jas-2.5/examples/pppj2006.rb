#
# jruby examples for jas.
# $Id: pppj2006.rb 3969 2012-06-30 14:31:43Z kredel $
#

require "examples/jas"

# pppj 2006 paper examples

r = PolyRing.new( ZZ(), "x1,x2,x3", PolyRing.lex );
puts "Ring: " + str(r);
puts;


f = 3 * x1**2 * x3**4 + 7 * x2**5 - 61;

puts "f = " + str(f);
puts;

id = r.ideal( "", list=[f] );
puts "Ideal: " + str(id);
puts;

ri = r.ring;
puts "ri = " + str(ri);

pol = r.pset;
puts "pol = " + str(pol);

pol = ri.parse( str(f) );
puts "pol = " + str(pol);
puts;

#startLog();
#terminate();
