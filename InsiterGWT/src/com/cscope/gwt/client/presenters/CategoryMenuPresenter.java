package com.cscope.gwt.client.presenters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.cscope.gwt.client.ClientFactory;
import com.cscope.gwt.client.event.ChangeNewsEvent;
import com.cscope.gwt.client.models.CategoryMenuModel;
import com.cscope.gwt.client.models.CategoryMenuModel.MenuItemsCallback;
import com.cscope.gwt.client.views.categorymenu.CategoryMenu;
//import com.cscope.gwt.shared.NewsClass;
//import com.cscope.gwt.shared.NewsClass.LowerMenu;
//import com.cscope.gwt.shared.NewsClass.Entity;
//import com.cscope.gwt.shared.NewsClass.Instance;
//import com.cscope.gwt.shared.NewsClass.Subject;
//import com.cscope.gwt.shared.NewsClass.UpperMenu;
//import com.cscope.gwt.shared.NewsMenu;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.web.bindery.event.shared.EventBus;

import insiter.ina.news.build.NewsClass;
import insiter.ina.news.build.NewsClass.LowerMenu;
import insiter.ina.news.build.NewsClass.Entity;
import insiter.ina.news.build.NewsClass.Instance;
import insiter.ina.news.build.NewsClass.Subject;
import insiter.ina.news.build.NewsClass.UpperMenu;
import insiter.ina.news.service.web.NewsMenu;

public class CategoryMenuPresenter implements CategoryMenu.Presenter, MenuItemsCallback {
	private ClientFactory cf;
	
	private EventBus eBus;
	private CategoryMenuModel am;
	private CategoryMenu categoryMenuView;
	private NewsMenu menuItems;

	private UpperMenu selectedUpperMenu = Entity.All;     // default is All
	private LowerMenu selectedLowerMenu = Subject.All; // default is All

	public CategoryMenuPresenter(CategoryMenuModel am, ClientFactory cf, EventBus eBus) {
		this.cf = cf;
		this.eBus = eBus;
		this.am = am;
	}

	public void go(HasWidgets.ForIsWidget panel) {
		categoryMenuView = cf.getCategoryMenuView(this);

		// Request menu entries to the server
		am.getMenuItems(this);

		panel.clear();
		panel.add(categoryMenuView);
		
		//selectedEntity  = Entity.All;
		//fire All/All request
		//eBus.fireEvent(new CategorySelectedEvent(new TweetClass(selectedEntity, Subject.All)));
	}
	
	@Override
	public void onMenuItemsReceived(NewsMenu _menuItems) {
		if(_menuItems == null) { 
			GWT.log("GOT NULL LIST For MENU");
			NewsMenu tm = new NewsMenu();
			tm.setUpperMenu(new ArrayList<UpperMenu>(Arrays.asList(NewsClass.Entity.All)));
			tm.setLowerAllMenu(new ArrayList<LowerMenu>(Arrays.asList(NewsClass.Subject.All)));
			tm.setLowerMenu(Entity.All, new ArrayList<LowerMenu>(Arrays.asList(NewsClass.Subject.All)));

			this.menuItems = tm;

		} else {
			this.menuItems = _menuItems;
		}
		// Initialize Entity Menu
		List<UpperMenu> entMenuList = menuItems.getEntMenu();
		if (entMenuList == null) 
			entMenuList = new ArrayList<UpperMenu>(Arrays.asList(NewsClass.Entity.All));

		// Initialize Subject Menu
		List<LowerMenu> sm = menuItems.getSubjAllMenu();
		if (sm == null)
			sm = new ArrayList<LowerMenu>(Arrays.asList(NewsClass.Subject.All));
		
		categoryMenuView.setUpperMenu(entMenuList);
		categoryMenuView.setLowerMenu(Subject.All, sm);
			
		selectedUpperMenu  = Entity.All;
		selectedLowerMenu = Subject.All;
		History.fireCurrentHistoryState();
		//onEntitySelected(selectedEntity);
	}

	@Override
	public void onSubjectSelected(LowerMenu botMenuItem) {
		//fire Ent/All request
		//Window.alert("onSubjectSelected");
	  selectedLowerMenu = botMenuItem;
		eBus.fireEvent(new ChangeNewsEvent(new NewsClass(selectedUpperMenu, selectedLowerMenu)));
	}
	
	@Override
	public void onEntitySelected(UpperMenu topMenu) {
		//fire Ent/All request
		//Window.alert("onEntitySelected");
    selectedUpperMenu = topMenu;
		// Change to select Subject.All when a new Entity is selected (Apr 14, 2016)
	  if (topMenu instanceof Entity) {
		  selectedLowerMenu = Subject.All;
	  } else {
	    selectedLowerMenu = Instance.all;
	  }
		// If this selected entity is 'All' OR if the current subject is belonged to
		// the selected entity, then keep the current subject, 'selectedSubject'.
		// Otherwise, use the 'Subject.All'
//		if (entity!=Entity.All &&
//		    (!this.menuItems.getSubjMenu(entity).contains(selectedSubject))) {
//		  selectedSubject = Subject.All;
//		}
		eBus.fireEvent(new ChangeNewsEvent(new NewsClass(topMenu, selectedLowerMenu)));
	}

	public void notifyNewsClassChange(NewsClass newsClass) {
	  //Window.alert("setting tc: " + tweetClass.toStrPair());
	  // Change the LowerMenu for the selected UpperMenu item
		List<LowerMenu> m = menuItems.getLowerMenu(newsClass.upperMenu);
		categoryMenuView.setLowerMenu(selectedLowerMenu, m);

		selectedUpperMenu = newsClass.upperMenu; 
		categoryMenuView.setSelectedItem(newsClass);
	}
}
