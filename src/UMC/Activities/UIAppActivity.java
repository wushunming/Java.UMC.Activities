package UMC.Activities;

import UMC.Data.JSON;
import UMC.Data.Utility;
import UMC.Web.*;
import com.sun.jndi.toolkit.url.Uri;

import java.awt.*;
import java.io.File;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.List;

public class UIAppActivity extends WebActivity {
    @Override
    public void processActivity(WebRequest webRequest, WebResponse webResponse) {


        String config = this.asyncDialog("Key", g -> this.dialogValue("none"));
        String file = Utility.mapPath("~/App_Data/app.json");
        if (new File(file).exists() == false) {
            String strAPP = Utility.httpString("http://oss.365lu.cn/UserResources/demo.json");

            Utility.writer(file, strAPP, false);

        }
        Map appConfig = (Map) JSON.deserialize(Utility.reader(file));

        switch (config) {
            case "Builder":
                if (Utility.isEmpty((String) appConfig.get("AppName"))) {
                    this.prompt("应用名称不能为空");
                }
                if (Utility.isEmpty((String) appConfig.get("IconSrc"))) {
                    this.prompt("请上传图标");
                }
                if (Utility.isEmpty((String) appConfig.get("BgSrc"))) {
                    this.prompt("请上传启动图");
                }


                Map<String, String> dataKey = new HashMap<>();
                dataKey.put("root", webRequest.uri().getPath().split("/")[1]);

                String urlString = webRequest.uri().toString();
                dataKey.put("host", urlString.substring(0, urlString.indexOf('/', 8) - 1));

                appConfig.put("DataKey", dataKey);
                webResponse.redirect(JSON.serialize(appConfig));
                break;
            case "json":
                webResponse.redirect(appConfig);
                break;
            case "Reset":
                String ResetName = this.asyncDialog("Reset", g ->
                {
                    UISelectDialog k = new UISelectDialog();

                    k.title("选择参考的默认的界面架构");

                    k.options().put("DOME架构", "demo");
                    return k;
                });


                String strAPP = Utility.httpString(String.format("http://oss.365lu.cn/UserResources/%s.json", ResetName));

                Map appConfig2 = (Map) JSON.deserialize(strAPP);
                appConfig2.put("BgSrc", appConfig.get("BgSrc"));
                appConfig2.put("IconSrc", appConfig.get("IconSrc"));
                appConfig2.put("AppName", appConfig.get("AppName"));
                appConfig = appConfig2;

                break;
            case "News": {

                WebMeta key = this.asyncDialog(g ->
                {
                    UIFormDialog k = new UIFormDialog();


                    k.title("新增Bar");

                    k.addText("标题", "text", "");
                    k.addOption("图标", "icon", "", "").command("System", "Icon");
                    k.addCheckBox("", "max", "no").put("显示大按钮", "true");
                    k.addRadio("Bar加载类型", "Type")
                            .put("默认主页", "Home").put("电商购物篮", "Cart").put("电商品类页", "Category")
                            .put("Tabs", "Tab配置页").put("点击项", "Click")
                            .put("基本页", "Pager");
                    k.submit("确认提交", webRequest, "AppConfig");
                    return k;
                }, "Sheet");


                WebMeta data = new WebMeta().put("key", key.get("Type")).put("text", key.get("text")).put("icon", key.put("icon"));
                String max = key.get("max");
                if (Utility.isEmpty(max) == false && max.contains("true")) {
                    data.put("max", true);
                }

                List<Object> footbar = Arrays.asList((Object[]) appConfig.get("footBar"));

                footbar.add(data);
                appConfig.put("footBar", footbar.toArray());
            }
            break;
            case "BgSrc":
            case "IconSrc": {

                String AppName = this.asyncDialog("Value", g ->
                {
                    UITextDialog k = new UITextDialog();
                    k.title("值");


                    return k;
                });
                appConfig.put(config, AppName);
//                appConfig[config] = AppName;
            }
            break;
            case "AppName": {
                Map finalAppConfig = appConfig;
                String AppName = this.asyncDialog("AppName", g ->
                {
                    UITextDialog k = new UITextDialog();
                    k.title("应用名称").value((String) finalAppConfig.get("AppName"));
                    return k;

                });
                appConfig.put("AppName", AppName);
            }
            break;
            case "Del": {

                List<Object> footbar = Arrays.asList((Object[]) appConfig.get("footBar"));


                int index = Utility.parse(this.asyncDialog("Index", "0"), -1);
                footbar.remove(index);
                appConfig.put("footBar", footbar.toArray());
            }
            break;
            default: {
                List<Object> footbar = Arrays.asList((Object[]) appConfig.get("footBar"));//.add()
                int index = Utility.parse(this.asyncDialog("Index", "0"), -1);

                Map hash = (Map) footbar.get(index);// as Hashtable;
                WebMeta settings = this.asyncDialog(g ->
                {
                    UIFormDialog fm = new UIFormDialog();
                    fm.title("图标");

                    fm.addText("标题", "text", (String) hash.get("text"));
                    fm.addOption("图标", "icon", (String) hash.get("icon"), (String) hash.get("icon")).command("System", "Icon");
                    switch (config) {
                        case "Setting":
                            switch ((String) hash.get("key")) {
                                case "Home":
                                case "Category":
                                    break;
                                case "Cart":
                                    fm.addCheckBox("", "max", "no").put("显示大按钮", "true", hash.containsKey("max"));
                                    break;
                                case "Click":
                                    fm.addCheckBox("", "max", "no").put("显示大按钮", "true", hash.containsKey("max"));
                                    Map click = Utility.isNull((Map) hash.get("click"), new HashMap<>());//??new Ma();
                                    fm.addText("事件模块", "model", (String) click.get("model"));
                                    fm.addText("事件指令", "cmd", (String) click.get("cmd"));
                                    fm.addText("事件参数", "send", (String) click.get("send")).notRequired();
                                    break;
                                case "Tabs":
                                    fm.addText("数据源模块", "model", (String) hash.get("model"));
                                    fm.addText("数据源指令", "cmd", (String) hash.get("cmd"));
                                    break;
                                case "Pager":
                                    fm.addText("加载模块", "model", (String) hash.get("model"));
                                    fm.addText("加载指令", "cmd", (String) hash.get("cmd"));
                                    break;
                                default:
                                    this.prompt("固定页面，不支持设置");
                                    ;
                                    break;
                            }
                            break;

                    }
                    return fm;
                }, "Settings");
                hash.put("text", settings.get("text"));
                hash.put("icon", settings.get("icon"));

                String maxValue = settings.get("max");
                if (Utility.isEmpty(maxValue) == false && maxValue.contains("true")) {
                    hash.put("max", true);
                } else {
                    hash.remove("max");
                }
                switch ((String) hash.get("key")) {
                    case "Click":
                        WebMeta click = new WebMeta().put("model", settings.get("model"))
                                .put("cmd", settings.get("cmd"));
                        if (Utility.isEmpty(settings.get("model")) == false) {
                            click.put("send", settings.get("send"));
                        }
                        hash.put("click", click);
                        break;
                    case "Cart":
                        break;
                    case "Tabs":
                        hash.put("model", settings.get("model"));
                        hash.put("cmd", settings.get("cmd"));
                        break;
                    case "Pager":
                        hash.put("model", settings.get("model"));
                        hash.put("cmd", settings.get("cmd"));
                        break;
                }

            }
            break;
        }

        Utility.writer(file, JSON.serialize(appConfig), false);
        this.context().send("AppConfig", new WebMeta().put("Config", appConfig), true);
    }

}
