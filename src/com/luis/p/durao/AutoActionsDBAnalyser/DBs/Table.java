package com.luis.p.durao.AutoActionsDBAnalyser.DBs;

import com.luis.p.durao.AutoActionsDBAnalyser.Structs.TableResult;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

abstract class Table {

    private static List<Class<?>> getDummyClasses(int n) {
        List<Class<?>> temp = new ArrayList<>(n);
        for(int i = 0 ; i<n ; i++)
            temp.add(Object.class);
        return temp;
    }

    protected final DBconnection db;

    protected Table(DBconnection db, String... columns) {
        this(db, null, getDummyClasses(columns.length), columns);
    }

    protected Table(DBconnection db, List<Class<?>> types, String... columns) {
        this(db, null, types, columns);
    }

    /*protected Table(DBconnection db, String tableName, String... columns) {
        this(db, tableName, getDummyClasses(columns.length), columns);
    }*/

    private Table(DBconnection db, String tableName, List<Class<?>> types, String... columns) {
        tableResult = new TableResult( (tableName==null ? this.getClass().getSimpleName().toLowerCase() : tableName) , types);
        tableResult.setColumnNames(columns);
        this.db = db;

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

    public final TableResult tableResult;
}