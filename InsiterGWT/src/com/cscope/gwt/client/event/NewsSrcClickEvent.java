package com.cscope.gwt.client.event;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.client.Window;

public class NewsSrcClickEvent extends GwtEvent<NewsSrcClickEventHandler> {
  public static Type<NewsSrcClickEventHandler> TYPE = new Type<NewsSrcClickEventHandler>();

  public NewsSrcClickEvent() {}

  @Override
  public Type<NewsSrcClickEventHandler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(NewsSrcClickEventHandler handler) {
//Window.alert("NewsSrcClickEvent.dispatch():calling handler");
    handler.onNewsSrcClick(this);
  }
}
