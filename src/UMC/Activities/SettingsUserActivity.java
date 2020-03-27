package UMC.Activities;

import UMC.Data.Database;
import UMC.Data.Entities.Role;
import UMC.Data.Entities.Session;
import UMC.Data.Entities.User;
import UMC.Data.Entities.UserToRole;
import UMC.Data.Sql.IObjectEntity;
import UMC.Data.Utility;
import UMC.Security.Identity;
import UMC.Security.Membership;
import UMC.Web.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;


public class SettingsUserActivity extends WebActivity {

    void Setting(UUID userId) {
        IObjectEntity<User> userEntity = Database.instance().objectEntity(User.class);
        User user = userEntity.where().and().equal("Id", userId).entities().single();
        IObjectEntity<UserToRole> userRoleEntity = Database.instance().objectEntity(UserToRole.class);

        userRoleEntity.where().and().equal("user_id", user.Id);


        IObjectEntity<Role> roleEntity = Database.instance().objectEntity(Role.class);
        roleEntity.where().and().unEqual("Rolename", Membership.GuestRole);

        WebMeta userValue = this.asyncDialog(d ->
        {
            UIFormDialog fdlg = new UIFormDialog();
            fdlg.menu("角色", "Settings", "Role", "");
            fdlg.title("账户设置");
            DateFormat dateFormat = new SimpleDateFormat("yy-MM-dd HH:mm");


            ListItemCollection opts2 = new ListItemCollection();

            opts2.put("别名", user.Alias);
            opts2.put("登录名", user.Username);
            if (user.ActiveTime != null)
                opts2.put("最后登录", dateFormat.format(user.ActiveTime));
            if (user.RegistrTime != null)
                opts2.put("注册时间", dateFormat.format(user.RegistrTime));

            fdlg.addTextValue(opts2);
            int flags = Utility.isNull(user.Flags, Membership.UserFlagsNormal);
            ListItemCollection opts = new ListItemCollection();
            boolean selected = ((flags & Membership.UserFlagsLock)) > 0;
            opts.put("锁定", "1", selected);
            selected = ((int) (flags & Membership.UserFlagsDisabled)) > 0;
            opts.put("禁用", "16", selected);
            fdlg.addCheckBox("状态", "Flags", opts, "0");

            List<UserToRole> uRs = new LinkedList<>();

            userRoleEntity.query(dr -> uRs.add(dr));
            ListItemCollection opts3 = new ListItemCollection();


            roleEntity.query(dr ->
            {
                switch (dr.Rolename) {
                    case Membership.AdminRole:
                        opts3.put("超级管理员", dr.Id.toString(), Utility.exists(uRs, ur -> ur.role_id.compareTo(dr.Id) == 0));
                        break;
                    case Membership.UserRole:
                        opts3.put("内部员工", dr.Id.toString(), Utility.exists(uRs, ur -> ur.role_id.compareTo(dr.Id) == 0));
                        break;
                    case "StoreManager":
                        opts3.put("店长", dr.Id.toString(), Utility.exists(uRs, ur -> ur.role_id.compareTo(dr.Id) == 0));
                        break;
                    case "Finance":
                        opts3.put("财务", dr.Id.toString(), Utility.exists(uRs, ur -> ur.role_id.compareTo(dr.Id) == 0));
                        break;
                    default:
                        opts3.put(dr.Rolename, dr.Id.toString(), Utility.exists(uRs, ur -> ur.role_id.compareTo(dr.Id) == 0));
                        break;
                }
            });

            fdlg.addCheckBox("部门角色", "Roles", opts3, "None");
            return fdlg;
        }, "User");
        int Flags = Membership.UserFlagsNormal;
        for (String k : userValue.get("Flags").split(",")) {
            Flags = Flags | Utility.parse(k, Membership.UserFlagsNormal);
        }
        userEntity.update(new User().Alias(userValue.get("Alias")).Flags(Flags));


        List<UserToRole> rids = new LinkedList<>();
        for (String k : userValue.get("Roles").split(",")) {


            switch (k) {
                case "None":
                    break;
                default:
                    UserToRole userToRole = new UserToRole();
                    userToRole.role_id = Utility.uuid(k);
                    userToRole.user_id = userId;
                    rids.add(userToRole);
                    ;
                    break;
            }
        }
        userRoleEntity.delete();
        if (rids.size() > 0) {
            userRoleEntity.insert(rids.toArray(new UserToRole[0]));

        }
        /*
         * 清空用户Seesion信息，让其重新登录
         * */

        Database.instance().objectEntity(Session.class)
                .where().and().equal(new Session().User_id(user.Id)).entities().delete();

        this.prompt("设置成功");
    }


    void setUser(User user) {
        WebMeta users = this.asyncDialog(d ->
        {
            ListItemCollection opts = new ListItemCollection();
            UIFormDialog fmDg = new UIFormDialog();
            if (user == null) {
                fmDg.title("添加新账户");

                fmDg.addText("账户名", "Username", "");
                fmDg.addText("别名", "Alias", "");
                fmDg.addPassword("密码", "Password", true);

            } else {
                fmDg.title("变更别名");
                opts.put("登录名", user.Username);
                fmDg.addText("新别名", "Alias", user.Alias);

            }
            fmDg.submit("确认提交", this.context().request(), "Setting");
            return fmDg;
        }, "User");
        if (user == null) {
            UUID userId = Membership.Instance().CreateUser(users.get("Username").trim(), users.get("Password"), users.get("Alias"));
            if (userId == null) {
                this.prompt(String.format("已经存在%s用户名", users.get("Username")));
            } else {
                Membership.Instance().AddRole(users.get("Username").trim(), Membership.UserRole);
            }


            this.prompt("账户添加成功", false);


        } else {
            Membership.Instance().ChangeAlias(user.Username, users.get("Alias"));
            this.prompt(String.format("%s的别名已重置成%s", user.Username, users.get("Alias")), false);
        }
        this.context().send(new UMC.Web.WebMeta().put("type", "Setting"), true);


    }

    void setPassword(User user) {
        WebMeta users = this.asyncDialog(d ->
        {
            ListItemCollection opts = new ListItemCollection();
            UIFormDialog fmDg = new UIFormDialog();
            fmDg.title("重置密码");
            opts.put("别名", user.Alias);
            opts.put("登录名", user.Username);
            fmDg.addTextValue(opts);
            fmDg.addPassword("密码", "Password", true);

            fmDg.submit("确认提交", this.context().request(), "Setting");
            return fmDg;
        }, "User");
        UMC.Security.Membership.Instance().Password(user.Username, users.get("Password"));
        this.context().send(new UMC.Web.WebMeta().put("type", "Setting"), false);
        this.prompt(String.format("%s的密码已重置", user.Alias));


    }


    @Override
    public void processActivity(WebRequest request, WebResponse response) {
        String strUser = this.asyncDialog("Id", d ->
        {
            UserDialog dlg = new UserDialog();
            dlg.search(true, false);// = true;
            dlg.setPage(true);// = true;
            if (request.isMaster()) {
                dlg.menu("创建", "Settings", "User", "News");
            }
            dlg.refreshEvent("Setting");
            return dlg;
        });


        UUID userId = Utility.uuid(strUser, true);

        IObjectEntity<User> userEntity = Database.instance().objectEntity(User.class);
        User user = userEntity.where().and().equal("Id", userId)
                .entities().single();

        if (user != null) {

            String setting = this.asyncDialog("Setting", d ->
            {
                UIRadioDialog frm = new UIRadioDialog();
                frm.title("用户操作");
                frm.options().put("部门角色", "Setting");
                frm.options().put("重置密码", "Passwrod");
                frm.options().put("设置权限", "Auth");
                frm.options().put("变更别名", "Alias");

                return frm;
            });
            switch (setting) {
                case "Setting":
                    this.Setting(userId);
                    break;
                case "Auth":
                    response.redirect("Settings", "Auth", new WebMeta().put("Type", "User", "Value", user.Username), true);
                    break;
                case "Passwrod":
                    setPassword(user);
                    break;
                case "Alias":
                    setUser(user);
                    break;
            }
        } else {
            setUser(user);
        }
    }
}