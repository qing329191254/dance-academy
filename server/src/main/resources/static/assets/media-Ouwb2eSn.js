function s(t){return t?/^https?:\/\//i.test(t)||t.startsWith("data:")||t.startsWith("blob:")||t.startsWith("/uploads/")||t.startsWith("/logo.png")?t:t.startsWith("/static/")?"":t:""}export{s as m};
