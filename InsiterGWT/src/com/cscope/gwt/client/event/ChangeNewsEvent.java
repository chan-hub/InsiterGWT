package com.cscope.gwt.client.event;

//import com.cscope.gwt.shared.NewsClass;
import insiter.ina.news.build.NewsClass;
import com.google.gwt.event.shared.GwtEvent;

public class ChangeNewsEvent extends GwtEvent<ChangeNewsEventHandler> {
  public static Type<ChangeNewsEventHandler> TYPE = new Type<ChangeNewsEventHandler>();

  private final NewsClass tClass;

  public ChangeNewsEvent(NewsClass tClass) {
    this.tClass = tClass;
  }

  public NewsClass getNewsClass() { return this.tClass; }

  @Override
  public Type<ChangeNewsEventHandler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(ChangeNewsEventHandler handler) {
    handler.onChangeNews(this);
  }
}
