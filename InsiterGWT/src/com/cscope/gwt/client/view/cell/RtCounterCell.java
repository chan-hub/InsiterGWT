package com.cscope.gwt.client.view.cell;

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

public class RtCounterCell extends AbstractCell<String> {
  // Template for the time string
  interface TimeTemplate extends SafeHtmlTemplates {
    @SafeHtmlTemplates.Template("<span style=\"{0}\">{1}</span>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;")
    SafeHtml cell(SafeStyles styles, SafeHtml value);
  }
  // Singleton instance of the template used to render the time string
  private static TimeTemplate timeTemplate = GWT.create(TimeTemplate.class);

  // Template for RT counters
  interface CounterTemplate extends SafeHtmlTemplates {
    @SafeHtmlTemplates.Template("<span style=\"{0}\">{1}</span>&nbsp;&nbsp;")
    SafeHtml cell(SafeStyles styles, String value);
  }
  // Singleton instance of the templates used to render the RT counts
  private static CounterTemplate counterTemplate = GWT.create(CounterTemplate.class);

  @Override
  public void render(Context context, String counter, SafeHtmlBuilder sb) {

    // Make sure neither is not null nor empty
    if (counter == null || counter.isEmpty()) {
      return;
    }
    // If the value comes from the user, we escape it to avoid XSS attacks
    //SafeHtml safeText = SafeHtmlUtils.fromString(time);

    // Build Styles for the time & counter strings
    SafeStylesBuilder builder = new SafeStylesBuilder();
    SafeStyles fontFamily =
      SafeStylesUtils.fromTrustedNameAndValue("font-family", "'Sans Open',Segoe,Frutiger,Proxima-nova,Helvetica");
    builder.append(fontFamily);
    SafeStyles fontSize = SafeStylesUtils.fromTrustedNameAndValue("font-size", "10px");
    builder.append(fontSize);

    // Render counter with the desired style
    SafeHtml rendered = counterTemplate.cell(builder.toSafeStyles(), counter);
    sb.append(rendered);
  }
}
