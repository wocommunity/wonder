var iBoxPlugin_Vimeo = function()
{
  var _private = {
    // http://www.vimeo.com/1604120
    url: /(?:http:\/\/)?(?:www\.)?vimeo\.com\/(\d+).*/
  }

  var _public = {
    match: function(url)
    {
      return url.match(_private.url);
    },

    render: function(url, params)
    {
      id = url.match(_private.url)[1];
      params.width = 506;
      params.height = 337;
      params.constrain = true;
      
      var html = '<object width="100%" height="100%"><param name="allowfullscreen" value="true" /> <param name="allowscriptaccess" value="always" /> <param name="movie" value="http://www.vimeo.com/moogaloop.swf?clip_id=' + id + '&amp;server=www.vimeo.com&amp;show_title=0&amp;show_byline=0&amp;show_portrait=0&amp;color=00adef&amp;fullscreen=1" /> <embed src="http://www.vimeo.com/moogaloop.swf?clip_id=' + id + '&amp;server=www.vimeo.com&amp;show_title=0&amp;show_byline=0&amp;show_portrait=0&amp;color=00adef&amp;fullscreen=1" type="application/x-shockwave-flash" allowfullscreen="true" allowscriptaccess="always" width="100%" height="100%"></embed></object>';
      iBox.html(html, params);
    }
  }
  return _public;
}();
iBox.plugins.register(iBoxPlugin_Vimeo);

var iBoxPlugin_MySpace = function()
{
  var _private = {
    // http://vids.myspace.com/index.cfm?fuseaction=vids.individual&VideoID=41178745
    url: /(?:http:\/\/)?vids\.myspace\.com\/index\.cfm\?fuseaction=vids.individual&VideoID=(\d+).*/
  }

  var _public = {
    match: function(url)
    {
      return url.match(_private.url);
    },

    render: function(url, params)
    {
      id = url.match(_private.url)[1];
      params.width = 425;
      params.height = 360;
      params.constrain = true;
      
      var html = '<object width="100%" height="100%" ><param name="allowFullScreen" value="true"/><param name="movie" value="http://mediaservices.myspace.com/services/media/embed.aspx/m=' + id + ',t=1,mt=video"/><embed src="http://mediaservices.myspace.com/services/media/embed.aspx/m=' + id + ',t=1,mt=video" width="100%" height="100%" allowFullScreen="true" type="application/x-shockwave-flash"></embed></object>';
      iBox.html(html, params);
    }
  }
  return _public;
}();
iBox.plugins.register(iBoxPlugin_MySpace);