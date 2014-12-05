/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package horstclientlite.domain;

import horstclientlite.PacketParser;
import static horstclientlite.PacketParser.readInt;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 *
 * @author Gof
 */
public class ChannelConfigPacket implements Packet {
    private boolean changeChannel;
    private int upper;
    private int channel;
    private int dwellTime;
    
    public ChannelConfigPacket(boolean change, int upper, int channel, int time) {
        this.changeChannel = change;
        this.upper = upper;
        this.channel = channel;
        this.dwellTime = time;
    }

    public static Packet parse(InputStream in) throws IOException {
        boolean changeChannel = PacketParser.readByte(in) == 1;
        int upperChannel = PacketParser.readByte(in);
        int channel = PacketParser.readByte(in) + 1;
        int dwellTime = readInt(in) / 1000;

        return new ChannelConfigPacket(changeChannel, upperChannel, channel, dwellTime);
    }
    
    public byte[] toBytes() {
        byte[] data = new byte[2+3+4];
        ByteBuffer bb = ByteBuffer.wrap(data);
        
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.put((byte)PacketParser.PROTO_VERSION);
        bb.put((byte)PacketParser.PacketType.PROTO_CONF_CHAN.getValue());
        bb.put((byte)(changeChannel?1:0));
        bb.put((byte)upper);
        bb.put((byte)(channel-1));
        bb.putInt(dwellTime*1000);
        
        return data;
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        sb.append("ChannelConfig");
        sb.append(" - ");
        sb.append(getChangeChannel());
        sb.append(" - ");
        sb.append(getChannel());
        sb.append(" - ");
        sb.append(getDwellTime());
        
        return sb.toString();
    }

    /**
     * @return the doChange
     */
    public boolean getChangeChannel() {
        return changeChannel;
    }

    /**
     * @param doChange the doChange to set
     */
    public void setChangeChannel(boolean doChange) {
        this.changeChannel = doChange;
    }

    /**
     * @return the upper
     */
    public int getUpper() {
        return upper;
    }

    /**
     * @param upper the upper to set
     */
    public void setUpper(int upper) {
        this.upper = upper;
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
     * @return the dwellTime in milliseconds
     */
    public int getDwellTime() {
        return dwellTime;
    }

    /**
     * @param dwellTime the dwellTime to set in milliseconds
     */
    public void setDwellTime(int dwellTime) {
        this.dwellTime = dwellTime;
    }
}
