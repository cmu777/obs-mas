#
# jython examples for jas.
# $Id: prime-decomp.py 3150 2010-05-24 11:20:27Z kredel $
#

from java.lang import System
from java.lang import Integer

from jas import Ring, PolyRing
from jas import terminate
from jas import startLog

from jas import QQ, DD

# polynomial examples: ideal prime decomposition

#r = Ring( "Rat(x) L" );
#r = Ring( "Q(x) L" );
r = PolyRing(QQ(),"x,y,z",PolyRing.lex);

print "Ring: " + str(r);
print;

[one,x,y,z] = r.gens();

f1 = (x**2 - 5)**2;
f2 = y**3 - x;
f3 = z**2 - y * x;

#print "f1 = ", f1;
print "f2 = ", f2;
print "f3 = ", f3;
print;

F = r.ideal( list=[f2,f3] );

print "F = ", F;
print;

startLog();

t = System.currentTimeMillis();
P = F.primeDecomp();
t1 = System.currentTimeMillis() - t;
print "P = ", P;
print;
print "prime decomp time =", t1, "milliseconds";
print;

print "F = ", F;
print;

#startLog();
terminate();
