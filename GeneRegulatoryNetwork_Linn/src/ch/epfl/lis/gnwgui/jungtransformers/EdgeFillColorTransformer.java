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

package ch.epfl.lis.gnwgui.jungtransformers;

import java.awt.Color;
import java.awt.Paint;
import java.util.logging.Logger;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.visualization.picking.PickedInfo;

/** This transformer defines the inside color of the edges belonging to the graph visualizations.
 * 
 * @author Thomas Schaffter (firstname.name@gmail.com)
 *
 * @param <E> Edge
 * 
 */
public class EdgeFillColorTransformer<E> implements Transformer<E,Paint> {
	
    /** Pick info */
    private PickedInfo<E> pi_ = null;
	
    /** Normal color of the graph vertices. */
    private Color defaultEdgeColor = null;
    /** Color of the picked vertex. */
    private Color pickedColor_ = null;
    
    /** Logger for this class */
    @SuppressWarnings("unused")
	private Logger log_ = Logger.getLogger(EdgeFillColorTransformer.class.getName());
    
    
    /**
     * Constructor
     * @param pi
     */
    public EdgeFillColorTransformer(PickedInfo<E> pi) {
        pi_ = pi;
        defaultEdgeColor = Color.RED;
        pickedColor_ = Color.YELLOW;
    }
    
    
    
    /**
     * Return a color used to paint the nodes. Different colors are returned following
     * particular cases.
     */
    public Paint transform(E e) {
    	if (pi_.isPicked(e))
    		return pickedColor_;
    	
    	return defaultEdgeColor;
    }
}
