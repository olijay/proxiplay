var sqlite3 = require('sqlite3').verbose();
var sqlutils = require('../util/sqlite');
var utils = require('../util/utils');
var msm = require('../routes/measurements');

var threshold = 60000; //  disregard measurements older than this (3600000 == 1hr)
var allNodes = [];
var allUsers = [];
var allUsersQueryStringArr = [];
var allUsersQueryString = "";
var nodeVolumes = [];
var anyNodeCurrentlyPlaying = false;
var nodePlayingListThreshold = [];

/* INIT METHODS */

exports.initNodesUsers = function(callback){
  
    // get all nodes and users
      // loop through both
    // retrieve 5 latest measurements for each node and user
    // average those
    // add to averages array
    var db = new sqlite3.Database('masternode.db', sqlite3.OPEN_READONLY); 
    db.serialize(function(){
        db.each("SELECT nodeId, roomId, mac FROM nodes;", 
            function(err, row){
                allNodes.push({"nodeId" : row.nodeId, "roomId" : row.roomId, "mac" : row.mac});
                
            //console.log("nodes array: " + JSON.stringify(nodes));
        }, function(err, rownum){ // complete
            db.each("SELECT mac FROM users;",                 
                function(err, row){                  
                    allUsers.push(row.mac);
                    allUsersQueryStringArr.push('"' + row.mac + '"');
                    
                                     
                    //console.log("users array: " + JSON.stringify(users));
                }, function(err,rows){
                    allUsersQueryString = allUsersQueryStringArr.join();                    
                    if (typeof(callback) == "function") {
                       callback(allNodes, allUsers);
                    }
                });
                    
        });
    });
}

/* VARIOUS DATABASE METHODS */

exports.addNode = function(req, res){ 
 
   var node = req.body;
    console.log("Adding node " + JSON.stringify(node));

    var db = new sqlite3.Database('masternode.db'); 
    db.serialize(function() {
        var stmt = db.prepare("INSERT INTO nodes (mac, nodeId, roomId) VALUES(?,?,?);");
        stmt.run(node.mac, node.nodeId, node.roomId);
        stmt.finalize();
        res.send("OK");
    });
    db.close(); 
};

exports.addEdge = function(req, res){ 
  
   var edge = req.body;    
    console.log("Adding edge " + JSON.stringify(edge));
    var db = new sqlite3.Database('masternode.db'); 
    db.serialize(function() {
        var stmt = db.prepare("INSERT INTO edges (fromNode, toNode, walls) VALUES(?,?,?);");
        stmt.run(edge.fromNode, edge.toNode, edge.walls);
        stmt.finalize();
        res.send("OK");
    });
    db.close(); 
};

exports.getAllNodes = function(req, res) {    
            res.json(allNodes);          
};

exports.getAllEdges = function(req, res) {
    allEdges = [];
    var db = new sqlite3.Database('masternode.db', sqlite3.OPEN_READONLY); 
    db.serialize(function() {
        db.each("SELECT fromNode, toNode, walls FROM edges;", 
        function(err, row) { // each row          
            allEdges.push(
                {
                    fromNode : row.fromNode, 
                    toNode : row.toNode, 
                    walls : row.walls});
        },
        function(err, rows) { // complete 
            res.json(allEdges);
        });
    });
    db.close(); 
    
};

exports.dropNodes = function(req,res) {
     sqlutils.deleteFromTable("nodes", function(){ res.send("OK")});
};

exports.dropEdges = function(req,res) {
     sqlutils.deleteFromTable("edges", function(){ res.send("OK")});
};

/* INTERNAL NODE CALCULATION METHODS */

// get the last reported closest node
function getMostRecentClosestNodeNaive(clientMac, callback) {
    closestnode = {};       
    var db = new sqlite3.Database('masternode.db', sqlite3.OPEN_READONLY); 
    db.serialize(function() {
        

        // select one row with the highest power measurement that has the highest ticks
        db.get("SELECT bssid, mac, power, nodeId, timestamp, ticks, channel FROM measurements "
                +"WHERE mac = ? ORDER BY power DESC, ticks DESC LIMIT 1", clientMac,
        function(err, row) { 
           
            if (row) {         
                     //console.log("closestnode is " + row.nodeId);  

                closestnode = 
                    {
                        bssid : row.bssid, 
                        mac : row.mac, 
                        power : row.power, 
                        nodeId : row.nodeId, 
                        timestamp : row.timestamp, 
                        ticks : row.ticks,
                        channel : row.channel
                };

                d = new Date();
                ticks = d.getTime() - closestnode.ticks;
                closestnode.seconds = ticks/1000;
                if (typeof(callback) == "function") {                    
                    callback(closestnode);
                }
            } else {
                if (typeof(callback) == "function") {
                    callback({"err":"not found"});
                }
            }
        });
    });
    db.close();

}
// returns a list of the most recent power value for each client and node
function rawPowerValues(nodes, users, callback) {
  N = 5;  
  N2 = parseInt(N/2);
  closestnodes = [];
  var db = new sqlite3.Database('masternode.db', sqlite3.OPEN_READONLY); 
  // loop through both nodes and users
  // retrieve latest measurement for each node and user
     tickThreshold = 0;
  d = new Date();
  ticks = d.getTime();
  tickThreshold = ticks - threshold;
  db.serialize(function(){
    for (var i = nodes.length - 1; i >= 0; i--) {
       for (var j = users.length - 1; j >= 0; j--) {   
        (function(i,j,nodes,tickThreshold) {
          
            db.all("SELECT bssid, mac, power, nodeId, timestamp, ticks, channel FROM measurements "
                +"WHERE mac = ? AND nodeId = ? AND ticks > ? ORDER BY ticks DESC LIMIT 1", 
                users[j], nodes[i].nodeId, tickThreshold,
                function(err,rows) {
                    if (err) {
                        console.log("rawPowerValues err: " + err);
                    }
                    else if (rows.length == 0) {
                        //console.log("No median rows");                        
                    }
                    else if ((!err || rows) && rows.length > 0 ) {
                           
                            closestnode = {
                                bssid : rows[0].bssid, 
                                mac : rows[0].mac, 
                                power : rows[0].power, 
                                nodeId : rows[0].nodeId,    
                                roomId : nodes[i].roomId,                                                   
                                channel : rows[0].channel,
                                timestamp : rows[0].timestamp, 
                                ticks : rows[0].ticks
                            };
                                                
                            closestnodes.push(closestnode);      
                        }                


                    

                    if (typeof(callback) == "function" && i == 0 && j == 0) { 
                                   
                            callback(closestnodes);
                    }
                
                    
                    
                });
            })(i,j,nodes,tickThreshold);
       };
   };                         
       
       
   });                    
}
// returns the median value of N many measurements for each client and node
function medianValues(nodes, users, callback) {
  N = 5;  
  N2 = parseInt(N/2);
  medians = [];
  var db = new sqlite3.Database('masternode.db', sqlite3.OPEN_READONLY); 
  // loop through both nodes and users
  // retrieve N latest measurements for each node and user

  tickThreshold = 0;
  d = new Date();
  ticks = d.getTime();
  tickThreshold = ticks - threshold;
    
  db.serialize(function(){
    for (var i = nodes.length - 1; i >= 0; i--) {
       for (var j = users.length - 1; j >= 0; j--) {   
        (function(i,j,N,N2,nodes,tickThreshold) {
          
            db.all("SELECT bssid, mac, power, nodeId, timestamp, ticks, channel FROM measurements "
                +"WHERE mac = ? AND nodeId = ? AND ticks > ? ORDER BY ticks DESC LIMIT ?", 
                users[j], nodes[i].nodeId, tickThreshold, N,
                function(err,rows) {
                    //console.log("medianValues i " + i + " j " + j);
                    if (err) {
                        console.log("medianValues err: " + err);
                    }
                    else if (rows.length == 0) {
                        //console.log("No median rows");                        
                    }
                    else if ((!err || rows) && rows.length > 0 ) {
                        // get the median value
                        
                        rows.sort(utils.nodePowerCompare);      
                        if (rows[N2]) {
                            closestnode = {
                                bssid : rows[N2].bssid, 
                                mac : rows[N2].mac, 
                                power : rows[N2].power, 
                                nodeId : rows[N2].nodeId,    
                                roomId : nodes[i].roomId,                                                   
                                channel : rows[N2].channel,
                                timestamp : rows[N2].timestamp, 
                                ticks : rows[N2].ticks
                            };
                                                
                            medians.push(closestnode);      
                        }                


                    }

                    if (typeof(callback) == "function" && i == 0 && j == 0) { 
                            //console.log("medianValues callback");        
                            callback(medians);
                    }
                    
                    
                });
            })(i,j,N,N2,nodes,tickThreshold);
       };
   };                         
       
       
   });                    
}

function medianValuesImproved(nodes, users, callback) {
  N = 5;  
  medians = [];
  var db = new sqlite3.Database('masternode.db', sqlite3.OPEN_READONLY); 
  // loop through both nodes and users
  // retrieve N latest measurements for each node and user
  // average those
  // add to avgs array
  tickThreshold = 0;
  d = new Date();
  ticks = d.getTime();
  tickThreshold = ticks - threshold;
  query = "SELECT bssid, mac, power, nodeId, timestamp, ticks, channel "
            + "FROM measurements "
            + "where ( "
            + "select count(*) from measurements as f "
            + "where f.mac = measurements.mac and f.nodeId = measurements.nodeId and f.ticks > measurements.ticks "
            + ") <= "+(N-1)+" and mac in ("+allUsersQueryString+") AND ticks > " + tickThreshold + " ORDER BY nodeId";
    //console.log(query);
    N2 = parseInt(N/2);
    (function(N,N2) {
            db.all(query,                 
                function(err,rows) {
                    if (err) {
                        console.log("medianValuesImproved err: " + err);
                    }
                    else if (rows.length == 0) {
                        console.log("No median rows");                        
                    }
                    else if ((!err || rows) && rows.length > 0 ) {
                        // nodesToSort is an array of arrays
                        // each array is bound to a specific client MAC and nodeId
                        // put N number of rows into each nodesToSort array
                        //console.log("query rows " + rows.length);
                        
                        nodesToSort = [];
                        for (var i = rows.length - 1; i >= 0; i--) {
                            if (!nodesToSort[rows[i].nodeId+rows[i].mac]) {
                                //console.log("nodesToSort[" + rows[i].nodeId+rows[i].mac + "]");
                                 nodesToSort[rows[i].nodeId+rows[i].mac]= [];                                
                            }
                            //console.log("nodesToSort[" + rows[i].nodeId+rows[i].mac + "].push(" + rows[i] + ")");
                            nodesToSort[rows[i].nodeId+rows[i].mac].push(rows[i]);
                            //console.log(JSON.stringify(rows[i]));
                        };

                        // now we sort each internal array according to power
                        // the middle value of each is then our median value
                        
                        for (var nodesArr in nodesToSort) {
                            
                            

                            nodesToSort[nodesArr].sort(utils.nodePowerCompare);

              
                            closestnode = {
                                bssid : nodesToSort[nodesArr][N2].bssid, 
                                mac : nodesToSort[nodesArr][N2].mac, 
                                power : nodesToSort[nodesArr][N2].power, 
                                nodeId : nodesToSort[nodesArr][N2].nodeId,                                                
                                channel : nodesToSort[nodesArr][N2].channel,
                                timestamp : nodesToSort[nodesArr][N2].timestamp, 
                                ticks : nodesToSort[nodesArr][N2].ticks
                                // todo: roomId is missing
                            };
                                            
                            medians.push(closestnode);     

                        };
                        
                        if (typeof(callback) == "function") {
                            //console.log("medianValuesImproved callback");  
                            callback(medians);
                        }
                        
                    }   
                    
                });
        })(N,N2);
            
}

// returns a list of measurements for active clients/users in the given list of nodes
function getClientsForNodes(cNodes,cUsers,callback) {
    clientsForNodes = [];  
    
    medianValues(cNodes, cUsers, function(values){
        d = new Date();
        ticks = d.getTime();
        tickThreshold = ticks - threshold;

        for (var i = values.length - 1; i >= 0; i--) {
            clientInList = false;
            for (var j = clientsForNodes.length - 1; j >= 0; j--) {
                
                if (values[i].mac == clientsForNodes[j].mac) {
                    clientInList = true;
                }                
            };

            if (clientInList == false && values[i].ticks >= tickThreshold) {
                clientsForNodes.push(values[i]);
            }            
        };
        if (typeof(callback) == "function") {
            callback(clientsForNodes);
        }
    });
}

// get a list of closest node for all clients
function getClosestNodeForEachClientInternal(nodes,users,nodeClientValueFunc,callback) {
    // nodeClientValueFunc can be medianValues or msm.medianMatrixValues or msm.rawMatrixValues
    nodeClientValueFunc(nodes, users, function(values){      

        // loop over list, add to closestnodes   
        //console.log("getClosestNodeForEachClientInternal result of nodeClientValueFunc " + JSON.stringify(values));
        closestNodesInternal = [];
        highestPowerPerMac = []; 
       
        for (var j = users.length - 1; j >= 0; j--) {
            
            highestValue = -2000;
            for (var i = values.length - 1; i >= 0; i--) {
                if (users[j] == values[i].mac && values[i].power > highestValue) {
                    highestValue = values[i].power;
                    highestPowerPerMac[values[i].mac] = values[i].power;
                }                
            }
        };

        for (var i = values.length - 1; i >= 0; i--) {
            if (values[i].power == highestPowerPerMac[values[i].mac]) {
                // this is a bug, if we have the same highestPowerPerMac for >1 nodes for a given mac
                // then we get more than one closest node

                closestNodesInternal.push(values[i]);
            }
        };

        if (typeof(callback) == "function") {
            callback(closestNodesInternal);
        }
    });
}

// get the closest node for a given client MAC, median algorithm
function getClosestNodeMedianInternal(clientMac, callback) {
    
    msm.medianMatrixValues(allNodes, allUsers, function(values){
        //console.log("getClosestNodeAveraged values: " + JSON.stringify(values));
         //var db = new sqlite3.Database('masternode.db', sqlite3.OPEN_READONLY); 
       highestMedian = -2000;
       closestnode = {};
       if (values && values.length > 0) {
            for (var i = values.length - 1; i >= 0; i--) {
                   (function(i){
                        if (values[i].mac == clientMac) {
                            if (values[i].power > highestMedian) {
                                //console.log("closestnode: "+JSON.stringify(values[i]));
                                closestnode = values[i];
                                d = new Date();
                                ticks = d.getTime() - closestnode.ticks;
                                closestnode.seconds = ticks/1000;
                                highestMedian = values[i].power;
                            }
                        }
                        if (typeof(callback) == "function" && i == 0) {
                            callback(closestnode);
                        }
            })(i);
            };
        }
        else if (typeof(callback) == "function" && (!values || values.length == 0)) {
            callback(closestnode);
        }

        
    });
    
}

// get the closest node for a given client MAC
function getClosestNodeRawInternal(clientMac, callback) {
    
    msm.rawMatrixValues(allNodes, allUsers, function(values){
        //console.log("rawMatrixValues values: " + JSON.stringify(values));
         //var db = new sqlite3.Database('masternode.db', sqlite3.OPEN_READONLY); 
       highestPowerValue = -2000;
       closestnode = {};
       if (values && values.length > 0) {
            //console.log("raw values.length" + values.length);
            for (var i = values.length - 1; i >= 0; i--) {

                   //(function(i){
                        if (values[i].mac == clientMac) {
                            //console.log("raw found clientMac" + clientMac + "values[i].power " + values[i].power);
                            if (values[i].power > highestPowerValue) {
                                //console.log("raw closestnode: "+JSON.stringify(values[i]));
                                closestnode = values[i];
                                d = new Date();
                                ticks = d.getTime() - closestnode.ticks;
                                closestnode.seconds = ticks/1000;
                                highestPowerValue = values[i].power;
                            }
                        }
                        if (typeof(callback) == "function" && i == 0) {
                            callback(closestnode);
                        }
            //})(i);
            };
        }
        else if (typeof(callback) == "function" && (!values || values.length == 0)) {
            callback(closestnode);
        }

        
    });
    
}


function averageValues(nodes, users, callback) {
  N = 5;  
  avgs = [];
     var db = new sqlite3.Database('masternode.db', sqlite3.OPEN_READONLY); 
    // loop through both nodes and users
    // retrieve N latest measurements for each node and user
    // average those
    // add to avgs array
  tickThreshold = 0;
  d = new Date();
  ticks = d.getTime();
  tickThreshold = ticks - threshold;
    db.serialize(function(){
        for (var i = nodes.length - 1; i >= 0; i--) {
           for (var j = users.length - 1; j >= 0; j--) {   
            (function(i,j,nodes,tickThreshold) {
              
                db.all("SELECT bssid, mac, power, nodeId, timestamp, ticks, channel FROM measurements "
                    +"WHERE mac = ? AND nodeId = ? AND ticks > ? ORDER BY ticks DESC LIMIT ?", 
                    users[j], nodes[i].nodeId, tickThreshold, N,
                    function(err,rows) {
                        if (rows.length == 0) {
                            // console.log("No avg rows");
                        } else if ((!err || rows) && rows.length > 0 ) {
                            //console.log("db.all i " + i + " j " + j);
                            

                            avg = 0;
                            for (var k = nodes.length - 1; k >= 0; k--) {
                                //console.log("power: " + r.power);
                                avg += rows[k].power;
                                
                            };
                            avg = avg/N;
                            //console.log("averages: " + avg);
                            closestnode = {
                                bssid : rows[0].bssid, 
                                mac : rows[0].mac, 
                                avgPower : avg, 
                                nodeId : rows[0].nodeId,    
                                roomId : nodes[i].roomId,                                                    
                                channel : rows[0].channel
                            };
                            
                            //console.log("closestnode: " + JSON.stringify(closestnode));                       
                            avgs.push(closestnode);

                            


                        }                        

                        if (typeof(callback) == "function" && i == 0 && j == 0) {
                            //console.log("averageValues callback");
                            callback(avgs);
                        }

                        if (err) {
                        console.log("medianValues err: " + err);
                    }

                    });
                })(i,j,nodes,tickThreshold);
           };
       };                           
       
       
   });                    
}

function getClosestNodeAveragedInternal(clientMac, callback) {
    averageValues(allNodes, allUsers, function(values){
        //console.log("getClosestNodeAveraged values: " + JSON.stringify(values));
         //var db = new sqlite3.Database('masternode.db', sqlite3.OPEN_READONLY); 
       highestAvg = -2000;
       closestnode = {};
       if (values && values.length > 0) {
            for (var i = values.length - 1; i >= 0; i--) {
                   (function(i){
                if (values[i].mac == clientMac) {
                    if (values[i].avgPower > highestAvg) {
                        //console.log("closestnode: "+JSON.stringify(values[i]));
                         closestnode = 
                            {
                                "bssid" : values[i].bssid, 
                                "mac" : values[i].mac, 
                                "power" : values[i].avgPower, 
                                "nodeId" : values[i].nodeId,
                                "channel" : values[i].channel
                            };
                        highestAvg = values[i].avgPower;
                    }
                }
                if (typeof(callback) == "function" && i == 0) {
                    callback(closestnode);
                }
            })(i);
            };
        }
        else if (typeof(callback) == "function" && (!values || values.length == 0)) {
            callback(closestnode);
        }

        
    });
    
}

/* NODE VOLUME CALCULATION METHODS, INTERNAL */

function processNodeVolumes(closestnodes,callback){
        nodeVolumes = [];
        //console.log("processNodeVolumes" + JSON.stringify(closestnodes));
        // populate nodeVolumes with all nodes with vol set to 0
        for (var i = allNodes.length - 1; i >= 0; i--) {            
            nodeVolumes[allNodes[i].nodeId] = { nodeId : allNodes[i].nodeId, roomId : allNodes[i].roomId, vol : 0};            
        };
        
        // for every node that a client is found close to, set the vol to 100
        for (var i = closestnodes.length - 1; i >= 0; i--) {
            if (nodeVolumes[closestnodes[i].nodeId]) {
                nodeVolumes[closestnodes[i].nodeId].vol = 100;                    
            }

        };

        nodeVolumes.splice(0,1); // remove extra null value at head of array
       
        // res.json(nodeVolumes);
        if (typeof(callback) == "function") {
            callback(nodeVolumes);
        }


}


/* NODE VOLUME EXPORTED METHODS */

exports.getNodeVolumesCached = function(req,res) {    
        res.json(nodeVolumes);
};

// pre calculate the volumes of each node
// any client seen in any room in the past N measurements
// NOT USED NOW see calculateNodeVolumesPowerThreshold instead
exports.calculateNodeVolumes = function() {
    // msm.medianMatrixValues msm.rawMatrixValues
    getClosestNodeForEachClientInternal(allNodes, allUsers, msm.rawMatrixValues, function(closestnodes){
        processNodeVolumes(closestnodes); // don't return anything, just store array in mem        
    });
}



exports.getNodeVolumesDbMedian = function(req,res) {
    // getClosestNodeForEachClientInternal(nodes,users,nodeClientValueFunc,callback)
    getClosestNodeForEachClientInternal(allNodes, allUsers, medianValues, function(closestnodes){
        processNodeVolumes(closestnodes, function(nodeVolumes){
            res.json(nodeVolumes);
        });        
    });
 
};

exports.getNodeVolumesMemMedian = function(req,res) {
    // getClosestNodeForEachClientInternal(nodes,users,nodeClientValueFunc,callback)
    getClosestNodeForEachClientInternal(allNodes, allUsers, msm.medianMatrixValues, function(closestnodes){
        processNodeVolumes(closestnodes, function(nodeVolumes){
            res.json(nodeVolumes);
        });        
    });
 
};

exports.getNodeVolumesDbRaw = function(req,res) {
    // getClosestNodeForEachClientInternal(nodes,users,nodeClientValueFunc,callback)
    getClosestNodeForEachClientInternal(allNodes, allUsers, rawPowerValues, function(closestnodes){
        processNodeVolumes(closestnodes, function(nodeVolumes){
            res.json(nodeVolumes);
        });        
    });
 
};

exports.getNodeVolumesMemRaw = function(req,res) {
    // getClosestNodeForEachClientInternal(nodes,users,nodeClientValueFunc,callback)
    getClosestNodeForEachClientInternal(allNodes, allUsers, msm.rawMatrixValues, function(closestnodes){
        processNodeVolumes(closestnodes, function(nodeVolumes){
            res.json(nodeVolumes);
        });        
    });
 
};

// returns test values for node volumes
exports.getNodeVolumesTest = function(req,res) { 
    console.log("getNodeVolumesTest called");
    nodeVolumesTest = [];
    d = new Date();
    ticks = d.getTime().toString();
    ticks = ticks.substring(ticks.length - 4);
    ticksInt = parseInt(ticks,10);

    if (ticksInt > 5000) {
        nodeVolumesTest.push({nodeId : 1, roomId : 1, vol : 100});
        nodeVolumesTest.push({nodeId : 2, roomId : 2, vol : 0});
    } else if (ticksInt <= 5000) {        
        nodeVolumesTest.push({nodeId : 1, roomId : 1, vol : 0});
        nodeVolumesTest.push({nodeId : 2, roomId : 2, vol : 100});
    }

    res.json(nodeVolumesTest);
    
};

// power value thresholded calculation metod
exports.calculateNodeVolumesPowerThreshold = function(callback) {
// pre calculate the volumes of each node
// 1. Initialize system by selecting closest node to client as playing (based on getClosestNodeForEachClientInternal).
// 2. After that initalization, each call to this function compares raw value of currently playing node/client pair with the 
//    closest value for that pair.
// 3. If the closest power value is considerably higher than of the currently playing node, assign closest value as currently 
//    playing node.

    
    if (!anyNodeCurrentlyPlaying) { // no node is playing, lets select the closest one to our client
        // call calculateNodeVolumes
        getClosestNodeForEachClientInternal(allNodes, allUsers, msm.rawMatrixValues, function(closestnodes){

            nodeVolumes = [];
        
            for (var i = allNodes.length - 1; i >= 0; i--) {            
                nodeVolumes[allNodes[i].nodeId] = { nodeId : allNodes[i].nodeId, roomId : allNodes[i].roomId, vol : 0};            
            };
            console.log("closestnodes.length " + closestnodes.length);
            // for every node that a client is found close to, set the vol to 100
            for (var i = closestnodes.length - 1; i >= 0; i--) {
                if (nodeVolumes[closestnodes[i].nodeId]) {
                    nodeVolumes[closestnodes[i].nodeId].vol = 100;   

                    var closestnode = closestnodes[i];
                    d = new Date();
                    ticks = d.getTime() - closestnode.ticks;
                    closestnode.seconds = ticks/1000;
                    console.log("Selected node " + closestnode.nodeId);
                    // set the nodePlayingListThreshold, a list of currently playing nodes/client pairs along with the power
                    nodePlayingListThreshold.push(closestnode);
                    //  set anyNodeCurrentlyPlaying = true;  
                    anyNodeCurrentlyPlaying = true;                 
                }

            };

            nodeVolumes.splice(0,1); // remove extra null value at head of array 
            if (typeof(callback) == "function") {
                callback(nodeVolumes);
            }               
        });              
        
        
    } else { // we have at least one playing node
        console.log("anyNodeCurrentlyPlaying");
        // get measurements for these currently playing node/client pairs
         getClosestNodeForEachClientInternal(allNodes, allUsers, msm.rawMatrixValues, function(closestnodes){
            nodeVolumes = [];
            var tempNodePlayingList = nodePlayingListThreshold;
            nodePlayingListThreshold = [];
            for (var i = allNodes.length - 1; i >= 0; i--) {            
                nodeVolumes[allNodes[i].nodeId] = { nodeId : allNodes[i].nodeId, roomId : allNodes[i].roomId, vol : 0};            
            };

            //  access nodes currently playing and for which clients are they playing
            //  compare the raw value of this pair to the closest node pair 
            // compare each client's current closest node to the playing node
            for (var j = closestnodes.length - 1; j >= 0; j--) {
                
            
                for (var k = tempNodePlayingList.length - 1; k >= 0; k--) {
                    

                    // get the newest value for this node
                    msm.getNewestNode(tempNodePlayingList[k].mac, tempNodePlayingList[k].nodeId, function(newestNode) {                    

                        if (tempNodePlayingList[k].mac == closestnodes[j].mac) {
                            // the closest node is NOT the currently playing node
                            if (tempNodePlayingList[k].nodeId != closestnodes[j].nodeId) {

                                // if not the same node, we need to compare power readings
                               
                                var closestPower = closestnodes[j].power;
                                var playingPower = tempNodePlayingList[k].power;
                                var powerDiff = closestPower - playingPower;                        

                                if (powerDiff > 6) {
                                    console.log("THRESHOLD PASSED - new node is #" + closestnodes[j].nodeId);
                                    // closest node has 15 higher value, set its vol to 100.
                                    nodeVolumes[closestnodes[j].nodeId].vol = 100;
                                    // push it to nodePlayingList
                                    nodePlayingListThreshold.push(closestnodes[j]);   
                                }
                                else { 
                                    console.log("THRESHOLD HOLDING, still at node #" + tempNodePlayingList[k].nodeId);
                                    // keep playing from currently playing node
                                    // get the current value of currently playing node

                                    // the "if" here is a fix, because if power from some nodes are equal, we get more than 1 closestnode for one mac.
                                    // so if we have already set nodeVolumes, no need to do it again and add to nodePlayingList
                                    if (nodeVolumes[tempNodePlayingList[k].nodeId].vol == 0) {

                                        
                                            // set vol to 100 for current node
                                            nodeVolumes[newestNode.nodeId].vol = 100; 
                                            // push the current value of currently playing node to nodePlayingList                                
                                            nodePlayingListThreshold.push(newestNode);                                          
                                    }                                   
                                }
                            }
                            // the closest node is the currently playing node
                            else if (tempNodePlayingList[k].nodeId == closestnodes[j].nodeId) {
                                 // update the nodePlayingListThreshold with the current measurement of the playing node
                                 nodeVolumes[closestnodes[j].nodeId].vol = 100;
                                 // push it to nodePlayingList
                                 nodePlayingListThreshold.push(closestnodes[j]); 
                            }
                        }

                    });

                };
            };                
            nodeVolumes.splice(0,1); // remove extra null value at head of array                   
            if (typeof(callback) == "function") {
                callback(nodeVolumes);
            }

        });        
    }
}

/* RAW OUTPUTS FROM VARIOUS CALCULATION METHODS */

exports.getAllMedianValues = function(req,res) {
    medianValues(allNodes,allUsers,function(values){
        res.json(values);
    });
};

exports.getAllMedianValuesImproved = function(req,res) {
    medianValuesImproved(allNodes,allUsers,function(values){      
        res.json(values);
    });
};



exports.getClosestNodeRaw = function(req, res) {
    var clientMac = req.params.mac;
    //console.log("raw mac " + clientMac);
    getClosestNodeRawInternal(clientMac, function (closestnode){           
        res.json(closestnode);        
    });
   
};



exports.getClosestNodeMedian = function(req, res) {
    var clientMac = req.params.mac;

    getClosestNodeMedianInternal(clientMac, function (closestnode){              
        res.json(closestnode);        
    });  
};

exports.getClosestNodeThreshold = function(req, res) {
    var clientMac = req.params.mac;
    //console.log("getClosestNodeThreshold nodePlayingList.length " + nodePlayingListThreshold.length);

    if (nodePlayingListThreshold.length == 0) {
        res.json({});
    }
    else {
        // extract the measurement for the given client mac from currently playing list of nodes nodePlayingListThreshold
        for (var i = nodePlayingListThreshold.length - 1; i >= 0; i--) {
           if (nodePlayingListThreshold[i].mac == clientMac) {
            res.json(nodePlayingListThreshold[i]); 
           }
           else {
            res.json({});
           }
        };
    }
               
    
};

exports.getClosestNodeAvg = function(req, res) {
    var clientMac = req.params.mac;
    getClosestNodeAveragedInternal(clientMac, function (closestnode){              
        res.json(closestnode);        
    });   
};

exports.getClosestNodeNaive = function(req, res) {
    var clientMac = req.params.mac;
    getMostRecentClosestNodeNaive(clientMac, function (closestnode){           
        res.json(closestnode);        
    });
   
};


exports.getClosestNodeForEachClient =  function(req,res) {
    getClosestNodeForEachClientInternal(allNodes,allUsers,function(closestnodes){
        res.json(closestnodes);
    });
}


/// STATISTICS ABOUT SYSTEM

// param: roomId
exports.getClientsInRoom = function(req,res) {
    nodesInRoom = [];
    
    for (var i = allNodes.length - 1; i >= 0; i--) {
        if (allNodes[i].roomId == req.params.roomId) {
            nodesInRoom.push(allNodes[i]);
        }
    };

    getClientsForNodes(nodesInRoom,allUsers, function(clientsInRoom){
        res.json(clientsInRoom);
    });
};

exports.getClientsAllRooms = function(req,res) {
    getClientsForNodes(allNodes,allUsers, function(clientsInRoom){
        res.json(clientsInRoom);
    });
};




/// BENCHMARKING STUFF

exports.benchMedians = function(req,res) {      
    d1 = new Date();
    (function(d1){
        getClosestNodeForEachClientInternal(allNodes, allUsers, msm.medianMatrixValues, function(closestnodes){
            processNodeVolumes(closestnodes,function(values){
                d2 = new Date();
                ticks2 = d2.getTime();
                ticks1 = d1.getTime();
                res.send((ticks2 - ticks1) + " ms");
            });        
        });
    })(d1);  

};

exports.benchThreshold = function(req,res) {  

    
    d1 = new Date();
    (function(d1){
        exports.calculateNodeVolumesPowerThreshold(function(nodeVolumes){
            d2 = new Date();
            ticks2 = d2.getTime();
            ticks1 = d1.getTime();
            res.send((ticks2 - ticks1) + " ms");
        });
    })(d1);  

};

exports.benchRaw = function(req,res) {          
    d1 = new Date();
    (function(d1){
        getClosestNodeForEachClientInternal(allNodes, allUsers, msm.rawMatrixValues, function(closestnodes){
            processNodeVolumes(closestnodes,function(values){
                d2 = new Date();
                ticks2 = d2.getTime();
                ticks1 = d1.getTime();
                res.send((ticks2 - ticks1) + " ms");
            });        
        });
    })(d1);  

};



