package com.cscope.gwt.client;

import insiter.ina.news.build.NewsClass;
import insiter.ina.news.service.web.NewsMenu;

import com.cscope.gwt.client.InsiterRPC;
import com.cscope.gwt.client.InsiterRPCAsync;
import com.cscope.gwt.client.models.AppModel;
//import com.cscope.gwt.shared.NewsMenu;
import com.cscope.gwt.shared.NewsList2Show;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

import insiter.ina.news.service.web.NewsMenu;

public class RPCDataModel implements AppModel {

  private InsiterRPCAsync rpcService = (InsiterRPCAsync)GWT.create(InsiterRPC.class);

  @Override
  public void getMenuItems(final MenuItemsCallback callback) {
    int maxCnt = 300;
	int numHours = 24 * 7;
	rpcService.getNewsMenu(maxCnt, numHours, SeqOrder.Time.toString(), "All:All",
      new AsyncCallback<NewsMenu>() {
        @Override
        public void onSuccess(NewsMenu result) {
          callback.onMenuItemsReceived(result);
        }
        @Override
        public void onFailure(Throwable caught) {
          Window.alert("ERROR During Menu Request: " + caught.getMessage());
        }
      }
	);    // rpcService.getNewsMenu();
  }

  static public String defOrder = SeqOrder.Trend.toString();
  public enum SeqOrder {
    Trend, Time
  }

  @Override
  public void getNewsList(NewsClass newsClass, AsyncCallback<NewsList2Show> callback) {
    int maxCnt = 200;
	//int maxCnt = 100;
	int numHours = 24;
    rpcService.getNewsList(maxCnt, numHours, defOrder, newsClass, callback);
  }

  @Override
  public InsiterRPCAsync getRPCService() {
	return rpcService;
  }
}
//