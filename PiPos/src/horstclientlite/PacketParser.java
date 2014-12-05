/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package horstclientlite;

import horstclientlite.domain.ChannelConfigPacket;
import horstclientlite.domain.ChannelListPacket;
import horstclientlite.domain.ClientPacket;
import horstclientlite.domain.FilterConfigPacket;
import horstclientlite.domain.Packet;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 *
 * @author Gof
 */
public class PacketParser {

    public static final int PROTO_VERSION = 1;

    public  static void readBytes(InputStream in, byte[] bytes) throws IOException {
        for(int i = 0; i<bytes.length; i++) {
            bytes[i] = (byte) readByte(in);
        }
    }

    public enum WlanMode {
        WLAN_MODE_UNKNOWN(0x0),
        WLAN_MODE_AP(0x01),
        WLAN_MODE_IBSS(0x02),
        WLAN_MODE_STA(0x04),
        WLAN_MODE_PROBE(0x08),
        WLAN_MODE_4ADDR(0x10),
        ;
        
        private final int value;

        WlanMode(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static WlanMode fromValue(int value) {
            for (WlanMode type : WlanMode.values()) {
                if (type.getValue() == value) {
                    return type;
                }
            }

            return WlanMode.WLAN_MODE_UNKNOWN;
        }
    }
    
    public enum PacketType {

        PROTO_PKG_INFO(0),
        PROTO_CHAN_LIST(1),
        PROTO_CONF_CHAN(2),
        PROTO_CONF_FILTER(3),;

        private final int value;

        PacketType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static PacketType fromValue(int value) {
            for (PacketType type : PacketType.values()) {
                if (type.getValue() == value) {
                    return type;
                }
            }

            throw new IllegalArgumentException("No PacketType exists with value: " + value);
        }
    }

    public static final int NUM_FILTERMACS = 9;
    public static final int MAC_LEN = 6;
    public static final int MAX_ESSID_LEN = 32;

    public static final int WLAN_MODE_AP = 0x01;
    public static final int WLAN_MODE_IBSS = 0x02;
    public static final int WLAN_MODE_STA = 0x04;
    public static final int WLAN_MODE_PROBE = 0x08;

    public enum PacketFilterType {

        PKT_TYPE_CTRL(0x000001),
        PKT_TYPE_MGMT(0x000002),
        PKT_TYPE_DATA(0x000004),
        PKT_TYPE_BADFCS(0x000008),
        PKT_TYPE_BEACON(0x000010),
        PKT_TYPE_PROBE(0x000020),
        PKT_TYPE_ASSOC(0x000040),
        PKT_TYPE_AUTH(0x000080),
        PKT_TYPE_RTS(0x000100),
        PKT_TYPE_CTS(0x000200),
        PKT_TYPE_ACK(0x000400),
        PKT_TYPE_NULL(0x000800),
        PKT_TYPE_ARP(0x001000),
        PKT_TYPE_IP(0x002000),
        PKT_TYPE_ICMP(0x004000),
        PKT_TYPE_UDP(0x008000),
        PKT_TYPE_TCP(0x010000),
        PKT_TYPE_OLSR(0x020000),
        PKT_TYPE_OLSR_LQ(0x040000),
        PKT_TYPE_OLSR_GW(0x080000),
        PKT_TYPE_BATMAN(0x100000),
        PKT_TYPE_MESHZ(0x200000),
        PKT_TYPE_QDATA(0x400000),;

        private final int value;

        PacketFilterType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static PacketFilterType fromValue(int value) {
            for (PacketFilterType type : PacketFilterType.values()) {
                if (type.getValue() == value) {
                    return type;
                }
            }

            throw new IllegalArgumentException("No PacketFilterType exists with value: " + value);
        }
    }

    private static PacketType lastGoodPacketType = null;

    public static Packet parsePacket(InputStream in) throws IOException {
        Config conf = Config.singleton();

        int version = readByte(in);
        if (version == -1) {
            throw new SocketException("EOF");
        }

        if (version == PROTO_VERSION) {

            PacketType type = PacketType.fromValue(readByte(in));

            lastGoodPacketType = type;

            switch (type) {
                case PROTO_PKG_INFO:
                    if (conf.isDebug()) {
                        System.out.println("Got PROTO_PKG_INFO");
                    }
                    return parsePkgInfo(in);
                case PROTO_CHAN_LIST:
                    if (conf.isDebug()) {
                        System.out.println("Got PROTO_CHAN_LIST");
                    }
                    return parseChanList(in);
                case PROTO_CONF_CHAN:
                    if (conf.isDebug()) {
                        System.out.println("Got PROTO_CONF_CHAN");
                    }
                    return parseConfChan(in);
                case PROTO_CONF_FILTER:
                    if (conf.isDebug()) {
                        System.out.println("Got PROTO_CONF_FILTER");
                    }
                    return parseConfFilter(in);
                default:
                    if (conf.isDebug()) {
                        System.out.println("Unknown packet type: " + type);
                        System.exit(-1);
                    }
            }

        } else {
            if (conf.isDebug()) {
                System.out.println("Wrong version: " + version);
                System.out.println("Last type: " + lastGoodPacketType);
            }
        }

        return null;
    }

    private static Packet parsePkgInfo(InputStream in) throws IOException {
        return ClientPacket.parse(in);
    }

    private static Packet parseChanList(InputStream in) throws IOException {
        return ChannelListPacket.parse(in);
    }

    private static Packet parseConfChan(InputStream in) throws IOException {
        return ChannelConfigPacket.parse(in);
    }

    private static Packet parseConfFilter(InputStream in) throws IOException {
        return FilterConfigPacket.parse(in);
    }

    public static int readInt(InputStream in) throws IOException {
        byte[] bytes = new byte[4];
        PacketParser.readBytes(in, bytes);

        ByteBuffer bb = ByteBuffer.wrap(bytes);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        return bb.getInt();
    }

    public static long readLong(InputStream in) throws IOException {
        byte[] bytes = new byte[8];
        PacketParser.readBytes(in, bytes);

        ByteBuffer bb = ByteBuffer.wrap(bytes);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        return bb.getLong();
    }

    public static String parseMac(byte[] src) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (byte b : src) {
            if (first) {
                first = false;
            } else {
                sb.append(":");
            }
            String hex = Integer.toHexString(b & 0xFF);
            if (hex.length() < 2) {
                sb.append("0");
            }
            sb.append(hex);
        }
        return sb.toString().trim();
    }

    private static final byte[] buffer = new byte[2048];
    private static int currentLength = 0;
    private static int currentByte = 0;
    
    public static int readByte(InputStream in) throws IOException {
        if(currentByte == currentLength) {
            currentLength = in.read(buffer, 0, buffer.length);
            currentByte = 0;
        }
        
        int i = buffer[currentByte];
        currentByte++;
        
        return i;
    }

}
