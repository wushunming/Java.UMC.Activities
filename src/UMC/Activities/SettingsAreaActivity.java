package UMC.Activities;

import UMC.Data.Database;
import UMC.Data.Entities.Location;
import UMC.Data.Sql.IObjectEntity;
import UMC.Data.Utility;
import UMC.Web.*;


@Mapping(model = "Settings", cmd = "Area", auth = WebAuthType.all, desc = "选择地址区域")
public class SettingsAreaActivity extends WebActivity {
    final int Nation = 1,
    Province = 2,
    City = 3,
    Region = 4;

    public void processActivity(WebRequest request, WebResponse response) {
        String key = this.asyncDialog("Key", g -> this.dialogValue("Select"));

        int Type = Utility.parse(this.asyncDialog("Type", g -> this.dialogValue("2")), 2);

        int Parent = Utility.parse(this.asyncDialog("Parent", g -> this.dialogValue("0")), 0);

        IObjectEntity<Location> entity = Database.instance().objectEntity(Location.class);// > (); ;
        int ValueId = Utility.parse(this.asyncDialog("Value", d ->
        {
            WebMeta meta = Utility.isNull(request.sendValues(), new WebMeta());
            if (meta.containsKey("start") == false) {
                UISectionBuilder buider = new UISectionBuilder(request.model(), request.cmd(), request.arguments());
                buider.closeEvent("UI.Event");
                this.context().send(buider.builder(), true);
            }
            WebMeta send = new UMC.Web.WebMeta(request.arguments().map());
            UITitle uITItle = new UITitle();

            switch (Type) {
                case Nation:
                    uITItle.title("选择国家");
                    break;
                case City:
                    uITItle.title("选择城市");
                    break;
                case Province:
                    uITItle.title("选择省份");
                    break;
                case Region:
                    uITItle.title("选择区县");
                    break;
            }

            UISection sestion = UISection.create(uITItle);
            UISection ui = sestion;
            if (Parent > 0) {


                Location cCode = entity.where().and().equal(new Location().Id(Parent)).entities().single();

                String title = "返回省份";

                switch (Type) {
                    case Region:
                        title = "返回城市";
                        break;
                }

                sestion.putCell('\uf112', title, cCode.Name, UIClick.query(new WebMeta().put("Parent", cCode.ParentId).put("Type", cCode.Type)));
                ui = sestion.newSection();
            }
            entity.where().reset().and().equal(new Location().Id(Parent).Type(Type));//.entities().single();
            final UISection ui2 = ui;

            entity.query(dr ->
            {
                switch (dr.Type) {
                    case Region:
                        ui2.putCell(dr.Name, new UIClick(new WebMeta(request.arguments()).put(d, dr.Id)).send(request.model(), request.cmd()));
                        break;
                    default:
                        ui2.putCell(dr.Name, UIClick.query(new WebMeta().put("Type", dr.Type + 1).put("Parent", dr.Id)));
                        break;

                }


            });
            response.redirect(sestion);
            return this.dialogValue("none");

        }), 0);

        Location region = entity.where().reset().and().equal(new Location().Id(ValueId)).entities().single();


        Location city = entity.where().reset().and().equal(new Location().Id(region.ParentId)
                .Type(region.Type - 1)).entities().single();

        Location province = entity.where().reset().and().equal(new Location().Id(city.ParentId)
                .Type(city.Type - 1)).entities().single();



        String area = String.format("%s %s %s", province.Name, city.Name, region.Name);

        this.context().send(new UMC.Web.WebMeta().event(key, new ListItem(area, area)), true);


    }
}