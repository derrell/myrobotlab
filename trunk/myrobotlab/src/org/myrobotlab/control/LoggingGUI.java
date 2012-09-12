package org.myrobotlab.control;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.myrobotlab.framework.Message;
import org.myrobotlab.service.interfaces.GUI;

public class LoggingGUI extends ServiceGUI implements ActionListener {

	static final long serialVersionUID = 1L;

	JTextArea log = new JTextArea(20, 40);
	ImageButton clearButton;
	
	public LoggingGUI(final String boundServiceName, final GUI myService) {
		super(boundServiceName, myService);
	}
	
	public void init() {
		display.setLayout(new BorderLayout());

		clearButton = new ImageButton("Logging", "clear", this);
		JPanel toolbar = new JPanel(new BorderLayout());
		toolbar.add(clearButton, BorderLayout.EAST);
		display.add(toolbar, BorderLayout.PAGE_START);
		
		log.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(log);
		
		display.add(scrollPane, BorderLayout.CENTER);
	}

	public void log (Message m)
	{
		
		StringBuffer data = null;
		
		if (m.data != null)
		{
			data = new StringBuffer();
			for (int i = 0; i < m.data.length; ++i)
			{
				data.append(m.data[i]);
				if (m.data.length > 1)
				{
					data.append(" ");
				}
			}
		}
		
		log.append(m.sender + "." + m.sendingMethod + "->" + data + "\n");
		
		log.setCaretPosition(log.getDocument().getLength()); // FIXME - do it the new Java 1.6 way
	}
	
	@Override
	public void attachGUI() {
		subscribe("log", "log", Message.class);		
	}

	@Override
	public void detachGUI() {
		unsubscribe("log", "log", Message.class);
	}

	@Override
	public void actionPerformed(ActionEvent action) {
		Object o = action.getSource();
		if (o == clearButton)
		{
			log.setText("");
		}
		
	}

}