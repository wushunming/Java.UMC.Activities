package UMC.Activities;

import UMC.Web.*;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.UUID;

//@Mapping(model = "Design", cmd = "Image")
public class DesignImageActivity extends DesignClickActivity {
    @Override
    public void processActivity(WebRequest request, WebResponse response) {


        String UI = this.asyncDialog("UI", "none");
        String section = this.asyncDialog("section", "-1");
        String row = this.asyncDialog("row", "-1");

        String Type = this.asyncDialog("Type", g ->
        {
            UISheetDialog shett = new UISheetDialog();
            shett.title("图片操作");
            shett.options().add(new UIClick(new UMC.Web.WebMeta(request.arguments()).put("Type", "Click")).text("点击连接").send(request.model(), request.cmd()));

            shett.options().add(new UIClick(new UMC.Web.WebMeta(request.arguments()).put("Type", "Reset")).text("更换图片").send(request.model(), request.cmd()));
            shett.options().add(new UIClick(new UMC.Web.WebMeta(request.arguments()).put("Type", "Del")).text("移除图片").send(request.model(), request.cmd()));
            return shett;
        });
        switch (Type) {
            case "Reset":

                String media_id = this.asyncDialog("media_id", m ->
                {
                    UIDialog f = UIDialog.createDialog("File");
                    f.config("Submit", new UIClick(new UMC.Web.WebMeta(request.arguments()).put("media_id", "Value")).send(request.model(), request.cmd()));
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

                webr.Transfer(url, urlKey);
                UMC.Web.WebMeta posmata = new UMC.Web.WebMeta();
                posmata.put("src", domain + urlKey);
                UMC.Web.WebMeta vale = new UMC.Web.WebMeta().put("section", section).put("row", row).put("method", "VALUE").put("reloadSinle", true).put("value", posmata);
                this.context().send(new UMC.Web.WebMeta().event("UI.Edit", UI, vale), true);
                break;
            case "Del":
                UMC.Web.WebMeta dvale = new UMC.Web.WebMeta().put("section", section).put("row", row).put("method", "DEL").put("reloadSinle", true).put("value", new UMC.Web.WebMeta());
                this.context().send(new UMC.Web.WebMeta().event("UI.Edit", UI, dvale), true);
                break;
            case "Click":
                UIClick click = this.Click(new UIClick());
                UMC.Web.WebMeta posmata2 = new UMC.Web.WebMeta();
                posmata2.put("click", click);
                this.prompt("图片点击设置成功", false);
                this.context().send("Click", false);
                this.context().send(new UMC.Web.WebMeta().event("UI.Edit", UI, new UMC.Web.WebMeta().put("section", section).put("row", row).put("method", "VALUE")
                        .put("value", posmata2)), true);
                break;
        }


    }
}
