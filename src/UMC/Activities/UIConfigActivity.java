package UMC.Activities;

import UMC.Data.WebResource;
import UMC.Web.*;

public class UIConfigActivity extends WebActivity {
    @Override
    public void processActivity(WebRequest request, WebResponse response) {

        WebMeta config = new WebMeta();
        config.put("src", WebResource.Instance().ResolveUrl(WebResource.ImageResource));
        response.redirect(config);

    }
}
