var mongo = require('mongodb');

function openDb(err,db,dbName) {
  if(!err) {
        
        console.log("Connected to "+dbName+" database");
        db.collection(dbName, {strict:false}, function(err, collection) {
            if (err) {
                console.log("Error: " + err);
            }
        });
    }
}

exports.openUsers = function(err, db) {
    openDb(err,db,'users');  
}

exports.openMeasurements = function(err, db) {
    openDb(err,db,'measurements');  
}

exports.openNodes = function(err, db) {
    openDb(err,db,'nodes');    
}