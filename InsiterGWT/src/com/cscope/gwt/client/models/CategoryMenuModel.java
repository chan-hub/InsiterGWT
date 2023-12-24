package com.cscope.gwt.client.models;

import insiter.ina.news.service.web.NewsMenu;
//import com.cscope.gwt.shared.NewsMenu;

public interface CategoryMenuModel {
	
	public interface MenuItemsCallback {
		public void onMenuItemsReceived(NewsMenu menuItems);
	}
	
	public void getMenuItems(MenuItemsCallback callback);
}
