/** 学员须知默认正文（后台未配置时使用）。格式：## 标题 + 正文段落 */
export const DEFAULT_STUDENT_NOTICE = `## 一、预约上课
团课请提前在约课页预约，并选择对应校区。固定班报名后按固定时段上课；私教需与老师协商具体时间。
预约成功后请按时到课。名额已满时可加入排队，前面有人取消将自动替补；可在「排队课程」查看排队进度。团课可在开课前取消；无故缺席或频繁爽约的，机构可限制预约。

## 二、签到与已上课程
到场后请扫描工作人员当场展示的签到码。扫码后需工作人员确认到场才算有效签到，请勿保存二维码远程签到。若未扫码，可由老师或前台在预约名单上手动确认到课。签到记录会同步到「已上课程」与约课排行榜。

## 三、校区与教室
请按预约校区到店。左上角可切换校区查看课表。进入教室请换鞋、保持安静，爱护音响与把杆等设备。

## 四、课程与购课
体验课、固定班、次通卡、私教及定制课程的价格与规则，以门店说明及课程顾问沟通为准。线上展示仅供介绍，购课请联系门店。

## 五、成长中心
新学员默认 T1 权益，可通过年限、考核升级。兼职、实习、演出等机会请按页面提示报名，提交后由机构审核通知。

## 六、安全健康
请根据自身身体状况参加训练。如有伤病或不适，请提前告知老师并量力而行。贵重物品请自行保管。`

export function parseStudentNotice(text) {
  const source = (text || '').trim() || DEFAULT_STUDENT_NOTICE
  const sections = []
  const blocks = source.split(/\n(?=##\s)/)
  for (const block of blocks) {
    const trimmed = block.trim()
    if (!trimmed) continue
    const lines = trimmed.split('\n')
    const heading = lines[0].replace(/^##\s*/, '').trim()
    const paragraphs = lines
      .slice(1)
      .map((line) => line.trim())
      .filter(Boolean)
    if (heading) {
      sections.push({ title: heading, paragraphs })
    }
  }
  return sections.length ? sections : parseStudentNotice(DEFAULT_STUDENT_NOTICE)
}
