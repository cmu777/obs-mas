#
# jruby examples for jas.
# $Id: squarefree_finite_alg.rb 3822 2011-10-30 21:09:42Z kredel $
#

require "examples/jas"

# polynomial examples: squarefree: characteristic p, finite algebraic

ar = PolyRing.new(ZM(7,field=true),"i",PolyRing.lex)
one,i = ar.gens();

# irred for 7, 11, 19
r = PolyRing.new(AN(i**2+1,field=true),"x, y, z",PolyRing.lex)
puts "Ring: " + str(r);
puts;

one,i,x,y,z = r.gens();

a = r.random(k=2,l=3);
b = r.random(k=2,l=3);
c = r.random(k=1,l=3);

if a.isZERO()
    a = x;
end
if b.isZERO()
    b = y;
end
if c.isZERO()
    c = z;
end

f = a**2 * b**7 * c;

puts "a = " + str(a);
puts "b = " + str(b);
puts "c = " + str(c);
puts "f = " + str(f);
puts;

t = System.currentTimeMillis();
F = r.squarefreeFactors(f);
t = System.currentTimeMillis() - t;
puts "factors:";

for g in F.keys()
    i = F[g];
    puts "g = #{g}**#{i}";
end
puts
puts "factor time = " + str(t) + " milliseconds";

startLog();
terminate();
