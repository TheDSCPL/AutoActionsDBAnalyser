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

    //Map<manualSession, autoSession>
    private static Map<Integer, Integer> firstAutoSessionInEachManualSession = null;

    //Map<manualSession, firstTime>
    private static Map<Integer, Double> firstLocationTimeOfEachManualSession = null;

    //Map<autoSession, firstTime>
    private static Map<Integer, Double> firstLocationTimeOfEachAutomaticSession = null;

    //Map<manualSession, firstTime>
    private static Map<Integer, Double> lastLocationTimeOfEachManualSession = null;

    //Map<autoSession, firstTime>
    private static Map<Integer, Double> lastLocationTimeOfEachAutomaticSession = null;

    //Map<manualSession, firstAutoSessionStartTime>
    private static Map<Integer, Double> firstLocationTimeOfTheFirstAutoSessionInEachManualSession = null;

    private static void init() {
        getAutoSessionsInEachManualSession();

        if(firstAutoSessionInEachManualSession == null)
            firstAutoSessionInEachManualSession = autoSessionsInEachManualSession.entrySet().stream()
                    .filter(o->o.getValue().stream().min(Integer::compareTo).isPresent())
                    .collect(Collectors.toMap(
                            o->o.getKey(),
                            o->o.getValue().stream().min(Integer::compareTo).get())
                    );

        if(firstLocationTimeOfEachManualSession == null)
            firstLocationTimeOfEachManualSession = DBconnection.manual.getGslocation().getFirstLocationTimeOfEachSession();

        if(firstLocationTimeOfEachAutomaticSession == null)
            firstLocationTimeOfEachAutomaticSession =  DBconnection.automatic.getGslocation().getFirstLocationTimeOfEachSession();

        if(lastLocationTimeOfEachManualSession == null)
            lastLocationTimeOfEachManualSession = DBconnection.manual.getGslocation().getLastLocationTimeOfEachSession();

        if(lastLocationTimeOfEachAutomaticSession == null)
            lastLocationTimeOfEachAutomaticSession = DBconnection.automatic.getGslocation().getLastLocationTimeOfEachSession();

        if(firstLocationTimeOfTheFirstAutoSessionInEachManualSession == null)
            firstLocationTimeOfTheFirstAutoSessionInEachManualSession =
                    firstAutoSessionInEachManualSession.entrySet().stream()
                            .filter(o -> firstLocationTimeOfEachAutomaticSession.containsKey(o.getValue()))
                            .collect(Collectors.toMap(
                            o -> o.getKey(),
                            o -> firstLocationTimeOfEachAutomaticSession.get(o.getValue())
                    ));
    }

    public static Map<Integer, Double> getTotalLostDistancePerManualSession() {
        //-pegar nas intersações e descobrir a primeira sessão automática de cada.
        //-ir à primeira sessão automática de cada manual e descobrir o tempo de início dessa sessão
        //-ir à sessão manual e somar todas as distâncias

        init();

        return firstLocationTimeOfTheFirstAutoSessionInEachManualSession.entrySet().stream().collect(Collectors.toMap(
                o -> o.getKey(),
                o -> DBconnection.manual.getGslocation().getTotalDistanceFromTo(o.getKey(),-1,o.getValue())
        ));
    }

    public static Map<Integer, Double> getTotalDiameterDistancePerManualSession() {
        init();
        return firstLocationTimeOfTheFirstAutoSessionInEachManualSession.entrySet().stream().collect(Collectors.toMap(
                o -> o.getKey(),
                o -> DBconnection.manual.getGslocation().getDiameterDistanceFromTo(o.getKey(),-1,o.getValue())
        ));
    }

    public static Map<Integer, Double> getLostTimePerManualSession() {
        return firstLocationTimeOfTheFirstAutoSessionInEachManualSession.entrySet().stream().collect(Collectors.toMap(
                o -> o.getKey(),
                o -> o.getValue() - firstLocationTimeOfEachManualSession.getOrDefault(o.getKey(),Double.NaN)
        ));
    }

    public static Map<Integer, Double> getDurationOfTestPerManualSession() {
        return lastLocationTimeOfEachAutomaticSession.entrySet().stream().collect(Collectors.toMap(
                o -> o.getKey(),
                o -> o.getValue() - firstLocationTimeOfEachManualSession.getOrDefault(o.getKey(), Double.NaN)
        ));
    }

    public static Map<Integer, Double> getStopTimePerManualSession() {
        return lastLocationTimeOfEachAutomaticSession.entrySet().stream().collect(Collectors.toMap(
                o -> o.getKey(),
                o -> o.getValue() - lastLocationTimeOfEachManualSession.getOrDefault(o.getKey(), Double.NaN)
        ));
    }
}
