package com.cscope.gwt.client.widgets.HorizontalScrollingMenu.Base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cscope.gwt.client.views.categorymenu.Item;
import com.cscope.gwt.client.views.categorymenu.Menu;
import com.cscope.gwt.client.widgets.HorizontalScrollingMenu.Base.css.HorizontalScrollingMenuResources;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

public abstract class BaseMenu<T> extends Composite implements Menu<T,Item<T>> {

	private static BaseMenuUiBinder uiBinder = GWT.create(BaseMenuUiBinder.class);

	@SuppressWarnings("rawtypes")
	interface BaseMenuUiBinder extends UiBinder<Widget, BaseMenu> {
	}

	@UiField(provided=true)
	HorizontalScrollingMenuResources resources;

	@UiField
	FlowPanel panel;

	private List<Item<T>> itemList = new ArrayList<Item<T>>();

	private Map<T,Item<T>> itemsMap = new HashMap<T,Item<T>>();

	private T selectedItem = null;

	public BaseMenu(HorizontalScrollingMenuResources res) {		
		this.resources = injectRes(res);	
		initWidget(uiBinder.createAndBindUi(this));
	}

	private HorizontalScrollingMenuResources injectRes(HorizontalScrollingMenuResources res) {
		HorizontalScrollingMenuResources r = res == null ? getDefaultResources() : res;
		r.horizontalScrollingMenu().ensureInjected();
		return r;
	}
	
	private static HorizontalScrollingMenuResources RESOURCES = null;
	private HorizontalScrollingMenuResources getDefaultResources() {
		if (RESOURCES == null)
			RESOURCES = GWT.create(HorizontalScrollingMenuResources.class);
		return RESOURCES;
	}

	@Override
	public void clear() {
		panel.clear();
		itemList.clear();
		itemsMap.clear();
		selectedItem = null;
		panel.getElement().setScrollLeft(0);
		panel.getElement().setScrollTop(0);
	}

	@Override
	public int addItem(Item<T> item) {
		panel.add(item);
		itemList.add(item);
		itemsMap.put(item.getValue(), item);
		
		item.asWidget().getElement().addClassName(resources.horizontalScrollingMenu().nav_item());
		item.asWidget().getElement().addClassName(resources.horizontalScrollingMenu().item());
		
		return itemList.size()-1; // itemList.indexOf(item);
	}
	
	@Override
	public void selectItem(T v) {
		if (this.selectedItem != null) {
			Item<T> i = itemsMap.get(this.selectedItem);
			if(i != null) {
				i.removeHiglight();			
				i.asWidget().getElement().removeClassName(resources.horizontalScrollingMenu().item_highlighted());
			}
		}
		this.selectedItem = v;
		
		Item<T> i = itemsMap.get(v);
		if (i != null) {
			i.highlight();
			i.asWidget().getElement().addClassName(resources.horizontalScrollingMenu().item_highlighted());
		}
	}

	@Override
	public T getSelectedItem() {
		return this.selectedItem;
	}

}
