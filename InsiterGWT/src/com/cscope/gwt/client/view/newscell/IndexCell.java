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

public class IndexCell extends AbstractCell<String> {
  // Index template used to render the index
  interface IndexTemplate extends SafeHtmlTemplates {
    @SafeHtmlTemplates.Template("<span style=\"{0}\">{1}</span>")
    SafeHtml cell(SafeStyles styles, String value);
  }

  // Singleton instances of the templates used to render this CustomCell
  private static IndexTemplate indexTemplate = GWT.create(IndexTemplate.class);

  @Override
  public void render(Context context, String index, SafeHtmlBuilder sb) {
    // Render the index with the desired style  .paddingLeft(5, Style.Unit.PX)
    SafeStylesBuilder builder = new SafeStylesBuilder().fontSize(10, Style.Unit.PX);
    SafeStyles idxPosition = SafeStylesUtils.forFloat(Style.Float.RIGHT);
    builder.append(idxPosition);
    SafeHtml rendered = indexTemplate.cell(builder.toSafeStyles(), index);
    sb.append(rendered);
  }
}