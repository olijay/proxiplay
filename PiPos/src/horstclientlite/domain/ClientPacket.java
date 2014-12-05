/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package horstclientlite.domain;

import horstclientlite.PacketParser;
import static horstclientlite.PacketParser.MAC_LEN;
import static horstclientlite.PacketParser.MAX_ESSID_LEN;
import horstclientlite.PacketParser.PacketFilterType;
import horstclientlite.PacketParser.WlanMode;
import static horstclientlite.PacketParser.parseMac;
import static horstclientlite.PacketParser.readInt;
import static horstclientlite.PacketParser.readLong;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author Gof
 */
public class ClientPacket implements Packet {

    private final String clientMac;
    private final String bssidMac;
    private final int signalStrength;
    private final int noiseStrength;
    private final int channel;
    private String essid;
    private final WlanMode wlanMode;
    
    public ClientPacket(String cMac, String bMac, int signal, int noise, int channel, String essid, WlanMode wlanMode) {
        this.clientMac = cMac;
        this.bssidMac = bMac;
        this.signalStrength = signal;
        this.noiseStrength = noise;
        this.channel = channel;
        this.essid = essid;
        this.wlanMode = wlanMode;
    }
    
    public static ClientPacket parse(InputStream in) throws IOException {
        int version = PacketParser.readByte(in);
        int pkgTypes = readInt(in);

        int signal = readInt(in);
        int noise = readInt(in);
        int snr = readInt(in);
        int rate = readInt(in);
        int freq = readInt(in);
        int channel = PacketParser.readByte(in);
        int flags = readInt(in);

        int packetLength = readInt(in);
        int frameControlField = readInt(in);

        byte[] src = new byte[MAC_LEN];
        PacketParser.readBytes(in, src);

        byte[] dst = new byte[MAC_LEN];
        PacketParser.readBytes(in, dst);

        byte[] bssid = new byte[MAC_LEN];
        PacketParser.readBytes(in, bssid);

        byte[] essid = new byte[MAX_ESSID_LEN];
        PacketParser.readBytes(in, essid);

        long beaconTimestamp = readLong(in);

        int beaconInterval = readInt(in);

        int wlanMode = readInt(in);

        int wlanChannel = PacketParser.readByte(in);

        int qosClass = PacketParser.readByte(in);

        int navDuration = readInt(in);

        int sequenceNumber = readInt(in);

        int wlanFlags = readInt(in);

        int ipSrc = readInt(in);
        int ipDst = readInt(in);
        int tcpUdpPort = readInt(in);
        int olsrType = readInt(in);
        int olsrNeighbor = readInt(in);
        int olsrTc = readInt(in);

        int rateIndex = PacketParser.readByte(in);
        int rateFlags = PacketParser.readByte(in);

        //long fix = rate + rateFlags + rateIndex + ipDst + ipSrc + olsrTc + olsrType + olsrNeighbor + tcpUdpPort + wlanChannel + wlanFlags + wlanMode + sequenceNumber + beaconInterval + beaconTimestamp + channel + navDuration + qosClass + snr + freq + flags + version + pkgTypes + packetLength + frameControlField;
        
        String clientMac = parseMac(src);
        
        String essidS = parseString(essid);
        
        String bssidMac = parseMac(bssid);
        
        WlanMode mode = WlanMode.fromValue(wlanMode);

        return new ClientPacket(clientMac, bssidMac, signal, noise, channel, essidS, mode);
    }

    /**
     * @return the clientMac
     */
    public String getClientMac() {
        return clientMac;
    }

    /**
     * @return the bssidMac
     */
    public String getBssidMac() {
        return bssidMac;
    }

    /**
     * @return the signalStrength
     */
    public int getSignalStrength() {
        return signalStrength;
    }

    /**
     * @return the noiseStrength
     */
    public int getNoiseStrength() {
        return noiseStrength;
    }

    @Override
    public byte[] toBytes() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * @return the channel
     */
    public int getChannel() {
        return channel;
    }
    
    @Override
    public String toString() {
        return "Client [Mac: "+this.clientMac+"] - [Bssid: "+this.bssidMac+"] - [Channel: "+this.channel+"] - [Essid: "+this.essid+"] - [Signal: "+this.signalStrength+"] - [Mode: "+this.wlanMode+"]";
    }

    public String getEssid() {
        return essid;
    }

    public boolean isBeacon() {
        if(this.getBssidMac() == null || this.getClientMac() == null) {
            return false;
        }
        
        if(!this.getBssidMac().equalsIgnoreCase("00:00:00:00:00:00")) {
            return this.getBssidMac().equalsIgnoreCase(this.getClientMac());
        }
        
        return false;
    }
    
    private static String parseString(byte[] essid) {
        StringBuilder sb = new StringBuilder();
        for(byte b : essid) {
            if(b == 0) {
                continue;
            }
            char c = (char)b;
            sb.append(c);
        }
        
        return sb.toString();
    }

    public void setEssid(String essid) {
        this.essid = essid;
    }

    /**
     * @return the wlanMode
     */
    public WlanMode getWlanMode() {
        return wlanMode;
    }
}
