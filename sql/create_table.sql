-- 创建库
create database if not exists yeying_BI;

-- 切换库
use yeying_BI;
-- 用户表
drop table if exists user;
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
drop table if exists chart_info;
create table if not exists chart_info
(
    id          bigint auto_increment comment '图表id' primary key,
    uid         bigint                             not null comment '用户ID',
    goal        text                               null comment '目的',
    `name`      varchar(128)                       null comment '图标名称',
    chartType   varchar(128)                       null comment '要生成的表格类型',
    genResult   text                               null comment '生成结果',
    `status`    int                                not null default 0 comment '状态信息 0-等待中 1-正在执行 2-执行成功 3-执行失败',
    execMessage text                               null comment '执行信息',
    createTime  datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime  datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete    tinyint  default 0                 not null comment '是否删除',
    index idx_chartid (id),
    index inx_uid (uid)
) comment '图标信息表' collate = utf8mb4_unicode_ci;


-- 用户调用接口关系表
drop table if exists user_interface_info;
create table user_interface_info
(
    `id`          bigint auto_increment primary key comment '主键',
    `userId`      bigint                             not null comment '用户ID',
    `interfaceId` bigint                             not null comment '接口ID',
    `status`      int      default 0                 not null comment '状态 0-正常 1-禁用',
    `totalNum`    int      default 0 comment '总调用次数',
    `surplusNum`  int      default 0 comment '剩余调用次数',
    `createTime`  datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    `updateTime`  datetime default CURRENT_TIMESTAMP not null on update current_timestamp comment '更新时间',
    `isDelete`    tinyint  default 0                 not null comment '是否删除（0-未删  1-以删'
) comment '用户调用接口关系' collate = utf8mb4_unicode_ci;

-- 接口限流表
drop table if exists rate_limit_info;
create table rate_limit_info
(
    `id`          bigint auto_increment primary key comment '主键',
    `interfaceId` bigint                             not null comment '接口ID',
    `redisKey` varchar(256)                             not null comment 'Redis中的键名',
    `limitPreset`      int      default 0                 not null comment '限流预设值',
    `rate`    int      default 0 comment '区间内科执行的次数',
    `rateInterval`  int      default 0 comment '时间区间',
    `createTime`  datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    `updateTime`  datetime default CURRENT_TIMESTAMP not null on update current_timestamp comment '更新时间',
    `isDelete`    tinyint  default 0                 not null comment '是否删除（0-未删  1-已删'
) comment '接口限流表' collate = utf8mb4_unicode_ci;

-- 支付订单表
drop table if exists order_record;
create table order_record
(
    `id`             bigint auto_increment primary key comment '主键',
    `userId`         bigint                                 not null comment '用户ID',
    `interfaceId`    bigint                                 not null comment '接口ID',
    `orderName`      varchar(255) DEFAULT NULL COMMENT '订单名',
    `outPayNo`       varchar(64)  DEFAULT NULL COMMENT '第三方支付交易流水号',
    `outPayChannel`  varchar(255) DEFAULT NULL default 'ALPAY' COMMENT '第三方支付渠道编号',
    `status`         int          default 0                 not null comment '订单状态 0-未支付 1-等待系统处理 2-支付成功 3-支付失败',
    `orderType`      int          default 0                 not null comment '订单类型 0-正常 1-优惠券',
    `totalNum`       int          default 0 comment '购买的总调用次数',
    `totalPrice`     bigint       default 0 comment '订单总额（单位分）',
    `paySuccessTime` datetime                               null comment '支付成功时间',
    `createTime`     datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    `updateTime`     datetime     default CURRENT_TIMESTAMP not null on update current_timestamp comment '更新时间',
    `isDelete`       tinyint      default 0                 not null comment '是否删除（0-未删  1-以删'
) comment '用户调用接口关系' collate = utf8mb4_unicode_ci;

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
    parentId    bigint   default 0 comment '前一次生成的图表Id',
    createTime  datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime  datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete    tinyint  default 0                 not null comment '是否删除'
) comment '用户表格元数据' collate = utf8mb4_unicode_ci;
