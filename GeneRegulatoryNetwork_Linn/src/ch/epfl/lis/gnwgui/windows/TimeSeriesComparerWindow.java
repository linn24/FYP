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
import java.awt.Insets;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;


/**
 * 
 * 
 * @author Thomas Schaffter (firstname.name@gmail.com)
 */
public class TimeSeriesComparerWindow extends TimeSeriesSelectionWindow
{	
	/** Serialization */
	private static final long serialVersionUID = 1L;
	
	protected JPanel numOfWindowPanel_;
	protected JPanel selectPanel_;
        
        protected JRadioButton oneWin_, twoWin_;
        protected JLabel selectVersion_;
        protected JButton bOpen_;
	
	
    /** Logger for this class */
	@SuppressWarnings("unused")
	private static Logger log_ = Logger.getLogger(TimeSeriesComparerWindow.class.getName());
	
	// ----------------------------------------------------------------------------
	// PUBLIC METHODS
	
	public TimeSeriesComparerWindow(Frame aFrame, IElement item)
	{	
		super(aFrame, item);		
		setSize(840, 665);
		setHeaderTitle("Time Series Comparison");
		setTitle("Compare Different Versions of Time Series");
                setLocationRelativeTo(GnwGuiSettings.getInstance().getGnwGui().getFrame());

		selectPanel_ = new JPanel();
		selectPanel_.setBackground(Color.WHITE);
		selectPanel_.setLayout(new GridLayout(0,2));
		selectPanel_.setPreferredSize(new Dimension(0, 0));
                getContentPane().add(selectPanel_);

                numOfWindowPanel_ = new JPanel();
		numOfWindowPanel_.setBackground(Color.WHITE);
		numOfWindowPanel_.setLayout(new GridLayout(0,2));
		numOfWindowPanel_.setPreferredSize(new Dimension(0, 0));
                getContentPane().add(numOfWindowPanel_);

                ButtonGroup buttonGroup = new ButtonGroup();
                oneWin_ = new JRadioButton("Display in one window");
                oneWin_.setFont(oneWin_.getFont().deriveFont(Font.BOLD));
                twoWin_ = new JRadioButton("Display in two windows");
                twoWin_.setFont(oneWin_.getFont().deriveFont(Font.BOLD));
                oneWin_.setSelected(true);
                buttonGroup.add(oneWin_);
                buttonGroup.add(twoWin_);
                
                numOfWindowPanel_.add(oneWin_);
                numOfWindowPanel_.add(twoWin_);
                
                selectVersion_ = new JLabel("Select Version to compare");
                selectVersion_.setFont(selectVersion_.getFont().deriveFont(Font.BOLD));
                selectVersion_.setHorizontalAlignment(JLabel.CENTER);
                selectVersion_.setBorder(BorderFactory.createEmptyBorder(10,0,0,0));
                
                bOpen_ = new JButton("Open");
                bOpen_.setMargin(new Insets(2, 0, 2, 0));
		bOpen_.setPreferredSize(new Dimension(100, 80));
		
                
                selectPanel_.add(selectVersion_);
                selectPanel_.add(bOpen_);
                
                getContentPane().add(selectPanel_);
                getContentPane().add(numOfWindowPanel_);
	}
}
