/*
 * $Id: GroebnerBasePseudoRecSeq.java 4794 2014-04-09 11:21:53Z kredel $
 */

package edu.jas.gbufd;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import org.apache.log4j.Logger;

import edu.jas.gb.GroebnerBaseAbstract;
import edu.jas.gb.OrderedPairlist;
import edu.jas.gb.Pair;
import edu.jas.gb.PairList;
import edu.jas.poly.GenPolynomial;
import edu.jas.poly.GenPolynomialRing;
import edu.jas.structure.GcdRingElem;
import edu.jas.structure.RingFactory;
import edu.jas.ufd.GCDFactory;
import edu.jas.ufd.GreatestCommonDivisorAbstract;


/**
 * Groebner Base with pseudo reduction sequential algorithm for integral
 * function coefficients. Implements polynomial fraction free coefficients
 * Groebner bases.
 * @param <C> base coefficient type
 * @author Heinz Kredel
 * 
 * @see edu.jas.application.GBAlgorithmBuilder
 * @see edu.jas.gbufd.GBFactory
 */

public class GroebnerBasePseudoRecSeq<C extends GcdRingElem<C>> extends
                GroebnerBaseAbstract<GenPolynomial<C>> {


    private static final Logger logger = Logger.getLogger(GroebnerBasePseudoRecSeq.class);


    private final boolean debug = logger.isDebugEnabled();


    /**
     * Greatest common divisor engine for coefficient content and primitive
     * parts.
     */
    protected final GreatestCommonDivisorAbstract<C> engine;


    /**
     * Pseudo reduction engine.
     */
    protected final PseudoReduction<GenPolynomial<C>> red;


    /**
     * Coefficient ring factory.
     */
    protected final RingFactory<GenPolynomial<C>> cofac;


    /**
     * Base coefficient ring factory.
     */
    protected final RingFactory<C> baseCofac;


    /**
     * Constructor.
     * @param rf coefficient ring factory.
     */
    public GroebnerBasePseudoRecSeq(RingFactory<GenPolynomial<C>> rf) {
        this(new PseudoReductionSeq<GenPolynomial<C>>(), rf, new OrderedPairlist<GenPolynomial<C>>(
                        new GenPolynomialRing<GenPolynomial<C>>(rf, 1))); // 1=hack
    }


    /**
     * Constructor.
     * @param rf coefficient ring factory.
     * @param pl pair selection strategy
     */
    public GroebnerBasePseudoRecSeq(RingFactory<GenPolynomial<C>> rf, PairList<GenPolynomial<C>> pl) {
        this(new PseudoReductionSeq<GenPolynomial<C>>(), rf, pl);
    }


    /**
     * Constructor.
     * @param red pseudo reduction engine. <b>Note:</b> red must be an instance
     *            of PseudoReductionSeq.
     * @param rf coefficient ring factory.
     * @param pl pair selection strategy
     */
    public GroebnerBasePseudoRecSeq(PseudoReduction<GenPolynomial<C>> red, RingFactory<GenPolynomial<C>> rf,
                    PairList<GenPolynomial<C>> pl) {
        super(red, pl);
        this.red = red;
        cofac = rf;
        GenPolynomialRing<C> rp = (GenPolynomialRing<C>) cofac;
        baseCofac = rp.coFac;
        //engine = (GreatestCommonDivisorAbstract<C>)GCDFactory.<C>getImplementation( baseCofac );
        //not used: 
        engine = GCDFactory.<C> getProxy(baseCofac);
    }


    /**
     * Groebner base using pairlist class.
     * @param modv module variable number.
     * @param F polynomial list.
     * @return GB(F) a Groebner base of F.
     */
    //@Override
    public List<GenPolynomial<GenPolynomial<C>>> GB(int modv, List<GenPolynomial<GenPolynomial<C>>> F) {
        GenPolynomial<GenPolynomial<C>> p;
        List<GenPolynomial<GenPolynomial<C>>> G = new ArrayList<GenPolynomial<GenPolynomial<C>>>();
        PairList<GenPolynomial<C>> pairlist = null;
        int l = F.size();
        ListIterator<GenPolynomial<GenPolynomial<C>>> it = F.listIterator();
        while (it.hasNext()) {
            p = it.next();
            if (p.length() > 0) {
                p = engine.recursivePrimitivePart(p); //p.monic();
                p = p.abs();
                if (p.isConstant()) {
                    G.clear();
                    G.add(p);
                    return G; // since no threads are activated
                }
                G.add(p);
                if (pairlist == null) {
                    //pairlist = new OrderedPairlist<GenPolynomial<C>>(modv, p.ring);
                    pairlist = strategy.create(modv, p.ring);
                }
                // putOne not required
                pairlist.put(p);
            } else {
                l--;
            }
        }
        if (l <= 1) {
            return G; // since no threads are activated
        }

        Pair<GenPolynomial<C>> pair;
        GenPolynomial<GenPolynomial<C>> pi;
        GenPolynomial<GenPolynomial<C>> pj;
        GenPolynomial<GenPolynomial<C>> S;
        GenPolynomial<GenPolynomial<C>> H;
        while (pairlist.hasNext()) {
            pair = pairlist.removeNext();
            if (pair == null)
                continue;

            pi = pair.pi;
            pj = pair.pj;
            if (debug) {
                logger.debug("pi    = " + pi);
                logger.debug("pj    = " + pj);
            }

            S = red.SPolynomial(pi, pj);
            if (S.isZERO()) {
                pair.setZero();
                continue;
            }
            if (debug) {
                logger.debug("ht(S) = " + S.leadingExpVector());
            }

            H = red.normalform(G, S);
            if (H.isZERO()) {
                pair.setZero();
                continue;
            }
            if (debug) {
                logger.debug("ht(H) = " + H.leadingExpVector());
            }
            H = engine.recursivePrimitivePart(H); //H.monic();
            H = H.abs();
            if (H.isConstant()) {
                G.clear();
                G.add(H);
                return G; // since no threads are activated
            }
            if (debug) {
                logger.debug("H = " + H);
            }
            if (H.length() > 0) {
                l++;
                G.add(H);
                pairlist.put(H);
            }
        }
        logger.debug("#sequential list = " + G.size());
        G = minimalGB(G);
        logger.info("" + pairlist);
        return G;
    }


    /**
     * Minimal ordered Groebner basis.
     * @param Gp a Groebner base.
     * @return a reduced Groebner base of Gp.
     */
    @Override
    public List<GenPolynomial<GenPolynomial<C>>> minimalGB(List<GenPolynomial<GenPolynomial<C>>> Gp) {
        if (Gp == null || Gp.size() <= 1) {
            return Gp;
        }
        // remove zero polynomials
        List<GenPolynomial<GenPolynomial<C>>> G = new ArrayList<GenPolynomial<GenPolynomial<C>>>(Gp.size());
        for (GenPolynomial<GenPolynomial<C>> a : Gp) {
            if (a != null && !a.isZERO()) { // always true in GB()
                // already positive a = a.abs();
                G.add(a);
            }
        }
        if (G.size() <= 1) {
            return G;
        }
        // remove top reducible polynomials
        GenPolynomial<GenPolynomial<C>> a;
        List<GenPolynomial<GenPolynomial<C>>> F;
        F = new ArrayList<GenPolynomial<GenPolynomial<C>>>(G.size());
        while (G.size() > 0) {
            a = G.remove(0);
            if (red.isTopReducible(G, a) || red.isTopReducible(F, a)) {
                // drop polynomial 
                if (debug) {
                    System.out.println("dropped " + a);
                    List<GenPolynomial<GenPolynomial<C>>> ff;
                    ff = new ArrayList<GenPolynomial<GenPolynomial<C>>>(G);
                    ff.addAll(F);
                    a = red.normalform(ff, a);
                    if (!a.isZERO()) {
                        System.out.println("error, nf(a) " + a);
                    }
                }
            } else {
                F.add(a);
            }
        }
        G = F;
        if (G.size() <= 1) {
            return G;
        }
        Collections.reverse(G); // important for lex GB
        // reduce remaining polynomials
        int len = G.size();
        int i = 0;
        while (i < len) {
            a = G.remove(0);
            //System.out.println("doing " + a.length());
            a = red.normalform(G, a);
            a = engine.recursivePrimitivePart(a); //a.monic(); was not required
            a = a.abs();
            //a = red.normalform( F, a );
            G.add(a); // adds as last
            i++;
        }
        return G;
    }

}
