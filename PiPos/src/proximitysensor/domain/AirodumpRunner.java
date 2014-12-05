/*
 * ProxiMagic - (c) 2013,2014 Rolf Bagge, Janus B. Kristensen - CAVI, Aarhus University
 * Website: https://laa.projects.cavi.au.dk
 */
package proximitysensor.domain;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.ProtocolException;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;

import proximitysensor.Main.CommandlineArguments;

/**
 * Wrapper and logic for AiroDump control
 * 
 * @author Rolf
 */
public class AirodumpRunner implements Runnable {

	private Airmon.AirmonInterface monitoriface;
	private Airmon.AirmonInterface dataiface;
	private Thread ourThread;
	private Process airodumpProcess;
	private final CommandlineArguments args;
	private List<String> argsArr;
	private long lastWpaRestart;
	private ServerCommunicator serverComm = null;
	public AirodumpRunner(Airmon.AirmonInterface dataiface,
			Airmon.AirmonInterface monitoriface,
			CommandlineArguments arguments) {
		// arguments.channel, arguments.bssid, arguments.sendWaitInterval,
		// arguments.updateInterval, arguments.berlin, arguments.host,
		// arguments.id
		
		Class aClass = CommandlineArguments.class;
		Field[] fields = aClass.getFields();
		System.out.println("arguments:");
		for (Field f : fields) {
			try {
				System.out.println(f.getName() + " " + f.get(arguments));
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		lastWpaRestart = 0;
		this.dataiface = dataiface;
		this.monitoriface= monitoriface;
		this.args = arguments;

		this.argsArr = new ArrayList<String>();
		argsArr.add("sudo");
		argsArr.add("airodump-ng");
		if (args.channel > 0) {
			argsArr.add("--channel");
			argsArr.add(Integer.toString(args.channel));
		}
		if (args.bssid != null && !args.bssid.isEmpty()) {
			argsArr.add("--bssid");
			argsArr.add(args.bssid);
		}
		/*if (args.updateInterval > 0) {
			argsArr.add("--update");
			argsArr.add(Integer.toString(args.updateInterval));
		}
		if (args.berlin > 0) {
			argsArr.add("--berlin");
			argsArr.add(Integer.toString(args.berlin));
		}*/
		this.serverComm = new ServerCommunicator(args.measurementUrl);
		ourThread = null;
		airodumpProcess = null;
	}

	public void stopCapture() {
		if (ourThread != null) {
			if (airodumpProcess != null) {
				airodumpProcess.destroy();
				airodumpProcess = null;
			}

			ourThread.interrupt();
			ourThread = null;
		}
	}

	public void startCapture() {
		ourThread = new Thread(this);
		ourThread.start();
	}

	@Override
	public void run() {

		try {

			// Enable monitoring mode

			System.out.println("Killing wpa_supplicant...");
			Airmon.killWpaSupplicant();
			System.out.println("Stopping all old monitoring interfaces...");
			Airmon.stopAllMonitoring();
			System.out.println("Starting monitoring on: " + monitoriface.getName());
			Airmon.startMon0(monitoriface.getName());

			//System.out.println("\nStarting WPASupplicant");
			//Airmon.startWpaSupplicant(dataiface.getName());
			//System.out.println("\nObtaining IP using DHCP");
			//Airmon.initDhcp(iface.getName());

			// System.out.println("\nRegistering node with master node");
			// registerNode(myID);
			System.out.println("\nStarting airodump-ng");
			// airodumpProcess = runProcess("sudo", "airodump-ng", "--channel",
			// ""+channel, mon.getName(), "--update", ""+dumpUpdateInterval,
			// "--berlin", ""+berlin);
			// airodumpProcess = runProcess("sudo", "airodump-ng", "--channel",
			// ""+channel, "--bssid", bssid, mon.getName());

			argsArr.add("mon0");
			for (String s : argsArr) {
				System.out.println("airodumpProcess arg: " + s);
			}
			airodumpProcess = runProcess(argsArr.toArray(new String[argsArr
					.size()]));

			BufferedReader errReader = new BufferedReader(
					new InputStreamReader(airodumpProcess.getErrorStream()));

			// InputStream in = airodumpProcess.getErrorStream();

			int i = errReader.read();

			StringBuilder sb = new StringBuilder();

			boolean escapeLine = false;

			while (i != -1) {
				char c = (char) i;
				// System.out.println(""+i+" - ["+c+"]");

				if (i == 27) {
					// Escape char
					escapeLine = true;
				}
				if (c == '\n') {
					// Newline
					String line = sb.toString();
					// System.out.println("Parsing: "+line);
					// if(!escapeLine) {
					parseLine(line);
					// parseLineRaw(line);
					// }
					escapeLine = false;
					// sb = new StringBuilder();
					sb.delete(0, sb.length() - 1);
				} else if (c == '\r') {
					// Return, ignore this
				} else {
					sb.append(c);
				}

				i = errReader.read();

			}
		} catch (IOException | InterruptedException ex) {
			System.out.println("Error in AirodumpRunner: " + ex.getLocalizedMessage());
			ex.printStackTrace();
			Logger.getLogger(AirodumpRunner.class.getName()).log(Level.SEVERE,
					null, ex);
		}
	}

	private InetAddress getIpAddress() {
		InetAddress ip;
		try {

			ip = InetAddress.getLocalHost();
			System.out.println("Current IP address : " + ip.getHostAddress());
			return ip;
		} catch (UnknownHostException e) {
			
			System.out.println("Error in AirodumpRunner: " + e.getLocalizedMessage());
			e.printStackTrace();

		}
		return null;

	}

	private String getMacAddress() {
		String macAddress = "";

		try {

			NetworkInterface network = NetworkInterface
					.getByInetAddress(getIpAddress());

			byte[] mac = network.getHardwareAddress();

			System.out.print("Current MAC address : ");

			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < mac.length; i++) {
				sb.append(String.format("%02X%s", mac[i],
						(i < mac.length - 1) ? "-" : ""));
			}
			macAddress = sb.toString();
			System.out.print("Current MAC address : " + macAddress);

		} catch (SocketException e) {

			System.out.println("Error in AirodumpRunner: " + e.getLocalizedMessage());
			e.printStackTrace();

		}
		return macAddress;

	}

	private int postData(URL postUrl, Object postData) {
		HttpURLConnection conn = null;
		int response = -1;
		try {
			

			conn = (HttpURLConnection) postUrl.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("User-Agent", "ProximitySensor/1.0");
			conn.setRequestProperty("Content-Type", "application/json");
		} catch (ProtocolException e) {
			// TODO Auto-generated catch block
			System.out.println("Error in AirodumpRunner: " + e.getLocalizedMessage());
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			System.out.println("Error in AirodumpRunner: " + e.getLocalizedMessage());
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			
			System.out.println("Error in AirodumpRunner: " + e.getLocalizedMessage());
			e.printStackTrace();
		}

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutput out = null;
		OutputStream output;
		try {
			out = new ObjectOutputStream(bos);
			out.writeObject(postData);
			byte[] postBytes = bos.toByteArray();
			conn.setRequestProperty("Content-Length",
					String.valueOf(postBytes.length));
			output = conn.getOutputStream();
			output.write(postBytes);

			if (out != null) {
				out.close();
			}

			if (bos != null) {
				bos.close();
			}

			if (output != null) {
				output.close();
			}
			conn.connect();

			response = conn.getResponseCode();
			if (response != 200) {
				System.out.println("Server returned error: " + response);
			}

		} catch (IOException ex) {
			System.out.println("Error in AirodumpRunner: " + ex.getLocalizedMessage());
			ex.printStackTrace();
		}
		return response;
	}

	private void registerNode(String nodeID) {
		JSONObject data = new JSONObject();
		data.append("mac", getMacAddress());
		data.append("ip", getIpAddress().getHostAddress());
		data.append("id", nodeID);

		postData(args.nodeRegisterUrl, data);

	}

	private static final Pattern refreshPattern = Pattern
			.compile("BSSID\\s+STATION\\s+PWR\\s+Rate\\s+Lost\\s+(Packets|Frames)\\s+Probes?");

	private static final Pattern stationPattern = Pattern
			.compile("(([0-9A-F]{2}[:-]){5}([0-9A-F]{2}))\\s+(([0-9A-F]{2}[:-]){5}([0-9A-F]{2}))\\s+(\\S+).+");
	private static final Pattern stationPattern2 = Pattern
			.compile("(\\(not associated\\))\\s+(([0-9A-F]{2}[:-]){5}([0-9A-F]{2}))\\s+(\\S+).+");
	private static final Pattern firstLinePattern = Pattern
			.compile("CH\\s?(\\S)\\s?");

	private ArrayList<StationReading> stations = new ArrayList<StationReading>();
	private ArrayList<String> stationsString = new ArrayList<String>();
	private long lastSend = -1;

	private void parseLine(String line) {
		line = line.trim();
		// System.out.println("Parsing line: "+line);
		Matcher refreshMatcher = refreshPattern.matcher(line);
		if (refreshMatcher.matches()) {
			// System.out.println("Matched a refresh line");
			long now = System.currentTimeMillis();
			if (now - lastSend > args.sendWaitInterval) {
				serverComm.sendStations(stations);
				lastSend = now;
			}
			stations.clear();

			// Don't try to parse as a station
			return;
		}

		Matcher stationMatcher = stationPattern.matcher(line);
		Matcher smatcher2 = stationPattern2.matcher(line);
		// Matcher firstLineMatcher = firstLinePattern.matcher(line);

		// if (firstLineMatcher.matches()) {
		// System.out.println("Channel:" + firstLineMatcher.group(1));
		// }

		if (stationMatcher.matches()) {
			// System.out.println("Matched a stationMatcher line");
			String baseStationMAC = stationMatcher.group(1);
			String stationMAC = stationMatcher.group(4);
			int power = Integer.parseInt(stationMatcher.group(7));
			Timestamp t = getTimestamp();
			stations.add(new StationReading(baseStationMAC, stationMAC, power,
					args.id, t, t.getTime(), args.channel));
		}
		if (smatcher2.matches()) {
			// System.out.println("Matched a smatcher2 line");
			String baseStationMAC = smatcher2.group(1);
			String stationMAC = smatcher2.group(2);
			int power = Integer.parseInt(smatcher2.group(5));
			Timestamp t = getTimestamp();
			stations.add(new StationReading(baseStationMAC, stationMAC, power,
					args.id, t, t.getTime(), args.channel));

		}
	}

	private static Process runProcess(String... cmd) throws IOException {
		ProcessBuilder pb = new ProcessBuilder(cmd);
		Process p = pb.start();
		StreamEater.eatStream(p.getInputStream(), "airodump", System.out);
		return p;
	}

	private Timestamp getTimestamp() {
		java.util.Date date = new java.util.Date();
		return new Timestamp(date.getTime());

	}

	

}
