@echo off
REM ============================================
REM 将管理员角色分配给用户
REM ============================================
echo 正在将管理员角色分配给用户...
echo.

REM 数据库配置（根据 application-local.yml）
set DB_HOST=127.0.0.1
set DB_PORT=3306
set DB_NAME=productivity_hub
set DB_USER=root
set DB_PASSWORD=root

REM 提示用户输入用户名
set /p USERNAME="请输入要分配管理员角色的用户名（例如：admin）: "

if "%USERNAME%"=="" (
    echo 错误: 用户名不能为空
    pause
    exit /b 1
)

echo.
echo 正在为用户 "%USERNAME%" 分配管理员角色...
echo.

REM 检查 MySQL 是否在 PATH 中
where mysql >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo 错误: 未找到 mysql 命令，请确保 MySQL 已安装并添加到 PATH 环境变量中
    echo.
    echo 请手动执行以下 SQL：
    echo INSERT IGNORE INTO acl_user_role (user_id, role_id, created_at)
    echo SELECT u.id, r.id, NOW()
    echo FROM user u
    echo CROSS JOIN acl_role r
    echo WHERE u.username = '%USERNAME%'
    echo   AND r.name = '管理员' 
    echo   AND r.type = 'ADMIN';
    pause
    exit /b 1
)

REM 创建临时 SQL 文件
set TEMP_SQL=%TEMP%\assign_admin_role_%RANDOM%.sql

(
    echo -- 将管理员角色分配给用户: %USERNAME%
    echo INSERT IGNORE INTO acl_user_role (user_id, role_id, created_at^)
    echo SELECT u.id, r.id, NOW^(^)
    echo FROM user u
    echo CROSS JOIN acl_role r
    echo WHERE u.username = '%USERNAME%'
    echo   AND r.name = '管理员' 
    echo   AND r.type = 'ADMIN';
    echo.
    echo -- 验证：查询用户已分配的角色
    echo SELECT 
    echo     u.id AS user_id,
    echo     u.username,
    echo     r.id AS role_id,
    echo     r.name AS role_name,
    echo     r.type AS role_type,
    echo     r.status AS role_status
    echo FROM user u
    echo INNER JOIN acl_user_role ur ON u.id = ur.user_id
    echo INNER JOIN acl_role r ON ur.role_id = r.id
    echo WHERE u.username = '%USERNAME%'
    echo ORDER BY r.type, r.name;
) > "%TEMP_SQL%"

REM 执行 SQL 脚本
mysql -h %DB_HOST% -P %DB_PORT% -u %DB_USER% -p%DB_PASSWORD% %DB_NAME% < "%TEMP_SQL%"

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ============================================
    echo 管理员角色分配成功！
    echo ============================================
    echo.
    echo 已为用户 "%USERNAME%" 分配管理员角色
    echo.
    echo 请重新登录系统以刷新菜单权限
    echo.
) else (
    echo.
    echo ============================================
    echo 管理员角色分配失败，请检查错误信息
    echo ============================================
    echo.
    echo 可能的原因：
    echo 1. 用户名不存在
    echo 2. 管理员角色不存在（请先执行 execute_acl_init_data.bat）
    echo 3. 数据库连接失败
    echo.
)

REM 清理临时文件
del "%TEMP_SQL%" >nul 2>&1

pause

