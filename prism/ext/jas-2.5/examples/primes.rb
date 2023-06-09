#
# jruby examples for jas.
# $Id: primes.rb 4262 2012-10-21 13:24:47Z kredel $
#

require "examples/jas"

# example for prime numbers
#

pl = PrimeList.new(PrimeList::Range.small);
puts "pl:   " + str(pl);
puts;
pp = "";
for i in 0..pl.size()+10 do
   pp = pp + " " + str(pl.get(i))
end 
puts "pp:   " + pp;
puts;

pl = PrimeList.new(PrimeList::Range.low);
puts "pl:   " + str(pl);
puts;
pp = "";
for i in 0..pl.size()+10 do
   pp = pp + " " + str(pl.get(i))
end 
puts "pp:   " + pp;
puts;

pl = PrimeList.new(PrimeList::Range.medium);
puts "pl:   " + str(pl);
puts;
pp = "";
for i in 0..pl.size()+10 do
   pp = pp + " " + str(pl.get(i))
end 
puts "pp:   " + pp;
puts;

pl = PrimeList.new(PrimeList::Range.large);
puts "pl:   " + str(pl);
puts;
pp = "";
for i in 0..pl.size()+10 do
   pp = pp + " " + str(pl.get(i))
end 
puts "pp:   " + pp;
puts;

#pl = PrimeList.new(PrimeList::Range.mersenne);
#puts "pl:   " + str(pl);
#puts;
