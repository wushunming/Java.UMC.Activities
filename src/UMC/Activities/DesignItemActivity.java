package UMC.Activities;


import UMC.Activities.Entities.Design_Item;
import UMC.Data.Database;
import UMC.Data.Sql.IObjectEntity;
import UMC.Data.WebResource;
import UMC.Security.Identity;
import UMC.Web.*;
import UMC.Data.Utility;

import java.util.Date;
import java.util.UUID;


class DesignItemActivity extends WebActivity {
    void seq(WebRequest request, WebResponse response, Design_Item item) {
        IObjectEntity<Design_Item> entity = Database.instance().objectEntity(Design_Item.class);
        entity.where().and().equal(new Design_Item().Id(item.Id));//.And().Equal(new Design_Item { Id = item.Id });

        WebMeta meta = this.asyncDialog(g ->
        {

            UIFormDialog from = new UIFormDialog();
            from.title("调整顺序");

            from.addNumber("展示顺序", "Seq", item.Seq);
            return from;
        }, "Setting");


        entity.update(new Design_Item().ModifiedDate(new Date()).Seq(Utility.parse(meta.get("Seq"), 0)));


    }

    void caption(WebRequest request, WebResponse response, UUID sid, UUID forid) {
        String Name = this.asyncDialog("Name", g -> new UITextDialog().title("新建栏位"));
        Design_Item item2 = new Design_Item().Id(UUID.randomUUID()).Design_id(sid).For_id(forid)
                .ItemName(Name).ModifiedDate(new Date()).Type(UIDesigner.StoreDesignTypeCaption);
        IObjectEntity<Design_Item> entity = Database.instance().objectEntity(Design_Item.class);

        Design_Item max = entity
                .where().and().equal(new Design_Item().Design_id(sid))
                .entities().max(new Design_Item().Seq(0));//.Seq+1;
        item2.Seq = Utility.isNull(max.Seq, 0) + 1;

        entity.insert(item2);
    }


    void icons(WebRequest request, UUID itemId) {

        IObjectEntity<Design_Item> entity = Database.instance().objectEntity(Design_Item.class);


        entity.where().and().equal(new Design_Item().Id(itemId));
        Design_Item item = entity.single();

        WebResource webr = UMC.Data.WebResource.Instance();

        WebMeta meta = this.asyncDialog(g ->
        {
            Design_Item finalItem = item;
            switch (Utility.isNull(item.Type, 0)) {
                case UIDesigner.StoreDesignTypeItem:
                    break;
                case UIDesigner.StoreDesignTypeIcons:


                    Design_Item item2 = new Design_Item().Id(UUID.randomUUID()).Design_id(item.design_id).For_id(item.Id)
                            .ModifiedDate(new Date()).Type(UIDesigner.StoreDesignTypeItem);


                    Design_Item max = entity
                            .where().reset().and().equal(new Design_Item().Design_id(item.design_id).For_id(item.Id))
                            .entities().max(new Design_Item().Seq(0));//.Seq+1;
                    item2.Seq = Utility.isNull(max.Seq, 0) + 1;

                    entity.insert(item2);


                    finalItem = item2;
                    request.arguments().put("Id", finalItem.Id);
                    break;
                default:
                    this.prompt("类型错误");
                    break;
            }
            UIFormDialog from = new UIFormDialog();
            from.title("图标");

            from.addFile("图片", "_Image", webr.ImageResolve(finalItem.Id, "1", 4))
                    .command("Design", "Image", new UMC.Web.WebMeta().put("id", finalItem.Id).put("seq", "1"));
            from.addText("标题", "ItemName", finalItem.ItemName);

            from.addNumber("顺序", "Seq", finalItem.Seq);

            from.submit("确认", request, "Design");
            return from;
        }, "Setting");


        entity.where().reset().and().equal(new Design_Item().Id(item.Id));
        entity.update(new Design_Item().ItemName(meta.get("ItemName")).ModifiedDate(new Date()).Seq(Utility.parse(meta.get("Seq"), 0)));


    }

    void Items(WebRequest request, UUID itemId) {
        UMC.Data.WebResource webr = UMC.Data.WebResource.Instance();

        IObjectEntity<Design_Item> entity = Database.instance().objectEntity(Design_Item.class);

        entity.where().and().equal(new Design_Item().Id(itemId));
        Design_Item item = entity.single();


        WebMeta meta = this.asyncDialog(g ->
        {
            Design_Item finalItem = item;
            switch (item.Type) {
                case UIDesigner.StoreDesignTypeItem:
                    break;
                case UIDesigner.StoreDesignTypeItems:
                    int count = entity.where().reset()
                            .and().equal(new Design_Item().For_id(itemId))
                            .entities().count();
                    if (count > 3) {
                        this.prompt("分列栏，只能添加4列");
                    }


                    Design_Item item2 = new Design_Item().Id(UUID.randomUUID()).Design_id(item.design_id).For_id(item.Id)
                            .ModifiedDate(new Date()).Type(UIDesigner.StoreDesignTypeItem);


                    Design_Item max = entity
                            .where().reset().and().equal(new Design_Item().Design_id(item.design_id).For_id(item.Id))
                            .entities().max(new Design_Item().Seq(0));//.Seq+1;
                    item2.Seq = Utility.isNull(max.Seq, 0) + 1;

                    entity.insert(item2);


                    finalItem = item2;
                    request.arguments().put("Id", item2.Id);
                    break;
                default:
                    this.prompt("类型错误");
                    break;
            }
            WebMeta data = Utility.isNull(UMC.Data.JSON.deserialize(finalItem.Data, WebMeta.class), new UMC.Web.WebMeta());

            UIFormDialog from = new UIFormDialog();
            from.title("图标");
            from.addFile("图片", "_Image", webr.ImageResolve(finalItem.Id, "1", 4))
                    .command("Design", "Image", new UMC.Web.WebMeta().put("id", finalItem.Id).put("seq", "1"));
            from.addText("标题", "title", finalItem.ItemName);
            from.addText("描述", "desc", finalItem.ItemDesc);
            from.add("Color", "startColor", "标题开始色", data.get("startColor"));
            from.add("Color", "endColor", "标题结束色", data.get("endColor"));
            from.addNumber("顺序", "Seq", finalItem.Seq);
            // from.submit("确认", request.model(), request.cmd(), new UMC.Web.WebMeta().put("Id", finalItem.Id).put("Type", "Edit"));

            from.submit("确认", request, "Design");
            return from;
        }, "Setting");

        entity.where().reset().and().equal(new Design_Item().Id(item.Id));
        entity.update(new Design_Item().ItemName(meta.get("title"))
                .ItemDesc(meta.get("desc"))
                .Data(UMC.Data.JSON.serialize(meta))
                .ModifiedDate(new Date()).Seq(Utility.parse(meta.get("Seq"), 0)));


    }


    void titleDesc(WebRequest request, UUID itemId) {
        UMC.Data.WebResource webr = UMC.Data.WebResource.Instance();

        IObjectEntity<Design_Item> entity = Database.instance().objectEntity(Design_Item.class);

        entity.where().and().equal(new Design_Item().Id(itemId));
        final Design_Item item = entity.single();


        WebMeta meta = this.asyncDialog(g ->
        {

            Design_Item finalItem = item;

            WebMeta config = new UMC.Web.WebMeta();
            switch (item.Type) {

                case UIDesigner.StoreDesignTypeItem:
                    Design_Item parent = entity.where().reset().and().equal(new Design_Item().Id(item.for_id)).entities().single();


                    config = Utility.isNull(UMC.Data.JSON.deserialize(parent.Data, WebMeta.class), new UMC.Web.WebMeta());

                    break;

                case UIDesigner.StoreDesignTypeTitleDesc:
                    config = Utility.isNull(UMC.Data.JSON.deserialize(item.Data, WebMeta.class), new UMC.Web.WebMeta());


                    Design_Item item2 = new Design_Item().Id(UUID.randomUUID()).Design_id(item.design_id).For_id(item.Id)
                            .ModifiedDate(new Date()).Type(UIDesigner.StoreDesignTypeItem);


                    Design_Item max = entity
                            .where().reset().and().equal(new Design_Item().Design_id(item.design_id).For_id(item.Id))
                            .entities().max(new Design_Item().Seq(0));//.Seq+1;
                    item2.Seq = Utility.isNull(max.Seq, 0) + 1;

                    entity.insert(item2);

                    finalItem = item2;
                    request.arguments().put("Id", item2.Id);
                    break;
                default:
                    this.prompt("类型错误");
                    break;
            }
            WebMeta data = Utility.isNull(UMC.Data.JSON.deserialize(finalItem.Data, WebMeta.class), new UMC.Web.WebMeta());

            UIFormDialog from = new UIFormDialog();
            from.title("图文项");


            String total = Utility.isNull(data.get("Total"), "1");

            from.addFile(String.format("%s比例图片", total == "1" ? "100:55" : "1:1"), "_Image",
                    webr.ImageResolve(finalItem.Id, "1", 4))
                    .command("Design", "Image", new UMC.Web.WebMeta().put("id", finalItem.Id).put("seq", "1"));
            String hide = Utility.isNull(config.get("Hide"), "");
            if (hide.indexOf("HideTitle") == -1)
                from.addText("图文标题", "title", finalItem.ItemName);
            if (hide.indexOf("HideDesc") == -1)
                from.addText("图文描述", "desc", finalItem.ItemDesc);
            if (hide.indexOf("HideLeft") == -1)
                from.addText("左角价格", "left", data.get("left"));
            if (hide.indexOf("HideRight") == -1)
                from.addText("右角说明", "right", data.get("right"));
            from.addNumber("顺序", "Seq", finalItem.Seq);

            from.submit("确认", request, "Design");
            return from;
        }, "Setting");

        entity.where().reset().and().equal(new Design_Item().Id(item.Id));
        entity.update(new Design_Item().ItemName(meta.get("title"))
                .ItemDesc(meta.get("desc"))
                .Data(UMC.Data.JSON.serialize(meta))
                .ModifiedDate(new Date()).Seq(Utility.parse(meta.get("Seq"), 0)));


    }

    void config(WebRequest request, UUID itemId) {


        UMC.Data.WebResource webr = UMC.Data.WebResource.Instance();

        IObjectEntity<Design_Item> entity = Database.instance().objectEntity(Design_Item.class);

        entity.where().and().equal(new Design_Item().Id(itemId));
        Design_Item item = entity.single();
        switch (item.Type) {
            case UIDesigner.StoreDesignTypeItem:
            case UIDesigner.StoreDesignTypeProduct:
                item = entity.where().reset().and().equal(new Design_Item().Id(item.for_id)).entities().single();
                break;
        }
        Design_Item finalItem = item;
        WebMeta meta = this.asyncDialog(g ->
        {


            WebMeta data = Utility.isNull(UMC.Data.JSON.deserialize(finalItem.Data, WebMeta.class), new UMC.Web.WebMeta());

            UIFormDialog from = new UIFormDialog();
            from.title("配置");


            from.addText("缩进", "Padding", Utility.isNull(data.get("Padding"), "0"));
            from.addNumber("展示顺序", "Seq", finalItem.Seq);


            switch (finalItem.Type) {
                case UIDesigner.StoreDesignTypeBanners:
                    from.title("广告横幅");
                    break;
                case UIDesigner.StoreDesignTypeItems:
                    from.title("分块区配置");
                    from.addRadio("风格", "Model").put("展示标题", "Title", data.get("Model").equals("Title") || data.containsKey("Model") == false).put("仅显示图片 ", "Image", data.get("Model").equals("Image"));

                    break;
                case UIDesigner.StoreDesignTypeTitleDesc:
                    from.title("图文区配置");

                    String total = Utility.isNull(data.get("Total"), "1");// data["Total"] ??"1";
                    String model = Utility.isNull(data.get("Hide"), "");// data["Hide"] ??"";
                    ;
                    from.addCheckBox("界面", "Hide", "T").put("不显示标题", "HideTitle", model.indexOf("HideTitle") > -1)
                            .put("不显描述 ", "HideDesc", model.indexOf("HideDesc") > -1)
                            .put("不显左角价格 ", "HideLeft", model.indexOf("HideLeft") > -1)
                            .put("不显右角说明 ", "HideRight", model.indexOf("HideRight") > -1);


                    from.addNumber("图文数量", "Total", Utility.parse(total, 0));
                    break;
                case UIDesigner.StoreDesignTypeCaption:

                    from.title("标题配置");
                    from.addText("标题", "ItemName", finalItem.ItemName);
                    from.addCheckBox("标题隐藏", "Show", "Y").put("隐藏", "Hide", "Hide".equals(data.get("Show")));

                    break;
                case UIDesigner.StoreDesignTypeProducts:
                    from.title("商品展示配置");
                    from.addText("标题", "ItemName", finalItem.ItemName);
                    from.addRadio("展示风格", "Model").put("分块展示", "Area", "Area".equals(data.get("Model")) || data.containsKey("Model") == false).put("分行展示 ", "Rows", "Rows".equals(data.get("Model")));

                    from.addNumber("单行商品数", "Total", Utility.parse(data.get("Total"), 1));//["Total"] ? ? "2");

                    break;
                case UIDesigner.StoreDesignTypeCustom:
                    String config = data.get("Config");
                    if (Utility.isEmpty(config) == false && config.startsWith("UI")) {
                        this.context().response().redirect("Design", config);

                    } else {
                        this.prompt("参数错误");
                    }
                    break;
                default:
                    this.prompt("参数错误");
                    break;
            }
            from.submit("确认", request, "Design");
            return from;
        }, "Setting");
        String show = meta.get("Show");
        if (Utility.isEmpty(show) == false) {
            meta.put("Show", show.contains("Hide") ? "Hide" : "Show");
        }
        entity.where().reset().and().equal(new Design_Item().Id(item.Id));
        entity.update(new Design_Item().ItemName(meta.get("ItemName"))
                .Data(UMC.Data.JSON.serialize(meta))
                .ModifiedDate(new Date()).Seq(Utility.parse(meta.get("Seq"), 0)));


    }

    void banner(WebRequest request, UUID itemId) {
        UMC.Data.WebResource webr = UMC.Data.WebResource.Instance();

        IObjectEntity<Design_Item> entity = Database.instance().objectEntity(Design_Item.class);

        entity.where().and().equal(new Design_Item().Id(itemId));
        Design_Item item = entity.single();


        WebMeta meta = this.asyncDialog(g ->
        {

            Design_Item finalItem = item;
            switch (item.Type) {
                case UIDesigner.StoreDesignTypeItem:
                    break;
                case UIDesigner.StoreDesignTypeBanners:


                    Design_Item item2 = new Design_Item().Id(UUID.randomUUID()).Design_id(item.design_id).For_id(item.Id)
                            .ModifiedDate(new Date()).Type(UIDesigner.StoreDesignTypeItem);


                    Design_Item max = entity
                            .where().reset().and().equal(new Design_Item().Design_id(item.design_id).For_id(item.Id))
                            .entities().max(new Design_Item().Seq(0));//.Seq+1;
                    item2.Seq = Utility.isNull(max.Seq, 0) + 1;

                    entity.insert(item2);
                    finalItem = item2;
                    request.arguments().put("Id", item2.Id);

                    break;
                default:
                    this.prompt("类型错误");
                    break;
            }
            UIFormDialog from = new UIFormDialog();
            from.title("配置");

            String size = request.arguments().get("Size");
            if ("none".equals(size)) {
                size = "默认尺寸100:55";
            } else {

                size = String.format("参考尺寸:%s", size);
            }
            from.addFile(size, "_Image",
                    webr.ImageResolve(finalItem.Id, "1", 4))
                    .command("Design", "Image", new UMC.Web.WebMeta().put("id", finalItem.Id).put("seq", "1"));


            from.addNumber("展示顺序", "Seq", finalItem.Seq);
            from.submit("确认", request, "Design");
            return from;
        }, "Setting");


        entity.where().reset().and().equal(new Design_Item().Id(item.Id));
        entity.update(new Design_Item()
                .Data(UMC.Data.JSON.serialize(meta))
                .ModifiedDate(new Date()).Seq(Utility.parse(meta.get("Seq"), 0)));


    }

    public void processActivity(WebRequest request, WebResponse response) {
        Identity user = UMC.Security.Identity.current();
        String ssid = this.asyncDialog("Id", d -> dialogValue(user.id().toString()));
        UUID sId = UMC.Data.Utility.uuid(ssid);
        String size = this.asyncDialog("Size", g -> this.dialogValue("none"));


        UMC.Data.WebResource webr = UMC.Data.WebResource.Instance();

        IObjectEntity<Design_Item> entity = Database.instance().objectEntity(Design_Item.class);

        Design_Item item = null;//entity.single();

        if (sId != null) {
            entity.where().and().equal(new Design_Item().Id(sId));

            item = entity.single();

        }

        if (item != null && item.Type != UIDesigner.StoreDesignType) {

            Design_Item finalItem = item;
            String type = this.asyncDialog("Type", g ->
            {
                UIRadioDialog di = new UIRadioDialog();

                switch (finalItem.Type) {
                    case UIDesigner.StoreDesignTypeCustom:
                        break;
                    case UIDesigner.StoreDesignType:
                        di.title("页面设计");
                        di.options().put("编辑此项", "Edit");
                        di.options().put("增加新项", "Append");
                        di.options().put("删除此项", "Delete");
                        break;
                    case UIDesigner.StoreDesignTypeItem:
                        di.title("单项设计");
                        di.options().put("编辑此项", "Edit");
                        di.options().put("配置参数", "Config");
                        di.options().put("增加新项", "Append");
                        di.options().put("点击到...", "Union");
                        di.options().put("删除此项", "Delete");
                        break;
                    case UIDesigner.StoreDesignTypeProduct:
                        di.title("商品栏位");
                        di.options().put("调整顺序", "Seq");
                        di.options().put("配置参数", "Config");
                        di.options().put("增加商品", "Append");
                        di.options().put("删除此项", "Delete");
                        break;
                    case UIDesigner.StoreDesignTypeDiscount:
                        di.title("卡券栏位");
                        di.options().put("调整顺序", "Seq");
                        //di.options().put("配置参数", "Config");
                        di.options().put("增加卡券", "Append");
                        di.options().put("删除此项", "Delete");
                        break;
                    case UIDesigner.StoreDesignTypeCaption:

                        di.title("栏目设计");
                        di.options().put("编辑栏目", "Config");
                        di.options().put("添加横幅区", "AddBanner");
                        di.options().put("添加图标区", "AddIcon");
                        di.options().put("添加分列区", "AddItem");
                        di.options().put("添加图文区", "AddTitleDesc");

                        di.options().put("删除栏目", "Delete");
                        return di;
                    case UIDesigner.StoreDesignTypeBanners:
                        di.title("横幅栏位");
                        di.options().put("添加横幅页", "Banners");
                        di.options().put("配置参数", "Config");
                        di.options().put("删除横幅栏", "Delete");

                        break;
                    case UIDesigner.StoreDesignTypeProducts:
                        di.title("商品栏位");
                        di.options().put("添加商品", "Product");
                        di.options().put("配置参数", "Config");
                        di.options().put("删除商品栏", "Delete");

                        break;
                    case UIDesigner.StoreDesignTypeDiscounts:
                        di.title("卡券栏位");
                        di.options().put("添加卡券", "Discount");
                        //di.options().put("配置参数", "Config");
                        di.options().put("删除卡券栏", "Delete");

                        break;
                    case UIDesigner.StoreDesignTypeTitleDesc:
                        di.title("图文栏位");
                        di.options().put("添加子项", "TitleDesc");

                        di.options().put("配置图文", "Config");
                        di.options().put("删除图文栏", "Delete");
                        break;
                    case UIDesigner.StoreDesignTypeItems:
                        di.title("分列栏位");
                        di.options().put("添加子列", "Items");
                        di.options().put("配置参数", "Config");
                        di.options().put("删除分列栏", "Delete");
                        break;
                    case UIDesigner.StoreDesignTypeIcons:
                        di.title("图标栏位");
                        di.options().put("添加子项", "Icons");
                        di.options().put("配置参数", "Config");
                        di.options().put("删除图标位", "Delete");
                        break;
                    default:
                        break;

                }
                return di;
            });
            switch (type) {
                case "Seq":

                    seq(request, response, item);
                    break;
                case "Delete":
                    if (item.Type == UIDesigner.StoreDesignType) {
                        entity.where().reset().and().equal(new Design_Item().Design_id(item.Id));
                        if (entity.count() > 0) {
                            this.prompt("请先删除子项");
                        }

                        entity.where().reset().and().equal(new Design_Item().Id(sId));
                        entity.delete();
                        this.context().send("Design", true);

                    } else {
                        entity.where().reset().and().equal(new Design_Item().For_id(item.Id));
                        if (entity.count() > 0) {
                            this.prompt("请先删除子项");
                        }

                        entity.where().reset().and().equal(new Design_Item().Id(sId));
                        entity.delete();
                        this.context().send("Design", true);
                    }
                    break;
                case "TitleDesc":
                    this.titleDesc(request, sId);
                    break;
                case "Config":
                    this.config(request, sId);
                    break;
                case "Union":
                    response.redirect("Design", "Click", sId.toString(), true);

                    break;
                case "Icons":
                    icons(request, sId);
                    break;
                case "Banners":
                    banner(request, sId);
                    break;
                case "Items":
                    Items(request, sId);
                    break;
                case "Edit":
                    if (item.Type == UIDesigner.StoreDesignTypeCustom) {
                        response.redirect("Design", "Custom", new UMC.Web.WebMeta().put("Id", item.Id.toString(), "Size", size), true);
                    } else {
                        Design_Item eitem = entity.where().reset().and().equal(new Design_Item().Id(item.for_id)).entities().single();

                        switch (eitem.Type) {
                            case UIDesigner.StoreDesignTypeTitleDesc:
                                titleDesc(request, sId);
                                break;
                            case UIDesigner.StoreDesignTypeBanners:
                                banner(request, sId);
                                break;
                            case UIDesigner.StoreDesignTypeIcons:
                                icons(request, sId);
                                break;
                            case UIDesigner.StoreDesignTypeItems:
                                Items(request, sId);
                                break;

                        }
                    }
                    break;
                case "AddCaption":
                    this.caption(request, response, item.design_id, item.Id);
                    break;
                case "AddTitleDesc":
                case "AddProduct":
                case "AddItem":
                case "AddIcon":
                case "AddBanner":


                    Design_Item item3 = new Design_Item().Id(UUID.randomUUID()).Design_id(item.design_id)
                            .For_id(item.Id);

                    switch (type) {
                        case "AddProduct":
                            item3.Type = UIDesigner.StoreDesignTypeProducts;
                            break;
                        case "AddIcon":
                            item3.Type = UIDesigner.StoreDesignTypeIcons;
                            break;
                        case "AddTitleDesc":
                            item3.Type = UIDesigner.StoreDesignTypeTitleDesc;
                            break;
                        case "AddBanner":
                            item3.Type = UIDesigner.StoreDesignTypeBanners;
                            break;
                        case "AddItem":
                            item3.Type = UIDesigner.StoreDesignTypeItems;
                            break;
                        case "AddDiscount":
                            item3.Type = UIDesigner.StoreDesignTypeDiscounts;
                            break;


                    }

                    if (item3.Type != null) {

                        Design_Item max = entity
                                .where().reset().and().equal(new Design_Item().For_id(item.Id))
                                .entities().max(new Design_Item().Seq(0));//.Seq+1;
                        item3.Seq = Utility.isNull(max.Seq, 0) + 1;


                        entity.insert(item3);

                    }
                    break;
                case "Append":

                    if (item.Type == UIDesigner.StoreDesignTypeCustom) {
                        WebMeta meta = UMC.Data.JSON.deserialize(item.Data, WebMeta.class);
                        response.redirect("Design", "Custom", new UMC.Web.WebMeta().put("Config", meta.get("Config")).put("Size", size), true);
                    }
                    Design_Item aitem = entity.where().reset().and().equal(new Design_Item().Id(item.for_id)).entities().single();


                    switch (aitem.Type) {
                        case UIDesigner.StoreDesignTypeTitleDesc:
                            titleDesc(request, aitem.Id);
                            break;
                        case UIDesigner.StoreDesignTypeBanners:
                            banner(request, aitem.Id);
                            break;
                        case UIDesigner.StoreDesignTypeIcons:
                            icons(request, aitem.Id);
                            break;
                        case UIDesigner.StoreDesignTypeItems:
                            Items(request, aitem.Id);
                            break;
                    }

                    break;
            }
        } else {
            String type = this.asyncDialog("Type", g ->
            {

                UIRadioDialog di = new UIRadioDialog();
                di.title("界面设计");
                di.options().put("添加标题栏", "Caption");
                di.options().put("添加广告栏", "Banner");
                di.options().put("添加图标栏", "Icons");
                di.options().put("添加分块栏", "Items");
                return di;
            });
            Design_Item item2 = new Design_Item().Id(UUID.randomUUID()).Design_id(sId)
                    .For_id(Utility.uuidEmpty);


            switch (type) {
                case "Caption":
                    caption(request, response, sId, Utility.uuidEmpty);
                    break;
                case "TitleDesc":
                    item2.Type = UIDesigner.StoreDesignTypeTitleDesc;
                    break;
                case "Products":
                    item2.Type = UIDesigner.StoreDesignTypeProducts;
                    break;
                case "Icons":
                    item2.Type = UIDesigner.StoreDesignTypeIcons;
                    break;
                case "Banner":
                    item2.Type = UIDesigner.StoreDesignTypeBanners;
                    break;
                case "Items":
                    item2.Type = UIDesigner.StoreDesignTypeItems;

                    break;
                case "Discounts":
                    item2.Type = UIDesigner.StoreDesignTypeDiscounts;

                    break;


            }
            if (item2.Type != null) {
                Design_Item max = entity
                        .where().reset().and().equal(new Design_Item().Design_id(sId).For_id(Utility.uuidEmpty))
                        .entities().max(new Design_Item().Seq(0));//.Seq+1;
                item2.Seq = Utility.isNull(max.Seq, 0) + 1;


                entity.insert(item2);
            }


        }

        this.context().send("Design", true);

    }

}