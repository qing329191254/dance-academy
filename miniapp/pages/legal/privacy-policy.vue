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
        <text class="p">2. 个人资料：包括您授权或自行填写的头像、真实姓名、性别、生日、手机号码，以及就读学校、学院年级等信息，用于完善学员资料、预约联系与服务展示。</text>
        <text class="p">3. 预约与上课信息：包括约课记录、签到记录、会员卡使用情况等，用于提供服务。</text>
        <text class="p">4. 选中的照片或视频：在您设置头像、上传教师简历或成长机会报名资料时，我们仅获取您主动选择或拍摄的照片、视频或文件，不会后台自动读取相册。</text>
        <text class="p">5. 教师/员工资料（如适用）：在您使用教师简历等功能时，可能收集您主动填写的自我介绍及上传的照片、视频，用于教师档案展示与内部管理。</text>
        <text class="p">6. 设备信息：包括设备型号、操作系统版本、网络状态等，用于保障服务安全稳定、适配页面显示。</text>
        <text class="p">7. 相机权限：在您使用扫码签到时扫描二维码；在您主动拍摄教师简历视频时用于录制画面。我们不会在其他场景后台启用相机。</text>
        <text class="p">8. 麦克风权限：仅在您主动拍摄教师简历展示视频时，用于录制声音。我们不会在其他场景后台启用麦克风。</text>
        <text class="p">9. 订阅消息：在您预约课程并同意后，用于向您发送课程预约相关的服务通知。</text>
        <text class="p">10. 意见反馈：在您提交意见反馈时，可能收集您填写的反馈内容及联系方式，用于问题回访与服务改进。</text>

        <text class="h">二、信息使用目的</text>
        <text class="p">我们收集的信息将用于：提供登录与账号服务；完善学员资料并通过您提供的手机号联系您处理预约、上课及相关服务事宜；完成课程预约、签到及已上课程记录；向您发送课程提醒与服务通知；处理教师简历与成长机会报名资料；改进产品体验；满足法律法规及监管要求。</text>

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
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getLegalInfo, loadLegalInfo } from '@/common/legal.js'
import { selectedCampusId } from '@/common/campus.js'

const info = ref(getLegalInfo())

onShow(async () => {
  await loadLegalInfo(selectedCampusId.value)
  info.value = getLegalInfo()
})
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
