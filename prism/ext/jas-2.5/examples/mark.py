#
# jython examples for jas.
# $Id: mark.py 3797 2011-10-10 10:08:57Z kredel $
#

#import sys;

from jas import Ring
from jas import Ideal
from jas import startLog

# mark, d-gb diplom example

r = Ring( "Z(x,y,z) L" );
print "Ring: " + str(r);
print;

ps = """
( 
 ( z + x y**2 + 4 x**2 + 1 ),
 ( y**2 z + 2 x + 1 ),
 ( x**2 z + y**2 + x )
) 
""";

f = r.ideal( ps );
print "Ideal: " + str(f);
print;

#startLog();

eg = f.eGB();
print "seq e-GB:", eg;
print "is e-GB:", eg.isGB();
print;

dg = f.dGB();
print "seq d-GB:", dg;
print "is d-GB:", dg.isGB();
print;

print "d-GB == e-GB:", eg.list.equals(dg.list);
print "d-GB == e-GB:", eg == dg;
