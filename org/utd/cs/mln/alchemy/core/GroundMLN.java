package org.utd.cs.mln.alchemy.core;

import org.utd.cs.gm.core.LogDouble;
import org.utd.cs.mln.alchemy.util.Aggregator;
import org.utd.cs.mln.alchemy.util.VecOperations;

import java.util.*;

/**
 * Created by Happy on 2/28/17.
 */
public class GroundMLN {
    public List<GroundPredicateSymbol> symbols = new ArrayList<>();
    public List<GroundPredicate> groundPredicates = new ArrayList();
    public List<GroundFormula> groundFormulas = new ArrayList<>();

    public void setNumConnections()
    {
        // Key is a triplet : predIndex, formulaId, predPos
        HashMap<List<Integer>, Double> connections = new HashMap<>();
        for(GroundFormula gf : groundFormulas)
        {
            int formulaId = gf.parentFormulaId;
            for (int i = 0; i < gf.groundPredIndices.size(); i++) {
                int gpIndex = gf.groundPredIndices.get(i);
                List<Integer> key = new ArrayList<>();
                key.add(gpIndex);
                key.add(formulaId);
                key.add(i);
                if(!connections.containsKey(key))
                {
                    connections.put(key,0.0);
                }
                double val = connections.get(key);
                connections.put(key,val+1);
            }
        }
        for(GroundFormula gf : groundFormulas)
        {
            int formulaId = gf.parentFormulaId;
            for (int i = 0; i < gf.groundPredIndices.size(); i++) {
                int gpIndex = gf.groundPredIndices.get(i);
                List<Integer> key = new ArrayList<>();
                key.add(gpIndex);
                key.add(formulaId);
                key.add(i);
                gf.numConnections.add(connections.get(key));
            }

            //System.out.println("parentId : "+gf.parentFormulaId);
            //System.out.println("numConnections : "+gf.numConnections);
        }
    }

    public void setEffWts(MLN mln)
    {
        for(GroundFormula gf : groundFormulas)
        {
//            if(gf.groundPredIndices.size() == 3)
//                System.out.println("come here");
            int parentFormulaId = gf.parentFormulaId;
            Formula f = mln.formulas.get(parentFormulaId);
            gf.weight = new LogDouble(f.weight.getValue(), true);
            List<Double> doubweight = new ArrayList<>();
            for(Integer pi : f.paramIndices)
            {
                doubweight.add(mln.params.get(pi).wt.getValue());
            }
            gf.effWeight = new LogDouble(VecOperations.dotprod(doubweight, Aggregator.aggregator(gf.numConnections)), true);
        }
    }

}


