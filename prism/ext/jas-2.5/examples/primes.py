#
# jython examples for jas.
# $Id: primes.py 4262 2012-10-21 13:24:47Z kredel $
#

#import sys;
#from jas import startLog, terminate

from edu.jas.arith import PrimeList

# example for prime numbers
#

pl = PrimeList(PrimeList.Range.small);
print "pl:   " + str(pl);
print;
pp = "";
for i in range(0,pl.size()+10):
    pp = pp + " " + str(pl.get(i))
print "pp:   " + pp;
print;

pl = PrimeList(PrimeList.Range.low);
print "pl:   " + str(pl);
print;
pp = "";
for i in range(0,pl.size()+10):
    pp = pp + " " + str(pl.get(i))
print "pp:   " + pp;
print;

pl = PrimeList(PrimeList.Range.medium);
print "pl:   " + str(pl);
print;
pp = "";
for i in range(0,pl.size()+10):
    pp = pp + " " + str(pl.get(i))
print "pp:   " + pp;
print;

pl = PrimeList(PrimeList.Range.large);
print "pl:   " + str(pl);
print;
pp = "";
for i in range(0,pl.size()+10):
    pp = pp + " " + str(pl.get(i))
print "pp:   " + pp;
print;

#pl = PrimeList(PrimeList.Range.mersenne);
#print "pl:   " + str(pl);
#print;
