import DBs.DBconnection;
import Structs.Interval;

import static DBs.DBconnection.*;

public class Main {
    //static DBconnection db;

    public static void main(String[] args) {
        manual = new DBconnection("manual.db");
        automatic = new DBconnection("auto.db");
        DBconnection.Session autoSession = automatic.getSession();
        autoSession.tableResult.print();
        /*for(Interval i : autoSession.getSessionIntervals().) {

        }*/
    }
}
