package org.utd.cs.mln.alchemy.core;

import org.utd.cs.gm.core.LogDouble;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Formula {

	public List<WClause> clauses = new ArrayList<>();
	public LogDouble weight;
	public List<Integer> paramIndices = new ArrayList<>();
	public int formulaId;
	public Set<Term> termsSet = new HashSet<>();
	public List<Double> numConnections = new ArrayList<>(); // For each predicate, stores number of partial groundings


	public Formula(List<WClause> clauses_,
			LogDouble weight_) {
		clauses = clauses_;
		weight = (weight_);
	}

}
