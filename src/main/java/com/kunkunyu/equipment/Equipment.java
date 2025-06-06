package com.kunkunyu.equipment;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Objects;
import lombok.Data;
import lombok.EqualsAndHashCode;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

/**
 * @author ryanwang
 */
@Data
@EqualsAndHashCode(callSuper = true)
@GVK(group = "equipment.kunkunyu.com", version = "v1alpha1", kind = "Equipment", plural = "equipments",
    singular = "equipment")
public class Equipment extends AbstractExtension {

    private EquipmentSpec spec;

    @Data
    public static class EquipmentSpec {
        @Schema(requiredMode = REQUIRED)
        private String displayName;

        private String specification;

        private String description;

        @Schema(requiredMode = REQUIRED)
        private String cover;

        private String url;

        private Integer priority;

        @Schema(requiredMode = REQUIRED, pattern = "^\\S+$")
        private String groupName;
    }

    @JsonIgnore
    public boolean isDeleted() {
        return Objects.equals(true,
            getMetadata().getDeletionTimestamp() != null
        );
    }

}
