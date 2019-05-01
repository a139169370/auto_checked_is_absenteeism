package Config;

/*
 *		Created by IntelliJ IDEA1.
 *		User:龙猫
 *		Date: 2019/1/29
 *		Time: 21:03
 *       email: foxmaillucien@126.com
 *       Description:
 */
public interface MessageConfig {
	// 短信应用SDK AppID
	int appid = ; // 1400开头

	// 短信应用SDK AppKey
	String appkey = "";

	// 短信模板ID，需要在短信应用中申请
	// 真实的模板ID需要在短信控制台中申请
	int templateId = ;

	// 签名
	// 真实的签名需要在短信控制台中申请，另外
	// 签名参数使用的是`签名内容`，而不是`签名ID`
	String smsSign = "";

	//数据源
	String JDBC_NAME = "";		//部署时需更改

}
