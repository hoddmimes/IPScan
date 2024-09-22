import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class LocalInterfaces
{
    private List<NetwrkInterface> mNetwrkInterfaces;


    LocalInterfaces() {
        scan();
    }

    boolean isLocalAddress( String pIpAddr ) {
        for( NetwrkInterface nif : mNetwrkInterfaces) {
            if (nif.mIpAddress.contentEquals( pIpAddr)) {
                return true;
            }
        }
        return false;
    }

    private void scan() {
        try {
            mNetwrkInterfaces = new ArrayList<>();
            Enumeration<NetworkInterface> tNics = NetworkInterface.getNetworkInterfaces();
            while (tNics.hasMoreElements()) {
                NetworkInterface tNetIf = tNics.nextElement();
                if ((ip4Address( tNetIf ) != null) && (!tNetIf.isLoopback())) {
                    mNetwrkInterfaces.add( new NetwrkInterface( tNetIf.getName(), ip4Address( tNetIf ), toHwAddress( tNetIf.getHardwareAddress() )));
                }
            }
        } catch (
                SocketException e) {
            e.printStackTrace();
        }
    }

    String getLocalSubNet() {
        String tIpAddress = mNetwrkInterfaces.get(0).mIpAddress;
        int tIdx = tIpAddress.lastIndexOf(".");
        String tSubnet = tIpAddress.substring(0,tIdx + 1 )+ "1";
        return tSubnet;
    }

    String getMacAddress( String pIpAddress ) {
        for (int i = 0; i < mNetwrkInterfaces.size(); i++) {
            if (mNetwrkInterfaces.get(i).mIpAddress.contentEquals( pIpAddress )) {
                return mNetwrkInterfaces.get(i).mHwAddress;
            }
        }
        return null;
    }

    private String toHwAddress( byte[] pHwBytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < pHwBytes.length; i++) {
            sb.append(String.format("%02X", (int) (pHwBytes[i] & 0xFF)) + ":");
        }
        return sb.toString().substring(0,sb.toString().length() - 1);
    }


    private String ip4Address(  NetworkInterface pNetIf ) {
        Enumeration<InetAddress> tAssignedIps = pNetIf.getInetAddresses();
        while (tAssignedIps.hasMoreElements()) {
            InetAddress tAddress = tAssignedIps.nextElement();
            if (tAddress instanceof Inet4Address) {
               return ((Inet4Address) tAddress).getHostAddress();
            }
        }
        return null;
    }


    void test() {
        dump();
    }

    class NetwrkInterface
    {
        String mName;
        String mIpAddress;
        String mHwAddress;

        NetwrkInterface( String pName, String pIpAddr, String pHwAddr ) {
            mName = pName;
            mIpAddress = pIpAddr;
            mHwAddress = pHwAddr;
        }

        public String toString() {
            return " name: " + this.mName + " ip addr: " + this.mIpAddress + " hw addr: " + this.mHwAddress ;
        }
    }

    public void dump() {
        for (int i = 0; i < mNetwrkInterfaces.size(); i++) {
            System.out.println(mNetwrkInterfaces.get(i).toString());
        }
    }

    public static void main(String[] args) {
        LocalInterfaces t = new LocalInterfaces();
        t.test();
    }
}
