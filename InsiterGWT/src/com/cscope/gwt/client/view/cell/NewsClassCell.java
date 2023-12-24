package com.cscope.gwt.client.view.cell;

import com.cscope.gwt.client.event.ChangeNewsEvent;
import insiter.ina.news.build.NewsClass;
//import com.insiter.ina.gwt.shared.NewsClass;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.safecss.shared.SafeStyles;
import com.google.gwt.safecss.shared.SafeStylesBuilder;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.web.bindery.event.shared.EventBus;

public class NewsClassCell extends AbstractCell<String> {

  // NameTemplate to render list of TweetClass
  interface ClassTemplate extends SafeHtmlTemplates {
    @SafeHtmlTemplates.Template("&nbsp;&nbsp;<span style=\"{0}\">{1}</span>")
    SafeHtml cell(SafeStyles styles, String value);
  }

  // Singleton instances of the templates used to render this CustomCell
  private static ClassTemplate classTemplate = GWT.create(ClassTemplate.class);

  private NewsClass tClass;

  private EventBus eBus;

  public NewsClassCell(EventBus eBus) {
    /*
     * Sink the click and keydown events. We handle click events in this
     * class. AbstractCell will handle the keydown event and call
     * onEnterKeyDown() if the user presses the enter key while the cell is
     * selected.
     */
    super("click", "keydown");
    this.eBus=eBus;
  }

  @Override
  public void render(Context context, String classStr, SafeHtmlBuilder sb) {
    // Nothing to do if classes is empty
    if (classStr==null || classStr.isEmpty()) {
      return;
    }
    // Extract the Entity & the Subject
    int index = classStr.indexOf(':');
    if (index<=0) {
      // Invalid classStr
      return;
    }
    // Instance a NewsClass to save
    this.tClass = new NewsClass(classStr);
//    String entityStr = classStr.substring(0, index);
//    Entity entity = Entity.getEntity(entityStr);
//    Subject subject = Subject.getSubject(classStr.substring(index+1));
//    this.tClass = new NewsClass(entity, subject);

    // Render the Subject with desired style
    //int fSize = 12;
    int fSize = 14;
    SafeStylesBuilder styleBuilder = new SafeStylesBuilder().
                              fontSize(fSize, Style.Unit.PX).fontWeight(FontWeight.BOLD);
    // Style for Positioning
//    SafeStyles classPosition = SafeStylesUtils.forFloat(Style.Float.RIGHT);
//    styleBuilder.append(classPosition);
    SafeHtml rendered =
        classTemplate.cell(styleBuilder.toSafeStyles(), this.tClass.lowerMenuStr2Show());
    //SafeHtml rendered = classTemplate.cell(styleBuilder.toSafeStyles(), this.tClass.toSubjStr2Show());
    sb.append(rendered);
  }

  @Override
  public void onBrowserEvent(Context context, Element parent,
                             String value, NativeEvent event,
                             ValueUpdater<String> valueUpdater) {
    // Let AbstractCell handle the keydown event.
    super.onBrowserEvent(context, parent, value, event, valueUpdater);

    // Handle the click event.
    if ("click".equals(event.getType())) {
      // Ignore clicks that occur outside of the outermost element.
      EventTarget eventTarget = event.getEventTarget();
      if (parent.getFirstChildElement().isOrHasChild(Element.as(eventTarget))) {
        doAction(value, valueUpdater);
      }
    }
  }

  /**
   * onEnterKeyDown is called when the user presses the ENTER key
   * You are not required to override this method, but its a
   * common convention that allows your cell to respond to key events.
   */
  @Override
  protected void onEnterKeyDown(Context context, Element parent, String value,
                                NativeEvent event, ValueUpdater<String> valueUpdater) {
    doAction(value, valueUpdater);
  }

  /**
   * A Subject is selected. Instance a TweetClass to forward on the EventBus
   * @param value
   * @param valueUpdater
   */
  private void doAction(String value, ValueUpdater<String> valueUpdater) {
    NewsClass clickedClass = new NewsClass(value);
//    //Entity entity = Entity.getFromPair(value);
//    // Use 'All' to fetch ALL Entities related to the Subject selected
//    Entity entity = Entity.All;
//    Subject subject = Subject.getFromPair(value);
//    TweetClass clickedClass = new TweetClass(entity, subject);
//    //Tektweet.eventBus.fireEvent(new ChangeTweetsEvent(clickedClass));
//    GWT.log("Request --> "+clickedClass.toStrPair());
    eBus.fireEvent(new ChangeNewsEvent(clickedClass));
  }
}