package org.openspaces.wan.demo.client;

import com.beust.jcommander.JCommander;
import com.j_spaces.core.IJSpace;
import org.openspaces.core.GigaSpaceConfigurer;
import org.openspaces.core.space.UrlSpaceConfigurer;

import javax.swing.*;
import java.awt.*;

public class SwingClient {

    private JFrame frame;
    private Configuration configuration;
    private JTextField txtLookupGroup;
    private JTable valueTable;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        final Configuration configuration = new Configuration();
        new JCommander(configuration, args);
        UrlSpaceConfigurer configurer = new UrlSpaceConfigurer(
                "jini://*/*/wanSpace" + configuration.getLookupGroup())
                .lookupGroups(configuration
                        .getLookupGroup());
        IJSpace ijSpace = configurer.space();
        configuration.setSpace(new GigaSpaceConfigurer(ijSpace).gigaSpace());
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    SwingClient window = new SwingClient(configuration);
                    window.frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the application.
     *
     * @wbp.parser.entryPoint
     */
    public SwingClient() {
        initialize();
    }

    public SwingClient(Configuration configuration) {
        this.configuration = configuration;
        initialize();
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        frame = new JFrame();
        frame.setBounds(100, 100, 450, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        frame.getContentPane().add(panel, BorderLayout.NORTH);

        JLabel lblLookupGroup = new JLabel("Lookup Group:");
        panel.add(lblLookupGroup);

        txtLookupGroup = new JTextField(configuration.getLookupGroup());
        txtLookupGroup.setEditable(false);
        panel.add(txtLookupGroup);
        txtLookupGroup.setColumns(10);

        valueTable = new JTable();
        valueTable.setModel(new WANDemoTableModel(configuration.getSpace()));
        JScrollPane scrollPane = new JScrollPane(valueTable);
        frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
    }
}
