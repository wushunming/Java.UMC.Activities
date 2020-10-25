package UMC.Activities;

import UMC.Web.Mapping;
import UMC.Web.UIClick;
import UMC.Web.WebRequest;
import UMC.Web.WebResponse;

//@Mapping(model = "Design", cmd = "Link")
public class DesignLinkActivity extends DesignClickActivity {
    @Override
    public void processActivity(WebRequest webRequest, WebResponse webResponse) {

        String Key = this.asyncDialog("Key", "none");
        String UI = this.asyncDialog("UI", "none");
        UIClick click = this.Click(new UIClick());
        this.context().send(new UMC.Web.WebMeta().put("type", "Click"), false);
        this.context().send(new UMC.Web.WebMeta().event(Key, UI, click), true);

    }
}
