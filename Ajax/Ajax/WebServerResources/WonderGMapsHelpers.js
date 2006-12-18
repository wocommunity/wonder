function showAddress(map, address, zoom) {
  var geocoder = new GClientGeocoder();
  geocoder.getLatLng(
    address,
    function(point) {
      if (!point) {
        alert(address + " not found");
      } else {
        map.setCenter(point, zoom);
        var mapMarker = new GMarker(point);
        map.addOverlay(mapMarker);
        mapMarker.openInfoWindowHtml(address);
       	GEvent.addListener(mapMarker, "click", function() {
					map.closeInfoWindow();
			    mapMarker.openInfoWindowHtml(address);
			  });
      }
    }
  );
}