@echo off
REM ============================================
REM 执行 ACL 初始数据脚本
REM ============================================
echo 正在执行 ACL 初始数据脚本...
echo.

REM 数据库配置（根据 application-local.yml）
set DB_HOST=127.0.0.1
set DB_PORT=3306
set DB_NAME=productivity_hub
set DB_USER=root
set DB_PASSWORD=root

REM SQL 脚本路径
set SQL_FILE=%~dp0..\..\base-service-basic\src\main\resources\db\migration\init_acl_data.sql

REM 检查 MySQL 是否在 PATH 中
where mysql >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo 错误: 未找到 mysql 命令，请确保 MySQL 已安装并添加到 PATH 环境变量中
    echo 或者手动执行以下命令：
    echo mysql -h %DB_HOST% -P %DB_PORT% -u %DB_USER% -p%DB_PASSWORD% %DB_NAME% ^< "%SQL_FILE%"
    pause
    exit /b 1
)

REM 检查 SQL 文件是否存在
if not exist "%SQL_FILE%" (
    echo 错误: SQL 文件不存在: %SQL_FILE%
    pause
    exit /b 1
)

REM 执行 SQL 脚本
echo 正在连接数据库: %DB_NAME%@%DB_HOST%:%DB_PORT%
echo 执行 SQL 脚本: %SQL_FILE%
echo.

mysql -h %DB_HOST% -P %DB_PORT% -u %DB_USER% -p%DB_PASSWORD% %DB_NAME% < "%SQL_FILE%"

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ============================================
    echo ACL 初始数据创建成功！
    echo ============================================
    echo.
    echo 已创建以下内容：
    echo 1. 系统管理目录
    echo 2. 菜单管理、角色管理、用户角色管理菜单
    echo 3. 管理员角色（类型：ADMIN）
    echo 4. 菜单与角色的关联关系
    echo.
    echo 注意：如果需要将管理员角色分配给用户，请手动执行：
    echo INSERT INTO acl_user_role (user_id, role_id, created_at)
    echo SELECT '用户ID', id, NOW() FROM acl_role WHERE name = '管理员';
    echo.
) else (
    echo.
    echo ============================================
    echo ACL 初始数据创建失败，请检查错误信息
    echo ============================================
)

pause

