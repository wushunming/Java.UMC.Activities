package UMC.Activities;

import UMC.Activities.Entities.Design_Item;
import UMC.Data.Database;
import UMC.Data.Sql.IObjectEntity;
import UMC.Data.WebResource;
import UMC.Security.Identity;
import UMC.Web.*;
import UMC.Data.Utility;

import java.net.URI;
import java.util.*;

//@Mapping(model = "Design", cmd = "UI", auth = WebAuthType.user, desc = "移动UI设计")
public class DesignUIActivity extends WebActivity {
    boolean _isEditer;

    public DesignUIActivity() {
        this._isEditer = true;
    }

    public DesignUIActivity(boolean isediter) {
        _isEditer = isediter;

    }

    void Design(WebRequest request, UUID itemId) {

        IObjectEntity<Design_Item> entity = Database.instance().objectEntity(Design_Item.class);


        entity.where().and().equal(new Design_Item().Id(itemId));
        Design_Item item = entity.single();

        if (item == null) {

            Design_Item max = entity
                    .where().reset().and().equal(new Design_Item().Design_id(Utility.uuidEmpty).For_id(Utility.uuidEmpty))
                    .entities().max(new Design_Item().Seq(0));
            max.Seq = Utility.isNull(max.Seq, 0) + 1;
            item = max;
        }
        Design_Item fitem = item;
        WebMeta meta = this.asyncDialog(g ->
        {

            UIFormDialog from = new UIFormDialog();
            from.title("页面分类项");

            from.addText("标题", "ItemName", fitem.ItemName);

            from.addNumber("顺序", "Seq", fitem.Seq);

            from.submit("确认", request, "Design");
            return from;
        }, "Setting");
        Design_Item newItem = new Design_Item().ItemName(meta.get("ItemName")).ModifiedDate(new Date()).Seq(Utility.parse(meta.get("Seq"), 0));
        entity.where().reset().and().equal(new Design_Item().Id(itemId));
        entity.iff(e -> e.update(newItem) == 0, e -> e.insert(newItem.Design_id(Utility.uuidEmpty).Type(UIDesigner.StoreDesignType).For_id(Utility.uuidEmpty).Id(UUID.randomUUID())));


        this.context().send("Design", true);
    }

    void Delete(UUID uuid) {
        IObjectEntity<Design_Item> entity = Database.instance().objectEntity(Design_Item.class);
        entity.where().reset().and().equal(new Design_Item().Design_id(uuid));
        if (entity.count() > 0) {
            this.prompt("设计页面有组件，先删除组件，再删除页面项");
        }

        entity.where().reset().and().equal(new Design_Item().Id(uuid));
        entity.delete();
        this.context().send("Design", true);
    }

    @Override
    public void processActivity(WebRequest request, WebResponse response) {

        UUID designId = UMC.Data.Utility.uuid(this.asyncDialog("Id", g -> this.dialogValue(Utility.uuidEmpty.toString())));//this.DialogValue(response.Ticket[AttributeNames.TICKET_STORE_ID])), true).Value;


        if (_isEditer) {

            WebMeta form = Utility.isNull(request.sendValues(), request.arguments());

            this.asyncDialog("Model", anycId -> {
                if (form.containsKey("limit") == false) {

                    this.context().send(new UISectionBuilder(request.model(), request.cmd(), new WebMeta().put("Id", designId))
                            .refreshEvent("Design", "image")
                            .builder(), true);
                }
                IObjectEntity<Design_Item> entity = Database.instance().objectEntity(Design_Item.class);
                entity.where().and().equal(new Design_Item().Design_id(Utility.uuidEmpty).For_id(Utility.uuidEmpty));
                entity.order().asc(new Design_Item().Seq(0));
                Design_Item[] headers = entity.query();

                UISection section = UISection.create(new UITitle("UMC"));

                int limit = UMC.Data.Utility.parse(form.get("limit"), 10);
                int start = UMC.Data.Utility.parse(form.get("start"), 0);

                switch (headers.length) {
                    case 0:
                        break;
                    case 1:
                        section.title().title(headers[0].ItemName);
                        //     section.title().right('\ue907', new UIClick("Id", headers[0].Id.toString(), "Model", "none").model(request.model(), request.cmd()));
                        break;
                    default:
                        if (start == 0) {

                            List<WebMeta> items = new LinkedList<>();
                            for (Design_Item item : headers) {

                                items.add(new UMC.Web.WebMeta().put("text", item.ItemName).put("search", new WebMeta().put("Id", item.Id)));
                            }
                            section.put(UICell.create("TabFixed", new UMC.Web.WebMeta().put("items", items)));

                        }
                        break;
                }


                if (designId.compareTo(Utility.uuidEmpty) == 0) {
                    switch (headers.length) {
                        case 0:
                            break;
                        default:
                            new UIDesigner(true).Section(section, headers[0].Id);
                            break;
                    }
                } else {
                    new UIDesigner(true).Section(section, designId);
                }
                if (section.length() == 0) {

                    section.put("Desc", new UMC.Web.WebMeta().put("desc", "未有设计分类项，请添加").put("icon", "\uEA05"), new UMC.Web.WebMeta().put("desc", "{icon}\n{desc}"),
                            new UIStyle().align(1).color(0xaaa).padding(20, 20).bgColor(0xfff).size(12).name("icon", new UIStyle().font("wdk").size(60)));
                }
                UIFootBar footer = new UIFootBar();
                footer.fixed(true);

                switch (headers.length) {
                    case 0:

                        footer.text(new UIEventText("添加分类项").click(new UIClick("Model", "News", "Type", "Append").send(request.model(), request.cmd())));
                        break;
                    default:
                        UUID did = designId;
                        if (designId.compareTo(Utility.uuidEmpty) == 0) {

                            did = headers[0].Id;

                        }
                        footer.icon(new UIEventText("分类项").icon('\uf009').click(new UIClick("Model", "News", "Id", did.toString()).send(request.model(), request.cmd())));


                        footer.text(new UIEventText("增加UI组件").click(new UIClick(did.toString()).send("Design", "Item")));
                        footer.text(new UIEventText("查看效果").style(new UIStyle().bgColor(0xef4f4f))
                                .click(new UIClick("Model", "News", "Type", "View").send(request.model(), request.cmd())));


                        break;
                }


                section.uiFootBar(footer);
                response.redirect(section);
                return this.dialogValue("none");
            });
            String type = this.asyncDialog("Type", g ->
            {
                UIRadioDialog di = new UIRadioDialog();
                di.title("页面设计");
                di.options().put("编辑分类项", "Edit");
                di.options().put("增加分类项", "Append");
                di.options().put("删除此分类", "Delete");
                return di;
            });
            switch (type) {
                case "Edit":
                    Design(request, designId);
                    break;
                case "Append":
                    Design(request, UUID.randomUUID());
                    break;
                case "Delete":
                    Delete(designId);
                    break;
                case "View":
                    if (request.isApp()) {
                        List<WebMeta> tabs = new LinkedList<>();

                        Database.instance().objectEntity(Design_Item.class)
                                .where().and().equal(new Design_Item().Design_id(Utility.uuidEmpty).For_id(Utility.uuidEmpty))

                                .entities().order().asc(new Design_Item().Seq(0))
                                .entities().query(dr -> {

                            tabs.add(new UMC.Web.WebMeta().put("text", dr.ItemName).put("search", new UMC.Web.WebMeta().put("Id", dr.Id.toString())).put("cmd", "Home", "model", "UI"));

                        });
                        if (tabs.size() == 1) {
                            UISectionBuilder builder = new UISectionBuilder("UI", "Home", new WebMeta().put("Id", tabs.get(0).meta("search").get("Id")));

                            this.context().send(builder.builder(), true);


                        } else {

                            this.context().send("Tab", new WebMeta().put("sections", tabs).put("text", "UMC界面设计"), true);

                        }
                    } else {

                        this.asyncDialog("From", k ->
                        {

                            UIFormDialog fm = new UMC.Web.UIFormDialog();
                            fm.title("移动效果体验");
                            fm.addImage(URI.create(UMC.Data.Utility.qrUrl("https://oss.365lu.cn/Click/UI/Home/")));


                            fm.addPrompt("请用支持UMC协议的APP“扫一扫”。");

                            return fm;
                        });
                        break;
                    }
                    break;
            }

        } else {


            if (designId.compareTo(Utility.uuidEmpty) == 0) {

                IObjectEntity<Design_Item> entity = Database.instance().objectEntity(Design_Item.class);
                entity.where().and().equal(new Design_Item().Design_id(Utility.uuidEmpty).For_id(Utility.uuidEmpty));

                List<WebMeta> tabs = new LinkedList<>();
                entity.order().asc(new Design_Item().Seq(0));
                entity.query(dr ->
                {
                    tabs.add(new UMC.Web.WebMeta().put("text", dr.ItemName).put("search", new UMC.Web.WebMeta().put("Id", dr.Id)).put("cmd", "Home", "model", "UI"));

                });

                Map chash = new Hashtable();
                UITitle title = new UITitle("UMC移动界面");
                title.left('\uea0e', UIClick.search());

                title.right(new UIEventText().icon('\uf2c0').click(new UIClick().send("Account", "Info")));


                chash.put("sections", tabs);
                chash.put("title", title);
                response.redirect(chash);

            } else {
                UIDesigner designer = new UIDesigner(false);
                response.redirect(designer.Section("", designId));

            }
        }

    }

}
