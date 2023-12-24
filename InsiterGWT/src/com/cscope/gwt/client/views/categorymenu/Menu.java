package com.cscope.gwt.client.views.categorymenu;

import com.google.gwt.user.client.ui.IsWidget;

public interface Menu <S, T extends Item<S>> extends IsWidget {
	
	public void clear();
	public int addItem(T item);
	public S getSelectedItem();
	void selectItem(S v);
	
}
