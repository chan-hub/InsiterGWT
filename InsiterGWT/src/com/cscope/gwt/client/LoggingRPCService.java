package com.cscope.gwt.client;

import insiter.ina.news.build.NewsClass;
//import com.cscope.gwt.shared.NewsClass;

import com.cscope.gwt.client.InsiterRPCAsync;
import com.cscope.gwt.shared.News2ShowExt;
//import com.cscope.gwt.shared.NewsMenu;
import com.cscope.gwt.shared.NewsList2Show;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

import insiter.ina.news.service.web.NewsMenu;

public class LoggingRPCService implements InsiterRPCAsync {

	private InsiterRPCAsync _rpcService;

	public LoggingRPCService(InsiterRPCAsync rpcService) {
		_rpcService = rpcService;
	}

	@Override
	public void getNewsMenu(int maxCnt, int numHours,
	                         String seqOrder, String classStr, final AsyncCallback<NewsMenu> callback) {
		if(Window.Location.getParameter("debug") != null) {
			Window.alert("LoggingRPCService - getMenuItems - REQUEST");
		}
		_rpcService.getNewsMenu(maxCnt, numHours, seqOrder, classStr, new AsyncCallback<NewsMenu>() {
			
			@Override
			public void onSuccess(NewsMenu result) {
				if (Window.Location.getParameter("debug") != null) {
					if(result == null) {
						Window.alert("LoggingRPCService - getMenuItems - RESPONSE: NULL");						
					} else {
						String em = "Entity Menu " + (result.getEntMenu() == null ? " null " : (" size: " + result.getEntMenu().size()));
						String sm = "Subject Menu "+ (result.getSubjAllMenu() == null ? " null " : (" size: " + result.getSubjAllMenu().size()));
						Window.alert("LoggingRPCService - getMenuItems - RESPONSE: " + "\n" + em + "\n" + sm );						
					}
				}
				callback.onSuccess(result);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				Window.alert("LoggingRPCService - getNewsMenu - RESPONSE FAILURE:" + caught.getMessage() ); 
				callback.onFailure(caught);
			}
		});		
	}

	//UNUSED, we use the original presenter
	//XXX
	@Override
	public void getNewsList(int maxCnt, int numHours, String seqOrder,
	                        NewsClass tClass, final AsyncCallback<NewsList2Show> callback) {
		AsyncCallback<NewsList2Show> cb = callback;

		if (Window.Location.getParameter("debug") != null) {
			Window.alert("LoggingRPCService - getNewsList - Request "
					+ "\n maxCnt: " + maxCnt 
					+ ",\n numHours: " + numHours
					+ ",\n seqOrder: " + seqOrder
					+ ",\n tClass: " + tClass.toStrPair());

			cb = new AsyncCallback<NewsList2Show>() {
				@Override
				public void onSuccess(NewsList2Show result) {
					Window.alert("LoggingRPCService - getNewsList - RESPONSE " + (result == null ? "NULL" : "len:" + result.size())); 	
					callback.onSuccess(result);
				}
				
				@Override
				public void onFailure(Throwable caught) {
					Window.alert("LoggingRPCService - getNewsList - RESPONSE FAILURE:" + caught.getMessage() ); 
					callback.onFailure(caught);
				}
			};
		}
		_rpcService.getNewsList(maxCnt, numHours, seqOrder, tClass, cb);
	}

	@Override
	public void getPageTagLine(AsyncCallback<String> callback) {
		_rpcService.getPageTagLine(callback);
	}

	@Override
	public void getNews(String id, String seqOrder, AsyncCallback<News2ShowExt> callback) {
		_rpcService.getNews(id, seqOrder, callback);
	}

	@Override
	public void putNews(String id, NewsClass[] tClasses, String rtCat, String adminId, AsyncCallback<String> callback) {
		_rpcService.putNews(id, tClasses, rtCat, adminId, callback);
	}

	@Override
	public void putFeedback(String feedback, AsyncCallback<String> callback) {
		_rpcService.putFeedback(feedback, callback);
	}

	@Override
	public void adminLogin(String id, String passwd, AsyncCallback<String> callback) {
		_rpcService.adminLogin(id, passwd, callback);
	}

	@Override
	public void adminLogout(String id, AsyncCallback<String> callback) {
		_rpcService.adminLogout(id, callback);
	}

	@Override
	public void addAdmin(String id, String passwd, String emailAddr, AsyncCallback<String> callback) {
		_rpcService.addAdmin(id, passwd, emailAddr, callback);
	}

	@Override
	public void rmAdmin(String id, AsyncCallback<String> callback) {
		_rpcService.rmAdmin(id, callback);
	}

}