package com.kunkunyu.equipment.vo;

import lombok.Builder;
import lombok.Value;
import run.halo.app.extension.MetadataOperator;
import run.halo.app.theme.finders.vo.ExtensionVoOperator;
import com.kunkunyu.equipment.Equipment;


@Value
@Builder
public class EquipmentVo implements ExtensionVoOperator {
    
    MetadataOperator metadata;
    
    Equipment.EquipmentSpec spec;
    
    public static EquipmentVo from(Equipment equipment) {
        return EquipmentVo.builder()
            .metadata(equipment.getMetadata())
            .spec(equipment.getSpec())
            .build();
    }
}
