package UMC.Activities;

import UMC.Activities.Entities.Design_Item;
import UMC.Activities.Entities.Design_Config;
import UMC.Data.Database;
import UMC.Data.JSON;
import UMC.Data.Sql.IObjectEntity;
import UMC.Data.WebResource;
import UMC.Web.*;
import UMC.Data.Utility;

import java.util.Date;

import java.util.UUID;

class DesignCustomActivity extends WebActivity {
    public void processActivity(WebRequest request, WebResponse response) {

        UUID itemId = Utility.uuid(this.asyncDialog("Id", g -> this.dialogValue(UUID.randomUUID().toString())), true);//, true).Value;

        final String[] config = {this.asyncDialog("Config", g -> this.dialogValue("none"))};




        IObjectEntity<Design_Item> itemsEntity = Database.instance().objectEntity(Design_Item.class);
        itemsEntity.where().and().equal(new Design_Item().Id(itemId));


        final String[] size = {this.asyncDialog("Size", g -> this.dialogValue("none"))};

        WebResource webr = UMC.Data.WebResource.Instance();
        WebMeta ts = this.asyncDialog(g ->
        {
            UIFormDialog fm = new UIFormDialog();
            fm.title("界面配置");

            Design_Item item = itemsEntity.single();
            WebMeta meta = new UMC.Web.WebMeta();
            if (item != null) {
                meta = Utility.isNull(UMC.Data.JSON.deserialize(item.Data, WebMeta.class), meta);
                if ("none".equalsIgnoreCase(config[0])) {
                    if (meta.containsKey("Config")) {
                        config[0] = meta.get("Config");
                    }
                }
            }
            if ("none".equalsIgnoreCase(config[0])) {
                this.prompt("您配置错误");
            } else if (config[0].equalsIgnoreCase("uiseo")) {
                fm.title("SEO优化");
                fm.addTextarea("标题", "Title", meta.get("Title"));
                fm.addTextarea("关键词", "Keywords", meta.get("Keywords"));
                fm.addTextarea("描述", "Description", meta.get("Description"));
                fm.submit("确认", request, "Design");
                return fm;
            }
            request.arguments().put("Config", config[0]);

            int keyIndex = config[0].indexOf('.');
            if (keyIndex > -1) {
                config[0] = config[0].substring(keyIndex + 1);
            }
            if (size[0].equals("none")) {
                size[0] = "注意图片尺寸";
            } else {

                size[0] = String.format("图片尺寸:%s", size[0]);
            }
            IObjectEntity<Design_Config> pictureEntity = Database.instance().objectEntity(Design_Config.class);
            pictureEntity.order().asc(new Design_Config().Sequence(0));

            Design_Config[] pices = pictureEntity.where().and().equal(new Design_Config().GroupBy(config[0])).entities().query();


            if (Utility.exists(pices, (dr -> dr.Value == "Image" && dr.Name == "none")) == false) {

                fm.addFile(size[0], "_Image", webr.ImageResolve(itemId, "1", 4))
                        .command("Platform", "Image", new UMC.Web.WebMeta().put("id", itemId).put("seq", "1", "type", "jpg"));
            }
            for (Design_Config dr : pices) {
                if (dr.Value == "Image" && dr.Name == "none") {

                } else {

                    fm.addText(dr.Name, dr.Value, meta.get(dr.Value));
                }
            }
            ;
            if (item == null) {
                item = itemsEntity.where().reset().and().equal(new Design_Item().Design_id(Utility.uuid(config[0], true)))
                        .entities().max(new Design_Item().Seq(0));
                item.Seq = Utility.isNull(item.Seq, 0) + 1;
            }

            fm.addNumber("展示顺序", "Seq", item.Seq);
            fm.submit("确认", request, "Design");

            return fm;
        }, "Setting");
        int seq = UMC.Data.Utility.parse(ts.get("Seq"), 0);
        ts.remove("Seq");
        ts.remove("Image");
        ts.put("Config", config[0]);
        Design_Item ite = new Design_Item()
                .Seq(seq).Type(UIDesigner.StoreDesignTypeCaption)
                .ModifiedDate(new Date()).Data(JSON.serialize(ts)).Id(itemId);


        itemsEntity.iff(e -> e.update(ite) == 0, e ->
        {
            ite.design_id = Utility.uuid(config[0], true);
            e.insert(ite);
        });
        this.context().send("Design", true);
    }
}