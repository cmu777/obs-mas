#
# jython examples for jas.
# $Id: factors_algeb_trans_build.py 3565 2011-03-13 14:32:58Z kredel $
#

import sys

from java.lang import System
from java.lang import Integer

from jas import Ring
from jas import PolyRing
from jas import Ideal
from jas import QQ, AN, RF, EF
from jas import terminate
from jas import startLog

# polynomial examples: factorization over Q(sqrt(2))(x)(sqrt(x))[y]

Yr = EF(QQ()).extend("w2","w2^2 - 2").extend("x").extend("wx","wx^2 - x").polynomial("y").build();
#print "Yr    = " + str(Yr);
#print

[one,w2,x,wx,y] = Yr.gens();
print "one   = " + str(one);
print "w2    = " + str(w2);
print "x     = " + str(x);
print "wx    = " + str(wx);
print "y     = " + str(y);
print;


f = ( y**2 - x ) * ( y**2 - 2 );
#f = ( y**2 - x )**2 * ( y**2 - 2 )**3;
#f = ( y**4 - x * 2 );
#f = ( y**7 - x * 2 );
#f = ( y**2 - 2 );
#f = ( y**2 - x );
#f = ( w2 * y**2 - 1 );
#f = ( y**2 - 1/x );
#f = ( y**2 - (1,2) );
#f = ( y**2 - 1/x ) * ( y**2 - (1,2) );

print "f = ", f;
print;

#sys.exit();

startLog();

t = System.currentTimeMillis();
G = Yr.factors(f);
t = System.currentTimeMillis() - t;
#print "G = ", G;
#print "factor time =", t, "milliseconds";

#sys.exit();

print "f    = ", f;
g = one;
for h, i in G.iteritems():
    if i > 1:
        print "h**i = ", h, "**" + str(i);
    else:
        print "h    = ", h;
    h = h**i;
    g = g*h;
#print "g = ", g;

if cmp(f,g) == 0:
    print "factor time =", t, "milliseconds,", "isFactors(f,g): true" ;
else:
    print "factor time =", t, "milliseconds,", "isFactors(f,g): ",  cmp(f,g);
print;

#startLog();
terminate();
