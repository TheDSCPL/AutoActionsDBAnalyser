package com.luis.p.durao.AutoActionsDBAnalyser;
import com.luis.p.durao.AutoActionsDBAnalyser.DBs.DBconnection;
import com.luis.p.durao.AutoActionsDBAnalyser.DBs.Session;

import java.lang.*;

import static com.luis.p.durao.AutoActionsDBAnalyser.DBs.DBconnection.*;

public class Main {
    //static DBconnection db;

    public static void main(String[] args) {
        automatic = new DBconnection("auto.db");
        manual = new DBconnection("auto.db");
        //manual = new DBconnection("manual.db");
        Session autoSession = automatic.getSession();
        autoSession.tableResult.print();
        //System.out.println(autoSession.getSessionIntervals());

        System.out.println(automatic.getGslocation().getFirstLocationTimeOfEachSession());

        System.out.println(Process.getTotalLostDistancePerManualSession());

        System.out.println(manual.getGslocation().getTotalDistanceFromTo(5, -1, -1));

        System.out.println(manual.getGslocation().getDiameterDistanceFromTo(5, -1, -1));

        System.out.println(Process.getLostTimePerManualSession());

        System.out.println(Process.getDurationOfTestPerManualSession());

        System.out.println(Process.getStopTimePerManualSession());

        /*//
        long aft, b4 = System.nanoTime();
        System.out.println(Process.getAutoSessionsInEachManualSession());
        aft = System.nanoTime();
        System.out.printf(String.valueOf(aft-b4));

        b4 = System.nanoTime();
        System.out.println(Process.getAutoSessionsInEachManualSession());
        aft = System.nanoTime();
        System.out.printf(String.valueOf(aft-b4));*/
    }
}
