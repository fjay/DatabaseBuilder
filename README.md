DatabaseBuilder
===============
DatabaseBuilder以一套统一的、简单的文本结构，构建主流数据库的表与数据。

## REQUIREMENT
* 基于纯文本格式，能够快速编辑表结构与数据，同时可以在版本控制中保留清晰的改动记录
* 应用运行在JVM，支持主流操作系统
* 根据需要构建指定的表测试数据，可以快速模拟出完整的测试数据库环境

## FEATURE
* 根据指定模板,生成表对应的实体类
* 生成指定数据库的表结构,支持数据库:
    * DB2
    * PostgreSql
    * Oracle
    * MS-SqlServer
    * MySql
    * H2
    * SQLITE
    * HSQL
    * DERBY
    * GBASE
    * SYBASE

* 生成指定表的数据记录,支持数据库:
  * HSQL
  * MySql

* 表数据文本格式支持动态表达式,支持自定义表达式
* 反向生成 DatabaseBuilder 文本结构,支持数据库:
  * HSQL
  * MySql

* 支持类库方式嵌入应用使用
* 支持命令行方式使用
* 生成markdown格式的表结构(TODO)

### ENVIRONMENT

* JDK 6+

## HOW TO USE

### Core

core项目提供了标准的API,供外部调用

#### 添加依赖包

Maven

```
<dependency>
    <groupId>org.team4u.dbb</groupId>
     <artifactId>database-builder-core</artifactId>
    <version>1.0.1</version>
</dependency>
```

```
// 初始化DatabaseBuilder
DatabaseBuilder builder = new DatabaseBuilder(dataSource())

// TODO 补充API说明文档
```

### Command

api-command项目提供了命令行方式调用Core项目的API

#### 打包安装

```
git clone
mvn clean install -Dmaven.test.skip=true

cd api-command
mvn clean package -Dmaven.test.skip=true
cd target/database-builder-command-*-bin/database-builder-command-*
```

#### 项目结构
```
config
lib
database-builder-command-*.jar
dbb.bat
dbb.sh
```

#### 配置文件

config/config.properties

```
# 表结构文件,支持相对路径（相对于应用目录）,支持绝对路径
tableFilePath=config/tables.yml
# 类所在包路径
tableClassPackage=com.asiainfo.test.entity
# 类模板文件,可选,需要定制生成类时使用,留空采用默认模板
tableClassTemplatePath=
# 类生成目录
tableClassPath=~/tmp/entity

# 数据来源文件,支持相对路径（相对于应用目录）,支持绝对路径
recordFilePath=config/records.yml
# 反向生成文件结构保存路径
tableDocumentPath=~/tmp/tables.yml

# 数据库配置
jdbc.url=jdbc:hsqldb:mem:db
jdbc.username=sa
jdbc.password=sa
```
#### JDBC依赖包

 应用默认只带了Hsqldb和MySql的jdbc依赖包,若需要支持其他数据库,请将对应的jdbc依赖包复制到lib文件夹

#### 使用方法

Window:

```
dbb.bat -h
```

Linux:

```
./dbb.sh -h
```

支持参数:

```
usage: dbb [-c <arg>] [-crd <arg>] [-ct <arg>] [-ctc <arg>] [-ctd <arg>] [-fd] [-h]
 -c,--config <arg>                     指定配置文件
 -crd,--create-record-document <arg>   反向生成数据文本结构
 -ct,--create-table <arg>              生成指定数据库的表结构
 -ctc,--create-table-class <arg>       生成表对应的实体类
 -ctd,--create-table-document <arg>    反向生成表结构文本结构
 -fd,--fill-data                       生成指定表的数据记录
 -h,--help                             帮助说明                   帮助说明
```

### 文本结构
#### 表
```
tables:
- name : TEST
  comment : 测试表
  columns : |
    NAME        |TYPE    |LENGTH|NULLABLE|COMMENT
    NAME        |VARCHAR |32    |0       |名称
    AGE         |INT     |5     |0       |年龄
    AMOUNT      |FLOAT   |10,2  |1       |金额
    CREATED_TIME|DATETIME|      |1       |创建时间
  indexes:
  - type : PK
    columns :
    - NAME

  - type : INDEX
    name : IDX_AGE
    columns :
    - AGE
    - CREATED_TIME

- name : TEST2
  comment : 测试表2
  columns : |
    NAME        |TYPE    |LENGTH|COMMENT
    NAME        |VARCHAR |32    |名称
    AGE         |INT     |5     |年龄
    AMOUNT      |FLOAT   |10,2  |金额
    CREATED_TIME|DATETIME|      |创建时间
```
#### 数据
```
records:
  - table : TEST
    loadMethod : CLEAR_AND_INSERT
    data : |
      NAME          |AGE|AMOUNT|CREATED_TIME
      Strings.uuid()|1  |1     |Dates.now()
      Strings.uuid()|2  |10.2  |Dates.now("yyyy-MM-dd")
  - table : TEST2
    loadMethod : CLEAR_AND_INSERT
    data : |
      NAME|AGE|AMOUNT|CREATED_TIME
      a   |1  |1     |2015-10-01
      b   |2  |10.2  |
```