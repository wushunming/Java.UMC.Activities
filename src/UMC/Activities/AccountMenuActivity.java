package UMC.Activities;

import UMC.Data.Entities.Menu;
import UMC.Data.Utility;
import UMC.Security.AuthManager;
import UMC.Web.WebActivity;
import UMC.Web.WebMeta;
import UMC.Web.WebRequest;
import UMC.Web.WebResponse;

import java.util.LinkedList;
import java.util.List;

public class AccountMenuActivity extends WebActivity {
    @Override
    public void processActivity(WebRequest request, WebResponse response) {


        List<String> ids = new LinkedList<>();
        List<Menu> menus = new LinkedList<>();
        UMC.Data.Database.instance().objectEntity(Menu.class).where().and().equal(new Menu().Disable(false)
        ).entities().order().asc(new UMC.Data.Entities.Menu().Seq(0))
                .entities()
                .query(dr ->
                {
                    ids.add(dr.Id.toString());
                    menus.add(dr);
                });
        boolean[] auths = AuthManager.authorization(ids.toArray(new String[0]));
        List<WebMeta> menu = new LinkedList<>();
        List<Menu> tops = Utility.findAll(menus, m -> Utility.isEmpty(m.ParentId));

        for (Menu p : tops) {
            if (auths[ids.indexOf(p.Id.toString())] == false) {
                continue;

            }
            WebMeta m = new WebMeta().put("icon", p.Icon).put("text", p.Caption).put("id", p.Id);

            List<Menu> childs = Utility.findAll(menus, c -> c.ParentId.compareTo(p.Id) == 0);

            if (childs.size() > 0) {
                childs = Utility.findAll(childs, d -> auths[ids.indexOf(d.Id.toString())]);

                if (childs.size() == 0) {
                    continue;
                }
                List<WebMeta> cmeun = new LinkedList<>();
                for (Menu ch : childs) {
                    cmeun.add(new WebMeta().put("id", ch.Id).put("text", ch.Caption).put("url", ch.Url));

                }

                m.put("menu", cmeun);
            } else {
                m.put("url", p.Url);
            }
            menu.add(m);
        }

        response.redirect(menu);
    }
}
