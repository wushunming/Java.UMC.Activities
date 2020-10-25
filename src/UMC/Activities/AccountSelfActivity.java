package UMC.Activities;


import UMC.Data.Database;
import UMC.Data.Entities.Account;
import UMC.Data.Entities.User;
import UMC.Data.Sql.IObjectEntity;
import UMC.Data.Utility;
import UMC.Security.Identity;
import UMC.Security.Membership;
import UMC.Web.*;
import UMC.Web.UI.UIImageTextValue;

import java.util.Date;
import java.util.UUID;

class AccountSelfActivity extends WebActivity {


    public void processActivity(WebRequest request, WebResponse response) {

        Identity identity = Identity.current();

        String Type = this.asyncDialog("Type", "Client");
        switch (Type) {
            case "Client":
                if (identity.isAuthenticated() == false) {
                    this.prompt("未登录", false);
                    response.redirect(request.model(), "Login");
                }
                break;
            case "Cashier":
                if (request.isCashier() == false) {
                    this.prompt("未登录", false);
                    response.redirect("Settings", "Login");

                }
                break;
        }
        UUID sid = identity.id();
        IObjectEntity<User> userEntity = Database.instance().objectEntity(User.class);
        User user = userEntity.where().and().equal(new User().Id(identity.id()))
                .entities().single();


        String Model = this.asyncDialog("Model", g ->
        {
            WebMeta form = Utility.isNull(request.sendValues(), new UMC.Web.WebMeta());


            if (form.containsKey("limit") == false) {
                this.context().send(new UISectionBuilder(request.model(), request.cmd())
                        .refreshEvent("Setting", "image", "Email", "Mobile")
                        .builder(), true);

            }


            String logoUrl = UMC.Data.WebResource.Instance().ImageResolve(user.Id, "1", 4);

            UITitle title = new UITitle("我的账户");


            UISection ui = UISection.create(title);
            UIImageTextValue imageTextValue = new UIImageTextValue(logoUrl, "头像", "");
            imageTextValue.style().name("image-width", "100");
            imageTextValue.click(new UIClick("id", user.Id.toString(), "seq", "1")
                    .send("Design", "Picture"));

            ui.put(imageTextValue);
            ui.putCell("昵称", user.Alias, new UIClick("Type", Type, g, "Alias").send(request.model(), request.cmd()));


            ui.putCell('\uf084', "登录账号", user.Username, new UIClick().send(request.model(), "Password"));


            IObjectEntity<Account> accountEntity = Database.instance().objectEntity(Account.class);

            Account[] accounts = accountEntity.where().and().equal(new Account().User_id(sid)).entities().query();

            String name = user.Username;

            Account ac = Utility.find(accounts, a -> a.Flags == Membership.EMAIL_ACCOUNT_KEY);
            if (ac != null && Utility.isEmpty(ac.Name) == false) {

                name = ac.Name;

                int c = name.indexOf('@');
                if (c > 0) {
                    String cname = name.substring(0, c);
                    name = name.substring(0, 2) + "***" + name.substring(c);
                }
                if ((ac.Flags & Membership.UserFlagsUnVerification) == Membership.UserFlagsUnVerification) {
                    name = name + "(未验证)";

                }
            } else {
                name = "点击绑定";
            }

            UISection section = ui.newSection().putCell('\uf199', "邮箱", name, new UIClick("Name", "Email").send("Account", "Email"));

            ac = Utility.find(accounts, a -> a.Flags == Membership.MOBILE_ACCOUNT_KEY);


            if (ac != null && Utility.isEmpty(ac.Name) == false) {
                name = ac.Name;
                if (name.length() > 3) {
                    name = name.substring(0, 3) + "****" + name.substring(name.length() - 3);
                }
                if ((ac.Flags & Membership.UserFlagsUnVerification) == Membership.UserFlagsUnVerification) {
                    name = name + "(未验证)";
                }
            } else {
                name = "点击绑定";
            }

            section.putCell('\ue91a', "手机号码", name, new UIClick("Name", "Mobile").send("Account", "Mobile"));

            UICell cell = UICell.create("UI", new UMC.Web.WebMeta().put("text", "退出登录").put("Icon", "\uf011").put("click", new UIClick().send("Account", "Close")));
            cell.style().name("text", new UIStyle().color(0xf00));
            ui.newSection().newSection().put(cell);
            response.redirect(ui);
            return this.dialogValue("none");

        });
        switch (Model) {
            case "Alias":
                String Alias = this.asyncDialog("Alias", a -> new UITextDialog(identity.alias()).title("修改别名"));
                Membership.Instance().ChangeAlias(identity.name(), Alias);
                this.prompt(String.format("您的账户的别名已修改成%s", Alias), false);

                this.context().send(new UMC.Web.WebMeta().put("type", "Setting"), true);


                break;
        }
    }


}