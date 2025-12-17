<template>
  <el-dialog
    v-model="visible"
    :title="isEdit ? '编辑标签' : parentTag ? '添加子标签' : '添加标签'"
    width="500px"
    @close="handleClose"
  >
    <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
      <el-form-item label="标签名称" prop="name">
        <el-input v-model="form.name" placeholder="请输入标签名称" />
      </el-form-item>
      <el-form-item v-if="!parentTag" label="标签层级" prop="level">
        <el-radio-group v-model="form.level">
          <el-radio :label="1">一级标签</el-radio>
          <el-radio :label="2">二级标签</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item
        v-if="form.level === 2 && !parentTag"
        label="父标签"
        prop="parentId"
      >
        <el-select v-model="form.parentId" placeholder="请选择父标签" filterable>
          <el-option
            v-for="tag in parentTags"
            :key="tag.id"
            :label="tag.name"
            :value="tag.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item v-if="parentTag" label="父标签">
        <el-input :value="parentTag.name" disabled />
      </el-form-item>
      <el-form-item label="排序顺序" prop="sortOrder">
        <el-input-number
          v-model="form.sortOrder"
          :min="0"
          placeholder="数字越小越靠前"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="loading">
        确定
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { bookmarkApi } from '@/services/bookmarkApi'
import type { BookmarkTag } from '@/types/bookmark'
import type { FormInstance, FormRules } from 'element-plus'

interface Props {
  modelValue: boolean
  tag?: BookmarkTag | null
  parentTag?: BookmarkTag | null
}

const props = withDefaults(defineProps<Props>(), {
  tag: null,
  parentTag: null,
})

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  success: []
}>()

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val),
})

const isEdit = computed(() => !!props.tag)
const formRef = ref<FormInstance>()

const form = ref({
  name: '',
  level: 1,
  parentId: '',
  sortOrder: 0,
})

const rules: FormRules = {
  name: [{ required: true, message: '请输入标签名称', trigger: 'blur' }],
  parentId: [
    {
      required: true,
      message: '请选择父标签',
      trigger: 'change',
      validator: (rule, value, callback) => {
        if (form.value.level === 2 && !props.parentTag && !value) {
          callback(new Error('请选择父标签'))
        } else {
          callback()
        }
      },
    },
  ],
}

const parentTags = ref<BookmarkTag[]>([])
const loading = ref(false)

// 加载父标签列表
const loadParentTags = async () => {
  try {
    parentTags.value = await bookmarkApi.getParentTags()
  } catch (error) {
    ElMessage.error('加载父标签失败')
  }
}

// 初始化表单
const initForm = () => {
  if (props.tag) {
    form.value = {
      name: props.tag.name,
      level: props.tag.level,
      parentId: props.tag.parentId || '',
      sortOrder: props.tag.sortOrder,
    }
  } else if (props.parentTag) {
    form.value = {
      name: '',
      level: 2,
      parentId: props.parentTag.id,
      sortOrder: 0,
    }
  } else {
    form.value = {
      name: '',
      level: 1,
      parentId: '',
      sortOrder: 0,
    }
  }
}

// 提交
const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      if (isEdit.value && props.tag) {
        await bookmarkApi.updateTag({
          id: props.tag.id,
          name: form.value.name,
          sortOrder: form.value.sortOrder,
        })
        ElMessage.success('更新成功')
      } else {
        await bookmarkApi.createTag({
          name: form.value.name,
          parentId: form.value.level === 2 ? form.value.parentId : undefined,
          sortOrder: form.value.sortOrder,
        })
        ElMessage.success('创建成功')
      }
      emit('success')
      handleClose()
    } catch (error: any) {
      ElMessage.error(error.message || '操作失败')
    } finally {
      loading.value = false
    }
  })
}

// 关闭
const handleClose = () => {
  visible.value = false
  formRef.value?.resetFields()
}

watch(
  () => props.modelValue,
  (val) => {
    if (val) {
      initForm()
      if (form.value.level === 2 && !props.parentTag) {
        loadParentTags()
      }
    }
  }
)
</script>

