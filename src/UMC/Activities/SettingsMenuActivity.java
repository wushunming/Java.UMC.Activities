package UMC.Activities;

import UMC.Data.Database;
import UMC.Data.Entities.Menu;
import UMC.Data.Sql.IObjectEntity;
import UMC.Data.Utility;
import UMC.Web.*;

import javax.xml.crypto.Data;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;


public class SettingsMenuActivity extends WebActivity {

    public void processActivity(WebRequest request, WebResponse response) {

        UUID TypeId = UMC.Data.Utility.uuid(this.asyncDialog("Id", dkey ->
        {
            List<Menu> menus = new LinkedList<>();
            UMC.Data.Database.instance().objectEntity(UMC.Data.Entities.Menu.class).order().asc(new UMC.Data.Entities.Menu().Seq(0))
                    .entities().query(dr -> menus.add(dr));

            List<WebMeta> menu = new LinkedList<>();
            List<Menu> tops = Utility.findAll(menus, d -> Utility.uuidEmpty.compareTo(d.ParentId) == 0);
            for (Menu p : tops) {
                boolean IsDisable = p.IsDisable == true;
                WebMeta m = new WebMeta().put("icon", p.Icon).put("text", p.Caption).put("id", p.Id).put("disable", p.IsDisable == true);

                //m.Put("url")
                menu.add(m);
                List<Menu> childs = Utility.findAll(menus, c -> c.ParentId == p.Id);
                if (childs.size() > 0) {
                    List<WebMeta> cmenu = new LinkedList<>();
                    for (Menu ch : childs) {
                        cmenu.add(new WebMeta().put("id", ch.Id).put("text", ch.Caption).put("url", ch.Url)
                                .put("disable", IsDisable || ch.IsDisable == true));
                    }

                    m.put("menu", cmenu);
                } else {
                    m.put("url", p.Url);
                }
            }

            response.redirect(menu);
            return this.dialogValue("none");

        }), true);

        IObjectEntity<Menu> cateEntity = Database.instance().objectEntity(UMC.Data.Entities.Menu.class);


        cateEntity.where().and().equal(new UMC.Data.Entities.Menu().Id(Utility.isNull(TypeId, UUID.randomUUID())));
        Menu link = Utility.isNull(cateEntity.single(), new Menu());

        UUID parentId = Utility.isNull(link.ParentId, Utility.isNull(Utility.uuid(this.asyncDialog("ParentId", "none")), Utility.uuidEmpty));


        WebMeta userValue = this.asyncDialog(d ->
        {
            UIFormDialog fdlg = new UIFormDialog();
            fdlg.title("菜单设置");

            if (Utility.isEmpty(parentId)) {
                fdlg.addOption("菜单图标", "Icon", link.Icon, Utility.isEmpty(link.Icon) ? "请选择" : "已选择").placeholder("请参考UMC图标库")
                        .command("System", "Icon");

            }
            fdlg.addText("菜单标题", "Caption", link.Caption);
            if (Utility.isEmpty(parentId)) {
                fdlg.addText("菜单网址", "Url", link.Url).notRequired();
            } else {

                fdlg.addText("菜单网址", "Url", link.Url);//.Put("tip", "");
            }
            fdlg.addNumber("展示顺序", "Seq", link.Seq);
            if (link.Id != null) {
                fdlg.addCheckBox("", "Status", "n").add("禁用此菜单", "Disable", link.IsDisable == true);
                fdlg.addUIIcon('\uf084', "权限设置").command(request.model(), "Auth", link.Id.toString());
            }


            fdlg.submit("确认", request, "Settings.Menu");
            return fdlg;
        }, "Settings");
        Menu nmenu = new Menu();
        Utility.setField(nmenu, userValue.map());
//        UMC.Data.Reflection.SetProperty(link, userValue.GetDictionary());
        if (link.Id != null) {
            nmenu.IsDisable = Utility.isNull(userValue.get("Status"), "").contains("Disable");
            cateEntity.update(nmenu);
            this.prompt("更新成功", false);
        } else {
            nmenu.ParentId = parentId;
            nmenu.Id = UUID.randomUUID();
            link.IsDisable = false;
            cateEntity.insert(nmenu);
            this.prompt("添加成功", false);
        }
        this.context().send("Settings.Menu", true);

    }
}
