/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.epfl.lis.gnwgui;

/**
 *
 * @author LinnHtet24
 */

import ch.epfl.lis.gnw.GeneNetwork;
import ch.epfl.lis.gnwgui.idesktop.IElement;
import ch.epfl.lis.gnwgui.windows.TimeSeriesSelectionWindow;
import ch.epfl.lis.imod.ImodNetwork;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.Vector;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


public class TimeSeriesSelection extends TimeSeriesSelectionWindow {
    TimeSeriesVisualizer visualizer_ = new TimeSeriesVisualizer(GnwGuiSettings.getInstance().getGnwGui().getFrame());
    
    Vector<String> genes = new Vector<String>();
    Vector<String> timeSeries = new Vector<String>();
    int totalTimeSeries;
    
    
    public TimeSeriesSelection(Frame aFrame, IElement item) {
        super(aFrame, item);

        //this.networkLabel = networkLabel;

        //visualizer_.displayGraph(networkLabel + "_" + options[optionList.getSelectedIndex()] + ".tsv");
        visualizer_.setHeaderInfo(getHeaderInfo());
        setHeaderInfo(getHeaderInfo());
        
        fileList_.addActionListener(
                new ActionListener(){
                    public void actionPerformed(ActionEvent e) {
                        visualizer_.graphWindow.dispose();
                        JComboBox cb = (JComboBox)e.getSource();
                        String fileName = (String)cb.getSelectedItem();
                        updateCombo(item_.getLabel() + "_" + fileName);

                    }
                }
            );
        
        timeSeriesList_.setModel(new DefaultComboBoxModel(timeSeries));        
        geneList_ = new JList(genes);
        
        if(timeSeriesList_.getItemCount() > 0){
            timeSeriesList_.setSelectedIndex(0);        
        }
        if(geneList_.getComponentCount() > 0){
            geneList_.setSelectedIndex(0);
        }
        
        updateCombo(item_.getLabel() + "_" + fileList_.getSelectedItem().toString());
        
    }

    
    
    protected void updateCombo(String name) {
        visualizer_.readFile(name + ".tsv");
        totalTimeSeries = visualizer_.getTotalTimeSeries();
        timeSeries.clear();
        
        for (int i = 0; i < totalTimeSeries; i++){
            timeSeries.add((i + 1) + "");
        }
        
        timeSeriesList_.setModel(new DefaultComboBoxModel(timeSeries));
        timeSeriesList_.addActionListener(
                new ActionListener(){
                    public void actionPerformed(ActionEvent e){
                        visualizer_.graphWindow.dispose();
                        JComboBox combo = (JComboBox)e.getSource();
                        int tsIndex = combo.getSelectedIndex();
                        visualizer_.displayGraph(tsIndex, geneList_.getSelectedIndex());//0);
                        visualizer_.setHeaderInfo(item_.getLabel());
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

        geneList_.setModel(model);
        geneList_.addListSelectionListener(
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
                            
                            visualizer_.displayGraph(timeSeriesList_.getSelectedIndex(), selectedValues);
                            visualizer_.setHeaderInfo(item_.getLabel());
                            visualizer_.setVisible(true);
                        }
                        
                    }
                }    
            );
        
        
        System.out.println("before");
        visualizer_.displayGraph(0, new int[]{0});
        visualizer_.setHeaderInfo(item_.getLabel());
        visualizer_.setVisible(true);
        
        System.out.println("after");
        //geneList.setSelectedIndex(0);
        geneList_.clearSelection();
        //System.out.println("genelist: " + geneList.getSelectedIndex() + " is selected.");
        timeSeriesList_.setSelectedIndex(0);        
        System.out.println("timeSeriesList: " + timeSeriesList_.getSelectedIndex() + " is selected.");
        
        
    }
    
    private String getHeaderInfo(){
        String title1, title2;
        title1 = title2 = "";

        GeneNetwork geneNetwork = ((DynamicalModelElement)item_).getGeneNetwork();
        title1 = item_.getLabel();
        title2 = geneNetwork.getSize() + " genes, " + geneNetwork.getNumEdges() + " interactions";
        
        return title1 + " (" + title2 + ")";
    }

}
