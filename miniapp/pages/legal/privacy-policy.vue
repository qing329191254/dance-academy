<template>
  <page-meta root-background-color="#111111" background-color="#111111" page-style="background-color:#111111;" />
  <view class="page">
    <view class="section">
      <view class="card doc">
        <text class="doc-title">隐私政策</text>
        <text class="doc-meta muted">更新日期：{{ info.updateDate }}</text>

        <text class="p">{{ info.company }}（以下简称「我们」）非常重视您的个人信息保护。本政策说明我们在「{{ info.brand }}」小程序中如何收集、使用、存储和保护您的个人信息。</text>

        <text class="h">一、我们收集的信息</text>
        <text class="p">1. 微信登录信息：包括微信登录凭证、openid 等，用于识别您的账号。</text>
        <text class="p">2. 个人资料：包括您授权或自行填写的微信头像、昵称、性别，以及选填的生日，用于完善账号资料与展示。</text>
        <text class="p">3. 预约与上课信息：包括约课记录、签到记录、会员卡使用情况等，用于提供服务。</text>
        <text class="p">4. 设备信息：包括设备型号、操作系统版本、网络状态等，用于保障服务安全稳定、适配页面显示。</text>
        <text class="p">5. 相机权限：在您使用扫码签到功能时，用于扫描二维码完成到课签到。我们不会将相机用于其他用途。</text>
        <text class="p">6. 订阅消息：在您预约课程并同意后，用于向您发送课程预约相关的服务通知。</text>

        <text class="h">二、信息使用目的</text>
        <text class="p">我们收集的信息将用于：提供登录与账号服务；完成课程预约、签到及习练记录；向您发送课程提醒与服务通知；改进产品体验；满足法律法规及监管要求。</text>

        <text class="h">三、信息共享与披露</text>
        <text class="p">未经您同意，我们不会向第三方出售您的个人信息。仅在以下情形可能共享：经您明确授权；为完成服务所必需的合作伙伴（如云服务托管），且其须承担保密义务；法律法规要求或有权机关依法调取。</text>

        <text class="h">四、信息存储与保护</text>
        <text class="p">您的信息存储于中华人民共和国境内。我们采取合理的技术和管理措施防止信息泄露、损毁或丢失。除非法律另有规定，我们仅在实现目的所必需的期限内保留您的信息。</text>

        <text class="h">五、您的权利</text>
        <text class="p">您有权查询、更正您的个人信息，有权撤回授权、注销账号或要求我们删除相关信息（法律法规另有规定的除外）。您可通过本政策底部联系方式向我们提出申请。</text>

        <text class="h">六、未成年人保护</text>
        <text class="p">若您为未成年人，请在监护人陪同下阅读本政策，并在取得监护人同意后使用我们的服务。</text>

        <text class="h">七、政策更新</text>
        <text class="p">我们可能适时修订本政策。重大变更将通过小程序公告等方式提示您。继续使用服务即表示您接受更新后的政策。</text>

        <text class="h">八、联系我们</text>
        <text class="p">个人信息保护负责人联系渠道：</text>
        <text class="p">运营主体：{{ info.company }}</text>
        <text class="p">联系电话：{{ info.phone }}</text>
        <text class="p">联系邮箱：{{ info.email }}</text>
        <text class="p">地址：{{ info.address }}</text>
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
