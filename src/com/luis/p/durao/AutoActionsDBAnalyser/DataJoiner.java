package com.luis.p.durao.AutoActionsDBAnalyser;

import com.luis.p.durao.AutoActionsDBAnalyser.DBs.DBConnection;
import com.luis.p.durao.AutoActionsDBAnalyser.DBs.Gslocation;
import com.luis.p.durao.AutoActionsDBAnalyser.DBs.Session;
import com.luis.p.durao.AutoActionsDBAnalyser.Structs.TableResult;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.regex.Pattern;

import static com.luis.p.durao.AutoActionsDBAnalyser.DBs.DBConnection.*;

/**
 * This class gathers all the data from the input DBs and CSV files, creates temporary databases for the manual and automatic sessions
 */
public class DataJoiner {

    public static HashMap<Integer,SessionOrigin> sessions = new HashMap<>();

    public static class SessionOrigin {
        public final File file;
        public final int localSession;
        private SessionOrigin(File file, int localSession) {
            this.file = file;
            this.localSession = localSession;
        }

        @Override
        public boolean equals(Object obj) {
            if(obj == null)
                return false;
            if(!(obj instanceof SessionOrigin))
                return false;
            if(obj == this)
                return true;
            SessionOrigin so = (SessionOrigin)obj;
            if(this.file==null) {
                if(so.file == null)
                    return this.localSession==so.localSession;
                return false;
            }
            else
                return so.localSession==this.localSession && this.file.equals(so.file);
        }

        @Override
        public String toString() {
            return "{\"" + file.toString().replaceAll(".*\\\\","") + "\", " + localSession + "}";
        }
    }

    public static void createDBFromSelectedSources(List<File> files) {
        if(automatic != null)
            try {
                automatic.c.close();
            } catch (SQLException e) {

            }
        if(manual != null)
            try {
                manual.c.close();
            } catch (SQLException e) {

            }
        Connection tempDB = null;

        {
            String tempDBName;

            File tempDBFile = null;
            try {
                tempDBFile = File.createTempFile("temp_db",".db");
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(18);
            }
            tempDBName = tempDBFile.getAbsoluteFile().getAbsolutePath();
            System.out.println(tempDBName);
            tempDBFile.delete();

            try {
                Class.forName("org.sqlite.JDBC");
                tempDB = DriverManager.getConnection("jdbc:sqlite:" + tempDBName);

                tempDB.prepareStatement("PRAGMA foreign_keys = ON;").executeUpdate();
                System.out.println();
                tempDB.prepareStatement( "CREATE TABLE IF NOT EXISTS session (\n" +
                        "session_id integer PRIMARY KEY,\n" +
                        "start_time integer,\n" +
                        "start_millis integer,\n" +
                        "start_elapsed_seconds integer,\n" +
                        "start_elapsed_millis integer,\n" +
                        "end_seconds integer,\n" +
                        "end_millis integer,\n" +
                        "end_elapsed_seconds integer,\n" +
                        "end_elapsed_millis integer);"
                    ).executeUpdate();
                tempDB.prepareStatement( "CREATE TABLE IF NOT EXISTS autoSessions (\n" +
                        "session_id integer PRIMARY KEY,\n" +
                        "start_time integer,\n" +
                        "start_millis integer,\n" +
                        "start_elapsed_seconds integer,\n" +
                        "start_elapsed_millis integer,\n" +
                        "end_seconds integer,\n" +
                        "end_millis integer,\n" +
                        "end_elapsed_seconds integer,\n" +
                        "end_elapsed_millis integer,\n" +
                        "FOREIGN KEY (session_id) REFERENCES session(session_id));"
                ).executeUpdate();
                tempDB.prepareStatement( "CREATE TABLE IF NOT EXISTS manualSessions (\n" +
                        "session_id integer PRIMARY KEY,\n" +
                        "start_time integer,\n" +
                        "start_millis integer,\n" +
                        "start_elapsed_seconds integer,\n" +
                        "start_elapsed_millis integer,\n" +
                        "end_seconds integer,\n" +
                        "end_millis integer,\n" +
                        "end_elapsed_seconds integer,\n" +
                        "end_elapsed_millis integer,\n" +
                        "FOREIGN KEY (session_id) REFERENCES session(session_id));"
                ).executeUpdate();
                tempDB.prepareStatement( "CREATE TABLE IF NOT EXISTS isAutoSessionOf (\n" +
                        "auto_id integer,\n" +
                        "manual_id integer,\n" +
                        "PRIMARY KEY (auto_id,manual_id)," +
                        "FOREIGN KEY (auto_id) REFERENCES autoSessions(session_id)," +
                        "FOREIGN KEY (manual_id) REFERENCES manualSessions(session_id));"
                ).executeUpdate();
                tempDB.prepareStatement("DELETE FROM session;").executeUpdate();
                tempDB.prepareStatement("CREATE TABLE IF NOT EXISTS gslocation (\n" +
                                                    "session_id integer NOT NULL,\n" +
                                                    "seconds integer,\n" +
                                                    "millis integer,\n" +
                                                    "gpstime integer,\n" +
                                                    "gpsmillis integer,\n" +
                                                    "lat real,\n" +
                                                    "lon real,\n" +
                                                    "alt real," +
                                                    "FOREIGN KEY (session_id) REFERENCES session(session_id));"
                    ).executeUpdate();
                tempDB.prepareStatement("DELETE FROM gslocation;").executeUpdate();
                if(!tempDB.getAutoCommit())
                    tempDB.commit();
            } catch ( Exception e ) {
                System.err.println( e.getClass().getName() + ": " + e.getMessage() );
                System.exit(1);
            }
        }   //temp SQL connections started and tables created

        {
            HashSet<File> errorFiles = new HashSet<>();
            for(File f : files) {
                if(!dumpInto(f,tempDB))
                    errorFiles.add(f);
            }
            if(!errorFiles.isEmpty())
            {
                StringBuilder sb = new StringBuilder("Couldn't import the following file(s):");
                for(File f : errorFiles)
                    sb.append(System.lineSeparator()).append(f);
                System.out.println(sb);
            }
            System.out.println(sessions);
        }   //"session" and "gslocation" filled

        {
            try {
                tempDB.prepareStatement("INSERT INTO manualSessions \n" +
                        "SELECT *\n" +
                        "FROM\n" +
                        "   (\n" +
                        "      SELECT DISTINCT A.session_id AS \"session_id\"\n" +
                        "       FROM session AS A, session AS B\n" +
                        "       WHERE \t(A.session_id <> B.session_id) AND\n" +
                        "           ((B.start_time + B.start_millis/1000.0) >= (A.start_time + A.start_millis/1000.0)) AND\n" +
                        "           ((B.start_time + B.start_millis/1000.0) < (A.end_seconds + A.end_millis/1000.0))\n" +
                        "   ) AS foo JOIN\n" +
                        "   (\n" +
                        "       SELECT *\n" +
                        "       FROM session\n" +
                        "   ) AS bar USING (session_id);").executeUpdate();
                if(!tempDB.getAutoCommit())
                    tempDB.commit();

                tempDB.prepareStatement("INSERT INTO autoSessions \n" +
                        "SELECT *\n" +
                        "FROM\n" +
                        "   (\n" +
                        "      SELECT DISTINCT B.session_id AS \"session_id\"\n" +
                        "       FROM session AS A, session AS B\n" +
                        "       WHERE \t(A.session_id <> B.session_id) AND\n" +
                        "           ((B.start_time + B.start_millis/1000.0) >= (A.start_time + A.start_millis/1000.0)) AND\n" +
                        "           ((B.start_time + B.start_millis/1000.0) < (A.end_seconds + A.end_millis/1000.0))\n" +
                        "   ) AS foo JOIN\n" +
                        "   (\n" +
                        "       SELECT *\n" +
                        "       FROM session\n" +
                        "   ) AS bar USING (session_id);").executeUpdate();
                if(!tempDB.getAutoCommit())
                    tempDB.commit();

                tempDB.prepareStatement("INSERT INTO isAutoSessionOf\n" +
                        "SELECT DISTINCT B.session_id,A.session_id\n" +
                        "FROM session AS A, session AS B\n" +
                        "WHERE  (A.session_id <> B.session_id) AND\n" +
                        "       ((B.start_time + B.start_millis/1000.0) >= (A.start_time + A.start_millis/1000.0)) AND\n" +
                        "       ((B.start_time + B.start_millis/1000.0) < (A.end_seconds + A.end_millis/1000.0))").executeUpdate();
                if(!tempDB.getAutoCommit())
                    tempDB.commit();
            } catch (SQLException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }   //manualSessions, autoSessions and isAutoSessionOf computed
    }

    @SuppressWarnings("all")
    private static Integer getUUID(File f, int localSession) {
        try {
            return sessions.entrySet().stream().filter(o -> (o.getValue()!= null && o.getValue().file==f && o.getValue().localSession==localSession)).map(Map.Entry::getKey).findFirst().get();
        } catch (java.util.NoSuchElementException ex) {
            return null;
        }
    }

    @SuppressWarnings("all")
    private static int getUUIDOrCreate(File f, int localSession) {
        Integer ret = getUUID(f,localSession);
        if(ret == null) {
            while( sessions.containsKey( ret = Math.abs(UUID.randomUUID().hashCode()) ) );
            sessions.put(ret,new SessionOrigin(f,localSession));
        }
        return ret;
    }

    /**
     * Dumps the source represented by <i>f</i> into the DB in <i>conn</i>
     * @param f
     * @param conn
     * @return true if successfully dumped
     */
    private static boolean dumpInto(File f, Connection conn) {
        if(Pattern.compile("^.*[.](db)$").matcher(f.getName()).matches())
            return dumpDBInto(f,conn);
        if(Pattern.compile("^.*[.](csv)$").matcher(f.getName()).matches())
            return dumpCSVInto(f,conn);
        return false;
    }

    /**
     * Dumps the csv file represented by <i>f</i> into the DB in <i>conn</i>
     * @param f
     * @param conn
     * @return true if successfully dumped
     */
    //TODO: implement
    private static boolean dumpCSVInto(File f, Connection conn) {
        return true;
    }

    /**
     * Dumps the SQLite DB represented by <i>f</i> into the DB in <i>conn</i>
     * @param f
     * @param conn
     * @return true if successfully dumped
     */
    private static boolean dumpDBInto(File f, Connection conn) {
        if(f==null || conn==null)
            return false;
        try {
            DBConnection temp = new DBConnection(f.getAbsoluteFile().getAbsolutePath());
            Session _tempSession = temp.getSession();
            session_outer_cycle:
            for (TableResult.Tuple t : _tempSession.tableResult.tuples) {
                PreparedStatement ps = conn.prepareStatement("INSERT INTO session VALUES (?,?,?,?,?,?,?,?,?)");
                for(int i=0 ; i<9 ; i++) {
                    Integer o = (Integer)t.values.get(i);
                    if(o==null)
                        continue session_outer_cycle;
                    if(i==0)    //transform session_id
                        o= getUUIDOrCreate(f,o);
                    ps.setInt(i+1, o);
                }
                ps.executeUpdate();
            }
            Gslocation _tempGSL = temp.getGslocation();
            gslocation_outer_cycle:
            for (TableResult.Tuple t : _tempGSL.tableResult.tuples) {
                Integer sess_id = getUUID(f,(Integer) t.values.get(0));
                if(sess_id==null)
                    continue;
                PreparedStatement ps = conn.prepareStatement("INSERT INTO gslocation VALUES (?,?,?,?,?,?,?,?)");
                ps.setInt(1,sess_id);
                int i=1;
                try {
                    for(i=1; i<8; i++) {
                        Object o = t.values.get(i);
                        if(o==null)
                            continue gslocation_outer_cycle;
                        //ps.setObject(i+1,o);
                        if(i<5)
                            ps.setInt(i+1,(Integer) o);
                        else
                            ps.setDouble(i+1,(Double)o);
                    }
                    ps.executeUpdate();
                } catch (ClassCastException ex) {
                    ex.printStackTrace();
                    System.err.println(i);
                    return false;
                }

            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
