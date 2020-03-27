package UMC.Activities;

import UMC.Data.Sql.IObjectEntity;
import UMC.Data.Utility;
import UMC.Security.Identity;
import UMC.Web.*;

import java.net.URI;
import java.util.Date;
import java.util.UUID;

public class DesignImageActivity extends WebActivity {


    public void processActivity(WebRequest request, WebResponse response) {
        Identity user = UMC.Security.Identity.current();
        UUID groupId = UMC.Data.Utility.uuid(this.asyncDialog("id", d ->
        {
            this.prompt("请传入参数");
            return this.dialogValue(user.id().toString());
        }), true);

        String Seq = this.asyncDialog("seq", g ->
        {
            if (request.sendValues() != null) {

                return this.dialogValue(Utility.isNull(request.sendValues().get("Seq"), "0"));
            } else {
                return this.dialogValue("0");
            }
        });
        UMC.Data.WebResource webr = UMC.Data.WebResource.Instance();
        String media_id = this.asyncDialog("media_id", g ->
        {
            if (request.isApp()) {
                UIDialog f = UIDialog.createDialog("File");
                f.config("Submit", new UIClick(new WebMeta(request.arguments().map()).put(g, "Value"))
                        .send(request.model(), request.cmd()));
                ;
                return f;

            } else {

                UIFormDialog from = new UIFormDialog();
                from.title("图片上传");

                from.addFile("选择图片", "media_id", webr.ImageResolve(groupId, Seq, 4));

                from.submit("确认上传", request, "image");
                return from;
            }
        });


        String type = this.asyncDialog("type", g -> this.dialogValue("jpg"));
        int seq = UMC.Data.Utility.parse(Seq, 1);
        if (media_id.startsWith("http://") || media_id.startsWith("https://")) {
            URI url = URI.create(media_id);

            if (url.getPath().toLowerCase().endsWith(type.toLowerCase())) {
                webr.Transfer(url, groupId, seq, type);
            } else {

                webr.Transfer(URI.create(String.format("%s?x-oss-process=image/format,%s", media_id, type)), groupId, seq, type);
            }


        } else {
            /*
             * 微信上传
             * */
        }


        this.context().send(new WebMeta().put("type", "image").put("id", groupId), true);


    }

}

