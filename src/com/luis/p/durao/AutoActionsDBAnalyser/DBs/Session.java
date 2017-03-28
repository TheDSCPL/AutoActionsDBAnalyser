package com.luis.p.durao.AutoActionsDBAnalyser.DBs;

import com.luis.p.durao.AutoActionsDBAnalyser.Structs.Interval;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class Session extends Table {

    Session(DBconnection db) {
        super(db,
                Arrays.asList(new Class<?>[] {
                        Integer.class,
                        Integer.class,
                        Integer.class,
                        Integer.class,
                        Integer.class,
                        Integer.class,
                        Integer.class,
                        Integer.class,
                        Integer.class
                }),
                "session_id",
                "start_time",
                "start_millis",
                "start_elapsed_seconds",
                "start_elapsed_millis",
                "end_seconds",
                "end_millis",
                "end_elapsed_seconds",
                "end_elapsed_millis");
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
