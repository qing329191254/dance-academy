const fs = require('fs')
const path = require('path')

const file = path.join(__dirname, '..', 'pages', 'login', 'profile.vue')
let content = fs.readFileSync(file, 'utf8')

content = content.replace(
  '            type="nickname"\r\n',
  '            type="text"\r\n'
)

content = content.replace(
  `          <view class="gender-group">
            <view class="gender-item" @click="gender = '男'">
              <view class="radio" :class="{ active: gender === '男' }">
                <text v-if="gender === '男'" class="check">✓</text>
              </view>
              <text>男</text>
            </view>
            <view class="gender-item" @click="gender = '女'">
              <view class="radio" :class="{ active: gender === '女' }">
                <text v-if="gender === '女'" class="check">✓</text>
              </view>
              <text>女</text>
            </view>
          </view>`,
  `          <view class="gender-group">
            <view class="gender-item" hover-class="none" @tap.stop="selectGender('男')">
              <view class="radio" :class="{ active: gender === '男' }">
                <text v-if="gender === '男'" class="check">✓</text>
              </view>
              <text>男</text>
            </view>
            <view class="gender-item" hover-class="none" @tap.stop="selectGender('女')">
              <view class="radio" :class="{ active: gender === '女' }">
                <text v-if="gender === '女'" class="check">✓</text>
              </view>
              <text>女</text>
            </view>
          </view>`
)

content = content.replace(
  'function onBirthdayChange(e) {\r\n  birthday.value = e.detail.value\r\n}\r\n\r\nfunction submit()',
  `function onBirthdayChange(e) {
  birthday.value = e.detail.value
}

function selectGender(value) {
  gender.value = value
}

function submit()`
)

fs.writeFileSync(file, content, 'utf8')
console.log('patched profile.vue')
