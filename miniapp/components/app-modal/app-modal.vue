<template>
  <view v-if="show" class="modal-mask" @click="onMask">
    <view class="modal-panel" @click.stop>
      <text v-if="title" class="modal-title">{{ title }}</text>
      <text v-if="content" class="modal-content">{{ content }}</text>
      <view class="modal-actions" :class="{ single: !showCancel }">
        <view v-if="showCancel" class="modal-btn cancel" @click="close">{{ cancelText }}</view>
        <view class="modal-btn confirm" @click="confirm">{{ confirmText }}</view>
      </view>
    </view>
  </view>
</template>

<script setup>
const props = defineProps({
  show: {
    type: Boolean,
    default: false,
  },
  title: {
    type: String,
    default: '',
  },
  content: {
    type: String,
    default: '',
  },
  confirmText: {
    type: String,
    default: '确定',
  },
  cancelText: {
    type: String,
    default: '取消',
  },
  showCancel: {
    type: Boolean,
    default: false,
  },
  maskClosable: {
    type: Boolean,
    default: false,
  },
})

const emit = defineEmits(['update:show', 'confirm', 'cancel'])

function close() {
  emit('update:show', false)
  emit('cancel')
}

function confirm() {
  emit('update:show', false)
  emit('confirm')
}

function onMask() {
  if (props.maskClosable) close()
}
</script>

<style scoped>
.modal-mask {
  position: fixed;
  inset: 0;
  z-index: 2000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48rpx;
  background: rgba(0, 0, 0, 0.72);
}

.modal-panel {
  width: 100%;
  max-width: 560rpx;
  background: #1c1c1c;
  border-radius: 24rpx;
  border: 1rpx solid #2e2e2e;
  overflow: hidden;
  box-shadow: 0 24rpx 80rpx rgba(0, 0, 0, 0.45);
}

.modal-title {
  display: block;
  padding: 48rpx 40rpx 20rpx;
  text-align: center;
  font-size: 34rpx;
  font-weight: 700;
  color: #ffffff;
}

.modal-content {
  display: block;
  padding: 0 40rpx 40rpx;
  text-align: center;
  font-size: 28rpx;
  line-height: 1.7;
  color: #9a9a9a;
}

.modal-actions {
  display: flex;
  border-top: 1rpx solid #2a2a2a;
}

.modal-actions.single .confirm {
  flex: 1;
}

.modal-btn {
  flex: 1;
  padding: 28rpx 0;
  text-align: center;
  font-size: 30rpx;
}

.cancel {
  color: #9a9a9a;
  border-right: 1rpx solid #2a2a2a;
}

.confirm {
  color: #8a74e5;
  font-weight: 600;
}
</style>
