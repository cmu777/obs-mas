/*
 * $Id: GroebnerBaseDistHybridTest.java 4010 2012-07-21 20:39:56Z kredel $
 */

package edu.jas.gb;


//import edu.jas.poly.GroebnerBase;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.log4j.BasicConfigurator; //import org.apache.log4j.Logger;

import edu.jas.kern.ComputerThreads;
import edu.jas.arith.BigRational;

import edu.jas.poly.GenPolynomial;
import edu.jas.poly.GenPolynomialRing;
import edu.jas.poly.GenPolynomialTokenizer;
import edu.jas.poly.PolynomialList;

import edu.jas.structure.RingElem;


/**
 * Distributed hybrid architecture GroebnerBase tests with JUnit.
 * @author Heinz Kredel
 */

public class GroebnerBaseDistHybridTest extends TestCase {


    //private static final Logger logger = Logger.getLogger(GroebnerBaseDistHybridTest.class);

    /**
     * main
     */
    public static void main(String[] args) {
        BasicConfigurator.configure();
        junit.textui.TestRunner.run(suite());
        //ComputerThreads.terminate();
    }


    /**
     * Constructs a <CODE>GroebnerBaseDistHybridTest</CODE> object.
     * @param name String.
     */
    public GroebnerBaseDistHybridTest(String name) {
        super(name);
    }


    /**
     * suite.
     */
    public static Test suite() {
        TestSuite suite = new TestSuite(GroebnerBaseDistHybridTest.class);
        return suite;
    }


    int port = 4711;


    String host = "localhost";


    GenPolynomialRing<BigRational> fac;


    List<GenPolynomial<BigRational>> L;


    PolynomialList<BigRational> F;


    List<GenPolynomial<BigRational>> G;


    GroebnerBase<BigRational> bbseq;


    GroebnerBaseDistributedHybrid<BigRational> bbdisthybs;


    GroebnerBaseDistributedHybrid<BigRational> bbdisthyb;


    GenPolynomial<BigRational> a;


    GenPolynomial<BigRational> b;


    GenPolynomial<BigRational> c;


    GenPolynomial<BigRational> d;


    GenPolynomial<BigRational> e;


    int rl = 3; //4; //3; 


    int kl = 4;


    int ll = 7;


    int el = 3;


    float q = 0.2f; //0.4f


    int threads = 2;


    int threadsPerNode = 2;


    @Override
    protected void setUp() {
        BigRational coeff = new BigRational(9);
        fac = new GenPolynomialRing<BigRational>(coeff, rl);
        a = b = c = d = e = null;
        bbseq = new GroebnerBaseSeq<BigRational>();
        bbdisthybs = null; //new GroebnerBaseDistributed<BigRational>(threads, port);
        bbdisthyb = null; //new GroebnerBaseDistributedHybrid<BigRational>(threads, threadsPerNode, port);
    }


    @Override
    protected void tearDown() {
        a = b = c = d = e = null;
        fac = null;
        bbseq = null;
        bbdisthybs.terminate();
        bbdisthybs = null;
        bbdisthyb.terminate();
        bbdisthyb = null;
        ComputerThreads.terminate();
    }


    /**
     * Helper method to start threads with distributed clients.
     * 
     */
    Thread[] startThreads() {
        Thread[] clients = new Thread[threads];
        for (int t = 0; t < threads; t++) {
            clients[t] = new Thread(new JunitClientHybrid(threadsPerNode, host, port));
            clients[t].start();
        }
        return clients;
    }


    /**
     * Helper method to stop threads with distributed clients.
     * 
     */
    void stopThreads(Thread[] clients) {
        for (int t = 0; t < threads; t++) {
            try {
                while ( clients[t].isAlive() ) {
                        clients[t].interrupt(); 
                        clients[t].join(100);
                }
            } catch (InterruptedException e) {
            }
        }
    }


    /**
     * Test distributed hybrid GBase.
     * 
     */
    public void testDistributedHybridGBase() {

        bbdisthybs = new GroebnerBaseDistributedHybrid<BigRational>(threads, threadsPerNode, port);
        bbdisthyb = new GroebnerBaseDistributedHybrid<BigRational>(threads, threadsPerNode, new OrderedSyzPairlist<BigRational>(), port);

        Thread[] clients;

        L = new ArrayList<GenPolynomial<BigRational>>();

        a = fac.random(kl, ll, el, q);
        b = fac.random(kl, ll, el, q);
        c = fac.random(kl, ll, el, q);
        d = fac.random(kl, ll, el, q);
        e = d; //fac.random(kl, ll, el, q );

        assertTrue("not isZERO( a )", !a.isZERO());
        L.add(a);

        clients = startThreads();
        L = bbdisthyb.GB(L);
        stopThreads(clients);
        assertTrue("isGB( { a } )", bbseq.isGB(L));
        //System.out.println("L = " + L.size() );

        assertTrue("not isZERO( b )", !b.isZERO());
        L.add(b);
        //System.out.println("L = " + L.size() );

        clients = startThreads();
        L = bbdisthyb.GB(L);
        stopThreads(clients);
        assertTrue("isGB( { a, b } )", bbseq.isGB(L));
        //System.out.println("L = " + L.size() );

        assertTrue("not isZERO( c )", !c.isZERO());
        L.add(c);

        clients = startThreads();
        L = bbdisthyb.GB(L);
        stopThreads(clients);
        assertTrue("isGB( { a, b, c } )", bbseq.isGB(L));
        //System.out.println("L = " + L.size() );

        assertTrue("not isZERO( d )", !d.isZERO());
        L.add(d);

        clients = startThreads();
        L = bbdisthyb.GB(L);
        stopThreads(clients);
        assertTrue("isGB( { a, b, c, d } )", bbseq.isGB(L));
        //System.out.println("L = " + L.size() );

        assertTrue("not isZERO( e )", !e.isZERO());
        L.add(e);

        clients = startThreads();
        L = bbdisthyb.GB(L);
        stopThreads(clients);
        assertTrue("isGB( { a, b, c, d, e } )", bbseq.isGB(L));
        //System.out.println("L = " + L.size() );
    }


    /**
     * Test Trinks7 GBase.
     * 
     */
    @SuppressWarnings("unchecked")
    public void testTrinks7GBase() {
        bbdisthybs = new GroebnerBaseDistributedHybrid<BigRational>(threads, threadsPerNode, port);
        bbdisthyb = new GroebnerBaseDistributedHybrid<BigRational>(threads, threadsPerNode, new OrderedSyzPairlist<BigRational>(), port);
        Thread[] clients;
        String exam = "(B,S,T,Z,P,W) L " + "( " + "( 45 P + 35 S - 165 B - 36 ), "
                + "( 35 P + 40 Z + 25 T - 27 S ), " + "( 15 W + 25 S P + 30 Z - 18 T - 165 B**2 ), "
                + "( - 9 W + 15 T P + 20 S Z ), " + "( P W + 2 T Z - 11 B**3 ), "
                + "( 99 W - 11 B S + 3 B**2 ), " + "( B**2 + 33/50 B + 2673/10000 ) " + ") ";
        Reader source = new StringReader(exam);
        GenPolynomialTokenizer parser = new GenPolynomialTokenizer(source);
        try {
            F = (PolynomialList<BigRational>) parser.nextPolynomialSet();
        } catch (IOException e) {
            fail("" + e);
        }
        //System.out.println("F = " + F);

        clients = startThreads();
        G = bbdisthyb.GB(F.list);
        stopThreads(clients);

        assertTrue("isGB( GB(Trinks7) )", bbseq.isGB(G));
        assertEquals("#GB(Trinks7) == 6", 6, G.size());
        //PolynomialList<BigRational> trinks = new PolynomialList<BigRational>(F.ring, G);
        //System.out.println("G = " + trinks);

    }


    /**
     * Test Trinks7 GBase.
     * 
     */
    @SuppressWarnings("unchecked")
    public void testTrinks7GBase_t1_p4() {

        //bbdisthyb.terminate();
        threads = 1;
        threadsPerNode = 4;
        bbdisthybs = new GroebnerBaseDistributedHybrid<BigRational>(threads, threadsPerNode, port);
        bbdisthyb = new GroebnerBaseDistributedHybrid<BigRational>(threads, threadsPerNode, new OrderedSyzPairlist<BigRational>(), port);

        Thread[] clients;
        String exam = "(B,S,T,Z,P,W) L " + "( " + "( 45 P + 35 S - 165 B - 36 ), "
                + "( 35 P + 40 Z + 25 T - 27 S ), " + "( 15 W + 25 S P + 30 Z - 18 T - 165 B**2 ), "
                + "( - 9 W + 15 T P + 20 S Z ), " + "( P W + 2 T Z - 11 B**3 ), "
                + "( 99 W - 11 B S + 3 B**2 ), " + "( B**2 + 33/50 B + 2673/10000 ) " + ") ";
        Reader source = new StringReader(exam);
        GenPolynomialTokenizer parser = new GenPolynomialTokenizer(source);
        try {
            F = (PolynomialList<BigRational>) parser.nextPolynomialSet();
        } catch (IOException e) {
            fail("" + e);
        }
        //System.out.println("F = " + F);

        clients = startThreads();
        G = bbdisthyb.GB(F.list);
        stopThreads(clients);

        assertTrue("isGB( GB(Trinks7) )", bbseq.isGB(G));
        assertEquals("#GB(Trinks7) == 6", 6, G.size());
        //PolynomialList<BigRational> trinks = new PolynomialList<BigRational>(F.ring, G);
        //System.out.println("G = " + trinks);
    }


    /**
     * Test Trinks7 GBase.
     * 
     */
    @SuppressWarnings("unchecked")
    public void testTrinks7GBase_t4_p1() {

        //bbdisthyb.terminate();
        threads = 4;
        threadsPerNode = 1;
        bbdisthybs = new GroebnerBaseDistributedHybrid<BigRational>(threads, threadsPerNode, port);
        bbdisthyb = new GroebnerBaseDistributedHybrid<BigRational>(threads, threadsPerNode, new OrderedSyzPairlist<BigRational>(), port);

        Thread[] clients;
        String exam = "(B,S,T,Z,P,W) L " + "( " + "( 45 P + 35 S - 165 B - 36 ), "
                + "( 35 P + 40 Z + 25 T - 27 S ), " + "( 15 W + 25 S P + 30 Z - 18 T - 165 B**2 ), "
                + "( - 9 W + 15 T P + 20 S Z ), " + "( P W + 2 T Z - 11 B**3 ), "
                + "( 99 W - 11 B S + 3 B**2 ), " + "( B**2 + 33/50 B + 2673/10000 ) " + ") ";
        Reader source = new StringReader(exam);
        GenPolynomialTokenizer parser = new GenPolynomialTokenizer(source);
        try {
            F = (PolynomialList<BigRational>) parser.nextPolynomialSet();
        } catch (IOException e) {
            fail("" + e);
        }
        //System.out.println("F = " + F);

        clients = startThreads();
        G = bbdisthyb.GB(F.list);
        stopThreads(clients);

        assertTrue("isGB( GB(Trinks7) )", bbseq.isGB(G));
        assertEquals("#GB(Trinks7) == 6", 6, G.size());
        //PolynomialList<BigRational> trinks = new PolynomialList<BigRational>(F.ring, G);
        //System.out.println("G = " + trinks);
    }


    /**
     * Test Trinks7 GBase.
     * 
     */
    @SuppressWarnings("unchecked")
    public void testTrinks7GBase_t2_p2() {

        //bbdisthyb.terminate();
        threads = 2;
        threadsPerNode = 4;
        bbdisthybs = new GroebnerBaseDistributedHybrid<BigRational>(threads, threadsPerNode, port);
        bbdisthyb = new GroebnerBaseDistributedHybrid<BigRational>(threads, threadsPerNode, new OrderedSyzPairlist<BigRational>(), port);

        Thread[] clients;
        String exam = "(B,S,T,Z,P,W) L " + "( " + "( 45 P + 35 S - 165 B - 36 ), "
                + "( 35 P + 40 Z + 25 T - 27 S ), " + "( 15 W + 25 S P + 30 Z - 18 T - 165 B**2 ), "
                + "( - 9 W + 15 T P + 20 S Z ), " + "( P W + 2 T Z - 11 B**3 ), "
                + "( 99 W - 11 B S + 3 B**2 ), " + ") ";
        //      + "( 99 W - 11 B S + 3 B**2 ), " + "( B**2 + 33/50 B + 2673/10000 ) " + ") ";
        Reader source = new StringReader(exam);
        GenPolynomialTokenizer parser = new GenPolynomialTokenizer(source);
        try {
            F = (PolynomialList<BigRational>) parser.nextPolynomialSet();
        } catch (IOException e) {
            fail("" + e);
        }
        //System.out.println("F = " + F);

        clients = startThreads();
        G = bbdisthyb.GB(F.list);
        stopThreads(clients);

        assertTrue("isGB( GB(Trinks7) )", bbseq.isGB(G));
        assertEquals("#GB(Trinks7) == 6", 6, G.size());
        //PolynomialList<BigRational> trinks = new PolynomialList<BigRational>(F.ring, G);
        //System.out.println("G = " + trinks);
    }

}


/**
 * Unit Test client to be executed by test threads.
 */

class JunitClientHybrid<C extends RingElem<C>> implements Runnable {


    private final int threadsPerNode;


    private final String host;


    private final int port;


    JunitClientHybrid(int threadsPerNode, String host, int port) {
        this.threadsPerNode = threadsPerNode;
        this.host = host;
        this.port = port;
    }


    public void run() {
        GroebnerBaseDistributedHybrid<C> bbd;
        bbd = new GroebnerBaseDistributedHybrid<C>(1, threadsPerNode, null, null, port);
        try {
            bbd.clientPart(host);
        } catch (IOException ignored) {
        }
        bbd.terminate();
    }
}
