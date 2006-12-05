Object.extend(Form, {
  serializeWithoutSubmits: function(form) {
    var elements = Form.getElements($(form));
    var queryComponents = new Array();

    for (var i = 0; i < elements.length; i++) {
			if (elements[i].type != 'submit') {
	      var queryComponent = Form.Element.serialize(elements[i]);
	      if (queryComponent) {
	        queryComponents.push(queryComponent);
				}
			}
    }

    return queryComponents.join('&');
  }
});