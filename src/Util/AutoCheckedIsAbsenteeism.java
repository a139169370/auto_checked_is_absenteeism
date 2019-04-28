package Util;

import Data.Database;
import Util.CheckedAndAddStudentID.CheckedUserUsernamePasswordAndClimbStudentID;
import Util.QueryAttendance.QueryAttendance;
import Util.SendMessageText.QcloudSmsTest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.http.HttpServlet;
import java.util.*;


/*
 *		Created by IntelliJ IDEA.
 *		User:龙猫
 *		Date: 2019/4/15
 *		Time: 9:26
 *       email: foxmaillucien@126.com
 *       Description:
 */
public class AutoCheckedIsAbsenteeism extends HttpServlet{
	private int times = 0;
	//4小时执行一次
	public static final long TOKEN_CHECKED_TIME=4 * 60 * 60 * 1000;

	static Logger logger = LogManager.getLogger(AutoCheckedIsAbsenteeism.class.getName());

	@Override
	public void init(){
		try {
			super.init();

			Timer t=new Timer();
			t.schedule(new MyTimer(),0,TOKEN_CHECKED_TIME);

		}catch (Throwable t){
			logger.error(t.getMessage(), t);
		}
	}
	public class MyTimer extends TimerTask {
		public void run() {
			try {
				Database database = new Database();
				//次数+1
				times++;

				//向日志文件插入记录
				logger.info("第" + times + "次自动运行程序");

				//检查账号密码正确性，并将studentID写入数据库
				CheckedAccount();
				//根据用户username查询考勤

				//向日志文件插入记录
				logger.info("获取需要检查考勤的学号");

				Map<String, List<String>> map = database.queryNeedCheckedUsernameForTableUsers();
//				database.close();
				List<String> username = map.get("username");
				List<String> phone = map.get("phone");


				//获取学号，并向数据库插入记录
				StringBuffer stringBuffer = new StringBuffer("需要检查考勤的学号为：");
				for (String getListUsername : username) {
					stringBuffer.append(getListUsername + "；");
				}

				//向日志文件插入记录
				logger.info(stringBuffer.toString());

				//如果两个数组长度相等则执行，不相等为程序错误
				if (username.size() == phone.size()) {
					//循环执行发送信息
					for (int i = 0; i < username.size(); i++) {

						Map<String, Integer> attendanceMap = QueryAttendance.QueryAttendance(username.get(i));
						if (attendanceMap.isEmpty()) {
							//如果是空的则说明系统数据与数据库数据相同，不发送信息
						} else {
							QcloudSmsTest.SendTextMessage(attendanceMap, phone.get(i));
						}
					}
				} else {
					//不相等为程序错误
					logger.error("username.size()与phone.size()不相等，致命错误！");
				}
			}catch (Throwable t){
				logger.error(t.getMessage(), t);
			}
		}
	}

	private void CheckedAccount() {
		try {
			Database database = new Database();
			//从数据库查询学生系统账号密码数组
			Map<String, List<String>> account = database.queryAllfromTableUsers();
			List<String> username = account.get("username");
			List<String> password = account.get("password");
			if (username.size() != password.size()) {
				//如果两个数组长度不等，则数据库数据非法
			}
			for (int i = 0; i < username.size(); i++) {
				Map<String, String> map = CheckedUserUsernamePasswordAndClimbStudentID.CheckedUserUsernamePasswordAndClimbStudentID(username.get(i), password.get(i));
				if (map.isEmpty() || map == null) {
					//如果map为空，则说明账号密码错误；

				}
				//获取studentID和gzcode
				String studentEncryptionID = map.get("studentID");
				String gzcode = map.get("gzcode");
				//插入用户信息
				database.insertUserMessage(username.get(i), studentEncryptionID, gzcode);
//				database.close();
			}
		}catch (Throwable t){
			logger.error(t.getMessage(), t);
		}
	}
}

