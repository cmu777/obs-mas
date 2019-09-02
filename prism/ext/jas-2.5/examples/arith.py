#
# jython examples for jas.
# $Id: arith.py 2586 2009-04-20 20:38:53Z kredel $
#

import sys;

from jas import Ring
from jas import ZZ
from jas import QQ
from jas import CC
from jas import RF
from jas import startLog
from jas import terminate

# example for rational and complex numbers
#
#

zn = ZZ(7);
print "zn:", zn;
print "zn^2:", zn*zn;
print;

x = 10000000000000000000000000000000000000000000000000;
rn = QQ(2*x,4*x);
print "rn:", rn;
print "rn^2:", rn*rn;
print;

rn = QQ((6,4));
print "rn:", rn;
print "rn^2:", rn*rn;
print;

c = CC();
print "c:", c;
c = c.one();
print "c:", c;
c = CC((2,),(3,));
print "c:", c;
print "c^5:", c**5 + c.one();
print;

c = CC( (2,),rn );
print "c:", c;
print;


r = Ring( "Q(x,y) L" );
print "Ring: " + str(r);
print;

# sage like: with generators for the polynomial ring
[one,x,y] = r.gens();
zero = r.zero();

try:
    f = RF(r);
except:
    f = None;
print "f: " + str(f);

d = x**2 + 5 * x - 6;
f = RF(r,d);
print "f: " + str(f);

n = d*d + y + 1;
f = RF(r,d,n);
print "f: " + str(f);
print;

# beware not to mix expressions
f = f**2 - f;
print "f^2-f: " + str(f);
print;

f = f/f;
print "f/f: " + str(f);

f = RF(r,d,one);
print "f: " + str(f);

f = RF(r,zero);
print "f: " + str(f);

f = RF(r,d,y);
print "f: " + str(f);

print "one:  " + str(f.one());
print "zero: " + str(f.zero());
print;

terminate();
#sys.exit();
