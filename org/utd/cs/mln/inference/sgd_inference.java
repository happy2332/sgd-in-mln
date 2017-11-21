package org.utd.cs.mln.inference;

import org.utd.cs.gm.utility.Timer;
import org.utd.cs.mln.alchemy.core.Evidence;
import org.utd.cs.mln.alchemy.core.GroundMLN;
import org.utd.cs.mln.alchemy.core.MLN;
import org.utd.cs.mln.alchemy.util.FullyGrindingMill;
import org.utd.cs.mln.alchemy.util.Parser;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Created by Happy on 7/4/17.
 */
public class sgd_inference {
    private static String mlnFile, outFile;
    private static boolean trackFormulaCounts = false, calculateMarginal = true;
    private static int NumBurnIn = 500, NumSamples = 1000, lambda = 6;

    private enum ArgsState {
        MlnFile,
        OutFile,
        Flag,
        Lambda,
        NumSamples
    }

    public static void main(String[] args) throws FileNotFoundException, CloneNotSupportedException {
        parseArgs(args);
        FullyGrindingMill fgm = new FullyGrindingMill();
        MLN mln = new MLN();
        Parser parser = new Parser(mln);
        parser.parseInputMLNFile(mlnFile);
        String typename = "dom1";
        Map<String, Set<Integer>> varTypeToDomain = createDomain(lambda,typename);
        mln.overWriteDomain(varTypeToDomain);
        mln.calculateNumConnections();
        System.out.println("Creating MRF...");
        long time = System.currentTimeMillis();
        GroundMLN groundMln = fgm.ground(mln);
        //fgm.countFormulaGndings(groundMln, mln);
        //fgm.countFormulaConnections(groundMln, mln);
        //System.out.println(mln.formulas.get(1).numAvgConnections);

        System.out.println("Time taken to create MRF : " + Timer.time((System.currentTimeMillis() - time) / 1000.0));
        System.out.println("Total number of ground formulas : " + groundMln.groundFormulas.size());

        GibbsSampler_v2 gs = new GibbsSampler_v2(mln, groundMln, null, NumBurnIn, NumSamples, trackFormulaCounts, calculateMarginal);
        PrintWriter writer = null;
        try{
            writer = new PrintWriter(new FileOutputStream(outFile));
        }
        catch (IOException e) {
        }
        gs.infer(true, true);
        gs.writeMarginal(writer);
        writer.close();
    }

    // Given a lambda, generates domain according to poisson distribution for each type.
    private static Map<String,Set<Integer>> createDomain(int lambda, String typename) {
        Map<String, Set<Integer>> varTypeToDomain = new HashMap<>();
        //int numConstants = getPoisson(lambda);
        int numConstants = 10;
        Set<Integer> constants = new HashSet<>();
        for (int i = 0; i < numConstants; i++) {
            constants.add(i);
        }
        varTypeToDomain.put(typename, constants);
        return varTypeToDomain;
    }

    public static int getPoisson(double lambda) {
        double L = Math.exp(-lambda);
        double p = 1.0;
        int k = 0;

        do {
            k++;
            p *= Math.random();
        } while (p > L);

        return k - 1;
    }

    private static void parseArgs(String[] args) {
        ArgsState state = ArgsState.Flag;
        if (args.length == 0) {
            System.out.println("No flags provided ");
            System.out.println("Following are the allowed flags : ");
            System.out.println(manual);
            System.exit(0);
        }
        System.out.println("Inference parameters given : ");
        for (String arg : args) {
            switch (state) {
                case MlnFile: // necessary
                    mlnFile = arg;
                    System.out.println("-i = " + arg);
                    state = ArgsState.Flag;
                    continue;

                case OutFile: // necessary
                    outFile = arg;
                    System.out.println("-o = " + arg);
                    state = ArgsState.Flag;
                    continue;


                case Lambda: // necessary
                    lambda = Integer.parseInt(arg);
                    System.out.println("-lambda = " + arg);
                    state = ArgsState.Flag;
                    continue;

                case NumSamples: // by default, it is 100
                    NumSamples = Integer.parseInt(arg);
                    state = ArgsState.Flag;
                    continue;


                case Flag:
                    if (arg.equals("-i")) {
                        state = ArgsState.MlnFile;
                    } else if (arg.equals(("-o"))) {
                        state = ArgsState.OutFile;
                    } else if (arg.equals(("-lambda"))) {
                        state = ArgsState.Lambda;
                    } else if (arg.equals(("-NumSamples"))) {
                        state = ArgsState.NumSamples;
                    } else {
                        System.out.println("Unknown flag " + arg);
                        System.out.println("Following are the allowed flags : ");
                        System.out.println(manual);
                        System.exit(0);
                    }

            }
        }
        if (mlnFile == null) {
            System.out.println("Necessary to provide MLN file, exiting !!!");
            System.exit(0);
        }

        if (outFile == null) {
            System.out.println("Necessary to provide output file, exiting !!!");
            System.exit(0);
        }

        System.out.println("-NumSamples = " + NumSamples);
        System.out.println("-lambda = " + lambda);
    }

    private static String manual = "-i\t(necessary) Input mln file\n" +
            "-o\t(Necessary) output file\n" +
            "-NumSamples\t(Optional, default 1000) number of Gibbs Samples\n" +
            "-lambda\t(Optional, default 6) lambda of poisson distribution to generate constants";

}
