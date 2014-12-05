var sqlite3 = require('sqlite3').verbose();
var utils = require('../util/sqlite');
var threshold = 5000;

// constants for WAFfle algorithm (currently WRONG AS HELL)
P0 = -24;
d0 = 1;
n = 3.415;
WAF = 3.1;

exports.addUser = function(req, res){
    var user = req.body;
    console.log('Adding user: ' + JSON.stringify(user));

    var db = new sqlite3.Database('masternode.db'); 
    db.serialize(function() {
        var stmt = db.prepare("INSERT INTO users (mac, ip) VALUES(?,?)");
        stmt.run(user.mac, user.ip);
        stmt.finalize();
        res.send("OK");
    });
    db.close(); 
};

exports.findByMac = function(req, res){ 
    users = [];
    var mac = req.params.mac;  
	var db = new sqlite3.Database('masternode.db', sqlite3.OPEN_READONLY); 
    db.serialize(function() {
        db.all("SELECT mac, ip FROM users WHERE mac = ?", mac, 
            function(err, row) {
                res.json(row);
        });
    });
    db.close(); 
};

exports.findByIp = function(req, res){ 
    var ip = req.params.ip;    
    var db = new sqlite3.Database('masternode.db', sqlite3.OPEN_READONLY); 
    db.serialize(function() {
        db.all("SELECT mac, ip FROM users WHERE ip = ?", ip, 
            function(err, row) {
            res.json(row);
        });
    });
    db.close(); 
};

exports.getAll = function(req, res) {
    users = [];
    var db = new sqlite3.Database('masternode.db', sqlite3.OPEN_READONLY); 
    db.serialize(function() {
        db.each("SELECT mac, ip FROM users", 
        function(err, row) { // each row
            //console.log("mac:" + row.mac + " ip: " + row.ip);
            users.push({"mac" : row.mac, "ip" : row.ip});
        },
        function(err, rows) { // complete 
            res.json(users);
        });
    });
    db.close(); 
    
};

exports.dropUsers = function(req,res) {
     utils.deleteFromTable("users", function(){ res.send("OK")});
};


// getLocation order by signal strength
exports.getLocation = function(req, res) {
    // get all nodes in the past tickThreshold time
    // order by SS
    // calculate pos from top 3 nodes
    nodeenum = [];
    nodelist = [];
    tickThreshold = 0;
    var db = new sqlite3.Database('masternode.db', sqlite3.OPEN_READONLY); 
    db.serialize(function() {
        db.each("SELECT MAX(ticks) as maxticks FROM measurements", 
            function(err, row) { // each row
               tickThreshold = row.maxticks - threshold;
        });

        
        db.each("SELECT bssid, mac, power, nodeId, timestamp, ticks, channel FROM measurements "
                +"WHERE ticks > ? ORDER BY power DESC", tickThreshold,
            function(err, row) { // each row
                // 
                if (nodeenum.indexOf(row.nodeId) < 0) {
                    nodelist.push( 
                        {"bssid" : row.bssid, "mac" : row.mac, 
                        "power" : row.power, "nodeId" : row.nodeId, 
                        "timestamp" : row.timestamp, "ticks" : row.ticks,
                        "channel" : row.channel});
                    nodeenum.push(row.nodeId);
                }
        },
        function(err, rows) { // complete 
            
            // we need 3 nodes
            if (nodelist.length >= 3) { 
                // estimate distances to nodes from power readings

                // look up number of walls from closest node to each node in list
                for (var i = nodelist.length - 1; i >= 0; i--) {
                    db.get("SELECT walls FROM edges WHERE fromNode = ? && toNode = ?", 
                        closestnode.nodeId, nodelist[i].nodeId, function(err,row) {
                            nW = row.walls;

                            dist = math.eval('10^()'); // hmmm

                        });
                };
                
            }
            
        });
    });
    db.close();

};