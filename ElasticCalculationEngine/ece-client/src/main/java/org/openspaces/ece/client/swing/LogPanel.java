package org.openspaces.ece.client.swing;

import org.openspaces.ece.client.ClientLogger;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Formatter;

import javax.swing.*;

public class LogPanel extends JPanel implements ClientLogger {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7963564052497318169L;
	JTextArea txtLog;

	public LogPanel() {
		this(BorderLayout.SOUTH);
	}

	/**
	 * Create the panel.
     * @param controlLocation the location of the control panel
     */
	public LogPanel(String controlLocation) {
		setLayout(new BorderLayout(0, 0));

		JPanel panel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		add(panel, controlLocation);

		JButton btnClearLog = new JButton("Clear");
		btnClearLog.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				txtLog.setText("");
			}
		});
		panel.add(btnClearLog);

		JScrollPane scrollPane = new JScrollPane();
		add(scrollPane, BorderLayout.CENTER);

		txtLog = new JTextArea();
		txtLog.setFont(new Font("Monospaced", Font.PLAIN, 11));
		scrollPane.setViewportView(txtLog);

	}

	@Override
	public void log(final String format, final Object... args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				StringBuilder output = new StringBuilder();
				Formatter formatter = new Formatter(output);
				formatter.format(format, args);
				txtLog.append(output.toString()
						+ System.getProperty("line.separator"));
                txtLog.invalidate();
			}
		});
	}

}
