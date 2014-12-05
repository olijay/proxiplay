$(document).ready(function () {
    $('#dropUsers').on('click',function () {
        if (confirm('Are you sure you want to drop the db?')) {
           window.location = "http://"+window.location.host+"/users/dropusers";
        }
    });
    $('#dropMeasurements').on('click',function () {
        if (confirm('Are you sure you want to drop the db?')) {
           window.location = "http://"+window.location.host+"/msm/dropmsm";
        }
    });
     $('#dropNodes').on('click',function () {
        if (confirm('Are you sure you want to drop the db?')) {
           window.location = "http://"+window.location.host+"/nodes/dropnodes";
        }
    });
      $('#dropEdges').on('click',function () {
        if (confirm('Are you sure you want to drop the db?')) {
           window.location = "http://"+window.location.host+"/nodes/dropedges";
        }
    });
    $('#seeUsers').on('click',function () {
        window.location = "http://"+window.location.host+"/users/users";
    });
    $('#seeMsms').on('click',function () {
        window.location = "http://"+window.location.host+"/msm/msm";
    });
    $('#seeNodes').on('click',function () {
        window.location = "http://"+window.location.host+"/nodes/nodes";
    });
    $('#seeEdges').on('click',function () {
        window.location = "http://"+window.location.host+"/nodes/edges";
    });
});

