package com.cscope.gwt.client.widgets.entitymenu;

import com.cscope.gwt.client.widgets.HorizontalScrollingMenu.Base.BaseMenu;
import com.cscope.gwt.client.widgets.entitymenu.css.EntityRes;
import com.google.gwt.core.shared.GWT;
import insiter.ina.news.build.NewsClass.UpperMenu;

public class EntityMenu extends BaseMenu<UpperMenu> {

	public EntityMenu(EntityRes res) {
		super(res);
	}

	public EntityMenu() {
		super((EntityRes)GWT.create(EntityRes.class));
	}
}
