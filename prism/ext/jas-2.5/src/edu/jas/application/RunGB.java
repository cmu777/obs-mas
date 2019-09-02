/*
 * $Id: RunGB.java 4693 2013-11-07 23:44:47Z kredel $
 */

package edu.jas.application;


import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Arrays;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;

import org.apache.log4j.BasicConfigurator;

import edu.jas.gb.GroebnerBaseAbstract;
import edu.jas.gb.GroebnerBaseParallel;
import edu.jas.gb.GroebnerBaseDistributedEC;
import edu.jas.gb.GroebnerBaseDistributedHybridEC;
import edu.jas.gb.GroebnerBaseSeq;
import edu.jas.gb.OrderedSyzPairlist;
import edu.jas.gb.ReductionPar;
import edu.jas.gb.ReductionSeq;
import edu.jas.gbufd.GBFactory;
import edu.jas.gbufd.GroebnerBasePseudoParallel;
import edu.jas.kern.ComputerThreads;
import edu.jas.poly.GenPolynomial;
import edu.jas.poly.GenPolynomialRing;
import edu.jas.poly.GenPolynomialTokenizer;
import edu.jas.poly.PolynomialList;
import edu.jas.util.CatReader;
import edu.jas.util.ExecutableServer;


/**
 * Simple setup to run a GB example. <br />
 * Usage: RunGB [seq(+)|par(+)|dist(+)|disthyb|cli] &lt;file&gt;
 * #procs/#threadsPerNode [machinefile]
 * @author Heinz Kredel
 */
public class RunGB {


    /**
     * Check result GB if it is a GB.
     */
    static boolean doCheck = false;


    /**
     * Enable logging.
     */
    static boolean doLog = true;


    /**
     * main method to be called from commandline <br />
     * Usage: RunGB [seq|par(+)|dist(+)|disthyb(+)|cli] &lt;file&gt;
     * #procs/#threadsPerNode [machinefile] &lt;check&gt; &lt;log&gt;
     */
    @SuppressWarnings("unchecked")
    public static void main(String[] args) {

        String[] allkinds = new String[] { "seq", "seq+", 
                                           "par", "par+", 
                                           "dist", "dist+", 
                                           "disthyb", "disthyb+", 
                                           "cli" }; // must be last

        String usage = "Usage: RunGB [ "
                        + join(allkinds, " | ") 
                        + "[port] ] " 
                        + "<file> " 
                        + "#procs/#threadsPerNode " 
                        + "[machinefile] " 
                        + "[check] [nolog]";

        if (args.length < 1) {
            System.out.println("args: " + Arrays.toString(args));
            System.out.println(usage);
            return;
        }

        boolean plusextra = false;
        String kind = args[0];
        boolean sup = false;
        int k = -1;
        for (int i = 0; i < args.length; i++) {
            int j = indexOf(allkinds, args[i]);
            if (j < 0) {
                continue;
            }
            sup = true;
            k = i;
            kind = args[k];
            break;
        }
        if (!sup) {
            System.out.println("args(sup): " + Arrays.toString(args));
            System.out.println(usage);
            return;
        }
        if (kind.indexOf("+") >= 0) {
            plusextra = true;
        }
        System.out.println("kind: " + kind + ", k = " + k);

        final int GB_SERVER_PORT = 7114;
        int port = GB_SERVER_PORT;

        if (kind.equals("cli")) {
            if (args.length - k >= 2) {
                try {
                    port = Integer.parseInt(args[k + 1]);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    System.out.println("args(port): " + Arrays.toString(args));
                    System.out.println(usage);
                    return;
                }
            }
            runClient(port);
            return;
        }

        String filename = null;
        if (!kind.equals("cli")) {
            if (args.length - k < 2) {
                System.out.println("args(file): " + Arrays.toString(args));
                System.out.println(usage);
                return;
            }
            filename = args[k + 1];
        }

        int j = indexOf(args, "check");
        if (j >= 0) {
            doCheck = true;
        }
        j = indexOf(args, "nolog");
        if (j >= 0) {
            doLog = false;
        }

        int threads = 0;
        int threadsPerNode = 1;
        if (kind.startsWith("par") || kind.startsWith("dist")) {
            if (args.length - k < 3) {
                System.out.println("args(par|dist): " + Arrays.toString(args));
                System.out.println(usage);
                return;
            }
            String tup = args[k + 2];
            String t = tup;
            int i = tup.indexOf("/");
            if (i >= 0) {
                t = tup.substring(0, i).trim();
                tup = tup.substring(i + 1).trim();
                try {
                    threadsPerNode = Integer.parseInt(tup);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    System.out.println("args(threadsPerNode): " + Arrays.toString(args));
                    System.out.println(usage);
                    return;
                }
            }
            try {
                threads = Integer.parseInt(t);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                System.out.println("args(threads): " + Arrays.toString(args));
                System.out.println(usage);
                return;
            }
        }

        String mfile = null;
        if (kind.startsWith("dist")) {
            if (args.length - k >= 4) {
                mfile = args[k + 3];
            } else {
                mfile = "machines";
            }
        }

        Reader problem = getReader(filename);
        if (problem == null) {
            System.out.println("args(file): " + filename);
            System.out.println("args(file): examples.jar(" + filename + ")");
            System.out.println("args(file): " + Arrays.toString(args));
            System.out.println(usage);
            return;
        }
        RingFactoryTokenizer rftok = new RingFactoryTokenizer(problem);
        GenPolynomialRing pfac = null;
        try {
            pfac = rftok.nextPolynomialRing();
            rftok = null;
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        Reader polyreader = new CatReader(new StringReader("("), problem); // ( has gone
        //Reader polyreader = problem; 
        GenPolynomialTokenizer tok = new GenPolynomialTokenizer(pfac, polyreader);
        PolynomialList S = null;
        try {
            S = new PolynomialList(pfac, tok.nextPolynomialList());
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        System.out.println("input S =\n" + S);

        if (doLog) {
            BasicConfigurator.configure();
        }

        if (kind.startsWith("seq")) {
            runSequential(S, plusextra);
        } else if (kind.startsWith("par")) {
            runParallel(S, threads, plusextra);
        } else if (kind.startsWith("disthyb")) {
            runMasterHyb(S, threads, threadsPerNode, mfile, port, plusextra);
        } else if (kind.startsWith("dist")) {
            runMaster(S, threads, mfile, port, plusextra);
        }
        ComputerThreads.terminate();
        //System.exit(0);
    }


    @SuppressWarnings("unchecked")
    static void runMaster(PolynomialList S, int threads, String mfile, int port, boolean plusextra) {
        List L = S.list;
        List G = null;
        long t, t1;
        GroebnerBaseDistributedEC gbd = null;
        GroebnerBaseDistributedEC gbds = null;

        System.out.println("\nGroebner base distributed (" + threads + ", " + mfile + ", " + port + ") ...");
        t = System.currentTimeMillis();
        if (plusextra) {
            //gbds = new GroebnerBaseDistributedEC(threads,mfile, port);
            gbds = new GroebnerBaseDistributedEC(mfile, threads, new OrderedSyzPairlist(), port);
        } else {
            gbd = new GroebnerBaseDistributedEC(mfile, threads, port);
        }
        t1 = System.currentTimeMillis();
        if (plusextra) {
            G = gbds.GB(L);
        } else {
            G = gbd.GB(L);
        }
        t1 = System.currentTimeMillis() - t1;
        if (plusextra) {
            gbds.terminate(); //false);
        } else {
            gbd.terminate(); //false);
        }
        S = new PolynomialList(S.ring, G);
        System.out.println("G =\n" + S);
        System.out.println("G.size() = " + G.size());
        t = System.currentTimeMillis() - t;
        if (plusextra) {
            System.out.print("d+ ");
        } else {
            System.out.print("d ");
        }
        System.out.println("= " + threads 
                         + ", time = " + t1 + " milliseconds, " + (t-t1) + " start-up " + ", total = " + t);
        checkGB(S);
        System.out.println("");
    }


    @SuppressWarnings("unchecked")
    static void runMasterHyb(PolynomialList S, int threads, int threadsPerNode, String mfile, int port,
                    boolean plusextra) {
        List L = S.list;
        List G = null;
        long t, t1;
        GroebnerBaseDistributedHybridEC gbd = null;
        GroebnerBaseDistributedHybridEC gbds = null;

        System.out.println("\nGroebner base distributed hybrid (" + threads + "/" + threadsPerNode
                        + ", " + mfile + ", " + port + ") ...");
        t = System.currentTimeMillis();
        if (plusextra) {
            // gbds = new GroebnerBaseDistributedHybridEC(mfile, threads,port);
            gbds = new GroebnerBaseDistributedHybridEC(mfile, threads, threadsPerNode, new OrderedSyzPairlist(), port);
        } else {
            gbd = new GroebnerBaseDistributedHybridEC(mfile, threads, threadsPerNode, port);
        }
        t1 = System.currentTimeMillis();
        if (plusextra) {
            G = gbds.GB(L);
        } else {
            G = gbd.GB(L);
        }
        t1 = System.currentTimeMillis() - t1;
        if (plusextra) {
            gbds.terminate(); // true
        } else {
            gbd.terminate(); // false plus eventually killed by script
        }
        t = System.currentTimeMillis() - t;
        S = new PolynomialList(S.ring, G);
        System.out.println("G =\n" + S);
        System.out.println("G.size() = " + G.size());
        if (plusextra) {
            System.out.print("d+ ");
        } else {
            System.out.print("d ");
        }
        System.out.println("= " + threads + ", ppn = " + threadsPerNode 
                         + ", time = " + t1 + " milliseconds, " + (t-t1) + " start-up " + ", total = " + t);
        checkGB(S);
        System.out.println("");
    }


    static void runClient(int port) {
        System.out.println("\nGroebner base distributed client (" + port + ") ...");
        ExecutableServer es = new ExecutableServer(port);
        es.init();
        try {
             es.join();
        } catch (InterruptedException e) {
            // ignored
        }
    }


    @SuppressWarnings("unchecked")
    static void runParallel(PolynomialList S, int threads, boolean plusextra) {
        List L = S.list;
        List G;
        long t;
        GroebnerBaseAbstract bb = null;
        GroebnerBaseAbstract bbs = null;
        if (plusextra) {
            //bbs = new GroebnerBaseSeqPairParallel(threads);
            bbs = new GroebnerBaseParallel(threads, new ReductionPar(), new OrderedSyzPairlist());
        } else {
            if (S.ring.coFac.isField()) {
                bb = new GroebnerBaseParallel(threads);
            } else {
                bb = new GroebnerBasePseudoParallel(threads,S.ring.coFac);
            }
        }
        System.out.println("\nGroebner base parallel (" + threads + ") ...");
        t = System.currentTimeMillis();
        if (plusextra) {
            G = bbs.GB(L);
        } else {
            G = bb.GB(L);
        }
        t = System.currentTimeMillis() - t;
        S = new PolynomialList(S.ring, G);
        System.out.println("G =\n" + S);
        System.out.println("G.size() = " + G.size());

        if (plusextra) {
            System.out.print("p+ ");
        } else {
            System.out.print("p ");
        }
        System.out.println("= " + threads + ", time = " + t + " milliseconds");
        if (plusextra) {
            bbs.terminate();
        } else {
            bb.terminate();
        }
        checkGB(S);
        System.out.println("");
    }


    @SuppressWarnings("unchecked")
    static void runSequential(PolynomialList S, boolean plusextra) {
        List L = S.list;
        List G;
        long t;
        GroebnerBaseAbstract bb = null;
        if (plusextra) {
            //bb = new GroebnerBaseSeqPlusextra();
            bb = new GroebnerBaseSeq(new ReductionSeq(), new OrderedSyzPairlist());
        } else {
            bb = GBFactory.getImplementation(S.ring.coFac); //new GroebnerBaseSeq();
        }
        System.out.println("\nGroebner base sequential ...");
        t = System.currentTimeMillis();
        G = bb.GB(L);
        t = System.currentTimeMillis() - t;
        S = new PolynomialList(S.ring, G);
        System.out.println("G =\n" + S);
        System.out.println("G.size() = " + G.size());
        if (plusextra) {
            System.out.print("seq+, ");
        } else {
            System.out.print("seq, ");
        }
        System.out.println("time = " + t + " milliseconds");
        checkGB(S);
        System.out.println("");
    }


    @SuppressWarnings("unchecked")
    static void checkGB(PolynomialList S) {
        if (!doCheck) {
            return;
        }
        GroebnerBaseAbstract bb = GBFactory.getImplementation(S.ring.coFac);
        long t = System.currentTimeMillis();
        boolean chk = bb.isGB(S.list,false);
        t = System.currentTimeMillis() - t;
        System.out.println("check isGB = " + chk + " in " + t + " milliseconds");
    }


    static int indexOf(String[] args, String s) {
        for (int i = 0; i < args.length; i++) {
            if (s.equals(args[i])) {
                return i;
            }
        }
        return -1;
    }


    static String join(String[] args, String d) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < args.length; i++) {
            if (i > 0) {
                sb.append(d);
            }
            sb.append(args[i]);
        }
        return sb.toString();
    }


    static Reader getReader(String filename) {
        Reader problem = null;
        Exception fnf = null;
        try {
            problem = new InputStreamReader(new FileInputStream(filename),Charset.forName("UTF8"));
            problem = new BufferedReader(problem);
        } catch (FileNotFoundException e) {
            fnf = e;
        }
        if (problem != null) {
            return problem;
        }
        String examples = "examples.jar";
        try {
            JarFile jf = new JarFile(examples);
            JarEntry je = jf.getJarEntry(filename);
            if (je == null) {
               fnf.printStackTrace();
               return problem;
            }
            problem = new InputStreamReader(jf.getInputStream(je),Charset.forName("UTF8"));
            problem = new BufferedReader(problem);
        } catch (FileNotFoundException e) {
            fnf.printStackTrace();
            e.printStackTrace();
        } catch (IOException e) {
            fnf.printStackTrace();
            e.printStackTrace();
        }
        return problem;
    }

}
