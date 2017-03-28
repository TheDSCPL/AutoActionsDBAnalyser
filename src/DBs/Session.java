package DBs;

import Structs.Interval;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.stream.Collectors;

public class Session extends Table {

    Session(DBconnection db) {
        super(db,
                "session",
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
            if(!db.c.isValid(2)) {
                System.err.println("DB \"" + db.fileName + "\" is not valid!");
                System.exit(2);
            }

            Statement statement = db.c.createStatement();
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

    @SuppressWarnings("unchecked")
    public Map<Integer, Interval<Integer>> getSessionIntervals() {
        return tableResult.tuples.stream()
                .filter(o ->
                                o.values.get(1)!=null &&
                                        //o.values.get(2)!=null &&
                                        o.values.get(5)!=null
                        //o.values.get(6)!=null
                ).collect(Collectors.toMap(
                        o->(Integer)o.values.get(0),
                        o->new Interval(
                                (Integer)o.values.get(1),
                                (Integer)o.values.get(5)
                        )
                        )
                );
    };
}
