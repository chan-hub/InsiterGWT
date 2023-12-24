package com.cscope.gwt.client;

import com.cscope.gwt.client.views.appview.AppView;
import com.cscope.gwt.client.views.categorymenu.CategoryMenu;

public interface ClientFactory {
	//Main View
	CategoryMenu getCategoryMenuView(CategoryMenu.Presenter presenter);

	AppView getAppView(AppView.Presenter appPresenter);

}
