package UMC.Activities;


import UMC.Activities.Entities.Design_Item;
import UMC.Data.Database;
import UMC.Data.JSON;
import UMC.Data.WebResource;
import UMC.Data.Sql.IObjectEntity;
import UMC.Web.*;
import UMC.Data.Utility;

import javax.xml.crypto.Data;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class DesignItemsActivity extends WebActivity {
    private boolean _IsDesign;

    public static boolean IsDesign(WebRequest request) {
        if (request.isCashier()) {
            return "true".equalsIgnoreCase(UMC.Security.AccessToken.get("UIDesign"));//, "true");

        }
        return false;
    }

    public void processActivity(WebRequest request, WebResponse response) {

        _IsDesign = IsDesign(request);

        List<UUID> ids = new LinkedList<>();
        List<String> strIds = new LinkedList<>();

        String strs = this.asyncDialog("Id", g -> this.dialogValue("none"));//, true).Value;
        if (strs.indexOf(',') > -1) {
            for (String s : strs.split(",")) {
                if (Utility.isEmpty(s) == false) {
                    ids.add(Utility.uuid(s, true));
                    strIds.add(s);
                }
            }
        } else {
            ids.add(Utility.uuid(strs, true));
        }

        List<UUID> pids = new LinkedList<>();

        List<Design_Item> items = new LinkedList<>();

        IObjectEntity<Design_Item> itemsEntity = Database.instance().objectEntity(Design_Item.class);
        if (ids.size() == 1) {
            itemsEntity.where().and().equal(new Design_Item().Design_id(ids.get(0)));

        } else {

            itemsEntity.where().and().in(new Design_Item().Design_id(ids.get(0)), ids.toArray())
                    .and().equal(new Design_Item().Type(UIDesigner.StoreDesignTypeCustom));

        }
        itemsEntity.order().asc(new Design_Item().Seq(0)).entities()
                .query(dr -> items.add(dr));


        List<WebMeta> lis = new LinkedList<>();

        WebResource webr = UMC.Data.WebResource.Instance();
        if (strIds.size() > 0) {
            String config = this.asyncDialog("Config", g -> this.dialogValue("none"));

            for (int i = 0; i < strIds.size(); i++)// var b in items)
            {
                UUID cid = ids.get(i);
                Design_Item item = Utility.find(items, g -> g.Id.compareTo(cid) == 0);

                if (item != null) {
                    WebMeta pms = JSON.deserialize(item.Data, WebMeta.class);
                    pms.put("id", strIds.get(i));
                    if (_IsDesign) {
                        pms.put("design", true);
                        if (config.equals("UISEO")) {
                            pms.put("click", new UIClick(new UMC.Web.WebMeta().put("Id", item.Id).put("Config", config))
                                    .send("Design", "Custom"));
                        } else {
                            pms.put("click", UIDesigner.Click(item, true));
                        }


                    } else {
                        pms.put("click", UIDesigner.Click(item, false));
                    }
                    pms.put("src", webr.ImageResolve(item.Id, "1", 0) + "?" + UIDesigner.TimeSpan(item.ModifiedDate));

                    lis.add(pms);
                } else {
                    if (_IsDesign) {
                        lis.add(new UMC.Web.WebMeta().put("design", true).put("id", strIds.get(i)).put("click", new UIClick(new UMC.Web.WebMeta().put("Id", Utility.uuid(strIds.get(i), true).toString(), "Config", config))
                                .send("Design", "Custom")));


                    }
                }
            }
        } else {

            items.removeAll(Utility.findAll(items, g ->
            {
                switch (g.Type) {
                    case UIDesigner.StoreDesignTypeCustom:
                    case UIDesigner.StoreDesignTypeItem:
                        return false;
                }
                return true;
            }));
            for (Design_Item b : items) {
                WebMeta pms = JSON.deserialize(b.Data, WebMeta.class);
                pms.put("id", b.Id);
                pms.put("click", UIDesigner.Click(b, _IsDesign));
                if (_IsDesign) {
                    pms.put("design", true);
                }
                pms.put("src", webr.ImageResolve(b.Id, "1", 0) +  UIDesigner. TimeSpan(b.ModifiedDate));
                lis.add(pms);
            }
            if (items.size() == 0) {
                if (_IsDesign) {
                    String config = this.asyncDialog("Config", g -> this.dialogValue(strs));
                    lis.add(new UMC.Web.WebMeta().put("design", true).put("click", new UIClick(new UMC.Web.WebMeta().put("Config", config))
                            .send("Design", "Custom")));

                }
            }
        }
        response.redirect(lis);
    }

//    int TimeSpan(Date date) {
//        return date != null ? (int) (date.getTime() / 1000) : 0;// date.HasValue ? UMC.Data.Utility.TimeSpan(date.Value) : 0;
//    }
}