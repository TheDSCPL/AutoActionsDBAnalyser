import DBs.DBconnection;
import static DBs.DBconnection.*;

public class Main {
    static DBconnection db;

    public static void main(String[] args) {
        manual = new DBconnection("manual.db");
        automatic = new DBconnection("auto.db");
        automatic.getSession().tableResult.print();
    }
}
