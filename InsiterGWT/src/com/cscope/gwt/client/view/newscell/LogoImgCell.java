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

public class LogoImgCell extends AbstractCell<String> {
  // Image templates used to render the logo image of news source
  interface LogoImgTemplate extends SafeHtmlTemplates {
    @SafeHtmlTemplates.
      Template("<div><img src=\"data:image/png;base64,{0}\"/></div>")
    //Template("<div style=\"padding-left:2px\"><img src=\"data:image/jpeg;base64,{0}\"/></div>")
    SafeHtml img(String base64img);
  }

  // Singleton instances of the templates used to render this CustomCell
  private static LogoImgTemplate logoImgTemplate = GWT.create(LogoImgTemplate.class);

  @Override
  public void render(Context context, String value, SafeHtmlBuilder sb) {
    // Initially assume that no index
    String logoImg = value;

    // Return if its logo image is null or empty
    if (logoImg == null || logoImg.isEmpty()) {
      return;
    }
    // Render the logo
    SafeHtml rendered = null;
    rendered = logoImgTemplate.img(logoImg);
    if (rendered != null) {
      sb.append(rendered);
    }
  }
}