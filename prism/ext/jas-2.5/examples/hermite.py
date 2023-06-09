#
# jython examples for jas.
# $Id: hermite.py 2448 2009-02-26 13:31:07Z kredel $
#

import sys;

from jas import Ring
from jas import Ideal
from jas import startLog
from jas import terminate

# hermite polynomial example
# H(0) = 1
# H(1) = 2 * x
# H(n) = 2 * x * H(n-1) - 2 * (n-1) * H(n-2)

r = Ring( "Z(x) L" );
print "Ring: " + str(r);
print;

# sage like: with generators for the polynomial ring
[one,x] = r.gens();

x2 = 2 * x;

N = 10;
H = [one,x2];
for n in range(2,N):
    h = x2 * H[n-1] - 2 * (n-1) * H[n-2];
    H.append( h );

for n in range(0,N):
    print "H[%s] = %s" % (n,H[n]);

print;

#sys.exit();
