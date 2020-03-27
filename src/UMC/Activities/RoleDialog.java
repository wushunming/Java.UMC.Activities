package UMC.Activities;

import UMC.Data.Entities.Role;
import UMC.Data.Sql.IObjectEntity;
import UMC.Web.UIGridDialog;

import java.util.HashMap;
import java.util.Map;

public class RoleDialog extends UIGridDialog {
    protected Map getHeader() {
        this.asyncData(true);

        Header header = new Header("Id", 25);
        header.add("Rolename", "角色名");
        return header.getHeaderMap();


    }

    protected Map getData(Map paramsKey) {

        IObjectEntity<Role> roleIObjectEntity = UMC.Data.Database.instance().objectEntity(Role.class);// < UMC.Data.Entities.Role > ();

        Map hash = new HashMap();
        hash.put("data", roleIObjectEntity.query());
        hash.put("totla", roleIObjectEntity.count());
        return hash;
    }

}
