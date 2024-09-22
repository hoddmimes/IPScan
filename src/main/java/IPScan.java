import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import table.ModelRenderCallback;
import table.Table;
import table.TableCallbackInterface;
import table.TableModel;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.*;
import java.util.List;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.concurrent.RunnableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.border.EmptyBorder;


/**
 * https://www.macvendorlookup.com/api/v2/{MAC_Address}
 */
public class IPScan  extends JFrame implements TableCallbackInterface, ModelRenderCallback {
    Pattern ADDR_PATTERN = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)\\.(\\d+)");
    Pattern MAC_ADDRESS = Pattern.compile("([^ ]+).+(([[0-9A-Fa-f]]{2}[:.-]?){5}[[0-9A-Fa-f]]{2})");
    LocalInterfaces mLocalInterfaces;
    Caches          mCaches;

    // Swing variables
    JTextField jNetworkTxtFld;
    JCheckBox jResetCustomChkBox;
    JButton jResetButton;
    JButton jExitButton;
    JButton jRescanButton;
    TableModel<HostEntry> mTableModel;
    Table mTable;



    public IPScan() {
        initFrame();
    }


    void callWebBrowser( String pAddress ) {

        try {
            // Create Desktop object
            Desktop d = Desktop.getDesktop();

            // Browse a URL, for example www.facebook.com
            d.browse(new URI("https://" + pAddress));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    void saveCustomName( String pIpAddress, String pName)
    {
        mCaches.mIpCustomNames.put( pIpAddress, pName);
        mCaches.saveCaches();
        List<HostEntry> tHostEntries = mTableModel.getObjects();
        for( HostEntry he: tHostEntries) {
            if (he.getIpAddress().contentEquals( pIpAddress)) {
                he.setIpName( pName );
                mTableModel.fireTableDataChanged();
            }
        }
    }

    private void initFrame() {
        this.setSize(  new Dimension(800,600 ));

        JPanel tRoot = new JPanel( new BorderLayout());
        tRoot.add( createHeaderPanel(), BorderLayout.NORTH);
        tRoot.add( createTablePanel(), BorderLayout.CENTER);

        tRoot.add( createBottomPanel(), BorderLayout.SOUTH);





        this.setContentPane( tRoot );

        this.setTitle("IPScan");

        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        int w = this.getSize().width;
        int h = this.getSize().height;
        int x = (dim.width-w)/4;
        int y = (dim.height-h)/4;

        // Move the window
        this.setLocation(x, y);
    }

    JPanel createTestPanel() {
        JPanel tPanel = new JPanel( new FlowLayout( FlowLayout.CENTER) );
        tPanel.setBorder( new LineBorder( Color.BLACK, 2, true));

        tPanel.add( new JLabel("Test Panel"));
        return tPanel;

    }

    JPanel createBottomPanel() {
        JPanel tPanel = new JPanel( new BorderLayout() );
        tPanel.setBorder( new LineBorder( Color.BLACK, 2, true));

        JPanel jResetPanel = new JPanel( new FlowLayout( FlowLayout.CENTER));

        jResetCustomChkBox = new JCheckBox("Reset custom names");
        jResetCustomChkBox.setFont(new Font("Dialog", Font.PLAIN, 12));
        jResetPanel.add( jResetCustomChkBox );
        tPanel.add(jResetPanel, BorderLayout.WEST);


        jResetButton = new JButton("Reset Caches");
        jResetButton.setFont(new Font("Dialog", Font.BOLD, 12));
        jResetButton.setPreferredSize( new Dimension( 120, 20));
        jResetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mCaches.reset( jResetCustomChkBox.isSelected());
                JOptionPane.showMessageDialog(null, "IPScan caches being reset");
            }
        });
        jResetPanel.add( jResetButton );
        tPanel.add( jResetPanel, BorderLayout.WEST );



        JPanel jButtonPanel = new JPanel( new FlowLayout( FlowLayout.CENTER));
        jButtonPanel.setBounds(480,5, 350, 40 );

        jExitButton = new JButton("Exit");
        jExitButton.setFont(new Font("Dialog", Font.BOLD, 12));
        jResetButton.setPreferredSize( new Dimension( 120, 20));
        jExitButton.setPreferredSize( new Dimension( 90, 20));
        jExitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                IPScan.this.dispose();
            }
        });
        jButtonPanel.add( jExitButton );

        jRescanButton = new JButton("Rescan");
        jRescanButton.setFont(new Font("Dialog", Font.BOLD, 12));
        jRescanButton.setPreferredSize( new Dimension( 120, 20));
        jRescanButton.setPreferredSize( new Dimension( 90, 20));
        jRescanButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mTableModel.clear();
                jRescanButton.setEnabled(false);
                SwingUtilities.invokeLater( new RunableRescan( IPScan.this ));
            }
        });
        jButtonPanel.add( jRescanButton );
        tPanel.add( jButtonPanel, BorderLayout.EAST );
        return tPanel;
    }

    JPanel createTablePanel() {
        JPanel tPanel = new JPanel( new BorderLayout());
        tPanel.setBorder( new EmptyBorder(10,5,10,5));

        mTableModel = new TableModel( HostEntry.class );
        mTableModel.setRenderCallback( this );
        mTable = new Table( mTableModel, new Dimension(800,425), this );
        tPanel.add( mTable, BorderLayout.CENTER );
        return tPanel;
    }



    JPanel createHeaderPanel() {
        JPanel jPanel = new JPanel( null );
        jPanel.setPreferredSize( new Dimension(800,30));
        jPanel.setBorder( new LineBorder( Color.BLACK, 2, true));
        JLabel label = new JLabel("Network");
        label.setBounds(30, 5, 60,20);
        jPanel.add( label );

        jNetworkTxtFld = new JTextField();
        jNetworkTxtFld.setHorizontalAlignment(SwingConstants.CENTER);
        jNetworkTxtFld.setFont(new Font("Dialog", Font.PLAIN, 10));
        jNetworkTxtFld.setEditable(true);
        jNetworkTxtFld.setToolTipText("xxx.xxx.xxx.xxx");
        jNetworkTxtFld.setBounds(100, 5, 120, 20);
        jPanel.add(jNetworkTxtFld);

        return jPanel;
    }

    private void execute() {
        mLocalInterfaces = new LocalInterfaces();
        mCaches = new Caches();

        while (true) {
            scan(mLocalInterfaces.getLocalSubNet());
            try {
                Thread.sleep(15000);
            } catch (InterruptedException e) {
            }
        }
    }



    void scan(String pIpNetStartAddress ) {
        //jRescanButton.setEnabled( false );
        String tIpAddressCalibrated = validateStartingAddress( pIpNetStartAddress);
        if (tIpAddressCalibrated == null) {
            JOptionPane.showMessageDialog(this,
                    "Invalid IP address \"" + pIpNetStartAddress + "\"",
                    "Invalid IP Address",
                    JOptionPane.WARNING_MESSAGE);
            jRescanButton.setEnabled( true );
            return;
        }

        long ipAddr = ipStringToInt(tIpAddressCalibrated);
        for (int i = 0; i < 255; i++) {
            String s = longToIpString(ipAddr + i);
            if (ping(s)) {
                HostEntry tEntry = new HostEntry(s);
                tEntry.setHasHttp( checkHttp(s));
                if (arp(tEntry))  {
                    // Get MAC manufactor
                    macToManfacture(tEntry);
                } else {
                    tEntry.setMacAddress("--");
                    tEntry.setManufactor("--");
                }

                getIpName( tEntry );
                mTableModel.addEntry( tEntry );
                System.out.println(tEntry);
            }
        }
        mCaches.saveCaches();
        jRescanButton.setEnabled( true );
    }

    private String validateStartingAddress( String pAddress) {
        Pattern tAddressPattern = Pattern.compile("^(([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(\\.(?!$)|$)){4}$");
        Matcher m = tAddressPattern.matcher( pAddress );
        if (!m.find()) {
            return null;
        }
        return pAddress.substring(0, pAddress.lastIndexOf(".") + 1) + "1";
    }




    private long ipStringToInt(String pAddrString) {
        long ipNumber = 0;

        Matcher m = ADDR_PATTERN.matcher(pAddrString);
        if (m.matches()) {
            for (int i = 1; i <= 4; i++) {
                String s = m.group(i);
                ipNumber += (long) Integer.parseInt(m.group(i)) << (24 - (8 * (i - 1)));
            }
        }
        return ipNumber;
    }

    private String longToIpString(long pIpNumber) {
        return String.format("%d.%d.%d.%d",
                (pIpNumber >> 24) & 0xFF,  // Extract the first byte
                (pIpNumber >> 16) & 0xFF,  // Extract the second byte
                (pIpNumber >> 8) & 0xFF,   // Extract the third byte
                pIpNumber & 0xFF);         // Extract the fourth byte
    }





    private boolean arp(HostEntry pEntry ) {
        String s;
        Process p;
        String tMacAddr = null;

        // Is the mac in the cache?
        if (mCaches.mMacAddresses.containsKey( pEntry.getIpAddress())) {
            pEntry.setMacAddress(mCaches.mMacAddresses.get(pEntry.getIpAddress()));
            return true;
        }

        //Is the ip address a local address
        if (mLocalInterfaces.getMacAddress(pEntry.getIpAddress()) != null) {
            pEntry.setMacAddress(mLocalInterfaces.getMacAddress(pEntry.getIpAddress()));
            mCaches.mMacAddresses.put( pEntry.getIpAddress(), mLocalInterfaces.getMacAddress(pEntry.getIpAddress()));
            return true;
        }

        // Get the Mac from the Arp cache
        try {
            boolean tFound = false;
            p = Runtime.getRuntime().exec("arp  " + pEntry.getIpAddress());
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(p.getInputStream()));
            while ((s = br.readLine()) != null) {
                Matcher m = MAC_ADDRESS.matcher(s);
                if (m.find()) {
                    tFound= true;
                    pEntry.setMacAddress(m.group(2));
                    mCaches.mMacAddresses.put(pEntry.getIpAddress(), m.group(2));
                    mCaches.mIpNames.put(pEntry.getIpAddress(), m.group(1));
                  break;
                }
            }

            p.waitFor();
            //System.out.println ("exit: " + p.exitValue());
            p.destroy();
            return tFound;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean ping(String pIpAddress) {
        String s;
        Process p;
        boolean found = false;


        try {
            p = Runtime.getRuntime().exec("ping -4 -c 1 -W 0.1  " + pIpAddress);
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(p.getInputStream()));
            while ((s = br.readLine()) != null) {
                if (s.indexOf("bytes from " + pIpAddress) > 0) {
                    found = true;
                    break;
                }
            }

            p.waitFor();
            //System.out.println ("exit: " + p.exitValue());
            p.destroy();
            return found;
        } catch (Exception e) {
            return false;
        }
    }

    void getIpName( HostEntry pEntry ) {
        try {
            if (mCaches.mIpCustomNames.containsKey( pEntry.getIpAddress())) {
                pEntry.setIpName( mCaches.mIpCustomNames.get( pEntry.getIpAddress()));
                return;
            }

            if (mCaches.mIpNames.containsKey( pEntry.getIpAddress())) {
                pEntry.setIpName( mCaches.mIpNames.get( pEntry.getIpAddress()));
                return;
            }

            // Get InetAddress object for the provided IP address
            InetAddress inetAddress = InetAddress.getByName(pEntry.getIpAddress());

            // Get the hostname from the InetAddress object
            String hostname = inetAddress.getHostName();
            if (!hostname.contentEquals(pEntry.getIpAddress())) {
                pEntry.setIpName(hostname);
                mCaches.mIpNames.put( pEntry.getIpName(), hostname);
                return;
            }

        } catch (UnknownHostException e) {
            // Handle exception when the IP address cannot be resolved
            pEntry.setIpName(pEntry.getIpAddress());
        }
    }

    void macToManfacture( HostEntry pEntry ) {
        if (mCaches.mManufactors.containsKey(pEntry.getMacAddress())) {
            pEntry.setManufactor(mCaches.mManufactors.get(pEntry.getMacAddress()));
            return;
        }

        try {
            String url = "https://www.macvendorlookup.com/api/v2/" + pEntry.getMacAddress(); // Replace with your URL
            URL obj = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) obj.openConnection();

            // Set the request method to GET
            connection.setRequestMethod("GET");

            // Set the request headers if necessary
            connection.setRequestProperty("Accept", "application/json");

            // Get the response code
            int responseCode = connection.getResponseCode();
            //System.out.println("Response Code: " + responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) { // success
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // Print the JSON response
                String tResponse = response.toString();
                //System.out.println("ip address: " + pEntry.ipAddress + "mac: " + pEntry.macAddress + " response text: " + response.toString());
                JsonObject jResponse = JsonParser.parseString(tResponse).getAsJsonArray().get(0).getAsJsonObject();
                pEntry.setManufactor(jResponse.get("company").getAsString());
                mCaches.mManufactors.put(pEntry.getMacAddress(), pEntry.getManufactor());
            } else {

                System.out.println("GET request failed host entry " + pEntry);
                pEntry.setManufactor("unknown");
            }

        } catch (Exception e) {
            e.printStackTrace();
            pEntry.setManufactor("unknown");
        }
    }


    private boolean checkHttp( String pIpAddress ) {
        try {
            URL url = new URL("http://" + pIpAddress);
            HttpURLConnection huc = (HttpURLConnection) url.openConnection();
            int responseCode = huc.getResponseCode();
            if (responseCode != 404) {
                return true;
            }
            return false;
        }
        catch( Exception e) {
            //e.printStackTrace();
        }
        return false;
    }





    public static void main(String args[]) {
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        } catch (Exception ex) {
            System.out.println( ex.getMessage());
        }

        IPScan thisClass = new IPScan();
        thisClass.mCaches = new Caches();
        thisClass.mLocalInterfaces = new LocalInterfaces();
        thisClass.jNetworkTxtFld.setText( thisClass.mLocalInterfaces.getLocalSubNet() );

        thisClass.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        thisClass.setVisible(true);
        thisClass.jRescanButton.setEnabled(false);
        thisClass.scan(  thisClass.mLocalInterfaces.getLocalSubNet());
    }

    public class RunableRescan implements Runnable{
        IPScan mScanner;

        public RunableRescan( IPScan pScanner) {
            mScanner = pScanner;
        }

        public void run() {
            mScanner.scan( mScanner.jNetworkTxtFld.getText());
        }
    }

    @Override
    public void tableMouseButton2(Object pObject, int pRow, int pCol) {
        HostEntry he =  (HostEntry) pObject;
        if (he.hasHttp() && (pCol == 0)) {
            callWebBrowser( he.getIpAddress());
        }
    }

    @Override
    public void tableMouseClick(Object pObject, int pRow, int pCol) {

    }

    @Override
    public void tableMouseDoubleClick(Object pObject, int pRow, int pCol) {
        System.out.println("double click col: " + pCol + " row: " + pRow );
        HostEntryDialog hed = new HostEntryDialog( this, (HostEntry) pObject);
        hed.pack();
        hed.setVisible(true);
    }

    @Override
    public void tableCellRendererComponent(JLabel pCellRenderObject, JTable pTable, Object pValue, int pRow, int pCol) {
        HostEntry he = mTableModel.getObjects().get(pRow);
        if (mLocalInterfaces.isLocalAddress(he.getIpAddress())) {
            //pCellRenderObject.setFont( new Font("Arial", Font.BOLD, 12));
            if ((pCol == 0) && (he.hasHttp())) {
                pCellRenderObject.setForeground(Color.blue);
            } else {
                pCellRenderObject.setForeground(Color.red);
            }
        } else if ((pCol == 0) && (he.hasHttp())) {
            pCellRenderObject.setForeground(Color.blue);
        }
    }
}



