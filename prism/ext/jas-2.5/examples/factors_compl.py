#
# jython examples for jas.
# $Id: factors_compl.py 2946 2009-12-30 12:42:06Z kredel $
#

import sys;

from java.lang import System
from java.lang import Integer

from jas import Ring
from jas import PolyRing
from jas import Ideal
from jas import terminate
from jas import startLog

from jas import QQ, DD, CR

# polynomial examples: complex factorization via algebraic factorization

r = PolyRing( CR(QQ()), "x", PolyRing.lex );

print "Ring: " + str(r);
print;

[one,I,x] = r.gens();


f1 = x**3 - 2;

f2 = ( x - I ) * ( x + I );

f3 = ( x**3 - 2 * I );

#f = f1**2 * f2 * f3;
f = f1 * f2 * f3;
#f = f1 * f2;
#f = f1 * f3;
#f = f2 * f3;
#f = f3;

#f = f**3;

print "f = ", f;
print;

#startLog();

t = System.currentTimeMillis();
R = r.factors(f);
t = System.currentTimeMillis() - t;
#print "R = ", R;
#print "complex factor time =", t, "milliseconds";

g = one;
for h, i in R.iteritems():
    print "h**i = "+  str(h) + "**" + str(i);
    h = h**i;
    g = g*h;
#print "g = ", g;

if cmp(f,g) == 0:
    print "complex factor time =", t, "milliseconds,", "isFactors(f,g): true" ;
else:
    print "complex factor time =", t, "milliseconds,", "isFactors(f,g): ",  cmp(f,g);
print;

#startLog();
terminate();
