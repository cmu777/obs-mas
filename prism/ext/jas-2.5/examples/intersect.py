#
# jython examples for jas.
# $Id: intersect.py 2435 2009-02-18 20:13:55Z kredel $
#

import sys;

from jas import Ring
from jas import Ideal
from jas import startLog
from jas import terminate


# ideal intersection example

r = Ring( "Rat(x,y,z) L" );
print "Ring: " + str(r);
print;

ps1 = """
(
 ( x - 1 ),
 ( y - 1 ),
 ( z - 1 )
)
""";

ps2 = """
(
 ( x - 2 ),
 ( y - 3 ),
 ( z - 3 )
)
""";

F1 = r.ideal( ps1 );
#print "Ideal: " + str(F1);
#print;

F2 = r.ideal( ps2 );
#print "Ideal: " + str(F2);
#print;

#startLog();

rg1 = F1.GB();
print "rg1 = ", rg1;
print;

rg2 = F2.GB();
print "rg2 = ", rg2;
print;

#startLog();

ig = F1.intersect(F2);
print "rg1 intersect rg2 = ", ig;
print;

terminate();
#sys.exit();
