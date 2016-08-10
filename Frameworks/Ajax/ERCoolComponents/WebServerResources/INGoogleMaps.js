/** INGoogleMaps.js
 * @author Amedeo Mantica
 * @copyright Amedeo Mantica
 * @version 0.2
 *
 * INGoogleMaps is an Utility collection to make the use of Google Maps very easy
 *
 * First of all you need an API KEY
 * Look at: https://developers.google.com/maps/documentation/javascript/tutorial#api_key
 * Then you will insert a tiny script in the page: head or body is not important
 *
 * <script type="text/javascript">
 *     var in_googleApiKey = "YOUR_GOOGLE_KEY";
 * </script>
 *
 *
 * DECLARING A MAP:
 *
 * To create a Map you have to declare a <div> element that must have class "in_GoogleMap",
 * the dimensions must be set (via style or css declaration)
 * these parameters are required too:
 *
 * @param data-zoom //number, specifies the map initial zoom level
 * @param data-type //text, specifies the type of map for initial view, see MapTypes below
 * @param data-lat //number, the center of map, latitude, decimal value
 * @param data-lng //number, the center of map, longitude, decimal value
 *
 * Map Types:
 * ROADMAP displays the normal, default 2D tiles of Google Maps.
 * SATELLITE displays photographic tiles.
 * HYBRID displays a mix of photographic tiles and a tile layer for prominent features (roads, city names).
 * TERRAIN displays physical relief tiles for displaying elevation and water features (mountains, rivers, etc.).
 *
 *
 * SIMPLE MARKERS:
 *
 * To create a marker you should create an <address> element and set a few parameters:
 * the <address> element can be created anywhere in the page!
 *
 * @param data-in_GoogleMap-id //this is very important, you must specify here the map id where you want to show the marker
 * @param data-lat //number, coordinate, latitude of the marker, decimal value
 * @param data-lng //number, coordinate, longitude of the marker, decimal value
 * @param data-draggable //boolean, true or false, if true the marker will be draggable, useful for input forms, see below
 *
 * inside the <address> tag you may put any html. that html will be shown in a balloon when the user clicks on the marker
 *
 *
 * INPUT FORMS:
 *
 * This is the reason why I started making this utility.
 * I had needs to create an address input form and the need to store coordinates too.
 * To do this you need to input fields, one for latitude, and one for longitude
 *
 * You can create two <input> tags (anywhere in the page!), you have to set their class to "in_InputField" and set these parameters:
 *
 * @param data-in_bound-object //this is very important, you must specify here the <address> id linked to this input
 * @param data-field // must be set respectively one to "lat" and one to "lng"
 *
 * if you have set the marker as draggable (@param data-draggable, see above) the values of these field will dynamically update
 *
 *
 * FULL EXAMPLE:
 *
 * <html>
 *     <head>
 *         .
 *         .
 *         .
 *          <script type="text/javascript">
 *              var in_googleApiKey = "YOUR_GOOGLE_KEY";
 *          </script>
 *         .
 *         .
 *         .
 *          <style>
 *              #myMap {
 *                  width: 700px;
 *                  height: 500px;
 *              }
 *          </style>
 *          .
 *          .
 *          .
 *      </head>
 *      <body>
 *          .
 *          .
 *          <div id="myMap" class="in_GoogleMap" data-mode="view" data-zoom="14" data-type="ROADMAP" data-lat="42.464278" data-lng="14.214189"></div>
 *          .
 *
 *          <address id="address1" class="in_GoogleMapMarker" data-in_GoogleMap-id="myMap" data-lat="42.47919030235361" data-lng="14.194427208770776" data-draggable="false">
 *              <p>Amedeo Mantica</p>
 *              <p>Via Tintoretto, 7</p>
 *              <p>65124 Pescara (PE)</p>
 *          </address>
 *          .
 *          .
 *          <address id="address2" class="in_GoogleMapMarker" data-in_GoogleMap-id="myMap" data-lat="42.45913615672084" data-lng="14.21304710375216" data-draggable="true">
 *               <p>Insigno Comunicazione srl</p>
 *               <p>Via Orazio, 146</p>
 *               <p>65128 Pescara (PE)</p>
 *          </address>
 *          .
 *          .
 *          .
 *          <input class="in_InputField" type="text" data-in_bound-object="address2" data-field="lat" />
 *          <input class="in_InputField" type="text" data-in_bound-object="address2" data-field="lng" />
 *          .
 *          .
 *          .
 *      </body>
 * </html>
 *
 */

window.IN_isIE = false;
window.IN_GoogleMaps = Array();

var IN_initializeMaps = function() {
    var i;
	var divElements = document.getElementsByTagName("div");
    for ( i = 0 ; i < divElements.length ; i++ ) {
        if (divElements[i].className.match(new RegExp('(\\s|^)'+ 'in_GoogleMap' +'(\\s|$)'))) {
            window.IN_GoogleMaps[ divElements[i].id ] = new INGoogleMap(divElements[i]);
        }
    }

    var addMarkerToMap = function(newMarker) {
        var targetMapId = newMarker.getAttribute('data-in_GoogleMap-id');
        if(window.IN_GoogleMaps[targetMapId]) {
            window.IN_GoogleMaps[targetMapId].addMarker(newMarker);
        }
    }

    var googleMapMarkers = Array();
    var addressElements = document.getElementsByTagName("address");
    for ( i = 0 ; i < addressElements.length ; i++ ) {
        if (addressElements[i].className.match(new RegExp('(\\s|^)'+ 'in_GoogleMapMarker' +'(\\s|$)'))) {
            addMarkerToMap(addressElements[i]);
        }
    }
}

/** Called after DOM finish loading, will append google scripts in the body
 */
function IN_setupGoogleMaps() {
    var apiKey = in_googleApiKey;
    var script = document.createElement("script");
    script.type = "text/javascript";

    // Ask Google to call IN_initializeMaps() after loading
    script.src = document.location.protocol + "//maps.googleapis.com/maps/api/js?key=" + apiKey + "&sensor=false&callback=IN_initializeMaps";
    document.body.appendChild(script);
}

/**
 *
 * @param element the div element that will contain the googlemap
 * @constructor INGoogleMap
 */
var INGoogleMap = function(element) {

    var mapType = Array();
    mapType["ROADMAP"] = google.maps.MapTypeId.ROADMAP;
    mapType["SATELLITE"] = google.maps.MapTypeId.SATELLITE;
    mapType["HYBRID"] = google.maps.MapTypeId.HYBRID;
    mapType["TERRAIN"] = google.maps.MapTypeId.TERRAIN;
    /** We parse attributes of the div element and will put data into the mapOptions Object that will be passed to the
     * google.maps.Map constructor
     * @type {Object}
     */

    var mapOptions = {
        zoom: parseInt(element.getAttribute("data-zoom")),
        center: _mapCenter,
        mapTypeId: mapType[element.getAttribute("data-type")]
    }

    /** Ask google to render the map
     * We store the map object in this._map
     * @type {google.maps.Map}
     * @private
     */
    this._map = new google.maps.Map(element, mapOptions);
    var _map = this._map;

    if ( (element.getAttribute("data-lat"))==null || (element.getAttribute("data-lng")==null) ) {
        //lat or lng not available, looking for address string
        var addressString = element.getAttribute("data-address");
        if(addressString == null) {
            alert('Unable to load map, there is no Latitude, Longitude or Address specified');
        } else {
            var geocoder = new google.maps.Geocoder();
            geocoder.geocode( { 'address' : addressString }, function(results, status) {
                if(status == 'OK') {
                    var _mapCenter = new google.maps.LatLng(results[0].geometry.location.lat(), results[0].geometry.location.lng());
                    _map.setCenter(_mapCenter);
                } else {
                    alert('Unable to lookup address')
                }
            });
        }
    } else {
        var _mapCenter = new google.maps.LatLng(element.getAttribute("data-lat"), element.getAttribute("data-lng"));
        _map.setCenter(_mapCenter);
    }

}

INGoogleMap.prototype = {

    /**
     *
     * @param this is Our address element, non the google marker object
     */
    addMarker: function(marker) {

        /* We parse the attribute values from the address object */
        var title = marker.getAttribute("data-title");
        var draggable = marker.getAttribute("data-draggable") === "true" ? true : false;
        var _map = this._map;
        var addressHTML = marker.innerHTML;

        /**
         * Create a marker on the map position at 0,0, we will reposition the marker immediately after creation
         * @type {google.maps.Marker}
         */
        var googleMarker = new google.maps.Marker({
            position: new google.maps.LatLng(0,0),
            map: this._map,
            title: title,
            draggable: draggable
        });


        if ( (marker.getAttribute("data-lat") == null) || (marker.getAttribute("data-lng") == null) ) {
            //lat or lng not available, looking for address string
            var addressString = marker.getAttribute("data-address");
            if(addressString == null) {
                alert('Unable to load marker, there is no Latitude, Longitude or Address specified');
                googleMarker.setMap(null);
                googleMarker = null; //Destroy the marker
            } else {
                var geocoder = new google.maps.Geocoder();

                geocoder.geocode( { 'address' : addressString }, function(results, status) {
                    if(status == 'OK') {
                        googleMarker.setPosition(results[0].geometry.location);
                        //_map.setCenter(results[0].geometry.location);
                        marker.setAttribute("data-lng",results[0].geometry.location.lng());
                        marker.setAttribute("data-lat",results[0].geometry.location.lat());
                    }
                });
            }
        } else {
            var latlng = new google.maps.LatLng(marker.getAttribute("data-lat"),marker.getAttribute("data-lng"));
            googleMarker.setPosition(latlng);
            //_map.setCenter(latlng);
        }

        if(googleMarker != null) {

            var latInput;
            var lngInput;

            var allInputs = document.getElementsByTagName("input");

            for(var i=0 ; i < allInputs.length ; i++) {
                if (allInputs[i].className.match(new RegExp('(\\s|^)'+ 'in_InputField' +'(\\s|$)'))) {
                    if(allInputs[i].getAttribute("data-in_bound-object")) {
                        boundObjectId = allInputs[i].getAttribute("data-in_bound-object");
                        if(document.getElementById(boundObjectId)) {
                            boundObject = document.getElementById(boundObjectId);
                            if( ( allInputs[i].getAttribute("data-field") === "lng") && (allInputs[i].getAttribute("data-in_bound-object") === marker.id ) ) {
                                lngInput = allInputs[i];
                                lngInput.value = marker.getAttribute("data-lng");
                            }
                            if( ( allInputs[i].getAttribute("data-field") === "lat") && (allInputs[i].getAttribute("data-in_bound-object") === marker.id ) ) {
                                latInput = allInputs[i];
                                latInput.value = marker.getAttribute("data-lat");
                            }
                        }
                    }
                }
            }


            if(draggable) {

                var markerDidMove = function() {
                    var newLng = googleMarker.position.lng();
                    var newLat = googleMarker.position.lat();

                    if(lngInput) {
                        lngInput.value = newLng;
                    }
                    if (latInput) {
                        latInput.value = newLat;
                    }

                    marker.setAttribute("data-lng",newLng);
                    marker.setAttribute("data-lat",newLat);
                }

                google.maps.event.addListener(googleMarker, 'drag', markerDidMove);

            } else {

                if(addressHTML) {
                    if(addressHTML!="") {
                        marker.infowindow = new google.maps.InfoWindow({
                            content: addressHTML
                        });

                        google.maps.event.addListener(googleMarker, 'click', function() {
                            marker.infowindow.open(_map,googleMarker);
                        });

                    }
                }
            }

            marker.lookupAddress = function(addressData) {
                var geocoder = new google.maps.Geocoder();

                geocoder.geocode( { 'address' : addressData }, function(results, status) {
                    if(status == 'OK') {
                        googleMarker.setPosition(results[0].geometry.location);
                        if(markerDidMove) {
                            markerDidMove();
                        }
                        _map.setCenter(results[0].geometry.location);
                    }
                });
            }

            marker.locateOnMap = function() {
                var latlng = new google.maps.LatLng(marker.getAttribute("data-lat"),marker.getAttribute("data-lng"));
                _map.setCenter(latlng);
                _map.setZoom(18);
                marker.infowindow.open(_map,googleMarker);
            }
        }

    }

}


if (document.addEventListener) {
    document.addEventListener("DOMContentLoaded", IN_setupGoogleMaps, false);
} else if (document.attachEvent) {
    window.IN_isIE = true;
    document.attachEvent("onreadystatechange", function() {
        if ( document.readyState === "complete" ) {
            IN_setupGoogleMaps();
        }
    });

}