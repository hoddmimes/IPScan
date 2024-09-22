import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class HostEntryDialog extends JFrame
{
    HostEntry mHostEntry;
    IPScan mIPScan;
    JButton jSaveBtn, jCancelBtn;
    JTextField jNameTxtFld;

    public HostEntryDialog(IPScan pIpScan, HostEntry pHostEntry ) {
        super("Host Entry " + pHostEntry.getIpAddress());
        mHostEntry = pHostEntry;
        mIPScan = pIpScan;
        init();
    }

    private void init() {
        JPanel tRootPanel = new JPanel( new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.LINE_START;
        c.weightx = 0.5;
        c.insets = new Insets( 10, 20, 0, 10);

        //------------------------------------------------------
        // Add IP Address
        JLabel jIpLabel = new JLabel("IP Address");
        c.gridy = 0; c.gridx = 0;
        tRootPanel.add( jIpLabel, c );

        // Add IP Field
        JTextField jIpTxtFld = new JTextField( mHostEntry.getIpAddress() );
        jIpTxtFld.setEditable(false);
        c.gridy = 0; c.gridx = 1;
        tRootPanel.add( jIpTxtFld, c );

        //------------------------------------------------------
        // Add host name label
        JLabel jNameLabel = new JLabel("Name");
        c.gridy = 1; c.gridx = 0;
        tRootPanel.add( jNameLabel, c );

        jNameTxtFld = new JTextField( 15);
        jNameTxtFld.setText( mHostEntry.getIpName());
        jNameTxtFld.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (mIPScan == null) {
                    System.out.println("Set Custom Name IP: " + mHostEntry.getIpAddress() + " name: " + jNameTxtFld.getText());
                }
                jSaveBtn.setEnabled(true);
            }
        });
        //jNameTxtFld.setBounds(0,0,220, 24);
        jNameTxtFld.setEditable(true);
        c.gridy = 1;  c.gridx = 1;
        tRootPanel.add( jNameTxtFld, c );

        //------------------------------------------------------
        // Add IP Address
        JLabel jMacLabel = new JLabel("MAC");
        c.gridy = 2; c.gridx = 0;
        tRootPanel.add( jMacLabel, c );

        // Add IP Field
        JTextField jMacTxtFld = new JTextField( mHostEntry.getMacAddress());
        jMacTxtFld.setEditable(false);
        c.gridy = 2; c.gridx = 1;
        tRootPanel.add( jMacTxtFld, c );
        this.setContentPane( tRootPanel );

        //------------------------------------------------------'
        // Add IP Address
        c.insets.bottom = 20;

        JLabel jManufactorLabel = new JLabel("Manufactor");
        c.gridy = 3; c.gridx = 0;
        tRootPanel.add( jManufactorLabel, c );

        // Add IP Field
        JTextField jManufactorTxtFld = new JTextField( mHostEntry.getManufactor());
        jManufactorTxtFld.setEditable(false);
        c.gridy = 3; c.gridx = 1;
       ;
        tRootPanel.add( jManufactorTxtFld, c );




        //------------------------------------------------------
        JPanel jBtnPanel = new JPanel(new FlowLayout( FlowLayout.CENTER, 30, 0));
        //jBtnPanel.setBackground( new Color(0xbbfaf8));
        jSaveBtn = new JButton("Save");
        jSaveBtn.setPreferredSize( new Dimension(80,20));
        jSaveBtn.setEnabled( false );
        jSaveBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (mIPScan == null) {
                    System.out.println("Save custom name IP: " + mHostEntry.getIpName() + " new custom name: " + jNameTxtFld.getText());
                } else {
                    mIPScan.saveCustomName( mHostEntry.getIpAddress(), jNameTxtFld.getText());
                }
            }
        });
        jBtnPanel.add( jSaveBtn );

        jCancelBtn = new JButton("Cancel");
        jCancelBtn.setEnabled( true );
        jCancelBtn.setPreferredSize( new Dimension(80,20));
        jCancelBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                HostEntryDialog.this.dispose();
            }
        });
        jBtnPanel.add( jCancelBtn );

        c.gridy = 4; c.gridx = 0;
        c.gridwidth = 2;
        tRootPanel.add( jBtnPanel, c );

        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        int w = this.getSize().width;
        int h = this.getSize().height;
        int x = (dim.width-w)/3;
        int y = (dim.height-h)/3;

        // Move the window
        this.setLocation(x, y);


        //tRootPanel.setBackground( new Color(0xbbfaf8));
        this.setContentPane( tRootPanel );
    }





    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        } catch (Exception ex) {
            System.out.println( ex.getMessage());
        }
        HostEntry he = new HostEntry("192.168.42.12");
        he.setMacAddress("ab:04:e6:1a:67:fe");
        he.setIpName("karma");
        he.setManufactor("Dell Inc.");

        HostEntryDialog thisClass = new HostEntryDialog(null, he);
        thisClass.pack();
        thisClass.setVisible(true);

    }
}
