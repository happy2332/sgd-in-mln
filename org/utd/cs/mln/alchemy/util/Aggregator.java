package org.utd.cs.mln.alchemy.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Happy on 9/20/17.
 */
public class Aggregator {
    public static List<Double> aggregator(List<Double> vec)
    {
        int numPreds = vec.size();
        List<Double> result = new ArrayList<>();
        //double factor = Collections.max(vec);
        double factor = 1;
        for(Double v : vec)
        {
            //factor = v;
            //result.add(v); // Identity aggregator
            if(v == 1)
                result.add(1.0/(factor*numPreds));
            else
                result.add(1.0/(factor*(numPreds))); // old MLN case
        }
        return result;
    }
}
