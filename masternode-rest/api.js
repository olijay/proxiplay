var http = require('http'); 
var express = require('express');  
	var users = require('./routes/users');
	var msm = require('./routes/measurements');
	var nodes = require('./routes/nodes');
var app = express();
app.use(express.json());
app.use(express.urlencoded());
app.configure(function() { app.use(express.favicon()); app.use(express['static'](__dirname ));  });
nodes.initNodesUsers(function(allNodes,allUsers){ // fill allUsers and allNodes with data from db
	msm.initMsmMatrix(allNodes, allUsers); // init the matrix of measurements
});
var volumeCalcInterval = 1000;
setInterval(nodes.calculateNodeVolumesPowerThreshold, volumeCalcInterval); // calculate the volumes of the nodes every 1sec


// stuff that changes frequently regarding configurations
app.post('/msm/msm', msm.addSSMeasurementMem);  
// addSSMeasurementDb addSSMeasurementMem
app.get('/nodes/volume', nodes.getNodeVolumesCached); 
// getNodeVolumesMemMedian getNodeVolumesDbMedian getNodeVolumesMemRaw getNodeVolumesDbRaw getNodeVolumesCached

// GET
app.get('/server/time', function(req,res){
	var d = new Date();
    var dateString = d.toISOString();
    res.send(dateString);
});
app.get('/users/users', users.getAll);  
app.get('/msm/msm', msm.getAll);  

app.get('/users/dropusers', users.dropUsers);  
app.get('/msm/dropmsm', msm.dropMeasurements); 
app.get('/nodes/dropnodes', nodes.dropNodes);  
app.get('/nodes/dropedges', nodes.dropEdges); 

app.get('/users/mac/:mac', users.findByMac);  
app.get('/users/ip/:ip', users.findByIp);  

//app.get('/nodes/closestnodenaive/:mac', nodes.getClosestNodeNaive); // obsolete

app.get('/nodes/newestnoderaw/:mac/:nodeid', msm.getNewestNodeRaw);
app.get('/nodes/closestnodemedian/:mac', nodes.getClosestNodeMedian);
app.get('/nodes/closestnoderaw/:mac', nodes.getClosestNodeRaw);
app.get('/nodes/closestnodethreshold/:mac', nodes.getClosestNodeThreshold); 
//app.get('/nodes/closestnodeavg/:mac', nodes.getClosestNodeAvg); 

app.get('/msm/mac/:mac', msm.findByMac);  

app.get('/nodes/nodes', nodes.getAllNodes);  
app.get('/nodes/edges', nodes.getAllEdges);  

app.get('/nodes/volumetest', nodes.getNodeVolumesTest); 

app.get('/nodes/rooms/:roomId', nodes.getClientsInRoom);
app.get('/nodes/rooms', nodes.getClientsAllRooms);

app.get('/nodes/medians', nodes.getAllMedianValues);
app.get('/nodes/mediansImproved', nodes.getAllMedianValuesImproved);
app.get('/nodes/closest', nodes.getClosestNodeForEachClient);

app.get('/nodes/benchmedians', nodes.benchMedians);
app.get('/nodes/benchthreshold', nodes.benchThreshold);
app.get('/nodes/benchraw', nodes.benchRaw);

//app.get('/nodes/getupdates', nodes.getUpdates); 

// POST
app.post('/users/user', users.addUser);


app.post('/nodes/nodes', nodes.addNode); 
app.post('/nodes/edges', nodes.addEdge); 


// Express route for any other unrecognised incoming requests 
app.get('*', function(req, res){ res.send('Unrecognised API call', 404); }); 

// Express route to handle errors 
app.use(function(err, req, res, next){ if (req.xhr) { res.send(500, 'Oops, Something went wrong!'); } else { next(err); } });

app.listen(3000); 
console.log('App Server running at port 3000');