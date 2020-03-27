package UMC.Activities;

import UMC.Activities.Entities.Design_Config;
import UMC.Web.Mapping;
import UMC.Web.WebActivity;
import UMC.Web.WebAuthType;
import UMC.Web.WebFlow;

import java.util.List;

@Mapping(model = "Account", desc = "账户访问")
public class AccountFlow extends WebFlow {
    @Override
    public WebActivity firstActivity() {
        switch (this.context().request().cmd()) {
            case "Check":
                return new AccountCheckActivity();
            case "Login":
                return new AccountLoginActivity();
            case "Register":
                return new AccountRegisterActivity();
            case "Forget":
                return new AccountForgetActivity();
            case "Password":
                return new AccountPasswordActivity();
            case "Self":
                return new AccountSelfActivity();
            case "Email":
                return new AccountEmailActivity();
            case "Mobile":
                return new AccountMobileActivity();
            case "Close":
                return new AccountCloseActivity();
            case "Menu":
                return new AccountMenuActivity();
        }

        return WebActivity.Empty;
    }
}
