FROM docker.1panel.live/library/nginx:stable-alpine

# 复制 nginx 反代配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 复制前端构建产物
COPY dist /usr/share/nginx/html

EXPOSE 80