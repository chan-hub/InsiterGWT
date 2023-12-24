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

public class NameAndLogoCell extends AbstractCell<String> {

  // NameTemplate to render publisher's name & screen name
  interface NameTemplate extends SafeHtmlTemplates {
    @SafeHtmlTemplates.Template("<span style=\"{0}\">{1}</span>")
    SafeHtml cell(SafeStyles styles, String value);
  }

  // Logo template used to render the Twitter logo
  interface LogoTemplate extends SafeHtmlTemplates {
    @SafeHtmlTemplates.Template("<img src=\"{0}\" style=\"{1}\"/>")
    SafeHtml img(String image, SafeStyles style);
  }

  // Singleton instances of the templates used to render this CustomCell
  private static NameTemplate nameTemplate = GWT.create(NameTemplate.class);
  private static LogoTemplate logoTemplate = GWT.create(LogoTemplate.class);

  private static String logoJpg = "images/TwitterLogo.png";

  // static logo renderer to avoid repeated rendering
  static SafeHtml renderedLogo;
  static {
    // Render the logo with scaling & positioning styles
    SafeStylesBuilder builder = new SafeStylesBuilder();
    // Style for the Scaling
    builder.append(SafeStylesUtils.fromTrustedNameAndValue("width", "auto"));
    builder.append(SafeStylesUtils.fromTrustedNameAndValue("height", "auto"));
    builder.append(SafeStylesUtils.fromTrustedNameAndValue("max-width", "20px"));
    builder.append(SafeStylesUtils.fromTrustedNameAndValue("max-height", "20px"));

    // Style for Positioning
    SafeStyles imagePosition = SafeStylesUtils.forFloat(Style.Float.RIGHT);
    builder.append(imagePosition);
    renderedLogo = logoTemplate.img(logoJpg, builder.toSafeStyles());
  }

  @Override
  public void render(Context context, String value, SafeHtmlBuilder sb) {
    // Split the value into name & screen name
    int index = value.indexOf(',');
    if (index <= 0 || (index+1)==value.length()) {
      return;
    }
    String name = value.substring(0, index);
    String sNameTime = value.substring(index+1);
    index = sNameTime.indexOf(',');
    if (index <= 0 || (index+1)==sNameTime.length()) {
      return;
    }
    String sName = sNameTime.substring(0, index);
    String time = sNameTime.substring(index+1);

    // Make sure neither is null nor empty
    if (name == null || name.isEmpty() ||
        sName == null || sName.isEmpty() || time == null || time.isEmpty()) {
      return;
    }

    // Render the publisher's name with desired style
    //int fSize = 12;
    int fSize = 14;
    SafeStyles styles = new SafeStylesBuilder().
                            fontSize(fSize, Style.Unit.PX).fontWeight(FontWeight.BOLD).toSafeStyles();
    SafeHtml rendered = nameTemplate.cell(styles, name);
    sb.append(rendered);

    // Render the publisher's screen name with desired style
    styles = new SafeStylesBuilder().
                   paddingLeft(10, Style.Unit.PX).fontSize(fSize, Style.Unit.PX).toSafeStyles();
    rendered = nameTemplate.cell(styles, sName);
    sb.append(rendered);

    // Render the time with desired style
    fSize = 12;
    styles = new SafeStylesBuilder().
                   paddingLeft(10, Style.Unit.PX).fontSize(fSize, Style.Unit.PX).toSafeStyles();
    rendered = nameTemplate.cell(styles, time);
    sb.append(rendered);
    
    // Render the logo with scaling & positioning styles
//    SafeStylesBuilder builder = new SafeStylesBuilder();
//    // Style for the Scaling
//    builder.append(SafeStylesUtils.fromTrustedNameAndValue("width", "auto"));
//    builder.append(SafeStylesUtils.fromTrustedNameAndValue("height", "auto"));
//    builder.append(SafeStylesUtils.fromTrustedNameAndValue("max-width", "20px"));
//    builder.append(SafeStylesUtils.fromTrustedNameAndValue("max-height", "20px"));
//
//    // Style for Positioning
//    SafeStyles imagePosition = SafeStylesUtils.forFloat(Style.Float.RIGHT);
//    builder.append(imagePosition);
//        
//    rendered = logoTemplate.img(logoJpg, builder.toSafeStyles());
//    sb.append(rendered);
//    
    sb.append(renderedLogo);
  }
}