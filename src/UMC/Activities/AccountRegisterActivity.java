package UMC.Activities;

import UMC.Data.Configuration;
import UMC.Data.Database;
import UMC.Data.Entities.Account;
import UMC.Data.Sql.IObjectEntity;
import UMC.Data.Utility;
import UMC.Security.AccessToken;
import UMC.Security.Identity;
import UMC.Security.Membership;
import UMC.Web.*;

import java.util.Hashtable;
import java.util.Map;
import java.util.UUID;

public class AccountRegisterActivity extends WebActivity {
    void SendMobileCode(String mobile) {
        Identity user = UMC.Security.Identity.current();


        Map hask = new Hashtable();


        Configuration<Map> session = new Configuration<Map>(mobile, Map.class);

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
        /***
         * 请实现发短信,并做好发送次数限制
         */

    }

    @Override
    public void processActivity(WebRequest request, WebResponse response) {

        WebMeta user = this.asyncDialog(d ->
        {
            if (request.sendValues() != null && request.sendValues().size() > 0) {
                return this.dialogValue(request.sendValues());
            }

            UIFormDialog dialog = new UIFormDialog();
            dialog.title("账户注册");
            dialog.addText("昵称", "Alias", "");

            dialog.addText("手机号码", "Username", "");
            dialog.addVerify("验证码", "VerifyCode", "您收到的验证码").put("For", "Username").put("To", "Mobile")
                    .command(request.model(), request.cmd());


            dialog.submit("确认注册", request, "register");
            return dialog;

        }, "Register");


        if (user.containsKey("Mobile")) {
            String mobile = user.get("Mobile");
            UMC.Data.Entities.Account account = Database.instance().objectEntity(UMC.Data.Entities.Account.class)
                    .where().and()
                    .equal(new Account().Name(mobile).Type(Membership.MOBILE_ACCOUNT_KEY)).entities().single();
            if (account != null) {
                this.prompt("此手机号码已经注册，你可直接登录");
            }
            this.SendMobileCode(mobile);
            this.prompt("验证码已发送", false);
            this.context().send(new UMC.Web.WebMeta().event("VerifyCode", this.asyncDialog("UI", "none"), new UMC.Web.WebMeta().put("text", "验证码已发送")), true);
        }
        String username = user.get("Username");
        if (user.containsKey("VerifyCode")) {
            String VerifyCode = user.get("VerifyCode");
            Configuration<Map> session = new Configuration(Map.class, username);
            if (session.Value != null) {
                String code = (String) session.Value.get("Code");

                if (VerifyCode.equalsIgnoreCase(code) == false) {
                    this.prompt("请输入正确的验证码");
                }
            } else {
                this.prompt("请输入正确的验证码");

            }
        }

        IObjectEntity<Account> entity = Database.instance().objectEntity(Account.class);


        Account ac = new Account();

        ac.Name = username;

        if (Utility.IsEmail(username)) {
            ac.Type = UMC.Security.Membership.EMAIL_ACCOUNT_KEY;
            entity.where().and().equal(ac);

        } else if (Utility.IsPhone(username)) {
            ac.Type = UMC.Security.Membership.MOBILE_ACCOUNT_KEY;
            entity.where().and().equal(ac);
        }
        if (ac.Type != null) {
            this.prompt("只支持手机号注册");
        }
        if (entity.count() > 0) {
            switch (ac.Type) {
                case UMC.Security.Membership.EMAIL_ACCOUNT_KEY:
                    this.prompt("此邮箱已经注册");
                    break;
                default:
                    this.prompt("此手机号已经注册");
                    break;
            }
        }
        String passwork = user.get("Password");
        String NewPassword2 = user.get("NewPassword2");

        if (Utility.isEmpty(NewPassword2) == false) {
            if (passwork.equals(NewPassword2) == false) {
                this.prompt("两次密码不相同，请确认密码");
            }

        }
        String Alias = user.get("Alias");// ??username;
        Membership uM = UMC.Security.Membership.Instance();
        UUID uid = uM.CreateUser(username, passwork, Alias);
        if (uid != null) {


            Identity iden = uM.Identity(username);

            AccessToken.login(iden, AccessToken.token(), request.isApp() ? "App" : "Client", true);

            this.context().send(new WebMeta().put("type", "register"), false);
            this.context().send(new WebMeta().put("type", "User"), false);
            this.prompt("注册成功");
        } else {
            this.prompt("已经存在这个用户");
        }


    }
}
