import table.TableAttribute;

public class HostEntry
{
        private String  ipAddress;
        private String  ipName;
        private String  macAddress;
        private String  manufactor;
        private boolean hasHttp;

        HostEntry( String pIpAddress ) {
            this.setIpAddress(pIpAddress);
        }

        public String toString() {
            return "ip: " + getIpAddress() + " mac: " + getMacAddress() + " name: " + getIpName()  + " manufacture: " + getManufactor() + " has HTTP: " + this.hasHttp;
        }

        @TableAttribute( header = "IP Address", column = 1, width = 140)
        public String getIpAddress() {
            return ipAddress;
        }

        public void setHasHttp( boolean pFlag ) {
            hasHttp = pFlag;
        }

        public boolean hasHttp() {
            return this.hasHttp;
        }

        public void setIpAddress(String ipAddress) {
            this.ipAddress = ipAddress;
        }

        @TableAttribute( header = "Name", column = 2, width = 220)
        public String getIpName() {
            return ipName;
        }

        public void setIpName(String ipName) {
            this.ipName = ipName;
        }

        @TableAttribute( header = "Mac", column = 3, width = 140)
        public String getMacAddress() {
            return macAddress;
        }

        public void setMacAddress(String macAddress) {
            this.macAddress = macAddress;
        }

        @TableAttribute( header = "Manufacture", column = 4, width = 286)
        public String getManufactor() {
            return manufactor;
        }

        public void setManufactor(String manufactor) {
            this.manufactor = manufactor;
        }
}
