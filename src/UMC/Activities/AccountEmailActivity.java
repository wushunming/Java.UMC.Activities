package UMC.Activities;

import UMC.Data.Configuration;
import UMC.Data.Database;
import UMC.Data.Entities.Account;
import UMC.Data.Sql.IObjectEntity;
import UMC.Data.Utility;
import UMC.Security.Identity;
import UMC.Security.Membership;
import UMC.Web.*;

import java.util.Hashtable;
import java.util.Map;


class AccountEmailActivity extends WebActivity {

    void SendEmail(String email) {
        Identity user = UMC.Security.Identity.current();


        Map hask = new Hashtable();

        String code = (Math.random() + "").substring(3, 9);

        hask.put("Code", code);
        hask.put("Date", System.currentTimeMillis() / 1000);


        Configuration<Map> session = new Configuration<Map>(email, Map.class);


        session.commit(hask, user);


    }

    void Remove() {
        Identity user = UMC.Security.Identity.current();
        IObjectEntity<Account> accountEntity = Database.instance().objectEntity(Account.class);

        Account[] accounts = accountEntity.where().and().equal(new Account().User_id(user.id())).entities().query();

        Account a = Utility.find(accounts, c -> c.Flags == Membership.EMAIL_ACCOUNT_KEY);

        String code = this.asyncDialog("Remove", d ->
        {

            UIFormDialog fm = new UIFormDialog();
            fm.title("解除验证");
            fm.addTextValue().put("邮箱", a.Name);
            fm.addVerify("验证码", "Code", "您邮箱收到的验证码")
                    .put("Command", "Email").put("Model", "Account").put("SendValue", new UMC.Web.WebMeta().put("Email", a.Name).put("Code", "Send")).put("Start", "YES");

            fm.submit("确认验证码", this.context().request(), "Email");
            return fm;
        });
        Configuration<Map> session = new Configuration<Map>(code, Map.class);

        if (session.Value != null) {
            if (code.equalsIgnoreCase((String) session.Value.get("Code"))) {

                Account account = new Account();
                account.user_id = user.id();
                account.Type = Membership.EMAIL_ACCOUNT_KEY;
                account.Flags = Membership.UserFlagsUnVerification;
                accountEntity.where().reset().and().equal(new Account().User_id(user.id()).Name(code))
                        .entities().update(account);
                this.prompt("邮箱解除绑定成功", false);
                this.context().send(new UMC.Web.WebMeta().put("type", "Email"), true);
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

        Account acc = Utility.find(accounts, c -> c.Flags == Membership.EMAIL_ACCOUNT_KEY);


        String value = this.asyncDialog("Email", d ->
        {
            if (acc != null && (acc.Flags & Membership.UserFlagsUnVerification) != Membership.UserFlagsUnVerification) {
                return new UIConfirmDialog("您确认解除与邮箱的绑定吗", "Change").title("解除确认");
            }


            UITextDialog t = new UITextDialog(acc != null ? acc.Name : "");
            t.title("邮箱绑定");
            t.config("submit", "下一步");
            return t;

        });
        switch (value) {
            case "Change":

                Remove();
                return;
        }

        if (Utility.IsEmail(value) == false) {
            this.prompt("邮箱格式不正确");
        }


        accountEntity.where().reset().and().equal("Name", value).and().equal("Type", Membership.EMAIL_ACCOUNT_KEY)
                .and().unEqual(new Account().User_id(user.id()));


        if (accountEntity.count() > 0) {
            this.prompt("此邮箱已存在绑定");

        }


        String Code = this.asyncDialog("Code", g ->
        {
            UIFormDialog fm = new UIFormDialog();
            fm.title("验证码");
            fm.addTextValue().put("邮箱", value);
            fm.addVerify("验证码", "Code", "您邮箱收到的验证码")
                    .put("Command", "Email").put("Model", "Account").put("SendValue", new UMC.Web.WebMeta().put("Email", value).put("Code", "Send")).put("Start", "YES");

            fm.submit("确认验证码", this.context().request(), "Email");
            return fm;
        });
        if (Code == "Send") {
            this.SendEmail(value);
            this.prompt("验证码已发送");
        }
        Configuration<Map> session = new Configuration<Map>(Code, Map.class);
        if (session.Value != null) {
            String code = (String) session.Value.get("Code");// as String;
            if (Code.equalsIgnoreCase(code)) {
                Account account = new Account();
                account.user_id = user.id();
                account.Type = Membership.EMAIL_ACCOUNT_KEY;
                account.Flags = Membership.UserFlagsNormal;
                accountEntity.where().reset().and().equal(new Account().User_id(user.id()).Name(code))
                        .entities().update(account);

                this.prompt("邮箱绑定成功", false);
                this.context().send(new UMC.Web.WebMeta().put("type", "Email"), true);
            }
        }


        this.prompt("您输入的验证码错误");


    }

}