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
import ch.epfl.lis.gnw.GnwSettings;
import ch.epfl.lis.gnwgui.idesktop.IElement;
import ch.epfl.lis.gnwgui.windows.TimeSeriesComparerWindow;
import ch.epfl.lis.gnwgui.windows.Wait;
import ch.epfl.lis.imod.ImodNetwork;
import ch.epfl.lis.networks.ios.ParseException;
import ch.epfl.lis.utilities.filefilters.FilenameUtilities;
import ch.epfl.lis.utilities.filefilters.FilterGnwDesktopZIP;
import ch.epfl.lis.utilities.filefilters.FilterNetworkAll;
import ch.epfl.lis.utilities.filefilters.FilterNetworkDOT;
import ch.epfl.lis.utilities.filefilters.FilterNetworkGML;
import ch.epfl.lis.utilities.filefilters.FilterNetworkSBML;
import ch.epfl.lis.utilities.filefilters.FilterNetworkTSVDREAMTSV;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;


public class TimeSeriesComparer extends TimeSeriesComparerWindow {
    TimeSeriesVisualizerComparison visualizer_ = null;
    TimeSeriesVisualizerComparison secondVisualizer_ = null;
    Vector<String> genes = new Vector<String>();
    Vector<String> timeSeries = new Vector<String>();
    int totalTimeSeries;
    protected static IElement itemToCompare_ = null;
    
    String[] arrGenes = null;
    
    /** Logger for this class */
    private static Logger log_ = Logger.getLogger(IONetwork.class.getName());

    
    public TimeSeriesComparer(Frame aFrame, IElement item) {
        super(aFrame, item);
        visualizer_ = new TimeSeriesVisualizerComparison(GnwGuiSettings.getInstance().getGnwGui().getFrame());
        secondVisualizer_ = new TimeSeriesVisualizerComparison(GnwGuiSettings.getInstance().getGnwGui().getFrame());
                            
        itemToCompare_ = null;
        //this.networkLabel = networkLabel;

        //visualizer_.displayGraph(networkLabel + "_" + options[optionList.getSelectedIndex()] + ".tsv");
        visualizer_.setHeaderInfo(getHeaderInfo(item_));
        setHeaderInfo(getHeaderInfo(item_));
        
        // disable list selection until another network has been selected
        geneList_.setEnabled(false);
        fileList_.setEnabled(false);
        timeSeriesList_.setEnabled(false);
        oneWin_.setEnabled(false);
        twoWin_.setEnabled(false);
        
        oneWin_.addActionListener(
                new ActionListener(){
                    @Override
                    public void actionPerformed(ActionEvent e){
                        if(oneWin_.isSelected()){
                            if(secondVisualizer_.graphWindow != null){
                                secondVisualizer_.graphWindow.dispose();      
                            }
                            visualizer_.setGraphOption(0);
                            updateGraph();
                        }
                    }
                }
                );
        
         twoWin_.addActionListener(
                new ActionListener(){
                    @Override
                    public void actionPerformed(ActionEvent e){
                        if(twoWin_.isSelected()){
                            visualizer_.setGraphOption(1);
                            updateGraph();
                            
                            secondVisualizer_.copyVisualizer(visualizer_);
                            secondVisualizer_.setGraphOption(2);
                            secondVisualizer_.displayGraph(timeSeriesList_.getSelectedIndex(), geneList_.getSelectedValues());
                            secondVisualizer_.setHeaderInfo(getHeaderInfo(itemToCompare_));
                            secondVisualizer_.setVisible(true);                            
                        }
                    }
                }
                );
        
        bOpen_.addActionListener(
                new ActionListener(){

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        open();     
                        if (itemToCompare_ != null){
                            // enable list selection when another network has been selected
                            geneList_.setEnabled(true);
                            fileList_.setEnabled(true);
                            timeSeriesList_.setEnabled(true);
                            oneWin_.setEnabled(true);
                            twoWin_.setEnabled(true);
                            
                            selected_.setText(itemToCompare_.getLabel());
                            System.out.println("compare");
                            visualizer_.clearDataLists();
                            visualizer_.readFile(itemToCompare_.getLabel() + "_" + fileList_.getSelectedItem().toString() + ".tsv"
                                    , visualizer_.lstCompareData, visualizer_.lstFixedCompareData, visualizer_.lstCompareTimeStamp, 2);
                            visualizer_.setCompareGenesHash(visualizer_.hashGeneList(visualizer_.compareGenes));  
                            visualizer_.getCommonGenes();
                            updateCombo();
                            //updateGraph();
                            checkWindow();
                        }                       
        
                    }
                    
                }
            );
        
        fileList_.addActionListener(
                new ActionListener(){
                    public void actionPerformed(ActionEvent e) {
                        visualizer_.graphWindow.dispose();
                        JComboBox cb = (JComboBox)e.getSource();
                        String fileName = (String)cb.getSelectedItem();
                        visualizer_.clearDataLists();        
                        if(itemToCompare_ != null){
                            visualizer_.readFile(itemToCompare_.getLabel() + "_" + fileList_.getSelectedItem().toString() + ".tsv"
                                    , visualizer_.lstCompareData, visualizer_.lstFixedCompareData, visualizer_.lstCompareTimeStamp, 2);
                        visualizer_.setCompareGenesHash(visualizer_.hashGeneList(visualizer_.compareGenes));            
                        }
                        visualizer_.readFile(item_.getLabel() + "_" + fileName + ".tsv"
                                , visualizer_.lstCurrentData, visualizer_.lstFixedCurrentData, visualizer_.lstCurrentTimeStamp, 1);
                        visualizer_.setCurrentGenesHash(visualizer_.hashGeneList(visualizer_.currentGenes));
                        visualizer_.getCommonGenes();
                        updateCombo();
                        //updateGraph();
                        checkWindow();
                    }
                }
            );
        
        timeSeriesList_.setModel(new DefaultComboBoxModel(timeSeries));    
        
        DefaultListModel model = new DefaultListModel();
        for (int i = 0; i < genes.size(); i++){
            model.addElement(genes.get(i));
        }
        geneList_.setModel(model);
        
        if(timeSeriesList_.getItemCount() > 0){
            timeSeriesList_.setSelectedIndex(0);        
        }
        if(geneList_.getComponentCount() > 0){
            geneList_.setSelectedIndex(0);
        }
        
        visualizer_.clearDataLists();        
        visualizer_.readFile(item_.getLabel() + "_" + fileList_.getSelectedItem().toString() + ".tsv"
                , visualizer_.lstCurrentData, visualizer_.lstFixedCurrentData, visualizer_.lstCurrentTimeStamp, 1);
        System.out.println("lstFixedCurrentData.size(): " + visualizer_.lstFixedCurrentData.size());
        System.out.println("currentGenes.length: " + visualizer_.currentGenes.length);
        visualizer_.setCurrentGenesHash(visualizer_.hashGeneList(visualizer_.currentGenes));
        updateCombo();
        
    }

    protected void checkWindow(){
        if(twoWin_.isSelected()){
            visualizer_.setGraphOption(1);
            updateGraph();

            secondVisualizer_.copyVisualizer(visualizer_);
            secondVisualizer_.setGraphOption(2);
            secondVisualizer_.displayGraph(timeSeriesList_.getSelectedIndex(), geneList_.getSelectedValues());
            secondVisualizer_.setHeaderInfo(getHeaderInfo(itemToCompare_));
            secondVisualizer_.setVisible(true);

            System.out.println("visaualizer_.graphOption: " + visualizer_.getGraphOption());
            System.out.println("secondVisaualizer_.graphOption: " + secondVisualizer_.getGraphOption());
        }else if(oneWin_.isSelected()){
            if(secondVisualizer_.graphWindow != null){
                secondVisualizer_.graphWindow.dispose();      
            }
            visualizer_.setGraphOption(0);
            updateGraph();
        }
    }
    
    protected void updateCombo() {
        
        totalTimeSeries = visualizer_.getCommonTimeSeries();//getTotalTimeSeries();
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
                        
                        if(twoWin_.isSelected()){
                            visualizer_.setGraphOption(1);
                            visualizer_.displayGraph(tsIndex, geneList_.getSelectedValues());//getSelectedIndex());//0);
                            visualizer_.setHeaderInfo(getHeaderInfo(item_));
                            visualizer_.setVisible(true);

                            secondVisualizer_.copyVisualizer(visualizer_);
                            secondVisualizer_.setGraphOption(2);
                            secondVisualizer_.displayGraph(timeSeriesList_.getSelectedIndex(), geneList_.getSelectedValues());
                            secondVisualizer_.setHeaderInfo(getHeaderInfo(itemToCompare_));
                            secondVisualizer_.setVisible(true);

                            System.out.println("visaualizer_.graphOption: " + visualizer_.getGraphOption());
                            System.out.println("secondVisaualizer_.graphOption: " + secondVisualizer_.getGraphOption());
                        }else if(oneWin_.isSelected()){
                            if(secondVisualizer_.graphWindow != null){
                                secondVisualizer_.graphWindow.dispose();      
                            }
                            visualizer_.setGraphOption(0);
                            visualizer_.displayGraph(tsIndex, geneList_.getSelectedValues());//getSelectedIndex());//0);
                            visualizer_.setHeaderInfo(getHeaderInfo(item_));
                            visualizer_.setVisible(true);
                        }
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
                            
                            Object[] selectedValues;
                            selectedValues = list.getSelectedValues();//list.getSelectedIndices();
                            for(int i = 0; i < selectedValues.length; i++){
                                System.out.println( selectedValues[i] + " is selected.");
                            }
                            
                            if(twoWin_.isSelected()){
                                visualizer_.setGraphOption(1);
                                visualizer_.displayGraph(timeSeriesList_.getSelectedIndex(), selectedValues);//getSelectedIndex());//0);
                                visualizer_.setHeaderInfo(getHeaderInfo(item_));
                                visualizer_.setVisible(true);

                                secondVisualizer_.copyVisualizer(visualizer_);
                                secondVisualizer_.setGraphOption(2);
                                secondVisualizer_.displayGraph(timeSeriesList_.getSelectedIndex(), selectedValues);
                                secondVisualizer_.setHeaderInfo(getHeaderInfo(itemToCompare_));
                                secondVisualizer_.setVisible(true);

                                System.out.println("visaualizer_.graphOption: " + visualizer_.getGraphOption());
                                System.out.println("secondVisaualizer_.graphOption: " + secondVisualizer_.getGraphOption());
                            }else if(oneWin_.isSelected()){
                                if(secondVisualizer_.graphWindow != null){
                                    secondVisualizer_.graphWindow.dispose();      
                                }
                                visualizer_.setGraphOption(0);
                                visualizer_.displayGraph(timeSeriesList_.getSelectedIndex(), selectedValues);//getSelectedIndex());//0);
                                visualizer_.setHeaderInfo(getHeaderInfo(item_));
                                visualizer_.setVisible(true);
                            }
                        }
                        
                    }
                }    
            );
    }
    
    public void updateGraph(){
        
        System.out.println("before");
        visualizer_.displayGraph(0, new String[]{"All genes"});
        visualizer_.setHeaderInfo(getHeaderInfo(item_));
        visualizer_.setVisible(true);
        
        System.out.println("after");
        //geneList.setSelectedIndex(0);
        geneList_.clearSelection();
        //System.out.println("genelist: " + geneList.getSelectedIndex() + " is selected.");
        timeSeriesList_.setSelectedIndex(0);        
        System.out.println("timeSeriesList: " + timeSeriesList_.getSelectedIndex() + " is selected.");
        
        
    }
    
    private String getHeaderInfo(IElement item){
        String title1, title2;
        title1 = title2 = "";
        
        GeneNetwork geneNetwork = ((DynamicalModelElement)item).getGeneNetwork();
        title1 = item.getLabel();
        title2 = geneNetwork.getSize() + " genes, " + geneNetwork.getNumEdges() + " interactions";
        
        return title1 + " (" + title2 + ")";
    }

    
    
    // functions to select different versions
    public static void open()
    {
        IODialog dialog = new IODialog(GnwGuiSettings.getInstance().getGnwGui().getFrame(), "Open Network",
                        GnwSettings.getInstance().getOutputDirectory(), IODialog.LOAD);

        //dialog.addFilter(new FilterNetworkTSVDREAMTSV());
        //		dialog.addFilter(new FilterNetworkTSVDREAM());
        //dialog.addFilter(new FilterNetworkGML());
        //dialog.addFilter(new FilterNetworkDOT());
        dialog.addFilter(new FilterNetworkSBML());
        //dialog.addFilter(new FilterGnwDesktopZIP());
        //dialog.addFilter(new FilterNetworkAll());

        dialog.setAcceptAllFileFilterUsed(false);
        dialog.display();

        Wait wait = new Wait(GnwGuiSettings.getInstance().getGnwGui().getFrame(), true);
        wait.setTitle("Open Network");
        TimeSeriesComparer.NetworkImport ni = new TimeSeriesComparer.NetworkImport(wait);

        ni.fileAbsPath_ = dialog.getSelection();
        ni.f_ = dialog.getSelectedFilter();

        ni.execute();
        wait.start();
    }
    
    public static void open(String absPath, FileFilter f)
    {
            try
            {
                    if (absPath != null)
                    {
                            String dir = FilenameUtilities.getDirectory(absPath);
                            String filename = FilenameUtilities.getFilenameWithoutPath(absPath);
                            URL url = GnwSettings.getInstance().getURL(absPath);

                            if (f == null || f instanceof FilterNetworkSBML){
                                    itemToCompare_ = new DynamicalModelElement(loadItem(filename, url, GeneNetwork.SBML));
                            }else{
                                    throw new Exception("Selected format unhandled!");
                            }
                            // Save the current path as user path
                            GnwSettings.getInstance().setOutputDirectory(dir);
                                    
                    }
            }
            catch (FileNotFoundException e)
            {
                    IONetwork.openingFailedDialog("GNW Message", absPath, "File not found!");
                    log_.log(Level.WARNING, "TimeSeriesComparer::open(): " + e.getMessage(), e);
            }
            catch (ParseException e)
            {
                    IONetwork.openingFailedDialog("GNW Message", absPath, "Error occurs during parsing.<br>" +
                    "See logs for more information.");
                    log_.log(Level.WARNING, "TimeSeriesComparer::open(): " + e.getMessage(), e);
            }
            catch (Exception e)
            {
                    IONetwork.openingFailedDialog("GNW Message", absPath, "Unhandled exception!<br>" +
                    "See logs for more information.");
                    log_.log(Level.WARNING, "TimeSeriesComparer::open(): " + e.getMessage(), e);
            }
            
    }

    public static DynamicalModelElement loadItem(String name, URL absPath, Integer format) throws 	FileNotFoundException, ParseException, Exception
    {
            if (name.equals("") || name.charAt(0) == '#')
                    name = FilenameUtilities.getFilenameWithoutPath(absPath.getPath());
            name = FilenameUtilities.getFilenameWithoutExtension(name);

            if ( IONetwork.isDynamicalNetworkFormat(format = IONetwork.isDynamicalNetworkExtension(absPath, format)) )
            {

                    DynamicalModelElement grn = IONetwork.loadDynamicNetworkItem(name, absPath, format);

                    IONetwork.printOpeningInfo(grn);

                    return grn;
            }
            else
                    throw new Exception("Unkown network format!");
    }



    // PRIVATE CLASSES

    private static class NetworkImport extends SwingWorker<Void, Void>
    {
        /** Dialog displayed during the process */
        private Wait wDialog_;

        /** Absolute path to the network file */
        private String fileAbsPath_;

        /** FileFilter selected (information about the type of network to load) */
        private FileFilter f_;

        // ----------------------------------------------------------------------------
        // PUBLIC METHODS

        public NetworkImport(Wait gui)
        {
                this.wDialog_ = gui;
        }

        // ----------------------------------------------------------------------------
        // PROTECTED METHODS

        @Override
        protected Void doInBackground() throws Exception
        {

                open(fileAbsPath_, f_);

                return null;
        }

        // ----------------------------------------------------------------------------

        @Override
        protected void done()
        {
                wDialog_.stop();
        }
    }

}
