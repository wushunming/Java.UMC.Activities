package UMC.Activities;

import UMC.Data.Utility;
import UMC.Security.AccessToken;
import UMC.Security.Identity;
import UMC.Web.*;

class AccountCheckActivity extends WebActivity {
    @Override
    public void processActivity(WebRequest request, WebResponse response) {
        Identity user = UMC.Security.Identity.current();
        switch (Utility.isNull(request.sendValue(), "")) {
            case "Info": {
                WebMeta info = new WebMeta();
                if (user.isAuthenticated()) {
                    info.put("Alias", user.alias());
                    info.put("Src", UMC.Data.WebResource.Instance().ResolveUrl(user.id(), "1", 4));// user.Alias;
                }
                info.put("IsCashier", request.isCashier());
                info.put("TimeSpan", System.currentTimeMillis() / 1000);

                info.put("Device", UMC.Data.Utility.uuid(UMC.Security.AccessToken.token()));

                String ContentType = AccessToken.current().ContentType;
                if (Utility.isEmpty(ContentType) == false) {
                    if (ContentType.contains("WeiXin")) {
                        info.put("IsWeiXin", true);
                    } else if (ContentType.contains("Client")) {

                        info.put("IsClient", true);
                    } else if (ContentType.contains("Corp")) {

                        info.put("IsCorp", true);
                    }
                }
                response.redirect(info);
            }
            break;
            case "User":
                if (request.isCashier() == false) {

                    response.redirect("Settings", "Login");
                }
                break;
            case "Client":
                if (user.isAuthenticated()) {

                    response.redirect("Account", "Login");
                } else {

                    response.redirect("Account", "Self");
                }
                break;
            case "Cashier":
                if (request.isCashier() == false) {

                    response.redirect("Settings", "Login");
                } else {

                    response.redirect("Account", "Self");
                }
                break;
            case "Event":
                if (user.isAuthenticated() == false) {

                    this.context().send("Login", true);
                } else {

                    response.redirect("Account", "Login");
                }
                break;
        }

        this.context().send(new UMC.Web.WebMeta().event("Login", this.asyncDialog("UI", "none"), new UMC.Web.WebMeta().put("icon", "\uE91c", "format", "{icon}").put("Alias", user.alias())
                .put("click", new UIClick().send("Account", "Self")).put("style", new UIStyle().name("icon", new UIStyle().font("wdk")))), true);

    }
}
