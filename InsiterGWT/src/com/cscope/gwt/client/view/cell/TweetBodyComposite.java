package com.cscope.gwt.client.view.cell;

import java.util.ArrayList;
import java.util.List;

//import com.cscope.gwt.shared.NewsContents;
import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.CompositeCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.HasCell;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safecss.shared.SafeStyles;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.web.bindery.event.shared.EventBus;

import insiter.ina.news.service.web.NewsContents;

public class TweetBodyComposite {

public interface AnchorCellTemplates extends SafeHtmlTemplates {
    @Template("<a href=\"{0}\">{1}</a>")
    SafeHtml anchor(SafeUri href, String name);
  }

  static public final AnchorCellTemplates anchorCellTemplate = GWT.create(AnchorCellTemplates.class);

  // URL templates used to render the URL block
  public interface UrlTemplate extends SafeHtmlTemplates {
    //@Template("<a href=\"{0}\">{1}</a}")
    //@Template("<span class=\"align-right\"><a href=\"{0}\">{1}</a></span>")
    //@Template("<span style=\"text-align:right\"><a href=\"{0}\">{1}</a></span>")
    @Template("<span style=\"width:100%; text-align:right;\"><a href=\"{0}\">{1}</a></span>")
    SafeHtml anchor(SafeUri href, String name);
  }

  static public final UrlTemplate urlTemplate = GWT.create(UrlTemplate.class);

  /**
   * Make a CompositeCell containing the following:
   * 1) Name, ScreenName, time-elapsed & Logo
   * 2) Tweet text
   * 3) Categories, RT counters, URL
   * @return
   */
  static public CompositeCell<NewsContents> makeTweetBody(EventBus eBus) {
    // Cell components of the CompositeCell to build
    List<HasCell<NewsContents, ?>> cellComponents = new ArrayList<HasCell<NewsContents, ?>>();
    
    // Publisher's name & @screen_name with logo
    cellComponents.add(new HasCell<NewsContents, String>() {
      private NameAndLogoCell cell = new NameAndLogoCell();
  
      public Cell<String> getCell() {
        return cell;
      }
      public FieldUpdater<NewsContents, String> getFieldUpdater() {
        return new FieldUpdater<NewsContents, String>() {
          public void update(int index, NewsContents tweet, String value) {
          }
        };
      }
      public String getValue(NewsContents tweet) {
        return tweet.name + ',' + tweet.sName + ',' + tweet.timePassed;
      }
    });

    // Txt msg string
    cellComponents.add(new HasCell<NewsContents, String>() {
      private TextMsgCell cell = new TextMsgCell();
  
      public Cell<String> getCell() {
        return cell;
      }
      public FieldUpdater<NewsContents, String> getFieldUpdater() {
        return new FieldUpdater<NewsContents, String>() {
          public void update(int index, NewsContents tweet, String value) {
            //ssm.setSelected(object, isCBChecked);
          }
        };
      }
      public String getValue(NewsContents tweet) {
        return tweet.txt;
      }
    });

    // Retweet Counter string
    cellComponents.add(new HasCell<NewsContents, String>() {
      private RtCounterCell cell = new RtCounterCell();
  
      public Cell<String> getCell() {
        return cell;
      }
      public FieldUpdater<NewsContents, String> getFieldUpdater() {
        return new FieldUpdater<NewsContents, String>() {
          public void update(int index, NewsContents tweet, String value) {
          }
        };
      }
      public String getValue(NewsContents tweet) {
        return tweet.cntNRT + ':' + tweet.cntCRT + ':' + tweet.cntMRT;
      }
    });

    // SafeHtmlCell to support URL reference
    cellComponents.add(new HasCell<NewsContents, SafeHtml>() {
      private SafeHtmlCell cell = new SafeHtmlCell();

      public Cell<SafeHtml> getCell() {
        return cell;
      }
      public FieldUpdater<NewsContents, SafeHtml> getFieldUpdater() {
        return new FieldUpdater<NewsContents, SafeHtml>() {
          public void update(int index, NewsContents tweet, SafeHtml value) {
            //ssm.setSelected(object, isCBChecked);
          }
        };
      }
      public SafeHtml getValue(NewsContents tweet) {
        if (tweet.url!=null && !tweet.url.isEmpty()) {
          SafeUri href = UriUtils.fromString(tweet.url);
          int index = tweet.url.indexOf("//");
          String showUrl = tweet.url.substring(index+1, index+11);
          return anchorCellTemplate.anchor(href, showUrl);
        } else {
          // Dummy cell instance as a placeholder
          SafeUri href = UriUtils.fromString("");
          return anchorCellTemplate.anchor(href, "");
        }
      }
    });

    // TweetClass is converted to clickable string - First of max 4 classes
    cellComponents.add(new NthTweetClassCell(eBus,0));
    // TweetClass clickable string - Second of max 4 classes
    cellComponents.add(new NthTweetClassCell(eBus,1));
    // TweetClass clickable string - Third of max 4 classes
    cellComponents.add(new NthTweetClassCell(eBus,2));
    // TweetClass clickable string - Fourth of max 4 classes
    cellComponents.add(new NthTweetClassCell(eBus,3));
    // Create a composite cell with the cell components
    CompositeCell<NewsContents> tweetBodyComposite = new CompositeCell<NewsContents>(cellComponents);
    return tweetBodyComposite;
  }
  
  
  static class NthTweetClassCell implements HasCell<NewsContents, String> {

	  private EventBus eBus;
	  private int position;

	  public NthTweetClassCell(EventBus eBus, int position) {
		  this.eBus = eBus;
		  this.position = position;
		  this.cell = new NewsClassCell(eBus);
	  }

	  private NewsClassCell cell;

	  public Cell<String> getCell() {
		  return cell;
	  }
	  public FieldUpdater<NewsContents, String> getFieldUpdater() {
		  return new FieldUpdater<NewsContents, String>() {
			  public void update(int index, NewsContents tweet, String value) {
			  }
		  };
	  }
	  public String getValue(NewsContents tweet) {
		  // positionTH TweetClass - Nothing to do if the classes is empty
		  if (tweet.tClasses==null || tweet.tClasses.size()<=this.position) {
			  return "";
		  }
		  
		  return tweet.tClasses.get(this.position).toStrPair();
	  }
  }
}
