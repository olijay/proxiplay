var sqlite3 = require('sqlite3').verbose();
exports.deleteFromTable = function(tableName, callback) {
	console.log("Deleting data from table " + tableName);
	var db = new sqlite3.Database('masternode.db');     
    db.exec("DELETE FROM " + tableName, callback);  
    db.close(); 
};