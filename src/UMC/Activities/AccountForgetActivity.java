package UMC.Activities;

import UMC.Data.Entities.Account;
import UMC.Data.Entities.User;
import UMC.Data.JSON;
import UMC.Data.Sql.IObjectEntity;
import UMC.Data.Utility;
import UMC.Data.Database;
import UMC.Security.Membership;
import UMC.Web.*;

import java.util.*;


public class AccountForgetActivity extends WebActivity {


    boolean forget(String name) {
        IObjectEntity<Account> accountIObjectEntity = Database.instance().objectEntity(Account.class);
        if (Utility.IsPhone(name)) {
            accountIObjectEntity.where().and().equal(new Account().Name(name).Type(Membership.MOBILE_ACCOUNT_KEY));//


        } else {
            accountIObjectEntity.where().and().equal(new Account().Name(name).Type(Membership.EMAIL_ACCOUNT_KEY));//


        }
        Account acc = accountIObjectEntity.single();
        if (acc != null) {
            User user = Database.instance().objectEntity(User.class)
                    .where().and().equal("Id", acc.user_id).entities().single();
            Map map = Utility.isNull((Map) JSON.deserialize(acc.ConfigData), new HashMap());
            Map hask = Utility.fieldMap(user);
            switch (Utility.isNull(acc.Type, Membership.MOBILE_ACCOUNT_KEY)) {
                case Membership.MOBILE_ACCOUNT_KEY: {
                    int times = Utility.parse(String.format("%s", map.get("Times")), 0) + 1;
                    if (times > 5) {

                        int sTime = Utility.parse(String.format("%s", map.get("Date")), 0) + 1 + 3600 * 3;

                        if (sTime > System.currentTimeMillis() / 1000) {
                            this.prompt("您已经超过了5次，请您三小时后再试");
                        } else {
                            times = 0;
                        }
                    }
                    String code = (Math.random() + "").substring(3, 9);
                    map.put("Times", times);
                    map.put("Date", System.currentTimeMillis() / 1000);
                    map.put("VerifyCode", code);
                    hask.put("Code", code);

                    accountIObjectEntity.update(new Account().ConfigData(JSON.serialize(map)));
                    sendForget(acc.Name, hask);

                }

                return true;

                case Membership.EMAIL_ACCOUNT_KEY: {

                    String code = (Math.random() + "").substring(3, 9);
                    map.put("Date", System.currentTimeMillis() / 1000);
                    map.put("UserHostAddress", this.context().request().userHostAddress());
                    map.put("VerifyCode", code);
                    hask.put("Code", code);

                    accountIObjectEntity.update(new Account().ConfigData(JSON.serialize(map)));
                    this.sendEmail(name, map);
                }
                return true;

            }
        }

        return false;
    }

    void sendForget(String mobile, Map map) {
        Utility.format("{Code}", map);

        /***
         * 请自行实现发送忘记密码短信
         */
    }

    void sendEmail(String email, Map map) {
        Utility.format("{Code}", map);

        /***
         * 请自行实现发送邮件
         */
    }

    @Override
    public void processActivity(WebRequest request, WebResponse response) {

        String username = this.asyncDialog("Username", d ->
        {
            UIFormDialog fd = new UIFormDialog();
            fd.title("找回密码");
            fd.addText("", "Username", "").put("placeholder", "手机号码或邮箱");

            fd.submit("下一步", request, "Forget");
            return fd;
        });
        IObjectEntity<Account> entity = Database.instance().objectEntity(Account.class);
        UMC.Data.Entities.Account ac = new UMC.Data.Entities.Account();

        ac.Name = username;
        if (Utility.IsEmail(username)) {
            ac.Type = Membership.EMAIL_ACCOUNT_KEY;
            entity.where().and().equal(ac);

        } else if (Utility.IsPhone(username)) {
            ac.Type = Membership.MOBILE_ACCOUNT_KEY;
            entity.where().and().equal(ac);
        }
        if (ac.Type != null) {
            this.prompt("只支持手机号和邮箱找回密码");
        }
        Account acct = entity.single();
        if (acct == null) {
            switch (ac.Type) {
                case Membership.EMAIL_ACCOUNT_KEY:
                    this.prompt("没有找到此邮箱绑定账户");
                    break;
                default:
                    this.prompt("没有找到此手机号绑定账户");
                    break;
            }
        }
        String Code = this.asyncDialog("Code", g ->
        {
            String ts = ac.Type == Membership.EMAIL_ACCOUNT_KEY ? "邮箱" : "手机";
            UIFormDialog fd = new UIFormDialog();
            fd.addTextValue().put(ts, username);

            fd.addVerify("验证码", "Code", String.format("%s收到的验证码", ts))
                    .command(request.model(), request.cmd(), new WebMeta().put("Username", username).put("Code", "Reset"));
            fd.title("验证" + ts);
            fd.submit("验证", request, "Password");
            this.context().send(new WebMeta().put("type", "Forget"), false);

            return fd;
        });
        if (Code.equals("Reset")) {
            ;
            if (this.forget(username)) {
                this.prompt("验证码已经发送，请注意查收", false);
                this.context().send(new WebMeta().event("VerifyCode", this.asyncDialog("UI", "none"), new WebMeta().put("text", "验证码已发送")), true);
            } else {
                switch (ac.Type) {
                    case Membership.EMAIL_ACCOUNT_KEY:
                        this.prompt("没有找到此邮箱绑定账户");
                        break;
                    default:
                        this.prompt("没有找到此手机号绑定账户");
                        break;
                }
            }
        }

        Map map = Utility.isNull((Map) JSON.deserialize(acct.ConfigData), new HashMap());


        String VerifyCode = (String) map.get("VerifyCode");


        if (Code.equalsIgnoreCase(VerifyCode)) {
            WebMeta print = new WebMeta();
            print.put("AccountType", acct.Type);
            print.put("VerifyCode", Code);
            print.put("user_id", acct.user_id);

            this.context().send(new WebMeta().put("type", "Forget"), false);
            response.redirect(request.model(), "Password", print, true);
        } else {
            this.prompt("您输入的验证码错误");
        }


    }
}
