package UMC.Activities.Entities;

import java.util.UUID;

public class Design_Config {
    public UUID Id;
    public String Value;
    public String Name;
    public String GroupBy;
    public Integer Sequence;

    public Design_Config Id(UUID id) {
        Id = id;
        return this;
    }

    public Design_Config Value(String value) {
        Value = value;
        return this;
    }

    public Design_Config Name(String name) {
        Name = name;
        return this;
    }

    public Design_Config GroupBy(String groupBy) {
        GroupBy = groupBy;
        return this;
    }

    public Design_Config Sequence(Integer sequence) {
        Sequence = sequence;
        return this;
    }
}
