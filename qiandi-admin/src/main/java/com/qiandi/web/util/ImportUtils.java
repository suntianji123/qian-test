package com.qiandi.web.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import com.qiandi.pojo.PhoneInformation;
import com.qiandi.pojo.PhoneInformationRedis;
import com.qiandi.service.PhoneInformationService;
import com.qiandi.util.CommonUtils;
import com.qiandi.util.IDUtils;
import com.qiandi.util.UploadUtils;

public class ImportUtils
{


	// 个性化短信，
	public static Map<String, Object> getPersonaliseSMS(InputStream inStream,
			PhoneInformationService phoneInformationService) throws InvalidFormatException, IOException
	{
		Map<String, Object> map = new HashMap<String, Object>();
		// 表格列名
		Map<Integer, String> columnMap = new HashMap<Integer, String>();
		List<Map<String, Object>> columnContentList = new ArrayList<Map<String, Object>>();


		// List<String> contentList = new ArrayList<String>();
		boolean isError = false;
		String errorMsg = null;



		SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");

		// 同时支持Excel 2003、2007
		Workbook workbook = WorkbookFactory.create(inStream); // 这种方式 Excel
		int sheetCount = workbook.getNumberOfSheets(); // Sheet的数量

		// 遍历每个Sheet
		for (int s = 0; s < sheetCount; s++)
		{

			if (isError)
			{
				break;
			}

			Sheet sheet = workbook.getSheetAt(s);
			int rowCount = sheet.getPhysicalNumberOfRows(); // 获取总行数
			// 遍历每一行

			for (int r = 0; r < rowCount; r++)
			{
				Map<String, Object> contentMap = new HashMap<String, Object>();

				if (isError)
				{
					break;
				}

				Row row = sheet.getRow(r);
				int cellCount = row.getPhysicalNumberOfCells(); // 获取总列数
				// 遍历每一列
				for (int c = 0; c < cellCount; c++)
				{
					Cell cell = row.getCell(c);
					int cellType = cell.getCellType();
					String cellValue = null;
					if(r==0&&cellType!=Cell.CELL_TYPE_STRING)
					{
						isError = true;
						errorMsg = "表格列名必须为字符";
						break;
					}
							
							

					switch (cellType)
					{
					case Cell.CELL_TYPE_STRING: // 文本
						cellValue = cell.getStringCellValue();
						break;
					case Cell.CELL_TYPE_NUMERIC: // 数字、日期
						if (DateUtil.isCellDateFormatted(cell))
						{
							cellValue = fmt.format(cell.getDateCellValue()); // 日期型
						} else
						{
							System.out.println(cellValue);
							cellValue = new DecimalFormat("#.##").format(cell.getNumericCellValue());
							


						}
						break;
					case Cell.CELL_TYPE_BOOLEAN: // 布尔型
						cellValue = String.valueOf(cell.getBooleanCellValue());
						break;
					case Cell.CELL_TYPE_BLANK: // 空白
						cellValue = cell.getStringCellValue();
						break;
					case Cell.CELL_TYPE_ERROR: // 错误
						cellValue = "错误";
						break;
					case Cell.CELL_TYPE_FORMULA: // 公式
						cellValue = "错误";
						break;
					default:
						cellValue = "错误";
					}



					cellValue = cellValue.replaceAll(" +", "");
					if (cellValue == "")
					{
						isError = true;
						errorMsg = "含有空格";
						break;
					}

					if (r == 0)
					{
						columnMap.put(c, cellValue);
					} else
					{
						String column = columnMap.get(c);
						contentMap.put(column, cellValue);
					}
					

				}
				if (!columnMap.values().contains("手机号"))
				{
					isError = true;
					errorMsg = "表格列中缺少'手机号'一栏";
					break;
				}
				columnContentList.add(contentMap);
			}
		}

		map.put("isError", isError);
		map.put("errorMsg", errorMsg);
		map.put("columnContentList", columnContentList);
		return map;
	}

	public static void copyFile(File inFile, File outFile)
	{
		// File file = new File("c:/shuju.txt");
		InputStream inStream = null;
		Reader reader = null;
		BufferedReader buffReader = null;
		OutputStream outStream = null;
		try
		{
			inStream = new FileInputStream(inFile);
			reader = new InputStreamReader(inStream, "UTF-8");
			buffReader = new BufferedReader(reader);
			outStream = new FileOutputStream(outFile);
			String line = null;
			while ((line = buffReader.readLine()) != null)
			{
				line = line.replaceAll(" ", "");
				int start = line.indexOf(',')+2;
				line = line.substring(start, start + 7) + "\n";
				byte[] bytes = line.getBytes();
				int len = bytes.length;
				outStream.write(bytes, 0, len);
				System.out.println(line);

			}

		} catch (IOException e)
		{
			throw new RuntimeException("解析文件出错了！", e);
		} finally
		{
			UploadUtils.closeQuietly(outStream);
			UploadUtils.closeQuietly(buffReader);
			UploadUtils.closeQuietly(reader);
			UploadUtils.closeQuietly(inStream);
		}
	}


	public static Map<String, Object> getPhoneNumsByTxtOrCsv(InputStream inStream)
	{
		Map<String, Object> map = new HashMap<String, Object>();
		List<String> phoneNumList = new ArrayList<String>();
		List<Integer> idList = new ArrayList<Integer>();
		if (inStream == null)
		{
			return null;
		}

		Reader reader = null;
		BufferedReader buffReader = null;
		try
		{
			reader = new InputStreamReader(inStream, "UTF-8");
			buffReader = new BufferedReader(reader);
			String line = null;
			while ((line = buffReader.readLine()) != null)
			{
				if (CommonUtils.isPhone(line))
				{
					phoneNumList.add(line);
					line = line.substring(0, 7);
					idList.add(Integer.parseInt(line));
				}
			}

			map.put("phoneNumList", phoneNumList);
			map.put("idList", idList);
			return map;

		} catch (IOException e)
		{
			throw new RuntimeException("解析文件出错了", e);
		} finally
		{
			UploadUtils.closeQuietly(buffReader);
			UploadUtils.closeQuietly(reader);
			UploadUtils.closeQuietly(inStream);
		}

	}

	public static Map<String, Object> getPhoneNumsByExcel(InputStream inStream,
			PhoneInformationService phoneInformationService, Long blackId, Long blankId)
			throws InvalidFormatException, IOException
	{
		Map<String, Object> returnMap = new HashMap<String, Object>();
		List<Integer> idList = new ArrayList<Integer>();
		List<String> phoneNumList = new ArrayList<String>();
		List<String> tempList = new ArrayList<String>();
		// 计算非手机号的数量
		int notPhoneNumCount = 0;
		// 计算重复手机号数量
		int repeatPhoneNumCount = 0;
		// 计算黑名单手机号数量
		int blackPhoneNumCount = 0;
		// 计算空号手机号数量
		int blankPhoneNumCount = 0;

		// 查询出所有白名单
		List<PhoneInformation> phoneInformationList = new ArrayList<PhoneInformation>();
		Map<String, Long> param = new HashMap<String, Long>();
		param.put("blackId", blackId);
		param.put("blankId", blankId);
		phoneInformationList = phoneInformationService.selectBlackAndBlankList(param);

		SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");

		// 同时支持Excel 2003、2007
		Workbook workbook = WorkbookFactory.create(inStream); // 这种方式 Excel
		int sheetCount = workbook.getNumberOfSheets(); // Sheet的数量

		// 遍历每个Sheet
		for (int s = 0; s < sheetCount; s++)
		{

			Sheet sheet = workbook.getSheetAt(s);
			int rowCount = sheet.getPhysicalNumberOfRows(); // 获取总行数
			// 遍历每一行

			for (int r = 0; r < rowCount; r++)
			{

				Row row = sheet.getRow(r);
				int cellCount = row.getPhysicalNumberOfCells(); // 获取总列数
				// 遍历每一列
				for (int c = 0; c < cellCount; c++)
				{
					Cell cell = row.getCell(c);
					int cellType = cell.getCellType();
					String cellValue = null;


					switch (cellType)
					{
					case Cell.CELL_TYPE_STRING: // 文本
						cellValue = cell.getStringCellValue();
						if (CommonUtils.isPhone(cellValue))
						{
							if (tempList.contains(cellValue))
							{
								repeatPhoneNumCount++;
							} else
							{
								tempList.add(cellValue);
							}

							if (!CommonUtils.isEmpty(phoneInformationList))
							{
								for (int k = 0; k < phoneInformationList.size(); k++)
								{
									PhoneInformation phoneInformation = phoneInformationList.get(k);
									if (phoneInformation.getPhoneNum().equals(cellValue))
									{
										if (blackId == phoneInformation.getStatusId())
										{
											blackPhoneNumCount++;
										} else if (blankId == phoneInformation.getStatusId())
										{
											blankPhoneNumCount++;
										}
									} else
									{
										if (!phoneNumList.contains(cellValue))
										{
											phoneNumList.add(cellValue);
											String cellValue1 = cellValue.substring(0, 7);
											idList.add(Integer.parseInt(cellValue1));
										}
									}
								}
							}
						} else
						{
							notPhoneNumCount++;
						}

						// System.out.println(cellValue);
						break;
					case Cell.CELL_TYPE_NUMERIC: // 数字、日期
						if (DateUtil.isCellDateFormatted(cell))
						{
							cellValue = fmt.format(cell.getDateCellValue()); // 日期型
						} else
						{

							cellValue = new DecimalFormat("#.##").format(cell.getNumericCellValue());
							if (CommonUtils.isPhone(cellValue))
							{
								if (tempList.contains(cellValue))
								{
									repeatPhoneNumCount++;
								} else
								{
									tempList.add(cellValue);
								}

								boolean flag = false;
								if (!CommonUtils.isEmpty(phoneInformationList))
								{

									for (int k = 0; k < phoneInformationList.size(); k++)
									{
										PhoneInformation phoneInformation = phoneInformationList.get(k);
										if (phoneInformation.getPhoneNum().equals(cellValue))
										{
											if (blackId.equals(phoneInformation.getStatusId()))
											{
												blackPhoneNumCount++;
											} else if (blankId.equals(phoneInformation.getStatusId()))
											{
												blankPhoneNumCount++;
											}
											flag = true;
											break;
										}
									}

								}

								if (!flag)
								{
									if (!phoneNumList.contains(cellValue))
									{
										phoneNumList.add(cellValue);
										String cellValue1 = cellValue.substring(0, 7);
										idList.add(Integer.parseInt(cellValue1));
									}
								}

							} else
							{
								notPhoneNumCount++;
							}
						}
						break;
					case Cell.CELL_TYPE_BOOLEAN: // 布尔型
						cellValue = String.valueOf(cell.getBooleanCellValue());
						break;
					case Cell.CELL_TYPE_BLANK: // 空白
						cellValue = cell.getStringCellValue();
						break;
					case Cell.CELL_TYPE_ERROR: // 错误
						cellValue = "错误";
						break;
					case Cell.CELL_TYPE_FORMULA: // 公式
						cellValue = "错误";
						break;
					default:
						cellValue = "错误";
					}

				}
			}
		}
		
		tempList = null;
		returnMap.put("phoneNumList", phoneNumList);
		returnMap.put("idList", idList);
		returnMap.put("notPhoneNumCount", notPhoneNumCount - 1);
		returnMap.put("repeatPhoneNumCount", repeatPhoneNumCount);
		returnMap.put("blackPhoneNumCount", blackPhoneNumCount);
		returnMap.put("blankPhoneNumCount", blankPhoneNumCount);
		return returnMap;
	}

	public static Map<String, Object> getPhoneNumsByExcel(InputStream inStream,
			PhoneInformationService phoneInformationService, Long blackId, Long blankId, Long whiteId)
			throws InvalidFormatException, IOException
	{
		Map<String, Object> returnMap = new HashMap<String, Object>();
		List<Integer> idList = new ArrayList<Integer>();
		List<String> phoneNumList = new ArrayList<String>();
		List<String> tempList = new ArrayList<String>();
		// 计算非手机号的数量
		int notPhoneNumCount = 0;
		// 计算重复手机号数量
		int repeatPhoneNumCount = 0;
		// 计算黑名单手机号数量
		int blackPhoneNumCount = 0;
		// 计算空号手机号数量
		int blankPhoneNumCount = 0;
		// 计算白名单手机号数量
		int whitePhoneNumCount = 0;

		// 查询出所有白名单
		List<PhoneInformation> phoneInformationList = new ArrayList<PhoneInformation>();
		Map<String, Long> param = new HashMap<String, Long>();
		param.put("blackId", blackId);
		param.put("blankId", blankId);
		phoneInformationList = phoneInformationService.selectBlackAndBlankList(param);

		SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");

		// 同时支持Excel 2003、2007
		Workbook workbook = WorkbookFactory.create(inStream); // 这种方式 Excel
		int sheetCount = workbook.getNumberOfSheets(); // Sheet的数量

		// 遍历每个Sheet
		for (int s = 0; s < sheetCount; s++)
		{

			Sheet sheet = workbook.getSheetAt(s);
			int rowCount = sheet.getPhysicalNumberOfRows(); // 获取总行数
			// 遍历每一行

			for (int r = 0; r < rowCount; r++)
			{

				Row row = sheet.getRow(r);
				int cellCount = row.getPhysicalNumberOfCells(); // 获取总列数
				// 遍历每一列
				for (int c = 0; c < cellCount; c++)
				{
					Cell cell = row.getCell(c);
					int cellType = cell.getCellType();
					String cellValue = null;

					switch (cellType)
					{
					case Cell.CELL_TYPE_STRING: // 文本
						cellValue = cell.getStringCellValue();
						if (CommonUtils.isPhone(cellValue))
						{
							if (tempList.contains(cellValue))
							{
								repeatPhoneNumCount++;
							} else
							{
								tempList.add(cellValue);
							}

							if (!CommonUtils.isEmpty(phoneInformationList))
							{
								for (int k = 0; k < phoneInformationList.size(); k++)
								{
									PhoneInformation phoneInformation = phoneInformationList.get(k);
									if (phoneInformation.getPhoneNum().equals(cellValue))
									{
										if (blackId.equals(phoneInformation.getStatusId()))
										{
											blackPhoneNumCount++;
										} else if (blankId.equals(phoneInformation.getStatusId()))
										{
											blankPhoneNumCount++;
										} else if (whiteId.equals(phoneInformation.getStatusId()))
										{
											whitePhoneNumCount++;
										}
									} else
									{
										if (!phoneNumList.contains(cellValue))
										{
											phoneNumList.add(cellValue);
											String cellValue1 = cellValue.substring(0, 7);
											idList.add(Integer.parseInt(cellValue1));
										}
									}
								}
							}
						} else
						{
							notPhoneNumCount++;
						}

						// System.out.println(cellValue);
						break;
					case Cell.CELL_TYPE_NUMERIC: // 数字、日期
						if (DateUtil.isCellDateFormatted(cell))
						{
							cellValue = fmt.format(cell.getDateCellValue()); // 日期型
						} else
						{

							cellValue = new DecimalFormat("#.##").format(cell.getNumericCellValue());
							if (CommonUtils.isPhone(cellValue))
							{
								if (tempList.contains(cellValue))
								{
									repeatPhoneNumCount++;
								} else
								{
									tempList.add(cellValue);
								}

								boolean flag = false;
								if (!CommonUtils.isEmpty(phoneInformationList))
								{

									for (int k = 0; k < phoneInformationList.size(); k++)
									{
										PhoneInformation phoneInformation = phoneInformationList.get(k);
										if (phoneInformation.getPhoneNum().equals(cellValue))
										{
											if (blackId.equals(phoneInformation.getStatusId()))
											{
												blackPhoneNumCount++;
											} else if (blankId.equals(phoneInformation.getStatusId()))
											{
												blankPhoneNumCount++;
											} else if (whiteId.equals(phoneInformation.getStatusId()))
											{
												whitePhoneNumCount++;
											}
											flag = true;
											break;
										}
									}

								}

								if (!flag)
								{
									if (!phoneNumList.contains(cellValue))
									{
										phoneNumList.add(cellValue);
										String cellValue1 = cellValue.substring(0, 7);
										idList.add(Integer.parseInt(cellValue1));
									}
								}

							} else
							{
								notPhoneNumCount++;
							}
						}
						break;
					case Cell.CELL_TYPE_BOOLEAN: // 布尔型
						cellValue = String.valueOf(cell.getBooleanCellValue());
						break;
					case Cell.CELL_TYPE_BLANK: // 空白
						cellValue = cell.getStringCellValue();
						break;
					case Cell.CELL_TYPE_ERROR: // 错误
						cellValue = "错误";
						break;
					case Cell.CELL_TYPE_FORMULA: // 公式
						cellValue = "错误";
						break;
					default:
						cellValue = "错误";
					}

				}
			}
		}

		tempList = null;
		returnMap.put("phoneNumList", phoneNumList);
		returnMap.put("idList", idList);
		returnMap.put("notPhoneNumCount", notPhoneNumCount - 1);
		returnMap.put("repeatPhoneNumCount", repeatPhoneNumCount);
		returnMap.put("blackPhoneNumCount", blackPhoneNumCount);
		returnMap.put("blankPhoneNumCount", blankPhoneNumCount);
		returnMap.put("whitePhoneNumCount", whitePhoneNumCount);
		return returnMap;
	}

	public static Map<String, Object> getPhoneInformationRedisByExcel(InputStream inStream, List<String> phoneNumList)
	{
		SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
		List<PhoneInformationRedis> list = new ArrayList<PhoneInformationRedis>();
		List<Integer> idList = new ArrayList<Integer>();

		// 同时支持Excel 2003、2007
		Workbook workbook;
		try
		{
			workbook = WorkbookFactory.create(inStream);
			int sheetCount = workbook.getNumberOfSheets(); // Sheet的数量
			IDUtils idUtils = new IDUtils(0,0);

			// 遍历每个Sheet
			for (int s = 0; s < sheetCount; s++)
			{

				Sheet sheet = workbook.getSheetAt(s);
				int rowCount = sheet.getPhysicalNumberOfRows(); // 获取总行数
				// 遍历每一行

				for (int r = 0; r < rowCount; r++)
				{

					Row row = sheet.getRow(r);
					int cellCount = row.getPhysicalNumberOfCells(); // 获取总列数
					// 遍历每一列
					for (int c = 0; c < cellCount; c++)
					{
						Cell cell = row.getCell(c);
						int cellType = cell.getCellType();
						String cellValue = null;

						switch (cellType)
						{
						case Cell.CELL_TYPE_STRING: // 文本
							cellValue = cell.getStringCellValue();
							if (CommonUtils.isPhone(cellValue) && !phoneNumList.contains(cellValue))
							{
								PhoneInformationRedis phoneInformationRedis = new PhoneInformationRedis();
								phoneInformationRedis.setId(idUtils.nextId());
								phoneInformationRedis.setPhoneNum(cellValue);
								phoneInformationRedis.setProvince("");
								phoneInformationRedis.setCity("");
								phoneInformationRedis.setOperator("");
								idList.add(Integer.parseInt(cellValue.substring(0, 7)));
								list.add(phoneInformationRedis);
							}

							// System.out.println(cellValue);
							break;
						case Cell.CELL_TYPE_NUMERIC: // 数字、日期
							if (DateUtil.isCellDateFormatted(cell))
							{
								cellValue = fmt.format(cell.getDateCellValue()); // 日期型
							} else
							{
								cellValue = new DecimalFormat("#.##").format(cell.getNumericCellValue());
								// 计算手机号
								if (CommonUtils.isPhone(cellValue) && !phoneNumList.contains(cellValue))
								{
									PhoneInformationRedis phoneInformationRedis = new PhoneInformationRedis();
									phoneInformationRedis.setId(idUtils.nextId());
									phoneInformationRedis.setPhoneNum(cellValue);
									phoneInformationRedis.setProvince("");
									phoneInformationRedis.setCity("");
									phoneInformationRedis.setOperator("");
									idList.add(Integer.parseInt(cellValue.substring(0, 7)));
									list.add(phoneInformationRedis);

								}

							}
							break;
						case Cell.CELL_TYPE_BOOLEAN: // 布尔型
							cellValue = String.valueOf(cell.getBooleanCellValue());
							break;
						case Cell.CELL_TYPE_BLANK: // 空白
							cellValue = cell.getStringCellValue();
							break;
						case Cell.CELL_TYPE_ERROR: // 错误
							cellValue = "错误";
							break;
						case Cell.CELL_TYPE_FORMULA: // 公式
							cellValue = "错误";
							break;
						default:
							cellValue = "错误";
						}

					}
				}
			}
		} catch (InvalidFormatException | IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // 这种方式 Excel

		Map<String, Object> map = new HashMap<String, Object>();
		
		List<PhoneInformationRedis> returnList = new ArrayList<PhoneInformationRedis>(
				new HashSet<PhoneInformationRedis>(list));
		Integer repeatCount = list.size() - returnList.size();
		map.put("repeatCount", repeatCount);
		map.put("phoneInformationRedisList", returnList);
		map.put("idList", new ArrayList<Integer>(new HashSet<Integer>(idList)));

		return map;

	}
}
