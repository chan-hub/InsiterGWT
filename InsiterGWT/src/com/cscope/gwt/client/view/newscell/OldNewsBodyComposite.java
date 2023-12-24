package com.cscope.gwt.client.view.newscell;

import java.util.ArrayList;
import java.util.List;

import com.cscope.gwt.client.view.cell.NameAndLogoCell;
import com.cscope.gwt.client.view.cell.RtCounterCell;
import com.cscope.gwt.client.view.cell.TextMsgCell;
import com.cscope.gwt.client.view.cell.NewsClassCell;
import com.cscope.gwt.client.view.newscell.NewsPhotoCell.NewsPhotoTemplate;
import com.cscope.gwt.client.view.newscell.NewsPhotoCell.ProfileTemplate;
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

/**
 * Deprecated - now use NewsCompositeCell.java instead
 */
public class OldNewsBodyComposite {

//  // Name template used to render the name
//  interface NameTemplate extends SafeHtmlTemplates {
//    @SafeHtmlTemplates.Template("<div style=\"{0}\">{1}</div>")
//    SafeHtml cell(SafeStyles styles, String value);
//  }
//  static private final NameTemplate nameTemplate = GWT.create(NameTemplate.class);

//public interface AnchorCellTemplates extends SafeHtmlTemplates {
//    @Template("<a href=\"{0}\">{1}</a>")
//    SafeHtml anchor(SafeUri href, String name);
//  }
//
//  static public final AnchorCellTemplates anchorCellTemplate = GWT.create(AnchorCellTemplates.class);

  // URL templates used to render the URL block
  public interface UrlTemplate extends SafeHtmlTemplates {
    //@Template("<a href=\"{0}\">{1}</a}")
    //@Template("<span class=\"align-right\"><a href=\"{0}\">{1}</a></span>")
    //@Template("<span style=\"text-align:right\"><a href=\"{0}\">{1}</a></span>")
    @Template("<span style=\"width:100%; text-align:right;\"><a href=\"{0}\">{1}</a></span>")
    SafeHtml anchor(SafeUri href, String name);
  }
  static public final UrlTemplate urlTemplate = GWT.create(UrlTemplate.class);

  static String titleAndUrlBinder = "\\\\";

  /**
   * Make a CompositeCell containing the following item per each row(s)
   * 1) Title in bold
   * 2) Article entry in light
   * 3) NameOfSource, Counter & Time-elapsed
   * 4) Item index
   * @return
   */
  static public CompositeCell<NewsContents> makeNewsBody(EventBus eBus) {
    // Cell components of the CompositeCell to build
    List<HasCell<NewsContents, ?>> cellComponents = new ArrayList<HasCell<NewsContents, ?>>();
    
    // News title
    cellComponents.add(new HasCell<NewsContents, String>() {
      //private NewsTitleCell2 cell = new NewsTitleCell2(eBus);
      private _NewsTitleCell cell = new _NewsTitleCell();
  
      public Cell<String> getCell() {
        return cell;
      }
      public FieldUpdater<NewsContents, String> getFieldUpdater() {
        return new FieldUpdater<NewsContents, String>() {
          public void update(int index, NewsContents tweet, String value) {
          }
        };
      }
      public String getValue(NewsContents news) {
        // Get the source URL
        String srcUrl = news.srcUrl;
        // Use 2 backslashes to combine the title & URL
        if (news.srcTitle!=null && !news.srcTitle.isEmpty()) {
          return news.srcTitle + titleAndUrlBinder + srcUrl;
        } else {
          return news.name + titleAndUrlBinder + srcUrl;
        }
      }
    });

    // News article head
    cellComponents.add(new HasCell<NewsContents, String>() {
      private ArticleCell cell = new ArticleCell();
  
      public Cell<String> getCell() {
        return cell;
      }
      public FieldUpdater<NewsContents, String> getFieldUpdater() {
        return new FieldUpdater<NewsContents, String>() {
          public void update(int index, NewsContents news, String value) {
            //ssm.setSelected(object, isCBChecked);
          }
        };
      }
      public String getValue(NewsContents news) {
        if (news.srcArticle!=null && !news.srcArticle.isEmpty()) {
          return news.srcArticle;
        } else {
          return news.txt;
        }
      }
    });

    // Source name string
    cellComponents.add(new HasCell<NewsContents, String>() {
      private SrcTimeTrendCell cell = new SrcTimeTrendCell();
  
      public Cell<String> getCell() {
        return cell;
      }
      public FieldUpdater<NewsContents, String> getFieldUpdater() {
        return new FieldUpdater<NewsContents, String>() {
          public void update(int index, NewsContents news, String value) {
          }
        };
      }

      // Get Str carrying srcName, timsPassed, trendCnt, clusterSize, isClusterHead, topNewsStatus
      public String getValue(NewsContents news) {
        // Get the total count
        int totalCnt = 0;
        if (news.cntTotal!=null && !news.cntTotal.isEmpty()) {
          totalCnt = Integer.parseInt(news.cntTotal);
        } else {
          totalCnt = Integer.parseInt(news.cntNRT) +
                     Integer.parseInt(news.cntCRT) + Integer.parseInt(news.cntMRT);
        }
        // Combine the timePassed, totalCnt, clusterSize, isClusterHead & optional topNewsStatus
        String timeAndCnt = news.timePassed + "," + totalCnt + "," +
        		            news.clstrSize + "," + news.isClstrLeader;
        // Add topNewsStatus if exists
        if (news.topNewsStatus != null) {
          timeAndCnt += ("," + news.topNewsStatus);
        }

        // Then add the srcName at the front
        if (news.srcName!=null && !news.srcName.isEmpty()) {
          return news.srcName + ',' + timeAndCnt;
        } else {
          return news.sName + ',' + timeAndCnt;
        }
      }
    });

    // Removed this extra URL link - replaced by SubjectButton(s)

//    // SafeHtmlCell to support URL reference
//    cellComponents.add(new HasCell<News2Show, SafeHtml>() {
//      private SafeHtmlCell cell = new SafeHtmlCell();
//
//      public Cell<SafeHtml> getCell() {
//        return cell;
//      }
//      public FieldUpdater<News2Show, SafeHtml> getFieldUpdater() {
//        return new FieldUpdater<News2Show, SafeHtml>() {
//          public void update(int index, News2Show tweet, SafeHtml value) {
//            //ssm.setSelected(object, isCBChecked);
//          }
//        };
//      }
//      public SafeHtml getValue(News2Show tweet) {
//        if (tweet.url!=null && !tweet.url.isEmpty()) {
//          SafeUri href = UriUtils.fromString(tweet.urlFull);
//          return urlTemplate.anchor(href, tweet.url);
////          int index = tweet.url.indexOf("//");
////          String showUrl = tweet.url.substring(index+1, index+11);
////          return urlTemplate.anchor(href, showUrl);
////          return anchorCellTemplate.anchor(href, showUrl);
//        } else {
//          // Dummy cell instance as a placeholder
//          SafeUri href = UriUtils.fromString("");
//          return urlTemplate.anchor(href, "");
////          return anchorCellTemplate.anchor(href, "");
//        }
//      }
//    });

    // Subject is converted to clickable string - First of max 2 Subjects
    cellComponents.add(new NthSubjButtonCell(eBus,0));
    // Subject is converted to clickable string - Second of max 2 Subjects
    cellComponents.add(new NthSubjButtonCell(eBus,1));
    
    // TweetClass is converted to clickable string - First of max 4 classes
//    cellComponents.add(new NthTweetClassCell(eBus,0));
    // TweetClass clickable string - Second of max 4 classes
//    cellComponents.add(new NthTweetClassCell(eBus,1));
    // TweetClass clickable string - Third of max 4 classes
//    cellComponents.add(new NthTweetClassCell(eBus,2));
    // TweetClass clickable string - Fourth of max 4 classes
//    cellComponents.add(new NthTweetClassCell(eBus,3));

//    // Index template used to render the index
//    interface IndexTemplate extends SafeHtmlTemplates {
//      @SafeHtmlTemplates.Template("<div style=\"{0}\">{1}</div>")
//      SafeHtml cell(SafeStyles styles, String value);
//    }
//
//    // Singleton instances of the templates used to render this CustomCell
//    private static IndexTemplate indexTemplate = GWT.create(IndexTemplate.class);
//  // Render the index with the desired style  .paddingLeft(5, Style.Unit.PX)
//  SafeStylesBuilder builder = new SafeStylesBuilder().fontSize(10, Style.Unit.PX);
//  SafeStyles idxPosition = SafeStylesUtils.forFloat(Style.Float.RIGHT);
//  builder.append(idxPosition);
//  rendered = indexTemplate.cell(builder.toSafeStyles(), idxStr);
//  sb.append(rendered);

    // News index
    cellComponents.add(new HasCell<NewsContents, String>() {
      private IndexCell cell = new IndexCell();
  
      public Cell<String> getCell() {
        return cell;
      }
      public FieldUpdater<NewsContents, String> getFieldUpdater() {
        return new FieldUpdater<NewsContents, String>() {
          public void update(int index, NewsContents news, String value) {
          }
        };
      }
      public String getValue(NewsContents news) {
        return news.index;
      }
    });

    // Create a composite cell with All of the cell components
    CompositeCell<NewsContents> tweetBodyComposite = new CompositeCell<NewsContents>(cellComponents);
    return tweetBodyComposite;
  }

  /**
   * NthSubjButtonCell is a Subject Button to support browsing NewsList of a Subject
   */
  static class NthSubjButtonCell implements HasCell<NewsContents, String> {

    private EventBus eBus;
    private int position;
    private SubjButtonCell cell;

    public NthSubjButtonCell(EventBus eBus, int position) {
      this.eBus = eBus;
      this.position = position;
      this.cell = new SubjButtonCell(eBus);
    }

    public Cell<String> getCell() {
      return cell;
    }

    public FieldUpdater<NewsContents, String> getFieldUpdater() {
      return new FieldUpdater<NewsContents, String>() {
        public void update(int index, NewsContents news, String value) {
        }
      };
    }

    public String getValue(NewsContents news) {
      // Extract the SubjStr of this position - Nothing to do if the SubjStr is empty
      String subjStr = news.getSubjStrAt(this.position);
      if (subjStr == null) {
        return "";
      }
      return subjStr;
    }
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
			  public void update(int index, NewsContents news, String value) {
			  }
		  };
	  }
	  public String getValue(NewsContents news) {
		  // positionTH TweetClass - Nothing to do if the classes is empty
		  if (news.tClasses==null || news.tClasses.size()<=this.position) {
			  return "";
		  }
		  
		  return news.tClasses.get(this.position).toStrPair();
	  }
  }
}
