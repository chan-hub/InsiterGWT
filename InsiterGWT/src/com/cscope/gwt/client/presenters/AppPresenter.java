package com.cscope.gwt.client.presenters;

import com.cscope.gwt.client.ClientFactory;
import com.cscope.gwt.client.event.ChangeNewsEvent;
import com.cscope.gwt.client.event.ChangeNewsEventHandler;
import com.cscope.gwt.client.models.AppModel;
import com.cscope.gwt.client.presenter.NewsPresenter;
import com.cscope.gwt.client.view.NewsListView;
import com.cscope.gwt.client.views.appview.AppView;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.storage.client.Storage;
import com.google.gwt.storage.client.StorageMap;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.web.bindery.event.shared.EventBus;
import insiter.ina.news.build.NewsClass;
import insiter.ina.news.build.NewsClass.Entity;
import insiter.ina.news.build.NewsClass.Instance;
import insiter.ina.news.build.NewsClass.Subject;
import insiter.ina.news.build.NewsClass.LowerMenu;
import insiter.ina.news.build.NewsClass.UpperMenu;

public class AppPresenter implements AppView.Presenter {
	private AppView appView;
	private AppModel appModel;
	private EventBus eventBus;
	private ClientFactory clientFactory;

	static public int getScrollLocH() {
    int loc = 0;
    Storage stockStore = Storage.getLocalStorageIfSupported();
    if (stockStore != null) {
      String locStr = stockStore.getItem("ScrollLocH");
      if (locStr!=null && !locStr.isEmpty()) {
        loc = Integer.parseInt(locStr);
      }
    }
    return loc;
  }
  static public void putScrollLocH(int loc) {
    Storage stockStore = Storage.getLocalStorageIfSupported();
    if (stockStore != null) {
      stockStore.setItem("ScrollLocH", loc+"");
      //Window.alert("putScrollH():Stored=" + loc);
    } else {
      //Window.alert("putScrollH():Can't store");
    }
  }

  static public int getScrollLocV() {
    int loc = 0;
    Storage stockStore = Storage.getLocalStorageIfSupported();
    if (stockStore != null) {
      String locStr = stockStore.getItem("ScrollLocV");
      if (locStr!=null && !locStr.isEmpty()) {
        loc = Integer.parseInt(locStr);
      }
    }
    return loc;
  }
  static public void putScrollLocV(int loc) {
    Storage stockStore = Storage.getLocalStorageIfSupported();
    if (stockStore != null) {
      stockStore.setItem("ScrollLocV", loc+"");
      //Window.alert("putScrollV():Stored=" + loc);
    } else {
      //Window.alert("putScrollV():Can't store");
    }
  }

	public AppPresenter(AppModel am, ClientFactory cf, EventBus eb) {
		this.appModel = am;
		this.clientFactory = cf;
		this.eventBus = eb;
	}

	public void go(HasWidgets.ForIsWidget panel) {
		this.appView = this.clientFactory.getAppView(this);

		// Start presenters
		final CategoryMenuPresenter cpm = new CategoryMenuPresenter(appModel, clientFactory, eventBus);
		panel.clear();
		panel.add(appView);

		// Add a ChangeNewsEvent Handler to its EventBus
		eventBus.addHandler(ChangeNewsEvent.TYPE, new ChangeNewsEventHandler() {
			@Override
			public void onChangeNews(ChangeNewsEvent event) {
				appView.getContentArea().clear();
//String URL = Window.Location.getHref();
//String hash = Window.Location.getHash();
//Window.alert("URL='"+URL+"' hash='"+hash + "' AppPresenter.onChangeNews():NewsClass='" + event.getNewsClass() + "'");
		    NewsPresenter presenter =
		      new NewsPresenter(appModel.getRPCService(), eventBus,
		      		              new NewsListView(eventBus), event.getNewsClass());
		    presenter.go(appView.getContentArea());
		    cpm.notifyNewsClassChange(event.getNewsClass());
		    // Need this 'false' to direct not to fire a new Event
		    History.newItem(event.getNewsClass().toToken(), false);
			}
		});

		// Let CategoryMenuPresenter get started
		cpm.go(appView.getCategoryMenu());

		// Then add ValueChangeHandler to History
		History.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
//String URL = Window.Location.getHref();
//Window.alert("URL='"+URL+"' ValueChangeEvent:event.getValue()='" + event.getValue() + "'");
				NewsClass nClass = new NewsClass(event.getValue());
				if (nClass.getUpperMenu()==null && nClass.getLowerMenu()==null) {
				  // Taking default if both Menus are null
					nClass = new NewsClass(NewsClass.Entity.All, NewsClass.Subject.All);
				} else if (nClass.getUpperMenu()==null) {
				  // If the UpperMenu is null, find out the LowerMenu's type
				  if (nClass.getLowerMenu() instanceof Subject)
				    // Taking NewsClass with its Entity.All top Menu & the Subject of botMenu
					  nClass = new NewsClass(NewsClass.Entity.All, (Subject)nClass.getLowerMenu());
				  else
				    // Taking NewsClass with its Subject.All top Menu & the Instance of botMenu
				    nClass = new NewsClass((UpperMenu)NewsClass.Subject.All, (LowerMenu)(Instance)nClass.getLowerMenu());
				} else if (nClass.getLowerMenu()==null) {
          if (nClass.getUpperMenu() instanceof Entity)
            // Taking NewsClass with its Subject.All bot Menu
            nClass=new NewsClass(nClass.getUpperMenu(), (LowerMenu)Subject.All);
          else
            // Taking NewsClass with its Instance.all bot Menu
            nClass=new NewsClass(nClass.getUpperMenu(), (LowerMenu)Instance.all);
				}
				String lastToken = nClass.toToken();
				History.newItem(lastToken, false);
//String URL = Window.Location.getHref();
//Window.alert("URL='"+URL+"' ValueChangeEvent:event.getValue()='" + event.getValue() +
//             "' newMenu:'" + lastToken + '\'');
				eventBus.fireEvent(new ChangeNewsEvent(nClass));
			}
		});
	}
}
