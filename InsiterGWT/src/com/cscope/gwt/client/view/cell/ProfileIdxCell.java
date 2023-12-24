package com.cscope.gwt.client.view.cell;

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

public class ProfileIdxCell extends AbstractCell<String> {
  // Profile template used to render the profile photo
  interface ProfileTemplate extends SafeHtmlTemplates {
    @SafeHtmlTemplates.Template("<div style=\"padding-left:5px\"><img src=\"{0}\"/></div>")
    SafeHtml img(String url);
  }

  // Index template used to render the index
  interface IndexTemplate extends SafeHtmlTemplates {
    @SafeHtmlTemplates.Template("<div style=\"{0}\">{1}</div>")
    SafeHtml cell(SafeStyles styles, String value);
  }

  // Singleton instances of the templates used to render this CustomCell
  private static ProfileTemplate profileTemplate = GWT.create(ProfileTemplate.class);
  private static IndexTemplate indexTemplate = GWT.create(IndexTemplate.class);

  @Override
  public void render(Context context, String value, SafeHtmlBuilder sb) {
    int i = value.indexOf(',');
    if (i <= 0) {
      return;
    }
    String profile = value.substring(0, i);
    // Make sure profile is not null nor empty
    if (profile == null || profile.isEmpty()) {
      return;
    }
    String index = value.substring(i+1);

    // Render the Profile photo first - it's fixed square of 48px by 48px
    SafeHtml rendered = profileTemplate.img(profile);
    sb.append(rendered);

    // Render the index with the desired style  .paddingLeft(5, Style.Unit.PX)
    SafeStylesBuilder builder = new SafeStylesBuilder().fontSize(10, Style.Unit.PX);
    SafeStyles idxPosition = SafeStylesUtils.forFloat(Style.Float.RIGHT);
    builder.append(idxPosition);
    rendered = indexTemplate.cell(builder.toSafeStyles(), index);
    sb.append(rendered);
  }
}