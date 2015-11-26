(function($) {
	$.fn.initSelectFilter = function(pb_id) {
		var filter_menu = '#' + pb_id + '-menu';
		var filter_listbox = '#' + pb_id + '-listbox';
		var filter_form = pb_id + '-form';
		var filter_dialog = '#' + pb_id + '-dialog';
	    $.mobile.document
	    // "filter-menu" is the ID generated for the listview when it is created
	    // by the custom selectmenu plugin. Upon creation of the listview widget we
	    // want to prepend an input field to the list to be used for a filter.
	    .on( "listviewcreate", filter_menu, function( e ) {
	        var input,
	            listbox = $( filter_listbox ),
	            form = listbox.jqmData( filter_form ),
	            listview = $( e.target );
	        // We store the generated form in a variable attached to the popup so we
	        // avoid creating a second form/input field when the listview is
	        // destroyed/rebuilt during a refresh.
	        if ( !form ) {
	            input = $( "<input data-type='search'></input>" );
	            form = $( "<form></form>" ).append( input );
	            input.textinput();
	            $( filter_listbox )
	                .prepend( form )
	                .jqmData( filter_form, form );
	        }
	        // Instantiate a filterable widget on the newly created listview and
	        // indicate that the generated input is to be used for the filtering.
	        listview.filterable({ input: input });
	    })
	    // The custom select list may show up as either a popup or a dialog,
	    // depending how much vertical room there is on the screen. If it shows up
	    // as a dialog, then the form containing the filter input field must be
	    // transferred to the dialog so that the user can continue to use it for
	    // filtering list items.
	    //
	    // After the dialog is closed, the form containing the filter input is
	    // transferred back into the popup.
	    .on( "pagebeforeshow pagehide", filter_dialog, function( e ) {
	        var form = $( filter_listbox ).jqmData( filter_form ),
	            placeInDialog = ( e.type === "pagebeforeshow" ),
	            destination = placeInDialog ? $( e.target ).find( ".ui-content" ) : $( filter_listbox );
	        form
	            .find( "input" )
	            // Turn off the "inset" option when the filter input is inside a dialog
	            // and turn it back on when it is placed back inside the popup, because
	            // it looks better that way.
	            .textinput( "option", "inset", !placeInDialog )
	            .end()
	            .prependTo( destination );
	    });
	};
})(jQuery);