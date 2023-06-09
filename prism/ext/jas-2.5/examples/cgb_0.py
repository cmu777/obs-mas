#
# jython examples for jas.
# $Id: cgb_0.py 2734 2009-07-12 12:04:37Z kredel $
#

import sys;

from jas import Ring
from jas import ParamIdeal
from jas import startLog
from jas import terminate


# simple example for comprehensive GB
# integral/rational function coefficients

#r = Ring( "RatFunc(u,v) (x,y) L" );
r = Ring( "IntFunc(u,v) (x,y) L" );
print "Ring: " + str(r);
print;

ps = """
(
 ( { v } x y + x ),
 ( { u } y^2 + x^2 )
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

#sys.exit();

#startLog();

gs = f.CGB();
print "CGB: " + str(gs);
print;

#startLog();

bg = gs.isCGB();
if bg:
    print "isCGB: true";
else:
    print "isCGB: false";
print;

terminate();
#sys.exit();

