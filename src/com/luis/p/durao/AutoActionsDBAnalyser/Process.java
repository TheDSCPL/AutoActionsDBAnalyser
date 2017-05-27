package com.luis.p.durao.AutoActionsDBAnalyser;

import com.luis.p.durao.AutoActionsDBAnalyser.DBs.DBConnection;
import com.luis.p.durao.AutoActionsDBAnalyser.Structs.Interval;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Process {

    //Map<manualSession, listOfAutoSessions>
    private static Map<Integer, List<Integer>> autoSessionsInEachManualSession = null;

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

    //Map<autoSession, firstTime>
    private static Map<Integer, Double> lastLocationTimeOfEachAutomaticSessionThatIsTheLastOneOfAManualOne = null;

    //Map<manualSession, firstAutoSessionStartTime>
    private static Map<Integer, Double> firstLocationTimeOfTheFirstAutoSessionInEachManualSession = null;

    private static Integer getMax(List<Integer> l) {
        return l.size() == 0 ? null : l.stream().max(Integer::compareTo).get();
    }

    public static void init() {
        if(autoSessionsInEachManualSession == null) {
            Map<Integer, Interval<Integer>> autoIntervals = DBConnection.automatic.getSession().getSessionIntervals();
            Map<Integer, Interval<Integer>> manualIntervals = DBConnection.manual.getSession().getSessionIntervals();

            autoSessionsInEachManualSession = new HashMap<>();
            for(Map.Entry<Integer, Interval<Integer>> em : manualIntervals.entrySet()) {
                for (Map.Entry<Integer, Interval<Integer>> ea : autoIntervals.entrySet()) {
                    if (ea.getValue().intersect(em.getValue())) {
                        autoSessionsInEachManualSession.putIfAbsent(em.getKey(), new ArrayList<>());
                        autoSessionsInEachManualSession.get(em.getKey()).add(ea.getKey());
                    }
                }
            }
        }

        if(firstAutoSessionInEachManualSession == null)
            firstAutoSessionInEachManualSession = autoSessionsInEachManualSession.entrySet().stream()
                    .filter(o->o.getValue().stream().min(Integer::compareTo).isPresent())
                    .collect(Collectors.toMap(
                            o->o.getKey(),
                            o->o.getValue().stream().min(Integer::compareTo).get())
                    );

        if(firstLocationTimeOfEachManualSession == null)
            firstLocationTimeOfEachManualSession = DBConnection.manual.getGslocation().getFirstLocationTimeOfEachSession();

        if(firstLocationTimeOfEachAutomaticSession == null)
            firstLocationTimeOfEachAutomaticSession =  DBConnection.automatic.getGslocation().getFirstLocationTimeOfEachSession();

        if(lastLocationTimeOfEachManualSession == null)
            lastLocationTimeOfEachManualSession = DBConnection.manual.getGslocation().getLastLocationTimeOfEachSession();

        if(lastLocationTimeOfEachAutomaticSession == null)
            lastLocationTimeOfEachAutomaticSession = DBConnection.automatic.getGslocation().getLastLocationTimeOfEachSession();

        if(lastLocationTimeOfEachAutomaticSessionThatIsTheLastOneOfAManualOne == null) {
            Map<Integer,Integer> lastAutoSessionPerManualSession = autoSessionsInEachManualSession
                    .entrySet().stream().collect(Collectors.toMap(
                            o -> o.getKey(),
                            o -> o.getValue().size() == 0 ? null : o.getValue().stream().max(Integer::compareTo).get()));
            lastLocationTimeOfEachAutomaticSessionThatIsTheLastOneOfAManualOne = autoSessionsInEachManualSession.entrySet().stream()
                    .filter(o -> o.getValue().stream().max(Integer::compareTo).isPresent())
                    .collect(Collectors.toMap(
                            o -> o.getValue().stream().max(Integer::compareTo).get(),
                            o -> lastLocationTimeOfEachAutomaticSession.get(o.getValue().stream().max(Integer::compareTo).get())
                    ));
//            lastLocationTimeOfEachAutomaticSessionThatIsTheLastOneOfAManualOne = lastLocationTimeOfEachAutomaticSession.entrySet().stream()
//                    .filter(o -> lastAutoSessionPerManualSession.containsKey(o.getKey()))
//                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

//            System.out.println(autoSessionsInEachManualSession);
//            System.out.println(lastLocationTimeOfEachAutomaticSession);
//            System.out.println(lastLocationTimeOfEachAutomaticSessionThatIsTheLastOneOfAManualOne);
        }

        if(firstLocationTimeOfTheFirstAutoSessionInEachManualSession == null)
            firstLocationTimeOfTheFirstAutoSessionInEachManualSession =
                    firstAutoSessionInEachManualSession.entrySet().stream()
                            .filter(o -> firstLocationTimeOfEachAutomaticSession.containsKey(o.getValue()))
                            .collect(Collectors.toMap(
                            o -> o.getKey(),
                            o -> firstLocationTimeOfEachAutomaticSession.get(o.getValue())
                    ));
    }

    private static int getManualSessionFromAutomatic(int _auto) {
        final Integer auto = new Integer(_auto);
        for(Map.Entry<Integer, List<Integer>> e : autoSessionsInEachManualSession.entrySet()) {
            if(e.getValue().contains(auto))
                return e.getKey();
        }
        return -1;
    }

    public static Map<Integer, Integer> getFalseStopsOfAutoActionsPerManualSession() {
        init();
        return autoSessionsInEachManualSession.entrySet().stream().collect(Collectors.toMap( o->o.getKey() , o->(o.getValue().size()-1) ));
    }

    public static Map<Integer, Double> getTotalLostDistancePerManualSession() {
        //-pegar nas intersações e descobrir a primeira sessão automática de cada.
        //-ir à primeira sessão automática de cada manual e descobrir o tempo de início dessa sessão
        //-ir à sessão manual e somar todas as distâncias

        init();

        return firstLocationTimeOfTheFirstAutoSessionInEachManualSession.entrySet().stream().collect(Collectors.toMap(
                o -> o.getKey(),
                o -> DBConnection.manual.getGslocation().getTotalDistanceFromTo(o.getKey(),-1,o.getValue())
        ));
    }

    public static Map<Integer, Double> getTotalLostDiameterDistancePerManualSession() {
        init();
        return firstLocationTimeOfTheFirstAutoSessionInEachManualSession.entrySet().stream().collect(Collectors.toMap(
                o -> o.getKey(),
                o -> DBConnection.manual.getGslocation().getDiameterDistanceFromTo(o.getKey(),-1,o.getValue())
        ));
    }

    public static Map<Integer, Double> getLostTimePerManualSession() {
        return firstLocationTimeOfTheFirstAutoSessionInEachManualSession.entrySet().stream().collect(Collectors.toMap(
                o -> o.getKey(),
                o -> o.getValue() - firstLocationTimeOfEachManualSession.getOrDefault(o.getKey(),Double.NaN)
        ));
    }

    public static Map<Integer, Double> getDurationOfTestPerManualSession() {
        //System.out.println("lastLocationTimeOfEachAutomaticSession = " + lastLocationTimeOfEachAutomaticSession);
        //System.out.println("firstLocationTimeOfEachManualSession = " + firstLocationTimeOfEachManualSession);
        return lastLocationTimeOfEachAutomaticSessionThatIsTheLastOneOfAManualOne.entrySet().stream()
                .filter(e -> getManualSessionFromAutomatic(e.getKey()) > 0)
                .collect(Collectors.toMap(
                o -> getManualSessionFromAutomatic(o.getKey()),
                o -> o.getValue() - firstLocationTimeOfEachManualSession.getOrDefault(getManualSessionFromAutomatic(o.getKey()), Double.NaN)
        ));
    }

    public static Map<Integer, Double> getStopTimePerManualSession() {
        return lastLocationTimeOfEachAutomaticSessionThatIsTheLastOneOfAManualOne.entrySet().stream()
                .filter(e -> getManualSessionFromAutomatic(e.getKey()) > 0)
                .collect(Collectors.toMap(
                o -> getManualSessionFromAutomatic(o.getKey()),
                o -> o.getValue() - lastLocationTimeOfEachManualSession.getOrDefault(getManualSessionFromAutomatic(o.getKey()), Double.NaN)
        ));
    }
}
