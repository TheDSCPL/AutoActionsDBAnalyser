import DBs.DBconnection;
import DBs.Session;
import Structs.Interval;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static DBs.DBconnection.*;

public class Main {
    //static DBconnection db;

    private static Map<Integer, List<Integer>> autoSessionsInEachManualSession = null;

    public static Map<Integer, List<Integer>> getAutoSessionsInEachManualSession() {
        if(autoSessionsInEachManualSession != null)
            return autoSessionsInEachManualSession;
        //NullPointerException is intentional because if it happens it means that the main didn't initialize the DBs yet and this shouldn't be yet running.
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

    public static void main(String[] args) {
        automatic = new DBconnection("auto.db");
        manual = new DBconnection("auto.db");
        //manual = new DBconnection("manual.db");
        Session autoSession = automatic.getSession();
        autoSession.tableResult.print();
        System.out.println(autoSession.getSessionIntervals());

        long aft, b4 = System.nanoTime();
        System.out.println(getAutoSessionsInEachManualSession());
        aft = System.nanoTime();
        System.out.printf(String.valueOf(aft-b4));

        b4 = System.nanoTime();
        System.out.println(getAutoSessionsInEachManualSession());
        aft = System.nanoTime();
        System.out.printf(String.valueOf(aft-b4));
    }
}
