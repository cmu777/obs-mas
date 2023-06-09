#
# jython examples for jas.
# $Id: raksanyi_cr.py 2819 2009-09-21 13:18:47Z kredel $
#

import sys;

from jas import Ring
from jas import ParamIdeal
from jas import startLog
from jas import terminate


# Raksanyi & Walter example
# integral/rational function coefficients

#r = Ring( "RatFunc(a1, a2, a3, a4) (x1, x2, x3, x4) L" );
r = Ring( "IntFunc(a1, a2, a3, a4) (x1, x2, x3, x4) L" );
print "Ring: " + str(r);
print;

ps = """
(
 ( x4 - { a4 - a2 } ),
 ( x1 + x2 + x3 + x4 - { a1 + a3 + a4 } ),
 ( x1 x3 + x1 x4 + x2 x3 + x3 x4 - { a1 a4 + a1 a3 + a3 a4 } ),
 ( x1 x3 x4 - { a1 a3 a4 } )
) 
""";

f = r.paramideal( ps );
print "ParamIdeal: " + str(f);
print;

#sys.exit();

#startLog();

gs = f.CGBsystem();
print "CGBsystem: " + str(gs);
print;

#sys.exit();

bg = gs.isCGBsystem();
if bg:
    print "isCGBsystem: true";
else:
    print "isCGBsystem: false";
print;


rs = gs.regularRepresentation();
print "regular representation: " + str(rs);
print;

rs = gs.regularRepresentationBC();
print "boolean closed regular representation: " + str(rs);
print;

startLog();

bg = rs.isRegularGB();
if bg:
    print "pre isRegularGB: true";
else:
    print "pre isRegularGB: false";
print;

rsg = rs.regularGB();
print "regular GB: " + str(rsg);
print;

bg = rsg.isRegularGB();
if bg:
    print "post isRegularGB: true";
else:
    print "post isRegularGB: false";
print;


ss = rsg.stringSlice();
print "regular string slice: " + str(ss);

terminate();
#sys.exit();

