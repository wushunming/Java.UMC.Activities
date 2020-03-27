package UMC.Activities;

import UMC.Activities.Entities.Design_Item;
import UMC.Data.Database;
import UMC.Data.Sql.IObjectEntity;
import UMC.Data.Utility;
import UMC.Web.*;

import java.net.URI;
import java.util.Map;
import java.util.UUID;


public class DesignClickActivity extends WebActivity {


    protected UIClick Click(UIClick ui) {
        String type = this.asyncDialog("Click", g ->
        {
            UIRadioDialog di = new UIRadioDialog();
            di.title("关联功能");
            ListItemCollection listItemCollection = di.options();//new ListItemCollection();
            listItemCollection.add("连接扫一扫", "Scanning");
            listItemCollection.add("连接指令", "Setting");
            listItemCollection.add("连接拨号", "Tel");
            listItemCollection.add("连接网址", "Url");

            return di;
        });
        switch (type) {
            case "Scanning":
                return UIClick.scanning();
            case "Tel":
                return UIClick.tel(this.asyncDialog("Tel", g ->
                {
                    UITextDialog di = new UITextDialog();
                    di.title("拨号号码");
                    return di;
                }));
            case "Url":
                return UIClick.url(URI.create(this.asyncDialog("Url", g ->
                {
                    UITextDialog di = new UITextDialog();
                    di.title("网址地址");
                    return di;
                })));


            default:
            case "Setting":

                Map c = UMC.Data.JSON.deserialize(UMC.Data.JSON.serialize(ui), Map.class);
                WebMeta settings = this.asyncDialog(g ->
                {
                    UIFormDialog di = new UIFormDialog();
                    di.title("功能指令");
                    di.addText("模块指令", "Model", (String) c.get("model"));
                    di.addText("模块指令", "Command", (String) c.get("cmd"));
                    di.addPrompt("此块内容为专业内容，请由工程师设置");

                    if (c.containsKey("send")) {
                        Object send = c.get("send");
                        if (send instanceof String) {
                            di.addText("参数", "Send", (String) send).placeholder("如果没参数，则用none");
                        } else {

                            di.addText("参数", "Send", UMC.Data.JSON.serialize(send)).placeholder("如果没参数，则用none");
                        }
                    } else {

                        di.addText("参数", "Send", "none").placeholder("如果没参数，则用none");
                    }

                    return di;
                }, "Send");
                UIClick click = new UIClick();
                String Model = settings.get("Model");
                String Command = settings.get("Command");
                String Send = settings.get("Send");
                click.send(Model, Command);

                if ("none".equalsIgnoreCase(Send) == false) {
                    if (Send.startsWith("{")) {
                        click.send(UMC.Data.JSON.deserialize(Send, WebMeta.class));
                    } else {
                        click.send(Send);
                    }
                }
                return click;
        }

    }

    @Override
    public void processActivity(WebRequest webRequest, WebResponse webResponse) {

        String ssid = this.asyncDialog("Id", "请输入ID");
        UUID sId = UMC.Data.Utility.uuid(ssid, true);


        IObjectEntity<Design_Item> entity = Database.instance().objectEntity(Design_Item.class);

        entity.where().and().equal(new Design_Item().Id(sId));


        Design_Item baner = entity.single();

        UIClick c = Utility.isNull(UMC.Data.JSON.deserialize(baner.Click, UIClick.class), new UIClick());


        entity.update(new Design_Item().Click(UMC.Data.JSON.serialize(this.Click(c))));
        this.context().send(new UMC.Web.WebMeta().put("type", "Click"), false);
        this.prompt("关联成功");
    }
}