import { definePlugin } from "@halo-dev/console-shared";
import { defineAsyncComponent, markRaw } from "vue";
import TablerDeviceGamepad3 from '~icons/tabler/device-gamepad-3'
import "uno.css";
import { VLoading } from "@halo-dev/components";

export default definePlugin({
  routes: [
    {
      parentName: "Root",
      route: {
        path: "/equipments",
        name: "Equipments",
        component: defineAsyncComponent({
          loader: () => import("@/views/EquipmentList.vue"),
          loadingComponent: VLoading,
        }),
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
