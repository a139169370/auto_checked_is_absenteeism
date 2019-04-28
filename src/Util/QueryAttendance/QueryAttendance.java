package Util.QueryAttendance;

import Data.Database;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 *		Created by IntelliJ IDEA.
 *		User:龙猫
 *		Date: 2019/2/21
 *		Time: 17:19
 *       email: foxmaillucien@126.com
 *       Description:根据用户username查询考勤
 */
public class QueryAttendance {
	static Logger logger = LogManager.getLogger(QueryAttendance.class.getName());
	public static Map<String, Integer> QueryAttendance(String username) {//根据用户username查询考勤

		Map<String, Integer> map = new HashMap<String, Integer>();
		try {
			//暂停5S，防止时间间隔过短被封IP
			Thread.sleep(5 * 1000);


			StringBuffer message = new StringBuffer();        //要返回的信息
			Database database = new Database();                //数据库对象
			String studentId = database.queryStudentId(username);        //向数据库查询学生id
			String gzcode = database.queryStudentGzcode(username);        //向数据库查询学生gzcode


			int leaveTime = 0;        //请假次数
			int lateTime = 0;        //迟到次数
			int truantTime = 0;        //旷课次数
			//如果找不到
			if (studentId == null || studentId.isEmpty() || gzcode == null || gzcode.isEmpty()) {

			}

			//个人信息查询将要跳转的url
			String personalInformationDataUrl = "http://class.sise.com.cn:7001/SISEWeb/pub/studentstatus/attendance/studentAttendanceViewAction.do?method=doMain&" +
					"studentID=" + studentId +
					"&gzcode=" + gzcode;

			//new一个HTTPClient对象
			HttpClient httpClient = new HttpClient();
			//获取个人信息页面
			GetMethod getMethod = new GetMethod(personalInformationDataUrl);

			//发送执行
			httpClient.executeMethod(getMethod);
			//将服务器返回的个人信息页面html文本保存在字符串中
			String personalInformationHtml = getMethod.getResponseBodyAsString();
			//使用完后关闭连接
			getMethod.releaseConnection();

			//使用jsoup解析
			Document document = Jsoup.parse(personalInformationHtml);

			//获取成绩
			//寻找所有table标签
			Elements tables = document.getElementsByTag("table");

			//获取第几学期
			String time = document.getElementsByAttributeValue("selected", "selected").text();

			//第六个table
			Element table = tables.get(6);
			//遍历过滤元素，找到成绩table中所有tr标签
			Elements trs = table.getElementsByTag("tr");

			message.append("======考勤信息======\n");
			message.append(time + "：\n");

			//布尔变量，用于判断考勤是否有缺勤或迟到或请假记录
			Boolean isAddMessage = false;
			for (Element tr : trs) {
				//判断第3个td是否有缺勤或迟到或请假，!=-1则说明找到，即有缺勤或迟到或请假
				if (tr.child(2).text().indexOf("请假") != -1) {
					//请假次数
					leaveTime += Integer.parseInt(tr.child(2).text().substring(3, 4));
					isAddMessage = true;
				}
				if (tr.child(2).text().indexOf("迟到") != -1) {
					//迟到次数
					lateTime += Integer.parseInt(tr.child(2).text().substring(3, 4));
					isAddMessage = true;
				}
				if (tr.child(2).text().indexOf("旷课") != -1) {
					//旷课次数
					truantTime += Integer.parseInt(tr.child(2).text().substring(3, 4));
					isAddMessage = true;
				}
			}

			//向日志插入操作记录
			logger.info("检查用户“" + username + "”数据库记录次数与教务系统是否相同；");

			//向数据库查询次数
			List<Integer> list = database.queryFromAttendance_recordWithUsername(username);

			//如果所有次数相同则说明已经发送了短信，不需要再次发送
			if (list.get(0) == leaveTime && list.get(1) == lateTime && list.get(2) == truantTime) {

				//向日志插入操作记录
				logger.info("数据库记录次数与教务系统相同，不需要发送信息；");

			} else {
				//如果不相同，则将数据写入map数组
				map.put("leaveTime", leaveTime);
				map.put("lateTime", lateTime);
				map.put("truantTime", truantTime);

				//向日志插入操作记录
				logger.info("数据库记录次数与教务系统不同，准备发送信息；");

			}
			//然后信息写入list数组并插入（为了第一次查询的时候创建数据库数据）
			List<Integer> dateList = new ArrayList<>();
			dateList.add(leaveTime);
			dateList.add(lateTime);
			dateList.add(truantTime);
			database.insertAttendance_record(username, dateList);
//			database.close();
			//如果没有有缺勤或迟到或请假记录，则添加返回信息
			if (!isAddMessage) {

			}
			return map;

		}catch (Throwable t){
			logger.error(t.getMessage(), t);
		}
		return map;
	}
}
