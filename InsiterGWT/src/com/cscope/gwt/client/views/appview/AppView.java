package com.cscope.gwt.client.views.appview;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class AppView extends ResizeComposite {

	//empty but kept for symmetry
	public interface Presenter {
	}

	private static AppViewUiBinder uiBinder = GWT.create(AppViewUiBinder.class);

	interface AppViewUiBinder extends UiBinder<Widget, AppView> {
	}

	@UiField
	SimplePanel topMenu;

	@UiField
	SimplePanel categoryMenu;

	@UiField
	LayoutPanel contentArea;

	@SuppressWarnings("unused")
	private Presenter presenter;

	public AppView() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	public SimplePanel getTopMenu() {
		return topMenu;
	}

	public SimplePanel getCategoryMenu() {
		return categoryMenu;
	}

	public LayoutPanel getContentArea() {
		return contentArea;
	}

	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
}
