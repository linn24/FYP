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
import java.util.Vector;


public class TimeSeriesVisualizer extends TimeSeriesVisualizerWindow {
    final int PAD = 80;
    static Vector<double[]> lstData = new Vector<double[]>();
    //static
    String[] genes;
    //static Vector<String> genes = new Vector<String>();
    TimeSeriesVisualizer graphWindow;
    MyTimeSeriesGraph graph;
        
    static Vector<double[]> lstFixedData = new Vector<double[]>();
    static Vector<String> lstTimeStamp = new Vector<String>(); 
    static int tsIndex = 0;
    static int geneIndex = 0;
    static int[] geneIndices = {-1};
    int counterForLoadingGenes = 0;
    static int numOfTimeSeries;
    
    public TimeSeriesVisualizer(Frame aFrame){
        super(aFrame);
        clearDataLists();
        numOfTimeSeries = 0;
    }
    
    public void clearDataLists(){
        lstData = new Vector<double[]>();
        lstFixedData = new Vector<double[]>();
    
    }
    
    
    public static String[] append(String[] arr, String element) {
        final int N = arr.length;
        arr = Arrays.copyOf(arr, N + 1);
        arr[N] = element;
        return arr;
    }
    
    public void readFile(String fileName){
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
            //int numOfTimeSeries = 0;
            String timeStamp = "";
            String[] arrGenes;
            
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
            fixVector();
            System.out.println("total arrays in list: " + lstFixedData.size());
            
            //Close the input stream
            in.close();
        }catch (Exception e){//Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }
    }
    
    
    
    private void fixVector(){
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
    }
    
    
    private double[] convertToDoubleArray(Object[] data){
        double[] arr = new double[data.length];
        for (int i = 0; i < data.length; i++){
            arr[i] = Double.parseDouble(data[i].toString());
            System.out.println("arr[" + (i + 1) + "]: " + arr[i]);
        }
        return arr;
    }
    
    
    
    private double getMax() {
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
        for (int i = 0; i < genes.length; i++){
            vGenes.add(genes[i]);
        }
        
    }
    
    public int getTotalGenes(){
        if (genes != null){
            return genes.length;
        }
        return 0;
    }
    
    public int getTotalTimeSeries(){
        //return lstFixedData.size() / genes.length;
        return numOfTimeSeries;
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

    public int[] getGeneIndices() {
        return geneIndices;
    }

    public void setGeneIndices(int[] indices) {
        geneIndices = indices;
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
    
    public void displayGraph(int tIndex, int[] gIndices){
        setTsIndex(tIndex);
        setGeneIndices(gIndices);
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
            double scale = (double)(h - 2*PAD)/getMax();
            float sw, sx;
            double label = 0;
            double xInc;

            while(label < getMax()){
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


            // Abcissa label.
            /*
            s = "x axis";
            sy = h - PAD + (PAD - sh)/2 + lm.getAscent();
            sw = (float)font.getStringBounds(s, frc).getWidth();
            sx = (w - sw)/2;
            g2.drawString(s, sx, sy);
            */

            xInc = (double)(w - 2*PAD)/(lstTimeStamp.size()-1);

            for (int i = 0; i < lstTimeStamp.size(); i++){
                s =  lstTimeStamp.get(i);
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
            sx = (float) (PAD + (lstTimeStamp.size()*0.5*xInc) - (sw/2));
            //AffineTransform orig = g2.getTransform();
            //g2.rotate(-Math.PI/2);
            g2.drawString(s, sx, sy);
            //g2.drawString(s, 100, 300);

            //g2.setTransform(orig);
            /*
            s = "Gene Expression";
            sy = h - PAD + (PAD - sh)/2 + 20;//+ lm.getAscent();
            sw = (float)font.getStringBounds(s, frc).getWidth();
            sx = (float) (PAD + (lstTimeStamp.size()*0.5*xInc) - (sw/2));
            g2.drawString(s, sx, sy);
            */

            double[] data;
            //final Random r = new Random();
            Color c;
            int rValue, gValue, bValue, aValue;
            rValue = 125;
            gValue = 663;
            bValue = 526;
            aValue = 98;
            
            int totalTimeSeries = numOfTimeSeries;
            //int totalTimeSeries = lstFixedData.size() / genes.length;
            System.out.println("total time series: " + totalTimeSeries);

            int index = -1;
            if (geneIndices.length > 0 && geneIndices[0] != -1 && geneIndices[0] != 0){
                for (int iList = 0; iList < geneIndices.length; iList++){//lstFixedData.size()
                    index = geneIndices[iList] - 1;
                    data = lstFixedData.get((totalTimeSeries * index) + tsIndex);
                    // Draw lines.
                    xInc = (double)(w - 2*PAD)/(data.length-1);

                    //c = new Color(r.nextInt(256),r.nextInt(256),r.nextInt(256),r.nextInt(256));
                    c = new Color((rValue * (index + 7)) % 256, (gValue * (index + 4)) % 256, 
                            (bValue * (index + 5)) % 256);//, (aValue * (index + 1)) % 200);
                    g2.setPaint(c);

                    // write legend
                    g2.draw(new Line2D.Double( w + 20 , PAD + (iList * ((h - PAD)  / genes.length)), w + 40, PAD + (iList * ((h - PAD) / genes.length))));
                    g2.drawString(genes[index], w + 40, PAD + (iList * ((h - PAD) / genes.length)));

                    //g2.setPaint(Color.green.darker());
                    for(int i = 0; i < data.length-1; i++) {
                        double x1 = PAD + i*xInc;
                        double y1 = h - PAD - scale*data[i];
                        double x2 = PAD + (i+1)*xInc;
                        double y2 = h - PAD - scale*data[i+1];
                        g2.draw(new Line2D.Double(x1, y1, x2, y2));
                    }
                    // Mark data points.
                    g2.setPaint(Color.red);
                    for(int i = 0; i < data.length; i++) {
                        double x = PAD + i*xInc;
                        double y = h - PAD - scale*data[i];
                        g2.fill(new Ellipse2D.Double(x-2, y-2, 4, 4));

                    }
                }
            }else if (geneIndex == 0 || geneIndices[0] == 0){
                for (int iList = 0; iList < genes.length; iList++){//lstFixedData.size()
                    data = lstFixedData.get((totalTimeSeries * iList) + tsIndex);
                    // Draw lines.
                    xInc = (double)(w - 2*PAD)/(data.length-1);

                    //c = new Color(r.nextInt(256),r.nextInt(256),r.nextInt(256),r.nextInt(256));
                    c = new Color((rValue * (iList + 7)) % 256, (gValue * (iList + 4)) % 256, 
                            (bValue * (iList + 5)) % 256);//, (aValue * (iList + 1)) % 200);
                    g2.setPaint(c);

                    // write legend
                    g2.draw(new Line2D.Double( w + 20 , PAD + (iList * ((h - PAD)  / genes.length)), w + 40, PAD + (iList * ((h - PAD) / genes.length))));
                    g2.drawString(genes[iList], w + 40, PAD + (iList * ((h - PAD) / genes.length)));

                    //g2.setPaint(Color.green.darker());
                    for(int i = 0; i < data.length-1; i++) {
                        double x1 = PAD + i*xInc;
                        double y1 = h - PAD - scale*data[i];
                        double x2 = PAD + (i+1)*xInc;
                        double y2 = h - PAD - scale*data[i+1];
                        g2.draw(new Line2D.Double(x1, y1, x2, y2));
                    }
                    // Mark data points.
                    g2.setPaint(Color.red);
                    for(int i = 0; i < data.length; i++) {
                        double x = PAD + i*xInc;
                        double y = h - PAD - scale*data[i];
                        g2.fill(new Ellipse2D.Double(x-2, y-2, 4, 4));

                    }
                }
            }else{

                data = lstFixedData.get((totalTimeSeries * (geneIndex - 1)) + tsIndex);
                // Draw lines.
                xInc = (double)(w - 2*PAD)/(data.length-1);

                g2.setPaint(Color.green.darker());

                // write legend
                g2.draw(new Line2D.Double( w + 20 , PAD + ((geneIndex - 1) * ((h - PAD)  / genes.length)), w + 40, PAD + ((geneIndex - 1) * ((h - PAD) / genes.length))));
                g2.drawString(genes[(geneIndex - 1)], w + 40, PAD + ((geneIndex - 1) * ((h - PAD) / genes.length)));


                for(int i = 0; i < data.length-1; i++) {
                    double x1 = PAD + i*xInc;
                    double y1 = h - PAD - scale*data[i];
                    double x2 = PAD + (i+1)*xInc;
                    double y2 = h - PAD - scale*data[i+1];
                    g2.draw(new Line2D.Double(x1, y1, x2, y2));
                }
                // Mark data points.
                g2.setPaint(Color.red);
                for(int i = 0; i < data.length; i++) {
                    double x = PAD + i*xInc;
                    double y = h - PAD - scale*data[i];
                    g2.fill(new Ellipse2D.Double(x-2, y-2, 4, 4));

                }

            }
        }

    }

}