/*
Copyright (c) 2008-2010 Daniel Marbach & Thomas Schaffter

We release this software open source under an MIT license (see below). If this
software was useful for your scientific work, please cite our paper(s) listed
on http://gnw.sourceforge.net.

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/

package ch.epfl.lis.gnwgui.windows;

import ch.epfl.lis.gnwgui.GnwGuiSettings;
import ch.epfl.lis.gnwgui.idesktop.IElement;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Vector;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SpringLayout;


/**
 * 
 * 
 * @author Thomas Schaffter (firstname.name@gmail.com)
 */
public class TimeSeriesSelectionWindow extends GenericWindow
{	
	/** Serialization */
	private static final long serialVersionUID = 1L;
	
	protected JPanel filePanel_, timeseriesPanel_, genePanel_;
	
        protected JLabel fileName_, timeSeriesName_, geneName_;
        protected JComboBox fileList_, timeSeriesList_;
        protected JList geneList_;
        protected JScrollPane scrollPane_;
        
        protected IElement item_;
	
    /** Logger for this class */
	@SuppressWarnings("unused")
	private static Logger log_ = Logger.getLogger(TimeSeriesSelectionWindow.class.getName());
	
	// ----------------------------------------------------------------------------
	// PUBLIC METHODS
	
	public TimeSeriesSelectionWindow(Frame aFrame, IElement item)
	{	
		super(aFrame, false);
                this.item_ = item;
		setSize(840, 665);
		setHeaderTitle("Time Series Selection");
		setTitle("Select Time Series");
                setLocationRelativeTo(GnwGuiSettings.getInstance().getGnwGui().getFrame());

		filePanel_ = new JPanel();
		filePanel_.setBackground(Color.WHITE);
		filePanel_.setLayout(new GridLayout(0,2));
		filePanel_.setPreferredSize(new Dimension(0, 0));
                
                timeseriesPanel_ = new JPanel();
		timeseriesPanel_.setBackground(Color.WHITE);
		timeseriesPanel_.setLayout(new GridLayout(0,2));
		timeseriesPanel_.setPreferredSize(new Dimension(0, 0));
                
                genePanel_ = new JPanel();
		genePanel_.setBackground(Color.WHITE);
		genePanel_.setLayout(new GridLayout(0,2));
		genePanel_.setPreferredSize(new Dimension(0, 0));
                
                Vector<String> vFiles = new Vector<String>();
                vFiles = this.populateFileOptions();
                
                fileList_ = new JComboBox();
                fileList_ = new JComboBox(vFiles);
                fileList_.setSelectedIndex(0);
                

                timeSeriesList_ = new JComboBox();        
                geneList_ = new JList();
                scrollPane_ = new JScrollPane(geneList_);

                fileName_ = new JLabel();
                timeSeriesName_ = new JLabel();
                geneName_ = new JLabel();

                fileName_.setFont(fileName_.getFont().deriveFont(Font.BOLD));
                fileName_.setHorizontalAlignment(JLabel.CENTER);
                fileName_.setBorder(BorderFactory.createEmptyBorder(10,0,0,0));
                fileName_.setText("Select File");

                timeSeriesName_.setFont(timeSeriesName_.getFont().deriveFont(Font.BOLD));
                timeSeriesName_.setHorizontalAlignment(JLabel.CENTER);
                timeSeriesName_.setBorder(BorderFactory.createEmptyBorder(10,0,0,0));
                timeSeriesName_.setText("Select Time Series");

                geneName_.setFont(geneName_.getFont().deriveFont(Font.BOLD));
                geneName_.setHorizontalAlignment(JLabel.CENTER);
                geneName_.setBorder(BorderFactory.createEmptyBorder(10,0,0,0));
                geneName_.setText("Select Gene");
                
                filePanel_.add(fileName_);
                filePanel_.add(fileList_);
                filePanel_.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
                
                timeseriesPanel_.add(timeSeriesName_);
                timeseriesPanel_.add(timeSeriesList_);
                timeseriesPanel_.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
                
                genePanel_.add(geneName_);        
                genePanel_.add(scrollPane_);
                genePanel_.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
                
                getContentPane().setLayout(new GridLayout(0,1));
		getContentPane().add(filePanel_);
                getContentPane().add(timeseriesPanel_);
                getContentPane().add(genePanel_);
	}
        
        private Vector<String> populateFileOptions(){
            Vector<String> vFiles = new Vector<String>();
            String[] files = { "dream4_timeseries", "noexpnoise_dream4_timeseries"
                    , "noexpnoise_proteins_dream4_timeseries", "nonoise_dream4_timeseries"
                    , "nonoise_proteins_dream4_timeseries", "proteins_dream4_timeseries"};

            File file;

            for (int i = 0; i < files.length; i++){
                file = new File(item_.getLabel() + "_" + files[i] + ".tsv");
                if(file.exists()){
                    vFiles.add(files[i]);
                }        
            }
            return vFiles;
        }
}
