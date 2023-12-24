package com.cscope.gwt.client.views;

import com.cscope.gwt.client.ClientFactory;
import com.cscope.gwt.client.views.appview.AppView;
import com.cscope.gwt.client.views.categorymenu.CategoryMenu;

public class ClientFactoryImpl implements ClientFactory {

  private AppView appView;
	private CategoryMenu categoryMenu;

  @Override
  public AppView getAppView(AppView.Presenter presenter) {
    if (appView == null) {
      appView = new AppView();
    }
    appView.setPresenter(presenter);
    return appView;
  }

  @Override
  public CategoryMenu getCategoryMenuView(CategoryMenu.Presenter presenter) {
    if (categoryMenu == null) {
      categoryMenu = new CategoryMenu();
    }
    categoryMenu.setPresenter(presenter);
    return categoryMenu;
  }

}
