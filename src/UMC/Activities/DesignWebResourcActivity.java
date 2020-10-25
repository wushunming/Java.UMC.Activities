package UMC.Activities;

import UMC.Data.WebResource;
import UMC.Security.Identity;
import UMC.Web.*;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.UUID;


public class DesignWebResourcActivity extends WebActivity {
    @Override
    public void processActivity(WebRequest request, WebResponse response) {
        WebResource oosr = WebResource.Instance();//as OssResource;
        String Key = this.asyncDialog("Key", g ->
        {
            UIDialog f = UIDialog.createDialog("File");
            f.config("Submit", new UIClick(new WebMeta().put("Key", "WebResource", "media_id", "Value", "UI", this.asyncDialog("UI", "none")))

                    .send(request.model(), request.cmd()));

            return f;
        });
        Identity user = Identity.current();
        String media_id = this.asyncDialog("media_id", "none");
        if (media_id.equals("none") == false) {
            URL url = null;
            try {
                url = new URL(media_id);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            String name = url.getPath().substring(url.getPath().lastIndexOf('/') + 1);
            String urlKey = String.format("UserResources/%s/%s", UMC.Data.Utility.uuid(UUID.randomUUID()), name);

            oosr.Transfer(url, urlKey);

            URL url1 = null;

            WebMeta posmata = new WebMeta();
            try {
                url1 = new URL(request.uri(), oosr.WebDomain() + urlKey);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            posmata.put("src", url1.toString()).put("name", name);
            this.context().send(new WebMeta().event(Key, this.asyncDialog("UI", "none"), posmata), true);
        } else {
            String UseKey = UMC.Data.Utility.parseEncode(UMC.Security.Identity.current().id().hashCode(), 36);
            //   var sourceKey = new Uri();

            URL sourceKey = null;
            try {
                sourceKey = new URL(String.format("%s/TEMP/%s/%s", oosr.TempDomain(), UMC.Data.Utility.getRoot(request.uri()), Key));

            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            Key = String.format("UserResources/%s/%s", UseKey, Key);


            oosr.Transfer(sourceKey, Key);


            response.redirect(new WebMeta().put("src", oosr.WebDomain() + Key));
        }
    }
}
