package com.cscope.gwt.client.view.newscell;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.safecss.shared.SafeStyles;
import com.google.gwt.safecss.shared.SafeStylesBuilder;
import com.google.gwt.safecss.shared.SafeStylesUtils;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.client.SafeHtmlTemplates.Template;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

public class _NewsTitleCell extends AbstractCell<String> {

  // TitleTemplate to render the news title
  interface TitleTemplate extends SafeHtmlTemplates {
    @SafeHtmlTemplates.Template("<div style=\"{0}\">{1}</div>")
    SafeHtml cell(SafeStyles styles, String value);
  }

  // Singleton instances of the templates used to render this CustomCell
  private static TitleTemplate titleTemplate = GWT.create(TitleTemplate.class);

  static private SafeStyles titleStyles = null;
  static {
    // Font family for the Article head
    SafeStylesBuilder builder = new SafeStylesBuilder();
    SafeStyles fontFamily = SafeStylesUtils.fromTrustedNameAndValue("font-family", "GhotamBold");
    builder.append(fontFamily);

    // Font size
    String fontSize = "15px";
    SafeStyles fontSizeStyle = SafeStylesUtils.fromTrustedNameAndValue("font-size", fontSize);
    builder.append(fontSizeStyle);
    SafeStyles fontWeight = SafeStylesUtils.fromTrustedNameAndValue("font-weight", "normal");
    builder.append(fontWeight);

    // Define height to limit the number of rows in the text area
    //String height = "45px";
    //String height = "52px";
    String height = "34px";
    SafeStyles boxHeight = SafeStylesUtils.fromTrustedNameAndValue("height", height);
    builder.append(boxHeight);

    // Define a style to hide overflowed text
    SafeStyles overtext = SafeStylesUtils.fromTrustedNameAndValue("overflow", "hidden");
    builder.append(overtext);

    // Define a style to mark text overflow with 3 dots
//    SafeStyles overflow = SafeStylesUtils.fromTrustedNameAndValue("text-overflow", "ellipsis");
//    builder.append(overflow);
    titleStyles = builder.toSafeStyles();
  }

  @Override
  public void render(Context context, String value, SafeHtmlBuilder sb) {
    // Render the title with desired style
//    SafeStylesBuilder builder = new SafeStylesBuilder();
//    SafeStyles fontFamily = SafeStylesUtils.fromTrustedNameAndValue("font-family", "GhotamBold");
//    builder.append(fontFamily);
//
//    SafeStyles fontSize = SafeStylesUtils.fromTrustedNameAndValue("font-size", "15px");
//    builder.append(fontSize);
//    SafeHtml rendered = titleTemplate.cell(builder.toSafeStyles(), value);
    SafeHtml rendered = titleTemplate.cell(titleStyles, value);
    sb.append(rendered);
  }
}