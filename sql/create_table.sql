-- 创建库
create database if not exists yeying_BI;

-- 切换库
use yeying_BI;
-- 用户表
create table if not exists user
(
    id           bigint auto_increment comment '用户id' primary key,
    userAccount  varchar(256)                           not null comment '账号',
    userPassword varchar(512)                           not null comment '密码',
    userName     varchar(256)                           null comment '用户昵称',
    userAvatar   varchar(1024)                          null comment '用户头像',
    userRole     varchar(256) default 'user'            not null comment '用户角色：user/admin/ban',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除',
    index idx_uid (id),
    index idx_account (userAccount)
) comment '用户表' collate = utf8mb4_unicode_ci;

-- 图表信息表
create table if not exists chart_info
(
    id         bigint auto_increment comment '图表id' primary key,
    uid        bigint                             not null comment '用户ID',
    goal       text                               null comment '目的',
    `name`     varchar(128)                       null comment '图标名称',
    chartData  text                               not null comment '表格数据',
    chartType  varchar(128)                       null comment '要生成的表格类型',
    genResult  text                               null comment '生成结果',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '是否删除',
    index idx_chartid (id),
    index inx_uid (uid)
) comment '图标信息表' collate = utf8mb4_unicode_ci;

-- 创建库
create database if not exists yeying_user_upload_table;
-- 切换库
use yeying_user_upload_table;
-- 用户表格元数据
drop table if exists user_chart_info;
create table user_chart_info
(
    id          bigint auto_increment comment '图表id' primary key,
    fieldsName  varchar(1024)                      not null comment '表格对应的字段名称',
    `rowNum`    int comment '行数',
    `columnNum` int comment '列数',
    parentId      bigint   default 0 comment '前一次生成的图表Id',
    createTime  datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime  datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete    tinyint  default 0                 not null comment '是否删除'
) comment '用户表格元数据' collate = utf8mb4_unicode_ci;
