/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.epfl.lis.gnwgui;

/**
 *
 * @author LinnHtet24
 */

import ch.epfl.lis.gnwgui.windows.TimeSeriesVisualizerWindow;
import java.awt.*;
import java.awt.font.*;
import java.awt.geom.*;
import javax.swing.*;
import java.io.*;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;


public class TimeSeriesVisualizerComparison extends TimeSeriesVisualizerWindow {
    final int PAD = 80;
    Vector<double[]> lstData = new Vector<double[]>();
    Vector<double[]> lstCurrentData = new Vector<double[]>();
    Vector<double[]> lstCompareData = new Vector<double[]>();
    
    String[] genes, currentGenes, compareGenes;
    
    TimeSeriesVisualizerComparison graphWindow;
    MyTimeSeriesGraph graph, currentGraph, compareGraph;
        
    Vector<double[]> lstFixedData = new Vector<double[]>();
    Vector<String> lstTimeStamp = new Vector<String>(); 
    Vector<double[]> lstFixedCurrentData = new Vector<double[]>();
    Vector<String> lstCurrentTimeStamp = new Vector<String>(); 
    Vector<double[]> lstFixedCompareData = new Vector<double[]>();
    Vector<String> lstCompareTimeStamp = new Vector<String>(); 
    
    int tsIndex = 0;
    int geneIndex = 0;
    Object[] geneIndices = {""};
    int counterForLoadingGenes = 0;
    //static int numOfTimeSeries;
    
    
    // different options of graph base on number of windows: 0 = both in one window, 1 = first graph, 2 = second graph
    private int graphOption = 0;
    
    private HashMap<String, String> currentGenesHash = new HashMap<String, String>(); 
    private HashMap<String, String> compareGenesHash = new HashMap<String, String>(); 
    
    
    public TimeSeriesVisualizerComparison(Frame aFrame){
        super(aFrame);
        clearDataLists();
        //numOfTimeSeries = 0;
    }
    
    public void copyVisualizer(TimeSeriesVisualizerComparison tsvc){
        this.currentGenes = tsvc.currentGenes;
        this.compareGenes = tsvc.compareGenes;
        this.currentGenesHash = tsvc.currentGenesHash;
        this.compareGenesHash = tsvc.compareGenesHash;
        this.lstCurrentData = tsvc.lstCurrentData;
        this.lstCompareData = tsvc.lstCompareData;
        this.lstFixedCurrentData = tsvc.lstFixedCurrentData;
        this.lstFixedCompareData = tsvc.lstFixedCompareData;
        this.lstCurrentTimeStamp = tsvc.lstCurrentTimeStamp;
        this.lstCompareTimeStamp = tsvc.lstCompareTimeStamp;
        this.genes = tsvc.genes;
        this.tsIndex = tsvc.tsIndex;
        this.geneIndex = tsvc.geneIndex;
        this.geneIndices = tsvc.geneIndices;
    }
    
    
    // hash gene list so that different indices of same gene in both networks can be handled
    public HashMap<String, String> hashGeneList(String[] genes){
        HashMap<String, String> hash = new HashMap<String, String>();
        for (int i = 0; i < genes.length; i++){
            hash.put(genes[i], "" + i);
        }
        return hash;
    }

    public void setCurrentGenesHash(HashMap<String, String> currentGenesHash) {
        this.currentGenesHash = currentGenesHash;
    }

    public void setCompareGenesHash(HashMap<String, String> compareGenesHash) {
        this.compareGenesHash = compareGenesHash;
    }

    public int getGraphOption() {
        return graphOption;
    }

    public void setGraphOption(int graphOption) {
        this.graphOption = graphOption;
    }
    
    
    
    public void clearDataLists(){
        lstData = new Vector<double[]>();
        lstFixedData = new Vector<double[]>();
    
    }
    
    
    public String[] append(String[] arr, String element) {
        final int N = arr.length;
                
        arr = Arrays.copyOf(arr, N + 1);
        arr[N] = element;
        return arr;
    }
    
    
    public void readFile(String fileName, Vector<double[]> lstData, Vector<double[]> lstFixedData, Vector<String> lstTimeStamp, int option){
        
        try{
            
            // Open the file that is the first 
            // command line parameter
            FileInputStream fstream = new FileInputStream(fileName);
            // Get the object of DataInputStream
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            
            String[] arrTS;
            double[] arrDoubleTS;
            int numOfTimeSeries = 0;
            String timeStamp = "";
            String[] arrGenes;
            String[] genes = null;
            
            //Read File Line By Line
            while ((strLine = br.readLine()) != null)   {
                /*
                 * read first line to get genes
                 * get substring to trim "times" in front
                 * split the substring into array using "\t"
                 */
                
                if (strLine.equals("")){
                    System.out.println("empty line");
                    numOfTimeSeries++;
                }else{
                    //System.out.println("before:" + strLine);
                    timeStamp = strLine.substring(0, strLine.indexOf("\t"));
                    //System.out.println("trimmed:" + timeStamp);

                    strLine = strLine.substring(strLine.indexOf("\t") + 1);
                    //System.out.println("after:" + strLine);

                    if (numOfTimeSeries == 0){
                        /* instead of checking with numOfTimeSeries 
                         * to differentiate whether genes or timestamp coming in,
                         * should use a boolean to identify the current info coming in
                         */
                        
                        /*
                        arrGenes = strLine.split("\t");
                        if(arrGenes.length != genes.size()){
                            for (int i = 0; i < arrGenes.length; i++){
                                genes.add(arrGenes[i]);
                            }
                        }
                        */
                        if(genes == null || genes.length == 0){
                            genes = strLine.split("\t");
                        }else if(counterForLoadingGenes != 1){
                            System.out.println(genes.length + " genes found");
                            arrGenes = strLine.split("\t");
                            
                            // to check if both arrays are identical instead of checking length only
                            if(arrGenes.length != genes.length){
                                for (int i = 0; i < arrGenes.length; i++){
                                    genes = append(genes, arrGenes[i]);
                                }
                                counterForLoadingGenes = 1;
                            }
                        }                        
                        System.out.println("total genes: " + genes.length);
                    }else {//if (numOfTimeSeries == 1){                        
                        if(!lstTimeStamp.contains(timeStamp)){
                            lstTimeStamp.add(timeStamp);
                            System.out.println(timeStamp + " is added to the timestamp list.");
                        }
                        arrTS = strLine.split("\t");
                        arrDoubleTS = convertToDoubleArray(arrTS);
                        System.out.println("double array length:" + arrDoubleTS.length);
                        System.out.println("arrDoubleTS[0]:" + arrDoubleTS[0]);
                        lstData.add(arrDoubleTS);

                    }
                }               
            }
            System.out.println("total arrays in list: " + lstData.size());
            lstFixedData = fixVector(lstData, lstFixedData, lstTimeStamp, genes);
            System.out.println("total arrays in list: " + lstFixedData.size());
            
            switch(option){
                case 1:
                    currentGenes = genes;
                    break;
                case 2:
                    compareGenes = genes;
                    break;
            }
            
            System.out.println("currentGenes: " + currentGenes.length);
            
            //Close the input stream
            in.close();
        }catch (Exception e){//Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }
    }
    
    
    
    private Vector<double[]> fixVector(Vector<double[]> lstData, Vector<double[]> lstFixedData, Vector<String> lstTimeStamp, String[] genes){
        double[] arr = new double[lstTimeStamp.size()];
         
        int aIndex = 0;
        for (int gIndex = 0; gIndex < genes.length; gIndex++){
            System.out.println("Gene: " + genes[gIndex]);
            aIndex = 0;
            while(aIndex < lstData.size()){
                arr = new double[lstTimeStamp.size()];
                for (int index = 0; index < lstTimeStamp.size(); index++){
                    arr[index] = lstData.get(aIndex)[gIndex];
                    //System.out.println("Time " + lstTimeStamp.get(index) + ": " + arr[index]);
                    aIndex++;
                }
                lstFixedData.add(arr);
            }
        }
        return lstFixedData;
    }
    
    
    private double[] convertToDoubleArray(Object[] data){
        double[] arr = new double[data.length];
        for (int i = 0; i < data.length; i++){
            arr[i] = Double.parseDouble(data[i].toString());
            System.out.println("arr[" + (i + 1) + "]: " + arr[i]);
        }
        return arr;
    }
    
    
    
    
    private double getMax(Vector<double[]> lstData) {
        double max = -Double.MAX_VALUE;
        double[] data;
        
        for(int iList = 0; iList < lstData.size(); iList++){
            data = lstData.get(iList);
            for(int i = 0; i < data.length; i++) {
                if(data[i] > max)
                    max = data[i];
            }
        }
            
        return max;
    }
 
        
    
    public void getGenes(Vector<String> vGenes){
        // find common genes from both networks
        Set<String> genes;
        String g;
        genes = currentGenesHash.keySet();
        Iterator i = genes.iterator();
        while(i.hasNext()){
            g = (String)i.next();
            if (compareGenesHash.containsKey(g)){
                vGenes.add(g);
            }
        }
    }
    
    public int getNumOfCommonGenes(){
        /*
        int count = 0;
        Set<String> genes;
        String g;
        genes = currentGenesHash.keySet();
        Iterator i = genes.iterator();
        while(i.hasNext()){
            g = (String)i.next();
            if (compareGenesHash.containsKey(g)){
                count++;
            }
        }
        return count;
        */
        return genes.length;
    }
    
    public void getCommonGenes(){        
        genes = new String[]{};
        Set<String> geneSet;
        String g;
        geneSet = currentGenesHash.keySet();
        Iterator i = geneSet.iterator();
        while(i.hasNext()){
            g = (String)i.next();
            if (compareGenesHash.containsKey(g)){
                genes =  append(genes, g);;
            }
        }
    }
    
    public int getCommonTimeSeries(){
        if (!lstFixedCurrentData.isEmpty() && !lstFixedCompareData.isEmpty()){
            return Math.min((lstFixedCurrentData.size() / currentGenes.length), (lstFixedCompareData.size() / compareGenes.length));
        }else{// if(lstFixedCurrentData != null){
            return lstFixedCurrentData.size() / currentGenes.length;
        }
    }
    
    public int getTotalGenes(){
        if (genes != null){
            return genes.length;
        }
        return 0;
    }
    
    public int getTotalTimeSeries(){
        return lstFixedData.size() / genes.length;
        //return numOfTimeSeries;
    }
    
    public int getTsIndex(){
        return tsIndex;
    }
    
    public void setTsIndex(int index){
        tsIndex = index;
    }
    
    public int getGeneIndex(){
        return geneIndex;
    }
    
    public void setGeneIndex(int index){
        geneIndex = index;
    }

    public Object[] getGeneIndices() {
        return geneIndices;
    }

    public void setGeneIndices(Object[] indices) {
        geneIndices = indices;
    }

    public String[] getGenes() {
        return genes;
    }

    public void setGenes(String[] genes) {
        this.genes = genes;
    }

    public Vector<double[]> getLstFixedData() {
        return lstFixedData;
    }

    public void setLstFixedData(Vector<double[]> lstFixedData) {
        this.lstFixedData = lstFixedData;
    }

    public Vector<String> getLstTimeStamp() {
        return lstTimeStamp;
    }

    public void setLstTimeStamp(Vector<String> lstTimeStamp) {
        this.lstTimeStamp = lstTimeStamp;
    }

    
    public void displayGraph(int tIndex, int gIndex){
        setTsIndex(tIndex);
        setGeneIndex(gIndex);
        graph = new MyTimeSeriesGraph();        
        graphWindow = this;
        graphPanel_.removeAll();
        graphPanel_.add(graph);
        graphPanel_.revalidate();
        graphPanel_.repaint();
    }
    
    public void displayGraph(int tIndex, Object[] gIndices){
        setTsIndex(tIndex);
        if(gIndices.length > 0){
            setGeneIndices(gIndices);
        }
        if (graphWindow != null){
            graphWindow.dispose();
        }
        graph = new MyTimeSeriesGraph();        
        graphWindow = this;
        graphPanel_.removeAll();
        graphPanel_.add(graph);
        graphPanel_.revalidate();
        graphPanel_.repaint();
    }
    
    
    
    public class MyTimeSeriesGraph extends JPanel{
        
        

        @SuppressWarnings("empty-statement")
        protected void paintComponent(Graphics g) { 

            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D)g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth() - 100;
            int h = getHeight();
            // Draw ordinate.
            g2.draw(new Line2D.Double(PAD, PAD, PAD, h-PAD));
            // Draw abcissa.
            g2.draw(new Line2D.Double(PAD, h-PAD, w-PAD, h-PAD));
            // Draw labels.
            Font font = g2.getFont();
            FontRenderContext frc = g2.getFontRenderContext();
            LineMetrics lm = font.getLineMetrics("0", frc);
            float sh = lm.getAscent() + lm.getDescent();
            // Ordinate label.
            String s = "Gene Expression";
            float sy = PAD + ((h - 2*PAD) - s.length()*sh)/2 + lm.getAscent();


            for(int i = 0; i < s.length(); i++) {
                String letter = String.valueOf(s.charAt(i));
                float sw = (float)font.getStringBounds(letter, frc).getWidth();
                float sx = (PAD - sw)/2;
                g2.drawString(letter, sx, sy);
                sy += sh;
            }


            // Space between axis and label.
            final int SPAD = 2;
            double max = Math.max(getMax(lstCurrentData), getMax(lstCompareData));
            double scale = (double)(h - 2*PAD)/max;
            float sw, sx;
            double label = 0;
            double xInc;

            while(label < max){
                //System.out.println("label:" + label);
                s = String.valueOf(Math.round(label * 10.0) / 10.0);
                sw = (float)font.getStringBounds(s, frc).getWidth();
                sx = PAD - sw - SPAD;
                // For the ordinate you count from the bottom up
                // (component/graphics origin is at upper left).
                // The actual value location on the ordinate will be
                // y = h - PAD - scale*data[i]
                // which you would use if you wanted to draw a tick mark on the
                // ordinate.
                // Offset this to locate the text origin. This usually requires
                // some experimenting. To start you can try something like
                sy = (float) (h - PAD - scale*label + lm.getAscent()/2);
                g2.drawString(s, sx, sy);
                g2.draw(new Line2D.Double(PAD - 2, sy - lm.getAscent()/2, PAD + 2, sy - lm.getAscent()/2));
                label += 0.1;
            }


            double[] currentData, compareData;
            //final Random r = new Random();
            Color c;
            int rValue, gValue, bValue, aValue;
            rValue = 125;
            gValue = 663;
            bValue = 526;
            aValue = 98;
            
            int currentTotalTimeSeries = lstFixedCurrentData.size() / currentGenes.length;//getCommonTimeSeries(); //lstFixedData.size() / genes.length;
            int compareTotalTimeSeries = lstFixedCompareData.size() / compareGenes.length;
            System.out.println("total time series: " + currentTotalTimeSeries + " and " + compareTotalTimeSeries);


            switch(graphOption){
                case 0:
                    xInc = (double)(w - 2*PAD)/(lstCurrentTimeStamp.size()-1);

                    for (int i = 0; i < lstCurrentTimeStamp.size(); i++){
                        s =  lstCurrentTimeStamp.get(i);
                        sy = h - PAD + (PAD - sh)/2;//+ lm.getAscent();
                        sw = (float)font.getStringBounds(s, frc).getWidth();
                        sx = (float) (PAD + i*xInc - (sw/2));
                                //(float) (PAD + ((i + 1) * (sw/2))); //+ lm.getAscent();
                        g2.drawString(s, sx, sy);
                        g2.draw(new Line2D.Double(sx + (sw/2), h - PAD - 2, sx + (sw/2), h - PAD + 2));
                    }
                    s = "Time";
                    sy = h - PAD + (PAD - sh)/2 + 20;//+ lm.getAscent();
                    sw = (float)font.getStringBounds(s, frc).getWidth();
                    sx = (float) (PAD + (lstCurrentTimeStamp.size()*0.5*xInc) - (sw/2));
                    g2.drawString(s, sx, sy);
                    
                    break;
                case 1:
                    xInc = (double)(w - 2*PAD)/(lstCurrentTimeStamp.size()-1);

                    for (int i = 0; i < lstCurrentTimeStamp.size(); i++){
                        s =  lstCurrentTimeStamp.get(i);
                        sy = h - PAD + (PAD - sh)/2;//+ lm.getAscent();
                        sw = (float)font.getStringBounds(s, frc).getWidth();
                        sx = (float) (PAD + i*xInc - (sw/2));
                                //(float) (PAD + ((i + 1) * (sw/2))); //+ lm.getAscent();
                        g2.drawString(s, sx, sy);
                        g2.draw(new Line2D.Double(sx + (sw/2), h - PAD - 2, sx + (sw/2), h - PAD + 2));
                    }
                    s = "Time";
                    sy = h - PAD + (PAD - sh)/2 + 20;//+ lm.getAscent();
                    sw = (float)font.getStringBounds(s, frc).getWidth();
                    sx = (float) (PAD + (lstCurrentTimeStamp.size()*0.5*xInc) - (sw/2));
                    g2.drawString(s, sx, sy);
                    break;
                case 2:
                    xInc = (double)(w - 2*PAD)/(lstCompareTimeStamp.size()-1);

                    for (int i = 0; i < lstCompareTimeStamp.size(); i++){
                        s =  lstCompareTimeStamp.get(i);
                        sy = h - PAD + (PAD - sh)/2;//+ lm.getAscent();
                        sw = (float)font.getStringBounds(s, frc).getWidth();
                        sx = (float) (PAD + i*xInc - (sw/2));
                                //(float) (PAD + ((i + 1) * (sw/2))); //+ lm.getAscent();
                        g2.drawString(s, sx, sy);
                        g2.draw(new Line2D.Double(sx + (sw/2), h - PAD - 2, sx + (sw/2), h - PAD + 2));
                    }
                    s = "Time";
                    sy = h - PAD + (PAD - sh)/2 + 20;//+ lm.getAscent();
                    sw = (float)font.getStringBounds(s, frc).getWidth();
                    sx = (float) (PAD + (lstCompareTimeStamp.size()*0.5*xInc) - (sw/2));
                    g2.drawString(s, sx, sy);
                    break;
            }
            
                       

           
            
            
            int index = -1;
            if (geneIndices.length > 0 && geneIndices[0] != "" && geneIndices[0] != "All genes"){
                for (int iList = 0; iList < geneIndices.length; iList++){//lstFixedData.size()
                    index = Integer.parseInt(currentGenesHash.get(geneIndices[iList].toString()));
                    
                    currentData = lstFixedCurrentData.get((currentTotalTimeSeries * index) + tsIndex);
                    compareData = lstFixedCompareData.get((compareTotalTimeSeries * (Integer.parseInt(compareGenesHash.get(geneIndices[iList].toString())))) + tsIndex);
                            
                    c = new Color((rValue * (index + 7)) % 256, (gValue * (index + 4)) % 256, 
                            (bValue * (index + 5)) % 256);//, (aValue * (index + 1)) % 200);
                    g2.setPaint(c);

                    switch(graphOption){
                        case 0:
                            //int len = Math.min(currentData.length, compareData.length) -1;
                            // Draw lines.
                            xInc = (double)(w - 2*PAD)/ (currentData.length - 1);


                            // write legend
                            g2.draw(new Line2D.Double( w + 20 , PAD + ((iList * 2) * ((h - PAD)  / (2 * getNumOfCommonGenes()))), w + 40, PAD + ((iList * 2) * ((h - PAD) / (2 * getNumOfCommonGenes())))));
                            g2.drawString(currentGenes[index] + "*", w + 40, PAD + ((iList * 2) * ((h - PAD) / (2 * getNumOfCommonGenes()))));
                           
                            for(int i = 0; i < currentData.length - 1; i++) {
                                double x1 = PAD + i*xInc;
                                double y1 = h - PAD - scale*currentData[i];
                                double x2 = PAD + (i+1)*xInc;
                                double y2 = h - PAD - scale*currentData[i+1];
                                g2.draw(new Line2D.Double(x1, y1, x2, y2));
                            }
                            // Mark data points.
                            g2.setPaint(Color.red);
                            for(int i = 0; i < currentData.length; i++) {
                                double x = PAD + i*xInc;
                                double y = h - PAD - scale*currentData[i];
                                g2.fill(new Ellipse2D.Double(x-2, y-2, 4, 4));

                            }
                            
                            
                            c = new Color((rValue * (index + 2)) % 256, (gValue * (index + 3)) % 256, 
                                    (bValue * (index + 2)) % 256);//, (aValue * (index + 1)) % 200);
                            g2.setPaint(c);
                    
                            xInc = (double)(w - 2*PAD)/ (compareData.length -1);
                            
                            g2.draw(new Line2D.Double( w + 20 , PAD + (((iList * 2) + 1) * ((h - PAD)  / (2 * getNumOfCommonGenes()))), w + 40, PAD + (((iList * 2) + 1) * ((h - PAD) / (2 * getNumOfCommonGenes())))));
                            g2.drawString(currentGenes[index], w + 40, PAD + (((iList * 2) + 1) * ((h - PAD) / (2 * getNumOfCommonGenes()))));

                            //g2.setPaint(Color.green.darker());
                            
                            
                            for(int i = 0; i < compareData.length - 1; i++) {
                                double x1 = PAD + i*xInc;
                                double y1 = h - PAD - scale*compareData[i];
                                double x2 = PAD + (i+1)*xInc;
                                double y2 = h - PAD - scale*compareData[i+1];
                                g2.draw(new Line2D.Double(x1, y1, x2, y2));
                            }
                            // Mark data points.
                            g2.setPaint(Color.red);
                            for(int i = 0; i < compareData.length; i++) {
                                double x = PAD + i*xInc;
                                double y = h - PAD - scale*compareData[i];
                                g2.fill(new Ellipse2D.Double(x-2, y-2, 4, 4));

                            }
                            break;
                        case 1:
                            // Draw lines.
                            xInc = (double)(w - 2*PAD)/(currentData.length-1);


                            // write legend
                            g2.draw(new Line2D.Double( w + 20 , PAD + (iList * ((h - PAD)  / getNumOfCommonGenes())), w + 40, PAD + (iList * ((h - PAD) / getNumOfCommonGenes()))));
                            g2.drawString(currentGenes[index] + "*", w + 40, PAD + (iList * ((h - PAD) / getNumOfCommonGenes())));

                            //g2.setPaint(Color.green.darker());
                            for(int i = 0; i < currentData.length-1; i++) {
                                double x1 = PAD + i*xInc;
                                double y1 = h - PAD - scale*currentData[i];
                                double x2 = PAD + (i+1)*xInc;
                                double y2 = h - PAD - scale*currentData[i+1];
                                g2.draw(new Line2D.Double(x1, y1, x2, y2));
                            }
                            // Mark data points.
                            g2.setPaint(Color.red);
                            for(int i = 0; i < currentData.length; i++) {
                                double x = PAD + i*xInc;
                                double y = h - PAD - scale*currentData[i];
                                g2.fill(new Ellipse2D.Double(x-2, y-2, 4, 4));

                            }
                            break;
                        case 2:
                            // Draw lines.
                            xInc = (double)(w - 2*PAD)/(compareData.length-1);


                            // write legend
                            g2.draw(new Line2D.Double( w + 20 , PAD + (iList * ((h - PAD)  / getNumOfCommonGenes())), w + 40, PAD + (iList * ((h - PAD) / getNumOfCommonGenes()))));
                            g2.drawString(currentGenes[index], w + 40, PAD + (iList * ((h - PAD) / getNumOfCommonGenes())));

                            //g2.setPaint(Color.green.darker());
                            for(int i = 0; i < compareData.length-1; i++) {
                                double x1 = PAD + i*xInc;
                                double y1 = h - PAD - scale*compareData[i];
                                double x2 = PAD + (i+1)*xInc;
                                double y2 = h - PAD - scale*compareData[i+1];
                                g2.draw(new Line2D.Double(x1, y1, x2, y2));
                            }
                            // Mark data points.
                            g2.setPaint(Color.red);
                            for(int i = 0; i < compareData.length; i++) {
                                double x = PAD + i*xInc;
                                double y = h - PAD - scale*compareData[i];
                                g2.fill(new Ellipse2D.Double(x-2, y-2, 4, 4));

                            }
                            break;
                    }
                }
            }else if (geneIndex == 0 || geneIndices[0] == "All genes"){
                for (int iList = 0; iList < genes.length; iList++){//lstFixedData.size()
                    index = Integer.parseInt(currentGenesHash.get(genes[iList].toString()));
                    
                    currentData = lstFixedCurrentData.get((currentTotalTimeSeries * index) + tsIndex);
                    compareData = lstFixedCompareData.get((compareTotalTimeSeries * (Integer.parseInt(compareGenesHash.get(genes[iList].toString())))) + tsIndex);
                            
                    c = new Color((rValue * (index + 7)) % 256, (gValue * (index + 4)) % 256, 
                            (bValue * (index + 5)) % 256);//, (aValue * (index + 1)) % 200);
                    g2.setPaint(c);
                    
                    switch(graphOption){
                        case 0:
                            // Draw lines.
                            xInc = (double)(w - 2*PAD)/ (currentData.length - 1);


                            // write legend
                            g2.draw(new Line2D.Double( w + 20 , PAD + ((iList * 2) * ((h - PAD)  / (2 * getNumOfCommonGenes()))), w + 40, PAD + ((iList * 2) * ((h - PAD) / (2 * getNumOfCommonGenes())))));
                            g2.drawString(currentGenes[index] + "*", w + 40, PAD + ((iList * 2) * ((h - PAD) / (2 * getNumOfCommonGenes()))));

                            for(int i = 0; i < currentData.length - 1; i++) {
                                double x1 = PAD + i*xInc;
                                double y1 = h - PAD - scale*currentData[i];
                                double x2 = PAD + (i+1)*xInc;
                                double y2 = h - PAD - scale*currentData[i+1];
                                g2.draw(new Line2D.Double(x1, y1, x2, y2));
                            }
                            // Mark data points.
                            g2.setPaint(Color.red);
                            for(int i = 0; i < currentData.length; i++) {
                                double x = PAD + i*xInc;
                                double y = h - PAD - scale*currentData[i];
                                g2.fill(new Ellipse2D.Double(x-2, y-2, 4, 4));

                            }


                            c = new Color((rValue * (index + 2)) % 256, (gValue * (index + 3)) % 256, 
                                    (bValue * (index + 2)) % 256);//, (aValue * (index + 1)) % 200);
                            g2.setPaint(c);

                            xInc = (double)(w - 2*PAD)/ (compareData.length -1);

                            g2.draw(new Line2D.Double( w + 20 , PAD + (((iList * 2) + 1) * ((h - PAD)  / (2 * getNumOfCommonGenes()))), w + 40, PAD + (((iList * 2) + 1) * ((h - PAD) / (2 * getNumOfCommonGenes())))));
                            g2.drawString(currentGenes[index], w + 40, PAD + (((iList * 2) + 1) * ((h - PAD) / (2 * getNumOfCommonGenes()))));

                            //g2.setPaint(Color.green.darker());


                            for(int i = 0; i < compareData.length - 1; i++) {
                                double x1 = PAD + i*xInc;
                                double y1 = h - PAD - scale*compareData[i];
                                double x2 = PAD + (i+1)*xInc;
                                double y2 = h - PAD - scale*compareData[i+1];
                                g2.draw(new Line2D.Double(x1, y1, x2, y2));
                            }
                            // Mark data points.
                            g2.setPaint(Color.red);
                            for(int i = 0; i < compareData.length; i++) {
                                double x = PAD + i*xInc;
                                double y = h - PAD - scale*compareData[i];
                                g2.fill(new Ellipse2D.Double(x-2, y-2, 4, 4));

                            }
                            break;
                        case 1:
                            // Draw lines.
                            xInc = (double)(w - 2*PAD)/ (currentData.length - 1);


                            // write legend
                            g2.draw(new Line2D.Double( w + 20 , PAD + (iList * ((h - PAD)  / getNumOfCommonGenes())), w + 40, PAD + (iList * ((h - PAD) / getNumOfCommonGenes()))));
                            g2.drawString(currentGenes[index] + "*", w + 40, PAD + (iList * ((h - PAD) / getNumOfCommonGenes())));

                            for(int i = 0; i < currentData.length - 1; i++) {
                                double x1 = PAD + i*xInc;
                                double y1 = h - PAD - scale*currentData[i];
                                double x2 = PAD + (i+1)*xInc;
                                double y2 = h - PAD - scale*currentData[i+1];
                                g2.draw(new Line2D.Double(x1, y1, x2, y2));
                            }
                            // Mark data points.
                            g2.setPaint(Color.red);
                            for(int i = 0; i < currentData.length; i++) {
                                double x = PAD + i*xInc;
                                double y = h - PAD - scale*currentData[i];
                                g2.fill(new Ellipse2D.Double(x-2, y-2, 4, 4));

                            }
                            break;
                        case 2:
                            c = new Color((rValue * (index + 2)) % 256, (gValue * (index + 3)) % 256, 
                                    (bValue * (index + 2)) % 256);//, (aValue * (index + 1)) % 200);
                            g2.setPaint(c);

                            xInc = (double)(w - 2*PAD)/ (compareData.length -1);

                            g2.draw(new Line2D.Double( w + 20 , PAD + (iList * ((h - PAD)  / getNumOfCommonGenes())), w + 40, PAD + (iList * ((h - PAD) / getNumOfCommonGenes()))));
                            g2.drawString(currentGenes[index], w + 40, PAD + (iList * ((h - PAD) / getNumOfCommonGenes())));

                            //g2.setPaint(Color.green.darker());


                            for(int i = 0; i < compareData.length - 1; i++) {
                                double x1 = PAD + i*xInc;
                                double y1 = h - PAD - scale*compareData[i];
                                double x2 = PAD + (i+1)*xInc;
                                double y2 = h - PAD - scale*compareData[i+1];
                                g2.draw(new Line2D.Double(x1, y1, x2, y2));
                            }
                            // Mark data points.
                            g2.setPaint(Color.red);
                            for(int i = 0; i < compareData.length; i++) {
                                double x = PAD + i*xInc;
                                double y = h - PAD - scale*compareData[i];
                                g2.fill(new Ellipse2D.Double(x-2, y-2, 4, 4));

                            }
                            break;
                    }
                }                                
            }
        }
        
    }

}