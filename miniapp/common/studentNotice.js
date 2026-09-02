/** 解析行内 **加粗** */
export function parseInlineParts(line) {
  const parts = []
  const re = /\*\*([^*]+)\*\*/g
  let last = 0
  let match
  while ((match = re.exec(line)) !== null) {
    if (match.index > last) {
      parts.push({ bold: false, text: line.slice(last, match.index) })
    }
    parts.push({ bold: true, text: match[1] })
    last = match.index + match[0].length
  }
  if (last < line.length) {
    parts.push({ bold: false, text: line.slice(last) })
  }
  if (!parts.length) {
    parts.push({ bold: false, text: line })
  }
  return parts
}

/** 按换行拆段，保留空行与空格；留空返回 [] */
export function parseStudentNotice(text) {
  if (text == null || !String(text).trim()) return []
  return String(text).split('\n').map((line) => ({
    empty: line.length === 0,
    parts: line.length === 0 ? [] : parseInlineParts(line),
  }))
}
