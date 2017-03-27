package DBs;
import org.sqlite.*;

import java.sql.*;

public class DBconnection {
    public static DBconnection manual=null, automatic=null;

    public DBconnection () {
        Connection c = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:manual.db");
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
        System.out.println("Opened database successfully");
    }

}
