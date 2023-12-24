package com.cscope.gwt.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.cscope.gwt.server.JmsReqHandler.JmsCmd;
import com.cscope.gwt.shared.News2ShowExt;
// com.cscope.gwt.shared.NewsContents;
import com.cscope.gwt.shared.News2ShowExt.RtCategory;
import com.cscope.gwt.shared.NewsList2Show;

import insiter.ina.news.build.NewsClass;
import insiter.ina.news.build.NewsClass.Entity;
import insiter.ina.news.build.NewsClass.Subject;
import insiter.ina.news.build.NewsClass.Instance;
import insiter.ina.news.build.NewsClass.LowerMenu;
import insiter.ina.news.build.NewsClass.UpperMenu;
import insiter.ina.news.service.web.NewsContents;
import insiter.ina.news.service.web.NewsMenu;

public class JmsRespHandler {
  // Initialize JmsRespHandler
  static void init() {
    // Start our jmsRespRecv thread
    System.out.println("\n=== JmsRespHandler:start init() ===");
    jmsRespRecvThread.start();
  }

  // Create a Thread instance to receive JMS respond
  static Thread jmsRespRecvThread = new Thread() {
    public void run() {
      long nullMsgCnt = 0L;
      boolean doLog = true;
      while (true) {
        doLog = (nullMsgCnt%100000000) == 0;
    	//doLog = nullMsgCnt < 100;
    	if (doLog) {
          System.out.println("=>JmsRespHandler.jmsRespRecvThread.run():waiting for a respond");
    	}
        String msg = CsStomp.recvMsg(JmsReqHandler.cscope2hotweet, doLog);
        if (msg == null) {
          if (++nullMsgCnt > 100000000000L) {
        	//nullMsgCnt = 0L;
            System.out.println("\n===>JmsRespHandler.jmseRespRecvThread.run():Terminate as it can't receive JMS response");
            break;
          }
          continue;
        }
        else {
          nullMsgCnt = 0L;
          System.out.println("=>JmsRespHandler.jmsRespRecvThread.run():received a respond");
          JmsRespHandler.processJsonRespond(msg);
        }
      }
    }
  };

  /**
   * Process a received JSON object per respond
   */
  static private void processJsonRespond(String jsonMsg) {
    JSONObject jsonMsgObj = (JSONObject)JSONValue.parse(jsonMsg);
    if (jsonMsgObj == null) {
      System.out.println("!!! JmsRespHandler.processJsonRespond():JSON is NULL. message:" + jsonMsg);
      return;
    }
    // Get the command first
    String cmdStr = (String)jsonMsgObj.get("cmd");
    if (cmdStr == null) {
      System.out.println("!!! JmsRespHandler.processJsonRespond():can't find cmd in:" + jsonMsg);
      return;
    }
    JmsCmd cmd = JmsCmd.getCmd(cmdStr);
    if (cmd == null) {
      System.out.println("!!! JmsRespHandler.processJsonRespond():invalid JmsCmd:" + cmdStr);
      return;
    }
    switch (cmd) {
      case GetMenu:
        JmsRespHandler.doNewsMenuResp(jsonMsgObj);
        break;

      case GetTweet:
        JmsRespHandler.parseGetTweetResp(jsonMsgObj);
        break;

      case PutTweet:
        JmsRespHandler.parseRespond(jsonMsgObj);
//        JmsRespHandler.putTweetResp = parseRespond(jsonMsgObj);
        break;

      case GetTweetList:
        JmsRespHandler.doNewsListResp(jsonMsgObj);
        break;

      case PutFeedback:
        JmsRespHandler.parseRespond(jsonMsgObj);
//        JmsRespHandler.putFeedbackResp = parseRespond(jsonMsgObj);
        break;

      case AdminLogin:
        JmsRespHandler.parseAdminLoginResp(jsonMsgObj);
//        JmsRespHandler.adminLoginResp = parseAdminLoginResp(jsonMsgObj);
        break;

      case AdminLogout:
        JmsRespHandler.parseRespond(jsonMsgObj);
//        JmsRespHandler.adminLogoutResp = parseRespond(jsonMsgObj);
        break;

      case AddAdmin:
        JmsRespHandler.parseRespond(jsonMsgObj);
//        JmsRespHandler.addAdminResp = parseRespond(jsonMsgObj);
        break;

      case RmAdmin:
        JmsRespHandler.parseRespond(jsonMsgObj);
//        JmsRespHandler.rmAdminResp = parseRespond(jsonMsgObj);
        break;

      default:
        break;
    }
  }

  static private void doNewsMenuResp(JSONObject jsonMsgObj) {
    String key = (String)jsonMsgObj.get("cacheKey");
    if (key == null) {
      System.out.println("!!! JmsRespHandler.doNewsMenuResp():can't get cacheKey of Menu respond");
      key = InsiterRPCImpl.getDefNewsMenuKey();
    } else {
      System.out.println("JmsRespHandler.doNewsMenuResp():Found Menu cacheKey:" + key);
    }
    // Get the NewsMenu instance to notify
    NewsMenu newsMenu = JmsReqHandler.getReqNewsMenu(key);
    if (newsMenu == null) {
      // Requesting thread must time out
      System.out.println("!!! JmsRespHandler.doNewsMenuResp():can't find NewsMenu to notify");
      // No thread to notify, but cache it for the next menu request
      newsMenu = new NewsMenu();
      JmsRespHandler.parseNewsMenu(jsonMsgObj, newsMenu);
      if (newsMenu.isFilledUp()) {
        NewsCache.putNewsMenu(key, newsMenu);
      }
      return;
    }
    JmsRespHandler.parseNewsMenu(jsonMsgObj, newsMenu);
    System.out.println("JmsRespHandler.doNewsMenuResp():NewsMenu has " + newsMenu.getEntMenu().size() +
    //System.out.println("JmsRespHandler.doNewsMenuResp():NewsMenu has " + newsMenu.upperMenu.size() +
                       " Entity && " + newsMenu.lowerAllMenu.size() + " Subject items");
    synchronized(newsMenu) {
      newsMenu.notifyAll();
    }
    NewsCache.putNewsMenu(key, newsMenu);
  }

  static private void doNewsListResp(JSONObject jsonMsgObj) {
    String cacheKey = (String)jsonMsgObj.get("cacheKey");
    if (cacheKey == null) {
      System.out.println("!!! JmsRespHandler.doNewsListResp():No NewsList 'cacheKey' JSON element");
      return;
    }
    NewsList2Show newsList = JmsReqHandler.getReqNewsList(cacheKey);
    if (newsList == null) {
      // Requesting thread must time out
      System.out.println("!!! JmsRespHandler.doNewsListResp():can't find NewsList of:'" +
                           cacheKey + "' to notify");
      // No thread to notify, but cache it for the next menu request
      newsList = new NewsList2Show();
      JmsRespHandler.parseNewsList(jsonMsgObj, newsList);
      NewsCache.putNewsList(cacheKey, newsList);
      return;
    }
    // Parse the JSON into NewsList
    JmsRespHandler.parseNewsList(jsonMsgObj, newsList);
    System.out.println("==>JmsRespHandler.doNewsListResp():notifying Threads waiting " +
                       "for NewsList respond, " + newsList.size() + " news items received");
    // Notify the waiting threads
    synchronized(newsList) {
      newsList.notifyAll();  // wake up ALL threads
    }
    // Update the News cache
    NewsCache.putNewsList(cacheKey, newsList);
  }

  static private String parseRespond(JSONObject jsonMsgObj) {
    String status = (String) jsonMsgObj.get("status");
    if (status != null) {
      return status;
    }
    return "";
  }

  static private String parseAdminLoginResp(JSONObject jsonMsgObj) {
    String status = (String) jsonMsgObj.get("status");
    if (status!=null) {
      if (status.equals("OK")) {
        String superAdmin = (String) jsonMsgObj.get("superAdmin");
        if (superAdmin != null) {
          status = "OK:superAdmin";
        }
      } else {
        // status already gets "ERROR:[error message]"
      }
      return status;
    }
    return "";
  }

  static private void parseNewsMenu(JSONObject jsonMsgObj, NewsMenu newsMenu) {
    // Get the Top menu list
    String topStr = (String)jsonMsgObj.get("topMenu");
    if (topStr == null) {
      System.err.println("!!! JmsRespHandler.parseNewsMenu():can't get topMenu in:" + jsonMsgObj);
      return;
    }
    // Get the all Bottom menu list
    String bottomAllStr = (String)jsonMsgObj.get("bottomAllMenu");
    if (bottomAllStr == null) {
      System.err.println("!!! JmsRespHandler.parseNewsMenu():can't get bottomAllMenu in:" + jsonMsgObj);
      return;
    }

    String[] topMenuStrArray = topStr.split(",");
    List<String> topMenuStrs = java.util.Arrays.asList(topMenuStrArray);
//    List<TopMenu> topMenuList = new ArrayList<TopMenu>();
//    for (String menuStr : topMenuStrArray) {
//      // Get the TopMenu item
//      TopMenu topMenu = NewsClass.getTopMenuItem(menuStr);
//      if (topMenu != null) {
//        topMenuList.add(topMenu);
//      }
//    }
    newsMenu.setUpperMenu2(topMenuStrs);
    System.out.println("JmsRespHandler.parseNewsMenu():TopMenu:" + topStr);

    // Put bottomAll list
    String[] bottomAllStrArray = bottomAllStr.split(",");
    List<String> botMenuStrs = java.util.Arrays.asList(bottomAllStrArray);
//    List<LowerMenu> bottomAllList = new ArrayList<LowerMenu>();
//    for (String menuStr : bottomAllStrArray) {
//      // Get the LowerMenu item
//      LowerMenu botMenu = NewsClass.getBotMenuItem(menuStr);
//      if (botMenu != null) {
//        bottomAllList.add(botMenu);
//      }
//    }
    newsMenu.setLowerAllMenu2(botMenuStrs);
    System.out.println("JmsRespHandler.parseNewsMenu():BottomAllMenu:" + bottomAllStr);

    // Get the SubjMenu contents to parse
    String botMenuMapStr = (String)jsonMsgObj.get("bottomMenu");
    if (botMenuMapStr == null) {
      System.err.println("!!! JmsRespHandler.parseNewsMenu():can't get LowerMenu map in:" + jsonMsgObj);
      return;
    }
    JSONObject botMenuMapObj = (JSONObject)JSONValue.parse(botMenuMapStr);

    // Now build LowerMenu for each Entity
    for (String topMenuStr : topMenuStrArray) {
      // Get the TopMenu item
      UpperMenu upperMenu = NewsClass.getUpperMenuItem(topMenuStr);
      if (upperMenu == null) {
        System.err.println("!!! JmsRespHandler.parseNewsMenu():No UpperMenu for:" + topMenuStr);
        continue;
      }
      // Get the bottomMenu string for this topMenu
      String botMenuStr = (String)botMenuMapObj.get(topMenuStr);
      if (botMenuStr == null) {
        System.err.println("!!! JmsRespHandler.parseNewsMenu():No LowerMenu for:" + topMenuStr);
        continue;
      }
      // OK - build the LowerMenu now
      String[] botMenuStrArray = botMenuStr.split(",");
      botMenuStrs = Arrays.asList(botMenuStrArray);
//      List<LowerMenu> bottomMenuList = new ArrayList<LowerMenu>();
//      for (String menuStr : botMenuStrArray) {
//        // Get a LowerMenu
//        LowerMenu botMenu = NewsClass.getBotMenuItem(menuStr);
//        if (botMenu != null) {
//          bottomMenuList.add(botMenu);
//        }
//      }
      // Put this LowerMenu to this UpperMenu item
      newsMenu.setLowerMenu2(upperMenu.toString(), botMenuStrs);
      System.out.println("JmsRespHandler.parseNewsMenu():UpperMenu:" + topMenuStr +
                         ", LowerList:" + botMenuStr);
    }
  }

  /**
   * Parse JSON into an instance of Tweet2ShowExt
   * @param jsonMsgObj
   */
  static private void parseGetTweetResp(JSONObject jsonMsgObj) {
    // Parse into a Tweet2ShowExt first
    News2ShowExt t2ShowExt = new News2ShowExt();
    JmsRespHandler.parseToNews2Show(t2ShowExt, jsonMsgObj);

    // Then extract the media URL stuffs
    if (jsonMsgObj.get("mdaUrl") != null) {
      t2ShowExt.mdaUrl = (String)jsonMsgObj.get("mdaUrl");
      t2ShowExt.mdaUrlHttps = (String)jsonMsgObj.get("mdaUrlHttps");
      t2ShowExt.mdaUrlSize = (String)jsonMsgObj.get("mdaUrlSize");
      t2ShowExt.mdaUrlType = (String)jsonMsgObj.get("mdaUrlType");
    }
    // classUpdated status
    t2ShowExt.classUpdated = (boolean)jsonMsgObj.get("classUpdated");

    // RtCategory
    t2ShowExt.rtCat = RtCategory.getCategory((String)jsonMsgObj.get("rtCat"));

    // Finally, extract related tweets if exists
    String tListInJson = (String)jsonMsgObj.get("tweetList");
    if (tListInJson != null) {
      t2ShowExt.relatedTweets = JmsRespHandler.convertToNews2Show(tListInJson);
    }
    // Let our RPC method take it now
    //JmsRespHandler.tweet2ShowExt = t2ShowExt;
  }

  /**
   * Parse JSONObject into a list of News items to send to clients
   */
  static private void parseNewsList(JSONObject jsonMsgObj, NewsList2Show newTweet4Display) {
    String listJsonStr = (String)jsonMsgObj.get("NewsList");
    if (listJsonStr == null) {
      System.err.println("!!! JmsRespHandler.parseNewsList():can't get NewsList in:" + jsonMsgObj);
      return;
    }
    // Convert the JSON into list of News to display, to set it the newsDisplay
    ArrayList<NewsContents> newTweetList = JmsRespHandler.convertToNews2Show(listJsonStr);
    newTweet4Display.setNewsList(newTweetList);
  }

  /**
   * Produce a List of News2Show, each instanced to carry a newsJSON object contents
   * @param newsInJsonStr
   * @return
   */
  static private ArrayList<NewsContents> convertToNews2Show(String newsInJsonStr) {
    ArrayList<NewsContents> newsList = new ArrayList<NewsContents>();
    JSONArray newsJsonArray = (JSONArray)JSONValue.parse(newsInJsonStr);
    if (newsJsonArray == null) {
      System.err.println("!!! JmsRespHandler.convertToNews2Show():can't get JSONArray from::" +
                              newsInJsonStr);
      return newsList;
    }
    // Parse each News in the JSON
    for (int i=0; i<newsJsonArray.size(); i++) {
      NewsContents n = new NewsContents();
      JSONObject tweetJson = (JSONObject)newsJsonArray.get(i);
      JmsRespHandler.parseToNews2Show(n, tweetJson);
      newsList.add(n);
    }
    return newsList;
  }

  /**
   * Parse newsJson into a NewsContents to deliver to GWT agents
   * 
   * @param newsContents
   * @param newsJson
   */
  static protected void parseToNews2Show(NewsContents newsContents, JSONObject newsJson) {
    newsContents.id = (String)newsJson.get("id");
    newsContents.index = (String)newsJson.get("index");
    newsContents.txt = (String)newsJson.get("txt");
    newsContents.name = (String)newsJson.get("name");
    newsContents.sName = (String)newsJson.get("sName");
    newsContents.date = (String)newsJson.get("date");
    newsContents.url = (String)newsJson.get("url");
    newsContents.urlFull = (String)newsJson.get("urlFull");
    newsContents.timePassed = (String)newsJson.get("timePassed");
    newsContents.srcUrl = (String)newsJson.get("srcUrl");
    newsContents.srcName = (String)newsJson.get("srcName");
    newsContents.srcTitle = (String)newsJson.get("srcTitle");
    newsContents.srcArticle = (String)newsJson.get("srcArticle");
    newsContents.logoImage = (String)newsJson.get("logoImage");
    newsContents.thumbImage = (String)newsJson.get("thumbImage");
    newsContents.largeImage = (String)newsJson.get("largeImage");
    newsContents.cntNRT = (String)newsJson.get("cntNRT");
    newsContents.cntCRT = (String)newsJson.get("cntCRT");
    newsContents.cntMRT = (String)newsJson.get("cntMRT");
    newsContents.cntBOT = (String)newsJson.get("cntBOT");
    newsContents.cntTotal = (String)newsJson.get("cntTotal");
    newsContents.clstrSize = (String)newsJson.get("clusterSize");
    newsContents.isClstrLeader = (String)newsJson.get("isClusterHead");

    newsContents.topNewsStatus = (String)newsJson.get("topNewsStatus");
    if (newsContents.topNewsStatus != null) {
      System.out.println("JsmRespHandler.parseToNews2Show():News:" + newsContents.id +
                         " has TopNewsStatus:" + newsContents.topNewsStatus);
    }

    // Take Key Subjects - separated by comma
    String subjStr = (String)newsJson.get("keySubjs");
    if (subjStr!=null && !subjStr.isEmpty()) {
      String[] subjStrs = subjStr.split(",");
      ArrayList<Subject> subjs = new ArrayList<>();
      for (int i=0; i<subjStrs.length; i++) {
        Subject subj = Subject.getSubject(subjStrs[i].trim());
        if (subj != null) {
          subjs.add(subj);
        }
      }
      newsContents.keySubjs = subjs;
    }

//    // Get the NewsClass info if available
//    String classes = (String)newsJson.get("classes");
//    if (classes!=null) {
//      news.tClasses = new ArrayList<TweetClass>();
//      classes = classes.substring(1, classes.length()-1);
//      String[] classPairs = classes.split(",");
//      for (int j=0; j<classPairs.length; j++) {
//        String classPair = classPairs[j];
//        classPair = classPair.substring(1, classPair.length()-1);
//        // Get the Entity & the Subject
//        Entity entity = Entity.getFromPair(classPair);
//        Subject subject = Subject.getFromPair(classPair);
//    //System.out.println("JmsRespHandler.parseTweetList():Received tweet has class:" + classPair);
//        // Add if we have both class info
//        if (entity!=null && subject!=null) {
//          // Add this TweetClass to this TweetJson if not added yet
//          news.tClasses.add(new NewsClass(entity, subject));
//    //System.out.println("JmsRespHandler.parseTweetList():Added class:" + classPair);
//        }
//      }
//    }
  }
}
