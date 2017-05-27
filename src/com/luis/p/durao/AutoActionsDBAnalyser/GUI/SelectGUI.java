package com.luis.p.durao.AutoActionsDBAnalyser.GUI;

import com.luis.p.durao.AutoActionsDBAnalyser.DataJoiner;
import com.luis.p.durao.AutoActionsDBAnalyser.Main;
import com.sun.istack.internal.Nullable;

import javax.swing.SwingUtilities;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JDialog;
import javax.swing.JButton;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.BorderLayout;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SelectGUI {
    private int progress = 0;
    private final int maxProgress = 1000;    // ‰
    private JDialog processing;
    private final JProgressBar progressBar;

    public int getMaxProgress() {
        return maxProgress;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        if(progress>maxProgress || progress < 0)
            throw new IndexOutOfBoundsException();
        this.progress=progress;
        SwingUtilities.invokeLater(() -> progressBar.setValue(progress));
    }

    public SelectGUI() {
        final String selectButtonText = "Select";
        final String deleteButtonText = "Remove";
        final Object[] newRow = {null,selectButtonText,deleteButtonText};

        progressBar = new JProgressBar(0,maxProgress);
        progressBar.addChangeListener(e -> {
            if(progressBar.getValue() >= progressBar.getMaximum())
                if(processing!=null)
                    processing.dispose();
        });

        String [] columnNames = {"File","",""};
        Object[][] data = {newRow};
        table1.setModel(new DefaultTableModel(data,columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column==1 || column==2;
            }
        });

        table1.getModel().addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                computeButtonsEnabled();
            }
        });

        int selectWidth = selectButtonText.length()*12;
        int deleteWidth = deleteButtonText.length()*15;
        table1.getColumnModel().getColumn(1).setResizable(false);
        table1.getColumnModel().getColumn(1).setPreferredWidth(selectWidth);
        table1.getColumnModel().getColumn(1).setMaxWidth(selectWidth);
        table1.getColumnModel().getColumn(1).setMinWidth(selectWidth);
        table1.getColumnModel().getColumn(2).setResizable(false);
        table1.getColumnModel().getColumn(2).setPreferredWidth(deleteWidth);
        table1.getColumnModel().getColumn(2).setMaxWidth(deleteWidth);
        table1.getColumnModel().getColumn(2).setMinWidth(deleteWidth);
        JFileChooser fileChooser = new JFileChooser("./");
        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.setDragEnabled(false);
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.addChoosableFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.exists() && !f.isDirectory() && f.canRead() && Pattern.compile("^.*[.](db|csv)$").matcher(f.getName()).matches();
            }

            @Override
            public String getDescription() {
                return "All accepted files (.db, .csv)";
            }
        });
        fileChooser.addChoosableFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.exists() && !f.isDirectory() && f.canRead() && Pattern.compile("^.*[.](db)$").matcher(f.getName()).matches();
            }

            @Override
            public String getDescription() {
                return "*.db";
            }
        });
        fileChooser.addChoosableFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.exists() && !f.isDirectory() && f.canRead() && Pattern.compile("^.*[.](csv)$").matcher(f.getName()).matches();
            }

            @Override
            public String getDescription() {
                return "*.csv";
            }
        });

        Action select = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JTable table = (JTable)e.getSource();
                DefaultTableModel model = (DefaultTableModel)table.getModel();
                final int row = Integer.valueOf(e.getActionCommand());

                if(fileChooser.showOpenDialog(mainPanel) == JFileChooser.APPROVE_OPTION) {
                    if(fileChooser.getSelectedFiles().length <= 0)
                        return;

                    final HashSet<File> IOex = new HashSet<>();
                    final HashSet<File> duplicates = new HashSet<>();

                    List<File> selectedFiles = Arrays.stream(fileChooser.getSelectedFiles())
                            .map(o -> getCanonicalOrNull(o,IOex)).filter(Objects::nonNull)    //remove IOexceptions
                            .distinct() //remove duplicates in this list
                            .map(o -> fileAlreadySelected(o,duplicates)).filter(Objects::nonNull) //remove duplicates comparing to te ones already in the table
                            .collect(Collectors.toList());

                    if(!selectedFiles.isEmpty()) {
                        model.setValueAt(selectedFiles.remove(0),row,0);
                    }

                    for (File f : selectedFiles) {
                        if (model.getRowCount() <= 0 || model.getValueAt(model.getRowCount() - 1, 0) != null)
                            model.addRow(newRow);
                        model.setValueAt(f, model.getRowCount() - 1, 0);
                    }

                    if(!IOex.isEmpty()) {
                        StringBuilder sb = new StringBuilder("Error opening file(s):");
                        for(File f : IOex)
                            sb.append(System.lineSeparator()).append(f);
                        JOptionPane.showMessageDialog(getMainPanel(),sb.toString(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    if(!duplicates.isEmpty()) {
                        StringBuilder sb = new StringBuilder("Ignored already selected file(s):");
                        for(File f : duplicates)
                            sb.append(System.lineSeparator()).append(f);
                        JOptionPane.showMessageDialog(getMainPanel(),sb.toString(),"Found duplicate file(s)",JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }
        };

        Action delete = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JTable table = (JTable)e.getSource();
                DefaultTableModel model = ((DefaultTableModel)table.getModel());
                if(model.getRowCount() <= 1 && model.getValueAt(0,0) == null)
                    return;
                int row = Integer.valueOf(e.getActionCommand());
                model.removeRow(row);
                if(model.getRowCount()==0)
                    model.addRow(newRow);
            }
        };

        new ButtonColumn(table1,select,1);
        new ButtonColumn(table1,delete,2);
        addButton.addActionListener(e -> {
            DefaultTableModel model = ((DefaultTableModel)table1.getModel());
            if(model.getValueAt(model.getRowCount()-1,0) == null)
                return;
            model.addRow(newRow);
        });

        computeButtonsEnabled();
        analyseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                processing = new JDialog(Main.frame,"Analysing data...", true);

                processing.add(BorderLayout.NORTH, new JLabel("Processing..."));
                processing.add(BorderLayout.CENTER, progressBar);
                processing.setSize(400,75);
                processing.setLocationRelativeTo(Main.frame);
                processing.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

                setProgress(0);

//                Thread t = new Thread(() -> {
//                    //Process.init();
//                    try {
//                        for(;progress < progressBar.getMaximum() && progress >= progressBar.getMinimum();setProgress(++progress)) {
//                            Thread.sleep(2);
//                        }
//                    } catch (InterruptedException e1) {
//                        processing.dispose();
//                        setProgress(getMaxProgress());
//                    }
//                });
//                t.start();
//                processing.setVisible(true);    //blocking call
//                t.interrupt();
//                if(progress < progressBar.getMaximum())
//                    return;
                Main.waiting = false;
                ArrayList<File> files = new ArrayList<>();
                for(int i = 0 ; i<table1.getModel().getRowCount() ; i++)
                    if(table1.getModel().getValueAt(i,0) != null)
                        files.add((File)table1.getModel().getValueAt(i,0));
                if(files.isEmpty())
                    return;
                try {
                    DataJoiner.createDBFromSelectedSources(files);
                } catch (IOException e1) {
                    e1.printStackTrace();
                    return;
                }
                Main.changeToAnalyse();
            }
        });
    }

    private boolean fileAlreadySelected(File f) {
        DefaultTableModel model = ((DefaultTableModel)table1.getModel());
        for(int i = 0 ; i<model.getRowCount() ; i++)
            if(f.equals(model.getValueAt(i,0)))
                return true;
        return false;
    };

    private File fileAlreadySelected(File f, HashSet<File> store) {
        if(!fileAlreadySelected(f))
            return f;
        store.add(f);
        return null;
    }

    @Nullable
    private static File getCanonicalOrNull(File f, HashSet<File> store) {
        try {
            return f.getCanonicalFile();
        } catch (IOException e) {
            store.add(f);
            return null;
        }
    }

    private void computeButtonsEnabled() {
        final DefaultTableModel model = (DefaultTableModel)table1.getModel();
        final int n = model.getRowCount();
        try {
            analyseButton.setEnabled(n > 1 || model.getValueAt(0,0) != null);
        } catch (IndexOutOfBoundsException ignore) {
            analyseButton.setEnabled(false);
        }

        addButton.setEnabled(n <= 0 || model.getValueAt(n-1,0)!=null);
    }

    private JPanel mainPanel;
    private JTable table1;
    private JButton analyseButton;
    private JButton addButton;

    public JPanel getMainPanel() {
        return mainPanel;
    }
}
