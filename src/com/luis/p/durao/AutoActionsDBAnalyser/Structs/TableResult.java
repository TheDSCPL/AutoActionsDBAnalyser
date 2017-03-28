package com.luis.p.durao.AutoActionsDBAnalyser.Structs;

import com.sun.istack.internal.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TableResult {
    public TableResult(String tableName, List<Class<?>> types) {
        this.tableName = tableName;
        this.types = Collections.unmodifiableList(types);
        this.tuples = new ArrayList<>();
        setColumnNames();
    }

    public TableResult(String tableName, Class<?>... types) {
        this(tableName, Arrays.asList(types));
    }

    public TableResult(String tableName, String... types) {
        List<Class<?>> temp = new ArrayList<>();
        for(String s : types) {
            try {
                temp.add(Class.forName(s));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                System.exit(6);
            }
        }
        this.types = Collections.unmodifiableList(temp);
        this.tuples = new ArrayList<>();
        this.tableName = tableName;
    }

    public final String STRING = "java.lang.String";
    public final String INTEGER = "java.lang.Integer";

    public final String tableName;

    public final List<Class<?>> types;

    public List<String> columnNames = null;

    public final List<Tuple> tuples;

    public final class Tuple {
        public final List<Object> values;

        private Tuple (@NotNull List<Object> values) {
            if(values.size() != numColumns()) {
                System.err.println("Number of values in a tuple must be the number of columns!");
                System.exit(7);
            }
            if(!checkTypes(values)) {
                System.err.println("At least one of the types passed to the tuple can't be converted to the respective column type.");
                System.exit(12);
            }
            this.values = Collections.unmodifiableList(values);
        }
        private Tuple (Object... values) {
            this(Arrays.asList(values));
        }

        private boolean checkTypes(@NotNull List<Object> values) {
            for(int i = 0 ; i<numColumns() ; i++) {
                if(!types.get(i).isInstance(values.get(i)) && values.get(i) != null)
                {
                    System.err.println(values.get(i).getClass().getSimpleName() + " can't be converted to " + types.get(i).getSimpleName());
                    return false;
                }
            }
            return true;
        }

        public void print() {
            print(System.out);
        }

        public void print(java.io.PrintStream stream) {
            stream.print(this);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for(int i = 0; i<numColumns() ; i++) {
                sb.append(columnNames.get(i) + "= " + values.get(i) + " ");
            }
            sb.append(System.lineSeparator());
            return sb.toString();
        }
    }

    private void setColumnNames() {
        List<String> temp = new ArrayList<>();
        for(int i = 0; i<types.size() ; i++)
            temp.add("Column " + i);
        setColumnNames(temp);
    }

    public void setColumnNames(List<String> columnNames) {
        if(columnNames.size() != types.size()) {
            System.err.println("Size of column names must be the same as types!");
            System.exit(8);
        }
        this.columnNames = Collections.unmodifiableList(columnNames);
    }

    public void setColumnNames(String... columnNames) {
        setColumnNames(Arrays.asList(columnNames));
    }

    public int numTuples() {
        return tuples.size();
    }

    public int numColumns() {
        return types.size();
    }

    public void addTuple(Object... values) {
        tuples.add(new Tuple(values));
    }

    public void print() {
        print(System.out);
    }

    public void print(java.io.PrintStream stream) {
        /*stream.println("Table \"" + tableName + "\"'s contents (#tuples: " + numTuples() +"):");
        tuples.forEach(o -> o.print(stream));*/
        stream.print(toString());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Table \"" + tableName + "\"'s contents (#tuples: " + numTuples() +"):")
          .append(System.lineSeparator());
        tuples.forEach(o -> sb.append(o));
        return sb.toString();
    }

    public void importResultSet(ResultSet rs) {
        try {
            while ( rs.next() ) {
                List<Object> temp = new ArrayList<>(numColumns());
                columnNames.stream().forEachOrdered(o -> {
                    try {
                        temp.add(rs.getObject(o));
                    } catch (SQLException e) {
                        e.printStackTrace();
                        System.exit(10);
                    }
                });
                tuples.add(new Tuple(temp));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(9);
        }
    }
}
