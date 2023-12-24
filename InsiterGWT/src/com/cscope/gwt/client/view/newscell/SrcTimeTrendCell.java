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

public class SrcTimeTrendCell extends AbstractCell<String> {

  // PhraseTemplate to render the source name, time passed & the trend counter
  interface PhraseTemplate extends SafeHtmlTemplates {
    @SafeHtmlTemplates.Template("<span style=\"{0}\">{1}</span>&nbsp;&nbsp;")
    SafeHtml cell(SafeStyles styles, String value);
  }

  // Singleton instances of the template used to render this CustomCell
  private static PhraseTemplate phraseTemplate = GWT.create(PhraseTemplate.class);

  static SafeStyles srcNameStyles;
  static {
    SafeStylesBuilder builder = new SafeStylesBuilder();
    SafeStyles fontFamily = SafeStylesUtils.fromTrustedNameAndValue("font-family", "GhotamBold");
    builder.append(fontFamily);

    int fSize = 12;
    SafeStyles fontSize = new SafeStylesBuilder().fontSize(fSize, Style.Unit.PX).toSafeStyles();
//                            fontSize(fSize, Style.Unit.PX).fontWeight(FontWeight.BOLD).toSafeStyles();
    builder.append(fontSize);
    srcNameStyles = builder.toSafeStyles();
  }

  static SafeStyles timeAndCntStyles;
  static {
    SafeStylesBuilder builder = new SafeStylesBuilder();
    SafeStyles fontFamily = SafeStylesUtils.fromTrustedNameAndValue("font-family", "GhotamLight");
    builder.append(fontFamily);

    int fSize = 12;
    SafeStyles fontSize = new SafeStylesBuilder().fontSize(fSize, Style.Unit.PX).toSafeStyles();
//                            fontSize(fSize, Style.Unit.PX).fontWeight(FontWeight.BOLD).toSafeStyles();
    builder.append(fontSize);
    timeAndCntStyles = builder.toSafeStyles();
  }

  @Override
  public void render(Context context, String value, SafeHtmlBuilder sb) {
    // Segment the value into srcName, timePassed, trendCnt, clusterSize & isClusterHead
    String[] items = value.split(",");
    if (items.length < 5) {
      return;
    }
    // Render the source name with desired style
    // Remove '@' if exists
    if (items[0].charAt(0)=='@') {
      items[0] = items[0].substring(1);
    }
    SafeHtml rendered = phraseTemplate.cell(srcNameStyles, items[0]);
    sb.append(rendered);

    // Render the timePassed, trendCnt & clusterSize
//    int fSize = 12;
//    SafeStyles fontSize = new SafeStylesBuilder().
//                 fontSize(fSize, Style.Unit.PX).fontWeight(FontWeight.NORMAL).toSafeStyles();
    // Take the time and trend counter first
    String timeAndCntAndStatus = items[1] + '(' + items[2] + ')';
    // Take the cluster count if exists
    if (!items[3].equals("0")) {
      if (items[4].equals("true")) {
        timeAndCntAndStatus += ('[' + items[3] + ']');
      } else {
        timeAndCntAndStatus += ('(' + items[3] + ')');
      }
    }
    // Take the topNewsStatus if exists
    if (items.length >= 6) {
      timeAndCntAndStatus += (' ' + items[5]);
    }
    // Finally render it with the String built
    rendered = phraseTemplate.cell(timeAndCntStyles, timeAndCntAndStatus);

//    if (items[3].equals("0"))
//      rendered = phraseTemplate.cell(timeAndCntStyles, items[1] + '(' + items[2] + ')');
//    else if (items[4].equals("true"))
//      rendered = phraseTemplate.cell(timeAndCntStyles,
//                                      items[1] + '(' + items[2] + ')' + '[' + items[3] + ']');
//    else
//      rendered = phraseTemplate.cell(timeAndCntStyles,
//                                      items[1] + '(' + items[2] + ')' + '(' + items[3] + ')');

    // Append the rendered timePassed & counter values
    sb.append(rendered);
  }
}