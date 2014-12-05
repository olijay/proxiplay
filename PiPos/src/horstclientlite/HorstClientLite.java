/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package horstclientlite;

import horstclientlite.domain.ChannelConfigPacket;
import horstclientlite.domain.FilterConfigPacket;
import horstclientlite.domain.MacFilter;
import horstclientlite.domain.Packet;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Gof
 */
public class HorstClientLite {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            // TODO code application logic here

            Socket sock = new Socket("10.0.0.40", 4260);
            
            InputStream in = sock.getInputStream();
            List<MacFilter> macs = new ArrayList<>();
            macs.add(new MacFilter("68:5d:43:7d:c3:28",true));
            FilterConfigPacket filterConfig = new FilterConfigPacket(macs,"50:67:f0:87:ef:f7",0,false);
            ChannelConfigPacket channelConfig = new ChannelConfigPacket(false, 6, 6, 250);
            
            sock.getOutputStream().write(channelConfig.toBytes());
            sock.getOutputStream().flush();
            sock.getOutputStream().write(filterConfig.toBytes());
            sock.getOutputStream().flush();
            
            while(true) {
                Packet p = PacketParser.parsePacket(in);
                
                System.out.println(""+p);
            }
            
        } catch (IOException ex) {
            Logger.getLogger(HorstClientLite.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
