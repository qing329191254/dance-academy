<template>
  <div class="upcoming-page page-card">
    <div class="toolbar">
      <div class="filters">
        <el-radio-group v-model="range" @change="load">
          <el-radio-button value="7">近 7 天</el-radio-button>
          <el-radio-button value="15">近 15 天</el-radio-button>
          <el-radio-button value="30">近 30 天</el-radio-button>
          <el-radio-button value="future">未来全部</el-radio-button>
        </el-radio-group>
        <el-button @click="load">刷新</el-button>
      </div>
    </div>

    <div v-if="isMobile" class="mobile-feed">
      <div v-for="row in list" :key="row.userId" class="mobile-feed-item">
        <div class="mobile-feed-head">
          <span class="mobile-feed-title">{{ row.nickname || `学员#${row.userId}` }}</span>
          <span class="mobile-feed-status">{{ row.upcomingCount }} 节</span>
        </div>
        <div
          v-for="cls in row.classes || []"
          :key="cls.bookingId"
          class="mobile-feed-meta"
        >
          <span>{{ cls.classDate }} {{ cls.timeText }}</span>
          <span>{{ cls.name }}</span>
          <span>{{ cls.status }}</span>
        </div>
      </div>
      <div v-if="!list.length" class="mobile-feed-empty">该范围内暂无上课学员</div>
    </div>

    <el-table v-else :data="flatRows" row-key="rowKey">
      <el-table-column prop="nickname" label="学员" width="140" />
      <el-table-column prop="classDate" label="日期" width="120" />
      <el-table-column prop="timeText" label="时间" width="130" />
      <el-table-column prop="name" label="课程" min-width="140" />
      <el-table-column prop="teacherName" label="老师" width="100" />
      <el-table-column prop="campusName" label="校区" width="120" />
      <el-table-column prop="status" label="状态" width="100" />
      <el-table-column label="学员课次" width="90">
        <template #default="{ row }">{{ row.upcomingCount }}</template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import http from '../api/http'
import { useCampusScope } from '../composables/useCampusScope'
import { useBreakpoint } from '../composables/useBreakpoint'

const { isMobile } = useBreakpoint()
const range = ref('7')
const list = ref([])

const flatRows = computed(() => {
  const rows = []
  for (const student of list.value) {
    for (const cls of student.classes || []) {
      rows.push({
        rowKey: `${student.userId}-${cls.bookingId}`,
        userId: student.userId,
        nickname: student.nickname || `学员#${student.userId}`,
        upcomingCount: student.upcomingCount,
        ...cls,
      })
    }
  }
  return rows
})

async function load() {
  const res = await http.get('/admin/upcoming-students', {
    params: { range: range.value, ...campusParams() },
  })
  list.value = res.data?.list || []
}

const { campusParams } = useCampusScope(load)
load()
</script>

<style scoped>
.filters {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
}
</style>
