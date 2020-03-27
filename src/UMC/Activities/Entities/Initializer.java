package UMC.Activities.Entities;

import UMC.Data.Utility;
import UMC.Web.Mapping;

@Mapping
public class Initializer extends UMC.Data.Entities.Initializer {
    public Initializer() {
        super();
        this.Setup(new Design_Config().Id(Utility.uuidEmpty), new Design_Config().Value(""));
        this.Setup(new Design_Item().Id(Utility.uuidEmpty), new Design_Item().Style("").Data("").Click(""));
    }
}
