function showAddress(map, address, zoom) {
  var geocoder = new GClientGeocoder();
  geocoder.getLatLng(
    address,
    function(point) {
      if (!point) {
        alert(address + " not found");
      } else {
        map.setCenter(point, zoom);
      }
    }
  );
}