package DBs;

import Structs.TableResult;

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

    protected Table(DBconnection db, String tableName, String... columns) {
        this(db, tableName, getDummyClasses(columns.length), columns);
    }

    protected Table(DBconnection db, String tableName, List<Class<?>> types, String... columns) {
        tableResult = new TableResult(tableName,types);
        tableResult.setColumnNames(columns);
        this.db = db;
    }

    public final TableResult tableResult;
}