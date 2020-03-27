package UMC.Activities;

import UMC.Data.Configuration;
import UMC.Data.Database;
import UMC.Data.Entities.Account;
import UMC.Data.Sql.IObjectEntity;
import UMC.Data.Utility;
import UMC.Security.AccessToken;
import UMC.Security.Identity;
import UMC.Security.Membership;
import UMC.Security.Principal;
import UMC.Web.*;

import java.util.Hashtable;


public class AccountLoginActivity extends WebActivity {
    @Override
    public void processActivity(WebRequest request, WebResponse response) {


        WebMeta user = this.asyncDialog(d ->
        {
            UIFormDialog dialog = new UIFormDialog();
            dialog.title("账户登录");
            if (request.isApp()) {
                dialog.addText("手机号码", "Username", "").put("placeholder", "手机");
                dialog.addVerify("验证码", "VerifyCode", "您收到的验证码").put("For", "Username").put("To", "Mobile")
                        .put("Command", request.cmd()).put("Model", request.model());
                dialog.submit("登录", request, "User");
                dialog.addUIIcon('\uf234', "注册新用户").put("Model", request.model()).put("Command", "Register");

            } else {
                dialog.addText("用户名", "Username", "").put("placeholder", "手机/邮箱");
                dialog.addPassword("用户密码", "Password", "");
                dialog.submit("登录", request, "User");
                dialog.addUIIcon('\uf1c6', "忘记密码").put("Model", request.model()).put("Command", "Forget");
                dialog.addUIIcon('\uf234', "注册新用户").put("Model", request.model()).put("Command", "Register");
            }
            return dialog;

        }, "Login");
        String username = user.get("Username");

        Membership userManager = UMC.Security.Membership.Instance();
        if (user.containsKey("VerifyCode")) {
            String VerifyCode = user.get("VerifyCode");
            Configuration<Hashtable> session = new Configuration<Hashtable>(username, Hashtable.class);
            if (session.Value != null) {
                String code = (String) session.Value.get("Code");// as string;
                if (code.equalsIgnoreCase(VerifyCode) == false) {
                    this.prompt("请输入正确的验证码");
                }
            } else {
                this.prompt("请输入正确的验证码");

            }
            IObjectEntity<Account> entity = Database.instance().objectEntity(Account.class);
            UMC.Data.Entities.Account ac = new UMC.Data.Entities.Account();
            ac.Name = username;
            ac.Type = Membership.MOBILE_ACCOUNT_KEY;

            Account eData = entity.where().and().equal(ac).entities().single();
            if (eData == null) {

                this.prompt("无此号码关联的账户，请注册");
            } else {
                Identity iden = userManager.Identity(eData.user_id);
                if (iden.isInRole(UMC.Security.Membership.UserRole)) {
                    this.prompt("您是内部账户，不可从此入口登录");
                }


                AccessToken.login(iden, AccessToken.token(), request.isApp() ? "App" : "Client", true);
                this.context().send("User", true);
            }
        } else {

            int maxTimes = 5;
            UMC.Security.Identity identity = null;
            if (Utility.IsPhone(username)) {
                identity = userManager.Identity(username, Membership.MOBILE_ACCOUNT_KEY);
                if (identity == null)
                    userManager.Identity(username);
            } else if (username.indexOf('@') > -1) {
                identity = userManager.Identity(username, Membership.EMAIL_ACCOUNT_KEY);
                if (identity == null)
                    identity = userManager.Identity(username);
            } else {
                identity = userManager.Identity(username);
            }
            if (identity == null) {
                this.prompt("用户不存在，请确认用户名");
            }
            String passwork = user.get("Password");
            int times = userManager.Password(identity.name(), passwork, maxTimes);
            switch (times) {
                case 0:
                    Identity iden = userManager.Identity(username);
                    if (iden.isInRole(UMC.Security.Membership.UserRole)) {
                        this.prompt("您是内部账户，不可从此入口登录");
                    }

                    Principal.create(iden);

                    AccessToken.login(iden, AccessToken.token(), request.isApp() ? "App" : "Client", true);


                    this.context().send("User", true);


                    break;
                case -2:
                    this.prompt("您的用户已经锁定，请过后登录");
                    break;
                case -1:
                    this.prompt("您的用户不存在，请确定用户名");

                    break;
                default:
                    this.prompt(String.format("您的用户和密码不正确，您还有%d次机会", maxTimes - times));

                    break;
            }
        }

    }
}
