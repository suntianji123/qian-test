package com.qiandi.web.util;

import java.sql.Connection;
import java.sql.SQLException;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationContext;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.qiandi.pojo.Setting;
import com.qiandi.service.SettingService;

public class AdminUtils
{

	public static String getValue(SettingService settingService, String name)
	{
		Setting setting = new Setting();
		setting.setName(name);
		setting = settingService.selectOne(setting);
		String value = setting.getValue();
		return value;
	}

	public static Connection getConnection() throws SQLException
	{
		ApplicationContext applicationContext = ApplicationContextHolder.applicationContext;
		BeanFactory factory = (BeanFactory) applicationContext;
		ComboPooledDataSource ds = (ComboPooledDataSource) factory.getBean("dataSource");

		return ds.getConnection();

	}
}
