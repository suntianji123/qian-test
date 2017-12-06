package com.qiandi.web.job;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.httpclient.HttpException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.qiandi.dao.SMSSendTemplateDAO;
import com.qiandi.pojo.SMSSendRecord;
import com.qiandi.pojo.SMSSendTemplate;
import com.qiandi.pojo.Setting;
import com.qiandi.service.RechargeRecordLateFeeService;
import com.qiandi.service.SMSSendRecordService;
import com.qiandi.service.SettingService;
import com.qiandi.util.BeanUtils;
import com.qiandi.util.CommonUtils;
import com.qiandi.util.Jedis2Utils;
import com.qiandi.util.Jedis2Utils.SortSet;
import com.qiandi.util.JsonUtils;
import com.qiandi.util.MessageContent;
import com.qiandi.util.MessageUtils;
import com.qiandi.util.SendMsgResult;
import com.qiandi.web.util.AdminUtils;

@Component
public class MyJob
{

	private ApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:beans.xml");
	private BeanFactory factory = (BeanFactory) applicationContext;
	private SettingService settingService = (SettingService) factory.getBean("settingService");
	private SMSSendRecordService smsSendRecordService = (SMSSendRecordService) factory.getBean("SMSSendRecordService");
	private SMSSendTemplateDAO smsSendTemplateDAO = new SMSSendTemplateDAO();
	private static final Logger logger = LogManager.getLogger(MyJob.class);

	@Autowired
	private RechargeRecordLateFeeService rechargeRecordLateFeeService;


	/*
	 * 每天零点执行，扫描滞纳金动态情况
	 */
	@Scheduled(cron = "0 0 0 * * ?") // 每天0点执行任务
	public void updateRechargeRecordLateFee()
	{
		// 1. 查询所有没有还清款的订单（支付方式为授信额度，还款状态为没有还清除）
		// 2.当前时间大于最迟还款时间，将逾期天数+1，根据逾期天数，查询出百分比，计算待付滞纳金=待还款金额*百分比


	}

	// @SuppressWarnings("unchecked")
	// @Scheduled(cron = "0/1 * * * * ?") // 每隔5秒隔行一次
	public void test2()
	{

	
		// 从redis缓存中取出手机号信息
		Jedis2Utils redis = Jedis2Utils.getInstance();
		SortSet sortSet = redis.SORTSET;

		// 查询出所有定时任务没有被禁用的任务，sendMode = true isOnRedis = false sendTime>当前时间，
		List<List<SMSSendRecord>> smsSendRecordList3 = smsSendRecordService.selectItemForRedis(true);

		if (!CommonUtils.isEmpty(smsSendRecordList3))
		{
			List<Long> ids = addToRedis(smsSendRecordList3);
			// 设置所有被选中的isOnRedis属性为true
			smsSendRecordService.updateIsOnRedisByList(ids);
		}

		// 更新所有定时短信任务，sendTime时间大于当前时间1000*60*2状态为已过期



		Set<String> set = sortSet.zrangeByScore("smsSendPackage", 0, System.currentTimeMillis());
		Iterator<String> iterator = set.iterator();
		String strMap = null;
		if (iterator.hasNext())
		{
			strMap = iterator.next();
		}
		if (CommonUtils.isEmpty(strMap))
		{
			// System.out.println("redis中暂时没有需要发送短信的任务");
			return;
		}

		strMap = strMap.substring(0, strMap.lastIndexOf('_'));
		List<Map<Object, Object>> smsSendRecordList = new ArrayList<Map<Object, Object>>();
		smsSendRecordList = JsonUtils.toBean(strMap, smsSendRecordList.getClass());
		List<MessageContent> messageContentList = new ArrayList<MessageContent>();
		
		if(CommonUtils.isEmpty(smsSendRecordList))
		{
			return;
		}


		SMSSendTemplate smsSendTemplate = new SMSSendTemplate();
		SMSSendRecord s1 = new SMSSendRecord();
		BeanUtils.mapToBean(smsSendRecordList.get(0), s1);
		smsSendTemplate.setId(s1.getSmsSendTemplateId());
		
		//Todo 检查短信余额
		
		
		

		// 遍历smsSendList集合，将手机号及对应的内容获取出来，添加至发送短信接口
		List<SMSSendRecord> smsSendRecordList2 = new ArrayList<SMSSendRecord>(smsSendRecordList.size());
		for (int i = 0; i < smsSendRecordList.size(); i++)
		{
			SMSSendRecord smsSendRecord = new SMSSendRecord();
			BeanUtils.mapToBean(smsSendRecordList.get(i), smsSendRecord);

			MessageContent messageContent = new MessageContent();
			messageContent.setPhone(smsSendRecord.getPhoneNum());
			messageContent.setContent(smsSendRecord.getContent());
			messageContentList.add(messageContent);
			smsSendRecordList2.add(smsSendRecord);
		}

		// 调用短信接口发送短信
		Setting setting = new Setting();
		setting.setName("sms_userid");
		setting = settingService.selectOne(setting);
		String userid = setting.getValue();

		setting = new Setting();
		setting.setName("sms_password");
		setting = settingService.selectOne(setting);
		String password = setting.getValue();

		try
		{
			// 发送短信
			List<SendMsgResult> messageResultList = MessageUtils.sendMsg(1, userid, password, messageContentList);

			logger.debug("从redis中删除了" + sortSet.zremrangeByRank("smsSendPackage", 0, 0) + "条短信包");
			logger.debug("发送短信完成！");


			if (CommonUtils.isEmpty(messageResultList))
			{
				return;
			}

			for (int i = 0; i < messageResultList.size(); i++)
			{
				SendMsgResult sendMsgResult = messageResultList.get(i);
				for (int j = 0; j < smsSendRecordList2.size(); j++)
				{

					SMSSendRecord smsSendRecord = smsSendRecordList2.get(j);

					if (sendMsgResult.getPhoneNum().equals(smsSendRecord.getPhoneNum()))
					{
						smsSendRecord.setMsgId(sendMsgResult.getMsgId());
						smsSendRecord.setSendTime(System.currentTimeMillis());
						if ("0".equals(sendMsgResult.getReturnNum()))
						{
							smsSendRecord.setSendStatus("1");
							smsSendTemplate.setSendStatus("1");
						} else if ("1004009".equals(sendMsgResult.getReturnNum()))
						{
							smsSendRecord.setSendStatus("3");
						} else if ("1001002".equals(sendMsgResult.getReturnNum()))
						{
							smsSendRecord.setSendStatus("4");
							smsSendRecord.setSendStatus("3");
						}

						smsSendRecordList2.set(j, smsSendRecord);
						break;
					}
				}

			}

			Connection conn = null;
			try
			{
				conn = AdminUtils.getConnection();
				smsSendTemplateDAO.updateSendData(conn, smsSendRecordList2, smsSendTemplate);
			} catch (SQLException e)
			{
				throw new RuntimeException("获取数据库连接出错了", e);
			}
			finally
			{
				if (conn != null)
				{
					try
					{
						conn.close();
					} catch (SQLException e)
					{
						throw new RuntimeException("获取数据库连接出错了", e);
					}
				}
			}



		} catch (HttpException e)
		{
			throw new RuntimeException("调用发送短信接口出错了！", e);
		} catch (IOException e)
		{
			throw new RuntimeException("调用发送短信接口出错了！", e);
		}

	}
	
	@SuppressWarnings("unchecked")
	private List<Long> addToRedis(List<List<SMSSendRecord>> smsSendRecordList)
	{
		// 分包，按照大小500

		int blockSize = 2;
		if (!CommonUtils.isEmpty(smsSendRecordList))
		{
			List<Long> smsSendRecordIdList = new ArrayList<Long>();
			Jedis2Utils redis = Jedis2Utils.getInstance();
			SortSet sortSet = redis.SORTSET;
			for (int i = 0; i < smsSendRecordList.size(); i++)
			{
				List<SMSSendRecord> list = smsSendRecordList.get(i);

				for (int z = 0; z < list.size(); z++)
				{
					SMSSendRecord sr = new SMSSendRecord();
					BeanUtils.mapToBean((Map<Object, Object>) list.get(i), sr);
					smsSendRecordIdList.add(sr.getId());
				}

				int listSize = list.size();
				if (listSize <= blockSize)
				{

					// 天假到redis缓存
					sortSet.zadd("smsSendPackage", System.currentTimeMillis(),
							JsonUtils.toJson(list) + "_" + UUID.randomUUID().toString());
					continue;
				}
				int batchSize = listSize / blockSize;
				int remain = listSize % blockSize;
				for (int j = 0; j < batchSize; j++)
				{
					int fromIndex = j * blockSize;
					int toIndex = fromIndex + blockSize;
					sortSet.zadd("smsSendPackage", System.currentTimeMillis(),
							JsonUtils.toJson(list.subList(fromIndex, toIndex)) + "_" + UUID.randomUUID().toString());
				}
				if (remain > 0)
				{
					sortSet.zadd("smsSendPackage", System.currentTimeMillis(),
							JsonUtils.toJson(list.subList(listSize - remain, listSize)) + "_"
									+ UUID.randomUUID().toString());
				}

			}

			if (!CommonUtils.isEmpty(smsSendRecordIdList))
			{
				return smsSendRecordIdList;
			}
		}
		return null;
	}



}
