#
# jython examples for jas.
# $Id: radical-decomp_charp_infin_0dim.py 3157 2010-05-27 19:46:36Z kredel $
#

import sys;

from java.lang import System
from java.lang import Integer

from jas import Ring, PolyRing
from jas import terminate
from jas import startLog

from jas import QQ, ZM, RF

# polynomial examples: ideal radical decomposition, inseparable case, 0-dim

cr = PolyRing(ZM(5),"c",PolyRing.lex);
print "coefficient Ring: " + str(cr);
rf = RF(cr);
print "coefficient quotient Ring: " + str(rf);

r = PolyRing(rf,"x,y,z",PolyRing.lex);

print "Ring: " + str(r);
print;

[one,c,x,y,z] = r.gens();
print one,c,x,y,z;

#sys.exit();

#f1 = (x**2 - 5)**2;
#f1 = (y**10 - x**5)**3;
#f2 = y**6 + 2 * x * y**4 + 4 * x**2 * y**2 + 4 * x**3;
#f2 = y**6 + 2 * x * y**4 + 3 * x**2 * y**2 + 4 * x**3;
f1 = (x**2 + 2)**2;
f2 = (y**2 + 2)**3;
#f2 = f2**5;
f3 = z**10 - c**5;

f4 = (y**2 - x)**3;

print "f1 = ", f1;
print "f2 = ", f2;
print "f3 = ", f3;
#print "f4 = ", f4;
print;

F = r.ideal( list=[f1,f2,f3] );

print "F = ", F;
print;

startLog();

t = System.currentTimeMillis();
R = F.radicalDecomp();
t = System.currentTimeMillis() - t;
print "R = ", R;
print;
print "decomp time =", t, "milliseconds";
print;

print "F = ", F;
print;

#startLog();
terminate();
