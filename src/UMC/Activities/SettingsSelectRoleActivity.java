package UMC.Activities;

import UMC.Data.Entities.Role;
import UMC.Data.Entities.User;
import UMC.Data.Utility;
import UMC.Web.*;

import java.util.UUID;

@Mapping(model = "Settings", cmd = "SelectRole", desc = "选择角色")
public class SettingsSelectRoleActivity extends WebActivity {

    public void processActivity(WebRequest request, WebResponse response) {
        String key = this.asyncDialog("Key", g -> this.dialogValue("Promotion"));

        UUID UserId = Utility.uuid(this.asyncDialog("UserId", g ->
        {
            return new RoleDialog().search(true, true).setPage(true).title("选择角色").closeEvent("UI.Event");//, IsSearch = true };

        }));


        Role user = UMC.Data.Database.instance().objectEntity(Role.class)
                .where().and().equal(new Role().Id(UserId)).entities().single();
        ListItem item = new ListItem(user.Rolename, user.Rolename);
        this.context().send(new WebMeta().event(key, item), true);
    }
}
