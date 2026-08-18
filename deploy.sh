#!/bin/bash
# BZY 外卖系统 - 一键部署脚本（服务器端）
# 使用方法：在服务器上执行 bash deploy.sh

set -e

echo "========================================"
echo "  BZY 外卖系统 - 一键部署脚本"
echo "========================================"
echo ""

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 检查是否以 root 用户运行
if [ "$EUID" -ne 0 ]; then 
  echo -e "${RED}请使用 root 用户运行此脚本${NC}"
  exit 1
fi

# 项目目录
PROJECT_DIR="/opt/bzy-takeaway"
UPLOADS_DIR="$PROJECT_DIR/uploads"

echo -e "${GREEN}>>> 检查 Docker 安装...${NC}"
if ! command -v docker &> /dev/null; then
    echo -e "${YELLOW}Docker 未安装，开始安装 Docker...${NC}"
    curl -fsSL https://get.docker.com | bash -s docker
    systemctl enable docker
    systemctl start docker
    echo -e "${GREEN}✓ Docker 安装完成${NC}"
else
    echo -e "${GREEN}✓ Docker 已安装${NC}"
fi

echo ""
echo -e "${GREEN}>>> 检查 Docker Compose 安装...${NC}"
if ! command -v docker-compose &> /dev/null; then
    echo -e "${YELLOW}Docker Compose 未安装，开始安装...${NC}"
    curl -L "https://github.com/docker/compose/releases/download/v2.20.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
    chmod +x /usr/local/bin/docker-compose
    echo -e "${GREEN}✓ Docker Compose 安装完成${NC}"
else
    echo -e "${GREEN}✓ Docker Compose 已安装${NC}"
fi

echo ""
echo -e "${GREEN}>>> 创建项目目录...${NC}"
mkdir -p "$PROJECT_DIR"
mkdir -p "$UPLOADS_DIR"
chmod -R 777 "$UPLOADS_DIR"
echo -e "${GREEN}✓ 项目目录创建完成：$PROJECT_DIR${NC}"

echo ""
echo -e "${GREEN}>>> 检查项目文件...${NC}"
if [ ! -f "$PROJECT_DIR/docker-compose.yml" ]; then
    echo -e "${RED}✗ 项目文件不存在，请先上传项目到 $PROJECT_DIR${NC}"
    echo -e "${YELLOW}提示：可以使用 scp 或 git clone 上传项目${NC}"
    exit 1
fi
echo -e "${GREEN}✓ 项目文件检查通过${NC}"

echo ""
echo -e "${GREEN}>>> 配置环境变量...${NC}"
cd "$PROJECT_DIR"

if [ -f ".env.prod" ]; then
    cp .env.prod .env
    echo -e "${GREEN}✓ 已复制 .env.prod 到 .env${NC}"
elif [ -f ".env" ]; then
    echo -e "${GREEN}✓ .env 文件已存在${NC}"
else
    echo -e "${YELLOW}⚠ 未找到 .env 文件，将使用默认配置${NC}"
    cat > .env << EOF
# 生产环境配置
VITE_API_BASE_URL=http://8.138.37.105:8080/api
MYSQL_ROOT_PASSWORD=Prod@2024Root#Secure9527
MYSQL_HOST=localhost
MYSQL_PORT=3306
MYSQL_DATABASE=bzy_takeaway
MYSQL_USERNAME=bzy_takeaway_user
MYSQL_PASSWORD=Bzy@2024Takeaway#Secure9527
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=Redis@2024Secure#7788
JWT_SECRET=Xk9#mP2$vL5@nQ8wR3&tY6*uI1^oA4sD7fG0hJ
SMS_DEV_MODE=true
EOF
    echo -e "${GREEN}✓ 已创建默认 .env 文件${NC}"
fi

echo ""
echo -e "${GREEN}>>> 停止旧容器...${NC}"
docker-compose down || true
echo -e "${GREEN}✓ 旧容器已停止${NC}"

echo ""
echo -e "${GREEN}>>> 构建前端资源...${NC}"
if [ -d "frontend" ] && [ -f "frontend/package.json" ]; then
    cd frontend
    if command -v npm &> /dev/null; then
        echo -e "${YELLOW}检测到 Node.js，开始构建前端...${NC}"
        npm install
        npm run build:prod
        echo -e "${GREEN}✓ 前端构建完成${NC}"
    else
        echo -e "${YELLOW}⚠ 未检测到 Node.js，跳过前端构建${NC}"
        echo -e "${YELLOW}提示：如果前端已构建，可以跳过此步骤${NC}"
    fi
    cd ..
else
    echo -e "${YELLOW}⚠ 未找到 frontend 目录，跳过前端构建${NC}"
fi

echo ""
echo -e "${GREEN}>>> 构建后端应用...${NC}"
if [ -f "pom.xml" ]; then
    if command -v mvn &> /dev/null; then
        echo -e "${YELLOW}检测到 Maven，开始构建后端...${NC}"
        mvn clean package -DskipTests
        echo -e "${GREEN}✓ 后端构建完成${NC}"
    else
        echo -e "${YELLOW}⚠ 未检测到 Maven，将使用已有的 JAR 包或直接运行${NC}"
    fi
else
    echo -e "${YELLOW}⚠ 未找到 pom.xml，跳过 Maven 构建${NC}"
fi

echo ""
echo -e "${GREEN}>>> 启动 Docker 容器...${NC}"
cd "$PROJECT_DIR"
docker-compose up -d

echo ""
echo -e "${GREEN}>>> 等待服务启动...${NC}"
sleep 10

echo ""
echo -e "${GREEN}>>> 检查容器状态...${NC}"
docker-compose ps

echo ""
echo -e "${GREEN}>>> 查看应用日志...${NC}"
docker-compose logs --tail=20 backend

echo ""
echo -e "${GREEN}========================================"
echo "  部署完成！"
echo "========================================${NC}"
echo ""
echo -e "${GREEN}访问地址：http://8.138.37.105:8080${NC}"
echo ""
echo -e "${YELLOW}默认账号：${NC}"
echo "  管理员：13800000000 / 123456"
echo "  商家：13900000000 / 123456"
echo ""
echo -e "${YELLOW}常用命令：${NC}"
echo "  查看日志：docker-compose logs -f"
echo "  重启服务：docker-compose restart"
echo "  停止服务：docker-compose down"
echo ""
echo -e "${GREEN}祝使用愉快！🎉${NC}"
