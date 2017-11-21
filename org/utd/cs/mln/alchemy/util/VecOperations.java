package org.utd.cs.mln.alchemy.util;

import java.util.List;

/**
 * Created by Happy on 9/20/17.
 */
public class VecOperations {

    public static double dotprod(List<Double> vec1, List<Double> vec2)
    {
        double ans = 0;
        if (vec1.size() != vec2.size())
        {
            System.out.println("Vectors should have same size!!!");
            return -1.0;
        }
        for(int i = 0; i < vec1.size() ; i++)
        {
            ans += vec1.get(i) * vec2.get(i);
        }
        return ans;
    }
}
