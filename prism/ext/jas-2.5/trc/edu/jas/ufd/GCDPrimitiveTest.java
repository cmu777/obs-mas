/*
 * $Id: GCDPrimitiveTest.java 3789 2011-10-01 18:54:43Z kredel $
 */

package edu.jas.ufd;


import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.BasicConfigurator;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import edu.jas.arith.BigInteger;
import edu.jas.arith.BigRational;
import edu.jas.poly.ExpVector;
import edu.jas.poly.GenPolynomial;
import edu.jas.poly.GenPolynomialRing;
import edu.jas.poly.PolyUtil;
import edu.jas.poly.TermOrder;


/**
 * GCD Primitive PRS algorithm tests with JUnit.
 * @author Heinz Kredel.
 */

public class GCDPrimitiveTest extends TestCase {


    /**
     * main.
     */
    public static void main(String[] args) {
        BasicConfigurator.configure();
        junit.textui.TestRunner.run(suite());
    }


    /**
     * Constructs a <CODE>GCDPrimitiveTest</CODE> object.
     * @param name String.
     */
    public GCDPrimitiveTest(String name) {
        super(name);
    }


    /**
     */
    public static Test suite() {
        TestSuite suite = new TestSuite(GCDPrimitiveTest.class);
        return suite;
    }


    //private final static int bitlen = 100;

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
        ufd = new GreatestCommonDivisorPrimitive<BigInteger>();
        String[] vars = ExpVector.STDVARS(rl);
        String[] cvars = ExpVector.STDVARS(rl - 1);
        String[] rvars = new String[] { vars[rl - 1] };
        dfac = new GenPolynomialRing<BigInteger>(new BigInteger(1), rl, to, vars);
        cfac = new GenPolynomialRing<BigInteger>(new BigInteger(1), rl - 1, to, cvars);
        rfac = new GenPolynomialRing<GenPolynomial<BigInteger>>(cfac, 1, to, rvars);
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
     * Test base quotioent and remainder.
     * 
     */
    public void testBaseQR() {
        di = new BigInteger(1);
        dfac = new GenPolynomialRing<BigInteger>(new BigInteger(1), 1, to);

        for (int i = 0; i < 5; i++) {
            a = dfac.random(kl * (i + 2), ll + 2 * i, el + 2 * i, q);
            c = dfac.random(kl * (i + 2), ll + 2 * i, el + 2 * i, q);
            //a = ufd.basePrimitivePart(a).abs();
            //c = ufd.basePrimitivePart(c);
            ci = di.random(kl * (i + 2));
            ci = ci.sum(di.getONE());

            //System.out.println("a  = " + a);
            //System.out.println("c  = " + c);
            //System.out.println("ci = " + ci);

            if (a.isZERO() || c.isZERO() || ci.isZERO()) {
                // skip for this turn
                continue;
            }
            assertTrue("length( c" + i + " ) <> 0", c.length() > 0);
            //assertTrue(" not isZERO( c"+i+" )", !c.isZERO() );
            //assertTrue(" not isONE( c"+i+" )", !c.isONE() );

            b = a.multiply(c);
            //System.out.println("b  = " + b);
            d = PolyUtil.<BigInteger> basePseudoRemainder(b, c);
            //System.out.println("d  = " + d);

            assertTrue("rem(ac,c) == 0", d.isZERO());

            b = a.multiply(ci);
            //System.out.println("b  = " + b);
            d = b.divide(ci);
            //System.out.println("d  = " + d);

            assertEquals("a == ac/c", a, d);

            b = a.multiply(c);
            //System.out.println("b  = " + b);
            d = PolyUtil.<BigInteger> basePseudoDivide(b, c);
            //System.out.println("d  = " + d);

            assertEquals("a == ac/c", a, d);
        }
    }


    /**
     * Test base content and primitive part.
     * 
     */
    public void testBaseContentPP() {
        di = new BigInteger(1);

        for (int i = 0; i < 13; i++) {
            c = dfac.random(kl * (i + 2), ll + 2 * i, el + i, q);
            c = c.multiply(di.random(kl * (i + 2)));

            if (c.isZERO()) {
                // skip for this turn
                continue;
            }
            assertTrue("length( c" + i + " ) <> 0", c.length() > 0);
            //assertTrue(" not isZERO( c"+i+" )", !c.isZERO() );
            //assertTrue(" not isONE( c"+i+" )", !c.isONE() );

            ci = ufd.baseContent(c);
            d = ufd.basePrimitivePart(c);
            //System.out.println("c  = " + c);
            //System.out.println("ci = " + ci);
            //System.out.println("d  = " + d);

            a = d.multiply(ci);
            assertEquals("c == cont(c)pp(c)", c, a);
        }
    }


    /**
     * Test base gcd.
     * 
     */
    public void testBaseGcd() {

        dfac = new GenPolynomialRing<BigInteger>(new BigInteger(1), 1, to);

        for (int i = 0; i < 5; i++) {
            a = dfac.random(kl * (i + 2), ll + 2 * i, el + 2 * i, q);
            b = dfac.random(kl * (i + 2), ll + 2 * i, el + 2 * i, q);
            c = dfac.random(kl * (i + 2), ll + 2 * i, el + 2 * i, q);
            //a = ufd.basePrimitivePart(a);
            //b = ufd.basePrimitivePart(b);
            //c = ufd.basePrimitivePart(c).abs();

            //System.out.println("a  = " + a);
            //System.out.println("b  = " + b);
            //System.out.println("c  = " + c);

            if (a.isZERO() || b.isZERO() || c.isZERO()) {
                // skip for this turn
                continue;
            }
            assertTrue("length( c" + i + " ) <> 0", c.length() > 0);
            //assertTrue(" not isZERO( c"+i+" )", !c.isZERO() );
            //assertTrue(" not isONE( c"+i+" )", !c.isONE() );

            a = a.multiply(c);
            b = b.multiply(c);

            d = ufd.baseGcd(a, b);
            e = PolyUtil.<BigInteger> basePseudoRemainder(d, c);
            //System.out.println("d  = " + d);

            assertTrue("c | gcd(ac,bc) " + e, e.isZERO());
        }
    }


    /**
     * Test recursive quotioent and remainder.
     * 
     */
    public void testRecursiveQR() {
        di = new BigInteger(1);
        dfac = new GenPolynomialRing<BigInteger>(new BigInteger(1), 2, to);
        cfac = new GenPolynomialRing<BigInteger>(new BigInteger(1), 2 - 1, to);
        rfac = new GenPolynomialRing<GenPolynomial<BigInteger>>(cfac, 1, to);

        for (int i = 0; i < 5; i++) {
            a = dfac.random(kl * (i + 1), ll + i, el + i, q);
            a = ufd.basePrimitivePart(a).abs();

            c = dfac.random(kl * (i + 1), ll + i, el + i, q);
            c = ufd.basePrimitivePart(a).abs();
            cr = PolyUtil.<BigInteger> recursive(rfac, c);

            c = cfac.random(kl * (i + 1), ll + 2 * i, el + 2 * i, q);
            c = ufd.basePrimitivePart(c).abs();

            ar = PolyUtil.<BigInteger> recursive(rfac, a);
            //System.out.println("ar = " + ar);
            //System.out.println("a  = " + a);
            //System.out.println("c  = " + c);
            //System.out.println("cr = " + cr);

            if (cr.isZERO() || c.isZERO()) {
                // skip for this turn
                continue;
            }
            assertTrue("length( cr" + i + " ) <> 0", cr.length() > 0);
            //assertTrue(" not isZERO( c"+i+" )", !c.isZERO() );
            //assertTrue(" not isONE( c"+i+" )", !c.isONE() );


            br = ar.multiply(cr);
            //System.out.println("br = " + br);
            dr = PolyUtil.<BigInteger> recursivePseudoRemainder(br, cr);
            //System.out.println("dr = " + dr);
            d = PolyUtil.<BigInteger> distribute(dfac, dr);
            //System.out.println("d  = " + d);

            assertTrue("rem(ac,c) == 0", d.isZERO());

            br = ar.multiply(c);
            //System.out.println("br = " + br);
            dr = PolyUtil.<BigInteger> recursiveDivide(br, c);
            //System.out.println("dr = " + dr);
            d = PolyUtil.<BigInteger> distribute(dfac, dr);
            //System.out.println("d  = " + d);

            assertEquals("a == ac/c", a, d);
        }
    }


    /**
     * Test recursive content and primitive part.
     * 
     */
    public void testRecursiveContentPP() {
        di = new BigInteger(1);
        dfac = new GenPolynomialRing<BigInteger>(new BigInteger(1), 2, to);
        cfac = new GenPolynomialRing<BigInteger>(new BigInteger(1), 2 - 1, to);
        rfac = new GenPolynomialRing<GenPolynomial<BigInteger>>(cfac, 1, to);

        for (int i = 0; i < 3; i++) {
            cr = rfac.random(kl * (i + 2), ll + 2 * i, el + i, q);
            //System.out.println("cr = " + cr);

            assertTrue("length( cr" + i + " ) <> 0", cr.length() > 0);
            //assertTrue(" not isZERO( c"+i+" )", !c.isZERO() );
            //assertTrue(" not isONE( c"+i+" )", !c.isONE() );

            c = ufd.recursiveContent(cr);
            dr = ufd.recursivePrimitivePart(cr);
            //System.out.println("c  = " + c);
            //System.out.println("dr = " + dr);

            ar = dr.multiply(c);
            assertEquals("c == cont(c)pp(c)", cr, ar);
        }
    }


    /**
     * Test recursive gcd.
     * 
     */
    public void testRecursiveGCD() {
        di = new BigInteger(1);
        dfac = new GenPolynomialRing<BigInteger>(new BigInteger(1), 2, to);
        cfac = new GenPolynomialRing<BigInteger>(new BigInteger(1), 2 - 1, to);
        rfac = new GenPolynomialRing<GenPolynomial<BigInteger>>(cfac, 1, to);

        for (int i = 0; i < 2; i++) {
            ar = rfac.random(kl, ll, el + i, q);
            br = rfac.random(kl, ll, el, q);
            cr = rfac.random(kl, ll, el, q);
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
            //System.out.println("dr = " + dr);

            er = PolyUtil.<BigInteger> recursivePseudoRemainder(dr, cr);
            //System.out.println("er = " + er);

            assertTrue("c | gcd(ac,bc) " + er, er.isZERO());
        }
    }


    /**
     * Test arbitrary recursive gcd.
     * 
     */
    public void testArbitraryRecursiveGCD() {
        di = new BigInteger(1);
        dfac = new GenPolynomialRing<BigInteger>(new BigInteger(1), 2, to);
        cfac = new GenPolynomialRing<BigInteger>(new BigInteger(1), 2 - 1, to);
        rfac = new GenPolynomialRing<GenPolynomial<BigInteger>>(cfac, 1, to);

        for (int i = 0; i < 2; i++) {
            ar = rfac.random(kl, ll, el + i, q);
            br = rfac.random(kl, ll, el, q);
            cr = rfac.random(kl, ll, el, q);
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
            //System.out.println("dr = " + dr);

            er = PolyUtil.<BigInteger> recursivePseudoRemainder(dr, cr);
            //System.out.println("er = " + er);

            assertTrue("c | gcd(ac,bc) " + er, er.isZERO());
        }
    }


    /**
     * Test content and primitive part.
     * 
     */
    public void testContentPP() {
        dfac = new GenPolynomialRing<BigInteger>(new BigInteger(1), 3, to);

        for (int i = 0; i < 3; i++) {
            c = dfac.random(kl * (i + 2), ll + 2 * i, el + i, q);
            //System.out.println("cr = " + cr);

            assertTrue("length( c" + i + " ) <> 0", c.length() > 0);
            //assertTrue(" not isZERO( c"+i+" )", !c.isZERO() );
            //assertTrue(" not isONE( c"+i+" )", !c.isONE() );

            a = ufd.content(c);
            e = a.extend(dfac, 0, 0L);
            b = ufd.primitivePart(c);
            //System.out.println("c  = " + c);
            //System.out.println("a  = " + a);
            //System.out.println("e  = " + e);
            //System.out.println("b  = " + b);

            d = e.multiply(b);
            assertEquals("c == cont(c)pp(c)", d, c);
        }
    }


    /**
     * Test gcd 3 variables.
     * 
     */
    public void testGCD3() {
        dfac = new GenPolynomialRing<BigInteger>(new BigInteger(1), 3, to);

        for (int i = 0; i < 4; i++) {
            a = dfac.random(kl, ll, el + i, q);
            b = dfac.random(kl, ll, el, q);
            c = dfac.random(kl, ll, el, q);
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

            d = ufd.gcd(a, b);
            //System.out.println("d = " + d);

            e = PolyUtil.<BigInteger> basePseudoRemainder(d, c);
            //System.out.println("e = " + e);

            assertTrue("c | gcd(ac,bc) " + e, e.isZERO());
        }
    }


    /**
     * Test gcd.
     * 
     */
    public void testGCD() {
        // dfac = new GenPolynomialRing<BigInteger>(new BigInteger(1),3,to);

        for (int i = 0; i < 1; i++) {
            a = dfac.random(kl, ll, el, q);
            b = dfac.random(kl, ll, el, q);
            c = dfac.random(kl, ll, el, q);
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

            d = ufd.gcd(a, b);
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
     * Test gcd field coefficients.
     * 
     */
    public void testGCDfield() {
        GenPolynomialRing<BigRational> dfac;
        dfac = new GenPolynomialRing<BigRational>(new BigRational(1), 3, to);

        GreatestCommonDivisorAbstract<BigRational> ufd = new GreatestCommonDivisorPrimitive<BigRational>();

        GenPolynomial<BigRational> a, b, c, d, e;

        for (int i = 0; i < 1; i++) {
            a = dfac.random(kl, ll, el, q);
            b = dfac.random(kl, ll, el, q);
            c = dfac.random(kl, ll, el, q);
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

            d = ufd.gcd(a, b);
            //System.out.println("d = " + d);

            e = PolyUtil.<BigRational> basePseudoRemainder(d, c);
            //System.out.println("e = " + e);

            assertTrue("c | gcd(ac,bc) " + e, e.isZERO());
        }
    }


    /**
     * Test lcm.
     * 
     */
    public void testLCM() {
        dfac = new GenPolynomialRing<BigInteger>(new BigInteger(1), 3, to);

        for (int i = 0; i < 1; i++) {
            a = dfac.random(kl, ll, el, q);
            b = dfac.random(kl, ll, el, q);
            c = dfac.random(kl, 3, 2, q);
            //System.out.println("a = " + a);
            //System.out.println("b = " + b);
            //System.out.println("c = " + c);

            if (a.isZERO() || b.isZERO() || c.isZERO()) {
                // skip for this turn
                continue;
            }
            assertTrue("length( a" + i + " ) <> 0", a.length() > 0);
            //assertTrue(" not isZERO( c"+i+" )", !c.isZERO() );
            //assertTrue(" not isONE( c"+i+" )", !c.isONE() );

            a = a.multiply(c);
            b = b.multiply(c);

            c = ufd.gcd(a, b);
            //System.out.println("c = " + c);

            d = ufd.lcm(a, b);
            //System.out.println("d = " + d);

            e = c.multiply(d);
            //System.out.println("e = " + e);
            c = a.multiply(b);
            //System.out.println("c = " + c);

            assertEquals("ab == gcd(a,b)lcm(ab)", c, e);
        }
    }


    /**
     * Test co-prime factors.
     * 
     */
    public void testCoPrime() {

        dfac = new GenPolynomialRing<BigInteger>(new BigInteger(1), 3, to);

        a = dfac.random(kl, 3, 2, q);
        b = dfac.random(kl, 3, 2, q);
        c = dfac.random(kl, 3, 2, q);
        //System.out.println("a  = " + a);
        //System.out.println("b  = " + b);
        //System.out.println("c  = " + c);

        if (a.isZERO() || b.isZERO() || c.isZERO()) {
            // skip for this turn
            return;
        }
        assertTrue("length( a ) <> 0", a.length() > 0);

        d = a.multiply(a).multiply(b).multiply(b).multiply(b).multiply(c);
        e = a.multiply(b).multiply(c);
        //System.out.println("d  = " + d);
        //System.out.println("c  = " + c);

        List<GenPolynomial<BigInteger>> F = new ArrayList<GenPolynomial<BigInteger>>(5);
        F.add(d);
        F.add(a);
        F.add(b);
        F.add(c);
        F.add(e);


        List<GenPolynomial<BigInteger>> P = ufd.coPrime(F);
        //System.out.println("F = " + F);
        //System.out.println("P = " + P);

        assertTrue("is co-prime ", ufd.isCoPrime(P));
        assertTrue("is co-prime of ", ufd.isCoPrime(P, F));


        //P = ufd.coPrimeSquarefree(F);
        //System.out.println("F = " + F);
        //System.out.println("P = " + P);
        //assertTrue("is co-prime ", ufd.isCoPrime(P) );
        //assertTrue("is co-prime of ", ufd.isCoPrime(P,F) );


        P = ufd.coPrimeRec(F);
        //System.out.println("F = " + F);
        //System.out.println("P = " + P);

        assertTrue("is co-prime ", ufd.isCoPrime(P));
        assertTrue("is co-prime of ", ufd.isCoPrime(P, F));
    }

}
