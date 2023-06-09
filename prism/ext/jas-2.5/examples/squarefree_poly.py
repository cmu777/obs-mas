#
# jython examples for jas.
# $Id: squarefree_poly.py 2740 2009-07-12 13:40:44Z kredel $
#

from java.lang import System
from java.lang import Integer

from jas import PolyRing, QQ, ZM, ZZ
from jas import terminate
from jas import startLog

# polynomial examples: squarefree: characteristic 0

#r = PolyRing(PolyRing(QQ(),"u,v",PolyRing.lex),"x, y",PolyRing.lex)
#r = PolyRing(PolyRing(ZZ(),"u,v",PolyRing.lex),"x, y",PolyRing.lex)
r = PolyRing(PolyRing(ZM(7),"u,v",PolyRing.lex),"x, y",PolyRing.lex)

print "Ring: " + str(r);
print;

[one,u,v,x,y] = r.gens();

a = r.random(k=1,l=3);
b = r.random(k=1,l=3);
c = r.random(k=1,l=3);

if a.isZERO():
    a = x;
if b.isZERO():
    b = y;
if c.isZERO():
    c = y;

f = a**2 * c**3 * b;

print "a = ", a;
print "b = ", b;
print "c = ", c;
print "f = ", f;
print;

t = System.currentTimeMillis();
F = r.squarefreeFactors(f);
t = System.currentTimeMillis() - t;
print "factors:";
for g in F.keys():
    i = F[g];
    print "g = %s**%s" % (g,i);
print
print "factor time =", t, "milliseconds";

#startLog();
terminate();
