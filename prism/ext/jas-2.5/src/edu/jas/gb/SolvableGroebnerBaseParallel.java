/*
 * $Id: SolvableGroebnerBaseParallel.java 4781 2014-04-06 21:50:57Z kredel $
 */

package edu.jas.gb;


import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.Semaphore;

import org.apache.log4j.Logger;

import edu.jas.poly.ExpVector;
import edu.jas.poly.GenSolvablePolynomial;
import edu.jas.poly.GenSolvablePolynomialRing;
import edu.jas.structure.RingElem;
import edu.jas.util.Terminator;
import edu.jas.util.ThreadPool;


/**
 * Solvable Groebner Base parallel algorithm. Implements a shared memory
 * parallel version of Groebner bases. Threads maintain pairlist.
 * @param <C> coefficient type
 * @author Heinz Kredel
 */

public class SolvableGroebnerBaseParallel<C extends RingElem<C>> extends SolvableGroebnerBaseAbstract<C> {


    private static final Logger logger = Logger.getLogger(SolvableGroebnerBaseParallel.class);


    //private static final boolean debug = logger.isDebugEnabled();


    /**
     * Number of threads to use.
     */
    protected final int threads;


    /**
     * Pool of threads to use.
     */
    protected transient final ThreadPool pool;


    /**
     * Constructor.
     */
    public SolvableGroebnerBaseParallel() {
        this(2);
    }


    /**
     * Constructor.
     * @param threads number of threads to use.
     */
    public SolvableGroebnerBaseParallel(int threads) {
        this(threads, new ThreadPool(threads));
    }


    /**
     * Constructor.
     * @param threads number of threads to use.
     * @param pool ThreadPool to use.
     */
    public SolvableGroebnerBaseParallel(int threads, ThreadPool pool) {
        this(threads, pool, new SolvableReductionPar<C>());
    }


    /**
     * Constructor.
     * @param threads number of threads to use.
     * @param sred parallelism aware reduction engine
     */
    public SolvableGroebnerBaseParallel(int threads, SolvableReduction<C> sred) {
        this(threads, new ThreadPool(threads), sred);
    }


    /**
     * Constructor.
     * @param threads number of threads to use.
     * @param pl pair selection strategy
     */
    public SolvableGroebnerBaseParallel(int threads, PairList<C> pl) {
        this(threads, new ThreadPool(threads), new SolvableReductionPar<C>(), pl);
    }


    /**
     * Constructor.
     * @param threads number of threads to use.
     * @param sred parallelism aware reduction engine
     * @param pl pair selection strategy
     */
    public SolvableGroebnerBaseParallel(int threads, SolvableReduction<C> sred, PairList<C> pl) {
        this(threads, new ThreadPool(threads), sred, pl);
    }


    /**
     * Constructor.
     * @param threads number of threads to use.
     * @param pool ThreadPool to use.
     * @param sred parallelism aware reduction engine
     */
    public SolvableGroebnerBaseParallel(int threads, ThreadPool pool, SolvableReduction<C> sred) {
        this(threads, pool, sred, new OrderedPairlist<C>());
    }


    /**
     * Constructor.
     * @param threads number of threads to use.
     * @param pool ThreadPool to use.
     * @param sred parallelism aware reduction engine
     * @param pl pair selection strategy
     */
    public SolvableGroebnerBaseParallel(int threads, ThreadPool pool, SolvableReduction<C> sred,
                    PairList<C> pl) {
        super(sred, pl);
        if (!(sred instanceof SolvableReductionPar)) {
            logger.warn("parallel GB should use parallel aware reduction");
        }
        if (threads < 1) {
            threads = 1;
        }
        this.threads = threads;
        this.pool = pool;
    }


    /**
     * Cleanup and terminate ThreadPool.
     */
    @Override
    public void terminate() {
        if (pool == null) {
            return;
        }
        pool.terminate();
    }


    /**
     * Parallel Groebner base using sequential pair order class. Threads
     * maintain pairlist.
     * @param modv number of module variables.
     * @param F polynomial list.
     * @return GB(F) a Groebner base of F.
     */
    public List<GenSolvablePolynomial<C>> leftGB(int modv, List<GenSolvablePolynomial<C>> F) {
        GenSolvablePolynomial<C> p;
        List<GenSolvablePolynomial<C>> G = new ArrayList<GenSolvablePolynomial<C>>();
        PairList<C> pairlist = null;
        int l = F.size();
        ListIterator<GenSolvablePolynomial<C>> it = F.listIterator();
        while (it.hasNext()) {
            p = it.next();
            if (p.length() > 0) {
                p = p.monic();
                if (p.isONE()) {
                    G.clear();
                    G.add(p);
                    return G; // since no threads activated jet
                }
                G.add(p);
                if (pairlist == null) {
                    //pairlist = new OrderedPairlist<C>( modv, p.ring );
                    pairlist = strategy.create(modv, p.ring);
                    if (!p.ring.coFac.isField()) {
                        throw new IllegalArgumentException("coefficients not from a field");
                    }
                }
                // putOne not required
                pairlist.put(p);
            } else {
                l--;
            }
        }
        if (l <= 1) {
            return G; // since no threads activated jet
        }

        Terminator fin = new Terminator(threads);
        LeftSolvableReducer<C> R;
        for (int i = 0; i < threads; i++) {
            R = new LeftSolvableReducer<C>(fin, G, pairlist);
            pool.addJob(R);
        }
        fin.waitDone();
        logger.debug("#parallel list = " + G.size());
        G = leftMinimalGB(G);
        // not in this context // pool.terminate();
        logger.info("" + pairlist);
        return G;
    }


    /**
     * Minimal ordered groebner basis, parallel.
     * @param Fp a Groebner base.
     * @return minimalGB(F) a minimal Groebner base of Fp.
     */
    @Override
    public List<GenSolvablePolynomial<C>> leftMinimalGB(List<GenSolvablePolynomial<C>> Fp) {
        GenSolvablePolynomial<C> a;
        ArrayList<GenSolvablePolynomial<C>> G;
        G = new ArrayList<GenSolvablePolynomial<C>>(Fp.size());
        ListIterator<GenSolvablePolynomial<C>> it = Fp.listIterator();
        while (it.hasNext()) {
            a = it.next();
            if (a.length() != 0) { // always true
                // already monic  a = a.monic();
                G.add(a);
            }
        }
        if (G.size() <= 1) {
            return G;
        }

        ExpVector e;
        ExpVector f;
        GenSolvablePolynomial<C> p;
        ArrayList<GenSolvablePolynomial<C>> F;
        F = new ArrayList<GenSolvablePolynomial<C>>(G.size());
        boolean mt;
        while (G.size() > 0) {
            a = G.remove(0);
            e = a.leadingExpVector();

            it = G.listIterator();
            mt = false;
            while (it.hasNext() && !mt) {
                p = it.next();
                f = p.leadingExpVector();
                mt = e.multipleOf(f);
            }
            it = F.listIterator();
            while (it.hasNext() && !mt) {
                p = it.next();
                f = p.leadingExpVector();
                mt = e.multipleOf(f);
            }
            if (!mt) {
                F.add(a); // no thread at this point
            } else {
                // System.out.println("dropped " + a.length());
            }
        }
        G = F;
        if (G.size() <= 1) {
            return G;
        }

        SolvableMiReducer<C>[] mirs = (SolvableMiReducer<C>[]) new SolvableMiReducer[G.size()];
        int i = 0;
        F = new ArrayList<GenSolvablePolynomial<C>>(G.size());
        while (G.size() > 0) {
            a = G.remove(0);
            // System.out.println("doing " + a.length());
            List<GenSolvablePolynomial<C>> R = new ArrayList<GenSolvablePolynomial<C>>(G.size() + F.size());
            R.addAll(G);
            R.addAll(F);
            mirs[i] = new SolvableMiReducer<C>(R, a);
            pool.addJob(mirs[i]);
            i++;
            F.add(a);
        }
        G = F;
        F = new ArrayList<GenSolvablePolynomial<C>>(G.size());
        for (i = 0; i < mirs.length; i++) {
            a = mirs[i].getNF();
            F.add(a);
        }
        return F;
    }


    /**
     * Solvable Extended Groebner base using critical pair class.
     * @param modv module variable number.
     * @param F solvable polynomial list.
     * @return a container for an extended left Groebner base of F.
     */
    public SolvableExtendedGB<C> extLeftGB(int modv, List<GenSolvablePolynomial<C>> F) {
        throw new UnsupportedOperationException("parallel extLeftGB not implemented");
    }


    /**
     * Twosided Groebner base using pairlist class.
     * @param modv number of module variables.
     * @param Fp solvable polynomial list.
     * @return tsGB(Fp) a twosided Groebner base of F.
     */
    @SuppressWarnings("unchecked")
    public List<GenSolvablePolynomial<C>> twosidedGB(int modv, List<GenSolvablePolynomial<C>> Fp) {
        if (Fp == null || Fp.size() == 0) { // 0 not 1
            return new ArrayList<GenSolvablePolynomial<C>>();
        }
        GenSolvablePolynomialRing<C> fac = Fp.get(0).ring; // assert != null
        //List<GenSolvablePolynomial<C>> X = generateUnivar( modv, Fp );
        List<GenSolvablePolynomial<C>> X = fac.univariateList(modv);
        //System.out.println("X univ = " + X);
        List<GenSolvablePolynomial<C>> F = new ArrayList<GenSolvablePolynomial<C>>(Fp.size() * (1 + X.size()));
        F.addAll(Fp);
        GenSolvablePolynomial<C> p, x, q;
        for (int i = 0; i < Fp.size(); i++) {
            p = Fp.get(i);
            for (int j = 0; j < X.size(); j++) {
                x = X.get(j);
                q = p.multiply(x);
                q = sred.leftNormalform(F, q);
                if (!q.isZERO()) {
                    F.add(q);
                }
            }
        }
        //System.out.println("F generated = " + F);
        List<GenSolvablePolynomial<C>> G = new ArrayList<GenSolvablePolynomial<C>>();
        PairList<C> pairlist = null;
        int l = F.size();
        ListIterator<GenSolvablePolynomial<C>> it = F.listIterator();
        while (it.hasNext()) {
            p = it.next();
            if (p.length() > 0) {
                p = p.monic();
                if (p.isONE()) {
                    G.clear();
                    G.add(p);
                    return G; // since no threads are activated
                }
                G.add(p);
                if (pairlist == null) {
                    //pairlist = new OrderedPairlist<C>( modv, p.ring );
                    pairlist = strategy.create(modv, p.ring);
                    if (!p.ring.coFac.isField()) {
                        throw new IllegalArgumentException("coefficients not from a field");
                    }
                }
                // putOne not required
                pairlist.put(p);
            } else {
                l--;
            }
        }
        //System.out.println("G to check = " + G);
        if (l <= 1) { // 1 ok
            return G; // since no threads are activated
        }
        Terminator fin = new Terminator(threads);
        TwosidedSolvableReducer<C> R;
        for (int i = 0; i < threads; i++) {
            R = new TwosidedSolvableReducer<C>(fin, X, G, pairlist);
            pool.addJob(R);
        }
        fin.waitDone();
        logger.debug("#parallel list = " + G.size());
        G = leftMinimalGB(G);
        // not in this context // pool.terminate();
        logger.info("" + pairlist);
        return G;
    }

}


/**
 * Reducing left worker threads.
 * @param <C> coefficient type
 */
class LeftSolvableReducer<C extends RingElem<C>> implements Runnable {


    private final List<GenSolvablePolynomial<C>> G;


    private final PairList<C> pairlist;


    private final Terminator pool;


    private final SolvableReductionPar<C> sred;


    private static final Logger logger = Logger.getLogger(LeftSolvableReducer.class);


    private static final boolean debug = logger.isDebugEnabled();


    LeftSolvableReducer(Terminator fin, List<GenSolvablePolynomial<C>> G, PairList<C> L) {
        pool = fin;
        this.G = G;
        pairlist = L;
        sred = new SolvableReductionPar<C>();
    }


    @SuppressWarnings("unchecked")
    public void run() {
        Pair<C> pair;
        GenSolvablePolynomial<C> S;
        GenSolvablePolynomial<C> H;
        boolean set = false;
        int reduction = 0;
        int sleeps = 0;
        while (pairlist.hasNext() || pool.hasJobs()) {
            while (!pairlist.hasNext()) {
                // wait
                pool.beIdle();
                set = true;
                try {
                    sleeps++;
                    if (sleeps % 10 == 0) {
                        logger.info(" reducer is sleeping");
                    } else {
                        logger.debug("r");
                    }
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    pool.allIdle();
                    logger.info("shutdown " + pool + " after: " + e);
                    //throw new RuntimeException("interrupt 1 in pairlist.hasNext loop");
                    break;
                }
                if (Thread.currentThread().isInterrupted()) {
                    //pool.initIdle(1);
                    pool.allIdle();
                    logger.info("shutdown after .isInterrupted(): " + pool);
                    //throw new RuntimeException("interrupt 2 in pairlist.hasNext loop");
                    break;
                }
                if (!pool.hasJobs()) {
                    break;
                }
            }
            if (!pairlist.hasNext() && !pool.hasJobs()) {
                break;
            }
            if (set) {
                pool.notIdle();
                set = false;
            }
            pair = pairlist.removeNext();
            if (pair == null) {
                continue;
            }
            if (debug) {
                logger.debug("pi = " + pair.pi);
                logger.debug("pj = " + pair.pj);
            }
            S = sred.leftSPolynomial((GenSolvablePolynomial<C>) pair.pi, (GenSolvablePolynomial<C>) pair.pj);
            if (S.isZERO()) {
                continue;
            }
            if (debug) {
                logger.debug("ht(S) = " + S.leadingExpVector());
            }
            H = sred.leftNormalform(G, S); //mod
            reduction++;
            if (H.isZERO()) {
                continue;
            }
            if (debug) {
                logger.debug("ht(H) = " + H.leadingExpVector());
            }
            H = H.monic();
            // System.out.println("H   = " + H);
            if (H.isONE()) {
                pairlist.putOne(); // not really required
                synchronized (G) {
                    G.clear();
                    G.add(H);
                }
                pool.allIdle();
                return;
            }
            if (debug) {
                logger.debug("H = " + H);
            }
            synchronized (G) {
                G.add(H);
            }
            pairlist.put(H);
        }
        logger.info("terminated, done " + reduction + " reductions");
    }
}


/**
 * Reducing twosided worker threads.
 * @param <C> coefficient type
 */
class TwosidedSolvableReducer<C extends RingElem<C>> implements Runnable {


    private final List<GenSolvablePolynomial<C>> X;


    private final List<GenSolvablePolynomial<C>> G;


    private final PairList<C> pairlist;


    private final Terminator pool;


    private final SolvableReductionPar<C> sred;


    private static final Logger logger = Logger.getLogger(TwosidedSolvableReducer.class);


    private static final boolean debug = logger.isDebugEnabled();


    TwosidedSolvableReducer(Terminator fin, List<GenSolvablePolynomial<C>> X,
                    List<GenSolvablePolynomial<C>> G, PairList<C> L) {
        pool = fin;
        this.X = X;
        this.G = G;
        pairlist = L;
        sred = new SolvableReductionPar<C>();
    }


    public void run() {
        GenSolvablePolynomial<C> p, x;
        Pair<C> pair;
        GenSolvablePolynomial<C> S;
        GenSolvablePolynomial<C> H;
        boolean set = false;
        int reduction = 0;
        int sleeps = 0;
        while (pairlist.hasNext() || pool.hasJobs()) {
            while (!pairlist.hasNext()) {
                // wait
                pool.beIdle();
                set = true;
                try {
                    sleeps++;
                    if (sleeps % 10 == 0) {
                        logger.info(" reducer is sleeping");
                    } else {
                        logger.debug("r");
                    }
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    break;
                }
                if (!pool.hasJobs()) {
                    break;
                }
            }
            if (!pairlist.hasNext() && !pool.hasJobs()) {
                break;
            }
            if (set) {
                pool.notIdle();
                set = false;
            }
            pair = pairlist.removeNext();
            if (pair == null) {
                continue;
            }
            if (debug) {
                logger.debug("pi = " + pair.pi);
                logger.debug("pj = " + pair.pj);
            }
            S = sred.leftSPolynomial((GenSolvablePolynomial<C>) pair.pi, (GenSolvablePolynomial<C>) pair.pj);
            if (S.isZERO()) {
                continue;
            }
            if (debug) {
                logger.debug("ht(S) = " + S.leadingExpVector());
            }
            H = sred.leftNormalform(G, S); //mod
            reduction++;
            if (H.isZERO()) {
                continue;
            }
            if (debug) {
                logger.debug("ht(H) = " + H.leadingExpVector());
            }
            H = H.monic();
            // System.out.println("H   = " + H);
            if (H.isONE()) {
                pairlist.putOne(); // not really required
                synchronized (G) {
                    G.clear();
                    G.add(H);
                }
                pool.allIdle();
                return;
            }
            if (debug) {
                logger.debug("H = " + H);
            }
            synchronized (G) {
                G.add(H);
            }
            pairlist.put(H);
            for (int j = 0; j < X.size(); j++) {
                x = X.get(j);
                p = H.multiply(x);
                p = sred.leftNormalform(G, p);
                if (!p.isZERO()) {
                    p = p.monic();
                    if (p.isONE()) {
                        synchronized (G) {
                            G.clear();
                            G.add(p);
                        }
                        pool.allIdle();
                        return;
                    }
                    synchronized (G) {
                        G.add(p);
                    }
                    pairlist.put(p);
                }
            }
        }
        logger.info("terminated, done " + reduction + " reductions");
    }
}


/**
 * Reducing worker threads for minimal GB.
 * @param <C> coefficient type
 */
class SolvableMiReducer<C extends RingElem<C>> implements Runnable {


    private final List<GenSolvablePolynomial<C>> G;


    private GenSolvablePolynomial<C> H;


    private final SolvableReductionPar<C> sred;


    private final Semaphore done = new Semaphore(0);


    private static final Logger logger = Logger.getLogger(SolvableMiReducer.class);


    private static final boolean debug = logger.isDebugEnabled();


    SolvableMiReducer(List<GenSolvablePolynomial<C>> G, GenSolvablePolynomial<C> p) {
        this.G = G;
        H = p;
        sred = new SolvableReductionPar<C>();
    }


    /**
     * getNF. Blocks until the normal form is computed.
     * @return the computed normal form.
     */
    public GenSolvablePolynomial<C> getNF() {
        try {
            done.acquire(); //done.P();
        } catch (InterruptedException e) {
        }
        return H;
    }


    public void run() {
        if (debug) {
            logger.debug("ht(H) = " + H.leadingExpVector());
        }
        H = sred.leftNormalform(G, H); //mod
        done.release(); //done.V();
        if (debug) {
            logger.debug("ht(H) = " + H.leadingExpVector());
        }
        // H = H.monic();
    }

}
