
package UMC.Activities;

import UMC.Data.Configuration;
import UMC.Data.Database;
import UMC.Data.Entities.Account;
import UMC.Data.Sql.IObjectEntity;
import UMC.Data.Utility;
import UMC.Security.AccessToken;
import UMC.Security.Identity;
import UMC.Security.Membership;
import UMC.Security.Principal;
import UMC.Web.*;

import java.util.Hashtable;


public class AccountCloseActivity extends WebActivity {
    @Override
    public void processActivity(WebRequest request, WebResponse response) {



        this.asyncDialog("Confirm", g->new  UIConfirmDialog("确认退出吗"));
        UMC.Security.AccessToken.signOut();


        this.prompt("退出成功", false);
        this.context().send("Close", false);


    }
}
