package com.cscope.gwt.client.widgets.entitymenu.css;

import com.cscope.gwt.client.widgets.HorizontalScrollingMenu.Base.css.HorizontalScrollingMenuResources;

public interface EntityRes extends HorizontalScrollingMenuResources {
//	public static final EntityRes INSTANCE =  GWT.create(EntityRes.class);

	@Source(EntityList.PATH)
	EntityList horizontalScrollingMenu();
		
}
