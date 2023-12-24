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

public class NewsPhotoCell extends AbstractCell<String> {
  // Image templates used to render the news photo OR profile photo
  interface NewsPhotoTemplate extends SafeHtmlTemplates {
    @SafeHtmlTemplates.
      Template("<div><img src=\"data:image/jpeg;base64,{0}\"/></div>")
    //Template("<div style=\"padding-left:2px\"><img src=\"data:image/jpeg;base64,{0}\"/></div>")
    SafeHtml img(String base64img);
//    @SafeHtmlTemplates.Template("<div style=\"padding-left:5px\"><img src=\"{0}\"/></div>")
//    SafeHtml img(String url);
  }

  interface ProfileTemplate extends SafeHtmlTemplates {
    @SafeHtmlTemplates.Template("<div style=\"padding-left:5px\"><img src=\"{0}\"/></div>")
    SafeHtml img(String url);
  }

//  // Index template used to render the index
//  interface IndexTemplate extends SafeHtmlTemplates {
//    @SafeHtmlTemplates.Template("<div style=\"{0}\">{1}</div>")
//    SafeHtml cell(SafeStyles styles, String value);
//  }

  // Singleton instances of the templates used to render this CustomCell
  private static NewsPhotoTemplate photoTemplate = GWT.create(NewsPhotoTemplate.class);
  private static ProfileTemplate profileTemplate = GWT.create(ProfileTemplate.class);
//  private static IndexTemplate indexTemplate = GWT.create(IndexTemplate.class);

  @Override
  public void render(Context context, String value, SafeHtmlBuilder sb) {
    // Initially assume that no index
    String photo = value;
//    // The index is concatenated with ',' mark, which is not in the base64 char set
//    int i = value.indexOf(',');
//    String idxStr = "";
//    if (i > 0) {
//      photo = value.substring(0, i);
//      idxStr = value.substring(i+1);
//    }
    // Make sure photo is not null nor empty
    if (photo == null || photo.isEmpty()) {
      return;
    }
    // Render the Photo
    SafeHtml rendered = null;
    if (value.length() > 256) {
      rendered = photoTemplate.img(photo);
    } else {
      rendered = profileTemplate.img(photo);
    }
    if (rendered != null) {
      sb.append(rendered);
    }

//    if (idxStr.isEmpty()) {
//      return;
//    }
//
//    // Render the index with the desired style  .paddingLeft(5, Style.Unit.PX)
//    SafeStylesBuilder builder = new SafeStylesBuilder().fontSize(10, Style.Unit.PX);
//    SafeStyles idxPosition = SafeStylesUtils.forFloat(Style.Float.RIGHT);
//    builder.append(idxPosition);
//    rendered = indexTemplate.cell(builder.toSafeStyles(), idxStr);
//    sb.append(rendered);
  }
}