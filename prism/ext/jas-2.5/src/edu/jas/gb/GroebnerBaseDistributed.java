/*
 * $Id: GroebnerBaseDistributed.java 4502 2013-07-24 15:06:46Z kredel $
 */

package edu.jas.gb;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.Semaphore;

import org.apache.log4j.Logger;

import edu.jas.poly.ExpVector;
import edu.jas.poly.GenPolynomial;
import edu.jas.structure.RingElem;
import edu.jas.util.ChannelFactory;
import edu.jas.util.DistHashTable;
import edu.jas.util.DistHashTableServer;
import edu.jas.util.SocketChannel;
import edu.jas.util.Terminator;
import edu.jas.util.ThreadPool;


/**
 * Groebner Base distributed algorithm. Implements a distributed memory parallel
 * version of Groebner bases. Using pairlist class, distributed tasks do
 * reduction, one communication channel per task.
 * @param <C> coefficient type
 * @author Heinz Kredel
 * @deprecated use GroebnerBaseDistributedEC
 */

@Deprecated
public class GroebnerBaseDistributed<C extends RingElem<C>> extends GroebnerBaseAbstract<C> {


    private static final Logger logger = Logger.getLogger(GroebnerBaseDistributed.class);


    /**
     * Number of threads to use.
     */
    protected final int threads;


    /**
     * Default number of threads.
     */
    protected static final int DEFAULT_THREADS = 2;


    /**
     * Pool of threads to use. <b>Note:</b> No ComputerThreads for one node
     * tests
     */
    protected transient final ThreadPool pool;


    /**
     * Default server port.
     */
    protected static final int DEFAULT_PORT = 4711;


    /**
     * Server port to use.
     */
    protected final int port;


    /**
     * Constructor.
     */
    public GroebnerBaseDistributed() {
        this(DEFAULT_THREADS, DEFAULT_PORT);
    }


    /**
     * Constructor.
     * @param threads number of threads to use.
     */
    public GroebnerBaseDistributed(int threads) {
        this(threads, new ThreadPool(threads), DEFAULT_PORT);
    }


    /**
     * Constructor.
     * @param threads number of threads to use.
     * @param port server port to use.
     */
    public GroebnerBaseDistributed(int threads, int port) {
        this(threads, new ThreadPool(threads), port);
    }


    /**
     * Constructor.
     * @param threads number of threads to use.
     * @param pool ThreadPool to use.
     * @param port server port to use.
     */
    public GroebnerBaseDistributed(int threads, ThreadPool pool, int port) {
        this(threads, pool, new OrderedPairlist<C>(), port);
    }


    /**
     * Constructor.
     * @param threads number of threads to use.
     * @param pl pair selection strategy
     * @param port server port to use.
     */
    public GroebnerBaseDistributed(int threads, PairList<C> pl, int port) {
        this(threads, new ThreadPool(threads), pl, port);
    }


    /**
     * Constructor.
     * @param threads number of threads to use.
     * @param pool ThreadPool to use.
     * @param pl pair selection strategy
     * @param port server port to use.
     */
    public GroebnerBaseDistributed(int threads, ThreadPool pool, PairList<C> pl, int port) {
        super(new ReductionPar<C>(), pl);
        if (threads < 1) {
            threads = 1;
        }
        this.threads = threads;
        this.pool = pool;
        this.port = port;
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
     * Distributed Groebner base.
     * @param modv number of module variables.
     * @param F polynomial list.
     * @return GB(F) a Groebner base of F or null, if a IOException occurs.
     */
    public List<GenPolynomial<C>> GB(int modv, List<GenPolynomial<C>> F) {

        final int DL_PORT = port + 100;
        ChannelFactory cf = new ChannelFactory(port);
        cf.init();
        DistHashTableServer<Integer> dls = new DistHashTableServer<Integer>(DL_PORT);
        dls.init();
        logger.debug("dist-list server running");

        GenPolynomial<C> p;
        List<GenPolynomial<C>> G = new ArrayList<GenPolynomial<C>>();
        PairList<C> pairlist = null;
        boolean oneInGB = false;
        int l = F.size();
        int unused;
        ListIterator<GenPolynomial<C>> it = F.listIterator();
        while (it.hasNext()) {
            p = it.next();
            if (p.length() > 0) {
                p = p.monic();
                if (p.isONE()) {
                    oneInGB = true;
                    G.clear();
                    G.add(p);
                    //return G; must signal termination to others
                }
                if (!oneInGB) {
                    G.add(p);
                }
                if (pairlist == null) {
                    //pairlist = new OrderedPairlist<C>(modv, p.ring);
                    pairlist = strategy.create(modv, p.ring);
                    if (!p.ring.coFac.isField()) {
                        throw new IllegalArgumentException("coefficients not from a field");
                    }
                }
                // theList not updated here
                if (p.isONE()) {
                    unused = pairlist.putOne();
                } else {
                    unused = pairlist.put(p);
                }
            } else {
                l--;
            }
        }
        //if (l <= 1) {
        //return G; must signal termination to others
        //}

        logger.debug("looking for clients");
        //long t = System.currentTimeMillis();
        // now in DL, uses resend for late clients
        //while ( dls.size() < threads ) { sleep(); }

        DistHashTable<Integer, GenPolynomial<C>> theList = new DistHashTable<Integer, GenPolynomial<C>>(
                        "localhost", DL_PORT);
        theList.init();
        List<GenPolynomial<C>> al = pairlist.getList();
        for (int i = 0; i < al.size(); i++) {
            // no wait required
            GenPolynomial<C> nn = theList.put(Integer.valueOf(i), al.get(i));
            if (nn != null) {
                logger.info("double polynomials " + i + ", nn = " + nn + ", al(i) = " + al.get(i));
            }
        }

        Terminator fin = new Terminator(threads);
        ReducerServer<C> R;
        for (int i = 0; i < threads; i++) {
            R = new ReducerServer<C>(fin, cf, theList, G, pairlist);
            pool.addJob(R);
        }
        logger.debug("main loop waiting");
        fin.waitDone();
        int ps = theList.size();
        logger.debug("#distributed list = " + ps);
        // make sure all polynomials arrived: not needed in master
        // G = (ArrayList)theList.values();
        G = pairlist.getList();
        if (ps != G.size()) {
            logger.info("#distributed list = " + theList.size() + " #pairlist list = " + G.size());
        }
        long time = System.currentTimeMillis();
        List<GenPolynomial<C>> Gp;
        Gp = minimalGB(G); // not jet distributed but threaded
        time = System.currentTimeMillis() - time;
        logger.info("parallel gbmi = " + time);
        /*
        time = System.currentTimeMillis();
        G = GroebnerBase.<C>GBmi(G); // sequential
        time = System.currentTimeMillis() - time;
        logger.info("sequential gbmi = " + time);
        */
        G = Gp;
        logger.debug("cf.terminate()");
        cf.terminate();
        // no more required // pool.terminate();
        logger.info("theList.terminate()");
        theList.terminate();
        logger.info("dls.terminate()");
        dls.terminate();
        logger.info("" + pairlist);
        return G;
    }


    /**
     * GB distributed client.
     * @param host the server runns on.
     * @throws IOException
     */
    public void clientPart(String host) throws IOException {

        ChannelFactory cf = new ChannelFactory(port + 10); // != port for localhost
        cf.init();
        SocketChannel pairChannel = cf.getChannel(host, port);

        final int DL_PORT = port + 100;
        DistHashTable<Integer, GenPolynomial<C>> theList = new DistHashTable<Integer, GenPolynomial<C>>(host,
                        DL_PORT);
        theList.init();

        ReducerClient<C> R = new ReducerClient<C>(pairChannel, theList);
        R.run();

        pairChannel.close();
        theList.terminate();
        cf.terminate();
        return;
    }


    /**
     * Minimal ordered groebner basis.
     * @param Fp a Groebner base.
     * @return a reduced Groebner base of Fp.
     */
    @Override
    public List<GenPolynomial<C>> minimalGB(List<GenPolynomial<C>> Fp) {
        GenPolynomial<C> a;
        ArrayList<GenPolynomial<C>> G;
        G = new ArrayList<GenPolynomial<C>>(Fp.size());
        ListIterator<GenPolynomial<C>> it = Fp.listIterator();
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
        GenPolynomial<C> p;
        ArrayList<GenPolynomial<C>> F;
        F = new ArrayList<GenPolynomial<C>>(G.size());
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
                F.add(a);
            } else {
                // System.out.println("dropped " + a.length());
            }
        }
        G = F;
        if (G.size() <= 1) {
            return G;
        }
        Collections.reverse(G); // important for lex GB

        MiReducerServer<C>[] mirs = (MiReducerServer<C>[]) new MiReducerServer[G.size()];
        int i = 0;
        F = new ArrayList<GenPolynomial<C>>(G.size());
        while (G.size() > 0) {
            a = G.remove(0);
            // System.out.println("doing " + a.length());
            List<GenPolynomial<C>> R = new ArrayList<GenPolynomial<C>>(G.size() + F.size());
            R.addAll(G);
            R.addAll(F);
            mirs[i] = new MiReducerServer<C>(R, a);
            pool.addJob(mirs[i]);
            i++;
            F.add(a);
        }
        G = F;
        F = new ArrayList<GenPolynomial<C>>(G.size());
        for (i = 0; i < mirs.length; i++) {
            a = mirs[i].getNF();
            F.add(a);
        }
        return F;
    }

}


/**
 * Distributed server reducing worker threads.
 * @param <C> coefficient type
 */

class ReducerServer<C extends RingElem<C>> implements Runnable {


    private final Terminator pool;


    private final ChannelFactory cf;


    private SocketChannel pairChannel;


    private final DistHashTable<Integer, GenPolynomial<C>> theList;


    //private List<GenPolynomial<C>> G;
    private final PairList<C> pairlist;


    private static final Logger logger = Logger.getLogger(ReducerServer.class);


    ReducerServer(Terminator fin, ChannelFactory cf, DistHashTable<Integer, GenPolynomial<C>> dl,
                    List<GenPolynomial<C>> G, PairList<C> L) {
        pool = fin;
        this.cf = cf;
        theList = dl;
        //this.G = G;
        pairlist = L;
    }


    public void run() {
        logger.debug("reducer server running");
        try {
            pairChannel = cf.getChannel();
        } catch (InterruptedException e) {
            logger.debug("get pair channel interrupted");
            e.printStackTrace();
            return;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("pairChannel = " + pairChannel);
        }
        Pair<C> pair;
        //GenPolynomial<C> pi;
        //GenPolynomial<C> pj;
        //GenPolynomial<C> S;
        GenPolynomial<C> H = null;
        boolean set = false;
        boolean goon = true;
        int polIndex = -1;
        int red = 0;
        int sleeps = 0;

        // while more requests
        while (goon) {
            // receive request
            logger.debug("receive request");
            Object req = null;
            try {
                req = pairChannel.receive();
            } catch (IOException e) {
                goon = false;
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                goon = false;
                e.printStackTrace();
            }
            //logger.debug("received request, req = " + req);
            if (req == null) {
                goon = false;
                break;
            }
            if (!(req instanceof GBTransportMessReq)) {
                goon = false;
                break;
            }

            // find pair
            logger.debug("find pair");
            while (!pairlist.hasNext()) { // wait
                if (!set) {
                    pool.beIdle();
                    set = true;
                }
                if (!pool.hasJobs() && !pairlist.hasNext()) {
                    goon = false;
                    break;
                }
                try {
                    sleeps++;
                    if (sleeps % 10 == 0) {
                        logger.info(" reducer is sleeping");
                    }
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    goon = false;
                    break;
                }
            }
            if (!pairlist.hasNext() && !pool.hasJobs()) {
                goon = false;
                break; //continue; //break?
            }
            if (set) {
                set = false;
                pool.notIdle();
            }

            pair = pairlist.removeNext();
            /*
             * send pair to client, receive H
             */
            logger.debug("send pair = " + pair);
            GBTransportMess msg = null;
            if (pair != null) {
                msg = new GBTransportMessPairIndex(pair);
            } else {
                msg = new GBTransportMess(); //End();
                // goon ?= false;
            }
            try {
                pairChannel.send(msg);
            } catch (IOException e) {
                e.printStackTrace();
                goon = false;
                break;
            }
            logger.debug("#distributed list = " + theList.size());
            Object rh = null;
            try {
                rh = pairChannel.receive();
            } catch (IOException e) {
                e.printStackTrace();
                goon = false;
                break;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                goon = false;
                break;
            }
            //logger.debug("received H polynomial");
            if (rh == null) {
                if (pair != null) {
                    pair.setZero();
                }
            } else if (rh instanceof GBTransportMessPoly) {
                // update pair list
                red++;
                H = ((GBTransportMessPoly<C>) rh).pol;
                if (logger.isDebugEnabled()) {
                    logger.debug("H = " + H);
                }
                if (H == null) {
                    if (pair != null) {
                        pair.setZero();
                    }
                } else {
                    if (H.isZERO()) {
                        pair.setZero();
                    } else {
                        if (H.isONE()) {
                            // pool.allIdle();
                            polIndex = pairlist.putOne();
                            GenPolynomial<C> nn = theList.put(Integer.valueOf(polIndex), H);
                            if (nn != null) {
                                logger.info("double polynomials nn = " + nn + ", H = " + H);
                            }
                            goon = false;
                            break;
                        }
                        polIndex = pairlist.put(H);
                        // use putWait ? but still not all distributed
                        GenPolynomial<C> nn = theList.put(Integer.valueOf(polIndex), H);
                        if (nn != null) {
                            logger.info("double polynomials nn = " + nn + ", H = " + H);
                        }
                    }
                }
            }
        }
        logger.info("terminated, done " + red + " reductions");

        /*
         * send end mark to client
         */
        logger.debug("send end");
        try {
            pairChannel.send(new GBTransportMessEnd());
        } catch (IOException e) {
            if (logger.isDebugEnabled()) {
                e.printStackTrace();
            }
        }
        pool.beIdle();
        pairChannel.close();
    }

}


/**
 * Distributed clients reducing worker threads.
 */

class ReducerClient<C extends RingElem<C>> implements Runnable {


    private final SocketChannel pairChannel;


    private final DistHashTable<Integer, GenPolynomial<C>> theList;


    private final ReductionPar<C> red;


    private static final Logger logger = Logger.getLogger(ReducerClient.class);


    ReducerClient(SocketChannel pc, DistHashTable<Integer, GenPolynomial<C>> dl) {
        pairChannel = pc;
        theList = dl;
        red = new ReductionPar<C>();
    }


    public void run() {
        logger.debug("pairChannel = " + pairChannel + " reducer client running");
        Pair<C> pair = null;
        GenPolynomial<C> pi;
        GenPolynomial<C> pj;
        GenPolynomial<C> S;
        GenPolynomial<C> H = null;
        //boolean set = false;
        boolean goon = true;
        int reduction = 0;
        //int sleeps = 0;
        Integer pix;
        Integer pjx;

        while (goon) {
            /* protocol:
             * request pair, process pair, send result
             */
            // pair = (Pair) pairlist.removeNext();
            Object req = new GBTransportMessReq();
            logger.debug("send request = " + req);
            try {
                pairChannel.send(req);
            } catch (IOException e) {
                goon = false;
                e.printStackTrace();
                break;
            }
            logger.debug("receive pair, goon = " + goon);
            Object pp = null;
            try {
                pp = pairChannel.receive();
            } catch (IOException e) {
                goon = false;
                if (logger.isDebugEnabled()) {
                    e.printStackTrace();
                }
                break;
            } catch (ClassNotFoundException e) {
                goon = false;
                e.printStackTrace();
            }
            if (logger.isDebugEnabled()) {
                logger.debug("received pair = " + pp);
            }
            H = null;
            if (pp == null) { // should not happen
                continue;
            }
            if (pp instanceof GBTransportMessEnd) {
                goon = false;
                continue;
            }
            if (pp instanceof GBTransportMessPair || pp instanceof GBTransportMessPairIndex) {
                pi = pj = null;
                if (pp instanceof GBTransportMessPair) {
                    pair = ((GBTransportMessPair<C>) pp).pair;
                    if (pair != null) {
                        pi = pair.pi;
                        pj = pair.pj;
                        //logger.debug("pair: pix = " + pair.i 
                        //               + ", pjx = " + pair.j);
                    }
                }
                if (pp instanceof GBTransportMessPairIndex) {
                    pix = ((GBTransportMessPairIndex) pp).i;
                    pjx = ((GBTransportMessPairIndex) pp).j;
                    pi = (GenPolynomial<C>) theList.getWait(pix);
                    pj = (GenPolynomial<C>) theList.getWait(pjx);
                    //logger.info("pix = " + pix + ", pjx = " +pjx);
                }

                if (pi != null && pj != null) {
                    S = red.SPolynomial(pi, pj);
                    //System.out.println("S   = " + S);
                    if (S.isZERO()) {
                        // pair.setZero(); does not work in dist
                    } else {
                        if (logger.isDebugEnabled()) {
                            logger.debug("ht(S) = " + S.leadingExpVector());
                        }
                        H = red.normalform(theList, S);
                        reduction++;
                        if (H.isZERO()) {
                            // pair.setZero(); does not work in dist
                        } else {
                            H = H.monic();
                            if (logger.isInfoEnabled()) {
                                logger.info("ht(H) = " + H.leadingExpVector());
                            }
                        }
                    }
                }
            }

            // send H or must send null
            if (logger.isDebugEnabled()) {
                logger.debug("#distributed list = " + theList.size());
                logger.debug("send H polynomial = " + H);
            }
            try {
                pairChannel.send(new GBTransportMessPoly<C>(H));
            } catch (IOException e) {
                goon = false;
                e.printStackTrace();
            }
        }
        logger.info("terminated, done " + reduction + " reductions");
        pairChannel.close();
    }
}


