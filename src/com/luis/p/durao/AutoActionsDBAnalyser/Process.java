package com.luis.p.durao.AutoActionsDBAnalyser;

import com.luis.p.durao.AutoActionsDBAnalyser.DBs.DBconnection;
import com.luis.p.durao.AutoActionsDBAnalyser.Structs.Interval;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Process {

    private static Map<Integer, List<Integer>> autoSessionsInEachManualSession = null;
    public static Map<Integer, List<Integer>> getAutoSessionsInEachManualSession() {
        if(autoSessionsInEachManualSession != null)
            return autoSessionsInEachManualSession;
        //NullPointerException is intentional because if it happens it means that the main didn't initialize the com.luis.p.durao.AutoActionsDBAnalyser.DBs yet and this shouldn't be yet running.
        Map<Integer, Interval<Integer>> autoIntervals = DBconnection.automatic.getSession().getSessionIntervals();
        Map<Integer, Interval<Integer>> manualIntervals = DBconnection.manual.getSession().getSessionIntervals();

        Map<Integer, List<Integer>> ret = new HashMap<>();
        for(Map.Entry<Integer, Interval<Integer>> em : manualIntervals.entrySet()) {
            for(Map.Entry<Integer, Interval<Integer>> ea : autoIntervals.entrySet()) {
                if(ea.getValue().intersect(em.getValue())) {
                    ret.putIfAbsent(em.getKey(), new ArrayList<>());
                    ret.get(em.getKey()).add(ea.getKey());
                }
            }
        }
        return autoSessionsInEachManualSession=ret;
    }

    public static Map<Integer, Integer> getFalseStopsOfAutoActionsPerManualSession() {
        return getAutoSessionsInEachManualSession().entrySet().stream().collect(Collectors.toMap( o->o.getKey() , o->(o.getValue().size()-1) ));
    }

    //http://stackoverflow.com/a/16794680
    /**
     * Calculate distance between two points in latitude and longitude taking
     * into account height difference. If you are not interested in height
     * difference pass 0.0. Uses Haversine method as its base.
     *
     * lat1, lon1 Start point lat2, lon2 End point el1 Start altitude in meters
     * el2 End altitude in meters
     * @returns Distance in Meters
     */
    private static double distance(double lat1, double lat2, double lon1,
                                  double lon2, double el1, double el2) {

        final int R = 6371; // Radius of the earth

        Double latDistance = Math.toRadians(lat2 - lat1);
        Double lonDistance = Math.toRadians(lon2 - lon1);
        Double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        double height = el1 - el2;

        distance = Math.pow(distance, 2) + Math.pow(height, 2);

        return Math.sqrt(distance);
    }
}
