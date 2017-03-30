package com.luis.p.durao.AutoActionsDBAnalyser.DBs;

import java.sql.*;

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
        //new Session();
    }

    public DBconnection reload() {
        session = null; //to force the session getter to reinitialize

        return this;    //to nest method calls
    }

    private Session session = null;
    public Session getSession() {
        return (session==null ? (session=new Session(this)) : session);
    }

    private Gslocation gslocation = null;
    public Gslocation getGslocation() {
        return (gslocation==null ? (gslocation=new Gslocation(this)) : gslocation);
    }
}
