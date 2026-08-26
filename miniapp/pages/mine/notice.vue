<template>
  <page-meta root-background-color="#111111" background-color="#111111" page-style="background-color:#111111;" />
  <view class="page">
    <view class="section">
      <view class="card doc">
        <text class="doc-title">学员须知</text>
        <text class="doc-meta muted">{{ info.brand }} · 请在上课与报名前提前阅读</text>

        <text class="h">一、预约上课</text>
        <text class="p">团课请提前在约课页预约，并选择对应校区。固定班报名后按固定时段上课；私教需与老师协商具体时间。</text>
        <text class="p">预约成功后请按时到课。名额已满时可加入排队，前面有人取消将自动替补；可在「排队课程」查看排队进度。团课可在开课前取消；无故缺席或频繁爽约的，机构可限制预约。</text>

        <text class="h">二、签到与习练</text>
        <text class="p">请在教室扫描课堂签到码完成签到。同一节课当日不可重复签到；若未扫码，可由老师或前台在预约名单上手动确认到课。签到记录会同步到「习练」与约课排行榜。</text>

        <text class="h">三、校区与教室</text>
        <text class="p">请按预约校区到店。左上角可切换校区查看课表。进入教室请换鞋、保持安静，爱护音响与把杆等设备。</text>

        <text class="h">四、课程与购课</text>
        <text class="p">体验课、固定班、次通卡、私教及定制课程的价格与规则，以门店说明及课程顾问沟通为准。线上展示仅供介绍，购课请联系门店。</text>

        <text class="h">五、成长中心</text>
        <text class="p">新学员默认 T1 权益，可通过年限、考核升级。兼职、实习、演出等机会请按页面提示报名，提交后由机构审核通知。</text>

        <text class="h">六、安全健康</text>
        <text class="p">请根据自身身体状况参加训练。如有伤病或不适，请提前告知老师并量力而行。贵重物品请自行保管。</text>

        <text class="h">七、联系我们</text>
        <text class="p">运营主体：{{ info.company }}</text>
        <text class="p">联系电话：{{ info.phone }}</text>
        <text class="p">联系邮箱：{{ info.email }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { legalInfo } from '@/common/legal.js'

const info = legalInfo
</script>

<style scoped>
.doc {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.doc-title {
  font-size: 40rpx;
  font-weight: 700;
}

.doc-meta {
  font-size: 24rpx;
  margin-bottom: 12rpx;
}

.h {
  display: block;
  font-size: 30rpx;
  font-weight: 600;
  margin-top: 20rpx;
}

.p {
  display: block;
  font-size: 26rpx;
  line-height: 1.8;
  color: #cccccc;
}
</style>
