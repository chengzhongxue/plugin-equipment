import EquipmentList from "@/views/EquipmentList.vue";
import { definePlugin } from "@halo-dev/console-shared";
import { markRaw } from "vue";
import TablerDeviceGamepad3 from '~icons/tabler/device-gamepad-3'


export default definePlugin({
  routes: [
    {
      parentName: "Root",
      route: {
        path: "/equipments",
        name: "Equipments",
        component: EquipmentList,
        meta: {
          permissions: ["plugin:equipment:view"],
          menu: {
            name: "装备",
            group: "content",
            icon: markRaw(TablerDeviceGamepad3),
          },
        },
      },
    },
  ],
});
