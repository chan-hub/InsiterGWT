package com.cscope.gwt.client.views.categorymenu;

import java.util.ArrayList;
import java.util.List;

import com.cscope.gwt.client.views.categorymenu.Item.ItemSelectHandler;
import com.cscope.gwt.client.widgets.entitymenu.EntityMenu;
import com.cscope.gwt.client.widgets.subjectmenu.SubjectMenu;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import insiter.ina.news.build.NewsClass;
import insiter.ina.news.build.NewsClass.LowerMenu;
import insiter.ina.news.build.NewsClass.Entity;
import insiter.ina.news.build.NewsClass.Instance;
import insiter.ina.news.build.NewsClass.Subject;
import insiter.ina.news.build.NewsClass.UpperMenu;

public class CategoryMenu extends Composite {

	private CategoryMenu.Presenter presenter;

	@UiField
	SubjectMenu subjectMenu;

	@UiField
	EntityMenu entityMenu;

	private ItemSelectHandler<UpperMenu> topMenuSelectHandler = new ItemSelectHandler<UpperMenu>() {

		@Override
		public void onSelect(UpperMenu t) {
			entityMenu.selectItem(t);
			presenter.onEntitySelected(t);
		}
	};

	private ItemSelectHandler<LowerMenu> botMenuSelectHandler = new ItemSelectHandler<LowerMenu>() {

		@Override
		public void onSelect(LowerMenu t) {
			subjectMenu.selectItem(t);
			presenter.onSubjectSelected(t);
		}
	};

	private static CategoryMenuUiBinder uiBinder = GWT.create(CategoryMenuUiBinder.class);

	interface CategoryMenuUiBinder extends UiBinder<Widget, CategoryMenu> {
	}

	public void setUpperMenu(List<UpperMenu> menuList) {
		List<EntityItem> items = new ArrayList<EntityItem>();
		for (UpperMenu e : menuList) {
			items.add(new EntityItem(e) ); 
		}
		entityMenu.clear();
		for (EntityItem i : items) {
			i.addSelectHandler(topMenuSelectHandler);
			entityMenu.addItem(i);
		}
		entityMenu.selectItem(Entity.All);
	}

	public void setLowerMenu(LowerMenu selectedBotMenu, List<LowerMenu> menuList) {

		if (menuList != null) {
			List<SubjectItem> items = new ArrayList<SubjectItem>();
			for(LowerMenu s : menuList) {
				items.add(new SubjectItem(s) );		
			}
			subjectMenu.clear();
			for (SubjectItem i : items) {
				i.addSelectHandler(botMenuSelectHandler);
				subjectMenu.addItem(i);
			}
						
		} else { // show only either 'Subject.All' or 'Instance.all'
			subjectMenu.clear();
			SubjectItem i = null;
			if (selectedBotMenu instanceof Subject) {
		  	i = new SubjectItem(Subject.All);
			} else {
			  i = new SubjectItem(Instance.all);
			}
		  i.addSelectHandler(botMenuSelectHandler);
          subjectMenu.addItem(i);
		}
		subjectMenu.selectItem(selectedBotMenu);
		//subjectMenu.selectItem(Subject.All);
	}

	public CategoryMenu() {
		initWidget(uiBinder.createAndBindUi(this));		
	}

	public void setPresenter(CategoryMenu.Presenter presenter) {
		this.presenter=presenter;
	}

	public interface Presenter {
		public void onEntitySelected(UpperMenu topMenu);
		public void onSubjectSelected(LowerMenu botMenu);
	}

	public void setSelectedItem(NewsClass newsClass) {
//		GWT.log(" selected Item --> "+tweetClass.toStrPair());
		entityMenu.selectItem(newsClass.upperMenu);
		subjectMenu.selectItem(newsClass.lowerMenu);
	}
}
