package Data;

import Config.MessageConfig;
import org.apache.commons.collections.map.HashedMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/*
 *		Created by IntelliJ IDEA.
 *		User:龙猫
 *		Date: 2019/1/11
 *		Time: 20:53
 *       email: foxmaillucien@126.com
 *       Description:数据库类，提供向数据库操作的方法
 */
public class Database {
	static Logger logger = LogManager.getLogger(Database.class.getName());
	public Database() {
		try {

		}catch (Exception e){
			logger.error(e.getMessage(), e);
		}
	}

	//返回所有需要查询成绩的用户信息（即有存储手机号码）；
	public Map<String, List<String>> queryNeedCheckedUsernameForTableUsers(){
		Map<String, List<String>> map = new HashedMap();
		List<String> username = new ArrayList<String>();
		List<String> phone = new ArrayList<String>();
		try {
			//获取连接
			InitialContext initialContext = new InitialContext();
			DataSource dataSource = (DataSource) initialContext.lookup("java:comp/env/" + MessageConfig.JDBC_NAME);
			Connection connection = dataSource.getConnection();
			//向数据库查询
			String sql = "select username,phone from users where phone is not null";
			PreparedStatement preparedStatement = connection.prepareStatement(sql);
			ResultSet resultSet = preparedStatement.executeQuery();
			//获取全部的数据
			while (resultSet.next()){
				username.add(resultSet.getString(1));
				phone.add(resultSet.getString(2));
			}
			map.put("username", username);
			map.put("phone", phone);
			connection.close();
		}catch (Throwable t){
			logger.error(t.getMessage(), t);
		}
		return map;
	}

	public List<Integer> queryFromAttendance_recordWithUsername(String username){
		List<Integer> list = new ArrayList<Integer>();
//		长度为3，依次为请假、迟到、旷课次数；
		try {
			//获取连接
			InitialContext initialContext = new InitialContext();
			DataSource dataSource = (DataSource) initialContext.lookup("java:comp/env/" + MessageConfig.JDBC_NAME);
			Connection connection = dataSource.getConnection();
			//向数据库查询
			String sql = "select * from attendance_record where username = ?";
			PreparedStatement preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setString(1, username);
			ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()){
				list.add(resultSet.getInt(2));
				list.add(resultSet.getInt(3));
				list.add(resultSet.getInt(4));
			}else {
				list.add(0);
				list.add(0);
				list.add(0);
			}
			connection.close();
		}catch (Throwable t){
			logger.error(t.getMessage(), t);
		}
		return list;
	}

	public void insertAttendance_record(String username,List<Integer> list){
		try {
			//list长度为3，依次为请假、迟到、旷课次数；
			//获取连接
			InitialContext initialContext = new InitialContext();
			DataSource dataSource = (DataSource) initialContext.lookup("java:comp/env/" + MessageConfig.JDBC_NAME);
			Connection connection = dataSource.getConnection();
			//向数据库查询
			String sql = "select * from attendance_record where username = ?";
			PreparedStatement preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setString(1, username);
			ResultSet resultSet = preparedStatement.executeQuery();
			//如果有记录则更新记录
			if (resultSet.next()){
				String updateSql = "update attendance_record set leaveTime = ?,lateTime = ?,truantTime = ? where username = ?";
				PreparedStatement preparedStatement1 = connection.prepareStatement(updateSql);
				preparedStatement1.setInt(1, list.get(0));
				preparedStatement1.setInt(2, list.get(1));
				preparedStatement1.setInt(3, list.get(2));
				preparedStatement1.setString(4, username);
				preparedStatement1.executeUpdate();
			}else {
				//没有记录则插入记录
				String insertSql = "insert attendance_record values(?,?,?,?)";
				PreparedStatement preparedStatement2 = connection.prepareStatement(insertSql);
				preparedStatement2.setString(1, username);
				preparedStatement2.setInt(2, list.get(0));
				preparedStatement2.setInt(3, list.get(1));
				preparedStatement2.setInt(4, list.get(2));
				preparedStatement2.executeUpdate();
			}
			connection.close();
		}catch (Throwable t){
			logger.error(t.getMessage(), t);
		}
	}

	public Map<String, List<String>> queryAllfromTableUsers(){
		Map<String, List<String>> map = new HashedMap();
		List<String> username = new ArrayList<String>();
		List<String> password = new ArrayList<String>();
		List<String> phone = new ArrayList<String>();
		try {
			//获取连接
			InitialContext initialContext = new InitialContext();
			DataSource dataSource = (DataSource) initialContext.lookup("java:comp/env/" + MessageConfig.JDBC_NAME);
			Connection connection = dataSource.getConnection();
			//向数据库查询
			String sql = "select * from users";
			PreparedStatement preparedStatement = connection.prepareStatement(sql);
			ResultSet resultSet = preparedStatement.executeQuery();
			//获取全部的数据
			while (resultSet.next()){
				username.add(resultSet.getString(1));
				password.add(resultSet.getString(2));
				phone.add(resultSet.getString(3));
			}
			map.put("username", username);
			map.put("password", password);
			map.put("phone", phone);
			connection.close();
		}catch (Throwable t){
			logger.error(t.getMessage(), t);
		}
		return map;
	}

	public String queryStudentId(String username){		//根据username查询其对应的加密学生ID，不存在则返回null
		String studentId = null;
		try {
			//获取连接
			InitialContext initialContext = new InitialContext();
			DataSource dataSource = (DataSource) initialContext.lookup("java:comp/env/" + MessageConfig.JDBC_NAME);
			Connection connection = dataSource.getConnection();
			//向数据库查询
			String sql = "select studentid from user_messages where username = ? and is_delete != 1";
			PreparedStatement preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setString(1,username);
			ResultSet resultSet = preparedStatement.executeQuery();

			if (resultSet.next()){		//判断记录是否存在，存在则向上移动一行，将studentid赋值
				studentId = resultSet.getString(1);
			}else {
				return null;
			}
			connection.close();
		}catch (Throwable t){
			logger.error(t.getMessage(), t);
		}
		return studentId;
	}

	public String queryStudentGzcode(String username){		//根据用户ID查询其对应的学生gzcode，不存在则返回null
		String gzcode = null;
		try {
			//获取连接
			InitialContext initialContext = new InitialContext();
			DataSource dataSource = (DataSource) initialContext.lookup("java:comp/env/" + MessageConfig.JDBC_NAME);
			Connection connection = dataSource.getConnection();
			//向数据库查询
			String sql = "select studentid,gzcode from user_messages where username = ? and is_delete != 1";
			PreparedStatement preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setString(1,username);
			ResultSet resultSet = preparedStatement.executeQuery();

			if (resultSet.next()){		//判断记录是否存在，存在则向上移动一行，将studentid赋值
				gzcode = resultSet.getString(2);
			}else {
				return null;
			}
			connection.close();
		}catch (Throwable t){
			logger.error(t.getMessage(), t);
		}
		return gzcode;
	}


	public void insertUserMessage(String username, String studentID, String gzcode){			//向user_messages表插入用户名，学生加密ID
		try {
			//获取连接
			InitialContext initialContext = new InitialContext();
			DataSource dataSource = (DataSource) initialContext.lookup("java:comp/env/" + MessageConfig.JDBC_NAME);
			Connection connection = dataSource.getConnection();
			//插入前先查询数据库中是否有记录
			String queryExistSql = "select * from user_messages where username = ?";
			PreparedStatement queryExistStatement = connection.prepareStatement(queryExistSql);
			queryExistStatement.setString(1,username);
			ResultSet resultSet = queryExistStatement.executeQuery();
			if (resultSet.next()){			//如果有，则更新绑定状态
				String sql = "update user_messages set studentid = ?,gzcode = ?,is_delete = 0 where username = ?";
				PreparedStatement preparedStatement = connection.prepareStatement(sql);
				preparedStatement.setString(1, studentID);
				preparedStatement.setString(2, gzcode);
				preparedStatement.setString(3, username);
				preparedStatement.executeUpdate();
			}else {							//没有则执行插入
				String sql = "insert user_messages values(?,?,?,0)";
				PreparedStatement preparedStatement = connection.prepareStatement(sql);
				preparedStatement.setString(1, username);
				preparedStatement.setString(2, studentID);
				preparedStatement.setString(3, gzcode);
				preparedStatement.executeUpdate();
			}
			connection.close();
		}catch (Throwable t){
			logger.error(t.getMessage(), t);
		}
	}

//	public Connection getConnection() {
//		return connection;
//	}
//
//	public void setConnection(Connection connection) {
//		this.connection = connection;
//	}
//
//	public void close() {				//关闭连接
//		try {
//			connection.close();
//		}catch (Throwable t){
//			logger.error(t.getMessage(), t);
//		}
//	}
}
