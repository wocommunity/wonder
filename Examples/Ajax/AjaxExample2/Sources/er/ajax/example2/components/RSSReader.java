package er.ajax.example2.components;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;
import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

public class RSSReader extends AjaxWOWODCPage {
  private NSMutableArray<FeedWrapper> _feeds;
  public FeedWrapper _repetitionFeed;
  public FeedWrapper _selectedFeed;
  public SyndEntry _repetitionEntry;
  public SyndEntry _selectedEntry;

  public RSSReader(WOContext context) {
    super(context);
    _feeds = new NSMutableArray<>();
    _feeds.addObject(new FeedWrapper("Project Wonder", "http://projectwonder.blogspot.com/feeds/posts/default"));
    _feeds.addObject(new FeedWrapper("WOLips", "http://feeds.feedburner.com/wolips"));
    _feeds.addObject(new FeedWrapper("Miguel Arroz", "http://terminalapp.net/feed/"));
    _feeds.addObject(new FeedWrapper("Kieran Kelleher", "http://homepage.mac.com/kelleherk/iblog/rss.xml"));
    _feeds.addObject(new FeedWrapper("David Leber", "http://davidleber.net/?feed=atom"));
    _feeds.addObject(new FeedWrapper("Mike Schrag", "http://mschrag.blogspot.com/feeds/posts/default"));
  }
  
  @Override
  protected boolean useDefaultComponentCSS() {
    return true;
  }
  
  public WOActionResults feedSelected() {
    _selectedEntry = null;
    return null;
  }

  public NSArray<FeedWrapper> feeds() {
    return _feeds;
  }

  public String feedEntryContents() {
    String feedEntryContents = null;
    if (_selectedEntry != null) {
      List contents = _selectedEntry.getContents();
      if (!contents.isEmpty()) {
        SyndContent content = (SyndContent) contents.get(0);
        feedEntryContents = content.getValue();
      }
    }
    return feedEntryContents;
  }

  public static class FeedWrapper {
    private String _name;
    private String _url;
    private SyndFeed _syndFeed;

    public FeedWrapper(String name, String url) {
      _name = name;
      _url = url;
    }

    public String name() {
      return _name;
    }

    public String url() {
      return _url;
    }

    @SuppressWarnings("unchecked")
    public NSArray entries() throws IllegalArgumentException, IOException, FeedException {
      return new NSArray<>(syndFeed().getEntries());
    }

    public SyndFeed syndFeed() throws IOException, IllegalArgumentException, FeedException {
      if (_syndFeed == null) {
        URL feedUrl = new URL(_url);
        HttpURLConnection feedConn = (HttpURLConnection) feedUrl.openConnection();
        feedConn.addRequestProperty("User-Agent", "Mozilla/5.001 (windows; U; NT4.0; en-us) Gecko/25250101");
        SyndFeedInput input = new SyndFeedInput();
        XmlReader reader = new XmlReader(feedConn);
        SyndFeed syndFeed;
        try {
          syndFeed = input.build(reader);
        }
        finally {
          reader.close();
        }
        _syndFeed = syndFeed;
      }
      return _syndFeed;
    }
  }
}