import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import table.ModelRenderCallback;
import table.Table;
import table.TableCallbackInterface;
import table.TableModel;
import java.net.InetAddress;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.border.EmptyBorder;


/**
 * https://www.macvendorlookup.com/api/v2/{MAC_Address}
 */
public class IPScan  extends JFrame implements TableCallbackInterface, ModelRenderCallback {
    Pattern ADDR_PATTERN = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)\\.(\\d+)");
    Pattern MAC_MAC_ADDRESS = Pattern.compile("([^ ]+) \\((\\d+\\.\\d+\\.\\d+\\.\\d+)\\) at (([0-9A-Fa-f]{1,2}[:.]?){5}[0-9A-Fa-f]{1,2})");
    Pattern LINUX_MAC_ADDRESS = Pattern.compile("([^ ]+).+(([[0-9A-Fa-f]]{2}[:.-]?){5}[[0-9A-Fa-f]]{2})");
    LocalInterfaces mLocalInterfaces;
    Caches          mCaches;
    int mReadTimeout = 150; // milliseconds

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

                String tIpAddressCalibrated = validateStartingAddress( jNetworkTxtFld.getText());
                if (tIpAddressCalibrated == null) {
                    JOptionPane.showMessageDialog(IPScan.this,
                            "Invalid IP address \"" + jNetworkTxtFld.getText() + "\"",
                            "Invalid IP Address",
                            JOptionPane.WARNING_MESSAGE);
                    jRescanButton.setEnabled( true );
                    return;
                }
                SwingUtilities.invokeLater( new RunableRescan( 0,5, ipStringToInt(tIpAddressCalibrated), IPScan.this ));
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





    void scan(int pOffset, int pBatchSize, long pBinIpStartAddress ) {

        int i = pOffset;
        while ((i < 255) && (i < (pOffset + pBatchSize))) {
            String s = longToIpString(pBinIpStartAddress + i);
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
            i++;
        }
        if (i == 255) {
            mCaches.saveCaches();
            jRescanButton.setEnabled(true);
        } else {
            SwingUtilities.invokeLater( new RunableRescan( i, pBatchSize, pBinIpStartAddress, this ));
        }
    }

    private String fillMacAddr( String pMac ) {
        StringBuffer sb = new StringBuffer();
        String arr[] = pMac.split(":");
        for( String a : arr ) {
            if (a.length() == 1) {
                sb.append("0");
            }
            sb.append(a + ":");
        }
        String str= sb.substring(0, sb.length() -  1);
        return str;
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
        boolean tMacOs = System.getProperty("os.name").startsWith("Mac");

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
                Matcher m = tMacOs ? MAC_MAC_ADDRESS.matcher(s) : LINUX_MAC_ADDRESS.matcher(s);
                if (m.find()) {
                    tFound= true;
                    if (tMacOs) {
                        pEntry.setMacAddress(fillMacAddr(m.group(3)));
                        mCaches.mMacAddresses.put(pEntry.getIpAddress(), fillMacAddr(m.group(3)));
                        if (!m.group(1).contentEquals("?")) {
                            mCaches.mIpNames.put(pEntry.getMacAddress(), m.group(1));
                        } else {
                            mCaches.mIpNames.put(pEntry.getMacAddress(), m.group(2));
                        }
                    } else {
                        pEntry.setMacAddress(m.group(2));
                        mCaches.mMacAddresses.put(pEntry.getIpAddress(), fillMacAddr(m.group(2)));
                        mCaches.mIpNames.put(pEntry.getMacAddress(), m.group(1));
                    }
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


    private boolean timedReader(InputStream pInputStream, int pTimeout, String ... pSeachStrings) throws IOException
    {
        long tStartTime = System.currentTimeMillis();
        int tLastLength = 0;

        ByteBuffer bb = ByteBuffer.allocate(2048);
        do {
            while (pInputStream.available() > 0) {
                int chr = pInputStream.read();
                if (chr < 0) { //EOF ?
                    return false; // failed to locate search string
                }
                bb.put((byte) (chr & 0xFF)); //save byte
            }
            if (tLastLength < bb.position()) { // have we new input?
                tLastLength = bb.position(); // update last known position
                String s = new String(Arrays.copyOfRange(bb.array(), 0, bb.position()));
                for( String ss : pSeachStrings) {
                    if (s.indexOf(ss) >= 0) {
                        return true;
                    }
                }
            }
            try { Thread.sleep(20L); } catch (InterruptedException ie) {}
        } while ( (System.currentTimeMillis() - tStartTime) < pTimeout);

        return false;
    }

    private boolean ping(String pIpAddress) {
        String s;
        Process p;
        boolean found = false;
        boolean  tMacOs = System.getProperty("os.name").startsWith("Mac");


        try {
            String cmd = tMacOs ? "ping -n -c 1 -W 0.2 " : "ping -4 -n -c 1 -W 0.2 ";
            p = Runtime.getRuntime().exec(cmd + pIpAddress);
            found = timedReader( p.getInputStream(), mReadTimeout,
                    ("bytes from " + pIpAddress),
                    "1 packets received");
            //p.waitFor();
            //System.out.println ("exit: " + p.exitValue());
            p.destroy();
            return found;
        } catch (Exception e) {
            return false;
        }
    }

    private String reversedDNSLookup( String pIpAddress ) {
        try {
            InetAddress ia =  InetAddress.getByName(pIpAddress);
            return ia.getCanonicalHostName();
        }
        catch(Exception e) {
            System.out.println("DNS exception: " + e.getMessage());
            return pIpAddress;
        }
    }
    void getIpName( HostEntry pEntry ) {
        if (mCaches.mIpCustomNames.containsKey( pEntry.getIpAddress())) {
            pEntry.setIpName( mCaches.mIpCustomNames.get( pEntry.getIpAddress()));
            return;
        }

        if (mCaches.mIpNames.containsKey( pEntry.getMacAddress())) {
            pEntry.setIpName( mCaches.mIpNames.get( pEntry.getMacAddress()));
            return;
        }

        String tHostName = reversedDNSLookup(pEntry.getIpAddress());
        mCaches.mIpNames.put( pEntry.getMacAddress(), tHostName);
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
            huc.setConnectTimeout(500);
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
        thisClass.parseParameters( args);
        thisClass.mCaches = new Caches();
        thisClass.mLocalInterfaces = new LocalInterfaces();
        thisClass.jNetworkTxtFld.setText( thisClass.mLocalInterfaces.getLocalSubNet() );

        thisClass.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        thisClass.setVisible(true);
        thisClass.jRescanButton.setEnabled(false);


        String tIpAddressCalibrated = thisClass.validateStartingAddress( thisClass.jNetworkTxtFld.getText());
        if (tIpAddressCalibrated == null) {
            JOptionPane.showMessageDialog(thisClass,
                    "Invalid IP address \"" + thisClass.jNetworkTxtFld.getText() + "\"",
                    "Invalid IP Address",
                    JOptionPane.WARNING_MESSAGE);
            thisClass.jRescanButton.setEnabled( true );
            return;
        }


        thisClass.scan(0,255, thisClass.ipStringToInt(tIpAddressCalibrated));
    }

    private void parseParameters( String [] args ) {
        int i = 0;
        while( i < args.length){
            if (args[i].equalsIgnoreCase("-timeout")) {
                mReadTimeout = Integer.parseInt(args[i+1]);
                i++;
            }
            i++;
        }
    }

    public class RunableRescan implements Runnable{
        IPScan mScanner;
        int mOffset;
        int mBatchSize;
        long mBinStartAddress;

        public RunableRescan( int pOffset, int pBatchSize, long pBinStartAddress,  IPScan pScanner) {
            mScanner = pScanner;
            mOffset = pOffset;
            mBatchSize = pBatchSize;
            mBinStartAddress = pBinStartAddress;
        }

        public void run() {
            mScanner.scan( mOffset, mBatchSize, mBinStartAddress );
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