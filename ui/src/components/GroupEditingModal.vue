<script lang="ts" setup>
import type { EquipmentGroup } from "@/types";
import { reset, submitForm } from "@formkit/core";
import { axiosInstance } from "@halo-dev/api-client";
import { VButton, VModal, VSpace } from "@halo-dev/components";
import { useMagicKeys } from "@vueuse/core";
import { cloneDeep } from "lodash-es";
import { computed, nextTick, ref, watch } from "vue";

const props = withDefaults(
  defineProps<{
    visible: boolean;
    group: EquipmentGroup | null;
  }>(),
  {
    visible: false,
    group: null,
  }
);

const emit = defineEmits<{
  (event: "update:visible", visible: boolean): void;
  (event: "close"): void;
}>();

const initialFormState: EquipmentGroup = {
  apiVersion: "equipment.kunkunyu.com/v1alpha1",
  kind: "EquipmentGroup",
  metadata: {
    name: "",
    generateName: "equipment-group-",
  },
  spec: {
    displayName: "",
    priority: 0,
  },
  status: {
    equipmentCount: 0,
  },
};

const formState = ref<EquipmentGroup>(initialFormState);
const saving = ref(false);

const isUpdateMode = computed(() => {
  return !!formState.value.metadata.creationTimestamp;
});
const isMac = /macintosh|mac os x/i.test(navigator.userAgent);
const modalTitle = computed(() => {
  return isUpdateMode.value ? "编辑分组" : "新建分组";
});
const annotationsGroupFormRef = ref();

const handleCreateOrUpdateGroup = async () => {
  annotationsGroupFormRef.value?.handleSubmit();
  await nextTick();
  const { customAnnotations, annotations, customFormInvalid, specFormInvalid } = annotationsGroupFormRef.value || {};
  if (customFormInvalid || specFormInvalid) {
    return;
  }
  formState.value.metadata.annotations = {
    ...annotations,
    ...customAnnotations,
  };
  try {
    saving.value = true;
    if (isUpdateMode.value) {
      await axiosInstance.put(
        `/apis/equipment.kunkunyu.com/v1alpha1/equipmentgroups/${formState.value.metadata.name}`,
        formState.value
      );
    } else {
      await axiosInstance.post("/apis/equipment.kunkunyu.com/v1alpha1/equipmentgroups", formState.value);
    }
    onVisibleChange(false);
  } catch (e) {
    console.error("Failed to create equipment group", e);
  } finally {
    saving.value = false;
  }
};

const onVisibleChange = (visible: boolean) => {
  emit("update:visible", visible);
  if (!visible) {
    emit("close");
  }
};

const handleResetForm = () => {
  formState.value = cloneDeep(initialFormState);
  reset("equipment-group-form");
};

watch(
  () => props.visible,
  (visible) => {
    if (visible && props.group) {
      formState.value = cloneDeep(props.group);
      return;
    }
    handleResetForm();
  }
);

const { ControlLeft_Enter, Meta_Enter } = useMagicKeys();

watch(ControlLeft_Enter, (v) => {
  if (v && !isMac) {
    submitForm("equipment-group-form");
  }
});

watch(Meta_Enter, (v) => {
  if (v && isMac) {
    submitForm("equipment-group-form");
  }
});
</script>
<template>
  <VModal :visible="visible" :width="600" :title="modalTitle" @update:visible="onVisibleChange">
    <FormKit
      id="equipment-group-form"
      v-model="formState.spec"
      name="equipment-group-form"
      :classes="{ form: 'w-full' }"
      type="form"
      :config="{ validationVisibility: 'submit' }"
      @submit="handleCreateOrUpdateGroup"
    >
      <div class="md:grid md:grid-cols-4 md:gap-6">
        <div class="md:col-span-1">
          <div class="sticky top-0">
            <span class="text-base font-medium text-gray-900"> 常规 </span>
          </div>
        </div>
        <div class="mt-5 divide-y divide-gray-100 md:col-span-3 md:mt-0">
          <FormKit
            name="displayName"
            label="分组名称"
            type="text"
            validation="required"
            help="可根据此名称查询装备"
          ></FormKit>
          <FormKit
            name="description"
            label="分组描述"
            type="textarea"
          ></FormKit>
          
        </div>
      </div>
    </FormKit>
    <div class="py-5">
      <div class="border-t border-gray-200"></div>
    </div>
    <div class="md:grid md:grid-cols-4 md:gap-6">
      <div class="md:col-span-1">
        <div class="sticky top-0">
          <span class="text-base font-medium text-gray-900"> 元数据 </span>
        </div>
      </div>
      <div class="mt-5 divide-y divide-gray-100 md:col-span-3 md:mt-0">
        <AnnotationsForm
          v-if="visible"
          :key="formState.metadata.name"
          ref="annotationsGroupFormRef"
          :value="formState.metadata.annotations"
          kind="EquipmentGroup"
          group="equipment.kunkunyu.com"
        />
      </div>
    </div>
    <template #footer>
      <VSpace>
        <VButton type="secondary" @click="submitForm('equipment-group-form')">
          提交 {{ `${isMac ? "⌘" : "Ctrl"} + ↵` }}
        </VButton>
        <VButton @click="onVisibleChange(false)">取消 Esc</VButton>
      </VSpace>
    </template>
  </VModal>
</template>
