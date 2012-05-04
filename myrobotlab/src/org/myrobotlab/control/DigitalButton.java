package org.myrobotlab.control;

import javax.swing.ImageIcon;
import javax.swing.JButton;

public class DigitalButton extends JButton{

	private static final long serialVersionUID = 1L;
	//public int ID = -1;
	public final Object parent;
	ImageIcon offIcon = null;
	ImageIcon onIcon = null;
	int type = -1;
	
	public DigitalButton(Object parent, ImageIcon offIcon, ImageIcon onIcon, int type) 
	{
		super();

		this.parent = parent;
		this.type = type;
		this.onIcon = onIcon;
		this.offIcon = offIcon;
		
		// image button properties
		setOpaque(false);
		setBorderPainted(false);
		setContentAreaFilled(false);		
		//setIcon(this.offIcon);
	}

}
