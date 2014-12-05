/*
 * ProxiMagic - (c) 2013,2014 Rolf Bagge, Janus B. Kristensen - CAVI, Aarhus University
 * Website: https://laa.projects.cavi.au.dk
 */
package proximitysensor;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

import org.json.JSONObject;

import proximitysensor.domain.Airmon;
import proximitysensor.domain.AirodumpRunner;
import proximitysensor.domain.HorstClientRunner;

/**
 * Main entry class for handling commandline arguments and starting the monitor
 * 
 * @author Rolf
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InterruptedException {
    	CommandlineArguments arguments = null;
    	try {
    		arguments = initialize();
    	} catch (IOException ex) {
    		System.out.println("Initialize error: " + ex.getLocalizedMessage());
    		System.exit(-1);
    	}
        /*
        AirodumpRunner airodump = 
        		new AirodumpRunner(new Airmon.AirmonInterface(arguments.dataiface, "", ""),
        				new Airmon.AirmonInterface(arguments.monitoriface, "", ""),
        		arguments);
        airodump.startCapture();*/
    	HorstClientRunner horst = new HorstClientRunner(arguments);
    	horst.startHorst();
    }
    
    private static CommandlineArguments initialize() throws IOException {
    	CommandlineArguments arguments = new CommandlineArguments();
		System.out.println("Initializing.");
		BufferedReader reader = new BufferedReader(new FileReader(
				"src/setup.json"));
		System.out.println("Setup file loaded.");
		String str, data = "";
		while ((str = reader.readLine()) != null) {
			data += str;
		}
		reader.close();
		JSONObject jo = new JSONObject(data);

		if (jo != null && jo.getInt("nodeId") >= 0 
				&& jo.getInt("channel") >= 0 && jo.getInt("channel") <= 100
				&& jo.getString("serverIp") != null
				&& !jo.getString("serverIp").equals("")
				&& jo.getString("datainterface") != null
				&& !jo.getString("datainterface").equals("")
				&& jo.getString("monitorinterface") != null
				&& !jo.getString("monitorinterface").equals("")
				&& jo.getInt("serverPort") >= 80
				&& jo.getInt("serverPort") <= 9999
				&& jo.getString("serverMeasurementPostMethod") != null
				&& !jo.getString("serverMeasurementPostMethod").equals("")
				&& jo.getString("nodeRegisterPostMethod") != null
				&& !jo.getString("nodeRegisterPostMethod").equals("")				
				&& jo.getInt("sendwaitInterval") > 0
				&& jo.getInt("sendwaitInterval") <= 9999
				&& jo.getInt("updateInterval") > 0
				&& jo.getInt("updateInterval") <= 9999
				&& jo.getInt("berlin") > 0
				&& jo.getInt("berlin") <= 9999) {

			System.out.println("JSON perfect");
			
			
			arguments.id = String.valueOf(jo.getInt("nodeId"));
			arguments.dataiface = jo.getString("datainterface");
			arguments.monitoriface = jo.getString("monitorinterface");
			arguments.channel = jo.getInt("channel");
			arguments.bssid = jo.getString("bssid");
			arguments.measurementUrl =  new URL("http://" 
					+ jo.getString("serverIp") + ":" 
					+ String.valueOf(jo.getInt("serverPort")) + "/"
					+ jo.getString("serverMeasurementPostMethod"));
			arguments.nodeRegisterUrl =  new URL("http://" 
					+ jo.getString("serverIp") + ":" 
					+ String.valueOf(jo.getInt("serverPort")) + "/"
					+ jo.getString("nodeRegisterPostMethod"));
			arguments.sendWaitInterval = jo.getInt("sendwaitInterval");
			arguments.updateInterval = jo.getInt("updateInterval");
			arguments.berlin = jo.getInt("berlin");

		} else {
			System.out
					.println("Something went wrong duing the initialization of the setup-file.\nI can't read it!");
		}
		
		return arguments;

	}

    public static class CommandlineArguments {
        // Interface to run the proximity sensor on
        public String monitoriface;
        
        public String dataiface;
        
        // Channel to use for scanning
        public int channel;
        
        // Filter by BaseStation MAC address", required = false)
        public String bssid;

        // Host address to use for data reporting
        public URL measurementUrl;
        
        // Host address to use for registering nodes
        public URL nodeRegisterUrl;

        // Id of this sensor)
        public String id;

        // Min interval between data send)
        public int sendWaitInterval;        
        
        // How often should the WiFi card be queried (secs))
        public int updateInterval;
        
        // How long should a client linger when a frame has been seen from it (ms)
        public int berlin;
        
    }
}
