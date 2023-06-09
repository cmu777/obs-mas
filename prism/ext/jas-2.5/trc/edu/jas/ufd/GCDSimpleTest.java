/*
 * $Id: GCDSimpleTest.java 4831 2014-06-21 21:26:23Z axelclk $
 */

package edu.jas.ufd;


//import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.log4j.BasicConfigurator;

import edu.jas.arith.BigInteger;
import edu.jas.poly.GenPolynomial;
import edu.jas.poly.GenPolynomialRing;
import edu.jas.poly.PolyUtil;
import edu.jas.poly.TermOrder;


/**
 * GCD Simple PRS algorithm tests with JUnit.
 * @author Heinz Kredel.
 */

public class GCDSimpleTest extends TestCase {


    /**
     * main.
     */
    public static void main(String[] args) {
        BasicConfigurator.configure();
        junit.textui.TestRunner.run(suite());
    }


    /**
     * Constructs a <CODE>GCDSimpleTest</CODE> object.
     * @param name String.
     */
    public GCDSimpleTest(String name) {
        super(name);
    }


    /**
     */
    public static Test suite() {
        TestSuite suite = new TestSuite(GCDSimpleTest.class);
        return suite;
    }


    GreatestCommonDivisorAbstract<BigInteger> ufd;


    TermOrder to = new TermOrder(TermOrder.INVLEX);


    GenPolynomialRing<BigInteger> dfac;


    GenPolynomialRing<BigInteger> cfac;


    GenPolynomialRing<GenPolynomial<BigInteger>> rfac;


    BigInteger ai;


    BigInteger bi;


    BigInteger ci;


    BigInteger di;


    BigInteger ei;


    GenPolynomial<BigInteger> a;


    GenPolynomial<BigInteger> b;


    GenPolynomial<BigInteger> c;


    GenPolynomial<BigInteger> d;


    GenPolynomial<BigInteger> e;


    GenPolynomial<GenPolynomial<BigInteger>> ar;


    GenPolynomial<GenPolynomial<BigInteger>> br;


    GenPolynomial<GenPolynomial<BigInteger>> cr;


    GenPolynomial<GenPolynomial<BigInteger>> dr;


    GenPolynomial<GenPolynomial<BigInteger>> er;


    int rl = 5;


    int kl = 4;


    int ll = 5;


    int el = 3;


    float q = 0.3f;


    @Override
    protected void setUp() {
        a = b = c = d = e = null;
        ai = bi = ci = di = ei = null;
        ar = br = cr = dr = er = null;
        ufd = new GreatestCommonDivisorSimple<BigInteger>();
        dfac = new GenPolynomialRing<BigInteger>(new BigInteger(1), rl, to);
        cfac = new GenPolynomialRing<BigInteger>(new BigInteger(1), rl - 1, to);
        rfac = new GenPolynomialRing<GenPolynomial<BigInteger>>(cfac, 1, to);
    }


    @Override
    protected void tearDown() {
        a = b = c = d = e = null;
        ai = bi = ci = di = ei = null;
        ar = br = cr = dr = er = null;
        ufd = null;
        dfac = null;
        cfac = null;
        rfac = null;
    }


    /**
     * Test base gcd simple.
     */
    public void testBaseGcdSimple() {

        dfac = new GenPolynomialRing<BigInteger>(new BigInteger(1), 1, to);

        for (int i = 0; i < 5; i++) {
            a = dfac.random(kl * (i + 2), ll + 2 * i, el + 2, q);
            b = dfac.random(kl * (i + 2), ll + 2 * i, el + 2, q);
            c = dfac.random(kl * (i + 2), ll + 2, el + 2, q);
            c = c.multiply(dfac.univariate(0));
            if (c.isZERO()) {
                // skip for this turn
                continue;
            }
            //a = ufd.basePrimitivePart(a);
            //b = ufd.basePrimitivePart(b);
            c = ufd.basePrimitivePart(c).abs();

            //System.out.println("a  = " + a);
            //System.out.println("b  = " + b);
            //System.out.println("c  = " + c);

            assertTrue("length( c" + i + " ) <> 0", c.length() > 0);
            //assertTrue(" not isZERO( c"+i+" )", !c.isZERO() );
            //assertTrue(" not isONE( c"+i+" )", !c.isONE() );

            a = a.multiply(c);
            b = b.multiply(c);

            d = ufd.baseGcd(a, b);
            e = PolyUtil.<BigInteger> basePseudoRemainder(d, c);
            //System.out.println("d  = " + d);
            //System.out.println("c  = " + c);
            assertTrue("c | gcd(ac,bc) " + e, e.isZERO());

            e = PolyUtil.<BigInteger> basePseudoRemainder(a, d);
            //System.out.println("e = " + e);
            assertTrue("gcd(a,b) | a" + e, e.isZERO());

            e = PolyUtil.<BigInteger> basePseudoRemainder(b, d);
            //System.out.println("e = " + e);
            assertTrue("gcd(a,b) | b" + e, e.isZERO());
        }
    }


    /**
     * Test recursive gcd simple.
     */
    public void testRecursiveGCDSimple() {

        di = new BigInteger(1);
        dfac = new GenPolynomialRing<BigInteger>(new BigInteger(1), 2, to);
        cfac = new GenPolynomialRing<BigInteger>(new BigInteger(1), 2 - 1, to);
        rfac = new GenPolynomialRing<GenPolynomial<BigInteger>>(cfac, 1, to);

        //kl = 3; ll = 2;

        for (int i = 0; i < 3; i++) {
            ar = rfac.random(kl, ll, el + i, q);
            br = rfac.random(kl, ll, el, q);
            cr = rfac.random(kl, ll, el, q);
            cr = ufd.recursivePrimitivePart(cr).abs();
            //System.out.println("ar = " + ar);
            //System.out.println("br = " + br);
            //System.out.println("cr = " + cr);

            if (ar.isZERO() || br.isZERO() || cr.isZERO()) {
                // skip for this turn
                continue;
            }
            assertTrue("length( cr" + i + " ) <> 0", cr.length() > 0);
            //assertTrue(" not isZERO( c"+i+" )", !c.isZERO() );
            //assertTrue(" not isONE( c"+i+" )", !c.isONE() );

            ar = ar.multiply(cr);
            br = br.multiply(cr);
            //System.out.println("ar = " + ar);
            //System.out.println("br = " + br);

            dr = ufd.recursiveUnivariateGcd(ar, br);
            //System.out.println("cr = " + cr);
            //System.out.println("dr = " + dr);

            er = PolyUtil.<BigInteger> recursivePseudoRemainder(dr, cr);
            //System.out.println("er = " + er);
            assertTrue("c | gcd(ac,bc) " + er, er.isZERO());

            er = PolyUtil.<BigInteger> recursivePseudoRemainder(ar, dr);
            //System.out.println("er = " + er);
            assertTrue("gcd(a,b) | a" + er, er.isZERO());

            er = PolyUtil.<BigInteger> recursivePseudoRemainder(br, dr);
            //System.out.println("er = " + er);
            assertTrue("gcd(a,b) | b" + er, er.isZERO());
        }
    }


    /**
     * Test arbitrary recursive gcd simple.
     */
    public void testArbitraryRecursiveGCDSimple() {

        di = new BigInteger(1);
        dfac = new GenPolynomialRing<BigInteger>(new BigInteger(1), 2, to);
        cfac = new GenPolynomialRing<BigInteger>(new BigInteger(1), 2 - 1, to);
        rfac = new GenPolynomialRing<GenPolynomial<BigInteger>>(cfac, 1, to);

        //kl = 3; ll = 2;

        for (int i = 0; i < 3; i++) {
            ar = rfac.random(kl, ll, el + i, q);
            br = rfac.random(kl, ll, el, q);
            cr = rfac.random(kl, ll, el, q);
            cr = ufd.recursivePrimitivePart(cr).abs();
            //System.out.println("ar = " + ar);
            //System.out.println("br = " + br);
            //System.out.println("cr = " + cr);

            if (ar.isZERO() || br.isZERO() || cr.isZERO()) {
                // skip for this turn
                continue;
            }
            assertTrue("length( cr" + i + " ) <> 0", cr.length() > 0);
            //assertTrue(" not isZERO( c"+i+" )", !c.isZERO() );
            //assertTrue(" not isONE( c"+i+" )", !c.isONE() );

            ar = ar.multiply(cr);
            br = br.multiply(cr);
            //System.out.println("ar = " + ar);
            //System.out.println("br = " + br);

            dr = ufd.recursiveGcd(ar, br);
            //System.out.println("cr = " + cr);
            //System.out.println("dr = " + dr);

            er = PolyUtil.<BigInteger> recursivePseudoRemainder(dr, cr);
            //System.out.println("er = " + er);
            assertTrue("c | gcd(ac,bc) " + er, er.isZERO());

            er = PolyUtil.<BigInteger> recursivePseudoRemainder(ar, dr);
            //System.out.println("er = " + er);
            assertTrue("gcd(a,b) | a" + er, er.isZERO());

            er = PolyUtil.<BigInteger> recursivePseudoRemainder(br, dr);
            //System.out.println("er = " + er);
            assertTrue("gcd(a,b) | b" + er, er.isZERO());
        }
    }


    /**
     * Test gcd simple.
     */
    public void testGCDSimple() {

        dfac = new GenPolynomialRing<BigInteger>(new BigInteger(1), 4, to);

        for (int i = 0; i < 2; i++) {
            a = dfac.random(kl, ll, el, q);
            b = dfac.random(kl, ll, el, q);
            c = dfac.random(kl, ll, el, q);
            c = c.multiply(dfac.univariate(0));
            c = ufd.primitivePart(c).abs();
            //System.out.println("a = " + a);
            //System.out.println("b = " + b);
            //System.out.println("c = " + c);

            if (a.isZERO() || b.isZERO() || c.isZERO()) {
                // skip for this turn
                continue;
            }
            assertTrue("length( c" + i + " ) <> 0", c.length() > 0);
            //assertTrue(" not isZERO( c"+i+" )", !c.isZERO() );
            //assertTrue(" not isONE( c"+i+" )", !c.isONE() );

            a = a.multiply(c);
            b = b.multiply(c);
            //System.out.println("a = " + a);
            //System.out.println("b = " + b);
            //System.out.println("c = " + c);

            d = ufd.gcd(a, b);
            //System.out.println("c = " + c);
            //System.out.println("d = " + d);

            e = PolyUtil.<BigInteger> basePseudoRemainder(d, c);
            //System.out.println("e = " + e);
            assertTrue("c | gcd(ac,bc) " + e, e.isZERO());

            e = PolyUtil.<BigInteger> basePseudoRemainder(a, d);
            //System.out.println("e = " + e);
            assertTrue("gcd(a,b) | a " + e, e.isZERO());

            e = PolyUtil.<BigInteger> basePseudoRemainder(b, d);
            //System.out.println("e = " + e);
            assertTrue("gcd(a,b) | b " + e, e.isZERO());
        }
    }


    /**
     * Test base resultant integral coefficients.
     */
    public void testBaseResultant() {

        dfac = new GenPolynomialRing<BigInteger>(new BigInteger(1), 1, to);

        GreatestCommonDivisorSimple<BigInteger> ufds = new GreatestCommonDivisorSimple<BigInteger>();
        GreatestCommonDivisorSubres<BigInteger> sres = new GreatestCommonDivisorSubres<BigInteger>();

        for (int i = 0; i < 1; i++) {
            a = dfac.random(kl, ll, el + 3 + i, q);
            b = dfac.random(kl, ll, el + 3 + i, q);
            c = dfac.random(kl, ll, el + 3 + i, q);
            //System.out.println("a = " + a);
            //System.out.println("b = " + b);
            //System.out.println("c = " + c);

            if (a.isZERO() || b.isZERO() || c.isZERO()) {
                // skip for this turn
                continue;
            }
            if (c.isConstant()) {
                c = dfac.univariate(0,1);
            }
            assertTrue("length( c" + i + " ) <> 0", c.length() > 0);

            d = ufds.baseResultant(a, b);
            //System.out.println("d = " + d);
            e = sres.baseResultant(a, b);
            //System.out.println("e = " + e);
            assertEquals("d == e: " + d.subtract(e), d.abs().signum(), e.abs().signum() );
            //assertEquals("d == e: " + d.subtract(e), d, e);

            GenPolynomial<BigInteger> ac = a.multiply(c);
            GenPolynomial<BigInteger> bc = b.multiply(c);
            //System.out.println("ac = " + ac);
            //System.out.println("bc = " + bc);

            d = ufds.baseResultant(ac, bc);
            //System.out.println("d = " + d);
            assertTrue("d == 0: " + d, d.isZERO());

            e = sres.baseResultant(ac, bc);
            //System.out.println("e = " + e);
            assertTrue("e == 0: " + e, e.isZERO());
        }
    }


    /**
     * Test recursive resultant simple.
     */
    public void testRecursiveResultantSimple() {

        di = new BigInteger(1);
        dfac = new GenPolynomialRing<BigInteger>(new BigInteger(1), 2, to);
        cfac = new GenPolynomialRing<BigInteger>(new BigInteger(1), 2 - 1, to);
        rfac = new GenPolynomialRing<GenPolynomial<BigInteger>>(cfac, 1, to);

        GreatestCommonDivisorSimple<BigInteger> ufds = new GreatestCommonDivisorSimple<BigInteger>();
        GreatestCommonDivisorSubres<BigInteger> sres = new GreatestCommonDivisorSubres<BigInteger>();

        //kl = 3; ll = 2;

        for (int i = 0; i < 1; i++) {
            ar = rfac.random(kl, ll, el + i, q);
            br = rfac.random(kl, ll, el, q);
            cr = rfac.random(kl, ll, el, q);
            cr = ufd.recursivePrimitivePart(cr).abs();
            //System.out.println("ar = " + ar);
            //System.out.println("br = " + br);

            if (ar.isZERO() || br.isZERO() || cr.isZERO()) {
                // skip for this turn
                continue;
            }
            if (cr.isConstant()) {
                cr = rfac.univariate(0,1);
            }
            //System.out.println("cr = " + cr);
            assertTrue("length( cr" + i + " ) <> 0", cr.length() > 0);

            dr = ufds.recursiveUnivariateResultant(ar, br);
            //System.out.println("dr = " + dr);
            er = sres.recursiveUnivariateResultant(ar, br);
            //System.out.println("er = " + er);
            assertEquals("dr == er: " + dr.subtract(er), dr.abs().signum(), er.abs().signum());
            //assertEquals("dr == er: " + dr.subtract(er), dr, er);

            GenPolynomial<GenPolynomial<BigInteger>> arc = ar.multiply(cr);
            GenPolynomial<GenPolynomial<BigInteger>> brc = br.multiply(cr);
            //System.out.println("ar = " + ar);
            //System.out.println("br = " + br);

            dr = ufds.recursiveUnivariateResultant(arc, brc);
            //System.out.println("dr = " + dr);
            //assertTrue("dr == 0: " + dr, dr.isZERO());

            er = sres.recursiveUnivariateResultant(arc, brc);
            //System.out.println("er = " + er);
            //assertTrue("er == 0: " + er, er.isZERO());

            assertEquals("dr == er: " + dr.subtract(er), dr.signum(), er.signum());
        }
    }

}
