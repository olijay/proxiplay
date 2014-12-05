/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package horstclientlite.domain;

import horstclientlite.PacketParser;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Gof
 */
public class ChannelListPacket implements Packet {
    private final List<Channel> channels;
    
    public ChannelListPacket(List<Channel> channels) {
        this.channels = new LinkedList<>(channels);
    }

    public static Packet parse(InputStream in) throws IOException {
        List<Channel> channels = new LinkedList<>();
        
        int numChannels = PacketParser.readByte(in);

        for (int i = 0; i < numChannels; i++) {
            int channel = PacketParser.readByte(in);
            int freq = PacketParser.readByte(in);

            channels.add(new Channel(channel, freq));
        }
        
        return new ChannelListPacket(channels);
    }

    @Override
    public byte[] toBytes() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
