/*
 * $Id: GenMatrixTest.java 4125 2012-08-19 19:05:22Z kredel $
 */

package edu.jas.vector;


import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import edu.jas.arith.BigRational;


/**
 * GenMatrix tests with JUnit
 * @author Heinz Kredel.
 */

public class GenMatrixTest extends TestCase {


    /**
     * main.
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }


    /**
     * Constructs a <CODE>GenMatrixTest</CODE> object.
     * @param name String.
     */
    public GenMatrixTest(String name) {
        super(name);
    }


    /**
     */
    public static Test suite() {
        TestSuite suite = new TestSuite(GenMatrixTest.class);
        return suite;
    }


    int rl = 5;


    int kl = 10;


    int ll = 10;


    float q = 0.5f;


    int rows = 3;


    int cols = 3;


    @Override
    protected void setUp() {
    }


    @Override
    protected void tearDown() {
    }


    /**
     * Test constructor and toString.
     */
    public void testConstruction() {
        BigRational cfac = new BigRational(1);
        GenMatrixRing<BigRational> mfac = new GenMatrixRing<BigRational>(cfac, rows, cols);

        assertTrue("#rows = " + rows, mfac.rows == rows);
        assertTrue("#columns = " + cols, mfac.cols == cols);
        assertTrue("cfac == coFac ", cfac == mfac.coFac);

        GenMatrix<BigRational> a;
        a = mfac.getZERO();
        //System.out.println("a = " + a);
        assertTrue("isZERO( a )", a.isZERO());

        GenMatrix<BigRational> b = new GenMatrix<BigRational>(mfac);
        //System.out.println("b = " + b);
        assertTrue("isZERO( b )", b.isZERO());

        assertTrue("a == b ", a.equals(b));

        GenMatrix<BigRational> c = b.copy();
        //System.out.println("c = " + c);
        assertTrue("isZERO( c )", c.isZERO());
        assertTrue("a == c ", a.equals(c));

        GenMatrix<BigRational> d = mfac.copy(b);
        //System.out.println("d = " + d);
        assertTrue("isZERO( d )", d.isZERO());
        assertTrue("a == d ", a.equals(d));

        a = mfac.getONE();
        //System.out.println("a = " + a);
        assertTrue("isONE( a )", a.isONE());

        List<ArrayList<BigRational>> m = a.matrix;
        List<List<BigRational>> ml = new ArrayList<List<BigRational>>(m.size());
        for (ArrayList<BigRational> r : m) {
            ml.add(r);
        }
        b = mfac.fromList(ml);
        assertEquals("a == fromList(a.matrix)", a, b);
    }


    /**
     * Test random matrix.
     */
    public void testRandom() {
        BigRational cfac = new BigRational(1);
        GenMatrixRing<BigRational> mfac = new GenMatrixRing<BigRational>(cfac, rows, cols);
        GenMatrixRing<BigRational> tfac = mfac.transpose();

        if (rows == cols) {
            assertTrue(" mfac = tfac ", mfac.equals(tfac));
        }

        GenMatrix<BigRational> a, b, c;

        for (int i = 0; i < 5; i++) {
            a = mfac.random(kl, q);
            //System.out.println("a = " + a);
            if (a.isZERO()) {
                continue;
            }
            assertTrue(" not isZERO( a" + i + " )", !a.isZERO());
            b = a.transpose(tfac);
            //System.out.println("b = " + b);
            assertTrue(" not isZERO( b" + i + " )", !b.isZERO());
            c = b.transpose(mfac);
            //System.out.println("c = " + c);
            assertEquals(" a^r^r == a ", a, c);
        }
    }


    /**
     * Test addition.
     */
    public void testAddition() {
        BigRational cfac = new BigRational(1);
        GenMatrixRing<BigRational> mfac = new GenMatrixRing<BigRational>(cfac, rows, cols);
        GenMatrix<BigRational> a, b, c, d, e;

        a = mfac.random(kl, q);
        b = mfac.random(kl, q);
        //System.out.println("a = " + a);
        //System.out.println("b = " + b);

        c = a.sum(b);
        d = c.subtract(b);
        //System.out.println("c = " + c);
        //System.out.println("d = " + d);
        assertEquals("a+b-b = a", a, d);

        c = a.sum(b);
        d = c.sum(b.negate());
        //System.out.println("c = " + c);
        //System.out.println("d = " + d);
        assertEquals("a+b+(-b) = a", a, d);

        c = a.sum(b);
        d = b.sum(a);
        //System.out.println("c = " + c);
        //System.out.println("d = " + d);
        assertEquals("a+b = b+a", c, d);

        c = mfac.random(kl, q);
        d = a.sum(b).sum(c);
        e = a.sum(b.sum(c));
        //System.out.println("d = " + d);
        //System.out.println("e = " + e);
        assertEquals("a+(b+c) = (a+b)+c", d, e);
    }


    /**
     * Test scalar multiplication.
     */
    public void testScalarMultiplication() {
        BigRational cfac = new BigRational(1);
        GenMatrixRing<BigRational> mfac = new GenMatrixRing<BigRational>(cfac, rows, cols);
        BigRational r, s, t;
        GenMatrix<BigRational> a, b, c, d;

        r = cfac.random(kl);
        //System.out.println("r = " + r);
        s = r.inverse();
        //System.out.println("s = " + s);

        a = mfac.random(kl, q);
        //System.out.println("a = " + a);

        c = a.scalarMultiply(r);
        d = c.scalarMultiply(s);
        //System.out.println("c = " + c);
        //System.out.println("d = " + d);
        assertEquals("a*b*(1/b) = a", a, d);

        b = mfac.random(kl, q);
        //System.out.println("b = " + b);

        t = cfac.getONE();
        //System.out.println("t = " + t);
        c = a.linearCombination(b, t);
        d = b.linearCombination(a, t);
        //System.out.println("c = " + c);
        //System.out.println("d = " + d);
        assertEquals("a+1*b = b+1*a", c, d);

        c = a.linearCombination(b, t);
        d = a.sum(b);
        //System.out.println("c = " + c);
        //System.out.println("d = " + d);
        assertEquals("a+1*b = b+1*a", c, d);

        s = t.negate();
        //System.out.println("s = " + s);
        c = a.linearCombination(b, t);
        d = c.linearCombination(b, s);
        //System.out.println("c = " + c);
        //System.out.println("d = " + d);
        assertEquals("a+1*b+(-1)*b = a", a, d);

        c = a.linearCombination(t, b, t);
        d = c.linearCombination(t, b, s);
        //System.out.println("c = " + c);
        //System.out.println("d = " + d);
        assertEquals("a*1+b*1+b*(-1) = a", a, d);

        t = cfac.getZERO();
        //System.out.println("t = " + t);
        c = a.linearCombination(b, t);
        //System.out.println("c = " + c);
        assertEquals("a+0*b = a", a, c);

        d = a.linearCombination(t, b, t);
        //System.out.println("d = " + d);
        assertEquals("0*a+0*b = 0", mfac.getZERO(), d);
    }


    /**
     * Test (simple) multiplication.
     */
    public void testSimpleMultiplication() {
        BigRational cfac = new BigRational(1);
        GenMatrixRing<BigRational> mfac = new GenMatrixRing<BigRational>(cfac, rows, cols);
        GenMatrix<BigRational> a, b, c, d, e, f;

        a = mfac.getZERO();
        b = mfac.getZERO();
        c = a.multiplySimple(b);
        //System.out.println("a = " + a);
        //System.out.println("b = " + b);
        //System.out.println("c = " + c);
        assertTrue("0*0 = 0 ", c.isZERO());

        a = mfac.getONE();
        b = mfac.getONE();
        c = a.multiplySimple(b);
        //System.out.println("a = " + a);
        //System.out.println("b = " + b);
        //System.out.println("c = " + c);
        assertTrue("1*1 = 1 ", c.isONE());

        a = mfac.random(kl, q);
        b = mfac.getONE();
        c = a.multiplySimple(b);
        d = a.multiply(b);
        //System.out.println("a = " + a);
        //System.out.println("b = " + b);
        //System.out.println("c = " + c);
        //System.out.println("d = " + d);
        assertEquals("a*1 = a ", a, c);
        assertEquals("a*1 = a*1 ", c, d);

        c = b.multiplySimple(a);
        d = a.multiply(b);
        //System.out.println("a = " + a);
        //System.out.println("b = " + b);
        //System.out.println("c = " + c);
        //System.out.println("d = " + d);
        assertEquals("1*a = a ", a, c);
        assertEquals("a*1 = a*1 ", c, d);

        b = mfac.random(kl, q);
        long s, t;
        s = System.currentTimeMillis();
        c = a.multiplySimple(b);
        s = System.currentTimeMillis() - s;
        assertTrue("nonsense " + s, s >= 0L);
        d = b.multiplySimple(a);
        t = System.currentTimeMillis();
        e = a.multiply(b);
        t = System.currentTimeMillis() - t;
        assertTrue("nonsense " + t, t >= 0L);
        f = b.multiply(a);
        //System.out.println("a = " + a);
        //System.out.println("b = " + b);
        //System.out.println("c = " + c);
        //System.out.println("d = " + d);
        //System.out.println("e = " + e);
        //System.out.println("f = " + e);
        //System.out.println("e = " + e);
        assertTrue("a*b != b*a ", !c.equals(d));
        assertEquals("a*1 = a*1 ", c, e);
        assertEquals("a*1 = a*1 ", d, f);
        //System.out.println("time: s = " + s + ", t = " + t);

        if (!mfac.isAssociative()) {
            return;
        }
        c = mfac.random(kl, q);

        d = a.multiply(b.sum(c));
        e = (a.multiply(b)).sum(a.multiply(c));
        assertEquals("a*(b+c) = a*b+a*c", d, e);

        d = a.multiply(b.multiply(c));
        e = (a.multiply(b)).multiply(c);
        assertEquals("a*(b*c) = (a*b)*c", d, e);
    }


    /**
     * Test parse matrix.
     */
    public void testParse() {
        BigRational cfac = new BigRational(1);
        GenMatrixRing<BigRational> mfac = new GenMatrixRing<BigRational>(cfac, rows, cols);

        GenMatrix<BigRational> a, c;

        a = mfac.random(kl, q);
        //System.out.println("a = " + a);
        if (!a.isZERO()) {
            //return;
            assertTrue(" not isZERO( a )", !a.isZERO());
        }
        String s = a.toString();
        //System.out.println("s = " + s);
        c = mfac.parse(s);
        //System.out.println("c = " + c);
        assertEquals("parse(toStirng(a) == a ", a, c);
    }

}
