<template>
  <page-meta root-background-color="#111111" background-color="#111111" page-style="background-color:#111111;" />
  <view class="page">
    <view class="section">
      <view class="card doc">
        <text class="doc-title">用户协议</text>
        <text class="doc-meta muted">更新日期：{{ info.updateDate }}</text>

        <text class="h">一、协议说明</text>
        <text class="p">欢迎使用「{{ info.brand }}」小程序。本协议由{{ info.company }}（以下简称「我们」）与使用本服务的用户（以下简称「您」）共同订立。请您在登录或使用服务前仔细阅读本协议。</text>

        <text class="h">二、服务内容</text>
        <text class="p">本小程序向您提供街舞课程浏览、约课预约、扫码签到、会员卡包、成长中心及相关机构服务信息展示等功能。具体以页面实际提供的服务为准。</text>

        <text class="h">三、账号与登录</text>
        <text class="p">您可通过微信授权登录使用本服务。您应保证授权信息真实有效，并妥善保管账号使用权。因您自身原因导致的账号风险由您自行承担。</text>

        <text class="h">四、预约与上课</text>
        <text class="p">您通过本小程序预约课程后，应按约定时间到课。如需取消或改期，请遵守机构公布的预约规则。无故缺席、频繁爽约的，我们有权限制预约权限。</text>

        <text class="h">五、会员卡与费用</text>
        <text class="p">会员卡、课程包的有效期、次数、转让及退款规则，以购买时公示说明及线下签署材料为准。线上信息仅供查询，不构成单独的合同变更。</text>

        <text class="h">六、用户行为规范</text>
        <text class="p">您不得利用本小程序从事违法违规活动，不得干扰系统正常运行，不得冒用他人身份预约或签到。我们有权对违规行为采取警告、限制功能、终止服务等措施。</text>

        <text class="h">七、知识产权</text>
        <text class="p">本小程序内的文字、图片、标识、课程资料等知识产权归我们或相关权利人所有，未经授权不得擅自使用、复制或传播。</text>

        <text class="h">八、免责声明</text>
        <text class="p">因网络故障、系统维护、不可抗力等导致服务中断或数据异常的，我们将尽力修复，但在法律允许范围内不承担由此产生的间接损失。参加舞蹈训练前，请根据自身健康状况谨慎参与。</text>

        <text class="h">九、协议变更</text>
        <text class="p">我们可能根据业务需要更新本协议，并通过小程序公告或页面提示等方式通知您。更新后继续使用服务的，视为您接受变更后的协议。</text>

        <text class="h">十、联系我们</text>
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
