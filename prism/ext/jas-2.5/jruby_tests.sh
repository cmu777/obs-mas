#!/bin/sh
# run most rb files

echo jruby -J-cp ../lib/log4j.jar:../lib/junit.jar:. -J-verbose:gc examples/all_rings.rb
time jruby -J-cp ../lib/log4j.jar:../lib/junit.jar:. -J-verbose:gc examples/all_rings.rb
echo jruby -J-cp ../lib/log4j.jar:../lib/junit.jar:. -J-verbose:gc examples/trinks.rb
time jruby -J-cp ../lib/log4j.jar:../lib/junit.jar:. -J-verbose:gc examples/trinks.rb
echo jruby -J-cp ../lib/log4j.jar:../lib/junit.jar:. -J-verbose:gc examples/0dim_primary-decomp.rb
time jruby -J-cp ../lib/log4j.jar:../lib/junit.jar:. -J-verbose:gc examples/0dim_primary-decomp.rb
echo jruby -J-cp ../lib/log4j.jar:../lib/junit.jar:. -J-verbose:gc examples/0dim_prime-decomp.rb
time jruby -J-cp ../lib/log4j.jar:../lib/junit.jar:. -J-verbose:gc examples/0dim_prime-decomp.rb
echo jruby -J-cp ../lib/log4j.jar:../lib/junit.jar:. -J-verbose:gc examples/0dim_radical.rb
time jruby -J-cp ../lib/log4j.jar:../lib/junit.jar:. -J-verbose:gc examples/0dim_radical.rb
echo jruby -J-cp ../lib/log4j.jar:../lib/junit.jar:. -J-verbose:gc examples/cgb_0.rb
time jruby -J-cp ../lib/log4j.jar:../lib/junit.jar:. -J-verbose:gc examples/cgb_0.rb
echo jruby -J-cp ../lib/log4j.jar:../lib/junit.jar:. -J-verbose:gc examples/cgb_2.rb
time jruby -J-cp ../lib/log4j.jar:../lib/junit.jar:. -J-verbose:gc examples/cgb_2.rb
#echo jruby -J-cp ../lib/log4j.jar:../lib/junit.jar:. -J-verbose:gc examples/cgb_3.rb
#time jruby -J-cp ../lib/log4j.jar:../lib/junit.jar:. -J-verbose:gc examples/cgb_3.rb
#echo jruby -J-cp ../lib/log4j.jar:../lib/junit.jar:. -J-verbose:gc examples/cgbmmn15.rb
#time jruby -J-cp ../lib/log4j.jar:../lib/junit.jar:. -J-verbose:gc examples/cgbmmn15.rb
echo jruby -J-cp ../lib/log4j.jar:../lib/junit.jar:. -J-verbose:gc examples/chebyshev.rb
time jruby -J-cp ../lib/log4j.jar:../lib/junit.jar:. -J-verbose:gc examples/chebyshev.rb
echo jruby -J-cp ../lib/log4j.jar:../lib/junit.jar:. -J-verbose:gc examples/e-gb.rb
time jruby -J-cp ../lib/log4j.jar:../lib/junit.jar:. -J-verbose:gc examples/e-gb.rb
echo jruby -J-cp ../lib/log4j.jar:../lib/junit.jar:. -J-verbose:gc examples/eliminate.rb
time jruby -J-cp ../lib/log4j.jar:../lib/junit.jar:. -J-verbose:gc examples/eliminate.rb
echo jruby -J-cp ../lib/log4j.jar:../lib/junit.jar:. -J-verbose:gc examples/factors.rb
time jruby -J-cp ../lib/log4j.jar:../lib/junit.jar:. -J-verbose:gc examples/factors.rb
echo jruby -J-cp ../lib/log4j.jar:../lib/junit.jar:. -J-verbose:gc examples/factors_abs.rb
time jruby -J-cp ../lib/log4j.jar:../lib/junit.jar:. -J-verbose:gc examples/factors_abs.rb
echo jruby -J-cp ../lib/log4j.jar:../lib/junit.jar:. -J-verbose:gc examples/factors_abs_complex.rb
time jruby -J-cp ../lib/log4j.jar:../lib/junit.jar:. -J-verbose:gc examples/factors_abs_complex.rb
echo jruby -J-cp ../lib/log4j.jar:../lib/junit.jar:. -J-verbose:gc examples/factors_abs_mult.rb
time jruby -J-cp ../lib/log4j.jar:../lib/junit.jar:. -J-verbose:gc examples/factors_abs_mult.rb
echo jruby -J-cp ../lib/log4j.jar:../lib/junit.jar:. -J-verbose:gc examples/factors_algeb.rb
time jruby -J-cp ../lib/log4j.jar:../lib/junit.jar:. -J-verbose:gc examples/factors_algeb.rb
echo jruby -J-cp ../lib/log4j.jar:../lib/junit.jar:. -J-verbose:gc examples/getstart.rb
time jruby -J-cp ../lib/log4j.jar:../lib/junit.jar:. -J-verbose:gc examples/getstart.rb
echo jruby -J-cp ../lib/log4j.jar:../lib/junit.jar:. -J-verbose:gc examples/hawes2.rb
time jruby -J-cp ../lib/log4j.jar:../lib/junit.jar:. -J-verbose:gc examples/hawes2.rb
echo jruby -J-cp ../lib/log4j.jar:../lib/junit.jar:. -J-verbose:gc examples/module.rb
time jruby -J-cp ../lib/log4j.jar:../lib/junit.jar:. -J-verbose:gc examples/module.rb
echo jruby -J-cp ../lib/log4j.jar:../lib/junit.jar:. -J-verbose:gc examples/polynomial.rb
time jruby -J-cp ../lib/log4j.jar:../lib/junit.jar:. -J-verbose:gc examples/polynomial.rb
echo jruby -J-cp ../lib/log4j.jar:../lib/junit.jar:. -J-verbose:gc examples/polypower.rb
time jruby -J-cp ../lib/log4j.jar:../lib/junit.jar:. -J-verbose:gc examples/polypower.rb
echo jruby -J-cp ../lib/log4j.jar:../lib/junit.jar:. -J-verbose:gc examples/powerseries.rb
time jruby -J-cp ../lib/log4j.jar:../lib/junit.jar:. -J-verbose:gc examples/powerseries.rb
echo jruby -J-cp ../lib/log4j.jar:../lib/junit.jar:. -J-verbose:gc examples/prime-decomp.rb
time jruby -J-cp ../lib/log4j.jar:../lib/junit.jar:. -J-verbose:gc examples/prime-decomp.rb
