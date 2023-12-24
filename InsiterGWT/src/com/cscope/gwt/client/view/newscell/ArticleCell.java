package com.cscope.gwt.client.view.newscell;

import java.util.logging.Level;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.safecss.shared.SafeStyles;
import com.google.gwt.safecss.shared.SafeStylesBuilder;
import com.google.gwt.safecss.shared.SafeStylesUtils;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

public class ArticleCell extends AbstractCell<String> {

  // MsgTemplate to render the article head
  interface MsgTemplate extends SafeHtmlTemplates {
//    @SafeHtmlTemplates.Template("<textarea rows=\"{0}\" cols=\"{1}\" style=\"{2}\">{3}</textarea>")
//    SafeHtml render(String maxRows, String maxChars, SafeStyles styles, SafeHtml value);
    @SafeHtmlTemplates.Template("<div style=\"{0}\">{1}</div>")
    SafeHtml render(SafeStyles styles, SafeHtml value);
  }
  // Create a singleton instance of the templates used to render the cell.
  private static MsgTemplate msgTemplate = GWT.create(MsgTemplate.class);

//  SafeStylesBuilder builder = new SafeStylesBuilder();
//  SafeStyles fontFamily = SafeStylesUtils.fromTrustedNameAndValue("font-family", "GhotamRegBold");
//  builder.append(fontFamily);
//
//  int fSize = 12;
//  SafeStyles fontSize = new SafeStylesBuilder().fontSize(fSize, Style.Unit.PX).toSafeStyles();
////                          fontSize(fSize, Style.Unit.PX).fontWeight(FontWeight.BOLD).toSafeStyles();
//  builder.append(fontSize);
//  srcNameStyles = builder.toSafeStyles();

  static private SafeStyles articleHeadStyles = null;
  static {
    // Build Styles for the Article head
    SafeStylesBuilder builder = new SafeStylesBuilder();
    String fontSize = "14px";
    //String fontSize = "15px";
    SafeStyles fontSizeStyle = SafeStylesUtils.fromTrustedNameAndValue("font-size", fontSize);
    builder.append(fontSizeStyle);
//    SafeStyles fontWeight = SafeStylesUtils.fromTrustedNameAndValue("font-weight", "bold");
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
    articleHeadStyles = builder.toSafeStyles();
  }

  @Override
  public void render(Context context, String value, SafeHtmlBuilder sb) {
    SafeHtml safeText = SafeHtmlUtils.fromString(value);

//    // Build Styles for the time & counter strings
//    SafeStylesBuilder builder = new SafeStylesBuilder();
////    SafeStyles fontFamily =
////      SafeStylesUtils.fromTrustedNameAndValue("font-family", "'Sans Open',Segoe,Frutiger,Proxima-nova,Helvetica");
////    builder.append(fontFamily);
//    String fontSize = "14px";
//    //String fontSize = "15px";
//    SafeStyles fontSizeStyle = SafeStylesUtils.fromTrustedNameAndValue("font-size", fontSize);
//    builder.append(fontSizeStyle);
//    SafeStyles fontWeight = SafeStylesUtils.fromTrustedNameAndValue("font-weight", "normal");
//    builder.append(fontWeight);
//
//    // Define height to limit the number of rows in the text area
//    //String height = "45px";
//    //String height = "52px";
//    String height = "34px";
//    SafeStyles boxHeight = SafeStylesUtils.fromTrustedNameAndValue("height", height);
//    builder.append(boxHeight);
//
//    // Define a style to hide overflowed text
//    SafeStyles overtext = SafeStylesUtils.fromTrustedNameAndValue("overflow", "hidden");
//    builder.append(overtext);
//
//    // Define a style to mark text overflow with 3 dots
////    SafeStyles overflow = SafeStylesUtils.fromTrustedNameAndValue("text-overflow", "ellipsis");
////    builder.append(overflow);

    // Render the article head msg
    SafeHtml rendered = msgTemplate.render(articleHeadStyles, safeText);
    sb.append(rendered);
  }
}