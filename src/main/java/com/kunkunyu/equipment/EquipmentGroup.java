package com.kunkunyu.equipment;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;


@Data
@EqualsAndHashCode(callSuper = true)
@GVK(group = "equipment.kunkunyu.com", version = "v1alpha1", kind = "EquipmentGroup",
    plural = "equipmentgroups", singular = "equipmentgroup")
public class EquipmentGroup extends AbstractExtension {

    @Schema(required = true)
    private EquipmentGroupSpec spec;

    @Schema
    private EquipmentGroupStatus status;

    @Data
    public static class EquipmentGroupSpec {
        @Schema(required = true)
        private String displayName;

        private String description;

        private Integer priority;
    }

    @JsonIgnore
    public EquipmentGroupStatus getStatusOrDefault() {
        if (this.status == null) {
            this.status = new EquipmentGroupStatus();
        }
        return this.status;
    }

    @Data
    public static class EquipmentGroupStatus {

        public Integer equipmentCount;
    }
}
