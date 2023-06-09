/*
 * $Id: GenPolynomialTokenizerTest.java 4516 2013-07-27 07:56:05Z kredel $
 */

package edu.jas.poly;


import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import java.util.ArrayList;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.log4j.BasicConfigurator;

import edu.jas.arith.BigComplex;
import edu.jas.arith.BigDecimal;
import edu.jas.arith.BigInteger;
import edu.jas.arith.BigQuaternion;
import edu.jas.arith.BigRational;
import edu.jas.arith.ModInteger;
import edu.jas.arith.ModLong;
import edu.jas.arith.ModLongRing;
import edu.jas.structure.RingFactory;


/**
 * GenPolynomialTokenizer tests with JUnit.
 * @author Heinz Kredel
 */

public class GenPolynomialTokenizerTest extends TestCase {


    /**
     * main.
     */
    public static void main(String[] args) {
        BasicConfigurator.configure();
        junit.textui.TestRunner.run(suite());
    }


    /**
     * Constructs a <CODE>GenPolynomialTokenizerTest</CODE> object.
     * @param name String.
     */
    public GenPolynomialTokenizerTest(String name) {
        super(name);
    }


    /**
     * suite.
     */
    public static Test suite() {
        TestSuite suite = new TestSuite(GenPolynomialTokenizerTest.class);
        return suite;
    }


    RingFactory fac; // unused


    GenPolynomialRing pfac;


    GenSolvablePolynomialRing spfac;


    GenPolynomialTokenizer parser;


    Reader source;


    @Override
    protected void setUp() {
        fac = null;
        pfac = null;
        parser = null;
        source = null;
    }


    @Override
    protected void tearDown() {
        fac = null;
        pfac = null;
        parser = null;
        source = null;
    }


    /**
     * Test rational polynomial.
     */
    @SuppressWarnings("unchecked")
    public void testBigRational() {
        String exam = "Rat(x,y,z) L " + "( " + "( 1 ), " + "( 0 ), " + "( 3/4 - 6/8 ), "
                        + "( 1 x + x^3 + 1/3 y z - x^3 ) " + " )";
        source = new StringReader(exam);
        parser = new GenPolynomialTokenizer(source);
        PolynomialList<BigRational> f = null;
        try {
            f = (PolynomialList<BigRational>) parser.nextPolynomialSet();
        } catch (IOException e) {
            fail("" + e);
        } catch (ClassCastException e) {
            fail("" + e);
        }
        //System.out.println("f = " + f);
        assertTrue("f != null", f.list != null);
        assertTrue("length( f ) = 4", f.list.size() == 4);

        BigRational fac = new BigRational(0);
        TermOrder tord = new TermOrder(TermOrder.INVLEX);
        String[] vars = new String[] { "x", "y", "z" };
        int nvar = vars.length;
        pfac = new GenPolynomialRing<BigRational>(fac, nvar, tord, vars);
        assertEquals("pfac == f.ring", pfac, f.ring);

        GenPolynomial<BigRational> a = f.list.get(0);
        //System.out.println("a = " + a);
        assertTrue("isONE( f.get(0) )", a.isONE());

        GenPolynomial<BigRational> b = f.list.get(1);
        //System.out.println("b = " + b);
        assertTrue("isZERO( f.get(1) )", b.isZERO());

        GenPolynomial<BigRational> c = f.list.get(2);
        //System.out.println("c = " + c);
        assertTrue("isZERO( f.get(2) )", c.isZERO());

        GenPolynomial<BigRational> d = f.list.get(3);
        //System.out.println("d = " + d);
        assertEquals("f.get(3).length() == 2", 2, d.length());
    }


    /**
     * Test integer polynomial.
     */
    @SuppressWarnings("unchecked")
    public void testBigInteger() {
        String exam = "Int(x,y,z) L " + "( " + "( 1 ), " + "( 0 ), " + "( 3 2 - 6 ), "
                        + "( 1 x + x^3 + 3 y z - x^3 ) " + " )";
        source = new StringReader(exam);
        parser = new GenPolynomialTokenizer(source);
        PolynomialList<BigInteger> f = null;
        try {
            f = (PolynomialList<BigInteger>) parser.nextPolynomialSet();
        } catch (IOException e) {
            fail("" + e);
        } catch (ClassCastException e) {
            fail("" + e);
        }
        //System.out.println("f = " + f);
        assertTrue("f != null", f.list != null);
        assertTrue("length( f ) = 4", f.list.size() == 4);

        BigInteger fac = new BigInteger(0);
        TermOrder tord = new TermOrder(TermOrder.INVLEX);
        String[] vars = new String[] { "x", "y", "z" };
        int nvar = vars.length;
        pfac = new GenPolynomialRing<BigInteger>(fac, nvar, tord, vars);
        assertEquals("pfac == f.ring", pfac, f.ring);


        GenPolynomial<BigInteger> a = f.list.get(0);
        //System.out.println("a = " + a);
        assertTrue("isONE( f.get(0) )", a.isONE());

        GenPolynomial<BigInteger> b = f.list.get(1);
        //System.out.println("b = " + b);
        assertTrue("isZERO( f.get(1) )", b.isZERO());

        GenPolynomial<BigInteger> c = f.list.get(2);
        //System.out.println("c = " + c);
        assertTrue("isZERO( f.get(2) )", c.isZERO());

        GenPolynomial<BigInteger> d = f.list.get(3);
        //System.out.println("d = " + d);
        assertEquals("f.get(3).length() == 2", 2, d.length());
    }


    /**
     * Test modular integer polynomial.
     */
    @SuppressWarnings("unchecked")
    public void testModInteger() {
        String exam = "Mod 19 (x,y,z) L " + "( " + "( 1 ), " + "( 0 ), " + "( 3 2 - 6 + 19 ), "
                        + "( 1 x + x^3 + 3 y z - x^3 ) " + " )";
        source = new StringReader(exam);
        parser = new GenPolynomialTokenizer(source);
        PolynomialList<ModInteger> f = null;
        try {
            f = (PolynomialList<ModInteger>) parser.nextPolynomialSet();
        } catch (IOException e) {
            fail("" + e);
        } catch (ClassCastException e) {
            fail("" + e);
        }
        //System.out.println("f = " + f);
        assertTrue("f != null", f.list != null);
        assertTrue("length( f ) = 4", f.list.size() == 4);

        ModLongRing fac = new ModLongRing(19);
        TermOrder tord = new TermOrder(TermOrder.INVLEX);
        String[] vars = new String[] { "x", "y", "z" };
        int nvar = vars.length;
        pfac = new GenPolynomialRing<ModLong>(fac, nvar, tord, vars);
        assertEquals("pfac == f.ring", pfac, f.ring);

        GenPolynomial<ModInteger> a = f.list.get(0);
        //System.out.println("a = " + a);
        assertTrue("isONE( f.get(0) )", a.isONE());

        GenPolynomial<ModInteger> b = f.list.get(1);
        //System.out.println("b = " + b);
        assertTrue("isZERO( f.get(1) )", b.isZERO());

        GenPolynomial<ModInteger> c = f.list.get(2);
        //System.out.println("c = " + c);
        assertTrue("isZERO( f.get(2) )", c.isZERO());

        GenPolynomial<ModInteger> d = f.list.get(3);
        //System.out.println("d = " + d);
        assertEquals("f.get(3).length() == 2", 2, d.length());
    }


    /**
     * Test complex polynomial.
     */
    @SuppressWarnings("unchecked")
    public void testBigComplex() {
        String exam = "Complex(x,y,z) L " + "( " + "( 1i0 ), " + "( 0i0 ), " + "( 3/4i2 - 6/8i2 ), "
                        + "( 1i0 x + x^3 + 1i3 y z - x^3 ) " + " )";
        source = new StringReader(exam);
        parser = new GenPolynomialTokenizer(source);
        PolynomialList<BigComplex> f = null;
        try {
            f = (PolynomialList<BigComplex>) parser.nextPolynomialSet();
        } catch (IOException e) {
            fail("" + e);
        } catch (ClassCastException e) {
            fail("" + e);
        }
        //System.out.println("f = " + f);
        assertTrue("f != null", f.list != null);
        assertTrue("length( f ) = 4", f.list.size() == 4);

        BigComplex fac = new BigComplex(0);
        TermOrder tord = new TermOrder(TermOrder.INVLEX);
        String[] vars = new String[] { "x", "y", "z" };
        int nvar = vars.length;
        pfac = new GenPolynomialRing<BigComplex>(fac, nvar, tord, vars);
        assertEquals("pfac == f.ring", pfac, f.ring);


        GenPolynomial<BigComplex> a = f.list.get(0);
        //System.out.println("a = " + a);
        assertTrue("isONE( f.get(0) )", a.isONE());

        GenPolynomial<BigComplex> b = f.list.get(1);
        //System.out.println("b = " + b);
        assertTrue("isZERO( f.get(1) )", b.isZERO());

        GenPolynomial<BigComplex> c = f.list.get(2);
        //System.out.println("c = " + c);
        assertTrue("isZERO( f.get(2) )", c.isZERO());

        GenPolynomial<BigComplex> d = f.list.get(3);
        //System.out.println("d = " + d);
        assertEquals("f.get(3).length() == 2", 2, d.length());
    }


    /**
     * Test decimal polynomial.
     */
    @SuppressWarnings("unchecked")
    public void testBigDecimal() {
        String exam = "D(x,y,z) L " + "( " + "( 1 ), " + "( 0 ), " + "( 0.25 * 0.25 - 0.25^2 ), "
                        + "( 1 x + x^3 + 0.3333333333333333333333 y z - x^3 ) " + " )";
        source = new StringReader(exam);
        parser = new GenPolynomialTokenizer(source);
        PolynomialList<BigDecimal> f = null;
        try {
            f = (PolynomialList<BigDecimal>) parser.nextPolynomialSet();
        } catch (IOException e) {
            fail("" + e);
        } catch (ClassCastException e) {
            fail("" + e);
        }
        //System.out.println("f = " + f);
        assertTrue("f != null", f.list != null);
        assertTrue("length( f ) = 4", f.list.size() == 4);

        BigDecimal fac = new BigDecimal(0);
        TermOrder tord = new TermOrder(TermOrder.INVLEX);
        String[] vars = new String[] { "x", "y", "z" };
        int nvar = vars.length;
        pfac = new GenPolynomialRing<BigDecimal>(fac, nvar, tord, vars);
        assertEquals("pfac == f.ring", pfac, f.ring);

        GenPolynomial<BigDecimal> a = f.list.get(0);
        //System.out.println("a = " + a);
        assertTrue("isONE( f.get(0) )", a.isONE());

        GenPolynomial<BigDecimal> b = f.list.get(1);
        //System.out.println("b = " + b);
        assertTrue("isZERO( f.get(1) )", b.isZERO());

        GenPolynomial<BigDecimal> c = f.list.get(2);
        //System.out.println("c = " + c);
        assertTrue("isZERO( f.get(2) )", c.isZERO());

        GenPolynomial<BigDecimal> d = f.list.get(3);
        //System.out.println("d = " + d);
        assertEquals("f.get(3).length() == 2", 2, d.length());
    }


    /**
     * Test quaternion polynomial.
     */
    @SuppressWarnings("unchecked")
    public void testBigQuaternion() {
        String exam = "Quat(x,y,z) L " + "( " + "( 1i0j0k0 ), " + "( 0i0j0k0 ), "
                        + "( 3/4i2j1k3 - 6/8i2j1k3 ), " + "( 1 x + x^3 + 1i2j3k4 y z - x^3 ) " + " )";
        source = new StringReader(exam);
        parser = new GenPolynomialTokenizer(source);
        PolynomialList<BigQuaternion> f = null;
        try {
            f = (PolynomialList<BigQuaternion>) parser.nextPolynomialSet();
        } catch (IOException e) {
            fail("" + e);
        } catch (ClassCastException e) {
            fail("" + e);
        }
        //System.out.println("f = " + f);
        assertTrue("f != null", f.list != null);
        assertTrue("length( f ) = 4", f.list.size() == 4);

        BigQuaternion fac = new BigQuaternion(0);
        TermOrder tord = new TermOrder(TermOrder.INVLEX);
        String[] vars = new String[] { "x", "y", "z" };
        int nvar = vars.length;
        pfac = new GenPolynomialRing<BigQuaternion>(fac, nvar, tord, vars);
        assertEquals("pfac == f.ring", pfac, f.ring);


        GenPolynomial<BigQuaternion> a = f.list.get(0);
        //System.out.println("a = " + a);
        assertTrue("isONE( f.get(0) )", a.isONE());

        GenPolynomial<BigQuaternion> b = f.list.get(1);
        //System.out.println("b = " + b);
        assertTrue("isZERO( f.get(1) )", b.isZERO());

        GenPolynomial<BigQuaternion> c = f.list.get(2);
        //System.out.println("c = " + c);
        assertTrue("isZERO( f.get(2) )", c.isZERO());

        GenPolynomial<BigQuaternion> d = f.list.get(3);
        //System.out.println("d = " + d);
        assertEquals("f.get(3).length() == 2", 2, d.length());
    }


    /**
     * Test rational solvable polynomial.
     */
    @SuppressWarnings("unchecked")
    public void testSolvableBigRational() {
        String exam = "Rat(x,y,z) L " + "RelationTable " + "( " + " ( z ), ( y ), ( y z - 1 ) " + ") " + "( "
                        + " ( 1 ), " + " ( 0 ), " + " ( 3/4 - 6/8 ), " + " ( 1 x + x^3 + 1/3 y z - x^3 ) "
                        + " )";
        source = new StringReader(exam);
        parser = new GenPolynomialTokenizer(source);
        PolynomialList<BigRational> f = null;
        try {
            f = (PolynomialList<BigRational>) parser.nextSolvablePolynomialSet();
        } catch (IOException e) {
            fail("" + e);
        } catch (ClassCastException e) {
            fail("" + e);
        }
        //System.out.println("f = " + f);
        //System.out.println("f.ring.table = " + ((GenSolvablePolynomialRing)f.ring).table);
        assertTrue("f != null", f.list != null);
        assertTrue("length( f ) = 4", f.list.size() == 4);

        BigRational fac = new BigRational(0);
        TermOrder tord = new TermOrder(TermOrder.INVLEX);
        String[] vars = new String[] { "x", "y", "z" };
        int nvar = vars.length;
        spfac = new GenSolvablePolynomialRing<BigRational>(fac, nvar, tord, vars);
        List<GenSolvablePolynomial<BigRational>> rel = new ArrayList<GenSolvablePolynomial<BigRational>>(3);
        rel.add( spfac.parse("z") );
        rel.add( spfac.parse("y") );
        rel.add( spfac.parse("y z - 1") );
        spfac.addSolvRelations(rel);
        assertEquals("spfac == f.ring", spfac, f.ring);
        //System.out.println("spfac = " + spfac);
        //System.out.println("spfac.table = " + spfac.table);


        GenSolvablePolynomial<BigRational> a = f.castToSolvableList().get(0);
        //System.out.println("a = " + a);
        assertTrue("isZERO( f.get(0) )", a.isONE());

        GenSolvablePolynomial<BigRational> b = f.castToSolvableList().get(1);
        //System.out.println("b = " + b);
        assertTrue("isZERO( f.get(1) )", b.isZERO());

        GenSolvablePolynomial<BigRational> c = f.castToSolvableList().get(2);
        //System.out.println("c = " + c);
        assertTrue("isONE( f.get(2) )", c.isZERO());

        GenSolvablePolynomial<BigRational> d = f.castToSolvableList().get(3);
        //System.out.println("d = " + d);
        assertEquals("f.get(3).length() == 2", 2, d.length());
    }


    /**
     * Test mod integer solvable polynomial.
     */
    @SuppressWarnings("unchecked")
    public void testSolvableModInteger() {
        String exam = "Mod 19 (x,y,z) L " + "RelationTable " + "( " + " ( z ), ( y ), ( y z - 1 ) " + ") "
                        + "( " + "( 1 ), " + "( 0 ), " + "( 3 2 - 6 + 19 ), "
                        + "( 1 x + x^3 + 3 y z - x^3 ) " + " )";
        source = new StringReader(exam);
        parser = new GenPolynomialTokenizer(source);
        PolynomialList<ModInteger> f = null;
        try {
            f = (PolynomialList<ModInteger>) parser.nextSolvablePolynomialSet();
        } catch (IOException e) {
            fail("" + e);
        } catch (ClassCastException e) {
            fail("" + e);
        }
        //System.out.println("f = " + f);
        //System.out.println("f.ring.table = " + ((GenSolvablePolynomialRing)f.ring).table);
        assertTrue("f != null", f.list != null);
        assertTrue("length( f ) = 4", f.list.size() == 4);

        ModLongRing fac = new ModLongRing(19);
        TermOrder tord = new TermOrder(TermOrder.INVLEX);
        String[] vars = new String[] { "x", "y", "z" };
        int nvar = vars.length;
        spfac = new GenSolvablePolynomialRing<ModLong>(fac, nvar, tord, vars);
        List<GenSolvablePolynomial<ModLong>> rel = new ArrayList<GenSolvablePolynomial<ModLong>>(3);
        rel.add( spfac.parse("z") );
        rel.add( spfac.parse("y") );
        rel.add( spfac.parse("y z - 1") );
        spfac.addSolvRelations(rel);
        assertEquals("spfac == f.ring", spfac, f.ring);
        //System.out.println("spfac = " + spfac);
        //System.out.println("spfac.table = " + spfac.table);


        GenSolvablePolynomial<ModInteger> a = f.castToSolvableList().get(0);
        //System.out.println("a = " + a);
        assertTrue("isZERO( f.get(0) )", a.isONE());

        GenSolvablePolynomial<ModInteger> b = f.castToSolvableList().get(1);
        //System.out.println("b = " + b);
        assertTrue("isZERO( f.get(1) )", b.isZERO());

        GenSolvablePolynomial<ModInteger> c = f.castToSolvableList().get(2);
        //System.out.println("c = " + c);
        assertTrue("isONE( f.get(2) )", c.isZERO());

        GenSolvablePolynomial<ModInteger> d = f.castToSolvableList().get(3);
        //System.out.println("d = " + d);
        assertEquals("f.get(3).length() == 2", 2, d.length());
    }


    /**
     * Test integer polynomial module.
     */
    @SuppressWarnings("unchecked")
    public void testBigIntegerModule() {
        String exam = "Int(x,y,z) L " + "( " + " ( " + "  ( 1 ), " + "  ( 0 ), " + "  ( 3 2 - 6 ), "
                        + "  ( 1 x + x^3 + 3 y z - x^3 ) " + " ), " + " ( ( 1 ), ( 0 ) ) " + ")";
        source = new StringReader(exam);
        parser = new GenPolynomialTokenizer(source);
        ModuleList<BigInteger> m = null;
        try {
            m = (ModuleList<BigInteger>) parser.nextSubModuleSet();
        } catch (IOException e) {
            fail("" + e);
        } catch (ClassCastException e) {
            fail("" + e);
        }
        //System.out.println("m = " + m);
        assertTrue("m != null", m.list != null);
        assertTrue("length( m ) = 2", m.list.size() == 2);
        assertTrue("length( m[0] ) = 4", ((List) m.list.get(0)).size() == 4);


        BigInteger fac = new BigInteger(0);
        TermOrder tord = new TermOrder(TermOrder.INVLEX);
        String[] vars = new String[] { "x", "y", "z" };
        int nvar = vars.length;
        pfac = new GenPolynomialRing<BigInteger>(fac, nvar, tord, vars);
        assertEquals("pfac == m.ring", pfac, m.ring);

        List<List<GenPolynomial<BigInteger>>> rows = m.list;
        List<GenPolynomial<BigInteger>> f;

        f = rows.get(0);
        GenPolynomial<BigInteger> a = f.get(0);
        //System.out.println("a = " + a);
        assertTrue("isONE( f.get(0) )", a.isONE());

        GenPolynomial<BigInteger> b = f.get(1);
        //System.out.println("b = " + b);
        assertTrue("isZERO( f.get(1) )", b.isZERO());

        GenPolynomial<BigInteger> c = f.get(2);
        //System.out.println("c = " + c);
        assertTrue("isZERO( f.get(2) )", c.isZERO());

        GenPolynomial<BigInteger> d = f.get(3);
        //System.out.println("d = " + d);
        assertEquals("f.get(3).length() == 2", 2, d.length());

        f = rows.get(1);
        assertTrue("length( f ) = 4", f.size() == 4);

        a = f.get(0);
        //System.out.println("a = " + a);
        assertTrue("isONE( f.get(0) )", a.isONE());

        b = f.get(1);
        //System.out.println("b = " + b);
        assertTrue("isZERO( f.get(1) )", b.isZERO());

        c = f.get(2);
        //System.out.println("c = " + c);
        assertTrue("isZERO( f.get(2) )", c.isZERO());

        d = f.get(3);
        //System.out.println("c = " + d);
        assertTrue("isZERO( f.get(3) )", d.isZERO());
    }


    /**
     * Test rational solvable polynomial module.
     */
    @SuppressWarnings("unchecked")
    public void testBigRationalSolvableModule() {
        String exam = "Rat(x,y,z) L " + "RelationTable " + "( " + " ( z ), ( y ), ( y z - 1 ) " + ") " + "( "
                        + " ( " + "  ( 1 ), " + "  ( 0 ), " + "  ( 3/4 - 6/8 ), "
                        + "  ( 1 x + x^3 + 1/3 y z - x^3 ) " + " ), " + " ( ( x ), ( 1 ), ( 0 ) ) " + " )";
        source = new StringReader(exam);
        parser = new GenPolynomialTokenizer(source);
        ModuleList<BigRational> m = null;
        try {
            m = (ModuleList<BigRational>) parser.nextSolvableSubModuleSet();
        } catch (IOException e) {
            fail("" + e);
        } catch (ClassCastException e) {
            fail("" + e);
        }
        //System.out.println("m = " + m);
        //System.out.println("m.ring = " + m.ring);
        assertTrue("m != null", m.list != null);
        assertTrue("length( m ) = 2", m.list.size() == 2);
        assertTrue("length( m[0] ) = 4", ((List) m.list.get(0)).size() == 4);

        BigRational fac = new BigRational(0);
        TermOrder tord = new TermOrder(TermOrder.INVLEX);
        String[] vars = new String[] { "x", "y", "z" };
        int nvar = vars.length;
        spfac = new GenSolvablePolynomialRing<BigRational>(fac, nvar, tord, vars);
        List<GenSolvablePolynomial<ModLong>> rel = new ArrayList<GenSolvablePolynomial<ModLong>>(3);
        rel.add( spfac.parse("z") );
        rel.add( spfac.parse("y") );
        rel.add( spfac.parse("y z - 1") );
        spfac.addSolvRelations(rel);
        assertEquals("spfac == m.ring", spfac, m.ring);

        List<List<GenSolvablePolynomial<BigRational>>> rows = m.castToSolvableList();
        List<GenSolvablePolynomial<BigRational>> f;

        f = rows.get(0);
        GenSolvablePolynomial<BigRational> a = f.get(0);
        //System.out.println("a = " + a);
        assertTrue("isONE( f.get(0) )", a.isONE());

        GenSolvablePolynomial<BigRational> b = f.get(1);
        //System.out.println("b = " + b);
        assertTrue("isZERO( f.get(1) )", b.isZERO());

        GenSolvablePolynomial<BigRational> c = f.get(2);
        //System.out.println("c = " + c);
        assertTrue("isZERO( f.get(2) )", c.isZERO());

        GenSolvablePolynomial<BigRational> d = f.get(3);
        //System.out.println("d = " + d);
        assertEquals("f.get(3).length() == 2", 2, d.length());

        f = rows.get(1);
        assertTrue("length( f ) = 4", f.size() == 4);

        a = f.get(0);
        //System.out.println("a = " + a);
        assertTrue("!isONE( f.get(0) )", !a.isONE());

        b = f.get(1);
        //System.out.println("b = " + b);
        assertTrue("isONE( f.get(1) )", b.isONE());

        c = f.get(2);
        //System.out.println("c = " + c);
        assertTrue("isZERO( f.get(2) )", c.isZERO());

        d = f.get(3);
        //System.out.println("d = " + d);
        assertTrue("isZERO( f.get(3) )", d.isZERO());

    }


    /**
     * Test algebraic number polynomial. <b>Note: </b> Syntax no more supported.
     */
    @SuppressWarnings("unchecked")
    public void removedTestAlgebraicNumber() {
        String exam = "AN[ (i) ( i^2 + 1 ) ] (x,y,z) L " + "( " + "( 1 ), " + "( _i_ ), " + "( 0 ), "
                        + "( _i^2_ + 1 ), " + "( 1 x + x^3 + _3 i_ y z - x^3 ) " + " )";
        source = new StringReader(exam);
        parser = new GenPolynomialTokenizer(source);
        PolynomialList<AlgebraicNumber<BigRational>> f = null;
        AlgebraicNumberRing<BigRational> fac = null;
        try {
            f = (PolynomialList<AlgebraicNumber<BigRational>>) parser.nextPolynomialSet();
            fac = (AlgebraicNumberRing<BigRational>) f.ring.coFac;
        } catch (IOException e) {
            fail("" + e);
        } catch (ClassCastException e) {
            fail("" + e);
        }
        //System.out.println("f = " + f);
        assertTrue("f != null", f.list != null);
        assertTrue("length( f ) = 5", f.list.size() == 5);

        TermOrder tord = new TermOrder(TermOrder.INVLEX);
        String[] vars = new String[] { "x", "y", "z" };
        int nvar = vars.length;
        pfac = new GenPolynomialRing<AlgebraicNumber<BigRational>>(fac, nvar, tord, vars);
        assertEquals("pfac == f.ring", pfac, f.ring);

        GenPolynomial<AlgebraicNumber<BigRational>> a = f.list.get(0);
        //System.out.println("a = " + a);
        assertTrue("isONE( f.get(0) )", a.isONE());

        GenPolynomial<AlgebraicNumber<BigRational>> b = f.list.get(1);
        //System.out.println("b = " + b);
        assertTrue("isUnit( f.get(1) )", b.isUnit());

        b = b.monic();
        //System.out.println("b = " + b);
        assertTrue("isUnit( f.get(1) )", b.isONE());

        GenPolynomial<AlgebraicNumber<BigRational>> c = f.list.get(2);
        //System.out.println("c = " + c);
        assertTrue("isZERO( f.get(1) )", c.isZERO());

        GenPolynomial<AlgebraicNumber<BigRational>> d = f.list.get(3);
        //System.out.println("d = " + d);
        assertTrue("isZERO( f.get(2) )", d.isZERO());

        GenPolynomial<AlgebraicNumber<BigRational>> e = f.list.get(4);
        //System.out.println("e = " + e);
        assertEquals("f.get(3).length() == 2", 2, e.length());
    }


    /**
     * Test Galois field coefficient polynomial. <b>Note: </b> Syntax no more
     * supported.
     */
    @SuppressWarnings("unchecked")
    public void removedTestGaloisField() {
        String exam = "AN[ 19 (i) ( i^2 + 1 ) ] (x,y,z) L " + "( " + "( 20 ), " + "( _i_ ), " + "( 0 ), "
                        + "( _i^2_ + 20 ), " + "( 1 x + x^3 + _3 i_ y z - x^3 ) " + " )";
        source = new StringReader(exam);
        parser = new GenPolynomialTokenizer(source);
        PolynomialList<AlgebraicNumber<ModInteger>> f = null;
        AlgebraicNumberRing<ModInteger> fac = null;
        try {
            f = (PolynomialList<AlgebraicNumber<ModInteger>>) parser.nextPolynomialSet();
            fac = (AlgebraicNumberRing<ModInteger>) f.ring.coFac;
        } catch (IOException e) {
            fail("" + e);
        } catch (ClassCastException e) {
            fail("" + e);
        }
        //System.out.println("f = " + f);
        assertTrue("f != null", f.list != null);
        assertTrue("length( f ) = 5", f.list.size() == 5);

        TermOrder tord = new TermOrder(TermOrder.INVLEX);
        String[] vars = new String[] { "x", "y", "z" };
        int nvar = vars.length;
        pfac = new GenPolynomialRing<AlgebraicNumber<ModInteger>>(fac, nvar, tord, vars);
        assertEquals("pfac == f.ring", pfac, f.ring);

        GenPolynomial<AlgebraicNumber<ModInteger>> a = f.list.get(0);
        //System.out.println("a = " + a);
        assertTrue("isONE( f.get(0) )", a.isONE());

        GenPolynomial<AlgebraicNumber<ModInteger>> b = f.list.get(1);
        //System.out.println("b = " + b);
        assertTrue("isUnit( f.get(1) )", b.isUnit());

        b = b.monic();
        //System.out.println("b = " + b);
        assertTrue("isUnit( f.get(1) )", b.isONE());

        GenPolynomial<AlgebraicNumber<ModInteger>> c = f.list.get(2);
        //System.out.println("c = " + c);
        assertTrue("isZERO( f.get(1) )", c.isZERO());

        GenPolynomial<AlgebraicNumber<ModInteger>> d = f.list.get(3);
        //System.out.println("d = " + d);
        assertTrue("isZERO( f.get(2) )", d.isZERO());

        GenPolynomial<AlgebraicNumber<ModInteger>> e = f.list.get(4);
        //System.out.println("e = " + e);
        assertEquals("f.get(3).length() == 2", 2, e.length());
    }


    /**
     * Test algebraic number polynomial with braces.
     */
    @SuppressWarnings("unchecked")
    public void testAlgebraicNumberBrace() {
        String exam = "AN[ (i) ( i^2 + 1 ) ] (x,y,z) L " + "( " + "( 1 ), " + "( { i } ), " + "( 0 ), "
                        + "( { i^2 } + 1 ), " + "( 1 x + x^3 + { 3 i }^2  y z - x^3 ) " + " )";
        source = new StringReader(exam);
        parser = new GenPolynomialTokenizer(source);
        PolynomialList<AlgebraicNumber<BigRational>> f = null;
        AlgebraicNumberRing<BigRational> fac = null;
        try {
            f = (PolynomialList<AlgebraicNumber<BigRational>>) parser.nextPolynomialSet();
            fac = (AlgebraicNumberRing<BigRational>) f.ring.coFac;
        } catch (IOException e) {
            fail("" + e);
        } catch (ClassCastException e) {
            fail("" + e);
        }
        //System.out.println("f = " + f);
        assertTrue("f != null", f.list != null);
        assertTrue("length( f ) = 5", f.list.size() == 5);

        TermOrder tord = new TermOrder(TermOrder.INVLEX);
        String[] vars = new String[] { "x", "y", "z" };
        int nvar = vars.length;
        pfac = new GenPolynomialRing<AlgebraicNumber<BigRational>>(fac, nvar, tord, vars);
        assertEquals("pfac == f.ring", pfac, f.ring);

        GenPolynomial<AlgebraicNumber<BigRational>> a = f.list.get(0);
        //System.out.println("a = " + a);
        assertTrue("isONE( f.get(0) )", a.isONE());

        GenPolynomial<AlgebraicNumber<BigRational>> b = f.list.get(1);
        //System.out.println("b = " + b);
        assertTrue("isUnit( f.get(1) )", b.isUnit());

        b = b.monic();
        //System.out.println("b = " + b);
        assertTrue("isUnit( f.get(1) )", b.isONE());

        GenPolynomial<AlgebraicNumber<BigRational>> c = f.list.get(2);
        //System.out.println("c = " + c);
        assertTrue("isZERO( f.get(1) )", c.isZERO());

        GenPolynomial<AlgebraicNumber<BigRational>> d = f.list.get(3);
        //System.out.println("d = " + d);
        assertTrue("isZERO( f.get(2) )", d.isZERO());

        GenPolynomial<AlgebraicNumber<BigRational>> e = f.list.get(4);
        //System.out.println("e = " + e);
        assertEquals("f.get(3).length() == 2", 2, e.length());
    }


    /**
     * Test Galois field coefficient polynomial with braces.
     */
    @SuppressWarnings("unchecked")
    public void testGaloisFieldBrace() {
        String exam = "AN[ 19 (i) ( i^2 + 1 ) ] (x,y,z) L " + "( " + "( 20 ), " + "( { i } ), " + "( 0 ), "
                        + "( { i^2 } + 20 ), " + "( 1 x + x^3 + { 3 i }^3 y z + { -1 }^3 x^3 ) " + " )";
        source = new StringReader(exam);
        parser = new GenPolynomialTokenizer(source);
        PolynomialList<AlgebraicNumber<ModInteger>> f = null;
        AlgebraicNumberRing<ModInteger> fac = null;
        try {
            f = (PolynomialList<AlgebraicNumber<ModInteger>>) parser.nextPolynomialSet();
            fac = (AlgebraicNumberRing<ModInteger>) f.ring.coFac;
        } catch (IOException e) {
            fail("" + e);
        } catch (ClassCastException e) {
            fail("" + e);
        }
        //System.out.println("f = " + f);
        assertTrue("f != null", f.list != null);
        assertTrue("length( f ) = 5", f.list.size() == 5);

        TermOrder tord = new TermOrder(TermOrder.INVLEX);
        String[] vars = new String[] { "x", "y", "z" };
        int nvar = vars.length;
        pfac = new GenPolynomialRing<AlgebraicNumber<ModInteger>>(fac, nvar, tord, vars);
        assertEquals("pfac == f.ring", pfac, f.ring);

        GenPolynomial<AlgebraicNumber<ModInteger>> a = f.list.get(0);
        //System.out.println("a = " + a);
        assertTrue("isONE( f.get(0) )", a.isONE());

        GenPolynomial<AlgebraicNumber<ModInteger>> b = f.list.get(1);
        //System.out.println("b = " + b);
        assertTrue("isUnit( f.get(1) )", b.isUnit());

        b = b.monic();
        //System.out.println("b = " + b);
        assertTrue("isUnit( f.get(1) )", b.isONE());

        GenPolynomial<AlgebraicNumber<ModInteger>> c = f.list.get(2);
        //System.out.println("c = " + c);
        assertTrue("isZERO( f.get(1) )", c.isZERO());

        GenPolynomial<AlgebraicNumber<ModInteger>> d = f.list.get(3);
        //System.out.println("d = " + d);
        assertTrue("isZERO( f.get(2) )", d.isZERO());

        GenPolynomial<AlgebraicNumber<ModInteger>> e = f.list.get(4);
        //System.out.println("e = " + e);
        assertEquals("f.get(3).length() == 2", 2, e.length());
    }


    /**
     * Test Galois field coefficient polynomial without braces.
     */
    @SuppressWarnings("unchecked")
    public void testGaloisFieldWoBrace() {
        String exam = "AN[ 19 (i) ( i^2 + 1 ) ] (x,y,z) L " + "( " + "( 20 ), " + "( i ), " + "( 0 ), "
                        + "( i^2 + 20 ), " + "( 1 x + x^3 + 3^3 i^3 y z - (x)^3 ) " + " )";
        source = new StringReader(exam);
        parser = new GenPolynomialTokenizer(source);
        PolynomialList<AlgebraicNumber<ModInteger>> f = null;
        AlgebraicNumberRing<ModInteger> fac = null;
        try {
            f = (PolynomialList<AlgebraicNumber<ModInteger>>) parser.nextPolynomialSet();
            fac = (AlgebraicNumberRing<ModInteger>) f.ring.coFac;
        } catch (IOException e) {
            fail("" + e);
        } catch (ClassCastException e) {
            fail("" + e);
        }
        //System.out.println("f = " + f);
        assertTrue("f != null", f.list != null);
        assertTrue("length( f ) = 5", f.list.size() == 5);

        TermOrder tord = new TermOrder(TermOrder.INVLEX);
        String[] vars = new String[] { "x", "y", "z" };
        int nvar = vars.length;
        pfac = new GenPolynomialRing<AlgebraicNumber<ModInteger>>(fac, nvar, tord, vars);
        assertEquals("pfac == f.ring", pfac, f.ring);

        GenPolynomial<AlgebraicNumber<ModInteger>> a = f.list.get(0);
        //System.out.println("a = " + a);
        assertTrue("isONE( f.get(0) )", a.isONE());

        GenPolynomial<AlgebraicNumber<ModInteger>> b = f.list.get(1);
        //System.out.println("b = " + b);
        assertTrue("isUnit( f.get(1) )", b.isUnit());

        b = b.monic();
        //System.out.println("b = " + b);
        assertTrue("isUnit( f.get(1) )", b.isONE());

        GenPolynomial<AlgebraicNumber<ModInteger>> c = f.list.get(2);
        //System.out.println("c = " + c);
        assertTrue("isZERO( f.get(1) )", c.isZERO());

        GenPolynomial<AlgebraicNumber<ModInteger>> d = f.list.get(3);
        //System.out.println("d = " + d);
        assertTrue("isZERO( f.get(2) )", d.isZERO());

        GenPolynomial<AlgebraicNumber<ModInteger>> e = f.list.get(4);
        //System.out.println("e = " + e);
        assertEquals("f.get(3).length() == 2", 2, e.length());
    }


    /**
     * Test rational polynomial with generic coefficients.
     */
    @SuppressWarnings("unchecked")
    public void testBigRationalGeneric() {
        String exam = "Rat(x,y,z) L " + "( " + "( 1^3 ), " + "( 0^3 ), " + "( { 3/4 }^2 - 6/8^2 ), "
                        + "( { 1 }^2 x + x^3 + 1/3 y z - x^3 ), "
                        + "( 1.0001 - 0.0001 + { 0.25 }**2 - 1/4^2 ) " + " )";
        source = new StringReader(exam);
        parser = new GenPolynomialTokenizer(source);
        PolynomialList<BigRational> f = null;
        try {
            f = (PolynomialList<BigRational>) parser.nextPolynomialSet();
        } catch (IOException e) {
            fail("" + e);
        } catch (ClassCastException e) {
            fail("" + e);
        }
        //System.out.println("f = " + f);
        assertTrue("f != null", f.list != null);
        assertTrue("length( f ) = 5", f.list.size() == 5);

        BigRational fac = new BigRational(0);
        TermOrder tord = new TermOrder(TermOrder.INVLEX);
        String[] vars = new String[] { "x", "y", "z" };
        int nvar = vars.length;
        pfac = new GenPolynomialRing<BigRational>(fac, nvar, tord, vars);
        assertEquals("pfac == f.ring", pfac, f.ring);


        GenPolynomial<BigRational> a = f.list.get(0);
        //System.out.println("a = " + a);
        assertTrue("isONE( f.get(0) )", a.isONE());

        GenPolynomial<BigRational> b = f.list.get(1);
        //System.out.println("b = " + b);
        assertTrue("isZERO( f.get(1) )", b.isZERO());

        GenPolynomial<BigRational> c = f.list.get(2);
        //System.out.println("c = " + c);
        assertTrue("isZERO( f.get(2) )", c.isZERO());

        GenPolynomial<BigRational> d = f.list.get(3);
        //System.out.println("d = " + d);
        assertEquals("f.get(3).length() == 2", 2, d.length());

        GenPolynomial<BigRational> e = f.list.get(4);
        //System.out.println("e = " + e);
        assertTrue("isONE( f.get(4) )", e.isONE());
    }


    /**
     * Test rational polynomial with errors.
     */
    @SuppressWarnings("unchecked")
    public void testBigRationalErorr() {
        // brace mismatch
        String exam = "Rat(x,y,z) L " + "( " + "( { 3 ), " + " )";
        source = new StringReader(exam);
        parser = new GenPolynomialTokenizer(source);
        PolynomialList<BigRational> f = null;
        try {
            f = (PolynomialList<BigRational>) parser.nextPolynomialSet();
        } catch (IOException e) {
            fail("" + e);
        } catch (ClassCastException e) {
            fail("" + e);
        } catch (InvalidExpressionException e) {
            // pass
        }

        // brace mismatch
        exam = "Rat(x,y,z) L " + "( " + "( 3 } ), " + " )";
        source = new StringReader(exam);
        parser = new GenPolynomialTokenizer(source);
        f = null;
        try {
            f = (PolynomialList<BigRational>) parser.nextPolynomialSet();
        } catch (IOException e) {
            fail("" + e);
        } catch (ClassCastException e) {
            fail("" + e);
        } catch (InvalidExpressionException e) {
            // pass
        }

        // invalid nesting
        exam = "Rat(x,y,z) L " + "( " + "( { x } ), " + " )";
        source = new StringReader(exam);
        parser = new GenPolynomialTokenizer(source);
        f = null;
        try {
            f = (PolynomialList<BigRational>) parser.nextPolynomialSet();
        } catch (IOException e) {
            fail("" + e);
        } catch (ClassCastException e) {
            fail("" + e);
        } catch (InvalidExpressionException e) {
            // pass
        }

        // unknown variable
        exam = "Rat(x,y,z) L " + "( " + "( w ), " + " )";
        source = new StringReader(exam);
        parser = new GenPolynomialTokenizer(source);
        f = null;
        try {
            f = (PolynomialList<BigRational>) parser.nextPolynomialSet();
        } catch (IOException e) {
            fail("" + e);
        } catch (ClassCastException e) {
            fail("" + e);
        } catch (InvalidExpressionException e) {
            // pass
        }
        assertTrue("f != null", f == null);
    }


    /**
     * Test variables.
     */
    public void testVariables() {
        String vars = "a,b,c,d,e";
        String[] variables = GenPolynomialTokenizer.variableList(vars);
        assertTrue("len == 5: ", variables.length == 5);

        String expr = "a,b,c,d,e";
        variables = GenPolynomialTokenizer.expressionVariables(expr);
        //System.out.println("variables = " + Arrays.toString(variables) + ", len = " + variables.length);
        assertTrue("len == 5: ", variables.length == 5);

        expr = "b,c,d,e*a,b,c,d";
        variables = GenPolynomialTokenizer.expressionVariables(expr);
        //System.out.println("variables = " + Arrays.toString(variables) + ", len = " + variables.length);
        assertTrue("len == 5: ", variables.length == 5);

        expr = "b + c^3 - d + e*a - b/c +d";
        variables = GenPolynomialTokenizer.expressionVariables(expr);
        //System.out.println("variables = " + Arrays.toString(variables) + ", len = " + variables.length);
        assertTrue("len == 5: ", variables.length == 5);

        expr = "(b + c)^3 - { d + e*a } / [ b/c + d ] + (b + 3f + f*3 + f3)";
        variables = GenPolynomialTokenizer.expressionVariables(expr);
        //System.out.println("variables = " + Arrays.toString(variables) + ", len = " + variables.length);
        assertTrue("len == 7: ", variables.length == 7);
    }

}
