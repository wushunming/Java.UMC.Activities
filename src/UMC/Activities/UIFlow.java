package UMC.Activities;

import UMC.Web.Mapping;
import UMC.Web.WebActivity;
import UMC.Web.WebAuthType;
import UMC.Web.WebFlow;


@Mapping(model = "UI", auth = WebAuthType.all,desc = "UI页面")
public class UIFlow extends WebFlow {
    @Override
    public WebActivity firstActivity() {
        switch (this.context().request().cmd()) {
            case "App":
                return  new UIAppActivity();
            case "Home":
                return new DesignUIActivity(false);
        }

        return WebActivity.Empty;
    }
}