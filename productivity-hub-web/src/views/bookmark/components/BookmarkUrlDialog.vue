<template>
  <el-dialog
    v-model="visible"
    :title="isEdit ? '编辑网址' : '添加网址'"
    width="600px"
    @close="handleClose"
  >
    <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
      <el-form-item label="网址标题" prop="title">
        <el-input 
          v-model="form.title" 
          placeholder="请输入网址标题" 
          maxlength="100"
          show-word-limit
        />
      </el-form-item>
      <el-form-item label="跳转URL" prop="url">
        <el-input 
          v-model="form.url" 
          placeholder="请输入网址URL（必须以http://或https://开头）" 
          maxlength="500"
          show-word-limit
        />
      </el-form-item>
      <el-form-item label="网站图标" prop="iconUrl">
        <el-input 
          v-model="form.iconUrl" 
          placeholder="请输入图标URL（可选）" 
          maxlength="500"
          show-word-limit
        />
      </el-form-item>
      <el-form-item label="网址描述" prop="description">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="3"
          placeholder="请输入网址描述（可选）"
          maxlength="500"
          show-word-limit
        />
      </el-form-item>
      <el-form-item label="标签归属" prop="tagIds">
        <el-select
          v-model="form.tagIds"
          multiple
          placeholder="请选择标签（至少选择一个）"
          filterable
          style="width: 100%"
        >
          <el-option-group
            v-for="parentTag in tagTree"
            :key="parentTag.id"
            :label="parentTag.name"
          >
            <el-option :label="parentTag.name" :value="parentTag.id" />
            <el-option
              v-for="childTag in parentTag.children || []"
              :key="childTag.id"
              :label="`  └─ ${childTag.name}`"
              :value="childTag.id"
            />
          </el-option-group>
        </el-select>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="loading" v-button-lock>
        确定
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { bookmarkApi } from '@/services/bookmarkApi'
import type { BookmarkTag, BookmarkUrl } from '@/types/bookmark'
import type { FormInstance, FormRules } from 'element-plus'

interface Props {
  modelValue: boolean
  url?: BookmarkUrl | null
}

const props = withDefaults(defineProps<Props>(), {
  url: null,
})

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  success: []
}>()

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val),
})

const isEdit = computed(() => !!props.url)
const formRef = ref<FormInstance>()

const form = ref({
  title: '',
  url: '',
  iconUrl: '',
  description: '',
  tagIds: [] as string[],
})

const rules: FormRules = {
  title: [{ required: true, message: '请输入网址标题', trigger: 'blur' }],
  url: [
    { required: true, message: '请输入网址URL', trigger: 'blur' },
    {
      pattern: /^https?:\/\/.+/,
      message: 'URL格式不正确，必须以http://或https://开头',
      trigger: 'blur',
    },
  ],
  tagIds: [
    {
      required: true,
      message: '请至少选择一个标签',
      trigger: 'change',
      validator: (rule, value, callback) => {
        if (!value || value.length === 0) {
          callback(new Error('请至少选择一个标签'))
        } else {
          callback()
        }
      },
    },
  ],
}

const tagTree = ref<BookmarkTag[]>([])
const loading = ref(false)

// 加载标签树
const loadTagTree = async () => {
  try {
    tagTree.value = await bookmarkApi.getTagTree()
  } catch (error) {
    ElMessage.error('加载标签树失败')
  }
}

// 初始化表单
const initForm = () => {
  if (props.url) {
    form.value = {
      title: props.url.title,
      url: props.url.url,
      iconUrl: props.url.iconUrl || '',
      description: props.url.description || '',
      tagIds: props.url.tags?.map((tag) => tag.id) || [],
    }
  } else {
    form.value = {
      title: '',
      url: '',
      iconUrl: '',
      description: '',
      tagIds: [],
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
      if (isEdit.value && props.url) {
        await bookmarkApi.updateUrl({
          id: props.url.id,
          title: form.value.title,
          url: form.value.url,
          iconUrl: form.value.iconUrl || undefined,
          description: form.value.description || undefined,
          tagIds: form.value.tagIds,
        })
        ElMessage.success('更新成功')
      } else {
        await bookmarkApi.createUrl({
          title: form.value.title,
          url: form.value.url,
          iconUrl: form.value.iconUrl || undefined,
          description: form.value.description || undefined,
          tagIds: form.value.tagIds,
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
      loadTagTree()
    }
  }
)
</script>

