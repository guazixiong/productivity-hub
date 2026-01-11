# 数据库脚本执行说明

## ACL 表结构创建

### 方法一：使用批处理脚本（Windows）

直接双击运行：
```
execute_acl_tables.bat
```

### 方法二：使用 MySQL 命令行

```bash
mysql -h 127.0.0.1 -P 3306 -u root -proot productivity_hub < ../base-service-basic/src/main/resources/db/migration/create_acl_tables.sql
```

### 方法三：使用 MySQL 客户端工具

1. 打开 MySQL 客户端（如 Navicat、MySQL Workbench、DBeaver 等）
2. 连接到数据库 `productivity_hub`
3. 打开文件：`productivity-hub-server/base-service-basic/src/main/resources/db/migration/create_acl_tables.sql`
4. 执行整个脚本

### 数据库配置说明

根据 `application-local.yml` 配置：
- **主机**: 127.0.0.1
- **端口**: 3306
- **数据库**: productivity_hub
- **用户名**: root
- **密码**: root

如果您的数据库配置不同，请修改批处理脚本中的相应参数。

### 创建的表

执行脚本后将创建以下表：
1. `acl_menu` - ACL菜单表
2. `acl_role` - ACL角色表
3. `acl_role_menu` - ACL角色-菜单关联表
4. `acl_user_role` - ACL用户-角色关联表

### 注意事项

- 脚本中包含 `DROP TABLE IF EXISTS` 语句，会删除已存在的表（如果存在）
- 请确保在执行前已备份重要数据
- 如果表已存在且包含数据，请谨慎执行

---

## ACL 初始数据创建

执行完表结构创建脚本后，需要执行初始数据脚本以创建菜单和角色。

### 方法一：使用批处理脚本（Windows）

直接双击运行：
```
execute_acl_init_data.bat
```

### 方法二：使用 MySQL 命令行

```bash
mysql -h 127.0.0.1 -P 3306 -u root -proot productivity_hub < ../base-service-basic/src/main/resources/db/migration/init_acl_data.sql
```

### 方法三：使用 MySQL 客户端工具

1. 打开 MySQL 客户端（如 Navicat、MySQL Workbench、DBeaver 等）
2. 连接到数据库 `productivity_hub`
3. 打开文件：`productivity-hub-server/base-service-basic/src/main/resources/db/migration/init_acl_data.sql`
4. 执行整个脚本

### 初始数据说明

执行脚本后将创建以下内容：

1. **菜单数据**：
   - 系统管理（目录）
     - 菜单管理（路径：`/settings/menus`）
     - 角色管理（路径：`/settings/roles`）
     - 用户角色管理（路径：`/settings/user-roles`）

2. **角色数据**：
   - 管理员角色（类型：ADMIN，拥有所有菜单权限）

3. **关联关系**：
   - 将系统管理相关菜单分配给管理员角色

### 将管理员角色分配给用户

**重要**：执行完初始数据脚本后，必须将管理员角色分配给用户，用户才能看到菜单。

#### 方法一：使用批处理脚本（推荐，Windows）

直接双击运行：
```
assign_admin_role.bat
```

脚本会提示您输入用户名，然后自动将管理员角色分配给该用户。

#### 方法二：使用 MySQL 命令行

```bash
# 将 'YOUR_USERNAME' 替换为您的实际用户名
mysql -h 127.0.0.1 -P 3306 -u root -proot productivity_hub -e "INSERT IGNORE INTO acl_user_role (user_id, role_id, created_at) SELECT u.id, r.id, NOW() FROM user u CROSS JOIN acl_role r WHERE u.username = 'YOUR_USERNAME' AND r.name = '管理员' AND r.type = 'ADMIN';"
```

#### 方法三：使用 SQL 脚本

1. 打开 MySQL 客户端（如 Navicat、MySQL Workbench、DBeaver 等）
2. 连接到数据库 `productivity_hub`
3. 打开文件：`productivity-hub-server/base-service-basic/src/main/resources/db/migration/assign_admin_role_to_user.sql`
4. 修改脚本中的 `YOUR_USERNAME` 或 `YOUR_USER_ID` 为您的实际用户名或用户ID
5. 执行脚本

#### 方法四：手动执行 SQL

```sql
-- 方式一：通过用户ID分配（需要知道用户的ID）
INSERT IGNORE INTO acl_user_role (user_id, role_id, created_at)
SELECT '用户ID', id, NOW() FROM acl_role WHERE name = '管理员' AND type = 'ADMIN';

-- 方式二：通过用户名查找用户ID后分配（推荐）
INSERT IGNORE INTO acl_user_role (user_id, role_id, created_at)
SELECT u.id, r.id, NOW() 
FROM user u
CROSS JOIN acl_role r
WHERE u.username = 'YOUR_USERNAME' 
  AND r.name = '管理员' 
  AND r.type = 'ADMIN';

-- 验证：查询用户已分配的角色
SELECT 
    u.id AS user_id,
    u.username,
    r.id AS role_id,
    r.name AS role_name,
    r.type AS role_type
FROM user u
INNER JOIN acl_user_role ur ON u.id = ur.user_id
INNER JOIN acl_role r ON ur.role_id = r.id
WHERE u.username = 'YOUR_USERNAME';
```

#### 查询所有用户（帮助找到用户名）

```sql
SELECT id, username, email, created_at 
FROM user 
ORDER BY created_at DESC;
```

#### 完成后的操作

分配完管理员角色后，请：
1. **退出登录**
2. **重新登录系统**
3. 菜单应该会出现在侧边栏的"系统管理"目录下

### 注意事项

- 初始数据脚本可以重复执行（使用 INSERT 语句，不会删除已有数据）
- 如果菜单或角色已存在，可能会因为唯一约束而报错，这是正常的
- 建议先执行表结构创建脚本，再执行初始数据脚本

