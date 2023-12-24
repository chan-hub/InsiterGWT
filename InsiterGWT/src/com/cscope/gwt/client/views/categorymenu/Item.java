package com.cscope.gwt.client.views.categorymenu;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.IsWidget;

public interface Item<T> extends IsWidget {

	//profide payload on select
	public interface ItemSelectHandler<T> {
		public void onSelect(T t);
	}
	
	public T getValue();
	
	public void highlight();
	public void removeHiglight();
	public boolean isHiglighted();
	
	public HandlerRegistration addSelectHandler(ItemSelectHandler<T> handler);
	
}
