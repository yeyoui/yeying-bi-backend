package com.yeyou.yeyingBIbackend.service.impl;

import static com.yeyou.yeyingBIbackend.constant.UserConstant.USER_LOGIN_STATE;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yeyou.yeyingBIbackend.common.ErrorCode;
import com.yeyou.yeyingBIbackend.constant.UserConstant;
import com.yeyou.yeyingBIbackend.exception.ThrowUtils;
import com.yeyou.yeyingBIbackend.model.dto.user.DailySignStatus;
import com.yeyou.yeyingBIbackend.model.dto.user.UserQueryRequest;
import com.yeyou.yeyingBIbackend.model.entity.InterfaceInfo;
import com.yeyou.yeyingBIbackend.model.entity.User;
import com.yeyou.yeyingBIbackend.model.enums.InterfaceStatusEnum;
import com.yeyou.yeyingBIbackend.model.enums.UserRoleEnum;
import com.yeyou.yeyingBIbackend.model.vo.LoginUserVO;
import com.yeyou.yeyingBIbackend.model.vo.UserVO;
import com.yeyou.yeyingBIbackend.service.InterfaceInfoService;
import com.yeyou.yeyingBIbackend.service.UserService;
import com.yeyou.yeyingBIbackend.utils.SqlUtils;
import com.yeyou.yeyingBIbackend.constant.CommonConstant;
import com.yeyou.yeyingBIbackend.exception.BusinessException;
import com.yeyou.yeyingBIbackend.mapper.UserMapper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

/**
 * 用户服务实现
 *
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private InterfaceInfoService interfaceInfoService;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    /**
     * 盐值，混淆密码
     */
    private static final String SALT = "yeyoui";

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        // 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        synchronized (userAccount.intern()) {
            // 账户不能重复
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("userAccount", userAccount);
            long count = this.baseMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
            }
            // 2. 加密
            String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
            // 3. 插入数据
            User user = new User();
            user.setUserAccount(userAccount);
            user.setUserPassword(encryptPassword);
            boolean saveResult = this.save(user);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
            }
            return user.getId();
        }
    }

    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号错误");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = this.baseMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        // 3. 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        return this.getLoginUserVO(user);
    }


    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        ThrowUtils.throwIf(request==null,ErrorCode.NOT_LOGIN_ERROR);
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 从数据库查询（追求性能的话可以注释，直接走缓存）
        long userId = currentUser.getId();
        currentUser = this.getById(userId);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }

    /**
     * 获取当前登录用户（允许未登录）
     *
     * @param request
     * @return
     */
    @Override
    public User getLoginUserPermitNull(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            return null;
        }
        // 从数据库查询（追求性能的话可以注释，直接走缓存）
        long userId = currentUser.getId();
        return this.getById(userId);
    }

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        // 仅管理员可查询
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        return isAdmin(user);
    }

    @Override
    public boolean isAdmin(User user) {
        return user != null && UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
    }

    /**
     * 用户注销
     *
     * @param request
     */
    @Override
    public boolean userLogout(HttpServletRequest request) {
        if (request.getSession().getAttribute(USER_LOGIN_STATE) == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录");
        }
        // 移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }

    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public List<UserVO> getUserVO(List<User> userList) {
        if (CollectionUtils.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = userQueryRequest.getId();
        String unionId = userQueryRequest.getUnionId();
        String userName = userQueryRequest.getUserName();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(id != null, "id", id);
        queryWrapper.eq(StringUtils.isNotBlank(unionId), "unionId", unionId);
        queryWrapper.eq(StringUtils.isNotBlank(userRole), "userRole", userRole);
        queryWrapper.like(StringUtils.isNotBlank(userName), "userName", userName);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    @Override
    public void invokeDeduction(long interfaceId, long userId) {
        InterfaceInfo interfaceInfo = interfaceInfoService.getById(interfaceId);
        //检查接口状态
        ThrowUtils.throwIf(interfaceInfo==null,ErrorCode.PARAMS_ERROR,"接口不存在");
        ThrowUtils.throwIf(!InterfaceStatusEnum.NORMAL.equals(interfaceInfo.getStatus())
                ,ErrorCode.PARAMS_ERROR,"接口不可用");
        //查询用户信息
        User user = this.getById(userId);
        Integer expenses = interfaceInfo.getExpenses();
        ThrowUtils.throwIf(user.getCredits()< expenses,ErrorCode.PARAMS_ERROR,"积分不足,请充值");
        //执行扣费
        updateAllocationCredits(userId,-1*expenses);
    }

    @Override
    public void updateAllocationCredits(long userId, int diff) {
        boolean succeed = this.update().eq("id", userId).setSql("credits=credits+" + diff).update();
        ThrowUtils.throwIf(!succeed,ErrorCode.SYSTEM_ERROR,"系统执行扣费失败");
    }


    @Override
    public DailySignStatus dailySign(Long userId, String dateStr,boolean doSign) {
        //todo 后续用户也能补签
        if(!UserConstant.ADMIN_ROLE.equals(getById(userId).getUserRole()) && StringUtils.isNotBlank(dateStr)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "补签卡不足");
        }
        //获取日期
        Date date = getDate(dateStr);
        //获取对应的天数
        int day = DateUtil.dayOfMonth(date);
        //根据当前日期计算
        Date today = new Date();
        //补签日期不能晚于现在
        ThrowUtils.throwIf(date.after(today),ErrorCode.PARAMS_ERROR,"补签日期不能晚于现在");
        //构建key
        String signKey = buildSignKey(userId,date);
        //新建返回对象
        DailySignStatus dailySignStatus = new DailySignStatus();
        if(doSign){
            //查看指定的日期是否已签到
            Boolean isSigned = redisTemplate.opsForValue().getBit(signKey, day);
            //已经签到
            ThrowUtils.throwIf(isSigned!=null && isSigned,ErrorCode.OPERATION_ERROR,"今日已签到");
            //执行签到
            redisTemplate.opsForValue().setBit(signKey, day,true);
            //增加积分
            updateAllocationCredits(userId,100);
            dailySignStatus.setTodaySigned(true);
        }else {
            //查看今天是否已签到
            Boolean isSigned = redisTemplate.opsForValue().getBit(signKey, DateUtil.dayOfMonth(today));
            if(isSigned==null) isSigned = false;
            dailySignStatus.setTodaySigned(isSigned);
        }
        //计算连续签到
        int continuousSignCount = getContinuousSignCount(userId, today);
        //计算签到总数
        Long sumSignCount = getSumSignCount(userId, today);
        dailySignStatus.setContinuousDay(continuousSignCount);
        dailySignStatus.setTotalDay(sumSignCount);
        return dailySignStatus;
    }
    /**
     * 统计连续签到次数
     *
     * @param userId 用户ID
     * @param date   查询的日期
     * @return
     */
    private int getContinuousSignCount(Long userId, Date date) {
        // 获取日期对应的天数，多少号，假设是 31
        int dayOfMonth = DateUtil.dayOfMonth(date);
        // 构建 Redis Key
        String signKey = buildSignKey(userId, date);
        BitFieldSubCommands bitFieldSubCommands = BitFieldSubCommands.create()
                .get(BitFieldSubCommands.BitFieldType.unsigned(dayOfMonth+1))
                .valueAt(0);
        // 获取用户从当前日期开始到 1 号的所有签到状态
        List<Long> list = redisTemplate.opsForValue().bitField(signKey, bitFieldSubCommands);
        if (list == null || list.isEmpty()) {
            return 0;
        }
        // 连续签到计数器
        int signCount = 0;
        long v = list.get(0) == null ? 0 : list.get(0);
        // 位移计算连续签到次数
        for (int i = dayOfMonth; i > 0; i--) {// i 表示位移操作次数
            // 右移再左移，如果等于自己说明最低位是 0，表示未签到
//            if (v >> 1 << 1 == v) {
            if ((v & 1) ==0) {
                // 用户可能当前还未签到，所以要排除是否是当天的可能性
                // 低位 0 且非当天说明连续签到中断了
                if (i != dayOfMonth) break;
            } else {
                // 右移再左移，如果不等于自己说明最低位是 1，表示签到
                signCount++;
            }
            // 右移一位并重新赋值，相当于把最低位丢弃一位然后重新计算
            v >>= 1;
        }
        return signCount;
    }
    /**
     * 统计总签到次数
     *
     * @param userId 用户ID
     * @param date   查询的日期
     * @return
     */
    private Long getSumSignCount(Long userId, Date date) {
        // 构建 Redis Key
        String signKey = buildSignKey(userId, date);
        return  redisTemplate.execute(
                (RedisCallback<Long>) con -> con.bitCount(signKey.getBytes())
        );
    }

    /**
     * 获取日期
     *
     * @param dateStr yyyy-MM-dd
     * @return
     */
    private Date getDate(String dateStr) {
        return StrUtil.isBlank(dateStr) ?
                new Date() : DateUtil.parseDate(dateStr);
    }

    /**
     * 构建 Redis Key - user:sign:userId:yyyyMM
     *
     * @param userId 用户ID
     * @param date   日期
     * @return
     */
    private String buildSignKey(Long userId, Date date) {
        return String.format("user:sign:%d:%s", userId,
                DateUtil.format(date, "yyyyMM"));
    }
}
