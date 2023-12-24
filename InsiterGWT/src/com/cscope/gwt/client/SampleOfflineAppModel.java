package com.cscope.gwt.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;

import insiter.ina.news.build.NewsClass;
import insiter.ina.news.build.NewsClass.Entity;
import insiter.ina.news.build.NewsClass.Subject;
import insiter.ina.news.build.NewsClass.LowerMenu;
import insiter.ina.news.build.NewsClass.UpperMenu;

import com.cscope.gwt.client.InsiterRPCAsync;
import com.cscope.gwt.client.models.AppModel;
//import com.cscope.gwt.shared.NewsMenu;
import com.cscope.gwt.shared.NewsList2Show;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

import insiter.ina.news.service.web.NewsMenu;

public class SampleOfflineAppModel implements AppModel {

	@Override
	public void getMenuItems(final MenuItemsCallback callback) {

		Scheduler.get().scheduleDeferred(new ScheduledCommand() {
			@Override
			public void execute() {

				NewsMenu tm = new NewsMenu();
				ArrayList<Entity> ale = new ArrayList<>(EnumSet.allOf(Entity.class));
				//tm.setUpperMenu( ale );

				ArrayList<LowerMenu> als = new ArrayList<LowerMenu>(EnumSet.allOf(Subject.class));
				//tm.setBottomAllMenu(als);

				//Map<Entity, List<Subject>> ee = new HashMap<Entity, List<Subject>>();

				//tm.setBotMenu((UpperMenu)Entity.valueOf("All"),				
				//		(Arrays.asList(
				//				(LowerMenu)Subject.valueOf("All")))
//								Subject.valueOf("newsMda"),
//								Subject.valueOf("legal"),
//								Subject.valueOf("space"),
//								Subject.valueOf("game"),
//								Subject.valueOf("moviTV"),
//								Subject.valueOf("invent"),
//								Subject.valueOf("finanStus"),
//								Subject.valueOf("eCommerc"),
//								Subject.valueOf("engering"),
//								Subject.valueOf("mobile"),
//								Subject.valueOf("health"),
//								Subject.valueOf("securty"),
//								Subject.valueOf("wearble"),
//								Subject.valueOf("compHW"),
//								Subject.valueOf("webSrvc"),
//								Subject.valueOf("artiIntl"),
//								Subject.valueOf("art"),
//								Subject.valueOf("aviaton"),
//								Subject.valueOf("cnsmElec"),
//								Subject.valueOf("inetMda"),
//								Subject.valueOf("brdCast")) 
						//);

//				tm.setSubjMenu(Entity.valueOf("Company"),				
//						Arrays.asList(
//								Subject.valueOf("All"),
//								Subject.valueOf("newsMda"),
//								Subject.valueOf("compHW"),
//								Subject.valueOf("eCommerc"),
//								Subject.valueOf("strtup"),
//								Subject.valueOf("soclMda"),
//								Subject.valueOf("fundng"),Subject.valueOf("prdStus"),Subject.valueOf("commSrvc"),Subject.valueOf("cnsmElec"),Subject.valueOf("autoMbl"),Subject.valueOf("freeMkt"),Subject.valueOf("semiCon"),Subject.valueOf("mobile"),Subject.valueOf("inetMda"),Subject.valueOf("inetCom"),Subject.valueOf("MandA"),Subject.valueOf("game"),Subject.valueOf("wearble"),Subject.valueOf("phtoShr"),Subject.valueOf("compSW"),Subject.valueOf("aviaton"),Subject.valueOf("compete"),Subject.valueOf("paySystm"),Subject.valueOf("computr"),Subject.valueOf("moviTV"),Subject.valueOf("engering"),Subject.valueOf("legal"),Subject.valueOf("blogMda"),Subject.valueOf("srchEng"),Subject.valueOf("storge"),Subject.valueOf("finanStus"),Subject.valueOf("brdCast"))
//						);
//
//
//				tm.setSubjMenu(
//						Entity.valueOf("SciTech"),
//						Arrays.asList(
//								Subject.valueOf("All"),
//								Subject.valueOf("securty"),
//								Subject.valueOf("newsMda"),
//								Subject.valueOf("engering"),
//								Subject.valueOf("environ"),
//								Subject.valueOf("energy"),
//								Subject.valueOf("clnEngy"),
//								Subject.valueOf("bioTech"),
//								Subject.valueOf("moviTV"),
//								Subject.valueOf("artiIntl"),
//								Subject.valueOf("virCrncy"),
//								Subject.valueOf("computr")));
//
//				tm.setSubjMenu(
//						Entity.valueOf("People"),
//						Arrays.asList(
//								Subject.valueOf("All"),
//								Subject.valueOf("securty"),
//								Subject.valueOf("newsMda"),
//								Subject.valueOf("fundng"),
//								Subject.valueOf("legal"),
//								Subject.valueOf("eCommerc"),
//								Subject.valueOf("mobile"),
//								Subject.valueOf("compSW"),
//								Subject.valueOf("engering")));
//
//				tm.setSubjMenu(
//						Entity.valueOf("Gov"),
//						Arrays.asList(
//								Subject.valueOf("All"),
//								Subject.valueOf("newsMda"),
//								Subject.valueOf("space"),
//								Subject.valueOf("reglate")));
//
//				tm.setSubjMenu(
//						Entity.valueOf("Event"),
//						Arrays.asList(
//								Subject.valueOf("All"),
//								Subject.valueOf("confernc"),
//								Subject.valueOf("newsMda"),
//								Subject.valueOf("festval"),
//								Subject.valueOf("inetMda"),
//								Subject.valueOf("compSW")));
//				
//				tm.setSubjMenu(
//						Entity.valueOf("Org"),
//						Arrays.asList(
//								Subject.valueOf("All"),
//								Subject.valueOf("finanStus")));
//
//				tm.setSubjMenu(
//						Entity.valueOf("Tech"),
//						Arrays.asList(
//								Subject.valueOf("All"),
//								Subject.valueOf("securty")));

				callback.onMenuItemsReceived(tm);

			}
		});
	}

	@Override
	public void getNewsList(NewsClass newsClass, AsyncCallback<NewsList2Show> callback) {
		Window.alert("NOT IMPLEMENTED: getNewsList ");
	}

	@Override
	public InsiterRPCAsync getRPCService() {
		Window.alert("NOT IMPLEMENTED: getRPCService ");
		return null;
	}

}
