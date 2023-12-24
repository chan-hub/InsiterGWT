package com.cscope.gwt.client.view.newscell;

import java.util.ArrayList;
import java.util.List;

import com.cscope.gwt.client.view.NewsListView;
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
 * NewsCompositeCell is to keep its components accessible
 * to support scroll position saving and restoration, by checking the NewsTitleCell2
 * of event source - in the click event handler
 *
 */
public class _NewsCompositeCell {

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

  // Instance variables of NewsCompositeCell
  private CompositeCell<NewsContents> compositeCell;
  private List<HasCell<NewsContents, ?>> cellComponents;

  public CompositeCell<NewsContents> getCompositeCell() {
    return this.compositeCell;
  }

  /**
   * Make a CompositeCell containing the following item per each row(s)
   * 1) Title in bold
   // Article is removed June 2023
   * 2) Article entry in light
   * 3) NameOfSource, Counter & Time-elapsed
   * 4) Item index
   * @return
   */
  public _NewsCompositeCell(final EventBus eBus) {
    // Cell components of the CompositeCell to build
    this.cellComponents = new ArrayList<HasCell<NewsContents, ?>>();
//  /**
//   * Make a CompositeCell containing the following item per each row(s)
//   * 1) Title in bold
    //Delete Article entry - June 2023
////   * 2) Article entry in light
//   * 3) NameOfSource, Counter & Time-elapsed
//   * 4) Item index
//   * @return
//   *///    // Cell components of the CompositeCell to build

//  static public CompositeCell<News2Show> makeNewsBody(EventBus eBus) {
//    List<HasCell<News2Show, ?>> cellComponents = new ArrayList<HasCell<News2Show, ?>>();
    
    // News title
    cellComponents.add(new HasCell<NewsContents, String>() {

      private NewsTitleCell cell = new NewsTitleCell(eBus);
      //private NewsTitleCell2 cell = new NewsTitleCell2();
  
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

    // Show Article for non-mobile devices - June 2023
    if (!NewsListView.isMobile()) {
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
    }

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

      // Get String carrying srcName, timsPassed, trendCnt, clusterSize, isClusterHead, topNewsStatus
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

    // Subject is converted to click-able string - First of max 2 Subjects
    cellComponents.add(new NthSubjButtonCell(eBus,0));
    // Subject is converted to click-able string - Second of max 2 Subjects
    cellComponents.add(new NthSubjButtonCell(eBus,1));

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
    this.compositeCell = new CompositeCell<NewsContents>(cellComponents);
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
        public void update(int index, NewsContents tweet, String value) {
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

  static class NthNewsClassCell implements HasCell<NewsContents, String> {
	  private EventBus eBus;
	  private int position;

	  public NthNewsClassCell(EventBus eBus, int position) {
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
	  public String getValue(NewsContents news) {
		  // positionTH TweetClass - Nothing to do if the classes is empty
		  if (news.tClasses==null || news.tClasses.size()<=this.position) {
			  return "";
		  }
		  return news.tClasses.get(this.position).toStrPair();
	  }
  }
}
