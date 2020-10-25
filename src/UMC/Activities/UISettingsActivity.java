package UMC.Activities;

import UMC.Web.*;

public class UISettingsActivity extends WebActivity {
    @Override
    public void processActivity(WebRequest request, WebResponse response) {

        UMC.Security.Identity user = UMC.Security.Identity.current();


        UIHeader header = new UIHeader();
        UITitle title = new UITitle();
        title.title("设置");
        UISection ui = UISection.create(title);
        if (user.isAuthenticated()) {
            if (request.isCashier()) {
                ui.newSection().putCell("账户密码", new UIClick().send("Account", "Password"));
            } else {
                ui.newSection().putCell("我的账户", new UIClick().send("Account", "Self"));

            }
        }
        ui.newSection()
                .putCell("清空缓存", "", new UIClick().key("ClearCache"))
                .putCell("检查更新", "", new UIClick("Version").send("UI", "App"))//;//{ Model = "UI", Command = "App" })
                .putCell("关于作者", "", UIClick.pager("Subject", "UIData", new UMC.Web.WebMeta().put("Id", "365lu/help/AboutUs"), true));


        if (user.isAuthenticated()) {
            UICell cell = UICell.create("UI", new UMC.Web.WebMeta().put("text", "退出登录").put("Icon", '\uf011').put("click", new UIClick()
                    .send("Account", "Close")));
            cell.style().name("text", new UIStyle().color(0xf00));
            ui.newSection().newSection()
                    .put(cell);
        }
        response.redirect(ui);

    }
}
