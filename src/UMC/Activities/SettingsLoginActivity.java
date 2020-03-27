package UMC.Activities;


import UMC.Data.Utility;
import UMC.Data.WebResource;
import UMC.Security.AccessToken;
import UMC.Security.Identity;
import UMC.Security.Membership;
import UMC.Web.*;

/*
 * 后台登录，进入管理后台
 *
 * */

@Mapping(model = "Settings", cmd = "Login", auth = WebAuthType.all, desc = "后台登录")
public class SettingsLoginActivity extends WebActivity {

    public void processActivity(WebRequest request, WebResponse response) {

        WebMeta user = this.asyncDialog(d ->
        {

            WebMeta sendValue = Utility.isNull(request.sendValues(), request.arguments());
            if (sendValue.size() > 0) {
                return this.dialogValue(sendValue);
            }
            UIFormDialog dialog = new UIFormDialog();
            dialog.title("账户登录");
            dialog.addText("账户", "Username", "");
            dialog.addPassword("账户密码", "Password", "");
            dialog.submit("确认登录", request, "Cashier");
            return dialog;

        }, "Login");
        String username = user.get("Username");
        String Password = user.get("Password");

        if (Utility.isEmpty(username) || Utility.isEmpty(Password)) {
            this.prompt("请输入用户名和密码");
        }


        int maxTimes = 5;
        Membership userManager = Membership.Instance();
        int times = userManager.Password(username, Password, maxTimes);

        switch (times) {
            case 0:
                String client = "POSClient";
                if (request.isApp()) {
                    client = "Mobile";
                }
                Identity iden = userManager.Identity(username);

                if (iden.isInRole(UMC.Security.Membership.UserRole) == false) {
                    this.prompt("您不是门店内部人员，不能从此登录。");
                }
                AccessToken.login(iden, AccessToken.token(), client, true);
                this.context().reset();

                this.prompt("登录成功", false);

                this.context().send("Cashier", new WebMeta().put("Src", WebResource.Instance().ImageResolve(iden.id(), "1", 5), "Alias", iden.alias()), true);
                break;
            case -4:
                this.prompt("您的账户已经禁用");
                break;
            case -3:
                this.prompt("无此子账户");
                break;
            case -2:
                this.prompt("您的用户已经锁定，请您联系管理员解锁");
                break;
            case -1:
                this.prompt("您的用户不存在，请确定用户名", false);
                break;
            default:
                this.prompt(String.format("您的用户和密码不正确，您还有%d次机会", maxTimes - times), false);


                break;
        }


    }
}