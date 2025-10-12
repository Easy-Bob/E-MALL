package com.bob.mall.order.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.bob.common.constant.OrderConstant;
import com.bob.common.exception.NoStockExecption;
import com.bob.common.utils.PageUtils;
import com.bob.common.vo.MemberVO;
import com.bob.mall.order.Interceptor.AuthInterceptor;
import com.bob.mall.order.dto.OrderCreateTO;
import com.bob.mall.order.dto.SeckillOrderDto;
import com.bob.mall.order.entity.OrderItemEntity;
import com.bob.mall.order.feign.CartService;
import com.bob.mall.order.feign.MemberService;
import com.bob.mall.order.feign.ProductService;
import com.bob.mall.order.feign.WareService;
import com.bob.mall.order.service.OrderItemService;
import com.bob.mall.order.utils.OrderMsgProducer;
import com.bob.mall.order.vo.*;
//import io.seata.spring.annotation.GlobalTransactional;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bob.common.utils.*;

import com.bob.mall.order.dao.OrderDao;
import com.bob.mall.order.entity.OrderEntity;
import com.bob.mall.order.service.OrderService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {


    @Autowired
    private MemberService memberFeignService;

    @Autowired
    private CartService cartFeignService;

    @Autowired
    private ProductService productFeignService;

    @Autowired
    private WareService wareFeignService;

    @Autowired
    private ThreadPoolExecutor executor;

    @Autowired
    private StringRedisTemplate redisTemplate;

//    @Autowired
//    private OrderService orderService;

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private OrderMsgProducer orderMsgProducer;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public OrderConfirmVo confirmOrder() {
        OrderConfirmVo vo = new OrderConfirmVo();
        MemberVO memberVO = AuthInterceptor.threadLocal.get();
        // 获取RequestContextHolder的相关信息
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        CompletableFuture<Void> addressFuture = CompletableFuture.runAsync(() -> {
            // 同步主线程中的 RequestContextHolder
            RequestContextHolder.setRequestAttributes(requestAttributes);
            // 1. 用户的会员 地址信息
            Long id = memberVO.getId();
            List<MemberAddressVo> address = memberFeignService.getAddress(id);
            vo.setAddress(address);
        }, executor);


        CompletableFuture<Void> cartFuture = CompletableFuture.runAsync(() -> {
            // 同步主线程中的 RequestContextHolder
            RequestContextHolder.setRequestAttributes(requestAttributes);
            // 2. 购物车商品
            List<OrderItemVo> userCartItems = cartFeignService.getUserCartItems();
            vo.setItems(userCartItems);
        }, executor);

        try {
            CompletableFuture.allOf(addressFuture, cartFuture).get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
        // 3. 订单总金额 和 需要支付的金额 Vo已有

        // 4. 生成防重的Token (uuid)
        String token = UUID.randomUUID().toString().replaceAll("-", "");

        redisTemplate.opsForValue().set(OrderConstant.ORDER_TOKEN_PREFIX + ":" + memberVO.getId(), token);
        vo.setOrderToken(token);
        return vo;

    }

    private Lock lock = new ReentrantLock();


    /**
     * Seata分布式事务管理
     * @param vo
     * @return
     */
    @Transactional
    @Override
    public OrderResponseVO submitOrder(OrderSubmitVO vo) {
        OrderResponseVO responseVO = new OrderResponseVO();

        // 获取当前登录的用户信息
        MemberVO memberVO = AuthInterceptor.threadLocal.get();
        // 1. 验证是否重复提交 保证redis中的 查询 和 删除 是原子性的
        String key = OrderConstant.ORDER_TOKEN_PREFIX + ":" + memberVO.getId();
        try{
            lock.lock();

            String redisToken = redisTemplate.opsForValue().get(key);
            if(redisToken != null && redisToken.equals(vo.getOrderToken())){
                // 表示第一次提交
                // 删除token
                redisTemplate.delete(key);
            }else{
                // 重复提交
                return responseVO;
            }
        }finally {
            lock.unlock();
        }

        // redis 脚本实现， 与上面效果相同
//        // 获取当前登录的用户信息
//        MemberVO memberVO = (MemberVO) AuthInterceptor.threadLocal.get();
//        // 1.验证是否重复提交  保证Redis中的token 的查询和删除是一个原子性操作
//        String key = OrderConstant.ORDER_TOKEN_PREFIX+":"+memberVO.getId();
//        String script = "if redis.call('get',KEYS[1])==ARGV[1] then return redis.call('del',KEYS[1]) else return 0";
//        Long result = redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class)
//                , Arrays.asList(key)
//                , vo.getOrderToken());
//        if(result == 0){
//            // 表示验证失败 说明是重复提交
//            return responseVO;
//        }

        //2. 创建订单
        OrderCreateTO orderCreateTO = createOrder(vo);
        responseVO.setOrderEntity(orderCreateTO.getOrderEntity());
        // 3. 保存订单信息
        saveOrder(orderCreateTO);

        // 4. 锁定库存信息
        // 订单号 SKU_ID SKU_NAME 商品数量
        lockWareSku(responseVO, orderCreateTO);

        // 5. 同步更新用户的会员积分

        // 订单成功后，给消息中间件发送延迟30s的关单消息
        orderMsgProducer.sendOrderMessage(orderCreateTO.getOrderEntity().getOrderSn());

        return responseVO;


    }

    @Transactional
    @Override
    public void quickCreateOrder(SeckillOrderDto seckillOrderDto) {
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(seckillOrderDto.getOrderSN());
        orderEntity.setStatus(OrderConstant.OrderStateEnum.FOR_THE_PAYMENT.getCode());
        orderEntity.setMemberId(seckillOrderDto.getMemberId());
        orderEntity.setTotalAmount(seckillOrderDto.getSeckillPrice().multiply(new BigDecimal(seckillOrderDto.getNum())));
        this.save(orderEntity);

        OrderItemEntity itemEntity = new OrderItemEntity();
        itemEntity.setOrderSn(seckillOrderDto.getOrderSN());
        itemEntity.setSkuPrice(seckillOrderDto.getSeckillPrice());
        itemEntity.setSkuId(seckillOrderDto.getSkuId());
        itemEntity.setRealAmount(seckillOrderDto.getSeckillPrice().multiply(new BigDecimal(seckillOrderDto.getNum())));
        itemEntity.setSkuQuantity(seckillOrderDto.getNum());

        orderItemService.save(itemEntity);
    }

    private void lockWareSku(OrderResponseVO responseVO, OrderCreateTO orderCreateTO){
        WareSkuLockVO wareSkuLockVO = new WareSkuLockVO();
        wareSkuLockVO.setOrderSN(orderCreateTO.getOrderEntity().getOrderSn());
        List<OrderItemVo> orderItemVos = orderCreateTO.getOrderItemEntitys().stream().map(item -> {
            OrderItemVo itemVo = new OrderItemVo();
            itemVo.setSkuId(item.getSkuId());
            itemVo.setTitle(item.getSkuName());
            itemVo.setCount(item.getSkuQuantity());
            return itemVo;
        }).collect(Collectors.toList());
        wareSkuLockVO.setItems(orderItemVos);
        R r = wareFeignService.orderLockStock(wareSkuLockVO);
        if(r.getCode() == 0){
            // 锁定库存成功
            responseVO.setCode(0);
        }else{
            // 失败
            responseVO.setCode(2);
            throw new NoStockExecption(100000l);
        }
    }

    private void saveOrder(OrderCreateTO orderCreateTO) {
        // 订单
        OrderEntity orderEntity = orderCreateTO.getOrderEntity();
        this.save(orderEntity);
        // 订单项
        List<OrderItemEntity> orderItemEntitys = orderCreateTO.getOrderItemEntitys();
        orderItemService.saveBatch(orderItemEntitys);

    }


    // 创建订单的方法
    private OrderCreateTO createOrder(OrderSubmitVO vo) {
        OrderCreateTO createTO = new OrderCreateTO();
        // 创建订单
        OrderEntity orderEntity = buildOrder(vo);
        createTO.setOrderEntity(orderEntity);

        // 创建OrderItemEntity 订单项
       List<OrderItemEntity> orderItemEntities =  buildOrderItems(orderEntity.getOrderSn());
        // 根据订单项计算出支付总额
        BigDecimal totalAmount = new BigDecimal(0);
        for (OrderItemEntity orderItemEntity : orderItemEntities) {
            BigDecimal total = orderItemEntity.getSkuPrice().multiply(new BigDecimal(orderItemEntity.getSkuQuantity()));
            totalAmount = totalAmount.add(total);
        }
        orderEntity.setTotalAmount(totalAmount);
       createTO.setOrderItemEntitys(orderItemEntities);

        return createTO;
    }

    private List<OrderItemEntity> buildOrderItems(String orderSN) {
        List<OrderItemEntity> orderItemEntities = new ArrayList<>();

        List<OrderItemVo> userCartItems = cartFeignService.getUserCartItems();
        if(userCartItems != null && !userCartItems.isEmpty()) {
            // 统一根据skuid查询出spu信息
            List<Long> spuIds = new ArrayList<>();
            for (OrderItemVo orderItemVo : userCartItems) {
                if(!spuIds.contains(orderItemVo.getSpuId())){
                    spuIds.add(orderItemVo.getSpuId());
                }
            }
            // 远程调用
            List<OrderItemSpuInfoVO> spuInfos = productFeignService.getOrderItemSpuInfoBySpuId(spuIds.toArray(new Long[0]));
            Map<Long, OrderItemSpuInfoVO> map = spuInfos.stream().collect(Collectors.toMap(OrderItemSpuInfoVO::getId, item -> item));
            for (OrderItemVo userCartItem : userCartItems) {
                OrderItemSpuInfoVO spuInfo = map.get(userCartItem.getSpuId());
                OrderItemEntity orderItemEntity = buildOrderItem(userCartItem, spuInfo);
                // 绑定对应的订单编号
                orderItemEntity.setOrderSn(orderSN);
                orderItemEntities.add(orderItemEntity);
            }
        }
        return orderItemEntities;
    }

    private OrderItemEntity buildOrderItem(OrderItemVo userCartItem, OrderItemSpuInfoVO spuInfo) {
        OrderItemEntity entity = new OrderItemEntity();
        // SKU
        entity.setSkuId(userCartItem.getSkuId());
        entity.setSkuName(userCartItem.getTitle());
        entity.setSkuPic(userCartItem.getImage());
        entity.setSkuPrice(userCartItem.getPrice());
        entity.setSkuQuantity(userCartItem.getCount());
        List<String> skuAttr = userCartItem.getSkuAttr();
        String skuAttrStr = StringUtils.collectionToDelimitedString(skuAttr, ";");
        entity.setSkuAttrsVals(skuAttrStr);
        // SPU
        entity.setSpuId(spuInfo.getId());
        entity.setSpuBrand(spuInfo.getBrandName());
        entity.setCategoryId(spuInfo.getCatalogId());
        entity.setSpuPic(spuInfo.getImg());
        // 优惠(无)

        // 积分
        entity.setGiftGrowth(userCartItem.getPrice().intValue());
        entity.setGiftIntegration(userCartItem.getPrice().intValue());
        return entity;
    }

    private OrderEntity buildOrder(OrderSubmitVO vo){
        OrderEntity orderEntity = new OrderEntity();

        // 创建订单编号
        String orderSn = IdWorker.getTimeId();
        orderEntity.setOrderSn(orderSn);
        MemberVO memberVO = AuthInterceptor.threadLocal.get();

        // 设置会员相关的信息
        orderEntity.setMemberId(memberVO.getId());
        orderEntity.setMemberUsername(memberVO.getUsername());

        //  根据收货地址ID获取地址
        MemberAddressVo memberAddressVo = memberFeignService.getAddressById(vo.getAddrId());

        // 创建orderItemEntity
        orderEntity.setReceiverCity(memberAddressVo.getCity());
        orderEntity.setReceiverDetailAddress(memberAddressVo.getDetailAddress());
        orderEntity.setReceiverName(memberAddressVo.getName());
        orderEntity.setReceiverPhone(memberAddressVo.getPhone());
        orderEntity.setReceiverPostCode(memberAddressVo.getPostCode());
        orderEntity.setReceiverRegion(memberAddressVo.getRegion());
        orderEntity.setReceiverProvince(memberAddressVo.getProvince());

        return orderEntity;
    }

}