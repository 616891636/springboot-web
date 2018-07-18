package com.cdj.springmvc.d_controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.imooc.miaosha.redis.RedisService;
import com.imooc.miaosha.result.Result;
import com.imooc.miaosha.service.MiaoshaUserService;
import com.imooc.miaosha.vo.LoginVo;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;

@Controller
@RequestMapping("/github.io")
public class GitHubProController {

    private static Logger log = LoggerFactory.getLogger(GitHubProController.class);

    @Autowired
    RedisService redisService;

    @Value("${crawler.pixiv.url}")
    private String pixiv_url;


    //访问路径：http://localhost:8080/ajaxMix/pixiv_wall  如：http://localhost:8080/miaosha/reset
    @RequestMapping(value="/pixiv_wall", method=RequestMethod.GET)
    @ResponseBody
    //public Result<String> doLogin(HttpServletResponse response, @Valid LoginVo loginVo) {
    public Result<String> PixivWall_URL_API(HttpServletRequest request, HttpServletResponse response, ModelMap model) throws Exception {
        //  response.setHeader("Access-Control-Allow-Origin", "*");//使用jsonp可以不用该配置
        //  response.setCharacterEncoding("UTF-8"); //解决返回乱码
        JSONArray jsonArray = new JSONArray();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String date = sdf.format(new Date());
        String pixivJson = redisService.get(null,"pixiv:" + date,String.class);
/* 请求范例！！！！
   $.ajax({
                url: 'http://127.0.0.1:8081/spring/ajaxMix/getJsonp',
                type: "get",
                async: false,
                dataType: "jsonp",
                jsonp: "callback", //服务端用于接收callback调用的function名的参数
                jsonpCallback: "success_jsonpCallback", //callback的function名称,服务端会把名称和data一起传递回来
                success: function(data) {
            console.log(data);
        },
        error: function() {
            alert('Error');
        }
        });*/
//jsonp核心代码END
        String callbackFun = request.getParameter("callback");//得到js函数名称
        if (pixivJson != null && !"".equals(pixivJson)) {
            System.out.println("现在从缓存中读取Json了");
            //jsonp核心代码
            if (callbackFun != null && !"".equals(callbackFun)) {
                System.out.println("使用jsonp方式请求");
                try {
                    response.getWriter().write(callbackFun + "(" + pixivJson + ")"); //返回jsonp数据
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
            return Result.success( JSON.parseArray(pixivJson).toJSONString());
            // return  JSON.parseArray(pixivJson).toJSONString();
        }

        StringBuffer sb = new StringBuffer();
        PrintWriter out = null;
        try {
            out = response.getWriter();
            System.out.println(pixiv_url);
            Document doc = Jsoup.connect(pixiv_url).get();
//				 String title = doc.title();
//				 Element body = doc.body();
            Elements eles= doc.select(".ranking-item");
            for(Element ele :eles){
                JSONObject jsonObject=new JSONObject();
                jsonObject.put("img_no",ele.attr("data-rank-text"));
                jsonObject.put("img_title", ele.attr("data-title"));
                jsonObject.put("img_painter", ele.attr("data-user-name"));
                jsonObject.put("img_link","http://www.pixiv.net/member_illust.php?mode=medium&illust_id="+ele.attr("data-id"));
                jsonObject.put("img_src",ele.select(".ranking-image-item").select("._layout-thumbnail").select("._thumbnail").attr("data-src"));
                jsonArray.add(jsonObject);
            }

            redisService.set(null,"pixiv:" + date,jsonArray.toString());
            //redis.saveOneKeyValue("pixiv:" + date, 500000,jsonArray.toString());

        }catch (Exception e){
            e.printStackTrace();
        }
        sb.append(jsonArray);
//实现两种方式（jsonp和ajax）都可以访问
        System.out.println("每日第一次生成redis缓存");
        if (callbackFun != null && !"".equals(callbackFun)) {
            try {
                response.getWriter().write(callbackFun + "(" + sb.toString() + ")"); //返回jsonp数据
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }else {
            System.out.println("每日第一次生成redis缓存--非jsonp");
            out.write(sb.toString());
            out.flush();
            out.close();
            System.out.println(sb.toString());
            return null;
        }
    }

    @RequestMapping(value="/viewNetPage", method=RequestMethod.GET)
    @ResponseBody
    public Result<String> viewNetPage(HttpServletRequest request, HttpServletResponse response, ModelMap model) throws Exception {
        response.setHeader("Access-Control-Allow-Origin", "*");//使用jsonp可以不用该配置
        response.setCharacterEncoding("UTF-8"); //解决返回乱码
        URL url=new URL("https://www.zhihu.com/topics");//取得资源对象
        URLConnection uc=url.openConnection();//生成连接对象
        uc.setDoOutput(true);
        uc.connect(); //发出连接
        String temp;
        final StringBuffer sb = new StringBuffer();
        final BufferedReader in = new BufferedReader(new InputStreamReader(
                url.openStream(),"utf-8"));
        while ((temp = in.readLine()) != null) {
            sb.append("\n");
            sb.append(temp);
        }
        in.close();
        System.out.println(sb);

        return Result.success( sb.toString());
    }



}
