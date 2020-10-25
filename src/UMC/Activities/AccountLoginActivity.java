package UMC.Activities;

import UMC.Data.Configuration;
import UMC.Data.Database;
import UMC.Data.Entities.Account;
import UMC.Data.Sql.IObjectEntity;
import UMC.Data.Utility;
import UMC.Net.Message;
import UMC.Security.AccessToken;
import UMC.Security.Identity;
import UMC.Security.Membership;
import UMC.Security.Principal;
import UMC.Web.*;
import UMC.Web.UI.UIDesc;

import java.util.Hashtable;
import java.util.Map;


public class AccountLoginActivity extends WebActivity {
    void SendMobileCode(String mobile) {

        Identity user = UMC.Security.Identity.current();


        Map hask = new Hashtable();


        Configuration<Map> session = new Configuration(mobile, Map.class);

        if (session.Value != null) {
            hask = session.Value;
        }

        int times = Utility.parse(String.format("%s", hask.get("Times")), 0) + 1;
        if (times > 5) {

            int sTime = Utility.parse(String.format("%s", hask.get("Date")), 0) + 1 + 3600 * 3;

            if (sTime > System.currentTimeMillis() / 1000) {
                this.prompt("您已经超过了5次，请您三小时后再试");
            } else {
                times = 0;
            }
        }
        String code = (Math.random() + "").substring(3, 9);
        hask.put("Time", times);
        hask.put("Code", code);
        hask.put("Date", System.currentTimeMillis() / 1000);
        session.commit(hask, user);


        Message.instance().send("Login", hask, mobile);
    }

    @Override
    public void processActivity(WebRequest request, WebResponse response) {
        String type = this.asyncDialog("type", t -> this.dialogValue("auto"));
        switch (type) {
            case "wx":
                this.context().send(new UMC.Web.WebMeta().put("type", "login.weixin"), true);
                break;
            case "qq":
                this.context().send(new UMC.Web.WebMeta().put("type", "login.qq"), true);
                break;
        }


        WebMeta user = this.asyncDialog(d ->
        {
            UIFormDialog dialog = new UIFormDialog();


            dialog.title("账户登录");
            switch (type) {
                case "User":
                    this.context().send("LoginChange", false);
                {
                    dialog.addText("用户名", "Username", "").put("placeholder", "用户名/手机/邮箱");

                    dialog.addPassword("用户密码", "Password", "");

                    dialog.submit("登录", request, "User", "LoginChange");
                    UIDesc uidesc = new UMC.Web.UI.UIDesc(new WebMeta().put("eula", "用户协议").put("private", "隐私政策"));
                    uidesc.desc("登录即同意“{eula}”和“{private}”");
                    uidesc.style().alignCenter();
                    uidesc.style().color(0x888).size(14).height(34);
                    uidesc.style().name("eula").color(0x3194d0).click(new UIClick("365lu/provision/eula").send("Subject", "UIData"));
                    uidesc.style().name("private").color(0x3194d0).click(new UIClick("365lu/provision/private").send("Subject", "UIData"));
                    dialog.add(uidesc);
                    dialog.addUIIcon('\uf2c1', "免密登录").command(request.model(), request.cmd(), "Mobile");
                    dialog.addUIIcon('\uf1c6', "忘记密码").put("Model", request.model()).put("Command", "Forget");
                    dialog.addUIIcon('\uf234', "注册新用户").put("Model", request.model()).put("Command", "Register");

                }
                break;
                default:
                    this.context().send("LoginChange", false);
                {
                    dialog.addText("手机号码", "Username", "").put("placeholder", "注册的手机号码");

                    dialog.addVerify("验证码", "VerifyCode", "您收到的验证码").put("For", "Username").put("To", "Mobile")
                            .put("Command", request.cmd()).put("Model", request.model());
                    dialog.submit("登录", request, "User", "LoginChange");

                    UIDesc uidesc = new UMC.Web.UI.UIDesc(new WebMeta().put("eula", "用户协议").put("private", "隐私政策"));
                    uidesc.desc("登录即同意“{eula}”和“{private}”");
                    uidesc.style().alignCenter();
                    uidesc.style().color(0x888).size(14).height(34);
                    uidesc.style().name("eula").color(0x3194d0).click(new UIClick("365lu/provision/eula").send("Subject", "UIData"));
                    uidesc.style().name("private").color(0x3194d0).click(new UIClick("365lu/provision/private").send("Subject", "UIData"));
                    dialog.add(uidesc);
                    dialog.addUIIcon('\uf13e', "密码登录").command(request.model(), request.cmd(), "User");
                    dialog.addUIIcon('\uf234', "注册新用户").command(request.model(), "Register");//.Put("Model", request.Model).Put("Command", "Register");
                }
                break;
            }
//            if (request.isApp()) {
//                dialog.addText("手机号码", "Username", "").put("placeholder", "手机");
//                dialog.addVerify("验证码", "VerifyCode", "您收到的验证码").put("For", "Username").put("To", "Mobile")
//                        .put("Command", request.cmd()).put("Model", request.model());
//                dialog.submit("登录", request, "User");
//                dialog.addUIIcon('\uf234', "注册新用户").put("Model", request.model()).put("Command", "Register");
//
//            } else {
//                dialog.addText("用户名", "Username", "").put("placeholder", "手机/邮箱");
//                dialog.addPassword("用户密码", "Password", "");
//                dialog.submit("登录", request, "User");
//                dialog.addUIIcon('\uf1c6', "忘记密码").put("Model", request.model()).put("Command", "Forget");
//                dialog.addUIIcon('\uf234', "注册新用户").put("Model", request.model()).put("Command", "Register");
//            }
            return dialog;

        }, "Login");
        if (user.containsKey("Mobile")) {
            String mobile = user.get("Mobile");

            Account account = Database.instance().objectEntity(UMC.Data.Entities.Account.class)
                    .where().and().equal(new Account().Name(mobile).Type(Membership.MOBILE_ACCOUNT_KEY))
                    .entities().single();


            if (account == null) {
                this.prompt("不存在此账户");
            }


            this.SendMobileCode(mobile);
            this.prompt("验证码已发送", false);
            this.context().send(new UMC.Web.WebMeta().event("VerifyCode", this.asyncDialog("UI", "none"), new UMC.Web.WebMeta().put("text", "验证码已发送")), true);
        }
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
//                if (iden.isInRole(UMC.Security.Membership.UserRole)) {
//                    this.prompt("您是内部账户，不可从此入口登录");
//                }


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
