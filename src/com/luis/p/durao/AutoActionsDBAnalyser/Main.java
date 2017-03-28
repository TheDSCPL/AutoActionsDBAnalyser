package com.luis.p.durao.AutoActionsDBAnalyser;
import com.luis.p.durao.AutoActionsDBAnalyser.DBs.DBconnection;
import com.luis.p.durao.AutoActionsDBAnalyser.DBs.Session;

import static com.luis.p.durao.AutoActionsDBAnalyser.DBs.DBconnection.*;

public class Main {
    //static DBconnection db;

    public static void main(String[] args) {
        automatic = new DBconnection("auto.db");
        manual = new DBconnection("auto.db");
        //manual = new DBconnection("manual.db");
        Session autoSession = automatic.getSession();
        autoSession.tableResult.print();
        System.out.println(autoSession.getSessionIntervals());

        long aft, b4 = System.nanoTime();
        System.out.println(Process.getAutoSessionsInEachManualSession());
        aft = System.nanoTime();
        System.out.printf(String.valueOf(aft-b4));

        b4 = System.nanoTime();
        System.out.println(Process.getAutoSessionsInEachManualSession());
        aft = System.nanoTime();
        System.out.printf(String.valueOf(aft-b4));
    }
}
