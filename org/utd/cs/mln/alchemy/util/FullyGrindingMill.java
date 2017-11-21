package org.utd.cs.mln.alchemy.util;

import org.apache.commons.collections.ArrayStack;
import org.utd.cs.gm.core.LogDouble;
import org.utd.cs.mln.alchemy.core.*;

import java.util.*;

/**
 * Created by Happy on 2/28/17.
 */
public class FullyGrindingMill {

    public static boolean queryEvidence = false;
    private GroundMLN groundMln;
    private List<GroundPredicate> groundPredicatesList;
    private Map<GroundPredicate, Integer> groundPredicateIntegerMap;

    private void init() {
        groundMln = new GroundMLN();
        groundPredicatesList = new ArrayList<>();
        groundPredicateIntegerMap = new HashMap<>();
    }

    public GroundMLN ground(MLN mln) {
        init();
        for(Formula formula : mln.formulas)
        {
            Set<Term> formulaWiseTermToGround = new HashSet<Term>();
            for (WClause clause : formula.clauses) {
                for (Atom atom : clause.atoms) {
                    for (int j = 0; j < atom.terms.size(); j++) {
                        Term term = atom.terms.get(j);
                        formulaWiseTermToGround.add(term);
                    }
                }
            }
            ground(formula, mln.params, new ArrayList<Term>(formulaWiseTermToGround));
        }

        groundMln.groundPredicates.addAll(groundPredicatesList);
        for(PredicateSymbol symbol : mln.symbols)
        {
            groundMln.symbols.add(new GroundPredicateSymbol(symbol.id, symbol.symbol, symbol.values, symbol.world));
        }
        return groundMln;
    }

    private void ground(Formula formula, List<Param> params, ArrayList<Term> terms) {
        int[][] permutations = permute(terms);

        for(int i = 0 ; i < permutations.length ; i++)
        {
            GroundFormula newFormula = new GroundFormula();
            int currentFormulaId = groundMln.groundFormulas.size();
            newFormula.formulaId = currentFormulaId;
            newFormula.parentFormulaId = formula.formulaId;
            newFormula.weight = new LogDouble(formula.weight.getValue(), true);
//            newFormula.numConnections = new ArrayList<>(formula.numConnections);
//            List<Double> doubweight = new ArrayList<>();
//            for(Integer pi : formula.paramIndices)
//            {
//                doubweight.add(params.get(pi).wt.getValue());
//            }
//            newFormula.effWeight = new LogDouble(VecOperations.dotprod(doubweight, Aggregator.aggregator(formula.numConnections)), true);
            List<GroundClause> newGroundClauseList = new ArrayList<GroundClause>();
            for(int c = 0 ; c < formula.clauses.size() ; c++)
            {
                WClause clause = formula.clauses.get(c);
                GroundClause newGroundClause = new GroundClause();
                newGroundClause.formulaId = currentFormulaId;
                newGroundClause.weight = new LogDouble(clause.weight.getValue(), true);
                Map<Integer, BitSet> gpIndexToSatVals = new HashMap<>();
                List<GroundPredicate> newGroundPreds = new ArrayList<>(); // We need this list because once a groundClause is created, we want to
                // update formulaIds info of each groundPred. We can't do it on the go because we don't know whether groundClause will be created
                // or not, since it can be removed due to preprocessing.
                boolean clauseToRemove = false;

                // Iterate over each first order atom, and create ground atom for it.
                for(int j = 0 ; j < clause.atoms.size() ; j++)
                {
                    boolean sign = clause.sign.get(j);
                    Atom oldAtom = clause.atoms.get(j); // first order atom
                    int valTrue = clause.valTrue.get(j);
                    GroundPredicate gp = new GroundPredicate(); // GroundPredicate to create
                    gp.symbol = new GroundPredicateSymbol(oldAtom.symbol.id,oldAtom.symbol.symbol,oldAtom.symbol.values, oldAtom.symbol.world);
                    // Fill in the terms with constants
                    for(Term term : oldAtom.terms)
                    {
                        int termIndex = terms.indexOf(term);
                        gp.terms.add(permutations[i][termIndex]);
                    }

                    // Check if this groundPredicate already exists, if it does not, then add it to groundPredicate List.
                    // Note that it may happen that this clause gets removed later due to preprocessing, but still,
                    // we need this groundPredicate, so there is no harm in adding it to groundPredicate List.
                    int gpIndex = -1;
                    if(groundPredicateIntegerMap.containsKey(gp))
                    {
                        gpIndex = groundPredicateIntegerMap.get(gp);
                    }
                    if(gpIndex == -1) {
                        groundPredicatesList.add(gp);
                        int numPossibleValues = oldAtom.symbol.values.values.size();
                        gp.numPossibleValues = numPossibleValues;
                        gpIndex = groundPredicatesList.size()-1;
                        groundPredicateIntegerMap.put(gp, gpIndex);
                    }
                    gp = groundPredicatesList.get(gpIndex);
                    newFormula.groundPredIndices.add(gpIndex); // added by Happy

                    // Check if this groundPredicate occurs first time in this ground clause. then update
                    // groundClause's data structures about this groundPredicate.
                    int gpIndexInClause = newGroundClause.groundPredIndices.indexOf(gpIndex);
                    //GroundAtom newGroundAtom = new GroundAtom(gpIndex, gpIndexInClause, valTrue, sign);
                    if(gpIndexInClause == -1)
                    {
                        newGroundPreds.add(gp);
                        newGroundClause.groundPredIndices.add(gpIndex);
                        gpIndexInClause = newGroundClause.groundPredIndices.size()-1;
                        newGroundClause.globalToLocalPredIndex.put(gpIndex,gpIndexInClause);
                        gpIndexToSatVals.put(gpIndexInClause, new BitSet(gp.numPossibleValues));
                    }

                    // Now once we have added new ground Atom, we need to check if ground clause gets satisfied or not.
                    BitSet gpBitSet = new BitSet(gp.numPossibleValues);
                    gpBitSet.set(valTrue);
                    if(sign == true)
                        gpBitSet.flip(0,gp.numPossibleValues);
                    gpBitSet.or(gpIndexToSatVals.get(gpIndexInClause));

                    // If all bits are set for this groundPred, then this clause will always be satisfied and hence,
                    // shouldn't be added into groundformula. Note that, although at this point, we know that
                    // this clause shouldn't be added, but still we shouldn't just break out of this loop, as we
                    // need to add groundPredicates, but we shouldn't add any clauseInfo into groundPredicates appearing
                    // in this clause.
//                    if(gpBitSet.cardinality() == gp.numPossibleValues)
//                        clauseToRemove = true;
                    gpIndexToSatVals.put(gpIndexInClause, gpBitSet);

                }

                // If this clause is to be added, then only update all gp's formulaId's info
                if(clauseToRemove == false)
                {
                    for(int gpId = 0 ; gpId < newGroundPreds.size() ; gpId++)
                    {
                        BitSet b = gpIndexToSatVals.get(gpId);
                        newGroundClause.grounPredBitSet.add(b);
                    }
                    newGroundClauseList.add(newGroundClause);
                    for(GroundPredicate gp : newGroundPreds)
                    {
                        int gpIndex = groundPredicateIntegerMap.get(gp);
                        //newFormula.groundPredIndices.add(gpIndex);
                        //newFormula.groundPredIndicesList.add(gpIndex);
                        if(!gp.groundFormulaIds.containsKey(currentFormulaId))
                        {
                            gp.groundFormulaIds.put(currentFormulaId, new HashSet<Integer>());
                        }
                        gp.groundFormulaIds.get(currentFormulaId).add(newGroundClauseList.size()-1);
                    }
                }
            }
            if(newGroundClauseList.size() > 0)
            {
                newFormula.groundClauses.addAll(newGroundClauseList);
                groundMln.groundFormulas.add(newFormula);
            }
        }
    }

    /**
     * Create all possible permutation of a the domains of the terms
     * @param terms
     * @return
     */

    private int[][] permute(List<Term> terms) {

        int permutaionSize = 1;
        for (Term term : terms) {
            permutaionSize *= term.domain.size();
        }

        int[][] permuations = new int[permutaionSize][terms.size()];

        for (int i = 0; i < permuations.length; i++) {
            int residue = i;
            for (int j = 0; j < terms.size(); j++) {
                int index = residue % terms.get(j).domain.size();
                residue = residue / terms.get(j).domain.size();
                permuations[i][j] = terms.get(j).domain.get(index);
            }
        }

        return permuations;

    }

    public GroundMLN handleEvidence(GroundMLN groundMln, Evidence evidence, Evidence truth, List<String> evidence_preds, List<String> query_preds, List<String> hidden_preds, boolean withEM) throws CloneNotSupportedException {
        GroundMLN newGroundMln = new GroundMLN();
        Map<Integer,Integer> newGpIndexToTrueVal = new HashMap<Integer,Integer>();
        //List<GroundPredicate> newGpList = new ArrayList<>();
        Map<GroundPredicate,Integer> newGpToIntegerMap = new HashMap<GroundPredicate,Integer>();

        for(int i = 0 ; i < groundMln.groundPredicates.size() ; i++)
        {
            GroundPredicate gp = groundMln.groundPredicates.get(i);
            GroundPredicate newGp = new GroundPredicate();

            //TODO : currently, no copy c'tor called
            newGp.symbol = new GroundPredicateSymbol(gp.symbol.id, gp.symbol.symbol,gp.symbol.values, gp.symbol.world);

            // Fill in the terms with constants
            for(Integer term : gp.terms)
            {
                newGp.terms.add(term);
            }
            newGp.numPossibleValues = gp.numPossibleValues;

            newGroundMln.groundPredicates.add(newGp);
            if(truth.predIdVal.containsKey(i))
            {
                int valTrue = truth.predIdVal.get(i);
                newGpIndexToTrueVal.put(i, valTrue);
            }
            else if(evidence.predIdVal.containsKey(i))
            {
                int valTrue = evidence.predIdVal.get(i);
                newGpIndexToTrueVal.put(i, valTrue);
            }
            else
            {
                newGpIndexToTrueVal.put(i, 0);
            }
            if(!query_preds.contains(gp.symbol.symbol))
                newGp.isEvidence = true;
            else
            {
                if(evidence_preds.contains(gp.symbol.symbol))
                {
                    if(evidence.predIdVal.containsKey(i))
                        newGp.isEvidence = true;
                }
                else
                {
                    if(queryEvidence && !truth.predIdVal.containsKey(i))
                        newGp.isEvidence = true;
                }
            }

//            if(evidence_preds.contains(gp.symbol.symbol) && gp.symbol.world == PredicateSymbol.WorldState.closed)
//            {
//                newGp.isEvidence = true;
//            }
//            else if(query_preds.contains(gp.symbol.symbol) && !truth.predIdVal.containsKey(i) && queryEvidence == true)
//            {
//                newGp.isEvidence = true;
//            }
        }

        for(GroundFormula gf : groundMln.groundFormulas)
        {
            GroundFormula newGroundFormula = new GroundFormula();
            /*
            for (int i = 0; i < gf.groundPredIndicesList.size(); i++) {
                newGroundFormula.groundPredIndicesList.add(gf.groundPredIndicesList.get(i));
            }*/
            int currentFormulaId = newGroundMln.groundFormulas.size();
            newGroundFormula.weight = gf.weight;
            newGroundFormula.effWeight = gf.effWeight;
            newGroundFormula.formulaId = gf.formulaId;
            newGroundFormula.parentFormulaId = gf.parentFormulaId;
            newGroundFormula.numConnections = new ArrayList<>(gf.numConnections);
            newGroundFormula.groundPredIndices = new ArrayList<>(gf.groundPredIndices);
            boolean keepFormula = true;
            List<GroundClause> newGcList = new ArrayList<>();
            for(GroundClause gc : gf.groundClauses)
            {
                GroundClause newGc = new GroundClause();
                newGc.formulaId = currentFormulaId;
                newGc.weight = new LogDouble(gc.weight.getValue(), true);
                //List<Integer> newGroundPredIndices = new ArrayList<>();
                boolean clauseToRemove = false;
                for(Integer gpIndex : gc.groundPredIndices)
                {
                    GroundPredicate gp = groundMln.groundPredicates.get(gpIndex);
                    BitSet b = gc.grounPredBitSet.get(gc.globalToLocalPredIndex.get(gpIndex));

                    // If gp is not in evidence and openworld
                    // and if gp is in truth and queryEvidence
                    boolean toAdd = false;
                    if(!withEM)
                    {
                        if(evidence_preds.contains(gp.symbol.symbol) && gp.symbol.world == PredicateSymbol.WorldState.open)
                            toAdd = true;
                        else if(query_preds.contains(gp.symbol.symbol) && (truth.predIdVal.containsKey(gpIndex) || !queryEvidence))
                        {
                            if(!evidence.predIdVal.containsKey(gpIndex))
                                toAdd = true;
                        }
                    }
                    else {
                        if (hidden_preds.contains(gp.symbol.symbol))
                            toAdd = true;
                    }

                    if(toAdd)
                    {

                        // Check if this groundPredicate already exists, if it does not, then add it to groundPredicate List.
                        // Note that it may happen that this clause gets removed later due to preprocessing, but still,
                        // we need this groundPredicate, so there is no harm in adding it to groundPredicate List.


                        //newGp = newGpList.get(newGpIndex);

                        // Check if this groundPredicate occurs first time in this ground clause. then update
                        // groundClause's data structures about this groundPredicate.
                        int newGpIndexInClause = newGc.groundPredIndices.indexOf(gpIndex);

                        if(newGpIndexInClause == -1)
                        {
                            //newGroundPreds.add(newGp);
                            newGc.groundPredIndices.add(gpIndex);
                            newGpIndexInClause = newGc.groundPredIndices.size()-1;
                            newGc.globalToLocalPredIndex.put(gpIndex,newGpIndexInClause);
                            newGc.grounPredBitSet.add((BitSet)b.clone());
                        }
                    }

                    else
                    {
                        if(evidence_preds.contains(gp.symbol.symbol))
                        {
                            if(evidence.predIdVal.containsKey(gpIndex)) {
                                if (b.get(evidence.predIdVal.get(gpIndex))) {
                                    clauseToRemove = true;
                                    break;
                                }
                            }
                            else
                            {
                                if(b.get(0)) // If it is closed world and not in evidence, then we assume that its true val is 0.
                                {
                                    clauseToRemove = true;
                                    break;
                                }
                            }
                        }
                        else // If it is not evidence pred, but queryevidence is true
                        {
                            if(b.get(0)) // If it is closed world and not in evidence, then we assume that its true val is 0.
                            {
                                clauseToRemove = true;
                                break;
                            }
                        }

                    }
                }
                if(clauseToRemove == false)
                {
                    if(newGc.groundPredIndices.size() > 0)
                    {
                        //clause add karna h
                        newGcList.add(newGc);
                        for (int gpIndex : newGc.groundPredIndices) {
                            //newGroundFormula.groundPredIndices.add(gpIndex);
                            GroundPredicate gp = newGroundMln.groundPredicates.get(gpIndex);
                            if (!gp.groundFormulaIds.containsKey(currentFormulaId)) {
                                gp.groundFormulaIds.put(currentFormulaId, new HashSet<Integer>());
                            }
                            gp.groundFormulaIds.get(currentFormulaId).add(newGcList.size() - 1);
                        }
                    }
                    else
                    {
                        keepFormula = false;
                        break;
                    }
                }
            }
            if(newGcList.size() > 0 && keepFormula)
            {
                newGroundFormula.groundClauses.addAll(newGcList);
                newGroundMln.groundFormulas.add(newGroundFormula);
            }
            else
            {
                // remove formulaIds from gp's formulaIdList
                for(GroundClause gc : newGcList)
                {
                    for(Integer gpId : gc.groundPredIndices)
                    {
                        GroundPredicate gp = newGroundMln.groundPredicates.get(gpId);
                        gp.groundFormulaIds.remove(currentFormulaId);
                    }
                }
            }

        }
        //newGroundMln.groundPredicates.addAll(newGpList);
        Set<GroundPredicateSymbol> gpsSet = new HashSet<>();
        for(GroundPredicate gp : newGroundMln.groundPredicates)
        {
            GroundPredicateSymbol gps = gp.symbol;
            gpsSet.add(new GroundPredicateSymbol(gps.id, gps.symbol, gps.values, gps.world));
        }
        newGroundMln.symbols.addAll(gpsSet);
        truth.predIdVal = newGpIndexToTrueVal;
        return newGroundMln;
    }



//    public void countFormulaConnections(GroundMLN gm, MLN mln)
//    {
//        int n = gm.groundFormulas.size();
//        for(int i = 0; i < n; i++)
//        {
//            GroundFormula gm_i = gm.groundFormulas.get(i);
//            for(int j = i+1 ; j < n ; j++)
//            {
//                GroundFormula gm_j = gm.groundFormulas.get(j);
//                if(gm_i.parentFormulaId != gm_j.parentFormulaId)
//                    continue;
//                Set<Integer> intersection = new HashSet<>(gm_i.groundPredIndices);
//                intersection.retainAll(gm_j.groundPredIndices);
//                if(intersection.size() > 0)
//                {
//                    gm_i.numConnections++;
//                    gm_j.numConnections++;
//                }
//            }
//        }
//        for(GroundFormula gf : gm.groundFormulas)
//        {
//            int parentFormulaId = gf.parentFormulaId;
//            mln.formulas.get(parentFormulaId).numTotalGroundings++;
//            mln.formulas.get(parentFormulaId).numTotalConnections += gf.numConnections;
//        }
//
//        for(Formula f : mln.formulas)
//        {
//            f.numAvgConnections = (1.0*f.numTotalConnections)/f.numTotalGroundings;
//        }
//    }
}
