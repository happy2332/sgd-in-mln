package org.utd.cs.mln.alchemy.core;

import org.utd.cs.gm.core.LogDouble;
import org.utd.cs.gm.utility.Pair;
import org.utd.cs.mln.alchemy.util.Aggregator;
import org.utd.cs.mln.alchemy.util.VecOperations;

import java.util.*;

/**
 * Created by Happy on 2/23/17.
 */
public class State {
    public GroundMLN groundMLN;
    public List<Integer> truthVals = new ArrayList<>(); // For each groundPredicate in mln.groundPredicates, stores its truthval
    public List<Set<Integer>> falseClausesSet = new ArrayList<>(); // for each groundformula, stores set of groundClauseIds which are false in this state
    public List<List<Integer>> numTrueLiterals = new ArrayList<>(); // for each groundformula, for each clauseId, stores numSatLiterals in that clause
    public List<List<Double>> wtsPerPredPerVal = new ArrayList<>(); // For each GroundPred, stores sat wts for each value

    public State(GroundMLN groundMLN) {
        this.groundMLN  = groundMLN;
        int numGroundPreds = groundMLN.groundPredicates.size();
        for(int i = 0 ; i < numGroundPreds ; i++)
        {
            truthVals.add(0);
            wtsPerPredPerVal.add(new ArrayList<>(Collections.nCopies(groundMLN.groundPredicates.get(i).numPossibleValues,0.0)));
        }
        int numGroundFormulas = groundMLN.groundFormulas.size();
        for(int i = 0 ; i < numGroundFormulas ; i++)
        {
            falseClausesSet.add(new HashSet<Integer>());
            int numGroundClauses = groundMLN.groundFormulas.get(i).groundClauses.size();
            numTrueLiterals.add(new ArrayList<Integer>());
            for(int j = 0 ; j < numGroundClauses ; j++)
            {
                numTrueLiterals.get(i).add(0);
            }
        }
    }

    public void setGroundFormulaWtsToParentWts(MLN mln) {
        groundMLN.setEffWts(mln);
    }

    public boolean isGroundFormulaTrue(GroundFormula gf)
    {
        boolean isFormulaSatisfied = true;
        for (GroundClause gc : gf.groundClauses) {
            boolean isClauseSatisfied = false;
            for (Integer gpId : gc.groundPredIndices) {
                BitSet b = gc.grounPredBitSet.get(gc.globalToLocalPredIndex.get(gpId));
                int trueVal = truthVals.get(gpId);
                isClauseSatisfied |= b.get(trueVal);
                if (isClauseSatisfied)
                    break;
            }
            isFormulaSatisfied &= isClauseSatisfied;
            if (!isFormulaSatisfied)
                break;
        }
        if (isFormulaSatisfied) {
            return true;
        }
        return false;
    }
    public int[] getNumTrueGndings(int numWts)
    {
        int []numTrueGndings = new int[numWts];
        for(GroundFormula gf : groundMLN.groundFormulas)
        {
            boolean isFormulaSatisfied = true;
            for(GroundClause gc : gf.groundClauses)
            {
                boolean isClauseSatisfied = false;
                for(Integer gpId : gc.groundPredIndices)
                {
                    BitSet b = gc.grounPredBitSet.get(gc.globalToLocalPredIndex.get(gpId));
                    int trueVal = truthVals.get(gpId);
                    isClauseSatisfied |= b.get(trueVal);
                    if(isClauseSatisfied)
                        break;
                }
                isFormulaSatisfied &= isClauseSatisfied;
                if(!isFormulaSatisfied)
                    break;
            }
            if(isFormulaSatisfied)
            {
                int parentFormulaId = gf.parentFormulaId;
                numTrueGndings[parentFormulaId]++;
            }
        }
        return numTrueGndings;
    }
}
