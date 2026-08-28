# 阿里云 ECS 部署

生产环境使用 Docker Compose 运行 MySQL 与 Spring Boot，宿主机 Nginx 负责 HTTPS。

## 目录约定

- 项目：`/opt/dance-academy`
- Nginx 配置：`/etc/nginx/conf.d/forgetdance.top.conf`
- SSL 证书：`/etc/nginx/ssl/forgetdance.top.pem`
- SSL 私钥：`/etc/nginx/ssl/forgetdance.top.key`

## 启动应用

在项目根目录准备权限为 `600` 的 `.env` 后执行：

```bash
sudo docker compose config --quiet
sudo docker compose up -d --build
sudo docker compose ps
```

## 安装 Nginx 配置

```bash
sudo dnf install -y nginx
sudo mkdir -p /etc/nginx/ssl
sudo cp deploy/nginx/forgetdance.top.conf /etc/nginx/conf.d/forgetdance.top.conf
sudo nginx -t
sudo systemctl enable --now nginx
```

运行 `nginx -t` 前必须先上传证书和私钥到约定路径。

## 验证

```bash
curl -fsS http://127.0.0.1:8080/api/app/course-intro
curl -I https://forgetdance.top
```

数据库与上传文件位于 Docker volumes `academy-mysql`、`academy-uploads`，必须定期备份。
