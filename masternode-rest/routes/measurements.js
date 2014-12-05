var sqlite3 = require('sqlite3').verbose();
var sqlutils = require('../util/sqlite');
var utils = require('../util/utils');
var msmMatrix = [];
var allUsers = [];
var allNodes = [];
var N = 5; // number of measurements in matrix

var threshold = 60000; //  disregard measurements older than this (3600000 == 1hr)
exports.initMsmMatrix = function(nodes, users, callback) {
    allUsers = users;
    allNodes = nodes;
    for (var i = allNodes.length - 1; i >= 0; i--) {       
        console.log("Matrix init for node " + allNodes[i].nodeId);
        msmMatrix[allNodes[i].nodeId] = [];
        for (var j = allUsers.length - 1; j >= 0; j--) {
            console.log("Matrix init for user " + allUsers[j]);
            msmMatrix[allNodes[i].nodeId][allUsers[j]] = [];
        };
    };
    var test = true;
    if (test) {
    // init test data
    // this is the node that will be selected for vol 100
    addSSMeasurementMemInternal({bssid : "50:67:F0:87:EF:F7", mac : "68:5D:43:7D:C3:28", power : 210, nodeId : 2, channel : 6});
    addSSMeasurementMemInternal({bssid : "50:67:F0:87:EF:F7", mac : "68:5D:43:7D:C3:28", power : 200, nodeId : 2, channel : 6});
    addSSMeasurementMemInternal({bssid : "50:67:F0:87:EF:F7", mac : "68:5D:43:7D:C3:28", power : 190, nodeId : 2, channel : 6});
    addSSMeasurementMemInternal({bssid : "50:67:F0:87:EF:F7", mac : "68:5D:43:7D:C3:28", power : 200, nodeId : 2, channel : 6});
    addSSMeasurementMemInternal({bssid : "50:67:F0:87:EF:F7", mac : "68:5D:43:7D:C3:28", power : 210, nodeId : 2, channel : 6});

    addSSMeasurementMemInternal({bssid : "50:67:F0:87:EF:F7", mac : "68:5D:43:7D:C3:28", power : 190, nodeId : 1, channel : 6});
    addSSMeasurementMemInternal({bssid : "50:67:F0:87:EF:F7", mac : "68:5D:43:7D:C3:28", power : 180, nodeId : 1, channel : 6});
    addSSMeasurementMemInternal({bssid : "50:67:F0:87:EF:F7", mac : "68:5D:43:7D:C3:28", power : 170, nodeId : 1, channel : 6});
    addSSMeasurementMemInternal({bssid : "50:67:F0:87:EF:F7", mac : "68:5D:43:7D:C3:28", power : 160, nodeId : 1, channel : 6});
    addSSMeasurementMemInternal({bssid : "50:67:F0:87:EF:F7", mac : "68:5D:43:7D:C3:28", power : 150, nodeId : 1, channel : 6});
    /*
    addSSMeasurementMemInternal({bssid : "50:67:F0:87:EF:F7", mac : "68:5D:43:7D:C3:28", power : -60, nodeId : 3, channel : 6});
    addSSMeasurementMemInternal({bssid : "50:67:F0:87:EF:F7", mac : "68:5D:43:7D:C3:28", power : -50, nodeId : 3, channel : 6});
    addSSMeasurementMemInternal({bssid : "50:67:F0:87:EF:F7", mac : "68:5D:43:7D:C3:28", power : -65, nodeId : 3, channel : 6});
    addSSMeasurementMemInternal({bssid : "50:67:F0:87:EF:F7", mac : "68:5D:43:7D:C3:28", power : -60, nodeId : 3, channel : 6});
    addSSMeasurementMemInternal({bssid : "50:67:F0:87:EF:F7", mac : "68:5D:43:7D:C3:28", power : -60, nodeId : 3, channel : 6});

    addSSMeasurementMemInternal({bssid : "50:67:F0:87:EF:F7", mac : "68:5D:43:7D:C3:28", power : -70, nodeId : 4, channel : 6});
    addSSMeasurementMemInternal({bssid : "50:67:F0:87:EF:F7", mac : "68:5D:43:7D:C3:28", power : -60, nodeId : 4, channel : 6});
    addSSMeasurementMemInternal({bssid : "50:67:F0:87:EF:F7", mac : "68:5D:43:7D:C3:28", power : -75, nodeId : 4, channel : 6});
    addSSMeasurementMemInternal({bssid : "50:67:F0:87:EF:F7", mac : "68:5D:43:7D:C3:28", power : -70, nodeId : 4, channel : 6});
    addSSMeasurementMemInternal({bssid : "50:67:F0:87:EF:F7", mac : "68:5D:43:7D:C3:28", power : -70, nodeId : 4, channel : 6});
    */
    }   

    if (typeof(callback) == "function") {
            callback(msmMatrix);
        }
    
};
function addMsmToMatrix(m,dateString,ticks,callback) {
               
    // m: bssid, mac, power, nodeId, timestamp, ticks, channel
    m.timestamp = dateString;
    m.ticks = ticks;
    
    //console.log("Add measurement for mac " + m.mac + " node " +m.nodeId+", " + " pwr " + m.power); 
    if (allUsers.indexOf(m.mac) != -1) { // only add measurement if user mac exists in memory
        //console.log("Add measurement for mac " + m.mac + " node " +m.nodeId+", " + " pwr " + m.power); 
        arr = (msmMatrix[m.nodeId][m.mac]);
        arr.unshift(m); // add to beginning of array
        if (arr.length > N) { // truncate to length N 
            arr = arr.slice(0,N);
        }

        msmMatrix[m.nodeId][m.mac] = arr;
        if (typeof(callback) == "function") {
            callback(m);
        }
    }       
};

function addMsmToDb(m,dateString,ticks,db,callback) {
        //if (m.mac == "88:9B:39:3C:4D:41") {
            //console.log("MAC " + m.mac + " found, pwr: " + m.power + " from node " + m.nodeId);
        //}
        m.timestamp = dateString;
        m.ticks = ticks;
        db.run("INSERT INTO measurements (bssid, mac, power, nodeId, timestamp, ticks, channel) VALUES(?,?,?,?,?,?,?)",
        m.bssid,m.mac,m.power,m.nodeId,m.timestamp,m.ticks,m.channel, 
        function(err){
            if (err) {
                console.log("addSSMeasurement error: " + err);
            }
        });
        if (typeof(callback) == "function") {
            callback(m);
        }
}

function addSSMeasurementMemInternal(msm) {
  
}


exports.addSSMeasurementMem = function(req, res){ 
    var msm = req.body;    
    addSSMeasurementMemInternal(msm);
      var d = new Date();
    var dateString = d.toISOString();
    var ticks = d.getTime();
    
    if (msm instanceof Array) {
        //console.log("Adding " + msm.length + " measurements from node " + msm[0].nodeId);
        for (var i = msm.length - 1; i >= 0; i--) {
            addMsmToMatrix(msm[i],dateString,ticks);
        };
    } else {
        //console.log("Adding obj measurement from node " + msm.nodeId);
        addMsmToMatrix(msm,dateString,ticks);
        
    }
    res.send("OK");
};

exports.addSSMeasurementDb = function(req, res){ 
    var msm = req.body;    
    var d = new Date();
    var dateString = d.toISOString();
    var ticks = d.getTime();
    var db = new sqlite3.Database('masternode.db'); 
    
    if (msm instanceof Array) {
        console.log("Yep its an array");
        console.log("Adding " + msm.length + " measurements from node " + msm[0].nodeId);
        for (var i = msm.length - 1; i >= 0; i--) {
            addMsmToDb(msm[i],dateString,ticks,db);
        };
    } else {
        console.log("Nope its an obj");
        console.log("Adding measurement from node " + msm.nodeId);
        addMsmToDb(msm,dateString,ticks,db, function(m){
            console.log("msmMatrix: " + (msmMatrix[m.nodeId][m.mac]).length);
        });
    }
    
    db.close();  
    res.send("OK");
};

exports.medianMatrixValues = function(nodes, users, callback) {  
  N2 = parseInt(N/2);
  medians = [];
  
    
  // loop through both nodes and users
  // retrieve latest measurement for each node and user
  // check for validity, measurement must be newer than the threshold value
    tickThreshold = 0;
    d = new Date();
    ticks = d.getTime();
    tickThreshold = ticks - threshold;

    for (var i = nodes.length - 1; i >= 0; i--) {
       for (var j = users.length - 1; j >= 0; j--) {  
            //console.log(nodes);
            //console.log(users);
            //console.log(nodes[i].nodeId);
            //console.log(msmMatrix[nodes[i]]);
            if (msmMatrix[nodes[i].nodeId] && msmMatrix[nodes[i].nodeId][users[j]]) {
                var rows = msmMatrix[nodes[i].nodeId][users[j]];
                //console.log("rows: " + rows);
                
               
               if (rows && rows.length > 0 ) {
                    
                    // check time threshold validity
                    var allRowsAreValid = true;
                    
                    for (var k = rows.length - 1; k >= 0; k--) {
                        if (rows[k].ticks < tickThreshold) { 
                            // older than tickThreshold, the current nodes[i] will never be a candidate closestnode
                            allRowsAreValid = false;
                            break;
                        }
                    }; 
                    if (allRowsAreValid) {
                    // get the median value                    
                        rows.sort(utils.nodePowerCompare);      
                        if (rows[N2]) {
                            var closestnodecandidate = {
                                bssid : rows[N2].bssid, 
                                mac : rows[N2].mac, 
                                power : rows[N2].power, 
                                nodeId : rows[N2].nodeId,    
                                roomId : nodes[i].roomId,                                                   
                                channel : rows[N2].channel,
                                timestamp : rows[N2].timestamp, 
                                ticks : rows[N2].ticks
                            };
                                                
                            medians.push(closestnodecandidate);      
                        }
                    }                


                }

                  
            }

            if (typeof(callback) == "function" && i == 0 && j == 0) { 
                    //console.log("medianValues callback");        
                    callback(medians);
            }      
            
        };  
    };                    
}

exports.rawMatrixValues = function(nodes, users, callback) {  
  rawvalues = [];
  
  // loop through both nodes and users
  // retrieve latest measurement for each node and user
  // check for validity, measurement must be newer than the threshold value
    tickThreshold = 0;
    d = new Date();
    ticks = d.getTime();
    tickThreshold = ticks - threshold;
        
  

    for (var i = nodes.length - 1; i >= 0; i--) {
       for (var j = users.length - 1; j >= 0; j--) {  
            //console.log(nodes);
            //console.log(users);
            //console.log(nodes[i].nodeId);
            //console.log(msmMatrix[nodes[i].nodeId]);
            if (msmMatrix[nodes[i].nodeId] && msmMatrix[nodes[i].nodeId][users[j]]) {
                var rows = msmMatrix[nodes[i].nodeId][users[j]];
                //console.log("raw rows.length: " + rows.length + " rows: " + rows);               
                
               
                if (rows && rows.length > 0 ) {
                    
                    // check time threshold validity
                    // newest measurement is always in rows[0] 
                    if (rows[0].ticks > tickThreshold) {
                    // get the latest value                     
                        
                            var closestnodecandidate = {
                                bssid : rows[0].bssid, 
                                mac : rows[0].mac, 
                                power : rows[0].power, 
                                nodeId : rows[0].nodeId,    
                                roomId : nodes[i].roomId,                                                   
                                channel : rows[0].channel,
                                timestamp : rows[0].timestamp, 
                                ticks : rows[0].ticks
                            };
                                                
                            rawvalues.push(closestnodecandidate);      
                        
                    }                


                }

                  
            }

            if (typeof(callback) == "function" && i == 0 && j == 0) { 
                    //console.log("medianValues callback");        
                    callback(rawvalues);
            }      
            
        };  
    };                    
}


// get the recent node value for a given client MAC and node ID
exports.getNewestNode = function getNewestNode(clientMac, nodeId, callback) {
    measurement = {};
    if (msmMatrix[nodeId] && msmMatrix[nodeId][clientMac] && msmMatrix[nodeId][clientMac][0]) {
        //console.log("yes it exists " + msmMatrix[nodeId][clientMac].length);
        measurement = msmMatrix[nodeId][clientMac][0];
        d = new Date();
        ticks = d.getTime() - measurement.ticks;
        measurement.seconds = ticks/1000;
        //console.log(measurement);
        
    }
    if (typeof(callback) == "function") {
        callback(measurement);
    }  
 
    
}

exports.getNewestNodeRaw = function(req, res) {
    var clientMac = req.params.mac;
    var nodeId = req.params.nodeid;
    //console.log("raw mac " + clientMac);
    exports.getNewestNode(clientMac, nodeId, function (closestnode){           
        res.json(closestnode);        
    });
   
};

exports.dropMeasurements = function(req,res) {
     
     sqlutils.deleteFromTable("measurements", function(){ res.send("OK")});

};

exports.findByMac = function(req, res){ 
   msms = [];
    var db = new sqlite3.Database('masternode.db', sqlite3.OPEN_READONLY); 
    db.serialize(function() {
        db.each("SELECT bssid, mac, power, nodeId, timestamp, ticks, channel FROM measurements WHERE mac = ?", mac, 
        function(err, row) { // each row            
            msms.push(
                {"bssid" : row.bssid, "mac" : row.mac, 
                "power" : row.power, "nodeId" : row.nodeId, 
                "timestamp" : row.timestamp, "ticks" : row.ticks,
                "channel" : row.channel});
        },
        function(err, rows) { // complete 
            res.send(msms);
        });
    });
    db.close(); 
};

exports.getAll = function(req, res) {
    msms = [];
    var db = new sqlite3.Database('masternode.db', sqlite3.OPEN_READONLY); 
    db.serialize(function() {
        db.each("SELECT bssid, mac, power, nodeId, timestamp, ticks, channel FROM measurements", 
        function(err, row) { // each row
            
            msms.push(
                {"bssid" : row.bssid, "mac" : row.mac, 
                "power" : row.power, "nodeId" : row.nodeId, 
                "timestamp" : row.timestamp, "ticks" : row.ticks,
                "channel" : row.channel});
        },
        function(err, rows) { // complete 
            res.send(msms);
        });
    });
    db.close(); 
};