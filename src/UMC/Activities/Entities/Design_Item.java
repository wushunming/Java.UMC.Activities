package UMC.Activities.Entities;

import java.util.Date;
import java.util.UUID;

public class Design_Item {

    public UUID Id;
    public Integer Type;
    public UUID design_id;
    public UUID for_id;

    public Design_Item Id(UUID id) {
        Id = id;
        return this;
    }

    public Design_Item Type(Integer type) {
        this.Type = type;
        return this;
    }

    public Design_Item Design_id(UUID design_id) {
        this.design_id = design_id;
        return this;
    }

    public Design_Item For_id(UUID for_id) {
        this.for_id = for_id;
        return this;
    }

    public Design_Item Value_id(UUID value_id) {
        this.value_id = value_id;
        return this;
    }

    public Design_Item Seq(Integer seq) {
        Seq = seq;
        return this;
    }

    public Design_Item ItemName(String itemName) {
        this.ItemName = itemName;
        return this;
    }

    public Design_Item ItemDesc(String itemDesc) {
        this.ItemDesc = itemDesc;
        return this;
    }

    public Design_Item Click(String click) {
        this.Click = click;
        return this;
    }

    public Design_Item Style(String style) {
        this.Style = style;
        return this;
    }

    public Design_Item Data(String data) {
        this.Data = data;
        return this;
    }

    public Design_Item ModifiedDate(Date modifiedDate) {
        this.ModifiedDate = modifiedDate;
        return this;
    }

    public UUID value_id;


    public Integer Seq;


    public String ItemName;


    public String ItemDesc;


    public String Click;


    public String Style;


    public String Data;


    public Date ModifiedDate;


}
