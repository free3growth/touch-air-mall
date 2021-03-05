package com.touch.air.mall.order.config;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.touch.air.mall.order.vo.PayVo;
import lombok.Data;
import org.springframework.stereotype.Component;

@Component
@Data
public class AlipayTemplate {

    //在支付宝创建的应用的id

    private   String app_id = "2021000117616521";

    //商户私钥，您的PKCS8格式RSA2私钥

    private String merchant_private_key = "MIIEvwIBADANBgkqhkiG9w0BAQEFAASCBKkwggSlAgEAAoIBAQDEgcIV8YctplofvLPYh1Cex2zppFxXEMKKYZ0RWwxYIUQzX63+ydFDZHfUs1nNwErtxPii5vWlJXeOEmyFcpq3pr7esKWJOLRd/wW6YFWyGWVrLBuX9zhUa4RvhTgHc03sblcbHe5FjlRvUV48vHt2JUuGNggqMudm8l5smlxVqL9yEFpctb72jEmBIJUwr53KMJD34r2FEFI5LFkaK0TEi5Cy/4H+DOfbaqiSPouTZs8Zh9FVs59o49+hR5yvPApV46m0JbpehPHe1OUW+x697LSTwjCisW0BY0ONtHc2j5gGBLvI+TUdfDyJZyQyWHd7WMyrSt32R19FJNhcNRjzAgMBAAECggEAI2DVJ4w3cSGNAj/ABVC/VAbFdf8QXOB4ld9jHtfMjiVXGQS117RQB5kT9ehgV6myE8KtnO6F9TicoKhyf0gV0NCldoRJGT5rQq0CQ/7KDumBl9z2DtMEgjZlPoq2fqjks5dd+VTXLBCm5O+6TqcEd03Zng0mRSqq/J7QHy9fRlBtFjcdIFR1NeoHhA4VYD4TtbCtCZi9HJL0pRNRUGMZOffQGYvrUaSMYfqDLmSFyHrZs4UwUAZy5FjnyuvppcO4wYQC5XuM+8gEWGQf6lzIdKJklPEJ4qsCoyQhE4nELay8lNKdCWsR1qfJeVsN8HGoi/ANd/1HMI1zKFKGsek5IQKBgQD3t7PF1VIj2sckoa21nOQ5JUvHVQIBYyHK9CWX28xH70nZxXc9p07RYceN/RJBrYVESTJH/FZLYcTI6jLOZmAMGdSze9tzrp8Xwa1WkneYaTTPrDFg8h9LtPjDyong6dawrUAx7BqAhl9vOqKyOYOp3XySrnvCRYoPlSM1YVIiIwKBgQDLE7nphvFGIsttQFrpKQPT0sYi01/+IJXIr0a1XDeCByiLe7BjHUVNKUZNqK9PwYwCfCzAEEuJ58CYqSiJmYUWGIvAqV0p9BGtPHPmo6e4B3WgqTepv5vjEaUCsBOdzBWFhpC6z1m9/a5iZ5lDI7wGlFKHtFNeISh/4YZ2uxmS8QKBgQCEj9LtWHXHaClPqhGqcZYUGqhNuGkFUwZfdH21oVLVX/E2T5y66pCcLeY7TqV5biE15LG6ogeP+Xb0SZf8qf5WG3Cex/YtFtJeRS+8d072yb4QMEaQaaFjwymopPdZ2+kezOfgj0ezZbUmcctk8rBJs5QZbrQdx1bVderMgxMP3wKBgQChyMZc4E+JU6EDGbkTdcSB1bJZ/lHEUEtkBRds1tm18mP/s3uTircyQMasic2Y2ZXkSO8R2CmF5SbPuDv+W1rmfomf5I2/JZ2LiNTBEvJEL1UruIQK7QaRAPaXzJTkKrmeldTprtVNAjQSW3yDO43JJBMX37ZnED2Gm7IXFvnJ0QKBgQCLW56Po3SGeXVvhqwGsgSfj45G6kb7iATjrccJyw8Peh0n+HrFn1n4Zgk7PxzqSe6D1lBDYqrhy/G0i0/3n+kQjjb4SqhP1ClivrRzee2w/Rn8QP/sB3Hukb/NpsArapzLi9DLZqIlAwmi133LvWE8XmSy5L2OrwK+u5eQ7vU4jQ==";

    //支付宝公钥,查看地址：https://openhome.alipay.com/platform/keyManage.htm 对应APPID下的支付宝公钥。

    private String alipay_public_key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAmrxkS37yN2xyhCO4wzqSJ+Bic/+wyvxazw2wWhhNFT6kyx0d8mEEbRAUVL3RKtJ6OQnPoTA59CATkUYoNteYlEYMQqWWI98UB6mVsLS6aLMs0fuYSjb1b9ixs4ZivFfuj928HpXCj/r3/oZUlSocNwC8s8VfZLAmVQDSQ6DkK1uxaGpOovpVxkOxZ9YRzhXvkT2MdzQAEY+ChaSFppWCgtZw+CUrouRWT1+hcoyAewcxqvt8/oyNhNTyDNxVYNvrp0vMcTX6DUoDPpZG1aXZK72T1xzYsKbGLGUCTLdofamBPM63K3yAH9irS7yTuHrCfRvp/9zO8iXyU7FzD9gdEQIDAQAB";

    //服务器[异步通知]页面路径  需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    //支付宝会悄悄的给我们发送一个请求，告诉我们支付成功的信息

    private String notify_url = "http://jcnf8qjge7.52http.net/payed/notify";

    //页面跳转同步通知页面路径 需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    //同步通知，支付成功，一般跳转到成功页

    private String return_url = "http://member.mall.com/memberOrder.html";

    //签名方式
    private  String sign_type = "RSA2";

    //订单超时
    private  String timeout_express = "1m";

    //字符编码格式
    private  String charset = "utf-8";

    //支付宝网关； https://openapi.alipaydev.com/gateway.do
    private  String gatewayUrl = "https://openapi.alipaydev.com/gateway.do";

    public  String pay(PayVo vo) throws AlipayApiException {

        //AlipayClient alipayClient = new DefaultAlipayClient(AlipayTemplate.gatewayUrl, AlipayTemplate.app_id, AlipayTemplate.merchant_private_key, "json", AlipayTemplate.charset, AlipayTemplate.alipay_public_key, AlipayTemplate.sign_type);
        //1、根据支付宝的配置生成一个支付客户端
        AlipayClient alipayClient = new DefaultAlipayClient(gatewayUrl,
                app_id, merchant_private_key, "json",
                charset, alipay_public_key, sign_type);

        //2、创建一个支付请求 //设置请求参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(return_url);
        alipayRequest.setNotifyUrl(notify_url);

        //商户订单号，商户网站订单系统中唯一订单号，必填
        String out_trade_no = vo.getOut_trade_no();
        //付款金额，必填
        String total_amount = vo.getTotal_amount();
        //订单名称，必填
        String subject = vo.getSubject();
        //商品描述，可空
        String body = vo.getBody();

        alipayRequest.setBizContent("{\"out_trade_no\":\""+ out_trade_no +"\","
                + "\"total_amount\":\""+ total_amount +"\","
                + "\"subject\":\""+ subject +"\","
                + "\"body\":\""+ body +"\","
                + "\"timeout_express\":\""+timeout_express+"\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");

        String result = alipayClient.pageExecute(alipayRequest).getBody();

        //会收到支付宝的响应，响应的是一个页面，只要浏览器显示这个页面，就会自动来到支付宝的收银台页面
        System.out.println("支付宝的响应："+result);

        return result;

    }
}
