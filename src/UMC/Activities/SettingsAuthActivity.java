package UMC.Activities;


import UMC.Data.Entities.Role;
import UMC.Data.Entities.Wildcard;
import UMC.Data.JSON;
import UMC.Data.Sql.IObjectEntity;
import UMC.Data.Utility;
import UMC.Data.WebResource;
import UMC.Security.AuthManager;
import UMC.Security.Identity;
import UMC.Web.*;

import java.util.*;


class SettingsAuthActivity extends WebActivity {

    void Wildcard(WebRequest request, WebResponse response, String name) {
        IObjectEntity<Wildcard> wddEntity = UMC.Data.Database.instance().objectEntity(UMC.Data.Entities.Wildcard.class);
        Wildcard wdk = wddEntity.where().and().equal(new Wildcard().WildcardKey(name)).entities().single();//.Single();

        List<Map> auths = new LinkedList<>();
        if (wdk != null) {

            Map[] s = JSON.deserialize(wdk.Authorizes, Map[].class);
            for (Map map : s)
                auths.add(map);

        }
        String Type = this.asyncDialog("WType", gg ->
        {
            WebMeta form = Utility.isNull(request.sendValues(), new UMC.Web.WebMeta());
            if (form.containsKey("limit") == false) {
                this.context().send(new UISectionBuilder(request.model(), request.cmd(), new WebMeta(request.arguments()))
                        .refreshEvent("Wildcard")
                        .builder(), true);
            }
            UISection ui = UMC.Web.UISection.create(new UITitle("权限设置"));
            ui.putCell('\uf084', "标识", Utility.uuid(name) != null ? "单项主键" : name);


            UISection ui3 = ui.newSection().putCell('\uf007', "许可用户", "添加", new UIClick(new WebMeta(request.arguments()).put(gg, "User")).send(request.model(), request.cmd()));
            List<Map> users = Utility.findAll(auths, g -> "UserAllow".equals(g.get("Type")));
            List<String> uids = new LinkedList<>();
            for (Map u : users) {
                uids.add(u.get("Value").toString());
            }

            List<Identity> dusers = UMC.Security.Membership.Instance().Identity(uids.toArray(new String[0]));


            for (Map u : users) {
                String value = (String) u.get("Value");
                String text = value;
                Identity u1 = Utility.find(dusers, d -> d.name().equalsIgnoreCase(value));//  dusers.Find(d = > d.Name == u.Value);
                if (u1 != null) {
                    text = u1.alias();
                }
                UICell cell = UICell.create("Cell", new WebMeta().put("value", value).put("text", text));//.Put("Icon", '\uf007'));

                ui3.delete(cell, new UIEventText().click(new UIClick(new WebMeta(request.arguments()).put(gg, value)).send(request.model(), request.cmd())));
            }
            if (users.size() == 0) {
                ui3.put("Desc", new UMC.Web.WebMeta().put("desc", "未设置许可用户").put("icon", "\uEA05"), new UMC.Web.WebMeta().put("desc", "{icon}\n{desc}"),
                        new UIStyle().align(1).color(0xaaa).padding(20, 20).bgColor(0xfff).size(12).name("icon", new UIStyle().font("wdk").size(60)));//.Name

            }

            UISection ui2 = ui.newSection().putCell('\uf0c0', "许可角色", "添加", new UIClick(new WebMeta(request.arguments()).put(gg, "Role")).send(request.model(), request.cmd()));
            List<Map> roles = Utility.findAll(auths, g -> "RoleAllow".equalsIgnoreCase(g.get("Type").toString()));

            for (Map u : roles) {
                String value = (String) u.get("Value");// u.Value;
                String text = value;
                UICell cell = UICell.create("Cell", new WebMeta().put("text", value));//.Put("Icon", '\uf0c0'));

                ui2.delete(cell, new UIEventText().click(new UIClick(new WebMeta(request.arguments()).put(gg, value)).send(request.model(), request.cmd())));
            }
            if (roles.size() == 0) {
                ui2.put("Desc", new UMC.Web.WebMeta().put("desc", "未设置许可角色").put("icon", "\uEA05"), new UMC.Web.WebMeta().put("desc", "{icon}\n{desc}"), new UIStyle().align(1).color(0xaaa).padding(20, 20).bgColor(0xfff).size(12).name("icon", new UIStyle().font("wdk").size(60)));//.Name

            }
            response.redirect(ui);
            return this.dialogValue("none");
        });
        switch (Type) {
            case "Role":
                String role = this.asyncDialog("SelectRole", request.model(), "SelectRole");
                auths.removeIf(k -> role.equalsIgnoreCase((String) k.get("Value")));
                auths.add(new WebMeta().put("Type", "RoleAllow").put("Value", role).map());

                wddEntity.iff(e -> e.update(new Wildcard().Authorizes(UMC.Data.JSON.serialize(auths))) == 0
                        , e -> e.insert(new Wildcard().WildcardKey(name).Authorizes(UMC.Data.JSON.serialize(auths))));


                this.context().send("Wildcard", true);
                break;
            case "User":

                String user = this.asyncDialog("SelectUser", request.model(), "SelectUser");

                auths.removeIf(k -> user.equalsIgnoreCase((String) k.get("Value")));
                auths.add(new WebMeta().put("Type", "UserAllow").put("Value", user).map());

                wddEntity.iff(e -> e.update(new Wildcard().Authorizes(UMC.Data.JSON.serialize(auths))) == 0
                        , e -> e.insert(new Wildcard().WildcardKey(name).Authorizes(UMC.Data.JSON.serialize(auths))));

                this.context().send("Wildcard", true);
                break;
            default:
                Map a = Utility.find(auths, k -> Type.equalsIgnoreCase((String) k.get("Value")));
                if (a != null) {
                    auths.remove(a);
                    wddEntity.update(new Wildcard().Authorizes(UMC.Data.JSON.serialize(auths)));
                    if (Utility.exists(auths, k -> ((String) k.get("Type")).equalsIgnoreCase((String) a.get("Type"))) == false) {
                        this.context().send("Wildcard", true);

                    }
                }
                break;
        }

    }

    @Override
    public void processActivity(WebRequest request, WebResponse response) {


        IObjectEntity<Role> roleEntity = UMC.Data.Database.instance().objectEntity(Role.class);
        String RoleType = this.asyncDialog("Type", d ->
        {
            if (roleEntity.count() < 4) {
                return UIDialog.returnValue("User");
            }
            UIRadioDialog rd = new UIRadioDialog();
            rd.title("选择设置账户类型");
            rd.options().add("角色", "Role");
            rd.options().add("用户", "User");
            return rd;
        });
        switch (RoleType) {
            case "Role":
            case "User":
                break;
            default:
                this.Wildcard(request, response, RoleType);
                return;
        }


        String setValue = this.asyncDialog("Value", d ->
        {
            if (RoleType.equalsIgnoreCase("role")) {

                UIRadioDialog rd = new UIRadioDialog();
                rd.title("请选择设置权限的角色");
                roleEntity.where().reset().and().notIn("Rolename",
                        UMC.Security.Membership.GuestRole
                        , UMC.Security.Membership.AdminRole);

                roleEntity.query(dr -> rd.options().add(dr.Rolename, dr.Rolename));
                return rd;
            } else {
                UserDialog userDialog = new UserDialog();
                userDialog.title("请选择设置权限的账户");
                return userDialog;

            }
        });

        List<WebMeta> configuration = WebServlet.auths();

        if (configuration.size() == 0) {
            this.prompt("现在的功能不需要设置权限");
        }
        List<String> wdcks = new LinkedList<>();
        for (int i = 0, l = configuration.size(); i < l; i++) {
            wdcks.add(configuration.get(i).get("key"));
        }
        List<Wildcard> wdks = new LinkedList<>();

        IObjectEntity<Wildcard> wddEntity = UMC.Data.Database.instance().objectEntity(Wildcard.class);
        wddEntity.where().and().in("WildcardKey", wdcks.toArray())
                .entities().query(dr -> wdks.add(dr));


        String WildcardKey = this.asyncDialog("Wildcards", d ->
        {
            UICheckboxDialog fmdg = new UICheckboxDialog("None");
            fmdg.title("功能权限设置");


            for (int i = 0, l = configuration.size(); i < l; i++) {

                WebMeta provider = configuration.get(i);
                String id = provider.get("key");

                Wildcard wdk = Utility.find(wdks, w -> id.equalsIgnoreCase(w.WildcardKey));

                if (wdk != null) {
                    Object[] config = (Object[]) JSON.deserialize(wdk.Authorizes);
                    if (config != null) {
                        boolean isS = false;
                        if (RoleType.equalsIgnoreCase("Role")) {
                            isS = Utility.exists(config, cd -> {
                                Map map = (Map) cd;
                                return map.get("Type").toString().equalsIgnoreCase("RoleDeny")
                                        && map.get("Value").toString().equalsIgnoreCase(setValue);
                            });

                        } else {
                            isS = Utility.exists(config, cd -> {
                                Map map = (Map) cd;
                                return map.get("Type").toString().equalsIgnoreCase("UserDeny")
                                        && map.get("Value").toString().equalsIgnoreCase(setValue);
                            });
                        }
                        fmdg.options().add(provider.get("desc"), id, !isS);
                    } else {
                        fmdg.options().add(provider.get("desc"), id, true);
                    }
                } else {
                    fmdg.options().add(provider.get("desc"), id, true);
                }
            }

            return fmdg;

        });

        for (int i = 0, l = configuration.size(); i < l; i++) {
            WebMeta provider = configuration.get(i);
            String id = provider.get("key");

            Wildcard wdk = Utility.find(wdks, w -> id.equalsIgnoreCase(w.WildcardKey));
            List<Object> authorizes = new LinkedList<>();

            if (wdk != null) {
                Object[] config = (Object[]) JSON.deserialize(wdk.Authorizes);

                authorizes.addAll(Arrays.asList(config));
            }
            if (RoleType.equalsIgnoreCase("Role")) {
                authorizes.removeAll(
                        Utility.findAll(authorizes, a -> {

                            Map map = (Map) a;
                            String Type = map.get("Type").toString();
                            return (Type.equalsIgnoreCase(AuthManager.RoleDeny) ||
                                    Type.equalsIgnoreCase(AuthManager.RoleAllow))
                                    && map.get("Value").toString().equalsIgnoreCase(setValue);

                        })
                );
            } else {
                authorizes.removeAll(
                        Utility.findAll(authorizes, a -> {

                            Map map = (Map) a;
                            String Type = map.get("Type").toString();
                            return (Type.equalsIgnoreCase(AuthManager.UserAllow) ||
                                    Type.equalsIgnoreCase(AuthManager.UserDeny))
                                    && map.get("Value").toString().equalsIgnoreCase(setValue);

                        })
                );
            }
            if (WildcardKey.indexOf(id) == -1) {

                if (RoleType.equalsIgnoreCase("Role")) {
                    Map map = new HashMap();
                    map.put("Value", setValue);
                    map.put("Type", AuthManager.RoleDeny);
                    authorizes.add(map);

                } else {
                    Map map = new HashMap();
                    map.put("Value", setValue);
                    map.put("Type", AuthManager.UserDeny);
                    authorizes.add(map);

                }
            }

            Wildcard newWdd = new Wildcard();
            newWdd.Authorizes = JSON.serialize(authorizes);
            newWdd.WildcardKey = id;
            newWdd.Description = provider.get("desc");

            wddEntity.where().reset().and().equal(new Wildcard().WildcardKey(id))

                    .entities().iff(e -> e.update(newWdd) == 0, e -> e.insert(newWdd));

        }
        this.prompt("设置成功");

    }


}