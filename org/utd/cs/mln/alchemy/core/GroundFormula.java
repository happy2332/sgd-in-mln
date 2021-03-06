package org.utd.cs.mln.alchemy.core;

import org.utd.cs.gm.core.LogDouble;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Happy on 2/21/17.
 */
public class GroundFormula {
    public List<GroundClause> groundClauses = new ArrayList<>();
    public List<Integer> groundPredIndices = new ArrayList<>(); // stores gpIndices position wise
    public int formulaId; // index of this formula in the MLN's list of ground formulas
    public int parentFormulaId; // id of first order formula from which this came
    public LogDouble weight;
    public LogDouble effWeight;
    public List<Double> numConnections = new ArrayList<>();
    public boolean isSatisfied;
}
