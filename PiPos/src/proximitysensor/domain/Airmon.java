/*
 * ProxiMagic - (c) 2013,2014 Rolf Bagge, Janus B. Kristensen - CAVI, Aarhus University
 * Website: https://laa.projects.cavi.au.dk
 */
package proximitysensor.domain;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Wrapper and logic for airmon control
 * 
 * @author Rolf
 */
public class Airmon {
	private static Pattern interfacePattern = Pattern
			.compile("([^\\t]+)\\t+([^\\t]+)\\t+([^\\t]+)");

	public static List<AirmonInterface> getInterfaces()
			throws InterruptedException {
		List<AirmonInterface> interfaces = new LinkedList<>();

		try {
			Process airmon = runProcess("sudo", "airmon-ng");
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					airmon.getInputStream()));

			String line;
			while ((line = reader.readLine()) != null) {
				Matcher m = interfacePattern.matcher(line);
				if (m.matches()) {
					interfaces.add(new AirmonInterface(m.group(1), m.group(2),
							m.group(3)));
				}
			}
			airmon.waitFor();
		} catch (IOException ex) {
			Logger.getLogger(Airmon.class.getName())
					.log(Level.SEVERE, null, ex);
		}

		return interfaces;
	}

	public static void stopMonitoring(String iface) throws InterruptedException {
		try {
			Process airmon = runProcess(true, "sudo", "airmon-ng", "stop",
					iface);
			airmon.waitFor();

		} catch (IOException ex) {
			Logger.getLogger(Airmon.class.getName())
					.log(Level.SEVERE, null, ex);
		}
	}

	public static void startMonitoring(String iface)
			throws InterruptedException {
		try {
			Process airmon = runProcess(true, "sudo", "airmon-ng", "start",
					iface);
			airmon.waitFor();

		} catch (IOException ex) {
			Logger.getLogger(Airmon.class.getName())
					.log(Level.SEVERE, null, ex);
		}
	}

	public static void startMon0(String monitoriface) throws InterruptedException {
		try {
			Process p = runProcess("sudo", "iw", "dev", monitoriface, "interface",
					"add", "mon0", "type", "monitor");
		} catch (IOException ex) {
			Logger.getLogger(Airmon.class.getName())
					.log(Level.SEVERE, null, ex);
		}
	}

	public static void killWpaSupplicant() throws InterruptedException {
		try {
			Process p = runProcess("sudo", "killall", "wpa_supplicant");
			p.waitFor();

		} catch (IOException ex) {
			Logger.getLogger(Airmon.class.getName())
					.log(Level.SEVERE, null, ex);
		}
	}

	public static void initDhcp(String dataiface) throws InterruptedException {

		try {
			Process p1 = runProcess("sudo", "killall", "dhcpcd-bin");
			p1.waitFor();
			Process p2 = runProcess("sudo", "dhcpcd", dataiface);
			p2.waitFor();

		} catch (IOException ex) {
			Logger.getLogger(Airmon.class.getName())
					.log(Level.SEVERE, null, ex);
		}
	}

	public static void startWpaSupplicant(String dataiface)
			throws InterruptedException {

		try {
			Process p = runProcess("sudo", "wpa_supplicant", "-B", "-i",
					dataiface, "-c", "/etc/wpa_supplicant/wpa_supplicant.conf");
			p.waitFor();

		} catch (IOException ex) {
			Logger.getLogger(Airmon.class.getName())
					.log(Level.SEVERE, null, ex);
		}
	}
	
	public static void ifdown(String dataiface) throws InterruptedException { 
		try {
			
			Process p1 = runProcess("sudo", "ifdown", dataiface);
			

		} catch (IOException ex) {
			
			Logger.getLogger(Airmon.class.getName())
					.log(Level.SEVERE, null, ex);
		}

	}
	
	public static void ifup(String dataiface) throws InterruptedException { 
		
		try {
			
			Process p1 = runProcess("sudo", "ifup", dataiface);
			

		} catch (IOException ex) {
			
			Logger.getLogger(Airmon.class.getName())
					.log(Level.SEVERE, null, ex);
		}
	}
	
	public static void restartWpaSupplicant(String dataiface)
			throws InterruptedException {

		try {
			
			Process p1 = runProcess("sudo", "killall", "wpa_supplicant");
			//p1.waitFor();
			System.out.println("wpa_supplicant killed, restarting it.");
			Process p2 = runProcess("sudo", "wpa_supplicant", "-B", "-i",
					dataiface, "-c", "/etc/wpa_supplicant/wpa_supplicant.conf");
			//p2.waitFor();
			//System.out.println("Killing dhcpcd-bin");
			//Process p3 = runProcess("sudo", "killall", "dhcpcd-bin");
			//p3.waitFor();
			//System.out.println("Restart dhcpcd on " + dataiface);
			//Process p4 = runProcess("sudo", "dhcpcd", dataiface);
			
			//p4.waitFor();

		} catch (IOException ex) {
			Logger.getLogger(Airmon.class.getName())
					.log(Level.SEVERE, null, ex);
		}
	}

	public static AirmonInterface getFirstMonitoringInterface()
			throws InterruptedException {
		AirmonInterface airmon = null;

		for (Airmon.AirmonInterface iface : Airmon.getInterfaces()) {
			if (iface.isMonitoring()) {
				airmon = iface;
				break;
			}
		}

		return airmon;
	}

	public static void stopAllMonitoring() throws InterruptedException {
		for (Airmon.AirmonInterface iface : Airmon.getInterfaces()) {
			if (iface.isMonitoring()) {
				iface.stop();
			}
		}
	}

	private static Process runProcess(String... cmd) throws IOException {
		return runProcess(false, cmd);
	}

	private static Process runProcess(boolean eatInputStream, String... cmd)
			throws IOException {
		ProcessBuilder pb = new ProcessBuilder(cmd);
		Process p = pb.start();
		StreamEater.eatStream(p.getErrorStream(), "airmon", System.out);
		if (eatInputStream) {
			StreamEater.eatStream(p.getInputStream(), "airmon", System.out);
		}
		return p;
	}

	public static class AirmonInterface {
		private String name;
		private String driver;
		private String chipset;

		public AirmonInterface(String name, String chipset, String driver) {
			this.name = name;
			this.chipset = chipset;
			this.driver = driver;
		}

		/**
		 * @return the name
		 */
		public String getName() {
			return name;
		}

		/**
		 * @return the driver
		 */
		public String getDriver() {
			return driver;
		}

		/**
		 * @return the chipset
		 */
		public String getChipset() {
			return chipset;
		}

		public boolean isMonitoring() {
			return this.name.matches("mon\\d+");
		}

		public void stop() throws InterruptedException {
			stopMonitoring(name);
		}

		public void start() throws InterruptedException {
			startMonitoring(name);
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();

			sb.append("Airmon Interface: ");
			sb.append(name);
			sb.append(", ").append(chipset);
			sb.append(", ").append(driver);

			return sb.toString();
		}
	}
}
