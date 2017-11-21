package org.utd.cs.mln.alchemy.core;

import org.utd.cs.gm.core.LogDouble;

/**
 * Created by Happy on 9/28/17.
 */
public class Param {
    public LogDouble wt;
    public int formulaId, predPos;

    public Param(LogDouble wt, int formulaId, int predPos) {
        this.wt = wt;
        this.formulaId = formulaId;
        this.predPos = predPos;
    }
}
