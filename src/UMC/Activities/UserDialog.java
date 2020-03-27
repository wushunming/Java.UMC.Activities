package UMC.Activities;

import UMC.Data.Database;
import UMC.Data.Entities.User;
import UMC.Data.Sql.IObjectEntity;
import UMC.Data.Utility;
import UMC.Security.Membership;
import UMC.Web.UIGridDialog;

import java.util.Hashtable;
import java.util.Map;

public class UserDialog extends UIGridDialog {

    public UserDialog() {
        this.title("账户管理");

    }

    protected Map getHeader() {


        Header header = new Header("Id", 25);
        header.add("Username", "登录名");
        header.add("Alias", "别名");
        return header.getHeaderMap();


    }

    protected Map getData(Map paramsKey) {
        int start = Utility.parse((String) Utility.isNull(paramsKey.get("start"), "0"), 0);
        int limit = Utility.parse((String) Utility.isNull(paramsKey.get("limit"), "25"), 25);


        IObjectEntity<User> scheduleEntity = Database.instance().objectEntity(User.class);

        String sort = (String) paramsKey.get("sort");
        String dir = (String) paramsKey.get("dir");


        if (!Utility.isEmpty(sort)) {
            switch (sort) {
                case "Disabled":
                    scheduleEntity.where().and("(Flags&{0})={0}", Membership.UserFlagsDisabled);
                    break;
                case "Lock":
                    scheduleEntity.where().and("(Flags&{0})={0}", Membership.UserFlagsLock);
                    break;

                default:
                    if (dir == "DESC") {
                        scheduleEntity.order().desc(sort);
                    } else {
                        scheduleEntity.order().asc(sort);
                    }
                    break;
            }
        } else {
            scheduleEntity.order().desc("RegistrTime");

        }

        String Keyword = (String) paramsKey.get("Keyword");

        if (Utility.isEmpty(Keyword) == false) {
            scheduleEntity.where().contains().or().like(new User().Username(Keyword).Alias(Keyword));

        }


        Map hash = new Hashtable();
        hash.put("data", scheduleEntity.query(start, limit));
        hash.put("total", scheduleEntity.count());

        /*
         * 当total等于0的时间hash支持msg参数，意思没有数据的时候数据提示文本
         * */
        return hash;
    }
}