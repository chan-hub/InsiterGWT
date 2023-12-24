package com.cscope.gwt.client.views.categorymenu;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HTML;

//import com.cscope.gwt.shared.NewsClass.UpperMenu;
import insiter.ina.news.build.NewsClass.UpperMenu;

public class EntityItem extends HTML implements Item<UpperMenu> {

	private UpperMenu topMenu;

	public EntityItem(UpperMenu item) {
		super(item.toString());
		this.topMenu = item;
	}
	
	@Override
	public void highlight() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeHiglight() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isHiglighted() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public HandlerRegistration addSelectHandler(final ItemSelectHandler<UpperMenu> handler) {
		return addClickHandler(new ClickHandler() {	
			@Override
			public void onClick(ClickEvent event) {
				handler.onSelect(topMenu);
			}
		});
	}

	@Override
	public UpperMenu getValue() {
		return topMenu;
	}

}
