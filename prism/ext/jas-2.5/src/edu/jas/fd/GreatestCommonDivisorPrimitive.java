/*
 * $Id: GreatestCommonDivisorPrimitive.java 4820 2014-04-21 07:59:48Z kredel $
 */

package edu.jas.fd;


import org.apache.log4j.Logger;

import edu.jas.poly.GenPolynomial;
import edu.jas.poly.GenSolvablePolynomial;
import edu.jas.poly.PolyUtil;
import edu.jas.structure.GcdRingElem;


/**
 * (Non-unique) factorization domain greatest common divisor common algorithms
 * with primitive polynomial remainder sequence.
 * @param <C> coefficient type
 * @author Heinz Kredel
 */

public class GreatestCommonDivisorPrimitive<C extends GcdRingElem<C>> extends
                GreatestCommonDivisorAbstract<C> {


    private static final Logger logger = Logger.getLogger(GreatestCommonDivisorPrimitive.class);


    private final boolean debug = true; //logger.isDebugEnabled();


    /**
     * Univariate GenSolvablePolynomial greatest common divisor. Uses
     * pseudoRemainder for remainder.
     * @param P univariate GenSolvablePolynomial.
     * @param S univariate GenSolvablePolynomial.
     * @return gcd(P,S) with P = P'*gcd(P,S) and S = S'*gcd(P,S).
     */
    @Override
    public GenSolvablePolynomial<C> leftBaseGcd(GenSolvablePolynomial<C> P, GenSolvablePolynomial<C> S) {
        if (S == null || S.isZERO()) {
            return P;
        }
        if (P == null || P.isZERO()) {
            return S;
        }
        if (P.ring.nvar > 1) {
            throw new IllegalArgumentException(this.getClass().getName() + " no univariate polynomial");
        }
        boolean field = P.ring.coFac.isField();
        long e = P.degree(0);
        long f = S.degree(0);
        GenSolvablePolynomial<C> q;
        GenSolvablePolynomial<C> r;
        if (f > e) {
            r = P;
            q = S;
            long g = f;
            f = e;
            e = g;
        } else {
            q = P;
            r = S;
        }
        if (debug) {
            logger.debug("degrees: e = " + e + ", f = " + f);
        }
        C c;
        if (field) {
            r = r.monic();
            q = q.monic();
            c = P.ring.getONECoefficient();
        } else {
            r = (GenSolvablePolynomial<C>) r.abs();
            q = (GenSolvablePolynomial<C>) q.abs();
            C a = rightBaseContent(r);
            C b = rightBaseContent(q);
            r = divide(r, a); // indirection
            q = divide(q, b); // indirection
            c = gcd(a, b); // indirection
        }
        //System.out.println("baseCont: gcd(cont) = " + b);
        if (r.isONE()) {
            return r.multiply(c);
        }
        if (q.isONE()) {
            return q.multiply(c);
        }
        GenSolvablePolynomial<C> x;
        //System.out.println("baseGCD: q = " + q);
        //System.out.println("baseGCD: r = " + r);
        while (!r.isZERO()) {
            x = FDUtil.<C> leftBaseSparsePseudoRemainder(q, r);
            q = r;
            if (field) {
                r = x.monic();
            } else {
                r = leftBasePrimitivePart(x);
            }
            //System.out.println("baseGCD: q = " + q);
            //System.out.println("baseGCD: r = " + r);
        }
        return (GenSolvablePolynomial<C>) (q.multiply(c)).abs();
    }


    /**
     * Univariate GenSolvablePolynomial right greatest common divisor. Uses
     * pseudoRemainder for remainder.
     * @param P univariate GenSolvablePolynomial.
     * @param S univariate GenSolvablePolynomial.
     * @return gcd(P,S) with P = gcd(P,S)*P' and S = gcd(P,S)*S'.
     */
    @Override
    public GenSolvablePolynomial<C> rightBaseGcd(GenSolvablePolynomial<C> P, GenSolvablePolynomial<C> S) {
        if (S == null || S.isZERO()) {
            return P;
        }
        if (P == null || P.isZERO()) {
            return S;
        }
        if (P.ring.nvar > 1) {
            throw new IllegalArgumentException(this.getClass().getName() + " no univariate polynomial");
        }
        boolean field = P.ring.coFac.isField();
        long e = P.degree(0);
        long f = S.degree(0);
        GenSolvablePolynomial<C> q;
        GenSolvablePolynomial<C> r;
        if (f > e) {
            r = P;
            q = S;
            long g = f;
            f = e;
            e = g;
        } else {
            q = P;
            r = S;
        }
        if (debug) {
            logger.debug("degrees: e = " + e + ", f = " + f);
        }
        C c;
        if (field) {
            r = r.monic();
            q = q.monic();
            c = P.ring.getONECoefficient();
        } else {
            r = (GenSolvablePolynomial<C>) r.abs();
            q = (GenSolvablePolynomial<C>) q.abs();
            C a = leftBaseContent(r);
            C b = leftBaseContent(q);
            r = divide(r, a); // indirection
            q = divide(q, b); // indirection
            c = gcd(a, b); // indirection
        }
        //System.out.println("baseCont: gcd(cont) = " + b);
        if (r.isONE()) {
            return r.multiply(c);
        }
        if (q.isONE()) {
            return q.multiply(c);
        }
        GenSolvablePolynomial<C> x;
        //System.out.println("baseGCD: q = " + q);
        //System.out.println("baseGCD: r = " + r);
        while (!r.isZERO()) {
            x = FDUtil.<C> rightBaseSparsePseudoRemainder(q, r);
            q = r;
            if (field) {
                r = x.monic();
            } else {
                r = rightBasePrimitivePart(x);
            }
            //System.out.println("baseGCD: q = " + q);
            //System.out.println("baseGCD: r = " + r);
        }
        return (GenSolvablePolynomial<C>) (q.multiplyLeft(c)).abs();
    }


    /**
     * Univariate GenSolvablePolynomial left recursive greatest common divisor.
     * Uses pseudoRemainder for remainder.
     * @param P univariate recursive GenSolvablePolynomial.
     * @param S univariate recursive GenSolvablePolynomial.
     * @return gcd(P,S) with P = P'*gcd(P,S)*p and S = S'*gcd(P,S)*s, where
     *         deg_main(p) = deg_main(s) == 0.
     */
    @Override
    public GenSolvablePolynomial<GenPolynomial<C>> leftRecursiveUnivariateGcd(
                    GenSolvablePolynomial<GenPolynomial<C>> P, GenSolvablePolynomial<GenPolynomial<C>> S) {
        if (S == null || S.isZERO()) {
            return P;
        }
        if (P == null || P.isZERO()) {
            return S;
        }
        if (P.ring.nvar > 1) {
            throw new IllegalArgumentException("no univariate polynomial");
        }
        boolean field = P.leadingBaseCoefficient().ring.coFac.isField();
        long e = P.degree(0);
        long f = S.degree(0);
        GenSolvablePolynomial<GenPolynomial<C>> q, r, x, qs, rs;
        if (f > e) {
            r = P;
            q = S;
            long g = f;
            f = e;
            e = g;
        } else if (f < e) {
            q = P;
            r = S;
        } else { // f == e
            if (P.leadingBaseCoefficient().degree() > S.leadingBaseCoefficient().degree()) {
                q = P;
                r = S;
            } else {
                r = P;
                q = S;
            }
        }
        if (debug) {
            logger.debug("degrees: e = " + e + ", f = " + f);
        }
        if (field) {
            r = PolyUtil.<C> monic(r);
            q = PolyUtil.<C> monic(q);
        } else {
            r = (GenSolvablePolynomial<GenPolynomial<C>>) r.abs();
            q = (GenSolvablePolynomial<GenPolynomial<C>>) q.abs();
        }
        GenSolvablePolynomial<C> a = rightRecursiveContent(r);
        rs = FDUtil.<C> recursiveDivideRightEval(r, a);
        if (debug) {
            logger.info("recCont a = " + a + ", r = " + r);
            logger.info("recCont r/a = " + rs + ", r%a = " + r.subtract(rs.multiply(a)));
            if (!r.equals(rs.multiply(a))) {
                System.out.println("recGcd, r         = " + r);
                System.out.println("recGcd, cont(r)   = " + a);
                System.out.println("recGcd, pp(r)     = " + rs);
                System.out.println("recGcd, pp(r)c(r) = " + rs.multiply(a));
                throw new RuntimeException("recGcd, pp: not divisible");
            }
        }
        r = rs;
        GenSolvablePolynomial<C> b = rightRecursiveContent(q);
        qs = FDUtil.<C> recursiveDivideRightEval(q, b);
        if (debug) {
            logger.info("recCont b = " + b + ", q = " + q);
            logger.info("recCont q/b = " + qs + ", q%b = " + q.subtract(qs.multiply(b)));
            if (!q.equals(qs.multiply(b))) {
                System.out.println("recGcd, q         = " + q);
                System.out.println("recGcd, cont(q)   = " + b);
                System.out.println("recGcd, pp(q)     = " + qs);
                System.out.println("recGcd, pp(q)c(q) = " + qs.multiply(b));
                throw new RuntimeException("recGcd, pp: not divisible");
            }
        }
        q = qs;
        //no: GenSolvablePolynomial<C> c = leftGcd(a, b); // go to recursion
        GenSolvablePolynomial<C> c = rightGcd(a, b); // go to recursion
        logger.info("Gcd(contents) c = " + c + ", a = " + a + ", b = " + b);
        if (r.isONE()) {
            return r.multiply(c);
        }
        if (q.isONE()) {
            return q.multiply(c);
        }
        if (debug) {
            logger.info("r.ring = " + r.ring.toScript());
        }
        while (!r.isZERO()) {
            x = FDUtil.<C> recursiveSparsePseudoRemainder(q, r);
            q = r;
            r = leftRecursivePrimitivePart(x);
            if (field) {
                r = PolyUtil.<C> monic(r);
            }
        }
        if (debug) {
            logger.info("gcd(pp) = " + q + ", ring = " + P.ring.toScript());
        }
        q = (GenSolvablePolynomial<GenPolynomial<C>>) q.multiply(c).abs();
        if (false) { // not checkable
            qs = FDUtil.<C> recursiveSparsePseudoRemainder(P, q);
            rs = FDUtil.<C> recursiveSparsePseudoRemainder(S, q);
            if (!qs.isZERO() || !rs.isZERO()) {
                System.out.println("recGcd, P  = " + P);
                System.out.println("recGcd, S  = " + S);
                System.out.println("recGcd, q  = " + q);
                System.out.println("recGcd, qs = " + qs);
                System.out.println("recGcd, rs = " + rs);
                throw new RuntimeException("recGcd: not divisible");
            }
            logger.info("recGcd(P,S) okay: q = " + q);
        }
        return q;
    }


    /**
     * Univariate GenSolvablePolynomial right recursive greatest common divisor.
     * Uses pseudoRemainder for remainder.
     * @param P univariate recursive GenSolvablePolynomial.
     * @param S univariate recursive GenSolvablePolynomial.
     * @return gcd(P,S) with P = p*gcd(P,S)*P' and S = s*gcd(P,S)*S', where
     *         deg_main(p) = deg_main(s) == 0.
     */
    @Override
    public GenSolvablePolynomial<GenPolynomial<C>> rightRecursiveUnivariateGcd(
                    GenSolvablePolynomial<GenPolynomial<C>> P, GenSolvablePolynomial<GenPolynomial<C>> S) {
        if (S == null || S.isZERO()) {
            return P;
        }
        if (P == null || P.isZERO()) {
            return S;
        }
        if (P.ring.nvar > 1) {
            throw new IllegalArgumentException("no univariate polynomial");
        }
        boolean field = P.leadingBaseCoefficient().ring.coFac.isField();
        long e = P.degree(0);
        long f = S.degree(0);
        GenSolvablePolynomial<GenPolynomial<C>> q, r, x, qs, rs;
        if (f > e) {
            r = P;
            q = S;
            long g = f;
            f = e;
            e = g;
        } else if (f < e) {
            q = P;
            r = S;
        } else { // f == e
            if (P.leadingBaseCoefficient().degree() > S.leadingBaseCoefficient().degree()) {
                q = P;
                r = S;
            } else {
                r = P;
                q = S;
            }
        }
        if (debug) {
            logger.debug("RI-degrees: e = " + e + ", f = " + f);
        }
        if (field) {
            r = PolyUtil.<C> monic(r);
            q = PolyUtil.<C> monic(q);
        } else {
            r = (GenSolvablePolynomial<GenPolynomial<C>>) r.abs();
            q = (GenSolvablePolynomial<GenPolynomial<C>>) q.abs();
        }
        GenSolvablePolynomial<C> a = leftRecursiveContent(r);
        rs = FDUtil.<C> recursiveDivide(r, a);
        if (debug) {
            logger.info("RI-recCont a = " + a + ", r = " + r);
            logger.info("RI-recCont r/a = " + r + ", r%a = " + r.subtract(rs.multiplyLeft(a)));
            if (!r.equals(rs.multiplyLeft(a))) {
                System.out.println("RI-recGcd, r         = " + r);
                System.out.println("RI-recGcd, cont(r)   = " + a);
                System.out.println("RI-recGcd, pp(r)     = " + rs);
                System.out.println("RI-recGcd, pp(r)c(r) = " + rs.multiplyLeft(a));
                throw new RuntimeException("RI-recGcd, pp: not divisible");
            }
        }
        r = rs;
        GenSolvablePolynomial<C> b = leftRecursiveContent(q);
        qs = FDUtil.<C> recursiveDivide(q, b);
        if (debug) {
            logger.info("RI-recCont b = " + b + ", q = " + q);
            logger.info("RI-recCont q/b = " + qs + ", q%b = " + q.subtract(qs.multiplyLeft(b)));
            if (!q.equals(qs.multiplyLeft(b))) {
                System.out.println("RI-recGcd, q         = " + q);
                System.out.println("RI-recGcd, cont(q)   = " + b);
                System.out.println("RI-recGcd, pp(q)     = " + qs);
                System.out.println("RI-recGcd, pp(q)c(q) = " + qs.multiplyLeft(b));
                throw new RuntimeException("RI-recGcd, pp: not divisible");
            }
        }
        q = qs;
        //no: GenSolvablePolynomial<C> c = rightGcd(a, b); // go to recursion
        GenSolvablePolynomial<C> c = leftGcd(a, b); // go to recursion
        logger.info("RI-Gcd(contents) c = " + c + ", a = " + a + ", b = " + b);
        if (r.isONE()) {
            return r.multiplyLeft(c);
        }
        if (q.isONE()) {
            return q.multiplyLeft(c);
        }
        if (debug) {
            logger.info("RI-r.ring = " + r.ring.toScript());
        }
        while (!r.isZERO()) {
            x = FDUtil.<C> recursiveRightSparsePseudoRemainder(q, r);
            q = r;
            r = rightRecursivePrimitivePart(x);
            if (field) {
                r = PolyUtil.<C> monic(r);
            }
        }
        if (debug) {
            logger.info("RI-gcd(pp) = " + q + ", ring = " + P.ring.toScript());
        }
        q = (GenSolvablePolynomial<GenPolynomial<C>>) q.multiplyLeft(c).abs();
        if (false) { // not checkable
            qs = FDUtil.<C> recursiveRightSparsePseudoRemainder(P, q);
            rs = FDUtil.<C> recursiveRightSparsePseudoRemainder(S, q);
            if (!qs.isZERO() || !rs.isZERO()) {
                System.out.println("RI-recGcd, P  = " + P);
                System.out.println("RI-recGcd, S  = " + S);
                System.out.println("RI-recGcd, q  = " + q);
                System.out.println("RI-recGcd, qs = " + qs);
                System.out.println("RI-recGcd, rs = " + rs);
                throw new RuntimeException("RI-recGcd: not divisible");
            }
            logger.info("RI-recGcd(P,S) post pp okay: q = " + q);
        }
        return q;
    }

}
