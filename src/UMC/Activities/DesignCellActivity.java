package UMC.Activities;

import UMC.Web.*;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.UUID;


public class DesignCellActivity extends WebActivity {
    WebMeta Text() {
        UMC.Web.WebMeta data = new UMC.Web.WebMeta().put("text", "插入文字");
        UICell cell = UICell.create("CMSText", data);
        return new UMC.Web.WebMeta().cell(cell);

    }

    WebMeta Image(WebRequest request) {
        String media_id = this.asyncDialog("media_id", m ->
        {
            UIDialog f = UIDialog.createDialog("File");
            f.config("Submit", new UIClick(new UMC.Web.WebMeta(request.arguments()).put("media_id", "Value"))
                    .send(request.model(), request.cmd()));
            ;
            return f;
        });
        URL url = null;
        try {
            url = new URL(media_id);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        String urlKey = String.format("UserResources/%s/%s", UMC.Data.Utility.uuid(UUID.randomUUID()), url.getPath().substring(url.getPath().lastIndexOf('/')));
        UMC.Data.WebResource webr = UMC.Data.WebResource.Instance();

        String domain = webr.WebDomain();
        WebMeta posmata = new UMC.Web.WebMeta();
        UMC.Web.UICell cell = UMC.Web.UICell.create("CMSImage", posmata);

        posmata.put("src", domain + urlKey);

        webr.Transfer(url, urlKey);
        cell.style().padding(10);
        return new UMC.Web.WebMeta().cell(cell);

    }

    @Override
    public void processActivity(WebRequest request, WebResponse webResponse) {
        String Key = this.asyncDialog("Key", "WebResource");
        String UI = this.asyncDialog("UI", "none");
        String Type = this.asyncDialog("Type", gKey ->
        {
            UISheetDialog seett = new UISheetDialog();
            seett.title("插入");
            UIClick click = new UIClick("UI", UI, "Key", "WebResource", "Type", "Image")
                    .send(request.model(), request.cmd()).text("插入图片");

            seett.options().add(click);
            click = new UIClick("UI", UI, "Key", "WebResource", "Type", "Text")
                    .send(request.model(), request.cmd()).text("插入文字");


            seett.options().add(click);
            return seett;

        });
        switch (Type) {
            case "Image":

                this.context().send(new UMC.Web.WebMeta().event(Key, UI, Image(request)), true);
                break;
            case "Text":
                this.context().send(new UMC.Web.WebMeta().event(Key, UI, Text()), true);
                break;
        }

    }
}
