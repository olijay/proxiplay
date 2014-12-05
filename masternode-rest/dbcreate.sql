CREATE TABLE measurements (bssid TEXT, mac TEXT, power INT, nodeId INT, timestamp TEXT, ticks INT, channel INT);
CREATE TABLE nodes (mac TEXT, nodeId INT, roomId INT);
CREATE TABLE edges (fromNode INT, toNode INT, walls INT);
CREATE TABLE users (mac TEXT, ip TEXT);

INSERT INTO users (mac,ip) VALUES ("88:9B:39:3C:4D:41","");
INSERT INTO users (mac,ip) VALUES ("84:51:81:40:1F:0E","");
INSERT INTO users (mac,ip) VALUES ("1C:B0:94:D5:D3:B4","");
INSERT INTO users (mac,ip) VALUES ("68:5D:43:7D:C3:28","");
INSERT INTO users (mac,ip) VALUES ("16:DB:C9:AC:AA:98","");
INSERT INTO users (mac,ip) VALUES ("94:DB:C9:B5:A7:34","");

INSERT INTO nodes (mac, nodeId, roomId) VALUES ("C4:A8:1D:42:C1:AF",1,1);
INSERT INTO nodes (mac, nodeId, roomId) VALUES ("C4:A8:1D:42:CB:21",2,2);
INSERT INTO nodes (mac, nodeId, roomId) VALUES ("C4:A8:1D:42:C1:B0",3,3);
INSERT INTO nodes (mac, nodeId, roomId) VALUES ("C4:A8:1D:42:C1:B8",4,4);