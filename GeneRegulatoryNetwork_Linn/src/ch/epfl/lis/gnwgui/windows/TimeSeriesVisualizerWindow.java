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
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.util.logging.Logger;
import javax.swing.JPanel;


/**
 * 
 * 
 * @author Thomas Schaffter (firstname.name@gmail.com)
 */
public class TimeSeriesVisualizerWindow extends GenericWindow
{	
	/** Serialization */
	private static final long serialVersionUID = 1L;
	
	protected JPanel graphPanel_;
		
    /** Logger for this class */
	@SuppressWarnings("unused")
	private static Logger log_ = Logger.getLogger(TimeSeriesVisualizerWindow.class.getName());
	
	// ----------------------------------------------------------------------------
	// PUBLIC METHODS
	
	public TimeSeriesVisualizerWindow(Frame aFrame)
	{	
		super(aFrame, false);		
                setSize(840, 665);
		setHeaderTitle("Time Series Representation");
		setTitle("Visualization");
                setLocationRelativeTo(GnwGuiSettings.getInstance().getGnwGui().getFrame());

		graphPanel_ = new JPanel();
		graphPanel_.setBackground(Color.WHITE);
		graphPanel_.setLayout(new BorderLayout());
		graphPanel_.setPreferredSize(new Dimension(0, 0));
                getContentPane().add(graphPanel_);

                
	}
}
