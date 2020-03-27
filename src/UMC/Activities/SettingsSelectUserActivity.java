package UMC.Activities;

import UMC.Data.Entities.User;
import UMC.Data.Utility;
import UMC.Web.*;

import java.util.UUID;

@Mapping(model = "Settings", cmd = "SelectUser", desc = "选择用户")
public class SettingsSelectUserActivity extends WebActivity {

    public void processActivity(WebRequest request, WebResponse response) {
        String key = this.asyncDialog("Key", g -> this.dialogValue("Promotion"));

        UUID UserId = Utility.uuid(this.asyncDialog("UserId", g ->
        {
            return new UserDialog().search(true, false).setPage(true).title("选择人员").closeEvent("UI.Event");//, IsSearch = true };

        }));


        User user = UMC.Data.Database.instance().objectEntity(User.class)
                .where().and().equal(new User().Id(UserId)).entities().single();
        ListItem item = new ListItem(user.Alias, user.Username);
        this.context().send(new UMC.Web.WebMeta().event(key, item), true);
    }
}
