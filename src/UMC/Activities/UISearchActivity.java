package UMC.Activities;

import UMC.Data.Database;
import UMC.Data.Entities.SearchKeyword;
import UMC.Data.Sql.IObjectEntity;
import UMC.Data.Utility;
import UMC.Security.Identity;
import UMC.Web.*;
import UMC.Web.UI.UITextItems;

import java.util.*;

public class UISearchActivity extends WebActivity {

    @Override
    public void processActivity(WebRequest request, WebResponse response) {
        Identity user = UMC.Security.Identity.current();
        WebMeta form = Utility.isNull(request.sendValues(), new UMC.Web.WebMeta());

        if (form.containsKey("limit")) {

            UISection ui = UISection.create();


            UITextItems hot = new UMC.Web.UI.UITextItems();
            ui.newSection().put(hot).header().put("text", "热门搜索");

            UITextItems history = new UMC.Web.UI.UITextItems(request.model(), request.cmd());
            history.event("SearchFor");
            ui.newSection().put(history).header().put("text", "历史搜索");

            IObjectEntity<SearchKeyword> entity = Database.instance().objectEntity(SearchKeyword.class)
                    .where()
                    .and().in(new SearchKeyword().User_id(Utility.uuidEmpty))
                    .entities().order().desc(new SearchKeyword().Time(0)).entities();
            entity.query(0, 20, dr -> hot.add(new UIEventText(dr.Keyword).click(new UIClick(dr.Keyword).key("SearchFor"))));


            response.redirect(ui);
        }

        if (Utility.isEmpty(request.sendValue())) {

            List<UIEventText> history = new LinkedList<>();

//            var entity = Data.Database.Instance().ObjectEntity<SearchKeyword>()
//                    .Where
//                    .And().In(new SearchKeyword { user_id = user.Id })
//                                 .Entities.Order.Desc(new SearchKeyword { Time = 0 }).Entities;
//            entity.Query(0, 20, dr => history.Add(new UIEventText(dr.Keyword).Click(new UIClick(dr.Keyword) { Key = "SearchFor" })));
            IObjectEntity<SearchKeyword> entity = Database.instance().objectEntity(SearchKeyword.class)
                    .where()
                    .and().in(new SearchKeyword().User_id(Utility.uuidEmpty))
                    .entities().order().desc(new SearchKeyword().Time(0)).entities();
            entity.query(0, 20, dr -> history.add(new UIEventText(dr.Keyword).click(new UIClick(dr.Keyword).key("SearchFor"))));
            Map hash = new LinkedHashMap();

            // var hash = new System.Collections.Hashtable();
            hash.put("data", history);
           // hash["data"] = history;
            if (history.size() == 0) {
                hash.put("msg", "请搜索");
            }
            response.redirect(hash);
        } else {
            String[] vs = request.sendValue().split(",|\\s");
//            var vs = request.sendValue().split(',', ' ', '　');
//            var entity = Data.Database.Instance().ObjectEntity < SearchKeyword > ()
//                    .Order.Desc(new SearchKeyword {
//                Time = 0
//            }).Entities;
//            var list = new List<SearchKeyword>();

            IObjectEntity<SearchKeyword> entity = Database.instance().objectEntity(SearchKeyword.class)
                    .where().and().in(new SearchKeyword().User_id(user.id())).entities().order().desc(new SearchKeyword().Time(0))

                    .entities();


            for(String i : vs)
            {
                if (Utility.isEmpty(i) == false) {
                    SearchKeyword search = new SearchKeyword().Keyword(i).User_id(user.id()).Time(UMC.Data.Utility.timeSpan());

                    entity.where().reset().and().equal(new SearchKeyword().Keyword(i).User_id(user.id()));

                    if (entity.update(new SearchKeyword().Time(UMC.Data.Utility.timeSpan())) == 0) {
                        entity.insert(search);
                    }
                    entity.where().reset().and().equal(new SearchKeyword()
                            .Keyword(i).User_id(Utility.uuidEmpty));
                    if (entity.update("{0}+{1}", new SearchKeyword().Time(1)) == 0) {
                        search.Time = 1;
                        search.user_id = Utility.uuidEmpty;
                        entity.insert(search);
                    }
                }
            }

            List<UIEventText> history = new LinkedList<>();

            entity.where().reset().and().equal(new SearchKeyword().User_id(user.id()));

            entity.query(0, 20, dr -> history.add (new UIEventText(dr.Keyword).click(new UIClick(dr.Keyword).key("SearchFor")
                 )));
            response.redirect(new WebMeta().put("data",history));

        }
        //var data= new System.Data.datat
        //.Query(0, 100, dr => products.Add(dr));

    }

//    @Override
//    public void processActivity(WebRequest request, WebResponse response) {
//
//        Identity user = UMC.Security.Identity.current();
//        if (Utility.isEmpty(request.sendValue())) {
//
//            List<String> hot = new LinkedList<>();
//
//            IObjectEntity<SearchKeyword> entity = Database.instance().objectEntity(SearchKeyword.class)
//                    .where().and().in(new SearchKeyword().User_id(user.id())).entities().order().desc(new SearchKeyword().Time(0))
//
//                    .entities();
//            entity.query(0, 20, dr -> hot.add(dr.Keyword));
//
//            List<String> history = new LinkedList();
//            entity.where().reset().and().equal(new SearchKeyword().User_id(user.id()));
//            entity.query(0, 20, dr -> history.add(dr.Keyword));
//
//            response.redirect(new WebMeta().put("hot", hot).put("history", history));
//        } else {
//            String[] vs = request.sendValue().split(",|\\s");
//
//            IObjectEntity<SearchKeyword> entity = Database.instance().objectEntity(SearchKeyword.class)
//                    .where().and().in(new SearchKeyword().User_id(user.id())).entities().order().desc(new SearchKeyword().Time(0))
//
//                    .entities();
//
////            List<String> list = new LinkedList<>();
//
//            for (String i : vs) {
//                if (Utility.isEmpty(i) == false) {
//                    SearchKeyword search = new SearchKeyword().Keyword(i).User_id(user.id()).Time(UMC.Data.Utility.timeSpan());
//
//                    entity.where().reset().and().equal(new SearchKeyword().Keyword(i).User_id(user.id()));
//
//                    if (entity.update(new SearchKeyword().Time(UMC.Data.Utility.timeSpan())) == 0) {
//                        entity.insert(search);
//                    }
//                    entity.where().reset().and().equal(new SearchKeyword()
//                            .Keyword(i).User_id(Utility.uuidEmpty));
//                    if (entity.update("{0}+{1}", new SearchKeyword().Time(1)) == 0) {
//                        search.Time = 1;
//                        search.user_id = Utility.uuidEmpty;
//                        entity.insert(search);
//                    }
//                }
//            }
//
//            List<String> history = new LinkedList();
//
//            entity.where().reset().and().equal(new SearchKeyword().User_id(user.id()));
//            entity.query(0, 20, dr -> history.add(dr.Keyword));
//
//
//            response.redirect(new WebMeta().put("history", history));
//
//        }
//    }
}
