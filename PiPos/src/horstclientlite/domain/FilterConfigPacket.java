/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package horstclientlite.domain;

import horstclientlite.PacketParser;
import horstclientlite.PacketParser.PacketFilterType;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Gof
 */
public class FilterConfigPacket implements Packet {

    private List<MacFilter> macFilters;
    private String bssid;
    private boolean disableAllFilters;
    private Map<PacketFilterType, Boolean> packetFilterTypes;

    public FilterConfigPacket(List<MacFilter> macFilters, String bssid, int flags, boolean disableAllFilters) {
        this.macFilters = new LinkedList<>(macFilters);
        this.bssid = bssid;
        this.disableAllFilters = disableAllFilters;
        
        packetFilterTypes = new HashMap<>();
        
        for(PacketFilterType filterType : PacketFilterType.values()) {
            boolean enabled = false;
            if((flags & filterType.getValue()) == filterType.getValue()) {
                enabled = true;
            }
            packetFilterTypes.put(filterType, enabled);
        }
    }   

    @Override
    public byte[] toBytes() {
        //int dataLength = 2+PacketParser.NUM_FILTERMACS*PacketParser.MAC_LEN + PacketParser.NUM_FILTERMACS + PacketParser.MAC_LEN + 4 + 1;
        int dataLength = 2+getMacFilters().size()*PacketParser.MAC_LEN + getMacFilters().size() + PacketParser.MAC_LEN + 4 + 1;
        byte[] data = new byte[dataLength];
        ByteBuffer bb = ByteBuffer.wrap(data);
        bb.order(ByteOrder.LITTLE_ENDIAN);

        bb.put((byte) PacketParser.PROTO_VERSION); // 1
        bb.put((byte) PacketParser.PacketType.PROTO_CONF_FILTER.getValue()); // 1

        //MAX_FILTERMAC * MAC_LEN
        for (MacFilter filter : getMacFilters()) {
            for (String s : filter.getMac().split(":")) {
                int i = Integer.parseInt(s, 16);
                bb.put((byte) i);
            }
        }

        //MAX_FILTERMAC
        for (MacFilter filter : getMacFilters()) {
            bb.put((byte) (filter.isEnabled() ? 1 : 0));
        }

        //MAC_LEN
        for (String s : getBssid().split(":")) {
            int i = Integer.parseInt(s, 16);
            bb.put((byte) i);
        }

        //4
        int filterPkt = 0;
        for(PacketFilterType filterType : PacketFilterType.values()) {
            if(packetFilterTypes.get(filterType)) {
                filterPkt |= filterType.getValue();
            }
        }
        
        bb.putInt(filterPkt);
        //1
        bb.put((byte) (isDisableAllFilters()?1:0));

        if(bb.remaining() > 0) {
            throw new IllegalStateException("Byte Buffer has extra room: "+bb.remaining());
        }
        
        return data;
    }

    public static FilterConfigPacket parse(InputStream in) throws IOException {
        List<MacFilter> macFilters = new LinkedList<>();

        LinkedList<byte[]> macs = new LinkedList<>();

        byte[] mac = new byte[PacketParser.MAC_LEN];
        for (int i = 0; i < PacketParser.NUM_FILTERMACS; i++) {
            PacketParser.readBytes(in, mac);
            macs.add(mac);
        }

        for (int i = 0; i < PacketParser.NUM_FILTERMACS; i++) {
            boolean enabled = PacketParser.readByte(in) == 1;
            mac = macs.removeFirst();
            macFilters.add(new MacFilter(PacketParser.parseMac(mac), enabled));
        }

        byte[] bssid = new byte[PacketParser.MAC_LEN];
        PacketParser.readBytes(in, bssid);

        int filterFlags = PacketParser.readInt(in);
        boolean filterOff = PacketParser.readByte(in) == 1;

        return new FilterConfigPacket(macFilters, PacketParser.parseMac(bssid), filterFlags, filterOff);
    }

    /**
     * @return the macFilters
     */
    public List<MacFilter> getMacFilters() {
        return macFilters;
    }

    /**
     * @param macFilters the macFilters to set
     */
    public void setMacFilters(List<MacFilter> macFilters) {
        this.macFilters = macFilters;
    }

    /**
     * @return the bssid
     */
    public String getBssid() {
        return bssid;
    }

    /**
     * @param bssid the bssid to set
     */
    public void setBssid(String bssid) {
        this.bssid = bssid;
    }

    /**
     * @return the filterOff
     */
    public boolean isDisableAllFilters() {
        return disableAllFilters;
    }

    /**
     * @param filterOff the filterOff to set
     */
    public void setDisableAllFilters(boolean filterOff) {
        this.disableAllFilters = filterOff;
    }
    
    public boolean isPacketFilterType(PacketFilterType type) {
        return packetFilterTypes.get(type);
    }
    
    public void setPacketFilterType(PacketFilterType type, boolean value) {
        packetFilterTypes.put(type, value);
    }
}
