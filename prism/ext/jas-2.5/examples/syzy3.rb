#
# jruby examples for jas.
# $Id: syzy3.rb 3823 2011-11-01 11:33:05Z kredel $
#

require "examples/jas"

# ? example

r = Ring.new( "Rat(x,y,z) L" );
print "Ring: " + str(r);
print;

ps = """
( 
 ( z^3 - y ),
 ( y z - x ),
 ( y^3 - x^2 z ),
 ( x z^2 - y^2 )
) 
""";

f = SimIdeal.new( r, ps );
print "Ideal: " + str(f);
print;


include_class "edu.jas.gbmod.SyzygyAbstract";

#startLog();

R = SyzygyAbstract.new().resolution( f.pset );

for i in 0..R.size() 
    puts "\n #{i+1}. resolution";
    puts "\n" + str(R[i]);
end

#terminate();
