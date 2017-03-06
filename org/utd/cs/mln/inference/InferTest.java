package org.utd.cs.mln.inference;

import org.utd.cs.mln.alchemy.core.GroundMLN;
import org.utd.cs.mln.alchemy.core.MLN;
import org.utd.cs.mln.alchemy.util.FullyGrindingMill;
import org.utd.cs.mln.alchemy.util.Parser;

import java.io.FileNotFoundException;

/**
 * Created by Happy on 2/28/17.
 */
public class InferTest {
    public static void main(String []args) throws FileNotFoundException {
        MLN mln = new MLN();
        String filename = "/Users/Happy/phd/experiments/without/data/MultiValued_data/smokes_mln.txt";
        Parser parser = new Parser(mln);
        parser.parseInputMLNFile(filename);
        FullyGrindingMill fgm = new FullyGrindingMill();
        System.out.println("Creating MRF...");
        long time = System.currentTimeMillis();
        GroundMLN groundMln = fgm.ground(mln);
        System.out.println("Time taken to create MRF : " + (System.currentTimeMillis() - time)/1000.0 + " s");
        System.out.println("Total number of ground formulas : " + groundMln.groundFormulas.size());

        GibbsSampler_v2 gs = new GibbsSampler_v2(groundMln, 100, 1000);
        gs.infer("/Users/Happy/phd/experiments/without/data/MultiValued_data/smokes_result.txt");
    }
}
