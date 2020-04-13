let n = "N";
let s = "S";
let e = "E";
let w = "W";

let ns = "NS";
let ew = "EW";

let homePos = new google.maps.LatLng(37.748857, -122.507248);

google.maps.visualRefresh = true;
let map;


function initialize() {
    // MapTypeId.ROADMAP
    // MapTypeId.SATELLITE
    // MapTypeId.HYBRID
    // MapTypeId.TERRAIN
    map = new google.maps.Map(document.getElementById('map-canvas'), {
        center: homePos,
        zoom: 2,
        mapTypeId: google.maps.MapTypeId.HYBRID,
        mapTypeControlOptions: {
            style: google.maps.MapTypeControlStyle.DROPDOWN_MENU,
            mapTypeIds: [
                google.maps.MapTypeId.SATELLITE,
                google.maps.MapTypeId.ROADMAP,
                google.maps.MapTypeId.HYBRID,
                google.maps.MapTypeId.TERRAIN ]
        }
    });
    map.setTilt(45);
}
google.maps.event.addDomListener(window, 'load', initialize);

