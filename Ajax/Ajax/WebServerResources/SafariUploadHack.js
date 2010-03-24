// (Aaron) Safari 4 has a bug discussed in many places on the web and solutions 
// are cross posted throughout. Evidentally the "keepAlive" Apache session must be closed 
// before a multipart submit can take place. A nice succinct description and solution is posted here: 
// http://blog.airbladesoftware.com/2007/8/17/note-to-self-prevent-uploads-hanging-in-safari 
// 
// The radar ticket is here: 
// https://bugs.webkit.org/show_bug.cgi?id=5760 
// 
/* A pretty little hack to make uploads not hang in Safari. Just call this 
 * immediately before the upload is submitted. This does an Ajax call to 
 * the server, which returns an empty document with the "Connection: close" 
 * header, telling Safari to close the active connection. A hack, but 
 * effective. */ 
function erxCloseKeepAlive() { 
    if (/AppleWebKit/.test(navigator.userAgent)) { 
        var fullHref = document.location.href; 
        var indexOfWO = fullHref.indexOf("/wo/"); 
        var partialHref = fullHref.substring(0, indexOfWO); 
        var closeHref = partialHref + "/wa/closeHTTPSession"; 
        
        // we want "synchronous" communication 
        var isAsync = false; 
        
        var dummyBody = "dummy"; 
        var req = new XMLHttpRequest();   
        req.open("post", closeHref, isAsync);   
    	req.setRequestHeader("Content-Type", "multipart/form-data"); 
    	req.setRequestHeader("If-Modified-Since", "Mon, 26 Jul 1997 05:00:00 GMT"); 
    	req.setRequestHeader("Cache-Control", "no-cache"); 
    	req.setRequestHeader("X-Requested-With", "XMLHttpRequest"); 
    	req.send(dummyBody); 
        req.responseText; 
    } 
} 

// Function to add "closeKeepAlive" Safari hack to each multipart/mime form. 
// More specifically, to all the "submit" buttons of each multipart/mime (binary upload) form. 
function erxAttachCloseKeepAliveToMultipartForms() { 
    if (/AppleWebKit/.test(navigator.userAgent)) { 
        $$('form[enctype="multipart/form-data" ]').each(function(uploadForm) { 
            // when you click a button, immediately close the http session 
            var formElems = uploadForm.elements; 
            for (var i = 0; i < formElems.length; i++) { 
                var formFieldIterator = formElems[i]; 
                if (formFieldIterator.type == 'submit' && ! formFieldIterator.erxCloseKeepAliveFlag) { 
                    formFieldIterator.erxCloseKeepAliveFlag = true; 
                    Event.observe(formFieldIterator, 'mousedown', erxCloseKeepAlive); 
                } 
            }                                                     
        }); 
    } 
} 

// invoke the above method after the dom has finished loading. Only do it for Safari. 
if (/AppleWebKit/.test(navigator.userAgent)) { 
    Event.observe(document, 'dom:loaded', erxAttachCloseKeepAliveToMultipartForms); 
}

// These few lines of code to add mousedown events to submit buttons that have been redrawn 
// due to refreshes of an AjaxUpdateContainer
erxCloseKeepAliveResponder = { 
    onComplete: function(request, transport) { 
        // For redrawn submit buttons, re-attach the mousedown listeners 
        erxAttachCloseKeepAliveToMultipartForms(); 
    } 
} 

Ajax.Responders.register(erxCloseKeepAliveResponder);