package UMC.Activities;

import UMC.Data.Entities.Account;
import UMC.Data.Entities.User;
import UMC.Data.Sql.IObjectEntity;
import UMC.Data.Utility;
import UMC.Security.Identity;
import UMC.Security.Membership;
import UMC.Web.*;

import java.util.Hashtable;
import java.util.UUID;

public class AccountPasswordActivity extends WebActivity {

    @Override
    public void processActivity(WebRequest request, WebResponse response) {

        int type = Utility.parse(this.asyncDialog("AccountType", g -> dialogValue("-1")), 0);
        Identity cUser = UMC.Security.Identity.current();
        UUID user_id = Utility.uuid(this.asyncDialog("user_id", g -> dialogValue(cUser.id().toString())));


        User user = UMC.Data.Database.instance().objectEntity(User.class)
                .where().and().equal(new User().Id(user_id)).entities().single();
        if (user == null) {

            type = 0;
        }

        String VerifyCode = this.asyncDialog("VerifyCode", g -> dialogValue("0"));

        int finalType1 = type;
        WebMeta Password = this.asyncDialog(g ->
        {
            if (request.sendValues() != null) {
                WebMeta meta = request.sendValues();
                if (meta.containsKey("NewPassword")) {
                    return dialogValue(meta);
                }
            }
            UIFormDialog dialog = new UIFormDialog();

            if (finalType1 > 0) {
                dialog.title("找回密码");
            } else if (finalType1 < 0) {
                dialog.title("找回密码");
                if (cUser.isAuthenticated() == false) {
                    this.prompt("请登录");
                }
                dialog.addPassword("原密码", "Password", true);//.Put("plo")
            } else {
                if (cUser.isAuthenticated() == false) {
                    this.prompt("请登录");
                }
                dialog.title("设置密码");
            }
            dialog.addPassword("新密码", "NewPassword", false);
            dialog.addPassword("确认新密码", "NewPassword2", false).put("ForName", "NewPassword");
            dialog.submit("确认修改", request, "account");
            return dialog;

        }, "Password");
        Membership mc = UMC.Security.Membership.Instance();
        if (Password.containsKey("Password")) {


            if (mc.Password(cUser.name(), Password.get("Password"), 0) == 0) {
                mc.Password(cUser.name(), Password.get("NewPassword"));
                this.prompt("密码修改成功，您可以用新密码登录了", false);


                this.context().send("account", true);

            } else {
                this.prompt("您的原密码不正确");
            }
        } else {

            if (user == null && cUser.id().compareTo(user_id) == 0) {
                Membership.Instance().CreateUser(cUser.id(), cUser.name(), Password.get("NewPassword"), cUser.alias());
                this.prompt("密码修改成功，您可以用新密码登录了", false);
                this.context().send("account", true);
            }

            IObjectEntity<Account> accountIObjectEntity = UMC.Data.Database.instance().objectEntity(Account.class);
            Account eac = UMC.Data.Database.instance().objectEntity(Account.class)
                    .where().and().equal(new Account().User_id(user_id).Type(type))
                    .entities().single();

            Hashtable m = UMC.Data.JSON.deserialize(eac.ConfigData, Hashtable.class);


            if (VerifyCode.equals(m.get("VerifyCode"))) {
                mc.Password(user.Username, Password.get("NewPassword"));

                Account newA = new Account();
                newA.ConfigData = "{}";

                accountIObjectEntity.update(newA);
                this.prompt("密码修改成功，您可以用新密码登录了", false);
                this.context().send("account", true);
            } else {
                this.prompt("非法入侵");
            }
        }


    }
}
