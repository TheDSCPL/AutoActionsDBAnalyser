package com.luis.p.durao.AutoActionsDBAnalyser.DBs;

import com.luis.p.durao.AutoActionsDBAnalyser.Structs.Coordinate;
import com.sun.istack.internal.Nullable;

import static com.luis.p.durao.AutoActionsDBAnalyser.Structs.TableResult.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

public class Gslocation extends Table {
    Gslocation(DBConnection db) {
        super(db,
                Arrays.asList(new Class<?>[] {
                        INTEGER,
                        INTEGER,
                        INTEGER,
                        INTEGER,
                        INTEGER,
                        DOUBLE,
                        DOUBLE,
                        DOUBLE
                }),
                "session_id",
                "seconds",
                "millis",
                "gpstime",
                "gpsmillis",
                "lat",
                "lon",
                "alt");
    }

    public Map<Integer, Double> getFirstLocationTimeOfEachSession() {
        Map<Integer, Double> ret = new HashMap<>();
        try {
            Statement stm = db.c.createStatement();
            ResultSet rs = stm.executeQuery("SELECT session_id, MIN((gpstime + gpsmillis/1000.0)) AS gps_secs\n" +
                    "FROM gslocation\n" +
                    "GROUP BY session_id\n" +
                    "ORDER BY session_id");
            while(rs.next()) {
                ret.putIfAbsent(rs.getInt(1),rs.getDouble(2));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.exit(15);
        }
        return ret;
    }

    public Map<Integer, Double> getLastLocationTimeOfEachSession() {
        Map<Integer, Double> ret = new HashMap<>();
        try {
            Statement stm = db.c.createStatement();
            ResultSet rs = stm.executeQuery("SELECT session_id, MAX((gpstime + gpsmillis/1000.0)) AS gps_secs\n" +
                    "FROM gslocation\n" +
                    "GROUP BY session_id\n" +
                    "ORDER BY session_id");
            while(rs.next()) {
                ret.putIfAbsent(rs.getInt(1),rs.getDouble(2));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.exit(15);
        }
        return ret;
    }

    /**
     *
     * @param session_id
     * @param from UNIX epoch time in seconds. Use a negative number to ignore lower boundary.
     * @param to UNIX epoch time in seconds. Use a negative number to ignore upper boundary.
     * @return returns a list with all Coordinates of a session in a time interval.
     */
    private List<Coordinate> getAllCoordinatesInTimeInterval(int session_id, double from, double to) {
        return tableResult.tuples.stream()
                .filter( e ->
                    e.values.get(0) != null &&
                    e.values.get(3) != null &&
                    e.values.get(4) != null &&
                    e.values.get(5) != null &&
                    e.values.get(6) != null &&
                    e.values.get(7) != null &&
                    (Integer)e.values.get(0) == session_id &&
                    (from<0 || ((Integer)e.values.get(3) + (Integer)e.values.get(4)/1000.0 >= from)) &&
                    (to<0 || ((Integer)e.values.get(3) + (Integer)e.values.get(4)/1000.0 <= to)))
                .sorted((a,b) -> Double.compare((Integer)a.values.get(3) + (Integer)a.values.get(4)/1000.0,(Integer)b.values.get(3) + (Integer)b.values.get(4)/1000.0)) //order by time
                .map(o -> new Coordinate(
                    (Double)o.values.get(5),
                    (Double)o.values.get(6),
                    (Double)o.values.get(7)))
                .collect(Collectors.toList());
    }

    /**
     * @param session_id session to get distance from
     * @param from UNIX epoch time in seconds. Use a negative number to ignore lower boundary.
     * @param to UNIX epoch time in seconds. Use a negative number to ignore upper boundary.
     * @return total distance of all points between from and to
     */
    public double getTotalDistanceFromTo(int session_id, double from, double to) {
        return getTotalDistance(getAllCoordinatesInTimeInterval(session_id, from, to));
    }

    public double getDiameterDistanceFromTo(int session_id, double from, double to) {
        return getDiameterDistanceFromTo(getAllCoordinatesInTimeInterval(session_id,from,to));
    }

    private double getDiameterDistanceFromTo(@Nullable List<Coordinate> l) {
        double ret = 0.0;
        if(l != null && l.size() > 1)
            ret = Coordinate.distance(l.get(0), l.get(l.size()-1));
        return ret;
    }

    private double getTotalDistance(@Nullable List<Coordinate> l) {
        double sum = 0.0;
        if(l != null)
            for(int i = 1 ; i < l.size() ; i++)
                sum+=Coordinate.distance(l.get(i-1),l.get(i));
        return sum;
    }
}
