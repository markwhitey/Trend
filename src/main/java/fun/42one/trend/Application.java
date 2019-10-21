package cn.how2j.trend;
import java.io.IOException;
import java.net.URISyntaxException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import cn.how2j.trend.util.LogPrintStream;
import cn.hutool.core.util.NetUtil; 



@SpringBootApplication
public class Application extends SpringBootServletInitializer{
    public static void main(String[] args) throws IOException, URISyntaxException {
    	String url = "http://127.0.0.1:9888/trend/";
    	int port = 9888;
		if(NetUtil.isUsableLocalPort(port)) {
        	LogPrintStream.init();
        	System.setProperty("java.awt.headless", "false");
        	SpringApplication.run(Application.class, args);
        	System.out.println(url);
    	}
    	else {
    		System.err.printf("本项目端口号 %d 已经被占用，无法启动%n", port);
    	}

    }
    
    @Bean
    RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
