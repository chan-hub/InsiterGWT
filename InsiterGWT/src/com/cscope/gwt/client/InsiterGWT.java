package com.cscope.gwt.client;

import com.cscope.gwt.client.models.AppModel;
import com.cscope.gwt.client.presenters.AppPresenter;
import com.cscope.gwt.client.views.ClientFactoryImpl;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.SimpleEventBus;

public class InsiterGWT implements EntryPoint {

  //@Override
  public void onModuleLoad() {
    EventBus eventBus = new SimpleEventBus();

    // Factory to provide views creation and caching
	ClientFactory clientFactory = new ClientFactoryImpl();

	// SAMPLE DATA MODEL
	// AppModel am=new SampleOfflineAppModel();

	// RPC DATA MODEL
	AppModel appModel = new RPCDataModel();
	AppPresenter appPresenter = new AppPresenter(appModel, clientFactory, eventBus);
	appPresenter.go(RootLayoutPanel.get());
  }
}
