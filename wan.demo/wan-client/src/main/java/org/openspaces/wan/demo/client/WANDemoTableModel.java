package org.openspaces.wan.demo.client;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.table.AbstractTableModel;

import org.openspaces.core.GigaSpace;
import org.openspaces.wan.demo.model.WANDemoEntry;

import com.gigaspaces.internal.backport.java.util.Arrays;

public class WANDemoTableModel extends AbstractTableModel {
	String[] columnNames = { "Name", "Value" };
	Class<?>[] columnTypes = { String.class, Double.class };

	@Override
	public String getColumnName(int column) {
		return columnNames[column];
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return columnTypes[columnIndex];
	}

	private static final long serialVersionUID = 4030684233329296006L;
	private GigaSpace space;
	private final ScheduledExecutorService scheduler = Executors
			.newScheduledThreadPool(1);

	WANDemoEntry entries[];

	public WANDemoTableModel(final GigaSpace gigaspace) {
		this.space = gigaspace;
		scheduler.scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				entries = space.readMultiple(new WANDemoEntry(),
						Integer.MAX_VALUE);
				Arrays.sort(entries);
				WANDemoTableModel.this.fireTableDataChanged();
			}
		}, 0, 1, TimeUnit.SECONDS);
	}

	@Override
	public int getRowCount() {
		if (entries == null) {
			return 0;
		}
		return entries.length;
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		WANDemoEntry element = entries[rowIndex];
		return columnIndex == 0 ? element.getName() : element.getValue();
	}
}
