package com.luis.p.durao.AutoActionsDBAnalyser;
import com.luis.p.durao.AutoActionsDBAnalyser.DBs.DBConnection;
import static com.luis.p.durao.AutoActionsDBAnalyser.DBs.DBConnection.*;
import com.luis.p.durao.AutoActionsDBAnalyser.GUI.AnalyseGUI;
import com.luis.p.durao.AutoActionsDBAnalyser.GUI.SelectGUI;

import javax.swing.*;
import java.lang.*;

//adb pull /sdcard/Android/data/future.cities.sensemyfeup/files/* ./

public class Main {
    //static DBConnection db;

    final public static JFrame frame;
    final private static SelectGUI selectGUI;

    static {
        frame = new JFrame(Main.class.getPackage().getName().replaceFirst("^(?:.*[.])(\\w*)$","$1"));

        selectGUI = new SelectGUI();

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public static void changeToAnalyse() {
        frame.setContentPane(new AnalyseGUI().getMainPanel());
        frame.pack();
        SwingUtilities.invokeLater(() -> frame.setVisible(true));
    }

    public static void changeToSelect() {
        frame.setContentPane(selectGUI.getMainPanel());
        frame.pack();
        SwingUtilities.invokeLater(() -> frame.setVisible(true));
    }

    public volatile static boolean waiting = true;

    public static void main(String[] args) {

        //changeToAnalyse();
        changeToSelect();

        /*automatic = new DBConnection("auto.db");
        //manual = new DBConnection("auto.db");
        manual = new DBConnection("manual.db");

        changeToSelect();

        try {
            while(waiting)
                Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //automatic.getSession().tableResult.print();
        //System.out.println(automatic.getSession().getSessionIntervals());

        System.out.println(manual.getSession().getStartEpochTimePerSession());

        System.out.println("Paragens indevidas: " + Process.getFalseStopsOfAutoActionsPerManualSession());

        System.out.println("Distância perdida: " + Process.getTotalLostDistancePerManualSession());

        System.out.println("Distância perdida em diâmetro: " + Process.getTotalLostDiameterDistancePerManualSession());

        System.out.println("Tempo perdido: " + Process.getLostTimePerManualSession());

        System.out.println("Duração total do teste: " + Process.getDurationOfTestPerManualSession());

        System.out.println("Tempo para parar: " + Process.getStopTimePerManualSession());*/

        /*System.out.println(automatic.getGslocation().getFirstLocationTimeOfEachSession());

        System.out.println(Process.getTotalLostDistancePerManualSession());

        System.out.println(manual.getGslocation().getTotalDistanceFromTo(5, -1, -1));

        System.out.println(manual.getGslocation().getDiameterDistanceFromTo(5, -1, -1));

        System.out.println(Process.getLostTimePerManualSession());

        System.out.println(Process.getDurationOfTestPerManualSession());

        System.out.println(Process.getStopTimePerManualSession());*/

        /*//
        long aft, b4 = System.nanoTime();
        System.out.println(Process.getAutoSessionsInEachManualSession());
        aft = System.nanoTime();
        System.out.printf(String.valueOf(aft-b4));

        b4 = System.nanoTime();
        System.out.println(Process.getAutoSessionsInEachManualSession());
        aft = System.nanoTime();
        System.out.printf(String.valueOf(aft-b4));*/
    }
}
