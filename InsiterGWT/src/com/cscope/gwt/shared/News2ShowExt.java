package com.cscope.gwt.shared;

import java.io.Serializable;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

import insiter.ina.news.service.web.NewsContents;

public class News2ShowExt extends NewsContents implements IsSerializable, Serializable {
  static private final long serialVersionUID = 100L;

  public enum RtCategory {
    HotNews,  // Lot of NRTs & CRTs with portion of MRTs too, total RTs minimum 1000
    OrdNews,  // NRTs is about 50% of CRT & MRT combined, total RTs minimum 300
    Ads,      // Excessive NRTs or CRTs (not both), with minimal (or zero) MRTs (NRT>90%, CRT>90%)
    Personal, // Experience, opinion, etc
    Spam,     // similar to Ads, but excessive Bot behavior, total RTs minimum 1000
    Composite;// All other

    static public RtCategory getCategory(String catStr) {
      RtCategory rtCat = null;
      try {
        rtCat = RtCategory.valueOf(catStr);
      } catch (Exception e) {
        
      }
      return rtCat;
    }
  }

  public String mdaUrl;
  public String mdaUrlHttps;
  public String mdaUrlSize;
  // "sizes":{
  //   "large":{"w":1024,"h":501,"resize":"fit"},
  //   "small":{"w":340,"h":166,"resize":"fit"},
  //   "thumb":{"w":150,"h":150,"resize":"crop"},
  //   "medium":{"w":600,"h":293,"resize":"fit"}
  //  }
  public String mdaUrlType;
  public boolean classUpdated;
  public RtCategory rtCat;
  public List<NewsContents> relatedTweets;

  public News2ShowExt() {}
}
