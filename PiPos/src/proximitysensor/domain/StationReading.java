package proximitysensor.domain;

import java.sql.Timestamp;

public class StationReading {
private String bssid;
private String mac;
private int power;
private String nodeId;
private Timestamp timestamp;
private long ticks;
private int channel;

public int getChannel() {
	return channel;
}

public String getNodeId() {
	return nodeId;
}

public void setNodeId(String nodeId) {
	this.nodeId = nodeId;
}

public StationReading() {
	bssid = "";
	mac = "";
	power = 0;
	channel = 0;
}

public StationReading(String bssid, String mac, int power, String nodeId, Timestamp timestamp, long ticks, int channel)
{
	this.bssid = bssid;
	this.mac = mac;
	this.power = power;
	this.nodeId = nodeId;
	this.timestamp = timestamp;
	this.ticks = ticks;
	this.channel = channel;
}
public Timestamp getTimestamp() {
	return timestamp;
}

public void setTimestamp(Timestamp timestamp) {
	this.timestamp = timestamp;
}

public String getBssid() {
	return bssid;
}
public void setBssid(String bssid) {
	this.bssid = bssid;
}
public String getMac() {
	return mac;
}
public void setMac(String mac) {
	this.mac = mac;
}
public int getPower() {
	return power;
}
public void setPower(int power) {
	this.power = power;
}

public long getTicks() {
	return ticks;
}

public void setTicks(long ticks) {
	this.ticks = ticks;
}

}
