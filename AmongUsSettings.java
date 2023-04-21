import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class AmongUsSettings {

    String serverIP;
    int ipPORT;
    double playerSpeed;

    public AmongUsSettings() {
    }

    public AmongUsSettings(String serverIP, int ipPORT, double playerSpeed) {
        this.serverIP = serverIP;
        this.ipPORT = ipPORT;
        this.playerSpeed = playerSpeed;
    }

    @XmlElement
    public void setServerIP(String serverIP) {
        this.serverIP = serverIP;
    }

    @XmlElement
    public void setIpPORT(int ipPORT) {
        this.ipPORT = ipPORT;
    }

    @XmlElement
    public void setPlayerSpeed(double playerSpeed) {
        this.playerSpeed = playerSpeed;
    }

    public String getServerIP() {
        return serverIP;
    }

    public int getIpPORT() {
        return ipPORT;
    }

    public double getPlayerSpeed() {
        return playerSpeed;
    }

    @Override
    public String toString() {
        return "AmongUsSettings [serverIP=" + serverIP + ", ipPORT=" + ipPORT + ", playerSpeed=" + playerSpeed + "]";
    }

}
