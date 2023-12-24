package com.cscope.gwt.client.views.categorymenu;

import insiter.ina.news.build.NewsClass.LowerMenu;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HTML;

public class SubjectItem extends HTML implements Item<LowerMenu> {

	private LowerMenu bottomMenu;

	public SubjectItem(LowerMenu item) {
		super(item.toString());
		this.bottomMenu = item;
	}
	
	@Override
	public void highlight() {
		scrollIntoView(this.getElement());
	}

	private static native void scrollIntoView(Element element) /*-{
		element.scrollIntoView();
	}-*/;

	@Override
	public void removeHiglight() {
	}

	@Override
	public boolean isHiglighted() {
		return false;
	}

	@Override
	public HandlerRegistration addSelectHandler(final ItemSelectHandler<LowerMenu> handler) {
		return addClickHandler(new ClickHandler() {	
			@Override
			public void onClick(ClickEvent event) {
				handler.onSelect(bottomMenu);
			}
		});
	}

	@Override
	public LowerMenu getValue() {
		return bottomMenu;
	}

}
