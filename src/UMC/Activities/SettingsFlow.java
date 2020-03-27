package UMC.Activities;

import UMC.Web.Mapping;
import UMC.Web.WebActivity;
import UMC.Web.WebAuthType;
import UMC.Web.WebFlow;

@Mapping(model = "Settings", auth = WebAuthType.admin, desc = "系统设置")
public class SettingsFlow extends WebFlow {
    @Override
    public WebActivity firstActivity() {
        switch (this.context().request().cmd()) {
            case "Role":
                return new SettingsRoleActivity();
            case "User":
                return new SettingsUserActivity();
            case "Auth":
                return new SettingsAuthActivity();
            case "Menu":
                return new SettingsMenuActivity();
        }

        return WebActivity.Empty;
    }
}
