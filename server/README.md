微信云托管 · 高校FOR-GET舞室

数据库只用 MySQL。当前体量（单店约课、发卡、报名审核）不需要 Redis：
- 登录用 JWT，不依赖集中 Session
- 防超订靠 MySQL 唯一索引 + 名额计数
- 首页课表数据量很小，直接查库即可

等以后出现明显缓存热点或高并发约课，再加 Redis。

本地启动：
1. docker compose up --build
2. 后端 http://localhost:8080
3. 管理后台 cd admin && npm install && npm run dev  → http://localhost:5173
4. 后台账号 admin / admin123

本地连接配置写在项目根目录 `.env`。Docker 里的 MySQL 会按 `MYSQL_DATABASE` 自动建库，本机不用再装一份。

云托管环境变量：
- MYSQL_ADDRESS  例如 10.x.x.x:3306
- MYSQL_USERNAME
- MYSQL_PASSWORD
- MYSQL_DATABASE  dance_academy
- WX_APPID / WX_SECRET  小程序登录（必填，走微信 code2session）
- JWT_SECRET
- ADMIN_USERNAME / ADMIN_PASSWORD
