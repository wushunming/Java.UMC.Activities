package UMC.Activities.Entities;

import UMC.Data.Database;
import UMC.Data.Entities.Location;
import UMC.Data.Provider;
import UMC.Data.ProviderConfiguration;
import UMC.Data.Sql.IObjectEntity;
import UMC.Data.Utility;
import UMC.Web.Mapping;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Mapping
public class Initializer extends UMC.Data.Entities.Initializer {
    @Override
    public String resourceJS() {
        return "UMC.js";
    }

    public Initializer() {
        super();
        this.Setup(new Design_Config().Id(Utility.uuidEmpty), new Design_Config().Value(""));
        this.Setup(new Design_Item().Id(Utility.uuidEmpty), new Design_Item().Style("").Data("").Click(""));
    }

    @Override
    protected void Setup(Map hash, Database factory) {
        super.Setup(hash, factory);


        IObjectEntity<Location> locationEntity = factory.objectEntity(Location.class);
        try {
            if (locationEntity.count() == 0) {
                InputStream inputStream = Initializer.class.getClassLoader().getResourceAsStream("Location.csv");

                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));//构造一个BufferedReader类来读取文件
                String s = null;
                List<Location> locations = new LinkedList<>();
                while ((s = br.readLine()) != null) {

                    String[] data = Utility.fromCsvLine(s);
                    if (data.length > 3) {
                        boolean isOk = true;
                        for (int i = 0; i < 4; i++) {
                            if (Utility.isEmpty(data[i])) {
                                isOk = false;
                                break;
                            }
                        }
                        if (isOk) {
                            Location location = new Location();
                            location.Id(Utility.parse(data[0], 0))
                                    .Type(Utility.parse(data[1], 1))
                                    .Name(data[2]).ParentId(Utility.parse(data[3], 0));
                            if (data.length > 4) {
                                location.ZipCode(data[4]);
                            }
                            locations.add(location);
                        }
                    }

                }
                br.close();
                if (locations.size() > 0) {
                    locationEntity.insert(locations.toArray(new Location[0]));
                }
            }


        } catch (Exception ex) {

        }
        ProviderConfiguration umc = UMC.Data.ProviderConfiguration.configuration("UMC");
        if (umc == null) {
            umc = new ProviderConfiguration();
        }
        Provider provider=   Provider.create("Subject","*");
        provider.attributes().put("src","https://ali.365lu.cn/UMC/*");
        provider.attributes().put("desc","帮助文档");
        umc.set(provider);
        try {
            File file = new File(Utility.mapPath("App_Data/UMC/UMC.xml"));
            umc.WriteTo(file);
        } catch (Exception e) {
            e.printStackTrace();
        }




    }
}
