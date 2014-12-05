package proximitysensor.domain;

import horstclientlite.PacketParser;
import horstclientlite.domain.ChannelConfigPacket;
import horstclientlite.domain.ClientPacket;
import horstclientlite.domain.FilterConfigPacket;
import horstclientlite.domain.MacFilter;
import horstclientlite.domain.Packet;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket; 
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import proximitysensor.Main.CommandlineArguments;

public class HorstClientRunner implements Runnable {
	private Thread ourThread;
	private Process horstProcess;
	
	private CommandlineArguments args = null;
	private ServerCommunicator serverComm = null;
	
	private long lastSend = -1;
	
	public HorstClientRunner(CommandlineArguments args) {
		this.args = args;
		this.serverComm = new ServerCommunicator(args.measurementUrl);
	}
	
	public void stopHorst() {
		if (ourThread != null) {
			if (horstProcess != null) {
				horstProcess.destroy();
				horstProcess = null;
			}

			ourThread.interrupt();
			ourThread = null;
		}
	}

	public void startHorst() {
		ourThread = new Thread(this);
		ourThread.start();
	}
	

	
	private void startHorstServer(String monitoriface) throws InterruptedException {
		try { // horst -C -p 4260 -i mon0

			ProcessBuilder pb = new ProcessBuilder("sudo","horst", "-C", "-p", "4260", "-i", monitoriface);
			Process p = pb.start();
			
			
		} catch (IOException ex) {
			System.out.println(ex.getLocalizedMessage());
		}
	}

	@Override
	public void run() {		
		ArrayList<StationReading> stations = new ArrayList<StationReading>();
		Socket sock;
		InputStream in = null;
		try {
			// start monitor mode
			Airmon.startMon0(args.monitoriface);
			startHorstServer(args.monitoriface);
			
			// connect to local horst			
			sock = new Socket("localhost", 4260);
			
			// set up config packets
	        in = sock.getInputStream();
	        List<MacFilter> macs = new ArrayList<>();
	        //macs.add(new MacFilter("68:5d:43:7d:c3:28",true));
	        //FilterConfigPacket filterConfig = new FilterConfigPacket(macs,"50:67:f0:87:ef:f7",0,false);
	        ChannelConfigPacket channelConfig = new ChannelConfigPacket(false, 6, 6, 100);
	        
	        sock.getOutputStream().write(channelConfig.toBytes());
	        sock.getOutputStream().flush();
	        //sock.getOutputStream().write(filterConfig.toBytes());
	        //sock.getOutputStream().flush();
	        
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
		
        // main loop
        while(true) {
            Packet p;
			try {
				p = PacketParser.parsePacket(in);
				if (p instanceof ClientPacket) {
					ClientPacket c = (ClientPacket)p;
					java.util.Date date = new java.util.Date();
					Timestamp t = new Timestamp(date.getTime());
					
					if (c.getClientMac() != "00:00:00:00:00:00") {
						stations.add(new StationReading(
								c.getBssidMac().toUpperCase(), 
								c.getClientMac().toUpperCase(), 
								c.getSignalStrength(),
								args.id, 
								t, 
								t.getTime(), 
								args.channel));
						
						long now = System.currentTimeMillis();
						if (now - lastSend > args.sendWaitInterval) {
							serverComm.sendStations(stations);
							lastSend = now;
						}
						stations.clear();
					}
					
				}
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				continue;
			}
			
        }
        
        
		

	}

}
