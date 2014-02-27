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
    TimeSeriesVisualizer visualizer_ = null;
    Vector<String> genes = new Vector<String>();
    Vector<String> timeSeries = new Vector<String>();
    int totalTimeSeries;
    protected static NetworkElement itemToCompare_ = null;
    
    /** Logger for this class */
    private static Logger log_ = Logger.getLogger(IONetwork.class.getName());

    
    public TimeSeriesComparer(Frame aFrame, IElement item) {
        super(aFrame, item);
        visualizer_ = new TimeSeriesVisualizer(GnwGuiSettings.getInstance().getGnwGui().getFrame());
    

        //this.networkLabel = networkLabel;

        //visualizer_.displayGraph(networkLabel + "_" + options[optionList.getSelectedIndex()] + ".tsv");
        visualizer_.setHeaderInfo(getHeaderInfo());
        setHeaderInfo(getHeaderInfo());
        
        bOpen_.addActionListener(
                new ActionListener(){

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        open();
                        updateCombo(item_.getLabel() + "_" + fileList_.getSelectedItem().toString());
        
                    }
                    
                }
            );
        
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
        
        updateCombo(item_.getLabel() + "_" + fileList_.getSelectedItem().toString());
        
    }

    
    
    protected void updateCombo(String name) {
        visualizer_.clearDataLists();
        visualizer_.readFile(name + ".tsv");
        if (itemToCompare_ != null){
            visualizer_.readFile(itemToCompare_.getLabel() + "_" + fileList_.getSelectedItem().toString() + ".tsv");
        }
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
                                    itemToCompare_ = new NetworkElement(loadItem(filename, url, GeneNetwork.SBML));
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

    public static NetworkElement loadItem(String name, URL absPath, Integer format) throws 	FileNotFoundException, ParseException, Exception
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
