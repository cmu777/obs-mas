#
# jython examples for jas.
# $Id: sicora.py 3316 2010-09-06 18:46:03Z kredel $
#

import sys;

from jas import Ring, Ideal
from jas import startLog, terminate

# sicora, e-gb example

r = Ring( "Z(t) L" );
print "Ring: " + str(r);
print;

ps = """
( 
 ( 2 t + 1 ),
 ( t**2 + 1 )
)
""";

f = r.ideal( ps );
print "Ideal: " + str(f);
print;

#startLog();

g = f.eGB();
print "seq e-GB:", g;
print "is e-GB:", g.iseGB();
print;
