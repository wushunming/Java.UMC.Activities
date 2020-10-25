package UMC.Activities;

import UMC.Activities.Entities.Design_Item;
import UMC.Data.Database;
import UMC.Data.JSON;
import UMC.Web.*;
import UMC.Web.UI.*;
import UMC.Data.Utility;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UIDesigner {
    boolean _editer;

    public UIDesigner(boolean editer) {
        _editer = editer;


    }

    public final static int


            StoreDesignTypeCaption = 1,
            StoreDesignTypeProduct = 2,
            StoreDesignTypeBanners = 4,
            StoreDesignTypeTitleDesc = 128,
            StoreDesignTypeItems = 16,
            StoreDesignTypeIcons = 64,
            StoreDesignTypeItem = 32,
            StoreDesignTypeCustom = 256,
            StoreDesignTypeProducts = 512,
            StoreDesignTypeDiscounts = 1024,
            StoreDesignTypeDiscount = 2048,
            StoreDesignType = 2048 * 2;

    void Sliders(UISlider slider, UUID parentId, List<Design_Item> baners) {
        //List<UISlider> list = new LinkedList<>();
        UMC.Data.WebResource webr = UMC.Data.WebResource.Instance();
        for (Design_Item b : baners) {

            String src = (webr.ImageResolve(b.Id, "1", 0) + "!slider?" + this.TimeSpan(b.ModifiedDate));

            if (_editer) {
                slider.add(new UIClick(new UMC.Web.WebMeta().put("Id", b.Id)).send("Design", "Item"), src);
            } else {
                if (UMC.Data.Utility.isEmpty(b.Click) == false) {
                    slider.add(UMC.Data.JSON.deserialize(b.Click, UIClick.class), src);

                } else {
                    slider.add(src);
                }

            }
        }
        if (slider.size() == 0 && _editer) {

            slider.add(new UIClick(parentId.toString()).send("Design", "Item"));

        }


    }

    void Sliders(Design_Item parent, List<Design_Item> baners, UISection U) {
        if (baners.size() > 0) {
            WebMeta config = Utility.isNull(UMC.Data.JSON.deserialize(parent.Data, WebMeta.class), new UMC.Web.WebMeta());


            UISlider slider2 = new UISlider();//.create(
            Sliders(slider2, parent.Id, baners);
            int[] paddings = UIStyle.padding(config);
            if (paddings.length > 0) {
                slider2.style().padding(paddings);
            }
            U.put(slider2);
        } else if (_editer) {

            UIDesc desc = new UIDesc("\ue907");
            desc.click(new UIClick(parent.Id.toString())
                    .send("Design", "Item"));
            desc.desc("{desc}\r\n配置横幅栏");
            desc.style().alignCenter().name("desc", new UIStyle().font("wdk").size(38)
            );
            U.put(desc);

        }
    }

    void Icons(UUID parentId, List<Design_Item> baners, UISection U) {
//        List<UIEventText> list = new LinkedList<>();
        UIIcon uiIcon = new UIIcon();
        uiIcon.style().name("icon").font("wdk").size(24);
        UMC.Data.WebResource webr = UMC.Data.WebResource.Instance();
        for (Design_Item b : baners) {
            UIEventText slider = new UIEventText(b.ItemName);
            if (Utility.isEmpty(b.Data) == false) {
                WebMeta s = UMC.Data.JSON.deserialize(b.Data, WebMeta.class);

                slider.icon(s.get("icon"), s.get("color"));

            } else {
                slider.src(webr.ImageResolve(b.Id, "1", 4) + "?" + this.TimeSpan(b.ModifiedDate));

            }
            slider.click(this.Click(b));

            uiIcon.add(slider);

        }
        if (uiIcon.size() > 0) {
            U.put(uiIcon);
        } else if (_editer) {
            UIDesc desc = new UIDesc("\ue907");
            desc.desc("{desc}\r\n配置图标栏");
            desc.click(new UIClick(parentId.toString())
                    .send("Design", "Item"));

            desc.style().alignCenter().name("desc", new UIStyle().font("wdk").size(38));
            U.put(desc);

        }

    }

    void Items(Design_Item parent, List<Design_Item> baners, UISection U) {
        UUID parentId = parent.Id;
        // List<UIItem> list = new LinkedList<>();
        UMC.Data.WebResource webr = UMC.Data.WebResource.Instance();
        if (baners.size() > 0) {
            UIItems uiItems = new UIItems();
            for (int i = 0; i < baners.size() && i < 4; i++) {
                Design_Item b = baners.get(i);
                WebMeta icon = Utility.isNull(UMC.Data.JSON.deserialize(b.Data, WebMeta.class), new UMC.Web.WebMeta());

                icon.put("click", this.Click(b));

                String t = "100";
                switch (baners.size()) {
                    case 1:
                        t = "4-1";
                        break;
                    case 2:
                        t = "2-1";
                        break;
                    case 3:
                        if (i == 0) {
                            t = "2-1";
                        }
                        break;
                }

                icon.put("src", String.format("%s!%s?%d", webr.ImageResolve(b.Id, "1", 0), t, this.TimeSpan(b.ModifiedDate)));

                uiItems.add(icon);

            }
            U.put(uiItems);

        } else if (_editer) {

            UIDesc desc = new UIDesc("\ue907");
            desc.desc("{desc}\r\n配置分块栏");

            desc.style().alignCenter().name("desc", new UIStyle().font("wdk").size(38).click(new UIClick(parentId.toString())
                    .send("Design", "Item")));
            U.put(desc);
        }
    }

    void TitleDesc(Design_Item parent, List<Design_Item> items, UISection U) {

        UMC.Data.WebResource webr = UMC.Data.WebResource.Instance();


        WebMeta config = Utility.isNull(UMC.Data.JSON.deserialize(parent.Data, WebMeta.class), new UMC.Web.WebMeta());

        int rows = UMC.Data.Utility.parse(Utility.isNull(config.get("Total"), "1"), 1);
        if (rows <= 1) {
            int[] padding = UIStyle.padding(config);
            for (Design_Item i : items) {
                UICell tdesc = this.TitleDesc(config, i, "cms1", webr);
                if (padding.length > 0)
                    tdesc.style().padding(padding);
                U.put(tdesc);
            }
        } else {
            int m = 0;
            String hide = Utility.isNull(config.get("Hide"), "");
            if (hide.contains("HideTitle")) {
                m |= 1;
            }
            if (hide.contains("HideDesc")) {
                m |= 2;
            }
            if (hide.contains("HideLeft")) {
                m |= 4;
            }
            if (hide.contains("HideRight")) {
                m |= 8;
            }

            int len = items.size();

            for (int i = 0; (i + rows - 1) < len; i = i + rows) {
                List<WebMeta> ls = new LinkedList<>();
                for (int c = 0; c < rows; c++) {
                    UICell p = TitleDesc(config, items.get(i + c), "350", webr);
                    ls.add(new UMC.Web.WebMeta().put("value", p.data()).put("format", p.format()).put("style", p.style()));

                }
                UICell desc = UICell.create("ItemsTitleDesc", new UMC.Web.WebMeta().put("items", ls.toArray()).put("total", rows).put("show", m));
                int[] paddings = UIStyle.padding(config);
                if (paddings.length > 0) {
                    desc.style().padding(paddings);
                }
                U.put(desc);
            }
            int total = len % rows;

            if (total > 0) {
                List<WebMeta> ls = new LinkedList<>();
                for (int c = total; c > 0; c--) {
                    UICell p = TitleDesc(config, items.get(len - c), "350", webr);
                    ls.add(new UMC.Web.WebMeta().put("value", p.data()).put("format", p.format()).put("style", p.style()));

                }

                UICell desc = UICell.create("ItemsTitleDesc", new UMC.Web.WebMeta().put("items", ls.toArray()).put("total", rows).put("show", m));
                int[] paddings = UIStyle.padding(config);
                if (paddings.length > 0) {
                    desc.style().padding(paddings);
                }
                U.put(desc);

            }


        }
        if (items.size() == 0 && _editer) {

//

            UIDesc desc = new UIDesc("\ue907");
            desc.desc("{desc}\r\n配置图文栏");
            desc.click(new UIClick(parent.Id.toString())
                    .send("Design", "Item"));

            desc.style().alignCenter().name("desc", new UIStyle().font("wdk").size(38));
            U.put(desc);

        }
    }

    UIImageTitleDescBottom TitleDesc(WebMeta config, Design_Item item, String img, UMC.Data.WebResource webr) {

        WebMeta data = Utility.isNull(UMC.Data.JSON.deserialize(item.Data, WebMeta.class), new UMC.Web.WebMeta());

        int m = 0;
        String hide = Utility.isNull(config.get("Hide"), "");

        if (hide.contains("HideTitle")) {
            m |= 1;
            data.remove("title");
        }
        if (hide.contains("HideDesc")) {
            m |= 2;
            data.remove("desc");
        }
        if (hide.contains("HideLeft")) {
            m |= 4;
            data.remove("left");
        }
        if (hide.contains("HideRight")) {
            m |= 8;
            data.remove("right");
        }
        data.put("show", m);
        String src = (String.format("%s!%s?%d", webr.ImageResolve(item.Id, "1", 0), img, this.TimeSpan(item.ModifiedDate)));

        UIImageTitleDescBottom btm = new UIImageTitleDescBottom(data, src);
        btm.click(this.Click(item));
        String left = data.get("left");
        if (Utility.isEmpty(left) == false) {
            int i = -1;

            Pattern pattern = Pattern.compile("\\d+\\.?\\d{0,2}}");

            Matcher matcher = pattern.matcher(left);

            StringBuffer operatorStr = new StringBuffer();
            int startIndex = 0;
            while (matcher.find()) {
                operatorStr.append(left.substring(startIndex, matcher.start()));
                i++;
                switch (i) {
                    case 0:
                        data.put("price", matcher.group());
                        operatorStr.append("￥{1:price} ");
                        break;
                    case 1:
                        data.put("orgin", matcher.group());
                        operatorStr.append(" {orgin}");
                        break;
                }
                startIndex = matcher.end();

            }
            operatorStr.append(left.substring(startIndex));
            btm.left(operatorStr.toString());
            btm.style().name("price", new UIStyle().size(16).color(0xdb3652)).name("unit", new UIStyle().size(12).color(0x999)).name("orgin", new UIStyle().color(0x999).size(12).delLine());

        }
        ;
        return btm;

    }

    public static UIClick Click(Design_Item item, boolean editer) {
        if (editer) {

            return new UIClick(item.Id.toString()).send("Design", "Item");
        } else {
            return JSON.deserialize(item.Click, UIClick.class);
        }
    }


    UIClick Click(Design_Item item) {

        return Click(item, _editer);
    }

    private UISection Section(UISection Us, List<Design_Item> items) {
        List<Design_Item> groups = Utility.findAll(items, g -> g.for_id.compareTo(Utility.uuidEmpty) == 0);

        Design_Item b = Utility.find(groups, g -> g.Type == StoreDesignTypeBanners);


        if (b != null) {
            Sliders(b, Utility.findAll(items, it -> it.for_id.compareTo(b.Id) == 0), Us);

            groups.remove(b);
        }
        for (Design_Item bp : groups) {
            UISection use = Us;
            if (Us.length() > 0) {
                use = Us.newSection();
            }
            switch (Utility.isNull(bp.Type, 0)) {
                case StoreDesignTypeBanners:
                    Sliders(bp, Utility.findAll(items, it -> it.for_id.compareTo(bp.Id) == 0), use);
                    break;
                case StoreDesignTypeIcons:
                    Icons(bp.Id, Utility.findAll(items, it -> it.for_id.compareTo(bp.Id) == 0), use);
                    break;
                case StoreDesignTypeItems:

                    Items(bp, Utility.findAll(items, it -> it.for_id.compareTo(bp.Id) == 0), use);
                    break;
                case StoreDesignTypeTitleDesc:

                    TitleDesc(bp, Utility.findAll(items, it -> it.for_id.compareTo(bp.Id) == 0), use);
                    break;
                case StoreDesignTypeProducts:
                case StoreDesignTypeDiscounts:
                    break;
                case StoreDesignTypeCaption:

                    WebMeta config = Utility.isNull(UMC.Data.JSON.deserialize(bp.Data, WebMeta.class), new UMC.Web.WebMeta());
                    if ("Hide".equals(config.get("Show"))) {
                        if (_editer) {
                            UITitleMore more = new UITitleMore(bp.ItemName).more("已隐藏{3:more}");
                            more.style().name("more", new UIStyle().color(0xc00));

                            use.put(more.click(this.Click(bp)));
                        }
                    } else {
                        UITitleMore more = new UITitleMore(bp.ItemName).click(this.Click(bp));

                        more.style().padding(UIStyle.padding(config));
                        use.put(more);
                    }
                    List<Design_Item> groups2 = Utility.findAll(items, it -> it.for_id.compareTo(bp.Id) == 0);// items.FindAll(it = > it.for_id == bp.Id);
                    for (Design_Item bp2 : groups2) {
                        switch (bp2.Type) {
                            case StoreDesignTypeBanners:
                                Sliders(bp2, Utility.findAll(items, it -> it.for_id.compareTo(bp2.Id) == 0), use);
                                break;
                            case StoreDesignTypeIcons:
                                Icons(bp2.Id, Utility.findAll(items, it -> it.for_id.compareTo(bp2.Id) == 0), use);
                                break;
                            case StoreDesignTypeItems:
                                Items(bp2, Utility.findAll(items, it -> it.for_id.compareTo(bp2.Id) == 0), use);
                                break;
                            case StoreDesignTypeTitleDesc:

                                TitleDesc(bp2, Utility.findAll(items, it -> it.for_id.compareTo(bp2.Id) == 0), use);
                                break;
                        }
                    }


                    break;


            }
        }
        return Us;
    }

    public static int TimeSpan(Date date) {
        return date != null ? (int) (date.getTime() / 1000) : 0;
    }

    public UISection Section(String title, UUID design_id) {

        UISection Us = Utility.isEmpty(title) ? UISection.create() : UISection.create(new UITitle(title));
        return this.Section(Us, design_id);
    }

    public UISection Section(UISection Us, UUID design_id) {
        List<Design_Item> items = new LinkedList<>();

        Database.instance().objectEntity(Design_Item.class)
                .where().and().equal(new Design_Item().Design_id(design_id))
                .entities().order().asc(new Design_Item().Seq(0)).entities()
                .query(dr -> items.add(dr));


        return this.Section(Us, items);
    }

}
