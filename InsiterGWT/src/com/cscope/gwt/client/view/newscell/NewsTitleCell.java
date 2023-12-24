package com.cscope.gwt.client.view.newscell;

import com.cscope.gwt.client.event.NewsSrcClickEvent;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.cell.client.Cell.Context;
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
import com.google.gwt.safehtml.client.SafeHtmlTemplates.Template;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.user.client.Window;
import com.google.web.bindery.event.shared.EventBus;

public class NewsTitleCell extends AbstractCell<String> {

  // TitleTemplate to render the news title with an anchor
  interface TitleTemplate extends SafeHtmlTemplates {
//    @Template("<span style=\"width:100%; text-align:right;\"><a href=\"{0}\">{1}</a></span>")
//    SafeHtml anchor(SafeUri href, String name);

    @SafeHtmlTemplates.Template("<div><a href=\"{2}\"><div style=\"{0}\">{1}</div></a></div>")
    SafeHtml anchorCell(SafeStyles styles, String title, String url);
  }
  // 
  static String titleAndUrlBinder = "\\\\";

  // Singleton instances of the templates used to render this CustomCell
  private static TitleTemplate titleTemplate = GWT.create(TitleTemplate.class);

  static private SafeStyles titleStyles = null;
  static {
    // Font family for the Article head
    SafeStylesBuilder builder = new SafeStylesBuilder();
    SafeStyles fontFamily = SafeStylesUtils.fromTrustedNameAndValue("font-family", "GhotamBold");
    builder.append(fontFamily);

    // Font size
    String fontSize = "16px";  // Increased after removing Article - 17px still big
    //String fontSize = "17px";  // Increased after removing Article - 18px still big
    //String fontSize = "18px";  // Increased after removing Article - 22px too big
    //String fontSize = "22px";  // Increased after removing Article
    //String fontSize = "15px";
    SafeStyles fontSizeStyle = SafeStylesUtils.fromTrustedNameAndValue("font-size", fontSize);
    builder.append(fontSizeStyle);
    SafeStyles fontWeight = SafeStylesUtils.fromTrustedNameAndValue("font-weight", "normal");
    builder.append(fontWeight);
    SafeStyles fontColor = SafeStylesUtils.fromTrustedNameAndValue("color", "black");
    builder.append(fontColor);

    // Define height to limit the number of lines in the text area
    String height = "36px";  // Increased after removing Article
    //String height = "38px";  // Increased after removing Article
    //String height = "34px";
    //String height = "45px";
    //String height = "52px";
    SafeStyles boxHeight = SafeStylesUtils.fromTrustedNameAndValue("height", height);
 // Delete height to remove the upper margin
 //   builder.append(boxHeight);

    // Define a style to hide overflowed text
    SafeStyles overtext = SafeStylesUtils.fromTrustedNameAndValue("overflow", "hidden");
    builder.append(overtext);

    // Define a style to mark text overflow with 3 dots
    titleStyles = builder.toSafeStyles();
  }

  private EventBus eBus;

  public NewsTitleCell() {}

  public NewsTitleCell(EventBus eb) {
    /**
     * Sink the click and key-down events.
     */
    super("click", "keydown");
    this.eBus = eb;
  }

  @Override
  public void render(Context context, String value, SafeHtmlBuilder sb) {
    // Render the title in desired style with the anchor URL
    String title = value;
    String url = "";
    int index = value.indexOf(NewsTitleCell.titleAndUrlBinder);
    if (index > 0) {
      title = value.substring(0, index);
      url = value.substring(index+(NewsTitleCell.titleAndUrlBinder.length()));
    }
    SafeHtml rendered = titleTemplate.anchorCell(titleStyles, title, url);
    sb.append(rendered);
  }

  @Override
  public void onBrowserEvent(Context context, Element parent,
                             String value, NativeEvent event,
                             ValueUpdater<String> valueUpdater) {
  //Window.alert("onBrowserEvent():Event="+event.getType());

  // Handle the click event.
    if ("click".equals(event.getType())) {

      // Fire event to save the scroll position by the NewsListGrid
      eBus.fireEvent(new NewsSrcClickEvent());
    }
    // Let AbstractCell handle the rest
    super.onBrowserEvent(context, parent, value, event, valueUpdater);
  }
}