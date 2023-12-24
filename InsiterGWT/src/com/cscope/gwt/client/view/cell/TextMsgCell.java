package com.cscope.gwt.client.view.cell;

import java.util.logging.Level;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safecss.shared.SafeStyles;
import com.google.gwt.safecss.shared.SafeStylesBuilder;
import com.google.gwt.safecss.shared.SafeStylesUtils;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

public class TextMsgCell extends AbstractCell<String> {

  // MsgTemplate to render the Tweet Msg
  interface MsgTemplate extends SafeHtmlTemplates {
    @SafeHtmlTemplates.Template("<div style=\"{0}\">{1}</div>")
    SafeHtml cell(SafeStyles styles, SafeHtml value);
  }
  // Create a singleton instance of the templates used to render the cell.
  private static MsgTemplate msgTemplate = GWT.create(MsgTemplate.class);

  @Override
  public void render(Context context, String value, SafeHtmlBuilder sb) {
//    // Split the value into msg & timePassed
//    int index = value.indexOf(',');
//    if (index <= 0 || (index+1)==value.length()) {
//      return;
//    }
//    String msg = value.substring(0, index);
//    String timePassed = value.substring(index+1);
//    /*
//     * Make sure neither is not null nor empty
//     */
//    if (msg == null || msg.isEmpty() || timePassed == null || timePassed.isEmpty()) {
//      return;
//    }
//    // If the value comes from the user, we escape it to avoid XSS attacks.
//    SafeHtml safeText = SafeHtmlUtils.fromString(msg);
//    // If the value comes from the user, we escape it to avoid XSS attacks.
    SafeHtml safeText = SafeHtmlUtils.fromString(value);

    // Build Styles for the time & counter strings
    SafeStylesBuilder builder = new SafeStylesBuilder();
//    SafeStyles fontFamily =
//      SafeStylesUtils.fromTrustedNameAndValue("font-family", "'Sans Open',Segoe,Frutiger,Proxima-nova,Helvetica");
//    builder.append(fontFamily);
    
    SafeStyles fontSize = SafeStylesUtils.fromTrustedNameAndValue("font-size", "14px");
    builder.append(fontSize);
    SafeStyles fontWeight = SafeStylesUtils.fromTrustedNameAndValue("font-weight", "normal");
    //SafeStyles fontWeight = SafeStylesUtils.fromTrustedNameAndValue("font-weight", "lighter");
    //SafeStyles fontWeight = SafeStylesUtils.fromTrustedNameAndValue("font-weight", "300");
    builder.append(fontWeight);

    // Render the Tweet msg
    SafeHtml rendered = msgTemplate.cell(builder.toSafeStyles(), safeText);
    sb.append(rendered);
  }
}