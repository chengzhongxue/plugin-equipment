<script lang="ts" setup>
import type { Equipment } from "@/types";
import { reset, submitForm } from "@formkit/core";
import { axiosInstance } from "@halo-dev/api-client";
import { IconSave, VButton, VModal } from "@halo-dev/components";
import { cloneDeep } from "lodash-es";
import { computed, nextTick, ref, watch } from "vue";

const props = withDefaults(
  defineProps<{
    visible: boolean;
    equipment?: Equipment;
    group?: string;
  }>(),
  {
    visible: false,
    equipment: undefined,
    group: undefined,
  }
);

const emit = defineEmits<{
  (event: "update:visible", value: boolean): void;
  (event: "close"): void;
  (event: "saved", equipment: Equipment): void;
}>();

const initialFormState: Equipment = {
  metadata: {
    name: "",
    generateName: "equipment-",
  },
  spec: {
    displayName: "",
    cover: "",
    groupName: props.group || "",
  },
  kind: "Equipment",
  apiVersion: "equipment.kunkunyu.com/v1alpha1",
} as Equipment;

const formState = ref<Equipment>(cloneDeep(initialFormState));

const saving = ref<boolean>(false);

const isUpdateMode = computed(() => {
  return !!formState.value.metadata.creationTimestamp;
});

const modalTitle = computed(() => {
  return isUpdateMode.value ? "编辑装备" : "添加装备";
});

const onVisibleChange = (visible: boolean) => {
  emit("update:visible", visible);
  if (!visible) {
    emit("close");
  }
};

const handleResetForm = () => {
  formState.value = cloneDeep(initialFormState);
  reset("equipment-form");
};

watch(
  () => props.visible,
  (visible) => {
    if (!visible && !props.equipment) {
      handleResetForm();
    }
  }
);

watch(
  () => props.equipment,
  (equipment) => {
    if (equipment) {
      formState.value = cloneDeep(equipment);
    } else {
      handleResetForm();
    }
  }
);
const annotationsFormRef = ref();

const handleSaveEquipment = async () => {
  annotationsFormRef.value?.handleSubmit();
  await nextTick();
  const { customAnnotations, annotations, customFormInvalid, specFormInvalid } = annotationsFormRef.value || {};
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
      await axiosInstance.put<Equipment>(
        `/apis/equipment.kunkunyu.com/v1alpha1/equipments/${formState.value.metadata.name}`,
        formState.value
      );
    } else {
      if (props.group) {
        formState.value.spec.groupName = props.group;
      }
      const { data } = await axiosInstance.post<Equipment>(`/apis/equipment.kunkunyu.com/v1alpha1/equipments`, formState.value);
      emit("saved", data);
    }
    onVisibleChange(false);
  } catch (e) {
    console.error(e);
  } finally {
    saving.value = false;
  }
};
</script>
<template>
  <VModal :title="modalTitle" :visible="visible" :width="650" @update:visible="onVisibleChange">
    <template #actions>
      <slot name="append-actions" />
    </template>

    <FormKit
      id="equipment-form"
      v-model="formState.spec"
      name="equipment-form"
      :actions="false"
      :config="{ validationVisibility: 'submit' }"
      type="form"
      @submit="handleSaveEquipment"
    >
      <div class="md:grid md:grid-cols-4 md:gap-6">
        <div class="md:col-span-1">
          <div class="sticky top-0">
            <span class="text-base font-medium text-gray-900"> 常规 </span>
          </div>
        </div>
        <div class="mt-5 divide-y divide-gray-100 md:col-span-3 md:mt-0">
          <FormKit name="displayName" label="名称" type="text" validation="required"></FormKit>
          <FormKit name="cover" label="封面" type="attachment" :accepts="['image/*']"></FormKit>
          <FormKit name="url" label="装备地址" type="text" :accepts="['image/*']"></FormKit>
          <FormKit name="specification" label="装备规格" type="text" ></FormKit>
          <FormKit name="description" label="描述" type="textarea"></FormKit>
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
          ref="annotationsFormRef"
          :value="formState.metadata.annotations"
          kind="Equipment"
          group="equipment.kunkunyu.com"
        />
      </div>
    </div>
    <template #footer>
      <VButton :loading="saving" type="secondary" @click="submitForm('equipment-form')">
        <template #icon>
          <IconSave class="size-full" />
        </template>
        保存
      </VButton>
    </template>
  </VModal>
</template>
