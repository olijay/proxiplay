/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package horstclientlite;

/**
 *
 * @author Gof
 */
public class Config {

    private static final String SETTINGS_FILE_PATH = "settings.xml";
    private static Config singleton;

    public static final Config singleton() {
        if (singleton == null) {
            singleton = new Config();
        }

        return singleton;
    }

    private String uniqueId = "abcdefghijklmnopqrstuvwxyz";
    private String sendUrl = "http://replacemewithsomethingusefull.dk";
    private int sendTimer = 500;

    private String host = "localhost";
    private int port = 4260;
    private int channel = 1;

    private String bssidFilter = "00:00:00:00:00:00";
    private String essidFilter = "";

    private boolean bssidFilterEnabled = false;
    private boolean essidFilterEnabled = false;

    private String installationId = "replaceme";

    private boolean debug = false;

    private Config() {
    }

    /**
     * @return the uniqueId
     */
    public String getUniqueId() {
        return uniqueId;
    }

    /**
     * @param uniqueId the uniqueId to set
     */
    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    /**
     * @return the sendUrl
     */
    public String getSendUrl() {
        return sendUrl;
    }

    /**
     * @param sendUrl the sendUrl to set
     */
    public void setSendUrl(String sendUrl) {
        this.sendUrl = sendUrl;
    }

    /**
     * @return the sendTimer
     */
    public int getSendTimer() {
        return sendTimer;
    }

    /**
     * @param sendTimer the sendTimer to set
     */
    public void setSendTimer(int sendTimer) {
        this.sendTimer = sendTimer;
    }

    /**
     * @return the host
     */
    public String getHost() {
        return host;
    }

    /**
     * @param host the host to set
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * @return the port
     */
    public int getPort() {
        return port;
    }

    /**
     * @param port the port to set
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * @return the channel
     */
    public int getChannel() {
        return channel;
    }

    /**
     * @param channel the channel to set
     */
    public void setChannel(int channel) {
        this.channel = channel;
    }

    /**
     * @return the installationId
     */
    public String getInstallationId() {
        return installationId;
    }

    /**
     * @param installationId the installationId to set
     */
    public void setInstallationId(String installationId) {
        this.installationId = installationId;
    }

    /**
     * @return the bssidFilter
     */
    public String getBssidFilter() {
        return bssidFilter;
    }

    /**
     * @param bssidFilter the bssidFilter to set
     */
    public void setBssidFilter(String bssidFilter) {
        this.bssidFilter = bssidFilter;
    }

    /**
     * @return the essidFilter
     */
    public String getEssidFilter() {
        return essidFilter;
    }

    /**
     * @param essidFilter the essidFilter to set
     */
    public void setEssidFilter(String essidFilter) {
        this.essidFilter = essidFilter;
    }

    /**
     * @return the bssidFilterEnabled
     */
    public boolean isBssidFilterEnabled() {
        return bssidFilterEnabled;
    }

    /**
     * @param bssidFilterEnabled the bssidFilterEnabled to set
     */
    public void setBssidFilterEnabled(boolean bssidFilterEnabled) {
        this.bssidFilterEnabled = bssidFilterEnabled;
    }

    /**
     * @return the essidFilterEnabled
     */
    public boolean isEssidFilterEnabled() {
        return essidFilterEnabled;
    }

    /**
     * @param essidFilterEnabled the essidFilterEnabled to set
     */
    public void setEssidFilterEnabled(boolean essidFilterEnabled) {
        this.essidFilterEnabled = essidFilterEnabled;
    }

    /**
     * @return the debug
     */
    public boolean isDebug() {
        return debug;
    }

    /**
     * @param debug the debug to set
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Config\n");
        
        sb.append("\tEssid: ").append(this.getEssidFilter()).append(" [").append(this.isEssidFilterEnabled()).append("]\n");
        sb.append("\tBssid: ").append(this.getBssidFilter()).append(" [").append(this.isBssidFilterEnabled()).append("]\n");
        sb.append("\tChannel: ").append(this.getChannel()).append("\n");
        sb.append("\tSendTimer: ").append(this.getSendTimer()).append("\n");
        
        return sb.toString();
    }
}
