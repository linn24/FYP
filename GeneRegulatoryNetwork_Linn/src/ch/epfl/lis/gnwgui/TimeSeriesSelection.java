/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.epfl.lis.gnwgui;

/**
 *
 * @author LinnHtet24
 */

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.Vector;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


public class TimeSeriesSelection extends JPanel implements ActionListener {
    TimeSeriesVisualizer visualizer_ = new TimeSeriesVisualizer(GnwGuiSettings.getInstance().getGnwGui().getFrame());
    JLabel fileName, timeSeriesName, geneName;
    JComboBox fileList, timeSeriesList;
    JList geneList;
    JScrollPane scrollPane;
    Vector<String> genes = new Vector<String>();
    Vector<String> timeSeries = new Vector<String>();
    int totalTimeSeries;
    String networkLabel;

    
    
    public TimeSeriesSelection(String networkLabel) {
        super(new GridLayout(0,2));

        this.networkLabel = networkLabel;

        //visualizer_.displayGraph(networkLabel + "_" + options[optionList.getSelectedIndex()] + ".tsv");
        visualizer_.setHeaderInfo(networkLabel);
        
        Vector<String> vFiles = new Vector<String>();
        vFiles = this.populateFileOptions();
        

        fileList = new JComboBox(vFiles);
        fileList.setSelectedIndex(0);
        fileList.addActionListener(this);
                
        timeSeriesList = new JComboBox(timeSeries);        
        geneList = new JList(genes);
        scrollPane = new JScrollPane(geneList);
        
        fileName = new JLabel();
        timeSeriesName = new JLabel();
        geneName = new JLabel();
                
        fileName.setFont(fileName.getFont().deriveFont(Font.ITALIC));
        fileName.setHorizontalAlignment(JLabel.CENTER);
        
        if(timeSeriesList.getItemCount() > 0){
            timeSeriesList.setSelectedIndex(0);        
        }
        if(geneList.getComponentCount() > 0){
            geneList.setSelectedIndex(0);
        }
        
        updateCombo(networkLabel + "_" + vFiles.get(fileList.getSelectedIndex()));
        
        fileName.setBorder(BorderFactory.createEmptyBorder(10,0,0,0));
        fileName.setText("Select File");
        
        timeSeriesName.setFont(fileName.getFont().deriveFont(Font.ITALIC));
        timeSeriesName.setHorizontalAlignment(JLabel.CENTER);
        timeSeriesName.setBorder(BorderFactory.createEmptyBorder(10,0,0,0));
        timeSeriesName.setText("Select Time Series");
        
        geneName.setFont(fileName.getFont().deriveFont(Font.ITALIC));
        geneName.setHorizontalAlignment(JLabel.CENTER);
        geneName.setBorder(BorderFactory.createEmptyBorder(10,0,0,0));
        geneName.setText("Select Gene");

        add(fileName);
        add(fileList);
        
        add(timeSeriesName);
        add(timeSeriesList);
        
        add(geneName);        
        add(scrollPane);        
        
        setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
    }

    /** Listens to the combo box. */
    
    public void actionPerformed(ActionEvent e) {
        visualizer_.graphWindow.dispose();
        JComboBox cb = (JComboBox)e.getSource();
        String fileName = (String)cb.getSelectedItem();
        updateCombo(networkLabel + "_" + fileName);
        
    }
    
    
    protected void updateCombo(String name) {
        visualizer_.readFile(name + ".tsv");
        totalTimeSeries = visualizer_.getTotalTimeSeries();
        timeSeries.clear();
        
        for (int i = 0; i < totalTimeSeries; i++){
            timeSeries.add((i + 1) + "");
        }
        
        timeSeriesList.setModel(new DefaultComboBoxModel(timeSeries));
        timeSeriesList.addActionListener(
                new ActionListener(){
                    public void actionPerformed(ActionEvent e){
                        visualizer_.graphWindow.dispose();
                        JComboBox combo = (JComboBox)e.getSource();
                        int tsIndex = combo.getSelectedIndex();
                        visualizer_.displayGraph(tsIndex, geneList.getSelectedIndex());//0);
                        visualizer_.setHeaderInfo(networkLabel);
                        visualizer_.setVisible(true);
                    }
                }    
            );
        genes.clear();
        genes.add("All genes");
        visualizer_.getGenes(genes); 
        System.out.println("total genes: " + genes.size());
        
                
        //update jlist
        DefaultListModel model = new DefaultListModel();
        for (int i = 0; i < genes.size(); i++){
            model.addElement(genes.get(i));
        }

        geneList.setModel(model);
        geneList.addListSelectionListener(
                new ListSelectionListener(){
                    @Override
                    public void valueChanged(ListSelectionEvent e) {
                        visualizer_.graphWindow.dispose();                            
                        if(!e.getValueIsAdjusting()){
                            JList list = (JList) e.getSource();
                            
                            int[] selectedValues;
                            selectedValues = list.getSelectedIndices();
                            for(int i = 0; i < selectedValues.length; i++){
                                System.out.println( selectedValues[i] + " is selected.");
                            }
                            
                            visualizer_.displayGraph(timeSeriesList.getSelectedIndex(), selectedValues);
                            visualizer_.setHeaderInfo(networkLabel);
                            visualizer_.setVisible(true);
                        }
                        
                    }
                }    
            );
        
        
        System.out.println("before");
        visualizer_.displayGraph(0, new int[]{0});
        visualizer_.setHeaderInfo(networkLabel);
        visualizer_.setVisible(true);
        
        System.out.println("after");
        //geneList.setSelectedIndex(0);
        geneList.clearSelection();
        //System.out.println("genelist: " + geneList.getSelectedIndex() + " is selected.");
        timeSeriesList.setSelectedIndex(0);        
        System.out.println("timeSeriesList: " + timeSeriesList.getSelectedIndex() + " is selected.");
        
        
    }

    private Vector<String> populateFileOptions(){
        Vector<String> vFiles = new Vector<String>();
        String[] files = { "dream4_timeseries", "noexpnoise_dream4_timeseries"
                , "noexpnoise_proteins_dream4_timeseries", "nonoise_dream4_timeseries"
                , "nonoise_proteins_dream4_timeseries", "proteins_dream4_timeseries"};
        
        File file;
        
        for (int i = 0; i < files.length; i++){
            file = new File(this.networkLabel + "_" + files[i] + ".tsv");
            if(file.exists()){
                vFiles.add(files[i]);
            }        
        }
        return vFiles;
    }

    
}
