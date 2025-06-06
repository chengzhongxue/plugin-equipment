package com.kunkunyu.equipment;

import static run.halo.app.extension.index.IndexAttributeFactory.simpleAttribute;

import org.springframework.stereotype.Component;
import run.halo.app.extension.SchemeManager;
import run.halo.app.extension.index.IndexSpec;
import run.halo.app.plugin.BasePlugin;
import run.halo.app.plugin.PluginContext;

@Component
public class EquipmentPlugin extends BasePlugin {
    private final SchemeManager schemeManager;

    public EquipmentPlugin(PluginContext pluginContext, SchemeManager schemeManager) {
        super(pluginContext);
        this.schemeManager = schemeManager;
    }

    @Override
    public void start() {
        schemeManager.register(Equipment.class, indexSpecs -> {
            indexSpecs.add(new IndexSpec()
                .setName("spec.groupName")
                .setIndexFunc(simpleAttribute(Equipment.class, equipment ->
                    equipment.getSpec() == null ? "" : equipment.getSpec().getGroupName()
                ))
            );
            indexSpecs.add(new IndexSpec()
                .setName("spec.displayName")
                .setIndexFunc(simpleAttribute(Equipment.class, equipment ->
                    equipment.getSpec() == null ? "" : equipment.getSpec().getDisplayName()
                ))
            );
            indexSpecs.add(new IndexSpec()
                .setName("spec.priority")
                .setIndexFunc(simpleAttribute(Equipment.class, equipment ->
                    equipment.getSpec() == null || equipment.getSpec().getPriority() == null
                        ? String.valueOf(0) : equipment.getSpec().getPriority().toString()
                ))
            );
        });
        schemeManager.register(EquipmentGroup.class, indexSpecs -> {
            indexSpecs.add(new IndexSpec()
                .setName("spec.priority")
                .setIndexFunc(simpleAttribute(EquipmentGroup.class, group ->
                    group.getSpec() == null || group.getSpec().getPriority() == null
                        ? String.valueOf(0) : group.getSpec().getPriority().toString()
                ))
            );
        });
    }

    @Override
    public void stop() {
        schemeManager.unregister(schemeManager.get(Equipment.class));
        schemeManager.unregister(schemeManager.get(EquipmentGroup.class));
    }
}
