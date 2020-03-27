package UMC.Activities;

import UMC.Data.Entities.Role;
import UMC.Data.Sql.IObjectEntity;
import UMC.Data.Utility;
import UMC.Security.Identity;
import UMC.Web.*;

import java.util.UUID;

public class SettingsRoleActivity extends WebActivity {

    @Override
    public void processActivity(WebRequest request, WebResponse response) {

        String strUser = this.asyncDialog("RoleId", d ->
        {
            RoleDialog dlg = new RoleDialog();
            dlg.title("角色管理");
            dlg.refreshEvent("Role");
            dlg.setPage(true);// = true;

            if (Identity.current().isInRole(UMC.Security.Membership.AdminRole)) {

                dlg.menu("新建", "Settings", "Rolr", "News");
            }
            return dlg;
        });
        UUID userId = Utility.uuid(strUser);

        IObjectEntity<Role> userEntity = UMC.Data.Database.instance().objectEntity(Role.class);

        Role role = Utility.isNull(userEntity.where().and().equal("Id", userId).entities().single(), new Role());


        WebMeta setting = this.asyncDialog(d ->
        {
            UIFormDialog frm = new UIFormDialog();
            frm.title("用户角色");
            frm.addText("角色名", "Rolename", role.Rolename);
            frm.addTextarea("角色说明", "Explain", role.Explain).put("tip", "角色说明");

            return frm;
        }, "Setting");

        Role nrole = new Role();
        Utility.setField(nrole, setting.map());
        if (role.Id != null) {
            userEntity.update(nrole);
        } else {
            nrole.Id = UUID.randomUUID();
            userEntity.insert(nrole);
        }


        this.context().send(new UMC.Web.WebMeta().put("type", "Role"), true);

    }
}


