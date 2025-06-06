<script lang="ts" setup>
import LazyImage from "@/components/LazyImage.vue";
import EquipmentEditingModal from "@/components/EquipmentEditingModal.vue";
import type {Equipment, EquipmentList} from "@/types";
import { axiosInstance } from "@halo-dev/api-client";
import {
  Dialog,
  IconAddCircle,
  IconArrowLeft,
  IconArrowRight,
  IconCheckboxFill,
  Toast,
  VButton,
  VCard,
  VDropdown,
  VDropdownItem,
  VEmpty,
  VLoading,
  VPageHeader,
  VPagination,
  VSpace,
} from "@halo-dev/components";
import type { AttachmentLike } from "@halo-dev/console-shared";
import { useQuery } from "@tanstack/vue-query";
import Fuse from "fuse.js";
import { computed, nextTick, ref, watch } from "vue";
import TablerDeviceGamepad3 from '~icons/tabler/device-gamepad-3'
import GroupList from "../components/GroupList.vue";
import {VueDraggable} from "vue-draggable-plus";

const removeFileExtension = (filename: string) => {
  return filename.replace(/\.[^/.]+$/, "");
};

const selectedEquipment = ref<Equipment | undefined>();
const selectedEquipments = ref<Set<Equipment>>(new Set<Equipment>());
const selectedGroup = ref<string>();
const editingModal = ref(false);
const checkedAll = ref(false);
const groupListRef = ref();

const page = ref(1);
const size = ref(20);
const total = ref(0);
const keyword = ref("");
const equipments = ref<Equipment[]>([]);

const {
  isLoading,
  refetch,
} = useQuery<Equipment[]>({
  queryKey: [page, size, keyword, selectedGroup],
  queryFn: async () => {
    if (!selectedGroup.value) {
      return [];
    }
    const { data } = await axiosInstance.get<EquipmentList>("/apis/console.api.equipment.kunkunyu.com/v1alpha1/equipments", {
      params: {
        page: page.value,
        size: size.value,
        keyword: keyword.value,
        group: selectedGroup.value,
      },
    });
    total.value = data.total;
    return data.items
      .map((group) => {
        if (group.spec) {
          group.spec.priority = group.spec.priority || 0;
        }
        return group;
      })
      .sort((a, b) => {
        return (a.spec?.priority || 0) - (b.spec?.priority || 0);
      });
  },
  refetchInterval(data) {
    const deletingGroups = data?.filter((group) => !!group.metadata.deletionTimestamp);

    return deletingGroups?.length ? 1000 : false;
  },
  onSuccess(data) {
    equipments.value = data;
  },
  refetchOnWindowFocus: false,
});

const handleSelectPrevious = () => {
  if (!equipments.value) {
    return;
  }

  const currentIndex = equipments.value.findIndex((equipment) => equipment.metadata.name === selectedEquipment.value?.metadata.name);

  if (currentIndex > 0) {
    selectedEquipment.value = equipments.value[currentIndex - 1];
    return;
  }

  if (currentIndex <= 0) {
    selectedEquipment.value = undefined;
  }
};

const handleSelectNext = () => {
  if (!equipments.value) {
    return;
  }

  if (!selectedEquipment.value) {
    selectedEquipment.value = equipments.value[0];
    return;
  }
  const currentIndex = equipments.value.findIndex((equipment) => equipment.metadata.name === selectedEquipment.value?.metadata.name);
  if (currentIndex !== equipments.value.length - 1) {
    selectedEquipment.value = equipments.value[currentIndex + 1];
  }
};

const handleOpenEditingModal = (equipment?: Equipment) => {
  selectedEquipment.value = equipment;
  editingModal.value = true;
};

const handleDeleteInBatch = () => {
  Dialog.warning({
    title: "是否确认删除所选的装备？",
    description: "删除之后将无法恢复。",
    confirmType: "danger",
    onConfirm: async () => {
      try {
        const promises = Array.from(selectedEquipments.value).map((equipment) => {
          return axiosInstance.delete(`/apis/equipment.kunkunyu.com/v1alpha1/equipments/${equipment.metadata.name}`);
        });
        await Promise.all(promises);
      } catch (e) {
        console.error(e);
      } finally {
        pageRefetch();
      }
    },
  });
};

const handleCheckAllChange = (e: Event) => {
  const { checked } = e.target as HTMLInputElement;
  handleCheckAll(checked);
};

const handleCheckAll = (checkAll: boolean) => {
  if (checkAll) {
    equipments.value?.forEach((equipment) => {
      selectedEquipments.value.add(equipment);
    });
  } else {
    selectedEquipments.value.clear();
  }
};

const isChecked = (equipment: Equipment) => {
  return (
    equipment.metadata.name === selectedEquipment.value?.metadata.name ||
    Array.from(selectedEquipments.value)
      .map((item) => item.metadata.name)
      .includes(equipment.metadata.name)
  );
};

watch(
  () => selectedEquipments.value.size,
  (newValue) => {
    checkedAll.value = newValue === equipments.value?.length;
  }
);

// search
let fuse: Fuse<Equipment> | undefined = undefined;

watch(
  () => equipments.value,
  () => {
    if (!equipments.value) {
      return;
    }

    fuse = new Fuse(equipments.value, {
      keys: ["spec.displayName", "metadata.name", "spec.description", "spec.url"],
      useExtendedSearch: true,
    });
  }
);

const searchResults = computed({
  get() {
    if (!fuse || !keyword.value) {
      return equipments.value || [];
    }

    return fuse?.search(keyword.value).map((item) => item.item);
  },
  set(value) {
    equipments.value = value;
  },
});

// create by attachments
const attachmentModal = ref(false);

const onAttachmentsSelect = async (attachments: AttachmentLike[]) => {
  const equipments: {
    url: string;
    cover?: string;
    displayName?: string;
    type?: string;
  }[] = attachments
    .map((attachment) => {
      const post = {
        groupName: selectedGroup.value || "",
      };

      if (typeof attachment === "string") {
        return {
          ...post,
          url: attachment,
          cover: attachment,
        };
      }
      if ("url" in attachment) {
        return {
          ...post,
          url: attachment.url,
          cover: attachment.url,
        };
      }
      if ("spec" in attachment) {
        return {
          ...post,
          cover: attachment.status?.permalink,
          displayName: attachment.spec.displayName ? removeFileExtension(attachment.spec.displayName) : undefined,
          type: attachment.spec.mediaType,
        };
      }
    })
    .filter(Boolean) as {
    url: string;
    cover?: string;
    displayName?: string;
    type?: string;
  }[];

  for (const equipment of equipments) {
    const type = equipment.type;
    if (!type) {
      Toast.error("只支持选择图片");
      nextTick(() => {
        attachmentModal.value = true;
      });

      return;
    }
    const fileType = type.split("/")[0];
    if (fileType !== "image") {
      Toast.error("只支持选择图片");
      nextTick(() => {
        attachmentModal.value = true;
      });
      return;
    }
  }

  const createRequests = equipments.map((equipment) => {
    return axiosInstance.post<Equipment>("/apis/equipment.kunkunyu.com/v1alpha1/equipments", {
      metadata: {
        name: "",
        generateName: "equipment-",
      },
      spec: equipment,
      kind: "Equipment",
      apiVersion: "equipment.kunkunyu.com/v1alpha1",
    });
  });

  await Promise.all(createRequests);

  Toast.success(`新建成功，一共创建了 ${equipments.length} 个装备。`);
  pageRefetch();
};

const handleSaveInBatch = async () => {
  try {
    const promises = equipments.value?.map((equipment: Equipment, index) => {
      if (equipment.spec) {
        equipment.spec.priority = index;
      }
      return axiosInstance.put(`/apis/equipment.kunkunyu.com/v1alpha1/equipments/${equipment.metadata.name}`, equipment);
    });
    if (promises) {
      await Promise.all(promises);
    }
  } catch (e) {
    console.error(e);
  } finally {
    refetch();
  }
};

const groupSelectHandle = (group?: string) => {
  selectedGroup.value = group;
};

const pageRefetch = async () => {
  await groupListRef.value.refetch();
  await refetch();
  selectedEquipments.value = new Set<Equipment>();
};
</script>
<template>
  <EquipmentEditingModal
    v-model:visible="editingModal"
    :equipment="selectedEquipment"
    :group="selectedGroup"
    @close="refetch()"
    @saved="pageRefetch"
  >
    <template #append-actions>
      <span @click="handleSelectPrevious">
        <IconArrowLeft />
      </span>
      <span @click="handleSelectNext">
        <IconArrowRight />
      </span>
    </template>
  </EquipmentEditingModal>
  <AttachmentSelectorModal v-model:visible="attachmentModal" :accepts="['image/*']" @select="onAttachmentsSelect" />
  <VPageHeader title="装备">
    <template #icon>
      <TablerDeviceGamepad3 class="mr-2 self-center" />
    </template>
  </VPageHeader>
  <div class="p-4">
    <div class="flex flex-col gap-2 lg:flex-row">
      <div class="w-full flex-none lg:w-96">
        <GroupList ref="groupListRef" @select="groupSelectHandle" />
      </div>
      <div class="flex-1 shrink min-w-0">
        <VCard>
          <template #header>
            <div class="block w-full bg-gray-50 px-4 py-3">
              <div class="relative flex flex-col items-start sm:flex-row sm:items-center">
                <div class="mr-4 hidden items-center sm:flex">
                  <input v-model="checkedAll" type="checkbox" @change="handleCheckAllChange" />
                </div>
                <div class="flex w-full flex-1 sm:w-auto">
                  <SearchInput v-if="!selectedEquipments.size" v-model="keyword" />
                  <VSpace v-else>
                    <VButton type="danger" @click="handleDeleteInBatch"> 删除 </VButton>
                  </VSpace>
                </div>
                <div v-if="selectedGroup" v-permission="['plugin:equipment:manage']" class="mt-4 flex sm:mt-0">
                  <VDropdown>
                    <VButton size="xs"> 新增 </VButton>
                    <template #popper>
                      <VDropdownItem @click="handleOpenEditingModal()"> 新增 </VDropdownItem>
                      <VDropdownItem @click="attachmentModal = true"> 从附件库选择 </VDropdownItem>
                    </template>
                  </VDropdown>
                </div>
              </div>
            </div>
          </template>
          <VLoading v-if="isLoading" />
          <Transition v-else-if="!selectedGroup" appear name="fade">
            <VEmpty message="请选择或新建分组" title="未选择分组"></VEmpty>
          </Transition>
          <Transition v-else-if="!searchResults.length" appear name="fade">
            <VEmpty message="你可以尝试刷新或者新建装备" title="当前没有装备">
              <template #actions>
                <VSpace>
                  <VButton @click="refetch"> 刷新</VButton>
                  <VButton v-permission="['plugin:equipment:manage']" type="primary" @click="handleOpenEditingModal()">
                    <template #icon>
                      <IconAddCircle class="size-full" />
                    </template>
                    新增装备
                  </VButton>
                </VSpace>
              </template>
            </VEmpty>
          </Transition>
          <Transition v-else appear name="fade">
            <VueDraggable
              v-model="equipments"
              class="mt-2 grid grid-cols-1 gap-x-2 gap-y-3 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-5"
              group="equipment"
              handle=".drag-element"
              item-key="metadata.name"
              tag="div"
              role="list"
              @update="handleSaveInBatch"
            >
              <VCard
                v-for="equipment in equipments"
                :key="equipment.metadata.name"
                :body-class="['!p-0']"
                :class="{
                  'ring-primary ring-1': isChecked(equipment),
                  'ring-1 ring-red-600': equipment.metadata.deletionTimestamp,
                }"
                class="hover:shadow drag-element "
                @click="handleOpenEditingModal(equipment)"
              >
                <div class="group relative bg-white">
                  <div class="aspect-16/9 block size-full cursor-pointer overflow-hidden bg-gray-100 relative">
                    <LazyImage
                      :key="equipment.metadata.name"
                      :alt="equipment.spec.displayName"
                      :src="equipment.spec.cover || equipment.spec.url"
                      classes="size-full pointer-events-none group-hover:opacity-75"
                    >
                      <template #loading>
                        <div class="flex h-full justify-center">
                          <VLoading></VLoading>
                        </div>
                      </template>
                      <template #error>
                        <div class="flex h-full items-center justify-center object-cover">
                          <span class="text-xs text-red-400"> 加载异常 </span>
                        </div>
                      </template>
                    </LazyImage>
                  </div>

                  <p
                    v-tooltip="equipment.spec.displayName"
                    class="block cursor-pointer truncate px-2 py-1 text-center text-xs font-medium text-gray-700"
                  >
                    {{ equipment.spec.displayName }}
                  </p>

                  <div v-if="equipment.metadata.deletionTimestamp" class="absolute top-1 right-1 text-xs text-red-300">
                    删除中...
                  </div>

                  <div
                    v-if="!equipment.metadata.deletionTimestamp"
                    v-permission="['plugin:equipment:manage']"
                    :class="{ '!flex': selectedEquipments.has(equipment) }"
                    class="absolute top-0 left-0 hidden h-1/3 w-full cursor-pointer justify-end bg-gradient-to-b from-gray-300 to-transparent ease-in-out group-hover:flex"
                    @click.stop="selectedEquipments.has(equipment) ? selectedEquipments.delete(equipment) : selectedEquipments.add(equipment)"
                  >
                    <IconCheckboxFill
                      :class="{
                        '!text-primary': selectedEquipments.has(equipment),
                      }"
                      class="hover:text-primary mt-1 mr-1 h-6 w-6 cursor-pointer text-white transition-all"
                    />
                  </div>
                </div>
              </VCard>
            </VueDraggable>
          </Transition>

          <template #footer>
            <VPagination v-model:page="page" v-model:size="size" :total="total" :size-options="[20, 30, 50, 100]" />
          </template>
        </VCard>
      </div>
    </div>
  </div>
</template>
