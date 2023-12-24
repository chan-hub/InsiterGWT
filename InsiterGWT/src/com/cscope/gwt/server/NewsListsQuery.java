package com.cscope.gwt.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.cscope.gwt.server.JmsReqHandler.JmsCmd;
//import com.cscope.gwt.shared.NewsContents;
import com.cscope.gwt.shared.NewsList2Show;

import insiter.ina.news.News;
import insiter.ina.news.NewsIndex;

import insiter.ina.news.build.NewsClass;
import insiter.ina.news.build.NewsClass.Entity;
import insiter.ina.news.build.NewsClass.Subject;
import insiter.ina.news.build.NewsClass.Instance;
import insiter.ina.news.build.NewsClass.Menu;
import insiter.ina.news.build.NewsClass.ClassTri;
import insiter.ina.news.build.NewsClass.UpperMenu;

import insiter.ina.news.cluster.NewsCluster;
import insiter.ina.news.table.NewsLists;
import insiter.ina.news.service.web.NewsContents;
import insiter.ina.news.service.web.NewsList4Menu;
import insiter.ina.news.service.web.NewsMenu;
//import insiter.ina.service.web.NewsList;
import insiter.ina.util.Util;

public class NewsListsQuery {
  static private NewsList4Menu latestList4Menu = null;
  static private NewsLists latestNewsLists = null;

  static protected void setNewsList4Menu(NewsList4Menu nLists) {
    NewsListsQuery.latestList4Menu = nLists;
    System.out.println("NewsListsQuery.setNewsList4Menu():Taking a freshly build NewsList4Menu at:" + Util.getCurrentTime());
  }

  static protected void setNewsLists(NewsLists nLists) {
    NewsListsQuery.latestNewsLists = nLists;
  }

  /**
   * This is the actual method to get NewsMenu
   * 
   * Build a NewsMenu from the top NewsList4Menu contents
   * 
   * @return
   */
  static protected NewsMenu getNewsMenu() {
    // Return empty NewsMenu when the latest NewsList4Menu is not ready
    if (NewsListsQuery.latestList4Menu == null) {
      System.out.println("NewsListsQuery.getNewsMenu():latest NewsList4Menu is null");
      return NewsMenu.EmptyMenuInstance;
    }
    // Instance a NewsMenu to return
    NewsMenu newsMenu = new NewsMenu();
    // Get the Up Menu list froom the latest NewsList4Menu instance (built w/ a reference menu)
    List<String> upMenuStrs = NewsList4Menu.getUpperMenuStrs();
    if (upMenuStrs!=null && upMenuStrs.size()>0) {
      newsMenu.setUpperMenu2(upMenuStrs);
      System.out.println("NewsListsQuery.getNewsMenu():UpperMenu:" + Util.toString(upMenuStrs));
    } else {
      System.out.println("NewsListsQuery.getNewsMenu():UpperMenu String is null OR empty");
    }

    // Get the Bottom-All menu list (i.e. Entity.All's LowerMenu items)
    List<String> lowerMenuStrs = NewsList4Menu.getLowerMenuStrs();
    if (lowerMenuStrs!=null && lowerMenuStrs.size()>0) {
      newsMenu.setLowerAllMenu2(lowerMenuStrs);
      System.out.println("NewsListsQuery.getNewsMenu():LowerAllMenu:" + Util.toString(lowerMenuStrs));
    } else {
      System.out.println("NewsListsQuery.getNewsMenu():LowerAllMenu String is null OR empty");
    }

    // Get the child Menu for each top Menu item
    List<Menu> upMenuList = NewsList4Menu.getUpMenu();
    for (Menu parentMenu : upMenuList) {
      List<String> childMenuStrs = NewsList4Menu.getMenuListStrOf(Entity.Root, parentMenu);
      if (childMenuStrs==null || childMenuStrs.isEmpty()) {
        System.out.println("NewsListsQuery.getNewsMenu():Up Menu:" + parentMenu +
                           "'s child's menu is null OR empty");
        continue;
      }
      // Put this MenuStrList to its parent Menu item
      newsMenu.setLowerMenu2(parentMenu.toString(), childMenuStrs);
      System.out.println("NewsListsQuery.getNewsMenu():Parent Menu:" + parentMenu +
                         " has its Child MenuList:" + childMenuStrs);
    }
    return newsMenu;
  }

  /**
   * API for InsiterRPCImpl, which calls this when a NewsList request arrives (GWT RPC)
   * 
   * Return the NewsList containing the News items of 'classStr' specifying the Subject
   * 
   * @param key
   * @param maxCnt
   * @param hourPeriod
   * @param seqOrder
   * @param classStr
   * @return
   */
  static public NewsList2Show getNewsList2Show(String key, int maxCnt, int hourPeriod,
		                                       String seqOrder, String classStr) {
    // Instance a NewsList2Show to fill
  	NewsList2Show nList2Show = new NewsList2Show();
    // Nothing to do if our latest NewsList4Menu is null
	  if (NewsListsQuery.latestList4Menu == null) {
	    System.out.println("NewsListsQuery.getNewsList2Show():Aborted as the latest NewsList4Menu object is null");
	    return nList2Show;
	  }
      // Extract the exact list of News belong to the NewsMenu (specified by 'classStr')
      // (here we can add the TopNews filter to 'getNewsList4MenuTri()')
	  NewsList4Menu newsList4Menu = NewsListsQuery.latestList4Menu.getNewsList4MenuStr(classStr, maxCnt);

	  // Get the list of NewsContents from this NewsList4Menu if available
	  List<NewsContents> newsContentList = newsList4Menu.getNewsContentsList();
	  if (newsContentList!=null && !newsContentList.isEmpty()) {
	    System.out.println("NewsListsQuery.getNewsList2Show():Got " + newsContentList.size() + " prebuilt NewsContents for=>'" + classStr + '\'');
	 // Put the list into NewsList2Show to return
	    nList2Show.setNewsList(newsContentList);
	    return nList2Show;
	  }
	  // Build NewsContents list
      List<News> newsList = newsList4Menu.getNewsList();
	  System.out.println("NewsListsQuery.getNewsList2Show():Got " + newsList.size() + " News for=>'" + classStr + '\'');

      // Get the latest NewsIndex & NewsCluster tables to refer (to render NewsJsonArray)
      Map<Long, NewsIndex> indexTable = TomcatClient.getNewsIndexTable();
      Map<Long, NewsCluster> ncTable = TomcatClient.getNewsClusterTable();

      // Convert the News list to list of NewsContents to send back
	  newsContentList = NewsListsQuery.buildNewsContentsList(newsList, indexTable, ncTable);
      // Put the list into NewsList2Show to return
      nList2Show.setNewsList(newsContentList);

      // Then build JSONArray of NewsJSON to transfer (for Servlet operation)
      JSONArray newsIdsJSON = newsList4Menu.getNewsIdsJSON();
      if (newsIdsJSON!=null && !newsIdsJSON.isEmpty()) {
      JSONArray NewsJSONs = new JSONArray();
      for (int i=0; i<newsIdsJSON.size(); i++) {
        JSONArray newsIds = (JSONArray)newsIdsJSON.get(i);
        if (newsIds.size() == 1) {
          // A single News
        } else {
          // A cluster
        }
      }
    }
    return nList2Show;
  }

  /**
   * Build list of NewsContents to be delivered to the GWT browsers - no JSON processing
   * 
   * @param newsList
   * @param indexTable
   * @param ncTable
   * @return
   */
  static private List<NewsContents> buildNewsContentsList(List<News> newsList,
    		          Map<Long, NewsIndex> indexTable, Map<Long, NewsCluster> ncTable) {
    List<NewsContents> newsCntnts2Rtn = new ArrayList<NewsContents>();
    // Convert each News into a NewsContents
  	for (int i=0; i<newsList.size(); i++) {
      News n = newsList.get(i);
      if (n == null) {
        System.err.println("NewsListsQuery.buildNewsContentsList():bypass null News instance in newsList");
        continue;
      }
      /**
       * Render 'NewsContents' directly from the News object associated
       */
      // To circumvent 'NO-CONTENTS ERROR', get contents from the News
      NewsIndex index = indexTable.get(n.id);
      if (index == null) {
        System.err.println("NewsListQuery.buildNewsContentsList():" + n.idStr +
                           " aborts - cannot find its NewsIndex instance");
        return null;
      }
      // Build a NewsContents
      NewsContents nct = NewsList4Menu.buildNewsCntnts2Cache(n, index, ncTable, TomcatClient.runMode);
      //NewsContents nct = NewsListsQuery.buildNewsContents(n, i, indexTable, ncTable);
      if (nct != null) {
        newsCntnts2Rtn.add(nct);
      }
    }
  	System.out.println("NewsListsQuery.buildNewsContentsList():Built NewsList of " + newsCntnts2Rtn.size() + " News");
	  return newsCntnts2Rtn;
  }

//  /**
//   * Build a NewsContents instance containing the information of News, 'n',
//   * to be transferred to web browsers to present
//   * 
//   * @param news
//   * @param idx
//   * @param indexTable
//   * @param ncTable
//   * @return
//   */
//  static public NewsContents
//        buildNewsContents(News news, int idx, Map<Long, NewsIndex>indexTable, Map<Long,NewsCluster>ncTable) {
//    NewsIndex index = indexTable.get(news.id);
//    if (index == null) {
//      System.err.println("NewsListQuery.buildNewsContents():Aborted as News " + news.idStr +
//                   " cannot find its NewsIndex instance");
//      return null;
//    }
//    NewsCluster nc = null;
//    long cid = index.getClusterId();
//    if (cid != 0L) {
//      nc = ncTable.get(cid);
//    }
//    // Instance a new NewsContents to return
////    NewsContents contents = new NewsContents(idx, news.idStr, news.getTweetTxt(), "*name*",
////                             index.getTimePublishedStr(), NewsJsonBuilder.getElpsdPubTimeStr(index));
////    // Set URL info
////    String shortUrl = "";
////    String srcUrl = news.getSrcUrl();
////    // Set shortUrl if srcUrl is empty
////    if (srcUrl!=null && srcUrl.isEmpty()) {
////      shortUrl = Url.getRootUriLessWWW(srcUrl);
////    }
////    contents.setURLs(srcUrl, shortUrl);
////
////    // Get the News' publisher, title & head of article
////    boolean debug = true;
////    // Get News images in B64 encoding - try the logo library first
////    String logoImg = NewsJsonBuilder.getB64LogoImage(news, news.getSrcUrl());
////    if (logoImg==null || logoImg.isEmpty()) {
////      logoImg = NewsJsonBuilder.getB64Image(ImgType.Logo, news, index, TomcatClient.runMode, debug);
////    }
////    String thumbImg = NewsJsonBuilder.getB64Image(ImgType.Thumb, news, index, TomcatClient.runMode, debug);
////    contents.setNewsInfo(news.getSrcName(), news.getSrcTitle(), news.getArt2Show(), logoImg, thumbImg);
////    //String srcImage = NewsJsonBuilder.getB64ThumbMedImg(news, index, TomcatClient.runMode, debug);
////    //cnts.setNewsInfo(news.getSrcName(), news.getSrcTitle(), news.getArt2Show(), srcImage);
////
////    // Set total retweet counts w/ BOT's retweets
////    contents.setRtCnts(index.getRtCnt(), index.getBotCnt());
////
////    // Set its Cluster info
////    String clstrSize = (nc==null) ? "0" : nc.size()+"";
////    String isClstrHead = (nc==null) ? "false" : (news.id==nc.getHeadId()?"true" : "false");
////    contents.setClstrInfo(clstrSize, isClstrHead);
////
////    // Set its menuSubject w/ associated NewsClasses
////    contents.setSubjAndClasses(news.getMenuSubj2Show(nc), news.getNewsClasses());
//    return contents;
//  }

// Old version creating JSON first, then instancing NewsContents from the JSON,
// waste of unnecessary duplicated conversion

//  static private List<NewsContents> _buildNewsContentsList(JSONArray newsJsonArray,
//		                                   Map<Long,NewsIndex>indexTable, Map<Long,NewsCluster>ncTable) {
//    ArrayList<NewsContents> newsContents = new ArrayList<NewsContents>();
//    // Convert each News into a NewsContents
//    for (int i=0; i<newsList.size(); i++) {
//      NewsContents t = new NewsContents();
//      JSONObject tweetJson = (JSONObject)newsJsonArray.get(i);
//      JmsRespHandler.parseToNews2Show(t, tweetJson);
//      newsContents.add(t);
//    }
//    return newsContents;
//  }
//
//  static public NewsList2Show _getNewsList2Show(String key, int maxCnt, int hourPeriod,
//                                                String seqOrder, String classStr) {
//    // Instance a NewsList2Show to fill upon the arrival of respond
//    NewsList2Show nList2Show = new NewsList2Show();
//    // Nothing to do if our NewsLists is empty
//    if (NewsListsQuery.latestNewsLists == null) {
//      System.out.println("NewsListsQuery.getNewsList2Show():The latest NewsLists object is null");
//    return nList2Show;
//    }
//
//    // Get the list of News first
//    ClassTri tri = NewsClass.getMenuTriFromPair(classStr);
//
//    // Here we can add the TopNews filter to 'getNewsList4MenuTri()'
//    List<News> newsList = NewsListsQuery.latestNewsLists.getNewsList4MenuTri(tri, maxCnt);;
//    System.out.println("NewsListsQuery.getNewsList2Show():Got "+newsList.size()+ " News for=>" + tri.toString());
//    long time = System.currentTimeMillis();
//
//    // Get the latest NewsIndex & NewsCluster tables to refer (to render NewsJsonArray)
//    Map<Long,NewsIndex> indexTable = TomcatClient.getNewsIndexTable();
//    Map<Long,NewsCluster> ncTable = TomcatClient.getNewsClusterTable();
//
//    // Convert the News list into JSONArray News items
//    JSONArray newsJsonArray =
//            NewsJsonBuilder.convertNewsList2JsonArray(maxCnt, newsList, indexTable, ncTable, TomcatClient.debug);
//    System.out.println("NewsListsQuery.getNewsList2Show():Got " + newsJsonArray.size() + " NewsJson");
//
//    // Then convert into a list of News2Show
//    List<NewsContents> news2ShowList = NewsListsQuery.convNewsJSONArray2NewsList2Show(newsJsonArray);
//    // Put the list into NewsList2Show to return
//    nList2Show.setNewsList(news2ShowList);
//    return nList2Show;
//  }
//
//  static private ArrayList<NewsContents> convNewsJSONArray2NewsList2Show(JSONArray newsJsonArray) {
//    ArrayList<NewsContents> newsList = new ArrayList<NewsContents>();
//    // Parse each News in the JSON
//    for (int i=0; i<newsJsonArray.size(); i++) {
//      NewsContents t = new NewsContents();
//      JSONObject tweetJson = (JSONObject)newsJsonArray.get(i);
//      JmsRespHandler.parseToNews2Show(t, tweetJson);
//      newsList.add(t);
//    }
//    return newsList;
//  }
}
