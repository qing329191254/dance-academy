<template>
  <el-alert
    v-if="campusFiltered"
    type="info"
    :closable="false"
    show-icon
    class="org-wide-notice"
    title="当前为全机构配置，不受顶部校区筛选影响"
  />
</template>

<script setup>
import { computed } from 'vue'
import { storeToRefs } from 'pinia'
import { useCampusStore } from '../stores/campus'

const campusStore = useCampusStore()
const { campusId } = storeToRefs(campusStore)
const campusFiltered = computed(() => !!campusId.value)
</script>

<style scoped>
.org-wide-notice {
  margin-bottom: 16px;
}
</style>
