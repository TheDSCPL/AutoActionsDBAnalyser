package DBs;
import Structs.Interval;
import Structs.TableResult;
import org.sqlite.*;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class DBconnection {
    public static DBconnection manual=null, automatic=null;

    public final Connection c;
    public final String fileName;

    public DBconnection (String file) {
        Connection c = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:" + file);
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(1);
        }
        this.c = c;
        this.fileName = file;
        System.out.println("Opened database \"" + file + "\" successfully");
        new Session();
    }

    private static List<Class<?>> getDummyClasses(int n) {
        List<Class<?>> temp = new ArrayList<>(n);
        for(int i = 0 ; i<n ; i++)
            temp.add(Object.class);
        return temp;
    }

    public abstract class Table {

        Table(String tableName, String... columns) {
            this(tableName, getDummyClasses(columns.length), columns);
        }

        Table(String tableName, List<Class<?>> types, String... columns) {
            tableResult = new TableResult(tableName,types);
            tableResult.setColumnNames(columns);
        }

        public final TableResult tableResult;
    }

    public class Session extends Table {

        private Session() {
            super("session",
                    "session_id",
                    "start_time",
                    "start_millis",
                    "start_elapsed_seconds",
                    "start_elapsed_millis",
                    "end_seconds",
                    "end_millis",
                    "end_elapsed_seconds",
                    "end_elapsed_millis");
            ResultSet res = null;
            try {
                if(!c.isValid(2)) {
                    System.err.println("DB \"" + fileName + "\" is not valid!");
                    System.exit(2);
                }

                Statement statement = c.createStatement();
                final String concatColumns = tableResult.columnNames.stream().reduce("",(a,b) -> a+(a.equals("")?"":", ")+b);
                final String query = "SELECT " + concatColumns + " FROM " + tableResult.tableName + ";";
                //System.out.println(query);
                res = statement.executeQuery(query);
                //res.close();

            } catch (SQLException e) {
                e.printStackTrace();
                System.exit(3);
            }
            tableResult.importResultSet(res);
            try {
                res.close();
            } catch (SQLException e) {
                e.printStackTrace();
                System.exit(13);
            }
        }

        //public Map<Integer, Interval<Integer>> get
    }

    public Session getSession() {
        return new Session();
    }
}
