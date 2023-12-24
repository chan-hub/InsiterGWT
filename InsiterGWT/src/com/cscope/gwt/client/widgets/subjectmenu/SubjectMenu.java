package com.cscope.gwt.client.widgets.subjectmenu;

import com.cscope.gwt.client.widgets.HorizontalScrollingMenu.Base.BaseMenu;
import com.cscope.gwt.client.widgets.subjectmenu.css.SubjectRes;
import com.google.gwt.core.shared.GWT;
import insiter.ina.news.build.NewsClass.LowerMenu;

public class SubjectMenu extends BaseMenu<LowerMenu> {
//	public SubjectMenu(SubjectRes res) {
//		super(res);
//	}
	
	public SubjectMenu() {
		super((SubjectRes)GWT.create(SubjectRes.class));
	}
//	
//	private static SubjectRes getStatcResources() {
//		return GWT.create(SubjectRes.class);
//	}

}
