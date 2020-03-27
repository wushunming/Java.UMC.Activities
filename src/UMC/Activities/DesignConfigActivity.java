package UMC.Activities;

import UMC.Activities.Entities.Design_Config;
import UMC.Data.Database;
import UMC.Data.Sql.IObjectEntity;
import UMC.Web.*;

import java.util.UUID;

public class DesignConfigActivity extends WebActivity {

    public void processActivity(WebRequest request, WebResponse response) {
        String group = request.cmd();

        IObjectEntity<Design_Config> entity = Database.instance().objectEntity(Design_Config.class);
        entity.order().asc(new Design_Config().Sequence(0));


        UUID vid = UMC.Data.Utility.uuid(this.asyncDialog("Id", s ->
        {
            entity.where().and().equal(new Design_Config().GroupBy(group));

            UIGridDialog rdoDig = UIGridDialog.create(new UIGridDialog.Header("Id", 0)
                            .put("Name", "标题").put("Value", "代码")
                    , true, entity.query());
            rdoDig.menu("新建配置", request.model(), request.cmd(), "News");
            rdoDig.refreshEvent("Settings");
            rdoDig.setPage(true);// = true;
            rdoDig.title("数据配置");


            return rdoDig;
        }));// ??Guid.Empty;

        WebMeta configs = this.asyncDialog(s ->
        {
            UIFormDialog fm = new UIFormDialog();
            if (vid == null) {
                fm.title("新增配置值");
            } else {
                fm.title("修改配置值");
            }
            entity.where().and().equal(new Design_Config().Id(vid));

            Design_Config con = null;
            if (vid != null) {
                con = entity.single();
            }
            if (con == null) {
                entity.where().reset().and().equal(new Design_Config().GroupBy(group));
                con = entity.max(new Design_Config().Sequence(0));
            }

            fm.addText("配置名称", "Name", con.Name);
            fm.addText("配置标题", "Value", con.Value);
            fm.addNumber("显示顺序", "Sequence", con.Sequence);
            return fm;
        }, "Config");
        Design_Config cv = new Design_Config();
        UMC.Data.Utility.setField(cv, configs.map());
        if (vid == null) {
            cv.GroupBy = group;
            cv.Id = UUID.randomUUID();
            entity.insert(cv);
        } else {
            cv.Id = vid;
            entity.where().reset().and().equal(new Design_Config().Id(vid));

            if (cv.Sequence == -1) {
                entity.delete();
            } else {
                entity.update(cv);
            }
        }
        this.context().send("Settings", true);
    }
}
