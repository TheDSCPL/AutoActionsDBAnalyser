import DBs.DBconnection;
import Structs.Interval;

import static DBs.DBconnection.*;

public class Main {
    //static DBconnection db;

    public static void main(String[] args) {
        /*manual = new DBconnection("manual.db");
        automatic = new DBconnection("auto.db");
        automatic.getSession().tableResult.print();*/
        Interval a = new Interval(1.2,1.7), b = new Interval(1.0,2.0);
        System.out.println(a.intersect(b));
    }
}
