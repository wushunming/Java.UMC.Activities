package UMC.Activities;


import UMC.Data.Configuration;
import UMC.Data.Database;
import UMC.Data.Entities.Account;
import UMC.Data.Sql.IObjectEntity;
import UMC.Data.Utility;
import UMC.Net.Message;
import UMC.Security.Identity;
import UMC.Security.Membership;
import UMC.Web.*;
import com.sun.xml.internal.ws.api.model.MEP;

import java.util.Date;
import java.util.Hashtable;
import java.util.Map;

class AccountMobileActivity extends WebActivity {

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


        Message.instance().send("Verify", hask, mobile);
    }


    void Remove() {
        Identity user = UMC.Security.Identity.current();

        IObjectEntity<Account> accountEntity = Database.instance().objectEntity(Account.class);

        Account[] accounts = accountEntity.where().and().equal(new Account().User_id(user.id())).entities().query();

        Account a = Utility.find(accounts, c -> c.Flags == Membership.MOBILE_ACCOUNT_KEY);

        String code = this.asyncDialog("Remove", d ->
        {

            UIFormDialog fm = new UIFormDialog();
            fm.title("解除验证");
            fm.addTextValue().put("手机号码", a.Name);
            fm.addVerify("验证码", "Code", "您收到的验证码")
                    .command("Account", "Mobile", new UMC.Web.WebMeta().put("Mobile", a.Name).put("Code", "Send"))
                    .put("Start", "YES");

            fm.submit("确认验证码", this.context().request(), "Mobile");
            return fm;
        });
        Configuration<Map> session = new Configuration<Map>(a.Name, Map.class);

        if (session.Value != null) {
            if (code.equalsIgnoreCase((String) session.Value.get("Code"))) {


                accountEntity.where().reset().and().equal(new Account().User_id(user.id()).Name(code))
                        .entities().update(new Account().User_id(user.id()).Type(Membership.MOBILE_ACCOUNT_KEY)
                        .Flags(Membership.UserFlagsUnVerification));


                this.prompt("手机解除绑定成功", false);
                this.context().send("Mobile", true);
            }
        }
        this.prompt("您输入的验证码错误");
    }

    public void processActivity(WebRequest request, WebResponse response) {

        Identity user = UMC.Security.Identity.current();

        if (user.isAuthenticated() == false) {
            this.prompt("请登录");
        }

        IObjectEntity<Account> accountEntity = Database.instance().objectEntity(Account.class);

        Account[] accounts = accountEntity.where().and().equal(new Account().User_id(user.id())).entities().query();

        Account acc = Utility.find(accounts, c -> c.Flags == Membership.MOBILE_ACCOUNT_KEY);


        String value = this.asyncDialog("Mobile", d ->
        {
            if (acc != null && (acc.Flags & Membership.UserFlagsUnVerification) != Membership.UserFlagsUnVerification) {
                return new UIConfirmDialog("您确认解除与手机的绑定吗", "Change")
                        .title("解除确认");
            }
            UIFormDialog fm = new UIFormDialog();
            fm.title("手机绑定");

            fm.addText("手机号码", "Mobile", acc != null ? acc.Name : "");

            fm.submit("下一步", request, "Mobile");
            return fm;

        });
        switch (value) {
            case "Change":

                Remove();
                return;
        }
        if (Utility.IsPhone(value) == false) {
            this.prompt("手机号码格式不正确");
        }
        accountEntity.where().reset().and().equal(new Account().Name(value).Type(Membership.MOBILE_ACCOUNT_KEY))
                .and().unEqual(new Account().User_id(user.id()));


        if (accountEntity.count() > 0) {
            this.prompt("此手机号码已被其他账号绑定");

        }


        String Code = this.asyncDialog("Code", g ->
        {
            UIFormDialog fm = new UIFormDialog();
            fm.title("手机号码验证");

            fm.addTextValue().put("手机号码", value);
            fm.addVerify("验证码", "Code", "您收到的验证码")
                    .command("Account", "Mobile", new WebMeta().put("Mobile", value).put("Code", "Send"))
                    .put("Start", "YES");

            fm.submit("确认验证码", request, "Mobile");
            return fm;
        });
        if (Code.equalsIgnoreCase("send")) {
            this.SendMobileCode(value);
            this.prompt("验证码已发送", false);
            this.context().send(new WebMeta().event("VerifyCode", this.asyncDialog("UI", "none"), new UMC.Web.WebMeta("Time", "100")), true);

        }
        Configuration<Map> session = new Configuration<Map>(Code, Map.class);
        if (session.Value != null) {
            String code = (String) session.Value.get("Code");
            if (Code.equalsIgnoreCase(code)) {

                accountEntity.where().reset().and().equal(new Account().User_id(user.id()).Name(code))
                        .entities().update(new Account().User_id(user.id())
                        .Type(Membership.MOBILE_ACCOUNT_KEY).Flags(Membership.UserFlagsNormal));

                this.prompt("手机号码绑定成功", false);
                this.context().send("Mobile", true);
            }
        }


        this.prompt("您输入的验证码错误");


    }

}