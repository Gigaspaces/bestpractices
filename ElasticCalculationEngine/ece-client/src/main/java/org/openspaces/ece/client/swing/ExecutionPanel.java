package org.openspaces.ece.client.swing;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import org.openspaces.ece.client.ClientLogger;
import org.openspaces.ece.client.ContainsAdmin;
import org.openspaces.ece.client.ECEClient;
import org.openspaces.ece.client.builders.ClientBuilder;
import org.openspaces.ece.client.clients.NoWorkersAvailableException;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ExecutionPanel extends JPanel {
    ClientLogger logger;
    ContainsAdmin containsAdmin;
    String group;
    String locator;

    /**
     *
     */
    private static final long serialVersionUID = -1006808632768911289L;
    private JTextField txtTradeCount;
    private JTextField txtThreadCount;
    private JTextField txtIterations;

    public ExecutionPanel(ContainsAdmin admin, final ClientLogger logger, final String group,
                          final String locator) {
        this.containsAdmin = admin;
        this.logger = logger;

        setLayout(new FormLayout(new ColumnSpec[]{ColumnSpec.decode("27px"),
                ColumnSpec.decode("117px"),
                FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
                ColumnSpec.decode("86px:grow"),
                FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
                ColumnSpec.decode("86px"),
                FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
                ColumnSpec.decode("92px:grow"),}, new RowSpec[]{
                FormFactory.LINE_GAP_ROWSPEC, RowSpec.decode("23px"),
                FormFactory.LINE_GAP_ROWSPEC, RowSpec.decode("20px"),
                FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,}));

        JLabel lblTaskExecution = new JLabel("Task Execution");
        lblTaskExecution.setHorizontalAlignment(SwingConstants.RIGHT);
        add(lblTaskExecution, "2, 2");

        final JCheckBox chckbxTaskExecution = new JCheckBox();
        chckbxTaskExecution.setText("Executor");
        add(chckbxTaskExecution, "4, 2, 3, 1, left, default");

        JLabel lblTradeCount = new JLabel("Trade Count");
        add(lblTradeCount, "2, 4, right, default");

        txtTradeCount = new JTextField();
        txtTradeCount.setText("100");
        add(txtTradeCount, "4, 4, fill, default");
        txtTradeCount.setColumns(10);

        JLabel lblIterations = new JLabel("Iterations");
        lblIterations.setHorizontalAlignment(SwingConstants.RIGHT);
        add(lblIterations, "6, 4, right, default");

        txtIterations = new JTextField();
        txtIterations.setText("4");
        add(txtIterations, "8, 4, fill, default");
        txtIterations.setColumns(10);

        JButton btnRun = new JButton("Run");
        btnRun.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Thread() {
                    public void run() {
                        ECEClient eceClient = new ClientBuilder()
                                .type(chckbxTaskExecution.isSelected()?"executor":"masterworker")
                                .group(group)
                                .locator(locator)
                                .trades(Integer.parseInt(txtTradeCount.getText()))
                                .iterations(Integer.parseInt(txtIterations.getText()))
                                .admin(containsAdmin)
                                .logger(logger)
                                .spaceUrl("jini://*/*/ece-datagrid")
                                .build();
                        try {
                            eceClient.init();
                        } catch (NoWorkersAvailableException e1) {
                            e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                        }

                        if (eceClient.isValid()) {
                            eceClient.issueTrades();
                        }
                    }
                }.start();
            }
        });
        add(btnRun, "8, 6");
    }

}
