package proximitysensor.domain;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;

public class ServerCommunicator {
	private URL measurementEndpoint = null;
	public ServerCommunicator(URL measurementEndpoint) {
		this.measurementEndpoint = measurementEndpoint;
	}
	
	public void sendStations(ArrayList<StationReading> stationsData) {
		try {
			
			if (stationsData != null && stationsData.size() != 0) {				
				System.out
				.println("Sending readings, count:" + stationsData.size());
				
				JSONArray arr = new JSONArray(stationsData);
				String data = arr.toString();
				// System.out.println("JSON array station data: "+data);

				
				long start = System.currentTimeMillis();
				HttpURLConnection conn = (HttpURLConnection) 
						measurementEndpoint.openConnection();
				conn.setDoOutput(true);
				conn.setRequestMethod("POST");
				conn.setRequestProperty("User-Agent", "ProximitySensor/1.0");
				conn.setRequestProperty("Content-Type", "application/json");

				conn.setRequestProperty("Content-Length",
						String.valueOf(data.length()));
				OutputStream output = null;
				try {
					output = conn.getOutputStream();
				
					output.write(data.getBytes());
					conn.connect();

					int response = conn.getResponseCode();
					if (response != 200) {
						System.out.println("Server returned error: " + response);
					}
					System.out.println("Server returned " + response
							+ ", send time: "
							+ (System.currentTimeMillis() - start) + "ms");
					
				} 
				catch (IOException ex) {
					System.out.println("Error sending station data: "
							+ ex.getLocalizedMessage());
					
					// run wpa restart if network unreachable or no route to host
					/*if (ex.getLocalizedMessage().contains("Network is unreachable"))
						// || ex.getLocalizedMessage().contains("No route to host")) 
						{
						Timestamp t = getTimestamp();
						if (t.getTime() - 10000 > lastWpaRestart) { // only run every 10 secs MAX {
							lastWpaRestart = t.getTime();
							System.out.println("Restarting WPA supplicant");
							try {
								Airmon.restartWpaSupplicant(args.dataiface); 
							} catch (InterruptedException ex2) {
								System.out.println("Could not restart WPA supplicant: " 
										+ ex2.getLocalizedMessage());
							}
						}
					}*/	
					
				} finally {
					try {
						if (output != null) {
							output.close();
						}
					} catch (IOException ex) {
						System.out.println("Error on output.close(): "
								+ ex.getLocalizedMessage());
					}
				}

				
			} else {
				System.out.println("No data to send");
			}
		} catch (IOException ex) {
			Logger.getLogger(AirodumpRunner.class.getName()).log(Level.SEVERE,
					null, ex);
		}
	}
	
	

}
