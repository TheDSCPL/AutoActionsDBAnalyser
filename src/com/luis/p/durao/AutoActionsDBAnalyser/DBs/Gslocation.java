package com.luis.p.durao.AutoActionsDBAnalyser.DBs;

import java.util.Arrays;

public class Gslocation extends Table {
    Gslocation(DBconnection db) {
        super(db,
                Arrays.asList(new Class<?>[] {
                        Integer.class,
                        Integer.class,
                        Integer.class,
                        Integer.class,
                        Integer.class,
                        Double.class,
                        Double.class,
                        Double.class
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
}
