package org.utd.cs.mln.alchemy.core;

import org.utd.cs.gm.core.LogDouble;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Happy on 3/1/17.
 */
public class GroundClause {
    public List<GroundAtom> groundAtoms;
    public LogDouble weight;
    public int formulaId; // id of groundformula of which this is a part
    public boolean isSatisfied;

    public GroundClause(){
        groundAtoms = new ArrayList<GroundAtom>();
        weight = LogDouble.ZERO;
        isSatisfied = false;
        formulaId = 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        org.utd.cs.mln.alchemy.core.GroundClause that = (org.utd.cs.mln.alchemy.core.GroundClause) o;

        if (formulaId != that.formulaId) return false;
        if (isSatisfied != that.isSatisfied) return false;
        if (groundAtoms != null ? !groundAtoms.equals(that.groundAtoms) : that.groundAtoms != null) return false;
        return weight != null ? weight.equals(that.weight) : that.weight == null;
    }

    @Override
    public int hashCode() {
        int result = groundAtoms != null ? groundAtoms.hashCode() : 0;
        result = 31 * result + (weight != null ? weight.hashCode() : 0);
        result = 31 * result + formulaId;
        result = 31 * result + (isSatisfied ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "GroundClause{" +
                "groundAtoms=" + groundAtoms +
                ", weight=" + weight +
                ", parentFormulaId=" + formulaId +
                ", isSatisfied=" + isSatisfied +
                '}';
    }
}
