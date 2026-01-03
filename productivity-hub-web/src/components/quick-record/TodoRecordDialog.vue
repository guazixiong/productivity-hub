<template>
  <el-dialog
    v-model="dialogVisible"
    title="新增待办"
    :width="isMobile ? '90%' : '500px'"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="100px"
    >
      <el-form-item label="待办模块" prop="moduleId">
        <el-select
          v-model="form.moduleId"
          placeholder="请选择待办模块"
          style="width: 100%"
          :loading="loadingModules"
        >
          <el-option
            v-for="module in modules"
            :key="module.id"
            :label="module.name"
            :value="module.id"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="待办事项" prop="title">
        <el-input
          v-model="form.title"
          placeholder="请输入待办事项"
          maxlength="200"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="截止时间" prop="dueDate">
        <el-date-picker
          v-model="form.dueDate"
          type="datetime"
          placeholder="选择截止时间"
          format="YYYY-MM-DD HH:mm"
          value-format="YYYY-MM-DDTHH:mm:ss"
          style="width: 100%"
        />
      </el-form-item>

      <el-form-item label="分类标签" prop="tags">
        <el-select
          v-model="form.tags"
          placeholder="可选，选择分类标签"
          multiple
          filterable
          allow-create
          default-first-option
          style="width: 100%"
        >
          <el-option label="工作" value="工作" />
          <el-option label="学习" value="学习" />
          <el-option label="生活" value="生活" />
        </el-select>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSubmit">
        保存
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
/**
 * 新增待办记录弹窗组件
 */

import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { todoApi } from '@/services/todoApi'
import type { TodoModule, TodoTaskCreateDTO } from '@/types/todo'

interface Props {
  visible: boolean
}

interface Emits {
  (e: 'update:visible', value: boolean): void
  (e: 'close'): void
  (e: 'success'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const dialogVisible = computed({
  get: () => props.visible,
  set: (value) => emit('update:visible', value),
})

const isMobile = computed(() => window.innerWidth < 768)

const formRef = ref<FormInstance>()
const modules = ref<TodoModule[]>([])
const loadingModules = ref(false)
const submitting = ref(false)

const form = reactive<TodoTaskCreateDTO & { tags: string[] }>({
  title: '',
  moduleId: '',
  priority: 'P3',
  tags: [],
  dueDate: '',
})

const rules: FormRules = {
  moduleId: [
    { required: true, message: '请选择待办模块', trigger: 'change' },
  ],
  title: [
    { required: true, message: '请输入待办事项', trigger: 'blur' },
    { min: 1, max: 200, message: '长度在 1 到 200 个字符', trigger: 'blur' },
  ],
}

const loadModules = async () => {
  try {
    loadingModules.value = true
    modules.value = await todoApi.listModules()
  } catch (error: any) {
    ElMessage.error(error.message || '加载模块列表失败')
  } finally {
    loadingModules.value = false
  }
}

const handleClose = () => {
  emit('update:visible', false)
  emit('close')
  formRef.value?.resetFields()
  Object.assign(form, {
    title: '',
    moduleId: '',
    priority: 'P3',
    tags: [],
    dueDate: '',
  })
}

const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    submitting.value = true

    const dto: TodoTaskCreateDTO = {
      title: form.title,
      moduleId: form.moduleId,
      priority: form.priority,
      tags: form.tags,
      dueDate: form.dueDate || undefined,
    }

    await todoApi.createTask(dto)
    ElMessage.success('记录成功')
    emit('success')
    handleClose()
  } catch (error: any) {
    if (error.message) {
      ElMessage.error(error.message)
    }
  } finally {
    submitting.value = false
  }
}

watch(() => props.visible, (visible) => {
  if (visible) {
    loadModules()
  }
})

onMounted(() => {
  if (props.visible) {
    loadModules()
  }
})
</script>

<style scoped lang="scss">
// 响应式设计已在模板中通过isMobile处理
</style>

