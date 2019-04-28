package Util.SendMessageText;

import Config.MessageConfig;
import com.github.qcloudsms.SmsMultiSender;
import com.github.qcloudsms.SmsMultiSenderResult;
import com.github.qcloudsms.httpclient.HTTPException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;

import java.io.IOException;
import java.util.Map;


public class QcloudSmsTest {
	static Logger logger = LogManager.getLogger(QcloudSmsTest.class.getName());
    public static void SendTextMessage(Map<String, Integer> map, String phone) {

		try {
			//向日志插入操作记录
			logger.info("准备发送信息给：" + phone);

			String leaveTime = map.get("leaveTime").toString();        //请假次数
			String lateTime = map.get("lateTime").toString();            //迟到次数
			String truantTime = map.get("truantTime").toString();        //逃课次数


			// 短信应用SDK AppID
			int appid = MessageConfig.appid; // 1400开头

			// 短信应用SDK AppKey
			String appkey = MessageConfig.appkey;

			// 需要发送短信的手机号码
//        String[] phoneNumbers = {"13724704026", "18689314636", "13480371231", "17876383199"};
			String[] phoneNumbers = {phone};

			// 短信模板ID，需要在短信应用中申请
			// 真实的模板ID需要在短信控制台中申请
			int templateId = MessageConfig.templateId;

			// 签名
			// 真实的签名需要在短信控制台中申请，另外
			// 签名参数使用的是`签名内容`，而不是`签名ID`
			String smsSign = MessageConfig.smsSign;

			// 单发短信
//        try {
//            SmsSingleSender ssender = new SmsSingleSender(appid, appkey);
//            SmsSingleSenderResult result = ssender.send(0, "86", phoneNumbers[0],
//                "【鹿与橙子公众号】您的验证码是: 5678", "", "");
//            System.out.print(result);
//        } catch (HTTPException e) {
//            // HTTP响应码错误
//            e.printStackTrace();
//        } catch (JSONException e) {
//            // json解析错误
//            e.printStackTrace();
//        } catch (IOException e) {
//            // 网络IO错误
//            e.printStackTrace();
//        }

			// 指定模板ID单发短信
			try {
				//依次为请假、迟到、逃课次数
				String[] params = {leaveTime, lateTime, truantTime};
				SmsMultiSender msender = new SmsMultiSender(appid, appkey);
				SmsMultiSenderResult result = msender.sendWithParam("86", phoneNumbers,
						templateId, params, smsSign, "", "");  // 签名参数未提供或者为空时，会使用默认签名发送短信

				//向日志插入操作记录
				logger.info("信息发送成功，返回状态为：" + result);
			} catch (HTTPException e) {
				logger.error(e.getMessage(), e);
			} catch (JSONException e) {
				logger.error(e.getMessage(), e);
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			} catch (Exception e){
				logger.error(e.getMessage(), e);
			}
		}catch (Throwable t){
			logger.error(t.getMessage(), t);
		}
    }
}
