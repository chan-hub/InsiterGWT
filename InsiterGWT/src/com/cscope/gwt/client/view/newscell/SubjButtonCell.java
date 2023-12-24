package com.cscope.gwt.client.view.newscell;

import com.cscope.gwt.client.event.ChangeNewsEvent;
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
import com.google.gwt.safecss.shared.SafeStylesUtils;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Window;
import com.google.web.bindery.event.shared.EventBus;

import insiter.ina.news.build.NewsClass;
import insiter.ina.news.build.NewsClass.Entity;
import insiter.ina.news.build.NewsClass.Instance;
import insiter.ina.news.build.NewsClass.Subject;
import insiter.ina.news.build.NewsClass.LowerMenu;
import insiter.ina.news.build.NewsClass.UpperMenu;
import insiter.ina.news.service.web.NewsMenuConfig;

public class SubjButtonCell extends AbstractCell<String> {

  // NameTemplate to render list of Subject buttons
  interface ClassTemplate extends SafeHtmlTemplates {
    @SafeHtmlTemplates.Template("&nbsp;&nbsp;<span style=\"{0}\">{1}</span>")
    SafeHtml cell(SafeStyles styles, String value);
  }

  // Singleton instances of the templates used to render this CustomCell
  private static ClassTemplate classTemplate = GWT.create(ClassTemplate.class);

  private NewsClass nClass;

  private EventBus eBus;

  public SubjButtonCell(EventBus eBus) {
    /**
     * Sink the click and key-down events. We handle click events in this
     * class. AbstractCell will handle the key-down event and call
     * onEnterKeyDown() if the user presses the enter key while the cell is
     * selected.
     */
    super("click", "keydown");
    this.eBus = eBus;
  }

  @Override
  public void render(Context context, String subjStr, SafeHtmlBuilder sb) {
    // Nothing to do if classes is empty
    if (subjStr==null || subjStr.isEmpty()) {
      return;
    }
    // Get the Subject
    Subject subj = Subject.getSubject(subjStr);
    if (subj == null) {
      // Invalid subjStr
      return;
    }
    // Instance a NewsClass representing this Subject
    this.nClass = new NewsClass(Entity.All, subj, Instance.all);

    // Render the Subject with desired style
    //int fSize = 12;
    int fSize = 14;
    SafeStylesBuilder styleBuilder =
        new SafeStylesBuilder().fontSize(fSize, Style.Unit.PX).fontWeight(FontWeight.BOLD);

    // Take Brighter for more contrast as the Entity menu may not be distinguishable - bright green
    String clrCode = "color:#2A8217";
    //String clrCode = "color:#287000";  // TOO dark green
    //String clrCode = "color:#308000";  // dark green
    //String clrCode = "color:#49be25";  // bright green
    //String clrCode = "color:#1818bb";  // bright blue
    SafeStyles fontColorStyle = SafeStylesUtils.fromTrustedString(clrCode);

    //// Use the Entity menu's highlighted background color
    //SafeStyles fontColorStyle = SafeStylesUtils.fromTrustedString("color:#2b406e");
    //SafeStyles fontColorStyle = SafeStylesUtils.fromTrustedString("color:blue");
    //SafeStyles fontColorStyle = SafeStylesUtils.fromTrustedString("float:left;width:16.0px;");
    styleBuilder.append(fontColorStyle);

    // Style for Positioning
//    SafeStyles classPosition = SafeStylesUtils.forFloat(Style.Float.RIGHT);
//    styleBuilder.append(classPosition);

    SafeHtml rendered =
        classTemplate.cell(styleBuilder.toSafeStyles(), subjStr);
    //SafeHtml rendered = classTemplate.cell(styleBuilder.toSafeStyles(), this.tClass.toSubjStr2Show());
    sb.append(rendered);
  }

  @Override
  public void onBrowserEvent(Context context, Element parent,
                             String value, NativeEvent event,
                             ValueUpdater<String> valueUpdater) {
    // Let AbstractCell handle the keydown event.
    super.onBrowserEvent(context, parent, value, event, valueUpdater);
//Window.alert("SubjButton.onBrowserEvent():Event="+event.getType());
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
//Window.alert("SubjButton.onEnterKeyDown():Event="+event.getType());
    doAction(value, valueUpdater);
  }

  public static native void consoleLog(String text)
  /*-{
      console.log(text);
  }-*/;

  /**
   * A Subject is selected. Instance a TweetClass to forward on the EventBus
   * @param value
   * @param valueUpdater
   */
  private void doAction(String value, ValueUpdater<String> valueUpdater) {
    Subject subj = Subject.getSubject(value);
    if (subj == null) {
      // Nothing to do
      return;
    }
//consoleLog("SubjButton.doAction():Received Subject:" + value);

    // Cast to UpperMenu & LowerMenu
    UpperMenu topMenu = (UpperMenu)subj;
    LowerMenu botMenu = (LowerMenu)Instance.all;
    //if (!NewsMenuConfig.isUpperMenu(subj)) {
    if (!subj.isUpperMenu()) {
      // Subject should be at the bottom menu
      topMenu = (UpperMenu)Entity.All;
      botMenu = (LowerMenu)subj;
    }
    // Instance a NewsClass with this Subject
    NewsClass clickedClass = new NewsClass(topMenu, botMenu);

    //// Instance a NewsClass with this Subject
    //NewsClass clickedClass = new NewsClass(Entity.All, subj);
    //NewsClass clickedClass = this.nClass;
    //NewsClass clickedClass = new NewsClass(value);

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